package model.internal;

import com.idos.util.DateUtil;
import model.AbstractBaseModel;
import model.Organization;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import model.Users;
import play.Logger;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

import javax.persistence.*;
import java.util.Calendar;
import java.util.List;

/**
 * @auther sunil namdev created on 24.09.2018
 */
@Entity
@Table(name = "IDOS_ADMIN_USER")
public class IdosAdminUser extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public IdosAdminUser() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String ACC_TYPE_HQL = "select obj from IdosAdminUser obj where organization.id = ?1 and obj.presentStatus=1";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private Users users; // company email id

    @Column(name = "ACCOUNT_TYPE")
    private Integer accountType; // 1- admin, 2- demo,

    @Column(name = "ACTIVE_FOR_DAYS")
    private Integer activeForDays; // -1: never expire

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Users getUsers() {
        return this.users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public Integer getAccountType() {
        return this.accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public Integer getActiveForDays() {
        return this.activeForDays;
    }

    public void setActiveForDays(Integer activeForDays) {
        this.activeForDays = activeForDays;
    }

    public String getExpiryDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(this.modifiedAt);
        c.add(Calendar.DATE, this.activeForDays);
        String expiryDate = DateUtil.idosdf.format(c.getTime());
        return expiryDate;
    }

    public long getExpiryRemainingDays() {
        Calendar c = Calendar.getInstance();
        Calendar modified = Calendar.getInstance();
        modified.setTime(this.modifiedAt);
        long days = DateUtil.daysBetween(modified, c);
        Logger.debug("====== " + days);
        return days;
    }

    public static IdosAdminUser findByOrganization(EntityManager entityManager, Long orgid) {
        IdosAdminUser idosAdminUser = null;
        Query query = entityManager.createQuery(ACC_TYPE_HQL);
        query.setParameter(1, orgid);
        List<IdosAdminUser> admins = query.getResultList();
        if (admins != null && admins.size() > 0) {
            idosAdminUser = admins.get(0);
        }
        return idosAdminUser;
    }
}
