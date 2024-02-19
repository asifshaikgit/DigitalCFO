package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Application;

public interface CashNBankDAO extends BaseDAO {
	public ObjectNode displayCashBook(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);

	public ObjectNode displayBankBook(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);

	public String exportCashAndBankBook(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction, String path, Application application);
}
