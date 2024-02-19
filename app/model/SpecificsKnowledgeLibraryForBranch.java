package model;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.EntityManager;

@Entity
@Table(name = "SPECIFICS_has_KNOWLEDGE_LIBRARY_for_BRANCH")
public class SpecificsKnowledgeLibraryForBranch extends AbstractBaseModel {

	private static final String FIND_BY_SPECIFIC_JPQL = "select obj from SpecificsKnowledgeLibraryForBranch obj WHERE obj.organization.id = ?1 and obj.specifics.id = ?2 and obj.presentStatus=1";

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
	private Particulars particulars;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "KNOWLEDGE_LIBRARY_ID")
	private SpecificsKnowledgeLibrary specificsKl;

	public SpecificsKnowledgeLibrary getSpecificsKl() {
		return specificsKl;
	}

	public void setSpecificsKl(SpecificsKnowledgeLibrary specificsKl) {
		this.specificsKl = specificsKl;
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

	public Particulars getParticulars() {
		return particulars;
	}

	public void setParticulars(Particulars particulars) {
		this.particulars = particulars;
	}

	public Long getEntityComparableParamId() {
		return getBranch().getId();
	}

	public static List<SpecificsKnowledgeLibraryForBranch> findBranchBySpecific(EntityManager entityManager, long orgid,
			long specificId) {
		List<SpecificsKnowledgeLibraryForBranch> SpecificsKnowledgeLibraryForBranchList = null;
		Query query = entityManager.createQuery(FIND_BY_SPECIFIC_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, specificId);
		SpecificsKnowledgeLibraryForBranchList = (List<SpecificsKnowledgeLibraryForBranch>) query.getResultList();
		return SpecificsKnowledgeLibraryForBranchList;
	}
}
