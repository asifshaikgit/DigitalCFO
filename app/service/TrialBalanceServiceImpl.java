package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.idos.dao.GenericDAO;
import com.idos.util.*;
import model.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import play.libs.Json;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class TrialBalanceServiceImpl implements TrialBalanceService {

    public static EntityManager em;

    @Override
    public void insertTrialBalance(Transaction transaction, Users user, EntityManager em) throws IDOSException {
        CREATE_TRIAL_BALANCE_DAO.insertTrialBalance(transaction, user, em);
    }

    @Override
    public String downloadTrialBalance(ObjectNode result, JsonNode json, Users user, EntityManager em, String path) {
        return trialBalanceDao.downloadTrialBalance(result, json, user, em, path);
    }

    @Override
    public List<TrialBalance> displayTrialBalance(ObjectNode result, JsonNode json, Users user, EntityManager em)
            throws IDOSException {
        return trialBalanceDao.displayTrialBalance(result, json, user, em);
    }

    @Override
    public ObjectNode exportTrialBalancePDF(String fromDate, String toDate, Users user, long branchId) {
        log.log(Level.FINE, "============Start");
        ObjectNode result = Json.newObject();
        ArrayNode an = result.putArray("trialBalanceFileCred");
        result.put("result", false);
        result.put("message", "Oops! Something went wrong. Please try again later.");
        if (null == fromDate || "".equals(fromDate)) {
            result.put("message", "From date required!");
        } else if (null == toDate || "".equals(toDate)) {
            result.put("message", "To date required!");
        } else {
            Branch branch = null;
            if (branchId > 0) {
                branch = Branch.findById(branchId);
            }
            String fileName = trialBalanceDao.exportTrialBalancePDF(fromDate, toDate, user, branch);
            // result.put("fileName", "/assets/TrialBalance/"+fileName);
            result.put("result", true);
            result.remove("message");
            ObjectNode datarow = Json.newObject();
            datarow.put("fileName", fileName);
            an.add(datarow);
        }
        return result;
    }

    @Override
    public void addTrialBalanceForCash(Users user, EntityManager em, GenericDAO genericDAO,
            ClaimTransaction claimTransaction, Double amount, boolean isCredit) {
        TrialBalanceBranchCash trialBalCash = new TrialBalanceBranchCash();
        trialBalCash.setTransactionId(claimTransaction.getId());
        trialBalCash.setTransactionPurpose(claimTransaction.getTransactionPurpose());
        trialBalCash.setDate(claimTransaction.getTransactionDate());
        trialBalCash.setBranch(claimTransaction.getTransactionBranch());
        trialBalCash.setOrganization(claimTransaction.getTransactionBranchOrganization());
        if (isCredit) {
            trialBalCash.setCreditAmount(amount);
        } else {
            trialBalCash.setDebitAmount(amount);
        }
        if (!claimTransaction.getTransactionBranch().getBranchDepositKeys().isEmpty()) {
            trialBalCash.setBranchDepositBoxKey(claimTransaction.getTransactionBranch().getBranchDepositKeys().get(0));
        }
        trialBalCash.setCashType(new Integer(IdosConstants.CASH));
        genericDAO.saveOrUpdate(trialBalCash, user, em);
    }

    @Override
    public void addTrialBalanceForBank(Users user, EntityManager em, GenericDAO genericDAO,
            ClaimTransaction claimTransaction, Double amount, boolean isCredit) {
        TrialBalanceBranchBank trialBalBank = new TrialBalanceBranchBank();
        trialBalBank.setTransactionId(claimTransaction.getId());
        trialBalBank.setTransactionPurpose(claimTransaction.getTransactionPurpose());
        trialBalBank.setDate(claimTransaction.getTransactionDate());
        trialBalBank.setBranch(claimTransaction.getTransactionBranch());
        trialBalBank.setOrganization(claimTransaction.getTransactionBranchOrganization());
        if (isCredit) {
            trialBalBank.setCreditAmount(amount);
        } else {
            trialBalBank.setDebitAmount(amount);
        }
        trialBalBank.setBranchBankAccounts(claimTransaction.getTransactionBranchBankAccount());
        genericDAO.saveOrUpdate(trialBalBank, user, em);
    }

    @Override
    public void addTrialBalanceCOAItems(Users user, EntityManager em, GenericDAO genericDAO,
            ClaimTransaction claimTransaction, Double amount, Specifics specifics, boolean isCredit)
            throws IDOSException {
        if (specifics == null) {
            throw new IDOSException(IdosConstants.TB_EXCEPTION_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                    "Specific is null", "Can't store TB for the Claim");
        }
        TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems(); // for sell on cash and credit both
        trialBalCOA.setTransactionId(claimTransaction.getId());
        trialBalCOA.setTransactionPurpose(claimTransaction.getTransactionPurpose());
        trialBalCOA.setTransactionSpecifics(specifics);
        trialBalCOA.setTransactionParticulars(specifics.getParticularsId());
        trialBalCOA.setDate(claimTransaction.getTransactionDate());
        trialBalCOA.setBranch(claimTransaction.getTransactionBranch());
        trialBalCOA.setOrganization(claimTransaction.getTransactionBranchOrganization());
        if (isCredit) {
            trialBalCOA.setCreditAmount(amount);
        } else {
            trialBalCOA.setDebitAmount(amount);
        }
        genericDAO.saveOrUpdate(trialBalCOA, user, em);
    }

    @Override
    public ObjectNode getTransactionForHead(Users user, EntityManager em, JsonNode json) throws IDOSException {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "======= Start " + json);
        ObjectNode result = Json.newObject();
        com.fasterxml.jackson.databind.node.ArrayNode itemTransData = jsonObjectMapper.createArrayNode(); // result.putArray("itemTransData");
        Long headId = json.findValue("specificid") != null ? json.findValue("specificid").asLong() : 0L;
        Long headid2 = json.findValue("headid2") != null ? json.findValue("headid2").asLong() : 0L;
        int mappingID = json.findValue("identForDataValid") != null ? json.findValue("identForDataValid").asInt() : 0;
        String fmDate = json.findValue("fromDate") != null ? json.findValue("fromDate").asText() : null;
        String tDate = json.findValue("toDate") != null ? json.findValue("toDate").asText() : null;
        String headType = json.findValue("headType") != null ? json.findValue("headType").asText() : null;
        String toplevelaccountcode = json.findValue("toplevelaccountcode") != null
                ? json.findValue("toplevelaccountcode").asText()
                : null;
        Long branchid = json.findValue("trialBalBranch") == null ? 0L : json.findValue("trialBalBranch").asLong();
        String fromDate = null;
        String toDate = null;
        Date fromDateDt = null;
        Date toDateDt = null;
        String period = null;
        Specifics specifics = null;
        if (headId != null && headType.equalsIgnoreCase(IdosConstants.HEAD_SPECIFIC)) {
            specifics = Specifics.findById(headId);
            if (mappingID == 0) {
                if (specifics != null) {
                    if (specifics.getIdentificationForDataValid() != null
                            && !specifics.getIdentificationForDataValid().equals("")) {
                        mappingID = Integer.parseInt(specifics.getIdentificationForDataValid());
                    }
                }
            }
        }
        // 214 - TDS ledger download - TDS Receivables save as xlsx & pdf issue fix -
        // begin
        if (mappingID == 0 && headId != null && headType.equalsIgnoreCase(IdosConstants.HEAD_TDS)) {
            specifics = Specifics.findById(headId);
            if (specifics != null && specifics.getIdentificationForDataValid() != null
                    && !specifics.getIdentificationForDataValid().equals("")) {
                mappingID = Integer.parseInt(specifics.getIdentificationForDataValid());
            }
        }
        // 214 - TDS ledger download - TDS Receivables save as xlsx & pdf issue fix -
        // end
        try {
            if (fmDate != null && !fmDate.equals("")) {
                fromDateDt = IdosConstants.IDOSDF.parse(fmDate);
                fromDate = IdosConstants.MYSQLDF.format(fromDateDt);
                fromDateDt = IdosConstants.MYSQLDF.parse(fromDate);
                period = IdosConstants.IDOSDF.format(fromDateDt);
            } else {
                fromDate = DateUtil.getCurrentFinacialStartDate(user.getOrganization().getFinancialStartDate());
                fromDateDt = IdosConstants.MYSQLDF.parse(fromDate);
                period = IdosConstants.IDOSDF.format(fromDateDt);
            }

            if (tDate != null && !tDate.equals("")) {
                toDateDt = IdosConstants.IDOSDF.parse(tDate);
                toDate = IdosConstants.MYSQLDF.format(toDateDt);
                toDateDt = IdosConstants.MYSQLDF.parse(toDate);
                period = period + " to " + IdosConstants.IDOSDF.format(toDateDt);
            } else {
                toDateDt = Calendar.getInstance().getTime();
                toDate = IdosConstants.MYSQLDF.format(toDateDt);
                period = period + " to " + IdosConstants.IDOSDF.format(toDateDt);
            }
            // period = DateUtil.idosdf.format(IdosConstants.idosdf.parse(fromDate));
            // period = period + " to " +
            // DateUtil.idosdf.format(IdosConstants.idosdf.parse(toDate));
        } catch (ParseException | NumberFormatException ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
                    "invalid date format", ex.getMessage());
        }

        result.put("period", period);
        if (toplevelaccountcode != null
                && (toplevelaccountcode.startsWith("1") || toplevelaccountcode.startsWith("2"))) {
            if (headType != null && headType.equalsIgnoreCase(IdosConstants.HEAD_PAYROLL_EXPENSE)) {
                payrollDAO.getTrialBalancePayrollEarnItems(em, user, headId, fromDateDt, toDateDt, branchid,
                        itemTransData);
            } else if (IdosConstants.HEAD_SPECIFIC.equals(headType)) {
                if (toplevelaccountcode.startsWith("1")) {
                    TRIAL_BALANCE_LEGDER_DAO.getTransactionsForIncomeExpenseCOAItems(em, user, headType, headId,
                            headid2, fromDateDt, toDateDt, branchid, itemTransData, IdosConstants.INCOME);
                } else {
                    TRIAL_BALANCE_LEGDER_DAO.getTransactionsForIncomeExpenseCOAItems(em, user, headType, headId,
                            headid2, fromDateDt, toDateDt, branchid, itemTransData, IdosConstants.EXPENSE);
                }
            }
        } else if (toplevelaccountcode != null && toplevelaccountcode.startsWith("3")) {
            if (headType != null && !"".equals(headType) && !IdosConstants.HEAD_SPECIFIC.equals(headType)) {
                TRIAL_BALANCE_LEGDER_DAO.getMappedItemsTransactionDetails(IdosConstants.ASSETS, headType, em, user,
                        branchid, headId, headid2, fromDateDt, toDateDt, itemTransData, mappingID);
            } else if (mappingID != 0 && IdosConstants.HEAD_SPECIFIC.equals(headType)) {
                TRIAL_BALANCE_LEGDER_DAO.getTransactionsForIncomeExpenseCOAItems(em, user, headType, headId, headid2,
                        fromDateDt, toDateDt, branchid, itemTransData, IdosConstants.ASSETS);
                TRIAL_BALANCE_LEGDER_DAO.getMappedItemsTransactionDetails(IdosConstants.ASSETS, headType, em, user,
                        branchid, headId, headid2, fromDateDt, toDateDt, itemTransData, mappingID);
            } else if (mappingID == 0 && IdosConstants.HEAD_SPECIFIC.equals(headType)) {
                TRIAL_BALANCE_LEGDER_DAO.getTransactionsForCoaChildItems(em, user, headType, headId, headid2,
                        fromDateDt, toDateDt, branchid, itemTransData, IdosConstants.ASSETS);
            }
        } else if (toplevelaccountcode != null && toplevelaccountcode.startsWith("4")) {
            if (headType != null && !"".equals(headType) && !IdosConstants.HEAD_SPECIFIC.equals(headType)) {
                TRIAL_BALANCE_LEGDER_DAO.getMappedItemsTransactionDetails(IdosConstants.LIABILITIES, headType, em, user,
                        branchid, headId, headid2, fromDateDt, toDateDt, itemTransData, mappingID);
            } else if (mappingID != 0 && IdosConstants.HEAD_SPECIFIC.equals(headType)) {
                TRIAL_BALANCE_LEGDER_DAO.getTransactionsForIncomeExpenseCOAItems(em, user, headType, headId, headid2,
                        fromDateDt, toDateDt, branchid, itemTransData, IdosConstants.LIABILITIES);
                TRIAL_BALANCE_LEGDER_DAO.getMappedItemsTransactionDetails(IdosConstants.LIABILITIES, headType, em, user,
                        branchid, headId, headid2, fromDateDt, toDateDt, itemTransData, mappingID);
            } else if (mappingID == 0 && IdosConstants.HEAD_SPECIFIC.equals(headType)) {
                TRIAL_BALANCE_LEGDER_DAO.getTransactionsForCoaChildItems(em, user, headType, headId, headid2,
                        fromDateDt, toDateDt, branchid, itemTransData, IdosConstants.LIABILITIES);
            }
        }

        try {
            List<TrialBalanceLedgerReport> tbReportList = IdosUtil.convertArrayNodeToList(itemTransData);
            Collections.sort(tbReportList);
            // tbReportList = IdosUtil.removeDuplicates(tbReportList);
            // HashSet<TrialBalanceLedgerReport> newList = new HashSet(tbReportList);
            // Set<TrialBalanceLedgerReport> newList = new
            // LinkedHashSet<TrialBalanceLedgerReport>(tbReportList);
            ArrayNode array = jsonObjectMapper.valueToTree(tbReportList);
            result.put("itemTransData", array);
        } catch (IOException e) {
            log.log(Level.SEVERE, user.getEmail(), e);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Parsing error", e.getMessage());
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "======= End " + result);
        return result;
    }

    @Override
    public void fetchTxnGSTDetails(EntityManager em, Transaction txn, Users user, ObjectNode row) {
        TRIAL_BALANCE_LEGDER_DAO.fetchTxnGSTDetails(em, txn, user, row);
    }

    @Override
    public void getProvisionJournalEntryHeads(EntityManager em, IdosProvisionJournalEntry provisionJournalEntry,
            String headType, Long headID, StringBuilder itemName, StringBuilder creditItems) {
        TRIAL_BALANCE_LEGDER_DAO.getProvisionJournalEntryHeads(em, provisionJournalEntry, headType, headID, itemName,
                creditItems, new StringBuilder(), new StringBuilder(), 0);
    }

    @Override
    public void saveTrialBalInterBranch(Transaction transaction, Users user, Integer typeIdentifier, EntityManager em,
            boolean isCredit) {
        CREATE_TRIAL_BALANCE_DAO.saveTrialBalInterBranch(transaction, user, typeIdentifier, em, isCredit);
    }

    @Override
    public Boolean saveTrialBalanceForRoundOff(Organization org, Branch branch, Long txnId,
            TransactionPurpose txnPurpose, Date txnDate, Double roundOffAmount, Users user, EntityManager em,
            boolean isCredit) throws IDOSException {
        return CREATE_TRIAL_BALANCE_DAO.saveTrialBalanceForRoundOff(org, branch, txnId, txnPurpose, txnDate,
                roundOffAmount, user, em, isCredit);
    }

    private void getTrialBalance4AllMappedAssets(EntityManager em, Users user, Long branchId, Long headId, Long headid2,
            Date fromDate, Date toDate, ArrayNode itemTransData, int mappingID) throws IDOSException {
        if (mappingID == 3) {
            TRIAL_BALANCE_LEGDER_DAO.getTrialBalanceCashTrans(em, user, branchId, headId, fromDate, toDate,
                    IdosConstants.CASH, itemTransData, mappingID);
        } else if (mappingID == 30) {
            TRIAL_BALANCE_LEGDER_DAO.getTrialBalanceCashTrans(em, user, branchId, headId, fromDate, toDate,
                    IdosConstants.PETTY_CASH, itemTransData, mappingID);
        } else if (mappingID == 4) {
            TRIAL_BALANCE_LEGDER_DAO.getTrialBalanceBankTrans(em, user, branchId, headId, fromDate, toDate,
                    itemTransData, mappingID);
        }
    }
}