package service;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import javax.inject.Inject;
import com.idos.dao.PwcAuthUsersDAO;
import model.PwcAuthUsers;
import play.db.jpa.JPAApi;

public class PwcAuthUsersDAOImpl implements PwcAuthUsersDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Inject
    public PwcAuthUsersDAOImpl(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
        entityManager = EntityManagerProvider.getEntityManager();
    }

    public void save(PwcAuthUsers user) {
        entityManager.persist(user);
    }

    public void update(PwcAuthUsers user) {
        entityManager.merge(user);
    }

    public void delete(PwcAuthUsers user) {
        entityManager.remove(user);
    }

    public PwcAuthUsers findById(int id) {
        return entityManager.find(PwcAuthUsers.class, id);
    }

    public List<PwcAuthUsers> findAll() {
        Query query = entityManager.createQuery("select obj from pwc_auth_users obj order by obj.id asc");
        return query.getResultList();
    }
}
