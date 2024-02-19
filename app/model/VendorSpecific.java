package model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;

@Entity
@Table(name = "VENDOR_has_SPECIFICS")
public class VendorSpecific extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public VendorSpecific() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String VEND_SPECIFIC_JQL = "select obj from VendorSpecific obj WHERE obj.organization.id=?1 and obj.branch.id=?2 and obj.specificsVendors.id=?3 and obj.vendorSpecific.id=?4 and obj.presentStatus = 1";
	private static final String ORG_SPECIFIC_JQL = "select obj from VendorSpecific obj WHERE obj.organization.id=?1 and obj.vendorSpecific.id=?2 and obj.presentStatus = 1";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id")
	private Vendor vendorSpecific;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specifics_id")
	private Specifics specificsVendors;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specifics_particulars_id")
	private Particulars particulars;

	@Column(name = "unit_price")
	private Double unitPrice;

	@Column(name = "advance_money")
	private Double advanceMoney;

	@Column(name = "BRANCH_SPECIFICS_TAX_FORMULA_ID")
	private Long reverseChargeItemId;

	@Column(name = "SPECIAL_ADJUSTMENT_MONEY")
	private Double specialAdjustmentMoney;

	@Column(name = "VEND_RCM_TAX_RATE")
	private Double rcmTaxRateVend;

	@Column(name = "VEND_CESS_TAX_RATE")
	private Double cessTaxRateVend;

	@Column(name = "VEND_TAX_APPLICABLE_DATE")
	private Date applicableDateVendTax;

	public Double getSpecialAdjustmentMoney() {
		return specialAdjustmentMoney;
	}

	public void setSpecialAdjustmentMoney(Double specialAdjustmentMoney) {
		this.specialAdjustmentMoney = specialAdjustmentMoney;
	}

	@Column(name = "discount_percentage")
	private Double discountPercentage;

	public Double getDiscountPercentage() {
		return discountPercentage;
	}

	public void setDiscountPercentage(Double discountPercentage) {
		this.discountPercentage = discountPercentage;
	}

	public Double getAdvanceMoney() {
		return advanceMoney;
	}

	public void setAdvanceMoney(Double advanceMoney) {
		this.advanceMoney = advanceMoney;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Long getReverseChargeItemId() {
		return reverseChargeItemId;
	}

	public void setReverseChargeItemId(Long rcm) {
		this.reverseChargeItemId = rcm;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Particulars getParticulars() {
		return particulars;
	}

	public void setParticulars(Particulars particulars) {
		this.particulars = particulars;
	}

	public Vendor getVendorSpecific() {
		return vendorSpecific;
	}

	public void setVendorSpecific(Vendor vendorSpecific) {
		this.vendorSpecific = vendorSpecific;
	}

	public Specifics getSpecificsVendors() {
		return specificsVendors;
	}

	public void setSpecificsVendors(Specifics specificsVendors) {
		this.specificsVendors = specificsVendors;
	}

	public Double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public Long getEntityComparableParamId() {
		return getSpecificsVendors().getId();
	}

	public Double getRcmTaxRateVend() {
		return rcmTaxRateVend;
	}

	public void setRcmTaxRateVend(Double rcmTaxRateVend) {
		this.rcmTaxRateVend = rcmTaxRateVend;
	}

	public Double getCessTaxRateVend() {
		return cessTaxRateVend;
	}

	public void setCessTaxRateVend(Double cessTaxRateVend) {
		this.cessTaxRateVend = cessTaxRateVend;
	}

	public Date getApplicableDateVendTax() {
		return applicableDateVendTax;
	}

	public void setApplicableDateVendTax(Date applicableDateVendTax) {
		this.applicableDateVendTax = applicableDateVendTax;
	}

	public static VendorSpecific findById(Long id) {
		return entityManager.find(VendorSpecific.class, id);
	}

	public static VendorSpecific findByVendorAndSpecific(EntityManager entityManager, Long orgId, Long branchId,
			Long vendId, Long specId) {
		VendorSpecific VendSpec = null;
		try {
			Query query = entityManager.createQuery(VEND_SPECIFIC_JQL);
			query.setParameter(1, orgId);
			query.setParameter(2, branchId);
			query.setParameter(3, specId);
			query.setParameter(4, vendId);
			List<VendorSpecific> resultList = query.getResultList();
			if (!resultList.isEmpty()) {
				VendSpec = resultList.get(0);
			}
			// VendSpec = (VendorSpecific) query.getSingleResult();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return VendSpec;
	}

	public static List<VendorSpecific> findBySpecific(EntityManager entityManager, Long orgId, Long specId) {
		List<VendorSpecific> VendSpec = null;
		try {
			Query query = entityManager.createQuery(ORG_SPECIFIC_JQL);
			query.setParameter(1, orgId);
			query.setParameter(2, specId);
			VendSpec = (List<VendorSpecific>) query.getResultList();
		} catch (NoResultException ex) {
		}
		return VendSpec;
	}

}
