package controllers.PLBSCOAMapper;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import controllers.StaticController;
import model.Users;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.plbscoamapperservice.PLBSCOAMapperService;
import service.plbscoamapperservice.PLBSCOAMapperServiceImpl;
import javax.inject.Inject;
import play.Application;
import play.mvc.Http;
import play.mvc.Http.Request;

public class PLBSCOAMapperController extends StaticController {
	public static Application application;
	public static JPAApi jpaApi;
	public static EntityManager entityManager;
	private Request request;

	// private Http.Session session = request.session();
	@Inject
	public PLBSCOAMapperController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result mapPLBSItemsToCOA(Request request) throws Exception {

		final PLBSCOAMapperService pmService = new PLBSCOAMapperServiceImpl();

		// final EntityManager entityManager=getEntityManager();
		final EntityTransaction entitytransaction = entityManager.getTransaction();
		entitytransaction.begin();

		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("useremail").asText();
			int plBsHead = json.findValue("plBsHead").asInt();
			// String orgId = json.findValue("orgId").asText();
			String coaIds = json.findValue("coaIds").asText();
			log.log(Level.FINE, ">>>>>> Start" + json);

			user = getUserInfo(request);
			if (null != user) {
				// TODO how to get different organization id
				Long orgId = user.getOrganization().getId();
				pmService.savePLBSCOAMapping(orgId, plBsHead, coaIds, user, entityManager);
				entitytransaction.commit();
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			final String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff.toString(), user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result).withHeader("ContentType", "application/json")
				.withHeader("Access-Control-Allow-Origin", "*");
	}// End of method - displayProfitLossReport

	@Transactional
	public Result fetchPLBSItemsToCOAMapping(Request request) {
		final PLBSCOAMapperService pmService = new PLBSCOAMapperServiceImpl();
		// final EntityManager entityManager=getEntityManager();
		// final EntityTransaction entitytransaction = entityManager.getTransaction();
		// entitytransaction.begin();
		ObjectNode result = null;
		Users user = null;
		try {
			// response().setHeader("Access-Control-Allow-Origin", "*");
			JsonNode json = request.body().asJson();
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, ">>>>>> Start " + json);
			}
			user = getUserInfo(request);
			if (null != user) {
				result = pmService.fetchPLBSCOAMapping(user, entityManager);
				// entitytransaction.commit();
			}
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			final String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff.toString(), user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, ">>>>>> End " + result);
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

}
// End of class
