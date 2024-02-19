package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IDOSException;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.dao.EmployeeAdvanceForExpensesDAO;
import com.idos.dao.EmployeeAdvanceForExpensesDAOImpl;

public class EmployeeAdvanceForExpensesServiceImpl implements EmployeeAdvanceForExpensesService {

        @Override
        public ObjectNode employeeAdvanceForExpenseItems(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager) {
                result = employeeAdvanceForExpenseDAO.employeeAdvanceForExpenseItems(result, json, user,
                                entityManager);
                return result;
        }

        @Override
        public ObjectNode displayUserEligibility(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager) {
                log.log(Level.FINE, "============ Start");
                result = employeeAdvanceForExpenseDAO.displayUserEligibility(result, json, user, entityManager);
                return result;
        }

        @Override
        public ObjectNode showExpenseClaimDetails(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager) {
                log.log(Level.FINE, "============ Start");
                result = employeeAdvanceForExpenseDAO.showExpenseClaimDetails(result, json, user, entityManager);
                return result;
        }

        @Override
        public ObjectNode submitForApproval(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
                        EntityTransaction entitytransaction) throws IDOSException {
                return employeeAdvanceForExpenseDAO.submitForApproval(result, json, user, entityManager,
                                entitytransaction);
        }

        @Override
        public ObjectNode approverAction(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
                        EntityTransaction entitytransaction) throws IDOSException {
                log.log(Level.FINE, "============ Start");
                result = employeeAdvanceForExpenseDAO.approverAction(result, json, user, entityManager,
                                entitytransaction);
                return result;
        }

        @Override
        public ObjectNode populateUserUnsettledExpenseAdvances(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager) {
                log.log(Level.FINE, "============ Start");
                result = employeeAdvanceForExpenseDAO.populateUserUnsettledExpenseAdvances(result, json, user,
                                entityManager);
                return result;
        }

        @Override
        public ObjectNode displayUnsettledAdvances(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager) {
                log.log(Level.FINE, "============ Start");
                result = employeeAdvanceForExpenseDAO.displayUnsettledAdvances(result, json, user, entityManager);
                return result;
        }

        @Override
        public ObjectNode expAdvanceSettlementAccountantAction(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager, EntityTransaction entitytransaction) throws IDOSException {
                result = employeeAdvanceForExpenseDAO.expAdvanceSettlementAccountantAction(result, json, user,
                                entityManager,
                                entitytransaction);
                return result;
        }

        @Override
        public ObjectNode getUserExpenseItemReimbursementAmountKl(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager, EntityTransaction entitytransaction) {
                log.log(Level.FINE, "============ Start");
                result = employeeAdvanceForExpenseDAO.getUserExpenseItemReimbursementAmountKl(result, json, user,
                                entityManager,
                                entitytransaction);
                return result;
        }

        @Override
        public ObjectNode reimbursementApproverAction(ObjectNode result, JsonNode json, Users user,
                        EntityManager entityManager, EntityTransaction entitytransaction) throws IDOSException {
                log.log(Level.FINE, "============ Start");
                result = employeeAdvanceForExpenseDAO.reimbursementApproverAction(result, json, user, entityManager,
                                entitytransaction);
                return result;
        }
}
