package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="MAIN_TO_PETTYCASH_APPROVER_LOGS")
public class MainToPettyApproverLogs extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_id")
	private Branch branch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="organization_id")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_CASH_COUNT_DETAILS_ID")
	private BranchCashCount branchCashCount;
	
	@Column(name="CREATOR_EMAIL")
	private String creatorEmail;
	
	@Column(name="APPROVER_EMAIL")
	private String approverEmail;
	
	@Column(name="TRANSFER_DOC")
	private String uploadedSupportingDoc;
	
	@Column(name="TRANSFER_STATUS")
	private String transferStatus;

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

	public String getCreatorEmail() {
		return creatorEmail;
	}

	public void setCreatorEmail(String creatorEmail) {
		this.creatorEmail = creatorEmail;
	}

	public String getApproverEmail() {
		return approverEmail;
	}

	public void setApproverEmail(String approverEmail) {
		this.approverEmail = approverEmail;
	}

	public String getUploadedSupportingDoc() {
		return uploadedSupportingDoc;
	}

	public void setUploadedSupportingDoc(String uploadedSupportingDoc) {
		this.uploadedSupportingDoc = uploadedSupportingDoc;
	}

	public String getTransferStatus() {
		return transferStatus;
	}

	public void setTransferStatus(String transferStatus) {
		this.transferStatus = transferStatus;
	}
}
