package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Organization;
import model.ReceiptAdvanceModal;
import model.Transaction;
import model.Users;

public class ReceiptServiceImpl implements ReceiptService {
	@Override
	public List<ReceiptAdvanceModal> generateReceipt(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception {
		List<ReceiptAdvanceModal> receiptAdvanceModelList = new ArrayList<ReceiptAdvanceModal>();
		ReceiptAdvanceModal receiptAdvanceModal = RECEIPT_DAO.generateReceipt(result, json, user, entityManager,
				entitytransaction);
		receiptAdvanceModelList.add(receiptAdvanceModal);
		return receiptAdvanceModelList;
	}

	@Override
	public List<ReceiptAdvanceModal> generateRefundPaymentReceiptData(Organization org, Transaction transaction,
			EntityManager entityManager) throws Exception {
		List<ReceiptAdvanceModal> receiptAdvanceModelList = RECEIPT_DAO.generateRefundPaymentReceiptData(org,
				transaction, entityManager);
		return receiptAdvanceModelList;
	}

	@Override
	public Map<String, Object> getGstReceiptParams(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception {
		Map<String, Object> generateGstInvoiceData = RECEIPT_DAO.generateGstInvoiceData(result, json, user,
				entityManager, entitytransaction);
		return generateGstInvoiceData;
	}

	@Override
	public List<ReceiptAdvanceModal> generateReceiptContent(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception {
		List<ReceiptAdvanceModal> receiptAdvanceModelList = new ArrayList<ReceiptAdvanceModal>();
		ReceiptAdvanceModal receiptAdvanceModalNew = RECEIPT_DAO.generateReceiptContent(result, json, user,
				entityManager, entitytransaction);
		receiptAdvanceModelList.add(receiptAdvanceModalNew);
		return receiptAdvanceModelList;
	}

	@Override
	public List<ReceiptAdvanceModal> generateMultiInvoiceData(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception {
		List<ReceiptAdvanceModal> receiptAdvanceModelList = new ArrayList<ReceiptAdvanceModal>();
		receiptAdvanceModelList = RECEIPT_DAO.generateMultiInvoiceData(result, json, user, entityManager,
				entitytransaction);
		return receiptAdvanceModelList;
	}

}
