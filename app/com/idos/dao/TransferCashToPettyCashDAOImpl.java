package com.idos.dao;

import actor.CreatorActor;
import com.idos.util.CodeHelper;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import java.util.logging.Level;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;
import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;

/**
 * @author Sunil K. Namdev created on 07.11.2019
 */
public class TransferCashToPettyCashDAOImpl implements TransferCashToPettyCashDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public Transaction submitForApproval(Users user, JsonNode json, EntityManager em, TransactionPurpose txnPurposeObj,
            ObjectNode result) throws IDOSException {
        Map<String, Object> criterias = new HashMap<String, Object>(3);
        String txnTMTPCABranch = json.findValue("txnTMTPCABranch").asText();
        String txnTMTPCAPurposeOfTransfer = json.findValue("txnTMTPCAPurposeOfTransfer").asText();
        String txnTMTPCAAmountOfTransfer = json.findValue("txnTMTPCAAmountOfTransfer").asText();
        String supportingdoc = json.findValue("supportingdoc").asText();
        String txnremarks = json.findValue("txnremarks").asText();
        Branch branch = Branch.findById(IdosUtil.convertStringToLong(txnTMTPCABranch));
        String purpOfTransfer = "Purpose Of Amount Transfer";
        String branchName = "";
        String paymentMode = "";
        String txnRemarks = "";
        String projectName = "";
        String itemName = "";
        String customerVendorName = "";
        String debitCredit = "Credit";
        branchName = branch.getName();
        Transaction txn = new Transaction();
        if (txnPurposeObj != null) {
            txn.setTransactionPurpose(txnPurposeObj);
        } else {
            log.log(Level.SEVERE, "TransactionPurpose not found");
            txn.setTransactionPurpose(TransactionPurpose.findById(IdosConstants.TRANSFER_MAIN_CASH_TO_PETTY_CASH));
        }
        txn.setTransactionBranch(branch);
        txn.setTransactionBranchOrganization(branch.getOrganization());
        txn.setNetAmount(IdosUtil.convertStringToDouble(txnTMTPCAAmountOfTransfer));
        if (!txnTMTPCAPurposeOfTransfer.equals("") && txnTMTPCAPurposeOfTransfer != null) {
            txn.setNetAmountResultDescription(purpOfTransfer + ":" + txnTMTPCAPurposeOfTransfer);
        }
        txn.setTransactionDate(new Date());
        int txnReceiptDetails = json.findValue("txnReceiptDetails") != null
                ? json.findValue("txnReceiptDetails").asInt()
                : 0;
        String txnReceiptTypeBankDetails = json.findValue("txnReceiptTypeBankDetails").asText();
        String txnInstrumentNum = "";
        String txnInstrumentDate = "";
        BUY_TRANSACTION_SERVICE.setTxnPaymentDetail(user, em, txn, txnReceiptDetails, 0L, txnInstrumentNum,
                txnInstrumentDate, result);
        if (IdosConstants.PAYMODE_CASH == txnReceiptDetails) {
            paymentMode = "CASH";
        }
        txn.setReceiptDetailsType(txnReceiptDetails);
        if (txnReceiptTypeBankDetails != null && !"".equals(txnReceiptTypeBankDetails)) {
            txn.setReceiptDetailsDescription(txnReceiptTypeBankDetails);
        }

        if (!txnremarks.equals("") && txnremarks != null) {
            txnRemarks = user.getEmail() + "#" + txnremarks;
            txn.setRemarks(txnRemarks);
            txnRemarks = txn.getRemarks();
        }
        txn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(txn.getSupportingDocs(), user.getEmail(),
                supportingdoc, user, em));
        txn.setTransactionStatus("Require Approval");
        // list of additional users all approver role users of thet organization
        criterias.put("role.name", "APPROVER");
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);
        String approverEmails = "";
        String additionalApprovarUsers = "";
        String selectedAdditionalApproval = "";
        for (UsersRoles usrRoles : approverRole) {
            additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
            // check for right in chart of accounts
            criterias.clear();
            criterias.put("user.id", usrRoles.getUser().getId());
            criterias.put("userRights.id", 2L);
            criterias.put("branch.id", branch.getId());
            criterias.put("presentStatus", 1);
            UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class, criterias, em);
            if (userHasRightInBranch != null) {
                approverEmails += usrRoles.getUser().getEmail() + ",";
            }
        }
        txn.setApproverEmails(approverEmails);
        txn.setAdditionalApproverEmails(additionalApprovarUsers);
        // list of approver user
        String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
        txn.setTransactionRefNumber(transactionNumber);
        txn.setTypeOfSupply(0);
        genericDao.saveOrUpdate(txn, user, em);
        FILE_UPLOAD_SERVICE.updateUploadFileLogs(em, user, supportingdoc, txn.getId(), IdosConstants.MAIN_TXN_TYPE);

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
            if (txn.getTransactionSpecifics() != null) {
                if (txn.getTransactionSpecifics().getParentSpecifics() != null
                        && !txn.getTransactionSpecifics().getParentSpecifics().equals("")) {
                    itemParentName = txn.getTransactionSpecifics().getParentSpecifics().getName();
                } else {
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
            if (txn.getNetAmountResultDescription() != null && !txn.getNetAmountResultDescription().equals("null")) {
                txnResultDesc = txn.getNetAmountResultDescription();
            }
            Integer typeOfSupply = txn.getTypeOfSupply() == null ? 0 : txn.getTypeOfSupply();
            String txnDocument = txn.getSupportingDocs() == null ? "" : txn.getSupportingDocs();
            TransactionViewResponse.addActionTxn(txn.getId(), branchName, projectName,
                    itemName,
                    itemParentName, "", "", "", "",
                    customerVendorName, txnPurposeObj.getId().toString(),
                    IdosConstants.IDOSDF.format(txn.getTransactionDate()), invoiceDateLabel,
                    invoiceDate, paymentMode,
                    0.0, 0.0, 0.0, txn.getNetAmount(), txnResultDesc, "",
                    txn.getTransactionStatus(),
                    txn.getCreatedBy().getEmail(), approverLabel, approverEmail, txnDocument,
                    txnRemarks, debitCredit,
                    approverEmails, additionalApprovarUsers, selectedAdditionalApproval,
                    txnSpecialStatus, 0.0, "", txnInstrumentNum, txnInstrumentDate,
                    txn.getTransactionPurpose().getId(),
                    "", "", 0, txn.getTransactionRefNumber(), 0l, 0.0, 0, typeOfSupply, result);
        }
        return txn;
    }
}
