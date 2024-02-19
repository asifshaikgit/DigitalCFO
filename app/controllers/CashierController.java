package controllers;

import java.util.*;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.BranchBankAccountBalance;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.BranchCashCountDenomination;
import model.ConfigParams;
import model.Users;
import model.UsersRoles;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http.Request;
// import play.mvc.Http.Session;
import com.idos.util.IdosUtil;
import play.Application;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Http;
import views.html.*;
import javax.inject.Inject;

public class CashierController extends StaticController {
	// private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;
	private Request request;
	// private Http.Session session = request.session();

	@Inject
	public CashierController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result getCashierInformation(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users usr = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("cashierInformationData");
			ArrayNode bnkan = result.putArray("branchBankAccountsData");
			ArrayNode appConan = result.putArray("approverControllerUserData");
			String email = json.findValue("useremail").asText();
			session.adding("email", email);
			usr = getUserInfo(request);
			String cashierKl = "";
			if (usr.getBranchSafeDepositBox() != null) {
				cashierKl = usr.getBranchSafeDepositBox().getCashierKnowledgeLibrary();
			}
			StringBuilder newsbquery = new StringBuilder(
					"select obj from BranchCashCount obj WHERE obj.branch.id=?1 AND obj.organization.id=?2 and obj.presentStatus=1 ORDER BY obj.date desc");
			ArrayList inparam = new ArrayList(2);
			inparam.add(usr.getBranch().getId());
			inparam.add(usr.getOrganization().getId());
			List<BranchCashCount> prevBranchCashCount = genericDAO.queryWithParams(newsbquery.toString(), entityManager,
					inparam);
			ObjectNode row = Json.newObject();
			row.put("cashierKl", cashierKl);
			if (prevBranchCashCount.size() > 0) {
				row.put("branchCashCountId", prevBranchCashCount.get(0).getId());
				row.put("cashCreditedTotal", prevBranchCashCount.get(0).getCreditAmount());
				row.put("cashDebitedTotal", prevBranchCashCount.get(0).getDebitAmount());
				row.put("resultantCashTotal", prevBranchCashCount.get(0).getResultantCash());
				row.put("notesTotal", prevBranchCashCount.get(0).getNotesTotal());
				row.put("coinsTotal", prevBranchCashCount.get(0).getCoinsTotal());
				row.put("smallerCoinsTotal", prevBranchCashCount.get(0).getSmallerCoinsTotal());
				row.put("grandTotal", prevBranchCashCount.get(0).getGrandTotal());
				row.put("mainToPettyTotal", prevBranchCashCount.get(0).getTotalMainCashToPettyCash());
				row.put("resultantPettyCash", prevBranchCashCount.get(0).getResultantPettyCash());
				row.put("debbitedPettyCash", prevBranchCashCount.get(0).getDebittedPettyCashAmount());
			} else {
				row.put("branchCashCountId", "");
				row.put("cashCreditedTotal", "");
				row.put("cashDebitedTotal", "");
				row.put("resultantCashTotal", "");
				row.put("notesTotal", "");
				row.put("coinsTotal", "");
				row.put("smallerCoinsTotal", "");
				row.put("grandTotal", "");
				row.put("mainToPettyTotal", "");
				row.put("resultantPettyCash", "");
				row.put("debbitedPettyCash", "");
			}
			an.add(row);
			StringBuilder sb = new StringBuilder(
					"select obj from UsersRoles obj WHERE (obj.role.id=4 or obj.role.id=6) AND obj.branch.id=?1 AND obj.organization.id=?2 and obj.presentStatus=1");
			inparam.clear();
			inparam.add(usr.getBranch().getId());
			inparam.add(usr.getOrganization().getId());
			List<UsersRoles> users = genericDAO.queryWithParams(sb.toString(), entityManager, inparam);
			for (UsersRoles userRoles : users) {
				ObjectNode userrows = Json.newObject();
				userrows.put("userID", userRoles.getUser().getId());
				userrows.put("userEmail", userRoles.getUser().getEmail());
				appConan.add(userrows);
			}
			List<BranchBankAccounts> branchBankAccounts = usr.getBranch().getBranchBankAccounts();
			for (BranchBankAccounts bnchBnkAccounts : branchBankAccounts) {
				ObjectNode rows = Json.newObject();
				rows.put("id", bnchBnkAccounts.getId());
				rows.put("name", bnchBnkAccounts.getBankName());
				StringBuilder newbnchbankactsbquery = new StringBuilder(
						"select obj from BranchBankAccountBalance obj WHERE obj.branch.id=?1 AND obj.organization.id=?2 and obj.branchBankAccounts.id=?3 and obj.presentStatus=1 ORDER BY obj.date desc");
				inparam.clear();
				inparam.add(usr.getBranch().getId());
				inparam.add(usr.getOrganization().getId());
				inparam.add(bnchBnkAccounts.getId());
				List<BranchBankAccountBalance> prevBranchBankBalance = genericDAO
						.queryWithParams(newbnchbankactsbquery.toString(), entityManager, inparam);
				if (prevBranchBankBalance.size() > 0) {
					BranchBankAccountBalance bnchBankActBal = prevBranchBankBalance.get(0);
					if (bnchBankActBal.getAmountBalance() != null) {
						rows.put("lastRecordedAmountEntered", bnchBankActBal.getAmountBalance());
					}
					if (bnchBankActBal.getAmountBalance() == null) {
						rows.put("lastRecordedAmountEntered", "0.0");
					}
					if (bnchBankActBal.getDebitAmount() != null) {
						rows.put("totalDebit", bnchBankActBal.getDebitAmount());
					}
					if (bnchBankActBal.getDebitAmount() == null) {
						rows.put("totalDebit", "0.0");
					}
					if (bnchBankActBal.getCreditAmount() != null) {
						rows.put("totalCredit", bnchBankActBal.getCreditAmount());
					}
					if (bnchBankActBal.getCreditAmount() == null) {
						rows.put("totalCredit", "0.0");
					}
					if (bnchBankActBal.getResultantCash() != null) {
						rows.put("resultantBalanceInAccount", bnchBankActBal.getResultantCash());
					}
					if (bnchBankActBal.getResultantCash() == null) {
						rows.put("resultantBalanceInAccount", "0.0");
					}
				} else {
					rows.put("lastRecordedAmountEntered", "0.0");
					rows.put("totalDebit", "0.0");
					rows.put("totalCredit", "0.0");
					rows.put("resultantBalanceInAccount", "0.0");
				}
				bnkan.add(rows);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result configCashCount(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users usr = null;
		Http.Session session = request.session();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			transaction.begin();
			BranchCashCount cashCountForTheDay = new BranchCashCount();
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("prevRecordedData");
			ArrayNode bnkan = result.putArray("branchBankAccountsData");
			String email = json.findValue("useremail").asText();
			String newNotesTotal = json.findValue("newNotesTotal").asText();
			String newCoinsTotal = json.findValue("newCoinsTotal").asText();
			String newGrandTotal = json.findValue("newGrandTotal").asText();
			String newSmallerCoinTotal = json.findValue("newSmallerCoinTotal") != null
					? json.findValue("newSmallerCoinTotal").asText()
					: null;
			String newNotesDenomination = json.findValue("newNotesDenomination").asText();
			String newNoteDenomination[] = newNotesDenomination.substring(0, (newNotesDenomination.length()))
					.split(",");
			String newNotesQuantity = json.findValue("newNotesQuantity").asText();
			String newNoteQuantity[] = newNotesQuantity.substring(0, (newNotesQuantity.length())).split(",");
			String newNoteTotal = json.findValue("newNoteTotal").asText();
			String newNoteResult[] = newNoteTotal.substring(0, (newNoteTotal.length())).split(",");
			String newCoinsDenomination = json.findValue("newCoinsDenomination").asText();
			String newCoinDenomination[] = newCoinsDenomination.substring(0, (newCoinsDenomination.length()))
					.split(",");
			String newCoinsQuantity = json.findValue("newCoinsQuantity").asText();
			String newCoinQuantity[] = newCoinsQuantity.substring(0, (newCoinsQuantity.length())).split(",");
			String newCoinTotal = json.findValue("newCoinTotal").asText();
			String newCoinResult[] = newCoinTotal.substring(0, (newCoinTotal.length())).split(",");
			session.adding("email", email);
			usr = getUserInfo(request);
			cashCountForTheDay.setDate(Calendar.getInstance().getTime());
			cashCountForTheDay.setNotesTotal(IdosUtil.convertStringToDouble(newNotesTotal));
			cashCountForTheDay.setCoinsTotal(IdosUtil.convertStringToDouble(newCoinsTotal));
			cashCountForTheDay.setGrandTotal(IdosUtil.convertStringToDouble(newGrandTotal));
			cashCountForTheDay.setDebitAmount(0.0);
			cashCountForTheDay.setCreditAmount(0.0);
			cashCountForTheDay.setResultantCash(IdosUtil.convertStringToDouble(newGrandTotal));
			if (!newSmallerCoinTotal.equals("") && newSmallerCoinTotal != null) {
				cashCountForTheDay.setSmallerCoinsTotal(IdosUtil.convertStringToDouble(newSmallerCoinTotal));
			}
			cashCountForTheDay.setBranch(usr.getBranch());
			cashCountForTheDay.setOrganization(usr.getOrganization());
			genericDAO.saveOrUpdate(cashCountForTheDay, usr, entityManager);
			auditDAO.saveAuditLogs("Added Branch cash count for the day", usr, cashCountForTheDay.getId(),
					BranchCashCount.class, ipAddress, json.toString(), entityManager);
			// for branch bank account logic
			String branchBankActId = json.findValue("branchBankActId").asText();
			String branchBankActIdArr[] = branchBankActId.substring(0, (branchBankActId.length())).split(",");
			String branchBankAccountBalance = json.findValue("branchBankActBalance").asText();
			String branchBankActBalance[] = branchBankAccountBalance.substring(0, (branchBankAccountBalance.length()))
					.split(",");
			String branchBankActBalanceStatement = json.findValue("branchBankActBalanceStatement").asText();
			String branchBankActBalStatement[] = branchBankActBalanceStatement
					.substring(0, (branchBankActBalanceStatement.length())).split(",");
			for (int i = 0; (i < branchBankActIdArr.length || i < branchBankActBalance.length
					|| i < branchBankActBalStatement.length); i++) {
				BranchBankAccounts bnchBankAct = BranchBankAccounts
						.findById(IdosUtil.convertStringToLong(branchBankActIdArr[i]));
				BranchBankAccountBalance bnchBankActBal = new BranchBankAccountBalance();
				bnchBankActBal.setAmountBalance(IdosUtil.convertStringToDouble(branchBankActBalance[i]));
				bnchBankActBal.setBranchBankAccounts(bnchBankAct);
				bnchBankActBal.setBranch(usr.getBranch());
				bnchBankActBal.setOrganization(usr.getOrganization());
				bnchBankActBal.setDate(Calendar.getInstance().getTime());
				bnchBankActBal.setResultantCash(IdosUtil.convertStringToDouble(branchBankActBalance[i]));
				if (branchBankActBalStatement.length > i) {
					if (!branchBankActBalStatement[i].trim().equals("")
							&& branchBankActBalStatement[i].trim() != null) {
						bnchBankActBal.setBalanceStatement(branchBankActBalStatement[i]);
					}
				}
				genericDAO.saveOrUpdate(bnchBankActBal, usr, entityManager);
				auditDAO.saveAuditLogs("Added Branch banck acclount balance for the day", usr, bnchBankActBal.getId(),
						BranchBankAccountBalance.class, ipAddress, json.toString(), entityManager);
			}
			// for notes denomination logic
			for (int i = 0; (i < newNoteDenomination.length || i < newNoteQuantity.length
					|| i < newNoteResult.length); i++) {
				BranchCashCountDenomination branchCashDen = new BranchCashCountDenomination();
				if (newNoteDenomination.length > i) {
					if (!newNoteDenomination[i].trim().equals("") && newNoteDenomination[i].trim() != null) {
						branchCashDen.setDenomination(IdosUtil.convertStringToInt(newNoteDenomination[i]));
					}
				}
				if (newNoteQuantity.length > i) {
					if (!newNoteQuantity[i].trim().equals("") && newNoteQuantity[i].trim() != null) {
						branchCashDen.setQuantity(IdosUtil.convertStringToInt(newNoteQuantity[i]));
					}
				}
				if (newNoteResult.length > i) {
					if (!newNoteResult[i].trim().equals("") && newNoteResult[i].trim() != null) {
						branchCashDen.setTotal(IdosUtil.convertStringToInt(newNoteResult[i]));
					}
				}
				branchCashDen.setDate(Calendar.getInstance().getTime());
				branchCashDen.setDenominationType(1);
				branchCashDen.setBranch(usr.getBranch());
				branchCashDen.setBranchCashCount(cashCountForTheDay);
				branchCashDen.setOrganization(usr.getOrganization());
				genericDAO.saveOrUpdate(branchCashDen, usr, entityManager);
				auditDAO.saveAuditLogs("Added Branch cash count notes denomination for the day", usr,
						branchCashDen.getId(), BranchCashCountDenomination.class, ipAddress, json.toString(),
						entityManager);
			}
			for (int i = 0; (i < newCoinDenomination.length || i < newCoinQuantity.length
					|| i < newCoinResult.length); i++) {
				BranchCashCountDenomination branchCashDen = new BranchCashCountDenomination();
				if (newCoinDenomination.length > i) {
					if (!newCoinDenomination[i].trim().equals("") && newCoinDenomination[i].trim() != null) {
						branchCashDen.setDenomination(IdosUtil.convertStringToInt(newCoinDenomination[i]));
					}
				}
				if (newCoinQuantity.length > i) {
					if (!newCoinQuantity[i].trim().equals("") && newCoinQuantity[i].trim() != null) {
						branchCashDen.setQuantity(IdosUtil.convertStringToInt(newCoinQuantity[i]));
					}
				}
				if (newCoinResult.length > i) {
					if (!newCoinResult[i].trim().equals("") && newCoinResult[i].trim() != null) {
						branchCashDen.setTotal(IdosUtil.convertStringToInt(newCoinResult[i]));
					}
				}
				branchCashDen.setDate(Calendar.getInstance().getTime());
				branchCashDen.setDenominationType(2);
				branchCashDen.setBranch(usr.getBranch());
				branchCashDen.setBranchCashCount(cashCountForTheDay);
				branchCashDen.setOrganization(usr.getOrganization());
				genericDAO.saveOrUpdate(branchCashDen, usr, entityManager);
				auditDAO.saveAuditLogs("Added Branch cash count coins denomination for the day", usr,
						branchCashDen.getId(), BranchCashCountDenomination.class, ipAddress, json.toString(),
						entityManager);
			}
			transaction.commit();
			ObjectNode row = Json.newObject();
			row.put("cashCreditedTotal", "0.0");
			row.put("cashDebitedTotal", "0.0");
			row.put("resultantCashTotal", newGrandTotal);
			row.put("notesTotal", newNotesTotal);
			row.put("coinsTotal", newCoinsTotal);
			row.put("smallerCoinsTotal", newSmallerCoinTotal);
			row.put("grandTotal", newGrandTotal);
			row.put("mainToPettyTotal", "0.0");
			row.put("resultantPettyCash", "0.0");
			row.put("debbitedPettyCash", "0.0");
			an.add(row);
			List<BranchBankAccounts> branchBankAccounts = usr.getBranch().getBranchBankAccounts();
			for (BranchBankAccounts bnchBnkAccounts : branchBankAccounts) {
				ObjectNode rows = Json.newObject();
				rows.put("id", bnchBnkAccounts.getId());
				rows.put("name", bnchBnkAccounts.getBankName());
				StringBuilder newbnchbankactsbquery = new StringBuilder("");
				newbnchbankactsbquery.append(
						"select obj from BranchBankAccountBalance obj WHERE obj.branch.id=?1 AND obj.organization.id=?2 and obj.branchBankAccounts.id=?3 and obj.presentStatus=1 ORDER BY obj.date desc");
				ArrayList inparam = new ArrayList(2);
				inparam.add(usr.getBranch().getId());
				inparam.add(usr.getOrganization().getId());
				inparam.add(bnchBnkAccounts.getId());
				List<BranchBankAccountBalance> prevBranchBankBalance = genericDAO
						.queryWithParams(newbnchbankactsbquery.toString(), entityManager, inparam);
				if (prevBranchBankBalance.size() > 0) {
					BranchBankAccountBalance bnchBankActBal = prevBranchBankBalance.get(0);
					rows.put("lastRecordedAmountEntered", bnchBankActBal.getAmountBalance());
					if (bnchBankActBal.getDebitAmount() != null) {
						rows.put("totalDebit", bnchBankActBal.getDebitAmount());
					}
					if (bnchBankActBal.getDebitAmount() == null) {
						rows.put("totalDebit", "0.0");
					}
					if (bnchBankActBal.getCreditAmount() != null) {
						rows.put("totalCredit", bnchBankActBal.getCreditAmount());
					}
					if (bnchBankActBal.getCreditAmount() == null) {
						rows.put("totalCredit", "0.0");
					}
					if (bnchBankActBal.getResultantCash() != null) {
						rows.put("resultantBalanceInAccount", bnchBankActBal.getResultantCash());
					}
					if (bnchBankActBal.getResultantCash() == null) {
						rows.put("resultantBalanceInAccount", "0.0");
					}
				} else {
					rows.put("lastRecordedAmountEntered", "0.0");
					rows.put("totalDebit", "0.0");
					rows.put("totalCredit", "0.0");
					rows.put("resultantBalanceInAccount", "0.0");
				}
				bnkan.add(rows);
			}
			// criterias.clear();
			// criterias.put("role.id", 6L);
			// criterias.put("branch.id", usr.getBranch().getId());
			// criterias.put("organization.id", usr.getOrganization().getId());
			// List<UsersRoles> userRoles=genericDAO.findByCriteria(UsersRoles.class,
			// criterias, entityManager);
			// String firstUserEmail="";
			// String approverEmails="";
			// Collection<InternetAddress> aCollection=new ArrayList<InternetAddress>();
			// for(UsersRoles usrRoles:userRoles){
			// firstUserEmail=usrRoles.getUser().getEmail();
			// aCollection.add(new InternetAddress(usrRoles.getUser().getEmail()));
			// }
			// if(!firstUserEmail.equals("") && aCollection.size()>0){
			// String body =
			// branchCashDenominationAlert.render(Double.valueOf(newNotesTotal),Double.valueOf(newCoinsTotal),Double.valueOf(newSmallerCoinTotal),Double.valueOf(newGrandTotal),usr.getBranch().getName(),usr.getOrganization().getName(),usr.getEmail()).body();
			// final String username =
			// Play.application().configuration().getString("smtp.user");
			// String subject="Branch CasH/Deposit Denomination Added";
			// Session session = emailsession;
			// mailTimerMultiple(body,username,session,firstUserEmail,aCollection,subject);
			// }
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result transferMainToPetty(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode pettytransferan = result.putArray("pettyCashTransferResultData");
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			String branchCashCountEntityId = json.findValue("branchCashCountEntityId").asText();
			Double resultantCash = 0.0;
			Double totalMainToPetty = 0.0;
			Double resultantPettyCash = 0.0;
			Double debbittedPettyCash = 0.0;
			if (branchCashCountEntityId != null && !branchCashCountEntityId.equals("")) {
				BranchCashCount branchCashCount = BranchCashCount.findById(Long.parseLong(branchCashCountEntityId));
				String resultantCashBranchCashAccount = json.findValue("resultantCashBranchCashAccount").asText();
				String cashMainActToPettyAccount = json.findValue("cashMainActToPettyAccount").asText();
				String cashTransferSuppDocs = json.findValue("cashTransferSuppDocs").asText();
				String approverUserEmail = json.findValue("approverUserEmail").asText();
				Boolean validationError = validateTxnService.validateMainCashToPettyTxn(user, transaction,
						branchCashCount, resultantCashBranchCashAccount, cashMainActToPettyAccount,
						cashTransferSuppDocs);
				if (!validationError) {
					if (branchCashCount.getResultantCash() != null) {
						resultantCash = branchCashCount.getResultantCash();
					}
					if (branchCashCount.getTotalMainCashToPettyCash() != null) {
						totalMainToPetty = branchCashCount.getTotalMainCashToPettyCash();
					}
					if (branchCashCount.getResultantPettyCash() != null) {
						resultantPettyCash = branchCashCount.getResultantPettyCash();
					}
					if (branchCashCount.getDebittedPettyCashAmount() != null) {
						debbittedPettyCash = branchCashCount.getDebittedPettyCashAmount();
					}
					ObjectNode row = Json.newObject();
					row.put("pettyCashTransferStat", "Success");
					row.put("resultantCash", resultantCash);
					row.put("totalMainToPetty", totalMainToPetty);
					row.put("resultantPettyCash", resultantPettyCash);
					row.put("debbittedPettyCash", debbittedPettyCash);
					pettytransferan.add(row);
					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.put("role.id", 6L);
					criterias.put("branch.id", user.getBranch().getId());
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("presentStatus", 1);
					List<UsersRoles> userRoles = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);
					String firstUserEmail = "";
					Collection<InternetAddress> aCollection = new ArrayList<InternetAddress>();
					for (UsersRoles usrRoles : userRoles) {
						firstUserEmail = usrRoles.getUser().getEmail();
						aCollection.add(new InternetAddress(usrRoles.getUser().getEmail()));
					}
					if (!firstUserEmail.equals("") && aCollection.size() > 0) {
						String body = branchMainToPettyCashTransferAlert
								.render(Double.valueOf(resultantCashBranchCashAccount),
										Double.valueOf(cashMainActToPettyAccount), user.getBranch().getName(),
										user.getOrganization().getName(), user.getEmail(), ConfigParams.getInstance())
								.body();
						final String username = ConfigFactory.load().getString("smtp.user");
						String subject = "Branch Main Cash To Petty Cash Transfer";
						Session mailsession = emailsession;
						mailTimerMultiple(body, username, mailsession, firstUserEmail, aCollection, subject);
					}
				}
				if (validationError) {
					ObjectNode row = Json.newObject();
					row.put("pettyCashTransferStat", "Failure");
					pettytransferan.add(row);
				}
			} else {
				ObjectNode row = Json.newObject();
				row.put("pettyCashTransferStat", "Failure");
				pettytransferan.add(row);
			}
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
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result recoincileCashAccount(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode cashaccountan = result.putArray("cashierInformationData");
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			BranchCashCount branchCashCount = cashierService.recoincileBranchCashAccount(user, transaction);
			String cashierKl = "";
			if (user.getBranchSafeDepositBox() != null) {
				cashierKl = user.getBranchSafeDepositBox().getCashierKnowledgeLibrary();
			}
			if (branchCashCount != null) {
				ObjectNode row = Json.newObject();
				row.put("cashierKl", cashierKl);
				row.put("branchCashCountId", branchCashCount.getId());
				row.put("cashCreditedTotal", branchCashCount.getCreditAmount());
				row.put("cashDebitedTotal", branchCashCount.getDebitAmount());
				row.put("resultantCashTotal", branchCashCount.getResultantCash());
				row.put("notesTotal", branchCashCount.getNotesTotal());
				row.put("coinsTotal", branchCashCount.getCoinsTotal());
				row.put("smallerCoinsTotal", branchCashCount.getSmallerCoinsTotal());
				row.put("grandTotal", branchCashCount.getGrandTotal());
				row.put("mainToPettyTotal", branchCashCount.getTotalMainCashToPettyCash());
				row.put("resultantPettyCash", branchCashCount.getResultantPettyCash());
				row.put("debbitedPettyCash", branchCashCount.getDebittedPettyCashAmount());
				cashaccountan.add(row);
			}
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
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result recoincileBankAccountBalance(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode bankaccountan = result.putArray("cashierBankInformationData");
		Users user = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("usermail").asText();
			String branchBankEntity = json.findValue("bnchBankActId").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			BranchBankAccounts branchBankAccount = BranchBankAccounts.findById(Long.parseLong(branchBankEntity));
			BranchBankAccountBalance branchBankAccountBalance = cashierService.recoincileBranchBankAccountBalance(user,
					branchBankAccount, transaction);
			if (branchBankAccountBalance != null) {
				ObjectNode rows = Json.newObject();
				rows.put("bankActId", branchBankAccountBalance.getBranchBankAccounts().getId());
				rows.put("lastRecordedAmountEntered", branchBankAccountBalance.getAmountBalance());
				rows.put("totalCredit", branchBankAccountBalance.getCreditAmount());
				rows.put("totalDebit", branchBankAccountBalance.getDebitAmount());
				rows.put("resultantBalanceInAccount", branchBankAccountBalance.getResultantCash());
				bankaccountan.add(rows);
			}
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
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}
}
