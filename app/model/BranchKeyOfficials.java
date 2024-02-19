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
@Table(name = "BRANCH_KEY_OFFICIALS")
public class BranchKeyOfficials extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BranchKeyOfficials() {
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
	private String countryPhnCode;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "designation")
	private String designation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	public String getDesignation() {
		return designation;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
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

	public String getCountryPhnCode() {
		return countryPhnCode;
	}

	public void setCountryPhnCode(String countryPhnCode) {
		this.countryPhnCode = countryPhnCode;
	}

	/**
	 * Find a BranchKeyOfficials by id.
	 */
	public static BranchKeyOfficials findById(Long id) {
		return entityManager.find(BranchKeyOfficials.class, id);
	}
}
