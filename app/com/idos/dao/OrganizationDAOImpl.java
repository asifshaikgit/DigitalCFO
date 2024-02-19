package com.idos.dao;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;

import model.Branch;
import model.Organization;
import model.OrganizationGstinSerials;
import model.Users;
import play.libs.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import play.db.jpa.JPAApi;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil Namdev on 30-01-2017.
 */
public class OrganizationDAOImpl implements OrganizationDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public boolean saveOrgSerialNumber(Users user, JsonNode json, EntityManager entityManager, ObjectNode result)
			throws IDOSException {
		Integer invoiceSerialNo = json.findValue("invoiceSerialNo") == null ? 1
				: json.findValue("invoiceSerialNo").asInt();
		Integer proformaSerialNo = json.findValue("proformaSerialNo") == null ? 1
				: json.findValue("proformaSerialNo").asInt();
		Integer quotationSerialNo = json.findValue("quotationSerialNo") == null ? 1
				: json.findValue("quotationSerialNo").asInt();
		Integer receiptSerialNo = json.findValue("receiptSerialNo") == null ? 1
				: json.findValue("receiptSerialNo").asInt();
		Integer advanceReceiptSerialNo = json.findValue("advanceReceiptSerialNo") == null ? 1
				: json.findValue("advanceReceiptSerialNo").asInt();
		Integer debitNoteCustSerialNo = json.findValue("debitNoteCustSerialNo") == null ? 1
				: json.findValue("debitNoteCustSerialNo").asInt();
		Integer creditNoteCustSerialNo = json.findValue("creditNoteCustSerialNo") == null ? 1
				: json.findValue("creditNoteCustSerialNo").asInt();
		Integer purchaseOrderSerialNo = json.findValue("purchaseOrderSerialNo") == null ? 1
				: json.findValue("purchaseOrderSerialNo").asInt();
		Integer refundAdvReceiptSerialNo = json.findValue("refundAdvReceiptSerialNo") == null ? 1
				: json.findValue("refundAdvReceiptSerialNo").asInt();
		Integer refundAmtAgainstInvoiceReceiptSerialNo = json
				.findValue("refundAmtAgainstInvoiceReceiptSerialNo") == null ? 1
						: json.findValue("refundAmtAgainstInvoiceReceiptSerialNo").asInt();
		Integer deliveryChallanReceiptSerialNo = json.findValue("deliveryChallanReceiptSerialNo") == null ? 1
				: json.findValue("deliveryChallanReceiptSerialNo").asInt();
		Integer paymentVoucherSerialNo = json.findValue("paymentVoucherSerialNo") == null ? 1
				: json.findValue("paymentVoucherSerialNo").asInt();
		Integer selfInvoiceSerialNo = json.findValue("selfInvoiceSerialNo") == null ? 1
				: json.findValue("selfInvoiceSerialNo").asInt();
		Integer createpurchaseOrderSerialNo = json.findValue("createpurchaseOrderSerialNo") == null ? 1
				: json.findValue("createpurchaseOrderSerialNo").asInt();
		Integer invoiceInterval = json.findValue("invoiceInterval") == null ? 1
				: json.findValue("invoiceInterval").asInt();
		Integer proformaInterval = json.findValue("proformaInterval") == null ? 1
				: json.findValue("proformaInterval").asInt();
		Integer receiptInterval = json.findValue("receiptInterval") == null ? 1
				: json.findValue("receiptInterval").asInt();
		Integer quotationInterval = json.findValue("quotationInterval") == null ? 1
				: json.findValue("quotationInterval").asInt();
		Integer advanceReceiptInterval = json.findValue("advanceReceiptInterval") == null ? 1
				: json.findValue("advanceReceiptInterval").asInt();
		Integer debitNoteCustInterval = json.findValue("debitNoteCustInterval") == null ? 1
				: json.findValue("debitNoteCustInterval").asInt();
		Integer creditNoteCustInterval = json.findValue("creditNoteCustInterval") == null ? 1
				: json.findValue("creditNoteCustInterval").asInt();
		Integer purchaseOrderInterval = json.findValue("purchaseOrderInterval") == null ? 1
				: json.findValue("purchaseOrderInterval").asInt();
		Integer refundAdvReceiptInterval = json.findValue("refundAdvReceiptInterval") == null ? 1
				: json.findValue("refundAdvReceiptInterval").asInt();
		Integer refundAmtAgainstInvoiceReceiptInterval = json
				.findValue("refundAmtAgainstInvoiceReceiptInterval") == null ? 1
						: json.findValue("refundAmtAgainstInvoiceReceiptInterval").asInt();
		Integer deliveryChallanReceiptInterval = json.findValue("deliveryChallanReceiptInterval") == null ? 1
				: json.findValue("deliveryChallanReceiptInterval").asInt();
		Integer paymentVoucherInterval = json.findValue("paymentVoucherInterval") == null ? 1
				: json.findValue("paymentVoucherInterval").asInt();
		Integer selfInvoieInterval = json.findValue("selfInvoieInterval") == null ? 1
				: json.findValue("selfInvoieInterval").asInt();
		Integer createpurchaseOrderInterval = json.findValue("createpurchaseOrderInterval") == null ? 1
				: json.findValue("createpurchaseOrderInterval").asInt();
		Integer serialNoCategory = json.findValue("serialNoCategory") == null ? 1
				: json.findValue("serialNoCategory").asInt();
		Organization org = user.getOrganization();
		org.setOrgSerialGenrationType(serialNoCategory);
		org.setInvoiceSerial(invoiceSerialNo - 1);
		org.setProformaSerial(proformaSerialNo - 1);
		org.setQuotationSerial(quotationSerialNo - 1);
		org.setReceiptSerial(receiptSerialNo - 1);
		org.setAdvanceReceiptSerial(advanceReceiptSerialNo - 1);
		org.setDebitNoteCustomerSerial(debitNoteCustSerialNo - 1);
		org.setCreditNoteCustomerSerial(creditNoteCustSerialNo - 1);
		org.setPurchaseOrderSerial(purchaseOrderSerialNo - 1);
		org.setRefundAdvanceReceiptSerial(refundAdvReceiptSerialNo - 1);
		org.setRefundAmounteReceiptSerial(refundAmtAgainstInvoiceReceiptSerialNo - 1);
		org.setDeliveryChallanReceiptSerial(deliveryChallanReceiptSerialNo - 1);
		org.setPaymentVoucherSerial(paymentVoucherSerialNo - 1);
		org.setSelfInvoice(selfInvoiceSerialNo - 1);
		org.setCreatePurchaseOrderSerial(createpurchaseOrderSerialNo - 1);
		org.setInvoiceInterval(invoiceInterval);
		org.setProformaInterval(proformaInterval);
		org.setQuotationInterval(quotationInterval);
		org.setReceiptInterval(receiptInterval);
		org.setAdvanceReceiptInterval(advanceReceiptInterval);
		org.setDebitNoteCustomerInterval(debitNoteCustInterval);
		org.setCreditNoteCustomerInterval(creditNoteCustInterval);
		org.setPurchaseOrderInterval(purchaseOrderInterval);
		org.setRefundAdvanceReceiptInterval(refundAdvReceiptInterval);
		org.setRefundAmountReceiptInterval(refundAmtAgainstInvoiceReceiptInterval);
		org.setDeliverChallanReceiptInterval(deliveryChallanReceiptInterval);
		org.setPaymentVoucherInterval(paymentVoucherInterval);
		org.setSelfInvoiceInterval(selfInvoiceSerialNo);
		org.setCreatePurchaseOrderInterval(createpurchaseOrderInterval);
		genericDao.saveOrUpdate(org, user, entityManager);
		return true;
	}

	@Override
	public boolean savePlaceOfSupplyType(Users user, JsonNode json, EntityManager entityManager) throws IDOSException {
		Integer type = json.findValue("placeOfSupplyType") == null ? 0 : json.findValue("placeOfSupplyType").asInt();
		Organization org = user.getOrganization();
		org.setPlaceOfSupplyType(type);
		genericDao.saveOrUpdate(org, user, entityManager);
		return true;
	}

	@Override
	public boolean saveOrgGstinSerialNumber(Users user, JsonNode json, EntityManager entityManager, ObjectNode result)
			throws IDOSException, JSONException {
		String salesInvoiceGstSerials = json.findValue("salesInvoiceGstSerials").toString();
		String proformaGstSerials = json.findValue("proformaGstSerials").toString();
		String quotationGstSerials = json.findValue("quotationGstSerials").toString();
		String receiptGstSerials = json.findValue("receiptGstSerials").toString();
		String advanceReceiptGstSerials = json.findValue("advanceReceiptGstSerials").toString();
		String debitNoteToCustGstSerials = json.findValue("debitNoteToCustGstSerials").toString();
		String creditNoteToCustGstSerials = json.findValue("creditNoteToCustGstSerials").toString();
		String purchaseOrderGstSerials = json.findValue("purchaseOrderGstSerials").toString();
		String refundAdvGstSerials = json.findValue("refundAdvGstSerials").toString();
		String refundAmtAgainstInvoiceSerials = json.findValue("refundAmtAgainstInvoiceSerials").toString();
		String paymentVoucherSerials = json.findValue("paymentVoucherSerials").toString();
		String gstSelfInvoiceSerials = json.findValue("gstSelfInvoiceSerials").toString();
		String createPurchaseOrderSerials = json.findValue("createPurchaseOrderSerials").toString();
		Integer serialNoCategory = json.findValue("serialNoCategory") == null ? 1
				: json.findValue("serialNoCategory").asInt();
		Integer gstInAllInterval = json.findValue("gstInAllInterval") == null ? 1
				: json.findValue("gstInAllInterval").asInt();
		Organization org = user.getOrganization();
		org.setGstInInterval(gstInAllInterval);
		org.setOrgSerialGenrationType(serialNoCategory);
		genericDao.saveOrUpdate(org, user, entityManager);

		saveGstinSerialsByOrg(user, new JSONArray(salesInvoiceGstSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(proformaGstSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(quotationGstSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(receiptGstSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(advanceReceiptGstSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(debitNoteToCustGstSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(creditNoteToCustGstSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(purchaseOrderGstSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(refundAdvGstSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(refundAmtAgainstInvoiceSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(paymentVoucherSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(gstSelfInvoiceSerials), entityManager);
		saveGstinSerialsByOrg(user, new JSONArray(createPurchaseOrderSerials), entityManager);
		return false;
	}

	@Override
	public ObjectNode getOrgGstinSerialNumber(Users user, EntityManager entityManager) throws IDOSException {
		// TODO Auto-generated method stub

		ObjectNode result = Json.newObject();
		ArrayNode salesInvoiceGstSerials = result.putArray("salesInvoiceGstSerials");
		ArrayNode proformaGstSerials = result.putArray("proformaGstSerials");
		ArrayNode quotationGstSerials = result.putArray("quotationGstSerials");
		ArrayNode receiptGstSerials = result.putArray("receiptGstSerials");
		ArrayNode advanceReceiptGstSerials = result.putArray("advanceReceiptGstSerials");
		ArrayNode debitNoteToCustGstSerials = result.putArray("debitNoteToCustGstSerials");
		ArrayNode creditNoteToCustGstSerials = result.putArray("creditNoteToCustGstSerials");
		ArrayNode purchaseOrderGstSerials = result.putArray("purchaseOrderGstSerials");
		ArrayNode refundAdvGstSerials = result.putArray("refundAdvGstSerials");
		ArrayNode refundAmtAgainstInvoiceSerials = result.putArray("refundAmtAgainstInvoiceSerials");
		ArrayNode paymentVoucherSerials = result.putArray("paymentVoucherSerials");
		ArrayNode gstSelfInvoiceSerials = result.putArray("gstSelfInvoiceSerials");
		ArrayNode createPurchaseOrderSerials = result.putArray("gstCreatePurchaseOrderSerials");

		if (user.getOrganization().getGstInInterval() != null) {
			result.put("gstInInterval", user.getOrganization().getGstInInterval());
		} else {
			result.put("gstInInterval", 1);
		}
		Long orgId = user.getOrganization().getId();
		List<String> findListOfGST = Branch.findListOfGSTIN(entityManager, orgId);
		if (findListOfGST != null) {
			for (String gstIn : findListOfGST) {
				// sales Invoice
				salesInvoiceGstSerials
						.add(getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_SALES_INVOICE, gstIn,
								entityManager));
				proformaGstSerials
						.add(getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_PROFORMA, gstIn, entityManager));
				quotationGstSerials.add(
						getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_QUOTATION, gstIn, entityManager));
				receiptGstSerials
						.add(getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_RECEIPT, gstIn, entityManager));
				advanceReceiptGstSerials
						.add(getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_ADVANCE_RECEIPT, gstIn,
								entityManager));
				debitNoteToCustGstSerials
						.add(getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_DEBIT_NOTE_TO_CUST, gstIn,
								entityManager));
				creditNoteToCustGstSerials
						.add(getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_CREDIT_NOTE_TO_CUST, gstIn,
								entityManager));
				purchaseOrderGstSerials
						.add(getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_PURCHASE_ORDER, gstIn,
								entityManager));
				refundAdvGstSerials
						.add(getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_REFUND_ADVANCE_RECEIVED, gstIn,
								entityManager));
				refundAmtAgainstInvoiceSerials.add(getNodeForGStAndOrg(orgId,
						IdosConstants.GSTIN_SERIAL_FOR_REFUND_AMOUNT_AGAINST_INVOICE_RECEIVED, gstIn, entityManager));
				paymentVoucherSerials
						.add(getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_PAYMENT_VOUCHER, gstIn,
								entityManager));
				gstSelfInvoiceSerials
						.add(getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_SELF_INVOICE, gstIn,
								entityManager));
				createPurchaseOrderSerials
						.add(getNodeForGStAndOrg(orgId, IdosConstants.GSTIN_SERIAL_FOR_CREATE_PURCHASE_ORDER, gstIn,
								entityManager));
			}
		}
		return result;
	}

	private ObjectNode getNodeForGStAndOrg(Long orgId, Integer catId, String gstIn, EntityManager entityManager) {
		ObjectNode row = Json.newObject();
		OrganizationGstinSerials obj = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId, catId,
				gstIn);
		if (obj != null) {
			row.put("id", obj.getId());
			row.put("gstIn", obj.getGstIn());
			if (obj.getSerialNo() != null) {
				row.put("serialNo", (obj.getSerialNo() + 1));
			} else {
				row.put("serialNo", "1");
			}
			row.put("catId", obj.getDocumentCategoryNo());
		} else {
			row.put("id", "");
			row.put("gstIn", gstIn);
			row.put("serialNo", "1");
			row.put("catId", catId);
		}
		return row;
	}

	private Boolean saveGstinSerialsByOrg(Users user, JSONArray arrJSON, EntityManager entityManager)
			throws JSONException, IDOSException {

		for (int i = 0; i < arrJSON.length(); i++) {
			JSONObject itemRow = new JSONObject(arrJSON.get(i).toString());
			String id = itemRow.getString("id") == "" ? "" : itemRow.getString("id");
			String gstIn = itemRow.getString("gstIn") == "" ? "" : itemRow.getString("gstIn");
			String serialNo = itemRow.getString("serialNo") == "" ? "" : itemRow.getString("serialNo");
			String catId = itemRow.getString("catId") == "" ? "" : itemRow.getString("catId");
			OrganizationGstinSerials obj = new OrganizationGstinSerials();
			if (!id.equals("")) {
				obj.setId(IdosUtil.convertStringToLong(id));
			}
			if (!catId.equals("")) {
				obj.setDocumentCategoryNo(IdosUtil.convertStringToInt(catId));
			}
			if (serialNo.equals("")) {
				serialNo = "1";
			}
			obj.setSerialNo((IdosUtil.convertStringToInt(serialNo) - 1));
			obj.setGstIn(gstIn);
			obj.setOrganization(user.getOrganization());
			genericDao.saveOrUpdate(obj, user, entityManager);
		}

		return true;
	}

	@Override
	public boolean saveTdsApplicableTrans(Users user, JsonNode json, EntityManager entityManager)
			throws IDOSException, JSONException {
		// TODO Auto-generated method stub
		String tdsAppliedTrans = json.findValues("tdsTrans") == null ? "" : json.findValue("tdsTrans").asText();
		Organization org = user.getOrganization();
		if (org != null) {
			org.setTdsApplicableTransactions(tdsAppliedTrans);
			genericDao.saveOrUpdate(org, user, entityManager);
			return true;
		}
		return false;
	}
}
