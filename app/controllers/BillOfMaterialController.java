package controllers;

import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import play.mvc.Http.Request;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.db.jpa.JPAApi;
import play.Application;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import java.util.logging.Level;

/**
 * @author Sunil K. Namdev created on 29.01.2019
 */
public class BillOfMaterialController extends StaticController {
    private static JPAApi jpaApi;
    private static EntityManager em;
    public static Application application;
    // public Request request;

    @Inject
    public BillOfMaterialController(Application application) {
        super(application);
        em = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result save(Request request) {
        Users user = getUserInfo(request);
        if (user == null) {
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        // EntityManager em = getEntityManager();
        ObjectNode result = Json.newObject();
        EntityTransaction entityTransaction = em.getTransaction();
        try {
            JsonNode json = request.body().asJson();
            Map<String, String> bomDetailMap = null;
            long billOFMaterialId = json.findValue("billOFMaterialId") == null ? 0L
                    : json.findValue("billOFMaterialId").asLong();
            entityTransaction.begin();
            if (billOFMaterialId <= 0L) {
                bomDetailMap = BILL_OF_MATERIAL_SERVICE.add(json, user, em);
                result.put("status", "added");
            } else {
                bomDetailMap = BILL_OF_MATERIAL_SERVICE.update(json, user, em, billOFMaterialId);
                result.put("status", "updated");
            }
            entityTransaction.commit();
            if (bomDetailMap != null) {
                ObjectMapper mapper = new ObjectMapper();
                final JsonNode jsonRes = mapper.convertValue(bomDetailMap, JsonNode.class);
                result.put("bomlist", jsonRes);
            } else {
                result.put("status", "failed");
            }
        } catch (Exception ex) {
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            result.put("status", "failed");
        }
        return Results.ok(result);
    }

    @Transactional
    public Result getByOrg(Request request) {
        Users user = getUserInfo(request);
        if (user == null) {
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        try {
            // EntityManager em = getEntityManager();
            List<BillOfMaterialModel> billOfMaterialModelList = BILL_OF_MATERIAL_SERVICE.getByOrg(user, em);
            ArrayNode bomlist = result.putArray("bomlist");
            for (BillOfMaterialModel billOfMaterial : billOfMaterialModelList) {
                ObjectNode row = Json.newObject();
                row.put("entityId", billOfMaterial.getId());
                row.put("branch", billOfMaterial.getBranch().getName());
                if (billOfMaterial.getProject() != null) {
                    row.put("project", billOfMaterial.getProject().getName());
                } else {
                    row.put("project", "");
                }
                row.put("income", billOfMaterial.getIncome().getName());
                bomlist.add(row);
            }
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

    @Transactional
    public Result getDetail(final long entityid, Request request) {
        Users user = getUserInfo(request);
        if (user == null) {
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        try {
            // EntityManager em = getEntityManager();
            BillOfMaterialModel billOfMaterial = BillOfMaterialModel.findById(entityid);
            result.put("branchid", billOfMaterial.getBranch().getId());
            result.put("incomeid", billOfMaterial.getIncome().getId());
            if (billOfMaterial.getProject() != null)
                result.put("projectid", billOfMaterial.getProject().getId());
            else
                result.put("projectid", "");
            result.put("bomid", billOfMaterial.getId());
            ArrayNode bomlist = result.putArray("bomlist");
            for (BillOfMaterialItemModel billOfMaterialItem : billOfMaterial.getBillOfMaterialItems()) {
                ObjectNode row = Json.newObject();
                row.put("entityId", billOfMaterialItem.getId());
                row.put("expenseId", billOfMaterialItem.getExpense().getId());
                row.put("unitOfMeasure", billOfMaterialItem.getMeasureName());
                if (billOfMaterialItem.getNoOfUnits() != null) {
                    row.put("noOfUnit", billOfMaterialItem.getNoOfUnits());
                } else {
                    row.put("noOfUnit", "");
                }
                row.put("vendorid", billOfMaterialItem.getVendor().getId());
                row.put("oem", billOfMaterialItem.getOem());
                row.put("tom", billOfMaterialItem.getTypeOfMaterial());
                row.put("knowledge", billOfMaterialItem.getKnowledgeLib());
                bomlist.add(row);
            }
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

    @Transactional
    public Result getBillOfMaterialIncomeItemsByBranch(Request request) {
        Users user = getUserInfo(request);
        if (user == null) {
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        JsonNode json = request.body().asJson();
        ArrayNode incomeItems = result.putArray("incomeItems");
        Map<String, Object> criterias = new HashMap<String, Object>();
        Long branchId = (null == json.findValue("branchId").asText() || "".equals(json.findValue("branchId").asText()))
                ? 0
                : json.findValue("branchId").asLong();

        try {
            // EntityManager em = getEntityManager();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("branch.id", branchId);
            criterias.put("presentStatus", 1);
            List<BillOfMaterialModel> billOfMaterialList = genericDAO.findByCriteria(BillOfMaterialModel.class,
                    criterias, em);
            for (BillOfMaterialModel billOfMaterialModel : billOfMaterialList) {
                ObjectNode itemRow = Json.newObject();
                itemRow.put("id", billOfMaterialModel.getIncome().getId());
                itemRow.put("name", billOfMaterialModel.getIncome().getName());
                itemRow.put("bomid", billOfMaterialModel.getId());
                incomeItems.add(itemRow);
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
    public Result getBillOfMaterialByIncomeItem(final long branchId, final long incomeId, Request request) {
        Users user = getUserInfo(request);
        if (user == null) {
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        ObjectNode result = Json.newObject();
        try {
            // EntityManager em = getEntityManager();
            List<BillOfMaterialItemModel> billOfMaterialItemsList = BILL_OF_MATERIAL_SERVICE.getByIncomeAndBranch(user,
                    em, branchId, incomeId);
            ArrayNode bomItemlist = result.putArray("bomItemlist");
            for (BillOfMaterialItemModel billOfMaterialItem : billOfMaterialItemsList) {
                ObjectNode row = Json.newObject();
                row.put("entityId", billOfMaterialItem.getId());
                row.put("expenseId", billOfMaterialItem.getExpense().getId());
                row.put("unitOfMeasure", billOfMaterialItem.getMeasureName());
                if (billOfMaterialItem.getNoOfUnits() != null) {
                    row.put("noOfUnit", billOfMaterialItem.getNoOfUnits());
                } else {
                    row.put("noOfUnit", "");
                }
                row.put("vendorid", billOfMaterialItem.getVendor().getId());
                row.put("oem", billOfMaterialItem.getOem());
                row.put("tom", billOfMaterialItem.getTypeOfMaterial());
                row.put("knowledge", billOfMaterialItem.getKnowledgeLib());
                Double availableStock = stockService.buyInventoryStockAvailable(result,
                        billOfMaterialItem.getExpense().getId(), branchId, user, em);
                row.put("availableStock", availableStock);
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
        return Results.ok(result);
    }
}
