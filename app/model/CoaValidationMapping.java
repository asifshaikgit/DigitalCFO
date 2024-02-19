package model;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Query;
import javax.persistence.Table;
import java.util.List;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil Namdev on 30-04-2016.
 */
@Entity
@Table(name = "COA_VALIDATION_MAPPING")
public class CoaValidationMapping extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public CoaValidationMapping(){
        entityManager = EntityManagerProvider.getEntityManager();
    }

    private static String sbquery = "select obj from CoaValidationMapping obj where upper(obj.mappingName)=?1 and obj.presentStatus=1";

    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    @Column(name = "MAPPING_NAME")
    private String mappingName;

    public static CoaValidationMapping findByName(EntityManager entityManager, String name) {
        CoaValidationMapping mapping = null;
        Query query = entityManager.createQuery(sbquery);
        query.setParameter(1, name);
        List<CoaValidationMapping> coaValidationMapping = query.getResultList();
        if (!coaValidationMapping.isEmpty() && coaValidationMapping.size() > 0) {
            mapping = coaValidationMapping.get(0);
        }
        return mapping;
    }
}
