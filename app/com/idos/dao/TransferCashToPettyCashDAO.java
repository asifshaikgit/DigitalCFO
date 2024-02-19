package com.idos.dao;

import com.idos.util.IDOSException;
import model.Transaction;
import model.TransactionPurpose;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @author Sunil K. Namdev created on 07.11.2019
 */
public interface TransferCashToPettyCashDAO extends BaseDAO {
    Transaction submitForApproval(Users user, JsonNode json, EntityManager em, TransactionPurpose txnPurposeObj,
            ObjectNode result) throws IDOSException;
}
