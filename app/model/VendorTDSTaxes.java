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
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;

@Entity
@Table(name = "VENDOR_TDS_BASIC")
public class VendorTDSTaxes extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public VendorTDSTaxes() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String BY_OGR_VEND_AND_SPECIFIC_JPQL = "select obj from VendorTDSTaxes obj where obj.organization.id= ?1 and obj.vendors like ?2 and obj.specifics = ?3 and obj.toDate >= ?4 and obj.fromDate <= ?5 and obj.presentStatus = 1 order by createdAt DESC";

	private static final String BY_OGR_SPEC_JPQL = "select obj from VendorTDSTaxes obj where obj.organization.id= ?1 and obj.specifics = ?2 and obj.vendors is null and obj.toDate >= ?3 and obj.fromDate <= ?4 and presentStatus = 1 order by createdAt DESC";

	private static final String BY_OGR_SPEC_HISTORY_JPQL = "select obj from VendorTDSTaxes obj where obj.organization.id= ?1 and obj.specifics = ?2 and obj.vendors is null and presentStatus = 1 order by createdAt DESC";

	private static final String BY_OGR_VEND_HISTORY_JPQL = "select obj from VendorTDSTaxes obj where obj.organization.id= ?1 and obj.vendors like ?2 and presentStatus = 1 order by createdAt DESC";

	private static final String BY_OGR_SPEC_TDS_JPQL = "select obj from VendorTDSTaxes obj where obj.organization.id= ?1 and obj.specifics = ?2 and obj.vendors is null and presentStatus = 1 order by createdAt DESC";

	@Column(name = "SPECIFICS_ID")
	private Long specifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@Column(name = "VENDORS")
	private String vendors;

	@Column(name = "FROM_DATE")
	public Date fromDate;

	@Column(name = "TO_DATE")
	public Date toDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WH_TDS_SECTION_ID")
	private WithholdingTypeDetails tdsSection;

	@Column(name = "MODE_OF_COMPUTE")
	private Integer modeOfComputation; // 1. Automatic , 2. Manual

	@Column(name = "TAX_RATE")
	private Double taxRate;

	@Column(name = "TRANS_LIMIT")
	private Double transLimit;

	@Column(name = "IS_OVERALL_LIMIT_APPLY")
	private Integer overAllLimitApply; // 1. Applicable , 2. Not Applicable

	@Column(name = "OVERALL_LIMIT")
	private Double overAllLimit;

	@Column(name = "TDS_UPLOAD_DOC")
	private String tdsUploadDoc;

	public Long getSpecifics() {
		return specifics;
	}

	public void setSpecifics(Long specifics) {
		this.specifics = specifics;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getVendors() {
		return vendors;
	}

	public void setVendors(String vendors) {
		this.vendors = vendors;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public WithholdingTypeDetails getTdsSection() {
		return tdsSection;
	}

	public void setTdsSection(WithholdingTypeDetails tdsSection) {
		this.tdsSection = tdsSection;
	}

	public Integer getModeOfComputation() {
		return modeOfComputation;
	}

	public void setModeOfComputation(Integer modeOfComputation) {
		this.modeOfComputation = modeOfComputation;
	}

	public Double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Double taxRate) {
		this.taxRate = taxRate;
	}

	public Double getTransLimit() {
		return transLimit;
	}

	public void setTransLimit(Double transLimit) {
		this.transLimit = transLimit;
	}

	public Integer getOverAllLimitApply() {
		return overAllLimitApply;
	}

	public void setOverAllLimitApply(Integer overAllLimitApply) {
		this.overAllLimitApply = overAllLimitApply;
	}

	public Double getOverAllLimit() {
		return overAllLimit;
	}

	public void setOverAllLimit(Double overAllLimit) {
		this.overAllLimit = overAllLimit;
	}

	public String getTdsUploadDoc() {
		return tdsUploadDoc;
	}

	public void setTdsUploadDoc(String tdsUploadDoc) {
		this.tdsUploadDoc = tdsUploadDoc;
	}

	public static VendorTDSTaxes findByOrgVend(EntityManager entityManager, Long orgId, Long vendId, Specifics specific,
			Date date) {
		VendorTDSTaxes vendorTDSTaxes = null;
		if (specific != null && specific.getIsTdsVendorSpecific() != null && specific.getIsTdsVendorSpecific() != 1) {
			vendorTDSTaxes = findByOrgSpecific(entityManager, orgId, specific.getId(), date);
		} else if (vendId != null) {
			Query query = entityManager.createQuery(BY_OGR_VEND_AND_SPECIFIC_JPQL);
			query.setParameter(1, orgId);
			query.setParameter(2, "%" + vendId + "%");
			query.setParameter(3, specific.getId());
			query.setParameter(4, date);
			query.setParameter(5, date);
			List<VendorTDSTaxes> resultList = query.getResultList();
			if (resultList != null && resultList.size() > 0) {
				vendorTDSTaxes = resultList.get(0);
			}
		}
		return vendorTDSTaxes;
	}

	public static VendorTDSTaxes findByOrgSpecific(EntityManager entityManager, Long orgId, Long specificId,
			Date date) {
		Query query = entityManager.createQuery(BY_OGR_SPEC_JPQL);
		query.setParameter(1, orgId);
		query.setParameter(2, specificId);
		query.setParameter(3, date);
		query.setParameter(4, date);
		List<VendorTDSTaxes> resultList = query.getResultList();
		if (resultList != null && resultList.size() > 0) {
			return resultList.get(0);
		}
		return null;
	}

	public static List<VendorTDSTaxes> findSpecificTdsHistory(EntityManager entityManager, Long orgId,
			Long specificId) {
		Query query = entityManager.createQuery(BY_OGR_SPEC_HISTORY_JPQL);
		query.setParameter(1, orgId);
		query.setParameter(2, specificId);
		List<VendorTDSTaxes> resultList = query.getResultList();
		return resultList;
	}

	public static List<VendorTDSTaxes> findVendorTdsHistory(EntityManager entityManager, Long orgId, Long vendId) {
		Query query = entityManager.createQuery(BY_OGR_VEND_HISTORY_JPQL);
		query.setParameter(1, orgId);
		query.setParameter(2, "%" + vendId + "%");
		List<VendorTDSTaxes> resultList = query.getResultList();
		return resultList;
	}

	public static VendorTDSTaxes isTdsSeupForSpecific(EntityManager entityManager, Long orgId, Long specificId) {
		Query query = entityManager.createQuery(BY_OGR_SPEC_TDS_JPQL);
		query.setParameter(1, orgId);
		query.setParameter(2, specificId);
		List<VendorTDSTaxes> resultList = query.getResultList();
		if (resultList != null && resultList.size() > 0) {
			return resultList.get(0);
		}
		return null;
	}
}
