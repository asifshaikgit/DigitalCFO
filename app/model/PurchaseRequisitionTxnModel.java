package model;

import com.idos.util.IdosConstants;
import com.idos.util.IdosDaoConstants;
import play.db.jpa.JPAApi;
import service.EntityManagerProvider;

import javax.persistence.Query;
import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @author Harish Kumar created on 01.05.2023
 */
@Entity
@Table(name="PURCHASE_REQUISITION_TXN")
public class PurchaseRequisitionTxnModel extends AbstractBaseModel {

    private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public PurchaseRequisitionTxnModel() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

    private static final String  BOM_JPQL = "from PurchaseRequisitionTxnModel obj WHERE obj.organization.id = ?1 and obj.branch.id= ?2 and obj.transactionPurpose.id in (?3) and obj.transactionStatus = ?4 and obj.isFulfilled = ?5 and obj.presentStatus=1";

    private static final String  BOM_UNFULFILLED_JPQL = "from PurchaseRequisitionTxnModel obj WHERE obj.organization.id = ?1 and obj.branch.id= ?2 and obj.transactionPurpose.id in (?3) and obj.transactionStatus = ?4 and (obj.isFulfilled = ?5 or obj.isFulfilled is null) and obj.presentStatus=1";

    private static final  String  BOM_DATE_RANGE_JPQL = "from PurchaseRequisitionTxnModel obj WHERE obj.organization.id = ?1 and obj.branch.id= ?2 and obj.transactionPurpose.id in (?3) and obj.transactionStatus = ?4 and obj.isFulfilled = ?5 and (obj.actionDate between ?6 and ?7) and obj.presentStatus=1";

    private static final String BOM_DATE_RANGE_UNFULFILLED_JPQL = "from PurchaseRequisitionTxnModel obj WHERE obj.organization.id = ?1 and obj.branch.id= ?2 and obj.transactionPurpose.id in (?3) and obj.transactionStatus = ?4 and (obj.isFulfilled = ?5 or obj.isFulfilled is null) and (obj.actionDate between ?6 and ?7) and obj.presentStatus=1";

    private static final String TXN_REF_HQL = "select obj from PurchaseRequisitionTxnModel obj where obj.organization.id = ?1 and obj.transactionRefNumber= ?2 and obj.presentStatus=1";

    private static final String PUR_REQ_ACTIVE_TXN_ORG = "select obj from PurchaseRequisitionTxnModel obj where obj.organization.id = ?1 and obj.presentStatus=1";

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="BRANCH_ID")
    private Branch branch;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PROJECT_ID")
    private Project project;

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

    @Column(name="BOM_SETUP_ID")
    private Double bomSetupId;

    @OneToMany(fetch=FetchType.LAZY, mappedBy="purchaseRequisitionTxn")
    private List<PurchaseRequisitionTxnItemModel> purchaseRequisitionTxnItemModels;

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

    public List<PurchaseRequisitionTxnItemModel> getPurchaseRequisitionTxnItemModels() {
        return this.purchaseRequisitionTxnItemModels;
    }

    public void setPurchaseRequisitionTxnItemModels(final List<PurchaseRequisitionTxnItemModel> purchaseRequisitionTxnItemModels) {
        this.purchaseRequisitionTxnItemModels = purchaseRequisitionTxnItemModels;
    }

    public Integer getTypeIdentifier() {
        return this.typeIdentifier;
    }

    public void setTypeIdentifier(final Integer typeIdentifier) {
        this.typeIdentifier = typeIdentifier;
    }

    public Double getBomSetupId() {
        return this.bomSetupId;
    }

    public void setBomSetupId(final Double bomSetupId) {
        this.bomSetupId = bomSetupId;
    }



    public static PurchaseRequisitionTxnModel findById(Long id) {
        return entityManager.find(PurchaseRequisitionTxnModel.class, id);
    }
    public static List<PurchaseRequisitionTxnModel> findPRByOrgIdAndBranchIdAndProjectId(long orgId, long branchId, long projectId) {
        List<PurchaseRequisitionTxnModel> list = null;
        Query query = entityManager.createQuery("from PurchaseRequisitionTxnModel obj WHERE obj.organization.id = ?1 and obj.branch.id= ?2 and obj.project.id= ?3 OR obj.project.id IS NULL and obj.presentStatus=1");
        
        query.setParameter(IdosDaoConstants.P1, orgId);
        query.setParameter(IdosDaoConstants.P2, branchId);
        query.setParameter(IdosDaoConstants.P3, projectId == 0 ? null : projectId);
        list = query.getResultList();
        return list;
            
    }
    public static List<PurchaseRequisitionTxnModel> findByOrgBranchBomTransaction(long orgid, long branchid, long transacrionPurposeId, String txnStatus, int isFulfilled) {
        String sql = null;
        if(IdosConstants.UN_FULFILLED_TRANACTION == isFulfilled)
            sql = BOM_UNFULFILLED_JPQL;
        else
            sql = BOM_JPQL;
        List<PurchaseRequisitionTxnModel> list = null;
        Query query = entityManager.createQuery(sql);
        query.setParameter(IdosDaoConstants.P1, orgid);
        query.setParameter(IdosDaoConstants.P2, branchid);
        query.setParameter(IdosDaoConstants.P3, transacrionPurposeId);
        query.setParameter(IdosDaoConstants.P4, txnStatus);
        query.setParameter(IdosDaoConstants.P5, isFulfilled);
        list = query.getResultList();
        return list;
    }

    public static List<PurchaseRequisitionTxnModel> findByOrgBranchBomTxnByDateRange(long orgid, long branchid, long transacrionPurposeId, String txnStatus, int isFulfilled, Date fromDate, Date toDate) {
        String sql = null;
        if(IdosConstants.UN_FULFILLED_TRANACTION == isFulfilled)
            sql = BOM_DATE_RANGE_UNFULFILLED_JPQL;
        else
            sql = BOM_DATE_RANGE_JPQL;
        List<PurchaseRequisitionTxnModel> list = null;
        Query query = entityManager.createQuery(sql);
        query.setParameter(IdosDaoConstants.P1, orgid);
        query.setParameter(IdosDaoConstants.P2, branchid);
        query.setParameter(IdosDaoConstants.P3, transacrionPurposeId);
        query.setParameter(IdosDaoConstants.P4, txnStatus);
        query.setParameter(IdosDaoConstants.P5, isFulfilled);
        query.setParameter(IdosDaoConstants.P6, fromDate);
        query.setParameter(IdosDaoConstants.P7, toDate);
        list = query.getResultList();
        return list;
    }

    public static PurchaseRequisitionTxnModel findByBomTxnReference(Long orgid, String txnRefNo) {
        List<PurchaseRequisitionTxnModel> list = null;
        PurchaseRequisitionTxnModel bomTxn = null;
        Query query = entityManager.createQuery(TXN_REF_HQL);
        query.setParameter(IdosDaoConstants.P1, orgid);
        query.setParameter(IdosDaoConstants.P2, txnRefNo);
        list = query.getResultList();
        if(list != null && list.size() > 0){
            bomTxn = list.get(0);
        }
        return bomTxn;
    }

    public static List<PurchaseRequisitionTxnModel> getAllActivePurchaseRequisitionTxnByOrg(Long orgid) {
        List<PurchaseRequisitionTxnModel> list = null;
        Query query = entityManager.createQuery(PUR_REQ_ACTIVE_TXN_ORG);
        query.setParameter(IdosDaoConstants.P1, orgid);
        list = query.getResultList();
        return list;
    }
}
