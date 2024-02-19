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
@Table(name = "SPECIFICS_KNOWLEDGE_LIBRARY")
public class SpecificsKnowledgeLibrary extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	private static final String FIND_BY_SPECIFIC_JPQL = "select obj from SpecificsKnowledgeLibrary obj WHERE obj.specifics.id = ?1 and obj.presentStatus=1";

	public SpecificsKnowledgeLibrary() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "KNOWLEDGE_LIBRARY_CONTENT")
	private String knowledgeLibraryContent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specifics_id")
	private Specifics specifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specifics_particulars_id")
	private Particulars particulars;

	@Column(name = "is_mandatory")
	private Integer isMandatory;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "specificsKl")
	List<SpecificsKnowledgeLibraryForBranch> specificsKlibrary;

	public List<SpecificsKnowledgeLibraryForBranch> getSpecificsKlibrary() {
		return specificsKlibrary;
	}

	public void setSpecificsKlibrary(
			List<SpecificsKnowledgeLibraryForBranch> specificsKlibrary) {
		this.specificsKlibrary = specificsKlibrary;
	}

	public Integer getIsMandatory() {
		return isMandatory;
	}

	public void setIsMandatory(Integer isMandatory) {
		this.isMandatory = isMandatory;
	}

	public String getKnowledgeLibraryContent() {
		return knowledgeLibraryContent;
	}

	public void setKnowledgeLibraryContent(String knowledgeLibraryContent) {
		this.knowledgeLibraryContent = IdosUtil.escapeHtml(knowledgeLibraryContent);
	}

	public Specifics getSpecifics() {
		return specifics;
	}

	public void setSpecifics(Specifics specifics) {
		this.specifics = specifics;
	}

	public Particulars getParticulars() {
		return particulars;
	}

	public void setParticulars(Particulars particulars) {
		this.particulars = particulars;
	}

	/**
	 * Find a SpecificsKnowledgeLibrary by id.
	 */
	public static SpecificsKnowledgeLibrary findById(Long id) {
		return entityManager.find(SpecificsKnowledgeLibrary.class, id);
	}

	public static List<SpecificsKnowledgeLibrary> findBySpecific(EntityManager entityManager, long specificId) {
		List<SpecificsKnowledgeLibrary> specificsKnowledgeLibraryList = null;
		Query query = entityManager.createQuery(FIND_BY_SPECIFIC_JPQL);
		query.setParameter(1, specificId);
		specificsKnowledgeLibraryList = (List<SpecificsKnowledgeLibrary>) query.getResultList();
		return specificsKnowledgeLibraryList;
	}
}
