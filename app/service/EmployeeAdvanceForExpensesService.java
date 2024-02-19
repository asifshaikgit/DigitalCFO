package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IDOSException;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface EmployeeAdvanceForExpensesService extends BaseService {
	public ObjectNode employeeAdvanceForExpenseItems(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager);

	public ObjectNode displayUserEligibility(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);

	public ObjectNode submitForApproval(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException;

	public ObjectNode showExpenseClaimDetails(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager);

	public ObjectNode approverAction(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException;

	public ObjectNode populateUserUnsettledExpenseAdvances(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager);

	public ObjectNode displayUnsettledAdvances(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager);

	public ObjectNode expAdvanceSettlementAccountantAction(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws IDOSException;

	public ObjectNode getUserExpenseItemReimbursementAmountKl(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction);

	public ObjectNode reimbursementApproverAction(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws IDOSException;
}
