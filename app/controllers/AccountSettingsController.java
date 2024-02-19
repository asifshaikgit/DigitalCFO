package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.UserProfileSecurity;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Http.Request;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import service.AccountSettingService;
import service.PasswordChangeService;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import play.mvc.Http;
import play.Application;
import com.idos.util.RandomSecurityQuestion;

@SuppressWarnings("unchecked")
public class AccountSettingsController extends StaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;

	@Inject
	public AccountSettingsController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result getUserDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users users = null;
		try {
			entityTransaction.begin();
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			String type = json.findValue("type").asText();
			long diff = 0;
			if (!"".equals(email) && null != email) {
				Http.Session session = request.session();
				session.adding("email", email);
				users = getUserInfo(request);
				if (null != users) {
					ArrayNode an = result.putArray("result");
					ObjectNode row = Json.newObject();
					if (null != users.getId()) {
						row.put("id", users.getId());
						if ("profile".equalsIgnoreCase(type)) {
							String query = "SELECT obj FROM UserProfileSecurity obj WHERE obj.user.id = ?";
							ArrayList inparamList = new ArrayList(1);
							inparamList.add(users.getId());
							List<UserProfileSecurity> profileSecurities = genericDAO.queryWithParams(query,
									entityManager, inparamList);
							if (!profileSecurities.isEmpty() && profileSecurities.size() > 0) {
								String question = RandomSecurityQuestion.returnRandomQuestion(profileSecurities);
								if (null != question) {
									for (UserProfileSecurity security : profileSecurities) {
										if (question.equals(security.getSecurityQuestion())) {
											row.put("question", question);
											row.put("questionId", security.getId());
										}
									}
								} else {
									row.put("question", "");
									row.put("questionId", "");
								}
							}
						}
					} else {
						row.put("id", "");
					}
					if (null != users.getEmail()) {
						row.put("email", users.getEmail());
					} else {
						row.put("email", "");
					}

					if ("profile".equalsIgnoreCase(type)) {
						if (null != users.getDob()) {
							row.put("dob", idosdf.format(users.getDob()));
						} else {
							row.put("dob", "");
						}
						if (users.getMobile() != null) {
							int n = users.getMobile().indexOf("-");
							row.put("phoneCode", users.getPhoneNumberCountryCode());
							row.put("phone", users.getMobile().substring(n + 1, users.getMobile().length()));
						} else {
							row.put("phoneCode", "");
							row.put("phone", "");
						}
						if (null != users.getAddress()) {
							row.put("address", users.getAddress());
						} else {
							row.put("address", "");
						}
						if (null != users.getBloodGroup()) {
							row.put("bloodGroup", users.getBloodGroup());
						} else {
							row.put("bloodGroup", "");
						}
						if (null != users.getIdproof()) {
							row.put("idProof", users.getIdproof());
						} else {
							row.put("idProof", "");
						}
						an.add(row);
					} else if ("general".equalsIgnoreCase(type)) {
						if (null != users.getLastLoginDate()) {
							row.put("lastLogin", idosdf.format(users.getLastLoginDate()));
							diff = PasswordChangeService.passwordChangeDifference(users.getLastLoginDate());
							row.put("lastLoginDays", diff);
						} else {
							row.put("lastLogin", "");
							row.put("lastLoginDays", "");
						}
						if (null != users.getLastUpdatedPasswordDate()) {
							row.put("lastPwdChange", idosdf.format(users.getLastUpdatedPasswordDate()));
							diff = PasswordChangeService.passwordChangeDifference(users.getLastUpdatedPasswordDate());
							row.put("lastPwdChangeDays", diff);
						} else {
							row.put("lastPwdChange", "");
							row.put("lastPwdChangeDays", "");
						}
						an.add(row);
					}
				}
			}
			entityTransaction.commit();
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.commit();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result updateUserProfile(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			entityTransaction.begin();
			JsonNode jsonNode = request.body().asJson();
			String question = jsonNode.findValue("question") != null ? jsonNode.findValue("question").asText() : null;
			String questionId = jsonNode.findValue("questionId") != null ? jsonNode.findValue("questionId").asText()
					: null;
			String answer = jsonNode.findValue("answer") != null ? jsonNode.findValue("answer").asText() : null;
			Long userId = jsonNode.findValue("userId").asLong();
			boolean update = jsonNode.findValue("update").asBoolean();
			UserProfileSecurity security = null;
			if ((null != question || null != questionId || null != answer) && !update) {
				Map<String, Object> criterias = new HashMap<String, Object>();
				criterias.put("user.id", userId);
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
				String email = jsonNode.findValue("email").asText();
				if (!"".equals(email) || null != email) {
					// String query = "SELECT obj FROM Users obj where obj.id = " + userId + " AND
					// obj.email = '" + email + "'";
					// List<Users> users = genericDAO.executeSimpleQueryWithLimit(query,
					// entityManager, 1);
					// if(!users.isEmpty() && users.size() > 0) {
					// user = users.get(0);
					user = Users.findById(userId);
					if (user != null) {
						String dob = jsonNode.findValue("dob").asText();
						String address = jsonNode.findValue("address").asText();
						String idProof = jsonNode.findValue("idProof").asText();
						String bloodGroup = jsonNode.findValue("bloodGroup").asText();
						String phoneCodeText = jsonNode.findValue("phoneCodeText").asText();
						String phoneCodeValue = jsonNode.findValue("phoneCodeValue").asText();
						String phone = jsonNode.findValue("phone").asText();
						if (!"".equals(dob) && null != dob) {
							user.setDob(mysqldf.parse(mysqldf.format(idosdf.parse(dob))));
						}
						if (!"".equals(phoneCodeValue) && null != phoneCodeValue) {
							phone = phoneCodeValue.concat("-" + phone);
						} else {
							phone = "-" + phone;
						}
						user.setAddress(address);
						user.setIdproof(idProof);
						user.setMobile(phone);
						user.setPhoneNumberCountryCode(phoneCodeText);
						user.setBloodGroup(bloodGroup);
						genericDAO.saveOrUpdate(user, user, entityManager);
						result.put("result", "success");
						result.put("message", "Profile Updation Successful.");
					} else {
						result.put("result", "failure");
						result.put("message", "Not able to find the user.");
					}
				} else {
					result.put("result", "failure");
					result.put("message", "Not able to find the user.");
				}
			} else {
				if (null == security) {
					result.put("result", "failure");
					result.put("message", "Provide a Correct Security Answer.");
				} else {
					result.put("result", "failure");
					result.put("message", "Not able to find the user.");
				}
			}
			entityTransaction.commit();
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			result.put("result", "failure");
			result.put("message", "Profile Updation Unsuccessful.");
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getUserSecurityQuestions(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("security");
		String userEmail = null;
		try {
			entityTransaction.begin();
			JsonNode jsonNode = request.body().asJson();
			Long userId = jsonNode.findValue("userId").asLong();
			userEmail = jsonNode.findValue("userEmail").asText();
			if (!"".equals(userId) || null != userId) {
				String query = "SELECT obj FROM UserProfileSecurity obj WHERE obj.user.id = ?";
				ArrayList inparamList = new ArrayList(1);
				inparamList.add(userId);
				List<UserProfileSecurity> profileSecurities = genericDAO.queryWithParams(query, entityManager,
						inparamList);
				List<String> questionList = new ArrayList<String>();
				for (UserProfileSecurity security : profileSecurities) {
					ObjectNode row = Json.newObject();
					if (null != security.getId()) {
						row.put("securityId", security.getId());
					} else {
						row.put("securityId", "");
					}
					if (null != security.getSecurityQuestion()) {
						questionList.add(security.getSecurityQuestion());
						row.put("question", security.getSecurityQuestion());
					} else {
						row.put("question", "");
					}
					if (null != security.getSecuredAnswer()) {
						row.put("answer", security.getSecuredAnswer());
					} else {
						row.put("answer", "");
					}
					an.add(row);
				}
				String[] questions = RandomSecurityQuestion.getNotInList(questionList);
				for (String question : questions) {
					ObjectNode row = Json.newObject();
					if (!"".equals(question) || null != question) {
						row.put("question", question);
						row.put("answer", "");
						an.add(row);
					}
				}
			}
			entityTransaction.commit();
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, userEmail, "GetUserSecurityQuestions Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getUserRandomQuestion(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			String userEmail = json.findValue("userEmail").asText();
			result = AccountSettingService.getUserRandomQuestion(userEmail);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "GetUserRandomQuestion Email", "GetUserRandomQuestion Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	/*
	 * @Transactional
	 * public Result userPasswordReset() {
	 * log.log(Level.FINE, ">>>> Start");
	 * ObjectNode result = Json.newObject();
	 * Users user = null;
	 * try {
	 * JsonNode json = request.body().asJson();
	 * String userEmail = json.findValue("userEmail").asText();
	 * String token = json.findValue("token").asText();
	 * String locHash = json.findValue("locHash").asText();
	 * if (null != userEmail && null != token) {
	 * String query = "SELECT obj FROM Users obj WHERE obj.email = '" + userEmail +
	 * "' AND obj.resetPasswordToken = '" + token + "'";
	 * List<Users> users = genericDAO.executeSimpleQueryWithLimit(query,
	 * getEntityManager(), 1);
	 * if (!users.isEmpty() && users.size() > 0) {
	 * user = users.get(0);
	 * if (null != user) {
	 * result = AccountSettingService.getUserRandomQuestion(userEmail);
	 * result.put("locHash", locHash);
	 * } else {
	 * result.put("message", false);
	 * }
	 * }
	 * }
	 * } catch (Exception ex) {
	 * log.log(Level.SEVERE, "Error", ex);
	 * // log.log(Level.SEVERE, ex.getMessage());
	 * String strBuff=getStackTraceMessage(ex);
	 * expService.sendExceptionReport(strBuff,"GetUserRandomQuestion Email",
	 * "GetUserRandomQuestion Organization",
	 * Thread.currentThread().getStackTrace()[1].getMethodName());
	 * }
	 * return result;
	 * }
	 */

	@Transactional
	public Result saveUserSecurityAnswers(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("userEmail").asText();
			if (!"".equals(email) && null != email) {

				Http.Session session = request.session();
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user) {
					Long length = json.findValue("length").asLong();
					for (long l = 0; l < length; l++) {
						String answer = json.findValue("answer_" + l).asText();
						if (!"".equals(answer) && null != answer) {
							String question = json.findValue("question_" + l).asText();
							String queryQuestion = question;
							if (question.contains("'")) {
								queryQuestion = question.replaceAll("'", "''");
							}
							String query = "SELECT obj FROM UserProfileSecurity obj WHERE obj.user.id = ?1 AND obj.securityQuestion=?2";
							ArrayList inparams = new ArrayList();
							inparams.add(user.getId());
							inparams.add(queryQuestion);
							List<UserProfileSecurity> securities = genericDAO.queryWithParamsName(query, entityManager,
									inparams);
							UserProfileSecurity security = null;
							if (!securities.isEmpty() && securities.size() > 0) {
								security = securities.get(0);
							} else {
								security = new UserProfileSecurity();
							}
							security.setUser(user);
							security.setOrganization(user.getOrganization());
							security.setSecuredAnswer(answer);
							security.setSecurityQuestion(question);
							entityTransaction.begin();
							genericDAO.saveOrUpdate(security, user, entityManager);
							entityTransaction.commit();
						}
					}
					result.put("result", "success");
					result.put("message", "Security Answers Save Successful.");
				} else {
					result.put("result", "failure");
					result.put("message", "Security Answers Save Unsuccessful.");
				}
			} else {
				result.put("result", "failure");
				result.put("message", "Security Answers Save Unsuccessful.");
			}
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			result.put("result", "failure");
			result.put("message", "Security Answers Save Unsuccessful.");
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "GetUserRandomQuestion Email", "GetUserRandomQuestion Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getCustomerAccountDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			Long orgId = json.findValue("orgId").asLong();
			Integer type = json.findValue("type").asInt();
			result = AccountSettingService.getCustomerAccountDetails(email, orgId, type);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "GetCustomerAccountDetails Email",
					"GetCustomerAccountDetails Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getPhoneCodesAndCountries() {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		try {
			result = AccountSettingService.getPhoneCodesAndCountries();
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "GetPhoneCodesAndCountries Email",
					"GetPhoneCodesAndCountries Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result saveCustomerProfile(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			long id = json.findValue("id").asLong();
			long orgId = json.findValue("orgId").asLong();
			String oldEmail = json.findValue("oldEmail").asText();
			int type = json.findValue("type").asInt();
			String newEmail = json.findValue("newEmail").asText();
			String name = json.findValue("name").asText();
			String address = json.findValue("address").asText();
			int country = json.findValue("country").asInt();
			String location = json.findValue("location").asText();
			String phoneCode = json.findValue("phoneCode").asText();
			String phone = json.findValue("phone").asText();
			result = AccountSettingService.saveCustomerProfile(id, orgId, oldEmail, type, name, newEmail, location,
					address, phoneCode, phone, country);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "SaveCustomerVendorProfile Email",
					"SaveCustomerVendorProfile Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getVendorAccountDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			result = AccountSettingService.getVendorAccountDetails(email);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "GetVendorAccountDetails Email",
					"GetVendorAccountDetails Organization", Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result saveVendorProfile(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		try {
			JsonNode json = request.body().asJson();
			long id = json.findValue("id").asLong();
			String email = json.findValue("email").asText();
			String name = json.findValue("name").asText();
			String phone = json.findValue("phone").asText();
			double reg = json.findValue("reg").asDouble();
			result = AccountSettingService.saveVendorProfile(id, email, name, phone, reg);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "SaveVendorProfile Email", "SaveVendorProfile Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}
}
