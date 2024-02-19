package controllers;

import java.util.*;
import java.io.File;

import javax.mail.Session;
import javax.persistence.*;
import javax.persistence.EntityManager;
import com.idos.util.IdosConstants;
import model.*;
import java.util.logging.Level;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.idos.util.CookieUtils;
import com.typesafe.config.ConfigFactory;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Http.MultipartFormData;
import service.EntityManagerProvider;
import service.SellerService;
import service.SellerServiceImpl;
import views.html.*;

import com.idos.util.PasswordUtil;
import javax.inject.Inject;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;

public class SellerController extends SellerStaticController {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	// private Request request;
	// private Http.Session session = request.session();

	private static final SellerService SELLER_SERVICE = new SellerServiceImpl();

	public SellerController() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result resetSellerAcount(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = result.putArray("resetinfocredential");
			String accountEmail = json.findValue("resetemail").asText();
			String accountNewPwd = json.findValue("resetpassword").asText();
			IdosRegisteredVendor regVend = IdosRegisteredVendor.findByEmailAddress(entityManager, accountEmail);
			if (regVend != null) {
				regVend.setVendorAccountPassword(PasswordUtil.encrypt(accountNewPwd));
				genericDAO.saveOrUpdate(regVend, null, entityManager);
				if (!ConfigParams.getInstance().getIsMailOff(IdosConstants.MAIL_SYSTEM_OFF_KEY)) {
					try {
						final String username = ConfigFactory.load().getString("smtp.user");
						String body = accountReset
								.render(regVend.getVendorEmail(), accountNewPwd, ConfigParams.getInstance()).body();
						Session session = emailsession;
						Email mail = new HtmlEmail();
						mail.setMailSession(session);
						mail.setFrom(username);
						mail.addTo(regVend.getVendorEmail());
						mail.setSubject("Login Details");
						mail.setSentDate(new Date());
						mail.setMsg(body);
						mail.send();
					} catch (EmailException ex) {
						log.log(Level.SEVERE, "Error", ex);
					}
				}
				ObjectNode row = Json.newObject();
				row.put("message", "Account Reset successfully.");
				an.add(row);
			} else {
				ObjectNode row = Json.newObject();
				row.put("message", "Not Able To Locate Seller Email.");
				an.add(row);
			}
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "resetSellerAcount Email", "resetSellerAcount Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result sellerLogout(Http.Request request) {
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
		ObjectNode results = Json.newObject();
		try {
			IdosRegisteredVendor idosRegSeller = getSellerInfo(entityManager, request);
			if (idosRegSeller != null) {
				entitytransaction.begin();
				idosRegSeller.setRegVendInSession(0);
				idosRegSeller.setAuthToken(null);
				genericDAO.saveOrUpdate(idosRegSeller, null, entityManager);
				entitytransaction.commit();
			}
			CookieUtils.discardCookie("user");
			CookieUtils.discardCookie(IdosConstants.AUTH_TOKEN);
			session.removing();
			results.put("logout", idosRegSeller.getVendorEmail());

		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "sellerLogout Email", "sellerLogout Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End");
		// return
		// Results.ok(logoutsllr.render(fullName,email,role,orgn,allowedProcurement));

		// log.log(Level.FINE, ">>>> End");
		return Results.ok(results);
	}

	@Transactional
	public Result availableLocations(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		// StringBuilder fromToOptionStr = new StringBuilder();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			ArrayNode an = results.putArray("ecommerceAllLocationData");
			ArrayNode restan = results.putArray("restLocationData");
			List<IdosLocations> idosLocationslist = genericDAO.findAll(IdosLocations.class, true, false, entityManager);
			for (IdosLocations idosLoc : idosLocationslist) {
				ObjectNode row = Json.newObject();
				row.put("locationName", idosLoc.getLocationName());
				an.add(row);
			}
			// List<IdosSubscriptionCountry> idosSubscriptionCountry = genericDAO
			// .findAll(IdosSubscriptionCountry.class, true, false,
			// jpaApi.em());
			// for (IdosSubscriptionCountry idosSubscriptionCountryEntity :
			// idosSubscriptionCountry) {
			// fromToOptionStr
			// .append("<option value=")
			// .append(idosSubscriptionCountryEntity
			// .getCountryName())
			// .append(">")
			// .append(idosSubscriptionCountryEntity
			// .getCountryName()).append("</option>");
			// }
			// StringBuilder sbr=new StringBuilder("");
			// sbr.append("select obj from IdosSubscriptionCountryStates obj");
			// List<IdosSubscriptionCountryStates> countryStates =
			// genericDAO.findAll(IdosSubscriptionCountryStates.class, true, false,
			// jpaApi.em());
			// for (IdosSubscriptionCountryStates idosSubsCtryStates : countryStates) {
			// fromToOptionStr.append("<option value=")
			// .append(idosSubsCtryStates.getStateName())
			// .append(">")
			// .append(idosSubsCtryStates.getStateName())
			// .append("</option>");
			// }
			// List<IdosCountryStatesCity> countryStateCity = genericDAO
			// .findAll(IdosCountryStatesCity.class, true, false,
			// jpaApi.em());
			// for (IdosCountryStatesCity idosCtryStatesCities : countryStateCity) {
			// fromToOptionStr.append("<option value=")
			// .append(idosCtryStatesCities.getCityName())
			// .append(">")
			// .append(idosCtryStatesCities.getCityName())
			// .append("</option>");
			// }
			// ObjectNode row = Json.newObject();
			// row.put("restlocationName", fromToOptionStr.toString());
			// restan.add(row);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "availableLocations Email", "availableLocations Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result listItemsPricings(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			JsonNode json = request.body().asJson();
			String vendselleremail = json.findValue("vendSellerEmail").asText();
			log.log(Level.INFO, vendselleremail + "  sadefedfdefdfdfdfgfrfgfgfgfdfd");
			ArrayNode an = results.putArray("ecommerceItemPricingData");
			ArrayNode svan = results.putArray("sellerVendorInfoData");
			criterias.put("vendorEmail", vendselleremail);
			criterias.put("presentStatus", 1);
			IdosRegisteredVendor idosRegVendor = genericDAO.getByCriteria(IdosRegisteredVendor.class, criterias,
					entityManager);
			if (idosRegVendor != null) {
				ObjectNode basicrow = Json.newObject();
				if (idosRegVendor.getVendorName() != null) {
					basicrow.put("vendorName", idosRegVendor.getVendorName());
				} else {
					basicrow.put("vendorName", "");
				}
				if (idosRegVendor.getVendorEmail() != null) {
					basicrow.put("vendorEmail", idosRegVendor.getVendorEmail());
				} else {
					basicrow.put("vendorEmail", "");
				}
				if (idosRegVendor.getVendorPhoneNumber() != null) {
					int k = idosRegVendor.getVendorPhoneNumber().indexOf("-");
					basicrow.put("vendorCountryCode", idosRegVendor.getVendorPhoneNumber().substring(0, k));
					basicrow.put("vendorPhoneNumber", idosRegVendor.getVendorPhoneNumber().substring(k + 1,
							idosRegVendor.getVendorPhoneNumber().length()));
				} else {
					basicrow.put("vendorCountryCode", "");
					basicrow.put("vendorPhoneNumber", "");
				}
				svan.add(basicrow);
			} else {
				criterias.clear();
				criterias.put("email", vendselleremail);
				criterias.put("type", 1);
				criterias.put("presentStatus", 1);
				Vendor vendor = genericDAO.getByCriteria(Vendor.class, criterias, entityManager);
				if (vendor != null) {
					ObjectNode basicrow = Json.newObject();
					if (vendor.getName() != null) {
						basicrow.put("vendorName", vendor.getName());
					} else {
						basicrow.put("vendorName", "");
					}
					if (vendor.getEmail() != null) {
						basicrow.put("vendorEmail", vendor.getEmail());
					} else {
						basicrow.put("vendorEmail", "");
					}
					if (vendor.getPhone() != null && vendor.getPhone().length() > 0) {
						int k = vendor.getPhone().indexOf("-");
						k = k == -1 ? 0 : k;
						basicrow.put("vendorCountryCode", vendor.getPhone().substring(0, k));
						basicrow.put("vendorPhoneNumber",
								vendor.getPhone().substring(k + 1, vendor.getPhone().length()));
					} else {
						basicrow.put("vendorCountryCode", "");
						basicrow.put("vendorPhoneNumber", "");
					}
					svan.add(basicrow);
				} else {
					ObjectNode basicrow = Json.newObject();
					basicrow.put("vendorName", "");
					basicrow.put("vendorEmail", "");
					basicrow.put("vendorCountryCode", "");
					basicrow.put("vendorPhoneNumber", "");
					svan.add(basicrow);
				}
			}
			// List<IdosRegisteredVendorPricings>
			// itempricinglist=genericDAO.findByCriteria(IdosRegisteredVendorPricings.class,
			// criterias, entityManager);
			// for(IdosRegisteredVendorPricings idospricing:itempricinglist){
			// ObjectNode row = Json.newObject();
			// row.put("primId", idospricing.getId());
			// row.put("itemName", idospricing.getVendorItems());
			// row.put("itemSuppliedInBranches", idospricing.getVendorLocations());
			// row.put("retailerPrice", idospricing.getVendorRetailerUnitPrice());
			// row.put("wholeSellerPrice", idospricing.getVendorWholesaleUnitPrice());
			// row.put("specialPrice", idospricing.getVendorSpecialUnitPrice());
			// row.put("specialPriceRequirements",
			// idospricing.getVendorSpecialPriceRequirements());
			// an.add(row);
			// }
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "listItemsPricings Email", "listItemsPricings Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> end " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result populateAllPossibleItems(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		ArrayNode an = results.putArray("ecommerceAvailableItemData");
		try {
			JsonNode json = request.body().asJson();
			String enteredValue = json.findPath("enteredValue").asText();
			String newsbquery = ("select obj from IdosItems obj WHERE obj.itemName like ? and obj.presentStatus=1 ORDER BY obj.itemName");
			ArrayList inparam = new ArrayList(1);
			inparam.add(enteredValue + "%");
			List<IdosItems> idosAvailableItems = genericDAO.queryWithParams(newsbquery, entityManager, inparam);
			for (IdosItems idosItems : idosAvailableItems) {
				ObjectNode row = Json.newObject();
				row.put("itemName", idosItems.getItemName());
				an.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "populateAllPossibleItems Email",
					"populateAllPossibleItems Organization", Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result supplierItemAlreadyExists(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		ArrayNode an = results.putArray("ecommerceAvailableItemDataInfo");
		ArrayNode itemexistan = results.putArray("itemDataExist");
		Map<String, Object> criterias = new HashMap<String, Object>();
		try {
			JsonNode json = request.body().asJson();
			String supplieremail = json.findPath("supplierEmail").asText();
			String enteredText = json.findPath("selectedTextValue").asText();
			criterias.clear();
			criterias.put("vendorEmail", supplieremail);
			criterias.put("vendorItems", enteredText);
			criterias.put("presentStatus", 1);
			IdosRegisteredVendorPricings idospricing = genericDAO.getByCriteria(IdosRegisteredVendorPricings.class,
					criterias, entityManager);
			if (idospricing != null) {
				ObjectNode row = Json.newObject();
				row.put("exist", "Already Entered Item Pricings For the supplier");
				itemexistan.add(row);
				ObjectNode datainforow = Json.newObject();
				row.put("primId", idospricing.getId());
				if (idospricing.getVendorItems() != null) {
					row.put("itemName", idospricing.getVendorItems());
				} else {
					row.put("itemName", "");
				}
				if (idospricing.getVendorItemsDescription() != null) {
					row.put("itemDescription", idospricing.getVendorItemsDescription());
				} else {
					row.put("itemDescription", "");
				}
				if (idospricing.getVendorLocations() != null) {
					row.put("itemSuppliedInBranches", idospricing.getVendorLocations());
				} else {
					row.put("itemSuppliedInBranches", "");
				}
				if (idospricing.getVendorRetailerUnitPrice() != null) {
					row.put("retailerPrice", decimalFormat.format(idospricing.getVendorRetailerUnitPrice()));
				} else {
					row.put("retailerPrice", "");
				}
				if (idospricing.getVendorWholesaleUnitPrice() != null) {
					row.put("wholeSellerPrice", decimalFormat.format(idospricing.getVendorWholesaleUnitPrice()));
				} else {
					row.put("wholeSellerPrice", "");
				}
				if (idospricing.getVendorSpecialUnitPrice() != null) {
					row.put("specialPrice", decimalFormat.format(idospricing.getVendorSpecialUnitPrice()));
				} else {
					row.put("specialPrice", "");
				}
				if (idospricing.getVendorSpecialPriceRequirements() != null) {
					row.put("specialPriceRequirements", idospricing.getVendorSpecialPriceRequirements());
				} else {
					row.put("specialPriceRequirements", "");
				}
				an.add(row);
			} else {
				ObjectNode row = Json.newObject();
				row.put("exist", "Not Exist");
				itemexistan.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "supplierItemAlreadyExists Email",
					"supplierItemAlreadyExists Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(results);
	}

	@Transactional
	public Result saveSupplierItemPricings(Request request) {

		// EntityManager entityManager = getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			String supplieritemPricePrimId = json.findPath("supplieritemPricePrimId") != null
					? json.findPath("supplieritemPricePrimId").asText()
					: null;
			String supplierRegName = json.findPath("supplierRegName").asText();
			String supplierRegEmail = json.findPath("supplierRegEmail").asText();
			String supplierRegPhoneNumber = json.findPath("supplierRegPhoneNumber").asText();
			String supplieritemPriceName = json.findPath("supplieritemPriceName").asText();
			String supplieritemDescription = json.findPath("supplieritemDescription").asText();
			String supplieritemAvailableLocations = json.findPath("supplieritemAvailableLocations").asText();
			String supplierresellerPrice = json.findPath("supplierresellerPrice").asText();
			String supplierwholesellerPrice = json.findPath("supplierwholesellerPrice").asText();
			String supplierspecialPrice = json.findPath("supplierspecialPrice").asText();
			String specialPriceRequirements = json.findPath("specialPriceRequirements").asText();
			IdosRegisteredVendorPricings idosRegVendPricings = null;
			if (supplieritemPricePrimId != null) {
				idosRegVendPricings = IdosRegisteredVendorPricings.findById(Long.parseLong(supplieritemPricePrimId));
			} else {
				idosRegVendPricings = new IdosRegisteredVendorPricings();
			}
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("vendorEmail", supplierRegEmail);
			criterias.put("presentStatus", 1);
			IdosRegisteredVendor idosRegVendor = genericDAO.getByCriteria(IdosRegisteredVendor.class, criterias,
					entityManager);
			if (idosRegVendor != null) {
				idosRegVendor.setVendorPhoneNumber(supplierRegPhoneNumber);
				genericDAO.saveOrUpdate(idosRegVendor, null, entityManager);
			}
			idosRegVendPricings.setVendorName(supplierRegName);
			idosRegVendPricings.setVendorEmail(supplierRegEmail);
			idosRegVendPricings.setVendorPhoneNumber(supplierRegPhoneNumber);
			idosRegVendPricings.setVendorItems(supplieritemPriceName);
			criterias.clear();
			criterias.put("itemName", supplieritemPriceName);
			criterias.put("presentStatus", 1);
			IdosItems existingItem = genericDAO.getByCriteria(IdosItems.class, criterias, entityManager);
			if (existingItem == null) {
				existingItem = new IdosItems();
				existingItem.setItemName(supplieritemPriceName);
				genericDAO.saveOrUpdate(existingItem, null, entityManager);
			}
			idosRegVendPricings.setVendorItemsDescription(supplieritemDescription);
			idosRegVendPricings.setVendorLocations(supplieritemAvailableLocations);
			if (supplierresellerPrice != null && !supplierresellerPrice.equals("")) {
				idosRegVendPricings.setVendorRetailerUnitPrice(Double.parseDouble(supplierresellerPrice));
			} else {
				idosRegVendPricings.setVendorRetailerUnitPrice(null);
			}
			if (supplierwholesellerPrice != null && !supplierwholesellerPrice.equals("")) {
				idosRegVendPricings.setVendorWholesaleUnitPrice(Double.parseDouble(supplierwholesellerPrice));
			} else {
				idosRegVendPricings.setVendorWholesaleUnitPrice(null);
			}
			if (supplierspecialPrice != null && !supplierspecialPrice.equals("")) {
				idosRegVendPricings.setVendorSpecialUnitPrice(Double.parseDouble(supplierspecialPrice));
			} else {
				idosRegVendPricings.setVendorSpecialUnitPrice(null);
			}
			idosRegVendPricings.setVendorSpecialPriceRequirements(specialPriceRequirements);
			genericDAO.saveOrUpdate(idosRegVendPricings, null, entityManager);
			transaction.commit();
		} catch (Exception ex) {
			if (transaction.isActive()) {
				transaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "saveSupplierItemPricings Email",
					"saveSupplierItemPricings Organization", Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End");
		return Results.ok(results);
	}

	@Transactional
	public Result availableSupplierThanMinWAP(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		Http.Session session = request.session();
		EntityTransaction entityTransaction = entityManager.getTransaction();
		ObjectNode results = Json.newObject();
		ArrayNode actualTransactionItemBranchWAPan = results.putArray("actualTransactionItemBranchWAPData");
		ArrayNode availableSuppliersLeassThanMinWAP = results.putArray("availableSuppliersLeassThanMinWAPData");
		Users user = null;
		try {
			entityTransaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("usermail").asText();
			session.adding("email", useremail);
			user = Users.findActiveByEmail(useremail);
			String transactionId = json.findValue("transactionId").asText();
			Double minWAP = json.findValue("minWAP").asDouble();
			Transaction txn = Transaction.findById(Long.parseLong(transactionId));
			ObjectNode availablerow = Json.newObject();
			availablerow.put("itemName", txn.getTransactionSpecifics().getName());
			availablerow.put("txnLocation", txn.getTransactionBranch().getLocation());
			availablerow.put("minWAP", decimalFormat.format(minWAP));
			actualTransactionItemBranchWAPan.add(availablerow);
			String sbquery = ("select obj from IdosRegisteredVendorPricings obj where obj.vendorItems=?1 and LOCATE(?2, obj.vendorLocations)>=0 and (obj.vendorRetailerUnitPrice<=?3 or obj.vendorWholesaleUnitPrice<=?4 or obj.vendorSpecialUnitPrice<=?5) and obj.presentStatus=1 ORDER BY obj.vendorRetailerUnitPrice desc");
			ArrayList inparam = new ArrayList(2);
			inparam.add(txn.getTransactionSpecifics().getName());
			inparam.add(txn.getTransactionBranch().getLocation());
			inparam.add(minWAP);
			inparam.add(minWAP);
			inparam.add(minWAP);
			List<IdosRegisteredVendorPricings> availableSuppliers = genericDAO.queryWithParams(sbquery, entityManager,
					inparam);
			Map<String, Object> criterias = new HashMap<String, Object>();
			if (availableSuppliers.size() > 0) {
				for (IdosRegisteredVendorPricings idosRegVendPricings : availableSuppliers) {
					ObjectNode availablesupplierrow = Json.newObject();
					availablesupplierrow.put("supplierName", idosRegVendPricings.getVendorName());
					availablesupplierrow.put("supplierEmail", idosRegVendPricings.getVendorEmail());
					availablesupplierrow.put("supplierNumber", idosRegVendPricings.getVendorPhoneNumber());
					if (idosRegVendPricings.getVendorRetailerUnitPrice() != null) {
						availablesupplierrow.put("supplierResellerPrice",
								decimalFormat.format(idosRegVendPricings.getVendorRetailerUnitPrice()));
					} else {
						availablesupplierrow.put("supplierResellerPrice", "");
					}
					if (idosRegVendPricings.getVendorWholesaleUnitPrice() != null) {
						availablesupplierrow.put("wholesellerPrice",
								decimalFormat.format(idosRegVendPricings.getVendorWholesaleUnitPrice()));
					} else {
						availablesupplierrow.put("wholesellerPrice", "");
					}
					if (idosRegVendPricings.getVendorSpecialUnitPrice() != null) {
						availablesupplierrow.put("specialPrice",
								decimalFormat.format(idosRegVendPricings.getVendorSpecialUnitPrice()));
					} else {
						availablesupplierrow.put("specialPrice", "");
					}
					availableSuppliersLeassThanMinWAP.add(availablesupplierrow);
					criterias.clear();
					criterias.put("vendorEmail", idosRegVendPricings.getVendorEmail());
					criterias.put("presentStatus", 1);
					IdosRegisteredVendor regVendor = genericDAO.getByCriteria(IdosRegisteredVendor.class, criterias,
							entityManager);
					if (regVendor != null) {
						if (regVendor.getNumberOfTimesSearched() != null) {
							int numberOfTimesSearched = regVendor.getNumberOfTimesSearched();
							regVendor.setNumberOfTimesSearched(++numberOfTimesSearched);
							genericDAO.saveOrUpdate(regVendor, null, entityManager);
						}
					}
				}
			}
			entityTransaction.commit();
		} catch (Exception ex) {
			if (entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
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
	public Result getVendorPricings(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email") == null ? "" : json.findValue("email").asText();
			result = SELLER_SERVICE.getVendorPriceLists(email);
		} catch (Exception ex) {
			result = Json.newObject();
			result.put("result", false);
			result.put("message", "Something went wrong. Please try again later.");
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Get Vendor Pricings Email", "Get Vendor Pricings Organization",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result getVendorPriceDetails(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = null;
		try {
			// entitytransaction.begin();
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			long id = json.findValue("id").asLong();
			result = SELLER_SERVICE.getVendorPriceDetails(email, id);
			// entitytransaction.commit();
		} catch (Exception ex) {
			result = Json.newObject();
			result.put("result", false);
			result.put("message", "Something went wrong. Please try again later.");
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "Get Vendor Price Details Email",
					"Get Vendor Price Details Organization", Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		return Results.ok(result);
	}

	@Transactional
	public Result uploadSupplierItems(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("successUploading");
		try {
			JsonNode json = request.body().asJson();
			MultipartFormData<File> body = request.body().asMultipartFormData();
			result = SELLER_SERVICE.uploadSupplierItems(request, result, json, body, entityManager, entitytransaction);
		} catch (Exception ex) {
			result = Json.newObject();
			result.put("result", false);
			result.put("message", "Something went wrong. Please try again later.");
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "upload supplier items Email", "upload supplier items Email",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		ObjectNode row = Json.newObject();
		row.put("message", "Uploaded Successfully");
		an.add(row);
		return Results.ok(result);
	}

	@Transactional
	public Result contactSupplierVendor(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("supplierContact");
		JsonNode json = request.body().asJson();
		String usermail = json.findValue("usermail").asText();
		Users user = null;
		try {
			entitytransaction.begin();
			user = Users.findActiveByEmail(usermail);
			String supplierVendorEmail = json.findValue("supplierVendorEmail").asText();
			String itemName = json.findValue("itemName").asText();
			String location = json.findValue("location").asText();
			if (itemName == null) {
				itemName = "";
			}
			if (location == null) {
				location = "";
			}
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("vendorEmail", supplierVendorEmail);
			criterias.put("presentStatus", 1);
			IdosRegisteredVendor idosRegVendor = genericDAO.getByCriteria(IdosRegisteredVendor.class, criterias,
					entityManager);
			if (idosRegVendor != null) {
				if (idosRegVendor.getNumberOfTimesContacted() != null) {
					int numberOfTimesContacted = idosRegVendor.getNumberOfTimesContacted();
					idosRegVendor.setNumberOfTimesContacted(++numberOfTimesContacted);
					genericDAO.saveOrUpdate(idosRegVendor, null, entityManager);
					IdosRegisteredVendorContacted idosRegVendorContacted = new IdosRegisteredVendorContacted();
					idosRegVendorContacted.setOrganization(user.getOrganization());
					idosRegVendorContacted.setIdosRegisteredVendor(idosRegVendor);
					idosRegVendorContacted.setContactedDate(Calendar.getInstance().getTime());
					idosRegVendorContacted.setContactedByUserEmail(user.getEmail());
					genericDAO.saveOrUpdate(idosRegVendorContacted, null, entityManager);
				}
				String body = supplierContact.render(user.getOrganization().getName(), itemName, location,
						user.getEmail(), ConfigParams.getInstance()).body();
				final String username = ConfigFactory.load().getString("smtp.user");
				String subject = "Contact Lead From Organization " + user.getOrganization().getName() + " Contact User "
						+ user.getEmail();
				Session session = emailsession;
				mailTimer(body, username, session, idosRegVendor.getVendorEmail(), null, subject);
				ObjectNode row = Json.newObject();
				row.put("successConnect", "success");
				an.add(row);
			} else {
				ObjectNode row = Json.newObject();
				row.put("successConnect", "failure");
				an.add(row);
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		return Results.ok(result);
	}

	@Transactional
	public Result addVendSupplierLocation(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		result.put("result", false);
		ArrayNode an = result.putArray("listedLocationData");
		try {
			entitytransaction.begin();
			JsonNode json = request.body().asJson();
			String userEmail = json.findValue("userEmail").asText();
			String locationName = json.findValue("locationName").asText();
			if (locationName != null && !locationName.equals("")) {
				IdosLocations idosLoc = new IdosLocations();
				idosLoc.setLocationName(locationName);
				genericDAO.saveOrUpdate(idosLoc, null, entityManager);
				result.put("result", true);
				ObjectNode row = Json.newObject();
				row.put("listedLocation", locationName);
				an.add(row);
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			result = Json.newObject();
			result.put("result", false);
			log.log(Level.SEVERE, "Error", ex);
			// log.log(Level.SEVERE, ex.getMessage());
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, "add vendor Supplier item listing location",
					"add vendor Supplier item listing location",
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		ObjectNode row = Json.newObject();
		row.put("message", "Uploaded Successfully");
		an.add(row);
		return Results.ok(result);
	}

}
