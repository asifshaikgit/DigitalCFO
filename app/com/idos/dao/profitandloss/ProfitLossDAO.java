package com.idos.dao.profitandloss;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.dao.BaseDAO;
import com.idos.util.IDOSException;
import model.profitloss.ProfitLossBean;
import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ser;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Users;

/**
 * @author Sunil Namdev
 *
 */
public interface ProfitLossDAO extends BaseDAO {

	public ProfitLossBean populateProfitLossBean(Users user, EntityManager entityManager, String currPLFromDate,
			String currPLToDate, String prevPLFromDate, String prevPLToDate) throws IDOSException;

	public ProfitLossBean displayProfitLoss(ObjectNode result, JsonNode json, Users user, EntityManager entityManager)
			throws IDOSException;

	public ObjectNode saveUpdateInventoryData(JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction);

	public ObjectNode displayInvetory(JsonNode json, Users user, EntityManager entityManager);

	public ProfitLossBean getPnLWithoutOpeningBalanceOfItem(Users user, EntityManager entityManager,
			String currPLFromDate, String currPLToDate, String prevPLFromDate, String prevPLToDate)
			throws IDOSException;

	StringBuilder cJpql = new StringBuilder(
			"SELECT PLBSHEAD, sum(oBalance + cIncome), pIncome, sum(oBalance + cExpense), pExpense FROM ")
			.append("( SELECT t1.PLBSHEAD, sum(ifnull(t2.CREDIT_AMOUNT,0) - ifnull(t2.DEBIT_AMOUNT,0)) as cIncome, 0 as pIncome, sum(ifnull(t2.DEBIT_AMOUNT,0)-ifnull(t2.CREDIT_AMOUNT,0)) as cExpense, 0 as pExpense, 0 as oBalance ")
			.append(" FROM PLBSCOAMAP t1 left join TRIALBALANCE_COAITEMS t2 ON t2.TRANSACTION_SPECIFICS = REPLACE(t1.coa_id,'item', '') AND (t2.date >= ?1 and t2.date <= ?2) ")
			.append(" where t1.ORGANIZATION_ID = ?3 and t2.BRANCH_ORGNIZATION_ID = t1.ORGANIZATION_ID AND (t1.PLBSHEAD >=100 and t1.PLBSHEAD < 300) GROUP by t1.PLBSHEAD, pIncome, pExpense ")
			.append(" UNION ALL ")
			.append(" SELECT t1.PLBSHEAD, 0 as cIncome, 0 as pIncome, 0 as cExpense, 0 as pExpense, sum(t3.OPENING_BALANCE) as oBalance from PLBSCOAMAP t1, SPECIFICS t3 ")
			.append(" WHERE t1.ORGANIZATION_ID = ?4 and t3.ORGANIZATION_ID = t1.ORGANIZATION_ID and t3.ID = REPLACE(t1.coa_id,'item', '') ")
			.append(" and (t1.PLBSHEAD >=100 and t1.PLBSHEAD < 300) GROUP by t1.PLBSHEAD, pIncome, pExpense )")
			.append(" as t5 group by PLBSHEAD, pIncome, pExpense ")
			.append(" UNION ALL ")
			.append("SELECT PLBSHEAD, sum(oBalance + cIncome), pIncome, sum(oBalance + cExpense), pExpense FROM ")
			.append("(SELECT t1.PLBSHEAD, sum(ifnull(t2.CREDIT_AMOUNT,0) - ifnull(t2.DEBIT_AMOUNT,0)) as cIncome, 0 as pIncome, sum(ifnull(t2.DEBIT_AMOUNT,0)-ifnull(t2.CREDIT_AMOUNT,0)) as cExpense, 0 as pExpense, 0 as oBalance ")
			.append(" FROM PLBSCOAMAP t1 left join TRIALBALANCE_PAYROLL_ITEM t2 ON t2.TRANSACTION_SPECIFICS = REPLACE(t1.coa_id,'pexp', '') AND (t2.DATE >= ?5 and t2.DATE <= ?6) ")
			.append(" WHERE t1.ORGANIZATION_ID = ?7 and t2.ORGANIZATION_ID = t1.ORGANIZATION_ID and (t1.PLBSHEAD >=100 and t1.PLBSHEAD < 300) GROUP by t1.PLBSHEAD, pIncome, pExpense ")
			.append(" UNION ALL ")
			.append(" select t1.PLBSHEAD, 0 as cIncome, 0 as pIncome, 0 as cExpense, 0 as pExpense, sum(t3.OPENING_BALANCE) as oBalance from PLBSCOAMAP t1, PAYROLL_SETUP t3 ")
			.append(" WHERE t1.ORGANIZATION_ID = ?8 and t3.ORGANIZATION_ID = t1.ORGANIZATION_ID and t3.ID = REPLACE(t1.coa_id,'pexp', '') ")
			.append(" and  (t1.PLBSHEAD >=100 and t1.PLBSHEAD < 300) GROUP by t1.PLBSHEAD, pIncome, pExpense ) as t5 group by PLBSHEAD, pIncome, pExpense ");

	String curJpql = cJpql.toString();

	StringBuilder pJpql = new StringBuilder(
			"SELECT PLBSHEAD, cIncome, sum(oBalance + pIncome), cExpense, sum(oBalance + pExpense) FROM ")
			.append("( SELECT t1.PLBSHEAD, 0 as cIncome, sum(ifnull(t2.CREDIT_AMOUNT,0) - ifnull(t2.DEBIT_AMOUNT,0)) as pIncome, 0 as cExpense, sum(ifnull(t2.DEBIT_AMOUNT,0)-ifnull(t2.CREDIT_AMOUNT,0)) as pExpense, 0 as oBalance ")
			.append(" FROM PLBSCOAMAP t1 left join TRIALBALANCE_COAITEMS t2 ON t2.TRANSACTION_SPECIFICS = REPLACE(t1.coa_id,'item', '') AND (t2.date >= ?1 and t2.date <= ?2) ")
			.append(" where t1.ORGANIZATION_ID = ?3  and t2.BRANCH_ORGNIZATION_ID = t1.ORGANIZATION_ID AND (t1.PLBSHEAD >=100 and t1.PLBSHEAD < 300) GROUP by t1.PLBSHEAD, cIncome, cExpense ")
			.append(" UNION ALL ")
			.append(" SELECT t1.PLBSHEAD, 0 as cIncome, 0 as pIncome, 0 as cExpense, 0 as oBalance, sum(t3.OPENING_BALANCE) as oBalance from PLBSCOAMAP t1, SPECIFICS t3 ")
			.append(" WHERE t1.ORGANIZATION_ID = ?4  and t3.ORGANIZATION_ID = t1.ORGANIZATION_ID and t3.ID = REPLACE(t1.coa_id,'item', '') ")
			.append(" and (t1.PLBSHEAD >=100 and t1.PLBSHEAD < 300) GROUP by t1.PLBSHEAD, cIncome, cExpense )")
			.append(" as t5 group by PLBSHEAD,cIncome, cExpense ")
			.append(" UNION ALL ")
			.append("SELECT PLBSHEAD, cIncome, sum(oBalance + pIncome), cExpense, sum(oBalance + pExpense) FROM ")
			.append("(SELECT t1.PLBSHEAD, 0 as cIncome, sum(ifnull(t2.CREDIT_AMOUNT,0) - ifnull(t2.DEBIT_AMOUNT,0)) as pIncome, 0 as cExpense, sum(ifnull(t2.DEBIT_AMOUNT,0)-ifnull(t2.CREDIT_AMOUNT,0)) as pExpense, 0 as oBalance ")
			.append(" FROM PLBSCOAMAP t1 left join TRIALBALANCE_PAYROLL_ITEM t2 ON t2.TRANSACTION_SPECIFICS = REPLACE(t1.coa_id,'pexp', '') AND (t2.DATE >= ?5 and t2.DATE <= ?6) ")
			.append(" WHERE t1.ORGANIZATION_ID = ?7 and t2.ORGANIZATION_ID = t1.ORGANIZATION_ID and (t1.PLBSHEAD >=100 and t1.PLBSHEAD < 300) GROUP by t1.PLBSHEAD, cIncome, cExpense ")
			.append(" UNION ALL ")
			.append(" select t1.PLBSHEAD, 0 as cIncome, 0 as pIncome, 0 as cExpense, 0 as oBalance, sum(t3.OPENING_BALANCE) as oBalance from PLBSCOAMAP t1, PAYROLL_SETUP t3 ")
			.append(" WHERE t1.ORGANIZATION_ID = ?8 and t3.ORGANIZATION_ID = t1.ORGANIZATION_ID and t3.ID = REPLACE(t1.coa_id,'pexp', '') ")
			.append(" and  (t1.PLBSHEAD >=100 and t1.PLBSHEAD < 300) GROUP by t1.PLBSHEAD, cIncome, cExpense ) as t5 group by PLBSHEAD, cIncome, cExpense ");

	String prevJpql = pJpql.toString();

	StringBuilder tmpSql = new StringBuilder(
			"SELECT t1.PLBSHEAD, sum(ifnull(t2.CREDIT_AMOUNT,0) - ifnull(t2.DEBIT_AMOUNT,0)) as cIncome, 0 as pIncome, sum(ifnull(t2.DEBIT_AMOUNT,0)-ifnull(t2.CREDIT_AMOUNT,0)) as cExpense, 0 as pExpense ")
			.append(" FROM PLBSCOAMAP t1 left join TRIALBALANCE_COAITEMS t2 ON t2.TRANSACTION_SPECIFICS = REPLACE(t1.coa_id,'item', '') AND (t2.date >= ?1 and t2.date <= ?2) ")
			.append(" where t1.ORGANIZATION_ID = ?3 and t2.BRANCH_ORGNIZATION_ID = t1.ORGANIZATION_ID AND (t1.PLBSHEAD >=100 and t1.PLBSHEAD < 300) GROUP by t1.PLBSHEAD ")
			.append(" UNION ALL ")
			.append("SELECT t1.PLBSHEAD, sum(ifnull(t2.CREDIT_AMOUNT,0) - ifnull(t2.DEBIT_AMOUNT,0)) as cIncome, 0 as pIncome, sum(ifnull(t2.DEBIT_AMOUNT,0)-ifnull(t2.CREDIT_AMOUNT,0)) as cExpense, 0 as pExpense ")
			.append(" FROM PLBSCOAMAP t1 left join TRIALBALANCE_PAYROLL_ITEM t2 ON t2.TRANSACTION_SPECIFICS = REPLACE(t1.coa_id,'pexp', '') AND (t2.DATE >= ?4 and t2.DATE <= ?5) ")
			.append(" WHERE t1.ORGANIZATION_ID = ?6 and t2.ORGANIZATION_ID = t1.ORGANIZATION_ID and (t1.PLBSHEAD >=100 and t1.PLBSHEAD < 300) GROUP by t1.PLBSHEAD ");

	String PROFIT_LOSS_JPQL_WITHOUT_OB = tmpSql.toString();
}
