package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.*;
import java.util.List;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "INTER_BRANCH_MAPPING")
public class InterBranchMapping extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public InterBranchMapping() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "FROM_BRANCH_ID")
	private Branch fromBranch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TO_BRANCH_ID")
	private Branch toBranch;

	@Column(name = "OPENING_BALANCE")
	private Double openingBalance;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Branch getToBranch() {
		return toBranch;
	}

	public void setToBranch(Branch toBranch) {
		this.toBranch = toBranch;
	}

	public Branch getFromBranch() {
		return fromBranch;
	}

	public void setFromBranch(Branch fromBranch) {
		this.fromBranch = fromBranch;
	}

	public Double getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(Double openingBalance) {
		this.openingBalance = openingBalance;
	}

	public static InterBranchMapping findByFromToBranches(EntityManager entityManger, Long fromBranch, Long toBranch) {
		String jpql = "select obj from InterBranchMapping obj where obj.fromBranch.id=?1 and obj.toBranch.id=?2 and obj.presentStatus=1";
		Query query = entityManager.createQuery(jpql);
		query.setParameter(1, fromBranch);
		query.setParameter(2, toBranch);
		List<InterBranchMapping> list = query.getResultList();
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	public static InterBranchMapping findById(Long id) {
		return entityManager.find(InterBranchMapping.class, id);
	}

	public String getName() {
		return this.getFromBranch().getName() + "-" + this.getToBranch().getName();
	}

}
