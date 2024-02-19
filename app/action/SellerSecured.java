package action;

import com.idos.util.IdosConstants;
import model.IdosRegisteredVendor;
import model.Users;
import java.util.logging.Logger;
import java.util.logging.Level;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import static play.mvc.Results.unauthorized;
import java.util.Optional;

/**
 * Created by Sunil Namdev on 26-09-2016.
 */
public class SellerSecured extends Security.Authenticator {
    private static EntityManager entityManager;
    private static Logger log = Logger.getLogger("action");

    @Override
    public Optional<String> getUsername(Http.Request request) {
        String useremail = null;

        Optional<String> authTokenHeaderOptional = request.getHeaders().get(IdosConstants.AUTH_TOKEN_HEADER);
        String[] authTokenHeaderValues = authTokenHeaderOptional.map(value -> new String[] { value })
                .orElse(new String[0]);
        if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1)
                && (authTokenHeaderValues[0] != null)) {
            IdosRegisteredVendor user = model.IdosRegisteredVendor.findByAuthToken(entityManager,
                    authTokenHeaderValues[0]);
            if (user != null) {
                // request.addAttr("user", user);
                useremail = user.getVendorEmail();
            }
        }
        log.log(Level.FINE, "======================" + useremail);
        return Optional.of(useremail);
    }

    @Override
    public Result onUnauthorized(Http.Request request) {
        return unauthorized();
    }
}
