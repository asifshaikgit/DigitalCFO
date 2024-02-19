package com.idos.dao.trialbalance;

import com.idos.dao.BaseDAO;
import model.Specifics;
import model.TrialBalance;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.Date;

/**
 * @author Sunil K Namdev created on 26.11.2019
 *
 */
public interface TrialBalanceTdsDAO extends BaseDAO {

    String TDS_ORG_SQL8 = "select sum(obalance), sum(credit), sum(debit), sid from( select OPENING_BALANCE as obalance, 0 as credit, 0 as debit, ID as sid from SPECIFICS where ORGANIZATION_ID = ?1 and ID = ?2 and IDENT_DATA_VALID =?3 union all select (sum(DEBIT_AMOUNT) - sum(CREDIT_AMOUNT)) as obalance, 0 as credit, 0 as debit, t1.BRANCH_TAXESID as sid from TRIALBALANCE_TAXES t1, SPECIFICS t2 where BRANCH_ORGNIZATION_ID=ORGANIZATION_ID and BRANCH_ORGNIZATION_ID=?4 and t1.BRANCH_TAXESID = ?5 and t1.BRANCH_TAXESID = t2.ID and t2.IDENT_DATA_VALID=?6 and tax_type=?7 and DATE < ?8 union all select 0 as obalance, sum(CREDIT_AMOUNT) as credit, sum(DEBIT_AMOUNT) as debit, t1.BRANCH_TAXESID as sid from TRIALBALANCE_TAXES t1, SPECIFICS t2 where BRANCH_ORGNIZATION_ID=ORGANIZATION_ID and BRANCH_ORGNIZATION_ID=?9 and t2.ID = ?10 and t1.BRANCH_TAXESID = t2.ID and t2.IDENT_DATA_VALID=?11 and tax_type=?12 and DATE between ?13 and ?14) as tbl group by sid";

    String TDS_BRANCH_SQL8 = "select sum(obalance), sum(credit), sum(debit), sid from( select t2.OPENING_BALANCE as obalance, 0 as credit, 0 as debit, t1.ID as sid from SPECIFICS t1, BRANCH_has_SPECIFICS t2 where t1.ORGANIZATION_ID=t2.BRANCH_ORGANIZATION_ID and t1.ORGANIZATION_ID = ?1 and t2.BRANCH_ID=?2 and t1.ID=t2.SPECIFICS_ID and t1.ID = ?3 and t1.IDENT_DATA_VALID =?4 union all select (sum(DEBIT_AMOUNT) - sum(CREDIT_AMOUNT)) as obalance, 0 as credit, 0 as debit, t1.BRANCH_TAXESID as sid from TRIALBALANCE_TAXES t1, SPECIFICS t2 where BRANCH_ORGNIZATION_ID=ORGANIZATION_ID and BRANCH_ORGNIZATION_ID=?5 and t1.BRANCH_ID=?6 and t2.ID = ?7 and t1.BRANCH_TAXESID = t2.ID and t2.IDENT_DATA_VALID=?8 and tax_type=?9 and DATE < ?10 union all select 0 as obalance, sum(CREDIT_AMOUNT) as credit, sum(DEBIT_AMOUNT) as debit, t1.BRANCH_TAXESID as sid from TRIALBALANCE_TAXES t1, SPECIFICS t2 where BRANCH_ORGNIZATION_ID=ORGANIZATION_ID and BRANCH_ORGNIZATION_ID=?11 and t1.BRANCH_ID=?12 and t2.ID = ?13 and t1.BRANCH_TAXESID = t2.ID and t2.IDENT_DATA_VALID=?14 and tax_type=?15 and DATE between ?16 and ?17) as tbl group by sid";

    TrialBalance getTrialBalanceForTds(Users user, EntityManager em, Date fromDate, Date toDate, Long branchId,
            Specifics specific, final int taxType, final short particularType, final int mappedID);

    String TDS_ORG_SQL9 = "select sum(obalance), sum(credit), sum(debit), sid from( select OPENING_BALANCE as obalance, 0 as credit, 0 as debit, ID as sid from SPECIFICS where ORGANIZATION_ID = ?1 and ID = ?2 and IDENT_DATA_VALID =?3 union all select (sum(CREDIT_AMOUNT)-sum(DEBIT_AMOUNT)) as obalance, 0 as credit, 0 as debit, t1.BRANCH_TAXESID as sid from TRIALBALANCE_TAXES t1, SPECIFICS t2 where BRANCH_ORGNIZATION_ID=ORGANIZATION_ID and BRANCH_ORGNIZATION_ID=?4 and t1.BRANCH_TAXESID = ?5 and t1.BRANCH_TAXESID = t2.ID and t2.IDENT_DATA_VALID=?6 and tax_type=?7 and DATE < ?8 union all select 0 as obalance, sum(CREDIT_AMOUNT) as credit, sum(DEBIT_AMOUNT) as debit, t1.BRANCH_TAXESID as sid from TRIALBALANCE_TAXES t1, SPECIFICS t2 where BRANCH_ORGNIZATION_ID=ORGANIZATION_ID and BRANCH_ORGNIZATION_ID=?9 and t2.ID = ?10 and t1.BRANCH_TAXESID = t2.ID and t2.IDENT_DATA_VALID=?11 and tax_type=?12 and DATE between ?13 and ?14) as tbl group by sid";

    String TDS_BRANCH_SQL9 = "select sum(obalance), sum(credit), sum(debit), sid from( select t2.OPENING_BALANCE as obalance, 0 as credit, 0 as debit, t1.ID as sid from SPECIFICS t1, BRANCH_has_SPECIFICS t2 where t1.ORGANIZATION_ID=t2.BRANCH_ORGANIZATION_ID and t1.ORGANIZATION_ID = ?1 and t2.BRANCH_ID=?2 and t1.ID=t2.SPECIFICS_ID and t1.ID = ?3 and t1.IDENT_DATA_VALID =?4 union all select (sum(CREDIT_AMOUNT)-sum(DEBIT_AMOUNT)) as obalance, 0 as credit, 0 as debit, t1.BRANCH_TAXESID as sid from TRIALBALANCE_TAXES t1, SPECIFICS t2 where BRANCH_ORGNIZATION_ID=ORGANIZATION_ID and BRANCH_ORGNIZATION_ID=?5 and t1.BRANCH_ID=?6 and t2.ID = ?7 and t1.BRANCH_TAXESID = t2.ID and t2.IDENT_DATA_VALID=?8 and tax_type=?9 and DATE < ?10 union all select 0 as obalance, sum(CREDIT_AMOUNT) as credit, sum(DEBIT_AMOUNT) as debit, t1.BRANCH_TAXESID as sid from TRIALBALANCE_TAXES t1, SPECIFICS t2 where BRANCH_ORGNIZATION_ID=ORGANIZATION_ID and BRANCH_ORGNIZATION_ID=?11 and t1.BRANCH_ID=?12 and t2.ID = ?13 and t1.BRANCH_TAXESID = t2.ID and t2.IDENT_DATA_VALID=?14 and tax_type=?15 and DATE between ?16 and ?17) as tbl group by sid";
}
