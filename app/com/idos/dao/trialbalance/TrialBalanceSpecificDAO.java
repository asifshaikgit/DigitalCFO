package com.idos.dao.trialbalance;

import com.idos.dao.BaseDAO;
import model.Specifics;
import model.TrialBalance;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.Date;

/**
 * @author $(USER) created on $(DATE)
 */
public interface TrialBalanceSpecificDAO extends BaseDAO {
    String SPECIFIC_ORG_JPQL = "select SUM(CREDIT_AMOUNT) as cdt, SUM(DEBIT_AMOUNT) as dbt, 0 as ob, 0 as pcrd, 0 as pdbt from TRIALBALANCE_COAITEMS where BRANCH_ORGNIZATION_ID = ?1 and TRANSACTION_SPECIFICS= ?2 and TRANSACTION_SPECIFICS_PARTICULARS= ?3 and PRESENT_STATUS=1 and DATE  between ?4 and ?5 group by TRANSACTION_SPECIFICS union all select 0 as cdt, 0 as dbt, 0 as ob, SUM(CREDIT_AMOUNT) as pcdt, SUM(DEBIT_AMOUNT) as pdbt from TRIALBALANCE_COAITEMS where BRANCH_ORGNIZATION_ID = ?6 and TRANSACTION_SPECIFICS= ?7 and TRANSACTION_SPECIFICS_PARTICULARS= ?8 and PRESENT_STATUS=1 and DATE < ?9 group by TRANSACTION_SPECIFICS union all select 0 as cdt, 0 as dbt, sum(OPENING_BALANCE) as ob, 0 as pcrd, 0 as pdbt from BRANCH_has_SPECIFICS where BRANCH_ORGANIZATION_ID= ?10 and SPECIFICS_ID= ?11 and PRESENT_STATUS=1 group by SPECIFICS_ID";

    String SPECIFIC_BRANCH_JPQL = "select SUM(CREDIT_AMOUNT) as cdt, SUM(DEBIT_AMOUNT) as dbt, 0 as ob, 0 as pcrd, 0 as pdbt from TRIALBALANCE_COAITEMS where BRANCH_ORGNIZATION_ID = ?1 and BRANCH_ID = ?2 and TRANSACTION_SPECIFICS= ?3 and TRANSACTION_SPECIFICS_PARTICULARS= ?4 and PRESENT_STATUS=1 and DATE between ?5 and ?6 group by TRANSACTION_SPECIFICS union all select 0 as cdt, 0 as dbt, 0 as ob, SUM(CREDIT_AMOUNT) as pcdt, SUM(DEBIT_AMOUNT) as pdbt from TRIALBALANCE_COAITEMS where BRANCH_ORGNIZATION_ID = ?7 and BRANCH_ID = ?8 and TRANSACTION_SPECIFICS= ?9 and TRANSACTION_SPECIFICS_PARTICULARS= ?10 and PRESENT_STATUS=1 and DATE < ?11 group by TRANSACTION_SPECIFICS union all select 0 as cdt, 0 as dbt, sum(OPENING_BALANCE) as ob, 0 as pcrd, 0 as pdbt from BRANCH_has_SPECIFICS where BRANCH_ORGANIZATION_ID= ?12 and BRANCH_ID = ?13 and SPECIFICS_ID= ?14 and PRESENT_STATUS=1 group by SPECIFICS_ID";

    TrialBalance getTrialBalance4Specific(Users user, EntityManager em, Specifics specific, Date fromDate, Date toDate,
            Long branchId, final short particularType);
}
