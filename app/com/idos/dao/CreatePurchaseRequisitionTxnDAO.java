package com.idos.dao;

import model.Branch;
import model.Specifics;
import model.Users;
import com.idos.util.IDOSException;
import model.PurchaseRequisitionTxnModel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;

/**
 * @author Sunil Namdev created on 8.3.2019
 */
public interface CreatePurchaseRequisitionTxnDAO extends BaseDAO{
    PurchaseRequisitionTxnModel approverAction(Users user, EntityManager em, JsonNode json, ObjectNode result) throws IDOSException;
    PurchaseRequisitionTxnModel submitForApproval(Users user, JsonNode json, EntityManager entityManager, ObjectNode result) throws IDOSException;
    void sendWebSocketResponse4Bom(PurchaseRequisitionTxnModel bomTxn, Users user, ObjectNode result);
    void setApproverEmailDetails(Users user, EntityManager em, PurchaseRequisitionTxnModel bomTxn, Branch branch, Specifics item);
}