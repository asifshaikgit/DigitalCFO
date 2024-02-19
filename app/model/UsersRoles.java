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
import service.EntityManagerProvider;

@Entity
@Table(name = "ROLE_has_USERS")
public class UsersRoles extends AbstractBaseModel {
	private static EntityManager entityManager;

	private static final String USR_ROLE_HQL = "select obj from UsersRoles obj where obj.organization.id =?1 and obj.user.id =?2 and obj.branch.id =?3 and obj.presentStatus=1 ";

	public UsersRoles() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id")
	private Role role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "users_id")
	private Users user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "users_branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "users_branch_organization_id")
	private Organization organization;

	@Column(name = "amount_limit")
	private Double amountLimit;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_officers_id")
	private OrganizationKeyOfficials branchKeyOff;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_safe_deposit_box_id")
	private BranchDepositBoxKey branchSafeDepositBox;

	public OrganizationKeyOfficials getBranchKeyOff() {
		return branchKeyOff;
	}

	public void setBranchKeyOff(OrganizationKeyOfficials branchKeyOff) {
		this.branchKeyOff = branchKeyOff;
	}

	public BranchDepositBoxKey getBranchSafeDepositBox() {
		return branchSafeDepositBox;
	}

	public void setBranchSafeDepositBox(BranchDepositBoxKey branchSafeDepositBox) {
		this.branchSafeDepositBox = branchSafeDepositBox;
	}

	public Double getAmountLimit() {
		return amountLimit;
	}

	public void setAmountLimit(Double amountLimit) {
		this.amountLimit = amountLimit;
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

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public Long getEntityComparableParamId() {
		return getRole().getId();
	}

	public static UsersRoles findByUsersRole(Long id, Users user) {
		try {
			Query query = entityManager.createQuery(
					"select obj from UsersRoles obj WHERE obj.user.id=?1 and obj.role.id=?2 and obj.presentStatus=1");
			query.setParameter(1, user.getId());
			query.setParameter(2, id);
			return (UsersRoles) query.getSingleResult();
		} catch (Exception e) {
			// Log or handle the exception as needed
			e.printStackTrace();
			return null; // or throw a custom exception, return a default value, etc.
		}
	}

	public static boolean deleteUsersRole(Long id) {
		Query query = entityManager.createQuery("DELETE FROM UsersRoles WHERE id = :id");
		query.setParameter("id", id);
		int deletedCount = query.executeUpdate();
		if (deletedCount > 0)
			return true;
		else
			return false;
	}

	public static List<UsersRoles> getUserRoleList(EntityManager entityManager, Long orgid, Long usrID,
			Long branchId) {
		List<UsersRoles> userRoleList = null;
		Query query = entityManager.createQuery(USR_ROLE_HQL);
		query.setParameter(1, orgid);
		query.setParameter(2, usrID);
		query.setParameter(3, branchId);
		userRoleList = query.getResultList();
		return userRoleList;
	}
}
