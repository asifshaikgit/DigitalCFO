package controllers;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http.Request;
import com.idos.util.IdosConstants;
import play.Application;
import model.Users;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

public class AgreementController extends StaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;

	// public Request request;
	@Inject
	public AgreementController(JPAApi jpaApi, Application application) {
		super(application);
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result agreedTermsAndConditions(Request request) {
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users user = getUserInfo(request);
		if (user != null) {
			JsonNode json = request.body().asJson();
			Integer agreeOrDisagree = json.findValue("agreeOrDisagree") != null
					? json.findValue("agreeOrDisagree").asInt()
					: null;
			if (agreeOrDisagree != null && agreeOrDisagree == IdosConstants.AGREE) {
				user.setAgreedTermsAndContions(IdosConstants.AGREE);
				entitytransaction.begin();
				genericDAO.saveOrUpdate(user, null, entityManager);
				entitytransaction.commit();
				results.put("url", "/config?" + user.getAuthToken());
			} else {
				results.put("url", "/signout");
			}

		} else {
			results.put("url", "/signout");
		}
		return Results.ok(results);
	}
}
