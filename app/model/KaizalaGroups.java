package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "KAIZALA_GROUPS")
public class KaizalaGroups extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public KaizalaGroups() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "GROUP_ID")
	private String groupId;

	@Column(name = "ASSIGNED_TO_PHONE_NUMBER")
	private String assignedToPhNo;

	@Column(name = "PO_GENERATOR_PHONE_NUMBER")
	private String poGeneratorPhNo;

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getAssignedToPhNo() {
		return assignedToPhNo;
	}

	public void setAssignedToPhNo(String assignedToPhNo) {
		this.assignedToPhNo = assignedToPhNo;
	}

	public String getPoGeneratorPhNo() {
		return poGeneratorPhNo;
	}

	public void setPoGeneratorPhNo(String poGeneratorPhNo) {
		this.poGeneratorPhNo = poGeneratorPhNo;
	}

	public static KaizalaGroups findById(Long id) {
		return entityManager.find(KaizalaGroups.class, id);
	}

}
