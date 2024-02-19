package service;

import java.text.ParseException;
import java.util.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.logging.Level;
import model.Branch;
import model.ConfigParams;
import model.KaizalaGroups;
import model.Organization;
import model.Project;
import model.PurchaseOrderActionBody;
import model.PurchaseOrderProperties;
import model.Specifics;
import model.Transaction;
import model.TransactionItems;
import model.TransactionPurpose;
import model.UserRightInBranch;
import model.UserRightSpecifics;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import model.VendorDetail;
import model.PurchaseOrderTxnModel;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;

import play.mvc.WebSocket;
import pojo.TransactionViewResponse;
import actor.CreatorActor;
import akka.stream.javadsl.*;
import akka.actor.*;
import com.idos.util.CodeHelper;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import akka.NotUsed;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import controllers.externalInterface.KaizalaAuthorization;
import flexjson.JSONSerializer;

public class PurchaseOrderServiceImpl implements PurchaseOrderService {
	//@Override
	//public Transaction submitForApprovalPurchaseOrder(Users user, JsonNode json, final EntityManager entityManager,
	//		EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
	//	Long txnforbranch = json.findValue("txnforbranch").asLong();
	//	String txnforproject = json.findValue("txnforproject").asText();
	//	String txnDestinGstin = json.findValue("txnDestinGstin").asText();
	//	String txnForItemStr = json.findValue("txnforitem").toString();
	//	String txnPurpose = json.findValue("txnPurpose").asText();
	//	long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
	//	Long txnforcustomer = json.findValue("txnforcustomer").asLong();
	//	String txnPoReference = json.findValue("txnPoReference") == null ? ""
	//			: json.findValue("txnPoReference").asText();
	//	Double txnnetamount = json.findValue("txnnetamount").asDouble();
	//	String txnremarks = json.findValue("txnremarks").asText();
	//	String txnRemarksPrivate = json.findValue("txnRemarksPrivate").asText();
	//	String supportingdoc = json.findValue("supportingdoc").asText();
	//	Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
	//			: json.findValue("txnEntityID").asLong();

	//	String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
	//	Date txnDate = null;
	//	try {
	//		if (selectedTxnDate != null) {
	//			txnDate = IdosConstants.IDOSDF.parse(selectedTxnDate);
	//		} else {
	//			txnDate = new Date();
	//		}
	//	} catch (ParseException e) {
	//		throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
	//				IdosConstants.NULL_KEY_EXC_ESMF_MSG,
	//				"cannot parse date: " + selectedTxnDate + " " + e.getMessage());
	//	}
	//	String txnRemarks = "";
	//	Branch txnBranch = null;
	//	String branchName = "";
	//	Vendor txncustomer = null;
	//	Project txnProject = null;
	//	String projectName = "";
	//	String itemName = "";
	//	String customerVendorName = "";
	//	Transaction transaction = null;
	//	try {
	//		JSONArray arrJSON = new JSONArray(txnForItemStr);

	//		if (txnEntityID > 0) {
	//			transaction = Transaction.findById(txnEntityID);
	//		} else {
	//			transaction = new Transaction();
	//		}
	//		if (txnforbranch != null && !txnforbranch.equals("")) {
	//			txnBranch = genericDAO.getById(Branch.class, txnforbranch, entityManager);
	//			branchName = txnBranch.getName();
	//		}
	//		if (txnforproject != null && !txnforproject.equals("")) {
	//			txnProject = genericDAO.getById(Project.class, IdosUtil.convertStringToLong(txnforproject),
	//					entityManager);
	//			projectName = txnProject.getName();
	//		}
	//		if (txnforcustomer != null && !txnforcustomer.equals("")) {
	//			txncustomer = genericDAO.getById(Vendor.class, txnforcustomer, entityManager);
	//			customerVendorName = txncustomer.getName();
	//		}
	//		TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);
	//		transaction.setTransactionPurpose(usertxnPurpose);
	//		transaction.setTransactionBranch(txnBranch);
	//		transaction.setTransactionBranchOrganization(txnBranch.getOrganization());
	//		transaction.setTransactionProject(txnProject);
	//		transaction.setTransactionVendorCustomer(txncustomer);
	//		transaction.setDestinationGstin(txnDestinGstin);
	//		// Enter data for first item in transaction table to be displayed in Transaction
	//		// list
	//		JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
	//		Long itemIdRow0 = firstRowItemData.getLong("txnItems");
	//		Specifics txnSpecificItem = genericDAO.getById(Specifics.class, itemIdRow0, entityManager);
	//		Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
	//		Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
	//		Double txnGrossRow0 = firstRowItemData.getDouble("txnGross");
	//		transaction.setTransactionSpecifics(txnSpecificItem);
	//		transaction.setTransactionParticulars(txnSpecificItem.getParticularsId());
	//		transaction.setNoOfUnits(txnNoOfUniRow0t);
	//		transaction.setPricePerUnit(txnPerUnitPriceRow0);
	//		transaction.setGrossAmount(txnGrossRow0);
	//		transaction.setPoReference(txnPoReference);
	//		transaction.setTransactionDate(txnDate);
	//		transaction.setNetAmount(txnnetamount);
	//		if (txnremarks != null && !txnremarks.equals("")) {
	//			if (transaction.getRemarks() != null) {
	//				transaction.setRemarks(txnremarks);
	//			} else {
	//				txnRemarks = user.getEmail() + "#" + txnremarks;
	//				transaction.setRemarks(txnRemarks);
	//			}
	//			txnRemarks = transaction.getRemarks(); // fetch encoded value
	//		}
	//		if (txnRemarksPrivate != null && !txnRemarksPrivate.equals("")) {
	//			if (transaction.getRemarksPrivate() != null) {
	//				txnRemarksPrivate = transaction.getRemarksPrivate() + "," + user.getEmail() + "#"
	//						+ txnRemarksPrivate;
	//				transaction.setRemarksPrivate(txnRemarksPrivate);
	//			} else {
	//				txnRemarksPrivate = user.getEmail() + "#" + txnRemarksPrivate;
	//				transaction.setRemarksPrivate(txnRemarksPrivate);
	//			}
	//			txnRemarksPrivate = transaction.getRemarksPrivate(); // fetch encoded value
	//		}
	//		transaction.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(transaction.getSupportingDocs(),
	//				user.getEmail(), supportingdoc, user, entityManager));

	//		transaction.setTransactionStatus("Require Approval");
	//		// list of additional users all approver role users of thet organization
	//		Map<String, Object> criterias = new HashMap<String, Object>();
	//		criterias.put("role.name", "APPROVER");
	//		criterias.put("organization.id", user.getOrganization().getId());
	//		criterias.put("presentStatus", 1);
	//		List<UsersRoles> approverRole = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);

	//		String approverEmails = "";
	//		String additionalApprovarUsers = "";
	//		String selectedAdditionalApproval = "";
	//		Boolean approver = null;
	//		/*
	//		 * for (UsersRoles usrRoles : approverRole) {
	//		 * approver=false;
	//		 * additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
	//		 * criterias.clear();
	//		 * criterias.put("user.id", usrRoles.getUser().getId());
	//		 * criterias.put("userRights.id", 2L);
	//		 * criterias.put("branch.id", txnBranch.getId());
	//		 * UserRightInBranch userHasRightInBranch =
	//		 * genericDAO.getByCriteria(UserRightInBranch.class, criterias, entityManager);
	//		 * if (userHasRightInBranch != null) {
	//		 * //check for right in chart of accounts
	//		 * criterias.clear();
	//		 * criterias.put("user.id", usrRoles.getUser().getId());
	//		 * criterias.put("userRights.id", 2L);
	//		 * criterias.put("specifics.id", txnSpecificItem.getId());
	//		 * UserRightSpecifics userHasRightInCOA =
	//		 * genericDAO.getByCriteria(UserRightSpecifics.class, criterias, entityManager);
	//		 * if (userHasRightInCOA != null) {
	//		 * approverEmails += usrRoles.getUser().getEmail() + ",";
	//		 * }
	//		 * }
	//		 * }
	//		 */
	//		for (UsersRoles usrRoles : approverRole) {
	//			approver = false;
	//			additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
	//			criterias.clear();
	//			criterias.put("user.id", usrRoles.getUser().getId());
	//			criterias.put("userRights.id", 2L);
	//			criterias.put("branch.id", txnBranch.getId());
	//			criterias.put("presentStatus", 1);

	//			UserRightInBranch userHasRightInBranch = genericDAO.getByCriteria(UserRightInBranch.class, criterias,
	//					entityManager);
	//			if (userHasRightInBranch != null) {
	//				/*
	//				 * //check for right in chart of accounts
	//				 * criterias.clear();
	//				 * criterias.put("user.id", usrRoles.getUser().getId());
	//				 * criterias.put("userRights.id", 2L);
	//				 * criterias.put("specifics.id", txnSpecificItem.getId());
	//				 * UserRightSpecifics userHasRightInCOA =
	//				 * genericDAO.getByCriteria(UserRightSpecifics.class, criterias, entityManager);
	//				 * if (userHasRightInCOA != null) {
	//				 * approverEmails += usrRoles.getUser().getEmail() + ",";
	//				 * }
	//				 */
	//				for (int i = 0; i < arrJSON.length(); i++) {
	//					// Double howMuchAdvance=0.0;Double txnTaxAmount=0.0;Double
	//					// customerAdvance=0.0;String txnTaxDesc="";Double withholdingAmount=0.0;
	//					JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
	//					// TransactionItems transactionItem = new TransactionItems();
	//					Long itemId = rowItemData.getLong("txnItems");
	//					Specifics txnItem = genericDAO.getById(Specifics.class, itemId, entityManager);
	//					criterias.clear();
	//					criterias.put("user.id", usrRoles.getUser().getId());
	//					criterias.put("userRights.id", 2L);
	//					criterias.put("specifics.id", txnItem.getId());
	//					criterias.put("presentStatus", 1);
	//					UserRightSpecifics userHasRightInCOA = genericDAO.getByCriteria(UserRightSpecifics.class,
	//							criterias, entityManager);
	//					if (userHasRightInCOA != null) {
	//						approver = true;
	//					} else {
	//						approver = false;
	//					}
	//				}
	//				if (approver) {
	//					approverEmails += usrRoles.getUser().getEmail() + ",";
	//				}

	//			}
	//		}
	//		transaction.setApproverEmails(approverEmails);
	//		transaction.setAdditionalApproverEmails(additionalApprovarUsers);

	//		// list of approver user
	//		String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
	//		transaction.setTransactionRefNumber(transactionNumber);
	//		genericDAO.saveOrUpdate(transaction, user, entityManager);
	//		FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, supportingdoc, transaction.getId(),
	//				IdosConstants.MAIN_TXN_TYPE);
	//		// Enter multiple items data into TransactionItems table
	//		if (txnEntityID > 0) {
	//			transactionItemsService.updateMultipleItemsTransactionItems(entityManager, user, arrJSON, transaction);
	//		} else {
	//			transactionItemsService.insertMultipleItemsTransactionItems(entityManager, user, arrJSON, transaction,
	//					txnDate);
	//		}
	//		entitytransaction.commit();
	//		if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
	//			// Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
	//			// Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
	//			// String sbquery = "select obj from Users obj WHERE obj.email = ?1 and
	//			// obj.presentStatus=1";
	//			// for (int i = 0; i < keyArray.length; i++) {
	//			// Query query = entityManager.createQuery(sbquery);
	//			// query.setParameter(1, keyArray[i]);
	//			// List<Users> orgusers = genericDAO.executeQuery(query, entityManager);
	//			// if (!orgusers.isEmpty()
	//			// && orgusers.get(0).getOrganization().getId() ==
	//			// user.getOrganization().getId()) {
	//			// orgtxnregistereduser.put(keyArray[i].toString(),
	//			// CreatorActor.expenseregistrered.get(keyArray[i]));
	//			// }
	//			// }
	//			String itemParentName = "";
	//			if (txnSpecificItem.getParentSpecifics() != null && !txnSpecificItem.getParentSpecifics().equals("")) {
	//				itemParentName = txnSpecificItem.getParentSpecifics().getName();
	//			} else {
	//				itemParentName = txnSpecificItem.getParticularsId().getName();
	//			}

	//			String approverEmail = "";
	//			String approverLabel = "";
	//			if (transaction.getApproverActionBy() != null) {
	//				approverLabel = "APPROVER:";
	//				approverEmail = transaction.getApproverActionBy().getEmail();
	//			}
	//			String txnSpecialStatus = "";
	//			if (transaction.getTransactionExceedingBudget() != null && transaction.getKlFollowStatus() != null) {
	//				if (transaction.getTransactionExceedingBudget() == 1 && transaction.getKlFollowStatus() == 0) {
	//					txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
	//				}
	//				if (transaction.getTransactionExceedingBudget() == 1 && transaction.getKlFollowStatus() == 1) {
	//					txnSpecialStatus = "Transaction Exceeding Budget";
	//				}
	//			}
	//			if (transaction.getTransactionExceedingBudget() == null && transaction.getKlFollowStatus() != null) {
	//				if (transaction.getKlFollowStatus() == 0) {
	//					txnSpecialStatus = "Rules Not Followed";
	//				}
	//			}
	//			if (transaction.getTransactionExceedingBudget() != null && transaction.getKlFollowStatus() == null) {
	//				txnSpecialStatus = "Transaction Exceeding Budget";
	//			}
	//			String txnResultDesc = "";
	//			if (transaction.getNetAmountResultDescription() != null
	//					&& !transaction.getNetAmountResultDescription().equals("null")) {
	//				txnResultDesc = transaction.getNetAmountResultDescription();
	//			}
	//			if (transaction.getDocRuleStatus() != null && transaction.getTransactionExceedingBudget() != null) {
	//				if (transaction.getDocRuleStatus() == 1 && transaction.getTransactionExceedingBudget() == 1) {
	//					txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
	//				}
	//				if (transaction.getKlFollowStatus() == 1 && transaction.getTransactionExceedingBudget() == 0) {
	//					txnSpecialStatus = "Rules Not Followed";
	//				}
	//			}
	//			if (transaction.getDocRuleStatus() != null && transaction.getTransactionExceedingBudget() == null) {
	//				txnSpecialStatus = "Rules Not Followed";
	//			}
	//			Integer typeOfSupply = transaction.getTypeOfSupply() == null ? 0 : transaction.getTypeOfSupply();
	//			String txnDocument = transaction.getSupportingDocs() == null ? "" : transaction.getSupportingDocs();
	//			TransactionViewResponse.addActionTxn(transaction.getId(), branchName, projectName, itemName,
	//					itemParentName, "",
	//					"", "", "", customerVendorName, transaction.getTransactionPurpose().getTransactionPurpose(),
	//					IdosConstants.idosdf.format(transaction.getTransactionDate()), "", "", "",
	//					transaction.getNoOfUnits(), transaction.getPricePerUnit(), transaction.getGrossAmount(),
	//					transaction.getNetAmount(), txnResultDesc, "", transaction.getTransactionStatus(),
	//					transaction.getCreatedBy().getEmail(), approverLabel, approverEmail, txnDocument, txnRemarks,
	//					"", approverEmails, additionalApprovarUsers, selectedAdditionalApproval,
	//					txnSpecialStatus, 0d, txnPoReference, "", "", transaction.getTransactionPurpose().getId(),
	//					txnRemarksPrivate, "", 0, transaction.getTransactionRefNumber(), 0l, 0.0, 0, typeOfSupply,
	//					result);
	//		}
	//	} catch (Exception ex) {
	//		log.log(Level.SEVERE, "Error", ex);
	//		throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
	//				"Error on Quotation- submit for approval", ex.getMessage());
	//	}
	//	return transaction;
	//}

	///*
	// * public Result showPurchaseOrderDetails(Users user, JsonNode json, final
	// * EntityManager entityManager,EntityTransaction entitytransaction) {
	// * log.log(Level.FINE, ">>>> Start");
	// * ObjectNode results = Json.newObject();
	// * Transaction txn = null;
	// * try {
	// * ArrayNode txndetailan = results.putArray("transactiondetailsData");
	// * ArrayNode txnItemsan = results.putArray("transactionItemdetailsData");
	// * 
	// * String transactionEntityId = json.findValue("transactionEntityId").asText();
	// * txn = Transaction.findById(Long.parseLong(transactionEntityId));
	// * if (txn != null) {
	// * ObjectNode row = Json.newObject();
	// * row.put("id", txn.getId());
	// * row.put("transactionPurpose",
	// * txn.getTransactionPurpose().getTransactionPurpose());
	// * row.put("transactionPurposeVal", txn.getTransactionPurpose().getId());
	// * row.put("noOfUnits", txn.getNoOfUnits());
	// * row.put("pricePerUnit", txn.getPricePerUnit());
	// * if (txn.getFrieghtCharges() != null)
	// * row.put("frieghtCharges", txn.getFrieghtCharges());
	// * else
	// * row.put("frieghtCharges", "");
	// * row.put("grossAmount", txn.getGrossAmount());
	// * row.put("transactionDate", idosdf.format(txn.getTransactionDate()));
	// * if (txn.getTransactionBranch() != null) {
	// * row.put("branchName", txn.getTransactionBranch().getName());
	// * row.put("branchId", txn.getTransactionBranch().getId());
	// * } else {
	// * row.put("branchName", "");
	// * row.put("branchId", "");
	// * }
	// * if (txn.getTransactionProject() != null) {
	// * row.put("projectName", txn.getTransactionProject().getName());
	// * } else {
	// * row.put("projectName", "");
	// * }
	// * if (txn.getTransactionProject() != null) {
	// * row.put("projectID", txn.getTransactionProject().getId());
	// * } else {
	// * row.put("projectID", "");
	// * }
	// * 
	// * row.put("poReference", txn.getPoReference() == null ? "" :
	// * txn.getPoReference());
	// * row.put("advanceType", txn.getAdvanceType() == null ? 0 :
	// * txn.getAdvanceType());
	// * row.put("netAmount", txn.getNetAmount() == null ? 0d : txn.getNetAmount());
	// * row.put("remarks", txn.getRemarks() == null ? "" : txn.getRemarks());
	// * row.put("remarksPrivate", txn.getRemarksPrivate() == null ? "" :
	// * txn.getRemarksPrivate());
	// * row.put("supportingDocs", txn.getSupportingDocs() == null ? "" :
	// * txn.getSupportingDocs());
	// * 
	// * row.put("createdBy", txn.getCreatedBy().getEmail());
	// * if (txn.getTransactionSpecifics() != null) {
	// * row.put("itemName", txn.getTransactionSpecifics().getName());
	// * row.put("itemId", txn.getTransactionSpecifics().getId());
	// * } else {
	// * row.put("itemName", "");
	// * row.put("itemId", "");
	// * }
	// * if (txn.getTransactionSpecifics() != null) {
	// * if (txn.getTransactionSpecifics().getParentSpecifics() != null &&
	// * !txn.getTransactionSpecifics().getParentSpecifics().equals("")) {
	// * row.put("itemParentName",
	// * txn.getTransactionSpecifics().getParentSpecifics().getName());
	// * } else {
	// * row.put("itemParentName",
	// * txn.getTransactionSpecifics().getParticularsId().getName());
	// * }
	// * } else {
	// * row.put("itemParentName", "");
	// * }
	// * if (txn.getTransactionVendorCustomer() != null) {
	// * row.put("customerVendorName", txn.getTransactionVendorCustomer().getName());
	// * row.put("customerVendorId", txn.getTransactionVendorCustomer().getId());
	// * } else {
	// * if (txn.getTransactionUnavailableVendorCustomer() != null) {
	// * row.put("customerVendorName", txn.getTransactionUnavailableVendorCustomer());
	// * row.put("customerVendorId", "");
	// * } else {
	// * row.put("customerVendorName", "");
	// * row.put("customerVendorId", "");
	// * }
	// * }
	// * 
	// * row.put("adjustmentFromAdvance", txn.getAdjustmentFromAdvance() == null ? 0.0
	// * : txn.getAdjustmentFromAdvance());
	// * row.put("netAmountResultDescription", txn.getNetAmountResultDescription() ==
	// * null ? "" : txn.getNetAmountResultDescription());
	// * row.put("withholdingTax", txn.getWithholdingTax() == null ? 0.0 :
	// * txn.getWithholdingTax());
	// * 
	// * if (txn.getTransactionPurpose().getId() ==
	// * IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT) {
	// * row.put("isitemallowsinputtaxes", 1);
	// * row.put("taxName1", txn.getTaxName1() == null ? "" : txn.getTaxName1());
	// * row.put("taxValue1", txn.getTaxValue1());
	// * row.put("taxName2", txn.getTaxName2() == null ? "" : txn.getTaxName2());
	// * row.put("taxValue2", txn.getTaxValue2());
	// * row.put("taxName3", txn.getTaxName3() == null ? "" : txn.getTaxName3());
	// * row.put("taxValue3", txn.getTaxValue3());
	// * row.put("taxName4", txn.getTaxName4() == null ? "" : txn.getTaxName4());
	// * row.put("taxValue4", txn.getTaxValue4());
	// * row.put("taxName5", txn.getTaxName5() == null ? "" : txn.getTaxName5());
	// * row.put("taxValue5", txn.getTaxValue5());
	// * row.put("taxName6", txn.getTaxName6() == null ? "" : txn.getTaxName6());
	// * row.put("taxValue6", txn.getTaxValue6());
	// * row.put("taxName7", txn.getTaxName7() == null ? "" : txn.getTaxName7());
	// * row.put("taxValue7", txn.getTaxValue7());
	// * BigDecimal totalInputTaxes = BigDecimal.valueOf((txn.getTaxValue1() == null ?
	// * 0.0 : txn.getTaxValue1())
	// * + (txn.getTaxValue2() == null ? 0.0 : txn.getTaxValue2()) +
	// * (txn.getTaxValue3() == null ? 0.0 : txn.getTaxValue3())
	// * + (txn.getTaxValue4() == null ? 0.0 : txn.getTaxValue4()) +
	// * (txn.getTaxValue5() == null ? 0.0 : txn.getTaxValue5())
	// * + (txn.getTaxValue6() == null ? 0.0 : txn.getTaxValue6()) +
	// * (txn.getTaxValue7() == null ? 0.0 : txn.getTaxValue7()));
	// * 
	// * row.put("totalInputTax", totalInputTaxes);
	// * } else if (txn.getTransactionPurpose().getId() ==
	// * IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY ||
	// * txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER
	// * || IdosConstants.PREPARE_QUOTATION == txn.getTransactionPurpose().getId() ||
	// * IdosConstants.PROFORMA_INVOICE == txn.getTransactionPurpose().getId()
	// * || IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW ==
	// * txn.getTransactionPurpose().getId() ||
	// * IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER ==
	// * txn.getTransactionPurpose().getId()) {
	// * Map<String, Object> criterias = new HashMap<String, Object>();
	// * criterias.put("transaction.id", Long.parseLong(transactionEntityId));
	// * List<TransactionItems> listTransactionItems =
	// * genericDAO.findByCriteria(TransactionItems.class, criterias, entityManager);
	// * for (TransactionItems txnItemrow : listTransactionItems) {
	// * ObjectNode itemRow = Json.newObject();
	// * itemRow.put("id", txnItemrow.getId());
	// * itemRow.put("noOfUnits", txnItemrow.getNoOfUnits());
	// * itemRow.put("pricePerUnit", txnItemrow.getPricePerUnit());
	// * itemRow.put("discountPer", txnItemrow.getDiscountPercent());
	// * itemRow.put("discountAmt", txnItemrow.getDiscountAmount());
	// * itemRow.put("grossAmount", txnItemrow.getGrossAmount());
	// * if (txnItemrow.getTransactionSpecifics() != null) {
	// * itemRow.put("itemName", txnItemrow.getTransactionSpecifics().getName());
	// * itemRow.put("itemId", txnItemrow.getTransactionSpecifics().getId());
	// * } else {
	// * itemRow.put("itemName", "");
	// * itemRow.put("itemId", "");
	// * }
	// * itemRow.put("taxDescription", txnItemrow.getTaxDescription());
	// * itemRow.put("totalInputTax", txnItemrow.getTotalTax());
	// * itemRow.put("withholdingAmount", txnItemrow.getWithholdingAmount());
	// * itemRow.put("availableAdvance", txnItemrow.getAvailableAdvance());
	// * itemRow.put("adjFromAdvance", txnItemrow.getAdjustmentFromAdvance());
	// * itemRow.put("netAmount", txnItemrow.getNetAmount());
	// * txnItemsan.add(itemRow);
	// * }
	// * }
	// * txndetailan.add(row);
	// * }
	// * } catch (Exception ex) {
	// * log.log(Level.SEVERE, "Error", ex);
	// * String strBuff = getStackTraceMessage(ex);
	// * expService.sendExceptionReport(strBuff, "", "",
	// * Thread.currentThread().getStackTrace()[1].getMethodName());
	// * List<String> errorList = getStackTrace(ex);
	// * return Results.ok(errorPage.render(ex, errorList));
	// * }
	// * log.log(Level.FINE, ">>>> End " + results);
	// * return Results.ok(results);
	// * }
	// */

	//public String createPurchaseOrderJSON(Transaction transaction, Users user, EntityManager entityManager,
	//		EntityTransaction entitytransaction) {
	//	String valuesArray = "[";
	//	Long transactionEntityId = transaction.getId();
	//	Map<String, Object> criterias = new HashMap<String, Object>();
	//	// criterias.put("transactionId", Long.parseLong(transactionEntityId));
	//	criterias.put("transaction.id", transactionEntityId);
	//	criterias.put("presentStatus", 1);
	//	List<TransactionItems> listTransactionItems = genericDAO.findByCriteria(TransactionItems.class, criterias,
	//			entityManager);
	//	for (TransactionItems txnItemrow : listTransactionItems) {
	//		String values = "{\"PN\":\"";
	//		String itemName = "", itemCode = "", itemUnit = "";
	//		String noOfUnits = (txnItemrow.getNoOfUnits()).toString();
	//		String pricePerUnits = (txnItemrow.getPricePerUnit()).toString();
	//		Specifics itemSpec = txnItemrow.getTransactionSpecifics();
	//		if (itemSpec != null) {
	//			itemName = itemSpec.getName();
	//			itemCode = itemSpec.getGstItemCode() != null ? itemSpec.getGstItemCode() : "Not Available";
	//			itemUnit = itemSpec.getExpenseUnitsMeasure();
	//		}
	//		// "Value":"[{\"PN\":\"The
	//		// I\",\"PQ\":\"1\",\"PPPU\":\"2\",\"PF\":\"1234568\",\"PU\":\"BAG-BAG\"}]"
	//		// String values="[{'PN':'Tomato', 'PQ':'50', 'PPPU':'30', 'PF':'123675',
	//		// 'PU':'Kgs'}]";
	//		values = values + itemName + "\", \"PQ\":\"" + noOfUnits + "\", \"PPPU\":\"" + pricePerUnits
	//				+ "\", \"PF\":\"" + itemCode + "\", \"PU\":\"" + itemUnit + "\"},";
	//		valuesArray = valuesArray + values;
	//	}
	//	String valuesArrayLastCommaRem = valuesArray.substring(0, valuesArray.lastIndexOf(","));
	//	valuesArray = valuesArrayLastCommaRem + "]";

	//	Organization org = transaction.getTransactionBranchOrganization();
	//	String orgName = org.getName();
	//	Branch branch = transaction.getTransactionBranch();
	//	String shippingAdd = branch.getAddress() == null ? "Not Available" : branch.getAddress();
	//	String shippingState = branch.getStateCode() == null ? "Not Available" : branch.getStateCode();
	//	// get phone nos to create group
	//	Vendor vendor = transaction.getTransactionVendorCustomer();
	//	String vendName = vendor.getName();
	//	String groupName = orgName + "-" + vendName;
	//	String assignToPhNo = "+919970177688";
	//	String poGeneratorPhNo = "+919930302208";
	//	if (vendor != null && vendor.getPhone() != null) {
	//		assignToPhNo = "+91" + vendor.getPhone();
	//	}
	//	String vendDetailId = transaction.getDestinationGstin();
	//	VendorDetail vendDetail = VendorDetail.findByVendorDetailID(Long.parseLong(vendDetailId));
	//	if (vendDetail != null && vendDetail.getPhone() != null) {
	//		assignToPhNo = "+91" + vendDetail.getPhone();
	//	}
	//	if (branch.getPhoneNumber() != null && branch.getPhoneNumber() != "") {
	//		String poGeneratorPhNosArray[] = branch.getPhoneNumber().split("-");
	//		poGeneratorPhNo = "+91" + poGeneratorPhNosArray[1];
	//	}

	//	KaizalaAuthorization kaAu = new KaizalaAuthorization(jpaApi, application);
	//	String accessToken = kaAu.getAccessToken();
	//	// check if group exists else create new group
	//	String groupId = checkIfGroupExist(kaAu, accessToken, groupName, assignToPhNo, poGeneratorPhNo, entityManager,
	//			user);
	//	String members = kaAu.getGroupMembers(accessToken, groupId, assignToPhNo, poGeneratorPhNo);
	//	String[] memArray = members.split(";");
	//	String assignedTo = memArray[0];
	//	String vendorId = memArray[1];

	//	JSONSerializer serializer = new JSONSerializer();
	//	PurchaseOrderProperties povaluepop = new PurchaseOrderProperties("PL", valuesArray, 6);
	//	PurchaseOrderProperties shippingAddpop = new PurchaseOrderProperties("SA", shippingAdd, 0);
	//	PurchaseOrderProperties shippingStatepop = new PurchaseOrderProperties("SS", shippingState, 0);
	//	PurchaseOrderProperties actionTopop = new PurchaseOrderProperties("aTo", assignedTo, 0); // assignedTo
	//	PurchaseOrderProperties VIDpop = new PurchaseOrderProperties("VID", assignedTo, 0); // vendor id same as
	//																						// assignedTo in first call
	//																						// from IDOS
	//	PurchaseOrderProperties vNamepop = new PurchaseOrderProperties("VNAME", vendName, 0);
	//	PurchaseOrderProperties BIDpop = new PurchaseOrderProperties("BID", vendorId, 0); // Buyer Id
	//	PurchaseOrderProperties bNamepop = new PurchaseOrderProperties("BNAME", orgName, 0);
	//	PurchaseOrderProperties orderStatepop = new PurchaseOrderProperties("state", "0", 1);
	//	// String str= serializer.exclude("*.class").serialize( shippingAddpop );

	//	ArrayList<PurchaseOrderProperties> propertiesArray = new ArrayList<PurchaseOrderProperties>();
	//	propertiesArray.add(povaluepop);
	//	propertiesArray.add(shippingAddpop);
	//	propertiesArray.add(shippingStatepop);
	//	propertiesArray.add(actionTopop);
	//	propertiesArray.add(VIDpop);
	//	propertiesArray.add(vNamepop);
	//	propertiesArray.add(BIDpop);
	//	propertiesArray.add(bNamepop);
	//	propertiesArray.add(orderStatepop);

	//	PurchaseOrderActionBody poa = new PurchaseOrderActionBody();
	//	poa.setTitle(transactionEntityId + "");
	//	poa.setProperties(propertiesArray);
	//	String poastr = serializer.exclude("*.class").include("properties").serialize(poa);
	//	String strBodyJSON = "{id:\"com.myidos.kaizala.miniapps.GSTInvoiceCard\", actionBody:" + poastr + "} }";
	//	// String strBodyJSON = "{id:\"com.microsoft.kaizala.miniapps.idostest\",
	//	// actionBody:{\"title\": \"purchase order #EF50034\",
	//	// properties:[{\"Name\":\"PL\", \"Value\":\" [{'PN':'Tomato', 'PQ':'50',
	//	// 'PPPU':'30', 'PF':'123675', 'PU':'Kgs'}]\", \"Type\":6}, { \"Name\":\"SA\",
	//	// \"Type\":0, \"Value\":\"Microsoft India R&D pvt. ltd.\" }, { \"Name\":\"SS\",
	//	// \"Type\":0, \"Value\":\"Telangana\" }, { \"Name\":\"aTo\", \"Type\":0,
	//	// \"Value\":\"c1e74ab7-c2ed-4f63-912e-b9f016a17983\" }, { \"Name\":\"VID\",
	//	// \"Type\":0, \"Value\":\"c1e74ab7-c2ed-4f63-912e-b9f016a17983\" }, {
	//	// \"Name\":\"POD\", \"Type\":3, \"Value\":\"1500357780666\" }, {
	//	// \"Name\":\"state\", \"Type\":1, \"Value\":\"0\" } ] } }";

	//	String actionId = kaAu.sendPurchaseOrderToKaizala(accessToken, groupId, strBodyJSON);
	//	transaction.setTransactionUnavailableVendorCustomer(groupId); // groupID
	//	transaction.setProcurementStatus(actionId);
	//	genericDAO.saveOrUpdate(transaction, user, entityManager);
	//	return strBodyJSON;
	//}

	//public String checkIfGroupExist(KaizalaAuthorization kaAu, String accessToken, String groupName,
	//		String assignToPhNo, String poGeneratorPhNo, EntityManager entityManager, Users user) {
	//	StringBuilder sbquery = new StringBuilder("");
	//	sbquery.append("select obj from KaizalaGroups obj WHERE obj.assignedToPhNo='" + assignToPhNo
	//			+ "' and obj.poGeneratorPhNo='" + poGeneratorPhNo + "' and obj.presentStatus=1");
	//	List<KaizalaGroups> groups = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
	//	String groupId = "";
	//	if (groups != null && groups.size() > 0) {
	//		KaizalaGroups group = groups.get(0);
	//		groupId = group.getGroupId();
	//	} else {
	//		groupId = kaAu.getGroupId(accessToken, groupName, assignToPhNo, poGeneratorPhNo);
	//		KaizalaGroups newGroup = new KaizalaGroups();
	//		newGroup.setAssignedToPhNo(assignToPhNo);
	//		newGroup.setPoGeneratorPhNo(poGeneratorPhNo);
	//		newGroup.setGroupId(groupId);
	//		genericDAO.saveOrUpdate(newGroup, user, entityManager);
	//	}
	//	return groupId;
	//}

	//public void sendPurchaseOrderProcessingDone(Transaction transaction, EntityManager entityManager,
	//		EntityTransaction entitytransaction) {
	//	KaizalaAuthorization kaAu = new KaizalaAuthorization(jpaApi, application);
	//	String accessToken = kaAu.getAccessToken();
	//	String groupId = transaction.getTransactionUnavailableVendorCustomer(); // groupID
	//	String actionId = transaction.getProcurementStatus();// action id
	//	kaAu.sendResponse(accessToken, groupId, actionId, true);
	//}

	@Override
	public PurchaseOrderTxnModel approverAction(Users user, EntityManager em, JsonNode json, ObjectNode result) throws IDOSException {
		return CREATE_PURCHASE_ORDER_TXN_DAO.approverAction(user, em, json, result);
	}

	@Override
	public PurchaseOrderTxnModel submitForApprovalPurchaseOrder(Users user, JsonNode json, EntityManager em, ObjectNode result) throws IDOSException {
		return CREATE_PURCHASE_ORDER_TXN_DAO.submitForApproval(user, json, em, result);
	}
    
    @Override
	public void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException {
		CREATE_PURCHASE_ORDER_TXN_ITEM_DAO.getListOfTxnItems(user, em, txnId, txnItemsAn);
	}
}
