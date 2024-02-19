package com.idos.util;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import model.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sunil Namdev on 28-07-2016.
 */
public class ChartOfAccountUtil {
    private static GenericDAO genericDAO = new GenericJpaDAO();
    private static final String CHILD_COA_ORG_HQL = "select obj from Specifics obj WHERE obj.organization.id =?1 and obj.presentStatus = 1 and obj.accountCodeHirarchy like ?2";
    private static EntityManager entityManager;

    public ChartOfAccountUtil() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    public static List<Specifics> getChildCOA(String coaActCode, Users user, Long branchId,
            EntityManager entityManager) {
        ArrayList inparams = new ArrayList(2);
        inparams.add(user.getOrganization().getId());
        inparams.add("%/" + coaActCode + "/");
        List<Specifics> specifics = genericDAO.queryWithParams(CHILD_COA_ORG_HQL, entityManager, inparams);
        return specifics;
    }

    public static void getCoaLeafNodes(List<Specifics> specfList, Users user, Long branchId,
            EntityManager entityManager, List<Specifics> leafItems) {
        for (Specifics specifics : specfList) {
            System.out.println("Specific ID : " + specifics.getId());
            List<Specifics> specListL1 = getChildCOA(specifics.getAccountCode().toString(), user, branchId,
                    entityManager);
            if (specListL1 != null && specListL1.size() > 0) {
                getCoaLeafNodes(specListL1, user, branchId, entityManager, leafItems);
            } else {
                leafItems.add(specifics);
            }
        }
    }

    public static void getCoaLeafNodesForSpecific(Specifics specifics, Users user, Long branchId,
            EntityManager entityManager, List<Specifics> leafItems) {
        List<Specifics> specListL1 = getChildCOA(specifics.getAccountCode().toString(), user, branchId, entityManager);
        if (specListL1 != null && specListL1.size() > 0) {
            getCoaLeafNodes(specListL1, user, branchId, entityManager, leafItems);
        } else {
            leafItems.add(specifics);
        }
    }
}
