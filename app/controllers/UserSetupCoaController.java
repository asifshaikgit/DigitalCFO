package controllers;

import com.idos.cache.UserTxnRuleSetupCache;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import model.CoaLiabilities;
import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.omg.CORBA.Object;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import javax.inject.Inject;
import play.Application;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

/**
 * @auther Sunil Namdev created on 06.07.2018
 */
public class UserSetupCoaController extends StaticController {
    public static Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    private Request request;
    // private Http.Session session = request.session();

    @Inject
    public UserSetupCoaController(JPAApi jpaApi, Application application) {
        super(application);
        this.jpaApi = jpaApi;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static String LIAILITY_SRCH_JPQL = "select obj from CoaLiabilities obj where obj.organization.id=?1 and obj.name like ?2 and obj.presentStatus=1";

    private static void getUnsavedCoaList(String selectedUser, int ruleType, int particular, List<String> coaList) {
        String unsavedListStr = UserTxnRuleSetupCache.getCOA(selectedUser, ruleType, particular);
        if (unsavedListStr != null) {
            List<String> unsavedList = new ArrayList<String>(Arrays.asList(unsavedListStr.split(",")));
            if (unsavedList != null) {
                if (coaList != null) {
                    coaList.addAll(unsavedList);
                } else {
                    coaList = unsavedList;
                }
            }
        }
    }

    private static void getUnsavedFromAmountList(String selectedUser, int ruleType, int particular,
            List<String> amountList) {
        String unsavedListStr = UserTxnRuleSetupCache.getFromAmount(selectedUser, ruleType, particular);
        if (unsavedListStr != null) {
            List<String> unsavedList = new ArrayList<String>(Arrays.asList(unsavedListStr.split(",")));
            if (unsavedList != null) {
                if (amountList != null) {
                    amountList.addAll(unsavedList);
                } else {
                    amountList = unsavedList;
                }
            }
        }
    }

    private static void getUnsavedToAmountList(String selectedUser, int ruleType, int particular,
            List<String> amountList) {
        String unsavedListStr = UserTxnRuleSetupCache.getToAmount(selectedUser, ruleType, particular);
        if (unsavedListStr != null) {
            List<String> unsavedList = new ArrayList<String>(Arrays.asList(unsavedListStr.split(",")));
            if (unsavedList != null) {
                if (amountList != null) {
                    amountList.addAll(unsavedList);
                } else {
                    amountList = unsavedList;
                }
            }
        }
    }

    @Transactional
    public Result getCoaIncomeItems(int ruleType, String selectedUser, Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode incomeItemsArray = result.putArray("coaItemData");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users loggedUser = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            loggedUser = getUserInfo(request);
            if (loggedUser == null) {
                log.log(Level.SEVERE, "Error: unauthorized access.");
                return unauthorized();
            }
            if (selectedUser == null || "".equals(selectedUser)) {
                log.log(Level.SEVERE, "selected your email is not valid.");
                result.put("error", "Selected your email is not valid.");
            }
            Users user = Users.findActiveByEmail(selectedUser);
            List<String> coaList = null;
            List<String> fromAmtList = null;
            List<String> toAmountList = null;
            if (user != null) {
                if (ruleType == UserTxnRuleSetupCache.CREATOR) {
                    if (user.getTxnCreationCoaIncome() != null) {
                        String income = user.getTxnCreationCoaIncome();
                        coaList = new ArrayList<String>(Arrays.asList(income.split(",")));
                    }
                    if (user.getTxnCreationCoaIncomeFromAmount() != null) {
                        fromAmtList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaIncomeFromAmount().split(",")));
                    }
                    if (user.getTxnCreationCoaIncomeToAmount() != null) {
                        toAmountList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaIncomeToAmount().split(",")));
                    }
                } else if (ruleType == UserTxnRuleSetupCache.APPROVER) {
                    if (user.getTxnApprovalCoaIncome() != null) {
                        String income = user.getTxnApprovalCoaIncome();
                        coaList = new ArrayList<String>(Arrays.asList(income.split(",")));
                    }
                    if (user.getTxnApprovalCoaIncomeFromAmount() != null) {
                        fromAmtList = new ArrayList<String>(
                                Arrays.asList(user.getTxnApprovalCoaIncomeFromAmount().split(",")));
                    }
                    if (user.getTxnApprovalCoaIncomeToAmount() != null) {
                        toAmountList = new ArrayList<String>(
                                Arrays.asList(user.getTxnApprovalCoaIncomeToAmount().split(",")));
                    }
                } else if (ruleType == UserTxnRuleSetupCache.AUDITOR) {
                    if (user.getTxnAuditorCoaIncome() != null) {
                        String income = user.getTxnAuditorCoaIncome();
                        coaList = new ArrayList<String>(Arrays.asList(income.split(",")));
                    }
                }
            }
            getUnsavedCoaList(selectedUser, ruleType, UserTxnRuleSetupCache.INCOME_HEAD, coaList);
            getUnsavedFromAmountList(selectedUser, ruleType, UserTxnRuleSetupCache.INCOME_HEAD, fromAmtList);
            getUnsavedToAmountList(selectedUser, ruleType, UserTxnRuleSetupCache.INCOME_HEAD, toAmountList);

            List<Specifics> itemsList = coaService.getIncomesCoaChildNodes(entityManager, loggedUser);
            for (Specifics specf : itemsList) {
                if (specf.getIdentificationForDataValid() != null
                        && !"".equals(specf.getIdentificationForDataValid())) {
                    int mappid = Integer.parseInt(specf.getIdentificationForDataValid());
                    if (mappid != 8 && mappid != 51 && mappid != 64) {
                        continue;
                    }
                }
                IdosUtil.setUserItemsDetail(coaList, fromAmtList, toAmountList, specf.getId().toString(),
                        specf.getName(), IdosConstants.HEAD_SPECIFIC, incomeItemsArray);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, loggedUser.getEmail(), loggedUser.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result getCoaExpenseItems(int ruleType, String selectedUser, Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager=getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode expenseItemsArray = result.putArray("coaItemData");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users loggedUser = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            loggedUser = getUserInfo(request);
            if (loggedUser == null) {
                log.log(Level.SEVERE, "Error: unauthorized access.");
                return unauthorized();
            }
            if (selectedUser == null || "".equals(selectedUser)) {
                log.log(Level.SEVERE, "selected your email is not valid.");
                result.put("error", "Selected your email is not valid.");
            }
            Users user = Users.findActiveByEmail(selectedUser);
            List<String> coaList = null;
            List<String> fromAmtList = null;
            List<String> toAmountList = null;
            if (user != null) {
                if (ruleType == UserTxnRuleSetupCache.CREATOR) {
                    if (user.getTxnCreationCoaExpense() != null) {
                        String expense = user.getTxnCreationCoaExpense();
                        coaList = new ArrayList<String>(Arrays.asList(expense.split(",")));
                    }
                    if (user.getTxnCreationCoaExpenseFromAmount() != null) {
                        fromAmtList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaExpenseFromAmount().split(",")));
                    }
                    if (user.getTxnCreationCoaExpenseToAmount() != null) {
                        toAmountList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaExpenseToAmount().split(",")));
                    }
                } else if (ruleType == UserTxnRuleSetupCache.APPROVER) {
                    if (user.getTxnApprovalCoaExpense() != null) {
                        String expense = user.getTxnApprovalCoaExpense();
                        coaList = new ArrayList<String>(Arrays.asList(expense.split(",")));
                    }
                    if (user.getTxnApprovalCoaExpenseFromAmount() != null) {
                        fromAmtList = new ArrayList<String>(
                                Arrays.asList(user.getTxnApprovalCoaExpenseFromAmount().split(",")));
                    }
                    if (user.getTxnApprovalCoaExpenseToAmount() != null) {
                        toAmountList = new ArrayList<String>(
                                Arrays.asList(user.getTxnApprovalCoaExpenseToAmount().split(",")));
                    }
                } else if (ruleType == UserTxnRuleSetupCache.AUDITOR) {
                    if (user.getTxnAuditorCoaExpense() != null) {
                        String expense = user.getTxnAuditorCoaExpense();
                        coaList = new ArrayList<String>(Arrays.asList(expense.split(",")));
                    }
                }
            }
            getUnsavedCoaList(selectedUser, ruleType, UserTxnRuleSetupCache.EXPENSE_HEAD, coaList);
            getUnsavedFromAmountList(selectedUser, ruleType, UserTxnRuleSetupCache.EXPENSE_HEAD, fromAmtList);
            getUnsavedToAmountList(selectedUser, ruleType, UserTxnRuleSetupCache.EXPENSE_HEAD, toAmountList);

            List<Specifics> expenseItemsList = coaService.getExpensesCoaChildNodes(entityManager, loggedUser);
            for (Specifics specf : expenseItemsList) {
                if (specf.getIdentificationForDataValid() != null
                        && !"".equals(specf.getIdentificationForDataValid())) {
                    if (specf.getIdentificationForDataValid() != null
                            && !"".equals(specf.getIdentificationForDataValid())) {
                        int mappid = Integer.parseInt(specf.getIdentificationForDataValid());
                        if (mappid != 23 && mappid != 62 && mappid != 63) {
                            continue;
                        }
                    }
                }
                IdosUtil.setUserItemsDetail(coaList, fromAmtList, toAmountList, specf.getId().toString(),
                        specf.getName(), IdosConstants.HEAD_SPECIFIC, expenseItemsArray);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, loggedUser.getEmail(), loggedUser.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result getCoaAssetsItems(int ruleType, String selectedUser, Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager=getEntityManager();
        // ObjectNode result = Json.newObject();
        // ArrayNode coaItemData = result.putArray("coaItemData");
        ObjectNode result = null;
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users loggedUser = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            loggedUser = getUserInfo(request);
            if (loggedUser == null) {
                log.log(Level.SEVERE, "Error: unauthorized access.");
                return unauthorized();
            }
            if (selectedUser == null || "".equals(selectedUser)) {
                log.log(Level.SEVERE, "selected your email is not valid.");
                result.put("error", "Selected your email is not valid.");
            }
            Users user = Users.findActiveByEmail(selectedUser);
            result = USER_SETUP_SERVICE.getAssetsCoaChildNodesWithAllHeads(entityManager, loggedUser);
            ArrayNode assetsCOAAn = (ArrayNode) result.get("coaItemData");
            List<String> coaList = null;
            List<String> fromAmtList = null;
            List<String> toAmountList = null;
            if (user != null) {
                if (ruleType == UserTxnRuleSetupCache.CREATOR) {
                    if (user.getTxnCreationCoaAsset() != null) {
                        String expense = user.getTxnCreationCoaAsset();
                        coaList = new ArrayList<String>(Arrays.asList(expense.split(",")));
                    }
                    if (user.getTxnCreationCoaAssetFromAmount() != null) {
                        fromAmtList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaAssetFromAmount().split(",")));
                    }
                    if (user.getTxnCreationCoaAssetToAmount() != null) {
                        toAmountList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaAssetToAmount().split(",")));
                    }
                } else if (ruleType == UserTxnRuleSetupCache.APPROVER) {
                    if (user.getTxnApprovalCoaAsset() != null) {
                        String expense = user.getTxnApprovalCoaAsset();
                        coaList = new ArrayList<String>(Arrays.asList(expense.split(",")));
                    }
                    if (user.getTxnApprovalCoaAssetFromAmount() != null) {
                        fromAmtList = new ArrayList<String>(
                                Arrays.asList(user.getTxnApprovalCoaAssetFromAmount().split(",")));
                    }
                    if (user.getTxnApprovalCoaAssetToAmount() != null) {
                        toAmountList = new ArrayList<String>(
                                Arrays.asList(user.getTxnApprovalCoaAssetToAmount().split(",")));
                    }
                } else if (ruleType == UserTxnRuleSetupCache.AUDITOR) {
                    if (user.getTxnAuditorCoaAsset() != null) {
                        String expense = user.getTxnAuditorCoaAsset();
                        coaList = new ArrayList<String>(Arrays.asList(expense.split(",")));
                    }
                }
            }
            getUnsavedCoaList(selectedUser, ruleType, UserTxnRuleSetupCache.ASSETS_HEAD, coaList);
            getUnsavedFromAmountList(selectedUser, ruleType, UserTxnRuleSetupCache.ASSETS_HEAD, fromAmtList);
            getUnsavedToAmountList(selectedUser, ruleType, UserTxnRuleSetupCache.ASSETS_HEAD, toAmountList);

            for (int i = 0; i < assetsCOAAn.size(); i++) {
                ObjectNode row = (ObjectNode) assetsCOAAn.get(i);
                if (null != coaList) {
                    int index = coaList.indexOf(row.findValue("id").asText());
                    if (index != -1) {
                        row.put("isChecked", "checked");
                        if (fromAmtList != null && fromAmtList.size() > index) {
                            row.put("fromAmount",
                                    IdosConstants.decimalFormat.format(Double.parseDouble(fromAmtList.get(index))));
                        } else {
                            row.put("fromAmount", 0.0);
                        }
                        if (toAmountList != null && index < toAmountList.size()) {
                            row.put("toAmount",
                                    IdosConstants.decimalFormat.format(Double.parseDouble(toAmountList.get(index))));
                        } else {
                            row.put("toAmount", 0.0);
                        }
                    } else {
                        row.put("isChecked", false);
                        row.put("fromAmount", 0.0);
                        row.put("toAmount", 0.0);
                    }
                } else {
                    row.put("isChecked", false);
                    row.put("fromAmount", 0.0);
                    row.put("toAmount", 0.0);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, loggedUser.getEmail(), loggedUser.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result getCoaLiabilitiesItems(int ruleType, String selectedUser, Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager=getEntityManager();
        ObjectNode result = null;
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users loggedUser = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            JsonNode json = request.body().asJson();
            loggedUser = getUserInfo(request);
            if (loggedUser == null) {
                log.log(Level.SEVERE, "Error: unauthorized access.");
                return unauthorized();
            }
            if (selectedUser == null || "".equals(selectedUser)) {
                log.log(Level.SEVERE, "selected your email is not valid.");
                result.put("error", "Selected your email is not valid.");
            }
            Users user = Users.findActiveByEmail(selectedUser);
            result = coaService.getLiabilitiesCoaLeafNodesWithAllHeads(entityManager, loggedUser);
            ArrayNode coaItemData = (ArrayNode) result.get("coaItemData");

            List<String> coaList = null;
            List<String> fromAmtList = null;
            List<String> toAmountList = null;
            if (user != null) {
                if (ruleType == UserTxnRuleSetupCache.CREATOR) {
                    if (user.getTxnCreationCoaLiabl() != null) {
                        String expense = user.getTxnCreationCoaLiabl();
                        coaList = new ArrayList<String>(Arrays.asList(expense.split(",")));
                    }
                    if (user.getTxnCreationCoaLiablFromAmount() != null) {
                        fromAmtList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaLiablFromAmount().split(",")));
                    }
                    if (user.getTxnCreationCoaLiablToAmount() != null) {
                        toAmountList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaLiablToAmount().split(",")));
                    }
                } else if (ruleType == UserTxnRuleSetupCache.APPROVER) {
                    if (user.getTxnApprovalCoaLiabl() != null) {
                        String expense = user.getTxnApprovalCoaLiabl();
                        coaList = new ArrayList<String>(Arrays.asList(expense.split(",")));
                    }
                    if (user.getTxnApprovalCoaLiablFromAmount() != null) {
                        fromAmtList = new ArrayList<String>(
                                Arrays.asList(user.getTxnApprovalCoaLiablFromAmount().split(",")));
                    }
                    if (user.getTxnApprovalCoaLiablToAmount() != null) {
                        toAmountList = new ArrayList<String>(
                                Arrays.asList(user.getTxnApprovalCoaLiablToAmount().split(",")));
                    }
                } else if (ruleType == UserTxnRuleSetupCache.AUDITOR) {
                    if (user.getTxnAuditorCoaLiabl() != null) {
                        String expense = user.getTxnAuditorCoaLiabl();
                        coaList = new ArrayList<String>(Arrays.asList(expense.split(",")));
                    }
                }
            }
            for (int i = 0; i < coaItemData.size(); i++) {
                ObjectNode row = (ObjectNode) coaItemData.get(i);
                if (null != coaList) {
                    int index = coaList.indexOf(row.findValue("id").asText());
                    if (index != -1) {
                        row.put("isChecked", "checked");
                        if (fromAmtList != null && fromAmtList.size() > index) {
                            row.put("fromAmount",
                                    IdosConstants.decimalFormat.format(Double.parseDouble(fromAmtList.get(index))));
                        } else {
                            row.put("fromAmount", 0.0);
                        }
                        if (fromAmtList != null && index < fromAmtList.size()) {
                            row.put("toAmount",
                                    IdosConstants.decimalFormat.format(Double.parseDouble(toAmountList.get(index))));
                        } else {
                            row.put("toAmount", 0.0);
                        }

                    } else {
                        row.put("isChecked", false);
                        row.put("fromAmount", 0.0);
                        row.put("toAmount", 0.0);
                    }
                } else {
                    row.put("isChecked", false);
                    row.put("fromAmount", 0.0);
                    row.put("toAmount", 0.0);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, loggedUser.getEmail(), loggedUser.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result seachTxnUserItems(int particular, int ruleType, String searchText, String selectedUser,
            Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start " + particular + " searchText: " + searchText);
        // EntityManager entityManager=getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode coaItemDataArray = result.putArray("coaItemData");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users loggedUser = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            // JsonNode json = request.body().asJson();
            loggedUser = getUserInfo(request);
            if (loggedUser == null) {
                log.log(Level.SEVERE, "Error: unauthorized access.");
                return unauthorized();
            }
            if (selectedUser == null || "".equals(selectedUser)) {
                log.log(Level.SEVERE, "selected your email is not valid.");
                result.put("error", "Selected your email is not valid.");
            }
            Users user = Users.findActiveByEmail(selectedUser);
            List<String> coaList = null;
            List<String> fromAmtList = null;
            List<String> toAmountList = null;
            if (searchText == null) {
                searchText = "";
            } else {
                searchText = searchText.toLowerCase();
            }
            List<Specifics> itemsList = null;
            if (particular == IdosConstants.INCOME && user != null) {
                itemsList = Specifics.findCoaByNameAndHead(entityManager, loggedUser, particular, searchText);
                if (user.getTxnCreationCoaIncome() != null) {
                    String[] nameArray = user.getTxnCreationCoaIncome().split(",");
                    coaList = new ArrayList<String>(Arrays.asList(nameArray));
                }
                if (user.getTxnCreationCoaIncomeFromAmount() != null) {
                    fromAmtList = new ArrayList<String>(
                            Arrays.asList(user.getTxnCreationCoaIncomeFromAmount().split(",")));
                }
                if (user.getTxnCreationCoaIncomeToAmount() != null) {
                    toAmountList = new ArrayList<String>(
                            Arrays.asList(user.getTxnCreationCoaIncomeToAmount().split(",")));
                }
            } else if (particular == IdosConstants.EXPENSE && user != null) {
                itemsList = Specifics.findCoaByNameAndHead(entityManager, loggedUser, particular, searchText);
                if (user.getTxnCreationCoaExpense() != null) {
                    String[] nameArray = user.getTxnCreationCoaExpense().split(",");
                    coaList = new ArrayList<String>(Arrays.asList(nameArray));
                }
                if (user.getTxnCreationCoaExpenseFromAmount() != null) {
                    fromAmtList = new ArrayList<String>(
                            Arrays.asList(user.getTxnCreationCoaExpenseFromAmount().split(",")));
                }
                if (user.getTxnCreationCoaExpenseToAmount() != null) {
                    toAmountList = new ArrayList<String>(
                            Arrays.asList(user.getTxnCreationCoaExpenseToAmount().split(",")));
                }
            } else if (particular == IdosConstants.ASSETS) {
                if (ruleType == UserTxnRuleSetupCache.CREATOR && user != null) {
                    if (user.getTxnCreationCoaAsset() != null) {
                        String[] nameArray = user.getTxnCreationCoaAsset().split(",");
                        coaList = new ArrayList<String>(Arrays.asList(nameArray));
                    }
                    if (user.getTxnCreationCoaAssetFromAmount() != null) {
                        fromAmtList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaAssetFromAmount().split(",")));
                    }
                    if (user.getTxnCreationCoaAssetToAmount() != null) {
                        toAmountList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaAssetToAmount().split(",")));
                    }
                } else if (ruleType == UserTxnRuleSetupCache.APPROVER && user != null) {
                    if (user.getTxnApprovalCoaAsset() != null) {
                        String[] nameArray = user.getTxnApprovalCoaAsset().split(",");
                        coaList = new ArrayList<String>(Arrays.asList(nameArray));
                    }
                    if (user.getTxnApprovalCoaAssetFromAmount() != null) {
                        fromAmtList = new ArrayList<String>(
                                Arrays.asList(user.getTxnApprovalCoaAssetFromAmount().split(",")));
                    }
                    if (user.getTxnApprovalCoaAssetToAmount() != null) {
                        toAmountList = new ArrayList<String>(
                                Arrays.asList(user.getTxnApprovalCoaAssetToAmount().split(",")));
                    }
                } else if (ruleType == UserTxnRuleSetupCache.AUDITOR && user != null) {
                    if (user.getTxnAuditorCoaAsset() != null) {
                        String[] nameArray = user.getTxnAuditorCoaAsset().split(",");
                        coaList = new ArrayList<String>(Arrays.asList(nameArray));
                    }
                }
                itemsList = coaService.getCOAChildNodesList(entityManager, loggedUser, particular);
                for (Specifics specifics : itemsList) {
                    coaService.findCoaByName(coaItemDataArray, entityManager, loggedUser, specifics, searchText,
                            coaList, fromAmtList, toAmountList);
                }
            } else if (particular == IdosConstants.LIABILITIES) {

                ArrayList inparams = new ArrayList(2);
                inparams.add(loggedUser.getOrganization().getId());
                inparams.add("%" + searchText + "%");
                List<CoaLiabilities> liabList = genericDAO.queryWithParams(LIAILITY_SRCH_JPQL, entityManager, inparams);
                if (user != null) {
                    if (user.getTxnCreationCoaLiabl() != null) {
                        String[] nameArray = user.getTxnCreationCoaLiabl().split(",");
                        coaList = new ArrayList<String>(Arrays.asList(nameArray));
                    }
                    if (user.getTxnCreationCoaLiablFromAmount() != null) {
                        fromAmtList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaLiablFromAmount().split(",")));
                    }
                    if (user.getTxnCreationCoaLiablToAmount() != null) {
                        toAmountList = new ArrayList<String>(
                                Arrays.asList(user.getTxnCreationCoaLiablToAmount().split(",")));
                    }
                }
                for (CoaLiabilities specf : liabList) {
                    IdosUtil.setUserItemsDetail(coaList, fromAmtList, toAmountList, specf.getId().toString(),
                            specf.getName(), specf.getHeadType(), coaItemDataArray);
                }
            }
            if (particular != IdosConstants.LIABILITIES) {
                for (Specifics specf : itemsList) {
                    if (specf.getIdentificationForDataValid() != null
                            && !"".equals(specf.getIdentificationForDataValid())) {
                        if (specf.getIdentificationForDataValid() != null
                                && !"".equals(specf.getIdentificationForDataValid())) {
                            int mappid = Integer.parseInt(specf.getIdentificationForDataValid());
                            if (mappid != 59) {
                                continue;
                            }
                        }
                    }
                    IdosUtil.setUserItemsDetail(coaList, fromAmtList, toAmountList, specf.getId().toString(),
                            specf.getName(), IdosConstants.HEAD_SPECIFIC, coaItemDataArray);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, loggedUser.getEmail(), loggedUser.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

}
