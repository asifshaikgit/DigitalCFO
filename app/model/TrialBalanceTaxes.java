package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="TRIALBALANCE_TAXES")
public class TrialBalanceTaxes extends AbstractBaseModel{
	//this is PK of transaction table
	@Column(name="TRANSACTION_ID") 
	private Long transactionId;
	
	//transaction type out of those 16 types of buy/sell on cash etc
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRANSACTION_PURPOSE")  
	private TransactionPurpose transactionPurpose;

	//Chart of items ID of vitamins
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRANSACTION_SPECIFICS")
	private Specifics transactionSpecifics;

	//COA types like income/expense etc
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRANSACTION_SPECIFICS_PARTICULARS")
	private Particulars transactionParticulars;
	
	//transaction date
	@Column(name="DATE") 
	private Date date;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_TAXESID")
	private BranchTaxes branchTaxes;
	
	//Branch which initiated this transaction
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID") 
	private Branch branch;
	
	//organization id like IDOS
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ORGNIZATION_ID") 
	private Organization organization;
	
	//for sell transaction this is affected
	@Column(name="CREDIT_AMOUNT")   
	private Double creditAmount=0.0;
	
	@Column(name="DEBIT_AMOUNT")
	private Double debitAmount=0.0;
	
	@Column(name="CLOSING_BALANCE")
	private Double closingBalance=0.0;
	
	////This is required where tax_type=1=input taxes, 2=output taxes, 3=Withholding tax payment received from customer, 4=Withholding tax payment made to vendor
	@Column(name="tax_type")
	private Integer taxType;
	
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

	public BranchTaxes getBranchTaxes() {
		return branchTaxes;
	}

	public void setBranchTaxes(BranchTaxes branchTaxes) {
		this.branchTaxes = branchTaxes;
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
	
	public Integer getTaxType() {
		return taxType;
	}

	public void setTaxType(Integer taxType) {
		this.taxType = taxType;
	}


	public Specifics getTransactionSpecifics() {
		return this.transactionSpecifics;
	}

	public void setTransactionSpecifics(Specifics transactionSpecifics) {
		this.transactionSpecifics = transactionSpecifics;
	}

	public Particulars getTransactionParticulars() {
		return this.transactionParticulars;
	}

	public void setTransactionParticulars(Particulars transactionParticulars) {
		this.transactionParticulars = transactionParticulars;
	}
}
