package service;

import java.text.ParseException;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IDOSException;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ClaimsService extends BaseService {
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

	// public ObjectNode populateUserUnsettledTravelClaimAdvances(ObjectNode
	// result,JsonNode json,Users user,EntityManager entityManager,EntityTransaction
	// entitytransaction);
	// public ObjectNode displayUnsettledAdvancesDetails(ObjectNode result,JsonNode
	// json,Users user,EntityManager entityManager,EntityTransaction
	// entitytransaction);
	// public ObjectNode claimSettlementAccountantAction(ObjectNode result,JsonNode
	// json,Users user,EntityManager entityManager,EntityTransaction
	// entitytransaction);
	public ObjectNode search(Users user, String txnRefNumber, Long txnType, Long item, String status, Integer claimType,
			Integer payMode, String travelMode,
			String accomodationMode, String fromDate, String toDate, Long branch, Long project, Double fromAmount,
			Double toAmount, Integer remarks, Integer documents,
			Long claimSearchTxnQuestion, Integer claimSearchUserType, EntityManager entityManager)
			throws ParseException;

	public ObjectNode getCountries(Users user, Integer claimTravelType, String name, EntityManager entityManager);

	void saveClaimTransactionBRSDate(Users user, EntityManager entityManager, EntityTransaction entitytransaction,
			String transactionRef, String brsBankDate);
}
