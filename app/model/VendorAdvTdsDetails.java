package model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import com.idos.util.IdosConstants;
import com.idos.util.IdosDaoConstants;

import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;

@Entity
@Table(name = "VENDOR_TDS_ADVANCE")
public class VendorAdvTdsDetails extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public VendorAdvTdsDetails() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String TDS_HISTORY_QUERY = "select obj from VendorAdvTdsDetails obj where obj.organization.id = ?1 and obj.vendor.id = ?2 and obj.specifics.id = ?3 and obj.presentStatus = 1 and obj.uptoDate between ?4 and ?5";

    private static final String TDS_FIND_BY_BASIC_QUERY = "select obj from VendorAdvTdsDetails obj where obj.organization.id = ?1 and obj.tdsBasicId = ?2 and obj.presentStatus=1";
    @Column(name = "UPTO_DATE")
    public Date uptoDate;
    @Column(name = "TDS_BASIC_ID")
    public Long tdsBasicId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VENDOR_ID")
    private Vendor vendor;
    @Column(name = "SPECIFICS_ID")
    private Specifics specifics;
    @Column(name = "EXPENCE_AMT")
    private Double expenceAmt;
    @Column(name = "EXP_TDS_EFFECTED")
    private Double expTdsEffected;
    @Column(name = "ADV_PAID_NOT_ADJUSTED")
    private Double advPaidNotAdjusted;
    @Column(name = "ADV_TDS_EFFECTED")
    private Double advTdsEffected;

    public static VendorAdvTdsDetails findById(Long id) {
        return entityManager.find(VendorAdvTdsDetails.class, id);
    }

    public static VendorAdvTdsDetails findVendorItemTDSHistory(EntityManager entityManager, Long orgid, Long specId,
            Long vendId, Date toDate, Date fromDate) {
        VendorAdvTdsDetails obj = null;
        Query query = entityManager.createQuery(TDS_HISTORY_QUERY);
        query.setParameter(1, orgid);
        query.setParameter(2, vendId);
        query.setParameter(3, specId);
        query.setParameter(4, fromDate);
        query.setParameter(5, toDate);
        List resultList = query.getResultList();
        if (resultList != null && resultList.size() > 0) {
            return (VendorAdvTdsDetails) resultList.get(0);
        }
        return obj;
    }

    public static VendorAdvTdsDetails findByTdsBasicId(EntityManager entityManager, Long orgid, Long tdsBasicId) {
        VendorAdvTdsDetails obj = null;
        Query query = entityManager.createQuery(TDS_FIND_BY_BASIC_QUERY);
        query.setParameter(1, orgid);
        query.setParameter(2, tdsBasicId);
        List resultList = query.getResultList();
        if (resultList != null && resultList.size() > 0) {
            return (VendorAdvTdsDetails) resultList.get(0);
        }
        return obj;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(
            Organization organization) {
        this.organization = organization;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Specifics getSpecifics() {
        return specifics;
    }

    public void setSpecifics(
            Specifics specifics) {
        this.specifics = specifics;
    }

    public Double getExpenceAmt() {
        return expenceAmt;
    }

    public void setExpenceAmt(
            Double expenceAmt) {
        this.expenceAmt = expenceAmt;
    }

    public Double getExpTdsEffected() {
        return expTdsEffected;
    }

    public void setExpTdsEffected(
            Double expTdsEffected) {
        this.expTdsEffected = expTdsEffected;
    }

    public Double getAdvPaidNotAdjusted() {
        return advPaidNotAdjusted;
    }

    public void setAdvPaidNotAdjusted(Double advPaidNotAdjusted) {
        this.advPaidNotAdjusted = advPaidNotAdjusted;
    }

    public Long getTdsBasicId() {
        return tdsBasicId;
    }

    public void setTdsBasicId(Long tdsBasicId) {
        this.tdsBasicId = tdsBasicId;
    }

    public Double getAdvTdsEffected() {
        return advTdsEffected;
    }

    public void setAdvTdsEffected(
            Double advTdsEffected) {
        this.advTdsEffected = advTdsEffected;
    }

    public Date getUptoDate() {
        return uptoDate;
    }

    public void setUptoDate(Date uptoDate) {
        this.uptoDate = uptoDate;
    }

}
