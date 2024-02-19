package controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.DateUtil;
import com.idos.util.FileUtil;
import com.idos.util.IdosConstants;

import controllers.payroll.PayrollController;
import model.Branch;
import model.InventoryReport;
import model.PayslipReport;
import model.Specifics;
import model.TDSReport;
import model.TransactionItems;
import model.Users;
import model.VendorTDSTaxes;
import model.WithholdingTypeDetails;
import model.payroll.PayrollSetup;
import model.payroll.PayrollUserPayslip;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import java.util.logging.Level;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.VendorTdsSetupService;
import views.html.errorPage;
import play.Application;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class TDSReportController extends StaticController {
	private final Application application;
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	// private Request request;
	// private Http.Session session = request.session();

	@Inject
	public TDSReportController(JPAApi jpaApi, Application application) {
		super(application);
		this.application = application;
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result generateTDSReport(Request request) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "in generateTDSReport");
		}
		// EntityManager entityManager = getEntityManager();
		Users user = null;
		File file = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			String periodFrom = json.findValue("fromDate").asText();
			String periodTo = json.findValue("toDate").asText();
			String fromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(periodFrom));
			String toDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(periodTo));
			String reportName = "tdsReport";
			String orgName = user.getOrganization().getName() == null ? "" : user.getOrganization().getName().trim();
			orgName = orgName.replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "");
			if (orgName.length() > 8) {
				orgName = orgName.substring(0, 7);
			}
			Long timeInMillis = Calendar.getInstance().getTimeInMillis();
			String fileName = "";
			String type = "";
			String path = "";
			if (json.findValue("reportType").asText().equals("pdf")) {
				fileName = orgName + "_" + reportName + timeInMillis + ".pdf";
				type = IdosConstants.PDF_TYPE;
				path = application.path().toString() + "/logs/report/";
				File filepath = new File(path);
				if (!filepath.exists()) {
					filepath.mkdir();
				}
				path = path + fileName;
			}
			if (json.findValue("reportType").asText().equals("xlsx")) {
				fileName = orgName + "_" + reportName + timeInMillis + ".xlsx";
				type = IdosConstants.XLSX_TYPE;
				path = application.path().toString() + "/logs/report/";
				File filepath = new File(path);
				if (!filepath.exists()) {
					filepath.mkdir();
				}
				path = path + fileName;
			}
			List<TDSReport> datas = TDSReportController.getTDSData(user, fromDate, toDate, request);
			Map<String, Object> params = new HashMap<String, Object>();
			String companyLogo = FileUtil.getCompanyLogo(user.getOrganization());
			if (companyLogo != null && !"".equals(companyLogo)) {
				params.put("companyLogo", companyLogo);
			}
			if (user.getFullName() != null) {
				params.put("fullName", user.getFullName());
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
			params.put("fromDate", periodFrom);
			params.put("toDate", periodTo);
			ByteArrayOutputStream out = dynReportService.generateStaticReport(reportName, datas, params, type, request);
			file = new File(path);
			if (file.exists()) {
				file.delete();
			}
			FileOutputStream fileOut = new FileOutputStream(path);
			out.writeTo(fileOut);
			fileOut.close();
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, ">>>> End");
			}
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

	@Transactional
	public static List<TDSReport> getTDSData(Users user, String fromDate, String toDate, Request request) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, ">>>> Start");
		}
		// EntityManager entityManager = getEntityManager();
		ArrayList<TDSReport> tdsReportList = new ArrayList<TDSReport>();
		StringBuilder txnTDSQuery = new StringBuilder("");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);

		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			txnTDSQuery.delete(0, txnTDSQuery.length());
			txnTDSQuery.append(
					"select obj from TransactionItems obj where obj.transaction.id in (select obj1.id from Transaction obj1 where obj1.transactionBranchOrganization.id='"
							+ user.getOrganization().getId() + "' and obj1.transactionDate between'" + fromDate
							+ "'and '" + toDate
							+ "' and obj1.transactionPurpose.id in (3,4,8,11,30,31,32,33) and obj1.transactionStatus = 'Accounted' and obj1.presentStatus=1) and obj.presentStatus=1 and obj.withholdingAmount > 0");
			List<TransactionItems> txnItemList = genericDAO.executeSimpleQuery(txnTDSQuery.toString(), entityManager);
			if (txnItemList.size() > 0) {
				for (TransactionItems item : txnItemList) {
					DecimalFormat f = new DecimalFormat("##.00");
					VendorTDSTaxes tdsBasicDetails = null;
					if (item.getTransactionId().getTransactionVendorCustomer() != null
							&& item.getTransactionSpecifics() != null) {
						tdsBasicDetails = VendorTDSTaxes.findByOrgVend(entityManager, item.getOrganization().getId(),
								item.getTransactionId().getTransactionVendorCustomer().getId(),
								item.getTransactionSpecifics(), item.getTransactionId().getCreatedAt());
					}
					if (tdsBasicDetails != null) {
						TDSReport tdsrepData = new TDSReport();
						tdsrepData.setDateOfTxn(idosdf.format(item.getTransactionId().getTransactionDate()));
						tdsrepData.setGrossAmt(f.format(item.getGrossAmount()));
						tdsrepData.setItemName(item.getTransactionSpecifics().getName());
						tdsrepData.setRate(f.format(tdsBasicDetails.getTaxRate()));
						tdsrepData.setSection(tdsBasicDetails.getTdsSection().getName());
						tdsrepData.setVendorName(
								item.getTransactionId().getTransactionVendorCustomer().getName().toString());
						if (item.getTransactionId().getTransactionPurpose().getId() == 33) {
							tdsrepData.setTdsAmt("-" + f.format(item.getWithholdingAmount()));
						} else {
							tdsrepData.setTdsAmt(f.format(item.getWithholdingAmount()));
						}
						tdsReportList.add(tdsrepData);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, ">>>> End");
		}
		return tdsReportList;
	}
}
