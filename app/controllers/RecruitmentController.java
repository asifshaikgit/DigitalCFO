package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.IdosIntegrationKey;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import service.RecruitmentService;
import service.RecruitmentServiceImpl;
import javax.inject.Inject;
import com.typesafe.config.Config;
import play.Application;
import com.idos.util.UnauthorizedAPIAccessException;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class RecruitmentController extends StaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	private static Application application;
	private Request request;
	// private Http.Session session = request.session();

	@Inject
	public RecruitmentController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
		this.application = application;
	}

	private final RecruitmentService recruitmentService = new RecruitmentServiceImpl(application);

	@Transactional
	public Result listOpenPositionExcel(Http.Request request) throws Exception {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			String accessKey = json.findValue("accessKey").asText();
			criterias.clear();
			criterias.put("presentStatus", 1);
			criterias.put("authKey", accessKey);
			List<IdosIntegrationKey> authorizedProduct = genericDAO.findByCriteria(IdosIntegrationKey.class, criterias,
					entityManager);
			if (!authorizedProduct.isEmpty() && authorizedProduct.size() > 0) {
				if (null != email && !"".equals(email)) {
					session.adding("email", email);
					user = getUserInfo(request);

				}
			} else {
				throw new UnauthorizedAPIAccessException();
			}
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
		return Results.ok(result).withHeader("ContentType", "application/json")
				.withHeader("Access-Control-Allow-Origin", "*");
	}

	@Transactional
	public Result listOpenPositionJSON(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			String accessKey = json.findValue("accessKey").asText();
			criterias.clear();
			criterias.put("presentStatus", 1);
			criterias.put("authKey", accessKey);
			List<IdosIntegrationKey> authorizedProduct = genericDAO.findByCriteria(IdosIntegrationKey.class, criterias,
					entityManager);
			if (!authorizedProduct.isEmpty() && authorizedProduct.size() > 0) {
				if (null != email && !"".equals(email)) {
					session.adding("email", email);
					user = getUserInfo(request);

				}
			} else {
				throw new UnauthorizedAPIAccessException();
			}
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
		return Results.ok(result).withHeader("ContentType", "application/json")
				.withHeader("Access-Control-Allow-Origin", "*");
	}
}
