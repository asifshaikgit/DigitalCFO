package controllers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.IdosRegisteredVendor;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import play.mvc.Results;
import service.SupportTicketService;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import play.db.jpa.JPAApi;
import play.Application;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class SupportTicketController extends StaticController {

	private static final String[] mailCc = { "alerts@myidos.com", "allusers@myidos.com" };
	public static JPAApi jpaApi;
	public static EntityManager entityManager;
	public static Application application;
	// private Request request;
	// private Http.Session session = request.session();

	@Inject
	public SupportTicketController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
		this.application = application;
	}

	@Transactional
	public Result createSupportTicket(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			MultipartFormData<File> body = request.body().asMultipartFormData();
			Map<String, String[]> inputParts = body.asFormUrlEncoded();
			String email = inputParts.get("supportUserEmail")[0];
			if (null != email || !"".equals(email)) {
				String accType = inputParts.get("accType")[0];
				String subject = inputParts.get("supportSubject")[0];
				String message = inputParts.get("supportMessage")[0];
				String attach = inputParts.get("supportFPLink")[0];
				System.out.println(attach);
				List<FilePart<File>> fileParts = body.getFiles();
				StringBuilder attachments = new StringBuilder();
				FilePart<File> filePart = null;
				Map<String, File> files = new HashMap<String, File>();
				if (!fileParts.isEmpty() && fileParts.size() > 0) {
					for (byte b = 0; b < fileParts.size(); b++) {
						filePart = fileParts.get(b);
						files.put(filePart.getFilename(), filePart.getRef());
						attachments.append(filePart.getFilename());
						if (b != fileParts.size() - 1) {
							attachments.append(",");
						}
					}
					filePart = fileParts.get(0);
				}
				if ("user".equalsIgnoreCase(accType)) {
					session.adding("email", email);
					user = getUserInfo(request);
					if (null != user) {
						result = SupportTicketService.createSupportTicket(user, subject, message,
								attachments.toString(), null, attach, entityManager);
					}
				} else if ("seller".equalsIgnoreCase(accType)) {
					IdosRegisteredVendor vendor = SupportTicketService.getRegisteredUser(email);
					if (null != vendor) {
						result = SupportTicketService.createSupportTicket(null, subject, message,
								attachments.toString(), vendor, attach, entityManager);
					}
				}
				subject += " (" + result.get("caseId") + ")";
				sendMailWithMultipleAttachment(emailsession, subject, files, "support@myidos.com", mailCc);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getSupportTicketById(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("userEmail").asText();
			if (null != email || !"".equals(email)) {
				String filterValue = json.findValue("filterValue").asText();
				String filter = json.findValue("filter").asText();
				String accType = json.findValue("accType").asText();
				if ("user".equalsIgnoreCase(accType)) {
					session.adding("email", email);
					user = getUserInfo(request);
					if (null != user) {
						result = SupportTicketService.getSupportTicketsById(user, filter, filterValue, null,
								entityManager);
					}
				} else if ("seller".equalsIgnoreCase(accType)) {
					IdosRegisteredVendor vendor = SupportTicketService.getRegisteredUser(email);
					if (null != vendor) {
						result = SupportTicketService.getSupportTicketsById(null, filter, filterValue, vendor,
								entityManager);
					}
				}
			}
		} catch (Exception ex) {
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
	public Result updateHelpful(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("userEmail").asText();
			if (null != email || !"".equals(email)) {
				Long id = json.findValue("id").asLong();
				String help = json.findValue("help").asText();
				String accType = json.findValue("accType").asText();
				if ("user".equalsIgnoreCase(accType)) {
					session.adding("email", email);
					user = getUserInfo(request);
					if (null != user) {
						result = SupportTicketService.updateHelpFulRating(user, id, help, false, null);
					}
				} else if ("seller".equalsIgnoreCase(accType)) {
					IdosRegisteredVendor vendor = SupportTicketService.getRegisteredUser(email);
					if (null != vendor) {
						result = SupportTicketService.updateHelpFulRating(null, id, help, false, vendor);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result updateRating(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("userEmail").asText();
			if (null != email || !"".equals(email)) {
				Long id = json.findValue("id").asLong();
				String rate = json.findValue("rate").asText();
				String accType = json.findValue("accType").asText();
				if ("user".equalsIgnoreCase(accType)) {
					session.adding("email", email);
					user = getUserInfo(request);
					if (null != user) {
						result = SupportTicketService.updateHelpFulRating(user, id, rate, true, null);
					}
				} else if ("seller".equalsIgnoreCase(accType)) {
					IdosRegisteredVendor vendor = SupportTicketService.getRegisteredUser(email);
					if (null != vendor) {
						result = SupportTicketService.updateHelpFulRating(null, id, rate, true, vendor);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result addComment(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		IdosRegisteredVendor vendor = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email") != null ? json.findValue("email").asText() : "";
			if (!"".equals(email) && null != email) {
				String ticketId = json.findValue("ticketId") != null ? json.findValue("ticketId").asText() : "";
				String ticketNumber = json.findValue("ticketNumber") != null ? json.findValue("ticketNumber").asText()
						: "";
				String comments = json.findValue("comments") != null ? json.findValue("comments").asText() : "";
				String attachment = json.findValue("attachment") != null ? json.findValue("attachment").asText() : null;
				String accType = json.findValue("accType").asText();
				if ("user".equalsIgnoreCase(accType)) {
					session.adding("email", email);
					user = getUserInfo(request);
					if (null != user) {
						result = SupportTicketService.addComment(ticketId, ticketNumber, comments, attachment, user,
								null);
					}
				} else if ("seller".equalsIgnoreCase(accType)) {
					vendor = SupportTicketService.getRegisteredUser(email);
					if (null != vendor) {
						System.out.println(vendor.getId());
						result = SupportTicketService.addComment(ticketId, ticketNumber, comments, attachment, null,
								vendor);
					}
				}
				if (result.findValue("result").asBoolean()) {
					if ("user".equalsIgnoreCase(accType)) {
						result = SupportTicketService.getSupportTicketsById(user, "searchtext", " / /" + ticketNumber,
								null, entityManager);
					} else if ("seller".equalsIgnoreCase(accType)) {
						result = SupportTicketService.getSupportTicketsById(null, "searchtext", " / /" + ticketNumber,
								vendor, entityManager);
					}
					result.put("result", true);
					result.put("message", "Thank you for your comment!");
				}
			} else {
				result.put("result", false);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result openOrCloseIssue(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email") != null ? json.findValue("email").asText() : "";
			if (!"".equals(email) && null != email) {
				String ticketId = json.findValue("ticketId") != null ? json.findValue("ticketId").asText() : "";
				String ticketNumber = json.findValue("ticketNumber") != null ? json.findValue("ticketNumber").asText()
						: "";
				Integer status = json.findValue("status") != null ? json.findValue("status").asInt() : 0;
				String accType = json.findValue("accType").asText();
				if ("user".equalsIgnoreCase(accType)) {
					session.adding("email", email);
					user = getUserInfo(request);
					result = SupportTicketService.openOrCloseIssue(ticketId, ticketNumber, status, user, null);
				} else if ("seller".equalsIgnoreCase(accType)) {
					IdosRegisteredVendor vendor = SupportTicketService.getRegisteredUser(email);
					if (null != vendor) {
						result = SupportTicketService.openOrCloseIssue(ticketId, ticketNumber, status, null, vendor);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}
}