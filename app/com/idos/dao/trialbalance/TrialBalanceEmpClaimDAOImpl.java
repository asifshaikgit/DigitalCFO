package com.idos.dao.trialbalance;

import com.idos.util.IdosConstants;
import model.TrialBalance;
import model.Users;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Sunil K Namdev created on 27.11.2019
 */
public class TrialBalanceEmpClaimDAOImpl implements TrialBalanceEmpClaimDAO {
    public TrialBalance getEmployeeClaim(Users user, EntityManager em, Date fromDate, Date toDate, Long branchId,
            Users emp) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*****Start ");
        String jpql = null;
        TrialBalance tb = null;
        ArrayList inparams = new ArrayList(9);
        inparams.add(user.getOrganization().getId());
        if (branchId != null && branchId > 0L) {
            jpql = EMP_CLAIM_BRNH_JPQL;
            inparams.add(branchId);
            inparams.add(emp.getId());
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
            inparams.add(emp.getId());
            inparams.add(fromDate);
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
        } else {
            jpql = EMP_CLAIM_ORG_JPQL;
            inparams.add(emp.getId());
            inparams.add(user.getOrganization().getId());
            inparams.add(emp.getId());
            inparams.add(fromDate);
            inparams.add(user.getOrganization().getId());
        }
        inparams.add(emp.getId());
        inparams.add(fromDate);
        inparams.add(toDate);
        List<Object[]> txnLists = genericDao.executeNativeQueryWithParamName(jpql, em, inparams);
        Double creditAmt = 0.0;
        Double debitAmt = 0.0;
        Double openingBalance = 0.0;
        if (txnLists.size() > 0) {
            Object[] usertraveladvData = txnLists.get(0);
            openingBalance = usertraveladvData[0] == null ? 0.0 : (Double) usertraveladvData[0];
            creditAmt = usertraveladvData[1] == null ? 0.0 : (Double) usertraveladvData[1];
            debitAmt = usertraveladvData[2] == null ? 0.0 : (Double) usertraveladvData[2];
            tb = new TrialBalance();
            tb.setAccountName(emp.getFullName() + "-Claim");
            tb.setSpecId(emp.getId());
            tb.setSpecfaccountCode("1");
            tb.setTopLevelAccountCode("4000000000000000000");
            tb.setOpeningBalance(openingBalance);
            tb.setDebit(debitAmt);
            tb.setCredit(creditAmt);
            tb.setClosingBalance(openingBalance + debitAmt - creditAmt);
            tb.setHeadType(IdosConstants.HEAD_EMP_CLAIM);
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*****End ");
        return tb;
    }
}
