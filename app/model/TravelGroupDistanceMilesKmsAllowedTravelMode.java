package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="TRAVELGROUP_DISTANCEMILESKMS_ALLOWED_TRAVELMODE")
public class TravelGroupDistanceMilesKmsAllowedTravelMode extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRAVEL_GROUP_ID")
	private Travel_Group travelgroup;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="DISTANCE_MILESKMS_ID")
	private DistanceMilesKm distanceMilesKms;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRAVEL_MODE_ID")
	private TravelMode travelMode;
	
	@Column(name="ONE_WAY_FARE")
	private Double oneWayFare=0.0;
	
	@Column(name="RETURN_FARE")
	private Double returnFare=0.0;

	public Double getOneWayFare() {
		return oneWayFare;
	}

	public void setOneWayFare(Double oneWayFare) {
		this.oneWayFare = oneWayFare;
	}

	public Double getReturnFare() {
		return returnFare;
	}

	public void setReturnFare(Double returnFare) {
		this.returnFare = returnFare;
	}

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

	public DistanceMilesKm getDistanceMilesKms() {
		return distanceMilesKms;
	}

	public void setDistanceMilesKms(DistanceMilesKm distanceMilesKms) {
		this.distanceMilesKms = distanceMilesKms;
	}

	public TravelMode getTravelMode() {
		return travelMode;
	}

	public void setTravelMode(TravelMode travelMode) {
		this.travelMode = travelMode;
	}
	
	public Long getEntityComparableParamId1(){
		return getTravelgroup().getId();
	}
	
	public Long getEntityComparableParamId2(){
		return getTravelMode().getId();
	}
	
	public Long getEntityComparableParamId3(){
		return getDistanceMilesKms().getId();
	}
}
