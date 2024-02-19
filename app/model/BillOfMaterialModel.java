package model;

//import com.fasterxml.jackson.databind.map.Serializers;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import javax.persistence.*;
import java.util.List;

/**
 * @author Sunil K. Namdev created on 29.01.2019
 */
@Entity
@Table(name = "BILL_OF_MATERIAL")
public class BillOfMaterialModel extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public BillOfMaterialModel() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORGANIZATION_ID")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BRANCH_ID")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROJECT_ID")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INCOME_ID")
    private Specifics income;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "billOfMaterial")
    private List<BillOfMaterialItemModel> billOfMaterialItems;

    public Organization getOrganization() {
        return this.organization;
    }

    public void setOrganization(final Organization organization) {
        this.organization = organization;
    }

    public Branch getBranch() {
        return this.branch;
    }

    public void setBranch(final Branch branch) {
        this.branch = branch;
    }

    public Project getProject() {
        return this.project;
    }

    public void setProject(final Project project) {
        this.project = project;
    }

    public Specifics getIncome() {
        return this.income;
    }

    public void setIncome(final Specifics income) {
        this.income = income;
    }

    public List<BillOfMaterialItemModel> getBillOfMaterialItems() {
        return this.billOfMaterialItems;
    }

    public void setBillOfMaterialItems(final List<BillOfMaterialItemModel> billOfMaterialItems) {
        this.billOfMaterialItems = billOfMaterialItems;
    }

    public static BillOfMaterialModel findById(Long id) {
        return entityManager.find(BillOfMaterialModel.class, id);
    }
}
