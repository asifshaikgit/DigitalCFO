package service;

import com.idos.dao.*;
import com.idos.dao.balancesheet.BalanceSheetDAO;
import com.idos.dao.balancesheet.BalanceSheetDAOImpl;
import com.idos.dao.plbscoamapper.PLBSCOAMapperDAO;
import com.idos.dao.plbscoamapper.PLBSCOAMapperDAOImpl;
import com.idos.dao.profitandloss.ProfitLossDAO;
import com.idos.dao.profitandloss.ProfitLossDAOImpl;
import com.idos.dao.trialbalance.*;
import com.idos.dao.upload.UploadDataDao;
import com.idos.dao.upload.UploadDataDaoImpl;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import play.Application;

/**
 * Created by Sunil K. Namdev on 07-09-2017.
 */
public interface BaseService {
    JPAApi jpaApi = null; // Initialize to null initially
    Application application = null;// Initialize to null initially

    /*
     * default void setjpaApi(JPAApi jpaApiInstance) {
     * jpaApi = jpaApiInstance;
     * }
     * default void setApplication(Application applicationInstance) {
     * application = applicationInstance;
     * }
     */
    Config config = ConfigFactory.load();
    Logger log = Logger.getLogger("service");

    // jpaApi jpaApi = play.api.Play.current().injector().instanceOf(jpaApi.class);
    // Application application();

    GenericDAO genericDAO = new GenericJpaDAO();
    BranchBankDAO branchBankDAO = new BranchBankDAOImpl();
    ClaimsDAO claimsDAO = new ClaimsDAOImpl();
    CustomerDAO customerDao = new CustomerDAOImpl();
    CashierDAO cashierDao = new CashierDAOImpl();
    CashNBankDAO cashNBankDAO = new CashNBankDAOImpl(application);
    ChartOfAccountsDAO chartsOfAccountsDAO = new ChartOfAccountsDAOImpl();
    CreateTrialBalanceDAO CREATE_TRIAL_BALANCE_DAO = new CreateTrialBalanceDAOImpl();
    EmployeeAdvanceForExpensesDAO employeeAdvanceForExpenseDAO = new EmployeeAdvanceForExpensesDAOImpl();
    GstTaxDAO taxDao = new GstTaxDAOImpl();
    InvoiceDAO invDAO = new InvoiceDAOImpl();
    OnlineDAO onlineDAO = new OnlineDAOImpl();
    OperationalDAO operationalDao = new OperationalDAOImpl();
    OrganizationDAO ordDao = new OrganizationDAOImpl();
    OrgAnalyticsDAO analyticsDAO = new OrgAnalyticsDAOImpl();
    ProvisionJournalEntryDAO provisionJournalDAO = new ProvisionJournalEntryDAOImpl();
    PayrollDAO payrollDAO = new PayrollDAOImpl();
    ReceiptDAO RECEIPT_DAO = new ReceiptDAOImpl();
    ReceiveFromCustomerDAO RECEIVE_FROM_CUSTOMER_DAO = new ReceiveFromCustomerDAOImpl();
    PayAdvanceToVendorDAO PAY_TO_VENDOR_DAO = new PayAdvanceToVendorDAOImpl();
    SellerDAO SELLER_DAO = new SellerDAOImpl();
    SpecificsDAO specificDAO = new SpecificsDAOImpl();
    StockService stockService = new StockServiceImpl();
    SellTransactionDAO SELL_TRANSACTION_DAO = new SellTransactionDAOImpl();
    TransactionDAO transactionDao = new TransactionDAOImpl();
    TransactionRuleDAO txnRuleDao = new TransactionRuleDAOImpl();
    TrialBalanceDAO trialBalanceDao = new TrialBalanceDAOImpl();
    ValidateTxnDAO valdateTxnDao = new ValidateTxnDAOImpl();
    VendorDAO VENDOR_DAO = new VendorDAOImpl();
    BuyTransactionDAO BUY_TRANSACTION_DAO = new BuyTransactionDAOImpl();
    InventoryDAO INVENTORY_DAO = new InventoryDAOImpl();
    ProfitLossDAO PROFIT_LOSS_DAO = new ProfitLossDAOImpl();
    BalanceSheetDAO BALANCE_SHEET_DAO = new BalanceSheetDAOImpl();
    AgeingReportDAO AGEING_REPORT_DAO = new AgeingReportDAOImpl();
    CreditDebitDAO CREDIT_DEBIT_DAO = new CreditDebitDAOImpl();
    InterBranchTransferDAO INTER_BRANCH_TRANSFER_DAO = new InterBranchTransferDAOImpl();
    DynamicReportService dynReportService = new DynamicReportServiceImpl(application);
    ClaimItemDetailsDAO CLAIM_DETAILS_DAO = new ClaimItemDetailsDAOImpl();
    ClaimSettlementDAO CLAIM_SETTLEMENT_DAO = new ClaimSettlementDAOImpl();
    SingleUserDAO singleUserDAO = new SingleUserDAOImpl(application); // Single User
    RefundAdvanceDAO REFUND_ADVANCE_DAO = new RefundAdvanceDAOImpl();
    BranchCashService branchCashService = new BranchCashServiceImpl();
    BranchBankService branchBankService = new BranchBankServiceImpl();
    TrialBalanceService trialBalanceService = new TrialBalanceServiceImpl();
    RefundAmountReceivedAgainstInvoiceDAO REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE_DAO = new RefundAmountReceivedAgainstInvoiceDAOImpl();
    PLBSCoaMappingDAO PLBS_COA_MAPPING_DAO = new PLBSCoaMappingDAOImpl();
    UserSetupDAO USER_SETUP_DAO = new UserSetupDAOImpl();
    VendorTdsSetupDAO VENDOR_TDS_DAO = new VendorTdsSetupDAOImpl();
    VendCustBillWiseOpeningBalanceDAO BILLWISE_OPENING_BAL_DAO = new VendCustBillwiseOpeningBalanceDAOImpl();
    // VendCustBranchWiseAdvanceBalanceDAO BRANCHWISE_ADVANCE_BAL_DAO = new
    // VendCustBranchWiseAdvBalanceDAOImpl();
    ExceptionService expService = new ExceptionServiceImpl();
    SellInventoryDAO SELL_INVENTORY_DAO = new SellInventoryDAOImpl();
    BuyInventoryDAO BUY_INVENTORY_DAO = new BuyInventoryDAOImpl();
    CreateTrialBalance4ClaimDAO TRIAL_BALANCE_CLAIMS = new CreateTrialBalance4ClaimDAOImpl();
    PLBSCOAMapperDAO plbscoaMapperDao = new PLBSCOAMapperDAOImpl();
    TransactionViewDAO TRANSACTION_VIEW_DAO = new TransactionViewDAOImpl();
    CancelInvoiceTxnDAO CANCEL_INVOICE_TXN_DAO = new CancelInvoiceTxnDAOImpl();
    BillOfMaterialDAO BILL_OF_MATERIAL_DAO = new BillOfMaterialDAOImpl();
    BillOfMaterialTxnDAO BILL_OF_MATERIAL_TXN_DAO = new BillOfMaterialTxnDAOImpl();
    CreatePurchaseOrderTxnDAO CREATE_PURCHASE_ORDER_TXN_DAO = new CreatePurchaseOrderTxnDAOImpl();
    CreatePurchaseOrderTxnItemDAO CREATE_PURCHASE_ORDER_TXN_ITEM_DAO = new CreatePurchaseOrderTxnItemDAOImpl();
    CreatePurchaseRequisitionTxnDAO CREATE_PURCHASE_REQUISITION_TXN_DAO = new CreatePurchaseRequisitionTxnDAOImpl();
    CreatePurchaseRequisitionTxnItemDAO CREATE_PURCHASE_REQUISITION_TXN_ITEM_DAO = new CreatePurchaseRequisitionTxnItemDAOImpl();
    DigitalSignatureDAO DIGITAL_SIGNATURE_DAO = new DigitalSignatureDAOImpl();
    TrialBalanceLedgerDAO TRIAL_BALANCE_LEGDER_DAO = new TrialBalanceLedgerDAOImpl();
    BillOfMaterialTxnItemDAO BILL_OF_MATERIAL_TXN_ITEM_DAO = new BillOfMaterialTxnItemDAOImpl();
    FileUploadDAO FILE_UPLOAD_DAO = new FileUploadDAOImpl();
    PayVendorDAO PAY_VENDOR_DAO = new PayVendorDAOImpl();
    TransferCashToPettyCashDAO TRANSFER_CASH_TO_PETTY_CASH_DAO = new TransferCashToPettyCashDAOImpl();
    TransactionItemsService TRANSACTION_ITEMS_SERVICE = new TransactionItemsServiceImpl();
    TrialBalanceBankDAO TRIAL_BALANCE_BANK_DAO = new TrialBalanceBankDAOImpl();
    TransactionItemDAO TRANSACTION_ITEM_DAO = new TransactionItemDAOImpl();
    UploadDataDao UPLOAD_DAO = new UploadDataDaoImpl();
    BOMDAOImpl BOM_DAO = new BOMDAOImpl();
}
