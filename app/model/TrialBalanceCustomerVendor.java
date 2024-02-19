package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="TRIALBALANCE_VENDOR_CUSTOMER")
public class TrialBalanceCustomerVendor extends AbstractBaseModel {
	@Column(name="TRANSACTION_ID")
	private Long transactionId;
	
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
	
	@Column(name="DATE")
	private Date date;
		
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID")
	private Branch branch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ORGNIZATION_ID")
	private Organization organization;
	
	//it will be vendor/customer id
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="VENDOR_ID")
	private Vendor vendor;
	  
	//if =1, then vendor else if=2 then customer	
	@Column(name="VENDOR_TYPE")
	private Integer vendorType;
	  
	@Column(name="CREDIT_AMOUNT")
	private Double creditAmount=0.0;
	
	@Column(name="DEBIT_AMOUNT")
	private Double debitAmount=0.0;
	
	@Column(name="CLOSING_BALANCE")
	private Double closingBalance=0.0;	
	
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
	
	public Specifics getTransactionSpecifics() {
		return transactionSpecifics;
	}

	public void setTransactionSpecifics(Specifics transactionSpecifics) {
		this.transactionSpecifics = transactionSpecifics;
	}

	public Particulars getTransactionParticulars() {
		return transactionParticulars;
	}

	public void setTransactionParticulars(Particulars transactionParticulars) {
		this.transactionParticulars = transactionParticulars;
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

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public Integer getVendorType() {
		return vendorType;
	}

	public void setVendorType(Integer vendorType) {
		this.vendorType = vendorType;
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

	/*public Double getAdvanceCreditAmount() {
		return advanceCreditAmount;
	}

	public void setAdvanceCreditAmount(Double advanceCreditAmount) {
		this.advanceCreditAmount = advanceCreditAmount;
	}

	public Double getAdvanceDebitAmount() {
		return advanceDebitAmount;
	}

	public void setAdvanceDebitAmount(Double advanceDebitAmount) {
		this.advanceDebitAmount = advanceDebitAmount;
	}*/
	

}
