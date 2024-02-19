package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.Users;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface OperationalService extends BaseService {
	public ObjectNode operationalAlertsDates(Users user, String monthFirstDate, String monthLastDate,
			EntityManager entityManager);
}
