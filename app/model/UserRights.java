package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;

@Entity
@Table(name = "USER_RIGHTS")
public class UserRights extends AbstractBaseModel {
  private static JPAApi jpaApi;
  private static EntityManager entityManager;

  public UserRights() {
    entityManager = EntityManagerProvider.getEntityManager();
  }

  @Column(name = "right_name")
  private String rightName;

  /**
   * Find UserRights by id.
   */
  public static UserRights findById(Long id) {
    return entityManager.find(UserRights.class, id);
  }

  public static List<UserRights> list(EntityManager entityManager) {
    List<UserRights> userRights = entityManager
        .createQuery("select obj from UserRights obj where obj.presentStatus = 1;").getResultList();
    return userRights;
  }
}
