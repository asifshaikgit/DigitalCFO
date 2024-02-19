package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;

@Entity
@Table(name = "MODES_OF_TRAVEL")
public class TravelMode extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public TravelMode() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "TRAVEL_MODE_NAME")
	private String travelModeName;

	public String getTravelModeName() {
		return travelModeName;
	}

	public void setTravelModeName(String travelModeName) {
		this.travelModeName = travelModeName;
	}

	/**
	 * Find a TravelMode by id.
	 */
	public static TravelMode findById(Long id) {
		return entityManager.find(TravelMode.class, id);
	}
}
