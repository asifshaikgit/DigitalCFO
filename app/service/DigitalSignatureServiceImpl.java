package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.fasterxml.jackson.databind.JsonNode;

import com.idos.dao.DigitalSignatureDAO;
import com.idos.util.IDOSException;

import model.Branch;
import model.Organization;
import model.Users;
import play.mvc.Result;

public class DigitalSignatureServiceImpl implements DigitalSignatureService {

	@Override
	public boolean digitalSignatureService(Organization org, Branch branch, Users user, JsonNode json,
			EntityManager entityManager) throws IDOSException {

		return DIGITAL_SIGNATURE_DAO.digitalSignatureDAO(org, branch, user, json, entityManager);
	}

}
