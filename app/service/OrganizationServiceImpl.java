package service;

import com.idos.util.IDOSException;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONException;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil Namdev on 30-01-2017.
 */
public class OrganizationServiceImpl implements OrganizationService {
	@Override
	public boolean saveOrgSerialNumber(Users user, JsonNode json, EntityManager entityManager, ObjectNode result)
			throws IDOSException {
		return ordDao.saveOrgSerialNumber(user, json, entityManager, result);
	}

	@Override
	public boolean savePlaceOfSupplyType(Users user, JsonNode json, EntityManager entityManager) throws IDOSException {
		return ordDao.savePlaceOfSupplyType(user, json, entityManager);
	}

	@Override
	public ObjectNode getOrgGstinSerialNumber(Users user, EntityManager entityManager) throws IDOSException {
		return ordDao.getOrgGstinSerialNumber(user, entityManager);
	}

	@Override
	public boolean saveOrgGstinSerialNumber(Users user, JsonNode json, EntityManager entityManager, ObjectNode result)
			throws IDOSException, JSONException {
		return ordDao.saveOrgGstinSerialNumber(user, json, entityManager, result);
	}

	@Override
	public boolean saveTdsApplicableTrans(Users user, JsonNode json, EntityManager entityManager)
			throws IDOSException, JSONException {
		// TODO Auto-generated method stub
		return ordDao.saveTdsApplicableTrans(user, json, entityManager);
	}

}
