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
public class TrialBalanceEmpAdvancePaidDAOImpl implements TrialBalanceEmpAdvancePaidDAO {
    @Override
    public TrialBalance getUserAdvanceForExpenseAndTravel(Users user, EntityManager em, Date fromDate, Date toDate,
            Long branchId, Users emp) {
        String USER_ADV_EXP_TRAV_ORG_JPQL = "select sum(obalance), sum(credit), sum(debit), name, uid from(select OPENING_BALANCE_ADV as obalance, 0 as credit, 0 as debit, FULL_NAME as name, ID as uid from USERS where BRANCH_ORGANIZATION_ID = ?1 and ID = ?2  and PRESENT_STATUS=1 union all select (sum(DEBIT_AMOUNT) - sum(CREDIT_AMOUNT)) as obalance, 0 as credit, 0 as debit, null as name, t1.USER_ID as uid  from TRIALBALANCE_USER_ADVANCE t1 where t1.BRANCH_ORGNIZATION_ID= ?3 and t1.USER_ID = ?4 and t1.DATE < ?5 and t1.PRESENT_STATUS=1 union all select 0 as obalance, sum(CREDIT_AMOUNT) as credit, sum(DEBIT_AMOUNT) as debit, null as name, t1.USER_ID as uid from TRIALBALANCE_USER_ADVANCE t1 where t1.BRANCH_ORGNIZATION_ID= ?6 and t1.USER_ID = ?7 and t1.DATE between ?8 and ?9 and t1.PRESENT_STATUS=1) tbl group by uid";
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*****Start ");
        String jpql = null;
        TrialBalance tb = null;

        ArrayList inparams = new ArrayList(9);
        inparams.add(user.getOrganization().getId());
        if (branchId != null && branchId > 0L) {
            jpql = USER_ADV_EXP_TRAV_BRNH_JPQL;
            inparams.add(branchId);
            inparams.add(emp.getId());
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
            inparams.add(emp.getId());
            inparams.add(fromDate);
            inparams.add(user.getOrganization().getId());
            inparams.add(branchId);
        } else {
            jpql = USER_ADV_EXP_TRAV_ORG_JPQL;
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
        String userName;
        Long userID = 0l;
        Double openingBalance = 0.0;

        if (txnLists.size() > 0) {
            Object[] usertraveladvData = txnLists.get(0);
            openingBalance = usertraveladvData[0] == null ? 0.0 : (Double) usertraveladvData[0];

            creditAmt = usertraveladvData[1] == null ? 0.0 : (Double) usertraveladvData[1];
            debitAmt = usertraveladvData[2] == null ? 0.0 : (Double) usertraveladvData[2];
            tb = new TrialBalance();
            tb.setAccountName(emp.getFullName() + "-Advance");
            tb.setSpecId(emp.getId());
            tb.setSpecfaccountCode("1");
            tb.setTopLevelAccountCode("3000000000000000000");
            tb.setOpeningBalance(openingBalance);
            tb.setDebit(debitAmt);
            tb.setCredit(creditAmt);
            tb.setClosingBalance(openingBalance + debitAmt - creditAmt);
            tb.setHeadType(IdosConstants.HEAD_EXP_ADV);
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "*****End ");
        return tb;
    }
}
