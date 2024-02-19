package controllers;

import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import model.Users;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;
import javax.inject.Inject;
import play.Application;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class VendorTDSController extends StaticController {
	public static Application application;
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	// private Request request;

	// private Http.Session session = request.session();
	@Inject
	public VendorTDSController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result saveVendorBasicTds(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			boolean status = VENDOR_TDS_SERVICE.saveVendorBasicTds(json, user, entityManager);
			transaction.commit();
			result.put("status", status);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getVendorBasicRowData(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			result = VENDOR_TDS_SERVICE.getVendorBasicRowData(result, user, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result displayTdsDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			VENDOR_TDS_SERVICE.getVendorBasicTdsDetails(result, user, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getVendorAdvanceRowData(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			JsonNode json = request.body().asJson();
			Long expenceLedger = json.get("expenceLedger") == null || "".equals(json.get("expenceLedger").asText())
					? null
					: json.get("expenceLedger").asLong();
			result = VENDOR_TDS_SERVICE.getVendorAdvanceRowData(result, user, expenceLedger, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result saveVendorAdvanceTds(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			boolean status = VENDOR_TDS_SERVICE.saveVendorAdvanceTds(json, user, entityManager);
			transaction.commit();
			result.put("status", status);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result displayTdsAdvanceDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			VENDOR_TDS_SERVICE.getVendorAdvanceTdsDetails(result, user, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

}
