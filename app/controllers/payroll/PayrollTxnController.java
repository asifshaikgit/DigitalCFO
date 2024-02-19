package controllers.payroll;

import actor.CreatorActor;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import java.util.logging.Level;
import controllers.StaticController;
import model.Specifics;
import model.Users;
import model.payroll.PayrollTransaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.db.jpa.JPAApi;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;
import play.Application;
import play.mvc.Http.Request;
import pojo.TransactionViewResponse;

/**
 * @author $(USER) created on $(DATE)
 */
public class PayrollTxnController extends StaticController {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    public static Application application;
    private Request request;

    @Inject
    public PayrollTxnController(Application application) {
        super(application);
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result payrollApproverAction(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        ArrayNode cashBankBalChk = result.putArray("cashBankBal");
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            String email = json.findValue("useremail").asText();
            String transactionProvisionPrimId = json.findValue("transactionPrimId").asText();
            String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
            String txnRmarks = json.findValue("txnRmarks").asText() != null ? json.findValue("txnRmarks").asText()
                    : null;
            PayrollTransaction payrollTxn = PayrollTransaction
                    .findById(IdosUtil.convertStringToLong(transactionProvisionPrimId));

            if (null != email && !"".equals(email)) {
                // session.adding("email", email);
                user = getUserInfo(request);
                Specifics specificsForMapping = coaService.getSpecificsForMapping(user, "66", entityManager);
                if (specificsForMapping == null) {
                    throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                            "COA mapping is not found: 66",
                            "TDS COA mapping not found for, type: Is this the Employee benefits payable account");
                }
                int returnVal = payrollService.payrollApproverAction(result, json, user, entityManager,
                        entitytransaction, payrollTxn, specificsForMapping);
                if (returnVal != -1) { // Failed
                    if (returnVal == 0) {
                        ObjectNode row = Json.newObject();
                        row.put("cashBalCheck", 1);
                        cashBankBalChk.add(row);
                        return Results.ok(result);
                    } else if (returnVal == 2) {
                        ObjectNode row = Json.newObject();
                        row.put("bankBalCheck", 1);
                        cashBankBalChk.add(row);
                        return Results.ok(result);
                    } else {
                        ObjectNode row = Json.newObject();
                        row.put("cashBalCheck", 0);
                        cashBankBalChk.add(row);
                        sendPayrollWebSocketResponse(payrollTxn, user, entityManager, result);
                    }
                }
                if (payrollTxn != null) {
                    result.put(TRANSACTION_ID, payrollTxn.getId());
                    result.put(TRANSACTION_REF_NO, payrollTxn.getTransactionRefNumber());
                }
            }
        } catch (Exception ex) {
            reportException(entityManager, entitytransaction, user, ex, result);
        } catch (Throwable th) {
            reportThrowable(entityManager, entitytransaction, user, th, result);
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    public static void sendPayrollWebSocketResponse(PayrollTransaction payrollTxn, Users user,
            EntityManager entityManager, ObjectNode result) {
        String branchName = "";
        String approverEmail = "";
        String approverLabel = "";
        String txnRemarks = "";
        String txnDocs = "";
        String selectedAddApproval = "";
        if (payrollTxn.getBranch() != null) {
            branchName = payrollTxn.getBranch().getName();
        }
        if (payrollTxn.getApproverActionBy().getEmail() != null) {
            approverLabel = "APPROVER:";
            approverEmail = payrollTxn.getApproverActionBy().getEmail();
        }
        // Map<String, ActorRef> orgtxnregistereduser = new HashMap<String, ActorRef>();
        // Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
        // for (int i = 0; i < keyArray.length; i++) {
        // StringBuilder sbquery = new StringBuilder("");
        // sbquery.append("select obj from Users obj WHERE obj.email ='" + keyArray[i] +
        // "' and obj.presentStatus=1");
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

        TransactionViewResponse.addActionTxn(payrollTxn.getId(), branchName, "", "", "", "", "", "", "", "",
                payrollTxn.getTransactionPurpose().getTransactionPurpose(), txnDate, "", "", payMode, 0d, 0.0,
                payrollTxn.getTotalTotalIncome(), payrollTxn.getTotalNetPay(), "", "",
                payrollTxn.getTransactionStatus(), payrollTxn.getCreatedBy().getEmail(), approverLabel, approverEmail,
                txnDocs, txnRemarks, "", payrollTxn.getApproverEmails(), payrollTxn.getAdditionalApproverEmails(), "",
                "", 0d, "", instrumentNo, instrumentDate, 0l, "", "", 0,
                payrollTxn.getTransactionRefNumber(), payrollTxn.getBranch().getId(),
                payrollTxn.getTotalTotalDeduction(), payrollTxn.getPayDays(), 0, result);
    }
}
