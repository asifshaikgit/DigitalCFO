package model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="PROJECT_has_BRANCH")
public class ProjectBranches extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="project_id")
	private Project project;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="project_organization_id")
	private Organization projectOrganization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_id")
	private Branch projectBranch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_organization_id")
	private Organization branchOrganization;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Organization getProjectOrganization() {
		return projectOrganization;
	}

	public void setProjectOrganization(Organization projectOrganization) {
		this.projectOrganization = projectOrganization;
	}

	public Branch getProjectBranch() {
		return projectBranch;
	}

	public void setProjectBranch(Branch projectBranch) {
		this.projectBranch = projectBranch;
	}

	public Organization getBranchOrganization() {
		return branchOrganization;
	}

	public void setBranchOrganization(Organization branchOrganization) {
		this.branchOrganization = branchOrganization;
	}
	
	public Long getEntityComparableParamId(){
		return getProjectBranch().getId();
	}	

}
