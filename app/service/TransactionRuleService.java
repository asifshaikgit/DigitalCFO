package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface TransactionRuleService extends BaseService {
	public ObjectNode ruleBasedUserExistence(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);
}
