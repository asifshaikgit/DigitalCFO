package model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Entity
@Table(name="IDOS_CUSTOMERS_SUPPLIER_CONNECT")
public class IdosCustomerSupplierConnect extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IDOS_REGISTERED_VENDOR")
	private IdosRegisteredVendor idosRegVendor;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CONNECTED_ORGANIZATION_ID")
	private Organization organization;

	public IdosRegisteredVendor getIdosRegVendor() {
		return idosRegVendor;
	}

	public void setIdosRegVendor(IdosRegisteredVendor idosRegVendor) {
		this.idosRegVendor = idosRegVendor;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
}
