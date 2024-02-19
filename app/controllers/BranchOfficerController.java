package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http.Request;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.BranchOfficerService;
import service.BranchOfficerServiceImpl;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import play.db.jpa.JPAApi;
import play.Application;
import java.util.logging.Level;

public class BranchOfficerController extends StaticController {
	public static JPAApi jpaApi;
	public static EntityManager entityManager;
	public static Application application;
	private Request request;

	@Inject
	public BranchOfficerController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
		this.application = application;
	}

	@Transactional
	public final Result getDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			result = OFFICER_SERVICE.getDetails(email);
		} catch (Exception ex) {
			result = Json.newObject();
			result.put("result", false);
			result.put("message", "Something went wrong. Please try again later.");
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "BranchOfficer Details Email", "BranchOfficer Details Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

}
