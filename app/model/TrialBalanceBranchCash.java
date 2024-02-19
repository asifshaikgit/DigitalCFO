package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="TRIALBALANCE_BRANCH_CASH")
public class TrialBalanceBranchCash extends AbstractBaseModel {

	@Column(name="TRANSACTION_ID")
	private Long transactionId;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRANSACTION_PURPOSE")
	private TransactionPurpose transactionPurpose;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_DEPOSITBOX_ID")
	private BranchDepositBoxKey branchDepositBoxKey;

	@Column(name="DATE")
	private Date date;
		
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID")
	private Branch branch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ORGNIZATION_ID")
	private Organization organization;
		  
	@Column(name="CREDIT_AMOUNT")
	private Double creditAmount=0.0;
	
	@Column(name="DEBIT_AMOUNT")
	private Double debitAmount=0.0;
	
	@Column(name="CLOSING_BALANCE")
	private Double closingBalance=0.0;
	
	////This is required where cash_type=1 normal cash, 2=petty cash
	@Column(name="cash_type")
	private Integer cashType;
	
	//=========================Getter and Setters Begins=====================================================//
	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public TransactionPurpose getTransactionPurpose() {
		return transactionPurpose;
	}

	public void setTransactionPurpose(TransactionPurpose transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

	public Double getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(Double creditAmount) {
		this.creditAmount = creditAmount;
	}

	public Double getDebitAmount() {
		return debitAmount;
	}

	public void setDebitAmount(Double debitAmount) {
		this.debitAmount = debitAmount;
	}

	public Double getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(Double closingBalance) {
		this.closingBalance = closingBalance;
	}

	public Integer getCashType() {
		return cashType;
	}

	public void setCashType(Integer cashType) {
		this.cashType = cashType;
	}
	public BranchDepositBoxKey getBranchDepositBoxKey() {
		return this.branchDepositBoxKey;
	}

	public void setBranchDepositBoxKey(BranchDepositBoxKey branchDepositBoxKey) {
		this.branchDepositBoxKey = branchDepositBoxKey;
	}
}
