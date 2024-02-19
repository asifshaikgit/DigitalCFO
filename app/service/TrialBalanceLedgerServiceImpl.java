package service;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.*;
import controllers.BaseController;
import model.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import play.libs.Json;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import play.Application;
import javax.inject.Inject;

/**
 * Created by Sunil Namdev on 17-10-2016.
 */
public class TrialBalanceLedgerServiceImpl implements TrialBalanceLedgerService {
    private static Application application;

    @Inject
    public TrialBalanceLedgerServiceImpl(Application application) {
        this.application = application;
    }

    @Override
    public ByteArrayOutputStream exportTrialBalanceLedger(Users user, EntityManager entityManager, JsonNode json,
            Application application)
            throws IDOSException {
        ByteArrayOutputStream out = null;
        ObjectNode result = null;
        ArrayNode arr = null;
        ArrayList al = new ArrayList<>();
        try {
            Long specificid = json.findValue("specificid") != null ? json.findValue("specificid").asLong() : 0;
            String toplevelaccountcode = json.findValue("toplevelaccountcode") != null
                    ? json.findValue("toplevelaccountcode").asText()
                    : null;

            if (specificid != 0) {
                result = trialBalanceService.getTransactionForHead(user, entityManager, json);
            } else if (toplevelaccountcode != null) {
                ObjectNode particular = Json.newObject();
                ObjectNode partJson = Json.newObject();
                partJson.put("trialBalanceFromDate", json.findValue("fromDate"));
                partJson.put("trialBalanceToDate", json.findValue("toDate"));
                partJson.put("identForDataValid", json.findValue("identForDataValid"));
                partJson.put("trialBalanceForBranch", json.findValue("trialBalBranch"));
                partJson.put("coaAccountCode", json.findValue("toplevelaccountcode"));

                List<TrialBalance> tbList = trialBalanceService.displayTrialBalance(particular, partJson, user,
                        entityManager);

                for (TrialBalance trialBalance : tbList) {
                    ObjectNode tbJson = Json.newObject();
                    tbJson.put("specificid", trialBalance.getSpecId());
                    tbJson.put("headid2", trialBalance.getHeadid2());
                    tbJson.put("identForDataValid", trialBalance.getIdentificationForDataValid());
                    tbJson.put("toplevelaccountcode", trialBalance.getTopLevelAccountCode());
                    tbJson.put("fromDate", json.findValue("fromDate"));
                    tbJson.put("toDate", json.findValue("toDate"));
                    tbJson.put("headType", trialBalance.getHeadType());
                    tbJson.put("trialBalBranch", json.findValue("trialBalBranch"));

                    result = trialBalanceService.getTransactionForHead(user, entityManager, tbJson);

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

            String period = result.get("period").asText();
            String exportType = json.get("exportType").asText();
            String accountName = json.get("accountName").asText();
            Double debitAmt = json.get("debitAmt") != null ? json.get("debitAmt").asDouble() : 0.0;
            Double creditAmt = json.get("creditAmt") != null ? json.get("creditAmt").asDouble() : 0.0;
            Double closingBalance = json.get("closingBalance") != null ? json.get("closingBalance").asDouble() : 0.0;
            Double openingBalance = json.get("openingBalance") != null ? json.get("openingBalance").asDouble() : 0.0;
            ArrayNode transArrayNode = (ArrayNode) result.get("itemTransData");
            toplevelaccountcode = json.findValue("toplevelaccountcode") != null
                    ? json.findValue("toplevelaccountcode").asText()
                    : null;
            String headType = json.findValue("headType") != null ? json.findValue("headType").asText() : null;
            String trialBalBranch = json.findValue("trialBalBranch").asText() != ""
                    ? json.findValue("trialBalBranch").asText()
                    : null;
            Branch branch = null;
            if (trialBalBranch != null) {
                branch = Branch.findById(new Long(trialBalBranch));
            }
            List<TrialBalanceLedgerReport> tbReportList = IdosUtil.convertArrayNodeToList(transArrayNode);
            for (TrialBalanceLedgerReport tbReport : tbReportList) {
                if (tbReport.getTransactionPurpose().equals("Make Provision/Journal Entry")) {
                    if (tbReport.getDebit() == 0) {
                        if (tbReport.getCreditProjectName() != null && !tbReport.getCreditProjectName().equals(""))
                            tbReport.setProjectName(tbReport.getCreditProjectName());
                    } else {
                        if (tbReport.getDebitProjectName() != null && !tbReport.getDebitProjectName().equals(""))
                            tbReport.setProjectName(tbReport.getDebitProjectName());
                    }
                }
            }
            Map<String, Object> params = new HashMap<String, Object>();

            if (user.getOrganization() != null) {
                String companyLogo = FileUtil.getCompanyLogo(user.getOrganization());
                if (companyLogo != null && !"".equals(companyLogo)) {
                    params.put("companyLogo", companyLogo);
                }
                if (user.getOrganization().getName() != null) {
                    params.put("companyName", user.getOrganization().getName());
                }
                if (user.getOrganization().getRegisteredAddress() != null) {
                    String address = user.getOrganization().getRegisteredAddress().replaceAll("\\r\\n|\\r|\\n", " ");
                    params.put("companyAddress", address);

                }
                if (branch != null) {
                    if (branch.getAddress() != null) {
                        String address = branch.getAddress().replaceAll("\\r\\n|\\r|\\n", " ");
                        params.put("branchAddress", address);

                        if (branch.getPhoneNumber() != null)
                            params.put("branchContactNo", branch.getPhoneNumber());
                    }
                } else {
                    if (user.getOrganization().getRegisteredAddress() != null) {
                        String address = user.getOrganization().getRegisteredAddress().replaceAll("\\r\\n|\\r|\\n",
                                " ");
                        params.put("branchAddress", address);

                        if (user.getOrganization().getRegisteredPhoneNumber() != null)
                            params.put("branchContactNo", user.getOrganization().getRegisteredPhoneNumber());
                    }
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
            }

            if (null != period) {
                params.put("period", period);
            } else {
                params.put("period", "");
            }
            if (null != accountName) {
                params.put("accountName", accountName);
            } else {
                params.put("accountName", "");
            }

            if (null != debitAmt) {
                params.put("debitAmount", debitAmt);
            } else {
                params.put("debitAmount", 0D);
            }
            if (creditAmt != null) {
                params.put("creditAmount", creditAmt);
            } else {
                params.put("creditAmount", 0D);
            }
            if (closingBalance != null) {
                params.put("closingBalance", closingBalance);
            } else {
                params.put("closingBalance", 0D);
            }
            if (openingBalance != null) {
                params.put("openingBalance", openingBalance);
            } else {
                params.put("openingBalance", 0D);
            }
            // String reportName = generateReport(exportType, "trialBalanceLedger",
            // tbReportList, params, );
            if (toplevelaccountcode != null
                    && (toplevelaccountcode.startsWith("1") || toplevelaccountcode.startsWith("3"))) {
                if (exportType.equals("xlsx"))
                    out = dynReportService.getJasperPrintFromFileUsingJtable("TrialBalanceLedgerRptXlsx", tbReportList,
                            params, exportType, null, application);
                else if (exportType.equals("pdf"))
                    out = dynReportService.getJasperPrintFromFileUsingJtable("TrialBalanceLedgerRpt", tbReportList,
                            params, exportType, null, application);
            } else if (toplevelaccountcode != null
                    && (toplevelaccountcode.startsWith("2") || toplevelaccountcode.startsWith("4"))) {
                if (headType.equals(IdosConstants.HEAD_PAYROLL_EXPENSE)
                        || headType.equals(IdosConstants.HEAD_PAYROLL_DEDUCTIONS)) {
                    if (exportType.equals("xlsx"))
                        out = dynReportService.getJasperPrintFromFileUsingJtable("payrollTBLedgerReportXlsx",
                                tbReportList, params, exportType, null, application);
                    else if (exportType.equals("pdf"))
                        out = dynReportService.getJasperPrintFromFileUsingJtable("payrollTBLedgerReport", tbReportList,
                                params, exportType, null, application);
                } else {
                    if (exportType.equals("xlsx"))
                        out = dynReportService.getJasperPrintFromFileUsingJtable("TrialBalanceLedgerRptExpXlsx",
                                tbReportList, params, exportType, null, application);
                    else if (exportType.equals("pdf"))
                        out = dynReportService.getJasperPrintFromFileUsingJtable("TrialBalanceLedgerRptExp",
                                tbReportList, params, exportType, null, application);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                    "Error on trial balance ledger download.", ex.getMessage());
        }
        return out;
    }

    private static String generateReport(String exportType, String fileName, List<TrialBalanceLedgerReport> data,
            Map<String, Object> params, String jasperTemplateName) {
        /*
         * if(data == null && data.isEmpty()){
         * return null;
         * }
         */
        try {

            Long timeInMillis = Calendar.getInstance().getTimeInMillis();
            if (IdosConstants.XLSX_TYPE.equalsIgnoreCase(exportType)) {
                fileName += timeInMillis + ".xlsx";
            } else if (IdosConstants.PDF_TYPE.equalsIgnoreCase(exportType)) {
                fileName += timeInMillis + ".pdf";
            } else {
                // todo throw expection invalid report type
            }
            String path = application.path().toString() + "/log/report/" + fileName;

            ByteArrayOutputStream out = dynReportService.generateStaticReport(jasperTemplateName, data, params,
                    exportType, null);

            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fileOut = new FileOutputStream(path);
            out.writeTo(fileOut);
            fileOut.close();
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
        return fileName;
    }

    /**
     * not in use
     */
    @Override
    @Deprecated
    public ObjectNode getTransactionForHead(Users user, EntityManager entityManager, JsonNode json)
            throws IDOSException {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "======= Start " + json);
        }
        ObjectNode result = Json.newObject();
        ArrayNode itemTransData = result.putArray("itemTransData");

        Long headId = json.findValue("specificid") != null ? json.findValue("specificid").asLong() : 0;
        Long headid2 = json.findValue("headid2") != null ? json.findValue("headid2").asLong() : 0;
        int mappingID = json.findValue("identForDataValid") != null ? json.findValue("identForDataValid").asInt() : 0;
        String fmDate = json.findValue("fromDate") != null ? json.findValue("fromDate").asText() : null;
        String tDate = json.findValue("toDate") != null ? json.findValue("toDate").asText() : null;
        String headType = json.findValue("headType") != null ? json.findValue("headType").asText() : null;
        String toplevelaccountcode = json.findValue("toplevelaccountcode") != null
                ? json.findValue("toplevelaccountcode").asText()
                : null;
        Long branchid = json.findValue("trialBalBranch") == null ? 0L : json.findValue("trialBalBranch").asLong();
        String fromDate = null;
        String toDate = null;
        Date fromDateDt = null;
        Date toDateDt = null;
        String period = null;
        try {
            if (fmDate != null && !fmDate.equals("")) {
                fromDateDt = IdosConstants.IDOSDF.parse(fmDate);
                fromDate = IdosConstants.MYSQLDF.format(fromDateDt);
                period = IdosConstants.IDOSDF.format(fromDateDt);
            } else {
                fromDate = DateUtil.getCurrentFinacialStartDate(user.getOrganization().getFinancialStartDate());
                fromDateDt = IdosConstants.MYSQLDF.parse(fromDate);
                period = IdosConstants.IDOSDF.format(fromDateDt);
            }

            if (tDate != null && !tDate.equals("")) {
                toDateDt = IdosConstants.IDOSDF.parse(tDate);
                toDate = IdosConstants.MYSQLDF.format(toDateDt);
                period = period + " to " + IdosConstants.IDOSDF.format(toDateDt);
            } else {
                toDateDt = Calendar.getInstance().getTime();
                toDate = IdosConstants.MYSQLDF.format(toDateDt);
                period = period + " to " + IdosConstants.IDOSDF.format(toDateDt);
            }
            // period = DateUtil.idosdf.format(IdosConstants.idosdf.parse(fromDate));
            // period = period + " to " +
            // DateUtil.idosdf.format(IdosConstants.idosdf.parse(toDate));
        } catch (ParseException ex) {
            log.log(Level.SEVERE, ex.getMessage());
            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
                    "Wrong date format", ex.getMessage());
        }

        result.put("period", period);
        if (toplevelaccountcode != null
                && (toplevelaccountcode.startsWith("1") || toplevelaccountcode.startsWith("2"))) {
            if (headType.equalsIgnoreCase(IdosConstants.HEAD_PAYROLL_EXPENSE)) {
                payrollDAO.getTrialBalancePayrollEarnItems(entityManager, user, headId, fromDateDt, toDateDt, branchid,
                        itemTransData);
            } else {
                // getTrialBalanceCOAItems(entityManager, user, headId, fromDateDt, toDateDt,
                // branchid, itemTransData);
            }
        } /*
           * else if(toplevelaccountcode != null && (toplevelaccountcode.startsWith("3")))
           * {
           * 
           * if(IdosConstants.HEAD_CASH.equalsIgnoreCase(headType)){
           * getTrialBalanceCashTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.CASH, itemTransData);
           * }else if(IdosConstants.HEAD_PETTY.equalsIgnoreCase(headType)){
           * getTrialBalanceCashTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.PETTY_CASH, itemTransData);
           * }else if(IdosConstants.HEAD_BANK.equalsIgnoreCase(headType)){
           * getTrialBalanceBankTrans(entityManager, user, headId, fromDate, toDate,
           * itemTransData);
           * }else if(IdosConstants.HEAD_VENDOR.equalsIgnoreCase(headType)){
           * getTrialBalanceCustomerVendorAdvTrans(entityManager, user, headId, fromDate,
           * toDate, IdosConstants.VENDOR, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_CUSTOMER.equalsIgnoreCase(headType)){
           * getTrialBalanceVendorCustomerTrans(entityManager, user, headId, fromDateDt,
           * toDateDt, IdosConstants.CUSTOMER, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_TAXS.equalsIgnoreCase(headType) || mappingID ==
           * 14){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.INPUT_TAX, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_SGST.equalsIgnoreCase(headType) || mappingID ==
           * 39){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.INPUT_SGST, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_CGST.equalsIgnoreCase(headType) || mappingID ==
           * 40){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.INPUT_CGST, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_IGST.equalsIgnoreCase(headType) || mappingID ==
           * 41){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.INPUT_IGST, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_CESS.equalsIgnoreCase(headType) || mappingID ==
           * 42){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.INPUT_CESS, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_RCM_SGST_IN.equalsIgnoreCase(headType) ||
           * mappingID == 53){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.RCM_SGST_IN, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_RCM_CGST_IN.equalsIgnoreCase(headType) ||
           * mappingID == 54){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.RCM_CGST_IN, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_RCM_IGST_IN.equalsIgnoreCase(headType) ||
           * mappingID == 55){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.RCM_IGST_IN, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_RCM_CESS_IN.equalsIgnoreCase(headType) ||
           * mappingID == 56){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.RCM_CESS_IN, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_TDS.equalsIgnoreCase(headType) || mappingID ==
           * 8){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.INPUT_TDS, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_TRVL_ADV.equalsIgnoreCase(headType)) {
           * getTrialBalanceUserAdvanceTrans(entityManager, user, headId, fromDate,
           * toDate, headType, itemTransData);
           * }else if(IdosConstants.HEAD_EXP_ADV.equalsIgnoreCase(headType)) {
           * getTrialBalanceUserAdvanceTrans(entityManager, user, headId, fromDate,
           * toDate, headType, itemTransData);
           * }else if(IdosConstants.HEAD_INTR_BRANCH_OUT.equalsIgnoreCase(headType) ||
           * IdosConstants.HEAD_INTR_BRANCH_IN.equalsIgnoreCase(headType)) {
           * getTrialBalanceInterBranchTxn(entityManager, user, headId, headid2,
           * fromDateDt, toDateDt, branchid, itemTransData, headType);
           * }else{
           * getTrialBalanceCOAItems(entityManager, user, headId, fromDateDt, toDateDt,
           * branchid, itemTransData);
           * }
           * }else if(toplevelaccountcode != null &&
           * (toplevelaccountcode.startsWith("4"))) {
           * if(IdosConstants.HEAD_CUSTOMER.equalsIgnoreCase(headType)){
           * getTrialBalanceCustomerVendorAdvTrans(entityManager, user, headId, fromDate,
           * toDate, IdosConstants.CUSTOMER, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_VENDOR.equalsIgnoreCase(headType)){
           * getTrialBalanceVendorCustomerTrans(entityManager, user, headId, fromDateDt,
           * toDateDt, IdosConstants.VENDOR, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_TAXS.equalsIgnoreCase(headType) || mappingID ==
           * 15){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.OUTPUT_TAX, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_SGST.equalsIgnoreCase(headType) || mappingID ==
           * 43){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.OUTPUT_SGST, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_CGST.equalsIgnoreCase(headType) || mappingID ==
           * 44){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.OUTPUT_CGST, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_IGST.equalsIgnoreCase(headType) || mappingID ==
           * 45){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.OUTPUT_IGST, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_CESS.equalsIgnoreCase(headType) || mappingID ==
           * 46){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.OUTPUT_CESS, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_RCM_SGST_OUTPUT.equalsIgnoreCase(headType) ||
           * mappingID == 47){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.RCM_SGST_OUTPUT, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_RCM_CGST_OUTPUT.equalsIgnoreCase(headType) ||
           * mappingID == 48){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.RCM_CGST_OUTPUT, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_RCM_IGST_OUTPUT.equalsIgnoreCase(headType) ||
           * mappingID == 49){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.RCM_IGST_OUTPUT, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_RCM_CESS_OUTPUT.equalsIgnoreCase(headType) ||
           * mappingID == 50){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.RCM_CESS_OUTPUT, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_TDS.equalsIgnoreCase(headType) || mappingID ==
           * 9){
           * getTrialBalanceTaxTrans(entityManager, user, headId, fromDate, toDate,
           * IdosConstants.OUTPUT_TDS, itemTransData, branchid);
           * }else if(IdosConstants.HEAD_TRVL_ADV.equalsIgnoreCase(headType)) {
           * getTrialBalanceUserAdvanceTrans(entityManager, user, headId, fromDate,
           * toDate, headType, itemTransData);
           * }else if(IdosConstants.HEAD_EXP_ADV.equalsIgnoreCase(headType)) {
           * getTrialBalanceUserAdvanceTrans(entityManager, user, headId, fromDate,
           * toDate, headType, itemTransData);
           * }else if(IdosConstants.HEAD_EMP_CLAIM.equalsIgnoreCase(headType)) {
           * getTrialBalanceEmployeeClaimTrans(entityManager, user, headId, fromDate,
           * toDate, headType, itemTransData);
           * }else if(headType.equalsIgnoreCase(IdosConstants.HEAD_PAYROLL_DEDUCTIONS)){
           * payrollDAO.getTrialBalancePayrollDeduItems(entityManager, user, headId,
           * fromDateDt, toDateDt, branchid, itemTransData);
           * }else{
           * getTrialBalanceCOAItems(entityManager, user, headId, fromDateDt, toDateDt,
           * branchid, itemTransData);
           * }
           * }
           */
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "======= End " + result);
        }
        return result;
    }

}
