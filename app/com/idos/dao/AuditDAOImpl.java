package com.idos.dao;

import java.util.Calendar;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.AuditLogs;
import model.BaseModel;
import model.Users;

public class AuditDAOImpl implements AuditDAO {

	@Override
	public <T extends BaseModel> void saveAuditLogs(String action, Users user, Long entityId, Class<T> clazz,
			String ipAddress, String entityData, EntityManager entityManager) {
		AuditLogs auditLogs = new AuditLogs();
		auditLogs.setCreatedAt(Calendar.getInstance().getTime());
		auditLogs.setCreatedBy(user);
		auditLogs.setEntityId(entityId);
		auditLogs.setEntityName(clazz.getSimpleName());
		auditLogs.setJsonEntityData(entityData);
		auditLogs.setIpAddress(ipAddress);
		auditLogs.setAction(action);
		if (user != null) {
			auditLogs.setBranch(user.getBranch());
			auditLogs.setOrganization(user.getOrganization());
		} else {
			auditLogs.setBranch(null);
			auditLogs.setOrganization(null);
		}
		genericDao.saveOrUpdate(auditLogs, user, entityManager);
	}
}
