package com.idos.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.logging.Level;
import com.idos.util.CodeHelper;
import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import pojo.TransactionViewResponse;
import javax.inject.Inject;
import actor.CreatorActor;
import model.Branch;
import model.ConfigParams;
import model.Project;
import model.Specifics;
import model.Transaction;
import model.TransactionItems;
import model.TransactionPurpose;
import model.UserRightSpecifics;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import model.VendorSpecific;
import play.mvc.WebSocket;
import play.db.jpa.JPAApi;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;

/**
 * Created by Manali on 23-03-2018.
 */

public class PayAdvanceToVendorDAOImpl implements PayAdvanceToVendorDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public Transaction submitForAprroval(Users user, JsonNode json, EntityManager entityManager,
			EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
		Transaction transaction = new Transaction();
		try {
			long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
			TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);
			Map<String, Object> criterias = new HashMap<String, Object>();
			long txnforbranch = json.findValue("txnforbranch").asLong();

			String txnforproject = json.findValue("txnforproject") == null ? ""
					: json.findValue("txnforproject").asText();
			Project txnProject = null;
			String projectName = "";
			if (txnforproject != null && !txnforproject.equals("")) {
				txnProject = genericDao.getById(Project.class, IdosUtil.convertStringToLong(txnforproject),
						entityManager);
				projectName = txnProject.getName();
			}
			long txnRCAFCCCreditCustomer = json.findValue("txnRCAFCCCreditCustomer") != null
					? json.findValue("txnRCAFCCCreditCustomer").asLong()
					: 0;
			SimpleDateFormat idosdf = new SimpleDateFormat("MMM dd,yyyy");
			String supportingdoc = json.findValue("supportingdoc").asText();
			String txnremarks = json.findValue("txnremarks").asText();
			String txnSourceGstin = json.findValue("txnSourceGstin").asText();
			String txnDestinGstin = json.findValue("txnDestinGstin").asText();
			String netAmountPaid = json.findValue("netAmountPaid").asText();
			Double totalTDSAmt = (json.findValue("totalTDSAmt") == null
					|| "".equals(json.findValue("totalTDSAmt").asText())) ? 0
							: json.findValue("totalTDSAmt").asDouble();

			int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0
					: json.findValue("txnTypeOfSupply").asInt();
			String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
			Date txnDate = IdosUtil.getFormatedDateWithTime(selectedTxnDate);

			Vendor vend = null;
			vend = Vendor.findById(txnRCAFCCCreditCustomer);
			if (vend == null) {
				throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
						"Customer is missing", IdosConstants.RECORD_NOT_FOUND);
			}

			// start with new transaction

			Branch txnBranch = Branch.findById(txnforbranch);
			String branchName = txnBranch.getName();
			// Enter data for first item in transaction table to be displayed in Transaction
			// list
			String txnForItemStr = json.findValue("txnforitem").toString();
			JSONArray arrJSON = new JSONArray(txnForItemStr);
			JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
			Long itemIdRow0 = firstRowItemData.getLong("txnItems");
			Specifics txnItem = Specifics.findById(itemIdRow0);
			Double txnGrossRow0 = firstRowItemData.getDouble("txnGross");
			transaction.setTransactionSpecifics(txnItem);
			transaction.setTransactionParticulars(txnItem.getParticularsId());
			transaction.setGrossAmount(txnGrossRow0);
			transaction.setSourceGstin(txnSourceGstin);
			transaction.setDestinationGstin(txnDestinGstin);
			transaction.setTypeOfSupply(txnTypeOfSupply);
			transaction.setTransactionPurpose(usertxnPurpose);
			transaction.setTransactionBranch(txnBranch);
			transaction.setTransactionBranchOrganization(txnBranch.getOrganization());
			transaction.setTransactionVendorCustomer(vend);
			if (netAmountPaid != "") {
				transaction.setNetAmount(Double.valueOf(netAmountPaid) - totalTDSAmt);
			}
			if (DateUtil.isBackDate(txnDate)) {
				transaction.setIsBackdatedTransaction(IdosConstants.BACK_DATED_TXN);
			}
			transaction.setTransactionDate(txnDate);

			genericDao.saveOrUpdate(transaction, user, entityManager);

			Long tdsRecSpecificID = TRANSACTION_ITEMS_SERVICE.saveMultiItemsTransPayAdvVend(entityManager, user, json,
					transaction, vend, result);
			// mapping is not done "is this where you classify TDS Receivable from
			// customer", so don't save anything and quit
			if (tdsRecSpecificID == null || tdsRecSpecificID == 0) {
				return transaction;
			}

			transaction.setTransactionPurpose(usertxnPurpose);
			transaction.setTransactionVendorCustomer(vend);
			String netDesc = "Advance Amount: " + transaction.getCustomerNetPayment() + ",Withholding Adjustment: "
					+ transaction.getWithholdingTax();
			// String netDesc = "Advance Received For Purpose: " +
			// transaction.getTransactionPurpose();
			transaction.setNetAmountResultDescription(netDesc);

			int txnReceiptDetails = json.findValue("txnReceiptDetails") != null
					? json.findValue("txnReceiptDetails").asInt()
					: 0;
			String txnReceiptTypeBankDetails = json.findValue("txnReceiptTypeBankDetails").asText();
			long txnReceiptPaymentBank = json.findValue("txnReceiptPaymentBank") != null
					? json.findValue("txnReceiptPaymentBank").asLong()
					: null;
			String txnInstrumentNum = json.findValue("txnInstrumentNum") != null
					? json.findValue("txnInstrumentNum").asText()
					: null;
			String txnInstrumentDate = json.findValue("txnInstrumentDate") != null
					? json.findValue("txnInstrumentDate").asText()
					: null;
			String paymentMode = "";
			BUY_TRANSACTION_SERVICE.setTxnPaymentDetail(user, entityManager, transaction, txnReceiptDetails,
					txnReceiptPaymentBank, txnInstrumentNum, txnInstrumentDate, result);
			if (IdosConstants.PAYMODE_CASH == txnReceiptDetails) {
				paymentMode = "CASH";
			} else if (IdosConstants.PAYMODE_BANK == txnReceiptDetails) {
				paymentMode = "BANK";
			}
			transaction.setReceiptDetailsType(txnReceiptDetails);
			if (txnReceiptTypeBankDetails != null && !"".equals(txnReceiptTypeBankDetails)) {
				transaction.setReceiptDetailsDescription(txnReceiptTypeBankDetails);
			}

			String txnDocument = "";
			String txnRemarks = "";
			if (!txnremarks.equals("") && txnremarks != null) {
				txnRemarks = user.getEmail() + "#" + txnremarks;
				transaction.setRemarks(txnRemarks);
				txnRemarks = transaction.getRemarks();
			}
			transaction.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(
					transaction.getSupportingDocs(), user.getEmail(), supportingdoc, user, entityManager));
			transaction.setTransactionStatus("Require Approval");
			// list of additional users all approver role users of thet organization
			criterias.clear();
			criterias.put("role.name", "APPROVER");
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
			String approverEmails = "";
			String additionalApprovarUsers = "";
			String selectedAdditionalApproval = "";
			for (UsersRoles usrRoles : approverRole) {
				additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
				// check for right in chart of accounts
				criterias.clear();
				criterias.put("user.id", usrRoles.getUser().getId());
				criterias.put("userRights.id", 2L);
				criterias.put("specifics.id", txnItem.getId());
				criterias.put("presentStatus", 1);
				UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class, criterias,
						entityManager);
				if (userHasRightInCOA != null) {
					boolean userAmtLimit = true; // false;
					/*
					 * if (userHasRightInCOA.getAmount() != null) {
					 * if (userHasRightInCOA.getAmount() > 0) {
					 * if (Double.parseDouble(txnPCAFCVAmountOfAdvance) >
					 * userHasRightInCOA.getAmount()) {
					 * userAmtLimit = false;
					 * }
					 * if (Double.parseDouble(txnPCAFCVAmountOfAdvance) <
					 * userHasRightInCOA.getAmount()) {
					 * userAmtLimit = true;
					 * }
					 * }
					 * }
					 * if (userHasRightInCOA.getAmountTo() != null) {
					 * if (userHasRightInCOA.getAmountTo() > 0) {
					 * if (Double.parseDouble(txnPCAFCVAmountOfAdvance) >
					 * userHasRightInCOA.getAmountTo()) {
					 * userAmtLimit = false;
					 * }
					 * if (Double.parseDouble(txnPCAFCVAmountOfAdvance) <
					 * userHasRightInCOA.getAmountTo()) {
					 * userAmtLimit = true;
					 * }
					 * }
					 * }
					 */
					if (userAmtLimit == true) {
						approverEmails += usrRoles.getUser().getEmail() + ",";
					}
				}
			}
			transaction.setApproverEmails(approverEmails);
			transaction.setAdditionalApproverEmails(additionalApprovarUsers);
			String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
			transaction.setTransactionRefNumber(transactionNumber);
			TRANSACTION_DAO.setInvoiceQuotProfSerial(user, entityManager, transaction);
			genericDao.saveOrUpdate(transaction, user, entityManager);
			FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, supportingdoc, transaction.getId(),
					IdosConstants.MAIN_TXN_TYPE);
			// CREATE_TRIAL_BALANCE_DAO.saveMultiItemTrialBalance(transaction, user,
			// entityManager, vend, tdsRecSpecificID);
			entitytransaction.commit();
			/*
			 * if (txnPCAFCVLeftOutWithholdTransIDs != null &&
			 * !"".equals(txnPCAFCVLeftOutWithholdTransIDs)) {
			 * updateWithholdingForLeftOutTrans(txnPCAFCVLeftOutWithholdTransIDs, user,
			 * entityManager); // call only if withholding for previous transaction was not
			 * calculated.
			 * }
			 */
			if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
				// Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
				// Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
				// for (int i = 0; i < keyArray.length; i++) {
				// StringBuilder sbquery = new StringBuilder("");
				// sbquery.append(
				// "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
				// obj.presentStatus=1");
				// List<Users> orgusers = genericDao.executeSimpleQuery(sbquery.toString(),
				// entityManager);
				// if (!orgusers.isEmpty()
				// && orgusers.get(0).getOrganization().getId() ==
				// user.getOrganization().getId()) {
				// orgtxnregistereduser.put(keyArray[i].toString(),
				// CreatorActor.expenseregistrered.get(keyArray[i]));
				// }
				// }
				String invoiceDate = "";
				String invoiceDateLabel = "";
				if (transaction.getTransactionInvoiceDate() != null) {
					invoiceDateLabel = "INVOICE DATE:";
					invoiceDate = idosdf.format(transaction.getTransactionInvoiceDate());
				}
				String itemParentName = "";
				if (transaction.getTransactionSpecifics().getParentSpecifics() != null
						&& !transaction.getTransactionSpecifics().getParentSpecifics().equals("")) {
					itemParentName = transaction.getTransactionSpecifics().getParentSpecifics().getName();
				} else {
					itemParentName = transaction.getTransactionSpecifics().getParticularsId().getName();
				}
				String approverEmail = "";
				String approverLabel = "";
				if (transaction.getApproverActionBy() != null) {
					approverLabel = "APPROVER:";
					approverEmail = transaction.getApproverActionBy().getEmail();
				}
				String txnSpecialStatus = "";
				if (transaction.getTransactionExceedingBudget() != null && transaction.getKlFollowStatus() != null) {
					if (transaction.getTransactionExceedingBudget() == 1 && transaction.getKlFollowStatus() == 0) {
						txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
					}
					if (transaction.getTransactionExceedingBudget() == 1 && transaction.getKlFollowStatus() == 1) {
						txnSpecialStatus = "Transaction Exceeding Budget";
					}
				}
				if (transaction.getTransactionExceedingBudget() == null && transaction.getKlFollowStatus() != null) {
					if (transaction.getKlFollowStatus() == 0) {
						txnSpecialStatus = "Rules Not Followed";
					}
				}
				if (transaction.getTransactionExceedingBudget() != null && transaction.getKlFollowStatus() == null) {
					txnSpecialStatus = "Transaction Exceeding Budget";
				}
				String txnResultDesc = "";
				if (transaction.getNetAmountResultDescription() != null
						&& !transaction.getNetAmountResultDescription().equals("null")) {
					txnResultDesc = transaction.getNetAmountResultDescription();
				}
				String debitCredit = "Credit";

				Integer typeOfSupply = transaction.getTypeOfSupply() == null ? 0 : transaction.getTypeOfSupply();
				txnDocument = transaction.getSupportingDocs() == null ? "" : transaction.getSupportingDocs();
				TransactionViewResponse.addActionTxn(transaction.getId(), branchName,
						projectName,
						txnItem.getName(),
						itemParentName, "", "", "", "", vend.getName(),
						transaction.getTransactionPurpose().getTransactionPurpose(),
						idosdf.format(transaction.getTransactionDate()), invoiceDateLabel,
						invoiceDate, paymentMode,
						0.0, 0.0, 0.0, transaction.getNetAmount(), txnResultDesc, "",
						transaction.getTransactionStatus(), transaction.getCreatedBy().getEmail(),
						approverLabel,
						approverEmail, txnDocument, txnRemarks, debitCredit, approverEmails,
						additionalApprovarUsers,
						selectedAdditionalApproval, txnSpecialStatus, 0.0, "",
						txnInstrumentNum,
						txnInstrumentDate, transaction.getTransactionPurpose().getId(), "", "", 0,
						transaction.getTransactionRefNumber(), 0l, 0.0, 0, typeOfSupply, result);
			}
			// SingleUser
			if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
				ArrayNode singleUserAccounting = result.putArray("singleUserAccounting");
				ObjectNode createSingleuserJson = SINGLE_USER_DAO.createSingleuserJson(transaction, json, user);
				singleUserAccounting.add(createSingleuserJson);
			}
		} catch (Exception ex) {
			if (ex instanceof IDOSException) {
				throw (IDOSException) ex;
			} else {
				throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
						"Error on Pay Advance to Vendor- submit for approval.", ex.getMessage());
			}
		}
		return transaction;
	}

	public boolean submitForAccountPayAdvToVendAdv(Transaction txn, EntityManager entityManager, Users user)
			throws IDOSException {
		try {
			Vendor vendor = txn.getTransactionVendorCustomer();
			Map<String, Object> criterias = new HashMap<String, Object>();
			// criterias.put("transactionId", Long.parseLong(transactionEntityId));
			criterias.put("transaction.id", txn.getId());
			criterias.put("presentStatus", 1);
			List<TransactionItems> listTransactionItems = genericDao.findByCriteria(TransactionItems.class, criterias,
					entityManager);
			for (TransactionItems txnItemrow : listTransactionItems) {
				Specifics incomeSpecf = txnItemrow.getTransactionSpecifics();
				Long reverseChargeItemId = txnItemrow.getReverseChargeItemId();
				ArrayList inparamList = new ArrayList(3);
				inparamList.add(user.getOrganization().getId());
				inparamList.add(incomeSpecf.getId());
				inparamList.add(vendor.getId());
				List<VendorSpecific> vendorSpecf = genericDao.queryWithParams(PAY_VENDOR_SPECIFIC_HQL, entityManager,
						inparamList);
				if (vendorSpecf.size() > 0) {
					VendorSpecific vendorSpecficsForAdvance = vendorSpecf.get(0);
					Double addedAdvance = null;
					if (vendorSpecficsForAdvance.getAdvanceMoney() != null) {
						addedAdvance = txnItemrow.getNetAmount() + vendorSpecficsForAdvance.getAdvanceMoney();
					}
					if (vendorSpecficsForAdvance.getAdvanceMoney() == null) {
						addedAdvance = txnItemrow.getNetAmount(); // net amt will have withholding tax too
					}
					vendorSpecficsForAdvance.setAdvanceMoney(addedAdvance);
					vendorSpecficsForAdvance.setReverseChargeItemId(reverseChargeItemId);
					genericDao.saveOrUpdate(vendorSpecficsForAdvance, user, entityManager);
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on Pay advance to vendor- submit for accounting.", ex.getMessage());
		}
		return true;
	}
}
