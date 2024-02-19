package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ORGANIZATION_PAYMENT_HISTORY")
public class OrganizationBranchPaymentHistory extends AbstractBaseModel {
	
	@Column(name="PAYMENT_DATE")
	private Date paymentDate;
	
	@Column(name="PAYMENT_MODE")
	private String paymentMode;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID")
	private Branch branch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_PAYMENT_HISTORYID")
	private OrganizationPaymentHistory organizationPaymentHistory;
	
	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public OrganizationPaymentHistory getOrganizationPaymentHistory() {
		return organizationPaymentHistory;
	}

	public void setOrganizationPaymentHistory(
			OrganizationPaymentHistory organizationPaymentHistory) {
		this.organizationPaymentHistory = organizationPaymentHistory;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PAYMENT_BY")
	private Users paymentBy;
	
	@Column(name="PAYMENT_AMOUNT")
	private Double paymentAmount;
	
	@Column(name="PAYMENT_UNIT")
	private Integer paymentUnit;
	
	@Column(name="UNIT_AMOUNT")
	private Double unitAmount;

	public Date getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Users getPaymentBy() {
		return paymentBy;
	}

	public void setPaymentBy(Users paymentBy) {
		this.paymentBy = paymentBy;
	}

	public Double getPaymentAmount() {
		return paymentAmount;
	}

	public void setPaymentAmount(Double paymentAmount) {
		this.paymentAmount = paymentAmount;
	}

	public Integer getPaymentUnit() {
		return paymentUnit;
	}

	public void setPaymentUnit(Integer paymentUnit) {
		this.paymentUnit = paymentUnit;
	}

	public Double getUnitAmount() {
		return unitAmount;
	}

	public void setUnitAmount(Double unitAmount) {
		this.unitAmount = unitAmount;
	}
}
