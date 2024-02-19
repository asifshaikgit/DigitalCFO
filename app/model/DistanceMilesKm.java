package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "DISTANCE_MILES_KMS")
public class DistanceMilesKm extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public DistanceMilesKm() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "DISTANCE_MILES_KMS")
	private String distanceInMilesKms;

	public String getDistanceInMilesKms() {
		return distanceInMilesKms;
	}

	public void setDistanceInMilesKms(String distanceInMilesKms) {
		this.distanceInMilesKms = distanceInMilesKms;
	}

	/**
	 * Find a DistanceMilesKm by id.
	 */
	public static DistanceMilesKm findById(Long id) {
		return entityManager.find(DistanceMilesKm.class, id);
	}
}
