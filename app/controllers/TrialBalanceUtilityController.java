package controllers;

import java.util.List;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosDaoConstants;
import model.Branch;
import model.Organization;
import model.Specifics;
import model.Transaction;
import model.TrialBalanceCOAItems;
import model.Users;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import java.util.logging.Level;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

// Controller for Administrative Changes on Trial Balance

public class TrialBalanceUtilityController extends BaseController {
	public static final String TRANSACTIONS_ORG_BRANCH_HQL = "select obj from Transaction obj where obj.transactionBranchOrganization.id = ?1 and obj.transactionBranch.id = ?2 and obj.transactionStatus = ?3 and obj.roundedCutPartOfNetAmount is not null and obj.presentStatus=1";
	public static final String TB_ORG_BRANCH_TXN_MAPPING_HQL = "select obj from TrialBalanceCOAItems obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.transactionId = ?3 and obj.transactionSpecifics.id = ?4 and obj.presentStatus=1";
	public static final String MAPPED_SPECIFIC_HQL = "select obj from Specifics obj where obj.organization.id = ?1 and obj.identificationForDataValid = ?2 and obj.presentStatus=1";
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	private Request request;
	// private Http.Session session = request.session();

	@Inject
	public TrialBalanceUtilityController(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result saveRoundupTrialBalanceForOrgAndBranch(final Long orgId) {
		Http.Session session = request.session();
		String email = session.getOptional("email").orElse("");
		Users user = Users.findActiveByEmail(email);
		if (user == null) {
			log.log(Level.SEVERE, "unauthorized access");
			return unauthorized();
		}
		ObjectNode result = Json.newObject();
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		try {
			if (orgId != null) {
				Organization organization = Organization.findById(orgId);
				if (organization != null) {
					Query query1 = entityManager.createQuery(MAPPED_SPECIFIC_HQL);
					query1.setParameter(1, orgId);
					query1.setParameter(2, "51");
					Specifics specificsMapped = null;
					try {
						specificsMapped = (Specifics) query1.getSingleResult();
					} catch (Exception e) {
						throw new IDOSException(IdosConstants.TB_EXCEPTION_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
								"Round-off mapping is not present.", "Cannot save TB for the Round-off");
					}

					if (specificsMapped == null) {
						result.put("Status", "Round-off mapping is not present for provided Organization");
						throw new IDOSException(IdosConstants.TB_EXCEPTION_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
								"Round-off mapping is not present.", "Cannot save TB for the Round-off");
					}
					List<Branch> branches = organization.getBranches();
					entitytransaction.begin();
					for (Branch branch : branches) {
						Query query = entityManager.createQuery(TRANSACTIONS_ORG_BRANCH_HQL);
						query.setParameter(1, orgId);
						query.setParameter(2, branch.getId());
						query.setParameter(3, IdosConstants.TXN_STATUS_ACCOUNTED);
						List<Transaction> transactionList = query.getResultList();
						for (Transaction transaction : transactionList) {
							query = entityManager.createQuery(TB_ORG_BRANCH_TXN_MAPPING_HQL);
							query.setParameter(1, orgId);
							query.setParameter(2, branch.getId());
							query.setParameter(3, transaction.getId());
							query.setParameter(4, specificsMapped.getId());
							List<TrialBalanceCOAItems> tbCOAList = query.getResultList();
							if (tbCOAList != null && tbCOAList.isEmpty()) {
								if (transaction.getTransactionPurpose()
										.getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
										|| transaction.getTransactionPurpose()
												.getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
										|| transaction.getTransactionPurpose()
												.getId() == IdosConstants.DEBIT_NOTE_CUSTOMER) {
									if (transaction.getRoundedCutPartOfNetAmount() != null
											&& transaction.getRoundedCutPartOfNetAmount() != 0.0) {
										if (transaction.getRoundedCutPartOfNetAmount() > 0) {
											TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
													transaction.getTransactionBranchOrganization(),
													transaction.getTransactionBranch(), transaction.getId(),
													transaction.getTransactionPurpose(),
													transaction.getTransactionDate(),
													transaction.getRoundedCutPartOfNetAmount(), user, entityManager,
													true);
										} else {
											TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
													transaction.getTransactionBranchOrganization(),
													transaction.getTransactionBranch(), transaction.getId(),
													transaction.getTransactionPurpose(),
													transaction.getTransactionDate(),
													transaction.getRoundedCutPartOfNetAmount(), user, entityManager,
													false);
										}
									}
								} else if (transaction.getTransactionPurpose()
										.getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
										|| transaction.getTransactionPurpose()
												.getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER
										|| transaction.getTransactionPurpose()
												.getId() == IdosConstants.CREDIT_NOTE_VENDOR) {
									if (transaction.getRoundedCutPartOfNetAmount() != null
											&& transaction.getRoundedCutPartOfNetAmount() != 0.0) {
										TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
												transaction.getTransactionBranchOrganization(),
												transaction.getTransactionBranch(), transaction.getId(),
												transaction.getTransactionPurpose(), transaction.getTransactionDate(),
												transaction.getRoundedCutPartOfNetAmount(), user, entityManager, false);
									} else {
										TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
												transaction.getTransactionBranchOrganization(),
												transaction.getTransactionBranch(), transaction.getId(),
												transaction.getTransactionPurpose(), transaction.getTransactionDate(),
												transaction.getRoundedCutPartOfNetAmount(), user, entityManager, true);
									}
								} else if (transaction.getTransactionPurpose()
										.getId() == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
									if (transaction.getRoundedCutPartOfNetAmount() != null
											&& transaction.getRoundedCutPartOfNetAmount() != 0.0) {
										if (transaction.getRoundedCutPartOfNetAmount() > 0) {
											TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
													transaction.getTransactionBranchOrganization(),
													transaction.getTransactionBranch(), transaction.getId(),
													transaction.getTransactionPurpose(),
													transaction.getTransactionDate(),
													transaction.getRoundedCutPartOfNetAmount(), user, entityManager,
													false);
										} else {
											TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
													transaction.getTransactionBranchOrganization(),
													transaction.getTransactionBranch(), transaction.getId(),
													transaction.getTransactionPurpose(),
													transaction.getTransactionDate(),
													transaction.getRoundedCutPartOfNetAmount(), user, entityManager,
													true);
										}
									}
								} else if (transaction.getTransactionPurpose()
										.getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER) {
									if (transaction.getRoundedCutPartOfNetAmount() != null
											&& transaction.getRoundedCutPartOfNetAmount() != 0.0) {
										if (transaction.getRoundedCutPartOfNetAmount() > 0) {
											TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
													transaction.getTransactionBranchOrganization(),
													transaction.getTransactionBranch(), transaction.getId(),
													transaction.getTransactionPurpose(),
													transaction.getTransactionDate(),
													transaction.getRoundedCutPartOfNetAmount(), user, entityManager,
													true);
										} else {
											TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
													transaction.getTransactionBranchOrganization(),
													transaction.getTransactionBranch(), transaction.getId(),
													transaction.getTransactionPurpose(),
													transaction.getTransactionDate(),
													transaction.getRoundedCutPartOfNetAmount(), user, entityManager,
													false);
										}
									}
								} else if (transaction.getTransactionPurpose()
										.getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
									if (transaction.getRoundedCutPartOfNetAmount() != null
											&& transaction.getRoundedCutPartOfNetAmount() != 0.0) {
										TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
												transaction.getTransactionBranchOrganization(),
												transaction.getTransactionBranch(), transaction.getId(),
												transaction.getTransactionPurpose(), transaction.getTransactionDate(),
												transaction.getRoundedCutPartOfNetAmount(), user, entityManager, true);
									} else {
										TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
												transaction.getTransactionBranchOrganization(),
												transaction.getTransactionBranch(), transaction.getId(),
												transaction.getTransactionPurpose(), transaction.getTransactionDate(),
												transaction.getRoundedCutPartOfNetAmount(), user, entityManager, false);
									}
								} else if (transaction.getTransactionPurpose()
										.getId() == IdosConstants.CREDIT_NOTE_CUSTOMER) {
									if (transaction.getRoundedCutPartOfNetAmount() != null
											&& transaction.getRoundedCutPartOfNetAmount() != 0.0) {
										if (transaction.getRoundedCutPartOfNetAmount() > 0) {
											TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
													transaction.getTransactionBranchOrganization(),
													transaction.getTransactionBranch(), transaction.getId(),
													transaction.getTransactionPurpose(),
													transaction.getTransactionDate(),
													transaction.getRoundedCutPartOfNetAmount(), user, entityManager,
													false);
										} else {
											TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
													transaction.getTransactionBranchOrganization(),
													transaction.getTransactionBranch(), transaction.getId(),
													transaction.getTransactionPurpose(),
													transaction.getTransactionDate(),
													transaction.getRoundedCutPartOfNetAmount(), user, entityManager,
													true);
										}
									}
								} else if (transaction.getTransactionPurpose()
										.getId() == IdosConstants.CANCEL_INVOICE) {
									if (transaction.getRoundedCutPartOfNetAmount() != null
											&& transaction.getRoundedCutPartOfNetAmount() != 0.0) {
										if (transaction.getRoundedCutPartOfNetAmount() > 0) {
											TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
													transaction.getTransactionBranchOrganization(),
													transaction.getTransactionBranch(), transaction.getId(),
													transaction.getTransactionPurpose(),
													transaction.getTransactionDate(),
													transaction.getRoundedCutPartOfNetAmount(), user, entityManager,
													false);
										} else {
											TRIAL_BALANCE_SERVICE.saveTrialBalanceForRoundOff(
													transaction.getTransactionBranchOrganization(),
													transaction.getTransactionBranch(), transaction.getId(),
													transaction.getTransactionPurpose(),
													transaction.getTransactionDate(),
													transaction.getRoundedCutPartOfNetAmount(), user, entityManager,
													true);
										}
									}
								}
							}
						}
					}
					entitytransaction.commit();
					result.put("status", "Succesfully updated for given Organization");
				}
			} else {
				result.put("Status", "Organization not found for provided ID");
				throw new IDOSException(IdosConstants.TB_EXCEPTION_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
						"Organization not found for provided ID.",
						"Cannot changed configuration for given Organization");
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			result.put("error", ex.getMessage());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}
}
