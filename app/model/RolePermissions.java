package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ROLE_has_PERMISSIONS")
public class RolePermissions extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="role_id")
	private Role rolePermissions;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="permission_id")
	private Permission permissionRoles;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_id")
	private Branch branch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_organization_id")
	private Organization organization;
	
	@Column(name="has_permission")
	private Integer hasPermission;
	
	public Integer getHasPermission() {
		return hasPermission;
	}

	public void setHasPermission(Integer hasPermission) {
		this.hasPermission = hasPermission;
	}

	public Role getRolePermissions() {
		return rolePermissions;
	}

	public void setRolePermissions(Role rolePermissions) {
		this.rolePermissions = rolePermissions;
	}

	public Permission getPermissionRoles() {
		return permissionRoles;
	}

	public void setPermissionRoles(Permission permissionRoles) {
		this.permissionRoles = permissionRoles;
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
}
