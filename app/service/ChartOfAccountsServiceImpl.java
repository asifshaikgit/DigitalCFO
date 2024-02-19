package service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.CoaMappingConstants;
import model.Branch;
import model.Specifics;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.dao.ChartOfAccountsDAO;
import com.idos.dao.ChartOfAccountsDAOImpl;

public class ChartOfAccountsServiceImpl implements ChartOfAccountsService {
	@Override
	public ObjectNode getCoaForBranchWithAllHeads(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) {
		result = chartsOfAccountsDAO.getCoaForBranchWithAllHeads(result, json, user, entityManager);
		return result;
	}

	@Override
	public ObjectNode getCoaForOrganizationWithAllHeads(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) {
		result = chartsOfAccountsDAO.getCoaForOrganizationWithAllHeads(result, json, user, entityManager);
		return result;
	}

	@Override
	public List<Specifics> getIncomesCoaChildNodes(EntityManager entityManager, Users user) {
		List<Specifics> specifics = chartsOfAccountsDAO.getIncomesCoaChildNodes(entityManager, user);
		return specifics;
	}

	@Override
	public List<Specifics> getSpecificsByBranchAndHeadType(EntityManager entityManager, Users user, Long branchId,
			int headType) {
		List<Specifics> specifics = chartsOfAccountsDAO.getSpecificsByBranchAndHeadType(entityManager, user, branchId,
				headType);
		return specifics;

	}

	@Override
	public List<Specifics> getExpensesCoaChildNodes(EntityManager entityManager, Users user) {
		List<Specifics> specifics = chartsOfAccountsDAO.getExpensesCoaChildNodes(entityManager, user);
		return specifics;
	}

	@Override
	public List<Specifics> getAssetsCoaChildNodes(EntityManager entityManager, Users user) {
		List<Specifics> specifics = chartsOfAccountsDAO.getAssetsCoaChildNodes(entityManager, user);
		return specifics;
	}

	@Override
	public List<Specifics> getLiabilitiesCoaLeafNodes(EntityManager entityManager, Users user) {
		List<Specifics> specifics = chartsOfAccountsDAO.getLiabilitiesCoaLeafNodes(entityManager, user);
		return specifics;
	}

	@Override
	public ObjectNode getAssetsCoaChildNodesWithAllHeads(EntityManager entityManager, Users user) {
		ObjectNode result = chartsOfAccountsDAO.getAssetsCoaChildNodesWithAllHeads(entityManager, user);
		return result;
	}

	@Override
	public ObjectNode getLiabilitiesCoaLeafNodesWithAllHeads(EntityManager entityManager, Users user) {
		ObjectNode result = chartsOfAccountsDAO.getLiabilitiesCoaLeafNodesWithAllHeads(entityManager, user);
		return result;
	}

	@Override
	public Specifics getSpecificsForMapping(Users user, String mappingId, EntityManager em) {
		return chartsOfAccountsDAO.getSpecificsForMapping(user, mappingId, em);
	}

	@Override
	public List<Specifics> getIncomeExpenseSpecifics4UserByBranch(EntityManager entityManager, Users user,
			Long branchId) {
		List<Specifics> specifics = chartsOfAccountsDAO.getIncomeExpenseSpecifics4UserByBranch(entityManager, user,
				branchId);
		return specifics;
	}

	@Override
	public List<Specifics> getIncomeOrExpenseSpecifics4UserByBranch(EntityManager entityManager, Users user,
			Long branchId, boolean isIncome) {
		List<Specifics> specifics = chartsOfAccountsDAO.getIncomeOrExpenseSpecifics4UserByBranch(entityManager, user,
				branchId, isIncome);
		return specifics;
	}

	@Override
	public boolean getAssetsCoaNodes(ArrayNode assetsCOAAn, long brachId, EntityManager entityManager, Users user,
			Specifics specifics) {
		return chartsOfAccountsDAO.getAssetsCoaNodes(assetsCOAAn, brachId, entityManager, user, specifics);
	}

	@Override
	public boolean getLiabilitiesCoaNodes(ArrayNode liabilitiesCoaAn, long brachId, EntityManager entityManager,
			Users user, Specifics specifics) {
		return chartsOfAccountsDAO.getLiabilitiesCoaNodes(liabilitiesCoaAn, brachId, entityManager, user, specifics);
	}

	@Override
	public List<Specifics> getCOAChildNodesList(EntityManager entityManager, Users user, int headType) {
		return chartsOfAccountsDAO.getCOAChildNodesList(entityManager, user, headType);
	}

	@Override
	public boolean findCoaByName(ArrayNode assetsCOAAn, EntityManager em, Users user, Specifics specifics,
			String searchText, List<String> coaList, List<String> fromAmt, List<String> toAmt) {
		return chartsOfAccountsDAO.findCoaByName(assetsCOAAn, em, user, specifics, searchText, coaList, fromAmt, toAmt);
	}

	@Override
	public List<Specifics> getCoaChildNodesByMapping(Long parentId, Users user, EntityManager entityManager) {
		// TODO Auto-generated method stub
		return chartsOfAccountsDAO.getCoaChildNodesByMapping(parentId, user, entityManager);
	}

	@Override
	public List<String> getAllCoaUnitForOrg(EntityManager em, Users user, int headType, ArrayNode coaArrayNode) {
		return chartsOfAccountsDAO.getAllCoaUnitForOrg(em, user, headType, coaArrayNode);
	}

	@Override
	public boolean checkToAddMappedItem(Specifics specific) {
		int mappingId = specific.getIdentificationForDataValid() == null
				|| "".equals(specific.getIdentificationForDataValid()) ? 0
						: Integer.parseInt(specific.getIdentificationForDataValid());
		boolean returnValue = true;
		if (CoaMappingConstants.ONLY_CHILD_COA.contains(mappingId)) {
			returnValue = false;
		}
		return returnValue;
	}
}
