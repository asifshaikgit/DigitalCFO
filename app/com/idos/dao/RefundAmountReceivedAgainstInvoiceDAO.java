package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;

import com.idos.util.IDOSException;

import model.Transaction;
import model.Users;

public interface RefundAmountReceivedAgainstInvoiceDAO extends BaseDAO {

	Transaction submitForApprovalRefundAmountRecived(Users user, JsonNode json, EntityManager entityManager,
			EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

	void insertMultipleItemsRefundAmountReceived(EntityManager entityManager, Users user, JSONArray arrJSON,
			Transaction transaction) throws IDOSException;
}
