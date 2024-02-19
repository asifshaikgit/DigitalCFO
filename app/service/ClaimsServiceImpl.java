package service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;

import model.ClaimTransaction;
import model.IdosCountryStatesCity;
import model.IdosSubscriptionCountry;
import model.IdosSubscriptionCountryStates;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.jpa.JPAApi;
import play.libs.Json;
import javax.inject.Inject;
import com.idos.dao.ClaimsDAO;
import com.idos.dao.ClaimsDAOImpl;
import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;

import controllers.StaticController;

public class ClaimsServiceImpl implements ClaimsService {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public ObjectNode locationOnTravelType(ObjectNode result, JsonNode json,
			Users user) {
		log.log(Level.FINE, "============ Start");
		result = claimsDAO.locationOnTravelType(result, json, user);
		return result;
	}

	@Override
	public ObjectNode displayTravelEligibility(ObjectNode result,
			JsonNode json, Users user) {
		log.log(Level.FINE, ">>>> Start");
		result = claimsDAO.displayTravelEligibility(result, json, user);
		return result;
	}

	@Override
	public ObjectNode submitForApproval(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException {
		result = claimsDAO.submitForApproval(result, json, user, entityManager, entitytransaction);
		return result;
	}

	@Override
	public ObjectNode approverAction(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException {
		result = claimsDAO.approverAction(result, json, user, entityManager, entitytransaction);
		return result;
	}

	@Override
	public ObjectNode userAdvancesTxnApprovedButNotAccountedCount(
			ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) {
		result = claimsDAO.userAdvancesTxnApprovedButNotAccountedCount(result, json, user, entityManager,
				entitytransaction);
		return result;
	}

	@Override
	public ObjectNode userClaimsTransactions(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) {
		result = claimsDAO.userClaimsTransactions(result, json, user, entityManager, entitytransaction);
		return result;
	}

	@Override
	public ObjectNode exitingClaimsAdvanceTxnRefAndAmount(ObjectNode result,
			JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) {
		result = claimsDAO.exitingClaimsAdvanceTxnRefAndAmount(result, json, user, entityManager, entitytransaction);
		return result;
	}

	/*
	 * @Override
	 * public ObjectNode populateUserUnsettledTravelClaimAdvances(
	 * ObjectNode result, JsonNode json, Users user,
	 * EntityManager entityManager, EntityTransaction entitytransaction) {
	 * result=claimsDAO.populateUserUnsettledTravelClaimAdvances(result,json,user,
	 * entityManager,entitytransaction);
	 * return result;
	 * }
	 */

	/*
	 * @Override
	 * public ObjectNode displayUnsettledAdvancesDetails(ObjectNode result,
	 * JsonNode json, Users user, EntityManager entityManager,
	 * EntityTransaction entitytransaction) {
	 * result=claimsDAO.displayUnsettledAdvancesDetails(result,json,user,
	 * entityManager,entitytransaction);
	 * return result;
	 * }
	 * 
	 * 
	 * @Override
	 * public ObjectNode claimSettlementAccountantAction(ObjectNode result,
	 * JsonNode json, Users user, EntityManager entityManager,
	 * EntityTransaction entitytransaction) {
	 * result=claimsDAO.claimSettlementAccountantAction(result,json,user,
	 * entityManager,entitytransaction);
	 * return result;
	 * }
	 */

	@Override
	public ObjectNode search(Users user, String txnRefNumber, Long txnType, Long item, String status, Integer claimType,
			Integer payMode, String travelMode, String accomodationMode, String fromDate, String toDate, Long branch,
			Long project, Double fromAmount, Double toAmount, Integer remarks, Integer documents,
			Long claimSearchTxnQuestion, Integer claimSearchUserType, EntityManager entityManager)
			throws ParseException {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		if (null == user || (null == user.getId() && (null == user.getEmail() || "".equals(user.getEmail())))) {
			result.put("message", "Please login again and try.");
		} else {
			ArrayList inParams = new ArrayList();
			String roles = StaticController.getUserRoles(user);
			roles = roles.substring(0, roles.length() - 1);
			StringBuilder sbquery = new StringBuilder(
					"select obj from ClaimTransaction obj WHERE obj.transactionBranchOrganization.id=?1");
			inParams.add(user.getOrganization().getId());
			if (roles.equals("CREATOR")) {
				sbquery.append(" and obj.createdBy.id=").append(user.getId());
			} else if (roles.equals("APPROVER")) {
				sbquery.append(
						" and (obj.approverActionBy.id= ").append(user.getId())
						.append("or LOCATE('").append(user.getEmail())
						.append("', obj.approverMails) > 0 or obj.selectedAdditionalApprover= ")
						.append(user.getEmail()).append("')");
			} else if (roles.equals("CREATOR,APPROVER")) {
				sbquery.append(
						" and (obj.createdBy.id = ").append(user.getId())
						.append(" or obj.approverActionBy.id= ").append(user.getId())
						.append(" or LOCATE('").append(user.getEmail())
						.append("', obj.approverMails) > 0 or obj.selectedAdditionalApprover=").append(user.getEmail())
						.append("')");
			} else if (roles.equals("CREATOR,ACCOUNTANT")) {
				;
			} else if (roles.equals("CREATOR,CASHIER")) {
				sbquery.append(" and obj.createdBy.id=").append(user.getId());
			} else if (roles.equals("CREATOR,APPROVER,ACCOUNTANT")) {
				;
			} else if (roles.equals("CREATOR,APPROVER,CASHIER")) {
				sbquery.append(
						" and (obj.createdBy.id = ").append(user.getId())
						.append(" or obj.approverActionBy.id= ").append(user.getId())
						.append(" or LOCATE('").append(user.getEmail())
						.append("', obj.approverMails) > 0 or obj.selectedAdditionalApprover=").append(user.getEmail())
						.append("')");
			} else if (roles.equals("CREATOR,ACCOUNTANT,CASHIER")) {
				;
			} else if (roles.equals("CREATOR,APPROVER,ACCOUNTANT,CASHIER")) {
				;
			} else if (roles.equals("APPROVER,ACCOUNTANT")) {
				;
			} else if (roles.equals("APPROVER,CASHIER")) {
				sbquery.append(" and (obj.approverActionBy.id=").append(user.getId()).append("or LOCATE('")
						.append(user.getEmail()).append("', obj.approverMails) > 0 or obj.selectedAdditionalApprover='")
						.append(user.getEmail()).append("')");
			} else if (roles.equals("APPROVER,ACCOUNTANT,CASHIER")) {
				;
			} else if (roles.contains("CONTROLLER")) {
				;
			} else if (roles.contains("ACCOUNTANT")) {
				;
			} else if (roles.contains("AUDITOR")) {
				;
			}
			if (null != claimSearchTxnQuestion && claimSearchTxnQuestion != 0) {
				sbquery.append(" and obj.transactionPurpose.id = ").append(claimSearchTxnQuestion);
			}
			if (null != claimSearchUserType && claimSearchUserType != 0) {
				if (claimSearchUserType.equals(1)) {
					sbquery.append(" and obj.createdBy.id= ").append(user.getId());
				} else if (claimSearchUserType.equals(2)) {
					sbquery.append(
							" and (obj.approverActionBy.id=").append(user.getId())
							.append(" or LOCATE('").append(user.getEmail())
							.append("', obj.approverMails) > 0 or obj.selectedAdditionalApprover='")
							.append(user.getEmail()).append("')");
				} else if (claimSearchUserType.equals(3)) {
					sbquery.append(" AND obj.accountingActionBy.id=").append(user.getId());
				}
			}
			if (null != txnRefNumber && !"".equals(txnRefNumber)) {
				sbquery.append(" AND obj.transactionRefNumber = '").append(txnRefNumber).append("'");
			}
			if (null != txnType && txnType != 0) {
				sbquery.append(" AND obj.transactionPurpose.id IN (17,18,19)");
			}
			if (null != item && item != 0) {
				sbquery.append(" AND obj.advanceForExpenseItems.id = ").append(item);
			}
			if (null != status && !"".equals(status)) {
				sbquery.append(" AND obj.transactionStatus = '").append(status).append("'");
			}
			if (null != claimType && claimType != 0) {
				if (1 == claimType) {
					sbquery.append(" AND obj.transactionPurpose.id IN (15,16)");
				} else if (2 == claimType) {
					sbquery.append(" AND obj.transactionPurpose.id IN (17,18,19)");
				}
			}
			if (null != payMode && payMode != 0) {
				sbquery.append(" AND obj.receiptDetailType = ").append(payMode);
			}
			if (null != travelMode && !"".equals(travelMode) && !travelMode.contains("Please")) {
				sbquery.append("AND LOCATE('").append(travelMode).append("', obj.travelEligibilityDetails)>0");
			}
			Date fromdate, todate;
			if (null != fromDate && !"".equals(fromDate)) {
				fromdate = StaticController.mysqldf
						.parse(StaticController.mysqldf.format(StaticController.idosdf.parse(fromDate)));
				if (null == toDate || "".equals(toDate)) {
					todate = Calendar.getInstance().getTime();
				} else {
					todate = StaticController.mysqldf
							.parse(StaticController.mysqldf.format(StaticController.idosdf.parse(toDate)));
				}
				String currentDate, previousDate;
				if (todate.after(fromdate)) {
					currentDate = StaticController.mysqldf.format(todate);
					previousDate = StaticController.mysqldf.format(fromdate);
				} else {
					currentDate = StaticController.mysqldf.format(fromdate);
					previousDate = StaticController.mysqldf.format(todate);
				}
				sbquery.append(" and obj.presentStatus=1 AND obj.transactionDate <=").append(currentDate)
						.append(" AND obj.transactionDate >= ").append(previousDate);
			}
			if (null != branch && branch != 0) {
				sbquery.append(" AND obj.transactionBranch.id = ").append(branch);
			}
			if (null != project && project != 0) {
				sbquery.append(" AND obj.transactionProject.id = ").append(project);
			}
			if (null != fromAmount && null != toAmount) {
				if (fromAmount > toAmount) {
					sbquery.append(" AND obj.newAmount <= ").append(fromAmount).append(" AND obj.newAmount >= ")
							.append(toAmount);
				} else if (toAmount > fromAmount) {
					sbquery.append(" AND obj.newAmount <= ").append(toAmount).append(" AND obj.newAmount >= ")
							.append(fromAmount);
				}
			}
			if (remarks == 0) {
				sbquery.append(" AND obj.txnRemarks IS NULL");
			} else {
				sbquery.append(" AND obj.txnRemarks IS NOT NULL");
			}
			if (documents == 0) {
				sbquery.append(" AND (obj.supportingDocuments IS NULL OR obj.supportingDocuments = '')");
			} else {
				sbquery.append(" AND obj.supportingDocuments IS NOT NULL");
			}
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "HQL : " + sbquery);
			}
			ArrayNode userClaimTxnDataAn = result.putArray("userClaimTxnData");
			List<ClaimTransaction> userClaimTransactionList = genericDAO.queryWithParams(sbquery.toString(),
					entityManager, inParams);
			for (ClaimTransaction claimTransaction : userClaimTransactionList) {
				result.put("result", true);
				Long id = claimTransaction.getId() != null ? claimTransaction.getId() : null;
				Long txnPurposeId = claimTransaction.getTransactionPurpose() != null
						? claimTransaction.getTransactionPurpose().getId()
						: null;
				String branchName = claimTransaction.getTransactionBranch() != null
						? claimTransaction.getTransactionBranch().getName()
						: "";
				String projectName = claimTransaction.getTransactionProject() != null
						? claimTransaction.getTransactionProject().getName()
						: "";
				String txnQuestionName = claimTransaction.getTransactionPurpose() != null
						? claimTransaction.getTransactionPurpose().getTransactionPurpose()
						: "";
				String txnOrgnName = claimTransaction.getTransactionBranchOrganization() != null
						? claimTransaction.getTransactionBranchOrganization().getName()
						: "";
				String travelType = claimTransaction.getTravelType() != null ? claimTransaction.getTravelType() : "";
				String noOfPlacesToVisit = claimTransaction.getNumberOfPlacesToVisit() != null
						? claimTransaction.getNumberOfPlacesToVisit().toString()
						: "";
				String placesSelectedOrEntered = claimTransaction.getTravelFromToPlaces() != null
						? claimTransaction.getTravelFromToPlaces()
						: "";
				String typeOfCity = claimTransaction.getTypeOfCity() != null ? claimTransaction.getTypeOfCity() : "";
				String appropriateDiatance = claimTransaction.getAppropriateDistance() != null
						? claimTransaction.getAppropriateDistance()
						: "";
				String totalDays = claimTransaction.getTotalDays() != null ? claimTransaction.getTotalDays().toString()
						: "";
				String travelDetailedConfDescription = claimTransaction.getTravelEligibilityDetails() != null
						? claimTransaction.getTravelEligibilityDetails()
						: "";
				String existingClaimsCurrentSettlementDetails = claimTransaction
						.getExistingClaimsCurrentSettlementDetails() != null
								? claimTransaction.getExistingClaimsCurrentSettlementDetails()
								: "";
				String claimuserAdvanveEligibility = claimTransaction.getAdvanceEligibilityDetails() != null
						? claimTransaction.getAdvanceEligibilityDetails()
						: "";
				String userExpenditureOnThisTxn = claimTransaction.getUserExpenditureOnThisTxn() != null
						? claimTransaction.getUserExpenditureOnThisTxn()
						: "";
				Double existingAdvance = claimTransaction.getExistingAdvance() != null
						? claimTransaction.getExistingAdvance()
						: 0.0;
				Double adjustedAdvance = claimTransaction.getAdjustedAdvance() != null
						? claimTransaction.getAdjustedAdvance()
						: 0.0;
				Double netSettlementAmount = claimTransaction.getClaimsNetSettlement() != null
						? claimTransaction.getClaimsNetSettlement()
						: null;
				Double dueSettlementAmount = claimTransaction.getClaimsDueSettlement() != null
						? claimTransaction.getClaimsDueSettlement()
						: null;
				Double requiredSettlement = claimTransaction.getClaimsRequiredSettlement() != null
						? claimTransaction.getClaimsRequiredSettlement()
						: null;
				Double returnSettlement = claimTransaction.getClaimsReturnSettlement() != null
						? claimTransaction.getClaimsReturnSettlement()
						: null;
				Double enteredAdvance = claimTransaction.getGrossAmount() != null ? claimTransaction.getGrossAmount()
						: 0.0;
				Double totalAdvance = claimTransaction.getNewAmount() != null ? claimTransaction.getNewAmount() : 0.0;
				String purposeOfVisit = claimTransaction.getPurposeOfVisit() != null
						? claimTransaction.getPurposeOfVisit()
						: "";
				String claimTxnRemarks = claimTransaction.getTxnRemarks() != null ? claimTransaction.getTxnRemarks()
						: "";
				String supportingDoc = claimTransaction.getSupportingDocuments() != null
						? claimTransaction.getSupportingDocuments()
						: "";
				String claimdebitCredit = claimTransaction.getDebitCredit() != null ? claimTransaction.getDebitCredit()
						: "";
				String claimTxnStatus = claimTransaction.getTransactionStatus() != null
						? claimTransaction.getTransactionStatus()
						: "";
				String claimapproverEmails = claimTransaction.getApproverMails() != null
						? claimTransaction.getApproverMails()
						: "";
				String additionalApprovalEmails = claimTransaction.getAdditionalApproverEmails() != null
						? claimTransaction.getAdditionalApproverEmails()
						: "";
				String claimselectedAdditionalApproval = claimTransaction.getSelectedAdditionalApprover() != null
						? claimTransaction.getSelectedAdditionalApprover()
						: "";
				String creatorLabel = "Created By:";
				String createdBy = claimTransaction.getCreatedBy() != null ? claimTransaction.getCreatedBy().getEmail()
						: "";
				String approverLabel = "Approved By:";
				String approvedBy = claimTransaction.getApproverActionBy() != null
						? claimTransaction.getApproverActionBy().getEmail()
						: "";
				String accountantEmails = claimTransaction.getAccountantEmails() != null
						? claimTransaction.getAccountantEmails()
						: "";
				String accountedLabel = "Accounted By:";
				String accountedBy = claimTransaction.getAccountingActionBy() != null
						? claimTransaction.getAccountingActionBy().getEmail()
						: "";
				String transactionDate = claimTransaction.getTransactionDate() != null
						? StaticController.idosdf.format(claimTransaction.getTransactionDate())
						: "";
				String txnSpecialStatus = "";
				if (claimTransaction.getKlFollowStatus() != null) {
					if (claimTransaction.getKlFollowStatus() == 0) {
						txnSpecialStatus = "Rules Not Followed";
					}
				}
				String paymentMode = "";
				if (claimTransaction.getReceiptDetailType() != null) {
					if (claimTransaction.getReceiptDetailType() == 1) {
						paymentMode = "CASH";
					}
					if (claimTransaction.getReceiptDetailType() == 2) {
						paymentMode = "BANK";
					}
				}
				Double expenseAdvanceTotalAdvanceAmount = claimTransaction.getTotalAdvanceForExpenseDuringTxn() != null
						? claimTransaction.getTotalAdvanceForExpenseDuringTxn()
						: 0.0;
				String expenseAdvancepurposeOfExpenseAdvance = claimTransaction.getPurposeOfAdvance() != null
						? claimTransaction.getPurposeOfAdvance()
						: "";
				String itemName = claimTransaction.getAdvanceForExpenseItems() != null
						? claimTransaction.getAdvanceForExpenseItems().getName()
						: "";
				String itemParticularName = claimTransaction.getAdvanceForExpenseItemsParticulars() != null
						? claimTransaction.getAdvanceForExpenseItemsParticulars().getName()
						: "";
				String parentSpecificName = "";
				if (claimTransaction.getAdvanceForExpenseItems() != null) {
					if (claimTransaction.getAdvanceForExpenseItems().getParentSpecifics() != null) {
						parentSpecificName = claimTransaction.getAdvanceForExpenseItems().getParentSpecifics()
								.getName();
					}
				}
				Double dueFromCompany = claimTransaction.getClaimsRequiredSettlement() != null
						? claimTransaction.getClaimsRequiredSettlement()
						: 0.0;
				Double dueToCompany = claimTransaction.getClaimsReturnSettlement() != null
						? claimTransaction.getClaimsReturnSettlement()
						: 0.0;
				Double amountReturnInCaseOfDueToCompany = claimTransaction.getAmountReturnInCaseOfDueToCompany() != null
						? claimTransaction.getAmountReturnInCaseOfDueToCompany()
						: 0.0;
				ObjectNode event = Json.newObject();

				event.put("userroles", roles);
				event.put("id", id);
				event.put("txnPurposeId", txnPurposeId);
				event.put("branchName", branchName);
				event.put("projectName", projectName);
				event.put("txnQuestionName", txnQuestionName);
				event.put("txnOrgnName", txnOrgnName);
				event.put("claimtravelType", travelType);
				event.put("claimnoOfPlacesToVisit", noOfPlacesToVisit);
				event.put("claimplacesSelectedOrEntered", placesSelectedOrEntered);
				event.put("claimtypeOfCity", typeOfCity);
				event.put("claimappropriateDiatance", appropriateDiatance);
				event.put("claimtotalDays", totalDays);
				event.put("claimtravelDetailedConfDescription", travelDetailedConfDescription);
				event.put("existingClaimsCurrentSettlementDetails", existingClaimsCurrentSettlementDetails);
				event.put("claimexistingAdvance", IdosConstants.decimalFormat.format(existingAdvance));
				event.put("claimuserAdvanveEligibility", claimuserAdvanveEligibility);
				event.put("userExpenditureOnThisTxn", userExpenditureOnThisTxn);
				event.put("claimadjustedAdvance", IdosConstants.decimalFormat.format(adjustedAdvance));
				event.put("claimenteredAdvance", IdosConstants.decimalFormat.format(enteredAdvance));
				event.put("claimtotalAdvance", IdosConstants.decimalFormat.format(totalAdvance));
				event.put("netSettlementAmount", IdosConstants.decimalFormat.format(netSettlementAmount));
				event.put("dueSettlementAmount", IdosConstants.decimalFormat.format(dueSettlementAmount));
				event.put("requiredSettlement", IdosConstants.decimalFormat.format(requiredSettlement));
				event.put("returnSettlement", IdosConstants.decimalFormat.format(returnSettlement));
				event.put("claimpurposeOfVisit", purposeOfVisit);
				event.put("claimtxnRemarks", claimTxnRemarks);
				event.put("claimsupportingDoc", supportingDoc);
				event.put("debitCredit", claimdebitCredit);
				event.put("claimTxnStatus", claimTxnStatus);
				event.put("approverEmails", claimapproverEmails);
				event.put("additionalApprovarUsers", additionalApprovalEmails);
				event.put("selectedAdditionalApproval", claimselectedAdditionalApproval);
				event.put("creatorLabel", creatorLabel);
				event.put("createdBy", createdBy);
				event.put("transactionDate", transactionDate);
				event.put("approverLabel", approverLabel);
				event.put("approvedBy", approvedBy);
				event.put("accountantEmails", accountantEmails);
				event.put("accountedLabel", accountedLabel);
				event.put("accountedBy", accountedBy);
				event.put("txnSpecialStatus", txnSpecialStatus);
				event.put("paymentMode", paymentMode);
				event.put("expenseAdvanceTotalAdvanceAmount", expenseAdvanceTotalAdvanceAmount);
				event.put("expenseAdvancepurposeOfExpenseAdvance", expenseAdvancepurposeOfExpenseAdvance);
				event.put("itemName", itemName);
				event.put("itemParticularName", itemParticularName);
				event.put("parentSpecificName", parentSpecificName);
				event.put("dueFromCompany", IdosConstants.decimalFormat.format(dueFromCompany));
				event.put("dueToCompany", IdosConstants.decimalFormat.format(dueToCompany));
				event.put("amountReturnInCaseOfDueToCompany",
						IdosConstants.decimalFormat.format(amountReturnInCaseOfDueToCompany));
				event.put("instrumentNumber",
						claimTransaction.getInstrumentNumber() == null ? "" : claimTransaction.getInstrumentNumber());
				event.put("instrumentDate",
						claimTransaction.getInstrumentDate() == null ? "" : claimTransaction.getInstrumentDate());
				userClaimTxnDataAn.add(event);
			}
			result.put("result", true);
			result.remove("message");
		}
		log.log(Level.FINE, "============ End " + result);
		return result;
	}

	@Override
	public ObjectNode getCountries(Users user, Integer claimTravelType, String name, EntityManager entityManager) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		if (claimTravelType != null && claimTravelType != 0 && null != name && !"".equals(name) && name.length() > 1) {
			ArrayNode fromToOptionAn = result.putArray("countryStateCityList");
			StringBuilder query = null;
			if (claimTravelType.equals(1)) {
				// domestic
				if (user.getOrganization().getCountry() != null) {
					if (user.getBranch().getCountry() != null) {
						query = new StringBuilder();
						query.append("SELECT obj FROM IdosSubscriptionCountryStates obj WHERE obj.presentStatus = 1");
						query.append(" AND obj.idosSubscriptionCountry.id = ")
								.append(user.getBranch().getCountry().longValue());
						query.append(" AND obj.stateName LIKE '%").append(name).append("%'");
						List<IdosSubscriptionCountryStates> domesticCountryStates = genericDAO
								.executeSimpleQuery(query.toString(), entityManager);
						for (IdosSubscriptionCountryStates idosSubsCtryStates : domesticCountryStates) {
							fromToOptionAn.add(idosSubsCtryStates.getStateName());
						}
						query = new StringBuilder();
						query.append("SELECT obj FROM IdosCountryStatesCity obj WHERE obj.presentStatus = 1");
						query.append(" AND obj.idosSubscriptionCountry.id = ")
								.append(user.getBranch().getCountry().longValue());
						query.append(" AND obj.cityName LIKE '%").append(name).append("%'");
						List<IdosCountryStatesCity> domesticCountryCity = genericDAO
								.executeSimpleQuery(query.toString(), entityManager);
						for (IdosCountryStatesCity idosCtryStatesCities : domesticCountryCity) {
							fromToOptionAn.add(idosCtryStatesCities.getCityName());
						}
					} else {
						query = new StringBuilder();
						query.append("SELECT obj FROM IdosSubscriptionCountryStates obj WHERE obj.presentStatus = 1");
						query.append(" AND obj.idosSubscriptionCountry.id = ")
								.append(user.getOrganization().getCountry().longValue());
						query.append(" AND obj.stateName LIKE '%").append(name).append("%'");
						List<IdosSubscriptionCountryStates> domesticCountryStates = genericDAO
								.executeSimpleQuery(query.toString(), entityManager);
						for (IdosSubscriptionCountryStates idosSubsCtryStates : domesticCountryStates) {
							fromToOptionAn.add(idosSubsCtryStates.getStateName());
						}
						query = new StringBuilder();
						query.append("SELECT obj FROM IdosCountryStatesCity obj WHERE obj.presentStatus = 1");
						query.append(" AND obj.idosSubscriptionCountry.id = ")
								.append(user.getOrganization().getCountry().longValue());
						query.append(" AND obj.cityName LIKE '%").append(name).append("%'");
						List<IdosCountryStatesCity> domesticCountryCity = genericDAO
								.executeSimpleQuery(query.toString(), entityManager);
						for (IdosCountryStatesCity idosCtryStatesCities : domesticCountryCity) {
							fromToOptionAn.add(idosCtryStatesCities.getCityName());
						}
					}
				} else {
					if (user.getBranch().getCountry() != null) {
						query = new StringBuilder();
						query.append("SELECT obj FROM IdosSubscriptionCountryStates obj WHERE obj.presentStatus = 1");
						query.append(" AND obj.idosSubscriptionCountry.id = ")
								.append(user.getBranch().getCountry().longValue());
						query.append(" AND obj.stateName LIKE '%").append(name).append("%'");
						List<IdosSubscriptionCountryStates> domesticCountryStates = genericDAO
								.executeSimpleQuery(query.toString(), entityManager);
						for (IdosSubscriptionCountryStates idosSubsCtryStates : domesticCountryStates) {
							fromToOptionAn.add(idosSubsCtryStates.getStateName());
						}
						query = new StringBuilder();
						query.append("SELECT obj FROM IdosCountryStatesCity obj WHERE obj.presentStatus = 1");
						query.append(" AND obj.idosSubscriptionCountry.id = ")
								.append(user.getBranch().getCountry().longValue());
						query.append(" AND obj.cityName LIKE '%").append(name).append("%'");
						List<IdosCountryStatesCity> domesticCountryCity = genericDAO
								.executeSimpleQuery(query.toString(), entityManager);
						for (IdosCountryStatesCity idosCtryStatesCities : domesticCountryCity) {
							fromToOptionAn.add(idosCtryStatesCities.getCityName());
						}
					} else {
						query = new StringBuilder();
						query.append("SELECT obj FROM IdosSubscriptionCountryStates obj WHERE obj.presentStatus = 1");
						query.append(" AND obj.idosSubscriptionCountry.id = ").append(91L);
						query.append(" AND obj.stateName LIKE '%").append(name).append("%'");
						List<IdosSubscriptionCountryStates> domesticCountryStates = genericDAO
								.executeSimpleQuery(query.toString(), entityManager);
						for (IdosSubscriptionCountryStates idosSubsCtryStates : domesticCountryStates) {
							fromToOptionAn.add(idosSubsCtryStates.getStateName());
						}
						query = new StringBuilder();
						query.append("SELECT obj FROM IdosCountryStatesCity obj WHERE obj.presentStatus = 1");
						query.append(" AND obj.idosSubscriptionCountry.id = ").append(91L);
						query.append(" AND obj.cityName LIKE '%").append(name).append("%'");
						List<IdosCountryStatesCity> domesticCountryCity = genericDAO
								.executeSimpleQuery(query.toString(), entityManager);
						for (IdosCountryStatesCity idosCtryStatesCities : domesticCountryCity) {
							fromToOptionAn.add(idosCtryStatesCities.getCityName());
						}
					}
				}
				result.put("result", true);
			} else if (claimTravelType.equals(2)) {
				// international
				query = new StringBuilder();
				query.append("SELECT obj FROM IdosSubscriptionCountry obj WHERE obj.presentStatus = 1");
				query.append(" AND obj.countryName LIKE '%").append(name).append("%'");
				List<IdosSubscriptionCountry> idosSubscriptionCountry = genericDAO.executeSimpleQuery(query.toString(),
						entityManager);
				for (IdosSubscriptionCountry idosSubscriptionCountryEntity : idosSubscriptionCountry) {
					fromToOptionAn.add(idosSubscriptionCountryEntity.getCountryName());
				}
				query = new StringBuilder();
				query.append("SELECT obj FROM IdosSubscriptionCountryStates obj WHERE obj.presentStatus = 1");
				query.append(" AND obj.stateName LIKE '%").append(name).append("%'");
				List<IdosSubscriptionCountryStates> countryStates = genericDAO.executeSimpleQuery(query.toString(),
						entityManager);
				for (IdosSubscriptionCountryStates idosSubsCtryStates : countryStates) {
					fromToOptionAn.add(idosSubsCtryStates.getStateName());
				}
				query = new StringBuilder();
				query.append("SELECT obj FROM IdosCountryStatesCity obj WHERE obj.presentStatus = 1");
				query.append(" AND obj.cityName LIKE '%").append(name).append("%'");
				List<IdosCountryStatesCity> countryStateCity = genericDAO.executeSimpleQuery(query.toString(),
						entityManager);
				for (IdosCountryStatesCity idosCtryStatesCities : countryStateCity) {
					fromToOptionAn.add(idosCtryStatesCities.getCityName());
				}
				result.put("result", true);
			}
			// try{
			// MemcachedClient memcacheClient = new MemcachedClient(new
			// InetSocketAddress("localhost", 11211));
			// memcacheClient.delete("interNationalOptions");
			// if(memcacheClient.get("interNationalOptions")==null){
			// fromToOptionStr=(StringBuilder) memcacheClient.get("interNationalOptions");
			// }
			// }catch(Exception ex){
			// log.log(Level.SEVERE, "Error", ex);
			// }
		}
		/*
		 * if (fromToOptionStr != null && !fromToOptionStr.equals("")) {
		 * result.put("result", true);
		 * ObjectNode efromToOptionRow = Json.newObject();
		 * efromToOptionRow.put("fromToOptionStr", fromToOptionStr.toString());
		 * efromToOptionRow.put("type", claimTravelType);
		 * fromToOptionAn.add(efromToOptionRow);
		 * }
		 */
		return result;
	}

	@Override
	public void saveClaimTransactionBRSDate(Users user, EntityManager entityManager,
			EntityTransaction entitytransaction, String transactionRef, String brsBankDate) {
		claimsDAO.saveClaimBRSDate(user, entityManager, entitytransaction, transactionRef, brsBankDate);
	}
}
