package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="VENDORGROUP_has_VENDORS")
public class VendorGroupVendors extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="VENDOR_ID")
	private Vendor vendor;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="VENDOR_GROUP_ID")
	private VendorGroup vendorGroup;
	
	@Column(name="TYPE")
	private Integer type;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;
	
	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public VendorGroup getVendorGroup() {
		return vendorGroup;
	}

	public void setVendorGroup(VendorGroup vendorGroup) {
		this.vendorGroup = vendorGroup;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
}
