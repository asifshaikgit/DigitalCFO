package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;

import com.idos.util.IDOSException;

import model.Branch;
import model.Organization;
import model.Users;

public interface DigitalSignatureDAO extends BaseDAO {
	public boolean digitalSignatureDAO(Organization org, Branch branch, Users user, JsonNode json,
			EntityManager entityManager) throws IDOSException;
}
