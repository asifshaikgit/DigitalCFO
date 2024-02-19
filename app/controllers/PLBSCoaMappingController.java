package controllers;

import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.inject.Inject;
import play.Application;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

/**
 * @auther Sunil Namdev created on 05.06.2018
 */
public class PLBSCoaMappingController extends StaticController {
    public static Application application;
    public static JPAApi jpaApi;
    public static EntityManager entityManager;
    // private Request request;
    // private Http.Session session = request.session();

    @Inject
    public PLBSCoaMappingController(Application application) {
        super(application);
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result getCoaForOrganizationWithAllHeads(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager=getEntityManager();
        // EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            user = getUserInfo(request);
            if (null != user) {
                result = PLBS_COA_MAPPING_SERVICE.getCoaForOrganizationWithAllHeads(result, json, user, entityManager);
            } else {
                return unauthorized();
            }
        } catch (Exception ex) {
            // if (entitytransaction.isActive()) {
            // entitytransaction.rollback();
            // }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }
}
