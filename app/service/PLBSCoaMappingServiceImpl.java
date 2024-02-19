package service;

import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @auther Sunil Namdev created on 05.06.2018
 */
public class PLBSCoaMappingServiceImpl implements PLBSCoaMappingService {

    @Override
    public ObjectNode getCoaForOrganizationWithAllHeads(ObjectNode result, JsonNode json, Users user,
            EntityManager entityManager) {
        result = PLBS_COA_MAPPING_DAO.getCoaForOrganizationWithAllHeads(result, json, user, entityManager);
        return result;
    }
}
