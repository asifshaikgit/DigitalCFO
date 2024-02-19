package service;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.idos.util.IDOSException;
import model.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.idos.dao.InvoiceDAO;
import com.idos.dao.InvoiceDAOImpl;
import com.fasterxml.jackson.databind.JsonNode;

public class InvoiceServiceImpl implements InvoiceService {

	@Override
	public List<InvoiceReportModel> generateInvoiceData(Organization org, Transaction transaction,
			EntityManager entityManager) throws Exception {
		List<InvoiceReportModel> invoiceReportModel = invDAO.getSellTxnInvoice(org, transaction, entityManager);
		return invoiceReportModel;
	}

	@Override
	public Map<String, Object> getInvoiceParams(Transaction transaction, JsonNode json, EntityManager entityManager)
			throws Exception {
		Map<String, Object> invoiceParams = invDAO.getInvoiceParams(transaction, json, entityManager);
		return invoiceParams;
	}

	@Override
	public List<InvoiceReportModel> generateGstInvoiceData(Organization org, Transaction transaction,
			EntityManager entityManager, JsonNode json, TransactionInvoice invoiceLog) throws Exception {
		return invDAO.getSellTxnGstInvoice(org, transaction, entityManager, json, invoiceLog);
	}

	@Override
	public List<InvoiceReportModel> generateBuyGstInvoiceData(Organization org, Transaction transaction,
			EntityManager entityManager) throws Exception {
		return invDAO.getBuyTxnGstInvoice(org, transaction, entityManager);
	}

	@Override
	public List<InvoiceAdvanceModel> getAdvTxnGstInvoice(Organization org, Transaction transaction,
			EntityManager entityManager) throws Exception {
		return invDAO.getAdvTxnGstInvoice(org, transaction, entityManager);
	}

	@Override
	public Map<String, Object> getGstInvoiceParams(Transaction transaction, JsonNode json,
			List<InvoiceReportModel> datas, TransactionInvoice invoiceLog, Users user, EntityManager entityManager)
			throws Exception {
		Map<String, Object> invoiceParams = invDAO.getGstInvoiceParams(transaction, json, datas, invoiceLog, user,
				entityManager);
		return invoiceParams;
	}

	@Override
	public int saveInvoiceLog(Users user, EntityManager entityManager, Transaction txn, String fileName, JsonNode json)
			throws IDOSException {
		return invDAO.saveInvoiceLog(user, entityManager, txn, fileName, json);
	}

	@Override
	public Map<String, Object> getGRNParams(Transaction transaction, JsonNode json, List<GRNoteModel> datas,
			TransactionInvoice invoiceLog) throws Exception {
		Map<String, Object> invoiceParams = invDAO.getGRNParams(transaction, json, datas, invoiceLog);
		return invoiceParams;
	}

	@Override
	public List<GRNoteModel> generateGRNDatas(Organization org, Transaction transaction, EntityManager entityManager)
			throws Exception {
		return invDAO.generateGRNDatas(org, transaction, entityManager);
	}

	@Override
	public List<createPurchaseOrderModel> generateDataForCreatePO(Organization org, BillOfMaterialTxnModel transaction,
			EntityManager entityManager, JsonNode json, TransactionInvoice invoiceLog) throws Exception {

		return invDAO.generateDataForCreatePO(org, transaction, entityManager, json, invoiceLog);
	}

	@Override
	public Map<String, Object> getCreatePOParams(BillOfMaterialTxnModel transaction, JsonNode json,
			List<createPurchaseOrderModel> datas, TransactionInvoice invoiceLog, Users user) throws Exception {
		return invDAO.getCreatePOParams(transaction, json, datas, invoiceLog, user);
	}

}
