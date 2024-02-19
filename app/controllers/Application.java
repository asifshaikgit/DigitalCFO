package controllers;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Level;
import java.io.File;

import javax.mail.Session;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.DateUtil;
import com.idos.util.IdosConstants;

import model.*;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.typesafe.config.ConfigFactory;
import play.mvc.Controller;
import play.db.jpa.JPAApi;

import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import play.mvc.Results;
import service.PasswordChangeService;
import service.SupportTicketService;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;
import views.html.*;
import com.idos.util.CountryCurrencyUtil;
import com.idos.util.PasswordUtil;

public class Application extends StaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public Request request;
	public static Logger log = Logger.getLogger("controllers");
	public static play.Application application;

	@Inject
	public Application(play.Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result registration(Request request) throws Exception {
		log.log(Level.FINE, ">>>>Started ==============");
		Http.Session session = request.session();
		Users user = getUserInfo(request);
		IdosRegisteredVendor idosRegVendor = SellerController.getSellerInfo(entityManager, request);
		if (user == null && idosRegVendor == null) {
			Cookie cookie = request.cookie("user");
			String userCookie = "";
			if (cookie != null) {
				userCookie = cookie.value();
			}
			if (userCookie != null && !userCookie.equals("")) {

				session.adding("email", userCookie);
				user = getUserInfo(request);
				List<UsersRoles> userRoles = user.getUserRoles();
				String role = "";
				for (UsersRoles roles : userRoles) {
					role += roles.getRole().getName() + ",";
				}
				if (role.length() > 0) {
					role = role.substring(0, role.length() - 1);
				}
				Organization orgn = user.getOrganization();
				String allowedProcurement = String.valueOf(user.getAllowedProcurementRequest());
				log.log(Level.FINE, "End1 ==============");
				return Results.ok(config.render(user.getFullName(), user.getEmail(), role, orgn, allowedProcurement,
						user, ConfigParams.getInstance()));
			} else {
				log.log(Level.FINE, "End2 ==============");
				return Results.ok(index.render(ConfigParams.getInstance()));
			}
		} else {
			if (user != null) {
				idosRegVendor = null;
				String role = "";
				session.adding("email", user.getEmail());
				session.adding("selleremail", "");
				Users usrinfo = getUserInfo(request);
				if (usrinfo.getInSession() == 1) {
					List<UsersRoles> userRoles = usrinfo.getUserRoles();
					for (UsersRoles roles : userRoles) {
						role += roles.getRole().getName() + ",";
					}
					if (role.length() > 0) {
						role = role.substring(0, role.length() - 1);
					}
					Organization orgn = usrinfo.getOrganization();
					String allowedProcurement = String.valueOf(usrinfo.getAllowedProcurementRequest());
					log.log(Level.FINE, "End3 ==============");
					return Results.ok(config.render(usrinfo.getFullName(), usrinfo.getEmail(), role, orgn,
							allowedProcurement, usrinfo, ConfigParams.getInstance()));
				} else {
					log.log(Level.FINE, "End4 ==============");
					return Results.ok(index.render(ConfigParams.getInstance()));
				}
			}
			if (idosRegVendor != null) {
				session.adding("email", "");
				session.adding("selleremail", idosRegVendor.getVendorEmail());
				user = null;
				String role = null;
				String allowedProcurement = null;
				Organization orgn = null;
				log.log(Level.FINE, "End5 ==============");
				return Results.ok(seller.render(idosRegVendor.getVendorName(), idosRegVendor.getVendorEmail(), role,
						orgn, allowedProcurement, ConfigParams.getInstance()));
			} else {
				log.log(Level.FINE, "End6 ==============");
				return Results.ok(index.render(ConfigParams.getInstance()));
			}
		}
	}

	@Transactional
	public Result allow() {
		log.log(Level.FINE, "=========Start==============");
		Result result = Results.ok();
		result.withHeader("Access-Control-Allow-Origin", "*");
		result.withHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
		result.withHeader("Access-Control-Max-Age", "300");
		result.withHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token,Token");
		return result;
	}

	@Transactional
	public Result configUserInSession(Request request) throws ClassNotFoundException, IOException {
		log.log(Level.FINE, "=========Start==============");

		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		ArrayNode configUserInSession = result.putArray("configUserInSessionData");
		String email = session.getOptional("email").orElse("");
		ObjectNode row = Json.newObject();
		row.put("sessionemail", email);
		configUserInSession.add(row);
		log.log(Level.FINE, "=========Return ==============" + result.asText());
		return Results.ok(result);
	}

	@Transactional
	public Result sellReceive(Request request) {
		log.log(Level.FINE, ">>>>Start==============");
		// EntityManager entityManager = getEntityManager();
		ObjectNode results = Json.newObject();
		ArrayNode sellReceiveDataan = results.putArray("sellReceiveData");
		ArrayNode userInSessionan = results.putArray("userInSessionData");
		Users user = getUserInfo(request);
		StringBuilder sb = new StringBuilder(
				"select obj from UsersRoles obj where obj.user.id=?1 and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id");
		ArrayList inparams = new ArrayList();
		inparams.add(user.getId());
		List<UsersRoles> userRoles = genericDAO.queryWithParams(sb.toString(), entityManager, inparams);
		String roles = "";
		for (UsersRoles role : userRoles) {
			if (!role.getRole().getName().equals("OFFICERS")) {
				roles += role.getRole().getName() + ",";
			}
		}
		if (roles.length() > 0) {
			roles = roles.substring(0, roles.length() - 1);
		}

		if (user != null) {
			try {
				List<UserTransactionPurpose> listOftxnPurpose = user.getUserTxnQuestions();
				if (roles.contains("CREATOR")) {
					for (UserTransactionPurpose txnPurpose : listOftxnPurpose) {
						ObjectNode row = Json.newObject();
						if (!(ConfigParams.getInstance().isDeploymentSingleUser(user)
								&& txnPurpose.getTransactionPurpose()
										.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER)) {
							row.put("id", txnPurpose.getTransactionPurpose().getId());
							row.put("name", txnPurpose.getTransactionPurpose().getTransactionPurpose());
							sellReceiveDataan.add(row);
						}
					}
				}
				if (roles.contains("ACCOUNTANT") || roles.contains("AUDITOR")) {
					ObjectNode row1 = Json.newObject();
					row1.put("id", 20);
					row1.put("name", "Make Provision/Journal Entry");
					sellReceiveDataan.add(row1);
				}
				ObjectNode userrow = Json.newObject();
				userrow.put("userinsession", "userinsession");
				userInSessionan.add(userrow);
			} catch (Exception ex) {
				log.log(Level.SEVERE, "Error", ex);
				String strBuff = getStackTraceMessage(ex);
				expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName());
				List<String> errorList = getStackTrace(ex);
				return Results.ok(errorPage.render(ex, errorList));
			}
		} else {
			ObjectNode userrow = Json.newObject();
			userrow.put("userinsession", "usernotinsession");
			userInSessionan.add(userrow);
		}
		log.log(Level.FINE, ">>>> End " + results);
		return Results.ok(results);
	}

	@Transactional
	public Result getsellReceive(Request request) {
		// EntityManager entityManager = getEntityManager();
		ObjectNode results = Json.newObject();
		System.out.println(">>>>>>>" + request);
		Users user = getUserInfo(request);
		ArrayNode sellReceiveDataan = results.putArray("sellReceiveData");
		try {

			List<TransactionPurpose> listOftxnPurpose = null;
			if (ConfigParams.getInstance().isDeploymentSingleUser(user)) {
				listOftxnPurpose = TransactionPurpose.getSingleUserTransactionPurpose(entityManager);
			} else {
				Map<String, Object> criterias = new HashMap<String, Object>();
				criterias.put("questionType", 1);
				criterias.put("presentStatus", 1);
				listOftxnPurpose = genericDAO.findByCriteria(TransactionPurpose.class, criterias, entityManager);
			}
			if (listOftxnPurpose != null) {
				for (TransactionPurpose txnPurpose : listOftxnPurpose) {
					ObjectNode row = Json.newObject();
					row.put("id", txnPurpose.getId());
					row.put("name", txnPurpose.getTransactionPurpose());
					sellReceiveDataan.add(row);
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
		return Results.ok(results);
	}

	@Transactional
	public Result resendActivationLink(Request request) {
		log.log(Level.FINE, "=========Start==============");

		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		ArrayNode resendactivationresponsedata = results.putArray("resendactivationresponsedata");
		Users usr = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			usr = Users.findActiveByEmail(useremail);
			String result = "";
			if (usr != null) {
				String password = PasswordUtil.gen(10);
				usr.setPassword(PasswordUtil.encrypt(password));
				genericDAO.saveOrUpdate(usr, usr, entityManager);
				result = "Account activation link sent.Please check your email and follow the link to activate the account.";
				String body1 = accountCreationLink.render(password, useremail, ConfigParams.getInstance()).body();
				final String username1 = ConfigFactory.load().getString("smtp.user");
				String subject1 = "Activate Your Account";
				Session session1 = emailsession;
				mailTimer(body1, username1, session1, useremail, null, subject1);
				ObjectNode row = Json.newObject();
				row.put("result", result);
				resendactivationresponsedata.add(row);
			}
			if (usr == null) {
				result = "Not able to find entered email.Please try again.";
				ObjectNode row = Json.newObject();
				row.put("result", result);
				resendactivationresponsedata.add(row);
			}
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usr.getEmail(), usr.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	/*
	 * @Transactional
	 * public Result forgotPassword() throws ClassNotFoundException,IOException {
	 * log.log(Level.FINE, "=========Start==============");
	 * String fullName=null;
	 * String email=null;
	 * String role=null;
	 * String allowedProcurement=null;
	 * Organization orgn=null;
	 * String pwdchanged=session.getOptional("pwdchanged").orElse("");
	 * session.removing();
	 * return
	 * Results.ok(registration.render(fullName,email,role,orgn,allowedProcurement,
	 * pwdchanged));
	 * //return
	 * Results.ok(forgotpwd.render(fullName,email,role,orgn,allowedProcurement,
	 * pwdchanged));
	 * }
	 */

	@Transactional
	public Result subscriberlogin() {
		log.log(Level.FINE, "=========Start==============");

		String fullName = null;
		String email = null;
		String role = null;
		String allowedProcurement = null;
		Organization orgn = null;
		return Results.ok(subscriberlogin.render(fullName, email, role, orgn, allowedProcurement));
	}

	@Transactional
	public Result branchalertsconfiguration() {
		log.log(Level.FINE, "=========Start==============");

		String fullName = null;
		String pageemail = null;
		String role = null;
		String allowedProcurement = null;
		Organization orgn = null;
		try {
			return Results.ok(branchAlertsAction.render(fullName, pageemail, role, orgn, allowedProcurement,
					ConfigParams.getInstance()));
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Branch Alerts Configuration Email",
					"Branch Alerts Configuration Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	@Transactional
	public Result branchstatalertsconfiguration() {
		log.log(Level.FINE, "=========Start==============");

		String fullName = null;
		String pageemail = null;
		String role = null;
		String allowedProcurement = null;
		Organization orgn = null;
		try {
			return Results.ok(branchAlertsAction.render(fullName, pageemail, role, orgn, allowedProcurement,
					ConfigParams.getInstance()));
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Branch Stat Alerts Configuration Email",
					"Branch Stat Alerts Configuration Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	@Transactional
	public Result branchoperremalertsconfiguration() {
		log.log(Level.FINE, "=========Start==============");

		String fullName = null;
		String pageemail = null;
		String role = null;
		String allowedProcurement = null;
		Organization orgn = null;
		try {
			return Results.ok(branchAlertsAction.render(fullName, pageemail, role, orgn, allowedProcurement,
					ConfigParams.getInstance()));
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Branch Operational Reminder Alerts Configuration Email",
					"Branch Operational Reminder Alerts Configuration Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	@Transactional
	public Result branchpolicyalertsconfiguration() {
		log.log(Level.FINE, "=========Start==============");

		String fullName = null;
		String pageemail = null;
		String role = null;
		String allowedProcurement = null;
		Organization orgn = null;
		try {
			return Results.ok(branchAlertsAction.render(fullName, pageemail, role, orgn, allowedProcurement,
					ConfigParams.getInstance()));
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Branch Policy Alerts Configuration Email",
					"Branch Policy Alerts Configuration Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	@Transactional
	public Result saveUserInfoInSession(Request request) {
		log.log(Level.FINE, "=========Start==============");

		Http.Session session = request.session();
		String useremail = null;
		try {
			JsonNode json = request.body().asJson();
			useremail = json.findValue("usermail").asText();
			if (!useremail.equals("") && useremail != null) {
				session.adding("email", useremail);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, useremail, "Save User Info Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok();
	}

	public static void mapChartOfAccountWithAllBranches(Http.Request request, Organization orgn) {
		log.log(Level.FINE, "=========Start==============");

		List<Branch> orgBranchList = orgn.getBranches();
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		if (orgBranchList.size() > 0) {
			for (Branch mapBranch : orgBranchList) {
				Map<String, Object> criterias = new HashMap<String, Object>();
				// criterias.put("accountCode", 2000000000000000000L); //for expense coa mapping
				// to all branch for budget testing
				criterias.put("accountCode", 1000000000000000000L); // for income coa mapping to all branch for tax
																	// setup
				criterias.put("organization.id", orgn.getId());
				criterias.put("presentStatus", 1);
				Particulars parts = genericDAO.getByCriteria(Particulars.class, criterias, entityManager);
				criterias.clear();
				criterias.put("particularsId.id", parts.getId());
				criterias.put("organization.id", orgn.getId());
				criterias.put("presentStatus", 1);
				List<Specifics> expenseSpecificsList = genericDAO.findByCriteria(Specifics.class, criterias,
						entityManager);
				for (Specifics specf : expenseSpecificsList) {
					BranchSpecifics newBranchSpecifics = new BranchSpecifics();
					newBranchSpecifics.setBranch(mapBranch);
					newBranchSpecifics.setOrganization(mapBranch.getOrganization());
					newBranchSpecifics.setSpecifics(specf);
					newBranchSpecifics.setParticular(specf.getParticularsId());
					genericDAO.saveOrUpdate(newBranchSpecifics, getUserInfo(request), entityManager);
				}
			}
		}
		transaction.commit();
	}

	@Transactional
	public Result cashconfiguration(Request request) {
		log.log(Level.FINE, "=========Start==============");

		Http.Session session = request.session();
		Users usrinfo = null;
		try {
			String role = session.getOptional("role").orElse("");
			usrinfo = getUserInfo(request);
			if (usrinfo == null) {
				return Results.ok(index.render(ConfigParams.getInstance()));
			} else {
				Organization orgn = usrinfo.getOrganization();
				String allowedProcurement = String.valueOf(usrinfo.getAllowedProcurementRequest());

				return Results.ok(config.render(usrinfo.getFullName(), usrinfo.getEmail(), role, orgn,
						allowedProcurement, usrinfo, ConfigParams.getInstance()));
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	@Transactional
	public Result account(Request request) {
		log.log(Level.FINE, "=========Start==============");

		Http.Session session = request.session();
		Users usrinfo = null;
		try {
			String role = session.getOptional("role").orElse("");
			usrinfo = getUserInfo(request);
			if (usrinfo == null) {
				return Results.ok(index.render(ConfigParams.getInstance()));
			} else {
				String allowedProcurement = String.valueOf(usrinfo.getAllowedProcurementRequest());
				Organization orgn = usrinfo.getOrganization();
				return Results.ok(config.render(usrinfo.getFullName(), usrinfo.getEmail(), role, orgn,
						allowedProcurement, usrinfo, ConfigParams.getInstance()));
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, usrinfo.getEmail(), usrinfo.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
	}

	/*
	 * @Transactional
	 * public Result vendCustAccount(){
	 * log.log(Level.FINE, "=========Start==============");
	 * 
	 * String fullName=null;
	 * String email=null;
	 * String role=null;
	 * String allowedProcurement=null;
	 * Organization orgn=null;
	 * String pwdchanged=null;
	 * try{
	 * Cookie cookie = request.cookie("vendorCustomer");
	 * if (null != cookie) {
	 * response().discardCookie("vendorCustomer");
	 * }
	 * }catch(Exception ex){
	 * log.log(Level.SEVERE, "Error", ex);
	 * // log.log(Level.SEVERE, ex.getMessage());
	 * String strBuff=getStackTraceMessage(ex);
	 * expService.sendExceptionReport(strBuff,"Vendor Customer Account Email",
	 * "Vendor Customer Account Configuration",
	 * Thread.currentThread().getStackTrace()[1].getMethodName());
	 * List<String> errorList=getStackTrace(ex);
	 * return Results.ok(errorPage.render(ex,errorList));
	 * }
	 * return
	 * Results.ok(registration.render(fullName,email,role,orgn,allowedProcurement,
	 * pwdchanged));
	 * }
	 */
	@Transactional
	public Result currency(Request request) {
		log.log(Level.FINE, "=========Start==============");

		// EntityManager entityManager = getEntityManager();
		ObjectNode results = Json.newObject();
		ArrayNode an = results.putArray("currencyData");
		Users user = getUserInfo(request);
		try {
			JsonNode json = request.body().asJson();
			String countryName = json.findValue("countryName").asText();
			Map<String, String> currencies = CountryCurrencyUtil.getAvailableCurrencies();
			String currency = currencies.get(countryName);
			ObjectNode row = Json.newObject();
			row.put("currency", currency);
			an.add(row);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result sellerLogin(Request request) {
		log.log(Level.FINE, "=========Start==============");

		// EntityManager entityManager = getEntityManager();
		String result = "";
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			String pass = json.findValue("password").asText();
			pass = PasswordUtil.encrypt(pass);
			IdosRegisteredVendor vendor = IdosRegisteredVendor.findByEmailAddressAndPassword(entityManager, email,
					pass);
			if (null != vendor) {
				if (1 == vendor.getPresentStatus()) {
					result = "success";
				} else {
					result = "activate";
				}
			} else {
				result = "failure";
			}
		} catch (Exception ex) {
			result = "failure";
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Activate Seller Email", "Activate Seller Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			// log.log(Level.SEVERE, ex.getMessage());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result indexSupport(Request request) {
		log.log(Level.FINE, "=========Start==============");

		String result = "failure";
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			String sub = json.findValue("sub").asText();
			String msg = json.findValue("msg").asText();
			ObjectNode on = SupportTicketService.createSupportTicket(null, sub, msg, null, null, null, entityManager);
			if (on.findValue("result").asBoolean()) {
				result = "success";
				sendMailWithMultipleAttachment(emailsession, sub + "-" + email, new HashMap<String, File>(),
						"support@myidos.com", new String[0]);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Activate Seller Email", "Activate Seller Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result channelPartnerSupport(Request request) {
		log.log(Level.FINE, "=========Start==============");

		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			String sub = json.findValue("sub").asText();
			String msg = json.findValue("msg").asText();
			String name = json.findValue("name").asText();
			String phone = json.findValue("phone").asText();
			String body = channelPartner.render(email, sub, phone, msg, name, ConfigParams.getInstance()).body();
			mailTimer(body, email, emailsession, "support@myidos.com", null, "Request For Channel Partner");
			result.put("result", true);
			result.put("message",
					"Request for channel partnership has been done. Please wait while we get back to you.");
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Activate Seller Email", "Activate Seller Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result trialbalanceallow() {
		log.log(Level.FINE, "=========Start==============");
		Result result = Results.ok();
		result.withHeader("Access-Control-Allow-Origin", "*");
		result.withHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
		result.withHeader("Access-Control-Max-Age", "300");
		result.withHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token,Token");
		return result;
	}

	@Transactional
	public Result gettrialbalanceallow() {
		log.log(Level.FINE, "=========Start==============");
		Result result = Results.ok();
		result.withHeader("Access-Control-Allow-Origin", "*");
		result.withHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
		result.withHeader("Access-Control-Max-Age", "300");
		result.withHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token,Token");
		return result;
	}

	@Transactional
	public Result budgetallow() {
		log.log(Level.FINE, "=========Start==============");
		Result result = Results.ok();
		result.withHeader("Access-Control-Allow-Origin", "*");
		result.withHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
		result.withHeader("Access-Control-Max-Age", "300");
		result.withHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token,Token");
		return Results.ok();
	}

	@Transactional
	public Result recruitmentallowExcel() {
		log.log(Level.FINE, "=========Start==============");
		Result result = Results.ok();
		result.withHeader("Access-Control-Allow-Origin", "*");
		result.withHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
		result.withHeader("Access-Control-Max-Age", "300");
		result.withHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token,Token");
		return Results.ok();
	}

	@Transactional
	public Result recruitmentallowJSON() {
		log.log(Level.FINE, "=========Start==============");
		Result result = Results.ok();
		result.withHeader("Access-Control-Allow-Origin", "*");
		result.withHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
		result.withHeader("Access-Control-Max-Age", "300");
		result.withHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token,Token");
		return Results.ok();
	}

	@Transactional
	public Result subscribe(Request request) {
		log.log(Level.FINE, "=========Start==============");
		// EntityManager em = getEntityManager();
		EntityTransaction et = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		result.put("result", false);
		result.put("message", "Oops! Something went wrong. Please try again later.");
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				StringBuilder query = new StringBuilder();
				query.append("SELECT obj FROM NewsFeatureSubscriberEmail obj WHERE obj.email = ?");
				ArrayList inparams = new ArrayList(1);
				inparams.add(email);
				List<NewsFeatureSubscriberEmail> nfsEmails = genericDAO.queryWithParams(query.toString(), entityManager,
						inparams);
				NewsFeatureSubscriberEmail nfsEmail = null;
				if (nfsEmails.isEmpty() && nfsEmails.size() == 0) {
					nfsEmail = new NewsFeatureSubscriberEmail();
					nfsEmail.setEmail(email);
					et.begin();
					genericDAO.saveOrUpdate(nfsEmail, null, entityManager);
					et.commit();
					result.put("result", true);
					result.put("message", "Your email has been registered for IDOS newsletters.");
				} else {
					result.put("message", "Your email has already been registered for IDOS newsletters.");
				}
			}
		} catch (Exception ex) {
			if (null != et && et.isActive()) {
				et.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Subscribe Email", "Subscribe Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result generateKey(Request request) {
		ObjectNode on = null;
		try {
			JsonNode json = request.body().asJson();
			String org = json.findValue("org").asText();
			String email = json.findValue("email").asText();
			String pName = json.findValue("pName").asText();
			String url = json.findValue("url").asText() != "" ? json.findValue("url").asText() : null;
			String phone = json.findValue("phone").asText() != "" ? json.findValue("phone").asText() : null;
			String note = json.findValue("note").asText() != "" ? json.findValue("note").asText() : null;
			on = APPLICATION_SERVICE.generateKey(org, pName, email, url, phone, note);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Generate Key", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Generate Key", "Generate Key",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(on);
	}

	@Transactional
	public Result activateApiAccess(Request request, String email, String key) {
		log.log(Level.FINE, "=====>>===Start==============");

		String res = null;
		try {
			JsonNode json = request.body().asJson();
			res = APPLICATION_SERVICE.activateApiAccess(email, key);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Generate Key", "Generate Key",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(res);
	}

	@Transactional
	public Result resetUserAccount(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users currentUser = getUserInfo(request);
		if (currentUser == null) {
			return unauthorized();
		}
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("resetinfocredential");
			String accountEmail = json.findValue("resetemail").asText();
			String accountNewPwd = json.findValue("resetpassword").asText();
			int activatation = json.findValue("activatation").asInt();
			List<Users> users = Users.findByEmailActDeact(entityManager, accountEmail);
			if (users.size() > 0) {
				Users user = users.get(0);
				if (!PasswordChangeService.isPresentInPasswordHistory(PasswordUtil.encrypt(accountNewPwd), user,
						entityManager)) {
					user.setPassword(PasswordUtil.encrypt(accountNewPwd));
					user.setPresentStatus(activatation);
					user.setFailedAttempt(0);
					Calendar cal = Calendar.getInstance();
					Date date = DateUtil.mysqldf.parse(DateUtil.mysqldf.format(cal.getTime()));
					user.setLastUpdatedPasswordDate(date);
					usercrud.save(user, users.get(0), entityManager);
					PasswordHistory history = new PasswordHistory(user.getEmail(), user, user.getOrganization(),
							PasswordUtil.encrypt(accountNewPwd));
					genericDAO.saveOrUpdate(history, user, entityManager);
					if (!ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
						try {
							final String username = ConfigFactory.load().getString("smtp.user");
							String body = accountReset
									.render(users.get(0).getEmail(), accountNewPwd, ConfigParams.getInstance()).body();
							Session session = emailsession;
							Email mail = new HtmlEmail();
							mail.setMailSession(session);
							mail.setFrom(username);
							mail.addTo(users.get(0).getEmail());
							mail.setSubject("Login Details");
							mail.setSentDate(new Date());
							mail.setMsg(body);
							mail.send();
						} catch (EmailException ex) {
							log.log(Level.SEVERE, "Error", ex);
						}
					}
					ObjectNode row = Json.newObject();
					row.put("message", "Account reset successfully.");
					row.put("result", true);
					an.add(row);
				} else {
					ObjectNode row = Json.newObject();
					row.put("message", "Password Already Used.");
					row.put("result", false);
					an.add(row);
				}
			} else {
				ObjectNode row = Json.newObject();
				row.put("message", "Not Able To Locate Email.");
				row.put("result", false);
				an.add(row);
			}
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, currentUser.getEmail(), ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, currentUser.getEmail(), "ResetPassword Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

}
