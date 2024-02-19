package controllers;

import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IdosConstants;
import play.mvc.Http.Request;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import play.Application;
import play.db.jpa.JPAApi;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import java.util.logging.Level;

/**
 * @author Sunil K Namdev created on 16.02.2019
 */
public class BillOfMaterialTxnController extends StaticController {

    private static JPAApi jpaApi;
    private static EntityManager em;
    public static Application application;
    private Request request;

    @Inject
    public BillOfMaterialTxnController(Application application) {
        super(application);
        em = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result getBomItemDetails(Request request) {
        Users user = getUserInfo(request);
        if (user == null) {
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        JsonNode json = request.body().asJson();
        ArrayNode bomItems = result.putArray("bomItems");
        Map<String, Object> criterias = new HashMap<String, Object>();
        Long bomItemId = (null == json.findValue("bomItemId").asText()
                || "".equals(json.findValue("bomItemId").asText())) ? 0 : json.findValue("bomItemId").asLong();
        try {
            BillOfMaterialItemModel bomItem = BillOfMaterialItemModel.findById(bomItemId);
            if (bomItem != null) {
                ObjectNode itemRow = Json.newObject();
                if (bomItem.getId() != null) {
                    itemRow.put("id", bomItem.getId());
                } else {
                    itemRow.put("id", "");
                }

                if (bomItem.getNoOfUnits() != null) {
                    itemRow.put("noOfUnits", bomItem.getNoOfUnits());
                } else {
                    itemRow.put("noOfUnits", "");
                }

                if (bomItem.getMeasureName() != null) {
                    itemRow.put("measureName", bomItem.getMeasureName());
                } else {
                    itemRow.put("measureName", "");
                }

                if (bomItem.getVendor() != null) {
                    if (bomItem.getVendor().getId() != null) {
                        itemRow.put("vendorId", bomItem.getVendor().getId());
                    } else {
                        itemRow.put("vendorId", "");
                    }
                    if (bomItem.getVendor().getName() != null) {
                        itemRow.put("vendorName", bomItem.getVendor().getName());
                    } else {
                        itemRow.put("vendorName", "");
                    }
                } else {
                    itemRow.put("vendorId", "");
                    itemRow.put("vendorName", "");
                }

                if (bomItem.getOem() != null) {
                    itemRow.put("oem", bomItem.getOem());
                } else {
                    itemRow.put("oem", "");
                }

                if (bomItem.getTypeOfMaterial() != null) {
                    itemRow.put("typeOfMaterial", bomItem.getTypeOfMaterial());
                } else {
                    itemRow.put("typeOfMaterial", "");
                }

                if (bomItem.getKnowledgeLib() != null) {
                    itemRow.put("klIsMandatory", bomItem.getTypeOfMaterial());
                } else {
                    itemRow.put("klIsMandatory", "");
                }
                bomItems.add(itemRow);
            }
        } catch (Exception ex) {
            result.put("status", "failed");
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return Results.ok(result);
    }

    @Transactional
    public Result getBomTxnList(Request request) {
        Users user = getUserInfo(request);
        if (user == null) {
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        JsonNode json = request.body().asJson();
        ArrayNode bomList = result.putArray("bomList");
        try {
            Long txnforBranch = (json.findValue("txnForBranch") == null || "".equals(json.findValue("txnForBranch")))
                    ? 0L
                    : json.findValue("txnForBranch").asLong();
            String txnFromDate = json.findValue("txnFromDate") == null ? null : json.findValue("txnFromDate").asText();
            String txnToDate = json.findValue("txnToDate") == null ? null : json.findValue("txnToDate").asText();
            long txnPurposeId = (json.findValue("txnPurposeId") == null || "".equals(json.findValue("txnPurposeId")))
                    ? 0L
                    : json.findValue("txnPurposeId").asLong();
            List<BillOfMaterialTxnModel> bomTxns = null;

            if (txnPurposeId == IdosConstants.CREATE_PURCHASE_ORDER) {
                txnPurposeId = IdosConstants.CREATE_PURCHASE_REQUISITION;
            } else {
                txnPurposeId = IdosConstants.BILL_OF_MATERIAL;
            }
            if (txnFromDate != null && !"".equals(txnFromDate) && txnToDate != null && !"".equals(txnToDate)) {
                Date fromDate = IdosConstants.MYSQLDTF
                        .parse(IdosConstants.MYSQLDTF.format(IdosConstants.IDOSDTF.parse(txnFromDate + " 00:00:00")));
                Date toDate = IdosConstants.MYSQLDTF
                        .parse(IdosConstants.MYSQLDTF.format(IdosConstants.IDOSDTF.parse(txnToDate + " 23:59:59")));
                bomTxns = BillOfMaterialTxnModel.findByOrgBranchBomTxnByDateRange(em, user.getOrganization().getId(),
                        txnforBranch, txnPurposeId, IdosConstants.TXN_STATUS_ACCOUNTED,
                        IdosConstants.UN_FULFILLED_TRANACTION, fromDate, toDate);
            } else {
                bomTxns = BillOfMaterialTxnModel.findByOrgBranchBomTransaction(em, user.getOrganization().getId(),
                        txnforBranch, txnPurposeId, IdosConstants.TXN_STATUS_ACCOUNTED,
                        IdosConstants.UN_FULFILLED_TRANACTION);
            }
            for (BillOfMaterialTxnModel txn : bomTxns) {
                ObjectNode itemRow = Json.newObject();
                itemRow.put("id", txn.getId());
                itemRow.put("name", txn.getTransactionRefNumber());
                bomList.add(itemRow);
            }
        } catch (Exception ex) {
            result.put("status", "failed");
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return Results.ok(result);
    }

    @Transactional
    public Result getBomTxnDetail(final Long entityid, final Long branchId, final Long vendorId,
            final Integer ischeck, Request request) {
        log.log(Level.FINE, " Start getBomTxnDetail " + branchId + " " + vendorId + " " + ischeck);
        Users user = getUserInfo(request);
        if (user == null) {
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        ArrayNode bomItemlist = result.putArray("bomItemlist");
        try {
            // EntityManager em = getEntityManager();
            BillOfMaterialTxnModel bomTransaction = BillOfMaterialTxnModel.findById(entityid);
            result.put("id", bomTransaction.getId());
            result.put("branchId", bomTransaction.getBranch().getId());
            if (bomTransaction.getIncome() != null)
                result.put("incomeId", bomTransaction.getIncome().getId());
            else
                result.put("incomeId", "");
            if (bomTransaction.getProject() != null) {
                result.put("projectId", bomTransaction.getProject().getId());
            } else {
                result.put("projectId", "");
            }
            if (bomTransaction.getCustomerVendor() != null)
                result.put("customerId", bomTransaction.getCustomerVendor().getId());
            else
                result.put("customerId", "");
            result.put("remark", bomTransaction.getRemarks());
            result.put("documents", bomTransaction.getSupportingDocs());
            if (bomTransaction.getTotalNetAmount() != null)
                result.put("totalNet", bomTransaction.getTotalNetAmount());
            else
                result.put("totalNet", "");
            List<BillOfMaterialTxnItemModel> bomTxnItems = bomTransaction.getBillOfMaterialTxnItemModels();
            for (BillOfMaterialTxnItemModel bomTxnItem : bomTxnItems) {
                if (ischeck == 1 && bomTxnItem.getVendor() != null
                        && bomTxnItem.getVendor().getId().compareTo(vendorId) != 0
                        && bomTxnItem.getBranch().getId().compareTo(branchId) != 0) {
                    continue;
                }
                ObjectNode row = Json.newObject();
                row.put("entityId", bomTxnItem.getId());
                row.put("expenseId", bomTxnItem.getExpense().getId());
                row.put("unitOfMeasure", bomTxnItem.getMeasureName());
                if (bomTxnItem.getNoOfUnits() != null) {
                    row.put("noOfUnit", bomTxnItem.getNoOfUnits());
                } else {
                    row.put("noOfUnit", "");
                }
                if (bomTxnItem.getVendor() != null) {
                    row.put("vendorid", bomTxnItem.getVendor().getId());
                } else {
                    row.put("vendorid", "");
                }
                row.put("oem", bomTxnItem.getOem());
                row.put("tom", bomTxnItem.getTypeOfMaterial());
                row.put("knowledge", bomTxnItem.getKlfollowStatus());
                if (bomTxnItem.getAvailableUnits() != null)
                    row.put("availableUnits", bomTxnItem.getAvailableUnits());
                else
                    row.put("availableUnits", "");

                if (bomTxnItem.getOrderedUnits() != null)
                    row.put("orderedUnits", bomTxnItem.getOrderedUnits());
                else
                    row.put("orderedUnits", "");

                if (bomTxnItem.getCommittedUnits() != null)
                    row.put("committedUnits", bomTxnItem.getCommittedUnits());
                else
                    row.put("committedUnits", "");

                if (bomTxnItem.getNetUnits() != null)
                    row.put("netUnits", bomTxnItem.getNetUnits());
                else
                    row.put("netUnits", "");

                if (bomTxnItem.getPricePerUnit() != null)
                    row.put("pricePerUnit", bomTxnItem.getPricePerUnit());
                else
                    row.put("pricePerUnit", "");

                if (bomTxnItem.getTotalPrice() != null)
                    row.put("totalPrice", bomTxnItem.getTotalPrice());
                else
                    row.put("totalPrice", "");

                double unfulfilledUnits = bomTxnItem.getNoOfUnits() == null ? 0.0 : bomTxnItem.getNoOfUnits();
                double fulfilledUnits = bomTxnItem.getFulfilledUnits() == null ? 0.0 : bomTxnItem.getFulfilledUnits();
                if (unfulfilledUnits > fulfilledUnits) {
                    unfulfilledUnits = unfulfilledUnits - fulfilledUnits;
                }
                row.put("unfulfilledUnits", unfulfilledUnits);

                bomItemlist.add(row);
            }
            result.put("status", "success");
        } catch (Exception ex) {
            result.put("status", "failed");
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        log.log(Level.FINE, " End getBomTxnDetail" + result);
        return Results.ok(result);
    }

    @Transactional
    public Result getUnfulfilledUnitsOfItem(final long entityid, final long branchid, Request request) {
        Users user = getUserInfo(request);
        if (user == null) {
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        // EntityManager em = getEntityManager();
        ObjectNode result = Json.newObject();
        try {
            double total = BILL_OF_MATERIAL_TXN_SERVICE.getPurchaseOrderUnfulfilledUnits(user, em, entityid, branchid);
            result.put("total", total);
            result.put("status", "success");
        } catch (Exception ex) {
            result.put("status", "failed");
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return Results.ok(result);
    }
}
