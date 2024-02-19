package service;

import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import controllers.StaticController;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import play.libs.Json;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import play.Application;
import javax.inject.Inject;

/**
 * Created by Sunil K. Namdev on 15-07-2017.
 */
public class VendorServiceImpl implements VendorService {
    private final Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Inject
    public VendorServiceImpl(Application application) {
        this.application = application;
    }

    public ObjectNode getSpecifics(final Long orgId, final Integer type) {
        log.log(Level.FINE, "============ Start");
        ObjectNode result = Json.newObject();
        result.put("result", false);
        result.put("message", "Oops! Something went wrong. Please try again later.");
        if (null == orgId || orgId.equals(0)) {
            result.put("message", "Cannot find the organization!");
        } else if (null == type || type.equals(0)) {
            result.put("message", "Cannot find the Item!");
        } else {
            StringBuilder sbquery = new StringBuilder();
            sbquery.append("select obj from Specifics obj where obj.organization.id=").append(orgId);
            sbquery.append(" AND obj.particularsId.organization=").append(orgId);
            long accountCode = 0;
            if (1 == type) {
                accountCode = 1000000000000000000L;
            } else if (2 == type) {
                accountCode = 2000000000000000000L;
            } else if (3 == type) {
                accountCode = 3000000000000000000L;
            } else if (4 == type) {
                accountCode = 4000000000000000000L;
            }
            sbquery.append(" and obj.presentStatus=1 AND obj.particularsId.accountCode=").append(accountCode);
            List<Specifics> itemList = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
            if (null == itemList || itemList.isEmpty() || itemList.size() == 0) {
                result.put("message", "Cannot find the items!");
            } else {
                ArrayNode datas = result.putArray("items");
                ObjectNode row = null;
                for (Specifics specific : itemList) {
                    if (null != specific && null != specific.getId() && null != specific.getName()
                            && !"".equals(specific.getName())) {
                        row = Json.newObject();
                        row.put("itemId", specific.getId());
                        row.put("itemName", specific.getName());
                        datas.add(row);
                    }
                    result.put("result", true);
                    result.remove("message");
                }
            }
        }
        return result;
    }

    public ObjectNode search(final Long custVendId, final int type, final String txnRefNumber, final Long txnType,
            final Long category, final Long item,
            final String fromDate, final String toDate, final Long branch, final Long project, final Double fromAmount,
            final Double toAmount, final String status) throws ParseException {
        log.log(Level.FINE, "============ Start");
        ObjectNode result = Json.newObject();
        result.put("result", false);
        result.put("message", "Oops! Something went wrong. Please try again later.");
        if (null == custVendId || custVendId == 0) {
            result.put("message", "Unable to fetch the details. Please login again and try.");
        } else {
            StringBuilder query = new StringBuilder();
            query.append("SELECT obj FROM Transaction obj WHERE obj.presentStatus = 1");
            query.append(" AND obj.transactionVendorCustomer.id = ").append(custVendId);
            if (null != txnRefNumber && !"".equals(txnRefNumber)) {
                query.append(" AND obj.transactionRefNumber LIKE '%").append(txnRefNumber).append("%'");
            }
            if (null != txnType && txnType != 0) {
                query.append(" AND obj.transactionPurpose.id = ").append(txnType);
            }
            if (null != category && category != 0) {
                if (null != item && item != 0) {
                    query.append(" AND obj.transactionSpecifics.id = ").append(item);
                }
            }
            Date fromdate, todate;
            if (null != fromDate && !"".equals(fromDate)) {
                fromdate = StaticController.mysqldf
                        .parse(StaticController.mysqldf.format(StaticController.idosdf.parse(fromDate)));
                if (null == toDate && "".equals(toDate)) {
                    todate = Calendar.getInstance().getTime();
                } else {
                    todate = StaticController.mysqldf
                            .parse(StaticController.mysqldf.format(StaticController.idosdf.parse(toDate)));
                }
                String currentDate, previousDate;
                if (todate.after(fromdate)) {
                    currentDate = StaticController.mysqldf.format(todate);
                    currentDate += " 23:59:59";
                    previousDate = StaticController.mysqldf.format(fromdate);
                    previousDate += " 00:00:00";
                } else {
                    currentDate = StaticController.mysqldf.format(fromdate);
                    currentDate += " 23:59:59";
                    previousDate = StaticController.mysqldf.format(todate);
                    previousDate += " 00:00:00";
                }
                query.append(" AND obj.createdAt <= '").append(currentDate).append("' AND obj.createdAt >= '")
                        .append(previousDate).append("'");
            }
            if (null != branch && branch != 0) {
                query.append(" AND obj.transactionBranch.id = ").append(branch);
            }
            if (null != project && project != 0) {
                query.append(" AND obj.transactionProject.id = ").append(project);
            }
            if (null != fromAmount && null != toAmount) {
                if (fromAmount > toAmount) {
                    query.append(" AND obj.netAmount <= ").append(fromAmount).append(" AND obj.netAmount >= ")
                            .append(toAmount);
                } else if (toAmount > fromAmount) {
                    query.append(" AND obj.netAmount <= ").append(toAmount).append(" AND obj.netAmount >= ")
                            .append(fromAmount);
                }
            }
            if (null != status && !"".equals(status)) {
                query.append(" AND obj.transactionStatus = '").append(status).append("'");
            }
            List<Transaction> transactions = genericDAO.executeSimpleQuery(query.toString(), entityManager);
            if (null != transactions && transactions.size() != 0) {
                Double currentOutstandings = 0.0, outstandingVendorSpecialAdjustments = 0.0,
                        receivedSpecialAdjustments = 0.0;
                Double paidSpecialAdjustments = 0.0;
                ObjectNode event = null;
                ArrayNode userTxnData = result.putArray("userTxnData"),
                        totalOutstandingsData = result.putArray("totalOutstandingsData");
                for (Transaction vendCustTxn : transactions) {
                    if (null != vendCustTxn) {
                        event = Json.newObject();
                        event.put("id", vendCustTxn.getId());
                        if (vendCustTxn.getTransactionBranch() != null) {
                            event.put("branchName", vendCustTxn.getTransactionBranch().getName());
                        } else {
                            event.put("branchName", "");
                        }
                        if (null != vendCustTxn.getCreatedBy()) {
                            event.put("email", vendCustTxn.getCreatedBy().getEmail());
                        }
                        if (vendCustTxn.getTransactionProject() != null) {
                            event.put("projectName", vendCustTxn.getTransactionProject().getName());
                        } else {
                            event.put("projectName", "");
                        }
                        if (vendCustTxn.getTransactionSpecifics() != null) {
                            event.put("itemName", vendCustTxn.getTransactionSpecifics().getName());
                        } else {
                            event.put("itemName", "");
                        }
                        String transactionVendCustOutstandings = "";
                        if (vendCustTxn.getTransactionPurpose().getId() == 1L) {
                            if (vendCustTxn.getCustomerNetPayment() != null) {
                                transactionVendCustOutstandings += "Amount Received On This Invoice:"
                                        + vendCustTxn.getCustomerNetPayment();
                            } else {
                                transactionVendCustOutstandings += "Amount Received On This Invoice:"
                                        + vendCustTxn.getNetAmount();
                            }
                            if (vendCustTxn.getCustomerDuePayment() != null) {
                                currentOutstandings += vendCustTxn.getCustomerDuePayment();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getCustomerDuePayment();
                            } else {
                                currentOutstandings += 0.0;
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
                            }
                        }
                        if (vendCustTxn.getTransactionPurpose().getId() == 2L) {
                            if (vendCustTxn.getCustomerNetPayment() != null) {
                                transactionVendCustOutstandings += "Amount Received On This Invoice:"
                                        + vendCustTxn.getCustomerNetPayment();
                            } else {
                                transactionVendCustOutstandings += "Amount Received On This Invoice:" + 0.0;
                            }
                            if (vendCustTxn.getCustomerDuePayment() != null) {
                                currentOutstandings += vendCustTxn.getCustomerDuePayment();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getCustomerDuePayment();
                            } else {
                                currentOutstandings += vendCustTxn.getNetAmount();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getNetAmount();
                            }
                        }
                        if (vendCustTxn.getTransactionPurpose().getId() == 3L) {
                            if (vendCustTxn.getVendorNetPayment() != null) {
                                transactionVendCustOutstandings += "Amount Paid On This Invoice:"
                                        + vendCustTxn.getVendorNetPayment();
                            } else {
                                transactionVendCustOutstandings += "Amount Paid On This Invoice:"
                                        + vendCustTxn.getNetAmount();
                            }
                            if (vendCustTxn.getVendorDuePayment() != null) {
                                currentOutstandings += vendCustTxn.getVendorDuePayment();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getVendorDuePayment();
                            } else {
                                currentOutstandings += 0.0;
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
                            }
                        }
                        if (vendCustTxn.getTransactionPurpose().getId() == 4L) {
                            if (vendCustTxn.getVendorNetPayment() != null) {
                                transactionVendCustOutstandings += "Amount Paid On This Invoice:"
                                        + vendCustTxn.getVendorNetPayment();
                            } else {
                                transactionVendCustOutstandings += "Amount Paid On This Invoice:" + 0.0;
                            }
                            if (vendCustTxn.getVendorDuePayment() != null) {
                                currentOutstandings += vendCustTxn.getVendorDuePayment();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getVendorDuePayment();
                            } else {
                                currentOutstandings += vendCustTxn.getNetAmount();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getNetAmount();
                            }
                        }
                        if (vendCustTxn.getTransactionPurpose().getId() == 5L) {
                            if (vendCustTxn.getCustomerNetPayment() != null) {
                                transactionVendCustOutstandings += "Amount Received On This Invoice:"
                                        + vendCustTxn.getCustomerNetPayment();
                            } else {
                                transactionVendCustOutstandings += "Amount Received On This Invoice:"
                                        + vendCustTxn.getNetAmount();
                            }
                            if (vendCustTxn.getCustomerDuePayment() != null) {
                                currentOutstandings += vendCustTxn.getCustomerDuePayment();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getCustomerDuePayment();
                            } else {
                                currentOutstandings += 0.0;
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
                            }
                        }
                        if (vendCustTxn.getTransactionPurpose().getId() == 6L) {
                            if (vendCustTxn.getCustomerNetPayment() != null) {
                                transactionVendCustOutstandings += "Amount Received On This Invoice:"
                                        + vendCustTxn.getCustomerNetPayment();
                            } else {
                                transactionVendCustOutstandings += "Amount Received On This Invoice:"
                                        + vendCustTxn.getNetAmount();
                            }
                            if (vendCustTxn.getCustomerDuePayment() != null) {
                                currentOutstandings += vendCustTxn.getCustomerDuePayment();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getCustomerDuePayment();
                            } else {
                                currentOutstandings += 0.0;
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
                            }
                        }
                        if (vendCustTxn.getTransactionPurpose().getId() == 7L) {
                            if (vendCustTxn.getVendorNetPayment() != null) {
                                transactionVendCustOutstandings += "Amount Paid On This Invoice:"
                                        + vendCustTxn.getVendorNetPayment();
                            } else {
                                transactionVendCustOutstandings += "Amount Paid On This Invoice:"
                                        + vendCustTxn.getNetAmount();
                            }
                            if (vendCustTxn.getVendorDuePayment() != null) {
                                currentOutstandings += vendCustTxn.getVendorDuePayment();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getVendorDuePayment();
                            } else {
                                currentOutstandings += 0.0;
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
                            }
                        }
                        if (vendCustTxn.getTransactionPurpose().getId() == 8L) {
                            if (vendCustTxn.getVendorNetPayment() != null) {
                                transactionVendCustOutstandings += "Amount Paid On This Invoice:"
                                        + vendCustTxn.getVendorNetPayment();
                            } else {
                                transactionVendCustOutstandings += "Amount Paid On This Invoice:"
                                        + vendCustTxn.getNetAmount();
                            }
                            if (vendCustTxn.getVendorDuePayment() != null) {
                                currentOutstandings += vendCustTxn.getVendorDuePayment();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getVendorDuePayment();
                            } else {
                                currentOutstandings += 0.0;
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
                            }
                        }
                        if (vendCustTxn.getTransactionPurpose().getId() == 9L) {
                            if (vendCustTxn.getNetAmount() != null) {
                                receivedSpecialAdjustments = vendCustTxn.getNetAmount();
                            }
                        }
                        if (vendCustTxn.getTransactionPurpose().getId() == 10L
                                && !vendCustTxn.getTransactionStatus().equals("Rejected")) {
                            if (vendCustTxn.getNetAmount() != null) {
                                paidSpecialAdjustments = vendCustTxn.getNetAmount();
                            }
                        }
                        if (vendCustTxn.getTransactionPurpose().getId() == 12L) {
                            if (vendCustTxn.getCustomerNetPayment() != null) {
                                transactionVendCustOutstandings += "Amount Received On This Invoice:"
                                        + vendCustTxn.getCustomerNetPayment();
                            } else {
                                transactionVendCustOutstandings += "Amount Received On This Invoice:"
                                        + vendCustTxn.getNetAmount();
                            }
                            if (vendCustTxn.getCustomerDuePayment() != null) {
                                currentOutstandings += vendCustTxn.getCustomerDuePayment();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getCustomerDuePayment();
                            } else {
                                currentOutstandings += 0.0;
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
                            }
                        }
                        if (vendCustTxn.getTransactionPurpose().getId() == 13L) {
                            if (vendCustTxn.getVendorNetPayment() != null) {
                                transactionVendCustOutstandings += "Amount Paid On This Invoice:"
                                        + vendCustTxn.getVendorNetPayment();
                            } else {
                                transactionVendCustOutstandings += "Amount Paid On This Invoice:"
                                        + vendCustTxn.getNetAmount();
                            }
                            if (vendCustTxn.getVendorDuePayment() != null) {
                                currentOutstandings += vendCustTxn.getVendorDuePayment();
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:"
                                        + vendCustTxn.getVendorDuePayment();
                            } else {
                                currentOutstandings += 0.0;
                                transactionVendCustOutstandings += ",Amount Due On This Invoice:" + 0.0;
                            }
                        }
                        event.put("transactionVendCustOutstandings", transactionVendCustOutstandings);
                        event.put("transactionPurpose", vendCustTxn.getTransactionPurpose().getTransactionPurpose());
                        event.put("txnDate", StaticController.idosdf.format(vendCustTxn.getTransactionDate()));
                        String invoiceDate = "";
                        String invoiceDateLabel = "";
                        if (vendCustTxn.getTransactionInvoiceDate() != null) {
                            invoiceDateLabel = "INVOICE DATE:";
                            invoiceDate = StaticController.idosdf.format(vendCustTxn.getTransactionInvoiceDate());
                        }
                        event.put("invoiceDateLabel", invoiceDateLabel);
                        event.put("invoiceDate", invoiceDate);
                        if (vendCustTxn.getReceiptDetailsType() != null) {
                            if (vendCustTxn.getReceiptDetailsType() == 1) {
                                event.put("paymentMode", "CASH");
                            }
                            if (vendCustTxn.getReceiptDetailsType() == 2) {
                                event.put("paymentMode", "BANK");
                            }
                        } else {
                            event.put("paymentMode", "");
                        }
                        if (vendCustTxn.getNoOfUnits() != null) {
                            event.put("noOfUnit", vendCustTxn.getNoOfUnits());
                        } else {
                            event.put("noOfUnit", "");
                        }
                        if (vendCustTxn.getPricePerUnit() != null) {
                            event.put("unitPrice", vendCustTxn.getPricePerUnit());
                        } else {
                            event.put("unitPrice", "");
                        }
                        if (vendCustTxn.getGrossAmount() != null) {
                            event.put("grossAmount", vendCustTxn.getGrossAmount());
                        } else {
                            event.put("grossAmount", "");
                        }
                        if (vendCustTxn.getVendCustRemarks() != null) {
                            event.put("vendCustRemarks", vendCustTxn.getVendCustRemarks());
                        } else {
                            event.put("vendCustRemarks", "");
                        }
                        event.put("netAmount", vendCustTxn.getNetAmount());
                        if (vendCustTxn.getNetAmountResultDescription() != null
                                && !vendCustTxn.getNetAmountResultDescription().equals("null")) {
                            event.put("netAmtDesc", vendCustTxn.getNetAmountResultDescription());
                        } else {
                            event.put("netAmtDesc", "");
                        }
                        event.put("status", vendCustTxn.getTransactionStatus());
                        event.put("vendCustAcceptence", vendCustTxn.getVendCustAcceptence());
                        userTxnData.add(event);
                    }
                }
                outstandingVendorSpecialAdjustments = receivedSpecialAdjustments - paidSpecialAdjustments;
                ObjectNode outstandings = Json.newObject();
                outstandings.put("type", type);
                outstandings.put("currentOutstandings", currentOutstandings);
                outstandings.put("outstandingVendorSpecialAdjustments", outstandingVendorSpecialAdjustments);
                totalOutstandingsData.add(outstandings);
            }
            result.put("result", true);
            result.remove("message");
        }
        return result;
    }

    public ObjectNode getStatements(final long id, final String type, final long org, final long branch,
            final String fromDate,
            final String toDate, final int getType) throws ParseException {
        log.log(Level.FINE, "============ Start");
        ObjectNode result = Json.newObject();
        result.put("result", false);
        result.put("message", "Oops! Something went wrong. Please try again later.");
        if (id != 0 && org != 0 && (null != type || !"".equals(type))) {
            Vendor vendor = Vendor.findById(id);
            if (null != vendor) {
                List<VendorCustomerStatementDateComparator> statements = getStatementTransactions(vendor, type, org,
                        branch, fromDate, toDate, getType);
                if (statements.isEmpty()) {
                    result.put("message", "No Transactions recorded!");
                } else {
                    ObjectNode row = null;
                    String created = null;
                    ArrayNode datas = result.putArray("statements");
                    for (VendorCustomerStatementDateComparator statement : statements) {
                        if (null != statement) {
                            row = Json.newObject();
                            created = (null == statement.getCreatedDate()) ? ""
                                    : DateUtil.mysqldf.format(statement.getCreatedDate());
                            row.put("balance", statement.getOpeningBalance());
                            row.put("name", statement.getAccountName());
                            row.put("created", created);
                            if (vendor.getType().equals(1)) {
                                if (null == statement.getCredit()) {
                                    row.put("debit", statement.getDebit());
                                    row.put("credit", "credit");
                                } else {
                                    row.put("credit", statement.getCredit());
                                    row.put("debit", 0);
                                }
                            } else if (vendor.getType().equals(2)) {
                                row.put("debit", statement.getDebit());
                                row.put("credit", 0);
                            }
                            datas.add(row);
                        }
                    }
                }
                result.put("result", true);
                result.remove("message");
            }
        }
        return result;
    }

    public ObjectNode exportStatements(final long id, final String type, final long org, final long branch,
            final String fromDate,
            final String toDate, final int getType, final String branchName, final String fileType)
            throws ParseException, IOException {
        log.log(Level.FINE, "============ Start");
        ObjectNode result = Json.newObject();
        result.put("result", false);
        result.put("message", "Oops! Something went wrong. Please try again later.");
        if (id != 0 && org != 0 && (null != type || !"".equals(type))) {
            Vendor vendor = Vendor.findById(id);
            if (null != vendor) {
                List<VendorCustomerStatementDateComparator> statements = getStatementTransactions(vendor, type, org,
                        branch, fromDate, toDate, getType);
                if (statements.isEmpty() || statements.size() <= 1) {
                    result.put("message", "No Transactions recorded!");
                } else {
                    VendorCustomerStatementDateComparator statement = statements.get(0);
                    if (null != statement) {
                        StringBuilder fileName = new StringBuilder("transaction-statement");
                        fileName.append(Calendar.getInstance().getTimeInMillis()).append(".").append(fileType);
                        String path = application.path().toString() + "/public/report/" + fileName;
                        String path1 = application.path().toString() + "/target/scala-2.10/classes/public/report/"
                                + fileName;
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        Double openingBalance = (null == statement.getOpeningBalance()) ? 0.0
                                : statement.getOpeningBalance();
                        statements.remove(0);
                        Map<String, Object> params = getParams(branchName, fromDate, toDate, openingBalance);
                        out = dynReportService.generateStaticReport("customerVendorStatementTransactions", statements,
                                params, fileType, null);
                        File file = new File(path);
                        File file1 = new File(path1);
                        if (file.exists()) {
                            file.delete();
                        }
                        if (file1.exists()) {
                            file1.delete();
                        }
                        FileOutputStream fileOut = new FileOutputStream(path);
                        FileOutputStream fileOut1 = new FileOutputStream(path1);
                        out.writeTo(fileOut);
                        out.writeTo(fileOut1);
                        fileOut.close();
                        fileOut1.close();
                        result.put("result", true);
                        result.put("message", "/assets/report/" + fileName);
                    }
                }
            }
        }
        return result;
    }

    private Map<String, Object> getParams(final String branch, final String fromDate, final String toDate,
            final Double openingBalance) {
        log.log(Level.FINE, "============ Start");
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            if (null == branch) {
                params.put("branchName", "");
            } else {
                params.put("branchName", branch);
            }
            if (null != fromDate && !fromDate.equals("")) {
                params.put("fromDate", StaticController.idosdf.parse(fromDate));
            } else {
                params.put("fromDate", StaticController.idosdf.parse(StaticController.idosdf
                        .format((StaticController.mysqldf.parse(DateUtil.returnOneMonthBackDate())))));
            }
            if (null != toDate && !toDate.equals("")) {
                params.put("toDate", StaticController.idosdf.parse(toDate));
            } else {
                params.put("toDate",
                        StaticController.idosdf.parse(StaticController.idosdf
                                .format(StaticController.mysqldf.parse(DateUtil.returnOneBackDate(
                                        StaticController.mysqldf.format(Calendar.getInstance().getTime()))))));
            }
            params.put("openingBalance", openingBalance);
        } catch (ParseException e) {
            params.put("fromDate", null);
            params.put("toDate", null);
        }
        return params;
    }

    public Double getOpeningBalance(final Vendor vendor, final long branch, final long org, final String fromDate,
            final String toDate, final String type) throws ParseException {
        log.log(Level.FINE, "============ Start");
        Double result = 0.0;
        if (null != vendor && (null != vendor.getId() || vendor.getId() != 0) && org != 0
                && (null != type || !"".equals(type))) {
            String from = null, to = null;
            StringBuilder query = new StringBuilder();
            query.append(
                    "SELECT obj FROM Transaction obj WHERE obj.presentStatus = 1 AND obj.transactionStatus = 'Accounted'");
            query.append(" AND obj.transactionVendorCustomer.id = ").append(vendor.getId());
            query.append(" AND obj.transactionBranchOrganization.id = ").append(org);
            if (branch != 0) {
                query.append(" AND obj.transactionBranch.id = ").append(branch);
            }
            if ((null == fromDate || "".equals(fromDate)) && (null == toDate || "".equals(toDate))) {
                to = DateUtil.returnOneMonthBackDate();
            } else if (null == toDate || "".equals(toDate)) {
                to = StaticController.mysqldf.format(Calendar.getInstance().getTime());
            } else {
                to = DateUtil
                        .returnOneBackDate(StaticController.mysqldf.format(StaticController.idosdf.parse(fromDate)));
            }
            from = DateUtil.returnOneYearBackDate(DateUtil.mysqldf.parse(to));
            query.append(" AND obj.transactionDate BETWEEN '").append(from).append("' AND '").append(to).append("'");
            if (vendor.getType().equals(2)) {
                query.append(" AND obj.transactionPurpose.id IN (").append(1).append(",").append(5).append(",")
                        .append(6).append(",").append(12).append(")");
            } else if (vendor.getType().equals(1)) {
                query.append(" AND obj.transactionPurpose.id IN (").append(3).append(",").append(7).append(",")
                        .append(8).append(",").append(9).append(",").append(10).append(",").append(11).append(",")
                        .append(13).append(")");
            }
            List<Transaction> transactions = genericDAO.executeSimpleQuery(query.toString(), entityManager);
            if (null != transactions && transactions.size() > 0) {
                for (Transaction transaction : transactions) {
                    if (null != transaction && (null != transaction.getNetAmount())) {
                        result += transaction.getNetAmount();
                    }
                }
            }
        }
        return result;
    }

    public Double getOpeningBalance(final long id, final long branch, final long org, final String fromDate,
            final String toDate, final String type) throws ParseException {
        log.log(Level.FINE, "============ Start");
        Double result = 0.0;
        if (id != 0 && org != 0 && (null != type || !"".equals(type))) {
            log.log(Level.FINE, "============ Start");
            Vendor vendor = Vendor.findById(id);
            result = getOpeningBalance(vendor, branch, org, fromDate, toDate, type);
        }
        return result;
    }

    @Override
    public Vendor saveVendor(JsonNode json, Users user, EntityManager em) throws IDOSException {
        return VENDOR_DAO.saveVendor(json, user, em, IdosConstants.VENDOR);
    }

    private List<VendorCustomerStatementDateComparator> getStatementTransactions(final Vendor vendor, final String type,
            final long org, final long branch,
            final String fromDate, final String toDate, final int getType) throws ParseException {
        log.log(Level.FINE, "============ Start");
        List<VendorCustomerStatementDateComparator> statements = Collections.emptyList();
        if (null != vendor && null != vendor.getId()) {
            String from = null, to = null;
            VendorCustomerStatementDateComparator statement = null;
            statements = new ArrayList<VendorCustomerStatementDateComparator>();
            Double openingBalance = getOpeningBalance(vendor, branch, org, fromDate, toDate, type);
            StringBuilder query = new StringBuilder();
            query.append(
                    "SELECT obj FROM Transaction obj WHERE obj.presentStatus = 1 AND obj.transactionStatus = 'Accounted'");
            query.append(" AND obj.transactionVendorCustomer.id = ").append(vendor.getId());
            query.append(" AND obj.transactionBranchOrganization.id = ").append(org);
            if (branch != 0) {
                query.append(" AND obj.transactionBranch.id = ").append(branch);
            }
            if ((null == fromDate || "".equals(fromDate)) && (null == toDate || "".equals(toDate))) {
                to = StaticController.mysqldf.format(Calendar.getInstance().getTime());
                to = DateUtil.returnOneBackDate(to);
                from = DateUtil.returnOneMonthBackDate(DateUtil.mysqldf.parse(to));
            } else if (null == toDate || "".equals(toDate)) {
                to = StaticController.mysqldf.format(Calendar.getInstance().getTime());
                to = DateUtil.returnOneBackDate(to);
                from = DateUtil.returnOneMonthBackDate(DateUtil.mysqldf.parse(to));
            } else if (null == fromDate || "".equals(fromDate)) {
                to = StaticController.mysqldf.format(StaticController.idosdf.parse(toDate));
                from = DateUtil.returnOneMonthBackDate(DateUtil.mysqldf.parse(to));
            } else {
                to = StaticController.mysqldf.format(StaticController.idosdf.parse(toDate));
                from = StaticController.mysqldf.format(StaticController.idosdf.parse(fromDate));
            }
            query.append(" AND obj.transactionDate BETWEEN '").append(from).append("' AND '").append(to).append("'");
            if ("Customer".equalsIgnoreCase(type)) {
                query.append(" AND obj.transactionPurpose.id IN (").append(1).append(",").append(5).append(",")
                        .append(6).append(",").append(12).append(")");
            } else if ("Vendor".equalsIgnoreCase(type)) {
                query.append(" AND obj.transactionPurpose.id IN (").append(3).append(",").append(7).append(",")
                        .append(8).append(",").append(9).append(",").append(10).append(",").append(11).append(",")
                        .append(13).append(")");
            }
            Double balance = openingBalance, netAmount = 0.0;
            Date created = null;
            List<Transaction> transactions = genericDAO.executeSimpleQuery(query.toString(), entityManager);
            if (null != transactions && transactions.size() > 0) {
                for (Transaction transaction : transactions) {
                    if (null != transaction && null != transaction.getTransactionSpecifics()) {
                        netAmount = 0.0;
                        statement = new VendorCustomerStatementDateComparator();
                        statement.setAccountName(transaction.getTransactionSpecifics().getName());
                        if (vendor.getType().equals(1)) {
                            netAmount = (null == transaction.getNetAmount()) ? 0.0 : transaction.getNetAmount();
                            balance += netAmount;
                            if (transaction.getTransactionPurpose().getId().equals(9)) {
                                statement.setDebit(netAmount);
                                statement.setCredit(null);
                            } else {
                                statement.setCredit(netAmount);
                            }
                        } else if (vendor.getType().equals(2)) {
                            netAmount = (null == transaction.getNetAmount()) ? 0.0 : transaction.getNetAmount();
                            balance -= netAmount;
                            statement.setDebit(netAmount);
                        }
                        created = (null == transaction.getCreatedAt()) ? null : transaction.getCreatedAt();
                        statement.setCreatedDate(created);
                        statement.setOpeningBalance(balance);
                    }
                    statements.add(statement);
                }
                Collections.sort(statements, new VendorCustomerStatementDateComparator());
            }
            // setting opening balance to the first element
            statement = new VendorCustomerStatementDateComparator();
            statement.setOpeningBalance(openingBalance);
            statements.add(0, statement);
        }
        return statements;
    }
}
