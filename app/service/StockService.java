package service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IDOSException;
import model.Branch;
import model.Specifics;
import model.Transaction;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface StockService extends BaseService {
	ObjectNode getItemPresentStock(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction);

	double branchSellStockAvailableCombSales(ObjectNode result, Users user, EntityManager entityManager, JsonNode json)
			throws IDOSException;

	double getBranchItemPresentStock(ObjectNode result, Users user, EntityManager entityManager, long branchID,
			long sepecificID);

	Double getPurchaseStockForThisIncomeLinkedExpenseItem(Branch bnch, Specifics specf, Users user,
			EntityManager entityManager);

	Double getSellStockForThisIncomeItem(Branch bnch, Specifics specf, Users user, EntityManager entityManager);

	Double buyInventoryStockAvailable(ObjectNode result, long itemId, long branchId, Users user,
			EntityManager entityManager);

	Double getPurchaseStockTransferredItem(Branch bnch, Specifics specf, Users user, EntityManager entityManager);

	Double getPurchaseStockTransferredInProgressItem(Branch bnch, Specifics specf, Users user,
			EntityManager entityManager);

	ObjectNode getPeriodicInventoryInfo(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction);

	String exportPeriodicInventory(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction, String path) throws Exception;

	// ObjectNode transferStockBnchtoBnch(ObjectNode result,JsonNode json,Users
	// user,EntityManager entityManager,EntityTransaction
	// entitytransaction,Transaction transaction);4
	@Deprecated
	Transaction openingStockInventoryItem(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException;

	Double openingStockInProgress(Branch bnch, Specifics specf, Users user, EntityManager entityManager);

	Double getPurchaseOpeningStock(Branch bnch, Specifics specf, Users user, EntityManager entityManager);

	ObjectNode getReportInventoryInfo(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction);

	Double getPurchaseStockForThisIncomeLinkedExpenseItemDateRange(Branch bnch, Specifics specf, Users user,
			EntityManager entityManager, String fromDate, String toDate);

	Double getSellStockForThisIncomeItemDateRange(Branch bnch, Specifics specf, Users user, EntityManager entityManager,
			String fromDate, String toDate);

	Double getPurchaseStockForThisIncomeLinkedExpenseItemOnDateOrg(Branch bnch, Specifics specf, Users user,
			EntityManager entityManager, String presentDate);

	Double getSellStockForThisIncomeItemOnDateOrg(Branch bnch, Specifics specf, Users user, EntityManager entityManager,
			String presentDate);

	Double getPurchaseStockForThisIncomeLinkedExpenseItemOnDate(Branch bnch, Specifics specf, Users user,
			EntityManager entityManager, String presentDate);

	Double getSellStockForThisIncomeItemOnDate(Branch bnch, Specifics specf, Users user, EntityManager entityManager,
			String presentDate);

	String exportReportInventory(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction, String path) throws Exception;

	ObjectNode getReportAllInventoryInfo(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);

	List<Specifics> listAllInventoryItems(final Users user);

	String exportInventory(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction, String path) throws Exception;

	void insertTradingInventory(Transaction transaction, Users user, EntityManager entityManager) throws IDOSException;

	ObjectNode displayDetailInventory(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction);

	void getMidInventory(ObjectNode result, JsonNode json, Users user, EntityManager em) throws IDOSException;

	void getTxnLevelInventory(ObjectNode result, JsonNode json, Users user, EntityManager em) throws IDOSException;

	double getSellStockForInventoryItem(Branch bnch, Date date, Specifics specf, Users user,
			EntityManager entityManager) throws IDOSException;

	double getPurchaseStockForInventoryItem(Branch bnch, Date date, Specifics specf, Users user,
			EntityManager entityManager);

	double checkIfCurrentItemPartOfPreviouslyEnteredCombSalesItem(EntityManager em, Users user,
			Specifics combSalesPrevious, Specifics itemCurrent);

	double getbranchSellStockAvailableCombSales(ObjectNode result, long sepecificID, long branchID,
			String selectedTxnDate, long inputQty, String txnForItemStr, Users user, EntityManager em);

}
