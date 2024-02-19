package com.idos.dao.trialbalance;

import com.idos.util.IdosConstants;
import model.BranchBankAccounts;
import model.BranchDepositBoxKey;
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
public class TrialBalanceCashDAOImpl implements TrialBalanceCashDAO {

    @Override
    public TrialBalance getTrialBalance4SpecificCash(Users user, EntityManager em, Date fromDate, Date toDate,
            final int cashType, Long branchId, BranchDepositBoxKey cashAccount) {
        log.log(Level.FINE, ">>>>>>>>>>Start ");
        String cashAcPostfix = null;
        String headType = null;

        if (cashType == IdosConstants.CASH) {
            cashAcPostfix = IdosConstants.PAYMENT_MODE_CASH;
            headType = IdosConstants.HEAD_CASH;
        } else {
            cashAcPostfix = IdosConstants.PAYMENT_MODE_PETTYCASH;
            headType = IdosConstants.HEAD_PETTY;
        }
        String sql = null;
        ArrayList inparam = new ArrayList(4);
        inparam.add(user.getOrganization().getId());
        if (branchId != null && branchId > 0L) {
            inparam.add(branchId);
            sql = cashBranch;
        } else {
            sql = cashOrg;
        }
        inparam.add(cashAccount.getId());
        inparam.add(fromDate);
        inparam.add(toDate);

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.INFO, "HQL: " + sql);
        }
        List<Object[]> txnLists = genericDao.queryWithParamsNameGeneric(sql, em, inparam);
        Double creditAmt = 0.0;
        Double debitAmt = 0.0;
        String cashAccNAme;
        Long cashID;
        TrialBalance tb = null;
        if (txnLists.size() > 0) {
            Object[] custData = txnLists.get(0);
            cashAccNAme = custData[0].toString() + " - " + cashAcPostfix;
            cashID = Long.parseLong(String.valueOf(custData[1]));
            creditAmt = Double.parseDouble(String.valueOf(custData[3]));
            debitAmt = Double.parseDouble(String.valueOf(custData[4]));
            double openBal = 0.0;
            if (cashType == IdosConstants.CASH) {
                openBal = Double.parseDouble(String.valueOf(custData[2] == null ? "0.0" : custData[2]));
            } else {
                openBal = Double.parseDouble(String.valueOf(custData[2] == null ? "0.0" : custData[5]));
            }
            Double derivedOpenBal = TRIAL_BALANCE_DAO.getOpeningBalForSpecifics("TrialBalanceBranchCash", openBal,
                    cashID, user, IdosConstants.MYSQLDF.format(fromDate), "branchDepositBoxKey", IdosConstants.ASSETS,
                    branchId, em, headType);

            tb = new TrialBalance();
            tb.setAccountName(cashAccNAme);
            tb.setSpecId(cashID);
            tb.setSpecfaccountCode("1");
            tb.setTopLevelAccountCode("3000000000000000000");
            tb.setOpeningBalance(derivedOpenBal);
            tb.setDebit(debitAmt);
            tb.setCredit(creditAmt);
            tb.setHeadType(headType);
            tb.setClosingBalance(derivedOpenBal + debitAmt - creditAmt);
        }
        log.log(Level.FINE, ">>>>>>>>>>End ");
        return tb;
    }

}
