/**
 * 
 */
package com.idos.dao;

import java.util.Date;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.idos.util.IDOSException;
import model.Transaction;
import com.fasterxml.jackson.databind.node.ArrayNode;

import model.IdosProvisionJournalEntry;
import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Sunil K Namdev created on 19.03.2019
 *
 */
public interface TrialBalanceLedgerDAO extends BaseDAO {

	String TB_LDGR_COA_ORG_JPQL = "select t2.TRANSACTION_ID, t2.TRANSACTION_PURPOSE, t2.CREDIT_AMOUNT, t2.DEBIT_AMOUNT, t1.IDENT_DATA_VALID, t1.ID, t1.NAME from SPECIFICS t1 left join TRIALBALANCE_COAITEMS t2 on t1.ID = t2.TRANSACTION_SPECIFICS where t1.ORGANIZATION_ID= ?1 and (t1.ID = ?2 or t1.account_code_hirarchy like ?3) and t1.PRESENT_STATUS=1 and t2.DATE between ?4 and ?5 order by t1.ID, t2.DATE";

	String TB_LDGR_COA_BRNCH_JPQL = "select t2.TRANSACTION_ID, t2.TRANSACTION_PURPOSE, t2.CREDIT_AMOUNT, t2.DEBIT_AMOUNT, t1.IDENT_DATA_VALID, t1.ID, t1.NAME from SPECIFICS t1 left join TRIALBALANCE_COAITEMS t2 on t1.ID = t2.TRANSACTION_SPECIFICS where t1.ORGANIZATION_ID = ?1 and t2.BRANCH_ID = ?2 and (t1.ID = ?3 or t1.account_code_hirarchy like ?4) and t1.PRESENT_STATUS=1 and t2.DATE between ?5 and ?6 order by t1.ID, t2.DATE";

	// String TB_LDGR_COA_BRNCH_JPQL = "select obj from TrialBalanceCOAItems obj
	// where obj.organization.id=?x and obj.branch.id=?x and
	// obj.transactionSpecifics.id = ?x and obj.presentStatus=1 and obj.date between
	// ?x and ?x order by obj.date";

	String TB_LDGR_COA_ORG_JPQL2 = "select obj from TrialBalanceCOAItems obj where obj.organization.id=?1 and obj.transactionSpecifics.id = ?2 and obj.presentStatus=1 and obj.date between ?3 and ?4 order by obj.date";
	String TB_LDGR_COA_BRNCH_JPQL2 = "select obj from TrialBalanceCOAItems obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.transactionSpecifics.id = ?3 and obj.presentStatus=1 and obj.date  between ?4 and ?5 order by obj.date";

	// String TB_LDGR_VENDCUST_ORG_JPQL = "select obj from
	// TrialBalanceCustomerVendor obj where obj.organization.id=?x and obj.vendor.id
	// = ?x and obj.vendorType=?x and obj.presentStatus=1 and obj.date between ?x
	// and ?x order by obj.date";
	// String TB_LDGR_VENDCUST_BRNCH_JPQL = "select obj from
	// TrialBalanceCustomerVendor obj where obj.organization.id=?x and
	// obj.branch.id=?x and obj.vendor.id = ?x and obj.vendorType=?x and
	// obj.presentStatus=1 and obj.date between ?x and ?x order by obj.date";

	String CASH_ORG_JPQL = "select obj from TrialBalanceBranchCash obj where obj.organization.id= ?1 and obj.branchDepositBoxKey.id = ?2 and cashType= ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 order by obj.branchDepositBoxKey, obj.date";
	String CASH_BRANCH_JPQL = "select obj from TrialBalanceBranchCash obj where obj.organization.id= ?1 and obj.branch.id= ?2 and obj.branchDepositBoxKey.id = ?3 and cashType= ?4 and obj.presentStatus=1 and obj.date between ?5 and ?6 order by obj.branchDepositBoxKey, obj.date";
	String ALL_CASH_ORG_JPQL = "select obj from TrialBalanceBranchCash obj where obj.organization.id= ?1 and cashType= ?2 and obj.presentStatus=1 and obj.date between ?3 and ?4 order by obj.branchDepositBoxKey, obj.date";
	String ALL_CASH_BRANCH_JPQL = "select obj from TrialBalanceBranchCash obj where obj.organization.id= ?1 and obj.branch.id= ?2 and cashType= ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 order by obj.branchDepositBoxKey, obj.date";

	String BANK_ORG_JPQL = "select obj from TrialBalanceBranchBank obj where obj.organization.id= ?1 and obj.branchBankAccounts.id = ?2 and obj.presentStatus=1 and obj.date between ?3 and ?4 order by obj.branchBankAccounts, obj.date";
	String BANK_BRANCH_JPQL = "select obj from TrialBalanceBranchBank obj where obj.organization.id= ?1 and obj.branch.id= ?2 and obj.branchBankAccounts.id = ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 order by obj.branchBankAccounts, obj.date";
	String ALL_BANK_ORG_JPQL = "select obj from TrialBalanceBranchBank obj where obj.organization.id= ?1 and obj.presentStatus=1 and obj.date between ?2 and ?3 order by obj.branchBankAccounts, obj.date";
	String ALL_BANK_BRANCH_JPQL = "select obj from TrialBalanceBranchBank obj where obj.organization.id= ?1 and obj.branch.id= ?2 and obj.presentStatus=1 and obj.date between ?3 and ?4 order by obj.branchBankAccounts, obj.date";

	String CUSTVEN_ADV_ORG_JPQL = "select obj from TrialBalanceVendorAdvance obj where obj.organization.id= ?1 and obj.vendor.id = ?2 and obj.vendorType= ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 order by obj.vendor, obj.date";
	String CUSTVEN_ADV_BRANCH_JPQL = "select obj from TrialBalanceVendorAdvance obj where obj.organization.id= ?1 and obj.branch.id= ?2 and obj.vendor.id = ?3 and obj.vendorType= ?4 and obj.presentStatus=1 and obj.date between ?5 and ?6 order by obj.vendor, obj.date";
	String ALL_CUSTVEN_ADV_ORG_JPQL = "select obj from TrialBalanceVendorAdvance obj where obj.organization.id= ?1 and obj.vendorType= ?2 and obj.presentStatus=1 and obj.date between ?3 and ?4 order by obj.vendor, obj.date";
	String ALL_CUSTVEN_ADV_BRANCH_JPQL = "select obj from TrialBalanceVendorAdvance obj where obj.organization.id= ?1 and obj.branch.id= ?2 and obj.vendorType= ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 order by obj.vendor, obj.date";

	String CUSTVEN_ORG_JPQL = "select obj from TrialBalanceCustomerVendor obj where obj.organization.id= ?1 and obj.vendor.id = ?2 and obj.vendorType= ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 order by obj.vendor, obj.date";
	String CUSTVEN_BRANCH_JPQL = "select obj from TrialBalanceCustomerVendor obj where obj.organization.id= ?1 and obj.branch.id= ?2 and obj.vendor.id = ?3 and obj.vendorType= ?4 and obj.presentStatus=1 and obj.date between ?5 and ?6 order by obj.vendor, obj.date";
	String ALL_CUSTVEN_ORG_JPQL = "select obj from TrialBalanceCustomerVendor obj where obj.organization.id= ?1 and obj.vendorType= ?2 and obj.presentStatus=1 and obj.date between ?3 and ?4 order by obj.vendor, obj.date";
	String ALL_CUSTVEN_BRANCH_JPQL = "select obj from TrialBalanceCustomerVendor obj where obj.organization.id= ?1 and obj.branch.id= ?2 and obj.vendorType= ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 order by obj.vendor, obj.date";

	String INTERBRANCH_ORG_JPQL = "select obj from TrialBalanceInterBranch obj where obj.organization.id= ?1 and obj.fromBranch.id = ?2 and obj.toBranch.id = ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 order by obj.interBranchMapping.id, obj.date";
	String ALL_INTERBRANCH_ORG_JPQL = "select obj from TrialBalanceInterBranch obj where obj.organization.id= ?1 and obj.presentStatus=1 and obj.date between ?2 and ?3 order by obj.interBranchMapping.id, obj.date";
	String ALL_INTERBRANCH_BRANCH_JPQL = "select obj from TrialBalanceInterBranch obj where obj.organization.id= ?1 and (obj.fromBranch.id = ?2 or obj.toBranch.id = ?3) and obj.presentStatus=1 and obj.date between ?3 and ?4 order by obj.interBranchMapping.id, obj.date";

	String TAX_ORG_JPQL = "select obj from TrialBalanceTaxes obj where obj.organization.id=?1 and obj.presentStatus=1 and (obj.branchTaxes.id = ?2 or obj.branchTaxes is null) and obj.taxType= ?3 and obj.date between ?4 and ?5 order by obj.branchTaxes.id, obj.date";
	String TAX_BRANCH_JPQL = "select obj from TrialBalanceTaxes obj where obj.organization.id=?1 and obj.branch.id= ?2 and obj.presentStatus=1 and (obj.branchTaxes.id = ?3 or obj.branchTaxes is null) and obj.taxType = ?4 and obj.date between ?5 and ?6 order by obj.branchTaxes.id, obj.date";
	String ALL_TAX_ORG_JPQL = "select obj from TrialBalanceTaxes obj where obj.organization.id=?1 and obj.taxType= ?2 and obj.presentStatus=1 and obj.date between ?3 and ?4 order by obj.branchTaxes.id, obj.date";
	String ALL_TAX_BRANCH_JPQL = "select obj from TrialBalanceTaxes obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.taxType = ?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 order by obj.branchTaxes.id, obj.date";

	String ALL_ORG_EMP_ADV_JPQL = "select obj from TrialBalanceUserAdvance obj where obj.organization.id = ?1 and obj.date between ?2 and ?3 order by obj.user.id, obj.date";
	String ORG_EMP_ADV_JPQL = "select obj from TrialBalanceUserAdvance obj where obj.organization.id = ?1 and obj.user.id = ?2 and obj.date between ?3 and ?4 order by obj.user.id, obj.date";
	String ALL_BRANCH_EMP_ADV_JPQL = "select obj from TrialBalanceUserAdvance obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.date between ?3 and ?4 order by obj.user.id, obj.date";
	String BRANCH_EMP_ADV_JPQL = "select obj from TrialBalanceUserAdvance obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.user.id = ?3 and obj.date between ?4 and ?5 order by obj.user.id, obj.date";

	String ALL_ORG_EMP_CLAIM_JPQL = "select obj from TrialBalanceUserClaims obj where obj.organization.id = ?1 and obj.date between ?2 and ?3 order by obj.user.id, obj.date";
	String ORG_EMP_CLAIM_JPQL = "select obj from TrialBalanceUserClaims obj where obj.organization.id = ?1 and obj.user.id = ?2 and obj.date between ?3 and ?4 order by obj.user.id, obj.date";
	String ALL_BRANCH_EMP_CLAIM_JPQL = "select obj from TrialBalanceUserClaims obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.date between ?3 and ?4 order by obj.user.id, obj.date";
	String BRANCH_EMP_CLAIM_JPQL = "select obj from TrialBalanceUserClaims obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.user.id = ?3 and obj.date between ?4 and ?5 order by obj.user.id, obj.date";

	String COA_ORG_TXN_JPQL = "select obj from Specifics obj where obj.organization.id = ?1 and (obj.id = ?2 or obj.accountCodeHirarchy like ?3) and obj.presentStatus = 1 order by obj.id";

	void getTransactionsForIncomeExpenseCOAItems(EntityManager entityManager, Users user, String headType, Long headId,
			Long headId2, Date fromDate, Date toDate, Long branchid, ArrayNode itemTransData, int ledgerType)
			throws IDOSException;

	ObjectNode fetchTxnDetailsCOAItems(EntityManager em, Users user, Long transactionID, Double debitAmount,
			Double creditAmount, Long transPurposeID, Specifics txnSpecific);

	ObjectNode fetchClaimTransactionDetails(EntityManager entityManager, Users user, Long transactionID,
			Double debitAmount, Double creditAmount, Long transPurposeID);

	ObjectNode fetchProvisionTransactionDetails(EntityManager entityManager, Users user, Long transactionID,
			Double debitAmount, Double creditAmount, Long transPurposeID, Long headId, int rowNoOfItem);

	void getProvisionJournalEntryHeads(EntityManager entityManager, IdosProvisionJournalEntry provisionJournalEntry,
			String headType, Long headID, StringBuilder itemName, StringBuilder creditItems,
			StringBuilder debitProjectName, StringBuilder creditProjectName, int rowNoOfItem);

	void getTrialBalanceCashTrans(EntityManager em, Users user, Long branchId, Long headId, Date fromDate, Date toDate,
			int cashType, ArrayNode itemTransData, int mappId) throws IDOSException;

	void getTrialBalanceBankTrans(EntityManager em, Users user, Long branchId, Long headId, Date fromDate, Date toDate,
			ArrayNode itemTransData, int mappId) throws IDOSException;

	void getTrialBalanceCustomerVendorAdvTrans(EntityManager em, Users user, Long branchId, Long headId, Date fromDate,
			Date toDate, short vendorOrCustomer, ArrayNode itemTransData, int mappId);

	void getTrialBalanceVendorCustomerTrans(EntityManager em, Users user, Long branchId, Long headId, Date fromDate,
			Date toDate, int vendorOrCustomer, ArrayNode itemTransData, int mappId);

	void getTrialBalanceTaxTrans(EntityManager em, Users user, long branchId, Long headId, Date fromDate, Date toDate,
			int taxType, ArrayNode itemTransData, int mappId);

	void getEmployeeAdvance(EntityManager em, Users user, long branchId, Long headId, Date fromDate, Date toDate,
			String headType, ArrayNode itemTransData, int mappId);

	void getEmployeeClaimPayable(EntityManager em, Users user, long branchId, Long headId, Date fromDate, Date toDate,
			String headType, ArrayNode itemTransData, int mappId);

	ObjectNode fetchTransactionDetails(EntityManager em, Users user, Long transactionID, Double debitAmount,
			Double creditAmount, Long transPurposeID) throws IDOSException;

	void getTrialBalanceInterBranchTxn(EntityManager em, Users user, Long branchId, Long headId, Long headid2,
			Date fromDate, Date toDate, ArrayNode itemTransData, String headType, int mappId) throws IDOSException;

	void fetchTxnGSTDetails(EntityManager em, Transaction txn, Users user, ObjectNode row);

	void getMappedItemsTransactionDetails(int ledgerType, String headType, EntityManager em, Users user, Long branchid,
			long headId, long headId2, Date fromDate, Date toDate, ArrayNode itemTransData, int mappingID)
			throws IDOSException;

	void getTransactionsForCoaChildItems(EntityManager em, Users user, String headType, Long headId, Long headId2,
			Date fromDate, Date toDate, Long branchid, ArrayNode itemTransData, int ledgerType) throws IDOSException;
}
