package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.idos.util.IdosUtil;
import play.data.validation.Constraints;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "ORGANIZATION_KEY_OFFICIALS")
public class OrganizationKeyOfficials extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public OrganizationKeyOfficials() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "name")
	private String name;

	@Column(name = "email", length = 256, unique = true, nullable = false)
	@Constraints.MaxLength(256)
	@Constraints.Required
	@Constraints.Email
	private String email;

	@Column(name = "country_phone_code")
	private String ctryPhCode;

	@Column(name = "personal_phone_country_code")
	private String personalPhoneCountryCode;

	@Column(name = "personal_phone_number")
	private String personalPhoneNumber;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "uploaded_id_url")
	private String uploadedId;

	@Column(name = "UPLOADED_KYC_URL")
	private String uploadedKycId;

	@Column(name = "country")
	private Integer country;

	@Column(name = "city")
	private String city;

	public Integer getCountry() {
		return country;
	}

	public void setCountry(Integer country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = IdosUtil.escapeHtml(city);
	}

	public String getUploadedId() {
		return uploadedId;
	}

	public void setUploadedId(String uploadedId) {
		this.uploadedId = uploadedId;
	}

	@Column(name = "designation")
	private String designation;

	@Column(name = "alert")
	private String alert;

	public String getAlert() {
		return alert;
	}

	public void setAlert(String alert) {
		this.alert = IdosUtil.escapeHtml(alert);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@Column(name = "personal_address")
	private String personalAddress;

	public String getPersonalPhoneCountryCode() {
		return personalPhoneCountryCode;
	}

	public void setPersonalPhoneCountryCode(String personalPhoneCountryCode) {
		this.personalPhoneCountryCode = personalPhoneCountryCode;
	}

	public String getPersonalPhoneNumber() {
		return personalPhoneNumber;
	}

	public void setPersonalPhoneNumber(String personalPhoneNumber) {
		this.personalPhoneNumber = personalPhoneNumber;
	}

	public String getPersonalAddress() {
		return personalAddress;
	}

	public void setPersonalAddress(String personalAddress) {
		this.personalAddress = IdosUtil.escapeHtml(personalAddress);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	public Branch getBranch() {
		return this.branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = IdosUtil.escapeHtml(designation);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = IdosUtil.escapeHtml(name);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getCtryPhCode() {
		return ctryPhCode;
	}

	public void setCtryPhCode(String ctryPhCode) {
		this.ctryPhCode = ctryPhCode;
	}

	/**
	 * Find a Project by id.
	 */
	public static OrganizationKeyOfficials findById(Long id) {
		return entityManager.find(OrganizationKeyOfficials.class, id);
	}

	public String getUploadedKycId() {
		return uploadedKycId;
	}

	public void setUploadedKycId(String uploadedKycId) {
		this.uploadedKycId = uploadedKycId;
	}

}
