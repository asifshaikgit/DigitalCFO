package model;

import javax.persistence.*;

import com.idos.util.IdosDaoConstants;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "BRANCH_SPECIFICS_TAX_FORMULA")
public class BranchSpecificsTaxFormula extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BranchSpecificsTaxFormula() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String TAX_HQL = "select obj from BranchSpecificsTaxFormula obj WHERE obj.branch.id = ?1 and obj.organization.id=?2 and obj.branchTaxes.id=?3 and obj.presentStatus=1";
	private static final String ORG_TAX_HQL = "select obj from BranchSpecificsTaxFormula obj WHERE obj.organization.id=?1 and obj.branchTaxes.id=?2 and obj.presentStatus=1";
	private static final String TAX_HQL_SPECIFIC = "select obj from BranchSpecificsTaxFormula obj WHERE obj.organization.id=?1 and obj.branch.id=?2 and obj.branchTaxes.id=?3 and obj.specifics.id=?4 and obj.vendor.id=?5 and UPPER(obj.hsnDesc) = ?6 and obj.presentStatus=1";
	private static final String QUERY_FOR_MAX_DATE = "select obj from BranchSpecificsTaxFormula obj WHERE obj.organization.id = ?1 and obj.branch.id = ?2 and obj.specifics.id = ?3 and obj.presentStatus=1 ORDER by obj.applicableFrom DESC";

	// private static final String DATE_SPECIFIC_TAX_JPQL = "select obj from
	// BranchSpecificsTaxFormula obj WHERE obj.organization.id = ?1 and
	// obj.branch.id = ?2 and obj.specifics.id = ?3 and obj.applicableFrom <= ?4
	// and obj.branchTaxes.id in (select tbl.id from BranchTaxes tbl where
	// tbl.organization.id = ?5 and tbl.branch.id = ?6 and tbl.taxType in (?7)
	// and tbl.presentStatus=1) ORDER by obj.applicableFrom DESC";

	private static final String DATE_SPECIFIC_TAX_JPQL = "select obj from BranchSpecificsTaxFormula obj WHERE obj.organization.id = ?1 and obj.branch.id = ?2 and obj.specifics.id = ?3 and obj.applicableFrom <= ?4 and obj.presentStatus=1 and obj.branchTaxes.id in (select tbl.id from BranchTaxes tbl where tbl.organization.id = ?5 and tbl.branch.id = ?6 and tbl.taxType in (?7) and tbl.presentStatus=1) ORDER by obj.applicableFrom DESC";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_TAXESID")
	private BranchTaxes branchTaxes;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_SPECIFICS_ID")
	private BranchSpecifics branchSpecifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specifics_id")
	private Specifics specifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specifics_particulars_id")
	private Particulars particular;

	@Column(name = "formula")
	private String formula;

	@Column(name = "invoice_value")
	private String invoiceValue;

	@Column(name = "appliedto")
	private String appliedTo;

	@Column(name = "add_deduct")
	private Integer addDeduct;

	@Column(name = "GST_ITEM_CODE")
	private String gstItemCode;

	@Column(name = "GST_ITEM_CATEGORY")
	private String gstItemCategory;

	@Column(name = "GST_TAX_RATE")
	private Double gstTaxRate;

	@Column(name = "HSN_DESC")
	private String hsnDesc;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VENDOR_ID")
	private Vendor vendor;

	@Column(name = "VENDOR_TYPE")
	private Integer vendorType;

	@Column(name = "APPLICABLE_FROM")
	private Date applicableFrom;

	public String getAppliedTo() {
		return appliedTo;
	}

	public void setAppliedTo(String appliedTo) {
		this.appliedTo = appliedTo;
	}

	public Integer getAddDeduct() {
		return addDeduct;
	}

	public void setAddDeduct(Integer addDeduct) {
		this.addDeduct = addDeduct;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public BranchTaxes getBranchTaxes() {
		return branchTaxes;
	}

	public void setBranchTaxes(BranchTaxes branchTaxes) {
		this.branchTaxes = branchTaxes;
	}

	public BranchSpecifics getBranchSpecifics() {
		return branchSpecifics;
	}

	public void setBranchSpecifics(BranchSpecifics branchSpecifics) {
		this.branchSpecifics = branchSpecifics;
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

	public Specifics getSpecifics() {
		return specifics;
	}

	public void setSpecifics(Specifics specifics) {
		this.specifics = specifics;
	}

	public Particulars getParticular() {
		return particular;
	}

	public void setParticular(Particulars particular) {
		this.particular = particular;
	}

	public String getInvoiceValue() {
		return invoiceValue;
	}

	public void setInvoiceValue(String invoiceValue) {
		this.invoiceValue = invoiceValue;
	}

	public String getGstItemCode() {
		return this.gstItemCode;
	}

	public void setGstItemCode(String gstItemCode) {
		this.gstItemCode = gstItemCode;
	}

	public String getGstItemCategory() {
		return this.gstItemCategory;
	}

	public void setGstItemCategory(String gstItemCategory) {
		this.gstItemCategory = gstItemCategory;
	}

	public Double getGstTaxRate() {
		return this.gstTaxRate;
	}

	public void setGstTaxRate(Double gstTaxRate) {
		this.gstTaxRate = gstTaxRate;
	}

	public String getHsnDesc() {
		return this.hsnDesc;
	}

	public void setHsnDesc(String hsnDesc) {
		this.hsnDesc = hsnDesc;
	}

	public Vendor getVendor() {
		return this.vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public Integer getVendorType() {
		return this.vendorType;
	}

	public void setVendorType(Integer vendorType) {
		this.vendorType = vendorType;
	}

	public static BranchSpecificsTaxFormula findById(Long id) {
		if (id == null) {
			return null;
		} else {
			return entityManager.find(BranchSpecificsTaxFormula.class, id);
		}
	}

	public Date getApplicableFrom() {
		return applicableFrom;
	}

	public void setApplicableFrom(Date applicableFrom) {
		this.applicableFrom = applicableFrom;
	}

	public static BranchSpecificsTaxFormula findByOrg(EntityManager entityManager, Long orgid, Long taxid) {
		BranchSpecificsTaxFormula branchTaxes = null;
		try {
			Query query = entityManager.createQuery(ORG_TAX_HQL);
			query.setParameter(1, orgid);
			query.setParameter(2, taxid);
			branchTaxes = (BranchSpecificsTaxFormula) query.getSingleResult();
		} catch (Exception ex) {
		}
		return branchTaxes;
	}

	public static BranchSpecificsTaxFormula findByTaxBranch(EntityManager entityManager, Long branchid, Long orgid,
			Long taxid) {
		BranchSpecificsTaxFormula branchTaxes = null;
		try {
			Query query = entityManager.createQuery(TAX_HQL);
			query.setParameter(1, branchid);
			query.setParameter(2, orgid);
			query.setParameter(3, taxid);
			branchTaxes = (BranchSpecificsTaxFormula) query.getSingleResult();
		} catch (Exception ex) {
		}
		return branchTaxes;
	}

	public static BranchSpecificsTaxFormula findByTaxBranchAndSpecific(EntityManager entityManager, Long orgid,
			Long branchid, Long taxid, Long specificId, Long vendorId, String desc) {
		BranchSpecificsTaxFormula branchTaxes = null;
		try {
			Query query = entityManager.createQuery(TAX_HQL_SPECIFIC);
			query.setParameter(1, orgid);
			query.setParameter(2, branchid);
			query.setParameter(3, taxid);
			query.setParameter(4, specificId);
			query.setParameter(5, vendorId);
			query.setParameter(6, "'" + desc.toUpperCase() + '"');
			branchTaxes = (BranchSpecificsTaxFormula) query.getSingleResult();
		} catch (Exception ex) {
		}
		return branchTaxes;
	}

	public static List<BranchSpecificsTaxFormula> findByOrgList(EntityManager entityManager, Long orgid, Long taxid) {
		List<BranchSpecificsTaxFormula> branchTaxesList = null;
		try {
			Query query = entityManager.createQuery(ORG_TAX_HQL);
			query.setParameter(1, orgid);
			query.setParameter(2, taxid);
			branchTaxesList = (List<BranchSpecificsTaxFormula>) query.getResultList();
		} catch (Exception ex) {
		}
		return branchTaxesList;
	}

	public static Date findLatestApplicableDate(EntityManager entityManager, Long orgid, Long branchid,
			Long specificId) {
		List<BranchSpecificsTaxFormula> branchTaxesList = null;
		try {
			Query query = entityManager.createQuery(QUERY_FOR_MAX_DATE);
			query.setParameter(1, orgid);
			query.setParameter(2, branchid);
			query.setParameter(3, specificId);
			branchTaxesList = (List<BranchSpecificsTaxFormula>) query.getResultList();
		} catch (Exception ex) {
		}
		if (branchTaxesList != null && branchTaxesList.size() > 0) {
			BranchSpecificsTaxFormula branchSpecificsTaxFormula = branchTaxesList.get(0);
			return branchSpecificsTaxFormula.getApplicableFrom();
		}
		return null;
	}

	public static List<BranchSpecificsTaxFormula> findTaxOnSpecificDate(EntityManager entityManager, long orgid,
			long branchid, long specificId, Date date, List<Integer> taxTypeList) {
		List<BranchSpecificsTaxFormula> branchTaxesList = null;
		Query query = entityManager.createQuery(DATE_SPECIFIC_TAX_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, branchid);
		query.setParameter(3, specificId);
		query.setParameter(4, date);
		query.setParameter(5, orgid);
		query.setParameter(6, branchid);
		query.setParameter(7, taxTypeList);
		branchTaxesList = (List<BranchSpecificsTaxFormula>) query.getResultList();
		return branchTaxesList;
	}
}
