package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="IDOS_COUNTRY_STATE_CITY")
public class IdosCountryStatesCity extends AbstractBaseModel {
	
	@Column(name="CITY_NAME")
	private String cityName;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IDOS_COUNTRY_ID")
	private IdosSubscriptionCountry idosSubscriptionCountry;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IDOS_COUNTRY_STATE_ID")
	private IdosSubscriptionCountryStates idosSubscriptionCountryStates;

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public IdosSubscriptionCountry getIdosSubscriptionCountry() {
		return idosSubscriptionCountry;
	}

	public void setIdosSubscriptionCountry(
			IdosSubscriptionCountry idosSubscriptionCountry) {
		this.idosSubscriptionCountry = idosSubscriptionCountry;
	}

	public IdosSubscriptionCountryStates getIdosSubscriptionCountryStates() {
		return idosSubscriptionCountryStates;
	}

	public void setIdosSubscriptionCountryStates(
			IdosSubscriptionCountryStates idosSubscriptionCountryStates) {
		this.idosSubscriptionCountryStates = idosSubscriptionCountryStates;
	}
}
