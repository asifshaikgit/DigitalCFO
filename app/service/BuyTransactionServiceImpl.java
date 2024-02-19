package service;

import actor.CreatorActor;

import com.idos.util.*;

import model.*;
import pojo.TransactionViewResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import play.libs.Json;
import play.mvc.WebSocket;

import java.util.logging.Level;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import akka.stream.javadsl.*;
import akka.actor.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import akka.NotUsed;

/**
 * Created by Sunil Namdev on 12-12-2016.
 */
public class BuyTransactionServiceImpl implements BuyTransactionService {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public Transaction submitForApprovalPurchaseReturn(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, TransactionPurpose transactionPurpose, ObjectNode result)
            throws IDOSException {
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
            Double netAmountTotalWithDecimalValue = json.findValue("netAmountTotalWithDecimalValue").asDouble();
            String txnremarks = (json.findValue("txnremarks") == null || "".equals(json.findValue("txnremarks"))) ? null
                    : json.findValue("txnremarks").asText();
            String supportingdoc = (json.findValue("supportingdoc") == null
                    || "".equals(json.findValue("supportingdoc"))) ? null : json.findValue("supportingdoc").asText();
            String txnForItemStr = json.findValue("txnforitem").toString();
            String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
            Date txnDate = null;
            try {
                if (selectedTxnDate != null && !"".equals(selectedTxnDate)) {
                    txnDate = IdosConstants.IDOSDF.parse(selectedTxnDate);
                } else {
                    txnDate = new Date();
                }
            } catch (ParseException e) {
                throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
                        IdosConstants.NULL_KEY_EXC_ESMF_MSG,
                        "cannot parse date: " + selectedTxnDate + " " + e.getMessage());
            }

            // Purchase Return transaction entry into Transaction table

            if (txnEntityID > 0) {
                txn = Transaction.findById(txnEntityID);
            } else {
                txn = new Transaction();
            }
            transactionPurpose = TransactionPurpose.findById(txnPurposeVal);
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
            Double roundedCutPartOfNetAmount = txnnetamount - netAmountTotalWithDecimalValue;
            txn.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
            String netDesc = "Invoice Value:" + invoiceTransaction.getNetAmount() + ",Purchase Return Value:"
                    + txnnetamount;
            txn.setNetAmountResultDescription(netDesc);
            txn.setTransactionDate(txnDate);
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
            // if (txnEntityID > 0) {
            // TRANSACTION_ITEMS_SERVICE.updateMultipleItemsSalesReturnTransactionItems(entityManager,
            // user, arrJSON,
            // txn);
            // } else {
            // TRANSACTION_ITEMS_SERVICE.insertMultipleItemsTransactionItems(entityManager,
            // user, arrJSON, txn,
            // txnDate);
            // }
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
    public Transaction submit4AccoutingBuyOnPetty(Users user, EntityManager em, JsonNode json,
            TransactionPurpose usertxnPurpose, ObjectNode result) throws IDOSException {
        return BUY_TRANSACTION_DAO.submit4AccoutingBuyOnPetty(user, em, json, usertxnPurpose, result);
    }

    @Override
    public Transaction submitForApproval(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, TransactionPurpose txnPurpose, ObjectNode result)
            throws IDOSException {
        Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                : json.findValue("txnEntityID").asLong();
        Long txnForBranch = json.findValue("txnForBranch").asLong();
        String projectID = json.findValue("txnForProject").asText();
        Long txnForProject = "".equals(projectID) ? null : json.findValue("txnForProject").asLong();
        String txnPOInvoice = json.findValue("txnPOInvoice") == null ? null : json.findValue("txnPOInvoice").asText();
        String txnPOInvoiceTxnRef = json.findValue("txnPOInvoiceTxnRef") == null ? null
                : json.findValue("txnPOInvoiceTxnRef").asText();
        String txnForItemStr = json.findValue("txnForItem").toString();
        Long txnForCustomer = "".equals(json.findValue("txnForCustomer").asText()) ? null
                : json.findValue("txnForCustomer").asLong();
        String txnForUnavailableCustomer = json.findValue("txnForUnavailableCustomer").asText();
        Double txnTotalNetAmount = json.findValue("txnTotalNetAmount") == null ? 0.0
                : json.findValue("txnTotalNetAmount").asDouble();
        Double totalTxnNetAmtWithDecimalValue = json.findValue("totalTxnNetAmtWithDecimalValue").asDouble();
        Double totalWithholdTaxAmt = json.findValue("totalWithholdTaxAmt") == null ? 0.0
                : json.findValue("totalWithholdTaxAmt").asDouble();
        Double totalTxnTaxAmt = json.findValue("totalTxnTaxAmt") == null ? 0.0
                : json.findValue("totalTxnTaxAmt").asDouble();
        Double totalTxnGrossAmt = json.findValue("totalTxnGrossAmt") == null ? 0.0
                : json.findValue("totalTxnGrossAmt").asDouble();
        int klfollowednotfollowed = json.findValue("klfollowednotfollowed") == null ? 1
                : json.findValue("klfollowednotfollowed").asInt();
        String txnRemarks = json.findValue("txnRemarks").asText();
        String txnprocrem = json.findValue("txnprocrem") != null ? json.findValue("txnprocrem").asText() : null;
        String supportingdoc = json.findValue("supportingdoc").asText();
        Boolean txnDocumentUploadRequired = json.findValue("txnDocumentUploadRequired") != null
                ? json.findValue("txnDocumentUploadRequired").asBoolean()
                : null;
        // String txnLeftOutWithholdTransIDs =
        // json.findValue("txnLeftOutWithholdTransIDs") == null ? null :
        // json.findValue("txnLeftOutWithholdTransIDs").asText();
        String txnSourceGstin = json.findValue("txnSourceGstin") == null ? null
                : json.findValue("txnSourceGstin").asText();
        String txnDestinGstin = json.findValue("txnDestinGstin") == null ? null
                : json.findValue("txnDestinGstin").asText();
        Long destinGstinId = (json.findValue("txnDestinGstinId") == null
                || "".equals(json.findValue("txnDestinGstinId")))
                        ? 0l
                        : json.findValue("txnDestinGstinId").asLong();
        int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0 : json.findValue("txnTypeOfSupply").asInt();
        int txnWalkinCustomerType = json.findValue("txnWalkinCustomerType") == null ? 0
                : json.findValue("txnWalkinCustomerType").asInt();
        if (txnWalkinCustomerType == 1 || txnWalkinCustomerType == 2) {
            VENDOR_DAO.saveVendor(json, user, entityManager, IdosConstants.WALK_IN_VENDOR);
        }
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
        String remarks = "";
        Branch txnBranch = null;
        String branchName = "";
        Vendor txncustomer = null;
        String paymentMode = "";
        Project txnProject = null;
        String projectName = "";
        String itemName = "";
        String customerVendorName = "";
        String netAmtInputTaxDesc = "";
        String debitCredit = "Credit";
        Transaction txn = null;
        try {
            if (txnEntityID > 0) {
                txn = Transaction.findById(txnEntityID);
            } else {
                txn = new Transaction();
            }
            if (txnForBranch != null) {
                txnBranch = Branch.findById(txnForBranch);
                branchName = txnBranch.getName();
            }
            if (txnForProject != null) {
                txnProject = Project.findById(txnForProject);
                projectName = txnProject.getName();
            }
            JSONArray arrJSON = new JSONArray(txnForItemStr);
            // Enter data for first item in transaction table to be displayed in Transaction
            // list
            JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
            Long itemIdRow0 = firstRowItemData.getLong("txnItems");
            Specifics txnSpecificItem = Specifics.findById(itemIdRow0);
            itemName = txnSpecificItem.getName();
            Double txnPerUnitPriceRow0 = firstRowItemData.getDouble("txnPerUnitPrice");
            Double txnNoOfUniRow0t = firstRowItemData.getDouble("txnNoOfUnit");
            // Double howMuchAdvance = 0.0;
            // Double customerAdvance = 0.0;
            // if (!firstRowItemData.isNull("customerAdvance") &&
            // !firstRowItemData.get("customerAdvance").equals("")) {
            // customerAdvance = firstRowItemData.getDouble("customerAdvance");
            // }
            // if (!firstRowItemData.isNull("howMuchAdvance") &&
            // !firstRowItemData.get("howMuchAdvance").equals("")) {
            // howMuchAdvance = firstRowItemData.getDouble("howMuchAdvance");
            // }
            txn.setTransactionSpecifics(txnSpecificItem);
            txn.setTransactionParticulars(txnSpecificItem.getParticularsId());
            txn.setNoOfUnits(txnNoOfUniRow0t);
            txn.setPricePerUnit(txnPerUnitPriceRow0);
            txn.setGrossAmount(totalTxnGrossAmt);
            // txn.setAvailableAdvance(customerAdvance);
            // txn.setAdjustmentFromAdvance(howMuchAdvance);
            txn.setPaidInvoiceRefNumber(txnPOInvoice); // Purchase Order transctionId which is settled using kaizala
            txn.setSourceGstin(txnSourceGstin);
            txn.setDestinationGstin(txnDestinGstin);
            txn.setTypeOfSupply(txnTypeOfSupply);
            txn.setWalkinCustomerType(txnWalkinCustomerType);
            txn.setTransactionPurpose(txnPurpose);
            txn.setTransactionBranch(txnBranch);
            txn.setTransactionBranchOrganization(txnBranch.getOrganization());
            txn.setTransactionProject(txnProject);
            txn.setTransactionUnavailableVendorCustomer(txnForUnavailableCustomer);
            if (txnForUnavailableCustomer != null && !txnForUnavailableCustomer.equals("")) {
                customerVendorName = txnForUnavailableCustomer;
            }
            if (txnForCustomer != null) {
                txncustomer = Vendor.findById(txnForCustomer);
                customerVendorName = txncustomer.getName();
            }
            txn.setTransactionVendorCustomer(txncustomer);
            txn.setKlFollowStatus(klfollowednotfollowed);

            Double roundedCutPartOfNetAmount = txnTotalNetAmount - totalTxnNetAmtWithDecimalValue;
            txn.setRoundedCutPartOfNetAmount(roundedCutPartOfNetAmount);
            txn.setTransactionDate(txnDate);
            if (DateUtil.isBackDate(txnDate)) {
                txn.setIsBackdatedTransaction(IdosConstants.BACK_DATED_TXN);
            }
            txn.setWithholdingTax(totalWithholdTaxAmt);
            txn.setNetAmount(txnTotalNetAmount);
            int txnReceiptDetails = json.findValue("txnReceiptDetails") != null
                    ? json.findValue("txnReceiptDetails").asInt()
                    : 0;
            String txnReceiptTypeBankDetails = json.findValue("txnReceiptDescription").asText();
            long txnReceiptPaymentBank = json.findValue("txnReceiptPaymentBank") != null
                    ? json.findValue("txnReceiptPaymentBank").asLong()
                    : null;
            String txnInstrumentNum = json.findValue("txnInstrumentNum") != null
                    ? json.findValue("txnInstrumentNum").asText()
                    : null;
            String txnInstrumentDate = json.findValue("txnInstrumentDate") != null
                    ? json.findValue("txnInstrumentDate").asText()
                    : null;
            // long txnReceiptPaymentBank = json.findValue("txnPaymentBank") != null ?
            // json.findValue("txnPaymentBank").asLong() : 0;
            ObjectNode result1 = Json.newObject();
            setTxnPaymentDetail(user, entityManager, txn, txnReceiptDetails, txnReceiptPaymentBank, txnInstrumentNum,
                    txnInstrumentDate, result1);
            if (IdosConstants.PAYMODE_CASH == txnReceiptDetails) {
                paymentMode = "CASH";
            } else if (IdosConstants.PAYMODE_BANK == txnReceiptDetails) {
                paymentMode = "BANK";
            } else if (IdosConstants.PAYMODE_PETTY_CASH == txnReceiptDetails) {
                paymentMode = "PETTY CASH";
            }
            txn.setReceiptDetailsType(txnReceiptDetails);
            txn.setReceiptDetailsDescription(txnReceiptTypeBankDetails);
            if (txnRemarks != null && !txnRemarks.equals("")) {
                if (txn.getRemarks() != null) {
                    txn.setRemarks(txnRemarks);
                } else {
                    remarks = user.getEmail() + "#" + txnRemarks;
                    txn.setRemarks(remarks);
                }
                remarks = txn.getRemarks(); // fetch encoded value
            }
            if (txnDocumentUploadRequired != null && txnDocumentUploadRequired) {
                txn.setDocRuleStatus(1);
            }
            txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, entityManager));
            txn.setTransactionStatus("Require Approval");
            txn.setNetAmountResultDescription(txn.getNetAmountResultDescription() + netAmtInputTaxDesc);
            // list of additional users all approver role users of thet organization
            Map<String, Object> criterias = new HashMap<String, Object>(2);
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
            // list of approver user
            String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
            txn.setTransactionRefNumber(transactionNumber);
            txn.setLinkedTxnRef(txnPOInvoiceTxnRef); // BOM Txn Ref
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
            genericDAO.saveOrUpdate(txn, user, entityManager); // to set total tax values

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
                if (txn.getTransactionInvoiceDate() != null) {
                    invoiceDateLabel = "INVOICE DATE:";
                    invoiceDate = IdosConstants.idosdf.format(txn.getTransactionInvoiceDate());
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
                String[] actbudgetForBranchExpenseItemArr = { "", "" }; // now budget is stored at Transaction_item
                                                                        // level
                // actbudgetForBranchExpenseItemArr=actbudgetForBranchExpenseItem.split(":");

                String[] budgetForBranchExpenseItemArr = { "", "" };
                // budgetForBranchExpenseItemArr=budgetForBranchExpenseItem.split(":");
                Integer typeOfSupply = txn.getTypeOfSupply() == null ? 0 : txn.getTypeOfSupply();
                String txnDocument = txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs();
                TransactionViewResponse.addActionTxn(txn.getId(), branchName, projectName, itemName,
                        itemParentName,
                        actbudgetForBranchExpenseItemArr[0], actbudgetForBranchExpenseItemArr[1],
                        budgetForBranchExpenseItemArr[0], budgetForBranchExpenseItemArr[1],
                        customerVendorName,
                        txn.getTransactionPurpose().getTransactionPurpose(),
                        IdosConstants.IDOSDF.format(txn.getTransactionDate()), invoiceDateLabel,
                        invoiceDate,
                        paymentMode, txn.getNoOfUnits(), txn.getPricePerUnit(), txn.getGrossAmount(),
                        txn.getNetAmount(), txnResultDesc, "", txn.getTransactionStatus(),
                        txn.getCreatedBy().getEmail(), approverLabel, approverEmail, txnDocument,
                        remarks, debitCredit,
                        approverEmails, additionalApprovarUsers, selectedAdditionalApproval,
                        txnSpecialStatus, txn.getFrieghtCharges(), "", "", "",
                        txn.getTransactionPurpose().getId(), "",
                        "", 0, txn.getTransactionRefNumber(), 0l, 0.0, 0, typeOfSupply, result);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Sell on Cash- submit for approval", ex.getMessage());
        }
        return txn;
    }

    @Override
    public boolean getAdvanceDiscount(Users user, EntityManager entityManager, JsonNode json, ObjectNode result)
            throws IDOSException {
        return BUY_TRANSACTION_DAO.getAdvanceDiscount(user, entityManager, json, result);
    }

    public void saveUpdateBudget4Items(Transaction txn, Users user, EntityManager em) throws IDOSException {
        if (txn != null && user != null && em != null) {
            BUY_TRANSACTION_DAO.saveUpdateBudget4Items(txn, user, em);
        }
    }

    @Override
    public void calculateAndSaveTds(EntityManager em, Users user, Transaction txn) throws IDOSException {
        if (txn != null && user != null && em != null) {
            BUY_TRANSACTION_DAO.calculateAndSaveTds(em, user, txn);
        }
    }

    @Override
    public boolean setTxnPaymentDetail(Users user, EntityManager em, Transaction txn, int paymentMode, long paymentBank,
            String txnInstrumentNumber, String txnInstrumentDate, ObjectNode results) throws IDOSException {
        txn.setReceiptDetailsType(paymentMode);
        if (IdosConstants.PAYMODE_CASH == paymentMode) {
            double resultantCash = branchCashService.updateBranchCashDetail(em, user, txn.getTransactionBranch(),
                    txn.getNetAmount(), true, txn.getTransactionDate(), results);
            if (resultantCash < 0) {
                throw new IDOSException(IdosConstants.INSUFFICIENT_BALANCE_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.INSUFFICIENT_BALANCE_EXCEPTION, "Insufficient cash balance: " + resultantCash);
            }
        } else if (IdosConstants.PAYMODE_BANK == paymentMode) {
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
                    txn.getNetAmount(), true, results, txn.getTransactionDate(), txn.getTransactionBranch());
            if (!branchBankDetailEntered) {
                return false; // since balance is in -ve don't make any changes in DB
            }
        }
        return true;
    }
}
