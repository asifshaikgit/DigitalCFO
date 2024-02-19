package controllers;

import java.util.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Branch;
import model.HiringRequest;
import model.Project;
import model.ProjectLabourPosition;
import model.ProjectLabourPositionLanguageProficiency;
import model.ProjectLabourPositionQualification;
import model.Users;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Application;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import javax.inject.Inject;
import views.html.errorPage;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class ProjectController extends StaticController {
	public static Application application;
	public static JPAApi jpaApi;
	public static EntityManager entityManager;

	// private Request request;
	// private Http.Session session = request.session();
	@Inject
	public ProjectController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result searchProject(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String searchProject = json.findValue("searchProject").asText();
			user = getUserInfo(request);
			ArrayNode projectlistan = result.putArray("projectListData");
			String newsbquery = null;
			ArrayList inparam = new ArrayList();
			inparam.add(user.getOrganization().getId());
			if (!searchProject.trim().isEmpty()) {
				newsbquery = "select obj from Project obj WHERE obj.organization.id = ?1 and (obj.name like '%"
						+ searchProject + "%' or obj.location like '%" + searchProject
						+ "%' or obj.number like '%" + searchProject
						+ "%' and obj.presentStatus=1) order by obj.createdAt desc";
			} else {
				newsbquery = "select obj from Project obj WHERE obj.organization.id =?1 and obj.presentStatus=1 order by obj.createdAt desc";
			}
			List<Project> projectList = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
			if (projectList.size() > 0) {
				for (Project projectTableList : projectList) {
					ObjectNode row = Json.newObject();
					row.put("id", projectTableList.getId());
					row.put("name", projectTableList.getName());
					row.put("number", projectTableList.getNumber());
					String startDate = "", endDate = "";

					if (projectTableList.getStartDate() != null) {
						if (projectTableList.getStartDate().toString()
								.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d")) {
							startDate = idosdf.format(mysqldf.parse(projectTableList.getStartDate().toString()));
						} else {
							startDate = idosdf
									.format(projectDateFormat.parse(projectTableList.getStartDate().toString()));
						}
					}
					if (projectTableList.getEndDate() != null) {
						if (projectTableList.getEndDate().toString()
								.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.\\d")) {
							endDate = idosdf.format(mysqldf.parse(projectTableList.getEndDate().toString()));
						} else {
							endDate = idosdf.format(projectDateFormat.parse(projectTableList.getEndDate().toString()));
						}
					}
					row.put("startDate", startDate);
					row.put("endDate", endDate);
					row.put("location", projectTableList.getLocation());
					if (projectTableList.getPresentStatus() == 0) {
						row.put("actionText", "Activate");
					}
					if (projectTableList.getPresentStatus() == 1) {
						row.put("actionText", "Deactivate");
					}
					projectlistan.add(row);
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result checkForDuplicacy(String name, Request request) {
		System.out.println("checkforDuplicacy" + name);
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		ObjectNode result = Json.newObject();
		try {
			List<Project> entityList = Project.findListByOrgEqualName(user.getOrganization().getId(), name);
			if (entityList.size() > 0) {
				result.put("ispresent", "true");
				result.put("dataid", entityList.get(0).getId());
			} else {
				result.put("ispresent", "false");
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getProjectDatas(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("userEmail").asText();
			session.adding("email", email);
			user = getUserInfo(request);
			ArrayNode projectdataan = result.putArray("projectDatas");
			ArrayNode projectpositionan = result.putArray("projectPositionDatas");
			String pjctId = json.findValue("pjctId").asText();
			Project pjct = Project.findById(Long.parseLong(pjctId));
			ObjectNode prow = Json.newObject();
			prow.put("projectname", pjct.getName());
			prow.put("projectnumber", pjct.getNumber());
			projectdataan.add(prow);
			Calendar newestcal = Calendar.getInstance();
			String currentDate = mysqldf.format(newestcal.getTime());
			StringBuilder sbquery = new StringBuilder("select obj from ProjectLabourPosition obj where obj.project.id='"
					+ pjct.getId() + "' and obj.organization.id='" + user.getOrganization().getId()
					+ "' and obj.presentStatus=1 and  obj.positionValidity <='" + currentDate
					+ "' and obj.positionValidityTo >='" + currentDate + "'");
			List<ProjectLabourPosition> projectLabPosList = genericDAO.executeSimpleQuery(sbquery.toString(),
					entityManager);
			for (ProjectLabourPosition projLabPos : projectLabPosList) {
				criterias.clear();
				criterias.put("projLabPosition.id", projLabPos.getId());
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("presentStatus", 1);
				HiringRequest hrequest = genericDAO.getByCriteria(HiringRequest.class, criterias, entityManager);
				if (hrequest == null) {
					ObjectNode pprow = Json.newObject();
					pprow.put("projectLabPosId", projLabPos.getId());
					pprow.put("projectLabPosName", projLabPos.getPositionName());
					projectpositionan.add(pprow);
				}
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
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getJobDetailsDatas(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("userEmail").asText();
			session.adding("email", email);
			user = getUserInfo(request);
			ArrayNode projectdataan = result.putArray("jobDetailsDatas");
			ArrayNode jobDetailsLanguagean = result.putArray("jobDetailsLanguageDatas");
			String pjctPosId = json.findValue("pjctPosId").asText();
			ProjectLabourPosition projLabPos = ProjectLabourPosition.findById(Long.parseLong(pjctPosId));
			String projLabQualification = "";
			String projLabQualificationDegree = "";
			ObjectNode plprow = Json.newObject();
			if (projLabPos.getLocation() != null) {
				plprow.put("requestAllowedForBranches", projLabPos.getLocation());
			} else {
				plprow.put("requestAllowedForBranches", "");
			}
			if (projLabPos.getJobDescription() != null) {
				plprow.put("jobDescription", projLabPos.getJobDescription());
			} else {
				plprow.put("jobDescription", "");
			}
			if (projLabPos.getPlaceOfAdvertisement() != null) {
				plprow.put("advertisingPlace", projLabPos.getPlaceOfAdvertisement());
			} else {
				plprow.put("advertisingPlace", "");
			}
			List<ProjectLabourPositionQualification> projLabPosQual = projLabPos.getPjctLabourpositionQualification();
			for (ProjectLabourPositionQualification pjctPosQual : projLabPosQual) {
				String projQual = pjctPosQual.getQualificationName();
				if (projQual.equals("1")) {
					projLabQualification += "High School,";
				}
				if (projQual.equals("2")) {
					projLabQualification += "Under Graduate,";
				}
				if (projQual.equals("3")) {
					projLabQualification += "Graduate,";
				}
				if (projQual.equals("4")) {
					projLabQualification += "Post Graduate,";
				}
				if (projQual.equals("5")) {
					projLabQualification += "Professional Qualification,";
				}
				if (projQual.equals("6")) {
					projLabQualification += "Doctoral Qualification,";
				}
				if (pjctPosQual.getQualificationDegree() != null) {
					projLabQualificationDegree += pjctPosQual.getQualificationDegree() + ",";
				}
			}
			if (!projLabQualification.equals("")) {
				plprow.put("projLabQualification",
						projLabQualification.substring(0, projLabQualification.length() - 1));
			} else {
				plprow.put("projLabQualification", projLabQualification);
			}
			if (!projLabQualificationDegree.equals("")) {
				plprow.put("projLabQualificationDegree",
						"," + projLabQualificationDegree.substring(0, projLabQualificationDegree.length() - 1));
			} else {
				plprow.put("projLabQualificationDegree", projLabQualificationDegree);
			}
			if (projLabPos.getBudget() != null) {
				plprow.put("advertisingDate", decimalFormat.format(projLabPos.getBudget()));
			} else {
				plprow.put("advertisingDate", "");
			}
			if (projLabPos.getExpRequired() != null) {
				plprow.put("expReq", projLabPos.getExpRequired());
			} else {
				plprow.put("expReq", "");
			}
			projectdataan.add(plprow);
			List<ProjectLabourPositionLanguageProficiency> projLabPosLangProf = projLabPos
					.getPjctLabourpositionLangProf();
			for (ProjectLabourPositionLanguageProficiency pjctLabLanProf : projLabPosLangProf) {
				ObjectNode plplangrow = Json.newObject();
				plplangrow.put("langProf", pjctLabLanProf.getLanguaugeProficiency());
				jobDetailsLanguagean.add(plplangrow);
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
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getAllHirings(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		ArrayNode an = result.putArray("result");
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			String search = json.findValue("search") != null ? json.findValue("search").asText() : null;
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user) {
					StringBuilder query = new StringBuilder(
							"SELECT obj FROM ProjectLabourPosition obj WHERE obj.organization.id = ?1 AND obj.presentStatus = 1");
					ArrayList inparam = new ArrayList(1);
					inparam.add(user.getOrganization().getId());
					if (null != search && !"".equals(search)) {
						query.append(
								" AND (obj.positionName LIKE ?2 OR obj.location LIKE ?3 OR obj.project.name LIKE ?4 OR obj.project.number LIKE ?5)");
						inparam.add("%" + search + "%");
						inparam.add("%" + search + "%");
						inparam.add("%" + search + "%");
						inparam.add("%" + search + "%");
					}
					List<ProjectLabourPosition> labourPositions = genericDAO.queryWithParamsName(query.toString(),
							entityManager, inparam);
					if (!labourPositions.isEmpty() && labourPositions.size() > 0) {
						ObjectNode row = null;
						for (ProjectLabourPosition labourPosition : labourPositions) {
							if (null != labourPosition) {
								row = Json.newObject();
								if (null != labourPosition.getPresentStatus()) {
									if (labourPosition.getPresentStatus().equals(1)) {
										row.put("status", "Open");
									} else {
										row.put("status", "Closed");
									}
								}
								if (null != labourPosition.getLocation()) {
									row.put("location", labourPosition.getLocation());
								}
								if (null != labourPosition.getPositionName()) {
									row.put("position", labourPosition.getPositionName());
								}
								if (null != labourPosition.getProject()) {
									if (null != labourPosition.getProject().getName()) {
										row.put("projectName", labourPosition.getProject().getName());
									}
									if (null != labourPosition.getProject().getNumber()) {
										row.put("projectNumber", labourPosition.getProject().getNumber());
									}
									if (null != labourPosition.getProject().getStartDate()) {
										row.put("startDate", idosdf.format(labourPosition.getProject().getStartDate()));
									}
									if (null != labourPosition.getProject().getEndDate()) {
										row.put("endDate", idosdf.format(labourPosition.getProject().getEndDate()));
									}
								} else {
									row.put("projectName", "");
									row.put("projectNumber", "");
									row.put("startDate", "");
									row.put("endDate", "");
								}
								an.add(row);
							}
						}
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
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result deactivateProject(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		Project project = null;
		try {
			entitytransaction.begin();
			ArrayNode projectactionan = result.putArray("projectActionData");
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (user != null) {
					String entityPrimaryId = json.findValue("entityPrimaryId").asText();
					String projectActionText = json.findValue("projectActionText").asText();
					if (projectActionText != null && !projectActionText.equals("")) {
						if (projectActionText.equals("Deactivate")) {
							project = Project.findById(Long.parseLong(entityPrimaryId));
							project.setPresentStatus(0);
							genericDAO.saveOrUpdate(project, user, entityManager);
							ObjectNode row = Json.newObject();
							row.put("id", entityPrimaryId);
							row.put("actionText", "Activate");
							projectactionan.add(row);
						} else if (projectActionText.equals("Activate")) {
							project = Project.findById(Long.parseLong(entityPrimaryId));
							project.setPresentStatus(1);
							genericDAO.saveOrUpdate(project, user, entityManager);
							ObjectNode row = Json.newObject();
							row.put("id", entityPrimaryId);
							row.put("actionText", "Deactivate");
							projectactionan.add(row);
						}
					}
				}
			}
			entitytransaction.commit();
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
		return Results.ok(result);
	}
}
