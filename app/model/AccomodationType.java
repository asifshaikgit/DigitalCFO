package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "ACCOMODATION_TYPE")
public class AccomodationType extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public AccomodationType() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "ACCOMODATION_TYPE_NAME")
	private String accomodationTypeName;

	public String getAccomodationTypeName() {
		return accomodationTypeName;
	}

	public void setAccomodationTypeName(String accomodationTypeName) {
		this.accomodationTypeName = accomodationTypeName;
	}

	/**
	 * Find a AccomodationType by id.
	 */
	public static AccomodationType findById(Long id) {
		return entityManager.find(AccomodationType.class, id);
	}
}
