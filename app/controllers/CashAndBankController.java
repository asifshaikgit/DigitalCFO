package controllers;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IdosConstants;
import com.typesafe.config.Config;
import play.mvc.Http.Request;
import javax.transaction.Transactional;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.*;
import java.util.logging.Level;

import java.io.File;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import play.Application;
import javax.inject.Inject;

public class CashAndBankController extends StaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;
	private Request request;

	@Inject
	public CashAndBankController(JPAApi jpaApi, Application application) {
		super(application);
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
		this.application = application;
	}

	public CashAndBankController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
		this.application = application;
	}

	@Transactional
	public Result displayCashNBank(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			String bookType = json.findValue("bookType") != null ? json.findValue("bookType").asText() : null;
			if (null != email && !"".equals(email) && bookType != null) {
				user = getUserInfo(request);
				if ("1".equals(bookType) || "3".equals(bookType)) {
					result = cashNBankService.displayCashBook(result, json, user, entityManager);
				} else if ("2".equals(bookType)) {
					result = cashNBankService.displayBankBook(result, json, user, entityManager);
				}
			}
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result exportCashNBank(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		File file = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			String path = application.path().toString() + "/logs/report/";
			File filePath = new File(path);
			if (!filePath.exists()) {
				filePath.mkdir();
			}
			String fname = cashNBankService.exportCashNBank(result, json, user, entityManager, entitytransaction, path,
					application);
			if (fname != null) {
				file = new File(path + fname);
			}
			return Results.ok(file).withHeader("ContentType", "application/xlsx").withHeader("Content-Disposition",
					"attachment; filename=" + fname);
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			return null;
		}

	}

	@Transactional
	public Result getData(Request request, final String id, final String type) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = null;
		Users user = null;
		try {
			user = getUserInfo(request);
			result = cashNBankService.getData(id, type, user, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result generateBankReconciliation(Request request) {
		// EntityManager entityManager = getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode creditArrayNode = result.putArray("creditBRSData");
		ArrayNode debitArrayNode = result.putArray("debitBRSData");

		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			String transDetails = json.findValue("transactionList") == null ? null
					: json.findValue("transactionList").asText();
			String manualBankDate = json.findValue("manualBankDate") == null ? null
					: json.findValue("manualBankDate").asText();
			String creditValue = json.findValue("creditVal") == null ? null : json.findValue("creditVal").asText();
			String debitValue = json.findValue("debitVal") == null ? null : json.findValue("debitVal").asText();
			String balance = json.findValue("balance") == null || "".equals(json.findValue("balance")) ? "0.0"
					: json.findValue("balance").asText();
			String toDate = json.findValue("toDate") == null ? null : json.findValue("toDate").asText();

			String manualBankDateArray[] = manualBankDate.split(Pattern.quote("|"), -1);
			String creditValueArray[] = creditValue.split(Pattern.quote("|"), -1);
			String debitValueArray[] = debitValue.split(Pattern.quote("|"), -1);
			String transDetailArray[] = transDetails.split(Pattern.quote("|"), -1);
			Date searchToDate = idosdf.parse(toDate);
			Double creditTotalforBRS = 0.0;
			Double debitTotalforBRS = 0.0;
			boolean flagToAdd = true;
			for (int i = 0; i < manualBankDateArray.length; i++) {
				if (manualBankDateArray[i] != null && !"".equals(manualBankDateArray[i])) {
					String tmpManualDate = manualBankDateArray[i];
					Date manualDate = idosdf.parse(tmpManualDate);
					flagToAdd = manualDate.after(searchToDate);
				}
				if (flagToAdd) {
					if ((creditValueArray[i] != null && !"".equals(creditValueArray[i]))) {
						ObjectNode creditEvent = Json.newObject();
						Double tmpCredit = 0.0;
						if (creditValueArray[i] != null && !creditValueArray[i].equals(""))
							tmpCredit = Double.parseDouble(creditValueArray[i]);
						creditTotalforBRS += tmpCredit;
						creditEvent.put("transactionDetail", transDetailArray[i]);
						creditEvent.put("creditAmount", creditValueArray[i]);
						creditArrayNode.add(creditEvent);
					}
					if ((debitValueArray[i] != null && !"".equals(debitValueArray[i]))) {
						ObjectNode debitEvent = Json.newObject();
						Double tmpDebit = 0.0;
						if (debitValueArray[i] != null && !debitValueArray[i].equals(""))
							tmpDebit = Double.parseDouble(debitValueArray[i]);
						debitTotalforBRS += tmpDebit;
						debitEvent.put("transactionDetail", transDetailArray[i]);
						debitEvent.put("debitAmount", debitValueArray[i]);
						debitArrayNode.add(debitEvent);
					}
				}
				flagToAdd = true;
			}
			Double derivedBalance = (Double.parseDouble(balance) - debitTotalforBRS) + creditTotalforBRS;
			result.put("creditTotalforBRS", IdosConstants.decimalFormat.format(creditTotalforBRS));
			result.put("debitTotalforBRS", IdosConstants.decimalFormat.format(debitTotalforBRS));
			result.put("derivedBalance", IdosConstants.decimalFormat.format(derivedBalance));

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			user = getUserInfo(request);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result validateBankReconciliationDate(Request request) {
		// EntityManager entityManager = getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			String manualBankDate = json.findValue("manualBankDate") == null ? null
					: json.findValue("manualBankDate").asText();
			String instrumentDate = json.findValue("instrumentDate") == null ? null
					: json.findValue("instrumentDate").asText();
			String transactionDate = json.findValue("transactionDate") == null ? null
					: json.findValue("transactionDate").asText();
			Date manualDate = null;
			Date insrumentDt = null;
			Date transactionDt = null;
			if (instrumentDate != null && !"".equals(instrumentDate)) {
				insrumentDt = idosdf.parse(instrumentDate);
			}
			if (manualBankDate != null && !"".equals(manualBankDate)) {
				manualDate = idosdf.parse(manualBankDate);
			}
			if (manualDate != null && insrumentDt != null) {
				if (manualDate.compareTo(insrumentDt) >= 0) {
					result.put("isValidBankDate", true);
				} else {
					result.put("isValidBankDate", false);
				}
			} else if (manualDate == null && insrumentDt == null) {
				result.put("isValidBankDate", true);
			} else if (manualDate != null && insrumentDt == null) {
				if (transactionDate != null && !"".equals(transactionDate)) {
					transactionDt = idosdf.parse(transactionDate);
				}
				if (manualDate.compareTo(transactionDt) >= 0) {
					result.put("isValidBankDate", true);
				} else {
					result.put("isValidBankDate", false);
				}
			} else if (manualDate == null && insrumentDt != null) {
				result.put("isValidBankDate", true);
			} else {
				result.put("isValidBankDate", false);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			user = getUserInfo(request);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);

	}

	@Transactional
	public Result saveTransBankDate(Request request) {
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			entitytransaction.begin();
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			String transactionRefList = json.findValue("transactionRefList") == null ? null
					: json.findValue("transactionRefList").asText();
			String manualBankDate = json.findValue("manualBankDate") == null ? null
					: json.findValue("manualBankDate").asText();
			String transactionIdArray[] = transactionRefList.split(Pattern.quote("|"), -1);
			String manualBankDateArray[] = manualBankDate.split(Pattern.quote("|"), -1);
			boolean flagToAdd = true;
			for (int i = 0; i < transactionIdArray.length; i++) {
				if (transactionIdArray[i].startsWith("TXN")) {
					if (manualBankDateArray[i] != null && !"".equals(manualBankDateArray[i])) {
						transactionService.saveTransactionBRSDate(user, entityManager, entitytransaction,
								transactionIdArray[i], manualBankDateArray[i]);
					}
				} else if (transactionIdArray[i].startsWith("PROVTXN")) {
					if (manualBankDateArray[i] != null && !"".equals(manualBankDateArray[i])) {
						provisionJournalEntryService.saveProvisionJournalEntryBRSDate(user, entityManager,
								entitytransaction, transactionIdArray[i], manualBankDateArray[i]);
					}
				} else if (transactionIdArray[i].startsWith("CLAIMTXN")) {
					if (manualBankDateArray[i] != null && !"".equals(manualBankDateArray[i])) {
						claimsService.saveClaimTransactionBRSDate(user, entityManager, entitytransaction,
								transactionIdArray[i], manualBankDateArray[i]);
					}
				}
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			user = getUserInfo(request);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}
}