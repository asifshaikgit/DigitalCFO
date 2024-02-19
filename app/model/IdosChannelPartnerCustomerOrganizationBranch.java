package model;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import javax.persistence.Entity;

@Entity
@Table(name="IDOS_CHANNEL_PARTNER_CUSTOMER_ORGANIZATION_BRANCH")
public class IdosChannelPartnerCustomerOrganizationBranch extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IDOS_CHANNEL_PARTNER_ID")
	private IdosChannelPartner idosChannelPartner;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IDOS_CHANNEL_PARTNER_CUSTOMER_ORGANIZATION_ID")
	private Organization customerOrganization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IDOS_CHANNEL_PARTNER_CUSTOMER_ORGANIZATION_BRANCH_ID")
	private Branch customerOrganizatioNbRANCH;

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

	public Branch getCustomerOrganizatioNbRANCH() {
		return customerOrganizatioNbRANCH;
	}

	public void setCustomerOrganizatioNbRANCH(Branch customerOrganizatioNbRANCH) {
		this.customerOrganizatioNbRANCH = customerOrganizatioNbRANCH;
	}
	
	public Long getEntityComparableParamId1(){
		return getCustomerOrganization().getId();
	}
	
	public Long getEntityComparableParamId2(){
		return getCustomerOrganizatioNbRANCH().getId();
	}
}
