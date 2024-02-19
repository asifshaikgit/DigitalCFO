package com.idos.cache;

import model.OrganizationConfig;
import play.db.jpa.JPAApi;
import service.EntityManagerProvider;


import javax.transaction.Transactional;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import javax.inject.*;
import java.util.Map.Entry;

public class OrganizationConfigCache {
    private static final String ORGCONFIG_JQL = "select obj from OrganizationConfig obj";
    private static HashMap<String, OrganizationConfig> orgConfigMap;
    private static JPAApi jpaApi;
    private static EntityManager entityManager = EntityManagerProvider.getEntityManager();

    @Inject
    public OrganizationConfigCache() {
    }

    @Transactional
    public static void initialize() {

        // jpaApi.bindForCurrentThread(entityManager);
        try {
            Query query = entityManager.createQuery(ORGCONFIG_JQL);
            List<OrganizationConfig> orgConfigList = query.getResultList();
            HashMap<String, OrganizationConfig> configOrgMap = new HashMap<>();
            for (OrganizationConfig organizationConfig : orgConfigList) {
                configOrgMap.put(organizationConfig.getOrganization().getId() + "-"
                        + organizationConfig.getParameterName(), organizationConfig);
            }
            orgConfigMap = configOrgMap;
            System.out.println("org config Initialized:");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            /*
             * if(em != null && em.isOpen()){
             * em.close();
             * }
             */
        }
    }

    public static final String getParamValue(Long orgId, String paramName) {
        if (orgConfigMap == null) {
            return null;
        }
        String key = orgId + "-" + paramName;
        if (orgConfigMap.containsKey(key)) {
            return orgConfigMap.get(key).getParameterValue();
        } else {
            return null;
        }
    }
}
