package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.dao.TransactionRuleDAO;
import com.idos.dao.TransactionRuleDAOImpl;

public class TransactionRuleServiceImpl implements TransactionRuleService {

	@Override
	public ObjectNode ruleBasedUserExistence(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) {
		result = txnRuleDao.ruleBasedUserExistence(result, json, user, entityManager);
		return result;
	}
}
