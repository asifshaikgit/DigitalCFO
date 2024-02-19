package controllers;

import java.io.File;
import java.sql.Types;
import java.util.*;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.idos.util.IdosConstants;
import model.IdosProvisionJournalEntry;
import model.Transaction;
import model.Users;
import model.UsersRoles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.SingletonDBConnection;

import com.typesafe.config.Config;

import javax.transaction.Transactional;
import play.mvc.Http.Request;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.Application;
import javax.inject.Inject;
import javax.persistence.Query;

import play.db.jpa.JPAApi;
import views.html.errorPage;

public class AccountantTranscationController extends StaticController {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    public static Application application;

    @Inject
    public AccountantTranscationController(JPAApi jpaApi, Application application) {
        super(application);
        this.jpaApi = jpaApi;
        entityManager = EntityManagerProvider.getEntityManager();
        this.application = application;
    }

    @Transactional
    public Result downLoadTransactions(Request request) {
        log.log(Level.FINE, ">>>> Start");
        Users user = getUserInfo(request);
        if (user == null) {
            return unauthorized();
        }
        // EntityManager entityManager = getEntityManager();
        File file = null;
        ObjectNode results = Json.newObject();
        ArrayNode an = results.putArray("transactionFileCred");
        JsonNode json = request.body().asJson();
        String useremail = json.findValue("useremail").asText();
        String txnDownloadAs = json.findValue("txnDownloadAs").asText();
        try {
            String fname = null;
            String path = null;
            List<Transaction> sellTxnList = null;
            List<Transaction> buyTxnList = null;
            List<Transaction> otherTxnList = null;
            List<IdosProvisionJournalEntry> pjeTxnList = new ArrayList<>();

            if ("csv".equals(txnDownloadAs)) {
                sellTxnList = new ArrayList<>();
            } else {
                sellTxnList = new ArrayList<>();
                buyTxnList = new ArrayList<>();
                otherTxnList = new ArrayList<>();
            }

            searchTransactionsDBSearch(json, entityManager, user, sellTxnList, buyTxnList, otherTxnList, pjeTxnList,
                    txnDownloadAs);

            String orgName = user.getOrganization().getName().replaceAll("\\s", "");
            if (orgName.length() > 4) {
                orgName = orgName.substring(0, 4);
            }

            if (txnDownloadAs != null && !txnDownloadAs.equals("") && txnDownloadAs.equals("xls")) {
                String sheetName = orgName + "Transactions";
                path = application.path().toString() + "/logs/TransactionExcel/";
                fname = excelService.createtransactionexcel(user, json, entityManager, path, sheetName, sellTxnList,
                        buyTxnList, otherTxnList, pjeTxnList);
                if (path != null) {
                    file = new File(path + fname);
                }
                return Results.ok(file).withHeader("Content-Type", "application/xlsx").withHeader("Content-Disposition",
                        "attachment; filename=" + fname);
            } else if (txnDownloadAs != null && !txnDownloadAs.equals("") && txnDownloadAs.equals("csv")) {
                path = application.path().toString() + "/logs/TransactionExcel/";
                fname = user.getOrganization().getName() + "_Transactions.csv";
                file = excelService.createtransactioncsv(user, json, entityManager, path, fname, sellTxnList,
                        pjeTxnList);
                return Results.ok(file).withHeader("Content-Type", "application/csv").withHeader("Content-Disposition",
                        "attachment; filename=" + fname);
            } else {
                return Results.ok();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    public static void searchTransactionsDBSearch(JsonNode json, EntityManager entityManager, Users user,
            List<Transaction> sellTxnList, List<Transaction> buyTxnList, List<Transaction> otherTxnList,
            List<IdosProvisionJournalEntry> pjeTxnList, String downloadType) {
        Query stmt = null;

        try {
            Long searchCategory = json.findValue("searchCategory") != null ? json.findValue("searchCategory").asLong()
                    : Types.NULL;
            String searchTransactionRefNumber = json.findValue("searchTransactionRefNumber") != null
                    ? json.findValue("searchTransactionRefNumber").asText()
                    : null;
            String searchItems = json.findValue("searchItems") != null ? json.findValue("searchItems").asText() : null;
            String searchTxnStatus = json.findValue("searchTxnStatus") != null
                    ? json.findValue("searchTxnStatus").asText()
                    : null;
            String searchTxnFromDate = json.findValue("searchTxnFromDate") != null
                    ? json.findValue("searchTxnFromDate").asText()
                    : null;
            String searchTxnToDate = json.findValue("searchTxnToDate") != null
                    ? json.findValue("searchTxnToDate").asText()
                    : null;
            String searchTxnBranch = json.findValue("searchTxnBranch") != null
                    ? json.findValue("searchTxnBranch").asText()
                    : null;
            String searchTxnProjects = json.findValue("searchTxnProjects") != null
                    ? json.findValue("searchTxnProjects").asText()
                    : null;
            String searchVendors = json.findValue("searchVendors") != null ? json.findValue("searchVendors").asText()
                    : null;
            String searchCustomers = json.findValue("searchCustomers") != null
                    ? json.findValue("searchCustomers").asText()
                    : null;
            Long searchTxnWithWithoutDoc = json.findValue("searchTxnWithWithoutDoc") != null
                    ? json.findValue("searchTxnWithWithoutDoc").asLong()
                    : Types.NULL;
            String searchTxnPyMode = json.findValue("searchTxnPyMode") != null
                    ? json.findValue("searchTxnPyMode").asText()
                    : null;
            Long searchTxnWithWithoutRemarks = json.findValue("searchTxnWithWithoutRemarks") != null
                    ? json.findValue("searchTxnWithWithoutRemarks").asLong()
                    : Types.NULL;
            String searchTxnException = json.findValue("searchTxnException") != null
                    ? json.findValue("searchTxnException").asText()
                    : null;
            Double searchAmountRanseLimitFrom = json.findValue("searchAmountRanseLimitFrom") != null
                    ? json.findValue("searchAmountRanseLimitFrom").asDouble()
                    : Types.NULL;
            Double searchAmountRanseLimitTo = json.findValue("searchAmountRanseLimitTo") != null
                    ? json.findValue("searchAmountRanseLimitTo").asDouble()
                    : Types.NULL;
            Long txnUserType = json.findValue("txnUserType") != null ? json.findValue("txnUserType").asLong()
                    : Types.NULL;
            // Long txnQuestion = json.findValue("txnQuestion") != null ?
            // json.findValue("txnQuestion").asLong() : Types.NULL;
            String txnQuestion = json.findValue("txnQuestion") != null ? json.findValue("txnQuestion").asText() : null;

            int fromRecord = 0;
            int toRecord = 0;
            int perPage = 0;

            Date txnFmDate = null;
            Date txnToDt = null;
            String fmDt = null;
            String tDt = null;
            if (searchTxnFromDate != null && !searchTxnFromDate.equals("")) {
                txnFmDate = mysqldf.parse(mysqldf.format(idosdf.parse(searchTxnFromDate)));
            }
            if (searchTxnToDate != null && !searchTxnToDate.equals("")) {
                txnToDt = mysqldf.parse(mysqldf.format(idosdf.parse(searchTxnToDate)));
            }
            if (txnFmDate != null) {
                fmDt = mysqldf.format(txnFmDate);
            }
            if (txnToDt != null) {
                tDt = mysqldf.format(txnToDt);
            }
            String query = "{CALL search_transactions(:p1,:p2,:p3,:p4,:p5,:p6,:p7,:p8,:p9,:p10,:p11,:p12,:p13,:p14,:p15,:p16,:p17,:p18,:p19,:p20,:p21,:p22,:p23,:p24,:p25)}";
            stmt = entityManager.createNativeQuery(query);
            stmt.setParameter("p1", user.getId());
            stmt.setParameter("p2", user.getOrganization().getId());
            stmt.setParameter("p3", txnUserType);
            if (txnQuestion != null)
                stmt.setParameter("p4", txnQuestion);
            else
                stmt.setParameter("p4", Types.NULL);
            stmt.setParameter("p5", searchCategory);
            stmt.setParameter("p6", searchItems);
            // stmt.setParameter(7, searchTxnStatus);
            if (searchTxnStatus != "")
                stmt.setParameter("p7", searchTxnStatus);
            else
                stmt.setParameter("p7", Types.NULL);
            stmt.setParameter("p8", searchTransactionRefNumber);
            stmt.setParameter("p9", searchTxnBranch);
            stmt.setParameter("p10", searchTxnProjects);
            stmt.setParameter("p11", searchAmountRanseLimitFrom);
            stmt.setParameter("p12", searchAmountRanseLimitTo);
            stmt.setParameter("p13", searchTxnWithWithoutDoc);
            stmt.setParameter("p14", searchTxnPyMode);
            stmt.setParameter("p15", searchTxnWithWithoutRemarks);
            stmt.setParameter("p16", searchTxnException);
            stmt.setParameter("p17", searchVendors);
            stmt.setParameter("p18", searchCustomers);
            stmt.setParameter("p19", fmDt);
            stmt.setParameter("p20", tDt);
            Long debugStoredPorc = 1l; // 1 means true, stored proc data will be logged into table LOG_STORED_PROC
            stmt.setParameter("p21", debugStoredPorc);
            stmt.setParameter("p22", user.getEmail());
            stmt.setParameter("p23", fromRecord);
            stmt.setParameter("p24", perPage);
            // stmt.setParameter(25, 0L);
            stmt.setParameter("p25", Types.INTEGER);
            List<Map<String, String>> data = stmt.getResultList();
            int totalRecords = Integer.parseInt(data.get(0).get("25"));
            log.log(Level.FINE, "fount count: " + totalRecords);
            if (data != null) {
                // while (rs.next()) {
                for (Map<String, String> rs : data) {
                    String tableName = rs.get("TABLENAME");
                    Long id = Long.parseLong(rs.get("ID"));
                    if (!tableName.equalsIgnoreCase("pv")) {
                        long txnPurpose = Long.parseLong(rs.get("TRANSACTION_PURPOSE"));
                        Transaction txn = Transaction.findById(id);
                        if ("csv".equals(downloadType)) {
                            sellTxnList.add(txn);
                        } else {
                            // long txnPurpose = txn.getTransactionPurpose().getId();
                            if (txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                                    || IdosConstants.BUY_ON_CREDIT_PAY_LATER == txnPurpose
                                    || IdosConstants.PAY_VENDOR_SUPPLIER == txnPurpose
                                    || IdosConstants.PAY_ADVANCE_TO_VENDOR_OR_SUPPLIER == txnPurpose
                                    || IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT == txnPurpose
                                    || IdosConstants.CREDIT_NOTE_VENDOR == txnPurpose
                                    || IdosConstants.DEBIT_NOTE_VENDOR == txnPurpose) {
                                buyTxnList.add(txn);

                            } else if (IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW == txnPurpose
                                    || IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER == txnPurpose
                                    || IdosConstants.RECEIVE_PAYMENT_FROM_CUSTOMER == txnPurpose
                                    || IdosConstants.RECEIVE_ADVANCE_FROM_CUSTOMER == txnPurpose
                                    || IdosConstants.PREPARE_QUOTATION == txnPurpose
                                    || IdosConstants.CREDIT_NOTE_CUSTOMER == txnPurpose
                                    || IdosConstants.DEBIT_NOTE_CUSTOMER == txnPurpose
                                    || IdosConstants.REFUND_ADVANCE_RECEIVED == txnPurpose
                                    || IdosConstants.REFUND_AMOUNT_RECEIVED_AGAINST_INVOICE == txnPurpose) {
                                sellTxnList.add(txn);
                            } else {
                                otherTxnList.add(txn);
                            }
                        }
                        // txnList.add(id);
                    } else { // DATA from PROVISION_JOURNAL_ENTRY table
                        IdosProvisionJournalEntry usrTxn = IdosProvisionJournalEntry.findById(id);
                        if (usrTxn != null) {
                            pjeTxnList.add(usrTxn);
                        }
                        // pjeList.add(id);
                    }
                }
            }
            // LIST.put("TXN_LIST", txnList);
            // LIST.put("PJE_LIST", pjeList);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
        }
    }

}
