package model;

import com.idos.util.IdosConstants;
import com.idos.util.IdosDaoConstants;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @author Sunil K Namdev created on 16.02.2019
 */
@Entity
@Table(name = "BILL_OF_MATERIAL_TXN")
public class BillOfMaterialTxnModel extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public BillOfMaterialTxnModel() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static final String BOM_JPQL = "from BillOfMaterialTxnModel obj WHERE obj.organization.id = ?1 and obj.branch.id= ?2 and obj.transactionPurpose.id in (?3) and obj.transactionStatus = ?4 and obj.isFulfilled = ?5 and obj.presentStatus=1";

    private static final String BOM_UNFULFILLED_JPQL = "from BillOfMaterialTxnModel obj WHERE obj.organization.id = ?1 and obj.branch.id= ?2 and obj.transactionPurpose.id in (?3) and obj.transactionStatus = ?4 and (obj.isFulfilled = ?5 or obj.isFulfilled is null) and obj.presentStatus=1";

    private static final String BOM_DATE_RANGE_JPQL = "from BillOfMaterialTxnModel obj WHERE obj.organization.id = ?1 and obj.branch.id= ?2 and obj.transactionPurpose.id in (?3) and obj.transactionStatus = ?4 and obj.isFulfilled = ?5 and (obj.actionDate between ?6 and ?7) and obj.presentStatus=1";

    private static final String BOM_DATE_RANGE_UNFULFILLED_JPQL = "from BillOfMaterialTxnModel obj WHERE obj.organization.id = ?1 and obj.branch.id= ?2 and obj.transactionPurpose.id in (?3) and obj.transactionStatus = ?4 and (obj.isFulfilled = ?5 or obj.isFulfilled is null) and (obj.actionDate between ?6 and ?7) and obj.presentStatus=1";

    private static final String TXN_REF_HQL = "select obj from BillOfMaterialTxnModel obj where obj.organization.id = ?1 and obj.transactionRefNumber= ?2 and obj.presentStatus=1";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INCOME_ID")
    private Specifics income;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER_VENDOR_ID")
    private Vendor customerVendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_PURPOSE")
    private TransactionPurpose transactionPurpose;

    @Column(name = "SOURCE_GSTIN")
    private String sourceGstin;

    @Column(name = "DESTINATION_GSTIN")
    private String destinationGstin;

    @Column(name = "ACTION_DATE")
    private Date actionDate;

    @Column(name = "INCOME_NO_OF_UNITS")
    private Double incomeNoOfUnits;

    @Column(name = "TOTAL_NO_OF_UNITS")
    private Double totalNoOfUnits;

    @Column(name = "TOTAL_AMOUNT")
    private Double totalAmount;

    @Column(name = "TOTAL_NET_AMOUNT")
    private Double totalNetAmount;

    @Column(name = "TRANSACTION_STATUS")
    private String transactionStatus;

    @Column(name = "TRANSACTION_REF_NUMBER")
    private String transactionRefNumber;

    @Column(name = "LINKED_REF_NO")
    private String linkedRefNumber;

    @Column(name = "DOCUMENT_REF")
    private String documentRef;

    @Column(name = "APPROVER_EMAILS")
    private String approverEmails;

    @Column(name = "ADDITIONAL_APPROVER_USER_EMAILS")
    private String additionalApproverEmails;

    @Column(name = "SELECTED_ADDITIONAL_APPROVER")
    private String selectedAdditionalApprover;

    @ManyToOne
    @JoinColumn(name = "APPROVER_ACTION_BY")
    private Users approverActionBy;

    @Column(name = "REMARKS")
    private String remarks;

    @Column(name = "PRIVATE_REMARKS")
    private String privateRemarks;

    @Column(name = "SUPPORTING_DOCS")
    private String supportingDocs;

    @Column(name = "IS_FULFILLED")
    private Integer isFulfilled;

    @Column(name = "FULFILLED_UNITS")
    private Double fulfilledUnits;

    @Column(name = "INVOICE_NUMBER")
    private String invoiceNumber;

    @Column(name = "TYPE_IDENTIFIER")
    private Integer typeIdentifier;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "billOfMaterialTxn")
    private List<BillOfMaterialTxnItemModel> billOfMaterialTxnItemModels;

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

    public Project getProject() {
        return this.project;
    }

    public void setProject(final Project project) {
        this.project = project;
    }

    public Specifics getIncome() {
        return this.income;
    }

    public void setIncome(final Specifics income) {
        this.income = income;
    }

    public Vendor getCustomerVendor() {
        return this.customerVendor;
    }

    public void setCustomerVendor(final Vendor customerVendor) {
        this.customerVendor = customerVendor;
    }

    public TransactionPurpose getTransactionPurpose() {
        return this.transactionPurpose;
    }

    public void setTransactionPurpose(final TransactionPurpose transactionPurpose) {
        this.transactionPurpose = transactionPurpose;
    }

    public String getSourceGstin() {
        return this.sourceGstin;
    }

    public void setSourceGstin(final String sourceGstin) {
        this.sourceGstin = sourceGstin;
    }

    public String getDestinationGstin() {
        return this.destinationGstin;
    }

    public void setDestinationGstin(final String destinationGstin) {
        this.destinationGstin = destinationGstin;
    }

    public Date getActionDate() {
        return this.actionDate;
    }

    public void setActionDate(final Date actionDate) {
        this.actionDate = actionDate;
    }

    public Double getIncomeNoOfUnits() {
        return this.incomeNoOfUnits;
    }

    public void setIncomeNoOfUnits(final Double incomeNoOfUnits) {
        this.incomeNoOfUnits = incomeNoOfUnits;
    }

    public Double getTotalNoOfUnits() {
        return this.totalNoOfUnits;
    }

    public void setTotalNoOfUnits(final Double totalNoOfUnits) {
        this.totalNoOfUnits = totalNoOfUnits;
    }

    public Double getTotalAmount() {
        return this.totalAmount;
    }

    public void setTotalAmount(final Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getTotalNetAmount() {
        return this.totalNetAmount;
    }

    public void setTotalNetAmount(final Double totalNetAmount) {
        this.totalNetAmount = totalNetAmount;
    }

    public String getTransactionStatus() {
        return this.transactionStatus;
    }

    public void setTransactionStatus(final String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getTransactionRefNumber() {
        return this.transactionRefNumber;
    }

    public void setTransactionRefNumber(final String transactionRefNumber) {
        this.transactionRefNumber = transactionRefNumber;
    }

    public String getLinkedRefNumber() {
        return this.linkedRefNumber;
    }

    public void setLinkedRefNumber(final String linkedRefNumber) {
        this.linkedRefNumber = linkedRefNumber;
    }

    public String getDocumentRef() {
        return this.documentRef;
    }

    public void setDocumentRef(final String documentRef) {
        this.documentRef = documentRef;
    }

    public String getApproverEmails() {
        return this.approverEmails;
    }

    public void setApproverEmails(final String approverEmails) {
        this.approverEmails = approverEmails;
    }

    public String getAdditionalApproverEmails() {
        return this.additionalApproverEmails;
    }

    public void setAdditionalApproverEmails(final String additionalApproverEmails) {
        this.additionalApproverEmails = additionalApproverEmails;
    }

    public String getSelectedAdditionalApprover() {
        return this.selectedAdditionalApprover;
    }

    public void setSelectedAdditionalApprover(final String selectedAdditionalApprover) {
        this.selectedAdditionalApprover = selectedAdditionalApprover;
    }

    public Users getApproverActionBy() {
        return this.approverActionBy;
    }

    public void setApproverActionBy(final Users approverActionBy) {
        this.approverActionBy = approverActionBy;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(final String remarks) {
        this.remarks = remarks;
    }

    public String getPrivateRemarks() {
        return this.privateRemarks;
    }

    public void setPrivateRemarks(final String privateRemarks) {
        this.privateRemarks = privateRemarks;
    }

    public String getSupportingDocs() {
        return this.supportingDocs;
    }

    public void setSupportingDocs(final String supportingDocs) {
        this.supportingDocs = supportingDocs;
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

    public String getInvoiceNumber() {
        return this.invoiceNumber;
    }

    public void setInvoiceNumber(final String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public List<BillOfMaterialTxnItemModel> getBillOfMaterialTxnItemModels() {
        return this.billOfMaterialTxnItemModels;
    }

    public void setBillOfMaterialTxnItemModels(final List<BillOfMaterialTxnItemModel> billOfMaterialTxnItemModels) {
        this.billOfMaterialTxnItemModels = billOfMaterialTxnItemModels;
    }

    public Integer getTypeIdentifier() {
        return this.typeIdentifier;
    }

    public void setTypeIdentifier(final Integer typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    public static BillOfMaterialTxnModel findById(Long id) {
        return entityManager.find(BillOfMaterialTxnModel.class, id);
    }

    public static List<BillOfMaterialTxnModel> findByOrgBranchBomTransaction(EntityManager entityManager, long orgid,
            long branchid, long transacrionPurposeId, String txnStatus, int isFulfilled) {
        String sql = null;
        if (IdosConstants.UN_FULFILLED_TRANACTION == isFulfilled)
            sql = BOM_UNFULFILLED_JPQL;
        else
            sql = BOM_JPQL;
        List<BillOfMaterialTxnModel> list = null;
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, orgid);
        query.setParameter(2, branchid);
        query.setParameter(3, transacrionPurposeId);
        query.setParameter(4, txnStatus);
        query.setParameter(5, isFulfilled);
        list = query.getResultList();
        return list;
    }

    public static List<BillOfMaterialTxnModel> findByOrgBranchBomTxnByDateRange(EntityManager entityManager, long orgid,
            long branchid, long transacrionPurposeId, String txnStatus, int isFulfilled, Date fromDate, Date toDate) {
        String sql = null;
        if (IdosConstants.UN_FULFILLED_TRANACTION == isFulfilled)
            sql = BOM_DATE_RANGE_UNFULFILLED_JPQL;
        else
            sql = BOM_DATE_RANGE_JPQL;
        List<BillOfMaterialTxnModel> list = null;
        Query query = entityManager.createQuery(sql);
        query.setParameter(1, orgid);
        query.setParameter(2, branchid);
        query.setParameter(3, transacrionPurposeId);
        query.setParameter(4, txnStatus);
        query.setParameter(5, isFulfilled);
        query.setParameter(6, fromDate);
        query.setParameter(7, toDate);
        list = query.getResultList();
        return list;
    }

    public static BillOfMaterialTxnModel findByBomTxnReference(EntityManager entityManager, Long orgid,
            String txnRefNo) {
        List<BillOfMaterialTxnModel> list = null;
        BillOfMaterialTxnModel bomTxn = null;
        Query query = entityManager.createQuery(TXN_REF_HQL);
        query.setParameter(1, orgid);
        query.setParameter(2, txnRefNo);
        list = query.getResultList();
        if (list != null && list.size() > 0) {
            bomTxn = list.get(0);
        }
        return bomTxn;
    }
}
