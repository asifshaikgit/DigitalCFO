package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.*;
import service.EntityManagerProvider;
import java.util.Date;
import java.util.List;

/**
 * Created by Sunil Namdev on 30-08-2017.
 */
@Entity
@Table(name = "ADVANCE_ADJUSTMENT_DETAIL")
public class AdvanceAdjustmentDetail extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public AdvanceAdjustmentDetail() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String HQL = "select obj from AdvanceAdjustmentDetail obj WHERE obj.transaction.id=?1 and obj.presentStatus=1";
    private static final String SQL = "select obj from AdvanceAdjustmentDetail obj WHERE obj.advTransaction.id=?1 and obj.presentStatus=1";
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCHID")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATIONID")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTIONID")
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ADV_TRANSACTIONID")
    private Transaction advTransaction;

    @Column(name = "ADJUSTED_AMOUNT")
    private Double adjustedAmount;

    @Column(name = "ADVADJ_TAX1_VALUE")
    private Double advAdjTax1Value;

    @Column(name = "ADVADJ_TAX2_VALUE")
    private Double advAdjTax2Value;

    @Column(name = "ADVADJ_TAX3_VALUE")
    private Double advAdjTax3Value;

    @Column(name = "ADVADJ_TAX4_VALUE")
    private Double advAdjTax4Value;

    @Column(name = "ADVADJ_TAX5_VALUE")
    private Double advAdjTax5Value;

    @Column(name = "ADVADJ_TAX6_VALUE")
    private Double advAdjTax6Value;

    @Column(name = "ADVADJ_TAX7_VALUE")
    private Double advAdjTax7Value;

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

    public Transaction getTransaction() {
        return this.transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getAdvTransaction() {
        return this.advTransaction;
    }

    public void setAdvTransaction(Transaction advTransaction) {
        this.advTransaction = advTransaction;
    }

    public Double getAdjustedAmount() {
        return this.adjustedAmount;
    }

    public void setAdjustedAmount(Double adjustedAmount) {
        this.adjustedAmount = adjustedAmount;
    }

    public Double getAdvAdjTax1Value() {
        return this.advAdjTax1Value;
    }

    public void setAdvAdjTax1Value(Double advAdjTax1Value) {
        this.advAdjTax1Value = advAdjTax1Value;
    }

    public Double getAdvAdjTax2Value() {
        return this.advAdjTax2Value;
    }

    public void setAdvAdjTax2Value(Double advAdjTax2Value) {
        this.advAdjTax2Value = advAdjTax2Value;
    }

    public Double getAdvAdjTax3Value() {
        return this.advAdjTax3Value;
    }

    public void setAdvAdjTax3Value(Double advAdjTax3Value) {
        this.advAdjTax3Value = advAdjTax3Value;
    }

    public Double getAdvAdjTax4Value() {
        return this.advAdjTax4Value;
    }

    public void setAdvAdjTax4Value(Double advAdjTax4Value) {
        this.advAdjTax4Value = advAdjTax4Value;
    }

    public Double getAdvAdjTax5Value() {
        return this.advAdjTax5Value;
    }

    public void setAdvAdjTax5Value(Double advAdjTax5Value) {
        this.advAdjTax5Value = advAdjTax5Value;
    }

    public Double getAdvAdjTax6Value() {
        return this.advAdjTax6Value;
    }

    public void setAdvAdjTax6Value(Double advAdjTax6Value) {
        this.advAdjTax6Value = advAdjTax6Value;
    }

    public Double getAdvAdjTax7Value() {
        return this.advAdjTax7Value;
    }

    public void setAdvAdjTax7Value(Double advAdjTax7Value) {
        this.advAdjTax7Value = advAdjTax7Value;
    }

    public static AdvanceAdjustmentDetail findById(Long id) {
        return entityManager.find(AdvanceAdjustmentDetail.class, id);
    }

    public static List<AdvanceAdjustmentDetail> findListByTxn(EntityManager entityManager, Long txnid) {
        Query query = entityManager.createQuery(HQL);
        query.setParameter(1, txnid);
        List<AdvanceAdjustmentDetail> list = query.getResultList();
        return list;
    }

    public static List<AdvanceAdjustmentDetail> findListByAdvTxn(EntityManager entityManager, Long txnid) {
        Query query = entityManager.createQuery(SQL);
        query.setParameter(1, txnid);
        List<AdvanceAdjustmentDetail> list = query.getResultList();
        return list;
    }
}
