package controllers;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import com.idos.util.CookieUtils;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Users;
import model.UsersRoles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.PasswordChangeService;
import play.mvc.Results;
import views.html.errorPage;
import java.util.logging.Level;
import com.idos.util.DateUtil;
import com.idos.util.IdosConstants;
import com.idos.util.PasswordUtil;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class OffAddInTranController extends BaseController {

	public static JPAApi jpaApi;
	public static EntityManager entityManager;

	private Request request;

	// private Http.Session session = request.session();
	@Inject
	public OffAddInTranController() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result loginFromAddIn(Http.Request request) {
		log.log(Level.INFO, "1*********************************");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		session.removing();
		log.log(Level.INFO, "2*********************************");
		try {
			transaction.begin();
			log.log(Level.INFO, "3*************************");
			log.log(Level.INFO, "4");
			JsonNode json = request.body().asJson();
			log.log(Level.INFO, "5" + json);

			ArrayNode an = results.putArray("logincredentials");
			ObjectNode row = Json.newObject();
			String username = json.findValue("userName").asText();
			String password = json.findValue("loginpwd").asText();
			StringBuilder sbquery = new StringBuilder("");
			sbquery.append("select obj from Users obj WHERE obj.presentStatus=1 AND obj.email='" + username
					+ "' AND obj.password ='" + PasswordUtil.encrypt(password) + "'");
			List<Users> users = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
			if (!users.isEmpty()) {
				Date lastPwdUpdate = users.get(0).getLastUpdatedPasswordDate();
				if (null == lastPwdUpdate) {
					lastPwdUpdate = users.get(0).getCreatedAt();
					users.get(0).setLastUpdatedPasswordDate(lastPwdUpdate);
				}
				users.get(0).setLastLoginDate(Calendar.getInstance().getTime());
				users.get(0).setInSession(1);
				String authToken = users.get(0).createToken();

				log.log(Level.FINE, "authToken generated==" + authToken);
				users.get(0).setAuthToken(authToken);
				genericDAO.saveOrUpdate(users.get(0), users.get(0), entityManager);
				String role = "";
				List<UsersRoles> userRoles = users.get(0).getUserRoles();
				String userRole;
				boolean authorizedToUseAddIn = false;
				for (UsersRoles roles : userRoles) {
					role += roles.getRole().getName() + ",";
					userRole = roles.getRole().getName();
					if (userRole.equals("APPROVER") || userRole.equals("ACCOUNTANT") || userRole.equals("CONTROLLER")) {
						authorizedToUseAddIn = true;
					}
				}
				role = role.substring(0, role.length() - 1);
				session.adding("email", username);
				session.adding("role", userRoles.get(0).getRole().getName());
				row.put("userrole", role);
				row.put("organization", users.get(0).getOrganization().getName());
				long diff = PasswordChangeService.passwordChangeDifference(lastPwdUpdate);
				if (diff > 90) {
					row.put("days", 1);
				} else {
					row.put("days", 0);
				}
				Date currentDate = Calendar.getInstance().getTime();
				if (users.get(0).getOrganization().getTrialEndDate() != null) {
					if (mysqldf.format(currentDate)
							.compareTo(mysqldf.format(users.get(0).getOrganization().getTrialEndDate())) >= 0) {
						row.put("trialOver", 1);
					} else {
						row.put("trialOver", 0);
					}
				}
				String result;
				if (authorizedToUseAddIn) {
					result = "Success";
				} else {
					result = "UnauthorizedRole";
				}
				row.put("message", result);
				String authTokenuserIdRoleId = authToken + "|" + users.get(0).getId() + "|"
						+ users.get(0).getUserRoleIDs();
				row.put("authTokenuserIdRoleId", authTokenuserIdRoleId);
				an.add(row);
				String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
				if (ipAddress == null) {
					ipAddress = request.remoteAddress();
				}
				String action = "client from ip address " + ipAddress + " login into system";
				log.log(Level.INFO, "**********6  successs");
				auditDAO.saveAuditLogs(action, users.get(0), users.get(0).getId(), Users.class, ipAddress,
						row.toString(), entityManager);
				// CookieUtils.setCookie(IdosConstants.AUTH_TOKEN, authToken +"|"+
				// users.get(0).getId()+"|"+users.get(0).getUserRoleIDs());
				transaction.commit();
			} else {
				StringBuilder newsbquery = new StringBuilder("");
				newsbquery.append("select obj from Users obj WHERE obj.email='" + username + "' AND obj.password ='"
						+ PasswordUtil.encrypt(password) + "' and obj.presentStatus=1");
				List<Users> newusers = genericDAO.executeSimpleQuery(newsbquery.toString(), entityManager);
				String result = "Failure";
				row.put("message", result);
				String reason = "Login failed! Your email or password was entered incorrectly.";
				if (newusers.size() > 0) {
					if (newusers.get(0).getPresentStatus() == 0) {
						reason = "Login failed! Please Activate Your Account.";
					}
				}
				row.put("failurereason", reason);
				an.add(row);
				String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
				if (ipAddress == null) {
					ipAddress = request.remoteAddress();
				}
				log.log(Level.INFO, "**********6  login failed");
				String action = "An attempt to login from ip address " + ipAddress + " is failed";
				auditDAO.saveAuditLogs(action, null, null, Users.class, ipAddress, row.toString(), entityManager);
				transaction.commit();
			}
			return Results.ok(results).withHeader("ContentType", "application/json")
					.withHeader("Access-Control-Allow-Origin", "*")
					.withHeader("Access-Control-Allow-Methods", "GET, POST, HEAD, OPTIONS");
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Login Email", "Login Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}

	}

	// **************SELLS AND CUSTERMOR TRANSACTIONS*************************//
	/*
	 * SELL AT EACH BRANCH - PLOT A BAR CHART SHOWING TOTAL SALES AT EACH BRANCH.
	 * WITHIN THAT BAR GRAPH, SHOW TWO COLORS, BOTTOM BEING CASH SALES AND TOP BEING
	 * CREDIT SALES
	 */
	@Transactional
	public Result getSalesAtEachBranch(Http.Request request) {
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			// entityTransaction.begin();
			JsonNode json = request.body().asJson();
			log.log(Level.INFO, "5 getSealesAtEachBranch " + json);
			String useremail = json.findValue("useremail").asText();
			log.log(Level.INFO, "5 useremail ***********  " + useremail);
			session.adding("email", useremail);
			Users user = StaticController.getUserInfo(request);
			ArrayNode an = result.putArray("branchSalesData");
			// branchwise cash sales
			StringBuilder sb = new StringBuilder();
			sb.append(
					"select obj2.name,sum(obj1.grossAmount)  from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =1 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  group by obj1.transactionBranch");
			List<Object[]> txnLists = entityManager.createQuery(sb.toString()).getResultList();
			Collection txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap branchwiseCashSales = new HashMap();
			if (!txnLists.isEmpty()) {
				log.log(Level.INFO, "getSalesAtEachBranch*********************************" + txnLists.size());
				for (Object[] salesData : txnLists) {
					branchwiseCashSales.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in hashmap
				}
			}
			// credit sales
			sb = new StringBuilder();
			sb.append(
					"select obj2.name,sum(obj1.grossAmount)   from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =2 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  group by obj1.transactionBranch");
			txnLists = entityManager.createQuery(sb.toString()).getResultList();
			txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap branchwiseCreditSales = new HashMap();
			if (!txnLists.isEmpty()) {
				for (Object[] salesData : txnLists) {
					branchwiseCreditSales.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in
																			// hashmap
				}
			}
			if (!branchwiseCashSales.isEmpty() && !branchwiseCreditSales.isEmpty()) {
				Iterator branchNameKeys = branchwiseCashSales.keySet().iterator();
				while (branchNameKeys.hasNext()) {
					Object branchName = branchNameKeys.next();
					double cashSales = branchwiseCashSales.get(branchName) != null
							? Double.parseDouble(branchwiseCashSales.get(branchName).toString())
							: 0.0;
					double creditSales = branchwiseCreditSales.get(branchName) != null
							? Double.parseDouble(branchwiseCreditSales.get(branchName).toString())
							: 0.0;
					double totalSales = cashSales + creditSales;
					ObjectNode row = Json.newObject();
					row.put("branchName", branchName.toString());
					row.put("cashSales", cashSales);
					row.put("creditSales", creditSales);
					row.put("totalSales", totalSales);
					an.add(row);
				}
			}
			// entityTransaction.commit();
		} catch (Exception ex) {
			// if (entityTransaction.isActive()) {
			// entityTransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			log.log(Level.INFO, strBuff);
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json")
				.withHeader("Access-Control-Allow-Origin", "*")
				.withHeader("Access-Control-Allow-Methods", "GET, POST, HEAD, OPTIONS");
	}

	// **************EXPENSES AND VENDOR TRANSACTIONS*************************//

	/*
	 * EXPENSES AT EACH BRANCH - PLOT A BAR CHART SHOWING TOTAL EXPENSES AT EACH
	 * BRANCH.
	 * WITHIN THAT BAR GRAPH, SHOW TWO COLORS, BOTTOM BEING CASH EXPENSES AND TOP
	 * BEING CREDIT EXPENSES
	 * Buy from vendor on cash and credit.
	 */
	@Transactional
	public Result getExpensesAtEachBranch(Http.Request request) {
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			// entityTransaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			log.log(Level.INFO, "5 useremail ***********  " + useremail);
			session.adding("email", useremail);
			Users user = StaticController.getUserInfo(request);
			ArrayNode an = result.putArray("branchExpensesData");
			// branchwise cash purchase
			StringBuilder sb = new StringBuilder();
			sb.append(
					"select obj2.name,sum(obj1.grossAmount)   from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =3 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  group by obj1.transactionBranch");
			List<Object[]> txnLists = entityManager.createQuery(sb.toString()).getResultList();
			Collection txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap branchwiseCashExp = new HashMap();
			if (!txnLists.isEmpty()) {
				log.log(Level.INFO, "getSalesAtEachBranch*********************************" + txnLists.size());
				for (Object[] salesData : txnLists) {
					branchwiseCashExp.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in hashmap
				}
			}
			// credit purchase
			sb = new StringBuilder();
			sb.append(
					"select obj2.name,sum(obj1.grossAmount)   from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =4 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  group by obj1.transactionBranch");
			txnLists = entityManager.createQuery(sb.toString()).getResultList();
			txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap branchwiseCreditExp = new HashMap();
			if (!txnLists.isEmpty()) {
				for (Object[] salesData : txnLists) {
					branchwiseCreditExp.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in hashmap
				}
			}
			if (!branchwiseCashExp.isEmpty() && !branchwiseCreditExp.isEmpty()) {
				Iterator branchNameKeys = branchwiseCashExp.keySet().iterator();
				while (branchNameKeys.hasNext()) {
					Object branchName = branchNameKeys.next();
					double cashExpenses = branchwiseCashExp.get(branchName) != null
							? Double.parseDouble(branchwiseCashExp.get(branchName).toString())
							: 0.0;
					double creditExpenses = branchwiseCreditExp.get(branchName) != null
							? Double.parseDouble(branchwiseCreditExp.get(branchName).toString())
							: 0.0;
					double totalExpenses = cashExpenses + creditExpenses;
					ObjectNode row = Json.newObject();
					row.put("branchName", branchName.toString());
					row.put("cashExpenses", cashExpenses);
					row.put("creditExpenses", creditExpenses);
					row.put("totalExpenses", totalExpenses);
					an.add(row);
				}
			}
			// entityTransaction.commit();
		} catch (Exception ex) {
			// if (entityTransaction.isActive()) {
			// entityTransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			log.log(Level.INFO, strBuff);
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json")
				.withHeader("Access-Control-Allow-Origin", "*")
				.withHeader("Access-Control-Allow-Methods", "GET, POST, HEAD, OPTIONS");
	}

	// ADVANCE from CUSTOMER

	/*
	 * BRANCH WISE ADVANCE RECEIVED from customer 0-30, 30-60, 60-90, 90-180days
	 */
	@Transactional
	public Result getAdvaReceivedAtEachBranchIn0to180days(Http.Request request) {
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			// entityTransaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			log.log(Level.INFO, "5 useremail ***********  " + useremail);
			session.adding("email", useremail);
			Users user = StaticController.getUserInfo(request);
			ArrayNode an = result.putArray("branchCustomerAdvanceReceived");
			// receive advance from customers
			// 0-30 days
			String currDateTime = mysqldf.format(Calendar.getInstance().getTime());
			String prev30daysDate = DateUtil.returnPrevOneMonthDate(mysqldf.parse(currDateTime));
			log.log(Level.INFO, " currDateTime " + currDateTime);
			log.log(Level.INFO, " next30daysDate " + prev30daysDate);
			StringBuilder sb = new StringBuilder();
			// and obj1.transactionDate between '"+prev30daysDate+"' and '"+currDateTime+"'
			sb.append(
					"select obj2.name,sum(obj1.netAmount) from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =6 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  group by obj1.transactionBranch");
			List<Object[]> txnLists = entityManager.createQuery(sb.toString()).getResultList();
			Collection txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap custadv0to30days = new HashMap();
			if (!txnLists.isEmpty()) {
				log.log(Level.INFO, "0 - 30 days*********************************" + txnLists.size());
				for (Object[] salesData : txnLists) {
					custadv0to30days.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in hashmap
				}
			}
			// 30-60days
			currDateTime = prev30daysDate;
			prev30daysDate = DateUtil.returnPrevOneMonthDate(mysqldf.parse(currDateTime));
			log.log(Level.INFO, " currDateTime " + currDateTime);
			log.log(Level.INFO, " prev30daysDate " + prev30daysDate);
			sb = new StringBuilder();
			sb.append(
					"select obj2.name,sum(obj1.netAmount) from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =6 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  and  obj1.transactionDate  between '"
							+ prev30daysDate + "' and '" + currDateTime + "'group by obj1.transactionBranch");
			txnLists = entityManager.createQuery(sb.toString()).getResultList();
			txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap custadv30to60days = new HashMap();
			if (!txnLists.isEmpty()) {
				log.log(Level.INFO, "30 - 60 days*********************************" + txnLists.size());
				for (Object[] salesData : txnLists) {
					custadv30to60days.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in hashmap
				}
			}
			// 60-90days
			currDateTime = prev30daysDate;
			prev30daysDate = DateUtil.returnPrevOneMonthDate(mysqldf.parse(currDateTime));
			log.log(Level.INFO, " currDateTime " + currDateTime);
			log.log(Level.INFO, " next30daysDate " + prev30daysDate);
			sb = new StringBuilder();
			sb.append(
					"select obj2.name,sum(obj1.netAmount) from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =6 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  and  obj1.transactionDate  between '"
							+ prev30daysDate + "' and '" + currDateTime + "'group by obj1.transactionBranch");
			txnLists = entityManager.createQuery(sb.toString()).getResultList();
			txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap custadv60to90days = new HashMap();
			if (!txnLists.isEmpty()) {
				log.log(Level.INFO, "60 - 90 days*********************************" + txnLists.size());
				for (Object[] salesData : txnLists) {
					custadv60to90days.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in hashmap
				}
			}
			// 90-180days
			currDateTime = prev30daysDate;
			String prev90daysDate = DateUtil.returnPrevThreeMonthDate(mysqldf.parse(currDateTime));
			log.log(Level.INFO, " currDateTime " + currDateTime);
			log.log(Level.INFO, " prev90daysDate " + prev90daysDate);
			sb = new StringBuilder();
			sb.append(
					"select obj2.name,sum(obj1.netAmount) from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =6 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  and  obj1.transactionDate  between '"
							+ prev90daysDate + "' and '" + currDateTime + "'group by obj1.transactionBranch");
			txnLists = entityManager.createQuery(sb.toString()).getResultList();
			txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap custadv90to180days = new HashMap();
			if (!txnLists.isEmpty()) {
				log.log(Level.INFO, "90 - 180 days*********************************" + txnLists.size());
				for (Object[] salesData : txnLists) {
					custadv90to180days.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in hashmap
				}
			}
			if (!custadv0to30days.isEmpty() && !custadv0to30days.isEmpty()) {
				Iterator branchNameKeys = custadv0to30days.keySet().iterator();
				while (branchNameKeys.hasNext()) {
					Object branchName = branchNameKeys.next();
					double advfor0to30days = custadv0to30days.get(branchName) != null
							? Double.parseDouble(custadv0to30days.get(branchName).toString())
							: 0.0;
					double advfor30to60days = custadv30to60days.get(branchName) != null
							? Double.parseDouble(custadv30to60days.get(branchName).toString())
							: 0.0;
					double advfor60to90days = custadv60to90days.get(branchName) != null
							? Double.parseDouble(custadv60to90days.get(branchName).toString())
							: 0.0;
					double advfor90to180days = custadv90to180days.get(branchName) != null
							? Double.parseDouble(custadv90to180days.get(branchName).toString())
							: 0.0;
					double totaladvance = advfor0to30days + advfor30to60days + advfor60to90days + advfor90to180days;
					ObjectNode row = Json.newObject();
					row.put("branchName", branchName.toString());
					row.put("advfor0to30days", advfor0to30days);
					row.put("advfor30to60days", advfor30to60days);
					row.put("advfor60to90days", advfor60to90days);
					row.put("advfor90to180days", advfor90to180days);
					row.put("totaladvance", totaladvance);
					an.add(row);
				}
			}
			// entityTransaction.commit();
		} catch (Exception ex) {
			// if (entityTransaction.isActive()) {
			// entityTransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			log.log(Level.INFO, strBuff);
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("Access-Control-Allow-Origin", "*")
				.withHeader("Access-Control-Allow-Methods", "GET, POST, HEAD, OPTIONS");
	}

	/*
	 * BRANCH WISE ADVANCE Paid to vendor 0-30, 30-60, 60-90, 90-180days
	 */
	@Transactional
	public Result getAdvaPaidVendorAtEachBranchIn0to180days(Http.Request request) {
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			// entityTransaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			log.log(Level.INFO, "5 useremail ***********  " + useremail);
			session.adding("email", useremail);
			Users user = StaticController.getUserInfo(request);
			ArrayNode an = result.putArray("branchVendorAdvancePaid");
			// receive advance from customers
			// 0-30 days
			String currDateTime = mysqldf.format(Calendar.getInstance().getTime());
			String prev30daysDate = DateUtil.returnPrevOneMonthDate(mysqldf.parse(currDateTime));
			log.log(Level.INFO, " currDateTime " + currDateTime);
			log.log(Level.INFO, " next30daysDate " + prev30daysDate);
			StringBuilder sb = new StringBuilder();
			// and obj1.transactionDate between '"+prev30daysDate+"' and '"+currDateTime+"'
			sb.append(
					"select obj2.name,sum(obj1.netAmount) from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =8 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  group by obj1.transactionBranch");
			List<Object[]> txnLists = entityManager.createQuery(sb.toString()).getResultList();
			Collection txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap custadv0to30days = new HashMap();
			if (!txnLists.isEmpty()) {
				log.log(Level.INFO, "0 - 30 days*********************************" + txnLists.size());
				for (Object[] salesData : txnLists) {
					custadv0to30days.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in hashmap
				}
			}
			// 30-60days
			currDateTime = prev30daysDate;
			prev30daysDate = DateUtil.returnPrevOneMonthDate(mysqldf.parse(currDateTime));
			log.log(Level.INFO, " currDateTime " + currDateTime);
			log.log(Level.INFO, " prev30daysDate " + prev30daysDate);
			sb = new StringBuilder();
			sb.append(
					"select obj2.name,sum(obj1.netAmount) from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =8 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  and  obj1.transactionDate  between '"
							+ prev30daysDate + "' and '" + currDateTime + "'group by obj1.transactionBranch");
			txnLists = entityManager.createQuery(sb.toString()).getResultList();
			txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap custadv30to60days = new HashMap();
			if (!txnLists.isEmpty()) {
				log.log(Level.INFO, "30 - 60 days*********************************" + txnLists.size());
				for (Object[] salesData : txnLists) {
					custadv30to60days.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in hashmap
				}
			}
			// 60-90days
			currDateTime = prev30daysDate;
			prev30daysDate = DateUtil.returnPrevOneMonthDate(mysqldf.parse(currDateTime));
			log.log(Level.INFO, " currDateTime " + currDateTime);
			log.log(Level.INFO, " next30daysDate " + prev30daysDate);
			sb = new StringBuilder();
			sb.append(
					"select obj2.name,sum(obj1.netAmount) from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =8 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  and  obj1.transactionDate  between '"
							+ prev30daysDate + "' and '" + currDateTime + "'group by obj1.transactionBranch");
			txnLists = entityManager.createQuery(sb.toString()).getResultList();
			txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap custadv60to90days = new HashMap();
			if (!txnLists.isEmpty()) {
				log.log(Level.INFO, "60 - 90 days*********************************" + txnLists.size());
				for (Object[] salesData : txnLists) {
					custadv60to90days.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in hashmap
				}
			}
			// 90-180days
			currDateTime = prev30daysDate;
			String prev90daysDate = DateUtil.returnPrevThreeMonthDate(mysqldf.parse(currDateTime));
			log.log(Level.INFO, " currDateTime " + currDateTime);
			log.log(Level.INFO, " prev90daysDate " + prev90daysDate);
			sb = new StringBuilder();
			sb.append(
					"select obj2.name,sum(obj1.netAmount) from Transaction obj1, Branch obj2 where obj1.transactionBranchOrganization='"
							+ user.getOrganization().getId()
							+ "' and obj1.transactionPurpose =8 and obj1.transactionStatus='Accounted' and obj1.presentStatus=1 and obj1.transactionBranch = obj2.id  and  obj1.transactionDate  between '"
							+ prev90daysDate + "' and '" + currDateTime + "'group by obj1.transactionBranch");
			txnLists = entityManager.createQuery(sb.toString()).getResultList();
			txns = entityManager.createQuery(sb.toString()).getResultList();
			HashMap custadv90to180days = new HashMap();
			if (!txnLists.isEmpty()) {
				log.log(Level.INFO, "90 - 180 days*********************************" + txnLists.size());
				for (Object[] salesData : txnLists) {
					custadv90to180days.put(salesData[0], salesData[1]); // i.e. branch,sum(gross_amt) is set in hashmap
				}
			}
			if (!custadv0to30days.isEmpty() && !custadv0to30days.isEmpty()) {
				Iterator branchNameKeys = custadv0to30days.keySet().iterator();
				while (branchNameKeys.hasNext()) {
					Object branchName = branchNameKeys.next();
					double advfor0to30days = custadv0to30days.get(branchName) != null
							? Double.parseDouble(custadv0to30days.get(branchName).toString())
							: 0.0;
					double advfor30to60days = custadv30to60days.get(branchName) != null
							? Double.parseDouble(custadv30to60days.get(branchName).toString())
							: 0.0;
					double advfor60to90days = custadv60to90days.get(branchName) != null
							? Double.parseDouble(custadv60to90days.get(branchName).toString())
							: 0.0;
					double advfor90to180days = custadv90to180days.get(branchName) != null
							? Double.parseDouble(custadv90to180days.get(branchName).toString())
							: 0.0;
					double totaladvance = advfor0to30days + advfor30to60days + advfor60to90days + advfor90to180days;
					ObjectNode row = Json.newObject();
					row.put("branchName", branchName.toString());
					row.put("advfor0to30days", advfor0to30days);
					row.put("advfor30to60days", advfor30to60days);
					row.put("advfor60to90days", advfor60to90days);
					row.put("advfor90to180days", advfor90to180days);
					row.put("totaladvance", totaladvance);
					an.add(row);
				}
			}
			// entityTransaction.commit();
		} catch (Exception ex) {
			// if (entityTransaction.isActive()) {
			// entityTransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			log.log(Level.INFO, strBuff);
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json")
				.withHeader("Access-Control-Allow-Origin", "*")
				.withHeader("Access-Control-Allow-Methods", "GET, POST, HEAD, OPTIONS");
	}
}
