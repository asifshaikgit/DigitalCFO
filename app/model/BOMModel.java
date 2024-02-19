package model;

//import com.fasterxml.jackson.databind.map.Serializers;
import play.db.jpa.JPAApi;
import service.EntityManagerProvider;

import javax.persistence.*;
import java.util.List;

/**
 * @author Harish Kumar created on 25.04.2023
 */
@Entity
@Table(name="BOM")
public class BOMModel extends AbstractBaseModel {

    private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BOMModel() {
		entityManager = EntityManagerProvider.getEntityManager();
	}
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="ORGANIZATION_ID")
    private Organization organization;

    @Column(name="BOM_NAME")
    private String bomName;

    @OneToMany(fetch=FetchType.LAZY, mappedBy="bom")
    private List<BOMItemModel> bomItems;

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }

    public String getBomName() {
        return this.bomName;
    }

    public void setBomName(String bomName) {
        this.bomName = bomName;
    }

    public List<BOMItemModel> getBomItems() {
        return this.bomItems;
    }

    public void setBomItems(final List<BOMItemModel> bomItems) {
        this.bomItems = bomItems;
    }

    public static BOMModel findById(Long id) {
        return entityManager.find(BOMModel.class, id);
    }
}

