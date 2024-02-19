package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="BRANCH_CASH_COUNT_DENOMINATION_DETAILS")
public class BranchCashCountDenomination extends AbstractBaseModel {
	
	@Column(name="date")
	private Date date;
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Column(name="denomination")
	private Integer denomination;
	
	@Column(name="denomination_type")
	private Integer denominationType;
	
	@Column(name="quantity")
	private Integer quantity;
	
	@Column(name="total")
	private Integer total;
	
	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_id")
	private Branch branch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_organization_id")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_cash_count_id")
	private BranchCashCount branchCashCount;

	public Integer getDenomination() {
		return denomination;
	}

	public void setDenomination(Integer denomination) {
		this.denomination = denomination;
	}

	public Integer getDenominationType() {
		return denominationType;
	}

	public void setDenominationType(Integer denominationType) {
		this.denominationType = denominationType;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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

	public BranchCashCount getBranchCashCount() {
		return branchCashCount;
	}

	public void setBranchCashCount(BranchCashCount branchCashCount) {
		this.branchCashCount = branchCashCount;
	}
}
