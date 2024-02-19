package controllers.payroll;

import java.text.SimpleDateFormat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import play.mvc.Results;
import java.util.logging.Level;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import akka.NotUsed;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import com.idos.util.IDOSException;
import com.idos.util.CodeHelper;
import com.idos.util.FileUtil;

import com.idos.util.FileUtil;

import com.idos.util.IdosConstants;
import com.idos.util.NumberToWordsInt;
import com.idos.util.UserRolesUtil;

import actor.CreatorActor;
import controllers.StaticController;
import model.BaseModel;
import model.Branch;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.Organization;
import model.PayslipReport;

import model.TransactionPurpose;
import model.TrialBalanceBranchBank;
import model.TrialBalanceBranchCash;
import model.TrialBalanceCOAItems;
import model.ProfitNLossReport;
import akka.stream.javadsl.*;
import akka.actor.*;

import model.ProfitNLossReport;

import model.Users;
import model.payroll.PayrollSetup;
import model.payroll.PayrollTransaction;
import model.payroll.PayrollUserData;
import model.payroll.PayrollUserPayslip;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import views.html.errorPage;
import play.Application;
import javax.inject.Inject;
import play.mvc.Http.Request;
import pojo.TransactionViewResponse;
import play.mvc.Http;

public class PayrollController extends StaticController {
    private Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    private static Request request;
    // private Http.Session session = request.session();

    @Inject
    public PayrollController(Application application) {
        super(application);
        entityManager = EntityManagerProvider.getEntityManager();
        this.application = application;
    }

    @Transactional
    public Result addPayrollSetup(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = getUserInfo(request);
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        JsonNode json = request.body().asJson();
        String earningItemsList = json.findValue("multipleEarningItemsData").toString();
        String deductionsItemsList = json.findValue("multipleDeductionsItemsData").toString();
        try {
            if (user == null) {
                return unauthorized();
            }
            entitytransaction.begin();
            StringBuilder sbr = new StringBuilder(
                    "select obj.payrollHeadName from PayrollSetup obj where obj.organization.id=?1 and obj.payrollType=1 and obj.inForce=1 and obj.presentStatus=1");
            ArrayList inparams = new ArrayList();
            inparams.add(user.getOrganization().getId());
            List<PayrollSetup> prev_earn_list = genericDAO.queryWithParams(sbr.toString(), entityManager, inparams);
            int prev_earn_list_length = prev_earn_list.size();
            sbr = new StringBuilder(
                    "select obj.payrollHeadName from PayrollSetup obj where obj.organization.id=?1 and obj.payrollType=2 and obj.inForce=1 and obj.presentStatus=1");
            inparams = new ArrayList();
            inparams.add(user.getOrganization().getId());
            List<PayrollSetup> prev_dedu_list = genericDAO.queryWithParams(sbr.toString(), entityManager, inparams);
            int prev_dedu_list_length = prev_dedu_list.size();
            String sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=1 and obj.presentStatus=1");
            JSONArray arrJSON = new JSONArray(earningItemsList);
            // add and update
            if (prev_earn_list_length < arrJSON.length()) {
                for (int i = 0; i < arrJSON.length(); i++) {
                    JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                    String itemName = rowItemData.getString("earningItem");
                    Integer isFixedEarn = Integer.parseInt(rowItemData.getString("isFixedEarn"));
                    if (i >= prev_earn_list_length) {
                        ArrayList inparam = new ArrayList();
                        inparam.add(itemName);
                        inparam.add(user.getOrganization().getId());
                        List<PayrollSetup> headNameExists = genericDAO.queryWithParams(sb, entityManager, inparam);
                        if (headNameExists.size() > 0) {
                            PayrollSetup headName1 = headNameExists.get(0);
                            headName1.setInForce(1);
                            headName1.setIsFixed(isFixedEarn);
                            genericDAO.saveOrUpdate(headName1, user, entityManager);
                        } else {
                            PayrollSetup setupModel = new PayrollSetup();
                            setupModel.setOrganization(user.getOrganization());
                            setupModel.setPayrollHeadName(itemName);
                            setupModel.setInForce(1);
                            setupModel.setIsFixed(isFixedEarn);
                            setupModel.setPayrollType(IdosConstants.PAYROLL_TYPE_EARNINGS); // Earning
                            genericDAO.saveOrUpdate(setupModel, user, entityManager);
                        }
                    } else {
                        if (!prev_earn_list.toArray()[i].toString().equals(itemName)) {
                            PayrollSetup headName = null;
                            sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=1 and obj.presentStatus=1");
                            ArrayList inparam = new ArrayList();
                            inparam.add(prev_earn_list.toArray()[i].toString());
                            inparam.add(user.getOrganization().getId());

                            List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager,
                                    inparam);
                            headName = payrollSetupList.get(0);
                            headName.setPayrollHeadName(itemName);
                            genericDAO.saveOrUpdate(headName, user, entityManager);
                        } else {
                            PayrollSetup headName = null;
                            sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=1 and obj.presentStatus=1");
                            ArrayList inparam = new ArrayList();
                            inparam.add(itemName);
                            inparam.add(user.getOrganization().getId());

                            List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager,
                                    inparam);
                            headName = payrollSetupList.get(0);
                            headName.setIsFixed(isFixedEarn);
                            genericDAO.saveOrUpdate(headName, user, entityManager);
                        }
                    }
                }
            }
            JSONArray dedArrJSON = new JSONArray(deductionsItemsList);

            if (prev_dedu_list_length < dedArrJSON.length()) {
                for (int i = 0; i < dedArrJSON.length(); i++) {
                    JSONObject dedRowItemData = new JSONObject(dedArrJSON.get(i).toString());
                    String itemName = dedRowItemData.getString("deductionItem");
                    Integer isFixedDedu = Integer.parseInt(dedRowItemData.getString("isFixedDedu"));
                    if (i >= prev_dedu_list_length) {
                        sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=2 and obj.presentStatus=1");
                        ArrayList inparam = new ArrayList();
                        inparam.add(itemName);
                        inparam.add(user.getOrganization().getId());
                        List<PayrollSetup> deduheadNameExists = genericDAO.queryWithParams(sb, entityManager, inparam);
                        if (deduheadNameExists.size() > 0) {

                            PayrollSetup deduheadName1 = deduheadNameExists.get(0);
                            deduheadName1.setInForce(1);
                            deduheadName1.setIsFixed(isFixedDedu);
                            genericDAO.saveOrUpdate(deduheadName1, user, entityManager);
                        } else {
                            PayrollSetup setupModel = new PayrollSetup();
                            setupModel.setOrganization(user.getOrganization());
                            setupModel.setPayrollHeadName(itemName);
                            setupModel.setInForce(1);
                            setupModel.setPayrollType(IdosConstants.PAYROLL_TYPE_DEDUCTIONS); // dedu
                            setupModel.setIsFixed(isFixedDedu);
                            genericDAO.saveOrUpdate(setupModel, user, entityManager);
                        }
                    } else {
                        if (!prev_dedu_list.toArray()[i].equals(itemName)) {
                            PayrollSetup deduHeadname = null;

                            sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=2 and obj.presentStatus=1");
                            ArrayList inparam = new ArrayList();
                            inparam.add(prev_dedu_list.toArray()[i].toString());
                            inparam.add(user.getOrganization().getId());

                            List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager,
                                    inparam);
                            deduHeadname = payrollSetupList.get(0);
                            deduHeadname.setPayrollHeadName(itemName);
                            genericDAO.saveOrUpdate(deduHeadname, user, entityManager);
                        } else {
                            PayrollSetup deduHeadname = null;

                            sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=2 and obj.presentStatus=1");
                            ArrayList inparam = new ArrayList();
                            inparam.add(itemName);
                            inparam.add(user.getOrganization().getId());

                            List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager,
                                    inparam);
                            deduHeadname = payrollSetupList.get(0);
                            deduHeadname.setIsFixed(isFixedDedu);
                            genericDAO.saveOrUpdate(deduHeadname, user, entityManager);
                        }
                    }
                }
            }
            // only update
            if (prev_earn_list_length == arrJSON.length()) {
                for (int i = 0; i < arrJSON.length(); i++) {
                    JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                    String itemName = rowItemData.getString("earningItem");
                    Integer isFixedEarn = Integer.parseInt(rowItemData.getString("isFixedEarn"));
                    if (!prev_earn_list.toArray()[i].toString().equals(itemName)) {
                        PayrollSetup headName = null;
                        sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=1 and obj.presentStatus=1");
                        ArrayList inparam = new ArrayList();
                        inparam.add(prev_earn_list.toArray()[i].toString());
                        inparam.add(user.getOrganization().getId());

                        List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager, inparam);
                        headName = payrollSetupList.get(0);
                        headName.setPayrollHeadName(itemName);
                        headName.setIsFixed(isFixedEarn);
                        genericDAO.saveOrUpdate(headName, user, entityManager);
                    } else {
                        PayrollSetup headName = null;
                        sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=1 and obj.presentStatus=1");
                        ArrayList inparam = new ArrayList();
                        inparam.add(itemName);
                        inparam.add(user.getOrganization().getId());

                        List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager, inparam);
                        headName = payrollSetupList.get(0);
                        headName.setIsFixed(isFixedEarn);
                        genericDAO.saveOrUpdate(headName, user, entityManager);
                    }
                }
            }

            if (prev_dedu_list_length == dedArrJSON.length()) {
                for (int i = 0; i < prev_dedu_list_length; i++) {
                    JSONObject dedRowItemData = new JSONObject(dedArrJSON.get(i).toString());
                    String itemName = dedRowItemData.getString("deductionItem");
                    Integer isFixedDedu = Integer.parseInt((dedRowItemData.getString("isFixedDedu")));
                    if (!prev_dedu_list.toArray()[i].equals(itemName)) {
                        PayrollSetup deduHeadname = null;

                        sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=2 and obj.presentStatus=1");
                        ArrayList inparam = new ArrayList();
                        inparam.add(prev_dedu_list.toArray()[i].toString());
                        inparam.add(user.getOrganization().getId());

                        List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager, inparam);
                        deduHeadname = payrollSetupList.get(0);
                        deduHeadname.setPayrollHeadName(itemName);
                        deduHeadname.setIsFixed(isFixedDedu);
                        genericDAO.saveOrUpdate(deduHeadname, user, entityManager);
                    } else {
                        PayrollSetup deduHeadname = null;

                        sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=2 and obj.presentStatus=1");
                        ArrayList inparam = new ArrayList();
                        inparam.add(itemName);
                        inparam.add(user.getOrganization().getId());

                        List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager, inparam);
                        deduHeadname = payrollSetupList.get(0);
                        deduHeadname.setIsFixed(isFixedDedu);
                        genericDAO.saveOrUpdate(deduHeadname, user, entityManager);
                    }
                }
            }
            // delete and update
            if (prev_earn_list_length > arrJSON.length()) {
                for (int i = 0; i < prev_earn_list_length; i++) {
                    if (i >= arrJSON.length()) {

                        PayrollSetup headName = null;
                        sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=1 and obj.presentStatus=1");
                        ArrayList inparam = new ArrayList();
                        inparam.add(prev_earn_list.toArray()[i].toString());
                        inparam.add(user.getOrganization().getId());
                        List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager, inparam);
                        headName = payrollSetupList.get(0);
                        headName.setInForce(0);
                        genericDAO.saveOrUpdate(headName, user, entityManager);
                    } else {
                        JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                        String itemName = rowItemData.getString("earningItem");
                        Integer isFixedEarn = Integer.parseInt(rowItemData.getString("isFixedEarn"));
                        if (!prev_earn_list.toArray()[i].toString().equals(itemName)) {
                            PayrollSetup headName = null;
                            sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=1 and obj.presentStatus=1");
                            ArrayList inparam = new ArrayList();
                            inparam.add(prev_earn_list.toArray()[i].toString());
                            inparam.add(user.getOrganization().getId());

                            List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager,
                                    inparam);
                            headName = payrollSetupList.get(0);
                            headName.setPayrollHeadName(itemName);
                            genericDAO.saveOrUpdate(headName, user, entityManager);
                        } else {
                            PayrollSetup headName = null;
                            sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=1 and obj.presentStatus=1");
                            ArrayList inparam = new ArrayList();
                            inparam.add(itemName);
                            inparam.add(user.getOrganization().getId());

                            List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager,
                                    inparam);
                            headName = payrollSetupList.get(0);
                            headName.setIsFixed(isFixedEarn);
                            genericDAO.saveOrUpdate(headName, user, entityManager);

                        }
                    }
                }
            }

            if (prev_dedu_list_length > dedArrJSON.length()) {
                for (int i = 0; i < prev_dedu_list_length; i++) {
                    if (i >= dedArrJSON.length()) {
                        PayrollSetup deduHeadname = null;
                        sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=2 and obj.presentStatus=1");
                        ArrayList inparam = new ArrayList();
                        inparam.add(prev_dedu_list.toArray()[i].toString());
                        inparam.add(user.getOrganization().getId());

                        List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager, inparam);
                        deduHeadname = payrollSetupList.get(0);
                        deduHeadname.setInForce(0);
                        genericDAO.saveOrUpdate(deduHeadname, user, entityManager);
                    } else {
                        JSONObject dedRowItemData = new JSONObject(dedArrJSON.get(i).toString());
                        String itemName = dedRowItemData.getString("deductionItem");
                        Integer isFixedDedu = Integer.parseInt(dedRowItemData.getString("isFixedDedu"));
                        if (!prev_dedu_list.toArray()[i].equals(itemName)) {
                            PayrollSetup deduHeadname = null;

                            sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=2 and obj.presentStatus=1");
                            ArrayList inparam = new ArrayList();
                            inparam.add(prev_dedu_list.toArray()[i].toString());
                            inparam.add(user.getOrganization().getId());

                            List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager,
                                    inparam);
                            deduHeadname = payrollSetupList.get(0);
                            deduHeadname.setPayrollHeadName(itemName);
                            genericDAO.saveOrUpdate(deduHeadname, user, entityManager);
                        } else {
                            PayrollSetup deduHeadname = null;

                            sb = ("select obj from PayrollSetup obj where obj.payrollHeadName=?1 and obj.organization.id=?2 and obj.payrollType=2 and obj.presentStatus=1");
                            ArrayList inparam = new ArrayList();
                            inparam.add(itemName);
                            inparam.add(user.getOrganization().getId());

                            List<PayrollSetup> payrollSetupList = genericDAO.queryWithParams(sb, entityManager,
                                    inparam);
                            deduHeadname = payrollSetupList.get(0);
                            deduHeadname.setIsFixed(isFixedDedu);
                            genericDAO.saveOrUpdate(deduHeadname, user, entityManager);

                        }
                    }
                }
            }
            entitytransaction.commit();
        } catch (Exception ex) {
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return internalServerError(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End");
        return Results.ok(result);
    }

    @Transactional
    public Result showPayrollSetup(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();
        ArrayNode payrollEarningList = result.putArray("allPayrollEarningsItemsData");
        ArrayNode payrollDeductionsList = result.putArray("allPayrollDeductionsItemsData");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            Map<String, Object> criterias = new HashMap<String, Object>();
            JsonNode json = request.body().asJson();
            String useremail = json.findValue("userEmail").asText();
            session.adding("email", useremail);
            user = getUserInfo(request);
            // earning list
            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("payrollType", IdosConstants.PAYROLL_TYPE_EARNINGS); // Earning list
            criterias.put("inForce", 1);
            criterias.put("presentStatus", 1);
            List<PayrollSetup> payrollSetupList = genericDAO.findByCriteria(PayrollSetup.class, criterias,
                    entityManager);
            if (payrollSetupList != null && payrollSetupList.size() > 0) {
                for (PayrollSetup payrollItem : payrollSetupList) {
                    ObjectNode row = Json.newObject();
                    row.put("id", payrollItem.getId());
                    row.put("name", payrollItem.getPayrollHeadName());
                    row.put("isFixed", payrollItem.getIsFixed());
                    payrollEarningList.add(row);
                }
            }
            // deduction list
            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("payrollType", IdosConstants.PAYROLL_TYPE_DEDUCTIONS); // Deductions list
            criterias.put("inForce", 1);
            criterias.put("presentStatus", 1);
            List<PayrollSetup> payrollDedSetupList = genericDAO.findByCriteria(PayrollSetup.class, criterias,
                    entityManager);
            if (payrollDedSetupList != null && payrollDedSetupList.size() > 0) {
                for (PayrollSetup payrollDedItem : payrollDedSetupList) {
                    ObjectNode row = Json.newObject();
                    row.put("id", payrollDedItem.getId());
                    row.put("name", payrollDedItem.getPayrollHeadName());
                    row.put("isFixed", payrollDedItem.getIsFixed());
                    payrollDeductionsList.add(row);
                }
            }
        } catch (Exception ex) {
            // log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
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
    public Result saveUserPayrollItems(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users users = null;
        try {
            transaction.begin();
            JsonNode json = request.body().asJson();
            ArrayNode an = result.putArray("newvendorGroupData");
            String useremail = json.findValue("userEmail").asText();
            String checkboxList = json.findValue("earningsCheckboxList") != null
                    ? json.findValue("earningsCheckboxList").asText()
                    : null;
            String annualIncome = json.findValue("annualIncome").asText() != null
                    ? json.findValue("annualIncome").asText()
                    : null;
            String monthlyIncome = json.findValue("monthlyIncome").asText() != null
                    ? json.findValue("monthlyIncome").asText()
                    : null;
            String payrollType = json.findValue("payrollType").asText() != null ? json.findValue("payrollType").asText()
                    : null;

            session.adding("email", useremail);
            Long userEntityId = json.findValue("userHiddenPrimKey").asLong();
            users = Users.findById(userEntityId);
            Organization organization = users.getOrganization();
            String[] earningsCheckboxListArr = checkboxList.split(",");
            String[] annualIncomeArr = annualIncome.split(",");
            String[] monthlyIncomeArr = monthlyIncome.split(",");
            for (int i = 0; i < earningsCheckboxListArr.length; i++) {
                if (!earningsCheckboxListArr[i].isEmpty()) {
                    PayrollSetup payrollsetup = PayrollSetup.findById(Long.parseLong(earningsCheckboxListArr[i]));
                    Map<String, Object> criterias = new HashMap<String, Object>();
                    criterias.put("organization.id", organization.getId());
                    criterias.put("user.id", users.getId());
                    criterias.put("payrollSetup.id", payrollsetup.getId());
                    criterias.put("presentStatus", 1);
                    List<PayrollUserData> payrollUsrsList = genericDAO.findByCriteria(PayrollUserData.class, criterias,
                            entityManager);
                    PayrollUserData earningUserData;
                    if (payrollUsrsList != null && payrollUsrsList.size() > 0) {
                        earningUserData = payrollUsrsList.get(0);
                    } else {
                        earningUserData = new PayrollUserData();
                        earningUserData.setOrganization(organization);
                        earningUserData.setUser(users);
                        earningUserData.setPayrollSetup(payrollsetup);
                        earningUserData.setPayrollType(new Integer(payrollType));
                    }
                    if (annualIncomeArr[i] != null && annualIncomeArr[i] != "") {
                        earningUserData.setAnnualIncome(new Double(annualIncomeArr[i]));
                    }
                    if (monthlyIncomeArr[i] != null && monthlyIncomeArr[i] != "") {
                        earningUserData.setMonthlyIncome(new Double(monthlyIncomeArr[i]));
                    }
                    genericDAO.saveOrUpdate(earningUserData, users, entityManager);
                }
            }
            transaction.commit();
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            ex.printStackTrace();
            expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result getUserEarningsData(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();
        ArrayNode payrollEarningList = result.putArray("allUserEarningsItemsData");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            Map<String, Object> criterias = new HashMap<String, Object>();
            Map<String, Object> criteriasRest = new HashMap<String, Object>();
            JsonNode json = request.body().asJson();
            String useremail = json.findValue("userEmail").asText();
            session.adding("email", useremail);
            Long userEntityId = json.findValue("userHiddenPrimKey").asLong();
            user = Users.findById(userEntityId);

            if (user != null) {
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("user.id", user.getId());
                criterias.put("payrollSetup.payrollType", 1);
                criterias.put("payrollSetup.inForce", 1);
                criterias.put("presentStatus", 1);
                List<PayrollUserData> payrollUsrsList = genericDAO.findByCriteria(PayrollUserData.class, criterias,
                        entityManager);

                criteriasRest.put("organization.id", user.getOrganization().getId());
                criteriasRest.put("payrollType", 1);
                criteriasRest.put("inForce", 1);
                criterias.put("presentStatus", 1);
                List<PayrollSetup> payrollRestHead = genericDAO.findByCriteria(PayrollSetup.class, criteriasRest,
                        entityManager);

                if (payrollUsrsList != null && payrollUsrsList.size() > 0) {
                    for (PayrollUserData userearningData : payrollUsrsList) {
                        ObjectNode row = Json.newObject();
                        row.put("id", userearningData.getPayrollSetup().getId());
                        row.put("name", userearningData.getPayrollSetup().getPayrollHeadName());
                        row.put("annualInc", IdosConstants.decimalFormat.format(userearningData.getAnnualIncome()));
                        row.put("monthlyInc", IdosConstants.decimalFormat.format(userearningData.getMonthlyIncome()));
                        payrollEarningList.add(row);
                    }
                    if (payrollUsrsList.size() < payrollRestHead.size()) {
                        for (int i = payrollUsrsList.size(); i < payrollRestHead.size(); i++) {
                            PayrollSetup payrollRestHeadList = payrollRestHead.get(i);
                            ObjectNode row = Json.newObject();
                            row.put("id", payrollRestHeadList.getId());
                            row.put("name", payrollRestHeadList.getPayrollHeadName());
                            row.put("annualInc", 0);
                            row.put("monthlyInc", 0);
                            payrollEarningList.add(row);
                        }
                    }
                    // if(payrollUsrsList.size()<payrollRestHead.size()){
                } else {
                    // earning list from PayrollSetup
                    criterias.clear();
                    criterias.put("organization.id", user.getOrganization().getId());
                    criterias.put("payrollType", 1); // Earning list
                    criterias.put("inForce", 1);
                    criterias.put("presentStatus", 1);
                    List<PayrollSetup> payrollSetupList = genericDAO.findByCriteria(PayrollSetup.class, criterias,
                            entityManager);
                    if (payrollSetupList != null && payrollSetupList.size() > 0) {
                        for (PayrollSetup payrollItem : payrollSetupList) {
                            ObjectNode row = Json.newObject();
                            row.put("id", payrollItem.getId());
                            row.put("name", payrollItem.getPayrollHeadName());
                            row.put("annualInc", 0);
                            row.put("monthlyInc", 0);
                            payrollEarningList.add(row);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // log.log(Level.SEVERE, ex.getMessage());
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
    public Result getUserDeductionData(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();
        ArrayNode payrollDeductionsList = result.putArray("allUserDeductionsItemsData");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            Map<String, Object> criterias = new HashMap<String, Object>();
            Map<String, Object> criteriasRest = new HashMap<String, Object>();
            JsonNode json = request.body().asJson();
            String useremail = json.findValue("userEmail").asText();
            session.adding("email", useremail);
            Long userEntityId = json.findValue("userHiddenPrimKey").asLong();
            user = Users.findById(userEntityId);
            if (user != null) {
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("user.id", user.getId());
                criterias.put("payrollSetup.payrollType", 2);
                criterias.put("payrollSetup.inForce", 1);
                criterias.put("presentStatus", 1);
                List<PayrollUserData> payrollUsrsList = genericDAO.findByCriteria(PayrollUserData.class, criterias,
                        entityManager);

                criteriasRest.put("organization.id", user.getOrganization().getId());
                criteriasRest.put("payrollType", 2);
                criteriasRest.put("inForce", 1);
                criterias.put("presentStatus", 1);
                List<PayrollSetup> payrollRestDeduHead = genericDAO.findByCriteria(PayrollSetup.class, criteriasRest,
                        entityManager);

                if (payrollUsrsList != null && payrollUsrsList.size() > 0) {
                    for (PayrollUserData userearningData : payrollUsrsList) {
                        ObjectNode row = Json.newObject();
                        row.put("id", userearningData.getPayrollSetup().getId());
                        row.put("name", userearningData.getPayrollSetup().getPayrollHeadName());
                        row.put("annualInc", IdosConstants.decimalFormat.format(userearningData.getAnnualIncome()));
                        row.put("monthlyInc", IdosConstants.decimalFormat.format(userearningData.getMonthlyIncome()));
                        payrollDeductionsList.add(row);
                    }
                    if (payrollUsrsList.size() < payrollRestDeduHead.size()) {
                        for (int i = payrollUsrsList.size(); i < payrollRestDeduHead.size(); i++) {
                            PayrollSetup payrollRestDeduHeadList = payrollRestDeduHead.get(i);
                            ObjectNode row = Json.newObject();
                            row.put("id", payrollRestDeduHeadList.getId());
                            row.put("name", payrollRestDeduHeadList.getPayrollHeadName());
                            row.put("annualInc", 0);
                            row.put("monthlyInc", 0);
                            payrollDeductionsList.add(row);
                        }
                    }
                } else {
                    // deduction list from PayrollSetup
                    criterias.clear();
                    criterias.put("organization.id", user.getOrganization().getId());
                    criterias.put("payrollType", 2);
                    criterias.put("inForce", 1);
                    criterias.put("presentStatus", 1);
                    List<PayrollSetup> payrollSetupList = genericDAO.findByCriteria(PayrollSetup.class, criterias,
                            entityManager);
                    if (payrollSetupList != null && payrollSetupList.size() > 0) {
                        for (PayrollSetup payrollItem : payrollSetupList) {
                            ObjectNode row = Json.newObject();
                            row.put("id", payrollItem.getId());
                            row.put("name", payrollItem.getPayrollHeadName());
                            row.put("annualInc", 0);
                            row.put("monthlyInc", 0);
                            payrollDeductionsList.add(row);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // log.log(Level.SEVERE, ex.getMessage());
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
    public Result generatePayslip(Http.Request request) {
        // EntityManager entityManager = getEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        ObjectNode results = Json.newObject();
        // List<Users> users=null;
        Users user1 = null;
        File file = null;
        // Result result = Results.ok(file);
        try {
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start" + json);
            user1 = getUserInfo(request);
            String period1 = json.findValue("period").asText();

            String period[] = period1.split(" ");
            Integer payslipYear = Integer.parseInt(period[1]);
            // Integer payslipMonth=Integer.parseInt(period[0]);
            // Integer monthPayslip=Integer.parseInt(period[0]);
            String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                    "October", "November", "December" };
            int index = -1;
            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(period[0])) {
                    index = i + 1;
                    break;
                }
            }
            Integer payslipMonth = index;

            String reportName = "payslip";

            String payslipusers = json.findValue("payslipusers").asText();
            Long userId = new Long(payslipusers);
            // String usersArr[] = payslipusers.split(",");
            // for(int i=0; i<usersArr.length; i++ ){
            // Long userId = new Long(usersArr[i]);
            Users user = Users.findById(userId);
            String orgName = user.getOrganization().getName() == null ? "" : user.getOrganization().getName().trim();
            orgName = orgName.replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "");
            if (orgName.length() > 8) {
                orgName = orgName.substring(0, 7);
            }
            entityTransaction.begin();
            Long timeInMillis = Calendar.getInstance().getTimeInMillis();
            String fileName = orgName + "_" + reportName + timeInMillis + ".pdf";
            String type = IdosConstants.PDF_TYPE;

            String path = application.path().toString() + "/logs/report/";
            File filepath = new File(path);
            if (!filepath.exists()) {
                filepath.mkdir();
            }
            path = path + fileName;

            List<PayslipReport> datas = getUserPayslipData(user, payslipYear, payslipMonth, request);
            Map<String, Object> params = new HashMap<String, Object>();
            String companyLogo = FileUtil.getCompanyLogo(user.getOrganization());
            if (companyLogo != null && !"".equals(companyLogo)) {
                params.put("companyLogo", companyLogo);
            }
            if (user.getFullName() != null) {
                params.put("fullName", user.getFullName());
            }
            if (user.getOrganization().getName() != null) {
                params.put("companyName", user.getOrganization().getName());
            }
            if (user.getOrganization().getRegisteredAddress() != null) {
                String address = user.getOrganization().getRegisteredAddress().replaceAll("\\r\\n|\\r|\\n", " ");
                params.put("companyAddress", address);
            }
            if (user.getOrganization().getCorporateMail() != null) {
                params.put("companyEmail", user.getOrganization().getCorporateMail());
            }
            if (user.getOrganization().getRegisteredPhoneNumber() != null) {
                params.put("companyPhNo", user.getOrganization().getRegisteredPhoneNumber());
            }
            if (user.getOrganization().getWebUrl() != null) {
                params.put("companyURL", user.getOrganization().getWebUrl());
            }
            params.put("workingDays", datas.get(0).getPayDays());

            params.put("payDays", datas.get(0).getEligibleDays());

            reportName = "payslip";
            ByteArrayOutputStream out = dynReportService.generateStaticReportOld(reportName, datas, params, type,
                    request, application);
            file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            out.writeTo(fileOut);
            fileOut.close();
            entityTransaction.commit();

            log.log(Level.FINE, ">>>> End");
            return Results.ok(file).withHeader("ContentType", "application/json").withHeader("Content-Disposition",
                    "attachment; filename=" + fileName);

        } catch (Exception ex) {
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            ex.printStackTrace();
            log.log(Level.SEVERE, user1.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user1.getEmail(), user1.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return internalServerError(errorPage.render(ex, errorList));
        }

    }

    @Transactional
    public Result getTransactionPayrollData(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();
        ArrayNode netPayList = result.putArray("allPayrollNetPayList");
        ArrayNode payrollEarningHeadersList = result.putArray("allPayrollEarningHeadersList");
        ArrayNode payrollDeductionsHeadersList = result.putArray("allPayrollDeductionHeadersList");
        ArrayNode payrollAllUsersEarningList = result.putArray("allUserPayrollEarningItemsData");
        ArrayNode payrollAllUsersDeductionsList = result.putArray("allUserPayrollDeductionsItemsData");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            Map<String, Object> criterias = new HashMap<String, Object>();
            JsonNode json = request.body().asJson();
            String useremail = json.findValue("userEmail").asText();
            String branchId = json.findValue("branchId").asText();
            Branch branchObj = Branch.findById(new Long(branchId));
            session.adding("email", useremail);
            user = getUserInfo(request);
            // Header data
            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("payrollType", IdosConstants.PAYROLL_TYPE_EARNINGS);
            criterias.put("inForce", 1);
            criterias.put("presentStatus", 1);
            List<PayrollSetup> payrollSetupEarningList = genericDAO.findByCriteria(PayrollSetup.class, criterias,
                    entityManager);
            if (payrollSetupEarningList != null && payrollSetupEarningList.size() > 0) {
                for (PayrollSetup payrollItem : payrollSetupEarningList) {
                    ObjectNode row = Json.newObject();
                    row.put("id", payrollItem.getId());
                    row.put("name", payrollItem.getPayrollHeadName());
                    row.put("isFixed", payrollItem.getIsFixed());
                    payrollEarningHeadersList.add(row);
                }
            }
            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("payrollType", IdosConstants.PAYROLL_TYPE_DEDUCTIONS);
            criterias.put("inForce", 1);
            criterias.put("presentStatus", 1);
            List<PayrollSetup> payrollSetupDeductionList = genericDAO.findByCriteria(PayrollSetup.class, criterias,
                    entityManager);
            if (payrollSetupDeductionList != null && payrollSetupDeductionList.size() > 0) {
                for (PayrollSetup payrollItem : payrollSetupDeductionList) {
                    ObjectNode row = Json.newObject();
                    row.put("id", payrollItem.getId());
                    row.put("name", payrollItem.getPayrollHeadName());
                    row.put("isFixed", payrollItem.getIsFixed());
                    payrollDeductionsHeadersList.add(row);
                }
            }
            // get all users for org
            double[] userEarningNetPaylist = new double[30];
            int ctEarn = 0;
            criterias.clear();
            criterias.put("organization.id", user.getOrganization().getId());
            criterias.put("branch.id", branchObj.getId());
            criterias.put("presentStatus", 1);
            List<Users> usersList = genericDAO.findByCriteria(Users.class, criterias, entityManager);
            // All users earnings data
            boolean userPayrollForHeadSet = false;
            for (Users userObj : usersList) {
                criterias.clear();
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("user.id", userObj.getId());
                criterias.put("payrollSetup.payrollType", IdosConstants.PAYROLL_TYPE_EARNINGS);
                criterias.put("presentStatus", 1);
                List<PayrollUserData> payrollUsrsList = genericDAO.findByCriteria(PayrollUserData.class, criterias,
                        entityManager);
                if (payrollUsrsList != null && payrollUsrsList.size() > 0) {
                    ObjectNode row = Json.newObject();
                    String monthlyInc = "";
                    double monthlyIncPerUserTotal = 0;
                    row.put("userName", userObj.getFullName());
                    row.put("userId", userObj.getId());
                    for (PayrollSetup earningHeaders : payrollSetupEarningList) {// output will be for
                                                                                 // basic=100,dearness=200 etc as per
                                                                                 // header list
                        for (PayrollUserData userdata : payrollUsrsList) {
                            userPayrollForHeadSet = false;
                            if (userdata.getPayrollSetup().getId() == earningHeaders.getId()) {
                                userPayrollForHeadSet = true;
                                // row.put("payrollItemId", earningHeaders.getId());
                                // row.put("monthlyInc", userdata.getMonthlyIncome());
                                monthlyInc = monthlyInc + userdata.getMonthlyIncome() + ",";
                                monthlyIncPerUserTotal = monthlyIncPerUserTotal + userdata.getMonthlyIncome();
                                break;
                            }
                        }
                        if (!userPayrollForHeadSet) {
                            monthlyInc = monthlyInc + "0,";
                        }
                    }
                    monthlyInc = monthlyInc.replaceAll(",$", "");
                    row.put("monthlyInc", monthlyInc);
                    payrollAllUsersEarningList.add(row);
                    userEarningNetPaylist[ctEarn] = monthlyIncPerUserTotal;
                    ctEarn++;
                }
            }
            // All users Deduction data
            double[] userDeduNetPaylist = new double[30];
            int ctDedu = 0;
            boolean deductionDataSet = false;
            for (Users userObj : usersList) {
                criterias.clear();
                criterias.put("organization.id", user.getOrganization().getId());
                criterias.put("user.id", userObj.getId());
                criterias.put("payrollSetup.payrollType", IdosConstants.PAYROLL_TYPE_DEDUCTIONS); // deduction
                criterias.put("presentStatus", 1);
                List<PayrollUserData> payrollUsrsList = genericDAO.findByCriteria(PayrollUserData.class, criterias,
                        entityManager);
                if (payrollUsrsList != null && payrollUsrsList.size() > 0) {
                    ObjectNode row = Json.newObject();
                    String monthlyInc = "";
                    double monthlyIncPerUserTotal = 0;
                    row.put("userName", userObj.getFullName());
                    row.put("userId", userObj.getId());
                    for (PayrollSetup dedHeaders : payrollSetupDeductionList) {// output will be for
                                                                               // basic=100,dearness=200 etc as per
                                                                               // header list
                        for (PayrollUserData userdata : payrollUsrsList) {
                            userPayrollForHeadSet = false;
                            if (userdata.getPayrollSetup().getId() == dedHeaders.getId()) {
                                deductionDataSet = true;
                                monthlyInc = monthlyInc + userdata.getMonthlyIncome() + ",";
                                monthlyIncPerUserTotal = monthlyIncPerUserTotal + userdata.getMonthlyIncome();
                                break;
                            }
                        }
                        if (!deductionDataSet) {
                            monthlyInc = monthlyInc + "0,";
                        }
                    }
                    monthlyInc = monthlyInc.replaceAll(",$", "");
                    row.put("monthlyInc", monthlyInc);
                    payrollAllUsersDeductionsList.add(row);
                    userDeduNetPaylist[ctDedu] = monthlyIncPerUserTotal;
                    ctDedu++;
                }
            }
            for (int i = 0; i < 30; i++) {
                ObjectNode row = Json.newObject();
                double netPayPerUser = userEarningNetPaylist[i] - userDeduNetPaylist[i];
                row.put("netPay", netPayPerUser);
                netPayList.add(row);
            }

        } catch (Exception ex) {
            // log.log(Level.SEVERE, ex.getMessage());
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
    public Result addPayrollMonthlyPaySlips(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        JsonNode json = request.body().asJson();

        try {
            entitytransaction.begin();
            user = getUserInfo(request);
            String payrollItems = json.findValue("payrollItems").toString();
            String branchId = json.findValue("branchId").asText();
            Branch branchObj = Branch.findById(new Long(branchId));
            String payrollMonth = json.findValue("payrollMonth").asText();
            String payrollDate = payrollMonth + "-01";
            String[] yearmonth = payrollMonth.split(" "); // "June 2018"
            String payDays = json.findValue("payDays").asText();
            String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                    "October", "November", "December" };
            int index = -1;
            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(yearmonth[0])) {
                    index = i + 1;
                    break;
                }
            }
            String month = Integer.toString(index);
            int txnreceiptdetails = json.findValue("txnReceiptDetails") != null
                    ? json.findValue("txnReceiptDetails").asInt()
                    : 0;
            String txnReceiptTypeBankDetails = json.findValue("txnReceiptTypeBankDetails") != null
                    ? json.findValue("txnReceiptTypeBankDetails").asText()
                    : null;
            long txnPurposeVal = json.findValue("txnPurpose").asLong();
            String totalEarning = json.findValue("totalEarning") != null ? json.findValue("totalEarning").asText()
                    : "0.0";
            String totalDeduction = json.findValue("totalDeduction") != null ? json.findValue("totalDeduction").asText()
                    : "0.0";
            String totalTotalEarnings = json.findValue("totalTotalEarnings") != null
                    ? json.findValue("totalTotalEarnings").asText()
                    : "0.0";
            String totalTotalDeductions = json.findValue("totalTotalDeductions") != null
                    ? json.findValue("totalTotalDeductions").asText()
                    : "0.0";
            String totalNetPay = json.findValue("totalNetPay") != null ? json.findValue("totalNetPay").asText() : "0.0";
            TransactionPurpose transactionPurpose = TransactionPurpose.findById(txnPurposeVal);
            String transactionNumber = CodeHelper.getForeverUniqueID("PRTXN", null);
            String approverEmails = "", additionalApprovarUsers = "", selectedAdditionalApproval = "";
            JSONArray arrJSON = new JSONArray(payrollItems);
            JSONArray earnHeadIdsArr = null, deduHeadIdsArr = null;
            // JSONArray earnPayrollItems= new JSONArray(),deduPayrollItems = new
            // JSONArray();
            for (int i = 0; i < arrJSON.length(); i++) {
                JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
                // {"eligibleDays":"2","deductionsData":["0.56"],"earningsData":["5.56","1.11"]}
                String userId = rowItemData.getString("userId");
                Users userObj = Users.findById(new Long(userId));
                String eligibleDays = rowItemData.getString("eligibleDays");
                String monthlyDedActualTotal = "0.00";
                String monthlyIncActualTotal = "0.00";
                if (!rowItemData.isNull("monthlyIncActualTotal")
                        && !rowItemData.getString("monthlyIncActualTotal").isEmpty()) {
                    monthlyIncActualTotal = rowItemData.getString("monthlyIncActualTotal");
                }
                if (!rowItemData.isNull("monthlyDedActualTotal")
                        && !rowItemData.getString("monthlyDedActualTotal").isEmpty()) {
                    monthlyDedActualTotal = rowItemData.getString("monthlyDedActualTotal");
                }
                String netPayActual = "0.00";
                if (!rowItemData.isNull("netPayActual") && !rowItemData.getString("netPayActual").isEmpty()) {
                    netPayActual = rowItemData.getString("netPayActual");
                }
                PayrollUserPayslip payslipData = new PayrollUserPayslip();
                if (userObj != null) {
                    payslipData.setUser(userObj);
                }
                payslipData.setOrganization(user.getOrganization());
                payslipData.setBranch(branchObj);
                payslipData.setPayslipYear(new Integer(yearmonth[1]));
                payslipData.setPayslipMonth(new Integer(month));
                payslipData.setPayDays(new Integer(payDays));
                payslipData.setEligibleDays(new Integer(eligibleDays));

                JSONArray earningsdataArr = rowItemData.getJSONArray("earningsData"); // ["10416.67","1666.67","4166.67"]
                JSONArray earningPayrollTypesArr = rowItemData.getJSONArray("earningPayrollTypes");
                earnHeadIdsArr = rowItemData.getJSONArray("earnHeadIds");
                // earnPayrollItems = rowItemData.getJSONArray("earningPayrollTypes");
                for (int j = 0; j < earningsdataArr.length(); j++) {
                    Double earningAmt = earningsdataArr.getDouble(j);
                    Integer earnHeadId = earnHeadIdsArr.getInt(j);
                    switch (j) {
                        case 0:
                            payslipData.setEarning1(earningAmt);
                            payslipData.setEarning1Id(earnHeadId);
                            break;
                        case 1:
                            payslipData.setEarning2(earningAmt);
                            payslipData.setEarning2Id(earnHeadId);
                            break;
                        case 2:
                            payslipData.setEarning3(earningAmt);
                            payslipData.setEarning3Id(earnHeadId);
                            break;
                        case 3:
                            payslipData.setEarning4(earningAmt);
                            payslipData.setEarning4Id(earnHeadId);
                            break;
                        case 4:
                            payslipData.setEarning5(earningAmt);
                            payslipData.setEarning5Id(earnHeadId);
                            break;
                        case 5:
                            payslipData.setEarning6(earningAmt);
                            payslipData.setEarning6Id(earnHeadId);
                            break;
                        case 6:
                            payslipData.setEarning7(earningAmt);
                            payslipData.setEarning7Id(earnHeadId);
                            break;
                    }
                }
                JSONArray deductionsdataArr = rowItemData.getJSONArray("deductionsData"); // ["10416.67","1666.67","4166.67"]
                JSONArray deductionPayrollTypesArr = rowItemData.getJSONArray("deductionsPayrollTypes");
                deduHeadIdsArr = rowItemData.getJSONArray("deduHeadIds");
                // deduPayrollItems = rowItemData.getJSONArray("deductionsPayrollTypes");
                for (int j = 0; j < deductionsdataArr.length(); j++) {
                    Double deductAmt = deductionsdataArr.getDouble(j);
                    Integer deduHeadId = deduHeadIdsArr.getInt(j);
                    switch (j) {
                        case 0:
                            payslipData.setDeduction1(deductAmt);
                            payslipData.setDeduction1Id(deduHeadId);
                            break;
                        case 1:
                            payslipData.setDeduction2(deductAmt);
                            payslipData.setDeduction2Id(deduHeadId);
                            break;
                        case 2:
                            payslipData.setDeduction3(deductAmt);
                            payslipData.setDeduction3Id(deduHeadId);
                            break;
                        case 3:
                            payslipData.setDeduction4(deductAmt);
                            payslipData.setDeduction4Id(deduHeadId);
                            break;
                        case 4:
                            payslipData.setDeduction5(deductAmt);
                            payslipData.setDeduction5Id(deduHeadId);
                            break;
                        case 5:
                            payslipData.setDeduction6(deductAmt);
                            payslipData.setDeduction6Id(deduHeadId);
                            break;
                        case 6:
                            payslipData.setDeduction7(deductAmt);
                            payslipData.setDeduction7Id(deduHeadId);
                            break;
                    }
                }
                payslipData.setTotalEarning(new Double(monthlyIncActualTotal));
                payslipData.setTotalDeduction(new Double(monthlyDedActualTotal));
                payslipData.setNetPay(new Double(netPayActual));
                payslipData.setPaymentMode(txnreceiptdetails);
                payslipData.setReceiptDetails(txnReceiptTypeBankDetails);
                payslipData.setTransactionRefNumber(transactionNumber);
                payslipData.setTransactionDate(Calendar.getInstance().getTime());
                genericDAO.saveOrUpdate(payslipData, user, entityManager);

            }
            // sendPayrollWebSocketResponse(id,branchName, txnPurpose,null, totalNetAmt,
            // transactionStatus, createdBy,approverEmails, additionalApprovarUsers,
            // transactionNumber, totalEarnings, totalDeductions,user, entityManager);
            PayrollTransaction payrollTxn = new PayrollTransaction();
            // JSONArray arrEarnJSON = new JSONArray(totalEarning);
            String totalEarningsArr[] = totalEarning.split(",");

            for (int i = 0; i < totalEarningsArr.length; i++) {
                Double totalIncome = Double.parseDouble(totalEarningsArr[i]);
                Integer earnHeadId = earnHeadIdsArr.getInt(i);
                switch (i) {
                    case 0:
                        payrollTxn.setTotalEarning1(totalIncome);
                        payrollTxn.setEarning1Id(earnHeadId);
                        break;
                    case 1:
                        payrollTxn.setTotalEarning2(totalIncome);
                        payrollTxn.setEarning2Id(earnHeadId);
                        break;
                    case 2:
                        payrollTxn.setTotalEarning3(totalIncome);
                        payrollTxn.setEarning3Id(earnHeadId);
                        break;
                    case 3:
                        payrollTxn.setTotalEarning4(totalIncome);
                        payrollTxn.setEarning4Id(earnHeadId);
                        break;
                    case 4:
                        payrollTxn.setTotalEarning5(totalIncome);
                        payrollTxn.setEarning5Id(earnHeadId);
                        break;
                    case 5:
                        payrollTxn.setTotalEarning6(totalIncome);
                        payrollTxn.setEarning6Id(earnHeadId);
                        break;
                    case 6:
                        payrollTxn.setTotalEarning7(totalIncome);
                        payrollTxn.setEarning7Id(earnHeadId);
                        break;
                }
            }
            // JSONArray arrDeduJSON = new JSONArray(totalDeduction);
            String totalDeduArr[] = totalDeduction.split(",");
            for (int i = 0; i < totalDeduArr.length; i++) {
                Double totalDedu = 0.0;
                if (totalDeduArr[i] != null && !totalDeduArr[i].equals("")) {
                    totalDedu = Double.parseDouble(totalDeduArr[i]);
                }
                Integer deduHeadId = deduHeadIdsArr.getInt(i);
                switch (i) {
                    case 0:
                        payrollTxn.setTotalDeduction1(totalDedu);
                        payrollTxn.setDeduction1Id(deduHeadId);
                        break;
                    case 1:
                        payrollTxn.setTotalDeduction2(totalDedu);
                        payrollTxn.setDeduction2Id(deduHeadId);
                        break;
                    case 2:
                        payrollTxn.setTotalDeduction3(totalDedu);
                        payrollTxn.setDeduction3Id(deduHeadId);
                        break;
                    case 3:
                        payrollTxn.setTotalDeduction4(totalDedu);
                        payrollTxn.setDeduction4Id(deduHeadId);
                        break;
                    case 4:
                        payrollTxn.setTotalDeduction5(totalDedu);
                        payrollTxn.setDeduction5Id(deduHeadId);
                        break;
                    case 5:
                        payrollTxn.setTotalDeduction6(totalDedu);
                        payrollTxn.setDeduction6Id(deduHeadId);
                        break;
                    case 6:
                        payrollTxn.setTotalDeduction7(totalDedu);
                        payrollTxn.setDeduction7Id(deduHeadId);
                        break;
                }
            }
            payrollTxn.setTotalTotalIncome(Double.parseDouble(totalTotalEarnings));
            if (totalTotalDeductions != null && !"".equals(totalTotalDeductions)) {
                payrollTxn.setTotalTotalDeduction(Double.parseDouble(totalTotalDeductions));
            } else {
                payrollTxn.setTotalTotalDeduction(0.0);
            }
            payrollTxn.setTotalNetPay(Double.parseDouble(totalNetPay));
            payrollTxn.setTransactionRefNumber(transactionNumber);
            List<String> emailSet = UserRolesUtil.approverAdditionalApprovalBasedOnSelectedBranch(user, branchObj,
                    entityManager);
            Object[] array = emailSet.toArray();
            for (int j = 0; j < array.length; j++) {
                if (j == 0) {
                    approverEmails = array[j].toString();
                }
                if (j == 1) {
                    additionalApprovarUsers = array[j].toString();
                }
                if (j == 2) {
                    selectedAdditionalApproval = array[j].toString();
                }
            }
            payrollTxn.setApproverEmails(approverEmails);
            payrollTxn.setAdditionalApproverEmails(additionalApprovarUsers);
            payrollTxn.setOrganization(user.getOrganization());
            payrollTxn.setBranch(branchObj);
            payrollTxn.setPayslipMonth(new Integer(month));
            payrollTxn.setPayslipYear(new Integer(yearmonth[1]));
            payrollTxn.setTransactionDate(Calendar.getInstance().getTime());
            payrollTxn.setPayDays(new Integer(payDays));
            payrollTxn.setTransactionPurpose(transactionPurpose);
            payrollTxn.setTransactionStatus("Require Approval");
            genericDAO.saveOrUpdate(payrollTxn, user, entityManager);
            sendPayrollWebSocketResponse(payrollTxn, user, entityManager, result);
            entitytransaction.commit();
            if (payrollTxn != null) {
                result.put(TRANSACTION_ID, payrollTxn.getId());
                result.put(TRANSACTION_REF_NO, payrollTxn.getTransactionRefNumber());
            }
        } catch (Exception ex) {
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return internalServerError(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End");
        return Results.ok(result);
    }

    private static void sendPayrollWebSocketResponse(PayrollTransaction payrollTxn, Users user,
            EntityManager entityManager, ObjectNode result) {
        // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
        // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
        // for (int i = 0; i < keyArray.length; i++) {
        // StringBuilder sbquery = new StringBuilder(
        // "select obj from Users obj WHERE obj.email ='" + keyArray[i] + "' and
        // obj.presentStatus=1");
        // List<Users> orgusers = genericDAO.executeSimpleQuery(sbquery.toString(),
        // entityManager);
        // if (!orgusers.isEmpty() && orgusers.get(0).getOrganization().getId() ==
        // user.getOrganization().getId()) {
        // orgtxnregistereduser.put(keyArray[i].toString(),
        // CreatorActor.expenseregistrered.get(keyArray[i]));
        // }
        // }
        String txnDate = idosdf.format(payrollTxn.getTransactionDate());
        String payMode = "", instrumentNo = "", instrumentDate = "";
        if (payrollTxn.getPayMode() != null)
            payMode = payrollTxn.getPayMode();

        if (payrollTxn.getInstrumentNumber() != null)
            instrumentNo = payrollTxn.getInstrumentNumber();

        if (payrollTxn.getInstrumentDate() != null)
            instrumentDate = payrollTxn.getInstrumentDate();

        TransactionViewResponse.addActionTxn(payrollTxn.getId(), payrollTxn.getBranch().getName(), "", "", "", "", "",
                "", "", "",
                payrollTxn.getTransactionPurpose().getTransactionPurpose(), txnDate, "", "", payMode, 0d, 0.0,
                payrollTxn.getTotalTotalIncome(), payrollTxn.getTotalNetPay(), "", "",
                payrollTxn.getTransactionStatus(), payrollTxn.getCreatedBy().getEmail(), "", "", "", "", "",
                payrollTxn.getApproverEmails(), payrollTxn.getAdditionalApproverEmails(), "", "",
                0d, "", instrumentNo, instrumentDate, 0l, "", "", 0, payrollTxn.getTransactionRefNumber(),
                payrollTxn.getBranch().getId(), payrollTxn.getTotalTotalDeduction(), payrollTxn.getPayDays(), 0,
                result);
    }

    // call it as List<PayslipReport> datas =
    // PayrollController.getUserPayslipData(user, payslipYear, payslipMonth);
    @Transactional
    public static List<PayslipReport> getUserPayslipData(Users paysllipUser, Integer payslipYear, Integer payslipMonth,
            Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ArrayList<PayslipReport> payReportList = new ArrayList<PayslipReport>();

        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = paysllipUser;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            PayslipReport payreport = new PayslipReport();
            Map<String, Object> criterias = new HashMap<String, Object>();
            // Earning Header data
            criterias.clear();
            criterias.put("organization.id", paysllipUser.getOrganization().getId());
            criterias.put("payrollType", IdosConstants.PAYROLL_TYPE_EARNINGS);
            criterias.put("inForce", 1);
            criterias.put("presentStatus", 1);
            List<PayrollSetup> payrollSetupEarningList = genericDAO.findByCriteria(PayrollSetup.class, criterias,
                    entityManager);

            // User earnings&deductions list
            StringBuilder sbr = new StringBuilder("select a from PayrollUserPayslip a where a.organization=");
            sbr.append(paysllipUser.getOrganization().getId());
            sbr.append(" and a.user.id = " + paysllipUser.getId());
            sbr.append(" and a.payslipYear = ").append(payslipYear)
                    .append(" and a.presentStatus=1 and a.payslipMonth = ").append(payslipMonth);
            List<PayrollUserPayslip> payslipLists = genericDAO.executeSimpleQuery(sbr.toString(), entityManager);

            int ct = 0;
            if (payslipLists != null && payslipLists.size() > 0) {
                log.log(Level.FINE, "payslipLists:::" + payslipLists);
                if (payrollSetupEarningList != null && payrollSetupEarningList.size() > 0) {
                    PayrollUserPayslip payslipData = payslipLists.get(0);
                    for (PayrollSetup payrollItem : payrollSetupEarningList) {
                        switch (ct) {
                            case 0:
                                payreport.setEarningHeadName1(payrollItem.getPayrollHeadName());
                                if (payslipData.getEarning1() != null)
                                    payreport.setEarningVal1(payslipData.getEarning1().toString());
                                else
                                    payreport.setEarningVal1("");
                                break;
                            case 1:
                                payreport.setEarningHeadName2(payrollItem.getPayrollHeadName());
                                if (payslipData.getEarning2() != null)
                                    payreport.setEarningVal2(payslipData.getEarning2().toString());
                                else
                                    payreport.setEarningVal2("");
                                break;
                            case 2:
                                payreport.setEarningHeadName3(payrollItem.getPayrollHeadName());
                                if (payslipData.getEarning3() != null)
                                    payreport.setEarningVal3(payslipData.getEarning3().toString());
                                else
                                    payreport.setEarningVal3("");
                                break;
                            case 3:
                                payreport.setEarningHeadName4(payrollItem.getPayrollHeadName());
                                if (payslipData.getEarning4() != null)
                                    payreport.setEarningVal4(payslipData.getEarning4().toString());
                                else
                                    payreport.setEarningVal4("");
                                break;
                            case 4:
                                payreport.setEarningHeadName5(payrollItem.getPayrollHeadName());
                                if (payslipData.getEarning5() != null)
                                    payreport.setEarningVal5(payslipData.getEarning5().toString());
                                else
                                    payreport.setEarningVal5("");
                                break;
                            case 5:
                                payreport.setEarningHeadName6(payrollItem.getPayrollHeadName());
                                if (payslipData.getEarning6() != null)
                                    payreport.setEarningVal6(payslipData.getEarning6().toString());
                                else
                                    payreport.setEarningVal6("");
                                break;
                            case 6:
                                payreport.setEarningHeadName7(payrollItem.getPayrollHeadName());
                                if (payslipData.getEarning7() != null)
                                    payreport.setEarningVal7(payslipData.getEarning7().toString());
                                else
                                    payreport.setEarningVal7("");
                                break;
                        }
                        ct++;
                    }
                    payreport.setTotalEarningHeadName("Total Earnings");
                    payreport.setTotalEarning(payslipData.getTotalEarning().toString());
                    payreport.setPayDays(payslipData.getPayDays().toString());
                    payreport.setEligibleDays(payslipData.getEligibleDays().toString());
                }
            }

            // deduction Header data
            criterias.clear();
            criterias.put("organization.id", paysllipUser.getOrganization().getId());
            criterias.put("payrollType", IdosConstants.PAYROLL_TYPE_DEDUCTIONS);
            criterias.put("inForce", 1);
            criterias.put("presentStatus", 1);
            List<PayrollSetup> payrollSetupDeductList = genericDAO.findByCriteria(PayrollSetup.class, criterias,
                    entityManager);

            int ctDed = 0;
            if (payslipLists != null && payslipLists.size() > 0) {
                if (payrollSetupDeductList != null && payrollSetupDeductList.size() > 0) {
                    PayrollUserPayslip payslipData = payslipLists.get(0);
                    for (PayrollSetup payrollItem : payrollSetupDeductList) {
                        switch (ctDed) {
                            case 0:
                                payreport.setDeductionHeadName1(payrollItem.getPayrollHeadName());
                                if (payslipData.getDeduction1() != null)
                                    payreport.setDeductionVal1(payslipData.getDeduction1().toString());
                                else
                                    payreport.setDeductionVal1("");
                                break;
                            case 1:
                                payreport.setDeductionHeadName2(payrollItem.getPayrollHeadName());
                                if (payslipData.getDeduction2() != null)
                                    payreport.setDeductionVal2(payslipData.getDeduction2().toString());
                                else
                                    payreport.setDeductionVal2("");
                                break;
                            case 2:
                                payreport.setDeductionHeadName3(payrollItem.getPayrollHeadName());
                                if (payslipData.getDeduction3() != null)
                                    payreport.setDeductionVal3(payslipData.getDeduction3().toString());
                                else
                                    payreport.setDeductionVal3("");
                                break;
                            case 3:
                                payreport.setDeductionHeadName4(payrollItem.getPayrollHeadName());
                                if (payslipData.getDeduction4() != null)
                                    payreport.setDeductionVal4(payslipData.getDeduction4().toString());
                                else
                                    payreport.setDeductionVal4("");
                                break;
                            case 4:
                                payreport.setDeductionHeadName5(payrollItem.getPayrollHeadName());
                                if (payslipData.getDeduction5() != null)
                                    payreport.setDeductionVal5(payslipData.getDeduction5().toString());
                                else
                                    payreport.setDeductionVal5("");
                                break;
                            case 5:
                                payreport.setDeductionHeadName6(payrollItem.getPayrollHeadName());
                                if (payslipData.getDeduction6() != null)
                                    payreport.setDeductionVal6(payslipData.getDeduction6().toString());
                                else
                                    payreport.setDeductionVal6("");
                                break;
                            case 6:
                                payreport.setDeductionHeadName7(payrollItem.getPayrollHeadName());
                                if (payslipData.getDeduction7() != null)
                                    payreport.setDeductionVal7(payslipData.getDeduction7().toString());
                                else
                                    payreport.setDeductionVal7("");
                                break;
                        }
                        ctDed++;
                    }
                    payreport.setTotalDeductionHeadName("Total Deductions");
                    payreport.setTotalDeduction(payslipData.getTotalDeduction().toString());
                    payreport.setNetPayHeadName("Net Pay");
                    payreport.setNetPay(payslipData.getNetPay().toString());
                    Long roundNetPay = Math.round(payslipData.getNetPay());
                    payreport.setNetPayInWords(NumberToWordsInt.convert(roundNetPay) + " Only.");
                }
            }
            payReportList.add(payreport);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
        }
        log.log(Level.FINE, ">>>> End");

        return payReportList;
    }

    @Transactional
    public Result getCashBalance(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode getCashBalance = result.putArray("cashBalanceData");
        Users user = null;
        try {
            user = getUserInfo(request);
            Double totalCashBalance = 0.0;
            StringBuilder sbr = new StringBuilder(
                    "select obj from BranchCashCount obj where obj.organization.id=?1 and obj.presentStatus=1");
            ArrayList inparams = new ArrayList();
            inparams.add(user.getOrganization().getId());
            List<BranchCashCount> orgCashBalance = genericDAO.queryWithParams(sbr.toString(), entityManager, inparams);
            ObjectNode row = Json.newObject();
            for (int i = 0; i < orgCashBalance.size(); i++) {
                totalCashBalance += orgCashBalance.get(i).getResultantCash();
            }
            row.put("cashBalance", totalCashBalance);
            getCashBalance.add(row);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
        }
        log.log(Level.FINE, ">>>> End");
        return Results.ok(result);
    }

    @Transactional
    public Result showPayslipHistory(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode payslipHistryan = result.putArray("payslipHistry");
        Users user = null;
        try {
            user = getUserInfo(request);
            StringBuilder sbr = new StringBuilder(
                    "select obj from PayrollUserPayslip obj where obj.organization.id = ?1 and obj.user.id = ?2 and obj.presentStatus=1 group by obj.payslipMonth");
            ArrayList inparams = new ArrayList();
            inparams.add(user.getOrganization().getId());
            inparams.add(user.getId());
            List<PayrollUserPayslip> payslipHistory = genericDAO.queryWithParams(sbr.toString(), entityManager,
                    inparams);
            String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                    "October", "November", "December" };
            for (PayrollUserPayslip payrollTxn : payslipHistory) {
                ObjectNode row = Json.newObject();
                String payrollMnth = months[payrollTxn.getPayslipMonth() - 1];
                row.put("userId", user.getId());
                row.put("month", payrollMnth + ',' + payrollTxn.getPayslipYear());
                row.put("grossPay", payrollTxn.getTotalEarning());
                row.put("netPay", payrollTxn.getNetPay());
                payslipHistryan.add(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result payslipForMonth(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = null;
        JsonNode json = request.body().asJson();
        String payrollMonth = json.findValue("month").toString();
        try {
            payrollMonth = payrollMonth.substring(1, payrollMonth.length() - 1);
            String monthYear[] = payrollMonth.split(",");
            String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                    "October", "November", "December" };
            int index = -1;
            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(monthYear[0])) {
                    index = i + 1;
                    break;
                }
            }
            int month = index;
            int year = Integer.parseInt(monthYear[1]);
            user = getUserInfo(request);
            StringBuilder sbr = new StringBuilder(
                    "select obj from PayrollUserPayslip obj where obj.organization.id = ?1 and obj.user.id = ?2 and obj.presentStatus=1 and obj.payslipMonth = ?3 and obj.payslipYear = ?4 order by obj.createdAt");
            ArrayList inparams = new ArrayList();
            inparams.add(user.getOrganization().getId());
            inparams.add(user.getId());
            inparams.add(month);
            inparams.add(year);
            List<PayrollUserPayslip> payslipForMonth = genericDAO.queryWithParams(sbr.toString(), entityManager,
                    inparams);
            if (payslipForMonth.size() > 0) {
                result.put("month", payrollMonth);
                result.put("grossPay", payslipForMonth.get(0).getTotalEarning());
                result.put("netPay", payslipForMonth.get(0).getNetPay());
                result.put("userId", user.getId());
            } else {
                result.put("month", "");
                result.put("grossPay", "");
                result.put("netPay", "");
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
        }
        return Results.ok(result);
    }

    @Transactional
    public Result getPayrollOpeningBalance(Http.Request request) {
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            Long itemId = json.findValue("itemId") != null ? json.findValue("itemId").asLong() : null;
            Long mapping = json.findValue("mappingId") != null ? json.findValue("mappingId").asLong() : null;
            String itemQuery = "select obj from PayrollSetup obj where obj.id =?1 and obj.organization.id = ?2 and obj.presentStatus=1";
            ArrayList inparams = new ArrayList();
            inparams.add(itemId);
            inparams.add(user.getOrganization().getId());
            List<PayrollSetup> item = genericDAO.queryWithParams(itemQuery, entityManager, inparams);
            PayrollSetup itemDetails = null;
            if (item != null) {
                itemDetails = item.get(0);
                result.put("id", itemId);
                if (itemDetails.getPayrollHeadName() != null)
                    result.put("name", itemDetails.getPayrollHeadName());
                else
                    result.put("name", "");

                if (itemDetails.getOpeningBal() != null)
                    result.put("openingBalance", IdosConstants.decimalFormat.format(itemDetails.getOpeningBal()));
                else
                    result.put("openingBalance", "");
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result savePayrollOpeningBalance(Http.Request request) {
        // EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            Long itemId = json.findValue("id") != null ? json.findValue("id").asLong() : null;
            Double openingBalance = json.findValue("openingBalance") != null
                    ? json.findValue("openingBalance").asDouble()
                    : null;
            String itemQuery = "select obj from PayrollSetup obj where obj.id =?1 and obj.organization.id = ?2 and obj.presentStatus=1";
            ArrayList inparams = new ArrayList();
            inparams.add(itemId);
            inparams.add(user.getOrganization().getId());
            transaction.begin();
            List<PayrollSetup> item = genericDAO.queryWithParams(itemQuery, entityManager, inparams);
            PayrollSetup itemDetails = null;
            if (item != null) {
                itemDetails = item.get(0);
                itemDetails.setOpeningBal(openingBalance);
                genericDAO.saveOrUpdate(itemDetails, user, entityManager);
            }
            transaction.commit();
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

}
