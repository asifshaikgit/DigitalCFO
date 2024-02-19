package controllers.Gstr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import com.idos.util.IdosConstants;

import controllers.StaticController;
import model.Users;
import com.typesafe.config.Config;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;
import play.Application;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import play.mvc.Http;
import play.mvc.Http.Request;

public class Gstr4JsonDownloadController extends StaticController {
    private final Application application;
    public static JPAApi jpaApi;
    public static EntityManager entityManager;
    private Request request;
    // private Http.Session session = request.session();

    @Inject
    public Gstr4JsonDownloadController(Application application, JPAApi jpaApi) {
        super(application);
        this.application = application;
    }

    @Transactional
    public Result downloadGSTR4JSONSFileForKarvy(Http.Request request) {
        log.log(Level.FINE, ">>>> Start inside download GSTR4 data");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        ObjectNode result = Json.newObject();
        File file = null;
        String path = application.path().toString() + "/logs/KarvyJSONData/";
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = null;
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {

            JsonNode json = request.body().asJson();
            String useremail = json.findValue("useremail").asText();
            String type = json.findValue("type").asText();
            String dateMonthAndYear = json.findValue("txtDate").asText();
            String gstIn = json.findValue("gstIn").asText();
            Double taxRate = json.findValue("taxRate").asDouble();
            Double turnOver = json.findValue("turnOver").asDouble();
            Double cgst = json.findValue("cgst").asDouble();
            Double sgst = json.findValue("cgst").asDouble();
            log.log(Level.INFO, "gstin=" + gstIn);
            session.adding("email", useremail);
            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }
            String year = dateMonthAndYear.substring(dateMonthAndYear.length() - 4, dateMonthAndYear.length());
            String month = dateMonthAndYear.substring(0, dateMonthAndYear.length() - 5);
            // January 1,2018
            String dateinjava = month + " 1," + year;
            log.log(Level.INFO, "date in java=" + dateinjava);

            Date fromTransDate = IdosConstants.MYSQLDF
                    .parse(IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(dateinjava)));

            Calendar cal = Calendar.getInstance();
            Date toTransDate = cal.getTime();
            cal.setTime(fromTransDate);
            Integer monthInInt = cal.get(Calendar.MONTH) + 1;
            String gstr1period = monthInInt.toString() + year;
            JSONObject outerObject = new JSONObject();
            JSONArray outerArray1 = new JSONArray();
            // JSONArray outerArray2= new JSONArray();
            JSONObject[] innerObjectForRegular = new JSONObject[1];
            innerObjectForRegular[0] = new JSONObject();
            innerObjectForRegular[0].put("rt", taxRate);
            innerObjectForRegular[0].put("trnovr", IdosConstants.decimalFormat.format(turnOver));
            innerObjectForRegular[0].put("camt", IdosConstants.decimalFormat.format(cgst));
            innerObjectForRegular[0].put("samt", IdosConstants.decimalFormat.format(sgst));
            outerArray1.put(innerObjectForRegular[0]);
            outerObject.put("txos", outerArray1);
            outerObject.put("fp", gstr1period);
            outerObject.put("GSTIN", gstIn);
            FileWriter fw = null;
            BufferedWriter writer = null;

            File fileDir = new File(path);
            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
            String fileName = user.getOrganization().getName() + "_" + gstr1period + "_GSTR4.json";
            path = path + fileName;
            file = new File(path);
            try {
                log.log(Level.INFO, "inside write the data");
                fw = new FileWriter(file);
                writer = new BufferedWriter(fw);
                log.log(Level.INFO, "inside write the data");
                writer.append(outerObject.toString());
                log.log(Level.INFO, "inside write the data");
                writer.newLine();
                log.log(Level.INFO, "inside write the data");
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error", e);

            } finally {
                try {
                    if (writer != null)
                        writer.close();

                    if (fw != null)
                        fw.close();
                } catch (IOException ex) {
                    log.log(Level.SEVERE, "Error", ex);
                }
            }
            return Results.ok(result).withHeader("ContentType", "application/json").withHeader("Content-Disposition",
                    "attachment; filename=" + fileName);

        } catch (Exception ex) {
            log.log(Level.INFO, "inside error");
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

}
