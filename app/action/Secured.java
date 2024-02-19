package action;

import java.util.Date;
import java.util.Optional;
import model.IdosRegisteredVendor;
import model.Users;
import model.Vendor;
import javax.inject.Inject;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.typesafe.config.Config;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
//import play.mvc.Http.Session;
import play.db.jpa.JPAApi;
import com.idos.util.IdosConstants;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

public class Secured extends Security.Authenticator {
    private static Logger log = Logger.getLogger("action");
    private static long sessionTimeout = 300000L;
    private final Config configuration;
    private static EntityManager entityManager;
    private static JPAApi jpaApi;

    @Inject
    public Secured(Config configuration, JPAApi jpaApi) {
        this.configuration = configuration;
        System.out.println("SK >>>>>>>>>>>> User jpaApi " + jpaApi);
        this.jpaApi = jpaApi;
        System.out.println("SK >>>>>>>>>>>> User after jpaApi " + jpaApi);
        entityManager = EntityManagerProvider.getEntityManager();
        System.out.println("SK >>>>>>>>>>>> User entity " + entityManager);
        String timeoutValue = configuration.getString("sessionTimeout");
        if (timeoutValue != null) {
            sessionTimeout = Long.valueOf(timeoutValue) * 1000 * 60;
        }
    }

    @Override
    public Optional<String> getUsername(Http.Request request) {
        String useremail = null;
        // see if the session is expired
        Http.Session session = request.session();
        String previousTick = session.getOptional("userTime").orElse("");
        if (previousTick != null && !previousTick.equals("")) {
            long previousT = Long.valueOf(previousTick);
            long currentT = new Date().getTime();
            if ((currentT - previousT) > sessionTimeout) {
                session = session.removing("userTime");
                return Optional.empty();
            }
        }
        // update time in session
        String tickString = Long.toString(new Date().getTime());

        session = session.adding("userTime", tickString);
        // String[] authTokenHeaderValues =
        // request.getHeaders().get(IdosConstants.AUTH_TOKEN_HEADER);
        Optional<String> authTokenHeaderOptional = request.getHeaders().get(IdosConstants.AUTH_TOKEN_HEADER);
        String[] authTokenHeaderValues = authTokenHeaderOptional.map(value -> new String[] { value })
                .orElse(new String[0]);
        System.out.println("<<<<<<<<<<<<<<<<<<<<<" + authTokenHeaderValues[0]);
        if (authTokenHeaderValues == null || authTokenHeaderValues.length < 1) {
            authTokenHeaderOptional = request.getHeaders().get("x-auth-token");
            authTokenHeaderValues = authTokenHeaderOptional.map(value -> new String[] { value }).orElse(new String[0]);
        }
        if (authTokenHeaderValues == null || authTokenHeaderValues.length < 1) {
            authTokenHeaderOptional = request.getHeaders().get("X-Auth-Token");
            authTokenHeaderValues = authTokenHeaderOptional.map(value -> new String[] { value }).orElse(new String[0]);
        }

        // String[] authTokenHeader0Values =
        // request.getHeaders().get(IdosConstants.AUTH_TOKEN_HEADER0);
        // String[] authTokenHeader1Values =
        // request.getHeaders().get(IdosConstants.AUTH_TOKEN_HEADER1);
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1)
                && (authTokenHeaderValues[0] != null)) {
            log.log(Level.FINE, "======================>" + authTokenHeaderValues[0]);
            if (authTokenHeaderValues[0].startsWith("se11")) {
                IdosRegisteredVendor user = IdosRegisteredVendor.findByAuthToken(entityManager,
                        authTokenHeaderValues[0]);
                if (user != null) {
                    // request.addAttr("user", user);
                    useremail = user.getVendorEmail();
                    session = session.adding(IdosConstants.SELLER_EMAIL, useremail);
                }
                log.log(Level.FINE, "sel======================" + useremail);
            } else if (authTokenHeaderValues[0].startsWith("vend")) {
                Vendor user = Vendor.findByAuthToken(entityManager, authTokenHeaderValues[0]);
                if (user != null) {
                    // request.addAttr("user", user);
                    useremail = user.getEmail();
                    session = session.adding(IdosConstants.VENDOR_EMAIL, useremail);
                }
                log.log(Level.FINE, "vend======================" + useremail);
            } else {
                System.out.println("authTokenHeaderValues");
                String[] tokenArr = authTokenHeaderValues[0].split("\\|");
                String token1 = null;
                String token2 = "0";
                String token3 = null;
                if (tokenArr.length > 0) {
                    token1 = tokenArr[0];
                }
                if (tokenArr.length > 1) {
                    token2 = tokenArr[1];
                }
                if (tokenArr.length > 2) {
                    token3 = tokenArr[2];
                }
                Users user = null;
                if (token2 != null && !"".equals(token2.trim())) {
                    user = Users.findByAuthToken(entityManager, token1, Long.parseLong(token2));
                }
                if (user != null) {
                    if (token3 != null && token3.equals(user.getUserRoleIDs())) {
                        // request.addAttr("user", user);
                        useremail = user.getEmail();
                        session = session.adding(IdosConstants.USER_EMAIL, useremail);
                    }
                }
                log.log(Level.FINE, " Usr======================" + useremail);
            }
        }
        // return Optional.of(useremail);
        return Optional.ofNullable(useremail);
    }

    @Override
    public Result onUnauthorized(Http.Request request) {
        return unauthorized();
    }

}
