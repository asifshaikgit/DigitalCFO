package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil Namdev on 30-04-2016.
 */
@Entity
@Table(name = "COA_VALIDATION_PL_BS")
public class CoaValidationPLBS extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public CoaValidationPLBS(){
        entityManager = EntityManagerProvider.getEntityManager();
    }

    public String getValidationName() {
        return validationName;
    }

    public void setValidationName(String validationName) {
        this.validationName = validationName;
    }

    @Column(name = "VALIDATION_NAME")
    private String validationName;

    /**
     * Find a CoaValidationPLBS by name.
     */
    public static CoaValidationPLBS findByName(EntityManager entityManager, String name) {
        CoaValidationPLBS mapping = null;
        StringBuilder sbquery = new StringBuilder(
                "select obj from CoaValidationPLBS obj where upper(obj.validationName) ='");
        sbquery.append(name.toUpperCase()).append("' and obj.presentStatus=1");
        List<CoaValidationPLBS> coaValidationPLBS = entityManager.createQuery(sbquery.toString()).getResultList();
        if (!coaValidationPLBS.isEmpty() && coaValidationPLBS.size() > 0) {
            mapping = coaValidationPLBS.get(0);
        }
        return mapping;
    }
}
