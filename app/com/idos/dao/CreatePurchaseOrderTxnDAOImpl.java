package com.idos.dao;

import com.idos.util.CodeHelper;
import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;

import actor.CreatorActor;
import controllers.BaseController;

import com.idos.util.IdosDaoConstants;
import com.idos.util.UploadUtil;

import model.*;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;
import service.BuyTransactionService;
import service.BuyTransactionServiceImpl;
import service.PurchaseOrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.logging.Level;
import java.util.*;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;


import static service.BaseService.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import service.EntityManagerProvider;

/**
 * @author Sunil Namdev created on 2.3.2019
 */
public class CreatePurchaseOrderTxnDAOImpl implements CreatePurchaseOrderTxnDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    private BuyTransactionService buyTransactionService;

    @Override
    public PurchaseOrderTxnModel submitForApproval(Users user, JsonNode json, EntityManager em, ObjectNode result) throws IDOSException {
        PurchaseOrderTxnModel bomTxn = null;
        try {
            Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l : json.findValue("txnEntityID").asLong();
            Long txnforBranch = (json.findValue("txnForBranch") == null || "".equals(json.findValue("txnForBranch"))) ? 0l : json.findValue("txnForBranch").asLong();
            Long txnforProject = (json.findValue("txnForProject") == null || "".equals(json.findValue("txnForProject"))) ? 0l : json.findValue("txnForProject").asLong();
            Date txnDate = new Date();
            long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
            String txnRemarks = json.findValue("txnRemarks").asText();
            String supportingdoc = json.findValue("supportingdoc").asText();
            String txnForItemStr = json.findValue("txnForItem").toString();
            Double prTxnId = json.findValue("prTxnId").asDouble();
            Long txnParentPurchaseOrder = json.findValue("txnParentPurchaseOrder").asLong();
            JSONArray arrJSON = new JSONArray(txnForItemStr);
            Branch txnBranch = null;
            Vendor vendor = null;
            Project txnProject = null;
            TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);
            if (txnEntityID > 0) {
                bomTxn = PurchaseOrderTxnModel.findById(txnEntityID);
            }else {
                bomTxn = new PurchaseOrderTxnModel();
            }
            if (txnforBranch != null && txnforBranch > 0) {
                txnBranch = Branch.findById(txnforBranch);
            }else{
                throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF,IdosConstants.BUSINESS_EXCEPTION, "Branch not found " + txnforBranch, IdosConstants.NULL_KEY_EXC_ESMF_MSG);
            }
            if (txnforProject != null && txnforProject > 0) {
                txnProject = Project.findById(txnforProject);
            }
            //Enter data for first item in bomTxn table to be displayed in Transaction list
            bomTxn.setTransactionPurpose(usertxnPurpose);
            bomTxn.setBranch(txnBranch);
            bomTxn.setTypeIdentifier(IdosConstants.PURCHASE_ORDER_NORMAL); //check how to set normal and bom type
            bomTxn.setOrganization(txnBranch.getOrganization());
            bomTxn.setProject(txnProject);
            bomTxn.setActionDate(txnDate);
            bomTxn.setParentPurchaseOrder(txnParentPurchaseOrder);
            bomTxn.setPurchaseRequisitionId(prTxnId);
            if (txnRemarks != null && !txnRemarks.equals("")) {
                txnRemarks = user.getEmail() + "#" + txnRemarks;
                bomTxn.setRemarks(txnRemarks);
                txnRemarks = bomTxn.getRemarks();
            }
            bomTxn.setTransactionStatus(IdosConstants.TXN_STATUS_REQUIRE_APPROVAL);

            String refNo = CodeHelper.getForeverUniqueID("PO", null);
            bomTxn.setTransactionRefNumber(refNo);
            bomTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(bomTxn.getSupportingDocs(), user.getEmail(), supportingdoc, user, em));
            genericDao.saveOrUpdate(bomTxn, user, em);
            FILE_UPLOAD_DAO.updateUploadFileLogs(em, user, supportingdoc, bomTxn.getId(), IdosConstants.BOM_TXN_TYPE);
            Specifics expenseItem = null;
            if(txnEntityID > 0)
                expenseItem = addUpdateCreatePOTxnItems(em, user, arrJSON, bomTxn, txnDate, false);
            else
                expenseItem = addUpdateCreatePOTxnItems(em, user, arrJSON, bomTxn, txnDate, true);
                CREATE_PURCHASE_ORDER_TXN_DAO.setApproverEmailDetails( user,  em, bomTxn, txnBranch, expenseItem);
            genericDao.saveOrUpdate(bomTxn, user, em);
            
            CREATE_PURCHASE_ORDER_TXN_DAO.sendWebSocketResponse4Bom(bomTxn, user, result);
            if(arrJSON.length() > 1) {
                //creating multiple purchase orders if there are multiple vendors rows
                createMultiplePOWithSingleParentPO(user, em, bomTxn, result);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION, "Error on Create Purchase Order for approval", ex.getMessage());
        }
        return bomTxn;
    }

    private Specifics addUpdateCreatePOTxnItems(EntityManager em, Users user, JSONArray arrJSON, PurchaseOrderTxnModel bomTxn, Date txnDate, boolean isNew) throws IDOSException {
        double totalUnits = 0.0, grossTotal = 0.0;
        Specifics txnItem = null;
        try {
            ArrayList<Long> selectedItemTxns = null;
            if(!isNew){
                selectedItemTxns = new ArrayList<>();
            }
            for(int i=0; i<arrJSON.length(); i++){
                JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                PurchaseOrderTxnItemModel bomItem = null;
                if(isNew) {
                    bomItem = new PurchaseOrderTxnItemModel();
                }else{
                    Long bomItemTxnId = rowItemData.get("bomItemTxnId") == null || "".equals(rowItemData.getString("bomItemTxnId")) ? null : rowItemData.getLong("bomItemTxnId");
                    bomItem = PurchaseOrderTxnItemModel.findById(bomItemTxnId);
                    selectedItemTxns.add(bomItemTxnId);
                }
                Long itemId = rowItemData.get("itemId") == null || "".equals(rowItemData.getString("itemId")) ? null : rowItemData.getLong("itemId");
                Double txnNoOfUnit = rowItemData.get("txnNoOfUnit")== null || "".equals(rowItemData.getString("txnNoOfUnit")) ? null : rowItemData.getDouble("txnNoOfUnit");
                if(txnNoOfUnit == null){
                    txnNoOfUnit = 0.0;
                }
                Long txnVendor = rowItemData.get("txnVendor")== null || "".equals(rowItemData.getString("txnVendor")) ? null : rowItemData.getLong("txnVendor");

                Vendor vendor = null;
                if (txnVendor != null && txnVendor > 0) {
                    vendor = Vendor.findById(txnVendor);
                }
                if(vendor == null){
                    throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF,IdosConstants.BUSINESS_EXCEPTION, "Vendor not found " + txnVendor,IdosConstants.NULL_KEY_EXC_ESMF_MSG);
                }
                String txnPlaceOfSupply = rowItemData.getString("txnPlaceOfSupply");
                String txnMeasureName = rowItemData.getString("txnMeasureName");
                String txnOem = rowItemData.getString("txnOem");
                String txnTypeOfMaterial = rowItemData.getString("txnTypeOfMaterial");
                txnItem = Specifics.findById(itemId);
                bomItem.setOrganization(bomTxn.getOrganization());
                bomItem.setNoOfUnits(txnNoOfUnit);
                bomItem.setVendor(vendor);
                bomItem.setPlaceOfSupply(txnPlaceOfSupply);
                bomItem.setMeasureName(txnMeasureName);
                bomItem.setOem(txnOem);
                bomItem.setTypeOfMaterial(txnTypeOfMaterial);
                if(bomTxn.getTypeIdentifier() == IdosConstants.PURCHASE_ORDER_NORMAL) {
                    String txnExpDelDate = rowItemData.get("txnExpDelDate")== null || "".equals(rowItemData.getString("txnExpDelDate")) ? null : rowItemData.getString("txnExpDelDate");
                    Date expectedDeliveryDate = null;
                    if(txnExpDelDate != null){
                        expectedDeliveryDate = IdosConstants.IDOSDF.parse(txnExpDelDate);
                    }
                    bomItem.setExpectedDatetime(expectedDeliveryDate);
                    
                }else{
                    Double txnTotalPrice = rowItemData.get("txnTotalPrice")== null || "".equals(rowItemData.getString("txnTotalPrice")) ? null : rowItemData.getDouble("txnTotalPrice");
                    if(txnTotalPrice == null){
                        txnTotalPrice = 0.0;
                    }
                    grossTotal += txnTotalPrice;
                }
                
                bomItem.setPurchaseOrderTxn(bomTxn);
                bomItem.setExpense(txnItem);
                genericDao.saveOrUpdate(bomItem, user, em);
                totalUnits += txnNoOfUnit;

            }
            if(selectedItemTxns != null) {
                String query = "delete from PurchaseOrderTxnItemModel t1 where t1.organization.id= ?1 and t1.purchaseOrderTxn.id not in (?2)";
                genericDao.deleteByParamName(query, em, selectedItemTxns);
            }
            bomTxn.setTotalAmount(grossTotal);
            bomTxn.setTotalNetAmount(grossTotal);
            bomTxn.setTotalNoOfUnits(totalUnits);
        }catch(Exception ex){
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION, "Error on save/update multiitems.", ex.getMessage());
        }
        return txnItem;
    }

    @Override
    public void sendWebSocketResponse4Bom(PurchaseOrderTxnModel bomTxn, Users user, ObjectNode result) {
        try {
            /*Map<String, WebSocket.Out<JsonNode>> orgtxnregistereduser = new HashMap<String, WebSocket.Out<JsonNode>>();
            Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
            for (int i = 0; i < keyArray.length; i++) {
                Users orgusers = Users.findActiveByEmail(String.valueOf(keyArray[i]));
                if (orgusers != null && orgusers.getOrganization().getId() == user.getOrganization().getId()) {
                    orgtxnregistereduser.put(keyArray[i].toString(), CreatorActor.expenseregistrered.get(keyArray[i]));
                }
            }*/

            String branchName = bomTxn.getBranch() == null ? "" : bomTxn.getBranch().getName();
            String projectName = bomTxn.getProject() == null ? "" : bomTxn.getProject().getName();
            //String itemName = bomTxn.getIncome() == null ? "" : bomTxn.getIncome().getName();
            String itemParentName = "";
            String budgetAllocated = "";
            String budgetAvailable ="";

            //String customerVendorName = bomTxn.getCustomerVendor() == null ? "" : bomTxn.getCustomerVendor().getName();
            String invoiceDate = "";
            String invoiceDateLabel = "";
            String paymentMode = "";

            Double noOfUnits = bomTxn.getTotalNoOfUnits() == null ? 0d : bomTxn.getTotalNoOfUnits();
            Double perUnitPrice = 0d;
            Double grossAmt = bomTxn.getTotalAmount() == null ? 0d : bomTxn.getTotalAmount();
            Double netAmount = bomTxn.getTotalNetAmount() == null ? 0d : bomTxn.getTotalNetAmount();
            String netAmtDesc = "";
            String outstandings = "";
            String createdBy = bomTxn.getCreatedBy().getEmail() == null ? "" : bomTxn.getCreatedBy().getEmail();
            String approverLabel = "";
            String approverEmail = "";
            if (bomTxn.getApproverActionBy() != null) {
                approverLabel = "APPROVER";
                approverEmail = bomTxn.getApproverActionBy().getEmail();
            }
            String approverEmails = bomTxn.getApproverEmails();
            String additionalApprovalEmails = "";
            if (bomTxn.getAdditionalApproverEmails() != null) {
                additionalApprovalEmails = bomTxn.getAdditionalApproverEmails();
            }
            String selectedAdditionalApproval = "";
            if (bomTxn.getSelectedAdditionalApprover() != null) {
                selectedAdditionalApproval = bomTxn.getSelectedAdditionalApprover();
            }
            String txnDocument = bomTxn.getSupportingDocs() == null ? "" : bomTxn.getSupportingDocs();
            String txnRemarks = bomTxn.getRemarks() == null ? "" : bomTxn.getRemarks();
            String debitCredit = "";

            String txnSpecialStatus = "";
            Double frieghtCharges = 0.0;
            String poReference = bomTxn.getDocumentRef() == null ? "" : bomTxn.getDocumentRef();
            String txnInstrumentNumber = "";
            String txnInstrumentDate = "";
            String txnRemarksPrivate = bomTxn.getPrivateRemarks() == null ? "" : bomTxn.getPrivateRemarks();
            String invoiceNumber ="";
            String txnPurpose = bomTxn.getTransactionPurpose().getTransactionPurpose();
            if(bomTxn.getTransactionPurpose().getId() == IdosConstants.CREATE_PURCHASE_ORDER && bomTxn.getTypeIdentifier() != null && bomTxn.getTypeIdentifier() == IdosConstants.PURCHASE_ORDER_AGAINST_REQUISITION){
                txnPurpose += " (Against Bill Of Material)";
            }
            TransactionViewResponse.addActionTxn(bomTxn.getId(), branchName, projectName, "", itemParentName, budgetAllocated, "", budgetAvailable, "", "", txnPurpose, IdosConstants.idosdf.format(bomTxn.getActionDate()), invoiceDateLabel, invoiceDate, paymentMode, noOfUnits, perUnitPrice, grossAmt, netAmount, netAmtDesc, outstandings, bomTxn.getTransactionStatus(), createdBy, approverLabel, approverEmail, txnDocument, txnRemarks, debitCredit, approverEmails, additionalApprovalEmails, selectedAdditionalApproval, txnSpecialStatus, frieghtCharges, poReference, txnInstrumentNumber, txnInstrumentDate, bomTxn.getTransactionPurpose().getId(), txnRemarksPrivate, invoiceNumber,0,bomTxn.getTransactionRefNumber(),0l,0.0,0, 0, result);
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
        }
    }

    @Override
    public void setApproverEmailDetails(Users user, EntityManager em, PurchaseOrderTxnModel bomTxn, Branch branch, Specifics item) {
        Map<String, Object> criterias = new HashMap<String, Object>(3);
        criterias.put("role.name", "APPROVER");
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);

        String approverEmails = "";
        String additionalApprovarUsers = "";
        for (UsersRoles usrRoles : approverRole) {
            additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
            criterias.clear();
            criterias.put("user.id", usrRoles.getUser().getId());
            criterias.put("userRights.id", 2L);
            criterias.put("branch.id", branch.getId());
            criterias.put("presentStatus", 1);
            UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class, criterias, em);
            if (userHasRightInBranch != null) {
                //check for right in chart of accounts
                criterias.clear();
                criterias.put("user.id", usrRoles.getUser().getId());
                criterias.put("userRights.id", 2L);
                criterias.put("specifics.id", item.getId());
                criterias.put("presentStatus", 1);
                UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class, criterias, em);
                if (userHasRightInCOA != null) {
                    approverEmails += usrRoles.getUser().getEmail() + ",";
                }
            }
        }
        if(approverEmails.equals("")){
            bomTxn.setApproverEmails(additionalApprovarUsers);
        }else {
            bomTxn.setApproverEmails(approverEmails);
        }
        bomTxn.setAdditionalApproverEmails(additionalApprovarUsers);
    }

    public PurchaseOrderTxnModel approverAction(Users user, EntityManager em, JsonNode json, ObjectNode result) throws IDOSException {
        PurchaseOrderTxnModel txn = null;
        try {
            Boolean isSingleUserDeploy = ConfigParams.getInstance().isDeploymentSingleUser(user);
            int selectedApproverAction = json.findValue("selectedApproverAction").asInt();
            Long transactionPrimId = json.findValue("transactionPrimId").asLong();
            String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
            String txnRmarks = json.findValue("txnRmarks").asText() != null ? json.findValue("txnRmarks").asText() : null;
            txn = PurchaseOrderTxnModel.findById(transactionPrimId);
            txn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(txn.getSupportingDocs(), user.getEmail(), suppDoc, user, em));
            if (txnRmarks != null && !txnRmarks.equals("")) {
                if (txn.getRemarks() != null) {
                    txn.setRemarks(txn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
                } else {
                    txn.setRemarks(user.getEmail() + "#" + txnRmarks);
                }
            }
            txn.setModifiedBy(user);

            if(!IdosConstants.TXN_STATUS_APPROVED.equals(txn.getTransactionStatus())){
                if (selectedApproverAction == 1) {
                    //approved action perform transaction operation
                    txn.setTransactionStatus(IdosConstants.TXN_STATUS_APPROVED);
                    txn.setApproverActionBy(user);
                }
            }
            if (selectedApproverAction == 5) {
                txn.setTransactionStatus(IdosConstants.TXN_STATUS_REQUIRE_CLARIFICATION);
            }
            if (selectedApproverAction == 6) {
                txn.setTransactionStatus(IdosConstants.TXN_STATUS_CLARIFIED);
            }
            /*
            if (selectedApproverAction == 7) {

            }
            if (selectedApproverAction == 8) {

            }*/
            if (!IdosConstants.TXN_STATUS_REJECTED.equals(txn.getTransactionStatus())) {
                if (selectedApproverAction == 2) {
                    //reject action
                    txn.setTransactionStatus("Rejected");
                    txn.setApproverActionBy(user);
                }
            }
            if (selectedApproverAction == 3) {
                //additional approval action
                txn.setTransactionStatus("Require Additional Approval");
                txn.setApproverActionBy(user);
                String selectedAddApproverEmail = json.findValue("selectedAddApproverEmail").asText();
                txn.setSelectedAdditionalApprover(selectedAddApproverEmail);
            }
            if (selectedApproverAction == 4) {
                if (!IdosConstants.TXN_STATUS_ACCOUNTED.equals(txn.getTransactionStatus())) {
                    txn.setActionDate(new Date());
                    txn.setTransactionStatus(IdosConstants.TXN_STATUS_ACCOUNTED);
                }
            }
            //BILL_OF_MATERIAL_TXN_DAO.setInvoiceQuotProfSerialForCreatePO(user, em, txn);
            genericDao.saveOrUpdate(txn, user, em);
            FILE_UPLOAD_SERVICE.updateUploadFileLogs(em, user, suppDoc, txn.getId(), IdosConstants.PR_TXN_TYPE);
            /*Map<String, WebSocket.Out<JsonNode>> orgtxnregistereduser = new HashMap<String, WebSocket.Out<JsonNode>>();
            Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
            for (int i = 0; i < keyArray.length; i++) {
                StringBuilder sbquery = new StringBuilder("");
                sbquery.append("select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and obj.presentStatus=1");
                List<Users> orgusers = genericDao.executeSimpleQuery(sbquery.toString(), em);
                if (orgusers != null && !orgusers.isEmpty() && orgusers.get(0).getOrganization().getId() == user.getOrganization().getId()) {
                    orgtxnregistereduser.put(keyArray[i].toString(), CreatorActor.expenseregistrered.get(keyArray[i]));
                }
            }*/

            String branchName = "";
            String projectName = "";
            String itemName = "";
            String budgetAllocated = "";
            String budgetAvailable = "";
            String budgetAllocatedAmt = "";
            String budgetAvailableAmt = "";
            String customerVendorName = "";
            String txnDate = "";
            String paymentMode = "";
            Double noOfUnit = 0d;
            Double unitPrice = 0.0;
            Double grossAmount = 0.0;
            Double netAmount = 0.0;
            String netAmountDesc = "";
            String status = "";
            String createdBy = "";
            String txnDocument = "";
            String txnRemarks = "";
            String approverEmails = "";
            String additionalapproverEmails = "";
            String selectedAdditionalApproval = "";
            Double frieghtCharges = 0.0;
            String poReference = "";
            if (txn.getBranch() != null) {
                branchName = txn.getBranch().getName();
            }
            if (txn.getProject() != null) {
                projectName = txn.getProject().getName();
            }
            if (txn.getTotalNoOfUnits() != null) {
                noOfUnit = txn.getTotalNoOfUnits();
            }
            if (txn.getTotalAmount() != null) {
                grossAmount = txn.getTotalAmount();
            }

            if (txn.getDocumentRef() != null) {
                poReference = txn.getDocumentRef();
            }
            txnDate = IdosConstants.idosdf.format(txn.getActionDate());
            if (txn.getTotalNetAmount() != null) {
                netAmount = txn.getTotalNetAmount();
            }
            status = txn.getTransactionStatus();
            createdBy = txn.getCreatedBy().getEmail();
            if (txn.getSupportingDocs() != null) {
                txnDocument = txn.getSupportingDocs();
            }
            if (txn.getRemarks() != null) {
                txnRemarks = txn.getRemarks();
            }
            approverEmails = txn.getApproverEmails();
            additionalapproverEmails = txn.getAdditionalApproverEmails();
            if (txn.getSelectedAdditionalApprover() != null) {
                selectedAdditionalApproval = txn.getSelectedAdditionalApprover();
            }
            String invDate = "";
            String invoiceDateLabel = "";
            String itemParentName = "";
            String approverEmail = "";
            String approverLabel = "";
            if (txn.getApproverActionBy() != null) {
                approverLabel = "APPROVER:";
                approverEmail = txn.getApproverActionBy().getEmail();
            }
            String txnSpecialStatus = "";

            String invoiceNumber = txn.getInvoiceNumber() == null ? "" : txn.getInvoiceNumber();
            String txnPurpose = txn.getTransactionPurpose().getTransactionPurpose();
            int txnIdentifier = 0;

            TransactionViewResponse.addActionTxn(txn.getId(), branchName, projectName, itemName, itemParentName, budgetAllocated, budgetAllocatedAmt, budgetAvailable, budgetAvailableAmt, customerVendorName, txnPurpose, txnDate, invoiceDateLabel, invDate, paymentMode, noOfUnit, unitPrice, grossAmount, netAmount, netAmountDesc, "", status, createdBy, approverLabel, approverEmail, txnDocument, txnRemarks, "", approverEmails, additionalapproverEmails, selectedAdditionalApproval, txnSpecialStatus, frieghtCharges, poReference, "", "", txn.getTransactionPurpose().getId(), "", invoiceNumber, txnIdentifier, txn.getTransactionRefNumber(),0l,0.0,0, 0, result);

            
        }catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION, "Error on Purchase Order for user action", ex.getMessage());
        }
        log.log(Level.FINE, ">>>> End ");
        return txn;
    }
    
    
    public void createMultiplePOWithSingleParentPO(Users user, EntityManager em, PurchaseOrderTxnModel parentPO, ObjectNode result) throws IDOSException {
        try {
            List<PurchaseOrderTxnItemModel> itemList =CREATE_PURCHASE_ORDER_TXN_ITEM_DAO.getAllItemsOfPO(user, em, parentPO.getId());

            if(itemList.size() > 1) {
                parentPO.setIsParent(1);
                genericDao.saveOrUpdate(parentPO, user, em);
                
                //creating purchase order only if a parent purchase order contain multiple item lines
                for (PurchaseOrderTxnItemModel txnItem : itemList) {
                    log.log(Level.FINE, "parent PO : " + parentPO.getId());
                    log.log(Level.FINE, "PO Item :" + txnItem.getId());
                    ArrayNode singleUserAccounting = result.putArray("singleUserAccounting");
                    ObjectNode row = Json.newObject();
                    row.put("txnEntityID", "");
                    row.put("txnPurpose", "npo");
                    row.put("txnPurposeVal", "41"); //put this from constant
                    row.put("txnForBranch", parentPO.getBranch().getId().toString());
                    row.put("txnForProject", parentPO.getProject().getId().toString());
                    row.put("txnRemarks", "");
                    row.put("supportingdoc", "");
                    row.put("prTxnId", "");
                    row.put("txnParentPurchaseOrder", parentPO.getId()); //set parent po id
                    row.put("txnDocumentUploadRequired", "false");
                    row.put("useremail", user.getEmail()); //set current user email

                    ArrayNode txnItemsList = row.putArray("txnForItem");

                    ObjectNode item = Json.newObject();
                    item.put("itemId", txnItem.getId().toString());
                    item.put("txnNoOfUnit", txnItem.getNoOfUnits().toString());
                    item.put("txnMeasureName", txnItem.getMeasureName());
                    item.put("txnVendor", txnItem.getVendor().getId().toString());
                    item.put("txnPlaceOfSupply", txnItem.getPlaceOfSupply() != null ? txnItem.getPlaceOfSupply() : "");
                    item.put("txnOem", txnItem.getOem() != null ? txnItem.getOem() : "");
                    SimpleDateFormat outputFormatter = new SimpleDateFormat("MMMM d, yyyy");
                    item.put("txnExpDelDate", outputFormatter.format(txnItem.getExpectedDatetime()));
                    item.put("txnTypeOfMaterial", txnItem.getTypeOfMaterial() != null ? txnItem.getTypeOfMaterial() : "");
                    txnItemsList.add(item);
                    log.log(Level.FINE, "PO Create request:- "+ row.toString());
                    PurchaseOrderTxnModel newTxn = submitForApproval(user, row, em, result);
                    log.log(Level.FINE, "New Sub PO create with id :"+newTxn.getId());
                    createBuyOnCreditTxnAgainstPO(user, em, newTxn, result);
                }
            } else {
                parentPO.setIsParent(0);
                genericDao.saveOrUpdate(parentPO, user, em);
                createBuyOnCreditTxnAgainstPO(user, em, parentPO, result);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error on Multiple Purchase Order creation -> "+ ex.getMessage());
            //throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION, "Error on Multiple Purchase Order creation ", ex.getMessage());
        } 
    }

    public void createBuyOnCreditTxnAgainstPO(Users user, EntityManager em, PurchaseOrderTxnModel po, ObjectNode result) {
        try {
            log.log(Level.FINE, "Creating BUY ON CREDIT against PO transaction " + po.getTransactionRefNumber());
            
            EntityTransaction entitytransaction = null;
            ArrayNode singleUserAccounting = result.putArray("singleUserAccounting");
            ObjectNode row = Json.newObject();
            List<PurchaseOrderTxnItemModel> itemList =CREATE_PURCHASE_ORDER_TXN_ITEM_DAO.getAllItemsOfPO(user, em, po.getId());
            Vendor vendor = null;
            ArrayNode txnItemsList = row.putArray("txnForItem");
            for (PurchaseOrderTxnItemModel txnItem : itemList) {
                vendor = txnItem.getVendor();
                ObjectNode item = Json.newObject();
                item.put("txnItems", txnItem.getId().toString());
                item.put("txnPerUnitPrice", "0");
                item.put("txnNoOfUnit", txnItem.getNoOfUnits());
                item.put("txnGross", "0");
                item.put("txnTaxDesc", "");
                item.put("txnTaxAmount", "");
                item.put("txnTaxRate", "");
                item.put("txnTaxName", "");
                item.put("txnTaxID", "");
                item.put("txnTaxFormulaId", "");
                item.put("txnRcmTaxItemID", "");
                item.put("txnDutiesAndTaxesAmount", "");
                item.put("withholdingAmount", "0");
                item.put("customerAdvance", "0.00");
                item.put("txnLeftOutWithholdTransIDs", "");
                item.put("howMuchAdvance", "0");
                item.put("netAmountVal", "0");
                item.put("txnTaxOnAdvAdj", "");
                item.put("txnTaxNameOnAdvAdj", "");
                item.put("actualbudgetDisplayVal", "");
                item.put("budgetDisplayVal", "0");
                item.put("amountRangeLimitRuleVal", "0");
                item.put("klmandatoryfollowednotfollowed", "0");
                txnItemsList.add(item);
            }
            row.put("txnEntityID", "");
            row.put("txnPurpose", "Buy on credit & pay later");
            //row.put("txnPurposeVal", IdosConstants.BUY_ON_CREDIT_PAY_LATER);
            row.put("txnForBranch", po.getBranch().getId());
            row.put("txnForProject", po.getProject().getId());
            row.put("txnPOInvoice", po.getId());
            row.put("txnPOInvoiceTxnRef", po.getTransactionRefNumber());
            
            row.put("txnForCustomer", vendor.getId()); //put vendor from first item list
            row.put("txnForUnavailableCustomer", "");
            row.put("txnTotalNetAmount", "0"); //calculate total amount from items
            row.put("totalTxnNetAmtWithDecimalValue", "0"); //calculate total amount from items without decimal
            row.put("totalWithholdTaxAmt", "");
            row.put("totalTxnTaxAmt", "0");
            row.put("totalTxnGrossAmt", ""); //put total amount of a line
            row.put("klfollowednotfollowed", ""); //not sure what is it, when i create transaction manully the no was 1
            row.put("txnRemarks", "");
            row.put("txnprocrem", "");
            row.put("supportingdoc", "");
            row.put("txnDocumentUploadRequired", "false");
            row.put("txnSourceGstin", po.getBranch().getGstin()); //branch gstin
            row.put("txnDestinGstin", ""); //not sure where to pick this it was 27 on manuall create
            row.put("txnTypeOfSupply", "1"); // i set it as Regular by default, need to check this with team
            row.put("txnWalkinCustomerType", "");
            Date date = new Date(); // Assuming you have the date information
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
            String formattedDate = dateFormat.format(date);
            row.put("txnDate", formattedDate);
            row.put("useremail", user.getEmail());
            row.put("txnReceiptDetails", "");
            row.put("txnReceiptPaymentBank", "");
            row.put("txnInstrumentNum", "");
            row.put("txnInstrumentDate", "");
            row.put("txnReceiptDescription", "");
            row.put("destCountry", po.getBranch().getCountry());
            row.put("destCurrencyCode", po.getBranch().getCurrency());
            row.put("amount", "");
            row.put("datetimeOfShipping", "");
            row.put("invRefNumber", "");
            row.put("grnRefDate", "");
            row.put("grnRefNumber", "");
            row.put("impRefDate", "");
            row.put("impRefNumber", "");
            row.put("portCode", "");
            row.put("reasonForReturn", "");
            row.put("remarksAddDetailsBuy", "");
            TransactionPurpose usertxnPurpose = TransactionPurpose.findById(IdosConstants.BUY_ON_CREDIT_PAY_LATER);
            log.log(Level.FINE, "buy on credit transaction request while creating PO: " + row.toString());
            BuyTransactionServiceImpl buyTxnService = new BuyTransactionServiceImpl();
            buyTxnService.submitForApproval(user, row, em, entitytransaction, usertxnPurpose,result);
            
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error on Buy on credit transaction create againt PO -> "+ ex.getMessage());
            
        } 
    }
     @Override
     public BillOfMaterialTxnModel submitForApprovalPoNormal(Users user, JsonNode json, EntityManager em, ObjectNode result) throws IDOSException {
         BillOfMaterialTxnModel bomTxn = null;
         try {
             Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l : json.findValue("txnEntityID").asLong();
             Long txnforBranch = (json.findValue("txnForBranch") == null || "".equals(json.findValue("txnForBranch"))) ? 0l : json.findValue("txnForBranch").asLong();
             Long txnforProject = (json.findValue("txnForProject") == null || "".equals(json.findValue("txnForProject"))) ? 0l : json.findValue("txnForProject").asLong();
             Long txnVendor = (json.findValue("txnVendor") == null || "".equals(json.findValue("txnVendor"))) ? 0l : json.findValue("txnVendor").asLong();
             String txnPoReference = json.findValue("txnPoReference") == null ? "" : json.findValue("txnPoReference").asText();
             Date txnDate = new Date();
             long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
             String txnRemarks = json.findValue("txnRemarks").asText();
             String supportingdoc = json.findValue("supportingdoc").asText();
             String txnForItemStr = json.findValue("txnForItem").toString();
             JSONArray arrJSON = new JSONArray(txnForItemStr);
             Branch txnBranch = null;
             Specifics masterItem = null;
             Vendor vendor = null;
             Project txnProject = null;

             TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);

             if (txnEntityID > 0) {
                 bomTxn = BillOfMaterialTxnModel.findById(txnEntityID);
             }else {
                 bomTxn = new BillOfMaterialTxnModel();
             }
             if (txnforBranch != null && txnforBranch > 0) {
                 txnBranch = Branch.findById(txnforBranch);
             }
             if(txnBranch == null){
                 throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF,IdosConstants.BUSINESS_EXCEPTION, "Branch not found " + txnforBranch, IdosConstants.NULL_KEY_EXC_ESMF_MSG);
             }
             if (txnforProject != null && txnforProject > 0) {
                 txnProject = Project.findById(txnforProject);
             }

             if (txnVendor != null && txnVendor > 0) {
                 vendor = Vendor.findById(txnVendor);
             }
             if(vendor == null){
                 throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF,IdosConstants.BUSINESS_EXCEPTION, "Vendor not found " + txnVendor,IdosConstants.NULL_KEY_EXC_ESMF_MSG);
             }

             //Enter data for first item in bomTxn table to be displayed in Transaction list
             bomTxn.setIncome(masterItem);
             bomTxn.setTotalAmount(0d);
             bomTxn.setTransactionPurpose(usertxnPurpose);
             bomTxn.setBranch(txnBranch);
             bomTxn.setOrganization(txnBranch.getOrganization());
             bomTxn.setProject(txnProject);
             bomTxn.setCustomerVendor(vendor);
             bomTxn.setActionDate(txnDate);
             if (txnRemarks != null && !txnRemarks.equals("")) {
                 txnRemarks = user.getEmail() + "#" + txnRemarks;
                 bomTxn.setRemarks(txnRemarks);
                 txnRemarks = bomTxn.getRemarks();
             }
             bomTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(bomTxn.getSupportingDocs(), user.getEmail(), supportingdoc, user, em));
             bomTxn.setTransactionStatus(IdosConstants.TXN_STATUS_REQUIRE_APPROVAL);

             String refNo = CodeHelper.getForeverUniqueID("BOM", null);
             bomTxn.setTransactionRefNumber(refNo);
             bomTxn.setTypeIdentifier(IdosConstants.PURCHASE_ORDER_NORMAL);
             genericDao.saveOrUpdate(bomTxn, user, em);
             FILE_UPLOAD_DAO.updateUploadFileLogs(em, user, supportingdoc, bomTxn.getId(), IdosConstants.BOM_TXN_TYPE);
             Specifics expenseItem = null;
             if(txnEntityID > 0)
                 expenseItem = addUpdateCreatePoTxnItems(em, user, arrJSON, bomTxn, txnDate, false);
             else
                 expenseItem = addUpdateCreatePoTxnItems(em, user, arrJSON, bomTxn, txnDate, true);
             BILL_OF_MATERIAL_TXN_DAO.setApproverEmailDetails( user,  em, bomTxn, txnBranch, expenseItem);
             genericDao.saveOrUpdate(bomTxn, user, em);
            
             BILL_OF_MATERIAL_TXN_DAO.sendWebSocketResponse4Bom(bomTxn, user, result);
         } catch (Exception ex) {
             log.log(Level.SEVERE, user.getEmail(), ex);
             throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION, "Error on Create Purchase Order for approval", ex.getMessage());
         }
         return bomTxn;
     }


    @Override
    public BillOfMaterialTxnModel submitForApprovalPoAgainstRequisition(Users user, JsonNode json, EntityManager em, ObjectNode result) throws IDOSException {
        BillOfMaterialTxnModel bomTxn = null;
        try {
            Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l : json.findValue("txnEntityID").asLong();
            Long txnforBranch = (json.findValue("txnForBranch") == null || "".equals(json.findValue("txnForBranch"))) ? 0l : json.findValue("txnForBranch").asLong();
            Long txnforProject = (json.findValue("txnForProject") == null || "".equals(json.findValue("txnForProject"))) ? 0l : json.findValue("txnForProject").asLong();

            Date txnDate = new Date();
            long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
            String txnRemarks = json.findValue("txnRemarks").asText();
            String supportingdoc = json.findValue("supportingdoc").asText();
            String txnForItemStr = json.findValue("txnForItem").toString();
            JSONArray arrJSON = new JSONArray(txnForItemStr);
            Branch txnBranch = null;
            Specifics masterItem = null;
            Vendor vendor = null;
            Project txnProject = null;

            TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);
            if (txnEntityID > 0) {
                bomTxn = BillOfMaterialTxnModel.findById(txnEntityID);
            }else {
                bomTxn = new BillOfMaterialTxnModel();
            }
            if (txnforBranch != null && txnforBranch > 0) {
                txnBranch = Branch.findById(txnforBranch);
            }
            if(txnBranch == null){
                throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF,IdosConstants.BUSINESS_EXCEPTION, "Branch not found " + txnforBranch, IdosConstants.NULL_KEY_EXC_ESMF_MSG);
            }
            if (txnforProject != null && txnforProject > 0) {
                txnProject = Project.findById(txnforProject);
            }

            //Enter data for first item in bomTxn table to be displayed in Transaction list
            bomTxn.setIncome(masterItem);
            bomTxn.setTotalAmount(0d);
            bomTxn.setTransactionPurpose(usertxnPurpose);
            bomTxn.setBranch(txnBranch);
            bomTxn.setOrganization(txnBranch.getOrganization());
            bomTxn.setProject(txnProject);
            bomTxn.setCustomerVendor(vendor);
            bomTxn.setActionDate(txnDate);
            if (txnRemarks != null && !txnRemarks.equals("")) {
                txnRemarks = user.getEmail() + "#" + txnRemarks;
                bomTxn.setRemarks(txnRemarks);
                txnRemarks = bomTxn.getRemarks();
            }
            bomTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(bomTxn.getSupportingDocs(), user.getEmail(), supportingdoc, user, em));
            bomTxn.setTransactionStatus(IdosConstants.TXN_STATUS_REQUIRE_APPROVAL);

            String refNo = CodeHelper.getForeverUniqueID("BOM", null);
            bomTxn.setTransactionRefNumber(refNo);
            bomTxn.setTypeIdentifier(IdosConstants.PURCHASE_ORDER_AGAINST_REQUISITION);
            genericDao.saveOrUpdate(bomTxn, user, em);
            Specifics expenseItem = null;
            if(txnEntityID > 0)
                expenseItem = addUpdateCreatePoTxnItems(em, user, arrJSON, bomTxn, txnDate, false);
            else
                expenseItem = addUpdateCreatePoTxnItems(em, user, arrJSON, bomTxn, txnDate, true);
            BILL_OF_MATERIAL_TXN_DAO.setApproverEmailDetails( user,  em, bomTxn, txnBranch, expenseItem);
            genericDao.saveOrUpdate(bomTxn, user, em);
            FILE_UPLOAD_DAO.updateUploadFileLogs(em, user, supportingdoc, bomTxn.getId(), IdosConstants.BOM_TXN_TYPE);
            BILL_OF_MATERIAL_TXN_DAO.sendWebSocketResponse4Bom(bomTxn, user, result);
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION, "Error on Create Purchase Order Against Requisition", ex.getMessage());
        }
        return bomTxn;
    }

    public Specifics addUpdateCreatePoTxnItems(EntityManager em, Users user, JSONArray arrJSON, BillOfMaterialTxnModel bomTxn, Date txnDate, boolean isNew) throws IDOSException {
        double totalUnits = 0.0, grossTotal = 0.0;
        Specifics txnItem = null;
        try {
            ArrayList<Long> selectedItemTxns = null;
            if(!isNew){
                selectedItemTxns = new ArrayList<>();
            }
            for(int i=0; i<arrJSON.length(); i++){
                JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                BillOfMaterialTxnItemModel bomItem = null;
                if(isNew) {
                    bomItem = new BillOfMaterialTxnItemModel();
                }else{
                    Long bomItemTxnId = rowItemData.get("bomItemTxnId") == null || "".equals(rowItemData.getString("bomItemTxnId")) ? null : rowItemData.getLong("bomItemTxnId");
                    bomItem = BillOfMaterialTxnItemModel.findById(bomItemTxnId);
                    selectedItemTxns.add(bomItemTxnId);
                }
                Long itemId = rowItemData.get("itemId") == null || "".equals(rowItemData.getString("itemId")) ? null : rowItemData.getLong("itemId");
                Double txnInStockUnit = rowItemData.get("txnInStockUnit") == null || "".equals(rowItemData.getString("txnInStockUnit")) ? null : rowItemData.getDouble("txnInStockUnit");
                Double txnCommitedUnit = rowItemData.get("txnCommitedUnit")== null || "".equals(rowItemData.getString("txnCommitedUnit")) ? null : rowItemData.getDouble("txnCommitedUnit");
                Double txnOrderedUnit = rowItemData.get("txnOrderedUnit")== null || "".equals(rowItemData.getString("txnOrderedUnit")) ? null : rowItemData.getDouble("txnOrderedUnit");
                Double txnNetUnit = rowItemData.get("txnNetUnit")== null || "".equals(rowItemData.getString("txnNetUnit")) ? null : rowItemData.getDouble("txnNetUnit");
                Double txnNoOfUnit = rowItemData.get("txnNoOfUnit")== null || "".equals(rowItemData.getString("txnNoOfUnit")) ? null : rowItemData.getDouble("txnNoOfUnit");
                Double txnPerUnitPrice = rowItemData.get("txnPerUnitPrice")== null || "".equals(rowItemData.getString("txnPerUnitPrice")) ? null : rowItemData.getDouble("txnPerUnitPrice");
                Double txnTotalPrice = rowItemData.get("txnTotalPrice")== null || "".equals(rowItemData.getString("txnTotalPrice")) ? null : rowItemData.getDouble("txnTotalPrice");
                String txnDestGstin = rowItemData.get("txnDestGstin") == null || "".equals(rowItemData.getString("txnDestGstin")) ? null : rowItemData.getString("txnDestGstin");

                txnItem = Specifics.findById(itemId);
                bomItem.setOrganization(bomTxn.getOrganization());
                bomItem.setBranch(bomTxn.getBranch());
                bomItem.setAvailableUnits(txnInStockUnit);
                bomItem.setCommittedUnits(txnCommitedUnit);
                bomItem.setOrderedUnits(txnOrderedUnit);
                bomItem.setNetUnits(txnNetUnit);
                bomItem.setNoOfUnits(txnNoOfUnit);
                bomItem.setPricePerUnit(txnPerUnitPrice);
                bomItem.setTotalPrice(txnTotalPrice);
                bomItem.setDestinationGstin(txnDestGstin);
                bomItem.setBillOfMaterialTxn(bomTxn);
                bomItem.setExpense(txnItem);
                if(bomTxn.getTypeIdentifier() == IdosConstants.PURCHASE_ORDER_AGAINST_REQUISITION){
                    Long txnVendor = rowItemData.get("txnVendor")== null || "".equals(rowItemData.getString("txnVendor")) ? null : rowItemData.getLong("txnVendor");
                    Vendor vendor = null;
                    if (txnVendor != null && txnVendor > 0) {
                        vendor = Vendor.findById(txnVendor);
                    }
                    if(vendor == null){
                        throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF,IdosConstants.BUSINESS_EXCEPTION, "Vendor not found " + txnVendor,IdosConstants.NULL_KEY_EXC_ESMF_MSG);
                    }
                    bomItem.setVendor(vendor);
                }
                genericDao.saveOrUpdate(bomItem, user, em);
                totalUnits += txnNoOfUnit;
                grossTotal += txnTotalPrice;
            }
            if(selectedItemTxns != null) {
                genericDao.deleteByParamName(DEL_OLD_TXN_ITEMS_JPQL, em, selectedItemTxns);
            }
            bomTxn.setTotalAmount(grossTotal);
            bomTxn.setTotalNetAmount(grossTotal);
            bomTxn.setTotalNoOfUnits(totalUnits);
        }catch(Exception ex){
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION, "Error on save/update multiitems.", ex.getMessage());
        }
        return txnItem;
    }

}
