package model;

import java.util.List;

import javax.persistence.*;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "PARTICULARS")
public class Particulars extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Particulars() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String MAX_ACCODE_ORG = "select MAX(obj.accountCode) from Particulars obj where obj.organization.id=?1 and obj.presentStatus=1";
	private static final String ORG_JPQL = "select obj from Particulars obj where obj.organization.id=?1 and obj.presentStatus=1";
	@Column(name = "name")
	private String name;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "particularsId")
	private List<Specifics> specifics;

	@Column(name = "account_code")
	private Long accountCode;

	@Column(name = "description")
	private String description;

	@Column(name = "account_code_hirarchy")
	private String accountCodeHirarchy;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "particulars")
	private List<Expense> particularsExpenses;

	public List<Expense> getParticularsExpenses() {
		return particularsExpenses;
	}

	public void setParticularsExpenses(List<Expense> particularsExpenses) {
		this.particularsExpenses = particularsExpenses;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getAccountCode() {
		return accountCode;
	}

	public void setAccountCode(Long accountCode) {
		this.accountCode = accountCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Specifics> getSpecifics() {
		return specifics;
	}

	public void setSpecifics(List<Specifics> specifics) {
		this.specifics = specifics;
	}

	/**
	 * list of particulars.
	 */
	public static List<Particulars> list(EntityManager entityManager, Organization orgn) {
		Query query = entityManager.createQuery(ORG_JPQL);
		query.setParameter(1, orgn.getId());
		List<Particulars> particulars = query.getResultList();
		return particulars;
	}

	public static List findMaxAccountCode(EntityManager entityManager, Long orgid) {
		Query query = entityManager.createQuery(MAX_ACCODE_ORG);
		query.setParameter(1, orgid);
		List particulars = query.getResultList();
		return particulars;
	}

	/**
	 * Find a particulars by id.
	 */
	public static Particulars findById(Long id) {
		return entityManager.find(Particulars.class, id);
	}

	public String getAccountCodeHirarchy() {
		return accountCodeHirarchy;
	}

	public void setAccountCodeHirarchy(String accountCodeHirarchy) {
		this.accountCodeHirarchy = accountCodeHirarchy;
	}

}
