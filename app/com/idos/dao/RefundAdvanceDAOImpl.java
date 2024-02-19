package com.idos.dao;

import java.text.ParseException;
import java.util.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Branch;
import model.BranchBankAccounts;
import model.BranchVendors;
import model.ConfigParams;
import model.CustomerBranchWiseAdvBalance;
// import model.CustomerBranchWiseAdvBalance;
import model.Specifics;
import model.Transaction;
import model.TransactionItems;
import model.TransactionPurpose;
import model.UserRightInBranch;
import model.UserRightSpecifics;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import model.VendorBillwiseOpBalance;
import model.VendorSpecific;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import com.idos.util.CodeHelper;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;

public class RefundAdvanceDAOImpl implements RefundAdvanceDAO {

    @Override
    public Transaction submitForApprovalRefundAdvanceRecived(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
        Transaction transaction = null;
        Long txnforbranch = json.findValue("txnforbranch").asLong();
        Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                : json.findValue("txnEntityID").asLong();
        /*
         * Long openingBalAdvId = (json.findValue("openingBalAdvId") == null
         * || "".equals(json.findValue("openingBalAdvId")))
         * ? 0l
         * : json.findValue("openingBalAdvId").asLong();
         */
        long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
        Long txnforcustomer = json.findValue("txnforcustomer").asLong();
        String txnInvoice = json.findValue("txnInvoice").asText();
        Double txnnetAdvance = json.findValue("txnnetAdvance") == null ? 0.0
                : json.findValue("txnnetAdvance").asDouble();
        Double txnnetTDS = json.findValue("txnnetTDS") == null ? 0.0 : json.findValue("txnnetTDS").asDouble();
        Double netAmountTotalWithDecimalValue = json.findValue("netAmountTotalWithDecimalValue").asDouble();
        String txnremarks = json.findValue("txnremarks").asText();
        String txnRemarksPrivate = json.findValue("txnRemarksPrivate").asText();
        String supportingdoc = json.findValue("supportingdoc").asText();
        String txnForItemStr = json.findValue("txnforitem").toString();
        String txnSourceGstin = json.findValue("txnSourceGstin").asText();
        String txnDestinGstin = json.findValue("txnDestinGstin").asText();
        int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0 : json.findValue("txnTypeOfSupply").asInt();
        String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
        Integer txnWithWithoutTax = json.findValue("txnWithWithoutTax") == null ? null
                : json.findValue("txnWithWithoutTax").asInt();
        String digitalSignatureContent = json.findValue("digitalSignatureContent") == null ? null
                : json.findValue("digitalSignatureContent").asText();
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
        String txnInstrumentNumber = "";
        String txnInstrumentDate = "";
        String txnDocument = "";
        String txnRemarks = "";
        Branch txnBranch = null;
        Vendor txncustomer = null;
        // it holds all info for transaction
        try {
            TransactionPurpose transactionPurpose = TransactionPurpose.findById(txnPurposeVal);

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

            // Enter data for first item in transaction table to be displayed in Transaction
            // list
            JSONObject firstRowItemData = new JSONObject(arrJSON.get(0).toString());
            Long itemIdRow0 = firstRowItemData.getLong("txnItems");
            Specifics txnSpecificItem = genericDao.getById(Specifics.class, itemIdRow0, entityManager);
            Double txnGrossRow0 = firstRowItemData.getDouble("txnGross");
            /*
             * if (txnInvoice.equals("-1")) {
             * if (openingBalAdvId > 0) {
             * CustomerBranchWiseAdvBalance openingAdvBalanceBranchTrans =
             * CustomerBranchWiseAdvBalance
             * .findById(openingBalAdvId);
             * transaction.setPaidInvoiceRefNumber(openingAdvBalanceBranchTrans.getId().
             * toString());
             * transaction.setLinkedTxnRef(openingAdvBalanceBranchTrans.getId().toString());
             * transaction.setTypeIdentifier(IdosConstants.
             * TXN_TYPE_OPENING_BALANCE_ADV_PAID_BRACHWISE_CUST);
             * 
             * } else {
             * // BranchVendors branchHasVendor =
             * // BranchVendors.findByVendorBranch(entityManager,
             * // user.getOrganization().getId(), txnBranch.getId(), txncustomer.getId());
             * // transaction.setPaidInvoiceRefNumber(branchHasVendor.getId().toString());
             * //
             * // For Vendor Opening
             * // // balance
             * // // transaction
             * // transaction.setLinkedTxnRef(branchHasVendor.getId().toString());
             * // transaction.setTypeIdentifier(IdosConstants.
             * TXN_TYPE_OPENING_BALANCE_ADV_PAID_CUST);
             * }
             * } else {
             * transaction.setTypeIdentifier(IdosConstants.TXN_TYPE_OTHER_TRANSACTIONS_CUST)
             * ;
             * if (txnInvoice != null && !txnInvoice.equals("")) {
             * Transaction trans =
             * Transaction.findById(IdosUtil.convertStringToLong(txnInvoice));
             * if (trans != null) {
             * transaction.setLinkedTxnRef(trans.getTransactionRefNumber());
             * }
             * }
             * }
             */
            transaction.setTransactionSpecifics(txnSpecificItem);
            transaction.setTransactionParticulars(txnSpecificItem.getParticularsId());
            transaction.setGrossAmount(txnnetAdvance + txnnetTDS);
            transaction.setWithholdingTax(txnnetTDS);
            transaction.setSourceGstin(txnSourceGstin);
            transaction.setDestinationGstin(txnDestinGstin);
            transaction.setTypeOfSupply(txnTypeOfSupply);
            transaction.setTransactionPurpose(transactionPurpose);
            transaction.setTransactionBranch(txnBranch);
            transaction.setTransactionBranchOrganization(txnBranch.getOrganization());
            transaction.setTransactionVendorCustomer(txncustomer);
            transaction.setNetAmount(txnnetAdvance);
            transaction.setWithWithoutTax(txnWithWithoutTax);
            if (txnInvoice != null && !txnInvoice.equals("")) {
                Transaction trans = Transaction.findById(IdosUtil.convertStringToLong(txnInvoice));
                if (trans != null) {
                    transaction.setLinkedTxnRef(trans.getTransactionRefNumber());
                }
            }
            transaction.setTransactionDate(new Date());
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
                    txnRemarksPrivate = transaction.getRemarksPrivate() + "," + user.getEmail() + "#"
                            + txnRemarksPrivate;
                    transaction.setRemarksPrivate(txnRemarksPrivate);
                } else {
                    txnRemarksPrivate = user.getEmail() + "#" + txnRemarksPrivate;
                    transaction.setRemarksPrivate(txnRemarksPrivate);
                }
                txnRemarksPrivate = transaction.getRemarksPrivate(); // fetch encoded value
            }
            transaction.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(
                    transaction.getSupportingDocs(), user.getEmail(), supportingdoc, user, entityManager));
            transaction.setTransactionStatus("Require Approval");

            // list of additional users all approver role users of thet organization
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("role.name", "APPROVER");
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("presentStatus", 1);
            List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
            String approverEmails = "";
            String additionalApprovarUsers = "";
            String selectedAdditionalApproval = "";
            for (UsersRoles usrRoles : approverRole) {
                additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
                criterias.clear();
                criterias.put("user.id", usrRoles.getUser().getId());
                criterias.put("userRights.id", 2L);
                criterias.put("branch.id", txnBranch.getId());
                criterias.put("presentStatus", 1);
                UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class, criterias,
                        entityManager);
                if (userHasRightInBranch != null) {
                    // check for right in chart of accounts
                    criterias.clear();
                    criterias.put("user.id", usrRoles.getUser().getId());
                    criterias.put("userRights.id", 2L);
                    criterias.put("specifics.id", txnSpecificItem.getId());
                    criterias.put("presentStatus", 1);
                    UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class, criterias,
                            entityManager);
                    if (userHasRightInCOA != null) {
                        approverEmails += usrRoles.getUser().getEmail() + ",";
                    }
                }
            }
            transaction.setApproverEmails(approverEmails);
            transaction.setAdditionalApproverEmails(additionalApprovarUsers);
            String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
            transaction.setTransactionRefNumber(transactionNumber);
            genericDao.saveOrUpdate(transaction, user, entityManager);
            FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, supportingdoc, transaction.getId(),
                    IdosConstants.MAIN_TXN_TYPE);

            int txnReceiptDetails = json.findValue("txnReceiptDetails") == null ? 0
                    : json.findValue("txnReceiptDetails").asInt();
            if (IdosConstants.PAYMODE_BANK == txnReceiptDetails) {
                long txnReceiptPaymentBank = json.findValue("txnReceiptPaymentBank") != null
                        ? json.findValue("txnReceiptPaymentBank").asLong()
                        : 0L;
                txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
                        ? json.findValue("txnInstrumentNum").asText()
                        : "";
                txnInstrumentDate = json.findValue("txnInstrumentDate") != null
                        ? json.findValue("txnInstrumentDate").asText()
                        : "";
                if (txnReceiptPaymentBank > 0) {
                    BranchBankAccounts bankAccount = BranchBankAccounts.findById(txnReceiptPaymentBank);
                    if (bankAccount == null) {
                        throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
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
            // Enter multiple items data into TransactionItems table
            if (txnEntityID > 0) {
                insertMultipleItemsRefundAdvanceReceived(entityManager, user, arrJSON, transaction, txnDate);
            } else {
                insertMultipleItemsRefundAdvanceReceived(entityManager, user, arrJSON, transaction, txnDate);
            }
            genericDao.saveOrUpdate(transaction, user, entityManager);
            entitytransaction.commit();
            if (!ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                TRANSACTION_SERVICE.sendStockWebSocketResponse(entityManager, transaction, user, result);
            }
            // SingleUser
            if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
                ArrayNode singleUserAccounting = result.putArray("singleUserAccounting");
                ObjectNode createSingleuserJson = SINGLE_USER_DAO.createSingleuserJson(transaction, json, user);
                singleUserAccounting.add(createSingleuserJson);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Refund Advance Received", ex.getMessage());
        }
        return transaction;
    }

    @Override
    public void insertMultipleItemsRefundAdvanceReceived(EntityManager entityManager, Users user, JSONArray arrJSON,
            Transaction transaction, Date txnDate) throws IDOSException {
        try {
            for (int i = 0; i < arrJSON.length(); i++) {
                Double howMuchAdvance = 0.0;
                Double customerAdvance = 0.0;
                String txnTaxDesc = "";
                Double withholdingAmount = 0.0;
                Double returnWithholdingAmount = 0.0;
                JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                TransactionItems transactionItem = new TransactionItems();
                Long itemId = rowItemData.getLong("txnItems");
                Specifics txnItem = genericDao.getById(Specifics.class, itemId, entityManager);
                Double txnGross = rowItemData.getDouble("txnGross");
                if (!rowItemData.isNull("txnTaxDesc") && !rowItemData.get("txnTaxDesc").equals("")) {
                    txnTaxDesc = rowItemData.getString("txnTaxDesc");
                }
                transactionItem.setTaxDescription(txnTaxDesc);
                transactionItem.setOrganization(transaction.getTransactionBranchOrganization());
                transactionItem.setBranch(transaction.getTransactionBranch());
                Long txnPurpose = transaction.getTransactionPurpose().getId();
                if (user.getOrganization().getGstCountryCode() != null
                        && !"".equals(user.getOrganization().getGstCountryCode())) {
                    transactionItemsService.saveTransactionTaxes(transactionItem, rowItemData, transaction);
                }
                String txnTaxNameOnAdvAdj = "";
                if (!rowItemData.isNull("txnTaxNameOnAdvAdj") && !rowItemData.get("txnTaxNameOnAdvAdj").equals("")) {
                    txnTaxNameOnAdvAdj = rowItemData.getString("txnTaxNameOnAdvAdj");
                }
                Double txnTaxOnAdvAdj = 0d;
                if (!rowItemData.isNull("txnTaxOnAdvAdj") && !rowItemData.get("txnTaxOnAdvAdj").equals("")) {
                    txnTaxOnAdvAdj = rowItemData.getDouble("txnTaxOnAdvAdj");
                }
                if (transactionItem.getTaxName1() != null
                        && transactionItem.getTaxName1().indexOf(txnTaxNameOnAdvAdj) != -1) {
                    transactionItem.setAdvAdjTax1Value(txnTaxOnAdvAdj);
                } else if (transactionItem.getTaxName2() != null
                        && transactionItem.getTaxName2().indexOf(txnTaxNameOnAdvAdj) != -1) {
                    transactionItem.setAdvAdjTax2Value(txnTaxOnAdvAdj);
                } else if (transactionItem.getTaxName3() != null
                        && transactionItem.getTaxName3().indexOf(txnTaxNameOnAdvAdj) != -1) {
                    transactionItem.setAdvAdjTax3Value(txnTaxOnAdvAdj);
                } else if (transactionItem.getTaxName4() != null
                        && transactionItem.getTaxName4().indexOf(txnTaxNameOnAdvAdj) != -1) {
                    transactionItem.setAdvAdjTax4Value(txnTaxOnAdvAdj);
                } else if (transactionItem.getTaxName5() != null
                        && transactionItem.getTaxName5().indexOf(txnTaxNameOnAdvAdj) != -1) {
                    transactionItem.setAdvAdjTax5Value(txnTaxOnAdvAdj);
                }
                /*
                 * if(!rowItemData.isNull("txnTaxAmount") &&
                 * !rowItemData.get("txnTaxAmount").equals("")){
                 * txnTaxAmount=rowItemData.getDouble("txnTaxAmount");
                 * }
                 */

                transactionItemsService.saveTransactionTaxes(transactionItem, rowItemData, transaction);

                if (!rowItemData.isNull("ResultantTax") && !rowItemData.get("ResultantTax").equals("")) {
                    withholdingAmount = rowItemData.getDouble("ResultantTax");
                }
                if (!rowItemData.isNull("advAvailForRefund") && !rowItemData.get("advAvailForRefund").equals("")) {
                    customerAdvance = rowItemData.getDouble("advAvailForRefund");
                }
                if (!rowItemData.isNull("advanceReceived") && !rowItemData.get("advanceReceived").equals("")) {
                    howMuchAdvance = rowItemData.getDouble("advanceReceived");
                }
                if (!rowItemData.isNull("taxAdjusted") && !rowItemData.get("taxAdjusted").equals("")) {
                    returnWithholdingAmount = rowItemData.getDouble("taxAdjusted");
                }
                // transactionItem.setTransactionId(transaction.getId());
                transactionItem.setTransactionId(transaction);
                transactionItem.setTransactionSpecifics(txnItem);
                transactionItem.setTransactionParticulars(txnItem.getParticularsId());
                transactionItem.setGrossAmount(howMuchAdvance + returnWithholdingAmount);
                transactionItem.setWithholdingAmount(returnWithholdingAmount);
                transactionItem.setAvailableAdvance(customerAdvance);
                transactionItem.setAdjustmentFromAdvance(howMuchAdvance);
                // transactionItem.setWithholdingAmountReturned(returnWithholdingAmount);
                transactionItem.setNetAmount(howMuchAdvance);
                genericDao.saveOrUpdate(transactionItem, user, entityManager);
                // advance adjustment
                Map<String, Object> criterias = new HashMap<String, Object>();
                if (howMuchAdvance != 0.0) {
                    // if (txnInvoice.equals("-1")) {
                    // if (openingBalAdvId > 0) {
                    // CustomerBranchWiseAdvBalance openingAdvBalanceBranchTrans =
                    // CustomerBranchWiseAdvBalance
                    // .findById(openingBalAdvId);
                    // openingAdvBalanceBranchTrans
                    // .setAdvanceAmount(openingAdvBalanceBranchTrans.getAdvanceAmount() -
                    // howMuchAdvance);
                    // genericDao.saveOrUpdate(openingAdvBalanceBranchTrans, user, entityManager);
                    // }

                    // } else {
                    criterias.clear();
                    criterias.put("vendorSpecific.id", transaction.getTransactionVendorCustomer().getId());
                    criterias.put("specificsVendors.id", txnItem.getId());
                    criterias.put("organization.id", user.getOrganization().getId());
                    criterias.put("presentStatus", 1);
                    VendorSpecific customerTxnSpecifics = genericDao.getByCriteria(VendorSpecific.class, criterias,
                            entityManager);
                    customerTxnSpecifics.setAdvanceMoney(customerTxnSpecifics.getAdvanceMoney() - howMuchAdvance);
                    genericDao.saveOrUpdate(customerTxnSpecifics, user, entityManager);
                    // }

                    SELL_TRANSACTION_DAO.saveAdvanceAdjustmentDetail(user, entityManager, txnItem, transactionItem,
                            transaction, howMuchAdvance, returnWithholdingAmount, txnDate);
                }

            }
        } catch (

        Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save/update multiitems.", ex.getMessage());
        }
    }
}
