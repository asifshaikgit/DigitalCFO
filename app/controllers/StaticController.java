package controllers;

import action.Secured;
import com.idos.util.IdosConstants;
import model.*;
import play.db.jpa.JPAApi;
//
import javax.transaction.Transactional;
import play.mvc.Security;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.Query;

import play.mvc.Controller;
import java.util.Optional;
import java.util.logging.Logger;

import play.mvc.Http;
import com.typesafe.config.Config;
import play.Application;

/**
 * Created by Sunil Namdev on 26-09-2016.
 */
@Security.Authenticated(Secured.class)
public class StaticController extends BaseController {
    protected static final String TRANSACTION_ID = "transactionPrimId";
    protected static final String TRANSACTION_REF_NO = "transactionRefNo";
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Inject
    public StaticController(Application application) {
        super(application); // Call the constructor of BaseController
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Transactional
    public static Users getUserInfo(Http.Request request) {
        Http.Session session = request.session();
        String email = session.getOptional("email").orElse("");
        System.out.println(">>>>getUserInfo" + email);
        Users user = Users.findActiveByEmail(email);
        try {
            if (user == null) {
                Vendor vendorUser = getVendorInfo(request);
                if (vendorUser != null) {
                    user = Users.findActiveByEmail(vendorUser.getOrganization().getCorporateMail());
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error" + e.getMessage());
        }
        return user;
    }

    @Transactional
    public static Vendor getVendorInfo(Http.Request request) throws Exception {
        Http.Session session = request.session();
        String email = session.getOptional("email").orElse("");
        return Vendor.findByEmailAddress(email);
    }

    @Transactional
    public static Branch getBranch(String branchName, Users user) {
        // Http.Session session = request.session();
        // Optional<String> email = request.session().get("email");---> sinse not using
        // in method
        Branch bnch = null;
        List<Branch> bnchlist = Branch.findListByOrgIdEqualName(user.getOrganization().getId(),
                branchName);
        if (bnchlist.size() > 0) {
            bnch = bnchlist.get(0);
        }
        return bnch;
    }

    @Transactional
    public static Project getProject(String projectName, Users user) {
        // Http.Session session = request.session();
        // Optional<String> email = request.session().get("email"); ---->sinse not using
        // in method
        Project pjct = null;
        List<Project> pjctlist = Project.findListByOrgEqualName(user.getOrganization().getId(),
                projectName);
        if (pjctlist.size() > 0) {
            pjct = pjctlist.get(0);
        }
        return pjct;
    }

    public static String getUserRoles(Users user) {
        StringBuffer userRolesStr = new StringBuffer();
        Query query = entityManager.createQuery(
                "select obj from UsersRoles obj WHERE obj.user.id=?1 and obj.presentStatus=1");
        query.setParameter(1, user.getId());
        List<UsersRoles> resultList = query.getResultList();
        for (UsersRoles usrRole : resultList) {
            userRolesStr.append(usrRole.getRole().getName()).append(",");
        }
        String actUserRoles = userRolesStr.substring(0, userRolesStr.length());
        return actUserRoles;
    }

    public static String getUserRolesIds(Users user) {
        StringBuffer userRolesStr = new StringBuffer();
        Query query = entityManager.createQuery(
                "select obj from UsersRoles obj WHERE obj.user.id=?1 and obj.presentStatus=1");
        query.setParameter(1, user.getId());
        List<UsersRoles> resultList = query.getResultList();
        for (UsersRoles usrRole : resultList) {
            userRolesStr.append(usrRole.getRole().getId()).append(",");
        }
        String actUserRoles = userRolesStr.substring(0, userRolesStr.length());
        return actUserRoles;
    }

    public static String getUserTransactionPurposeIds(Users user, EntityManager entityManager) {
        StringBuffer userTxnPurpStr = new StringBuffer();
        Long userId = user.getId();
        Long orgId = user.getOrganization().getId();
        List<UserTransactionPurpose> listuserTxnPurpose = UserTransactionPurpose.getUserTransactionListByUserId(entityManager,orgId,userId);
        if(listuserTxnPurpose != null){
           for (UserTransactionPurpose usrTxnPurp : listuserTxnPurpose) {
            userTxnPurpStr.append(usrTxnPurp.getTransactionPurpose().getId()).append(",");
           }  
        }
        String str = userTxnPurpStr.substring(0, userTxnPurpStr.length());
        return str;
    }
}
