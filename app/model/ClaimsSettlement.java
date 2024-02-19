package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.*;
import service.EntityManagerProvider;
import java.util.List;

/**
 * Created by Sunil Namdev on 31-08-2016.
 */

@Entity
@Table(name = "CLAIMS_SETTLEMENT")
public class ClaimsSettlement extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public ClaimsSettlement() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_ID")
    private ClaimTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_SPECIFICS")
    private Specifics transactionSpecifics;

    public Specifics getTransactionSpecifics() {
        return transactionSpecifics;
    }

    public void setTransactionSpecifics(Specifics transactionSpecifics) {
        this.transactionSpecifics = transactionSpecifics;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Branch getBranch() {
        return this.branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    @Column(name = "ITEM_NAME")
    private String itemName;

    @Column(name = "ITEM_VALUE")
    private Double itemValue;

    @Column(name = "ITEM_GROSS_AMOUNT")
    private Double itemGross;

    @Column(name = "ITEM_TAX")
    private Double itemTax;

    public String getItemName() {
        return this.itemName;
    }

    public Double getItemValue() {
        return this.itemValue;
    }

    public void setItemValue(Double itemValue) {
        this.itemValue = itemValue;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public ClaimTransaction getTransaction() {
        return this.transaction;
    }

    public void setTransaction(ClaimTransaction transaction) {
        this.transaction = transaction;
    }

    public Double getItemTax() {
        return itemTax;
    }

    public void setItemTax(Double itemTax) {
        this.itemTax = itemTax;
    }

    public static List<ClaimsSettlement> findClaimsSettlementByClaimID(EntityManager entityManager,
            long transactionID) {
        String sbquery = "select obj from ClaimsSettlement obj where obj.transaction = " + transactionID
                + " and obj.presentStatus=1";
        List<ClaimsSettlement> claimsSettlementList = entityManager.createQuery(sbquery).getResultList();
        return claimsSettlementList;
    }

    public Double getItemGross() {
        return this.itemGross;
    }

    public void setItemGross(Double itemGross) {
        this.itemGross = itemGross;
    }
}
