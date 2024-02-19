package model;

//import com.fasterxml.jackson.databind.map.Serializers;
import play.db.jpa.JPAApi;
import service.EntityManagerProvider;

import javax.persistence.*;

/**
 * @author Harish Kumar created on 25.04.2023
 */
@Entity
@Table(name="BOM_ITEMS")
public class BOMItemModel extends AbstractBaseModel {
    private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BOMItemModel() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="BOM_ID")
    private BOMModel bom;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="EXPENSE_ID")
    private Specifics expense;

    @Column(name="NO_OF_UNITS")
    private Double noOfUnits;

    public BOMModel getBOM() {
        return this.bom;
    }

    public void setBOM(final BOMModel bom) {
        this.bom = bom;
    }

    public Specifics getExpense() {
        return this.expense;
    }

    public void setExpense(final Specifics expense) {
        this.expense = expense;
    }

    public Double getNoOfUnits() {
        return this.noOfUnits;
    }

    public void setNoOfUnits(final Double noOfUnits) {
        this.noOfUnits = noOfUnits;
    }

    public static BOMItemModel findById(Long id) {
        return entityManager.find(BOMItemModel.class, id);
    }
}
