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

import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import java.util.logging.Level;
import play.libs.Json;

import play.mvc.Result;
import play.mvc.Results;
import service.InvoiceService;
import service.InvoiceServiceImpl;
import service.ReceiptService;
import service.ReceiptServiceImpl;
import play.Application;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class ReceiptController extends StaticController {
	private static ReceiptService RECEIPT_SERVICE = new ReceiptServiceImpl();
	private static InvoiceService invService = new InvoiceServiceImpl();
	private final Application application;
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	// private Request request;
	// private Http.Session session = request.session();

	@Inject
	public ReceiptController(JPAApi jpaApi, Application application) {
		super(application);
		this.application = application;
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result generateReceipt(Request request) {

		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		ArrayNode an = results.putArray("receiptFileName");
		Users user = null;
		Vendor vendorUser = null;
		File file = null;
		try {
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
			JsonNode json = request.body().asJson();
			if (log.isLoggable(Level.FINE))
				log.log(Level.FINE, ">>>> Start " + json);
			// String email = json.findValue("email").asText();
			Transaction txn = null;
			String transactionId = json.findValue("entityTxnId") != null ? json.findValue("entityTxnId").asText()
					: null;
			String fileName = null;
			if (transactionId != null) {
				txn = Transaction.findById(Long.parseLong(transactionId));
				// Long timeInMillis=Calendar.getInstance().getTimeInMillis();
				String reportName = null;
				String orgName = user.getOrganization().getName().replaceAll("\\s", "");
				String type = "pdf";
				String path = application.path().toString() + "/logs/report/";
				File filepath = new File(path);
				if (!filepath.exists()) {
					filepath.mkdir();
				}
				ByteArrayOutputStream out = null;

				TransactionPurpose txnPur = txn.getTransactionPurpose();
				long txnId = txnPur.getId();

				if (txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER
						|| txn.getTransactionPurpose().getId() == IdosConstants.PAY_VENDOR_SUPPLIER
						|| txn.getTransactionPurpose().getId() == IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER) {
					fileName = orgName + "_receipt.pdf";
					List<ReceiptAdvanceModal> datas = null;

					if (txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER
							|| txn.getTransactionPurpose().getId() == IdosConstants.PAY_VENDOR_SUPPLIER) {
						reportName = "multipleInvoiceReceipt";
						datas = RECEIPT_SERVICE.generateMultiInvoiceData(results, json, user, entityManager,
								entitytransaction);
					} else {
						reportName = "CustomerPaymentReceipt";
						datas = RECEIPT_SERVICE.generateReceiptContent(results, json, user, entityManager,
								entitytransaction);
					}
					Map<String, Object> params = RECEIPT_SERVICE.getGstReceiptParams(results, json, user, entityManager,
							entitytransaction);
					out = dynReportService.getJasperPrintFromFileUsingJtable(reportName, datas, params, type, request,
							application);
				} else if ((user.getOrganization().getGstCountryCode() != null
						&& !"".equals(user.getOrganization().getGstCountryCode()))
						&& txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER) {
					invService.saveInvoiceLog(user, entityManager, txn, fileName, json);
					fileName = orgName + "_advreceiptgst.pdf";
					reportName = "advreceiptgst";
					List<InvoiceReportModel> datas = invService.generateGstInvoiceData(user.getOrganization(), txn,
							entityManager, json, null);
					List<InvoiceAdvanceModel> advList = null; // invService.getAdvTxnGstInvoice(user.getOrganization(),
																// txn, entityManager);
					Map<String, Object> params = invService.getGstInvoiceParams(txn, json, datas, null, user,
							entityManager);
					out = dynReportService.getJasperPrintFromFileUsingJtable(reportName, datas, advList, params, type,
							request, application);

				} else if ((user.getOrganization().getGstCountryCode() == null
						|| "".equals(user.getOrganization().getGstCountryCode()))
						&& txn.getTransactionPurpose().getId() == IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER) {
					invService.saveInvoiceLog(user, entityManager, txn, fileName, json);
					fileName = orgName + "_advancereceipt.pdf";
					reportName = "advancereceipt";
					List<ReceiptAdvanceModal> datas = RECEIPT_SERVICE.generateReceipt(results, json, user,
							entityManager, entitytransaction);
					out = dynReportService.generateStaticReport(reportName, datas, null, type, request);
				} else if ((user.getOrganization().getGstCountryCode() != null
						|| !"".equals(user.getOrganization().getGstCountryCode()))
						&& txn.getTransactionPurpose().getId() == IdosConstants.REFUND_ADVANCE_RECEIVED) {
					fileName = orgName + "receipt.pdf";
					reportName = "refundAdvReceived";
					List<InvoiceReportModel> datas = invService.generateGstInvoiceData(user.getOrganization(), txn,
							entityManager, json, null);
					List<InvoiceAdvanceModel> advList = null; // invService.getAdvTxnGstInvoice(user.getOrganization(),
																// txn, entityManager);
					Map<String, Object> params = invService.getGstInvoiceParams(txn, json, datas, null, user,
							entityManager);
					out = dynReportService.getJasperPrintFromFileUsingJtable(reportName, datas, advList, params, type,
							request, application);
				} else if (txn.getTransactionPurpose()
						.getId() == IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE) {
					fileName = orgName + "receipt.pdf";
					reportName = "refundPayment1";
					List<ReceiptAdvanceModal> datas = RECEIPT_SERVICE
							.generateRefundPaymentReceiptData(user.getOrganization(), txn, entityManager);
					List<InvoiceReportModel> dataList = null;
					Map<String, Object> params = invService.getGstInvoiceParams(txn, json, dataList, null, user,
							entityManager);
					// out = dynReportService.generateStaticReport(reportName, datas, params, type,
					// request);
					out = dynReportService.getJasperPrintFromFileUsingJtable(reportName, datas, null, params, type,
							request, application);
				}
				Long timeInMillis = Calendar.getInstance().getTimeInMillis();
				fileName = orgName + "_" + reportName + timeInMillis + ".pdf";
				path = path + fileName;
				file = new File(path);
				if (file.exists()) {
					file.delete();
				}
				FileOutputStream fileOut = new FileOutputStream(path);
				out.writeTo(fileOut);
				fileOut.close();
			}
			return Results.ok(file).withHeader("ContentType", "application/pdf").withHeader("Content-Disposition",
					"attachment; filename=" + fileName);
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return null;
	}
}
