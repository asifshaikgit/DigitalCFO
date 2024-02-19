package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.HiringRequest;
import model.ProjectLabourPosition;
import model.ReportsTo;
import model.UserRightForProject;
import model.Users;
import model.UsersRoles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import actor.CreatorActor;
import java.util.logging.Level;
import com.idos.util.CodeHelper;
import com.idos.util.IdosUtil;
import com.idos.util.JsonToMap;
import play.Application;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import pojo.TransactionViewResponse;
import views.html.*;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class LabourController extends StaticController {
	public static Application application;
	public static JPAApi jpaApi;
	public static EntityManager entityManager;
	// private Request request;

	// private Http.Session session = request.session();
	@Inject
	public LabourController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	protected static HiringRequest hiringRequest = new HiringRequest();
	protected static CRUDController<HiringRequest> hirerequestcntr = new CRUDController<HiringRequest>(application);

	@Transactional
	public Result hiringRequest(Http.Request req) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = req.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			transaction.begin();
			JsonNode json = req.body().asJson();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			user = getUserInfo(req);
			result.put("useremail", user.getEmail());
			String role = "";
			List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, user.getOrganization().getId(),
					user.getId(),
					user.getBranch().getId());
			for (UsersRoles roles : userRoles) {
				role += roles.getRole().getName() + ",";
			}
			role = role.substring(0, role.length() - 1);
			result.put("role", role);
			String hiringLabourPrimkey = json.findValue("hiringLabourPrimKey").asText();
			String projectNumber = json.findValue("project_number").asText();
			String projectTitle = json.findValue("project_name").asText();
			String requestType = json.findValue("request_type").asText();
			String position = json.findValue("jobPosition").asText();
			String status = json.findValue("status").asText();
			String remarks = json.findValue("remarks") != null ? json.findValue("remarks").asText() : null;
			String document = json.findValue("document") != null ? json.findValue("document").asText() : null;
			String projectLabourPosStr = json.findValue("jobPositionValue") != null
					? json.findValue("jobPositionValue").asText()
					: null;
			String employeeName = json.findValue("emp_details.emp_name") != null
					? json.findValue("emp_details.emp_name").asText()
					: null;
			HiringRequest request = null;
			ProjectLabourPosition pjctLabPos = null;
			Map<String, Object> b = new HashMap<String, Object>();
			Map<String, String> jsonMap = new HashMap<String, String>();
			b = new JsonToMap().toJavaMap(json, b);
			jsonMap = new JsonToMap().getSigleMap(b);
			if (hiringLabourPrimkey != "" && hiringLabourPrimkey != null) {
				request = hiringRequest.findById(IdosUtil.convertStringToLong(hiringLabourPrimkey));
			}
			if (hiringLabourPrimkey == "" || hiringLabourPrimkey == null) {
				request = new HiringRequest();
				request.setEmpId(CodeHelper.getRAMUniqueID());
			}
			if (projectLabourPosStr != null && !projectLabourPosStr.equals("")) {
				pjctLabPos = ProjectLabourPosition.findById(IdosUtil.convertStringToLong(projectLabourPosStr));
				request.setProjLabPosition(pjctLabPos);
			}
			if (null != request.getDocuments() && null != document) {
				request.setDocuments(request.getDocuments() + "," + document);
			} else if (null != document) {
				request.setDocuments(document);
			}
			if (null != request.getRemarks() && null != remarks) {
				request.setRemarks(request.getRemarks() + "," + remarks);
			} else if (null != remarks) {
				request.setRemarks(remarks);
			}
			request.setData(json.toString());
			request.setUserName(user.getFullName());
			request.setOrganization(user.getOrganization());
			hirerequestcntr.save(user, request, entityManager);
			transaction.commit();
			criterias.clear();
			criterias.put("user.organization.id", user.getOrganization().getId());
			criterias.put("userRights.id", 2L);
			criterias.put("project.id", pjctLabPos.getProject().getId());
			criterias.put("presentStatus", 1);
			List<UserRightForProject> userHasRightForProject = genericDAO.findByCriteria(UserRightForProject.class,
					criterias, entityManager);
			String approverEmailList = "";
			for (UserRightForProject userRghtPjct : userHasRightForProject) {
				approverEmailList += userRghtPjct.getUser().getEmail();
			}
			TransactionViewResponse.requestHiring(request.getId(), projectNumber, projectTitle, user.getEmail(),
					requestType,
					position, status, request.getRemarks(), document, approverEmailList, result);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
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
	public Result action(Http.Request req) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = req.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users user = null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			transaction.begin();
			JsonNode json = req.body().asJson();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			user = getUserInfo(req);
			String hiringLabour = json.findPath("hiringRequestId").asText();
			HiringRequest request = hiringRequest.findById(IdosUtil.convertStringToLong(hiringLabour));
			String status = json.findPath("status").asText();
			String remarks = json.findValue("remarks") != null ? json.findValue("remarks").asText() : null;
			String document = json.findValue("document") != null ? json.findValue("document").asText() : null;
			JsonNode jsonNode = Json.parse(request.getData());
			String requester = jsonNode.findValue("useremail").asText();
			((ObjectNode) jsonNode).put("status", status);
			request.setData(jsonNode.toString());
			request.setUserName(user.getFullName());
			request.setOrganization(user.getOrganization());
			if (null != request.getDocuments() && null != document) {
				request.setDocuments(request.getDocuments() + "," + document);
			} else if (null != document) {
				request.setDocuments(document);
			}
			if (null != request.getRemarks() && null != remarks) {
				request.setRemarks(request.getRemarks() + "," + remarks);
			} else if (null != remarks) {
				request.setRemarks(remarks);
			}
			hirerequestcntr.save(user, request, entityManager);
			if (status.equals("Employee Agreement Issued")) {
				ProjectLabourPosition pjctLabPos = request.getProjLabPosition();
				if (pjctLabPos != null) {
					pjctLabPos.setPresentStatus(0);
					genericDAO.saveOrUpdate(pjctLabPos, user, entityManager);
				}
			}
			transaction.commit();
			ProjectLabourPosition pjctLabPos = request.getProjLabPosition();
			criterias.clear();
			criterias.put("user.organization.id", user.getOrganization().getId());
			criterias.put("userRights.id", 2L);
			criterias.put("project.id", pjctLabPos.getProject().getId());
			criterias.put("presentStatus", 1);
			List<UserRightForProject> userHasRightForProject = genericDAO.findByCriteria(UserRightForProject.class,
					criterias, entityManager);
			String approverEmailList = "";
			for (UserRightForProject userRghtPjct : userHasRightForProject) {
				approverEmailList += userRghtPjct.getUser().getEmail();
			}
			TransactionViewResponse.actionHiring(request.getId(), status, request.getRemarks(), document, requester,
					approverEmailList, results);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result addNewReportsTo(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode newreportstoan = results.putArray("addedReprtsToDesignation");
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			String newReportsToDesignationName = json.findValue("newReportsToDesignationName").asText();
			ReportsTo reportTo = new ReportsTo();
			reportTo.setReportsToName(newReportsToDesignationName);
			reportTo.setOrganization(user.getOrganization());
			genericDAO.saveOrUpdate(reportTo, user, entityManager);
			transaction.commit();
			ObjectNode row = Json.newObject();
			row.put("addedReportTo", newReportsToDesignationName);
			newreportstoan.add(row);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result listProjectsReportsto(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users user = null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode projectsan = results.putArray("organizationprojects");
			ArrayNode reportstoan = results.putArray("organizationreportsto");
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			criterias.clear();
			criterias.put("user.id", user.getId());
			criterias.put("userRights.id", 1L);
			criterias.put("user.organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<UserRightForProject> userRightForProjects = genericDAO.findByCriteria(UserRightForProject.class,
					criterias, entityManager);
			for (UserRightForProject userCreationRightPjct : userRightForProjects) {
				ObjectNode row = Json.newObject();
				row.put("id", userCreationRightPjct.getProject().getId());
				row.put("projectName", userCreationRightPjct.getProject().getName() + "("
						+ userCreationRightPjct.getProject().getNumber() + ")");
				projectsan.add(row);
			}
			criterias.clear();
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<ReportsTo> orgRportsTo = genericDAO.findByCriteria(ReportsTo.class, criterias, entityManager);
			for (ReportsTo orgReportTo : orgRportsTo) {
				ObjectNode row = Json.newObject();
				row.put("reportToName", orgReportTo.getReportsToName());
				reportstoan.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result submitEmployeeDetails(Http.Request req) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = req.session();
		EntityTransaction transaction = entityManager.getTransaction();
		JsonNode jsonNode = null;
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = req.body().asJson();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			user = getUserInfo(req);
			String hiringLabour = json.findPath("hiringRequestId").asText();
			HiringRequest request = hiringRequest.findById(IdosUtil.convertStringToLong(hiringLabour));
			jsonNode = Json.parse(request.getData());
			((ObjectNode) jsonNode).put("hiringLabourPrimKey", hiringLabour);
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(jsonNode);
	}

	@Transactional
	public Result labourDetails(Http.Request req) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = req.session();
		EntityTransaction transaction = entityManager.getTransaction();
		JsonNode jsonNode = null;
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = req.body().asJson();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			user = getUserInfo(req);
			String hiringLabour = json.findPath("hiringRequestId").asText();
			String remarks = json.findValue("remarks") != null ? json.findValue("remarks").asText() : null;
			String document = json.findValue("document") != null ? json.findValue("document").asText() : null;
			HiringRequest request = hiringRequest.findById(IdosUtil.convertStringToLong(hiringLabour));
			jsonNode = Json.parse(request.getData());
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(jsonNode);
	}

	@Transactional
	public Result labourList(Request request) {

		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Map<String, Object> criterias = new HashMap<String, Object>();
		Users usrinfo = getUserInfo(request);
		ArrayNode labourHiring = result.putArray("labourHiring");
		try {
			entityTransaction.begin();
			StringBuilder sb = new StringBuilder();
			sb.append("select obj from UsersRoles obj where obj.user='" + usrinfo.getId()
					+ "' and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id");
			List<UsersRoles> userRoles = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
			String roles = "";
			for (UsersRoles role : userRoles) {
				roles += role.getRole().getName() + ",";
			}
			roles = roles.substring(0, roles.length() - 1);
			criterias.clear();
			criterias.put("organization.id", usrinfo.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<HiringRequest> hiringRequest = genericDAO.findByCriteria(HiringRequest.class, criterias, "id", true,
					entityManager);
			if (hiringRequest.size() > 0) {
				for (HiringRequest req : hiringRequest) {
					JsonNode json = Json.parse(req.getData());
					String projectNumber = json.findValue("project_number").asText();
					String projectTitle = json.findValue("project_name").asText();
					String requester = json.findValue("useremail").asText();
					String requestType = json.findValue("request_type").asText();
					String position = json.findValue("jobPosition").asText();
					String status = json.findValue("status").asText();
					ObjectNode row = Json.newObject();
					row.put("id", req.getId());
					row.put("projectNumber", projectNumber);
					row.put("projectTitle", projectTitle);
					row.put("requester", requester);
					row.put("requetType", requestType);
					row.put("position", position);
					row.put("status", status);
					row.put("userroles", roles);
					if (null != req.getDocuments() && !"".equals(req.getDocuments())) {
						row.put("document", req.getDocuments());
					}
					if (null != req.getRemarks()) {
						row.put("remarks", req.getRemarks());
					}
					ProjectLabourPosition pjctLabPos = req.getProjLabPosition();
					criterias.clear();
					criterias.put("user.organization.id", usrinfo.getOrganization().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("project.id", pjctLabPos.getProject().getId());
					criterias.put("presentStatus", 1);
					List<UserRightForProject> userHasRightForProject = genericDAO
							.findByCriteria(UserRightForProject.class, criterias, entityManager);
					String approverEmailList = "";
					for (UserRightForProject userRghtPjct : userHasRightForProject) {
						approverEmailList += userRghtPjct.getUser().getEmail();
					}
					row.put("approverEmailList", approverEmailList);
					labourHiring.add(row);
				}
			}
			entityTransaction.commit();
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result searchHiredLabour(Http.Request req) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = req.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		ArrayNode labourHiring = result.putArray("labourHiring");
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			transaction.begin();
			JsonNode jsons = req.body().asJson();
			String useremail = jsons.findValue("userEmail").asText();
			session.adding("email", useremail);
			user = getUserInfo(req);
			String hiredLabourFreeTextSearchStr = jsons.findPath("hiredLabourFreeTextSearchStr").asText();
			StringBuilder sb = new StringBuilder();
			sb.append("select obj from UsersRoles obj where obj.user='" + user.getId()
					+ "' and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id");
			List<UsersRoles> userRoles = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
			String roles = "";
			for (UsersRoles role : userRoles) {
				roles += role.getRole().getName() + ",";
			}
			roles = roles.substring(0, roles.length() - 1);
			sb.delete(0, sb.length());
			if (hiredLabourFreeTextSearchStr != null && !hiredLabourFreeTextSearchStr.equals("")) {
				sb.append("select obj from HiringRequest obj where obj.presentStatus=1 and  obj.organization='"
						+ user.getOrganization().getId() + "' and obj.presentStatus=1 and (LOCATE('"
						+ hiredLabourFreeTextSearchStr + "',obj.data)>0 or LOCATE('" + hiredLabourFreeTextSearchStr
						+ "',obj.remarks)>0) ORDER BY obj.createdAt DESC");
			} else {
				sb.append("select obj from HiringRequest obj where obj.presentStatus=1 and  obj.organization='"
						+ user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.createdAt DESC");
			}
			List<HiringRequest> hiringRequest = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
			if (hiringRequest.size() > 0) {
				for (HiringRequest request : hiringRequest) {
					JsonNode json = Json.parse(request.getData());
					String projectNumber = json.findValue("project_number").asText();
					String projectTitle = json.findValue("project_name").asText();
					String requester = json.findValue("useremail").asText();
					String requestType = json.findValue("request_type").asText();
					String position = json.findValue("jobPosition").asText();
					String status = json.findValue("status").asText();
					ObjectNode row = Json.newObject();
					row.put("id", request.getId());
					row.put("projectNumber", projectNumber);
					row.put("projectTitle", projectTitle);
					row.put("requester", requester);
					row.put("requetType", requestType);
					row.put("position", position);
					row.put("status", status);
					row.put("userroles", roles);
					if (null != request.getDocuments() && !"".equals(request.getDocuments())) {
						row.put("document", request.getDocuments());
					}
					if (null != request.getRemarks()) {
						row.put("remarks", request.getRemarks());
					}
					ProjectLabourPosition pjctLabPos = request.getProjLabPosition();
					criterias.clear();
					criterias.put("user.organization.id", user.getOrganization().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("project.id", pjctLabPos.getProject().getId());
					criterias.put("presentStatus", 1);
					List<UserRightForProject> userHasRightForProject = genericDAO
							.findByCriteria(UserRightForProject.class, criterias, entityManager);
					String approverEmailList = "";
					for (UserRightForProject userRghtPjct : userHasRightForProject) {
						approverEmailList += userRghtPjct.getUser().getEmail();
					}
					row.put("approverEmailList", approverEmailList);
					labourHiring.add(row);
				}
			}
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
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
