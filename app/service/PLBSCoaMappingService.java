package service;

import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @auther Sunil Namdev created on 05.06.2018
 */
public interface PLBSCoaMappingService extends BaseService {
    ObjectNode getCoaForOrganizationWithAllHeads(ObjectNode result, JsonNode json, Users user,
            EntityManager entityManager);
}
