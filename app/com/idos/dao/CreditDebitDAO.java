package com.idos.dao;

import com.idos.util.IDOSException;
import model.Transaction;
import model.TransactionPurpose;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

/**
 * Created by Sunil Namdev on 17-01-2018.
 */
public interface CreditDebitDAO extends BaseDAO {
    Transaction submitForApproval(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

    Transaction submitForApprovalVendor(Users user, JsonNode json, EntityManager em, EntityTransaction et,
            TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException;
}
