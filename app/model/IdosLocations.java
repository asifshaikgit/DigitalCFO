package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="IDOS_LOCATIONS")
public class IdosLocations extends AbstractBaseModel{
	
	@Column(name="LOCATION_NAME")
	private String locationName;

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}
}
