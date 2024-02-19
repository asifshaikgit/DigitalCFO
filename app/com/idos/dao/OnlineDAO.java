package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface OnlineDAO extends BaseDAO {
	public ObjectNode getOnlineIdosUsers(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);
}
