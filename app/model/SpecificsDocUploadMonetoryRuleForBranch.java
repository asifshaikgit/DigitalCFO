package model;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.EntityManager;

@Entity
@Table(name = "SPECIFICS_has_DOC_UPLOAD_RULE_for_BRANCHES")
public class SpecificsDocUploadMonetoryRuleForBranch extends AbstractBaseModel {

	private static final String FIND_BY_SPECIFIC_JPQL = "select obj from SpecificsDocUploadMonetoryRuleForBranch obj WHERE obj.organization.id = ?1 and obj.specifics.id = ?2 and obj.presentStatus=1";

	@Column(name = "MONETORY_LIMIT")
	private Double monetoryLimit = 0.0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SPECIFICS_ID")
	private Specifics specifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SPECIFICS_PARTICULARS_ID")
	private Particulars particulars;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ID")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ORGANIZATION_ID")
	private Organization organization;

	public Double getMonetoryLimit() {
		return monetoryLimit;
	}

	public void setMonetoryLimit(Double monetoryLimit) {
		this.monetoryLimit = monetoryLimit;
	}

	public Specifics getSpecifics() {
		return specifics;
	}

	public void setSpecifics(Specifics specifics) {
		this.specifics = specifics;
	}

	public Particulars getParticulars() {
		return particulars;
	}

	public void setParticulars(Particulars particulars) {
		this.particulars = particulars;
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

	public Long getEntityComparableParamId1() {
		return getSpecifics().getId();
	}

	public Long getEntityComparableParamId2() {
		return getBranch().getId();
	}

	public static List<SpecificsDocUploadMonetoryRuleForBranch> findBranchBySpecific(EntityManager entityManager,
			long orgid,
			long specificId) {
		List<SpecificsDocUploadMonetoryRuleForBranch> specificsDocUploadMonetoryRuleForBranchList = null;
		Query query = entityManager.createQuery(FIND_BY_SPECIFIC_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, specificId);
		specificsDocUploadMonetoryRuleForBranchList = (List<SpecificsDocUploadMonetoryRuleForBranch>) query
				.getResultList();
		return specificsDocUploadMonetoryRuleForBranchList;
	}
}
