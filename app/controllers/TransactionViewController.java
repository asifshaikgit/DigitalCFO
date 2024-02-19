package controllers;

import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import com.idos.dbconnection.DatabaseConnection;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import java.util.logging.Level;

import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import pojo.AccountingReport;
import pojo.GenericTransaction;
import views.html.errorPage;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.*;
import play.Application;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import org.hibernate.procedure.ParameterRegistration;

import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

/**
 * @auther Sunil Namdev created on 26.12.2018
 */
public class TransactionViewController extends StaticController {
    private Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    // private Request request;
    // private Http.Session session = request.session();

    @Inject
    public TransactionViewController(Application application) {
        super(application);
        this.application = application;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result getTransactions(Http.Request request) {
        // EntityManager em = getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            JsonNode json = request.body().asJson();
            ArrayNode sessionan = result.putArray("sessionuserTxnData");
            ArrayNode recordsArrayNode = result.putArray("userTxnData");
            int fromRecord = json.findValue("fromRecord") == null ? 0
                    : json.findValue("fromRecord").asInt();
            int toRecord = json.findValue("toRecord") == null ? 20 : json.findValue("toRecord").asInt();
            int perPage = json.findValue("perPage") == null ? 20 : json.findValue("perPage").asInt();
            ObjectNode sessevent = Json.newObject();

            sessionan.add(sessevent);
            BigInteger totalRecords = TRANSACTION_VIEW_SERVICE.getTrnsactionsList(user, recordsArrayNode,
                    entityManager,
                    fromRecord, perPage);
            result.put("totalRecords", String.valueOf(totalRecords));
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
    public Result searchTransactions(Request request) {
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();
        log.log(Level.FINE, ">>>> Start");
        Users user = null;
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        try {
            JsonNode json = request.body().asJson();
            ArrayNode an = result.putArray("userTxnData");
            String useremail = json.findValue("useremail").asText();
            session.adding("email", useremail);
            user = getUserInfo(request);
            StringBuilder sb = new StringBuilder();
            sb.append("select obj from UsersRoles obj where obj.user='" + user.getId()
                    + "' and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id");
            List<UsersRoles> userRoles = genericDAO.executeSimpleQuery(sb.toString(), entityManager);
            String roles = "";
            for (UsersRoles role : userRoles) {
                if (!role.getRole().getName().equals("OFFICERS")) {
                    roles += role.getRole().getName() + ",";
                }
            }
            roles = roles.substring(0, roles.length() - 1);
            Long searchCategory = json.findValue("searchCategory") != null
                    ? json.findValue("searchCategory").asLong()
                    : 0;
            String searchTransactionRefNumber = json.findValue("searchTransactionRefNumber") != null
                    ? json.findValue("searchTransactionRefNumber").asText()
                    : null;
            String searchItems = json.findValue("searchItems") != null
                    ? json.findValue("searchItems").asText()
                    : null;
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
            String searchVendors = json.findValue("searchVendors") != null
                    ? json.findValue("searchVendors").asText()
                    : null;
            String searchCustomers = json.findValue("searchCustomers") != null
                    ? json.findValue("searchCustomers").asText()
                    : null;
            Long searchTxnWithWithoutDoc = json.findValue("searchTxnWithWithoutDoc") != null
                    ? json.findValue("searchTxnWithWithoutDoc").asLong()
                    : 0;
            String searchTxnPyMode = json.findValue("searchTxnPyMode") != null
                    ? json.findValue("searchTxnPyMode").asText()
                    : null;
            Long searchTxnWithWithoutRemarks = json.findValue("searchTxnWithWithoutRemarks") != null
                    ? json.findValue("searchTxnWithWithoutRemarks").asLong()
                    : 0;
            String searchTxnException = json.findValue("searchTxnException") != null
                    ? json.findValue("searchTxnException").asText()
                    : null;
            Double searchAmountRanseLimitFrom = json.findValue("searchAmountRanseLimitFrom") != null
                    ? json.findValue("searchAmountRanseLimitFrom").asDouble()
                    : 0;
            Double searchAmountRanseLimitTo = json.findValue("searchAmountRanseLimitTo") != null
                    ? json.findValue("searchAmountRanseLimitTo").asDouble()
                    : 0;
            Long txnUserType = json.findValue("txnUserType") != null
                    ? json.findValue("txnUserType").asLong()
                    : 0;
            // Long txnQuestion = json.findValue("txnQuestion") != null ?
            // json.findValue("txnQuestion").asLong() : Types.NULL;
            String txnQuestion = json.findValue("txnQuestion") != null
                    ? json.findValue("txnQuestion").asText()
                    : null;

            int fromRecord = json.findValue("fromRecord") == null ? 0
                    : json.findValue("fromRecord").asInt();
            int toRecord = json.findValue("toRecord") == null ? 20 : json.findValue("toRecord").asInt();
            int perPage = json.findValue("perPage") == null ? 20 : json.findValue("perPage").asInt();

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
            conn = DatabaseConnection.getConnection();
            String query = "{CALL search_transactions(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}";
            stmt = conn.prepareCall(query);
            stmt.setLong(1, user.getId());
            stmt.setLong(2, user.getOrganization().getId());
            stmt.setLong(3, txnUserType);
            stmt.setString(4, txnQuestion);
            stmt.setLong(5, searchCategory);
            stmt.setString(6, searchItems);
            stmt.setString(7, searchTxnStatus);
            stmt.setString(8, searchTransactionRefNumber);
            stmt.setString(9, searchTxnBranch);
            stmt.setString(10, searchTxnProjects);
            stmt.setDouble(11, searchAmountRanseLimitFrom);
            stmt.setDouble(12, searchAmountRanseLimitTo);
            stmt.setLong(13, searchTxnWithWithoutDoc);
            stmt.setString(14, searchTxnPyMode);
            stmt.setLong(15, searchTxnWithWithoutRemarks);
            stmt.setString(16, searchTxnException);
            stmt.setString(17, searchVendors);
            stmt.setString(18, searchCustomers);
            stmt.setString(19, fmDt);
            stmt.setString(20, tDt);
            Long debugStoredPorc = 1l;
            stmt.setLong(21, debugStoredPorc);
            stmt.setString(22, user.getEmail());
            stmt.setInt(23, fromRecord);
            stmt.setInt(24, perPage);
            stmt.registerOutParameter(25, Types.INTEGER);
            rs = stmt.executeQuery();
            int totalRecords = stmt.getInt(25);
            System.out.println("<<txnQuestion>>" + totalRecords);
            while (rs.next()) {
                ObjectNode event = Json.newObject();
                String tableName = rs.getString("TABLENAME");
                event.put("id", rs.getLong("ID"));
                Long tranPurId = rs.getLong("TRANSACTION_PURPOSE");
                TransactionPurpose tranPurpose = TransactionPurpose.findById(tranPurId);
                event.put("transactionPurpose", tranPurpose.getTransactionPurpose());
                event.put("transactionPurposeID", tranPurpose.getId());
                Long branchId = rs.getLong("TRANSACTION_BRANCH");
                Branch branch = Branch.findById(branchId);
                String branchName = null;
                if (branch != null) {
                    branchName = branch.getName();
                    event.put("branchName", branch.getName());
                } else {
                    branchName = "";
                    event.put("branchName", "");
                }

                String txnPurpose = tranPurpose.getTransactionPurpose();
                Transaction txn = Transaction.findById(rs.getLong("ID"));
                if (tranPurpose.getId() == IdosConstants.CREDIT_NOTE_VENDOR) {
                    if (txn.getTypeIdentifier() != null) {
                        if (txn.getTypeIdentifier() == 1) {
                            txnPurpose += " - Increase in Price";
                            event.put("txnIdentifier", 1);
                        } else {
                            txnPurpose += " - Increase in Quantity";
                            event.put("txnIdentifier", 2);
                        }
                    } else {
                        event.put("txnIdentifier", 0);
                    }
                } else if (tranPurpose.getId() == IdosConstants.DEBIT_NOTE_VENDOR) {
                    if (txn.getTypeIdentifier() != null) {
                        if (txn.getTypeIdentifier() == 1) {
                            txnPurpose += " - Decrease in Price";
                            event.put("txnIdentifier", 1);
                        } else {
                            txnPurpose += " - Decrease in Quantity";
                            event.put("txnIdentifier", 2);
                        }
                    } else {
                        event.put("txnIdentifier", 0);
                    }
                } else if (tranPurpose.getId() == IdosConstants.CREDIT_NOTE_CUSTOMER) {
                    if (txn.getTypeIdentifier() != null) {
                        if (txn.getTypeIdentifier() == 1) {
                            txnPurpose += " - Decrease in Price";
                            event.put("txnIdentifier", 1);
                        } else {
                            txnPurpose += " - Decrease in Quantity";
                            event.put("txnIdentifier", 2);
                        }
                    } else {
                        event.put("txnIdentifier", 0);
                    }
                } else if (tranPurpose.getId() == IdosConstants.DEBIT_NOTE_CUSTOMER) {
                    if (txn.getTypeIdentifier() != null) {
                        if (txn.getTypeIdentifier() == 1) {
                            txnPurpose += " - Increase in Price";
                            event.put("txnIdentifier", 1);
                        } else {
                            txnPurpose += " - Increase in Quantity";
                            event.put("txnIdentifier", 2);
                        }
                    } else {
                        event.put("txnIdentifier", 0);
                    }
                } else if (tranPurpose.getId() != IdosConstants.MAKE_PROVISION_JOURNAL_ENTRY
                        && tranPurpose.getId() != IdosConstants.PROCESS_PAYROLL
                        && tranPurpose.getId() < IdosConstants.BILL_OF_MATERIAL) {
                    if (txn != null) {
                        event.put("invoiceNumber", txn.getInvoiceNumber() == null ? ""
                                : txn.getInvoiceNumber());
                    }
                    if (txn != null && txn.getTransactionPurpose()
                            .getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
                        if (txn.getTransactionToBranch() != null
                                && txn.getTransactionToBranch().getName() != null) {
                            branchName += " to " + txn.getTransactionToBranch().getName();
                        }
                        if (txn.getTypeIdentifier() != null) {
                            if (txn.getTypeIdentifier() == 1) {
                                txnPurpose += "(Outward)";
                                event.put("txnIdentifier", 1);
                            } else {
                                txnPurpose += "(Inward)";
                                event.put("txnIdentifier", 2);
                            }
                        } else {
                            event.put("txnIdentifier", 0);
                        }
                    } else {
                        event.put("txnIdentifier", 0);
                    }
                    // if (txn != null) {
                    // event.put("txnReferenceNo", txn.getTransactionRefNumber());
                    // }
                } else {
                    event.put("invoiceNumber", "");
                }
                if (txn != null) {
                    event.put("txnReferenceNo", txn.getTransactionRefNumber());
                }
                event.put("userroles", roles);
                event.put("transactionPurpose", txnPurpose);
                if (rs.getDate("TRANSACTION_ACTIONDATE") != null) {
                    event.put("txnDate", idosdf.format(rs.getDate("TRANSACTION_ACTIONDATE")));
                }
                event.put("grossAmount",
                        IdosConstants.decimalFormat.format(rs.getDouble("GROSS_AMOUNT")));
                event.put("netAmount", IdosConstants.decimalFormat.format(rs.getDouble("NET_AMOUNT")));
                event.put("status", rs.getString("TRANSACTION_STATUS"));
                String netAmtDesc = rs.getString("NET_AMOUNT_RESULT_DESCRIPTION");
                if (netAmtDesc != null && !netAmtDesc.equals("null")) {
                    event.put("netAmtDesc", netAmtDesc);
                } else {
                    event.put("netAmtDesc", "");
                }

                event.put("status", rs.getString("TRANSACTION_STATUS"));
                Users createdByUser = Users.findById(rs.getLong("CREATED_BY"));
                event.put("createdBy", createdByUser.getEmail());
                Users approvedByUser = Users.findById(rs.getLong("APPROVER_ACTION_BY"));
                if (approvedByUser != null) {
                    event.put("approverLabel", "APPROVER:");
                    event.put("approverEmail", approvedByUser.getEmail());
                } else {
                    event.put("approverLabel", "");
                    event.put("approverEmail", "");
                }
                if (rs.getString("SUPPORTING_DOCS") != null) {
                    event.put("txnDocument", rs.getString("SUPPORTING_DOCS"));
                } else {
                    event.put("txnDocument", "");
                }
                if (rs.getString("REMARKS") != null) {
                    event.put("txnRemarks", rs.getString("REMARKS"));
                } else {
                    event.put("txnRemarks", "");
                }

                event.put("roles", roles);
                event.put("useremail", user.getEmail());
                event.put("approverEmails", rs.getString("APPROVER_EMAILS"));
                event.put("additionalapproverEmails", rs.getString("ADDITIONAL_APPROVER_USER_EMAILS"));
                event.put("selectedAdditionalApproval", rs.getString("SELECTED_ADDITIONAL_APPROVER"));
                String instrumentNo = rs.getString("INSTRUMENT_NUMBER");
                String instumentDate = rs.getString("INSTRUMENT_DATE");
                event.put("instrumentNumber", instrumentNo == null ? "" : instrumentNo);
                event.put("instrumentDate", instumentDate == null ? "" : instumentDate);
                if (!tableName.equalsIgnoreCase("pv")) { // IF DATA COMING FROM TRANSACTION TABLE
                    String invoiceDate = "";
                    String invoiceDateLabel = "";
                    if (rs.getString("TRANSACTION_INVOICE_DATE") != null
                            && rs.getString("TRANSACTION_INVOICE_DATE") != "") {
                        invoiceDateLabel = "INVOICE DATE:";
                        invoiceDate = IdosConstants.idosdf
                                .format(rs.getDate("TRANSACTION_INVOICE_DATE"));
                    }
                    event.put("invoiceDateLabel", invoiceDateLabel);
                    event.put("invoiceDate", invoiceDate);
                    Long projectId = rs.getLong("TRANSACTION_PROJECT");
                    Project project = Project.findById(projectId);
                    if (project != null) {
                        event.put("projectName", project.getName());
                    } else {
                        event.put("projectName", "");
                    }
                    Long specId = rs.getLong("TRANSACTION_SPECIFICS");
                    Specifics specifics = Specifics.findById(specId);
                    if (specifics != null) {
                        event.put("itemName", specifics.getName());
                    } else {
                        event.put("itemName", "");
                    }

                    if (specifics != null) {
                        if (specifics.getParentSpecifics() != null
                                && !specifics.getParentSpecifics().equals("")) {
                            event.put("itemParentName",
                                    specifics.getParentSpecifics().getName());
                        } else {
                            event.put("itemParentName",
                                    specifics.getParticularsId().getName());
                        }
                    } else {
                        event.put("itemParentName", "");
                    }
                    String budgetAvaDuringTxn = rs.getString("BUDGET_AVAILABLE_DURING_TXN");
                    String actualAllocatedBudget = rs.getString("ACTUAL_ALLOCATED_BUDGET");
                    if (budgetAvaDuringTxn != null) {
                        String[] budgetAvailableArr = budgetAvaDuringTxn.split(":");
                        event.put("budgetAvailable", budgetAvailableArr[0]);
                        if (budgetAvailableArr.length > 1) {
                            event.put("budgetAvailableAmt", budgetAvailableArr[1]);
                        } else {
                            event.put("budgetAvailableAmt", "");
                        }
                    } else {
                        event.put("budgetAvailable", "");
                        event.put("budgetAvailableAmt", "");
                    }
                    if (actualAllocatedBudget != null) {
                        String[] budgetAllocatedArr = actualAllocatedBudget.split(":");
                        event.put("budgetAllocated", budgetAllocatedArr[0]);
                        if (budgetAllocatedArr.length > 1) {
                            event.put("budgetAllocatedAmt", budgetAllocatedArr[1]);
                        } else {
                            event.put("budgetAllocatedAmt", "");
                        }
                    } else {
                        event.put("budgetAllocated", "");
                        event.put("budgetAllocatedAmt", "");
                    }
                    Long vendId = rs.getLong("TRANSACTION_VENDOR_CUSTOMER");
                    Vendor vendor = Vendor.findById(vendId);
                    if (vendor != null) {
                        event.put("customerVendorName", vendor.getName());
                    } else {
                        if (rs.getString("TRANSACTION_UNAVAILABLE_VENDOR_CUSTOMER") != null) {
                            event.put("customerVendorName", rs.getString(
                                    "TRANSACTION_UNAVAILABLE_VENDOR_CUSTOMER"));
                        } else {
                            event.put("customerVendorName", "");
                        }
                    }
                    Integer txnExceedingBud = rs.getInt("TRANSACTION_EXCEEDING_BUDGET");
                    Integer k1FollowedSta = rs.getInt("KLFOLLOWSTATUS");
                    String txnSpecialStatus = "";
                    if (txnExceedingBud != null && k1FollowedSta != null) {
                        if (txnExceedingBud == 1 && k1FollowedSta == 0) {
                            txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
                        }
                        if (txnExceedingBud == 1 && k1FollowedSta == 1) {
                            txnSpecialStatus = "Transaction Exceeding Budget";
                        }
                    }

                    if (txnExceedingBud == null && k1FollowedSta != null) {
                        if (k1FollowedSta == 0) {
                            txnSpecialStatus = "Rules Not Followed";
                        }
                    }
                    if (txnExceedingBud != null && k1FollowedSta == null) {
                        txnSpecialStatus = "Transaction Exceeding Budget";
                    }
                    Integer docRulestatus = rs.getInt("DOC_RULE_STATUS");
                    if (docRulestatus != null && txnExceedingBud != null) {
                        if (docRulestatus == 1 && txnExceedingBud == 1) {
                            txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
                        }
                        if (k1FollowedSta == 1 && txnExceedingBud == 0) {
                            txnSpecialStatus = "Rules Not Followed";
                        }
                    }
                    if (docRulestatus != null && txnExceedingBud == 0) {
                        txnSpecialStatus = "Rules Not Followed";
                    }
                    event.put("txnSpecialStatus", txnSpecialStatus);
                    Integer receiptType = rs.getInt("RECEIPT_DETAILS_TYPE");
                    if (receiptType != null) {
                        if (receiptType == 1) {
                            event.put("paymentMode", "CASH");
                        } else if (receiptType == 2) {
                            event.put("paymentMode", "BANK");
                        } else {
                            event.put("paymentMode", "");
                        }
                    } else {
                        event.put("paymentMode", "");
                    }
                    Double noOfUnits = rs.getDouble("NO_OF_UNITS");
                    if (noOfUnits != null) {
                        event.put("noOfUnit", IdosConstants.decimalFormat.format(noOfUnits));
                    } else {
                        event.put("noOfUnit", "");
                    }
                    event.put("unitPrice", IdosConstants.decimalFormat
                            .format(rs.getDouble("PRICE_PER_UNIT")));
                    String poRef = rs.getString("PO_REFERENCE");
                    if (poRef != null)
                        event.put("poReference", poRef);
                    else
                        event.put("poReference", "");
                } else { // DATA from PROVISION_JOURNAL_ENTRY table
                    event.put("projectName", "");
                    StringBuilder itemParentName = new StringBuilder();
                    StringBuilder creditItemsName = new StringBuilder();
                    StringBuilder debitItemsName = new StringBuilder();
                    IdosProvisionJournalEntry usrTxn = IdosProvisionJournalEntry
                            .findById(rs.getLong("ID"));
                    if (usrTxn != null) {
                        provisionJournalEntryService.getProvisionJournalEntryDetail(
                                entityManager, usrTxn, itemParentName, creditItemsName,
                                debitItemsName);
                    }
                    event.put("itemName", IdosUtil.removeLastChar(debitItemsName.toString()) + "|"
                            + IdosUtil.removeLastChar(creditItemsName.toString()));
                    event.put("debitItemsName", IdosUtil.removeLastChar(debitItemsName.toString()));
                    event.put("creditItemsName",
                            IdosUtil.removeLastChar(creditItemsName.toString()));
                    event.put("itemParentName", IdosUtil.removeLastChar(itemParentName.toString()));

                    String invoiceDate = "";
                    String invoiceDateLabel = "";
                    if (rs.getString("TRANSACTION_INVOICE_DATE") != null
                            && rs.getString("TRANSACTION_INVOICE_DATE") != "") {
                        invoiceDateLabel = "REVERSAL DATE:";
                        invoiceDate = IdosConstants.idosdf
                                .format(rs.getDate("TRANSACTION_INVOICE_DATE"));
                    }
                    event.put("budgetAvailable", "");
                    event.put("budgetAvailableAmt", "");
                    event.put("customerVendorName", "");
                    event.put("invoiceDateLabel", invoiceDateLabel);
                    event.put("invoiceDate", invoiceDate);
                    event.put("paymentMode", "");
                    event.put("noOfUnit", "");
                    event.put("unitPrice", "");
                    event.put("txnReferenceNo", usrTxn.getTransactionRefNumber());

                }
                an.add(event);
            }
            result.put("totalRecords", totalRecords);
        } catch (Exception ex) {
            reportException(entityManager, null, user, ex, result);
        } catch (Throwable th) {
            reportThrowable(entityManager, null, user, th, result);
        }

        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result getAccountingReport(Request request) throws Exception {
        // EntityManager entityManager = getEntityManager();
        JsonNode json = request.body().asJson();
        String transactionEntityId = json.findValue("entityTxnId").asText();
        GenericTransaction genericTransaction = TRANSACTION_VIEW_SERVICE.getTypeofTransaction(
                transactionEntityId,
                entityManager);
        List<AccountingReport> accountingReports = AccountingReport
                .creatAccountingReportList(TRANSACTION_VIEW_SERVICE.getAccountingList(
                        genericTransaction.getId(),
                        genericTransaction.getTransactionPurpose(), entityManager));

        String fileType = json.findValue("reportType").asText().equals(IdosConstants.PDF_TYPE)
                ? IdosConstants.PDF_TYPE
                : IdosConstants.XLSX_TYPE;
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("transactionRefNumber", genericTransaction.getTransactionRefNumber());
        parameters.put("submitter", genericTransaction.getSubmitter());
        parameters.put("approval", genericTransaction.getApproval());
        parameters.put("dateCreated", genericTransaction.getDateCreated());

        String path = application.path().toString() + "/logs/report/";
        Long timeInMillis = Calendar.getInstance().getTimeInMillis();
        String fileName = "accountingreport" + timeInMillis + "." + fileType;
        path = path + fileName;

        ByteArrayOutputStream out = dynReportService.generateStaticReport("accountingreport", accountingReports,
                parameters, fileType, null);

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        FileOutputStream fileOut = new FileOutputStream(path);
        out.writeTo(fileOut);
        fileOut.close();
        return Results.ok(file).withHeader("Content-Type", "application/" + fileType).withHeader(
                "Content-Disposition",
                "attachment; filename=" + fileName);

    }

    @Transactional
    public Result getAccountingInfo(Request request) {
        // EntityManager entityManager = getEntityManager();
        ObjectNode results = Json.newObject();
        ArrayNode an = results.putArray("accountingFileName");
        Users user = getUserInfo(request);
        JsonNode json = request.body().asJson();
        ArrayNode txnItemsan = results.putArray("accountingItems");
        String transactionEntityId = json.findValue("transactionNumber").asText();

        GenericTransaction genericTransaction = TRANSACTION_VIEW_SERVICE.getTypeofTransaction(
                transactionEntityId,
                entityManager);

        List<Object[]> what = entityManager
                .createNativeQuery("call getTransactionAccountingInfo(:parameter,:transactionpurpose)")
                .setParameter("parameter", genericTransaction.getId())
                .setParameter("transactionpurpose", genericTransaction.getTransactionPurpose())
                .getResultList();

        Double totalDebit = 0.00;
        Double totalCredit = 0.00;

        for (int i = 0; i < what.size(); i++) {
            ObjectNode row = Json.newObject();
            row.put("sroId", what.get(i)[0].toString());
            row.put("accountingDetails", what.get(i)[1].toString());
            row.put("creditBalance", what.get(i)[2] != null ? String.format("%.2f", what.get(i)[2]) : "0.00");
            row.put("debitBalance", what.get(i)[3] != null ? String.format("%.2f", what.get(i)[3]) : "0.00");
            totalCredit += Double.valueOf(what.get(i)[2] != null ? what.get(i)[2].toString() : "0.00");
            totalDebit += Double.valueOf(what.get(i)[3] != null ? what.get(i)[3].toString() : "0.00");
            txnItemsan.add(row);
        }
        results.put("totalCredit", String.format("%.2f", totalCredit));
        results.put("totalDebit", String.format("%.2f", totalDebit));
        results.put("createdAt", genericTransaction.getDateCreated());
        return Results.ok(results).withHeader("ContentType", "application/json");
    }

    /*
     * ot in use
     * 
     * @Transactional public Result userTransaction() { // EntityManager
     * entityManager = getEntityManager(); ObjectNode result = Json.newObject();
     * Users user = null; try { user = getUserInfo(request); if (user == null) {
     * return unauthorized(); } JsonNode json = request.body().asJson();
     * log.log(Level.FINE, ">>>> Start" + json.toString()); ArrayNode sessionan =
     * result.putArray("sessionuserTxnData"); ArrayNode recordsArrayNode =
     * result.putArray("userTxnData"); //String sessemail =
     * session.getOptional("email").orElse("");
     * 
     * int perPage = json.findValue("perPage").asInt(); int curPage =
     * json.findValue("curPage").asInt(); ObjectNode sessevent = Json.newObject();
     * //sessevent.put("sessemail", sessemail); sessionan.add(sessevent); //String
     * useremail = json.findValue("usermail").asText();
     * 
     * String sb =
     * ("select obj from UsersRoles obj where obj.user.id=?x and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id"
     * ); ArrayList inParamsTmp = new ArrayList(1); inParamsTmp.add(user.getId());
     * List<UsersRoles> userRoles = genericDAO.queryWithParams(sb, entityManager,
     * inParamsTmp); String roles = ""; for (UsersRoles role : userRoles) { if
     * (!role.getRole().getName().equals("OFFICERS")) { roles +=
     * role.getRole().getName() + ","; } } roles = roles.substring(0, roles.length()
     * - 1); List<Transaction> userTransactionList = null; StringBuilder sbquery =
     * null; StringBuilder countSQL = null; //if role is only of creator if
     * (roles.equals("CREATOR")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE obj.createdBy.id=?x and obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(2); inParams.add(user.getId());
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.equals("APPROVER")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE (obj.approverActionBy.id=?x or LOCATE(?, obj.approverEmails)>0 or obj.selectedAdditionalApprover=?x) and obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(4); inParams.add(user.getId());
     * inParams.add(user.getEmail()); inParams.add(user.getEmail());
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.equals("CREATOR,APPROVER")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE (obj.createdBy.id=?x or obj.approverActionBy.id=?x or LOCATE(?, obj.approverEmails)>0 or obj.selectedAdditionalApprover=?x) and obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(5); inParams.add(user.getId());
     * inParams.add(user.getId()); inParams.add(user.getEmail());
     * inParams.add(user.getEmail()); inParams.add(user.getOrganization().getId());
     * userTransactionList = genericDAO.queryWithParams(sbquery.toString(),
     * entityManager, inParams); } else if (roles.equals("CREATOR,ACCOUNTANT")) {
     * sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(1);
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.equals("CREATOR,CASHIER")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE obj.createdBy.id =?x and obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(2); inParams.add(user.getId());
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.equals("CREATOR,APPROVER,ACCOUNTANT")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(1);
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.equals("CREATOR,APPROVER,CASHIER")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE (obj.createdBy.id=?x or obj.approverActionBy.id=?x or LOCATE(?,obj.approverEmails)>0 or obj.selectedAdditionalApprover=?x) and obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(5); inParams.add(user.getId());
     * inParams.add(user.getId()); inParams.add(user.getEmail());
     * inParams.add(user.getEmail()); inParams.add(user.getOrganization().getId());
     * userTransactionList = genericDAO.queryWithParams(sbquery.toString(),
     * entityManager, inParams);
     * 
     * } else if (roles.equals("CREATOR,ACCOUNTANT,CASHIER")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(1);
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.equals("CREATOR,APPROVER,ACCOUNTANT,CASHIER")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(1);
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.equals("APPROVER,ACCOUNTANT")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(1);
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.equals("APPROVER,CASHIER")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE (obj.approverActionBy.id=?x or LOCATE(?,obj.approverEmails)>0 or obj.selectedAdditionalApprover=?x) and obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * );
     * 
     * ArrayList inParams = new ArrayList(4); inParams.add(user.getId());
     * inParams.add(user.getEmail()); inParams.add(user.getEmail());
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.equals("APPROVER,ACCOUNTANT,CASHIER")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(1);
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.contains("CONTROLLER")) { sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(1);
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.contains("ACCOUNTANT")) { sbquery = new StringBuilder("");
     * sbquery.
     * append("select obj from Transaction obj WHERE obj.transactionBranchOrganization.id=?x and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(1);
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * else if (roles.contains("AUDITOR") && !roles.contains("MASTER ADMIN")) {
     * sbquery = new
     * StringBuilder("select obj from Transaction obj WHERE obj.transactionBranchOrganization.id=?x and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 ORDER BY obj.createdAt desc"
     * ); ArrayList inParams = new ArrayList(1);
     * inParams.add(user.getOrganization().getId()); userTransactionList =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); }
     * Long totalRecords = 0L; // genericDAO.executeCountQuery(countSQL.toString(),
     * entityManager); result.put("totalRecords", totalRecords);
     * 
     * //userTransactionList =
     * genericDAO.executeSimpleQueryWithLimit(sbquery.toString(), entityManager,
     * curPage, perPage); for (Transaction usrTxn : userTransactionList) {
     * ObjectNode event = Json.newObject(); event.put("userroles", roles);
     * event.put("id", usrTxn.getId());
     * 
     * if (usrTxn.getTransactionProject() != null) { event.put("projectName",
     * usrTxn.getTransactionProject().getName()); } else { event.put("projectName",
     * ""); } if (usrTxn.getTransactionSpecifics() != null) { event.put("itemName",
     * usrTxn.getTransactionSpecifics().getName()); } else { event.put("itemName",
     * ""); } if (usrTxn.getTransactionSpecifics() != null) { if
     * (usrTxn.getTransactionSpecifics().getParentSpecifics() != null &&
     * !usrTxn.getTransactionSpecifics().getParentSpecifics().equals("")) {
     * event.put("itemParentName",
     * usrTxn.getTransactionSpecifics().getParentSpecifics().getName()); } else {
     * event.put("itemParentName",
     * usrTxn.getTransactionSpecifics().getParticularsId().getName()); } } else {
     * event.put("itemParentName", ""); } if (usrTxn.getBudgetAvailDuringTxn() !=
     * null) { String[] budgetAvailableArr =
     * usrTxn.getBudgetAvailDuringTxn().split(":"); event.put("budgetAvailable",
     * budgetAvailableArr[0]); if (budgetAvailableArr.length > 1) {
     * event.put("budgetAvailableAmt", budgetAvailableArr[1]); } else {
     * event.put("budgetAvailableAmt", ""); } } else { event.put("budgetAvailable",
     * ""); event.put("budgetAvailableAmt", ""); } if
     * (usrTxn.getActualAllocatedBudget() != null) { String[] budgetAllocatedArr =
     * usrTxn.getActualAllocatedBudget().split(":"); event.put("budgetAllocated",
     * budgetAllocatedArr[0]); if (budgetAllocatedArr.length > 1) {
     * event.put("budgetAllocatedAmt", budgetAllocatedArr[1]); } else {
     * event.put("budgetAllocatedAmt", ""); } } else { event.put("budgetAllocated",
     * ""); event.put("budgetAllocatedAmt", ""); } if
     * (usrTxn.getTransactionVendorCustomer() != null) {
     * event.put("customerVendorName",
     * usrTxn.getTransactionVendorCustomer().getName()); } else { if
     * (usrTxn.getTransactionUnavailableVendorCustomer() != null) {
     * event.put("customerVendorName",
     * usrTxn.getTransactionUnavailableVendorCustomer()); } else {
     * event.put("customerVendorName", ""); } } if (usrTxn.getTransactionBranch() !=
     * null) { event.put("branchName", usrTxn.getTransactionBranch().getName()); }
     * else {
     * 
     * } String branchName = usrTxn.getTransactionBranch().getName() == null ? "" :
     * usrTxn.getTransactionBranch().getName(); String txnPurpose =
     * usrTxn.getTransactionPurpose().getTransactionPurpose(); if
     * (usrTxn.getTransactionPurpose().getId() ==
     * IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) { if
     * (usrTxn.getTransactionToBranch() != null &&
     * usrTxn.getTransactionToBranch().getName() != null) { branchName += " to " +
     * usrTxn.getTransactionToBranch().getName(); } if (usrTxn.getTypeIdentifier()
     * != null) { if (usrTxn.getTypeIdentifier() == 1) { txnPurpose += "(Outward)";
     * event.put("txnIdentifier", 1); } else { txnPurpose += "(Inward)";
     * event.put("txnIdentifier", 2); } } else { event.put("txnIdentifier", 0); } }
     * else { event.put("txnIdentifier", 0); } event.put("branchName", branchName);
     * event.put("transactionPurpose", txnPurpose);
     * event.put("transactionPurposeID", usrTxn.getTransactionPurpose().getId()); if
     * (usrTxn.getTransactionDate() != null) { event.put("txnDate",
     * idosdf.format(usrTxn.getTransactionDate())); } else { event.put("txnDate",
     * ""); } String invoiceDate = ""; String invoiceDateLabel = ""; if
     * (usrTxn.getTransactionInvoiceDate() != null) { invoiceDateLabel =
     * "INVOICE DATE:"; invoiceDate =
     * idosdf.format(usrTxn.getTransactionInvoiceDate()); }
     * event.put("invoiceDateLabel", invoiceDateLabel); event.put("invoiceDate",
     * invoiceDate); if (usrTxn.getReceiptDetailsType() != null) { if
     * (usrTxn.getReceiptDetailsType() == IdosConstants.PAYMODE_CASH) {
     * event.put("paymentMode", "CASH"); } else if (usrTxn.getReceiptDetailsType()
     * == IdosConstants.PAYMODE_BANK) { event.put("paymentMode", "BANK"); } else if
     * (usrTxn.getReceiptDetailsType() == IdosConstants.PAYMODE_PETTY_CASH) {
     * event.put("paymentMode", "PETTYCASH"); } else { event.put("paymentMode", "");
     * } } else { event.put("paymentMode", ""); } if (usrTxn.getNoOfUnits() != null)
     * { event.put("noOfUnit",
     * IdosConstants.DECIMAL_FORMAT.format(usrTxn.getNoOfUnits())); } else {
     * event.put("noOfUnit", ""); } if (usrTxn.getPricePerUnit() != null) {
     * event.put("unitPrice",
     * IdosConstants.decimalFormat.format(usrTxn.getPricePerUnit())); } else {
     * event.put("unitPrice", ""); } if (usrTxn.getFrieghtCharges() != null) {
     * event.put("frieghtCharges",
     * IdosConstants.decimalFormat.format(usrTxn.getFrieghtCharges())); } else {
     * event.put("frieghtCharges", ""); } if (usrTxn.getGrossAmount() != null) {
     * event.put("grossAmount",
     * IdosConstants.DECIMAL_FORMAT.format(usrTxn.getGrossAmount())); } else {
     * event.put("grossAmount", ""); } if (usrTxn.getNetAmount() != null) {
     * event.put("netAmount",
     * IdosConstants.DECIMAL_FORMAT.format(usrTxn.getNetAmount())); } else {
     * event.put("netAmount", ""); } if (usrTxn.getNetAmountResultDescription() !=
     * null && !usrTxn.getNetAmountResultDescription().equals("null")) {
     * event.put("netAmtDesc", usrTxn.getNetAmountResultDescription()); } else {
     * event.put("netAmtDesc", ""); }
     * 
     * if (usrTxn.getPoReference() != null) { event.put("poReference",
     * usrTxn.getPoReference()); } else { event.put("poReference", ""); }
     * 
     * event.put("status", usrTxn.getTransactionStatus()); event.put("createdBy",
     * usrTxn.getCreatedBy().getEmail()); if (usrTxn.getApproverActionBy() != null)
     * { event.put("approverLabel", "APPROVER:"); event.put("approverEmail",
     * usrTxn.getApproverActionBy().getEmail()); } else { event.put("approverLabel",
     * ""); event.put("approverEmail", ""); } if (usrTxn.getSupportingDocs() !=
     * null) { event.put("txnDocument", usrTxn.getSupportingDocs()); } else {
     * event.put("txnDocument", ""); } if (usrTxn.getRemarks() != null) {
     * event.put("txnRemarks", usrTxn.getRemarks()); } else {
     * event.put("txnRemarks", ""); } if (usrTxn.getRemarksPrivate() != null) {
     * event.put("remarksPrivate", usrTxn.getRemarksPrivate()); } else {
     * event.put("remarksPrivate", ""); }
     * 
     * if (usrTxn.getInvoiceNumber() != null) { event.put("invoiceNumber",
     * usrTxn.getInvoiceNumber()); } else { event.put("invoiceNumber", ""); } String
     * txnSpecialStatus = ""; if (usrTxn.getTransactionExceedingBudget() != null &&
     * usrTxn.getKlFollowStatus() != null) { if
     * (usrTxn.getTransactionExceedingBudget() == 1 && usrTxn.getKlFollowStatus() ==
     * 0) { txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
     * } if (usrTxn.getTransactionExceedingBudget() == 1 &&
     * usrTxn.getKlFollowStatus() == 1) { txnSpecialStatus =
     * "Transaction Exceeding Budget"; } } if
     * (usrTxn.getTransactionExceedingBudget() == null && usrTxn.getKlFollowStatus()
     * != null) { if (usrTxn.getKlFollowStatus() == 0) { txnSpecialStatus =
     * "Rules Not Followed"; } } if (usrTxn.getTransactionExceedingBudget() != null
     * && usrTxn.getKlFollowStatus() == null) { txnSpecialStatus =
     * "Transaction Exceeding Budget"; } if (usrTxn.getTransactionExceedingBudget()
     * != null && usrTxn.getKlFollowStatus() == null) { txnSpecialStatus =
     * "Transaction Exceeding Budget"; } if (usrTxn.getDocRuleStatus() != null &&
     * usrTxn.getTransactionExceedingBudget() != null) { if
     * (usrTxn.getDocRuleStatus() == 1 && usrTxn.getTransactionExceedingBudget() ==
     * 1) { txnSpecialStatus = "Transaction Exceeding Budget & Rules Not Followed";
     * } if (usrTxn.getKlFollowStatus() != null && usrTxn.getKlFollowStatus() == 1
     * && usrTxn.getTransactionExceedingBudget() == 0) { txnSpecialStatus =
     * "Rules Not Followed"; } } if (usrTxn.getDocRuleStatus() != null &&
     * usrTxn.getTransactionExceedingBudget() == null) { txnSpecialStatus =
     * "Rules Not Followed"; } event.put("txnSpecialStatus", txnSpecialStatus);
     * event.put("roles", roles); event.put("useremail", user.getEmail());
     * event.put("approverEmails", usrTxn.getApproverEmails());
     * event.put("additionalapproverEmails", usrTxn.getAdditionalApproverEmails());
     * event.put("selectedAdditionalApproval",
     * usrTxn.getSelectedAdditionalApprover()); event.put("instrumentNumber",
     * usrTxn.getInstrumentNumber() == null ? "" : usrTxn.getInstrumentNumber());
     * event.put("instrumentDate", usrTxn.getInstrumentDate() == null ? "" :
     * usrTxn.getInstrumentDate()); event.put("txnReferenceNo",
     * usrTxn.getTransactionRefNumber()); recordsArrayNode.add(event); }
     * provisionJournalEntryService.getProvisionJournalEntryList(user, roles,
     * recordsArrayNode, entityManager); payrollService.getPayrollTxnList(user,
     * roles, recordsArrayNode, entityManager);
     * 
     * if (roles.contains("CREATOR") || roles.contains("APPROVER") ||
     * roles.contains("AUDITOR") || roles.contains("ACCOUNTANT")) { sbquery = new
     * StringBuilder("select COUNT(obj) from Transaction obj WHERE obj.transactionBranchOrganization.id=?x"
     * ); sbquery.
     * append(" AND obj.presentStatus = 1 AND (obj.transactionStatus = 'Require Approval' OR obj.transactionStatus = 'Require Additional Approval')"
     * ); sbquery.
     * append(" AND (obj.approverActionBy.id=?x or LOCATE(?,obj.approverEmails) > 0 or obj.selectedAdditionalApprover=?x or obj.createdBy.id =?x)"
     * ); List<Transaction> list = null;
     * 
     * ArrayList inParams = new ArrayList(5);
     * inParams.add(user.getOrganization().getId()); inParams.add(user.getId());
     * inParams.add(user.getEmail()); inParams.add(user.getEmail());
     * inParams.add(user.getId()); list =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams);
     * 
     * Object row = null; int res = 0; if (list.size() > 0) { row = list.get(0); res
     * = Integer.parseInt(row.toString()); } sbquery = new
     * StringBuilder("select COUNT(obj) from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization.id=?x"
     * ); sbquery.
     * append(" AND obj.presentStatus = 1 AND (obj.transactionStatus = 'Require Approval' OR obj.transactionStatus = 'Require Additional Approval')"
     * ); sbquery.
     * append(" AND (obj.approverActionBy.id=?x or LOCATE(?,obj.approverEmails)>0 or obj.selectedAdditionalApprover=?x or obj.createdBy.id =?x)"
     * ); inParams.clear(); inParams.add(user.getOrganization().getId());
     * inParams.add(user.getId()); inParams.add(user.getEmail());
     * inParams.add(user.getEmail()); inParams.add(user.getId()); list =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams);
     * 
     * if (list.size() > 0) { row = list.get(0); res +=
     * Integer.parseInt(row.toString()); }
     * 
     * result.put("approval", "Require Approval : " + res); sbquery = new
     * StringBuilder("select COUNT(obj) from Transaction obj WHERE obj.transactionBranchOrganization.id=?x"
     * ); sbquery.
     * append(" AND obj.presentStatus = 1 AND obj.transactionStatus = 'Approved'");
     * sbquery.
     * append(" AND (obj.approverActionBy.id=?x or LOCATE(?,obj.approverEmails)>0 or obj.selectedAdditionalApprover=?x or obj.createdBy.id =?x)"
     * ); inParams.clear(); inParams.add(user.getOrganization().getId());
     * inParams.add(user.getId()); inParams.add(user.getEmail());
     * inParams.add(user.getEmail()); inParams.add(user.getId()); list =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams);
     * 
     * if (list.size() > 0) { row = list.get(0); res =
     * Integer.parseInt(row.toString()); } sbquery = new
     * StringBuilder("select COUNT(obj) from IdosProvisionJournalEntry obj WHERE obj.provisionMadeForOrganization.id=?x"
     * ); sbquery.
     * append(" AND obj.presentStatus = 1 AND (obj.transactionStatus = 'Approved')"
     * ); sbquery.
     * append(" AND (obj.approverActionBy.id=?x or LOCATE(?,obj.approverEmails)>0 or obj.selectedAdditionalApprover=?x or obj.createdBy.id =?x)"
     * ); inParams.clear(); inParams.add(user.getOrganization().getId());
     * inParams.add(user.getId()); inParams.add(user.getEmail());
     * inParams.add(user.getEmail()); inParams.add(user.getId()); list =
     * genericDAO.queryWithParams(sbquery.toString(), entityManager, inParams); if
     * (list.size() > 0) { row = list.get(0); res +=
     * Integer.parseInt(row.toString()); } result.put("approved",
     * "Complete Accounting : " + res); } else { result.put("approval", "");
     * result.put("approved", ""); }
     * result.withHeader("ContentType","application/json"); } catch (Exception ex) {
     * reportException(entityManager, null, user, ex, result); }catch (Throwable
     * th){ reportThrowable(entityManager, null, user, th, result); }
     * log.log(Level.FINE, ">>>> End " + result); return Results.ok(result); }
     * 
     */

}
