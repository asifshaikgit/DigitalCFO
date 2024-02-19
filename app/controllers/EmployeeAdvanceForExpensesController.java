package controllers;

import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;
import model.Users;
import model.UsersRoles;

import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Application;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.EmployeeAdvanceForExpensesService;
import service.EmployeeAdvanceForExpensesServiceImpl;
import views.html.errorPage;
import javax.inject.Inject;

public class EmployeeAdvanceForExpensesController extends StaticController {

	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;
	private Request request;
	// private Http.Session session = request.session();

	@Inject
	public EmployeeAdvanceForExpensesController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result employeeAdvanceForExpenseItems(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = employeeAdvanceForExpenseService.employeeAdvanceForExpenseItems(result, json, user,
						entityManager);
			}
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }
			ex.printStackTrace();
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result displayUserEligibility(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");

		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = employeeAdvanceForExpenseService.displayUserEligibility(result, json, user, entityManager);
			}
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }

			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result submitForApproval(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result.put("useremail", user.getEmail());
				String role = "";
				List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, user.getOrganization().getId(),
						user.getId(),
						user.getBranch().getId());
				for (UsersRoles roles : userRoles) {
					role += roles.getRole().getName() + ",";
				}
				role = role.substring(0, role.length() - 1);
				result.put("role", role);
				result = employeeAdvanceForExpenseService.submitForApproval(result, json, user, entityManager,
						entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}

			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result showExpenseClaimDetails(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = employeeAdvanceForExpenseService.showExpenseClaimDetails(result, json, user, entityManager);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "", "", Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result approverAction(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (null != user) {
				result = employeeAdvanceForExpenseService.approverAction(result, json, user, entityManager,
						entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result populateUserUnsettledExpenseAdvances(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = employeeAdvanceForExpenseService.populateUserUnsettledExpenseAdvances(result, json, user,
						entityManager);
			}
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }

			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result displayUnsettledAdvances(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = employeeAdvanceForExpenseService.displayUnsettledAdvances(result, json, user, entityManager);
			}
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }

			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result expAdvanceSettlementAccountantAction(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = employeeAdvanceForExpenseService.expAdvanceSettlementAccountantAction(result, json, user,
						entityManager, entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getUserExpenseItemReimbursementAmountKl(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = employeeAdvanceForExpenseService.getUserExpenseItemReimbursementAmountKl(result, json, user,
						entityManager, entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}

			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result reimbursementApproverAction(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = employeeAdvanceForExpenseService.reimbursementApproverAction(result, json, user, entityManager,
						entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}

			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}
}
