package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IdosConstants;
import model.Branch;
import model.BranchSpecifics;
import model.BranchTaxes;
import model.IdosIntegrationKey;
import model.Specifics;
import model.SpecificsTransactionPurpose;
import model.Transaction;
import model.TransactionItems;
import model.TransactionPurpose;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.mvc.Results;
import java.util.logging.Level;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;
import play.Application;
import javax.inject.Inject;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

import com.idos.util.UnauthorizedAPIAccessException;

public class StockWarehouseController extends StaticController {
    private static Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    // private Request request;
    // private Http.Session session = request.session();

    @Inject
    public StockWarehouseController(Application application) {
        super(application);
        this.application = application;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result displayDetailInventory(Http.Request request) {
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        // EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            String email = json.findValue("usermail").asText();
            if (null != email && !"".equals(email)) {
                session.adding("email", email);
                user = getUserInfo(request);
                stockService.getTxnLevelInventory(result, json, user, entityManager);
            }
        } catch (Exception ex) {
            // if (entitytransaction.isActive()) {
            // entitytransaction.rollback();
            // }
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result getMidInventory(Request request) {
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = getUserInfo(request);
        if (user == null) {
            return unauthorized();
        }
        try {
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start " + json);
            String email = json.findValue("usermail").asText();
            stockService.getMidInventory(result, json, user, entityManager);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result availableIncomeItemStock(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            entitytransaction.begin();
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            if (null != email && !"".equals(email)) {
                session.adding("email", email);
                user = getUserInfo(request);
                result = stockService.getItemPresentStock(result, json, user, entityManager, entitytransaction);
            }
            entitytransaction.commit();
        } catch (Exception ex) {
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result buyInventoryStockAvailable(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        // EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            if (null != email && !"".equals(email)) {
                session.adding("email", email);
                user = getUserInfo(request);
                long mappedExpenseItem = json.findValue("expenseSpecificsId") != null
                        ? json.findValue("expenseSpecificsId").asLong()
                        : 0L;
                long branchId = json.findValue("txnBranch") != null ? json.findValue("txnBranch").asLong() : 0L;
                stockService.buyInventoryStockAvailable(result, mappedExpenseItem, branchId, user, entityManager);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result branchSellStockAvailableCombSales(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        // EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            JsonNode json = request.body().asJson();
            stockService.branchSellStockAvailableCombSales(result, user, entityManager, json);
        } catch (Exception ex) {
            // if (entitytransaction.isActive()) {
            // entitytransaction.rollback();
            // }
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return internalServerError(errorPage.render(ex, errorList));
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result branchIncomeAvailableStock(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        // EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            JsonNode json = request.body().asJson();
            long incomeItem = json.findValue("incomeSpecificsId").asLong();
            long branch = json.findValue("branchId").asLong();
            long inputQty = json.findValue("inputQty").asLong();
            stockService.getBranchItemPresentStock(result, user, entityManager, branch, incomeItem);
        } catch (Exception ex) {
            // if (entitytransaction.isActive()) {
            // entitytransaction.rollback();
            // }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return internalServerError(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result displayPeriodicInventory(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            entitytransaction.begin();
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            if (null != email && !"".equals(email)) {
                session.adding("email", email);
                user = getUserInfo(request);
                result = stockService.getPeriodicInventoryInfo(result, json, user, entityManager, entitytransaction);
            }
            entitytransaction.commit();
        } catch (Exception ex) {
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return Results.ok(result);
    }

    @Transactional
    public Result exportPeriodicInventory(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        File file = null;
        try {
            entitytransaction.begin();
            // result.withHeader("ContentType","application/json");
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            String accessKey = json.findValue("accessKey").asText();
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("presentStatus", 1);
            criterias.put("authKey", accessKey);
            List<IdosIntegrationKey> authorizedProduct = genericDAO.findByCriteria(IdosIntegrationKey.class, criterias,
                    entityManager);
            String fname;
            if (!authorizedProduct.isEmpty() && authorizedProduct.size() > 0) {
                String path = application.path().toString() + "/logs/report/";
                File filepath = new File(path);
                if (!filepath.exists()) {
                    filepath.mkdir();
                }
                fname = stockService.exportPeriodicInventory(result, json, user, entityManager, entitytransaction,
                        path);
                // response.setContentType("application/pdf");
                path = path + fname;
                file = new File(path);
            } else {
                throw new UnauthorizedAPIAccessException();
            }
            entitytransaction.commit();
            return Results.ok(file).withHeader("Access-Control-Allow-Origin", "*").withHeader("Content-Disposition",
                    "attachment; filename=" + fname);
        } catch (Exception ex) {
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return null;
    }

    @Transactional
    public Result exportReportInventory(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        File file = null;
        String fileName = null;
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            entitytransaction.begin();
            // result.withHeader("ContentType","application/json");
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            String accessKey = json.findValue("accessKey").asText();
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("presentStatus", 1);
            criterias.put("authKey", accessKey);
            List<IdosIntegrationKey> authorizedProduct = genericDAO.findByCriteria(IdosIntegrationKey.class, criterias,
                    entityManager);
            if (!authorizedProduct.isEmpty() && authorizedProduct.size() > 0) {
                String path = application.path().toString() + "/logs/report/";
                File filepath = new File(path);
                if (!filepath.exists()) {
                    filepath.mkdir();
                }
                fileName = stockService.exportReportInventory(result, json, user, entityManager, entitytransaction,
                        path);
                // response.setContentType("application/pdf");
                path = path + fileName;
                file = new File(path);
            } else {
                throw new UnauthorizedAPIAccessException();
            }
            entitytransaction.commit();
        } catch (Exception ex) {
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        if (file != null) {
            return Results.ok(file).withHeader("Content-Disposition", "attachment; filename=" + fileName);
        } else {
            return Results.ok(result).withHeader("Access-Control-Allow-Origin", "*");
        }
    }

    @Transactional
    public Result listAllInventoryItems(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();
        result.put("result", false);
        ArrayNode an = result.putArray("inventoryItemsData");
        Users user = null;
        StringBuilder sbr = new StringBuilder("");
        TransactionPurpose txnPurpose = null;
        try {
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            String txnPurposeId = json.findValue("txnPurposeId") != null ? json.findValue("txnPurposeId").asText()
                    : null;
            if (txnPurposeId != null && !txnPurposeId.equals("")) {
                txnPurpose = TransactionPurpose.findById(Long.parseLong(txnPurposeId));
            }
            if (txnPurpose.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                if (null != email && !"".equals(email) && txnPurpose != null) {
                    session.adding("email", email);
                    user = getUserInfo(request);
                    sbr.append("select obj from SpecificsTransactionPurpose obj where obj.organization='"
                            + user.getOrganization().getId()
                            + "' and obj.specifics.particularsId.accountCode=2000000000000000000 and obj.transactionPurpose='"
                            + txnPurpose.getId() + "' and obj.presentStatus=1");
                    List<SpecificsTransactionPurpose> specfTxnPurposeList = genericDAO
                            .executeSimpleQuery(sbr.toString(), entityManager);
                    if (!specfTxnPurposeList.isEmpty() && specfTxnPurposeList.size() > 0) {
                        for (SpecificsTransactionPurpose invSpecf : specfTxnPurposeList) {
                            String sbr1 = ("select obj from Specifics obj where obj.particularsId.accountCode=1000000000000000000 and obj.organization='"
                                    + user.getOrganization().getId() + "' and obj.linkIncomeExpenseSpecifics='"
                                    + invSpecf.getSpecifics().getId() + "' and obj.presentStatus=1");
                            List<Specifics> invItem = genericDAO.executeSimpleQuery(sbr1, entityManager);
                            if (!invItem.isEmpty() && invItem.size() > 0) {
                                result.put("result", true);
                                ObjectNode row = Json.newObject();
                                row.put("id", invSpecf.getSpecifics().getId());
                                row.put("name", invSpecf.getSpecifics().getName());
                                an.add(row);
                            }
                        }
                    }
                }
            } else if (txnPurpose.getId() == 26) {
                if (null != email && !"".equals(email) && txnPurpose != null) {
                    session.adding("email", email);
                    user = getUserInfo(request);
                    sbr.append("select obj from Specifics obj where obj.organization='" + user.getOrganization().getId()
                            + "' and obj.particularsId.accountCode=2000000000000000000 and obj.presentStatus=1");
                    List<Specifics> specfTxnPurposeList = genericDAO.executeSimpleQuery(sbr.toString(), entityManager);
                    if (!specfTxnPurposeList.isEmpty() && specfTxnPurposeList.size() > 0) {
                        for (Specifics invSpecf : specfTxnPurposeList) {
                            sbr.delete(0, sbr.length());
                            sbr.delete(0, sbr.length());
                            sbr.append(
                                    "select obj from Specifics obj where obj.particularsId.accountCode=1000000000000000000 and obj.organization='"
                                            + user.getOrganization().getId() + "' and obj.linkIncomeExpenseSpecifics='"
                                            + invSpecf.getId() + "' and obj.presentStatus=1");
                            List<Specifics> invItem = genericDAO.executeSimpleQuery(sbr.toString(), entityManager);
                            if (!invItem.isEmpty() && invItem.size() > 0) {
                                result.put("result", true);
                                ObjectNode row = Json.newObject();
                                row.put("id", invSpecf.getId());
                                row.put("name", invSpecf.getName());
                                an.add(row);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return Results.ok(result);
    }

    @Transactional
    public Result inventoryStockTransferBranches(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        // EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        result.put("result", false);
        StringBuilder sbr = new StringBuilder("");
        ArrayNode an = result.putArray("inventoryBranchesData");
        Users user = null;
        Specifics invSpecf = null;
        try {
            // entitytransaction.begin();
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            String invItemId = json.findValue("invItemId") != null ? json.findValue("invItemId").asText() : null;
            String invOpeningBalance = json.findValue("invOpeningBalance") != null
                    ? json.findValue("invOpeningBalance").asText()
                    : null;
            if (invItemId != null && !invItemId.equals("")) {
                invSpecf = Specifics.findById(Long.parseLong(invItemId));
            }
            if (null != email && !"".equals(email) && invSpecf != null) {
                session.adding("email", email);
                user = getUserInfo(request);
                if (invOpeningBalance == null) {
                    // sbr.append("select obj from Transaction obj WHERE
                    // obj.transactionBranchOrganization='"+user.getOrganization().getId()+"' and
                    // obj.transactionSpecifics='"+invSpecf.getId()+"' AND (obj.transactionPurpose=3
                    // or obj.transactionPurpose=4 or obj.transactionPurpose=11) and
                    // obj.transactionStatus='Accounted' GROUP BY obj.transactionBranch");
                    // List<Transaction>
                    // itemStockInBranchesList=genericDAO.executeSimpleQuery(sbr.toString(),
                    // entityManager);
                    String QUERY_ITEMBRANCH_HQL = "select itemobj from TransactionItems itemobj WHERE itemobj.transactionSpecifics.id=?1 AND itemobj.transaction.id in (SELECT obj.id FROM Transaction obj WHERE obj.transactionBranchOrganization.id=?2 and obj.transactionPurpose in (3,4,11) and obj.transactionStatus='Accounted' and obj.presentStatus=1) group by branch.id";
                    ArrayList inparamList = new ArrayList(2);
                    inparamList.add(invSpecf.getId());
                    inparamList.add(user.getOrganization().getId());
                    List<TransactionItems> itemStockInBranchesList = genericDAO.queryWithParams(QUERY_ITEMBRANCH_HQL,
                            entityManager, inparamList);
                    if (!itemStockInBranchesList.isEmpty() && itemStockInBranchesList.size() > 0) {
                        result.put("result", true);
                        for (TransactionItems txnBnch : itemStockInBranchesList) {
                            ObjectNode row = Json.newObject();
                            row.put("id", txnBnch.getBranch().getId());
                            row.put("name", txnBnch.getBranch().getName());
                            an.add(row);
                        }
                    }
                }
                if (invOpeningBalance != null) {
                    sbr.append("select obj from BranchSpecifics obj WHERE obj.organization='"
                            + user.getOrganization().getId() + "' and obj.specifics='" + invSpecf.getId()
                            + "' and obj.presentStatus=1");
                    List<BranchSpecifics> itemStockInBranchesList = genericDAO.executeSimpleQuery(sbr.toString(),
                            entityManager);
                    if (!itemStockInBranchesList.isEmpty() && itemStockInBranchesList.size() > 0) {
                        result.put("result", true);
                        for (BranchSpecifics txnBnch : itemStockInBranchesList) {
                            ObjectNode row = Json.newObject();
                            row.put("id", txnBnch.getBranch().getId());
                            row.put("name", txnBnch.getBranch().getName());
                            an.add(row);
                        }
                    }
                }

            }
            // entitytransaction.commit();
        } catch (Exception ex) {
            // if (entitytransaction.isActive()) {
            // entitytransaction.rollback();
            // }
            System.out.println("Exception " + ex);
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return Results.ok(result);
    }

    @Transactional
    public Result inventoryToBranches(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        // EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        result.put("result", false);
        ArrayNode anToBranch = result.putArray("inventoryTransferToBranches");
        Users user = null;
        Specifics invSpecf = null;
        Branch branch = null;
        try {
            // entitytransaction.begin();
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            String invItemId = json.findValue("invItemId") != null ? json.findValue("invItemId").asText() : null;
            String invBranchId = json.findValue("invBranchId") != null ? json.findValue("invBranchId").asText() : null;
            if (invItemId != null && !invItemId.equals("")) {
                invSpecf = Specifics.findById(Long.parseLong(invItemId));
            }
            if (invBranchId != null && !invBranchId.equals("")) {
                branch = Branch.findById(Long.parseLong(invBranchId));
            }
            if (null != email && !"".equals(email) && invSpecf != null && branch != null) {
                user = getUserInfo(request);
                result.put("result", true);
                String BRANCH_HQL = "select obj from BranchSpecifics obj where obj.organization.id=?1 and obj.specifics.id=?2 and obj.presentStatus=1";
                ArrayList inparamList = new ArrayList(2);
                inparamList.add(user.getOrganization().getId());
                inparamList.add(invSpecf.getId());
                List<BranchSpecifics> branchesList = genericDAO.queryWithParams(BRANCH_HQL, entityManager, inparamList);
                for (BranchSpecifics branchSpec : branchesList) {
                    if (branchSpec.getBranch().getId() != branch.getId()) {
                        ObjectNode row1 = Json.newObject();
                        row1.put("id", branchSpec.getBranch().getId());
                        row1.put("name", branchSpec.getBranch().getName());
                        anToBranch.add(row1);
                    }
                }
            }
            // entitytransaction.commit();
        } catch (Exception ex) {
            // if (entitytransaction.isActive()) {
            // entitytransaction.rollback();
            // }
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return Results.ok(result);
    }

    @Transactional
    public Result inventoryStockInBranches(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        // EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        result.put("result", false);
        StringBuilder sbr = new StringBuilder("");
        ArrayNode an = result.putArray("inventoryItemBranchesStockData");
        Users user = null;
        Specifics invSpecf = null;
        Branch branch = null;
        try {
            // entitytransaction.begin();
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            String invItemId = json.findValue("invItemId") != null ? json.findValue("invItemId").asText() : null;
            String invBranchId = json.findValue("invBranchId") != null ? json.findValue("invBranchId").asText() : null;
            String elementId = json.findValue("elementId") != null ? json.findValue("elementId").asText() : null;
            if (invItemId != null && !invItemId.equals("")) {
                invSpecf = Specifics.findById(Long.parseLong(invItemId));
            }
            if (invBranchId != null && !invBranchId.equals("")) {
                branch = Branch.findById(Long.parseLong(invBranchId));
            }
            if (null != email && !"".equals(email) && invSpecf != null && branch != null) {
                session.adding("email", email);
                user = getUserInfo(request);
                sbr.append(
                        "select obj from Specifics obj where obj.particularsId.accountCode=1000000000000000000 and obj.organization='"
                                + user.getOrganization().getId() + "' and obj.linkIncomeExpenseSpecifics='"
                                + invSpecf.getId() + "' and obj.presentStatus=1");
                List<Specifics> invItem = genericDAO.executeSimpleQuery(sbr.toString(), entityManager);
                if (!invItem.isEmpty() && invItem.size() > 0) {
                    result.put("result", true);
                    Specifics incomeSpecifics = invItem.get(0);
                    Double purchaseStock = stockService.getPurchaseStockForThisIncomeLinkedExpenseItem(branch,
                            incomeSpecifics, user, entityManager);
                    Double sellStock = stockService.getSellStockForThisIncomeItem(branch, incomeSpecifics, user,
                            entityManager);
                    Double stockTransferred = stockService.getPurchaseStockTransferredItem(branch, invSpecf, user,
                            entityManager);
                    Double stockTransferInProgress = stockService.getPurchaseStockTransferredInProgressItem(branch,
                            invSpecf, user, entityManager);
                    Double stockAvailable = purchaseStock - sellStock - stockTransferred;
                    ObjectNode row = Json.newObject();
                    row.put("stockAvailable", stockAvailable);
                    row.put("stockTransferInProgress", stockTransferInProgress);
                    if (elementId.equals("invOpeningTxnForBranchesId")) {
                        Double openingStockInProgress = stockService.openingStockInProgress(branch, invSpecf, user,
                                entityManager);
                        row.put("openingStockInProgress", openingStockInProgress);
                    }
                    row.put("elementId", elementId);
                    an.add(row);
                }
            }
            // entitytransaction.commit();
        } catch (Exception ex) {
            // if (entitytransaction.isActive()) {
            // entitytransaction.rollback();
            // }
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return Results.ok(result);
    }

    @Transactional
    public Result displayInventoryReport(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            entitytransaction.begin();
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            if (null != email && !"".equals(email)) {
                session.adding("email", email);
                user = getUserInfo(request);
                result = stockService.getReportInventoryInfo(result, json, user, entityManager, entitytransaction);
            }
            entitytransaction.commit();
        } catch (Exception ex) {
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return Results.ok(result);
    }

    @Transactional
    public Result displayAllInventoryReport(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        // EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            // entitytransaction.begin();
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (null != user) {
                result = stockService.getReportAllInventoryInfo(result, json, user, entityManager);
            }
            // entitytransaction.commit();
        } catch (Exception ex) {
            // if (entitytransaction.isActive()) {
            // entitytransaction.rollback();
            // }
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result exportInventory(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        File file = null;
        String fileName = null;
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }

            // result.withHeader("ContentType","application/json");
            JsonNode json = request.body().asJson();
            // String email = json.findValue("email").asText();
            // String accessKey = json.findValue("accessKey").asText();
            // Map<String, Object> criterias = new HashMap<String, Object>();
            // criterias.put("presentStatus", 1);
            // criterias.put("authKey", accessKey);
            // List<IdosIntegrationKey> authorizedProduct =
            // genericDAO.findByCriteria(IdosIntegrationKey.class, criterias,
            // entityManager);
            // if(!authorizedProduct.isEmpty() && authorizedProduct.size()>0){
            String path = application.path().toString() + "/logs/report/";
            File filepath = new File(path);
            if (!filepath.exists()) {
                filepath.mkdir();
            }
            entitytransaction.begin();
            fileName = stockService.exportInventory(result, json, user, entityManager, entitytransaction, path);
            entitytransaction.commit();
            /*
             * if(fileName != null && fileName.endsWith("pdf")) {
             * response.setContentType("application/pdf");
             * }else{
             * result.withHeader("ContentType","application/xlsx");
             * }
             */
            path = path + fileName;
            file = new File(path);
            // }else{
            // throw new UnauthorizedAPIAccessException();
            // }

        } catch (Exception ex) {
            reportException(entityManager, entitytransaction, user, ex, result);
        } catch (Throwable th) {
            reportThrowable(entityManager, entitytransaction, user, th, result);
        }

        if (file != null) {
            return Results.ok(file).withHeader("Access-Control-Allow-Origin", "*").withHeader("Content-Disposition",
                    "attachment; filename=" + fileName);
        } else {
            return Results.ok(result).withHeader("Access-Control-Allow-Origin", "*");
        }
    }
}
