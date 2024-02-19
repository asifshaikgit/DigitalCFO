package controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.idos.util.FileUtil;
import model.AlertMailLogs;
import model.Branch;
import model.BranchSpecifics;
import model.BranchVendors;
import model.ConfigParams;
import model.Organization;
import model.Transaction;
import model.UserRightInBranch;
import model.UserRightSpecifics;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import model.VendorSpecific;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.typesafe.config.Config;
import play.db.jpa.JPAApi;

import javax.transaction.Transactional;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.libs.Files.TemporaryFile;
//import play.mvc.Http.Session;
import play.mvc.Result;
import play.mvc.Results;
import service.OperationalService;
import service.OperationalServiceImpl;
import service.OrganizationService;
import service.OrganizationServiceImpl;
import views.html.*;
// import com.idos.util.MySqlConnection;
import com.idos.util.PasswordUtil;
import java.sql.Statement;
import play.Application;
import javax.inject.Inject;
import java.util.logging.Level;

/**
 * Created by Sunil Namdev
 */
public class OrganizationController extends StaticController {

	private static OperationalService operationalService = new OperationalServiceImpl();
	private static OrganizationService organizationService = new OrganizationServiceImpl();
	private Application application;
	private static EntityManager entityManager;
	private Request request;
	// private Http.Session session = request.session();

	@Inject
	public OrganizationController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
		this.application = application;
	}

	@Transactional
	public Result getOrganization(Request request) {
		log.log(Level.FINE, ">>>> Start");
		System.out.println(">>>>>><<<<<<>>>>>>>>>>>>>>>>>>>>getOrganization");
		// EntityManager entityManager=getEntityManager();
		ObjectNode results = Json.newObject();
		String email = null;
		String orgName = null;
		try {
			Users user = getUserInfo(request);
			System.out.println(">>>>>><<<<<<>>>>>>>>>>>>>>>>>>>>" + user);
			ArrayNode organ = results.putArray("organizationData");
			Organization orgn = null;
			if (user != null) {
				orgn = user.getOrganization();
				email = user.getEmail();
			}
			System.out.println(">>>>>><<<<<<>>>>>>>>>>>>>>>>>>>>getOrganization" + orgn);
			String finStDate = null, finEndDate = null;
			if (orgn != null) {
				ObjectNode row = Json.newObject();
				orgName = orgn.getName();
				row.put("name", orgName);
				row.put("corporateEmail", orgn.getCorporateMail());
				if (orgn.getRegisteredPhoneNumber() != "" && orgn.getRegisteredPhoneNumber() != null) {
					int n = orgn.getRegisteredPhoneNumber().indexOf("-");
					System.out.println(">>>>>><<<<<<>>>>>>>>>phone>>>>>>>>>>>" + n);
					row.put("regPhnCtryCode", orgn.getRegPhNoCtryCode());
					row.put("regPhn",
							orgn.getRegisteredPhoneNumber().substring(n + 1, orgn.getRegisteredPhoneNumber().length()));
				} else {
					row.put("regPhnCtryCode", "");
					row.put("regPhn", "");
				}
				row.put("webUrl", orgn.getWebUrl());
				if (orgn.getGstCountryCode() != null && orgn.getGstCountryCode().equals("IN")) {
					row.put("GSTApplicable", 1);
				} else {
					row.put("GSTApplicable", 0);
				}
				row.put("regAdd", orgn.getRegisteredAddress());
				if (orgn.getFinancialStartDate() != null) {
					finStDate = idosmdtdf.format(orgn.getFinancialStartDate());
				}
				row.put("finStDate", finStDate);
				if (orgn.getFinancialEndDate() != null) {
					finEndDate = idosmdtdf.format(orgn.getFinancialEndDate());
				}
				if (orgn.getCountry() != null) {
					row.put("country", orgn.getCountry());
				} else {
					row.put("country", "");
				}
				if (orgn.getAutoItemRegistrationAllowed() != null) {
					if (orgn.getAutoItemRegistrationAllowed() == 1) {
						row.put("companyProductListings", orgn.getAutoItemRegistrationAllowed());
					} else {
						row.put("companyProductListings", orgn.getAutoItemRegistrationAllowed());
					}
				} else {
					row.put("companyProductListings", "0");
				}
				if (orgn.getIsCompositionScheme() != null) {
					if (orgn.getIsCompositionScheme() == 1) {
						row.put("isCompositionScheme", "1");
					} else {
						row.put("isCompositionScheme", "0");
					}
				} else {
					row.put("isCompositionScheme", "0");
				}
				if (orgn.getLimitDaysForBackdatedTxn() != null) {
					row.put("limitForBackDatedTxn", orgn.getLimitDaysForBackdatedTxn());
				} else {
					row.put("limitForBackDatedTxn", "0");
				}
				row.put("currency", orgn.getCurrency());
				row.put("orgnChart", orgn.getOrganizationChartDoc());
				row.put("prevAuditReport", orgn.getAuditedAccountDoc());
				row.put("acctManual", orgn.getAccountingManualDoc());
				row.put("prevYrTaxRtrn", orgn.getTaxReturnDoc());
				row.put("signatoryList", orgn.getSignatoryListDoc());
				row.put("companyTemplates", orgn.getCompanyTemplateDoc());
				row.put("companyLogo", orgn.getCompanyLogo());
				row.put("finEndDate", finEndDate);
				organ.add(row);
			}
		} catch (Exception ex) {
			System.out.println(">>>>>><<<<<<>>>>>>>>>err>>>>>>>>>>>" + ex.toString());
			log.log(Level.SEVERE, email, ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, email, orgName,
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		} finally {
			// EntityManagerProvider.close();
		}
		log.log(Level.FINE, ">>>> End " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getOrganizationTemplate(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		ArrayNode txncomptemplatean = result.putArray("txncompanytemplate");
		Users user = null;
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			ObjectNode row = Json.newObject();
			if (user.getOrganization().getCompanyTemplateDoc() != null) {
				row.put("templateurl", user.getOrganization().getCompanyTemplateDoc());
			} else {
				row.put("templateurl", "");
			}
			txncomptemplatean.add(row);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getRealTimeAlertsInfo(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		JsonNode json = request.body().asJson();
		String useremail = json.findValue("usermail").asText();
		String monthFirstDate = json.findValue("monthFirstDate").asText();
		String monthLastDate = json.findValue("monthLastDate").asText();
		session.adding("email", useremail);
		Users user = getUserInfo(request);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			result = operationalService.operationalAlertsDates(user, monthFirstDate, monthLastDate, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getOperationals(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode orgTaskManagerData = result.putArray("orgTaskManagerData");
		Users user = null;
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			entityTransaction.begin();
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
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
			List<AlertMailLogs> finalAlertMailLogs = new ArrayList<AlertMailLogs>();
			StringBuilder type1sbquery = new StringBuilder("");
			type1sbquery.append(
					"select obj from AlertMailLogs obj WHERE alertType=1 and obj.branch IS NOT NULL and obj.organization='"
							+ user.getOrganization().getId() + "' and obj.presentStatus=1 GROUP BY obj.branch");
			List<AlertMailLogs> type1alertbranch = genericDAO.executeSimpleQuery(type1sbquery.toString(),
					entityManager);
			for (AlertMailLogs alert1BnchMailLogs : type1alertbranch) {
				StringBuilder sbquery = new StringBuilder("");
				sbquery.append("select obj from AlertMailLogs obj WHERE alertType=1 and obj.branch.id='"
						+ alert1BnchMailLogs.getBranch().getId()
						+ "' and obj.taskAlertGroupingDate!='null' and obj.organization='"
						+ user.getOrganization().getId()
						+ "' and obj.presentStatus=1 GROUP BY obj.taskAlertGroupingDate ORDER BY obj.taskAlertGroupingDate DESC");
				List<AlertMailLogs> alertMailLogs = genericDAO.executeSimpleQueryWithLimit(sbquery.toString(),
						entityManager, 150);
				finalAlertMailLogs.addAll(alertMailLogs);
			}
			StringBuilder type2sbquery = new StringBuilder("");
			type2sbquery.append(
					"select obj from AlertMailLogs obj WHERE alertType=2 and obj.branch IS NOT NULL and obj.organization='"
							+ user.getOrganization().getId() + "' and obj.presentStatus=1 GROUP BY obj.branch");
			List<AlertMailLogs> type2alertbranch = genericDAO.executeSimpleQuery(type2sbquery.toString(),
					entityManager);
			for (AlertMailLogs alert1BnchMailLogs : type2alertbranch) {
				StringBuilder sbquery = new StringBuilder("");
				sbquery.append("select obj from AlertMailLogs obj WHERE alertType=2 and obj.branch.id='"
						+ alert1BnchMailLogs.getBranch().getId()
						+ "' and obj.taskAlertGroupingDate!='null' and obj.organization='"
						+ user.getOrganization().getId()
						+ "' and obj.presentStatus=1 GROUP BY obj.taskAlertGroupingDate ORDER BY obj.taskAlertGroupingDate DESC");
				List<AlertMailLogs> alertMailLogs = genericDAO.executeSimpleQueryWithLimit(sbquery.toString(),
						entityManager, 150);
				finalAlertMailLogs.addAll(alertMailLogs);
			}
			StringBuilder type3sbquery = new StringBuilder("");
			type3sbquery.append(
					"select obj from AlertMailLogs obj WHERE alertType=3 and obj.branch IS NOT NULL and obj.organization='"
							+ user.getOrganization().getId() + "' and obj.presentStatus=1 GROUP BY obj.branch");
			List<AlertMailLogs> type3alertbranch = genericDAO.executeSimpleQuery(type3sbquery.toString(),
					entityManager);
			for (AlertMailLogs alert1BnchMailLogs : type3alertbranch) {
				StringBuilder sbquery = new StringBuilder("");
				sbquery.append("select obj from AlertMailLogs obj WHERE alertType=3 and obj.branch.id='"
						+ alert1BnchMailLogs.getBranch().getId()
						+ "' and obj.taskAlertGroupingDate!='null' and obj.organization='"
						+ user.getOrganization().getId()
						+ "' and obj.presentStatus=1 GROUP BY obj.taskAlertGroupingDate ORDER BY obj.taskAlertGroupingDate DESC");
				List<AlertMailLogs> alertMailLogs = genericDAO.executeSimpleQueryWithLimit(sbquery.toString(),
						entityManager, 150);
				finalAlertMailLogs.addAll(alertMailLogs);
			}
			StringBuilder type4sbquery = new StringBuilder("");
			type4sbquery.append(
					"select obj from AlertMailLogs obj WHERE alertType=4 and obj.branchStatutory IS NOT NULL and obj.organization='"
							+ user.getOrganization().getId()
							+ "' and obj.presentStatus=1 GROUP BY obj.branchStatutory");
			List<AlertMailLogs> type4alertstatutory = genericDAO.executeSimpleQuery(type4sbquery.toString(),
					entityManager);
			for (AlertMailLogs alert4StatMailLogs : type4alertstatutory) {
				StringBuilder sbquery = new StringBuilder("");
				sbquery.append("select obj from AlertMailLogs obj WHERE alertType=4 and obj.branchStatutory.id='"
						+ alert4StatMailLogs.getBranchStatutory().getId()
						+ "' and obj.taskAlertGroupingDate!='null' and obj.organization='"
						+ user.getOrganization().getId()
						+ "' and obj.presentStatus=1 GROUP BY obj.taskAlertGroupingDate ORDER BY obj.taskAlertGroupingDate DESC");
				List<AlertMailLogs> alertMailLogs = genericDAO.executeSimpleQueryWithLimit(sbquery.toString(),
						entityManager, 150);
				finalAlertMailLogs.addAll(alertMailLogs);
			}
			StringBuilder type5sbquery = new StringBuilder("");
			type5sbquery.append(
					"select obj from AlertMailLogs obj WHERE alertType=5 and obj.branchOperationalRemainder IS NOT NULL and obj.organization='"
							+ user.getOrganization().getId()
							+ "' and obj.presentStatus=1 GROUP BY obj.branchOperationalRemainder");
			List<AlertMailLogs> type5alertoper = genericDAO.executeSimpleQuery(type5sbquery.toString(), entityManager);
			for (AlertMailLogs alert5OperMailLogs : type5alertoper) {
				StringBuilder sbquery = new StringBuilder("");
				sbquery.append(
						"select obj from AlertMailLogs obj WHERE alertType=5 and obj.branchOperationalRemainder.id='"
								+ alert5OperMailLogs.getBranchOperationalRemainder().getId()
								+ "' and obj.taskAlertGroupingDate!='null' and obj.organization='"
								+ user.getOrganization().getId()
								+ "' and obj.presentStatus=1 GROUP BY obj.taskAlertGroupingDate ORDER BY obj.taskAlertGroupingDate DESC");
				List<AlertMailLogs> alertMailLogs = genericDAO.executeSimpleQueryWithLimit(sbquery.toString(),
						entityManager, 150);
				finalAlertMailLogs.addAll(alertMailLogs);
			}
			StringBuilder type6sbquery = new StringBuilder("");
			type6sbquery.append(
					"select obj from AlertMailLogs obj WHERE alertType=6 and obj.branchInsurence IS NOT NULL and obj.organization='"
							+ user.getOrganization().getId()
							+ "' and obj.presentStatus=1 GROUP BY obj.branchInsurence");
			List<AlertMailLogs> type6alertins = genericDAO.executeSimpleQuery(type6sbquery.toString(), entityManager);
			for (AlertMailLogs alert6InsMailLogs : type6alertins) {
				StringBuilder sbquery = new StringBuilder("");
				sbquery.append("select obj from AlertMailLogs obj WHERE alertType=6 and obj.branchInsurence.id='"
						+ alert6InsMailLogs.getBranchInsurence().getId()
						+ "' and obj.taskAlertGroupingDate!='null' and obj.organization='"
						+ user.getOrganization().getId()
						+ "' and obj.presentStatus=1 GROUP BY obj.taskAlertGroupingDate ORDER BY obj.taskAlertGroupingDate DESC");
				List<AlertMailLogs> alertMailLogs = genericDAO.executeSimpleQueryWithLimit(sbquery.toString(),
						entityManager, 150);
				finalAlertMailLogs.addAll(alertMailLogs);
			}
			StringBuilder type7sbquery = new StringBuilder("");
			type7sbquery.append(
					"select obj from AlertMailLogs obj WHERE alertType=7 and obj.branchInsurence IS NOT NULL and obj.organization='"
							+ user.getOrganization().getId()
							+ "' and obj.presentStatus=1 GROUP BY obj.branchInsurence");
			List<AlertMailLogs> type7alertins = genericDAO.executeSimpleQuery(type7sbquery.toString(), entityManager);
			for (AlertMailLogs alert7InsMailLogs : type7alertins) {
				StringBuilder sbquery = new StringBuilder("");
				sbquery.append("select obj from AlertMailLogs obj WHERE alertType=7 and obj.branchInsurence.id='"
						+ alert7InsMailLogs.getBranchInsurence().getId()
						+ "' and obj.taskAlertGroupingDate!='null' and obj.organization='"
						+ user.getOrganization().getId()
						+ "' and obj.presentStatus=1 GROUP BY obj.taskAlertGroupingDate ORDER BY obj.taskAlertGroupingDate DESC");
				List<AlertMailLogs> alertMailLogs = genericDAO.executeSimpleQueryWithLimit(sbquery.toString(),
						entityManager, 150);
				finalAlertMailLogs.addAll(alertMailLogs);
			}
			for (AlertMailLogs altMailLogs : finalAlertMailLogs) {
				ObjectNode row = Json.newObject();
				row.put("remainderActionid", altMailLogs.getId());
				String remainderActionTask = "";
				if (altMailLogs.getMailSubject() != null) {
					remainderActionTask = altMailLogs.getMailSubject();
					row.put("remainderActionTask", remainderActionTask);
				}
				if (altMailLogs.getAlertForAction() != null) {
					row.put("alertForAction", altMailLogs.getAlertForAction());
				}
				if (altMailLogs.getAlertForInformation() != null) {
					row.put("alertForInformation", altMailLogs.getAlertForInformation());
				}
				if (altMailLogs.getTaskAlertGroupingDate() != null) {
					row.put("remainderDueDated", idosdf.format(altMailLogs.getTaskAlertGroupingDate()));
				}
				if (altMailLogs.getConfirmationStatus() != null) {
					row.put("confirmationStatus", altMailLogs.getConfirmationStatus());
				} else {
					row.put("confirmationStatus", "PENDING");
				}
				orgTaskManagerData.add(row);
			}
			entityTransaction.commit();
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getFinancials(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		ArrayNode dashboardan = results.putArray("dashBoardData");
		ArrayNode sessionan = results.putArray("sessionuserTxnData");
		ArrayNode lastForteenDaysCustomersan = results.putArray("lastForteenDaysCustomerData");
		ArrayNode lastForteenDaysVendorsan = results.putArray("lastForteenDaysVendorsData");
		ArrayNode lastForteenDaysUsersan = results.putArray("lastForteenDaysUsersData");
		ArrayNode lastForteenDaysPendingApprovalan = results.putArray("lastForteenDaysPendingApprovalData");
		ArrayNode lastForteenDaysTxnExceedingBudgetAHWan = results.putArray("lastForteenDaysTxnExceedingBudgetAWHData");
		ArrayNode lastForteenDaysTxnExceedingBudgetan = results.putArray("lastForteenDaysTxnExceedingBudgetData");
		ArrayNode lastForteenDaysTxnKlNotFollwedAHWan = results.putArray("lastForteenDaysTxnKlNotFollwedAWHData");
		ArrayNode lastForteenDaysTxnKlNotFollwedan = results.putArray("lastForteenDaysTxnKlNotFollwedData");
		ArrayNode thisWeekBranchWiseCashExpensean = results.putArray("thisWeekBranchWiseCashExpenseData");
		ArrayNode previousWeekBranchWiseCashExpensean = results.putArray("previousWeekBranchWiseCashExpenseData");
		ArrayNode thisWeekPrevWeekVarianceBranchWiseCashExpensean = results
				.putArray("thisWeekPrevWeekVarianceBranchWiseCashExpenseData");
		ArrayNode thisWeekBranchWiseCreditExpensean = results.putArray("thisWeekBranchWiseCreditExpenseData");
		ArrayNode previousWeekBranchWiseCreditExpensean = results.putArray("previousWeekBranchWiseCreditExpenseData");
		ArrayNode thisWeekPrevWeekVarianceBranchWiseCreditExpensean = results
				.putArray("thisWeekPrevWeekVarianceBranchWiseCreditExpenseData");
		ArrayNode thisWeekBranchWiseCashIncomean = results.putArray("thisWeekBranchWiseCashIncome");
		ArrayNode previousWeekBranchWiseCashIncomean = results.putArray("previousWeekBranchWiseCashIncomeData");
		ArrayNode thisWeekPrevWeekVarianceBranchWiseCashIncomean = results
				.putArray("thisWeekPrevWeekVarianceBranchWiseCashIncomeData");
		ArrayNode thisWeekBranchWiseCreditIncomean = results.putArray("thisWeekBranchWiseCreditIncomeData");
		ArrayNode previousWeekBranchWiseCreditIncomean = results.putArray("previousWeekBranchWiseCreditIncomeData");
		ArrayNode thisWeekPrevWeekVarianceBranchWiseCreditIncomean = results
				.putArray("thisWeekPrevWeekVarianceBranchWiseCreditIncomeData");
		ArrayNode thisWeekBranchWiseBudgetAvailablean = results.putArray("thisWeekBranchWiseBudgetAvailableData");
		ArrayNode previousWeekBranchWiseBudgetAvailablean = results
				.putArray("previousWeekBranchWiseBudgetAvailableData");
		ArrayNode thisWeekPrevWeekVarianceBranchWiseBudgetAvailablean = results
				.putArray("thisWeekPrevWeekVarianceBranchWiseBudgetAvailableData");
		ArrayNode thisWeekBranchWiseTotalReceivablesan = results.putArray("thisWeekBranchWiseTotalReceivablesData");
		ArrayNode previousWeekBranchWiseTotalReceivablesan = results
				.putArray("previousWeekBranchWiseTotalReceivablesData");
		ArrayNode thisWeekPrevWeekVarianceBranchWiseTotalReceivablesan = results
				.putArray("thisWeekPrevWeekVarianceBranchWiseTotalReceivablesData");
		ArrayNode thisWeekBranchWiseTotalPayablesan = results.putArray("thisWeekBranchWiseTotalPayablesData");
		ArrayNode previousWeekBranchWiseTotalPayablesan = results.putArray("previousWeekBranchWiseTotalPayablesData");
		ArrayNode thisWeekPrevWeekVarianceBranchWiseTotalPayablesan = results
				.putArray("thisWeekPrevWeekVarianceBranchWiseTotalPayablesData");
		String sessemail = session.getOptional("email").orElse("");
		Users user = null;
		try {
			transaction.begin();
			if (sessemail != null && !sessemail.equals("")) {
				ObjectNode sessevent = Json.newObject();
				sessevent.put("sessemail", sessemail);
				sessionan.add(sessevent);
				ObjectNode dashboardrow = Json.newObject();
				JsonNode json = request.body().asJson();
				String usermail = json.findValue("usermail").asText();
				session.adding("email", usermail);
				user = getUserInfo(request);
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				String currentWeekStartDate = mysqldf.format(cal.getTime());
				cal.add(Calendar.DAY_OF_WEEK, 6);
				String currentWeekEndDate = mysqldf.format(cal.getTime());
				cal.add(Calendar.DAY_OF_WEEK, 1);
				// get live data of current week
				// sum total cash expense this week
				StringBuilder sbquery = new StringBuilder("");
				sbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
				List<Transaction> cashexpensetxn = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
				Double creditExpenseThisWeek = null;
				Double cashExpenseThisWeek = null;
				if (cashexpensetxn.size() > 0) {
					Object val = cashexpensetxn.get(0);
					if (val != null) {
						dashboardrow.put("cashExpense", decimalFormat.format(val));
						cashExpenseThisWeek = Double.parseDouble(String.valueOf(val));
					} else {
						dashboardrow.put("cashExpense", "");
					}
				} else {
					dashboardrow.put("cashExpense", "");
				}
				// sum total cash sell this week
				StringBuilder cssbquery = new StringBuilder("");
				cssbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=1 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
				List<Transaction> cashselltxn = genericDAO.executeSimpleQuery(cssbquery.toString(), entityManager);
				if (cashselltxn.size() > 0) {
					Object val = cashselltxn.get(0);
					if (val != null) {
						dashboardrow.put("cashIncome", decimalFormat.format(val));
					} else {
						dashboardrow.put("cashIncome", "");
					}
				} else {
					dashboardrow.put("cashIncome", "");
				}
				// sum total credit expense this week
				StringBuilder cdtsbquery = new StringBuilder("");
				cdtsbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
				List<Transaction> creditexpensetxn = genericDAO.executeSimpleQuery(cdtsbquery.toString(),
						entityManager);
				if (creditexpensetxn.size() > 0) {
					Object val = creditexpensetxn.get(0);
					if (val != null) {
						dashboardrow.put("creditExpense", decimalFormat.format(val));
						creditExpenseThisWeek = Double.parseDouble(String.valueOf(val));
					} else {
						dashboardrow.put("creditExpense", "");
					}
				} else {
					dashboardrow.put("creditExpense", "");
				}
				// sum total credit income this week
				StringBuilder cdtincsbquery = new StringBuilder("");
				cdtincsbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
				List<Transaction> creditincometxn = genericDAO.executeSimpleQuery(cdtincsbquery.toString(),
						entityManager);
				if (creditincometxn.size() > 0) {
					Object val = creditincometxn.get(0);
					if (val != null) {
						dashboardrow.put("creditIncome", decimalFormat.format(val));
					} else {
						dashboardrow.put("creditIncome", "");
					}
				} else {
					dashboardrow.put("creditIncome", "");
				}
				// sum total of expense budget allocated
				StringBuilder expbudgetallocsbquery = new StringBuilder("");
				expbudgetallocsbquery
						.append("select SUM(obj.budgetTotal) from BranchSpecifics obj WHERE obj.organization='"
								+ user.getOrganization().getId() + "' and obj.presentStatus=1");
				List<BranchSpecifics> expbudgetalloc = genericDAO.executeSimpleQuery(expbudgetallocsbquery.toString(),
						entityManager);
				Object expBudAllocAmount = expbudgetalloc.get(0);
				StringBuilder expbudgetdeducsbquery = new StringBuilder("");
				expbudgetdeducsbquery
						.append("select SUM(obj.budgetDeductedTotal) from BranchSpecifics obj WHERE obj.organization='"
								+ user.getOrganization().getId() + "' and obj.presentStatus=1");
				List<BranchSpecifics> expbudgetdeducted = genericDAO
						.executeSimpleQuery(expbudgetdeducsbquery.toString(), entityManager);
				Object expBudDeductedAmount = expbudgetdeducted.get(0);
				Double expBudgetAvail = null;
				if (expBudDeductedAmount == null) {
					if (expBudAllocAmount != null) {
						expBudgetAvail = Double.parseDouble(decimalFormat.format(expBudAllocAmount));
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
					dashboardrow.put("expBudgetAvail", decimalFormat.format(expBudgetAvail));
				} else {
					dashboardrow.put("expBudgetAvail", "");
				}
				// sum of total receivables this week
				StringBuilder creditincomepaymentmadequery = new StringBuilder("");
				creditincomepaymentmadequery.append(
						"select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
				List<Transaction> creditIncomeCustomerNetPaymentMade = genericDAO
						.executeSimpleQuery(creditincomepaymentmadequery.toString(), entityManager);
				Object creditIncomeCustPaymentMade = creditIncomeCustomerNetPaymentMade.get(0);
				Double netRecievableThisWeek = null;
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
					dashboardrow.put("netRecievableThisWeek", decimalFormat.format(netRecievableThisWeek));
				} else {
					dashboardrow.put("netRecievableThisWeek", "");
				}
				// sum of total payables this week
				StringBuilder creditexpensepaymentmadequery = new StringBuilder("");
				creditexpensepaymentmadequery.append(
						"select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
				List<Transaction> creditExpenseCustomerNetPaymentMade = genericDAO
						.executeSimpleQuery(creditexpensepaymentmadequery.toString(), entityManager);
				Object creditExpenseCustPaymentMade = creditExpenseCustomerNetPaymentMade.get(0);
				Double netPayableThisWeek = null;
				if (creditExpenseCustPaymentMade != null && !creditExpenseCustPaymentMade.equals("")) {
					if (creditexpensetxn.size() > 0) {
						Object val = creditexpensetxn.get(0);
						if (val != null && !val.equals("")) {
							netPayableThisWeek = Double.parseDouble(decimalFormat.format(val))
									- Double.parseDouble(decimalFormat.format(creditExpenseCustPaymentMade));
						}
					}
				}
				if (creditExpenseCustPaymentMade == null) {
					if (creditexpensetxn.size() > 0) {
						Object val = creditexpensetxn.get(0);
						if (val != null && !val.equals("")) {
							netPayableThisWeek = Double.parseDouble(decimalFormat.format(val));
						}
					}
				}
				if (netPayableThisWeek != null) {
					dashboardrow.put("netPayableThisWeek", decimalFormat.format(netPayableThisWeek));
				} else {
					dashboardrow.put("netPayableThisWeek", "");
				}
				// this week dash board data ends
				// start previous week dash board datas
				Calendar newcal = Calendar.getInstance();
				newcal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				newcal.add(Calendar.DAY_OF_WEEK, -7);
				String previousWeekStartDate = mysqldf.format(newcal.getTime());
				newcal.add(Calendar.DAY_OF_WEEK, 6);
				String previousWeekEndDate = mysqldf.format(newcal.getTime());
				Double previousWeekCashExpense = null;
				Double previousWeekCreditExpense = null;
				// sum total cash expense previous week
				StringBuilder pwsbquery = new StringBuilder("");
				pwsbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
				List<Transaction> pwcashexpensetxn = genericDAO.executeSimpleQuery(pwsbquery.toString(), entityManager);
				if (pwcashexpensetxn.size() > 0) {
					Object val = pwcashexpensetxn.get(0);
					if (val != null) {
						dashboardrow.put("previousWeekcashExpense", decimalFormat.format(val));
						previousWeekCashExpense = Double.parseDouble(String.valueOf(val));
					} else {
						dashboardrow.put("previousWeekcashExpense", "");
					}
				} else {
					dashboardrow.put("previousWeekcashExpense", "");
				}
				// sum total cash sell previous week
				StringBuilder pwcssbquery = new StringBuilder("");
				pwcssbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=1 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
				List<Transaction> pwcashselltxn = genericDAO.executeSimpleQuery(pwcssbquery.toString(), entityManager);
				if (pwcashselltxn.size() > 0) {
					Object val = pwcashselltxn.get(0);
					if (val != null) {
						dashboardrow.put("previousWeekcashIncome", decimalFormat.format(val));
					} else {
						dashboardrow.put("previousWeekcashIncome", "");
					}
				} else {
					dashboardrow.put("previousWeekcashIncome", "");
				}
				// sum total credit expense previous week
				StringBuilder pwcdtsbquery = new StringBuilder("");
				pwcdtsbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
				List<Transaction> pwcreditexpensetxn = genericDAO.executeSimpleQuery(pwcdtsbquery.toString(),
						entityManager);
				if (pwcreditexpensetxn.size() > 0) {
					Object val = pwcreditexpensetxn.get(0);
					if (val != null) {
						dashboardrow.put("previousWeekcreditExpense", decimalFormat.format(val));
						previousWeekCreditExpense = Double.parseDouble(String.valueOf(val));
					} else {
						dashboardrow.put("previousWeekcreditExpense", "");
					}
				} else {
					dashboardrow.put("previousWeekcreditExpense", "");
				}
				// sum total credit income previous week
				StringBuilder pwcdtincsbquery = new StringBuilder("");
				pwcdtincsbquery.append(
						"select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
				List<Transaction> pwcreditincometxn = genericDAO.executeSimpleQuery(pwcdtincsbquery.toString(),
						entityManager);
				if (pwcreditincometxn.size() > 0) {
					Object val = pwcreditincometxn.get(0);
					if (val != null) {
						dashboardrow.put("previousWekcreditIncome", decimalFormat.format(val));
					} else {
						dashboardrow.put("previousWekcreditIncome", "");
					}
				} else {
					dashboardrow.put("previousWekcreditIncome", "");
				}
				// sum total of expense budget allocated
				StringBuilder pwexpbudgetallocsbquery = new StringBuilder("");
				pwexpbudgetallocsbquery
						.append("select SUM(obj.budgetTotal) from BranchSpecifics obj WHERE obj.organization='"
								+ user.getOrganization().getId() + "' and obj.presentStatus=1");
				List<BranchSpecifics> pwexpbudgetalloc = genericDAO.executeSimpleQuery(expbudgetallocsbquery.toString(),
						entityManager);
				Object pwexpBudAllocAmount = pwexpbudgetalloc.get(0);
				StringBuilder pwexpbudgetdeducsbquery = new StringBuilder("");
				pwexpbudgetdeducsbquery
						.append("select SUM(obj.budgetDeductedTotal) from BranchSpecifics obj WHERE obj.organization='"
								+ user.getOrganization().getId() + "' and obj.presentStatus=1");
				List<BranchSpecifics> pwexpbudgetdeducted = genericDAO
						.executeSimpleQuery(expbudgetdeducsbquery.toString(), entityManager);
				Object pwexpBudDeductedAmount = pwexpbudgetdeducted.get(0);
				Double pwexpBudgetAvail = null;
				if (pwexpBudDeductedAmount == null) {
					if (pwexpBudAllocAmount != null) {
						pwexpBudgetAvail = Double.parseDouble(decimalFormat.format(pwexpBudAllocAmount));
					}
				}
				if (pwexpBudDeductedAmount != null && !pwexpBudDeductedAmount.equals("")) {
					pwexpBudgetAvail = Double.parseDouble(decimalFormat.format(pwexpBudAllocAmount))
							- Double.parseDouble(decimalFormat.format(pwexpBudDeductedAmount));
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
				Calendar pwnewcal = Calendar.getInstance();
				if (pwexpBudgetAvail != null) {
					dashboardrow.put("previousWeekexpBudgetAvail", decimalFormat.format(pwexpBudgetAvail));
				} else {
					dashboardrow.put("previousWeekexpBudgetAvail", "");
				}
				// sum of total receivables previous week
				StringBuilder pwcreditincomepaymentmadequery = new StringBuilder("");
				pwcreditincomepaymentmadequery.append(
						"select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
				List<Transaction> pwcreditIncomeCustomerNetPaymentMade = genericDAO
						.executeSimpleQuery(creditincomepaymentmadequery.toString(), entityManager);
				Object pwcreditIncomeCustPaymentMade = pwcreditIncomeCustomerNetPaymentMade.get(0);
				Double netRecievablePreviousWeek = null;
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
					dashboardrow.put("netRecievablePreviousWeek", decimalFormat.format(netRecievablePreviousWeek));
				} else {
					dashboardrow.put("netRecievablePreviousWeek", "");
				}
				// sum of total payables previous week
				StringBuilder pwcreditexpensepaymentmadequery = new StringBuilder("");
				pwcreditexpensepaymentmadequery.append(
						"select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
								+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
				List<Transaction> pwcreditExpenseCustomerNetPaymentMade = genericDAO
						.executeSimpleQuery(creditexpensepaymentmadequery.toString(), entityManager);
				Object pwcreditExpenseCustPaymentMade = pwcreditExpenseCustomerNetPaymentMade.get(0);
				Double netPayablePreviousWeek = null;
				if (pwcreditExpenseCustPaymentMade != null && !pwcreditExpenseCustPaymentMade.equals("")) {
					if (pwcreditexpensetxn.size() > 0) {
						Object val = pwcreditexpensetxn.get(0);
						if (val != null && !val.equals("")) {
							netPayablePreviousWeek = Double.parseDouble(decimalFormat.format(val))
									- Double.parseDouble(decimalFormat.format(pwcreditExpenseCustPaymentMade));
						}
					}
				}
				if (pwcreditExpenseCustPaymentMade == null) {
					if (pwcreditexpensetxn.size() > 0) {
						Object val = pwcreditexpensetxn.get(0);
						if (val != null && !val.equals("")) {
							netPayablePreviousWeek = Double.parseDouble(decimalFormat.format(val));
						}
					}
				}
				if (netPayablePreviousWeek != null) {
					dashboardrow.put("netPayablePreviousWeek", decimalFormat.format(netPayablePreviousWeek));
				} else {
					dashboardrow.put("netPayablePreviousWeek", "");
				}
				// this week dash board data ends
				newcal.add(Calendar.DAY_OF_WEEK, 1);
				Calendar newestcal = Calendar.getInstance();
				String currentDate = mysqldf.format(newestcal.getTime());
				String currDate = currentDate;
				currentDate += " 23:59:59";
				newestcal.add(Calendar.DAY_OF_WEEK, -14);
				String forteenDaysBack = mysqldf.format(newestcal.getTime());
				String forteenBackDate = forteenDaysBack;
				forteenDaysBack += " 00:00:00";
				StringBuilder fortenDaysBackCustomer = new StringBuilder();
				fortenDaysBackCustomer.append("select obj from Vendor obj WHERE obj.type=2 and obj.organization='"
						+ user.getOrganization().getId() + "' and obj.presentStatus=1 and (obj.createdAt  between '"
						+ forteenDaysBack + "' and '" + currentDate + "' or obj.modifiedAt between '" + forteenDaysBack
						+ "' and '" + currentDate + "')");
				List<Vendor> lastForteenDaysAddedModifiedCustomers = genericDAO
						.executeSimpleQuery(fortenDaysBackCustomer.toString(), entityManager);
				for (Vendor customers : lastForteenDaysAddedModifiedCustomers) {
					ObjectNode lastForteenDaysCustomersrow = Json.newObject();
					lastForteenDaysCustomersrow.put("lastForteenDaysCustomers", customers.getName());
					List<BranchVendors> customerBranches = customers.getVendorBranches();
					String branches = "";
					String customerItems = "";
					for (BranchVendors custBnchs : customerBranches) {
						branches += custBnchs.getBranch().getName() + ",";
					}
					lastForteenDaysCustomersrow.put("lastForteenDaysCustomersBranches", branches);
					List<VendorSpecific> customerItemsList = customers.getVendorsSpecifics();
					for (VendorSpecific custItems : customerItemsList) {
						customerItems += custItems.getSpecificsVendors().getName() + ",";
					}
					lastForteenDaysCustomersrow.put("lastForteenDaysCustomersItems", customerItems);
					lastForteenDaysCustomersan.add(lastForteenDaysCustomersrow);
				}
				StringBuilder fortenDaysBackVendor = new StringBuilder();
				fortenDaysBackVendor.append("select obj from Vendor obj WHERE obj.type=1 and obj.organization='"
						+ user.getOrganization().getId() + "' and obj.presentStatus=1 and (obj.createdAt  between '"
						+ forteenDaysBack + "' and '" + currentDate + "' or obj.modifiedAt between '" + forteenDaysBack
						+ "' and '" + currentDate + "')");
				List<Vendor> lastForteenDaysAddedModifiedVendors = genericDAO
						.executeSimpleQuery(fortenDaysBackVendor.toString(), entityManager);
				for (Vendor vendors : lastForteenDaysAddedModifiedVendors) {
					ObjectNode lastForteenDaysVendorsrow = Json.newObject();
					lastForteenDaysVendorsrow.put("lastForteenDaysVendors", vendors.getName());
					List<BranchVendors> vendorBranches = vendors.getVendorBranches();
					String branches = "";
					String vendorItems = "";
					for (BranchVendors vendBnchs : vendorBranches) {
						branches += vendBnchs.getBranch().getName() + ",";
					}
					lastForteenDaysVendorsrow.put("lastForteenDaysVendorBranches", branches);
					List<VendorSpecific> vendorItemsList = vendors.getVendorsSpecifics();
					for (VendorSpecific vendItems : vendorItemsList) {
						vendorItems += vendItems.getSpecificsVendors().getName() + ",";
					}
					lastForteenDaysVendorsrow.put("lastForteenDaysVendorItems", vendorItems);
					lastForteenDaysVendorsan.add(lastForteenDaysVendorsrow);
				}
				StringBuilder fortenDaysBackUsers = new StringBuilder();
				fortenDaysBackUsers.append("select obj from Users obj WHERE obj.presentStatus=1 and obj.organization='"
						+ user.getOrganization().getId() + "' and (obj.createdAt  between '" + forteenDaysBack
						+ "' and '" + currentDate + "' or obj.modifiedAt between '" + forteenDaysBack + "' and '"
						+ currentDate + "')");
				List<Users> lastForteenDaysAddedModifiedUsers = genericDAO
						.executeSimpleQuery(fortenDaysBackUsers.toString(), entityManager);
				for (Users users : lastForteenDaysAddedModifiedUsers) {
					String userCreationRights = "";
					String userApprovalRights = "";
					String userRoles = "";
					String userCreationRightsSpecifics = "";
					String userApprovalRightsSpecifics = "";
					ObjectNode lastForteenDaysUsersrow = Json.newObject();
					lastForteenDaysUsersrow.put("lastForteenDaysUsers",
							users.getFullName() + "(" + users.getEmail() + ")");
					List<UserRightInBranch> userRightsInBranches = users.getUserRightsInBranches();
					for (UserRightInBranch usrRghtBnchs : userRightsInBranches) {
						if (usrRghtBnchs.getUserRights().getId() == 1L) {
							userCreationRights += usrRghtBnchs.getBranch().getName() + ",";
						}
						if (usrRghtBnchs.getUserRights().getId() == 2L) {
							userApprovalRights += usrRghtBnchs.getBranch().getName() + ",";
						}
					}
					lastForteenDaysUsersrow.put("lastForteenDaysUsersCreationRightForBranches", userCreationRights);
					lastForteenDaysUsersrow.put("lastForteenDaysUsersApprovalRightForBranches", userApprovalRights);
					List<UsersRoles> userRolesList = users.getUserRoles();
					for (UsersRoles usrRoles : userRolesList) {
						userRoles += usrRoles.getRole().getName() + ",";
					}
					lastForteenDaysUsersrow.put("lastForteenDaysUsersRoles", userRoles);
					StringBuilder userrightsspecifics = new StringBuilder();
					userrightsspecifics.append("select obj from UserRightSpecifics obj WHERE obj.user='" + users.getId()
							+ "' and obj.presentStatus=1");
					List<UserRightSpecifics> userRightSpecificsList = genericDAO
							.executeSimpleQuery(userrightsspecifics.toString(), entityManager);
					for (UserRightSpecifics usrRightSpecf : userRightSpecificsList) {
						if (usrRightSpecf.getUserRights().getId() == 1L) {
							userCreationRightsSpecifics += usrRightSpecf.getSpecifics().getName() + ",";
						}
						if (usrRightSpecf.getUserRights().getId() == 2L) {
							userApprovalRightsSpecifics += usrRightSpecf.getSpecifics().getName() + ",";
						}
					}
					lastForteenDaysUsersrow.put("lastForteenDaysUsersCreationRightForItems",
							userCreationRightsSpecifics);
					lastForteenDaysUsersrow.put("lastForteenDaysUsersApprovalRightForItems",
							userApprovalRightsSpecifics);
					lastForteenDaysUsersan.add(lastForteenDaysUsersrow);
				}
				StringBuilder forteenBackDatePendingApproval = new StringBuilder("");
				forteenBackDatePendingApproval
						.append("select obj from Transaction obj WHERE obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' and obj.presentStatus=1 and (obj.transactionStatus='Require Approval' or obj.transactionStatus='Require Additional Approval') and obj.transactionDate between '"
								+ forteenBackDate + "' and '" + currDate + "'");
				List<Transaction> txnPendindApproval = genericDAO
						.executeSimpleQuery(forteenBackDatePendingApproval.toString(), entityManager);
				if (txnPendindApproval.size() > 0) {
					dashboardrow.put("forteenBackDatePendingApproval", txnPendindApproval.size());
				} else {
					dashboardrow.put("forteenBackDatePendingApproval", "");
				}
				for (Transaction pendTxn : txnPendindApproval) {
					ObjectNode forteenBackDatePendingApprovalrow = Json.newObject();
					forteenBackDatePendingApprovalrow.put("forteenBackDatePendingApprovalTxnRef",
							pendTxn.getTransactionRefNumber());
					lastForteenDaysPendingApprovalan.add(forteenBackDatePendingApprovalrow);
				}
				// Connection con=MySqlConnection.getConnection();
				// Statement stmt = null;
				StringBuilder maxexpensebranchquery = new StringBuilder("");
				maxexpensebranchquery.append(
						"SELECT TRANSACTION_BRANCH, SUM(NET_AMOUNT) AS NET_AMOUNT FROM TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION='"
								+ user.getOrganization().getId()
								+ "' AND TRANSACTION_STATUS='Accounted' AND (TRANSACTION_PURPOSE=3 OR TRANSACTION_PURPOSE=4 OR TRANSACTION_PURPOSE=11) AND TRANSACTION_ACTIONDATE BETWEEN  '"
								+ forteenBackDate + "' and '" + currDate
								+ "' GROUP BY TRANSACTION_BRANCH ORDER BY NET_AMOUNT DESC LIMIT 1");
				// stmt = (Statement) con.createStatement();
				// ResultSet rs = stmt.executeQuery(maxexpensebranchquery.toString());
				Query query = entityManager.createQuery(maxexpensebranchquery.toString());
				List<Object[]> resultList = query.getResultList();
				String maxExpenseBranch = "";
				for (Object[] resultObj : resultList) {
					// Integer branchId=rs.getInt("TRANSACTION_BRANCH");
					// Double maxNetAmount=rs.getDouble("NET_AMOUNT");
					Integer branchId = (Integer) resultObj[0];
					Double maxNetAmount = (Double) resultObj[1];
					Branch maxTxnBranchEntity = null;
					if (branchId != null && branchId > 0) {
						maxTxnBranchEntity = Branch.findById(branchId.longValue());
					}
					if (maxNetAmount != null && maxNetAmount > 0.0) {
						maxExpenseBranch = maxTxnBranchEntity.getName() + ":" + String.valueOf(maxNetAmount);
					}
				}
				dashboardrow.put("maxExpenseBranch", maxExpenseBranch);
				Statement stmt1 = null;
				StringBuilder maxincomebranchquery = new StringBuilder("");
				maxincomebranchquery.append(
						"SELECT TRANSACTION_BRANCH, SUM(NET_AMOUNT) AS NET_AMOUNT FROM TRANSACTION WHERE TRANSACTION_BRANCH_ORGANIZATION='"
								+ user.getOrganization().getId()
								+ "' AND TRANSACTION_STATUS='Accounted' AND (TRANSACTION_PURPOSE=1 OR TRANSACTION_PURPOSE=2) AND TRANSACTION_ACTIONDATE BETWEEN  '"
								+ forteenBackDate + "' and '" + currDate
								+ "' GROUP BY TRANSACTION_BRANCH ORDER BY NET_AMOUNT DESC LIMIT 1");
				// stmt1 = (Statement) con.createStatement();
				// ResultSet rs1 = stmt1.executeQuery(maxincomebranchquery.toString());
				String maxIncomeBranch = "";
				query = entityManager.createQuery(maxincomebranchquery.toString());
				List<Object[]> resultBList = query.getResultList();
				for (Object[] resultObj : resultBList) {
					// Integer branchId=rs.getInt("TRANSACTION_BRANCH");
					// Double maxNetAmount=rs.getDouble("NET_AMOUNT");
					Integer branchId = (Integer) resultObj[0];
					Double maxNetAmount = (Double) resultObj[1];
					Branch maxTxnBranchEntity = null;
					if (branchId != null && branchId > 0) {
						maxTxnBranchEntity = Branch.findById(branchId.longValue());
					}
					if (maxNetAmount != null && maxNetAmount > 0.0) {
						maxIncomeBranch = maxTxnBranchEntity.getName() + ":" + String.valueOf(maxNetAmount);
					}
				}
				dashboardrow.put("maxIncomeBranch", maxIncomeBranch);
				// con.close();
				StringBuilder fortenDaysBackTxnExceedingBudgetAccountHeadWise = new StringBuilder();
				fortenDaysBackTxnExceedingBudgetAccountHeadWise.append(
						"select obj from Transaction obj WHERE obj.transactionExceedingBudget=1 and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId() + "' and obj.transactionDate between '"
								+ forteenBackDate + "' and '" + currDate
								+ "' and obj.presentStatus=1 GROUP BY obj.transactionSpecifics");
				List<Transaction> lastForteenDaysTxnExceedingBudgetAccountHeadWise = genericDAO
						.executeSimpleQuery(fortenDaysBackTxnExceedingBudgetAccountHeadWise.toString(), entityManager);
				for (Transaction txnExceedingBudgetAHW : lastForteenDaysTxnExceedingBudgetAccountHeadWise) {
					String txnExcBudRefNumberCommaSeparatedString = "";
					ObjectNode lastForteenDaysTxnExceedingBudgetAHWrow = Json.newObject();
					String immediateParent = "";
					if (txnExceedingBudgetAHW.getTransactionSpecifics().getParentSpecifics() != null) {
						immediateParent = txnExceedingBudgetAHW.getTransactionSpecifics().getParentSpecifics()
								.getName();
					} else {
						immediateParent = txnExceedingBudgetAHW.getTransactionSpecifics().getParticularsId().getName();
					}
					lastForteenDaysTxnExceedingBudgetAHWrow.put("txnExceedingBudgetBranchRefNoAHSpecificsId",
							txnExceedingBudgetAHW.getTransactionSpecifics().getId());
					lastForteenDaysTxnExceedingBudgetAHWrow.put("txnExceedingBudgetBranchRefNoAH", "(" + immediateParent
							+ " )-->" + txnExceedingBudgetAHW.getTransactionSpecifics().getName() + "");
					StringBuilder fortenDaysBackTxnExceedingBudget = new StringBuilder();
					fortenDaysBackTxnExceedingBudget.append(
							"select obj from Transaction obj WHERE obj.transactionExceedingBudget=1 and obj.transactionBranchOrganization='"
									+ user.getOrganization().getId() + "' and obj.transactionSpecifics='"
									+ txnExceedingBudgetAHW.getTransactionSpecifics().getId()
									+ "' and obj.presentStatus=1 and obj.transactionDate between '" + forteenBackDate
									+ "' and '" + currDate + "'");
					List<Transaction> lastForteenDaysTxnExceedingBudget = genericDAO
							.executeSimpleQuery(fortenDaysBackTxnExceedingBudget.toString(), entityManager);
					for (Transaction txnExceedingBudget : lastForteenDaysTxnExceedingBudget) {
						ObjectNode lastForteenDaysTxnExceedingBudgetrow = Json.newObject();
						lastForteenDaysTxnExceedingBudgetrow.put("txnExceedingBudgetBranchRefNo",
								txnExceedingBudget.getTransactionRefNumber());
						lastForteenDaysTxnExceedingBudgetan.add(lastForteenDaysTxnExceedingBudgetrow);
						txnExcBudRefNumberCommaSeparatedString += txnExceedingBudget.getTransactionRefNumber() + ",";
					}
					lastForteenDaysTxnExceedingBudgetAHWrow.put("txnExceedingBudgetBranchRefNo",
							txnExcBudRefNumberCommaSeparatedString.substring(0,
									txnExcBudRefNumberCommaSeparatedString.length() - 1));
					lastForteenDaysTxnExceedingBudgetAHWan.add(lastForteenDaysTxnExceedingBudgetAHWrow);
				}
				StringBuilder fortenDaysBackTxnKlNotFollwedAccountHeadWise = new StringBuilder();
				fortenDaysBackTxnKlNotFollwedAccountHeadWise.append(
						"select obj from Transaction obj WHERE obj.klFollowStatus=0 and obj.transactionBranchOrganization='"
								+ user.getOrganization().getId()
								+ "' and obj.presentStatus=1 and obj.transactionDate between '" + forteenBackDate
								+ "' and '" + currDate + "' GROUP BY obj.transactionSpecifics");
				List<Transaction> lastForteenDaysTxnKlNotFollwedAccountHeadWise = genericDAO
						.executeSimpleQuery(fortenDaysBackTxnKlNotFollwedAccountHeadWise.toString(), entityManager);
				for (Transaction txnKlNotFollowedAHW : lastForteenDaysTxnKlNotFollwedAccountHeadWise) {
					String txnKlNotFollowedRefNumberCommaSeparatedString = "";
					ObjectNode lastForteenDaysKlNotFollowedAHWrow = Json.newObject();
					String immediateParent = "";
					if (txnKlNotFollowedAHW.getTransactionSpecifics().getParentSpecifics() != null) {
						immediateParent = txnKlNotFollowedAHW.getTransactionSpecifics().getParentSpecifics().getName();
					} else {
						immediateParent = txnKlNotFollowedAHW.getTransactionSpecifics().getParticularsId().getName();
					}
					lastForteenDaysKlNotFollowedAHWrow.put("txnKlNotFollowedBranchRefNoAHSpecificsId",
							txnKlNotFollowedAHW.getTransactionSpecifics().getId());
					lastForteenDaysKlNotFollowedAHWrow.put("txnKlNotFollowedBranchRefNoAH", "(" + immediateParent
							+ " )-->" + txnKlNotFollowedAHW.getTransactionSpecifics().getName() + "");
					StringBuilder fortenDaysBackTxnKlNotFollowed = new StringBuilder();
					fortenDaysBackTxnKlNotFollowed.append(
							"select obj from Transaction obj WHERE obj.klFollowStatus=0 and obj.transactionBranchOrganization='"
									+ user.getOrganization().getId() + "' and obj.transactionSpecifics='"
									+ txnKlNotFollowedAHW.getTransactionSpecifics().getId()
									+ "' and obj.presentStatus=1 and obj.transactionDate between '" + forteenBackDate
									+ "' and '" + currDate + "'");
					List<Transaction> lastForteenDaysTxnKlNotFollowed = genericDAO
							.executeSimpleQuery(fortenDaysBackTxnKlNotFollowed.toString(), entityManager);
					for (Transaction txnKLNotFollowed : lastForteenDaysTxnKlNotFollowed) {
						ObjectNode lastForteenDaysTxnKlNotFollowedrow = Json.newObject();
						lastForteenDaysTxnKlNotFollowedrow.put("txnKlNotFollowedBranchRefNo",
								txnKLNotFollowed.getTransactionRefNumber());
						lastForteenDaysTxnKlNotFollwedan.add(lastForteenDaysTxnKlNotFollowedrow);
						txnKlNotFollowedRefNumberCommaSeparatedString += txnKLNotFollowed.getTransactionRefNumber()
								+ ",";
					}
					lastForteenDaysKlNotFollowedAHWrow.put("txnKlNotFollowedBranchRefNo",
							txnKlNotFollowedRefNumberCommaSeparatedString.substring(0,
									txnKlNotFollowedRefNumberCommaSeparatedString.length() - 1));
					lastForteenDaysTxnKlNotFollwedAHWan.add(lastForteenDaysKlNotFollowedAHWrow);
				}
				List<Branch> branchList = user.getOrganization().getBranches();
				for (Branch bnch : branchList) {
					ObjectNode thisWeekBranchWiseCashExpenserow = Json.newObject();
					ObjectNode previousWeekBranchWiseCashExpenserow = Json.newObject();
					ObjectNode thisWeekPrevWeekVarianceBranchWiseCashExpenserow = Json.newObject();
					ObjectNode thisWeekBranchWiseCreditExpenserow = Json.newObject();
					ObjectNode previousWeekBranchWiseCreditExpenserow = Json.newObject();
					ObjectNode thisWeekPrevWeekVarianceBranchWiseCreditExpenserow = Json.newObject();
					ObjectNode thisWeekBranchWiseCashIncomerow = Json.newObject();
					ObjectNode previousWeekBranchWiseCashIncomerow = Json.newObject();
					ObjectNode thisWeekPrevWeekVarianceBranchWiseCashIncomerow = Json.newObject();
					ObjectNode thisWeekBranchWiseCreditIncomerow = Json.newObject();
					ObjectNode previousWeekBranchWiseCreditIncomerow = Json.newObject();
					ObjectNode thisWeekPrevWeekVarianceBranchWiseCreditIncomerow = Json.newObject();
					ObjectNode thisWeekBranchWiseExpenseBudgetrow = Json.newObject();
					ObjectNode previousWeekBranchWiseExpenseBudgetrow = Json.newObject();
					ObjectNode thisWeekPrevWeekVarianceBranchWiseExpenseBudgetrow = Json.newObject();
					ObjectNode thisWeekBranchWiseAccountReceivablesrow = Json.newObject();
					ObjectNode previousWeekBranchWiseAccountReceivablesrow = Json.newObject();
					ObjectNode thisWeekPrevWeekVarianceBranchWiseAccountReceivablesrow = Json.newObject();
					ObjectNode thisWeekBranchWiseAccountPayablesrow = Json.newObject();
					ObjectNode previousWeekBranchWiseAccountPayablesrow = Json.newObject();
					ObjectNode thisWeekPrevWeekVarianceBranchWiseAccountPayablesrow = Json.newObject();
					Double thisWeekBranchCashExpense = 0.0;
					Double thisWeekBranchCreditExpense = 0.0;
					Double thisWeekBranchCashIncome = 0.0;
					Double prevWeekBranchCashExpense = 0.0;
					Double previousWeekBranchCreditExpense = 0.0;
					Double prevWeekBranchCashIncome = 0.0;
					Double thisweekprevWeekBranchCashExpenseVariance = 0.0;
					Double thisweekprevWeekBranchCreditExpenseVarience = 0.0;
					Double thisweekprevWeekBranchCashIncomeVariance = 0.0;
					Double thisWeekBranchCreditIncome = 0.0;
					Double thisWeekBranchExpenseBudgetAvailable = 0.0;
					Double thisWeekBranchExpenseBudgetAllocated = 0.0;
					Double thisWeekBranchExpenseBudgetDeducted = 0.0;
					Double previousWeekBranchCreditIncome = 0.0;
					Double previousWeekBranchExpenseBudgetAvailable = 0.0;
					Double previousWeekBranchExpenseBudgetAllocated = 0.0;
					Double previousWeekBranchExpenseBudgetDeducted = 0.0;
					Double thisweekprevWeekBranchCreditIncomeVarience = 0.0;
					Double thisweekprevWeekBranchExpenseBudgetVarience = 0.0;
					Double thisWeekBranchAccountReceivables = 0.0;
					Double previousWeekBranchAccountReceivables = 0.0;
					Double thisweekpreviousWeekBranchAccountReceivablesVariance = 0.0;
					Double thisWeekBranchAccountPayables = 0.0;
					Double previousWeekBranchAccountPayables = 0.0;
					Double thisweekpreviousWeekBranchAccountPayablesVariance = 0.0;
					StringBuilder bnchsbquery = new StringBuilder("");
					bnchsbquery.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
							+ bnch.getId() + "' and obj.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
							+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
					List<Transaction> bnchcashexpensetxn = genericDAO.executeSimpleQuery(bnchsbquery.toString(),
							entityManager);
					if (bnchcashexpensetxn.size() > 0) {
						Object val = bnchcashexpensetxn.get(0);
						if (val != null) {
							thisWeekBranchCashExpense = Double.parseDouble(String.valueOf(val));
						}
					}
					thisWeekBranchWiseCashExpenserow.put(bnch.getName(), thisWeekBranchCashExpense);
					thisWeekBranchWiseCashExpensean.add(thisWeekBranchWiseCashExpenserow);
					StringBuilder branchpwsbquery = new StringBuilder("");
					branchpwsbquery
							.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
									+ bnch.getId() + "' and obj.transactionBranchOrganization='"
									+ user.getOrganization().getId()
									+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=11) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
									+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
					List<Transaction> bnchpwcashexpensetxn = genericDAO.executeSimpleQuery(branchpwsbquery.toString(),
							entityManager);
					if (bnchpwcashexpensetxn.size() > 0) {
						Object val = bnchpwcashexpensetxn.get(0);
						if (val != null) {
							prevWeekBranchCashExpense = Double.parseDouble(String.valueOf(val));
						}
					}
					previousWeekBranchWiseCashExpenserow.put(bnch.getName(), prevWeekBranchCashExpense);
					previousWeekBranchWiseCashExpensean.add(previousWeekBranchWiseCashExpenserow);
					thisweekprevWeekBranchCashExpenseVariance = thisWeekBranchCashExpense - prevWeekBranchCashExpense;
					thisWeekPrevWeekVarianceBranchWiseCashExpenserow.put(bnch.getName(),
							thisweekprevWeekBranchCashExpenseVariance);
					thisWeekPrevWeekVarianceBranchWiseCashExpensean
							.add(thisWeekPrevWeekVarianceBranchWiseCashExpenserow);
					StringBuilder branchcdtsbquery = new StringBuilder("");
					branchcdtsbquery
							.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
									+ bnch.getId() + "' and obj.transactionBranchOrganization='"
									+ user.getOrganization().getId()
									+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
									+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
					List<Transaction> bnchcreditexpensetxn = genericDAO.executeSimpleQuery(branchcdtsbquery.toString(),
							entityManager);
					if (bnchcreditexpensetxn.size() > 0) {
						Object val = bnchcreditexpensetxn.get(0);
						if (val != null) {
							thisWeekBranchCreditExpense = Double.parseDouble(String.valueOf(val));
						}
					}
					thisWeekBranchWiseCreditExpenserow.put(bnch.getName(), thisWeekBranchCreditExpense);
					thisWeekBranchWiseCreditExpensean.add(thisWeekBranchWiseCreditExpenserow);
					StringBuilder branchpwcdtsbquery = new StringBuilder("");
					branchpwcdtsbquery
							.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
									+ bnch.getId() + "' and obj.transactionBranchOrganization='"
									+ user.getOrganization().getId()
									+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
									+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
					List<Transaction> bnchpwcreditexpensetxn = genericDAO
							.executeSimpleQuery(branchpwcdtsbquery.toString(), entityManager);
					if (bnchpwcreditexpensetxn.size() > 0) {
						Object val = bnchpwcreditexpensetxn.get(0);
						if (val != null) {
							previousWeekBranchCreditExpense = Double.parseDouble(String.valueOf(val));
						}
					}
					previousWeekBranchWiseCreditExpenserow.put(bnch.getName(), previousWeekBranchCreditExpense);
					previousWeekBranchWiseCreditExpensean.add(previousWeekBranchWiseCreditExpenserow);
					thisweekprevWeekBranchCreditExpenseVarience = thisWeekBranchCreditExpense
							- previousWeekBranchCreditExpense;
					thisWeekPrevWeekVarianceBranchWiseCreditExpenserow.put(bnch.getName(),
							thisweekprevWeekBranchCreditExpenseVarience);
					thisWeekPrevWeekVarianceBranchWiseCreditExpensean
							.add(thisWeekPrevWeekVarianceBranchWiseCreditExpenserow);
					StringBuilder branchcssbquery = new StringBuilder("");
					branchcssbquery
							.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
									+ bnch.getId() + "' and obj.transactionBranchOrganization='"
									+ user.getOrganization().getId()
									+ "' AND obj.transactionPurpose=1 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
									+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
					List<Transaction> bnchcashselltxn = genericDAO.executeSimpleQuery(branchcssbquery.toString(),
							entityManager);
					if (bnchcashselltxn.size() > 0) {
						Object val = bnchcashselltxn.get(0);
						if (val != null) {
							thisWeekBranchCashIncome = Double.parseDouble(String.valueOf(val));
						}
					}
					thisWeekBranchWiseCashIncomerow.put(bnch.getName(), thisWeekBranchCashIncome);
					thisWeekBranchWiseCashIncomean.add(thisWeekBranchWiseCashIncomerow);
					StringBuilder branchpwcssbquery = new StringBuilder("");
					branchpwcssbquery
							.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
									+ bnch.getId() + "' and obj.transactionBranchOrganization='"
									+ user.getOrganization().getId()
									+ "' AND obj.transactionPurpose=1 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
									+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
					List<Transaction> bnchpwcashselltxn = genericDAO.executeSimpleQuery(branchpwcssbquery.toString(),
							entityManager);
					if (bnchpwcashselltxn.size() > 0) {
						Object val = bnchpwcashselltxn.get(0);
						if (val != null) {
							prevWeekBranchCashIncome = Double.parseDouble(String.valueOf(val));
						}
					}
					previousWeekBranchWiseCashIncomerow.put(bnch.getName(), prevWeekBranchCashIncome);
					previousWeekBranchWiseCashIncomean.add(previousWeekBranchWiseCashIncomerow);
					thisweekprevWeekBranchCashIncomeVariance = thisWeekBranchCashIncome - prevWeekBranchCashIncome;
					thisWeekPrevWeekVarianceBranchWiseCashIncomerow.put(bnch.getName(),
							thisweekprevWeekBranchCashIncomeVariance);
					thisWeekPrevWeekVarianceBranchWiseCashIncomean.add(thisWeekPrevWeekVarianceBranchWiseCashIncomerow);
					StringBuilder branchcdtincsbquery = new StringBuilder("");
					branchcdtincsbquery
							.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
									+ bnch.getId() + "' and obj.transactionBranchOrganization='"
									+ user.getOrganization().getId()
									+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
									+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
					List<Transaction> bnchcreditincometxn = genericDAO
							.executeSimpleQuery(branchcdtincsbquery.toString(), entityManager);
					if (bnchcreditincometxn.size() > 0) {
						Object val = bnchcreditincometxn.get(0);
						if (val != null) {
							thisWeekBranchCreditIncome = Double.parseDouble(String.valueOf(val));
						}
					}
					thisWeekBranchWiseCreditIncomerow.put(bnch.getName(), thisWeekBranchCreditIncome);
					thisWeekBranchWiseCreditIncomean.add(thisWeekBranchWiseCreditIncomerow);
					StringBuilder branchpwcdtincsbquery = new StringBuilder("");
					branchpwcdtincsbquery
							.append("select SUM(obj.netAmount) from Transaction obj WHERE obj.transactionBranch='"
									+ bnch.getId() + "' and obj.transactionBranchOrganization='"
									+ user.getOrganization().getId()
									+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
									+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
					List<Transaction> bnchpwcreditincometxn = genericDAO
							.executeSimpleQuery(branchpwcdtincsbquery.toString(), entityManager);
					if (bnchpwcreditincometxn.size() > 0) {
						Object val = bnchpwcreditincometxn.get(0);
						if (val != null) {
							previousWeekBranchCreditIncome = Double.parseDouble(String.valueOf(val));
						}
					}
					previousWeekBranchWiseCreditIncomerow.put(bnch.getName(), previousWeekBranchCreditIncome);
					previousWeekBranchWiseCreditIncomean.add(previousWeekBranchWiseCreditIncomerow);
					thisweekprevWeekBranchCreditIncomeVarience = thisWeekBranchCreditIncome
							- previousWeekBranchCreditIncome;
					thisWeekPrevWeekVarianceBranchWiseCreditIncomerow.put(bnch.getName(),
							thisweekprevWeekBranchCreditIncomeVarience);
					thisWeekPrevWeekVarianceBranchWiseCreditIncomean
							.add(thisWeekPrevWeekVarianceBranchWiseCreditIncomerow);
					StringBuilder branchwiseexpbudgetallocsbquery = new StringBuilder("");
					branchwiseexpbudgetallocsbquery
							.append("select SUM(obj.budgetTotal) from BranchSpecifics obj WHERE obj.organization='"
									+ user.getOrganization().getId() + "' and obj.branch='" + bnch.getId()
									+ "' and obj.presentStatus=1");
					List<BranchSpecifics> bnchwiseexpbudgetalloc = genericDAO
							.executeSimpleQuery(branchwiseexpbudgetallocsbquery.toString(), entityManager);
					if (bnchwiseexpbudgetalloc.size() > 0) {
						Object val = bnchwiseexpbudgetalloc.get(0);
						if (val != null) {
							thisWeekBranchExpenseBudgetAllocated = Double.parseDouble(String.valueOf(val));
						}
					}
					StringBuilder branchwiseexpbudgetdeducsbquery = new StringBuilder("");
					branchwiseexpbudgetdeducsbquery.append(
							"select SUM(obj.budgetDeductedTotal) from BranchSpecifics obj WHERE obj.organization='"
									+ user.getOrganization().getId() + "' and obj.branch='" + bnch.getId()
									+ "' and obj.presentStatus=1");
					List<BranchSpecifics> branchwiseexpbudgetdeducted = genericDAO
							.executeSimpleQuery(branchwiseexpbudgetdeducsbquery.toString(), entityManager);
					if (branchwiseexpbudgetdeducted.size() > 0) {
						Object val = branchwiseexpbudgetdeducted.get(0);
						if (val != null) {
							thisWeekBranchExpenseBudgetDeducted = Double.parseDouble(String.valueOf(val));
						}
					}
					if (thisWeekBranchExpenseBudgetDeducted != null) {
						thisWeekBranchExpenseBudgetAvailable = Double
								.parseDouble(decimalFormat.format(thisWeekBranchExpenseBudgetAllocated))
								- Double.parseDouble(decimalFormat.format(thisWeekBranchExpenseBudgetDeducted));
					}
					if (thisWeekBranchCashExpense != null) {
						thisWeekBranchExpenseBudgetAvailable = thisWeekBranchExpenseBudgetAvailable
								+ thisWeekBranchCashExpense;
					}
					if (thisWeekBranchCreditExpense != null) {
						thisWeekBranchExpenseBudgetAvailable = thisWeekBranchExpenseBudgetAvailable
								+ thisWeekBranchCreditExpense;
					}
					StringBuilder branchwisepwexpbudgetallocsbquery = new StringBuilder("");
					branchwisepwexpbudgetallocsbquery
							.append("select SUM(obj.budgetTotal) from BranchSpecifics obj WHERE obj.organization='"
									+ user.getOrganization().getId() + "' and obj.branch='" + bnch.getId()
									+ "' and obj.presentStatus=1");
					List<BranchSpecifics> branchwisepwexpbudgetalloc = genericDAO
							.executeSimpleQuery(branchwisepwexpbudgetallocsbquery.toString(), entityManager);
					if (branchwisepwexpbudgetalloc.size() > 0) {
						Object val = branchwisepwexpbudgetalloc.get(0);
						if (val != null) {
							previousWeekBranchExpenseBudgetAllocated = Double.parseDouble(String.valueOf(val));
						}
					}
					StringBuilder branchwisepwexpbudgetdeducsbquery = new StringBuilder("");
					branchwisepwexpbudgetdeducsbquery.append(
							"select SUM(obj.budgetDeductedTotal) from BranchSpecifics obj WHERE obj.organization='"
									+ user.getOrganization().getId() + "' and obj.branch='" + bnch.getId()
									+ "' and obj.presentStatus=1");
					List<BranchSpecifics> branchwisepwexpbudgetdeducted = genericDAO
							.executeSimpleQuery(branchwisepwexpbudgetdeducsbquery.toString(), entityManager);
					if (branchwisepwexpbudgetdeducted.size() > 0) {
						Object val = branchwisepwexpbudgetdeducted.get(0);
						if (val != null) {
							previousWeekBranchExpenseBudgetDeducted = Double.parseDouble(String.valueOf(val));
						}
					}
					if (previousWeekBranchExpenseBudgetDeducted == null) {
						if (previousWeekBranchExpenseBudgetAllocated != null) {
							previousWeekBranchExpenseBudgetAvailable = Double
									.parseDouble(decimalFormat.format(previousWeekBranchExpenseBudgetAllocated));
						}
					}
					if (previousWeekBranchExpenseBudgetDeducted != null
							&& !previousWeekBranchExpenseBudgetDeducted.equals("")) {
						previousWeekBranchExpenseBudgetAvailable = Double
								.parseDouble(decimalFormat.format(previousWeekBranchExpenseBudgetAllocated))
								- Double.parseDouble(decimalFormat.format(previousWeekBranchExpenseBudgetDeducted));
					}
					if (thisWeekBranchCashExpense != null) {
						previousWeekBranchExpenseBudgetAvailable = previousWeekBranchExpenseBudgetAvailable
								+ thisWeekBranchCashExpense;
					}
					if (thisWeekBranchCreditExpense != null) {
						previousWeekBranchExpenseBudgetAvailable = previousWeekBranchExpenseBudgetAvailable
								+ thisWeekBranchCreditExpense;
					}
					if (prevWeekBranchCashExpense != null) {
						previousWeekBranchExpenseBudgetAvailable = previousWeekBranchExpenseBudgetAvailable
								+ prevWeekBranchCashExpense;
					}
					if (previousWeekBranchCreditExpense != null) {
						previousWeekBranchExpenseBudgetAvailable = previousWeekBranchExpenseBudgetAvailable
								+ previousWeekBranchCreditExpense;
					}
					thisweekprevWeekBranchExpenseBudgetVarience = thisWeekBranchExpenseBudgetAvailable
							- previousWeekBranchExpenseBudgetAvailable;
					thisWeekBranchWiseExpenseBudgetrow.put(bnch.getName(), thisWeekBranchExpenseBudgetAvailable);
					thisWeekBranchWiseBudgetAvailablean.add(thisWeekBranchWiseExpenseBudgetrow);
					previousWeekBranchWiseExpenseBudgetrow.put(bnch.getName(),
							previousWeekBranchExpenseBudgetAvailable);
					previousWeekBranchWiseBudgetAvailablean.add(previousWeekBranchWiseExpenseBudgetrow);
					thisWeekPrevWeekVarianceBranchWiseExpenseBudgetrow.put(bnch.getName(),
							thisweekprevWeekBranchExpenseBudgetVarience);
					thisWeekPrevWeekVarianceBranchWiseBudgetAvailablean
							.add(thisWeekPrevWeekVarianceBranchWiseExpenseBudgetrow);
					StringBuilder branchwisecreditincomepaymentmadequery = new StringBuilder("");
					branchwisecreditincomepaymentmadequery.append(
							"select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization='"
									+ user.getOrganization().getId() + "' and obj.transactionBranch='" + bnch.getId()
									+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
									+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
					List<Transaction> branchwisecreditIncomeCustomerNetPaymentMade = genericDAO
							.executeSimpleQuery(branchwisecreditincomepaymentmadequery.toString(), entityManager);
					Object branchwisecreditIncomeCustNetPaymentMade = branchwisecreditIncomeCustomerNetPaymentMade
							.get(0);
					if (branchwisecreditIncomeCustNetPaymentMade != null
							&& !branchwisecreditIncomeCustNetPaymentMade.equals("")) {
						if (bnchcreditincometxn.size() > 0) {
							Object val = bnchcreditincometxn.get(0);
							if (val != null && !val.equals("")) {
								thisWeekBranchAccountReceivables = Double.parseDouble(decimalFormat.format(val))
										- Double.parseDouble(
												decimalFormat.format(branchwisecreditIncomeCustNetPaymentMade));
							}
						}
					}
					if (branchwisecreditIncomeCustNetPaymentMade == null) {
						if (bnchcreditincometxn.size() > 0) {
							Object val = bnchcreditincometxn.get(0);
							if (val != null && !val.equals("")) {
								thisWeekBranchAccountReceivables = Double.parseDouble(decimalFormat.format(val));
							}
						}
					}
					StringBuilder branchwisepwcreditincomepaymentmadequery = new StringBuilder("");
					branchwisepwcreditincomepaymentmadequery.append(
							"select SUM(obj.customerNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization='"
									+ user.getOrganization().getId() + "' and obj.transactionBranch='" + bnch.getId()
									+ "' AND obj.transactionPurpose=2 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
									+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
					List<Transaction> branchwisepwcreditIncomeCustomerNetPaymentMade = genericDAO
							.executeSimpleQuery(branchwisepwcreditincomepaymentmadequery.toString(), entityManager);
					Object branchwisepwcreditIncomeCustNetPaymentMade = branchwisepwcreditIncomeCustomerNetPaymentMade
							.get(0);
					if (branchwisepwcreditIncomeCustNetPaymentMade != null
							&& !branchwisepwcreditIncomeCustNetPaymentMade.equals("")) {
						if (bnchpwcreditincometxn.size() > 0) {
							Object val = bnchpwcreditincometxn.get(0);
							if (val != null && !val.equals("")) {
								previousWeekBranchAccountReceivables = Double.parseDouble(decimalFormat.format(val))
										- Double.parseDouble(
												decimalFormat.format(branchwisepwcreditIncomeCustNetPaymentMade));
							}
						}
					}
					if (branchwisepwcreditIncomeCustNetPaymentMade == null) {
						if (bnchpwcreditincometxn.size() > 0) {
							Object val = bnchpwcreditincometxn.get(0);
							if (val != null && !val.equals("")) {
								previousWeekBranchAccountReceivables = Double.parseDouble(decimalFormat.format(val));
							}
						}
					}
					thisweekpreviousWeekBranchAccountReceivablesVariance = thisWeekBranchAccountReceivables
							- previousWeekBranchAccountReceivables;
					thisWeekBranchWiseAccountReceivablesrow.put(bnch.getName(), thisWeekBranchAccountReceivables);
					thisWeekBranchWiseTotalReceivablesan.add(thisWeekBranchWiseAccountReceivablesrow);
					previousWeekBranchWiseAccountReceivablesrow.put(bnch.getName(),
							previousWeekBranchAccountReceivables);
					previousWeekBranchWiseTotalReceivablesan.add(previousWeekBranchWiseAccountReceivablesrow);
					thisWeekPrevWeekVarianceBranchWiseAccountReceivablesrow.put(bnch.getName(),
							thisweekpreviousWeekBranchAccountReceivablesVariance);
					thisWeekPrevWeekVarianceBranchWiseTotalReceivablesan
							.add(thisWeekPrevWeekVarianceBranchWiseAccountReceivablesrow);
					StringBuilder branchwisecreditexpensepaymentmadequery = new StringBuilder("");
					branchwisecreditexpensepaymentmadequery.append(
							"select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization='"
									+ user.getOrganization().getId() + "' and obj.transactionBranch='" + bnch.getId()
									+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
									+ currentWeekStartDate + "' and '" + currentWeekEndDate + "'");
					List<Transaction> branchwisecreditExpenseCustomerNetPaymentMade = genericDAO
							.executeSimpleQuery(branchwisecreditexpensepaymentmadequery.toString(), entityManager);
					Object branchwisecreditExpenseCustNetPaymentMade = branchwisecreditExpenseCustomerNetPaymentMade
							.get(0);
					if (branchwisecreditExpenseCustNetPaymentMade != null
							&& !branchwisecreditExpenseCustNetPaymentMade.equals("")) {
						if (bnchcreditexpensetxn.size() > 0) {
							Object val = bnchcreditexpensetxn.get(0);
							if (val != null && !val.equals("")) {
								thisWeekBranchAccountPayables = Double.parseDouble(decimalFormat.format(val)) - Double
										.parseDouble(decimalFormat.format(branchwisecreditExpenseCustNetPaymentMade));
							}
						}
					}
					if (branchwisecreditExpenseCustNetPaymentMade == null) {
						if (bnchcreditexpensetxn.size() > 0) {
							Object val = bnchcreditexpensetxn.get(0);
							if (val != null && !val.equals("")) {
								thisWeekBranchAccountPayables = Double.parseDouble(decimalFormat.format(val));
							}
						}
					}
					StringBuilder branchwisepwcreditexpensepaymentmadequery = new StringBuilder("");
					branchwisepwcreditexpensepaymentmadequery.append(
							"select SUM(obj.vendorNetPayment) from Transaction obj WHERE obj.transactionBranchOrganization='"
									+ user.getOrganization().getId() + "' and obj.transactionBranch='" + bnch.getId()
									+ "' AND obj.transactionPurpose=4 and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate  between '"
									+ previousWeekStartDate + "' and '" + previousWeekEndDate + "'");
					List<Transaction> branchwisepwcreditExpenseCustomerNetPaymentMade = genericDAO
							.executeSimpleQuery(branchwisepwcreditexpensepaymentmadequery.toString(), entityManager);
					Object branchwisepwcreditExpenseCustNetPaymentMade = branchwisepwcreditExpenseCustomerNetPaymentMade
							.get(0);
					if (branchwisepwcreditExpenseCustNetPaymentMade != null
							&& !branchwisepwcreditExpenseCustNetPaymentMade.equals("")) {
						if (bnchpwcreditexpensetxn.size() > 0) {
							Object val = bnchpwcreditexpensetxn.get(0);
							if (val != null && !val.equals("")) {
								previousWeekBranchAccountPayables = Double.parseDouble(decimalFormat.format(val))
										- Double.parseDouble(
												decimalFormat.format(branchwisepwcreditExpenseCustNetPaymentMade));
							}
						}
					}
					if (branchwisepwcreditExpenseCustNetPaymentMade == null) {
						if (bnchpwcreditexpensetxn.size() > 0) {
							Object val = bnchpwcreditexpensetxn.get(0);
							if (val != null && !val.equals("")) {
								previousWeekBranchAccountPayables = Double.parseDouble(decimalFormat.format(val));
							}
						}
					}
					thisweekpreviousWeekBranchAccountPayablesVariance = thisWeekBranchAccountPayables
							- previousWeekBranchAccountPayables;
					thisWeekBranchWiseAccountPayablesrow.put(bnch.getName(), thisWeekBranchAccountPayables);
					thisWeekBranchWiseTotalPayablesan.add(thisWeekBranchWiseAccountPayablesrow);
					previousWeekBranchWiseAccountPayablesrow.put(bnch.getName(), previousWeekBranchAccountPayables);
					previousWeekBranchWiseTotalPayablesan.add(previousWeekBranchWiseAccountPayablesrow);
					thisWeekPrevWeekVarianceBranchWiseAccountPayablesrow.put(bnch.getName(),
							thisweekpreviousWeekBranchAccountPayablesVariance);
					thisWeekPrevWeekVarianceBranchWiseTotalPayablesan
							.add(thisWeekPrevWeekVarianceBranchWiseAccountPayablesrow);
				}
				dashboardan.add(dashboardrow);
			} else {
				ObjectNode sessevent = Json.newObject();
				sessevent.put("sessemail", "null");
				sessionan.add(sessevent);
			}
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
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
	public Result selectSubscription() {
		log.log(Level.FINE, ">>>> Start");
		Users usrinfo = null;
		try {
			String role = "";
			usrinfo = getUserInfo(request);
			if (usrinfo == null) {
				String fullName = null;
				String roles = null;
				String email = null;
				String allowedProcurement = null;
				Organization orgn = null;
				return Results.ok(subscriberlogin.render(fullName, email, roles, orgn, allowedProcurement));
			} else {
				List<UsersRoles> userRoles = usrinfo.getUserRoles();
				for (UsersRoles roles : userRoles) {
					role += roles.getRole().getName() + ",";
				}
				role = role.substring(0, role.length() - 1);
				Organization orgn = usrinfo.getOrganization();
				Map<String, Object> criterias = new HashMap<String, Object>();
				criterias.put("presentStatus", 0);
				criterias.put("organization.id", orgn.getId());
				criterias.put("presentStatus", 1);
				List<Branch> orgActiveBranches = genericDAO.findByCriteria(Branch.class, criterias, entityManager);
				String allowedProcurement = String.valueOf(usrinfo.getAllowedProcurementRequest());
				log.log(Level.FINE, ">>>> End calling now companybranchessubscription");
				if (usrinfo != null) {
					return Results.ok(companybranchessubscription.render(usrinfo.getFullName(), usrinfo.getEmail(),
							role, orgn, allowedProcurement, orgActiveBranches, ConfigParams.getInstance()));
				} else {
					throw new Exception("User is null");
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	@Transactional
	public Result saveOrgSerialNumber(Request request) {
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();

			entitytransaction.begin();
			boolean success = organizationService.saveOrgSerialNumber(user, json, entityManager, result);
			entitytransaction.commit();
		} catch (Throwable ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			result.put("status", "failed");
		}
		return Results.ok(result);
	}

	@Transactional
	public Result uploadOrgLogo(Long orgid, Request request) {
		boolean isSaved = false;

		// EntityManager entityManager = getEntityManager();
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		ObjectNode result = Json.newObject();
		result.put("status", "failed");
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>>>>> Start + " + orgid);
			// Long orgId = json.findValue("orgId").asLong();
			Organization org = Organization.findById(orgid);
			entityManager.getTransaction().begin();
			Http.MultipartFormData<File> formData = request.body().asMultipartFormData();
			if (formData != null) {
				List<Http.MultipartFormData.FilePart<File>> filePartList = formData.getFiles();
				for (Http.MultipartFormData.FilePart<File> logoFilePart : filePartList) {
					if (logoFilePart != null) {
						// String fileName = filePart.getFilename();
						String contentType = logoFilePart.getContentType();
						TemporaryFile temporaryFile = (TemporaryFile) logoFilePart.getRef();
						String filePath = temporaryFile.path().toString();
						File file = new File(filePath);
						InputStream is = new java.io.FileInputStream(file);
						byte[] imgBytes = new byte[(int) file.length()];
						java.io.DataInputStream dataIs = new java.io.DataInputStream(is);
						dataIs.readFully(imgBytes);
						if (org != null) {
							String fileName = FileUtil.getFileName(orgid.toString() + org.getName());
							org.setLogo(imgBytes);
							org.setLogoMimeType(contentType);
							org.setLogoFileName(fileName);
							String absoluteLogoPath = application.path().toString() + "/public/images/companylogo/"
									+ fileName;
							FileUtil.saveBytesToFile(absoluteLogoPath, imgBytes);
							org.setCompanyLogo("public/images/companylogo/" + fileName);
							genericDAO.saveOrUpdate(org, user, entityManager);
							result.put("status", "success");
							isSaved = true;
						}
						dataIs.close();
						is.close();
					}
				}
			}
			entityManager.getTransaction().commit();
		} catch (Exception ex) {
			if (entityManager.getTransaction().isActive()) {
				entityManager.getTransaction().rollback();
			}
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			result.put("status", "failed");
		}
		log.log(Level.FINE, ">>>>>>> End" + result);
		return Results.ok(result);
	}

	@Transactional
	public Result savePlaceOfSupplyType(Request request) {
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			entitytransaction.begin();
			boolean success = organizationService.savePlaceOfSupplyType(user, json, entityManager);
			entitytransaction.commit();
		} catch (Throwable ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			result.put("status", "failed");
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getOrgGstinSerialNumber(Request request) {
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		// EntityManager entityManager = getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			result = organizationService.getOrgGstinSerialNumber(user, entityManager);
		} catch (Throwable ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result saveOrgGstinSerialNumber(Request request) {
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			entitytransaction.begin();
			boolean success = organizationService.saveOrgGstinSerialNumber(user, json, entityManager, result);
			entitytransaction.commit();
		} catch (Throwable ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			result.put("status", "failed");
		}
		return Results.ok(result);
	}

	@Transactional
	public Result saveTdsApplicableTrans(Request request) {
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			entitytransaction.begin();
			boolean success = organizationService.saveTdsApplicableTrans(user, json, entityManager);
			entitytransaction.commit();
			result.put("status", success);
		} catch (Throwable ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			result.put("status", "failed");
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getTdsApplyedTrans(Request request) {
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		// EntityManager entityManager = getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			Organization organization = user.getOrganization();
			String tdsApplicableTransactions = organization.getTdsApplicableTransactions();
			if (tdsApplicableTransactions != null && !"".equals(tdsApplicableTransactions)) {
				result.put("status", true);
				result.put("transactions",
						tdsApplicableTransactions.substring(0, tdsApplicableTransactions.length() - 1));
			} else {
				result.put("status", false);
			}

		} catch (Throwable ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			result.put("status", "failed");
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getCompanyOrgList(Request request) {
		log.log(Level.FINE, ">>>> Start");

		// EntityManager entityManager = getEntityManager();
		JsonNode json = request.body().asJson();
		Integer companyId = json.findValue("companyId") != null ? json.findValue("companyId").asInt() : null;
		ObjectNode result = Json.newObject();
		ArrayNode cmpOrgList = result.putArray("companyOrgList");

		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}

		try {
			List<Organization> orgList = Organization.findByCompanyId(entityManager, companyId);
			if (orgList.size() > 0) {
				for (Organization companyOrgTableList : orgList) {
					if (companyOrgTableList.getId() != (long) companyId) {
						ObjectNode row = Json.newObject();
						row.put("companyOrgId", companyOrgTableList.getId());
						row.put("companyOrgName", companyOrgTableList.getName());
						row.put("companyOrgPerName", companyOrgTableList.getPersonName());
						row.put("companyOrgEmail", companyOrgTableList.getCorporateMail());
						row.put("companyOrgPhoneNo", companyOrgTableList.getRegisteredPhoneNumber());
						row.put("companyOrgWebsite", companyOrgTableList.getWebUrl());
						cmpOrgList.add(row);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}
}
