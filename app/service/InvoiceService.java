package service;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.idos.util.IDOSException;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;

public interface InvoiceService extends BaseService {
	List<InvoiceReportModel> generateInvoiceData(Organization org, Transaction transaction, EntityManager entityManager)
			throws Exception;

	Map<String, Object> getInvoiceParams(Transaction transaction, JsonNode json, EntityManager entityManager)
			throws Exception;

	List<InvoiceReportModel> generateGstInvoiceData(Organization org, Transaction transaction,
			EntityManager entityManager, JsonNode json, TransactionInvoice invoiceLog) throws Exception;

	List<InvoiceReportModel> generateBuyGstInvoiceData(Organization org, Transaction transaction,
			EntityManager entityManager) throws Exception;

	List<InvoiceAdvanceModel> getAdvTxnGstInvoice(Organization org, Transaction transaction,
			EntityManager entityManager) throws Exception;

	Map<String, Object> getGstInvoiceParams(Transaction transaction, JsonNode json, List<InvoiceReportModel> datas,
			TransactionInvoice invoiceLog, Users user, EntityManager entityManager) throws Exception;

	int saveInvoiceLog(Users user, EntityManager entityManager, Transaction txn, String fileName, JsonNode json)
			throws IDOSException;

	Map<String, Object> getGRNParams(Transaction transaction, JsonNode json, List<GRNoteModel> datas,
			TransactionInvoice invoiceLog) throws Exception;

	List<GRNoteModel> generateGRNDatas(Organization org, Transaction transaction, EntityManager entityManager)
			throws Exception;

	List<createPurchaseOrderModel> generateDataForCreatePO(Organization org, BillOfMaterialTxnModel transaction,
			EntityManager entityManager, JsonNode json, TransactionInvoice invoiceLog) throws Exception;

	Map<String, Object> getCreatePOParams(BillOfMaterialTxnModel transaction, JsonNode json,
			List<createPurchaseOrderModel> datas, TransactionInvoice invoiceLog, Users user) throws Exception;
}
