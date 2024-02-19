package model;

import java.util.List;

import javax.persistence.*;

import com.idos.util.IdosDaoConstants;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil Namdev on 06-07-2016.
 */
@Entity
@Table(name = "PROVISION_JOURNAL_ENTRY_DETAIL")
public class ProvisionJournalEntryDetail extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public ProvisionJournalEntryDetail() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String ORG_TXN_SPCFC_JPQL = "from ProvisionJournalEntryDetail obj where obj.organization.id = ?1 and (obj.headID = ?2 or obj.headID2 = ?3)";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROVISION_JOURNAL_ENTRY_ID")
    private IdosProvisionJournalEntry provisionJournalEntry;

    @Column(name = "HEAD_ID")
    private Long headID;

    @Column(name = "HEAD_ID2")
    private Long headID2;

    @Column(name = "HEAD_TYPE")
    private String headType;

    @Column(name = "IS_DEBIT")
    private Integer isDebit;

    @Column(name = "HEAD_AMOUNT")
    private Double headAmount;

    @Column(name = "UNITS")
    private Double units;

    @Column(name = "PRICE")
    private Double unitPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    public IdosProvisionJournalEntry getProvisionJournalEntry() {
        return provisionJournalEntry;
    }

    public void setProvisionJournalEntry(IdosProvisionJournalEntry provisionJournalEntry) {
        this.provisionJournalEntry = provisionJournalEntry;
    }

    public Long getHeadID() {
        return headID;
    }

    public void setHeadID(Long headID) {
        this.headID = headID;
    }

    public Long getHeadID2() {
        return this.headID2;
    }

    public void setHeadID2(Long headID2) {
        this.headID2 = headID2;
    }

    public String getHeadType() {
        return headType;
    }

    public void setHeadType(String headType) {
        this.headType = headType;
    }

    public Integer getIsDebit() {
        return isDebit;
    }

    public void setIsDebit(Integer isDebit) {
        this.isDebit = isDebit;
    }

    public Double getHeadAmount() {
        return headAmount;
    }

    public void setHeadAmount(Double headAmount) {
        this.headAmount = headAmount;
    }

    public Branch getBranch() {
        return this.branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Double getUnits() {
        return this.units;
    }

    public void setUnits(Double units) {
        this.units = units;
    }

    public Double getUnitPrice() {
        return this.unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public static List<ProvisionJournalEntryDetail> findByOrgSpecific(EntityManager entityManager, long orgid,
            long specificId) {
        List<ProvisionJournalEntryDetail> list = null;
        Query query = entityManager.createQuery(ORG_TXN_SPCFC_JPQL);
        query.setParameter(1, orgid);
        query.setParameter(2, specificId);
        query.setParameter(3, specificId);
        list = query.getResultList();
        return list;
    }

}
