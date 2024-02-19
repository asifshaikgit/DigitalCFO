package controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IdosConstants;
import java.util.logging.Level;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.DynamicReportService;
import service.DynamicReportServiceImpl;
import service.InvoiceService;
import service.InvoiceServiceImpl;
import play.Application;
import javax.inject.Inject;
import views.html.*;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class InvoiceVoucherController extends StaticController {
	private Application application;
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	private Request request;
	// private Http.Session session = request.session();

	@Inject
	public InvoiceVoucherController(Application application) {
		super(application);
		this.application = application;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result generateInvoice(Request request) {
		// EntityManager entityManager = getEntityManager();
		// EntityTransaction entityTransaction = entityManager.getTransaction();
		String reportType = null;
		String reportName = null;

		ObjectNode results = Json.newObject();
		ArrayNode an = results.putArray("invoiceFileName");
		Users user = null;
		Vendor vendorUser = null;
		File file = null;
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start" + json);
			user = getUserInfo(request);
			if (user == null) {
				vendorUser = getVendorInfo(request);
				if (vendorUser == null) {
					log.log(Level.SEVERE, "unauthorized");
					return unauthorized(results);
				} else {
					user = Users.findActiveByEmail(vendorUser.getOrganization().getCorporateMail());
				}
			}
			Long entityTxnId = json.findValue("entityTxnId").asLong();
			String exportType = json.findValue("exportType") == null ? IdosConstants.PDF_TYPE
					: json.findValue("exportType").asText();

			Transaction transaction = genericDAO.getById(Transaction.class, entityTxnId, entityManager);
			if (transaction == null) {
				log.log(Level.SEVERE, "Transaction not found: " + entityTxnId);
				return internalServerError();
			}
			reportName = "invoice";

			Long txnPurpose = transaction.getTransactionPurpose().getId();
			if (txnPurpose == IdosConstants.PREPARE_QUOTATION) {
				reportName = "quotation";
			} else if (txnPurpose == IdosConstants.PROFORMA_INVOICE || txnPurpose == IdosConstants.PURCHASE_ORDER) {
				reportName = "proforma";
			}
			String orgName = user.getOrganization().getName() == null ? "" : user.getOrganization().getName().trim();
			orgName = orgName.replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "");
			if (orgName.indexOf("/") != -1) {
				orgName = orgName.replaceAll("/", "");
			}
			if (orgName.indexOf("\\") != -1) {
				orgName = orgName.replaceAll("\\\\", "");
			}
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
			String path = application.path().toString() + "/public/report/";
			File filepath = new File(path);
			if (!filepath.exists()) {
				filepath.mkdir();
			}
			path = path + fileName;
			List<InvoiceReportModel> datas = null;
			List<GRNoteModel> datasGrn = null;
			Map<String, Object> params = null;
			ByteArrayOutputStream out = null;
			// entityTransaction.begin();

			if (txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
					|| txnPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
					|| (txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
							&& transaction.getTypeIdentifier() == 2)) {
				TransactionInvoice invoiceLog = TransactionInvoice.findByTransactionID(entityManager,
						user.getOrganization().getId(), transaction.getId());
				Integer option = null;
				if (json.findValue("optionValue") != null)
					option = json.findValue("optionValue").asInt();
				else
					option = 0;
				if (option != null && option == 1) {

					datas = INVOICE_SERVICE.generateBuyGstInvoiceData(user.getOrganization(), transaction,
							entityManager);

					params = INVOICE_SERVICE.getGstInvoiceParams(transaction, json, datas, invoiceLog, user,
							entityManager);
					reportName = (String) params.get("invoiceName");

					out = dynReportService.getJasperPrintFromFileUsingJtable(reportName, datas, params, type, request,
							application);
				} else if (option != null && (option == 2 || option == 0)) {

					datasGrn = INVOICE_SERVICE.generateGRNDatas(user.getOrganization(), transaction, entityManager);

					params = INVOICE_SERVICE.getGRNParams(transaction, json, null, invoiceLog);
					reportName = (String) params.get("invoiceName");
					out = dynReportService.getJasperPrintFromFileUsingJtable(reportName, datasGrn, params, type,
							request, application);
				}

			} else if (user.getOrganization().getGstCountryCode() != null
					&& !"".equals(user.getOrganization().getGstCountryCode())
					&& (txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
							|| txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
							|| txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER
							|| txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER
							|| (txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
									&& transaction.getTypeIdentifier() == 1))) {
				if (txnPurpose != IdosConstants.CREDIT_NOTE_CUSTOMER
						&& txnPurpose != IdosConstants.DEBIT_NOTE_CUSTOMER) {
					INVOICE_SERVICE.saveInvoiceLog(user, entityManager, transaction, fileName, json);
				}
				TransactionInvoice invoiceLog = TransactionInvoice.findByTransactionID(entityManager,
						user.getOrganization().getId(), transaction.getId());
				datas = INVOICE_SERVICE.generateGstInvoiceData(user.getOrganization(), transaction, entityManager, json,
						invoiceLog);
				List<InvoiceAdvanceModel> advList = INVOICE_SERVICE.getAdvTxnGstInvoice(user.getOrganization(),
						transaction, entityManager);
				params = INVOICE_SERVICE.getGstInvoiceParams(transaction, json, datas, invoiceLog, user, entityManager);
				reportName = (String) params.get("invoiceName");
				out = dynReportService.getJasperPrintFromFileUsingJtable(reportName, datas, advList, params, type,
						request, application);
				/*
				 * if (txnPurpose != IdosConstants.CREDIT_NOTE_CUSTOMER && txnPurpose !=
				 * IdosConstants.DEBIT_NOTE_CUSTOMER) {
				 * INVOICE_SERVICE.saveInvoiceLog(user, entityManager, transaction, fileName,
				 * json);
				 * }
				 */
			} else {
				datas = INVOICE_SERVICE.generateInvoiceData(user.getOrganization(), transaction, entityManager);
				params = INVOICE_SERVICE.getInvoiceParams(transaction, json, entityManager);
				if (datas != null && datas.size() > 0) {
					params.put("netInvValueAfterTax", datas.get(0).getNetAmt()); // this value doesn't contain advance
																					// adjustments
				}
				out = dynReportService.getJasperPrintFromFileUsingJtable(reportName, datas, params, type, request,
						application);
			}

			file = new File(path);
			if (file.exists()) {
				file.delete();
			}

			FileOutputStream fileOut = new FileOutputStream(file);
			out.writeTo(fileOut);
			fileOut.close();
			// entityTransaction.commit();
			log.log(Level.FINE, ">>>> End");
			return Results.ok(file).withHeader("ContentType", "application/json").withHeader("Content-Disposition",
					"attachment; filename=" + fileName);

		} catch (NullPointerException ex) {
			log.log(Level.SEVERE, "Null Pointer Exception", ex);
		} catch (Exception ex) {
			// if (entityTransaction.isActive()) {
			// entityTransaction.rollback();
			// }
			String strBuff = getStackTraceMessage(ex);
			if (user != null) {
				log.log(Level.SEVERE, user.getEmail(), ex);
				expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName());
			} else if (vendorUser != null) {
				log.log(Level.SEVERE, vendorUser.getEmail(), ex);
				expService.sendExceptionReport(strBuff, vendorUser.getEmail(), vendorUser.getOrganization().getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName());
			} else {
				log.log(Level.SEVERE, "Error", ex);
				expService.sendExceptionReport(strBuff, "", "",
						Thread.currentThread().getStackTrace()[1].getMethodName());
			}
			List<String> errorList = getStackTrace(ex);
			return internalServerError(errorPage.render(ex, errorList));
		}
		return null;
	}

	@Transactional
	public Result getShppingAddress(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = getUserInfo(request);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start" + json);
			Long entityTxnId = json.findValue("entityTxnId").asLong();
			Transaction transaction = Transaction.findById(entityTxnId);
			if (transaction == null) {
				log.log(Level.SEVERE, "Transaction not found: " + entityTxnId);
				return internalServerError();
			}
			sellTransactionService.getShippingAddress(user, transaction, result, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getAdditionalDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = getUserInfo(request);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start" + json);
			Long entityTxnId = json.findValue("entityTxnId").asLong();
			Transaction transaction = Transaction.findById(entityTxnId);
			if (transaction == null) {
				log.log(Level.SEVERE, "Transaction not found: " + entityTxnId);
				return internalServerError();
			}
			sellTransactionService.getAdditionalDetails(user, transaction, result, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result generateBomDocs(Request request) {
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		String reportType = null;
		String reportName = null;

		ObjectNode results = Json.newObject();
		ArrayNode an = results.putArray("invoiceFileName");
		Users user = null;
		File file = null;
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start" + json);
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized(results);
			}
			Long entityTxnId = json.findValue("entityTxnId").asLong();
			String exportType = json.findValue("exportType").asText();

			BillOfMaterialTxnModel transaction = genericDAO.getById(BillOfMaterialTxnModel.class, entityTxnId,
					entityManager);
			if (transaction == null) {
				log.log(Level.SEVERE, "Transaction not found: " + entityTxnId);
				return internalServerError();
			}
			reportName = "invoice";

			Long txnPurpose = transaction.getTransactionPurpose().getId();

			String orgName = user.getOrganization().getName() == null ? "" : user.getOrganization().getName().trim();
			orgName = orgName.replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "");
			if (orgName.indexOf("/") != -1) {
				orgName = orgName.replaceAll("/", "");
			}
			if (orgName.indexOf("\\") != -1) {
				orgName = orgName.replaceAll("\\\\", "");
			}
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
			Map<String, Object> params = null;
			ByteArrayOutputStream out = null;
			entityTransaction.begin();
			if (txnPurpose == IdosConstants.CREATE_PURCHASE_ORDER) {

				List<createPurchaseOrderModel> datasCreatePO = null;
				datasCreatePO = INVOICE_SERVICE.generateDataForCreatePO(user.getOrganization(), transaction,
						entityManager, json, null);
				params = INVOICE_SERVICE.getCreatePOParams(transaction, json, datasCreatePO, null, user);
				reportName = "createPO";
				out = dynReportService.getJasperPrintFromFileUsingJtable(reportName, datasCreatePO, null, params, type,
						request, application);

			}
			file = new File(path);
			if (file.exists()) {
				file.delete();
			}
			FileOutputStream fileOut = new FileOutputStream(path);
			out.writeTo(fileOut);
			fileOut.close();
			entityTransaction.commit();
			log.log(Level.FINE, ">>>> End");
			return Results.ok(file).withHeader("ContentType", "application/json").withHeader("Content-Disposition",
					"attachment; filename=" + fileName);
		} catch (NullPointerException ex) {
			log.log(Level.SEVERE, "Null Pointer Exception", ex);
		} catch (Exception ex) {
			if (entityTransaction.isActive())
				entityTransaction.rollback();
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return internalServerError(errorPage.render(ex, errorList));
		}
		return null;

	}
}
