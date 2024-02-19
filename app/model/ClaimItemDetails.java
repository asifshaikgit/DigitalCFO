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

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "CLAIM_DETAILS")
public class ClaimItemDetails extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public ClaimItemDetails() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final long serialVersionUID = -2638200201279328082L;
	private static final String BY_ID_JPQL = "select obj from ClaimItemDetails obj where obj.id= ?1 and obj.presentStatus=1";
	private static final String BY_CLAIM_ID = "select obj from ClaimItemDetails obj where obj.transaction.id = ?1 and obj.presentStatus=1";
	private static final String BY_SETTL_ID = "select obj from ClaimItemDetails obj where obj.settlementId = ?1 and obj.presentStatus=1";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ID")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CLAIM_ID")
	private ClaimTransaction transaction;

	@Column(name = "CLAIM_SPECIFICS")
	private Long claimSpecific;

	@Column(name = "SETTLEMENT_ID")
	private Long settlementId;

	@Column(name = "VENDOR_NAME")
	private String vendorName;

	@Column(name = "VENDOR_GSTIN")
	private String vendorGstin;

	@Column(name = "VENDOR_STATE")
	private String vendorState;

	@Column(name = "IS_REGISTERED")
	private Integer isRegistered;

	@Column(name = "INV_BILL_REF_NUMBER")
	private String invoiceBillRefNo;

	@Column(name = "INV_BILL_REF_DATE")
	private Date invoiceBillRefDate;

	@Column(name = "ITEM_SERVICE_NAME")
	private String itemServiceName;

	@Column(name = "HSN_SAC_CODE")
	private String hsnOrSacCode;

	@Column(name = "PRODUCT_SERVICE_DESC")
	private String productServiceDesc;

	@Column(name = "UQC")
	private String uqc;

	@Column(name = "QUANTITY")
	private Double quantity;

	@Column(name = "RATE")
	private Double rate;

	@Column(name = "GROSS_AMOUNT")
	private Double grossAmt;

	@Column(name = "SGST_TAX_ID")
	private Long sgstId;

	@Column(name = "SGST_RATE")
	private Double sgstRate;

	@Column(name = "SGST_AMOUNT")
	private Double sgstAmt;

	@Column(name = "CGST_TAX_ID")
	private Long cgstId;

	@Column(name = "CGST_RATE")
	private Double cgstRate;

	@Column(name = "CGST_AMOUNT")
	private Double cgstAmt;

	@Column(name = "IGST_TAX_ID")
	private Long igstId;

	@Column(name = "IGST_RATE")
	private Double igstRate;

	@Column(name = "IGST_AMOUNT")
	private Double igstAmt;

	@Column(name = "CESS_TAX_ID")
	private Long cessId;

	@Column(name = "CESS_RATE")
	private Double cessRate;

	@Column(name = "CESS_AMOUNT")
	private Double cessAmt;

	@Column(name = "NET_AMOUNT")
	private Double netAmount;

	@Column(name = "ITEM_CATEGORY")
	private String itemCategory;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public ClaimTransaction getTransaction() {
		return transaction;
	}

	public void setTransaction(ClaimTransaction transaction) {
		this.transaction = transaction;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVendorGstin() {
		return vendorGstin;
	}

	public void setVendorGstin(String vendorGstin) {
		this.vendorGstin = vendorGstin;
	}

	public String getVendorState() {
		return vendorState;
	}

	public void setVendorState(String vendorState) {
		this.vendorState = vendorState;
	}

	public Integer getIsRegistered() {
		return isRegistered;
	}

	public void setIsRegistered(Integer isRegistered) {
		this.isRegistered = isRegistered;
	}

	public String getInvoiceBillRefNo() {
		return invoiceBillRefNo;
	}

	public void setInvoiceBillRefNo(String invoiceBillRefNo) {
		this.invoiceBillRefNo = invoiceBillRefNo;
	}

	public Date getInvoiceBillRefDate() {
		return invoiceBillRefDate;
	}

	public void setInvoiceBillRefDate(Date invoiceBillRefDate) {
		this.invoiceBillRefDate = invoiceBillRefDate;
	}

	public String getItemServiceName() {
		return itemServiceName;
	}

	public void setItemServiceName(String itemServiceName) {
		this.itemServiceName = itemServiceName;
	}

	public String getHsnOrSacCode() {
		return hsnOrSacCode;
	}

	public void setHsnOrSacCode(String hsnOrSacCode) {
		this.hsnOrSacCode = hsnOrSacCode;
	}

	public String getProductServiceDesc() {
		return productServiceDesc;
	}

	public void setProductServiceDesc(String productServiceDesc) {
		this.productServiceDesc = productServiceDesc;
	}

	public String getUqc() {
		return uqc;
	}

	public void setUqc(String uqc) {
		this.uqc = uqc;
	}

	public Double getQuantity() {
		return quantity;
	}

	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public Double getGrossAmt() {
		return grossAmt;
	}

	public void setGrossAmt(Double grossAmt) {
		this.grossAmt = grossAmt;
	}

	public Double getCgstRate() {
		return cgstRate;
	}

	public void setCgstRate(Double cgstRate) {
		this.cgstRate = cgstRate;
	}

	public Double getCgstAmt() {
		return cgstAmt;
	}

	public void setCgstAmt(Double cgstAmt) {
		this.cgstAmt = cgstAmt;
	}

	public Double getSgstRate() {
		return sgstRate;
	}

	public void setSgstRate(Double sgstRate) {
		this.sgstRate = sgstRate;
	}

	public Double getSgstAmt() {
		return sgstAmt;
	}

	public void setSgstAmt(Double sgstAmt) {
		this.sgstAmt = sgstAmt;
	}

	public Double getIgstRate() {
		return igstRate;
	}

	public void setIgstRate(Double igstRate) {
		this.igstRate = igstRate;
	}

	public Double getIgstAmt() {
		return igstAmt;
	}

	public void setIgstAmt(Double igstAmt) {
		this.igstAmt = igstAmt;
	}

	public Double getCessRate() {
		return cessRate;
	}

	public void setCessRate(Double cessRate) {
		this.cessRate = cessRate;
	}

	public Double getCessAmt() {
		return cessAmt;
	}

	public void setCessAmt(Double cessAmt) {
		this.cessAmt = cessAmt;
	}

	public String getItemCategory() {
		return itemCategory;
	}

	public void setItemCategory(String itemCategory) {
		this.itemCategory = itemCategory;
	}

	public Long getSgstId() {
		return this.sgstId;
	}

	public void setSgstId(Long sgstId) {
		this.sgstId = sgstId;
	}

	public Long getCgstId() {
		return this.cgstId;
	}

	public void setCgstId(Long cgstId) {
		this.cgstId = cgstId;
	}

	public Long getIgstId() {
		return this.igstId;
	}

	public void setIgstId(Long igstId) {
		this.igstId = igstId;
	}

	public Long getCessId() {
		return this.cessId;
	}

	public void setCessId(Long cessId) {
		this.cessId = cessId;
	}

	public Long getClaimSettlementId() {
		return settlementId;
	}

	public void setClaimSettlementId(Long claimSettlementId) {
		this.settlementId = claimSettlementId;
	}

	public Long getClaimSpecific() {
		return claimSpecific;
	}

	public void setClaimSpecific(Long claimSpecific) {
		this.claimSpecific = claimSpecific;
	}

	public Double getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(Double netAmt) {
		this.netAmount = netAmt;
	}

	public static ClaimItemDetails findById(EntityManager entityManager, Long id) {
		Query query = entityManager.createQuery(BY_ID_JPQL);
		query.setParameter(1, id);
		ClaimItemDetails obj = (ClaimItemDetails) query.getSingleResult();
		return obj;
	}

	public static List<ClaimItemDetails> findByClaimID(EntityManager entityManager, long claimid) {
		Query query = entityManager.createQuery(BY_CLAIM_ID);
		query.setParameter(1, claimid);
		List<ClaimItemDetails> claimItems = query.getResultList();
		return claimItems;
	}

	public static List<ClaimItemDetails> findBySettlementID(EntityManager entityManager, long id) {
		Query query = entityManager.createQuery(BY_SETTL_ID);
		query.setParameter(1, id);
		List<ClaimItemDetails> claimItems = query.getResultList();
		return claimItems;
	}
}
