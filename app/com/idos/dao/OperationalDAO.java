package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.Users;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface OperationalDAO extends BaseDAO {
	public ObjectNode operationalAlertsDates(Users user, String monthFirstDate, String monthLastDate,
			EntityManager entityManager);
}
