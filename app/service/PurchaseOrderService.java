package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.PurchaseOrderTxnModel;
import model.Transaction;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.IDOSException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public interface PurchaseOrderService extends BaseService {
	/*TransactionItemsService transactionItemsService = new TransactionItemsServiceImpl();

	Transaction submitForApprovalPurchaseOrder(Users user, JsonNode json, EntityManager entityManager,
			EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

	public String createPurchaseOrderJSON(Transaction transaction, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException;

	public void sendPurchaseOrderProcessingDone(Transaction transaction, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException;*/
	PurchaseOrderTxnModel approverAction(Users user, EntityManager em, JsonNode json, ObjectNode result) throws IDOSException;
	PurchaseOrderTxnModel submitForApprovalPurchaseOrder(Users user, JsonNode json, EntityManager em, ObjectNode result) throws IDOSException;
	void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException;
}
