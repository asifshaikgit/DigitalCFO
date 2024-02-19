package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.*;
import java.util.List;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "IDOS_FILE_UPLOAD_LOGS")
public class IdosUploadFilesLogs extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public IdosUploadFilesLogs() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String BY_URL = "select obj from IdosUploadFilesLogs obj where obj.organization.id= ?1 and obj.fileUrl= ?2";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ID")
	private Branch branch;

	@Column(name = "REFERENCE_ID")
	private Long referenceId;

	@Column(name = "UPLOAD_MODULE")
	private String uploadModule;

	@Column(name = "UPLOAD_MODULE_ELEMENT")
	private String uploadModuleElement;

	@Column(name = "DESTINATION")
	private Integer destination;

	@Column(name = "FILE_NAME")
	private String fileName;

	@Column(name = "FILE_URL")
	private String fileUrl;

	@Column(name = "FILE_SIZE")
	private Double fileSize;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Double getFileSize() {
		return fileSize;
	}

	public void setFileSize(Double fileSize) {
		this.fileSize = fileSize;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Long getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(Long referenceId) {
		this.referenceId = referenceId;
	}

	public String getUploadModule() {
		return uploadModule;
	}

	public void setUploadModule(String uploadModule) {
		this.uploadModule = uploadModule;
	}

	public String getUploadModuleElement() {
		return uploadModuleElement;
	}

	public void setUploadModuleElement(String uploadModuleElement) {
		this.uploadModuleElement = uploadModuleElement;
	}

	public Integer getDestination() {
		return destination;
	}

	public void setDestination(Integer destination) {
		this.destination = destination;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public static List<IdosUploadFilesLogs> findByFileUrl(EntityManager entityManager, long orgid, String fileUrl) {
		List<IdosUploadFilesLogs> list = null;
		Query query = entityManager.createQuery(BY_URL);
		query.setParameter("p1", orgid);
		query.setParameter("p2", fileUrl);
		list = query.getResultList();
		return list;
	}
}
