package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "IDOS_SUBSCRIPTION_COUNTRY")
public class IdosSubscriptionCountry extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public IdosSubscriptionCountry() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "COUNTRY_NAME")
	private String countryName;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "idosSubscriptionCountry")
	private List<IdosSubscriptionCountryStates> idosSubscriptionCountryStates;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "idosSubscriptionCountry")
	private List<IdosCountryStatesCity> idosSubscriptionCountryCities;

	public List<IdosSubscriptionCountryStates> getIdosSubscriptionCountryStates() {
		return idosSubscriptionCountryStates;
	}

	public void setIdosSubscriptionCountryStates(
			List<IdosSubscriptionCountryStates> idosSubscriptionCountryStates) {
		this.idosSubscriptionCountryStates = idosSubscriptionCountryStates;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	/**
	 * Find a IdosSubscriptionCountry by id.
	 */
	public static IdosSubscriptionCountry findById(Long id) {
		return entityManager.find(IdosSubscriptionCountry.class, id);
	}

	public List<IdosCountryStatesCity> getIdosSubscriptionCountryCities() {
		return idosSubscriptionCountryCities;
	}

	public void setIdosSubscriptionCountryCities(
			List<IdosCountryStatesCity> idosSubscriptionCountryCities) {
		this.idosSubscriptionCountryCities = idosSubscriptionCountryCities;
	}
}
