package com.idos.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;

public interface GSTR2JsonDAO extends BaseDAO {
	ObjectMapper OBJ_MAPPER = new ObjectMapper();
	public static final String B2B_QUERY = "";

	public String createGSTR2Json(JsonNode json, Users user, EntityManager entityManager) throws Exception;
}
