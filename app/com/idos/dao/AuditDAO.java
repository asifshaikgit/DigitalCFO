package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.BaseModel;
import model.Users;

public interface AuditDAO extends BaseDAO {
	<T extends BaseModel> void saveAuditLogs(String action, Users user, Long entityId, Class<T> clazz, String ipAddress,
			String entityData, EntityManager entityManager);
}
