package controllers;

import com.idos.util.IdosConstants;
import com.idos.util.UnauthorizedAPIAccessException;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.Application;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
//import play.mvc.Http.Session;

public class TrialBalanceController extends StaticController {
    private Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    private Request request;
    // private Http.Session session = request.session();

    @Inject
    public TrialBalanceController(Application application) {
        super(application);
        this.application = application;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result getParticularsForOrg(Request request) {
        // EntityManager entityManager = getEntityManager();
        ObjectNode results = Json.newObject();
        Users usrinfo = null;
        try {
            usrinfo = getUserInfo(request);
            if (usrinfo == null) {
                return unauthorized();
            }
            JsonNode jsonRequest = request.body().asJson();
            String fmDate = jsonRequest.findValue("trialBalanceFromDate") != null
                    ? jsonRequest.findValue("trialBalanceFromDate").asText()
                    : null;
            String tDate = jsonRequest.findValue("trialBalanceToDate") != null
                    ? jsonRequest.findValue("trialBalanceToDate").asText()
                    : null;
            Long branchId = jsonRequest.findValue("trialBalanceForBranch") != null
                    ? jsonRequest.findValue("trialBalanceForBranch").asLong()
                    : null;
            ObjectNode json = Json.newObject();
            json.put("trialBalanceFromDate", fmDate);
            json.put("trialBalanceToDate", tDate);
            json.put("identForDataValid", "0");
            if (branchId != null) {
                json.put("trialBalanceForBranch", branchId);
            }
            ArrayNode partan = results.putArray("partData");
            ArrayNode coaSpecfChildData = results.putArray("coaSpecfChildData");
            List<Particulars> catList = Particulars.list(entityManager, usrinfo.getOrganization());
            Particulars liabilitiesParticular = null;
            if (catList.size() > 0) {
                TrialBalance tbIncome = new TrialBalance();
                TrialBalance tbExp = new TrialBalance();
                for (Particulars part : catList) {
                    if (part.getAccountCode() == 4000000000000000000L) {
                        liabilitiesParticular = part;
                        continue;
                    }
                    Double openingBalance = 0D;
                    Double debitAmount = 0D;
                    Double creditAmount = 0D;
                    Double closingBalance = 0D;
                    ObjectNode particular = Json.newObject();
                    ObjectNode row = Json.newObject();
                    row.put("id", part.getId());
                    row.put("name", part.getName());
                    row.put("accountCode", part.getAccountCode());
                    json.put("coaAccountCode", part.getAccountCode());
                    List<TrialBalance> tbList = TRIAL_BALANCE_SERVICE.displayTrialBalance(particular, json, usrinfo,
                            entityManager);
                    for (TrialBalance trialBalance : tbList) {
                        openingBalance += trialBalance.getOpeningBalance() == null ? 0d
                                : trialBalance.getOpeningBalance();
                        debitAmount += trialBalance.getDebit() == null ? 0d : trialBalance.getDebit();
                        creditAmount += trialBalance.getCredit() == null ? 0d : trialBalance.getCredit();
                        closingBalance += trialBalance.getClosingBalance() == null ? 0d
                                : trialBalance.getClosingBalance();
                    }

                    if (part.getAccountCode() == 1000000000000000000L) {
                        tbIncome.setOpeningBalance(openingBalance);
                        tbIncome.setDebit(debitAmount);
                        tbIncome.setCredit(creditAmount);
                        tbIncome.setClosingBalance(closingBalance);
                        row.put("openingBalance", "0.00");
                        row.put("closingBalance", "0.00");
                    } else if (part.getAccountCode() == 2000000000000000000L) {
                        tbExp.setOpeningBalance(openingBalance);
                        tbExp.setDebit(debitAmount);
                        tbExp.setCredit(creditAmount);
                        tbExp.setClosingBalance(closingBalance);
                        row.put("openingBalance", "0.00");
                        row.put("closingBalance", "0.00");
                    } else {
                        row.put("openingBalance", IdosConstants.decimalFormat.format(openingBalance));
                        row.put("closingBalance", IdosConstants.decimalFormat.format(closingBalance));
                    }
                    row.put("credit", IdosConstants.decimalFormat.format(creditAmount));
                    row.put("debit", IdosConstants.decimalFormat.format(debitAmount));
                    partan.add(row);
                }

                if (liabilitiesParticular != null) {
                    Double openingBalance = 0D;
                    Double debitAmount = 0D;
                    Double creditAmount = 0D;
                    Double closingBalance = 0D;
                    ObjectNode particular = Json.newObject();
                    ObjectNode row = Json.newObject();
                    row.put("id", liabilitiesParticular.getId());
                    row.put("name", liabilitiesParticular.getName());
                    row.put("accountCode", liabilitiesParticular.getAccountCode());
                    json.put("coaAccountCode", liabilitiesParticular.getAccountCode());
                    List<TrialBalance> tbList = TRIAL_BALANCE_SERVICE.displayTrialBalance(particular, json, usrinfo,
                            entityManager);
                    for (TrialBalance trialBalance : tbList) {
                        openingBalance += trialBalance.getOpeningBalance() == null ? 0d
                                : trialBalance.getOpeningBalance();
                        debitAmount += trialBalance.getDebit() == null ? 0d : trialBalance.getDebit();
                        creditAmount += trialBalance.getCredit() == null ? 0d : trialBalance.getCredit();
                        closingBalance += trialBalance.getClosingBalance() == null ? 0d
                                : trialBalance.getClosingBalance();
                    }
                    /*
                     * openingBalance += tbIncome.getOpeningBalance() - tbExp.getOpeningBalance();
                     * creditAmount += tbIncome.getCredit() - tbExp.getCredit();
                     * debitAmount += tbIncome.getDebit() - tbExp.getDebit();
                     * closingBalance += tbIncome.getClosingBalance() - tbExp.getClosingBalance();
                     * closingBalance += tbIncome.getClosingBalance() - tbExp.getClosingBalance();
                     */
                    row.put("openingBalance", IdosConstants.decimalFormat.format(openingBalance));
                    row.put("credit", IdosConstants.decimalFormat.format(creditAmount));
                    row.put("debit", IdosConstants.decimalFormat.format(debitAmount));
                    row.put("closingBalance", IdosConstants.decimalFormat.format(closingBalance));
                    partan.add(row);
                }

                /*
                 * Specifics specifics = coaService.getSpecificsForMapping(usrinfo, "67",
                 * entityManager);
                 * ObjectNode row = Json.newObject();
                 * if (specifics != null) {
                 * row.put("specId", specifics.getId());
                 * row.put("accountName", specifics.getName());
                 * row.put("accountCode", specifics.getAccountCode());
                 * row.put("specfaccountCode", specifics.getAccountCode());
                 * row.put("identificationForDataValid",
                 * specifics.getIdentificationForDataValid());
                 * row.put("headType", IdosConstants.HEAD_SPECIFIC);
                 * }else {
                 * row.put("specId", "-111");
                 * row.put("accountName", "Profit and Loss");
                 * row.put("accountCode", "4000000000000000000");
                 * row.put("specfaccountCode", "4000000000000000000");
                 * row.put("identificationForDataValid", "67");
                 * }
                 * row.put("topLevelAccountCode", "4000000000000000000");
                 * row.put("headType", IdosConstants.HEAD_SPECIFIC);
                 * row.put("headType2", "");
                 * row.put("openingBalance",
                 * IdosConstants.DECIMAL_FORMAT.format(tbIncome.getOpeningBalance() -
                 * tbExp.getOpeningBalance()));
                 * row.put("credit", "0.00");
                 * row.put("debit", "0.00");
                 * row.put("closingBalance",
                 * IdosConstants.DECIMAL_FORMAT.format(tbIncome.getOpeningBalance() -
                 * tbExp.getOpeningBalance()));
                 * coaSpecfChildData.add(row);
                 */
            }
        } catch (Exception ex) {
            reportException(entityManager, null, usrinfo, ex, results);
        } catch (Throwable th) {
            reportThrowable(entityManager, null, usrinfo, th, results);
        }
        log.log(Level.FINE, ">>>> End " + results);
        return Results.ok(results).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result downloadTrialBalance(Request request) throws Exception {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = null;
        File file = null;
        try {
            // result.withHeader("ContentType","application/json");
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            String accessKey = json.findValue("accessKey").asText();
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("presentStatus", 1);
            criterias.put("authKey", accessKey);
            List<IdosIntegrationKey> authorizedProduct = genericDAO.findByCriteria(IdosIntegrationKey.class, criterias,
                    entityManager);
            String fname = null;
            // if(!authorizedProduct.isEmpty() && authorizedProduct.size()>0){
            if (null != email && !"".equals(email)) {
                user = getUserInfo(request);
                if (user != null) {
                    String path = application.path().toString() + "/logs/TrialBalance/";
                    File filePath = new File(path);
                    if (!filePath.exists()) {
                        filePath.mkdir();
                    }
                    fname = TRIAL_BALANCE_SERVICE.downloadTrialBalance(result, json, user, entityManager, path);
                    if (fname != null) {
                        file = new File(path + fname);
                    }
                } else {
                    throw new UnauthorizedAPIAccessException();
                }
            }
            // }else{
            // throw new UnauthorizedAPIAccessException();
            // }
            return Results.ok(file).withHeader("Access-Control-Allow-Origin", "*")
                    .withHeader("ContentType", "application/xlsx")
                    .withHeader("Content-Disposition", "attachment; filename=" + fname);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return null;
    }

    @Transactional
    public Result trialBalance(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            String email = json.findValue("email").asText();
            String accessKey = json.findValue("accessKey").asText();
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("presentStatus", 1);
            criterias.put("authKey", accessKey);
            List<IdosIntegrationKey> authorizedProduct = genericDAO.findByCriteria(IdosIntegrationKey.class, criterias,
                    entityManager);
            if (!authorizedProduct.isEmpty() && authorizedProduct.size() > 0) {
                if (null != email && !"".equals(email)) {
                    session.adding("email", email);
                    user = getUserInfo(request);
                    if (user != null) {
                        TRIAL_BALANCE_SERVICE.displayTrialBalance(result, json, user, entityManager);
                    } else {
                        throw new UnauthorizedAPIAccessException();
                    }
                }
            } else {
                throw new UnauthorizedAPIAccessException();
            }
        } catch (Exception ex) {
            reportException(entityManager, entitytransaction, user, ex, result);
        } catch (Throwable th) {
            reportThrowable(entityManager, entitytransaction, user, th, result);
        }
        return Results.ok(result).withHeader("ContentType", "application/json")
                .withHeader("Access-Control-Allow-Origin", "*");
    }

    @Transactional
    public Result displayTrialBalance(Http.Request request) {

        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start " + json);
            String email = json.findValue("email").asText();
            if (null != email && !"".equals(email)) {
                session.adding("email", email);
                user = getUserInfo(request);
                TRIAL_BALANCE_SERVICE.displayTrialBalance(result, json, user, entityManager);
            }
        } catch (Exception ex) {
            reportException(entityManager, entitytransaction, user, ex, result);
        } catch (Throwable th) {
            reportThrowable(entityManager, entitytransaction, user, th, result);
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result).withHeader("ContentType", "application/json")
                .withHeader("Access-Control-Allow-Origin", "*");
    }

    @Transactional
    public Result exportTrialBalancePDF(Request request) throws Exception {
        log.log(Level.FINE, ">>>> Start");
        ObjectNode result = null;
        Users user = null;
        try {
            user = getUserInfo(request);
            JsonNode json = request.body().asJson();
            String from = json.findValue("fromDate").asText();
            String to = json.findValue("toDate").asText();
            long branchId = 0l; // "".equals(json.findValue("branchId").asText()) ? 0 :
                                // json.findValue("branchId").asLong();
            result = TRIAL_BALANCE_SERVICE.exportTrialBalancePDF(from, to, user, branchId);
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
    public Result displayTransactionsForHead(Http.Request request) {
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode result = null;
        Users user = null;
        String email = null;

        ObjectNode particular = Json.newObject();
        ArrayNode arr = null;
        ArrayList al = new ArrayList<>();
        try {
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start ");
            Long specificid = json.findValue("specificid") != null ? json.findValue("specificid").asLong() : 0;
            String toplevelaccountcode = json.findValue("toplevelaccountcode") != null
                    ? json.findValue("toplevelaccountcode").asText()
                    : null;
            email = session.getOptional("email").orElse(null);
            if (null != email && !"".equals(email) && specificid != 0) {
                user = getUserInfo(request);
                result = TRIAL_BALANCE_SERVICE.getTransactionForHead(user, entityManager, json);
            } else if (toplevelaccountcode != null) {
                user = getUserInfo(request);
                ObjectNode partjson = Json.newObject();
                partjson.put("trialBalanceFromDate", json.findValue("fromDate"));
                partjson.put("trialBalanceToDate", json.findValue("toDate"));
                partjson.put("identForDataValid", json.findValue("identForDataValid"));
                partjson.put("trialBalanceForBranch", json.findValue("trialBalBranch"));
                partjson.put("coaAccountCode", json.findValue("toplevelaccountcode"));

                List<TrialBalance> tbList = TRIAL_BALANCE_SERVICE.displayTrialBalance(particular, partjson, user,
                        entityManager);
                for (TrialBalance trialBalance : tbList) {
                    ObjectNode json1 = Json.newObject();
                    json1.put("specificid", trialBalance.getSpecId());
                    json1.put("headid2", trialBalance.getHeadid2());
                    json1.put("identForDataValid", trialBalance.getIdentificationForDataValid());
                    json1.put("toplevelaccountcode", trialBalance.getTopLevelAccountCode());
                    json1.put("fromDate", json.findValue("fromDate"));
                    json1.put("toDate", json.findValue("toDate"));
                    json1.put("headType", trialBalance.getHeadType());
                    json1.put("trialBalBranch", json.findValue("trialBalBranch"));

                    result = TRIAL_BALANCE_SERVICE.getTransactionForHead(user, entityManager, json1);

                    arr = (ArrayNode) result.get("itemTransData");
                    if (arr.isArray()) {
                        for (final JsonNode objNode : arr) {
                            al.add(objNode);
                        }
                    }
                }
                ObjectMapper jsonObjectMapper = new ObjectMapper();
                ArrayNode alArray = jsonObjectMapper.valueToTree(al);
                result.put("period", result.get("period"));
                result.put("itemTransData", alArray);
            }
        } catch (Exception ex) {
            reportException(entityManager, null, user, ex, result);
        } catch (Throwable th) {
            reportThrowable(entityManager, null, user, th, result);
        }
        log.log(Level.FINE, ">>>> End ");
        return Results.ok(result).withHeader("ContentType", "application/json")
                .withHeader("Access-Control-Allow-Origin", "*");
    }

    @Transactional
    public Result exportTrialBalanceLedger(Request request) {
        // EntityManager entityManager = getEntityManager();
        Users user = null;
        File file = null;
        try {
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start " + json);
            Long specificid = json.findValue("specificid") != null ? json.findValue("specificid").asLong() : 0;
            String toplevelaccountcode = json.findValue("toplevelaccountcode") != null
                    ? json.findValue("toplevelaccountcode").asText()
                    : null;
            user = getUserInfo(request);
            String fileName = null;
            String exportType = null;
            if (user == null) {
                return unauthorized();
            }
            if (specificid != 0 || toplevelaccountcode != null) {
                exportType = json.get("exportType").asText();
                String orgName = user.getOrganization().getName().replaceAll("\\s", "");
                if (orgName.length() > 4) {
                    orgName = orgName.substring(0, 4);
                }
                String path = application.path().toString() + "/logs/report/";
                File filepath = new File(path);
                if (!filepath.exists()) {
                    filepath.mkdir();
                }

                ByteArrayOutputStream out = TRIAL_BALANCE_LEDGER_SERVICE.exportTrialBalanceLedger(user, entityManager,
                        json, application);

                Long timeInMillis = Calendar.getInstance().getTimeInMillis();
                fileName = orgName + "_trialBalanceLedger" + timeInMillis + "." + exportType;
                path = path + fileName;
                file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                FileOutputStream fileOut = new FileOutputStream(path);
                out.writeTo(fileOut);
                fileOut.close();
            }
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, ">>>> End ");
            return Results.ok(file).withHeader("ContentType", "application/" + exportType)
                    .withHeader("Content-Disposition", "attachment; filename=" + fileName);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return null;
    }

}
