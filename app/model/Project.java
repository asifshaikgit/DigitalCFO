package model;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import com.idos.util.IdosUtil;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "PROJECT")
public class Project extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Project() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String ORG_PROJ_EQUL_HQL = "select obj from Project obj WHERE obj.organization.id=?1 and upper(obj.name) = ?2 and obj.presentStatus=1";
	private static final String CHK_PROJ_DUB_HQL = "select obj from Project obj WHERE  obj.organization.id=?1 and (upper(obj.name) = ?2 or obj.number = ?3) and obj.presentStatus=1";
	@Column(name = "name")
	private String name;

	@Column(name = "number")
	private String number;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "location")
	private String location;

	@Column(name = "country")
	private Integer country;

	public Integer getCountry() {
		return country;
	}

	public void setCountry(Integer country) {
		this.country = country;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project")
	private List<ProjectBranches> projectBranch;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "projectBranch")
	private List<ProjectBranches> branchProject;

	@Column(name = "project_director_name")
	private String projectDirectorName;

	@Column(name = "director_phone_number_country_code")
	private String dirPhnNumCtryCode;

	@Column(name = "director_phone_number")
	private String directorPhoneNumber;

	@Column(name = "project_manager_name")
	private String projectManagerName;

	@Column(name = "manager_phone_number_country_code")
	private String managerPhnNumCtryCode;

	public String getDirPhnNumCtryCode() {
		return dirPhnNumCtryCode;
	}

	public void setDirPhnNumCtryCode(String dirPhnNumCtryCode) {
		this.dirPhnNumCtryCode = dirPhnNumCtryCode;
	}

	public String getManagerPhnNumCtryCode() {
		return managerPhnNumCtryCode;
	}

	public void setManagerPhnNumCtryCode(String managerPhnNumCtryCode) {
		this.managerPhnNumCtryCode = managerPhnNumCtryCode;
	}

	@Column(name = "manager_phone_number")
	private String managerPhoneNumber;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@Column(name = "ALLOW_ACCESS_TO_RECRUITMENT_SERVICES")
	private Integer allowAccessToRecruitmentServices = 0;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = IdosUtil.escapeHtml(name);
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = IdosUtil.escapeHtml(number);
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = IdosUtil.escapeHtml(location);
	}

	public List<ProjectBranches> getProjectBranch() {
		return projectBranch;
	}

	public void setProjectBranch(List<ProjectBranches> projectBranch) {
		this.projectBranch = projectBranch;
	}

	public String getProjectDirectorName() {
		return projectDirectorName;
	}

	public void setProjectDirectorName(String projectDirectorName) {
		this.projectDirectorName = IdosUtil.escapeHtml(projectDirectorName);
	}

	public String getDirectorPhoneNumber() {
		return directorPhoneNumber;
	}

	public void setDirectorPhoneNumber(String directorPhoneNumber) {
		this.directorPhoneNumber = directorPhoneNumber;
	}

	public String getProjectManagerName() {
		return projectManagerName;
	}

	public void setProjectManagerName(String projectManagerName) {
		this.projectManagerName = IdosUtil.escapeHtml(projectManagerName);
	}

	public String getManagerPhoneNumber() {
		return managerPhoneNumber;
	}

	public void setManagerPhoneNumber(String managerPhoneNumber) {
		this.managerPhoneNumber = managerPhoneNumber;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public List<ProjectBranches> getBranchProject() {
		return branchProject;
	}

	public void setBranchProject(List<ProjectBranches> branchProject) {
		this.branchProject = branchProject;
	}

	/**
	 * Find a Project by id.
	 */
	public static Project findById(Long id) {
		return entityManager.find(Project.class, id);
	}

	public Integer getAllowAccessToRecruitmentServices() {
		return allowAccessToRecruitmentServices;
	}

	public void setAllowAccessToRecruitmentServices(
			Integer allowAccessToRecruitmentServices) {
		this.allowAccessToRecruitmentServices = allowAccessToRecruitmentServices;
	}

	public static List<Project> findListByOrgEqualName(Long orgid, String name) {
		if (name == null) {
			return null;
		}
		List<Project> list = null;
		Query query = entityManager.createQuery(ORG_PROJ_EQUL_HQL);
		query.setParameter(1, orgid);
		query.setParameter(2, name.toUpperCase());
		list = query.getResultList();
		return list;
	}

	public static Project checkDiplicateProject(EntityManager entityManager, Long orgid, String name,
			String number) {
		Project project = null;
		try {
			Query query = entityManager.createQuery(CHK_PROJ_DUB_HQL);
			query.setParameter(1, orgid);
			query.setParameter(2, name.toUpperCase());
			query.setParameter(3, number);
			List<Project> listProject = query.getResultList();
			if (listProject.size() > 0) {
				project = listProject.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			project = null;
		}
		return project;
	}
}
