package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.idos.util.IdosUtil;
import com.idos.util.PasswordUtil;
import play.data.validation.Constraints;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@Table(name = "PWC_AUTH_USERS")
public class PwcAuthUsers extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public PwcAuthUsers() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String PWC_USERS_HQL = "select obj from PwcAuthUsers obj WHERE obj.pwcPreferredMail=?1 and obj.presentStatus=1 order by id";

    @Column(name = "PWC_ACCESS_TOKEN")
    private String pwcAccessToken;

    @Column(name = "PWC_REFRESH_TOKEN")
    private String pwcRefreshToken;

    @Column(name = "PWC_SCOPE")
    private String pwcScope;

    @Column(name = "PWC_ID_TOKEN")
    private String pwcIdToken;

    @Column(name = "PWC_TOKEN_TYPE")
    private String pwcTokenType;

    @Column(name = "PWC_ACCESS_TOKEN_EXPIRE")
    private String pwcAccessTokenExpire;

    @Column(name = "PWC_UID")
    private String pwcUid;

    @Column(name = "PWC_GIVEN_NAME")
    private String pwcGivenName;

    @Column(name = "PWC_FAMILY_NAME")
    private String pwcFamilyName;

    @Column(name = "PWC_NAME")
    private String pwcName;

    @Column(name = "PWC_PREFERRED_MAIL")
    private String pwcPreferredMail;

    @Column(name = "PWC_EMAIL")
    private String pwcEmail;

    @Column(name = "USER_EMAIL")
    private String userEmail;

    @Column(name = "PWC_SUB")
    private String pwcSub;

    @Column(name = "PWC_REALM")
    private String pwcRealM;

    @Column(name = "PWC_AUD")
    private String pwcAud;

    @Column(name = "PWC_UPN")
    private String pwcUpn;

    public String getPwcAccessToken() {
        return pwcAccessToken;
    }

    public void setPwcAccessToken(String pwcAccessToken) {
        this.pwcAccessToken = pwcAccessToken;
    }

    public String getRefreshToken() {
        return pwcRefreshToken;
    }

    public void setPwcRefreshToken(String pwcRefreshToken) {
        this.pwcRefreshToken = pwcRefreshToken;
    }

    public String getPwcScope() {
        return pwcScope;
    }

    public void setPwcScope(String pwcScope) {
        this.pwcScope = pwcScope;
    }

    public String getPwcIdToken() {
        return pwcIdToken;
    }

    public void setPwcIdToken(String pwcIdToken) {
        this.pwcIdToken = pwcIdToken;
    }

    public String getPwcTokenType() {
        return pwcTokenType;
    }

    public void setPwcTokenType(String pwcTokenType) {
        this.pwcTokenType = pwcTokenType;
    }

    public String getPwcAccessTokenExpire() {
        return pwcAccessTokenExpire;
    }

    public void setPwcAccessTokenExpire(String pwcAccessTokenExpire) {
        this.pwcAccessTokenExpire = pwcAccessTokenExpire;
    }

    public String getPwcUid() {
        return pwcUid;
    }

    public void setPwcUid(String pwcUid) {
        this.pwcUid = pwcUid;
    }

    public String getPwcGivenName() {
        return pwcGivenName;
    }

    public void setPwcGivenName(String pwcGivenName) {
        this.pwcGivenName = pwcGivenName;
    }

    public String getPwcFamilyName() {
        return pwcFamilyName;
    }

    public void setPwcFamilyName(String pwcFamilyName) {
        this.pwcFamilyName = pwcFamilyName;
    }

    public String getPwcName() {
        return pwcName;
    }

    public void setPwcName(String pwcName) {
        this.pwcName = pwcName;
    }

    public String getPwcPreferredMail() {
        return pwcPreferredMail;
    }

    public void setPwcPreferredMail(String pwcPreferredMail) {
        this.pwcPreferredMail = pwcPreferredMail;
    }

    public String getPwcEmail() {
        return pwcEmail;
    }

    public void setPwcEmail(String pwcEmail) {
        this.pwcEmail = pwcEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPwcSub() {
        return pwcSub;
    }

    public void setPwcSub(String pwcSub) {
        this.pwcSub = pwcSub;
    }

    public String getPwcRealM() {
        return pwcRealM;
    }

    public void setPwcRealM(String pwcRealM) {
        this.pwcRealM = pwcRealM;
    }

    public String getPwcAud() {
        return pwcAud;
    }

    public void setPwcAud(String pwcAud) {
        this.pwcAud = pwcAud;
    }

    public String getPwcUpn() {
        return pwcUpn;
    }

    public void setPwcUpn(String pwcUpn) {
        this.pwcUpn = pwcUpn;
    }

    public void save() {
        entityManager.persist(this);
    }

    public static boolean checkPwcUserAlreadyCreated(EntityManager entityManager, String pwcEmail, String userEmail) {
        boolean isValid = false;
        String query = "FROM PwcAuthUsers u WHERE u.pwcEmail = ?wcEmail AND u.userEmail = :userEmail";
        Query q = entityManager.createQuery(query);
        q.setParameter("pwcEmail", pwcEmail);
        q.setParameter("userEmail", userEmail);
        try {
            isValid = q.getSingleResult() == null ? false : true;
        } catch (NoResultException ex) {
            // getLogger().error("Security code not found", ex);
        }
        return isValid;
    }

    public static List<PwcAuthUsers> getUserByPwcEmail(EntityManager entityManager, String pwcPreferredMail) {
        List<PwcAuthUsers> users = null;
        if (pwcPreferredMail == null) {
            return users;
        }
        Query query = entityManager.createQuery("FROM PwcAuthUsers u WHERE u.pwcPreferredMail = ?wcPreferredMail");
        query.setParameter("pwcPreferredMail", pwcPreferredMail);
        users = query.getResultList();
        if (users.size() <= 0) {
            users = null;
        }
        return users;
    }

    @SuppressWarnings("unchecked")
    public static List<PwcAuthUsers> findByPWCEmail(EntityManager entityManager, String email) {
        List<PwcAuthUsers> users = null;
        if (email == null) {
            return users;
        }
        Query query = entityManager.createQuery(PWC_USERS_HQL);
        query.setParameter(1, email);
        users = query.getResultList();
        return users;
    }
}
