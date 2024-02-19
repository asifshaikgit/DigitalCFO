package controllers;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import model.Branch;
import model.Organization;
import model.Users;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import javax.inject.Inject;
import play.Application;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.BaseService;
import service.DigitalSignatureService;
import service.DigitalSignatureServiceImpl;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class DigitalSignatureController extends StaticController {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    public static Application application;
    public Request request;
    // private Http.Session session = request.session();

    @Inject
    public DigitalSignatureController(JPAApi jpaApi, Application application) {
        super(application);
        this.jpaApi = jpaApi;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public static Result saveDigitalSignatureDetails(Organization org, Branch branch, Users user, JsonNode json,
            EntityManager entityManager) {
        Result results = null;
        if (user == null) {
            log.log(Level.SEVERE, "unauthorized access");
            return unauthorized();
        }
        // EntityManager em = getEntityManager();
        ObjectNode result = Json.newObject();
        try {
            DigitalSignatureService DIGITAL_SIGNATURE_SERVICE = new DigitalSignatureServiceImpl();
            boolean digiSignBoolean = DIGITAL_SIGNATURE_SERVICE.digitalSignatureService(org, branch, user, json,
                    entityManager);
        } catch (Exception ex) {
            result.put("status", "failed");
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return results;
    }
}
