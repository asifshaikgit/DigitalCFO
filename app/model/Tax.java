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
@Table(name = "TAX")
public class Tax extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Tax() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "name")
	private String name;

	@Column(name = "rate")
	private Double rate;

	@Column(name = "tax_impact_type")
	private Integer taxImpactType;

	@Column(name = "tax_impact_on")
	private Integer taxImpactOn;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "taxSpecifics")
	private List<SpecificsTax> taxSpecifics;

	public List<TaxBranches> getTaxBranches() {
		return taxBranches;
	}

	public void setTaxBranches(List<TaxBranches> taxBranches) {
		this.taxBranches = taxBranches;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "taxBranches")
	private List<TaxBranches> taxBranches;

	public Integer getTaxImpactOn() {
		return taxImpactOn;
	}

	public void setTaxImpactOn(Integer taxImpactOn) {
		this.taxImpactOn = taxImpactOn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<SpecificsTax> getTaxSpecifics() {
		return taxSpecifics;
	}

	public void setTaxSpecifics(List<SpecificsTax> taxSpecifics) {
		this.taxSpecifics = taxSpecifics;
	}

	public Double getRate() {
		return rate;
	}

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public Integer getTaxImpactType() {
		return taxImpactType;
	}

	public void setTaxImpactType(Integer taxImpactType) {
		this.taxImpactType = taxImpactType;
	}

	/**
	 * Find tax by id.
	 */
	public static Tax findById(Long id) {
		return entityManager.find(Tax.class, id);
	}
}
