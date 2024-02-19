package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.*;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @author Sunil K. Namdev created on 29.01.2019
 */
@Entity
@Table(name = "BILL_OF_MATERIAL_ITEM")
public class BillOfMaterialItemModel extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public BillOfMaterialItemModel() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BILL_OF_MATERIAL_ID")
    private BillOfMaterialModel billOfMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VENDOR_ID")
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXPENSE_ID")
    private Specifics expense;

    @Column(name = "MEASURE_NAME")
    private String measureName;

    @Column(name = "OEM")
    private String oem;

    @Column(name = "NO_OF_UNITS")
    private Double noOfUnits;

    @Column(name = "TYPE_OF_MATERIAL")
    private String typeOfMaterial;

    @Column(name = "KNOWLEDGE_LIB")
    private String knowledgeLib;

    public BillOfMaterialModel getBillOfMaterial() {
        return this.billOfMaterial;
    }

    public void setBillOfMaterial(final BillOfMaterialModel billOfMaterial) {
        this.billOfMaterial = billOfMaterial;
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

    public Vendor getVendor() {
        return this.vendor;
    }

    public void setVendor(final Vendor vendor) {
        this.vendor = vendor;
    }

    public Specifics getExpense() {
        return this.expense;
    }

    public void setExpense(final Specifics expense) {
        this.expense = expense;
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

    public String getKnowledgeLib() {
        return this.knowledgeLib;
    }

    public void setKnowledgeLib(final String knowledgeLib) {
        this.knowledgeLib = knowledgeLib;
    }

    public static BillOfMaterialItemModel findById(Long id) {
        return entityManager.find(BillOfMaterialItemModel.class, id);
    }
}
