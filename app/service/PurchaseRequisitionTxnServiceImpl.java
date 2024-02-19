package service;

import actor.CreatorActor;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.PurchaseRequisitionTxnModel;
import model.Transaction;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.WebSocket;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Harish Kumar created on 01.05.2023
 */
public class PurchaseRequisitionTxnServiceImpl implements PurchaseRequisitionTxnService {
    @Override
	public PurchaseRequisitionTxnModel approverAction(Users user, EntityManager em, JsonNode json, ObjectNode result) throws IDOSException {
		return CREATE_PURCHASE_REQUISITION_TXN_DAO.approverAction(user, em, json, result);
	}

	@Override
	public PurchaseRequisitionTxnModel submitForApprovalPurchaseRequisition(Users user, JsonNode json, EntityManager em, ObjectNode result) throws IDOSException {
		return CREATE_PURCHASE_REQUISITION_TXN_DAO.submitForApproval(user, json, em, result);
	}
    
    @Override
	public void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException {
		CREATE_PURCHASE_REQUISITION_TXN_ITEM_DAO.getListOfTxnItems(user, em, txnId, txnItemsAn);
	}
	
}

