package model;

import com.idos.util.IdosConstants;
import com.idos.util.IdosDaoConstants;
import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import javax.persistence.Query;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @author Harish Kumar created on 30.05.2023
 */
@Entity
@Table(name="PURCHASE_ORDER_TXN")
public class PurchaseOrderTxnModel extends AbstractBaseModel {

    private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public PurchaseOrderTxnModel() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

    public static List<PurchaseOrderTxnModel> findSubPObyOrgIdAndBranchIdAndProjectId(long orgId, long branchId, long projectId) {
        List<PurchaseOrderTxnModel> list = null;
        Query query = entityManager.createQuery("from PurchaseOrderTxnModel obj WHERE obj.organization.id = ?1 and obj.branch.id= ?2 and obj.project.id= ?3 OR obj.project.id IS NULL and obj.presentStatus=1 and isParent != 0");
        
        query.setParameter(IdosDaoConstants.P1, orgId);
        query.setParameter(IdosDaoConstants.P2, branchId);
        query.setParameter(IdosDaoConstants.P3, projectId == 0 ? null : projectId);
        list = query.getResultList();
        return list;
            
    }

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="BRANCH_ID")
    private Branch branch;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PROJECT_ID")
    private Project project;

    @Column(name="PURCHASE_REQUISITION_ID")
    private Double purchaseRequisitionId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="TRANSACTION_PURPOSE")
    private TransactionPurpose transactionPurpose;

    @Column(name="ACTION_DATE")
    private Date actionDate;

    @Column(name="TOTAL_NO_OF_UNITS")
    private Double totalNoOfUnits;

    @Column(name="TOTAL_AMOUNT")
    private Double totalAmount;

    @Column(name="TOTAL_NET_AMOUNT")
    private Double totalNetAmount;

    @Column(name="TRANSACTION_STATUS")
    private String transactionStatus;

    @Column(name="TRANSACTION_REF_NUMBER")
    private String transactionRefNumber;

    @Column(name="LINKED_REF_NO")
    private String linkedRefNumber;

    @Column(name="DOCUMENT_REF")
    private String documentRef;

    @Column(name="APPROVER_EMAILS")
    private String approverEmails;

    @Column(name="ADDITIONAL_APPROVER_USER_EMAILS")
    private String additionalApproverEmails;

    @Column(name="SELECTED_ADDITIONAL_APPROVER")
    private String selectedAdditionalApprover;

    @ManyToOne
    @JoinColumn(name="APPROVER_ACTION_BY")
    private Users approverActionBy;

    @Column(name="REMARKS")
    private String remarks;

    @Column(name="PRIVATE_REMARKS")
    private String privateRemarks;

    @Column(name="SUPPORTING_DOCS")
    private String supportingDocs;

    @Column(name="IS_FULFILLED")
    private Integer isFulfilled;

    @Column(name="FULFILLED_UNITS")
    private Double fulfilledUnits;

    @Column(name="INVOICE_NUMBER")
    private String invoiceNumber;

    @Column(name="TYPE_IDENTIFIER")
    private Integer typeIdentifier;

    @Column(name="PARENT_PURCHASE_ORDER")
    private Long parentPurchaseOrder;

    @Column(name="IS_PARENT")
    private Integer isParent;

    @OneToMany(fetch=FetchType.LAZY, mappedBy="purchaseOrderTxn")
    private List<PurchaseOrderTxnItemModel> purchaseOrderTxnItemModels;

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

    public Double getPurchaseRequisitionId() {
        return this.purchaseRequisitionId;
    }

    public void setPurchaseRequisitionId(final Double purchaseRequisitionId) {
        this.purchaseRequisitionId = purchaseRequisitionId;
    }

    public TransactionPurpose getTransactionPurpose() {
        return this.transactionPurpose;
    }

    public void setTransactionPurpose(final TransactionPurpose transactionPurpose) {
        this.transactionPurpose = transactionPurpose;
    }

    public Date getActionDate() {
        return this.actionDate;
    }

    public void setActionDate(final Date actionDate) {
        this.actionDate = actionDate;
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

    public List<PurchaseOrderTxnItemModel> getPurchaseOrderTxnItemModels() {
        return this.purchaseOrderTxnItemModels;
    }

    public void setPurchaseOrderTxnItemModels(final List<PurchaseOrderTxnItemModel> purchaseOrderTxnItemModels) {
        this.purchaseOrderTxnItemModels = purchaseOrderTxnItemModels;
    }

    public Integer getTypeIdentifier() {
        return this.typeIdentifier;
    }

    public void setTypeIdentifier(final Integer typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    public void setParentPurchaseOrder(final Long parentPurchaseOrder) {
        this.parentPurchaseOrder = parentPurchaseOrder;
    }

    public Long getParentPurchaseOrder() {
        return this.parentPurchaseOrder;
    }

    public Integer getIsParent() {
        return this.isParent;
    }

    public void setIsParent(final Integer isParent) {
        this.isParent = isParent;
    }

    public static PurchaseOrderTxnModel findById(Long id) {
        return entityManager.find(PurchaseOrderTxnModel.class, id);
    }
    
}

