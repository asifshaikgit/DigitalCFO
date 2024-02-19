package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ALERT_MAIL_LOGS")
public class AlertMailLogs extends AbstractBaseModel {
	
	@Column(name="MAIL_FROM")
	private String mailFrom;
	
	@Column(name="MAIL_TO")
	private String mailTo;
	
	@Column(name="MAIL_SUBJECT")
	private String mailSubject;
	
	@Column(name="ALERT_TYPE")
	private Integer alertType;
	
	@Column(name="TASK_ALERT_GROUPING_DATE")
	private Date taskAlertGroupingDate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_STATUTORY_ID")
	private StatutoryDetails branchStatutory;
	
	public StatutoryDetails getBranchStatutory() {
		return branchStatutory;
	}

	public void setBranchStatutory(StatutoryDetails branchStatutory) {
		this.branchStatutory = branchStatutory;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_OPERATIONAL_REMAINDER_ID")
	private OrganizationOperationalRemainders branchOperationalRemainder;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_INSURENCE_ID")
	private BranchInsurance branchInsurence;
	
	@Column(name="CONFIRMATION_STATUS")
	private String confirmationStatus;
	
	@Column(name="ALERT_FOR_ACTION")
	private String alertForAction;
	
	@Column(name="ALERT_FOR_INFORMATION")
	private String alertForInformation;
	
	public String getAlertForAction() {
		return alertForAction;
	}

	public void setAlertForAction(String alertForAction) {
		this.alertForAction = alertForAction;
	}

	public String getAlertForInformation() {
		return alertForInformation;
	}

	public void setAlertForInformation(String alertForInformation) {
		this.alertForInformation = alertForInformation;
	}

	public Integer getAlertType() {
		return alertType;
	}

	public void setAlertType(Integer alertType) {
		this.alertType = alertType;
	}

	public Date getTaskAlertGroupingDate() {
		return taskAlertGroupingDate;
	}

	public void setTaskAlertGroupingDate(Date taskAlertGroupingDate) {
		this.taskAlertGroupingDate = taskAlertGroupingDate;
	}

	public OrganizationOperationalRemainders getBranchOperationalRemainder() {
		return branchOperationalRemainder;
	}

	public void setBranchOperationalRemainder(
			OrganizationOperationalRemainders branchOperationalRemainder) {
		this.branchOperationalRemainder = branchOperationalRemainder;
	}

	public BranchInsurance getBranchInsurence() {
		return branchInsurence;
	}

	public void setBranchInsurence(BranchInsurance branchInsurence) {
		this.branchInsurence = branchInsurence;
	}

	public String getConfirmationStatus() {
		return confirmationStatus;
	}

	public void setConfirmationStatus(String confirmationStatus) {
		this.confirmationStatus = confirmationStatus;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID")
	private Branch branch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ORGANIZATION_ID")
	private Organization organization;

	public String getMailFrom() {
		return mailFrom;
	}

	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}

	public String getMailTo() {
		return mailTo;
	}

	public void setMailTo(String mailTo) {
		this.mailTo = mailTo;
	}

	public String getMailSubject() {
		return mailSubject;
	}

	public void setMailSubject(String mailSubject) {
		this.mailSubject = mailSubject;
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
}
