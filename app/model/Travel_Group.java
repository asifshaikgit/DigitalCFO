package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.idos.util.IdosUtil;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "TRAVEL_GROUP")
public class Travel_Group extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Travel_Group() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String TRL_GRP_HQL = "select obj from Travel_Group obj WHERE obj.organization.id = ?1 and obj.presentStatus=1";

	@Column(name = "TRAVEL_GROUP_NAME")
	private String travelGroupName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "travelgroup")
	private List<TravelGroupPermittedBoardingLodging> travelGroupPermittedBoardingLodging;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "travelgroup")
	private List<TravelGroupKnowledgeLibrary> travelGroupkL;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "travelgroup")
	private List<TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses> dailyLimitOtherOfficialPurposeExpenses;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "travelgroup")
	private List<TravelGroupDistanceMilesKmsAllowedTravelMode> distanceMilesKmsAllowedTravelModes;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "travelgroup")
	private List<TravelGroupFixedDailyPerDIAM> fixedDailyPerDIAM;

	public List<TravelGroupPermittedBoardingLodging> getTravelGroupPermittedBoardingLodging() {
		return travelGroupPermittedBoardingLodging;
	}

	public void setTravelGroupPermittedBoardingLodging(
			List<TravelGroupPermittedBoardingLodging> travelGroupPermittedBoardingLodging) {
		this.travelGroupPermittedBoardingLodging = travelGroupPermittedBoardingLodging;
	}

	public String getTravelGroupName() {
		return travelGroupName;
	}

	public void setTravelGroupName(String travelGroupName) {
		this.travelGroupName = IdosUtil.escapeHtml(travelGroupName);
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public List<TravelGroupKnowledgeLibrary> getTravelGroupkL() {
		return travelGroupkL;
	}

	public void setTravelGroupkL(List<TravelGroupKnowledgeLibrary> travelGroupkL) {
		this.travelGroupkL = travelGroupkL;
	}

	public List<TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses> getDailyLimitOtherOfficialPurposeExpenses() {
		return dailyLimitOtherOfficialPurposeExpenses;
	}

	public void setDailyLimitOtherOfficialPurposeExpenses(
			List<TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses> dailyLimitOtherOfficialPurposeExpenses) {
		this.dailyLimitOtherOfficialPurposeExpenses = dailyLimitOtherOfficialPurposeExpenses;
	}

	public List<TravelGroupDistanceMilesKmsAllowedTravelMode> getDistanceMilesKmsAllowedTravelModes() {
		return distanceMilesKmsAllowedTravelModes;
	}

	public void setDistanceMilesKmsAllowedTravelModes(
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> distanceMilesKmsAllowedTravelModes) {
		this.distanceMilesKmsAllowedTravelModes = distanceMilesKmsAllowedTravelModes;
	}

	public List<TravelGroupFixedDailyPerDIAM> getFixedDailyPerDIAM() {
		return fixedDailyPerDIAM;
	}

	public void setFixedDailyPerDIAM(
			List<TravelGroupFixedDailyPerDIAM> fixedDailyPerDIAM) {
		this.fixedDailyPerDIAM = fixedDailyPerDIAM;
	}

	/**
	 * Find a Travel_Group by id.
	 */
	public static Travel_Group findById(Long id) {
		return entityManager.find(Travel_Group.class, id);
	}

	public static List<Travel_Group> getTravelGroupList(EntityManager entityManager, Long orgId) throws Exception {
		List<Travel_Group> travelGroupList = null;
		Query query = entityManager.createQuery(TRL_GRP_HQL);
		query.setParameter(1, orgId);
		travelGroupList = query.getResultList();
		return travelGroupList;
	}
}
