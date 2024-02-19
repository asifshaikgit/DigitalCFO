package controllers;

import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import play.Application;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

/**
 * @auther Sunil Namdev created on 23.07.2018
 */
public class UIThemeController extends StaticController {
    public static Application application;
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    // private Request request;
    // private Http.Session session = request.session();

    @Inject
    public UIThemeController(JPAApi jpaApi, Application application) {
        super(application);
        this.jpaApi = jpaApi;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result getUserTheme(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager=getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            user = getUserInfo(request);
            if (user == null) {
                log.log(Level.SEVERE, "unauthorized access");
                return unauthorized();
            }

            if (user.getTheme() != null) {
                result.put("theme", user.getTheme());
            } else {
                result.put("theme", "0");
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result saveUserTheme(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager em = getEntityManager();
        ObjectNode result = Json.newObject();
        Users user = null;
        EntityTransaction et = entityManager.getTransaction();
        try {
            user = getUserInfo(request);
            if (user == null) {
                log.log(Level.SEVERE, "unauthorized access");
                return unauthorized();
            }
            JsonNode json = request.body().asJson();
            Integer theme = json.findValue("theme") == null ? null : json.findValue("theme").asInt();
            et.begin();
            user.setTheme(theme);
            et.commit();
        } catch (Exception ex) {
            if (et.isActive())
                et.rollback();
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result).withHeader("ContentType", "application/json");
    }
}
