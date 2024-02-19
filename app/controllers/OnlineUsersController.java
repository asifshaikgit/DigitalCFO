package controllers;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.OnlineService;
import service.OnlineServiceImpl;
import actor.AdminActor;
import actor.CreatorActor;
import javax.inject.Inject;
import play.Application;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;
import pojo.TransactionViewResponse;

public class OnlineUsersController extends StaticController {

	public static JPAApi jpaApi;
	public static EntityManager entityManager;
	public static Application application;
	private Request request;

	// private Http.Session session = request.session();
	@Inject
	public OnlineUsersController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result getOnlineIdosUsers(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			result = ONLINE_SERVICE.getOnlineIdosUsers(result, json, null, entityManager);
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getOnlineOrgUsers(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			entitytransaction.begin();
			user = getUserInfo(request);
			if (null == user) {
				result.put("result", false);
				result.put("message", "No users online!");
			} else {
				result = ONLINE_SERVICE.getOnlineOrgUsers(user, entityManager);
				if (result.findValue("result").asBoolean()) {
					JsonNode node = result.findValue("users");
					if (node.isArray()) {
						result.put("type", "onlineUsers");
						result.put("result", result.findValue("result").asBoolean());
						result.put("users", node);
						// AdminActor.online(result.findValue("result").asBoolean(), (ArrayNode) node);
						TransactionViewResponse.online(result.findValue("result").asBoolean(), (ArrayNode) node,
								result);

					}
				}
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result searchOnlineUser(final String search, final String skipEmail, Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			entitytransaction.begin();
			user = getUserInfo(request);
			if (null == user) {
				result.put("result", false);
				result.put("message", "No users online!");
			} else {
				result = ONLINE_SERVICE.getOnlineOrgUsers(user, entityManager, search, skipEmail);
				/*
				 * if (result.findValue("result").asBoolean()) {
				 * JsonNode node = result.findValue("users");
				 * if (node.isArray()) {
				 * AdminActor.online(result.findValue("result").asBoolean(), (ArrayNode) node);
				 * CreatorActor.online(result.findValue("result").asBoolean(), (ArrayNode)
				 * node);
				 * }
				 * }
				 */
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result sendChatMessage(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			entitytransaction.begin();
			user = getUserInfo(request);
			if (null == user) {
				result.put("result", false);
				result.put("message", "No users online!");
			} else {
				JsonNode json = request.body().asJson();
				String from = json.findValue("from").asText();
				String to = json.findValue("to").asText();
				String message = json.findValue("message").asText();
				result = ONLINE_SERVICE.sendChatMessage(user, entityManager, from, to, message);
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getChatHistory(final int month) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			entitytransaction.begin();
			user = getUserInfo(request);
			result = ONLINE_SERVICE.getChatHistory(user, entityManager, month);
			entitytransaction.commit();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}
}