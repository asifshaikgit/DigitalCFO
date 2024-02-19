package model;

import javax.persistence.*;
import java.util.Date;

/**
 * @auther Sunil K Namdev created on 12.03.2018
 */
@Entity
@Table (name="TRIALBALANCE_INTER_BRANCH")
public class TrialBalanceInterBranch extends AbstractBaseModel {

	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="INTER_BRANCH_MAPPING_ID")
	private InterBranchMapping interBranchMapping;

	@Column(name="TRANSACTION_ID")
	private Long transactionId;

	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="TRANSACTION_PURPOSE")
	private TransactionPurpose transactionPurpose;

	//transaction date
	@Column(name="DATE")
	private Date date;

	//Branch which initiated this transaction
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="FROM_BRANCH_ID")
	private Branch fromBranch;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TO_BRANCH_ID")
	private Branch toBranch;

	//organization id like IDOS
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGNIZATION_ID")
	private Organization organization;

	//for sell transaction this is affected
	@Column(name="CREDIT_AMOUNT")
	private Double creditAmount=0.0;

	@Column(name="DEBIT_AMOUNT")
	private Double debitAmount=0.0;

	@Column(name="CLOSING_BALANCE")
	private Double closingBalance=0.0;

	@Column(name = "TYPE_IDENTIFIER")
	private Integer typeIdentifier;

	public Long getTransactionId() {
		return this.transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public TransactionPurpose getTransactionPurpose() {
		return this.transactionPurpose;
	}

	public void setTransactionPurpose(TransactionPurpose transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
	}

	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Branch getFromBranch() {
		return this.fromBranch;
	}

	public void setFromBranch(Branch fromBranch) {
		this.fromBranch = fromBranch;
	}

	public Branch getToBranch() {
		return this.toBranch;
	}

	public void setToBranch(Branch toBranch) {
		this.toBranch = toBranch;
	}

	public Organization getOrganization() {
		return this.organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Double getCreditAmount() {
		return this.creditAmount;
	}

	public void setCreditAmount(Double creditAmount) {
		this.creditAmount = creditAmount;
	}

	public Double getDebitAmount() {
		return this.debitAmount;
	}

	public void setDebitAmount(Double debitAmount) {
		this.debitAmount = debitAmount;
	}

	public Double getClosingBalance() {
		return this.closingBalance;
	}

	public void setClosingBalance(Double closingBalance) {
		this.closingBalance = closingBalance;
	}

	public Integer getTypeIdentifier() {
		return this.typeIdentifier;
	}

	public void setTypeIdentifier(Integer typeIdentifier) {
		this.typeIdentifier = typeIdentifier;
	}

	public InterBranchMapping getInterBranchMapping() {
		return this.interBranchMapping;
	}

	public void setInterBranchMapping(InterBranchMapping interBranchMapping) {
		this.interBranchMapping = interBranchMapping;
	}
}
