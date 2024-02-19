package com.idos.dao;

import com.idos.util.IDOSException;
import model.Transaction;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

/**
 * Created by Sunil K. Namdev on 18-01-2018.
 */
public interface CancelInvoiceTxnDAO extends BaseDAO {
    Transaction submitForCancellation(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;
}
