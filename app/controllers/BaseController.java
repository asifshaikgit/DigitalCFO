package controllers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.swing.Timer;
import com.idos.dao.*;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.*;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;

import javax.transaction.Transactional;
import javax.inject.Inject;
import play.mvc.Controller;
import play.mvc.Results;
import service.*;
import service.ProfitLoss.ProfitLossService;
import service.ProfitLoss.ProfitLossServiceImpl;
import service.balancesheetservice.BalanceSheetService;
import service.balancesheetservice.BalanceSheetServiceImpl;
import service.upload.UploadDataService;
import service.upload.UploadDataServiceImpl;
import javax.persistence.EntityManager;

import play.Application;
import play.mvc.*;
import play.mvc.Http.Status;
import views.html.errorPage;

public class BaseController extends Controller {
	// public static JPAApi jpaApi;
	// public static EntityManager entityManager;
	public static Application application;

	@Inject
	public BaseController(Application application) {
		this.application = application;
	}

	protected static final String userUpdateJQL = "update Users set lastLoginDate = ?1, inSession = 1, authToken=?2, lastUpdatedPasswordDate=?3, failedAttempt=0 where id=?4";
	public static final GenericDAO genericDAO = new GenericJpaDAO();
	protected static final TransactionDAO transactionDao = new TransactionDAOImpl();

	protected static final GstTaxService taxService = new GstTaxServiceImpl();
	protected static final CreateExcelService excelService = new CreateExcelServiceImpl(application);
	protected static final ExceptionService expService = new ExceptionServiceImpl();
	protected static final ApplicationService APPLICATION_SERVICE = new ApplicationServiceImpl();
	protected static final AnalyticsService ANALYTICS_SERVICE = new AnalyticsServiceImpl();
	protected static final OnlineService ONLINE_SERVICE = OnlineServiceImpl.getInstance();
	protected static final BranchBankService branchBankService = new BranchBankServiceImpl();
	protected static final BranchOfficerService OFFICER_SERVICE = new BranchOfficerServiceImpl();
	protected static final ClaimsSettlementService claimsSettlementService = new ClaimsSettlementServiceImpl();
	protected static final BudgetService BUDGET_SERVICE = new BudgetServiceImpl();
	protected static final CashNBankService cashNBankService = new CashNBankServiceImpl();
	protected static final ClaimsService claimsService = new ClaimsServiceImpl();
	protected static final validateTransactionService validateTxnService = new validateTransactionServiceImpl();
	protected static final CashierService cashierService = new CashierServiceImpl();
	protected static final TransactionService transactionService = new TransactionServiceImpl();
	protected static final TrialBalanceService TRIAL_BALANCE_SERVICE = new TrialBalanceServiceImpl();
	protected static final TrialBalanceLedgerService TRIAL_BALANCE_LEDGER_SERVICE = new TrialBalanceLedgerServiceImpl(
			application);
	protected static final ProvisionJournalEntryService provisionJournalEntryService = new ProvisionJournalEntryServiceImpl();
	protected static final PayrollService payrollService = new PayrollServiceImpl();
	protected static final EmployeeAdvanceForExpensesService employeeAdvanceForExpenseService = new EmployeeAdvanceForExpensesServiceImpl();
	protected static final DashboardService dashboardService = new DashboardServiceImpl();
	protected static final StockService stockService = new StockServiceImpl();
	protected static final QuotationProformaService quotationProformaService = new QuotationProformaServiceImpl();
	protected static final SellTransactionService sellTransactionService = new SellTransactionServiceImpl();
	protected static final BuyTransactionService buyTransactionService = new BuyTransactionServiceImpl();
	protected static final ChartOfAccountsService coaService = new ChartOfAccountsServiceImpl();
	protected static final PurchaseOrderService purchaseOrderService = new PurchaseOrderServiceImpl();
	protected static final BranchCashService branchCashService = new BranchCashServiceImpl();
	protected static final CustomerService customerService = new CustomerServiceImpl();
	protected static final DynamicReportService dynReportService = new DynamicReportServiceImpl(application);
	protected static final SpecificsService SPECIFICS_SERVICE = new SpecificsServiceImpl();
	protected static final TransactionRuleService txnRuleService = new TransactionRuleServiceImpl();
	protected static final VendorService VENDOR_SERVICE = new VendorServiceImpl(application);
	protected static final InvoiceService INVOICE_SERVICE = new InvoiceServiceImpl();
	protected static final ProfitLossService PROFIT_LOSS_SERVICE = new ProfitLossServiceImpl();
	protected static final BalanceSheetService BALANCE_SHEET_SERVICE = new BalanceSheetServiceImpl();
	protected static final AgeingReportService AGEING_REPORT_SERVICE = new AgeingReportServiceImpl();
	protected static final TransactionItemsService TRANSACTION_ITEMS_SERVICE = new TransactionItemsServiceImpl();
	protected static final ClaimItemDetailsServiceImpl CLAIM_ITEM_DETAILS_SERVICE = new ClaimItemDetailsServiceImpl();
	protected static final PLBSCoaMappingService PLBS_COA_MAPPING_SERVICE = new PLBSCoaMappingServiceImpl();
	protected static final InterBranchTransferService INTER_BRANCH_TRANSFER_SERVICE = new InterBranchTransferServiceImpl();
	protected static final VendorTdsSetupService VENDOR_TDS_SERVICE = new VendorTdsSetupServiceImpl();
	protected static final VendCustBillWiseOpeningBalanceService BILLWISE_OPENING_BALANCE_SERVICE = new VendCustBillWiseOpeningBalanceServiceImpl();
	// protected static final VendCustBranchWiseAdvanceBalanceService
	// BRANCHWISE_ADVANCE_BALANCE_SERVICE = new
	// VendCustBranchWiseAdvanceBalanceServiceImpl();
	protected static final SingleUserService singleUserService = new SingleUserServiceImpl(); // Single User
	protected static final TransactionViewService TRANSACTION_VIEW_SERVICE = new TransactionViewServiceImpl();
	protected static final GSTR2JsonDAO GSTR2_DAO = new GSTR2JsonDAOImpl();
	protected static final ReversalOfITCDAO REVERSAL_ITC_DAO = new ReversalOfITCDAOImpl();
	protected static final UserSetupService USER_SETUP_SERVICE = new UserSetupServiceImpl();
	protected static final BillOfMaterialService BILL_OF_MATERIAL_SERVICE = new BillOfMaterialServiceImpl();
	protected static final BillOfMaterialTxnService BILL_OF_MATERIAL_TXN_SERVICE = new BillOfMaterialTxnServiceImpl();
	protected static final PurchaseRequisitionTxnService PURCHASE_REQUISITION_TXN_SERVICE = new PurchaseRequisitionTxnServiceImpl();
	protected static final FileUploadService FILE_UPLOAD_SERVICE = new FileUploadServiceImpl();
	protected static final UploadDataService UPLOAD_DATA_SERVICE = new UploadDataServiceImpl();
	protected static final BOMService BOM_SERVICE = new BOMServiceImpl();

	protected static final CRUDController<BranchSpecifics> bnchspecfcntr = new CRUDController<BranchSpecifics>(
			application);
	protected static final CRUDController<Expense> cntr = new CRUDController<Expense>(application);
	protected static final CRUDController<Organization> orgcrud = new CRUDController<Organization>(application);
	protected static final CRUDController<BranchInsurance> bnchInscrud = new CRUDController<BranchInsurance>(
			application);
	protected static final CRUDController<BranchBankAccounts> bnchBankActcrud = new CRUDController<BranchBankAccounts>(
			application);
	protected static final CRUDController<BranchTaxes> taxBranchCrud = new CRUDController<BranchTaxes>(application);
	protected static final CRUDController<BranchDepositBoxKey> bnchdepboxcrud = new CRUDController<BranchDepositBoxKey>(
			application);
	protected static final CRUDController<Branch> bnchcrud = new CRUDController<Branch>(application);
	protected static final CRUDController<Particulars> partcrud = new CRUDController<Particulars>(application);
	protected static final CRUDController<Specifics> specfcrud = new CRUDController<Specifics>(application);
	protected static final CRUDController<Vendor> vendcrud = new CRUDController<Vendor>(application);
	protected static final CRUDController<VendorSpecific> vendorspecfcrud = new CRUDController<VendorSpecific>(
			application);
	protected static final CRUDController<StatutoryDetails> orgstatdtlscrud = new CRUDController<StatutoryDetails>(
			application);
	protected static final CRUDController<OrganizationKeyOfficials> bnchkeycrud = new CRUDController<OrganizationKeyOfficials>(
			application);
	protected static final CRUDController<OrganizationOperationalRemainders> orgOpeRemCrud = new CRUDController<OrganizationOperationalRemainders>(
			application);
	protected static final CRUDController<ProjectBranches> projBranchcrud = new CRUDController<ProjectBranches>(
			application);
	protected static final CRUDController<Tax> taxcrud = new CRUDController<Tax>(application);
	protected static final CRUDController<UserRolesSpecifics> usrRoleSpeccrud = new CRUDController<UserRolesSpecifics>(
			application);
	protected static final CRUDController<ProjectLabourPosition> projLabPosCrud = new CRUDController<ProjectLabourPosition>(
			application);
	protected static final CRUDController<Role> rolecrud = new CRUDController<Role>(application);
	protected static final CRUDController<Users> usercrud = new CRUDController<Users>(application);
	protected static final CRUDController<UsersRoles> userrolecrud = new CRUDController<UsersRoles>(application);
	protected static final CRUDController<UserUsability> userusabcntr = new CRUDController<UserUsability>(application);
	protected static final CRUDController<UserRights> userRightsCrud = new CRUDController<UserRights>(application);
	protected static final CRUDController<Project> projcrud = new CRUDController<Project>(application);

	public static Session emailsession = mailSession();
	public static Session exceptionEmailSession = exceptionEmailSession();
	protected static Session feedbackSession = feedbackSession();
	protected static Session noreplySession = noReplySession();
	protected static Session alertSession = alertSession();
	protected static Session channelSalesSession = channelSalesSession();
	// public static int i=0;
	protected static AuditDAO auditDAO = new AuditDAOImpl();

	public BaseController() {
		super();
	}

	public static Logger log = Logger.getLogger("controllers");
	public static DecimalFormat decimalFormat = new DecimalFormat("######.00");
	public static SimpleDateFormat idosdf = new SimpleDateFormat("MMM dd,yyyy");
	public static SimpleDateFormat idosmdtdf = new SimpleDateFormat("MMM dd");
	public static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
	public static SimpleDateFormat mysqldf = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat projectDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");// asif added
	public static SimpleDateFormat mysqlmdtdf = new SimpleDateFormat("MM-dd");
	public static SimpleDateFormat reportdf = new SimpleDateFormat("dd-MM-yyyy");
	public static SimpleDateFormat mysqldtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static SimpleDateFormat timefmt = new SimpleDateFormat("HH:mm:ss");
	// public static Map <String, Object> criterias =new HashMap<String, Object>();
	public static SimpleDateFormat monthtext = new SimpleDateFormat("MMM");

	public static Session mailSession() {
		String smtpHost = ConfigFactory.load().getString("smtp.host");
		String smtpPort = ConfigFactory.load().getString("smtp.port");
		final String username = ConfigFactory.load().getString("smtp.user");
		final String password = ConfigFactory.load().getString("smtp.password");
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", smtpPort);
		props.put("mail.from", username);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		// props.put("mail.debug", "true");
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		return session;
	}

	public static Session exceptionEmailSession() {
		String smtpHost = ConfigFactory.load().getString("smtp.host");
		String smtpPort = ConfigFactory.load().getString("smtp.port");
		final String username = ConfigFactory.load().getString("smtpexception.user");
		final String password = ConfigFactory.load().getString("smtpexception.password");
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", smtpPort);
		props.put("mail.from", username);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		// props.put("mail.debug", "true");
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		return session;
	}

	public static Session feedbackSession() {
		String smtpHost = ConfigFactory.load().getString("smtp.host");
		String smtpPort = ConfigFactory.load().getString("smtp.port");
		final String username = ConfigFactory.load().getString("smtpfeedback.user");
		final String password = ConfigFactory.load().getString("smtpfeedback.password");
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", smtpPort);
		props.put("mail.from", username);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		// props.put("mail.debug", "true");
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		return session;
	}

	public static Session noReplySession() {
		String smtpHost = ConfigFactory.load().getString("smtp.host");
		String smtpPort = ConfigFactory.load().getString("smtp.port");
		final String username = ConfigFactory.load().getString("smtpnoreply.user");
		final String password = ConfigFactory.load().getString("smtpnoreply.password");
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", smtpPort);
		props.put("mail.from", username);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		// props.put("mail.debug", "true");
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		return session;
	}

	public static Session channelSalesSession() {
		String smtpHost = ConfigFactory.load().getString("smtp.host");
		String smtpPort = ConfigFactory.load().getString("smtp.port");
		final String username = ConfigFactory.load().getString("smtpchannelsales.user");
		final String password = ConfigFactory.load().getString("smtpchannelsales.password");
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", smtpPort);
		props.put("mail.from", username);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		// props.put("mail.debug", "true");
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		return session;
	}

	public static Session alertSession() {
		String smtpHost = ConfigFactory.load().getString("smtp.host");
		String smtpPort = ConfigFactory.load().getString("smtp.port");
		final String username = ConfigFactory.load().getString("smtpalert.user");
		final String password = ConfigFactory.load().getString("smtpalert.password");
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", smtpPort);
		props.put("mail.from", username);
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.auth", "true");
		// props.put("mail.debug", "true");
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		return session;
	}

	/*
	 * public static EntityManager getEntityManager() {
	 * return entityManager;
	 * }
	 */

	public Class getControllerClass() {
		return (Class) (getClass());
	}

	/*
	 * public static List<String> getBussinessMethods(){
	 * List<String> businessMethodNames=new ArrayList<String>();
	 * EntityManager entityManager=jpaApi.em();
	 * try{
	 * entityManager.getTransaction().begin();
	 * Map<String,Object> param=new HashMap<String,Object>();
	 * Method[] methods=ConfigurationController.class.getDeclaredMethods();
	 * Method[] expensemethods=ExpenseController.class.getDeclaredMethods();
	 * Method[] particularsmethods=ParticularController.class.getDeclaredMethods();
	 * Method[] specificsmethods=SpecificsController.class.getDeclaredMethods();
	 * Method[] usermethods=UserController.class.getDeclaredMethods();
	 * Method[] vendormethods=VendorController.class.getDeclaredMethods();
	 * for(Method method:methods){
	 * if(method.getReturnType().getName()=="play.mvc.Result"){
	 * param.clear();
	 * param.put("actionName", method.getName());
	 * businessMethodNames.add(method.getName());
	 * if(genericDAO.isUnique(new BusinessAction(), param, jpaApi.em())){
	 * BusinessAction newba=new BusinessAction();
	 * newba.setActionName(method.getName());
	 * genericDAO.saveOrUpdate(newba, null, jpaApi.em());
	 * }
	 * }
	 * }
	 * for(Method method:expensemethods){
	 * if(method.getReturnType().getName()=="play.mvc.Result"){
	 * param.clear();
	 * param.put("actionName", method.getName());
	 * businessMethodNames.add(method.getName());
	 * if(genericDAO.isUnique(new BusinessAction(), param, jpaApi.em())){
	 * BusinessAction newba=new BusinessAction();
	 * newba.setActionName(method.getName());
	 * genericDAO.saveOrUpdate(newba, null, jpaApi.em());
	 * }
	 * }
	 * }
	 * for(Method method:particularsmethods){
	 * if(method.getReturnType().getName()=="play.mvc.Result"){
	 * param.clear();
	 * param.put("actionName", method.getName());
	 * businessMethodNames.add(method.getName());
	 * if(genericDAO.isUnique(new BusinessAction(), param, jpaApi.em())){
	 * BusinessAction newba=new BusinessAction();
	 * newba.setActionName(method.getName());
	 * genericDAO.saveOrUpdate(newba, null, jpaApi.em());
	 * }
	 * }
	 * }
	 * for(Method method:specificsmethods){
	 * if(method.getReturnType().getName()=="play.mvc.Result"){
	 * param.clear();
	 * param.put("actionName", method.getName());
	 * businessMethodNames.add(method.getName());
	 * if(genericDAO.isUnique(new BusinessAction(), param, jpaApi.em())){
	 * BusinessAction newba=new BusinessAction();
	 * newba.setActionName(method.getName());
	 * genericDAO.saveOrUpdate(newba, null, jpaApi.em());
	 * }
	 * }
	 * }
	 * for(Method method:usermethods){
	 * if(method.getReturnType().getName()=="play.mvc.Result"){
	 * param.clear();
	 * param.put("actionName", method.getName());
	 * businessMethodNames.add(method.getName());
	 * if(genericDAO.isUnique(new BusinessAction(), param, jpaApi.em())){
	 * BusinessAction newba=new BusinessAction();
	 * newba.setActionName(method.getName());
	 * genericDAO.saveOrUpdate(newba, null, jpaApi.em());
	 * }
	 * }
	 * }
	 * for(Method method:vendormethods){
	 * if(method.getReturnType().getName()=="play.mvc.Result"){
	 * param.clear();
	 * param.put("actionName", method.getName());
	 * businessMethodNames.add(method.getName());
	 * if(genericDAO.isUnique(new BusinessAction(), param, jpaApi.em())){
	 * BusinessAction newba=new BusinessAction();
	 * newba.setActionName(method.getName());
	 * genericDAO.saveOrUpdate(newba, null, jpaApi.em());
	 * }
	 * }
	 * }
	 * entityManager.getTransaction().commit();
	 * }catch(Exception ex){
	 * log.log(Level.SEVERE, "Error", ex);
	 * String strBuff=getStackTraceMessage(ex);
	 * expService.sendExceptionReport(strBuff,"GetBussinessMethods Email",
	 * "GetBussinessMethods Organization",
	 * Thread.currentThread().getStackTrace()[1].getMethodName());
	 * }
	 * return businessMethodNames;
	 * }
	 * 
	 * public static void saveBusinessObject(){
	 * Map<String,Object> param=new HashMap<String,Object>();
	 * Map<String, Object> criterias=new HashMap<String, Object>();
	 * // EntityManager entityManager=getEntityManager();
	 * try{
	 * entityManager.getTransaction().begin();
	 * String []
	 * adminBusinessObject={"Expense Category / Item","Vendor Setup","Project Setup"
	 * ,"Branch Setup","Action Setup","Permission Setup","Budget Setup"
	 * ,"Users Setup","Role Setup"};
	 * String [] creatorBusinessObject={"Create Expense"};
	 * String [] approvarBusinessObject={"Expenses"};
	 * for(String bo:adminBusinessObject){
	 * param.clear();
	 * param.put("permissionName", bo);
	 * if(genericDAO.isUnique(new Permission(), param, jpaApi.em())){
	 * Permission permission=new Permission();
	 * permission.setPermissionName(bo);
	 * criterias.clear();
	 * criterias.put("name", "ADMIN");
	 * Role role=genericDAO.getByCriteria(Role.class, criterias, entityManager);
	 * permission.setRole(role);
	 * genericDAO.saveOrUpdate(permission, null, entityManager);
	 * }
	 * }
	 * for(String bo:creatorBusinessObject){
	 * param.clear();
	 * param.put("permissionName", bo);
	 * if(genericDAO.isUnique(new Permission(), param, jpaApi.em())){
	 * Permission permission=new Permission();
	 * permission.setPermissionName(bo);
	 * criterias.clear();
	 * criterias.put("name", "CREATOR");
	 * Role role=genericDAO.getByCriteria(Role.class, criterias, entityManager);
	 * permission.setRole(role);
	 * genericDAO.saveOrUpdate(permission, null, entityManager);
	 * }
	 * }
	 * for(String bo:approvarBusinessObject){
	 * param.clear();
	 * param.put("permissionName", bo);
	 * if(genericDAO.isUnique(new Permission(), param, jpaApi.em())){
	 * Permission permission=new Permission();
	 * permission.setPermissionName(bo);
	 * criterias.clear();
	 * criterias.put("name", "APPROVER");
	 * Role role=genericDAO.getByCriteria(Role.class, criterias, entityManager);
	 * permission.setRole(role);
	 * genericDAO.saveOrUpdate(permission, null, entityManager);
	 * }
	 * }
	 * entityManager.getTransaction().commit();
	 * }catch(Exception ex){
	 * log.log(Level.SEVERE, "Error", ex);
	 * String strBuff=getStackTraceMessage(ex);
	 * expService.sendExceptionReport(strBuff,"SaveBusinessObject Email",
	 * "SaveBusinessObject Organization",
	 * Thread.currentThread().getStackTrace()[1].getMethodName());
	 * }
	 * }
	 */
	@Transactional
	public static void sendMailWithAttachment(final Session session, final String subject, final String fileName,
			final java.io.File file, final String mailTo, final String mailCcTo) {
		if (ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
			return;
		}
		try {
			MimeMessage message = new MimeMessage(session);
			final String username = ConfigFactory.load().getString("smtp.user");
			message.setFrom(new InternetAddress(username));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailCcTo));
			message.setSubject(subject);
			MimeBodyPart messageBodyPart2 = new MimeBodyPart();
			DataSource source = new FileDataSource(file);
			messageBodyPart2.setDataHandler(new DataHandler(source));
			messageBodyPart2.setFileName(fileName);
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart2);
			message.setContent(multipart);
			Transport.send(message);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "SendMailWithAttachment Email",
					"SendMailWithAttachment Organization", Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}

	@Transactional
	public static void sendUserMailWithSetUpsAttachment(final Session session, final String subject,
			final String fileName, final java.io.File file, final String fileName1, final java.io.File file1,
			final String mailTo) {
		if (ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
			return;
		}
		Timer timer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					MimeMessage message = new MimeMessage(session);
					final String username = ConfigFactory.load().getString("smtp.user");
					message.setFrom(new InternetAddress(username));
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
					message.setSubject(subject);
					MimeBodyPart messageBodyPart2 = new MimeBodyPart();
					MimeBodyPart messageBodyPart3 = new MimeBodyPart();
					DataSource source = new FileDataSource(file);
					DataSource source1 = new FileDataSource(file1);
					messageBodyPart2.setDataHandler(new DataHandler(source));
					messageBodyPart3.setDataHandler(new DataHandler(source1));
					messageBodyPart2.setFileName(fileName);
					messageBodyPart3.setFileName(fileName1);
					Multipart multipart = new MimeMultipart();
					multipart.addBodyPart(messageBodyPart2);
					multipart.addBodyPart(messageBodyPart3);
					message.setContent(multipart);
					Transport.send(message);
				} catch (Exception ex) {
					log.log(Level.SEVERE, "Error", ex);
					String strBuff = getStackTraceMessage(ex);
					expService.sendExceptionReport(strBuff, "SendUserMailWithSetUpsAttachment Email",
							"SendUserMailWithSetUpsAttachment Organization",
							Thread.currentThread().getStackTrace()[1].getMethodName());
				}
			}
		});
		timer.setRepeats(false); // Only execute once
		timer.start();
	}

	@Transactional
	public static void sendMailWithMultipleAttachment(final Session session, final String subject,
			final Map<String, File> files, final String mailTo, final String[] mailCcTo) {
		if (ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
			return;
		}
		try {
			MimeMessage message = new MimeMessage(session);
			final String username = ConfigFactory.load().getString("smtp.user");
			message.setFrom(new InternetAddress(username));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			Address[] address = null;
			if (mailCcTo.length > 0) {
				address = new InternetAddress[mailCcTo.length];
				for (byte b = 0; b < mailCcTo.length; b++) {
					address[b] = new InternetAddress(mailCcTo[b]);
				}
				message.addRecipients(Message.RecipientType.TO, address);
			}
			message.setSubject(subject);
			Multipart multipart = new MimeMultipart();
			for (Map.Entry<String, File> file : files.entrySet()) {
				MimeBodyPart messageBodyPart2 = new MimeBodyPart();
				DataSource source = new FileDataSource(file.getValue());
				messageBodyPart2.setDataHandler(new DataHandler(source));
				messageBodyPart2.setFileName(file.getKey());
				multipart.addBodyPart(messageBodyPart2);
			}
			message.setContent(multipart);
			Transport.send(message);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "SendMailWithAttachment Email",
					"SendMailWithAttachment Organization", Thread.currentThread().getStackTrace()[1].getMethodName());
		}
	}

	@Transactional
	public static void mailTimer(final String body, final String username, final Session session, final String email,
			final String cc, final String subject) {
		if (ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
			return;
		}
		Timer timer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Email mail = new HtmlEmail();
					mail.setMailSession(session);
					mail.setFrom(username);
					mail.addTo(email);
					if (cc != null) {
						mail.addCc(cc);
					}
					mail.setSubject(subject);
					mail.setSentDate(new Date());
					mail.setMsg(body);
					// mail.send();
				} catch (EmailException ex) {
					final Session reattemptsession = alertSession();
					final String alertusername = ConfigFactory.load().getString("smtpalert.user");
					mailReattempt(body, alertusername, reattemptsession, email, cc, subject);
					log.log(Level.SEVERE, "Error", ex);
				}
			}
		});
		timer.setRepeats(false); // Only execute once
		timer.start();
	}

	@Transactional
	public static void mailTimerMultiple(final String body, final String username, final Session session,
			final String email, final Collection<InternetAddress> cc, final String subject) {
		if (ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
			return;
		}
		Timer timer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Email mail = new HtmlEmail();
					mail.setMailSession(session);
					mail.setFrom(username);
					mail.addTo(email);
					if (cc.size() > 0) {
						mail.setCc(cc);
					}
					mail.setSubject(subject);
					mail.setSentDate(new Date());
					mail.setMsg(body);
					// mail.send();
				} catch (EmailException ex) {
					final Session reattemptsession = alertSession();
					final String alertusername = ConfigFactory.load().getString("smtpalert.user");
					mailReattemptMultiple(body, alertusername, reattemptsession, email, cc, subject);
					log.log(Level.SEVERE, "Error", ex);
				}
			}
		});
		timer.setRepeats(false); // Only execute once
		timer.start();
	}

	@Transactional
	public static void mailReattemptMultiple(final String body, final String username, final Session session,
			final String email, final Collection<InternetAddress> cc, final String subject) {
		if (ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
			return;
		}
		Timer timer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Email mail = new HtmlEmail();
					mail.setMailSession(session);
					mail.setFrom(username);
					mail.addTo(email);
					if (cc.size() > 0) {
						mail.setCc(cc);
					}
					mail.setSubject(subject);
					mail.setSentDate(new Date());
					mail.setMsg(body);
					// mail.send();
				} catch (EmailException ex) {
					log.log(Level.SEVERE, "Error", ex);
				}
			}
		});
		timer.setRepeats(false); // Only execute once
		timer.start();
	}

	@Transactional
	public static void mailTimer1(final String body, final String username, final Session session, final String email,
			final String cc, final String subject) {
		if (ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
			return;
		}
		Timer timer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Email mail = new HtmlEmail();
					mail.setMailSession(session);
					mail.setFrom(username);
					mail.addTo(email);
					if (cc != null) {
						mail.addCc(cc);
					}
					mail.setSubject(subject);
					mail.setSentDate(new Date());
					mail.setMsg(body);
					// mail.send();
				} catch (EmailException ex) {
					final Session reattemptsession = alertSession();
					final String alertusername = ConfigFactory.load().getString("smtpalert.user");
					mailReattempt(body, alertusername, reattemptsession, email, cc, subject);
					log.log(Level.SEVERE, "Error", ex);
				}
			}
		});
		timer.setRepeats(false); // Only execute once
		timer.start();
	}

	@Transactional
	public static void mailReattempt(final String body, final String username, final Session session,
			final String email, final String cc, final String subject) {
		if (ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
			return;
		}
		Timer timer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					Email mail = new HtmlEmail();
					mail.setMailSession(session);
					mail.setFrom(username);
					mail.addTo(email);
					if (cc != null) {
						mail.addCc(cc);
					}
					mail.setSubject(subject);
					mail.setSentDate(new Date());
					mail.setMsg(body);
					// mail.send();
				} catch (EmailException ex) {
					log.log(Level.SEVERE, "Error", ex);
				}
			}
		});
		timer.setRepeats(false); // Only execute once
		timer.start();
	}

	public static List<String> getStackTrace(Throwable ex) {
		StackTraceElement[] stmsg = ex.getStackTrace();
		List<String> errorList = new ArrayList<String>();
		for (int i = 0; i < stmsg.length; i++) {
			errorList.add(stmsg[i].toString());
		}
		return errorList;
	}

	public static String getStackTraceMessage(Throwable ex) {
		StringBuilder strbuf = new StringBuilder(ex.toString());
		for (StackTraceElement stackTraceElement : ex.getStackTrace()) {
			strbuf.append("\n    at ");
			strbuf.append(stackTraceElement.toString());
		}
		Throwable cause = ex.getCause();
		while (cause != null) {
			strbuf.append("\nCaused by: ");
			strbuf.append(cause.toString());
			cause = cause.getCause();
		}

		return strbuf.toString();
	}

	public Result reportException(EntityManager entityManager, EntityTransaction entityTransaction, Users user,
			Exception ex, ObjectNode result) {
		String strBuff = getStackTraceMessage(ex);
		if (user != null) {
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		} else {
			expService.sendExceptionReport(strBuff, "", "", Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		if (entityTransaction != null && entityTransaction.isActive()) {
			entityTransaction.rollback();
		}
		List<String> errorList = getStackTrace(ex);
		Result status = null;
		if (ex instanceof IDOSException) {
			if (user != null) {
				log.log(Level.SEVERE, user.getEmail(), ex);
			} else {
				log.log(Level.SEVERE, "Error", ex);
			}
			result.put("message", ((IDOSException) ex).getErrorText());
			status = Results.ok(result);
		} else {
			if (user != null) {
				log.log(Level.SEVERE, user.getEmail(), ex);
			} else {
				log.log(Level.SEVERE, "Error", ex);
			}
			// if(entityManager.isOpen())
			// entityManager.close();
			// if(entityManager.isOpen())
			// // entityManager.close();
			status = internalServerError(errorPage.render(ex, errorList));
		}

		return status;
	}

	public Result reportThrowable(EntityManager entityManager, EntityTransaction entityTransaction, Users user,
			Throwable throwable, ObjectNode result) {
		String strBuff = getStackTraceMessage(throwable);
		if (user != null)
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		else
			expService.sendExceptionReport(strBuff, "", "", Thread.currentThread().getStackTrace()[1].getMethodName());
		if (entityTransaction != null && entityTransaction.isActive()) {
			entityTransaction.rollback();
		}
		if (user != null) {
			log.log(Level.SEVERE, user.getEmail(), throwable);
		} else {
			log.log(Level.SEVERE, "Error", throwable);
		}
		List<String> errorList = getStackTrace(throwable);
		// if(entityManager.isOpen())
		// entityManager.close();
		return internalServerError(errorPage.render(throwable, errorList));
	}
}
