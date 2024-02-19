package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="users_claim_coa_hotel")
public class UsersClaimCOAHotel extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id")
	private Users user;
	
	@Column(name="COUNTRY_CAPITAL_LIMIT_FROM")
	private Double countryCapitalLimitFrom;
	
	@Column(name="COUNTRY_CAPITAL_LIMIT_TO")
	private Double countryCapitalLimitTo;
	
	@Column(name="STATE_CAPITAL_LIMIT_FROM")
	private Double stateCapitalLimitFrom;
	
	@Column(name="STATE_CAPITAL_LIMIT_TO")
	private Double stateCapitalLimitTo;
	
	@Column(name="OTHER_LOCATION_CAPITAL_LIMIT_FROM")
	private Double otherLocationCapitalLimitFrom;
	
	@Column(name="OTHER_LOCATION_CAPITAL_LIMIT_TO")
	private Double otherLocationCapitalLimitTo;

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public Double getCountryCapitalLimitFrom() {
		return countryCapitalLimitFrom;
	}

	public void setCountryCapitalLimitFrom(Double countryCapitalLimitFrom) {
		this.countryCapitalLimitFrom = countryCapitalLimitFrom;
	}

	public Double getCountryCapitalLimitTo() {
		return countryCapitalLimitTo;
	}

	public void setCountryCapitalLimitTo(Double countryCapitalLimitTo) {
		this.countryCapitalLimitTo = countryCapitalLimitTo;
	}

	public Double getStateCapitalLimitFrom() {
		return stateCapitalLimitFrom;
	}

	public void setStateCapitalLimitFrom(Double stateCapitalLimitFrom) {
		this.stateCapitalLimitFrom = stateCapitalLimitFrom;
	}

	public Double getStateCapitalLimitTo() {
		return stateCapitalLimitTo;
	}

	public void setStateCapitalLimitTo(Double stateCapitalLimitTo) {
		this.stateCapitalLimitTo = stateCapitalLimitTo;
	}

	public Double getOtherLocationCapitalLimitFrom() {
		return otherLocationCapitalLimitFrom;
	}

	public void setOtherLocationCapitalLimitFrom(
			Double otherLocationCapitalLimitFrom) {
		this.otherLocationCapitalLimitFrom = otherLocationCapitalLimitFrom;
	}

	public Double getOtherLocationCapitalLimitTo() {
		return otherLocationCapitalLimitTo;
	}

	public void setOtherLocationCapitalLimitTo(Double otherLocationCapitalLimitTo) {
		this.otherLocationCapitalLimitTo = otherLocationCapitalLimitTo;
	}
}
