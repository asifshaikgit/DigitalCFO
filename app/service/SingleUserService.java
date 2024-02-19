
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

public interface SingleUserService extends BaseService {

	public abstract void updateOnOrganizationCreation(Users user, Organization org, Branch branch,
			EntityManager entityManager);

	public abstract void updateOnBranchCreation(Users user, Branch branch, EntityManager entityManager);

	public abstract void updateOnCOACreation(Users user, Specifics specific, EntityManager entityManager);

	public abstract void updateOnProjectCreation(Users user, Project project, EntityManager entityManager);

	public abstract ObjectNode createSingleuserJson(Transaction txn, JsonNode json, Users user);
}
