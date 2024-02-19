/*Added by Puja Lohia*/
package controllers.Karvy;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import play.mvc.Results;
import play.cache.Cached;
import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import play.mvc.Http;
import play.mvc.Http.Request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.dao.GenericDAO;
import com.idos.util.IdosConstants;
import com.idos.util.PasswordUtil;

import controllers.BaseController;
import controllers.StaticController;
import model.Branch;
import model.ConfigParams;
import model.Organization;
import model.Role;
import model.Users;
import java.util.logging.Level;

import java.time.Duration;
import model.UsersRoles;
import model.Vendor;
import model.karvy.ExternalUserCompanyDetails;
import com.typesafe.config.ConfigFactory;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.mvc.Results;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import com.idos.util.CookieUtils;
import java.util.Optional;
import views.html.*;

public class ExternalUserLoginController extends BaseController {
    public static EntityManager entityManager;
    public Request request;

    @Inject
    public ExternalUserLoginController() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public Result externalUserSignUp(Http.Request request) throws ClassNotFoundException, IOException {
        log.log(Level.FINE, "=========Start as externalUser==============");
        // Http.Request request = request();
        Http.Session session = request.session();
        String fullName = null;
        String email = null;
        String role = null;
        String allowedProcurement = null;
        Organization orgn = null;
        String pwdchanged = session.getOptional("pwdchanged").orElse("");
        session.removing();
        return Results.ok(extUserRegistration.render(fullName, email, role, orgn, allowedProcurement, pwdchanged,
                ConfigParams.getInstance()));
        // log.log(Level.FINE, "=========Start==============");
        // return
        // Results.ok(register.render(fullName,email,role,orgn,allowedProcurement,pwdchanged));
    }

    @Transactional
    public Result externalUserSignIn(Http.Request request) {
        Http.Session session = request.session();
        System.out.print(">>>>>>>>>>>>>>>>>>" + session);
        String email = session.getOptional("email").orElse("");
        Result result = Results.ok();
        // EntityManager entityManager = getEntityManager();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        log.log(Level.FINE, "==============start email " + email);
        Organization orgn = null;
        String orgName = null;
        try {
            Users usrinfo = Users.findActiveByEmail(email);
            String role = "";
            if (usrinfo == null) {
                Http.Cookie cookie = request.cookie("user");
                String userCookie = "";
                if (cookie != null) {
                    userCookie = cookie.value();
                }
                if (userCookie != null && !userCookie.equals("")) {
                    session.adding("email", userCookie);
                    usrinfo = Users.findActiveByEmail(userCookie);
                    List<UsersRoles> userRoles = usrinfo.getUserRoles();
                    for (UsersRoles roles : userRoles) {
                        role += roles.getRole().getName() + ",";
                    }
                    if (role.length() > 0) {
                        role = role.substring(0, role.length() - 1);
                    }
                    orgn = usrinfo.getOrganization();
                    orgName = orgn.getName();
                    String allowedProcurement = String.valueOf(usrinfo.getAllowedProcurementRequest());
                    log.log(Level.FINE, "=========End1==============");
                    return Results.ok(config.render(usrinfo.getFullName(), usrinfo.getEmail(), role, orgn,
                            allowedProcurement, usrinfo, ConfigParams.getInstance()));
                } else {
                    String fullName = null;
                    String allowedProcurement = null;
                    String pwdchanged = session.getOptional("pwdchanged").orElse("");
                    session.removing();
                    log.log(Level.FINE, "=========End2==============");
                    return Results.ok(registration.render(fullName, email, role, orgn, allowedProcurement, pwdchanged,
                            ConfigParams.getInstance()));
                    // return
                    // Results.ok(logonpage.render(fullName,email,role,orgn,allowedProcurement,pwdchanged));
                }
            } else {
                List<UsersRoles> userRoles = usrinfo.getUserRoles();
                for (UsersRoles roles : userRoles) {
                    role += roles.getRole().getName() + ",";
                }
                role = role.substring(0, role.length() - 1);
                log.log(Level.INFO, "User role:" + role);
                orgn = usrinfo.getOrganization();
                orgName = orgn.getName();
                String allowedProcurement = String.valueOf(usrinfo.getAllowedProcurementRequest());
                log.log(Level.FINE, "=========End3==============");
                return Results.ok(extUserCompanyDetails.render(usrinfo.getFullName(), usrinfo.getEmail(), role, orgn,
                        allowedProcurement, usrinfo, ConfigParams.getInstance()));
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, email, ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, email, orgName,
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    @Cached(key = "signin")
    @Transactional
    public Result externalLogin(Http.Request request) throws ClassNotFoundException, IOException {
        Organization orgn = null;
        Http.Session session = request.session();
        CookieUtils.discardCookie("user");
        CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
        String email = session.getOptional("email").orElse("");
        try {
            Users usrinfo = Users.findActiveByEmail(email);
            if (usrinfo != null) {
                String fullName = null;
                String role = null;
                String allowedProcurement = null;
                // String pwdchanged = session.getOptional("pwdchanged").orElse("");
                String pwdchanged = IdosConstants.PUBLICK;
                // session.removing();
                CookieUtils.discardCookie("user");
                CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
                log.log(Level.FINE, " ===3================");
                Duration expirationDuration = Duration.ZERO;
                return Results.ok(extUserRegistration.render(fullName, email, role, orgn, allowedProcurement,
                        pwdchanged, ConfigParams.getInstance()));
                // return
                // Results.ok(loginToIdos.render(fullName,email,role,orgn,allowedProcurement,pwdchanged));
                // }
            } else {
                String fullName = null;
                String role = null;
                String allowedProcurement = null;
                // String pwdchanged = session.getOptional("pwdchanged").orElse("");
                String pwdchanged = IdosConstants.PUBLICK;
                // session.removing();
                log.log(Level.FINE, " ===externalUser Login====5====" + pwdchanged);
                // return
                // Results.ok(loginToIdos.render(fullName,email,role,orgn,allowedProcurement,pwdchanged));

                Duration expirationDuration = Duration.ZERO;
                return Results.ok(extUserRegistration.render(fullName, email, role, orgn, allowedProcurement,
                        pwdchanged, ConfigParams.getInstance()));
                // return
                // Results.ok(logonpage.render(fullName,email,role,orgn,allowedProcurement,pwdchanged));

                // }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, email, null,
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    @Transactional
    public Result addExternalUser(Http.Request request) {
        log.log(Level.FINE, ">>>> Start in addExternalUser");
        Http.Session httpsession = request.session();
        String email = httpsession.getOptional("email").orElse("");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        Users user = Users.findActiveByEmail(email);
        Users newUsers = new Users();
        ExternalUserCompanyDetails newExtUser = new ExternalUserCompanyDetails();
        UsersRoles newuserroles = new UsersRoles();
        try {
            JsonNode json = request.body().asJson();
            // entitytransaction.begin();
            // boolean success = externalUserService.saveExternalUser(user,json,
            // entityManager, result);
            // entitytransaction.commit();

            String corporateEmail = json.findValue("companyEmailId").asText().toLowerCase();
            String adminPassword = json.findValue("adminPwd").asText();
            String personName = json.findValue("contactName").asText();
            String orgPhoneNumber = json.findValue("adminPhonenumber").asText();
            String orgWebsite = json.findValue("adminWebsite").asText();
            String businessName = json.findValue("companyName").asText();
            String address = json.findValue("address").asText();
            String pwd = PasswordUtil.encrypt(adminPassword);

            Map<String, Object> criterias = new HashMap<String, Object>(2);
            criterias.put("name", "EXTERNAL_USER_ORG");
            criterias.put("presentStatus", 1);
            Organization org = genericDAO.getByCriteria(Organization.class, criterias, entityManager);

            criterias.clear();
            criterias.put("name", "EXTERNAL_USER_BRANCH");
            criterias.put("presentStatus", 1);
            Branch branch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
            criterias.clear();

            newUsers.setOrganization(org);
            newUsers.setBranch(branch);
            newUsers.setPassword(pwd);
            newUsers.setFullName(personName);
            newUsers.setEmail(corporateEmail);
            newUsers.setPresentStatus(0);
            newUsers.setMobile(orgPhoneNumber);
            newUsers.setAddress(address);
            entitytransaction.begin();
            usercrud.save(user, newUsers, entityManager);

            StringBuilder sbquery = new StringBuilder("");
            sbquery.append("select obj from Role obj WHERE obj.name='EXTERNAL USER' and obj.presentStatus=1");
            List<Role> role = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
            newuserroles.setRole(role.get(0));
            newuserroles.setUser(Users.findById(newUsers.getId()));
            newuserroles.setBranch(Branch.findById(branch.getId()));
            newuserroles.setOrganization(Organization.findById(org.getId()));
            userrolecrud.save(user, newuserroles, entityManager);
            String OTPCode = generateAccessCode();
            log.log(Level.FINE, "OTPCode==" + OTPCode);
            newExtUser.setExtUserId(newUsers);
            newExtUser.setOrg(org);
            newExtUser.setPresentStatus(1);
            newExtUser.setExternalUserStatus(IdosConstants.EXTERNAL_USER_REGISTERED); // 4-External user without any
                                                                                      // access to any org.
            newExtUser.setOrgOtp(OTPCode);
            genericDAO.saveOrUpdate(newExtUser, null, entityManager);
            entitytransaction.commit();

            String body1 = extUserAccountCreationLink
                    .render(OTPCode, adminPassword, corporateEmail, ConfigParams.getInstance()).body();
            // final String username1 =
            // Play.application().configuration().getString("smtp.user");
            final String username = ConfigFactory.load().getString("smtp.user");
            String subject1 = "Activate Your Account";
            javax.mail.Session session1 = emailsession;
            mailTimer(body1, username, session1, corporateEmail, null, subject1);

            String body = accountCreation.render(adminPassword, corporateEmail, ConfigParams.getInstance()).body();
            String subject = "Account Created Successfully";
            javax.mail.Session session = emailsession;
            mailTimer(body, username, session, "alert@myidos.com", null, subject);
        } catch (Throwable ex) {
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            result.put("status", "failed");
        }
        return Results.ok(result);
    }

    @Transactional
    public Result externalUserCompanyList(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        Http.Session httpsession = request.session();
        // EntityManager entityManager = getEntityManager();
        ObjectNode result = Json.newObject();
        CookieUtils.discardCookie("user");
        CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
        String email = httpsession.getOptional("email").orElse("");
        Users usrinfo = Users.findActiveByEmail(email);
        try {

            JsonNode json = request.body().asJson();
            ArrayNode companyData = result.putArray("companyListData");
            // String email = json.findValue("usermail").asText();
            // session.adding("email", email);
            if (usrinfo == null) {
                return unauthorized();
            }

            Long extUserId = usrinfo.getId();
            Map<String, Object> criterias = new HashMap<String, Object>();
            List<ExternalUserCompanyDetails> companyList = ExternalUserCompanyDetails.findCompanyList(entityManager,
                    extUserId);
            if (companyList.size() != 0) {

                for (ExternalUserCompanyDetails company : companyList) {
                    ObjectNode row = Json.newObject();
                    row.put("corrUserId", company.getCorrUserId().getId());
                    row.put("corrEmail", company.getCorrUserId().getEmail());
                    row.put("corrPwd", company.getCorrUserId().getPassword());
                    row.put("companyId", company.getOrg().getId().toString());
                    row.put("companyName", company.getOrg().getName());
                    row.put("accessCode", company.getAccessCode());
                    row.put("companyUrl", company.getOrg().getWebUrl());
                    row.put("dateOfAccess", company.getDateOfAccess().toString());
                    row.put("adminContact", company.getOrg().getRegisteredPhoneNumber());
                    row.put("adminName", company.getOrg().getPersonName());
                    String adminEmail = null;
                    criterias.put("role.name", "MASTER ADMIN");
                    criterias.put("organization.id", company.getOrg().getId());
                    criterias.put("presentStatus", 1);
                    UsersRoles adminUser = genericDAO.getByCriteria(UsersRoles.class, criterias, entityManager);
                    if (adminUser != null) {
                        log.log(Level.FINE, "admin email==" + adminUser.getUser().getEmail());
                        adminEmail = adminUser.getUser().getEmail();
                    }
                    row.put("adminEmail", adminEmail);
                    companyData.add(row);
                    criterias.clear();
                }
            } else {
                ObjectNode row = Json.newObject();
                row.put("message", "Failure");
                companyData.add(row);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, usrinfo.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        Duration expirationDuration = Duration.ZERO;
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result externalUserCompanyToBeAdded(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session httpsession = request.session();
        ObjectNode result = Json.newObject();
        String email = httpsession.getOptional("email").orElse("");
        Users usrinfo = Users.findActiveByEmail(email);
        try {

            JsonNode json = request.body().asJson();
            ArrayNode companyData = result.putArray("companyToBeAddedList");
            String accessCode = json.findValue("accesscode").asText().toUpperCase();
            log.log(Level.FINE, "accessCode==" + accessCode);
            // String email = json.findValue("usermail").asText();
            // session.adding("email", email);
            if (usrinfo == null) {
                return unauthorized();
            }

            Long extUserId = usrinfo.getId();
            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("extUserId.id", extUserId);
            criterias.put("accessCode", accessCode);
            criterias.put("presentStatus", 1);
            ExternalUserCompanyDetails company = genericDAO.getByCriteria(ExternalUserCompanyDetails.class, criterias,
                    entityManager);
            criterias.clear();
            // Organization org = company.getOrg();
            if (company != null) {
                ObjectNode row = Json.newObject();
                row.put("orgid", company.getOrg().getId());
                row.put("name", company.getOrg().getName());
                row.put("registeredPhoneNumber", company.getOrg().getRegisteredPhoneNumber());
                row.put("registeredAddress", company.getOrg().getRegisteredAddress());
                row.put("dateOfAccess", company.getDateOfAccess().toString());
                if (company.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_CODE_SENT)
                    row.put("externalUserStatus", "No Access");
                else if (company.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_SUBMIT_FOR_APPROVAL)
                    row.put("externalUserStatus", "Waiting Access Confirmation");
                else if (company.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_TO_ORG)
                    row.put("externalUserStatus", "Access Given");
                else if (company.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_DEACTIVATED)
                    row.put("externalUserStatus", "Deactivated");
                companyData.add(row);

            } else {
                ObjectNode row = Json.newObject();
                row.put("message", "Failure");
                companyData.add(row);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, usrinfo.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result submitForAccessApproval(Http.Request request) {
        log.log(Level.FINE, ">>>> Start submitForAccessApproval");
        // EntityManager entityManager = getEntityManager();
        Http.Session httpsession = request.session();
        Users user = StaticController.getUserInfo(request);
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        CookieUtils.discardCookie("user");
        CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
        String email = httpsession.getOptional("email").orElse("");
        Users usrinfo = Users.findActiveByEmail(email);
        try {
            entitytransaction.begin();
            JsonNode json = request.body().asJson();
            ArrayNode companyData = result.putArray("userStatusChanged");
            String accessCode = json.findValue("accesscode").asText().toUpperCase();
            log.log(Level.FINE, "accessCode==" + accessCode);
            Users corrUser = new Users();
            // String email = json.findValue("usermail").asText();
            // session.adding("email", email);
            if (usrinfo == null) {
                return unauthorized();
            }

            Long extUserId = usrinfo.getId();

            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("extUserId.id", extUserId);
            criterias.put("accessCode", accessCode);
            criterias.put("presentStatus", 1);
            ExternalUserCompanyDetails company = genericDAO.getByCriteria(ExternalUserCompanyDetails.class, criterias,
                    entityManager);

            // Organization org = company.getOrg();
            if (company != null) {
                company.setExternalUserStatus(IdosConstants.EXTERNAL_USER_SUBMIT_FOR_APPROVAL); // changing status =2 as
                                                                                                // its waiting
                                                                                                // confirmation from the
                                                                                                // admin

                genericDAO.saveOrUpdate(company, usrinfo, entityManager);
                entitytransaction.commit();
                ObjectNode row = Json.newObject();
                if (company.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_CODE_SENT)
                    row.put("externalUserStatus", "No Access");
                else if (company.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_SUBMIT_FOR_APPROVAL)
                    row.put("externalUserStatus", "Waiting Access Confirmation");
                else if (company.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_TO_ORG)
                    row.put("externalUserStatus", "Access Given");

                companyData.add(row);

            } else {
                ObjectNode row = Json.newObject();
                row.put("message", "Failure");
                companyData.add(row);
            }

            criterias.clear();
            log.log(Level.FINE, "getting admin mail id...");
            String adminEmail = null;
            criterias.put("role.name", "MASTER ADMIN");
            criterias.put("organization.id", company.getOrg().getId());
            criterias.put("presentStatus", 1);
            UsersRoles adminUser = genericDAO.getByCriteria(UsersRoles.class, criterias, entityManager);
            if (adminUser != null) {
                String extUserEmail = usrinfo.getEmail();
                String corrUserEmail = "";
                String[] arrOfStr = extUserEmail.split("@");
                corrUserEmail = corrUserEmail + arrOfStr[0] + adminUser.getOrganization().getId().toString() + "@"
                        + arrOfStr[1];
                log.log(Level.FINE, "corrUserEmail==" + corrUserEmail);
                log.log(Level.FINE, "admin email==" + adminUser.getUser().getEmail());
                adminEmail = adminUser.getUser().getEmail();
                // creating a corresponding email id and password
                entitytransaction.begin();
                corrUser.setFullName(usrinfo.getFullName());
                corrUser.setPresentStatus(1);
                corrUser.setBranch(adminUser.getBranch());
                corrUser.setOrganization(adminUser.getOrganization());
                corrUser.setEmail(corrUserEmail);
                corrUser.setPassword(usrinfo.getPassword());
                // giving a refernce of corresponding user id to external user.
                genericDAO.save(corrUser, usrinfo, entityManager);
                company.setCorrUserId(corrUser);
                entitytransaction.commit();
                UsersRoles newuserroles = new UsersRoles();
                StringBuilder sbquery = new StringBuilder("");
                sbquery.append(
                        "select obj from Role obj WHERE obj.name IN ('APPROVER','CREATOR','ACCOUNTANT','CONTROLLER') and obj.presentStatus=1");
                List<Role> roleList = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
                if (roleList != null) {
                    for (Role role : roleList) {
                        newuserroles.setRole(role);
                        newuserroles.setUser(Users.findById(corrUser.getId()));
                        newuserroles.setBranch(corrUser.getBranch());
                        newuserroles.setOrganization(corrUser.getOrganization());
                        entitytransaction.begin();
                        userrolecrud.save(user, newuserroles, entityManager);
                        entitytransaction.commit();
                        newuserroles = new UsersRoles();
                    }

                }

                entitytransaction.begin();
                genericDAO.saveOrUpdate(company, usrinfo, entityManager);
                entitytransaction.commit();
            } else {
                log.log(Level.FINE, "admin email error");
                ObjectNode row = Json.newObject();
                row.put("message", "Failure");
            }
            String orgId = company.getOrg().getId().toString();
            final String username = ConfigFactory.load().getString("smtp.user");
            String body1 = externalUserConfirmAccessMail.render(usrinfo.getFullName(), usrinfo.getId().toString(),
                    orgId, company.getOrg().getName(), ConfigParams.getInstance()).body();
            String subject = "External User Access Confirmation";
            javax.mail.Session session = emailsession;
            mailTimer(body1, "alert@myidos.com", session, adminEmail, null, subject);

        } catch (Exception ex) {
            log.log(Level.SEVERE, usrinfo.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        Duration expirationDuration = Duration.ZERO;
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result confirmAccess(String extUserId, String orgId, Http.Request request)
            throws ClassNotFoundException, IOException {
        log.log(Level.FINE, "=========Start==============");

        // EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        Http.Session session = request.session();
        String fullName = null;
        String email = null;
        String role = null;
        String allowedProcurement = null;
        Organization orgn = null;
        String pwdchanged = null;
        Users users = Users.findActiveByEmail(email);
        ObjectNode results = Json.newObject();
        ArrayNode an = results.putArray("confirmAccess");
        Long organizationId = Long.parseLong(orgId);
        Long id = Long.parseLong(extUserId);
        ObjectNode row = Json.newObject();
        try {
            transaction.begin();
            if (orgId != null) {
                Map<String, Object> criterias = new HashMap<String, Object>();
                criterias.put("org.id", organizationId);
                criterias.put("extUserId.id", id);
                criterias.put("presentStatus", 1);
                ExternalUserCompanyDetails extUser = genericDAO.getByCriteria(ExternalUserCompanyDetails.class,
                        criterias, entityManager);
                if (extUser != null) {
                    extUser.setExternalUserStatus(IdosConstants.EXTERNAL_USER_ACCESS_TO_ORG);
                    extUser.setDateOfAcceptance(Calendar.getInstance().getTime());
                    genericDAO.saveOrUpdate(extUser, users, entityManager);
                    row.put("confirmAccess", "User has been given access");
                    an.add(row);
                    fullName = extUser.getExtUserId().getFullName();
                    orgn = extUser.getOrg();
                } else {
                    row.put("activated", "Given Email is not registered with us.");
                    an.add(row);
                }
            }
            session.removing();
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
        if (orgn != null) {
            return Results.ok(confirmAccess.render(fullName, orgn.getName(), ConfigParams.getInstance()));
        } else {
            return Results.ok(confirmAccess.render(fullName, "", ConfigParams.getInstance()));
        }
    }

    @Transactional
    public Result getAllExternalUsers(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        Http.Session httpsession = request.session();
        ObjectNode result = Json.newObject();
        CookieUtils.discardCookie("user");
        CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
        String email = httpsession.getOptional("email").orElse("");
        Users usrinfo = Users.findActiveByEmail(email);
        try {

            JsonNode json = request.body().asJson();
            ArrayNode extUsersStatus = result.putArray("extUserListWithAccess");
            // String email = json.findValue("usermail").asText();
            // session.adding("email", email);
            if (usrinfo == null) {
                return unauthorized();
            }

            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("role.name", "EXTERNAL USER");
            criterias.put("presentStatus", 1);
            List<UsersRoles> extUsersList = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);
            criterias.clear();
            if (extUsersList != null) {

                for (UsersRoles extUser : extUsersList) {

                    criterias.put("extUserId.id", extUser.getUser().getId());
                    criterias.put("org.id", usrinfo.getOrganization().getId());
                    criterias.put("presentStatus", 1);
                    ExternalUserCompanyDetails extUserWithAccess = genericDAO
                            .getByCriteria(ExternalUserCompanyDetails.class, criterias, entityManager);
                    criterias.clear();
                    if (extUserWithAccess != null) {
                        ObjectNode row = Json.newObject();
                        row.put("extUserId", extUserWithAccess.getExtUserId().getId());
                        row.put("orgName", extUserWithAccess.getOrg().getName());
                        row.put("extUserEmail", extUser.getUser().getEmail());
                        row.put("orgId", extUserWithAccess.getOrg().getId());
                        row.put("extUserName", extUserWithAccess.getExtUserId().getFullName());
                        if (extUserWithAccess.getExtUserId().getMobile() == null)
                            row.put("extUserPhNo", "");
                        else
                            row.put("extUserPhNo", extUserWithAccess.getExtUserId().getMobile());

                        if (extUserWithAccess.getExtUserId().getAddress() == null)
                            row.put("extUserAddress", "");
                        else
                            row.put("extUserAddress", extUserWithAccess.getExtUserId().getAddress());

                        if (extUserWithAccess.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_CODE_SENT)
                            row.put("status", "Access Code Sent");
                        else if (extUserWithAccess
                                .getExternalUserStatus() == IdosConstants.EXTERNAL_USER_SUBMIT_FOR_APPROVAL)
                            row.put("status", "Awaiting Confirmation From Admin");
                        else if (extUserWithAccess.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_TO_ORG)
                            row.put("status", "Access Given");
                        else if (extUserWithAccess.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_DEACTIVATED)
                            row.put("status", "Deactivated");
                        else if (extUserWithAccess.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ORG_ADD)
                            row.put("status", "No Access");

                        extUsersStatus.add(row);
                    }
                }

            } else {
                ObjectNode row = Json.newObject();
                row.put("message", "Failure");
                extUsersStatus.add(row);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, usrinfo.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        Duration expirationDuration = Duration.ZERO;
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    public static String generateAccessCode() {
        String aToZ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // 36 letter.
        // Random rand = new Random();
        SecureRandom rand = new SecureRandom();
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randIndex = rand.nextInt(aToZ.length());
            res.append(aToZ.charAt(randIndex));
        }
        return res.toString();
    }

    @Transactional
    public Result provideAccessToExtUser(Long id, Http.Request request) {
        log.log(Level.FINE, ">>>> Start provideAccessToExtUser");
        // EntityManager entityManager = getEntityManager();
        Http.Session httpsession = request.session();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        CookieUtils.discardCookie("user");
        CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
        String email = httpsession.getOptional("email").orElse("");
        Users usrinfo = Users.findActiveByEmail(email);
        try {

            JsonNode json = request.body().asJson();
            ArrayNode companyData = result.putArray("provideAccessToExtUser");

            String accessCode = generateAccessCode();
            log.log(Level.FINE, "accessCode==" + accessCode);

            if (usrinfo == null) {
                return unauthorized();
            }

            Map<String, Object> criterias = new HashMap<String, Object>();

            // Activating a deactive user....
            criterias.put("extUserId.id", id);
            criterias.put("org.id", usrinfo.getOrganization().getId());
            criterias.put("externalUserStatus", IdosConstants.EXTERNAL_USER_DEACTIVATED);
            criterias.put("presentStatus", 1);
            ExternalUserCompanyDetails deactiveUser = genericDAO.getByCriteria(ExternalUserCompanyDetails.class,
                    criterias, entityManager);
            if (deactiveUser != null) {
                ObjectNode row = Json.newObject();
                deactiveUser.setAccessCode(accessCode);
                deactiveUser.setExternalUserStatus(IdosConstants.EXTERNAL_USER_ACCESS_CODE_SENT);
                deactiveUser.setDateOfAccess(Calendar.getInstance().getTime());
                entitytransaction.begin();
                genericDAO.saveOrUpdate(deactiveUser, usrinfo, entityManager);
                entitytransaction.commit();
                criterias.clear();
                row.put("message", "Access Code Successfully sent");
                row.put("extUserId", id);
                String body1 = externalUserAccessCodeMail.render(deactiveUser.getExtUserId().getFullName(),
                        usrinfo.getOrganization().getName(), accessCode, ConfigParams.getInstance()).body();
                String subject = "Access Code for Organization " + usrinfo.getOrganization().getName();
                javax.mail.Session session = emailsession;
                mailTimer(body1, "alert@myidos.com", session, deactiveUser.getExtUserId().getEmail(), null, subject);

                String body2 = externalUserAdminNotifyMail.render(deactiveUser.getExtUserId().getFullName(),
                        usrinfo.getOrganization().getName(), ConfigParams.getInstance()).body();
                String subject2 = "External User Access sent for Organization " + usrinfo.getOrganization().getName();
                mailTimer(body2, "alert@myidos.com", session, usrinfo.getEmail(), null, subject2);

                companyData.add(row);
            } else {

                criterias.clear();
                criterias.put("id", id);
                Users user = genericDAO.getByCriteria(Users.class, criterias, entityManager);
                criterias.clear();
                criterias.put("extUserId.id", id);
                criterias.put("org.id", usrinfo.getOrganization().getId());
                criterias.put("externalUserStatus", IdosConstants.EXTERNAL_USER_ORG_ADD);
                criterias.put("presentStatus", 1);
                ExternalUserCompanyDetails extUser = genericDAO.getByCriteria(ExternalUserCompanyDetails.class,
                        criterias, entityManager);
                if (extUser != null) {
                    log.log(Level.FINE, "inside if... provide access");
                    ObjectNode row = Json.newObject();
                    extUser.setAccessCode(accessCode);
                    extUser.setCompanyUrl(usrinfo.getOrganization().getWebUrl());
                    extUser.setDateOfAccess(Calendar.getInstance().getTime());
                    extUser.setExternalUserStatus(IdosConstants.EXTERNAL_USER_ACCESS_CODE_SENT);
                    entitytransaction.begin();
                    genericDAO.saveOrUpdate(extUser, usrinfo, entityManager);
                    entitytransaction.commit();
                    // String orgId = usrinfo.getOrganization().getId().toString();
                    row.put("message", "Access Code Successfully sent");
                    row.put("extUserId", id);
                    String body1 = externalUserAccessCodeMail.render(user.getFullName(),
                            usrinfo.getOrganization().getName(), accessCode, ConfigParams.getInstance()).body();
                    String subject = "Access Code for Organization " + usrinfo.getOrganization().getName();
                    javax.mail.Session session = emailsession;
                    mailTimer(body1, "alerts@myidos.com", session, user.getEmail(), null, subject);

                    String body2 = externalUserAdminNotifyMail
                            .render(user.getFullName(), usrinfo.getOrganization().getName(), ConfigParams.getInstance())
                            .body();
                    String subject2 = "External User Access sent for Organization "
                            + usrinfo.getOrganization().getName();
                    mailTimer(body2, "alert@myidos.com", session, usrinfo.getEmail(), null, subject2);

                    companyData.add(row);
                } else {
                    ObjectNode row = Json.newObject();
                    row.put("message", "Failure");
                    companyData.add(row);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, usrinfo.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        Duration expirationDuration = Duration.ZERO;
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result deactivateExtUser(Long id, Http.Request request) {
        log.log(Level.FINE, ">>>> Start deactivateExtUser");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        CookieUtils.discardCookie("user");
        CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
        String email = session.getOptional("email").orElse("");
        Users usrinfo = Users.findActiveByEmail(email);
        try {

            JsonNode json = request.body().asJson();
            ArrayNode companyData = result.putArray("deactivateExtUser");

            if (usrinfo == null) {
                return unauthorized();
            }

            Map<String, Object> criterias = new HashMap<String, Object>();
            criterias.put("extUserId.id", id);
            criterias.put("org.id", usrinfo.getOrganization().getId());
            criterias.put("presentStatus", 1);
            ExternalUserCompanyDetails extUser = genericDAO.getByCriteria(ExternalUserCompanyDetails.class, criterias,
                    entityManager);
            if (extUser != null) {
                ObjectNode row = Json.newObject();
                extUser.setExternalUserStatus(IdosConstants.EXTERNAL_USER_DEACTIVATED); // deactivate external user.
                entitytransaction.begin();
                genericDAO.saveOrUpdate(extUser, usrinfo, entityManager);
                entitytransaction.commit();
                // String orgId = usrinfo.getOrganization().getId().toString();
                row.put("message", "User Deativated");
                row.put("extUserId", id);

                companyData.add(row);
            } else {
                ObjectNode row = Json.newObject();
                row.put("message", "Failure");
                companyData.add(row);
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, usrinfo.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        Duration expirationDuration = Duration.ZERO;
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result showExtUserDetails(Http.Request request) {
        log.log(Level.FINE, ">>>> Start in showExtUserDetails()...");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        // EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        CookieUtils.discardCookie("user");
        CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
        String email = session.getOptional("email").orElse("");
        Users usrinfo = Users.findActiveByEmail(email);
        try {
            JsonNode json = request.body().asJson();
            ArrayNode companyData = result.putArray("extUserToBeAdded");
            String OTPCode = json.findValue("OTPCode").asText().toUpperCase();
            log.log(Level.FINE, "OTPCode==" + OTPCode);
            // String email = json.findValue("usermail").asText();
            // session.adding("email", email);
            if (usrinfo == null) {
                return unauthorized();
            }
            ObjectNode row = Json.newObject();
            Map<String, Object> criterias = new HashMap<String, Object>();

            criterias.put("orgOtp", OTPCode);
            criterias.put("presentStatus", 1);
            List<ExternalUserCompanyDetails> extUserList = genericDAO.findByCriteria(ExternalUserCompanyDetails.class,
                    criterias, entityManager);
            log.log(Level.FINE, "extUserList.size=====" + extUserList.size());
            if (extUserList.size() == 0) {
                row.put("message", "Failure");
                companyData.add(row);
            } else if (extUserList.size() != 0) {
                criterias.clear();

                criterias.put("id", extUserList.get(0).getExtUserId().getId());
                criterias.put("presentStatus", 1);
                Users extUserInfo = genericDAO.getByCriteria(Users.class, criterias, entityManager);
                log.log(Level.FINE, "extUserInfo.getId()=" + extUserInfo.getId());
                criterias.clear();
                criterias.put("orgOtp", OTPCode);
                criterias.put("org.id", usrinfo.getOrganization().getId());
                criterias.put("presentStatus", 1);
                ExternalUserCompanyDetails extUser = genericDAO.getByCriteria(ExternalUserCompanyDetails.class,
                        criterias, entityManager);
                if (extUser != null) {
                    row.put("name", extUser.getExtUserId().getFullName());
                    row.put("email", extUser.getExtUserId().getEmail());
                    if (extUser.getExtUserId().getMobile() == null)
                        row.put("registeredPhoneNumber", "");
                    else
                        row.put("registeredPhoneNumber", extUser.getExtUserId().getMobile());
                    if (extUser.getExtUserId().getAddress() == null)
                        row.put("registeredAddress", "");
                    else
                        row.put("registeredAddress", extUser.getExtUserId().getAddress());
                    if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_CODE_SENT)
                        row.put("externalUserStatus", "Access Code Sent");
                    else if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_SUBMIT_FOR_APPROVAL)
                        row.put("externalUserStatus", "Waiting Access Confirmation");
                    else if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_TO_ORG)
                        row.put("externalUserStatus", "Access Given");
                    else if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_DEACTIVATED)
                        row.put("externalUserStatus", "Deactivated");
                    else if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_REGISTERED)
                        row.put("externalUserStatus", "No Access");
                    else if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ORG_ADD)
                        row.put("externalUserStatus", "Already added to the Organization");
                    row.put("message", "success");
                    companyData.add(row);
                } else {
                    row.put("name", extUserInfo.getFullName());
                    row.put("email", extUserInfo.getEmail());
                    row.put("registeredPhoneNumber", extUserInfo.getMobile());
                    row.put("registeredAddress", extUserInfo.getAddress());
                    row.put("externalUserStatus", "No Access");
                    row.put("message", "success");
                    companyData.add(row);

                }
            }

        } catch (Exception ex) {
            log.log(Level.SEVERE, usrinfo.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result addExtUserToOrgViaOtp(Http.Request request) {
        log.log(Level.FINE, ">>>> Start in addExtUserToOrgViaOtp()...");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        String email = session.getOptional("email").orElse("");
        Users usrinfo = Users.findActiveByEmail(email);
        try {
            JsonNode json = request.body().asJson();
            ArrayNode companyData = result.putArray("extUserToBeAdded");
            String OTPCode = json.findValue("OTPCode").asText().toUpperCase();
            log.log(Level.FINE, "OTPCode==" + OTPCode);
            // String email = json.findValue("usermail").asText();
            // session.adding("email", email);
            if (usrinfo == null) {
                return unauthorized();
            }

            Map<String, Object> criterias = new HashMap<String, Object>();

            // getting users info
            criterias.clear();
            criterias.put("orgOtp", OTPCode);
            criterias.put("presentStatus", 1);
            List<ExternalUserCompanyDetails> extUserList = genericDAO.findByCriteria(ExternalUserCompanyDetails.class,
                    criterias, entityManager);
            if (extUserList.size() != 0) {
                criterias.clear();
                criterias.put("id", extUserList.get(0).getExtUserId().getId());
                Users extUserInfo = genericDAO.getByCriteria(Users.class, criterias, entityManager);
                criterias.clear();

                // getting otp and org id if prsent...
                criterias.put("orgOtp", OTPCode);
                criterias.put("org.id", usrinfo.getOrganization().getId());
                criterias.put("presentStatus", 1);
                ExternalUserCompanyDetails extUser = genericDAO.getByCriteria(ExternalUserCompanyDetails.class,
                        criterias, entityManager);
                criterias.clear();
                if (extUser == null) {
                    ExternalUserCompanyDetails newExtUser = new ExternalUserCompanyDetails();
                    newExtUser.setExtUserId(extUserInfo);
                    newExtUser.setOrg(usrinfo.getOrganization());
                    newExtUser.setExternalUserStatus(IdosConstants.EXTERNAL_USER_ORG_ADD);
                    newExtUser.setCompanyUrl(usrinfo.getOrganization().getWebUrl());
                    newExtUser.setPresentStatus(1);
                    newExtUser.setOrgOtp(OTPCode);
                    entitytransaction.begin();
                    genericDAO.save(newExtUser, usrinfo, entityManager);
                    entitytransaction.commit();
                    ObjectNode row = Json.newObject();
                    row.put("name", newExtUser.getExtUserId().getFullName());
                    row.put("email", newExtUser.getExtUserId().getEmail());
                    row.put("registeredPhoneNumber", newExtUser.getExtUserId().getMobile());
                    row.put("registeredAddress", newExtUser.getExtUserId().getAddress());
                    if (newExtUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_CODE_SENT)
                        row.put("externalUserStatus", "Access Code Sent");
                    else if (newExtUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_SUBMIT_FOR_APPROVAL)
                        row.put("externalUserStatus", "Waiting Access Confirmation");
                    else if (newExtUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_TO_ORG)
                        row.put("externalUserStatus", "Access Given");
                    else if (newExtUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_DEACTIVATED)
                        row.put("externalUserStatus", "Deactivated");
                    else if (newExtUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_REGISTERED)
                        row.put("externalUserStatus", "No Access");
                    else if (newExtUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ORG_ADD)
                        row.put("externalUserStatus", "External User Added");
                    row.put("message", "success");
                    companyData.add(row);
                } else {
                    ObjectNode row = Json.newObject();
                    row.put("name", extUser.getExtUserId().getFullName());
                    row.put("email", extUser.getExtUserId().getEmail());
                    row.put("registeredPhoneNumber", extUser.getExtUserId().getMobile());
                    row.put("registeredAddress", extUser.getExtUserId().getAddress());
                    if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_CODE_SENT)
                        row.put("externalUserStatus", "Access Code Sent");
                    else if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_SUBMIT_FOR_APPROVAL)
                        row.put("externalUserStatus", "Waiting Access Confirmation");
                    else if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ACCESS_TO_ORG)
                        row.put("externalUserStatus", "Access Given");
                    else if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_DEACTIVATED)
                        row.put("externalUserStatus", "Deactivated");
                    else if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_REGISTERED)
                        row.put("externalUserStatus", "No Access");
                    else if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_ORG_ADD)
                        row.put("externalUserStatus", "External User Already added");
                    row.put("message", "success");
                    companyData.add(row);
                }
            } else {
                ObjectNode row = Json.newObject();
                row.put("message", "Failure");
                companyData.add(row);
            }

            /*
             * List<ExternalUserCompanyDetails>extUserList =
             * genericDAO.findByCriteria(ExternalUserCompanyDetails.class,
             * criterias,entityManager);
             * if(extUserList.size()!=0){
             * criterias.clear();
             * criterias.put("id", extUserList.get(0).getExtUserId().getId());
             * Users extUserInfo = genericDAO.getByCriteria(Users.class, criterias,
             * entityManager);
             * criterias.clear();
             * for(ExternalUserCompanyDetails existExtUser : extUserList){
             * if(existExtUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_REGISTERED){
             * log.log(Level.FINE, "When status =4...");
             * existExtUser.setOrg(usrinfo.getOrganization());
             * existExtUser.setExternalUserStatus(IdosConstants.EXTERNAL_USER_ORG_ADD);
             * //ext user added by admin to its org..(access still not given)...
             * entitytransaction.begin();
             * genericDAO.saveOrUpdate(existExtUser, usrinfo, entityManager);
             * entitytransaction.commit();
             * ObjectNode row = Json.newObject();
             * row.put("message", "Success");
             * row.put("externalUserStatus", "External User Added");
             * row.put("name", existExtUser.getExtUserId().getFullName());
             * row.put("email", existExtUser.getExtUserId().getEmail());
             * row.put("registeredPhoneNumber", existExtUser.getExtUserId().getMobile());
             * row.put("registeredAddress", existExtUser.getExtUserId().getAddress());
             * break;
             * }
             * else{
             * int i=0;
             * while(i < extUserList.size()){
             * log.log(Level.FINE, "i iterating in list..."+i);
             * ExternalUserCompanyDetails existUser = extUserList.get(i);
             * log.log(Level.FINE,
             * "existUser name==="+extUserList.get(i).getExtUserId().getId());
             * if(existUser.getOrg().getId() == usrinfo.getOrganization().getId()){
             * ObjectNode row = Json.newObject();
             * row.put("name", existUser.getExtUserId().getFullName());
             * row.put("email", existUser.getExtUserId().getEmail());
             * row.put("registeredPhoneNumber", existUser.getExtUserId().getMobile());
             * row.put("registeredAddress", existUser.getExtUserId().getAddress());
             * if(existUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_ACCESS_CODE_SENT)
             * row.put("externalUserStatus","Access Code Sent");
             * else if(existUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_SUBMIT_FOR_APPROVAL)
             * row.put("externalUserStatus","Waiting Access Confirmation");
             * else if(existUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_ACCESS_TO_ORG)
             * row.put("externalUserStatus","Access Given");
             * else if(existUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_DEACTIVATED)
             * row.put("externalUserStatus","Deactivated");
             * else if(existUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_REGISTERED)
             * row.put("externalUserStatus","No Access");
             * else if(existUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_ORG_ADD)
             * row.put("externalUserStatus","External User Already added");
             * row.put("message", "success");
             * companyData.add(row);
             * break;
             * }
             * 
             * i++;
             * }
             * if(i == extUserList.size()){
             * log.log(Level.FINE, "when i=="+i);
             * ExternalUserCompanyDetails newExtUser = new ExternalUserCompanyDetails();
             * newExtUser.setExtUserId(extUserInfo);
             * newExtUser.setOrg(usrinfo.getOrganization());
             * newExtUser.setExternalUserStatus(IdosConstants.EXTERNAL_USER_ORG_ADD);
             * newExtUser.setCompanyUrl(usrinfo.getOrganization().getWebUrl());
             * newExtUser.setPresentStatus(1);
             * newExtUser.setOrgOtp(OTPCode);
             * entitytransaction.begin();
             * genericDAO.save(newExtUser, usrinfo, entityManager);
             * entitytransaction.commit();
             * ObjectNode row = Json.newObject();
             * row.put("name", newExtUser.getExtUserId().getFullName());
             * row.put("email", newExtUser.getExtUserId().getEmail());
             * row.put("registeredPhoneNumber", newExtUser.getExtUserId().getMobile());
             * row.put("registeredAddress", newExtUser.getExtUserId().getAddress());
             * if(newExtUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_ACCESS_CODE_SENT)
             * row.put("externalUserStatus","Access Code Sent");
             * else if(newExtUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_SUBMIT_FOR_APPROVAL)
             * row.put("externalUserStatus","Waiting Access Confirmation");
             * else if(newExtUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_ACCESS_TO_ORG)
             * row.put("externalUserStatus","Access Given");
             * else if(newExtUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_DEACTIVATED)
             * row.put("externalUserStatus","Deactivated");
             * else if(newExtUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_REGISTERED)
             * row.put("externalUserStatus","No Access");
             * else if(newExtUser.getExternalUserStatus() ==
             * IdosConstants.EXTERNAL_USER_ORG_ADD)
             * row.put("externalUserStatus","External User Added");
             * row.put("message", "success");
             * companyData.add(row);
             * }
             * }
             * }
             */

        } catch (Exception ex) {
            log.log(Level.SEVERE, usrinfo.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result logoutExtUser(Http.Request request) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> Start in logoutExtUser");
        // EntityManager entityManager = getEntityManager();
        Http.Session session = request.session();
        CookieUtils.discardCookie("user");
        CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
        EntityTransaction entitytransaction = entityManager.getTransaction();
        String fullName = null;
        String role = null;
        String email = null;
        String pwdchangemsg = null;
        String allowedProcurement = null;
        Organization orgn = null;
        Users user = null;
        ObjectNode results = Json.newObject();

        try {
            email = session.getOptional("email").orElse("");
            user = Users.findActiveByEmail(email);
            log.log(Level.FINE, "user info = " + user.getEmail());
            log.log(Level.FINE, "user id==" + user.getId());
            ArrayNode an = results.putArray("usertype");
            ObjectNode row = Json.newObject();
            if (user != null) {
                user.setInSession(0);
                user.setAuthToken(null);
                entitytransaction.begin();
                genericDAO.saveOrUpdate(user, user, entityManager);
                entitytransaction.commit();
                session.removing();
                results.put("logout", user.getEmail());
            }

        } catch (Exception ex) {
            if (entitytransaction.isActive()) {
                entitytransaction.rollback();
            }
            log.log(Level.FINE, "Catch block");
            log.log(Level.INFO, user.getEmail(), ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, ">>>> End");
        Duration expirationDuration = Duration.ZERO;
        return Results.ok(results);
    }

}
