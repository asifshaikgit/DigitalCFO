package controllers;

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
import play.Application;
import views.html.errorPage;
import java.util.logging.Level;
import play.mvc.Http.Request;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;
import javax.inject.Inject;

/**
 * @author Sunil Namdev
 */
public class BuyTransactionController extends StaticController {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    public static Application application;
    private Request request;

    @Inject
    public BuyTransactionController(Application application) {
        super(application);
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private final static String BRANCH_TAX_HQL = "select obj from BranchTaxes obj WHERE obj.organization.id=?1 and obj.branch.id = ?2 and obj.taxRate=?3 and obj.presentStatus=1 and obj.taxType IN (10,11) order by obj.id";
    private final static String BRANCH_RCM_TAX_HQL = "select obj from BranchTaxes obj WHERE obj.organization.id=?1 and obj.branch.id = ?2 and obj.taxRate=?3 and obj.presentStatus=1 and obj.taxType IN (30,31) order by obj.id";

    @Transactional
    public Result getNetAmtAndTaxComponent(Request request) {
        EntityManager entityManager = EntityManagerProvider.getEntityManager();
        ObjectNode result = Json.newObject();
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        ArrayNode branchTaxDetail = result.putArray("branchTaxDetail");
        ArrayNode branchTdsDetail = result.putArray("branchTdsDetail");
        result.put("taxCalculateStatusFalse", true);
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            Map<String, Object> criterias = new HashMap<String, Object>();
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start " + json);
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            // String useremail = json.findValue("useremail").asText();
            String userTxnPurposeText = json.findValue("userTxnPurposeText") == null ? ""
                    : json.findValue("userTxnPurposeText").asText();
            long txnBranchId = json.findValue("txnBranchId") == null ? 0l : json.findValue("txnBranchId").asLong();
            Long txnSpecificsId = json.findValue("txnSpecificsId").asLong();
            Double txnGrossAmt = json.findValue("txnGrossAmt") == null ? 0d : json.findValue("txnGrossAmt").asDouble();
            long txnPurposeValue = json.findValue("txnPurposeValue").asLong();
            Double txnAdjustmentAmount = json.findValue("txnAdjustmentAmount") == null ? 0.0
                    : json.findValue("txnAdjustmentAmount").asDouble();
            long txnGstTaxID = json.findValue("txnGstTaxID") == null ? 0l : json.findValue("txnGstTaxID").asLong();
            Double txnGstTaxRate = (json.findValue("txnGstTaxRate") == null
                    || "".equals(json.findValue("txnGstTaxRate").asText())) ? -1d
                            : json.findValue("txnGstTaxRate").asDouble();
            long txnCessID = json.findValue("txnCessID") == null ? 0l : json.findValue("txnCessID").asLong();
            String txnSelectedVendorCustomer = json.findValue("txnSelectedVendorCustomer") == null ? ""
                    : json.findValue("txnSelectedVendorCustomer").asText();
            String txnDestVendorDetailId = json.findValue("txnDestVendorDetailId") == null ? ""
                    : json.findValue("txnDestVendorDetailId").asText();
            String txnSourceGstinCode = json.findValue("txnSourceGstinCode") == null ? ""
                    : json.findValue("txnSourceGstinCode").asText();
            String txnDestGstinCode = json.findValue("txnDestGstinCode") == null ? ""
                    : json.findValue("txnDestGstinCode").asText();
            int txnTypeOfSupply = json.findValue("txnTypeOfSupply") == null ? 0
                    : json.findValue("txnTypeOfSupply").asInt();

            String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
            Date txnDate = IdosUtil.getFormatedDateWithTime(selectedTxnDate);

            Double newTxnNetAmount = txnGrossAmt;
            int txnSrcGstinStateCode = 0;
            int txnDstnGstinStateCode = 0;
            Vendor vendor = null;
            if (txnSelectedVendorCustomer != null && !"".equals(txnSelectedVendorCustomer)) {
                vendor = Vendor.findById(Long.valueOf(txnSelectedVendorCustomer));
            }
            if (txnSourceGstinCode != null && !"null".equalsIgnoreCase(txnSourceGstinCode)
                    && !"".equals(txnSourceGstinCode) && txnSourceGstinCode.length() > 1) {
                txnSourceGstinCode = txnSourceGstinCode.substring(0, 2);
                txnSrcGstinStateCode = IdosUtil.convertStringToInt(txnSourceGstinCode);
            }
            if (txnDestGstinCode != null && !"null".equalsIgnoreCase(txnDestGstinCode) && !"".equals(txnDestGstinCode)
                    && txnDestGstinCode.length() > 1) {
                txnDestGstinCode = txnDestGstinCode.substring(0, 2);
                txnDstnGstinStateCode = IdosUtil.convertStringToInt(txnDestGstinCode);
            }
            Double totalTaxAmount = 0d;
            Boolean isSezSupply = false;
            if (txnPurposeValue == IdosConstants.DEBIT_NOTE_VENDOR
                    || txnPurposeValue == IdosConstants.CREDIT_NOTE_VENDOR) {
                if (txnTypeOfSupply == 4 || txnTypeOfSupply == 5) {
                    isSezSupply = true;
                }
            }
            if (txnSrcGstinStateCode == txnDstnGstinStateCode && txnGstTaxRate >= 0d && !isSezSupply) {
                ArrayList inparamList = new ArrayList(3);
                inparamList.add(user.getOrganization().getId());
                inparamList.add(txnBranchId);
                inparamList.add(txnGstTaxRate / 2);
                List<BranchTaxes> branchTaxes = null;
                System.out.println("selectedTxnDate" + entityManager);
                if (txnTypeOfSupply == 2 || txnTypeOfSupply == 3) {
                    branchTaxes = genericDAO.queryWithParams(BRANCH_RCM_TAX_HQL, entityManager, inparamList);
                } else {
                    branchTaxes = genericDAO.queryWithParams(BRANCH_TAX_HQL, entityManager, inparamList);
                }
                if (branchTaxes != null) {
                    for (BranchTaxes taxes : branchTaxes) {
                        ObjectNode row = Json.newObject();
                        String taxName = taxes.getTaxName();
                        if (taxName != null && taxName.length() > 4) {
                            row.put("taxName", taxName.substring(0, 4));
                        } else {
                            row.put("taxName", taxName);
                        }
                        Double taxRate = taxes.getTaxRate();
                        if (taxRate != null) {
                            row.put("taxRate", taxRate);
                        } else {
                            row.put("taxRate", "");
                        }
                        row.put("taxid", taxes.getId());
                        Double taxAmount = txnGrossAmt * (taxRate / (100.0));
                        row.put("taxAmount", IdosConstants.decimalFormat.format(taxAmount));
                        row.put("individualTax",
                                taxName + "(+" + taxRate + "%):" + IdosConstants.decimalFormat.format(taxAmount));
                        totalTaxAmount += taxAmount;
                        branchTaxDetail.add(row);
                    }
                }
            } else {
                BranchTaxes igstTax = BranchTaxes.findById(txnGstTaxID);
                if (igstTax != null) {
                    ObjectNode row = Json.newObject();
                    String taxName = igstTax.getTaxName();
                    if (taxName != null && taxName.length() > 4) {
                        row.put("taxName", taxName.substring(0, 4));
                    } else {
                        row.put("taxName", taxName);
                    }
                    Double taxRate = igstTax.getTaxRate();
                    if (taxRate != null) {
                        row.put("taxRate", taxRate);
                    } else {
                        row.put("taxRate", "");
                    }
                    row.put("taxid", igstTax.getId());
                    Double taxAmount = txnGrossAmt * (taxRate / (100.0));
                    row.put("taxAmount", IdosConstants.decimalFormat.format(taxAmount));
                    row.put("individualTax",
                            taxName + "(+" + taxRate + "%):" + IdosConstants.decimalFormat.format(taxAmount));
                    totalTaxAmount += taxAmount;
                    branchTaxDetail.add(row);
                }
            }

            BranchTaxes cessTax = BranchTaxes.findById(txnCessID);
            if (cessTax != null) {
                ObjectNode row = Json.newObject();
                String taxName = cessTax.getTaxName();
                if (taxName != null && taxName.length() > 4) {
                    row.put("taxName", taxName.substring(0, 4));
                } else {
                    row.put("taxName", taxName);
                }
                Double taxRate = cessTax.getTaxRate();
                if (taxRate != null) {
                    row.put("taxRate", taxRate);
                } else {
                    row.put("taxRate", 0.0);
                }
                row.put("taxid", cessTax.getId());
                Double taxAmount = txnGrossAmt * (taxRate / (100.0));
                row.put("taxAmount", IdosConstants.decimalFormat.format(taxAmount));
                row.put("individualTax",
                        taxName + "(+" + taxRate + "%):" + IdosConstants.decimalFormat.format(taxAmount));
                totalTaxAmount += taxAmount;
                branchTaxDetail.add(row);
            }
            result.put("taxTotalAmount", IdosConstants.decimalFormat.format(totalTaxAmount));
            newTxnNetAmount = totalTaxAmount + txnGrossAmt - txnAdjustmentAmount;
            result.put("txnNetAmount", IdosConstants.decimalFormat.format(newTxnNetAmount));

            if (vendor != null && (txnPurposeValue == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
                    || txnPurposeValue == IdosConstants.BUY_ON_CREDIT_PAY_LATER
                    || txnPurposeValue == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
                    || txnPurposeValue == IdosConstants.DEBIT_NOTE_VENDOR
                    || txnPurposeValue == IdosConstants.CREDIT_NOTE_VENDOR)) {
                if (txnTypeOfSupply == 1) {
                    if (vendor.getIsRegistered() == 0 || vendor.getIsBusiness() == 3) { // GSTIn not registered OR Type
                                                                                        // of Vendor is Composition
                                                                                        // deler
                        branchTaxDetail.removeAll();
                        result.put("taxTotalAmount", 0.0);
                        result.put("taxCalculateStatusFalse", false);
                        result.put("txnNetAmount", IdosConstants.decimalFormat.format(txnGrossAmt));
                    }
                }
            }
            // Start TDS
            Specifics specf = Specifics.findById(txnSpecificsId);
            if (specf != null && vendor != null) {
                VENDOR_TDS_SERVICE.calculateTds(result, branchTdsDetail, user, specf, vendor, txnGrossAmt,
                        newTxnNetAmount, txnPurposeValue, txnDate, entityManager);
            } else {
                log.log(Level.SEVERE, "Specific details not found for id " + txnSpecificsId);
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
}
