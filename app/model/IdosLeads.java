package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "IDOS_LEADS")
public class IdosLeads extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public IdosLeads() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final long serialVersionUID = 1L;

	@Column(name = "NAME")
	private String name;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "COMPANY_NAME")
	private String companyName;

	@Column(name = "PHONE")
	private String phone;

	@Column(name = "SOURCE")
	private String source;

	@Column(name = "ENQUIRY_TYPE")
	private Integer enquiryType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Integer getEnquiryType() {
		return this.enquiryType;
	}

	public void setEnquiryType(Integer enquiryType) {
		this.enquiryType = enquiryType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IdosLeads [name=");
		builder.append(name);
		builder.append(", email=");
		builder.append(email);
		builder.append(", companyName=");
		builder.append(companyName);
		builder.append(", phone=");
		builder.append(phone);
		builder.append(", source=");
		builder.append(source);
		builder.append("]");
		return builder.toString();
	}

	public static IdosLeads findById(final long id) {
		return entityManager.find(IdosLeads.class, id);
	}
}
