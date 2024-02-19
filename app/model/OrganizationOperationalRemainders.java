package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.idos.util.IdosUtil;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "ORGANIZATION_OPERATIONAL_REMAINDERS")
public class OrganizationOperationalRemainders extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public OrganizationOperationalRemainders() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = IdosUtil.escapeHtml(remarks);
	}

	@Column(name = "requirements")
	private String requiements;

	@Column(name = "due_on") // valid from
	private Date dueOn;

	@Column(name = "valid_to")
	private Date validTo;

	@Column(name = "recurrences")
	private Integer recurrences;

	@Column(name = "alert_for_action")
	private String alertForAction;

	@Column(name = "alert_for_information")
	private String alertForInformation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@Column(name = "remarks")
	private String remarks;

	@Column(name = "LAST_OPERATIONAL_REM_ACTION_DATE")
	private Date lastOperationalRemActionDate;

	public Date getLastOperationalRemActionDate() {
		return lastOperationalRemActionDate;
	}

	public void setLastOperationalRemActionDate(Date lastOperationalRemActionDate) {
		this.lastOperationalRemActionDate = lastOperationalRemActionDate;
	}

	public Date getLastOperationalRemActionDueDated() {
		return lastOperationalRemActionDueDated;
	}

	public void setLastOperationalRemActionDueDated(
			Date lastOperationalRemActionDueDated) {
		this.lastOperationalRemActionDueDated = lastOperationalRemActionDueDated;
	}

	@Column(name = "LAST_OPERATIONAL_REM_ACTION_DUE_DATED")
	private Date lastOperationalRemActionDueDated;

	public String getRequiements() {
		return this.requiements;
	}

	public void setRequiements(String requiements) {
		this.requiements = IdosUtil.escapeHtml(requiements);
	}

	public Date getDueOn() {
		return this.dueOn;
	}

	public void setDueOn(Date dueOn) {
		this.dueOn = dueOn;
	}

	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	public Integer getRecurrences() {
		return this.recurrences;
	}

	public void setRecurrences(Integer recurrences) {
		this.recurrences = recurrences;
	}

	public String getAlertForAction() {
		return this.alertForAction;
	}

	public void setAlertForAction(String alertForAction) {
		this.alertForAction = alertForAction;
	}

	public String getAlertForInformation() {
		return this.alertForInformation;
	}

	public void setAlertForInformation(String alertForInformation) {
		this.alertForInformation = alertForInformation;
	}

	public Organization getOrganization() {
		return this.organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Branch getBranch() {
		return this.branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	/**
	 * Find a OrganizationOptionalRemainders by id.
	 */
	public static OrganizationOperationalRemainders findById(Long id) {
		return entityManager.find(OrganizationOperationalRemainders.class, id);
	}

}
