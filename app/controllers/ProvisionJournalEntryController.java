package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import actor.CreatorActor;
import com.idos.util.IdosUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import java.util.logging.Level;
import model.Branch;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.BranchDepositBoxKey;
import model.BranchTaxes;
import model.IdosProvisionJournalEntry;
import model.Project;
import model.ProvisionJournalEntryDetail;
import model.Specifics;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import play.db.jpa.JPAApi;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import pojo.ProvisionJournalEntryDetailPojo;
import javax.inject.Inject;
import play.Application;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class ProvisionJournalEntryController extends StaticController {
	public static Application application;
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	// private Request request;
	// private Http.Session session = request.session();
	@Inject
	public ProvisionJournalEntryController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result provisionApproverAction(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("useremail").asText();
			String transactionProvisionPrimId = json.findValue("transactionPrimId").asText();
			IdosProvisionJournalEntry provisionTxn = IdosProvisionJournalEntry
					.findById(IdosUtil.convertStringToLong(transactionProvisionPrimId));
			if (null != email && !"".equals(email)) {
				// session.adding("email", email);
				user = getUserInfo(request);
				result.put("useremail", user.getEmail());
				String userRolesAsString = "";
				List<UsersRoles> userRolesList = UsersRoles.getUserRoleList(entityManager,
						user.getOrganization().getId(),
						user.getId(),
						user.getBranch().getId());
				for (UsersRoles userRole : userRolesList) {
					userRolesAsString += userRole.getRole().getName() + ",";
				}
				userRolesAsString = userRolesAsString.substring(0, userRolesAsString.length() - 1);
				result.put("role", userRolesAsString);
				int returnVal = provisionJournalEntryService.provisionApproverAction(result, json, user, entityManager,
						entitytransaction, provisionTxn);
				if (returnVal != -1) { // Failed
					provisionJournalEntryService.sendProvisionWebSocketResponse(provisionTxn, user, entityManager,
							result);
				}
				if (provisionTxn != null) {
					result.put(TRANSACTION_ID, provisionTxn.getId());
					result.put(TRANSACTION_REF_NO, provisionTxn.getTransactionRefNumber());
				}
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			reportException(entityManager, entitytransaction, user, ex, result);
		} catch (Throwable th) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			reportThrowable(entityManager, entitytransaction, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getListOfPjeItemsDetails(Request request) {
		ObjectNode results = Json.newObject();
		// EntityManager em=getEntityManager();
		Users user = getUserInfo(request);
		try {
			JsonNode json = request.body().asJson();

			Long pjeId = json.findValue("transactionEntityId").asLong();

			if (pjeId != null) {
				IdosProvisionJournalEntry pjeTxn = IdosProvisionJournalEntry.findById(pjeId);

				StringBuilder debitItems = new StringBuilder();
				StringBuilder creditItems = new StringBuilder();
				StringBuilder itemParentName = new StringBuilder();
				List<ProvisionJournalEntryDetailPojo> pjeItemDetailList = provisionJournalEntryService
						.getProvisionJournalEntryDetail(entityManager, pjeTxn, itemParentName, creditItems, debitItems);

				if (pjeTxn != null) {
					results.put("debitBranch", pjeTxn.getDebitBranch().getName());
					results.put("creditBranch", pjeTxn.getCreditBranch().getName());
				}
				ObjectMapper oMapper = new ObjectMapper();
				final JsonNode pjeItemDetailListJson = oMapper.convertValue(pjeItemDetailList, JsonNode.class);
				results.put("PjeItemDetailList", pjeItemDetailListJson);

				Double debitTotalAmount = 0.0;
				Double creditTotalAmount = 0.0;
				for (ProvisionJournalEntryDetailPojo pjeItemDetail : pjeItemDetailList) {
					if (pjeItemDetail.getIsDebit() == 1) {
						if (pjeItemDetail.getHeadAmount() != null)
							debitTotalAmount += pjeItemDetail.getHeadAmount();
					} else if (pjeItemDetail.getIsDebit() == 0) {
						if (pjeItemDetail.getHeadAmount() != null)
							creditTotalAmount += pjeItemDetail.getHeadAmount();
					}

				}
				results.put("debitTotalAmount", IdosConstants.decimalFormat.format(debitTotalAmount));
				results.put("creditTotalAmount", IdosConstants.decimalFormat.format(creditTotalAmount));
				if (log.isLoggable(Level.FINE))
					log.log(Level.FINE, ">>>> End " + results);
			}
		} catch (Exception ex) {
			// log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(results);
	}

}
