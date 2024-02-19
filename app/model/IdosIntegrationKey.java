package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "IDOS_INTEGRATION_KEY")
public class IdosIntegrationKey extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public IdosIntegrationKey() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "ORGANIZATION_NAME")
	private String orgName;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "AUTH_KEY")
	private String authKey;

	@Column(name = "NOTE")
	private String note;

	@Column(name = "PRODUCT_NAME")
	private String productName;

	@Column(name = "COMPANY_URL")
	private String companyUrl;

	@Column(name = "PHONE_NUMBER")
	private String phoneNumber;

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAuthKey() {
		return authKey;
	}

	public void setAuthKey(String authKey) {
		this.authKey = authKey;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getCompanyUrl() {
		return companyUrl;
	}

	public void setCompanyUrl(String companyUrl) {
		this.companyUrl = companyUrl;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * Find a Travel_Group by id.
	 */
	public static IdosIntegrationKey findById(Long id) {
		return entityManager.find(IdosIntegrationKey.class, id);
	}

}
