package controllers;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;

import model.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import play.Application;

/**
 * Created by Sunil Namdev on 26-06-2017.
 */
public class GstTaxController extends StaticController {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    public static Application application;
    private Request request;
    // private Http.Session session = request.session();

    @Inject
    public GstTaxController(Application application) {
        super(application);
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result getItemTaxDetail(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager=getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode itemTaxList = result.putArray("itemTaxList");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            // String useremail=json.findValue("usermail").asText();
            Long branchId = json.findValue("branchPrimaryId").asLong();
            String specificsId = json.findValue("specificsPrimId").asText();
            Integer taxCategory = json.findValue("taxCategory").asInt();
            Specifics specificsentity = Specifics.findById(IdosUtil.convertStringToLong(specificsId));
            if (specificsentity != null && branchId != null) {
                result.put("id", specificsentity.getId());
                result.put("itemCategory", specificsentity.getGstTypeOfSupply());
                if (specificsentity.getGstTaxRate() != null) {
                    result.put("rate", specificsentity.getGstTaxRate().toString());
                } else {
                    result.put("rate", "");
                }
                if (specificsentity.getCessTaxRate() != null) {
                    result.put("cessRate", specificsentity.getCessTaxRate().toString());
                } else {
                    result.put("cessRate", "");
                }

                result.put("hsnSacCode", specificsentity.getGstItemCode());
                result.put("specificsname", specificsentity.getName());

                if (specificsentity.getGstTaxRate() != null) {
                    Double otherRates = specificsentity.getGstTaxRate() / 2;
                    result.put("sgstRate", otherRates);
                    result.put("cgstRate", otherRates);
                    result.put("igstRate", specificsentity.getGstTaxRate());
                } else {
                    result.put("sgstRate", 0);
                    result.put("cgstRate", 0);
                    result.put("igstRate", 0);
                }
            }
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("presentStatus", 1);
            criterias.put("branch.id", branchId);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("specifics.id", specificsentity.getId());
            // criterias.put("gstItemCode", specificsentity.getGstItemCode());
            // criterias.put("gstItemCategory", specificsentity.getGstTypeOfSupply());
            criterias.put("gstTaxRate", specificsentity.getGstTaxRate());
            criterias.put("presentStatus", 1);
            List<BranchSpecificsTaxFormula> formulaList = genericDAO.findByCriteria(BranchSpecificsTaxFormula.class,
                    criterias, entityManager);
            if (formulaList == null || formulaList.isEmpty()) {
                result.put("allowAddNew", 1);
            } else {
                result.put("allowAddNew", 0);
            }
            Date taxApplicableDate = BranchSpecificsTaxFormula.findLatestApplicableDate(entityManager,
                    user.getOrganization().getId(), branchId, specificsentity.getId());
            Date txnAppliedDate = Transaction.getAppliedDateForCOA(entityManager, user.getOrganization().getId(),
                    branchId, specificsentity.getId());
            if (taxApplicableDate != null && txnAppliedDate != null) {
                int dateResult = taxApplicableDate.compareTo(txnAppliedDate);
                if (dateResult > 0) { // taxApplicableDate 1 max
                    result.put("applicableDate", IdosConstants.IDOSDF.format(taxApplicableDate));
                } else if (dateResult < 0) { // txnAppliedDate max
                    result.put("applicableDate", IdosConstants.IDOSDF.format(txnAppliedDate));
                } else { // Both Equal
                    result.put("applicableDate", IdosConstants.IDOSDF.format(txnAppliedDate));
                }
            } else if (taxApplicableDate != null) {
                result.put("applicableDate", IdosConstants.IDOSDF.format(taxApplicableDate));
            } else if (txnAppliedDate != null) {
                result.put("applicableDate", IdosConstants.IDOSDF.format(txnAppliedDate));
            } else {
                result.put("applicableDate", "");
            }
            HashMap<String, String> gstDetailMap = new HashMap<String, String>();
            criterias.clear();
            criterias.put("branch.id", branchId);
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("specifics.id", specificsentity.getId());
            criterias.put("presentStatus", 1);
            List<BranchSpecificsTaxFormula> taxFormulaList = genericDAO.findByCriteria(BranchSpecificsTaxFormula.class,
                    criterias, entityManager);
            String gstItemCode = null;
            String gstItemCategory = null;
            Double gstTaxRate = 0.0;
            for (BranchSpecificsTaxFormula branchSpecificsTaxFormula : taxFormulaList) {
                gstItemCode = branchSpecificsTaxFormula.getGstItemCode();
                gstItemCategory = branchSpecificsTaxFormula.getGstItemCategory();
                gstTaxRate = branchSpecificsTaxFormula.getGstTaxRate();
                String key = gstItemCode + gstItemCategory + gstTaxRate;
                ObjectNode row = Json.newObject();

                if (!gstDetailMap.containsKey(key)) {
                    gstDetailMap.put(key, gstItemCode);
                    row.put("itemName", specificsentity.getName());
                    row.put("gstItemCode", gstItemCode);
                    row.put("gstItemCategory", gstItemCategory);
                    row.put("gstTaxRate", gstTaxRate);
                }

                BranchTaxes branchTax = BranchTaxes.findById(branchSpecificsTaxFormula.getBranchTaxes().getId());
                if (branchTax != null) {
                    ArrayNode branchTaxList = row.putArray("branchTaxList");
                    ObjectNode row1 = Json.newObject();
                    row1.put("id", branchTax.getId());
                    row1.put("date", "" + IdosConstants.IDOSDF.format(branchSpecificsTaxFormula.getApplicableFrom()));
                    row1.put("taxName", branchTax.getTaxName());
                    row1.put("status", branchTax.getPresentStatus());
                    branchTaxList.add(row1);
                }
                itemTaxList.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result saveUpdateItemsTaxDetail(Request request) {
        // EntityManager entityManager=getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        result.put("rescode", 0);
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start " + json);
            transaction.begin();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            String branchId = json.findValue("branchId").asText();
            int taxCategory = json.findValue("taxCategory").asInt();
            if (taxCategory == IdosConstants.RCM_INPUT_TAX) {
                taxService.saveUpdateBranchTax(branchId, json, new Integer(IdosConstants.RCM_INPUT_TAX), entityManager,
                        user);
                taxService.saveUpdateBranchTax(branchId, json, new Integer(IdosConstants.RCM_OUTPUT_TAX), entityManager,
                        user);
            } else {
                taxService.saveUpdateBranchTax(branchId, json, taxCategory, entityManager, user);
            }

            String specificsId = json.findValue("specificsId").asText();
            taxService.applyTaxRulesToEachBranchSpecifics(specificsId, branchId, json, entityManager, user); // apply
                                                                                                             // tax rule
                                                                                                             // to that
                                                                                                             // item for
                                                                                                             // which
                                                                                                             // rules
                                                                                                             // are set,
                                                                                                             // single
                                                                                                             // item

            String multiItemsSpecificsId = json.findValue("applyRulesToMultiItemsList").asText();
            String[] itemsList = null;
            if (multiItemsSpecificsId != null && multiItemsSpecificsId != "") {
                itemsList = multiItemsSpecificsId.split(",");
            }
            if (itemsList != null) { // if multiple items are selected to apply same tax rules
                // for(int x=0;x<itemsList.length;x++){
                // specificsId=itemsList[x];
                taxService.applyTaxRulesToMultipleBranchSpecifics(specificsId, branchId, json, entityManager, user);
                // }
            } /*
               * else{ //single item
               * taxService.applyTaxRulesToEachBranchSpecifics(specificsId, branchId, json,
               * entityManager, user);
               * }
               */
            transaction.commit();
        } catch (IDOSException ex) {
            // log.log(Level.SEVERE, ex.getMessage());
            if (ex.getErrorDescription().equals("Mapping between Branch and Item is missing!")) {
                result.put("message", "Mapping between Branch and Item is missing!");
                result.put("rescode", ex.getErrorCode());
                return Results.ok(result);
            }
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));

        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result searchGSTItemBasedOnDesc(Request request) {
        // EntityManager entityManager=getEntityManager();
        ObjectNode results = Json.newObject();
        Users usrinfo = null;
        try {
            JsonNode json = request.body().asJson();
            ArrayNode gstItemsan = results.putArray("gstItemsData");
            usrinfo = getUserInfo(request);
            Integer gstTypeOfSupply = json.findValue("gstTypeOfSupply") != null
                    ? json.findValue("gstTypeOfSupply").asInt()
                    : null;
            String itemDesc = json.findValue("itemDesc") != null ? json.findValue("itemDesc").asText() : "";
            itemDesc = itemDesc.toUpperCase();
            if (gstTypeOfSupply != null && gstTypeOfSupply == IdosConstants.GST_SERVICES) {
                String newsbquery = "select obj from GSTServicesCodes obj WHERE obj.serviceDescription like ? and obj.presentStatus=1";
                ArrayList inparam = new ArrayList(1);
                inparam.add("%" + itemDesc + "%");
                List<GSTServicesCodes> gstCodes = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
                for (GSTServicesCodes gstCode : gstCodes) {
                    ObjectNode row = Json.newObject();
                    row.put("id", gstCode.getId());
                    row.put("label", gstCode.getServiceDescription());
                    row.put("code", gstCode.getServiceCode());
                    row.put("rate", gstCode.getTaxRate());
                    gstItemsan.add(row);
                }
            } else {
                String newsbquery = "select obj from GSTGoodsCodes obj WHERE obj.goodsDescription like ? and obj.presentStatus=1";
                ArrayList inparam = new ArrayList(1);
                inparam.add("%" + itemDesc + "%");
                List<GSTGoodsCodes> gstGoodsCodes = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
                for (GSTGoodsCodes gstCode : gstGoodsCodes) {
                    ObjectNode row = Json.newObject();
                    row.put("id", gstCode.getId());
                    row.put("label", gstCode.getGoodsDescription());
                    row.put("code", gstCode.getGoodsCode());
                    row.put("rate", gstCode.getTaxRate());
                    gstItemsan.add(row);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            ex.printStackTrace();
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(results).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result getInputTaxList4Branch(Long branchId, Request request) {
        log.log(Level.FINE, ">>>> Start " + branchId);
        // EntityManager entityManager=getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode itemTaxList = result.putArray("itemTaxList");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            List<BranchTaxes> inputTaxes = BranchTaxes.findInputTaxByBranch(entityManager, branchId,
                    user.getOrganization().getId());
            for (BranchTaxes branchTaxes : inputTaxes) {
                ObjectNode row = Json.newObject();
                row.put("id", branchTaxes.getId());
                row.put("taxname", branchTaxes.getTaxName());
                row.put("rate", branchTaxes.getTaxRate());
                itemTaxList.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result saveInputTaxBranch(Request request) {
        // EntityManager entityManager=getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start " + json);
            transaction.begin();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            taxService.saveInputTaxBranch(json, entityManager, user);
            transaction.commit();
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result saveRcmTaxBranch(Request request) {
        // EntityManager entityManager=getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start " + json);
            transaction.begin();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            taxService.saveRcmTaxBranch(json, entityManager, user);
            transaction.commit();
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result getRcmTaxList4Branch(Request request) {
        log.log(Level.FINE, ">>>> Start ");
        // EntityManager entityManager=getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode itemTaxList = result.putArray("itemTaxList");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            List<BranchTaxes> inputTaxes = BranchTaxes.findRcmTaxByOrg(entityManager, user.getOrganization().getId());
            for (BranchTaxes branchTaxes : inputTaxes) {

                List<BranchSpecificsTaxFormula> formulaList = BranchSpecificsTaxFormula.findByOrgList(entityManager,
                        user.getOrganization().getId(), branchTaxes.getId());
                if (formulaList != null && !formulaList.isEmpty()) {
                    for (BranchSpecificsTaxFormula formula : formulaList) {
                        ObjectNode row = Json.newObject();
                        row.put("id", branchTaxes.getId());
                        row.put("date", IdosConstants.mysqldf.format(branchTaxes.getCreatedAt()));
                        row.put("taxname", branchTaxes.getTaxName());
                        row.put("rate", branchTaxes.getTaxRate());
                        if ("1".equals(formula.getGstItemCategory())) {
                            row.put("supplyType", "Goods");
                        } else {
                            row.put("supplyType", "Services");
                        }
                        row.put("desc", formula.getHsnDesc());
                        row.put("branch", formula.getBranch().getName());

                        row.put("itemName", formula.getSpecifics().getName());
                        row.put("branchSpecificsTaxFormulaId", formula.getId());
                        itemTaxList.add(row);
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result getGstInTaxesCess4Branch(Long branchId, Request request) {
        log.log(Level.FINE, ">>>> Start " + branchId);
        // EntityManager entityManager=getEntityManager();
        ObjectNode result = null;
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            result = taxService.getGstInTaxesCess4Branch(branchId, entityManager, user);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result getBranchTaxOpeningBalance(Long branchTaxId, Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start >>" + branchTaxId);
        ObjectNode result = Json.newObject();
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = getUserInfo(request);
        if (user == null) {
            return unauthorized();
        }
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            BranchTaxes branchTax = BranchTaxes.findById(branchTaxId);
            if (branchTax != null) {
                result.put("id", branchTax.getId());
                result.put("name", branchTax.getTaxName());
                if (branchTax.getOpeningBalance() != null) {
                    result.put("openingBalance", branchTax.getOpeningBalance());
                } else {
                    result.put("openingBalance", "");
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result addOrUpdateTaxOpeningBalance(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager=getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users users = getUserInfo(request);
        if (users == null) {
            return unauthorized();
        }
        try {
            JsonNode json = request.body().asJson();
            Long id = json.findValue("id").asLong();
            Double openingBalance = json.findValue("openingBalance").asDouble();
            transaction.begin();
            BranchTaxes branchTax = BranchTaxes.findById(id);
            if (branchTax != null) {
                branchTax.setOpeningBalance(openingBalance);
                genericDAO.saveOrUpdate(branchTax, users, entityManager);
            }
            transaction.commit();
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);

    }

    @Transactional
    public Result getReverseChargeItemsforSpecific(Request request) {

        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        ArrayNode reverseChargeData = result.putArray("reverseChargeData");
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, ">>>> Start " + json);
            String useremail = json.findValue("useremail").asText();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            result = taxService.getRcmTaxesForSpecific(json, entityManager, user);

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return internalServerError(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End: " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result getRcmTax(Request request) {
        log.log(Level.FINE, ">>>> Start ");
        // EntityManager entityManager=getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode itemTaxList = result.putArray("itemTaxList");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            JsonNode json = request.body().asJson();
            Long rcmId = json.findValue("rcmId") != null ? json.findValue("rcmId").asLong() : null;
            Long rcmTaxFormulaId = json.findValue("rcmTaxFormulaId") != null
                    ? json.findValue("rcmTaxFormulaId").asLong()
                    : null;
            BranchSpecificsTaxFormula formula = null;
            BranchTaxes rcmTax = null;

            if (rcmId != null) {
                rcmTax = BranchTaxes.findById(rcmId);
            }
            if (rcmTaxFormulaId != null) {
                formula = BranchSpecificsTaxFormula.findById(rcmTaxFormulaId);
            }
            if (rcmTax != null && formula != null) {
                ObjectNode row = Json.newObject();
                row.put("id", rcmTax.getId());
                row.put("date", IdosConstants.mysqldf.format(rcmTax.getCreatedAt()));
                row.put("taxname", rcmTax.getTaxName());
                row.put("rate", rcmTax.getTaxRate());
                row.put("supplyType", formula.getGstItemCategory());
                row.put("desc", formula.getSpecifics().getGstDesc());
                row.put("branch", formula.getBranch().getId());
                row.put("vendor", formula.getVendor().getId());
                row.put("hsnsac", formula.getGstItemCode());
                row.put("itemName", formula.getSpecifics().getName());
                row.put("itemId", formula.getSpecifics().getId());
                itemTaxList.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result getReverseChargeTaxforTypeOfSupply(Request request) {

        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, ">>>> Start " + json);
            String useremail = json.findValue("useremail").asText();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            result = taxService.getRcmTaxesForSpecificTypeOfSupply(json, entityManager, user);

        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return internalServerError(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End: " + result);
        return Results.ok(result);
    }

}
