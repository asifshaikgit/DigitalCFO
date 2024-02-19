package controllers;

import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import model.AgeingReport;
import model.BalanceSheetReport;
import model.ConfigParams;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;

import javax.transaction.Transactional;

import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import play.Application;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import java.util.logging.Level;
import play.mvc.Http.Request;

/**
 * @author Sunil
 * @since 25.11.2017
 */

public class AgeingReportController extends StaticController {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    public static Application application;
    // public Request request;

    @Inject
    public AgeingReportController(JPAApi jpaApi, Application application) {
        super(application);
        this.jpaApi = jpaApi;
        entityManager = EntityManagerProvider.getEntityManager();
        this.application = application;
    }

    @Transactional
    public Result downloadAgeingRpt(Request request) {
        // EntityManager entityManager = getEntityManager();
        ObjectNode results = Json.newObject();
        Users user = null;
        File file = null;
        String fileName = null;
        try {
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>> Start" + json);
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized(results);
            }
            String exportType = json.findValue("exportType").asText();
            String ageingDate = json.findValue("ageingDate").asText();
            String tabid = json.findValue("tabid").asText();
            String rptfileName = json.get("fileName") == null ? null : json.findValue("fileName").asText();
            Long bankId = json.get("bankId") == null ? null : json.findValue("bankId").asLong();
            String reportName = "Receiable";
            String orgName = user.getOrganization().getName() == null ? "" : user.getOrganization().getName().trim();
            orgName = orgName.replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "");
            if (orgName.length() > 8) {
                orgName = orgName.substring(0, 7);
            }
            Long timeInMillis = Calendar.getInstance().getTimeInMillis();
            String type;
            String jsprRptName = "ageingrptpdf";

            Map<String, Object> params = new HashMap<String, Object>();
            IdosUtil.seOrganization4Report(params, user.getOrganization());
            boolean isPayable = false;
            if ("accountsPayablesAllBranches".equals(tabid)) {
                params.put("message", "Ageing Report for Payables, Date: " + ageingDate);
                reportName = "Payable";
                isPayable = true;
            } else {
                if (bankId != null && bankId != 0) {
                    params.put("message", "Customer/Vendor Report for Date: " + ageingDate);
                } else {
                    params.put("message", "Ageing Report for Receivables, Date: " + ageingDate);
                }
            }
            if (exportType.equalsIgnoreCase(IdosConstants.XLSX_TYPE)) {
                if (bankId != null && bankId != 0) {
                    fileName = orgName + "_brstemplate_" + timeInMillis + ".xlsx";
                } else {
                    fileName = orgName + "_" + reportName + timeInMillis + ".xlsx";
                }
                type = IdosConstants.XLSX_TYPE;
                if (rptfileName != null) {
                    jsprRptName = rptfileName;
                } else {
                    jsprRptName = "ageingrpt";
                }
            } else {
                fileName = orgName + "_" + reportName + timeInMillis + ".pdf";
                type = IdosConstants.PDF_TYPE;
            }
            String path = application.path().toString() + "/logs/report/";
            File filepath = new File(path);
            if (!filepath.exists()) {
                filepath.mkdir();
            }
            path = path + fileName;
            java.lang.System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
            List<AgeingReport> datas = null;
            if (rptfileName != null) {
                if (bankId != null && bankId != 0) {

                    datas = AGEING_REPORT_SERVICE.getBrsTempList(ageingDate, bankId, user, entityManager, isPayable);
                } else {
                    datas = AGEING_REPORT_SERVICE.getAgeingReport2List(ageingDate, user, entityManager, isPayable);
                }
            } else {
                datas = AGEING_REPORT_SERVICE.getAgeingReportList(ageingDate, user, entityManager, isPayable);
            }
            if (datas != null) {
                System.out.println(datas.toString());
                ByteArrayOutputStream out = dynReportService.getJasperPrintFromFileUsingJtable(jsprRptName, datas,
                        params, type, request, application);
                file = new File(path);
                if (file.exists()) {
                    file.delete();
                }

                FileOutputStream fileOut = new FileOutputStream(path);
                out.writeTo(fileOut);
                fileOut.close();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return internalServerError(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End");
        if (file != null)
            return Results.ok(file).withHeader("Content-Disposition", "attachment; filename=" + fileName);
        else {
            results.put("message", "Record not found");
            return Results.ok(results).withHeader("ContentType", "application/json");
        }
    }
}
