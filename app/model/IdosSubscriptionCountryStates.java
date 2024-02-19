package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="IDOS_SUBSCRIPTION_COUNTRY_STATES")
public class IdosSubscriptionCountryStates extends AbstractBaseModel {
	
	@Column(name="STATE_NAME")
	private String stateName;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="COUNTRY_ID")
	private IdosSubscriptionCountry idosSubscriptionCountry;
	
	@OneToMany(fetch=FetchType.LAZY,mappedBy="idosSubscriptionCountryStates")
	private List<IdosCountryStatesCity> idosCountryCities;

	public String getStateName() {
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public IdosSubscriptionCountry getIdosSubscriptionCountry() {
		return idosSubscriptionCountry;
	}

	public void setIdosSubscriptionCountry(
			IdosSubscriptionCountry idosSubscriptionCountry) {
		this.idosSubscriptionCountry = idosSubscriptionCountry;
	}

	public List<IdosCountryStatesCity> getIdosCountryCities() {
		return idosCountryCities;
	}

	public void setIdosCountryCities(List<IdosCountryStatesCity> idosCountryCities) {
		this.idosCountryCities = idosCountryCities;
	}
	
}
