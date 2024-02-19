package com.idos.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import java.util.logging.Level;
import model.Branch;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.TransactionPurpose;
import model.TrialBalanceBranchBank;
import model.TrialBalanceBranchCash;
import model.Users;
import model.payroll.PayrollSetup;
import model.payroll.PayrollTransaction;
import model.payroll.TrialBalancePayrollItem;
import play.libs.Json;
import service.BranchBankService;
import service.BranchBankServiceImpl;
import service.BranchCashService;
import service.BranchCashServiceImpl;

public class PayrollDAOImpl implements PayrollDAO {

    @Override
    public int payrollApproverAction(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
            EntityTransaction entitytransaction, PayrollTransaction payrollTxn) throws Exception {
        String selectedApproverAction = json.findValue("selectedApproverAction").asText();
        String transactionPrimId = json.findValue("transactionPrimId").asText();
        String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
        String txnRmarks = json.findValue("txnRmarks") != null ? json.findValue("txnRmarks").asText() : null;

        entitytransaction.begin();
        if (!payrollTxn.getTransactionStatus().equals("Approved")) {
            if (selectedApproverAction.equals("1")) {
                payrollTxn.setTransactionStatus("Approved");
                payrollTxn.setModifiedBy(user);
                payrollTxn.setApproverActionBy(user);
                payrollTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(
                        payrollTxn.getSupportingDocs(), user.getEmail(), suppDoc, user, entityManager));
                if (txnRmarks != null && !txnRmarks.equals("")) {
                    if (payrollTxn.getRemarks() != null) {
                        payrollTxn.setRemarks(payrollTxn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
                    } else {
                        payrollTxn.setRemarks(user.getEmail() + "#" + txnRmarks);
                    }
                }
            }
        }

        if (!payrollTxn.getTransactionStatus().equals("Rejected")) {
            if (selectedApproverAction.equals("2")) {
                payrollTxn.setTransactionStatus("Rejected");
                payrollTxn.setModifiedBy(user);
                payrollTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(
                        payrollTxn.getSupportingDocs(), user.getEmail(), suppDoc, user, entityManager));
                if (txnRmarks != null && !txnRmarks.equals("")) {
                    if (payrollTxn.getRemarks() != null) {
                        payrollTxn.setRemarks(payrollTxn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
                    } else {
                        payrollTxn.setRemarks(user.getEmail() + "#" + txnRmarks);
                    }
                }
            }
        }

        if (!payrollTxn.getTransactionStatus().equals("Require Additional Approval")) {
            if (selectedApproverAction.equals("3")) {
                payrollTxn.setTransactionStatus("Require Additional Approval");
                payrollTxn.setModifiedBy(user);
                payrollTxn.setApproverActionBy(user);
                String selectedAddApproverEmail = json.findValue("selectedAddApproverEmail").asText();
                payrollTxn.setSelectedAdditionalApprover(selectedAddApproverEmail);
                payrollTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(
                        payrollTxn.getSupportingDocs(), user.getEmail(), suppDoc, user, entityManager));
                if (txnRmarks != null && !txnRmarks.equals("")) {
                    if (payrollTxn.getRemarks() != null) {
                        payrollTxn.setRemarks(payrollTxn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
                    } else {
                        payrollTxn.setRemarks(user.getEmail() + "#" + txnRmarks);
                    }
                }
            }
        }

        if (selectedApproverAction.equals("4")) {
            if (!payrollTxn.getTransactionStatus().equals("Accounted")) {
                BranchCashService branchCashService = new BranchCashServiceImpl();
                BranchBankService branchBankService = new BranchBankServiceImpl();
                TransactionPurpose transactionPurpose = TransactionPurpose.findById(IdosConstants.PROCESS_PAYROLL);
                int paymentOption = json.findValue("paymentDetails") != null ? json.findValue("paymentDetails").asInt()
                        : 0;
                String selectedTransactionBranch = json.findValue("selectedTransactionBranch") != null
                        ? json.findValue("selectedTransactionBranch").asText()
                        : "";
                Branch branchObj = Branch.findById(new Long(selectedTransactionBranch));
                StringBuilder sbr = new StringBuilder(
                        "select obj from BranchCashCount obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.presentStatus=1");
                ArrayList inparams = new ArrayList();
                inparams.add(user.getOrganization().getId());
                inparams.add(branchObj.getId());
                List<BranchCashCount> branchCashBalance = genericDao.queryWithParams(sbr.toString(), entityManager,
                        inparams);
                Double branchCashBal = branchCashBalance.get(0).getResultantCash();
                if (payrollTxn.getTotalNetPay() > branchCashBal) {
                    return 0;
                }
                payrollTxn.setTransactionStatus("Accounted");
                payrollTxn.setModifiedBy(user);
                payrollTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(
                        payrollTxn.getSupportingDocs(), user.getEmail(), suppDoc, user, entityManager));
                if (txnRmarks != null && !txnRmarks.equals("")) {
                    if (payrollTxn.getRemarks() != null) {
                        payrollTxn.setRemarks(payrollTxn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
                    } else {
                        payrollTxn.setRemarks(user.getEmail() + "#" + txnRmarks);
                    }
                }
                if (IdosConstants.PAYMODE_CASH == paymentOption) {
                    branchCashService.updateBranchCashDetail(entityManager, user, branchObj,
                            payrollTxn.getTotalNetPay(), false, payrollTxn.getTransactionDate(), result);
                    payrollTxn.setPayMode("CASH");
                    TrialBalanceBranchCash trialBalCash = new TrialBalanceBranchCash(); // will affect only for sell on
                                                                                        // cash
                    trialBalCash.setTransactionId(payrollTxn.getId());
                    trialBalCash.setTransactionPurpose(transactionPurpose);
                    // trialBalCash.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(payrollDate));
                    trialBalCash.setDate(payrollTxn.getTransactionDate());
                    trialBalCash.setBranch(branchObj);
                    trialBalCash.setOrganization(user.getOrganization());
                    trialBalCash.setCashType(new Integer(IdosConstants.PAYMODE_CASH));
                    trialBalCash.setCreditAmount(payrollTxn.getTotalNetPay());
                    if (!user.getBranch().getBranchDepositKeys().isEmpty()) {
                        trialBalCash.setBranchDepositBoxKey(user.getBranch().getBranchDepositKeys().get(0));
                    }
                    genericDao.saveOrUpdate(trialBalCash, user, entityManager);

                } else if (IdosConstants.PAYMODE_BANK == paymentOption) {
                    String txnreceiptPaymentBank = json.findValue("txnPaymentBank") != null
                            ? json.findValue("txnPaymentBank").asText()
                            : null;
                    String txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
                            ? json.findValue("txnInstrumentNum").asText()
                            : "";
                    String txnInstrumentDate = json.findValue("txnInstrumentDate") != null
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
                        payrollTxn.setPayMode("BANK");
                        payrollTxn.setInstrumentNumber(txnInstrumentNumber);
                        payrollTxn.setInstrumentDate(txnInstrumentDate);
                        payrollTxn.setTransactionBranchBankAccount(bankAccount);
                        if (txnInstrumentNumber != null && !"".equals(txnInstrumentNumber)) {
                            payrollTxn.setInstrumentNumber(txnInstrumentNumber);
                        }
                        if (txnInstrumentDate != null && !"".equals(txnInstrumentDate)) {
                            payrollTxn.setInstrumentDate(txnInstrumentDate);
                        }
                        boolean branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(
                                entityManager, user, bankAccount, payrollTxn.getTotalNetPay(), true, result,
                                payrollTxn.getTransactionDate(), payrollTxn.getBranch());
                        if (!branchBankDetailEntered) {
                            return 2; // since balance is in -ve don't make any changes in DB
                        }
                        TrialBalanceBranchBank trialBalBank = new TrialBalanceBranchBank(); // will affect only for sell
                                                                                            // on cash if bank check or
                                                                                            // DD
                        trialBalBank.setTransactionId(payrollTxn.getId());
                        trialBalBank.setTransactionPurpose(transactionPurpose);
                        // trialBalBank.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(payrollDate));
                        trialBalBank.setDate(payrollTxn.getTransactionDate());
                        trialBalBank.setBranch(branchObj);
                        if (payrollTxn.getTransactionBranchBankAccount() == null) {
                            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                                    "Bank account is missing", IdosConstants.RECORD_NOT_FOUND);
                        }
                        trialBalBank.setBranchBankAccounts(payrollTxn.getTransactionBranchBankAccount());
                        trialBalBank.setOrganization(user.getOrganization());
                        trialBalBank.setCreditAmount(payrollTxn.getTotalNetPay());
                        genericDao.saveOrUpdate(trialBalBank, user, entityManager);

                    }
                }
                genericDao.saveOrUpdate(payrollTxn, user, entityManager); // save instru no, date etc
                // earnings

                if (payrollTxn.getEarning1Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getEarning1Id())));
                    tBPayroll.setDebitAmount(payrollTxn.getTotalEarning1());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }
                if (payrollTxn.getEarning2Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getEarning2Id())));
                    tBPayroll.setDebitAmount(payrollTxn.getTotalEarning2());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }
                if (payrollTxn.getEarning3Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getEarning3Id())));
                    tBPayroll.setDebitAmount(payrollTxn.getTotalEarning3());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }
                if (payrollTxn.getEarning4Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getEarning4Id())));
                    tBPayroll.setDebitAmount(payrollTxn.getTotalEarning4());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }
                if (payrollTxn.getEarning5Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getEarning5Id())));
                    tBPayroll.setDebitAmount(payrollTxn.getTotalEarning5());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }
                if (payrollTxn.getEarning6Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getEarning6Id())));
                    tBPayroll.setDebitAmount(payrollTxn.getTotalEarning6());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }
                if (payrollTxn.getEarning7Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getEarning7Id())));
                    tBPayroll.setDebitAmount(payrollTxn.getTotalEarning7());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }

                // deductions
                if (payrollTxn.getDeduction1Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getDeduction1Id())));
                    tBPayroll.setCreditAmount(payrollTxn.getTotalDeduction1());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }

                if (payrollTxn.getDeduction2Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getDeduction2Id())));
                    tBPayroll.setCreditAmount(payrollTxn.getTotalDeduction2());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }

                if (payrollTxn.getDeduction3Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getDeduction3Id())));
                    tBPayroll.setCreditAmount(payrollTxn.getTotalDeduction3());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }

                if (payrollTxn.getDeduction4Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getDeduction4Id())));
                    tBPayroll.setCreditAmount(payrollTxn.getTotalDeduction4());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }

                if (payrollTxn.getDeduction5Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getDeduction5Id())));
                    tBPayroll.setCreditAmount(payrollTxn.getTotalDeduction5());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }

                if (payrollTxn.getDeduction6Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getDeduction6Id())));
                    tBPayroll.setCreditAmount(payrollTxn.getTotalDeduction6());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }

                if (payrollTxn.getDeduction7Id() != null) {
                    TrialBalancePayrollItem tBPayroll = new TrialBalancePayrollItem();
                    tBPayroll.setOrganization(user.getOrganization());
                    tBPayroll.setBranch(branchObj);
                    tBPayroll.setTransactionId(payrollTxn.getId());
                    tBPayroll.setUser(user);
                    tBPayroll.setTransactionPurpose(transactionPurpose);
                    tBPayroll.setDate(payrollTxn.getTransactionDate());
                    tBPayroll.setPayrollItem(PayrollSetup.findById(new Long(payrollTxn.getDeduction7Id())));
                    tBPayroll.setCreditAmount(payrollTxn.getTotalDeduction7());
                    genericDao.saveOrUpdate(tBPayroll, user, entityManager);
                }

            }

        }

        if (!payrollTxn.getTransactionStatus().equals("Require Clarification")) {
            if (selectedApproverAction.equals("5")) {
                payrollTxn.setTransactionStatus("Require Clarification");
                payrollTxn.setModifiedBy(user);
                payrollTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(
                        payrollTxn.getSupportingDocs(), user.getEmail(), suppDoc, user, entityManager));
                if (txnRmarks != null && !txnRmarks.equals("")) {
                    if (payrollTxn.getRemarks() != null) {
                        payrollTxn.setRemarks(payrollTxn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
                    } else {
                        payrollTxn.setRemarks(user.getEmail() + "#" + txnRmarks);
                    }
                }
            }
        }
        if (!payrollTxn.getTransactionStatus().equals("Clarified")) {
            if (selectedApproverAction.equals("6")) {
                payrollTxn.setTransactionStatus("Clarified");
                payrollTxn.setModifiedBy(user);
                payrollTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(
                        payrollTxn.getSupportingDocs(), user.getEmail(), suppDoc, user, entityManager));
                if (txnRmarks != null && !txnRmarks.equals("")) {
                    if (payrollTxn.getRemarks() != null) {
                        payrollTxn.setRemarks(payrollTxn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
                    } else {
                        payrollTxn.setRemarks(user.getEmail() + "#" + txnRmarks);
                    }
                }
            }
        }
        if (selectedApproverAction.equals("7")) {
            payrollTxn.setModifiedBy(user);
            payrollTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(payrollTxn.getSupportingDocs(),
                    user.getEmail(), suppDoc, user, entityManager));
            if (txnRmarks != null && !txnRmarks.equals("")) {
                if (payrollTxn.getRemarks() != null) {
                    payrollTxn.setRemarks(payrollTxn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
                } else {
                    payrollTxn.setRemarks(user.getEmail() + "#" + txnRmarks);
                }
            }
        }
        if (selectedApproverAction.equals("8")) {
            payrollTxn.setModifiedBy(user);
            payrollTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(payrollTxn.getSupportingDocs(),
                    user.getEmail(), suppDoc, user, entityManager));
            if (txnRmarks != null && !txnRmarks.equals("")) {
                if (payrollTxn.getRemarks() != null) {
                    payrollTxn.setRemarks(payrollTxn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
                } else {
                    payrollTxn.setRemarks(user.getEmail() + "#" + txnRmarks);
                }
            }
        }
        entitytransaction.commit();

        return 1;
    }

    @Override
    public void getPayrollTxnList(Users user, String roles, ArrayNode recordsArrayNode, EntityManager entityManager) {
        log.log(Level.FINE, ">>>> Start ");
        try {
            List<PayrollTransaction> userPayrollTransactionList = null;
            StringBuilder sbquery = null;
            // if role is only of creator
            if (roles.equals("CREATOR")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE obj.createdBy ='" + user.getId()
                        + "' and obj.organization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("APPROVER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE (obj.approverActionBy='" + user.getId()
                        + "' or LOCATE('" + user.getEmail()
                        + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='" + user.getEmail()
                        + "') and obj.organization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,APPROVER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE (obj.createdBy ='" + user.getId()
                        + "' or obj.approverActionBy='" + user.getId() + "' or LOCATE('" + user.getEmail()
                        + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='" + user.getEmail()
                        + "') and obj.organization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,ACCOUNTANT")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE obj.organization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,CASHIER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE obj.createdBy ='" + user.getId()
                        + "' and obj.organization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,APPROVER,ACCOUNTANT")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE obj.organization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,APPROVER,CASHIER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE (obj.createdBy ='" + user.getId()
                        + "' or obj.approverActionBy='" + user.getId() + "' or LOCATE('" + user.getEmail()
                        + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='" + user.getEmail()
                        + "') and obj.organization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,ACCOUNTANT,CASHIER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE obj.organization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("CREATOR,APPROVER,ACCOUNTANT,CASHIER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE obj.organization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("APPROVER,ACCOUNTANT")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE obj.organization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("APPROVER,CASHIER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE (obj.approverActionBy='" + user.getId()
                        + "' or LOCATE('" + user.getEmail()
                        + "',obj.approverEmails)>0 or obj.selectedAdditionalApprover='" + user.getEmail()
                        + "') and obj.organization='" + user.getOrganization().getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.equals("APPROVER,ACCOUNTANT,CASHIER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE obj.organization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.contains("CONTROLLER")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE obj.organization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.contains("ACCOUNTANT")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE obj.organization='"
                        + user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            if (roles.contains("AUDITOR")) {
                sbquery = new StringBuilder("");
                sbquery.append("select obj from PayrollTransaction obj WHERE obj.organization='"
                        + user.getOrganization().getId()
                        + "' and obj.transactionStatus = 'Approved' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
            }
            userPayrollTransactionList = genericDao.executeSimpleQueryWithLimit(sbquery.toString(), entityManager, 100);
            for (PayrollTransaction usrTxn : userPayrollTransactionList) {
                ObjectNode event = Json.newObject();
                event.put("userroles", roles);
                event.put("id", usrTxn.getId());
                if (usrTxn.getBranch() != null) {
                    event.put("branchId", usrTxn.getBranch().getId());
                    event.put("branchName", usrTxn.getBranch().getName());
                } else {
                    event.put("branchName", "");
                }
                event.put("projectName", "");
                // StringBuilder itemParentName= new StringBuilder("");
                // String itemName = getProvisionJournalEntryDetail(entityManager, usrTxn,
                // itemParentName);

                // event.put("itemName", itemName);
                // event.put("itemParentName", itemParentName.toString());
                event.put("budgetAvailable", "");
                event.put("budgetAvailableAmt", "");
                event.put("customerVendorName", "");
                log.log(Level.FINE, "usrTxn.getTransactionPurpose().getTransactionPurpose():::"
                        + usrTxn.getTransactionPurpose().getTransactionPurpose());
                event.put("transactionPurpose", usrTxn.getTransactionPurpose().getTransactionPurpose());

                if (usrTxn.getTransactionDate() != null) {
                    event.put("txnDate", IdosConstants.idosdf.format(usrTxn.getTransactionDate()));
                }
                String invoiceDate = "";
                String invoiceDateLabel = "";
                /*
                 * if(usrTxn.getReversalDate()!=null){
                 * invoiceDateLabel="REVERSAL DATE:";
                 * invoiceDate=IdosConstants.idosdf.format(usrTxn.getReversalDate());
                 * }
                 */
                event.put("invoiceDateLabel", invoiceDateLabel);
                event.put("invoiceDate", invoiceDate);

                event.put("paymentMode", usrTxn.getPayMode() == null ? "" : usrTxn.getPayMode());
                event.put("noOfUnit", "");
                event.put("unitPrice", "");
                event.put("workingDays", usrTxn.getPayDays());
                if (usrTxn.getTotalTotalIncome() != null) {
                    event.put("grossAmount", IdosConstants.decimalFormat.format(usrTxn.getTotalTotalIncome()));
                } else {
                    event.put("grossAmount", "");
                }
                if (usrTxn.getTotalTotalDeduction() != null) {
                    event.put("totalDeductions", IdosConstants.decimalFormat.format(usrTxn.getTotalTotalDeduction()));
                } else {
                    event.put("totalDeductions", "");
                }
                event.put("netAmount", IdosConstants.decimalFormat.format(usrTxn.getTotalNetPay()));
                String txnResultDesc = "";
                /*
                 * if(usrTxn.getPurpose()!=null && !usrTxn.getPurpose().equals("null")){
                 * txnResultDesc=usrTxn.getPurpose();
                 * }
                 */
                event.put("netAmtDesc", txnResultDesc);
                event.put("status", usrTxn.getTransactionStatus());
                event.put("createdBy", usrTxn.getCreatedBy().getEmail());
                if (usrTxn.getApproverActionBy() != null) {
                    event.put("approverLabel", "APPROVER:");
                    event.put("approverEmail", usrTxn.getApproverActionBy().getEmail());
                } else {
                    event.put("approverLabel", "");
                    event.put("approverEmail", "");
                }
                /*
                 * if(usrTxn.getSupportingDocuments()!=null){
                 * event.put("txnDocument", usrTxn.getSupportingDocuments());
                 * }else{
                 * event.put("txnDocument", "");
                 * }
                 * if(usrTxn.getTxnRemarks()!=null){
                 * event.put("txnRemarks", usrTxn.getTxnRemarks());
                 * }else{
                 * event.put("txnRemarks", "");
                 * }
                 */
                String txnSpecialStatus = "";
                event.put("txnSpecialStatus", txnSpecialStatus);
                event.put("roles", roles);
                event.put("useremail", user.getEmail());
                event.put("approverEmails", usrTxn.getApproverEmails());
                event.put("additionalapproverEmails", usrTxn.getAdditionalApproverEmails());
                event.put("selectedAdditionalApproval", usrTxn.getSelectedAdditionalApprover());
                event.put("instrumentNumber", usrTxn.getInstrumentNumber() == null ? "" : usrTxn.getInstrumentNumber());
                event.put("instrumentDate", usrTxn.getInstrumentDate() == null ? "" : usrTxn.getInstrumentDate());
                event.put("txnReferenceNo", usrTxn.getTransactionRefNumber());
                recordsArrayNode.add(event);
                log.log(Level.FINE, ">>>> End " + event);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
    }

    public void getTrialBalancePayrollEarnItems(EntityManager entityManager, Users user, Long headId, Date fromDate,
            Date toDate, Long branchid, ArrayNode itemTransData) {
        ArrayList inparams = new ArrayList();
        String valueQuery = "";
        inparams.add(user.getOrganization().getId());
        inparams.add(fromDate);
        inparams.add(toDate);
        if (branchid == 0) {
            valueQuery = "select obj from PayrollTransaction obj where obj.organization.id=?1 and (obj.transactionDate between ?2 and ?3) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1";
        } else {
            inparams.add(branchid);
            valueQuery = "select obj from PayrollTransaction obj where obj.organization.id=?1 and (obj.transactionDate between ?2 and ?3) and obj.branch.id=?4 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1";
        }
        // inparams.add("'Accounted'");
        List<PayrollTransaction> prTxnList = genericDao.queryWithParams(valueQuery, entityManager, inparams);

        for (PayrollTransaction prTxn : prTxnList) {
            ObjectNode row = Json.newObject();
            row.put("txnRef", prTxn.getTransactionRefNumber());
            row.put("tranDate", IdosConstants.idosdf.format(prTxn.getTransactionDate()));
            row.put("email", prTxn.getCreatedBy().getEmail());
            row.put("branchName", prTxn.getBranch().getName());
            row.put("transactionPurpose", prTxn.getTransactionPurpose().getTransactionPurpose());
            String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                    "October", "November", "December" };
            String particularsMonth = months[prTxn.getPayslipMonth() - 1];
            row.put("particulars", particularsMonth + ',' + prTxn.getPayslipYear().toString());
            row.put("paymode", prTxn.getPayMode());
            if (prTxn.getRemarks() != null) {
                row.put("remarks", prTxn.getRemarks());
            } else {
                row.put("remarks", "");
            }

            if (prTxn.getEarning1Id() != null) {
                if (prTxn.getEarning1Id() == Integer.parseInt(headId.toString())) {
                    row.put("debit", prTxn.getTotalEarning1());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getEarning2Id() != null) {
                if (prTxn.getEarning2Id() == Integer.parseInt(headId.toString())) {
                    row.put("debit", prTxn.getTotalEarning2());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getEarning3Id() != null) {
                if (prTxn.getEarning3Id() == Integer.parseInt(headId.toString())) {
                    row.put("debit", prTxn.getTotalEarning3());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getEarning4Id() != null) {
                if (prTxn.getEarning4Id() == Integer.parseInt(headId.toString())) {
                    row.put("debit", prTxn.getTotalEarning4());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getEarning5Id() != null) {
                if (prTxn.getEarning5Id() == Integer.parseInt(headId.toString())) {
                    row.put("debit", prTxn.getTotalEarning5());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getEarning6Id() != null) {
                if (prTxn.getEarning6Id() == Integer.parseInt(headId.toString())) {
                    row.put("debit", prTxn.getTotalEarning6());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getEarning7Id() != null) {
                if (prTxn.getEarning7Id() == Integer.parseInt(headId.toString())) {
                    row.put("debit", prTxn.getTotalEarning7());
                    itemTransData.add(row);
                }
            }
        }
    }

    public void getTrialBalancePayrollDeduItems(EntityManager entityManager, Users user, Long headId, Date fromDate,
            Date toDate, Long branchid, ArrayNode itemTransData) {

        ArrayList inparams = new ArrayList();

        String valueQuery = "";
        inparams.add(user.getOrganization().getId());
        inparams.add(fromDate);
        inparams.add(toDate);
        if (branchid == 0) {
            valueQuery = "select obj from PayrollTransaction obj where obj.organization.id=?1 and (obj.transactionDate between ?2 and ?3) and obj.transactionStatus = 'Accounted' and obj.presentStatus=1";
        } else {
            inparams.add(branchid);
            valueQuery = "select obj from PayrollTransaction obj where obj.organization.id=?1 and (obj.transactionDate between ?2 and ?3) and obj.branch.id=?4 and obj.transactionStatus = 'Accounted' and obj.presentStatus=1";
        }

        // inparams.add("'Accounted'");
        List<PayrollTransaction> prTxnList = genericDao.queryWithParams(valueQuery, entityManager, inparams);
        for (PayrollTransaction prTxn : prTxnList) {
            ObjectNode row = Json.newObject();
            row.put("txnRef", prTxn.getTransactionRefNumber());
            row.put("tranDate", IdosConstants.idosdf.format(prTxn.getTransactionDate()));
            row.put("email", prTxn.getCreatedBy().getEmail());
            row.put("branchName", prTxn.getBranch().getName());
            row.put("transactionPurpose", prTxn.getTransactionPurpose().getTransactionPurpose());
            String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                    "October", "November", "December" };
            String particularsMonth = months[prTxn.getPayslipMonth() - 1];
            row.put("particulars", particularsMonth + ',' + prTxn.getPayslipYear().toString());
            row.put("paymode", prTxn.getPayMode());
            if (prTxn.getRemarks() != null) {
                row.put("remarks", prTxn.getRemarks());
            } else {
                row.put("remarks", "");
            }
            if (prTxn.getDeduction1Id() != null) {
                if (prTxn.getDeduction1Id() == Integer.parseInt(headId.toString())) {
                    row.put("credit", prTxn.getTotalDeduction1());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getDeduction2Id() != null) {
                if (prTxn.getDeduction2Id() == Integer.parseInt(headId.toString())) {
                    row.put("credit", prTxn.getTotalDeduction2());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getDeduction3Id() != null) {
                if (prTxn.getDeduction3Id() == Integer.parseInt(headId.toString())) {
                    row.put("credit", prTxn.getTotalDeduction3());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getDeduction4Id() != null) {
                if (prTxn.getDeduction4Id() == Integer.parseInt(headId.toString())) {
                    row.put("credit", prTxn.getTotalDeduction4());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getDeduction5Id() != null) {
                if (prTxn.getDeduction5Id() == Integer.parseInt(headId.toString())) {
                    row.put("credit", prTxn.getTotalDeduction5());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getDeduction6Id() != null) {
                if (prTxn.getDeduction6Id() == Integer.parseInt(headId.toString())) {
                    row.put("credit", prTxn.getTotalDeduction6());
                    itemTransData.add(row);
                }
            }
            if (prTxn.getDeduction7Id() != null) {
                if (prTxn.getDeduction7Id() == Integer.parseInt(headId.toString())) {
                    row.put("credit", prTxn.getTotalDeduction7());
                    itemTransData.add(row);
                }
            }
        }
    }

    public ObjectNode fetchPayrollTransactionDetails(EntityManager entityManager, Users user, Long transactionID,
            Double debitAmount, Double creditAmount, Long transPurposeID, Long headId) {
        ObjectNode row = null;
        PayrollTransaction transaction = PayrollTransaction.findById(transactionID);
        if (transaction == null) {
            return row;
        }
        row = Json.newObject();
        if (transaction.getTransactionRefNumber() != null)
            row.put("txnRef", transaction.getTransactionRefNumber());
        else
            row.put("txnRef", "");
        row.put("tranDate", IdosConstants.idosdf.format(transaction.getTransactionDate()));
        row.put("email", transaction.getCreatedBy().getEmail());
        row.put("branchName", transaction.getBranch().getName());

        row.put("projectName", "");
        row.put("typeOfSupply", "");

        row.put("custVendName", "");
        row.put("placeOfSupply", "");
        row.put("poRef", "");
        row.put("transactionPurpose", transaction.getTransactionPurpose().getTransactionPurpose());

        row.put("paymode", "");
        String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December" };
        String particularsMonth = months[transaction.getPayslipMonth() - 1];
        row.put("particulars", particularsMonth + ',' + transaction.getPayslipYear().toString());

        if (creditAmount != null)
            row.put("credit", IdosConstants.decimalFormat.format(creditAmount));
        else
            row.put("credit", "0.00");
        if (debitAmount != null)
            row.put("debit", IdosConstants.decimalFormat.format(debitAmount));
        else
            row.put("debit", "0.00");

        if (transaction.getRemarks() != null)
            row.put("remarks", transaction.getRemarks());
        else
            row.put("remarks", "");

        StringBuilder debitItems = new StringBuilder("");
        StringBuilder creditItems = new StringBuilder("");
        row.put("itemName", debitItems.toString());
        row.put("creditItemsName", creditItems.toString());
        row.put("invoiceDate", "");
        row.put("invoiceNo", "");
        row.put("grnDate", "");
        row.put("grnNo", "");
        row.put("impDate", "");
        row.put("impNo", "");
        row.put("taxRate", "");
        row.put("cessRate", "");
        row.put("typeOfSupply", "");
        row.put("hsnSac", "");
        row.put("refNo", "");
        row.put("placeOfSupply", "");
        row.put("poRef", "");
        return row;
    }
}
