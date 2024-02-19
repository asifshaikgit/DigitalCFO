package com.idos.dao;

import com.idos.util.IDOSException;
import model.BillOfMaterialTxnModel;
import model.Branch;
import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import service.FileUploadService;
import service.FileUploadServiceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.Date;
import java.util.List;

public interface BillOfMaterialTxnDAO extends BaseDAO {
        BillOfMaterialTxnModel submitForApproval(Users user, JsonNode json, EntityManager entityManager,
                        ObjectNode result)
                        throws IDOSException;

        void sendWebSocketResponse4Bom(BillOfMaterialTxnModel bomTxn, Users user, ObjectNode result);

        BillOfMaterialTxnModel approverAction(Users user, EntityManager em, JsonNode json, ObjectNode results)
                        throws IDOSException;

        double getPurchaseOrderUnfulfilledUnits(Users user, EntityManager em, final long entityid, long branchid)
                        throws IDOSException;

        double insertBillOfMaterialTxnItems(EntityManager em, Users user, JSONArray arrJSON,
                        BillOfMaterialTxnModel bomTxn,
                        Date txnDate, boolean isNew) throws IDOSException;

        void setApproverEmailDetails(Users user, EntityManager em, BillOfMaterialTxnModel bomTxn, Branch branch,
                        Specifics item);

        void setInvoiceQuotProfSerialForCreatePO(Users user, EntityManager entityManager, BillOfMaterialTxnModel txn)
                        throws IDOSException;

        void setInvoiceQuotProfGstinSerialForCreatePO(Users user, EntityManager entityManager,
                        BillOfMaterialTxnModel txn)
                        throws IDOSException;

        // default String olInvoiceStr() {
        // Config config = ConfigFactory.load();
        // return ConfigFactory.load().getString("offline.invoice.prefix");
        // }
        // String olInvoiceStr =
        // ConfigFactory.load().getString("offline.invoice.prefix");
        List<BillOfMaterialTxnModel> getPurchaseOrSalesOrderUnfulfillTxns(Users user, EntityManager em, Long branchid,
                        Long vendorId, Long bomTxnType, ArrayNode bomTxnsArrNode) throws IDOSException;

        String UNFUL_TXN_JPQL = "from BillOfMaterialTxnModel obj WHERE obj.organization.id= ?1 and obj.branch.id= ?2 and obj.transactionPurpose.id= ?3 and obj.transactionStatus ='Accounted' and (obj.isFulfilled != 1 or obj.isFulfilled is null)";
}
