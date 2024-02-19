package controllers;

import model.Users;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import service.NotesService;
import service.NotesServiceImpl;
import javax.inject.Inject;
import play.Application;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class NotesController extends StaticController {
	public static JPAApi jpaApi;
	public static EntityManager entityManager;
	public static Application application;
	private Request request;

	// private Http.Session session = request.session();
	@Inject
	public NotesController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private final static NotesService notesService = new NotesServiceImpl();

	@Transactional
	public Result getOrganizationUsersAndProjects(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		result.put("result", false);
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText().trim();
			if (null != email && !"".equals(email)) {
				String role = json.findValue("role").asText();
				ArrayNode an = null;
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user && null != user.getOrganization() && null != user.getOrganization().getId()) {
					an = result.putArray("projects");
					ObjectNode row = notesService.getProjects(user.getOrganization(), entityManager);
					an.add(row);
					an = result.putArray("users");
					row = notesService.getUsers(user.getOrganization(), entityManager);
					an.add(row);
					an = result.putArray("branches");
					row = notesService.getBranches(user.getOrganization(), entityManager);
					an.add(row);
					an = result.putArray("notification");
					row = notesService.getNotesNotification(user);
					an.add(row);
					result.put("isTransaction", false);
					if (role.contains("ACCOUNTANT") || role.contains("AUDITOR")) {
						result.put("isTransaction", true);
						an = result.putArray("transactions");
						row = notesService.getTransactions(user.getOrganization());
						an.add(row);
						an = result.putArray("claimTransactions");
						row = notesService.getClaimTransactions(user.getOrganization());
						an.add(row);
					}
					result.put("result", true);
				}
			}
		} catch (Exception ex) {
			result.put("result", false);
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result saveNote(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText().trim();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user) {
					Long id = json.findValue("id").asLong();
					String sub = json.findValue("sub").asText();
					String note = json.findValue("note").asText();
					Long project = !"".equals(json.findValue("project").asLong()) ? json.findValue("project").asLong()
							: null;
					Long branch = !"".equals(json.findValue("branch").asLong()) ? json.findValue("branch").asLong()
							: null;
					String users = !"".equals(json.findValue("users").asText()) ? json.findValue("users").asText()
							: null;
					String file = !"".equals(json.findValue("file").asText()) ? json.findValue("file").asText() : null;
					String transaction = !"".equals(json.findValue("transaction").asText())
							? json.findValue("transaction").asText()
							: null;
					result = notesService.saveNote(id, user, users, branch, project, sub, note, file, transaction);
				}
			}
		} catch (Exception ex) {
			result.put("result", false);
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getNotes(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		result.put("result", false);
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText().trim();
			// String email = "shivkumar200013@gmail.com";
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user) {
					ArrayNode an = result.putArray("projects");
					ObjectNode row = notesService.getAllNotes(user);
					an.add(row);
					row = notesService.getAllSharedNotes(user);
					an.add(row);
					result.put("result", true);
				}
			}
		} catch (Exception ex) {
			result.put("result", false);
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getNoteById(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		result.put("result", false);
		result.put("message", "Something went wrong. Please try again later.");
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText().trim();
			Long noteId = json.findValue("id").asLong();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user) {
					result = notesService.getNoteById(noteId);
					if (null != result.findValue("id")) {
						result.put("result", true);
						result.remove("message");
					}
				}
			}
		} catch (Exception ex) {
			result.put("result", false);
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result addRemark(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText().trim();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user) {
					Long noteId = json.findValue("id").asLong();
					String remark = json.findValue("remark").asText();
					String attachment = json.findValue("file").asText();
					result = notesService.addRemark(noteId, user, remark, attachment);
				}
			}
		} catch (Exception ex) {
			result.put("result", false);
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result search(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText().trim();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user) {
					int days = json.findValue("days").asInt();
					String keyword = json.findValue("keyword").asText();
					result = notesService.search(user, keyword, days);
				}
			}
		} catch (Exception ex) {
			result.put("result", false);
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getNotesCount(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		String email = null;
		String orgName = null;
		try {
			JsonNode json = request.body().asJson();
			email = json.findValue("email").asText().trim();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				Users user = getUserInfo(request);
				if (null != user) {
					orgName = user.getOrganization().getName();
					result = notesService.getNotesNotification(user, result);

				}
			}
		} catch (Exception ex) {
			result.put("result", false);
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, email, orgName,
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}
}
