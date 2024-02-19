package com.idos.dao.trialbalance;

import com.idos.util.IdosConstants;
import model.Specifics;
import model.TrialBalance;
import model.Users;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author $(USER) created on $(DATE)
 */
public class TrialBalanceSpecificDAOImpl implements TrialBalanceSpecificDAO {

    public TrialBalance getTrialBalance4Specific(Users user, EntityManager em, Specifics specific, Date fromDate,
            Date toDate, Long branchId, final short particularType) {
        String sql = null;
        ArrayList inparams = new ArrayList(5);
        inparams.add(user.getOrganization().getId());
        if (branchId != null && branchId > 0L) {
            sql = SPECIFIC_BRANCH_JPQL;
            inparams.add(branchId);
            inparams.add(specific.getId());
            inparams.add(specific.getParticularsId().getId());
            inparams.add(fromDate);
            inparams.add(toDate);
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
            inparams.add(specific.getId());
            inparams.add(specific.getParticularsId().getId());
            inparams.add(fromDate);
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
            inparams.add(specific.getId());
        } else {
            sql = SPECIFIC_ORG_JPQL;
            inparams.add(specific.getId());
            inparams.add(specific.getParticularsId().getId());
            inparams.add(fromDate);
            inparams.add(toDate);
            inparams.add(user.getOrganization().getId());
            inparams.add(specific.getId());
            inparams.add(specific.getParticularsId().getId());
            inparams.add(fromDate);
            inparams.add(user.getOrganization().getId());
            inparams.add(specific.getId());
        }
        TrialBalance tb = null;
        Double creditAmt = 0.0;
        Double debitAmt = 0.0;
        Double openBal = 0.0;
        Double prevCreditAmt = 0.0;
        Double prevDebitAmt = 0.0;
        List<Object[]> txnLists = genericDao.executeNativeQueryWithParam(sql, em, inparams);
        for (Object[] val : txnLists) {
            creditAmt += val[0] != null ? Double.parseDouble(String.valueOf(val[0])) : 0.0;
            debitAmt += val[1] != null ? Double.parseDouble(String.valueOf(val[1])) : 0.0;
            openBal += val[2] != null ? Double.parseDouble(String.valueOf(val[2])) : 0.0;
            prevCreditAmt += val[3] != null ? Double.parseDouble(String.valueOf(val[3])) : 0.0;
            prevDebitAmt += val[4] != null ? Double.parseDouble(String.valueOf(val[4])) : 0.0;
        }
        log.log(Level.FINE, "----- " + openBal + " " + prevCreditAmt + "   " + prevDebitAmt);
        if (txnLists.size() > 0) {
            tb = new TrialBalance();
            tb.setAccountName(specific.getName());
            tb.setSpecfaccountCode(specific.getAccountCode().toString());
            tb.setSpecId(specific.getId());
            tb.setDebit(debitAmt);
            tb.setCredit(creditAmt);
            tb.setHeadType(IdosConstants.HEAD_SPECIFIC);
            if (particularType == IdosConstants.ASSETS || particularType == IdosConstants.EXPENSE) {
                tb.setClosingBalance(openBal + debitAmt - creditAmt);
                tb.setOpeningBalance(openBal + prevDebitAmt - prevCreditAmt);
            } else {
                tb.setClosingBalance(openBal + creditAmt - debitAmt);
                tb.setOpeningBalance(openBal + prevCreditAmt - prevDebitAmt);
            }
        }
        log.log(Level.FINE, ">>>>>>>>>> End  " + tb);
        return tb;
    }
}
