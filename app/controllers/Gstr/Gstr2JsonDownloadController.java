package controllers.Gstr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import controllers.StaticController;
import model.Users;
import com.typesafe.config.Config;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.Application;
import javax.inject.Inject;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;

public class Gstr2JsonDownloadController extends StaticController {
    private final Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    private Request request;
    // private Http.Session session = request.session();

    @Inject
    public Gstr2JsonDownloadController(Application application, JPAApi jpaApi) {
        super(application);
        this.application = application;
        this.jpaApi = jpaApi;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result downloadGSTR2JSONSFileForKarvy(Request request) {
        log.log(Level.FINE, ">>>> Start inside download GSTR4 data");
        // EntityManager entityManager = getEntityManager();
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

            user = getUserInfo(request);
            if (user == null) {
                return unauthorized();
            }

            String jsonStr = null;
            FileWriter fw = null;
            BufferedWriter writer = null;

            jsonStr = GSTR2_DAO.createGSTR2Json(json, user, entityManager);
            File fileDir = new File(path);
            if (!fileDir.exists()) {
                fileDir.mkdir();
            }
            String fileName = user.getOrganization().getName() + "_GSTR2.json";
            path = path + fileName;
            file = new File(path);
            try {
                log.log(Level.INFO, "inside write the data");
                fw = new FileWriter(file);
                writer = new BufferedWriter(fw);
                log.log(Level.INFO, "inside write the data");
                writer.append(jsonStr);
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
