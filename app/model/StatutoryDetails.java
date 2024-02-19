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
@Table(name = "ORGANIZATION_STATUTORY")
public class StatutoryDetails extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public StatutoryDetails() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "statutory_details")
	private String statutoryDetails;

	@Column(name = "registration_number")
	private String registrationNumber;

	@Column(name = "IS_STATUTORY_AVAILABLE_FOR_INVOICE")
	private Integer isStatutoryAvailableForInvoice;

	public Integer getIsStatutoryAvailableForInvoice() {
		return isStatutoryAvailableForInvoice;
	}

	public void setIsStatutoryAvailableForInvoice(Integer isStatutoryAvailableForInvoice) {
		this.isStatutoryAvailableForInvoice = isStatutoryAvailableForInvoice;
	}

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

	@Column(name = "LAST_UPDATED_VALIDITY_DATE")
	private Date lastUpdatedValidityDate;

	public Date getLastUpdatedValidityDate() {
		return lastUpdatedValidityDate;
	}

	public void setLastUpdatedValidityDate(Date lastUpdatedValidityDate) {
		this.lastUpdatedValidityDate = lastUpdatedValidityDate;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = IdosUtil.escapeHtml(remarks);
	}

	public String getStatutoryDetails() {
		return statutoryDetails;
	}

	public void setStatutoryDetails(String statutoryDetails) {
		this.statutoryDetails = IdosUtil.escapeHtml(statutoryDetails);
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

	public String getNameAddressOfConsultant() {
		return nameAddressOfConsultant;
	}

	public void setNameAddressOfConsultant(String nameAddressOfConsultant) {
		this.nameAddressOfConsultant = IdosUtil.escapeHtml(nameAddressOfConsultant);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = IdosUtil.escapeHtml(registrationNumber);
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

	/**
	 * Find an statutorydetails by id.
	 */
	public static StatutoryDetails findById(Long id) {
		return entityManager.find(StatutoryDetails.class, id);
	}
}
