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

public interface ClaimItemDetailsService extends BaseService {
	public boolean saveClaimItemDetails(Users user, JsonNode json, EntityManager entityManager,
			ClaimTransaction claimTransaction, ClaimsSettlement claimsSettlement) throws IDOSException;

	public boolean saveClaimDetailsRow(Users user, JSONObject rowItemData, String itemCategory,
			ClaimTransaction claimTransaction, EntityManager entityManager, Long claimsSettlementId)
			throws IDOSException;

	public ArrayNode getClaimDetails(ArrayNode result, JsonNode json, Users user, EntityManager entityManager,
			ClaimTransaction claimTransaction, String itemCategory) throws IDOSException;

	public ObjectNode paymentForClaims(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException;
}
