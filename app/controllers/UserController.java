package controllers;

import java.util.*;
import java.nio.charset.StandardCharsets;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.cache.UserTxnRuleSetupCache;
import com.idos.util.*;
import java.util.logging.Level;
import model.Branch;
import model.ConfigParams;
import model.ExpenseGroup;
import model.Organization;
import model.OrganizationKeyOfficials;
import model.OrganizationOperationalRemainders;
import model.PasswordHistory;
import model.Project;
import model.Role;
import model.Specifics;
import model.StatutoryDetails;
import model.Transaction;
import model.TransactionItems;
import model.TransactionPurpose;
import model.Travel_Group;
import model.UserProfileSecurity;
import model.UserRightForProject;
import model.UserRightInBranch;
import model.UserRightSpecifics;
import model.UserRights;
import model.UserTransactionPurpose;
import model.Users;
import model.UsersRoles;
import model.Vendor;
import model.karvy.ExternalUserCompanyDetails;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.type.TypeReference;
import javax.inject.Inject;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Results;
import service.PasswordChangeService;
import views.html.*;
import actor.AdminActor;
import actor.CreatorActor;
import actor.ProjectTransactionActor;
import actor.SpecificsTransactionActor;
import actor.VendorTransactionActor;
import akka.util.ByteString;
import play.mvc.Http;
import play.mvc.Http.Request;
// import play.mvc.Http.Session;
import play.Application;

public class UserController extends StaticController {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;
	// private Request request;

	@Inject
	public UserController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result logout(Http.Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		String fullName = null;
		String role = null;
		String email = null;
		String pwdchangemsg = null;
		String allowedProcurement = null;
		Organization orgn = null;
		Users user = null;
		ObjectNode results = Json.newObject();
		try {
			user = getUserInfo(request);
			ArrayNode an = results.putArray("usertype");
			ObjectNode row = Json.newObject();
			if (user != null) {
				user.setInSession(0);
				user.setAuthToken(null);
				entitytransaction.begin();
				genericDAO.saveOrUpdate(user, user, entityManager);
				entitytransaction.commit();
				session.removing();
				CookieUtils.discardCookie("user");
				CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
				System.out.println("logout" + user.getEmail());
				// SpecificsTransactionActor.unregister(user.getEmail());
				// VendorTransactionActor.unregister(user.getEmail());
				// CreatorActor.unregister(user.getEmail());
				// AdminActor.unregister(user.getEmail());
				// ProjectTransactionActor.unregister(user.getEmail());
				/*
				 * ObjectNode result = OnlineServiceImpl.getInstance().getOnlineOrgUsers(user,
				 * entityManager);
				 * if (result.findValue("result").asBoolean()) {
				 * JsonNode node = result.findValue("users");
				 * if (node.isArray()) {
				 * AdminActor.online(result.findValue("result").asBoolean(), (ArrayNode) node);
				 * CreatorActor.online(result.findValue("result").asBoolean(), (ArrayNode)
				 * node);
				 * }
				 * }
				 */
				results.put("logout", user.getEmail());
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End");
		return Results.ok(results);
	}

	@Transactional
	public Result subslogin(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		session.removing();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("logincredentials");
			ObjectNode row = Json.newObject();
			String username = json.findValue("userName").asText();
			String password = json.findValue("loginpwd").asText();
			Users users = Users.findByEmailAddressAndPassword(entityManager, username, password);
			if (users != null && users.getPresentStatus() == 1) {
				users.setLastLoginDate(Calendar.getInstance().getTime());
				users.setInSession(1);
				genericDAO.saveOrUpdate(users, users, entityManager);
				String role = "";
				List<UsersRoles> userRoles = users.getUserRoles();
				for (UsersRoles roles : userRoles) {
					role += roles.getRole().getName() + ",";
				}
				role = role.substring(0, role.length() - 1);
				session.adding("email", username);
				session.adding("role", userRoles.get(0).getRole().getName());
				String result = "Success";
				row.put("useremail", users.getEmail());
				row.put("message", result);
				row.put("userrole", role);
				an.add(row);
				String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
				if (ipAddress == null) {
					ipAddress = request.remoteAddress();
				}
				String action = "client from ip address " + ipAddress + " login into system";
				auditDAO.saveAuditLogs(action, users, users.getId(), Users.class, ipAddress, row.toString(),
						entityManager);
				transaction.commit();
			} else {
				String result = "Failure";
				row.put("message", result);
				String reason = "Login failed! Your email or password was entered incorrectly.";

				if (users != null && users.getPresentStatus() == 0) {
					reason = "Login failed! Please Activate Your Account.";
				}

				row.put("failurereason", reason);
				an.add(row);
				String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
				if (ipAddress == null) {
					ipAddress = request.remoteAddress();
				}
				String action = "An attempt to login from ip address " + ipAddress + " is failed";
				auditDAO.saveAuditLogs(action, null, null, Users.class, ipAddress, row.toString(), entityManager);
				transaction.commit();
			}
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Subslogin Email", "Subslogin Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result forgotCustVendLoginInfo(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("vendorCustomerAccountData");
			String accountOrganization = json.findPath("accountOrganization").asText();
			String accountEmail = json.findPath("accountEmail").asText();
			String entityType = json.findPath("entityType").asText();
			String accountPassword = json.findPath("accountPassword").asText();
			Integer type = null;
			if (entityType.equals("Vendor")) {
				type = 1;
			}
			if (entityType.equals("Customer")) {
				type = 2;
			}
			Organization org = Organization.findById(Long.parseLong(accountOrganization));
			Map<Object, Object> criteria = new HashMap<Object, Object>();
			criteria.clear();
			criteria.put("email", accountEmail);
			criteria.put("organization.id", org.getId());
			criteria.put("type", type);
			criteria.put("accessPassword", PasswordUtil.encrypt(accountPassword));
			criteria.put("presentStatus", 1);
			List<Vendor> custvendors = genericDAO.findByCriteria(Vendor.class, criteria, entityManager);
			if (custvendors != null && custvendors.size() > 0) {
				Vendor existingVendor = null;
				if (custvendors.size() > 0) {
					existingVendor = custvendors.get(0);
				}
				existingVendor.setAccessPassword(PasswordUtil.encrypt(accountPassword));
				genericDAO.saveOrUpdate(existingVendor, null, entityManager);
				String body = vendorCustomerAccountAccessLink.render(accountPassword, existingVendor.getEmail(),
						org.getName(), org.getId().toString(), entityType, ConfigParams.getInstance()).body();
				final String username = ConfigFactory.load().getString("smtp.user");
				Session session = emailsession;
				String subject = "Vendor/Customer Account Access Link for Organization: " + org.getName() + "";
				mailTimer(body, username, session, existingVendor.getEmail(), null, subject);
				ObjectNode row = Json.newObject();
				row.put("message", "success");
				an.add(row);
			} else {
				ObjectNode row = Json.newObject();
				row.put("message", "failure");
				an.add(row);
			}
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "ForgotCustVendLoginInfo Email",
					"ForgotCustVendLoginInfo Organization", Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result accountChanged(Http.Request request) {
		Http.Session session = request.session();
		log.log(Level.FINE, ">>>> Start");
		String fullName = null;
		String email = null;
		String role = null;
		String pwdchangemsg = null;
		String allowedProcurement = null;
		Organization orgn = null;
		session.removing();
		return Results.ok(registration.render(fullName, email, role, orgn, allowedProcurement, pwdchangemsg,
				ConfigParams.getInstance()));
	}

	@Transactional
	public Result getRoles(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		Http.Session session = request.session();
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			String role = session.getOptional("role").orElse("");
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("userRolesData");
			user = getUserInfo(request);

			List<Role> sessionrole = Role.findByRoleName(entityManager, role);
			List<UsersRoles> userRoles = user.getUserRoles();
			if (!userRoles.isEmpty()) {
				for (UsersRoles userRole : userRoles) {
					ObjectNode row = Json.newObject();
					row.put("id", userRole.getRole().getId());
					row.put("name", userRole.getRole().getName());
					row.put("selectedrole", sessionrole.get(0).getId());
					an.add(row);
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result changeRole(Http.Request request) {
		Http.Session session = request.session();
		log.log(Level.FINE, ">>>> Start");
		JsonNode json = request.body().asJson();
		ObjectNode results = Json.newObject();
		String roleId = json.findValue("roleid").asText();
		Role userrole = Role.findById(Long.parseLong(roleId));
		session.adding("role", userrole.getName());
		return Results.ok(results);
	}

	@Transactional
	public Result changePassword(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("userChangedPwdData");
			String question = json.findValue("question") != null ? json.findValue("question").asText() : null;
			String questionId = json.findValue("questionId") != null ? json.findValue("questionId").asText() : null;
			String answer = json.findValue("answer") != null ? json.findValue("answer").asText() : null;
			String userId = json.findValue("userId").asText();
			boolean update = json.findValue("update").asBoolean();
			UserProfileSecurity security = null;
			Map<String, Object> criterias = new HashMap<String, Object>();
			if ((null != question || null != questionId || null != answer) && !update) {
				criterias.put("user.id", Long.parseLong(userId));
				criterias.put("id", Long.parseLong(questionId));
				criterias.put("securityQuestion", question);
				criterias.put("securedAnswer", answer);
				criterias.put("presentStatus", 1);
				security = genericDAO.getByCriteria(UserProfileSecurity.class, criterias, entityManager);
				if (null != security) {
					update = true;
				} else {
					update = false;
				}
			}
			if (update) {
				String accountCred = json.findValue("accounttCred").asText();
				String accountOldPwd = json.findValue("oldPwd").asText();
				String accountNewPwd = json.findValue("newPwd").asText();
				Users user = Users.findByEmailAddressAndPassword(entityManager, accountCred, accountOldPwd);
				if (user != null) {
					if (!PasswordChangeService.isPresentInPasswordHistory(PasswordUtil.encrypt(accountNewPwd), user,
							entityManager)) {
						user.setPassword(PasswordUtil.encrypt(accountNewPwd));
						user.setLastUpdatedPasswordDate(Calendar.getInstance().getTime());
						usercrud.save(user, user, entityManager);
						PasswordHistory history = new PasswordHistory(user.getEmail(), user, user.getOrganization(),
								PasswordUtil.encrypt(accountNewPwd));
						genericDAO.saveOrUpdate(history, user, entityManager);
						Session mailsession = emailsession;
						final String username = ConfigFactory.load().getString("smtp.user");
						String body = passwordChange
								.render(accountNewPwd, user.getFullName(), user.getEmail(), ConfigParams.getInstance())
								.body();
						String subject = "Login Details";
						session.adding("pwdchanged", "Password Changed Succesfully.");
						transaction.commit();
						mailTimer(body, username, mailsession, user.getEmail(), null, subject);
						ObjectNode row = Json.newObject();
						row.put("pwdchanged", "Password Changed Succesfully.");
						an.add(row);
					} else {
						ObjectNode row = Json.newObject();
						row.put("pwdchanged",
								"Password Provided has been Already Used in the Past History. Please try a Unique Password.");
						an.add(row);
					}
				} else {
					ObjectNode row = Json.newObject();
					row.put("pwdchanged", "Not Able to Find Account with Provided Account Credential.");
					an.add(row);
				}
			} else {
				if (null == security) {
					ObjectNode row = Json.newObject();
					row.put("pwdchanged",
							"Security Answer Provided is not as per the records. Please Correct you Answer.");
					an.add(row);
				} else {
					ObjectNode row = Json.newObject();
					row.put("pwdchanged", "Not Able to Find Account with Provided Account Credential.");
					an.add(row);
				}
			}
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "ChangePassword Email", "ChangePassword Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result showUserDetails(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		ObjectNode results = Json.newObject();
		Users loggedUser = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode userdetailan = results.putArray("userdetailsData");
			loggedUser = getUserInfo(request);
			if (loggedUser == null) {
				log.log(Level.SEVERE, "unauthorized access");
				return unauthorized();
			}
			Long userEntityId = json.findValue("entityPrimaryId").asLong();
			Users user = Users.findById(userEntityId);
			if (user != null) {
				String userRoles = getUserRolesIds(user);
				String userTxnQuestions = getUserTransactionPurposeIds(user, entityManager);
				ObjectNode row = Json.newObject();
				row.put("id", user.getId());
				row.put("userTxnQuestions", userTxnQuestions);
				row.put("userRoles", userRoles);
				row.put("fullName", user.getFullName());
				row.put("emailId", user.getEmail());
				row.put("userBranch", user.getBranch().getId());
				Map<String, Object> criterias = new HashMap<String, Object>();
				if (userRoles.contains("1")) {
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("isHeadQuarter", 1);
					Branch headQuarterBranch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
					row.put("headQuarterBranch", headQuarterBranch.getId());
				}
				if (userRoles.contains("8")) {
					if (user.getBranchSafeDepositBox() != null && user.getBranchSafeDepositBox().getBranch() != null) {
						row.put("cashierBranch", user.getBranchSafeDepositBox().getBranch().getId());
					} else {
						row.put("cashierBranch", "");
					}
				}
				if (userRoles.contains("9")) {
					if (user.getBranchKeyOff() != null && user.getBranchKeyOff().getBranch() != null) {
						row.put("branchAdministratorBranch", user.getBranchKeyOff().getBranch().getId());
					} else {
						row.put("branchAdministratorBranch", "");
					}
				}
				if (userRoles.contains("12")) {
					if (user.getBranchKeyOff() != null && user.getBranchKeyOff().getBranch() != null) {
						row.put("branchOfficersBranch", user.getBranchKeyOff().getBranch().getId());
					} else {
						row.put("branchOfficersBranch", "");
					}
				}
				if (user.getAddress() != null) {
					row.put("address", user.getAddress());
				} else {
					row.put("address", "");
				}
				if (user.getDob() != null) {
					row.put("dob", idosdf.format(user.getDob()));
				} else {
					row.put("dob", "");
				}
				if (user.getMobile() != null) {
					int n = user.getMobile().indexOf("-");
					row.put("userPhnCtryCode", user.getPhoneNumberCountryCode());
					row.put("mobile", user.getMobile().substring(n + 1, user.getMobile().length()));
				} else {
					row.put("userPhnCtryCode", "");
					row.put("mobile", "");
				}
				if (user.getPhotograph() != null) {
					row.put("photograph", user.getPhotograph());
				} else {
					row.put("photograph", "");
				}
				if (user.getIdproof() != null) {
					row.put("idproof", user.getIdproof());
				} else {
					row.put("idproof", "");
				}
				row.put("bloodGroup", user.getBloodGroup());
				if (null == user.getDateOfConfirmation()) {
					row.put("confirmDate", "");
				} else {
					row.put("confirmDate", idosdf.format(user.getDateOfConfirmation()));
				}
				if (null == user.getDateOfHire()) {
					row.put("hireDate", "");
				} else {
					row.put("hireDate", idosdf.format(user.getDateOfHire()));
				}
				if (null == user.getDateOfRelease()) {
					row.put("releaseDate", "");
				} else {
					row.put("releaseDate", idosdf.format(user.getDateOfRelease()));
				}
				if (null == user.getNoticeStartDate()) {
					row.put("noticeDate", "");
				} else {
					row.put("noticeDate", idosdf.format(user.getNoticeStartDate()));
				}
				if (null == user.getSource()) {
					row.put("source", "");
				} else {
					row.put("source", user.getSource());
				}
				if (null == user.getSupervisorUser()) {
					row.put("mgr", "");
				} else {
					row.put("mgr", user.getSupervisorUser().getId());
				}
				if (null == user.getHrManager()) {
					row.put("hrMgr", "");
				} else {
					row.put("hrMgr", user.getHrManager().getId());
				}
				if (null == user.getEmploymentType()) {
					row.put("empType", "");
				} else {
					row.put("empType", user.getEmploymentType());
				}
				if (null == user.getPanNumber()) {
					row.put("pan", "");
				} else {
					row.put("pan", user.getPanNumber());
				}
				if (null == user.getPassportNumber()) {
					row.put("passport", "");
				} else {
					row.put("passport", user.getPassportNumber());
				}
				if (null == user.getDesignation()) {
					row.put("designation", "");
				} else {
					row.put("designation", user.getDesignation());
				}
				if (null == user.getDepartment()) {
					row.put("department", "");
				} else {
					row.put("department", user.getDepartment());
				}
				if (user.getTransactionCreationBranches() != null) {
					row.put("txnCreationInBranches", user.getTransactionCreationBranches());
				} else {
					row.put("txnCreationInBranches", "");
				}
				if (user.getTransactionCreationProjects() != null) {
					row.put("txnCreationForProjects", user.getTransactionCreationProjects());
				} else {
					row.put("txnCreationForProjects", "");
				}
				/*
				 * if (user.getTxnCreationCoaIncome() != null) {
				 * row.put("txnCreationCoaIn", user.getTxnCreationCoaIncome());
				 * } else {
				 * row.put("txnCreationCoaIn", "");
				 * }
				 * if (user.getTxnCreationCoaIncomeFromAmount() != null) {
				 * row.put("txnCreationCoaInFromAmount",
				 * user.getTxnCreationCoaIncomeFromAmount());
				 * } else {
				 * row.put("txnCreationCoaInFromAmount", "");
				 * }
				 * if (user.getTxnCreationCoaIncomeToAmount() != null) {
				 * row.put("txnCreationCoaInToAmount", user.getTxnCreationCoaIncomeToAmount());
				 * } else {
				 * row.put("txnCreationCoaInToAmount", "");
				 * }
				 * if (user.getTxnCreationCoaExpense() != null) {
				 * row.put("txnCreationCoaEx", user.getTxnCreationCoaExpense());
				 * } else {
				 * row.put("txnCreationCoaEx", "");
				 * }
				 * if (user.getTxnCreationCoaExpenseFromAmount() != null) {
				 * row.put("txnCreationCoaExFromAmount",
				 * user.getTxnCreationCoaExpenseFromAmount());
				 * } else {
				 * row.put("txnCreationCoaExFromAmount", "");
				 * }
				 * if (user.getTxnCreationCoaIncomeToAmount() != null) {
				 * row.put("txnCreationCoaExToAmount", user.getTxnCreationCoaIncomeToAmount());
				 * } else {
				 * row.put("txnCreationCoaExToAmount", "");
				 * }
				 * 
				 * if (user.getTxnCreationCoaAsset() != null) {
				 * row.put("txnCreationCoaAs", user.getTxnCreationCoaAsset());
				 * } else {
				 * row.put("txnCreationCoaAs", "");
				 * }
				 * if (user.getTxnCreationCoaAssetFromAmount()!= null) {
				 * row.put("txnCreationCoaAsFromAmount",
				 * user.getTxnCreationCoaAssetFromAmount());
				 * } else {
				 * row.put("txnCreationCoaAsFromAmount", "");
				 * }
				 * if (user.getTxnCreationCoaAssetToAmount() != null) {
				 * row.put("txnCreationCoaAsToAmount", user.getTxnCreationCoaAssetToAmount());
				 * } else {
				 * row.put("txnCreationCoaAsToAmount", "");
				 * }
				 * 
				 * if (user.getTxnCreationCoaLiabl() != null) {
				 * row.put("txnCreationCoaLi", user.getTxnCreationCoaLiabl());
				 * } else {
				 * row.put("txnCreationCoaLi", "");
				 * }
				 * if (user.getTxnCreationCoaLiablFromAmount()!= null) {
				 * row.put("txnCreationCoaLiFromAmount",
				 * user.getTxnCreationCoaLiablFromAmount());
				 * } else {
				 * row.put("txnCreationCoaLiFromAmount", "");
				 * }
				 * if (user.getTxnCreationCoaLiablToAmount() != null) {
				 * row.put("txnCreationCoaLiToAmount", user.getTxnCreationCoaLiablToAmount());
				 * } else {
				 * row.put("txnCreationCoaLiToAmount", "");
				 * }
				 */

				if (user.getTransactionApprovalBranches() != null) {
					row.put("txnApprovalInBranches", user.getTransactionApprovalBranches());
				} else {
					row.put("txnApprovalInBranches", "");
				}
				if (user.getTransactionApproverProject() != null) {
					row.put("txnApprovalInProjects", user.getTransactionApproverProject());
				} else {
					row.put("txnApprovalInProjects", "");
				}
				/*
				 * if (user.getTxnApprovalCoaIncome() != null) {
				 * row.put("txnApprovalCoaIn", user.getTxnApprovalCoaIncome());
				 * } else {
				 * row.put("txnApprovalCoaIn", "");
				 * }
				 * if (user.getTxnApprovalCoaIncomeFromAmount() != null) {
				 * row.put("txnApproverCoaInFromAmount",
				 * user.getTxnApprovalCoaIncomeFromAmount());
				 * } else {
				 * row.put("txnApproverCoaInFromAmount", "");
				 * }
				 * if (user.getTxnApprovalCoaIncomeToAmount() != null) {
				 * row.put("txnApproverCoaInToAmount", user.getTxnApprovalCoaIncomeToAmount());
				 * } else {
				 * row.put("txnApproverCoaInToAmount", "");
				 * }
				 * 
				 * if (user.getTxnApprovalCoaExpense() != null) {
				 * row.put("txnApprovalCoaEx", user.getTxnApprovalCoaExpense());
				 * } else {
				 * row.put("txnApprovalCoaEx", "");
				 * }
				 * if (user.getTxnApprovalCoaExpenseFromAmount() != null) {
				 * row.put("txnApproverCoaExFromAmount",
				 * user.getTxnApprovalCoaExpenseFromAmount());
				 * } else {
				 * row.put("txnApproverCoaExFromAmount", "");
				 * }
				 * if (user.getTxnApprovalCoaExpenseToAmount() != null) {
				 * row.put("txnApproverCoaExToAmount", user.getTxnApprovalCoaExpenseToAmount());
				 * } else {
				 * row.put("txnApproverCoaExToAmount", "");
				 * }
				 * 
				 * if (user.getTxnApprovalCoaAsset() != null) {
				 * row.put("txnApprovalCoaAs", user.getTxnApprovalCoaAsset());
				 * } else {
				 * row.put("txnApprovalCoaAs", "");
				 * }
				 * if (user.getTxnApprovalCoaAssetFromAmount() != null) {
				 * row.put("txnApproverCoaAsFromAmount",
				 * user.getTxnApprovalCoaAssetFromAmount());
				 * } else {
				 * row.put("txnApproverCoaAsFromAmount", "");
				 * }
				 * if (user.getTxnApprovalCoaAssetToAmount() != null) {
				 * row.put("txnApproverCoaAsToAmount", user.getTxnApprovalCoaAssetToAmount());
				 * } else {
				 * row.put("txnApproverCoaAsToAmount", "");
				 * }
				 * 
				 * if (user.getTxnApprovalCoaLiabl() != null) {
				 * row.put("txnApprovalCoaLi", user.getTxnApprovalCoaLiabl());
				 * } else {
				 * row.put("txnApprovalCoaLi", "");
				 * }
				 * if (user.getTxnApprovalCoaLiablFromAmount() != null) {
				 * row.put("txnApproverCoaLiFromAmount",
				 * user.getTxnApprovalCoaLiablFromAmount());
				 * } else {
				 * row.put("txnApproverCoaLiFromAmount", "");
				 * }
				 * if (user.getTxnApprovalCoaLiablToAmount() != null) {
				 * row.put("txnApproverCoaLiToAmount", user.getTxnApprovalCoaLiablToAmount());
				 * } else {
				 * row.put("txnApproverCoaLiToAmount", "");
				 * }
				 */

				if (user.getTransactionAuditorBranches() != null) {
					row.put("txnAuditorInBranches", user.getTransactionAuditorBranches());
				} else {
					row.put("txnAuditorInBranches", "");
				}
				/*
				 * if (user.getTxnAuditorCoaIncome() != null) {
				 * row.put("txnAuditorCoaIn", user.getTxnAuditorCoaIncome());
				 * } else {
				 * row.put("txnAuditorCoaIn", "");
				 * }
				 * 
				 * if (user.getTxnAuditorCoaExpense() != null) {
				 * row.put("txnAuditorCoaEx", user.getTxnAuditorCoaExpense());
				 * } else {
				 * row.put("txnAuditorCoaEx", "");
				 * }
				 * if (user.getTxnAuditorCoaAsset() != null) {
				 * row.put("txnAuditorCoaAs", user.getTxnAuditorCoaAsset());
				 * } else {
				 * row.put("txnAuditorCoaAs", "");
				 * }
				 * if (user.getTxnAuditorCoaLiabl() != null) {
				 * row.put("txnAuditorCoaLi", user.getTxnAuditorCoaLiabl());
				 * } else {
				 * row.put("txnAuditorCoaLi", "");
				 * }
				 * if(user.getAllowedProcurementRequest()!=null){
				 * row.put("procurementRequest", user.getAllowedProcurementRequest());
				 * }else{
				 * row.put("procurementRequest", "0");
				 * }
				 */
				if (null != user.gettGroup()) {
					row.put("userTravelEligibility", user.gettGroup().getId());
				} else {
					row.put("userTravelEligibility", "");
				}
				if (null != user.getUsersTravelClaimTxnQuestions()
						&& !"".equals(user.getUsersTravelClaimTxnQuestions())) {
					row.put("userTravelTransactionPurpose", user.getUsersTravelClaimTxnQuestions());
				} else {
					row.put("userTravelTransactionPurpose", "");
				}
				if (null != user.geteGroup()) {
					row.put("userExpenseEligibility", user.geteGroup().getId());
				} else {
					row.put("userExpenseEligibility", "");
				}
				if (null != user.getUsersExpenseClaimTxnQuestions()
						&& !"".equals(user.getUsersExpenseClaimTxnQuestions())) {
					row.put("userExpenseTransactionPurpose", user.getUsersExpenseClaimTxnQuestions());
				} else {
					row.put("userExpenseTransactionPurpose", "");
				}
				row.put("customerCreator", user.canCreateCustomer());
				row.put("customerActivator", user.canActivateCustomer());
				row.put("vendorCreator", user.canCreateVendor());
				row.put("vendorActivator", user.canActivateVendor());
				String rightsStr = "";
				char[] rights = user.getModuleRights() == null ? null : user.getModuleRights().toCharArray();
				if (rights != null) {
					for (int i = 0; i < 14; i++) {
						rightsStr += rights[i] + ",";
					}
				}
				row.put("moduleRights", rightsStr);

				if (null == user.getEmergencyContactEmail()) {
					row.put("userEmergencyEmail", "");
				} else {
					row.put("userEmergencyEmail", user.getEmergencyContactEmail());
				}
				if (null == user.getEmergencyContactName()) {
					row.put("userEmergencyName", "");
				} else {
					row.put("userEmergencyName", user.getEmergencyContactName());
				}
				if (null == user.getEmergencyContactPhone()) {
					row.put("userEmergencyPhone", "");
				} else {
					row.put("userEmergencyPhone", user.getEmergencyContactPhone());
				}
				userdetailan.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, loggedUser.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, loggedUser.getEmail(), loggedUser.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End ");
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result configureNewAdmin(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users user = null;
		try {
			transaction.begin();
			Map<String, Object> criterias = new HashMap<String, Object>();
			JsonNode json = request.body().asJson();
			String fullName = json.findValue("newAdminFname").asText();
			String emailId = json.findValue("newAdminEmail").asText();
			String loggedUser = json.findValue("loggedUser").asText();
			session.adding("email", loggedUser);
			javax.mail.Session mailsession = emailsession;
			user = getUserInfo(request);
			if (null != user && null != user.getOrganization() && !emailId.equalsIgnoreCase(loggedUser)) {
				criterias.clear();
				criterias.put("email", emailId);
				criterias.put("presentStatus", 1);
				Users newUser = genericDAO.getByCriteria(Users.class, criterias, entityManager);
				if (null != newUser) {
					if (newUser.getOrganization().getId().equals(user.getOrganization().getId())) {
						results.put("result", true);
						Role role = Role.findById(1L);
						criterias.clear();
						criterias.put("user.email", loggedUser);
						criterias.put("organization.id", newUser.getOrganization().getId());
						criterias.put("role.id", role.getId());
						criterias.put("presentStatus", 1);
						UsersRoles userRoles = genericDAO.getByCriteria(UsersRoles.class, criterias, entityManager);
						if (null != userRoles) {
							userRoles.setUser(newUser);
						} else {
							userRoles = new UsersRoles();
							userRoles.setUser(newUser);
							userRoles.setRole(role);
							userRoles.setBranch(newUser.getBranch());
							userRoles.setOrganization(user.getOrganization());
						}
						genericDAO.saveOrUpdate(userRoles, user, entityManager);
						transaction.commit();
						results.put("message", "New Admin is assigned.");
						String body = roleChangeMail.render(emailId, role.getName(), ConfigParams.getInstance()).body();
						final String username = ConfigFactory.load().getString("smtp.user");
						String subject = "Role Changed";
						mailTimer(body, username, mailsession, emailId, null, subject);
					} else {
						results.put("result", false);
						results.put("message", "User already registered with other organization.");
					}
				} else {
					Role adminrole = Role.findById(1L);
					criterias.clear();
					criterias.put("user.id", user.getId());
					criterias.put("role.id", adminrole.getId());
					genericDAO.deleteByCriteria(UsersRoles.class, criterias, entityManager);
					String password = PasswordUtil.gen(10);
					Users newadminuser = new Users();
					newadminuser.setFullName(fullName);
					newadminuser.setEmail(emailId);
					newadminuser.setPassword(PasswordUtil.encrypt(password));
					newadminuser.setBranch(user.getBranch());
					newadminuser.setOrganization(user.getOrganization());
					genericDAO.saveOrUpdate(newadminuser, user, entityManager);
					UsersRoles newAdminRole = new UsersRoles();
					newAdminRole.setUser(newadminuser);
					newAdminRole.setRole(adminrole);
					newAdminRole.setBranch(user.getBranch());
					newAdminRole.setOrganization(user.getOrganization());
					genericDAO.saveOrUpdate(newAdminRole, user, entityManager);
					Organization orgn = user.getOrganization();
					orgn.setCorporateMail(emailId);
					genericDAO.saveOrUpdate(orgn, user, entityManager);
					transaction.commit();
					String body = userAccountCreation.render(emailId, password, ConfigParams.getInstance()).body();
					final String username = ConfigFactory.load().getString("smtp.user");
					String subject = "Successfully Created Users for Organization " + user.getOrganization().getName()
							+ "";
					mailTimer(body, username, mailsession, emailId, null, subject);
					results.put("result", true);
					results.put("message", "New Admin is assigned.");
				}
			} else {
				if (emailId.equalsIgnoreCase(loggedUser)) {
					results.put("result", false);
					results.put("message", "User specified is already a admin.");
				}
			}
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result deactivateUser(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users user = getUserInfo(request);
		try {

			JsonNode json = request.body().asJson();
			Long elementId = json.findValue("entityPrimaryId").asLong();
			Users usr = Users.findById(elementId);
			usr.setPresentStatus(0);
			usercrud.save(user, usr, entityManager);
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("organization.id", usr.getOrganization().getId());
			criterias.put("role.id", 1L);
			criterias.put("presentStatus", 1);
			UsersRoles usrRole = genericDAO.getByCriteria(UsersRoles.class, criterias, entityManager);
			Users masterAdminForOrganization = usrRole.getUser();
			transaction.begin();
			StringBuilder sbr = new StringBuilder("select obj from Branch obj where obj.organization=")
					.append(usr.getOrganization().getId())
					.append(" and obj.alertForAction='").append(usr.getEmail()).append("' and obj.presentStatus=1");
			List<Branch> deactiveUsersAlertsForActionBranch = genericDAO.executeSimpleQuery(sbr.toString(),
					entityManager);
			for (Branch bnch : deactiveUsersAlertsForActionBranch) {
				bnch.setAlertForAction(masterAdminForOrganization.getEmail());
				genericDAO.saveOrUpdate(bnch, usr, entityManager);
			}
			sbr.delete(0, sbr.length());
			sbr.append("select obj from Branch obj where obj.organization='" + usr.getOrganization().getId()
					+ "' and obj.alertForInformation='" + usr.getEmail() + "' and obj.presentStatus=1");
			List<Branch> deactiveUsersAlertsForInformationBranch = genericDAO.executeSimpleQuery(sbr.toString(),
					entityManager);
			for (Branch bnch : deactiveUsersAlertsForActionBranch) {
				bnch.setAlertForInformation(masterAdminForOrganization.getEmail());
				genericDAO.saveOrUpdate(bnch, usr, entityManager);
			}
			OrganizationKeyOfficials orgnKeyOff = usr.getBranchKeyOff();
			if (orgnKeyOff != null) {
				orgnKeyOff.setPresentStatus(0);
				genericDAO.saveOrUpdate(orgnKeyOff, usr, entityManager);
			}
			sbr.delete(0, sbr.length());
			sbr.append("select obj from StatutoryDetails obj where obj.organization='" + usr.getOrganization().getId()
					+ "' and obj.alertForAction='" + usr.getEmail() + "' and obj.presentStatus=1");
			List<StatutoryDetails> deactiveUsersAlertsForActionBranchStat = genericDAO
					.executeSimpleQuery(sbr.toString(), entityManager);
			for (StatutoryDetails stat : deactiveUsersAlertsForActionBranchStat) {
				stat.setAlertForAction(masterAdminForOrganization.getEmail());
				genericDAO.saveOrUpdate(stat, usr, entityManager);
			}
			sbr.delete(0, sbr.length());
			sbr.append("select obj from StatutoryDetails obj where obj.organization='" + usr.getOrganization().getId()
					+ "' and obj.alertForInformation='" + usr.getEmail() + "' and obj.presentStatus=1");
			List<StatutoryDetails> deactiveUsersAlertsForInformationStat = genericDAO.executeSimpleQuery(sbr.toString(),
					entityManager);
			for (StatutoryDetails stat : deactiveUsersAlertsForInformationStat) {
				stat.setAlertForInformation(masterAdminForOrganization.getEmail());
				genericDAO.saveOrUpdate(stat, usr, entityManager);
			}
			sbr.delete(0, sbr.length());
			sbr.append("select obj from OrganizationOperationalRemainders obj where obj.organization='"
					+ usr.getOrganization().getId() + "' and obj.alertForAction='" + usr.getEmail()
					+ "' and obj.presentStatus=1");
			List<OrganizationOperationalRemainders> deactiveUsersAlertsForActionBranchOper = genericDAO
					.executeSimpleQuery(sbr.toString(), entityManager);
			for (OrganizationOperationalRemainders stat : deactiveUsersAlertsForActionBranchOper) {
				stat.setAlertForAction(masterAdminForOrganization.getEmail());
				genericDAO.saveOrUpdate(stat, usr, entityManager);
			}
			sbr.delete(0, sbr.length());
			sbr.append("select obj from OrganizationOperationalRemainders obj where obj.organization='"
					+ usr.getOrganization().getId() + "' and obj.alertForInformation='" + usr.getEmail()
					+ "' and obj.presentStatus=1");
			List<OrganizationOperationalRemainders> deactiveUsersAlertsForInformationOper = genericDAO
					.executeSimpleQuery(sbr.toString(), entityManager);
			for (OrganizationOperationalRemainders stat : deactiveUsersAlertsForInformationOper) {
				stat.setAlertForInformation(masterAdminForOrganization.getEmail());
				genericDAO.saveOrUpdate(stat, usr, entityManager);
			}
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result listAlertUser(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Users user = null;
		try {
			ArrayNode an = results.putArray("alertUser");
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			String branchPrimKey = json.findValue("branchPrimKey").asText();
			StringBuilder sbr = new StringBuilder("");
			if (!branchPrimKey.equals("") && branchPrimKey != null) {
				Branch bnch = Branch.findById(IdosUtil.convertStringToLong(branchPrimKey));
				if (bnch.getIsHeadQuarter() == 0) {
					Map<Object, Object> criteria = new HashMap<Object, Object>();
					criteria.clear();
					criteria.put("isHeadQuarter", 1);
					criteria.put("organization.id", bnch.getOrganization().getId());
					criteria.put("presentStatus", 1);
					Branch hqbnch = genericDAO.getByCriteria(Branch.class, criteria, entityManager);
					sbr.append("select obj from OrganizationKeyOfficials obj where obj.presentStatus=1 and obj.branch='"
							+ hqbnch.getId() + "' and obj.organization='" + hqbnch.getOrganization().getId()
							+ "' and obj.presentStatus=1");
					List<OrganizationKeyOfficials> hqbranchOfficers = genericDAO.executeSimpleQuery(sbr.toString(),
							entityManager);
					if (hqbranchOfficers.size() > 0) {
						for (OrganizationKeyOfficials bnchOfficers : hqbranchOfficers) {
							ObjectNode row = Json.newObject();
							row.put("id", bnchOfficers.getId());
							row.put("email", bnchOfficers.getEmail());
							row.put("name", bnchOfficers.getName());
							an.add(row);
						}
					}
				}
				sbr.delete(0, sbr.length());
				sbr.append("select obj from OrganizationKeyOfficials obj where obj.presentStatus=1 and obj.branch='"
						+ bnch.getId() + "' and obj.organization='" + bnch.getOrganization().getId()
						+ "' and obj.presentStatus=1");
				List<OrganizationKeyOfficials> branchOfficers = genericDAO.executeSimpleQuery(sbr.toString(),
						entityManager);
				if (branchOfficers.size() > 0) {
					for (OrganizationKeyOfficials bnchOfficers : branchOfficers) {
						ObjectNode row = Json.newObject();
						row.put("id", bnchOfficers.getId());
						row.put("email", bnchOfficers.getEmail());
						row.put("name", bnchOfficers.getName());
						an.add(row);
					}
				}
			}
			if (branchPrimKey.equals("")) {
				Map<Object, Object> criteria = new HashMap<Object, Object>();
				criteria.clear();
				criteria.put("isHeadQuarter", 1);
				criteria.put("organization.id", user.getOrganization().getId());
				criteria.put("presentStatus", 1);
				Branch hqbnch = genericDAO.getByCriteria(Branch.class, criteria, entityManager);
				sbr.delete(0, sbr.length());
				sbr.append("select obj from OrganizationKeyOfficials obj where obj.presentStatus=1 and obj.branch='"
						+ hqbnch.getId() + "' and obj.organization='" + hqbnch.getOrganization().getId()
						+ "' and obj.presentStatus=1");
				List<OrganizationKeyOfficials> hqbranchOfficers = genericDAO.executeSimpleQuery(sbr.toString(),
						entityManager);
				if (hqbranchOfficers.size() > 0) {
					for (OrganizationKeyOfficials bnchOfficers : hqbranchOfficers) {
						ObjectNode row = Json.newObject();
						row.put("id", bnchOfficers.getId());
						row.put("email", bnchOfficers.getEmail());
						row.put("name", bnchOfficers.getName());
						an.add(row);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result listHqAlertUser(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		ObjectNode results = Json.newObject();
		Users user = null;
		try {
			ArrayNode an = results.putArray("alertHqUser");
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			user = getUserInfo(request);
			Map<Object, Object> criteria = new HashMap<Object, Object>();
			criteria.clear();
			criteria.put("isHeadQuarter", 1);
			criteria.put("organization.id", user.getOrganization().getId());
			criteria.put("presentStatus", 1);
			Branch hqbnch = genericDAO.getByCriteria(Branch.class, criteria, entityManager);
			List<OrganizationKeyOfficials> hqbranchOfficers = hqbnch.getBranchOfficers();
			if (hqbranchOfficers.size() > 0) {
				for (OrganizationKeyOfficials bnchOfficers : hqbranchOfficers) {
					ObjectNode row = Json.newObject();
					row.put("id", bnchOfficers.getId());
					row.put("email", bnchOfficers.getEmail());
					row.put("name", bnchOfficers.getName());
					an.add(row);
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getAllUsers(Request request) {
		log.log(Level.FINE, ">>>> Start in getAllUser....");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = getUserInfo(request);
		if (user == null) {
			return unauthorized();
		}
		ArrayNode userlistan = result.putArray("userListData");
		ArrayNode extuserlistan = result.putArray("extUserListData");
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		try {
			List<Users> userList = Users.findAllActByOrg(entityManager, user.getOrganization().getId());
			if (userList.size() > 0) {
				for (Users userTableList : userList) {
					if (userTableList.getPresentStatus() == 1) {
						String userRoles = getUserRoles(userTableList);
						if (userRoles != null && !userRoles.equals("")) {
							userRoles = userRoles.substring(0, userRoles.length() - 1);
						}
						ObjectNode row = Json.newObject();
						row.put("id", userTableList.getId());
						row.put("fullName", userTableList.getFullName());
						row.put("userEmail", userTableList.getEmail());
						row.put("userBranch", userTableList.getBranch().getName());
						row.put("userRole", userRoles);
						row.put("isActive", false);
						userlistan.add(row);
					}
				}
			}
			if (!ConfigParams.getInstance().getCompanyOwner().equals("PWC")) {
				log.log(Level.FINE, "inside karvy or idos...");
				List<Users> extUserList = Users.findAllExtUserActByOrg(entityManager, user.getOrganization().getId());
				if (extUserList.size() > 0) {
					for (Users extUserTableList : extUserList) {
						if (extUserTableList.getPresentStatus() == 1) {
							String userRoles = "EXTERNAL USER," + getUserRoles(extUserTableList);
							if (userRoles != null && !userRoles.equals("")) {
								userRoles = userRoles.substring(0, userRoles.length() - 1);
							}
							ObjectNode row = Json.newObject();
							row.put("id", extUserTableList.getId());
							row.put("fullName", extUserTableList.getFullName());
							row.put("userEmail", extUserTableList.getEmail());
							row.put("userBranch", extUserTableList.getBranch().getName());
							row.put("userRole", userRoles);
							row.put("isActive", false);
							extuserlistan.add(row);
						}
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result setTxnCoaRule(Request request) {
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		ArrayNode an = results.putArray("userListData");
		Map<Object, Object> criteria = new HashMap<Object, Object>();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users usr = null;
		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			// JsonNode json = null;
			// String rawBody = null;
			try {
				/*
				 * rawBody = new String(request.body().asRaw().asBytes(), "UTF-8");
				 * json = objectMapper.readValue(rawBody, new TypeReference<JsonNode>(){
				 * });
				 */
				JsonNode json = request.body().asJson();
				log.log(Level.FINE, ">>>> Start " + json);
				String email = json.findValue("userEmail").asText().toLowerCase();
				String coaRule = json.findValue("rightForCOA").asText();
				String coaAmountLimit = json.findValue("coaAmountLimit").asText();
				StringTokenizer stzr = new StringTokenizer(coaAmountLimit, ",");
				String coaAmountLimitArr = null;
				StringBuilder fromAmount = new StringBuilder();
				StringBuilder toAmount = new StringBuilder();
				while (stzr.hasMoreElements()) {
					String amount = stzr.nextToken();
					StringTokenizer st = new StringTokenizer(amount, "-");
					while (st.hasMoreElements()) {
						fromAmount.append(st.nextToken()).append(",");
						toAmount.append(st.nextToken()).append(",");
					}
				}
				if (fromAmount.length() > 1) {
					fromAmount.substring(0, fromAmount.length() - 1);
				}
				if (toAmount.length() > 1) {
					toAmount.substring(0, toAmount.length() - 1);
				}
				int headType = json.findValue("headType").asInt();
				int ruleType = json.findValue("ruleType").asInt();
				UserTxnRuleSetupCache.putCOA(email, coaRule, fromAmount.toString(), toAmount.toString(), ruleType,
						headType);
			} catch (Exception e) {
				log.log(Level.SEVERE, "err", e);
				// throw new RuntimeException("Bad request is received: " + rawBody);
			}
			log.log(Level.FINE, ">>>> End ");
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	@BodyParser.Of(BodyParser.Raw.class)
	public Result createUser(Request request) {
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		ArrayNode an = results.putArray("userListData");
		Map<Object, Object> criteria = new HashMap<Object, Object>();
		String ipAddress = request.getHeaders().get("X-FORWARDED-FOR").orElse(null);
		Users usr = getUserInfo(request);
		if (usr == null) {
			return unauthorized();
		}

		if (ipAddress == null) {
			ipAddress = request.remoteAddress();
		}
		try {
			JsonNode json = null;
			String rawBody = null;
			String byteString = null;
			try {
				rawBody = new String(request.body().asBytes().toArray());
				json = objectMapper.readValue(rawBody, new TypeReference<JsonNode>() {
				});
			} catch (Exception e) {
				log.log(Level.SEVERE, "err", e);
				throw new RuntimeException("Body was not a json: " + rawBody);
			}
			log.log(Level.FINE, ">>>> Start ");
			transaction.begin();
			Users newUser = null;
			String password = null;
			String userPrimKey = json.findValue("userHiddenPrimKey").asText();
			String userbranch = json.findValue("branch").asText();
			Branch newUserBnch = Branch.findById(Long.parseLong(userbranch));
			boolean isNewUser = false;
			if (!userPrimKey.equals("") && userPrimKey != null) {
				newUser = Users.findById(IdosUtil.convertStringToLong(userPrimKey));
				List<UsersRoles> userRoles = newUser.getUserRoles();
				Map<String, Object> criterias = new HashMap<String, Object>();
				for (UsersRoles usrRole : userRoles) {
					if (usrRole.getRole().getId() == 1L) {
						criterias.clear();
						criterias.put("organization.id", newUser.getOrganization().getId());
						criterias.put("isHeadQuarter", 1);
						criterias.put("presentStatus", 1);
						newUserBnch = genericDAO.getByCriteria(Branch.class, criterias, entityManager);
					}
					if (usrRole.getRole().getId() == 8L) {
						if (newUser.getBranchSafeDepositBox() != null) {
							newUserBnch = newUser.getBranchSafeDepositBox().getBranch();
						}
					}
					if (usrRole.getRole().getId() == 9L) {
						if (newUser.getBranchKeyOff() != null) {
							newUserBnch = newUser.getBranchKeyOff().getBranch();
						}
					}
				}
			} else {
				isNewUser = true;
				newUser = new Users();
				password = PasswordUtil.gen(10);
				newUser.setPassword(PasswordUtil.encrypt(password));
				newUser.setAllowedProcurementRequest(0);
			}

			ObjectNode newUserObjNode = Json.newObject();
			String userName = json.findValue("userName").asText(); // save in full name
			String email = json.findValue("userEmail").asText().toLowerCase();
			String userPhoneNumberCountryCode = json.findValue("userctryCodeText").asText();
			String userPhoneNumber = json.findValue("userNumber").asText();
			String userAddress = json.findValue("userAddress") == null ? null : json.findValue("userAddress").asText();
			String dateOfBirth = json.findValue("dateOfBirth") == null ? null : json.findValue("dateOfBirth").asText();
			String bloodGroup = json.findValue("bloodGroup") == null ? null : json.findValue("bloodGroup").asText();
			String idProof = json.findValue("idProof") == null ? null : json.findValue("idProof").asText();
			int userSetupIsSearchClicked = json.findValue("idProof") == null ? 0 : json.findValue("idProof").asInt();
			// String allowedProcRequest=json.findValue("allowedProcReq").asText();
			// String status = json.findValue("status").asText();
			// String ethnicity = json.findValue("ethnicity").asText();
			String hireDate = json.findValue("hireDate").asText();
			String confirmDate = json.findValue("confirmDate").asText();
			String noticeDate = json.findValue("noticeDate").asText();
			String releaseDate = json.findValue("releaseDate").asText();
			long manager = json.findValue("manager").asLong();
			long hrManager = json.findValue("hrManager").asLong();
			String empType = json.findValue("empType").asText();
			String source = json.findValue("source").asText();
			String pan = json.findValue("pan").asText();
			String passport = json.findValue("passport").asText();
			String designation = json.findValue("designation").asText();
			String department = json.findValue("department").asText();
			String userEmergencyName = json.findValue("userEmergencyName").asText();
			String userEmergencyEmail = json.findValue("userEmergencyEmail").asText();
			String userEmergencyPhone = json.findValue("userEmergencyPhone").asText();

			boolean creatorChange = json.findValue("CREATOR_CHANGE").asBoolean();
			boolean approverChange = json.findValue("APPROVER_CHANGE").asBoolean();
			boolean auditorChange = json.findValue("AUDITOR_CHANGE").asBoolean();

			boolean creatorIncomeChange = json.findValue("CREATOR_INCOME_CHANGE").asBoolean();
			boolean creatorExpenseChange = json.findValue("CREATOR_EXPENSE_CHANGE").asBoolean();
			boolean creatorAssetsChange = json.findValue("CREATOR_ASSETS_CHANGE").asBoolean();
			boolean creatorLiabilitiesChange = json.findValue("CREATOR_LIABILITIES_CHANGE").asBoolean();

			boolean approverIncomeChange = json.findValue("APPROVER_INCOME_CHANGE").asBoolean();
			boolean approverExpenseChange = json.findValue("APPROVER_EXPENSE_CHANGE").asBoolean();
			boolean approverAssetsChange = json.findValue("APPROVER_ASSETS_CHANGE").asBoolean();
			boolean approverLiabilitiesChange = json.findValue("APPROVER_LIABILITIES_CHANGE").asBoolean();

			boolean auditorIncomeChange = json.findValue("AUDITOR_INCOME_CHANGE").asBoolean();
			boolean auditorExpenseChange = json.findValue("AUDITOR_EXPENSE_CHANGE").asBoolean();
			boolean auditorAssetsChange = json.findValue("AUDITOR_ASSETS_CHANGE").asBoolean();
			boolean auditorLiabilitiesChange = json.findValue("AUDITOR_LIABILITIES_CHANGE").asBoolean();

			Date dob = null, hire = null, confirm = null, notice = null, release = null;
			newUser.setFullName(userName);
			newUserObjNode.put("fullName", userName);
			newUser.setEmail(email);
			newUserObjNode.put("userEmail", email);
			newUserObjNode.put("password", password);
			newUser.setPhoneNumberCountryCode(userPhoneNumberCountryCode);
			newUserObjNode.put("userPhoneNumberCountryCode", userPhoneNumberCountryCode);
			newUser.setMobile(userPhoneNumber);
			newUserObjNode.put("userPhoneNumber", userPhoneNumber);
			newUser.setAddress(userAddress);
			newUserObjNode.put("userAddress", userAddress);
			// newUser.setAllowedProcurementRequest(Integer.parseInt(allowedProcRequest));
			if (dateOfBirth != null && !dateOfBirth.equals("")) {
				dob = mysqldf.parse(mysqldf.format(idosdf.parse(dateOfBirth)));
			}
			newUser.setDob(dob);
			newUserObjNode.put("userAddress", userAddress);
			newUser.setBloodGroup(bloodGroup);
			newUserObjNode.put("bloodGroup", bloodGroup);
			newUser.setIdproof(idProof);
			newUserObjNode.put("idProof", idProof);
			newUser.setOrganization(usr.getOrganization());
			newUserObjNode.put("userOrganization", usr.getOrganization().getName());
			newUser.setBranch(newUserBnch);
			newUserObjNode.put("userBranch", newUserBnch.getName());
			// newUser.setStatus(status);
			// newUser.setEthnicity(ethnicity);
			newUser.setEmergencyContactName(userEmergencyName);
			newUser.setEmergencyContactEmail(userEmergencyEmail);
			newUser.setEmergencyContactPhone(userEmergencyPhone);
			if (null != hireDate && !"".equals(hireDate)) {
				hire = mysqldf.parse(mysqldf.format(idosdf.parse(hireDate)));
			}
			if (null != confirmDate && !"".equals(confirmDate)) {
				confirm = mysqldf.parse(mysqldf.format(idosdf.parse(confirmDate)));
			}
			if (null != noticeDate && !"".equals(noticeDate)) {
				notice = mysqldf.parse(mysqldf.format(idosdf.parse(noticeDate)));
			}
			if (null != releaseDate && !"".equals(releaseDate)) {
				release = mysqldf.parse(mysqldf.format(idosdf.parse(releaseDate)));
			}
			Users mgr = null, hrMgr = null;
			if (manager > 0) {
				mgr = Users.findById(manager);
			}
			if (hrManager > 0) {
				hrMgr = Users.findById(hrManager);
			}
			newUser.setHrManager(hrMgr);
			newUser.setSupervisorUser(mgr);
			newUser.setDateOfConfirmation(confirm);
			newUser.setDateOfHire(hire);
			newUser.setDateOfRelease(release);
			newUser.setNoticeStartDate(notice);
			newUser.setSource(source);
			newUser.setPanNumber(pan);
			newUser.setEmploymentType(empType);
			newUser.setPassportNumber(passport);
			newUser.setDesignation(designation);
			newUser.setDepartment(department);
			auditDAO.saveAuditLogs("added/updated new user for the organization", usr, newUser.getId(), Users.class,
					ipAddress, newUserObjNode.toString(), entityManager);
			// usercreationrights for selected branches
			String usercreationrightinbranches = json.findValue("creationrightsInBranches") != null
					? json.findValue("creationrightsInBranches").asText()
					: null;
			newUser.setTransactionCreationBranches(usercreationrightinbranches);
			// user creation rights for selected projects
			String usercreationrightinprojects = json.findValue("creationrightsInProjects") != null
					? json.findValue("creationrightsInProjects").asText()
					: null;
			newUser.setTransactionCreationProjects(usercreationrightinprojects);
			// user creation right for chart of account
			String usercreationrightforcoa = null;
			String usercreationrightforcoaamountlimit = null;
			String usercreationrightforcoaamountcriteria = null;
			if (creatorChange) {
				if (creatorIncomeChange) {
					String creatCoa1 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.INCOME_HEAD);
					String creatFrom1 = UserTxnRuleSetupCache.getFromAmount(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.INCOME_HEAD);
					String creatTo1 = UserTxnRuleSetupCache.getToAmount(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.INCOME_HEAD);
					if ("allcoaitems0".equals(creatCoa1)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder(creatFrom1);
						StringBuilder toAmountSb = new StringBuilder(creatTo1);
						creatCoa1 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.INCOME_HEAD);
						creatFrom1 = fromAmountsb.toString();
						creatTo1 = toAmountSb.toString();
						UserTxnRuleSetupCache.putCOA(email, creatCoa1, creatFrom1, creatTo1,
								UserTxnRuleSetupCache.CREATOR, UserTxnRuleSetupCache.INCOME_HEAD);
					}

					if (userSetupIsSearchClicked == 1 && !isNewUser) {
						String currentCreateCoaIncome = newUser.getTxnCreationCoaIncome() == null ? ""
								: newUser.getTxnCreationCoaIncome();
						String currentCreateFromAmount = newUser.getTxnCreationCoaIncomeFromAmount() == null ? ""
								: newUser.getTxnCreationCoaIncomeFromAmount();
						String currentCreateToAmount = newUser.getTxnCreationCoaIncomeToAmount() == null ? ""
								: newUser.getTxnCreationCoaIncomeToAmount();
						String currentCreateCoaIncomeArr[] = currentCreateCoaIncome.split(",");
						String currentCreateFromAmountArr[] = currentCreateFromAmount.split(",");
						String currentCreateToAmountArr[] = currentCreateToAmount.split(",");
						String creatCoa1Arr[] = creatCoa1.split(",");
						for (int i = 0; i < currentCreateCoaIncomeArr.length; i++) {
							for (int j = 0; j < creatCoa1Arr.length; j++) {
								if (currentCreateCoaIncomeArr[i].equalsIgnoreCase(creatCoa1Arr[j])) {
									currentCreateCoaIncomeArr[i] = currentCreateCoaIncomeArr[i + 1];
									currentCreateFromAmountArr[i] = currentCreateFromAmountArr[i + 1];
									currentCreateToAmountArr[i] = currentCreateToAmountArr[i + 1];
								}
							}
						}
					}
					if (!isNewUser && creatCoa1 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa1, creatFrom1, creatTo1, entityManager,
								newUser, UserTxnRuleSetupCache.INCOME_HEAD, IdosConstants.CREATOR_RIGHTS, isNewUser);
					}
					newUser.setTxnCreationCoaIncome(creatCoa1);
					newUser.setTxnCreationCoaIncomeFromAmount(creatFrom1);
					newUser.setTxnCreationCoaIncomeToAmount(creatTo1);

				}
				if (creatorExpenseChange) {
					String creatCoa2 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.EXPENSE_HEAD);
					String creatFrom2 = UserTxnRuleSetupCache.getFromAmount(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.EXPENSE_HEAD);
					String creatTo2 = UserTxnRuleSetupCache.getToAmount(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.EXPENSE_HEAD);
					if ("allcoaitems0".equals(creatCoa2)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder(creatFrom2);
						StringBuilder toAmountSb = new StringBuilder(creatTo2);
						creatCoa2 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.EXPENSE_HEAD);
						creatFrom2 = fromAmountsb.toString();
						creatTo2 = toAmountSb.toString();
						UserTxnRuleSetupCache.putCOA(email, creatCoa2, creatFrom2, creatTo2,
								UserTxnRuleSetupCache.CREATOR, UserTxnRuleSetupCache.EXPENSE_HEAD);
					}
					if (!isNewUser && creatCoa2 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa2, creatFrom2, creatTo2, entityManager,
								newUser, UserTxnRuleSetupCache.EXPENSE_HEAD, IdosConstants.CREATOR_RIGHTS, isNewUser);
					}
					newUser.setTxnCreationCoaExpense(creatCoa2);
					newUser.setTxnCreationCoaExpenseFromAmount(creatFrom2);
					newUser.setTxnCreationCoaExpenseToAmount(creatTo2);
				}
				if (creatorAssetsChange) {
					String creatCoa3 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.ASSETS_HEAD);
					String creatFrom3 = UserTxnRuleSetupCache.getFromAmount(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.ASSETS_HEAD);
					String creatTo3 = UserTxnRuleSetupCache.getToAmount(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.ASSETS_HEAD);
					if ("allcoaitems0".equals(creatCoa3)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder(creatFrom3);
						StringBuilder toAmountSb = new StringBuilder(creatTo3);
						creatCoa3 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.ASSETS_HEAD);
						creatFrom3 = fromAmountsb.toString();
						creatTo3 = toAmountSb.toString();
						UserTxnRuleSetupCache.putCOA(email, creatCoa3, creatFrom3, creatTo3,
								UserTxnRuleSetupCache.CREATOR, UserTxnRuleSetupCache.ASSETS_HEAD);
					}
					if (!isNewUser && creatCoa3 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa3, creatFrom3, creatTo3, entityManager,
								newUser, UserTxnRuleSetupCache.ASSETS_HEAD, IdosConstants.CREATOR_RIGHTS, isNewUser);
					}
					newUser.setTxnCreationCoaAsset(creatCoa3);
					newUser.setTxnCreationCoaAssetFromAmount(creatFrom3);
					newUser.setTxnCreationCoaAssetToAmount(creatTo3);
				}
				if (creatorLiabilitiesChange) {
					String creatCoa4 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.LIABILITES_HEAD);
					String creatFrom4 = UserTxnRuleSetupCache.getFromAmount(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.LIABILITES_HEAD);
					String creatTo4 = UserTxnRuleSetupCache.getToAmount(email, UserTxnRuleSetupCache.CREATOR,
							UserTxnRuleSetupCache.LIABILITES_HEAD);
					if ("allcoaitems0".equals(creatCoa4)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder(creatFrom4);
						StringBuilder toAmountSb = new StringBuilder(creatTo4);
						creatCoa4 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.LIABILITES_HEAD);
						creatFrom4 = fromAmountsb.toString();
						creatTo4 = toAmountSb.toString();
						UserTxnRuleSetupCache.putCOA(email, creatCoa4, creatFrom4, creatTo4,
								UserTxnRuleSetupCache.CREATOR, UserTxnRuleSetupCache.LIABILITES_HEAD);
					}
					if (!isNewUser && creatCoa4 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa4, creatFrom4, creatTo4, entityManager,
								newUser, UserTxnRuleSetupCache.LIABILITES_HEAD, IdosConstants.CREATOR_RIGHTS,
								isNewUser);
					}
					newUser.setTxnCreationCoaLiabl(creatCoa4);
					newUser.setTxnCreationCoaLiablFromAmount(creatFrom4);
					newUser.setTxnCreationCoaLiablToAmount(creatTo4);
				}
				usercreationrightforcoa = UserTxnRuleSetupCache.getCOAList(email, UserTxnRuleSetupCache.CREATOR);
				usercreationrightforcoaamountlimit = UserTxnRuleSetupCache.getFromAmountList(email,
						UserTxnRuleSetupCache.CREATOR);
				usercreationrightforcoaamountcriteria = UserTxnRuleSetupCache.getToAmountList(email,
						UserTxnRuleSetupCache.CREATOR);
			}
			// userapprovalrights for selected branches
			String userapprovalrightinbranches = json.findValue("approvalrightsInBranches") != null
					? json.findValue("approvalrightsInBranches").asText()
					: null;
			newUser.setTransactionApprovalBranches(userapprovalrightinbranches);
			// user approval rights for selected projects
			String userapprovalrightinprojects = json.findValue("approvalrightsInProjects") != null
					? json.findValue("approvalrightsInProjects").asText()
					: null;
			newUser.setTransactionApproverProject(userapprovalrightinprojects);
			// user approval right for chart of account
			String userapprovalrightforcoa = null;
			String userapprovalrightforcoaamountlimit = null;
			String userapprovalrightforcoaamountcriteria = null;
			if (approverChange) {
				if (approverIncomeChange) {
					String creatCoa1 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.INCOME_HEAD);
					String creatFrom1 = UserTxnRuleSetupCache.getFromAmount(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.INCOME_HEAD);
					String creatTo1 = UserTxnRuleSetupCache.getToAmount(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.INCOME_HEAD);
					if ("allcoaitems0".equals(creatCoa1)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder(creatFrom1);
						StringBuilder toAmountSb = new StringBuilder(creatTo1);
						creatCoa1 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.INCOME_HEAD);
						creatFrom1 = fromAmountsb.toString();
						creatTo1 = toAmountSb.toString();
						UserTxnRuleSetupCache.putCOA(email, creatCoa1, creatFrom1, creatTo1,
								UserTxnRuleSetupCache.APPROVER, UserTxnRuleSetupCache.INCOME_HEAD);
					}
					if (!isNewUser && creatCoa1 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa1, creatFrom1, creatTo1, entityManager,
								newUser, UserTxnRuleSetupCache.INCOME_HEAD, IdosConstants.APPROVER_RIGHTS, isNewUser);
					}
					newUser.setTxnApprovalCoaIncome(creatCoa1);
					newUser.setTxnApprovalCoaIncomeFromAmount(creatFrom1);
					newUser.setTxnApprovalCoaIncomeToAmount(creatTo1);
				}
				if (approverExpenseChange) {
					String creatCoa2 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.EXPENSE_HEAD);
					String creatFrom2 = UserTxnRuleSetupCache.getFromAmount(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.EXPENSE_HEAD);
					String creatTo2 = UserTxnRuleSetupCache.getToAmount(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.EXPENSE_HEAD);
					if ("allcoaitems0".equals(creatCoa2)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder(creatFrom2);
						StringBuilder toAmountSb = new StringBuilder(creatTo2);
						creatCoa2 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.EXPENSE_HEAD);
						creatFrom2 = fromAmountsb.toString();
						creatTo2 = toAmountSb.toString();
						UserTxnRuleSetupCache.putCOA(email, creatCoa2, creatFrom2, creatTo2,
								UserTxnRuleSetupCache.APPROVER, UserTxnRuleSetupCache.EXPENSE_HEAD);
					}
					if (!isNewUser && creatCoa2 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa2, creatFrom2, creatTo2, entityManager,
								newUser, UserTxnRuleSetupCache.EXPENSE_HEAD, IdosConstants.APPROVER_RIGHTS, isNewUser);
					}
					newUser.setTxnApprovalCoaExpense(creatCoa2);
					newUser.setTxnApprovalCoaExpenseFromAmount(creatFrom2);
					newUser.setTxnApprovalCoaExpenseToAmount(creatTo2);
				}
				if (approverAssetsChange) {
					String creatCoa3 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.ASSETS_HEAD);
					String creatFrom3 = UserTxnRuleSetupCache.getFromAmount(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.ASSETS_HEAD);
					String creatTo3 = UserTxnRuleSetupCache.getToAmount(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.ASSETS_HEAD);
					if ("allcoaitems0".equals(creatCoa3)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder(creatFrom3);
						StringBuilder toAmountSb = new StringBuilder(creatTo3);
						creatCoa3 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.ASSETS_HEAD);
						creatFrom3 = fromAmountsb.toString();
						creatTo3 = toAmountSb.toString();
						UserTxnRuleSetupCache.putCOA(email, creatCoa3, creatFrom3, creatTo3,
								UserTxnRuleSetupCache.APPROVER, UserTxnRuleSetupCache.ASSETS_HEAD);
					}
					if (!isNewUser && creatCoa3 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa3, creatFrom3, creatTo3, entityManager,
								newUser, UserTxnRuleSetupCache.ASSETS_HEAD, IdosConstants.APPROVER_RIGHTS, isNewUser);
					}
					newUser.setTxnApprovalCoaAsset(creatCoa3);
					newUser.setTxnApprovalCoaAssetFromAmount(creatFrom3);
					newUser.setTxnApprovalCoaAssetToAmount(creatTo3);
				}
				if (approverLiabilitiesChange) {
					String creatCoa4 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.LIABILITES_HEAD);
					String creatFrom4 = UserTxnRuleSetupCache.getFromAmount(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.LIABILITES_HEAD);
					String creatTo4 = UserTxnRuleSetupCache.getToAmount(email, UserTxnRuleSetupCache.APPROVER,
							UserTxnRuleSetupCache.LIABILITES_HEAD);
					if ("allcoaitems0".equals(creatCoa4)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder(creatFrom4);
						StringBuilder toAmountSb = new StringBuilder(creatTo4);
						creatCoa4 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.LIABILITES_HEAD);
						creatFrom4 = fromAmountsb.toString();
						creatTo4 = toAmountSb.toString();
						UserTxnRuleSetupCache.putCOA(email, creatCoa4, creatFrom4, creatTo4,
								UserTxnRuleSetupCache.APPROVER, UserTxnRuleSetupCache.LIABILITES_HEAD);
					}
					if (!isNewUser && creatCoa4 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa4, creatFrom4, creatTo4, entityManager,
								newUser, UserTxnRuleSetupCache.LIABILITES_HEAD, IdosConstants.APPROVER_RIGHTS,
								isNewUser);
					}
					newUser.setTxnApprovalCoaLiabl(creatCoa4);
					newUser.setTxnApprovalCoaLiablFromAmount(creatFrom4);
					newUser.setTxnApprovalCoaLiablToAmount(creatTo4);
				}
				userapprovalrightforcoa = UserTxnRuleSetupCache.getCOAList(email, UserTxnRuleSetupCache.APPROVER);
				userapprovalrightforcoaamountlimit = UserTxnRuleSetupCache.getFromAmountList(email,
						UserTxnRuleSetupCache.APPROVER);
				userapprovalrightforcoaamountcriteria = UserTxnRuleSetupCache.getToAmountList(email,
						UserTxnRuleSetupCache.APPROVER);
			}

			// user auditor rights for selected branches
			String userauditorrightinbranches = json.findValue("auditorrightsInBranches") != null
					? json.findValue("auditorrightsInBranches").asText()
					: null;
			newUser.setTransactionAuditorBranches(userauditorrightinbranches);
			// user auditor right for chart of account
			String userauditorrightforcoa = null;
			if (auditorChange) {
				if (auditorIncomeChange) {
					String creatCoa1 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.AUDITOR,
							UserTxnRuleSetupCache.INCOME_HEAD);
					if ("allcoaitems0".equals(creatCoa1)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder("");
						StringBuilder toAmountSb = new StringBuilder("");
						creatCoa1 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.INCOME_HEAD);
						UserTxnRuleSetupCache.putCOA(email, creatCoa1, "", "", UserTxnRuleSetupCache.AUDITOR,
								UserTxnRuleSetupCache.INCOME_HEAD);
					}
					if (!isNewUser && creatCoa1 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa1, "", "", entityManager, newUser,
								UserTxnRuleSetupCache.INCOME_HEAD, IdosConstants.AUDITOR_RIGHTS, isNewUser);
					}
					newUser.setTxnAuditorCoaIncome(creatCoa1);
				}
				if (auditorExpenseChange) {
					String creatCoa1 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.AUDITOR,
							UserTxnRuleSetupCache.EXPENSE_HEAD);
					if ("allcoaitems0".equals(creatCoa1)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder("");
						StringBuilder toAmountSb = new StringBuilder("");
						creatCoa1 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.EXPENSE_HEAD);
						UserTxnRuleSetupCache.putCOA(email, creatCoa1, "", "", UserTxnRuleSetupCache.AUDITOR,
								UserTxnRuleSetupCache.EXPENSE_HEAD);
					}
					if (!isNewUser && creatCoa1 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa1, "", "", entityManager, newUser,
								UserTxnRuleSetupCache.EXPENSE_HEAD, IdosConstants.AUDITOR_RIGHTS, isNewUser);
					}
					newUser.setTxnAuditorCoaExpense(creatCoa1);
				}
				if (auditorAssetsChange) {
					String creatCoa1 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.AUDITOR,
							UserTxnRuleSetupCache.ASSETS_HEAD);
					if ("allcoaitems0".equals(creatCoa1)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder("");
						StringBuilder toAmountSb = new StringBuilder("");
						creatCoa1 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.ASSETS_HEAD);
						UserTxnRuleSetupCache.putCOA(email, creatCoa1, "", "", UserTxnRuleSetupCache.AUDITOR,
								UserTxnRuleSetupCache.ASSETS_HEAD);
					}
					if (!isNewUser && creatCoa1 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa1, "", "", entityManager, newUser,
								UserTxnRuleSetupCache.ASSETS_HEAD, IdosConstants.AUDITOR_RIGHTS, isNewUser);
					}
					newUser.setTxnAuditorCoaAsset(creatCoa1);
				}
				if (auditorLiabilitiesChange) {
					String creatCoa1 = UserTxnRuleSetupCache.getCOA(email, UserTxnRuleSetupCache.AUDITOR,
							UserTxnRuleSetupCache.LIABILITES_HEAD);
					if ("allcoaitems0".equals(creatCoa1)) {
						StringBuilder itemsSb = new StringBuilder();
						StringBuilder fromAmountsb = new StringBuilder("");
						StringBuilder toAmountSb = new StringBuilder("");
						creatCoa1 = USER_SETUP_SERVICE.getAllCoaItemsList(fromAmountsb, toAmountSb, entityManager,
								newUser, UserTxnRuleSetupCache.LIABILITES_HEAD);
						UserTxnRuleSetupCache.putCOA(email, creatCoa1, "", "", UserTxnRuleSetupCache.AUDITOR,
								UserTxnRuleSetupCache.LIABILITES_HEAD);
					}
					if (!isNewUser && creatCoa1 != null) {
						USER_SETUP_SERVICE.saveUpdateTransactionRule(creatCoa1, "", "", entityManager, newUser,
								UserTxnRuleSetupCache.LIABILITES_HEAD, IdosConstants.AUDITOR_RIGHTS, isNewUser);
					}
					newUser.setTxnAuditorCoaLiabl(creatCoa1);
				}
				userauditorrightforcoa = UserTxnRuleSetupCache.getCOAList(email, UserTxnRuleSetupCache.AUDITOR);
			}
			String userTravelEligibility = json.findValue("userTravelEligibility") != null
					? json.findValue("userTravelEligibility").asText()
					: null;
			String userTravelTransPurpose = json.findValue("userTravelTransPurpose") != null
					? json.findValue("userTravelTransPurpose").asText()
					: null;
			if (null != userTravelEligibility && !"".equals(userTravelEligibility)) {
				Travel_Group travelGroup = Travel_Group.findById(IdosUtil.convertStringToLong(userTravelEligibility));
				newUser.settGroup(travelGroup);
			} else {
				newUser.settGroup(null);
			}
			if (null != userTravelTransPurpose && !"".equals(userTravelTransPurpose)) {
				newUser.setUsersTravelClaimTxnQuestions(userTravelTransPurpose);
			} else {
				newUser.setUsersTravelClaimTxnQuestions(null);
			}
			String userExpenseEligibility = json.findValue("userExpenseEligibility") != null
					? json.findValue("userExpenseEligibility").asText()
					: null;
			String userExpenseTransPurpose = json.findValue("userExpenseTransPurpose") != null
					? json.findValue("userExpenseTransPurpose").asText()
					: null;
			if (null != userExpenseEligibility && !"".equals(userExpenseEligibility)) {
				ExpenseGroup expenseGroup = ExpenseGroup.findById(IdosUtil.convertStringToLong(userExpenseEligibility));
				newUser.seteGroup(expenseGroup);
			} else {
				newUser.seteGroup(null);
			}
			if (null != userExpenseTransPurpose && !"".equals(userExpenseTransPurpose)) {
				newUser.setUsersExpenseClaimTxnQuestions(userExpenseTransPurpose);
			} else {
				newUser.setUsersExpenseClaimTxnQuestions(null);
			}

			StringBuilder accessRights = new StringBuilder();
			String customerCreator = json.findValue("customerCreator").asText();
			String vednorCreator = json.findValue("vednorCreator").asText();
			String customerActivator = json.findValue("customerActivator").asText();
			String vednorActivator = json.findValue("vednorActivator").asText();
			accessRights.append(customerCreator).append(customerActivator).append(vednorCreator)
					.append(vednorActivator);
			newUser.setAccessRights(accessRights.toString());

			String moudleAccessStr = "";
			String moduleAccessList = json.findValue("moduleAccess").asText();
			if (moduleAccessList != null && !moduleAccessList.equals("")) {
				String moduleAccess[] = moduleAccessList.split(",");
				for (int i = 0; i < 20; i++) {
					if (ConfigParams.getInstance().getCompanyOwner().equals("PWC") && i == 12) {
						if ("0".equals(moduleAccess[i])) {
							moudleAccessStr += "0";
							moudleAccessStr += "0";
						} else if ("1".equals(moduleAccess[i])) {
							moudleAccessStr += "0";
							moudleAccessStr += "1";
						}
					} else if (ConfigParams.getInstance().getCompanyOwner().equals("PWC") && i == 13) {
						continue;
					} else {
						if (i < moduleAccess.length && moduleAccess[i] != null && !"".equals(moduleAccess[i])
								&& !"0".equals(moduleAccess[i])) {
							moudleAccessStr += "1";
						} else {
							moudleAccessStr += "0";
						}
					}
				}
			}
			newUser.setModuleRights(moudleAccessStr);
			genericDAO.saveOrUpdate(newUser, usr, entityManager);

			newUserObjNode.put("id", newUser.getId());
			newUserObjNode.put("moduleAccess", newUser.getModuleRights());
			an.add(newUserObjNode);

			String userroles = json.findValue("userRoles") != null ? json.findValue("userRoles").asText() : null;
			// List<UsersRoles> oldUserRoles = newUser.getUserRoles();
			String oldUserRoles = getUserRolesIds(newUser);
			String[] oldRolesArray = null;
			if (!oldUserRoles.isEmpty()) {
				oldRolesArray = oldUserRoles.split(",");
			}
			String[] newRoleArray = userroles.split(",");
			if (oldRolesArray != null) {
				for (String oldId : oldRolesArray) {
					if (!contains(newRoleArray, oldId)) {
						// Old ID not present in new IDs, perform delete action
						UsersRoles usrRoles = UsersRoles.findByUsersRole(IdosUtil.convertStringToLong(oldId), newUser);
						if (usrRoles != null) {
							UsersRoles.deleteUsersRole(usrRoles.getId());
						}
					}
				}
			}

			for (String newId : newRoleArray) {
				if (oldRolesArray == null || !contains(oldRolesArray, newId)) {
					// New ID not present in old IDs or oldRolesArray is null, perform add action
					UsersRoles newuserRole = new UsersRoles();
					Role userrole = Role.findById(IdosUtil.convertStringToLong(newId));
					newuserRole.setUser(newUser);
					newuserRole.setRole(userrole);
					newuserRole.setOrganization(usr.getOrganization());
					newuserRole.setBranch(newUserBnch);
					genericDAO.saveOrUpdate(newuserRole, newUser, entityManager);
				}

			}
			// List<UsersRoles> newUserList = new ArrayList<UsersRoles>();
			String userRole = getUserRoles(newUser);
			// if (userroles != null && !userroles.equals("")) {
			// String roles[] = userroles.split(",");
			// for (int i = 0; i < roles.length; i++) {
			// UsersRoles newuserRole = new UsersRoles();
			// Role userrole = Role.findById(IdosUtil.convertStringToLong(roles[i]));
			// newuserRole.setUser(newUser);
			// newuserRole.setRole(userrole);
			// newuserRole.setOrganization(usr.getOrganization());
			// newuserRole.setBranch(newUserBnch);
			// newUserList.add(newuserRole);
			// userRole += userrole.getName() + ",";
			// }
			// }
			newUserObjNode.put("userRole", userRole.substring(0, userRole.length() - 1));
			// List<List<UsersRoles>> userRolesTransactionList =
			// ListUtility.getUserRoleTransactionList(oldUserRoles,
			// newUserList);
			// for (int i = 0; i < userRolesTransactionList.size(); i++) {
			// if (i == 0) {
			// List<UsersRoles> oldUserRole = userRolesTransactionList.get(i);
			// if (oldUserRole != null) {
			// for (UsersRoles usrRoles : oldUserRole) {
			// if (!entityManager.contains(usrRoles)) {
			// // If not contained, merge to associate with the current session
			// usrRoles = entityManager.merge(usrRoles);
			// }
			// entityManager.remove(usrRoles);
			// }
			// }
			// }
			// if (i == 1) {
			// List<UsersRoles> newRoleUser = userRolesTransactionList.get(i);
			// if (newRoleUser != null) {
			// for (UsersRoles newuserRoles : newRoleUser) {
			// genericDAO.saveOrUpdate(newuserRoles, newUser, entityManager);
			// }
			// }
			// }
			// }

			String userTransactionQuestions = json.findValue("userTransactionQuestions") != null
					? json.findValue("userTransactionQuestions").asText()
					: null;
			List<UserTransactionPurpose> oldUserTxnPurpose = newUser.getUserTxnQuestions();
			List<UserTransactionPurpose> newUserTxnQuestions = new ArrayList<UserTransactionPurpose>();
			if (userTransactionQuestions != null && !userTransactionQuestions.equals("")) {
				String txnQuestions[] = userTransactionQuestions.split(",");
				for (int i = 0; i < txnQuestions.length; i++) {
					UserTransactionPurpose usrTxnPurp = new UserTransactionPurpose();
					usrTxnPurp.setUser(newUser);
					TransactionPurpose txnPurp = TransactionPurpose
							.findById(IdosUtil.convertStringToLong(txnQuestions[i]));
					usrTxnPurp.setTransactionPurpose(txnPurp);
					usrTxnPurp.setBranch(newUser.getBranch());
					usrTxnPurp.setOrganization(newUser.getOrganization());
					newUserTxnQuestions.add(usrTxnPurp);
				}
			}
			List<List<UserTransactionPurpose>> userTransactionPurposeList = ListUtility
					.getUserTransactionPurposeTransactionList(oldUserTxnPurpose, newUserTxnQuestions);
			for (int i = 0; i < userTransactionPurposeList.size(); i++) {
				if (i == 0) {
					List<UserTransactionPurpose> oldUserTxnPurp = userTransactionPurposeList.get(i);
					if (oldUserTxnPurp != null) {
						for (UserTransactionPurpose usrTxnPurp : oldUserTxnPurp) {
							entityManager.remove(usrTxnPurp);
						}
					}
				}
				if (i == 1) {
					List<UserTransactionPurpose> newUserTxnPurp = userTransactionPurposeList.get(i);
					if (newUserTxnPurp != null) {
						for (UserTransactionPurpose newuserTxnPurp : newUserTxnPurp) {
							genericDAO.saveOrUpdate(newuserTxnPurp, newUser, entityManager);
						}
					}
				}
			}

			criteria.clear();
			criteria.put("user.id", newUser.getId());
			criteria.put("userRights.id", 1L);
			criteria.put("presentStatus", 1);
			List<UserRightInBranch> olduserCreationRightInBranches = genericDAO.findByCriteria(UserRightInBranch.class,
					criteria, entityManager);
			List<UserRightInBranch> newuserCreationRightInBranches = new ArrayList<UserRightInBranch>();
			if (usercreationrightinbranches != null && !usercreationrightinbranches.equals("")) {
				String creationRightInBranches[] = usercreationrightinbranches.split(",");
				for (int i = 0; i < creationRightInBranches.length; i++) {
					UserRightInBranch newUserCreationRight = new UserRightInBranch();
					UserRights usrRights = UserRights.findById(1L);
					Branch usrRightInBranch = Branch.findById(IdosUtil.convertStringToLong(creationRightInBranches[i]));
					newUserCreationRight.setUser(newUser);
					newUserCreationRight.setUserRights(usrRights);
					newUserCreationRight.setOrganization(usr.getOrganization());
					newUserCreationRight.setBranch(usrRightInBranch);
					newuserCreationRightInBranches.add(newUserCreationRight);
				}
			}
			List<List<UserRightInBranch>> userCreationRightTransactionList = ListUtility
					.getUserRightsInBranchList(olduserCreationRightInBranches, newuserCreationRightInBranches);
			for (int i = 0; i < userCreationRightTransactionList.size(); i++) {
				if (i == 0) {
					List<UserRightInBranch> oldCreationRightInBranches = userCreationRightTransactionList.get(i);
					if (oldCreationRightInBranches != null) {
						for (UserRightInBranch userRight : oldCreationRightInBranches) {
							entityManager.remove(userRight);
						}
					}
				}
				if (i == 1) {
					List<UserRightInBranch> newCreationRightInBranches = userCreationRightTransactionList.get(i);
					if (newCreationRightInBranches != null) {
						for (UserRightInBranch userRight : newCreationRightInBranches) {
							genericDAO.saveOrUpdate(userRight, newUser, entityManager);
						}
					}
				}
			}

			criteria.clear();
			criteria.put("user.id", newUser.getId());
			criteria.put("userRights.id", 1L);
			criteria.put("presentStatus", 1);
			List<UserRightForProject> olduserCreationRightInProjects = genericDAO
					.findByCriteria(UserRightForProject.class, criteria, entityManager);
			List<UserRightForProject> newuserCreationRightInProject = new ArrayList<UserRightForProject>();
			if (usercreationrightinprojects != null && !usercreationrightinprojects.equals("")) {
				String creationRightInProjects[] = usercreationrightinprojects.split(",");
				for (int i = 0; i < creationRightInProjects.length; i++) {
					UserRightForProject newUserCreationRight = new UserRightForProject();
					UserRights usrRights = UserRights.findById(1L);
					Project usrRightInProject = Project
							.findById(IdosUtil.convertStringToLong(creationRightInProjects[i]));
					newUserCreationRight.setUser(newUser);
					newUserCreationRight.setUserRights(usrRights);
					newUserCreationRight.setProject(usrRightInProject);
					newuserCreationRightInProject.add(newUserCreationRight);
				}
			}
			List<List<UserRightForProject>> userCreationRightProjectTransactionList = ListUtility
					.getUserRightsInProjectList(olduserCreationRightInProjects, newuserCreationRightInProject);
			for (int i = 0; i < userCreationRightProjectTransactionList.size(); i++) {
				if (i == 0) {
					List<UserRightForProject> oldCreationRightInProjects = userCreationRightProjectTransactionList
							.get(i);
					if (oldCreationRightInProjects != null) {
						for (UserRightForProject userRight : oldCreationRightInProjects) {
							entityManager.remove(userRight);
						}
					}
				}
				if (i == 1) {
					List<UserRightForProject> newCreationRightInProjects = userCreationRightProjectTransactionList
							.get(i);
					if (newCreationRightInProjects != null) {
						for (UserRightForProject userRight : newCreationRightInProjects) {
							genericDAO.saveOrUpdate(userRight, newUser, entityManager);
						}
					}
				}
			}

			if (usercreationrightforcoa != null && !usercreationrightforcoa.equals("") && isNewUser) {
				USER_SETUP_SERVICE.saveUpdateTransactionRule(usercreationrightforcoa,
						usercreationrightforcoaamountlimit, usercreationrightforcoaamountcriteria, entityManager,
						newUser, UserTxnRuleSetupCache.ALL_HEAD, IdosConstants.CREATOR_RIGHTS, isNewUser);
			}

			// transaction approval related crud
			criteria.clear();
			criteria.put("user.id", newUser.getId());
			criteria.put("userRights.id", 2L);
			criteria.put("presentStatus", 1);
			List<UserRightInBranch> olduserApprovalRightInBranches = genericDAO.findByCriteria(UserRightInBranch.class,
					criteria, entityManager);
			List<UserRightInBranch> newuserApprovalRightInBranches = new ArrayList<UserRightInBranch>();
			if (userapprovalrightinbranches != null && !userapprovalrightinbranches.equals("")) {
				String approvalRightInBranches[] = userapprovalrightinbranches.split(",");
				for (int i = 0; i < approvalRightInBranches.length; i++) {
					UserRightInBranch newUserApprovalRight = new UserRightInBranch();
					UserRights usrRights = UserRights.findById(2L);
					Branch usrRightInBranch = Branch.findById(IdosUtil.convertStringToLong(approvalRightInBranches[i]));
					newUserApprovalRight.setUser(newUser);
					newUserApprovalRight.setUserRights(usrRights);
					newUserApprovalRight.setOrganization(usr.getOrganization());
					newUserApprovalRight.setBranch(usrRightInBranch);
					newuserApprovalRightInBranches.add(newUserApprovalRight);
					/*
					 * criteria.clear();
					 * criteria.put("transactionBranchOrganization.id",
					 * newUser.getOrganization().getId());
					 * criteria.put("transactionBranch.id", usrRightInBranch.getId());
					 * criteria.put("transactionStatus", "Require Approval");
					 * 
					 * List<Transaction>
					 * listOfTransactionForApproval=genericDAO.findByCriteria(Transaction.class,
					 * criteria, entityManager);
					 * log.log(Level.INFO,
					 * "list of transaction= "+listOfTransactionForApproval.size());
					 * for(Transaction transaction1:listOfTransactionForApproval){
					 * String approverMail=transaction1.getApproverEmails();
					 * log.log(Level.INFO, "approver email="+approverMail);
					 * approverMail += newUser.getEmail() + ",";
					 * transaction1.setApproverEmails(approverMail);
					 * log.log(Level.INFO, "approver email after update="+approverMail);
					 * genericDAO.saveOrUpdate(transaction1, newUser, entityManager);
					 * 
					 * }
					 * if(listOfTransactionForApproval.size()>0){
					 * Transaction transaction1=listOfTransactionForApproval.get(0);
					 * log.log(Level.INFO, "transaction number="+transaction1.getApproverEmails());
					 * }
					 * for(int j=0;j<listOfTransactionForApproval.size();j++){
					 * String approverMail=listOfTransactionForApproval.get(j).getApproverEmails();
					 * log.log(Level.INFO, "approver email="+approverMail);
					 * approverMail += newUser.getEmail() + ",";
					 * listOfTransactionForApproval.get(j).setApproverEmails(approverMail);
					 * listOfTransactionForApproval.get(j).setAdditionalApproverEmails(approverMail)
					 * ;
					 * log.log(Level.INFO, "approver email after update="+approverMail);
					 * genericDAO.saveOrUpdate(listOfTransactionForApproval.get(j), newUser,
					 * entityManager);
					 * 
					 * }
					 */
				}
			}
			List<List<UserRightInBranch>> userApprovalRightTransactionList = ListUtility
					.getUserRightsInBranchList(olduserApprovalRightInBranches, newuserApprovalRightInBranches);
			for (int i = 0; i < userApprovalRightTransactionList.size(); i++) {
				if (i == 0) {
					List<UserRightInBranch> oldApprovalRightInBranches = userApprovalRightTransactionList.get(i);
					if (oldApprovalRightInBranches != null) {
						for (UserRightInBranch userRight : oldApprovalRightInBranches) {
							entityManager.remove(userRight);
						}
					}
				}
				if (i == 1) {
					List<UserRightInBranch> newApprovalRightInBranches = userApprovalRightTransactionList.get(i);
					if (newApprovalRightInBranches != null) {
						for (UserRightInBranch userRight : newApprovalRightInBranches) {
							genericDAO.saveOrUpdate(userRight, newUser, entityManager);
						}
					}
				}
			}

			criteria.clear();
			criteria.put("user.id", newUser.getId());
			criteria.put("userRights.id", 2L);
			criteria.put("presentStatus", 1);
			List<UserRightForProject> olduserApprovalRightInProjects = genericDAO
					.findByCriteria(UserRightForProject.class, criteria, entityManager);
			List<UserRightForProject> newuserApprovalRightInProject = new ArrayList<UserRightForProject>();
			if (userapprovalrightinprojects != null && !userapprovalrightinprojects.equals("")) {
				String approvalRightInProjects[] = userapprovalrightinprojects.split(",");
				for (int i = 0; i < approvalRightInProjects.length; i++) {
					UserRightForProject newUserCreationRight = new UserRightForProject();
					UserRights usrRights = UserRights.findById(2L);
					Project usrRightInProject = Project
							.findById(IdosUtil.convertStringToLong(approvalRightInProjects[i]));
					newUserCreationRight.setUser(newUser);
					newUserCreationRight.setUserRights(usrRights);
					newUserCreationRight.setProject(usrRightInProject);
					newuserApprovalRightInProject.add(newUserCreationRight);
				}
			}

			List<List<UserRightForProject>> userApprovalRightProjectTransactionList = ListUtility
					.getUserRightsInProjectList(olduserApprovalRightInProjects, newuserApprovalRightInProject);
			for (int i = 0; i < userApprovalRightProjectTransactionList.size(); i++) {
				if (i == 0) {
					List<UserRightForProject> oldApprovalRightInProjects = userApprovalRightProjectTransactionList
							.get(i);
					if (oldApprovalRightInProjects != null) {
						for (UserRightForProject userRight : oldApprovalRightInProjects) {
							entityManager.remove(userRight);
						}
					}
				}
				if (i == 1) {
					List<UserRightForProject> newApprovalRightInProjects = userApprovalRightProjectTransactionList
							.get(i);
					if (newApprovalRightInProjects != null) {
						for (UserRightForProject userRight : newApprovalRightInProjects) {
							genericDAO.saveOrUpdate(userRight, newUser, entityManager);
						}
					}
				}
			}

			if (userapprovalrightforcoa != null && !userapprovalrightforcoa.equals("") && isNewUser) {
				USER_SETUP_SERVICE.saveUpdateTransactionRule(userapprovalrightforcoa,
						userapprovalrightforcoaamountlimit, userapprovalrightforcoaamountcriteria, entityManager,
						newUser, UserTxnRuleSetupCache.ALL_HEAD, 2L, isNewUser);
			}

			// added by firdous
			Boolean approver = null;
			for (int i = 0; i < newuserApprovalRightInBranches.size(); i++) {
				approver = false;
				Branch usrRightInBranch = Branch.findById(newuserApprovalRightInBranches.get(i).getBranch().getId());
				criteria.clear();
				criteria.put("transactionBranch.id", usrRightInBranch.getId());
				criteria.put("transactionStatus", "Require Approval");
				criteria.put("presentStatus", 1);
				List<Transaction> listOfTransactionForApproval = genericDAO.findByCriteria(Transaction.class, criteria,
						entityManager);
				for (Transaction transaction1 : listOfTransactionForApproval) {
					criteria.clear();
					criteria.put("transaction.id", transaction1.getId());
					List<TransactionItems> listOfTransactionItems = genericDAO.findByCriteria(TransactionItems.class,
							criteria, entityManager);
					if (listOfTransactionItems != null) {
						approver = true;
					} else {
						for (int l = 0; l < listOfTransactionItems.size(); l++) {
							TransactionItems transactionItem = listOfTransactionItems.get(l);
							Specifics txnItem = genericDAO.getById(Specifics.class,
									transactionItem.getTransactionSpecifics().getId(), entityManager);
							criteria.clear();
							criteria.put("user.id", newUser.getId());
							criteria.put("userRights.id", 2L);
							criteria.put("specifics.id", txnItem.getId());
							criteria.put("presentStatus", 1);
							UserRightSpecifics userHasRightInCOA = genericDAO.getByCriteria(UserRightSpecifics.class,
									criteria, entityManager);
							if (userHasRightInCOA != null) {
								approver = true;
							} else {
								approver = false;
							}
						}
					}
					if (approver) {
						String approverMail = transaction1.getApproverEmails();
						approverMail += newUser.getEmail() + ",";
						transaction1.setApproverEmails(approverMail);
						transaction1.setAdditionalApproverEmails(approverMail);
						genericDAO.saveOrUpdate(transaction1, newUser, entityManager);
					}
				}
			}

			// transaction auditor related crud
			criteria.clear();
			criteria.put("user.id", newUser.getId());
			criteria.put("userRights.id", 3L); // static data set in user_rights table
			criteria.put("presentStatus", 1);
			List<UserRightInBranch> olduserAuditorRightInBranches = genericDAO.findByCriteria(UserRightInBranch.class,
					criteria, entityManager);
			List<UserRightInBranch> newuserAuditorRightInBranches = new ArrayList<UserRightInBranch>();
			if (userauditorrightinbranches != null && !userauditorrightinbranches.equals("")) {
				String auditorRightInBranches[] = userauditorrightinbranches.split(",");
				for (int i = 0; i < auditorRightInBranches.length; i++) {
					UserRightInBranch newUserAuditorRight = new UserRightInBranch();
					UserRights usrRights = UserRights.findById(3L);
					Branch usrRightInBranch = Branch.findById(IdosUtil.convertStringToLong(auditorRightInBranches[i]));
					newUserAuditorRight.setUser(newUser);
					newUserAuditorRight.setUserRights(usrRights);
					newUserAuditorRight.setOrganization(usr.getOrganization());
					newUserAuditorRight.setBranch(usrRightInBranch);
					newuserAuditorRightInBranches.add(newUserAuditorRight);
				}
			}
			List<List<UserRightInBranch>> userAuditorRightTransactionList = ListUtility
					.getUserRightsInBranchList(olduserAuditorRightInBranches, newuserAuditorRightInBranches);
			for (int i = 0; i < userAuditorRightTransactionList.size(); i++) {
				if (i == 0) {
					List<UserRightInBranch> oldAuditorRightInBranches = userAuditorRightTransactionList.get(i);
					if (oldAuditorRightInBranches != null) {
						for (UserRightInBranch userRight : oldAuditorRightInBranches) {
							entityManager.remove(userRight);
						}
					}
				}
				if (i == 1) {
					List<UserRightInBranch> newAuditorRightInBranches = userAuditorRightTransactionList.get(i);
					if (newAuditorRightInBranches != null) {
						for (UserRightInBranch userRight : newAuditorRightInBranches) {
							genericDAO.saveOrUpdate(userRight, newUser, entityManager);
						}
					}
				}
			}

			if (userauditorrightforcoa != null && !userauditorrightforcoa.equals("") && isNewUser) {
				USER_SETUP_SERVICE.saveUpdateTransactionRule(userauditorrightforcoa, "", "", entityManager, newUser,
						UserTxnRuleSetupCache.ALL_HEAD, 3L, isNewUser);
			}

			transaction.commit();
			String body = userAccountCreation.render(newUser.getEmail(), password, ConfigParams.getInstance()).body();
			final String username = ConfigFactory.load().getString("smtp.user");
			Session session = emailsession;
			String subject = "";
			if (userPrimKey != "" && userPrimKey != null) {
				subject = "A user account updated for " + newUser.getOrganization().getName();
				if (!email.equals(newUser.getEmail())) {
					mailTimer(body, username, session, newUser.getEmail(), null, subject);
				}
			}
			if (userPrimKey.equals("") || userPrimKey == null) {
				subject = "A new user account added for " + newUser.getOrganization().getName();
				mailTimer(body, username, session, newUser.getEmail(), null, subject);
			}

		} catch (

		Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	private static boolean contains(String[] array, String target) {
		for (String s : array) {
			if (s.equals(target)) {
				return true;
			}
		}
		return false;
	}

	@Transactional
	public Result searchUsers(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode serachUserData = result.putArray("userListData");
			user = getUserInfo(request);
			String enteredUserValue = json.findPath("freeTextSearchUsersVal") != null
					? json.findPath("freeTextSearchUsersVal").asText()
					: null;
			enteredUserValue = IdosUtil.escapeHtml(enteredUserValue);
			List<Users> userList = null;
			List<Users> extUserList = new ArrayList<Users>();
			if (enteredUserValue != null) {
				if (!enteredUserValue.equals("")) {
					String newsbquery = ("select obj from Users obj WHERE obj.organization.id =?1 and obj.presentStatus=1 and (obj.fullName like ?2 or obj.email like ?3)");
					ArrayList inparam = new ArrayList(3);
					inparam.add(user.getOrganization().getId());
					inparam.add(enteredUserValue + "%");
					inparam.add(enteredUserValue + "%");
					userList = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
				} else {
					userList = Users.findAllActByOrg(entityManager, user.getOrganization().getId());
					// log.log(Level.FINE, "company
					// name==="+ConfigParams.getInstance().getCompanyName());
					if (!ConfigParams.getInstance().getCompanyOwner().equals("PWC")) {
						log.log(Level.FINE, "inside karvy or idos...");
						extUserList = Users.findAllExtUserActByOrg(entityManager, user.getOrganization().getId());
					}
				}
			} else {
				userList = Users.findAllActByOrg(entityManager, user.getOrganization().getId());
				if (!ConfigParams.getInstance().getCompanyOwner().equals("PWC")) {
					log.log(Level.FINE, "inside karvy or idos...");
					extUserList = Users.findAllExtUserActByOrg(entityManager, user.getOrganization().getId());
				}
			}

			if (userList != null && userList.size() > 0) {
				// excluding deactivated external users from search list
				for (int i = 0; i < userList.size(); i++) {
					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.put("corrUserId.id", userList.get(i).getId());
					criterias.put("org.id", userList.get(i).getOrganization().getId());
					criterias.put("presentStatus", 1);
					ExternalUserCompanyDetails extUser = genericDAO.getByCriteria(ExternalUserCompanyDetails.class,
							criterias, entityManager);
					if (extUser != null) {
						log.log(Level.FINE, "ext user name==" + extUser.getCorrUserId().getFullName());
						if (extUser.getExternalUserStatus() == IdosConstants.EXTERNAL_USER_DEACTIVATED) {
							log.log(Level.FINE, "deactivated user..");
							userList.remove(i);
						} else {
							extUserList.add(extUser.getCorrUserId());
							userList.remove(i);
						}
					}
				}
			}
			log.log(Level.FINE, "userList.size()==" + userList.size());
			if (userList != null && userList.size() > 0) {
				for (Users userTableList : userList) {
					String userRoles = getUserRoles(userTableList);
					if (userRoles != null && !userRoles.equals("")) {
						userRoles = userRoles.substring(0, userRoles.length() - 1);
					}
					ObjectNode row = Json.newObject();
					row.put("id", userTableList.getId());
					row.put("fullName", userTableList.getFullName());
					row.put("userEmail", userTableList.getEmail());
					row.put("userBranch", userTableList.getBranch().getName());
					row.put("userRole", userRoles);
					row.put("isActive", false);
					serachUserData.add(row);
				}
			}
			if (extUserList != null && extUserList.size() > 0) {
				for (Users extUserTableList : extUserList) {

					String userRoles = getUserRoles(extUserTableList);
					if (userRoles != null && !userRoles.equals("")) {
						userRoles = userRoles.substring(0, userRoles.length() - 1);
					}
					ObjectNode row = Json.newObject();
					row.put("id", extUserTableList.getId());
					row.put("fullName", extUserTableList.getFullName());
					row.put("userEmail", extUserTableList.getEmail());
					row.put("userBranch", extUserTableList.getBranch().getName());
					row.put("userRole", "EXTERNAL USER, " + userRoles);
					row.put("isActive", false);
					serachUserData.add(row);
				}
			}

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result getUsersClaimData(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			ArrayNode userClaimCoaDatas = result.putArray("userClaimCoaDatas");
			ArrayNode userClaimModesOfTravelDatas = result.putArray("userClaimModesOfTravelDatas");
			String email = json.findValue("usermail").asText();
			session.adding("email", email);
			user = getUserInfo(request);
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.clear();
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("employeeClaimItem", 1);
			criterias.put("presentStatus", 1);
			List<Specifics> orgTravelClaimCoas = genericDAO.findByCriteria(Specifics.class, criterias, entityManager);
			if (orgTravelClaimCoas.size() > 0) {
				for (Specifics claimSpecf : orgTravelClaimCoas) {
					ObjectNode row = Json.newObject();
					row.put("id", claimSpecf.getId());
					row.put("name", claimSpecf.getName());
					userClaimCoaDatas.add(row);
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result resetPasswordExpiry(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		String result = null;
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("usermail").asText();
			String oldPwd = json.findValue("oldPwd").asText();
			String newPwd = json.findValue("newPwd").asText();
			String confirmPwd = json.findValue("confirmPwd").asText();
			if (newPwd.equals(confirmPwd)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user) {
					oldPwd = PasswordUtil.encrypt(oldPwd);
					if (oldPwd.equals(user.getPassword())) {
						String newPwd1 = PasswordUtil.encrypt(newPwd);
						if (!PasswordChangeService.isPresentInPasswordHistory(newPwd1, user, entityManager)) {
							user.setPassword(newPwd1);
							user.setLastUpdatedPasswordDate(Calendar.getInstance().getTime());
							entityTransaction.begin();
							genericDAO.saveOrUpdate(user, user, entityManager);
							PasswordHistory history = new PasswordHistory(email, user, user.getOrganization(), newPwd1);
							genericDAO.saveOrUpdate(history, user, entityManager);
							entityTransaction.commit();
							final String username = ConfigFactory.load().getString("smtp.user");
							String body = passwordChange
									.render(newPwd, user.getFullName(), user.getEmail(), ConfigParams.getInstance())
									.body();
							String subject = "Login Details";
							mailTimer(body, username, emailsession, user.getEmail(), null, subject);
							result = "success";
						} else {
							result = "passwordpresent";
						}
					} else {
						result = "wrongpassword";
					}
				} else {
					result = "failure";
				}
			} else {
				result = "notequal";
			}
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			result = "failure";
		}
		return Results.ok(result);
	}

	@Transactional
	public Result resetSecurityAnswerLink(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			String token = json.findValue("token").asText();
			if ((null != email || !"".equals(email)) && (null != token && !"".equals(token))) {
				user = Users.findByEmailNresetPasswordToken(entityManager, token, email);
				if (user != null) {
					final String username = ConfigFactory.load().getString("smtp.user");
					String body = resetSecurityAnswerLink.render(email, token, ConfigParams.getInstance()).body();
					mailTimer(body, username, emailsession, email, null, "Reset Security Answer");
					result.put("result", true);
					result.put("message", "Check your mail and follow the link to reset your security answer.");
				}
			}
		} catch (Exception ex) {
			result.put("message", "Something went wrong. Please try again later.");
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getSecurityQuestionsByUser(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("questions");
		result.put("result", false);
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			String token = json.findValue("token").asText();
			if ((null != email || !"".equals(email)) && (null != token && !"".equals(token))) {
				user = Users.findByEmailNresetPasswordToken(entityManager, token, email);
				if (user != null) {
					if (null != user) {
						StringBuffer query = new StringBuffer(
								"SELECT obj FROM UserProfileSecurity obj WHERE obj.user.id = ").append(user.getId());
						List<UserProfileSecurity> securities = genericDAO.executeSimpleQuery(query.toString(),
								entityManager);
						if (!securities.isEmpty() && securities.size() > 0) {
							ObjectNode row = null;
							for (UserProfileSecurity security : securities) {
								if (null != security) {
									row = Json.newObject();
									if (null != security.getSecurityQuestion()) {
										row.put("question", security.getSecurityQuestion());
									}
									if (null != security.getId()) {
										row.put("questionId", security.getId());
									}
									an.add(row);
								}
							}
							result.put("result", true);
						}
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getUserOpeningBalAdvClaim(Request request) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (user == null) {
				return unauthorized();
			}
			Long userId = json.findValue("userId") != null ? json.findValue("userId").asLong() : null;
			Long mapping = json.findValue("mappingId") != null ? json.findValue("mappingId").asLong() : null;
			Users mappedUser = Users.findById(userId);
			if (mappedUser != null) {
				result.put("id", mappedUser.getId());
				if (mapping == 60l) {
					result.put("name", mappedUser.getFullName() + "- Advance");
					if (mappedUser.getOpenigBalanceAdvance() != null) {
						result.put("openingBalance", mappedUser.getOpenigBalanceAdvance());
					} else {
						result.put("openingBalance", "");
					}
				} else if (mapping == 61l) {
					if (mappedUser.getOpenigBalanceClaim() != null) {
						result.put("name", mappedUser.getFullName() + "- Claim");
						result.put("openingBalance", mappedUser.getOpenigBalanceClaim());
					} else {
						result.put("openingBalance", "");
					}
				} else {
					result.put("openingBalance", "");
				}
			}

		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result saveOpeningBalanceAdvClaim(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users users = getUserInfo(request);
		if (users == null) {
			return unauthorized();
		}
		try {
			JsonNode json = request.body().asJson();
			Long id = json.findValue("id").asLong();
			Double openingBalance = json.findValue("openingBalance").asDouble();
			Long mappingId = json.findValue("mappingId").asLong();
			transaction.begin();
			Users mappedUser = users.findById(id);
			if (mappedUser != null) {
				if (mappingId != null && mappingId == 60l) {
					mappedUser.setOpenigBalanceAdvance(openingBalance);
				} else if (mappingId != null && mappingId == 61l) {
					mappedUser.setOpenigBalanceClaim(openingBalance);
				}
				genericDAO.saveOrUpdate(mappedUser, users, entityManager);
			}
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);

	}

}
