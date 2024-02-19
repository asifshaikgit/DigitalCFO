package model.karvy;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import model.AbstractBaseModel;
import model.Branch;
import model.ClaimTransaction;
import model.Organization;
import model.Transaction;
import model.TransactionPurpose;

@Entity
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@Table(name="GST_FILING")
public class GSTFiling  extends AbstractBaseModel{
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRANSACTION_ID")
	private Transaction transactionId;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CLAIM_TRANSACTION_ID")
	private ClaimTransaction claimTransactionId;
	
	public ClaimTransaction getClaimTransactionId() {
		return claimTransactionId;
	}

	public void setClaimTransactionId(ClaimTransaction claimTransactionId) {
		this.claimTransactionId = claimTransactionId;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID")
	private Branch branchId;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organizationId;
	
	@Column(name="TRANSACTION_DATE")
	private Date transactionDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRANSACTION_PURPOSE")
	private TransactionPurpose transactionPurpose;
	
	@Column(name="GST_FILING_STATUS")
	private Integer gstFilingStatus;
	
	@Column(name="GST_AGENT_NAME")
	private String agentName;

	public Transaction getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Transaction transactionId) {
		this.transactionId = transactionId;
	}

	public Branch getBranchId() {
		return branchId;
	}

	public void setBranchId(Branch branchId) {
		this.branchId = branchId;
	}

	public Organization getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(Organization organizationId) {
		this.organizationId = organizationId;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}

	public TransactionPurpose getTransactionPurpose() {
		return transactionPurpose;
	}

	public void setTransactionPurpose(TransactionPurpose transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
	}

	public Integer getGstFilingStatus() {
		return gstFilingStatus;
	}

	public void setGstFilingStatus(Integer gstFilingStatus) {
		this.gstFilingStatus = gstFilingStatus;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}
	
	

}


