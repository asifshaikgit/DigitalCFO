package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Sunil K Namdev created on 16.02.2019
 */
@Entity
@Table(name = "BILL_OF_MATERIAL_TXN_ITEM")
public class BillOfMaterialTxnItemModel extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public BillOfMaterialTxnItemModel() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOM_TXN_ID")
    private BillOfMaterialTxnModel billOfMaterialTxn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPENSE_ID")
    private Specifics expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VENDOR_ID")
    private Vendor vendor;

    @Column(name = "MEASURE_NAME")
    private String measureName;

    @Column(name = "OEM")
    private String oem;

    @Column(name = "NO_OF_UNITS")
    private Double noOfUnits;

    @Column(name = "PRICE_PER_UNIT")
    private Double pricePerUnit;

    @Column(name = "TOTAL_PRICE")
    private Double totalPrice;

    @Column(name = "TYPE_OF_MATERIAL")
    private String typeOfMaterial;

    @Column(name = "AVAILABLE_UNITS")
    private Double availableUnits;

    @Column(name = "COMMITTED_UNITS")
    private Double committedUnits;

    @Column(name = "ORDERED_UNITS")
    private Double orderedUnits;

    @Column(name = "NET_UNITS")
    private Double netUnits;

    @Column(name = "FULFILLED_UNITS")
    private Double fulfilledUnits;

    @Column(name = "EXPECTED_DATETIME")
    private Date expectedDatetime;

    @Column(name = "KLFOLLOW_STATUS")
    private Integer klfollowStatus;

    @Column(name = "DESTINATION_GSTIN")
    private String destinationGstin;

    @Column(name = "IS_FULFILLED")
    private Integer isFulfilled;

    @Column(name = "FULFILLED_TXN_REF_NO")
    private String fulfilledTxnRefNo;

    public BillOfMaterialTxnModel getBillOfMaterialTxn() {
        return this.billOfMaterialTxn;
    }

    public void setBillOfMaterialTxn(final BillOfMaterialTxnModel billOfMaterialTxn) {
        this.billOfMaterialTxn = billOfMaterialTxn;
    }

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }

    public Branch getBranch() {
        return this.branch;
    }

    public void setBranch(final Branch branch) {
        this.branch = branch;
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

    public Double getPricePerUnit() {
        return this.pricePerUnit;
    }

    public void setPricePerUnit(final Double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public Double getTotalPrice() {
        return this.totalPrice;
    }

    public void setTotalPrice(final Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getTypeOfMaterial() {
        return this.typeOfMaterial;
    }

    public void setTypeOfMaterial(final String typeOfMaterial) {
        this.typeOfMaterial = typeOfMaterial;
    }

    public Double getAvailableUnits() {
        return this.availableUnits;
    }

    public void setAvailableUnits(final Double availableUnits) {
        this.availableUnits = availableUnits;
    }

    public Double getCommittedUnits() {
        return this.committedUnits;
    }

    public void setCommittedUnits(final Double committedUnits) {
        this.committedUnits = committedUnits;
    }

    public Double getOrderedUnits() {
        return this.orderedUnits;
    }

    public void setOrderedUnits(final Double orderedUnits) {
        this.orderedUnits = orderedUnits;
    }

    public Double getNetUnits() {
        return this.netUnits;
    }

    public void setNetUnits(final Double netUnits) {
        this.netUnits = netUnits;
    }

    public Date getExpectedDatetime() {
        return this.expectedDatetime;
    }

    public void setExpectedDatetime(final Date expectedDatetime) {
        this.expectedDatetime = expectedDatetime;
    }

    public Integer getKlfollowStatus() {
        return this.klfollowStatus;
    }

    public void setKlfollowStatus(final Integer klfollowStatus) {
        this.klfollowStatus = klfollowStatus;
    }

    public String getDestinationGstin() {
        return this.destinationGstin;
    }

    public void setDestinationGstin(final String destinationGstin) {
        this.destinationGstin = destinationGstin;
    }

    public Integer getIsFulfilled() {
        return this.isFulfilled;
    }

    public void setIsFulfilled(final Integer isFulfilled) {
        this.isFulfilled = isFulfilled;
    }

    public Double getFulfilledUnits() {
        return this.fulfilledUnits;
    }

    public void setFulfilledUnits(final Double fulfilledUnits) {
        this.fulfilledUnits = fulfilledUnits;
    }

    public String getFulfilledTxnRefNo() {
        return this.fulfilledTxnRefNo;
    }

    public void setFulfilledTxnRefNo(final String fulfilledTxnRefNo) {
        this.fulfilledTxnRefNo = fulfilledTxnRefNo;
    }

    public static BillOfMaterialTxnItemModel findById(Long id) {
        return entityManager.find(BillOfMaterialTxnItemModel.class, id);
    }
}
