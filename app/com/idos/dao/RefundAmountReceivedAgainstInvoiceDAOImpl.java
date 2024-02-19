package com.idos.dao;

import actor.CreatorActor;
import akka.NotUsed;
import akka.stream.javadsl.*;
import akka.actor.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idos.util.CodeHelper;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import model.*;
import org.json.JSONArray;
import org.json.JSONObject;
import play.db.jpa.JPAApi;
import play.mvc.WebSocket;
import java.util.logging.Level;

public class RefundAmountReceivedAgainstInvoiceDAOImpl implements RefundAmountReceivedAgainstInvoiceDAO {

        private static JPAApi jpaApi;
        private static EntityManager entityManager;

        @Override
        public Transaction submitForApprovalRefundAmountRecived(
                        Users user,
                        JsonNode json,
                        EntityManager entityManager,
                        EntityTransaction entitytransaction,
                        ObjectNode result) throws IDOSException {
                Transaction transaction = null;
                Long txnforbranch = json.findValue("txnforbranch").asLong();
                Long txnEntityID = (json.findValue("txnEntityID") == null ||
                                "".equals(json.findValue("txnEntityID")))
                                                ? 0l
                                                : json.findValue("txnEntityID").asLong();
                long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
                Long txnforcustomer = json.findValue("txnforcustomer").asLong();
                String txnInvoice = json.findValue("txnInvoice").asText();
                Double txnnetAmount = json.findValue("txnnetAmount") == null
                                ? 0.0
                                : json.findValue("txnnetAmount").asDouble();
                Double txnnetTDS = json.findValue("txnnetTDS") == null
                                ? 0.0
                                : json.findValue("txnnetTDS").asDouble();
                Double netAmountTotalWithDecimalValue = json
                                .findValue("netAmountTotalWithDecimalValue")
                                .asDouble();
                String txnremarks = json.findValue("txnremarks").asText();
                String txnRemarksPrivate = json.findValue("txnRemarksPrivate").asText();
                String supportingdoc = json.findValue("supportingdoc").asText();
                String txnForItemStr = json.findValue("txnforitem").toString();
                String txnSourceGstin = json.findValue("txnSourceGstin").asText();
                String txnDestinGstin = json.findValue("txnDestinGstin").asText();
                int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null
                                ? 0
                                : json.findValue("txnTypeOfSupply").asInt();
                String txnRemarks = "";
                Branch txnBranch = null;
                Vendor txncustomer = null;
                // it holds all info for transaction
                try {
                        TransactionPurpose transactionPurpose = TransactionPurpose.findById(
                                        txnPurposeVal);
                        if (txnEntityID > 0) {
                                transaction = Transaction.findById(txnEntityID);
                        } else {
                                transaction = new Transaction();
                        }

                        JSONArray arrJSON = new JSONArray(txnForItemStr);
                        if (txnforbranch != null && !txnforbranch.equals("")) {
                                txnBranch = genericDao.getById(Branch.class, txnforbranch, entityManager);
                        }

                        if (txnforcustomer != null && !txnforcustomer.equals("")) {
                                txncustomer = genericDao.getById(Vendor.class, txnforcustomer, entityManager);
                        }

                        transaction.setSourceGstin(txnSourceGstin);
                        transaction.setDestinationGstin(txnDestinGstin);
                        transaction.setTypeOfSupply(txnTypeOfSupply);
                        transaction.setTransactionPurpose(transactionPurpose);
                        transaction.setTransactionBranch(txnBranch);
                        transaction.setTransactionBranchOrganization(txnBranch.getOrganization());
                        transaction.setTransactionVendorCustomer(txncustomer);
                        transaction.setNetAmount(txnnetAmount);
                        transaction.setTransactionDate(Calendar.getInstance().getTime());
                        transaction.setWithholdingTax(txnnetTDS);

                        int txnReceiptDetails = json.findValue("txnReceiptDetails") == null
                                        ? 0
                                        : json.findValue("txnReceiptDetails").asInt();
                        if (IdosConstants.PAYMODE_BANK == txnReceiptDetails) {
                                long txnReceiptPaymentBank = json.findValue("txnReceiptPaymentBank") != null
                                                ? json.findValue("txnReceiptPaymentBank").asLong()
                                                : 0L;
                                String txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
                                                ? json.findValue("txnInstrumentNum").asText()
                                                : "";
                                String txnInstrumentDate = json.findValue("txnInstrumentDate") != null
                                                ? json.findValue("txnInstrumentDate").asText()
                                                : "";
                                if (txnReceiptPaymentBank > 0) {
                                        BranchBankAccounts bankAccount = BranchBankAccounts.findById(
                                                        txnReceiptPaymentBank);
                                        if (bankAccount == null) {
                                                throw new IDOSException(
                                                                IdosConstants.INVALID_DATA_ERRCODE,
                                                                IdosConstants.BUSINESS_EXCEPTION,
                                                                IdosConstants.INVALID_DATA_EXCEPTION,
                                                                "Bank is not selected in transaction when payment mode is Bank.");
                                        }
                                        transaction.setTransactionBranchBankAccount(bankAccount);
                                        if (txnInstrumentNumber != null && !"".equals(txnInstrumentNumber)) {
                                                transaction.setInstrumentNumber(txnInstrumentNumber);
                                        }
                                        if (txnInstrumentDate != null && !"".equals(txnInstrumentDate)) {
                                                transaction.setInstrumentDate(txnInstrumentDate);
                                        }
                                }
                        }
                        transaction.setReceiptDetailsType(txnReceiptDetails);
                        String txnReceiptDescription = json.findValue("txnReceiptDescription") != null
                                        ? json.findValue("txnReceiptDescription").asText()
                                        : null;
                        transaction.setReceiptDetailsDescription(txnReceiptDescription);

                        if (txnremarks != null && !txnremarks.equals("")) {
                                if (transaction.getRemarks() != null) {
                                        transaction.setRemarks(txnRemarks);
                                } else {
                                        txnRemarks = user.getEmail() + "#" + txnremarks;
                                        transaction.setRemarks(txnRemarks);
                                }
                                txnRemarks = transaction.getRemarks(); // fetch encoded value
                        }

                        if (txnRemarksPrivate != null && !txnRemarksPrivate.equals("")) {
                                if (transaction.getRemarksPrivate() != null) {
                                        txnRemarksPrivate = transaction.getRemarksPrivate() +
                                                        "," +
                                                        user.getEmail() +
                                                        "#" +
                                                        txnRemarksPrivate;
                                        transaction.setRemarksPrivate(txnRemarksPrivate);
                                } else {
                                        txnRemarksPrivate = user.getEmail() + "#" + txnRemarksPrivate;
                                        transaction.setRemarksPrivate(txnRemarksPrivate);
                                }
                                txnRemarksPrivate = transaction.getRemarksPrivate(); // fetch encoded value
                        }
                        transaction.setSupportingDocs(
                                        TRANSACTION_DAO.getAndDeleteSupportingDocument(
                                                        transaction.getSupportingDocs(),
                                                        user.getEmail(),
                                                        supportingdoc,
                                                        user,
                                                        entityManager));
                        transaction.setTransactionStatus("Require Approval");

                        // list of additional users all approver role users of thet organization
                        Map<String, Object> criterias = new HashMap<String, Object>();
                        criterias.put("role.name", "APPROVER");
                        criterias.put("organization.id", user.getOrganization().getId());
                        criterias.put("presentStatus", 1);
                        List<UsersRoles> approverRole = genericDao.findByCriteria(
                                        UsersRoles.class,
                                        criterias,
                                        entityManager);
                        String approverEmails = "";
                        String additionalApprovarUsers = "";
                        // String selectedAdditionalApproval = "";
                        for (UsersRoles usrRoles : approverRole) {
                                additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
                                criterias.clear();
                                criterias.put("user.id", usrRoles.getUser().getId());
                                criterias.put("userRights.id", 2L);
                                criterias.put("branch.id", txnBranch.getId());
                                criterias.put("presentStatus", 1);
                                UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(
                                                UserRightInBranch.class,
                                                criterias,
                                                entityManager);
                                if (userHasRightInBranch != null) {
                                        approverEmails += usrRoles.getUser().getEmail() + ",";
                                }
                        }
                        transaction.setApproverEmails(approverEmails);
                        transaction.setAdditionalApproverEmails(additionalApprovarUsers);

                        String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
                        transaction.setTransactionRefNumber(transactionNumber);

                        Transaction pendingTransaction = Transaction.findById(
                                        IdosUtil.convertStringToLong(txnInvoice));
                        if (pendingTransaction != null) {
                                transaction.setTransactionSpecifics(
                                                pendingTransaction.getTransactionSpecifics());
                                transaction.setTransactionParticulars(
                                                pendingTransaction.getTransactionParticulars());
                                // transaction.setPaidInvoiceRefNumber(pendingTransaction.getInvoiceNumber());
                                transaction.setPaidInvoiceRefNumber(
                                                pendingTransaction.getTransactionRefNumber());
                                transaction.setLinkedTxnRef(
                                                pendingTransaction.getTransactionRefNumber());
                        } else {
                                throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE,
                                                IdosConstants.TECHNICAL_EXCEPTION,
                                                "Error on submit for approval, on Refund against invoice. " +
                                                                txnInvoice +
                                                                " is not found",
                                                txnInvoice + " is not found");
                        }

                        genericDao.saveOrUpdate(transaction, user, entityManager);
                        FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, supportingdoc, transaction.getId(),
                                        IdosConstants.MAIN_TXN_TYPE);
                        if (txnEntityID > 0) {
                                insertMultipleItemsRefundAmountReceived(entityManager, user, arrJSON, transaction);
                        } else {
                                insertMultipleItemsRefundAmountReceived(entityManager, user, arrJSON, transaction);
                        }
                        entitytransaction.commit();
                        if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                                // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
                                // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
                                // for (int i = 0; i < keyArray.length; i++) {
                                // StringBuilder sbquery = new StringBuilder("");
                                // sbquery.append(
                                // "select obj from Users obj WHERE obj.email ='" +
                                // keyArray[i] +
                                // "' and obj.presentStatus=1");
                                // List<Users> orgusers = genericDao.executeSimpleQuery(
                                // sbquery.toString(),
                                // entityManager);
                                // if (!orgusers.isEmpty() &&
                                // orgusers.get(0).getOrganization().getId() == user
                                // .getOrganization().getId()) {
                                // orgtxnregistereduser.put(
                                // keyArray[i].toString(),
                                // CreatorActor.expenseregistrered.get(keyArray[i]));
                                // }
                                // }
                        }
                        if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                                TRANSACTION_SERVICE.sendStockWebSocketResponse(entityManager, transaction, user,
                                                result);
                        }
                        // SingleUser
                        if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                                ArrayNode singleUserAccounting = result.putArray(
                                                "singleUserAccounting");
                                ObjectNode createSingleuserJson = SINGLE_USER_DAO.createSingleuserJson(
                                                transaction,
                                                json,
                                                user);
                                singleUserAccounting.add(createSingleuserJson);
                        }
                } catch (Exception ex) {
                        log.log(Level.SEVERE, "Error", ex);
                        if ((ex instanceof IDOSException) == false) {
                                throw new IDOSException(
                                                IdosConstants.UNKNOWN_EXCEPTION_ERRCODE,
                                                IdosConstants.TECHNICAL_EXCEPTION,
                                                "Error on submit for approval, on Refund against invoice.",
                                                ex.getMessage());
                        } else {
                                throw (IDOSException) ex;
                        }
                }
                return transaction;
        }

        public void insertMultipleItemsRefundAmountReceived(
                        EntityManager entityManager,
                        Users user,
                        JSONArray arrJSON,
                        Transaction transaction) throws IDOSException {
                log.log(Level.INFO, "inside multi items save");
                try {
                        for (int i = 0; i < arrJSON.length(); i++) {
                                Double amtAvailForRefund = 0.0;
                                Double tdsAvailForRefund = 0.0;
                                Double amountToRefund = 0.0;
                                Double taxToRefund = 0.0;
                                Double resultantAmt = 0.0;
                                Double resultantTax = 0.0;
                                JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                                TransactionItems transactionItem = new TransactionItems();
                                Long itemId = rowItemData.getLong("txnItems");
                                log.log(Level.INFO, "item id=" + itemId);
                                Transaction txnItem = genericDao.getById(
                                                Transaction.class,
                                                itemId,
                                                entityManager);
                                // Double txnGross=rowItemData.getDouble("txnGross");
                                /*
                                 * if(!rowItemData.isNull("txnTaxDesc") &&
                                 * !rowItemData.get("txnTaxDesc").equals("")){
                                 * txnTaxDesc=rowItemData.getString("txnTaxDesc");
                                 * }
                                 */
                                // transactionItem.setTaxDescription(txnTaxDesc);
                                Map<String, Object> criterias = new HashMap<String, Object>();
                                criterias.clear();
                                criterias.put("id", itemId);
                                criterias.put(
                                                "transactionBranchOrganization.id",
                                                user.getOrganization().getId());
                                criterias.put("presentStatus", 1);
                                Transaction receievPaymentTransaction = genericDao.getByCriteria(
                                                Transaction.class,
                                                criterias,
                                                entityManager);

                                if (!rowItemData.isNull("amtAvailForRefund") &&
                                                !rowItemData.get("amtAvailForRefund").equals("")) {
                                        amtAvailForRefund = rowItemData.getDouble("amtAvailForRefund");
                                }
                                if (!rowItemData.isNull("tdsAvailForRefund") &&
                                                !rowItemData.get("tdsAvailForRefund").equals("")) {
                                        tdsAvailForRefund = rowItemData.getDouble("tdsAvailForRefund");
                                }
                                if (!rowItemData.isNull("amountToRefund") &&
                                                !rowItemData.get("amountToRefund").equals("")) {
                                        amountToRefund = rowItemData.getDouble("amountToRefund");
                                }
                                if (!rowItemData.isNull("taxToRefund") &&
                                                !rowItemData.get("taxToRefund").equals("")) {
                                        taxToRefund = rowItemData.getDouble("taxToRefund");
                                }
                                if (!rowItemData.isNull("resultantAmt") &&
                                                !rowItemData.get("resultantAmt").equals("")) {
                                        resultantAmt = rowItemData.getDouble("resultantAmt");
                                }
                                if (!rowItemData.isNull("resultantTax") &&
                                                !rowItemData.get("resultantTax").equals("")) {
                                        resultantTax = rowItemData.getDouble("resultantTax");
                                }
                                // tdsAvailForRefund
                                transactionItem.setOrganization(
                                                transaction.getTransactionBranchOrganization());
                                transactionItem.setBranch(transaction.getTransactionBranch());
                                Long txnPurpose = transaction.getTransactionPurpose().getId();
                                transactionItem.setGrossAmount(amountToRefund + taxToRefund);
                                transactionItem.setGrossAmounReturned(amountToRefund + taxToRefund);
                                transactionItem.setTransactionId(transaction);
                                // transactionItem.setTransactionSpecifics(txnItem);
                                transactionItem.setTransactionRefNumber(
                                                receievPaymentTransaction.getTransactionRefNumber());
                                transactionItem.setWithholdingAmount(taxToRefund);
                                transactionItem.setWithholdingAmountReturned(taxToRefund);
                                // transaction.setWithholdingTax
                                transactionItem.setNetAmountReturned(amountToRefund);
                                transactionItem.setNetAmount(amountToRefund);
                                genericDao.saveOrUpdate(transactionItem, user, entityManager);
                        }
                } catch (Exception ex) {
                        log.log(Level.SEVERE, "Error", ex);
                        throw new IDOSException(
                                        IdosConstants.UNKNOWN_EXCEPTION_ERRCODE,
                                        IdosConstants.TECHNICAL_EXCEPTION,
                                        "Error on save/update multiitems.",
                                        ex.getMessage());
                }
        }
}
