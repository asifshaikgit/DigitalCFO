package com.idos.dao;

import com.idos.dao.profitandloss.ProfitLossDAO;
import com.idos.dao.profitandloss.ProfitLossDAOImpl;
import com.idos.dao.trialbalance.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import service.*;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import play.Application;
import javax.persistence.EntityManager;

/**
 * Created by Sunil K. Namdev on 07-09-2017.
 */
public interface BaseDAO {
    Logger log = Logger.getLogger("dao");
    JPAApi jpaApi = null; // Initialize to null initially
    Application application = null; // Initialize to null initially
    // public EntityManager entityManager =
    // EntityManagerProvider.getEntityManager();

    /*
     * default void setjpaApi(JPAApi jpaApiInstance) {
     * jpaApi = jpaApiInstance;
     * }
     * default void setApplication(Application applicationInstance) {
     * application = applicationInstance;
     * }
     */
    // JPAApi jpaApi=new JPAApi();
    // Application application=new Application();
    // JPAApi jpaApi = JPAApiProvider.get();
    GenericDAO genericDao = new GenericJpaDAO();
    // jpaApi jpaApi = play.api.Play.current().injector().instanceOf(jpaApi.class);
    // Application application();
    TransactionDAO TRANSACTION_DAO = new TransactionDAOImpl();
    CreateTrialBalanceDAO CREATE_TRIAL_BALANCE_DAO = new CreateTrialBalanceDAOImpl();
    ChartOfAccountsDAO coaDAO = new ChartOfAccountsDAOImpl();
    VendorDAO VENDOR_DAO = new VendorDAOImpl();
    CustomerDAO CUSTOMER_DAO = new CustomerDAOImpl();
    SpecificsDAO SPECIFICS_DAO = new SpecificsDAOImpl();
    ClaimItemDetailsDAO CLAIM_DETAILS_DAO = new ClaimItemDetailsDAOImpl();
    InterBranchTransferDAO INTER_BRANCH_TRANSFER_DAO = new InterBranchTransferDAOImpl();
    InventoryDAO INVENTORY_DAO = new InventoryDAOImpl();
    TransactionItemsService transactionItemsService = new TransactionItemsServiceImpl();
    TrialBalanceService trialBalanceService = new TrialBalanceServiceImpl();
    BranchCashService branchCashService = new BranchCashServiceImpl();
    BranchBankService branchBankService = new BranchBankServiceImpl();
    ClaimSettlementDAO CLAIM_SETTLEMENT_DAO = new ClaimSettlementDAOImpl();
    StockService STOCK_SERVICE = new StockServiceImpl();
    SellTransactionDAO SELL_TRANSACTION_DAO = new SellTransactionDAOImpl();
    OrganizationDAO ORGANIZATION_DAO = new OrganizationDAOImpl();
    SingleUserDAO SINGLE_USER_DAO = new SingleUserDAOImpl(application);
    VendorTdsSetupDAO VENDOR_TDS_DAO = new VendorTdsSetupDAOImpl();
    VendCustBillWiseOpeningBalanceDAO BILLWISE_OPENING_BAL_DAO = new VendCustBillwiseOpeningBalanceDAOImpl();
    // VendCustBranchWiseAdvanceBalanceDAO BRACHWISE_ADVANCE_BAL_DAO = new
    // VendCustBranchWiseAdvBalanceDAOImpl();
    ClaimsDAO CLAIM_DAO = new ClaimsDAOImpl();
    PjeInventoryDAO PJE_INVENTORY_DAO = new PjeInventoryDAOImpl();
    CreateTrialBalance4ClaimDAO TRIAL_BALANCE_CLAIMS = new CreateTrialBalance4ClaimDAOImpl();
    ProvisionJournalEntryDAO PROVISION_JOURNAL_ENTRY_DAO = new ProvisionJournalEntryDAOImpl();
    TransactionService TRANSACTION_SERVICE = new TransactionServiceImpl();
    InvoiceDAO INVOICE_DAO = new InvoiceDAOImpl();
    TransactionService transactionService = new TransactionServiceImpl();
    BillOfMaterialTxnDAO BILL_OF_MATERIAL_TXN_DAO = new BillOfMaterialTxnDAOImpl();
    CreatePurchaseOrderTxnDAO CREATE_PURCHASE_ORDER_TXN_DAO = new CreatePurchaseOrderTxnDAOImpl();
    CreatePurchaseRequisitionTxnDAO CREATE_PURCHASE_REQUISITION_TXN_DAO = new CreatePurchaseRequisitionTxnDAOImpl();
    PayrollDAO PAYROLL_DAO = new PayrollDAOImpl();
    GstTaxDAO GST_TAX_DAO = new GstTaxDAOImpl();
    FileUploadDAO FILE_UPLOAD_DAO = new FileUploadDAOImpl();
    TransactionItemDAO TRANSACTION_ITEM_DAO = new TransactionItemDAOImpl();
    FileUploadService FILE_UPLOAD_SERVICE = new FileUploadServiceImpl();
    ProfitLossDAO PROFIT_LOSS_DAO = new ProfitLossDAOImpl();
    BuyTransactionService BUY_TRANSACTION_SERVICE = new BuyTransactionServiceImpl();
    TrialBalanceDAO TRIAL_BALANCE_DAO = new TrialBalanceDAOImpl();
    TrialBalanceSpecificDAO TRIAL_BALANCE_SPECIFIC_DAO = new TrialBalanceSpecificDAOImpl();
    TrialBalanceCashDAO TRIAL_BALANCE_CASH_DAO = new TrialBalanceCashDAOImpl();
    TrialBalanceBankDAO TRIAL_BALANCE_BANK_DAO = new TrialBalanceBankDAOImpl();
    TrialBalanceVendorCustomerDAO TRIAL_BALANCE_VENDOR_CUSTOMER_DAO = new TrialBalanceVendorCustomerDAOImpl();
    TrialBalanceTdsDAO TRIAL_BALANCE_TDS_DAO = new TrialBalanceTdsDAOImpl();
    TrialBalanceGstTaxDAO TRIAL_BALANCE_GST_TAX_DAO = new TrialBalanceGstTaxDAOImpl();
    TrialBalanceEmpAdvancePaidDAO TRIAL_BALANCE_EMP_ADVANCE_PAID_DAO = new TrialBalanceEmpAdvancePaidDAOImpl();
    TrialBalanceEmpClaimDAO TRIAL_BALANCE_EMP_CLAIM_DAO = new TrialBalanceEmpClaimDAOImpl();
    // TrialBalancePayrollDAO TRIAL_BALANCE_PAYROLL_DAO = new
    // TrialBalancePayrollDAOImpl();
    ChartOfAccountsService CHART_OF_ACCOUNTS_SERVICE = new ChartOfAccountsServiceImpl();
}
