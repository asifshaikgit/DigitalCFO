package controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Specifics;
import model.Transaction;
import model.TransactionItems;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.*;

import com.idos.util.IdosUtil;
import java.util.logging.Level;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import javax.inject.Inject;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class KaizalaWebhookController extends BaseController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	// private Request request;
	// private Http.Session session = request.session();

	@Inject
	public KaizalaWebhookController() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result getPOFromKaizala(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		JsonNode json = request.body().asJson();
		// {"objectId":"com.microsoft.kaizala.miniapps.GSTTest","objectType":"ActionPackage","eventType":"ActionResponse","eventId":"5b8d1360-1282-407a-b8df-a0ec7708e2a2","data":{"actionId":"4d8c810a-1444-4037-bd8d-fe39bc922548","actionPackageId":"com.microsoft.kaizala.miniapps.GSTTest","packageId":"com.microsoft.kaizala.miniapps.GSTTest.2","groupId":"bf56024a-55ee-440d-ba03-b3a36ac48f1f","responseId":"5b8d1360-1282-407a-b8df-a0ec7708e2a2","responder":"+918123678066","responseDetails":{"responseWithQuestions":[{"title":"PO
		// Number","type":"Text","options":[],"answer":"purchase order
		// #5620"},{"title":"Product
		// List","type":"Text","options":[],"answer":"[\"{\\\"PN\\\":\\\"Buy
		// 1\\\",\\\"PQ\\\":\\\"1.0\\\",\\\"PPPU\\\":\\\"143.0\\\",\\\"PF\\\":\\\"851680\\\",\\\"PU\\\":\\\"E.g.
		// Carton\\\"}\"]"},{"title":"Vendor
		// Name","type":"Text","options":[]},{"title":"Vendor
		// Id","type":"Text","options":[]},{"title":"Shipping
		// Address","type":"Text","options":[]},{"title":"Shipping
		// State","type":"Text","options":[]},{"title":"PO
		// Approval","type":"SingleOption","options":[{"title":"Invoice
		// Raised"},{"title":"PO Reject"},{"title":"PO Accept"},{"title":"ERP Upload
		// Failure"},{"title":"ERP Upload Success"}],"answer":["PO
		// Accept"]},{"title":"Buyer ID","type":"Text","options":[]},{"title":"Buyer
		// Name","type":"Text","options":[]}]}}}
		ObjectNode results = Json.newObject();

		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users user = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {

			String jsonBody = json.toString(); // request.body().asText();

			JSONObject obj1 = new JSONObject(jsonBody);
			if (!(obj1.has("data")))
				return Results.ok(results);

			boolean _poAccepted = POAccepted(jsonBody);
			JSONObject resObj = new JSONObject(jsonBody);
			JSONObject responseDetails = resObj.getJSONObject("data").getJSONObject("responseDetails");
			JSONArray arr = responseDetails
					.getJSONArray("responseWithQuestions");
			// To get the prouctList
			String poTransactionId = arr.getJSONObject(0).getString("answer");

			String actionId = resObj.getJSONObject("data").getString("actionId");

			Transaction transaction = Transaction.findById(IdosUtil.convertStringToLong(poTransactionId));
			transaction.setProcurementStatus(actionId);

			entitytransaction.begin();
			if (_poAccepted == false) {
				transaction.setPerformaInvoice(false);
				return Results.ok(results);
			} else {
				String prodList = arr.getJSONObject(1).getString("answer");
				JSONArray prodListObj = new JSONArray(prodList);
				double totalNet = 0.0;

				for (int i = 0; i < prodListObj.length(); i++) {
					JSONObject obj;
					if (prodListObj.get(i).getClass().equals(String.class))
						obj = new JSONObject((String) prodListObj.get(i));
					else
						obj = prodListObj.getJSONObject(i);
					// JSONObject obj = new JSONObject(prodListObj.get(i));
					// Product prod = new
					// Product(obj.getString("PU"),obj.getString("PN"),Integer.parseInt(obj.getString("PQ")),Integer.parseInt(obj.getString("PPPU")),Integer.parseInt(obj.getString("PF")));
					// productList.add(prod);

					Map<String, Object> criterias = new HashMap<String, Object>();
					String poTransactionSpecific = obj.getString("PN");
					String poNoOfUnits = obj.getString("PQ");
					String poPricePerUnit = obj.getString("PPPU");

					criterias.put("presentStatus", 1);
					criterias.put("organization.id", transaction.getTransactionBranchOrganization().getId());
					criterias.put("name", poTransactionSpecific);
					criterias.put("presentStatus", 1);
					List<Specifics> itemSpecifics = genericDAO.findByCriteria(
							Specifics.class, criterias, "id", true,
							entityManager);
					if (itemSpecifics != null && itemSpecifics.size() > 0) {
						Specifics itemSpec = itemSpecifics.get(0);
						criterias.clear();
						criterias.put("transaction.id", IdosUtil.convertStringToLong(poTransactionId));
						criterias.put("transactionSpecifics.id", itemSpec.getId());
						criterias.put("presentStatus", 1);
						List<TransactionItems> listTransactionItems = genericDAO.findByCriteria(TransactionItems.class,
								criterias, entityManager);
						if (listTransactionItems != null
								&& listTransactionItems.size() > 0) {
							TransactionItems txnItemRow = listTransactionItems
									.get(0);
							double orgNoOfUnits = txnItemRow.getNoOfUnits();
							double orgPricePerUnit = txnItemRow
									.getPricePerUnit();
							double orgGrossAmt = txnItemRow.getGrossAmount();
							txnItemRow.setNoOfUnitsReturned(orgNoOfUnits);
							txnItemRow.setPricePerUnitReturned(orgPricePerUnit);
							txnItemRow.setGrossAmounReturned(orgGrossAmt);
							double poGross = IdosUtil.convertStringToDouble(poNoOfUnits)
									* IdosUtil.convertStringToDouble(poPricePerUnit);
							txnItemRow.setNoOfUnits(IdosUtil.convertStringToDouble(poNoOfUnits));
							txnItemRow.setPricePerUnit(IdosUtil.convertStringToDouble(poPricePerUnit));
							txnItemRow.setGrossAmount(poGross);
							txnItemRow.setNetAmount(poGross);
							totalNet = totalNet + poGross;
							genericDAO.saveOrUpdate(txnItemRow, user,
									entityManager);
						}
					}
				}
				transaction.setAdvanceType(1); // PO has been approved by Kaizala
				transaction.setNetAmount(totalNet);
				genericDAO.saveOrUpdate(transaction, user, entityManager);
				entitytransaction.commit();
			}

		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			ex.printStackTrace();
			// log.log(Level.SEVERE, ex.getMessage());
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user
					.getOrganization().getName(),
					Thread.currentThread()
							.getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
		}
		log.log(Level.FINE, ">>>> End ");
		return Results.ok(results);
	}

	static boolean POAccepted(String response) {
		JSONObject obj;
		try {
			obj = new JSONObject(response);
			JSONObject responseDetails = obj.getJSONObject("data")
					.getJSONObject("responseDetails");
			JSONArray arr = responseDetails
					.getJSONArray("responseWithQuestions");
			JSONArray answer = arr.getJSONObject(6).getJSONArray("answer");
			return (("PO Accept").equals((String) answer.get(0)));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

}
