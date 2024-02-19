package service;

import com.idos.util.IDOSException;
import model.BillOfMaterialTxnModel;
import model.PurchaseRequisitionTxnModel;
import model.Transaction;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.List;

/**
 * @author Sunil K. Namdev created on 05.02.2019
 */
public interface BillOfMaterialTxnService extends BaseService {
        BillOfMaterialTxnModel submitForApproval(Users user, JsonNode json, EntityManager entityManager,
                        ObjectNode result)
                        throws IDOSException;

        BillOfMaterialTxnModel approverAction(Users user, EntityManager em, JsonNode json, ObjectNode results)
                        throws IDOSException;

        double getPurchaseOrderUnfulfilledUnits(Users user, EntityManager em, final long entityid, long branchid)
                        throws IDOSException;

        //BillOfMaterialTxnModel submitForApprovalPo(Users user, JsonNode json, EntityManager em, ObjectNode result)
        //                throws IDOSException;

        BillOfMaterialTxnModel submitForApprovalPurchaseRequisition(Users user, JsonNode json, EntityManager em,
                        ObjectNode result) throws IDOSException;

        void setInvoiceQuotProfSerialForCreatePO(Users user, EntityManager entityManager, BillOfMaterialTxnModel txn)
                        throws IDOSException;

        void setInvoiceQuotProfGstinSerialForCreatePO(Users user, EntityManager entityManager,
                        BillOfMaterialTxnModel txn)
                        throws IDOSException;

        void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException;

        List<BillOfMaterialTxnModel> getPurchaseOrSalesOrderUnfulfillTxns(Users user, EntityManager em, Long branchid,
                        Long vendorId, Long bomTxnType, ArrayNode bomTxnsArrNode) throws IDOSException;
}
