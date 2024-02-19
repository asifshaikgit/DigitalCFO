package com.idos.dao;

import java.io.FileOutputStream;
import java.util.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.idos.util.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import controllers.StaticController;
import model.Branch;
import model.BranchBankAccountBalance;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.BranchSpecifics;
import model.BranchVendors;
import model.ConfigParams;
import model.IdosProvisionJournalEntry;
import model.Organization;
import model.OrganizationGstinSerials;
import model.Project;
import model.ProvisionJournalEntryDetail;
import model.Specifics;
import model.SpecificsDocUploadMonetoryRuleForBranch;
import model.Transaction;
import model.TransactionPurpose;
import model.UserRightInBranch;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import model.VendorSpecific;
import play.db.jpa.JPAApi;
import play.libs.Json;
import service.ProvisionJournalEntryService;
import service.ProvisionJournalEntryServiceImpl;
import com.typesafe.config.ConfigFactory;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

public class TransactionDAOImpl implements TransactionDAO {
    public static JPAApi jpaApi;
    public static EntityManager entityManager;

    @Override
    public Transaction receiveSpecialAdjustmentsFromVendors(String txnPurpose, TransactionPurpose usertxnPurpose,
            Users user, String txnRSAAFVCreditVendor, String txnRSAAFVAmountReceived, String txnRSAAFVForProject,
            String txnreceiptdetails, String txnreceiptPaymentBank, String txnreceipttypebankdetails,
            String supportingdoc, String txnremarks, String klfollowednotfollowed, EntityTransaction entitytransaction,
            EntityManager em) throws IDOSException {
        Transaction transaction = new Transaction();
        Vendor vendor = Vendor.findById(Long.parseLong(txnRSAAFVCreditVendor));
        Double amountReceivedFromVendor = Double.valueOf(txnRSAAFVAmountReceived);
        // by default transaction branch will be vendor branch
        Branch txnBranch = null;
        Project txnproject = null;
        if ((vendor != null) && (vendor.getBranch() != null)) {
            txnBranch = vendor.getBranch();
        }
        vendorAccountAddDeductSpecialAdjustments(txnPurpose, user, vendor, amountReceivedFromVendor);
        if (txnRSAAFVForProject != null && !txnRSAAFVForProject.equals("")) {
            txnproject = Project.findById(Long.parseLong(txnRSAAFVForProject));
        }
        if ((txnreceiptdetails != null && !txnreceiptdetails.equals(""))
                && (txnreceiptdetails.equals("1"))) {
            // add amount to the branch cash in case receipt mode is cash
            branchCashAccountAddDeductSpecialAdjustments(txnPurpose, user, vendor, amountReceivedFromVendor);
        }
        if (txnreceiptdetails.equals("2")) {
            if (txnreceiptPaymentBank != null && !txnreceiptPaymentBank.equals("")) {
                Double creditAmount = null;
                Double debitAmount = null;
                Double resultantAmount = null;
                Double amountBalance = null;
                BranchBankAccounts bankAccount = BranchBankAccounts.findById(Long.parseLong(txnreceiptPaymentBank));
                if (bankAccount == null) {
                    throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                            IdosConstants.INVALID_DATA_EXCEPTION,
                            "Bank is not selected in transaction when payment mode is Bank.");
                }
                transaction.setTransactionBranchBankAccount(bankAccount);
                StringBuilder newsbquery = new StringBuilder("");
                newsbquery.append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='"
                        + vendor.getBranch().getId() + "' AND obj.organization.id='" + vendor.getOrganization().getId()
                        + "' and obj.branchBankAccounts.id='" + bankAccount.getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                List<BranchBankAccountBalance> branchBankAccountBal = genericDao
                        .executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
                if (branchBankAccountBal.size() > 0) {
                    if (branchBankAccountBal.get(0).getCreditAmount() == null) {
                        creditAmount = Double.parseDouble(txnRSAAFVAmountReceived);
                    }
                    if (branchBankAccountBal.get(0).getCreditAmount() != null) {
                        creditAmount = branchBankAccountBal.get(0).getCreditAmount()
                                + Double.parseDouble(txnRSAAFVAmountReceived);
                    }
                    if (branchBankAccountBal.get(0).getDebitAmount() == null) {
                        debitAmount = 0.0;
                    }
                    if (branchBankAccountBal.get(0).getDebitAmount() != null) {
                        debitAmount = branchBankAccountBal.get(0).getDebitAmount();
                    }
                    if (branchBankAccountBal.get(0).getAmountBalance() != null) {
                        amountBalance = branchBankAccountBal.get(0).getAmountBalance();
                    } else {
                        amountBalance = 0.0;
                    }
                    resultantAmount = amountBalance + creditAmount - debitAmount;
                    branchBankAccountBal.get(0).setCreditAmount(creditAmount);
                    branchBankAccountBal.get(0).setDebitAmount(debitAmount);
                    branchBankAccountBal.get(0).setResultantCash(resultantAmount);
                    genericDao.saveOrUpdate(branchBankAccountBal.get(0), user, entityManager);
                }
            }
        }
        transaction.setTransactionBranch(txnBranch);
        transaction.setTransactionProject(txnproject);
        transaction.setPaymentStatus("PAID");
        transaction.setTransactionPurpose(usertxnPurpose);
        transaction.setTransactionVendorCustomer(vendor);
        transaction.setNetAmount(amountReceivedFromVendor);
        String netDesc = "Special Adjustment Received:" + amountReceivedFromVendor;
        transaction.setNetAmountResultDescription(netDesc);
        transaction.setTransactionDate(Calendar.getInstance().getTime());
        transaction.setCustomerDuePayment(0.0);
        transaction.setCustomerNetPayment(amountReceivedFromVendor);
        transaction.setTransactionBranchOrganization(user.getOrganization());
        if (!txnreceiptdetails.equals("")) {
            transaction.setReceiptDetailsType(Integer.parseInt(txnreceiptdetails));
        }
        transaction.setReceiptDetailsDescription(txnreceipttypebankdetails);
        if (klfollowednotfollowed != null && !klfollowednotfollowed.equals("")) {
            transaction.setKlFollowStatus(Integer.parseInt(klfollowednotfollowed));
        }
        String txnRemarks = "";
        if (!txnremarks.equals("") && txnremarks != null) {
            txnRemarks = user.getEmail() + "#" + txnremarks;
            transaction.setRemarks(txnRemarks);
        }
        transaction.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(transaction.getSupportingDocs(),
                user.getEmail(), supportingdoc, user, em));
        transaction.setTransactionStatus("Accounted");
        String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
        transaction.setTransactionRefNumber(transactionNumber);
        genericDao.saveOrUpdate(transaction, user, em);
        entitytransaction.commit();
        return transaction;
    }

    public void vendorAccountAddDeductSpecialAdjustments(String txnPurpose, Users user, Vendor vendor, Double amount) {
        if (txnPurpose.equals("Receive special adjustments amount from vendors")) {
            if (vendor.getAvailableSpecAdjAmount() != null) {
                vendor.setAvailableSpecAdjAmount(vendor.getAvailableSpecAdjAmount() + amount);
            } else {
                vendor.setAvailableSpecAdjAmount(amount);
            }
        }
        if (txnPurpose.equals("Pay special adjustments amount to vendors")) {
            if (vendor.getAvailableSpecAdjAmount() != null) {
                vendor.setAvailableSpecAdjAmount(vendor.getAvailableSpecAdjAmount() - amount);
            } else {
                vendor.setAvailableSpecAdjAmount(amount);
            }
        }
        genericDao.saveOrUpdate(vendor, user, entityManager);
    }

    public void branchCashAccountAddDeductSpecialAdjustments(String txnPurpose, Users user, Vendor vendor,
            Double amount) {
        Double creditAmount = null;
        Double debitAmount = null;
        Double resultantCash = null;
        Double mainToPettyCash = null;
        Double grandTotal = null;
        StringBuilder newsbquery = new StringBuilder("");
        newsbquery.append("select obj from BranchCashCount obj WHERE obj.branch.id='" + vendor.getBranch().getId()
                + "' AND obj.organization.id='" + vendor.getOrganization().getId()
                + "' and obj.presentStatus=1 ORDER BY obj.date desc");
        List<BranchCashCount> branchCashCount = genericDao.executeSimpleQueryWithLimit(newsbquery.toString(),
                entityManager, 1);
        if (branchCashCount.size() > 0) {
            if (branchCashCount.get(0).getCreditAmount() == null) {
                creditAmount = amount;
            }
            if (branchCashCount.get(0).getCreditAmount() != null) {
                creditAmount = branchCashCount.get(0).getCreditAmount() + amount;
            }
            if (branchCashCount.get(0).getDebitAmount() == null) {
                debitAmount = 0.0;
            }
            if (branchCashCount.get(0).getDebitAmount() != null) {
                debitAmount = branchCashCount.get(0).getDebitAmount();
            }
            if (branchCashCount.get(0).getTotalMainCashToPettyCash() != null) {
                mainToPettyCash = branchCashCount.get(0).getTotalMainCashToPettyCash();
            }
            if (branchCashCount.get(0).getTotalMainCashToPettyCash() == null) {
                mainToPettyCash = 0.0;
            }
            if (branchCashCount.get(0).getGrandTotal() != null) {
                grandTotal = branchCashCount.get(0).getGrandTotal();
            } else {
                grandTotal = 0.0;
            }
            resultantCash = grandTotal + creditAmount - debitAmount - mainToPettyCash;
            branchCashCount.get(0).setCreditAmount(creditAmount);
            branchCashCount.get(0).setResultantCash(resultantCash);
            genericDao.saveOrUpdate(branchCashCount.get(0), user, entityManager);
        }
    }

    @Override
    public Transaction paySpecialAdjustmentsToVendors(String txnPurpose, TransactionPurpose usertxnPurpose, Users user,
            String txnPCAFCVCreditVendor, String txnPSAATVAmountPaid, String txnPSAATVForProject, String supportingdoc,
            String txnremarks, String klfollowednotfollowed, EntityTransaction entitytransaction, EntityManager em)
            throws IDOSException {
        Map<String, Object> criterias = new HashMap<String, Object>();
        Transaction transaction = new Transaction();
        Vendor vendor = Vendor.findById(IdosUtil.convertStringToLong(txnPCAFCVCreditVendor));
        Double amountPaidToVendor = Double.valueOf(txnPSAATVAmountPaid);
        Branch txnBranch = null;
        Project txnproject = null;
        if ((vendor != null) && (vendor.getBranch() != null)) {
            txnBranch = vendor.getBranch();
        }
        vendorAccountAddDeductSpecialAdjustments(txnPurpose, user, vendor, amountPaidToVendor);
        if (txnPSAATVForProject != null && !txnPSAATVForProject.equals("")) {
            txnproject = Project.findById(IdosUtil.convertStringToLong(txnPSAATVForProject));
        }
        transaction.setTransactionBranch(txnBranch);
        transaction.setTransactionProject(txnproject);
        transaction.setTransactionPurpose(usertxnPurpose);
        transaction.setTransactionBranchOrganization(user.getOrganization());
        transaction.setTransactionVendorCustomer(vendor);
        transaction.setNetAmount(amountPaidToVendor);
        String netDesc = "Special Adjustment Paid:" + txnPSAATVAmountPaid;
        transaction.setNetAmountResultDescription(netDesc);
        transaction.setTransactionDate(Calendar.getInstance().getTime());
        String txnRemarks = "";
        if (!txnremarks.equals("") && txnremarks != null) {
            txnRemarks = user.getEmail() + "#" + txnremarks;
            transaction.setRemarks(txnRemarks);
        }
        transaction.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(transaction.getSupportingDocs(),
                user.getEmail(), supportingdoc, user, em));
        transaction.setTransactionStatus("Require Approval");
        // list of additional users all approver role users of thet organization
        criterias.clear();
        criterias.put("role.name", "APPROVER");
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
        String approverEmails = "";
        String additionalApprovarUsers = "";
        String selectedAdditionalApproval = "";
        for (UsersRoles usrRoles : approverRole) {
            additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
            // check for all user which has right in headquarter branch
            criterias.clear();
            criterias.put("user.id", usrRoles.getUser().getId());
            criterias.put("userRights.id", 2L);
            criterias.put("branch.id", txnBranch.getId());
            criterias.put("presentStatus", 1);
            List<UserRightInBranch> userHasRightInBranch = genericDao.findByCriteria(UserRightInBranch.class, criterias,
                    entityManager);
            for (UserRightInBranch usrRightInBnch : userHasRightInBranch) {
                approverEmails += usrRightInBnch.getUser().getEmail() + ",";
            }
        }
        transaction.setApproverEmails(approverEmails);
        transaction.setAdditionalApproverEmails(additionalApprovarUsers);
        String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
        transaction.setTransactionRefNumber(transactionNumber);
        genericDao.saveOrUpdate(transaction, user, em);
        FILE_UPLOAD_DAO.updateUploadFileLogs(em, user, transaction.getSupportingDocs(), transaction.getId(),
                IdosConstants.MAIN_TXN_TYPE);
        entitytransaction.commit();
        return transaction;
    }

    private Double custVendorOpeningBalanceTotal(int type, Users user, EntityManager entityManager) {
        Double totalOpeningBal = 0.0;
        StringBuilder sumOBquery = new StringBuilder(
                "select SUM(obj.totalOpeningBalance) from Vendor obj WHERE obj.organization = '"
                        + user.getOrganization().getId() + "' AND obj.type = '" + type
                        + "' and obj.presentStatus  = 1");
        List<Transaction> sumOBquerytxn = genericDao.executeSimpleQuery(sumOBquery.toString(), entityManager);
        if (sumOBquerytxn.size() > 0) {
            Object val = sumOBquerytxn.get(0);
            if (val != null) {
                totalOpeningBal = Double.valueOf(val.toString());
            }
        }
        return totalOpeningBal;
    }

    @Override
    public synchronized ObjectNode approverCashBankReceivablePayables(Users user, EntityManager entityManager) {
        ObjectNode row = Json.newObject();
        try {
            Map<String, Object> criterias = new HashMap<String, Object>();
            Double cashBalance = 0.0, bankBalance = 0.0, accountsReceivables = 0.0, accountsPayables = 0.0,
                    accountsReceivablesOverdues = 0.0, accountsPayablesOverdues = 0.0;
            Organization org = user.getOrganization();
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int finStartMonth = 4;
            int finEndMonth = 3;
            String finStartDate = null;
            String finStDt = null;
            StringBuilder startYear = null;
            String finEndDate = null;
            String finEndDt = null;
            StringBuilder endYear = null;
            if (org.getFinancialStartDate() != null) {
                finStartMonth = org.getFinancialStartDate().getMonth() + 1;
            }
            if (org.getFinancialEndDate() != null) {
                finEndMonth = org.getFinancialEndDate().getMonth() + 1;
            }
            if (currentMonth < finStartMonth) {
                startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1));
                endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            } else {
                startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1));
            }
            if (org.getFinancialStartDate() != null) {
                finStDt = StaticController.idosmdtdf.format(org.getFinancialStartDate()) + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            } else {
                finStDt = "Apr 01" + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            }
            if (org.getFinancialEndDate() != null) {
                finEndDt = StaticController.idosmdtdf.format(org.getFinancialEndDate()) + "," + endYear;
                finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
            } else {
                finEndDt = "Mar 31" + "," + endYear;
                finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
            }
            String roles = getUserRoles(user);
            // Opening balances
            double totalCustOpeningBal = custVendorOpeningBalanceTotal(IdosConstants.CUSTOMER, user, entityManager);
            double totalVendOpeningBal = custVendorOpeningBalanceTotal(IdosConstants.VENDOR, user, entityManager);
            if (roles.contains("CONTROLLER") || roles.contains("ACCOUNTANT")) {
                criterias.clear();
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("presentStatus", 1);
                List<Branch> availableBranches = genericDao.findByCriteria(Branch.class, criterias, entityManager);
                for (Branch bnch : availableBranches) {
                    StringBuilder newsbquery = new StringBuilder("");
                    newsbquery.append("select obj from BranchCashCount obj WHERE obj.branch.id='" + bnch.getId()
                            + "' AND obj.organization.id='" + bnch.getOrganization().getId()
                            + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                    List<BranchCashCount> prevBranchCashCount = genericDao
                            .executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
                    if (prevBranchCashCount.size() > 0) {
                        if (prevBranchCashCount.get(0).getResultantCash() != null) {
                            cashBalance += prevBranchCashCount.get(0).getResultantCash();
                        }
                        if (prevBranchCashCount.get(0).getResultantPettyCash() != null) {
                            cashBalance += prevBranchCashCount.get(0).getResultantPettyCash();
                        }
                    }
                    List<BranchBankAccounts> bnchBankAccounts = bnch.getBranchBankAccounts();
                    for (BranchBankAccounts bnchBnkAccounts : bnchBankAccounts) {
                        StringBuilder newbnchbankactsbquery = new StringBuilder("");
                        newbnchbankactsbquery
                                .append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='"
                                        + bnch.getId() + "' AND obj.organization.id='" + bnch.getOrganization().getId()
                                        + "' and obj.branchBankAccounts.id='" + bnchBnkAccounts.getId()
                                        + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                        List<BranchBankAccountBalance> prevBranchBankBalance = genericDao
                                .executeSimpleQueryWithLimit(newbnchbankactsbquery.toString(), entityManager, 1);
                        if (prevBranchBankBalance.size() > 0) {
                            if (prevBranchBankBalance.get(0).getResultantCash() != null)
                                bankBalance += prevBranchBankBalance.get(0).getResultantCash();
                        }
                    }
                    StringBuilder branchcreditincomesbquery = new StringBuilder(
                            "select obj from Transaction obj WHERE obj.transactionBranch='" + bnch.getId()
                                    + "' and obj.transactionBranchOrganization='" + bnch.getOrganization().getId()
                                    + "' and obj.transactionPurpose in (2,38) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate between '"
                                    + finStartDate + "' and '" + finEndDate + "'");
                    List<Transaction> bnchcreditincometxn = genericDao
                            .executeSimpleQuery(branchcreditincomesbquery.toString(), entityManager);
                    Vendor cust = null;
                    for (Transaction cdtincmtxn : bnchcreditincometxn) {
                        if (cdtincmtxn.getNetAmount() != null) {
                            if (cdtincmtxn.getCustomerNetPayment() != null) {
                                if (cdtincmtxn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                                    accountsReceivables -= cdtincmtxn.getCustomerNetPayment();
                                } else {
                                    accountsReceivables += cdtincmtxn.getNetAmount()
                                            - cdtincmtxn.getCustomerNetPayment();
                                }
                            } else if (cdtincmtxn.getNetAmount() != null) {
                                if (cdtincmtxn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                                    accountsReceivables -= cdtincmtxn.getNetAmount();
                                } else {
                                    accountsReceivables += cdtincmtxn.getNetAmount();
                                }
                            }
                        }
                        if (cdtincmtxn.getTransactionVendorCustomer() != null) {
                            cust = cdtincmtxn.getTransactionVendorCustomer();
                            if ((cust.getPurchaseType() == 0 || cust.getPurchaseType() == 2)
                                    && cust.getDaysForCredit() != null) {
                                int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
                                        - cdtincmtxn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
                                if (daysdiff > cust.getDaysForCredit()) {
                                    if (cdtincmtxn.getCustomerNetPayment() != null) {
                                        accountsReceivablesOverdues += cdtincmtxn.getNetAmount()
                                                - cdtincmtxn.getCustomerNetPayment();
                                    } else if (cdtincmtxn.getNetAmount() != null) {
                                        accountsReceivablesOverdues += cdtincmtxn.getNetAmount();
                                    }
                                }
                                /*
                                 * if(cust.getDaysForCredit()>14){
                                 * int daysdiff=(int)(Calendar.getInstance().getTimeInMillis()-cdtincmtxn.
                                 * getTransactionDate().getTime()/1000*60*60*24);
                                 * if(daysdiff<14 || daysdiff==14){
                                 * if(cdtincmtxn.getCustomerNetPayment()!=null){
                                 * accountsReceivables+=cdtincmtxn.getNetAmount()-cdtincmtxn.
                                 * getCustomerNetPayment();
                                 * }else{
                                 * accountsReceivables+=cdtincmtxn.getNetAmount();
                                 * }
                                 * }
                                 * }else{
                                 * accountsReceivables+=cdtincmtxn.getNetAmount();
                                 * }
                                 */
                            }
                            /*
                             * else if(cust.getDaysForCredit()==null){
                             * accountsReceivables+=cdtincmtxn.getNetAmount();
                             * }
                             */
                        }
                    }
                    // Journal entry receivables: when cust select on lef side increase
                    StringBuilder journalTxnQuery = new StringBuilder(
                            "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                    journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_CUSTOMER).append("'");
                    journalTxnQuery.append(
                            " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.debitBranch='");
                    journalTxnQuery.append(bnch.getId()).append("' and obj1.provisionMadeForOrganization=")
                            .append(bnch.getOrganization().getId());
                    journalTxnQuery.append(
                            " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                    journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                    String sqlStr = journalTxnQuery.toString();
                    sqlStr = sqlStr.replace("(1?)", "1");
                    List<IdosProvisionJournalEntry> provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                    if (provJourTxn.size() > 0) {
                        Object val = provJourTxn.get(0);
                        if (val != null) {
                            accountsReceivables += IdosUtil.convertStringToDouble(String.valueOf(val));
                        }
                    }
                    /* now fetching credit records */
                    journalTxnQuery = new StringBuilder(
                            "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                    journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_CUSTOMER).append("'");
                    journalTxnQuery.append(
                            " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.creditBranch='");
                    journalTxnQuery.append(bnch.getId()).append("' and obj1.provisionMadeForOrganization=")
                            .append(bnch.getOrganization().getId());
                    journalTxnQuery.append(
                            " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                    journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                    sqlStr = journalTxnQuery.toString();
                    sqlStr = sqlStr.replace("(1?)", "0");
                    provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                    if (provJourTxn.size() > 0) {
                        Object val = provJourTxn.get(0);
                        if (val != null) {
                            accountsReceivables -= IdosUtil.convertStringToDouble(String.valueOf(val));
                        }
                    }
                    StringBuilder branchcreditexpensebquery = new StringBuilder("");
                    branchcreditexpensebquery.append("select obj from Transaction obj WHERE obj.transactionBranch='"
                            + bnch.getId() + "' and obj.transactionBranchOrganization='"
                            + bnch.getOrganization().getId()
                            + "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate  between '"
                            + finStartDate + "' and '" + finEndDate + "'");
                    List<Transaction> bnchcreditexpensetxn = genericDao
                            .executeSimpleQuery(branchcreditexpensebquery.toString(), entityManager);
                    Vendor vend = null;
                    for (Transaction cdtexptxn : bnchcreditexpensetxn) {
                        if (cdtexptxn.getNetAmount() != null) {
                            if (cdtexptxn.getVendorNetPayment() != null) {
                                accountsPayables += cdtexptxn.getNetAmount() - cdtexptxn.getVendorNetPayment();
                            } else {
                                accountsPayables += cdtexptxn.getNetAmount();
                            }
                        }
                        if (cdtexptxn.getTransactionVendorCustomer() != null) {
                            vend = cdtexptxn.getTransactionVendorCustomer();
                            if ((vend.getPurchaseType() == 0 || vend.getPurchaseType() == 2)
                                    && vend.getDaysForCredit() != null) {
                                int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
                                        - cdtexptxn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
                                if (daysdiff > vend.getDaysForCredit()) {
                                    if (cdtexptxn.getVendorNetPayment() != null) {
                                        accountsPayablesOverdues += cdtexptxn.getNetAmount()
                                                - cdtexptxn.getVendorNetPayment();
                                    } else {
                                        accountsPayablesOverdues += cdtexptxn.getNetAmount();
                                    }
                                }
                            }
                        }
                    }
                    // Journal entry receivables: when cust select on lef side increase
                    journalTxnQuery = new StringBuilder(
                            "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                    journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_VENDOR).append("'");
                    journalTxnQuery.append(
                            " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.debitBranch='");
                    journalTxnQuery.append(bnch.getId()).append("' and obj1.provisionMadeForOrganization=")
                            .append(bnch.getOrganization().getId());
                    journalTxnQuery.append(
                            " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                    journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                    sqlStr = journalTxnQuery.toString();
                    sqlStr = sqlStr.replace("(1?)", "1");
                    provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                    if (provJourTxn.size() > 0) {
                        Object val = provJourTxn.get(0);
                        if (val != null) {
                            accountsPayables -= IdosUtil.convertStringToDouble(String.valueOf(val));
                        }
                    }
                    /* now fetching credit records */
                    journalTxnQuery = new StringBuilder(
                            "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                    journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_VENDOR).append("'");
                    journalTxnQuery.append(
                            " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.creditBranch='");
                    journalTxnQuery.append(bnch.getId()).append("' and obj1.provisionMadeForOrganization=")
                            .append(bnch.getOrganization().getId());
                    journalTxnQuery.append(
                            " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                    journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                    sqlStr = journalTxnQuery.toString();
                    sqlStr = sqlStr.replace("(1?)", "0");
                    provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                    if (provJourTxn.size() > 0) {
                        Object val = provJourTxn.get(0);
                        if (val != null) {
                            accountsPayables += IdosUtil.convertStringToDouble(String.valueOf(val));
                        }
                    }
                }
            } else {
                criterias.clear();
                criterias.put("user.id", user.getId());
                criterias.put("userRights.id", 2L);
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("presentStatus", 1);
                List<UserRightInBranch> userApproverRightInBranches = genericDao.findByCriteria(UserRightInBranch.class,
                        criterias, entityManager);
                for (UserRightInBranch usrRghtBnchs : userApproverRightInBranches) {
                    StringBuilder newsbquery = new StringBuilder("");
                    newsbquery.append("select obj from BranchCashCount obj WHERE obj.branch.id='"
                            + usrRghtBnchs.getBranch().getId() + "' AND obj.organization.id='"
                            + usrRghtBnchs.getOrganization().getId()
                            + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                    List<BranchCashCount> prevBranchCashCount = genericDao
                            .executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
                    if (prevBranchCashCount.size() > 0) {
                        if (prevBranchCashCount.get(0).getResultantCash() != null) {
                            cashBalance += prevBranchCashCount.get(0).getResultantCash();
                        }
                        if (prevBranchCashCount.get(0).getResultantPettyCash() != null) {
                            cashBalance += prevBranchCashCount.get(0).getResultantPettyCash();
                        }
                    }
                    List<BranchBankAccounts> bnchBankAccounts = usrRghtBnchs.getBranch().getBranchBankAccounts();
                    for (BranchBankAccounts bnchBnkAccounts : bnchBankAccounts) {
                        StringBuilder newbnchbankactsbquery = new StringBuilder("");
                        newbnchbankactsbquery
                                .append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='"
                                        + usrRghtBnchs.getBranch().getId() + "' AND obj.organization.id='"
                                        + usrRghtBnchs.getOrganization().getId() + "' and obj.branchBankAccounts.id='"
                                        + bnchBnkAccounts.getId() + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                        List<BranchBankAccountBalance> prevBranchBankBalance = genericDao
                                .executeSimpleQueryWithLimit(newbnchbankactsbquery.toString(), entityManager, 1);
                        if (prevBranchBankBalance.size() > 0) {
                            if (prevBranchBankBalance.get(0).getResultantCash() != null)
                                bankBalance += prevBranchBankBalance.get(0).getResultantCash();
                        }
                    }
                    StringBuilder branchcreditincomesbquery = new StringBuilder(
                            "select obj from Transaction obj WHERE obj.transactionBranch='"
                                    + usrRghtBnchs.getBranch().getId() + "' and obj.transactionBranchOrganization='"
                                    + usrRghtBnchs.getOrganization().getId()
                                    + "' AND obj.transactionPurpose in (2,38) and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate  between '"
                                    + finStartDate + "' and '" + finEndDate + "'");
                    List<Transaction> bnchcreditincometxn = genericDao
                            .executeSimpleQuery(branchcreditincomesbquery.toString(), entityManager);
                    Vendor cust = null;
                    for (Transaction cdtincmtxn : bnchcreditincometxn) {
                        if (cdtincmtxn.getCustomerNetPayment() != null) {
                            if (cdtincmtxn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                                accountsReceivables -= cdtincmtxn.getCustomerNetPayment();
                            } else {
                                accountsReceivables += cdtincmtxn.getNetAmount() - cdtincmtxn.getCustomerNetPayment();
                            }
                        } else if (cdtincmtxn.getNetAmount() != null) {
                            if (cdtincmtxn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                                accountsReceivables -= cdtincmtxn.getNetAmount();
                            } else {
                                accountsReceivables += cdtincmtxn.getNetAmount();
                            }
                        }

                        if (cdtincmtxn.getTransactionVendorCustomer() != null) {
                            cust = cdtincmtxn.getTransactionVendorCustomer();
                            if ((cust.getPurchaseType() == 0 || cust.getPurchaseType() == 2)
                                    && cust.getDaysForCredit() != null) {
                                int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
                                        - cdtincmtxn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
                                if (daysdiff > cust.getDaysForCredit()) {
                                    if (cdtincmtxn.getCustomerNetPayment() != null) {
                                        accountsReceivablesOverdues += cdtincmtxn.getNetAmount()
                                                - cdtincmtxn.getCustomerNetPayment();
                                    } else {
                                        accountsReceivablesOverdues += cdtincmtxn.getNetAmount();
                                    }
                                }
                                /*
                                 * if(cust.getDaysForCredit()>14){
                                 * int daysdiff=(int)(Calendar.getInstance().getTimeInMillis()-cdtincmtxn.
                                 * getTransactionDate().getTime()/1000*60*60*24);
                                 * if(daysdiff<14 || daysdiff==14){
                                 * if(cdtincmtxn.getCustomerNetPayment()!=null){
                                 * accountsReceivables+=cdtincmtxn.getNetAmount()-cdtincmtxn.
                                 * getCustomerNetPayment();
                                 * }else{
                                 * accountsReceivables+=cdtincmtxn.getNetAmount();
                                 * }
                                 * }
                                 * }else{
                                 * accountsReceivables+=cdtincmtxn.getNetAmount();
                                 * }
                                 */
                            }
                            /*
                             * if(cust.getDaysForCredit()==null){
                             * accountsReceivables+=cdtincmtxn.getNetAmount();
                             * }
                             */
                        }
                    }
                    // Journal entry receivables: when cust select on lef side increase
                    StringBuilder journalTxnQuery = new StringBuilder(
                            "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                    journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_CUSTOMER).append("'");
                    journalTxnQuery.append(
                            " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.debitBranch='");
                    journalTxnQuery.append(usrRghtBnchs.getId()).append("' and obj1.provisionMadeForOrganization=")
                            .append(usrRghtBnchs.getOrganization().getId());
                    journalTxnQuery.append(
                            " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                    journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                    String sqlStr = journalTxnQuery.toString();
                    sqlStr = sqlStr.replace("(1?)", "1");
                    List<IdosProvisionJournalEntry> provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                    if (provJourTxn.size() > 0) {
                        Object val = provJourTxn.get(0);
                        if (val != null) {
                            accountsReceivables += IdosUtil.convertStringToDouble(String.valueOf(val));
                        }
                    }
                    /* now fetching credit records */
                    journalTxnQuery = new StringBuilder(
                            "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                    journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_CUSTOMER).append("'");
                    journalTxnQuery.append(
                            " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.creditBranch='");
                    journalTxnQuery.append(usrRghtBnchs.getId()).append("' and obj1.provisionMadeForOrganization=")
                            .append(usrRghtBnchs.getOrganization().getId());
                    journalTxnQuery.append(
                            " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                    journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                    sqlStr = journalTxnQuery.toString();
                    sqlStr = sqlStr.replace("(1?)", "0");
                    provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                    if (provJourTxn.size() > 0) {
                        Object val = provJourTxn.get(0);
                        if (val != null) {
                            accountsReceivables -= IdosUtil.convertStringToDouble(String.valueOf(val));
                        }
                    }
                    StringBuilder branchcreditexpensebquery = new StringBuilder(
                            "select obj from Transaction obj WHERE obj.transactionBranch='"
                                    + usrRghtBnchs.getBranch().getId() + "' and obj.transactionBranchOrganization='"
                                    + usrRghtBnchs.getOrganization().getId()
                                    + "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate  between '"
                                    + finStartDate + "' and '" + finEndDate + "'");
                    List<Transaction> bnchcreditexpensetxn = genericDao
                            .executeSimpleQuery(branchcreditexpensebquery.toString(), entityManager);
                    Vendor vend = null;
                    for (Transaction cdtexptxn : bnchcreditexpensetxn) {
                        if (cdtexptxn.getVendorNetPayment() != null) {
                            accountsPayables += cdtexptxn.getNetAmount() - cdtexptxn.getVendorNetPayment();
                        } else {
                            accountsPayables += cdtexptxn.getNetAmount();
                        }
                        if (cdtexptxn.getTransactionVendorCustomer() != null) {
                            vend = cdtexptxn.getTransactionVendorCustomer();
                            if ((vend.getPurchaseType() == 0 || vend.getPurchaseType() == 2)
                                    && vend.getDaysForCredit() != null) {
                                int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
                                        - cdtexptxn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
                                if (daysdiff > vend.getDaysForCredit()) {
                                    if (cdtexptxn.getVendorNetPayment() != null) {
                                        accountsPayablesOverdues += cdtexptxn.getNetAmount()
                                                - cdtexptxn.getVendorNetPayment();
                                    } else {
                                        accountsPayablesOverdues += cdtexptxn.getNetAmount();
                                    }
                                }
                                /*
                                 * if(vend.getDaysForCredit()>14){
                                 * int daysdiff=(int)(Calendar.getInstance().getTimeInMillis()-cdtexptxn.
                                 * getTransactionDate().getTime()/1000*60*60*24);
                                 * if(daysdiff<14 || daysdiff==14){
                                 * if(cdtexptxn.getVendorNetPayment()!=null){
                                 * accountsPayables+=cdtexptxn.getNetAmount()-cdtexptxn.getVendorNetPayment();
                                 * }else{
                                 * accountsPayables+=cdtexptxn.getNetAmount();
                                 * }
                                 * }
                                 * }else{
                                 * accountsPayables+=cdtexptxn.getNetAmount();
                                 * }
                                 */
                            }
                            /*
                             * if(vend.getDaysForCredit()==null){
                             * accountsPayables+=cdtexptxn.getNetAmount();
                             * }
                             */
                        }
                    }
                    // Journal entry receivables: when cust select on lef side increase
                    journalTxnQuery = new StringBuilder(
                            "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                    journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_VENDOR).append("'");
                    journalTxnQuery.append(
                            " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.debitBranch='");
                    journalTxnQuery.append(usrRghtBnchs.getId()).append("' and obj1.provisionMadeForOrganization=")
                            .append(usrRghtBnchs.getOrganization().getId());
                    journalTxnQuery.append(
                            " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                    journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                    sqlStr = journalTxnQuery.toString();
                    sqlStr = sqlStr.replace("(1?)", "1");
                    provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                    if (provJourTxn.size() > 0) {
                        Object val = provJourTxn.get(0);
                        if (val != null) {
                            accountsPayables -= IdosUtil.convertStringToDouble(String.valueOf(val));
                        }
                    }
                    /* now fetching credit records */
                    journalTxnQuery = new StringBuilder(
                            "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                    journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_VENDOR).append("'");
                    journalTxnQuery.append(
                            " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.creditBranch='");
                    journalTxnQuery.append(usrRghtBnchs.getId()).append("' and obj1.provisionMadeForOrganization=")
                            .append(usrRghtBnchs.getOrganization().getId());
                    journalTxnQuery.append(
                            " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                    journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                    sqlStr = journalTxnQuery.toString();
                    sqlStr = sqlStr.replace("(1?)", "0");
                    provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                    if (provJourTxn.size() > 0) {
                        Object val = provJourTxn.get(0);
                        if (val != null) {
                            accountsPayables += IdosUtil.convertStringToDouble(String.valueOf(val));
                        }
                    }
                }
            }
            row.put("cashBalance", IdosConstants.decimalFormat.format(cashBalance));
            row.put("bankBalance", IdosConstants.decimalFormat.format(bankBalance));
            row.put("accountsReceivables",
                    IdosConstants.decimalFormat.format(accountsReceivables + totalCustOpeningBal));
            row.put("accountsPayables", IdosConstants.decimalFormat.format(accountsPayables + totalVendOpeningBal));
            row.put("accountsReceivablesOverdues", IdosConstants.decimalFormat.format(accountsReceivablesOverdues));
            row.put("accountsPayablesOverdues", IdosConstants.decimalFormat.format(accountsPayablesOverdues));
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return row;
    }

    @Override
    public ObjectNode branchWiseApproverCashBankReceivablePayables(Users user, EntityManager entityManager,
            String tabElement) {
        log.log(Level.FINE, "************* Start " + tabElement);
        ObjectNode result = Json.newObject();
        ArrayNode branchWiseCashBankRecivablesPayablesan = result.putArray("branchWiseCashBankRecivablesPayablesData");
        try {
            Map<String, Object> criterias = new HashMap<String, Object>();
            Organization org = user.getOrganization();
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int finStartMonth = 4;
            int finEndMonth = 3;
            String finStartDate = null;
            String finStDt = null;
            StringBuilder startYear = null;
            String finEndDate = null;
            String finEndDt = null;
            StringBuilder endYear = null;
            if (org.getFinancialStartDate() != null) {
                finStartMonth = org.getFinancialStartDate().getMonth() + 1;
            }
            if (org.getFinancialEndDate() != null) {
                finEndMonth = org.getFinancialEndDate().getMonth() + 1;
            }
            if (currentMonth < finStartMonth) {
                startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1));
                endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
            } else {
                startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
                endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1));
            }
            if (org.getFinancialStartDate() != null && !org.getFinancialStartDate().equals("")) {
                finStDt = StaticController.idosmdtdf.format(org.getFinancialStartDate()) + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            } else {
                finStDt = "Apr 01" + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            }
            if (org.getFinancialStartDate() != null && !org.getFinancialStartDate().equals("")) {
                finStDt = StaticController.idosmdtdf.format(org.getFinancialStartDate()) + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            } else {
                finStDt = "Apr 01" + "," + startYear;
                finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
            }
            if (org.getFinancialEndDate() != null && !org.getFinancialEndDate().equals("")) {
                finEndDt = StaticController.idosmdtdf.format(org.getFinancialEndDate()) + "," + endYear;
                finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
            } else {
                finEndDt = "Mar 31" + "," + endYear;
                finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
            }
            String roles = getUserRoles(user);
            if (roles.contains("CONTROLLER") || roles.contains("ACCOUNTANT")) {
                criterias.clear();
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("presentStatus", 1);
                List<Branch> availableBranches = genericDao.findByCriteria(Branch.class, criterias, entityManager);
                for (Branch bnch : availableBranches) {
                    ObjectNode row = Json.newObject();
                    Double cashBalance = 0.0, bankBalance = 0.0, accountsReceivables = 0.0, accountsPayables = 0.0,
                            accountsReceivablesOverdues = 0.0, accountsPayablesOverdues = 0.0;
                    Double pettyCashBalance = 0.0;
                    if (tabElement != null && tabElement.equals("cashBalanceAllBranches")) {
                        StringBuilder newsbquery = new StringBuilder(
                                "select obj from BranchCashCount obj WHERE obj.branch.id='" + bnch.getId()
                                        + "' AND obj.organization.id='" + bnch.getOrganization().getId()
                                        + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                        List<BranchCashCount> prevBranchCashCount = genericDao
                                .executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
                        if (prevBranchCashCount.size() > 0) {
                            if (prevBranchCashCount.get(0).getResultantCash() != null) {
                                cashBalance += prevBranchCashCount.get(0).getResultantCash();
                            }
                            if (prevBranchCashCount.get(0).getResultantPettyCash() != null) {
                                pettyCashBalance += prevBranchCashCount.get(0).getResultantPettyCash();
                            }
                        }
                        row.put("cashBalance", bnch.getName() + ":" + IdosConstants.decimalFormat.format(cashBalance)
                                + ":" + IdosConstants.decimalFormat.format(pettyCashBalance));
                        branchWiseCashBankRecivablesPayablesan.add(row);
                    } else if (tabElement != null && tabElement.equals("bankBalanceAllBranches")) {
                        List<BranchBankAccounts> bnchBankAccounts = bnch.getBranchBankAccounts();
                        for (BranchBankAccounts bnchBnkAccounts : bnchBankAccounts) {
                            StringBuilder newbnchbankactsbquery = new StringBuilder("");
                            newbnchbankactsbquery.append(
                                    "select obj from BranchBankAccountBalance obj WHERE obj.branch.id='" + bnch.getId()
                                            + "' AND obj.organization.id='" + bnch.getOrganization().getId()
                                            + "' and obj.branchBankAccounts.id='" + bnchBnkAccounts.getId()
                                            + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                            List<BranchBankAccountBalance> prevBranchBankBalance = genericDao
                                    .executeSimpleQueryWithLimit(newbnchbankactsbquery.toString(), entityManager, 1);
                            if (prevBranchBankBalance.size() > 0) {
                                if (prevBranchBankBalance.get(0).getResultantCash() != null)
                                    bankBalance += prevBranchBankBalance.get(0).getResultantCash();
                            }
                        }
                        row.put("bankBalance", bnch.getName() + ":" + IdosConstants.decimalFormat.format(bankBalance)
                                + ":" + bnch.getId());
                        branchWiseCashBankRecivablesPayablesan.add(row);
                    } else if (tabElement != null && (tabElement.equals("accountsReceivablesAllBranches")
                            || tabElement.equals("receivableOverduesAllBranches"))) {
                        StringBuilder branchcreditincomesbquery = new StringBuilder("");
                        // for sell transaction and sales returns
                        branchcreditincomesbquery.append("select obj from Transaction obj WHERE obj.transactionBranch='"
                                + bnch.getId() + "' and obj.transactionBranchOrganization='"
                                + bnch.getOrganization().getId()
                                + "' AND obj.transactionPurpose in (2,38) and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate  between '"
                                + finStartDate + "' and '" + finEndDate + "'");
                        List<Transaction> bnchcreditincometxn = genericDao
                                .executeSimpleQuery(branchcreditincomesbquery.toString(), entityManager);
                        Vendor cust = null;
                        for (Transaction cdtincmtxn : bnchcreditincometxn) {
                            if (cdtincmtxn.getCustomerNetPayment() != null) {
                                if (cdtincmtxn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                                    accountsReceivables -= cdtincmtxn.getCustomerNetPayment();
                                } else {
                                    accountsReceivables += cdtincmtxn.getNetAmount()
                                            - cdtincmtxn.getCustomerNetPayment();
                                }
                            } else if (cdtincmtxn.getNetAmount() != null) {
                                if (cdtincmtxn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                                    accountsReceivables -= cdtincmtxn.getNetAmount();
                                } else {
                                    accountsReceivables += cdtincmtxn.getNetAmount();
                                }
                            }
                            if (cdtincmtxn.getTransactionVendorCustomer() != null) {
                                cust = cdtincmtxn.getTransactionVendorCustomer();
                                if ((cust.getPurchaseType() == 0 || cust.getPurchaseType() == 2)
                                        && cust.getDaysForCredit() != null) {
                                    int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
                                            - cdtincmtxn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
                                    if (daysdiff > cust.getDaysForCredit()) {
                                        if (cdtincmtxn.getCustomerNetPayment() != null) {
                                            accountsReceivablesOverdues += cdtincmtxn.getNetAmount()
                                                    - cdtincmtxn.getCustomerNetPayment();
                                        } else {
                                            accountsReceivablesOverdues += cdtincmtxn.getNetAmount();
                                        }
                                    }
                                }
                            }
                        }
                        // Journal entry receivables: when cust select on lef side increase
                        StringBuilder journalTxnQuery = new StringBuilder(
                                "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                        journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_CUSTOMER).append("'");
                        journalTxnQuery.append(
                                " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.debitBranch='");
                        journalTxnQuery.append(bnch.getId()).append("' and obj1.provisionMadeForOrganization=")
                                .append(bnch.getOrganization().getId());
                        journalTxnQuery.append(
                                " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                        journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                        String sqlStr = journalTxnQuery.toString();
                        sqlStr = sqlStr.replace("(1?)", "1");
                        List<IdosProvisionJournalEntry> provJourTxn = genericDao.executeSimpleQuery(sqlStr,
                                entityManager);
                        if (provJourTxn.size() > 0) {
                            Object val = provJourTxn.get(0);
                            if (val != null) {
                                accountsReceivables += IdosUtil.convertStringToDouble(String.valueOf(val));
                            }
                        }
                        /* now fetching credit records */
                        journalTxnQuery = new StringBuilder(
                                "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                        journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_CUSTOMER).append("'");
                        journalTxnQuery.append(
                                " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.creditBranch='");
                        journalTxnQuery.append(bnch.getId()).append("' and obj1.provisionMadeForOrganization=")
                                .append(bnch.getOrganization().getId());
                        journalTxnQuery.append(
                                " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                        journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                        sqlStr = journalTxnQuery.toString();
                        sqlStr = sqlStr.replace("(1?)", "0");
                        provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                        if (provJourTxn.size() > 0) {
                            Object val = provJourTxn.get(0);
                            if (val != null) {
                                accountsReceivables -= IdosUtil.convertStringToDouble(String.valueOf(val));
                            }
                        }
                        row.put("accountsReceivables", bnch.getName() + ":"
                                + IdosConstants.decimalFormat.format(accountsReceivables) + ":" + bnch.getId());
                        row.put("accountsReceivablesOverdues", bnch.getName() + ":"
                                + IdosConstants.decimalFormat.format(accountsReceivablesOverdues) + ":" + bnch.getId());
                        branchWiseCashBankRecivablesPayablesan.add(row);
                    } else if (tabElement != null && (tabElement.equals("accountsPayablesAllBranches")
                            || tabElement.equals("payableOverduesAllBranches"))) {
                        StringBuilder branchcreditexpensebquery = new StringBuilder("");
                        branchcreditexpensebquery.append("select obj from Transaction obj WHERE obj.transactionBranch='"
                                + bnch.getId() + "' and obj.transactionBranchOrganization='"
                                + bnch.getOrganization().getId()
                                + "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate  between '"
                                + finStartDate + "' and '" + finEndDate + "'");
                        List<Transaction> bnchcreditexpensetxn = genericDao
                                .executeSimpleQuery(branchcreditexpensebquery.toString(), entityManager);
                        Vendor vend = null;
                        for (Transaction cdtexptxn : bnchcreditexpensetxn) {
                            if (cdtexptxn.getNetAmount() != null) {
                                if (cdtexptxn.getVendorNetPayment() != null) {
                                    accountsPayables += cdtexptxn.getNetAmount() - cdtexptxn.getVendorNetPayment();
                                } else {
                                    accountsPayables += cdtexptxn.getNetAmount();
                                }
                            }
                            if (cdtexptxn.getTransactionVendorCustomer() != null) {
                                vend = cdtexptxn.getTransactionVendorCustomer();
                                if ((vend.getPurchaseType() == 0 || vend.getPurchaseType() == 2)
                                        && vend.getDaysForCredit() != null) {
                                    int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
                                            - cdtexptxn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
                                    if (daysdiff > vend.getDaysForCredit()) {
                                        if (cdtexptxn.getNetAmount() != null) {
                                            if (cdtexptxn.getVendorNetPayment() != null) {
                                                accountsPayablesOverdues += cdtexptxn.getNetAmount()
                                                        - cdtexptxn.getVendorNetPayment();
                                            } else {
                                                accountsPayablesOverdues += cdtexptxn.getNetAmount();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Journal entry payables: when vend select on lef side decrease
                        StringBuilder journalTxnQuery = new StringBuilder(
                                "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                        journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_VENDOR).append("'");
                        journalTxnQuery.append(
                                " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.debitBranch='");
                        journalTxnQuery.append(bnch.getId()).append("' and obj1.provisionMadeForOrganization=")
                                .append(bnch.getOrganization().getId());
                        journalTxnQuery.append(
                                " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                        journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                        String sqlStr = journalTxnQuery.toString();
                        sqlStr = sqlStr.replace("(1?)", "1");
                        List<IdosProvisionJournalEntry> provJourTxn = genericDao.executeSimpleQuery(sqlStr,
                                entityManager);
                        if (provJourTxn.size() > 0) {
                            Object val = provJourTxn.get(0);
                            if (val != null) {
                                accountsPayables -= IdosUtil.convertStringToDouble(String.valueOf(val));
                            }
                        }
                        /* now fetching credit records */
                        journalTxnQuery = new StringBuilder(
                                "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                        journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_VENDOR).append("'");
                        journalTxnQuery.append(
                                " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.creditBranch='");
                        journalTxnQuery.append(bnch.getId()).append("' and obj1.provisionMadeForOrganization=")
                                .append(bnch.getOrganization().getId());
                        journalTxnQuery.append(
                                " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                        journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                        sqlStr = journalTxnQuery.toString();
                        sqlStr = sqlStr.replace("(1?)", "0");
                        provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                        if (provJourTxn.size() > 0) {
                            Object val = provJourTxn.get(0);
                            if (val != null) {
                                accountsPayables += IdosUtil.convertStringToDouble(String.valueOf(val));
                            }
                        }
                        row.put("accountsPayables", bnch.getName() + ":"
                                + IdosConstants.decimalFormat.format(accountsPayables) + ":" + bnch.getId());
                        row.put("accountsPayablesOverdues", bnch.getName() + ":"
                                + IdosConstants.decimalFormat.format(accountsPayablesOverdues) + ":" + bnch.getId());
                        branchWiseCashBankRecivablesPayablesan.add(row);
                    }
                }
            } else {
                criterias.clear();
                criterias.put("user.id", user.getId());
                criterias.put("userRights.id", 2L);
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("presentStatus", 1);
                List<UserRightInBranch> userApproverRightInBranches = genericDao.findByCriteria(UserRightInBranch.class,
                        criterias, entityManager);
                for (UserRightInBranch usrRghtBnchs : userApproverRightInBranches) {
                    ObjectNode row = Json.newObject();
                    Double cashBalance = 0.0, bankBalance = 0.0, accountsReceivables = 0.0, accountsPayables = 0.0,
                            pettyCashBalance = 0.0, accountsReceivablesOverdues = 0.0, accountsPayablesOverdues = 0.0;
                    if (tabElement != null && tabElement.equals("cashBalanceAllBranches")) {
                        StringBuilder newsbquery = new StringBuilder("");
                        newsbquery.append("select obj from BranchCashCount obj WHERE obj.branch.id='"
                                + usrRghtBnchs.getBranch().getId() + "' AND obj.organization.id='"
                                + usrRghtBnchs.getOrganization().getId()
                                + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                        List<BranchCashCount> prevBranchCashCount = genericDao
                                .executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
                        if (prevBranchCashCount.size() > 0) {
                            if (prevBranchCashCount.get(0).getResultantCash() != null) {
                                cashBalance += prevBranchCashCount.get(0).getResultantCash();
                            }
                            if (prevBranchCashCount.get(0).getResultantPettyCash() != null) {
                                pettyCashBalance += prevBranchCashCount.get(0).getResultantPettyCash();
                            }
                        }
                        row.put("cashBalance",
                                usrRghtBnchs.getBranch().getName() + ":"
                                        + IdosConstants.decimalFormat.format(cashBalance) + ":"
                                        + IdosConstants.decimalFormat.format(pettyCashBalance));
                        branchWiseCashBankRecivablesPayablesan.add(row);
                    } else if (tabElement != null && tabElement.equals("bankBalanceAllBranches")) {
                        List<BranchBankAccounts> bnchBankAccounts = usrRghtBnchs.getBranch().getBranchBankAccounts();
                        for (BranchBankAccounts bnchBnkAccounts : bnchBankAccounts) {
                            StringBuilder newbnchbankactsbquery = new StringBuilder("");
                            newbnchbankactsbquery
                                    .append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='"
                                            + usrRghtBnchs.getBranch().getId() + "' AND obj.organization.id='"
                                            + usrRghtBnchs.getOrganization().getId()
                                            + "' and obj.branchBankAccounts.id='" + bnchBnkAccounts.getId()
                                            + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                            List<BranchBankAccountBalance> prevBranchBankBalance = genericDao
                                    .executeSimpleQueryWithLimit(newbnchbankactsbquery.toString(), entityManager, 1);
                            if (prevBranchBankBalance.size() > 0) {
                                if (prevBranchBankBalance.get(0).getResultantCash() != null)
                                    bankBalance += prevBranchBankBalance.get(0).getResultantCash();
                            }
                        }
                        row.put("bankBalance",
                                usrRghtBnchs.getBranch().getName() + ":"
                                        + IdosConstants.decimalFormat.format(bankBalance) + ":"
                                        + usrRghtBnchs.getBranch().getId());
                        branchWiseCashBankRecivablesPayablesan.add(row);
                    } else if (tabElement != null && (tabElement.equals("accountsReceivablesAllBranches")
                            || tabElement.equals("receivableOverduesAllBranches"))) {
                        StringBuilder branchcreditincomesbquery = new StringBuilder(
                                "select obj from Transaction obj WHERE obj.transactionBranch='"
                                        + usrRghtBnchs.getBranch().getId() + "' and obj.transactionBranchOrganization='"
                                        + usrRghtBnchs.getOrganization().getId()
                                        + "' AND obj.transactionPurpose in (2,38) and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate  between '"
                                        + finStartDate + "' and '" + finEndDate + "'");
                        List<Transaction> bnchcreditincometxn = genericDao
                                .executeSimpleQuery(branchcreditincomesbquery.toString(), entityManager);
                        Vendor cust = null;
                        for (Transaction cdtincmtxn : bnchcreditincometxn) {
                            if (cdtincmtxn.getCustomerNetPayment() != null) {
                                if (cdtincmtxn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                                    accountsReceivables -= cdtincmtxn.getCustomerNetPayment();
                                } else {
                                    accountsReceivables += cdtincmtxn.getNetAmount()
                                            - cdtincmtxn.getCustomerNetPayment();
                                }
                            } else if (cdtincmtxn.getNetAmount() != null) {
                                if (cdtincmtxn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                                    accountsReceivables -= cdtincmtxn.getNetAmount();
                                } else {
                                    accountsReceivables += cdtincmtxn.getNetAmount();
                                }
                            }

                            if (cdtincmtxn.getTransactionVendorCustomer() != null) {
                                cust = cdtincmtxn.getTransactionVendorCustomer();
                                if ((cust.getPurchaseType() == 0 || cust.getPurchaseType() == 2)
                                        && cust.getDaysForCredit() != null) {
                                    int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
                                            - cdtincmtxn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
                                    if (daysdiff > cust.getDaysForCredit()) {
                                        if (cdtincmtxn.getCustomerNetPayment() != null) {
                                            accountsReceivablesOverdues += cdtincmtxn.getNetAmount()
                                                    - cdtincmtxn.getCustomerNetPayment();
                                        } else {
                                            accountsReceivablesOverdues += cdtincmtxn.getNetAmount();
                                        }
                                    }
                                }
                            }
                        }
                        // Journal entry receivables: when cust select on lef side increase
                        StringBuilder journalTxnQuery = new StringBuilder(
                                "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                        journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_CUSTOMER).append("'");
                        journalTxnQuery.append(
                                " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.debitBranch='");
                        journalTxnQuery.append(usrRghtBnchs.getId()).append("' and obj1.provisionMadeForOrganization=")
                                .append(usrRghtBnchs.getOrganization().getId());
                        journalTxnQuery.append(
                                " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                        journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                        String sqlStr = journalTxnQuery.toString();
                        sqlStr = sqlStr.replace("(1?)", "1");
                        List<IdosProvisionJournalEntry> provJourTxn = genericDao.executeSimpleQuery(sqlStr,
                                entityManager);
                        if (provJourTxn.size() > 0) {
                            Object val = provJourTxn.get(0);
                            if (val != null) {
                                accountsReceivables += IdosUtil.convertStringToDouble(String.valueOf(val));
                            }
                        }
                        /* now fetching credit records */
                        sqlStr = journalTxnQuery.toString();
                        sqlStr = sqlStr.replace("(1?)", "0");
                        provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                        if (provJourTxn.size() > 0) {
                            Object val = provJourTxn.get(0);
                            if (val != null) {
                                accountsReceivables -= IdosUtil.convertStringToDouble(String.valueOf(val));
                            }
                        }
                        row.put("accountsReceivables",
                                usrRghtBnchs.getBranch().getName() + ":"
                                        + IdosConstants.decimalFormat.format(accountsReceivables) + ":"
                                        + usrRghtBnchs.getBranch().getId());
                        row.put("accountsReceivablesOverdues",
                                usrRghtBnchs.getBranch().getName() + ":"
                                        + IdosConstants.decimalFormat.format(accountsReceivablesOverdues) + ":"
                                        + usrRghtBnchs.getBranch().getId());
                        branchWiseCashBankRecivablesPayablesan.add(row);
                    } else if (tabElement != null && (tabElement.equals("accountsPayablesAllBranches")
                            || tabElement.equals("payableOverduesAllBranches"))) {
                        StringBuilder branchcreditexpensebquery = new StringBuilder("");
                        branchcreditexpensebquery.append("select obj from Transaction obj WHERE obj.transactionBranch='"
                                + usrRghtBnchs.getBranch().getId() + "' and obj.transactionBranchOrganization='"
                                + usrRghtBnchs.getOrganization().getId()
                                + "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate  between '"
                                + finStartDate + "' and '" + finEndDate + "'");
                        List<Transaction> bnchcreditexpensetxn = genericDao
                                .executeSimpleQuery(branchcreditexpensebquery.toString(), entityManager);
                        Vendor vend = null;
                        for (Transaction cdtexptxn : bnchcreditexpensetxn) {
                            if (cdtexptxn.getVendorNetPayment() != null) {
                                accountsPayables += cdtexptxn.getNetAmount() - cdtexptxn.getVendorNetPayment();
                            } else {
                                accountsPayables += cdtexptxn.getNetAmount();
                            }
                            if (cdtexptxn.getTransactionVendorCustomer() != null) {
                                vend = cdtexptxn.getTransactionVendorCustomer();
                                if ((vend.getPurchaseType() == 0 || vend.getPurchaseType() == 2)
                                        && vend.getDaysForCredit() != null) {
                                    int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
                                            - cdtexptxn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
                                    if (daysdiff > vend.getDaysForCredit()) {
                                        if (cdtexptxn.getVendorNetPayment() != null) {
                                            accountsPayablesOverdues += cdtexptxn.getNetAmount()
                                                    - cdtexptxn.getVendorNetPayment();
                                        } else {
                                            accountsPayablesOverdues += cdtexptxn.getNetAmount();
                                        }
                                    }
                                }
                            }
                        }
                        // Journal entry payables: when vend select on lef side decrease
                        StringBuilder journalTxnQuery = new StringBuilder(
                                "select sum(obj.headAmount) from ProvisionJournalEntryDetail obj where obj.isDebit=(1?) ");
                        journalTxnQuery.append(" and obj.headType='").append(IdosConstants.HEAD_VENDOR).append("'");
                        journalTxnQuery.append(
                                " and obj.presentStatus=1 and obj.provisionJournalEntry in (select obj1.id from IdosProvisionJournalEntry obj1 WHERE obj1.debitBranch='");
                        journalTxnQuery.append(usrRghtBnchs.getId()).append("' and obj1.provisionMadeForOrganization=")
                                .append(usrRghtBnchs.getOrganization().getId());
                        journalTxnQuery.append(
                                " AND obj1.transactionPurpose=20 and obj1.presentStatus=1 and obj1.transactionStatus='Accounted' and obj1.transactionDate  between '");
                        journalTxnQuery.append(finStartDate).append("' and '").append(finEndDate).append("')");
                        String sqlStr = journalTxnQuery.toString();
                        sqlStr = sqlStr.replace("(1?)", "1");
                        List<IdosProvisionJournalEntry> provJourTxn = genericDao.executeSimpleQuery(sqlStr,
                                entityManager);
                        if (provJourTxn.size() > 0) {
                            Object val = provJourTxn.get(0);
                            if (val != null) {
                                accountsPayables -= IdosUtil.convertStringToDouble(String.valueOf(val));
                            }
                        }
                        /* now fetching credit records */
                        sqlStr = journalTxnQuery.toString();
                        sqlStr = sqlStr.replace("(1?)", "0");
                        provJourTxn = genericDao.executeSimpleQuery(sqlStr, entityManager);
                        if (provJourTxn.size() > 0) {
                            Object val = provJourTxn.get(0);
                            if (val != null) {
                                accountsPayables += IdosUtil.convertStringToDouble(String.valueOf(val));
                            }
                        }
                        row.put("accountsPayables",
                                usrRghtBnchs.getBranch().getName() + ":"
                                        + IdosConstants.decimalFormat.format(accountsPayables) + ":"
                                        + usrRghtBnchs.getBranch().getId());
                        row.put("accountsPayablesOverdues",
                                usrRghtBnchs.getBranch().getName() + ":"
                                        + IdosConstants.decimalFormat.format(accountsPayablesOverdues) + ":"
                                        + usrRghtBnchs.getBranch().getId());
                        branchWiseCashBankRecivablesPayablesan.add(row);
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
        }
        log.log(Level.FINE, "************* End " + result);
        return result;
    }

    @Override
    public ObjectNode wightedAverageForTransaction(Users user,
            Transaction transaction, String period, EntityManager entityManager) {
        ObjectNode result = Json.newObject();
        ArrayNode branchVendorWiseWeightedAveragePricean = result.putArray("branchVendorWiseWeightedAveragePriceData");
        ArrayNode branchVendorWiseMinMaxWeightedAveragePricean = result
                .putArray("branchVendorWiseMinMaxWeightedAveragePriceData");
        ArrayNode selectedTransactionMinWeightedAveragePricean = result
                .putArray("selectedTransactionMinWeightedAveragePriceData");
        try {
            Map<String, Object> criterias = new HashMap<String, Object>();
            Specifics txnspecf = transaction.getTransactionSpecifics();
            List<BranchSpecifics> specificsAvailableInBranches = txnspecf.getSpecificsBranch();
            Double minGrossWeightedAverage = 0.0;
            Double maxGrossWeightedAverage = 0.0;
            Double minNetWeightedAverage = 0.0;
            Double maxNetWeightedAverage = 0.0;
            Double vendorminGrossWeightedAverage = 0.0;
            Double vendormaxGrossWeightedAverage = 0.0;
            Double vendorminNetWeightedAverage = 0.0;
            Double vendormaxNetWeightedAverage = 0.0;
            String periodStartDate = null;
            String periodEndDate = null;
            Calendar newcal = Calendar.getInstance();
            if (period != null && period.equals("1")) {
                newcal.add(Calendar.DAY_OF_WEEK, -(1 * 30));
                periodStartDate = StaticController.mysqldf.format(newcal.getTime());
                periodEndDate = StaticController.mysqldf.format(Calendar.getInstance().getTime());
            }
            if (period != null && period.equals("3")) {
                newcal.add(Calendar.DAY_OF_WEEK, -(3 * 30));
                periodStartDate = StaticController.mysqldf.format(newcal.getTime());
                periodEndDate = StaticController.mysqldf.format(Calendar.getInstance().getTime());
            }
            if (period != null && period.equals("6")) {
                newcal.add(Calendar.DAY_OF_WEEK, -(6 * 30));
                periodStartDate = StaticController.mysqldf.format(newcal.getTime());
                periodEndDate = StaticController.mysqldf.format(Calendar.getInstance().getTime());
            }
            if (period != null && period.equals("12")) {
                newcal.add(Calendar.DAY_OF_WEEK, -(12 * 30));
                periodStartDate = StaticController.mysqldf.format(newcal.getTime());
                periodEndDate = StaticController.mysqldf.format(Calendar.getInstance().getTime());
            }
            for (BranchSpecifics bnchSpecf : specificsAvailableInBranches) {
                List<VendorSpecific> branchVendors = null;
                criterias.clear();
                criterias.put("organization.id", bnchSpecf.getOrganization().getId());
                criterias.put("specificsVendors.id", txnspecf.getId());
                criterias.put("particulars.id", txnspecf.getParticularsId().getId());
                if (transaction.getTransactionPurpose().getTransactionPurpose()
                        .equals("Sell on cash & collect payment now")
                        || transaction.getTransactionPurpose().getTransactionPurpose()
                                .equals("Sell on credit & collect payment later")) {
                    criterias.put("vendorSpecific.type", 2);
                    criterias.put("presentStatus", 1);
                    branchVendors = genericDao.findByCriteria(VendorSpecific.class, criterias, entityManager);
                } else {
                    criterias.put("vendorSpecific.type", 1);
                    criterias.put("presentStatus", 1);
                    branchVendors = genericDao.findByCriteria(VendorSpecific.class, criterias, entityManager);
                }
                Double grossWeightedAverage = 0.0;
                Double netWeightedAverage = 0.0;
                ObjectNode row = Json.newObject();
                StringBuilder sumunitsbquery = new StringBuilder("");
                sumunitsbquery.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranch='"
                        + bnchSpecf.getBranch().getId() + "' and obj.transactionBranchOrganization='"
                        + bnchSpecf.getOrganization().getId() + "' AND obj.transactionSpecifics='" + txnspecf.getId()
                        + "' AND obj.transactionPurpose='" + transaction.getTransactionPurpose().getId()
                        + "' and (obj.transactionStatus='Accounted' or obj.transactionStatus='Approved') and obj.presentStatus=1 and obj.transactionDate  between '"
                        + periodStartDate + "' and '" + periodEndDate + "'");
                List<Transaction> sumunitsbquerytxn = genericDao.executeSimpleQuery(sumunitsbquery.toString(),
                        entityManager);
                Double totalUnit = null;
                if (sumunitsbquerytxn.size() > 0) {
                    Object val = sumunitsbquerytxn.get(0);
                    if (val != null) {
                        totalUnit = Double.valueOf(val.toString());
                        row.put("totalUnitSold", IdosConstants.decimalFormat.format(val));
                    } else {
                        row.put("totalUnitSold", "");
                    }
                }
                StringBuilder sumgrosssbquery = new StringBuilder("");
                sumgrosssbquery.append("select SUM(obj.grossAmount) from Transaction obj WHERE obj.transactionBranch='"
                        + bnchSpecf.getBranch().getId() + "' and obj.transactionBranchOrganization='"
                        + bnchSpecf.getOrganization().getId() + "' AND obj.transactionSpecifics='" + txnspecf.getId()
                        + "' AND obj.transactionPurpose='" + transaction.getTransactionPurpose().getId()
                        + "' and (obj.transactionStatus='Accounted' or obj.transactionStatus='Approved') and obj.presentStatus=1 and obj.transactionDate  between '"
                        + periodStartDate + "' and '" + periodEndDate + "'");
                List<Transaction> sumgrosssbquerytxn = genericDao.executeSimpleQuery(sumgrosssbquery.toString(),
                        entityManager);
                if (sumgrosssbquerytxn.size() > 0) {
                    Object val = sumgrosssbquerytxn.get(0);
                    if (val != null && totalUnit != null) {
                        grossWeightedAverage = (Double.valueOf(val.toString()) / (totalUnit));
                        row.put("grossWeightedAverage", IdosConstants.decimalFormat.format(grossWeightedAverage));
                        if (minGrossWeightedAverage == 0.0) {
                            minGrossWeightedAverage = grossWeightedAverage;
                        } else {
                            if (grossWeightedAverage < minGrossWeightedAverage) {
                                minGrossWeightedAverage = grossWeightedAverage;
                            }
                        }
                        if (maxGrossWeightedAverage == 0.0) {
                            maxGrossWeightedAverage = grossWeightedAverage;
                        } else {
                            if (grossWeightedAverage > maxGrossWeightedAverage) {
                                maxGrossWeightedAverage = grossWeightedAverage;
                            }
                        }
                    } else {
                        row.put("grossWeightedAverage", "");
                    }
                }
                if (transaction.getTransactionBranch().getId() == bnchSpecf.getBranch().getId()) {
                    ObjectNode txnbnchrow = Json.newObject();
                    txnbnchrow.put("transactionBranchMinWeightedAverage",
                            IdosConstants.decimalFormat.format(grossWeightedAverage));
                    selectedTransactionMinWeightedAveragePricean.add(txnbnchrow);
                }
                StringBuilder sumnetsbquery = new StringBuilder("");
                sumnetsbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
                        + bnchSpecf.getBranch().getId() + "' and obj.transactionBranchOrganization='"
                        + bnchSpecf.getOrganization().getId() + "' AND obj.transactionSpecifics='" + txnspecf.getId()
                        + "' AND obj.transactionPurpose='" + transaction.getTransactionPurpose().getId()
                        + "' and obj.presentStatus=1 and (obj.transactionStatus='Accounted' or obj.transactionStatus='Approved') and obj.transactionDate  between '"
                        + periodStartDate + "' and '" + periodEndDate + "'");
                List<Transaction> sumnetsbquerytxn = genericDao.executeSimpleQuery(sumnetsbquery.toString(),
                        entityManager);
                if (sumnetsbquerytxn.size() > 0) {
                    Object val = sumnetsbquerytxn.get(0);
                    if (val != null && totalUnit != null) {
                        netWeightedAverage = (Double.valueOf(val.toString()) / (totalUnit));
                        row.put("netWeightedAverage", IdosConstants.decimalFormat.format(netWeightedAverage));
                        if (minNetWeightedAverage == 0.0) {
                            minNetWeightedAverage = netWeightedAverage;
                        } else {
                            if (netWeightedAverage < minNetWeightedAverage) {
                                minNetWeightedAverage = netWeightedAverage;
                            }
                        }
                        if (maxNetWeightedAverage == 0.0) {
                            maxNetWeightedAverage = netWeightedAverage;
                        } else {
                            if (netWeightedAverage > maxNetWeightedAverage) {
                                maxNetWeightedAverage = netWeightedAverage;
                            }
                        }
                    } else {
                        row.put("netWeightedAverage", "");
                    }
                }
                row.put("branchVendorName", bnchSpecf.getBranch().getName());
                row.put("itemName", "");
                row.put("itemSpecificsName", bnchSpecf.getSpecifics().getName());
                if (bnchSpecf.getBranch().getPhoneNumber() != null) {
                    row.put("period", bnchSpecf.getBranch().getPhoneNumber());
                } else {
                    row.put("period", "");
                }
                if (transaction.getTransactionPurpose().getTransactionPurpose()
                        .equals("Sell on cash & collect payment now")
                        || transaction.getTransactionPurpose().getTransactionPurpose()
                                .equals("Sell on credit & collect payment later")) {
                    row.put("vendCustBreakups", "CUSTOMER WISE WAP BREAKUPS");
                } else {
                    row.put("vendCustBreakups", "VENDOR WISE WAP BREAKUPS");
                }
                branchVendorWiseWeightedAveragePricean.add(row);
                if (branchVendors != null) {
                    if (branchVendors.size() > 0) {
                        for (VendorSpecific bnchVendors : branchVendors) {
                            criterias.clear();
                            criterias.put("branch.id", bnchSpecf.getBranch().getId());
                            criterias.put("organization.id", bnchSpecf.getOrganization().getId());
                            criterias.put("vendor.id", bnchVendors.getVendorSpecific().getId());
                            if (transaction.getTransactionPurpose().getTransactionPurpose()
                                    .equals("Sell on cash & collect payment now")
                                    || transaction.getTransactionPurpose().getTransactionPurpose()
                                            .equals("Sell on credit & collect payment later")) {
                                criterias.put("vendor.type", 2);
                            } else {
                                criterias.put("vendor.type", 1);
                            }
                            criterias.put("presentStatus", 1);
                            BranchVendors bnchVend = genericDao.getByCriteria(BranchVendors.class, criterias,
                                    entityManager);
                            if (bnchVend != null) {
                                Double vendorgrossWeightedAverage = 0.0;
                                Double vendornetWeightedAverage = 0.0;
                                ObjectNode vendorrow = Json.newObject();
                                StringBuilder vendorsumunitsbquery = new StringBuilder("");
                                vendorsumunitsbquery.append(
                                        "select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranch='"
                                                + bnchSpecf.getBranch().getId()
                                                + "' and obj.transactionBranchOrganization='"
                                                + bnchSpecf.getOrganization().getId()
                                                + "' AND obj.transactionSpecifics='" + txnspecf.getId()
                                                + "' AND obj.transactionVendorCustomer='"
                                                + bnchVendors.getVendorSpecific().getId()
                                                + "' AND obj.transactionPurpose='"
                                                + transaction.getTransactionPurpose().getId()
                                                + "' and (obj.transactionStatus='Accounted' or obj.transactionStatus='Approved') and obj.presentStatus=1 and obj.transactionDate  between '"
                                                + periodStartDate + "' and '" + periodEndDate + "'");
                                List<Transaction> vendorsumunitsbquerytxn = genericDao
                                        .executeSimpleQuery(vendorsumunitsbquery.toString(), entityManager);
                                Double vendortotalUnit = null;
                                if (vendorsumunitsbquerytxn.size() > 0) {
                                    Object val = vendorsumunitsbquerytxn.get(0);
                                    if (val != null) {
                                        vendortotalUnit = Double.valueOf(val.toString());
                                        vendorrow.put("totalUnitSold", IdosConstants.decimalFormat.format(val));
                                    } else {
                                        vendorrow.put("totalUnitSold", "");
                                    }
                                }
                                StringBuilder vendorsumgrosssbquery = new StringBuilder("");
                                vendorsumgrosssbquery.append(
                                        "select SUM(obj.grossAmount) from Transaction obj WHERE obj.transactionBranch='"
                                                + bnchSpecf.getBranch().getId()
                                                + "' and obj.transactionBranchOrganization='"
                                                + bnchSpecf.getOrganization().getId()
                                                + "' AND obj.transactionSpecifics='" + txnspecf.getId()
                                                + "' AND obj.transactionVendorCustomer='"
                                                + bnchVendors.getVendorSpecific().getId()
                                                + "' AND obj.transactionPurpose='"
                                                + transaction.getTransactionPurpose().getId()
                                                + "' and (obj.transactionStatus='Accounted' or obj.transactionStatus='Approved') and obj.presentStatus=1 and obj.transactionDate  between '"
                                                + periodStartDate + "' and '" + periodEndDate + "'");
                                List<Transaction> vendorsumgrosssbquerytxn = genericDao
                                        .executeSimpleQuery(vendorsumgrosssbquery.toString(), entityManager);
                                if (vendorsumgrosssbquerytxn.size() > 0) {
                                    Object val = vendorsumgrosssbquerytxn.get(0);
                                    if (val != null && vendortotalUnit != null) {
                                        vendorgrossWeightedAverage = (Double.valueOf(val.toString())
                                                / (vendortotalUnit));
                                        vendorrow.put("grossWeightedAverage",
                                                IdosConstants.decimalFormat.format(vendorgrossWeightedAverage));
                                        if (vendorminGrossWeightedAverage == 0.0) {
                                            vendorminGrossWeightedAverage = vendorgrossWeightedAverage;
                                        } else {
                                            if (vendorgrossWeightedAverage < vendorminGrossWeightedAverage) {
                                                vendorminGrossWeightedAverage = vendorgrossWeightedAverage;
                                            }
                                        }
                                        if (vendormaxGrossWeightedAverage == 0.0) {
                                            vendormaxGrossWeightedAverage = vendorgrossWeightedAverage;
                                        } else {
                                            if (vendorgrossWeightedAverage > vendormaxGrossWeightedAverage) {
                                                vendormaxGrossWeightedAverage = vendorgrossWeightedAverage;
                                            }
                                        }
                                    } else {
                                        vendorrow.put("grossWeightedAverage", "");
                                    }
                                }
                                StringBuilder vendorsumnetsbquery = new StringBuilder("");
                                vendorsumnetsbquery.append(
                                        "select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
                                                + bnchSpecf.getBranch().getId()
                                                + "' and obj.transactionBranchOrganization='"
                                                + bnchSpecf.getOrganization().getId()
                                                + "' AND obj.transactionSpecifics='" + txnspecf.getId()
                                                + "' AND obj.transactionVendorCustomer='"
                                                + bnchVendors.getVendorSpecific().getId()
                                                + "' AND obj.transactionPurpose='"
                                                + transaction.getTransactionPurpose().getId()
                                                + "' and (obj.transactionStatus='Accounted' or obj.transactionStatus='Approved') and obj.presentStatus=1 and obj.transactionDate  between '"
                                                + periodStartDate + "' and '" + periodEndDate + "'");
                                List<Transaction> vendorsumnetsbquerytxn = genericDao
                                        .executeSimpleQuery(vendorsumnetsbquery.toString(), entityManager);
                                if (vendorsumnetsbquerytxn.size() > 0) {
                                    Object val = vendorsumnetsbquerytxn.get(0);
                                    if (val != null && totalUnit != null) {
                                        vendornetWeightedAverage = (Double.valueOf(val.toString()) / (vendortotalUnit));
                                        vendorrow.put("netWeightedAverage",
                                                IdosConstants.decimalFormat.format(vendornetWeightedAverage));
                                        if (vendorminNetWeightedAverage == 0.0) {
                                            vendorminNetWeightedAverage = vendornetWeightedAverage;
                                        } else {
                                            if (vendornetWeightedAverage < vendorminNetWeightedAverage) {
                                                vendorminNetWeightedAverage = vendornetWeightedAverage;
                                            }
                                        }
                                        if (vendormaxNetWeightedAverage == 0.0) {
                                            vendormaxNetWeightedAverage = vendornetWeightedAverage;
                                        } else {
                                            if (vendornetWeightedAverage > vendormaxNetWeightedAverage) {
                                                vendormaxNetWeightedAverage = vendornetWeightedAverage;
                                            }
                                        }
                                    } else {
                                        vendorrow.put("netWeightedAverage", "");
                                    }
                                }
                                vendorrow.put("branchVendorName", bnchVendors.getVendorSpecific().getName());
                                vendorrow.put("itemSpecificsName", bnchVendors.getSpecificsVendors().getName());
                                if (bnchVendors.getVendorSpecific().getEmail() != null) {
                                    vendorrow.put("itemName", bnchVendors.getVendorSpecific().getEmail());
                                } else {
                                    vendorrow.put("itemName", "");
                                }
                                if (bnchVendors.getVendorSpecific().getPhone() != null) {
                                    vendorrow.put("period", bnchVendors.getVendorSpecific().getPhone());
                                } else {
                                    vendorrow.put("period", "");
                                }
                                branchVendorWiseWeightedAveragePricean.add(vendorrow);
                            }
                        }
                    }
                }
            }
            ObjectNode minmaxrow = Json.newObject();
            minmaxrow.put("minGrossWeightedAverage", IdosConstants.decimalFormat.format(minGrossWeightedAverage));
            minmaxrow.put("maxGrossWeightedAverage", IdosConstants.decimalFormat.format(maxGrossWeightedAverage));
            minmaxrow.put("minNetWeightedAverage", IdosConstants.decimalFormat.format(minNetWeightedAverage));
            minmaxrow.put("maxNetWeightedAverage", IdosConstants.decimalFormat.format(maxNetWeightedAverage));
            minmaxrow.put("vendorminGrossWeightedAverage",
                    IdosConstants.decimalFormat.format(vendorminGrossWeightedAverage));
            minmaxrow.put("vendormaxGrossWeightedAverage",
                    IdosConstants.decimalFormat.format(vendormaxGrossWeightedAverage));
            minmaxrow.put("vendorminNetWeightedAverage",
                    IdosConstants.decimalFormat.format(vendorminNetWeightedAverage));
            minmaxrow.put("vendormaxNetWeightedAverage",
                    IdosConstants.decimalFormat.format(vendormaxNetWeightedAverage));
            branchVendorWiseMinMaxWeightedAveragePricean.add(minmaxrow);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return result;
    }

    public static String getUserRoles(Users user) {
        String userRolesStr = "";
        List<UsersRoles> listuserRoles = user.getUserRoles();
        for (UsersRoles usrRole : listuserRoles) {
            userRolesStr += usrRole.getRole().getName() + ",";
        }
        String actUserRoles = userRolesStr.substring(0, userRolesStr.length());
        return actUserRoles;
    }

    @Override
    public ObjectNode documentRule(Users user, String txnForExpItem, String txnForExpBranch, String txnForExpNetAmount)
            throws IDOSException {
        ObjectNode result = Json.newObject();
        ArrayNode documentRuleMessagean = result.putArray("documentRuleMessage");
        ObjectNode docrulerow = Json.newObject();
        Specifics txnSpecf = Specifics.findById(IdosUtil.convertStringToLong(txnForExpItem));
        Branch txnBnch = Branch.findById(IdosUtil.convertStringToLong(txnForExpBranch));
        Double netAmount = IdosUtil.convertStringToDouble(txnForExpNetAmount);
        StringBuilder sbr = new StringBuilder("");
        sbr.append("select obj from SpecificsDocUploadMonetoryRuleForBranch obj where obj.specifics='"
                + txnSpecf.getId() + "' and obj.particulars='" + txnSpecf.getParticularsId().getId()
                + "' and obj.branch='" + txnBnch.getId() + "' and obj.presentStatus=1 and obj.organization='"
                + txnBnch.getOrganization().getId() + "'");
        List<SpecificsDocUploadMonetoryRuleForBranch> specfDocUploadMonetoryLimitList = genericDao
                .executeSimpleQuery(sbr.toString(), entityManager);
        if (specfDocUploadMonetoryLimitList.size() > 0) {
            SpecificsDocUploadMonetoryRuleForBranch specfDocUploadMonetoryLimit = specfDocUploadMonetoryLimitList
                    .get(0);
            if (specfDocUploadMonetoryLimit != null) {
                if (specfDocUploadMonetoryLimit.getMonetoryLimit() != null) {
                    Double monetoryLimit = specfDocUploadMonetoryLimit.getMonetoryLimit();
                    if (netAmount > monetoryLimit) {
                        docrulerow.put("ruleMessage", "Required");
                    } else {
                        docrulerow.put("ruleMessage", "Not Required");
                    }
                } else {
                    docrulerow.put("ruleMessage", "Not Required");
                }
            } else {
                docrulerow.put("ruleMessage", "Not Required");
            }
        } else {
            docrulerow.put("ruleMessage", "Not Required");
        }
        documentRuleMessagean.add(docrulerow);
        return result;
    }

    @Override
    public ObjectNode documentRulePVS(Users user, String txnInv, String txnpaymentReceived, EntityManager entityManager) throws IDOSException {
        ObjectNode result = Json.newObject();
        ArrayNode documentRuleMessagean = result.putArray("documentRuleMessage");
        ObjectNode docrulerow = Json.newObject();
        Transaction invtxn = Transaction.findById(IdosUtil.convertStringToLong(txnInv));
        if (txnInv.equals("-1")) { // if opening balance invoice chosen in Pay vendor, then invoice is -1 as it has
                                   // no credit sell transaction associated with it
            docrulerow.put("ruleMessage", "Not Required");
        } else {
            Specifics txnSpecf = invtxn.getTransactionSpecifics();
            Branch txnBnch = invtxn.getTransactionBranch();
            Double netAmount = IdosUtil.convertStringToDouble(txnpaymentReceived);
            StringBuilder sbr = new StringBuilder("");
            sbr.append("select obj from SpecificsDocUploadMonetoryRuleForBranch obj where obj.specifics='"
                    + txnSpecf.getId() + "' and obj.particulars='" + txnSpecf.getParticularsId().getId()
                    + "' and obj.branch='" + txnBnch.getId() + "' and obj.organization='"
                    + txnBnch.getOrganization().getId() + "' and obj.presentStatus=1");
            List<SpecificsDocUploadMonetoryRuleForBranch> specfDocUploadMonetoryLimitList = genericDao
                    .executeSimpleQuery(sbr.toString(), entityManager);
            if (specfDocUploadMonetoryLimitList.size() > 0) {
                SpecificsDocUploadMonetoryRuleForBranch specfDocUploadMonetoryLimit = specfDocUploadMonetoryLimitList
                        .get(0);
                if (specfDocUploadMonetoryLimit != null) {
                    if (specfDocUploadMonetoryLimit.getMonetoryLimit() != null) {
                        Double monetoryLimit = specfDocUploadMonetoryLimit.getMonetoryLimit();
                        if (netAmount > monetoryLimit) {
                            docrulerow.put("ruleMessage", "Required");
                        } else {
                            docrulerow.put("ruleMessage", "Not Required");
                        }
                    } else {
                        docrulerow.put("ruleMessage", "Not Required");
                    }
                } else {
                    docrulerow.put("ruleMessage", "Not Required");
                }
            } else {
                docrulerow.put("ruleMessage", "Not Required");
            }
        }
        documentRuleMessagean.add(docrulerow);
        return result;
    }

    // TODO need to check whether getting used or not
    @Override
    public ObjectNode accountHeadTransactions(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
            EntityTransaction entitytransaction) {
        try {
            ArrayNode an = result.putArray("accountHeadTransactions");
            String accountHeads = json.findValue("accountHeads") != null ? json.findValue("accountHeads").asText()
                    : null;
            if (accountHeads != null && !accountHeads.equals("")) {
                String firstFourDebit = accountHeads.substring(0, 4);
                StringBuilder sbr = new StringBuilder("");
                if (firstFourDebit.equals("item")) {
                    String restAfterFourId = accountHeads.substring(4, accountHeads.length());
                    if (restAfterFourId != null && !restAfterFourId.equals("")) {
                        Specifics specf = Specifics.findById(IdosUtil.convertStringToLong(restAfterFourId));
                        sbr.append("select obj from Transaction where obj.transactionSpecifics='" + specf.getId()
                                + "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
                                + "' and (obj.transactionPurpose=2 or obj.transactionPurpose=4) and obj.transactionStatus='Accounted' and obj.presentStatus=1");
                    }
                }
                if (firstFourDebit.equals("cust")) {
                    String restAfterFourId = accountHeads.substring(4, accountHeads.length());
                    if (restAfterFourId != null && !restAfterFourId.equals("")) {

                    }
                }
                if (firstFourDebit.equals("cash")) {
                    String restAfterFourId = accountHeads.substring(4, accountHeads.length());
                    if (restAfterFourId != null && !restAfterFourId.equals("")) {

                    }
                }
                if (firstFourDebit.equals("bank")) {
                    String restAfterFourId = accountHeads.substring(4, accountHeads.length());
                    if (restAfterFourId != null && !restAfterFourId.equals("")) {

                    }
                }
                if (firstFourDebit.equals("vend")) {
                    String restAfterFourId = accountHeads.substring(4, accountHeads.length());
                    if (restAfterFourId != null && !restAfterFourId.equals("")) {

                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return result;
    }

    @Override
    public Transaction bankServices(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
            EntityTransaction entitytransaction) throws IDOSException {
        Transaction transaction = new Transaction();
        String txnPurpose = json.findValue("txnPurpose").asText();
        String txnPurposeVal = json.findValue("txnPurposeVal").asText();
        TransactionPurpose usertxnPurpose = TransactionPurpose.findById(Long.valueOf(txnPurposeVal));
        String txnbankBranch = json.findValue("txnbankBranch") != null ? json.findValue("txnbankBranch").asText()
                : null;
        String txnbranchBank = json.findValue("txnbranchBank") != null ? json.findValue("txnbranchBank").asText()
                : null;
        String txnbranchBankDetails = json.findValue("txnbranchBankDetails") != null
                ? json.findValue("txnbranchBankDetails").asText()
                : null;
        String txntoBankBranch = json.findValue("txntoBankBranch") != null ? json.findValue("txntoBankBranch").asText()
                : null;
        String txntoBranchBank = json.findValue("txntoBranchBank") != null ? json.findValue("txntoBranchBank").asText()
                : null;
        String txntoBranchBankDetails = json.findValue("txntoBranchBankDetails") != null
                ? json.findValue("txntoBranchBankDetails").asText()
                : null;
        String txnenteredAmount = json.findValue("txnenteredAmount") != null
                ? json.findValue("txnenteredAmount").asText()
                : null;
        String txnresultAmount = json.findValue("txnresultAmount") != null ? json.findValue("txnresultAmount").asText()
                : null;
        String txnpurpose = json.findValue("txnpurpose") != null ? json.findValue("txnpurpose").asText() : null;
        String txnremarks = json.findValue("txnremarks") != null ? json.findValue("txnremarks").asText() : null;
        String supportingdoc = json.findValue("supportingdoc") != null ? json.findValue("supportingdoc").asText()
                : null;
        Branch bnch = null;
        Branch toBranch = null;
        String txnRemarks = "";
        transaction.setTransactionPurpose(usertxnPurpose);
        transaction.setTransactionDate(Calendar.getInstance().getTime());
        if (txnbankBranch != null && !txnbankBranch.equals("")) {
            bnch = Branch.findById(IdosUtil.convertStringToLong(txnbankBranch));
            transaction.setTransactionBranch(bnch);
        }
        if (bnch != null) {
            transaction.setTransactionBranchOrganization(bnch.getOrganization());
        }
        if (txnbranchBank != null && !txnbranchBank.equals("")) {
            BranchBankAccounts fromBankAccount = BranchBankAccounts
                    .findById(IdosUtil.convertStringToLong(txnbranchBank));
            if (fromBankAccount == null) {
                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.INVALID_DATA_EXCEPTION,
                        "Bank is not selected in transaction when payment mode is Bank.");
            }
            transaction.setTransactionBranchBankAccount(fromBankAccount);
        }
        if (txnbranchBankDetails != null && !txnbranchBankDetails.equals("")) {
            transaction.setNetAmountResultDescription(txnbranchBankDetails);
        }
        if (txntoBankBranch != null && !txntoBankBranch.equals("")) {
            toBranch = Branch.findById(IdosUtil.convertStringToLong(txntoBankBranch));
            transaction.setTransactionToBranch(toBranch);
            transaction.setTransactionToBranchOrganization(toBranch.getOrganization());
        }
        if (txntoBranchBank != null && !txntoBranchBank.equals("")) {
            BranchBankAccounts toBankAccount = BranchBankAccounts
                    .findById(IdosUtil.convertStringToLong(txntoBranchBank));
            if (toBankAccount == null) {
                throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        IdosConstants.INVALID_DATA_EXCEPTION,
                        "Bank is not selected in transaction when payment mode is Bank.");
            }
            transaction.setTransactionToBranchBankAccount(toBankAccount);
        }
        if (txntoBranchBankDetails != null && !txntoBranchBankDetails.equals("")) {
            if (transaction.getNetAmountResultDescription() != null) {
                transaction.setNetAmountResultDescription(
                        transaction.getNetAmountResultDescription() + "," + txntoBranchBankDetails);
            }
        }
        if (txnenteredAmount != null && !txnenteredAmount.equals("")) {
            transaction.setGrossAmount(IdosUtil.convertStringToDouble(txnenteredAmount));
            transaction.setNetAmount(IdosUtil.convertStringToDouble(txnenteredAmount));
        }
        if (txnpurpose != null && !txnpurpose.equals("")) {
            if (transaction.getNetAmountResultDescription() != null) {
                transaction.setNetAmountResultDescription(
                        transaction.getNetAmountResultDescription() + "," + "Purpose:" + txnpurpose);
            }
        }
        if (!txnremarks.equals("") && txnremarks != null) {
            txnRemarks = user.getEmail() + "#" + txnremarks;
            transaction.setRemarks(txnRemarks);
        }
        transaction.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(transaction.getSupportingDocs(),
                user.getEmail(), supportingdoc, user, entityManager));
        transaction.setTransactionStatus("Require Approval");
        List<String> emailSet = UserRolesUtil.approverAdditionalApprovalBasedOnSelectedBranch(user, bnch,
                entityManager);
        Object[] array = emailSet.toArray();
        String approverEmails = "";
        String additionalApprovarUsers = "";
        String selectedAdditionalApproval = "";
        for (int i = 0; i < array.length; i++) {
            if (i == 0) {
                approverEmails = array[i].toString();
            }
            if (i == 1) {
                additionalApprovarUsers = array[i].toString();
            }
            if (i == 2) {
                selectedAdditionalApproval = array[i].toString();
            }
        }
        transaction.setApproverEmails(approverEmails);
        transaction.setAdditionalApproverEmails(additionalApprovarUsers);
        String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
        transaction.setTransactionRefNumber(transactionNumber);
        String txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
                ? json.findValue("txnInstrumentNum").asText()
                : null;
        String txnInstrumentDate = json.findValue("txnInstrumentDate") != null
                ? json.findValue("txnInstrumentDate").asText()
                : null;
        transaction.setInstrumentNumber(txnInstrumentNumber);
        transaction.setInstrumentDate(txnInstrumentDate);
        genericDao.saveOrUpdate(transaction, user, entityManager);
        FILE_UPLOAD_DAO.updateUploadFileLogs(entityManager, user, supportingdoc, transaction.getId(),
                IdosConstants.MAIN_TXN_TYPE);
        entitytransaction.commit();
        // Single User
        if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
            ArrayNode singleUserAccounting = result.putArray("singleUserAccounting");
            ObjectNode createSingleuserJson = SINGLE_USER_DAO.createSingleuserJson(transaction, json, user);
            singleUserAccounting.add(createSingleuserJson);
        }
        return transaction;
    }

    @Override
    public void cashBankBalanceEffectWithdrawalFromBank(BranchBankAccounts branchBankAccount, Branch branch,
            Double amount, EntityManager entityManager, EntityTransaction entitytransaction, Users user) {
        Double creditAmount = null;
        Double debitAmount = null;
        Double resultantCash = null;
        Double mainToPettyCash = null;
        Double grandTotal = null;
        StringBuilder newsbquery = new StringBuilder("");
        newsbquery.append("select obj from BranchCashCount obj WHERE obj.branch.id='" + branch.getId()
                + "' AND obj.organization.id='" + branch.getOrganization().getId()
                + "' and obj.presentStatus=1 ORDER BY obj.date desc");
        List<BranchCashCount> branchCashCount = genericDao.executeSimpleQueryWithLimit(newsbquery.toString(),
                entityManager, 1);
        if (branchCashCount.size() > 0) {
            if (branchCashCount.get(0).getCreditAmount() == null) {
                creditAmount = 0.0;
            } else if (branchCashCount.get(0).getCreditAmount() != null) {
                creditAmount = branchCashCount.get(0).getCreditAmount();
            }
            if (branchCashCount.get(0).getDebitAmount() == null) {
                debitAmount = amount;
            } else if (branchCashCount.get(0).getDebitAmount() != null) {
                debitAmount = branchCashCount.get(0).getDebitAmount() + amount;
            }
            if (branchCashCount.get(0).getTotalMainCashToPettyCash() != null) {
                mainToPettyCash = branchCashCount.get(0).getTotalMainCashToPettyCash();
            }
            if (branchCashCount.get(0).getTotalMainCashToPettyCash() == null) {
                mainToPettyCash = 0.0;
            }
            if (branchCashCount.get(0).getGrandTotal() != null) {
                grandTotal = branchCashCount.get(0).getGrandTotal();
            } else {
                grandTotal = 0.0;
            }
            resultantCash = grandTotal + debitAmount - creditAmount - mainToPettyCash;
            branchCashCount.get(0).setDebitAmount(debitAmount);
            branchCashCount.get(0).setResultantCash(resultantCash);
            genericDao.saveOrUpdate(branchCashCount.get(0), user, entityManager);
        }
        Double creditAmountBank = null;
        Double debitAmountBank = null;
        Double resultantAmountBank = null;
        Double amountBalanceBank = null;
        newsbquery.delete(0, newsbquery.length());
        newsbquery.append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='" + branch.getId()
                + "' AND obj.organization.id='" + branch.getOrganization().getId() + "' and obj.branchBankAccounts.id='"
                + branchBankAccount.getId() + "' and obj.presentStatus=1 ORDER BY obj.date desc");
        List<BranchBankAccountBalance> branchBankAccountBal = genericDao
                .executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
        if (branchBankAccountBal.size() > 0) {
            if (branchBankAccountBal.get(0).getCreditAmount() == null) {
                creditAmountBank = amount;
            } else if (branchBankAccountBal.get(0).getCreditAmount() != null) {
                creditAmountBank = branchBankAccountBal.get(0).getCreditAmount() + amount;
            }
            if (branchBankAccountBal.get(0).getDebitAmount() == null) {
                debitAmountBank = 0.0;
            } else if (branchBankAccountBal.get(0).getDebitAmount() != null) {
                debitAmountBank = branchBankAccountBal.get(0).getDebitAmount();
            }
            if (branchBankAccountBal.get(0).getAmountBalance() != null) {
                amountBalanceBank = branchBankAccountBal.get(0).getAmountBalance();
            } else {
                amountBalanceBank = 0.0;
            }
            resultantAmountBank = amountBalanceBank + debitAmountBank - creditAmountBank;
            branchBankAccountBal.get(0).setCreditAmount(creditAmountBank);
            branchBankAccountBal.get(0).setDebitAmount(debitAmountBank);
            branchBankAccountBal.get(0).setResultantCash(resultantAmountBank);
            genericDao.saveOrUpdate(branchBankAccountBal.get(0), user, entityManager);
        }
    }

    @Override
    public void cashBankBalanceEffectDepositToBank(BranchBankAccounts branchBankAccount, Branch branch, Double amount,
            EntityManager entityManager, EntityTransaction entitytransaction, Users user) {
        Double creditAmount = null;
        Double debitAmount = null;
        Double resultantCash = null;
        Double mainToPettyCash = null;
        Double grandTotal = null;
        StringBuilder newsbquery = new StringBuilder("");
        newsbquery.append("select obj from BranchCashCount obj WHERE obj.branch.id='" + branch.getId()
                + "' AND obj.organization.id='" + branch.getOrganization().getId()
                + "' and obj.presentStatus=1 ORDER BY obj.date desc");
        List<BranchCashCount> branchCashCount = genericDao.executeSimpleQueryWithLimit(newsbquery.toString(),
                entityManager, 1);
        if (branchCashCount.size() > 0) {
            if (branchCashCount.get(0).getCreditAmount() == null) {
                creditAmount = 0.0;
            }
            if (branchCashCount.get(0).getCreditAmount() != null) {
                creditAmount = branchCashCount.get(0).getCreditAmount();
            }
            if (branchCashCount.get(0).getDebitAmount() == null) {
                debitAmount = amount;
            }
            if (branchCashCount.get(0).getDebitAmount() != null) {
                debitAmount = branchCashCount.get(0).getDebitAmount() + amount;
            }
            if (branchCashCount.get(0).getTotalMainCashToPettyCash() != null) {
                mainToPettyCash = branchCashCount.get(0).getTotalMainCashToPettyCash();
            }
            if (branchCashCount.get(0).getTotalMainCashToPettyCash() == null) {
                mainToPettyCash = 0.0;
            }
            if (branchCashCount.get(0).getGrandTotal() != null) {
                grandTotal = branchCashCount.get(0).getGrandTotal();
            } else {
                grandTotal = 0.0;
            }
            resultantCash = grandTotal + creditAmount - debitAmount - mainToPettyCash;
            branchCashCount.get(0).setDebitAmount(debitAmount);
            branchCashCount.get(0).setResultantCash(resultantCash);
            genericDao.saveOrUpdate(branchCashCount.get(0), user, entityManager);
        }
        Double creditAmountBank = null;
        Double debitAmountBank = null;
        Double resultantAmountBank = null;
        Double amountBalanceBank = null;
        newsbquery.delete(0, newsbquery.length());
        newsbquery.append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='" + branch.getId()
                + "' AND obj.organization.id='" + branch.getOrganization().getId() + "' and obj.branchBankAccounts.id='"
                + branchBankAccount.getId() + "' and obj.presentStatus=1 ORDER BY obj.date desc");
        List<BranchBankAccountBalance> branchBankAccountBal = genericDao
                .executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
        if (branchBankAccountBal.size() > 0) {
            if (branchBankAccountBal.get(0).getCreditAmount() == null) {
                creditAmountBank = amount;
            }
            if (branchBankAccountBal.get(0).getCreditAmount() != null) {
                creditAmountBank = branchBankAccountBal.get(0).getCreditAmount() + amount;
            }
            if (branchBankAccountBal.get(0).getDebitAmount() == null) {
                debitAmountBank = 0.0;
            }
            if (branchBankAccountBal.get(0).getDebitAmount() != null) {
                debitAmountBank = branchBankAccountBal.get(0).getDebitAmount();
            }
            if (branchBankAccountBal.get(0).getAmountBalance() != null) {
                amountBalanceBank = branchBankAccountBal.get(0).getAmountBalance();
            } else {
                amountBalanceBank = 0.0;
            }
            resultantAmountBank = amountBalanceBank + creditAmountBank - debitAmountBank;
            branchBankAccountBal.get(0).setCreditAmount(creditAmountBank);
            branchBankAccountBal.get(0).setDebitAmount(debitAmountBank);
            branchBankAccountBal.get(0).setResultantCash(resultantAmountBank);
            genericDao.saveOrUpdate(branchBankAccountBal.get(0), user, entityManager);
        }
    }

    @Override
    public void cashBankBalanceEffectTransferFromOneBankToAnotherBank(BranchBankAccounts fromBranchBankAccount,
            Branch fromBranch, BranchBankAccounts toBranchBankAccount, Branch toBranch, Double amount,
            EntityManager entityManager, EntityTransaction entitytransaction, Users user) {
        Double creditAmountBank = null;
        Double debitAmountBank = null;
        Double resultantAmountBank = null;
        Double amountBalanceBank = null;
        StringBuilder newsbquery = new StringBuilder("");
        newsbquery.append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='" + fromBranch.getId()
                + "' AND obj.organization.id='" + fromBranch.getOrganization().getId()
                + "' and obj.branchBankAccounts.id='" + fromBranchBankAccount.getId()
                + "' and obj.presentStatus=1 ORDER BY obj.date desc");
        List<BranchBankAccountBalance> branchBankAccountBal = genericDao
                .executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
        if (branchBankAccountBal.size() > 0) {
            if (branchBankAccountBal.get(0).getCreditAmount() == null) {
                creditAmountBank = 0.0;
            }
            if (branchBankAccountBal.get(0).getCreditAmount() != null) {
                creditAmountBank = branchBankAccountBal.get(0).getCreditAmount();
            }
            if (branchBankAccountBal.get(0).getDebitAmount() == null) {
                debitAmountBank = amount;
            }
            if (branchBankAccountBal.get(0).getDebitAmount() != null) {
                debitAmountBank = branchBankAccountBal.get(0).getDebitAmount() + amount;
            }
            if (branchBankAccountBal.get(0).getAmountBalance() != null) {
                amountBalanceBank = branchBankAccountBal.get(0).getAmountBalance();
            } else {
                amountBalanceBank = 0.0;
            }
            resultantAmountBank = amountBalanceBank + creditAmountBank - debitAmountBank;
            branchBankAccountBal.get(0).setCreditAmount(creditAmountBank);
            branchBankAccountBal.get(0).setDebitAmount(debitAmountBank);
            branchBankAccountBal.get(0).setResultantCash(resultantAmountBank);
            genericDao.saveOrUpdate(branchBankAccountBal.get(0), user, entityManager);
        }
        newsbquery.delete(0, newsbquery.length());
        Double creditToAmountBank = null;
        Double debitToAmountBank = null;
        Double resultantToAmountBank = null;
        Double amountToBalanceBank = null;
        newsbquery.append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='" + toBranch.getId()
                + "' AND obj.organization.id='" + toBranch.getOrganization().getId()
                + "' and obj.branchBankAccounts.id='" + toBranchBankAccount.getId()
                + "' and obj.presentStatus=1 ORDER BY obj.date desc");
        List<BranchBankAccountBalance> toBranchBankAccountBal = genericDao
                .executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
        if (toBranchBankAccountBal.size() > 0) {
            if (toBranchBankAccountBal.get(0).getCreditAmount() == null) {
                creditToAmountBank = amount;
            }
            if (toBranchBankAccountBal.get(0).getCreditAmount() != null) {
                creditToAmountBank = toBranchBankAccountBal.get(0).getCreditAmount() + amount;
            }
            if (toBranchBankAccountBal.get(0).getDebitAmount() == null) {
                debitToAmountBank = 0.0;
            }
            if (toBranchBankAccountBal.get(0).getDebitAmount() != null) {
                debitToAmountBank = toBranchBankAccountBal.get(0).getDebitAmount();
            }
            if (toBranchBankAccountBal.get(0).getAmountBalance() != null) {
                amountToBalanceBank = toBranchBankAccountBal.get(0).getAmountBalance();
            } else {
                amountToBalanceBank = 0.0;
            }
            resultantToAmountBank = amountToBalanceBank + creditAmountBank - debitAmountBank;
            toBranchBankAccountBal.get(0).setCreditAmount(creditToAmountBank);
            toBranchBankAccountBal.get(0).setDebitAmount(debitToAmountBank);
            toBranchBankAccountBal.get(0).setResultantCash(resultantToAmountBank);
            genericDao.saveOrUpdate(toBranchBankAccountBal.get(0), user, entityManager);
        }
    }

    @Override
    public ObjectNode checkMaxDiscountForWalkinCust(ObjectNode result,
            JsonNode json, Users user, EntityManager entityManager) {
        try {
            Map<String, Object> criterias = new HashMap<String, Object>();
            result.put("result", false);
            ArrayNode an = result.putArray("maxWalikinCustomerDiscountData");
            String bnchPrimaryKeyId = json.findValue("bnchPrimaryKeyId") != null
                    ? json.findValue("bnchPrimaryKeyId").asText()
                    : null;
            String specfPrimaryKeyId = json.findValue("specfPrimaryKeyId") != null
                    ? json.findValue("specfPrimaryKeyId").asText()
                    : null;
            String enteredDiscount = json.findValue("enteredDiscountValue") != null
                    ? json.findValue("enteredDiscountValue").asText()
                    : null;
            Branch bnch = null;
            Specifics specf = null;
            Double enteredDiscountValue = 0.0;
            Double configuredDiscountValue = 0.0;
            if (bnchPrimaryKeyId != null && !bnchPrimaryKeyId.equals("")) {
                bnch = Branch.findById(IdosUtil.convertStringToLong(bnchPrimaryKeyId));
            }
            if (specfPrimaryKeyId != null && !specfPrimaryKeyId.equals("")) {
                specf = Specifics.findById(IdosUtil.convertStringToLong(specfPrimaryKeyId));
            }
            if (enteredDiscount != null && !enteredDiscount.equals("")) {
                enteredDiscountValue = IdosUtil.convertStringToDouble(enteredDiscount);
            }
            if (bnch != null && specf != null) {
                criterias.clear();
                criterias.put("branch.id", bnch.getId());
                criterias.put("specifics.id", specf.getId());
                criterias.put("organization.id", bnch.getOrganization().getId());
                criterias.put("presentStatus", 1);
                BranchSpecifics bnchSpecf = genericDao.getByCriteria(BranchSpecifics.class, criterias, entityManager);
                if (bnchSpecf != null) {
                    if (bnchSpecf.getWalkinCustomerMaxDiscount() != null) {
                        configuredDiscountValue = bnchSpecf.getWalkinCustomerMaxDiscount();
                        if (enteredDiscountValue.compareTo(configuredDiscountValue) <= 0) {
                            result.put("result", true);
                        } else {
                            result.put("result", false);
                        }
                    }
                    if (bnchSpecf.getWalkinCustomerMaxDiscount() == null) {
                        result.put("result", false);
                    }
                }
                ObjectNode row = Json.newObject();
                row.put("discountPercentage", configuredDiscountValue);
                an.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return result;
    }

    @Override
    public ObjectNode branchCustomerVendorReceivablePayables(ObjectNode result,
            JsonNode json, Users user, EntityManager entityManager) {
        try {
            result.put("result", false);
            ArrayNode an = result.putArray("branchCustomerVendorReceivablePayablesData");
            // String
            // bnchName=json.findValue("bnchName")!=null?json.findValue("bnchName").asText():null;
            // Sunil

            String branchId = json.findValue("branchID") != null ? json.findValue("branchID").asText() : null;
            String txnModelFor = json.findValue("txnModelFor") != null ? json.findValue("txnModelFor").asText() : null;

            log.log(Level.FINE, "************* Start " + txnModelFor);

            Branch branch = null;
            /*
             * Sunil, two branch can be with the same name.
             * hence system was fetching different branch data as name matches.
             * if(bnchName!=null && !bnchName.equals("")){
             * branch=Branch.findByName(bnchName);
             * }
             */

            if (branchId != null && !branchId.equals("")) {
                branch = Branch.findById(IdosUtil.convertStringToLong(branchId));
            }

            if (txnModelFor != null && txnModelFor.equals("customerReceivables")) {
                customerReceivables(user, branch, result, an, entityManager);
            }
            if (txnModelFor != null && txnModelFor.equals("vendorPayables")) {
                vendorPayables(user, branch, result, an, entityManager);
            }
            if (txnModelFor != null && txnModelFor.equals("bankwiseBalances")) {
                bankwiseBalances(user, branch, result, an, entityManager);
            }
            /*
             * if(txnModelFor!=null &&
             * txnModelFor.equals("customerReceivablesOnDashboard")){
             * customerReceivablesOnDashboard(user, branch, result, an, entityManager,
             * entitytransaction);
             * }
             * if(txnModelFor!=null && txnModelFor.equals("vendorPayablesOnDashboard")){
             * vendorPayablesOnDashboard(user, branch, result, an, entityManager,
             * entitytransaction);
             * }
             */
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        log.log(Level.FINE, "************* End " + result);
        return result;
    }

    public void customerReceivables(Users user, Branch branch, ObjectNode result, ArrayNode an,
            EntityManager entityManager) {
        String[] arr = DateUtil.getFinancialDate(user);
        String finStartDate = arr[0];
        String finEndDate = arr[1];
        try {
            Date oneEightyDaysBackDate = DateUtil.returnOneEightyDaysBackDateDate();
            String oneEightyDaysBackDateStr = IdosConstants.mysqldf.format(oneEightyDaysBackDate);
            // Journal entry data customer/vendor wise payables/receivables
            Map provisionEntries = new HashMap();
            Map allSpecifcsAmtData = new HashMap();
            Map vendorPayablesDataUnder180days = new HashMap();
            Map custReceivablesDataUnder180days = new HashMap();
            ProvisionJournalEntryService jourObj = new ProvisionJournalEntryServiceImpl();
            Map branchMap = jourObj.getDashboardProvisionEntriesDataForBranch(oneEightyDaysBackDateStr, finEndDate,
                    user, allSpecifcsAmtData, vendorPayablesDataUnder180days, custReceivablesDataUnder180days, branch,
                    entityManager);
            Map vendorPayablesDataOver180days = new HashMap();
            Map custReceivablesDataOver180days = new HashMap();
            branchMap = jourObj.getDashboardProvisionEntriesDataForBranch(finStartDate, oneEightyDaysBackDateStr, user,
                    allSpecifcsAmtData, vendorPayablesDataOver180days, custReceivablesDataOver180days, branch,
                    entityManager);

            StringBuilder branchcreditincomesbquery = new StringBuilder(
                    "select obj from Transaction obj WHERE obj.transactionBranch='" + branch.getId()
                            + "' and obj.transactionBranchOrganization='" + branch.getOrganization().getId()
                            + "' AND obj.transactionPurpose in (2,38) and obj.transactionStatus='Accounted' and obj.transactionVendorCustomer IS NOT NULL and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate  between '"
                            + finStartDate + "' and '" + finEndDate
                            + "' and obj.presentStatus=1 GROUP BY obj.transactionVendorCustomer");
            List<Transaction> bnchcustcreditincometxn = genericDao
                    .executeSimpleQuery(branchcreditincomesbquery.toString(), entityManager);
            if (!bnchcustcreditincometxn.isEmpty() && bnchcustcreditincometxn.size() > 0) {
                result.put("result", true);
                for (Transaction maintxn : bnchcustcreditincometxn) {
                    Double amount = 0.0;
                    Double over180daysamount = 0.0;
                    Double under180daysamount = 0.0;
                    branchcreditincomesbquery.delete(0, branchcreditincomesbquery.length());
                    branchcreditincomesbquery.append("select obj from Transaction obj WHERE obj.transactionBranch='"
                            + branch.getId() + "' and obj.transactionBranchOrganization='"
                            + branch.getOrganization().getId() + "' AND obj.transactionVendorCustomer='"
                            + maintxn.getTransactionVendorCustomer().getId()
                            + "' AND obj.transactionPurpose in (2,38) and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate  between '"
                            + finStartDate + "' and '" + finEndDate + "'");
                    List<Transaction> bnchcustcreditincometxncust = genericDao
                            .executeSimpleQuery(branchcreditincomesbquery.toString(), entityManager);
                    for (Transaction txn : bnchcustcreditincometxncust) {
                        Double overunder180daysnetamountforthistran = 0.0;
                        if ((txn.getTransactionVendorCustomer().getPurchaseType() == 0
                                || txn.getTransactionVendorCustomer().getPurchaseType() == 2)
                                && txn.getTransactionVendorCustomer().getDaysForCredit() != null) {
                            if (txn.getTransactionVendorCustomer().getDaysForCredit() > 14) {
                                int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
                                        - txn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
                                if (daysdiff < 14 || daysdiff == 14) {
                                    if (txn.getCustomerNetPayment() != null) {
                                        if (txn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                                            amount -= txn.getNetAmount();
                                            overunder180daysnetamountforthistran = txn.getNetAmount();
                                        } else {
                                            overunder180daysnetamountforthistran = txn.getNetAmount()
                                                    - txn.getCustomerNetPayment();
                                            amount += txn.getNetAmount() - txn.getCustomerNetPayment();
                                        }
                                    } else if (txn.getNetAmount() != null) {
                                        if (txn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                                            amount -= txn.getNetAmount();
                                        } else {
                                            overunder180daysnetamountforthistran = txn.getNetAmount();
                                            amount += txn.getNetAmount();
                                        }
                                    }

                                }
                            } else {
                                overunder180daysnetamountforthistran = txn.getNetAmount();
                                amount += txn.getNetAmount();
                            }
                        }
                        if (txn.getTransactionVendorCustomer().getDaysForCredit() == null) {
                            if (txn.getCustomerNetPayment() != null) {
                                overunder180daysnetamountforthistran = txn.getNetAmount() - txn.getCustomerNetPayment();
                                amount += txn.getNetAmount() - txn.getCustomerNetPayment();
                            } else {
                                overunder180daysnetamountforthistran = txn.getNetAmount();
                                amount += txn.getNetAmount();
                            }
                        }
                        // Over 180days amount
                        if ((txn.getTransactionDate()).compareTo(oneEightyDaysBackDate) < 0) // it is over180days
                                                                                             // transaction
                        {
                            over180daysamount += overunder180daysnetamountforthistran;
                        } else if ((txn.getTransactionDate()).compareTo(oneEightyDaysBackDate) > 0)// txn.getTransactionDate()
                                                                                                   // is after
                                                                                                   // oneEightyDaysBackDate
                                                                                                   // -this transaction
                                                                                                   // is under 180days
                        {
                            under180daysamount += overunder180daysnetamountforthistran;
                        }
                        if (custReceivablesDataUnder180days
                                .containsKey(maintxn.getTransactionVendorCustomer().getId())) {
                            amount = amount + new Double(custReceivablesDataUnder180days
                                    .get(maintxn.getTransactionVendorCustomer().getId()).toString());
                            under180daysamount = under180daysamount + new Double(custReceivablesDataUnder180days
                                    .get(maintxn.getTransactionVendorCustomer().getId()).toString());
                        }
                        if (custReceivablesDataOver180days
                                .containsKey(maintxn.getTransactionVendorCustomer().getId())) {
                            amount = amount + new Double(custReceivablesDataOver180days
                                    .get(maintxn.getTransactionVendorCustomer().getId()).toString());
                            over180daysamount = over180daysamount + new Double(custReceivablesDataOver180days
                                    .get(maintxn.getTransactionVendorCustomer().getId()).toString());
                        }
                    }
                    if (amount > 0.0) {
                        ObjectNode row = Json.newObject();
                        row.put("id", maintxn.getTransactionVendorCustomer().getId());
                        row.put("branchName", branch.getName());
                        row.put("customerName", maintxn.getTransactionVendorCustomer().getName());
                        row.put("netAmount", IdosConstants.decimalFormat.format(amount));
                        row.put("txnModelFor", "customerReceivables");
                        row.put("over180daysamount", IdosConstants.decimalFormat.format(over180daysamount));
                        row.put("under180daysamount", IdosConstants.decimalFormat.format(under180daysamount));
                        row.put("branchID", branch.getId());
                        an.add(row);
                    }
                }
                addJournalEntryDataForCustVendNotPresentInTransList(custReceivablesDataUnder180days,
                        custReceivablesDataOver180days, bnchcustcreditincometxn, "customerReceivables", an,
                        entityManager);

            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            ex.printStackTrace();
        }
    }

    public void vendorPayables(Users user, Branch branch, ObjectNode result, ArrayNode an,
            EntityManager entityManager) {
        String[] arr = DateUtil.getFinancialDate(user);
        String finStartDate = arr[0];
        String finEndDate = arr[1];
        Date oneEightyDaysBackDate = DateUtil.returnOneEightyDaysBackDateDate();
        try {
            // get Journal entry data vendor wise
            String oneEightyDaysBackDateStr = IdosConstants.mysqldf.format(oneEightyDaysBackDate);
            Map provisionEntries = new HashMap();
            Map allSpecifcsAmtData = new HashMap();
            Map vendorPayablesDataUnder180days = new HashMap();
            Map custReceivablesDataUnder180days = new HashMap();
            ProvisionJournalEntryService jourObj = new ProvisionJournalEntryServiceImpl();
            Map branchMap = jourObj.getDashboardProvisionEntriesDataForBranch(oneEightyDaysBackDateStr, finEndDate,
                    user, allSpecifcsAmtData, vendorPayablesDataUnder180days, custReceivablesDataUnder180days, branch,
                    entityManager);
            Map vendorPayablesDataOver180days = new HashMap();
            Map custReceivablesDataOver180days = new HashMap();
            branchMap = jourObj.getDashboardProvisionEntriesDataForBranch(finStartDate, oneEightyDaysBackDateStr, user,
                    allSpecifcsAmtData, vendorPayablesDataOver180days, custReceivablesDataOver180days, branch,
                    entityManager);

            StringBuilder branchcreditincomesbquery = new StringBuilder("");
            branchcreditincomesbquery.append("select obj from Transaction obj WHERE obj.transactionBranch='"
                    + branch.getId() + "' and obj.transactionBranchOrganization='" + branch.getOrganization().getId()
                    + "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.transactionVendorCustomer IS NOT NULL and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate  between '"
                    + finStartDate + "' and '" + finEndDate + "' GROUP BY obj.transactionVendorCustomer");
            List<Transaction> bnchvendcreditexpensetxn = genericDao
                    .executeSimpleQuery(branchcreditincomesbquery.toString(), entityManager);
            if (!bnchvendcreditexpensetxn.isEmpty() && bnchvendcreditexpensetxn.size() > 0) {
                result.put("result", true);
                log.log(Level.INFO, "size of transaction=" + bnchvendcreditexpensetxn.size());

                for (Transaction maintxn : bnchvendcreditexpensetxn) {
                    Double amount = 0.0;
                    Double over180daysamount = 0.0;
                    Double under180daysamount = 0.0;
                    branchcreditincomesbquery.delete(0, branchcreditincomesbquery.length());
                    branchcreditincomesbquery.append("select obj from Transaction obj WHERE obj.transactionBranch='"
                            + branch.getId() + "' and obj.transactionBranchOrganization='"
                            + branch.getOrganization().getId() + "' AND obj.transactionVendorCustomer='"
                            + maintxn.getTransactionVendorCustomer().getId()
                            + "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate  between '"
                            + finStartDate + "' and '" + finEndDate + "'");
                    List<Transaction> bnchvendcreditexpensetxnvend = genericDao
                            .executeSimpleQuery(branchcreditincomesbquery.toString(), entityManager);
                    log.log(Level.INFO, "size of transaction for vendor=" + bnchvendcreditexpensetxnvend.size());
                    for (Transaction txn : bnchvendcreditexpensetxnvend) {
                        Double overunder180daysnetamountforthistran = 0.0;
                        if ((txn.getTransactionVendorCustomer().getPurchaseType() == 0
                                || txn.getTransactionVendorCustomer().getPurchaseType() == 2)
                                && txn.getTransactionVendorCustomer().getDaysForCredit() != null) {
                            if (txn.getTransactionVendorCustomer().getDaysForCredit() > 14) {
                                int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
                                        - txn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
                                if (daysdiff < 14 || daysdiff == 14) {
                                    if (txn.getCustomerNetPayment() != null) {
                                        overunder180daysnetamountforthistran = txn.getNetAmount()
                                                - txn.getCustomerNetPayment();
                                        amount += txn.getNetAmount() - txn.getCustomerNetPayment();
                                    } else {
                                        overunder180daysnetamountforthistran = txn.getNetAmount();
                                        amount += txn.getNetAmount();
                                    }
                                }
                            } else {
                                overunder180daysnetamountforthistran = txn.getNetAmount();
                                amount += txn.getNetAmount();
                            }
                        }
                        if (txn.getTransactionVendorCustomer().getDaysForCredit() == null) {
                            Double vendorDuePayment = 0.0;
                            if (txn.getVendorNetPayment() != null) {
                                vendorDuePayment = txn.getVendorNetPayment();
                            }
                            overunder180daysnetamountforthistran = txn.getNetAmount() - vendorDuePayment;
                            amount += txn.getNetAmount() - vendorDuePayment;
                        }

                        // Over 180days amount
                        if ((txn.getTransactionDate()).compareTo(oneEightyDaysBackDate) < 0) // it is over180days
                                                                                             // transaction
                        {
                            over180daysamount += overunder180daysnetamountforthistran;
                        } else if ((txn.getTransactionDate()).compareTo(oneEightyDaysBackDate) > 0)// txn.getTransactionDate()
                                                                                                   // is after
                                                                                                   // oneEightyDaysBackDate
                                                                                                   // -this transaction
                                                                                                   // is under 180days
                        {
                            under180daysamount += overunder180daysnetamountforthistran;
                        }
                        // Journal entry receivables: when cust select on lef side increase
                        if (vendorPayablesDataUnder180days
                                .containsKey(maintxn.getTransactionVendorCustomer().getId())) {
                            amount = amount + new Double(vendorPayablesDataUnder180days
                                    .get(maintxn.getTransactionVendorCustomer().getId()).toString());
                            under180daysamount = under180daysamount + new Double(vendorPayablesDataUnder180days
                                    .get(maintxn.getTransactionVendorCustomer().getId()).toString());
                        }
                        if (vendorPayablesDataOver180days.containsKey(maintxn.getTransactionVendorCustomer().getId())) {
                            amount = amount + new Double(vendorPayablesDataOver180days
                                    .get(maintxn.getTransactionVendorCustomer().getId()).toString());
                            over180daysamount = over180daysamount + new Double(vendorPayablesDataOver180days
                                    .get(maintxn.getTransactionVendorCustomer().getId()).toString());
                        }
                    }
                    if (amount > 0.0) {
                        ObjectNode row = Json.newObject();
                        row.put("id", maintxn.getTransactionVendorCustomer().getId());
                        row.put("branchName", branch.getName());
                        row.put("customerName", maintxn.getTransactionVendorCustomer().getName());
                        row.put("netAmount", IdosConstants.decimalFormat.format(amount));
                        row.put("txnModelFor", "vendorPayables");
                        row.put("over180daysamount", IdosConstants.decimalFormat.format(over180daysamount));
                        row.put("under180daysamount", IdosConstants.decimalFormat.format(under180daysamount));
                        row.put("branchID", branch.getId());
                        an.add(row);
                    }
                }
                addJournalEntryDataForCustVendNotPresentInTransList(vendorPayablesDataUnder180days,
                        vendorPayablesDataOver180days, bnchvendcreditexpensetxn, "vendorPayables", an, entityManager);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            ex.printStackTrace();
        }
    }

    public void addJournalEntryDataForCustVendNotPresentInTransList(Map custReceivablesDataUnder180days,
            Map custReceivablesDataOver180days, List<Transaction> custVendTxnList, String modelName, ArrayNode an,
            EntityManager entityManager) {
        Set<String> keySet = custReceivablesDataUnder180days.keySet();
        Iterator<String> keySetIterator = keySet.iterator();
        Map<String, Object> criterias = new HashMap<String, Object>();
        while (keySetIterator.hasNext()) {
            String key = keySetIterator.next();
            boolean found = false;
            for (Transaction maintxn : custVendTxnList) {
                if (key.equals(maintxn.getTransactionVendorCustomer().getId().toString())) {
                    found = true;
                }
            }
            if (!found) {
                criterias.clear();
                criterias.put("id", new Long(key));
                criterias.put("presentStatus", 1);
                List<Vendor> custList = genericDao.findByCriteria(Vendor.class, criterias, entityManager);
                if (!custList.isEmpty()) {
                    Vendor cust = custList.get(0);
                    double amount = 0.0, under180daysamount = 0.0, over180daysamount = 0.0;
                    if (custReceivablesDataUnder180days.containsKey(key)) {
                        amount = amount + new Double(custReceivablesDataUnder180days.get(key).toString());
                        under180daysamount = new Double(custReceivablesDataUnder180days.get(key).toString());
                    }
                    if (custReceivablesDataOver180days.containsKey(key)) {
                        amount = amount + new Double(custReceivablesDataOver180days.get(key).toString());
                        over180daysamount = new Double(custReceivablesDataOver180days.get(key).toString());
                        ;
                    }
                    ObjectNode row = Json.newObject();
                    row.put("id", cust.getId());
                    row.put("branchName", cust.getBranch().getName());
                    row.put("customerName", cust.getName());
                    row.put("netAmount", amount);
                    row.put("txnModelFor", modelName);
                    row.put("over180daysamount", over180daysamount);
                    row.put("under180daysamount", under180daysamount);
                    row.put("branchID", cust.getBranch().getId());
                    an.add(row);
                }
            }
        }
    }

    /*
     * Name Balance 0-30 Days 31-60 Days 61-90 Days 91-180 Days Over 180 Days
     * Customer A 2000 600 300 400 500 200
     */
    /*
     * public void customerReceivablesOnDashboard(Users user, Branch branch,
     * ObjectNode result, ArrayNode an, EntityManager entityManager,
     * EntityTransaction entitytransaction){
     * String[] arr=DateUtil.getFinancialDate(user);
     * String finStartDate=arr[0];
     * String finEndDate=arr[1];
     * StringBuilder branchcreditincomesbquery = new StringBuilder("");
     * try{
     * Date oneEightyDaysBackDate=DateUtil.returnOneEightyDaysBackDateDate();
     * Date
     * currDateTime=mysqldf.parse(mysqldf.format(Calendar.getInstance().getTime()));
     * Date under0to30daysDate = DateUtil.returnPrevOneMonthDateDate(currDateTime);
     * Date under31to60daysDate =
     * DateUtil.returnPrevOneMonthDateDate(under0to30daysDate);
     * Date under61to90daysDate =
     * DateUtil.returnPrevOneMonthDateDate(under31to60daysDate);
     * Date under91To180daysDate =
     * DateUtil.returnPrevThreeMonthDateDate(under61to90daysDate);
     * branchcreditincomesbquery.
     * append("select obj from Transaction obj WHERE obj.transactionBranch='"+branch
     * .getId()+"' and obj.transactionBranchOrganization='"+branch.getOrganization()
     * .getId()
     * +"' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.transactionVendorCustomer IS NOT NULL and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate  between '"
     * +finStartDate+"' and '"+finEndDate+"' GROUP BY obj.transactionVendorCustomer"
     * );
     * List<Transaction> bnchcustcreditincometxn=genericDao.executeSimpleQuery(
     * branchcreditincomesbquery.toString(),entityManager);
     * if(!bnchcustcreditincometxn.isEmpty() && bnchcustcreditincometxn.size()>0){
     * result.put("result", true);
     * for(Transaction maintxn:bnchcustcreditincometxn){
     * Double amount=0.0;
     * Double over180daysamount =0.0;
     * Double under180daysamount =0.0,under91to180daysamount
     * =0.0,under61to90daysamount =0.0,under31to60daysamount
     * =0.0,under0to30daysamount =0.0;
     * Double
     * overdueamount=0.0,over180daysoverdueamount=0.0,under180daysoverdueamount=0.0,
     * under91to180daysoverdueamount=0.0,under61to90daysoverdueamount=0.0,
     * under31to60daysoverdueamount=0.0,under0to30daysoverdueamount=0.0;
     * branchcreditincomesbquery.delete(0, branchcreditincomesbquery.length());
     * branchcreditincomesbquery.
     * append("select obj from Transaction obj WHERE obj.transactionBranch='"+branch
     * .getId()+"' and obj.transactionBranchOrganization='"+branch.getOrganization()
     * .getId()+"' AND obj.transactionVendorCustomer='"+maintxn.
     * getTransactionVendorCustomer().getId()
     * +"' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate  between '"
     * +finStartDate+"' and '"+finEndDate+"'");
     * List<Transaction> bnchcustcreditincometxncust=genericDao.executeSimpleQuery(
     * branchcreditincomesbquery.toString(),entityManager);
     * for(Transaction txn:bnchcustcreditincometxncust){
     * Double overunder180daysOverdueamountforthistran=0.0,
     * overunder180daysnetamountforthistran = 0.0;
     * if(txn.getCustomerNetPayment()!=null){
     * overunder180daysnetamountforthistran =
     * txn.getNetAmount()-txn.getCustomerNetPayment();
     * amount+=txn.getNetAmount()-txn.getCustomerNetPayment();
     * }else{
     * overunder180daysnetamountforthistran = txn.getNetAmount();
     * amount+=txn.getNetAmount();
     * }
     * if((txn.getTransactionVendorCustomer().getPurchaseType()==0 ||
     * txn.getTransactionVendorCustomer().getPurchaseType()==2) &&
     * txn.getTransactionVendorCustomer().getDaysForCredit()!=null){
     * int daysdiff=(int)(Calendar.getInstance().getTimeInMillis()-txn.
     * getTransactionDate().getTime()/1000*60*60*24);
     * if(daysdiff>txn.getTransactionVendorCustomer().getDaysForCredit()){
     * if(txn.getCustomerNetPayment()!=null){
     * overunder180daysOverdueamountforthistran =
     * txn.getNetAmount()-txn.getCustomerNetPayment();
     * overdueamount+=txn.getNetAmount()-txn.getCustomerNetPayment();
     * }else{
     * overunder180daysOverdueamountforthistran = txn.getNetAmount();
     * overdueamount+=txn.getNetAmount();
     * }
     * }
     * 
     * }
     * //Over 180days amount
     * //If today 1 Oct 2016, then over 180days means sell on credit transaction
     * which are created in tranDate<April 2016
     * if((txn.getTransactionDate()).compareTo(oneEightyDaysBackDate) <0) // it is
     * over180days transaction
     * {
     * over180daysamount+=overunder180daysnetamountforthistran;
     * over180daysoverdueamount+=overunder180daysOverdueamountforthistran;
     * }
     * else if((txn.getTransactionDate()).compareTo(oneEightyDaysBackDate)
     * >0)//txn.getTransactionDate() is after oneEightyDaysBackDate -this
     * transaction is under 180days
     * {
     * under180daysamount+=overunder180daysnetamountforthistran;
     * under180daysoverdueamount+=overunder180daysOverdueamountforthistran;
     * if(txn.getTransactionDate().before(currDateTime) &&
     * txn.getTransactionDate().after(under0to30daysDate)){//0 to 30days means in
     * Sept 2016 to Oct2016
     * under0to30daysamount += overunder180daysnetamountforthistran;
     * under0to30daysoverdueamount += overunder180daysOverdueamountforthistran;
     * }else if(txn.getTransactionDate().before(under0to30daysDate) &&
     * txn.getTransactionDate().after(under31to60daysDate)){//31 to 60days
     * under31to60daysamount += overunder180daysnetamountforthistran;
     * under31to60daysoverdueamount += overunder180daysOverdueamountforthistran;
     * }else if(txn.getTransactionDate().before(under31to60daysDate) &&
     * txn.getTransactionDate().after(under61to90daysDate)){//61 to 90days
     * under61to90daysamount += overunder180daysnetamountforthistran;
     * under61to90daysoverdueamount += overunder180daysOverdueamountforthistran;
     * }else if(txn.getTransactionDate().before(under61to90daysDate) &&
     * txn.getTransactionDate().after(under91To180daysDate)){//91 to 180days
     * under91to180daysamount += overunder180daysnetamountforthistran;
     * under91to180daysoverdueamount += overunder180daysOverdueamountforthistran;
     * }
     * }
     * }
     * //if(amount>0.0){
     * if(maintxn.getTransactionVendorCustomer().getOpeningBalance()!=null){
     * amount=amount + maintxn.getTransactionVendorCustomer().getOpeningBalance();
     * overdueamount = overdueamount +
     * maintxn.getTransactionVendorCustomer().getOpeningBalance();
     * }
     * ObjectNode row = Json.newObject();
     * row.put("id", maintxn.getTransactionVendorCustomer().getId());
     * row.put("branchName", branch.getName());
     * row.put("customerName", maintxn.getTransactionVendorCustomer().getName());
     * row.put("openingBalance",
     * maintxn.getTransactionVendorCustomer().getOpeningBalance());
     * row.put("netAmount", amount);
     * row.put("txnModelFor", "customerReceivables");
     * row.put("over180daysamount",over180daysamount);
     * row.put("under180daysamount",under180daysamount);
     * row.put("under91to180daysamount",under91to180daysamount);
     * row.put("under61to90daysamount",under61to90daysamount);
     * row.put("under31to60daysamount",under31to60daysamount);
     * row.put("under0to30daysamount",under0to30daysamount);
     * row.put("overdueAmount", overdueamount);
     * row.put("over180daysoverdueamount",over180daysoverdueamount);
     * row.put("under180daysoverdueamount",under180daysoverdueamount);
     * row.put("under91to180daysoverdueamount",under91to180daysoverdueamount);
     * row.put("under61to90daysoverdueamount",under61to90daysoverdueamount);
     * row.put("under31to60daysoverdueamount",under31to60daysoverdueamount);
     * row.put("under0to30daysoverdueamount",under0to30daysoverdueamount);
     * row.put("branchID",branch.getId());
     * an.add(row);
     * //}
     * }
     * }
     * }catch(Exception ex){
     * log.log(Level.SEVERE, "Error", ex);
     * }
     * }
     */

    /*
     * public void vendorPayablesOnDashboard(Users user,Branch branch,ObjectNode
     * result,ArrayNode an,EntityManager entityManager,EntityTransaction
     * entitytransaction){
     * String[] arr=DateUtil.getFinancialDate(user);
     * String finStartDate=arr[0];
     * String finEndDate=arr[1];
     * try{
     * Date oneEightyDaysBackDate=DateUtil.returnOneEightyDaysBackDateDate();
     * Date
     * currDateTime=mysqldf.parse(mysqldf.format(Calendar.getInstance().getTime()));
     * Date under0to30daysDate = DateUtil.returnPrevOneMonthDateDate(currDateTime);
     * Date under31to60daysDate =
     * DateUtil.returnPrevOneMonthDateDate(under0to30daysDate);
     * Date under61to90daysDate =
     * DateUtil.returnPrevOneMonthDateDate(under31to60daysDate);
     * Date under91To180daysDate =
     * DateUtil.returnPrevThreeMonthDateDate(under61to90daysDate);
     * StringBuilder branchcreditincomesbquery = new StringBuilder("");
     * branchcreditincomesbquery.
     * append("select obj from Transaction obj WHERE obj.transactionBranch='"+branch
     * .getId()+"' and obj.transactionBranchOrganization='"+branch.getOrganization()
     * .getId()
     * +"' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.transactionVendorCustomer IS NOT NULL and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate  between '"
     * +finStartDate+"' and '"+finEndDate+"' GROUP BY obj.transactionVendorCustomer"
     * );
     * List<Transaction> bnchvendcreditexpensetxn=genericDao.executeSimpleQuery(
     * branchcreditincomesbquery.toString(),entityManager);
     * if(!bnchvendcreditexpensetxn.isEmpty() && bnchvendcreditexpensetxn.size()>0){
     * result.put("result", true);
     * for(Transaction maintxn:bnchvendcreditexpensetxn){
     * Double amount=0.0;
     * Double over180daysamount =0.0;
     * Double under180daysamount =0.0;Double under91to180daysamount =0.0;Double
     * under61to90daysamount =0.0;Double under31to60daysamount =0.0;Double
     * under0to30daysamount =0.0;
     * Double
     * overdueamount=0.0,over180daysoverdueamount=0.0,under180daysoverdueamount=0.0,
     * under91to180daysoverdueamount=0.0,under61to90daysoverdueamount=0.0,
     * under31to60daysoverdueamount=0.0,under0to30daysoverdueamount=0.0;
     * branchcreditincomesbquery.delete(0, branchcreditincomesbquery.length());
     * branchcreditincomesbquery.
     * append("select obj from Transaction obj WHERE obj.transactionBranch='"+branch
     * .getId()+"' and obj.transactionBranchOrganization='"+branch.getOrganization()
     * .getId()+"' AND obj.transactionVendorCustomer='"+maintxn.
     * getTransactionVendorCustomer().getId()
     * +"' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate  between '"
     * +finStartDate+"' and '"+finEndDate+"'");
     * List<Transaction> bnchvendcreditexpensetxnvend=genericDao.executeSimpleQuery(
     * branchcreditincomesbquery.toString(),entityManager);
     * for(Transaction txn:bnchvendcreditexpensetxnvend){
     * Double overunder180daysOverdueamountforthistran=0.0,
     * overunder180daysnetamountforthistran = 0.0;
     * if(txn.getCustomerNetPayment()!=null){
     * overunder180daysnetamountforthistran =
     * txn.getNetAmount()-txn.getCustomerNetPayment();
     * amount+=txn.getNetAmount()-txn.getCustomerNetPayment();
     * }else{
     * overunder180daysnetamountforthistran = txn.getNetAmount();
     * amount+=txn.getNetAmount();
     * }
     * if((txn.getTransactionVendorCustomer().getPurchaseType()==0 ||
     * txn.getTransactionVendorCustomer().getPurchaseType()==2) &&
     * txn.getTransactionVendorCustomer().getDaysForCredit()!=null){
     * int daysdiff=(int)(Calendar.getInstance().getTimeInMillis()-txn.
     * getTransactionDate().getTime()/1000*60*60*24);
     * if(daysdiff>txn.getTransactionVendorCustomer().getDaysForCredit()){
     * if(txn.getCustomerNetPayment()!=null){
     * overunder180daysOverdueamountforthistran =
     * txn.getNetAmount()-txn.getCustomerNetPayment();
     * overdueamount+=txn.getNetAmount()-txn.getCustomerNetPayment();
     * }else{
     * overunder180daysOverdueamountforthistran = txn.getNetAmount();
     * overdueamount+=txn.getNetAmount();
     * }
     * }
     * }
     * //Over 180days amount
     * if((txn.getTransactionDate()).compareTo(oneEightyDaysBackDate) <0) // it is
     * over180days transaction
     * {
     * over180daysamount+=overunder180daysnetamountforthistran;
     * over180daysoverdueamount+=overunder180daysOverdueamountforthistran;
     * }
     * else if((txn.getTransactionDate()).compareTo(oneEightyDaysBackDate)
     * >0)//txn.getTransactionDate() is after oneEightyDaysBackDate -this
     * transaction is under 180days
     * {
     * under180daysamount+=overunder180daysnetamountforthistran;
     * under180daysoverdueamount+=overunder180daysOverdueamountforthistran;
     * if( txn.getTransactionDate().after(under0to30daysDate)){//0 to 30days means
     * in Sept 2016 to Oct2016 txn.getTransactionDate().before(currDateTime) &&
     * under0to30daysamount += overunder180daysnetamountforthistran;
     * under0to30daysoverdueamount += overunder180daysOverdueamountforthistran;
     * }else if(txn.getTransactionDate().before(under0to30daysDate) &&
     * txn.getTransactionDate().after(under31to60daysDate)){//31 to 60days
     * under31to60daysamount += overunder180daysnetamountforthistran;
     * under31to60daysoverdueamount += overunder180daysOverdueamountforthistran;
     * }else if(txn.getTransactionDate().before(under31to60daysDate) &&
     * txn.getTransactionDate().after(under61to90daysDate)){//61 to 90days
     * under61to90daysamount += overunder180daysnetamountforthistran;
     * under61to90daysoverdueamount += overunder180daysOverdueamountforthistran;
     * }else if(txn.getTransactionDate().before(under61to90daysDate) &&
     * txn.getTransactionDate().after(under91To180daysDate)){//91 to 180days
     * under91to180daysamount += overunder180daysnetamountforthistran;
     * under91to180daysoverdueamount += overunder180daysOverdueamountforthistran;
     * }
     * }
     * }
     * //if(amount>0.0){
     * if(maintxn.getTransactionVendorCustomer().getOpeningBalance()!=null){
     * amount=amount + maintxn.getTransactionVendorCustomer().getOpeningBalance();
     * overdueamount = overdueamount +
     * maintxn.getTransactionVendorCustomer().getOpeningBalance();
     * }
     * ObjectNode row = Json.newObject();
     * row.put("id", maintxn.getTransactionVendorCustomer().getId());
     * row.put("branchName", branch.getName());
     * row.put("customerName", maintxn.getTransactionVendorCustomer().getName());
     * row.put("openingBalance",
     * maintxn.getTransactionVendorCustomer().getOpeningBalance());
     * row.put("netAmount", amount);
     * row.put("txnModelFor", "vendorPayables");
     * row.put("over180daysamount",over180daysamount);
     * row.put("under180daysamount",under180daysamount);
     * row.put("under91to180daysamount",under91to180daysamount);
     * row.put("under61to90daysamount",under61to90daysamount);
     * row.put("under31to60daysamount",under31to60daysamount);
     * row.put("under0to30daysamount",under0to30daysamount);
     * row.put("overdueAmount", overdueamount);
     * row.put("over180daysoverdueamount",over180daysoverdueamount);
     * row.put("under180daysoverdueamount",under180daysoverdueamount);
     * row.put("under91to180daysoverdueamount",under91to180daysoverdueamount);
     * row.put("under61to90daysoverdueamount",under61to90daysoverdueamount);
     * row.put("under31to60daysoverdueamount",under31to60daysoverdueamount);
     * row.put("under0to30daysoverdueamount",under0to30daysoverdueamount);
     * row.put("branchID",branch.getId());
     * an.add(row);
     * //}
     * 
     * }
     * }
     * }catch(Exception ex){
     * log.log(Level.SEVERE, "Error", ex);
     * }
     * }
     */
    // From Transactions->Bank tab when we click on branch href, it will give bank,
    // balance for all banks in that branch
    // E.g. in Mumbai branch - Balance is 10000. Now when we click on Mumbai, it
    // will give SBI-6000, ICICI-5000 etc
    public void bankwiseBalances(Users user, Branch branch, ObjectNode result, ArrayNode an,
            EntityManager entityManager) {
        List<BranchBankAccounts> bnchBankAccounts = branch.getBranchBankAccounts();
        if (!bnchBankAccounts.isEmpty() && bnchBankAccounts.size() > 0) {
            result.put("result", true);
            for (BranchBankAccounts bnchBnkAccounts : bnchBankAccounts) {
                Double bankBalance = 0.0;
                ObjectNode row = Json.newObject();
                StringBuilder newbnchbankactsbquery = new StringBuilder("");
                newbnchbankactsbquery.append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='"
                        + branch.getId() + "' AND obj.organization.id='" + branch.getOrganization().getId()
                        + "' and obj.branchBankAccounts.id='" + bnchBnkAccounts.getId()
                        + "' and obj.presentStatus=1 ORDER BY obj.date desc");
                List<BranchBankAccountBalance> branchBankBalance = genericDao
                        .executeSimpleQueryWithLimit(newbnchbankactsbquery.toString(), entityManager, 1);
                if (branchBankBalance.size() > 0) {
                    if (branchBankBalance.get(0).getResultantCash() != null)
                        bankBalance += branchBankBalance.get(0).getResultantCash();
                }
                row.put("branchName", branch.getName());
                row.put("bankName", bnchBnkAccounts.getBankName());
                row.put("bankBalance", IdosConstants.decimalFormat.format(bankBalance));
                row.put("txnModelFor", "bankwiseBalances");
                an.add(row);
            }
        }
    }

    @Override
    public ObjectNode overUnderOneEightyReceivablePayablesTxn(ObjectNode result, JsonNode json, Users user,
            EntityManager entityManager, EntityTransaction entitytransaction) {
        result.put("result", false);
        try {
            log.log(Level.FINE, "************* Start " + json);
            ArrayNode an = result.putArray("overUnderOneEightyReceivablePayablesTxnData");
            String custVendId = json.findValue("custVendId") != null ? json.findValue("custVendId").asText() : null;
            // String
            // bnchName=json.findValue("branchName")!=null?json.findValue("branchName").asText():null;

            String branchID = json.findValue("branchID") != null ? json.findValue("branchID").asText() : null;
            String txnModelFor = json.findValue("txnModelFor") != null ? json.findValue("txnModelFor").asText() : null;
            String clickedButtonFor = json.findValue("clickedButtonFor") != null
                    ? json.findValue("clickedButtonFor").asText()
                    : null;
            Branch branch = null;
            Vendor vendor = null;
            if (custVendId != null && !custVendId.equals("")) {
                vendor = Vendor.findById(IdosUtil.convertStringToLong(custVendId));
            }
            /*
             * Sunil
             * if(bnchName!=null && !bnchName.equals("")){
             * branch=Branch.findByName(bnchName);
             * }
             */

            if (branchID != null && !branchID.equals("")) {
                branch = Branch.findById(IdosUtil.convertStringToLong(branchID));
            }

            if (txnModelFor != null && txnModelFor.equals("customerReceivables")) {
                customerReceivablesTxnWise(user, branch, vendor, clickedButtonFor, result, an, entityManager,
                        entitytransaction);
            }
            if (txnModelFor != null && txnModelFor.equals("vendorPayables")) {
                vendorPayablesTxnWise(user, branch, vendor, clickedButtonFor, result, an, entityManager,
                        entitytransaction);
            }
            if (txnModelFor != null && txnModelFor.equals("receivableOverduesAllBranches")
                    || txnModelFor.equals("payableOverduesAllBranches")) {
                payablesRecOverduesTxnWise(user, txnModelFor, branch, vendor, clickedButtonFor, result, an,
                        entityManager, entitytransaction);
            }
            if (txnModelFor != null && txnModelFor.equals("proformaInvoice")) {
                proformaInvoiceTxnWise(user, branch, vendor, clickedButtonFor, result, an, entityManager,
                        entitytransaction);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        log.log(Level.FINE, "************* End " + result);
        return result;
    }

    public void proformaInvoiceTxnWise(Users user, Branch branch, Vendor vendor, String clickedButtonFor,
            ObjectNode result, ArrayNode an, EntityManager entityManager, EntityTransaction entitytransaction) {
        StringBuilder sbr = new StringBuilder("");
        String[] arr = DateUtil.getFinancialDate(user);
        String finStartDate = arr[0];
        String finEndDate = arr[1];
        sbr.append("select obj from Transaction obj WHERE obj.transactionBranch='" + branch.getId()
                + "' and obj.transactionBranchOrganization='" + branch.getOrganization().getId()
                + "' AND obj.transactionVendorCustomer='" + vendor.getId()
                + "' AND obj.transactionPurpose=2 and obj.performaInvoice=true and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate  between '"
                + finStartDate + "' and '" + finEndDate + "'");
        List<Transaction> txnList = genericDao.executeSimpleQuery(sbr.toString(), entityManager);
        if (!txnList.isEmpty() && txnList.size() > 0) {
            result.put("result", true);
            Double totalamount = 0.0;
            for (Transaction txn : txnList) {
                ObjectNode row = Json.newObject();
                row.put("branchName", branch.getName());
                row.put("txnRefNumber", txn.getTransactionRefNumber());
                row.put("vendorName", txn.getTransactionVendorCustomer().getName());
                Double netAmount = 0.0;
                if (txn.getCustomerNetPayment() != null) {
                    netAmount = txn.getNetAmount() - txn.getCustomerNetPayment();
                    totalamount += netAmount;
                }
                if (txn.getCustomerNetPayment() == null) {
                    netAmount = txn.getNetAmount();
                    totalamount += netAmount;
                }
                row.put("netAmount", netAmount);
                row.put("createdBy", txn.getCreatedBy().getEmail());
                row.put("txnModelFor", "customerReceivables");
                row.put("clickedButtonFor", "over");
                row.put("totalamount", totalamount);
                an.add(row);
            }
        }

    }

    public void payablesRecOverduesTxnWise(Users user, String txnModelFor, Branch branch, Vendor vendor, String period,
            ObjectNode result, ArrayNode an, EntityManager entityManager, EntityTransaction entitytransaction) {
        try {
            int tranPurpose = 2;
            if (txnModelFor.equals("receivableOverduesAllBranches")) {
                tranPurpose = 2;
            } else {
                tranPurpose = 4; // paybles
            }
            String[] arr = DateUtil.getFinancialDate(user);
            String periodStartDate = arr[0];
            String periodEndDate = arr[1];
            String oneEightyDaysBackDate = DateUtil.returnOneEightyDaysBackDate();
            Date currDateTime = IdosConstants.mysqldf
                    .parse(IdosConstants.mysqldf.format(Calendar.getInstance().getTime()));
            String currDate = IdosConstants.mysqldf.format(currDateTime);
            Date under0to30daysDatedate = DateUtil.returnPrevOneMonthDateDate(currDateTime);
            String under0to30daysDate = IdosConstants.mysqldf.format(under0to30daysDatedate);
            Date under31to60daysDatedate = DateUtil.returnPrevOneMonthDateDate(under0to30daysDatedate);
            String under31to60daysDate = IdosConstants.mysqldf.format(under31to60daysDatedate);
            Date under61to90daysDatedate = DateUtil.returnPrevOneMonthDateDate(under31to60daysDatedate);
            String under61to90daysDate = IdosConstants.mysqldf.format(under61to90daysDatedate);
            Date under91To180daysDatedate = DateUtil.returnPrevThreeMonthDateDate(under61to90daysDatedate);
            String under91To180daysDate = IdosConstants.mysqldf.format(under91To180daysDatedate);
            if (period.equalsIgnoreCase("under0to30")) {
                periodStartDate = under0to30daysDate;
                periodEndDate = currDate;
            } else if (period.equalsIgnoreCase("under31to60")) {
                periodStartDate = under31to60daysDate;
                periodEndDate = under0to30daysDate;
            } else if (period.equalsIgnoreCase("under61to90")) {
                periodStartDate = under61to90daysDate;
                periodEndDate = under31to60daysDate;
            } else if (period.equalsIgnoreCase("under91to180")) {
                periodStartDate = under91To180daysDate;
                periodEndDate = under61to90daysDate;
            } else if (period.equalsIgnoreCase("over")) {
                periodStartDate = periodStartDate;
                periodEndDate = oneEightyDaysBackDate;
            }
            StringBuilder query = new StringBuilder("");
            query.append("select obj from Transaction obj WHERE obj.transactionBranch='" + branch.getId()
                    + "' and obj.transactionBranchOrganization='" + branch.getOrganization().getId()
                    + "' AND obj.transactionVendorCustomer='" + vendor.getId()
                    + "'and (obj.transactionVendorCustomer IS NOT NULL and (obj.transactionVendorCustomer.purchaseType=0 OR obj.transactionVendorCustomer.purchaseType=2) and obj.presentStatus=1 AND obj.transactionVendorCustomer.daysForCredit IS NOT NULL)  AND obj.transactionPurpose='"
                    + tranPurpose
                    + "' and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate  between '"
                    + periodStartDate + "' and '" + periodEndDate + "'");
            List<Transaction> bnchcustcreditincometxncust = genericDao.executeSimpleQuery(query.toString(),
                    entityManager);
            result.put("result", true);
            for (Transaction txn : bnchcustcreditincometxncust) {
                int daysdiff = (int) (Calendar.getInstance().getTimeInMillis()
                        - txn.getTransactionDate().getTime() / 1000 * 60 * 60 * 24);
                if (daysdiff > txn.getTransactionVendorCustomer().getDaysForCredit()) {
                    double overdueamount = 0.0;
                    if (tranPurpose == 4) {
                        if (txn.getVendorNetPayment() != null) {
                            overdueamount += txn.getNetAmount() - txn.getVendorNetPayment();
                        } else {
                            overdueamount += txn.getNetAmount();
                        }
                    } else {
                        if (txn.getCustomerNetPayment() != null) {
                            overdueamount += txn.getNetAmount() - txn.getCustomerNetPayment();
                        } else {
                            overdueamount += txn.getNetAmount();
                        }
                    }
                    ObjectNode row = Json.newObject();
                    row.put("branchName", branch.getName());
                    row.put("txnRefNumber", txn.getTransactionRefNumber());
                    row.put("vendorName", txn.getTransactionVendorCustomer().getName());
                    row.put("netAmount", IdosConstants.decimalFormat.format(overdueamount));
                    row.put("createdBy", txn.getCreatedBy().getEmail());
                    row.put("txnModelFor", "customerReceivables");
                    row.put("clickedButtonFor", period);
                    row.put("totalamount", IdosConstants.decimalFormat.format(overdueamount));
                    an.add(row);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            ex.printStackTrace();
        }
    }

    public void customerReceivablesTxnWise(Users user, Branch branch, Vendor vendor, String clickedButtonFor,
            ObjectNode result, ArrayNode an, EntityManager entityManager, EntityTransaction entitytransaction) {
        try {
            log.log(Level.FINE, "********Start");
            StringBuilder sbr = new StringBuilder("");
            String[] arr = DateUtil.getFinancialDate(user);
            String finStartDate = arr[0];
            String finEndDate = arr[1];
            String oneEightyDaysBackDate = DateUtil.returnOneEightyDaysBackDate();
            Date currDateTime = IdosConstants.mysqldf
                    .parse(IdosConstants.mysqldf.format(Calendar.getInstance().getTime()));
            String currDate = IdosConstants.mysqldf.format(currDateTime);
            Date under0to30daysDatedate = DateUtil.returnPrevOneMonthDateDate(currDateTime);
            String under0to30daysDate = IdosConstants.mysqldf.format(under0to30daysDatedate);
            Date under31to60daysDatedate = DateUtil.returnPrevOneMonthDateDate(under0to30daysDatedate);
            String under31to60daysDate = IdosConstants.mysqldf.format(under31to60daysDatedate);
            Date under61to90daysDatedate = DateUtil.returnPrevOneMonthDateDate(under31to60daysDatedate);
            String under61to90daysDate = IdosConstants.mysqldf.format(under61to90daysDatedate);
            Date under91To180daysDatedate = DateUtil.returnPrevThreeMonthDateDate(under61to90daysDatedate);
            String under91To180daysDate = IdosConstants.mysqldf.format(under91To180daysDatedate);
            if (clickedButtonFor.equals("over")) {
                sbr.append("select obj from Transaction obj WHERE obj.transactionBranch='" + branch.getId()
                        + "' and obj.transactionBranchOrganization='" + branch.getOrganization().getId()
                        + "' AND obj.transactionVendorCustomer='" + vendor.getId()
                        + "' AND obj.transactionPurpose in (2,38) and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate<'"
                        + oneEightyDaysBackDate + "' and obj.transactionDate  between '" + finStartDate + "' and '"
                        + finEndDate + "'");
                List<Transaction> txnList = genericDao.executeSimpleQuery(sbr.toString(), entityManager);
                if (!txnList.isEmpty() && txnList.size() > 0) {
                    result.put("result", true);
                    Double totalamount = 0.0;
                    for (Transaction txn : txnList) {
                        ObjectNode row = Json.newObject();
                        row.put("branchName", branch.getName());
                        row.put("txnRefNumber", txn.getTransactionRefNumber());
                        row.put("vendorName", txn.getTransactionVendorCustomer().getName());
                        Double netAmount = 0.0;
                        /*
                         * if(txn.getCustomerNetPayment()!=null){
                         * netAmount=txn.getNetAmount()-txn.getCustomerNetPayment();
                         * totalamount+=netAmount;
                         * }
                         * if(txn.getCustomerNetPayment()==null){
                         * netAmount=txn.getNetAmount();
                         * totalamount+=netAmount;
                         * }
                         */
                        if (txn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                            row.put("netAmount", IdosConstants.decimalFormat.format(txn.getNetAmount() * -1));
                        } else {
                            row.put("netAmount", IdosConstants.decimalFormat.format(txn.getNetAmount()));
                        }
                        row.put("createdBy", txn.getCreatedBy().getEmail());
                        row.put("txnModelFor", "customerReceivables");
                        row.put("clickedButtonFor", "over");
                        row.put("totalamount", IdosConstants.decimalFormat.format(totalamount));
                        an.add(row);
                    }
                }
                sbr = new StringBuilder();
                sbr.append("select obj from Transaction obj WHERE obj.transactionBranch='" + branch.getId()
                        + "' and obj.transactionBranchOrganization='" + branch.getOrganization().getId()
                        + "' AND obj.transactionVendorCustomer='" + vendor.getId()
                        + "' AND obj.transactionPurpose =5 and obj.transactionStatus='Accounted' and obj.paymentStatus='PAID' and obj.presentStatus=1 and obj.transactionDate<'"
                        + oneEightyDaysBackDate + "' and obj.transactionDate  between '" + finStartDate + "' and '"
                        + finEndDate + "'");
                txnList = genericDao.executeSimpleQuery(sbr.toString(), entityManager);
                if (!txnList.isEmpty() && txnList.size() > 0) {
                    result.put("result", true);
                    Double totalamount = 0.0;
                    for (Transaction txn : txnList) {
                        ObjectNode row = Json.newObject();
                        row.put("branchName", branch.getName());
                        row.put("txnRefNumber", txn.getTransactionRefNumber());
                        row.put("vendorName", txn.getTransactionVendorCustomer().getName());
                        Double netAmount = 0.0;
                        /*
                         * if(txn.getCustomerNetPayment()!=null){
                         * netAmount=txn.getNetAmount()-txn.getCustomerNetPayment();
                         * totalamount+=netAmount;
                         * }
                         * if(txn.getCustomerNetPayment()==null){
                         * netAmount=txn.getNetAmount();
                         * totalamount+=netAmount;
                         * }
                         */
                        row.put("netAmount", IdosConstants.decimalFormat.format(txn.getNetAmount() * -1));
                        row.put("createdBy", txn.getCreatedBy().getEmail());
                        row.put("txnModelFor", "customerReceivables");
                        row.put("clickedButtonFor", "over");
                        row.put("totalamount", IdosConstants.decimalFormat.format(totalamount));
                        an.add(row);
                    }
                }
            } else if (clickedButtonFor.equals("under")) {
                sbr.append("select obj from Transaction obj WHERE obj.transactionBranch='" + branch.getId()
                        + "' and obj.transactionBranchOrganization='" + branch.getOrganization().getId()
                        + "' AND obj.transactionVendorCustomer='" + vendor.getId()
                        + "' AND obj.transactionPurpose in (2,38) and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate>'"
                        + oneEightyDaysBackDate + "' and obj.transactionDate  between '" + finStartDate + "' and '"
                        + finEndDate + "'");
                List<Transaction> txnList = genericDao.executeSimpleQuery(sbr.toString(), entityManager);
                if (!txnList.isEmpty() && txnList.size() > 0) {
                    result.put("result", true);
                    Double totalamount = 0.0;
                    for (Transaction txn : txnList) {
                        ObjectNode row = Json.newObject();
                        row.put("branchName", branch.getName());
                        row.put("txnRefNumber", txn.getTransactionRefNumber());
                        row.put("vendorName", txn.getTransactionVendorCustomer().getName());
                        Double netAmount = 0.0;
                        /*
                         * if(txn.getCustomerNetPayment()!=null){
                         * netAmount=txn.getNetAmount()-txn.getCustomerNetPayment();
                         * totalamount+=netAmount;
                         * }
                         * if(txn.getCustomerNetPayment()==null){
                         * netAmount=txn.getNetAmount();
                         * totalamount+=netAmount;
                         * }
                         */
                        if (txn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                            row.put("netAmount", IdosConstants.decimalFormat.format(txn.getNetAmount() * -1));
                        } else {
                            row.put("netAmount", IdosConstants.decimalFormat.format(txn.getNetAmount()));
                        }
                        row.put("createdBy", txn.getCreatedBy().getEmail());
                        row.put("txnModelFor", "customerReceivables");
                        row.put("clickedButtonFor", "under");
                        row.put("totalamount", IdosConstants.decimalFormat.format(totalamount));
                        an.add(row);
                    }
                }
                sbr = new StringBuilder();
                sbr.append("select obj from Transaction obj WHERE obj.transactionBranch='" + branch.getId()
                        + "' and obj.transactionBranchOrganization='" + branch.getOrganization().getId()
                        + "' AND obj.transactionVendorCustomer='" + vendor.getId()
                        + "' AND obj.transactionPurpose =5 and obj.transactionStatus='Accounted' and obj.paymentStatus='PAID' and obj.presentStatus=1 and obj.transactionDate>'"
                        + oneEightyDaysBackDate + "' and obj.transactionDate  between '" + finStartDate + "' and '"
                        + finEndDate + "'");
                txnList = genericDao.executeSimpleQuery(sbr.toString(), entityManager);
                if (!txnList.isEmpty() && txnList.size() > 0) {
                    result.put("result", true);
                    Double totalamount = 0.0;
                    for (Transaction txn : txnList) {
                        ObjectNode row = Json.newObject();
                        row.put("branchName", branch.getName());
                        row.put("txnRefNumber", txn.getTransactionRefNumber());
                        row.put("vendorName", txn.getTransactionVendorCustomer().getName());
                        Double netAmount = 0.0;
                        /*
                         * if(txn.getCustomerNetPayment()!=null){
                         * netAmount=txn.getNetAmount()-txn.getCustomerNetPayment();
                         * totalamount+=netAmount;
                         * }
                         * if(txn.getCustomerNetPayment()==null){
                         * netAmount=txn.getNetAmount();
                         * totalamount+=netAmount;
                         * }
                         */
                        row.put("netAmount", IdosConstants.decimalFormat.format(txn.getNetAmount() * -1));
                        row.put("createdBy", txn.getCreatedBy().getEmail());
                        row.put("txnModelFor", "customerReceivables");
                        row.put("clickedButtonFor", "under");
                        row.put("totalamount", IdosConstants.decimalFormat.format(totalamount));
                        an.add(row);
                    }
                }
            } else {
                String periodStartDate = arr[0];
                String periodEndDate = arr[1];
                String period = clickedButtonFor;
                if (period.equalsIgnoreCase("under0to30")) {
                    periodStartDate = under0to30daysDate;
                    periodEndDate = currDate;
                } else if (period.equalsIgnoreCase("under31to60")) {
                    periodStartDate = under31to60daysDate;
                    periodEndDate = under0to30daysDate;
                } else if (period.equalsIgnoreCase("under61to90")) {
                    periodStartDate = under61to90daysDate;
                    periodEndDate = under31to60daysDate;
                } else if (period.equalsIgnoreCase("under91to180")) {
                    periodStartDate = under91To180daysDate;
                    periodEndDate = under61to90daysDate;
                } else if (period.equalsIgnoreCase("over")) {
                    periodStartDate = periodStartDate;
                    periodEndDate = under91To180daysDate;
                }
                sbr.append("select obj from Transaction obj WHERE obj.transactionBranch='" + branch.getId()
                        + "' and obj.transactionBranchOrganization='" + branch.getOrganization().getId()
                        + "' AND obj.transactionVendorCustomer='" + vendor.getId()
                        + "' AND obj.transactionPurpose in (2,38) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate  between '"
                        + periodStartDate + "' and '" + periodEndDate + "'");
                List<Transaction> txnList = genericDao.executeSimpleQuery(sbr.toString(), entityManager);
                if (!txnList.isEmpty() && txnList.size() > 0) {
                    result.put("result", true);
                    Double totalamount = 0.0;
                    for (Transaction txn : txnList) {
                        ObjectNode row = Json.newObject();
                        row.put("branchName", branch.getName());
                        row.put("txnRefNumber", txn.getTransactionRefNumber());
                        row.put("vendorName", txn.getTransactionVendorCustomer().getName());
                        Double netAmount = 0.0;
                        if (txn.getCustomerNetPayment() != null) {
                            netAmount = txn.getNetAmount() - txn.getCustomerNetPayment();
                            totalamount += netAmount;
                        }
                        if (txn.getCustomerNetPayment() == null) {
                            netAmount = txn.getNetAmount();
                            totalamount += netAmount;
                        }
                        if (txn.getTransactionPurpose().getId() == IdosConstants.CANCEL_INVOICE) {
                            row.put("netAmount", IdosConstants.decimalFormat.format(netAmount * -1));
                        } else {
                            row.put("netAmount", IdosConstants.decimalFormat.format(netAmount));
                        }
                        row.put("createdBy", txn.getCreatedBy().getEmail());
                        row.put("txnModelFor", "customerReceivables");
                        row.put("clickedButtonFor", "under91to180");
                        row.put("totalamount", IdosConstants.decimalFormat.format(totalamount));
                        an.add(row);
                    }
                }
                // provision entry transaction

                /*
                 * Map allSpecifcsAmtData=new HashMap();
                 * Map vendorPayablesData=new HashMap();
                 * Map custReceivablesData=new HashMap();
                 * 
                 * Map branchMap =
                 * jourObj.getDashboardProvisionEntriesDataForBranch(periodStartDate,
                 * periodEndDate,user,allSpecifcsAmtData,vendorPayablesData,custReceivablesData,
                 * branch, entityManager);
                 * if(custReceivablesData!=null &&
                 * custReceivablesData.containsKey(vendor.getId().toString())){
                 */
                ProvisionJournalEntryService jourObj = new ProvisionJournalEntryServiceImpl();
                List<ProvisionJournalEntryDetail> userProvisionDetailTransactionList = jourObj
                        .getDetailProvisionEntriesForCustVen(entityManager, periodStartDate, periodEndDate, user,
                                branch, vendor, "cust");
                for (ProvisionJournalEntryDetail journalEntDetailTrx : userProvisionDetailTransactionList) {
                    IdosProvisionJournalEntry journalEntryTran = journalEntDetailTrx.getProvisionJournalEntry();
                    result.put("result", true);
                    double journalEntryamount = journalEntDetailTrx.getHeadAmount();
                    ObjectNode row = Json.newObject();
                    row.put("branchName", branch.getName());
                    // row.put("txnRefNumber", "JournalEntryTransaction");
                    row.put("txnRefNumber", journalEntryTran.getTransactionRefNumber());
                    row.put("vendorName", vendor.getName());
                    row.put("netAmount", IdosConstants.decimalFormat.format(journalEntryamount));
                    row.put("createdBy", "Journal Entry");
                    row.put("txnModelFor", "customerReceivables");
                    row.put("clickedButtonFor", period);
                    row.put("totalamount", IdosConstants.decimalFormat.format(journalEntryamount));
                    an.add(row);
                }
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
    }

    public void vendorPayablesTxnWise(Users user, Branch branch, Vendor vendor, String clickedButtonFor,
            ObjectNode result, ArrayNode an, EntityManager entityManager, EntityTransaction entitytransaction) {
        try {
            log.log(Level.FINE, "********Start");
            String[] arr = DateUtil.getFinancialDate(user);
            String finStartDate = arr[0];
            String finEndDate = arr[1];
            String oneEightyDaysBackDate = DateUtil.returnOneEightyDaysBackDate();
            Date currDateTime = IdosConstants.mysqldf
                    .parse(IdosConstants.mysqldf.format(Calendar.getInstance().getTime()));
            String currDate = IdosConstants.mysqldf.format(currDateTime);
            Date under0to30daysDatedate = DateUtil.returnPrevOneMonthDateDate(currDateTime);
            String under0to30daysDate = IdosConstants.mysqldf.format(under0to30daysDatedate);
            Date under31to60daysDatedate = DateUtil.returnPrevOneMonthDateDate(under0to30daysDatedate);
            String under31to60daysDate = IdosConstants.mysqldf.format(under31to60daysDatedate);
            Date under61to90daysDatedate = DateUtil.returnPrevOneMonthDateDate(under31to60daysDatedate);
            String under61to90daysDate = IdosConstants.mysqldf.format(under61to90daysDatedate);
            Date under91To180daysDatedate = DateUtil.returnPrevThreeMonthDateDate(under61to90daysDatedate);
            String under91To180daysDate = IdosConstants.mysqldf.format(under91To180daysDatedate);
            StringBuilder sbr = new StringBuilder("select obj from Transaction obj WHERE obj.transactionBranch=")
                    .append(branch.getId());
            sbr.append(" and obj.transactionBranchOrganization=").append(branch.getOrganization().getId());
            if (clickedButtonFor.equals("over")) {
                sbr.append(" AND obj.transactionVendorCustomer='" + vendor.getId()
                        + "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate<'"
                        + oneEightyDaysBackDate + "' and obj.transactionDate  between '" + finStartDate + "' and '"
                        + finEndDate + "'");
                List<Transaction> txnList = genericDao.executeSimpleQuery(sbr.toString(), entityManager);
                if (!txnList.isEmpty() && txnList.size() > 0) {
                    result.put("result", true);
                    Double totalamount = 0.0;
                    for (Transaction txn : txnList) {
                        ObjectNode row = Json.newObject();
                        row.put("branchName", branch.getName());
                        row.put("txnRefNumber", txn.getTransactionRefNumber());
                        row.put("vendorName", txn.getTransactionVendorCustomer().getName());
                        Double netAmount = 0.0;
                        /*
                         * if(txn.getCustomerNetPayment()!=null){
                         * netAmount=txn.getNetAmount()-txn.getCustomerNetPayment();
                         * totalamount+=netAmount;
                         * }
                         * if(txn.getCustomerNetPayment()==null){
                         * netAmount=txn.getNetAmount();
                         * totalamount+=netAmount;
                         * }
                         */
                        if (txn.getVendorNetPayment() != null) {
                            netAmount = txn.getNetAmount() - txn.getVendorNetPayment();
                            totalamount += netAmount;
                        }
                        if (txn.getVendorNetPayment() == null) {
                            netAmount = txn.getNetAmount();
                            totalamount += netAmount;
                        }
                        row.put("netAmount", IdosConstants.decimalFormat.format(netAmount));
                        row.put("createdBy", txn.getCreatedBy().getEmail());
                        row.put("txnModelFor", "vendorPayables");
                        row.put("clickedButtonFor", "over");
                        row.put("totalamount", IdosConstants.decimalFormat.format(totalamount));
                        an.add(row);
                    }
                }
            } else if (clickedButtonFor.equals("under")) {
                sbr.append(" AND obj.transactionVendorCustomer='" + vendor.getId()
                        + "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate>'"
                        + oneEightyDaysBackDate + "' and obj.transactionDate  between '" + finStartDate + "' and '"
                        + finEndDate + "'");
                List<Transaction> txnList = genericDao.executeSimpleQuery(sbr.toString(), entityManager);
                if (!txnList.isEmpty() && txnList.size() > 0) {
                    result.put("result", true);
                    Double totalamount = 0.0;
                    for (Transaction txn : txnList) {
                        ObjectNode row = Json.newObject();
                        row.put("branchName", branch.getName());
                        row.put("txnRefNumber", txn.getTransactionRefNumber());
                        row.put("vendorName", txn.getTransactionVendorCustomer().getName());
                        Double netAmount = 0.0;
                        /*
                         * if(txn.getCustomerNetPayment()!=null){
                         * netAmount=txn.getNetAmount()-txn.getCustomerNetPayment();
                         * totalamount+=netAmount;
                         * }
                         * if(txn.getCustomerNetPayment()==null){
                         * netAmount=txn.getNetAmount();
                         * totalamount+=netAmount;
                         * }
                         */
                        if (txn.getVendorNetPayment() != null) {
                            netAmount = txn.getNetAmount() - txn.getVendorNetPayment();
                            totalamount += netAmount;
                        }
                        if (txn.getVendorNetPayment() == null) {
                            netAmount = txn.getNetAmount();
                            totalamount += netAmount;
                        }
                        row.put("netAmount", IdosConstants.decimalFormat.format(netAmount));
                        row.put("createdBy", txn.getCreatedBy().getEmail());
                        row.put("txnModelFor", "vendorPayables");
                        row.put("clickedButtonFor", "under");
                        row.put("totalamount", IdosConstants.decimalFormat.format(totalamount));
                        an.add(row);
                    }
                }
            } else {
                String periodStartDate = arr[0];
                String periodEndDate = arr[1];
                String period = clickedButtonFor;
                if (period.equalsIgnoreCase("under0to30")) {
                    periodStartDate = under0to30daysDate;
                    periodEndDate = currDate;
                } else if (period.equalsIgnoreCase("under31to60")) {
                    periodStartDate = under31to60daysDate;
                    periodEndDate = under0to30daysDate;
                } else if (period.equalsIgnoreCase("under61to90")) {
                    periodStartDate = under61to90daysDate;
                    periodEndDate = under31to60daysDate;
                } else if (period.equalsIgnoreCase("under91to180")) {
                    periodStartDate = under91To180daysDate;
                    periodEndDate = under61to90daysDate;
                } else if (period.equalsIgnoreCase("over")) {
                    periodStartDate = periodStartDate;
                    periodEndDate = under91To180daysDate;
                }
                sbr.append(" AND obj.transactionVendorCustomer='" + vendor.getId()
                        + "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.transactionDate  between '"
                        + periodStartDate + "' and '" + periodEndDate + "'");
                List<Transaction> txnList = genericDao.executeSimpleQuery(sbr.toString(), entityManager);
                if (!txnList.isEmpty() && txnList.size() > 0) {
                    result.put("result", true);
                    Double totalamount = 0.0;
                    for (Transaction txn : txnList) {
                        ObjectNode row = Json.newObject();
                        row.put("branchName", branch.getName());
                        row.put("txnRefNumber", txn.getTransactionRefNumber());
                        row.put("vendorName", txn.getTransactionVendorCustomer().getName());
                        Double netAmount = 0.0;
                        /*
                         * if(txn.getCustomerNetPayment()!=null){
                         * netAmount=txn.getNetAmount()-txn.getCustomerNetPayment();
                         * totalamount+=netAmount;
                         * }
                         * if(txn.getCustomerNetPayment()==null){
                         * netAmount=txn.getNetAmount();
                         * totalamount+=netAmount;
                         * }
                         */
                        if (txn.getVendorNetPayment() != null) {
                            netAmount = txn.getNetAmount() - txn.getVendorNetPayment();
                            totalamount += netAmount;
                        }
                        if (txn.getVendorNetPayment() == null) {
                            netAmount = txn.getNetAmount();
                            totalamount += netAmount;
                        }
                        row.put("netAmount", IdosConstants.decimalFormat.format(netAmount));
                        row.put("createdBy", txn.getCreatedBy().getEmail());
                        row.put("txnModelFor", "vendorPayables");
                        row.put("clickedButtonFor", "under91to180");
                        row.put("totalamount", IdosConstants.decimalFormat.format(totalamount));
                        an.add(row);
                    }
                }
                // provision entry transaction
                /*
                 * Map provisionEntries=new HashMap();
                 * Map allSpecifcsAmtData=new HashMap();
                 * Map vendorPayablesData=new HashMap();
                 * Map custReceivablesData=new HashMap();
                 * Map branchMap =
                 * jourObj.getDashboardProvisionEntriesDataForBranch(periodStartDate,
                 * periodEndDate,user,allSpecifcsAmtData,vendorPayablesData,custReceivablesData,
                 * branch, entityManager);
                 * if(vendorPayablesData!=null &&
                 * vendorPayablesData.containsKey(vendor.getId().toString())){
                 */
                ProvisionJournalEntryService jourObj = new ProvisionJournalEntryServiceImpl();
                List<ProvisionJournalEntryDetail> userProvisionDetailTransactionList = jourObj
                        .getDetailProvisionEntriesForCustVen(entityManager, periodStartDate, periodEndDate, user,
                                branch, vendor, "cust");
                for (ProvisionJournalEntryDetail journalEntDetailTrx : userProvisionDetailTransactionList) {
                    IdosProvisionJournalEntry journalEntryTran = journalEntDetailTrx.getProvisionJournalEntry();
                    result.put("result", true);
                    double journalEntryamount = journalEntDetailTrx.getHeadAmount();
                    ObjectNode row = Json.newObject();
                    row.put("branchName", branch.getName());
                    row.put("txnRefNumber", journalEntryTran.getTransactionRefNumber());
                    row.put("vendorName", vendor.getName());
                    row.put("netAmount", IdosConstants.decimalFormat.format(journalEntryamount));
                    row.put("createdBy", "Journal Entry");
                    row.put("txnModelFor", "customerReceivables");
                    row.put("clickedButtonFor", period);
                    row.put("totalamount", IdosConstants.decimalFormat.format(journalEntryamount));
                    an.add(row);
                }
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
    }

    @Override
    public String downloadOverUnderOneEightyDayaTxnExcel(ObjectNode result, JsonNode json, Users user,
            EntityManager entityManager,
            EntityTransaction entitytransaction, String path) {
        log.log(Level.FINE, "************* Start " + json);
        result.put("result", false);
        String downloadButtonFor = json.findValue("downloadButtonFor") != null
                ? json.findValue("downloadButtonFor").asText()
                : null;
        String downloadTxnModelFor = json.findValue("downloadTxnModelFor") != null
                ? json.findValue("downloadTxnModelFor").asText()
                : null;
        String invoice = null;
        String secColumn = null;
        String[] arr = DateUtil.getFinancialDate(user);
        String finStartDate = arr[0];
        String finEndDate = arr[1];
        String oneEightyDaysBackDate = DateUtil.returnOneEightyDaysBackDate();
        ArrayNode an = result.putArray("overUnderOneEightyReceivablePayablesTxnDataDownload");
        String fileName = null;
        String HQL = null;
        try {
            if (downloadTxnModelFor != null && !downloadTxnModelFor.equals("")) {
                if (downloadTxnModelFor.equals("customerReceivables")) {
                    invoice = "Receivables";
                    secColumn = "Customer Name";
                    if (downloadButtonFor != null && downloadButtonFor.equals("over")) {
                        StringBuilder sbr = new StringBuilder(
                                "select obj from Transaction obj WHERE obj.transactionBranchOrganization.id="
                                        + user.getOrganization().getId()
                                        + " AND obj.transactionPurpose in (2,38) and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate<'"
                                        + oneEightyDaysBackDate + "' and obj.transactionDate  between '" + finStartDate
                                        + "' and '" + finEndDate + "'");
                        HQL = sbr.toString();
                    }
                    if (downloadButtonFor != null && downloadButtonFor.equals("under")) {
                        StringBuilder sbr = new StringBuilder(
                                "select obj from Transaction obj WHERE obj.transactionBranchOrganization.id="
                                        + user.getOrganization().getId()
                                        + " AND obj.transactionPurpose  in (2,38) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate>'"
                                        + oneEightyDaysBackDate + "' and obj.transactionDate  between '" + finStartDate
                                        + "' and '" + finEndDate + "'");
                        HQL = sbr.toString();
                    }
                }
                if (downloadTxnModelFor.equals("vendorPayables")) {
                    invoice = "Payables";
                    secColumn = "Vendor Name";
                    if (downloadButtonFor != null && downloadButtonFor.equals("over")) {
                        StringBuilder sbr = new StringBuilder(
                                "select obj from Transaction obj WHERE obj.transactionBranchOrganization.id="
                                        + user.getOrganization().getId()
                                        + " AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate<'"
                                        + oneEightyDaysBackDate + "' and obj.transactionDate  between '" + finStartDate
                                        + "' and '" + finEndDate + "'");
                        HQL = sbr.toString();
                    }
                    if (downloadButtonFor != null && downloadButtonFor.equals("under")) {
                        StringBuilder sbr = new StringBuilder(
                                "select obj from Transaction obj WHERE obj.transactionBranchOrganization.id="
                                        + user.getOrganization().getId()
                                        + " AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and (obj.paymentStatus='NOT-PAID' or obj.paymentStatus='PARTLY-PAID') and obj.presentStatus=1 and obj.transactionDate>'"
                                        + oneEightyDaysBackDate + "' and obj.transactionDate  between '" + finStartDate
                                        + "' and '" + finEndDate + "'");
                        HQL = sbr.toString();
                    }
                }
            }
            List<Transaction> txnList = genericDao.executeSimpleQuery(HQL, entityManager);
            String orgName = user.getOrganization().getName().replaceAll("\\s", "");
            String sheetName = orgName + invoice;
            fileName = sheetName + ".xlsx";

            Workbook wb = new XSSFWorkbook();
            CreationHelper createHelper = wb.getCreationHelper();
            CellStyle unlockedCellStyle = wb.createCellStyle();
            unlockedCellStyle.setLocked(false);
            Sheet sheets = wb.createSheet(sheetName);
            Row row = sheets.createRow((short) 0);
            sheets.protectSheet("");
            row.createCell(0).setCellValue(createHelper.createRichTextString("Transaction Reference Number"));
            row.createCell(1).setCellValue(createHelper.createRichTextString(secColumn));
            row.createCell(2).setCellValue(createHelper.createRichTextString("Branch Name"));
            row.createCell(3).setCellValue(createHelper.createRichTextString("Created Date"));
            row.createCell(4).setCellValue(createHelper.createRichTextString("Created By"));
            row.createCell(5).setCellValue(createHelper.createRichTextString("Amount"));
            if (!txnList.isEmpty() && txnList.size() > 0) {
                result.put("result", true);
                for (int i = 0; i < txnList.size(); i++) {
                    Double netAmount = 0.0;
                    Row datarows = sheets.createRow((short) i + 1);
                    Cell datacells1 = datarows.createCell(0);
                    datacells1
                            .setCellValue(createHelper.createRichTextString(txnList.get(i).getTransactionRefNumber()));
                    Cell datacells2 = datarows.createCell(1);
                    datacells2.setCellValue(
                            createHelper.createRichTextString(txnList.get(i).getTransactionVendorCustomer().getName()));
                    Cell datacells3 = datarows.createCell(2);
                    datacells3.setCellValue(
                            createHelper.createRichTextString(txnList.get(i).getTransactionBranch().getName()));
                    Cell datacells4 = datarows.createCell(3);
                    datacells4.setCellValue(createHelper
                            .createRichTextString(IdosConstants.idosdf.format(txnList.get(i).getTransactionDate())));
                    Cell datacells5 = datarows.createCell(4);
                    datacells5
                            .setCellValue(createHelper.createRichTextString(txnList.get(i).getCreatedBy().getEmail()));
                    if (txnList.get(i).getCustomerNetPayment() != null) {
                        netAmount = txnList.get(i).getNetAmount() == null ? 0d : txnList.get(i).getNetAmount();
                        netAmount = netAmount - txnList.get(i).getCustomerNetPayment();
                    } else {
                        netAmount = txnList.get(i).getNetAmount();
                    }
                    Cell datacells6 = datarows.createCell(5);
                    datacells6.setCellValue(createHelper.createRichTextString(String.valueOf(netAmount)));
                }
                sheets.autoSizeColumn(0);
                sheets.autoSizeColumn(1);
                sheets.autoSizeColumn(2);
                sheets.autoSizeColumn(3);
                sheets.autoSizeColumn(4);
                sheets.autoSizeColumn(5);
                path = path.concat(fileName);
                FileOutputStream fileOut = new FileOutputStream(path);
                wb.write(fileOut);
                fileOut.close();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        log.log(Level.FINE, "************* End " + fileName);
        return fileName;
    }

    @Override
    public void saveTransactionBRSDate(Users user, EntityManager entityManager, EntityTransaction entitytransaction,
            String transactionRef, String brsBankDate) {
        Map<String, Object> criterias = new HashMap<String, Object>();
        criterias.clear();
        criterias.put("transactionRefNumber", transactionRef);
        criterias.put("presentStatus", 1);
        Transaction transaction = genericDao.getByCriteria(Transaction.class, criterias, entityManager);
        if (null != transaction && null != transaction.getId()) {
            transaction.setBrsBankDate(brsBankDate);
            genericDao.saveOrUpdate(transaction, user, entityManager);
        }
    }

    @Override
    public void setInvoiceQuotProfSerial(Users user, EntityManager entityManager, Transaction txn)
            throws IDOSException {
        Organization organization = user.getOrganization();
        if (organization.getOrgSerialGenrationType() != null && organization.getOrgSerialGenrationType() == 2) {
            setInvoiceQuotProfGstinSerial(user, entityManager, txn);
        } else {
            Integer serialno = 0;
            String serialStr = "";
            Date date = txn.getTransactionDate();
            if (date == null) {
                date = new Date();
            }
            int fullYear = DateUtil.getDateYear(date);
            int year = fullYear % 100;
            String monthName = DateUtil.getMonthName(date);
            String monthNoStr = DateUtil.getMonthNumber(date);
            if (monthNoStr.length() < 2) {
                monthNoStr = "0" + monthNoStr;
            }
            Date startDate = null;
            if (txn.getTransactionBranch().getOrganization().getFinancialStartDate() != null) {
                startDate = txn.getTransactionBranch().getOrganization().getFinancialStartDate();
            } else {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, 3);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDate = cal.getTime();
            }
            Calendar today = Calendar.getInstance();
            boolean isYearChange = false;
            if (organization.getSerialChangedDateYear() != null) {
                Calendar compairCal = Calendar.getInstance();
                compairCal.setTime(organization.getSerialChangedDateYear());
                compairCal.set(Calendar.YEAR, (DateUtil.getDateYear(organization.getSerialChangedDateYear()) + 1));
                isYearChange = today.after(compairCal) ? true : false;
            } else {
                organization.setSerialChangedDateYear(startDate);
            }

            boolean isSerialMonthChange = false;
            if (organization.getSerialCurrentMonth() != null) {
                isSerialMonthChange = (today.get(Calendar.MONTH)) != organization.getSerialCurrentMonth() ? true
                        : false;
            } else {
                Calendar tempCal = Calendar.getInstance();
                tempCal.setTime(startDate);
                organization.setSerialCurrentMonth(tempCal.get(Calendar.MONTH));
                genericDao.saveOrUpdate(organization, user, entityManager);
            }
            Integer serial = 0;
            if (isYearChange) {
                if (organization.getInvoiceInterval() != null && organization.getInvoiceInterval() != 2) {
                    organization.setInvoiceSerial(serial);
                }
                if (organization.getProformaInterval() != null && organization.getProformaInterval() != 2) {
                    organization.setProformaSerial(serial);
                }
                if (organization.getQuotationInterval() != null && organization.getQuotationInterval() != 2) {
                    organization.setQuotationSerial(serial);
                }
                if (organization.getAdvanceReceiptInterval() != null && organization.getAdvanceReceiptInterval() != 2) {
                    organization.setReceiptSerial(serial);
                }
                if (organization.getAdvanceReceiptInterval() != null && organization.getAdvanceReceiptInterval() != 2) {
                    organization.setAdvanceReceiptSerial(serial);
                }
                if (organization.getDebitNoteCustomerInterval() != null
                        && organization.getDebitNoteCustomerInterval() != 2) {
                    organization.setDebitNoteCustomerSerial(serial);
                }
                if (organization.getCreditNoteCustomerSerial() != null
                        && organization.getCreditNoteCustomerSerial() != 2) {
                    organization.setCreditNoteCustomerSerial(serial);
                }
                if (organization.getPurchaseOrderInterval() != null && organization.getPurchaseOrderInterval() != 2) {
                    organization.setPurchaseOrderSerial(serial);
                }
                if (organization.getRefundAdvanceReceiptInterval() != null
                        && organization.getRefundAdvanceReceiptInterval() != 2) {
                    organization.setRefundAdvanceReceiptInterval(serial);
                }
                if (organization.getRefundAmountReceiptInterval() != null
                        && organization.getRefundAmountReceiptInterval() != 2) {
                    organization.setRefundAmountReceiptInterval(serial);
                }
                if (organization.getDeliverChallanReceiptInterval() != null
                        && organization.getDeliverChallanReceiptInterval() != 2) {
                    organization.setDeliverChallanReceiptInterval(serial);
                }
                if (organization.getSelfInvoiceInterval() != null && organization.getSelfInvoiceInterval() != 2) {
                    organization.setSelfInvoiceInterval(serial);
                }

                organization.setSerialChangedDateYear(today.getTime());
                genericDao.saveOrUpdate(organization, user, entityManager);

            } else if (isSerialMonthChange) {
                if (organization.getInvoiceInterval() != null && organization.getInvoiceInterval() == 2) {
                    organization.setInvoiceSerial(serial);
                }
                if (organization.getProformaInterval() != null && organization.getProformaInterval() == 2) {
                    organization.setProformaSerial(serial);
                }
                if (organization.getQuotationInterval() != null && organization.getQuotationInterval() == 2) {
                    organization.setQuotationSerial(serial);
                }
                if (organization.getReceiptInterval() != null && organization.getReceiptInterval() == 2) {
                    organization.setReceiptSerial(serial);
                }
                if (organization.getAdvanceReceiptInterval() != null && organization.getAdvanceReceiptInterval() == 2) {
                    organization.setAdvanceReceiptSerial(serial);
                }
                if (organization.getDebitNoteCustomerInterval() != null
                        && organization.getDebitNoteCustomerInterval() == 2) {
                    organization.setDebitNoteCustomerSerial(serial);
                }
                if (organization.getCreditNoteCustomerSerial() != null
                        && organization.getCreditNoteCustomerSerial() == 2) {
                    organization.setCreditNoteCustomerSerial(serial);
                }
                if (organization.getPurchaseOrderInterval() != null && organization.getPurchaseOrderInterval() == 2) {
                    organization.setPurchaseOrderSerial(serial);
                }
                if (organization.getRefundAdvanceReceiptSerial() != null
                        && organization.getRefundAdvanceReceiptSerial() == 2) {
                    organization.setRefundAdvanceReceiptSerial(serial);
                }
                if (organization.getRefundAmounteReceiptSerial() != null
                        && organization.getRefundAmounteReceiptSerial() == 2) {
                    organization.setRefundAmounteReceiptSerial(serial);
                }
                if (organization.getDeliverChallanReceiptInterval() != null
                        && organization.getDeliverChallanReceiptInterval() == 2) {
                    organization.setDeliverChallanReceiptInterval(serial);
                }
                if (organization.getSelfInvoiceInterval() != null && organization.getSelfInvoiceInterval() == 2) {
                    organization.setSelfInvoiceInterval(serial);
                }

                organization.setSerialCurrentMonth(today.get(Calendar.MONTH));
                genericDao.saveOrUpdate(organization, user, entityManager);
            }

            String bramchName = txn.getTransactionBranch().getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "")
                    .substring(0, 3);
            String olInvoiceStr = ConfigFactory.load().getString("offline.invoice.prefix");
            if (olInvoiceStr != null && !"".equals(olInvoiceStr)) {
                if (txn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                        || txn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
                    if (txn.getInvoiceNumber() != null && txn.getInvoiceNumber() != "")
                        serialStr = txn.getInvoiceNumber();
                    else {
                        serialno = organization.getOffInvoiceSerial() == null ? 1
                                : organization.getOffInvoiceSerial() + 1;
                        organization.setOffInvoiceSerial(serialno);
                    }
                    bramchName = "IV/" + bramchName;
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.CREDIT_NOTE_CUSTOMER) {
                    serialno = organization.getOffInvoiceSerial() == null ? 1 : organization.getOffInvoiceSerial() + 1;
                    organization.setOffInvoiceSerial(serialno);
                    bramchName = "CN/" + bramchName;
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_CUSTOMER) {
                    serialno = organization.getOffInvoiceSerial() == null ? 1 : organization.getOffInvoiceSerial() + 1;
                    organization.setOffInvoiceSerial(serialno);
                    bramchName = "DN/" + bramchName;
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.PREPARE_QUOTATION) {
                    serialno = organization.getOffQuotationSerial() == null ? 1
                            : organization.getOffQuotationSerial() + 1;
                    organization.setOffQuotationSerial(serialno);
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.PROFORMA_INVOICE) {
                    serialno = organization.getOffProformaSerial() == null ? 1
                            : organization.getOffProformaSerial() + 1;
                    organization.setOffProformaSerial(serialno);
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER) {
                    serialno = organization.getOffReceiptSerial() == null ? 1 : organization.getOffReceiptSerial() + 1;
                    organization.setOffReceiptSerial(serialno);
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.PURCHASE_ORDER) {
                    serialno = organization.getOffPurchaseOrderSerial() == null ? 1
                            : organization.getOffPurchaseOrderSerial() + 1;
                    organization.setOffPurchaseOrderSerial(serialno);
                } else if (txn.getTransactionPurpose()
                        .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                    serialno = organization.getOffInvoiceSerial() == null ? 1 : organization.getOffInvoiceSerial() + 1;
                    organization.setOffInvoiceSerial(serialno);
                    if (txn.isTaxApplied()) {
                        bramchName = "IV/" + bramchName;
                    } else {
                        bramchName = "DC/" + bramchName;
                    }
                }

                if (serialStr == null || serialStr == "") {
                    if (serialno.toString().length() == 1)
                        serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + "0000" + serialno;
                    else if (serialno.toString().length() == 2)
                        serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + "000" + serialno;
                    else if (serialno.toString().length() == 3)
                        serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + "00" + serialno;
                    else if (serialno.toString().length() == 4)
                        serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + "0" + serialno;
                    else if (serialno.toString().length() == 5)
                        serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + serialno;
                    else
                        serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + "00000";
                }
                if (serialStr.length() > 16) {
                    do {
                        serialStr = new StringBuilder(serialStr).deleteCharAt(13).toString();
                    } while (serialStr.length() > 16);
                }
                txn.setInvoiceNumber(serialStr.toUpperCase());
            } else {
                if (txn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                        || txn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
                    if (txn.getInvoiceNumber() != null && txn.getInvoiceNumber() != "")
                        serialStr = txn.getInvoiceNumber();
                    else {
                        serialno = organization.getInvoiceSerial() == null ? 1 : organization.getInvoiceSerial() + 1;
                        organization.setInvoiceSerial(serialno);
                    }
                    bramchName = "IV/" + bramchName;
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.CREDIT_NOTE_CUSTOMER) {
                    serialno = organization.getCreditNoteCustomerSerial() == null ? 1
                            : organization.getCreditNoteCustomerSerial() + 1;
                    organization.setCreditNoteCustomerSerial(serialno);
                    bramchName = "CN/" + bramchName;
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_CUSTOMER) {
                    serialno = organization.getDebitNoteCustomerSerial() == null ? 1
                            : organization.getDebitNoteCustomerSerial() + 1;
                    organization.setDebitNoteCustomerSerial(serialno);
                    bramchName = "DN/" + bramchName;
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.PREPARE_QUOTATION) {
                    serialno = organization.getQuotationSerial() == null ? 1 : organization.getQuotationSerial() + 1;
                    organization.setQuotationSerial(serialno);
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.PROFORMA_INVOICE) {
                    serialno = organization.getProformaSerial() == null ? 1 : organization.getProformaSerial() + 1;
                    organization.setProformaSerial(serialno);
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER) {
                    serialno = organization.getReceiptSerial() == null ? 1 : organization.getReceiptSerial() + 1;
                    organization.setReceiptSerial(serialno);
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER) {
                    serialno = organization.getAdvanceReceiptSerial() == null ? 1
                            : organization.getAdvanceReceiptSerial() + 1;
                    organization.setAdvanceReceiptSerial(serialno);
                    bramchName = "ARV/" + bramchName;
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.PURCHASE_ORDER) {
                    serialno = organization.getPurchaseOrderSerial() == null ? 1
                            : organization.getPurchaseOrderSerial() + 1;
                    organization.setPurchaseOrderSerial(serialno);
                } else if (txn.getTransactionPurpose()
                        .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                    serialno = organization.getDeliveryChallanReceiptSerial() == null ? 1
                            : organization.getDeliveryChallanReceiptSerial() + 1;
                    organization.setDeliveryChallanReceiptSerial(serialno);
                    bramchName = "DC/" + bramchName;
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                    serialno = organization.getRefundAdvanceReceiptSerial() == null ? 1
                            : organization.getRefundAdvanceReceiptSerial() + 1;
                    organization.setRefundAdvanceReceiptSerial(serialno);
                    bramchName = "RF/" + bramchName;
                } else if (txn.getTransactionPurpose()
                        .getId() == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
                    serialno = organization.getRefundAmounteReceiptSerial() == null ? 1
                            : organization.getRefundAmounteReceiptSerial() + 1;
                    organization.setRefundAmounteReceiptSerial(serialno);
                    bramchName = "RFR/" + bramchName;
                } else if (txn.getTransactionPurpose().getId() == IdosConstants.PAY_VENDOR_SUPPLIER
                        || txn.getTransactionPurpose().getId() == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
                    serialno = organization.getPaymentVoucherSerial() == null ? 1
                            : organization.getPaymentVoucherSerial() + 1;
                    organization.setRefundAmounteReceiptSerial(serialno);
                    bramchName = "PV/" + bramchName;
                } else if ((txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                        || txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER)
                        && (txn.getTypeOfSupply() == 2 || txn.getTypeOfSupply() == 3)) {
                    serialno = organization.getSelfInvoice() == null ? 1 : organization.getSelfInvoice() + 1;
                    organization.setSelfInvoice(serialno);
                    bramchName = "SIV/" + bramchName;
                }

                if (serialStr == null || serialStr == "") {
                    if (serialno.toString().length() == 1)
                        serialStr = bramchName + monthNoStr + year + "-" + "0000" + serialno;
                    else if (serialno.toString().length() == 2)
                        serialStr = bramchName + monthNoStr + year + "-" + "000" + serialno;
                    else if (serialno.toString().length() == 3)
                        serialStr = bramchName + monthNoStr + year + "-" + "00" + serialno;
                    else if (serialno.toString().length() == 4)
                        serialStr = bramchName + monthNoStr + year + "-" + "0" + serialno;
                    else if (serialno.toString().length() == 5)
                        serialStr = bramchName + monthNoStr + year + "-" + serialno;
                    else
                        serialStr = bramchName + monthNoStr + year + "-" + "00000";
                }
                if (serialStr.length() > 16) {
                    do {
                        serialStr = new StringBuilder(serialStr).deleteCharAt(13).toString();
                    } while (serialStr.length() > 16);
                }
                txn.setInvoiceNumber(serialStr.toUpperCase());
            }
            genericDao.saveOrUpdate(organization, user, entityManager);
        }
    }

    @Override
    public void setInvoiceQuotProfGstinSerial(Users user, EntityManager entityManager, Transaction txn)
            throws IDOSException {
        OrganizationGstinSerials orgSerialBranch = null;
        Organization organization = txn.getTransactionBranch().getOrganization();
        Integer serialno = 0;
        String serialStr = "";
        Date date = txn.getTransactionDate();
        if (date == null) {
            date = Calendar.getInstance().getTime();
        }
        int fullYear = DateUtil.getDateYear(date);
        int year = fullYear % 100;
        String monthName = DateUtil.getMonthName(date);
        String monthNoStr = DateUtil.getMonthNumber(date);
        if (monthNoStr.length() < 2) {
            monthNoStr = "0" + monthNoStr;
        }
        Long orgId = txn.getTransactionBranch().getOrganization().getId();
        String gstIn = txn.getTransactionBranch().getGstin();
        Date startDate = null;
        if (txn.getTransactionBranch().getOrganization().getFinancialStartDate() != null) {
            startDate = txn.getTransactionBranch().getOrganization().getFinancialStartDate();
        } else {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, 3);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            startDate = cal.getTime();
        }
        Calendar today = Calendar.getInstance();
        boolean isYearChange = false;
        if (organization.getSerialChangedDateYear() != null) {
            Calendar compairCal = Calendar.getInstance();
            compairCal.setTime(organization.getSerialChangedDateYear());
            compairCal.set(Calendar.YEAR, (DateUtil.getDateYear(organization.getSerialChangedDateYear()) + 1));
            isYearChange = today.after(compairCal) ? true : false;
        } else {
            organization.setSerialChangedDateYear(startDate);
            isYearChange = true;
        }
        boolean isSerialMonthChange = false;
        if (organization.getSerialCurrentMonth() != null) {
            isSerialMonthChange = (today.get(Calendar.MONTH)) != organization.getSerialCurrentMonth() ? true : false;
        } else {
            Calendar tempCal = Calendar.getInstance();
            tempCal.setTime(startDate);
            organization.setSerialCurrentMonth(tempCal.get(Calendar.MONTH));
            genericDao.saveOrUpdate(organization, user, entityManager);
        }
        Integer interval = 0;
        if (txn.getTransactionBranch().getOrganization().getGstInInterval() != null) {
            interval = txn.getTransactionBranch().getOrganization().getGstInInterval();
        } else {
            interval = 1;
        }
        if (interval == 1) {
            if (isYearChange) {
                List<OrganizationGstinSerials> byOrganization = OrganizationGstinSerials
                        .getByOrganization(entityManager, orgId);
                for (OrganizationGstinSerials obj : byOrganization) {
                    obj.setSerialNo(0);
                }
                organization.setSerialChangedDateYear(today.getTime());
                genericDao.saveOrUpdate(organization, user, entityManager);
            }
        } else if (interval == 2) {
            if (isSerialMonthChange) {
                List<OrganizationGstinSerials> byOrganization = OrganizationGstinSerials
                        .getByOrganization(entityManager, orgId);
                for (OrganizationGstinSerials obj : byOrganization) {
                    obj.setSerialNo(0);
                }
                organization.setSerialCurrentMonth(today.get(Calendar.MONTH));
                genericDao.saveOrUpdate(organization, user, entityManager);
            }
        }
        String bramchName = txn.getTransactionBranch().getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "").substring(0,
                3);
        String olInvoiceStr = ConfigFactory.load().getString("offline.invoice.prefix");
        if (olInvoiceStr != null && !"".equals(olInvoiceStr)) {
            if (txn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                    || txn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
                if (txn.getInvoiceNumber() != null && txn.getInvoiceNumber() != "")
                    serialStr = txn.getInvoiceNumber();
                else {
                    orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                            IdosConstants.GSTIN_SERIAL_FOR_SALES_INVOICE, gstIn);
                    if (orgSerialBranch == null) {
                        orgSerialBranch = new OrganizationGstinSerials();
                        orgSerialBranch.setGstIn(gstIn);
                        orgSerialBranch.setOrganization(user.getOrganization());
                        orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_SALES_INVOICE);
                        serialno = 1;
                    } else {
                        serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                    }
                }
                bramchName = "IV/" + bramchName;
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.CREDIT_NOTE_CUSTOMER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_CREDIT_NOTE_TO_CUST, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_CREDIT_NOTE_TO_CUST);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "CN/" + bramchName;
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_CUSTOMER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_DEBIT_NOTE_TO_CUST, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_DEBIT_NOTE_TO_CUST);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "DN/" + bramchName;
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.PREPARE_QUOTATION) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_QUOTATION, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_QUOTATION);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.PROFORMA_INVOICE) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_PROFORMA, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_PROFORMA);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_RECEIPT, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_RECEIPT);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.PURCHASE_ORDER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_PURCHASE_ORDER, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_PURCHASE_ORDER);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }

                orgSerialBranch.setSerialNo(serialno);
            } else if (txn.getTransactionPurpose()
                    .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_SALES_INVOICE, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_SALES_INVOICE);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                if (txn.isTaxApplied()) {
                    bramchName = "IV/" + bramchName;
                } else {
                    bramchName = "DC/" + bramchName;
                }
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_REFUND_ADVANCE_RECEIVED, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_REFUND_ADVANCE_RECEIVED);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "RF/" + bramchName;
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_REFUND_AMOUNT_AGAINST_INVOICE_RECEIVED, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(
                            IdosConstants.GSTIN_SERIAL_FOR_REFUND_AMOUNT_AGAINST_INVOICE_RECEIVED);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "RFR/" + bramchName;
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                    || txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_SELF_INVOICE, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_SELF_INVOICE);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "SIV/" + bramchName;
            }

            if (serialStr == null || serialStr == "") {
                if (serialno.toString().length() == 1)
                    serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + "0000" + serialno;
                else if (serialno.toString().length() == 2)
                    serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + "000" + serialno;
                else if (serialno.toString().length() == 3)
                    serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + "00" + serialno;
                else if (serialno.toString().length() == 4)
                    serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + "0" + serialno;
                else if (serialno.toString().length() == 5)
                    serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + serialno;
                else
                    serialStr = bramchName + monthNoStr + year + "-" + olInvoiceStr + "00000";
            }
            if (serialStr.length() > 16) {
                do {
                    serialStr = new StringBuilder(serialStr).deleteCharAt(13).toString();
                } while (serialStr.length() > 16);
            }
            txn.setInvoiceNumber(serialStr.toUpperCase());
        } else {
            if (txn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
                    || txn.getTransactionPurpose().getId() == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER) {
                if (txn.getInvoiceNumber() != null && txn.getInvoiceNumber() != "")
                    serialStr = txn.getInvoiceNumber();
                else {
                    orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                            IdosConstants.GSTIN_SERIAL_FOR_SALES_INVOICE, gstIn);
                    if (orgSerialBranch == null) {
                        orgSerialBranch = new OrganizationGstinSerials();
                        orgSerialBranch.setGstIn(gstIn);
                        orgSerialBranch.setOrganization(user.getOrganization());
                        orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_SALES_INVOICE);
                        serialno = 1;
                    } else {
                        serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                    }
                    orgSerialBranch.setSerialNo(serialno);
                }
                bramchName = "IV/" + bramchName;
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.CREDIT_NOTE_CUSTOMER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_CREDIT_NOTE_TO_CUST, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_CREDIT_NOTE_TO_CUST);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "CN/" + bramchName;
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.DEBIT_NOTE_CUSTOMER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_DEBIT_NOTE_TO_CUST, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_DEBIT_NOTE_TO_CUST);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "DN/" + bramchName;
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.PREPARE_QUOTATION) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_QUOTATION, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_QUOTATION);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.PROFORMA_INVOICE) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_PROFORMA, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_PROFORMA);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_RECEIPT, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_RECEIPT);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_ADVANCE_RECEIPT, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_ADVANCE_RECEIPT);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "ARV/" + bramchName;

            } else if (txn.getTransactionPurpose().getId() == IdosConstants.PURCHASE_ORDER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_PURCHASE_ORDER, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_PURCHASE_ORDER);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
            } else if (txn.getTransactionPurpose()
                    .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_SALES_INVOICE, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_SALES_INVOICE);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                if (txn.isTaxApplied()) {
                    bramchName = "IV/" + bramchName;
                } else {
                    bramchName = "DC/" + bramchName;
                }
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_REFUND_ADVANCE_RECEIVED, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_REFUND_ADVANCE_RECEIVED);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "RF/" + bramchName;
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_REFUND_AMOUNT_AGAINST_INVOICE_RECEIVED, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(
                            IdosConstants.GSTIN_SERIAL_FOR_REFUND_AMOUNT_AGAINST_INVOICE_RECEIVED);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "RFR/" + bramchName;
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.PAY_VENDOR_SUPPLIER
                    || txn.getTransactionPurpose().getId() == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_PAYMENT_VOUCHER, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_PAYMENT_VOUCHER);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "PV/" + bramchName;
            } else if (txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                    || txn.getTransactionPurpose().getId() == IdosConstants.BUY_ON_CREDIT_PAY_LATER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_SELF_INVOICE, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_PAYMENT_VOUCHER);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo() + 1;
                }
                orgSerialBranch.setSerialNo(serialno);
                bramchName = "SIV/" + bramchName;
            }
            if (serialStr == null || serialStr == "") {
                if (serialno.toString().length() == 1 || serialStr == "")
                    serialStr = bramchName + monthNoStr + year + "-" + "0000" + serialno;
                else if (serialno.toString().length() == 2)
                    serialStr = bramchName + monthNoStr + year + "-" + "000" + serialno;
                else if (serialno.toString().length() == 3)
                    serialStr = bramchName + monthNoStr + year + "-" + "00" + serialno;
                else if (serialno.toString().length() == 4)
                    serialStr = bramchName + monthNoStr + year + "-" + "0" + serialno;
                else if (serialno.toString().length() == 5)
                    serialStr = bramchName + monthNoStr + year + "-" + serialno;
                else
                    serialStr = bramchName + monthNoStr + year + "-" + "00000";
            }
            if (serialStr.length() > 16) {
                do {
                    serialStr = new StringBuilder(serialStr).deleteCharAt(13).toString();
                } while (serialStr.length() > 16);
            }
            txn.setInvoiceNumber(serialStr.toUpperCase());
        }
        genericDao.saveOrUpdate(orgSerialBranch, user, entityManager);
    }

    @Override
    public String getAndDeleteSupportingDocument(String exisitingDocs, String email, String newSupportingDoc,
            Users user, EntityManager em) throws IDOSException {
        List<String> list = UploadUtil.getSupportingDocuments(exisitingDocs, email, newSupportingDoc);
        String newList = null;
        if (list.size() > 0) {
            newList = list.get(0);
        }
        if (list.size() > 1) {
            FILE_UPLOAD_SERVICE.deleteBlobsList(list.get(1), user.getOrganization(), user, false, em);
        }
        return newList;
    }

    @Override
    public List<Transaction> findByOrgCustVendPaymentStatusLinkedTxn(long orgid, long branchId, long custVendId,
            List<Long> txnPurposeList, List<String> paymentStatusList, String txnStatus, String linkedTxnRef,
            EntityManager em) {
        List<Transaction> list = null;
        ArrayList inparams = new ArrayList(7);
        inparams.add(orgid);
        inparams.add(branchId);
        inparams.add(custVendId);
        inparams.add(txnPurposeList);
        inparams.add(paymentStatusList);
        inparams.add(txnStatus);
        inparams.add(linkedTxnRef);
        list = genericDao.queryWithParamsName(LINKED_TXN_JPQL, em, inparams);
        return list;
    }
}
