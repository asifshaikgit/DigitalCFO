package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;

import com.idos.util.IDOSException;

import model.Branch;
import model.Organization;
import model.Users;
import play.mvc.Result;

public interface DigitalSignatureService extends BaseService {

	public boolean digitalSignatureService(Organization org, Branch branch, Users user, JsonNode json,
			EntityManager entityManager) throws IDOSException;

}
