package model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="USERS_CLAIM_COA")
public class UsersClaimCOA extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id")
	private Users user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="specifics_id")
	private Specifics specifics;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="specifics_particulars_id")
	private Particulars particulars;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID")
	private Branch branch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ORGANIZATION_ID")
	private Organization organization;

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
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
}
