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
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "ORGANIZATION_GSTIN_SERIALS")
public class OrganizationGstinSerials extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public OrganizationGstinSerials() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final long serialVersionUID = 8347137673258336280L;

	private static final String ORG_HQL = "select obj from OrganizationGstinSerials obj where obj.organization.id=?1 and obj.presentStatus=1";

	private static final String ORG_CAT_GST_HQL = "select obj from OrganizationGstinSerials obj where obj.organization.id=?1 and obj.documentCategoryNo=?2 and obj.gstIn = ?3 and obj.presentStatus=1";

	@Column(name = "GSTIN")
	private String gstIn;

	@Column(name = "SERIAL_NO")
	private Integer serialNo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@Column(name = "DOC_CAT_NO")
	private Integer documentCategoryNo;

	public String getGstIn() {
		return gstIn;
	}

	public void setGstIn(String gstIn) {
		this.gstIn = gstIn;
	}

	public Integer getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(Integer serialNo) {
		this.serialNo = serialNo;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Integer getDocumentCategoryNo() {
		return documentCategoryNo;
	}

	public void setDocumentCategoryNo(Integer documentCategoryNo) {
		this.documentCategoryNo = documentCategoryNo;
	}

	public static List<OrganizationGstinSerials> getByOrganization(EntityManager entityManager, Long orgId) {
		List<OrganizationGstinSerials> list = null;
		Query query = entityManager.createQuery(ORG_HQL);
		query.setParameter(1, orgId);
		list = query.getResultList();
		return list;
	}

	public static OrganizationGstinSerials getByOrgAndDocCategory(EntityManager entityManager, Long orgId,
			Integer catId, String gstIn) {
		List<OrganizationGstinSerials> list = null;
		Query query = entityManager.createQuery(ORG_CAT_GST_HQL);
		query.setParameter(1, orgId);
		query.setParameter(2, catId);
		query.setParameter(3, gstIn);
		list = query.getResultList();
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

}
