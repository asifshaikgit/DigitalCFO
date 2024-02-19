package com.idos.dao.trialbalance;

import com.idos.dao.BaseDAO;
import model.TrialBalance;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.Date;

/**
 * @author Sunil K Namdev created on 27.11.2019
 */
public interface TrialBalanceEmpClaimDAO extends BaseDAO {
    String EMP_CLAIM_ORG_JPQL = "select sum(obalance), sum(credit), sum(debit), name, uid from(select OPENING_BALANCE_CLAIM as obalance, 0 as credit, 0 as debit, FULL_NAME as name, ID as uid from USERS where BRANCH_ORGANIZATION_ID = ?1 and ID = ?2 and PRESENT_STATUS=1 union all select (sum(DEBIT_AMOUNT) - sum(CREDIT_AMOUNT)) as obalance, 0 as credit, 0 as debit, null as name, t1.USER_ID as uid  from TRIALBALANCE_USER_CLAIM t1 where t1.ORGNIZATION_ID= ?3 and t1.USER_ID = ?4 and t1.DATE < ?5 and t1.PRESENT_STATUS=1  union all select 0 as obalance, sum(CREDIT_AMOUNT) as credit, sum(DEBIT_AMOUNT) as debit, null as name, t1.USER_ID as uid from TRIALBALANCE_USER_CLAIM t1 where t1.ORGNIZATION_ID= ?6 and t1.USER_ID = ?7 and t1.DATE between ?8 and ?9 and t1.PRESENT_STATUS=1) tbl group by uid";

    String EMP_CLAIM_BRNH_JPQL = "select sum(obalance), sum(credit), sum(debit), name, uid from(select OPENING_BALANCE_CLAIM as obalance, 0 as credit, 0 as debit, FULL_NAME as name, ID as uid from USERS where BRANCH_ORGANIZATION_ID = ?1 and BRANCH_ID = ?2 and ID = ?3 and PRESENT_STATUS=1 union all select (sum(DEBIT_AMOUNT) - sum(CREDIT_AMOUNT)) as obalance, 0 as credit, 0 as debit, null as name, t1.USER_ID as uid  from TRIALBALANCE_USER_CLAIM t1 where t1.ORGNIZATION_ID = ?4 and t1.BRANCH_ID = ?5 and t1.USER_ID = ?6 and t1.DATE < ?7 and t1.PRESENT_STATUS=1 union all select 0 as obalance, sum(CREDIT_AMOUNT) as credit, sum(DEBIT_AMOUNT) as debit, null as name, t1.USER_ID as uid from TRIALBALANCE_USER_CLAIM t1 where t1.ORGNIZATION_ID= ?8 and t1.BRANCH_ID = ?9 and t1.USER_ID = ?10 and t1.DATE between ?11 and ?12 and t1.PRESENT_STATUS=1) tbl group by uid";

    TrialBalance getEmployeeClaim(Users user, EntityManager em, Date fromDate, Date toDate, Long branchId, Users emp);
}
