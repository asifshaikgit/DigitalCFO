package com.idos.dao;

import com.idos.util.IDOSException;
import model.Transaction;
import model.TransactionPurpose;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.Date;

/**
 * Created by Ankush Sapkal.
 */
public interface RefundAdvanceDAO extends BaseDAO {
        Transaction submitForApprovalRefundAdvanceRecived(Users user, JsonNode json, EntityManager entityManager,
                        EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

        // public void insertMultipleItemsRefundAdvanceReceived(EntityManager
        // entityManager, Users user, JSONArray arrJSON,
        // Transaction transaction, Date txnDate, String txnInvoice, Long
        // openingBalAdvId)
        // throws IDOSException;
        public void insertMultipleItemsRefundAdvanceReceived(EntityManager entityManager, Users user, JSONArray arrJSON,
                        Transaction transaction, Date txnDate)
                        throws IDOSException;
}
