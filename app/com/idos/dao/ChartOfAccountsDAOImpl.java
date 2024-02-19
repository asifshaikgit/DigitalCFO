package com.idos.dao;

import java.math.BigInteger;
import java.util.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.logging.Level;
import com.idos.util.CoaMappingConstants;
import com.idos.util.IdosConstants;
import com.idos.util.IdosDaoConstants;
import com.idos.util.IdosUtil;
import model.*;

import model.payroll.PayrollSetup;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.jpa.JPAApi;
import play.libs.Json;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @author Sunil Namdev
 *         rewritten and made recursevie to fetch last child of each node
 *         earlier it was fetching till 2nd child.
 */
public class ChartOfAccountsDAOImpl implements ChartOfAccountsDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	/**
	 * instead of showCOA now this method will be called from provision journal
	 * entry transaction.
	 * this method fetches only child record for a specific Branch.
	 * 
	 * @param result
	 * @param json
	 * @param user
	 * @param entityManager
	 * @return leaf node list
	 */
	@Override
	public ObjectNode getCoaForBranchWithAllHeads(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) {
		ArrayNode incomeCOAAn = result.putArray("incomeCOAData");
		ArrayNode expenseCOAAn = result.putArray("expenseCOAData");
		ArrayNode assetsCOAAn = result.putArray("assetsCOAData");
		ArrayNode liabilitiesCOAAn = result.putArray("liabilitiesCOAData");
		ArrayNode tableRowIdAn = result.putArray("tableRowIdData");
		ArrayNode projectBranchData = result.putArray("projectData");

		String tableRowId = json.findValue("tableRowId").asText();
		Long branchId = json.findValue("txnBranchID") != null ? json.findValue("txnBranchID").asLong() : 0l;
		ObjectNode tableRowIdRow = Json.newObject();
		tableRowIdRow.put("tableRowId", tableRowId);
		tableRowIdAn.add(tableRowIdRow);
		Boolean isProjectReq = false;
		if (json.has("projectReq")) {
			isProjectReq = (json.findValue("projectReq").asText() == null) ? false
					: json.findValue("projectReq").asBoolean();
		}
		result.put("incomeresult", false);
		result.put("expenseresult", false);
		result.put("assetsresult", false);
		result.put("liabilitiesresult", false);
		result.put("projectresult", false);

		if (isProjectReq && branchId != 0l) {
			Map<String, Object> criterias = new HashMap<String, Object>(2);
			criterias.clear();
			criterias.put("projectOrganization.id", user.getOrganization().getId());
			criterias.put("projectBranch.id", branchId);
			criterias.put("presentStatus", 1);
			List<ProjectBranches> projectBranches = genericDao.findByCriteria(ProjectBranches.class, criterias,
					entityManager);
			if (projectBranches != null && !projectBranches.isEmpty()) {
				result.put("projectresult", true);
				for (ProjectBranches projectBranch : projectBranches) {
					ObjectNode projectRow = Json.newObject();
					projectRow.put("id", projectBranch.getProject().getId());
					projectRow.put("name", projectBranch.getProject().getName());
					projectBranchData.add(projectRow);
				}
			}
		}

		List<Specifics> incomeSpecfList = getCOAChildNodesList(entityManager, user, 1);
		if (!incomeSpecfList.isEmpty()) {
			result.put("incomeresult", true);
			for (Specifics specf : incomeSpecfList) {
				ObjectNode incomesRow = Json.newObject();
				incomesRow.put("id", IdosConstants.HEAD_SPECIFIC + specf.getId());
				incomesRow.put("name", specf.getName());
				incomeCOAAn.add(incomesRow);
			}
		}

		List<Specifics> expenseSpecfList = getCOAChildNodesList(entityManager, user, 2);
		if (!expenseSpecfList.isEmpty()) {
			result.put("expenseresult", true);
			for (Specifics specf : expenseSpecfList) {
				ObjectNode expenseRow = Json.newObject();
				expenseRow.put("id", IdosConstants.HEAD_SPECIFIC + specf.getId());
				expenseRow.put("name", specf.getName());
				expenseCOAAn.add(expenseRow);
			}
		}
		List<Specifics> assetsSpecfList = getCOAChildNodesList(entityManager, user, 3);
		if (!assetsSpecfList.isEmpty()) {
			result.put("assetsresult", true);
			for (Specifics specfics : assetsSpecfList) {
				boolean isChildNodeAdded = getAssetsCoaNodes(assetsCOAAn, branchId, entityManager, user, specfics);
				if (!isChildNodeAdded) {
					ObjectNode assetsRow = Json.newObject();
					assetsRow.put("id", IdosConstants.HEAD_SPECIFIC + specfics.getId());
					assetsRow.put("name", specfics.getName());
					assetsCOAAn.add(assetsRow);
				}
			}
		}

		List<Specifics> liabilitiesSpecfList = getCOAChildNodesList(entityManager, user, 4);
		if (!liabilitiesSpecfList.isEmpty()) {
			result.put("liabilitiesresult", true);
			for (Specifics specfics : liabilitiesSpecfList) {
				boolean isChildNodeAdded = getLiabilitiesCoaNodes(liabilitiesCOAAn, branchId, entityManager, user,
						specfics);
				if (!isChildNodeAdded) {
					ObjectNode liabilitiesRow = Json.newObject();
					liabilitiesRow.put("id", IdosConstants.HEAD_SPECIFIC + specfics.getId());
					liabilitiesRow.put("name", specfics.getName());
					liabilitiesCOAAn.add(liabilitiesRow);
				}
			}
		}
		return result;
	}

	/**
	 * Will fetch only child nodes for each head type
	 * 
	 * @param entityManager
	 * @param user
	 * @param headType      values can be 1,2,3,4
	 * @return
	 */
	public List<Specifics> getCOAChildNodesList(EntityManager entityManager, Users user, int headType) {
		// if(log.isLoggable(Level.INFO)) log.log(Level.FINE, "Hql: " + SQL_SPECIFICS);
		System.out.println(user + "..............<<<<<<<>>>>>>>>>");
		TypedQuery<Specifics> query = (TypedQuery<Specifics>) entityManager.createQuery(SQL_SPECIFICS, Specifics.class);
		System.out.println("<><>" + user.getOrganization());
		System.out.println("<><<>>" + headType);
		query.setParameter(1, user.getOrganization());
		query.setParameter(2, headType + "%");
		List<Specifics> leafNodes = query.getResultList();
		System.out.println(user + "..............<<<<<<<>>>>>>>>>" + leafNodes);
		if (log.isLoggable(Level.INFO))
			log.log(Level.FINE, "End: " + leafNodes);
		return leafNodes;
	}

	@Override
	public List<Specifics> getSpecificsByBranchAndHeadType(EntityManager entityManager, Users user, Long branchId,
			int headType) {
		ArrayList inparms = new ArrayList<>(3);
		inparms.add(user.getOrganization().getId());
		inparms.add(branchId);
		inparms.add("/" + headType + "%");
		List<Specifics> specificsList = genericDao.queryWithParams(BY_BRANCH_SPECIFICS_JPAQ, entityManager, inparms);
		return specificsList;
	}

	@Override
	public List<Specifics> getIncomesCoaChildNodes(EntityManager entityManager, Users user) {
		List<Specifics> incomeLeafNodes = getCOAChildNodesList(entityManager, user, 1);
		return incomeLeafNodes;
	}

	@Override
	public List<Specifics> getExpensesCoaChildNodes(EntityManager entityManager, Users user) {
		List<Specifics> expenseLeafNodes = getCOAChildNodesList(entityManager, user, 2);
		return expenseLeafNodes;
	}

	@Override
	public List<Specifics> getAssetsCoaChildNodes(EntityManager entityManager, Users user) {
		List<Specifics> assetsLeafNodes = getCOAChildNodesList(entityManager, user, 3);
		return assetsLeafNodes;
	}

	@Override
	public List<Specifics> getLiabilitiesCoaLeafNodes(EntityManager entityManager, Users user) {
		List<Specifics> liabilitiesLeafNodes = getCOAChildNodesList(entityManager, user, 4);
		return liabilitiesLeafNodes;
	}

	@Override
	public boolean getAssetsCoaNodes(ArrayNode assetsCOAAn, long brachId, EntityManager entityManager, Users user,
			Specifics specifics) {
		Map<String, Object> criterias = new HashMap<String, Object>();
		boolean isChildNodeAdded = false;
		if (specifics.getIdentificationForDataValid() != null
				&& specifics.getIdentificationForDataValid().equals("3")) {
			criterias.clear();
			if (brachId != 0)
				criterias.put("branch.id", brachId);
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			/*
			 * BranchDepositBoxKey bnchCashCount =
			 * genericDao.getByCriteria(BranchDepositBoxKey.class, criterias,
			 * entityManager);
			 * if (bnchCashCount != null) {
			 * ObjectNode assetsCashRow = Json.newObject();
			 * assetsCashRow.put("id", IdosConstants.HEAD_CASH + bnchCashCount.getId());
			 * assetsCashRow.put("name", bnchCashCount.getBranch().getName() + " Cash");
			 * assetsCOAAn.add(assetsCashRow);
			 * isChildNodeAdded = true;
			 * }
			 */
			List<BranchDepositBoxKey> bnchCashCountList = genericDao.findByCriteria(BranchDepositBoxKey.class,
					criterias, entityManager);
			if (!bnchCashCountList.isEmpty()) {
				isChildNodeAdded = true;
				for (BranchDepositBoxKey bnchCashCount : bnchCashCountList) {
					ObjectNode assetsCashRow = Json.newObject();
					assetsCashRow.put("id", IdosConstants.HEAD_CASH + bnchCashCount.getId());
					assetsCashRow.put("name", bnchCashCount.getBranch().getName() + " Cash");
					assetsCashRow.put("headType", IdosConstants.HEAD_CASH);
					assetsCOAAn.add(assetsCashRow);
				}
			}
		} else if ("30".equals(specifics.getIdentificationForDataValid())) {
			criterias.clear();
			if (brachId != 0)
				criterias.put("branch.id", brachId);
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			/*
			 * BranchDepositBoxKey bnchCashCount =
			 * genericDao.getByCriteria(BranchDepositBoxKey.class, criterias,
			 * entityManager);
			 * if (bnchCashCount != null) {
			 * ObjectNode assetsCashRow = Json.newObject();
			 * assetsCashRow.put("id", IdosConstants.HEAD_PETTY + bnchCashCount.getId());
			 * assetsCashRow.put("name", bnchCashCount.getBranch().getName() +
			 * " Petty Cash");
			 * assetsCOAAn.add(assetsCashRow);
			 * isChildNodeAdded = true;
			 * }
			 */

			List<BranchDepositBoxKey> bnchCashCountList = genericDao.findByCriteria(BranchDepositBoxKey.class,
					criterias, entityManager);
			if (!bnchCashCountList.isEmpty()) {
				isChildNodeAdded = true;
				for (BranchDepositBoxKey bnchCashCount : bnchCashCountList) {
					ObjectNode assetsCashRow = Json.newObject();
					assetsCashRow.put("id", IdosConstants.HEAD_PETTY + bnchCashCount.getId());
					assetsCashRow.put("name", bnchCashCount.getBranch().getName() + " Petty Cash");
					assetsCashRow.put("headType", IdosConstants.HEAD_PETTY);
					assetsCOAAn.add(assetsCashRow);
				}
			}
		} else if (specifics.getIdentificationForDataValid() != null
				&& specifics.getIdentificationForDataValid().equals("4")) {
			criterias.clear();
			if (brachId != 0)
				criterias.put("branch.id", brachId);
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<BranchBankAccounts> bnchBankAccounts = genericDao.findByCriteria(BranchBankAccounts.class, criterias,
					entityManager);
			if (!bnchBankAccounts.isEmpty()) {
				isChildNodeAdded = true;
				for (BranchBankAccounts indBnchBankAccounts : bnchBankAccounts) {
					ObjectNode assetsBankRow = Json.newObject();
					assetsBankRow.put("id", IdosConstants.HEAD_BANK + indBnchBankAccounts.getId());
					assetsBankRow.put("name",
							indBnchBankAccounts.getBranch().getName() + " " + indBnchBankAccounts.getBankName());
					assetsBankRow.put("headType", IdosConstants.HEAD_BANK);
					assetsCOAAn.add(assetsBankRow);
				}
			}
		} else if ("1".equals(specifics.getIdentificationForDataValid())) {
			// <option value="1">Is this customer account / debtors</option> ==
			// Assets-Account Receivables , cust credit sales
			// all customers of the organization is also assets for the company
			String customerHQL = null;
			ArrayList inparms = new ArrayList<>(3);
			inparms.add(user.getOrganization().getId());
			if (brachId != 0) {
				customerHQL = VEND_CUST_BRANCH_HQL;
				inparms.add(brachId);
			} else {
				customerHQL = IdosConstants.VEND_CUST_ORG_HQL;
			}
			inparms.add(2);
			inparms.add(3);
			List<Vendor> customersList = genericDao.queryWithParams(customerHQL, entityManager, inparms);
			if (!customersList.isEmpty()) {
				isChildNodeAdded = true;
				for (Vendor cust : customersList) {
					ObjectNode assetsCustRow = Json.newObject();
					assetsCustRow.put("id", IdosConstants.HEAD_CUSTOMER + cust.getId());
					assetsCustRow.put("name", cust.getName());
					assetsCustRow.put("headType", IdosConstants.HEAD_CUSTOMER);
					assetsCOAAn.add(assetsCustRow);
				}
			}
		} else if ("7".equals(specifics.getIdentificationForDataValid())) { // 7- Is this the account where you classify
																			// advance paid to vendors / creditors
			String vendorHql = null;
			ArrayList inparms = new ArrayList<>(3);
			inparms.add(user.getOrganization().getId());
			if (brachId != 0) {
				inparms.add(brachId);
				vendorHql = VEND_CUST_BRANCH_HQL;
			} else {
				vendorHql = IdosConstants.VEND_CUST_ORG_HQL;
			}
			inparms.add(1);
			inparms.add(4);
			List<Vendor> customersList = genericDao.queryWithParams(vendorHql, entityManager, inparms);
			if (!customersList.isEmpty()) {
				isChildNodeAdded = true;
				for (Vendor vendor : customersList) {
					ObjectNode assetsCustRow = Json.newObject();
					assetsCustRow.put("id", IdosConstants.HEAD_VENDOR_ADV + vendor.getId()); // In TB shown Under
																								// Assets: Vendor
																								// advance
					assetsCustRow.put("name", vendor.getName() + "_Adv");
					assetsCustRow.put("headType", IdosConstants.HEAD_VENDOR_ADV);
					assetsCOAAn.add(assetsCustRow);
				}
			}
		} else if ("12".equals(specifics.getIdentificationForDataValid())) {
			// 12 - Is this the account where you classify travel advances paid
			StringBuilder newsbquery = new StringBuilder("select obj from Users obj where obj.organization.id=")
					.append(user.getOrganization().getId());
			if (brachId != 0) {
				newsbquery.append(" and obj.branch.id=").append(brachId);
			}
			newsbquery.append(
					" and id in (select distinct createdBy from  ClaimTransaction claim where claim.transactionBranchOrganization.id = ")
					.append(user.getOrganization().getId());
			if (brachId != 0) {
				newsbquery.append(" and transactionBranch.id=").append(brachId);
			}
			newsbquery.append(" and claim.transactionStatus='Accounted' and claim.presentStatus=1")
					.append(" and claim.transactionPurpose.id = 15) and obj.presentStatus=1");

			List<Users> travelAdvUserList = genericDao.executeSimpleQuery(newsbquery.toString(), entityManager);
			if (travelAdvUserList.size() > 0) {
				for (Users userTmp : travelAdvUserList) {
					ObjectNode assetsUsersRow = Json.newObject();
					assetsUsersRow.put("id", IdosConstants.HEAD_USER + userTmp.getId());
					assetsUsersRow.put("name", userTmp.getFullName());
					assetsUsersRow.put("headType", IdosConstants.HEAD_USER);
					assetsCOAAn.add(assetsUsersRow);
				}
				isChildNodeAdded = true;
			}
		} else if ("13".equals(specifics.getIdentificationForDataValid())) {
			// 13">Is this the account where you classify advance paid to staff for expenses
			StringBuilder newsbquery = new StringBuilder("select obj from Users obj where obj.organization.id=")
					.append(user.getOrganization().getId());
			if (brachId != 0) {
				newsbquery.append(" and obj.branch.id=").append(brachId);
			}
			newsbquery.append(
					" and id in (select distinct createdBy from ClaimTransaction claim where claim.transactionBranchOrganization.id = ")
					.append(user.getOrganization().getId());
			if (brachId != 0)
				newsbquery.append(" and transactionBranch.id=").append(brachId);
			newsbquery.append(" and claim.transactionStatus='Accounted' and claim.presentStatus=1")
					.append(" and claim.transactionPurpose.id = 17) and obj.presentStatus=1");
			List<Users> travelAdvUserList = genericDao.executeSimpleQuery(newsbquery.toString(), entityManager);
			if (travelAdvUserList.size() > 0) {
				for (Users userTmp : travelAdvUserList) {
					ObjectNode assetsUsersRow = Json.newObject();
					assetsUsersRow.put("id", IdosConstants.HEAD_USER + userTmp.getId());
					assetsUsersRow.put("name", userTmp.getFullName());
					assetsUsersRow.put("headType", IdosConstants.HEAD_USER);
					assetsCOAAn.add(assetsUsersRow);
				}
				isChildNodeAdded = true;
			}
		} else if ("57".equals(specifics.getIdentificationForDataValid())) {
			if (brachId != 0) {
				isChildNodeAdded = SPECIFICS_DAO.getInterBranchAccountsWithHead4Branch(entityManager, user, assetsCOAAn,
						brachId);
			} else {
				isChildNodeAdded = SPECIFICS_DAO.getInterBranchAccountsWithHead(entityManager, user, assetsCOAAn);
			}

		} else if (specifics.getIdentificationForDataValid() != null) {
			int taxType = 0;
			String headType = IdosConstants.HEAD_TAXS;
			if (specifics.getIdentificationForDataValid().equals("8")) {
				headType = IdosConstants.HEAD_TDS_INPUT;
				isChildNodeAdded = true;
				ObjectNode assetsUsersRow = Json.newObject();
				assetsUsersRow.put("id", headType + specifics.getId());
				assetsUsersRow.put("name", specifics.getName());
				assetsUsersRow.put("headType", headType);
				assetsCOAAn.add(assetsUsersRow);
			} else if (specifics.getIdentificationForDataValid().equals("14")) {
				taxType = IdosConstants.INPUT_TAX;
				headType = IdosConstants.HEAD_TAXS;
			} else if (specifics.getIdentificationForDataValid().equals("39")) {
				taxType = IdosConstants.INPUT_SGST;
				headType = IdosConstants.HEAD_SGST;
			} else if (specifics.getIdentificationForDataValid().equals("40")) {
				taxType = IdosConstants.INPUT_CGST;
				headType = IdosConstants.HEAD_CGST;
			} else if (specifics.getIdentificationForDataValid().equals("41")) {
				taxType = IdosConstants.INPUT_IGST;
				headType = IdosConstants.HEAD_IGST;
			} else if (specifics.getIdentificationForDataValid().equals("42")) {
				taxType = IdosConstants.INPUT_CESS;
				headType = IdosConstants.HEAD_CESS;
			} else if (specifics.getIdentificationForDataValid().equals("53")) {
				taxType = IdosConstants.RCM_SGST_IN;
				headType = IdosConstants.HEAD_RCM_SGST_IN;
			} else if (specifics.getIdentificationForDataValid().equals("54")) {
				taxType = IdosConstants.RCM_CGST_IN;
				headType = IdosConstants.HEAD_RCM_CGST_IN;
			} else if (specifics.getIdentificationForDataValid().equals("55")) {
				taxType = IdosConstants.RCM_IGST_IN;
				headType = IdosConstants.HEAD_RCM_IGST_IN;
			} else if (specifics.getIdentificationForDataValid().equals("56")) {
				taxType = IdosConstants.RCM_CESS_IN;
				headType = IdosConstants.HEAD_RCM_CESS_IN;
			}
			if (taxType != 0) {
				criterias.clear();
				if (brachId != 0)
					criterias.put("branch.id", brachId);
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("taxType", taxType);
				criterias.put("presentStatus", 1);
				List<BranchTaxes> branchTaxesList = genericDao.findByCriteria(BranchTaxes.class, criterias,
						entityManager);
				if (!branchTaxesList.isEmpty()) {
					isChildNodeAdded = true;
				}
				for (BranchTaxes branchTaxes : branchTaxesList) {
					ObjectNode assetsUsersRow = Json.newObject();
					assetsUsersRow.put("id", headType + branchTaxes.getId());
					assetsUsersRow.put("name", branchTaxes.getTaxName());
					assetsUsersRow.put("headType", headType);
					assetsCOAAn.add(assetsUsersRow);
				}
			}
		}
		return isChildNodeAdded;
	}

	@Override
	public boolean getLiabilitiesCoaNodes(ArrayNode liabilitiesCoaAn, long brachId, EntityManager entityManager,
			Users user, Specifics specifics) {
		if (log.isLoggable(Level.INFO))
			log.log(Level.FINE, "==== Start");
		Map<String, Object> criterias = new HashMap<String, Object>();
		boolean isChildNodeAdded = false;
		int mappingId = 0;
		if (specifics.getIdentificationForDataValid() != null
				&& !specifics.getIdentificationForDataValid().equalsIgnoreCase("")) {
			mappingId = Integer.parseInt(specifics.getIdentificationForDataValid());
		}

		if (specifics.getIdentificationForDataValid() != null
				&& specifics.getIdentificationForDataValid().equals("2")) {
			// <option value="2">Is this vendor account / creditors</option> == Liabilities
			// - Acct payables(purchase on credit from vendors)
			// all vendors of the organization is liabilities for the company
			String vendorHQL = null;
			ArrayList inparms = new ArrayList<>(3);
			inparms.add(user.getOrganization().getId());
			if (brachId != 0) {
				inparms.add(brachId);
				vendorHQL = VEND_CUST_BRANCH_HQL;
			} else {
				vendorHQL = IdosConstants.VEND_CUST_ORG_HQL;
			}
			inparms.add(1);
			inparms.add(4);
			List<Vendor> vendorsList = genericDao.queryWithParams(vendorHQL, entityManager, inparms);
			if (!vendorsList.isEmpty()) {
				isChildNodeAdded = true;
			}
			for (Vendor vend : vendorsList) {
				ObjectNode liabilitiesVendRow = Json.newObject();
				liabilitiesVendRow.put("id", IdosConstants.HEAD_VENDOR + vend.getId());// Shown under TB-liabilities -
																						// Accounts Payables
				liabilitiesVendRow.put("name", vend.getName());
				liabilitiesVendRow.put("headType", IdosConstants.HEAD_VENDOR);
				liabilitiesCoaAn.add(liabilitiesVendRow);
			}
		} else if (specifics.getIdentificationForDataValid() != null
				&& specifics.getIdentificationForDataValid().equals("5")) {
			criterias.clear();
			if (brachId != 0)
				criterias.put("branch.id", brachId);
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<BranchBankAccounts> bnchBankAccounts = genericDao.findByCriteria(BranchBankAccounts.class, criterias,
					entityManager);
			if (!bnchBankAccounts.isEmpty()) {
				isChildNodeAdded = true;
			}
			for (BranchBankAccounts indBnchBankAccounts : bnchBankAccounts) {
				ObjectNode liabilitiesBankRow = Json.newObject();
				liabilitiesBankRow.put("id", IdosConstants.HEAD_BANK + indBnchBankAccounts.getId());
				liabilitiesBankRow.put("name",
						indBnchBankAccounts.getBranch().getName() + " " + indBnchBankAccounts.getBankName());
				liabilitiesBankRow.put("headType", IdosConstants.HEAD_BANK);
				liabilitiesCoaAn.add(liabilitiesBankRow);
			}
		} else if (specifics.getIdentificationForDataValid() != null
				&& specifics.getIdentificationForDataValid().equals("6")) {
			String customerHQL = null;
			ArrayList inparms = new ArrayList<>(3);
			inparms.add(user.getOrganization().getId());
			if (brachId != 0) {
				inparms.add(brachId);
				customerHQL = VEND_CUST_BRANCH_HQL;
			} else {
				customerHQL = IdosConstants.VEND_CUST_ORG_HQL;
			}
			inparms.add(2);
			inparms.add(3);
			List<Vendor> customersList = genericDao.queryWithParams(customerHQL, entityManager, inparms);
			if (!customersList.isEmpty()) {
				isChildNodeAdded = true;
			}
			for (Vendor cust : customersList) {
				ObjectNode liabilityCustRow = Json.newObject();
				liabilityCustRow.put("id", IdosConstants.HEAD_CUSTOMER_ADV + cust.getId()); // In TB:showne under
																							// Liabilities - Customer
																							// advance
				liabilityCustRow.put("name", cust.getName() + "_Adv");
				liabilityCustRow.put("headType", IdosConstants.HEAD_CUSTOMER_ADV);
				liabilitiesCoaAn.add(liabilityCustRow);
			}
		} else if (specifics.getIdentificationForDataValid() != null
				&& specifics.getIdentificationForDataValid().equals("59")) {
			isChildNodeAdded = coaDAO.getPayrollItem4CoaNode(liabilitiesCoaAn, 0, entityManager, user, specifics);
		} else if (specifics.getIdentificationForDataValid() != null
				&& !specifics.getIdentificationForDataValid().equalsIgnoreCase("")
				&& (mappingId < 57 && mappingId > 0)) {
			int taxType = 0;
			String headType = IdosConstants.HEAD_TAXS;
			if (specifics.getIdentificationForDataValid().equals("31")) {
				headType = IdosConstants.HEAD_TDS_192;
			} else if (specifics.getIdentificationForDataValid().equals("32")) {
				headType = IdosConstants.HEAD_TDS_194A;
			} else if (specifics.getIdentificationForDataValid().equals("33")) {
				headType = IdosConstants.HEAD_TDS_194C1;
			} else if (specifics.getIdentificationForDataValid().equals("34")) {
				headType = IdosConstants.HEAD_TDS_194C2;
			} else if (specifics.getIdentificationForDataValid().equals("35")) {
				headType = IdosConstants.HEAD_TDS_194H;
			} else if (specifics.getIdentificationForDataValid().equals("36")) {
				headType = IdosConstants.HEAD_TDS_194I1;
			} else if (specifics.getIdentificationForDataValid().equals("37")) {
				headType = IdosConstants.HEAD_TDS_194I2;
			} else if (specifics.getIdentificationForDataValid().equals("38")) {
				headType = IdosConstants.HEAD_TDS_194J;
			} else if (specifics.getIdentificationForDataValid().equals("15")) {
				taxType = IdosConstants.OUTPUT_TAX;
				headType = IdosConstants.HEAD_TAXS;
			} else if (specifics.getIdentificationForDataValid().equals("43")) {
				taxType = IdosConstants.OUTPUT_SGST;
				headType = IdosConstants.HEAD_SGST;
			} else if (specifics.getIdentificationForDataValid().equals("44")) {
				taxType = IdosConstants.OUTPUT_CGST;
				headType = IdosConstants.HEAD_CGST;
			} else if (specifics.getIdentificationForDataValid().equals("45")) {
				taxType = IdosConstants.OUTPUT_IGST;
				headType = IdosConstants.HEAD_IGST;
			} else if (specifics.getIdentificationForDataValid().equals("46")) {
				taxType = IdosConstants.OUTPUT_CESS;
				headType = IdosConstants.HEAD_CESS;
			} else if (specifics.getIdentificationForDataValid().equals("47")) {
				taxType = IdosConstants.RCM_SGST_OUTPUT;
				headType = IdosConstants.HEAD_RCM_SGST_OUTPUT;
			} else if (specifics.getIdentificationForDataValid().equals("48")) {
				taxType = IdosConstants.RCM_CGST_OUTPUT;
				headType = IdosConstants.HEAD_RCM_CGST_OUTPUT;
			} else if (specifics.getIdentificationForDataValid().equals("49")) {
				taxType = IdosConstants.RCM_IGST_OUTPUT;
				headType = IdosConstants.HEAD_RCM_IGST_OUTPUT;
			} else if (specifics.getIdentificationForDataValid().equals("50")) {
				taxType = IdosConstants.RCM_CESS_OUTPUT;
				headType = IdosConstants.HEAD_RCM_CESS_OUTPUT;
			}
			if (taxType != 0) {
				criterias.clear();
				if (brachId != 0)
					criterias.put("branch.id", brachId);
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("taxType", taxType);
				criterias.put("presentStatus", 1);
				List<BranchTaxes> branchTaxList = genericDao.findByCriteria(BranchTaxes.class, criterias,
						entityManager);
				if (!branchTaxList.isEmpty()) {
					isChildNodeAdded = true;
				}
				for (BranchTaxes branchTaxes : branchTaxList) {
					ObjectNode liabilityTaxRow = Json.newObject();
					liabilityTaxRow.put("id", headType + branchTaxes.getId());
					liabilityTaxRow.put("name", branchTaxes.getTaxName());
					liabilityTaxRow.put("headType", headType);
					liabilitiesCoaAn.add(liabilityTaxRow);
				}
			} else {
				isChildNodeAdded = true;
				ObjectNode liabilityTaxRow = Json.newObject();
				liabilityTaxRow.put("id", headType + specifics.getId());
				liabilityTaxRow.put("name", specifics.getName());
				liabilityTaxRow.put("headType", headType);
				liabilitiesCoaAn.add(liabilityTaxRow);
			}
		}
		if (log.isLoggable(Level.INFO))
			log.log(Level.FINE, "==== End");
		return isChildNodeAdded;
	}

	/**
	 * instead of showCOA now this method will be called from provision journal
	 * entry transaction.
	 * this method fetchs only child record for a specific Branch.
	 *
	 * @param result
	 * @param json
	 * @param user
	 * @param entityManager
	 * @return leaf node list
	 */
	@Override
	public ObjectNode getCoaForOrganizationWithAllHeads(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) {
		ArrayNode incomeCOAAn = result.putArray("incomeCOAData");
		ArrayNode expenseCOAAn = result.putArray("expenseCOAData");
		ArrayNode assetsCOAAn = result.putArray("assetsCOAData");
		ArrayNode liabilitiesCOAAn = result.putArray("liabilitiesCOAData");

		result.put("incomeresult", false);
		result.put("expenseresult", false);
		result.put("assetsresult", false);
		result.put("liabilitiesresult", false);

		List<Specifics> incomeSpecfList = getCOAChildNodesList(entityManager, user, 1);
		if (!incomeSpecfList.isEmpty()) {
			result.put("incomeresult", true);
			for (Specifics specf : incomeSpecfList) {
				ObjectNode incomesRow = Json.newObject();
				incomesRow.put("id", IdosConstants.HEAD_SPECIFIC + specf.getId());
				incomesRow.put("name", specf.getName());
				incomeCOAAn.add(incomesRow);
			}
		}

		List<Specifics> expenseSpecfList = getCOAChildNodesList(entityManager, user, 2);
		if (!expenseSpecfList.isEmpty()) {
			result.put("expenseresult", true);
			for (Specifics specf : expenseSpecfList) {
				ObjectNode expenseRow = Json.newObject();
				expenseRow.put("id", IdosConstants.HEAD_SPECIFIC + specf.getId());
				expenseRow.put("name", specf.getName());
				expenseCOAAn.add(expenseRow);
			}
		}
		List<Specifics> assetsSpecfList = getCOAChildNodesList(entityManager, user, 3);
		if (!assetsSpecfList.isEmpty()) {
			result.put("assetsresult", true);
			for (Specifics specfics : assetsSpecfList) {
				boolean isChildNodeAdded = getAssetsCoaNodes(assetsCOAAn, 0, entityManager, user, specfics);
				if (!isChildNodeAdded) {
					ObjectNode assetsRow = Json.newObject();
					assetsRow.put("id", IdosConstants.HEAD_SPECIFIC + specfics.getId());
					assetsRow.put("name", specfics.getName());
					assetsCOAAn.add(assetsRow);
				}
			}
		}

		List<Specifics> liabilitiesSpecfList = getCOAChildNodesList(entityManager, user, 4);
		if (!liabilitiesSpecfList.isEmpty()) {
			result.put("liabilitiesresult", true);
			for (Specifics specfics : liabilitiesSpecfList) {
				boolean isChildNodeAdded = getLiabilitiesCoaNodes(liabilitiesCOAAn, 0, entityManager, user, specfics);
				if (!isChildNodeAdded) {
					ObjectNode liabilitiesRow = Json.newObject();
					liabilitiesRow.put("id", IdosConstants.HEAD_SPECIFIC + specfics.getId());
					liabilitiesRow.put("name", specfics.getName());
					liabilitiesCOAAn.add(liabilitiesRow);
				}
			}
		}
		log.log(Level.FINE, "*********End " + result);
		return result;
	}

	@Override
	public ObjectNode getAssetsCoaChildNodesWithAllHeads(EntityManager entityManager, Users user) {
		log.log(Level.FINE, "===Start ");
		ObjectNode result = Json.newObject();
		ArrayNode assetsCOAAn = result.putArray("coaItemData");
		List<Specifics> assetsSpecfList = getCOAChildNodesList(entityManager, user, 3);
		for (Specifics specfics : assetsSpecfList) {
			boolean isChildNodeAdded = getAssetsCoaNodes(assetsCOAAn, 0, entityManager, user, specfics);
			if (!isChildNodeAdded) {
				ObjectNode assetsRow = Json.newObject();
				assetsRow.put("id", IdosConstants.HEAD_SPECIFIC + specfics.getId());
				assetsRow.put("name", specfics.getName());
				assetsRow.put("headType", IdosConstants.HEAD_SPECIFIC);
				assetsCOAAn.add(assetsRow);
			}
		}
		log.log(Level.FINE, "===End ");
		return result;
	}

	@Override
	public ObjectNode getLiabilitiesCoaLeafNodesWithAllHeads(EntityManager entityManager, Users user) {
		ObjectNode result = Json.newObject();
		ArrayNode liabilitiesCOAAn = result.putArray("coaItemData");
		List<Specifics> liabilitiesSpecfList = getCOAChildNodesList(entityManager, user, 4);
		for (Specifics specfics : liabilitiesSpecfList) {
			boolean isChildNodeAdded = getLiabilitiesCoaNodes(liabilitiesCOAAn, 0, entityManager, user, specfics);
			if (!isChildNodeAdded && (specfics.getIdentificationForDataValid() == null
					|| "".equals(specfics.getIdentificationForDataValid())
					|| "59".equals(specfics.getIdentificationForDataValid()))) {
				ObjectNode liabilitiesRow = Json.newObject();
				liabilitiesRow.put("id", IdosConstants.HEAD_SPECIFIC + specfics.getId());
				liabilitiesRow.put("name", specfics.getName());
				liabilitiesRow.put("headType", IdosConstants.HEAD_SPECIFIC);
				liabilitiesCOAAn.add(liabilitiesRow);
			}
		}
		log.log(Level.FINE, "======" + result);
		return result;
	}

	@Override
	public Specifics getSpecificsForMapping(Users user, String mappingId, EntityManager em) {
		Map<String, Object> criterias = new HashMap<String, Object>(3);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("identificationForDataValid", mappingId);
		criterias.put("presentStatus", 1);
		Specifics specf = genericDao.getByCriteria(Specifics.class, criterias, em);
		return specf;
	}

	/**
	 * this method returns depth of tree for a orgnization COA
	 *
	 * @param user
	 * @param em
	 * @return
	 */
	@Override
	public int getOrgCOATreeDepth(Users user, EntityManager em) {
		Query query = em.createNativeQuery(SQL_TREE_DEPTH);
		query.setParameter(1, user.getOrganization().getId());
		Object bsValLst = query.getSingleResult();
		int depth = ((BigInteger) bsValLst).intValue();
		/*
		 * int depth = 0;
		 * for(Object[] specific :bsValLst){
		 * depth = ((BigInteger)specific[0]).intValue();
		 * }
		 */
		return depth - 1;
	}

	@Override
	public List<String> getCoaForOrganizationWithAllHeads(Users user, EntityManager entityManager) {
		ObjectNode result = Json.newObject();
		getCoaForOrganizationWithAllHeads(result, null, user, entityManager);
		JsonNode incomeArrNode = result.get("incomeCOAData");
		JsonNode expenseArrNode = result.get("expenseCOAData");
		JsonNode assetsArrNode = result.get("assetsCOAData");
		JsonNode liabilitiesArrNode = result.get("liabilitiesCOAData");
		List<String> coaList = new ArrayList<String>();
		if (incomeArrNode.isArray()) {
			for (JsonNode valueNode : incomeArrNode) {
				String id = valueNode.findValue("id") == null ? "0" : valueNode.findValue("id").asText();
				String name = valueNode.findValue("name") == null ? "0" : valueNode.findValue("name").asText();
				coaList.add(id);
			}
		}
		if (expenseArrNode.isArray()) {
			for (JsonNode valueNode : expenseArrNode) {
				String id = valueNode.findValue("id") == null ? "0" : valueNode.findValue("id").asText();
				String name = valueNode.findValue("name") == null ? "0" : valueNode.findValue("name").asText();
				coaList.add(id);
			}
		}

		if (assetsArrNode.isArray()) {
			for (JsonNode valueNode : assetsArrNode) {
				String id = valueNode.findValue("id") == null ? "0" : valueNode.findValue("id").asText();
				String name = valueNode.findValue("name") == null ? "0" : valueNode.findValue("name").asText();
				coaList.add(id);
			}
		}
		if (liabilitiesArrNode.isArray()) {
			for (JsonNode valueNode : liabilitiesArrNode) {
				String id = valueNode.findValue("id") == null ? "0" : valueNode.findValue("id").asText();
				String name = valueNode.findValue("name") == null ? "0" : valueNode.findValue("name").asText();
				coaList.add(id);
			}
		}
		log.log(Level.FINE, "****** End " + coaList);
		return coaList;
	}

	@Override
	public List<Specifics> getIncomeExpenseSpecifics4UserByBranch(EntityManager entityManager, Users user,
			Long branchId) {
		ArrayList inparms = new ArrayList<>(5);
		inparms.add(user.getOrganization().getId());
		inparms.add(branchId);
		inparms.add(1L);
		inparms.add(user.getId());
		inparms.add("/1%");
		inparms.add("/2%");
		List<Specifics> specificsList = genericDao.queryWithParams(USER_BRANCH_RULE_INCEXP_ITEMS_JPAQ, entityManager,
				inparms);
		return specificsList;
	}

	@Override
	public List<Specifics> getIncomeOrExpenseSpecifics4UserByBranch(EntityManager entityManager, Users user,
			Long branchId, boolean isIncome) {
		ArrayList inparms = new ArrayList<>(5);
		inparms.add(user.getOrganization().getId());
		inparms.add(branchId);
		inparms.add(1L);
		inparms.add(user.getId());
		if (isIncome)
			inparms.add("/1%");
		else
			inparms.add("/2%");
		List<Specifics> specificsList = genericDao.queryWithParams(USER_BRANCH_RULE_INC_OR_EXP_ITEMS_JPAQ,
				entityManager, inparms);
		return specificsList;
	}

	@Override
	public boolean findCoaByName(ArrayNode assetsCOAAn, EntityManager entityManager, Users user, Specifics specifics,
			String searchText, List<String> coaList, List<String> fromAmt, List<String> toAmount) {
		if (searchText != null) {
			searchText = searchText.toLowerCase();
		}
		if (log.isLoggable(Level.INFO))
			log.log(Level.FINE,
					" ****** Start " + searchText + " " + coaList + " " + specifics.getIdentificationForDataValid());
		boolean isChildNodeAdded = false;
		if (specifics.getIdentificationForDataValid() != null
				&& specifics.getIdentificationForDataValid().equals("3")) {
			String hql = null;
			ArrayList inparams = new ArrayList(2);
			inparams.add(user.getOrganization().getId());
			if (searchText.indexOf("cas") != -1 || searchText.indexOf("ash") != -1) {
				hql = "select obj from BranchDepositBoxKey obj where obj.organization.id = ?1 and obj.presentStatus=1";
			} else {
				hql = "select obj from BranchDepositBoxKey obj where obj.organization.id = ?1 and obj.presentStatus=1 and obj.branch.id in (select t1.id from Branch t1 where t1.organization.id=?2 and t1.presentStatus=1 and lower(t1.name) like ?3)";
				inparams.add(user.getOrganization().getId());
				inparams.add("%" + searchText + "%");
			}
			List<BranchDepositBoxKey> bnchCashCountList = genericDao.queryWithParams(hql, entityManager, inparams);
			if (!bnchCashCountList.isEmpty()) {
				isChildNodeAdded = true;
				for (BranchDepositBoxKey bnchCashCount : bnchCashCountList) {
					IdosUtil.setUserItemsDetail(coaList, fromAmt, toAmount, bnchCashCount.getId().toString(),
							bnchCashCount.getBranch().getName() + " Cash", IdosConstants.HEAD_CASH, assetsCOAAn);
				}
			}
		} else if ("30".equals(specifics.getIdentificationForDataValid())) {
			String hql = null;
			ArrayList inparams = new ArrayList(2);
			inparams.add(user.getOrganization().getId());
			if (searchText.indexOf("cas") != -1 || searchText.indexOf("ash") != -1 || searchText.indexOf("pet") != -1
					|| searchText.indexOf("ett") != -1 || searchText.indexOf("tty") != -1) {
				hql = "select obj from BranchDepositBoxKey obj where obj.organization.id = ?1 and obj.presentStatus=1";
			} else {
				hql = "select obj from BranchDepositBoxKey obj where obj.organization.id = ?1 and obj.presentStatus=1 and obj.branch.id in (select t1.id from Branch t1 where t1.organization.id=?2 and t1.presentStatus=1 and lower(t1.name) like ?3)";
				inparams.add(user.getOrganization().getId());
				inparams.add(user.getOrganization().getId());
				inparams.add("%" + searchText + "%");
			}
			List<BranchDepositBoxKey> bnchCashCountList = genericDao.queryWithParams(hql, entityManager, inparams);
			if (!bnchCashCountList.isEmpty()) {
				isChildNodeAdded = true;
				for (BranchDepositBoxKey bnchCashCount : bnchCashCountList) {
					IdosUtil.setUserItemsDetail(coaList, fromAmt, toAmount, bnchCashCount.getId().toString(),
							bnchCashCount.getBranch().getName() + " Petty Cash", IdosConstants.HEAD_PETTY, assetsCOAAn);
				}
			}
		} else if (specifics.getIdentificationForDataValid() != null
				&& specifics.getIdentificationForDataValid().equals("4")) {
			String hql = null;
			ArrayList inparams = new ArrayList(2);
			inparams.add(user.getOrganization().getId());
			if (searchText.toLowerCase().indexOf("ban") != -1 || searchText.toLowerCase().indexOf("ank") != -1) {
				hql = "select obj from BranchBankAccounts obj where obj.organization.id = ?1 and obj.presentStatus=1";
			} else {
				hql = "select obj from BranchBankAccounts obj where obj.organization.id = ?1 and obj.presentStatus=1 and lower(obj.branch.name) like ?";
				inparams.add("%" + searchText + "%");
			}
			List<BranchBankAccounts> bnchBankAccounts = genericDao.queryWithParams(hql, entityManager, inparams);
			if (!bnchBankAccounts.isEmpty()) {
				isChildNodeAdded = true;
				for (BranchBankAccounts indBnchBankAccounts : bnchBankAccounts) {
					IdosUtil.setUserItemsDetail(coaList, fromAmt, toAmount, indBnchBankAccounts.getId().toString(),
							indBnchBankAccounts.getBranch().getName() + " " + indBnchBankAccounts.getBankName(),
							IdosConstants.HEAD_BANK, assetsCOAAn);
				}
			}
		} else if ("1".equals(specifics.getIdentificationForDataValid())) {
			String hql = "select obj from Vendor obj where obj.organization.id = ?1 and obj.presentStatus=1 and type in (2,3) and lower(name) like ?";
			ArrayList inparams = new ArrayList(2);
			inparams.add(user.getOrganization().getId());
			inparams.add("%" + searchText + "%");
			List<Vendor> customersList = genericDao.queryWithParams(hql, entityManager, inparams);
			if (!customersList.isEmpty()) {
				isChildNodeAdded = true;
				for (Vendor cust : customersList) {
					IdosUtil.setUserItemsDetail(coaList, fromAmt, toAmount, cust.getId().toString(), cust.getName(),
							IdosConstants.HEAD_CUSTOMER, assetsCOAAn);
				}
			}
		} else if ("7".equals(specifics.getIdentificationForDataValid())) {
			// 7- Is this the account where you classify advance paid to vendors / creditors
			String hql = null;
			ArrayList inparams = new ArrayList(2);
			inparams.add(user.getOrganization().getId());
			if (searchText.toLowerCase().indexOf("adv") != -1) {
				hql = "select obj from Vendor obj where obj.organization.id = ?1 and obj.presentStatus=1 and type in (1,4)";
			} else {
				hql = "select obj from Vendor obj where obj.organization.id = ?1 and obj.presentStatus=1 and type in (1,4) and lower(obj.name) like ?";
				inparams.add("%" + searchText + "%");
			}
			List<Vendor> customersList = genericDao.queryWithParams(hql, entityManager, inparams);
			if (!customersList.isEmpty()) {
				isChildNodeAdded = true;
				for (Vendor vendor : customersList) {
					IdosUtil.setUserItemsDetail(coaList, fromAmt, toAmount, vendor.getId().toString(),
							vendor.getName() + "_Adv", IdosConstants.HEAD_VENDOR_ADV, assetsCOAAn);
				}
			}
		} else if ("12".equals(specifics.getIdentificationForDataValid())) {
			// 12 - Is this the account where you classify travel advances paid
			String hql = "select obj from Users obj where obj.organization.id=?1 and obj.presentStatus=1 and lower(obj.fullName) like ?2 and id in (select distinct createdBy from ClaimTransaction claim where claim.transactionBranchOrganization.id = ?3 and claim.transactionStatus='Accounted' and claim.transactionPurpose.id = 15)";
			ArrayList inparams = new ArrayList(3);
			inparams.add(user.getOrganization().getId());
			inparams.add("%" + searchText + "%");
			inparams.add(user.getOrganization().getId());
			List<Users> travelAdvUserList = genericDao.queryWithParams(hql, entityManager, inparams);
			if (travelAdvUserList.size() > 0) {
				for (Users userTmp : travelAdvUserList) {
					IdosUtil.setUserItemsDetail(coaList, fromAmt, toAmount, userTmp.getId().toString(),
							userTmp.getFullName(), IdosConstants.HEAD_USER, assetsCOAAn);
				}
				isChildNodeAdded = true;
			}
		} else if ("13".equals(specifics.getIdentificationForDataValid())) {
			// 13">Is this the account where you classify advance paid to staff for expenses
			String hql = "select obj from Users obj where obj.organization.id=?1 and obj.presentStatus=1 and lower(obj.fullName) like ?2 and id in (select distinct createdBy from ClaimTransaction claim where claim.transactionBranchOrganization.id = ?3 and claim.transactionStatus='Accounted' and claim.transactionPurpose.id = 17)";
			ArrayList inparams = new ArrayList(3);
			inparams.add(user.getOrganization().getId());
			inparams.add("%" + searchText + "%");
			inparams.add(user.getOrganization().getId());
			List<Users> travelAdvUserList = genericDao.queryWithParams(hql, entityManager, inparams);
			if (travelAdvUserList.size() > 0) {
				for (Users userTmp : travelAdvUserList) {
					IdosUtil.setUserItemsDetail(coaList, fromAmt, toAmount, userTmp.getId().toString(),
							userTmp.getFullName(), IdosConstants.HEAD_USER, assetsCOAAn);
				}
				isChildNodeAdded = true;
			}
		} else if ("57".equals(specifics.getIdentificationForDataValid())) {
			Query query = entityManager.createQuery(SRCH_NAME_57_JPQL);
			query.setParameter(1, user.getOrganization().getId());
			query.setParameter(2, "%" + searchText + "%");
			List<Object[]> branchList = query.getResultList();
			if (branchList.size() > 0) {
				isChildNodeAdded = true;
			}
			for (Object[] branch : branchList) {
				String id = String.valueOf(branch[4]);
				String name = (String) branch[2] + "-" + (String) branch[3];
				IdosUtil.setUserItemsDetail(coaList, fromAmt, toAmount, id, name, IdosConstants.HEAD_INTR_BRANCH,
						assetsCOAAn);
			}
		} else if (specifics.getIdentificationForDataValid() != null) {
			int taxType = 0;
			String headType = IdosConstants.HEAD_TAXS;
			if (specifics.getIdentificationForDataValid().equals("8")
					&& specifics.getName().toLowerCase().indexOf(searchText) != -1) {
				headType = IdosConstants.HEAD_TDS_INPUT;
				isChildNodeAdded = true;
				IdosUtil.setUserItemsDetail(coaList, fromAmt, toAmount, specifics.getId().toString(),
						specifics.getName(), headType, assetsCOAAn);
			} else if (specifics.getIdentificationForDataValid().equals("14")) {
				taxType = IdosConstants.INPUT_TAX;
				headType = IdosConstants.HEAD_TAXS;
			} else if (specifics.getIdentificationForDataValid().equals("39")) {
				taxType = IdosConstants.INPUT_SGST;
				headType = IdosConstants.HEAD_SGST;
			} else if (specifics.getIdentificationForDataValid().equals("40")) {
				taxType = IdosConstants.INPUT_CGST;
				headType = IdosConstants.HEAD_CGST;
			} else if (specifics.getIdentificationForDataValid().equals("41")) {
				taxType = IdosConstants.INPUT_IGST;
				headType = IdosConstants.HEAD_IGST;
			} else if (specifics.getIdentificationForDataValid().equals("42")) {
				taxType = IdosConstants.INPUT_CESS;
				headType = IdosConstants.HEAD_CESS;
			} else if (specifics.getIdentificationForDataValid().equals("53")) {
				taxType = IdosConstants.RCM_SGST_IN;
				headType = IdosConstants.HEAD_RCM_SGST_IN;
			} else if (specifics.getIdentificationForDataValid().equals("54")) {
				taxType = IdosConstants.RCM_CGST_IN;
				headType = IdosConstants.HEAD_RCM_CGST_IN;
			} else if (specifics.getIdentificationForDataValid().equals("55")) {
				taxType = IdosConstants.RCM_IGST_IN;
				headType = IdosConstants.HEAD_RCM_IGST_IN;
			} else if (specifics.getIdentificationForDataValid().equals("56")) {
				taxType = IdosConstants.RCM_CESS_IN;
				headType = IdosConstants.HEAD_RCM_CESS_IN;
			}
			if (taxType != 0) {
				List<BranchTaxes> branchTaxesList = BranchTaxes.findByNameAndType(entityManager,
						user.getOrganization().getId(), taxType, searchText);
				if (!branchTaxesList.isEmpty()) {
					isChildNodeAdded = true;
				}
				String branchName = null;
				for (BranchTaxes branchTaxes : branchTaxesList) {
					branchName = branchTaxes.getTaxName();
					IdosUtil.setUserItemsDetail(coaList, fromAmt, toAmount, branchTaxes.getId().toString(), branchName,
							headType, assetsCOAAn);
				}
			}
		}
		if (log.isLoggable(Level.INFO))
			log.log(Level.FINE, " ****** End" + isChildNodeAdded);
		return isChildNodeAdded;
	}

	@Override
	public List<Specifics> getCoaChildNodesByMapping(Long parentId, Users user, EntityManager entityManager) {
		Query query = entityManager.createQuery(MAPPING_LEAFNODE_JPQL);
		query.setParameter(1, user.getOrganization().getId());
		query.setParameter(2, parentId);
		List<Specifics> leafNodes = query.getResultList();
		if (log.isLoggable(Level.INFO))
			log.log(Level.FINE, "End: " + leafNodes);
		return leafNodes;
	}

	@Override
	public boolean getPayrollItem4CoaNode(ArrayNode coaArrayNode, long brachId, EntityManager em, Users user,
			Specifics specifics) {
		int mappingID = specifics.getIdentificationForDataValid() == null
				|| specifics.getIdentificationForDataValid().equals("") ? 0
						: Integer.parseInt(specifics.getIdentificationForDataValid());
		int itemType = 0;
		if (mappingID == CoaMappingConstants.PAYROLL_SALARIES_EXPENSES) {
			itemType = 1;
		} else if (mappingID == CoaMappingConstants.PAYROLL_SALARIES_DEDUCTIONS) {
			itemType = 2;
		} else {
			log.log(Level.SEVERE, "Payroll items mapping is not found: " + user.getEmail());
			return false;
		}
		boolean isChildNodeAdded = false;
		ArrayList inparams = new ArrayList(1);
		inparams.add(user.getOrganization().getId());
		inparams.add(itemType);
		List<PayrollSetup> payItemsList = genericDao.queryWithParams(PAYROLL_TEMS_JPQL, em, inparams);
		if (!payItemsList.isEmpty()) {
			isChildNodeAdded = true;
			for (PayrollSetup payrollItem : payItemsList) {
				ObjectNode row = Json.newObject();
				row.put("name", payrollItem.getPayrollHeadName());
				if (payrollItem.getPayrollType() == 1) {
					row.put("headType", IdosConstants.HEAD_PAYROLL_EXPENSE);
					row.put("id", IdosConstants.HEAD_PAYROLL_EXPENSE + payrollItem.getId());
				} else {
					row.put("headType", IdosConstants.HEAD_PAYROLL_DEDUCTIONS);
					row.put("id", IdosConstants.HEAD_PAYROLL_DEDUCTIONS + payrollItem.getId());
				}
				coaArrayNode.add(row);
			}
		}
		return isChildNodeAdded;
	}

	@Override
	public List<String> getAllCoaUnitForOrg(EntityManager em, Users user, int headType, ArrayNode coaArrayNode) {
		Query query = em.createQuery(COA_EXPENSE_UNITS);
		query.setParameter(1, user.getOrganization().getId());
		query.setParameter(2, "/" + headType + "%");
		if (log.isLoggable(Level.INFO))
			log.log(Level.FINE, "JPQL: " + COA_EXPENSE_UNITS);
		List<String> list = query.getResultList();
		for (String unit : list) {
			ObjectNode row = Json.newObject();
			row.put("unitName", unit);
			coaArrayNode.add(row);
		}
		return list;
	}
}
