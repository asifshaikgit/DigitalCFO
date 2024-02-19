package service;

import com.idos.dao.SpecificsDAO;
import com.idos.dao.SpecificsDAOImpl;
import com.idos.util.IDOSException;

import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil K. Namdev on 06-07-2017.
 */
public interface SpecificsService extends BaseService {

    public void getChildCOA(JsonNode json, Users user, EntityManager entityManager, ObjectNode results)
            throws IDOSException;

    public void getTaxCOAChilds(int coaIdentForDataValid, EntityManager entityManager, Users user, String coaActCode,
            ArrayNode an);

    public void getTaxCOAChildsForBranch(int coaIdentForDataValid, EntityManager entityManager, Users user,
            String coaActCode, Long branchId, ArrayNode an);

    public Boolean isSpecificHasTxnsAndOpeningBal(Specifics specifics, EntityManager entityManager)
            throws IDOSException;
}
