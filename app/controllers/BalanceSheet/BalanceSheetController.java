package controllers.BalanceSheet;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.FileUtil;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import model.BalanceSheetReport;
import model.ProfitNLossReport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import controllers.StaticController;
import model.Users;
import com.typesafe.config.Config;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.balancesheetservice.BalanceSheetService;
import service.balancesheetservice.BalanceSheetServiceImpl;
import views.html.errorPage;
import play.db.jpa.JPAApi;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import play.Application;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;

public class BalanceSheetController extends StaticController {
	private Application application;
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	private Request request;

	@Inject
	public BalanceSheetController(Application application) {
		super(application);
		this.application = application;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result displayBalanceSheet(Request request) {
		// final EntityManager entityManager=entityManager;
		final EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			if (log.isLoggable(Level.FINE))
				log.log(Level.FINE, ">>>>>>>>> Start " + json);

			user = StaticController.getUserInfo(request);
			if (null != user) {
				result = BALANCE_SHEET_SERVICE.displayBalanceSheet(result, json, user, entityManager);
			} else {
				return unauthorized();
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			final String strBuff = StaticController.getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff.toString(), user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>>>>>>> End" + result);
		return Results.ok(result).withHeader("ContentType", "application/json")
				.withHeader("Access-Control-Allow-Origin", "*");
	}// End of method - displayProfitLossReport

	@Transactional
	public Result downloadBS(Request request) {
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
			String reportName = "bs";
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

			List<BalanceSheetReport> datas = BALANCE_SHEET_SERVICE.getBalanceSheetData(results, json, user,
					entityManager);
			Map<String, Object> params = new HashMap<String, Object>();
			IdosUtil.seOrganization4Report(params, user.getOrganization());
			if (results.get("isCurrentDiff").asBoolean() || results.get("isPreviousDiff").asBoolean()) {
				params.put("message", "*There is difference between Assets and Liabilities.");
			} else {
				params.put("message", "");
			}

			String currPLFromDate = json.findValue("currPLFromDate") != null ? json.findValue("currPLFromDate").asText()
					: null;
			String currPLToDate = json.findValue("currPLToDate") != null ? json.findValue("currPLToDate").asText()
					: null;
			String prevPLFromDate = json.findValue("prevPLFromDate") != null ? json.findValue("prevPLFromDate").asText()
					: null;
			String prevPLToDate = json.findValue("prevPLToDate") != null ? json.findValue("prevPLToDate").asText()
					: null;

			currPLFromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currPLFromDate));
			currPLToDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currPLToDate));
			params.put("currentDateRange", currPLFromDate + " to " + currPLToDate);
			if (prevPLFromDate != null && !"".equals(prevPLFromDate) && prevPLToDate != null
					&& !"".equals(prevPLToDate)) {
				prevPLFromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevPLFromDate));
				prevPLToDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevPLToDate));
				params.put("prevDateRange", prevPLFromDate + " to " + prevPLToDate);
			}
			reportName = "balancesheetrpt";
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
}
