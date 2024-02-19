package model;

import java.util.Comparator;
import java.util.List;

import javax.persistence.*;

import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;

@Entity
@Table(name = "withholding_type_details")
public class WithholdingTypeDetails extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public WithholdingTypeDetails() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "name")
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static String getStringWHType(EntityManager entityManager, Integer id) {
		Long lId = Long.parseLong(id.toString());
		String type = "";

		Query query = entityManager
				.createQuery("select name from WithholdingTypeDetails where id = ?1 and presentStatus=1");
		query.setParameter(1, lId);
		type = (String) query.getSingleResult();
		return type;

	}

	public static WithholdingTypeDetails findById(EntityManager entityManager, Long id) {
		return entityManager.find(WithholdingTypeDetails.class, id);
	}
}
