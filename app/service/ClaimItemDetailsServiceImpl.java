package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONObject;

import com.idos.util.IDOSException;

import model.ClaimTransaction;
import model.ClaimsSettlement;
import model.Users;

public class ClaimItemDetailsServiceImpl implements ClaimItemDetailsService {

	@Override
	public boolean saveClaimItemDetails(Users user, JsonNode json, EntityManager entityManager,
			ClaimTransaction claimTransaction, ClaimsSettlement claimsSettlement) throws IDOSException {
		return CLAIM_DETAILS_DAO.saveClaimItemDetails(user, json, entityManager, claimTransaction, claimsSettlement);
	}

	@Override
	public boolean saveClaimDetailsRow(Users user, JSONObject rowItemData, String itemCategory,
			ClaimTransaction claimTransaction, EntityManager entityManager, Long claimsSettlementId)
			throws IDOSException {
		return CLAIM_DETAILS_DAO.saveClaimDetailsRow(user, rowItemData, itemCategory, claimTransaction, entityManager,
				claimsSettlementId);
	}

	@Override
	public ArrayNode getClaimDetails(ArrayNode result, JsonNode json, Users user, EntityManager entityManager,
			ClaimTransaction claimTransaction, String itemCategory) throws IDOSException {
		return CLAIM_DETAILS_DAO.getClaimDetails(result, json, user, entityManager, claimTransaction, itemCategory);
	}

	@Override
	public ObjectNode paymentForClaims(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException {
		return CLAIM_DETAILS_DAO.paymentForClaims(result, json, user, entityManager, entitytransaction);
	}

}
