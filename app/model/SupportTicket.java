package model;

import com.idos.util.IdosUtil;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="SUPPORT_TICKET")
public class SupportTicket extends AbstractBaseModel {

	@Column(name="CASE_ID")
	private String caseId;

	@Column(name="SUBJECT")
	private String subject;

	@Column(name="MESSAGE")
	private String message;

	@Column(name="CASE_SEVERITY")
	private Integer caseSeverity;

	@Column(name="CASE_STATUS")
	private Integer caseStatus;

	@Column(name="SUPPORTING_ATTACHMENT")
	private String supportingAttachment;

	@Column(name="ATTACHMENT")
	private String attachment;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID")
	private Branch branch;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CASE_ATTENDED_BY")
	public Users caseAttendedBy;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CASE_ASSIGNED_TO")
	public Users caseAssignedTo;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="REGISTERED_VENDOR")
	private IdosRegisteredVendor vendor;

	@OneToMany(fetch=FetchType.LAZY, mappedBy = "supportTicket")
	public List<SupportTicketComments> comments;

	@OneToMany(fetch=FetchType.LAZY, mappedBy = "supportTicket")
	public List<SupportTicketReply> replies;

	@Column(name = "RATING")
	private String rating;

	@Column(name = "HELPFUL")
	private Integer helpful;

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject =  IdosUtil.escapeHtml(subject);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message =  IdosUtil.escapeHtml(message);
	}

	public Integer getCaseSeverity() {
		return caseSeverity;
	}

	public void setCaseSeverity(Integer caseSeverity) {
		this.caseSeverity = caseSeverity;
	}

	public Integer getCaseStatus() {
		return caseStatus;
	}

	public void setCaseStatus(Integer caseStatus) {
		this.caseStatus = caseStatus;
	}

	public String getSupportingAttachment() {
		return supportingAttachment;
	}

	public void setSupportingAttachment(String supportingAttachment) {
		this.supportingAttachment = supportingAttachment;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Users getCaseAttendedBy() {
		return caseAttendedBy;
	}

	public void setCaseAttendedBy(Users caseAttendedBy) {
		this.caseAttendedBy = caseAttendedBy;
	}

	public Users getCaseAssignedTo() {
		return caseAssignedTo;
	}

	public void setCaseAssignedTo(Users caseAssignedTo) {
		this.caseAssignedTo = caseAssignedTo;
	}

	public List<SupportTicketComments> getComments() {
		return comments;
	}

	public void setComments(List<SupportTicketComments> comments) {
		this.comments = comments;
	}

	public List<SupportTicketReply> getReplies() {
		return replies;
	}

	public void setReplies(List<SupportTicketReply> replies) {
		this.replies = replies;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public Integer getHelpful() {
		return helpful;
	}

	public void setHelpful(Integer helpful) {
		this.helpful = helpful;
	}

	public IdosRegisteredVendor getVendor() {
		return vendor;
	}

	public void setVendor(IdosRegisteredVendor vendor) {
		this.vendor = vendor;
	}

	public String getAttachment() {
		return attachment;
	}

	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}
}