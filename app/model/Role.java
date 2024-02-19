package model;

import java.util.Comparator;
import java.util.List;

import javax.persistence.*;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "ROLE")
public class Role extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Role() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String ROLES_HQL = ("select obj from Role obj where obj.name!='MASTER ADMIN' and obj.name!='ADMIN' and obj.name!='SUPPORT ADMIN' and obj.name!='SUPPORT USER' and obj.name!='EXTERNAL USER' and obj.name!='CASHIER' and obj.name!='SUPER ADMIN' and obj.presentStatus=1");

	@Column(name = "name")
	private String name;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "role")
	private List<UsersRoles> roleUsers;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "rolePermissions")
	private List<RolePermissions> rolePermissions;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "role")
	private List<Permission> permissions;

	public List<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<Permission> permissions) {
		this.permissions = permissions;
	}

	public List<RolePermissions> getRolePermissions() {
		return rolePermissions;
	}

	public void setRolePermissions(List<RolePermissions> rolePermissions) {
		this.rolePermissions = rolePermissions;
	}

	public List<UsersRoles> getRoleUsers() {
		return roleUsers;
	}

	public void setRoleUsers(List<UsersRoles> roleUsers) {
		this.roleUsers = roleUsers;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * list of role.
	 */
	public static List<Role> list(EntityManager entityManager) {
		List<Role> roles = entityManager.createQuery(ROLES_HQL).getResultList();
		return roles;
	}

	/**
	 * Find a role by id.
	 */
	public static Role findById(Long id) {
		return entityManager.find(Role.class, id);
	}

	public static List<Role> findByRoleName(EntityManager entityManager, String name) {
		String sbquery = ("select obj from Role obj where obj.name=?1 and obj.presentStatus=1");
		Query query = entityManager.createQuery(sbquery);
		query.setParameter(1, name);
		List<Role> roles = query.getResultList();
		return roles;
	}

	public static List<Role> findRoleforSingleUser(EntityManager entityManager) {
		String sbquery = ("select obj from Role obj where obj.id not in (2,7,8,9,10,11,12) and obj.presentStatus=1");
		Query query = entityManager.createQuery(sbquery);
		List<Role> roles = query.getResultList();
		return roles;
	}

	public int compare(Role r1, Role r2) {
		return (int) (r1.getId() - r2.getId());
	}
}
