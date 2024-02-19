package controllers;

import model.BranchTaxes;
import model.InterBranchMapping;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.errorPage;
import play.Application;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.List;
import javax.inject.Inject;
import java.util.logging.Level;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

/**
 * @auther Sunil created on 26.6.18
 */
public class InterbranchController extends StaticController {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    public static Application application;
    private Request request;
    // private Http.Session session = request.session();

    @Inject
    public InterbranchController(JPAApi jpaApi, Application application) {
        super(application);
        this.jpaApi = jpaApi;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result getInterBranchDetail(Long interbranchid, Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start >>" + interbranchid);
        ObjectNode result = Json.newObject();
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        Users user = getUserInfo(request);
        if (user == null) {
            return unauthorized();
        }
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }
        try {
            InterBranchMapping mapping = InterBranchMapping.findById(interbranchid);
            if (mapping != null) {
                result.put("id", mapping.getId());
                result.put("name", mapping.getName());
                if (mapping.getOpeningBalance() != null) {
                    result.put("openingBalance", mapping.getOpeningBalance());
                } else {
                    result.put("openingBalance", "");
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result saveOpeningBalance(Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager=getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users users = getUserInfo(request);
        if (users == null) {
            return unauthorized();
        }
        try {
            JsonNode json = request.body().asJson();
            Long id = json.findValue("id").asLong();
            Double openingBalance = json.findValue("openingBalance").asDouble();
            transaction.begin();
            InterBranchMapping mapping = InterBranchMapping.findById(id);
            if (mapping != null) {
                mapping.setOpeningBalance(openingBalance);
                genericDAO.saveOrUpdate(mapping, users, entityManager);
            }
            transaction.commit();
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);

    }
}
