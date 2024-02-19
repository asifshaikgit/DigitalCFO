package controllers;

import com.idos.util.*;
import com.idos.util.KARVY.AESShaEncryptionKARVY;
import controllers.Karvy.KarvyAuthorization;
import model.*;
import service.PwcAuthUsersDAOImpl;
import com.idos.dao.PwcAuthUsersDAO;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;
import play.cache.Cached;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Http.Cookie;
import service.AccountSettingService;
import service.ApplicationService;
import service.ApplicationServiceImpl;
import service.PasswordChangeService;
import views.html.*;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.logging.Level;

import java.io.File;
import java.io.IOException;
import java.util.*;
import views.html.pwc.*;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.Karvy.ExternalUserLoginController;
import play.Application;
import javax.inject.Inject;

/**
 * Created by Sunil Namdev on 23-09-2016.
 */
public class ApplicationController extends BaseController {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;
    public static Application application;
    public Request request;

    @Inject
    public ApplicationController(Application application) {
        super(application);
        System.out.println("SK >>>>>>>>>>>> Inside AppController ");
        entityManager = EntityManagerProvider.getEntityManager();
        this.application = application;
        System.out.println("SK >>>>>>>>>>>> Inside AppController ");
    }

    private static CRUDController<Organization> orgcrud = new CRUDController<Organization>(application);
    private static CRUDController<Branch> bnchcrud = new CRUDController<Branch>(application);
    private static CRUDController<Users> usercrud = new CRUDController<Users>(application);
    private static CRUDController<Particulars> partcrud = new CRUDController<Particulars>(application);
    protected static CRUDController<UsersRoles> userrolecrud = new CRUDController<UsersRoles>(application);
    private static final ApplicationService APPLICATION_SERVICE = new ApplicationServiceImpl();
    private static Http.Session httpSession;

    @Cached(key = "homepage", duration = 3600)
    public Result registration() {
        ConfigParams configParams = ConfigParams.getInstance();
        System.out.println("SK >>>>>>>>>>>> Inside registration " + ConfigFactory.load().getString("company.owner"));
        // return Results.ok(auditfirm.render());
        System.out.println("SK >>>>>>>>>>>> ConfigParams " + configParams);
        System.out.println("SK >>>>>>>>>>>> ConfigParams " + configParams.getCompanyOwner());
        if (configParams.getCompanyOwner().equals("KARVY")) {
            return Results.ok(indexkarvy.render(configParams));
        } else if (configParams.getCompanyOwner().equals("PWC")) {
            System.out.println("SK >>>>>>>>>>>> Inside PWC " + configParams.getCompanyOwner());
            return Results.ok(indexpwc.render(configParams));
        } else if (configParams.getCompanyOwner().equals("KPMG")) {
            return Results.ok(indexkpmg.render(configParams));
        } else if (configParams.getCompanyOwner().equals("SOLV")) {
            return Results.ok(indexsolv.render(configParams));
        } else {
            System.out.println("SK >>>>>>>>>>>> Inside else");
            return Results.ok(index.render(configParams));
        }
    }

    @Cached(key = "more", duration = 3600)
    public Result more() {
        return Results.ok(more.render());
    }

    public Result selectedOption(int option) {
        play.mvc.Result result = null;
        if (option == 1) {
            result = ok(faciliate.render());
        } else if (option == 2) {
            result = Results.ok(smallbusiness.render());
        } else if (option == 3) {
            result = Results.ok(midsizedbusiness.render());
        } else if (option == 4) {
            result = Results.ok(functioningerp.render());
        } else if (option == 5) {
            result = Results.ok(auditfirm.render());
        } else if (option == 6) {
            result = Results.ok(faciliatecompliances.render());
        } else if (option == 7) {
            result = Results.ok(empowerourbus.render());
        } else if (option == 9) {
            result = Results.ok(weareabank.render());
        } else if (option == 10) {
            result = Results.ok(collaboratepartner.render());
        } else if (option == 11) {
            result = Results.ok(compbackground.render());
        } else if (option == 12) {
            result = Results.ok(goodsnservice.render());
        } else if (option == 14) {
            result = Results.ok(idoscost.render());
        } else if (option == 15) {
            result = Results.ok(casestudiesservices.render());
        } else if (option == 16) {
            result = Results.ok(casestudiesstartups.render());
        } else if (option == 17) {
            return Results.ok(casestudiesmanufact.render());
        }
        return result;
    }

    @Cached(key = "aboutus", duration = 3600)
    public Result aboutUs() {
        // return Results.ok(about.render(ConfigParams.getInstance()));
        return Results.ok();
    }

    @Cached(key = "contactus", duration = 3600)
    public Result contactUs() {
        // return Results.ok(contact.render(ConfigParams.getInstance()));
        return Results.ok();
    }

    @Cached(key = "pricing", duration = 3600)
    public Result pricing() {
        // return Results.ok(pricing.render(ConfigParams.getInstance()));
        return Results.ok();
    }

    @Cached(key = "lastmile", duration = 3600)
    public Result lastMile() {
        // return Results.ok(lastmile.render(ConfigParams.getInstance()));
        return Results.ok();
    }

    @Cached(key = "requestdemo", duration = 3600)
    public Result requestDemo() {
        return Results.ok();
        // return Results.ok(requestdemo.render(ConfigParams.getInstance()));
    }

    @Cached(key = "gstknowledge", duration = 3600)
    public Result gstKnowledge() {
        return Results.ok(gstknowledge.render(ConfigParams.getInstance()));
    }

    public Result reg() {
        return Results.ok(register.render());
    }

    @Transactional
    public Result ssoAuthorization(Http.Request request) {
        String useripAddress = request.remoteAddress();
        Long stateString = System.currentTimeMillis();

        Cookie checkStateCookie = request.cookie("NOUNCE_STATE");
        if (null != checkStateCookie) {
            CookieUtils.discardCookie("NOUNCE_STATE");
        }

        log.log(Level.FINE, "Saving state key NOUNCE_STATE" + useripAddress + " in cookie for validating auth user : "
                + stateString.toString());
        System.out.println("Saving state value in session for validating auth user : " + stateString.toString());
        log.log(Level.FINE,
                "redirecting user to pwc's authorization page: " + ConfigParams.getSsoPwcApiHost()
                        + "/authorize?response_type=code&state=" + stateString.toString() + "&client_id="
                        + ConfigParams.getSsoPwcClientId() + "&redirect_uri=" + ConfigParams.getSsoPwcRedirectUrl()
                        + "&scope=" + ConfigParams.getSsoPwcScope());
        System.out.println("redirecting user to pwc's authorization page: " + ConfigParams.getSsoPwcApiHost()
                + "/authorize?response_type=code&state=" + stateString.toString() + "&client_id="
                + ConfigParams.getSsoPwcClientId() + "&redirect_uri=" + ConfigParams.getSsoPwcRedirectUrl() + "&scope="
                + ConfigParams.getSsoPwcScope());
        Cookie cookie = Cookie.builder("NOUNCE_STATE", stateString.toString()).build();
        return Results.ok(ssoAuth.render(ConfigParams.getSsoPwcApiHost(), ConfigParams.getSsoPwcClientId(),
                ConfigParams.getSsoPwcClientSecret(), ConfigParams.getSsoPwcRedirectUrl(),
                ConfigParams.getSsoPwcScope(), stateString.toString())).withCookies(cookie);

    }

    @Transactional
    public Result ssoCallback(Http.Request request) {
        log.log(Level.FINE, "HK >>> callback called");
        System.out.println("HK >>> callback called");
        // EntityManager entityManager= jpaApi.em();
        EntityTransaction entityTransaction = entityManager.getTransaction();

        String code = request.getQueryString("code");
        String iss = request.getQueryString("iss");
        String state = request.getQueryString("state");
        String clientId = request.getQueryString("client_id");
        String useripAddress = request.remoteAddress();
        Boolean isUserAuthenticate = false;
        String previousNounce = "";
        // PwcAuthUsersDAO pwcAuthUsersDao = new PwcAuthUsersDAOImpl();
        try {

            Cookie checkStateCookie = request.cookie("NOUNCE_STATE");
            if (checkStateCookie != null) {
                previousNounce = checkStateCookie.value();
            }

            log.log(Level.FINE, "Reached to sso callback");
            System.out.println("Reached to sso callback");
            log.log(Level.FINE, "Matching previously saved state : '" + previousNounce
                    + "', with the state sent from authorization: '" + state + "'");
            System.out.println("Matching previously saved state : '" + previousNounce
                    + "', with the state sent from authorization: '" + state + "'");
            if (state.equals(previousNounce)) {
                log.log(Level.FINE, "state matched with previous state");
                System.out.println("state matched with previous state");
                // below propery is important to make http request and receive response as
                // TLSv1.2 is not enabled by default in java 1.7.0_80
                java.lang.System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
                AccessTokenResponse accessTokenResponse = getPwcAccessToken(code, ConfigParams.getSsoPwcClientId(),
                        ConfigParams.getSsoPwcClientSecret(), ConfigParams.getSsoPwcRedirectUrl());
                if (accessTokenResponse != null) {
                    log.log(Level.FINE, "Received Access Token from pwc: ");
                    System.out.println("Received Access Token from pwc: ");
                    // System.out.println(accessTokenResponse.access_token);
                    PwcUserInfoResponse pwcUserInfo = getPwcUserInfo(accessTokenResponse.access_token);
                    log.log(Level.FINE, "user response : " + pwcUserInfo.name);
                    System.out.println("user response : " + pwcUserInfo.name);
                    if (pwcUserInfo != null) {
                        log.log(Level.FINE, "received userinfo with accesstoken");
                        System.out.println("received userinfo with accesstoken");
                        // Users usrinfo = Users.findActiveByEmail(jpaApi,pwcUserInfo.email);

                        // saving the access token and user info in pwc_auth_user table
                        PwcAuthUsers pwcAuthUser = new PwcAuthUsers();
                        pwcAuthUser.setPwcAccessToken(accessTokenResponse.access_token);
                        pwcAuthUser.setPwcRefreshToken(accessTokenResponse.refresh_token);
                        pwcAuthUser.setPwcScope(accessTokenResponse.scope);
                        pwcAuthUser.setPwcIdToken(accessTokenResponse.id_token);
                        pwcAuthUser.setPwcTokenType(accessTokenResponse.token_type);
                        pwcAuthUser.setPwcAccessTokenExpire(accessTokenResponse.expires_in);
                        pwcAuthUser.setPwcUid(pwcUserInfo.uid);
                        pwcAuthUser.setPwcGivenName(pwcUserInfo.given_name);
                        pwcAuthUser.setPwcFamilyName(pwcUserInfo.family_name);
                        pwcAuthUser.setPwcName(pwcUserInfo.name);
                        pwcAuthUser.setPwcPreferredMail(pwcUserInfo.preferredMail);
                        pwcAuthUser.setPwcEmail(pwcUserInfo.email);
                        pwcAuthUser.setPwcSub(pwcUserInfo.sub);
                        pwcAuthUser.setPwcRealM(pwcUserInfo.realm);
                        pwcAuthUser.setPwcAud(pwcUserInfo.aud);
                        pwcAuthUser.setPwcUpn(pwcUserInfo.upn);
                        log.log(Level.FINE, "Access TOken :" + pwcAuthUser.getPwcAccessToken());
                        System.out.println("Access TOken :" + pwcAuthUser.getPwcAccessToken());
                        log.log(Level.FINE, "Refresh Token : " + pwcAuthUser.getRefreshToken());
                        System.out.println("Refresh Token : " + pwcAuthUser.getRefreshToken());
                        log.log(Level.FINE, "Scope : " + pwcAuthUser.getPwcScope());
                        System.out.println("Scope : " + pwcAuthUser.getPwcScope());
                        log.log(Level.FINE, "TOken Id : " + pwcAuthUser.getPwcIdToken());
                        System.out.println("TOken Id : " + pwcAuthUser.getPwcIdToken());
                        log.log(Level.FINE, "Token Type : " + pwcAuthUser.getPwcTokenType());
                        System.out.println("Token Type : " + pwcAuthUser.getPwcTokenType());
                        log.log(Level.FINE, "Access token expire : " + pwcAuthUser.getPwcAccessTokenExpire());
                        System.out.println("Access token expire : " + pwcAuthUser.getPwcAccessTokenExpire());
                        log.log(Level.FINE, "user UID : " + pwcAuthUser.getPwcUid());
                        System.out.println("user UID : " + pwcAuthUser.getPwcUid());
                        log.log(Level.FINE, "User Given Name : " + pwcAuthUser.getPwcGivenName());
                        System.out.println("User Given Name : " + pwcAuthUser.getPwcGivenName());
                        log.log(Level.FINE, "User Family Name : " + pwcAuthUser.getPwcFamilyName());
                        System.out.println("User Family Name : " + pwcAuthUser.getPwcFamilyName());
                        log.log(Level.FINE, "User Name : " + pwcAuthUser.getPwcName());
                        System.out.println("User Name : " + pwcAuthUser.getPwcName());
                        log.log(Level.FINE, "User Preferred Mail : " + pwcAuthUser.getPwcPreferredMail());
                        System.out.println("User Preferred Mail : " + pwcAuthUser.getPwcPreferredMail());
                        log.log(Level.FINE, "User Email : " + pwcAuthUser.getPwcEmail());
                        System.out.println("User Email : " + pwcAuthUser.getPwcEmail());
                        log.log(Level.FINE, "User Sub :" + pwcAuthUser.getPwcSub());
                        System.out.println("User Sub :" + pwcAuthUser.getPwcSub());
                        log.log(Level.FINE, "User realm : " + pwcAuthUser.getPwcRealM());
                        System.out.println("User realm : " + pwcAuthUser.getPwcRealM());
                        log.log(Level.FINE, "User AUD : " + pwcAuthUser.getPwcAud());
                        System.out.println("User AUD : " + pwcAuthUser.getPwcAud());
                        log.log(Level.FINE, "User UPN : " + pwcAuthUser.getPwcUpn());
                        System.out.println("User UPN : " + pwcAuthUser.getPwcUpn());

                        // checking if pwcemail and user email is already added in pwc_auth_user table
                        List<PwcAuthUsers> checkExist = pwcAuthUser.getUserByPwcEmail(entityManager,
                                pwcUserInfo.preferredMail);

                        if (checkExist == null) {
                            entityTransaction.begin();
                            genericDAO.saveOrUpdate(pwcAuthUser, null, entityManager);
                            entityTransaction.commit();
                            log.log(Level.FINE, "Added pwc User email " + pwcAuthUser.getPwcEmail());
                            System.out.println("Added pwc User email " + pwcAuthUser.getPwcEmail());
                        } else {
                            for (PwcAuthUsers pwcUser : checkExist) {
                                pwcAuthUser.setId(pwcUser.getId());
                                pwcAuthUser.setUserEmail(pwcUser.getUserEmail());
                                pwcAuthUser.setPwcEmail(pwcUser.getPwcEmail());
                                entityTransaction.begin();
                                genericDAO.saveOrUpdate(pwcAuthUser, null, entityManager);
                                entityTransaction.commit();
                                String logMsg = "updated pwc user email " + pwcAuthUser.getPwcEmail()
                                        + " into the pwc_auth_user table with id " + pwcAuthUser.getId();
                                log.log(Level.FINE, logMsg);
                                System.out.println(logMsg);
                            }
                        }

                        isUserAuthenticate = true;
                        System.out.println("");

                        Cookie checkAuthEmailCookie = request.cookie("authEmail");
                        if (null != checkAuthEmailCookie) {
                            CookieUtils.discardCookie("authEmail");
                        }
                        Cookie cookie = Cookie.builder("authEmail", pwcUserInfo.preferredMail).build();
                    }
                } else {
                    log.log(Level.SEVERE, "Not received accesstoken!");
                    System.out.println("Not received accesstoken!");

                }

            } else {
                log.log(Level.FINE,
                        "The state not matched with previous state, It looks like that the authorization is not happening from pwc server");
                System.out.println(
                        "The state not matched with previous state, It looks like that the authorization is not happening from pwc server");
            }
        } catch (Exception ex) {
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            log.log(Level.SEVERE, "Error on /pwc/callback : " + ex.getMessage());
            System.out.println("Error on /pwc/callback : " + ex.getMessage());
        }
        return Results.ok(ssoCallback.render(code, iss, state, clientId, previousNounce, isUserAuthenticate));
    }

    public static AccessTokenResponse getPwcAccessToken(String authCode, String pwcClientId, String pwcClientSecret,
            String pwcRedirectUrl) {
        AccessTokenResponse accessTokenRes = null;
        try {
            log.log(Level.FINE, "Attempting to get access token from pwc endpoint :" + ConfigParams.getSsoPwcApiHost()
                    + "/access_token");
            log.log(Level.FINE, "posting parameter:");
            log.log(Level.FINE, "code= " + authCode);
            log.log(Level.FINE, "client_id=" + pwcClientId);
            log.log(Level.FINE, "client_secret=" + pwcClientSecret);
            log.log(Level.FINE, "redirect_uri=" + pwcRedirectUrl);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("code=");
            stringBuilder.append(authCode);
            stringBuilder.append("&client_id=");
            stringBuilder.append(pwcClientId);
            stringBuilder.append("&client_secret=");
            stringBuilder.append(pwcClientSecret);
            stringBuilder.append("&redirect_uri=");
            stringBuilder.append(pwcRedirectUrl);
            stringBuilder.append("&grant_type=authorization_code");
            String buildPostData = stringBuilder.toString();
            byte[] postData = buildPostData.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            HttpsURLConnection con = (HttpsURLConnection) new URL(ConfigParams.getSsoPwcApiHost() + "/access_token")
                    .openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            con.getOutputStream().write(postData);

            con.connect();
            int resCode = con.getResponseCode();
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            if (100 <= resCode && resCode <= 399) {
                System.out.println("reaching on 200");
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
            }
            ObjectMapper mapper = new ObjectMapper();
            accessTokenRes = mapper.readValue(sb.toString(), AccessTokenResponse.class);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return accessTokenRes;
    }

    public static PwcUserInfoResponse getPwcUserInfo(String accessToken) {
        PwcUserInfoResponse pwcUserInfo = null;
        try {
            log.log(Level.FINE, "Attempting to get user info from endpoint: " + ConfigParams.getSsoPwcApiHost()
                    + "/userinfo?access_token=" + accessToken);

            HttpsURLConnection con = (HttpsURLConnection) new URL(
                    ConfigParams.getSsoPwcApiHost() + "/userinfo?access_token=" + accessToken).openConnection();
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Authorization", "Bearer " + accessToken);
            con.connect();
            int resCode = con.getResponseCode();
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            if (100 <= resCode && resCode <= 399) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
            }
            ObjectMapper mapper = new ObjectMapper();
            pwcUserInfo = mapper.readValue(sb.toString(), PwcUserInfoResponse.class);

        } catch (Exception e) {
            log.log(Level.FINE, e.getMessage());
        }
        return pwcUserInfo;
    }

    @Transactional
    public Result signIn(Http.Request request) throws ClassNotFoundException, IOException {
        Organization orgn = null;
        CookieUtils.discardCookie("user");
        CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
        String email = null;
        Http.Session session = request.session();
        Cookie checkEmailCookie = request.cookie("authEmail");
        System.out.println("SK >>>>>>>>>>>> Inside SignIn" + checkEmailCookie);
        if (checkEmailCookie != null) {
            email = checkEmailCookie.value();
        }
        log.log(Level.FINE, "Received email from authEmail cookie : " + email);
        System.out.println("SK >>>>>>>>>>>> Inside SignIn email" + email);
        try {
            Users usrinfo = Users.findActiveByEmail(email);
            System.out.println("SK >>>>>>>>>>>> Inside SignIn usrinfo" + usrinfo);
            if (usrinfo != null) {
                String fullName = null;
                String role = null;
                String allowedProcurement = null;
                // String pwdchanged = session.getOptional("pwdchanged").orElse("");
                String pwdchanged = IdosConstants.PUBLICK;
                session.removing();
                log.log(Level.FINE, " ===3================");
                return Results.ok(registration.render(fullName, email, role, orgn, allowedProcurement, pwdchanged,
                        ConfigParams.getInstance()));
                // return
                // Results.ok(loginToIdos.render(fullName,email,role,orgn,allowedProcurement,pwdchanged));
                // }
            } else {
                /*
                 * Http.Cookie cookie = request.cookie("user");
                 * String userCookie = "";
                 * if (cookie != null) {
                 * userCookie = cookie.value();
                 * }
                 * if (userCookie != null && !userCookie.equals("")) {
                 * session.adding("email", userCookie);
                 * usrinfo = Users.findActiveByEmail(email);
                 * if(usrinfo == null){
                 * return unauthorized();
                 * }
                 * List<UsersRoles> userRoles = usrinfo.getUserRoles();
                 * String role = "";
                 * for (UsersRoles roles : userRoles) {
                 * role += roles.getRole().getName() + ",";
                 * }
                 * role = role.substring(0, role.length() - 1);
                 * orgn = usrinfo.getOrganization();
                 * String allowedProcurement =
                 * String.valueOf(usrinfo.getAllowedProcurementRequest());
                 * System.out.
                 * println("inside signIn method, calling config.render when userinfo null, else part"
                 * );
                 * log.log(Level.FINE, " ==========4=========" + usrinfo.getFullName() +
                 * usrinfo.getEmail() + role + orgn + allowedProcurement);
                 * return Results.ok(config.render(usrinfo.getFullName(), usrinfo.getEmail(),
                 * role, orgn, allowedProcurement));
                 * } else {
                 */
                String fullName = null;
                String role = null;
                String allowedProcurement = null;
                // String pwdchanged = session.getOptional("pwdchanged").orElse("");
                String pwdchanged = IdosConstants.PUBLICK;
                session.removing();
                log.log(Level.FINE, " ==========5=========" + pwdchanged);
                // return
                // Results.ok(loginToIdos.render(fullName,email,role,orgn,allowedProcurement,pwdchanged));
                return Results.ok(registration.render(fullName, email, role, orgn, allowedProcurement, pwdchanged,
                        ConfigParams.getInstance()));
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
    public Result vendCustAccount(Request request) {
        log.log(Level.FINE, "=========Start==============");

        String fullName = null;
        String email = null;
        String role = null;
        String allowedProcurement = null;
        Organization orgn = null;
        String pwdchanged = null;
        try {
            Cookie cookie = request.cookie("vendorCustomer");
            if (null != cookie) {
                CookieUtils.discardCookie("vendorCustomer");
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "Vendor Customer Account Email",
                    "Vendor Customer Account Configuration", Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(registration.render(fullName, email, role, orgn, allowedProcurement, pwdchanged,
                ConfigParams.getInstance()));
    }

    @Transactional
    public Result vendcustconfiguration() {
        log.log(Level.FINE, "=========Start==============");

        String fullName = null;
        String pageemail = null;
        String role = null;
        String allowedProcurement = null;
        Organization orgn = null;
        try {
            return Results.ok(vendorAccount.render(fullName, pageemail, role, orgn, allowedProcurement,
                    ConfigParams.getInstance()));
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "Vendor Customer Configuration Email",
                    "Vendor Customer Configuration Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    @Transactional
    public Result register(String source, Http.Request request) throws ClassNotFoundException, IOException {
        log.log(Level.FINE, "=========Start==============");

        String fullName = null;
        String email = source;
        String role = null;
        String allowedProcurement = null;
        Organization orgn = null;
        Http.Session session = request.session();
        String pwdchanged = session.getOptional("pwdchanged").orElse("");
        session.removing();
        return Results.ok(registration.render(fullName, email, role, orgn, allowedProcurement, pwdchanged,
                ConfigParams.getInstance()));
        // log.log(Level.FINE, "=========Start==============");
        // return
        // Results.ok(register.render(fullName,email,role,orgn,allowedProcurement,pwdchanged));
    }

    @Transactional
    public Result signUp(Http.Request request) throws ClassNotFoundException, IOException {
        log.log(Level.FINE, "=========Start==============");

        String fullName = null;
        String email = null;
        String role = null;
        String allowedProcurement = null;
        Organization orgn = null;
        Http.Session session = request.session();
        System.out.print(">>>>>>>>>>>>>>>>>session" + session);
        String pwdchanged = session.getOptional("pwdchanged").orElse("");
        session.removing();
        return Results.ok(registration.render(fullName, email, role, orgn, allowedProcurement, pwdchanged,
                ConfigParams.getInstance()));
        // log.log(Level.FINE, "=========Start==============");
        // return
        // Results.ok(register.render(fullName,email,role,orgn,allowedProcurement,pwdchanged));
    }

    @Transactional
    public Result generalFaq() throws ClassNotFoundException, IOException {
        if (IdosConstants.PWC.equalsIgnoreCase(ConfigParams.getInstance().getCompanyOwner())) {
            return Results.ok(pwcfaq.render(ConfigParams.getInstance()));
        } else {
            return Results.ok(generalFaq.render(ConfigParams.getInstance()));
        }
    }

    @Transactional
    public Result setupFaq() throws ClassNotFoundException, IOException {
        return Results.ok(setupFAQ.render(ConfigParams.getInstance()));
    }

    @Transactional
    public Result configuration(Request request) {
        Http.Session session = request.session();
        String email = session.getOptional("csrfToken").orElse("");
        System.out.println("SK >>>>>>>>>>>> Session value " + email);
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
                    } else {

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
                System.out.println(
                        "Inside Application configuration, where userinfo is NOT NULL, so calling config.scala to enter");
                log.log(Level.FINE, "=========End3==============");
                return Results.ok(config.render(usrinfo.getFullName(), usrinfo.getEmail(), role, orgn,
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

    @Transactional
    public Result addOrganization(Request request) {
        log.log(Level.FINE, ">>>> Start");
        ObjectNode result = Json.newObject();
        Users user = StaticController.getUserInfo(request);
        // EntityManager entityManager= jpaApi.em();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        try {
            String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
            if (ipAddress == null) {
                ipAddress = request.remoteAddress();
            }

            Organization newOrg = new Organization();
            Branch newbnch = new Branch();
            Users newUsers = new Users();
            BranchCashCount bcashcnt = new BranchCashCount();
            UsersRoles newuserroles = new UsersRoles();
            JsonNode json = request.body().asJson();
            ObjectNode results = Json.newObject();
            String corporateEmail = json.findValue("companyEmailId").asText().toLowerCase();
            if (ConfigParams.getInstance().getInstanceDeploymentMode() == 1) {
                // Modified by Manali
                String emailQ = "select obj from Users obj WHERE obj.email ='" + corporateEmail
                        + "' and obj.presentStatus=1";
                List<Users> usersList = genericDAO.executeSimpleQuery(emailQ, entityManager);
                if (usersList.size() > 0) {
                    throw new IDOSException(IdosConstants.BUSINESS_EXCEPTION, IdosConstants.BUSINESS_EXCEPTION,
                            "Email is already exist!", "Use different email address");
                }
            }
            ArrayNode can = results.putArray("companyAccount");
            ArrayNode ban = results.putArray("branchAccount");
            ArrayNode uan = results.putArray("userAccount");
            ArrayNode uran = results.putArray("userRoleAccount");
            ArrayNode incomepartan = results.putArray("userIncomeParticularAccount");
            Integer companyId = json.findValue("companyId").asText() != null ? json.findValue("companyId").asInt()
                    : null;
            String orgName = json.findValue("companyName").asText();
            String adminPassword = json.findValue("adminPwd").asText();
            String personName = json.findValue("contactName").asText();
            String registrationSource = json.findValue("registrationSource").asText();
            String orgPhoneNumber = json.findValue("adminPhonenumber").asText();
            String orgWebsite = json.findValue("adminWebsite").asText();
            Integer userMode = (json.findValue("userMode") == null || "".equals(json.findValue("userMode").asText()))
                    ? 0
                    : json.findValue("userMode").asInt();
            newOrg.setName(orgName);
            newOrg.setCompanyHasChartOfAccounts(0);
            newOrg.setCorporateMail(corporateEmail);
            newOrg.setTrialStartDate(Calendar.getInstance().getTime());
            newOrg.setPersonName(personName);
            newOrg.setRegistrationSource(registrationSource);
            newOrg.setRegisteredPhoneNumber(orgPhoneNumber);
            newOrg.setWebUrl(orgWebsite);
            if (ConfigParams.getInstance().getDeployMode() != null) {
                if (ConfigParams.getInstance().getDeployMode().equals(IdosConstants.DeployMode.SINGLE.name())) {
                    newOrg.setUserMode(1);
                } else if (ConfigParams.getInstance().getDeployMode().equals(IdosConstants.DeployMode.MULTI.name())) {
                    newOrg.setUserMode(0);
                } else {
                    newOrg.setUserMode(userMode);
                }
            } else {
                newOrg.setUserMode(0);
            }
            Calendar dtcal = Calendar.getInstance();
            dtcal.add(Calendar.DATE, 15);
            newOrg.setTrialEndDate(dtcal.getTime());
            Calendar cal = Calendar.getInstance();
            cal.setTime(newOrg.getTrialEndDate());
            int day = cal.get(Calendar.DAY_OF_MONTH);
            newOrg.setPayConsiderationDayOfMonth(day);
            if (companyId != 0 && companyId != null) {
                newOrg.setCompanyId(companyId);
            }
            entityTransaction.begin();
            orgcrud.save(user, newOrg, entityManager);
            if (companyId == 0) {
                Query query = entityManager.createQuery("update Organization set companyId = ?1 where id=?2");
                query.setParameter(1, newOrg.getId().intValue());
                query.setParameter(2, newOrg.getId());
                query.executeUpdate();
            }
            ObjectNode crow = Json.newObject();
            crow.put("cname", orgName);
            crow.put("ccorpEmail", corporateEmail);
            can.add(crow);
            String action = "Create New Account Company";
            newbnch.setName(orgName + "-hq");
            newbnch.setIsHeadQuarter(1);
            newbnch.setGstin(null);
            Organization bOrg = Organization.findById(newOrg.getId());
            newbnch.setOrganization(bOrg);
            bnchcrud.save(user, newbnch, entityManager);
            ObjectNode brow = Json.newObject();
            brow.put("bname", orgName + "-hq");
            brow.put("isHeadQuarter", 1);
            brow.put("bOrg", bOrg.getName());
            ban.add(brow);
            String baction = "Create New Account Branch";
            String pwd = PasswordUtil.encrypt(adminPassword);
            final String companyowner = ConfigFactory.load().getString("company.owner");
            if (companyowner.equals("KARVY")) {
                String encryptedString = AESShaEncryptionKARVY.Encrypt(adminPassword);
                newUsers.setPasswordForKarvy(encryptedString);
            }
            newUsers.setPassword(pwd);
            newUsers.setFullName(orgName);
            newUsers.setEmail(corporateEmail);
            if (ConfigParams.getInstance().getInstanceDeploymentMode() == IdosConstants.INSTANCE_API_MODE) {
                newUsers.setPresentStatus(1);
            } else {
                newUsers.setPresentStatus(0);
            }
            newUsers.setAllowedProcurementRequest(0);
            Branch uBnch = Branch.findById(newbnch.getId());
            newUsers.setBranch(uBnch);
            newUsers.setOrganization(bOrg);
            usercrud.save(user, newUsers, entityManager);

            if (companyowner.equals("KARVY")) {
                log.log(Level.INFO, "inside karvy");
                ObjectNode karvyRow = Json.newObject();

                karvyRow.put("EmailID", corporateEmail);
                karvyRow.put("Password", adminPassword);
                karvyRow.put("MobileNo", orgPhoneNumber);
                karvyRow.put("DomainID", "Tax payer");

                KarvyAuthorization.signupForKarvyGST(karvyRow);
            }
            bcashcnt.setBranch(newbnch);
            bcashcnt.setOrganization(bOrg);
            bcashcnt.setDate(Calendar.getInstance().getTime());
            genericDAO.saveOrUpdate(bcashcnt, newUsers, entityManager);
            ObjectNode urow = Json.newObject();
            urow.put("upwd", pwd);
            urow.put("ufullName", orgName);
            urow.put("uEmail", corporateEmail);
            urow.put("uPresentStatus", 0);
            urow.put("uBranch", uBnch.getName());
            urow.put("uOrg", bOrg.getName());
            uan.add(urow);
            String uaction = "Create New Account Admin User";
            auditDAO.saveAuditLogs(uaction, newUsers, newUsers.getId(), Users.class, ipAddress, urow.toString(),
                    entityManager);
            auditDAO.saveAuditLogs(baction, newUsers, newbnch.getId(), Branch.class, ipAddress, brow.toString(),
                    entityManager);
            auditDAO.saveAuditLogs(action, newUsers, newOrg.getId(), Organization.class, ipAddress, crow.toString(),
                    entityManager);

            String sbquery = "select obj from Role obj WHERE obj.name='MASTER ADMIN' and obj.presentStatus=1";
            if (companyId == 0) {
                sbquery = "select obj from Role obj WHERE obj.name='SUPER ADMIN' and obj.presentStatus=1";
            }
            List<Role> role = genericDAO.executeSimpleQuery(sbquery, entityManager);
            newuserroles.setRole(role.get(0));
            newuserroles.setUser(newUsers);
            newuserroles.setBranch(Branch.findById(newbnch.getId()));
            newuserroles.setOrganization(Organization.findById(newOrg.getId()));
            userrolecrud.save(user, newuserroles, entityManager);

            ObjectNode urrow = Json.newObject();
            urrow.put("urrole", role.get(0).getName());
            urrow.put("uruser", newUsers.getEmail());
            urrow.put("uBranch", uBnch.getName());
            urrow.put("uOrg", bOrg.getName());
            uran.add(urrow);
            String uraction = "Create New Account User Role";
            auditDAO.saveAuditLogs(uraction, newUsers, newuserroles.getId(), UsersRoles.class, ipAddress,
                    urrow.toString(), entityManager);
            Particulars incomePart = new Particulars();
            incomePart.setName("Incomes");
            incomePart.setAccountCode(1000000000000000000L);
            incomePart.setCreatedBy(newUsers);
            incomePart.setOrganization(newOrg);
            incomePart.setAccountCodeHirarchy("/");
            ObjectNode incomePartRow = Json.newObject();
            urrow.put("partName", incomePart.getName());
            urrow.put("accountCode", incomePart.getAccountCode());
            urrow.put("createdBy", incomePart.getCreatedBy().getEmail());
            urrow.put("organization", newOrg.getName());
            incomepartan.add(incomePartRow);
            partcrud.save(user, incomePart, entityManager);
            auditDAO.saveAuditLogs("Created Default Income Particular", newUsers, newUsers.getId(), Particulars.class,
                    ipAddress, incomePartRow.toString(), entityManager);

            Particulars expensePart = new Particulars();
            expensePart.setName("Expenses");
            expensePart.setAccountCode(2000000000000000000L);
            expensePart.setCreatedBy(newUsers);
            expensePart.setOrganization(newOrg);
            expensePart.setAccountCodeHirarchy("/");
            ObjectNode expensePartRow = Json.newObject();
            urrow.put("partName", expensePart.getName());
            urrow.put("accountCode", expensePart.getAccountCode());
            urrow.put("createdBy", expensePart.getCreatedBy().getEmail());
            urrow.put("organization", newOrg.getName());
            partcrud.save(user, expensePart, entityManager);
            auditDAO.saveAuditLogs("Created Default Expense Particular", newUsers, newUsers.getId(), Particulars.class,
                    ipAddress, expensePartRow.toString(), entityManager);
            /*
             * Specifics expenseDiscount=new Specifics();
             * expenseDiscount.setParticularsId(expensePart);
             * expenseDiscount.setName("Discounts(Registered Customers)");
             * expenseDiscount.setOrganization(neworg);
             * expenseDiscount.setAccountCode(2100000000000000000L);
             * expenseDiscount.setAccountCodeHirarchy("/2000000000000000000/");
             * genericDAO.saveOrUpdate(expenseDiscount, newUsers, entityManager);
             * Specifics expenseDiscountWc=new Specifics();
             * expenseDiscountWc.setParticularsId(expensePart);
             * expenseDiscountWc.setName("Discounts(Walkin Customers)");
             * expenseDiscountWc.setOrganization(neworg);
             * expenseDiscountWc.setAccountCode(2200000000000000000L);
             * expenseDiscountWc.setAccountCodeHirarchy("/2000000000000000000/");
             * genericDAO.saveOrUpdate(expenseDiscount, newUsers, entityManager);
             */
            Particulars assetsPart = new Particulars();
            assetsPart.setName("Assets");
            assetsPart.setAccountCode(3000000000000000000L);
            assetsPart.setCreatedBy(newUsers);
            assetsPart.setOrganization(newOrg);
            assetsPart.setAccountCodeHirarchy("/");
            ObjectNode assetsPartRow = Json.newObject();
            urrow.put("partName", assetsPart.getName());
            urrow.put("accountCode", assetsPart.getAccountCode());
            urrow.put("createdBy", assetsPart.getCreatedBy().getEmail());
            urrow.put("organization", newOrg.getName());
            partcrud.save(user, assetsPart, entityManager);
            auditDAO.saveAuditLogs("Created Default Assets Particular", newUsers, newUsers.getId(), Particulars.class,
                    ipAddress, assetsPartRow.toString(), entityManager);
            
            //// Current Assets default coa creation
            //Specifics currentAssets=new Specifics();
            //currentAssets.setParticularsId(assetsPart);
            //currentAssets.setName("Current Assets");
            //currentAssets.setOrganization(newOrg);
            //currentAssets.setAccountCode(3100000000000000000L);
            //currentAssets.setAccountCodeHirarchy("/3000000000000000000/");
            //currentAssets.setPresentStatus(1);
            //genericDAO.saveOrUpdate(currentAssets, newUsers, entityManager);
            
            //// Cash and Bank default coa creation
            //Specifics cashNBank=new Specifics();
            //cashNBank.setParticularsId(assetsPart);
            //cashNBank.setName("Cash and Bank");
            //cashNBank.setOrganization(newOrg);
            //cashNBank.setAccountCode(3110000000000000000L);
            //cashNBank.setAccountCodeHirarchy("/3000000000000000000/3100000000000000000/");
            //cashNBank.setPresentStatus(1);
            //genericDAO.saveOrUpdate(cashNBank, newUsers, entityManager);

            //// Bank Balances default coa creation
            //Specifics bankBalances=new Specifics();
            //bankBalances.setParticularsId(assetsPart);
            //bankBalances.setName("Bank Balances");
            //bankBalances.setOrganization(newOrg);
            //bankBalances.setAccountCode(3111000000000000000L);
            //bankBalances.setAccountCodeHirarchy("/3000000000000000000/3100000000000000000/3110000000000000000/");
            //bankBalances.setPresentStatus(1);
            //bankBalances.setIdentificationForDataValid("4");
            //genericDAO.saveOrUpdate(bankBalances, newUsers, entityManager);
            /*
             * Specifics employeeAdvancesAssets=new Specifics();
             * employeeAdvancesAssets.setParticularsId(assetsPart);
             * employeeAdvancesAssets.setName("Current Assets");
             * employeeAdvancesAssets.setOrganization(neworg);
             * employeeAdvancesAssets.setAccountCode(3100000000000000000L);
             * employeeAdvancesAssets.setAccountCodeHirarchy("/3000000000000000000/");
             * genericDAO.saveOrUpdate(employeeAdvancesAssets, newUsers, entityManager);
             * 
             * Specifics advancePaidToVendorAssets=new Specifics();
             * advancePaidToVendorAssets.setParticularsId(assetsPart);
             * advancePaidToVendorAssets.setName("Advance Paid To Vendors");
             * advancePaidToVendorAssets.setOrganization(neworg);
             * advancePaidToVendorAssets.setAccountCode(3200000000000000000L);
             * advancePaidToVendorAssets.setAccountCodeHirarchy("/3000000000000000000/");
             * genericDAO.saveOrUpdate(advancePaidToVendorAssets, newUsers, entityManager);
             * 
             * Specifics cashAccountAssets=new Specifics();
             * cashAccountAssets.setParticularsId(assetsPart);
             * cashAccountAssets.setName("Cash Account");
             * cashAccountAssets.setOrganization(neworg);
             * cashAccountAssets.setAccountCode(3300000000000000000L);
             * cashAccountAssets.setAccountCodeHirarchy("/3000000000000000000/");
             * genericDAO.saveOrUpdate(cashAccountAssets, newUsers, entityManager);
             * 
             * Specifics bankAccountAssets=new Specifics();
             * bankAccountAssets.setParticularsId(assetsPart);
             * bankAccountAssets.setName("Bank Account");
             * bankAccountAssets.setOrganization(neworg);
             * bankAccountAssets.setAccountCode(3400000000000000000L);
             * bankAccountAssets.setAccountCodeHirarchy("/3000000000000000000/");
             * genericDAO.saveOrUpdate(bankAccountAssets, newUsers, entityManager);
             * 
             * Specifics accountsReceivableCreditSalesCustomersAssets=new Specifics();
             * accountsReceivableCreditSalesCustomersAssets.setParticularsId(assetsPart);
             * accountsReceivableCreditSalesCustomersAssets.
             * setName("Account Receivable(credit sales / customers)");
             * accountsReceivableCreditSalesCustomersAssets.setAccountCode(
             * 3500000000000000000L);
             * accountsReceivableCreditSalesCustomersAssets.setAccountCodeHirarchy(
             * "/3000000000000000000/");
             * genericDAO.saveOrUpdate(accountsReceivableCreditSalesCustomersAssets,
             * newUsers, entityManager);
             * 
             * Specifics advancesAssets=new Specifics();
             * advancesAssets.setParticularsId(assetsPart);
             * advancesAssets.setName("Advances");
             * advancesAssets.setOrganization(neworg);
             * advancesAssets.setAccountCode(3110000000000000000L);
             * advancesAssets.setAccountCodeHirarchy(
             * "/3000000000000000000/3100000000000000000/");
             * advancesAssets.setParentSpecifics(employeeAdvancesAssets);
             * 
             * Specifics travelAdvancesAssets=new Specifics();
             * travelAdvancesAssets.setParticularsId(assetsPart);
             * travelAdvancesAssets.setName("Travel advances");
             * travelAdvancesAssets.setOrganization(neworg);
             * travelAdvancesAssets.setAccountCode(3111000000000000000L);
             * travelAdvancesAssets.setAccountCodeHirarchy(
             * "/3000000000000000000/3100000000000000000/3110000000000000000/");
             * travelAdvancesAssets.setParentSpecifics(employeeAdvancesAssets);
             * genericDAO.saveOrUpdate(travelAdvancesAssets, newUsers, entityManager);
             * 
             * Specifics advanceForExpensesAssets=new Specifics();
             * advanceForExpensesAssets.setParticularsId(assetsPart);
             * advanceForExpensesAssets.setName("Advance for expenses");
             * advanceForExpensesAssets.setOrganization(neworg);
             * advanceForExpensesAssets.setAccountCode(3112000000000000000L);
             * advanceForExpensesAssets.setAccountCodeHirarchy(
             * "/3000000000000000000/3100000000000000000/3110000000000000000/");
             * advanceForExpensesAssets.setParentSpecifics(employeeAdvancesAssets);
             * genericDAO.saveOrUpdate(advanceForExpensesAssets, newUsers, entityManager);
             * 
             * //pay special adjustment default assets creation start
             * Specifics paySpecialAdjustments=new Specifics();
             * paySpecialAdjustments.setParticularsId(assetsPart);
             * paySpecialAdjustments.setName("Pay special adjustment");
             * paySpecialAdjustments.setOrganization(neworg);
             * paySpecialAdjustments.setAccountCode(3120000000000000000L);
             * cashAccountAssets.setAccountCodeHirarchy(
             * "/3000000000000000000/3100000000000000000/");
             * genericDAO.saveOrUpdate(paySpecialAdjustments, newUsers, entityManager);
             * 
             * //pay special adjustment default assets creation end
             * //Withholding tax receivable creation start
             * Specifics withheldingtax=new Specifics();
             * withheldingtax.setParticularsId(assetsPart);
             * withheldingtax.setName("Withholding tax Receivable");
             * withheldingtax.setOrganization(neworg);
             * withheldingtax.setAccountCode(3130000000000000000L);
             * withheldingtax.setAccountCodeHirarchy(
             * "/3000000000000000000/3100000000000000000/");
             * genericDAO.saveOrUpdate(withheldingtax, newUsers, entityManager);
             */
            // Withholding tax receivable creation end
            Particulars liabilitiesPart = new Particulars();
            liabilitiesPart.setName("Liabilities");
            liabilitiesPart.setAccountCode(4000000000000000000L);
            liabilitiesPart.setCreatedBy(newUsers);
            liabilitiesPart.setOrganization(newOrg);
            liabilitiesPart.setAccountCodeHirarchy("/");
            ObjectNode liabilitiesPartRow = Json.newObject();
            urrow.put("partName", liabilitiesPart.getName());
            urrow.put("accountCode", liabilitiesPart.getAccountCode());
            urrow.put("createdBy", liabilitiesPart.getCreatedBy().getEmail());
            urrow.put("organization", newOrg.getName());
            partcrud.save(user, liabilitiesPart, entityManager);
            PasswordHistory history = new PasswordHistory(newUsers.getEmail(), newUsers, newUsers.getOrganization(),
                    pwd);
            genericDAO.saveOrUpdate(history, newUsers, entityManager);
            auditDAO.saveAuditLogs("Created Default Liabilities Particular", newUsers, newUsers.getId(),
                    Particulars.class, ipAddress, liabilitiesPartRow.toString(), entityManager);

            // current liabilities default coa creation
            Specifics profitNLoss = new Specifics();
            profitNLoss.setParticularsId(liabilitiesPart);
            profitNLoss.setName("Profit and Loss");
            profitNLoss.setOrganization(newOrg);
            profitNLoss.setAccountCode(4100000000000000000L);
            profitNLoss.setAccountCodeHirarchy("/4000000000000000000/");
            profitNLoss.setIdentificationForDataValid("67");
            profitNLoss.setPresentStatus(1);
            genericDAO.saveOrUpdate(profitNLoss, newUsers, entityManager);
            
            //// Current Liabilities default coa creation
            //Specifics currentLiability=new Specifics();
            //currentLiability.setParticularsId(liabilitiesPart);
            //currentLiability.setName("Current Liabilities");
            //currentLiability.setOrganization(newOrg);
            //currentLiability.setAccountCode(4200000000000000000L);
            //currentLiability.setAccountCodeHirarchy("/4000000000000000000/");
            //currentLiability.setPresentStatus(1);
            //genericDAO.saveOrUpdate(currentLiability, newUsers, entityManager);

            //// Bank Liabilities default coa creation
            //Specifics bankAccountsLiability=new Specifics();
            //bankAccountsLiability.setParticularsId(liabilitiesPart);
            //bankAccountsLiability.setName("Bank Liabilities");
            //bankAccountsLiability.setOrganization(newOrg);
            //bankAccountsLiability.setAccountCode(4210000000000000000L);
            //bankAccountsLiability.setAccountCodeHirarchy("/4000000000000000000/4200000000000000000/");
            //bankAccountsLiability.setPresentStatus(1);
            //bankAccountsLiability.setIdentificationForDataValid("5");
            //genericDAO.saveOrUpdate(bankAccountsLiability, newUsers, entityManager);
            /* //Over Draft default coa creation
             * Specifics overDraftLiability=new Specifics();
             * overDraftLiability.setParticularsId(liabilitiesPart);
             * overDraftLiability.setName("Over draft");
             * overDraftLiability.setOrganization(neworg);
             * overDraftLiability.setAccountCode(4120000000000000000L);
             * overDraftLiability.setAccountCodeHirarchy(
             * "/4000000000000000000/4100000000000000000/");
             * genericDAO.saveOrUpdate(overDraftLiability, newUsers, entityManager);
             * //Cash credit default coa creation
             * Specifics cashCreditLiability=new Specifics();
             * cashCreditLiability.setParticularsId(liabilitiesPart);
             * cashCreditLiability.setName("Cash credit");
             * cashCreditLiability.setOrganization(neworg);
             * cashCreditLiability.setAccountCode(4130000000000000000L);
             * cashCreditLiability.setAccountCodeHirarchy(
             * "/4000000000000000000/4100000000000000000/");
             * genericDAO.saveOrUpdate(cashCreditLiability, newUsers, entityManager);
             * //Credit card default coa creation
             * Specifics creditCardLiability=new Specifics();
             * creditCardLiability.setParticularsId(liabilitiesPart);
             * creditCardLiability.setName("Credit card");
             * creditCardLiability.setOrganization(neworg);
             * creditCardLiability.setAccountCode(414000000000000000L);
             * creditCardLiability.setAccountCodeHirarchy(
             * "/4000000000000000000/4100000000000000000/");
             * genericDAO.saveOrUpdate(creditCardLiability, newUsers, entityManager);
             * //Loan default coa creation
             * Specifics loanLiability=new Specifics();
             * loanLiability.setParticularsId(liabilitiesPart);
             * loanLiability.setName("Loan");
             * loanLiability.setOrganization(neworg);
             * loanLiability.setAccountCode(4150000000000000000L);
             * loanLiability.setAccountCodeHirarchy(
             * "/4000000000000000000/4100000000000000000/");
             * genericDAO.saveOrUpdate(loanLiability, newUsers, entityManager);
             * //Receive special adjustment current liabilities creation
             * Specifics receivespecadjustment=new Specifics();
             * receivespecadjustment.setParticularsId(liabilitiesPart);
             * receivespecadjustment.setName("Receive special adjustment");
             * receivespecadjustment.setOrganization(neworg);
             * receivespecadjustment.setAccountCode(4160000000000000000L);
             * receivespecadjustment.setAccountCodeHirarchy(
             * "/4000000000000000000/4100000000000000000/");
             * genericDAO.saveOrUpdate(receivespecadjustment, newUsers, entityManager);
             * //Receive special adjustment current liabilities creation
             * //Withholding tax payable current liabilities creation
             * Specifics withheldingpayable=new Specifics();
             * withheldingpayable.setParticularsId(liabilitiesPart);
             * withheldingpayable.setName("Withholding tax payable");
             * withheldingpayable.setOrganization(neworg);
             * withheldingpayable.setAccountCode(4170000000000000000L);
             * withheldingpayable.setAccountCodeHirarchy(
             * "/4000000000000000000/4100000000000000000/");
             * genericDAO.saveOrUpdate(withheldingpayable, newUsers, entityManager);
             * //Withholding tax payable current liabilities creation
             * Specifics advanceFromCustomersLiability=new Specifics();
             * advanceFromCustomersLiability.setParticularsId(liabilitiesPart);
             * advanceFromCustomersLiability.setName("Advance From Customers");
             * advanceFromCustomersLiability.setOrganization(neworg);
             * advanceFromCustomersLiability.setAccountCode(4200000000000000000L);
             * advanceFromCustomersLiability.setAccountCodeHirarchy("/4000000000000000000/")
             * ;
             * genericDAO.saveOrUpdate(advanceFromCustomersLiability, newUsers,
             * entityManager);
             * 
             * Specifics accountsPayableCreditPurchaseVendorsLiability=new Specifics();
             * accountsPayableCreditPurchaseVendorsLiability.setParticularsId(
             * liabilitiesPart);
             * accountsPayableCreditPurchaseVendorsLiability.
             * setName("Accounts Payable (credit purchase / vendors)");
             * accountsPayableCreditPurchaseVendorsLiability.setOrganization(neworg);
             * accountsPayableCreditPurchaseVendorsLiability.setAccountCode(
             * 4300000000000000000L);
             * accountsPayableCreditPurchaseVendorsLiability.setAccountCodeHirarchy(
             * "/4000000000000000000/");
             * genericDAO.saveOrUpdate(accountsPayableCreditPurchaseVendorsLiability,
             * newUsers, entityManager);
             */
            StringBuilder sbr = new StringBuilder(
                    "select obj from IdosChannelPartnerAlloteOrganization obj where obj.organizationName=?1 and obj.email=?2 and obj.presentStatus=1");
            ArrayList inparams = new ArrayList();
            inparams.add(newOrg.getName());
            inparams.add(newOrg.getCorporateMail());
            List<IdosChannelPartnerAlloteOrganization> idosCpCommitmentList = genericDAO.queryWithParams(sbr.toString(),
                    entityManager, inparams);
            if (!idosCpCommitmentList.isEmpty() && idosCpCommitmentList.size() > 0) {
                for (IdosChannelPartnerAlloteOrganization idosCpCommitment : idosCpCommitmentList) {
                    IdosChannelPartnerCustomerOrganizationBranch newCpOrgBnch = new IdosChannelPartnerCustomerOrganizationBranch();
                    newCpOrgBnch.setCustomerOrganizatioNbRANCH(newbnch);
                    newCpOrgBnch.setCustomerOrganization(newOrg);
                    newCpOrgBnch.setIdosChannelPartner(idosCpCommitment.getChannelPartner());
                    genericDAO.saveOrUpdate(newCpOrgBnch, newUsers, entityManager);
                    idosCpCommitment.setCommitmentStatus("ALLOTED");
                    genericDAO.saveOrUpdate(idosCpCommitment, newUsers, entityManager);
                    // send mail to channel partner whose prospect is registering as idos customer
                    String location = "";
                    if (newbnch.getLocation() != null) {
                        location = newbnch.getLocation();
                    }
                    String cpBody = channelPartnerProspectConverted
                            .render(idosCpCommitment.getChannelPartner().getChannelPartnerName(),
                                    idosCpCommitment.getChannelPartner().getChannelPartnerEmail(), newOrg.getName(),
                                    newOrg.getCorporateMail(), newbnch.getName(), location, ConfigParams.getInstance())
                            .body();
                    final String cpusername = ConfigFactory.load().getString("smtpchannelsales.user");
                    String cpsubject = "Organization/Branch Registered Into Your Channel Partner Account With Idos";
                    Session cpsession = channelSalesSession;
                    mailTimer(cpBody, cpusername, cpsession,
                            idosCpCommitment.getChannelPartner().getChannelPartnerEmail(), null, cpsubject);
                }
            }
            String body1 = accountCreationLink.render(adminPassword, corporateEmail, ConfigParams.getInstance()).body();
            // final String username1 = ConfigFactory.load().getString("smtp.user");
            final String username = ConfigFactory.load().getString("smtp.user");
            String subject1 = "Activate Your Account";
            Session session1 = emailsession;
            mailTimer(body1, username, session1, corporateEmail, null, subject1);
            String body = accountCreation.render(adminPassword, corporateEmail, ConfigParams.getInstance()).body();

            String subject = "Account Created Successfully";
            Session session = emailsession;
            mailTimer(body, username, session, "alert@myidos.com", null, subject);
            String fileName = "IDOS-Easy_set_up_guide.pdf";
            String path = application.path().toString() + "/public/usersetup/" + fileName;
            String fileName1 = "IDOS_USER_GUIDELINES.pdf";
            String path1 = application.path().toString() + "/public/usersetup/" + fileName;
            File file = new File(path);
            File file1 = new File(path1);
            String attachmentSubject = "IDOS USER SETUP GUIDELINES";
            // sendUserMailWithSetUpsAttachment(session,attachmentSubject,fileName,file,fileName1,file1,corporateEmail);
            String body2 = welcome.render(ConfigParams.getInstance()).body();
            mailTimer(body2, username, session1, corporateEmail, null,
                    "Welcome to " + ConfigParams.getInstance().getCompanyApp());
            String salesTeamEmail = ConfigFactory.load().getString("smtpregistersale.user");
            if (salesTeamEmail == null || "".equals(salesTeamEmail)) {
                salesTeamEmail = "sales@myidos.com";
            }
            String salesMailBody = newOrganizationMail
                    .render(orgName, personName, corporateEmail, orgPhoneNumber, orgWebsite, registrationSource).body();
            mailTimer(salesMailBody, username, session, salesTeamEmail, null,
                    "A new organization has been registered.");
            entityTransaction.commit();
            result.put("companyOrgId", bOrg.getId());
            result.put("companyOrgName", bOrg.getName());
            result.put("companyOrgPerName", bOrg.getPersonName());
            result.put("companyOrgEmail", bOrg.getCorporateMail());
            result.put("companyOrgPhoneNo", bOrg.getRegisteredPhoneNumber());
            result.put("companyOrgWebsite", bOrg.getWebUrl());
        } catch (Exception ex) {
            if (entityTransaction.isActive()) {
                entityTransaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "SignUpOrganization Email", "SignUpOrganization Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result sellerSignUp(Request request) {
        log.log(Level.FINE, "=========Start==============");

        // EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        String result = "";
        try {
            JsonNode json = request.body().asJson();
            String accountName = json.findValue("accountName").asText();
            String email = json.findValue("email").asText();
            String password = json.findValue("password").asText();
            String encryptPassword = PasswordUtil.encrypt(password);
            IdosRegisteredVendor vendor = IdosRegisteredVendor.findByEmailAddress(entityManager, email);
            if (null == vendor) {
                vendor = new IdosRegisteredVendor(accountName, email, encryptPassword);
                vendor.setPresentStatus(0);
                transaction.begin();
                genericDAO.saveOrUpdate(vendor, null, entityManager);
                transaction.commit();
                String body1 = sellerAccountCreationLink
                        .render(password, email, "Please find the login details below:", ConfigParams.getInstance())
                        .body();
                final String username1 = ConfigFactory.load().getString("smtp.user");
                String subject1 = "Activate Your Account";
                Session session1 = noreplySession;
                mailTimer(body1, username1, session1, email, null, subject1);
                body1 = sellerAccountCreationLink.render(null, email, "Seller Email:", ConfigParams.getInstance())
                        .body();
                mailTimer(body1, username1, session1, "sales@myidos.com", null, "A new Seller/Vendor has signed up!");
                result = "success";
            } else {
                result = "failure";
            }
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "Seller SignUp Email", "Seller SignUp Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result Seller() {
        log.log(Level.FINE, ">>>> Start");
        String fullName = null;
        String email = null;
        String role = null;
        String allowedProcurement = null;
        Organization orgn = null;
        String pwdchanged = null;
        try {
            IdosRegisteredVendor idosRegSeller = SellerStaticController.getSellerInfo(entityManager, request);
            if (idosRegSeller != null) {
                return Results.ok(seller.render(idosRegSeller.getVendorName(), idosRegSeller.getVendorEmail(), role,
                        orgn, allowedProcurement, ConfigParams.getInstance()));
            } else {
                // return
                // Results.ok(registration.render(fullName,email,role,orgn,allowedProcurement,pwdchanged));
                return Results.ok(logoutsllr.render(null, null, null, null, null, ConfigParams.getInstance()));
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "Seller Email", "Seller Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    @Transactional
    public Result getOrgDataForSales(Http.Request request) {
        try {
            String fullName = null;
            String email = null;
            String role = null;
            String allowedProcurement = null;
            Organization orgn = null;
            Http.Session session = request.session();
            String pwdchanged = session.getOptional("pwdchanged").orElse("");
            session.removing();
            return Results.ok(allOrganizationsData.render(fullName, email, role, orgn, allowedProcurement,
                    ConfigParams.getInstance()));
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            String strBuff = getStackTraceMessage(e);
            expService.sendExceptionReport(strBuff, "Get Org data for Sales", "Seller Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(e);
            return Results.ok(errorPage.render(e, errorList));
        }

        // log.log(Level.FINE, "=========Start==============");
        // return
        // Results.ok(register.render(fullName,email,role,orgn,allowedProcurement,pwdchanged));
    }

    @Transactional
    public Result resend() {
        log.log(Level.FINE, "=========Start==============");

        String fullName = null;
        String email = null;
        String role = null;
        String allowedProcurement = null;
        Organization orgn = null;
        return Results.ok(
                resendactivation.render(fullName, email, role, orgn, allowedProcurement, ConfigParams.getInstance()));
    }

    @Transactional
    public Result checkEmail(Request request) {
        log.log(Level.FINE, ">>>> Start");
        ObjectNode result = Json.newObject();
        try {
            // EntityManager entityManager = getEntityManager();
            JsonNode json = request.body().asJson();
            ArrayNode an = result.putArray("userExistData");
            String email = json.findValue("email").asText().toLowerCase();
            // Modified by Puja Lohia 13th July'18
            StringBuilder sbquery = new StringBuilder("");
            sbquery.append("select obj from Users obj WHERE obj.email ='" + email + "' and obj.presentStatus=1");
            List<Users> users = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);

            if (users.size() > 0) {
                ObjectNode row = Json.newObject();
                row.put("dupusrmessage", "Email Already Registered.Please try a new Email Id.");
                an.add(row);
            } else {
                ObjectNode row = Json.newObject();
                row.put("dupusrmessage", "Email Available.");
                an.add(row);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "CheckEmail Email", "CheckEmail Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result activate(String corporateEmail, Http.Request request) throws ClassNotFoundException, IOException {
        log.log(Level.FINE, "=========Start==============");

        // // EntityManager entityManager=getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        String fullName = null;
        String email = null;
        String role = null;
        String allowedProcurement = null;
        Http.Session session = request.session();
        Organization orgn = null;
        String pwdchanged = null;
        Users users = null;
        ObjectNode results = Json.newObject();
        ArrayNode an = results.putArray("activateAccount");
        ObjectNode row = Json.newObject();
        Boolean activaterStatus = false;
        try {
            transaction.begin();
            if (corporateEmail != null) {
                List<Users> userList = Users.findByEmailActDeact(entityManager, corporateEmail);
                if (userList != null && userList.size() > 0) {
                    users = userList.get(0);
                    if (users.getPresentStatus() == 0) {
                        activaterStatus = true;
                    }
                    users.setPresentStatus(1);
                    genericDAO.saveOrUpdate(users, users, entityManager);
                    row.put("activated", "Company account is activated");
                    an.add(row);
                    String action = "activated user account";
                    String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                    if (ipAddress == null) {
                        ipAddress = request.remoteAddress();
                    }
                    auditDAO.saveAuditLogs(action, users, users.getId(), Users.class, ipAddress, row.toString(),
                            entityManager);
                } else {
                    row.put("activated", "Given Email is not registered with us.");
                    an.add(row);
                    String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                    if (ipAddress == null) {
                        ipAddress = request.remoteAddress();
                    }
                }
            }
            pwdchanged = IdosConstants.PUBLICK;

            if (ConfigParams.getInstance().isDeploymentSingleUser(users)) { // #SingleUser
                // For Single User Deployment Only
                if (users != null && activaterStatus) {
                    Organization organization = users.getOrganization();
                    List<Branch> branches = organization.getBranches();
                    if (branches != null && branches.size() > 0) {
                        Branch branch = branches.get(0);
                        singleUserService.updateOnOrganizationCreation(users, organization, branch, entityManager);
                        singleUserService.updateOnBranchCreation(users, branch, entityManager);
                    }

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

        return Results.ok(registration.render(fullName, email, role, orgn, allowedProcurement, pwdchanged,
                ConfigParams.getInstance()));
    }

    @Transactional
    public Result activateSellerAccount(final String corporateEmail, Http.Request request) {
        log.log(Level.FINE, "=========Start==============");

        // // EntityManager entityManager=getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        String fullName = null;
        String email = null;
        String pwdchanged = null;
        ObjectNode results = Json.newObject();
        Http.Session session = request.session();
        ArrayNode an = results.putArray("activateAccount");
        ObjectNode row = Json.newObject();
        IdosRegisteredVendor users = null;
        try {
            transaction.begin();
            if (corporateEmail != null) {
                users = IdosRegisteredVendor.findByEmailAddress(entityManager, corporateEmail);
                log.log(Level.INFO, users.getVendorEmail());
                users.setPresentStatus(1);
                log.log(Level.INFO, "Info " + users.getPresentStatus());
                genericDAO.saveOrUpdate(users, null, entityManager);
                row.put("activated", "company account is activated");
                an.add(row);
                String action = "activated user account";
                String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                if (ipAddress == null) {
                    ipAddress = request.remoteAddress();
                }
                // auditDAO.saveAuditLogs(action,users,users.getId(),Users.class,ipAddress,row.toString(),entityManager);
            }
            pwdchanged = session.getOptional("pwdchanged").orElse("");
            session.removing();
            transaction.commit();
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "Activate Seller Email", "Activate Seller Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            // log.log(Level.SEVERE, ex.getMessage());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results
                .ok(registration.render(fullName, email, null, null, null, pwdchanged, ConfigParams.getInstance()));
    }

    @Transactional
    public Result checkSellerEmail(Request request) {
        log.log(Level.FINE, "=========Start==============");

        // EntityManager entityManager = getEntityManager();
        String result = "";
        String email = request.body().asJson().findValue("email").asText();
        try {
            IdosRegisteredVendor vendor = IdosRegisteredVendor.findByEmailAddress(entityManager, email);
            if (null != vendor && (null != vendor.getVendorEmail() || !"".equals(vendor.getVendorEmail()))) {
                result = "failure";
            } else {
                Map<String, Object> criterias = new HashMap<String, Object>();
                criterias.put("email", email);
                criterias.put("type", 1);
                criterias.put("presentStatus", 1);
                Vendor vend = genericDAO.getByCriteria(Vendor.class, criterias, entityManager);
                if (vend != null) {
                    result = "failure";
                } else {
                    result = "";
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "CheckSellerEmail Email", "CheckSellerEmail SignUp Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(result);
    }

    @Transactional
    public Result forgotLoginInfo(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode results = Json.newObject();
        try {
            transaction.begin();
            JsonNode json = request.body().asJson();
            Http.Session session = request.session();
            ArrayNode an = results.putArray("logininfocredential");
            ObjectNode row = Json.newObject();
            String email = json.findPath("emailId").asText();
            StringBuilder sbquery = new StringBuilder("");
            sbquery.append("select obj from Users obj WHERE obj.email ='" + email + "' and obj.presentStatus=1");
            Users users = Users.findActiveByEmail(email);
            String reason = "Please check your email " + email + " and follow the instructions to reset your account.";
            if (users != null) {
                session.adding("user", users.getEmail());
                row.put("message", reason);
                an.add(row);
                try {
                    final String username = ConfigFactory.load().getString("smtp.user");
                    String token = PasswordUtil.getResetPasswordToken();
                    String body = resetLink.render(email, token, ConfigParams.getInstance()).body();
                    users.setResetPasswordToken(token);
                    genericDAO.save(users, users, entityManager);
                    Session mailsession = emailsession;
                    String subject = "Reset Account Password Link";
                    String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                    if (ipAddress == null) {
                        ipAddress = request.remoteAddress();
                    }
                    String action = "client from ip address " + ipAddress + " requested for password reset link";
                    auditDAO.saveAuditLogs(action, users, users.getId(), Users.class, ipAddress, row.toString(),
                            entityManager);
                    transaction.commit();
                    mailTimer(body, username, mailsession, email, null, subject);
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Error", ex);
                    String strBuff = getStackTraceMessage(ex);
                    expService.sendExceptionReport(strBuff, "ForgotLoginInfo Email", "ForgotLoginInfo Organization",
                            Thread.currentThread().getStackTrace()[1].getMethodName());
                    return Results.ok(results);
                }
                return Results.ok(results);
            } else {
                String result = "Failure";
                row.put("message", result);
                row.put("failurereason", reason);
                an.add(row);
                String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
                if (ipAddress == null) {
                    ipAddress = request.remoteAddress();
                }
                String action = "An attempt to forgotLoginInfo from ip address " + ipAddress + " is failed";
                auditDAO.saveAuditLogs(action, null, null, Users.class, ipAddress, row.toString(), entityManager);
                transaction.commit();
            }
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "ForgotLoginInfo Email", "ForgotLoginInfo Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(results).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result resetAccount(String email, Http.Request request) throws ClassNotFoundException, IOException {
        log.log(Level.FINE, "=========Start==============");

        String fullName = null;
        String pageemail = null;
        String role = null;
        String allowedProcurement = null;
        Organization orgn = null;
        String pwdchanged = null;
        Http.Session session = request.session();
        session.removing();
        return Results.ok(registration.render(fullName, pageemail, role, orgn, allowedProcurement, pwdchanged,
                ConfigParams.getInstance()));
    }

    @Transactional
    public Result userPasswordReset(Request request) {
        log.log(Level.FINE, ">>>> Start");
        ObjectNode result = Json.newObject();
        Users user = null;
        try {
            JsonNode json = request.body().asJson();
            String userEmail = json.findValue("userEmail").asText();
            String token = json.findValue("token").asText();
            String locHash = json.findValue("locHash").asText();
            if (null != userEmail && null != token) {
                user = Users.findByEmailNresetPasswordToken(entityManager, token, userEmail);
                if (user != null) {
                    if (null != user) {
                        result = AccountSettingService.getUserRandomQuestion(userEmail);
                        result.put("locHash", locHash);
                    } else {
                        result.put("message", false);
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "GetUserRandomQuestion Email", "GetUserRandomQuestion Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return Results.ok(result);
    }

    @Transactional
    public Result resetPassword(Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode result = Json.newObject();
        try {
            transaction.begin();
            JsonNode json = request.body().asJson();
            ArrayNode an = result.putArray("resetinfocredential");
            String accountEmail = json.findValue("resetemail").asText();
            String accountNewPwd = json.findValue("resetpassword").asText();
            Users user = Users.findActiveByEmail(accountEmail);
            if (user != null) {
                String question = json.findValue("question") != null ? json.findValue("question").asText() : null;
                String questionId = json.findValue("questionId") != null ? json.findValue("questionId").asText() : null;
                String answer = json.findValue("answer") != null ? json.findValue("answer").asText() : null;
                boolean update = json.findValue("update").asBoolean();
                UserProfileSecurity security = null;
                if ((null != question || null != questionId || null != answer) && !update) {
                    Map<String, Object> criterias = new HashMap<String, Object>();
                    criterias.put("user.id", user.getId());
                    criterias.put("id", Long.parseLong(questionId));
                    criterias.put("securityQuestion", question);
                    criterias.put("securedAnswer", answer);
                    criterias.put("presentStatus", 1);
                    security = genericDAO.getByCriteria(UserProfileSecurity.class, criterias, entityManager);
                    if (null != security) {
                        update = true;
                    } else {
                        update = false;
                    }
                }
                if (update) {
                    if (!PasswordChangeService.isPresentInPasswordHistory(PasswordUtil.encrypt(accountNewPwd), user,
                            entityManager)) {
                        user.setPassword(PasswordUtil.encrypt(accountNewPwd));
                        Calendar cal = Calendar.getInstance();
                        Date date = DateUtil.mysqldf.parse(DateUtil.mysqldf.format(cal.getTime()));
                        user.setLastUpdatedPasswordDate(date);
                        usercrud.save(user, user, entityManager);
                        PasswordHistory history = new PasswordHistory(user.getEmail(), user, user.getOrganization(),
                                PasswordUtil.encrypt(accountNewPwd));
                        genericDAO.saveOrUpdate(history, user, entityManager);
                        if (!ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
                            try {
                                final String username = ConfigFactory.load().getString("smtp.user");
                                String body = accountReset
                                        .render(user.getEmail(), accountNewPwd, ConfigParams.getInstance()).body();
                                Session mailsession = emailsession;
                                Email mail = new HtmlEmail();
                                mail.setMailSession(mailsession);
                                mail.setFrom(username);
                                mail.addTo(user.getEmail());
                                mail.setSubject("Login Details");
                                mail.setSentDate(new Date());
                                mail.setMsg(body);
                                // mail.send();
                            } catch (EmailException ex) {
                                log.log(Level.SEVERE, "Error", ex);
                            }
                        }
                        ObjectNode row = Json.newObject();
                        row.put("message", "Account reset successfully.");
                        row.put("result", true);
                        an.add(row);
                    } else {
                        ObjectNode row = Json.newObject();
                        row.put("message", "Password Already Used.");
                        row.put("result", false);
                        an.add(row);
                    }
                } else {
                    if (null == security) {
                        ObjectNode row = Json.newObject();
                        row.put("message", "Provide a Correct Security Answer.");
                        row.put("result", false);
                        an.add(row);
                    } else {
                        ObjectNode row = Json.newObject();
                        row.put("message", "Not Able to Find Account with Provided Account Credential.");
                        row.put("result", false);
                        an.add(row);
                    }
                }
            } else {
                ObjectNode row = Json.newObject();
                row.put("message", "Not Able To Locate Email.");
                row.put("result", false);
                an.add(row);
            }
            transaction.commit();
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "ResetPassword Email", "ResetPassword Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, ">>>> End " + result);
        return Results.ok(result);
    }

    @Transactional
    public Result terms() {
        log.log(Level.FINE, "=========Start==============");

        String fullName = null;
        String email = null;
        String role = null;
        String allowedProcurement = null;
        Organization orgn = null;
        if (ConfigParams.getInstance().getCompanyOwner().equals("PWC")) {
            return Results.ok(
                    pwcTerms.render(fullName, email, role, orgn, allowedProcurement, null, ConfigParams.getInstance()));
        } else {
            return Results.ok(
                    terms.render(fullName, email, role, orgn, allowedProcurement, null, ConfigParams.getInstance()));
        }
        // return
        // Results.ok(terms.render(fullName,email,role,orgn,allowedProcurement,null,
        // ConfigParams.getInstance()));

    }

    @Transactional
    public Result privacy() {
        log.log(Level.FINE, "=========Start==============");
        String fullName = null;
        String email = null;
        String role = null;
        String allowedProcurement = null;
        Organization orgn = null;
        if (ConfigParams.getInstance().getCompanyOwner().equals("PWC")) {
            return Results.ok(pwcPrivacy.render(fullName, email, role, orgn, allowedProcurement, null,
                    ConfigParams.getInstance()));
        } else {
            return Results.ok(
                    privacy.render(fullName, email, role, orgn, allowedProcurement, null, ConfigParams.getInstance()));
        }
    }

    @Transactional
    public Result enquiry(Request request) {
        log.log(Level.FINE, ">>>>> Start");
        ObjectNode result = Json.newObject();
        result.put("result", false);
        result.put("message", "Oops! Something went wrong. Please try again later.");
        try {
            JsonNode json = request.body().asJson();
            String cName = json.findValue("cName").asText();
            String enqEmail = json.findValue("enqEmail").asText();
            String enqComments = json.findValue("enqComments") == null ? null : json.findValue("enqComments").asText();
            String company = json.findValue("companyName") == null ? null : json.findValue("companyName").asText();
            String enqPhone = json.findValue("enqPhone").asText();
            int enquiryType = json.findValue("enquiryType") == null ? 1 : json.findValue("enquiryType").asInt();
            if (enqEmail != null) {
                APPLICATION_SERVICE.saveIdosLeads(cName, enqEmail, enqPhone, enquiryType);

                String subject = "Enquiry-" + cName + "-" + enqPhone;
                if (enquiryType == 2) {
                    subject = "Demo Request from " + cName + "-" + enqPhone;
                }
                String body = enquiryEmail
                        .render(cName, enqEmail, enqComments, enqPhone, company, ConfigParams.getInstance()).body();
                // log.log(Level.FINE, "email sent from " + enqEmail);
                mailTimer(body, enqEmail, emailsession, "sales@myidos.com", null, subject);
                result.put("result", true);
                // result.put("message", "Thank you for contacting us. We will get back to you
                // soon.");
                // result.put("message", "Thank you for your interest, We will connect with you
                // and help you get started with taking advantage of IDOS for your
                // organization.");
                result.put("message",
                        "Thank you for your interest in IDOS. Your message has been received and we will respond to you soon.");
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "Enquiry Email", "Enquiry Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        return Results.ok(result);
    }

    @Transactional
    public Result changePassword() {
        String usremail = "";
        try {
            log.log(Level.FINE, "====================" + usremail);
            // JsonNode json = request.body().asJson();
            return Results.ok(changePasswd.render(null, usremail, null, null, null, null, ConfigParams.getInstance()));
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, usremail, null,
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
    }

    @Transactional
    public Result sellerForgotLoginInfo(Http.Request request) {
        log.log(Level.FINE, ">>>> Start");
        // EntityManager entityManager = getEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        ObjectNode results = Json.newObject();
        try {
            transaction.begin();
            JsonNode json = request.body().asJson();
            ArrayNode an = results.putArray("logininfocredential");
            ObjectNode row = Json.newObject();
            String email = json.findPath("emailId").asText();
            Http.Session session = request.session();
            IdosRegisteredVendor regVendors = IdosRegisteredVendor.findByEmailAddress(entityManager, email);
            if (regVendors != null) {
                session.adding("user", regVendors.getVendorEmail());
                String result = "Check your mail " + email + " and follow the link to reset your account";
                row.put("message", result);
                an.add(row);
                try {
                    final String username = ConfigFactory.load().getString("smtp.user");
                    String body = sellerresetLink.render(email, ConfigParams.getInstance()).body();
                    Session mailsession = emailsession;
                    String subject = "Reset Seller Account Password Link";
                    transaction.commit();
                    mailTimer(body, username, mailsession, email, null, subject);
                } catch (Exception ex) {
                    log.log(Level.SEVERE, "Error", ex);
                    // log.log(Level.SEVERE, ex.getMessage());
                    return Results.ok(results);
                }
                return Results.ok(results);
            } else {
                String result = "Failure";
                row.put("message", result);
                String reason = "Check your email's inbox and follow the link to reset your account.";
                row.put("failurereason", reason);
                an.add(row);
            }
        } catch (Exception ex) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.log(Level.SEVERE, "Error", ex);
            // log.log(Level.SEVERE, ex.getMessage());
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "sellerForgotLoginInfo Email", "sellerForgotLoginInfo Organization",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        return Results.ok(results).withHeader("ContentType", "application/json");
    }

    @Transactional
    public Result verifyUser(Http.Request request) throws ClassNotFoundException, IOException {
        Organization orgn = null;
        Http.Session session = request.session();
        String email = session.getOptional("email").orElse("");
        Users user = Users.findActiveByEmail(email);
        // String OTPCode = generateAccessCode();
        String OTPCode = "12345";
        String body = views.html.loginVerificationLink.render(OTPCode, ConfigParams.getInstance()).body();

        new Mail.Builder()
                .withSession(emailsession)
                .withFrom(ConfigFactory.load().getString("smtp.user"))
                .withTo(user.getEmail())
                .withCC(null)
                .withSubject("Login verification code")
                .withMessageBody(body)
                .withSentDate(new Date())
                .sendMail();

        updateUserOTPCode(user.getEmail(), OTPCode);
        String fullName = null;
        String role = null;
        String allowedProcurement = null;
        String pwdchanged = IdosConstants.PUBLICK;
        return Results.ok(userverification.render(fullName, email, role, orgn, allowedProcurement, OTPCode,
                ConfigParams.getInstance()));

    }

    @Transactional
    public static void updateUserOTPCode(String emailAddress, String OTPCODE) {
        // EntityManager entityManager = getEntityManager();
        EntityTransaction entitytransaction = entityManager.getTransaction();
        entitytransaction.begin();
        Query query = entityManager.createQuery("update Users set verificationCode = ?1 where email=?2");
        query.setParameter(1, OTPCODE);
        query.setParameter(2, emailAddress);
        query.executeUpdate();
        entitytransaction.commit();
    }

    @Transactional
    public Result getPWCUsers(Http.Request request) {
        JsonNode json = request.body().asJson();

        log.log(Level.FINE, "SK>>>> Start getPWCUsers : " + json.findValue("userEmail").asText());
        // // EntityManager entityManager=getEntityManager();
        ObjectNode result = Json.newObject();
        ArrayNode userlistan = result.putArray("userListData");
        String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
        System.out.println("SK >>>>>>>>>>>> Inside getPWCUsers" + ipAddress);
        if (ipAddress == null) {
            ipAddress = request.remoteAddress();
        }

        try {
            List<PwcAuthUsers> userList = PwcAuthUsers.findByPWCEmail(entityManager,
                    json.findValue("userEmail").asText());
            if (userList.size() > 0) {
                for (PwcAuthUsers userTableList : userList) {
                    if (userTableList.getPresentStatus() == 1) {
                        ObjectNode row = Json.newObject();
                        // row.put("id", userTableList.getId());
                        // row.put("usersId", userTableList.getUsersId());
                        row.put("userEmail", userTableList.getUserEmail());
                        row.put("pwcEmail", userTableList.getPwcPreferredMail());
                        userlistan.add(row);
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
            String strBuff = getStackTraceMessage(ex);
            expService.sendExceptionReport(strBuff, "Get PWC Users", "Get PWC Users",
                    Thread.currentThread().getStackTrace()[1].getMethodName());
            List<String> errorList = getStackTrace(ex);
            return Results.ok(errorPage.render(ex, errorList));
        }
        log.log(Level.FINE, "SK>>>> end getPWCUsers : " + result);
        return Results.ok(result);
    }
}
