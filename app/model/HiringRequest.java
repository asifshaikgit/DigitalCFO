package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "HIRING_EMPLOYEE1")
public class HiringRequest extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public HiringRequest() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "data")
	private String data;

	@Column(name = "Emp_id")
	private long empId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "REMARKS")
	private String remarks;

	@Column(name = "DOCUMENTS")
	private String documents;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PROJECT_LABOUR_POSITION_ID")
	private ProjectLabourPosition projLabPosition;

	public ProjectLabourPosition getProjLabPosition() {
		return projLabPosition;
	}

	public void setProjLabPosition(ProjectLabourPosition projLabPosition) {
		this.projLabPosition = projLabPosition;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public long getEmpId() {
		return empId;
	}

	public void setEmpId(long empId) {
		this.empId = empId;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getDocuments() {
		return documents;
	}

	public void setDocuments(String documents) {
		this.documents = documents;
	}

	/**
	 * Find a HiringRequest by id.
	 */
	public static HiringRequest findById(Long id) {
		return entityManager.find(HiringRequest.class, id);
	}
}
