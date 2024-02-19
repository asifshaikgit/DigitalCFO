package com.idos.dao;

import com.idos.util.IDOSException;
import model.BOMItemModel;
import model.BOMModel;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

/**
 * @author Harish Kumar created on 25.04.2023
 */
public interface BOMDAO extends BaseDAO {

    String BOM_BY_ORG_JPQL = "from BOMModel obj where obj.organization.id = ?1";
    Map<String, String> add(JsonNode json, Users user, EntityManager em) throws IDOSException;
    Map<String, String> update(JsonNode json , Users user, EntityManager em, long bomId) throws IDOSException;
    List<BOMModel> getByOrg(Users user, EntityManager em) throws IDOSException;
    
}
