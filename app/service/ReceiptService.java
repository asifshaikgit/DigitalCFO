package service;

import java.util.List;
import java.util.Map;

import model.InvoiceReportModel;
import model.Organization;
import model.ReceiptAdvanceModal;
import model.Transaction;
import model.Users;
import com.typesafe.config.Config;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

public interface ReceiptService extends BaseService {
	public List<ReceiptAdvanceModal> generateReceipt(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception;

	public List<ReceiptAdvanceModal> generateRefundPaymentReceiptData(Organization org, Transaction transaction,
			EntityManager entityManager) throws Exception;

	public List<ReceiptAdvanceModal> generateReceiptContent(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception;

	public Map<String, Object> getGstReceiptParams(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception;

	public List<ReceiptAdvanceModal> generateMultiInvoiceData(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) throws Exception;
}
