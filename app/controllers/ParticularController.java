package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Branch;
import model.Organization;
import model.Particulars;
import model.Project;
import model.Specifics;
import model.Users;
import model.UsersRoles;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import javax.inject.Inject;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;
import play.Application;
import com.idos.util.AccountCodeUtil;
import com.idos.util.IdosUtil;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class ParticularController extends StaticController {
	public static JPAApi jpaApi;
	public static EntityManager entityManager;
	public static Application application;
	private Request request;

	// private Http.Session session = request.session();
	@Inject
	public ParticularController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result saveParticular(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			Particulars newpart = null;
			String oldCatName = null;
			String categoryHidpk = json.findValue("categoryHiddenPrimaryKey").asText();
			ArrayNode an = result.putArray("newparticularsData");
			String partName = json.findValue("particularName").asText();
			String btnName = json.findValue("btnName").asText();
			String description = json.findPath("categoryDesc") != null ? json.findPath("categoryDesc").asText() : null;
			user = getUserInfo(request);
			Organization org = user.getOrganization();
			if (categoryHidpk != "" && categoryHidpk != null) {
				newpart = Particulars.findById(Long.parseLong(categoryHidpk));
				oldCatName = newpart.getName();
			} else {
				Long parentActCode = null;
				Long maxActCode = null;
				newpart = new Particulars();
				List maxPartObj = Particulars.findMaxAccountCode(entityManager, org.getId());
				if (maxPartObj.get(0) != null) {
					maxActCode = (Long) maxPartObj.get(0);
					Long actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.put("accountCode", actCode);
					criterias.put("organization.id", org.getId());
					criterias.put("presentStatus", 1);
					Specifics specf = genericDAO.getByCriteria(Specifics.class, criterias, entityManager);
					if (specf != null) {
						maxActCode = actCode;
						actCode = AccountCodeUtil.generateAccountCode(parentActCode, maxActCode);
					}
					newpart.setAccountCode(actCode);
					newpart.setAccountCodeHirarchy("/");
				}
			}
			newpart.setOrganization(user.getOrganization());
			newpart.setName(partName);
			newpart.setDescription(description);
			partcrud.save(user, newpart, entityManager);
			String stbuf = ("select obj from  Particulars obj where obj.organization.id=?1 and obj.id=?2 and obj.name=?3 and obj.presentStatus=1");
			ArrayList inparam = new ArrayList();
			inparam.add(user.getOrganization().getId());
			inparam.add(newpart.getId());
			inparam.add(partName);
			List<Particulars> particular = genericDAO.queryWithParams(stbuf, entityManager, inparam);
			for (Particulars part : particular) {
				ObjectNode row = Json.newObject();
				row.put("id", part.getId());
				row.put("name", part.getName());
				row.put("btnName", btnName);
				if (part.getDescription() != null) {
					row.put("description", part.getDescription());
				} else {
					row.put("description", "");
				}
				row.put("accountCode", part.getAccountCode());
				an.add(row);
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

	@Transactional
	public Result showCategoryDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode results = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			ArrayNode categorydetailan = results.putArray("categorydetailsData");
			String categoryEntityId = json.findValue("entityPrimaryId").asText();
			Particulars particulars = Particulars.findById(IdosUtil.convertStringToLong(categoryEntityId));
			if (particulars != null) {
				ObjectNode row = Json.newObject();
				row.put("id", particulars.getId());
				row.put("name", particulars.getName());
				if (particulars.getDescription() != null) {
					row.put("description", particulars.getDescription());
				} else {
					row.put("description", "");
				}
				categorydetailan.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "ShowCategoryDetails Email", "ShowCategoryDetails Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getUserParticularData(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			entityTransaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode particularsData = result.putArray("particularsData");
			String email = json.findValue("usermail").asText();
			session.adding("email", email);
			user = getUserInfo(request);
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.clear();
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<Particulars> particulars = genericDAO.findByCriteria(Particulars.class, criterias, entityManager);
			if (particulars.size() > 0) {
				for (Particulars part : particulars) {
					ObjectNode row = Json.newObject();
					row.put("id", part.getId());
					row.put("name", part.getName());
					particularsData.add(row);
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
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getUserProjectData(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode projectData = result.putArray("projectData");
			String email = json.findValue("usermail").asText();
			session.adding("email", email);
			user = getUserInfo(request);
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.clear();
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<Project> projects = genericDAO.findByCriteria(Project.class, criterias, entityManager);
			if (projects.size() > 0) {
				for (Project pjct : projects) {
					ObjectNode row = Json.newObject();
					row.put("id", pjct.getId());
					row.put("name", pjct.getName());
					projectData.add(row);
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
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getUserBranchData(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		// EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			// transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode branchData = result.putArray("branchData");
			String email = json.findValue("usermail").asText();
			session.adding("email", email);
			user = getUserInfo(request);
			// For Auditor get branches dropdown for which AUditor has access
			StringBuilder sb = new StringBuilder();
			sb.append("select obj from UsersRoles obj where obj.user='" + user.getId()
					+ "' and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id");
			List<UsersRoles> userRoles = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
			String roles = "";
			for (UsersRoles role : userRoles) {
				if (!role.getRole().getName().equals("OFFICERS")) {
					roles += role.getRole().getName() + ",";
				}
			}
			roles = roles.substring(0, roles.length() - 1);

			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.clear();
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			String[] audBranchArray = null;
			if (roles.equals("AUDITOR")) {
				String auditorBranches = user.getTransactionAuditorBranches();
				audBranchArray = (auditorBranches != null) ? auditorBranches.split(",") : null;
				// criterias.put("id", auditorBranches);
			}
			List<Branch> branches = genericDAO.findByCriteria(Branch.class, criterias, entityManager);
			if (branches.size() > 0) {
				for (Branch bnch : branches) {
					ObjectNode row = Json.newObject();
					// if Auditor then from Users table get Auditor Branches Rule and show only
					// those branches for search tran/trail balance/cash n bank branch
					if (roles.equals("AUDITOR") && audBranchArray != null) {
						for (int i = 0; i < audBranchArray.length; i++) {
							if (bnch.getId().intValue() == Integer.parseInt(audBranchArray[i])) {
								row.put("id", bnch.getId());
								row.put("name", bnch.getName());
								branchData.add(row);
							}
						}
					} else {
						row.put("id", bnch.getId());
						row.put("name", bnch.getName());
						branchData.add(row);
					}
				}
			}
			// transaction.commit();
		} catch (Exception ex) {
			// if (transaction.isActive()) {
			// transaction.rollback();
			// }
			// log.log(Level.SEVERE, ex.getMessage());
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End");
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

}
