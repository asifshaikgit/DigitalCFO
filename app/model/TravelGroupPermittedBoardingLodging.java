package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="TRAVELGROUP_PERMITTED_BOARDINGLODGING")
public class TravelGroupPermittedBoardingLodging extends AbstractBaseModel{
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRAVEL_GROUP_ID")
	private Travel_Group travelgroup;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ACCOMODATION_TYPE_ID")
	private AccomodationType accomodationType;
	
	@Column(name="MAX_PERMITTED_ROOM_COST_PER_NIGHT")
	private Double maxPermittedRoomCostPerNight=0.0;
	
	@Column(name="MAX_PERMITTED_FOOD_COST_PER_DAY")
	private Double maxPermittedFoodCostPerDay=0.0;
	
	@Column(name="CITY_TYPE")
	private Integer cityType;

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

	public AccomodationType getAccomodationType() {
		return accomodationType;
	}

	public void setAccomodationType(AccomodationType accomodationType) {
		this.accomodationType = accomodationType;
	}

	public Double getMaxPermittedRoomCostPerNight() {
		return maxPermittedRoomCostPerNight;
	}

	public void setMaxPermittedRoomCostPerNight(Double maxPermittedRoomCostPerNight) {
		this.maxPermittedRoomCostPerNight = maxPermittedRoomCostPerNight;
	}

	public Double getMaxPermittedFoodCostPerDay() {
		return maxPermittedFoodCostPerDay;
	}

	public void setMaxPermittedFoodCostPerDay(Double maxPermittedFoodCostPerDay) {
		this.maxPermittedFoodCostPerDay = maxPermittedFoodCostPerDay;
	}
	
	public Long getEntityComparableParamId(){
		return getTravelgroup().getId();
	}
	
	public Long getEntityComparableParamId1(){
		return getAccomodationType().getId();
	}

	public Integer getCityType() {
		return cityType;
	}

	public void setCityType(Integer cityType) {
		this.cityType = cityType;
	}
}
