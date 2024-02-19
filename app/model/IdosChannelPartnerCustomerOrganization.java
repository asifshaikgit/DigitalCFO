package model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="")
public class IdosChannelPartnerCustomerOrganization extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IDOS_CHANNEL_PARTNER_ID")
	private IdosChannelPartner idosChannelPartner;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IDOS_CHANNEL_PARTNER_CUSTOMER_ORGANIZATION_ID")
	private Organization customerOrganization;

	public IdosChannelPartner getIdosChannelPartner() {
		return idosChannelPartner;
	}

	public void setIdosChannelPartner(IdosChannelPartner idosChannelPartner) {
		this.idosChannelPartner = idosChannelPartner;
	}

	public Organization getCustomerOrganization() {
		return customerOrganization;
	}

	public void setCustomerOrganization(Organization customerOrganization) {
		this.customerOrganization = customerOrganization;
	}
}
