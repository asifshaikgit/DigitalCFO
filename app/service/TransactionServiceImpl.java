package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import actor.CreatorActor;

import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import akka.stream.javadsl.*;
import akka.actor.*;
import model.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.json.JSONObject;
import play.libs.Json;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;

import java.util.logging.Level;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import akka.NotUsed;

public class TransactionServiceImpl implements TransactionService {
    private EntityManager entityManager;

    @Override
    public Transaction receiveSpecialAdjustmentsFromVendors(String txnPurpose, TransactionPurpose usertxnPurpose,
            Users user, String txnRSAAFVCreditVendor, String txnRSAAFVAmountReceived, String txnRSAAFVForProject,
            String txnreceiptdetails, String txnreceiptPaymentBank, String txnreceipttypebankdetails,
            String supportingdoc, String txnremarks, String klfollowednotfollowed, EntityTransaction entitytransaction,
            EntityManager em) throws IDOSException {
        return transactionDao.receiveSpecialAdjustmentsFromVendors(txnPurpose, usertxnPurpose, user,
                txnRSAAFVCreditVendor, txnRSAAFVAmountReceived, txnRSAAFVForProject, txnreceiptdetails,
                txnreceiptPaymentBank, txnreceipttypebankdetails, supportingdoc, txnremarks, klfollowednotfollowed,
                entitytransaction, em);
    }

    @Override
    public Transaction paySpecialAdjustmentsToVendors(String txnPurpose, TransactionPurpose usertxnPurpose, Users user,
            String txnPSAATVCreditVendor, String txnPSAATVAmountPaid, String txnPSAATVForProject, String supportingdoc,
            String txnremarks, String klfollowednotfollowed, EntityTransaction entitytransaction, EntityManager em)
            throws IDOSException {
        Transaction txn = transactionDao.paySpecialAdjustmentsToVendors(txnPurpose, usertxnPurpose, user,
                txnPSAATVCreditVendor, txnPSAATVAmountPaid, txnPSAATVForProject, supportingdoc, txnremarks,
                klfollowednotfollowed, entitytransaction, em);
        return txn;
    }

    @Override
    public ObjectNode approverCashBankReceivablePayables(Users user, EntityManager em) {
        ObjectNode row = transactionDao.approverCashBankReceivablePayables(user, em);
        return row;
    }

    @Override
    public ObjectNode branchWiseApproverCashBankReceivablePayables(Users user, EntityManager em, String tabElement) {
        ObjectNode row = transactionDao.branchWiseApproverCashBankReceivablePayables(user, em, tabElement);
        return row;
    }

    @Override
    public ObjectNode wightedAverageForTransaction(Users user, Transaction transaction, String period,
            EntityManager em) {
        ObjectNode row = transactionDao.wightedAverageForTransaction(user, transaction, period, em);
        return row;
    }

    @Override
    public ObjectNode documentRule(Users user, String txnForExpItem, String txnForExpBranch, String txnForExpNetAmount)
            throws IDOSException {
        ObjectNode row = transactionDao.documentRule(user, txnForExpItem, txnForExpBranch, txnForExpNetAmount);
        return row;
    }

    @Override
    public ObjectNode documentRulePVS(Users user, String txnInv, String txnpaymentReceived, EntityManager entityManager)
            throws IDOSException {
        ObjectNode row = transactionDao.documentRulePVS(user, txnInv, txnpaymentReceived, entityManager);
        return row;
    }

    @Override
    public ObjectNode accountHeadTransactions(ObjectNode result, JsonNode json, Users user, EntityManager em,
            EntityTransaction entitytransaction) {
        result = transactionDao.accountHeadTransactions(result, json, user, em, entitytransaction);
        return result;
    }

    @Override
    public Transaction bankServices(ObjectNode result, JsonNode json, Users user, EntityManager em,
            EntityTransaction entitytransaction) throws IDOSException {
        return transactionDao.bankServices(result, json, user, em, entitytransaction);
    }

    @Override
    public void cashBankBalanceEffectWithdrawalFromBank(BranchBankAccounts branchBankAccount, Branch branch,
            Double amount, EntityManager em, EntityTransaction entitytransaction, Users user) {
        log.log(Level.FINE, "============ Start");
        transactionDao.cashBankBalanceEffectWithdrawalFromBank(branchBankAccount, branch, amount, em, entitytransaction,
                user);
    }

    @Override
    public void cashBankBalanceEffectDepositToBank(BranchBankAccounts branchBankAccount, Branch branch, Double amount,
            EntityManager em, EntityTransaction entitytransaction, Users user) {
        transactionDao.cashBankBalanceEffectDepositToBank(branchBankAccount, branch, amount, em, entitytransaction,
                user);
    }

    @Override
    public void cashBankBalanceEffectTransferFromOneBankToAnotherBank(BranchBankAccounts fromBranchBankAccount,
            Branch fromBranch, BranchBankAccounts toBranchBankAccount, Branch toBranch, Double amount, EntityManager em,
            EntityTransaction entitytransaction, Users user) {
        transactionDao.cashBankBalanceEffectTransferFromOneBankToAnotherBank(fromBranchBankAccount, fromBranch,
                toBranchBankAccount, toBranch, amount, em, entitytransaction, user);
    }

    @Override
    public ObjectNode checkMaxDiscountForWalkinCust(ObjectNode result, JsonNode json, Users user, EntityManager em) {
        result = transactionDao.checkMaxDiscountForWalkinCust(result, json, user, em);
        return result;
    }

    @Override
    public ObjectNode branchCustomerVendorReceivablePayables(ObjectNode result, JsonNode json, Users user,
            EntityManager em) {
        result = transactionDao.branchCustomerVendorReceivablePayables(result, json, user, em);
        return result;
    }

    @Override
    public ObjectNode overUnderOneEightyReceivablePayablesTxn(ObjectNode result, JsonNode json, Users user,
            EntityManager em, EntityTransaction entitytransaction) {
        result = transactionDao.overUnderOneEightyReceivablePayablesTxn(result, json, user, em, entitytransaction);
        return result;
    }

    @Override
    public String downloadOverUnderOneEightyDayaTxnExcel(ObjectNode result, JsonNode json, Users user, EntityManager em,
            EntityTransaction entitytransaction, String path) {
        String filename = transactionDao.downloadOverUnderOneEightyDayaTxnExcel(result, json, user, em,
                entitytransaction, path);
        return filename;
    }

    @Override
    public void saveTransactionBRSDate(Users user, EntityManager em, EntityTransaction entitytransaction,
            String transactionRef, String brsBankDate) {
        transactionDao.saveTransactionBRSDate(user, em, entitytransaction, transactionRef, brsBankDate);
    }

    @Override
    public void sendStockWebSocketResponse(EntityManager entityManager, Transaction transaction, Users user,
            ObjectNode result) {
        try {
            // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
            // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
            // for (int i = 0; i < keyArray.length; i++) {
            // Users orgusers = Users.findActiveByEmail(String.valueOf(keyArray[i]));
            // if (orgusers != null && orgusers.getOrganization().getId() ==
            // user.getOrganization().getId()) {
            // orgtxnregistereduser.put(keyArray[i].toString(),
            // CreatorActor.expenseregistrered.get(keyArray[i]));
            // }
            // }

            String branchName = transaction.getTransactionBranch().getName() == null ? ""
                    : transaction.getTransactionBranch().getName();
            String projectName = transaction.getTransactionProject() == null ? ""
                    : transaction.getTransactionProject().getName();
            String itemName = transaction.getTransactionSpecifics() == null ? ""
                    : transaction.getTransactionSpecifics().getName();
            String itemParentName = "";
            if (transaction.getTransactionSpecifics() != null) {
                if (transaction.getTransactionSpecifics().getParentSpecifics() != null
                        && !transaction.getTransactionSpecifics().getParentSpecifics().equals("")) {
                    itemParentName = transaction.getTransactionSpecifics().getParentSpecifics().getName();
                } else {
                    if (transaction.getTransactionSpecifics().getParticularsId() != null
                            && !transaction.getTransactionSpecifics().getParticularsId().equals("")) {
                        itemParentName = transaction.getTransactionSpecifics().getParticularsId().getName();
                    }
                }
            }

            String budgetAllocated = transaction.getActualAllocatedBudget() == null ? ""
                    : transaction.getActualAllocatedBudget();
            String budgetAvailable = transaction.getBudgetAvailDuringTxn() == null ? ""
                    : String.valueOf(transaction.getBudgetAvailDuringTxn());

            String customerVendorName = transaction.getTransactionVendorCustomer() == null ? ""
                    : transaction.getTransactionVendorCustomer().getName();
            String invoiceDate = "";
            String invoiceDateLabel = "";
            if (transaction.getTransactionInvoiceDate() != null) {
                invoiceDateLabel = "INVOICE DATE:";
                invoiceDate = IdosConstants.idosdf.format(transaction.getTransactionInvoiceDate());
            }
            String paymentMode = "";
            if (transaction.getReceiptDetailsType() != null) {
                if (IdosConstants.PAYMODE_CASH == transaction.getReceiptDetailsType()) {
                    paymentMode = "CASH";
                } else if (IdosConstants.PAYMODE_BANK == transaction.getReceiptDetailsType()) {
                    paymentMode = "BANK";
                }
            }
            Double noOfUnits = transaction.getNoOfUnits() == null ? 0d : transaction.getNoOfUnits();
            Double perUnitPrice = transaction.getPricePerUnit() == null ? 0d : transaction.getPricePerUnit();
            Double grossAmt = transaction.getGrossAmount() == null ? 0d : transaction.getGrossAmount();
            Double netAmount = transaction.getNetAmount() == null ? 0d : transaction.getNetAmount();
            String netAmtDesc = transaction.getNetAmountResultDescription() == null ? ""
                    : transaction.getNetAmountResultDescription();
            String outstandings = "";
            String createdBy = transaction.getCreatedBy().getEmail() == null ? ""
                    : transaction.getCreatedBy().getEmail();
            String approverLabel = "";
            String approverEmail = "";
            if (transaction.getApproverActionBy() != null) {
                approverLabel = "APPROVER";
                approverEmail = transaction.getApproverActionBy().getEmail();
            }
            String additionalApprovalEmails = "";
            if (transaction.getAdditionalApproverEmails() != null) {
                additionalApprovalEmails = transaction.getAdditionalApproverEmails();
            }
            String selectedAdditionalApproval = "";
            if (transaction.getSelectedAdditionalApprover() != null) {
                selectedAdditionalApproval = transaction.getSelectedAdditionalApprover();
            }
            String txnDocument = transaction.getSupportingDocs() == null ? "" : transaction.getSupportingDocs();
            String txnRemarks = transaction.getRemarks() == null ? "" : transaction.getRemarks();
            String debitCredit = "";

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
            Double frieghtCharges = transaction.getFrieghtCharges() == null ? 0.0 : transaction.getFrieghtCharges();
            String poReference = transaction.getPoReference() == null ? "" : transaction.getPoReference();
            String txnInstrumentNumber = transaction.getInstrumentNumber() == null ? ""
                    : transaction.getInstrumentNumber();
            String txnInstrumentDate = transaction.getInstrumentDate() == null ? "" : transaction.getInstrumentDate();
            String txnRemarksPrivate = transaction.getRemarksPrivate() == null ? "" : transaction.getRemarksPrivate();
            String invoiceNumber = transaction.getInvoiceNumber() == null ? "" : transaction.getInvoiceNumber();
            Integer typeOfSupply = transaction.getTypeOfSupply() == null ? 0 : transaction.getTypeOfSupply();
            TransactionViewResponse.addActionTxn(transaction.getId(), branchName, projectName, itemName, itemParentName,
                    budgetAllocated, "", budgetAvailable, "", customerVendorName,
                    transaction.getTransactionPurpose().getTransactionPurpose(),
                    IdosConstants.idosdf.format(transaction.getTransactionDate()), invoiceDateLabel, invoiceDate,
                    paymentMode, noOfUnits, perUnitPrice, grossAmt, netAmount, netAmtDesc, outstandings,
                    transaction.getTransactionStatus(), createdBy, approverLabel, approverEmail, txnDocument,
                    txnRemarks, debitCredit, approverEmail, additionalApprovalEmails, selectedAdditionalApproval,
                    txnSpecialStatus, frieghtCharges, poReference, txnInstrumentNumber,
                    txnInstrumentDate, transaction.getTransactionPurpose().getId(), txnRemarksPrivate, invoiceNumber, 0,
                    transaction.getTransactionRefNumber(), 0l, 0.0, 0, typeOfSupply, result);
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
        }
    }

    @Override
    public void sendBankWebSocketResponse(EntityManager entityManager, Transaction transaction, Users user,
            ObjectNode result) {
        try {
            Long id = null;
            String branchName = "";
            String projectName = "";
            String itemName = "";
            String itemParentName = "";
            String budgetAllocated = "";
            String budgetAllocatedAmt = "";
            String budgetAvailable = "";
            String budgetAvailableAmt = "";
            String customerVendorName = "";
            String transactionPurpose = "";
            String txnDate = "";
            String invoiceDateLabel = "";
            String invoiceDate = "";
            String paymentMode = "";
            Integer noOfUnit = null;
            Double unitPrice = null;
            Double grossAmount = null;
            Double netAmount = null;
            String netAmtDesc = "";
            String outStandings = "";
            String status = "";
            String createdBy = "";
            String approverLabel = "";
            String approverEmail = "";
            String txnDocument = "";
            String txnRemarks = "";
            String debitCredit = "";
            String approverEmails = "";
            String additionalApprovalEmails = "";
            String selectedAdditionalApproval = "";
            Map<String, ActorRef> orgexpenseregistrered = null;
            String txnSpecialStatus = "";
            if (transaction.getId() != null) {
                id = transaction.getId();
            }
            if (transaction.getTransactionBranch() != null) {
                branchName = transaction.getTransactionBranch().getName();
            }
            if (transaction.getTransactionPurpose() != null) {
                transactionPurpose = transaction.getTransactionPurpose().getTransactionPurpose();
            }
            if (transaction.getTransactionDate() != null) {
                txnDate = IdosConstants.idosdf.format(transaction.getTransactionDate());
            }
            if (transaction.getGrossAmount() != null) {
                grossAmount = transaction.getGrossAmount();
            }
            if (transaction.getNetAmount() != null) {
                netAmount = transaction.getNetAmount();
            }
            if (transaction.getNetAmountResultDescription() != null) {
                netAmtDesc = transaction.getNetAmountResultDescription();
            }
            if (transaction.getTransactionStatus() != null) {
                status = transaction.getTransactionStatus();
            }
            if (transaction.getCreatedBy() != null) {
                createdBy = transaction.getCreatedBy().getEmail();
            }
            if (transaction.getApproverActionBy() != null) {
                approverLabel = "APPROVER";
                approverEmail = transaction.getApproverActionBy().getEmail();
            }
            if (transaction.getApproverEmails() != null) {
                approverEmails = transaction.getApproverEmails();
            }
            if (transaction.getAdditionalApproverEmails() != null) {
                additionalApprovalEmails = transaction.getAdditionalApproverEmails();
            }
            if (transaction.getSelectedAdditionalApprover() != null) {
                selectedAdditionalApproval = transaction.getSelectedAdditionalApprover();
            }
            if (transaction.getSupportingDocs() != null) {
                txnDocument = transaction.getSupportingDocs();
            }
            // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
            // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
            // for (int i = 0; i < keyArray.length; i++) {
            // Users orgusers = Users.findActiveByEmail(String.valueOf(keyArray[i]));
            // if (orgusers.getOrganization().getId() == user.getOrganization().getId()) {
            // orgtxnregistereduser.put(keyArray[i].toString(),
            // CreatorActor.expenseregistrered.get(keyArray[i]));
            // }
            // }
            String txnInstrumentNumber = transaction.getInstrumentNumber() == null ? ""
                    : transaction.getInstrumentNumber();
            String txnInstrumentDate = transaction.getInstrumentDate() == null ? "" : transaction.getInstrumentNumber();
            String invoiceNumber = transaction.getInvoiceNumber() == null ? "" : transaction.getInvoiceNumber();
            Integer typeOfSupply = transaction.getTypeOfSupply() == null ? 0 : transaction.getTypeOfSupply();
            TransactionViewResponse.addActionTxn(id, branchName, projectName, itemName, itemParentName, budgetAllocated,
                    budgetAllocatedAmt, budgetAvailable, budgetAvailableAmt, customerVendorName, transactionPurpose,
                    txnDate, invoiceDateLabel, invoiceDate, paymentMode, 0d, 0.0, grossAmount, netAmount, netAmtDesc,
                    "", status, createdBy, approverLabel, approverEmail, txnDocument, txnRemarks, debitCredit,
                    approverEmails, additionalApprovalEmails, selectedAdditionalApproval,
                    txnSpecialStatus, 0.0, "", "", "", transaction.getTransactionPurpose().getId(), "", invoiceNumber,
                    0, transaction.getTransactionRefNumber(), 0l, 0.0, 0, typeOfSupply, result);

        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
        }
    }

@Override
public boolean getTaxOnAdvOrAdj(JsonNode json, EntityManager entityManager, Users user, ObjectNode result)
            throws IDOSException {
        ArrayNode advAdjTaxResult = result.putArray("advAdjTaxData");
        Map<String, Object> criterias = new HashMap<String, Object>();
        String txnBranchId = json.findValue("txnBranchId") == null ? "" : json.findValue("txnBranchId").asText();
        Long txnSpecificsId = json.findValue("txnSpecificsId").asLong();
        Double txnAdjustmentAmount = json.findValue("txnAdjustmentAmount") == null ? 0.0
                : json.findValue("txnAdjustmentAmount").asDouble();
        long txnPurposeValue = json.findValue("txnPurposeValue").asLong();
        String txnSourceGstinCode = json.findValue("txnSourceGstinCode") == null ? ""
                : json.findValue("txnSourceGstinCode").asText();
        String txnDestGstinCode = json.findValue("txnDestGstinCode") == null ? ""
                : json.findValue("txnDestGstinCode").asText();

        int txnSrcGstinStateCode = 0;
        int txnDstnGstinStateCode = 0;
        /*Specifics specifics = Specifics.findById(txnSpecificsId);
     if(specifics.getGSTApplicable().equals("true")){*/
        if (txnSourceGstinCode != null && !"".equals(txnSourceGstinCode) && txnSourceGstinCode.length() > 1) {
            txnSourceGstinCode = txnSourceGstinCode.substring(0, 2);
            txnSrcGstinStateCode = IdosUtil.convertStringToInt(txnSourceGstinCode);
        }
        if (txnDestGstinCode != null && !"".equals(txnDestGstinCode) && !"".equals("null")
                && txnDestGstinCode.length() > 1) {
            txnDestGstinCode = txnDestGstinCode.substring(0, 2);
            txnDstnGstinStateCode = IdosUtil.convertStringToInt(txnDestGstinCode);
        }
        Specifics specifics = Specifics.findById(txnSpecificsId);
        boolean isGstTaxApplicable = true;
        if ("1".equals(specifics.getGstItemCategory()) || "2".equals(specifics.getGstItemCategory())
                || "3".equals(specifics.getGstItemCategory())) {
            isGstTaxApplicable = false;
            result.put("isGstTaxApplicable", "false");
        }
        if (IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW == txnPurposeValue
                || IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeValue ||
                IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER == txnPurposeValue
                || IdosConstants.CREDIT_NOTE_CUSTOMER == txnPurposeValue
                || IdosConstants.DEBIT_NOTE_CUSTOMER == txnPurposeValue
                || IdosConstants.REFUND_ADVANCE_RECEIVED == txnPurposeValue) {
            int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0
                    : json.findValue("txnTypeOfSupply").asInt();
            Integer txnWithWithoutTax = json.findValue("txnWithWithoutTax") == null ? null
                    : json.findValue("txnWithWithoutTax").asInt();
            String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
            Date txnDate = null;
            try {
                if (selectedTxnDate != null && !"".equals(selectedTxnDate)) {
                    selectedTxnDate = selectedTxnDate + " 23:59:59";
                    txnDate = IdosConstants.IDOSDTF.parse(selectedTxnDate);
                } else {
                    txnDate = new Date();
                }
                txnDate = IdosConstants.MYSQLDTF.parse(IdosConstants.MYSQLDTF.format(txnDate));
            } catch (ParseException e) {
                throw new IDOSException(IdosConstants.INVALID_DATA_EXCEPTION, IdosConstants.DATA_FORMAT_EXCEPTION,
                        "Date Parse Issue", IdosConstants.INVALID_DATA_EXCEPTION);
            }
            if (txnTypeOfSupply != 1 && txnTypeOfSupply != 3 && txnTypeOfSupply != 4 && txnTypeOfSupply != 5
                    && txnTypeOfSupply != 6) {
                return true;
            }
            if ((txnTypeOfSupply == 3 || txnTypeOfSupply == 4 || txnTypeOfSupply == 5) && txnWithWithoutTax != null
                    && txnWithWithoutTax == 2) {
                return true;
            }
            Double cessRate = 0.0;
            Double igstRate = 0.0;
            /*
             * criterias.clear();
             * criterias.put("branch.id", Long.parseLong(txnBranchId));
             * criterias.put("organization.id", user.getOrganization().getId());
             * criterias.put("specifics.id", txnSpecificsId);
             * criterias.put("presentStatus", 1);
             * List<BranchSpecificsTaxFormula> bnchSpecfTaxFormula =
             * genericDAO.findByCriteria(BranchSpecificsTaxFormula.class, criterias, entityManager);
             */
            List<BranchSpecificsTaxFormula> bnchSpecfTaxFormula = new ArrayList<BranchSpecificsTaxFormula>();

            List<Integer> taxTypeList = new ArrayList<Integer>(1);
            if (specifics.getGstTaxRate() != null || DateUtil.isBackDate(txnDate)) {
                taxTypeList.add(new Integer(IdosConstants.OUTPUT_SGST));
                List<BranchSpecificsTaxFormula> findTaxOnSpecificDate = BranchSpecificsTaxFormula.findTaxOnSpecificDate(
                        entityManager, user.getOrganization().getId(), IdosUtil.convertStringToLong(txnBranchId),
                        txnSpecificsId, txnDate, taxTypeList);
                if (findTaxOnSpecificDate != null && findTaxOnSpecificDate.size() > 0) {
                    bnchSpecfTaxFormula.add(findTaxOnSpecificDate.get(0));
                }

                taxTypeList.clear();
                taxTypeList.add(new Integer(IdosConstants.OUTPUT_CGST));
                findTaxOnSpecificDate = BranchSpecificsTaxFormula.findTaxOnSpecificDate(entityManager,
                        user.getOrganization().getId(), IdosUtil.convertStringToLong(txnBranchId), txnSpecificsId,
                        txnDate, taxTypeList);
                if (findTaxOnSpecificDate != null && findTaxOnSpecificDate.size() > 0) {
                    bnchSpecfTaxFormula.add(findTaxOnSpecificDate.get(0));
                }

                taxTypeList.clear();
                taxTypeList.add(new Integer(IdosConstants.OUTPUT_IGST));
                findTaxOnSpecificDate = BranchSpecificsTaxFormula.findTaxOnSpecificDate(entityManager,
                        user.getOrganization().getId(), IdosUtil.convertStringToLong(txnBranchId), txnSpecificsId,
                        txnDate, taxTypeList);
                if (findTaxOnSpecificDate != null && findTaxOnSpecificDate.size() > 0) {
                    bnchSpecfTaxFormula.add(findTaxOnSpecificDate.get(0));
                }

            }
            if (specifics.getCessTaxRate() != null || DateUtil.isBackDate(txnDate)) {
                taxTypeList.clear();
                taxTypeList.add(new Integer(IdosConstants.OUTPUT_CESS));
                List<BranchSpecificsTaxFormula> findTaxOnSpecificDate = BranchSpecificsTaxFormula.findTaxOnSpecificDate(
                        entityManager, user.getOrganization().getId(), IdosUtil.convertStringToLong(txnBranchId),
                        txnSpecificsId, txnDate, taxTypeList);
                if (findTaxOnSpecificDate != null && findTaxOnSpecificDate.size() > 0) {
                    bnchSpecfTaxFormula.add(findTaxOnSpecificDate.get(0));
                }
            }
            for (int i = 0; i < bnchSpecfTaxFormula.size(); i++) {
                if (cessRate != 0.0 && igstRate != 0.0) {
                    break;
                }
                int taxType = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxType();
                if (taxType == IdosConstants.OUTPUT_CESS) {
                    cessRate = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate() == null ? 0.0
                            : bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate();
                } else if (taxType == IdosConstants.OUTPUT_IGST) {
                    igstRate = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate() == null ? 0.0
                            : bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate();
                }
            }
            for (int i = 0; i < bnchSpecfTaxFormula.size(); i++) {
                int taxType = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxType();
                if (taxType == IdosConstants.OUTPUT_TAX || taxType == IdosConstants.OUTPUT_SGST
                        || taxType == IdosConstants.OUTPUT_CGST
                        || taxType == IdosConstants.OUTPUT_IGST || taxType == IdosConstants.OUTPUT_CESS) {

                    String taxName = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxName();
                    if (taxName != null) {
                        if ((!isGstTaxApplicable) && (taxName.startsWith("SGST") || taxName.startsWith("CGST")
                                || taxName.startsWith("IGST") || taxName.startsWith("CESS"))) {
                            continue;
                        }
                        if (isGstTaxApplicable && txnWithWithoutTax != null && txnWithWithoutTax != null
                                && txnWithWithoutTax == 1
                                && (taxName.startsWith("SGST") || taxName.startsWith("CGST"))) {
                            continue;
                        }
                        if (txnTypeOfSupply != 3) {
                            if (((txnSrcGstinStateCode == 0 || txnDstnGstinStateCode == 0)
                                    && ((taxName.startsWith("SGST") || taxName.startsWith("CGST"))))
                                    || (txnSrcGstinStateCode != txnDstnGstinStateCode
                                            && ((taxName.startsWith("SGST") || taxName.startsWith("CGST"))))) {
                                continue;
                            }
                            if ((txnSrcGstinStateCode == 0 || txnDstnGstinStateCode == 0) && taxName.startsWith("IGST")
                                    || (txnSrcGstinStateCode == txnDstnGstinStateCode && (taxName.startsWith("IGST")
                                            && txnWithWithoutTax != null && txnWithWithoutTax != 1))) {
                                continue;
                            }
                        }
                    }
                    Double taxRate = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate() == null ? 0.0
                            : bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate();
                    ObjectNode row = Json.newObject();
                    if (taxName != null && taxName.length() > 4) {
                        row.put("taxName", taxName.substring(0, 4));
                    } else {
                        row.put("taxName", taxName);
                    }
                    row.put("taxRate", taxRate);
                    row.put("taxid", bnchSpecfTaxFormula.get(i).getBranchTaxes().getId());
                    Double taxAmount = 0.0;

                    if (isGstTaxApplicable) {
                        if (taxName.startsWith("SGST") || taxName.startsWith("CGST")) {
                            Double taxRateToApply = taxRate * 2;
                            double amount = ((txnAdjustmentAmount) / (100 + cessRate + taxRateToApply)) * 100;
                            taxAmount = (amount * (taxRate / 100));
                        } else if (taxName.startsWith("IGST")) {
                            double amount = ((txnAdjustmentAmount) / (100 + cessRate + taxRate)) * 100;
                            taxAmount = (amount * (taxRate / 100));
                        } else if (taxName.startsWith("CESS")) {
                            double amount = ((txnAdjustmentAmount) / (100 + igstRate + taxRate)) * 100;
                            taxAmount = (amount * taxRate) / 100;
                        }
                        row.put("individualTax",
                                taxName + "(+" + taxRate + "%):" + IdosConstants.DECIMAL_FORMAT2.format(taxAmount));
                    }
                    row.put("taxAmountWithoutRoundup", taxAmount);
                    row.put("taxAmount", IdosConstants.DECIMAL_FORMAT2.format(taxAmount));
                    advAdjTaxResult.add(row);
                }
            }
        }
    //}
        return true;
 }

    @Override
    public Transaction submitForAccountingRecCust(Users user, JsonNode json, EntityManager em,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
        return RECEIVE_FROM_CUSTOMER_DAO.submitForAccounting(user, json, em, entitytransaction, result);
    }

    @Override
    public Transaction submitForApprovalPayAdvToVend(Users user, JsonNode json, EntityManager em,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
        return PAY_TO_VENDOR_DAO.submitForAprroval(user, json, em, entitytransaction, result);
    }

    @Override
    public boolean submitForAccountPayAdvToVendAdv(Transaction txn, EntityManager em, Users user) throws IDOSException {
        return PAY_TO_VENDOR_DAO.submitForAccountPayAdvToVendAdv(txn, em, user);
    }

    @Override
    public Transaction submitForApprovalNote(Users user, JsonNode json, EntityManager em,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException {
        return CREDIT_DEBIT_DAO.submitForApproval(user, json, em, entitytransaction, result);
    }

    @Override
    public Transaction submitForApprovalVendorNote(Users user, JsonNode json, EntityManager em, EntityTransaction et,
            TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException {
        return CREDIT_DEBIT_DAO.submitForApprovalVendor(user, json, em, et, txnPurpose, result);
    }

    @Override
    public Transaction submitForApprovalInterBranchTransfer(Users user, JsonNode json, EntityManager em,
            EntityTransaction et, TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException {
        return INTER_BRANCH_TRANSFER_DAO.submitForApproval(user, json, em, et, txnPurpose, result);
    }

    @Override
    public Transaction submitForApprovalRefundAdvanceRecived(Users user, JsonNode json, EntityManager em,
            EntityTransaction et, TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException {
        return REFUND_ADVANCE_DAO.submitForApprovalRefundAdvanceRecived(user, json, em, et, result);
    }

    @Override
    public Transaction submitForApprovalRefundAmountRecivedAgainstInvoice(Users user, JsonNode json, EntityManager em,
            EntityTransaction entitytransaction, TransactionPurpose usertxnPurpose, ObjectNode result)
            throws IDOSException {
        return REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE_DAO.submitForApprovalRefundAmountRecived(user, json, em,
                entitytransaction, result);
    }

    @Override
    public boolean getInclusiveTaxCalculated(JsonNode json, EntityManager em, Users user, ObjectNode result)
            throws IDOSException {
        ArrayNode advAdjTaxResult = result.putArray("advAdjTaxData");
        Map<String, Object> criterias = new HashMap<String, Object>();
        String txnBranchId = json.findValue("txnBranchId") == null ? "" : json.findValue("txnBranchId").asText();
        Long txnSpecificsId = json.findValue("txnSpecificsId").asLong();
        Double txnAdjustmentAmount = json.findValue("txnAdjustmentAmount") == null ? 0.0
                : json.findValue("txnAdjustmentAmount").asDouble();
        long txnPurposeValue = json.findValue("txnPurposeValue").asLong();
        String txnSourceGstinCode = json.findValue("txnSourceGstinCode") == null ? ""
                : json.findValue("txnSourceGstinCode").asText();
        String txnDestGstinCode = json.findValue("txnDestGstinCode") == null ? ""
                : json.findValue("txnDestGstinCode").asText();

        int txnSrcGstinStateCode = 0;
        int txnDstnGstinStateCode = 0;
        if (txnSourceGstinCode != null && !"".equals(txnSourceGstinCode) && txnSourceGstinCode.length() > 1) {
            txnSourceGstinCode = txnSourceGstinCode.substring(0, 2);
            txnSrcGstinStateCode = IdosUtil.convertStringToInt(txnSourceGstinCode);
        }
        if (txnDestGstinCode != null && !"".equals(txnDestGstinCode) && !"".equals("null")
                && txnDestGstinCode.length() > 1) {
            txnDestGstinCode = txnDestGstinCode.substring(0, 2);
            txnDstnGstinStateCode = IdosUtil.convertStringToInt(txnDestGstinCode);
        }
        Specifics specifics = Specifics.findById(txnSpecificsId);
        boolean isGstTaxApplicable = true;
        if ("1".equals(specifics.getGstItemCategory()) || "2".equals(specifics.getGstItemCategory())
                || "3".equals(specifics.getGstItemCategory())) {
            isGstTaxApplicable = false;
            result.put("isGstTaxApplicable", "false");
        }
        if (IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW == txnPurposeValue
                || IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurposeValue ||
                IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER == txnPurposeValue
                || IdosConstants.CREDIT_NOTE_CUSTOMER == txnPurposeValue
                || IdosConstants.DEBIT_NOTE_CUSTOMER == txnPurposeValue
                || IdosConstants.REFUND_ADVANCE_RECEIVED == txnPurposeValue) {
            int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0
                    : json.findValue("txnTypeOfSupply").asInt();
            Integer txnWithWithoutTax = json.findValue("txnWithWithoutTax") == null ? null
                    : json.findValue("txnWithWithoutTax").asInt();
            String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
            Date txnDate = IdosUtil.getFormatedDateWithTime(selectedTxnDate);

            if (txnTypeOfSupply != 1 && txnTypeOfSupply != 3 && txnTypeOfSupply != 4 && txnTypeOfSupply != 5
                    && txnTypeOfSupply != 6) {
                return true;
            }
            if ((txnTypeOfSupply == 3 || txnTypeOfSupply == 4 || txnTypeOfSupply == 5) && txnWithWithoutTax != null
                    && txnWithWithoutTax == 2) {
                return true;
            }
            Double cessRate = 0.0;
            Double igstRate = 0.0;
            /*
             * criterias.clear();
             * criterias.put("branch.id", Long.parseLong(txnBranchId));
             * criterias.put("organization.id", user.getOrganization().getId());
             * criterias.put("specifics.id", txnSpecificsId);
             * criterias.put("presentStatus", 1);
             * List<BranchSpecificsTaxFormula> bnchSpecfTaxFormula =
             * genericDAO.findByCriteria(BranchSpecificsTaxFormula.class, criterias, em);
             */
            List<BranchSpecificsTaxFormula> bnchSpecfTaxFormula = new ArrayList<BranchSpecificsTaxFormula>();

            List<Integer> taxTypeList = new ArrayList<Integer>(1);
            if (specifics.getGstTaxRate() != null || DateUtil.isBackDate(txnDate)) {
                taxTypeList.add(new Integer(IdosConstants.OUTPUT_SGST));
                List<BranchSpecificsTaxFormula> findTaxOnSpecificDate = BranchSpecificsTaxFormula.findTaxOnSpecificDate(
                        entityManager, user.getOrganization().getId(), IdosUtil.convertStringToLong(txnBranchId),
                        txnSpecificsId, txnDate, taxTypeList);
                if (findTaxOnSpecificDate != null && findTaxOnSpecificDate.size() > 0) {
                    bnchSpecfTaxFormula.add(findTaxOnSpecificDate.get(0));
                }

                taxTypeList.clear();
                taxTypeList.add(new Integer(IdosConstants.OUTPUT_CGST));
                findTaxOnSpecificDate = BranchSpecificsTaxFormula.findTaxOnSpecificDate(entityManager,
                        user.getOrganization().getId(), IdosUtil.convertStringToLong(txnBranchId), txnSpecificsId,
                        txnDate, taxTypeList);
                if (findTaxOnSpecificDate != null && findTaxOnSpecificDate.size() > 0) {
                    bnchSpecfTaxFormula.add(findTaxOnSpecificDate.get(0));
                }

                taxTypeList.clear();
                taxTypeList.add(new Integer(IdosConstants.OUTPUT_IGST));
                findTaxOnSpecificDate = BranchSpecificsTaxFormula.findTaxOnSpecificDate(entityManager,
                        user.getOrganization().getId(), IdosUtil.convertStringToLong(txnBranchId), txnSpecificsId,
                        txnDate, taxTypeList);
                if (findTaxOnSpecificDate != null && findTaxOnSpecificDate.size() > 0) {
                    bnchSpecfTaxFormula.add(findTaxOnSpecificDate.get(0));
                }
            }
            if (specifics.getCessTaxRate() != null || DateUtil.isBackDate(txnDate)) {

                taxTypeList.clear();
                taxTypeList.add(new Integer(IdosConstants.OUTPUT_CESS));
                List<BranchSpecificsTaxFormula> findTaxOnSpecificDate = BranchSpecificsTaxFormula.findTaxOnSpecificDate(
                        entityManager, user.getOrganization().getId(), IdosUtil.convertStringToLong(txnBranchId),
                        txnSpecificsId, txnDate, taxTypeList);
                if (findTaxOnSpecificDate != null && findTaxOnSpecificDate.size() > 0) {
                    bnchSpecfTaxFormula.add(findTaxOnSpecificDate.get(0));
                }

            }

            for (int i = 0; i < bnchSpecfTaxFormula.size(); i++) {
                if (cessRate != 0.0 && igstRate != 0.0) {
                    break;
                }
                int taxType = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxType();
                if (taxType == IdosConstants.OUTPUT_CESS) {
                    cessRate = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate() == null ? 0.0
                            : bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate();
                } else if (taxType == IdosConstants.OUTPUT_IGST) {
                    igstRate = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate() == null ? 0.0
                            : bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate();
                }
            }
            for (int i = 0; i < bnchSpecfTaxFormula.size(); i++) {
                int taxType = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxType();
                if (taxType == IdosConstants.OUTPUT_TAX || taxType == IdosConstants.OUTPUT_SGST
                        || taxType == IdosConstants.OUTPUT_CGST
                        || taxType == IdosConstants.OUTPUT_IGST || taxType == IdosConstants.OUTPUT_CESS) {

                    String taxName = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxName();
                    if (taxName != null) {
                        if ((!isGstTaxApplicable) && (taxName.startsWith("SGST") || taxName.startsWith("CGST")
                                || taxName.startsWith("IGST") || taxName.startsWith("CESS"))) {
                            continue;
                        }
                        if (isGstTaxApplicable && txnWithWithoutTax != null && txnWithWithoutTax == 1
                                && (taxName.startsWith("SGST") || taxName.startsWith("CGST"))) {
                            continue;
                        }
                        if (txnTypeOfSupply != 3) {
                            if (((txnSrcGstinStateCode == 0 || txnDstnGstinStateCode == 0)
                                    && ((taxName.startsWith("SGST") || taxName.startsWith("CGST"))))
                                    || (txnSrcGstinStateCode != txnDstnGstinStateCode
                                            && ((taxName.startsWith("SGST") || taxName.startsWith("CGST"))))) {
                                continue;
                            }
                            if ((txnSrcGstinStateCode == 0 || txnDstnGstinStateCode == 0) && taxName.startsWith("IGST")
                                    || (txnSrcGstinStateCode == txnDstnGstinStateCode && (taxName.startsWith("IGST")
                                            && txnWithWithoutTax != null && txnWithWithoutTax != 1))) {
                                continue;
                            }
                        }
                    }
                    Double taxRate = bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate() == null ? 0.0
                            : bnchSpecfTaxFormula.get(i).getBranchTaxes().getTaxRate();
                    ObjectNode row = Json.newObject();
                    if (taxName != null && taxName.length() > 4) {
                        row.put("taxName", taxName.substring(0, 4));
                    } else {
                        row.put("taxName", taxName);
                    }
                    row.put("taxRate", taxRate);
                    row.put("taxid", bnchSpecfTaxFormula.get(i).getBranchTaxes().getId());
                    Double taxAmount = 0.0;
                    if (isGstTaxApplicable) {
                        if (taxName.startsWith("SGST") || taxName.startsWith("CGST")) {
                            Double taxRateToApply = taxRate * 2;
                            double amount = ((txnAdjustmentAmount) / (100 + cessRate + taxRateToApply)) * 100;
                            taxAmount = (amount * (taxRate / 100));
                        } else if (taxName.startsWith("IGST")) {
                            double amount = ((txnAdjustmentAmount) / (100 + cessRate + taxRate)) * 100;
                            taxAmount = (amount * (taxRate / 100));
                        } else if (taxName.startsWith("CESS")) {
                            double amount = ((txnAdjustmentAmount) / (100 + igstRate + taxRate)) * 100;
                            taxAmount = (amount * taxRate) / 100;
                        }
                        row.put("individualTax",
                                taxName + "(+" + taxRate + "%):" + IdosConstants.DECIMAL_FORMAT2.format(taxAmount));
                    }
                    row.put("taxAmountWithoutRoundup", taxAmount);
                    row.put("taxAmount", IdosConstants.DECIMAL_FORMAT2.format(taxAmount));
                    advAdjTaxResult.add(row);
                }
            }
        }
        return true;
    }

    @Override
    public Transaction submitForApprovalPayVendor(Users user, JsonNode json, EntityManager em,
            TransactionPurpose txnPurposeObj, ObjectNode result) throws IDOSException {
        return PAY_VENDOR_DAO.submitForApproval(user, json, em, txnPurposeObj, result);
    }

    @Override
    public Transaction submitForApprovalTransferCashToPetty(Users user, JsonNode json, EntityManager em,
            TransactionPurpose txnPurposeObj, ObjectNode result) throws IDOSException {
        return TRANSFER_CASH_TO_PETTY_CASH_DAO.submitForApproval(user, json, em, txnPurposeObj, result);
    }

    @Override
    public Transaction findByReferenceNumber(String referenceNumber, EntityManager entityManager) {
        Transaction t = (Transaction) entityManager
                .createQuery("SELECT t FROM Transaction t WHERE t.transactionRefNumber =:referenceNumber")
                .setParameter("referenceNumber", referenceNumber).getSingleResult();
        return t;
    }
    /*
     * public String[] getApprovers(EntityManager em, Users user){
     * Map<String, Object> criterias = new HashMap<String, Object>(2);
     * criterias.put("role.name", "APPROVER");
     * criterias.put("organization.id", user.getOrganization().getId());
     * criterias.put("presentStatus", 1);
     * List<UsersRoles> approverRole = genericDAO.findByCriteria(UsersRoles.class,
     * criterias, em);
     * String approverEmails = "";
     * String additionalApprovarUsers = "";
     * String selectedAdditionalApproval = "";
     * Boolean approver=null;
     * for (UsersRoles usrRoles : approverRole) {
     * approver=false;
     * additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
     * criterias.clear();
     * criterias.put("user.id", usrRoles.getUser().getId());
     * criterias.put("userRights.id", 2L);
     * criterias.put("branch.id", txnBranch.getId());
     * criterias.put("presentStatus", 1);
     * UserRightInBranch userHasRightInBranch =
     * genericDAO.getByCriteria(UserRightInBranch.class, criterias, em);
     * if (userHasRightInBranch != null) {
     * for(int i=0;i<arrJSON.length();i++){
     * //Double howMuchAdvance=0.0;Double txnTaxAmount=0.0;Double
     * customerAdvance=0.0;String txnTaxDesc="";Double withholdingAmount=0.0;
     * JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
     * //TransactionItems transactionItem = new TransactionItems();
     * Long itemId =rowItemData.getLong("txnItems");
     * Specifics txnItem=genericDAO.getById(Specifics.class, itemId, entityManager);
     * criterias.clear();
     * criterias.put("user.id", usrRoles.getUser().getId());
     * criterias.put("userRights.id", 2L);
     * criterias.put("specifics.id",txnItem.getId());
     * criterias.put("presentStatus", 1);
     * UserRightSpecifics userHasRightInCOA =
     * genericDAO.getByCriteria(UserRightSpecifics.class, criterias, entityManager);
     * if (userHasRightInCOA!= null) {
     * approver = true;
     * } else {
     * approver = false;
     * }
     * }
     * if(approver){
     * approverEmails += usrRoles.getUser().getEmail() + ",";
     * }
     * }
     * }
     * }
     */
}
