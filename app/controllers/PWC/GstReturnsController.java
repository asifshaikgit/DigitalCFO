package controllers.PWC;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.PWC.AESShaEncryptionPWC;

import controllers.StaticController;
import model.Users;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;
import play.Application;
import javax.inject.Inject;

public class GstReturnsController extends StaticController {
	private final Application application;
	public static JPAApi jpaApi;
	public static EntityManager entityManager;
	private Request request;

	// private Http.Session session = request.session();
	@Inject
	public GstReturnsController(Application application, JPAApi jpaApi) {
		super(application);
		this.application = application;
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result downloadTransactionDataInFile(Http.Request request) {
		log.log(Level.FINE, ">>>> Start inside download transaction data");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		File file = null;

		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			entityTransaction.begin();
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			String fromDate = json.findValue("fromDate").asText();
			String toDate = json.findValue("toDate").asText();
			String exportType = json.findValue("exportType").asText();
			session.adding("email", useremail);

			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Date fromTransDate = null;
			Date toTransDate = null;

			String orgName = user.getOrganization().getName().replaceAll("\\s", "");
			// String fileName = orgName + "_Outward_Supply.txt";
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY HH:mm");
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("MM");
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("YYYY");
			Date date = new Date();
			String datewithTime = dateFormat.format(date);
			String mysz2 = datewithTime.replaceFirst("\\s", "-");
			String formateedDateTime = mysz2.replaceFirst(":", "-");

			String fileName = "FINANCIAL_SALES_AASAANGST_" + user.getOrganization().getId() + "_"
					+ dateFormat2.format(date) + "_" + dateFormat1.format(date) + "_" + formateedDateTime;
			if (exportType.equals("TXT")) {
				fileName += ".txt";
			} else {
				fileName += ".xlsx";
			}
			try {
				if (fromDate != null && !fromDate.equals("")) {
					fromTransDate = IdosConstants.mysqldf
							.parse(IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(fromDate)));
				}
				if (toDate != null && !toDate.equals("")) {
					toTransDate = IdosConstants.mysqldf
							.parse(IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(toDate)));
				}
			} catch (java.text.ParseException ex) {
				log.log(Level.SEVERE, "Date cannot be parsed", ex);
				throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
						IdosConstants.INVALID_DATA_EXCEPTION, "Date cannot be parsed");
			}
			String path = application.path().toString() + "/logs/OrgTransData/";
			if (exportType.equals("TXT")) {
				file = excelService.createOrgTransactionSellAndRecieveAdvanceData(user, entityManager, path, fileName,
						fromTransDate, toTransDate);
				entityTransaction.commit();
			} else {
				File filepath = new File(path);
				if (!filepath.exists()) {
					filepath.mkdir();
				}
				path = path + fileName;
				String sheetName = "GST Return Sales";
				excelService.createOrgTransactionSellAndRecieveAdvanceDataXlsx(user, entityManager, path, sheetName,
						fromTransDate, toTransDate);
				entityTransaction.commit();
				file = new File(path);
			}
			return Results.ok(file).withHeader("ContentType", "application/txt").withHeader("Content-Disposition",
					"attachment; filename=" + fileName);
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}

	}

	@Transactional
	public Result downloadBuyTransactionDataInFile(Http.Request request) {
		log.log(Level.FINE, ">>>> Start inside download buy transaction data");

		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		File file = null;

		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			entityTransaction.begin();
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			String fromDate = json.findValue("fromDate").asText();
			String toDate = json.findValue("toDate").asText();
			String exportType = json.findValue("exportType").asText();
			session.adding("email", useremail);

			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Date fromTransDate = null;
			Date toTransDate = null;
			String orgName = user.getOrganization().getName().replaceAll("\\s", "");
			// String fileName = orgName + "_financial_purchase.txt";
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY HH:mm");
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("MM");
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("YYYY");
			Date date = new Date();
			String datewithTime = dateFormat.format(date);
			String mysz2 = datewithTime.replaceFirst("\\s", "-");
			String formateedDateTime = mysz2.replaceFirst(":", "-");
			String fileName = "FINANCIAL_PURCHASE_AASAANGST_" + user.getOrganization().getId() + "_"
					+ dateFormat2.format(date) + "_" + dateFormat1.format(date) + "_" + formateedDateTime;
			if (exportType.equals("TXT")) {
				fileName += ".txt";
			} else {
				fileName += ".xlsx";
			}
			try {
				if (fromDate != null && !fromDate.equals("")) {
					fromTransDate = IdosConstants.mysqldf
							.parse(IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(fromDate)));
				}
				if (toDate != null && !toDate.equals("")) {
					toTransDate = IdosConstants.mysqldf
							.parse(IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(toDate)));
				}
			} catch (java.text.ParseException ex) {
				log.log(Level.SEVERE, "Date cannot be parsed", ex);
				throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
						IdosConstants.INVALID_DATA_EXCEPTION, "Date cannot be parsed");
			}
			String path = application.path().toString() + "/logs/OrgTransData/";
			if (exportType.equals("TXT")) {
				file = excelService.createOrgBuySideTransactionData(user, entityManager, path, fileName, fromTransDate,
						toTransDate);
				entityTransaction.commit();
			} else {
				File filepath = new File(path);
				if (!filepath.exists()) {
					filepath.mkdir();
				}
				path = path + fileName;
				String sheetName = "GST Return Buy";
				excelService.createOrgBuySideTransactionDataXlsx(user, entityManager, path, sheetName, fromTransDate,
						toTransDate);
				entityTransaction.commit();
				file = new File(path);
			}
			return Results.ok(result).withHeader("ContentType", "application/txt").withHeader("Content-Disposition",
					"attachment; filename=" + fileName);
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	@Transactional
	public Result callPWCGBIUrl(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();

		Users usrinfo = null;
		String userEmail = null;
		String orgaName = null;
		try {
			usrinfo = getUserInfo(request);
			if (usrinfo == null) {
				return unauthorized();
			}
			String userName = usrinfo.getFullName();
			userEmail = usrinfo.getEmail();
			Long unixtimestamp = System.currentTimeMillis() / 1000; // prints the same Unix timestamp in seconds
			String urlparams = userEmail + "|#|" + unixtimestamp.toString();
			String encryptionKey = "LZxqpewNsRpRrfOt";
			System.out.println(new String(urlparams));
			// byte[] encryptionKey = "MZygpewJsCpRrfOr".getBytes(StandardCharsets.UTF_8);
			// AESEncodeDecode advancedEncryptionStandard = new
			// AESEncodeDecode(encryptionKey);
			String encryptedString = AESShaEncryptionPWC.Encrypt(urlparams, encryptionKey);
			// byte[] plainText = urlparams.getBytes(StandardCharsets.UTF_8);
			System.out.println(new String(encryptedString));
			// byte[] cipherText = advancedEncryptionStandard.encrypt(plainText);
			// byte[] decryptedCipherText = advancedEncryptionStandard.decrypt(cipherText);
			String decryptedString = AESShaEncryptionPWC.Decrypt(encryptedString, encryptionKey);
			System.out.println(new String(decryptedString));

			// String pwcurl =
			// "http://gstwebqa.southindia.cloudapp.azure.com:8082?token="+encryptedString;
			String pwcurl = "https://aasangst.gbi-azr.pwc.co.in?token=" + encryptedString;
			result.put("pwcurl", pwcurl);
			System.out.println("pwcurl" + pwcurl);
		} catch (Exception ex) {
			log.log(Level.SEVERE, userEmail, ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, userEmail, orgaName,
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End");
		return Results.ok(result);
	}

}
