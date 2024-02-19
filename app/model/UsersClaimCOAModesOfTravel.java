package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="users_claim_coa_modes_of_travel")
public class UsersClaimCOAModesOfTravel extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id")
	private Users user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID")
	private Branch branch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ORGANIZATION_ID")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="travel_mode")
	private TravelMode travelMode;
	
	@Column(name="DISTANCE_FROM")
	private Double distanceFrom;
	
	@Column(name="DISTANCE_TO")
	private Double distanceTo;
	
	@Column(name="MONETARY_LIMIT_FROM")
	private Double monetoryLimitFrom;
	
	@Column(name="MONETARY_LIMIT_TO")
	private Double monetoryLimitTo;

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public TravelMode getTravelMode() {
		return travelMode;
	}

	public void setTravelMode(TravelMode travelMode) {
		this.travelMode = travelMode;
	}

	public Double getDistanceFrom() {
		return distanceFrom;
	}

	public void setDistanceFrom(Double distanceFrom) {
		this.distanceFrom = distanceFrom;
	}

	public Double getDistanceTo() {
		return distanceTo;
	}

	public void setDistanceTo(Double distanceTo) {
		this.distanceTo = distanceTo;
	}

	public Double getMonetoryLimitFrom() {
		return monetoryLimitFrom;
	}

	public void setMonetoryLimitFrom(Double monetoryLimitFrom) {
		this.monetoryLimitFrom = monetoryLimitFrom;
	}

	public Double getMonetoryLimitTo() {
		return monetoryLimitTo;
	}

	public void setMonetoryLimitTo(Double monetoryLimitTo) {
		this.monetoryLimitTo = monetoryLimitTo;
	}
}
