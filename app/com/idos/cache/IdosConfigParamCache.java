package com.idos.cache;

import model.IdosConfigParamModel;
import model.Specifics;
import play.db.jpa.JPAApi;
import service.EntityManagerProvider;

import javax.transaction.Transactional;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import javax.inject.*;

public class IdosConfigParamCache {
    private static JPAApi jpaApi;
    private static EntityManager entityManager = EntityManagerProvider.getEntityManager();

    @Inject
    public IdosConfigParamCache() {
        // initialize();
    }

    private static final String IDOSPARAM_JQL = "select obj from IdosConfigParamModel obj";
    private static HashMap<String, String> idosParamMap;

    @Transactional
    public static void initialize() {
        // EntityManager entityManager =
        // jpaApi.em("default").getEntityManagerFactory().createEntityManager();
        // JPAApi.bindForCurrentThread(entityManager);
        try {
            System.out.println("SK >>>>>>>>>>>> IdosConfigParamCache IDOSPARAM_JQL : " + IDOSPARAM_JQL);
            Query query = entityManager.createQuery(IDOSPARAM_JQL);
            // Query query = entityManager.createNativeQuery(IDOSPARAM_JQL,
            // IdosConfigParamModel.class);
            System.out.println("SK >>>>>>>>>>>> IdosConfigParamCache query : " + query);
            List<IdosConfigParamModel> idosParamList = query.getResultList();
            System.out.println("SK >>>>>>>>>>>> IdosConfigParamCache query response : " + idosParamList.toString());
            HashMap<String, String> tmpMap = new HashMap<>();
            for (IdosConfigParamModel idosParam : idosParamList) {
                tmpMap.put(idosParam.getParameterName(), idosParam.getParameterValue());
            }
            idosParamMap = tmpMap;
            System.out.println("idos config param has Initialized:");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final String getParamValue(String paramName) {
        if (idosParamMap == null) {
            return null;
        }
        if (paramName == null) {
            return null;
        }
        if (idosParamMap.containsKey(paramName)) {
            return idosParamMap.get(paramName);
        } else {
            return null;
        }
    }
}
