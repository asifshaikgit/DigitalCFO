package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="EXPENSE_GROUP_EXPENSE_ITEM_MONETORY_CLAIM")
public class ExpenseGroupExpenseItemMonetoryClaim extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="EXPENSE_GROUP_ID")
	private ExpenseGroup expenseGroup;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SPECIFICS_ID")
	private Specifics specificsItem;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SPECIFICS_PARTICULARS_ID")
	private Particulars specificsParticulars;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;
	
	@Column(name="MAXIMUM_PERMITTED_ADVANCE")
	private Double maximumPermittedAdvance;
	
	@Column(name="MONTHLY_MONETORY_LIMIT_FOR_REIMBURSEMENT")
	private Double monthlyMonetoryLimitForReimbursement;

	public ExpenseGroup getExpenseGroup() {
		return expenseGroup;
	}

	public void setExpenseGroup(ExpenseGroup expenseGroup) {
		this.expenseGroup = expenseGroup;
	}

	public Specifics getSpecificsItem() {
		return specificsItem;
	}

	public void setSpecificsItem(Specifics specificsItem) {
		this.specificsItem = specificsItem;
	}

	public Particulars getSpecificsParticulars() {
		return specificsParticulars;
	}

	public void setSpecificsParticulars(Particulars specificsParticulars) {
		this.specificsParticulars = specificsParticulars;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Double getMaximumPermittedAdvance() {
		return maximumPermittedAdvance;
	}

	public void setMaximumPermittedAdvance(Double maximumPermittedAdvance) {
		this.maximumPermittedAdvance = maximumPermittedAdvance;
	}

	public Double getMonthlyMonetoryLimitForReimbursement() {
		return monthlyMonetoryLimitForReimbursement;
	}

	public void setMonthlyMonetoryLimitForReimbursement(
			Double monthlyMonetoryLimitForReimbursement) {
		this.monthlyMonetoryLimitForReimbursement = monthlyMonetoryLimitForReimbursement;
	}
	
	public Long getEntityComparableParamId(){
		return getExpenseGroup().getId();
	}	
	public Long getEntityComparableParamId1(){
		return getSpecificsItem().getId();
	}
}
