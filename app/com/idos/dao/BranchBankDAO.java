package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public interface BranchBankDAO extends BaseDAO {
	ObjectNode branchBank(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);

	ObjectNode branchBankDetails(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);

	boolean getOrganizationBanks(ObjectNode result, Users user, EntityManager em);
}
