package com.idos.dao;

import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Branch;
import model.Specifics;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ChartOfAccountsDAO extends BaseDAO {
	String SQL_TREE_DEPTH = "select max(depth) from ( SELECT  (LENGTH(account_code_hirarchy) - LENGTH(REPLACE(account_code_hirarchy, '/', ''))) as depth from SPECIFICS where ORGANIZATION_ID = ?1 and PRESENT_STATUS = 1) as t1";

	String SQL_SPECIFICS = "SELECT t1 FROM Specifics t1 LEFT JOIN Specifics t2 ON t1.id = t2.parentSpecifics WHERE t1.organization = ?1 and t1.presentStatus=1 and CAST(t1.accountCode AS string) like ?2 and t2.id IS NULL order by t1.id";

	public String MAPPING_LEAFNODE_JPQL = "select obj from Specifics obj where obj.organization.id= ?1 and obj.parentSpecifics.id = ?2 and obj.presentStatus=1";

	String BY_BRANCH_SPECIFICS_JPAQ = "SELECT t2 FROM BranchSpecifics t1, Specifics t2 where t1.organization.id = ?1 and t1.branch.id = ?2 and t1.specifics.id = t2.id and t1.presentStatus=1 and t2.accountCodeHirarchy like ?3 ORDER BY t2.name";

	String USER_BRANCH_RULE_INCEXP_ITEMS_JPAQ = "SELECT t3 FROM UserRightInBranch t1, UserRightSpecifics t2, Specifics t3 where t1.organization.id = t3.organization.id and t1.organization.id = ?1 and t1.branch.id = ?2 and t2.specifics.id = t3.id and t1.userRights.id=t2.userRights.id and t1.user.id = t2.user.id and t2.userRights.id=?3 and t2.user.id=?4 and t3.presentStatus=1 and ( t3.accountCodeHirarchy like ?5 or t3.accountCodeHirarchy like ?6)";

	String USER_BRANCH_RULE_INC_OR_EXP_ITEMS_JPAQ = "SELECT t3 FROM UserRightInBranch t1, UserRightSpecifics t2, Specifics t3 where t1.organization.id = t3.organization.id and t1.organization.id = ?1 and t1.branch.id = ?2 and t2.specifics.id = t3.id and t1.userRights.id=t2.userRights.id and t1.user.id = t2.user.id and t2.userRights.id=?3 and t2.user.id=?4 and t3.presentStatus=1 and t3.accountCodeHirarchy like ?5";

	String SRCH_NAME_57_JPQL = "select t1.fromBranch.id, t1.toBranch.id, t1.fromBranch.name, t1.toBranch.name, t1.id from InterBranchMapping t1 where t1.organization.id=?1 and t1.presentStatus=1 and lower(t1.fromBranch.name) like ?2 order by t1.fromBranch.name";
	// String VEND_CUST_ORG_HQL = "select a from Vendor a where a.organization.id=?x
	// and a.presentStatus=1 and a.type in (?x,?x)";
	String VEND_CUST_BRANCH_HQL = "select a.vendor from BranchVendors a where a.organization.id=?1 and a.branch.id = ?2 and a.vendor.presentStatus=1 and a.vendor.type in (?3,?4)";

	String PAYROLL_TEMS_JPQL = "select obj from PayrollSetup obj where obj.organization.id=?1 and obj.presentStatus=1 and obj.payrollType=?2";

	String COA_EXPENSE_UNITS = "select distinct obj.expenseUnitsMeasure from Specifics obj where obj.organization.id = ?1 and obj.accountCodeHirarchy like ?2 and obj.presentStatus=1 and obj.expenseUnitsMeasure is not null and obj.expenseUnitsMeasure != ''";

	// public ObjectNode showCOA(ObjectNode result,JsonNode json,Users
	// user,EntityManager entityManager,EntityTransaction entitytransaction);
	public ObjectNode getCoaForBranchWithAllHeads(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager);

	public List<Specifics> getIncomesCoaChildNodes(EntityManager entityManager, Users user);

	public List<Specifics> getExpensesCoaChildNodes(EntityManager entityManager, Users user);

	public List<Specifics> getAssetsCoaChildNodes(EntityManager entityManager, Users user);

	public List<Specifics> getLiabilitiesCoaLeafNodes(EntityManager entityManager, Users user);

	public List<Specifics> getSpecificsByBranchAndHeadType(EntityManager entityManager, Users user, Long branchId,
			int headType);

	// public void getAssetsCoaNodes(ArrayNode assetsCOAAn, long brachId,
	// EntityManager entityManager, Users user);
	// public void getLiabilitiesCoaNodes(ArrayNode liabilitiesCoaAn, long brachId,
	// EntityManager entityManager, Users user);
	public boolean getAssetsCoaNodes(ArrayNode assetsCOAAn, long brachId, EntityManager entityManager, Users user,
			Specifics specifics);

	public boolean getLiabilitiesCoaNodes(ArrayNode liabilitiesCoaAn, long brachId, EntityManager entityManager,
			Users user, Specifics specifics);

	public ObjectNode getCoaForOrganizationWithAllHeads(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager);

	public List<String> getCoaForOrganizationWithAllHeads(Users user, EntityManager entityManager);

	public ObjectNode getAssetsCoaChildNodesWithAllHeads(EntityManager entityManager, Users user);

	public ObjectNode getLiabilitiesCoaLeafNodesWithAllHeads(EntityManager entityManager, Users user);

	public Specifics getSpecificsForMapping(Users user, String mappingId, EntityManager em);

	public int getOrgCOATreeDepth(Users user, EntityManager em);

	public List<Specifics> getIncomeExpenseSpecifics4UserByBranch(EntityManager entityManager, Users user,
			Long branchId);

	public List<Specifics> getIncomeOrExpenseSpecifics4UserByBranch(EntityManager entityManager, Users user,
			Long branchId, boolean isIncome);

	List<Specifics> getCOAChildNodesList(EntityManager entityManager, Users user, int headType);

	boolean findCoaByName(ArrayNode assetsCOAAn, EntityManager entityManager, Users user, Specifics specifics,
			String searchText, List<String> coaList, List<String> fromAmt, List<String> toAmt);

	public List<Specifics> getCoaChildNodesByMapping(Long parentId, Users user, EntityManager entityManager);

	boolean getPayrollItem4CoaNode(ArrayNode coaArrayNode, long brachId, EntityManager em, Users user,
			Specifics specifics);

	List<String> getAllCoaUnitForOrg(EntityManager em, Users user, int headType, ArrayNode coaArrayNode);
}
