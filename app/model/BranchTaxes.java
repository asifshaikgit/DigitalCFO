package model;

import javax.persistence.*;

import com.idos.util.IdosUtil;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "BRANCH_TAXES")
public class BranchTaxes extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BranchTaxes() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String TAX_HQL = "select obj from BranchTaxes obj WHERE obj.branch.id = ?1 and obj.organization.id=?2 and obj.taxRate=?3 and obj.presentStatus=1 and obj.taxType=?4 order by id desc";
	private static final String BRANCH_TAX_HQL = "select obj from BranchTaxes obj WHERE obj.organization.id=?1 and obj.branch.id = ?2 and obj.presentStatus=1 and obj.taxType IN (10,11,12,13) order by id desc";
	private static final String ORG_TAX_HQL = "select obj from BranchTaxes obj WHERE obj.organization.id=?1 and obj.taxType IN (30,31,32,33) and obj.presentStatus=1 order by id desc";

	private static final String SRCH_NAME_ORG_HQL = "select obj from BranchTaxes obj WHERE obj.organization.id=?1 and obj.presentStatus=1 and obj.taxType = ?2 and lower(obj.taxName) like ?3 order by obj.taxName";

	@Column(name = "tax_name")
	private String taxName;

	@Column(name = "tax_rate")
	private Double taxRate;

	@Column(name = "opening_balance")
	private Double openingBalance;

	@Column(name = "tax_type")
	private Integer taxType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	public String getTaxName() {
		return taxName;
	}

	public void setTaxName(String taxName) {
		this.taxName = IdosUtil.escapeHtml(taxName);
	}

	public Double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Double taxRate) {
		this.taxRate = taxRate;
	}

	public Double getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(Double openingBalance) {
		this.openingBalance = openingBalance;
	}

	public Integer getTaxType() {
		return taxType;
	}

	public void setTaxType(Integer taxType) {
		this.taxType = taxType;
	}

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

	/**
	 * Find a BranchTaxes by id.
	 */
	public static BranchTaxes findById(Long id) {
		return entityManager.find(BranchTaxes.class, id);
	}

	public static BranchTaxes findByRateType(EntityManager entityManager, Long branchid, Long orgid, int type,
			Double taxRate) {
		BranchTaxes branchTaxes = null;
		Query query = entityManager.createQuery(TAX_HQL);
		query.setParameter(1, branchid);
		query.setParameter(2, orgid);
		query.setParameter(3, taxRate);
		query.setParameter(4, type);
		List<BranchTaxes> branchTaxesList = query.getResultList();
		if (branchTaxesList != null && branchTaxesList.size() > 0) {
			branchTaxes = branchTaxesList.get(0);
		}
		return branchTaxes;
	}

	public static List<BranchTaxes> findInputTaxByBranch(EntityManager entityManager, Long branchid, Long orgid) {
		List<BranchTaxes> branchTaxesList = null;
		Query query = entityManager.createQuery(BRANCH_TAX_HQL);
		query.setParameter(1, orgid);
		query.setParameter(2, branchid);
		branchTaxesList = query.getResultList();
		return branchTaxesList;
	}

	public static List<BranchTaxes> findRcmTaxByOrg(EntityManager entityManager, Long orgid) {
		List<BranchTaxes> branchTaxesList = null;
		try {
			Query query = entityManager.createQuery(ORG_TAX_HQL);
			query.setParameter(1, orgid);
			branchTaxesList = query.getResultList();
		} catch (Exception ex) {
		}
		return branchTaxesList;
	}

	public static List<BranchTaxes> findByNameAndType(EntityManager entityManager, Long orgid, int type,
			String searchText) {
		List<BranchTaxes> branchTaxesList = null;
		Query query = entityManager.createQuery(SRCH_NAME_ORG_HQL);
		query.setParameter(1, orgid);
		query.setParameter(2, type);
		query.setParameter(3, "%" + searchText + "%");
		branchTaxesList = query.getResultList();
		return branchTaxesList;
	}
}
