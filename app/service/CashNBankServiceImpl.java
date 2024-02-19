package service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.DateUtil;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import model.ClaimTransaction;
import model.IdosProvisionJournalEntry;
import model.Transaction;
import model.Users;
import model.UsersRoles;

import model.payroll.PayrollTransaction;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.jpa.JPAApi;
import play.libs.Json;
import javax.inject.Inject;
import com.idos.dao.CashNBankDAO;
import com.idos.dao.CashNBankDAOImpl;
import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import play.Application;
import controllers.StaticController;

public class CashNBankServiceImpl implements CashNBankService {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public ObjectNode displayCashBook(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) {
		result = cashNBankDAO.displayCashBook(result, json, user, entityManager);
		return result;
	}

	@Override
	public ObjectNode displayBankBook(ObjectNode result, JsonNode json, Users user, EntityManager entityManager) {
		result = cashNBankDAO.displayBankBook(result, json, user, entityManager);
		return result;
	}

	@Override
	public String exportCashNBank(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction, String path, Application application) {
		return cashNBankDAO.exportCashAndBankBook(result, json, user, entityManager, entitytransaction, path,
				application);
	}

	@Override
	public ObjectNode getData(String id, String type, Users user, EntityManager entityManager) {
		log.log(Level.FINE, ">>>> Start Reference No: " + id + " Type: " + type);
		// EntityManager entityManager= jpaApi.em();
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		if (null == id) {
			result.put("message", "Transaction ID not specified.");
		} else if (null == type) {
			result.put("message", "Transaction type not specified.");
		} else {
			result.put("transactionRefNumber", id);
			Map<String, Object> criterias = new HashMap<String, Object>();
			if ("transactionLookUp".equalsIgnoreCase(type) || "BulkTransactionLookUp".equalsIgnoreCase(type)) {
				criterias.put("transactionRefNumber", id);
				criterias.put("presentStatus", 1);
				Transaction transaction = genericDAO.getByCriteria(Transaction.class, criterias, entityManager);
				if (null != transaction && null != transaction.getId()) {
					result.put("id", transaction.getId());
					result.put("type", 1);
					if (transaction.getTransactionPurpose()
							.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
						String branchName = "";
						if (null != transaction.getTransactionBranch()
								&& null != transaction.getTransactionBranch().getName()) {
							branchName = transaction.getTransactionBranch().getName();
						}
						if (null != transaction.getTransactionToBranch()
								&& null != transaction.getTransactionToBranch().getName()) {
							branchName += " to " + transaction.getTransactionToBranch().getName();
						}
						result.put("branchName", branchName);
						String txnPurpose = transaction.getTransactionPurpose().getTransactionPurpose();
						if (transaction.getTypeIdentifier() == 1) {
							txnPurpose += " (Outward)";
						} else {
							txnPurpose += " (Inward)";
						}
						result.put("transactionPurpose", txnPurpose);

					} else {
						if (null == transaction.getTransactionBranch()
								|| null == transaction.getTransactionBranch().getName()) {
							result.put("branchName", "");
						} else {
							result.put("branchName", transaction.getTransactionBranch().getName());
						}
						if (null == transaction.getTransactionPurpose()
								&& null == transaction.getTransactionPurpose().getTransactionPurpose()) {
							result.put("transactionPurpose", "");
						} else {
							result.put("transactionPurpose",
									transaction.getTransactionPurpose().getTransactionPurpose());
						}
					}
					result.put("txnPurposeID", transaction.getTransactionPurpose().getId());
					if (null == transaction.getTransactionProject()
							|| null == transaction.getTransactionProject().getName()) {
						result.put("projectName", "");
					} else {
						result.put("projectName", transaction.getTransactionProject().getName());
					}
					if (null == transaction.getTransactionSpecifics()
							|| null == transaction.getTransactionSpecifics().getName()) {
						result.put("itemName", "");
					} else {
						result.put("itemName", transaction.getTransactionSpecifics().getName());
					}
					if (null == transaction.getTransactionSpecifics()) {
						result.put("itemParentName", "");
					} else {
						if (null == transaction.getTransactionSpecifics().getParentSpecifics()
								|| null == transaction.getTransactionSpecifics().getParentSpecifics().getName()) {
							result.put("itemParentName", "");
						} else {
							result.put("itemParentName",
									transaction.getTransactionSpecifics().getParentSpecifics().getName());
						}
					}
					if (null == transaction.getBudgetAvailDuringTxn()) {
						result.put("budgetAvailable", "");
						result.put("budgetAvailableAmt", "");
					} else {
						String[] budgetAvailableArr = transaction.getBudgetAvailDuringTxn().split(":");
						result.put("budgetAvailable", budgetAvailableArr[0]);
						if (budgetAvailableArr.length > 1) {
							result.put("budgetAvailableAmt", budgetAvailableArr[1]);
						} else {
							result.put("budgetAvailableAmt", "");
						}
					}
					if (null == transaction.getActualAllocatedBudget()) {
						result.put("budgetAllocated", "");
						result.put("budgetAllocatedAmt", "");
					} else {
						String[] budgetAllocatedArr = transaction.getActualAllocatedBudget().split(":");
						result.put("budgetAllocated", budgetAllocatedArr[0]);
						if (budgetAllocatedArr.length > 1) {
							result.put("budgetAllocatedAmt", budgetAllocatedArr[1]);
						} else {
							result.put("budgetAllocatedAmt", "");
						}
					}

					if (null == transaction.getTransactionVendorCustomer()) {
						if (null == transaction.getTransactionUnavailableVendorCustomer()) {
							result.put("customerVendorName", "");
						} else {
							result.put("customerVendorName", transaction.getTransactionUnavailableVendorCustomer());
						}
					} else {
						result.put("customerVendorName", transaction.getTransactionVendorCustomer().getName());
					}

					if (null == transaction.getTransactionDate()) {
						result.put("txnDate", "");
					} else {
						result.put("txnDate", StaticController.idosdf.format(transaction.getTransactionDate()));
					}
					result.put("invoiceDateLabel", "INVOICE DATE:");
					if (null == transaction.getTransactionInvoiceDate()) {
						result.put("invoiceDate", "");
					} else {
						result.put("invoiceDate",
								StaticController.idosdf.format(transaction.getTransactionInvoiceDate()));
					}
					if (null == transaction.getReceiptDetailsType()) {
						result.put("paymentMode", "");
					} else {
						if (1 == transaction.getReceiptDetailsType()) {
							result.put("paymentMode", "CASH");
						} else if (2 == transaction.getReceiptDetailsType()) {
							result.put("paymentMode", "BANK");
						} else {
							result.put("paymentMode", "");
						}
					}
					result.put("instrumentNumber",
							transaction.getInstrumentNumber() == null ? "" : transaction.getInstrumentNumber());
					result.put("instrumentDate",
							transaction.getInstrumentDate() == null ? "" : transaction.getInstrumentDate());
					if (null == transaction.getNoOfUnits()) {
						result.put("noOfUnit", 0);
					} else {
						result.put("noOfUnit", transaction.getNoOfUnits());
					}
					if (null == transaction.getPricePerUnit()) {
						result.put("unitPrice", 0.0);
					} else {
						result.put("unitPrice", IdosConstants.decimalFormat.format(transaction.getPricePerUnit()));
					}
					if (null == transaction.getGrossAmount()) {
						result.put("grossAmount", 0.0);
					} else {
						result.put("grossAmount", IdosConstants.decimalFormat.format(transaction.getGrossAmount()));
					}
					if (null == transaction.getNetAmount()) {
						result.put("netAmount", 0.0);
					} else {
						result.put("netAmount", IdosConstants.decimalFormat.format(transaction.getNetAmount()));
					}
					if (null == transaction.getNetAmountResultDescription()
							|| "null".equalsIgnoreCase(transaction.getNetAmountResultDescription())) {
						result.put("netAmtDesc", "");
					} else {
						result.put("netAmtDesc", transaction.getNetAmountResultDescription());
					}
					if (null == transaction.getTransactionStatus()) {
						result.put("status", "");
					} else {
						result.put("status", transaction.getTransactionStatus());
					}
					if (null == transaction.getCreatedBy() && null == transaction.getCreatedBy().getEmail()) {
						result.put("createdBy", "");
					} else {
						result.put("createdBy", transaction.getCreatedBy().getEmail());
					}
					if (null == transaction.getApproverActionBy()
							|| null == transaction.getApproverActionBy().getEmail()) {
						result.put("approverLabel", "");
						result.put("approverEmail", "");
					} else {
						result.put("approverLabel", "APPROVER:");
						result.put("approverEmail", transaction.getApproverActionBy().getEmail());
					}
					if (null == transaction.getSupportingDocs()) {
						result.put("txnDocument", "");
					} else {
						result.put("txnDocument", transaction.getSupportingDocs());
					}
					if (null == transaction.getRemarks()) {
						result.put("remarks", "");
					} else {
						result.put("remarks", transaction.getRemarks());
					}
					String txnSpecialStatus = "";
					if (null != transaction.getTransactionExceedingBudget()
							&& null != transaction.getKlFollowStatus()) {
						if (1 == transaction.getTransactionExceedingBudget() && 0 == transaction.getKlFollowStatus()) {
							txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
						} else if (1 == transaction.getTransactionExceedingBudget()
								&& 1 == transaction.getKlFollowStatus()) {
							txnSpecialStatus = "Transaction Exceeding Budget";
						} else if (0 == transaction.getTransactionExceedingBudget()
								&& 1 == transaction.getKlFollowStatus()) {
							txnSpecialStatus = "Rules Not Followed";
						} else if (0 == transaction.getKlFollowStatus()) {
							txnSpecialStatus = "Rules Not Followed";
						}
					} else if (null != transaction.getTransactionExceedingBudget()
							&& null == transaction.getKlFollowStatus()) {
						txnSpecialStatus = "Transaction Exceeding Budget";
					} else if (null != transaction.getDocRuleStatus()
							&& null != transaction.getTransactionExceedingBudget()) {
						if (1 == transaction.getDocRuleStatus() && 1 == transaction.getTransactionExceedingBudget()) {
							txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
						}
						if (1 == transaction.getDocRuleStatus() && 0 == transaction.getTransactionExceedingBudget()) {
							txnSpecialStatus = "Rules Not Followed";
						}
					} else if (null != transaction.getDocRuleStatus()
							&& null == transaction.getTransactionExceedingBudget()) {
						txnSpecialStatus = "Rules Not Followed";
					}
					result.put("txnSpecialStatus", txnSpecialStatus);
					if (null == transaction.getApproverEmails()) {
						result.put("approverEmails", "");
					} else {
						result.put("approverEmails", transaction.getApproverEmails());
					}
					if (null == transaction.getAdditionalApproverEmails()) {
						result.put("additionalapproverEmails", "");
					} else {
						result.put("additionalapproverEmails", transaction.getAdditionalApproverEmails());
					}
					if (null == transaction.getSelectedAdditionalApprover()) {
						result.put("selectedAdditionalApproval", "");
					} else {
						result.put("selectedAdditionalApproval", transaction.getSelectedAdditionalApprover());
					}
					String roles = "";
					if (null != user) {
						StringBuilder sb = new StringBuilder();
						sb.append("select obj from UsersRoles obj where obj.user='" + user.getId()
								+ "' and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id");
						List<UsersRoles> userRoles = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
						for (UsersRoles role : userRoles) {
							if (!role.getRole().getName().equals("OFFICERS")) {
								roles += role.getRole().getName() + ",";
							}
						}
						roles = roles.substring(0, roles.length() - 1);
					}
					result.put("roles", roles);
					if (null != user && (null != user.getEmail() || !"".equals(user.getEmail()))) {
						result.put("useremail", user.getEmail());
					} else {
						result.put("useremail", "");
					}
					result.put("result", true);
					result.remove("message");
				}
			} else if ("claimTransactionLookUp".equalsIgnoreCase(type)) {
				criterias.put("transactionRefNumber", id);
				criterias.put("presentStatus", 1);
				ClaimTransaction claimTransaction = genericDAO.getByCriteria(ClaimTransaction.class, criterias,
						entityManager);
				if (null != claimTransaction && null != claimTransaction.getId()) {
					result.put("id", claimTransaction.getId());
					result.put("type", 2);
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
					String travelType = claimTransaction.getTravelType() != null ? claimTransaction.getTravelType()
							: "";
					String noOfPlacesToVisit = claimTransaction.getNumberOfPlacesToVisit() != null
							? claimTransaction.getNumberOfPlacesToVisit().toString()
							: "";
					String placesSelectedOrEntered = claimTransaction.getTravelFromToPlaces() != null
							? claimTransaction.getTravelFromToPlaces()
							: "";
					String typeOfCity = claimTransaction.getTypeOfCity() != null ? claimTransaction.getTypeOfCity()
							: "";
					String appropriateDiatance = claimTransaction.getAppropriateDistance() != null
							? claimTransaction.getAppropriateDistance()
							: "";
					String totalDays = claimTransaction.getTotalDays() != null
							? claimTransaction.getTotalDays().toString()
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
					Double enteredAdvance = claimTransaction.getGrossAmount() != null
							? claimTransaction.getGrossAmount()
							: 0.0;
					Double totalAdvance = claimTransaction.getNewAmount() != null ? claimTransaction.getNewAmount()
							: 0.0;
					String purposeOfVisit = claimTransaction.getPurposeOfVisit() != null
							? claimTransaction.getPurposeOfVisit()
							: "";
					String claimTxnRemarks = claimTransaction.getTxnRemarks() != null ? claimTransaction.getTxnRemarks()
							: "";
					String supportingDoc = claimTransaction.getSupportingDocuments() != null
							? claimTransaction.getSupportingDocuments()
							: "";
					String claimdebitCredit = claimTransaction.getDebitCredit() != null
							? claimTransaction.getDebitCredit()
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
					String createdBy = claimTransaction.getCreatedBy() != null
							? claimTransaction.getCreatedBy().getEmail()
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
					Double expenseAdvanceTotalAdvanceAmount = claimTransaction
							.getTotalAdvanceForExpenseDuringTxn() != null
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
					Double amountReturnInCaseOfDueToCompany = claimTransaction
							.getAmountReturnInCaseOfDueToCompany() != null
									? claimTransaction.getAmountReturnInCaseOfDueToCompany()
									: 0.0;
					result.put("txnPurposeId", txnPurposeId);
					result.put("branchName", branchName);
					result.put("projectName", projectName);
					result.put("txnQuestionName", txnQuestionName);
					result.put("txnOrgnName", txnOrgnName);
					result.put("claimtravelType", travelType);
					result.put("claimnoOfPlacesToVisit", noOfPlacesToVisit);
					result.put("claimplacesSelectedOrEntered", placesSelectedOrEntered);
					result.put("claimtypeOfCity", typeOfCity);
					result.put("claimappropriateDiatance", appropriateDiatance);
					result.put("claimtotalDays", totalDays);
					result.put("claimtravelDetailedConfDescription", travelDetailedConfDescription);
					result.put("existingClaimsCurrentSettlementDetails", existingClaimsCurrentSettlementDetails);
					result.put("claimexistingAdvance", IdosConstants.decimalFormat.format(existingAdvance));
					result.put("claimuserAdvanveEligibility", claimuserAdvanveEligibility);
					result.put("userExpenditureOnThisTxn", userExpenditureOnThisTxn);
					result.put("claimadjustedAdvance", adjustedAdvance);
					result.put("claimenteredAdvance", enteredAdvance);
					result.put("claimtotalAdvance", totalAdvance);
					result.put("netSettlementAmount", netSettlementAmount);
					result.put("dueSettlementAmount", dueSettlementAmount);
					result.put("requiredSettlement", requiredSettlement);
					result.put("returnSettlement", returnSettlement);
					result.put("claimpurposeOfVisit", purposeOfVisit);
					result.put("claimtxnRemarks", claimTxnRemarks);
					result.put("claimsupportingDoc", supportingDoc);
					result.put("debitCredit", claimdebitCredit);
					result.put("claimTxnStatus", claimTxnStatus);
					result.put("approverEmails", claimapproverEmails);
					result.put("additionalApprovarUsers", additionalApprovalEmails);
					result.put("selectedAdditionalApproval", claimselectedAdditionalApproval);
					result.put("creatorLabel", creatorLabel);
					result.put("createdBy", createdBy);
					result.put("transactionDate", transactionDate);
					result.put("approverLabel", approverLabel);
					result.put("approvedBy", approvedBy);
					result.put("accountantEmails", accountantEmails);
					result.put("accountedLabel", accountedLabel);
					result.put("accountedBy", accountedBy);
					result.put("txnSpecialStatus", txnSpecialStatus);
					result.put("paymentMode", paymentMode);
					result.put("expenseAdvanceTotalAdvanceAmount", expenseAdvanceTotalAdvanceAmount);
					result.put("expenseAdvancepurposeOfExpenseAdvance", expenseAdvancepurposeOfExpenseAdvance);
					result.put("itemName", itemName);
					result.put("itemParticularName", itemParticularName);
					result.put("parentSpecificName", parentSpecificName);
					result.put("dueFromCompany", dueFromCompany);
					result.put("dueToCompany", dueToCompany);
					result.put("amountReturnInCaseOfDueToCompany", amountReturnInCaseOfDueToCompany);
					StringBuilder sb = new StringBuilder();
					sb.append("select obj from UsersRoles obj where obj.user='" + user.getId()
							+ "' and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id");
					List<UsersRoles> userRoles = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
					String roles = "";
					for (UsersRoles role : userRoles) {
						if (!role.getRole().getName().equals("OFFICERS")) {
							roles += role.getRole().getName() + ",";
						}
					}
					roles = roles.substring(0, roles.length() - 1);
					result.put("roles", roles);
					result.put("userroles", roles);
					if (null != user && (null != user.getEmail() || !"".equals(user.getEmail()))) {
						result.put("useremail", user.getEmail());
					} else {
						result.put("useremail", "");
					}
					result.put("instrumentNumber", claimTransaction.getInstrumentNumber() == null ? ""
							: claimTransaction.getInstrumentNumber());
					result.put("instrumentDate",
							claimTransaction.getInstrumentDate() == null ? "" : claimTransaction.getInstrumentDate());
					result.put("result", true);
					result.remove("message");
				}
			} else if ("provisionalTransactionLookUp".equalsIgnoreCase(type)) {
				criterias.put("transactionRefNumber", id);
				criterias.put("presentStatus", 1);
				IdosProvisionJournalEntry transaction = genericDAO.getByCriteria(IdosProvisionJournalEntry.class,
						criterias, entityManager);
				if (null != transaction && null != transaction.getId()) {
					result.put("id", transaction.getId());
					result.put("type", 3);
					if (null == transaction.getDebitBranch() || null == transaction.getDebitBranch().getName()) {
						result.put("branchName", "");
					} else {
						result.put("branchName", transaction.getDebitBranch().getName());
					}
					result.put("projectName", "");

					StringBuilder itemParentName = new StringBuilder();
					StringBuilder creditItemsName = new StringBuilder();
					StringBuilder debitItemsName = new StringBuilder();
					provisionJournalDAO.getProvisionJournalEntryDetail(entityManager, transaction, itemParentName,
							creditItemsName, debitItemsName);
					result.put("itemName", IdosUtil.removeLastChar(debitItemsName.toString()) + "|"
							+ IdosUtil.removeLastChar(creditItemsName.toString()));
					result.put("debitItemsName", IdosUtil.removeLastChar(debitItemsName.toString()));
					result.put("creditItemsName", IdosUtil.removeLastChar(creditItemsName.toString()));
					result.put("itemParentName", IdosUtil.removeLastChar(itemParentName.toString()));
					result.put("budgetAvailable", "");
					result.put("budgetAvailableAmt", "");
					result.put("customerVendorName", "");
					if (null == transaction.getTransactionPurpose()) {
						result.put("transactionPurpose", "");
					} else {
						result.put("transactionPurpose", transaction.getTransactionPurpose().getTransactionPurpose());
					}
					if (null == transaction.getTransactionDate()) {
						result.put("txnDate", "");
					} else {
						result.put("txnDate", StaticController.idosdf.format(transaction.getTransactionDate()));
					}
					String invoiceDate = "";
					String invoiceDateLabel = "";
					if (null != transaction.getReversalDate()) {
						invoiceDateLabel = "REVERSAL DATE:";
						invoiceDate = StaticController.idosdf.format(transaction.getReversalDate());
					}
					result.put("invoiceDateLabel", invoiceDateLabel);
					result.put("invoiceDate", invoiceDate);
					result.put("paymentMode", "");
					result.put("noOfUnit", "");
					result.put("unitPrice", "");
					if (null == transaction.getTotalDebitAmount()) {
						result.put("grossAmount", 0.0);
						result.put("netAmount", 0.0);
					} else {
						result.put("grossAmount",
								IdosConstants.decimalFormat.format(transaction.getTotalDebitAmount()));
						result.put("netAmount", IdosConstants.decimalFormat.format(transaction.getTotalDebitAmount()));
					}
					String txnResultDesc = "";
					if (null != transaction.getPurpose() && !"null".equals(transaction.getPurpose())) {
						txnResultDesc = transaction.getPurpose();
					}
					result.put("netAmtDesc", txnResultDesc);
					if (null == transaction.getTransactionStatus()) {
						result.put("status", "");
					} else {
						result.put("status", transaction.getTransactionStatus());
					}
					if (null == transaction.getCreatedBy() && null == transaction.getCreatedBy().getEmail()) {
						result.put("createdBy", "");
					} else {
						result.put("createdBy", transaction.getCreatedBy().getEmail());
					}
					if (null == transaction.getApproverActionBy()
							|| null == transaction.getApproverActionBy().getEmail()) {
						result.put("approverLabel", "");
						result.put("approverEmail", "");
					} else {
						result.put("approverLabel", "APPROVER:");
						result.put("approverEmail", transaction.getApproverActionBy().getEmail());
					}
					if (null == transaction.getSupportingDocuments()) {
						result.put("txnDocument", "");
					} else {
						result.put("txnDocument", transaction.getSupportingDocuments());
					}
					if (null == transaction.getTxnRemarks()) {
						result.put("remarks", "");
					} else {
						result.put("remarks", transaction.getTxnRemarks());
					}
					String txnSpecialStatus = "";
					result.put("txnSpecialStatus", txnSpecialStatus);
					if (null == transaction.getAdditionalApproverUserEmails()) {
						result.put("additionalapproverEmails", "");
					} else {
						result.put("additionalapproverEmails", transaction.getAdditionalApproverUserEmails());
					}
					if (null == transaction.getApproverEmails()) {
						result.put("approverEmails", "");
					} else {
						result.put("approverEmails", transaction.getApproverEmails());
					}
					if (null == transaction.getSelectedAdditionalApprover()) {
						result.put("selectedAdditionalApproval", "");
					} else {
						result.put("selectedAdditionalApproval", transaction.getSelectedAdditionalApprover());
					}
					StringBuilder sb = new StringBuilder();
					sb.append("select obj from UsersRoles obj where obj.user='" + user.getId()
							+ "' and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id");
					List<UsersRoles> userRoles = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
					String roles = "";
					for (UsersRoles role : userRoles) {
						if (!role.getRole().getName().equals("OFFICERS")) {
							roles += role.getRole().getName() + ",";
						}
					}
					roles = roles.substring(0, roles.length() - 1);
					result.put("roles", roles);
					if (null != user && (null != user.getEmail() || !"".equals(user.getEmail()))) {
						result.put("useremail", user.getEmail());
					} else {
						result.put("useremail", "");
					}
					result.put("instrumentNumber",
							transaction.getInstrumentNumber() == null ? "" : transaction.getInstrumentNumber());
					result.put("instrumentDate",
							transaction.getInstrumentDate() == null ? "" : transaction.getInstrumentDate());
					result.put("result", true);
					result.remove("message");
				}
			} else if ("payTransactionLookUp".equalsIgnoreCase(type)) {
				criterias.put("transactionRefNumber", id);
				criterias.put("presentStatus", 1);
				PayrollTransaction transaction = genericDAO.getByCriteria(PayrollTransaction.class, criterias,
						entityManager);

				if (null != transaction && null != transaction.getId()) {
					result.put("id", transaction.getId());
					result.put("type", 1);

					if (null == transaction.getBranch() || null == transaction.getBranch().getName()) {
						result.put("branchName", "");
					} else {
						result.put("branchName", transaction.getBranch().getName());
					}
					if (null == transaction.getTransactionPurpose()
							&& null == transaction.getTransactionPurpose().getTransactionPurpose()) {
						result.put("transactionPurpose", "");
					} else {
						result.put("transactionPurpose", transaction.getTransactionPurpose().getTransactionPurpose());
					}

					result.put("txnPurposeID", transaction.getTransactionPurpose().getId());
					result.put("projectName", "");

					String itemName = DateUtil.getMonthName(transaction.getPayslipMonth());
					itemName += "," + transaction.getPayslipYear();
					result.put("itemName", itemName);

					result.put("itemParentName", "");
					result.put("budgetAvailable", "");
					result.put("budgetAvailableAmt", "");
					result.put("budgetAllocated", "");
					result.put("budgetAllocatedAmt", "");
					result.put("customerVendorName", "");

					if (null == transaction.getTransactionDate()) {
						result.put("txnDate", "");
					} else {
						result.put("txnDate", StaticController.idosdf.format(transaction.getTransactionDate()));
					}
					result.put("invoiceDateLabel", "INVOICE DATE:");
					if (null == transaction.getTransactionDate()) {
						result.put("invoiceDate", "");
					} else {
						result.put("invoiceDate", StaticController.idosdf.format(transaction.getTransactionDate()));
					}
					if (null == transaction.getPayMode()) {
						result.put("paymentMode", "");
					} else {
						if ("CASH".equalsIgnoreCase(transaction.getPayMode())) {
							result.put("paymentMode", "CASH");
						} else if ("BANK".equalsIgnoreCase(transaction.getPayMode())) {
							result.put("paymentMode", "BANK");
						} else {
							result.put("paymentMode", "");
						}
					}
					result.put("instrumentNumber",
							transaction.getInstrumentNumber() == null ? "" : transaction.getInstrumentNumber());
					result.put("instrumentDate",
							transaction.getInstrumentDate() == null ? "" : transaction.getInstrumentDate());

					result.put("noOfUnit", "");

					result.put("unitPrice", "");

					if (null == transaction.getTotalTotalIncome()) {
						result.put("grossAmount", 0.0);
					} else {
						result.put("grossAmount",
								IdosConstants.decimalFormat.format(transaction.getTotalTotalIncome()));
					}
					if (null == transaction.getTotalNetPay()) {
						result.put("netAmount", 0.0);
					} else {
						result.put("netAmount", IdosConstants.decimalFormat.format(transaction.getTotalNetPay()));
					}
					result.put("netAmtDesc", "");

					if (null == transaction.getTransactionStatus()) {
						result.put("status", "");
					} else {
						result.put("status", transaction.getTransactionStatus());
					}
					if (null == transaction.getCreatedBy() && null == transaction.getCreatedBy().getEmail()) {
						result.put("createdBy", "");
					} else {
						result.put("createdBy", transaction.getCreatedBy().getEmail());
					}
					if (null == transaction.getApproverActionBy()
							|| null == transaction.getApproverActionBy().getEmail()) {
						result.put("approverLabel", "");
						result.put("approverEmail", "");
					} else {
						result.put("approverLabel", "APPROVER:");
						result.put("approverEmail", transaction.getApproverActionBy().getEmail());
					}
					if (null == transaction.getSupportingDocs()) {
						result.put("txnDocument", "");
					} else {
						result.put("txnDocument", transaction.getSupportingDocs());
					}
					if (null == transaction.getRemarks()) {
						result.put("remarks", "");
					} else {
						result.put("remarks", transaction.getRemarks());
					}

					result.put("txnSpecialStatus", "");
					if (null == transaction.getApproverEmails()) {
						result.put("approverEmails", "");
					} else {
						result.put("approverEmails", transaction.getApproverEmails());
					}
					if (null == transaction.getAdditionalApproverEmails()) {
						result.put("additionalapproverEmails", "");
					} else {
						result.put("additionalapproverEmails", transaction.getAdditionalApproverEmails());
					}
					if (null == transaction.getSelectedAdditionalApprover()) {
						result.put("selectedAdditionalApproval", "");
					} else {
						result.put("selectedAdditionalApproval", transaction.getSelectedAdditionalApprover());
					}
					String roles = "";
					if (null != user) {
						StringBuilder sb = new StringBuilder();
						sb.append("select obj from UsersRoles obj where obj.user='" + user.getId()
								+ "' and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id");
						List<UsersRoles> userRoles = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
						for (UsersRoles role : userRoles) {
							if (!role.getRole().getName().equals("OFFICERS")) {
								roles += role.getRole().getName() + ",";
							}
						}
						roles = roles.substring(0, roles.length() - 1);
					}
					result.put("roles", roles);
					if (null != user && (null != user.getEmail() || !"".equals(user.getEmail()))) {
						result.put("useremail", user.getEmail());
					} else {
						result.put("useremail", "");
					}
					result.put("result", true);
					result.remove("message");
				}

			} else {
				result.put("message", "Transaction type not specified.");
			}
		}
		log.log(Level.FINE, ">>>> End" + result);
		return result;
	}

}
