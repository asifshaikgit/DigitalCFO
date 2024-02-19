/**
 *  @author Mritunjay
 */

package com.idos.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;

import model.BaseModel;
import model.Users;

@SuppressWarnings("unchecked")
public interface GenericDAO {

    String PARAM_NAME = "p";

    <T extends BaseModel> List<T> findAll(Class<T> clazz, boolean activeOnly, boolean desc,
            EntityManager entityManager);

    <T extends BaseModel> T getById(Class<T> clazz, Long id, EntityManager entityManager);

    <T extends BaseModel> void save(T entity, Users usr, EntityManager entityManager);

    <T extends BaseModel> void saveOrUpdate(T entity, Users usr, EntityManager entityManager);

    <T extends BaseModel> void deleteById(Class<T> clazz, Long id, EntityManager entityManager);

    <T extends BaseModel> int deleteByCriteria(Class<T> clazz, Map criterias, EntityManager entityManager);

    @Deprecated
    <T extends BaseModel> List<T> executeSimpleQuery(String query, EntityManager entityManager);

    @Deprecated
    <T extends BaseModel> List<T> executeQuery(Query query, EntityManager entityManager);

    @Deprecated
    <T extends BaseModel> List<T> executeSimpleQueryWithLimit(String query, EntityManager entityManager, int limit);

    <T extends BaseModel> List<T> executeSimpleQueryWithLimit(String query, EntityManager entityManager, int pageNumber,
            int pageSize, ArrayList paramList);

    <T extends BaseModel> boolean isUnique(T entity, Map<String, Object> param, EntityManager entityManager);

    @Deprecated
    <T extends BaseModel> T getByCriteria(Class<T> clazz, Map criterias, EntityManager entityManager);

    @Deprecated
    <T extends BaseModel> List<T> findByCriteria(Class<T> clazz, Map criterias, EntityManager entityManager);

    @Deprecated
    <T extends BaseModel> List<T> findByCriteria(Class<T> clazz, Map criterias, String orderField, boolean desc,
            EntityManager entityManager);

    <T extends BaseModel> Long executeCountQuery(String query, EntityManager entityManager, ArrayList inParams);

    <T extends BaseModel> List<T> queryWithParams(String query, EntityManager entityManager, ArrayList paramList);

    <T extends BaseModel> List<T> queryWithNamedParams(String query, EntityManager entityManager,
            Map<String, Object> paramMap);

    BigInteger executeCountNativeQuery(String query, EntityManager entityManager, ArrayList paramList);

    List<Object[]> executeNativeQueryWithParam(String queryStr, EntityManager entityManager, ArrayList paramList);

    <T extends BaseModel> List<T> queryWithParamsName(String query, EntityManager entityManager, ArrayList paramList);

    List<Object[]> queryWithParamsNameGeneric(String query, EntityManager entityManager, ArrayList paramList);

    <T extends BaseModel> int deleteByParamName(String query, EntityManager entityManager, ArrayList paramList);

    List<Object[]> executeNativeQueryWithParamName(String queryStr, EntityManager entityManager, ArrayList paramList);
}
