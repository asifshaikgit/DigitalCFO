package controllers;

import action.Secured;
import action.SellerSecured;
import model.IdosRegisteredVendor;
import play.db.jpa.JPAApi;

import javax.transaction.Transactional;
import play.mvc.Security;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.List;
import play.mvc.Http;
import java.util.Optional;
import javax.inject.Inject;

/**
 * Created by Sunil Namdev on 26-09-2016.
 */
@Security.Authenticated(Secured.class)
public class SellerStaticController extends BaseController {
    @Transactional
    public static IdosRegisteredVendor getSellerInfo(EntityManager entityManager, Http.Request request)
            throws Exception {
        Http.Session session = request.session();
        String email = session.getOptional("selleremail").orElse("");
        return IdosRegisteredVendor.findByEmailAddress(entityManager, email);
    }
}
