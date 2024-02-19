package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="TRAVELGROUP_MAX_DAILY_LIMIT_OTHER_EXPENSE")
public class TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRAVEL_GROUP_ID")
	private Travel_Group travelgroup;
	
	@Column(name="COUNTRY_CAPITAL")
	private Double countryCapital=0.0;
	
	@Column(name="STATE_CAPITAL")
	private Double stateCapital=0.0;
	
	@Column(name="METRO_CITY")
	private Double metroCity=0.0;
	
	@Column(name="OTHER_CITY")
	private Double otherCities=0.0;
	
	@Column(name="TOWN")
	private Double town=0.0;
	
	@Column(name="COUNTY")
	private Double county=0.0;
	
	@Column(name="MUNICIPALITY")
	private Double municipality=0.0;
	
	@Column(name="VILLAGE")
	private Double village=0.0;
	
	@Column(name="REMOTE_LOCATION")
	private Double remoteLocation=0.0;
	
	@Column(name="TWENTY_MILES_AWAY_CLOSEST_CITY_TOWN")
	private Double twentyMilesAwayFromClosestCityTown;
	
	@Column(name="HILL_STATION")
	private Double hillStation=0.0;
	
	@Column(name="RESORT")
	private Double resort=0.0;
	
	@Column(name="CONFLICT_WARZONE_PLACE")
	private Double conflictWarZonePlace=0.0;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Travel_Group getTravelgroup() {
		return travelgroup;
	}

	public void setTravelgroup(Travel_Group travelgroup) {
		this.travelgroup = travelgroup;
	}

	public Double getCountryCapital() {
		return countryCapital;
	}

	public void setCountryCapital(Double countryCapital) {
		this.countryCapital = countryCapital;
	}

	public Double getStateCapital() {
		return stateCapital;
	}

	public void setStateCapital(Double stateCapital) {
		this.stateCapital = stateCapital;
	}

	public Double getMetroCity() {
		return metroCity;
	}

	public void setMetroCity(Double metroCity) {
		this.metroCity = metroCity;
	}

	public Double getOtherCities() {
		return otherCities;
	}

	public void setOtherCities(Double otherCities) {
		this.otherCities = otherCities;
	}

	public Double getTown() {
		return town;
	}

	public void setTown(Double town) {
		this.town = town;
	}

	public Double getCounty() {
		return county;
	}

	public void setCounty(Double county) {
		this.county = county;
	}

	public Double getMunicipality() {
		return municipality;
	}

	public void setMunicipality(Double municipality) {
		this.municipality = municipality;
	}

	public Double getVillage() {
		return village;
	}

	public void setVillage(Double village) {
		this.village = village;
	}

	public Double getRemoteLocation() {
		return remoteLocation;
	}

	public void setRemoteLocation(Double remoteLocation) {
		this.remoteLocation = remoteLocation;
	}

	public Double getTwentyMilesAwayFromClosestCityTown() {
		return twentyMilesAwayFromClosestCityTown;
	}

	public void setTwentyMilesAwayFromClosestCityTown(
			Double twentyMilesAwayFromClosestCityTown) {
		this.twentyMilesAwayFromClosestCityTown = twentyMilesAwayFromClosestCityTown;
	}

	public Double getHillStation() {
		return hillStation;
	}

	public void setHillStation(Double hillStation) {
		this.hillStation = hillStation;
	}

	public Double getResort() {
		return resort;
	}

	public void setResort(Double resort) {
		this.resort = resort;
	}

	public Double getConflictWarZonePlace() {
		return conflictWarZonePlace;
	}

	public void setConflictWarZonePlace(Double conflictWarZonePlace) {
		this.conflictWarZonePlace = conflictWarZonePlace;
	}
}
