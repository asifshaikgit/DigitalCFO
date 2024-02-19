package controllers;

import java.io.File;
import java.util.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import model.Branch;
import model.BranchSpecifics;
import model.ClaimTransaction;
import model.ClaimsSettlement;
import model.IdosLocations;
import model.Organization;
import model.Project;
import model.Specifics;
import model.Transaction;
import model.TransactionItems;
import model.Users;
import model.Vendor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;
import javax.transaction.Transactional;

import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.*;
import views.html.*;
import java.util.logging.Level;
import com.idos.util.DateUtil;
import com.idos.util.IdosConstants;
// import com.idos.util.MySqlConnection;
import java.sql.Statement;
import play.Application;
import javax.inject.Inject;

public class DashboardController extends StaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;
	// private Request request;
	// private Http.Session session = request.session();

	@Inject
	public DashboardController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
		this.application = application;
	}

	@Transactional
	public Result getBranchesOrProjectsOrOperationalData(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = null;
		JsonNode requests = request.body().asJson();
		Users usr = null;
		try {
			String usermail = requests.findValue("useremail").asText();
			int type = requests.findValue("type").asInt();
			usr = getUserInfo(request);
			if (usr == null) {
				return unauthorized();
			}
			result = dashboardService.getBranchesOrProjectsOrOperationalData(usr.getOrganization(), type, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getVendorsOrCustomers(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = null;
		Http.Session session = request.session();
		JsonNode requests = request.body().asJson();
		Users usr = null;
		try {
			String usermail = requests.findValue("useremail").asText();
			int type = requests.findValue("type").asInt();
			session.adding("email", usermail);
			usr = getUserInfo(request);
			result = dashboardService.getVendorsOrCustomers(usr.getOrganization(), type, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result searchOperationalData(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = null;
		Http.Session session = request.session();
		JsonNode requests = request.body().asJson();
		Users usr = null;
		try {
			String usermail = requests.findValue("useremail").asText();
			session.adding("email", usermail);
			usr = getUserInfo(request);
			int type = requests.findValue("type").asInt();
			long fetchDataId = 0;
			if (1 == type) {
				/* Organization Details */
				result = dashboardService.getOrganizationDetails(usr.getOrganization());
				log.log(Level.INFO, "result=" + result.toString());
			} else if (2 == type) {
				/* Branch Details */
				fetchDataId = requests.findValue("fetchDataId").asLong();
				int subType = requests.findValue("subType").asInt();
				result = dashboardService.getBranchDetails(fetchDataId, subType, entityManager);
			} else if (3 == type) {
				/* Chart of Accounts */
				fetchDataId = requests.findValue("fetchDataId").asLong();
				result = dashboardService.getChartOfAccount(fetchDataId);
			} else if (4 == type) {
				/* Projects */
				fetchDataId = requests.findValue("fetchDataId").asLong();
				result = dashboardService.getProject(fetchDataId);
			} else if (5 == type) {
				/* Vendor Details */
				fetchDataId = requests.findValue("fetchDataId").asLong();
				result = dashboardService.getVendorOrCustomerDetails(fetchDataId);
			} else if (6 == type) {
				/* Customer Details */
				fetchDataId = requests.findValue("fetchDataId").asLong();
				result = dashboardService.getVendorOrCustomerDetails(fetchDataId);
			} else if (7 == type) {
				/* User Details */
				fetchDataId = requests.findValue("fetchDataId").asLong();
				result = dashboardService.getUser(fetchDataId);
			} else if (8 == type) {
				/* Travel Group Details */
				fetchDataId = requests.findValue("fetchDataId").asLong();
				result = dashboardService.getTravelClaims(fetchDataId);
			} else if (9 == type) {
				/* Expense Group Details */
				fetchDataId = requests.findValue("fetchDataId").asLong();
				result = dashboardService.getExpenseClaims(fetchDataId);
			}
		} catch (Exception ex) {

			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "SearchOperationalData Email", "SearchOperationalData Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result downloadOperationalVendorCustomerData(Request request) {
		log.log(Level.FINE, ">>>> Start");

		ObjectNode result = Json.newObject();
		JsonNode requests = request.body().asJson();
		Users usr = null;
		File file = null;
		try {
			String usermail = requests.findValue("useremail").asText();
			int type = requests.findValue("type").asInt();
			usr = getUserInfo(request);
			if (usr == null) {
				return unauthorized();
			}
			String sheetName = usr.getOrganization().getName();
			if (1 == type) {
				sheetName = sheetName + "Vendor";
			} else if (2 == type) {
				sheetName = sheetName + "Customer";
			}
			String path = application.path().toString() + "/logs/OrgVendorCustomer/";
			CreateExcelService excelService = new CreateExcelServiceImpl(application);
			String fileName = excelService.createOrgVendorCustomerExcel(usr.getOrganization(), type, path, sheetName,
					entityManager);
			// result.put("filename", filename);
			// DashboardService.downloadOperationalVendorCustomerData(usr.getOrganization(),
			// type);
			file = new File(path + fileName);
			return Results.ok(file).withHeader("ContentType", "application/xlsx").withHeader("Content-Disposition",
					"attachment; filename=" + fileName);
		} catch (Exception ex) {

			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	@Transactional
	public Result chartOfAccountBreakUps(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode results = Json.newObject();
		JsonNode requests = request.body().asJson();
		String filename = null;
		Users user = null;
		try {
			ArrayNode coaBreakUps = results.putArray("coaBreakUpsData");
			String usermail = requests.findValue("useremail").asText();
			String branchName = requests.findValue("branchName").asText();
			String branchId = requests.findValue("branchId") == null ? null : requests.findValue("branchId").asText();
			String amtValue = requests.findValue("amtValue").asText();
			String requirements = requests.findValue("displayReq").asText();
			String addOnParameter = requests.findValue("addOnParameter") != null
					? requests.findValue("addOnParameter").asText()
					: null;
			String currDashboardFromDate = requests.findValue("currDashboardFromDate") != null
					? requests.findValue("currDashboardFromDate").asText()
					: null;
			String currDashboardToDate = requests.findValue("currDashboardToDate") != null
					? requests.findValue("currDashboardToDate").asText()
					: null;
			String prevDashboardFromDate = requests.findValue("prevDashboardFromDate") != null
					? requests.findValue("prevDashboardFromDate").asText()
					: null;
			String prevDashboardToDate = requests.findValue("prevDashboardToDate") != null
					? requests.findValue("prevDashboardToDate").asText()
					: null;

			Date currDateTime = mysqldtf.parse(mysqldtf.format(Calendar.getInstance().getTime()));
			String currentWeekStartDate = null, currentWeekEndDate = null;
			if (currDashboardFromDate != null && !"".equals(currDashboardFromDate) && currDashboardToDate != null
					&& !"".equals(currDashboardToDate)) {
				currentWeekStartDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currDashboardFromDate));
				currentWeekEndDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currDashboardToDate));
			} else {
				Calendar cal = Calendar.getInstance();
				// cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
				currentWeekStartDate = mysqldf.format(cal.getTime());
				// cal.add(Calendar.DAY_OF_WEEK, 6);
				cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
				currentWeekEndDate = mysqldf.format(cal.getTime());
			}
			// cal.add(Calendar.DAY_OF_WEEK, 1);
			// start previous week dash board datas
			String previousWeekStartDate = null, previousWeekEndDate = null;
			if (prevDashboardFromDate != null && !"".equals(prevDashboardFromDate) && prevDashboardToDate != null
					&& !"".equals(prevDashboardToDate)) {
				previousWeekStartDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevDashboardFromDate));
				previousWeekEndDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevDashboardToDate));
			} else {
				Calendar newcal = Calendar.getInstance();
				newcal.add(Calendar.MONTH, -1);
				newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMinimum(Calendar.DAY_OF_MONTH));
				// newcal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				// newcal.add(Calendar.DAY_OF_WEEK, -7);
				previousWeekStartDate = mysqldf.format(newcal.getTime());
				// newcal.add(Calendar.DAY_OF_WEEK, 6);
				newcal.set(Calendar.DAY_OF_MONTH, newcal.getActualMaximum(Calendar.DAY_OF_MONTH));
				previousWeekEndDate = mysqldf.format(newcal.getTime());
			}
			user = getUserInfo(request);
			Branch branch = null;
			Project project = null;
			if (addOnParameter != null && !addOnParameter.equals("")) {
				if (addOnParameter.equals("branch") || addOnParameter.equals("projectbranch")) {
					if (branchId != null && !"".equals(branchId)) {
						branch = Branch.findById(Long.parseLong(branchId));
					}
					if (branch == null) {
						branch = getBranch(branchName, user);
					}
				}
				if (addOnParameter.equals("project")) {
					if (branchId != null && !"".equals(branchId)) {
						project = Project.findById(Long.parseLong(branchId));
					}
					if (project == null) {
						project = getProject(branchName, user);
					}
				}
			}
			Map<String, Object> criterias = new HashMap<String, Object>();
			if (branch != null) {
				// get provision entry data
				Map provisionEntries = new HashMap();
				Map allSpecifcsAmtData = new HashMap();
				Map vendorPayablesData = new HashMap();
				Map custReceivablesData = new HashMap();
				// EntityManager entityManager = getEntityManager();
				ProvisionJournalEntryService jourObj = new ProvisionJournalEntryServiceImpl();
				Map journalResultThisWeek = jourObj.getDashboardProvisionEntriesDataForBranch(currentWeekStartDate,
						currentWeekEndDate, user, allSpecifcsAmtData, vendorPayablesData, custReceivablesData, branch,
						entityManager);
				Double cashExpenseThisWeek = null;
				Double creditExpenseThisWeek = null;
				Double previousWeekCashExpense = null;
				Double previousWeekCreditExpense = null;
				criterias.clear();
				criterias.put("branch.id", branch.getId());
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("particular.name", "Expenses");
				criterias.put("presentStatus", 1);
				List<BranchSpecifics> branchExpenseSpecifics = genericDAO.findByCriteria(BranchSpecifics.class,
						criterias, entityManager);
				criterias.clear();
				criterias.put("branch.id", branch.getId());
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("particular.name", "Incomes");
				criterias.put("presentStatus", 1);
				List<BranchSpecifics> branchIncomesSpecifics = genericDAO.findByCriteria(BranchSpecifics.class,
						criterias, entityManager);
				if ("thisWeekCashExpense".equalsIgnoreCase(requirements)
						|| "previousWeekCashExpense".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekCashExpenseVarience".equalsIgnoreCase(requirements)) {
					for (BranchSpecifics bnchSpecf : branchExpenseSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						String key = bnchSpecf.getSpecifics().getId() + "CashExpense";
						if (allSpecifcsAmtData != null && allSpecifcsAmtData.containsKey(key)) {
							thisWeekResult = new Double(allSpecifcsAmtData.get(key).toString());
						}
						/*
						 * sbquery.
						 * append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
						 * +currDateTime+"' and obj.transactionSpecifics='"+bnchSpecf.getSpecifics().
						 * getId()+"' and obj.transactionBranch='"+branch.getId()
						 * +"' and obj.transactionBranchOrganization='"+user.getOrganization().getId()
						 * +"' AND (obj.transactionPurpose=3 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.transactionDate  between '"
						 * +currentWeekStartDate+"' and '"+currentWeekEndDate+"'");
						 * List<Transaction>
						 * cashexpensetxn=genericDAO.executeSimpleQuery(sbquery.toString(),entityManager
						 * );
						 * if(cashexpensetxn.size()>0){
						 * Object val=cashexpensetxn.get(0);
						 * if(val!=null){
						 * thisWeekResult=thisWeekResult + Double.parseDouble(String.valueOf(val));
						 * }
						 * }
						 */
						StringBuilder sbquery = new StringBuilder(
								"select obj from Transaction obj WHERE obj.createdAt<'" + currDateTime
										+ "' and obj.transactionBranch='" + branch.getId()
										+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
										+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
										+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> bnchcustcreditincometxncust = genericDAO
								.executeSimpleQuery(sbquery.toString(), entityManager);
						for (Transaction txn : bnchcustcreditincometxncust) {
							criterias.clear();
							criterias.put("transaction.id", txn.getId());
							criterias.put("presentStatus", 1);
							List<TransactionItems> listTransactionItems = genericDAO
									.findByCriteria(TransactionItems.class, criterias, entityManager);
							for (TransactionItems txnItemrow : listTransactionItems) {
								if (txnItemrow.getTransactionSpecifics().getId() == bnchSpecf.getSpecifics().getId()) {
									thisWeekResult = thisWeekResult + txnItemrow.getNetAmount();
								}
							}
						}
						// Expenses due to claims transaction ident_data_Valid=23,24,25,26 or
						// tran_purpose=16,18,19 cash payment when settling travel advance/settle
						// expense reimbursement
						// Settle advance for expense(18)/req for expense reimbursemnt(19) say for Item
						// Pen
						StringBuilder sb = new StringBuilder();
						sb.append(
								"select SUM(obj.newAmount) from ClaimTransaction obj WHERE obj.transactionBranchOrganization='"
										+ user.getOrganization().getId() + "' and obj.transactionBranch='"
										+ branch.getId() + "' and obj.advanceForExpenseItems='"
										+ bnchSpecf.getSpecifics().getId()
										+ "' and obj.transactionPurpose in (18,19) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.receiptDetailType = 1 and obj.transactionDate  between '"
										+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<ClaimTransaction> clmTxnList = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
						if (!clmTxnList.isEmpty() && clmTxnList.size() > 0) {
							Object val = clmTxnList.get(0);
							if (val != null) {
								thisWeekResult += Double.parseDouble(String.valueOf(val));
							}
						}
						// get data for Settle Travel Advance - Either Travel Expense/Boarding and
						// Lodging expense/Other travel/Fixed Travel Allowance
						sb = new StringBuilder();
						sb.append("select SUM(obj.itemValue) from ClaimsSettlement obj WHERE obj.organization='"
								+ user.getOrganization().getId() + "' and obj.branch='" + branch.getId()
								+ "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transaction.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transaction.receiptDetailType in (1,0) and obj.transaction.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<ClaimsSettlement> settlementclmTxnList = genericDAO.executeSimpleQuery(sb.toString(),
								entityManager);
						if (!settlementclmTxnList.isEmpty() && settlementclmTxnList.size() > 0) {
							Object val = settlementclmTxnList.get(0);
							if (val != null) {
								thisWeekResult += Double.parseDouble(String.valueOf(val));
							}
						}
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcashexpensetxn = genericDAO.executeSimpleQuery(sbquery1.toString(),
								entityManager);
						if (pwcashexpensetxn.size() > 0) {
							Object val = pwcashexpensetxn.get(0);
							if (val != null) {
								previousWeekResult += Double.parseDouble(String.valueOf(val));
							}
						}
						// Expenses due to claims transaction ident_data_Valid=23,24,25,26 or
						// tran_purpose=16,18,19 cash payment when settling travel advance/settle
						// expense reimbursement
						// Settle advance for expense(18)/req for expense reimbursemnt(19) say for Item
						// Pen
						sb = new StringBuilder();
						sb.append(
								"select SUM(obj.newAmount) from ClaimTransaction obj WHERE obj.transactionBranchOrganization='"
										+ user.getOrganization().getId() + "' and obj.transactionBranch='"
										+ branch.getId() + "' and obj.advanceForExpenseItems='"
										+ bnchSpecf.getSpecifics().getId()
										+ "' and obj.transactionPurpose in (18,19) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.receiptDetailType = 1 and obj.transactionDate  between '"
										+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						clmTxnList = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
						if (!clmTxnList.isEmpty() && clmTxnList.size() > 0) {
							Object val = clmTxnList.get(0);
							if (val != null) {
								previousWeekResult += Double.parseDouble(String.valueOf(val));
							}
						}
						// get data for Settle Travel Advance - Either Travel Expense/Boarding and
						// Lodging expense/Other travel/Fixed Travel Allowance
						sb = new StringBuilder();
						sb.append("select SUM(obj.itemValue) from ClaimsSettlement obj WHERE obj.organization='"
								+ user.getOrganization().getId() + "' and obj.branch='" + branch.getId()
								+ "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transaction.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transaction.receiptDetailType in (1,0) and obj.transaction.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						settlementclmTxnList = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
						if (!settlementclmTxnList.isEmpty() && settlementclmTxnList.size() > 0) {
							Object val = settlementclmTxnList.get(0);
							if (val != null) {
								previousWeekResult += Double.parseDouble(String.valueOf(val));
							}
						}
						Double variance = Double.parseDouble(decimalFormat.format(thisWeekResult - previousWeekResult));
						log.log(Level.INFO, "variance value=" + variance);
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								// percentageVarience=decimalFormat.format(((thisWeekResult/previousWeekResult)*100))+"%";
								percentageVarience = decimalFormat.format(((variance / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						log.log(Level.INFO, "percentage variance value=" + percentageVarience);
						ObjectNode row = Json.newObject();
						if (thisWeekResult != 0 || previousWeekResult != 0) {
							row.put("accountHeadName", bnchSpecf.getSpecifics().getName());
							row.put("thisWeekAmount", decimalFormat.format(thisWeekResult));
							row.put("previousWeekAmount", IdosConstants.decimalFormat
									.format(Double.parseDouble(String.valueOf(previousWeekResult))));
							row.put("variance",
									IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
							coaBreakUps.add(row);
						}
					}
				}
				if ("thisWeekCashIncome".equalsIgnoreCase(requirements)
						|| "previousWeeKCashIncome".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekCashIncomeVarience".equalsIgnoreCase(requirements)) {
					for (BranchSpecifics bnchSpecf : branchIncomesSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						String key = bnchSpecf.getSpecifics().getId() + "CashIncome";
						if (allSpecifcsAmtData != null && allSpecifcsAmtData.containsKey(key)) {
							thisWeekResult = new Double(allSpecifcsAmtData.get(key).toString());
						}
						StringBuilder sbquery = new StringBuilder("");
						sbquery.append("select obj from Transaction obj WHERE obj.createdAt<'" + currDateTime
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=1 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> bnchcustcreditincometxncust = genericDAO
								.executeSimpleQuery(sbquery.toString(), entityManager);
						for (Transaction txn : bnchcustcreditincometxncust) {
							criterias.clear();
							criterias.put("transaction.id", txn.getId());
							criterias.put("presentStatus", 1);
							List<TransactionItems> listTransactionItems = genericDAO
									.findByCriteria(TransactionItems.class, criterias, entityManager);
							for (TransactionItems txnItemrow : listTransactionItems) {
								if (txnItemrow.getTransactionSpecifics().getId() == bnchSpecf.getSpecifics().getId()) {
									/* thisWeekResult= thisWeekResult +txnItemrow.getGrossAmount(); */
									thisWeekResult = thisWeekResult + txnItemrow.getNetAmount();

								}
							}
						}
						// sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE
						// obj.createdAt<'"+currDateTime+"' and
						// obj.transactionSpecifics='"+bnchSpecf.getSpecifics().getId()+"' and
						// obj.transactionBranch='"+branch.getId()+"' and
						// obj.transactionBranchOrganization='"+user.getOrganization().getId()+"' AND
						// obj.transactionPurpose=1 and obj.transactionStatus='Accounted' and
						// obj.transactionDate between '"+currentWeekStartDate+"' and
						// '"+currentWeekEndDate+"'");
						/*
						 * sbquery.
						 * append("select SUM(obj.netAmount) from TransactionItems obj WHERE obj.createdAt<'"
						 * +currDateTime+"' and obj.transactionSpecifics='"+bnchSpecf.getSpecifics().
						 * getId()+"' and obj.transaction.transactionBranch='"+branch.getId()
						 * +"' and obj.transaction.transactionBranchOrganization='"+user.getOrganization
						 * ().getId()
						 * +"' AND obj.transaction.transactionPurpose=1 and obj.transaction.transactionStatus='Accounted' and obj.transaction.transactionDate  between '"
						 * +currentWeekStartDate+"' and '"+currentWeekEndDate+"'");
						 * List<Transaction>
						 * cashexpensetxn=genericDAO.executeSimpleQuery(sbquery.toString(),entityManager
						 * );
						 * if(cashexpensetxn.size()>0){
						 * Object val=cashexpensetxn.get(0);
						 * if(val!=null){
						 * thisWeekResult=thisWeekResult +
						 * Double.parseDouble(decimalFormat.format(val));
						 * }
						 * }
						 */
						sbquery = new StringBuilder("");
						sbquery.append("select obj from Transaction obj WHERE obj.createdAt<'" + currDateTime
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=1 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						bnchcustcreditincometxncust = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
						for (Transaction txn : bnchcustcreditincometxncust) {
							criterias.clear();
							criterias.put("transaction.id", txn.getId());
							criterias.put("presentStatus", 1);
							List<TransactionItems> listTransactionItems = genericDAO
									.findByCriteria(TransactionItems.class, criterias, entityManager);
							for (TransactionItems txnItemrow : listTransactionItems) {
								if (txnItemrow.getTransactionSpecifics().getId() == bnchSpecf.getSpecifics().getId()) {
									/* previousWeekResult= previousWeekResult +txnItemrow.getGrossAmount(); */
									previousWeekResult = previousWeekResult + txnItemrow.getNetAmount();
								}
							}
						}
						// sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE
						// obj.createdAt<'"+currDateTime+"' and
						// obj.transactionSpecifics='"+bnchSpecf.getSpecifics().getId()+"' and
						// obj.transactionBranch='"+branch.getId()+"' and
						// obj.transactionBranchOrganization='"+user.getOrganization().getId()+"' AND
						// obj.transactionPurpose=1 and obj.transactionStatus='Accounted' and
						// obj.transactionDate between '"+previousWeekStartDate+"' and
						// '"+previousWeekEndDate+"'");
						/*
						 * sbquery1.
						 * append("select SUM(obj.netAmount) from TransactionItems obj WHERE obj.createdAt<'"
						 * +currDateTime+"' and obj.transactionSpecifics='"+bnchSpecf.getSpecifics().
						 * getId()+"' and obj.transaction.transactionBranch='"+branch.getId()
						 * +"' and obj.transaction.transactionBranchOrganization='"+user.getOrganization
						 * ().getId()
						 * +"' AND obj.transaction.transactionPurpose=1 and obj.transaction.transactionStatus='Accounted' and obj.transaction.transactionDate  between '"
						 * +previousWeekStartDate+"' and '"+previousWeekEndDate+"'");
						 * List<Transaction>
						 * pwcashexpensetxn=genericDAO.executeSimpleQuery(sbquery1.toString(),
						 * entityManager);
						 * if(pwcashexpensetxn.size()>0){
						 * Object val=pwcashexpensetxn.get(0);
						 * if(val!=null){
						 * row.put("previousWeekAmount",
						 * decimalFormat.format(Double.parseDouble(String.valueOf(val))));
						 * previousWeekResult=Double.parseDouble(String.valueOf(val));
						 * }else{
						 * row.put("previousWeekAmount", 0.0);
						 * }
						 * }else{
						 * row.put("previousWeekAmount", 0.0);
						 * }
						 */
						Double variance = Double.parseDouble(decimalFormat.format(thisWeekResult - previousWeekResult));
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								percentageVarience = decimalFormat.format(((variance / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						if (thisWeekResult != 0 || previousWeekResult != 0) {
							ObjectNode row = Json.newObject();
							row.put("accountHeadName", bnchSpecf.getSpecifics().getName());
							row.put("thisWeekAmount", decimalFormat.format(thisWeekResult));
							row.put("previousWeekAmount", IdosConstants.decimalFormat.format(previousWeekResult));
							row.put("variance",
									IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
							coaBreakUps.add(row);
						}
					}
				}
				if ("thisWeekCreditExpense".equalsIgnoreCase(requirements)
						|| "previousWeekCreditExpense".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekCreditExpenseVarience".equalsIgnoreCase(requirements)) {
					for (BranchSpecifics bnchSpecf : branchExpenseSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						String key = bnchSpecf.getSpecifics().getId() + "CreditExpense";
						if (allSpecifcsAmtData != null && allSpecifcsAmtData.containsKey(key)) {
							thisWeekResult = new Double(allSpecifcsAmtData.get(key).toString());
						}
						StringBuilder sbquery = new StringBuilder("");
						/*
						 * sbquery.
						 * append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
						 * +currDateTime+"' and obj.transactionSpecifics='"+bnchSpecf.getSpecifics().
						 * getId()+"' and obj.transactionBranch='"+branch.getId()
						 * +"' and obj.transactionBranchOrganization='"+user.getOrganization().getId()
						 * +"' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.transactionDate  between '"
						 * +currentWeekStartDate+"' and '"+currentWeekEndDate+"'");
						 * List<Transaction>
						 * cashexpensetxn=genericDAO.executeSimpleQuery(sbquery.toString(),entityManager
						 * );
						 * if(cashexpensetxn.size()>0){
						 * Object val=cashexpensetxn.get(0);
						 * if(val!=null){
						 * thisWeekResult = thisWeekResult +Double.parseDouble(String.valueOf(val));
						 * }
						 * }
						 */
						sbquery.append("select obj from Transaction obj WHERE obj.createdAt<'" + currDateTime
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> bnchcustcreditincometxncust = genericDAO
								.executeSimpleQuery(sbquery.toString(), entityManager);
						for (Transaction txn : bnchcustcreditincometxncust) {
							criterias.clear();
							criterias.put("transaction.id", txn.getId());
							criterias.put("presentStatus", 1);
							List<TransactionItems> listTransactionItems = genericDAO
									.findByCriteria(TransactionItems.class, criterias, entityManager);
							for (TransactionItems txnItemrow : listTransactionItems) {
								if (txnItemrow.getTransactionSpecifics().getId() == bnchSpecf.getSpecifics().getId()) {
									/* thisWeekResult= thisWeekResult +txnItemrow.getGrossAmount(); */
									thisWeekResult = thisWeekResult + txnItemrow.getNetAmount();
								}
							}
						}
						// purchase retrun of 200 then totalbuy - purchaseReturn should be shown
						sbquery = new StringBuilder("");
						sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=13 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> salesReturnTxn = genericDAO.executeSimpleQuery(sbquery.toString(),
								entityManager);
						if (salesReturnTxn.size() > 0) {
							Object val = salesReturnTxn.get(0);
							if (val != null) {
								thisWeekResult = thisWeekResult - Double.parseDouble(String.valueOf(val));
							}
						}
						// Expenses due to claims transaction ident_data_Valid=23,24,25,26 or
						// tran_purpose=16,18,19 cash payment when settling travel advance/settle
						// expense reimbursement
						// Settle advance for expense(18)/req for expense reimbursemnt(19) say for Item
						// Pen
						StringBuilder sb = new StringBuilder();
						sb.append(
								"select SUM(obj.newAmount) from ClaimTransaction obj WHERE obj.transactionBranchOrganization='"
										+ user.getOrganization().getId() + "' and obj.transactionBranch='"
										+ branch.getId() + "' and obj.advanceForExpenseItems='"
										+ bnchSpecf.getSpecifics().getId()
										+ "' and obj.transactionPurpose in (18,19) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.receiptDetailType = 2 and obj.transactionDate  between '"
										+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<ClaimTransaction> clmTxnList = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
						if (!clmTxnList.isEmpty() && clmTxnList.size() > 0) {
							Object val = clmTxnList.get(0);
							if (val != null) {
								thisWeekResult += Double.parseDouble(String.valueOf(val));
							}
						}
						// get data for Settle Travel Advance - Either Travel Expense/Boarding and
						// Lodging expense/Other travel/Fixed Travel Allowance
						/*
						 * sb = new StringBuilder();
						 * sb.
						 * append("select SUM(obj.itemValue) from ClaimsSettlement obj WHERE obj.organization='"
						 * +user.getOrganization().getId()+"' and obj.branch='"+branch.getId()
						 * +"' and obj.transactionSpecifics='"+bnchSpecf.getSpecifics().getId()
						 * +"' and obj.transaction.transactionStatus='Accounted' and obj.transaction.receiptDetailType = 2 and obj.transaction.transactionDate  between '"
						 * +currentWeekStartDate+"' and '"+currentWeekEndDate+"'");
						 * List<ClaimsSettlement>
						 * settlementclmTxnList=genericDAO.executeSimpleQuery(sb.toString(),
						 * entityManager);
						 * if(!settlementclmTxnList.isEmpty() && settlementclmTxnList.size()>0){
						 * Object val=settlementclmTxnList.get(0);
						 * if(val!=null){
						 * thisWeekResult+=Double.parseDouble(String.valueOf(val));
						 * }
						 * }
						 */
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcashexpensetxn = genericDAO.executeSimpleQuery(sbquery1.toString(),
								entityManager);
						double cashExpPrevWeek = 0.0;
						if (pwcashexpensetxn.size() > 0) {
							Object val = pwcashexpensetxn.get(0);
							if (val != null) {
								cashExpPrevWeek = Double.parseDouble(String.valueOf(val));
							}
							previousWeekResult = cashExpPrevWeek;
						}
						// purchase retrun of 200 then totalbuy - purchaseReturn should be shown
						sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=13 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						salesReturnTxn = genericDAO.executeSimpleQuery(sbquery1.toString(), entityManager);
						if (salesReturnTxn.size() > 0) {
							Object val = salesReturnTxn.get(0);
							if (val != null) {
								previousWeekResult = cashExpPrevWeek - Double.parseDouble(String.valueOf(val));
							}
						}
						// Expenses due to claims transaction ident_data_Valid=23,24,25,26 or
						// tran_purpose=16,18,19 cash payment when settling travel advance/settle
						// expense reimbursement
						// Settle advance for expense(18)/req for expense reimbursemnt(19) say for Item
						// Pen
						sb = new StringBuilder();
						sb.append(
								"select SUM(obj.newAmount) from ClaimTransaction obj WHERE obj.transactionBranchOrganization='"
										+ user.getOrganization().getId() + "' and obj.transactionBranch='"
										+ branch.getId() + "' and obj.advanceForExpenseItems='"
										+ bnchSpecf.getSpecifics().getId()
										+ "' and obj.transactionPurpose in (18,19) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.receiptDetailType = 1 and obj.transactionDate  between '"
										+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						clmTxnList = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
						if (!clmTxnList.isEmpty() && clmTxnList.size() > 0) {
							Object val = clmTxnList.get(0);
							if (val != null) {
								previousWeekResult += Double.parseDouble(String.valueOf(val));
							}
						}
						/*
						 * else{
						 * //get data for Settle Travel Advance - Either Travel Expense/Boarding and
						 * Lodging expense/Other travel/Fixed Travel Allowance
						 * sb = new StringBuilder();
						 * sb.
						 * append("select SUM(obj.itemValue) from ClaimsSettlement obj WHERE obj.organization='"
						 * +user.getOrganization().getId()+"' and obj.branch='"+branch.getId()
						 * +"' and obj.transactionSpecifics='"+bnchSpecf.getSpecifics().getId()
						 * +"' and obj.transaction.transactionStatus='Accounted' and obj.transaction.receiptDetailType = 1 and obj.transaction.transactionDate  between '"
						 * +previousWeekStartDate+"' and '"+previousWeekEndDate+"'");
						 * List<ClaimsSettlement>
						 * settlementclmTxnList=genericDAO.executeSimpleQuery(sb.toString(),
						 * entityManager);
						 * if(!settlementclmTxnList.isEmpty() && settlementclmTxnList.size()>0){
						 * Object val=settlementclmTxnList.get(0);
						 * if(val!=null){
						 * previousWeekResult+=Double.parseDouble(String.valueOf(val));
						 * }
						 * }
						 * }
						 */
						Double variance = Double.parseDouble(decimalFormat.format(thisWeekResult - previousWeekResult));
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								percentageVarience = decimalFormat.format(((variance / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						if (thisWeekResult != 0 || previousWeekResult != 0) {
							ObjectNode row = Json.newObject();
							row.put("accountHeadName", bnchSpecf.getSpecifics().getName());
							row.put("thisWeekAmount", IdosConstants.decimalFormat.format(thisWeekResult));
							row.put("previousWeekAmount", IdosConstants.decimalFormat.format(previousWeekResult));
							row.put("variance",
									IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
							coaBreakUps.add(row);
						}
					}
				}
				if ("thisWeekCreditIncome".equalsIgnoreCase(requirements)
						|| "previousWeekCreditIncome".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekCreditIncomeVarience".equalsIgnoreCase(requirements)) {
					for (BranchSpecifics bnchSpecf : branchIncomesSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						String key = bnchSpecf.getSpecifics().getId() + "CreditIncome";
						if (allSpecifcsAmtData != null && allSpecifcsAmtData.containsKey(key)) {
							thisWeekResult = new Double(allSpecifcsAmtData.get(key).toString());
						}
						StringBuilder sbquery = new StringBuilder("");
						sbquery.append("select obj from Transaction obj WHERE obj.createdAt<'" + currDateTime
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> bnchcustcreditincometxncust = genericDAO
								.executeSimpleQuery(sbquery.toString(), entityManager);
						for (Transaction txn : bnchcustcreditincometxncust) {
							criterias.clear();
							criterias.put("transaction.id", txn.getId());
							criterias.put("presentStatus", 1);
							List<TransactionItems> listTransactionItems = genericDAO
									.findByCriteria(TransactionItems.class, criterias, entityManager);
							for (TransactionItems txnItemrow : listTransactionItems) {
								if (txnItemrow.getTransactionSpecifics().getId() == bnchSpecf.getSpecifics().getId()) {
									/* thisWeekResult= thisWeekResult +txnItemrow.getGrossAmount(); */
									thisWeekResult = thisWeekResult + txnItemrow.getNetAmount();
								}
							}
						}
						// sales retrun of 200 then totalsales - salesReturn should be shown
						sbquery = new StringBuilder("");
						sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=12 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> salesReturnTxn = genericDAO.executeSimpleQuery(sbquery.toString(),
								entityManager);
						if (salesReturnTxn.size() > 0) {
							Object val = salesReturnTxn.get(0);
							if (val != null) {
								thisWeekResult = thisWeekResult - Double.parseDouble(String.valueOf(val));
							}
						}
						sbquery = new StringBuilder("");
						sbquery.append("select obj from Transaction obj WHERE obj.createdAt<'" + currDateTime
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						bnchcustcreditincometxncust = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
						for (Transaction txn : bnchcustcreditincometxncust) {
							criterias.clear();
							criterias.put("transaction.id", txn.getId());
							criterias.put("presentStatus", 1);
							List<TransactionItems> listTransactionItems = genericDAO
									.findByCriteria(TransactionItems.class, criterias, entityManager);
							for (TransactionItems txnItemrow : listTransactionItems) {
								if (txnItemrow.getTransactionSpecifics().getId() == bnchSpecf.getSpecifics().getId()) {
									/* previousWeekResult= previousWeekResult +txnItemrow.getGrossAmount(); */
									previousWeekResult = previousWeekResult + txnItemrow.getNetAmount();
								}
							}
						}
						// sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE
						// obj.createdAt<'"+currDateTime+"' and
						// obj.transactionSpecifics='"+bnchSpecf.getSpecifics().getId()+"' and
						// obj.transactionBranch='"+branch.getId()+"' and
						// obj.transactionBranchOrganization='"+user.getOrganization().getId()+"' AND
						// obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and
						// obj.transactionDate between '"+previousWeekStartDate+"' and
						// '"+previousWeekEndDate+"'");
						/*
						 * sbquery1.
						 * append("select SUM(obj.netAmount) from TransactionItems obj WHERE obj.createdAt<'"
						 * +currDateTime+"' and obj.transactionSpecifics='"+bnchSpecf.getSpecifics().
						 * getId()+"' and obj.transaction.transactionBranch='"+branch.getId()
						 * +"' and obj.transaction.transactionBranchOrganization='"+user.getOrganization
						 * ().getId()
						 * +"' AND obj.transaction.transactionPurpose=2 and obj.transaction.transactionStatus='Accounted' and obj.transaction.transactionDate  between '"
						 * +previousWeekStartDate+"' and '"+previousWeekEndDate+"'");
						 * List<Transaction>
						 * pwcashexpensetxn=genericDAO.executeSimpleQuery(sbquery1.toString(),
						 * entityManager);
						 * double cashExpPrevWeek=0.0;
						 * if(pwcashexpensetxn.size()>0){
						 * Object val=pwcashexpensetxn.get(0);
						 * if(val!=null){
						 * cashExpPrevWeek=Double.parseDouble(String.valueOf(val));
						 * }
						 * previousWeekResult = cashExpPrevWeek;
						 * }
						 */
						// sales retrun of 200 then totalsales - salesReturn should be shown
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=12 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						salesReturnTxn = genericDAO.executeSimpleQuery(sbquery1.toString(), entityManager);
						if (salesReturnTxn.size() > 0) {
							Object val = salesReturnTxn.get(0);
							if (val != null) {
								previousWeekResult = previousWeekResult - Double.parseDouble(String.valueOf(val));
							}
						}
						Double variance = Double.parseDouble(decimalFormat.format(thisWeekResult - previousWeekResult));
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								percentageVarience = decimalFormat.format(((variance / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						if (thisWeekResult != 0 || previousWeekResult != 0) {
							ObjectNode row = Json.newObject();
							row.put("accountHeadName", bnchSpecf.getSpecifics().getName());
							row.put("thisWeekAmount", IdosConstants.decimalFormat.format(thisWeekResult));
							row.put("previousWeekAmount", IdosConstants.decimalFormat.format(previousWeekResult));
							row.put("variance",
									IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
							coaBreakUps.add(row);
						}
					}
				}
				if ("thisWeekExpenseBudget".equalsIgnoreCase(requirements)
						|| "previousWeekExpenseBudget".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekExpenseBudgetVarience".equalsIgnoreCase(requirements)) {
					for (BranchSpecifics bnchSpecf : branchExpenseSpecifics) {
						ObjectNode row = Json.newObject();
						row.put("accountHeadName", bnchSpecf.getSpecifics().getName());
						StringBuilder sbquery = new StringBuilder("");
						sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> cashexpensetxn = genericDAO.executeSimpleQuery(sbquery.toString(),
								entityManager);
						if (cashexpensetxn.size() > 0) {
							Object val = cashexpensetxn.get(0);
							if (val != null) {
								row.put("thisWeekAmount", Double.parseDouble(String.valueOf(val)));
								cashExpenseThisWeek = Double.parseDouble(String.valueOf(val));
							}
						}
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> creditexpensetxn = genericDAO.executeSimpleQuery(sbquery1.toString(),
								entityManager);
						if (creditexpensetxn.size() > 0) {
							Object val = creditexpensetxn.get(0);
							if (val != null) {
								creditExpenseThisWeek = Double.parseDouble(String.valueOf(val));
							}
						}
						StringBuilder sbquery2 = new StringBuilder("");
						sbquery2.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcashexpensetxn = genericDAO.executeSimpleQuery(sbquery2.toString(),
								entityManager);
						if (pwcashexpensetxn.size() > 0) {
							Object val = pwcashexpensetxn.get(0);
							if (val != null) {
								previousWeekCashExpense = Double.parseDouble(String.valueOf(val));
							}
						}
						StringBuilder sbquery3 = new StringBuilder("");
						sbquery3.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcreditexpensetxn = genericDAO.executeSimpleQuery(sbquery3.toString(),
								entityManager);
						if (pwcreditexpensetxn.size() > 0) {
							Object val = pwcreditexpensetxn.get(0);
							if (val != null) {
								previousWeekCreditExpense = Double.parseDouble(String.valueOf(val));
							}
						}
						StringBuilder expbudgetallocsbquery = new StringBuilder("");
						expbudgetallocsbquery.append("select obj from BranchSpecifics obj WHERE obj.specifics='"
								+ bnchSpecf.getSpecifics().getId() + "' and obj.branch='" + branch.getId()
								+ "' and obj.organization='" + user.getOrganization().getId()
								+ "' and obj.presentStatus=1");
						List<BranchSpecifics> expbudgetalloc = genericDAO
								.executeSimpleQuery(expbudgetallocsbquery.toString(), entityManager);
						Double expBudAllocAmount = 0.0;
						if (expbudgetalloc.size() > 0) {
							if (expbudgetalloc.get(0).getBudgetTotal() != null) {
								expBudAllocAmount = expbudgetalloc.get(0).getBudgetTotal();
							}
						}
						StringBuilder expbudgetdeducsbquery = new StringBuilder("");
						expbudgetdeducsbquery.append("select obj from BranchSpecifics obj WHERE obj.specifics='"
								+ bnchSpecf.getSpecifics().getId() + "' and obj.branch='" + branch.getId()
								+ "' and obj.organization='" + user.getOrganization().getId()
								+ "' and obj.presentStatus=1");
						List<BranchSpecifics> expbudgetdeducted = genericDAO
								.executeSimpleQuery(expbudgetdeducsbquery.toString(), entityManager);
						Double expBudDeductedAmount = 0.0;
						if (expbudgetdeducted.size() > 0) {
							if (expbudgetdeducted.get(0).getBudgetDeductedTotal() != null) {
								expBudDeductedAmount = expbudgetdeducted.get(0).getBudgetDeductedTotal();
							}
						}
						Double expBudgetAvail = 0.0;
						Double pwexpBudgetAvail = 0.0;
						if (expBudDeductedAmount == null) {
							if (expBudAllocAmount != null) {
								expBudgetAvail = Double.parseDouble(decimalFormat.format(expBudAllocAmount));
								pwexpBudgetAvail = Double.parseDouble(decimalFormat.format(expBudAllocAmount));
							}
						}
						if (expBudDeductedAmount != null && !expBudDeductedAmount.equals("")) {
							expBudgetAvail = Double.parseDouble(decimalFormat.format(expBudAllocAmount))
									- Double.parseDouble(decimalFormat.format(expBudDeductedAmount));
						}
						if (cashExpenseThisWeek != null) {
							expBudgetAvail = expBudgetAvail + cashExpenseThisWeek;
						}
						if (creditExpenseThisWeek != null) {
							expBudgetAvail = expBudgetAvail + creditExpenseThisWeek;
						}
						if (expBudgetAvail != null) {
							row.put("thisWeekAmount", decimalFormat.format(expBudgetAvail));
						}
						if (expBudDeductedAmount != null && !expBudDeductedAmount.equals("")) {
							pwexpBudgetAvail = Double.parseDouble(decimalFormat.format(expBudAllocAmount))
									- Double.parseDouble(decimalFormat.format(expBudDeductedAmount));
						}
						if (cashExpenseThisWeek != null) {
							pwexpBudgetAvail = pwexpBudgetAvail + cashExpenseThisWeek;
						}
						if (creditExpenseThisWeek != null) {
							pwexpBudgetAvail = pwexpBudgetAvail + creditExpenseThisWeek;
						}
						if (previousWeekCashExpense != null) {
							pwexpBudgetAvail = pwexpBudgetAvail + previousWeekCashExpense;
						}
						if (previousWeekCreditExpense != null) {
							pwexpBudgetAvail = pwexpBudgetAvail + previousWeekCreditExpense;
						}
						if (pwexpBudgetAvail != null) {
							row.put("previousWeekAmount", IdosConstants.decimalFormat.format(pwexpBudgetAvail));
						}
						Double variance = expBudgetAvail - pwexpBudgetAvail;
						String percentageVarience = "";
						if (pwexpBudgetAvail > 0) {
							if (expBudgetAvail > 0) {
								percentageVarience = decimalFormat.format(((expBudgetAvail / pwexpBudgetAvail) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						row.put("variance",
								IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
						coaBreakUps.add(row);
					}
				}
				if ("thisWeekTotalReceivables".equalsIgnoreCase(requirements)
						|| "previousWeekTotalReceivables".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekTotalReceivablesVarience".equalsIgnoreCase(requirements)) {
					for (BranchSpecifics bnchSpecf : branchIncomesSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						ObjectNode row = Json.newObject();
						row.put("accountHeadName", bnchSpecf.getSpecifics().getName());
						StringBuilder sbquery = new StringBuilder("");
						sbquery.append("select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> creditIncomeCustomerNetPaymentMade = genericDAO
								.executeSimpleQuery(sbquery.toString(), entityManager);
						Object creditIncomeCustPaymentMade = creditIncomeCustomerNetPaymentMade.get(0);
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> creditincometxn = genericDAO.executeSimpleQuery(sbquery1.toString(),
								entityManager);
						Double netRecievableThisWeek = 0.0;
						Double netRecievablePreviousWeek = 0.0;
						if (creditIncomeCustPaymentMade != null && !creditIncomeCustPaymentMade.equals("")) {
							if (creditincometxn.size() > 0) {
								Object val = creditincometxn.get(0);
								if (val != null && !val.equals("")) {
									netRecievableThisWeek = Double.parseDouble(decimalFormat.format(val))
											- Double.parseDouble(decimalFormat.format(creditIncomeCustPaymentMade));
								}
							}
						}
						if (creditIncomeCustPaymentMade == null) {
							if (creditincometxn.size() > 0) {
								Object val = creditincometxn.get(0);
								if (val != null && !val.equals("")) {
									netRecievableThisWeek = Double.parseDouble(decimalFormat.format(val));
								}
							}
						}
						if (netRecievableThisWeek != null) {
							row.put("thisWeekAmount", decimalFormat.format(netRecievableThisWeek));
							thisWeekResult = netRecievableThisWeek;
						} else {
							row.put("thisWeekAmount", 0.0);
						}
						StringBuilder sbquery2 = new StringBuilder("");
						sbquery2.append("select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcreditIncomeCustomerNetPaymentMade = genericDAO
								.executeSimpleQuery(sbquery2.toString(), entityManager);
						Object pwcreditIncomeCustPaymentMade = pwcreditIncomeCustomerNetPaymentMade.get(0);
						StringBuilder sbquery3 = new StringBuilder("");
						sbquery3.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcreditincometxn = genericDAO.executeSimpleQuery(sbquery3.toString(),
								entityManager);
						if (pwcreditIncomeCustPaymentMade != null && !pwcreditIncomeCustPaymentMade.equals("")) {
							if (pwcreditincometxn.size() > 0) {
								Object val = pwcreditincometxn.get(0);
								if (val != null && !val.equals("")) {
									netRecievablePreviousWeek = Double.parseDouble(decimalFormat.format(val))
											- Double.parseDouble(decimalFormat.format(pwcreditIncomeCustPaymentMade));
								}
							}
						}
						if (pwcreditIncomeCustPaymentMade == null) {
							if (pwcreditincometxn.size() > 0) {
								Object val = pwcreditincometxn.get(0);
								if (val != null && !val.equals("")) {
									netRecievablePreviousWeek = Double.parseDouble(decimalFormat.format(val));
								}
							}
						}
						if (netRecievablePreviousWeek != null) {
							row.put("previousWeekAmount",
									IdosConstants.decimalFormat.format(netRecievablePreviousWeek));
							previousWeekResult = netRecievablePreviousWeek;
						} else {
							row.put("previousWeekAmount", 0.0);
						}
						Double variance = thisWeekResult - previousWeekResult;
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								percentageVarience = decimalFormat.format(((thisWeekResult / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						row.put("variance",
								IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
						coaBreakUps.add(row);
					}
				}
				if ("thisWeekTotalPayables".equalsIgnoreCase(requirements)
						|| "previousWeekTotalPayables".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekTotalPayablesVarience".equalsIgnoreCase(requirements)) {
					for (BranchSpecifics bnchSpecf : branchExpenseSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						ObjectNode row = Json.newObject();
						row.put("accountHeadName", bnchSpecf.getSpecifics().getName());
						StringBuilder sbquery = new StringBuilder("");
						sbquery.append("select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> creditExpenseVendorNetPaymentMade = genericDAO
								.executeSimpleQuery(sbquery.toString(), entityManager);
						Object creditExpenseVendPaymentMade = creditExpenseVendorNetPaymentMade.get(0);
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> creditexpensetxn = genericDAO.executeSimpleQuery(sbquery1.toString(),
								entityManager);
						Double netPayablesThisWeek = 0.0;
						Double netPayablesPreviousWeek = 0.0;
						if (creditExpenseVendPaymentMade != null && !creditExpenseVendPaymentMade.equals("")) {
							if (creditexpensetxn.size() > 0) {
								Object val = creditexpensetxn.get(0);
								if (val != null && !val.equals("")) {
									netPayablesThisWeek = Double.parseDouble(decimalFormat.format(val))
											- Double.parseDouble(decimalFormat.format(creditExpenseVendPaymentMade));
								}
							}
						}
						if (creditExpenseVendPaymentMade == null) {
							if (creditexpensetxn.size() > 0) {
								Object val = creditexpensetxn.get(0);
								if (val != null && !val.equals("")) {
									netPayablesThisWeek = Double.parseDouble(decimalFormat.format(val));
								}
							}
						}
						if (netPayablesThisWeek != null) {
							row.put("thisWeekAmount", decimalFormat.format(netPayablesThisWeek));
							thisWeekResult = netPayablesThisWeek;
						} else {
							row.put("thisWeekAmount", 0.0);
						}
						StringBuilder sbquery2 = new StringBuilder("");
						sbquery2.append("select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcreditExpenseVendorNetPaymentMade = genericDAO
								.executeSimpleQuery(sbquery2.toString(), entityManager);
						Object pwcreditExpenseVendPaymentMade = pwcreditExpenseVendorNetPaymentMade.get(0);
						StringBuilder sbquery3 = new StringBuilder("");
						sbquery3.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='" + bnchSpecf.getSpecifics().getId()
								+ "' and obj.transactionBranch='" + branch.getId()
								+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcreditexpensetxn = genericDAO.executeSimpleQuery(sbquery3.toString(),
								entityManager);
						if (pwcreditExpenseVendPaymentMade != null && !pwcreditExpenseVendPaymentMade.equals("")) {
							if (pwcreditexpensetxn.size() > 0) {
								Object val = pwcreditexpensetxn.get(0);
								if (val != null && !val.equals("")) {
									netPayablesPreviousWeek = Double.parseDouble(decimalFormat.format(val))
											- Double.parseDouble(decimalFormat.format(pwcreditExpenseVendPaymentMade));
								}
							}
						}
						if (pwcreditExpenseVendPaymentMade == null) {
							if (pwcreditexpensetxn.size() > 0) {
								Object val = pwcreditexpensetxn.get(0);
								if (val != null && !val.equals("")) {
									netPayablesPreviousWeek = Double.parseDouble(decimalFormat.format(val));
								}
							}
						}
						if (netPayablesPreviousWeek != null) {
							row.put("previousWeekAmount", IdosConstants.decimalFormat.format(netPayablesPreviousWeek));
							previousWeekResult = netPayablesPreviousWeek;
						} else {
							row.put("previousWeekAmount", 0.0);
						}
						Double variance = thisWeekResult - previousWeekResult;
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								percentageVarience = decimalFormat.format(((thisWeekResult / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						row.put("variance",
								IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
						coaBreakUps.add(row);
					}
				}
			}
			if (project != null) {
				Double cashExpenseThisWeek = null;
				Double creditExpenseThisWeek = null;
				Double previousWeekCashExpense = null;
				Double previousWeekCreditExpense = null;
				StringBuilder sbr = new StringBuilder("");
				sbr.append("select obj from Transaction obj where obj.transactionProject='" + project.getId()
						+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
						+ "' and obj.transactionSpecifics.particularsId.name='Expenses' and obj.presentStatus=1 GROUP BY obj.transactionSpecifics");
				List<Transaction> projectExpenseSpecifics = genericDAO.executeSimpleQuery(sbr.toString(),
						entityManager);
				sbr.delete(0, sbr.length());
				sbr.append("select obj from Transaction obj where obj.transactionProject='" + project.getId()
						+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
						+ "' and obj.transactionSpecifics.particularsId.name='Incomes' and obj.presentStatus=1 GROUP BY obj.transactionSpecifics");
				List<Transaction> projectIncomesSpecifics = genericDAO.executeSimpleQuery(sbr.toString(),
						entityManager);
				if ("thisWeekCashExpense".equalsIgnoreCase(requirements)
						|| "previousWeekCashExpense".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekCashExpenseVarience".equalsIgnoreCase(requirements)) {
					for (Transaction txn : projectExpenseSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						ObjectNode row = Json.newObject();
						row.put("accountHeadName", txn.getTransactionSpecifics().getName());
						StringBuilder sbquery = new StringBuilder("");
						sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> cashexpensetxn = genericDAO.executeSimpleQuery(sbquery.toString(),
								entityManager);
						if (cashexpensetxn.size() > 0) {
							Object val = cashexpensetxn.get(0);
							if (val != null) {
								row.put("thisWeekAmount", Double.parseDouble(String.valueOf(val)));
								thisWeekResult = Double.parseDouble(String.valueOf(val));
							} else {
								row.put("thisWeekAmount", 0.0);
							}
						} else {
							row.put("thisWeekAmount", 0.0);
						}
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcashexpensetxn = genericDAO.executeSimpleQuery(sbquery1.toString(),
								entityManager);
						if (pwcashexpensetxn.size() > 0) {
							Object val = pwcashexpensetxn.get(0);
							if (val != null) {
								row.put("previousWeekAmount",
										IdosConstants.decimalFormat.format(Double.parseDouble(String.valueOf(val))));
								previousWeekResult = Double.parseDouble(String.valueOf(val));
							} else {
								row.put("previousWeekAmount", 0.0);
							}
						} else {
							row.put("previousWeekAmount", 0.0);
						}
						Double variance = thisWeekResult - previousWeekResult;
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								percentageVarience = decimalFormat.format(((thisWeekResult / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						row.put("variance",
								IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
						coaBreakUps.add(row);
					}
				}
				if ("thisWeekCashIncome".equalsIgnoreCase(requirements)
						|| "previousWeeKCashIncome".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekCashIncomeVarience".equalsIgnoreCase(requirements)) {
					for (Transaction txn : projectIncomesSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						ObjectNode row = Json.newObject();
						row.put("accountHeadName", txn.getTransactionSpecifics().getName());
						StringBuilder sbquery = new StringBuilder("");
						sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=1 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> cashexpensetxn = genericDAO.executeSimpleQuery(sbquery.toString(),
								entityManager);
						if (cashexpensetxn.size() > 0) {
							Object val = cashexpensetxn.get(0);
							if (val != null) {
								row.put("thisWeekAmount", Double.parseDouble(String.valueOf(val)));
								thisWeekResult = Double.parseDouble(String.valueOf(val));
							} else {
								row.put("thisWeekAmount", 0.0);
							}
						} else {
							row.put("thisWeekAmount", 0.0);
						}
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=1 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcashexpensetxn = genericDAO.executeSimpleQuery(sbquery1.toString(),
								entityManager);
						if (pwcashexpensetxn.size() > 0) {
							Object val = pwcashexpensetxn.get(0);
							if (val != null) {
								row.put("previousWeekAmount",
										IdosConstants.decimalFormat.format(Double.parseDouble(String.valueOf(val))));
								previousWeekResult = Double.parseDouble(String.valueOf(val));
							} else {
								row.put("previousWeekAmount", 0.0);
							}
						} else {
							row.put("previousWeekAmount", 0.0);
						}
						Double variance = thisWeekResult - previousWeekResult;
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								percentageVarience = decimalFormat.format(((thisWeekResult / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						row.put("variance",
								IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
						coaBreakUps.add(row);
					}
				}
				if ("thisWeekCreditExpense".equalsIgnoreCase(requirements)
						|| "previousWeekCreditExpense".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekCreditExpenseVarience".equalsIgnoreCase(requirements)) {
					for (Transaction txn : projectExpenseSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						ObjectNode row = Json.newObject();
						row.put("accountHeadName", txn.getTransactionSpecifics().getName());
						StringBuilder sbquery = new StringBuilder("");
						sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> cashexpensetxn = genericDAO.executeSimpleQuery(sbquery.toString(),
								entityManager);
						if (cashexpensetxn.size() > 0) {
							Object val = cashexpensetxn.get(0);
							if (val != null) {
								row.put("thisWeekAmount", Double.parseDouble(String.valueOf(val)));
								thisWeekResult = Double.parseDouble(String.valueOf(val));
							} else {
								row.put("thisWeekAmount", 0.0);
							}
						} else {
							row.put("thisWeekAmount", 0.0);
						}
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcashexpensetxn = genericDAO.executeSimpleQuery(sbquery1.toString(),
								entityManager);
						if (pwcashexpensetxn.size() > 0) {
							Object val = pwcashexpensetxn.get(0);
							if (val != null) {
								row.put("previousWeekAmount",
										IdosConstants.decimalFormat.format(Double.parseDouble(String.valueOf(val))));
								previousWeekResult = Double.parseDouble(String.valueOf(val));
							} else {
								row.put("previousWeekAmount", 0.0);
							}
						} else {
							row.put("previousWeekAmount", 0.0);
						}
						Double variance = thisWeekResult - previousWeekResult;
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								percentageVarience = decimalFormat.format(((thisWeekResult / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						row.put("variance",
								IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
						coaBreakUps.add(row);
					}
				}
				if ("thisWeekCreditIncome".equalsIgnoreCase(requirements)
						|| "previousWeekCreditIncome".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekCreditIncomeVarience".equalsIgnoreCase(requirements)) {
					for (Transaction txn : projectIncomesSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						ObjectNode row = Json.newObject();
						row.put("accountHeadName", txn.getTransactionSpecifics().getName());
						StringBuilder sbquery = new StringBuilder("");
						sbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> cashexpensetxn = genericDAO.executeSimpleQuery(sbquery.toString(),
								entityManager);
						if (cashexpensetxn.size() > 0) {
							Object val = cashexpensetxn.get(0);
							if (val != null) {
								row.put("thisWeekAmount", Double.parseDouble(String.valueOf(val)));
								thisWeekResult = Double.parseDouble(String.valueOf(val));
							} else {
								row.put("thisWeekAmount", 0.0);
							}
						} else {
							row.put("thisWeekAmount", 0.0);
						}
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcashexpensetxn = genericDAO.executeSimpleQuery(sbquery1.toString(),
								entityManager);
						if (pwcashexpensetxn.size() > 0) {
							Object val = pwcashexpensetxn.get(0);
							if (val != null) {
								row.put("previousWeekAmount",
										IdosConstants.decimalFormat.format(Double.parseDouble(String.valueOf(val))));
								previousWeekResult = Double.parseDouble(String.valueOf(val));
							} else {
								row.put("previousWeekAmount", 0.0);
							}
						} else {
							row.put("previousWeekAmount", 0.0);
						}
						Double variance = thisWeekResult - previousWeekResult;
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								percentageVarience = decimalFormat.format(((thisWeekResult / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						row.put("variance",
								IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
						coaBreakUps.add(row);
					}
				}
				if ("thisWeekTotalReceivables".equalsIgnoreCase(requirements)
						|| "previousWeekTotalReceivables".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekTotalReceivablesVarience".equalsIgnoreCase(requirements)) {
					for (Transaction txn : projectIncomesSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						ObjectNode row = Json.newObject();
						row.put("accountHeadName", txn.getTransactionSpecifics().getName());
						StringBuilder sbquery = new StringBuilder("");
						sbquery.append("select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> creditIncomeCustomerNetPaymentMade = genericDAO
								.executeSimpleQuery(sbquery.toString(), entityManager);
						Object creditIncomeCustPaymentMade = creditIncomeCustomerNetPaymentMade.get(0);
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> creditincometxn = genericDAO.executeSimpleQuery(sbquery1.toString(),
								entityManager);
						Double netRecievableThisWeek = 0.0;
						Double netRecievablePreviousWeek = 0.0;
						if (creditIncomeCustPaymentMade != null && !creditIncomeCustPaymentMade.equals("")) {
							if (creditincometxn.size() > 0) {
								Object val = creditincometxn.get(0);
								if (val != null && !val.equals("")) {
									netRecievableThisWeek = Double.parseDouble(decimalFormat.format(val))
											- Double.parseDouble(decimalFormat.format(creditIncomeCustPaymentMade));
								}
							}
						}
						if (creditIncomeCustPaymentMade == null) {
							if (creditincometxn.size() > 0) {
								Object val = creditincometxn.get(0);
								if (val != null && !val.equals("")) {
									netRecievableThisWeek = Double.parseDouble(decimalFormat.format(val));
								}
							}
						}
						if (netRecievableThisWeek != null) {
							row.put("thisWeekAmount", decimalFormat.format(netRecievableThisWeek));
							thisWeekResult = netRecievableThisWeek;
						} else {
							row.put("thisWeekAmount", 0.0);
						}
						StringBuilder sbquery2 = new StringBuilder("");
						sbquery2.append("select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcreditIncomeCustomerNetPaymentMade = genericDAO
								.executeSimpleQuery(sbquery2.toString(), entityManager);
						Object pwcreditIncomeCustPaymentMade = pwcreditIncomeCustomerNetPaymentMade.get(0);
						StringBuilder sbquery3 = new StringBuilder("");
						sbquery3.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcreditincometxn = genericDAO.executeSimpleQuery(sbquery3.toString(),
								entityManager);
						if (pwcreditIncomeCustPaymentMade != null && !pwcreditIncomeCustPaymentMade.equals("")) {
							if (pwcreditincometxn.size() > 0) {
								Object val = pwcreditincometxn.get(0);
								if (val != null && !val.equals("")) {
									netRecievablePreviousWeek = Double.parseDouble(decimalFormat.format(val))
											- Double.parseDouble(decimalFormat.format(pwcreditIncomeCustPaymentMade));
								}
							}
						}
						if (pwcreditIncomeCustPaymentMade == null) {
							if (pwcreditincometxn.size() > 0) {
								Object val = pwcreditincometxn.get(0);
								if (val != null && !val.equals("")) {
									netRecievablePreviousWeek = Double.parseDouble(decimalFormat.format(val));
								}
							}
						}
						if (netRecievablePreviousWeek != null) {
							row.put("previousWeekAmount", IdosConstants.decimalFormat
									.format(decimalFormat.format(netRecievablePreviousWeek)));
							previousWeekResult = netRecievablePreviousWeek;
						} else {
							row.put("previousWeekAmount", 0.0);
						}
						Double variance = thisWeekResult - previousWeekResult;
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								percentageVarience = decimalFormat.format(((thisWeekResult / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						row.put("variance",
								IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
						coaBreakUps.add(row);
					}
				}
				if ("thisWeekTotalPayables".equalsIgnoreCase(requirements)
						|| "previousWeekTotalPayables".equalsIgnoreCase(requirements)
						|| "thisWeekPreviousWeekTotalPayablesVarience".equalsIgnoreCase(requirements)) {
					for (Transaction txn : projectExpenseSpecifics) {
						Double thisWeekResult = 0.0;
						Double previousWeekResult = 0.0;
						ObjectNode row = Json.newObject();
						row.put("accountHeadName", txn.getTransactionSpecifics().getName());
						StringBuilder sbquery = new StringBuilder("");
						sbquery.append("select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> creditExpenseVendorNetPaymentMade = genericDAO
								.executeSimpleQuery(sbquery.toString(), entityManager);
						Object creditExpenseVendPaymentMade = creditExpenseVendorNetPaymentMade.get(0);
						StringBuilder sbquery1 = new StringBuilder("");
						sbquery1.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
						List<Transaction> creditexpensetxn = genericDAO.executeSimpleQuery(sbquery1.toString(),
								entityManager);
						Double netPayablesThisWeek = 0.0;
						Double netPayablesPreviousWeek = 0.0;
						if (creditExpenseVendPaymentMade != null && !creditExpenseVendPaymentMade.equals("")) {
							if (creditexpensetxn.size() > 0) {
								Object val = creditexpensetxn.get(0);
								if (val != null && !val.equals("")) {
									netPayablesThisWeek = Double.parseDouble(decimalFormat.format(val))
											- Double.parseDouble(decimalFormat.format(creditExpenseVendPaymentMade));
								}
							}
						}
						if (creditExpenseVendPaymentMade == null) {
							if (creditexpensetxn.size() > 0) {
								Object val = creditexpensetxn.get(0);
								if (val != null && !val.equals("")) {
									netPayablesThisWeek = Double.parseDouble(decimalFormat.format(val));
								}
							}
						}
						if (netPayablesThisWeek != null) {
							row.put("thisWeekAmount", decimalFormat.format(netPayablesThisWeek));
							thisWeekResult = netPayablesThisWeek;
						} else {
							row.put("thisWeekAmount", 0.0);
						}
						StringBuilder sbquery2 = new StringBuilder("");
						sbquery2.append("select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcreditExpenseVendorNetPaymentMade = genericDAO
								.executeSimpleQuery(sbquery2.toString(), entityManager);
						Object pwcreditExpenseVendPaymentMade = pwcreditExpenseVendorNetPaymentMade.get(0);
						StringBuilder sbquery3 = new StringBuilder("");
						sbquery3.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.createdAt<'"
								+ currDateTime + "' and obj.transactionSpecifics='"
								+ txn.getTransactionSpecifics().getId() + "' and obj.transactionProject='"
								+ project.getId() + "' and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
						List<Transaction> pwcreditexpensetxn = genericDAO.executeSimpleQuery(sbquery3.toString(),
								entityManager);
						if (pwcreditExpenseVendPaymentMade != null && !pwcreditExpenseVendPaymentMade.equals("")) {
							if (pwcreditexpensetxn.size() > 0) {
								Object val = pwcreditexpensetxn.get(0);
								if (val != null && !val.equals("")) {
									netPayablesPreviousWeek = Double.parseDouble(decimalFormat.format(val))
											- Double.parseDouble(decimalFormat.format(pwcreditExpenseVendPaymentMade));
								}
							}
						}
						if (pwcreditExpenseVendPaymentMade == null) {
							if (pwcreditexpensetxn.size() > 0) {
								Object val = pwcreditexpensetxn.get(0);
								if (val != null && !val.equals("")) {
									netPayablesPreviousWeek = Double.parseDouble(decimalFormat.format(val));
								}
							}
						}
						if (netPayablesPreviousWeek != null) {
							row.put("previousWeekAmount",
									IdosConstants.decimalFormat.format(decimalFormat.format(netPayablesPreviousWeek)));
							previousWeekResult = netPayablesPreviousWeek;
						} else {
							row.put("previousWeekAmount", 0.0);
						}
						Double variance = thisWeekResult - previousWeekResult;
						String percentageVarience = "";
						if (previousWeekResult > 0) {
							if (thisWeekResult > 0) {
								percentageVarience = decimalFormat.format(((thisWeekResult / previousWeekResult) * 100))
										+ "%";
							} else {
								percentageVarience = "100.00%";
							}
						}
						row.put("variance",
								IdosConstants.decimalFormat.format(variance) + "(" + percentageVarience + ")");
						coaBreakUps.add(row);
					}
				}
			}
		} catch (Exception ex) {
			String uemail = "Error";
			if (user != null) {
				uemail = user.getEmail();
			}

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff.toString(), uemail, uemail,
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End");
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result downloadBudgetDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		JsonNode requests = request.body().asJson();
		Users usr = null;
		File file = null;
		try {
			String usermail = requests.findValue("useremail").asText();
			// session.adding("email", usermail);
			usr = getUserInfo(request);
			String sheetName = usr.getOrganization().getName() + "Budget";
			String path = application.path().toString() + "/logs/OrgVendorCustomer/";

			CreateExcelService excelService = new CreateExcelServiceImpl(application);
			String fileName = excelService.createBudgetDetails(usr.getOrganization(), path, sheetName);
			// result.put("filename", filename);
			file = new File(path + fileName);
			return Results.ok(file).withHeader("ContentType", "application/json")
					.withHeader("ContentType", "application/xlsx")
					.withHeader("Content-Disposition", "attachment; filename=" + fileName);

		} catch (Exception ex) {

			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	@Transactional
	public Result showPendingTxnDetails(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode results = Json.newObject();
		JsonNode requests = request.body().asJson();
		Users usr = null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("individualTxnData");
			String usermail = requests.findValue("usermail").asText();
			String selecteTxnRefNumber = requests.findValue("selecteTxnRefNumber").asText();
			criterias.clear();
			criterias.put("transactionRefNumber", selecteTxnRefNumber);
			criterias.put("presentStatus", 1);
			Transaction usrTxn = genericDAO.getByCriteria(Transaction.class, criterias, entityManager);
			session.adding("email", usermail);
			usr = getUserInfo(request);
			ObjectNode event = Json.newObject();
			event.put("id", usrTxn.getId());
			if (usrTxn.getTransactionBranch() != null) {
				event.put("branchName", usrTxn.getTransactionBranch().getName());
			} else {
				event.put("branchName", "");
			}
			if (usrTxn.getTransactionProject() != null) {
				event.put("projectName", usrTxn.getTransactionProject().getName());
			} else {
				event.put("projectName", "");
			}
			if (usrTxn.getTransactionSpecifics() != null) {
				event.put("itemName", usrTxn.getTransactionSpecifics().getName());
			} else {
				event.put("itemName", "");
			}
			if (usrTxn.getTransactionSpecifics() != null) {
				if (usrTxn.getTransactionSpecifics().getParentSpecifics() != null
						&& !usrTxn.getTransactionSpecifics().getParentSpecifics().equals("")) {
					event.put("itemParentName", usrTxn.getTransactionSpecifics().getParentSpecifics().getName());
				} else {
					event.put("itemParentName", usrTxn.getTransactionSpecifics().getParticularsId().getName());
				}
			} else {
				event.put("itemParentName", "");
			}
			/*
			 * if(usrTxn.getBudgetAvailDuringTxn()!=null){
			 * String []budgetAvailableArr=usrTxn.getBudgetAvailDuringTxn().split(":");
			 * log.log(Level.INFO, "budget available="+ budgetAvailableArr[0]);
			 * event.put("budgetAvailable", budgetAvailableArr[0]);
			 * Double budgetExceededBy=0.0;
			 * if(budgetAvailableArr.length>1){
			 * event.put("budgetAvailableAmt", budgetAvailableArr[1]);
			 * 
			 * log.log(Level.INFO, "budget available amount="+ budgetAvailableArr[1]);
			 * budgetExceededBy=Double.parseDouble(budgetAvailableArr[1])-usrTxn.
			 * getNetAmount();
			 * log.log(Level.INFO, "budget exceeded by="+budgetExceededBy);
			 * }else{
			 * event.put("budgetAvailableAmt", "");
			 * }
			 * if(budgetExceededBy>0.0 || budgetExceededBy<0.0){
			 * event.put("budgetExceededBy", decimalFormat.format(budgetExceededBy));
			 * }else{
			 * event.put("budgetExceededBy", "");
			 * }
			 * }
			 */
			if (usrTxn.getTransactionExceedingBudget() != null) {

				event.put("budgetAvailable", usrTxn.getTransactionItems().get(0).getBudgetAvailDuringTxn());
				Double budgetExceededBy = 0.0;

				event.put("budgetAvailableAmt",
						Double.parseDouble(usrTxn.getTransactionItems().get(0).getBudgetAvailDuringTxn()));

				// log.log(Level.INFO, "budget available amount="+ budgetAvailableArr[1]);
				budgetExceededBy = Double.parseDouble(usrTxn.getTransactionItems().get(0).getBudgetAvailDuringTxn())
						- usrTxn.getNetAmount();
				log.log(Level.INFO, "budget exceeded by=" + budgetExceededBy);

				if (budgetExceededBy > 0.0 || budgetExceededBy < 0.0) {
					event.put("budgetExceededBy", IdosConstants.decimalFormat.format(budgetExceededBy));
				} else {
					event.put("budgetExceededBy", "");
				}
			} else {
				event.put("budgetAvailable", "");
				log.log(Level.INFO, "budget exceeded by=0");
				event.put("budgetAvailableAmt", "");
				log.log(Level.INFO, "budget available amount=0");
			}
			if (usrTxn.getActualAllocatedBudget() != null) {
				String[] budgetAllocatedArr = usrTxn.getActualAllocatedBudget().split(":");
				event.put("budgetAllocated", budgetAllocatedArr[0]);
				if (budgetAllocatedArr.length > 1) {
					event.put("budgetAllocatedAmt", budgetAllocatedArr[1]);
				} else {
					event.put("budgetAllocatedAmt", "");
				}
			} else {
				event.put("budgetAllocated", "");
				event.put("budgetAllocatedAmt", "");
			}
			if (usrTxn.getTransactionVendorCustomer() != null) {
				event.put("customerVendorName", usrTxn.getTransactionVendorCustomer().getName());
			} else {
				if (usrTxn.getTransactionUnavailableVendorCustomer() != null) {
					event.put("customerVendorName", usrTxn.getTransactionUnavailableVendorCustomer());
				} else {
					event.put("customerVendorName", "");
				}
			}
			event.put("transactionPurpose", usrTxn.getTransactionPurpose().getTransactionPurpose());
			event.put("txnDate", idosdf.format(usrTxn.getTransactionDate()));
			String invoiceDate = "";
			String invoiceDateLabel = "";
			if (usrTxn.getTransactionInvoiceDate() != null) {
				invoiceDateLabel = "INVOICE DATE:";
				invoiceDate = idosdf.format(usrTxn.getTransactionInvoiceDate());
			}
			event.put("invoiceDateLabel", invoiceDateLabel);
			event.put("invoiceDate", invoiceDate);
			if (usrTxn.getReceiptDetailsType() != null) {
				if (usrTxn.getReceiptDetailsType() == 1) {
					event.put("paymentMode", "CASH");
				}
				if (usrTxn.getReceiptDetailsType() == 2) {
					event.put("paymentMode", "BANK");
				}
			} else {
				event.put("paymentMode", "");
			}
			if (usrTxn.getNoOfUnits() != null) {
				event.put("noOfUnit", usrTxn.getNoOfUnits());
			} else {
				event.put("noOfUnit", "");
			}
			if (usrTxn.getPricePerUnit() != null) {
				event.put("unitPrice", IdosConstants.decimalFormat.format(usrTxn.getPricePerUnit()));
			} else {
				event.put("unitPrice", "");
			}
			if (usrTxn.getGrossAmount() != null) {
				event.put("grossAmount", IdosConstants.decimalFormat.format(usrTxn.getGrossAmount()));
			} else {
				event.put("grossAmount", "");
			}
			event.put("netAmount", IdosConstants.decimalFormat.format(usrTxn.getNetAmount()));
			if (usrTxn.getNetAmountResultDescription() != null
					&& !usrTxn.getNetAmountResultDescription().equals("null")) {
				event.put("netAmtDesc", usrTxn.getNetAmountResultDescription());
			} else {
				event.put("netAmtDesc", "");
			}
			event.put("status", usrTxn.getTransactionStatus());
			event.put("createdBy", usrTxn.getCreatedBy().getEmail());
			if (usrTxn.getApproverActionBy() != null) {
				event.put("approverLabel", "APPROVER:");
				event.put("approverEmail", usrTxn.getApproverActionBy().getEmail());
			} else {
				event.put("approverLabel", "");
				event.put("approverEmail", "");
			}
			if (usrTxn.getSupportingDocs() != null) {
				event.put("txnDocument", usrTxn.getSupportingDocs());
			} else {
				event.put("txnDocument", "");
			}
			if (usrTxn.getRemarks() != null) {
				event.put("txnRemarks", usrTxn.getRemarks());
			} else {
				event.put("txnRemarks", "");
			}
			String txnSpecialStatus = "";
			if (usrTxn.getTransactionExceedingBudget() != null && usrTxn.getKlFollowStatus() != null) {
				if (usrTxn.getTransactionExceedingBudget() == 1 && usrTxn.getKlFollowStatus() == 0) {
					txnSpecialStatus = "Transaction Exceeding Budget && Knowledge library not followed";
				}
				if (usrTxn.getTransactionExceedingBudget() == 1 && usrTxn.getKlFollowStatus() == 1) {
					txnSpecialStatus = "Transaction Exceeding Budget";
				}
			}
			if (usrTxn.getTransactionExceedingBudget() == null && usrTxn.getKlFollowStatus() != null) {
				if (usrTxn.getKlFollowStatus() == 0) {
					txnSpecialStatus = "Knowledge Library Not Followed";
				}
			}
			if (usrTxn.getTransactionExceedingBudget() != null && usrTxn.getKlFollowStatus() == null) {
				txnSpecialStatus = "Transaction Exceeding Budget";
			}
			event.put("txnSpecialStatus", txnSpecialStatus);
			event.put("useremail", usr.getEmail());
			event.put("approverEmails", usrTxn.getApproverEmails());
			event.put("additionalapproverEmails", usrTxn.getAdditionalApproverEmails());
			event.put("selectedAdditionalApproval", usrTxn.getSelectedAdditionalApprover());
			an.add(event);
		} catch (Exception ex) {

			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result availableLocations(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode results = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("ecommerceAllLocationData");
			String enteredValue = json.findPath("enteredValue").asText();
			String newsbquery = "select obj from IdosLocations obj WHERE obj.locationName like ? and obj.presentStatus=1 ORDER BY obj.locationName";
			Query query = entityManager.createQuery(newsbquery);
			ArrayList inparam = new ArrayList(1);
			inparam.add(enteredValue + "%");
			List<IdosLocations> idosLocationslist = genericDAO.queryWithParams(newsbquery.toString(), entityManager,
					inparam);
			for (IdosLocations idosLoc : idosLocationslist) {
				ObjectNode row = Json.newObject();
				row.put("locationName", idosLoc.getLocationName());
				an.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "availableLocations Email", "availableLocations Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result plotbranchAggregateData(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		JsonNode requests = request.body().asJson();
		ObjectNode results = Json.newObject();
		StringBuilder newsbquery = new StringBuilder();
		Users usr = null;
		Specifics specf = null;
		Branch firstBranch = null;
		Branch secondBranch = null;
		Double firstBranchNetAmount = 0.0;
		Double secondBranchNetAmount = 0.0;
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("dashboardAggregateData");
			String usermail = requests.findValue("usermail").asText();
			String entereditemname = requests.findValue("entereditemname").asText();
			String enteredlocationfirst = requests.findValue("enteredlocationfirst").asText();
			String enteredlocationsecond = requests.findValue("enteredlocationsecond").asText();
			String enteredPeriod = requests.findValue("enteredPeriod").asText();
			session.adding("email", usermail);
			usr = getUserInfo(request);

			criterias.put("name", entereditemname);
			criterias.put("presentStatus", 1);
			specf = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);

			criterias.clear();
			criterias.put("location", enteredlocationfirst);
			criterias.put("presentStatus", 1);
			firstBranch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);

			criterias.clear();
			criterias.put("location", enteredlocationsecond);
			criterias.put("presentStatus", 1);
			secondBranch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);

			String periodStartDate = null;
			String periodEndDate = null;
			Calendar newcal = Calendar.getInstance();
			if (enteredPeriod != null && enteredPeriod.equals("1")) {
				newcal.add(Calendar.DAY_OF_WEEK, -(1 * 30));
				periodStartDate = StaticController.mysqldf.format(newcal.getTime());
				periodEndDate = StaticController.mysqldf.format(Calendar.getInstance().getTime());
			}
			if (enteredPeriod != null && enteredPeriod.equals("3")) {
				newcal.add(Calendar.DAY_OF_WEEK, -(3 * 30));
				periodStartDate = StaticController.mysqldf.format(newcal.getTime());
				periodEndDate = StaticController.mysqldf.format(Calendar.getInstance().getTime());
			}
			if (enteredPeriod != null && enteredPeriod.equals("6")) {
				newcal.add(Calendar.DAY_OF_WEEK, -(6 * 30));
				periodStartDate = StaticController.mysqldf.format(newcal.getTime());
				periodEndDate = StaticController.mysqldf.format(Calendar.getInstance().getTime());
			}
			if (enteredPeriod != null && enteredPeriod.equals("12")) {
				newcal.add(Calendar.DAY_OF_WEEK, -(12 * 30));
				periodStartDate = StaticController.mysqldf.format(newcal.getTime());
				periodEndDate = StaticController.mysqldf.format(Calendar.getInstance().getTime());
			}
			StringBuilder sbquery = new StringBuilder("");
			if (specf != null && firstBranch != null) {
				sbquery.append("select SUM(obj.netAmount) from Transaction obj where obj.transactionSpecifics.name='"
						+ specf.getName() + "' and obj.transactionBranch.location='" + firstBranch.getLocation()
						+ "' and (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
						+ periodStartDate + "' and '" + periodEndDate + "' ");

				log.log(Level.INFO, sbquery.toString());
				List<Transaction> firstLocationNetTransaction = genericDAO.executeSimpleQuery(sbquery.toString(),
						entityManager);
				if (firstLocationNetTransaction.size() > 0) {
					Object val = firstLocationNetTransaction.get(0);
					if (val != null && !val.equals("")) {
						firstBranchNetAmount = Double.parseDouble(decimalFormat.format(val));
					}
				}
			}

			sbquery = new StringBuilder("");
			if (specf != null && secondBranch != null) {
				sbquery.append("select SUM(obj.netAmount) from Transaction obj where obj.transactionSpecifics.name='"
						+ specf.getName() + "' and obj.transactionBranch.location='" + secondBranch.getLocation()
						+ "' and (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
						+ periodStartDate + "' and '" + periodEndDate + "' ");

				log.log(Level.INFO, sbquery.toString());
				List<Transaction> secondLocationNetTransaction = genericDAO.executeSimpleQuery(sbquery.toString(),
						entityManager);
				if (secondLocationNetTransaction.size() > 0) {
					Object val = secondLocationNetTransaction.get(0);
					if (val != null && !val.equals("")) {
						secondBranchNetAmount = Double.parseDouble(decimalFormat.format(val));
					}
				}
			}

			ObjectNode row = Json.newObject();
			row.put("itemName", entereditemname);
			row.put("firstLocation", enteredlocationfirst);
			row.put("enteredlocationsecond", enteredlocationsecond);
			row.put("firstBranchNetAmount", firstBranchNetAmount);
			row.put("secondBranchNetAmount", secondBranchNetAmount);
			an.add(row);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result highestExpenseIncomeBI(Http.Request request) {
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		JsonNode requests = request.body().asJson();
		ObjectNode results = Json.newObject();
		StringBuilder sbquery = new StringBuilder();
		Users user = null;
		Statement stmt = null;
		log.log(Level.FINE, ">>>> Start " + request);
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("dashboardHighestExpenseIncomeData");
			String usermail = requests.findValue("usermail").asText();
			String clickSourceString = requests.findValue("clickSourceString").asText();
			session.adding("email", usermail);
			user = getUserInfo(request);
			Calendar newestcal = Calendar.getInstance();
			String currentDate = mysqldf.format(newestcal.getTime());
			String currDate = currentDate;
			currentDate += " 23:59:59";
			newestcal.add(Calendar.DAY_OF_WEEK, -30);
			String forteenDaysBack = mysqldf.format(newestcal.getTime());
			String forteenBackDate = forteenDaysBack;
			forteenDaysBack += " 00:00:00";
			if (clickSourceString.equals("expenseLast3")) {
				sbquery.append(
						"SELECT TRANSACTION_BRANCH, SUM(NET_AMOUNT) AS NET_AMOUNT FROM TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION='"
								+ user.getOrganization().getId()
								+ "' AND TRANSACTION_STATUS='Accounted' AND (TRANSACTION_PURPOSE=3 OR TRANSACTION_PURPOSE=4 OR TRANSACTION_PURPOSE=11) AND TRANSACTION_ACTIONDATE BETWEEN  '"
								+ forteenBackDate + "' and '" + currDate
								+ "' GROUP BY TRANSACTION_BRANCH ORDER BY NET_AMOUNT DESC LIMIT 1");
				Query query = entityManager.createQuery(sbquery.toString());
				List<Object[]> resultList = query.getResultList();
				for (Object[] resultObj : resultList) {
					Integer branchId = (Integer) resultObj[0];
					Double maxNetAmount = (Double) resultObj[1];
					double totalMax = 0.0;
					Branch maxTxnBranchEntity = null;
					if (branchId != null && branchId > 0) {
						maxTxnBranchEntity = Branch.findById(branchId.longValue());
						StringBuilder sbr = new StringBuilder("");
						sbr.append("select obj from Transaction obj where obj.transactionBranchOrganization='"
								+ user.getOrganization().getId() + "' and obj.transactionBranch='"
								+ maxTxnBranchEntity.getId()
								+ "' and obj.transactionStatus='Accounted' and (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11) and obj.transactionDate between '"
								+ forteenBackDate + "' and '" + currDate
								+ "' and obj.presentStatus=1 GROUP BY obj.transactionSpecifics ORDER BY obj.netAmount desc");
						List<Transaction> highestThreeTxnItemWise = genericDAO
								.executeSimpleQueryWithLimit(sbr.toString(), entityManager, 3);
						for (Transaction itemTxn : highestThreeTxnItemWise) {
							if (itemTxn.getTransactionPurpose().getId() == 3
									|| itemTxn.getTransactionPurpose().getId() == 4) {
								sbr = new StringBuilder("");
								sbr.append("select obj from TransactionItems obj where obj.transaction.id='"
										+ itemTxn.getId() + "' and obj.presentStatus=1 ORDER BY obj.netAmount desc");
								List<TransactionItems> highestTxnItem = genericDAO.executeSimpleQuery(sbr.toString(),
										entityManager);
								if (highestTxnItem != null && highestTxnItem.size() > 0) {
									for (TransactionItems txnItem : highestTxnItem) {
										ObjectNode row = Json.newObject();
										row.put("itemName", txnItem.getTransactionSpecifics().getName());
										row.put("branchName", maxTxnBranchEntity.getName());
										row.put("itemAmount", decimalFormat.format(txnItem.getNetAmount()));
										totalMax = totalMax + txnItem.getNetAmount();
										an.add(row);
									}
								}
							} else {
								ObjectNode row = Json.newObject();
								row.put("itemName", itemTxn.getTransactionSpecifics().getName());
								row.put("branchName", maxTxnBranchEntity.getName());
								row.put("itemAmount", decimalFormat.format(itemTxn.getNetAmount()));
								totalMax = totalMax + itemTxn.getNetAmount();
								an.add(row);
							}
						}
					}
					maxNetAmount = totalMax;
				}
			} else if (clickSourceString.equals("incomeLast3")) {
				sbquery.append(
						"SELECT TRANSACTION_BRANCH, SUM(NET_AMOUNT) AS NET_AMOUNT FROM TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION='"
								+ user.getOrganization().getId()
								+ "' AND TRANSACTION_STATUS='Accounted' AND (TRANSACTION_PURPOSE=1 OR TRANSACTION_PURPOSE=2) AND TRANSACTION_ACTIONDATE BETWEEN  '"
								+ forteenBackDate + "' and '" + currDate
								+ "' GROUP BY TRANSACTION_BRANCH ORDER BY NET_AMOUNT DESC LIMIT 1");
				// stmt = (Statement) con.createStatement();
				// ResultSet rs = stmt.executeQuery(sbquery.toString());
				Query query = entityManager.createQuery(sbquery.toString());
				List<Object[]> resultList = query.getResultList();
				// while(rs.next()){
				for (Object[] resultObj : resultList) {
					// Integer branchId=rs.getInt("TRANSACTION_BRANCH");
					// Double maxNetAmount=rs.getDouble("NET_AMOUNT");
					Integer branchId = (Integer) resultObj[0];
					Double maxNetAmount = (Double) resultObj[1];
					Branch maxTxnBranchEntity = null;
					if (branchId != null && branchId > 0) {
						maxTxnBranchEntity = Branch.findById(branchId.longValue());
						StringBuilder sbr = new StringBuilder("");
						sbr.append("select obj from Transaction obj where obj.transactionBranchOrganization='"
								+ user.getOrganization().getId() + "' and obj.transactionBranch='"
								+ maxTxnBranchEntity.getId()
								+ "' and obj.transactionStatus='Accounted' and (obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.presentStatus=1 and obj.transactionDate between '"
								+ forteenBackDate + "' and '" + currDate
								+ "' GROUP BY obj.transactionSpecifics ORDER BY obj.netAmount desc");
						List<Transaction> highestThreeTxnItemWise = genericDAO
								.executeSimpleQueryWithLimit(sbr.toString(), entityManager, 3);
						for (Transaction itemTxn : highestThreeTxnItemWise) {
							ObjectNode row = Json.newObject();
							row.put("itemName", itemTxn.getTransactionSpecifics().getName());
							row.put("branchName", maxTxnBranchEntity.getName());
							row.put("itemAmount", decimalFormat.format(itemTxn.getNetAmount()));
							an.add(row);
						}
					}
				}
			} else if (clickSourceString.equals("projectexpenseLast3")) {
				sbquery.delete(0, sbquery.length());
				sbquery.append(
						"SELECT TRANSACTION_PROJECT, SUM(NET_AMOUNT) AS NET_AMOUNT FROM TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION='"
								+ user.getOrganization().getId()
								+ "' AND TRANSACTION_PROJECT IS NOT NULL AND TRANSACTION_STATUS='Accounted' AND (TRANSACTION_PURPOSE=3 OR TRANSACTION_PURPOSE=4 OR TRANSACTION_PURPOSE=11) AND TRANSACTION_ACTIONDATE BETWEEN  '"
								+ forteenBackDate + "' and '" + currDate
								+ "' GROUP BY TRANSACTION_PROJECT ORDER BY NET_AMOUNT DESC LIMIT 1");
				// stmt = (Statement) con.createStatement();
				// ResultSet rs = stmt.executeQuery(sbquery.toString());
				Query query = entityManager.createQuery(sbquery.toString());
				List<Object[]> resultList = query.getResultList();
				// while(rs.next()){
				for (Object[] resultObj : resultList) {
					// Integer projectId=rs.getInt("TRANSACTION_PROJECT");
					// Double maxNetAmount=rs.getDouble("NET_AMOUNT");
					Integer projectId = (Integer) resultObj[0];
					Double maxNetAmount = (Double) resultObj[1];

					Project maxTxnProjectEntity = null;
					if (projectId != null && projectId > 0) {
						maxTxnProjectEntity = Project.findById(projectId.longValue());
						StringBuilder sbr = new StringBuilder("");
						sbr.append("select obj from Transaction obj where obj.transactionBranchOrganization='"
								+ user.getOrganization().getId() + "' and obj.transactionProject='"
								+ maxTxnProjectEntity.getId()
								+ "' and obj.transactionStatus='Accounted' and (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11) and obj.presentStatus=1 and obj.transactionDate between '"
								+ forteenBackDate + "' and '" + currDate
								+ "' GROUP BY obj.transactionSpecifics ORDER BY obj.netAmount desc");
						List<Transaction> highestThreeTxnItemWise = genericDAO
								.executeSimpleQueryWithLimit(sbr.toString(), entityManager, 3);
						for (Transaction itemTxn : highestThreeTxnItemWise) {
							ObjectNode row = Json.newObject();
							row.put("itemName", itemTxn.getTransactionSpecifics().getName());
							row.put("branchName", maxTxnProjectEntity.getName());
							row.put("itemAmount", decimalFormat.format(itemTxn.getNetAmount()));
							an.add(row);
						}
					}
				}
			} else if (clickSourceString.equals("projectincomeLast3")) {
				sbquery.delete(0, sbquery.length());
				sbquery.append(
						"SELECT TRANSACTION_PROJECT, SUM(NET_AMOUNT) AS NET_AMOUNT FROM TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION='"
								+ user.getOrganization().getId()
								+ "' AND TRANSACTION_PROJECT IS NOT NULL AND TRANSACTION_STATUS='Accounted' AND (TRANSACTION_PURPOSE=1 OR TRANSACTION_PURPOSE=2) AND TRANSACTION_ACTIONDATE BETWEEN  '"
								+ forteenBackDate + "' and '" + currDate
								+ "' GROUP BY TRANSACTION_PROJECT ORDER BY NET_AMOUNT DESC LIMIT 1");
				// stmt = (Statement) con.createStatement();
				// ResultSet rs = stmt.executeQuery(sbquery.toString());
				Query query = entityManager.createQuery(sbquery.toString());
				List<Object[]> resultList = query.getResultList();
				// while(rs.next()){
				for (Object[] resultObj : resultList) {
					// Integer projectId=rs.getInt("TRANSACTION_PROJECT");
					// Double maxNetAmount=rs.getDouble("NET_AMOUNT");
					Integer projectId = (Integer) resultObj[0];
					Double maxNetAmount = (Double) resultObj[1];

					Project maxTxnProjectEntity = null;
					if (projectId != null && projectId > 0) {
						maxTxnProjectEntity = Project.findById(projectId.longValue());
						StringBuilder sbr = new StringBuilder("");
						sbr.append("select obj from Transaction obj where obj.transactionBranchOrganization='"
								+ user.getOrganization().getId() + "' and obj.transactionProject='"
								+ maxTxnProjectEntity.getId()
								+ "' and obj.transactionStatus='Accounted' and (obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.presentStatus=1 and obj.transactionDate between '"
								+ forteenBackDate + "' and '" + currDate
								+ "' GROUP BY obj.transactionSpecifics ORDER BY obj.netAmount desc");
						List<Transaction> highestThreeTxnItemWise = genericDAO
								.executeSimpleQueryWithLimit(sbr.toString(), entityManager, 3);
						for (Transaction itemTxn : highestThreeTxnItemWise) {
							ObjectNode row = Json.newObject();
							row.put("itemName", itemTxn.getTransactionSpecifics().getName());
							row.put("branchName", maxTxnProjectEntity.getName());
							row.put("itemAmount", decimalFormat.format(itemTxn.getNetAmount()));
							an.add(row);
						}
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result customerVendorAdvanceBI(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		JsonNode requests = request.body().asJson();
		ObjectNode results = Json.newObject();
		StringBuilder sbquery = new StringBuilder();
		Users user = null;
		Statement stmt = null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("dashboardCustomerVendorAdvanceData");
			String usermail = requests.findValue("usermail").asText();
			String clickSourceString = requests.findValue("clickSourceString").asText();
			session.adding("email", usermail);
			user = getUserInfo(request);
			Organization org = user.getOrganization();
			int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
			int finStartMonth = 4;
			int finEndMonth = 3;
			String finStartDate = null;
			String finStDt = null;
			StringBuilder startYear = null;
			String finEndDate = null;
			String finEndDt = null;
			StringBuilder endYear = null;
			if (org.getFinancialStartDate() != null) {
				finStartMonth = org.getFinancialStartDate().getMonth() + 1;
			}
			if (org.getFinancialEndDate() != null) {
				finEndMonth = org.getFinancialEndDate().getMonth() + 1;
			}
			if (currentMonth < finStartMonth) {
				startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1));
				endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
			} else {
				startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
				endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1));
			}
			if (org.getFinancialStartDate() != null && !org.getFinancialStartDate().equals("")) {
				finStDt = StaticController.idosmdtdf.format(org.getFinancialStartDate()) + "," + startYear;
				finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
			} else {
				finStDt = "Apr 01" + "," + startYear;
				finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
			}
			if (org.getFinancialEndDate() != null && !org.getFinancialEndDate().equals("")) {
				finEndDt = StaticController.idosmdtdf.format(org.getFinancialEndDate()) + "," + endYear;
				finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
			} else {
				finEndDt = "Mar 31" + "," + endYear;
				finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
			}
			if (clickSourceString.equals("customerAdvancePending")) {
				criterias.clear();
				criterias.put("type", 2);
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("presentStatus", 1);
				List<Vendor> allCustomers = genericDAO.findByCriteria(Vendor.class, criterias, entityManager);
				for (Vendor cust : allCustomers) {
					Double totalAdvanceCollected = 0.0;
					Double totalAdvanceAdjusted = 0.0;
					Double totalAdvancePending = 0.0;
					sbquery.append(
							"select COALESCE(SUM(COALESCE(T.creditAmount,0)-COALESCE(T.debitAmount,0)),0) from TrialBalanceVendorAdvance T where T.organization.id="
									+ user.getOrganization().getId()
									+ " and T.creditAmount IS NOT NULL and T.vendor.id =" + cust.getId()
									+ " and T.vendorType = 2 and T.transactionPurpose.id in (6,12) and T.presentStatus=1 and T.date between '"
									+ finStartDate + "' and '" + finEndDate + "'");
					Query createQueryCredit = entityManager.createQuery(sbquery.toString());
					totalAdvanceCollected = (Double) createQueryCredit.getSingleResult();
					sbquery.delete(0, sbquery.length());
					sbquery.append(
							"select COALESCE(SUM(COALESCE(T.debitAmount,0)-COALESCE(T.creditAmount,0)),0) from TrialBalanceVendorAdvance T where T.organization.id="
									+ user.getOrganization().getId()
									+ " and T.creditAmount IS NOT NULL and T.vendor.id =" + cust.getId()
									+ " and T.vendorType = 2 and T.transactionPurpose.id in (1,2) and T.presentStatus=1 and T.date between '"
									+ finStartDate + "' and '" + finEndDate + "'");
					Query createQueryDebit = entityManager.createQuery(sbquery.toString());
					totalAdvanceAdjusted = (Double) createQueryDebit.getSingleResult();
					sbquery.delete(0, sbquery.length());
					totalAdvancePending = totalAdvanceCollected - totalAdvanceAdjusted;
					if (totalAdvanceCollected > 0.0 && totalAdvancePending > 0.0) {
						ObjectNode row = Json.newObject();
						row.put("name", cust.getName());
						row.put("id", cust.getId());
						row.put("totalAdvanceCollected", decimalFormat.format(totalAdvanceCollected));
						row.put("totalAdvanceAdjusted", decimalFormat.format(totalAdvanceAdjusted));
						row.put("totalAdvancePending", decimalFormat.format(totalAdvancePending));
						an.add(row);
					}
				}
			}
			if (clickSourceString.equals("vendorAdvancePending")) {
				criterias.clear();
				criterias.put("type", 1);
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("presentStatus", 1);
				List<Vendor> allVendors = genericDAO.findByCriteria(Vendor.class, criterias, entityManager);
				for (Vendor vend : allVendors) {
					Double totalAdvanceCollected = 0.0;
					Double totalAdvanceAdjusted = 0.0;
					Double totalAdvancePending = 0.0;
					// sbquery.append("select SUM(obj.netAmount) from Transaction obj where
					// obj.transactionBranchOrganization.id="+user.getOrganization().getId()+" and
					// obj.transactionVendorCustomer.id="+vend.getId()+" and
					// obj.transactionStatus='Accounted' and obj.transactionPurpose.id in (8,13) and
					// obj.transactionDate between '"+finStartDate+"' and '"+finEndDate+"'");
					sbquery.append(
							"select COALESCE(SUM(COALESCE(T.debitAmount,0)-COALESCE(T.creditAmount,0)),0) from TrialBalanceVendorAdvance T where T.organization.id="
									+ user.getOrganization().getId()
									+ " and T.creditAmount IS NOT NULL and T.vendor.id =" + vend.getId()
									+ " and T.vendorType = 1 and T.presentStatus=1 and T.transactionPurpose.id in (8,13) and T.date between '"
									+ finStartDate + "' and '" + finEndDate + "'");
					Query createQueryCredit = entityManager.createQuery(sbquery.toString());
					totalAdvanceCollected = (Double) createQueryCredit.getSingleResult();
					sbquery.delete(0, sbquery.length());

					// sbquery.append("select COALESCE(SUM(COALESCE(T.adjustmentFromAdvance,0)),0)
					// from TransactionItems T where
					// T.organization.id="+user.getOrganization().getId()+" and
					// T.adjustmentFromAdvance IS NOT NULL and T.transaction.id in (select obj.id
					// from Transaction obj where
					// obj.transactionBranchOrganization.id="+user.getOrganization().getId()+" and
					// obj.transactionVendorCustomer.id="+vend.getId()+" and
					// obj.transactionStatus='Accounted' and obj.transactionPurpose.id in (3,4) and
					// obj.transactionDate between '"+finStartDate+"' and '"+finEndDate+"')");
					sbquery.append(
							"select COALESCE(SUM(COALESCE(T.creditAmount,0)-COALESCE(T.debitAmount,0)),0) from TrialBalanceVendorAdvance T where T.organization.id="
									+ user.getOrganization().getId()
									+ " and T.creditAmount IS NOT NULL and T.vendor.id =" + vend.getId()
									+ " and T.vendorType = 1 and T.presentStatus=1 and T.transactionPurpose.id in (3,4) and T.presentStatus=1 and T.date between '"
									+ finStartDate + "' and '" + finEndDate + "'");
					Query createQueryDebit = entityManager.createQuery(sbquery.toString());
					totalAdvanceAdjusted = (Double) createQueryDebit.getSingleResult();
					sbquery.delete(0, sbquery.length());

					totalAdvancePending = totalAdvanceCollected - totalAdvanceAdjusted;
					if (totalAdvanceCollected > 0.0 && totalAdvancePending > 0.0) {
						ObjectNode row = Json.newObject();
						row.put("name", vend.getName());
						row.put("id", vend.getId());
						row.put("totalAdvanceCollected", decimalFormat.format(totalAdvanceCollected));
						row.put("totalAdvanceAdjusted", decimalFormat.format(totalAdvanceAdjusted));
						row.put("totalAdvancePending", decimalFormat.format(totalAdvancePending));
						an.add(row);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result customerVendorAdvanceTransactionBI(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		JsonNode requests = request.body().asJson();
		ObjectNode results = Json.newObject();
		StringBuilder sbquery = new StringBuilder();
		Users user = null;
		Statement stmt = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("dashboardCustomerVendorAdvanceTransactionData");
			String usermail = requests.findValue("usermail").asText();
			String entityId = requests.findValue("vendCustEntityId").asText();
			String pendAmount = requests.findValue("pendAmount").asText();
			session.adding("email", usermail);
			user = getUserInfo(request);
			Organization org = user.getOrganization();
			int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
			int finStartMonth = 4;
			int finEndMonth = 3;
			String finStartDate = null;
			String finStDt = null;
			StringBuilder startYear = null;
			String finEndDate = null;
			String finEndDt = null;
			StringBuilder endYear = null;
			if (org.getFinancialStartDate() != null) {
				finStartMonth = org.getFinancialStartDate().getMonth() + 1;
			}
			if (org.getFinancialEndDate() != null) {
				finEndMonth = org.getFinancialEndDate().getMonth() + 1;
			}
			if (currentMonth < finStartMonth) {
				startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1));
				endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
			} else {
				startYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
				endYear = new StringBuilder(String.valueOf(Calendar.getInstance().get(Calendar.YEAR) + 1));
			}
			if (org.getFinancialStartDate() != null && !org.getFinancialStartDate().equals("")) {
				finStDt = StaticController.idosmdtdf.format(org.getFinancialStartDate()) + "," + startYear;
				finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
			} else {
				finStDt = "Apr 01" + "," + startYear;
				finStartDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finStDt));
			}
			if (org.getFinancialEndDate() != null && !org.getFinancialEndDate().equals("")) {
				finEndDt = StaticController.idosmdtdf.format(org.getFinancialEndDate()) + "," + endYear;
				finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
			} else {
				finEndDt = "Mar 31" + "," + endYear;
				finEndDate = StaticController.mysqldf.format(StaticController.idosdf.parse(finEndDt));
			}
			Double pendingAmount = Double.parseDouble(pendAmount);
			Double pendingAmountToCompare = 0.0;
			Vendor vendor = Vendor.findById(Long.parseLong(entityId));
			if (vendor.getType() == 1) {
				sbquery.append("select obj from Transaction obj where obj.transactionBranchOrganization='"
						+ user.getOrganization().getId() + "' and obj.transactionVendorCustomer='" + vendor.getId()
						+ "' and obj.transactionStatus='Accounted' and (obj.transactionPurpose=8 or obj.transactionPurpose=13) and obj.presentStatus=1 and obj.transactionDate  between '"
						+ finStartDate + "' and '" + finEndDate + "' ORDER BY transactionDate desc");
				List<Transaction> advanceCollectedTransaction = genericDAO.executeSimpleQuery(sbquery.toString(),
						entityManager);
				for (int i = 0; i < advanceCollectedTransaction.size(); i++) {
					Transaction txn = advanceCollectedTransaction.get(i);
					Double totalTax = 0.0d;
					if (pendingAmount > 0) {
						String txnCreatedDate = "";
						long pendingDays = 0;
						Double totalAmt = 0.0d;
						ObjectNode row = Json.newObject();
						row.put("name", vendor.getName());
						row.put("pendingAmount", decimalFormat.format(pendingAmount));
						totalTax += (txn.getTaxValue1() == null) ? 0.0d : txn.getTaxValue1();
						totalTax += (txn.getTaxValue2() == null) ? 0.0d : txn.getTaxValue2();
						totalTax += (txn.getTaxValue3() == null) ? 0.0d : txn.getTaxValue3();
						totalTax += (txn.getTaxValue4() == null) ? 0.0d : txn.getTaxValue4();
						totalTax += (txn.getTaxValue5() == null) ? 0.0d : txn.getTaxValue5();
						totalTax += (txn.getTaxValue6() == null) ? 0.0d : txn.getTaxValue6();
						totalTax += (txn.getTaxValue7() == null) ? 0.0d : txn.getTaxValue7();
						Double gross = (txn.getNetAmount() - totalTax);
						if ((pendingAmount - gross) > 0) {
							totalAmt = gross;
							pendingAmount = pendingAmount - gross;
						} else {
							totalAmt = pendingAmount;
						}
						row.put("totalAdvanceCollected", decimalFormat.format(totalAmt));
						if (txn.getTransactionDate() != null) {
							txnCreatedDate = idosdf.format(txn.getTransactionDate());
						}
						if (txn.getTransactionInvoiceDate() != null) {
							txnCreatedDate = idosdf.format(txn.getTransactionInvoiceDate());
						}
						row.put("advanceCollecteddate", txnCreatedDate);
						pendingDays = DateUtil.calculateDays(txnCreatedDate,
								idosdf.format(Calendar.getInstance().getTime()));
						row.put("totalAdvancePendingSince", "Advance Pending Since " + pendingDays + "Days");
						an.add(row);
					}
					if (pendingAmount <= 0) {
						break;
					}
				}
			}
			if (vendor.getType() == 2) {
				sbquery.append("select obj from Transaction obj where obj.transactionBranchOrganization='"
						+ user.getOrganization().getId() + "' and obj.transactionVendorCustomer='" + vendor.getId()
						+ "' and obj.transactionStatus='Accounted' and (obj.transactionPurpose=6 or obj.transactionPurpose=12) and obj.presentStatus=1 and obj.transactionDate  between '"
						+ finStartDate + "' and '" + finEndDate + "' ORDER BY transactionDate desc");
				List<Transaction> advanceCollectedTransaction = genericDAO.executeSimpleQuery(sbquery.toString(),
						entityManager);
				for (int i = 0; i < advanceCollectedTransaction.size(); i++) {
					Transaction txn = advanceCollectedTransaction.get(i);
					Double totalTax = 0.0d;
					if (pendingAmount > 0) {
						String txnCreatedDate = "";
						long pendingDays = 0;
						Double totalAmt = 0.0d;
						ObjectNode row = Json.newObject();
						row.put("name", vendor.getName());
						row.put("pendingAmount", decimalFormat.format(pendingAmount));
						totalTax += (txn.getTaxValue1() == null) ? 0.0d : txn.getTaxValue1();
						totalTax += (txn.getTaxValue2() == null) ? 0.0d : txn.getTaxValue2();
						totalTax += (txn.getTaxValue3() == null) ? 0.0d : txn.getTaxValue3();
						totalTax += (txn.getTaxValue4() == null) ? 0.0d : txn.getTaxValue4();
						totalTax += (txn.getTaxValue5() == null) ? 0.0d : txn.getTaxValue5();
						totalTax += (txn.getTaxValue6() == null) ? 0.0d : txn.getTaxValue6();
						totalTax += (txn.getTaxValue7() == null) ? 0.0d : txn.getTaxValue7();
						Double gross = (txn.getGrossAmount() - totalTax);
						if ((pendingAmount - gross) > 0) {
							totalAmt = gross;
							pendingAmount = pendingAmount - gross;
						} else {
							totalAmt = pendingAmount;
						}
						row.put("totalAdvanceCollected", decimalFormat.format(totalAmt));
						if (txn.getTransactionDate() != null) {
							txnCreatedDate = idosdf.format(txn.getTransactionDate());
						}
						row.put("advanceCollecteddate", txnCreatedDate);
						pendingDays = DateUtil.calculateDays(txnCreatedDate,
								idosdf.format(Calendar.getInstance().getTime()));
						row.put("totalAdvancePendingSince", "Advance Pending Since " + pendingDays + "Days");
						an.add(row);
					}
					if (pendingAmount <= 0) {
						break;
					}
				}
			}
		} catch (Exception ex) {

			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getTransactionExceedingBudgetGroupDetails(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				long specificId = json.findValue("specId").asLong();
				result = dashboardService.getTransactionExceedingBudgetGroupDetails(specificId, user);
			}
		} catch (Exception ex) {

			log.log(Level.SEVERE, "Error", ex);
			if (null != user) {
				String strBuff = getStackTraceMessage(ex);
				expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getDashboardFinancial(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		JsonNode json = request.body().asJson();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			user = getUserInfo(request);
			if (user != null) {
				result = dashboardService.getDashboardFinancial(user, json, entityManager);
			} else {
				return unauthorized();
			}
		} catch (Exception ex) {
			if (null != user) {
				log.log(Level.SEVERE, user.getEmail(), ex);
				String strBuff = getStackTraceMessage(ex);
				expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName());
			} else {
				log.log(Level.SEVERE, "error", ex);
			}
		}
		log.log(Level.FINE, ">>>> End");
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getDashboardProjectFinancial(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entityTransaction = entityManager.getTransaction();
		JsonNode json = request.body().asJson();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			String sessemail = session.getOptional("email").orElse("");
			if (sessemail != null && !sessemail.equals("")) {
				session.adding("email", sessemail);
				user = getUserInfo(request);
				result = dashboardService.getDashboardProjectFinancial(user, json, entityManager);
			}
		} catch (Exception ex) {

			log.log(Level.SEVERE, "Error", ex);
			if (null != user) {
				String strBuff = getStackTraceMessage(ex);
				expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	// final int dashboardType, final int graphType
	public Result getGraph(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entityTransaction = entityManager.getTransaction();
		JsonNode json = request.body().asJson();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			String sessemail = session.getOptional("email").orElse("");
			if (sessemail != null && !sessemail.equals("")) {
				session.adding("email", sessemail);
				user = getUserInfo(request);
				// result = DashboardService.getGraph(user, entityManager, dashboardType,
				// graphType);
				result = dashboardService.getGraph(user, entityManager, json);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			if (null != user) {
				String strBuff = getStackTraceMessage(ex);
				expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getProjectGraph(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entityTransaction = entityManager.getTransaction();
		JsonNode json = request.body().asJson();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			String sessemail = session.getOptional("email").orElse("");
			;
			if (sessemail != null && !sessemail.equals("")) {
				session.adding("email", sessemail);
				user = getUserInfo(request);
				result = dashboardService.getProjectGraph(user, entityManager, json);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			if (null != user) {
				String strBuff = getStackTraceMessage(ex);
				expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result customerwiseProformaInvoice(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			String usermail = json.findValue("useremail").asText();
			String branchName = json.findValue("branchName").asText();
			Branch branch = getBranch(branchName, user);
			if (user != null) {
				result = dashboardService.customerwiseProformaInvoice(result, branch, user, entityManager);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result recPayablesOpeningBalAndCurrentYearTotal(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user != null) {
				String tabElement = json.findValue("tabElement").asText();
				result = dashboardService.recPayablesOpeningBalAndCurrentYearTotal(json, tabElement, user,
						entityManager);
			} else {
				return unauthorized();
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result branchWiseReceivablePayablesGraphData(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			String tabElement = json.findValue("tabElement").asText();
			if (user != null) {
				result = dashboardService.branchWiseReceivablePayablesGraphData(json, tabElement, user, entityManager);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result displayBarnchAndPeriodWiseCustVend(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user != null) {
				result = dashboardService.displayBarnchAndPeriodWiseCustVend(result, json, user, entityManager);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok().withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result custVendOpeningBalanceBreakup(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			String tabElement = json.findValue("tabElement").asText();
			if (user != null) {
				result = dashboardService.custVendOpeningBalanceBreakup(result, tabElement, user, entityManager);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getQuotationProformaBranchBy(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Date fromDate = null;
			Date toDate = null;
			String invoiceType = json.findValue("displayReq").asText();
			Calendar calendar = Calendar.getInstance();
			String fmDate = null;
			String tDate = null;
			if (invoiceType.startsWith("previous")) {
				calendar.add(Calendar.MONTH, -1);
				fmDate = json.findValue("currDashboardFromDate") != null
						? json.findValue("currDashboardFromDate").asText()
						: null;
				tDate = json.findValue("currDashboardToDate") != null ? json.findValue("currDashboardToDate").asText()
						: null;
			} else {
				fmDate = json.findValue("prevDashboardFromDate") != null
						? json.findValue("prevDashboardFromDate").asText()
						: null;
				tDate = json.findValue("prevDashboardToDate") != null ? json.findValue("prevDashboardToDate").asText()
						: null;
			}
			if (fmDate == null || fmDate.equals("")) {
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
				fromDate = mysqldf.parse(IdosConstants.mysqldf.format(calendar.getTime()));
			} else {
				fromDate = mysqldf.parse(mysqldf.format(fmDate));
			}
			if (tDate == null || tDate.equals("")) {
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				toDate = mysqldf.parse(IdosConstants.mysqldf.format(calendar.getTime()));
			} else {
				toDate = mysqldf.parse(mysqldf.format(idosdf.parse(tDate)));
			}

			String branchName = json.findValue("branchName").asText();
			String branchId = json.findValue("branchId").asText();
			long transactionPurposeID = IdosConstants.PREPARE_QUOTATION;
			if ("thisWeekProformaInvoice".equals(invoiceType) || "previousWeekProformaInvoice".equals(invoiceType)) {
				transactionPurposeID = IdosConstants.PROFORMA_INVOICE;
			}
			Branch branch = null;
			if (branchId != null && !"".equals(branchId)) {
				branch = Branch.findById(Long.parseLong(branchId));
			}
			if (branch == null) {
				branch = getBranch(branchName, user);
			}
			quotationProformaService.getQuotationProformaProjectBy(branch, user, entityManager, transactionPurposeID,
					fromDate, toDate, result);

		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getQuotationProformaItems(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Date fromDate = null;
			Date toDate = null;
			String invoiceType = json.findValue("displayReq").asText();
			Calendar calendar = Calendar.getInstance();
			String fmDate = null;
			String tDate = null;
			if (invoiceType.startsWith("previous")) {
				calendar.add(Calendar.MONTH, -1);
				fmDate = json.findValue("currDashboardFromDate") != null
						? json.findValue("currDashboardFromDate").asText()
						: null;
				tDate = json.findValue("currDashboardToDate") != null ? json.findValue("currDashboardToDate").asText()
						: null;
			} else {
				fmDate = json.findValue("prevDashboardFromDate") != null
						? json.findValue("prevDashboardFromDate").asText()
						: null;
				tDate = json.findValue("prevDashboardToDate") != null ? json.findValue("prevDashboardToDate").asText()
						: null;
			}

			if (fmDate == null || fmDate.equals("")) {
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
				fromDate = mysqldf.parse(IdosConstants.mysqldf.format(calendar.getTime()));
			} else {
				fromDate = mysqldf.parse(mysqldf.format(fmDate));
			}

			if (tDate == null || tDate.equals("")) {
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				toDate = mysqldf.parse(IdosConstants.mysqldf.format(calendar.getTime()));
			} else {
				toDate = mysqldf.parse(mysqldf.format(idosdf.parse(tDate)));
			}

			long transactionPurposeID = IdosConstants.PREPARE_QUOTATION;
			if ("thisWeekProformaInvoice".equals(invoiceType) || "previousWeekProformaInvoice".equals(invoiceType)) {
				transactionPurposeID = IdosConstants.PROFORMA_INVOICE;
			}
			quotationProformaService.getQuotationProformaItems(entityManager, user, json, fromDate, toDate,
					transactionPurposeID, result);
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getTransactionsForItem(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Date fromDate = null;
			Date toDate = null;
			String invoiceType = json.findValue("displayReq").asText();
			Calendar calendar = Calendar.getInstance();
			String fmDate = null;
			String tDate = null;
			if (invoiceType.startsWith("previous")) {
				calendar.add(Calendar.MONTH, -1);
				fmDate = json.findValue("currDashboardFromDate") != null
						? json.findValue("currDashboardFromDate").asText()
						: null;
				tDate = json.findValue("currDashboardToDate") != null ? json.findValue("currDashboardToDate").asText()
						: null;
			} else {
				fmDate = json.findValue("prevDashboardFromDate") != null
						? json.findValue("prevDashboardFromDate").asText()
						: null;
				tDate = json.findValue("prevDashboardToDate") != null ? json.findValue("prevDashboardToDate").asText()
						: null;
			}

			if (fmDate == null || fmDate.equals("")) {
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
				fromDate = mysqldf.parse(IdosConstants.mysqldf.format(calendar.getTime()));
			} else {
				fromDate = mysqldf.parse(mysqldf.format(fmDate));
			}

			if (tDate == null || tDate.equals("")) {
				calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				toDate = mysqldf.parse(IdosConstants.mysqldf.format(calendar.getTime()));
			} else {
				toDate = mysqldf.parse(mysqldf.format(idosdf.parse(tDate)));
			}

			long transactionPurposeID = IdosConstants.PREPARE_QUOTATION;
			if ("thisWeekProformaInvoice".equals(invoiceType) || "previousWeekProformaInvoice".equals(invoiceType)) {
				transactionPurposeID = IdosConstants.PROFORMA_INVOICE;
			}
			quotationProformaService.getTransactionsForItem(entityManager, user, json, fromDate, toDate,
					transactionPurposeID, result);
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result).withHeader("ContentType", "application/json");
	}
}