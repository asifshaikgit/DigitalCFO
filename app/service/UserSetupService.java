package service;

import com.idos.util.IDOSException;
import model.Users;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @auther Sunil Namdev created on 31.07.2018
 */
public interface UserSetupService extends BaseService {
    ObjectNode getAssetsCoaChildNodesWithAllHeads(EntityManager entityManager, Users user);

    void saveUpdateTransactionRule(String items, String fromAmounts, String toAmounts, EntityManager em, Users newUser,
            Integer headType, Long userRight, boolean isNew) throws IDOSException;

    String getAllCoaItemsList(StringBuilder fromAmount, StringBuilder toAmount, EntityManager em, Users user,
            int particular) throws IDOSException;
}
