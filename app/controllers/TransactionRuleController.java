package controllers;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.TransactionRuleService;
import service.TransactionRuleServiceImpl;
import javax.inject.Inject;
import play.Application;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class TransactionRuleController extends StaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;
	private Request request;
	// private Http.Session session = request.session();

	@Inject
	public TransactionRuleController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result ruleBasedUserExistence(Request request) {
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
				result = txnRuleService.ruleBasedUserExistence(result, json, user, entityManager);
			}
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}
}
