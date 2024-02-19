package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IDOSException;

import model.TransactionPurpose;
import model.Users;

public interface ReversalOfITCDAO extends BaseDAO {
	Transaction submitForAccounting(Users user, JsonNode json, EntityManager entityManager,
			EntityTransaction entitytransaction, TransactionPurpose usertxnPurpose, ObjectNode result)
			throws IDOSException;

}