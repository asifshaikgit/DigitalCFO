package service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.*;
import model.*;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import akka.stream.javadsl.*;
import akka.actor.*;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;
import actor.CreatorActor;
import controllers.Karvy.KarvyAuthorization;
import akka.NotUsed;

/**
 * Created by Sunil Namdev on 10-12-2016.
 */
public class SellTransactionServiceImpl implements SellTransactionService {
    @Override
    public Transaction submitForApproval(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
        Long txnforbranch = json.findValue("txnforbranch").asLong();
        Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                : json.findValue("txnEntityID").asLong();
        long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
        Long txnforcustomer = json.findValue("txnforcustomer").asLong();
        String txnforproject = json.findValue("txnforproject").asText();
        Boolean performaInvoice = json.findValue("performaInvoice").asBoolean();
        String txnforunavailablecustomer = json.findValue("txnforunavailablecustomer").asText();
        String txnPoReference = json.findValue("txnPoReference").asText();
        String txncustomerdiscountavailable = json.findValue("txncustomerdiscountavailable").asText();
        Double txnnetamount = json.findValue("txnnetamount") == null ? 0.0 : json.findValue("txnnetamount").asDouble();
        // Double netAmountTotalWithDecimalValue =
        // json.findValue("netAmountTotalWithDecimalValue").asDouble();
        String txnremarks = json.findValue("txnremarks").asText();
        String supportingdoc = json.findValue("supportingdoc").asText();
        String txnForItemStr = json.findValue("txnforitem").toString();
        String txnSourceGstin = json.findValue("txnSourceGstin").asText();
        String txnDestinGstin = json.findValue("txnDestinGstin").asText();
        int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0 : json.findValue("txnTypeOfSupply").asInt();
        int txnWalkinCustomerType = json.findValue("txnWalkinCustomerType") == null ? 0
                : json.findValue("txnWalkinCustomerType").asInt();
        String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
        Integer txnWithWithoutTax = json.findValue("txnWithWithoutTax") == null ? null
                : json.findValue("txnWithWithoutTax").asInt();
        String docRefNo = json.findValue("txnDocRefNo") == null ? "" : json.findValue("txnDocRefNo").asText();
        Date txnDate = IdosUtil.getFormatedDate(selectedTxnDate);
        String txnRemarks = "";
        Branch txnBranch = null;
        String branchName = "";
        Vendor txncustomer = null;
        String paymentMode = null;
        Project txnProject = null;
        String projectName = "";
        String itemName = "";
        String customerVendorName = "";
        String debitCredit = "Credit";
        Transaction txn = null;
        // it holds all info for txn
        try {
            TransactionPurpose transactionPurpose = TransactionPurpose.findById(txnPurposeVal);
            if (txnEntityID > 0) {
                txn = Transaction.findById(txnEntityID);
            } else {
                txn = new Transaction();
            }

            JSONArray arrJSON = new JSONArray(txnForItemStr);
            if (txnforbranch != null && !txnforbranch.equals("")) {
                txnBranch = genericDAO.getById(Branch.class, txnforbranch, entityManager);
                branchName = txnBranch.getName();
            }
            if (txnforproject != null && !txnforproject.equals("")) {
                txnProject = genericDAO.getById(Project.class, IdosUtil.convertStringToLong(txnforproject),
                        entityManager);
                projectName = txnProject.getName();
            }
            txn.setPaymentStatus("NOT-PAID");
            if (txnforcustomer != null && !txnforcustomer.equals("")) {
                txncustomer = genericDAO.getById(Vendor.class, txnforcustomer, entityManager);
                if (txncustomer != null)
                    customerVendorName = txncustomer.getName();
                else
                    customerVendorName = txnforunavailablecustomer;
            }
            // Enter data for first item in transaction table to be displayed in Transaction
            // list
            JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
            Long itemIdRow0 = firstRowItemData.getLong("txnItems");
            Specifics txnSpecificItem = genericDAO.getById(Specifics.class, itemIdRow0, entityManager);
            Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
            Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
            Double txnGrossRow0 = firstRowItemData.getDouble("txnGross");

            txn.setTransactionSpecifics(txnSpecificItem);
            txn.setTransactionParticulars(txnSpecificItem.getParticularsId());
            txn.setNoOfUnits(txnNoOfUniRow0t);
            txn.setPricePerUnit(txnPerUnitPriceRow0);
            txn.setGrossAmount(txnGrossRow0);
            txn.setSourceGstin(txnSourceGstin);
            txn.setDestinationGstin(txnDestinGstin);
            txn.setTypeOfSupply(txnTypeOfSupply);
            txn.setWalkinCustomerType(txnWalkinCustomerType);
            txn.setTransactionPurpose(transactionPurpose);
            txn.setTransactionBranch(txnBranch);
            txn.setTransactionBranchOrganization(txnBranch.getOrganization());
            txn.setTransactionProject(txnProject);
            txn.setTransactionVendorCustomer(txncustomer);
            txn.setTransactionUnavailableVendorCustomer(txnforunavailablecustomer);
            txn.setWithWithoutTax(txnWithWithoutTax);
            if (!txnforunavailablecustomer.equals("") && txnforunavailablecustomer != null) {
                customerVendorName = txnforunavailablecustomer;
            }
            txn.setPoReference(txnPoReference);
            txn.setNetAmount(txnnetamount);

            // Double roundedCutPartOfNetAmount = txnnetamount -
            // netAmountTotalWithDecimalValue;
            // txn.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
            if (DateUtil.isBackDate(txnDate)) {
                txn.setIsBackdatedTransaction(IdosConstants.BACK_DATED_TXN);
            }
            txn.setTransactionDate(txnDate);
            String txnInstrumentNumber = null;
            String txnInstrumentDate = null;
            if (txnPurposeVal == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW) {
                int txnPaymentMode = json.findValue("txnReceiptDetails") == null ? 0
                        : json.findValue("txnReceiptDetails").asInt();
                if (IdosConstants.PAYMODE_BANK == txnPaymentMode) {
                    long txnReceiptPaymentBank = json.findValue("txnReceiptPaymentBank") != null
                            ? json.findValue("txnReceiptPaymentBank").asLong()
                            : 0L;
                    txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
                            ? json.findValue("txnInstrumentNum").asText()
                            : "";
                    txnInstrumentDate = json.findValue("txnInstrumentDate") != null
                            ? json.findValue("txnInstrumentDate").asText()
                            : "";
                    BranchBankAccounts bankAccount = BranchBankAccounts.findById(txnReceiptPaymentBank);
                    if (bankAccount == null) {
                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                                IdosConstants.INVALID_DATA_EXCEPTION,
                                "Bank is not selected in transaction when payment mode is Bank.");
                    }
                    txn.setTransactionBranchBankAccount(bankAccount);
                    if (txnInstrumentNumber != null && !"".equals(txnInstrumentNumber)) {
                        txn.setInstrumentNumber(txnInstrumentNumber);
                    }
                    if (txnInstrumentDate != null && !"".equals(txnInstrumentDate)) {
                        txn.setInstrumentDate(txnInstrumentDate);
                    }
                    paymentMode = IdosConstants.PAYMENT_MODE_BANK;
                } else {
                    txnInstrumentNumber = "";
                    txnInstrumentDate = "";
                    paymentMode = IdosConstants.PAYMENT_MODE_CASH;
                }
                txn.setReceiptDetailsType(txnPaymentMode);
                String txnReceiptDescription = json.findValue("txnReceiptDescription") != null
                        ? json.findValue("txnReceiptDescription").asText()
                        : null;
                txn.setReceiptDetailsDescription(txnReceiptDescription);
            } else {
                txnInstrumentNumber = "";
                txnInstrumentDate = "";
                paymentMode = "";
            }
            if (txnremarks != null && !txnremarks.equals("")) {
                if (txn.getRemarks() != null) {
                    txn.setRemarks(txnRemarks);
                } else {
                    txnRemarks = user.getEmail() + "#" + txnremarks;
                    txn.setRemarks(txnRemarks);
                }
                txnRemarks = txn.getRemarks(); // fetch encoded value
            }
            txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, entityManager));
            txn.setTransactionStatus("Require Approval");

            // list of additional users all approver role users of thet organization
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("role.name", "APPROVER");
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<UsersRoles> approverRole = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);
            String approverEmails = "";
            String additionalApprovarUsers = "";
            String selectedAdditionalApproval = "";
            Boolean approver = null;
            for (UsersRoles usrRoles : approverRole) {
                approver = false;
                additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
                criterias.clear();
                criterias.put("user.id", usrRoles.getUser().getId());
                criterias.put("userRights.id", 2L);
                criterias.put("branch.id", txnBranch.getId());
                criterias.put("presentStatus", 1);

                UserRightInBranch userHasRightInBranch = genericDAO.getByCriteria(UserRightInBranch.class, criterias,
                        entityManager);
                if (userHasRightInBranch != null) {
                    for (int i = 0; i < arrJSON.length(); i++) {
                        // Double howMuchAdvance=0.0;Double txnTaxAmount=0.0;Double
                        // customerAdvance=0.0;String txnTaxDesc="";Double withholdingAmount=0.0;
                        JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                        // TransactionItems transactionItem = new TransactionItems();
                        Long itemId = rowItemData.getLong("txnItems");
                        Specifics txnItem = genericDAO.getById(Specifics.class, itemId, entityManager);
                        criterias.clear();
                        criterias.put("user.id", usrRoles.getUser().getId());
                        criterias.put("userRights.id", 2L);
                        criterias.put("specifics.id", txnItem.getId());
                        criterias.put("presentStatus", 1);
                        UserRightSpecifics userHasRightInCOA = genericDAO.getByCriteria(UserRightSpecifics.class,
                                criterias, entityManager);
                        if (userHasRightInCOA != null) {
                            approver = true;
                        } else {
                            approver = false;
                        }
                    }
                    if (approver) {
                        approverEmails += usrRoles.getUser().getEmail() + ",";
                    }
                }
            }
            txn.setApproverEmails(approverEmails);
            txn.setAdditionalApproverEmails(additionalApprovarUsers);
            String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
            txn.setTransactionRefNumber(transactionNumber);
            txn.setInvoiceNumber(docRefNo);
            genericDAO.saveOrUpdate(txn, user, entityManager);
            FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, supportingdoc, txn.getId(),
                    IdosConstants.MAIN_TXN_TYPE);
            // Enter multiple items data into TransactionItems table
            if (txnEntityID > 0) {
                TRANSACTION_ITEMS_SERVICE.updateMultipleItemsTransactionItems(entityManager, user, arrJSON, txn);
            } else {
                TRANSACTION_ITEMS_SERVICE.insertMultipleItemsTransactionItems(entityManager, user, arrJSON, txn,
                        txnDate);
            }
            // if trading inventory then need this for INVENTORY_REPORT
            /*
             * inventory should change on complete accounting
             * if (txn.getTransactionSpecifics() != null &&
             * txn.getTransactionSpecifics().getIsTradingInvenotryItem() != null &&
             * txn.getTransactionSpecifics().getIsTradingInvenotryItem() == 1) {
             * StockWarehouseController.insertTradingInventory(txn, user, entityManager);
             * }
             */
            if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
                // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
                // for (int i = 0; i < keyArray.length; i++) {
                // StringBuilder sbquery = new StringBuilder(
                // "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
                // obj.presentStatus=1");
                // List<Users> orgusers = genericDAO.executeSimpleQuery(sbquery.toString(),
                // entityManager);
                // if (!orgusers.isEmpty()
                // && orgusers.get(0).getOrganization().getId() ==
                // user.getOrganization().getId()) {
                // orgtxnregistereduser.put(keyArray[i].toString(),
                // CreatorActor.expenseregistrered.get(keyArray[i]));
                // }
                // }
                entitytransaction.commit();
                String itemParentName = "";
                if (txnSpecificItem.getParentSpecifics() != null && !txnSpecificItem.getParentSpecifics().equals("")) {
                    itemParentName = txnSpecificItem.getParentSpecifics().getName();
                } else {
                    itemParentName = txnSpecificItem.getParticularsId().getName();
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
                String txnDocument = txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs();
                TransactionViewResponse.addActionTxn(txn.getId(), branchName, projectName, itemName, itemParentName, "",
                        "", "",
                        "", customerVendorName, txn.getTransactionPurpose().getTransactionPurpose(),
                        IdosConstants.idosdf.format(txn.getTransactionDate()), "", "", paymentMode, txn.getNoOfUnits(),
                        txn.getPricePerUnit(), txn.getGrossAmount(), txn.getNetAmount(), txnResultDesc, "",
                        txn.getTransactionStatus(), txn.getCreatedBy().getEmail(), approverLabel, approverEmail,
                        txnDocument, txnRemarks, "", approverEmails, additionalApprovarUsers,
                        selectedAdditionalApproval, txnSpecialStatus, 0d, txnPoReference,
                        txnInstrumentNumber, txnInstrumentDate, txn.getTransactionPurpose().getId(), "", "", 0,
                        txn.getTransactionRefNumber(), 0l, 0.0, 0, typeOfSupply, result);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Sell on Cash- submit for approval", ex.getMessage());
        }
        return txn;
    }

    @Override
    public Transaction submitForApprovalSalesReturns(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
        String txnRemarks = "";
        Branch txnBranch = null;
        String branchName = "";
        Vendor txncustomer = null;
        String paymentMode = "";
        Project txnProject = null;
        String projectName = "";
        String itemName = "";
        String customerVendorName = "";
        String debitCredit = "Credit";
        Transaction txn = null;

        try {
            Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                    : json.findValue("txnEntityID").asLong();
            Long transactionInvoiceId = (json.findValue("transactionInvoiceId") == null
                    || "".equals(json.findValue("transactionInvoiceId"))) ? 0l
                            : json.findValue("transactionInvoiceId").asLong();
            Transaction invoiceTransaction = Transaction.findById(transactionInvoiceId); // get original sell on credit
                                                                                         // tran
            Long txnforbranch = json.findValue("txnforbranch").asLong();
            long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
            Long txnforcustomer = json.findValue("txnforcustomer").asLong();
            String txnforproject = json.findValue("txnforproject").asText();
            Double txnnetamount = json.findValue("txnnetamount") == null ? 0.0
                    : json.findValue("txnnetamount").asDouble();
            // Double netAmountTotalWithDecimalValue =
            // json.findValue("netAmountTotalWithDecimalValue").asDouble();
            String txnremarks = (json.findValue("txnremarks") == null || "".equals(json.findValue("txnremarks"))) ? null
                    : json.findValue("txnremarks").asText();
            String supportingdoc = (json.findValue("supportingdoc") == null
                    || "".equals(json.findValue("supportingdoc"))) ? null : json.findValue("supportingdoc").asText();
            String txnForItemStr = json.findValue("txnforitem").toString();

            String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
            Date txnDate = null;
            try {
                if (selectedTxnDate != null) {
                    txnDate = IdosConstants.IDOSDF.parse(selectedTxnDate);
                } else {
                    txnDate = new Date();
                }
            } catch (ParseException e) {
                throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
                        IdosConstants.NULL_KEY_EXC_ESMF_MSG,
                        "cannot parse date: " + selectedTxnDate + " " + e.getMessage());
            }

            // Sales Return transaction entry into Transaction table

            if (txnEntityID > 0) {
                txn = Transaction.findById(txnEntityID);
            } else {
                txn = new Transaction();
            }
            TransactionPurpose transactionPurpose = TransactionPurpose.findById(txnPurposeVal);
            if (txnforbranch != null && !txnforbranch.equals("")) {
                txnBranch = genericDAO.getById(Branch.class, txnforbranch, entityManager);
                branchName = txnBranch.getName();
            }
            if (txnforproject != null && !txnforproject.equals("")) {
                txnProject = genericDAO.getById(Project.class, IdosUtil.convertStringToLong(txnforproject),
                        entityManager);
                projectName = txnProject.getName();
            }
            if (txnforcustomer != null && !txnforcustomer.equals("")) {
                txncustomer = genericDAO.getById(Vendor.class, txnforcustomer, entityManager);
                if (txncustomer != null)
                    customerVendorName = txncustomer.getName();
            }
            // Enter data for first item in transaction table to be displayed in Transaction
            // list
            JSONArray arrJSON = new JSONArray(txnForItemStr);
            JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
            Long itemIdRow0 = firstRowItemData.getLong("txnItems");
            Specifics txnSpecificItem = genericDAO.getById(Specifics.class, itemIdRow0, entityManager);
            Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
            Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
            Double txnGrossRow0 = firstRowItemData.getDouble("txnGross");
            txn.setTransactionSpecifics(txnSpecificItem);
            txn.setTransactionParticulars(txnSpecificItem.getParticularsId());
            txn.setNoOfUnits(txnNoOfUniRow0t);
            txn.setPricePerUnit(txnPerUnitPriceRow0);
            txn.setGrossAmount(txnGrossRow0);
            txn.setTransactionPurpose(transactionPurpose);
            txn.setTransactionBranch(txnBranch);
            txn.setTransactionBranchOrganization(txnBranch.getOrganization());
            txn.setTransactionProject(txnProject);
            txn.setTransactionVendorCustomer(txncustomer);
            txn.setPaidInvoiceRefNumber(invoiceTransaction.getTransactionRefNumber());
            txn.setNetAmount(txnnetamount);
            // Double roundedCutPartOfNetAmount = txnnetamount -
            // netAmountTotalWithDecimalValue;
            // txn.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
            String netDesc = "Invoice Value:" + invoiceTransaction.getNetAmount() + ",Sales Return Value:"
                    + txnnetamount;
            txn.setNetAmountResultDescription(netDesc);
            txn.setTransactionDate(txnDate);
            if (DateUtil.isBackDate(txnDate)) {
                txn.setIsBackdatedTransaction(IdosConstants.BACK_DATED_TXN);
            }
            if (txnremarks != null && !txnremarks.equals("")) {
                txnRemarks = user.getEmail() + "#" + txnremarks;
                txn.setRemarks(txnRemarks);
                txnRemarks = txn.getRemarks();
            }
            txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, entityManager));
            txn.setTransactionStatus("Require Approval");
            // list of additional users all approver role users of thet organization
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.clear();
            criterias.put("role.name", "APPROVER");
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<UsersRoles> approverRole = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);
            String approverEmails = "";
            String additionalApprovarUsers = "";
            String selectedAdditionalApproval = "";
            for (UsersRoles usrRoles : approverRole) {
                additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
                criterias.clear();
                criterias.put("user.id", usrRoles.getUser().getId());
                criterias.put("userRights.id", 2L);
                criterias.put("branch.id", invoiceTransaction.getTransactionBranch().getId());
                criterias.put("presentStatus", 1);
                UserRightInBranch userHasRightInBranch = genericDAO.getByCriteria(UserRightInBranch.class, criterias,
                        entityManager);
                if (userHasRightInBranch != null) {
                    // check for right in chart of accounts
                    criterias.clear();
                    criterias.put("user.id", usrRoles.getUser().getId());
                    criterias.put("userRights.id", 2L);
                    criterias.put("specifics.id", invoiceTransaction.getTransactionSpecifics().getId());
                    criterias.put("presentStatus", 1);
                    UserRightSpecifics userHasRightInCOA = genericDAO.getByCriteria(UserRightSpecifics.class, criterias,
                            entityManager);
                    if (userHasRightInCOA != null) {
                        boolean userAmtLimit = false;
                        if (userHasRightInCOA.getAmount() != null) {
                            if (userHasRightInCOA.getAmount() > 0) {
                                if (txnnetamount > userHasRightInCOA.getAmount()) {
                                    userAmtLimit = false;
                                }
                                if (txnnetamount < userHasRightInCOA.getAmount()) {
                                    userAmtLimit = true;
                                }
                            }
                        }
                        if (userHasRightInCOA.getAmountTo() != null) {
                            if (userHasRightInCOA.getAmountTo() > 0) {
                                if (txnnetamount > userHasRightInCOA.getAmountTo()) {
                                    userAmtLimit = false;
                                }
                                if (txnnetamount < userHasRightInCOA.getAmountTo()) {
                                    userAmtLimit = true;
                                }
                            }
                        }
                        if (userAmtLimit == true) {
                            approverEmails += usrRoles.getUser().getEmail() + ",";
                        }
                    }
                }
            }
            txn.setApproverEmails(approverEmails);
            txn.setAdditionalApproverEmails(additionalApprovarUsers);
            // list of approver user
            String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
            txn.setTransactionRefNumber(transactionNumber);
            genericDAO.saveOrUpdate(txn, user, entityManager);
            FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, supportingdoc, txn.getId(),
                    IdosConstants.MAIN_TXN_TYPE);
            // Enter multiple items data into TransactionItems table
            // if (txnEntityID > 0) {
            TRANSACTION_ITEMS_SERVICE.insertMultipleItemsTransactionItems(entityManager, user, arrJSON, txn, txnDate);
            TRANSACTION_ITEMS_SERVICE.updateMultipleItemsSalesReturnTransactionItems(entityManager, user, arrJSON, txn);

            entitytransaction.commit();

            if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
                // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
                // for (int i = 0; i < keyArray.length; i++) {
                // StringBuilder sbquery = new StringBuilder("");
                // sbquery.append(
                // "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
                // obj.presentStatus=1");
                // List<Users> orgusers = genericDAO.executeSimpleQuery(sbquery.toString(),
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
                SimpleDateFormat idosdf = new SimpleDateFormat("MMM dd,yyyy");
                if (txn.getTransactionInvoiceDate() != null) {
                    invoiceDateLabel = "INVOICE DATE:";
                    invoiceDate = idosdf.format(txn.getTransactionInvoiceDate());
                }
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
                String txnInstrumentNumber = "";
                String txnInstrumentDate = "";
                Integer typeOfSupply = txn.getTypeOfSupply() == null ? 0 : txn.getTypeOfSupply();
                String txnDocument = txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs();
                TransactionViewResponse.addActionTxn(txn.getId(), branchName, projectName, itemName, itemParentName, "",
                        "", "",
                        "", customerVendorName, txn.getTransactionPurpose().getTransactionPurpose(),
                        idosdf.format(txn.getTransactionDate()), invoiceDateLabel, invoiceDate, paymentMode, 0.0, 0.0,
                        0.0, txn.getNetAmount(), txnResultDesc, "", txn.getTransactionStatus(),
                        txn.getCreatedBy().getEmail(), approverLabel, approverEmail, txnDocument, txnRemarks,
                        debitCredit, approverEmails, additionalApprovarUsers, selectedAdditionalApproval,
                        txnSpecialStatus, 0.0, "", txnInstrumentNumber, txnInstrumentDate,
                        txn.getTransactionPurpose().getId(), "", "", 0, txn.getTransactionRefNumber(), 0l, 0.0, 0,
                        typeOfSupply, result);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            ex.printStackTrace();
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Sell on Cash- submit for approval", ex.getMessage());
        }
        return txn;
    }

    @Override
    public Transaction submitForAccounting(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
        Transaction txn = null;
        try {
            Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                    : json.findValue("txnEntityID").asLong();
            long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
            String txnforbranch = json.findValue("txnforbranch").asText();
            String txnforproject = json.findValue("txnforproject").asText();
            String txnforcustomer = json.findValue("txnforcustomer").asText();
            Boolean performaInvoice = json.findValue("performaInvoice").asBoolean();
            String txnforunavailablecustomer = json.findValue("txnforunavailablecustomer").asText();
            String txnPoReference = json.findValue("txnPoReference").asText();
            String txnBomTxnRef = json.findValue("txnBomTxnRef") == null ? "" : json.findValue("txnBomTxnRef").asText();
            String txncustomerdiscountavailable = json.findValue("txncustomerdiscountavailable").asText();
            Double txnnetamount = json.findValue("txnnetamount") == null ? 0.0
                    : json.findValue("txnnetamount").asDouble();
            // Double netAmountTotalWithDecimalValue =
            // json.findValue("ChartOfAccountsDAOImpl").asDouble();
            int txnreceiptdetails = json.findValue("txnReceiptDetails") == null ? 0
                    : json.findValue("txnReceiptDetails").asInt();
            String txnreceipttypebankdetails = json.findValue("txnReceiptDescription").asText();
            String txnremarks = json.findValue("txnremarks").asText();
            String supportingdoc = json.findValue("supportingdoc").asText();
            String txnForItemStr = json.findValue("txnforitem").toString();
            String txnSourceGstin = json.findValue("txnSourceGstin").asText();
            String txnDestinGstin = json.findValue("txnDestinGstin").asText();
            int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0
                    : json.findValue("txnTypeOfSupply").asInt();
            Integer txnWithWithoutTax = json.findValue("txnWithWithoutTax") == null ? null
                    : json.findValue("txnWithWithoutTax").asInt();
            int txnWalkinCustomerType = json.findValue("txnWalkinCustomerType") == null ? 0
                    : json.findValue("txnWalkinCustomerType").asInt();
            int klfollowednotfollowed = json.findValue("klfollowednotfollowed") == null ? 1
                    : json.findValue("klfollowednotfollowed").asInt();
            String docRefNo = json.findValue("txnDocRefNo") == null ? "" : json.findValue("txnDocRefNo").asText();
            String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
            Date txnDate = IdosUtil.getFormatedDate(selectedTxnDate);

            String customerVendorName = "";
            Vendor txncustomer = null;
            if (txnforunavailablecustomer != null && !"".equals(txnforunavailablecustomer)) {
                ObjectNode objectNode = (ObjectNode) json;
                if (txnWalkinCustomerType == 1 || txnWalkinCustomerType == 2) {
                    objectNode.put("custRegisteredOrUnReg", 1);
                } else {
                    objectNode.put("custRegisteredOrUnReg", 0);
                }
                JsonNode modifiedJson = objectNode;
                txncustomer = customerDao.saveCustomer(modifiedJson, user, entityManager, entitytransaction,
                        IdosConstants.WALK_IN_CUSTOMER, txnWalkinCustomerType);
            } else if (txnforcustomer != null && !txnforcustomer.equals("")) {
                txncustomer = genericDAO.getById(Vendor.class, IdosUtil.convertStringToLong(txnforcustomer),
                        entityManager);
                customerVendorName = txncustomer.getName();
            }

            if (txncustomer == null) {
                throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                        "Customer is missing", IdosConstants.RECORD_NOT_FOUND);
            }

            JSONArray arrJSON = new JSONArray(txnForItemStr);
            String txnRemarks = "";
            Branch txnBranch = null;
            String branchName = "";
            String paymentMode = "";
            Project txnProject = null;
            String projectName = "";
            String itemName = "";
            String debitCredit = "Credit";
            String txnInstrumentNumber = "";
            String txnInstrumentDate = "";
            TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);

            if (txnEntityID > 0) {
                txn = Transaction.findById(txnEntityID);
            } else {
                txn = new Transaction();
            }
            if (txnforbranch != null && !txnforbranch.equals("")) {
                txnBranch = genericDAO.getById(Branch.class, IdosUtil.convertStringToLong(txnforbranch), entityManager);
                branchName = txnBranch.getName();
            }
            if (txnforproject != null && !txnforproject.equals("")) {
                txnProject = genericDAO.getById(Project.class, IdosUtil.convertStringToLong(txnforproject),
                        entityManager);
                projectName = txnProject.getName();
            }
            if (IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeVal) {
                txn.setPaymentStatus("NOT-PAID");
                txn.setPerformaInvoice(performaInvoice);
                txn.setCustomerDuePayment(txnnetamount);
            }

            for (int i = 0; i < arrJSON.length(); i++) {
                JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                String itemIdList = rowItemData.getString("txnItems");
            }

            // Enter data for first item in transaction table to be displayed in Transaction
            // list
            JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
            Long itemIdRow0 = firstRowItemData.getLong("txnItems");
            Specifics txnItem = Specifics.findById(itemIdRow0);
            Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
            Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
            Double txnGrossRow0 = firstRowItemData.getDouble("txnGross");
            // String howMuchAdvance = firstRowItemData.getString("howMuchAdvance");
            txn.setTransactionSpecifics(txnItem);
            txn.setTransactionParticulars(txnItem.getParticularsId());
            txn.setNoOfUnits(txnNoOfUniRow0t);
            txn.setPricePerUnit(txnPerUnitPriceRow0);
            txn.setGrossAmount(txnGrossRow0);
            txn.setSourceGstin(txnSourceGstin);
            txn.setDestinationGstin(txnDestinGstin);
            txn.setTypeOfSupply(txnTypeOfSupply);
            txn.setWithWithoutTax(txnWithWithoutTax);
            txn.setWalkinCustomerType(txnWalkinCustomerType);
            txn.setTransactionPurpose(usertxnPurpose);
            txn.setTransactionBranch(txnBranch);
            txn.setTransactionBranchOrganization(txnBranch.getOrganization());
            txn.setTransactionProject(txnProject);
            txn.setTransactionVendorCustomer(txncustomer);
            txn.setTransactionUnavailableVendorCustomer(txnforunavailablecustomer);
            txn.setInvoiceNumber(docRefNo);
            if (!txnforunavailablecustomer.equals("") && txnforunavailablecustomer != null) {
                customerVendorName = txnforunavailablecustomer;
            }
            // if (howMuchAdvance != null && !howMuchAdvance.equals("")) {
            // CustomerBranchWiseAdvBalance advAmountForItem =
            // CustomerBranchWiseAdvBalance
            // .getAdvAmountForItem(entityManager, user.getOrganization().getId(),
            // Long.parseLong(txnforcustomer),
            // Long.parseLong(txnforbranch), txnTypeOfSupply,
            // Long.parseLong(txnDestinGstin),
            // itemIdRow0);
            // Double advAmount = advAmountForItem.getAdvanceAmount();
            // advAmount = advAmount - Double.parseDouble(howMuchAdvance);
            // advAmountForItem.setAdvanceAmount(advAmount);
            // genericDAO.saveOrUpdate(advAmountForItem, user, entityManager);
            // }
            txn.setPoReference(txnPoReference);
            txn.setNetAmount(txnnetamount);
            // Double roundedCutPartOfNetAmount = txnnetamount -
            // netAmountTotalWithDecimalValue;
            // txn.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
            txn.setTransactionDate(txnDate);
            if (DateUtil.isBackDate(txnDate)) {
                txn.setIsBackdatedTransaction(IdosConstants.BACK_DATED_TXN);
            }
            txn.setReceiptDetailsType(txnreceiptdetails);
            txn.setReceiptDetailsDescription(txnreceipttypebankdetails);
            if (IdosConstants.PAYMODE_CASH == txnreceiptdetails) {
                paymentMode = "CASH";
            } else if (IdosConstants.PAYMODE_BANK == txnreceiptdetails) {
                paymentMode = "BANK";
            }
            if (!txnremarks.equals("") && txnremarks != null) {
                txnRemarks = user.getEmail() + "#" + txnremarks;
                txn.setRemarks(txnRemarks);
                txnRemarks = txn.getRemarks();
            }
            txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, entityManager));
            txn.setTransactionStatus("Accounted");
            txn.setKlFollowStatus(klfollowednotfollowed);
            String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
            txn.setTransactionRefNumber(transactionNumber);
            if (txnPurposeVal == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW) {
                txn.setTxnDone(1);
                txn.setPaymentStatus("PAID");
                txn.setCustomerDuePayment(0.0);
                txn.setCustomerNetPayment(txnnetamount);
                if (IdosConstants.PAYMODE_CASH == txnreceiptdetails) {
                    branchCashService.updateBranchCashDetail(entityManager, user, txnBranch, txnnetamount, false,
                            txn.getTransactionDate(), result);
                } else if (IdosConstants.PAYMODE_BANK == txnreceiptdetails) {
                    String txnreceiptPaymentBank = json.findValue("txnReceiptPaymentBank") != null
                            ? json.findValue("txnReceiptPaymentBank").asText()
                            : null;
                    txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
                            ? json.findValue("txnInstrumentNum").asText()
                            : "";
                    txnInstrumentDate = json.findValue("txnInstrumentDate") != null
                            ? json.findValue("txnInstrumentDate").asText()
                            : "";
                    if (txnreceiptPaymentBank != null && !txnreceiptPaymentBank.equals("")) {
                        BranchBankAccounts bankAccount = BranchBankAccounts
                                .findById(IdosUtil.convertStringToLong(txnreceiptPaymentBank));
                        if (bankAccount == null) {
                            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                    IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                    "Bank is not selected in transaction when payment mode is Bank.");
                        }
                        txn.setTransactionBranchBankAccount(bankAccount);
                        if (txnInstrumentNumber != null && !"".equals(txnInstrumentNumber)) {
                            txn.setInstrumentNumber(txnInstrumentNumber);
                        }
                        if (txnInstrumentDate != null && !"".equals(txnInstrumentDate)) {
                            txn.setInstrumentDate(txnInstrumentDate);
                        }
                        boolean branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(
                                entityManager, user, bankAccount, txnnetamount, false, result, txn.getTransactionDate(),
                                txn.getTransactionBranch());
                        if (!branchBankDetailEntered) {
                            return txn; // since balance is in -ve don't make any changes in DB
                        }
                    }
                }
            }
            txn.setLinkedTxnRef(txnBomTxnRef);
            transactionDao.setInvoiceQuotProfSerial(user, entityManager, txn);
            genericDAO.saveOrUpdate(txn, user, entityManager);
            FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, supportingdoc, txn.getId(),
                    IdosConstants.MAIN_TXN_TYPE);
            List<TransactionItems> transactionItems = new ArrayList<TransactionItems>();
            // Enter multiple items data into TransactionItems table
            // TransactionItemsController.insertListOfMultipleItemsIntoTransactionItems(arrJSON,
            // txn);
            if (txnEntityID > 0) {
                TRANSACTION_ITEMS_SERVICE.updateMultipleItemsTransactionItems(entityManager, user, arrJSON, txn);
            } else {
                TRANSACTION_ITEMS_SERVICE.insertMultipleItemsTransactionItems(entityManager, user, arrJSON, txn,
                        txnDate);
            }
            genericDAO.saveOrUpdate(txn, user, entityManager); // need to update becuase of desgin issue
            // Trial balance entries
            trialBalanceService.insertTrialBalance(txn, user, entityManager);
            if (txn.getRoundedCutPartOfNetAmount() != null && txn.getRoundedCutPartOfNetAmount() != 0.0) {
                Boolean roundupMappingFound = null;
                if (txn.getRoundedCutPartOfNetAmount() > 0) {
                    roundupMappingFound = CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceForRoundOff(
                            txn.getTransactionBranchOrganization(), txn.getTransactionBranch(), txn.getId(),
                            txn.getTransactionPurpose(), txn.getTransactionDate(), txn.getRoundedCutPartOfNetAmount(),
                            user, entityManager, true);
                } else {
                    roundupMappingFound = CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceForRoundOff(
                            txn.getTransactionBranchOrganization(), txn.getTransactionBranch(), txn.getId(),
                            txn.getTransactionPurpose(), txn.getTransactionDate(), txn.getRoundedCutPartOfNetAmount(),
                            user, entityManager, false);
                }
                result.put("roundupMappingFound", roundupMappingFound);
                if (!roundupMappingFound) {
                    return txn;
                }
            }
            // if trading inventory then need this for INVENTORY_REPORT
            stockService.insertTradingInventory(txn, user, entityManager);

            // call Karvy API to submit Sell data for GST Filing
            KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
            karvyAPICall.saveGSTFilingData(user, txn, entityManager);
            // karvyAPICall.sendSellTranDataToKarvy(txn,entityManager);

            // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
            // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
            // for (int i = 0; i < keyArray.length; i++) {
            // StringBuilder sbquery = new StringBuilder("");
            // sbquery.append(
            // "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
            // obj.presentStatus=1");
            // List<Users> orgusers = genericDAO.executeSimpleQuery(sbquery.toString(),
            // entityManager);
            // if (!orgusers.isEmpty()
            // && orgusers.get(0).getOrganization().getId() ==
            // user.getOrganization().getId()) {
            // orgtxnregistereduser.put(keyArray[i].toString(),
            // CreatorActor.expenseregistrered.get(keyArray[i]));
            // }
            // }
            entitytransaction.commit();
            String invoiceDate = "";
            String invoiceDateLabel = "";
            if (txn.getTransactionInvoiceDate() != null) {
                invoiceDateLabel = "INVOICE DATE:";
                invoiceDate = IdosConstants.idosdf.format(txn.getTransactionInvoiceDate());
            }
            String itemParentName = "";
            if (txnItem.getParentSpecifics() != null && !txnItem.getParentSpecifics().equals("")) {
                itemParentName = txnItem.getParentSpecifics().getName();
            } else {
                itemParentName = txnItem.getParticularsId().getName();
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
            if (txn.getNetAmountResultDescription() != null && !txn.getNetAmountResultDescription().equals("null")) {
                txnResultDesc = txn.getNetAmountResultDescription();
            }
            String tranInvoice = txn.getInvoiceNumber();
            Integer typeOfSupply = txn.getTypeOfSupply() == null ? 0 : txn.getTypeOfSupply();
            String txnDocument = txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs();
            TransactionViewResponse.addTxn(txn.getId(), branchName, projectName, itemName, itemParentName,
                    customerVendorName,
                    txn.getTransactionPurpose().getTransactionPurpose(),
                    IdosConstants.idosdf.format(txn.getTransactionDate()), invoiceDateLabel, invoiceDate, paymentMode,
                    txn.getNoOfUnits(), txn.getPricePerUnit(), txn.getGrossAmount(), txn.getNetAmount(), txnResultDesc,
                    "", txn.getTransactionStatus(), txn.getCreatedBy().getEmail(), "", "", txnDocument, txnRemarks,
                    debitCredit, txnSpecialStatus, txn.getFrieghtCharges(), txn.getPoReference(),
                    txnInstrumentNumber, txnInstrumentDate, txn.getTransactionPurpose().getId(), tranInvoice,
                    txn.getTransactionRefNumber(), typeOfSupply, result);
        } catch (

        Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Sell on Cash- submit for approval", ex.getMessage());
        }
        return txn;
    }

    @Override
    public boolean completeAccounting(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, Transaction txn, ObjectNode result) throws IDOSException {
        log.log(Level.FINE, "============ Start");
        try {
            int txnreceiptdetails = json.findValue("paymentDetails") != null ? json.findValue("paymentDetails").asInt()
                    : 0;
            String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
            String txnRmarks = json.findValue("txnRmarks").asText();
            txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
                    user.getEmail(), suppDoc, user, entityManager));
            if (txnRmarks != null && !txnRmarks.equals("")) {
                if (txn.getRemarks() != null) {
                    txn.setRemarks(txn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
                } else {
                    txn.setRemarks(user.getEmail() + "#" + txnRmarks);
                }
            }
            txn.setTransactionStatus("Accounted");
            String txnInstrumentNumber = "";
            String txnInstrumentDate = "";

            if (txn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW) {
                txn.setTxnDone(1);
                txn.setPaymentStatus("PAID");
                txn.setCustomerDuePayment(0.0);
                txn.setCustomerNetPayment(txn.getNetAmount());
                String txnReceipttyDesciption = null;
                long txnReceiptPaymentBank = 0L;
                if (txn.getReceiptDetailsType() == null || txn.getReceiptDetailsType() == 0) {
                    txnReceiptPaymentBank = json.findValue("txnPaymentBank") != null
                            ? json.findValue("txnPaymentBank").asLong()
                            : 0L;
                    txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
                            ? json.findValue("txnInstrumentNum").asText()
                            : "";
                    txnInstrumentDate = json.findValue("txnInstrumentDate") != null
                            ? json.findValue("txnInstrumentDate").asText()
                            : "";
                    txnReceipttyDesciption = json.findValue("bankInf") != null ? json.findValue("bankInf").asText()
                            : null;
                } else {
                    txnreceiptdetails = txn.getReceiptDetailsType();
                    if (txnreceiptdetails == IdosConstants.PAYMODE_BANK) {
                        if (txn.getTransactionBranchBankAccount() == null) {
                            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
                                    IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
                                    "Bank is not selected in transaction when payment mode is Bank.");
                        }
                        txnReceiptPaymentBank = txn.getTransactionBranchBankAccount().getId();
                        txnInstrumentNumber = txn.getInstrumentNumber();
                        txnInstrumentDate = txn.getInstrumentDate();
                    }
                    txnReceipttyDesciption = txn.getReceiptDetailsDescription();
                }
                txn.setReceiptDetailsDescription(txnReceipttyDesciption);
                boolean isValid = setTxnPaymentDetail(user, entityManager, txn, txnreceiptdetails,
                        txnReceiptPaymentBank, txnInstrumentNumber, txnInstrumentDate, result);
                if (!isValid) {
                    return false;
                }
                if (txn.getInvoiceNumber() == null || txn.getInvoiceNumber().equals("")) {
                    transactionDao.setInvoiceQuotProfSerial(user, entityManager, txn);
                }
                genericDAO.saveOrUpdate(txn, user, entityManager);
                FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, suppDoc, txn.getId(),
                        IdosConstants.MAIN_TXN_TYPE);
                KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
                karvyAPICall.saveGSTFilingData(user, txn, entityManager);
            } else if (IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txn.getTransactionPurpose().getId()
                    || IdosConstants.CREDIT_NOTE_CUSTOMER == txn.getTransactionPurpose().getId()
                    || IdosConstants.DEBIT_NOTE_CUSTOMER == txn.getTransactionPurpose().getId()
                    || IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == txn.getTransactionPurpose()
                            .getId()) {
                txn.setPaymentStatus("NOT-PAID");
                txn.setPerformaInvoice(false);
                txn.setCustomerDuePayment(txn.getNetAmount());
                if (IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txn.getTransactionPurpose().getId()
                        && (txn.getInvoiceNumber() == null || txn.getInvoiceNumber().equals(""))) {
                    transactionDao.setInvoiceQuotProfSerial(user, entityManager, txn);
                } else {
                    transactionDao.setInvoiceQuotProfSerial(user, entityManager, txn);
                }
                genericDAO.saveOrUpdate(txn, user, entityManager);
                KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
                karvyAPICall.saveGSTFilingData(user, txn, entityManager);

                if (IdosConstants.CREDIT_NOTE_CUSTOMER == txn.getTransactionPurpose().getId()
                        || IdosConstants.DEBIT_NOTE_CUSTOMER == txn.getTransactionPurpose().getId()) {
                    List<Transaction> sellTxnList = Transaction.findByTxnReference(entityManager,
                            txn.getTransactionBranchOrganization().getId(), txn.getLinkedTxnRef());
                    Transaction sellTxn = null;
                    if (sellTxnList != null && !sellTxnList.isEmpty()) {
                        sellTxn = sellTxnList.get(0);
                    }
                    if (sellTxn != null) {
                        Double salesReturn = sellTxn.getSalesReturnAmount() == null ? 0.0
                                : sellTxn.getSalesReturnAmount();
                        salesReturn += txn.getNetAmount();
                        sellTxn.setSalesReturnAmount(salesReturn);
                        genericDAO.saveOrUpdate(sellTxn, user, entityManager);
                    }
                }

            } else if (IdosConstants.SALES_RETURNS == txn.getTransactionPurpose().getId()) {
                txn.setPaymentStatus("PAID");
                txn.setCustomerDuePayment(0.0);
                txn.setCustomerNetPayment(txn.getNetAmount());
                genericDAO.saveOrUpdate(txn, user, entityManager);
                Map<String, Object> criterias = new HashMap<String, Object>(2);
                criterias.put("transactionRefNumber", txn.getPaidInvoiceRefNumber());
                criterias.put("presentStatus", 1);
                Transaction previousTransaction = genericDAO.getByCriteria(Transaction.class, criterias, entityManager);
                Double saleReturnValue = 0.0;
                if (previousTransaction.getSalesReturnAmount() != null) {
                    saleReturnValue = previousTransaction.getSalesReturnAmount() + txn.getNetAmount();
                } else if (previousTransaction.getSalesReturnAmount() == null) {
                    saleReturnValue = txn.getNetAmount();
                }
                previousTransaction.setSalesReturnAmount(saleReturnValue);
                genericDAO.saveOrUpdate(previousTransaction, user, entityManager);
                KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
                karvyAPICall.saveGSTFilingData(user, txn, entityManager);
            }

            if (txn.getTransactionPurpose().getId() != IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
                    && txn.getTransactionPurpose().getId() != IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
                txn.setReceiptDetailsType(txnreceiptdetails);
            }

            // Trial balance entries
            trialBalanceService.insertTrialBalance(txn, user, entityManager);
            if (IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txn.getTransactionPurpose().getId()
                    || IdosConstants.DEBIT_NOTE_CUSTOMER == txn.getTransactionPurpose().getId()) {
                if (txn.getRoundedCutPartOfNetAmount() != null && txn.getRoundedCutPartOfNetAmount() != 0.0) {
                    Boolean roundupMappingFound = null;
                    if (txn.getRoundedCutPartOfNetAmount() > 0) {
                        roundupMappingFound = CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceForRoundOff(
                                txn.getTransactionBranchOrganization(), txn.getTransactionBranch(), txn.getId(),
                                txn.getTransactionPurpose(), txn.getTransactionDate(),
                                txn.getRoundedCutPartOfNetAmount(), user, entityManager, true);
                    } else {
                        roundupMappingFound = CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceForRoundOff(
                                txn.getTransactionBranchOrganization(), txn.getTransactionBranch(), txn.getId(),
                                txn.getTransactionPurpose(), txn.getTransactionDate(),
                                txn.getRoundedCutPartOfNetAmount(), user, entityManager, false);
                    }
                    result.put("roundupMappingFound", roundupMappingFound);
                    if (roundupMappingFound == null || !roundupMappingFound) {
                        return false;
                    }
                }
            } else if (IdosConstants.CREDIT_NOTE_CUSTOMER == txn.getTransactionPurpose().getId()) {
                if (txn.getRoundedCutPartOfNetAmount() != null && txn.getRoundedCutPartOfNetAmount() != 0.0) {
                    Boolean roundupMappingFound = null;
                    if (txn.getRoundedCutPartOfNetAmount() > 0) {
                        roundupMappingFound = CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceForRoundOff(
                                txn.getTransactionBranchOrganization(), txn.getTransactionBranch(), txn.getId(),
                                txn.getTransactionPurpose(), txn.getTransactionDate(),
                                txn.getRoundedCutPartOfNetAmount(), user, entityManager, false);
                    } else {
                        roundupMappingFound = CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceForRoundOff(
                                txn.getTransactionBranchOrganization(), txn.getTransactionBranch(), txn.getId(),
                                txn.getTransactionPurpose(), txn.getTransactionDate(),
                                txn.getRoundedCutPartOfNetAmount(), user, entityManager, true);
                    }
                    result.put("roundupMappingFound", roundupMappingFound);
                    if (roundupMappingFound == null || !roundupMappingFound) {
                        return false;
                    }
                }
            }
            // if trading inventory then need this for INVENTORY_REPORT
            // if (IdosConstants.SALES_RETURNS != txn.getTransactionPurpose().getId() &&
            // txn.getTransactionSpecifics() != null &&
            // txn.getTransactionSpecifics().getIsTradingInvenotryItem() != null &&
            // txn.getTransactionSpecifics().getIsTradingInvenotryItem() == 1) {
            stockService.insertTradingInventory(txn, user, entityManager);
            // }

            // entitytransaction.commit();
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error Complete accounting", ex.getMessage());
        }
        log.log(Level.FINE, "============ End");
        return true;
    }

    @Override
    public boolean verifyItemInvetory(Users user, EntityManager entityManager, Transaction txn, ObjectNode result)
            throws IDOSException {
        boolean returnValue = true;
        Map<String, Object> criterias = new HashMap<String, Object>(2);
        criterias.put("transaction.id", txn.getId());
        criterias.put("presentStatus", 1);
        List<TransactionItems> listTransactionItems = genericDAO.findByCriteria(TransactionItems.class, criterias,
                entityManager);
        for (TransactionItems transItem : listTransactionItems) {
            if (transItem.getTransactionSpecifics().getIsTradingInvenotryItem() != null
                    && transItem.getTransactionSpecifics().getIsTradingInvenotryItem() == 1) {
                double totalStock = stockService.getBranchItemPresentStock(result, user, entityManager,
                        txn.getTransactionBranch().getId(), transItem.getTransactionSpecifics().getId());
                if (totalStock == 0 || transItem.getNoOfUnits() > totalStock) {
                    returnValue = false;
                    throw new IDOSException(IdosConstants.INSUFFICIENT_INVENTORY_ERRCODE,
                            IdosConstants.INSUFFICIENT_INVENTORY_EXCEPTION,
                            "Insufficeint inventory balance of item: " + transItem.getTransactionSpecifics().getName()
                                    + " " + transItem.getTransactionSpecifics().getId(),
                            "Insufficeint inventory balance of item: " + transItem.getTransactionSpecifics().getName()
                                    + ", balance: " + totalStock);
                }
            }
        }
        return returnValue;
    }

    @Override
    public boolean getAdvanceDiscount(Users user, EntityManager entityManager, JsonNode json, ObjectNode result)
            throws IDOSException {
        return SELL_TRANSACTION_DAO.getAdvanceDiscount(user, entityManager, json, result);
    }

    @Override
    public boolean getShippingAddress(Users user, Transaction txn, ObjectNode result, EntityManager entityManager) {
        return SELL_TRANSACTION_DAO.getShippingAddress(user, txn, result, entityManager);
    }

    @Override
    public boolean getAdditionalDetails(Users user, Transaction txn, ObjectNode result, EntityManager entityManager) {
        return SELL_TRANSACTION_DAO.getAdditionalDetails(user, txn, result, entityManager);
    }

    @Override
    public Transaction submitForCancellation(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
        return CANCEL_INVOICE_TXN_DAO.submitForCancellation(user, json, entityManager, entitytransaction, result);
    }

    @Override
    public boolean setTxnPaymentDetail(Users user, EntityManager em, Transaction txn, int paymentMode, long paymentBank,
            String txnInstrumentNumber, String txnInstrumentDate, ObjectNode result) throws IDOSException {
        if (IdosConstants.PAYMODE_CASH == paymentMode) {
            branchCashService.updateBranchCashDetail(em, user, txn.getTransactionBranch(), txn.getNetAmount(), false,
                    txn.getTransactionDate(), result);
        } else if (IdosConstants.PAYMODE_BANK == paymentMode) {
            Double creditAmount = null;
            Double debitAmount = null;
            Double resultantAmount = null;
            Double amountBalance = null;
            BranchBankAccounts bankAccount = BranchBankAccounts.findById(paymentBank);
            if (bankAccount == null) {
                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.INVALID_DATA_EXCEPTION,
                        "Bank is not selected in transaction when payment mode is Bank.");
            }
            txn.setTransactionBranchBankAccount(bankAccount);
            if (txnInstrumentNumber != null && !"".equals(txnInstrumentNumber)) {
                txn.setInstrumentNumber(txnInstrumentNumber);
            }
            if (txnInstrumentDate != null && !"".equals(txnInstrumentDate)) {
                txn.setInstrumentDate(txnInstrumentDate);
            }
            boolean branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(em, user, bankAccount,
                    txn.getNetAmount(), false, result, txn.getTransactionDate(), txn.getTransactionBranch());
            if (!branchBankDetailEntered) {
                return false; // since balance is in -ve don't make any changes in DB
            }
        }
        return true;
    }
}
