package com.idos.dao;

import java.util.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.inject.Inject;
import play.db.jpa.JPAApi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import com.idos.util.CodeHelper;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import java.util.logging.Level;
import actor.CreatorActor;
import controllers.Karvy.KarvyAuthorization;
import model.Branch;
import model.BranchBankAccounts;
import model.Specifics;
import model.Transaction;
import model.TransactionPurpose;
import model.Users;
import model.Vendor;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;

/**
 * Created by Sunil Namdev on 25-07-2017.
 */
public class ReceiveFromCustomerDAOImpl implements ReceiveFromCustomerDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public Transaction submitForAccounting(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
        Transaction transaction = null;
        try {
            long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
            TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);
            Map<String, Object> criterias = new HashMap<String, Object>();
            long txnforbranch = json.findValue("txnforbranch").asLong();
            // String txnRCAFCCIncomeItem = json.findValue("txnRCAFCCIncomeItem").asText();
            long txnRCAFCCCreditCustomer = json.findValue("txnRCAFCCCreditCustomer") != null
                    ? json.findValue("txnRCAFCCCreditCustomer").asLong()
                    : 0;
            String txnforunavailablecustomer = json.findValue("txnforunavailablecustomer") != null
                    ? json.findValue("txnforunavailablecustomer").asText()
                    : null;
            // String txnRCAFCCPurposeOfAdvance =
            // json.findValue("txnRCAFCCPurposeOfAdvance").asText();
            // Double txnRCAFCCAmountOfAdvance = json.findValue("txnRCAFCCAmountOfAdvance")
            // == null ? 0.0 : json.findValue("txnRCAFCCAmountOfAdvance").asDouble();
            int txnreceiptdetails = json.findValue("txnReceiptDetails") == null ? 0
                    : json.findValue("txnReceiptDetails").asInt();
            String txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
                    ? json.findValue("txnInstrumentNum").asText()
                    : "";
            String txnInstrumentDate = json.findValue("txnInstrumentDate") != null
                    ? json.findValue("txnInstrumentDate").asText()
                    : "";
            String txnreceipttypebankdetails = json.findValue("txnReceiptDescription").asText();
            String supportingdoc = json.findValue("supportingdoc").asText();
            String txnremarks = json.findValue("txnremarks").asText();
            String txnSourceGstin = json.findValue("txnSourceGstin").asText();
            String txnDestinGstin = json.findValue("txnDestinGstin").asText();
            int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0
                    : json.findValue("txnTypeOfSupply").asInt();
            Integer txnWithWithoutTax = json.findValue("txnWithWithoutTax") == null ? null
                    : json.findValue("txnWithWithoutTax").asInt();
            int txnWalkinCustomerType = json.findValue("txnWalkinCustomerType") == null ? 0
                    : json.findValue("txnWalkinCustomerType").asInt();
            Vendor vend = null;
            if (txnforunavailablecustomer != null && !"".equals(txnforunavailablecustomer)) {
                txnforunavailablecustomer = txnforunavailablecustomer.toUpperCase();
                vend = CUSTOMER_DAO.saveCustomer(json, user, entityManager, entitytransaction,
                        IdosConstants.WALK_IN_CUSTOMER, txnWalkinCustomerType);
            } else {
                vend = Vendor.findById(txnRCAFCCCreditCustomer);
            }

            if (vend == null) {
                throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                        "Customer is missing", IdosConstants.RECORD_NOT_FOUND);
            }
            // start with new transaction
            transaction = new Transaction();
            Branch txnBranch = Branch.findById(txnforbranch);

            // Enter data for first item in transaction table to be displayed in Transaction
            // list
            String txnForItemStr = json.findValue("txnforitem").toString();
            JSONArray arrJSON = new JSONArray(txnForItemStr);
            if (arrJSON != null && arrJSON.length() > 0) {
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
                transaction.setWithWithoutTax(txnWithWithoutTax);
                transaction.setWalkinCustomerType(txnWalkinCustomerType);
                transaction.setTransactionPurpose(usertxnPurpose);
                transaction.setTransactionBranch(txnBranch);
                transaction.setTransactionBranchOrganization(txnBranch.getOrganization());
                transaction.setTransactionVendorCustomer(vend);
                transaction.setTransactionUnavailableVendorCustomer(txnforunavailablecustomer);
                transaction.setTransactionDate(new Date());
                transaction.setReceiptDetailsType(txnreceiptdetails);
                transaction.setReceiptDetailsDescription(txnreceipttypebankdetails);
                genericDao.saveOrUpdate(transaction, user, entityManager);
            }
            Long tdsRecSpecificID = TRANSACTION_ITEMS_SERVICE.saveMultiItemsTransRecAdvCust(entityManager, user, json,
                    transaction, vend, result);
            // mapping is not done "is this where you classify TDS Receivable from
            // customer", so don't save anything and quit
            if (tdsRecSpecificID == null || tdsRecSpecificID == 0) {
                return transaction;
            }
            if (IdosConstants.PAYMODE_CASH == txnreceiptdetails) {
                BRANCH_CASH_SERVICE.updateBranchCashDetail(entityManager, user, txnBranch,
                        transaction.getCustomerNetPayment(), false, transaction.getTransactionDate(), result);
            } else if (IdosConstants.PAYMODE_BANK == txnreceiptdetails) {
                String txnreceiptPaymentBank = json.findValue("txnReceiptPaymentBank") != null
                        ? json.findValue("txnReceiptPaymentBank").asText()
                        : null;
                if (txnreceiptPaymentBank != null && !txnreceiptPaymentBank.equals("")) {
                    Double creditAmount = null;
                    Double debitAmount = null;
                    Double resultantAmount = null;
                    Double amountBalance = null;
                    BranchBankAccounts bankAccount = BranchBankAccounts
                            .findById(IdosUtil.convertStringToLong(txnreceiptPaymentBank));
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
                    boolean branchBankDetailEntered = BRANCH_BANK_SERVICE.updateBranchBankDetailTransaction(
                            entityManager, user, bankAccount, transaction.getCustomerNetPayment(), false, result,
                            transaction.getTransactionDate(), transaction.getTransactionBranch());
                    if (!branchBankDetailEntered) {
                        return transaction; // since balance is in -ve don't make any changes in DB
                    }
                    // BRANCH_BANK_SERVICE.updateBranchBankDetail(entityManager, genericDao, user,
                    // bankAccount, transaction.getCustomerNetPayment(), false);
                }
            }
            transaction.setPaymentStatus("PAID");
            transaction.setTransactionPurpose(usertxnPurpose);
            String projectName = "";
            transaction.setTransactionVendorCustomer(vend);
            transaction.setTransactionUnavailableVendorCustomer(txnforunavailablecustomer);

            String netDesc = "Advance Received For Purpose: " + transaction.getCustomerNetPayment()
                    + ",Withholding Adjustment: " + transaction.getWithholdingTax();
            transaction.setNetAmountResultDescription(netDesc);
            transaction.setTransactionDate(Calendar.getInstance().getTime());
            transaction.setReceiptDetailsType(txnreceiptdetails);
            transaction.setCustomerDuePayment(0.0);
            transaction.setTransactionBranch(txnBranch);
            transaction.setTransactionBranchOrganization(user.getOrganization());
            transaction.setReceiptDetailsDescription(txnreceipttypebankdetails);
            String paymentMode = "";
            String txnDocument = "";
            String txnRemarks = "";
            if (IdosConstants.PAYMODE_CASH == txnreceiptdetails) {
                paymentMode = "CASH";
            } else if (IdosConstants.PAYMODE_BANK == txnreceiptdetails) {
                paymentMode = "BANK";
            }
            if (!txnremarks.equals("") && txnremarks != null) {
                txnRemarks = user.getEmail() + "#" + txnremarks;
                transaction.setRemarks(txnRemarks);
                txnRemarks = transaction.getRemarks();
            }
            transaction.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(
                    transaction.getSupportingDocs(), user.getEmail(), supportingdoc, user, entityManager));
            transaction.setTransactionStatus("Accounted");
            String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
            transaction.setTransactionRefNumber(transactionNumber);
            TRANSACTION_DAO.setInvoiceQuotProfSerial(user, entityManager, transaction);
            genericDao.saveOrUpdate(transaction, user, entityManager);
            FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, supportingdoc, transaction.getId(),
                    IdosConstants.MAIN_TXN_TYPE);
            CREATE_TRIAL_BALANCE_DAO.saveMultiItemTrialBalance(transaction, user, entityManager, vend,
                    tdsRecSpecificID);
            // call Karvy API to submit Sell data for GST Filing
            KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
            karvyAPICall.saveGSTFilingData(user, transaction, entityManager);
            // karvyAPICall.sendReceiveAdvFromCustTranDataToKarvy(transaction,entityManager);

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
            String debitCredit = "Credit";
            entitytransaction.commit();
            String invoiceDate = "";
            String invoiceDateLabel = "";
            if (transaction.getTransactionInvoiceDate() != null) {
                invoiceDateLabel = "INVOICE DATE:";
                invoiceDate = IdosConstants.idosdf.format(transaction.getTransactionInvoiceDate());
            }
            String itemParentName = "";
            if (transaction.getTransactionSpecifics().getParentSpecifics() != null
                    && !transaction.getTransactionSpecifics().getParentSpecifics().equals("")) {
                itemParentName = transaction.getTransactionSpecifics().getParentSpecifics().getName();
            } else {
                itemParentName = transaction.getTransactionSpecifics().getParticularsId().getName();
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
            String invoiceNumber = transaction.getInvoiceNumber() == null ? "" : transaction.getInvoiceNumber();
            Integer typeOfSupply = transaction.getTypeOfSupply() == null ? 0 : transaction.getTypeOfSupply();
            txnDocument = transaction.getSupportingDocs() == null ? "" : transaction.getSupportingDocs();
            TransactionViewResponse.addTxn(transaction.getId(), txnBranch.getName(), projectName,
                    transaction.getTransactionSpecifics().getName(), itemParentName,
                    transaction.getTransactionVendorCustomer().getName(),
                    transaction.getTransactionPurpose().getTransactionPurpose(),
                    IdosConstants.idosdf.format(transaction.getTransactionDate()), invoiceDateLabel, invoiceDate,
                    paymentMode, 0.0, 0.0, 0.0, transaction.getNetAmount(), txnResultDesc, "",
                    transaction.getTransactionStatus(), transaction.getCreatedBy().getEmail(), "", "", txnDocument,
                    txnRemarks, debitCredit, txnSpecialStatus, 0.0, "", txnInstrumentNumber,
                    txnInstrumentDate, transaction.getTransactionPurpose().getId(), invoiceNumber,
                    transaction.getTransactionRefNumber(), typeOfSupply, result);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Receive Advance from customer- submit for accounting.", ex.getMessage());
        }
        return transaction;
    }
}
