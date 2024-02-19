package model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "SPECIFIC_TAX_HISTORY")
public class SpecificTaxHistory extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public SpecificTaxHistory() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final long serialVersionUID = 1L;
	private static final String SPECIFIC_HISTORY = "select obj from SpecificTaxHistory obj where obj.organization.id= ?1 and obj.specifics.id= ?2 and obj.presentStatus=1 ORDER by obj.applicableDate DESC";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGNIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SPECIFIC_ID")
	private Specifics specifics;

	@Column(name = "APPLICABLE_DATE")
	private Date applicableDate;

	@Column(name = "TAX_RATE")
	private Double taxRate;

	@Column(name = "CESS_RATE")
	private Double cessRate;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Specifics getSpecifics() {
		return specifics;
	}

	public void setSpecifics(Specifics specifics) {
		this.specifics = specifics;
	}

	public Date getApplicableDate() {
		return applicableDate;
	}

	public void setApplicableDate(Date applicableDate) {
		this.applicableDate = applicableDate;
	}

	public Double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Double taxRate) {
		this.taxRate = taxRate;
	}

	public Double getCessRate() {
		return cessRate;
	}

	public void setCessRate(Double cessRate) {
		this.cessRate = cessRate;
	}

	public static SpecificTaxHistory findLastUpdatedTaxRate(Long orgid, Long specificid) {
		List<SpecificTaxHistory> findSpecificHistory = findSpecificHistory(entityManager, orgid, specificid);
		if (findSpecificHistory != null && findSpecificHistory.size() > 0) {
			return findSpecificHistory.get(0);
		}
		return null;
	}

	public static Boolean validate(Long orgid, Long specificid, Double taxRate, Double cessRate) {
		Boolean flag = true;
		SpecificTaxHistory specHistory = findLastUpdatedTaxRate(orgid, specificid);
		if (specHistory != null) {
			if (specHistory.getTaxRate() != null && taxRate != null) {
				if (specHistory.getTaxRate().equals(taxRate)) {
					flag = false;
				}
			}
			if (specHistory.getCessRate() != null && cessRate != null) {
				if (specHistory.getCessRate().equals(cessRate)) {
					flag = false;
				}
			}
		}

		return flag;
	}

	public static List<SpecificTaxHistory> findSpecificHistory(EntityManager entityManager, Long orgid,
			Long specificid) {
		Query query = entityManager.createQuery(SPECIFIC_HISTORY);
		query.setParameter(1, orgid);
		query.setParameter(2, specificid);
		List<SpecificTaxHistory> list = (List<SpecificTaxHistory>) query.getResultList();
		return list;
	}
}
