package service;

import com.idos.util.IDOSException;
import model.PurchaseRequisitionTxnModel;
import model.Transaction;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

/**
 * @author Harish Kumar created on 01.05.2023
 */
public interface PurchaseRequisitionTxnService extends BaseService{
    //PurchaseRequisitionTxnModel submitForApproval(Users user, JsonNode json, EntityManager entityManager,  ObjectNode result) throws IDOSException;
    PurchaseRequisitionTxnModel approverAction(Users user, EntityManager em, JsonNode json, ObjectNode result) throws IDOSException;
    
    PurchaseRequisitionTxnModel submitForApprovalPurchaseRequisition(Users user, JsonNode json, EntityManager em, ObjectNode result) throws IDOSException;
    void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException;
}

