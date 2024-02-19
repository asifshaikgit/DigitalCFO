package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "IDOS_CHANNEL_PARTNER_ALLOTE_ORGANIZATION")
public class IdosChannelPartnerAlloteOrganization extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	private static final long serialVersionUID = 1L;

	public IdosChannelPartnerAlloteOrganization() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	public IdosChannelPartnerAlloteOrganization(String organizationName,
			String contactPerson, String email, Date closingDate) {
		super();
		this.organizationName = organizationName;
		this.contactPerson = contactPerson;
		this.email = email;
		this.closingDate = closingDate;
	}

	public IdosChannelPartnerAlloteOrganization(String organizationName,
			String contactPerson, String email, String phone, Date closingDate) {
		super();
		this.organizationName = organizationName;
		this.contactPerson = contactPerson;
		this.email = email;
		this.phone = phone;
		this.closingDate = closingDate;
	}

	@Column(name = "ORGANIZATION_NAME")
	private String organizationName;

	@Column(name = "CONTACT_PERSON")
	private String contactPerson;

	@Column(name = "EMAIL")
	private String email;

	@Column(name = "PHONE")
	private String phone;

	@Column(name = "CLOSING_DATE")
	private Date closingDate;

	@Column(name = "COMMITMENT_STATUS")
	private String commitmentStatus;

	@ManyToOne
	@JoinColumn(name = "IDOS_CHANNEL_PARTNER_ID")
	private IdosChannelPartner channelPartner;

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Date getClosingDate() {
		return closingDate;
	}

	public void setClosingDate(Date closingDate) {
		this.closingDate = closingDate;
	}

	public IdosChannelPartner getChannelPartner() {
		return channelPartner;
	}

	public void setChannelPartner(IdosChannelPartner channelPartner) {
		this.channelPartner = channelPartner;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IdosChannelPartnerAlloteOrganization [organizationName=");
		builder.append(organizationName);
		builder.append(", contactPerson=");
		builder.append(contactPerson);
		builder.append(", email=");
		builder.append(email);
		builder.append(", phone=");
		builder.append(phone);
		builder.append(", closingDate=");
		builder.append(closingDate);
		builder.append(", channelPartner=");
		builder.append(channelPartner);
		builder.append("]");
		return builder.toString();
	}

	public static IdosChannelPartnerAlloteOrganization findById(final Long id) {
		return entityManager.find(IdosChannelPartnerAlloteOrganization.class, id);
	}

	public String getCommitmentStatus() {
		return commitmentStatus;
	}

	public void setCommitmentStatus(String commitmentStatus) {
		this.commitmentStatus = commitmentStatus;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
