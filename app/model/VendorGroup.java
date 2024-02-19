package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;

@Entity
@Table(name = "VENDOR_GROUP")
public class VendorGroup extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public VendorGroup() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String ORG_VG_NAME_LIKE_HQL = "select obj from VendorGroup obj WHERE obj.organization.id=?1 and obj.groupName like ?2 and obj.presentStatus=1";
	@Column(name = "GROUP_NAME")
	private String groupName;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getKnowledgeLibrary() {
		return knowledgeLibrary;
	}

	public void setKnowledgeLibrary(String knowledgeLibrary) {
		this.knowledgeLibrary = knowledgeLibrary;
	}

	public Integer getGroupType() {
		return groupType;
	}

	public void setGroupType(Integer groupType) {
		this.groupType = groupType;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	@Column(name = "KNOWLEDGE_LIBRARY")
	private String knowledgeLibrary;

	@Column(name = "GROUP_TYPE")
	private Integer groupType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	/**
	 * Find a vendorgroup by id.
	 */
	public static VendorGroup findById(Long id) {
		return entityManager.find(VendorGroup.class, id);
	}

	public static List<VendorGroup> findListByOrgIdAndName(EntityManager entityManager, Long orgid, String name) {
		List<VendorGroup> customerList = null;
		Query query = entityManager.createQuery(ORG_VG_NAME_LIKE_HQL);
		query.setParameter(1, orgid);
		query.setParameter(2, "%" + name + "%");
		customerList = query.getResultList();
		return customerList;
	}
}
