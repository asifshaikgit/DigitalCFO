package com.idos.dao;

import com.idos.util.CodeHelper;
import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import actor.CreatorActor;
import com.idos.util.IdosDaoConstants;
import com.idos.util.UploadUtil;
import pojo.TransactionViewResponse;
import model.*;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.WebSocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import static service.BaseService.invDAO;
import java.util.logging.Level;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.typesafe.config.ConfigFactory;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;

public class BillOfMaterialTxnDAOImpl implements BillOfMaterialTxnDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public BillOfMaterialTxnModel submitForApproval(Users user, JsonNode json, EntityManager em, ObjectNode result)
            throws IDOSException {
        BillOfMaterialTxnModel bomTxn = null;
        try {
            Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                    : json.findValue("txnEntityID").asLong();
            Long txnforBranch = (json.findValue("txnForBranch") == null || "".equals(json.findValue("txnForBranch")))
                    ? 0l
                    : json.findValue("txnForBranch").asLong();
            Long txnforProject = (json.findValue("txnForProject") == null || "".equals(json.findValue("txnForProject")))
                    ? 0l
                    : json.findValue("txnForProject").asLong();
            Long txnforCustomer = (json.findValue("txnForCustomer") == null
                    || "".equals(json.findValue("txnForCustomer"))) ? 0l : json.findValue("txnForCustomer").asLong();
            Long txnMasterItem = (json.findValue("txnMasterItem") == null || "".equals(json.findValue("txnMasterItem")))
                    ? 0l
                    : json.findValue("txnMasterItem").asLong();
            Double txnNoOfUnit = (json.findValue("txnNoOfUnit") == null || "".equals(json.findValue("txnNoOfUnit")))
                    ? 0.0
                    : json.findValue("txnNoOfUnit").asDouble();
            Date txnDate = new Date();
            long txnPurposeVal = json.findValue("txnPurposeVal").asLong();
            String txnRemarks = json.findValue("txnRemarks").asText();
            String supportingdoc = json.findValue("supportingdoc").asText();
            String txnForItemStr = json.findValue("txnForItem").toString();
            JSONArray arrJSON = new JSONArray(txnForItemStr);
            Branch txnBranch = null;
            Specifics masterItem = null;
            Vendor customer = null;
            Project txnProject = null;

            TransactionPurpose usertxnPurpose = TransactionPurpose.findById(txnPurposeVal);

            if (txnEntityID > 0) {
                bomTxn = BillOfMaterialTxnModel.findById(txnEntityID);
            } else {
                bomTxn = new BillOfMaterialTxnModel();
            }
            if (txnforBranch != null && txnforBranch > 0) {
                txnBranch = Branch.findById(txnforBranch);
            } else {
                throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
                        "Branch not found " + txnforBranch, IdosConstants.NULL_KEY_EXC_ESMF_MSG);
            }
            if (txnforProject != null && txnforProject > 0) {
                txnProject = Project.findById(txnforProject);
            }

            if (txnMasterItem != null && txnMasterItem > 0) {
                masterItem = Specifics.findById(txnMasterItem);
            } else {
                throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
                        "Master item not found " + txnMasterItem, IdosConstants.NULL_KEY_EXC_ESMF_MSG);
            }
            if (txnforCustomer != null && txnforCustomer > 0) {
                customer = Vendor.findById(txnforCustomer);
            } else {
                throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
                        "Customer not found " + txnforCustomer, IdosConstants.NULL_KEY_EXC_ESMF_MSG);
            }

            // Enter data for first item in bomTxn table to be displayed in Transaction list
            bomTxn.setIncome(masterItem);
            bomTxn.setIncomeNoOfUnits(txnNoOfUnit);
            bomTxn.setTotalAmount(0d);
            bomTxn.setTransactionPurpose(usertxnPurpose);
            bomTxn.setBranch(txnBranch);
            bomTxn.setOrganization(txnBranch.getOrganization());
            bomTxn.setProject(txnProject);
            bomTxn.setCustomerVendor(customer);
            bomTxn.setActionDate(txnDate);
            if (txnRemarks != null && !txnRemarks.equals("")) {
                txnRemarks = user.getEmail() + "#" + txnRemarks;
                bomTxn.setRemarks(txnRemarks);
                txnRemarks = bomTxn.getRemarks();
            }
            bomTxn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(bomTxn.getSupportingDocs(),
                    user.getEmail(), supportingdoc, user, em));
            bomTxn.setTransactionStatus(IdosConstants.TXN_STATUS_REQUIRE_APPROVAL);
            // list of additional users all approver role users of thet organization
            Map<String, Object> criterias = new HashMap<String, Object>();
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
                criterias.put("branch.id", txnBranch.getId());
                criterias.put("presentStatus", 1);
                UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class, criterias,
                        em);
                if (userHasRightInBranch != null) {
                    // check for right in chart of accounts
                    criterias.clear();
                    criterias.put("user.id", usrRoles.getUser().getId());
                    criterias.put("userRights.id", 2L);
                    criterias.put("specifics.id", bomTxn.getIncome().getId());
                    criterias.put("presentStatus", 1);
                    UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class, criterias,
                            em);
                    if (userHasRightInCOA != null) {
                        approverEmails += usrRoles.getUser().getEmail() + ",";
                    }
                }
            }
            bomTxn.setApproverEmails(approverEmails);
            bomTxn.setAdditionalApproverEmails(additionalApprovarUsers);
            String refNo = CodeHelper.getForeverUniqueID("BOM", null);
            bomTxn.setTransactionRefNumber(refNo);
            genericDao.saveOrUpdate(bomTxn, user, em);
            FILE_UPLOAD_DAO.updateUploadFileLogs(em, user, supportingdoc, bomTxn.getId(), IdosConstants.BOM_TXN_TYPE);
            double totalUnits = 0.0;
            // Enter multiple items data into TransactionItems table
            if (txnEntityID > 0)
                totalUnits = insertBillOfMaterialTxnItems(em, user, arrJSON, bomTxn, txnDate, false);
            else
                totalUnits = insertBillOfMaterialTxnItems(em, user, arrJSON, bomTxn, txnDate, true);
            bomTxn.setTotalNoOfUnits(totalUnits);
            genericDao.saveOrUpdate(bomTxn, user, em);
            sendWebSocketResponse4Bom(bomTxn, user, result);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Bill of Material for approval", ex.getMessage());
        }
        return bomTxn;
    }

    @Override
    public double insertBillOfMaterialTxnItems(EntityManager em, Users user, JSONArray arrJSON,
            BillOfMaterialTxnModel bomTxn, Date txnDate, boolean isNew) throws IDOSException {
        double totalUnits = 0.0;
        try {
            ArrayList<Long> selectedItemTxns = null;
            if (!isNew) {
                selectedItemTxns = new ArrayList<>();
            }
            for (int i = 0; i < arrJSON.length(); i++) {
                JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                BillOfMaterialTxnItemModel bomItem = null;
                if (isNew) {
                    bomItem = new BillOfMaterialTxnItemModel();
                } else {
                    Long bomItemTxnId = rowItemData.get("bomItemTxnId") == null
                            || "".equals(rowItemData.getString("bomItemTxnId")) ? null
                                    : rowItemData.getLong("bomItemTxnId");
                    bomItem = BillOfMaterialTxnItemModel.findById(bomItemTxnId);
                    selectedItemTxns.add(bomItemTxnId);
                }
                Long itemId = rowItemData.get("itemId") == null || "".equals(rowItemData.getString("itemId")) ? null
                        : rowItemData.getLong("itemId");
                String bomUnitOfMeasure = rowItemData.get("bomUnitOfMeasure") == null
                        || "".equals(rowItemData.getString("bomUnitOfMeasure")) ? null
                                : rowItemData.getString("bomUnitOfMeasure");
                Double bomNoOfUnit = rowItemData.get("bomNoOfUnit") == null
                        || "".equals(rowItemData.getString("bomNoOfUnit")) ? null
                                : rowItemData.getDouble("bomNoOfUnit");
                Long bomVendor = rowItemData.get("bomVendor") == null || "".equals(rowItemData.getString("bomVendor"))
                        ? null
                        : rowItemData.getLong("bomVendor");
                String bomOem = rowItemData.get("bomOem") == null || "".equals(rowItemData.getString("bomOem")) ? null
                        : rowItemData.getString("bomOem");
                String bomTom = rowItemData.get("bomTom") == null || "".equals(rowItemData.getString("bomTom")) ? null
                        : rowItemData.getString("bomTom");
                Double bomInStockUnit = rowItemData.get("bomInStockUnit") == null
                        || "".equals(rowItemData.getString("bomInStockUnit")) ? null
                                : rowItemData.getDouble("bomInStockUnit");
                Integer bomKnowledgeFollowed = rowItemData.get("bomKnowledgeFollowed") == null
                        || "".equals(rowItemData.getString("bomKnowledgeFollowed")) ? null
                                : rowItemData.getInt("bomKnowledgeFollowed");

                Vendor vendor = Vendor.findById(bomVendor);
                Specifics txnItem = Specifics.findById(itemId);
                bomItem.setOrganization(bomTxn.getOrganization());
                bomItem.setBranch(bomTxn.getBranch());
                Long txnPurpose = bomTxn.getTransactionPurpose().getId();
                bomItem.setOem(bomOem);
                bomItem.setTypeOfMaterial(bomTom);
                bomItem.setVendor(vendor);
                bomItem.setKlfollowStatus(bomKnowledgeFollowed);
                bomItem.setMeasureName(bomUnitOfMeasure);
                bomItem.setBillOfMaterialTxn(bomTxn);
                bomItem.setExpense(txnItem);
                bomItem.setNoOfUnits(bomNoOfUnit);
                genericDao.saveOrUpdate(bomItem, user, em);
                totalUnits += bomNoOfUnit;
            }
            if (selectedItemTxns != null) {
                String query = "delete from BillOfMaterialTxnItemModel t1 where t1.organization.id= ?1 and t1.billOfMaterialTxn.id not in (?2)";
                genericDao.deleteByParamName(query, em, selectedItemTxns);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on save/update multiitems.", ex.getMessage());
        }
        return totalUnits;
    }

    @Override
    public void sendWebSocketResponse4Bom(BillOfMaterialTxnModel bomTxn, Users user, ObjectNode result) {
        try {
            // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
            // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
            // for (int i = 0; i < keyArray.length; i++) {
            // Users orgusers = Users.findActiveByEmail(String.valueOf(keyArray[i]));
            // if (orgusers != null && orgusers.getOrganization().getId() ==
            // user.getOrganization().getId()) {
            // orgtxnregistereduser.put(keyArray[i].toString(),
            // CreatorActor.expenseregistrered.get(keyArray[i]));
            // }
            // }

            String branchName = bomTxn.getBranch() == null ? "" : bomTxn.getBranch().getName();
            String projectName = bomTxn.getProject() == null ? "" : bomTxn.getProject().getName();
            String itemName = bomTxn.getIncome() == null ? "" : bomTxn.getIncome().getName();
            String itemParentName = "";
            String budgetAllocated = "";
            String budgetAvailable = "";

            String customerVendorName = bomTxn.getCustomerVendor() == null ? "" : bomTxn.getCustomerVendor().getName();
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
            String invoiceNumber = "";
            String txnPurpose = bomTxn.getTransactionPurpose().getTransactionPurpose();
            if (bomTxn.getTransactionPurpose().getId() == IdosConstants.CREATE_PURCHASE_REQUISITION
                    && bomTxn.getTypeIdentifier() != null
                    && bomTxn.getTypeIdentifier() == IdosConstants.PURCHASE_REQUISITION_AGAINST_SALES_ORDER) {
                txnPurpose += " (Against Sales Order)";
            }
            TransactionViewResponse.addActionTxn(bomTxn.getId(), branchName, projectName, itemName, itemParentName,
                    budgetAllocated, "", budgetAvailable, "", customerVendorName, txnPurpose,
                    IdosConstants.idosdf.format(bomTxn.getActionDate()), invoiceDateLabel, invoiceDate, paymentMode,
                    noOfUnits, perUnitPrice, grossAmt, netAmount, netAmtDesc, outstandings,
                    bomTxn.getTransactionStatus(), createdBy, approverLabel, approverEmail, txnDocument, txnRemarks,
                    debitCredit, approverEmails, additionalApprovalEmails, selectedAdditionalApproval,
                    txnSpecialStatus, frieghtCharges, poReference, txnInstrumentNumber,
                    txnInstrumentDate, bomTxn.getTransactionPurpose().getId(), txnRemarksPrivate, invoiceNumber, 0,
                    bomTxn.getTransactionRefNumber(), 0l, 0.0, 0, 0, result);
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
        }
    }

    public BillOfMaterialTxnModel approverAction(Users user, EntityManager em, JsonNode json, ObjectNode result)
            throws IDOSException {
        BillOfMaterialTxnModel txn = null;
        try {
            Boolean isSingleUserDeploy = ConfigParams.getInstance().isDeploymentSingleUser(user);
            int selectedApproverAction = json.findValue("selectedApproverAction").asInt();
            Long transactionPrimId = json.findValue("transactionPrimId").asLong();
            String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
            String txnRmarks = json.findValue("txnRmarks").asText() != null ? json.findValue("txnRmarks").asText()
                    : null;
            txn = BillOfMaterialTxnModel.findById(transactionPrimId);
            txn.setSupportingDocs(TRANSACTION_DAO.getAndDeleteSupportingDocument(txn.getSupportingDocs(),
                    user.getEmail(), suppDoc, user, em));
            if (txnRmarks != null && !txnRmarks.equals("")) {
                if (txn.getRemarks() != null) {
                    txn.setRemarks(txn.getRemarks() + "|" + user.getEmail() + "#" + txnRmarks);
                } else {
                    txn.setRemarks(user.getEmail() + "#" + txnRmarks);
                }
            }
            txn.setModifiedBy(user);

            if (!IdosConstants.TXN_STATUS_APPROVED.equals(txn.getTransactionStatus())) {
                if (selectedApproverAction == 1) {
                    // approved action perform transaction operation
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
             * if (selectedApproverAction == 7) {
             * 
             * }
             * if (selectedApproverAction == 8) {
             * 
             * }
             */
            if (!IdosConstants.TXN_STATUS_REJECTED.equals(txn.getTransactionStatus())) {
                if (selectedApproverAction == 2) {
                    // reject action
                    txn.setTransactionStatus("Rejected");
                    txn.setApproverActionBy(user);
                }
            }
            if (selectedApproverAction == 3) {
                // additional approval action
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
            BILL_OF_MATERIAL_TXN_DAO.setInvoiceQuotProfSerialForCreatePO(user, em, txn);
            genericDao.saveOrUpdate(txn, user, em);
            FILE_UPLOAD_SERVICE.updateUploadFileLogs(em, user, suppDoc, txn.getId(), IdosConstants.BOM_TXN_TYPE);
            // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
            // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
            // for (int i = 0; i < keyArray.length; i++) {
            // StringBuilder sbquery = new StringBuilder("");
            // sbquery.append(
            // "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
            // obj.presentStatus=1");
            // List<Users> orgusers = genericDao.executeSimpleQuery(sbquery.toString(), em);
            // if (orgusers != null && !orgusers.isEmpty()
            // && orgusers.get(0).getOrganization().getId() ==
            // user.getOrganization().getId()) {
            // orgtxnregistereduser.put(keyArray[i].toString(),
            // CreatorActor.expenseregistrered.get(keyArray[i]));
            // }
            // }

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
            if (txn.getIncome() != null) {
                itemName = txn.getIncome().getName();
            }
            if (txn.getCustomerVendor() != null) {
                customerVendorName = txn.getCustomerVendor().getName();
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
            if (txn.getIncome() != null) {
                if (txn.getIncome().getParentSpecifics() != null && !txn.getIncome().getParentSpecifics().equals("")) {
                    itemParentName = txn.getIncome().getParentSpecifics().getName();
                } else {
                    itemParentName = txn.getIncome().getParticularsId().getName();
                }
            }
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

            TransactionViewResponse.addActionTxn(txn.getId(), branchName, projectName, itemName, itemParentName,
                    budgetAllocated,
                    budgetAllocatedAmt, budgetAvailable, budgetAvailableAmt, customerVendorName, txnPurpose, txnDate,
                    invoiceDateLabel, invDate, paymentMode, noOfUnit, unitPrice, grossAmount, netAmount, netAmountDesc,
                    "", status, createdBy, approverLabel, approverEmail, txnDocument, txnRemarks, "", approverEmails,
                    additionalapproverEmails, selectedAdditionalApproval, txnSpecialStatus,
                    frieghtCharges, poReference, "", "", txn.getTransactionPurpose().getId(), "", invoiceNumber,
                    txnIdentifier, txn.getTransactionRefNumber(), 0l, 0.0, 0, 0, result);

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on Bill of Material for user action", ex.getMessage());
        }
        log.log(Level.FINE, ">>>> End ");
        return txn;
    }

    @Override
    public double getPurchaseOrderUnfulfilledUnits(Users user, EntityManager em, long entityid, long branchid)
            throws IDOSException {
        Double total = 0.0;
        try {
            String SQL = "select sum(obj.noOfUnits), sum(obj.fulfilledUnits) from BillOfMaterialTxnItemModel obj where obj.organization.id= ?1 and obj.branch.id= ?2 and  obj.expense.id = ?3 and obj.billOfMaterialTxn.id in (select t1.id from BillOfMaterialTxnModel t1 where t1.organization.id= ?4 and t1.branch.id= ?5 and  t1.transactionPurpose.id = ?6 and t1.presentStatus=1) and (obj.isFulfilled=0 or obj.isFulfilled is null)";
            Query q = em.createQuery(SQL);
            q.setParameter(1, user.getOrganization().getId());
            q.setParameter(2, branchid);
            q.setParameter(3, entityid);
            q.setParameter(4, user.getOrganization().getId());
            q.setParameter(5, branchid);
            q.setParameter(6, IdosConstants.CREATE_PURCHASE_ORDER);
            if (log.isLoggable(Level.INFO))
                log.log(Level.INFO, "JPQL: " + SQL);
            List<Object[]> txnLists = q.getResultList();
            Double noOfUnits = 0.0;
            Double fulfulledUnits = 0.0;
            for (Object[] custData : txnLists) {
                if (custData[0] != null)
                    noOfUnits += Double.parseDouble(String.valueOf(custData[0]));
                if (custData[1] != null)
                    fulfulledUnits += Double.parseDouble(String.valueOf(custData[1]));
            }
            total = noOfUnits - fulfulledUnits;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on fetching unfilfulled units", ex.getMessage());
        }
        if (log.isLoggable(Level.INFO))
            log.log(Level.INFO, "End : " + total);
        return total;
    }

    @Override
    public void setApproverEmailDetails(Users user, EntityManager em, BillOfMaterialTxnModel bomTxn, Branch branch,
            Specifics item) {
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
                // check for right in chart of accounts
                criterias.clear();
                criterias.put("user.id", usrRoles.getUser().getId());
                criterias.put("userRights.id", 2L);
                criterias.put("specifics.id", item.getId());
                criterias.put("presentStatus", 1);
                UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class, criterias,
                        em);
                if (userHasRightInCOA != null) {
                    approverEmails += usrRoles.getUser().getEmail() + ",";
                }
            }
        }
        if (approverEmails.equals("")) {
            bomTxn.setApproverEmails(additionalApprovarUsers);
        } else {
            bomTxn.setApproverEmails(approverEmails);
        }
        bomTxn.setAdditionalApproverEmails(additionalApprovarUsers);
    }

    @Override
    public void setInvoiceQuotProfSerialForCreatePO(Users user, EntityManager entityManager, BillOfMaterialTxnModel txn)
            throws IDOSException {
        Organization organization = user.getOrganization();
        if (organization.getOrgSerialGenrationType() != null && organization.getOrgSerialGenrationType() == 2) {
            BILL_OF_MATERIAL_TXN_DAO.setInvoiceQuotProfGstinSerialForCreatePO(user, entityManager, txn);
        } else {

            Integer serialno = 0;
            String serialStr = "";

            Date date = txn.getActionDate();
            if (date == null) {
                date = new Date();
            }
            int fullYear = DateUtil.getDateYear(date);
            int year = fullYear % 100;
            String monthName = DateUtil.getMonthName(date);
            String monthNoStr = DateUtil.getMonthNumber(date);
            if (monthNoStr.length() < 2) {
                monthNoStr = "0" + monthNoStr;
            }
            Date startDate = null;
            if (txn.getBranch().getOrganization().getFinancialStartDate() != null) {
                startDate = txn.getBranch().getOrganization().getFinancialStartDate();
            } else {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, 3);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startDate = cal.getTime();
            }
            Calendar today = Calendar.getInstance();
            boolean isYearChange = false;
            if (organization.getSerialChangedDateYear() != null) {
                Calendar compairCal = Calendar.getInstance();
                compairCal.setTime(organization.getSerialChangedDateYear());
                compairCal.set(Calendar.YEAR, (DateUtil.getDateYear(organization.getSerialChangedDateYear()) + 1));
                isYearChange = today.after(compairCal) ? true : false;
            } else {
                organization.setSerialChangedDateYear(startDate);
            }

            boolean isSerialMonthChange = false;
            if (organization.getSerialCurrentMonth() != null) {
                isSerialMonthChange = (today.get(Calendar.MONTH)) != organization.getSerialCurrentMonth() ? true
                        : false;
            } else {
                Calendar tempCal = Calendar.getInstance();
                tempCal.setTime(startDate);
                organization.setSerialCurrentMonth(tempCal.get(Calendar.MONTH));
                genericDao.saveOrUpdate(organization, user, entityManager);
            }
            Integer serial = 0;
            if (isYearChange) {
                if (organization.getCreatePurchaseOrderInterval() != null
                        && organization.getCreatePurchaseOrderInterval() != 2)
                    organization.setCreatePurchaseOrderInterval(serial);

                organization.setSerialChangedDateYear(today.getTime());
                genericDao.saveOrUpdate(organization, user, entityManager);
            } else if (isSerialMonthChange) {
                if (organization.getCreatePurchaseOrderInterval() != null
                        && organization.getCreatePurchaseOrderInterval() == 2)
                    organization.setCreatePurchaseOrderInterval(serial);

                organization.setSerialCurrentMonth(today.get(Calendar.MONTH));
                genericDao.saveOrUpdate(organization, user, entityManager);
            }

            String branchName = txn.getBranch().getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "").substring(0, 3);
            String olInvoiceStr = ConfigFactory.load().getString("offline.invoice.prefix");
            if (olInvoiceStr != null && !"".equals(olInvoiceStr)) {
                if (txn.getTransactionPurpose().getId() == IdosConstants.CREATE_PURCHASE_ORDER) {
                    serialno = organization.getCreatePurchaseOrderSerial() == null ? 1
                            : organization.getCreatePurchaseOrderSerial() + 1;
                    organization.setCreatePurchaseOrderSerial(serialno + 1);
                    branchName = "PO/" + branchName;
                }
                if (serialStr == null || serialStr == "") {
                    if (serialno.toString().length() == 1)
                        serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + "0000" + serialno;
                    else if (serialno.toString().length() == 2)
                        serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + "000" + serialno;
                    else if (serialno.toString().length() == 3)
                        serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + "00" + serialno;
                    else if (serialno.toString().length() == 4)
                        serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + "0" + serialno;
                    else if (serialno.toString().length() == 5)
                        serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + serialno;
                    else
                        serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + "00000";
                }
                txn.setInvoiceNumber(serialStr.toUpperCase());
            } else {
                if (txn.getTransactionPurpose().getId() == IdosConstants.CREATE_PURCHASE_ORDER) {
                    serialno = organization.getCreatePurchaseOrderSerial() == null ? 1
                            : organization.getCreatePurchaseOrderSerial();
                    organization.setCreatePurchaseOrderSerial(serialno + 1);
                    branchName = "PO/" + branchName;
                }

                if (serialStr == null || serialStr == "") {
                    if (serialno.toString().length() == 1)
                        serialStr = branchName + monthNoStr + year + "-" + "0000" + serialno;
                    else if (serialno.toString().length() == 2)
                        serialStr = branchName + monthNoStr + year + "-" + "000" + serialno;
                    else if (serialno.toString().length() == 3)
                        serialStr = branchName + monthNoStr + year + "-" + "00" + serialno;
                    else if (serialno.toString().length() == 4)
                        serialStr = branchName + monthNoStr + year + "-" + "0" + serialno;
                    else if (serialno.toString().length() == 5)
                        serialStr = branchName + monthNoStr + year + "-" + serialno;
                    else
                        serialStr = branchName + monthNoStr + year + "-" + "00000";
                }
                txn.setInvoiceNumber(serialStr.toUpperCase());
            }
        }
        genericDao.saveOrUpdate(organization, user, entityManager);
    }

    @Override
    public void setInvoiceQuotProfGstinSerialForCreatePO(Users user, EntityManager entityManager,
            BillOfMaterialTxnModel txn) throws IDOSException {
        OrganizationGstinSerials orgSerialBranch = null;
        Organization organization = txn.getBranch().getOrganization();
        Integer serialno = 0;
        String serialStr = "";

        Date date = txn.getActionDate();
        if (date == null) {
            date = Calendar.getInstance().getTime();
        }
        int fullYear = DateUtil.getDateYear(date);
        int year = fullYear % 100;
        String monthName = DateUtil.getMonthName(date);
        String monthNoStr = DateUtil.getMonthNumber(date);
        if (monthNoStr.length() < 2) {
            monthNoStr = "0" + monthNoStr;
        }
        Long orgId = txn.getBranch().getOrganization().getId();
        String gstIn = txn.getBranch().getGstin();
        Date startDate = null;
        if (txn.getBranch().getOrganization().getFinancialStartDate() != null) {
            startDate = txn.getBranch().getOrganization().getFinancialStartDate();
        } else {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, 3);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            startDate = cal.getTime();
        }
        Calendar today = Calendar.getInstance();
        boolean isYearChange = false;
        if (organization.getSerialChangedDateYear() != null) {
            Calendar compairCal = Calendar.getInstance();
            compairCal.setTime(organization.getSerialChangedDateYear());
            compairCal.set(Calendar.YEAR, (DateUtil.getDateYear(organization.getSerialChangedDateYear()) + 1));
            isYearChange = today.after(compairCal) ? true : false;
        } else {
            organization.setSerialChangedDateYear(startDate);
            isYearChange = true;
        }
        boolean isSerialMonthChange = false;
        if (organization.getSerialCurrentMonth() != null) {
            isSerialMonthChange = (today.get(Calendar.MONTH)) != organization.getSerialCurrentMonth() ? true : false;
        } else {
            Calendar tempCal = Calendar.getInstance();
            tempCal.setTime(startDate);
            organization.setSerialCurrentMonth(tempCal.get(Calendar.MONTH));
            genericDao.saveOrUpdate(organization, user, entityManager);
        }
        Integer interval = 0;
        if (txn.getBranch().getOrganization().getGstInInterval() != null) {
            interval = txn.getBranch().getOrganization().getGstInInterval();
        } else {
            interval = 1;
        }
        if (interval == 1) {
            if (isYearChange) {
                List<OrganizationGstinSerials> byOrganization = OrganizationGstinSerials
                        .getByOrganization(entityManager, orgId);
                for (OrganizationGstinSerials obj : byOrganization) {
                    obj.setSerialNo(0);
                }
                organization.setSerialChangedDateYear(today.getTime());
                genericDao.saveOrUpdate(organization, user, entityManager);
            }
        } else if (interval == 2) {
            if (isSerialMonthChange) {
                List<OrganizationGstinSerials> byOrganization = OrganizationGstinSerials
                        .getByOrganization(entityManager, orgId);
                for (OrganizationGstinSerials obj : byOrganization) {
                    obj.setSerialNo(0);
                }
                organization.setSerialCurrentMonth(today.get(Calendar.MONTH));
                genericDao.saveOrUpdate(organization, user, entityManager);
            }
        }
        String branchName = txn.getBranch().getName().replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "").substring(0, 3);
        String olInvoiceStr = ConfigFactory.load().getString("offline.invoice.prefix");
        if (olInvoiceStr != null && !"".equals(olInvoiceStr)) {
            if (txn.getTransactionPurpose().getId() == IdosConstants.CREATE_PURCHASE_ORDER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_CREATE_PURCHASE_ORDER, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_CREATE_PURCHASE_ORDER);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo();
                }
                orgSerialBranch.setSerialNo(serialno + 1);
                branchName = "PO/" + branchName;
            }
            if (serialStr == null || serialStr == "") {
                if (serialno.toString().length() == 1)
                    serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + "0000" + serialno;
                else if (serialno.toString().length() == 2)
                    serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + "000" + serialno;
                else if (serialno.toString().length() == 3)
                    serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + "00" + serialno;
                else if (serialno.toString().length() == 4)
                    serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + "0" + serialno;
                else if (serialno.toString().length() == 5)
                    serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + serialno;
                else
                    serialStr = branchName + monthNoStr + year + "-" + olInvoiceStr + "00000";
            }
            txn.setInvoiceNumber(serialStr.toUpperCase());
        } else {
            if (txn.getTransactionPurpose().getId() == IdosConstants.CREATE_PURCHASE_ORDER) {
                orgSerialBranch = OrganizationGstinSerials.getByOrgAndDocCategory(entityManager, orgId,
                        IdosConstants.GSTIN_SERIAL_FOR_CREATE_PURCHASE_ORDER, gstIn);
                if (orgSerialBranch == null) {
                    orgSerialBranch = new OrganizationGstinSerials();
                    orgSerialBranch.setGstIn(gstIn);
                    orgSerialBranch.setOrganization(user.getOrganization());
                    orgSerialBranch.setDocumentCategoryNo(IdosConstants.GSTIN_SERIAL_FOR_CREATE_PURCHASE_ORDER);
                    serialno = 1;
                } else {
                    serialno = orgSerialBranch.getSerialNo() == null ? 1 : orgSerialBranch.getSerialNo();
                }
                orgSerialBranch.setSerialNo(serialno + 1);
                branchName = "PO/" + branchName;
            }
            if (serialStr == null || serialStr == "") {
                if (serialno.toString().length() == 1)
                    serialStr = branchName + monthNoStr + year + "-" + "0000" + serialno;
                else if (serialno.toString().length() == 2)
                    serialStr = branchName + monthNoStr + year + "-" + "000" + serialno;
                else if (serialno.toString().length() == 3)
                    serialStr = branchName + monthNoStr + year + "-" + "00" + serialno;
                else if (serialno.toString().length() == 4)
                    serialStr = branchName + monthNoStr + year + "-" + "0" + serialno;
                else if (serialno.toString().length() == 5)
                    serialStr = branchName + monthNoStr + year + "-" + serialno;
                else
                    serialStr = branchName + monthNoStr + year + "-" + "00000";
            }
            txn.setInvoiceNumber(serialStr.toUpperCase());
        }
        genericDao.saveOrUpdate(orgSerialBranch, user, entityManager);
    }

    @Override
    public List<BillOfMaterialTxnModel> getPurchaseOrSalesOrderUnfulfillTxns(Users user, EntityManager em,
            Long branchid, Long vendorId, Long bomTxnType, ArrayNode bomTxnsArrNode) throws IDOSException {
        ArrayList inparams = new ArrayList(4);
        inparams.add(user.getOrganization().getId());
        inparams.add(branchid);
        inparams.add(bomTxnType);
        List<BillOfMaterialTxnModel> bomTxnList = genericDao.queryWithParamsName(UNFUL_TXN_JPQL, em, inparams);
        for (BillOfMaterialTxnModel bomTxn : bomTxnList) {
            ObjectNode row = Json.newObject();
            row.put("id", bomTxn.getId());
            if (bomTxn.getTotalNetAmount() != null) {
                row.put("name", IdosConstants.IDOSDF.format(bomTxn.getActionDate()) + "(" + bomTxn.getTotalNetAmount()
                        + ")" + bomTxn.getTransactionRefNumber());
            } else {
                row.put("name",
                        IdosConstants.IDOSDF.format(bomTxn.getActionDate()) + ":" + bomTxn.getTransactionRefNumber());
            }
            row.put("txnRefNo", bomTxn.getTransactionRefNumber());

            if (bomTxn.getIncome() != null)
                row.put("incomeId", bomTxn.getIncome().getId());
            else
                row.put("incomeId", "");
            if (bomTxn.getProject() != null) {
                row.put("projectId", bomTxn.getProject().getId());
            } else {
                row.put("projectId", "");
            }
            if (bomTxn.getCustomerVendor() != null)
                row.put("customerId", bomTxn.getCustomerVendor().getId());
            else
                row.put("customerId", "");
            if (bomTxn.getIncomeNoOfUnits() != null)
                row.put("incomeUnits", bomTxn.getIncomeNoOfUnits());
            else
                row.put("incomeUnits", "");
            bomTxnsArrNode.add(row);
        }
        return bomTxnList;
    }

}
