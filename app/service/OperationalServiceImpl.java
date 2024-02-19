package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.dao.OperationalDAO;
import com.idos.dao.OperationalDAOImpl;

public class OperationalServiceImpl implements OperationalService {
	@Override
	public ObjectNode operationalAlertsDates(Users user, String monthFirstDate, String monthLastDate,
			EntityManager entityManager) {
		log.log(Level.FINE, "============ Start");
		ObjectNode result = operationalDao.operationalAlertsDates(user, monthFirstDate, monthLastDate, entityManager);
		return result;
	}

}
