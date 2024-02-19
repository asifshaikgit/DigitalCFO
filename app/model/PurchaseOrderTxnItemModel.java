package model;

import play.db.jpa.JPAApi;
import service.EntityManagerProvider;

import javax.persistence.*;

import com.idos.util.IdosDaoConstants;

import java.util.Date;
import java.util.List;

/**
 * @author Harish Kumar created on 30.05.2023
 */
@Entity
@Table(name="PURCHASE_ORDER_TXN_ITEM")
public class PurchaseOrderTxnItemModel extends AbstractBaseModel {
    private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public PurchaseOrderTxnItemModel() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PURCHASE_ORDER_ID")
    private PurchaseOrderTxnModel purchaseOrderTxn;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="EXPENSE_ID")
    private Specifics expense;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="VENDOR_ID")
    private Vendor vendor;

    
    @Column(name="PLACE_OF_SUPPLY")
    private String placeOfSupply;

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

    public PurchaseOrderTxnModel getPurchaseOrderTxn() {
        return this.purchaseOrderTxn;
    }

    public void setPurchaseOrderTxn(final PurchaseOrderTxnModel purchaseOrderTxn) {
        this.purchaseOrderTxn = purchaseOrderTxn;
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

    public String getPlaceOfSupply() {
        return this.placeOfSupply;
    }

    public void setPlaceOfSupply(final String placeOfSupply) {
        this.placeOfSupply = placeOfSupply;
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

    public static PurchaseOrderTxnItemModel findById(Long id) {
        return entityManager.find(PurchaseOrderTxnItemModel.class, id);
    }

    
}

