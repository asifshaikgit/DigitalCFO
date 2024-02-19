package com.idos.dao;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.InvoiceReportModel;
import model.Organization;
import model.ReceiptAdvanceModal;
import model.Transaction;
import model.Users;

public interface ReceiptDAO extends BaseDAO {
	public ReceiptAdvanceModal generateReceipt(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception;

	public List<ReceiptAdvanceModal> generateRefundPaymentReceiptData(Organization org, Transaction transaction,
			EntityManager entityManager) throws Exception;

	public ReceiptAdvanceModal generateReceiptContent(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception;

	public Map<String, Object> generateGstInvoiceData(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception;

	public List<ReceiptAdvanceModal> generateMultiInvoiceData(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception;
}
