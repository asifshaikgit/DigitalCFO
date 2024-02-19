package com.idos.dao;

import com.idos.util.IDOSException;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONException;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil on 30-01-2017.
 */
public interface OrganizationDAO extends BaseDAO {
    boolean saveOrgSerialNumber(Users user, JsonNode json, EntityManager entityManager, ObjectNode result)
            throws IDOSException;

    boolean savePlaceOfSupplyType(Users user, JsonNode json, EntityManager entityManager) throws IDOSException;

    boolean saveOrgGstinSerialNumber(Users user, JsonNode json, EntityManager entityManager, ObjectNode result)
            throws IDOSException, JSONException;

    ObjectNode getOrgGstinSerialNumber(Users user, EntityManager entityManager) throws IDOSException;

    boolean saveTdsApplicableTrans(Users user, JsonNode json, EntityManager entityManager)
            throws IDOSException, JSONException;
}
