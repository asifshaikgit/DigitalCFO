package service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import java.util.logging.Logger;
import java.util.logging.Level;

import model.PasswordHistory;
import model.Users;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;

public class PasswordChangeService implements BaseService {
	public static boolean isPresentInPasswordHistory(final String newPwd, final Users user,
			final EntityManager entityManager) {

		boolean result = false;
		if ((!"".equals(newPwd) || null != newPwd) && null != user && null != entityManager) {
			String query = "SELECT obj FROM PasswordHistory obj WHERE obj.user.id = " + user.getId()
					+ " AND obj.userEmail = '" + user.getEmail() + "' AND obj.password = '" + newPwd
					+ "' ORDER BY obj.createdAt DESC";
			List<PasswordHistory> passwords = genericDAO.executeSimpleQueryWithLimit(query, entityManager, 5);
			if (!passwords.isEmpty() && passwords.size() > 0) {
				result = true;
			} else {
				result = false;
			}
		}
		return result;
	}

	public static long passwordChangeDifference(final Date lastPwdUpdate) {
		long result = 0;
		if (null != lastPwdUpdate) {
			Date currentDate = Calendar.getInstance().getTime();
			result = (currentDate.getTime() - lastPwdUpdate.getTime()) / (24 * 60 * 60 * 1000);
		}
		return result;
	}
}
