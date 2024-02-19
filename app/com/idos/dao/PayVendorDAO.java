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
 * @author Sunil K. Namdev created on 06.11.2019
 */
public interface PayVendorDAO extends BaseDAO {
    String SPEC_QUERY_FOR_MULTI_INVOICE = "select obj from Specifics obj where obj.organization.id =?1 and obj.identificationForDataValid =?2 and obj.presentStatus=1";

    Transaction submitForApproval(Users user, JsonNode json, EntityManager em, TransactionPurpose txnPurposeObj,
            ObjectNode result) throws IDOSException;
}
