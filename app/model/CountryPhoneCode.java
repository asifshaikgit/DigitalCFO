package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="COUNTRY_PHONE_CODE")
public class CountryPhoneCode extends AbstractBaseModel {
	
	@Column(name="area_code")
	private String areaCode;
	
	@Column(name="country_with_code")
	private String countryWithCode;

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public String getCountryWithCode() {
		return countryWithCode;
	}

	public void setCountryWithCode(String countryWithCode) {
		this.countryWithCode = countryWithCode;
	}
}
