package service;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.CountryPhoneCode;
import model.IdosRegisteredVendor;
import model.UserProfileSecurity;
import model.Users;
import model.Vendor;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

import com.idos.dao.GenericDAO;
import com.idos.util.CountryCurrencyUtil;
import com.idos.util.RandomSecurityQuestion;

import controllers.StaticController;

public class AccountSettingService implements BaseService {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public AccountSettingService(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
		entityManager = EntityManagerProvider.getEntityManager();
	}

	public static ObjectNode getUserRandomQuestion(final String userEmail) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = entityManager;
		ObjectNode result = Json.newObject();
		result.put("question", "");
		result.put("questionId", "");
		result.put("message", true);
		result.put("email", userEmail);
		if (null != userEmail) {
			String query = "SELECT obj FROM Users obj WHERE obj.email = '" + userEmail + "'";
			List<Users> users = genericDAO.executeSimpleQueryWithLimit(query, entityManager, 1);
			if (!users.isEmpty() && users.size() > 0) {
				query = "SELECT obj FROM UserProfileSecurity obj WHERE obj.user.id = " + users.get(0).getId();
				List<UserProfileSecurity> profileSecurities = genericDAO.executeSimpleQuery(query, entityManager);
				if (!profileSecurities.isEmpty() && profileSecurities.size() > 0) {
					String question = RandomSecurityQuestion.returnRandomQuestion(profileSecurities);
					if (null != question) {
						for (UserProfileSecurity security : profileSecurities) {
							if (question.equals(security.getSecurityQuestion())) {
								result.put("question", question);
								result.put("questionId", security.getId());
							}
						}
					}
				}
			}
		}
		return result;
	}

	public static ObjectNode getCustomerAccountDetails(final String email, final Long orgId, final Integer type) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		if ((null != email && !"".equals(email) && (null != orgId && !"".equals(orgId))
				&& (null != type && !"".equals(type)))) {
			StringBuilder query = new StringBuilder();
			query.append("SELECT obj FROM Vendor obj WHERE obj.type = ").append(type);
			query.append(" AND obj.email = '").append(email).append("'");
			query.append(" AND obj.organization.id = ").append(orgId);
			List<Vendor> vendors = genericDAO.executeSimpleQueryWithLimit(query.toString(), entityManager, 1);
			if (!vendors.isEmpty() && vendors.size() > 0) {
				Vendor vendor = vendors.get(0);
				if (null != vendor) {
					result.put("result", true);
					if (null != vendor.getId()) {
						result.put("id", vendor.getId());
						if (null != vendor.getEmail()) {
							result.put("email", vendor.getEmail());
						} else {
							result.put("email", "");
						}
						if (null != vendor.getName()) {
							result.put("name", vendor.getName());
						} else {
							result.put("name", "");
						}
						if (null != vendor.getCountry()) {
							result.put("country", vendor.getCountry());
						} else {
							result.put("country", "");
						}
						if (null != vendor.getAddress()) {
							result.put("address", vendor.getAddress());
						} else {
							result.put("address", "");
						}
						if (null != vendor.getLocation()) {
							result.put("location", vendor.getLocation());
						} else {
							result.put("location", "");
						}
						if (null != vendor.getPhone()) {
							result.put("phone", vendor.getPhone());
							String phone = vendor.getPhone();
							if (phone.contains("-") && phone.length() > 9) {
								phone = phone.substring(phone.lastIndexOf("-") + 1, phone.length());
								result.put("phone1", phone.substring(0, 3));
								result.put("phone2", phone.substring(3, 6));
								result.put("phone3", phone.substring(6, 10));
							} else {
								result.put("phone", phone);
							}
						} else {
							result.put("phone", "");
						}
						if (null != vendor.getPhoneCtryCode()) {
							result.put("phoneCode", vendor.getPhoneCtryCode());
						} else {
							result.put("phoneCode", "");
						}
						if (null != vendor.getOrganization()) {
							result.put("org", vendor.getOrganization().getName());
						} else {
							result.put("org", "");
						}
					}
				}
			}
		}
		return result;
	}

	public static ObjectNode getPhoneCodesAndCountries() {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		try {
			result.put("result", false);
			result.put("message", "Problem in fetching phone codes. Please try again later.");
			List<CountryPhoneCode> codes = genericDAO.findAll(CountryPhoneCode.class, true, false, entityManager);
			if (!codes.isEmpty()) {
				ArrayNode an = result.putArray("codes");
				ObjectNode row = null;
				result.put("result", true);
				result.remove("message");
				for (CountryPhoneCode code : codes) {
					if (null != code) {
						row = Json.newObject();
						if (null != code.getAreaCode()) {
							row.put("code", code.getAreaCode());
						}
						if (null != code.getCountryWithCode()) {
							row.put("country", code.getCountryWithCode());
						}
						an.add(row);
					}
				}
				an = result.putArray("countries");
				Map<String, String> countries = CountryCurrencyUtil.getCountries();
				for (Map.Entry<String, String> country : countries.entrySet()) {
					if (null != country) {
						row = Json.newObject();
						row.put("code", country.getKey());
						row.put("country", country.getValue());
					}
					an.add(row);
				}
			}
		} catch (Exception e) {
		}
		return result;
	}

	public static ObjectNode saveCustomerProfile(final Long id, final Long orgId, final String oldEmail,
			final Integer type, final String name, final String newEmail, final String location, final String address,
			final String phoneCode, final String phone, final Integer country) throws Exception {
		ObjectNode result = Json.newObject();
		result.put("result", false);
		// EntityManager manager = entityManager;
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			log.log(Level.FINE, ">>>> Start");

			if ((null != id && !"".equals(id)) && (null != orgId && !"".equals(orgId))
					&& (null != oldEmail && !"".equals(oldEmail)) && (null != type && !"".equals(type))) {
				StringBuilder query = new StringBuilder();
				query.append("SELECT obj FROM Vendor obj WHERE obj.type = ").append(type);
				query.append(" AND obj.email = '").append(oldEmail).append("'");
				query.append(" AND obj.organization.id = ").append(orgId);
				query.append(" AND obj.id = ").append(id);
				List<Vendor> vendors = genericDAO.executeSimpleQueryWithLimit(query.toString(), entityManager, 1);
				if (!vendors.isEmpty() && vendors.size() > 0) {
					Vendor vendor = vendors.get(0);
					if (null != vendor) {
						if (!"".equals(newEmail)) {
							vendor.setEmail(newEmail);
						} else {
							vendor.setEmail(null);
						}
						if (!"".equals(name)) {
							vendor.setName(name);
						} else {
							vendor.setName(null);
						}
						if (!"".equals(country)) {
							vendor.setCountry(country);
						} else {
							vendor.setCountry(null);
						}
						if (!"".equals(phone)) {
							vendor.setPhone(phone);
						} else {
							vendor.setPhone(null);
						}
						if (!"".equals(phoneCode)) {
							vendor.setPhoneCtryCode(phoneCode);
						} else {
							vendor.setPhoneCtryCode(null);
						}
						if (!"".equals(location)) {
							vendor.setLocation(location);
						} else {
							vendor.setLocation(null);
						}
						if (!"".equals(address)) {
							vendor.setAddress(address);
						} else {
							vendor.setAddress(null);
						}
						transaction.begin();
						genericDAO.saveOrUpdate(vendor, null, entityManager);
						transaction.commit();
						result = getCustomerAccountDetails(newEmail, orgId, type);
					}
				}
			}
		} catch (Exception ex) {
			if (null != transaction && transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			throw ex;
		}
		return result;
	}

	public static ObjectNode getVendorAccountDetails(final String email) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		if (null != email && !"".equals(email)) {
			StringBuilder query = new StringBuilder();
			query.append("SELECT obj FROM IdosRegisteredVendor obj WHERE obj.vendorEmail = '").append(email)
					.append("'");
			List<IdosRegisteredVendor> vendors = genericDAO.executeSimpleQueryWithLimit(query.toString(), entityManager,
					1);
			if (!vendors.isEmpty() && vendors.size() > 0) {
				IdosRegisteredVendor vendor = vendors.get(0);
				if (null != vendor) {
					if (null != vendor.getId()) {
						result.put("result", true);
						long diff = 0;
						result.put("id", vendor.getId());
						if (null != vendor.getVendorEmail()) {
							result.put("email", vendor.getVendorEmail());
						}
						if (null != vendor.getVendorName()) {
							result.put("name", vendor.getVendorName());
						} else {
							result.put("name", "");
						}
						if (null != vendor.getVendorRegistrationNumber()) {
							result.put("reg", vendor.getVendorRegistrationNumber());
						} else {
							result.put("reg", "");
						}
						if (null != vendor.getVendorPhoneNumber()) {
							result.put("phone", vendor.getVendorPhoneNumber());
							String phone = vendor.getVendorPhoneNumber();
							if (phone.contains("-")) {
								result.put("phoneCode", phone.substring(0, phone.lastIndexOf("-")));
								phone = phone.substring(phone.lastIndexOf("-") + 1, phone.length());
								result.put("phone1", phone.substring(0, 3));
								result.put("phone2", phone.substring(3, 6));
								result.put("phone3", phone.substring(6, 10));
							}
						} else {
							result.put("phone", "");
						}
						if (null != vendor.getLastLoginDate()) {
							result.put("lastLogin", StaticController.idosdf.format(vendor.getLastLoginDate()));
							diff = PasswordChangeService.passwordChangeDifference(vendor.getLastLoginDate());
							result.put("lastLoginDays", diff);
						} else {
							result.put("lastLogin", "");
							result.put("lastLoginDays", "");
						}
						if (null != vendor.getLastUpdatedPassword()) {
							result.put("lastPwdChange",
									StaticController.idosdf.format(vendor.getLastUpdatedPassword()));
							diff = PasswordChangeService.passwordChangeDifference(vendor.getLastUpdatedPassword());
							result.put("lastPwdChangeDays", diff);
						} else {
							result.put("lastPwdChange", "");
							result.put("lastPwdChangeDays", "");
						}
					}
				}
			}
		}
		return result;
	}

	public static ObjectNode saveVendorProfile(final Long id, final String email, final String name, final String phone,
			final Double reg) throws Exception {
		ObjectNode result = Json.newObject();
		result.put("result", false);
		// EntityManager manager = entityManager;
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			log.log(Level.FINE, ">>>> Start");
			if ((null != id && !"".equals(id)) && (null != email && !"".equals(email))) {
				StringBuilder query = new StringBuilder();
				query.append("SELECT obj FROM IdosRegisteredVendor obj WHERE obj.vendorEmail = '").append(email)
						.append("'");
				query.append(" AND obj.id = ").append(id);
				List<IdosRegisteredVendor> vendors = genericDAO.executeSimpleQueryWithLimit(query.toString(),
						entityManager, 1);
				if (!vendors.isEmpty() && vendors.size() > 0) {
					IdosRegisteredVendor vendor = vendors.get(0);
					if (null != vendor) {
						if (!"".equals(name)) {
							vendor.setVendorName(name);
						} else {
							vendor.setVendorName(null);
						}
						if (!"".equals(phone)) {
							vendor.setVendorPhoneNumber(phone);
						} else {
							vendor.setVendorPhoneNumber(null);
						}
						if (!"".equals(reg)) {
							vendor.setVendorRegistrationNumber(reg);
						} else {
							vendor.setVendorRegistrationNumber(null);
						}
						transaction.begin();
						genericDAO.saveOrUpdate(vendor, null, entityManager);
						transaction.commit();
						result = getVendorAccountDetails(email);
					}
				}
			}
		} catch (Exception ex) {
			if (null != transaction && transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			throw ex;
		}
		return result;
	}
}
