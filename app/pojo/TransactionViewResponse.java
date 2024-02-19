package pojo;

import java.util.Map;

import org.omg.PortableServer.POAPackage.ObjectNotActive;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idos.util.IdosConstants;

public class TransactionViewResponse {
    public static void addActionTxn(final Long id, final String branchName, final String projectName,
            final String itemName, final String itemParentName, final String budgetAllocated,
            final String budgetAllocatedAmt, final String budgetAvailable, final String budgetAvailableAmt,
            final String customerVendorName, final String transactionPurpose, final String txnDate,
            final String invoiceDateLabel, final String invoiceDate, final String paymentMode, final Double noOfUnits,
            final Double perUnitPrice, final Double grossAmt, final Double netAmount, final String netAmtDesc,
            final String outstandings, final String status, final String createdBy, final String approverLabel,
            final String approverEmail, final String txnDocument, final String txnRemarks, final String debitCredit,
            final String approverEmails, final String additionalApprovalEmails, final String selectedAdditionalApproval,
            final String txnSpecialStatus,
            final Double frieghtCharges, final String poReference, final String txnInstrumentNumber,
            final String txnInstrumentDate, final Long transactionPurposeID, final String txnRemarksPrivate,
            final String serialNumber, int txnIdentifier, String txnRefNo, final Long branchId,
            final Double totalDeductions, final int workingDays, final int typeOfSupply, ObjectNode result) {

        // if (transactionPurpose.equals("Buy on credit & pay later")
        // || transactionPurpose.equals("Buy on cash & pay right away")
        // || transactionPurpose.equals("Sell on cash & collect payment now")
        // || transactionPurpose.equals("Sell on credit & collect payment later")
        // || transactionPurpose.equals("Pay vendor/supplier")
        // || transactionPurpose.equals("Pay advance to vendor or supplier")
        // || transactionPurpose.equals("Receive payment from customer")
        // || transactionPurpose.equals("Receive advance from customer")
        // || transactionPurpose.equals("Buy on Petty Cash Account")
        // || transactionPurpose.equals(" Transfer main cash to petty cash")) {
        // result.put("txnType", "sellExpenseTxn");
        // } else
        if (transactionPurpose.equals("Make Provision/Journal Entry")) {
            result.put("txnType", "expenseAssetsLiabilitiesProvisionTxn");
        } else if (transactionPurpose.equalsIgnoreCase("process payroll")) {
            result.put("txnType", "processPayroll");
        } else {
            result.put("txnType", "sellExpenseTxn");
        }
        // Withdraw Cash From Bank
        result.put("id", id);
        result.put("branchName", branchName);
        result.put("projectName", projectName);
        result.put("itemName", itemName);
        result.put("itemParentName", itemParentName);
        result.put("budgetAllocated", budgetAllocated);
        result.put("budgetAllocatedAmt", budgetAllocatedAmt);
        result.put("budgetAvailable", budgetAvailable);
        result.put("budgetAvailableAmt", budgetAvailableAmt);
        result.put("customerVendorName", customerVendorName);
        result.put("transactionPurpose", transactionPurpose);
        result.put("txnDate", txnDate);
        result.put("invoiceDateLabel", invoiceDateLabel);
        result.put("invoiceDate", invoiceDate);
        result.put("paymentMode", paymentMode);
        result.put("noOfUnit", noOfUnits);
        try {
            if (perUnitPrice != null) {
                result.put("unitPrice", IdosConstants.decimalFormat.format(perUnitPrice));
            } else {
                result.put("unitPrice", "");
            }
            if (grossAmt != null) {
                result.put("grossAmount", IdosConstants.decimalFormat.format(grossAmt));
            } else {
                result.put("grossAmount", "");
            }
            if (netAmount != null) {
                result.put("netAmount", IdosConstants.decimalFormat.format(netAmount));
            } else {
                result.put("netAmount", "");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Error : " + e.getMessage());
        }
        result.put("netAmtDesc", netAmtDesc);
        result.put("outstandings", outstandings);
        result.put("status", status);
        result.put("createdBy", createdBy);
        result.put("approverLabel", approverLabel);
        result.put("approverEmail", approverEmail);
        result.put("txnDocument", txnDocument);
        result.put("txnRemarks", txnRemarks);
        result.put("debitCredit", debitCredit);
        result.put("approverEmails", approverEmails);
        result.put("additionalApprovarUsers", additionalApprovalEmails);
        result.put("selectedAdditionalApproval", selectedAdditionalApproval);
        result.put("txnSpecialStatus", txnSpecialStatus);
        result.put("frieghtCharges", frieghtCharges);
        result.put("poReference", poReference);
        result.put("instrumentNumber", txnInstrumentNumber);
        result.put("instrumentDate", txnInstrumentDate);
        result.put("transactionPurposeID", transactionPurposeID);
        result.put("remarksPrivate", txnRemarksPrivate);
        result.put("invoiceNumber", serialNumber);
        result.put("txnRefNo", txnRefNo);
        result.put("branchId", branchId);
        result.put("totalDeductions", totalDeductions);
        result.put("workingDays", workingDays);
        result.put("typeOfSupply", typeOfSupply);
    }

    public static void addTxn(final Long id, final String branchName, final String projectName, final String itemName,
            final String itemParentName, final String customerVendorName, final String transactionPurpose,
            final String txnDate, final String invoiceDateLabel, final String invoiceDate, final String paymentMode,
            final Double noOfUnits, final Double perUnitPrice, final Double grossAmt, final Double netAmount,
            final String netAmtDesc, final String outstandings, final String status, final String createdBy,
            final String approverLabel, final String approverEmail, final String txnDocument, final String txnRemarks,
            final String debitCredit,
            final String txnSpecialStatus, final Double frieghtCharges, final String poReference,
            final String txnInstrumentNumber, final String txnInstrumentDate, final Long transactionPurposeID,
            final String serialNumber, final String txnRefNo, final Integer typeOfSupply, ObjectNode result) {
        // if (transactionPurpose.equals("Buy on credit & pay later")
        // || transactionPurpose.equals("Buy on cash & pay right away")
        // || transactionPurpose.equals("Sell on cash & collect payment now")
        // || transactionPurpose.equals("Sell on credit & collect payment later")
        // || transactionPurpose.equals("Pay vendor/supplier")
        // || transactionPurpose.equals("Pay advance to vendor or supplier")
        // || transactionPurpose.equals("Receive payment from customer")
        // || transactionPurpose.equals("Receive advance from customer")
        // || transactionPurpose.equals("Buy on Petty Cash Account")
        // || transactionPurpose.equals(" Transfer main cash to petty cash")) {
        // result.put("txnType", "sellExpenseTxn");
        // } else
        if (transactionPurpose.equals("Make Provision/Journal Entry")) {
            result.put("txnType", "expenseAssetsLiabilitiesProvisionTxn");
        } else if (transactionPurpose.equalsIgnoreCase("process payroll")) {
            result.put("txnType", "processPayroll");
        } else {
            result.put("txnType", "sellExpenseTxn");
        }

        result.put("id", id);
        result.put("branchName", branchName);
        result.put("projectName", projectName);
        result.put("itemName", itemName);
        result.put("itemParentName", itemParentName);
        result.put("customerVendorName", customerVendorName);
        result.put("transactionPurpose", transactionPurpose);
        result.put("txnDate", txnDate);
        result.put("invoiceDateLabel", invoiceDateLabel);
        result.put("invoiceDate", invoiceDate);
        result.put("paymentMode", paymentMode);
        result.put("noOfUnit", noOfUnits);
        try {
            if (perUnitPrice != null) {
                result.put("unitPrice", IdosConstants.decimalFormat.format(perUnitPrice));
            } else {
                result.put("unitPrice", "");
            }
            if (grossAmt != null) {
                result.put("grossAmount", IdosConstants.decimalFormat.format(grossAmt));
            } else {
                result.put("grossAmount", "");
            }
            if (netAmount != null) {
                result.put("netAmount", IdosConstants.decimalFormat.format(netAmount));
            } else {
                result.put("netAmount", "");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.out.println("Error : " + e.getMessage());
        }
        result.put("netAmtDesc", netAmtDesc);
        result.put("outstandings", outstandings);
        result.put("status", status);
        result.put("createdBy", createdBy);
        result.put("approverLabel", approverLabel);
        result.put("approverEmail", approverEmail);
        result.put("txnDocument", txnDocument);
        result.put("txnRemarks", txnRemarks);
        result.put("debitCredit", debitCredit);
        result.put("txnSpecialStatus", txnSpecialStatus);
        result.put("frieghtCharges", frieghtCharges);
        result.put("poReference", poReference);
        result.put("instrumentNumber", txnInstrumentNumber);
        result.put("instrumentDate", txnInstrumentDate);
        result.put("transactionPurposeID", transactionPurposeID);
        result.put("invoiceNumber", serialNumber);
        result.put("txnRefNo", txnRefNo);
        result.put("typeOfSupply", typeOfSupply);
    }

    public static void addClaimTxn(final Long id, final String branchName, final String projectName,
            final String txnQuestionName, final String txnOrgnName, final String claimtravelType,
            final String claimnoOfPlacesToVisit, final String claimplacesSelectedOrEntered,
            final String claimtypeOfCity, final String claimappropriateDiatance, final String claimtotalDays,
            final String claimtravelDetailedConfDescription, final Double claimexistingAdvance,
            final String claimuserAdvanveEligibility, final Double claimadjustedAdvance,
            final Double claimenteredAdvance,
            final Double claimtotalAdvance, final String claimpurposeOfVisit, final String claimtxnRemarks,
            final String claimsupportingDoc, final String debitCredit, final String claimTxnStatus,
            final String approverEmails, final String additionalApprovalEmails,
            final String selectedAdditionalApproval, final String creatorLabel, final String createdBy,
            final String transactionDate, final String approverLabel, final String approvedBy,
            final String accountantEmails, final String accountedLabel, final String accountedBy,
            final String txnSpecialStatus,
            final String paymentMode, final String instrumentNumber, final String instrumentDate,
            final String userEmail, String claimTxnRefNo, ObjectNode result) {

        if (txnQuestionName.equals("Request For Travel Advance")) {
            result.put("txnType", "claimTxn");
        }
        if (txnQuestionName.equals("Request Advance For Expense")) {
            result.put("txnType", "expenseAdvanceTxn");
        }
        if (txnQuestionName.equals("Settle Advance For Expense")) {
            result.put("txnType", "expAdvanceSettlementTxn");
        }
        if (txnQuestionName.equals("Request For Expense Reimbursement")) {
            result.put("txnType", "expReimbursementTxn");
        }
        if (txnQuestionName.equals("Settle Travel Advance")) {
            result.put("txnType", "claimSettlementTxn");
        }
        result.put("id", id);
        result.put("branchName", branchName);
        result.put("projectName", projectName);
        result.put("txnQuestionName", txnQuestionName);
        result.put("txnOrgnName", txnOrgnName);
        result.put("claimtravelType", claimtravelType);
        result.put("claimnoOfPlacesToVisit", claimnoOfPlacesToVisit);
        result.put("claimplacesSelectedOrEntered", claimplacesSelectedOrEntered);
        result.put("claimtypeOfCity", claimtypeOfCity);
        result.put("claimappropriateDiatance", claimappropriateDiatance);
        result.put("claimtotalDays", claimtotalDays);
        result.put("claimtravelDetailedConfDescription", claimtravelDetailedConfDescription);
        result.put("claimexistingAdvance", IdosConstants.decimalFormat.format(claimexistingAdvance));
        result.put("claimuserAdvanveEligibility", claimuserAdvanveEligibility);
        try {
            result.put("claimexistingAdvance",
                    IdosConstants.decimalFormat.format(claimexistingAdvance));
            result.put("claimadjustedAdvance",
                    IdosConstants.decimalFormat.format(claimadjustedAdvance));
            result.put("claimenteredAdvance",
                    IdosConstants.decimalFormat.format(claimenteredAdvance));
            result.put("claimtotalAdvance", IdosConstants.decimalFormat.format(claimtotalAdvance));
        } catch (NumberFormatException e) {
            System.out.println("Error : " + e.getMessage());
        }

        result.put("claimpurposeOfVisit", claimpurposeOfVisit);
        result.put("claimtxnRemarks", claimtxnRemarks);
        result.put("claimsupportingDoc", claimsupportingDoc);
        result.put("debitCredit", debitCredit);
        result.put("claimTxnStatus", claimTxnStatus);
        result.put("approverEmails", approverEmails);
        result.put("additionalApprovarUsers", additionalApprovalEmails);
        result.put("selectedAdditionalApproval", selectedAdditionalApproval);
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
        result.put("expenseAdvanceTotalAdvanceAmount", "");
        result.put("expenseAdvancepurposeOfExpenseAdvance", "");
        result.put("itemName", "");
        result.put("itemParticularName", "");
        result.put("parentSpecificName", "");
        result.put("parentSpecificName", "");
        result.put("parentSpecificName", "");
        result.put("parentSpecificName", "");
        result.put("dueFromCompany", "");
        result.put("dueToCompany", "");
        result.put("amountReturnInCaseOfDueToCompany", "");
        result.put("instrumentNumber", instrumentNumber);
        result.put("instrumentDate", instrumentDate);
        result.put("claimTxnRefNo", claimTxnRefNo);
    }

    public static void addClaimSettlementTxn(final Long id, final String branchName, final String projectName,
            final String txnQuestionName, final String txnOrgnName, final String travelType,
            final String noOfPlacesToVisit, final String placesSelectedOrEntered, final String typeOfCity,
            final String appropriateDiatance, final String totalDays, final String travelDetailedConfDescription,
            final String existingClaimsCurrentSettlementDetails, final String claimuserAdvanveEligibility,
            final String userExpenditureOnThisTxn, final Double netSettlementAmount, final Double dueSettlementAmount,
            final Double requiredSettlement, final Double returnSettlement, final String purposeOfVisit,
            final String claimdebitCredit, final String claimTxnStatus, final String creatorLabel,
            final String createdBy, final String accountedLabel, final String accountedBy, final String transactionDate,
            final String paymentMode, final String accountantEmails, final String claimTxnRemarks,
            final String supportingDoc, final String instrumentNumber, final String instrumentDate,
            final String userEmail,
            final Double amountReturnInCaseOfDueToCompany, String claimTxnRefNo, ObjectNode result) {
        result.put("txnType", "claimSettlementTxn");
        result.put("id", id);
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
        result.put("travelDetailedConfDescription", travelDetailedConfDescription);
        result.put("existingClaimsCurrentSettlementDetails", existingClaimsCurrentSettlementDetails);
        result.put("claimuserAdvanveEligibility", claimuserAdvanveEligibility);
        result.put("userExpenditureOnThisTxn", userExpenditureOnThisTxn);
        try {
            result.put("netSettlementAmount",
                    IdosConstants.decimalFormat.format(netSettlementAmount));
            result.put("dueSettlementAmount",
                    IdosConstants.decimalFormat.format(dueSettlementAmount));
            result.put("requiredSettlement", IdosConstants.decimalFormat.format(requiredSettlement));
            result.put("returnSettlement", IdosConstants.decimalFormat.format(returnSettlement));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
        }
        result.put("purposeOfVisit", purposeOfVisit);
        result.put("claimdebitCredit", claimdebitCredit);
        result.put("claimTxnStatus", claimTxnStatus);
        result.put("creatorLabel", creatorLabel);
        result.put("createdBy", createdBy);
        result.put("accountedLabel", accountedLabel);
        result.put("accountedBy", accountedBy);
        result.put("transactionDate", transactionDate);
        result.put("paymentMode", paymentMode);
        result.put("claimtxnRemarks", "");
        result.put("claimsupportingDoc", "");
        result.put("accountantEmails", accountantEmails);
        result.put("instrumentNumber", instrumentNumber);
        result.put("instrumentDate", instrumentDate);
        result.put("useremail", userEmail);
        result.put("amountReturnInCaseOfDueToCompany", amountReturnInCaseOfDueToCompany);
        result.put("claimTxnRefNo", claimTxnRefNo);
    }

    public static void addAdvanceExpenseTxn(final Long id, final String branchName, final String projectName,
            final String txnQuestionName, final String txnOrgnName, final String claimtravelType,
            final String claimnoOfPlacesToVisit, final String claimplacesSelectedOrEntered,
            final String claimtypeOfCity, final String claimappropriateDiatance, final String claimtotalDays,
            final String claimtravelDetailedConfDescription, final Double claimexistingAdvance,
            final String claimuserAdvanveEligibility, final Double claimadjustedAdvance,
            final Double claimenteredAdvance,
            final Double claimtotalAdvance, final String claimpurposeOfVisit, final String claimtxnRemarks,
            final String claimsupportingDoc, final String debitCredit, final String claimTxnStatus,
            final String approverEmails, final String additionalApprovalEmails,
            final String selectedAdditionalApproval, final String creatorLabel, final String createdBy,
            final String transactionDate, final String approverLabel, final String approvedBy,
            final String accountantEmails, final String accountedLabel, final String accountedBy,
            final String txnSpecialStatus,
            final String paymentMode,
            final Double expenseAdvanceTotalAdvanceAmount, final String expenseAdvancepurposeOfExpenseAdvance,
            final String itemName, final String itemParticularName, final String parentSpecificName,
            final Double dueFromCompany, final Double dueToCompany, final Double amountReturnInCaseOfDueToCompany,
            final String instrumentNumber, final String instrumentDate, final String userEmail, String claimTxnRefNo,
            ObjectNode result) {
        if (txnQuestionName.equals("Request For Travel Advance")) {
            result.put("txnType", "claimTxn");
        }
        if (txnQuestionName.equals("Request Advance For Expense")) {
            result.put("txnType", "expenseAdvanceTxn");
        }
        if (txnQuestionName.equals("Settle Advance For Expense")) {
            result.put("txnType", "expAdvanceSettlementTxn");
        }
        if (txnQuestionName.equals("Request For Expense Reimbursement")) {
            result.put("txnType", "expReimbursementTxn");
        }
        if (txnQuestionName.equals("Settle Travel Advance")) {
            result.put("txnType", "claimSettlementTxn");
        }
        result.put("id", id);
        result.put("branchName", branchName);
        result.put("projectName", projectName);
        result.put("txnQuestionName", txnQuestionName);
        result.put("txnOrgnName", txnOrgnName);
        result.put("claimtravelType", claimtravelType);
        result.put("claimnoOfPlacesToVisit", claimnoOfPlacesToVisit);
        result.put("claimplacesSelectedOrEntered", claimplacesSelectedOrEntered);
        result.put("claimtypeOfCity", claimtypeOfCity);
        result.put("claimappropriateDiatance", claimappropriateDiatance);
        result.put("claimtotalDays", claimtotalDays);
        result.put("claimtravelDetailedConfDescription", claimtravelDetailedConfDescription);
        result.put("claimexistingAdvance", IdosConstants.decimalFormat.format(claimexistingAdvance));
        result.put("claimuserAdvanveEligibility", claimuserAdvanveEligibility);
        try {
            result.put("claimexistingAdvance",
                    IdosConstants.decimalFormat.format(claimexistingAdvance));
            result.put("claimadjustedAdvance",
                    IdosConstants.decimalFormat.format(claimadjustedAdvance));
            result.put("claimenteredAdvance",
                    IdosConstants.decimalFormat.format(claimenteredAdvance));
            result.put("claimtotalAdvance", IdosConstants.decimalFormat.format(claimtotalAdvance));
        } catch (NumberFormatException e) {
            System.out.println("Error : " + e.getMessage());
        }

        result.put("claimpurposeOfVisit", claimpurposeOfVisit);
        result.put("claimtxnRemarks", claimtxnRemarks);
        result.put("claimsupportingDoc", claimsupportingDoc);
        result.put("debitCredit", debitCredit);
        result.put("claimTxnStatus", claimTxnStatus);
        result.put("approverEmails", approverEmails);
        result.put("additionalApprovarUsers", additionalApprovalEmails);
        result.put("selectedAdditionalApproval", selectedAdditionalApproval);
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
        result.put("parentSpecificName", parentSpecificName);
        result.put("parentSpecificName", parentSpecificName);
        result.put("parentSpecificName", parentSpecificName);
        result.put("dueFromCompany", dueFromCompany);
        result.put("dueToCompany", dueToCompany);
        result.put("amountReturnInCaseOfDueToCompany", amountReturnInCaseOfDueToCompany);
        result.put("instrumentNumber", instrumentNumber);
        result.put("instrumentDate", instrumentDate);
        result.put("claimTxnRefNo", claimTxnRefNo);
    }

    public static void requestHiring(final Long id, final String projectNumber, final String projectTitle,
            final String requester, final String requestType, final String position, final String status,
            final String remarks, final String document, final String approverEmailList, ObjectNode result) {
        result.put("txnType", "sellExpenseTxn");
        result.put("id", id);
        result.put("projectNumber", projectNumber);
        result.put("projectTitle", projectTitle);
        result.put("requester", requester);
        result.put("requetType", requestType);
        result.put("position", position);
        result.put("status", status);
        result.put("workflowAction", "requestHiring");
        result.put("remarks", remarks);
        result.put("document", document);
        result.put("approverEmailList", approverEmailList);
    }

    public static void add(final Long id, final String expenseCreator, final String expenseItem,
            final String expenseVendor, final Integer expenseQty, final Double totalAmount,
            final String expenseActionDate, final String expenseStatus, final String expenseDoc,
            final String txnRemarks, ObjectNode result) {
        result.put("txnType", "sellExpenseTxn");
        result.put("id", id);
        result.put("expenseCreator", expenseCreator);
        result.put("expenseItem", expenseItem);
        result.put("expenseVendor", expenseVendor);
        result.put("expenseQty", expenseQty);
        result.put("totalAmount", totalAmount);
        result.put("expenseActionDate", expenseActionDate);
        result.put("expenseStatus", expenseStatus);
        result.put("expenseDoc", expenseDoc);
        result.put("expenseTxnRemarks", txnRemarks);
        result.put("workflowAction", "addExpense");
    }

    public static void action(final Long id, final String expenseStatus, final String nextApprover,
            final String fileUrl, final String txnRemarks, ObjectNode result) {
        result.put("txnType", "sellExpenseTxn");
        result.put("id", id);
        result.put("status", expenseStatus);
        result.put("nextApprover", nextApprover);
        result.put("fileUrl", fileUrl);
        result.put("expenseTxnRemarks", txnRemarks);
        result.put("workflowAction", "actionExpense");
    }

    public static void online(final boolean result, final ArrayNode node, ObjectNode results) {
        results.put("txnType", "onlineUsers");
        results.put("result", result);
        results.put("users", node);
    }

    public static void chat(final String from, final String to, final String message, final String name,
            ObjectNode result) {
        result.put("type", "chat");
        result.put("from", from);
        result.put("to", to);
        result.put("message", message);
        result.put("name", name);
    }

    public static void actionHiring(final Long id, final String status, final String remarks, final String document,
            final String requester, final String approverEmailList, ObjectNode result) {
        result.put("txnType", "sellExpenseTxn");
        result.put("id", id);
        result.put("status", status);
        result.put("workflowAction", "actionHiring");
        result.put("remarks", remarks);
        result.put("document", document);
        result.put("requester", requester);
        result.put("approverEmailList", approverEmailList);
    }
}
