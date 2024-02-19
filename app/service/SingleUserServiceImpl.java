package service;

import model.Branch;
import model.Organization;
import model.Project;
import model.Specifics;
import model.Transaction;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SingleUserServiceImpl implements SingleUserService {

	@Override
	public void updateOnOrganizationCreation(Users user, Organization org, Branch branch, EntityManager entityManager) {
		singleUserDAO.updateOnOrganizationCreation(user, org, branch, entityManager);
	}

	@Override
	public void updateOnBranchCreation(Users user, Branch branch, EntityManager entityManager) {
		singleUserDAO.updateOnBranchCreation(user, branch, entityManager);
	}

	@Override
	public void updateOnCOACreation(Users user, Specifics specific, EntityManager entityManager) {
		singleUserDAO.updateOnCOACreation(user, specific, entityManager);
	}

	@Override
	public void updateOnProjectCreation(Users user, Project project, EntityManager entityManager) {
		singleUserDAO.updateOnProjectCreation(user, project, entityManager);
	}

	@Override
	public ObjectNode createSingleuserJson(Transaction txn, JsonNode json, Users user) {
		// TODO Auto-generated method stub
		return singleUserDAO.createSingleuserJson(txn, json, user);
	}

}