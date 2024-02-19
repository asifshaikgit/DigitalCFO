package service;

import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Branch;
import model.Specifics;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ChartOfAccountsService extends BaseService {
	ObjectNode getCoaForBranchWithAllHeads(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);

	List<Specifics> getIncomesCoaChildNodes(EntityManager entityManager, Users user);

	List<Specifics> getExpensesCoaChildNodes(EntityManager entityManager, Users user);

	List<Specifics> getSpecificsByBranchAndHeadType(EntityManager entityManager, Users user, Long branchId,
			int headType);

	List<Specifics> getAssetsCoaChildNodes(EntityManager entityManager, Users user);

	List<Specifics> getLiabilitiesCoaLeafNodes(EntityManager entityManager, Users user);

	ObjectNode getCoaForOrganizationWithAllHeads(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager);

	ObjectNode getAssetsCoaChildNodesWithAllHeads(EntityManager entityManager, Users user);

	ObjectNode getLiabilitiesCoaLeafNodesWithAllHeads(EntityManager entityManager, Users user);

	Specifics getSpecificsForMapping(Users user, String mappingId, EntityManager em);

	List<Specifics> getIncomeExpenseSpecifics4UserByBranch(EntityManager entityManager, Users user, Long branchId);

	boolean getAssetsCoaNodes(ArrayNode assetsCOAAn, long brachId, EntityManager entityManager, Users user,
			Specifics specifics);

	boolean getLiabilitiesCoaNodes(ArrayNode liabilitiesCoaAn, long brachId, EntityManager entityManager, Users user,
			Specifics specifics);

	List<Specifics> getIncomeOrExpenseSpecifics4UserByBranch(EntityManager entityManager, Users user, Long branchId,
			boolean isIncome);

	List<Specifics> getCOAChildNodesList(EntityManager entityManager, Users user, int headType);

	boolean findCoaByName(ArrayNode assetsCOAAn, EntityManager entityManager, Users user, Specifics specifics,
			String searchText, List<String> coaList, List<String> fromAmt, List<String> toAmt);

	public List<Specifics> getCoaChildNodesByMapping(Long parentId, Users user, EntityManager entityManager);

	List<String> getAllCoaUnitForOrg(EntityManager em, Users user, int headType, ArrayNode coaArrayNode);

	boolean checkToAddMappedItem(Specifics specific);
}
