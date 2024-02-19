package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Query;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "SPECIFICS_COMBINATION_SALES")
public class SpecificsCombinationSales extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public SpecificsCombinationSales() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COMB_SPECIFICS_ID")
	private Specifics combSpecificsId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SPECIFICS_ID")
	private Specifics specificsId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@Column(name = "OPENING_BALANCE_UNITS")
	private Double openingBalUnits;

	@Column(name = "OPENING_BALANCE_RATE")
	private Double openingBalRate;

	public Specifics getCombSpecificsId() {
		return combSpecificsId;
	}

	public void setCombSpecificsId(Specifics combSpecificsId) {
		this.combSpecificsId = combSpecificsId;
	}

	public Specifics getSpecificsId() {
		return specificsId;
	}

	public void setSpecificsId(Specifics specificsId) {
		this.specificsId = specificsId;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Double getOpeningBalUnits() {
		return openingBalUnits;
	}

	public void setOpeningBalUnits(Double openingBalUnits) {
		this.openingBalUnits = openingBalUnits;
	}

	public Double getOpeningBalRate() {
		return openingBalRate;
	}

	public void setOpeningBalRate(Double openingBalRate) {
		this.openingBalRate = openingBalRate;
	}

	public static SpecificsCombinationSales findById(Long id) {
		return entityManager.find(SpecificsCombinationSales.class, id);
	}

	/*
	 * public static CustomerDetail findByCustomerID(Long customerId){
	 * CustomerDetail customerDetail = null;
	 * try {
	 * Query query = JPA.em().createQuery(CUSTOMER_DETAIL_HQL);
	 * query.setParameter(1, customerId);
	 * customerDetail = (CustomerDetail)query.getSingleResult();
	 * }
	 * catch (NoResultException ex){
	 * }
	 * return customerDetail;
	 * }
	 */

}
