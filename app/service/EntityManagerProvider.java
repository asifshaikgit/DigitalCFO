package service;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Singleton
public class EntityManagerProvider {
    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;

    // Static initialization block
    static {
        String persistenceUnitName = "IDOS_db";

        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
        }

        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = entityManagerFactory.createEntityManager();
        }
    }

    @Inject
    private EntityManagerProvider() {
    }

    public static EntityManager getEntityManager() {
        return entityManager;
    }

    public static void close() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
