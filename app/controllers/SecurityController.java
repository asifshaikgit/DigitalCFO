package controllers;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.time.Duration;
import com.idos.util.*;

import model.ConfigParams;
import model.IdosRegisteredVendor;
import model.Organization;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import play.mvc.Http.Cookie;
import play.mvc.Http.CookieBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;

import javax.transaction.Transactional;
import javax.inject.Inject;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Session;
import play.mvc.Result;
import play.mvc.Results;
import pojo.OrganiationPage;
import service.PasswordChangeService;
import views.html.errorPage;
import views.html.forgotPwdResetSuccess;
import views.html.logout;
import views.html.logoutsllr;
import views.html.passwordExpiry;
import views.html.termsAndConditions;
import views.html.passwordExpirySuccess;
import views.html.pwc.*;
import views.html.internal.*;
import play.mvc.Http.Request;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.logging.Level;

import static controllers.ApplicationController.updateUserOTPCode;
import static controllers.Karvy.ExternalUserLoginController.generateAccessCode;
import static controllers.StaticController.getUserInfo;

/**
 * Created by Sunil Namdev on 19-09-2016.
 */
public class SecurityController extends BaseController {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    private Request request;

    @Inject
    public SecurityController() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String userFailUpdateJQL = "update Users set inSession = 0, authToken=null, failedAttempt=?1 where id=?2";

    public static Users getUser(Request request) {
        try {
            String users = request.getQueryString("user");
            ObjectMapper mapper = new ObjectMapper();
            Users user = mapper.readValue(users, Users.class);
            return user;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Transactional
    public Result loginToOrg(Request request) throws ClassNotFoundException, IOException {
        // EntityManager entityManager = getEntityManager();
        Session session = request.session();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        entitytransaction.begin();
        ObjectNode result = Json.newObject();
        ArrayNode message = result.putArray("loginToOrgMessage");
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            String corporateEmail = json.findValue("userName").asText().toLowerCase();
            String adminPassword = json.findValue("loginpwd").asText();

            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("email", corporateEmail);
            // criterias.put("password", adminPassword);
            criterias.put("presentStatus", 1);
            user = genericDAO.getByCriteria(Users.class, criterias, entityManager);
            ObjectNode row = Json.newObject();
            if (user != null) {

                session.removing();
                session.adding("email", user.getEmail());
                row.put("message", "success");
            } else
                row.put("message", "failure");
            message.add(row);
            // String action="client from ip address "+ipAddress+" login into system";
            user.setLastLoginDate(Calendar.getInstance().getTime());
            user.setInSession(1);
            String authToken = user.createToken();
            log.log(Level.FINE, "authToken generated==" + authToken);
            user.setAuthToken(authToken);
            Query updateUser = entityManager.createQuery(userUpdateJQL);
            updateUser.setParameter(1, Calendar.getInstance().getTime());
            updateUser.setParameter(2, authToken);
            updateUser.setParameter(3, Calendar.getInstance().getTime());
            updateUser.setParameter(4, user.getId());
            updateUser.executeUpdate();
            entitytransaction.commit();
            result.put("loggedin", authToken);
            log.log(Level.FINE, ">>>> End " + result);
            // .withHeader("Cookie",IdosConstants.AUTH_TOKEN, authToken +"|"+
            // user.getId()+"|"+user.getUserRoleIDs())
            return Results.ok(result);
        } catch (Exception ex) {
            log.log(Level.SEVERE, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    @Transactional
    public Result idosAdmin() {
        try {
            return Results.ok(idosadmin.render(null, null, null, null, null, ConfigParams.getInstance()));
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, null, null,
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    @Transactional
    public Result demoExpired() {
        return Results.ok(demoExpired.render(null, null, null, null, null, ConfigParams.getInstance()));
    }

    @Transactional
    public Result login(Request request) {
        Http.Session session = request.session();
        ObjectNode results = Json.newObject();
        System.out.println("SK >>>>>>>>>>>> Inside login " + results);

        System.out.println("SK >>>>>>>>>>>> Entity " + entityManager);
        EntityTransaction entitytransaction = entityManager.getTransaction();
        try {
            session.removing();
            List<UsersRoles> userRoles = null;
            JsonNode json = request.body().asJson();
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, ">>>> Start ");
            JsonNode node = json.findValue("userName");
            String username = node == null ? null : node.asText().trim();
            node = json.findValue("loginpwd");
            String encpassword = node == null ? null : node.asText().trim();
            results.put("wrongses", "true");
            byte[] decodedData = RSAEncodeDecode.decryptByPrivateKey(encpassword, IdosConstants.PRIVATEK);
            String password = new String(decodedData);
            ObjectNode row = Json.newObject();
            ArrayNode loginCredentialsAN = results.putArray("logincredentials");
            // Form<LoginForm> loginForm =
            // formFactory.form(LoginForm.class).bindFromRequest();
            if (username == null || password == null) {
                row.put("message", "Failure");
                row.put("failurereason", "Provide user name and password both.");
                loginCredentialsAN.add(row);
                return Results.ok(results);
            }
            System.out.println("SK >>>>>>>>>>>> Inside login JPAA " + password);
            Users user = Users.findByEmailAddressAndPassword(entityManager, username, password);
            System.out.println("SK >>>>>>>>>>>> Inside login user " + user);
            String authToken = "";
            if (user == null) {
                String result = "Failure";
                row.put("message", result);
                String reason = "Login failed! Incorrect email or password.";
                row.put("failurereason", reason);
                loginCredentialsAN.add(row);
                String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                if (ipAddress == null) {
                    ipAddress = request.remoteAddress();
                }
                String action = "An attempt to login from ip address " + ipAddress + " is failed";
                entitytransaction.begin();
                user = Users.findActiveByEmail(username);
                if (user != null) {
                    Date lastPwdUpdate = user.getLastUpdatedPasswordDate();
                    if (null == lastPwdUpdate) {
                        lastPwdUpdate = user.getCreatedAt();
                        user.setLastUpdatedPasswordDate(lastPwdUpdate);
                    }
                    int failedAttempts = user.getFailedAttempt() == null ? 0 : user.getFailedAttempt();
                    failedAttempts++;
                    if (failedAttempts > 4) {
                        row.put("failurereason",
                                "Number of attempts exceeded and account is locked. Please contact support");
                    } else {
                        user.setFailedAttempt(failedAttempts);
                        Query updateUser = entityManager.createQuery(userFailUpdateJQL);
                        updateUser.setParameter(1, failedAttempts);
                        updateUser.setParameter(2, user.getId());
                        updateUser.executeUpdate();
                    }
                    auditDAO.saveAuditLogs(action, user, user.getId(), Users.class, ipAddress, row.toString(),
                            entityManager);
                } else {
                    auditDAO.saveAuditLogs(action, null, null, Users.class, ipAddress, row.toString(), entityManager);
                }
                entitytransaction.commit();
                return Results.ok(results);
            } else if (user.getFailedAttempt() > 4) {
                String result = "Failure";
                String reason = "Login failed! Account is locked. Please contact support";
                row.put("message", result);
                row.put("failurereason", reason);
                loginCredentialsAN.add(row);
                String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                if (ipAddress == null) {
                    ipAddress = request.remoteAddress();
                }
                String action = "An attempt to login from ip address " + ipAddress + " is failed";
                entitytransaction.begin();
                auditDAO.saveAuditLogs(action, user, user.getId(), Users.class, ipAddress, row.toString(),
                        entityManager);
                entitytransaction.commit();
                return Results.ok(results);
            } else if (user.getPresentStatus() == 0 || "0".equals(user.getPresentStatus())) {
                String result = "Failure";
                String reason = "Login failed! Please Activate Your Account.";
                row.put("message", result);
                row.put("failurereason", reason);
                loginCredentialsAN.add(row);
                String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                if (ipAddress == null) {
                    ipAddress = request.remoteAddress();
                }
                String action = "An attempt to login from ip address " + ipAddress + " is failed";
                entitytransaction.begin();
                auditDAO.saveAuditLogs(action, null, null, Users.class, ipAddress, row.toString(), entityManager);
                entitytransaction.commit();
                return Results.ok(results);
            } else {
                Date lastPwdUpdate = user.getLastUpdatedPasswordDate();
                if (null == lastPwdUpdate) {
                    lastPwdUpdate = user.getCreatedAt();
                    user.setLastUpdatedPasswordDate(lastPwdUpdate);
                }
                String role = "";
                userRoles = UsersRoles.getUserRoleList(entityManager, user.getOrganization().getId(), user.getId(),
                        user.getBranch().getId());
                for (UsersRoles roles : userRoles) {
                    role += roles.getRole().getName() + ",";
                }
                if (role.length() <= 0) {
                    String result = "Failure";
                    String reason = "Login failed! No access right assinged to this user, please contact your admin to provide access rights.";
                    row.put("message", result);
                    row.put("failurereason", reason);
                    loginCredentialsAN.add(row);
                    return Results.ok(results).as(IdosConstants.CONTENT_TYPE_JSON);
                }
                role = role.substring(0, role.length() - 1);
                results.put("role", role);
                // session.adding("email", username);
                // session.adding("role", userRoles.get(0).getRole().getName());
                String result = "Success";
                row.put("message", result);
                row.put("userrole", role);
                // loginCredentialsAN.add(row);
                int daysExp = 0;
                // long diff = PasswordChangeService.passwordChangeDifference(lastPwdUpdate);
                // if (diff > 90) {
                // row.put("days", 1);
                // daysExp = 1;
                // } else {
                // row.put("days", 0);
                // }
                Date currentDate = Calendar.getInstance().getTime();
                if (!role.equals("EXTERNAL USER")) {
                    if (mysqldf.format(currentDate)
                            .compareTo(IdosConstants.mysqldf.format(user.getOrganization().getTrialEndDate())) >= 0) {
                        row.put("trialOver", 1);
                    } else {
                        row.put("trialOver", 0);
                    }
                }
                // response().setCookie("user", username);
                String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                if (ipAddress == null) {
                    ipAddress = request.remoteAddress();
                }
                String action = "client from ip address " + ipAddress + " login into system";
                user.setLastLoginDate(Calendar.getInstance().getTime());
                user.setInSession(1);
                authToken = user.createToken();
                log.log(Level.FINE, "authToken==" + authToken);
                user.setAuthToken(authToken);
                String authTokenuserIdRoleId = authToken + "|" + user.getId() + "|" + user.getUserRoleIDs();
                if (ConfigParams.getInstance().getInstanceDeploymentMode() == IdosConstants.INSTANCE_API_MODE) {
                    row.put("authTokenuserIdRoleId", authTokenuserIdRoleId);
                }
                loginCredentialsAN.add(row);
                entitytransaction.begin();
                auditDAO.saveAuditLogs(action, user, user.getId(), Users.class, ipAddress, row.toString(),
                        entityManager);
                // genericDAO.saveOrUpdate(user, user, entityManager);
                Query updateUser = entityManager.createQuery(userUpdateJQL);
                updateUser.setParameter(1, Calendar.getInstance().getTime());
                updateUser.setParameter(2, authToken);
                updateUser.setParameter(3, lastPwdUpdate);
                updateUser.setParameter(4, user.getId());
                updateUser.executeUpdate();
                entitytransaction.commit();
                // results.put(IdosConstants.AUTH_TOKEN, authToken);
                // response().setCookie(IdosConstants.AUTH_TOKEN0, user.getEmail());
                // response().setCookie(IdosConstants.AUTH_TOKEN1, user.getUserRoleIDs());

                // response().setCookie(IdosConstants.AUTH_TOKEN0, user.getEmail());
                // response().setCookie(IdosConstants.AUTH_TOKEN1, user.getUserRoleIDs());
                System.out.println(
                        "SK >>>>>>>>>>>> get organization" + user.getOrganization().getAccoutType(entityManager));
                if (user.getOrganization().getAccoutType(entityManager) == 1) {
                    results.put("url", "/myadmin");
                    System.out.println("SK >>>>>>>>>>>> Transaction ended ");
                } else if (user.getOrganization().getAccoutType(entityManager) == 2
                        && user.getOrganization().isAccountExpired()) {
                    results.put("url", "/demoexp");
                    System.out.println("SK >>>>>>>>>>>> Transaction ended ");
                } else if ("EXTERNAL USER".equalsIgnoreCase(role)) {
                    results.put("url", "/externalUserConfig");
                    System.out.println("SK >>>>>>>>>>>> Transaction ended ");
                } else if (daysExp == 1) {
                    results.put("url", "/passwordExpiry");
                    System.out.println("SK >>>>>>>>>>>> Transaction ended ");
                } else if (user.getAgreedTermsAndContions() != null && user.getAgreedTermsAndContions() == 0) {
                    if (ConfigParams.getInstance().getCompanyOwner().equals("PWC")) {
                        // PWC Terms & Conditions page
                        System.out.println("SK >>>>>>>>>>>> Transaction ended ");
                        results.put("url", "/termsAndConditions");
                    }
                    if (ConfigParams.getInstance().getCompanyOwner().equals("KARVY")) {
                        // KARVY Terms & Conditions page
                        results.put("url", "/termsAndConditions");
                        System.out.println("SK >>>>>>>>>>>> Transaction ended ");
                    } else {
                        // Default IDOS Terms Page
                        results.put("url", "/termsAndConditions");
                        System.out.println("SK >>>>>>>>>>>> Transaction ended ");
                    }
                } else {
                    results.put("url", "/config");
                    results.put("loggedin", authToken);
                    System.out.println("SK >>>>>>>>>>>> Transaction ended ");
                }
                System.out.println("SK >>>>>>>>>>>> Transaction ended ");
            }
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "End " + results);
            Duration maxAge = Duration.ofSeconds(3600);
            return Results.ok(results).as(IdosConstants.CONTENT_TYPE_JSON).withHeader("ContentType", "application/json")
                    .withHeader("Access-Control-Allow-Origin", "*")
                    .withCookies(Cookie.builder("user", username).withHttpOnly(false).withMaxAge(maxAge).build())
                    // .withCookies(Cookie.builder(IdosConstants.AUTH_TOKEN0,
                    // user.getEmail()).withHttpOnly(false)
                    // .withMaxAge(maxAge).build())
                    // .withCookies(Cookie.builder(IdosConstants.AUTH_TOKEN1,
                    // user.getUserRoleIDs()).withHttpOnly(false)
                    // .withMaxAge(maxAge).build())
                    .withCookies(Cookie
                            .builder(IdosConstants.AUTH_TOKEN,
                                    authToken + "|" + user.getId() + "|" + user.getUserRoleIDs())
                            .withHttpOnly(false).withMaxAge(maxAge).build())
                    .addingToSession(request, "email", username)
                    .addingToSession(request, "role", userRoles.get(0).getRole().getName());
            // .withHeader(CONTENT_TYPE, "no-cache, max-age=0, must-revalidate, no-store,
            // private").withHeader(PRAGMA,
            // "no-cache").withHeader("Cookie",IdosConstants.AUTH_TOKEN, authToken + "|" +
            // user.getId() + "|" + user.getUserRoleIDs())
        } catch (javax.crypto.BadPaddingException ex) {
            results.put("wrongses", "false");
            System.out.println("SK >>>>>>>>>>>> Inside login crypto ex " + ex);
            log.log(Level.SEVERE, "parsing Error " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("SK >>>>>>>>>>>> Inside login ex " + ex);
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.SEVERE, "Login Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "Login Email", "Login Organisation",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        } finally {
            // EntityManagerProvider.close();
        }
        return null;
    }

    @Transactional
    public Result ssoLogin(Request request) {
        ObjectNode results = Json.newObject();
        Session session = request.session();
        // EntityManager entityManager=getEntityManager();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        try {
            session.removing();
            JsonNode json = request.body().asJson();
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, ">>>> Start ");
            JsonNode node = json.findValue("userName");
            String username = node == null ? null : node.asText().trim();
            node = json.findValue("loginpwd");
            String encpassword = node == null ? null : node.asText().trim();
            results.put("wrongses", "true");
            // byte[] decodedData = RSAEncodeDecode.decryptByPrivateKey(encpassword,
            // IdosConstants.PRIVATEK);
            // String password = new String(decodedData);
            ObjectNode row = Json.newObject();
            ArrayNode loginCredentialsAN = results.putArray("logincredentials");
            // Form<LoginForm> loginForm =
            // formFactory.form(LoginForm.class).bindFromRequest();
            // if (username == null || password == null) {
            if (username == null) {
                row.put("message", "Failure");
                row.put("failurereason", "Provide user name and password both.");
                loginCredentialsAN.add(row);
                return Results.ok(results);
            }
            // Users user = Users.findByEmailAddressAndPassword(jpaApi, username, password);
            Users user = Users.findActiveByEmail(username);
            if (user == null) {
                String result = "Failure";
                row.put("message", result);
                String reason = "Login failed! Incorrect email or password.";
                row.put("failurereason", reason);
                loginCredentialsAN.add(row);
                String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                if (ipAddress == null) {
                    ipAddress = request.remoteAddress();
                }
                String action = "An attempt to login from ip address " + ipAddress + " is failed";
                entitytransaction.begin();
                user = Users.findActiveByEmail(username);
                if (user != null) {
                    Date lastPwdUpdate = user.getLastUpdatedPasswordDate();
                    if (null == lastPwdUpdate) {
                        lastPwdUpdate = user.getCreatedAt();
                        user.setLastUpdatedPasswordDate(lastPwdUpdate);
                    }
                    int failedAttempts = user.getFailedAttempt() == null ? 0 : user.getFailedAttempt();
                    failedAttempts++;
                    if (failedAttempts > 4) {
                        row.put("failurereason",
                                "Number of attempts exceeded and account is locked. Please contact support");
                    } else {
                        user.setFailedAttempt(failedAttempts);
                        Query updateUser = entityManager.createQuery(userFailUpdateJQL);
                        updateUser.setParameter(1, failedAttempts);
                        updateUser.setParameter(2, user.getId());
                        updateUser.executeUpdate();
                    }
                    auditDAO.saveAuditLogs(action, user, user.getId(), Users.class, ipAddress, row.toString(),
                            entityManager);
                } else {
                    auditDAO.saveAuditLogs(action, null, null, Users.class, ipAddress, row.toString(), entityManager);
                }
                entitytransaction.commit();
                /*
                 * }else if(user.getFailedAttempt() > 4){
                 * String result="Failure";
                 * String reason="Login failed! Account is locked. Please contact support";
                 * row.put("message", result);
                 * row.put("failurereason", reason);
                 * loginCredentialsAN.add(row);
                 * String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                 * if (ipAddress == null) {
                 * ipAddress = request.remoteAddress();
                 * }
                 * String action="An attempt to login from ip address "+ipAddress+" is failed";
                 * entitytransaction.begin();
                 * auditDAO.saveAuditLogs(action,user,user.getId(),Users.class,ipAddress,row.
                 * toString(), entityManager);
                 * entitytransaction.commit();
                 * }else if(user.getPresentStatus()==0 || "0".equals(user.getPresentStatus())){
                 * String result="Failure";
                 * String reason="Login failed! Please Activate Your Account.";
                 * row.put("message", result);
                 * row.put("failurereason", reason);
                 * loginCredentialsAN.add(row);
                 * String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                 * if (ipAddress == null) {
                 * ipAddress = request.remoteAddress();
                 * }
                 * String action="An attempt to login from ip address "+ipAddress+" is failed";
                 * entitytransaction.begin();
                 * auditDAO.saveAuditLogs(action,null,null,Users.class,ipAddress,row.toString(),
                 * entityManager);
                 * entitytransaction.commit();
                 */
            } else {
                Date lastPwdUpdate = user.getLastUpdatedPasswordDate();
                if (null == lastPwdUpdate) {
                    lastPwdUpdate = user.getCreatedAt();
                    user.setLastUpdatedPasswordDate(lastPwdUpdate);
                }
                String role = "";
                List<UsersRoles> userRoles = user.getUserRoles();
                for (UsersRoles roles : userRoles) {
                    role += roles.getRole().getName() + ",";
                }
                if (role.length() <= 0) {
                    String result = "Failure";
                    String reason = "Login failed! No access right assinged to this user, please contact your admin to provide access rights.";
                    row.put("message", result);
                    row.put("failurereason", reason);
                    loginCredentialsAN.add(row);
                    return Results.ok(results).as(IdosConstants.CONTENT_TYPE_JSON);
                }
                role = role.substring(0, role.length() - 1);
                results.put("role", role);
                session = session.adding("email", username).adding("role", userRoles.get(0).getRole().getName());
                String result = "Success";
                row.put("message", result);
                row.put("userrole", role);
                // loginCredentialsAN.add(row);
                int daysExp = 0;
                long diff = PasswordChangeService.passwordChangeDifference(lastPwdUpdate);
                if (diff > 90) {
                    row.put("days", 1);
                    daysExp = 1;
                } else {
                    row.put("days", 0);
                }
                Date currentDate = Calendar.getInstance().getTime();
                if (!role.equals("EXTERNAL USER")) {
                    if (mysqldf.format(currentDate)
                            .compareTo(IdosConstants.mysqldf.format(user.getOrganization().getTrialEndDate())) >= 0) {
                        row.put("trialOver", 1);
                    } else {
                        row.put("trialOver", 0);
                    }
                }
                // response().setCookie("user", username);
                String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                if (ipAddress == null) {
                    ipAddress = request.remoteAddress();
                }
                String action = "client from ip address " + ipAddress + " login into system";
                user.setLastLoginDate(Calendar.getInstance().getTime());
                user.setInSession(1);
                String authToken = user.createToken();
                log.log(Level.FINE, "authToken==" + authToken);
                user.setAuthToken(authToken);
                String authTokenuserIdRoleId = authToken + "|" + user.getId() + "|" + user.getUserRoleIDs();
                if (ConfigParams.getInstance().getInstanceDeploymentMode() == IdosConstants.INSTANCE_API_MODE) {
                    row.put("authTokenuserIdRoleId", authTokenuserIdRoleId);
                }
                loginCredentialsAN.add(row);
                entitytransaction.begin();
                auditDAO.saveAuditLogs(action, user, user.getId(), Users.class, ipAddress, row.toString(),
                        entityManager);
                // genericDAO.saveOrUpdate(user, user, entityManager);
                Query updateUser = entityManager.createQuery(userUpdateJQL);
                updateUser.setParameter(1, Calendar.getInstance().getTime());
                updateUser.setParameter(2, authToken);
                updateUser.setParameter(3, lastPwdUpdate);
                updateUser.setParameter(4, user.getId());
                updateUser.executeUpdate();
                entitytransaction.commit();

                if (ConfigParams.getInstance().getIdosConfigParamValue("IS_OTP.ENABLED").equals("1")) {
                    results.put("url", "/userVerification");
                } else {
                    results.put("url", "/config");
                }
                results.put("loggedin", authToken);

                /*
                 * if (user.getOrganization().getAccoutType() == 1){
                 * results.put("url", "/myadmin");
                 * response.setCookie(IdosConstants.AUTH_TOKEN, authToken +"|"+
                 * user.getId()+"|"+user.getUserRoleIDs());
                 * }else if(user.getOrganization().getAccoutType() == 2 &&
                 * user.getOrganization().isAccountExpired()) {
                 * results.put("url", "/demoexp");
                 * }else if("EXTERNAL USER".equalsIgnoreCase(role)){
                 * results.put("url", "/externalUserConfig");
                 * response.setCookie(IdosConstants.AUTH_TOKEN, authToken +"|"+
                 * user.getId()+"|"+user.getUserRoleIDs());
                 * }else if(daysExp == 1){
                 * results.put("url", "/passwordExpiry");
                 * response.setCookie(IdosConstants.AUTH_TOKEN, authToken +"|"+
                 * user.getId()+"|"+user.getUserRoleIDs());
                 * }else if(user.getAgreedTermsAndContions() != null &&
                 * user.getAgreedTermsAndContions() == 0){
                 * if(ConfigParams.getInstance().getCompanyOwner().equals("PWC")){
                 * // PWC Terms & Conditions page
                 * results.put("url", "/termsAndConditions");
                 * }if(ConfigParams.getInstance().getCompanyOwner().equals("KARVY")){
                 * // KARVY Terms & Conditions page
                 * results.put("url", "/termsAndConditions");
                 * }else{
                 * // Default IDOS Terms Page
                 * results.put("url", "/termsAndConditions");
                 * }
                 * response.setCookie(IdosConstants.AUTH_TOKEN, authToken +"|"+
                 * user.getId()+"|"+user.getUserRoleIDs());
                 * }else{
                 * results.put("url", "/config");
                 * results.put("loggedin", authToken);
                 * response.setCookie(IdosConstants.AUTH_TOKEN, authToken + "|" + user.getId() +
                 * "|" + user.getUserRoleIDs());
                 * }
                 */
            }
            if (log.isLoggable(Level.FINE))
                log.log(Level.FINE, "End " + results);
            return Results.ok(results).as(IdosConstants.CONTENT_TYPE_JSON).withHeader("ContentType", "application/json")
                    .withHeader("Access-Control-Allow-Origin", "*");
            // .withHeader(CONTENT_TYPE, "no-cache, max-age=0, must-revalidate, no-store,
            // private").withHeader(PRAGMA,
            // "no-cache").withHeader("Cookie",IdosConstants.AUTH_TOKEN, authToken + "|" +
            // user.getId() + "|" + user.getUserRoleIDs())
        } catch (Exception ex) {
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.SEVERE, "Login Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "Login Email", "Login Organisation",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    @Transactional
    public Result loginSeller(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager=getEntityManager();
        ObjectNode results = Json.newObject();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ArrayNode loginCredentialsAN = results.putArray("logincredentials");
        Session session = request.session();
        session.removing();
        try {
            JsonNode json = request.body().asJson();
            ObjectNode row = Json.newObject();
            String username = json.findValue("userName") == null ? null : json.findValue("userName").asText();
            String encpassword = json.findValue("loginpwd") == null ? null : json.findValue("loginpwd").asText();
            entitytransaction.begin();
            if (username == null || encpassword == null) {
                row.put("message", "Failure");
                row.put("failurereason", "Provide user name and password both.");
                loginCredentialsAN.add(row);
                log.log(Level.FINE, ">>>>1 End " + results);
                return Results.ok(results);
            }

            results.put("wrongses", "true");
            byte[] decodedData = RSAEncodeDecode.decryptByPrivateKey(encpassword, IdosConstants.PRIVATEK);
            String password = new String(decodedData);

            IdosRegisteredVendor regvendors = IdosRegisteredVendor.findByEmailAddressAndPassword(entityManager,
                    username, password);
            if (regvendors == null) {
                String result = "Failure";
                row.put("message", result);
                String reason = "Login failed! incorrect email or password.";
                row.put("failurereason", reason);
                loginCredentialsAN.add(row);
                String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                if (ipAddress == null) {
                    ipAddress = request.remoteAddress();
                }
                String action = "An attempt to login from ip address " + ipAddress + " is failed";
                auditDAO.saveAuditLogs(action, null, null, IdosRegisteredVendor.class, ipAddress, row.toString(),
                        entityManager);
                log.log(Level.FINE, ">>>>2 End " + results);
                return Results.ok(results);
            } else if (regvendors.getPresentStatus() == 0) {
                String reason = "Login failed! Please Activate Your Account.";
                row.put("message", "Failure");
                row.put("failurereason", reason);
                loginCredentialsAN.add(row);
                String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                if (ipAddress == null) {
                    ipAddress = request.remoteAddress();
                }
                String action = "An attempt to login from ip address " + ipAddress + " is failed";
                auditDAO.saveAuditLogs(action, null, null, IdosRegisteredVendor.class, ipAddress, row.toString(),
                        entityManager);
                log.log(Level.FINE, ">>>> 3 End " + results);
                return Results.ok(results);
            } else {
                Date lastPwdUpdate = regvendors.getLastUpdatedPassword();
                if (null == lastPwdUpdate) {
                    lastPwdUpdate = regvendors.getCreatedAt();
                    regvendors.setLastUpdatedPassword(lastPwdUpdate);
                }
                regvendors.setLastLoginDate(Calendar.getInstance().getTime());
                regvendors.setRegVendInSession(1);
                session.adding("selleremail", username);
                row.put("message", "success");
                loginCredentialsAN.add(row);
                long diff = PasswordChangeService.passwordChangeDifference(lastPwdUpdate);
                if (diff > 90) {
                    row.put("days", 1);
                } else {
                    row.put("days", 0);
                }
                String authToken = regvendors.createToken();
                regvendors.setAuthToken(authToken);
                genericDAO.saveOrUpdate(regvendors, null, entityManager);
                results.put(IdosConstants.AUTH_TOKEN, authToken);

            }
            entitytransaction.commit();
            log.log(Level.FINE, ">>>> End " + results);
            return Results.ok(results).withHeader("ContentType", "application/json")
                    .withHeader("Access-Control-Allow-Origin", "*");
            // .withHeader("Cookie",IdosConstants.AUTH_TOKEN, authToken)
        } catch (javax.crypto.BadPaddingException ex) {
            results.put("wrongses", "false");
            // log.log(Level.SEVERE, ex.getMessage());
        } catch (Exception ex) {
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "SellerLogin Email", "SellerLogin Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return null;
    }

    @Transactional
    public Result custVendLogin(Request request) {
        log.log(Level.FINE, ">>>> Start");
        Session session = request.session();
        // EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode results = Json.newObject();
        try {
            transaction.begin();
            JsonNode json = request.body().asJson();
            log.log(Level.FINE, ">>>>>> Start " + json);
            ArrayNode an = results.putArray("vendorCustomerLoginData");
            Long accountOrganization = json.findValue("accountOrganization").asLong();
            String accountEmail = json.findValue("accountEmail").asText();
            String entityType = json.findValue("entityType").asText();
            String accountPassword = json.findValue("accountPassword").asText();
            Integer type = null;
            if (entityType.equals("Vendor")) {
                type = 1;
            }
            if (entityType.equals("Customer")) {
                type = 2;
            }
            Organization org = Organization.findById(accountOrganization);
            Map<Object, Object> criteria = new HashMap<Object, Object>();
            criteria.put("email", accountEmail);
            criteria.put("organization.id", org.getId());
            criteria.put("type", type);
            criteria.put("accessPassword", PasswordUtil.encrypt(accountPassword));

            List<Vendor> custvendors = genericDAO.findByCriteria(Vendor.class, criteria, entityManager);
            if (custvendors != null && custvendors.size() > 0) {
                if (custvendors.get(0).getGrantAccess() != 1) {
                    ObjectNode row = Json.newObject();
                    row.put("message", "nogrant");
                    an.add(row);
                } else if (custvendors.get(0).getPresentStatus() != 1) {
                    ObjectNode row = Json.newObject();
                    row.put("message", "inactive");
                    an.add(row);
                } else if (custvendors.get(0).getAccessPassword() != null
                        && custvendors.get(0).getAccessPassword().equals(PasswordUtil.encrypt(accountPassword))) {
                    ObjectNode row = Json.newObject();
                    row.put("message", "success");
                    row.put("accountEmail", accountEmail);
                    row.put("accountName", custvendors.get(0).getName());
                    row.put("accountOrganization", accountOrganization);
                    row.put("entityType", entityType);
                    row.put("id", custvendors.get(0).getId());
                    an.add(row);
                    Vendor vend = custvendors.get(0);
                    Date lastPwdUpdate = vend.getLastUpdatedPassword();
                    if (null == lastPwdUpdate) {
                        lastPwdUpdate = vend.getCreatedAt();
                        vend.setLastUpdatedPassword(lastPwdUpdate);
                    }
                    vend.setLastLoginDate(Calendar.getInstance().getTime());
                    vend.setRegVendInSession(1);
                    session.adding("vendoremail", accountEmail);
                    session.adding("entityType", entityType);
                    long diff = PasswordChangeService.passwordChangeDifference(lastPwdUpdate);
                    if (diff > 90) {
                        row.put("days", 1);
                    } else {
                        row.put("days", 0);
                    }
                    String authToken = custvendors.get(0).createToken();
                    custvendors.get(0).setAuthToken(authToken);
                    genericDAO.saveOrUpdate(custvendors.get(0), null, entityManager);
                    results.put(IdosConstants.AUTH_TOKEN, authToken);
                } else {
                    ObjectNode row = Json.newObject();
                    row.put("message", "failure");
                    an.add(row);
                }
            } else {
                ObjectNode row = Json.newObject();
                row.put("message", "failure");
                an.add(row);
            }

            transaction.commit();
            return Results.ok(results).withHeader("ContentType", "application/json");
            // .withHeader("Cookie","vendorCustomer",
            // accountEmail).withHeader("Cookie",IdosConstants.AUTH_TOKEN, authToken)
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "CustVendLogin Email", "CustVendLogin Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    @Transactional
    public Result passwordExpiry(Request request) {
        Users user = StaticController.getUserInfo(request);
        Long tdiff = null;
        if (null != user) {
            Date lastUpdatePwd = user.getLastUpdatedPasswordDate();
            if (null == lastUpdatePwd) {
                lastUpdatePwd = user.getCreatedAt();
            }
            long diff = PasswordChangeService.passwordChangeDifference(lastUpdatePwd);
            return Results.ok(passwordExpiry.render(user.getFullName(), user.getEmail(), null, user.getOrganization(),
                    String.valueOf(user.getAllowedProcurementRequest()), diff, ConfigParams.getInstance()));
        } else {
            return Results.ok(passwordExpiry.render(null, null, null, null, null, tdiff, ConfigParams.getInstance()));
        }
    }

    public Result signOut() {
        return Results.ok(logout.render(null, null, null, null, null, ConfigParams.getInstance()));
    }

    public Result signOutSeller() {
        return Results.ok(logoutsllr.render(null, null, null, null, null, ConfigParams.getInstance()));
    }

    public Result forgotPwdResetSuccess() {
        return Results.ok(forgotPwdResetSuccess.render(null, null, null, null, null, ConfigParams.getInstance()));
    }

    public Result pwdExpiredSuccess() {
        return Results.ok(passwordExpirySuccess.render(null, null, null, null, null, ConfigParams.getInstance()));
    }

    @Transactional
    public Result verifyCaptcha(Request request) {
        log.log(Level.FINE, ">>>> Start");
        Session session = request.session();
        ObjectNode results = Json.newObject();
        session.removing();
        try {
            JsonNode json = request.body().asJson();
            String recaptchaResponse = json.findValue("recaptchaResponse").asText();
            String verifiedValue = VerifyRecaptcha.verify(recaptchaResponse);
            results.put("verifiedValue", verifiedValue);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "Login Email", "Login Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>>>>>>>End " + results);
        return Results.ok(results).withHeader("ContentType", "application/json")
                .withHeader("Access-Control-Allow-Origin", "*");
    }

    @Transactional
    public Result showTermsAndConditions(Request request) {
        Users user = StaticController.getUserInfo(request);
        if (user != null) {
            if (ConfigParams.getInstance().getCompanyOwner().equals("PWC")) {
                return Results.ok(pwcUserTermsAndConditions.render(user.getFullName(), user.getEmail(), null,
                        user.getOrganization(), null, null, ConfigParams.getInstance()));
            } else {
                return Results.ok(termsAndConditions.render(user.getFullName(), user.getEmail(), null,
                        user.getOrganization(), null, null, ConfigParams.getInstance()));
            }
        } else {
            return Results.ok(logout.render(null, null, null, null, null, ConfigParams.getInstance()));
        }
    }

    @Transactional
    public Result verification(Request request) {
        ObjectNode results = Json.newObject();
        JsonNode json = request.body().asJson();
        Http.Session session = request.session();
        String username = session.getOptional("email").orElse("");
        JsonNode password = json.findValue("securitycode");
        Users user = getUserInfo(request);

        if (user == null) {
            return unauthorized();
        }

        if (Users.validateSecurityCode(entityManager, username, password.asText())) {
            results.put("url", "/config");
            results.put("loggedin", user.getAuthToken());
        } else {
            results.removeAll();
            results.put("message", "false");
        }
        return Results.ok(results);
        // .withHeader("Cookie",IdosConstants.AUTH_TOKEN, user.getAuthToken() + "|" +
        // user.getId() + "|" +
        // user.getUserRoleIDs()).as(IdosConstants.CONTENT_TYPE_JSON)
    }

    @Transactional
    public Result resendMail(Request request) {
        ObjectNode results = Json.newObject();
        Http.Session session = request.session();
        String username = session.getOptional("email").orElse("");
        Users user = Users.findActiveByEmail(username);
        String OTPCode = generateAccessCode();
        String body = views.html.loginVerificationLink.render(OTPCode, ConfigParams.getInstance()).body();
        updateUserOTPCode(username, OTPCode);
        new Mail.Builder()
                .withSession(emailsession)
                .withFrom(ConfigFactory.load().getString("smtp.user"))
                .withTo(user.getEmail())
                .withCC(null)
                .withSubject("Login security code")
                .withMessageBody(body)
                .withSentDate(new Date())
                .sendMail();

        return ok(results).as(IdosConstants.CONTENT_TYPE_JSON);
    }
}
