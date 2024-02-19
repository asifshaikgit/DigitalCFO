package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "IDOS_CHANNEL_PARTNER")
public class IdosChannelPartner extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public IdosChannelPartner() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "CHANNEL_PARTNER_NAME")
	private String channelPartnerName;

	@Column(name = "CHANNEL_PARTNER_EMAIL")
	private String channelPartnerEmail;

	@Column(name = "CHANNEL_PARTNER_ADDRESS")
	private String channelPartnerAddress;

	@Column(name = "CHANNEL_PARTNER_PHNNUMBER")
	private String channelPartnerPHNnumber;

	@Column(name = "CHANNEL_PARTNER_COUNTRY")
	private String channelPartnerCountry;

	@Column(name = "CHANNEL_PARTNER_PANCARDNUMBER")
	private String channelPartnerPanCardNumber;

	@Column(name = "CHANNEL_PARTNER_CONTRACTDOCUMENT")
	private String channelPartnerContractDocument;

	@Column(name = "CHANNEL_PARTNER_ACCOUNTNUMBER")
	private String channelPartnerAccountNumber;

	public List<IdosChannelPartnerCustomerOrganizationBranch> getIdosChannelPartnerCustomerOrgBnchList() {
		return idosChannelPartnerCustomerOrgBnchList;
	}

	public void setIdosChannelPartnerCustomerOrgBnchList(
			List<IdosChannelPartnerCustomerOrganizationBranch> idosChannelPartnerCustomerOrgBnchList) {
		this.idosChannelPartnerCustomerOrgBnchList = idosChannelPartnerCustomerOrgBnchList;
	}

	@Column(name = "PASSWORD")
	private String password;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "idosChannelPartner")
	private List<IdosChannelPartnerCustomerOrganizationBranch> idosChannelPartnerCustomerOrgBnchList;

	public String getChannelPartnerName() {
		return channelPartnerName;
	}

	public void setChannelPartnerName(String channelPartnerName) {
		this.channelPartnerName = channelPartnerName;
	}

	public String getChannelPartnerEmail() {
		return channelPartnerEmail;
	}

	public void setChannelPartnerEmail(String channelPartnerEmail) {
		this.channelPartnerEmail = channelPartnerEmail;
	}

	public String getChannelPartnerAddress() {
		return channelPartnerAddress;
	}

	public void setChannelPartnerAddress(String channelPartnerAddress) {
		this.channelPartnerAddress = channelPartnerAddress;
	}

	public String getChannelPartnerPHNnumber() {
		return channelPartnerPHNnumber;
	}

	public void setChannelPartnerPHNnumber(String channelPartnerPHNnumber) {
		this.channelPartnerPHNnumber = channelPartnerPHNnumber;
	}

	public String getChannelPartnerCountry() {
		return channelPartnerCountry;
	}

	public void setChannelPartnerCountry(String channelPartnerCountry) {
		this.channelPartnerCountry = channelPartnerCountry;
	}

	public String getChannelPartnerPanCardNumber() {
		return channelPartnerPanCardNumber;
	}

	public void setChannelPartnerPanCardNumber(String channelPartnerPanCardNumber) {
		this.channelPartnerPanCardNumber = channelPartnerPanCardNumber;
	}

	public String getChannelPartnerContractDocument() {
		return channelPartnerContractDocument;
	}

	public void setChannelPartnerContractDocument(
			String channelPartnerContractDocument) {
		this.channelPartnerContractDocument = channelPartnerContractDocument;
	}

	public String getChannelPartnerAccountNumber() {
		return channelPartnerAccountNumber;
	}

	public void setChannelPartnerAccountNumber(String channelPartnerAccountNumber) {
		this.channelPartnerAccountNumber = channelPartnerAccountNumber;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Find a IdosChannelPartner by id.
	 */
	public static IdosChannelPartner findById(Long id) {
		return entityManager.find(IdosChannelPartner.class, id);
	}

}
