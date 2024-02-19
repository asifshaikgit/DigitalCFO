package com.idos.dao;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IdosConstants;
import model.Branch;
import model.BranchBankAccountBalance;
//import model.BranchBankAccountMapping;
import model.BranchBankAccounts;
import model.Transaction;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;

public class BranchBankDAOImpl implements BranchBankDAO {
    @Override
    public ObjectNode branchBank(ObjectNode result, JsonNode json, Users user, EntityManager entityManager) {
        log.log(Level.FINE, ">> Start ");
        result.put("result", false);
        ArrayNode branchBankListan = result.putArray("branchBankList");
        ArrayNode cashierKlDataan = result.putArray("cashierKlData");
        String branchId = json.findValue("branchId") != null ? json.findValue("branchId").asText() : null;
        String branchSelectId = json.findValue("branchSelectId") != null ? json.findValue("branchSelectId").asText()
                : null;
        if (branchId != null && !branchId.equals("")) {
            Branch branch = Branch.findById(Long.parseLong(branchId));
            List<BranchBankAccounts> branchBankAccounts = branch.getBranchBankAccounts();
            for (BranchBankAccounts bnchBankAccount : branchBankAccounts) {
                result.put("result", true);
                ObjectNode row = Json.newObject();
                row.put("id", bnchBankAccount.getId());
                row.put("bankName", bnchBankAccount.getBankName());
                row.put("bankNumber", bnchBankAccount.getAccountNumber());
                row.put("branchSelectId", branchSelectId);
                StringBuilder sbr = new StringBuilder("");
                sbr.append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='" + branch.getId()
                        + "' AND obj.organization.id='" + branch.getOrganization().getId()
                        + "' and obj.branchBankAccounts.id='" + bnchBankAccount.getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                List<BranchBankAccountBalance> bnchBnkActBalanceList = genericDao.executeSimpleQuery(sbr.toString(),
                        entityManager);
                BranchBankAccountBalance bnchBnkActBal = null;
                if (bnchBnkActBalanceList.size() > 0 && !bnchBnkActBalanceList.isEmpty()) {
                    bnchBnkActBal = bnchBnkActBalanceList.get(0);
                }
                sbr.delete(0, sbr.length());
                sbr.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization='"
                        + branch.getOrganization().getId() + "' and obj.transactionBranchBankAccount='"
                        + bnchBankAccount.getId()
                        + "' AND (obj.transactionPurpose=22 or obj.transactionPurpose=24) and obj.presentStatus=1 and obj.transactionStatus!='Accounted' AND obj.transactionStatus!='Rejected'");
                List<Transaction> bnkTxnInProgressList = genericDao.executeSimpleQuery(sbr.toString(), entityManager);
                Double progressAmt = 0.0;
                if (bnkTxnInProgressList.size() > 0) {
                    Object val = bnkTxnInProgressList.get(0);
                    if (val != null) {
                        progressAmt = Double.parseDouble(String.valueOf(val));
                    }
                }
                if (bnchBnkActBal != null) {
                    row.put("bankAmount",
                            IdosConstants.decimalFormat.format(bnchBnkActBal.getResultantCash() - progressAmt));
                }
                branchBankListan.add(row);
            }
        }
        log.log(Level.FINE, ">> end " + result);
        return result;
    }

    @Override
    public ObjectNode branchBankDetails(ObjectNode result, JsonNode json, Users user, EntityManager entityManager) {
        log.log(Level.FINE, ">> Start ");
        result.put("result", false);
        ArrayNode branchBankDetailsan = result.putArray("branchBankDetails");
        String bankId = json.findValue("bankId") != null ? json.findValue("bankId").asText() : null;
        String bankSelectId = json.findValue("bankSelectId") != null ? json.findValue("bankSelectId").asText() : null;
        if (bankId != null && !bankId.equals("")) {
            BranchBankAccounts branchBankAccount = BranchBankAccounts.findById(Long.parseLong(bankId));
            if (branchBankAccount != null) {
                result.put("result", true);
                ObjectNode row = Json.newObject();
                row.put("id", branchBankAccount.getId());
                row.put("bankName", branchBankAccount.getBankName());
                row.put("bankNumber", branchBankAccount.getAccountNumber());
                row.put("bankSelectId", bankSelectId);
                if (branchBankAccount.getAccountType() == 1 || branchBankAccount.getAccountType() == 5
                        || branchBankAccount.getAccountType() == 6 || branchBankAccount.getAccountType() == 7
                        || branchBankAccount.getAccountType() == 9 || branchBankAccount.getAccountType() == 10
                        || branchBankAccount.getAccountType() == 11 || branchBankAccount.getAccountType() == 12) { // balance
                                                                                                                   // -ve
                                                                                                                   // is
                                                                                                                   // allowed
                    row.put("branchTypeAllowsNegBal", true);
                } else {
                    row.put("branchTypeAllowsNegBal", false);
                }
                StringBuilder sbr = new StringBuilder("");
                sbr.append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='"
                        + branchBankAccount.getBranch().getId() + "' AND obj.organization.id='"
                        + branchBankAccount.getBranch().getOrganization().getId() + "' and obj.branchBankAccounts.id='"
                        + branchBankAccount.getId() + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                List<BranchBankAccountBalance> bnchBnkActBalanceList = genericDao.executeSimpleQuery(sbr.toString(),
                        entityManager);
                BranchBankAccountBalance bnchBnkActBal = null;
                if (bnchBnkActBalanceList.size() > 0 && !bnchBnkActBalanceList.isEmpty()) {
                    bnchBnkActBal = bnchBnkActBalanceList.get(0);
                }
                sbr.delete(0, sbr.length());
                sbr.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization='"
                        + branchBankAccount.getBranch().getOrganization().getId()
                        + "' and obj.transactionBranchBankAccount='" + branchBankAccount.getId()
                        + "' AND (obj.transactionPurpose=22 or obj.transactionPurpose=24) and obj.presentStatus=1 and obj.transactionStatus!='Accounted' AND obj.transactionStatus!='Rejected'");
                List<Transaction> bnkTxnInProgressList = genericDao.executeSimpleQuery(sbr.toString(), entityManager);
                Double progressAmt = 0.0;
                if (bnkTxnInProgressList.size() > 0) {
                    Object val = bnkTxnInProgressList.get(0);
                    if (val != null) {
                        progressAmt = Double.parseDouble(String.valueOf(val));
                    }
                }
                if (bnchBnkActBal != null) {
                    row.put("bankAmount",
                            IdosConstants.decimalFormat.format(bnchBnkActBal.getResultantCash() - progressAmt));
                }
                branchBankDetailsan.add(row);
            }
        }
        log.log(Level.FINE, ">> end ");
        return result;
    }

    @Override
    public boolean getOrganizationBanks(ObjectNode result, Users user, EntityManager em) {
        String sql = "from BranchBankAccounts t1 where t1.organization.id= ?1 and presentStatus=1";
        ArrayList inparams = new ArrayList(3);
        inparams.add(user.getOrganization().getId());
        ArrayNode bankAccounts = result.putArray("bankAccounts");
        List<BranchBankAccounts> list = genericDao.queryWithParamsName(sql, em, inparams);
        for (BranchBankAccounts bankAccount : list) {
            ObjectNode row = Json.newObject();
            // String branchName = bankAccount.getBranch().getName();
            String bankName = bankAccount.getBankName();
            // bankName += "-" + branchName;
            row.put("bankName", bankName);
            row.put("bankid", bankAccount.getId());
            bankAccounts.add(row);
        }
        return true;
    }
}
