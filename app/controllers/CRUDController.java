/**
 *  @author Mritunjay
 */

package controllers;

import java.util.List;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;

import model.BaseModel;
import model.Users;
import play.mvc.Http;
import play.mvc.Http.Request;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import play.Application;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import java.util.logging.Level;

@SuppressWarnings("unchecked")
public class CRUDController<T extends BaseModel> extends StaticController {
	public static JPAApi jpaApi;
	public static EntityManager entityManager;
	public static Application application;
	public Request request;

	@Inject
	public CRUDController(Application application) {
		super(application);
	}

	protected Class<T> getEntityClass() {
		return (Class<T>) (getClass());
	}

	protected T getEntityInstance() {
		log.log(Level.FINE, ">>>> Start");
		try {
			return (T) Class.forName(getEntityClass().getName()).newInstance();
		} catch (InstantiationException e) {
			String strBuff = getStackTraceMessage(e);
			expService.sendExceptionReport(strBuff, "GetEntityInstance Email", "GetEntityInstance Oragnization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			String strBuff = getStackTraceMessage(e);
			expService.sendExceptionReport(strBuff, "GetEntityInstance Email", "GetEntityInstance Oragnization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			String strBuff = getStackTraceMessage(e);
			expService.sendExceptionReport(strBuff, "GetEntityInstance Email", "GetEntityInstance Oragnization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			throw new RuntimeException(e);
		}
	}

	public String list(EntityManager entityManager) {
		log.log(Level.FINE, ">>>> Start");
		String returnMsg = "";
		StringBuilder sbquery = new StringBuilder("");
		sbquery.append("select obj from Users obj where obj.presentStatus=1");
		List<Users> users = genericDAO.executeSimpleQuery(sbquery.toString(), entityManager);
		if (!users.isEmpty()) {
			returnMsg = "Retrieved entity list";
		} else {
			returnMsg = "There Is Not Any Data For The Entity";
		}
		return returnMsg;
	}

	public String save(Users user, T entity, EntityManager entityManager) {
		log.log(Level.FINE, ">>>> Start ");
		// Users user = getUserInfo(request);
		String returnMsg = "";
		try {
			if (entity.getId() == null) {
				returnMsg = "successfully created new entity record";
			} else {
				returnMsg = "entity record updated successfully";
			}
			genericDAO.saveOrUpdate(entity, user, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error saving record " + entity.getId(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			returnMsg = "";
		}
		log.log(Level.FINE, ">>>> End ");
		return returnMsg;
	}

	public String delete(T entity, EntityManager entityManager) {
		log.log(Level.FINE, ">>>> Start");
		String returnMsg = "";
		try {
			genericDAO.deleteById(entity.getClass(), entity.getId(), entityManager);
			returnMsg = "entity record deleted successfully";
		} catch (Exception ex) {
			log.log(Level.WARNING, "Error deleting record " + entity.getId(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Delete Email", "Delete Oragnization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		// return to list
		return returnMsg;
	}
}
