package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@Table(name="GST_SERVICES_CODES")
public class GSTServicesCodes extends AbstractBaseModel{
	
	@Column(name="SERVICE_DESCRIPTION")
	private String serviceDescription;
	
	@Column(name="GST_COUNTRY_CODE")
	private String GSTCountryCode;
	
	@Column(name="TAX_RATE")
	private Double taxRate;
	
	@Column(name="GROUP_ID")
	private Integer groupId;
	
	@Column(name="SERVICE_CODE")
	private String serviceCode;

	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	public String getGSTCountryCode() {
		return GSTCountryCode;
	}

	public void setGSTCountryCode(String gSTCountryCode) {
		GSTCountryCode = gSTCountryCode;
	}

	public Double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Double taxRate) {
		this.taxRate = taxRate;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}
	
	

}
