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
@Table(name = "SPECIFICS_TRANSACTION_PURPOSE")
public class SpecificsTransactionPurpose extends AbstractBaseModel {

	private static final String FIND_BY_SPECIFIC_JPQL = "select obj from SpecificsTransactionPurpose obj WHERE obj.organization.id = ?1 and obj.specifics.id = ?2 and obj.presentStatus=1";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_PURPOSE_ID")
	private TransactionPurpose transactionPurpose;

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

	public TransactionPurpose getTransactionPurpose() {
		return transactionPurpose;
	}

	public void setTransactionPurpose(TransactionPurpose transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
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

	public Long getEntityComparableParamId() {
		if (getTransactionPurpose() == null) {
			return new Long(0);
		} else {
			return getTransactionPurpose().getId();
		}
	}

	public static List<SpecificsTransactionPurpose> findSpecificsTransactionPurpose(EntityManager entityManager,
			long orgid,
			long specificId) {
		List<SpecificsTransactionPurpose> specificsTransactionPurposeList = null;
		Query query = entityManager.createQuery(FIND_BY_SPECIFIC_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, specificId);
		specificsTransactionPurposeList = (List<SpecificsTransactionPurpose>) query.getResultList();
		return specificsTransactionPurposeList;
	}
}
