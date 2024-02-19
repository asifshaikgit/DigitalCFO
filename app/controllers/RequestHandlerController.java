package controllers;

import play.mvc.Result;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.libs.concurrent.HttpExecutionContext;
import java.util.concurrent.CompletableFuture;
import controllers.payroll.*;
import javax.inject.Inject;

import controllers.Karvy.*;
import controllers.Gstr.*;
import controllers.PLBSCOAMapper.*;
import controllers.ProfitLoss.*;
import controllers.BalanceSheet.*;
import play.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.locks.ReentrantLock;
import play.mvc.Results.*;
import service.EntityManagerProvider;
import javax.persistence.EntityManager;

public class RequestHandlerController extends Controller {

    private final ReentrantLock lock = new ReentrantLock();
    public final play.Application application;
    private final HttpExecutionContext httpExecutionContext;
    private EntityManager entityManager;

    @Inject
    public RequestHandlerController(HttpExecutionContext httpExecutionContext,
            play.Application application) {
        this.httpExecutionContext = httpExecutionContext;
        this.application = application;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    public Result handleRequest(Request req) {
        lock.lock();
        try {
            return executeRequestLogic(req).toCompletableFuture().join();
        } finally {
            entityManager.clear();
            lock.unlock();
        }
    }

    public Result handleFileRequest(Request req, String name) {
        lock.lock();
        try {
            return executeFileRequestLogic(req, name).toCompletableFuture().join();
        } finally {
            entityManager.clear();
            lock.unlock();
        }
    }

    private CompletionStage<Result> executeFileRequestLogic(Request req, String param) {
        String path = req.path();
        if (path.startsWith("/files/")) {
            return CompletableFuture.supplyAsync(
                    () -> new FileUploadController(application).serveFile(req, param),
                    httpExecutionContext.current());
        } else if (path.startsWith("/tax/getintaxes/")) {
            return CompletableFuture.supplyAsync(
                    () -> new GstTaxController(application).getGstInTaxesCess4Branch(Long.parseLong(param), req),
                    httpExecutionContext.current());
        } else {
            return CompletableFuture.completedFuture(notFound("Route not found"));
        }
    }

    private CompletionStage<Result> executeRequestLogic(Request req) {
        String path = req.path();
        switch (path.split("\\?")[0]) {
            case "/config/getcurrcountry":
                return CompletableFuture.supplyAsync(
                        () -> new ConfigurationController(application).getCountryCurrencyData(req),
                        httpExecutionContext.current());
            case "/config/getorgdatas":
                return CompletableFuture.supplyAsync(() -> new ConfigurationController(application).getDatas(req),
                        httpExecutionContext.current());
            case "/config/getorg":
                return CompletableFuture.supplyAsync(() -> new OrganizationController(application).getOrganization(req),
                        httpExecutionContext.current());
            case "/claims/userAdvancesTxnApprovedButNotAccountedCount":
                return CompletableFuture.supplyAsync(
                        () -> new ClaimController(application).userAdvancesTxnApprovedButNotAccountedCount(req),
                        httpExecutionContext.current());
            case "/config/allUsers":
                return CompletableFuture.supplyAsync(() -> new UserController(application).getAllUsers(req),
                        httpExecutionContext.current());
            case "/claims/getTravelStaticData":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).getTravelStaticData(req),
                        httpExecutionContext.current());
            case "/claims/getAvailableExpenseClaimItems":
                return CompletableFuture.supplyAsync(
                        () -> new ClaimController(application).getAvailableExpenseClaimItems(req),
                        httpExecutionContext.current());
            case "/claims/getAvailableTravelExpenseGroups":
                return CompletableFuture.supplyAsync(
                        () -> new ClaimController(application).getAvailableTravelExpenseGroups(req),
                        httpExecutionContext.current());
            case "/user/approverCashBankReceivablePayables":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).approverCashBankReceivablePayables(req),
                        httpExecutionContext.current());
            case "/config/getVendorData":
                return CompletableFuture.supplyAsync(() -> new VendorController(application).listVendor(req),
                        httpExecutionContext.current());
            case "/user/getBranchData":
                return CompletableFuture.supplyAsync(() -> new ParticularController(application).getUserBranchData(req),
                        httpExecutionContext.current());
            case "/user/getParticularsData":
                return CompletableFuture.supplyAsync(
                        () -> new ParticularController(application).getUserParticularData(req),
                        httpExecutionContext.current());
            case "/user/getProjectData":
                return CompletableFuture.supplyAsync(
                        () -> new ParticularController(application).getUserProjectData(req),
                        httpExecutionContext.current());
            case "/config/getsellreceicve":
                return CompletableFuture.supplyAsync(() -> new Application(application).sellReceive(req),
                        httpExecutionContext.current());
            case "/config/getdatasellreceicve":
                return CompletableFuture.supplyAsync(() -> new Application(application).getsellReceive(req),
                        httpExecutionContext.current());
            case "/vendor/listVendorGroup":
                return CompletableFuture.supplyAsync(() -> new VendorController(application).listVendorGroup(req),
                        httpExecutionContext.current());
            case "/data/getcoaexpenseitems":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application).getChartOfAccountsExpenseItems(req),
                        httpExecutionContext.current());
            case "/config/vendorDetails":
                return CompletableFuture.supplyAsync(() -> new VendorController(application).showVendorDetails(req),
                        httpExecutionContext.current());
            case "/vendor/addVendor":
                return CompletableFuture.supplyAsync(() -> new VendorController(application).saveVendor(req),
                        httpExecutionContext.current());
            case "/vendor/searchVendor":
                return CompletableFuture.supplyAsync(() -> new VendorController(application).searchVendor(req),
                        httpExecutionContext.current());
            case "/dashboard/getFinancials":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).getDashboardFinancial(req),
                        httpExecutionContext.current());
            case "/config/checkIfCombinationSalesIncomeItem":
                return CompletableFuture.supplyAsync(
                        () -> new SpecificsController(application).checkIfCombinationSalesIncomeItem(req),
                        httpExecutionContext.current());
            case "/chartOfAccounts/allChartOfAccounts":
                return CompletableFuture.supplyAsync(
                        () -> new SpecificsController(application).getAllChartOfAccounts(req),
                        httpExecutionContext.current());
            case "/config/itemDetails":
                return CompletableFuture.supplyAsync(() -> new SpecificsController(application).showItemsDetails(req),
                        httpExecutionContext.current());
            case "/specifics/getSupportDocLimit":
                return CompletableFuture.supplyAsync(() -> new SpecificsController(application).getSupportDocLimit(req),
                        httpExecutionContext.current());
            case "/config/getListOfCombinationSalesItems":
                return CompletableFuture.supplyAsync(
                        () -> new SpecificsController(application).getListOfCombinationSalesItems(req),
                        httpExecutionContext.current());
            case "/specifics/addSpecifics":
                return CompletableFuture.supplyAsync(() -> new SpecificsController(application).saveSpecifics(req),
                        httpExecutionContext.current());
            case "/specifics/incomeAvailableStock":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).availableIncomeItemStock(req),
                        httpExecutionContext.current());
            case "/data/getcoaincomeitems":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application).getChartOfAccountsIncomeItems(req),
                        httpExecutionContext.current());
            case "/transaction/gettaxandnetamt":
                return CompletableFuture.supplyAsync(
                        () -> new BuyTransactionController(application).getNetAmtAndTaxComponent(req),
                        httpExecutionContext.current());
            case "/transaction/getTxnItemsCustomerVendors":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getCreditCustomerVendorItems(req),
                        httpExecutionContext.current());
            case "/transaction/getklinvoices":
                return CompletableFuture.supplyAsync(() -> new TransactionController(application).getklInvoices(req),
                        httpExecutionContext.current());
            case "/vendor/vendorlocations":
                return CompletableFuture.supplyAsync(() -> new VendorController(application).getVendorLocations(req),
                        httpExecutionContext.current());
            case "/customer/customerlocations":
                return CompletableFuture.supplyAsync(
                        () -> new CustomerController(application).getCustomerLocations(req),
                        httpExecutionContext.current());
            case "/customer/searchCustomer":
                return CompletableFuture.supplyAsync(() -> new CustomerController(application).searchCustomer(req),
                        httpExecutionContext.current());
            case "/payroll/getUserDeductionData":
                return CompletableFuture.supplyAsync(() -> new PayrollController(application).getUserDeductionData(req),
                        httpExecutionContext.current());
            case "/payroll/getUserEarningsData":
                return CompletableFuture.supplyAsync(() -> new PayrollController(application).getUserEarningsData(req),
                        httpExecutionContext.current());
            case "/customerVendorAccount":
                return CompletableFuture.supplyAsync(() -> new ApplicationController(application).vendCustAccount(req),
                        httpExecutionContext.current());
            case "/cashconfig":
                return CompletableFuture.supplyAsync(
                        () -> new controllers.Application(application).cashconfiguration(req),
                        httpExecutionContext.current());
            case "/account":
                return CompletableFuture.supplyAsync(() -> new controllers.Application(application).account(req),
                        httpExecutionContext.current());
            case "/expense/getexpdatas":
                return CompletableFuture.supplyAsync(() -> new ExpenseController(application).expensesList(req),
                        httpExecutionContext.current());
            case "/expenseslist":
                return CompletableFuture.supplyAsync(() -> new ExpenseController(application).expenses(req),
                        httpExecutionContext.current());
            case "/expenses/particulars":
                return CompletableFuture.supplyAsync(() -> new ExpenseController(application).getParticulars(req),
                        httpExecutionContext.current());
            case "/expenses/allspecifics":
                return CompletableFuture.supplyAsync(() -> new ExpenseController(application).getAllSpecifics(req),
                        httpExecutionContext.current());
            case "/users/getroles":
                return CompletableFuture.supplyAsync(() -> new UserController(application).getRoles(req),
                        httpExecutionContext.current());
            case "/expenses/getExpenseDetails":
                return CompletableFuture.supplyAsync(() -> new ExpenseController(application).getExpenseDetails(req),
                        httpExecutionContext.current());
            case "/config/showChartOFAccount":
                return CompletableFuture.supplyAsync(
                        () -> new ConfigurationController(application).getChartOfAccount(req),
                        httpExecutionContext.current());
            case "/orgsalesdata1s3d5":
                return CompletableFuture.supplyAsync(
                        () -> new ApplicationController(application).getOrgDataForSales(req),
                        httpExecutionContext.current());
            case "/organization/getallorg":
                return CompletableFuture.supplyAsync(() -> new OperationController(application).getOranizationsList(),
                        httpExecutionContext.current());
            case "/user/userDetails":
                return CompletableFuture.supplyAsync(() -> new UserController(application).showUserDetails(req),
                        httpExecutionContext.current());
            case "/user/txnrule":
                return CompletableFuture.supplyAsync(() -> new UserController(application).setTxnCoaRule(req),
                        httpExecutionContext.current());
            case "/config/addUpdateTaxes":
                return CompletableFuture.supplyAsync(() -> new ConfigurationController(application).saveTax(),
                        httpExecutionContext.current());
            case "/transaction/getCompanyTemplate":
                return CompletableFuture.supplyAsync(
                        () -> new OrganizationController(application).getOrganizationTemplate(req),
                        httpExecutionContext.current());
            case "/transaction/validateTxnRefNo":
                return CompletableFuture.supplyAsync(() -> new TransactionController(application).validateTxnRefNo(req),
                        httpExecutionContext.current());
            case "/customer/listCustomerGroup":
                return CompletableFuture.supplyAsync(() -> new CustomerController(application).listCustomerGroup(req),
                        httpExecutionContext.current());
            case "/customer/getCustomerListForBranch":
                return CompletableFuture.supplyAsync(
                        () -> new CustomerController(application).getCustomerListForBranch(req),
                        httpExecutionContext.current());
            case "/customer/getCustomerListForBranchAndTypeOfSuply":
                return CompletableFuture.supplyAsync(
                        () -> new CustomerController(application).getCustomerListForBranchAndTypeOfSuply(req),
                        httpExecutionContext.current());
            case "/customer/getOpenignBalAdvCustomer":
                return CompletableFuture.supplyAsync(
                        () -> new CustomerController(application).getOpenignBalAdvCustomer(req),
                        httpExecutionContext.current());
            case "/customer/checkIfCustomerCreditLimitExceeded":
                return CompletableFuture.supplyAsync(
                        () -> new CustomerController(application).checkIfCustomerCreditLimitExceeded(req),
                        httpExecutionContext.current());
            case "/customer/customerSalesMonthWiseItemsData":
                return CompletableFuture.supplyAsync(
                        () -> new CustomerController(application).customerSalesMonthWiseItemsData(req),
                        httpExecutionContext.current());
            case "/vendor/customerGroupDetails":
                return CompletableFuture.supplyAsync(
                        () -> new CustomerController(application).customerGroupDetails(req),
                        httpExecutionContext.current());
            case "/user/getUserAdvClaim":
                return CompletableFuture.supplyAsync(
                        () -> new UserController(application).getUserOpeningBalAdvClaim(req),
                        httpExecutionContext.current());
            case "/expenses/specifics":
                return CompletableFuture.supplyAsync(() -> new ExpenseController(application).getSpecifics(req),
                        httpExecutionContext.current());
            case "/expenses/vendors":
                return CompletableFuture.supplyAsync(() -> new ExpenseController(application).getVendors(req),
                        httpExecutionContext.current());
            case "/expenses/getExpense":
                return CompletableFuture.supplyAsync(() -> new ExpenseController(application).getExpense(req),
                        httpExecutionContext.current());
            case "/config/getCompanyOrgList":
                return CompletableFuture.supplyAsync(
                        () -> new OrganizationController(application).getCompanyOrgList(req),
                        httpExecutionContext.current());

            case "/config/compLogo":
                return CompletableFuture.supplyAsync(() -> new ConfigurationController(application).updateCompLogo(req),
                        httpExecutionContext.current());
            case "/config/categoryDetails":
                return CompletableFuture.supplyAsync(
                        () -> new ParticularController(application).showCategoryDetails(req),
                        httpExecutionContext.current());
            case "/config/getBranchTax":
                return CompletableFuture.supplyAsync(() -> new ConfigurationController(application).getBranchTaxes(req),
                        httpExecutionContext.current());
            case "/config/getConfig":
                return CompletableFuture.supplyAsync(
                        () -> new ConfigurationController(application).getConfiguration(req),
                        httpExecutionContext.current());
            case "/config/projectDetails":
                return CompletableFuture.supplyAsync(
                        () -> new ConfigurationController(application).showProjectDetails(req),
                        httpExecutionContext.current());
            case "/config/customerDetails":
                return CompletableFuture.supplyAsync(() -> new CustomerController(application).showCustomerDetails(req),
                        httpExecutionContext.current());
            case "/config/branchDetails":
                return CompletableFuture.supplyAsync(
                        () -> new ConfigurationController(application).showBranchDetails(req),
                        httpExecutionContext.current());
            case "/expense/getTax":
                return CompletableFuture.supplyAsync(() -> new ConfigurationController(application).getTaxes(),
                        httpExecutionContext.current());
            case "/expenses/getcost":
                return CompletableFuture.supplyAsync(() -> new ExpenseController(application).getCost(req),
                        httpExecutionContext.current());
            case "/expense/getLibrary":
                return CompletableFuture.supplyAsync(
                        () -> new SpecificsController(application).getItemKnowledgelibrary(req),
                        httpExecutionContext.current());
            case "/hiringRequest":
                return CompletableFuture.supplyAsync(() -> new LabourController(application).hiringRequest(req),
                        httpExecutionContext.current());
            case "/labour/getlabdatas":
                return CompletableFuture.supplyAsync(() -> new LabourController(application).labourList(req),
                        httpExecutionContext.current());
            case "/config/getCoaChild":
                return CompletableFuture.supplyAsync(() -> new SpecificsController(application).getChildCOA(req),
                        httpExecutionContext.current());
            case "/expenses/getTransaction":
                return CompletableFuture.supplyAsync(() -> new ExpenseController(application).getTransactionList(req),
                        httpExecutionContext.current());
            case "/config/getAlertUser":
                return CompletableFuture.supplyAsync(() -> new UserController(application).listAlertUser(req),
                        httpExecutionContext.current());
            case "/config/getHqAlertUser":
                return CompletableFuture.supplyAsync(() -> new UserController(application).listHqAlertUser(req),
                        httpExecutionContext.current());
            case "/config/getCurrency":
                return CompletableFuture.supplyAsync(() -> new controllers.Application(application).currency(req),
                        httpExecutionContext.current());
            case "/chartOfAccounts/allChartOfAccountsLRUCache":
                return CompletableFuture.supplyAsync(
                        () -> new SpecificsController(application).getAllChartOfAccountsLRUCache2(req),
                        httpExecutionContext.current());
            case "/chartOfAccounts/categoryBasedChartOfAccounts":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application).categoryBasedChartOfAccounts(req),
                        httpExecutionContext.current());
            case "/config/getCashierKl":
                return CompletableFuture.supplyAsync(
                        () -> new CashierController(application).getCashierInformation(req),
                        httpExecutionContext.current());
            case "/cashier/configCashCount":
                return CompletableFuture.supplyAsync(() -> new CashierController(application).configCashCount(req),
                        httpExecutionContext.current());
            case "/config/getBranchIncomesCoa":
                return CompletableFuture.supplyAsync(
                        () -> new SpecificsController(application).getBranchIncomesCoa(req),
                        httpExecutionContext.current());
            case "/tax/getBranchTaxes":
                return CompletableFuture.supplyAsync(() -> new TaxController(application).getBranchTaxes(req),
                        httpExecutionContext.current());
            case "/tax/itemTaxDetail":
                return CompletableFuture.supplyAsync(() -> new GstTaxController(application).getItemTaxDetail(req),
                        httpExecutionContext.current());
            case "/tax/addBranchSpecificTaxRules":
                return CompletableFuture.supplyAsync(
                        () -> new TaxController(application).applyTaxRulesToBranchSpecifics(req),
                        httpExecutionContext.current());
            case "/tax/showrcmtaxes":
                return CompletableFuture.supplyAsync(() -> new GstTaxController(application).getRcmTaxList4Branch(req),
                        httpExecutionContext.current());
            case "/transaction/getPOFromKaizala":
                return CompletableFuture.supplyAsync(() -> new KaizalaWebhookController().getPOFromKaizala(req),
                        httpExecutionContext.current());
            case "/transaction/getTransactionPurposeItems":
                return CompletableFuture.supplyAsync(() -> new TransactionController(application).getTxnItems(req),
                        httpExecutionContext.current());
            case "/transaction/getTxnPurposePjctItemOnBranch":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getTxnPjctItemsOnBranch(req),
                        httpExecutionContext.current());
            case "/transaction/fetchItemsForBranchProject":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).fetchItemsForBranchProject(req),
                        httpExecutionContext.current());
            case "/transaction/getTxnBranchSpecificsKL":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getTxnBnchSpecfKL(req),
                        httpExecutionContext.current());
            case "/transaction/getAdvanceDiscount":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getAdvanceDiscount(req),
                        httpExecutionContext.current());
            case "/transaction/calculateNetAmount":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getNetAmountTaxComponent(req),
                        httpExecutionContext.current());
            case "/transaction/calculatePriceForInclusive":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getPriceForTaxInclusive(req),
                        httpExecutionContext.current());
            case "/transaction/getAdvAdjTax":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getTaxesOnAdvanceOrAfterAdvAdj(req),
                        httpExecutionContext.current());
            case "/transaction/submitForAccounting":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).submitForAccounting(req),
                        httpExecutionContext.current());
            case "/transaction/submitForApproval":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).submitForApproval(req),
                        httpExecutionContext.current());
            case "/transaction/approverAction":
                return CompletableFuture.supplyAsync(() -> new TransactionController(application).approverActions(req),
                        httpExecutionContext.current());
            case "/transaction/showTransactionDetails":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).showTransactionDetails(req),
                        httpExecutionContext.current());
            case "/transaction/resubmitForApproval":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).resubmitForApproval(req),
                        httpExecutionContext.current());
            case "/transaction/getBranchInputTaxList":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getInputTaxesListOnBranch(req),
                        httpExecutionContext.current());
            case "/user/userTransactions":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionViewController(application).getTransactions(req),
                        httpExecutionContext.current());
            case "/transaction/barcodeItemFetch":
                return CompletableFuture.supplyAsync(() -> new TransactionController(application).barcodeItemFetch(req),
                        httpExecutionContext.current());
            case "/transaction/barcodeFetch":
                return CompletableFuture.supplyAsync(() -> new TransactionController(application).barcodeFetch(req),
                        httpExecutionContext.current());
            case "/transaction/getAccountingInfo":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionViewController(application).getAccountingInfo(req),
                        httpExecutionContext.current());
            case "/transaction/getCustVendPendingInvoices":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getCustomerVendorPendingInvoices(req),
                        httpExecutionContext.current());
            case "/transaction/getAllIncomeItems":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getAllIncomeItems(req),
                        httpExecutionContext.current());
            case "/transaction/getinvoiceOutstandings":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).gettxnOutstandings(req),
                        httpExecutionContext.current());
            case "/transaction/userTxnSearchBased":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionViewController(application).searchTransactions(req),
                        httpExecutionContext.current());
            case "/organization/getFinancials":
                return CompletableFuture.supplyAsync(() -> new OrganizationController(application).getFinancials(req),
                        httpExecutionContext.current());
            case "/transaction/userAuditorRemarks":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).userAuditorRemarks(req),
                        httpExecutionContext.current());
            case "/transaction/getprocurementrequest":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getProcurementRequest(req),
                        httpExecutionContext.current());
            case "/procure/procureRequest":
                return CompletableFuture.supplyAsync(() -> new TransactionController(application).procureRequest(req),
                        httpExecutionContext.current());
            case "/vendor/searchVendorName":
                return CompletableFuture.supplyAsync(() -> new VendorController(application).searchVendorName(req),
                        httpExecutionContext.current());
            case "/customer/searchCustomerName":
                return CompletableFuture.supplyAsync(() -> new CustomerController(application).searchCustomerName(req),
                        httpExecutionContext.current());
            case "/users/searchUsers":
                return CompletableFuture.supplyAsync(() -> new UserController(application).searchUsers(req),
                        httpExecutionContext.current());
            case "/transaction/vendorSupplierWithholdingData":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).vendorSupplierWithholdingData(req),
                        httpExecutionContext.current());
            case "/vendorcustomer/transactionsList":
                return CompletableFuture.supplyAsync(
                        () -> new VendorController(application).getVendorCustomerTransactions(req),
                        httpExecutionContext.current());
            case "/transaction/getTxnItemParent":
                return CompletableFuture.supplyAsync(() -> new TransactionController(application).getTxnItemParent(req),
                        httpExecutionContext.current());
            case "/branch/branchPremiseEntityInformation":
                return CompletableFuture.supplyAsync(
                        () -> new BranchController(application).branchPremiseEntityInformation(req),
                        httpExecutionContext.current());
            case "/branch/branchIndividualStatutoryInfo":
                return CompletableFuture.supplyAsync(
                        () -> new BranchController(application).branchIndividualStatutoryInfo(req),
                        httpExecutionContext.current());
            case "/branch/branchIndividualOperationalRemInfo":
                return CompletableFuture.supplyAsync(
                        () -> new BranchController(application).branchIndividualOperationalRemainderInfo(req),
                        httpExecutionContext.current());
            case "/branch/branchIndividualPolicyInfo":
                return CompletableFuture.supplyAsync(
                        () -> new BranchController(application).branchIndividualPolicyInfo(req),
                        httpExecutionContext.current());
            case "/specifics/searchCoa":
                return CompletableFuture.supplyAsync(() -> new SpecificsController(application).searchCoa(req),
                        httpExecutionContext.current());
            case "/shippingAddress":
                return CompletableFuture.supplyAsync(
                        () -> new InvoiceVoucherController(application).getShppingAddress(req),
                        httpExecutionContext.current());
            case "/addtionalDetails":
                return CompletableFuture.supplyAsync(
                        () -> new InvoiceVoucherController(application).getAdditionalDetails(req),
                        httpExecutionContext.current());
            case "/organization/getOperationals":
                return CompletableFuture.supplyAsync(() -> new OrganizationController(application).getOperationals(req),
                        httpExecutionContext.current());
            case "/organization/getRealTimeAlertsInfo":
                return CompletableFuture.supplyAsync(
                        () -> new OrganizationController(application).getRealTimeAlertsInfo(req),
                        httpExecutionContext.current());
            case "/transaction/invoiceData":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionItemsController(application).getInvoiceData(req),
                        httpExecutionContext.current());
            case "/specialadjustments/vendorProjects":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).vendorSpecialAdjustmentsProjects(req),
                        httpExecutionContext.current());
            case "/specialadjustments/amountName":
                return CompletableFuture.supplyAsync(() -> new TransactionController(application).amountName(req),
                        httpExecutionContext.current());
            case "/cash/transferMainToPetty":
                return CompletableFuture.supplyAsync(() -> new CashierController(application).transferMainToPetty(req),
                        httpExecutionContext.current());
            case "/vendorcustomer/acceptTransaction":
                return CompletableFuture.supplyAsync(() -> new VendorController(application).acceptTransaction(req),
                        httpExecutionContext.current());
            case "/subscriberLogin":
                return CompletableFuture.supplyAsync(() -> new controllers.Application(application).subscriberlogin(),
                        httpExecutionContext.current());
            case "/selectsubscription":
                return CompletableFuture.supplyAsync(() -> new OrganizationController(application).selectSubscription(),
                        httpExecutionContext.current());
            case "/dashboard/dashboardGetBranchProjectOperation":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).getBranchesOrProjectsOrOperationalData(req),
                        httpExecutionContext.current());
            case "/dashboard/dashboardGetVendorsOrCustomers":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).getVendorsOrCustomers(req),
                        httpExecutionContext.current());
            case "/dashboard/operationalDataSearch":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).searchOperationalData(req),
                        httpExecutionContext.current());
            case "/dashboard/customerwiserProformaInvoice":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).customerwiseProformaInvoice(req),
                        httpExecutionContext.current());
            case "/dashboard/quotationProformabybranch":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).getQuotationProformaBranchBy(req),
                        httpExecutionContext.current());
            case "/dashboard/quotationProformaItems":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).getQuotationProformaItems(req),
                        httpExecutionContext.current());
            case "/dashboard/transactionsforitm":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).getTransactionsForItem(req),
                        httpExecutionContext.current());
            case "/transaction/proceedingPettyTxnApproval":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getCurrentPettyCashAccount(req),
                        httpExecutionContext.current());
            case "/hiring/listProjectsReportsto":
                return CompletableFuture.supplyAsync(() -> new LabourController(application).listProjectsReportsto(req),
                        httpExecutionContext.current());
            case "/cashier/recoincileCashAccount":
                return CompletableFuture.supplyAsync(
                        () -> new CashierController(application).recoincileCashAccount(req),
                        httpExecutionContext.current());
            case "/cashier/recoincileBankAccountBalance":
                return CompletableFuture.supplyAsync(
                        () -> new CashierController(application).recoincileBankAccountBalance(req),
                        httpExecutionContext.current());
            case "/branch/bankAccountsForPayment":
                return CompletableFuture.supplyAsync(
                        () -> new BranchController(application).bankAccountsForPayment(req),
                        httpExecutionContext.current());
            case "/dashboard/chartOfAccountBreakUps":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).chartOfAccountBreakUps(req),
                        httpExecutionContext.current());
            case "/dashboard/recPayablesOpeningBalAndCurrentYearTotal":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).recPayablesOpeningBalAndCurrentYearTotal(req),
                        httpExecutionContext.current());
            case "/dashboard/branchWiseReceivablePayablesGraphData":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).branchWiseReceivablePayablesGraphData(req),
                        httpExecutionContext.current());
            case "/dashboard/displayBarnchAndPeriodWiseCustVend":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).displayBarnchAndPeriodWiseCustVend(req),
                        httpExecutionContext.current());
            case "/user/branchWiseApproverCashBankReceivablePayables":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).branchWiseApproverCashBankReceivablePayables(req),
                        httpExecutionContext.current());
            case "/transaction/wightedAverageForTransaction":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).wightedAverageForTransaction(req),
                        httpExecutionContext.current());
            case "/project/searchProject":
                return CompletableFuture.supplyAsync(() -> new ProjectController(application).searchProject(req),
                        httpExecutionContext.current());
            case "/vendor/availableLocations":
                return CompletableFuture.supplyAsync(() -> new SellerController().availableLocations(req),
                        httpExecutionContext.current());
            case "/dashboard/availableLocations":
                return CompletableFuture.supplyAsync(() -> new DashboardController(application).availableLocations(req),
                        httpExecutionContext.current());
            case "/dashboard/plotbranchAggregateData":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).plotbranchAggregateData(req),
                        httpExecutionContext.current());
            case "/dashboard/highestExpenseIncomeBI":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).highestExpenseIncomeBI(req),
                        httpExecutionContext.current());
            case "/dashboard/showPendingTxnDetails":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).showPendingTxnDetails(req),
                        httpExecutionContext.current());
            case "/dashboard/customerVendorAdvanceBI":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).customerVendorAdvanceBI(req),
                        httpExecutionContext.current());
            case "/dashboard/customerVendorAdvanceTransactionBI":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).customerVendorAdvanceTransactionBI(req),
                        httpExecutionContext.current());
            case "/config/getbranchadministratordatas":
                return CompletableFuture.supplyAsync(
                        () -> new BranchController(application).getBranchAdministratorData(req),
                        httpExecutionContext.current());
            case "/config/taxFormulaValidation":
                return CompletableFuture.supplyAsync(() -> new TaxController(application).taxFormulaValidation(req),
                        httpExecutionContext.current());
            case "/passwordExpiry":
                return CompletableFuture.supplyAsync(() -> new SecurityController().passwordExpiry(req),
                        httpExecutionContext.current());
            case "/accountSetting/getUserDetails":
                return CompletableFuture.supplyAsync(
                        () -> new AccountSettingsController(application).getUserDetails(req),
                        httpExecutionContext.current());
            case "/accountSetting/updateUserProfile":
                return CompletableFuture.supplyAsync(
                        () -> new AccountSettingsController(application).updateUserProfile(req),
                        httpExecutionContext.current());
            case "/accountSetting/getUserSecurityQuestions":
                return CompletableFuture.supplyAsync(
                        () -> new AccountSettingsController(application).getUserSecurityQuestions(req),
                        httpExecutionContext.current());
            case "/accountSetting/getUserRandomQuestion":
                return CompletableFuture.supplyAsync(
                        () -> new AccountSettingsController(application).getUserRandomQuestion(req),
                        httpExecutionContext.current());
            case "/support/getSupportTicketById":
                return CompletableFuture.supplyAsync(
                        () -> new SupportTicketController(application).getSupportTicketById(req),
                        httpExecutionContext.current());
            case "/claims/getPendingEmployeeClaims":
                return CompletableFuture.supplyAsync(
                        () -> new ClaimController(application).getPendingEmployeeClaims(req),
                        httpExecutionContext.current());
            case "/claims/getPaidEmployeeClaims":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).getPaidEmployeeClaims(req),
                        httpExecutionContext.current());
            case "/claims/empPendingClaimSettlement":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).settleEmpPendingClaim(req),
                        httpExecutionContext.current());
            case "/user/getSecurityQuestion":
                return CompletableFuture.supplyAsync(
                        () -> new UserController(application).getSecurityQuestionsByUser(req),
                        httpExecutionContext.current());
            case "/dashboard/getExceedingBudgetDetails":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).getTransactionExceedingBudgetGroupDetails(req),
                        httpExecutionContext.current());
            case "/claims/showTravelGroup":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).showTravelGroup(req),
                        httpExecutionContext.current());
            case "/claims/showExpenseGroup":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).showExpenseGroup(req),
                        httpExecutionContext.current());
            case "/claims/getUserClaimBranchProject":
                return CompletableFuture.supplyAsync(
                        () -> new ClaimController(application).getUserClaimBranchProject(req),
                        httpExecutionContext.current());
            case "/claims/getUserRelatedClaimsAvailable":
                return CompletableFuture.supplyAsync(
                        () -> new ClaimController(application).getUserRelatedClaimsAvailable(req),
                        httpExecutionContext.current());
            case "/claims/getSearchCriteriaData":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).getSearchCriteriaData(req),
                        httpExecutionContext.current());
            case "/labour/getProjectDatas":
                return CompletableFuture.supplyAsync(() -> new ProjectController(application).getProjectDatas(req),
                        httpExecutionContext.current());
            case "/labour/getJobDetailsDatas":
                return CompletableFuture.supplyAsync(() -> new ProjectController(application).getJobDetailsDatas(req),
                        httpExecutionContext.current());
            case "/project/getAllHirings":
                return CompletableFuture.supplyAsync(() -> new ProjectController(application).getAllHirings(req),
                        httpExecutionContext.current());
            case "/transaction/checkForDocumentUploadingRule":
                return CompletableFuture.supplyAsync(() -> new TransactionController(application).documentRule(req),
                        httpExecutionContext.current());
            case "/transaction/checkForDocumentUploadingRulePVS":
                return CompletableFuture.supplyAsync(() -> new TransactionController(application).documentRulePVS(req),
                        httpExecutionContext.current());
            case "/claims/locationOnTravelType":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).locationOnTravelType(req),
                        httpExecutionContext.current());
            case "/claims/displayTravelEligibility":
                return CompletableFuture.supplyAsync(
                        () -> new ClaimController(application).displayTravelEligibility(req),
                        httpExecutionContext.current());
            case "/support/openCloseIssue":
                return CompletableFuture.supplyAsync(
                        () -> new SupportTicketController(application).openOrCloseIssue(req),
                        httpExecutionContext.current());
            case "/claims/getclaimgstdata":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).getClaimGstData(req),
                        httpExecutionContext.current());
            case "/privacy/customerDetails":
                return CompletableFuture.supplyAsync(
                        () -> new AccountSettingsController(application).getCustomerAccountDetails(req),
                        httpExecutionContext.current());
            case "/privacy/vendorDetails":
                return CompletableFuture.supplyAsync(
                        () -> new AccountSettingsController(application).getVendorAccountDetails(req),
                        httpExecutionContext.current());
            case "/app/getPhoneCountry":
                return CompletableFuture.supplyAsync(
                        () -> new AccountSettingsController(application).getPhoneCodesAndCountries(),
                        httpExecutionContext.current());
            case "/claims/approverAction":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).approverAction(req),
                        httpExecutionContext.current());
            case "/user/userClaimsTransactions":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).userClaimsTransactions(req),
                        httpExecutionContext.current());
            case "/claims/exitingClaimsAdvanceTxnRefAndAmount":
                return CompletableFuture.supplyAsync(
                        () -> new ClaimController(application).exitingClaimsAdvanceTxnRefAndAmount(req),
                        httpExecutionContext.current());
            case "/claims/populateUserUnsettledTravelClaimAdvances":
                return CompletableFuture.supplyAsync(
                        () -> new ClaimController(application).populateUserUnsettledTravelClaimAdvances(req),
                        httpExecutionContext.current());
            case "/claims/displayUnsettledAdvances":
                return CompletableFuture.supplyAsync(
                        () -> new ClaimController(application).displayUnsettledAdvancesDetails(req),
                        httpExecutionContext.current());
            case "/claims/claimSettlementAccountantAction":
                return CompletableFuture.supplyAsync(
                        () -> new ClaimController(application).claimSettlementAccountantAction(req),
                        httpExecutionContext.current());
            case "/claimsbranch/bankAccountsForPayment":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).bankAccountsForPayment(req),
                        httpExecutionContext.current());
            case "/budget/getDetails":
                return CompletableFuture.supplyAsync(() -> new BudgetController(application).getBudgetDetails(req),
                        httpExecutionContext.current());
            case "/plbscoa/fetch":
                return CompletableFuture.supplyAsync(
                        () -> new PLBSCOAMapperController(application).fetchPLBSItemsToCOAMapping(req),
                        httpExecutionContext.current());
            case "/reportInventory/displayAllInventory":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).displayAllInventoryReport(req),
                        httpExecutionContext.current());
            case "/notes/getUsersAndProjects":
                return CompletableFuture.supplyAsync(
                        () -> new NotesController(application).getOrganizationUsersAndProjects(req),
                        httpExecutionContext.current());
            case "/notes/getNotes":
                return CompletableFuture.supplyAsync(() -> new NotesController(application).getNotes(req),
                        httpExecutionContext.current());
            case "/notes/getNoteById":
                return CompletableFuture.supplyAsync(() -> new NotesController(application).getNoteById(req),
                        httpExecutionContext.current());
            case "/notes/search":
                return CompletableFuture.supplyAsync(() -> new NotesController(application).search(req),
                        httpExecutionContext.current());
            case "/advance/userAdvanceForExpenseItems":
                return CompletableFuture.supplyAsync(
                        () -> new EmployeeAdvanceForExpensesController(application).employeeAdvanceForExpenseItems(req),
                        httpExecutionContext.current());
            case "/expenseAdvances/displayUserEligibility":
                return CompletableFuture.supplyAsync(
                        () -> new EmployeeAdvanceForExpensesController(application).displayUserEligibility(req),
                        httpExecutionContext.current());
            case "/expenseclaims/showExpenseClaimDetails":
                return CompletableFuture.supplyAsync(
                        () -> new EmployeeAdvanceForExpensesController(application).showExpenseClaimDetails(req),
                        httpExecutionContext.current());
            case "/advanceExpense/approverAction":
                return CompletableFuture.supplyAsync(
                        () -> new EmployeeAdvanceForExpensesController(application).approverAction(req),
                        httpExecutionContext.current());
            case "/advance/populateUserUnsettledExpenseAdvances":
                return CompletableFuture.supplyAsync(
                        () -> new EmployeeAdvanceForExpensesController(application)
                                .populateUserUnsettledExpenseAdvances(req),
                        httpExecutionContext.current());
            case "/advanceExpense/displayUnsettledAdvances":
                return CompletableFuture.supplyAsync(
                        () -> new EmployeeAdvanceForExpensesController(application).displayUnsettledAdvances(req),
                        httpExecutionContext.current());
            case "/advanceExpenses/expAdvanceSettlementAccountantAction":
                return CompletableFuture.supplyAsync(
                        () -> new EmployeeAdvanceForExpensesController(application)
                                .expAdvanceSettlementAccountantAction(req),
                        httpExecutionContext.current());
            case "/reimbursement/getUserExpenseItemReimbursementAmountKl":
                return CompletableFuture.supplyAsync(
                        () -> new EmployeeAdvanceForExpensesController(application)
                                .getUserExpenseItemReimbursementAmountKl(req),
                        httpExecutionContext.current());
            case "/reimbursement/reimbursementApproverAction":
                return CompletableFuture.supplyAsync(
                        () -> new EmployeeAdvanceForExpensesController(application).reimbursementApproverAction(req),
                        httpExecutionContext.current());
            case "/notes/count":
                return CompletableFuture.supplyAsync(() -> new NotesController(application).getNotesCount(req),
                        httpExecutionContext.current());
            case "/seller/getPricings":
                return CompletableFuture.supplyAsync(() -> new SellerController().getVendorPricings(req),
                        httpExecutionContext.current());
            case "/seller/getPriceDetails":
                return CompletableFuture.supplyAsync(() -> new SellerController().getVendorPriceDetails(req),
                        httpExecutionContext.current());
            case "/transaction/ruleBasedUserExistence":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionRuleController(application).ruleBasedUserExistence(req),
                        httpExecutionContext.current());
            case "/data/getCoaForBranch":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application).getCoaForBranchWithAllHeads(req),
                        httpExecutionContext.current());
            case "/data/coaUserBranch":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application).getCoa4UserByBranch(req),
                        httpExecutionContext.current());
            case "/txn/coaUserBranchList":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application).getCoaItems4UserByBranch(req),
                        httpExecutionContext.current());
            case "/data/getcoaliabilitiesitems":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application).getChartOfAccountsLiabilitiesItems(req),
                        httpExecutionContext.current());
            case "/data/getcoaincomeitemsWithTaxRules":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application).getChartOfAccountsIncomeItemsWithTaxRules(req),
                        httpExecutionContext.current());
            case "/data/getcoaExpenceitemsWithTaxRules":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application)
                                .getChartOfAccountsExpenceItemsWithTaxRules(req),
                        httpExecutionContext.current());
            case "/data/getcoaexpenceitemsbranchwise":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application).getChartOfAccountsExpenceItemsBranchwise(req),
                        httpExecutionContext.current());
            case "/data/getCoaForOraganization":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application).getCoaForOrganizationWithAllHeads(req),
                        httpExecutionContext.current());
            case "/data/getcoaunits":
                return CompletableFuture.supplyAsync(
                        () -> new ChartOfAccountsController(application).getAllCoaUnitsofOrg(req),
                        httpExecutionContext.current());
            case "/data/coaPlbsMap":
                return CompletableFuture.supplyAsync(
                        () -> new PLBSCoaMappingController(application).getCoaForOrganizationWithAllHeads(req),
                        httpExecutionContext.current());
            case "/officer/getDetails":
                return CompletableFuture.supplyAsync(() -> new BranchOfficerController(application).getDetails(req),
                        httpExecutionContext.current());
            case "/transactionProvision/approverAction":
                return CompletableFuture.supplyAsync(
                        () -> new ProvisionJournalEntryController(application).provisionApproverAction(req),
                        httpExecutionContext.current());
            case "/transactionPayroll/approverAction":
                return CompletableFuture.supplyAsync(
                        () -> new PayrollTxnController(application).payrollApproverAction(req),
                        httpExecutionContext.current());
            case "/idos/enquiry":
                return CompletableFuture.supplyAsync(() -> new ApplicationController(application).enquiry(req),
                        httpExecutionContext.current());
            case "/provisio/accountHeadTransactions":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).accountHeadTransactions(req),
                        httpExecutionContext.current());
            case "/data/branchBank":
                return CompletableFuture.supplyAsync(() -> new BranchBankController(application).branchBank(req),
                        httpExecutionContext.current());
            case "/data/branchBankDetails":
                return CompletableFuture.supplyAsync(() -> new BranchBankController(application).branchBankDetails(req),
                        httpExecutionContext.current());
            case "/data/orgBanks":
                return CompletableFuture.supplyAsync(
                        () -> new BranchBankController(application).getOrgBankAccounts(req),
                        httpExecutionContext.current());
            case "/ecommerce/contactSupplierVendor":
                return CompletableFuture.supplyAsync(() -> new SellerController().contactSupplierVendor(req),
                        httpExecutionContext.current());
            case "/cashnbank/display":
                return CompletableFuture.supplyAsync(
                        () -> new CashAndBankController(application).displayCashNBank(req),
                        httpExecutionContext.current());
            case "/cashnbank/bankReconciliation":
                return CompletableFuture.supplyAsync(
                        () -> new CashAndBankController(application).generateBankReconciliation(req),
                        httpExecutionContext.current());
            case "/cashnbank/validateBankDate":
                return CompletableFuture.supplyAsync(
                        () -> new CashAndBankController(application).validateBankReconciliationDate(req),
                        httpExecutionContext.current());
            case "/trialBalance/display":
                return CompletableFuture.supplyAsync(
                        () -> new TrialBalanceController(application).displayTrialBalance(req),
                        httpExecutionContext.current());
            case "/online/getUsers":
                return CompletableFuture.supplyAsync(
                        () -> new OnlineUsersController(application).getOnlineIdosUsers(req),
                        httpExecutionContext.current());
            case "/vendorcustomer/search":
                return CompletableFuture.supplyAsync(() -> new VendorController(application).search(req),
                        httpExecutionContext.current());
            case "/claims/search":
                return CompletableFuture.supplyAsync(() -> new ClaimController(application).search(req),
                        httpExecutionContext.current());
            case "/vendorTds/getBasicRow":
                return CompletableFuture.supplyAsync(
                        () -> new VendorTDSController(application).getVendorBasicRowData(req),
                        httpExecutionContext.current());
            case "/vendorTds/displayTdsDetails":
                return CompletableFuture.supplyAsync(() -> new VendorTDSController(application).displayTdsDetails(req),
                        httpExecutionContext.current());
            case "/vendorTds/displayTdsAdvanceDetails":
                return CompletableFuture.supplyAsync(
                        () -> new VendorTDSController(application).displayTdsAdvanceDetails(req),
                        httpExecutionContext.current());
            case "/vendorTds/getAdvanceRow":
                return CompletableFuture.supplyAsync(
                        () -> new VendorTDSController(application).getVendorAdvanceRowData(req),
                        httpExecutionContext.current());
            case "/vendorTds/getTdsApplyedTrans":
                return CompletableFuture.supplyAsync(
                        () -> new OrganizationController(application).getTdsApplyedTrans(req),
                        httpExecutionContext.current());
            case "/orgnization/orgGstSerials":
                return CompletableFuture.supplyAsync(
                        () -> new OrganizationController(application).getOrgGstinSerialNumber(req),
                        httpExecutionContext.current());
            case "/vendorCustomer/statements":
                return CompletableFuture.supplyAsync(() -> new VendorController(application).statements(req),
                        httpExecutionContext.current());
            case "/sell/checkMaxDiscountForWalkinCust":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).checkMaxDiscountForWalkinCust(req),
                        httpExecutionContext.current());
            case "/dashboard/getProjectFinancials":
                return CompletableFuture.supplyAsync(
                        () -> new DashboardController(application).getDashboardProjectFinancial(req),
                        httpExecutionContext.current());
            case "/dashboard/getProjectGraph":
                return CompletableFuture.supplyAsync(() -> new DashboardController(application).getProjectGraph(req),
                        httpExecutionContext.current());
            case "/dashboard/getGraph":
                return CompletableFuture.supplyAsync(() -> new DashboardController(application).getGraph(req),
                        httpExecutionContext.current());
            case "/specifics/buyInventoryStockAvailable":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).buyInventoryStockAvailable(req),
                        httpExecutionContext.current());
            case "/specifics/branchIncomeAvailableStock":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).branchIncomeAvailableStock(req),
                        httpExecutionContext.current());
            case "/specifics/branchSellStockAvailableCombSales":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).branchSellStockAvailableCombSales(req),
                        httpExecutionContext.current());
            case "/periodicInventory/display":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).displayPeriodicInventory(req),
                        httpExecutionContext.current());
            case "/stock/getInventoryItems":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).listAllInventoryItems(req),
                        httpExecutionContext.current());
            case "/stock/inventoryStockTransferBranches":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).inventoryStockTransferBranches(req),
                        httpExecutionContext.current());
            case "/stock/inventoryStockInBranch":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).inventoryStockInBranches(req),
                        httpExecutionContext.current());
            case "/stock/inventoryToBranches":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).inventoryToBranches(req),
                        httpExecutionContext.current());
            case "/reportInventory/display":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).displayInventoryReport(req),
                        httpExecutionContext.current());
            case "/transaction/getReverseChargeTaxforTypeOfSupply":
                return CompletableFuture.supplyAsync(
                        () -> new GstTaxController(application).getReverseChargeTaxforTypeOfSupply(req),
                        httpExecutionContext.current());
            case "/user/branchCustomerVendorReceivablePayables":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).branchCustomerVendorReceivablePayables(req),
                        httpExecutionContext.current());
            case "/user/overUnderOneEightyReceivablePayablesTxn":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).overUnderOneEightyReceivablePayablesTxn(req),
                        httpExecutionContext.current());
            case "/transaction/rcmItems":
                return CompletableFuture.supplyAsync(
                        () -> new GstTaxController(application).getReverseChargeItemsforSpecific(req),
                        httpExecutionContext.current());
            case "/transactionItems/getListOfMultipleItems":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionItemsController(application).getListOfMultipleItems(req),
                        httpExecutionContext.current());
            case "/transactionItems/getPayrollItems":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionItemsController(application).getPayrollItems(req),
                        httpExecutionContext.current());
            case "/config/getSelectedExpenseItemUnit":
                return CompletableFuture.supplyAsync(
                        () -> new SpecificsController(application).getSelectedExpenseItemUnit(req),
                        httpExecutionContext.current());
            case "/reportInventory/midInventory":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).getMidInventory(req),
                        httpExecutionContext.current());
            case "/reportInventory/displayDetailInventory":
                return CompletableFuture.supplyAsync(
                        () -> new StockWarehouseController(application).displayDetailInventory(req),
                        httpExecutionContext.current());
            case "/GSTController/searchGSTItemBasedOnDesc":
                return CompletableFuture.supplyAsync(
                        () -> new GstTaxController(application).searchGSTItemBasedOnDesc(req),
                        httpExecutionContext.current());
            case "/trialBalance/getTrialBalance":
                return CompletableFuture.supplyAsync(() -> new TrialBalanceController(application).trialBalance(req),
                        httpExecutionContext.current());
            case "/recruitment/listOpenPositions":
                return CompletableFuture.supplyAsync(
                        () -> new RecruitmentController(application).listOpenPositionJSON(req),
                        httpExecutionContext.current());
            case "/config/getParticularsForOrg":
                return CompletableFuture.supplyAsync(
                        () -> new TrialBalanceController(application).getParticularsForOrg(req),
                        httpExecutionContext.current());
            case "/profitLoss/display":
                return CompletableFuture.supplyAsync(
                        () -> new ProfitLossController(application).displayProfitLossReport(req),
                        httpExecutionContext.current());
            case "/profitLoss/displayinventory":
                return CompletableFuture.supplyAsync(() -> new ProfitLossController(application).displayInvetory(req),
                        httpExecutionContext.current());
            case "/balanceSheet/display":
                return CompletableFuture.supplyAsync(
                        () -> new BalanceSheetController(application).displayBalanceSheet(req),
                        httpExecutionContext.current());
            case "/transaction/loginFromAddIn":
                return CompletableFuture.supplyAsync(() -> new OffAddInTranController().loginFromAddIn(req),
                        httpExecutionContext.current());
            case "/transaction/branchwisesales":
                return CompletableFuture.supplyAsync(() -> new OffAddInTranController().getSalesAtEachBranch(req),
                        httpExecutionContext.current());
            case "/transaction/branchwiseexpense":
                return CompletableFuture.supplyAsync(() -> new OffAddInTranController().getExpensesAtEachBranch(req),
                        httpExecutionContext.current());
            case "/transaction/branchwisecustomeradvance":
                return CompletableFuture.supplyAsync(
                        () -> new OffAddInTranController().getAdvaReceivedAtEachBranchIn0to180days(req),
                        httpExecutionContext.current());
            case "/transaction/branchwisevendoradvance":
                return CompletableFuture.supplyAsync(
                        () -> new OffAddInTranController().getAdvaPaidVendorAtEachBranchIn0to180days(req),
                        httpExecutionContext.current());
            case "/ledger/itemtransactions":
                return CompletableFuture.supplyAsync(
                        () -> new TrialBalanceController(application).displayTransactionsForHead(req),
                        httpExecutionContext.current());
            case "/config/callKARVYUrl":
                return CompletableFuture.supplyAsync(() -> new KarvyAuthorization(application).callKARVYUrl(req),
                        httpExecutionContext.current());
            case "/config/getbranchlist":
                return CompletableFuture.supplyAsync(
                        () -> new Gstr3bJsonController(application).getBrnachGstinList(req),
                        httpExecutionContext.current());
            case "/config/getturnover":
                return CompletableFuture.supplyAsync(() -> new Gstr3bJsonController(application).populateValues(req),
                        httpExecutionContext.current());
            case "/config/getGSTR3BDataForKarvy":
                return CompletableFuture.supplyAsync(
                        () -> new Gstr3bJsonController(application).getKarvyGSTR3BSDataForTransactionsKarvy(req),
                        httpExecutionContext.current());
            case "/config/getStatewiseDataForKarvy":
                return CompletableFuture.supplyAsync(
                        () -> new Gstr3bJsonController(application).getGSTR3BTableThreePointOneDataForKarvy(req),
                        httpExecutionContext.current());
            case "/externalUserConfig":
                return CompletableFuture.supplyAsync(() -> new ExternalUserLoginController().externalUserSignIn(req),
                        httpExecutionContext.current());
            case "/externalUser/companyList":
                return CompletableFuture.supplyAsync(
                        () -> new ExternalUserLoginController().externalUserCompanyList(req),
                        httpExecutionContext.current());
            case "/externalUserList/allUsers":
                return CompletableFuture.supplyAsync(
                        () -> new ExternalUserLoginController().getAllExternalUsers(req),
                        httpExecutionContext.current());
            case "/externalUser/showExtUserDetails":
                return CompletableFuture.supplyAsync(() -> new ExternalUserLoginController().showExtUserDetails(req),
                        httpExecutionContext.current());
            case "/payroll/showPayrollSetupItems":
                return CompletableFuture.supplyAsync(() -> new PayrollController(application).showPayrollSetup(req),
                        httpExecutionContext.current());
            case "/payroll/getTransactionPayrollData":
                return CompletableFuture.supplyAsync(
                        () -> new PayrollController(application).getTransactionPayrollData(req),
                        httpExecutionContext.current());
            case "/payroll/getPayrollOpeningBalance":
                return CompletableFuture.supplyAsync(
                        () -> new PayrollController(application).getPayrollOpeningBalance(req),
                        httpExecutionContext.current());
            case "/payroll/generatePayslip":
                return CompletableFuture.supplyAsync(() -> new PayrollController(application).generatePayslip(req),
                        httpExecutionContext.current());
            case "/payroll/getCashBalance":
                return CompletableFuture.supplyAsync(() -> new PayrollController(application).getCashBalance(req),
                        httpExecutionContext.current());
            case "/payroll/payslipHistory":
                return CompletableFuture.supplyAsync(() -> new PayrollController(application).showPayslipHistory(req),
                        httpExecutionContext.current());
            case "/payroll/payslipForMonth":
                return CompletableFuture.supplyAsync(() -> new PayrollController(application).payslipForMonth(req),
                        httpExecutionContext.current());
            case "/transactionProvision/getProvisionJournalEntryDetails":
                return CompletableFuture.supplyAsync(
                        () -> new ProvisionJournalEntryController(application).getListOfPjeItemsDetails(req),
                        httpExecutionContext.current());
            case "/bom/getbyorg":
                return CompletableFuture.supplyAsync(() -> new BillOfMaterialController(application).getByOrg(req),
                        httpExecutionContext.current());
            case "/bom/incomeitemsbybranch":
                return CompletableFuture.supplyAsync(
                        () -> new BillOfMaterialController(application).getBillOfMaterialIncomeItemsByBranch(req),
                        httpExecutionContext.current());
            case "/bom/bomDetailsByExpence":
                return CompletableFuture.supplyAsync(
                        () -> new BillOfMaterialTxnController(application).getBomItemDetails(req),
                        httpExecutionContext.current());
            case "/bom/notaccountedbomtxnlist":
                return CompletableFuture.supplyAsync(
                        () -> new BillOfMaterialTxnController(application).getBomTxnList(req),
                        httpExecutionContext.current());
            case "/getPWCUsers":
                return CompletableFuture.supplyAsync(() -> new ApplicationController(application).getPWCUsers(req),
                        httpExecutionContext.current());
            case "/branch/getAllBranchDetails":
                return CompletableFuture.supplyAsync(() -> new BranchController(application).getAllBranchDetails(req),
                        httpExecutionContext.current());
            case "/transactions/purchaseRequisitionList":
                return CompletableFuture.supplyAsync(
                        () -> new TransactionController(application).getPurchaseRequisitionList(req),
                        httpExecutionContext.current());
            case "/bom/saveorupdate":
                return CompletableFuture.supplyAsync(() -> new BOMController(application).save(req), httpExecutionContext.current());
            case "/bom/getbyorganization":
                return CompletableFuture.supplyAsync(() -> new BOMController(application).getByOrg(req), httpExecutionContext.current());
            // case "/files/deleteUncommittedFiles":
            // return CompletableFuture.supplyAsync(
            // () -> new FileUploadController(application).deleteUncommittedFiles(req),
            // httpExecutionContext.current());
            default:
                return CompletableFuture.completedFuture(notFound("Route not found"));
        }
    }

}
