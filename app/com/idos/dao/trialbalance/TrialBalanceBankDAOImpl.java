package com.idos.dao.trialbalance;

import com.idos.util.IdosConstants;
import model.BranchBankAccounts;
import model.TrialBalance;
import model.Users;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Sunil K Namdev created on 24.11.2019
 *
 */
public class TrialBalanceBankDAOImpl implements TrialBalanceBankDAO {

    @Override
    public TrialBalance getTrialBalance4SpecificBank(Users user, EntityManager em, Date fromDate, Date toDate,
            Long branchId, BranchBankAccounts bankAccount) {
        log.log(Level.FINE, ">>>>>>>>>>Start ");
        String sql = null;
        ArrayList inparam = new ArrayList(4);
        inparam.add(user.getOrganization().getId());
        if (branchId != null && branchId > 0L) {
            inparam.add(branchId);
            sql = branchBank;
        } else {
            sql = orgBank;
        }
        inparam.add(bankAccount.getId());
        inparam.add(fromDate);
        inparam.add(toDate);

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.INFO, "HQL: " + sql);
        }
        List<Object[]> txnLists = genericDao.queryWithParamsNameGeneric(sql, em, inparam);
        Double creditAmt = 0.0;
        Double debitAmt = 0.0;
        String bankName;
        Long bankId;
        TrialBalance tb = null;
        if (txnLists.size() > 0) {
            Object[] custData = txnLists.get(0);
            bankName = String.valueOf(custData[0] == null ? "" : custData[0].toString()) + " - "
                    + String.valueOf(custData[1] == null ? "" : custData[1].toString());
            bankId = Long.parseLong(String.valueOf(custData[2] == null ? "0" : custData[2]));
            double openBal = Double.parseDouble(String.valueOf(custData[3] == null ? "0.0" : custData[3]));
            creditAmt = Double.parseDouble(String.valueOf(custData[4] == null ? "0.0" : custData[4]));
            debitAmt = Double.parseDouble(String.valueOf(custData[5] == null ? "0.0" : custData[5]));
            Double derivedOpenBal = TRIAL_BALANCE_DAO.getOpeningBalForSpecifics("TrialBalanceBranchBank", openBal,
                    bankId, user, IdosConstants.MYSQLDF.format(fromDate), "branchBankAccounts", IdosConstants.ASSETS,
                    branchId, em, IdosConstants.HEAD_BANK);
            tb = new TrialBalance();
            tb.setAccountName(bankName);
            tb.setSpecId(bankId);
            tb.setSpecfaccountCode("1");
            tb.setTopLevelAccountCode("3000000000000000000");
            tb.setOpeningBalance(derivedOpenBal);
            tb.setDebit(debitAmt);
            tb.setCredit(creditAmt);
            tb.setHeadType(IdosConstants.HEAD_BANK);
            tb.setClosingBalance(derivedOpenBal + debitAmt - creditAmt);
        }
        log.log(Level.FINE, ">>>>>>>>>>End ");
        return tb;
    }
}
