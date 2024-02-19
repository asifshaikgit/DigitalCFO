package model.payroll;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import model.AbstractBaseModel;
import model.Organization;
import model.VendorGroup;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "PAYROLL_SETUP")
public class PayrollSetup extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public PayrollSetup() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@Column(name = "PAYROLL_TYPE")
	private Integer payrollType; // 1=Earning, 0= deduction

	@Column(name = "PAYROLL_HEAD_NAME")
	private String payrollHeadName;

	@Column(name = "in_force")
	private int inForce;

	@Column(name = "IS_FIXED")
	private int isFixed;

	@Column(name = "OPENING_BALANCE")
	private Double openingBal;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Integer getPayrollType() {
		return payrollType;
	}

	public void setPayrollType(Integer payrollType) {
		this.payrollType = payrollType;
	}

	public String getPayrollHeadName() {
		return payrollHeadName;
	}

	public void setPayrollHeadName(String payrollHeadName) {
		this.payrollHeadName = payrollHeadName;
	}

	public int getInForce() {
		return inForce;
	}

	public void setInForce(int inForce) {
		this.inForce = inForce;
	}

	public int getIsFixed() {
		return isFixed;
	}

	public void setIsFixed(int isFixed) {
		this.isFixed = isFixed;
	}

	public Double getOpeningBal() {
		return openingBal;
	}

	public void setOpeningBal(Double openingBal) {
		this.openingBal = openingBal;
	}

	public static PayrollSetup findById(Long id) {
		return entityManager.find(PayrollSetup.class, id);
	}
}
