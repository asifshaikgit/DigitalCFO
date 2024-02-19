package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="IDOS_REGISTERED_VENDOR_CONTACTED")
public class IdosRegisteredVendorContacted extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CONTACTED_BY")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IDOS_REGISTERED_VENDOR")
	private IdosRegisteredVendor idosRegisteredVendor;
	
	@Column(name="CONTACTED_DATE")
	private Date contactedDate;
	
	@Column(name="CONTACTED_BY_USER")
	private String contactedByUserEmail;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Date getContactedDate() {
		return contactedDate;
	}

	public void setContactedDate(Date contactedDate) {
		this.contactedDate = contactedDate;
	}

	public IdosRegisteredVendor getIdosRegisteredVendor() {
		return idosRegisteredVendor;
	}

	public void setIdosRegisteredVendor(IdosRegisteredVendor idosRegisteredVendor) {
		this.idosRegisteredVendor = idosRegisteredVendor;
	}

	public String getContactedByUserEmail() {
		return contactedByUserEmail;
	}

	public void setContactedByUserEmail(String contactedByUserEmail) {
		this.contactedByUserEmail = contactedByUserEmail;
	}
}
