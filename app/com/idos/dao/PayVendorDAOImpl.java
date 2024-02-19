package com.idos.dao;

import actor.CreatorActor;
import com.idos.util.*;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import play.mvc.WebSocket;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.*;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;
import java.util.logging.Level;
import pojo.TransactionViewResponse;

/**
 * @author Sunil K. Namdev created on 06.11.2019
 */
public class PayVendorDAOImpl implements PayVendorDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public Transaction submitForApproval(Users user, JsonNode json, EntityManager em, TransactionPurpose txnPurposeObj,
            ObjectNode result) throws IDOSException {
        Transaction txn = null;
        try {
            String txnforbranch = json.findValue("txnForBranch").asText();
            String creditMCPFCVVendor = json.findValue("creditMCPFCVVendor").asText();// credit vendor
            String txnMCPFCVoutstandings = json.findValue("txnMCPFCVoutstandings").asText();// net amount description
                                                                                            // which is outstandings
                                                                                            // here
            String txnMCPFCVInvoice = json.findValue("txnMCPFCVInvoice").asText();// transaction for which payment to
                                                                                  // vendor
            Integer vendorAdvanceType = json.findValue("vendorAdvanceType").asInt();// advance if any
            String txnMCPFCVVendorAdvanceIfAny = json.findValue("txnMCPFCVVendorAdvanceIfAny").asText();// advance if
                                                                                                        // any
            String txnMCPFCVVendorAdvanceAdjustment = json.findValue("txnMCPFCVVendorAdvanceAdjustment").asText();
            double txnTotalPaymentReceived = json.findValue("txnMCPFCVpaymentReceived").asDouble();
            double txnTotalDiscountReceived = json.findValue("txnMCPFCVTotalDiscountReceived") != null
                    ? json.findValue("txnMCPFCVTotalDiscountReceived").asDouble()
                    : 0.0;
            String txnMCPFCVpaymentDue = json.findValue("txnMCPFCVpaymentDue").asText();
            String txnactPayToVend = json.findValue("txnactPayToVend").asText();
            String supportingdoc = json.findValue("supportingdoc").asText();
            String txnremarks = json.findValue("txnremarks").asText();
            Long openingBalBillId = (json.findValue("openingBalBillId") == null
                    || "".equals(json.findValue("openingBalBillId").asText())) ? null
                            : json.findValue("openingBalBillId").asLong();
            String klfollowednotfollowed = json.findValue("klfollowednotfollowed").asText();
            String txndocumentUploadRequired = json.findValue("txndocumentUploadRequired") != null
                    ? json.findValue("txndocumentUploadRequired").asText()
                    : null;
            String txnForItemStr = json.findValue("txnForItem").toString();
            String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
            Date txnDate = IdosUtil.getFormatedDateWithTime(selectedTxnDate);

            Vendor txnVendor = Vendor.findById(IdosUtil.convertStringToLong(creditMCPFCVVendor));
            String customerVendorName = "";
            String txnRemarks = "";
            String branchName = "";
            String paymentMode = "";
            String projectName = "";
            String itemName = "";
            String debitCredit = "Credit";
            String approverEmails = "";
            String additionalApprovarUsers = "";
            String selectedAdditionalApproval = "";

            JSONArray arrJSON = new JSONArray(txnForItemStr);
            Branch transactionBranch = null;
            if (txnforbranch != null && !txnforbranch.equals("")) {
                transactionBranch = genericDao.getById(Branch.class, Long.parseLong(txnforbranch), em);
                branchName = transactionBranch.getName();
            }
            Map<String, Object> criterias = new HashMap<String, Object>(3);
            txn = new Transaction();
            txn.setGrossAmount(txnTotalPaymentReceived);
            if (txnMCPFCVInvoice.equals("-1")) {
                if (openingBalBillId != null) {
                    VendorBillwiseOpBalance OpeningBalanceBillTrans = VendorBillwiseOpBalance
                            .findById(openingBalBillId);
                    txn.setPaidInvoiceRefNumber(OpeningBalanceBillTrans.getId().toString());
                    txn.setLinkedTxnRef(OpeningBalanceBillTrans.getId().toString());
                    txn.setTypeIdentifier(IdosConstants.TXN_TYPE_OPENING_BALANCE_BILLWISE_VEND);
                } else {
                    BranchVendors branchHasVendor = BranchVendors.findByVendorBranch(entityManager,
                            user.getOrganization().getId(), transactionBranch.getId(), txnVendor.getId());
                    txn.setPaidInvoiceRefNumber(branchHasVendor.getId().toString()); // For Vendor Opening balance
                                                                                     // transaction
                    txn.setLinkedTxnRef(branchHasVendor.getId().toString());
                    txn.setTypeIdentifier(IdosConstants.TXN_TYPE_OPENING_BALANCE_VEND);
                }
                // list of additional users all approver role users of thet organization

                criterias.put("role.name", "APPROVER");
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("presentStatus", 1);
                List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);
                Boolean approver = null;
                for (UsersRoles usrRoles : approverRole) {
                    approver = false;
                    additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
                    criterias.clear();
                    criterias.put("user.id", usrRoles.getUser().getId());
                    criterias.put("userRights.id", 2L);
                    criterias.put("branch.id", transactionBranch.getId());
                    criterias.put("presentStatus", 1);
                    UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
                            criterias, em);
                    if (userHasRightInBranch != null) {
                        approver = true;
                    } else {
                        approver = false;
                    }
                    if (approver) {
                        approverEmails += usrRoles.getUser().getEmail() + ",";
                    }
                }
            } else {
                // Transaction pendingTransaction =
                // Transaction.findById(Long.parseLong(txnMCPFCVInvoice));
                // transactionBranch = pendingTransaction.getTransactionBranch();
                txn.setTypeIdentifier(IdosConstants.TXN_TYPE_OTHER_TRANSACTIONS_VEND);
                /*
                 * if (pendingTransaction.getTransactionProject() != null) {
                 * projectName = pendingTransaction.getTransactionProject().getName();
                 * txn.setTransactionProject(pendingTransaction.getTransactionProject());
                 * }
                 * itemName = pendingTransaction.getTransactionSpecifics().getName();
                 * txn.setTransactionSpecifics(pendingTransaction.getTransactionSpecifics());
                 * txn.setBudgetAvailDuringTxn(pendingTransaction.getBudgetAvailDuringTxn());
                 * txn.setActualAllocatedBudget(pendingTransaction.getActualAllocatedBudget());
                 * txn.setUserTxnLimitDesc(pendingTransaction.getUserTxnLimitDesc());
                 * txn.setTransactionParticulars(pendingTransaction.getTransactionSpecifics().
                 * getParticularsId());
                 * txn.setNoOfUnits(pendingTransaction.getNoOfUnits());
                 * txn.setPricePerUnit(pendingTransaction.getPricePerUnit());
                 * txn.setGrossAmount(pendingTransaction.getGrossAmount());
                 * txn.setPaidInvoiceRefNumber(pendingTransaction.getTransactionRefNumber());
                 */
                // list of additional users all approver role users of thet organization
                criterias.clear();
                criterias.put("role.name", "APPROVER");
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("presentStatus", 1);
                List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);
                for (UsersRoles usrRoles : approverRole) {
                    additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
                    criterias.clear();
                    criterias.put("user.id", usrRoles.getUser().getId());
                    criterias.put("userRights.id", 2L);
                    criterias.put("branch.id", transactionBranch.getId());
                    criterias.put("presentStatus", 1);
                    UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
                            criterias, em);
                    if (userHasRightInBranch != null) {
                        for (int i = 0; i < arrJSON.length(); i++) {
                            JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                            Transaction pendingTransaction = Transaction.findById(rowItemData.getLong("pendingTxn"));
                            // check for right in chart of accounts
                            criterias.clear();
                            criterias.put("user.id", usrRoles.getUser().getId());
                            criterias.put("userRights.id", 2L);
                            criterias.put("specifics.id", pendingTransaction.getTransactionSpecifics().getId());
                            criterias.put("presentStatus", 1);
                            UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class,
                                    criterias, em);
                            if (userHasRightInCOA != null) {
                                boolean userAmtLimit = false;
                                if (userHasRightInCOA.getAmount() != null) {
                                    if (userHasRightInCOA.getAmount() > 0) {
                                        if (txnTotalPaymentReceived > userHasRightInCOA.getAmount()) {
                                            userAmtLimit = false;
                                        }
                                        if (txnTotalPaymentReceived < userHasRightInCOA.getAmount()) {
                                            userAmtLimit = true;
                                        }
                                    }
                                }
                                if (userHasRightInCOA.getAmountTo() != null) {
                                    if (userHasRightInCOA.getAmountTo() > 0) {
                                        if (txnTotalPaymentReceived > userHasRightInCOA.getAmountTo()) {
                                            userAmtLimit = false;
                                        }
                                        if (txnTotalPaymentReceived < userHasRightInCOA.getAmountTo()) {
                                            userAmtLimit = true;
                                        }
                                    }
                                }
                                if (userAmtLimit == true) {
                                    if (!approverEmails.contains(usrRoles.getUser().getEmail()))
                                        approverEmails += usrRoles.getUser().getEmail() + ",";
                                }
                            }
                        }
                    }
                }
            }
            // common fields for OB vendor (account payables opening balance) and vendor
            // invoice adjustment
            txn.setTransactionBranch(transactionBranch);
            customerVendorName = txnVendor.getName();
            if (klfollowednotfollowed != null && !klfollowednotfollowed.equals("")) {
                txn.setKlFollowStatus(IdosUtil.convertStringToInt(klfollowednotfollowed));
            }
            if (txndocumentUploadRequired != null) {
                if (txndocumentUploadRequired == "true") {
                    txn.setDocRuleStatus(1);
                }
            }
            txn.setTransactionVendorCustomer(txnVendor);
            txn.setTransactionPurpose(txnPurposeObj);
            txn.setTransactionBranchOrganization(transactionBranch.getOrganization());
            Double advanceIfAnyForAdjustment = null;
            Double advanceAdjustment = null;
            if (!txnMCPFCVVendorAdvanceIfAny.equals("") && !txnMCPFCVVendorAdvanceAdjustment.equals("")) {
                advanceIfAnyForAdjustment = IdosUtil.convertStringToDouble(txnMCPFCVVendorAdvanceIfAny);
                advanceAdjustment = IdosUtil.convertStringToDouble(txnMCPFCVVendorAdvanceAdjustment);
            } else {
                advanceIfAnyForAdjustment = 0.0;
                advanceAdjustment = 0.0;
            }
            txn.setAdvanceType(vendorAdvanceType);
            txn.setAvailableAdvance(advanceIfAnyForAdjustment);
            txn.setAdjustmentFromAdvance(advanceAdjustment);
            if (DateUtil.isBackDate(txnDate)) {
                txn.setIsBackdatedTransaction(IdosConstants.BACK_DATED_TXN);
            }
            txn.setNetAmount(txnTotalPaymentReceived);
            txn.setAvailableDiscountAmountForTxn(txnTotalDiscountReceived);
            String netDesc = "Payment Made:" + txnTotalPaymentReceived + ",Advance Adjustment:" + advanceAdjustment
                    + ",Due Balance:" + txnMCPFCVpaymentDue;
            txn.setNetAmountResultDescription(netDesc);
            if (txnactPayToVend != null && !txnactPayToVend.equals("")) {
                txn.setNetAmountResultDescription(txn.getNetAmountResultDescription() + "," + txnactPayToVend);
                String txnActPayVend[] = txnactPayToVend.split(",");
                if (txnActPayVend.length > 1) {
                    String txnActPayVendwithcolon[] = txnActPayVend[1].split(":");
                    txn.setWithholdingTax(IdosUtil.convertStringToDouble(txnActPayVendwithcolon[1]));
                }
            }
            txn.setTransactionDate(txnDate);
            int txnReceiptDetails = json.findValue("txnReceiptDetails") != null
                    ? json.findValue("txnReceiptDetails").asInt()
                    : 0;
            String txnReceiptDescription = json.findValue("txnReceiptDescription").asText();
            Long txnReceiptPaymentBank = json.findValue("txnReceiptPaymentBank") != null
                    ? json.findValue("txnReceiptPaymentBank").asLong()
                    : null;
            String txnInstrumentNum = json.findValue("txnInstrumentNum") != null
                    ? json.findValue("txnInstrumentNum").asText()
                    : "";
            String txnInstrumentDate = txnInstrumentDate = json.findValue("txnInstrumentDate") != null
                    ? json.findValue("txnInstrumentDate").asText()
                    : "";
            if (IdosConstants.PAYMODE_CASH == txnReceiptDetails) {
                paymentMode = "CASH";
            } else if (IdosConstants.PAYMODE_BANK == txnReceiptDetails) {
                paymentMode = "BANK";
            }
            BUY_TRANSACTION_SERVICE.setTxnPaymentDetail(user, em, txn, txnReceiptDetails, txnReceiptPaymentBank,
                    txnInstrumentNum, txnInstrumentDate, result);
            txn.setReceiptDetailsType(txnReceiptDetails);
            if (txnReceiptDescription != null && !"".equals(txnReceiptDescription)) {
                txn.setReceiptDetailsDescription(txnReceiptDescription);
            }

            if (!txnremarks.equals("") && txnremarks != null) {
                txnRemarks = user.getEmail() + "#" + txnremarks;
                txn.setRemarks(txnRemarks);
                txnRemarks = txn.getRemarks();
            }
            txn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, em));
            txn.setTransactionStatus("Require Approval");

            txn.setApproverEmails(approverEmails);
            txn.setAdditionalApproverEmails(additionalApprovarUsers);
            // list of approver user
            String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
            txn.setTransactionRefNumber(transactionNumber);
            txn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, em));
            genericDao.saveOrUpdate(txn, user, em);
            FILE_UPLOAD_SERVICE.updateUploadFileLogs(em, user, supportingdoc, txn.getId(), IdosConstants.MAIN_TXN_TYPE);
            for (int i = 0; i < arrJSON.length(); i++) {
                JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                if (rowItemData.getString("pendingTxn") != null && !rowItemData.getString("pendingTxn").equals("")) {
                    if (!rowItemData.getString("pendingTxn").equals("-1")) {
                        Transaction pendingTransaction = Transaction.findById(rowItemData.getLong("pendingTxn"));
                        TransactionItems saveSelectedTxns = new TransactionItems();
                        saveSelectedTxns.setOrganization(user.getOrganization());
                        saveSelectedTxns.setTransactionId(txn);
                        saveSelectedTxns.setTransactionRefNumber(pendingTransaction.getTransactionRefNumber());
                        if (rowItemData.getString("amountPaid") != null
                                && !rowItemData.getString("amountPaid").equals(""))
                            saveSelectedTxns.setNetAmount(rowItemData.getDouble("amountPaid"));
                        if (rowItemData.getString("discReceived") != null
                                && !rowItemData.getString("discReceived").equals(""))
                            saveSelectedTxns.setDiscountAmount(rowItemData.getDouble("discReceived"));
                        saveSelectedTxns.setBranch(transactionBranch);
                        genericDao.saveOrUpdate(saveSelectedTxns, user, em);
                    } else {
                        Transaction pendingTransaction = Transaction.findById(rowItemData.getLong("pendingTxn"));
                        TransactionItems saveSelectedTxns = new TransactionItems();
                        saveSelectedTxns.setOrganization(user.getOrganization());
                        saveSelectedTxns.setTransactionId(txn);
                        saveSelectedTxns.setTransactionRefNumber("-1");
                        if (rowItemData.getString("amountPaid") != null
                                && !rowItemData.getString("amountPaid").equals(""))
                            saveSelectedTxns.setNetAmount(rowItemData.getDouble("amountPaid"));
                        if (rowItemData.getString("discReceived") != null
                                && !rowItemData.getString("discReceived").equals(""))
                            saveSelectedTxns.setDiscountAmount(rowItemData.getDouble("discReceived"));

                        saveSelectedTxns.setGrossAmount(txnVendor.getTotalOpeningBalance());
                        saveSelectedTxns.setTransactionRefNumber("-1"); // For Vendor Opening balance transaction
                        saveSelectedTxns.setBranch(transactionBranch);
                        genericDao.saveOrUpdate(saveSelectedTxns, user, em);
                        // saveSelectedTxns.setTypeIdentifier(IdosConstants.TXN_TYPE_OPENING_BALANCE_VEND);

                        // list of additional users all approver role users of thet organization
                        criterias.clear();
                        criterias.put("role.name", "APPROVER");
                        criterias.put("organization.id", user.getOrganization().getId());
                        criterias.put("presentStatus", 1);
                        List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);

                        Boolean approver = null;
                        for (UsersRoles usrRoles : approverRole) {
                            approver = false;
                            additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
                            criterias.clear();
                            criterias.put("user.id", usrRoles.getUser().getId());
                            criterias.put("userRights.id", 2L);
                            criterias.put("branch.id", transactionBranch.getId());
                            criterias.put("presentStatus", 1);

                            UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
                                    criterias, em);
                            if (userHasRightInBranch != null) {

                                approver = true;
                            } else {
                                approver = false;
                            }
                            if (approver) {
                                approverEmails += usrRoles.getUser().getEmail() + ",";
                            }

                        }
                    }
                }
            }

            if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
                // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
                // for (int i = 0; i < keyArray.length; i++) {
                // StringBuilder sbquery = new StringBuilder("");
                // sbquery.append(
                // "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
                // obj.presentStatus=1");
                // List<Users> orgusers = genericDao.executeSimpleQuery(sbquery.toString(), em);
                // if (!orgusers.isEmpty()
                // && orgusers.get(0).getOrganization().getId() ==
                // user.getOrganization().getId()) {
                // orgtxnregistereduser.put(keyArray[i].toString(),
                // CreatorActor.expenseregistrered.get(keyArray[i]));
                // }
                // }
                String invoiceDate = "";
                String invoiceDateLabel = "";
                if (txn.getTransactionInvoiceDate() != null) {
                    invoiceDateLabel = "INVOICE DATE:";
                    invoiceDate = IdosConstants.IDOSDF.format(txn.getTransactionInvoiceDate());
                }
                String itemParentName = "";
                if (txn.getTransactionSpecifics() != null && txn.getTransactionSpecifics().getParentSpecifics() != null
                        && !txn.getTransactionSpecifics().getParentSpecifics().equals("")) {
                    itemParentName = txn.getTransactionSpecifics().getParentSpecifics().getName();
                } else {
                    if (txn.getTransactionSpecifics() != null
                            && txn.getTransactionSpecifics().getParticularsId() != null) {
                        itemParentName = txn.getTransactionSpecifics().getParticularsId().getName();
                    }
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
                if (txn.getDocRuleStatus() != null && txn.getTransactionExceedingBudget() != null) {
                    if (txn.getDocRuleStatus() == 1 && txn.getTransactionExceedingBudget() == 1) {
                        txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
                    }
                    if (txn.getKlFollowStatus() == 1 && txn.getTransactionExceedingBudget() == 0) {
                        txnSpecialStatus = "Rules Not Followed";
                    }
                }
                if (txn.getDocRuleStatus() != null && txn.getTransactionExceedingBudget() == null) {
                    txnSpecialStatus = "Rules Not Followed";
                }
                String[] actbudgetForBranchExpenseItemArr = { "0", "0" };
                if (txn.getActualAllocatedBudget() != null) {
                    actbudgetForBranchExpenseItemArr = txn.getActualAllocatedBudget().split(":");
                }
                String[] budgetForBranchExpenseItemArr = { "0", "0" };
                if (txn.getBudgetAvailDuringTxn() != null) {
                    budgetForBranchExpenseItemArr = txn.getBudgetAvailDuringTxn().split(":");
                }
                String transactionPur = txn.getTransactionPurpose().getTransactionPurpose() != null
                        ? txn.getTransactionPurpose().getTransactionPurpose()
                        : "";
                long transactionPurId = txn.getTransactionPurpose().getId() != null
                        ? txn.getTransactionPurpose().getId()
                        : 0l;
                String transDate = IdosConstants.IDOSDF.format(txn.getTransactionDate());

                String creatorEmail = txn.getCreatedBy() == null ? user.getEmail() : txn.getCreatedBy().getEmail();
                String invoiceNumber = txn.getInvoiceNumber() == null ? "" : txn.getInvoiceNumber();
                Integer typeOfSupply = txn.getTypeOfSupply() == null ? 0 : txn.getTypeOfSupply();
                String txnDocument = txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs();
                TransactionViewResponse.addActionTxn(txn.getId(), branchName, projectName,
                        itemName,
                        itemParentName,
                        actbudgetForBranchExpenseItemArr[0], actbudgetForBranchExpenseItemArr[1],
                        budgetForBranchExpenseItemArr[0], budgetForBranchExpenseItemArr[1],
                        customerVendorName,
                        transactionPur, transDate, invoiceDateLabel, invoiceDate, paymentMode,
                        txn.getNoOfUnits(),
                        txn.getPricePerUnit(), txn.getGrossAmount(), txn.getNetAmount(),
                        txnResultDesc, "",
                        txn.getTransactionStatus(), creatorEmail, approverLabel, approverEmail,
                        txnDocument, txnRemarks,
                        debitCredit, approverEmails, additionalApprovarUsers,
                        selectedAdditionalApproval,
                        txnSpecialStatus, 0.0, "", txnInstrumentNum,
                        txnInstrumentDate,
                        transactionPurId, "", invoiceNumber, 0, txn.getTransactionRefNumber(), 0l,
                        0.0, 0,
                        typeOfSupply, result);
                // transactionService.sendStockWebSocketResponse(entityManager, txn, user);
            }
            // transactionService.sendStockWebSocketResponse(transaction, user); later use
            // this in
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Pay to Vendor- submit for approval.", ex.getMessage());
        }
        return txn;
    }
}
