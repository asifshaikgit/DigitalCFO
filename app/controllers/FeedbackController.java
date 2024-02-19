package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.CountryPhoneCode;
import model.IdosCustomerFeedback;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Application;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.*;
import java.util.logging.Level;

@SuppressWarnings("unchecked")
public class FeedbackController extends StaticController {
	public static Application application;
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	private Request request;

	@Inject
	public FeedbackController(JPAApi jpaApi, Application application) {
		super(application);
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result submit(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		String result = null;
		Users user = null;
		try {
			JsonNode requests = request.body().asJson();
			String email = requests.findValue("email").asText();
			String name = requests.findValue("name").asText();
			String number = requests.findValue("number").asText();
			String subject = requests.findValue("subject").asText();
			String text = requests.findValue("text").asText();
			user = getUserInfo(request);
			if ("" != text) {
				IdosCustomerFeedback feedback = new IdosCustomerFeedback(name, number, subject, email, text);
				transaction.begin();
				if (user != null) {
					genericDAO.saveOrUpdate(feedback, user, entityManager);
				} else {
					genericDAO.saveOrUpdate(feedback, null, entityManager);
				}
				StringBuilder sub = new StringBuilder("Feedback - ").append(subject);
				mailTimer(text, email, feedbackSession, "feedback@myidos.com", "alerts@myidos.com", sub.toString());
				result = "success";
				transaction.commit();
			}
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			// log.log(Level.SEVERE, ex.getMessage());
			result = "failure";
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getAreaCodes() {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("result");
		ObjectNode row = null;
		try {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("presentStatus", 1);
			List<CountryPhoneCode> phoneCodes = genericDAO.findByCriteria(CountryPhoneCode.class, criterias,
					"countryWithCode", false, entityManager);
			if (!phoneCodes.isEmpty() && phoneCodes.size() > 0) {
				for (CountryPhoneCode phoneCode : phoneCodes) {
					if (null != phoneCode) {
						row = Json.newObject();
						if (null != phoneCode.getCountryWithCode()) {
							row.put("countryCode", phoneCode.getCountryWithCode());
						}
						if (null != phoneCode.getAreaCode()) {
							row.put("areaCode", phoneCode.getAreaCode());
						}
						an.add(row);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Get Area Codes Email", "Get Area Codes Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result submitContactUs(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		String result = null;
		Users user = null;
		try {
			JsonNode requests = request.body().asJson();
			String email = requests.findValue("email").asText();
			String name = requests.findValue("name").asText();
			String number = requests.findValue("number").asText();
			String subject = requests.findValue("subject").asText();
			String text = requests.findValue("text").asText();
			user = getUserInfo(request);
			if ("" != text) {
				IdosCustomerFeedback feedback = new IdosCustomerFeedback(name, number, subject, email, text);
				transaction.begin();
				if (user != null) {
					genericDAO.saveOrUpdate(feedback, user, entityManager);
				} else {
					genericDAO.saveOrUpdate(feedback, null, entityManager);
				}
				StringBuilder sub = new StringBuilder("Feedback - ").append(subject);
				mailTimer(text, email, feedbackSession, "feedback@myidos.com", "alerts@myidos.com", sub.toString());
				result = "success";
				transaction.commit();
			}
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			// log.log(Level.SEVERE, ex.getMessage());
			result = "failure";
		}
		return Results.ok(result);
	}

}
