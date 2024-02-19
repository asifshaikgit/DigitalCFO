package model;

import java.util.Date;
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
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "PROJECT_LABOUR_POSITION")
public class ProjectLabourPosition extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public ProjectLabourPosition() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "position_name")
	private String positionName;

	@Column(name = "position_validity")
	private Date positionValidity;

	@Column(name = "position_validity_to")
	private Date positionValidityTo;

	public Date getPositionValidityTo() {
		return positionValidityTo;
	}

	public void setPositionValidityTo(Date positionValidityTo) {
		this.positionValidityTo = positionValidityTo;
	}

	@Column(name = "exp_required")
	private String expRequired;

	public String getExpRequired() {
		return expRequired;
	}

	public void setExpRequired(String expRequired) {
		this.expRequired = IdosUtil.escapeHtml(expRequired);
	}

	public Date getPositionValidity() {
		return positionValidity;
	}

	public void setPositionValidity(Date positionValidity) {
		this.positionValidity = positionValidity;
	}

	@Column(name = "location")
	private String location;

	@Column(name = "languages")
	private String languages;

	@Column(name = "proficiency")
	private String proficiency;

	@Column(name = "job_description")
	private String jobDescription;

	@Column(name = "place_of_advertisement")
	private String placeOfAdvertisement;

	@Column(name = "budget")
	private Double budget;

	@Column(name = "currency")
	private String currency;

	@Column(name = "amount")
	private Double amount;

	@Column(name = "agreement_template_doc")
	private String agreementTemlateDoc;

	public String getAgreementTemlateDoc() {
		return agreementTemlateDoc;
	}

	public void setAgreementTemlateDoc(String agreementTemlateDoc) {
		this.agreementTemlateDoc = agreementTemlateDoc;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id")
	private Project project;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@Column(name = "requires_approval")
	private Integer requiresApproval;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "labourPosition")
	private List<ProjectLabourPositionQualification> pjctLabourpositionQualification;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "labourPosition")
	private List<ProjectLabourPositionLanguageProficiency> pjctLabourpositionLangProf;

	public List<ProjectLabourPositionLanguageProficiency> getPjctLabourpositionLangProf() {
		return pjctLabourpositionLangProf;
	}

	public void setPjctLabourpositionLangProf(
			List<ProjectLabourPositionLanguageProficiency> pjctLabourpositionLangProf) {
		this.pjctLabourpositionLangProf = pjctLabourpositionLangProf;
	}

	public List<ProjectLabourPositionQualification> getPjctLabourpositionQualification() {
		return pjctLabourpositionQualification;
	}

	public void setPjctLabourpositionQualification(
			List<ProjectLabourPositionQualification> pjctLabourpositionQualification) {
		this.pjctLabourpositionQualification = pjctLabourpositionQualification;
	}

	public Integer getRequiresApproval() {
		return requiresApproval;
	}

	public void setRequiresApproval(Integer requiresApproval) {
		this.requiresApproval = requiresApproval;
	}

	public String getPositionName() {
		return positionName;
	}

	public void setPositionName(String positionName) {
		this.positionName = IdosUtil.escapeHtml(positionName);
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = IdosUtil.escapeHtml(location);
	}

	public String getLanguages() {
		return languages;
	}

	public void setLanguages(String languages) {
		this.languages = IdosUtil.escapeHtml(languages);
	}

	public String getProficiency() {
		return proficiency;
	}

	public void setProficiency(String proficiency) {
		this.proficiency = IdosUtil.escapeHtml(proficiency);
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = IdosUtil.escapeHtml(jobDescription);
	}

	public String getPlaceOfAdvertisement() {
		return placeOfAdvertisement;
	}

	public void setPlaceOfAdvertisement(String placeOfAdvertisement) {
		this.placeOfAdvertisement = IdosUtil.escapeHtml(placeOfAdvertisement);
	}

	public Double getBudget() {
		return budget;
	}

	public void setBudget(Double budget) {
		this.budget = budget;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = IdosUtil.escapeHtml(currency);
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	/**
	 * Find a ProjectLabourPosition by id.
	 */
	public static ProjectLabourPosition findById(Long id) {
		return entityManager.find(ProjectLabourPosition.class, id);
	}
}
