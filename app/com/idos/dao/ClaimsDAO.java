package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IDOSException;

import model.ClaimTransaction;
import model.Users;
import service.FileUploadService;
import service.FileUploadServiceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ClaimsDAO extends BaseDAO {
	public ObjectNode locationOnTravelType(ObjectNode result, JsonNode json, Users user);

	public ObjectNode displayTravelEligibility(ObjectNode result, JsonNode json, Users user);

	public ObjectNode submitForApproval(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException;

	public ObjectNode approverAction(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException;

	public ObjectNode userAdvancesTxnApprovedButNotAccountedCount(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction);

	public ObjectNode userClaimsTransactions(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction);

	public ObjectNode exitingClaimsAdvanceTxnRefAndAmount(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction);

	// public ObjectNode displayUnsettledAdvancesDetails(ObjectNode result,JsonNode
	// json,Users user,EntityManager entityManager,EntityTransaction
	// entitytransaction);
	void saveClaimBRSDate(Users user, EntityManager entityManager, EntityTransaction entitytransaction,
			String transactionRef, String brsBankDate);

	public void addAdvanceToUserAccount(ClaimTransaction claimTransaction, Users user);
}
