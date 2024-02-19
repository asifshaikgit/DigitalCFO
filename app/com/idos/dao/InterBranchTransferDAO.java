package com.idos.dao;

import com.idos.util.IDOSException;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.Date;
import java.util.List;

/**
 * Created by Sunil Namdev on 12-03-2018.
 */
public interface InterBranchTransferDAO extends BaseDAO {

	String IB_TB_BRANCH_JPQL = "select sum(ob), sum(debit), sum(credit), fromB, toB, IDENTITY from (select (sum(t1.DEBIT_AMOUNT)- sum(t1.CREDIT_AMOUNT)) as ob, 0 as debit, 0 as credit, t1.FROM_BRANCH_ID as fromB, t1.TO_BRANCH_ID as toB, t1.TYPE_IDENTIFIER as IDENTITY from TRIALBALANCE_INTER_BRANCH t1 where t1.ORGNIZATION_ID=?1 and t1.TO_BRANCH_ID=?2 and t1.PRESENT_STATUS = 1 and t1.date < ?3 group by t1.FROM_BRANCH_ID, t1.TO_BRANCH_ID  union all select 0 as ob, sum(t2.DEBIT_AMOUNT) as debit, sum(t2.CREDIT_AMOUNT) as credit, t2.FROM_BRANCH_ID as fromB, t2.TO_BRANCH_ID as toB, t2.TYPE_IDENTIFIER as IDENTITY from TRIALBALANCE_INTER_BRANCH t2 where t2.ORGNIZATION_ID=?4 and t2.PRESENT_STATUS = 1 and t2.TO_BRANCH_ID=?5 and t2.date between ?6 and ?7 group by t2.FROM_BRANCH_ID, t2.TO_BRANCH_ID union all select OPENING_BALANCE as ob, 0 as debit, 0 as credit, t1.FROM_BRANCH_ID as fromB, t1.TO_BRANCH_ID as toB, 0 as IDENTITY from INTER_BRANCH_MAPPING t1 where t1.ORGANIZATION_ID=?8 and t1.PRESENT_STATUS = 1 and t1.TO_BRANCH_ID=?9 group by t1.FROM_BRANCH_ID, t1.TO_BRANCH_ID union all (select 0 as ob, 0 as debit, 0 as credit, a.ID as fromB, b.ID as toB, 0 as IDENTITY from BRANCH a, BRANCH b where a.ORGANIZATION_ID=b.ORGANIZATION_ID and a.ID != b.ID and a.ORGANIZATION_ID=?10 and a.PRESENT_STATUS = 1 and b.ID=?11 order by a.NAME)) tbl group by tbl.fromB, tbl.toB";

	String IB_TB_ORG_JPQL = "select sum(ob), sum(debit), sum(credit), fromB, toB, IDENTITY from (select (sum(t1.DEBIT_AMOUNT)- sum(t1.CREDIT_AMOUNT)) as ob, 0 as debit, 0 as credit, t1.FROM_BRANCH_ID as fromB, t1.TO_BRANCH_ID as toB, t1.TYPE_IDENTIFIER as IDENTITY from TRIALBALANCE_INTER_BRANCH t1 where t1.ORGNIZATION_ID=?1 and t1.PRESENT_STATUS = 1 and t1.date < ?2 group by t1.FROM_BRANCH_ID, t1.TO_BRANCH_ID  union all select 0 as ob, sum(t2.DEBIT_AMOUNT) as debit, sum(t2.CREDIT_AMOUNT) as credit, t2.FROM_BRANCH_ID as fromB, t2.TO_BRANCH_ID as toB, t2.TYPE_IDENTIFIER as IDENTITY from TRIALBALANCE_INTER_BRANCH t2 where t2.ORGNIZATION_ID=?3 and t2.PRESENT_STATUS = 1 and t2.date between ?4 and ?5 group by t2.FROM_BRANCH_ID, t2.TO_BRANCH_ID union all select sum(OPENING_BALANCE) as ob, 0 as debit, 0 as credit, t1.FROM_BRANCH_ID as fromB, t1.TO_BRANCH_ID as toB, 0 as IDENTITY from INTER_BRANCH_MAPPING t1 where t1.ORGANIZATION_ID=?6 and t1.PRESENT_STATUS = 1 group by t1.FROM_BRANCH_ID, t1.TO_BRANCH_ID union all (select 0 as ob, 0 as debit, 0 as credit, a.ID as fromB, b.ID as toB, 0 as IDENTITY from BRANCH a, BRANCH b where a.ORGANIZATION_ID=b.ORGANIZATION_ID and a.ID != b.ID and a.ORGANIZATION_ID=?7 and a.PRESENT_STATUS = 1 order by a.NAME)) tbl group by tbl.fromB, tbl.toB";

	String IB_TB_TOTAL_BRANCH_JPQL = "select sum(ob), sum(debit), sum(credit), fromB, toB from (select (sum(t1.DEBIT_AMOUNT)- sum(t1.CREDIT_AMOUNT)) as ob, 0 as debit, 0 as credit, t1.FROM_BRANCH_ID as fromB, t1.TO_BRANCH_ID as toB from TRIALBALANCE_INTER_BRANCH t1 where t1.ORGNIZATION_ID=?1 and t1.TO_BRANCH_ID=?2 and t1.PRESENT_STATUS = 1 and t1.date <?3 group by t1.FROM_BRANCH_ID, t1.TO_BRANCH_ID union all select 0 as ob, sum(t2.DEBIT_AMOUNT) as debit, sum(t2.CREDIT_AMOUNT) as credit, t2.FROM_BRANCH_ID as fromB, t2.TO_BRANCH_ID as toB from TRIALBALANCE_INTER_BRANCH t2 where t2.ORGNIZATION_ID=?4 and t2.PRESENT_STATUS = 1 and t2.TO_BRANCH_ID=?5 and t2.date between ?6 and ?7 group by t2.FROM_BRANCH_ID, t2.TO_BRANCH_ID union all select sum(OPENING_BALANCE) as ob, 0 as debit, 0 as credit, t1.FROM_BRANCH_ID as fromB, t1.TO_BRANCH_ID as toB from INTER_BRANCH_MAPPING t1 where t1.ORGANIZATION_ID=?8 and t1.PRESENT_STATUS = 1 and t1.TO_BRANCH_ID=?9) tbl";

	String IB_TB_TOTAL_ORG_JPQL = "select sum(ob), sum(debit), sum(credit), fromB, toB from (select (sum(t1.DEBIT_AMOUNT)- sum(t1.CREDIT_AMOUNT)) as ob, 0 as debit, 0 as credit, t1.FROM_BRANCH_ID as fromB, t1.TO_BRANCH_ID as toB from TRIALBALANCE_INTER_BRANCH t1 where t1.ORGNIZATION_ID=?1 and t1.PRESENT_STATUS = 1 and t1.date < ?2 group by t1.FROM_BRANCH_ID, t1.TO_BRANCH_ID union all select sum(OPENING_BALANCE) as ob, 0 as debit, 0 as credit, t1.FROM_BRANCH_ID as fromB, t1.TO_BRANCH_ID as toB from INTER_BRANCH_MAPPING t1 where t1.ORGANIZATION_ID=?3 and t1.PRESENT_STATUS = 1 union all select 0 as ob, sum(t2.DEBIT_AMOUNT) as debit, sum(t2.CREDIT_AMOUNT) as credit, t2.FROM_BRANCH_ID as fromB, t2.TO_BRANCH_ID as toB from TRIALBALANCE_INTER_BRANCH t2 where t2.ORGNIZATION_ID=?4 and t2.PRESENT_STATUS = 1 and t2.date between ?5 and ?6 group by t2.FROM_BRANCH_ID, t2.TO_BRANCH_ID) tbl";

	Transaction submitForApproval(Users user, JsonNode json, EntityManager em, EntityTransaction et,
			TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException;

	void getTrialBalanceInterBranchTotal(TrialBalance tb, Users user, String fromDate, String toDate, Long branchId,
			EntityManager em);

	void getTrialBalanceInterBranch(List<TrialBalance> trialBalanceList, Users user, String fromDate, String toDate,
			Long branchId, EntityManager em);

	void createInterBranchMapping(Users user, EntityManager em, Branch branch);

	String ONE_IB_TB_BRANCH_JPQL = "select sum(ob), sum(debit), sum(credit), mappingId, IDENTITY from (select (sum(t1.DEBIT_AMOUNT)- sum(t1.CREDIT_AMOUNT)) as ob, 0 as debit, 0 as credit, t1.INTER_BRANCH_MAPPING_ID as mappingId, t1.TYPE_IDENTIFIER as IDENTITY from TRIALBALANCE_INTER_BRANCH t1 where t1.ORGNIZATION_ID= ?1 and t1.TO_BRANCH_ID= ?2 and t1.INTER_BRANCH_MAPPING_ID = ?3 and t1.PRESENT_STATUS = 1 and t1.date < ?4 union all select 0 as ob, sum(t2.DEBIT_AMOUNT) as debit, sum(t2.CREDIT_AMOUNT) as credit, t2.INTER_BRANCH_MAPPING_ID as mappingId, t2.TYPE_IDENTIFIER as IDENTITY from TRIALBALANCE_INTER_BRANCH t2 where t2.ORGNIZATION_ID = ?5 and t2.TO_BRANCH_ID= ?6 and t2.INTER_BRANCH_MAPPING_ID = ?7 and t2.PRESENT_STATUS = 1 and t2.date between ?8 and ?9 union all select OPENING_BALANCE as ob, 0 as debit, 0 as credit, t1.ID as mappingId, 0 as IDENTITY from INTER_BRANCH_MAPPING t1 where t1.ORGANIZATION_ID= ?10 and t1.TO_BRANCH_ID= ?11 and t1.ID = ?12 and t1.PRESENT_STATUS = 1) tbl group by tbl.mappingId";

	String ONE_IB_TB_ORG_JPQL = "select sum(ob), sum(debit), sum(credit), mappingId, IDENTITY from (select (sum(t1.DEBIT_AMOUNT)- sum(t1.CREDIT_AMOUNT)) as ob, 0 as debit, 0 as credit, t1.INTER_BRANCH_MAPPING_ID as mappingId, t1.TYPE_IDENTIFIER as IDENTITY from TRIALBALANCE_INTER_BRANCH t1 where t1.ORGNIZATION_ID= ?1 and t1.INTER_BRANCH_MAPPING_ID = ?2 and t1.PRESENT_STATUS = 1 and t1.date < ?3 group by t1.FROM_BRANCH_ID, t1.TO_BRANCH_ID union all select 0 as ob, sum(t2.DEBIT_AMOUNT) as debit, sum(t2.CREDIT_AMOUNT) as credit, t2.INTER_BRANCH_MAPPING_ID as mappingId, t2.TYPE_IDENTIFIER as IDENTITY from TRIALBALANCE_INTER_BRANCH t2 where t2.ORGNIZATION_ID= ?4 and t2.INTER_BRANCH_MAPPING_ID = ?5 and t2.PRESENT_STATUS = 1 and t2.date between ?6 and ?7 union all select sum(OPENING_BALANCE) as ob, 0 as debit, 0 as credit, t1.ID as mappingId, 0 as IDENTITY from INTER_BRANCH_MAPPING t1 where t1.ORGANIZATION_ID= ?8 and t1.ID = ?9 and t1.PRESENT_STATUS = 1) tbl group by tbl.mappingId";

	TrialBalance getTrialBalance4SpecificInterBranch(Users user, EntityManager em, Date fromDate, Date toDate,
			Long branchId, InterBranchMapping interBranchMapping);
}
