/**
 *  @author Mritunjay
 */
package com.idos.dao;

import java.math.BigInteger;
import java.util.*;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;

import java.util.logging.Logger;
import java.util.logging.Level;

import model.BaseModel;
import model.BusinessAction;
import model.Users;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

import javax.transaction.Transactional;

@SuppressWarnings({ "unchecked" })
public class GenericJpaDAO implements GenericDAO {

	/*
	 * public static JPAApi jpaApi;
	 * 
	 * public GenericJpaDAO(JPAApi jpaApi){
	 * this.jpaApi = jpaApi;
	 * EntityManager entityManager = EntityManagerProvider.getEntityManager();
	 * }
	 */

	protected static Logger log = Logger.getLogger("dao");

	// method to get entity by passing particular id of a particular entity
	public <T extends BaseModel> T getById(Class<T> clazz, Long id, EntityManager entityManager) {
		log.log(Level.INFO, "genericDao getById{} returns entity by the primary key");
		return entityManager.find(clazz, id);
	}

	// method to execute simple hibernate query
	public <T extends BaseModel> List<T> executeSimpleQuery(String query, EntityManager entityManager) {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "******* Start " + query);
		}
		System.out.println(entityManager);
		javax.persistence.Query q = entityManager.createQuery(query);

		List<T> list = q.getResultList();
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "******* End ");
		}
		return list;
	}

	public <T extends BaseModel> List<T> executeQuery(Query query, EntityManager entityManager) {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "******* Start " + query);
		}
		List<T> list = query.getResultList();
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "******* End ");
		}
		return list;
	}

	@Override
	public <T extends BaseModel> List<T> executeSimpleQueryWithLimit(String query, EntityManager entityManager,
			int limit) {
		log.log(Level.INFO,
				"genericDao executeSimpleQueryWithLimit{} returns list of entity by executing plain hql query on entity and limit the size");
		log.log(Level.INFO, query.toString());
		javax.persistence.Query q = entityManager.createQuery(query).setMaxResults(limit);
		return q.getResultList();
	}

	/*
	 * genericDao executeSimpleQueryWithLimit{} returns list of entity by executing
	 * plain hql query on entity and limit the size
	 * (non-Javadoc)
	 * 
	 * @see com.idos.dao.GenericDAO#executeSimpleQueryWithLimit(java.lang.String,
	 * javax.persistence.EntityManager, int, int)
	 */
	@Override
	public <T extends BaseModel> List<T> executeSimpleQueryWithLimit(String queryStr, EntityManager entityManager,
			int fromRec, int maxRecord, ArrayList paramList) {
		Query query = entityManager.createQuery(queryStr);
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "Info " + paramList);
			log.log(Level.INFO, "Info " + queryStr);
		}
		if (paramList != null) {
			Iterator iterator = paramList.iterator();
			int count = 1;
			while (iterator.hasNext()) {
				query.setParameter(count++, iterator.next());
			}
		}
		query.setFirstResult(fromRec);
		query.setMaxResults(maxRecord);
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "******* End " + query.getResultList().size());
		}
		return query.getResultList();
	}

	// method to save an entity in database
	public <T extends BaseModel> void save(T entity, Users usr, EntityManager entityManager) {
		log.log(Level.INFO, "genericDao save{} performs a save operation on the entity.");
		if (entity.getId() == null) {
			log.log(Level.INFO, "save on entity new object" + entity);
			entity.setCreatedAt(Calendar.getInstance().getTime());
			if (usr != null) {
				log.log(Level.INFO, "save on entity new object when user is not null" + entity);
				entity.setCreatedBy(usr);
			} else {
				log.log(Level.INFO, "save on entity new object when user is null" + entity);
				entity.setCreatedBy(null);
			}
			entityManager.persist(entity);
		}
	}

	// method to save or update an entity in database
	public <T extends BaseModel> void saveOrUpdate(T entity, Users usr, EntityManager entityManager) {
		// log.log(Level.INFO, "genericDao saveOrUpdate{} performs a save operation on
		// the entity if primary key of entity does not exist.");
		// log.log(Level.INFO, "if primary key exists then perform update on the
		// entity.");
		// log.log(Level.INFO, "******* Start ");
		try {
			if (entity.getId() == null) {
				entity.setCreatedAt(Calendar.getInstance().getTime());
				if (usr != null) {
					entity.setCreatedBy(usr);
				} else {
					entity.setCreatedBy(null);
				}
				entityManager.persist(entity);
			} else {
				entity.setModifiedAt(Calendar.getInstance().getTime());
				if (usr != null) {
					entity.setModifiedBy(usr);
				} else {
					entity.setModifiedBy(null);
				}
				entityManager.merge(entity);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error while saving data."); // changed by Sunil Namdev
			log.log(Level.SEVERE, "failed", ex);
			// log.log(Level.SEVERE, "Error", ex);
		}
		// log.log(Level.INFO, "******* End ");
	}

	// method to delete an entity object from database
	public <T extends BaseModel> void deleteById(Class<T> clazz, Long id, EntityManager entityManager) {
		log.log(Level.INFO, "genericDao deleteById{} delete entity records by primary key.");
		log.log(Level.INFO, "delete on" + clazz.getSimpleName());
		entityManager.remove(getById(clazz, id, entityManager));
	}

	@Override
	public <T extends BaseModel> boolean isUnique(T entity, Map<String, Object> param, EntityManager entityManager) {
		log.log(Level.INFO, "genericDao isUnique{} check on the entire object of entity for uniqueness.");
		boolean unique = true;
		for (Entry<String, Object> entityMap : param.entrySet()) {
			String key = entityMap.getKey();
			String value = entityMap.getValue().toString();
			List<T> resultentity = entityManager
					.createQuery(
							"from " + entity.getClass().getSimpleName() + " obj WHERE obj." + key + "='" + value + "'")
					.getResultList();
			log.log(Level.INFO,
					entityManager.createQuery(
							"from " + entity.getClass().getSimpleName() + " obj WHERE obj." + key + "='" + value + "'")
							.toString());
			if (resultentity.size() > 0) {
				unique = false;
			}
		}
		return unique;
	}

	@Override
	public <T extends BaseModel> T getByCriteria(Class<T> clazz, Map criterias, EntityManager entityManager) {
		if (log.isLoggable(Level.INFO))
			log.log(Level.INFO, "Start");
		List<T> items = findByCriteria(clazz, criterias, null, false, entityManager);
		if (items != null && items.size() > 0) {
			if (log.isLoggable(Level.INFO))
				log.log(Level.INFO, "End");
			return items.get(0);
		}
		return null;
	}

	@Override
	public <T extends BaseModel> List<T> findByCriteria(Class<T> clazz,
			Map criterias, EntityManager entityManager) {
		return findByCriteria(clazz, criterias, null, false, entityManager);
	}

	@Override
	public <T extends BaseModel> List<T> findByCriteria(Class<T> clazz, Map criterias, String orderField, boolean desc,
			EntityManager entityManager) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "************** Start " + criterias);
		// Build the Query String with Search Criteria
		StringBuilder query = new StringBuilder("Select obj from ").append(clazz.getSimpleName()).append(" obj");
		if (criterias != null && criterias.size() > 0) {
			Object[] keyArray = criterias.keySet().toArray();
			for (int i = 0; i < keyArray.length; i++) {
				if (i == 0) {
					query.append(" where");
				}
				// criterias.get(keyArray[i])
				query.append(" obj.").append(keyArray[i]).append("=:").append("p" + i);
				if (i != (keyArray.length - 1)) {
					query.append(" and");
				}
			}
		}
		if (orderField != null) {
			query.append("  ORDER BY obj.").append(orderField);
			if (!desc)
				query.append(" asc");
			else
				query.append(" desc");

		}
		// Build the query Object
		javax.persistence.Query jpaQuery = entityManager.createQuery(query.toString());
		log.log(Level.INFO, query.toString());
		// Set the search Parameters for the jpaQuery
		if (criterias != null && criterias.size() > 0) {
			Object[] keyArray = criterias.keySet().toArray();
			for (int i = 0; i < keyArray.length; i++)
				jpaQuery.setParameter("p" + i, criterias.get(keyArray[i]));
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "************** End ");
		return (List<T>) jpaQuery.getResultList();
	}

	@Override
	public <T extends BaseModel> List<T> findAll(Class<T> clazz, boolean activeOnly, boolean desc,
			EntityManager entityManager) {
		// TODO Auto-generated method stub
		StringBuilder query = new StringBuilder("Select obj from ").append(
				clazz.getSimpleName()).append(" obj");
		if (activeOnly) {
			query.append(" where obj.presentStatus=1");
		}
		if (!desc) {
			query.append(" ORDER BY obj.id asc");
		}
		if (desc) {
			query.append(" ORDER BY obj.id desc");
		}
		Query jpaQuery = entityManager.createQuery(query.toString());
		log.log(Level.INFO, query.toString());
		return (List<T>) jpaQuery.getResultList();
	}

	@Override
	public <T extends BaseModel> int deleteByCriteria(Class<T> clazz, Map criterias, EntityManager entityManager) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************** Start " + criterias);
		}
		StringBuilder query = new StringBuilder("Delete from ").append(
				clazz.getSimpleName()).append(" obj");
		if (criterias != null && criterias.size() > 0) {
			Object[] keyArray = criterias.keySet().toArray();
			for (int i = 0; i < keyArray.length; i++) {
				if (i == 0) {
					query.append(" where");
				}
				query.append(" obj.").append(keyArray[i]).append("=:").append("p" + i);
				if (i != (keyArray.length - 1)) {
					query.append(" and");
				}
			}
		}
		// Build the query Object
		Query jpaQuery = entityManager.createQuery(query.toString());
		log.log(Level.INFO, query.toString());
		// Set the search Parameters for the jpaQuery
		if (criterias != null && criterias.size() > 0) {
			Object[] keyArray = criterias.keySet().toArray();
			for (int i = 0; i < keyArray.length; i++)
				jpaQuery.setParameter("p" + i, criterias.get(keyArray[i]));
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************** End ");
		}
		return jpaQuery.executeUpdate();
	}

	// method to execute simple hibernate query
	@Override
	public <T extends BaseModel> Long executeCountQuery(String query, EntityManager entityManager,
			ArrayList paramList) {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, paramList.toString());
			log.log(Level.INFO, query.toString());
		}
		Query q = entityManager.createQuery(query);
		if (paramList != null) {
			Iterator iterator = paramList.iterator();
			int count = 1;
			while (iterator.hasNext()) {
				q.setParameter(count++, iterator.next());
			}
		}
		Long cnt = (Long) q.getSingleResult();
		if (log.isLoggable(Level.INFO))
			log.log(Level.INFO, "******* End " + cnt);
		return cnt;

	}

	@Override
	public <T extends BaseModel> List<T> queryWithParams(String query, EntityManager entityManager,
			ArrayList paramList) {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, paramList.toString());
			log.log(Level.INFO, query.toString());
		}
		Query q = entityManager.createQuery(query);
		if (paramList != null) {
			Iterator iterator = paramList.iterator();
			int count = 1;
			while (iterator.hasNext()) {
				q.setParameter(count++, iterator.next());
			}
		}
		if (log.isLoggable(Level.INFO))
			log.log(Level.INFO, "******* End " + q.getResultList().size());
		System.out.println("result " + q.getResultList().size());
		return q.getResultList();
	}

	@Override
	public <T extends BaseModel> List<T> queryWithNamedParams(String query, EntityManager entityManager,
			Map<String, Object> paramMap) {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, paramMap.toString());
			log.log(Level.INFO, query.toString());
		}
		try {
			Query q = entityManager.createQuery(query);
			if (paramMap != null) {
				for (String param : paramMap.keySet()) {
					q.setParameter(param, paramMap.get(param));
				}
			}
			if (log.isLoggable(Level.INFO))
				log.log(Level.INFO, "******* End " + q.getResultList().size());
			return q.getResultList();
		} catch (RuntimeException e) {
			System.err.println(query);
			e.printStackTrace(System.err);
			throw e;
		}
	}

	@Override
	public BigInteger executeCountNativeQuery(String query, EntityManager entityManager, ArrayList paramList) {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, paramList.toString());
			log.log(Level.INFO, query.toString());
		}
		Query q = entityManager.createNativeQuery(query);
		if (paramList != null) {
			Iterator iterator = paramList.iterator();
			int count = 1;
			while (iterator.hasNext()) {
				q.setParameter(count++, iterator.next());
			}
		}
		BigInteger cnt = (BigInteger) q.getSingleResult();
		if (log.isLoggable(Level.INFO))
			log.log(Level.INFO, "******* End " + cnt);
		return cnt;

	}

	@Override
	public List<Object[]> executeNativeQueryWithParam(String queryStr, EntityManager entityManager,
			ArrayList paramList) {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, paramList.toString());
			log.log(Level.INFO, queryStr.toString());
		}
		Query query = entityManager.createNativeQuery(queryStr);
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, paramList.toString());
			log.log(Level.INFO, queryStr.toString());
		}
		if (paramList != null) {
			Iterator iterator = paramList.iterator();
			int count = 1;
			while (iterator.hasNext()) {
				query.setParameter(count++, iterator.next());
			}
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "******* End " + query.getResultList().size());
		}
		System.out.println(query);
		return query.getResultList();
	}

	@Override
	public List<Object[]> executeNativeQueryWithParamName(String queryStr, EntityManager entityManager,
			ArrayList paramList) {
		Query query = entityManager.createNativeQuery(queryStr);
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, paramList.toString());
			log.log(Level.INFO, queryStr.toString());
		}
		if (paramList != null && paramList.size() > 0) {
			Iterator iterator = paramList.iterator();
			int count = 1;
			while (iterator.hasNext()) {
				query.setParameter(count++, iterator.next());
			}
		}
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, "******* End " + query.getResultList().size());
		}
		return query.getResultList();
	}

	@Override
	public <T extends BaseModel> List<T> queryWithParamsName(String query, EntityManager entityManager,
			ArrayList paramList) {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, paramList.toString());
			log.log(Level.INFO, query.toString());
		}
		Query q = entityManager.createQuery(query);
		if (paramList != null) {
			Iterator iterator = paramList.iterator();
			int count = 1;
			while (iterator.hasNext()) {
				q.setParameter(count++, iterator.next());
			}
		}
		if (log.isLoggable(Level.INFO))
			log.log(Level.INFO, "******* End " + q.getResultList().size());
		return q.getResultList();
	}

	@Override
	public List<Object[]> queryWithParamsNameGeneric(String query, EntityManager entityManager, ArrayList paramList) {
		if (log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, paramList.toString());
			log.log(Level.INFO, query.toString());
		}
		Query q = entityManager.createQuery(query);
		if (paramList != null) {
			Iterator iterator = paramList.iterator();
			int count = 1;
			while (iterator.hasNext()) {
				q.setParameter(count++, iterator.next());
			}
		}
		if (log.isLoggable(Level.INFO))
			log.log(Level.INFO, "******* End ");
		return q.getResultList();
	}

	@Override
	public <T extends BaseModel> int deleteByParamName(String query, EntityManager entityManager, ArrayList paramList) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************** Start " + paramList);
		}

		// Build the query Object
		Query q = entityManager.createQuery(query);
		if (paramList != null) {
			Iterator iterator = paramList.iterator();
			int count = 1;
			while (iterator.hasNext()) {
				q.setParameter(count++, iterator.next());
			}
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************** End ");
		}
		return q.executeUpdate();
	}
}
