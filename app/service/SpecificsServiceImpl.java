package service;

import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IDOSException;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil K. Namdev on 06-07-2017.
 */
public class SpecificsServiceImpl implements SpecificsService {
	@Override
	public void getChildCOA(JsonNode json, Users user, EntityManager entityManager, ObjectNode results)
			throws IDOSException {
		specificDAO.getChildCOA(json, user, entityManager, results);
	}

	@Override
	public void getTaxCOAChilds(int coaIdentForDataValid, EntityManager entityManager, Users user, String coaActCode,
			ArrayNode an) {
		specificDAO.getTaxCOAChilds(coaIdentForDataValid, entityManager, user, coaActCode, an);
	}

	@Override
	public void getTaxCOAChildsForBranch(int coaIdentForDataValid, EntityManager entityManager, Users user,
			String coaActCode, Long branchId, ArrayNode an) {
		specificDAO.getTaxCOAChildsForBranch(coaIdentForDataValid, entityManager, user, coaActCode, branchId, an);
	}

	@Override
	public Boolean isSpecificHasTxnsAndOpeningBal(Specifics specifics, EntityManager entityManager)
			throws IDOSException {
		return specificDAO.isSpecificHasTxnsAndOpeningBal(specifics, entityManager);
	}

}
