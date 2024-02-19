package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="IDOS_COUNTRY_STATE_TAX_RATES")
public class IdosCountryStateTaxRate extends AbstractBaseModel {
	
	@Column(name="TAX_RATE")
	private Double taxRate;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUBSCRIPTON_COUNTRY_ID")
	private IdosSubscriptionCountry idosSubscriptionCountry;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="SUBSCRIPTON_STATE_ID")
	private IdosSubscriptionCountryStates idosSubscriptionCountryStates;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="IDOS_SUBSCRIPTION_TAX")
	private IdosSubscriptionTax idosSubscriptionTax;

	public Double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Double taxRate) {
		this.taxRate = taxRate;
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

	public IdosSubscriptionTax getIdosSubscriptionTax() {
		return idosSubscriptionTax;
	}

	public void setIdosSubscriptionTax(IdosSubscriptionTax idosSubscriptionTax) {
		this.idosSubscriptionTax = idosSubscriptionTax;
	}
}
