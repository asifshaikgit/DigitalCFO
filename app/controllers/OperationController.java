package controllers;

import model.ConfigParams;
import model.Organization;
import model.Users;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import views.html.allOrganizationsData;
import views.html.changePasswd;
import views.html.errorPage;
import java.util.logging.Level;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import play.db.jpa.JPAApi;
import play.Application;
import play.mvc.Http;
import play.mvc.Http.Request;

/**
 * Created by Sunil Namdev on 24-08-2017.
 */
public class OperationController extends StaticController {
    public static JPAApi jpaApi;
    public static EntityManager entityManager;
    public static Application application;
    private Request request;

    @Inject
    public OperationController(Application application) {
        super(application);
        entityManager = EntityManagerProvider.getEntityManager();
        this.application = application;
    }

    @Transactional
    public Result getOranizationsList() {
        log.log(Level.FINE, ">>>> Start");
        ObjectNode results = Json.newObject();
        Users user = null;
        try {
            List<Organization> orgList = Organization.getAll(entityManager);

            results.put("draw", 1);
            results.put("recordsTotal", orgList.size());
            results.put("recordsFiltered", orgList.size());
            ArrayNode orgArrayNode = results.putArray("data");
            long diffInDays = 0L;
            for (Organization org : orgList) {
                ObjectNode row = Json.newObject();
                // row.put("id", org.getId());
                if (org.getCreatedAt() != null) {
                    row.put("orgDate", org.getCreatedAt().toString());
                    diffInDays = (long) ((new Date().getTime() - org.getCreatedAt().getTime()) / (1000 * 60 * 60 * 24));
                }
                row.put("orgName", org.getName() == null ? "-" : org.getName());
                row.put("orgPersonName", org.getPersonName() == null ? "-" : org.getPersonName());
                row.put("orgContactNo", org.getRegisteredPhoneNumber() == null ? "-" : org.getRegisteredPhoneNumber());
                row.put("orgContactEmail", org.getCorporateMail() == null ? "-" : org.getCorporateMail());
                row.put("orgRegisteredDays", diffInDays);
                List<Users> usersList = Users.getAllUsersByOrganization(entityManager, org);
                row.put("orgUsersCount", usersList.size());
                row.put("orgSource", org.getRegistrationSource() == null ? "-" : org.getRegistrationSource());
                orgArrayNode.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Err", ex);
        }
        log.log(Level.FINE, ">>>> End");
        return Results.ok(results).withHeader("ContentType", "application/json");
    }
}
