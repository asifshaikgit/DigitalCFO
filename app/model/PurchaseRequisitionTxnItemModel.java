package model;

import play.db.jpa.JPAApi;
import service.EntityManagerProvider;

import javax.persistence.Query;
import javax.persistence.*;

import com.idos.util.IdosDaoConstants;

import java.util.Date;
import java.util.List;

/**
 * @author Harish Kumar created on 01.05.2023
 */
@Entity
@Table(name="PURCHASE_REQUISITION_TXN_ITEM")
public class PurchaseRequisitionTxnItemModel extends AbstractBaseModel {

    private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public PurchaseRequisitionTxnItemModel() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PUR_REQ_TXN_ID")
    private PurchaseRequisitionTxnModel purchaseRequisitionTxn;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="EXPENSE_ID")
    private Specifics expense;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="VENDOR_ID")
    private Vendor vendor;

    @Column(name="MEASURE_NAME")
    private String measureName;

    @Column(name="OEM")
    private String oem;

    @Column(name="NO_OF_UNITS")
    private Double noOfUnits;

    @Column(name="TYPE_OF_MATERIAL")
    private String typeOfMaterial;

    @Column(name="EXPECTED_DATETIME")
    private Date expectedDatetime;

    public PurchaseRequisitionTxnModel getPurchaseRequisitionTxn() {
        return this.purchaseRequisitionTxn;
    }

    public void setPurchaseRequisitionTxn(final PurchaseRequisitionTxnModel purchaseRequisitionTxn) {
        this.purchaseRequisitionTxn = purchaseRequisitionTxn;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }

    public Specifics getExpense() {
        return this.expense;
    }

    public void setExpense(final Specifics expense) {
        this.expense = expense;
    }

    public Vendor getVendor() {
        return this.vendor;
    }

    public void setVendor(final Vendor vendor) {
        this.vendor = vendor;
    }

    public String getMeasureName() {
        return this.measureName;
    }

    public void setMeasureName(final String measureName) {
        this.measureName = measureName;
    }

    public String getOem() {
        return this.oem;
    }

    public void setOem(final String oem) {
        this.oem = oem;
    }

    public Double getNoOfUnits() {
        return this.noOfUnits;
    }

    public void setNoOfUnits(final Double noOfUnits) {
        this.noOfUnits = noOfUnits;
    }

    public String getTypeOfMaterial() {
        return this.typeOfMaterial;
    }

    public void setTypeOfMaterial(final String typeOfMaterial) {
        this.typeOfMaterial = typeOfMaterial;
    }

    public Date getExpectedDatetime() {
        return this.expectedDatetime;
    }

    public void setExpectedDatetime(final Date expectedDatetime) {
        this.expectedDatetime = expectedDatetime;
    }

    public static PurchaseRequisitionTxnItemModel findById(Long id) {
        return entityManager.find(PurchaseRequisitionTxnItemModel.class, id);
    }

    public static List<PurchaseRequisitionTxnItemModel> getPurchaseRequisitionTxnItemByPRId(Long orgId, Long purReqId) {
        List<PurchaseRequisitionTxnItemModel> list = null;
        Query query = entityManager.createQuery("select obj from PurchaseRequisitionTxnItemModel obj where obj.organization.id = ?1 and obj.purchaseRequisitionTxn.id = ?2 and obj.presentStatus=1");
        query.setParameter(IdosDaoConstants.P1, orgId);
        query.setParameter(IdosDaoConstants.P2, purReqId);
        list = query.getResultList();
        return list;
    }
    
}
