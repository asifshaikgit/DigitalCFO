package com.idos.dao;

import actor.CreatorActor;
import com.idos.util.CodeHelper;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;

import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;

import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.text.ParseException;
import java.util.*;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import akka.actor.*;
import static service.BaseService.invDAO;
import akka.NotUsed;

/**
 * Created by Sunil Namdev on 12-03-2018.
 */
public class InterBranchTransferDAOImpl implements InterBranchTransferDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public Transaction submitForApproval(Users user, JsonNode json, EntityManager em, EntityTransaction et,
			TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException {
		Transaction txn = new Transaction();
		try {
			Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
					: json.findValue("txnEntityID").asLong();
			Double totalTxnTaxAmt = json.findValue("totalTxnTaxAmt") == null ? 0.0
					: json.findValue("totalTxnTaxAmt").asDouble();
			long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
			String txnFromBranch = json.findValue("txnFromBranch").asText();
			String txnToBranch = json.findValue("txnToBranch").asText();
			String sourceGstin = json.findValue("txnSourceGstin").asText();
			String destinGstin = json.findValue("txnDestinGstin").asText();
			String txnNetAmountDescription = json.findValue("txnNetAmountDescription").asText();
			int inOrOutWord = json.findValue("txnTransferType") == null ? 0 : json.findValue("txnTransferType").asInt();
			Transaction outWordTxn = null;

			Double totalTxnGrossAmt = json.findValue("totalTxnGrossAmt") == null ? 0.0
					: json.findValue("totalTxnGrossAmt").asDouble();
			Double txnTotalNetAmount = json.findValue("txnTotalNetAmount") == null ? 0.0
					: json.findValue("txnTotalNetAmount").asDouble();
			Double netAmountTotalWithDecimalValue = json.findValue("totalTxnNetAmtWithDecimalValue").asDouble();
			String txnremarks = json.findValue("txnRemarks").asText();
			String supportingdoc = json.findValue("supportingdoc").asText();
			String txnForItemStr = json.findValue("txnForItem").toString();

			String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
			Date txnDate = null;
			try {
				if (selectedTxnDate != null) {
					txnDate = IdosConstants.IDOSDF.parse(selectedTxnDate);
				} else {
					txnDate = new Date();
					;
				}
			} catch (ParseException e) {
				throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
						IdosConstants.NULL_KEY_EXC_ESMF_MSG,
						"cannot parse date: " + selectedTxnDate + " " + e.getMessage());
			}

			JSONArray arrJSON = new JSONArray(txnForItemStr);
			String txnDocument = "";
			String txnRemarks = "";
			Branch fromBranch = null;
			Branch toBranch = null;
			String branchName = "";
			String itemName = "";
			String txnInstrumentNumber = "";
			String txnInstrumentDate = "";
			TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);

			if (inOrOutWord == 2) {
				long outWordTxnID = json.findValue("txnSaleEntityID").asLong();
				outWordTxn = Transaction.findById(outWordTxnID);
				if (outWordTxn == null) {
					throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
							"Outword transaction not found for id: " + outWordTxnID, IdosConstants.RECORD_NOT_FOUND);
				} else {
					txn.setLinkedTxnRef(outWordTxn.getTransactionRefNumber());
				}
			}

			if (txnFromBranch != null && !txnFromBranch.equals("")) {
				fromBranch = Branch.findById(IdosUtil.convertStringToLong(txnFromBranch));
				branchName = fromBranch.getName();
			}
			if (txnToBranch != null && !txnToBranch.equals("")) {
				toBranch = Branch.findById(IdosUtil.convertStringToLong(txnToBranch));
				branchName += " to " + toBranch.getName();
			}

			txn.setPaymentStatus("NOT-PAID");
			txn.setCustomerDuePayment(txnTotalNetAmount);

			for (int i = 0; i < arrJSON.length(); i++) {
				JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
				String itemIdList = rowItemData.getString("txnItems");
			}

			// Enter data for first item in txn table to be displayed in Transaction list
			JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
			Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
			Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
			Long itemIdRow0 = firstRowItemData.getLong("txnItems");
			Specifics specifics = Specifics.findById(itemIdRow0);
			txn.setTypeIdentifier(inOrOutWord);
			txn.setTransactionSpecifics(specifics);
			txn.setTransactionParticulars(specifics.getParticularsId());
			txn.setNoOfUnits(txnNoOfUniRow0t);
			txn.setPricePerUnit(txnPerUnitPriceRow0);
			txn.setGrossAmount(totalTxnGrossAmt);
			txn.setSourceGstin(sourceGstin);
			txn.setDestinationGstin(destinGstin);
			txn.setTransactionPurpose(usertxnPurpose);
			txn.setTransactionBranch(fromBranch);
			txn.setTransactionToBranch(toBranch);
			txn.setTransactionBranchOrganization(user.getOrganization());
			txn.setNetAmount(txnTotalNetAmount);
			txn.setNetAmountResultDescription(txnNetAmountDescription);
			Double roundedCutPartOfNetAmount = txnTotalNetAmount - netAmountTotalWithDecimalValue;
			txn.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
			txn.setTransactionDate(txnDate);
			if (!txnremarks.equals("") && txnremarks != null) {
				txnRemarks = user.getEmail() + "#" + txnremarks;
				txn.setRemarks(txnRemarks);
				txnRemarks = txn.getRemarks();
			}
			txn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
					user.getEmail(), supportingdoc, user, em));
			txn.setTransactionStatus("Require Approval");
			// list of additional users all approver role users of thet organization
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("role.name", "APPROVER");
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);
			String approverEmails = "";
			String additionalApprovarUsers = "";
			String selectedAdditionalApproval = "";
			for (UsersRoles usrRoles : approverRole) {
				additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
				criterias.clear();
				criterias.put("user.id", usrRoles.getUser().getId());
				criterias.put("userRights.id", 2L);
				criterias.put("branch.id", toBranch.getId());
				criterias.put("presentStatus", 1);
				UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class, criterias,
						em);
				if (userHasRightInBranch != null) {
					// check for right in chart of accounts
					criterias.clear();
					criterias.put("user.id", usrRoles.getUser().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("specifics.id", txn.getTransactionSpecifics().getId());
					criterias.put("presentStatus", 1);
					UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class, criterias,
							em);
					if (userHasRightInCOA != null) {
						approverEmails += usrRoles.getUser().getEmail() + ",";
					}
				}
			}
			txn.setApproverEmails(approverEmails);
			txn.setAdditionalApproverEmails(additionalApprovarUsers);
			String refNo = CodeHelper.getForeverUniqueID("TXN", null);
			txn.setTransactionRefNumber(refNo);
			genericDao.saveOrUpdate(txn, user, em);
			FILE_UPLOAD_DAO.updateUploadFileLogs(em, user, supportingdoc, txn.getId(), IdosConstants.MAIN_TXN_TYPE);
			// Enter multiple items data into TransactionItems table
			if (txnEntityID > 0) {
				transactionItemsService.updateMultipleItemsTransactionItems(em, user, arrJSON, txn);
			} else {
				transactionItemsService.insertMultipleItemsTransactionItems(em, user, arrJSON, txn, txnDate);
			}
			genericDao.saveOrUpdate(txn, user, em); // need to update becuase of desgin issue, as need to set total
													// taxes amount
			if (inOrOutWord == 2) {
				invDAO.saveInvoiceLog(user, em, txn, null, json); // set popup addinal details.
			}
			et.commit();
			if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
				// Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
				// Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
				// for (int i = 0; i < keyArray.length; i++) {
				// List<Users> orgusers = Users.findByEmailActDeact(em, (String) keyArray[i]);
				// if (!orgusers.isEmpty()
				// && orgusers.get(0).getOrganization().getId() ==
				// user.getOrganization().getId()) {
				// orgtxnregistereduser.put(keyArray[i].toString(),
				// CreatorActor.expenseregistrered.get(keyArray[i]));
				// }
				// }

				String itemParentName = "";
				if (txn.getTransactionSpecifics().getParentSpecifics() != null
						&& !txn.getTransactionSpecifics().getParentSpecifics().equals("")) {
					itemParentName = txn.getTransactionSpecifics().getParentSpecifics().getName();
				} else {
					itemParentName = txn.getTransactionSpecifics().getParticularsId().getName();
				}
				String approverEmail = "";
				String approverLabel = "";
				if (txn.getApproverActionBy() != null) {
					approverLabel = "APPROVER:";
					approverEmail = txn.getApproverActionBy().getEmail();
				}
				String txnSpecialStatus = "";
				if (txn.getTransactionExceedingBudget() != null && txn.getKlFollowStatus() != null) {
					if (txn.getTransactionExceedingBudget() == 1 && txn.getKlFollowStatus() == 0) {
						txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
					}
					if (txn.getTransactionExceedingBudget() == 1 && txn.getKlFollowStatus() == 1) {
						txnSpecialStatus = "Transaction Exceeding Budget";
					}
				}
				if (txn.getTransactionExceedingBudget() == null && txn.getKlFollowStatus() != null) {
					if (txn.getKlFollowStatus() == 0) {
						txnSpecialStatus = "Rules Not Followed";
					}
				}
				if (txn.getTransactionExceedingBudget() != null && txn.getKlFollowStatus() == null) {
					txnSpecialStatus = "Transaction Exceeding Budget";
				}
				String txnResultDesc = "";
				if (txn.getNetAmountResultDescription() != null
						&& !txn.getNetAmountResultDescription().equals("null")) {
					txnResultDesc = txn.getNetAmountResultDescription();
				}
				Integer typeOfSupply = txn.getTypeOfSupply() == null ? 0 : txn.getTypeOfSupply();
				txnDocument = txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs();
				TransactionViewResponse.addActionTxn(txn.getId(), branchName, "", itemName, itemParentName, "", "", "",
						"", "",
						txn.getTransactionPurpose().getTransactionPurpose(),
						IdosConstants.idosdf.format(txn.getTransactionDate()), "", "", "", txn.getNoOfUnits(),
						txn.getPricePerUnit(), txn.getGrossAmount(), txn.getNetAmount(), txnResultDesc, "",
						txn.getTransactionStatus(), txn.getCreatedBy().getEmail(), approverLabel, approverEmail,
						txnDocument, txnRemarks, "", approverEmails, additionalApprovarUsers,
						selectedAdditionalApproval, txnSpecialStatus, 0d, "", "", "",
						txn.getTransactionPurpose().getId(), "", refNo, inOrOutWord, txn.getTransactionRefNumber(), 0l,
						0.0, 0, typeOfSupply, result);
			}
			// Single User
			if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
				ArrayNode singleUserAccounting = result.putArray("singleUserAccounting");
				ObjectNode createSingleuserJson = SINGLE_USER_DAO.createSingleuserJson(txn, json, user);
				singleUserAccounting.add(createSingleuserJson);
			}

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on Transfer Inventory- submit for approval", ex.getMessage());
		}
		return txn;
	}

	/**
	 *
	 * @param tb
	 * @param user
	 * @param fromDate
	 * @param toDate
	 * @param em
	 * @param itemSpecifics
	 * @param vendorType    1- vendor, 2 - customer, type=1 means vendors to whom we
	 *                      have paid advance
	 */
	@Override
	public void getTrialBalanceInterBranchTotal(TrialBalance tb, Users user, String fromDate, String toDate,
			Long branchId, EntityManager em) {
		String hql = null;
		if (branchId != null && branchId > 0) {
			hql = IB_TB_TOTAL_BRANCH_JPQL;
		} else {
			hql = IB_TB_TOTAL_ORG_JPQL;
		}
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "SQL: " + hql);
		}
		Query query = em.createQuery(hql);
		if (branchId != null && branchId > 0) {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, branchId);
			query.setParameter(3, fromDate);
			query.setParameter(4, user.getOrganization().getId());
			query.setParameter(5, branchId);
			query.setParameter(6, fromDate);
			query.setParameter(7, toDate);
			query.setParameter(8, user.getOrganization().getId());
			query.setParameter(9, branchId);
		} else {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, fromDate);
			query.setParameter(3, user.getOrganization().getId());
			query.setParameter(4, user.getOrganization().getId());
			query.setParameter(5, fromDate);
			query.setParameter(6, toDate);
		}
		List<Object[]> txnLists = query.getResultList();
		Double creditAmt = 0.0;
		Double debitAmt = 0.0;
		Double openingBal = 0.0;
		for (Object[] custData : txnLists) {

			if (custData[0] != null) {
				openingBal += (Double) custData[0];
			}
			if (custData[2] != null) {
				creditAmt = Double.parseDouble(String.valueOf(custData[2]));
			}
			if (custData[1] != null) {
				debitAmt = Double.parseDouble(String.valueOf(custData[1]));
			}
		}
		tb.setOpeningBalance(openingBal);
		tb.setDebit(debitAmt);
		tb.setCredit(creditAmt);
		Double closingBal = openingBal + debitAmt - creditAmt;
		tb.setClosingBalance(closingBal);
	}

	@Override
	public void getTrialBalanceInterBranch(List<TrialBalance> trialBalanceList, Users user, String fromDate,
			String toDate, Long branchId, EntityManager em) {
		String hql = null;
		if (branchId != null && branchId > 0) {
			hql = IB_TB_BRANCH_JPQL;
		} else {
			hql = IB_TB_ORG_JPQL;
		}
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "DATA: " + fromDate + " " + toDate + " branchId: " + branchId);
			log.log(Level.INFO, "SQL: " + hql);
		}
		Query query = em.createQuery(hql);
		if (branchId != null && branchId > 0) {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, branchId);
			query.setParameter(3, fromDate);
			query.setParameter(4, user.getOrganization().getId());
			query.setParameter(5, branchId);
			query.setParameter(6, fromDate);
			query.setParameter(7, toDate);
			query.setParameter(8, user.getOrganization().getId());
			query.setParameter(9, branchId);
			query.setParameter(10, user.getOrganization().getId());
			query.setParameter(11, branchId);
		} else {
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, fromDate);
			query.setParameter(3, user.getOrganization().getId());
			query.setParameter(4, fromDate);
			query.setParameter(5, toDate);
			query.setParameter(6, user.getOrganization().getId());
			query.setParameter(7, user.getOrganization().getId());
		}
		List<Object[]> txnLists = query.getResultList();
		for (Object[] custData : txnLists) {
			long fBranchId = (Integer) custData[3];
			long tBranchId = (Integer) custData[4];
			int itentity = Integer.parseInt(String.valueOf(custData[5]));
			Branch fBranch = Branch.findById(fBranchId);
			Branch tBranch = Branch.findById(tBranchId);
			Double creditAmt = 0.0;
			Double debitAmt = 0.0;
			Double openingBal = 0.0;
			Double closingBal = 0.0;
			if (custData[0] != null) {
				openingBal = (Double) custData[0];
			}
			String name = fBranch.getName() + "-" + tBranch.getName();
			if (custData[1] != null) {
				debitAmt = Double.parseDouble(String.valueOf(custData[1]));
			}
			if (custData[2] != null) {
				creditAmt = Double.parseDouble(String.valueOf(custData[2]));
			}
			closingBal = openingBal + debitAmt - creditAmt;
			TrialBalance tb = new TrialBalance();
			tb.setAccountName(name);
			tb.setSpecId(fBranchId);
			tb.setHeadid2(tBranchId);
			tb.setSpecfaccountCode("" + tBranchId);
			tb.setOpeningBalance(openingBal);
			tb.setDebit(debitAmt);
			tb.setCredit(creditAmt);
			tb.setTopLevelAccountCode("3000000000000000000");
			tb.setClosingBalance(closingBal);
			if (itentity == 0) {
				tb.setHeadType(IdosConstants.HEAD_INTR_BRANCH);
			} else if (itentity == 1) {
				tb.setHeadType(IdosConstants.HEAD_INTR_BRANCH_OUT);
			} else if (itentity == 2) {
				tb.setHeadType(IdosConstants.HEAD_INTR_BRANCH_IN);
			}
			trialBalanceList.add(tb);
		}
	}

	@Override
	public void createInterBranchMapping(Users user, EntityManager entityManager, Branch branch) {
		if (user.getOrganization().getId() != null && branch.getId() != null) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<Branch> branchList = genericDao.findByCriteria(Branch.class, criterias, entityManager);
			if (branchList != null && !branchList.isEmpty()) {
				for (Branch branchOrgWise : branchList) {
					if (branchOrgWise.getId() != branch.getId()) {
						InterBranchMapping toFromBranch = new InterBranchMapping();
						InterBranchMapping fromToBranch = new InterBranchMapping();
						// A to B Map
						toFromBranch.setOrganization(user.getOrganization());
						toFromBranch.setToBranch(branch);
						toFromBranch.setFromBranch(branchOrgWise);
						genericDao.saveOrUpdate(toFromBranch, user, entityManager);
						// B to A Map
						fromToBranch.setOrganization(user.getOrganization());
						fromToBranch.setToBranch(branchOrgWise);
						fromToBranch.setFromBranch(branch);
						genericDao.saveOrUpdate(fromToBranch, user, entityManager);
					}
				}
			}
		}
	}

	@Override
	public TrialBalance getTrialBalance4SpecificInterBranch(Users user, EntityManager em, Date fromDate, Date toDate,
			Long branchId, InterBranchMapping interBranchMapping) {
		if (interBranchMapping == null) {
			return null;
		}
		String hql = null;
		TrialBalance tb = null;
		if (branchId != null && branchId > 0) {
			hql = ONE_IB_TB_BRANCH_JPQL;
		} else {
			hql = ONE_IB_TB_ORG_JPQL;
		}
		ArrayList inparams = new ArrayList(7);
		inparams.add(user.getOrganization().getId());
		if (branchId != null && branchId > 0) {
			inparams.add(branchId);
			inparams.add(interBranchMapping.getId());
			inparams.add(fromDate);
			inparams.add(user.getOrganization().getId());
			inparams.add(branchId);
			inparams.add(interBranchMapping.getId());
			inparams.add(fromDate);
			inparams.add(toDate);
			inparams.add(user.getOrganization().getId());
			inparams.add(branchId);
			inparams.add(interBranchMapping.getId());
		} else {
			inparams.add(interBranchMapping.getId());
			inparams.add(fromDate);
			inparams.add(user.getOrganization().getId());
			inparams.add(interBranchMapping.getId());
			inparams.add(fromDate);
			inparams.add(toDate);
			inparams.add(user.getOrganization().getId());
			inparams.add(interBranchMapping.getId());
		}
		List<Object[]> txnLists = genericDao.executeNativeQueryWithParamName(hql, em, inparams);

		if (txnLists.size() > 0) {
			Object[] custData = txnLists.get(0);
			int itentity = 0;
			if (!"null".equalsIgnoreCase(String.valueOf(custData[4])) && custData[4] != null) {
				itentity = Integer.parseInt(String.valueOf(custData[4]));
			}

			Double creditAmt = 0.0;
			Double debitAmt = 0.0;
			Double openingBal = 0.0;
			Double closingBal = 0.0;
			if (custData[0] != null) {
				openingBal = (Double) custData[0];
			}
			String name = interBranchMapping.getFromBranch().getName() + "-"
					+ interBranchMapping.getToBranch().getName();
			if (custData[1] != null) {
				debitAmt = Double.parseDouble(String.valueOf(custData[1]));
			}
			if (custData[2] != null) {
				creditAmt = Double.parseDouble(String.valueOf(custData[2]));
			}
			closingBal = openingBal + debitAmt - creditAmt;
			tb = new TrialBalance();
			tb.setAccountName(name);
			tb.setSpecId(interBranchMapping.getFromBranch().getId());
			tb.setHeadid2(interBranchMapping.getToBranch().getId());
			tb.setSpecfaccountCode("" + interBranchMapping.getId());
			tb.setOpeningBalance(openingBal);
			tb.setDebit(debitAmt);
			tb.setCredit(creditAmt);
			tb.setTopLevelAccountCode("3000000000000000000");
			tb.setClosingBalance(closingBal);
			if (itentity == 0) {
				tb.setHeadType(IdosConstants.HEAD_INTR_BRANCH);
			} else if (itentity == 1) {
				tb.setHeadType(IdosConstants.HEAD_INTR_BRANCH_OUT);
			} else if (itentity == 2) {
				tb.setHeadType(IdosConstants.HEAD_INTR_BRANCH_IN);
			}
		}
		return tb;
	}
}
