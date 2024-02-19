package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.idos.util.IdosUtil;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "EXPENSE_GROUP")
public class ExpenseGroup extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	private static final String EXP_GRP_HQL = "select obj from ExpenseGroup obj WHERE obj.organization.id = ?1 and obj.presentStatus=1";

	public ExpenseGroup() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "EXPENSE_GROUP_NAME")
	private String expenseGroupName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@OneToMany(mappedBy = "expenseGroup", fetch = FetchType.LAZY)
	private List<ExpenseGroupExpenseItemMonetoryClaim> expenseGroupExpenseItemMonetoryClaims;

	public String getExpenseGroupName() {
		return expenseGroupName;
	}

	public void setExpenseGroupName(String expenseGroupName) {
		this.expenseGroupName = IdosUtil.escapeHtml(expenseGroupName);
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public List<ExpenseGroupExpenseItemMonetoryClaim> getExpenseGroupExpenseItemMonetoryClaims() {
		return expenseGroupExpenseItemMonetoryClaims;
	}

	public void setExpenseGroupExpenseItemMonetoryClaims(
			List<ExpenseGroupExpenseItemMonetoryClaim> expenseGroupExpenseItemMonetoryClaims) {
		this.expenseGroupExpenseItemMonetoryClaims = expenseGroupExpenseItemMonetoryClaims;
	}

	/**
	 * Find a ExpenseGroup by id.
	 */
	public static ExpenseGroup findById(Long id) {
		return entityManager.find(ExpenseGroup.class, id);
	}

	public static List<ExpenseGroup> getExpenseGroupList(EntityManager entityManager, Long orgId) throws Exception {
		List<ExpenseGroup> expenseGroupList = null;
		Query query = entityManager.createQuery(EXP_GRP_HQL);
		query.setParameter(1, orgId);
		expenseGroupList = query.getResultList();
		return expenseGroupList;
	}
}
