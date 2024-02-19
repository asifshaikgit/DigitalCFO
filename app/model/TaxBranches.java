package model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="BRANCH_has_TAX")
public class TaxBranches extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="tax_id")
	private Tax taxBranches;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_id")
	private Branch branchTaxes;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_organization_id")
	private Organization organization;


	public Tax getTaxBranches() {
		return taxBranches;
	}

	public void setTaxBranches(Tax taxBranches) {
		this.taxBranches = taxBranches;
	}

	public Branch getBranchTaxes() {
		return branchTaxes;
	}

	public void setBranchTaxes(Branch branchTaxes) {
		this.branchTaxes = branchTaxes;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
}
