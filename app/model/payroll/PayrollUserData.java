package model.payroll;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import model.AbstractBaseModel;
import model.Organization;
import model.Users;

@Entity
@Table(name="PAYROLL_USERS_DATA")
public class PayrollUserData extends AbstractBaseModel {
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USER_ID")
	private Users user;

	@Column(name="PAYROLL_TYPE")
	private Integer payrollType;  //1=Earning, 0= deduction
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PAYROLL_SETUP_ID")
	private PayrollSetup payrollSetup;
	
	@Column(name="ANNUAL_INCOME")
	private Double annualIncome;
	
	@Column(name="MONTHLY_INCOME")
	private Double monthlyIncome;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}
	
	public Integer getPayrollType() {
		return payrollType;
	}

	public void setPayrollType(Integer payrollType) {
		this.payrollType = payrollType;
	}

	public PayrollSetup getPayrollSetup() {
		return payrollSetup;
	}

	public void setPayrollSetup(PayrollSetup payrollSetup) {
		this.payrollSetup = payrollSetup;
	}

	public Double getAnnualIncome() {
		return annualIncome;
	}

	public void setAnnualIncome(Double annualIncome) {
		this.annualIncome = annualIncome;
	}

	public Double getMonthlyIncome() {
		return monthlyIncome;
	}

	public void setMonthlyIncome(Double monthlyIncome) {
		this.monthlyIncome = monthlyIncome;
	}

}
