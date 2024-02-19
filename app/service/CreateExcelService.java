package service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.IdosProvisionJournalEntry;
import com.fasterxml.jackson.databind.JsonNode;

import model.Organization;
import model.Transaction;
import model.Users;

public interface CreateExcelService extends BaseService {
	public String PJE_DEBIT_CREDIT_ITEMS_DETAILS_QUERY = "select obj from ProvisionJournalEntryDetail obj where obj.provisionJournalEntry.id = ?1 and obj.isDebit = ?2 and obj.presentStatus=1";

	public String createbudgetexcel(Organization org, EntityManager entityManager, String path, String sheetName)
			throws Exception;

	String createtransactionexcel(Users user, JsonNode json, EntityManager entityManager, String path, String sheetName,
			List<Transaction> sellTxnList, List<Transaction> buyTxnList, List<Transaction> otherTxnList,
			List<IdosProvisionJournalEntry> pjeTxnList) throws Exception;

	File createtransactioncsv(Users user, JsonNode json, EntityManager entityManager, String path, String fileName,
			List<Transaction> txnList, List<IdosProvisionJournalEntry> pjeTxnList) throws Exception;

	public String createOrgCOAExcel(Organization org, EntityManager entityManager, String path, String sheetName)
			throws Exception;

	public String createOrgCOATemplateExcel(Users user, EntityManager entityManager, int coaType, String path,
			String sheetName) throws Exception;

	public String createOrgCustomerTemplateExcel(Users user, EntityManager entityManager, String path, String sheetName)
			throws Exception;

	public String createOrgVendorCustomerExcel(Organization org, int type, String path, String sheetName, EntityManager entityManager)
			throws Exception;

	public String createBudgetDetails(Organization org, String path, String sheetName) throws Exception;

	public String createOrgVendorTemplateExcel(Users user, EntityManager entityManager, String path, String sheetName)
			throws Exception;

	public String createOrgBranchTemplateExcel(Users user, EntityManager entityManager, String path, String sheetName)
			throws FileNotFoundException, IOException;

	public String createTransactionTemplateExcel(Users user, EntityManager entityManager, String path, String sheetName)
			throws FileNotFoundException, IOException;

	public String createRecievePaymentFromCustomerTransactionTemplateExcel(Users user, EntityManager entityManager,
			String path, String sheetName) throws Exception;

	// public String createOrgTransactionTemplateExcel(Users user, EntityManager
	// entityManager, String path,
	// String sheetName, Date fromTransDate, Date toTransDate) throws Exception;
	public File createOrgTransactionSellAndRecieveAdvanceData(Users user, EntityManager entityManager, String path,
			String fileName, Date fromTransDate, Date toTransDate) throws Exception;

	// public File createOrgTransactionSellAndRecieveAdvanceData(Users user,
	// EntityManager entityManager, String path,
	// String fileName) throws Exception;
	public File createOrgBuySideTransactionData(Users user, EntityManager entityManager, String path, String fileName,
			Date fromTransDate, Date toTransDate) throws Exception;

	// public File createOrgBuySideTransactionData(Users user, EntityManager
	// entityManager, String path, String fileName) throws Exception;
	public String createSellOnCashTransactionTemplateExcel(Users user, EntityManager entityManager, String path,
			String sheetName) throws FileNotFoundException, IOException;

	public String createBuyOnCreditTransactionTemplateExcel(Users user, EntityManager entityManager, String path,
			String sheetName) throws FileNotFoundException, IOException;

	public String createBuyOnCashTransactionTemplateExcel(Users user, EntityManager entityManager, String path,
			String sheetName) throws FileNotFoundException, IOException;

	public String createPayVendorTransactionTemplateExcel(Users user, EntityManager entityManager, String path,
			String sheetName) throws FileNotFoundException, IOException;

	public String createOrgTransactionSellAndRecieveAdvanceDataXlsx(Users user, EntityManager entityManager,
			String path, String sheetName, Date fromTransDate, Date toTransDate)
			throws FileNotFoundException, IOException;

	public String createOrgBuySideTransactionDataXlsx(Users user, EntityManager entityManager, String path,
			String sheetName, Date fromTransDate, Date toTransDate) throws Exception;
}
