package model;

import service.EntityManagerProvider;
import model.Specifics;
import model.VendorDetail;
import model.Vendor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import com.idos.dao.GenericDAO;

@Entity
@Table(name = "VENDOR_BRANCHWISE_ADVANCE_BAL")
public class VendorBranchWiseAdvBalance extends AbstractBaseModel {

    private static EntityManager entityManager;

    private static final String BY_OGR_CUST_BRANCH_JPQL = "select DISTINCT(obj.branch) from VendorBranchWiseAdvBalance obj where obj.organization.id= ?1 and obj.vendor.id = ?2 and obj.presentStatus =1";

    private static final String GET_ADV_AMT_FOR_TRX = "select obj from VendorBranchWiseAdvBalance obj where obj.organization.id= ?1 and obj.vendor.id =?2 and obj.branch.id =?3 and obj.typeOfSupply =?4 and obj.vendorDetail.id =?5 and obj.specifics.id =?6 and obj.presentStatus =1";

    private static final String BRANCH_ADV_OPENING_BALANCE_JPQL = "select obj from VendorBranchWiseAdvBalance obj where obj.organization.id= ?1 and branch.id = ?2 and obj.vendor.id = ?3 and obj.receiptDate <= ?4 and obj.presentStatus = 1";

    private static final String BRANCH_OB_PAID_AMT_JPQL = "select sum(t2.vendorNetPayment) from Transaction t2 where t2.transactionBranchOrganization.id = ?1 and t2.transactionBranch.id = ?2 and t2.transactionVendorCustomer.id = ?3 and t2.transactionPurpose.id = ?4 and t2.typeIdentifier = ?5 and t2.presentStatus = 1 group by t2.paidInvoiceRefNumber";

    public VendorBranchWiseAdvBalance() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VENDOR_ID")
    private Vendor vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @Column(name = "RECEIPT_NUMBER")
    private String receiptNo;

    @Column(name = "RECEIPT_DATE")
    public Date receiptDate;

    @Column(name = "ADVANCE_AMOUNT")
    private Double advanceAmount;

    @Column(name = "TYPE_OF_SUPPLY")
    private Integer typeOfSupply;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VENDOR_DETAIL_ID")
    private VendorDetail vendorDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SPECIFICS_ID")
    private Specifics specifics;

    @Column(name = "OPENING_BALANCE")
    private Double openingBalance;

    public Double getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(Double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public Date getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(Date receiptDate) {
        this.receiptDate = receiptDate;
    }

    public Double getAdvanceAmount() {
        return advanceAmount;
    }

    public void setAdvanceAmount(Double advanceAmount) {
        this.advanceAmount = advanceAmount;
    }

    public Integer getTypeOfSupply() {
        return typeOfSupply;
    }

    public void setTypeOfSupply(Integer typeOfSupply) {
        this.typeOfSupply = typeOfSupply;
    }

    public VendorDetail getVendrDetail() {
        return vendorDetail;
    }

    public void setVendorDetail(VendorDetail vendorDetail) {
        this.vendorDetail = vendorDetail;
    }

    public Specifics getSpecifics() {
        return specifics;
    }

    public void setSpecifics(Specifics specifics) {
        this.specifics = specifics;
    }

    public static VendorBranchWiseAdvBalance findById(Long id) {
        return entityManager.find(VendorBranchWiseAdvBalance.class, id);
    }

    public static List<Branch> findBranchWiseAdvanceBalance(EntityManager entityManager, Long orgId, Long vendId) {
        Query query = entityManager.createQuery(BY_OGR_CUST_BRANCH_JPQL);
        query.setParameter(1, orgId);
        query.setParameter(2, vendId);
        List<Branch> resultList = query.getResultList();
        return resultList;
    }

    public static VendorBranchWiseAdvBalance getAdvAmountForItem(EntityManager entityManager, Long orgId,
            Long vendId, Long branchId, int typeOfSupply, Long placeOfSupply, Long item) {
        VendorBranchWiseAdvBalance itemAdvAmount = null;
        Query query = entityManager.createQuery(GET_ADV_AMT_FOR_TRX);
        query.setParameter(1, orgId);
        query.setParameter(2, vendId);
        query.setParameter(3, branchId);
        query.setParameter(4, typeOfSupply);
        query.setParameter(5, placeOfSupply);
        query.setParameter(6, item);
        try {
            itemAdvAmount = (VendorBranchWiseAdvBalance) query.getSingleResult();
        } catch (Exception e) {
            itemAdvAmount = null;
        }
        return itemAdvAmount;
    }

    public static List<VendorBranchWiseAdvBalance> findOpeningBalance(EntityManager entityManager, Long orgId,
            Long vendId, Long branchId, Date date) {
        Query query = entityManager.createQuery(BRANCH_ADV_OPENING_BALANCE_JPQL);
        query.setParameter(1, orgId);
        query.setParameter(2, branchId);
        query.setParameter(3, vendId);
        query.setParameter(4, date);
        List<VendorBranchWiseAdvBalance> resultList = query.getResultList();
        return resultList;
    }

}
