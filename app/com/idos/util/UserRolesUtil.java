package com.idos.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Query;

import java.util.logging.Logger;
import java.util.logging.Level;

import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;

import model.Branch;
import model.UserRightInBranch;
import model.Users;
import model.UsersRoles;

public class UserRolesUtil {

	private final static String APROVER_HQL = "select obj from UsersRoles obj where obj.organization.id = ?1 and obj.role.name = ?2 and obj.presentStatus = 1";
	private final static String BRANCH_USER_RIGHT_HQL = "select obj from UserRightInBranch obj where obj.organization.id = ?1 and obj.branch.id = ?2 and obj.user.id = ?3 and obj.userRights.id = ?4 and obj.presentStatus = 1";
	public static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Inject
	public UserRolesUtil(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	public static Logger log = Logger.getLogger("util");
	public static DecimalFormat decimalFormat = new DecimalFormat("######.00");
	public static SimpleDateFormat idosdf = new SimpleDateFormat("MMM dd,yyyy");
	public static SimpleDateFormat idosmdtdf = new SimpleDateFormat("MMM dd");
	public static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
	public static SimpleDateFormat mysqldf = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat mysqlmdtdf = new SimpleDateFormat("MM-dd");
	public static SimpleDateFormat reportdf = new SimpleDateFormat("dd-MM-yyyy");
	public static SimpleDateFormat mysqldtf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	public static SimpleDateFormat timefmt = new SimpleDateFormat("HH:mm:ss");
	public static Map criterias = new HashMap();
	public static SimpleDateFormat monthtext = new SimpleDateFormat("MMM");
	public static GenericDAO genericDAO = new GenericJpaDAO();

	public static List<String> approverAdditionalApprovalBasedOnSelectedBranch(Users user, Branch branch,
			EntityManager entityManager) {
		List<String> setString = new ArrayList<String>();

		Query query = entityManager.createQuery(APROVER_HQL);
		query.setParameter(1, user.getOrganization().getId());
		query.setParameter(2, "APPROVER");
		List<UsersRoles> approverRole = query.getResultList();
		String approverEmails = "";
		String additionalApprovarUsers = "";
		String selectedAdditionalApproval = "";
		for (UsersRoles usrRoles : approverRole) {
			additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
			if (usrRoles != null && branch != null) {
				Query usrRoleQuery = entityManager.createQuery(BRANCH_USER_RIGHT_HQL);
				usrRoleQuery.setParameter(1, user.getOrganization().getId());
				usrRoleQuery.setParameter(2, branch.getId());
				usrRoleQuery.setParameter(3, usrRoles.getUser().getId());
				usrRoleQuery.setParameter(4, 2L);
				List<UserRightInBranch> userHasRightInBranch = usrRoleQuery.getResultList();
				if (userHasRightInBranch != null) {
					approverEmails += usrRoles.getUser().getEmail() + ",";
				}
			}
		}
		setString.add(approverEmails);
		setString.add(additionalApprovarUsers);
		setString.add(selectedAdditionalApproval);
		return setString;
	}

	public static List<String> allControllersInTheOrganization(Users user) {
		List<String> setString = new ArrayList<String>();
		criterias.clear();
		criterias.put("role.name", "CONTROLLER");
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		String controllersEmails = "";
		List<UsersRoles> controllerRole = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);
		for (UsersRoles usrRoles : controllerRole) {
			controllersEmails += usrRoles.getUser().getEmail() + ",";
		}
		setString.add(controllersEmails);
		return setString;
	}

	public static List<String> allAccountantsInTheOrganization(Users user) {
		List<String> setString = new ArrayList<String>();
		criterias.clear();
		criterias.put("role.name", "ACCOUNTANT");
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		String accountantsEmails = "";
		List<UsersRoles> accountantRole = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);
		for (UsersRoles usrRoles : accountantRole) {
			accountantsEmails += usrRoles.getUser().getEmail() + ",";
		}
		setString.add(accountantsEmails);
		return setString;
	}

	public static List<String> allAuditorsInTheOrganization(Users user) {
		List<String> setString = new ArrayList<String>();
		criterias.clear();
		criterias.put("role.name", "AUDITOR");
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		String auditorsEmails = "";
		List<UsersRoles> auditorRole = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);
		for (UsersRoles usrRoles : auditorRole) {
			auditorsEmails += usrRoles.getUser().getEmail() + ",";
		}
		setString.add(auditorsEmails);
		return setString;
	}
}
