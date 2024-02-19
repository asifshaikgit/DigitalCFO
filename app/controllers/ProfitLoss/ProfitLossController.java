package controllers.ProfitLoss;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.FileUtil;
import com.idos.util.IdosConstants;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import controllers.StaticController;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.ProfitLoss.ProfitLossService;
import service.ProfitLoss.ProfitLossServiceImpl;
import views.html.errorPage;
import play.mvc.Http;
import play.mvc.Http.Request;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import play.Application;
import javax.inject.Inject;

public class ProfitLossController extends StaticController {
	private Application application;
	public static JPAApi jpaApi;
	public static EntityManager entityManager;
	private Request request;

	// private Http.Session session = request.session();
	@Inject
	public ProfitLossController(Application application) {
		super(application);
		this.application = application;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result displayProfitLossReport(Request request) {
		final // EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, ">>>> Stat ");
			}
			user = getUserInfo(request);
			if (null != user) {
				PROFIT_LOSS_SERVICE.displayProfitLossReport(result, json, user, entityManager);
			} else {
				return unauthorized();
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			final String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff.toString(), user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, ">>>> End " + result);
		}
		return Results.ok(result).withHeader("ContentType", "application/json")
				.withHeader("Access-Control-Allow-Origin", "*");
	}// End of method - displayProfitLossReport

	@Transactional
	public Result saveUpdateInventoryData(Request request) {
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, ">>>> Start");
			}
			user = getUserInfo(request);
			if (null != user) {
				entitytransaction.begin();
				result = PROFIT_LOSS_SERVICE.saveUpdateInventoryData(json, user, entityManager, entitytransaction);
				entitytransaction.commit();
			} else {
				return unauthorized();
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
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, ">>>> End " + result);
		}
		return Results.ok(result).withHeader("ContentType", "application/json")
				.withHeader("Access-Control-Allow-Origin", "*");
	}// End of method - displayProfitLossReport

	@Transactional
	public Result displayInvetory(Request request) {
		// EntityManager entityManager=getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = null;
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, ">>>> Stat ");
			}
			user = getUserInfo(request);
			if (null != user) {
				result = PROFIT_LOSS_SERVICE.displayInvetory(json, user, entityManager);
			} else {
				return unauthorized();
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
			log.log(Level.FINE, ">>>> End " + result);
		}
		return Results.ok(result).withHeader("ContentType", "application/json")
				.withHeader("Access-Control-Allow-Origin", "*");
	}// End of method - displayProfitLossReport

	@Transactional
	public Result downloadPnL(Request request) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode results = Json.newObject();
		Users user = null;
		File file = null;
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start" + json);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized(results);
			}
			String exportType = json.findValue("exportType").asText();
			String reportName = "pnl";
			String orgName = user.getOrganization().getName() == null ? "" : user.getOrganization().getName().trim();
			orgName = orgName.replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "");
			if (orgName.length() > 8) {
				orgName = orgName.substring(0, 7);
			}
			Long timeInMillis = Calendar.getInstance().getTimeInMillis();
			String fileName, type;

			if (exportType.equalsIgnoreCase(IdosConstants.XLSX_TYPE)) {
				fileName = orgName + "_" + reportName + timeInMillis + ".xlsx";
				type = IdosConstants.XLSX_TYPE;
			} else {
				fileName = orgName + "_" + reportName + timeInMillis + ".pdf";
				type = IdosConstants.PDF_TYPE;
			}
			String path = application.path().toString() + "/logs/report/";
			File filepath = new File(path);
			if (!filepath.exists()) {
				filepath.mkdir();
			}
			path = path + fileName;

			List<ProfitNLossReport> datas = PROFIT_LOSS_SERVICE.getProfitNLossData(results, json, user, entityManager);
			Map<String, Object> params = new HashMap<String, Object>();
			String companyLogo = FileUtil.getCompanyLogo(user.getOrganization());
			if (companyLogo != null && !"".equals(companyLogo)) {
				params.put("companyLogo", companyLogo);
			}
			if (user.getOrganization().getName() != null) {
				params.put("companyName", user.getOrganization().getName());
			}
			if (user.getOrganization().getRegisteredAddress() != null) {
				String address = user.getOrganization().getRegisteredAddress().replaceAll("\\r\\n|\\r|\\n", " ");
				params.put("companyAddress", address);
			}
			if (user.getOrganization().getCorporateMail() != null) {
				params.put("companyEmail", user.getOrganization().getCorporateMail());
			}
			if (user.getOrganization().getRegisteredPhoneNumber() != null) {
				params.put("companyPhNo", user.getOrganization().getRegisteredPhoneNumber());
			}
			if (user.getOrganization().getWebUrl() != null) {
				params.put("companyURL", user.getOrganization().getWebUrl());
			}
			reportName = "profitnlossrpt";
			ByteArrayOutputStream out = dynReportService.getJasperPrintFromFileUsingJtable(reportName, datas, params,
					type, request, application);

			file = new File(path);
			if (file.exists()) {
				file.delete();
			}
			FileOutputStream fileOut = new FileOutputStream(path);
			out.writeTo(fileOut);
			fileOut.close();
			log.log(Level.FINE, ">>>> End");
			return Results.ok(file).withHeader("ContentType", "application/json").withHeader("Content-Disposition",
					"attachment; filename=" + fileName);
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return internalServerError(errorPage.render(ex, errorList));
		}
	}

}// End of class
