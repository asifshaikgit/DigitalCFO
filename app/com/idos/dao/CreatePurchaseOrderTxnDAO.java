package com.idos.dao;

import com.idos.util.IDOSException;

import model.PurchaseOrderTxnModel;
import model.Branch;
import model.Specifics;
import model.Users;
import model.BillOfMaterialTxnModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @author Sunil Namdev created on 2.3.2019
 */
public interface CreatePurchaseOrderTxnDAO extends BaseDAO{
    String DEL_OLD_TXN_ITEMS_JPQL = "delete from BillOfMaterialTxnItemModel t1 where t1.organization.id= ?1 and t1.billOfMaterialTxn.id not in (?2)";
    BillOfMaterialTxnModel submitForApprovalPoNormal(Users user, JsonNode json, EntityManager entityManager, ObjectNode result) throws IDOSException;
    BillOfMaterialTxnModel submitForApprovalPoAgainstRequisition(Users user, JsonNode json, EntityManager entityManager, ObjectNode result) throws IDOSException;
    PurchaseOrderTxnModel approverAction(Users user, EntityManager em, JsonNode json, ObjectNode result) throws IDOSException;
    PurchaseOrderTxnModel submitForApproval(Users user, JsonNode json, EntityManager entityManager, ObjectNode result) throws IDOSException;
    void sendWebSocketResponse4Bom(PurchaseOrderTxnModel bomTxn, Users user, ObjectNode result);
    void setApproverEmailDetails(Users user, EntityManager em, PurchaseOrderTxnModel bomTxn, Branch branch, Specifics item);
}
