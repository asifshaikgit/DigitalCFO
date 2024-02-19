package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "ORGANIZATION_STATUTORY_DETAILS")
public class OrganizationStatutoryDetails extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public OrganizationStatutoryDetails() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "statutory_details")
	private String statutoryDetails;

	@Column(name = "registration_number")
	private String registrationNumber;

	@Column(name = "registration_doc")
	private String registrationDoc;

	@Column(name = "valid_from")
	private Date validFrom;

	@Column(name = "valid_To")
	private Date validTo;

	@Column(name = "alert_for_action")
	private String alertForAction;

	@Column(name = "alert_for_information")
	private String alertForInformation;

	@Column(name = "name_address_of_consultant")
	private String nameAddressOfConsultant;

	@Column(name = "remarks")
	private String remarks;

	public String getNameAddressOfConsultant() {
		return nameAddressOfConsultant;
	}

	public void setNameAddressOfConsultant(String nameAddressOfConsultant) {
		this.nameAddressOfConsultant = nameAddressOfConsultant;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	public String getStatutoryDetails() {
		return statutoryDetails;
	}

	public void setStatutoryDetails(String statutoryDetails) {
		this.statutoryDetails = statutoryDetails;
	}

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

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public String getRegistrationDoc() {
		return registrationDoc;
	}

	public void setRegistrationDoc(String registrationDoc) {
		this.registrationDoc = registrationDoc;
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTo() {
		return validTo;
	}

	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	/**
	 * Find an organizationstatutorydetails by id.
	 */
	public static OrganizationStatutoryDetails findById(Long id) {
		return entityManager.find(OrganizationStatutoryDetails.class, id);
	}
}
