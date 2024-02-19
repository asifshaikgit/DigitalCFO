package service;

import actor.CreatorActor;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.BillOfMaterialTxnModel;
import model.PurchaseOrderTxnModel;
import model.PurchaseRequisitionTxnModel;
import model.Transaction;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.WebSocket;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sunil K. Namdev created on 05.02.2019
 */
public class BillOfMaterialTxnServiceImpl implements BillOfMaterialTxnService {

	@Override
	public BillOfMaterialTxnModel submitForApproval(Users user, JsonNode json, EntityManager em, ObjectNode result)
			throws IDOSException {
		return BILL_OF_MATERIAL_TXN_DAO.submitForApproval(user, json, em, result);
	}

	@Override
	public BillOfMaterialTxnModel approverAction(Users user, EntityManager em, JsonNode json, ObjectNode results)
			throws IDOSException {
		return BILL_OF_MATERIAL_TXN_DAO.approverAction(user, em, json, results);
	}

	@Override
	public double getPurchaseOrderUnfulfilledUnits(Users user, EntityManager em, long entityid, long branchid)
			throws IDOSException {
		return BILL_OF_MATERIAL_TXN_DAO.getPurchaseOrderUnfulfilledUnits(user, em, entityid, branchid);
	}

	//@Override
	//public BillOfMaterialTxnModel submitForApprovalPo(Users user, JsonNode json, EntityManager em, ObjectNode result)
	//		throws IDOSException {
	//	String purchaseOrderCategoryId = json.findValue("purchaseOrderCategoryId") == null ? null
	//			: json.findValue("purchaseOrderCategoryId").asText();
	//	if ("npo".equals(purchaseOrderCategoryId)) {
	//		return CREATE_PURCHASE_ORDER_TXN_DAO.submitForApprovalPoNormal(user, json, em, result);
	//	} else {
	//		return CREATE_PURCHASE_ORDER_TXN_DAO.submitForApprovalPoAgainstRequisition(user, json, em, result);
	//	}
	//}

	@Override
	public BillOfMaterialTxnModel submitForApprovalPurchaseRequisition(Users user, JsonNode json, EntityManager em,
			ObjectNode result) throws IDOSException {
		BillOfMaterialTxnModel bom = null;
		return bom;
		//return CREATE_PURCHASE_REQUISITION_TXN_DAO.submitForApproval(user, json, em, result);
	}

	@Override
	public void setInvoiceQuotProfSerialForCreatePO(Users user, EntityManager entityManager, BillOfMaterialTxnModel txn)
			throws IDOSException {
		BILL_OF_MATERIAL_TXN_DAO.setInvoiceQuotProfSerialForCreatePO(user, entityManager, txn);

	}

	@Override
	public void setInvoiceQuotProfGstinSerialForCreatePO(Users user, EntityManager entityManager,
			BillOfMaterialTxnModel txn) throws IDOSException {
		BILL_OF_MATERIAL_TXN_DAO.setInvoiceQuotProfGstinSerialForCreatePO(user, entityManager, txn);

	}

	@Override
	public void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException {
		BILL_OF_MATERIAL_TXN_ITEM_DAO.getListOfTxnItems(user, em, txnId, txnItemsAn);
	}

	@Override
	public List<BillOfMaterialTxnModel> getPurchaseOrSalesOrderUnfulfillTxns(Users user, EntityManager em,
			Long branchid, Long vendorId, Long bomTxnType, ArrayNode bomTxnsArrNode) throws IDOSException {
		return BILL_OF_MATERIAL_TXN_DAO.getPurchaseOrSalesOrderUnfulfillTxns(user, em, branchid, vendorId, bomTxnType,
				bomTxnsArrNode);
	}
}
