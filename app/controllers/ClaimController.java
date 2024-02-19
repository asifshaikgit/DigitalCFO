package controllers;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.idos.util.IDOSException;
import model.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import javax.transaction.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Http.Request;
//import play.mvc.Http.Session;
import play.mvc.Result;
import play.mvc.Results;
import service.ClaimsService;
import service.ClaimsServiceImpl;
import service.ClaimsSettlementService;
import service.ClaimsSettlementServiceImpl;
import views.html.*;
import play.Application;
import com.idos.util.IdosConstants;
import com.idos.util.ListUtility;
import java.util.logging.Level;

public class ClaimController extends StaticController {

	private static final String SPECIFICS_HQL = "SELECT obj FROM Specifics obj WHERE obj.organization.id = ?1 AND obj.employeeClaimItem = 1";
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	public static Application application;
	private Request request;
	// private Http.Session session = request.session();

	@Inject

	public ClaimController(Application application) {
		super(application);
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Transactional
	public Result getTravelStaticData(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		ObjectNode row = null;
		ArrayNode an = result.putArray("array");
		result.put("result", false);
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			// String email = json.findValue("userEmail").asText();
			String type = json.findValue("type").asText();
			user = getUserInfo(request);
			if (null == user) {
				return unauthorized();
			}
			if ("distances".equalsIgnoreCase(type)) {
				List<DistanceMilesKm> milesKms = genericDAO.findAll(DistanceMilesKm.class, true, false, entityManager);
				if (!milesKms.isEmpty() && milesKms.size() > 0) {
					result.put("result", true);
					for (DistanceMilesKm milesKm : milesKms) {
						if (null != milesKm) {
							row = Json.newObject();
							if (null != milesKm.getId()) {
								row.put("id", milesKm.getId());
							}
							if (null != milesKm.getDistanceInMilesKms()) {
								row.put("mileKm", milesKm.getDistanceInMilesKms());
							}
						}
						an.add(row);
					}
				}
			} else if ("travel".equalsIgnoreCase(type)) {
				List<TravelMode> modes = genericDAO.findAll(TravelMode.class, true, false, entityManager);
				if (!modes.isEmpty() && modes.size() > 0) {
					result.put("result", true);
					for (TravelMode mode : modes) {
						if (null != mode) {
							row = Json.newObject();
							if (null != mode.getId()) {
								row.put("id", mode.getId());
							}
							if (null != mode.getTravelModeName()) {
								row.put("name", mode.getTravelModeName());
							}
							an.add(row);
						}
					}
				}
			} else if ("accomodation".equalsIgnoreCase(type)) {
				List<AccomodationType> accomodationTypes = genericDAO.findAll(AccomodationType.class, true, false,
						entityManager);
				if (!accomodationTypes.isEmpty() && accomodationTypes.size() > 0) {
					result.put("result", true);
					for (AccomodationType accomodationType : accomodationTypes) {
						if (null != accomodationType) {
							row = Json.newObject();
							if (null != accomodationType.getId()) {
								row.put("id", accomodationType.getId());
							}
							if (null != accomodationType.getAccomodationTypeName()) {
								row.put("name", accomodationType.getAccomodationTypeName());
							}
							an.add(row);
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
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End ");
		return Results.ok(result);
	}

	@Transactional
	public Result getAvailableExpenseClaimItems(Request request) {
		Http.Session session = request.session();
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		result.put("result", false);
		ObjectNode row = null;
		ArrayNode an = result.putArray("array");
		result.put("result", false);
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("userEmail").asText();
			session.adding("email", email);
			user = getUserInfo(request);
			if (null == user) {
				return unauthorized();
			}
			ArrayList inparamList = new ArrayList(1);
			inparamList.add(user.getOrganization().getId());
			List<Specifics> specifics = genericDAO.queryWithParams(SPECIFICS_HQL, entityManager, inparamList);
			if (!specifics.isEmpty() && specifics.size() > 0) {
				result.put("result", true);
				for (Specifics specific : specifics) {
					if (null != specific) {
						row = Json.newObject();
						if (null != specific.getId()) {
							row.put("id", specific.getId());
						}
						if (null != specific.getName()) {
							row.put("name", specific.getName());
						}
						an.add(row);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End ");
		return Results.ok(result);
	}

	@Transactional
	public Result getAvailableTravelExpenseGroups(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		result.put("result", false);
		ObjectNode travelrow = null;
		ObjectNode expenserow = null;
		ArrayNode travelan = result.putArray("travelarray");
		ArrayNode expensean = result.putArray("expensearray");
		result.put("result", false);
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("userEmail").asText();
			session.adding("email", email);
			user = getUserInfo(request);
			if (user != null) {
				List<Travel_Group> orgTravelGroupList = Travel_Group.getTravelGroupList(entityManager,
						user.getOrganization().getId());
				List<ExpenseGroup> orgExpenseGroupList = ExpenseGroup.getExpenseGroupList(entityManager,
						user.getOrganization().getId());
				if (!orgTravelGroupList.isEmpty() && orgTravelGroupList.size() > 0) {
					result.put("result", true);
					for (Travel_Group tgroup : orgTravelGroupList) {
						if (tgroup != null) {
							travelrow = Json.newObject();
							travelrow.put("id", tgroup.getId());
							travelrow.put("name", tgroup.getTravelGroupName());
							travelan.add(travelrow);
						}
					}
				}
				if (!orgExpenseGroupList.isEmpty() && orgExpenseGroupList.size() > 0) {
					result.put("result", true);
					for (ExpenseGroup egroup : orgExpenseGroupList) {
						if (egroup != null) {
							expenserow = Json.newObject();
							expenserow.put("id", egroup.getId());
							expenserow.put("name", egroup.getExpenseGroupName());
							expensean.add(expenserow);
						}
					}
				}
			}
			log.log(Level.INFO, travelan.toString());
			log.log(Level.INFO, expensean.toString());
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result createTravelGroup(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		ArrayNode travelan = result.putArray("travelarray");
		Users users = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			users = getUserInfo(request);
			String tGroupName = json.findValue("tGroupName").asText();
			String tGroupEntityHiddenId = json.findValue("tGroupEntityHiddenId").asText();
			Travel_Group tGroup = null;
			if (tGroupEntityHiddenId != null && !tGroupEntityHiddenId.equals("")) {
				tGroup = Travel_Group.findById(Long.parseLong(tGroupEntityHiddenId));
			} else {
				tGroup = new Travel_Group();
			}
			tGroup.setTravelGroupName(tGroupName);
			tGroup.setOrganization(users.getOrganization());
			genericDAO.saveOrUpdate(tGroup, users, entityManager);
			ObjectNode travelrow = Json.newObject();
			travelrow.put("travelGroupId", tGroup.getId());
			travelrow.put("travelGroupName", tGroup.getTravelGroupName());
			travelan.add(travelrow);
			// travel group country capital boarding and lodging maximum permitted room cost
			// and food cost add update logic start
			String tGroupBoardingLodgingTypeCtryCap = json.findValue("tGroupBoardingLodgingTypeCtryCap") != null
					? json.findValue("tGroupBoardingLodgingTypeCtryCap").asText()
					: null;
			String tGroupMaxRoomCostPerNightCtryCap = json.findValue("tGroupMaxRoomCostPerNightCtryCap") != null
					? json.findValue("tGroupMaxRoomCostPerNightCtryCap").asText()
					: null;
			String tGroupMaxFoodCostPerDayCtryCap = json.findValue("tGroupMaxFoodCostPerDayCtryCap") != null
					? json.findValue("tGroupMaxFoodCostPerDayCtryCap").asText()
					: null;
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 1);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodging = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodging = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeCtryCap != null && !tGroupBoardingLodgingTypeCtryCap.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeCtryCap.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightCtryCap.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayCtryCap.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(1);
					newTravelPermittedBoardingLodging.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingList = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodging, newTravelPermittedBoardingLodging);
			for (int i = 0; i < tGroupBoardingnLodgingList.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingList.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingList.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingList.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group country capital boarding and lodging maximum permitted room cost
			// and food cost add update logic end
			// travel group state capital boarding and lodging maximum permitted room cost
			// and food cost add update logic start
			String tGroupBoardingLodgingTypeStateCap = json.findValue("tGroupBoardingLodgingTypeStateCap") != null
					? json.findValue("tGroupBoardingLodgingTypeStateCap").asText()
					: null;
			String tGroupMaxRoomCostPerNightStateCap = json.findValue("tGroupMaxRoomCostPerNightStateCap") != null
					? json.findValue("tGroupMaxRoomCostPerNightStateCap").asText()
					: null;
			String tGroupMaxFoodCostPerDayStateCap = json.findValue("tGroupMaxFoodCostPerDayStateCap") != null
					? json.findValue("tGroupMaxFoodCostPerDayStateCap").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 2);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingSC = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingSC = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeStateCap != null && !tGroupBoardingLodgingTypeStateCap.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeStateCap.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightStateCap.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayStateCap.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(2);
					newTravelPermittedBoardingLodgingSC.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListSC = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingSC, newTravelPermittedBoardingLodgingSC);
			for (int i = 0; i < tGroupBoardingnLodgingListSC.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListSC.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListSC.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListSC.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group state capital boarding and lodging maximum permitted room cost
			// and food cost add update logic end
			// travel group metro city boarding and lodging maximum permitted room cost and
			// food cost add update logic start
			String tGroupBoardingLodgingTypeMetroCity = json.findValue("tGroupBoardingLodgingTypeMetroCity") != null
					? json.findValue("tGroupBoardingLodgingTypeMetroCity").asText()
					: null;
			String tGroupMaxRoomCostPerNightMetroCity = json.findValue("tGroupMaxRoomCostPerNightMetroCity") != null
					? json.findValue("tGroupMaxRoomCostPerNightMetroCity").asText()
					: null;
			String tGroupMaxFoodCostPerDayMetroCity = json.findValue("tGroupMaxFoodCostPerDayMetroCity") != null
					? json.findValue("tGroupMaxFoodCostPerDayMetroCity").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 3);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingMC = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingMC = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeMetroCity != null && !tGroupBoardingLodgingTypeMetroCity.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeMetroCity.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightMetroCity.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayMetroCity.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (tGroupPerMaxRcpN.length > 0 && !tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (tGroupPerMaxFcpD.length > 0 && !tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(3);
					newTravelPermittedBoardingLodgingMC.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListMC = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingMC, newTravelPermittedBoardingLodgingMC);
			for (int i = 0; i < tGroupBoardingnLodgingListMC.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListMC.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListMC.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListMC.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group metro city boarding and lodging maximum permitted room cost and
			// food cost add update logic end
			// travel group other city boarding and lodging maximum permitted room cost and
			// food cost add update logic start
			String tGroupBoardingLodgingTypeOtherCity = json.findValue("tGroupBoardingLodgingTypeOtherCity") != null
					? json.findValue("tGroupBoardingLodgingTypeOtherCity").asText()
					: null;
			String tGroupMaxRoomCostPerNightOtherCity = json.findValue("tGroupMaxRoomCostPerNightOtherCity") != null
					? json.findValue("tGroupMaxRoomCostPerNightOtherCity").asText()
					: null;
			String tGroupMaxFoodCostPerDayOtherCity = json.findValue("tGroupMaxFoodCostPerDayOtherCity") != null
					? json.findValue("tGroupMaxFoodCostPerDayOtherCity").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 4);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingOC = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingOC = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeOtherCity != null && !tGroupBoardingLodgingTypeOtherCity.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeOtherCity.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightOtherCity.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayOtherCity.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(4);
					newTravelPermittedBoardingLodgingOC.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListOC = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingOC, newTravelPermittedBoardingLodgingOC);
			for (int i = 0; i < tGroupBoardingnLodgingListOC.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListOC.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListOC.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListOC.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group other city boarding and lodging maximum permitted room cost and
			// food cost add update logic end
			// travel group town boarding and lodging maximum permitted room cost and food
			// cost add update logic start
			String tGroupBoardingLodgingTypeTown = json.findValue("tGroupBoardingLodgingTypeTown") != null
					? json.findValue("tGroupBoardingLodgingTypeTown").asText()
					: null;
			String tGroupMaxRoomCostPerNightTown = json.findValue("tGroupMaxRoomCostPerNightTown") != null
					? json.findValue("tGroupMaxRoomCostPerNightTown").asText()
					: null;
			String tGroupMaxFoodCostPerDayTown = json.findValue("tGroupMaxFoodCostPerDayTown") != null
					? json.findValue("tGroupMaxFoodCostPerDayTown").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 5);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingT = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingT = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeTown != null && !tGroupBoardingLodgingTypeTown.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeTown.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightTown.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayTown.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(5);
					newTravelPermittedBoardingLodgingT.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListT = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingT, newTravelPermittedBoardingLodgingT);
			for (int i = 0; i < tGroupBoardingnLodgingListT.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListT.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListT.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListT.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group town boarding and lodging maximum permitted room cost and food
			// cost add update logic end
			// travel group county boarding and lodging maximum permitted room cost and food
			// cost add update logic start
			String tGroupBoardingLodgingTypeCountry = json.findValue("tGroupBoardingLodgingTypeCountry") != null
					? json.findValue("tGroupBoardingLodgingTypeCountry").asText()
					: null;
			String tGroupMaxRoomCostPerNightCountry = json.findValue("tGroupMaxRoomCostPerNightCountry") != null
					? json.findValue("tGroupMaxRoomCostPerNightCountry").asText()
					: null;
			String tGroupMaxFoodCostPerDayCountry = json.findValue("tGroupMaxFoodCostPerDayCountry") != null
					? json.findValue("tGroupMaxFoodCostPerDayCountry").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 6);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingCounty = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingCounty = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeCountry != null && !tGroupBoardingLodgingTypeCountry.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeCountry.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightCountry.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayCountry.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(6);
					newTravelPermittedBoardingLodgingCounty.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListCounty = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingCounty, newTravelPermittedBoardingLodgingCounty);
			for (int i = 0; i < tGroupBoardingnLodgingListCounty.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListCounty.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListCounty.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListCounty.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group county boarding and lodging maximum permitted room cost and food
			// cost add update logic end
			// travel group municipality boarding and lodging maximum permitted room cost
			// and food cost add update logic start
			String tGroupBoardingLodgingTypeMunicipality = json
					.findValue("tGroupBoardingLodgingTypeMunicipality") != null
							? json.findValue("tGroupBoardingLodgingTypeMunicipality").asText()
							: null;
			String tGroupMaxRoomCostPerNightMunicipality = json
					.findValue("tGroupMaxRoomCostPerNightMunicipality") != null
							? json.findValue("tGroupMaxRoomCostPerNightMunicipality").asText()
							: null;
			String tGroupMaxFoodCostPerDayMunicipality = json.findValue("tGroupMaxFoodCostPerDayMunicipality") != null
					? json.findValue("tGroupMaxFoodCostPerDayMunicipality").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 7);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingMunicipality = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingMunicipality = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeMunicipality != null && !tGroupBoardingLodgingTypeMunicipality.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeMunicipality.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightMunicipality.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayMunicipality.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(7);
					newTravelPermittedBoardingLodgingMunicipality.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListMunicipality = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingMunicipality,
							newTravelPermittedBoardingLodgingMunicipality);
			for (int i = 0; i < tGroupBoardingnLodgingListMunicipality.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListMunicipality
							.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListMunicipality.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListMunicipality
							.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group municipality boarding and lodging maximum permitted room cost
			// and food cost add update logic end
			// travel group village boarding and lodging maximum permitted room cost and
			// food cost add update logic start
			String tGroupBoardingLodgingTypeVillage = json.findValue("tGroupBoardingLodgingTypeVillage") != null
					? json.findValue("tGroupBoardingLodgingTypeVillage").asText()
					: null;
			String tGroupMaxRoomCostPerNightVillage = json.findValue("tGroupMaxRoomCostPerNightVillage") != null
					? json.findValue("tGroupMaxRoomCostPerNightVillage").asText()
					: null;
			String tGroupMaxFoodCostPerDayVillage = json.findValue("tGroupMaxFoodCostPerDayVillage") != null
					? json.findValue("tGroupMaxFoodCostPerDayVillage").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 8);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingVillage = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingVillage = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeVillage != null && !tGroupBoardingLodgingTypeVillage.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeVillage.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightVillage.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayVillage.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(8);
					newTravelPermittedBoardingLodgingVillage.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListVillage = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingVillage, newTravelPermittedBoardingLodgingVillage);
			for (int i = 0; i < tGroupBoardingnLodgingListVillage.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListVillage.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListVillage.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListVillage.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group village boarding and lodging maximum permitted room cost and
			// food cost add update logic end
			// travel group remote location boarding and lodging maximum permitted room cost
			// and food cost add update logic start
			String tGroupBoardingLodgingTypeRemoteLoc = json.findValue("tGroupBoardingLodgingTypeRemoteLoc") != null
					? json.findValue("tGroupBoardingLodgingTypeRemoteLoc").asText()
					: null;
			String tGroupMaxRoomCostPerNightRemoteLoc = json.findValue("tGroupMaxRoomCostPerNightRemoteLoc") != null
					? json.findValue("tGroupMaxRoomCostPerNightRemoteLoc").asText()
					: null;
			String tGroupMaxFoodCostPerDayRemoteLoc = json.findValue("tGroupMaxFoodCostPerDayRemoteLoc") != null
					? json.findValue("tGroupMaxFoodCostPerDayRemoteLoc").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 9);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingRemoteLocation = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingRemoteLocation = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeRemoteLoc != null && !tGroupBoardingLodgingTypeRemoteLoc.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeRemoteLoc.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightRemoteLoc.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayRemoteLoc.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(9);
					newTravelPermittedBoardingLodgingRemoteLocation.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListRemoteLocation = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingRemoteLocation,
							newTravelPermittedBoardingLodgingRemoteLocation);
			for (int i = 0; i < tGroupBoardingnLodgingListRemoteLocation.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListRemoteLocation
							.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListRemoteLocation
							.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListRemoteLocation
							.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group remote location boarding and lodging maximum permitted room cost
			// and food cost add update logic end
			// travel group 20 miles away location boarding and lodging maximum permitted
			// room cost and food cost add update logic start
			String tGroupBoardingLodgingType20Miles = json.findValue("tGroupBoardingLodgingType20Miles") != null
					? json.findValue("tGroupBoardingLodgingType20Miles").asText()
					: null;
			String tGroupMaxRoomCostPerNight20Miles = json.findValue("tGroupMaxRoomCostPerNight20Miles") != null
					? json.findValue("tGroupMaxRoomCostPerNight20Miles").asText()
					: null;
			String tGroupMaxFoodCostPerDay20Miles = json.findValue("tGroupMaxFoodCostPerDay20Miles") != null
					? json.findValue("tGroupMaxFoodCostPerDay20Miles").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 10);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingTMA = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingTMA = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingType20Miles != null && !tGroupBoardingLodgingType20Miles.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingType20Miles.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNight20Miles.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDay20Miles.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(10);
					newTravelPermittedBoardingLodgingTMA.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListTMA = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingTMA, newTravelPermittedBoardingLodgingTMA);
			for (int i = 0; i < tGroupBoardingnLodgingListTMA.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListTMA.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListTMA.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListTMA.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group twenty miles away boarding and lodging maximum permitted room
			// cost and food cost add update logic end
			// travel group hill station location boarding and lodging maximum permitted
			// room cost and food cost add update logic start
			String tGroupBoardingLodgingTypeHillStation = json.findValue("tGroupBoardingLodgingTypeHillStation") != null
					? json.findValue("tGroupBoardingLodgingTypeHillStation").asText()
					: null;
			String tGroupMaxRoomCostPerNightHillStation = json.findValue("tGroupMaxRoomCostPerNightHillStation") != null
					? json.findValue("tGroupMaxRoomCostPerNightHillStation").asText()
					: null;
			String tGroupMaxFoodCostPerDayHillStation = json.findValue("tGroupMaxFoodCostPerDayHillStation") != null
					? json.findValue("tGroupMaxFoodCostPerDayHillStation").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 11);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingRL = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingRL = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeHillStation != null && !tGroupBoardingLodgingTypeHillStation.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeHillStation.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightHillStation.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayHillStation.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(11);
					newTravelPermittedBoardingLodgingRL.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListRL = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingRL, newTravelPermittedBoardingLodgingRL);
			for (int i = 0; i < tGroupBoardingnLodgingListRL.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListRL.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListRL.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListRL.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group hill station boarding and lodging maximum permitted room cost
			// and food cost add update logic end
			// travel group resort boarding and lodging maximum permitted room cost and food
			// cost add update logic start
			String tGroupBoardingLodgingTypeResort = json.findValue("tGroupBoardingLodgingTypeResort") != null
					? json.findValue("tGroupBoardingLodgingTypeResort").asText()
					: null;
			String tGroupMaxRoomCostPerNightResort = json.findValue("tGroupMaxRoomCostPerNightResort") != null
					? json.findValue("tGroupMaxRoomCostPerNightResort").asText()
					: null;
			String tGroupMaxFoodCostPerDayResort = json.findValue("tGroupMaxFoodCostPerDayResort") != null
					? json.findValue("tGroupMaxFoodCostPerDayResort").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 12);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingResort = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingResort = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeResort != null && !tGroupBoardingLodgingTypeResort.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeResort.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightResort.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayResort.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(12);
					newTravelPermittedBoardingLodgingResort.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListResort = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingResort, newTravelPermittedBoardingLodgingResort);
			for (int i = 0; i < tGroupBoardingnLodgingListRL.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListResort.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListResort.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListResort.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group resort boarding and lodging maximum permitted room cost and food
			// cost add update logic end
			// travel group place of conflict warzone boarding and lodging maximum permitted
			// room cost and food cost add update logic start
			String tGroupBoardingLodgingTypeConflictWar = json.findValue("tGroupBoardingLodgingTypeConflictWar") != null
					? json.findValue("tGroupBoardingLodgingTypeConflictWar").asText()
					: null;
			String tGroupMaxRoomCostPerNightConflictWar = json.findValue("tGroupMaxRoomCostPerNightConflictWar") != null
					? json.findValue("tGroupMaxRoomCostPerNightConflictWar").asText()
					: null;
			String tGroupMaxFoodCostPerDayConflictWar = json.findValue("tGroupMaxFoodCostPerDayConflictWar") != null
					? json.findValue("tGroupMaxFoodCostPerDayConflictWar").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 13);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingPCW = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			List<TravelGroupPermittedBoardingLodging> newTravelPermittedBoardingLodgingPCW = new ArrayList<TravelGroupPermittedBoardingLodging>();
			if (tGroupBoardingLodgingTypeConflictWar != null && !tGroupBoardingLodgingTypeConflictWar.equals("")) {
				String tGroupPermBnL[] = tGroupBoardingLodgingTypeConflictWar.split(",");
				String tGroupPerMaxRcpN[] = tGroupMaxRoomCostPerNightConflictWar.split(",");
				String tGroupPerMaxFcpD[] = tGroupMaxFoodCostPerDayConflictWar.split(",");
				for (int i = 0; i < tGroupPermBnL.length; i++) {
					TravelGroupPermittedBoardingLodging tGroupPBnL = new TravelGroupPermittedBoardingLodging();
					tGroupPBnL.setTravelgroup(tGroup);
					tGroupPBnL.setOrganization(users.getOrganization());
					tGroupPBnL.setAccomodationType(AccomodationType.findById(Long.parseLong(tGroupPermBnL[i])));
					if (!tGroupPerMaxRcpN[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(Double.parseDouble(tGroupPerMaxRcpN[i]));
					} else {
						tGroupPBnL.setMaxPermittedRoomCostPerNight(0.0);
					}
					if (!tGroupPerMaxFcpD[i].trim().equals("")) {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(Double.parseDouble(tGroupPerMaxFcpD[i]));
					} else {
						tGroupPBnL.setMaxPermittedFoodCostPerDay(0.0);
					}
					tGroupPBnL.setCityType(13);
					newTravelPermittedBoardingLodgingPCW.add(tGroupPBnL);
				}
			}
			List<List<TravelGroupPermittedBoardingLodging>> tGroupBoardingnLodgingListPCW = ListUtility
					.gettGroupBnL(oldTravelPermittedBoardingLodgingPCW, newTravelPermittedBoardingLodgingPCW);
			for (int i = 0; i < tGroupBoardingnLodgingListPCW.size(); i++) {
				if (i == 0) {
					List<TravelGroupPermittedBoardingLodging> oldtGroupBnL = tGroupBoardingnLodgingListPCW.get(i);
					if (oldtGroupBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : oldtGroupBnL) {
							entityManager.remove(tGBnL);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupPermittedBoardingLodging> newtGBnL = tGroupBoardingnLodgingListPCW.get(i);
					if (newtGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : newtGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupPermittedBoardingLodging> updatetGBnL = tGroupBoardingnLodgingListPCW.get(i);
					if (updatetGBnL != null) {
						for (TravelGroupPermittedBoardingLodging tGBnL : updatetGBnL) {
							genericDAO.saveOrUpdate(tGBnL, users, entityManager);
						}
					}
				}
			}
			// travel group place of conflict warzone boarding and lodging maximum permitted
			// room cost and food cost add update logic end
			// travel group travel mode less than 100 maximum permitted one way and return
			// fare add update logic start
			String tGrouplessThanHundredModesOfTravel = json.findValue("tGrouplessThanHundredModesOfTravel") != null
					? json.findValue("tGrouplessThanHundredModesOfTravel").asText()
					: null;
			String tGrouplessThanHundredMaxOneWayFare = json.findValue("tGrouplessThanHundredMaxOneWayFare") != null
					? json.findValue("tGrouplessThanHundredMaxOneWayFare").asText()
					: null;
			String tGrouplessThanHundredMaxReturnFare = json.findValue("tGrouplessThanHundredMaxReturnFare") != null
					? json.findValue("tGrouplessThanHundredMaxReturnFare").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 1L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGrouplessThanHundredModesOfTravel != null && !tGrouplessThanHundredModesOfTravel.equals("")) {
				String tGroupLTHDMKTM[] = tGrouplessThanHundredModesOfTravel.split(",");
				String tGroupLTHDMKTMOWF[] = tGrouplessThanHundredMaxOneWayFare.split(",");
				String tGroupLTHDMKTMRF[] = tGrouplessThanHundredMaxReturnFare.split(",");
				for (int i = 0; i < tGroupLTHDMKTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupLTHDMKATM = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupLTHDMKATM.setTravelgroup(tGroup);
					tGroupLTHDMKATM.setOrganization(users.getOrganization());
					tGroupLTHDMKATM.setTravelMode(TravelMode.findById(Long.parseLong(tGroupLTHDMKTM[i])));
					tGroupLTHDMKATM.setDistanceMilesKms(DistanceMilesKm.findById(1L));
					if (!tGroupLTHDMKTMOWF[i].trim().equals("")) {
						tGroupLTHDMKATM.setOneWayFare(Double.parseDouble(tGroupLTHDMKTMOWF[i]));
					} else {
						tGroupLTHDMKATM.setOneWayFare(0.0);
					}
					if (!tGroupLTHDMKTMRF[i].trim().equals("")) {
						tGroupLTHDMKATM.setReturnFare(Double.parseDouble(tGroupLTHDMKTMRF[i]));
					} else {
						tGroupLTHDMKATM.setReturnFare(0.0);
					}
					newTravelGroupDistanceMilesAllowedTravelModes.add(tGroupLTHDMKATM);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupLTHDMKATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupDistanceMilesAllowedTravelModes, newTravelGroupDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupLTHDMKATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupLTHDMKATM = tGroupLTHDMKATMList.get(i);
					if (oldtGroupLTHDMKATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGLTHDMKATM : oldtGroupLTHDMKATM) {
							entityManager.remove(tGLTHDMKATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGLTHDMKATM = tGroupLTHDMKATMList.get(i);
					if (newtGLTHDMKATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGLTHDMKATM : newtGLTHDMKATM) {
							genericDAO.saveOrUpdate(tGLTHDMKATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGLTHDMKATM = tGroupLTHDMKATMList.get(i);
					if (updatetGLTHDMKATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGLTHDMKATM : updatetGLTHDMKATM) {
							genericDAO.saveOrUpdate(tGLTHDMKATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode less than 100 maximum permitted one way and return
			// fare add update logic end
			// travel group travel mode 100-250 maximum permitted one way and return fare
			// add update logic start
			String tGrouphundredToTwoFiftyModesOfTravel = json.findValue("tGrouphundredToTwoFiftyModesOfTravel") != null
					? json.findValue("tGrouphundredToTwoFiftyModesOfTravel").asText()
					: null;
			String tGrouphundredToTwoFiftyMaxOneWayFare = json.findValue("tGrouphundredToTwoFiftyMaxOneWayFare") != null
					? json.findValue("tGrouphundredToTwoFiftyMaxOneWayFare").asText()
					: null;
			String tGrouphundredToTwoFiftyMaxReturnFare = json.findValue("tGrouphundredToTwoFiftyMaxReturnFare") != null
					? json.findValue("tGrouphundredToTwoFiftyMaxReturnFare").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 2L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupHTTHDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupHTTHDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGrouphundredToTwoFiftyModesOfTravel != null && !tGrouphundredToTwoFiftyModesOfTravel.equals("")) {
				String tGroupHTTHTM[] = tGrouphundredToTwoFiftyModesOfTravel.split(",");
				String tGroupHTTHOWF[] = tGrouphundredToTwoFiftyMaxOneWayFare.split(",");
				String tGroupLHTTHRF[] = tGrouphundredToTwoFiftyMaxReturnFare.split(",");
				for (int i = 0; i < tGroupHTTHTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupHTTHTMNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupHTTHTMNew.setTravelgroup(tGroup);
					tGroupHTTHTMNew.setOrganization(users.getOrganization());
					tGroupHTTHTMNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupHTTHTM[i])));
					tGroupHTTHTMNew.setDistanceMilesKms(DistanceMilesKm.findById(2L));
					if (!tGroupHTTHOWF[i].trim().equals("")) {
						tGroupHTTHTMNew.setOneWayFare(Double.parseDouble(tGroupHTTHOWF[i]));
					} else {
						tGroupHTTHTMNew.setOneWayFare(0.0);
					}
					if (!tGroupLHTTHRF[i].trim().equals("")) {
						tGroupHTTHTMNew.setReturnFare(Double.parseDouble(tGroupLHTTHRF[i]));
					} else {
						tGroupHTTHTMNew.setReturnFare(0.0);
					}
					newTravelGroupHTTHDistanceMilesAllowedTravelModes.add(tGroupHTTHTMNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupHTTHATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupHTTHDistanceMilesAllowedTravelModes,
					newTravelGroupHTTHDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupHTTHATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupHTTHATM = tGroupHTTHATMList.get(i);
					if (oldtGroupHTTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGHTTHATM : oldtGroupHTTHATM) {
							entityManager.remove(tGHTTHATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGHTTHATM = tGroupHTTHATMList.get(i);
					if (newtGHTTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGHTTHATM : newtGHTTHATM) {
							genericDAO.saveOrUpdate(tGHTTHATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGHTTHATM = tGroupHTTHATMList.get(i);
					if (updatetGHTTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGHTTHATM : updatetGHTTHATM) {
							genericDAO.saveOrUpdate(tGHTTHATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode 100-250 maximum permitted one way and return fare
			// add update logic end
			// travel group travel mode 250-500 maximum permitted one way and return fare
			// add update logic start
			String tGrouptwoFiftyToFiveHundredModesOfTravel = json
					.findValue("tGrouptwoFiftyToFiveHundredModesOfTravel") != null
							? json.findValue("tGrouptwoFiftyToFiveHundredModesOfTravel").asText()
							: null;
			String tGrouptwoFiftyToFiveHundredMaxOneWayFare = json
					.findValue("tGrouptwoFiftyToFiveHundredMaxOneWayFare") != null
							? json.findValue("tGrouptwoFiftyToFiveHundredMaxOneWayFare").asText()
							: null;
			String tGrouptwoFiftyToFiveHundredMaxReturnFare = json
					.findValue("tGrouptwoFiftyToFiveHundredMaxReturnFare") != null
							? json.findValue("tGrouptwoFiftyToFiveHundredMaxReturnFare").asText()
							: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 3L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTFTFHDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupTFTFHDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGrouptwoFiftyToFiveHundredModesOfTravel != null
					&& !tGrouptwoFiftyToFiveHundredModesOfTravel.equals("")) {
				String tGroupTFTFHTM[] = tGrouptwoFiftyToFiveHundredModesOfTravel.split(",");
				String tGroupTFTFHOWF[] = tGrouptwoFiftyToFiveHundredMaxOneWayFare.split(",");
				String tGroupTFTFHRF[] = tGrouptwoFiftyToFiveHundredMaxReturnFare.split(",");
				for (int i = 0; i < tGroupTFTFHTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupTFTFHNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupTFTFHNew.setTravelgroup(tGroup);
					tGroupTFTFHNew.setOrganization(users.getOrganization());
					tGroupTFTFHNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupTFTFHTM[i])));
					tGroupTFTFHNew.setDistanceMilesKms(DistanceMilesKm.findById(3L));
					if (!tGroupTFTFHOWF[i].trim().equals("")) {
						tGroupTFTFHNew.setOneWayFare(Double.parseDouble(tGroupTFTFHOWF[i]));
					} else {
						tGroupTFTFHNew.setOneWayFare(0.0);
					}
					if (!tGroupTFTFHRF[i].trim().equals("")) {
						tGroupTFTFHNew.setReturnFare(Double.parseDouble(tGroupTFTFHRF[i]));
					} else {
						tGroupTFTFHNew.setReturnFare(0.0);
					}
					newTravelGroupTFTFHDistanceMilesAllowedTravelModes.add(tGroupTFTFHNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupTFTFHATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupTFTFHDistanceMilesAllowedTravelModes,
					newTravelGroupTFTFHDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupTFTFHATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupTFTFHATM = tGroupTFTFHATMList.get(i);
					if (oldtGroupTFTFHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTFTFHATM : oldtGroupTFTFHATM) {
							entityManager.remove(tGTFTFHATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGTFTFHATM = tGroupTFTFHATMList.get(i);
					if (newtGTFTFHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTFTFHATM : newtGTFTFHATM) {
							genericDAO.saveOrUpdate(tGTFTFHATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGTFTFHATM = tGroupTFTFHATMList.get(i);
					if (updatetGTFTFHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTFTFHATM : updatetGTFTFHATM) {
							genericDAO.saveOrUpdate(tGTFTFHATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode 250-500 maximum permitted one way and return fare
			// add update logic end
			// travel group travel mode 500-1000 maximum permitted one way and return fare
			// add update logic start
			String tGroupfiveHundredToThousandModesOfTravel = json
					.findValue("tGroupfiveHundredToThousandModesOfTravel") != null
							? json.findValue("tGroupfiveHundredToThousandModesOfTravel").asText()
							: null;
			String tGroupfiveHundredToThousandMaxOneWayFare = json
					.findValue("tGroupfiveHundredToThousandMaxOneWayFare") != null
							? json.findValue("tGroupfiveHundredToThousandMaxOneWayFare").asText()
							: null;
			String tGroupfiveHundredToThousandMaxReturnFare = json
					.findValue("tGroupfiveHundredToThousandMaxReturnFare") != null
							? json.findValue("tGroupfiveHundredToThousandMaxReturnFare").asText()
							: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 4L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupFHTTDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupFHTTDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGroupfiveHundredToThousandModesOfTravel != null
					&& !tGroupfiveHundredToThousandModesOfTravel.equals("")) {
				String tGroupFHTTTM[] = tGroupfiveHundredToThousandModesOfTravel.split(",");
				String tGroupFHTTOWF[] = tGroupfiveHundredToThousandMaxOneWayFare.split(",");
				String tGroupFHTTRF[] = tGroupfiveHundredToThousandMaxReturnFare.split(",");
				for (int i = 0; i < tGroupFHTTTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupFHTTNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupFHTTNew.setTravelgroup(tGroup);
					tGroupFHTTNew.setOrganization(users.getOrganization());
					tGroupFHTTNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupFHTTTM[i])));
					tGroupFHTTNew.setDistanceMilesKms(DistanceMilesKm.findById(4L));
					if (!tGroupFHTTOWF[i].trim().equals("")) {
						tGroupFHTTNew.setOneWayFare(Double.parseDouble(tGroupFHTTOWF[i]));
					} else {
						tGroupFHTTNew.setOneWayFare(0.0);
					}
					if (!tGroupFHTTRF[i].trim().equals("")) {
						tGroupFHTTNew.setReturnFare(Double.parseDouble(tGroupFHTTRF[i]));
					} else {
						tGroupFHTTNew.setReturnFare(0.0);
					}
					newTravelGroupFHTTDistanceMilesAllowedTravelModes.add(tGroupFHTTNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupFHTTATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupFHTTDistanceMilesAllowedTravelModes,
					newTravelGroupFHTTDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupFHTTATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupFHTTATM = tGroupFHTTATMList.get(i);
					if (oldtGroupFHTTATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFHTTATM : oldtGroupFHTTATM) {
							entityManager.remove(tGFHTTATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGFHTTATM = tGroupFHTTATMList.get(i);
					if (newtGFHTTATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFHTTATM : newtGFHTTATM) {
							genericDAO.saveOrUpdate(tGFHTTATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGFHTTATM = tGroupFHTTATMList.get(i);
					if (updatetGFHTTATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFHTTATM : updatetGFHTTATM) {
							genericDAO.saveOrUpdate(tGFHTTATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode 500-1000 maximum permitted one way and return fare
			// add update logic end
			// travel group travel mode 1000-1500 maximum permitted one way and return fare
			// add update logic start
			String tGroupthousandToThousandFiveHundredModesOfTravel = json
					.findValue("tGroupthousandToThousandFiveHundredModesOfTravel") != null
							? json.findValue("tGroupthousandToThousandFiveHundredModesOfTravel").asText()
							: null;
			String tGroupthousandToThousandFiveHundredMaxOneWayFare = json
					.findValue("tGroupthousandToThousandFiveHundredMaxOneWayFare") != null
							? json.findValue("tGroupthousandToThousandFiveHundredMaxOneWayFare").asText()
							: null;
			String tGroupthousandToThousandFiveHundredMaxReturnFare = json
					.findValue("tGroupthousandToThousandFiveHundredMaxReturnFare") != null
							? json.findValue("tGroupthousandToThousandFiveHundredMaxReturnFare").asText()
							: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 5L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTTTFHDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupTTTFHDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGroupthousandToThousandFiveHundredModesOfTravel != null
					&& !tGroupthousandToThousandFiveHundredModesOfTravel.equals("")) {
				String tGroupTTTFHTM[] = tGroupthousandToThousandFiveHundredModesOfTravel.split(",");
				String tGroupTTTFHOWF[] = tGroupthousandToThousandFiveHundredMaxOneWayFare.split(",");
				String tGroupTTTFHRF[] = tGroupthousandToThousandFiveHundredMaxReturnFare.split(",");
				for (int i = 0; i < tGroupTTTFHTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupTTTFHNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupTTTFHNew.setTravelgroup(tGroup);
					tGroupTTTFHNew.setOrganization(users.getOrganization());
					tGroupTTTFHNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupTTTFHTM[i])));
					tGroupTTTFHNew.setDistanceMilesKms(DistanceMilesKm.findById(5L));
					if (!tGroupTTTFHOWF[i].trim().equals("")) {
						tGroupTTTFHNew.setOneWayFare(Double.parseDouble(tGroupTTTFHOWF[i]));
					} else {
						tGroupTTTFHNew.setOneWayFare(0.0);
					}
					if (!tGroupTTTFHRF[i].trim().equals("")) {
						tGroupTTTFHNew.setReturnFare(Double.parseDouble(tGroupTTTFHRF[i]));
					} else {
						tGroupTTTFHNew.setReturnFare(0.0);
					}
					newTravelGroupTTTFHDistanceMilesAllowedTravelModes.add(tGroupTTTFHNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupTTTFHATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupTTTFHDistanceMilesAllowedTravelModes,
					newTravelGroupTTTFHDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupTTTFHATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupTTTFHATM = tGroupTTTFHATMList.get(i);
					if (oldtGroupTTTFHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTTFHATM : oldtGroupTTTFHATM) {
							entityManager.remove(tGTTTFHATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGTTTFHATM = tGroupTTTFHATMList.get(i);
					if (newtGTTTFHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTTFHATM : newtGTTTFHATM) {
							genericDAO.saveOrUpdate(tGTTTFHATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGTTTFHATM = tGroupTTTFHATMList.get(i);
					if (updatetGTTTFHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTTFHATM : updatetGTTTFHATM) {
							genericDAO.saveOrUpdate(tGTTTFHATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode 1000-1500 maximum permitted one way and return fare
			// add update logic end
			// travel group travel mode 1500-2000 maximum permitted one way and return fare
			// add update logic start
			String tGroupthousandFiveHundredToTwoThousandModesOfTravel = json
					.findValue("tGroupthousandFiveHundredToTwoThousandModesOfTravel") != null
							? json.findValue("tGroupthousandFiveHundredToTwoThousandModesOfTravel").asText()
							: null;
			String tGroupthousandFiveHundredToTwoThousandMaxOneWayFare = json
					.findValue("tGroupthousandFiveHundredToTwoThousandMaxOneWayFare") != null
							? json.findValue("tGroupthousandFiveHundredToTwoThousandMaxOneWayFare").asText()
							: null;
			String tGroupthousandFiveHundredToTwoThousandMaxReturnFare = json
					.findValue("tGroupthousandFiveHundredToTwoThousandMaxReturnFare") != null
							? json.findValue("tGroupthousandFiveHundredToTwoThousandMaxReturnFare").asText()
							: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 6L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTFHTTHDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupTFHTTHDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGroupthousandFiveHundredToTwoThousandModesOfTravel != null
					&& !tGroupthousandFiveHundredToTwoThousandModesOfTravel.equals("")) {
				String tGroupTFHTTHTM[] = tGroupthousandFiveHundredToTwoThousandModesOfTravel.split(",");
				String tGroupTFHTTHOWF[] = tGroupthousandFiveHundredToTwoThousandMaxOneWayFare.split(",");
				String tGroupTFHTTHRF[] = tGroupthousandFiveHundredToTwoThousandMaxReturnFare.split(",");
				for (int i = 0; i < tGroupTFHTTHTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupTFHTTHNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupTFHTTHNew.setTravelgroup(tGroup);
					tGroupTFHTTHNew.setOrganization(users.getOrganization());
					tGroupTFHTTHNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupTFHTTHTM[i])));
					tGroupTFHTTHNew.setDistanceMilesKms(DistanceMilesKm.findById(6L));
					if (!tGroupTFHTTHOWF[i].trim().equals("")) {
						tGroupTFHTTHNew.setOneWayFare(Double.parseDouble(tGroupTFHTTHOWF[i]));
					} else {
						tGroupTFHTTHNew.setOneWayFare(0.0);
					}
					if (!tGroupTFHTTHRF[i].trim().equals("")) {
						tGroupTFHTTHNew.setReturnFare(Double.parseDouble(tGroupTFHTTHRF[i]));
					} else {
						tGroupTFHTTHNew.setReturnFare(0.0);
					}
					newTravelGroupTFHTTHDistanceMilesAllowedTravelModes.add(tGroupTFHTTHNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupFHTTHATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupTFHTTHDistanceMilesAllowedTravelModes,
					newTravelGroupTFHTTHDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupFHTTHATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupFHTTHATM = tGroupFHTTHATMList.get(i);
					if (oldtGroupFHTTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFHTTHATM : oldtGroupFHTTHATM) {
							entityManager.remove(tGFHTTHATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGFHTTHATM = tGroupFHTTHATMList.get(i);
					if (newtGFHTTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFHTTHATM : newtGFHTTHATM) {
							genericDAO.saveOrUpdate(tGFHTTHATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGFHTTHATM = tGroupFHTTHATMList.get(i);
					if (updatetGFHTTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFHTTHATM : updatetGFHTTHATM) {
							genericDAO.saveOrUpdate(tGFHTTHATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode 1500-2000 maximum permitted one way and return fare
			// add update logic end
			// travel group travel mode 2000-2500 maximum permitted one way and return fare
			// add update logic start
			String tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel = json
					.findValue("tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel") != null
							? json.findValue("tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel").asText()
							: null;
			String tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare = json
					.findValue("tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare") != null
							? json.findValue("tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare").asText()
							: null;
			String tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare = json
					.findValue("tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare") != null
							? json.findValue("tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare").asText()
							: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 7L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTTHTTHFHDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupTTHTTHFHDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel != null
					&& !tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel.equals("")) {
				String tGroupTTHTTHFHTM[] = tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel.split(",");
				String tGroupTTHTTHFHOWF[] = tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare.split(",");
				String tGroupTTHTTHFHRF[] = tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare.split(",");
				for (int i = 0; i < tGroupTTHTTHFHTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupTTHTTHFHNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupTTHTTHFHNew.setTravelgroup(tGroup);
					tGroupTTHTTHFHNew.setOrganization(users.getOrganization());
					tGroupTTHTTHFHNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupTTHTTHFHTM[i])));
					tGroupTTHTTHFHNew.setDistanceMilesKms(DistanceMilesKm.findById(7L));
					if (!tGroupTTHTTHFHOWF[i].trim().equals("")) {
						tGroupTTHTTHFHNew.setOneWayFare(Double.parseDouble(tGroupTTHTTHFHOWF[i]));
					} else {
						tGroupTTHTTHFHNew.setOneWayFare(0.0);
					}
					if (!tGroupTTHTTHFHRF[i].trim().equals("")) {
						tGroupTTHTTHFHNew.setReturnFare(Double.parseDouble(tGroupTTHTTHFHRF[i]));
					} else {
						tGroupTTHTTHFHNew.setReturnFare(0.0);
					}
					newTravelGroupTTHTTHFHDistanceMilesAllowedTravelModes.add(tGroupTTHTTHFHNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupTTHTTHFHATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupTTHTTHFHDistanceMilesAllowedTravelModes,
					newTravelGroupTTHTTHFHDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupTTHTTHFHATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupTTHTTHFHATM = tGroupTTHTTHFHATMList
							.get(i);
					if (oldtGroupTTHTTHFHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTHTTHFHATM : oldtGroupTTHTTHFHATM) {
							entityManager.remove(tGTTHTTHFHATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGTTHTTHFHATM = tGroupTTHTTHFHATMList.get(i);
					if (newtGTTHTTHFHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTHTTHFHATM : newtGTTHTTHFHATM) {
							genericDAO.saveOrUpdate(tGTTHTTHFHATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGTTHTTHFHATM = tGroupTTHTTHFHATMList
							.get(i);
					if (updatetGTTHTTHFHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTHTTHFHATM : updatetGTTHTTHFHATM) {
							genericDAO.saveOrUpdate(tGTTHTTHFHATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode 2000-2500 maximum permitted one way and return fare
			// add update logic end
			// travel group travel mode 2500-3000 maximum permitted one way and return fare
			// add update logic start
			String tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel = json
					.findValue("tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel") != null
							? json.findValue("tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel").asText()
							: null;
			String tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare = json
					.findValue("tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare") != null
							? json.findValue("tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare").asText()
							: null;
			String tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare = json
					.findValue("tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare") != null
							? json.findValue("tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare").asText()
							: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 8L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTTHFHTTHDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupTTHFHTTHDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel != null
					&& !tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel.equals("")) {
				String tGroupTTHFHTTHTM[] = tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel.split(",");
				String tGroupTTHFHTTHOWF[] = tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare.split(",");
				String tGroupTTHFHTTHRF[] = tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare.split(",");
				for (int i = 0; i < tGroupTTHFHTTHTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupTTHFHTTHNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupTTHFHTTHNew.setTravelgroup(tGroup);
					tGroupTTHFHTTHNew.setOrganization(users.getOrganization());
					tGroupTTHFHTTHNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupTTHFHTTHTM[i])));
					tGroupTTHFHTTHNew.setDistanceMilesKms(DistanceMilesKm.findById(8L));
					if (!tGroupTTHFHTTHOWF[i].trim().equals("")) {
						tGroupTTHFHTTHNew.setOneWayFare(Double.parseDouble(tGroupTTHFHTTHOWF[i]));
					} else {
						tGroupTTHFHTTHNew.setOneWayFare(0.0);
					}
					if (!tGroupTTHFHTTHRF[i].trim().equals("")) {
						tGroupTTHFHTTHNew.setReturnFare(Double.parseDouble(tGroupTTHFHTTHRF[i]));
					} else {
						tGroupTTHFHTTHNew.setReturnFare(0.0);
					}
					newTravelGroupTTHFHTTHDistanceMilesAllowedTravelModes.add(tGroupTTHFHTTHNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupTTHFHTTHATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupTTHFHTTHDistanceMilesAllowedTravelModes,
					newTravelGroupTTHFHTTHDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupTTHFHTTHATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupTTHFHTTHATM = tGroupTTHFHTTHATMList
							.get(i);
					if (oldtGroupTTHFHTTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTHFHTTHATM : oldtGroupTTHFHTTHATM) {
							entityManager.remove(tGTTHFHTTHATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGTTHFHTTHATM = tGroupTTHFHTTHATMList.get(i);
					if (newtGTTHFHTTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTHFHTTHATM : newtGTTHFHTTHATM) {
							genericDAO.saveOrUpdate(tGTTHFHTTHATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGTTHFHTTHATM = tGroupTTHFHTTHATMList
							.get(i);
					if (updatetGTTHFHTTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTHFHTTHATM : updatetGTTHFHTTHATM) {
							genericDAO.saveOrUpdate(tGTTHFHTTHATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode 2500-3000 maximum permitted one way and return fare
			// add update logic end
			// travel group travel mode 3000-4000 maximum permitted one way and return fare
			// add update logic start
			String tGroupthreeThousandToFourThousandModesOfTravel = json
					.findValue("tGroupthreeThousandToFourThousandModesOfTravel") != null
							? json.findValue("tGroupthreeThousandToFourThousandModesOfTravel").asText()
							: null;
			String tGroupthreeThousandToFourThousandMaxOneWayFare = json
					.findValue("tGroupthreeThousandToFourThousandMaxOneWayFare") != null
							? json.findValue("tGroupthreeThousandToFourThousandMaxOneWayFare").asText()
							: null;
			String tGroupthreeThousandToFourThousandMaxReturnFare = json
					.findValue("tGroupthreeThousandToFourThousandMaxReturnFare") != null
							? json.findValue("tGroupthreeThousandToFourThousandMaxReturnFare").asText()
							: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 9L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTTHTFTHDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupTTHTFTHDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGroupthreeThousandToFourThousandModesOfTravel != null
					&& !tGroupthreeThousandToFourThousandModesOfTravel.equals("")) {
				String tGroupTTHTFTHTM[] = tGroupthreeThousandToFourThousandModesOfTravel.split(",");
				String tGroupTTHTFTHOWF[] = tGroupthreeThousandToFourThousandMaxOneWayFare.split(",");
				String tGroupTTHTFTHRF[] = tGroupthreeThousandToFourThousandMaxReturnFare.split(",");
				for (int i = 0; i < tGroupTTHTFTHTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupTTHTFTHNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupTTHTFTHNew.setTravelgroup(tGroup);
					tGroupTTHTFTHNew.setOrganization(users.getOrganization());
					tGroupTTHTFTHNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupTTHTFTHTM[i])));
					tGroupTTHTFTHNew.setDistanceMilesKms(DistanceMilesKm.findById(9L));
					if (!tGroupTTHTFTHOWF[i].trim().equals("")) {
						tGroupTTHTFTHNew.setOneWayFare(Double.parseDouble(tGroupTTHTFTHOWF[i]));
					} else {
						tGroupTTHTFTHNew.setOneWayFare(0.0);
					}
					if (!tGroupTTHTFTHRF[i].trim().equals("")) {
						tGroupTTHTFTHNew.setReturnFare(Double.parseDouble(tGroupTTHTFTHRF[i]));
					} else {
						tGroupTTHTFTHNew.setReturnFare(0.0);
					}
					newTravelGroupTTHTFTHDistanceMilesAllowedTravelModes.add(tGroupTTHTFTHNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupTTHTFTHATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupTTHTFTHDistanceMilesAllowedTravelModes,
					newTravelGroupTTHTFTHDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupTTHTFTHATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupTTHTFTHATM = tGroupTTHTFTHATMList
							.get(i);
					if (oldtGroupTTHTFTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTHTFTHATM : oldtGroupTTHTFTHATM) {
							entityManager.remove(tGTTHTFTHATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGTTHTFTHATM = tGroupTTHTFTHATMList.get(i);
					if (newtGTTHTFTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTHTFTHATM : newtGTTHTFTHATM) {
							genericDAO.saveOrUpdate(tGTTHTFTHATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGTTHTFTHATM = tGroupTTHTFTHATMList.get(i);
					if (updatetGTTHTFTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTTHTFTHATM : updatetGTTHTFTHATM) {
							genericDAO.saveOrUpdate(tGTTHTFTHATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode 3000-4000 maximum permitted one way and return fare
			// add update logic end
			// travel group travel mode 4000-5000 maximum permitted one way and return fare
			// add update logic start
			String tGroupfourToFiveThousandModesOfTravel = json
					.findValue("tGroupfourToFiveThousandModesOfTravel") != null
							? json.findValue("tGroupfourToFiveThousandModesOfTravel").asText()
							: null;
			String tGroupfourToFiveThousandMaxOneWayFare = json
					.findValue("tGroupfourToFiveThousandMaxOneWayFare") != null
							? json.findValue("tGroupfourToFiveThousandMaxOneWayFare").asText()
							: null;
			String tGroupfourToFiveThousandMaxReturnFare = json
					.findValue("tGroupfourToFiveThousandMaxReturnFare") != null
							? json.findValue("tGroupfourToFiveThousandMaxReturnFare").asText()
							: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 10L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupFTHTFTHDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupFTHTFTHDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGroupfourToFiveThousandModesOfTravel != null && !tGroupfourToFiveThousandModesOfTravel.equals("")) {
				String tGroupFTHTFTHTM[] = tGroupfourToFiveThousandModesOfTravel.split(",");
				String tGroupFTHTFTHOWF[] = tGroupfourToFiveThousandMaxOneWayFare.split(",");
				String tGroupFTHTFTHRF[] = tGroupfourToFiveThousandMaxReturnFare.split(",");
				for (int i = 0; i < tGroupFTHTFTHTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupFTHTFTHNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupFTHTFTHNew.setTravelgroup(tGroup);
					tGroupFTHTFTHNew.setOrganization(users.getOrganization());
					tGroupFTHTFTHNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupFTHTFTHTM[i])));
					tGroupFTHTFTHNew.setDistanceMilesKms(DistanceMilesKm.findById(10L));
					if (!tGroupFTHTFTHOWF[i].trim().equals("")) {
						tGroupFTHTFTHNew.setOneWayFare(Double.parseDouble(tGroupFTHTFTHOWF[i]));
					} else {
						tGroupFTHTFTHNew.setOneWayFare(0.0);
					}
					if (!tGroupFTHTFTHRF[i].trim().equals("")) {
						tGroupFTHTFTHNew.setReturnFare(Double.parseDouble(tGroupFTHTFTHRF[i]));
					} else {
						tGroupFTHTFTHNew.setReturnFare(0.0);
					}
					newTravelGroupFTHTFTHDistanceMilesAllowedTravelModes.add(tGroupFTHTFTHNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupFTHTFTHATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupFTHTFTHDistanceMilesAllowedTravelModes,
					newTravelGroupFTHTFTHDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupFTHTFTHATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupFTHTFTHATM = tGroupFTHTFTHATMList
							.get(i);
					if (oldtGroupFTHTFTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFTHTFTHATM : oldtGroupFTHTFTHATM) {
							entityManager.remove(tGFTHTFTHATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGFTHTFTHATM = tGroupFTHTFTHATMList.get(i);
					if (newtGFTHTFTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFTHTFTHATM : newtGFTHTFTHATM) {
							genericDAO.saveOrUpdate(tGFTHTFTHATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGFTHTFTHATM = tGroupFTHTFTHATMList.get(i);
					if (updatetGFTHTFTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFTHTFTHATM : updatetGFTHTFTHATM) {
							genericDAO.saveOrUpdate(tGFTHTFTHATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode 4000-5000 maximum permitted one way and return fare
			// add update logic end
			// travel group travel mode 5000-6000 maximum permitted one way and return fare
			// add update logic start
			String tGroupfiveToSixThousandModesOfTravel = json.findValue("tGroupfiveToSixThousandModesOfTravel") != null
					? json.findValue("tGroupfiveToSixThousandModesOfTravel").asText()
					: null;
			String tGroupfiveToSixThousandMaxOneWayFare = json.findValue("tGroupfiveToSixThousandMaxOneWayFare") != null
					? json.findValue("tGroupfiveToSixThousandMaxOneWayFare").asText()
					: null;
			String tGroupfiveToSixThousandMaxReturnFare = json.findValue("tGroupfiveToSixThousandMaxReturnFare") != null
					? json.findValue("tGroupfiveToSixThousandMaxReturnFare").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 11L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupFTHTSTHDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupFTHTSTHDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGroupfiveToSixThousandModesOfTravel != null && !tGroupfiveToSixThousandModesOfTravel.equals("")) {
				String tGroupFTHTSTHTM[] = tGroupfiveToSixThousandModesOfTravel.split(",");
				String tGroupFTHTSTHOWF[] = tGroupfiveToSixThousandMaxOneWayFare.split(",");
				String tGroupFTHTSTHRF[] = tGroupfiveToSixThousandMaxReturnFare.split(",");
				for (int i = 0; i < tGroupFTHTSTHTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupFTHTSTHNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupFTHTSTHNew.setTravelgroup(tGroup);
					tGroupFTHTSTHNew.setOrganization(users.getOrganization());
					tGroupFTHTSTHNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupFTHTSTHTM[i])));
					tGroupFTHTSTHNew.setDistanceMilesKms(DistanceMilesKm.findById(11L));
					if (!tGroupFTHTSTHOWF[i].trim().equals("")) {
						tGroupFTHTSTHNew.setOneWayFare(Double.parseDouble(tGroupFTHTSTHOWF[i]));
					} else {
						tGroupFTHTSTHNew.setOneWayFare(0.0);
					}
					if (!tGroupFTHTSTHRF[i].trim().equals("")) {
						tGroupFTHTSTHNew.setReturnFare(Double.parseDouble(tGroupFTHTSTHRF[i]));
					} else {
						tGroupFTHTSTHNew.setReturnFare(0.0);
					}
					newTravelGroupFTHTSTHDistanceMilesAllowedTravelModes.add(tGroupFTHTSTHNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupFTHTSTHATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupFTHTSTHDistanceMilesAllowedTravelModes,
					newTravelGroupFTHTSTHDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupFTHTSTHATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupFTHTSTHATM = tGroupFTHTSTHATMList
							.get(i);
					if (oldtGroupFTHTSTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFTHTSTHATM : oldtGroupFTHTSTHATM) {
							entityManager.remove(tGFTHTSTHATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGFTHTSTHATM = tGroupFTHTSTHATMList.get(i);
					if (newtGFTHTSTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFTHTSTHATM : newtGFTHTSTHATM) {
							genericDAO.saveOrUpdate(tGFTHTSTHATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGFTHTSTHATM = tGroupFTHTSTHATMList.get(i);
					if (updatetGFTHTSTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGFTHTSTHATM : updatetGFTHTSTHATM) {
							genericDAO.saveOrUpdate(tGFTHTSTHATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode 5000-6000 maximum permitted one way and return fare
			// add update logic end
			// travel group travel mode 6000-7000 maximum permitted one way and return fare
			// add update logic start
			String tGroupsixToSevenThousandModesOfTravel = json
					.findValue("tGroupsixToSevenThousandModesOfTravel") != null
							? json.findValue("tGroupsixToSevenThousandModesOfTravel").asText()
							: null;
			String tGroupsixToSevenThousandMaxOneWayFare = json
					.findValue("tGroupsixToSevenThousandMaxOneWayFare") != null
							? json.findValue("tGroupsixToSevenThousandMaxOneWayFare").asText()
							: null;
			String tGroupsixToSevenThousandMxReturnFare = json.findValue("tGroupsixToSevenThousandMxReturnFare") != null
					? json.findValue("tGroupsixToSevenThousandMxReturnFare").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 12L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupSTHTSTHDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupTHTSTHDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGroupsixToSevenThousandModesOfTravel != null && !tGroupsixToSevenThousandModesOfTravel.equals("")) {
				String tGroupSTHTSTHTM[] = tGroupsixToSevenThousandModesOfTravel.split(",");
				String tGroupSTHTSTHOWF[] = tGroupsixToSevenThousandMaxOneWayFare.split(",");
				String tGroupSTHTSTHRF[] = tGroupsixToSevenThousandMxReturnFare.split(",");
				for (int i = 0; i < tGroupSTHTSTHTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupSTHTSTHNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupSTHTSTHNew.setTravelgroup(tGroup);
					tGroupSTHTSTHNew.setOrganization(users.getOrganization());
					tGroupSTHTSTHNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupSTHTSTHTM[i])));
					tGroupSTHTSTHNew.setDistanceMilesKms(DistanceMilesKm.findById(12L));
					if (!tGroupSTHTSTHOWF[i].trim().equals("")) {
						tGroupSTHTSTHNew.setOneWayFare(Double.parseDouble(tGroupSTHTSTHOWF[i]));
					} else {
						tGroupSTHTSTHNew.setOneWayFare(0.0);
					}
					if (!tGroupSTHTSTHRF[i].trim().equals("")) {
						tGroupSTHTSTHNew.setReturnFare(Double.parseDouble(tGroupSTHTSTHRF[i]));
					} else {
						tGroupSTHTSTHNew.setReturnFare(0.0);
					}
					newTravelGroupTHTSTHDistanceMilesAllowedTravelModes.add(tGroupSTHTSTHNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupSTHTSTHATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupSTHTSTHDistanceMilesAllowedTravelModes,
					newTravelGroupTHTSTHDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupSTHTSTHATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupSTHTSTHATM = tGroupSTHTSTHATMList
							.get(i);
					if (oldtGroupSTHTSTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGSTHTSTHATM : oldtGroupSTHTSTHATM) {
							entityManager.remove(tGSTHTSTHATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGSTHTSTHATM = tGroupSTHTSTHATMList.get(i);
					if (newtGSTHTSTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGSTHTSTHATM : newtGSTHTSTHATM) {
							genericDAO.saveOrUpdate(tGSTHTSTHATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGSTHTSTHATM = tGroupSTHTSTHATMList.get(i);
					if (updatetGSTHTSTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGSTHTSTHATM : updatetGSTHTSTHATM) {
							genericDAO.saveOrUpdate(tGSTHTSTHATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode 6000-7000 maximum permitted one way and return fare
			// add update logic end
			// travel group travel mode above 7000 maximum permitted one way and return fare
			// add update logic start
			String tGroupaboveSevenThousandModesOfTravel = json
					.findValue("tGroupaboveSevenThousandModesOfTravel") != null
							? json.findValue("tGroupaboveSevenThousandModesOfTravel").asText()
							: null;
			String tGroupaboveSevenThousandMaxOneWayFare = json
					.findValue("tGroupaboveSevenThousandMaxOneWayFare") != null
							? json.findValue("tGroupaboveSevenThousandMaxOneWayFare").asText()
							: null;
			String tGroupaboveSevenThousandMaxReturnFare = json
					.findValue("tGroupaboveSevenThousandMaxReturnFare") != null
							? json.findValue("tGroupaboveSevenThousandMaxReturnFare").asText()
							: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 13L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupABVSTHDistanceMilesAllowedTravelModes = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> newTravelGroupABVSTHDistanceMilesAllowedTravelModes = new ArrayList<TravelGroupDistanceMilesKmsAllowedTravelMode>();
			if (tGroupaboveSevenThousandModesOfTravel != null && !tGroupaboveSevenThousandModesOfTravel.equals("")) {
				String tGroupABVSTHTM[] = tGroupaboveSevenThousandModesOfTravel.split(",");
				String tGroupABVSTHOWF[] = tGroupaboveSevenThousandMaxOneWayFare.split(",");
				String tGroupABVSTHRF[] = tGroupaboveSevenThousandMaxReturnFare.split(",");
				for (int i = 0; i < tGroupABVSTHTM.length; i++) {
					TravelGroupDistanceMilesKmsAllowedTravelMode tGroupABVSTHNew = new TravelGroupDistanceMilesKmsAllowedTravelMode();
					tGroupABVSTHNew.setTravelgroup(tGroup);
					tGroupABVSTHNew.setOrganization(users.getOrganization());
					tGroupABVSTHNew.setTravelMode(TravelMode.findById(Long.parseLong(tGroupABVSTHTM[i])));
					tGroupABVSTHNew.setDistanceMilesKms(DistanceMilesKm.findById(13L));
					if (!tGroupABVSTHOWF[i].trim().equals("")) {
						tGroupABVSTHNew.setOneWayFare(Double.parseDouble(tGroupABVSTHOWF[i]));
					} else {
						tGroupABVSTHNew.setOneWayFare(0.0);
					}
					if (!tGroupABVSTHRF[i].trim().equals("")) {
						tGroupABVSTHNew.setReturnFare(Double.parseDouble(tGroupABVSTHRF[i]));
					} else {
						tGroupABVSTHNew.setReturnFare(0.0);
					}
					newTravelGroupABVSTHDistanceMilesAllowedTravelModes.add(tGroupABVSTHNew);
				}
			}
			List<List<TravelGroupDistanceMilesKmsAllowedTravelMode>> tGroupABVSTHATMList = ListUtility.getTGDMKMATM(
					oldTravelGroupABVSTHDistanceMilesAllowedTravelModes,
					newTravelGroupABVSTHDistanceMilesAllowedTravelModes);
			for (int i = 0; i < tGroupABVSTHATMList.size(); i++) {
				if (i == 0) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldtGroupABVSTHATM = tGroupABVSTHATMList.get(i);
					if (oldtGroupABVSTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGABVSTHATM : oldtGroupABVSTHATM) {
							entityManager.remove(tGABVSTHATM);
						}
					}
				}
				if (i == 1) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> newtGABVSTHATM = tGroupABVSTHATMList.get(i);
					if (newtGABVSTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGABVSTHATM : newtGABVSTHATM) {
							genericDAO.saveOrUpdate(tGABVSTHATM, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<TravelGroupDistanceMilesKmsAllowedTravelMode> updatetGABVSTHATM = tGroupABVSTHATMList.get(i);
					if (updatetGABVSTHATM != null) {
						for (TravelGroupDistanceMilesKmsAllowedTravelMode tGABVSTHATM : updatetGABVSTHATM) {
							genericDAO.saveOrUpdate(tGABVSTHATM, users, entityManager);
						}
					}
				}
			}
			// travel group travel mode above 7000 maximum permitted one way and return fare
			// add update logic end
			// travel group travel maximum permitted daily limit for other expenses add
			// update logic start
			String tGroupCtryCapital = json.findValue("tGroupCtryCapital") != null
					? json.findValue("tGroupCtryCapital").asText()
					: null;
			String tGroupStateCapital = json.findValue("tGroupStateCapital") != null
					? json.findValue("tGroupStateCapital").asText()
					: null;
			String tGrouptravelMaxMetroCity = json.findValue("tGrouptravelMaxMetroCity") != null
					? json.findValue("tGrouptravelMaxMetroCity").asText()
					: null;
			String tGrouptravelMaxOtherCity = json.findValue("tGrouptravelMaxOtherCity") != null
					? json.findValue("tGrouptravelMaxOtherCity").asText()
					: null;
			String tGrouptravelMaxTown = json.findValue("tGrouptravelMaxTown") != null
					? json.findValue("tGrouptravelMaxTown").asText()
					: null;
			String tGrouptravelMaxCountry = json.findValue("tGrouptravelMaxCountry") != null
					? json.findValue("tGrouptravelMaxCountry").asText()
					: null;
			String tGrouptravelMaxMunicipality = json.findValue("tGrouptravelMaxMunicipality") != null
					? json.findValue("tGrouptravelMaxMunicipality").asText()
					: null;
			String tGrouptravelMaxVillage = json.findValue("tGrouptravelMaxVillage") != null
					? json.findValue("tGrouptravelMaxVillage").asText()
					: null;
			String tGrouptravelMaxRemoteLoc = json.findValue("tGrouptravelMaxRemoteLoc") != null
					? json.findValue("tGrouptravelMaxRemoteLoc").asText()
					: null;
			String tGrouptravelMaxAwayCityTown = json.findValue("tGrouptravelMaxAwayCityTown") != null
					? json.findValue("tGrouptravelMaxAwayCityTown").asText()
					: null;
			String tGrouptravelMaxHillStation = json.findValue("tGrouptravelMaxHillStation") != null
					? json.findValue("tGrouptravelMaxHillStation").asText()
					: null;
			String tGrouptravelMaxresort = json.findValue("tGrouptravelMaxresort") != null
					? json.findValue("tGrouptravelMaxresort").asText()
					: null;
			String tGrouptravelMaxWarZone = json.findValue("tGrouptravelMaxWarZone") != null
					? json.findValue("tGrouptravelMaxWarZone").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("presentStatus", 1);
			TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses = genericDAO
					.getByCriteria(TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses.class, criterias,
							entityManager);
			if (oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses == null) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses = new TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses();
			}
			oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setTravelgroup(tGroup);
			oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setOrganization(users.getOrganization());
			if (tGroupCtryCapital != null && !tGroupCtryCapital.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses
						.setCountryCapital(Double.parseDouble(tGroupCtryCapital));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setCountryCapital(0.0);
			}
			if (tGroupStateCapital != null && !tGroupStateCapital.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses
						.setStateCapital(Double.parseDouble(tGroupStateCapital));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setStateCapital(0.0);
			}
			if (tGrouptravelMaxMetroCity != null && !tGrouptravelMaxMetroCity.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses
						.setMetroCity(Double.parseDouble(tGrouptravelMaxMetroCity));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setMetroCity(0.0);
			}
			if (tGrouptravelMaxOtherCity != null && !tGrouptravelMaxOtherCity.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses
						.setOtherCities(Double.parseDouble(tGrouptravelMaxOtherCity));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setOtherCities(0.0);
			}
			if (tGrouptravelMaxTown != null && !tGrouptravelMaxTown.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setTown(Double.parseDouble(tGrouptravelMaxTown));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setTown(0.0);
			}
			if (tGrouptravelMaxCountry != null && !tGrouptravelMaxCountry.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses
						.setCounty(Double.parseDouble(tGrouptravelMaxCountry));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setCounty(0.0);
			}
			if (tGrouptravelMaxMunicipality != null && !tGrouptravelMaxMunicipality.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses
						.setMunicipality(Double.parseDouble(tGrouptravelMaxMunicipality));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setMunicipality(0.0);
			}
			if (tGrouptravelMaxVillage != null && !tGrouptravelMaxVillage.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses
						.setVillage(Double.parseDouble(tGrouptravelMaxVillage));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setVillage(0.0);
			}
			if (tGrouptravelMaxRemoteLoc != null && !tGrouptravelMaxRemoteLoc.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses
						.setRemoteLocation(Double.parseDouble(tGrouptravelMaxRemoteLoc));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setRemoteLocation(0.0);
			}
			if (tGrouptravelMaxAwayCityTown != null && !tGrouptravelMaxAwayCityTown.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses
						.setTwentyMilesAwayFromClosestCityTown(Double.parseDouble(tGrouptravelMaxAwayCityTown));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setTwentyMilesAwayFromClosestCityTown(0.0);
			}
			if (tGrouptravelMaxHillStation != null && !tGrouptravelMaxHillStation.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses
						.setHillStation(Double.parseDouble(tGrouptravelMaxHillStation));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setHillStation(0.0);
			}
			if (tGrouptravelMaxresort != null && !tGrouptravelMaxresort.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setResort(Double.parseDouble(tGrouptravelMaxresort));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setResort(0.0);
			}
			if (tGrouptravelMaxWarZone != null && !tGrouptravelMaxWarZone.equals("")) {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses
						.setConflictWarZonePlace(Double.parseDouble(tGrouptravelMaxWarZone));
			} else {
				oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.setConflictWarZonePlace(0.0);
			}
			genericDAO.saveOrUpdate(oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses, users, entityManager);
			// travel group travel maximum permitted daily limit for other expenses add
			// update logic end
			// travel group travel fixed per diam add update logic start
			String tGroupfixedCountryCapital = json.findValue("tGroupfixedCountryCapital") != null
					? json.findValue("tGroupfixedCountryCapital").asText()
					: null;
			String tGroupfixedStateCapital = json.findValue("tGroupfixedStateCapital") != null
					? json.findValue("tGroupfixedStateCapital").asText()
					: null;
			String tGroupfixedMetroCity = json.findValue("tGroupfixedMetroCity") != null
					? json.findValue("tGroupfixedMetroCity").asText()
					: null;
			String tGrouptravelfixedOtherCity = json.findValue("tGrouptravelfixedOtherCity") != null
					? json.findValue("tGrouptravelfixedOtherCity").asText()
					: null;
			String tGroupfixedTown = json.findValue("tGroupfixedTown") != null
					? json.findValue("tGroupfixedTown").asText()
					: null;
			String tGroupfixedCountry = json.findValue("tGroupfixedCountry") != null
					? json.findValue("tGroupfixedCountry").asText()
					: null;
			String tGroupfixedMunicipality = json.findValue("tGroupfixedMunicipality") != null
					? json.findValue("tGroupfixedMunicipality").asText()
					: null;
			String tGroupfixedVillage = json.findValue("tGroupfixedVillage") != null
					? json.findValue("tGroupfixedVillage").asText()
					: null;
			String tGroupfixedRemoteLoc = json.findValue("tGroupfixedRemoteLoc") != null
					? json.findValue("tGroupfixedRemoteLoc").asText()
					: null;
			String tGroupfixedAwayCityTown = json.findValue("tGroupfixedAwayCityTown") != null
					? json.findValue("tGroupfixedAwayCityTown").asText()
					: null;
			String tGroupfixedHillStation = json.findValue("tGroupfixedHillStation") != null
					? json.findValue("tGroupfixedHillStation").asText()
					: null;
			String tGroupfixedresort = json.findValue("tGroupfixedresort") != null
					? json.findValue("tGroupfixedresort").asText()
					: null;
			String tGroupfixedWarZone = json.findValue("tGroupfixedWarZone") != null
					? json.findValue("tGroupfixedWarZone").asText()
					: null;
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("presentStatus", 1);
			TravelGroupFixedDailyPerDIAM oldtGroupFixedDailyPerDIAM = genericDAO
					.getByCriteria(TravelGroupFixedDailyPerDIAM.class, criterias, entityManager);
			if (oldtGroupFixedDailyPerDIAM == null) {
				oldtGroupFixedDailyPerDIAM = new TravelGroupFixedDailyPerDIAM();
			}
			oldtGroupFixedDailyPerDIAM.setTravelgroup(tGroup);
			oldtGroupFixedDailyPerDIAM.setOrganization(users.getOrganization());
			if (tGroupfixedCountryCapital != null && !tGroupfixedCountryCapital.equals("")) {
				oldtGroupFixedDailyPerDIAM.setCountryCapital(Double.parseDouble(tGroupfixedCountryCapital));
			} else {
				oldtGroupFixedDailyPerDIAM.setCountryCapital(0.0);
			}
			if (tGroupfixedStateCapital != null && !tGroupfixedStateCapital.equals("")) {
				oldtGroupFixedDailyPerDIAM.setStateCapital(Double.parseDouble(tGroupfixedStateCapital));
			} else {
				oldtGroupFixedDailyPerDIAM.setStateCapital(0.0);
			}
			if (tGroupfixedMetroCity != null && !tGroupfixedMetroCity.equals("")) {
				oldtGroupFixedDailyPerDIAM.setMetroCity(Double.parseDouble(tGroupfixedMetroCity));
			} else {
				oldtGroupFixedDailyPerDIAM.setMetroCity(0.0);
			}
			if (tGrouptravelfixedOtherCity != null && !tGrouptravelfixedOtherCity.equals("")) {
				oldtGroupFixedDailyPerDIAM.setOtherCities(Double.parseDouble(tGrouptravelfixedOtherCity));
			} else {
				oldtGroupFixedDailyPerDIAM.setOtherCities(0.0);
			}
			if (tGroupfixedTown != null && !tGroupfixedTown.equals("")) {
				oldtGroupFixedDailyPerDIAM.setTown(Double.parseDouble(tGroupfixedTown));
			} else {
				oldtGroupFixedDailyPerDIAM.setTown(0.0);
			}
			if (tGroupfixedCountry != null && !tGroupfixedCountry.equals("")) {
				oldtGroupFixedDailyPerDIAM.setCounty(Double.parseDouble(tGroupfixedCountry));
			} else {
				oldtGroupFixedDailyPerDIAM.setCounty(0.0);
			}
			if (tGroupfixedMunicipality != null && !tGroupfixedMunicipality.equals("")) {
				oldtGroupFixedDailyPerDIAM.setMunicipality(Double.parseDouble(tGroupfixedMunicipality));
			} else {
				oldtGroupFixedDailyPerDIAM.setMunicipality(0.0);
			}
			if (tGroupfixedVillage != null && !tGroupfixedVillage.equals("")) {
				oldtGroupFixedDailyPerDIAM.setVillage(Double.parseDouble(tGroupfixedVillage));
			} else {
				oldtGroupFixedDailyPerDIAM.setVillage(0.0);
			}
			if (tGroupfixedRemoteLoc != null && !tGroupfixedRemoteLoc.equals("")) {
				oldtGroupFixedDailyPerDIAM.setRemoteLocation(Double.parseDouble(tGroupfixedRemoteLoc));
			} else {
				oldtGroupFixedDailyPerDIAM.setRemoteLocation(0.0);
			}
			if (tGroupfixedAwayCityTown != null && !tGroupfixedAwayCityTown.equals("")) {
				oldtGroupFixedDailyPerDIAM
						.setTwentyMilesAwayFromClosestCityTown(Double.parseDouble(tGroupfixedAwayCityTown));
			} else {
				oldtGroupFixedDailyPerDIAM.setTwentyMilesAwayFromClosestCityTown(0.0);
			}
			if (tGroupfixedHillStation != null && !tGroupfixedHillStation.equals("")) {
				oldtGroupFixedDailyPerDIAM.setHillStation(Double.parseDouble(tGroupfixedHillStation));
			} else {
				oldtGroupFixedDailyPerDIAM.setHillStation(0.0);
			}
			if (tGroupfixedresort != null && !tGroupfixedresort.equals("")) {
				oldtGroupFixedDailyPerDIAM.setResort(Double.parseDouble(tGroupfixedresort));
			} else {
				oldtGroupFixedDailyPerDIAM.setResort(0.0);
			}
			if (tGroupfixedWarZone != null && !tGroupfixedWarZone.equals("")) {
				oldtGroupFixedDailyPerDIAM.setConflictWarZonePlace(Double.parseDouble(tGroupfixedWarZone));
			} else {
				oldtGroupFixedDailyPerDIAM.setConflictWarZonePlace(0.0);
			}
			genericDAO.saveOrUpdate(oldtGroupFixedDailyPerDIAM, users, entityManager);
			// travel group travel fixed per diam add update logic end
			String travelGroupKlRemarks = json.findValue("tGroupKlRemarks") != null
					? json.findValue("tGroupKlRemarks").asText()
					: null;
			String travelGroupKlMandatory = json.findValue("tGroupKlMandatory") != null
					? json.findValue("tGroupKlMandatory").asText()
					: null;
			String tGroupKlContentArray[] = travelGroupKlRemarks.split(",");
			String tGroupKlContentMandatoryArray[] = travelGroupKlMandatory.split(",");
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("presentStatus", 1);
			List<TravelGroupKnowledgeLibrary> existingTravelGroupKl = genericDAO
					.findByCriteria(TravelGroupKnowledgeLibrary.class, criterias, entityManager);
			if (!existingTravelGroupKl.isEmpty() && existingTravelGroupKl.size() > 0) {
				for (int i = 0; i < existingTravelGroupKl.size(); i++) {
					existingTravelGroupKl.get(i).setTravelgroup(tGroup);
					existingTravelGroupKl.get(i).setOrganization(users.getOrganization());
					existingTravelGroupKl.get(i).setKlContent(tGroupKlContentArray[i]);
					existingTravelGroupKl.get(i).setKlMandatory(Integer.parseInt(tGroupKlContentMandatoryArray[i]));
					genericDAO.saveOrUpdate(existingTravelGroupKl.get(i), users, entityManager);
				}
			} else {
				for (int i = 0; i < tGroupKlContentArray.length; i++) {
					TravelGroupKnowledgeLibrary newTgKl = new TravelGroupKnowledgeLibrary();
					newTgKl.setTravelgroup(tGroup);
					newTgKl.setOrganization(users.getOrganization());
					newTgKl.setKlContent(tGroupKlContentArray[i]);
					newTgKl.setKlMandatory(Integer.parseInt(tGroupKlContentMandatoryArray[i]));
					genericDAO.saveOrUpdate(newTgKl, users, entityManager);
				}
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
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result createExpenseGroup(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction transaction = entityManager.getTransaction();
		Http.Session session = request.session();
		ObjectNode result = Json.newObject();
		ArrayNode expensean = result.putArray("expensearray");
		Users users = null;
		try {
			transaction.begin();
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("useremail").asText();
			session.adding("email", useremail);
			users = getUserInfo(request);
			String eGroupExpenseGroupName = json.findValue("eGroupExpenseGroupName").asText();
			String eGroupEntityHiddenId = json.findValue("eGroupEntityHiddenId").asText();
			ExpenseGroup eGroup = null;
			if (eGroupEntityHiddenId != null && !eGroupEntityHiddenId.equals("")) {
				eGroup = ExpenseGroup.findById(Long.parseLong(eGroupEntityHiddenId));
			} else {
				eGroup = new ExpenseGroup();
			}
			eGroup.setExpenseGroupName(eGroupExpenseGroupName);
			eGroup.setOrganization(users.getOrganization());
			genericDAO.saveOrUpdate(eGroup, users, entityManager);
			ObjectNode expenserow = Json.newObject();
			expenserow.put("expenseGroupId", eGroup.getId());
			expenserow.put("expenseGroupName", eGroup.getExpenseGroupName());
			expensean.add(expenserow);
			// expense group expense items maximum permitted advance and monthly monetary
			// limit for reimbursement add update logic start
			String eGroupexpenseItems = json.findValue("eGroupexpenseItems") != null
					? json.findValue("eGroupexpenseItems").asText()
					: null;
			String eGroupexpenseItemMaximumPermittedAdvance = json
					.findValue("eGroupexpenseItemMaximumPermittedAdvance") != null
							? json.findValue("eGroupexpenseItemMaximumPermittedAdvance").asText()
							: null;
			String eGroupexpenseItemMonthlyMonetoryLimitForReimbursement = json
					.findValue("eGroupexpenseItemMonthlyMonetoryLimitForReimbursement") != null
							? json.findValue("eGroupexpenseItemMonthlyMonetoryLimitForReimbursement").asText()
							: null;
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("expenseGroup.id", eGroup.getId());
			criterias.put("presentStatus", 1);
			List<ExpenseGroupExpenseItemMonetoryClaim> oldeGroupExpenseItemsMonetoryClaim = genericDAO
					.findByCriteria(ExpenseGroupExpenseItemMonetoryClaim.class, criterias, entityManager);
			List<ExpenseGroupExpenseItemMonetoryClaim> neweGroupExpenseItemsMonetoryClaim = new ArrayList<ExpenseGroupExpenseItemMonetoryClaim>();
			if (eGroupexpenseItems != null && !eGroupexpenseItems.equals("")) {
				String eGroupExpenseItems[] = eGroupexpenseItems.split(",");
				String eGroupExpenseItemsMaximumPermittedAdvance[] = eGroupexpenseItemMaximumPermittedAdvance
						.split(",");
				String eGroupExpenseItemsMonthlyMonetoryLimit[] = eGroupexpenseItemMonthlyMonetoryLimitForReimbursement
						.split(",");
				for (int i = 0; i < eGroupExpenseItems.length; i++) {
					ExpenseGroupExpenseItemMonetoryClaim eGroupEIMC = new ExpenseGroupExpenseItemMonetoryClaim();
					eGroupEIMC.setExpenseGroup(eGroup);
					eGroupEIMC.setOrganization(users.getOrganization());
					Specifics expItems = Specifics.findById(Long.parseLong(eGroupExpenseItems[i]));
					eGroupEIMC.setSpecificsItem(expItems);
					eGroupEIMC.setSpecificsParticulars(expItems.getParticularsId());
					if (!eGroupExpenseItemsMaximumPermittedAdvance[i].trim().equals("")) {
						eGroupEIMC.setMaximumPermittedAdvance(
								Double.parseDouble(eGroupExpenseItemsMaximumPermittedAdvance[i]));
					} else {
						eGroupEIMC.setMaximumPermittedAdvance(0.0);
					}
					if (i >= 0 && i < eGroupExpenseItemsMonthlyMonetoryLimit.length) {
						if (!eGroupExpenseItemsMonthlyMonetoryLimit[i].trim().equals("")) {
							eGroupEIMC.setMonthlyMonetoryLimitForReimbursement(
									Double.parseDouble(eGroupExpenseItemsMonthlyMonetoryLimit[i]));
						} else {
							eGroupEIMC.setMonthlyMonetoryLimitForReimbursement(0.0);
						}
						neweGroupExpenseItemsMonetoryClaim.add(eGroupEIMC);
					}
				}
			}
			List<List<ExpenseGroupExpenseItemMonetoryClaim>> eGroupExpenseItemMonetoryClaim = ListUtility
					.geteGroupEIMC(oldeGroupExpenseItemsMonetoryClaim, neweGroupExpenseItemsMonetoryClaim);
			for (int i = 0; i < eGroupExpenseItemMonetoryClaim.size(); i++) {
				if (i == 0) {
					List<ExpenseGroupExpenseItemMonetoryClaim> oldeGroupEIMC = eGroupExpenseItemMonetoryClaim.get(i);
					if (oldeGroupEIMC != null) {
						for (ExpenseGroupExpenseItemMonetoryClaim eGEIMC : oldeGroupEIMC) {
							entityManager.remove(eGEIMC);
						}
					}
				}
				if (i == 1) {
					List<ExpenseGroupExpenseItemMonetoryClaim> neweGroupEIMC = eGroupExpenseItemMonetoryClaim.get(i);
					if (neweGroupEIMC != null) {
						for (ExpenseGroupExpenseItemMonetoryClaim eGEIMC : neweGroupEIMC) {
							genericDAO.saveOrUpdate(eGEIMC, users, entityManager);
						}
					}
				}
				if (i == 2) {
					List<ExpenseGroupExpenseItemMonetoryClaim> updateeGroupEIMC = eGroupExpenseItemMonetoryClaim.get(i);
					if (updateeGroupEIMC != null) {
						for (ExpenseGroupExpenseItemMonetoryClaim eGEIMC : updateeGroupEIMC) {
							genericDAO.saveOrUpdate(eGEIMC, users, entityManager);
						}
					}
				}
			}
			// travel group boarding and lodging maximum permitted room cost and food cost
			// add update logic end
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
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result showTravelGroup(Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode results = Json.newObject();
		results.put("result", false);
		// EntityManager entityManager=getEntityManager();
		Users users = null;
		try {
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("userEmail").asText();
			String tGroupEntityId = json.findValue("tGroupEntityId").asText();
			Travel_Group tGroup = Travel_Group.findById(Long.parseLong(tGroupEntityId));
			if (tGroup != null) {
				results.put("result", true);
			}
			// session.adding("email", useremail);
			users = getUserInfo(request);
			if (users == null) {
				return unauthorized();
			}
			ArrayNode tGroupDetailAn = results.putArray("travelGroupEntityDetails");
			ObjectNode tGroupDetailRow = Json.newObject();
			tGroupDetailRow.put("tGroupId", tGroup.getId());
			tGroupDetailRow.put("tGroupGroupName", tGroup.getTravelGroupName());
			// country capital BnL data fetch start
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 1);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingCtryCap = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeCtryCap = "";
			String tGroupMaxRoomCostPerNightCtryCap = "";
			String tGroupMaxFoodCostPerDayCtryCap = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingCtryCap) {
				tGroupBoardingLodgingTypeCtryCap += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightCtryCap += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayCtryCap += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeCtryCap.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeCtryCap",
						tGroupBoardingLodgingTypeCtryCap.substring(0, tGroupBoardingLodgingTypeCtryCap.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightCtryCap",
						tGroupMaxRoomCostPerNightCtryCap.substring(0, tGroupMaxRoomCostPerNightCtryCap.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayCtryCap",
						tGroupMaxFoodCostPerDayCtryCap.substring(0, tGroupMaxFoodCostPerDayCtryCap.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeCtryCap", tGroupBoardingLodgingTypeCtryCap);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightCtryCap", tGroupMaxRoomCostPerNightCtryCap);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayCtryCap", tGroupMaxFoodCostPerDayCtryCap);
			}
			// country capital BnL data fetch end
			// state capital BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 2);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingStateCap = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeStateCap = "";
			String tGroupMaxRoomCostPerNightStateCap = "";
			String tGroupMaxFoodCostPerDayStateCap = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingStateCap) {
				tGroupBoardingLodgingTypeStateCap += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightStateCap += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayStateCap += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeStateCap.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeStateCap",
						tGroupBoardingLodgingTypeStateCap.substring(0, tGroupBoardingLodgingTypeStateCap.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightStateCap",
						tGroupMaxRoomCostPerNightStateCap.substring(0, tGroupMaxRoomCostPerNightStateCap.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayStateCap",
						tGroupMaxFoodCostPerDayStateCap.substring(0, tGroupMaxFoodCostPerDayStateCap.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeStateCap", tGroupBoardingLodgingTypeStateCap);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightStateCap", tGroupMaxRoomCostPerNightStateCap);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayStateCap", tGroupMaxFoodCostPerDayStateCap);
			}
			// state capital BnL data fetch end
			// metro city BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 3);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingMetroCity = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeMetroCity = "";
			String tGroupMaxRoomCostPerNightMetroCity = "";
			String tGroupMaxFoodCostPerDayMetroCity = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingMetroCity) {
				tGroupBoardingLodgingTypeMetroCity += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightMetroCity += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayMetroCity += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeMetroCity.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeMetroCity", tGroupBoardingLodgingTypeMetroCity
						.substring(0, tGroupBoardingLodgingTypeMetroCity.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightMetroCity", tGroupMaxRoomCostPerNightMetroCity
						.substring(0, tGroupMaxRoomCostPerNightMetroCity.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayMetroCity",
						tGroupMaxFoodCostPerDayMetroCity.substring(0, tGroupMaxFoodCostPerDayMetroCity.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeMetroCity", tGroupBoardingLodgingTypeMetroCity);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightMetroCity", tGroupMaxRoomCostPerNightMetroCity);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayMetroCity", tGroupMaxFoodCostPerDayMetroCity);
			}
			// metro city BnL data fetch end
			// other city BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 4);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingOtherCity = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeOtherCity = "";
			String tGroupMaxRoomCostPerNightOtherCity = "";
			String tGroupMaxFoodCostPerDayOtherCity = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingOtherCity) {
				tGroupBoardingLodgingTypeOtherCity += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightOtherCity += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayOtherCity += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeOtherCity.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeOtherCity", tGroupBoardingLodgingTypeOtherCity
						.substring(0, tGroupBoardingLodgingTypeOtherCity.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightOtherCity", tGroupMaxRoomCostPerNightOtherCity
						.substring(0, tGroupMaxRoomCostPerNightOtherCity.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayOtherCity",
						tGroupMaxFoodCostPerDayOtherCity.substring(0, tGroupMaxFoodCostPerDayOtherCity.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeOtherCity", tGroupBoardingLodgingTypeOtherCity);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightOtherCity", tGroupMaxRoomCostPerNightOtherCity);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayOtherCity", tGroupMaxFoodCostPerDayOtherCity);
			}
			// other city BnL data fetch end
			// town BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 5);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingOtherTown = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeTown = "";
			String tGroupMaxRoomCostPerNightTown = "";
			String tGroupMaxFoodCostPerDayTown = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingOtherTown) {
				tGroupBoardingLodgingTypeTown += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightTown += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayTown += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeTown.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeTown",
						tGroupBoardingLodgingTypeTown.substring(0, tGroupBoardingLodgingTypeTown.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightTown",
						tGroupMaxRoomCostPerNightTown.substring(0, tGroupMaxRoomCostPerNightTown.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayTown",
						tGroupMaxFoodCostPerDayTown.substring(0, tGroupMaxFoodCostPerDayTown.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeTown", tGroupBoardingLodgingTypeTown);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightTown", tGroupMaxRoomCostPerNightTown);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayTown", tGroupMaxFoodCostPerDayTown);
			}
			// town BnL data fetch end
			// county BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 6);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingCounty = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeCountry = "";
			String tGroupMaxRoomCostPerNightCountry = "";
			String tGroupMaxFoodCostPerDayCountry = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingCounty) {
				tGroupBoardingLodgingTypeCountry += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightCountry += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayCountry += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeCountry.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeCountry",
						tGroupBoardingLodgingTypeCountry.substring(0, tGroupBoardingLodgingTypeCountry.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightCountry",
						tGroupMaxRoomCostPerNightCountry.substring(0, tGroupMaxRoomCostPerNightCountry.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayCountry",
						tGroupMaxFoodCostPerDayCountry.substring(0, tGroupMaxFoodCostPerDayCountry.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeCountry", tGroupBoardingLodgingTypeCountry);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightCountry", tGroupMaxRoomCostPerNightCountry);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayCountry", tGroupMaxFoodCostPerDayCountry);
			}
			// county BnL data fetch end
			// municipality BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 7);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingMunicipality = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeMunicipality = "";
			String tGroupMaxRoomCostPerNightMunicipality = "";
			String tGroupMaxFoodCostPerDayMunicipality = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingMunicipality) {
				tGroupBoardingLodgingTypeMunicipality += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightMunicipality += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayMunicipality += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeMunicipality.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeMunicipality", tGroupBoardingLodgingTypeMunicipality
						.substring(0, tGroupBoardingLodgingTypeMunicipality.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightMunicipality", tGroupMaxRoomCostPerNightMunicipality
						.substring(0, tGroupMaxRoomCostPerNightMunicipality.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayMunicipality", tGroupMaxFoodCostPerDayMunicipality
						.substring(0, tGroupMaxFoodCostPerDayMunicipality.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeMunicipality", tGroupBoardingLodgingTypeMunicipality);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightMunicipality", tGroupMaxRoomCostPerNightMunicipality);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayMunicipality", tGroupMaxFoodCostPerDayMunicipality);
			}
			// municipality BnL data fetch end
			// village BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 8);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingVillage = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeVillage = "";
			String tGroupMaxRoomCostPerNightVillage = "";
			String tGroupMaxFoodCostPerDayVillage = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingVillage) {
				tGroupBoardingLodgingTypeVillage += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightVillage += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayVillage += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeVillage.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeVillage",
						tGroupBoardingLodgingTypeVillage.substring(0, tGroupBoardingLodgingTypeVillage.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightVillage",
						tGroupMaxRoomCostPerNightVillage.substring(0, tGroupMaxRoomCostPerNightVillage.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayVillage",
						tGroupMaxFoodCostPerDayVillage.substring(0, tGroupMaxFoodCostPerDayVillage.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeVillage", tGroupBoardingLodgingTypeVillage);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightVillage", tGroupMaxRoomCostPerNightVillage);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayVillage", tGroupMaxFoodCostPerDayVillage);
			}
			// village BnL data fetch end
			// remote location BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 9);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingRemoteLocation = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeRemoteLoc = "";
			String tGroupMaxRoomCostPerNightRemoteLoc = "";
			String tGroupMaxFoodCostPerDayRemoteLoc = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingRemoteLocation) {
				tGroupBoardingLodgingTypeRemoteLoc += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightRemoteLoc += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayRemoteLoc += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeRemoteLoc.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeRemoteLoc", tGroupBoardingLodgingTypeRemoteLoc
						.substring(0, tGroupBoardingLodgingTypeRemoteLoc.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightRemoteLoc", tGroupMaxRoomCostPerNightRemoteLoc
						.substring(0, tGroupMaxRoomCostPerNightRemoteLoc.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayRemoteLoc",
						tGroupMaxFoodCostPerDayRemoteLoc.substring(0, tGroupMaxFoodCostPerDayRemoteLoc.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeRemoteLoc", tGroupBoardingLodgingTypeRemoteLoc);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightRemoteLoc", tGroupMaxRoomCostPerNightRemoteLoc);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayRemoteLoc", tGroupMaxFoodCostPerDayRemoteLoc);
			}
			// remote location BnL data fetch end
			// twenty miles away BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 10);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingTMA = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingType20Miles = "";
			String tGroupMaxRoomCostPerNight20Miles = "";
			String tGroupMaxFoodCostPerDay20Miles = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingTMA) {
				tGroupBoardingLodgingType20Miles += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNight20Miles += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDay20Miles += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingType20Miles.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingType20Miles",
						tGroupBoardingLodgingType20Miles.substring(0, tGroupBoardingLodgingType20Miles.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNight20Miles",
						tGroupMaxRoomCostPerNight20Miles.substring(0, tGroupMaxRoomCostPerNight20Miles.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDay20Miles",
						tGroupMaxFoodCostPerDay20Miles.substring(0, tGroupMaxFoodCostPerDay20Miles.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingType20Miles", tGroupBoardingLodgingType20Miles);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNight20Miles", tGroupMaxRoomCostPerNight20Miles);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDay20Miles", tGroupMaxFoodCostPerDay20Miles);
			}
			// twenty miles away BnL data fetch end
			// hill station BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 11);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingHillStation = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeHillStation = "";
			String tGroupMaxRoomCostPerNightHillStation = "";
			String tGroupMaxFoodCostPerDayHillStation = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingHillStation) {
				tGroupBoardingLodgingTypeHillStation += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightHillStation += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayHillStation += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeHillStation.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeHillStation", tGroupBoardingLodgingTypeHillStation
						.substring(0, tGroupBoardingLodgingTypeHillStation.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightHillStation", tGroupMaxRoomCostPerNightHillStation
						.substring(0, tGroupMaxRoomCostPerNightHillStation.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayHillStation", tGroupMaxFoodCostPerDayHillStation
						.substring(0, tGroupMaxFoodCostPerDayHillStation.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeHillStation", tGroupBoardingLodgingTypeHillStation);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightHillStation", tGroupMaxRoomCostPerNightHillStation);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayHillStation", tGroupMaxFoodCostPerDayHillStation);
			}
			// hill station BnL data fetch end
			// resort BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 12);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingResort = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeResort = "";
			String tGroupMaxRoomCostPerNightResort = "";
			String tGroupMaxFoodCostPerDayResort = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingResort) {
				tGroupBoardingLodgingTypeResort += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightResort += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayResort += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeResort.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeResort",
						tGroupBoardingLodgingTypeResort.substring(0, tGroupBoardingLodgingTypeResort.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightResort",
						tGroupMaxRoomCostPerNightResort.substring(0, tGroupMaxRoomCostPerNightResort.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayResort",
						tGroupMaxFoodCostPerDayResort.substring(0, tGroupMaxFoodCostPerDayResort.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeResort", tGroupBoardingLodgingTypeResort);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightResort", tGroupMaxRoomCostPerNightResort);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayResort", tGroupMaxFoodCostPerDayResort);
			}
			// resort BnL data fetch end
			// place of conflics or warzone BnL data fetch start
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("cityType", 13);
			criterias.put("presentStatus", 1);
			List<TravelGroupPermittedBoardingLodging> oldTravelPermittedBoardingLodgingPOCW = genericDAO
					.findByCriteria(TravelGroupPermittedBoardingLodging.class, criterias, entityManager);
			String tGroupBoardingLodgingTypeConflictWar = "";
			String tGroupMaxRoomCostPerNightConflictWar = "";
			String tGroupMaxFoodCostPerDayConflictWar = "";
			for (TravelGroupPermittedBoardingLodging tGBnL : oldTravelPermittedBoardingLodgingPOCW) {
				tGroupBoardingLodgingTypeConflictWar += tGBnL.getAccomodationType().getId() + ",";
				tGroupMaxRoomCostPerNightConflictWar += tGBnL.getMaxPermittedRoomCostPerNight().toString() + ",";
				tGroupMaxFoodCostPerDayConflictWar += tGBnL.getMaxPermittedFoodCostPerDay().toString() + ",";
			}
			if (!tGroupBoardingLodgingTypeConflictWar.equals("")) {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeConflictWar", tGroupBoardingLodgingTypeConflictWar
						.substring(0, tGroupBoardingLodgingTypeConflictWar.length() - 1));
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightConflictWar", tGroupMaxRoomCostPerNightConflictWar
						.substring(0, tGroupMaxRoomCostPerNightConflictWar.length() - 1));
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayConflictWar", tGroupMaxFoodCostPerDayConflictWar
						.substring(0, tGroupMaxFoodCostPerDayConflictWar.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupBoardingLodgingTypeConflictWar", tGroupBoardingLodgingTypeConflictWar);
				tGroupDetailRow.put("tGroupMaxRoomCostPerNightConflictWar", tGroupMaxRoomCostPerNightConflictWar);
				tGroupDetailRow.put("tGroupMaxFoodCostPerDayConflictWar", tGroupMaxFoodCostPerDayConflictWar);
			}
			// place of conflics or warzone BnL data fetch end
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 1L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupDistanceMilesLTHATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			String tGrouplessThanHundredModesOfTravel = "";
			String tGrouplessThanHundredMaxOneWayFare = "";
			String tGrouplessThanHundredMaxReturnFare = "";
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGLTHATM : oldTravelGroupDistanceMilesLTHATM) {
				tGrouplessThanHundredModesOfTravel += tGLTHATM.getTravelMode().getId() + ",";
				tGrouplessThanHundredMaxOneWayFare += tGLTHATM.getOneWayFare().toString() + ",";
				tGrouplessThanHundredMaxReturnFare += tGLTHATM.getReturnFare().toString() + ",";
			}
			if (!tGrouplessThanHundredModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGrouplessThanHundredModesOfTravel", tGrouplessThanHundredModesOfTravel
						.substring(0, tGrouplessThanHundredModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGrouplessThanHundredMaxOneWayFare", tGrouplessThanHundredMaxOneWayFare
						.substring(0, tGrouplessThanHundredMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGrouplessThanHundredMaxReturnFare", tGrouplessThanHundredMaxReturnFare
						.substring(0, tGrouplessThanHundredMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGrouplessThanHundredModesOfTravel", tGrouplessThanHundredModesOfTravel);
				tGroupDetailRow.put("tGrouplessThanHundredMaxOneWayFare", tGrouplessThanHundredMaxOneWayFare);
				tGroupDetailRow.put("tGrouplessThanHundredMaxReturnFare", tGrouplessThanHundredMaxReturnFare);
			}
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 2L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupHTTHDATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			String tGrouphundredToTwoFiftyModesOfTravel = "";
			String tGrouphundredToTwoFiftyMaxOneWayFare = "";
			String tGrouphundredToTwoFiftyMaxReturnFare = "";
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGHTTHDATM : oldTravelGroupHTTHDATM) {
				tGrouphundredToTwoFiftyModesOfTravel += tGHTTHDATM.getTravelMode().getId() + ",";
				tGrouphundredToTwoFiftyMaxOneWayFare += tGHTTHDATM.getOneWayFare().toString() + ",";
				tGrouphundredToTwoFiftyMaxReturnFare += tGHTTHDATM.getReturnFare().toString() + ",";
			}
			if (!tGrouphundredToTwoFiftyModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGrouphundredToTwoFiftyModesOfTravel", tGrouphundredToTwoFiftyModesOfTravel
						.substring(0, tGrouphundredToTwoFiftyModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGrouphundredToTwoFiftyMaxOneWayFare", tGrouphundredToTwoFiftyMaxOneWayFare
						.substring(0, tGrouphundredToTwoFiftyMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGrouphundredToTwoFiftyMaxReturnFare", tGrouphundredToTwoFiftyMaxReturnFare
						.substring(0, tGrouphundredToTwoFiftyMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGrouphundredToTwoFiftyModesOfTravel", tGrouphundredToTwoFiftyModesOfTravel);
				tGroupDetailRow.put("tGrouphundredToTwoFiftyMaxOneWayFare", tGrouphundredToTwoFiftyMaxOneWayFare);
				tGroupDetailRow.put("tGrouphundredToTwoFiftyMaxReturnFare", tGrouphundredToTwoFiftyMaxReturnFare);
			}
			String tGrouptwoFiftyToFiveHundredModesOfTravel = "";
			String tGrouptwoFiftyToFiveHundredMaxOneWayFare = "";
			String tGrouptwoFiftyToFiveHundredMaxReturnFare = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 3L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTFTFHDMATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGTFTFHDMATM : oldTravelGroupTFTFHDMATM) {
				tGrouptwoFiftyToFiveHundredModesOfTravel += tGTFTFHDMATM.getTravelMode().getId() + ",";
				tGrouptwoFiftyToFiveHundredMaxOneWayFare += tGTFTFHDMATM.getOneWayFare().toString() + ",";
				tGrouptwoFiftyToFiveHundredMaxReturnFare += tGTFTFHDMATM.getReturnFare().toString() + ",";
			}
			if (!tGrouptwoFiftyToFiveHundredModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGrouptwoFiftyToFiveHundredModesOfTravel", tGrouptwoFiftyToFiveHundredModesOfTravel
						.substring(0, tGrouptwoFiftyToFiveHundredModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGrouptwoFiftyToFiveHundredMaxOneWayFare", tGrouptwoFiftyToFiveHundredMaxOneWayFare
						.substring(0, tGrouptwoFiftyToFiveHundredMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGrouptwoFiftyToFiveHundredMaxReturnFare", tGrouptwoFiftyToFiveHundredMaxReturnFare
						.substring(0, tGrouptwoFiftyToFiveHundredMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGrouptwoFiftyToFiveHundredModesOfTravel",
						tGrouptwoFiftyToFiveHundredModesOfTravel);
				tGroupDetailRow.put("tGrouptwoFiftyToFiveHundredMaxOneWayFare",
						tGrouptwoFiftyToFiveHundredMaxOneWayFare);
				tGroupDetailRow.put("tGrouptwoFiftyToFiveHundredMaxReturnFare",
						tGrouptwoFiftyToFiveHundredMaxReturnFare);
			}

			String tGroupfiveHundredToThousandModesOfTravel = "";
			String tGroupfiveHundredToThousandMaxOneWayFare = "";
			String tGroupfiveHundredToThousandMaxReturnFare = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 4L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupFHTTDMATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGroupFHTTDMATM : oldTravelGroupFHTTDMATM) {
				tGroupfiveHundredToThousandModesOfTravel += tGroupFHTTDMATM.getTravelMode().getId() + ",";
				tGroupfiveHundredToThousandMaxOneWayFare += tGroupFHTTDMATM.getOneWayFare().toString() + ",";
				tGroupfiveHundredToThousandMaxReturnFare += tGroupFHTTDMATM.getReturnFare().toString() + ",";
			}
			if (!tGroupfiveHundredToThousandModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGroupfiveHundredToThousandModesOfTravel", tGroupfiveHundredToThousandModesOfTravel
						.substring(0, tGroupfiveHundredToThousandModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGroupfiveHundredToThousandMaxOneWayFare", tGroupfiveHundredToThousandMaxOneWayFare
						.substring(0, tGroupfiveHundredToThousandMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGroupfiveHundredToThousandMaxReturnFare", tGroupfiveHundredToThousandMaxReturnFare
						.substring(0, tGroupfiveHundredToThousandMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupfiveHundredToThousandModesOfTravel",
						tGroupfiveHundredToThousandModesOfTravel);
				tGroupDetailRow.put("tGroupfiveHundredToThousandMaxOneWayFare",
						tGroupfiveHundredToThousandMaxOneWayFare);
				tGroupDetailRow.put("tGroupfiveHundredToThousandMaxReturnFare",
						tGroupfiveHundredToThousandMaxReturnFare);
			}

			String tGroupthousandToThousandFiveHundredModesOfTravel = "";
			String tGroupthousandToThousandFiveHundredMaxOneWayFare = "";
			String tGroupthousandToThousandFiveHundredMaxReturnFare = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 5L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTTTFHDMATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGroupTTTFHDMATM : oldTravelGroupTTTFHDMATM) {
				tGroupthousandToThousandFiveHundredModesOfTravel += tGroupTTTFHDMATM.getTravelMode().getId() + ",";
				tGroupthousandToThousandFiveHundredMaxOneWayFare += tGroupTTTFHDMATM.getOneWayFare().toString() + ",";
				tGroupthousandToThousandFiveHundredMaxReturnFare += tGroupTTTFHDMATM.getReturnFare().toString() + ",";
			}
			if (!tGroupthousandToThousandFiveHundredModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGroupthousandToThousandFiveHundredModesOfTravel",
						tGroupthousandToThousandFiveHundredModesOfTravel.substring(0,
								tGroupthousandToThousandFiveHundredModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGroupthousandToThousandFiveHundredMaxOneWayFare",
						tGroupthousandToThousandFiveHundredMaxOneWayFare.substring(0,
								tGroupthousandToThousandFiveHundredMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGroupthousandToThousandFiveHundredMaxReturnFare",
						tGroupthousandToThousandFiveHundredMaxReturnFare.substring(0,
								tGroupthousandToThousandFiveHundredMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupthousandToThousandFiveHundredModesOfTravel",
						tGroupthousandToThousandFiveHundredModesOfTravel);
				tGroupDetailRow.put("tGroupthousandToThousandFiveHundredMaxOneWayFare",
						tGroupthousandToThousandFiveHundredMaxOneWayFare);
				tGroupDetailRow.put("tGroupthousandToThousandFiveHundredMaxReturnFare",
						tGroupthousandToThousandFiveHundredMaxReturnFare);
			}
			String tGroupthousandFiveHundredToTwoThousandModesOfTravel = "";
			String tGroupthousandFiveHundredToTwoThousandMaxOneWayFare = "";
			String tGroupthousandFiveHundredToTwoThousandMaxReturnFare = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 6L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTFHTTHDMATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGroupTFHTTHDMATM : oldTravelGroupTFHTTHDMATM) {
				tGroupthousandFiveHundredToTwoThousandModesOfTravel += tGroupTFHTTHDMATM.getTravelMode().getId() + ",";
				tGroupthousandFiveHundredToTwoThousandMaxOneWayFare += tGroupTFHTTHDMATM.getOneWayFare().toString()
						+ ",";
				tGroupthousandFiveHundredToTwoThousandMaxReturnFare += tGroupTFHTTHDMATM.getReturnFare().toString()
						+ ",";
			}
			if (!tGroupthousandFiveHundredToTwoThousandModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGroupthousandFiveHundredToTwoThousandModesOfTravel",
						tGroupthousandFiveHundredToTwoThousandModesOfTravel.substring(0,
								tGroupthousandFiveHundredToTwoThousandModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGroupthousandFiveHundredToTwoThousandMaxOneWayFare",
						tGroupthousandFiveHundredToTwoThousandMaxOneWayFare.substring(0,
								tGroupthousandFiveHundredToTwoThousandMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGroupthousandFiveHundredToTwoThousandMaxReturnFare",
						tGroupthousandFiveHundredToTwoThousandMaxReturnFare.substring(0,
								tGroupthousandFiveHundredToTwoThousandMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupthousandFiveHundredToTwoThousandModesOfTravel",
						tGroupthousandFiveHundredToTwoThousandModesOfTravel);
				tGroupDetailRow.put("tGroupthousandFiveHundredToTwoThousandMaxOneWayFare",
						tGroupthousandFiveHundredToTwoThousandMaxOneWayFare);
				tGroupDetailRow.put("tGroupthousandFiveHundredToTwoThousandMaxReturnFare",
						tGroupthousandFiveHundredToTwoThousandMaxReturnFare);
			}
			String tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel = "";
			String tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare = "";
			String tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 7L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTTHTTHFHDMATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGroupTTHTTHFHDMATM : oldTravelGroupTTHTTHFHDMATM) {
				tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel += tGroupTTHTTHFHDMATM.getTravelMode().getId()
						+ ",";
				tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare += tGroupTTHTTHFHDMATM.getOneWayFare().toString()
						+ ",";
				tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare += tGroupTTHTTHFHDMATM.getReturnFare().toString()
						+ ",";
			}
			if (!tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel",
						tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel.substring(0,
								tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare",
						tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare.substring(0,
								tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare",
						tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare.substring(0,
								tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel",
						tGrouptwoThousandToTwoThousandFiveHundredModesOfTravel);
				tGroupDetailRow.put("tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare",
						tGrouptwoThousandToTwoThousandFiveHundredMaxOneWayFare);
				tGroupDetailRow.put("tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare",
						tGrouptwoThousandToTwoThousandFiveHundredMaxReturnFare);
			}
			String tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel = "";
			String tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare = "";
			String tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 8L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTTHFHTTHDMATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGroupTTHFHTTHDMATM : oldTravelGroupTTHFHTTHDMATM) {
				tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel += tGroupTTHFHTTHDMATM.getTravelMode().getId()
						+ ",";
				tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare += tGroupTTHFHTTHDMATM.getOneWayFare()
						.toString() + ",";
				tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare += tGroupTTHFHTTHDMATM.getReturnFare()
						.toString() + ",";
			}
			if (!tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel",
						tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel.substring(0,
								tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare",
						tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare.substring(0,
								tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare",
						tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare.substring(0,
								tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel",
						tGrouptwoThousandFiveHundredToThreeThousandModesOfTravel);
				tGroupDetailRow.put("tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare",
						tGrouptwoThousandFiveHundredToThreeThousandMaxOneWayFare);
				tGroupDetailRow.put("tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare",
						tGrouptwoThousandFiveHundredToThreeThousandMaxReturnFare);
			}
			String tGroupthreeThousandToFourThousandModesOfTravel = "";
			String tGroupthreeThousandToFourThousandMaxOneWayFare = "";
			String tGroupthreeThousandToFourThousandMaxReturnFare = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 9L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupTTHTFTHDMATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGroupTTHTFTHDMATM : oldTravelGroupTTHTFTHDMATM) {
				tGroupthreeThousandToFourThousandModesOfTravel += tGroupTTHTFTHDMATM.getTravelMode().getId() + ",";
				tGroupthreeThousandToFourThousandMaxOneWayFare += tGroupTTHTFTHDMATM.getOneWayFare().toString() + ",";
				tGroupthreeThousandToFourThousandMaxReturnFare += tGroupTTHTFTHDMATM.getReturnFare().toString() + ",";
			}
			if (!tGroupthreeThousandToFourThousandModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGroupthreeThousandToFourThousandModesOfTravel",
						tGroupthreeThousandToFourThousandModesOfTravel.substring(0,
								tGroupthreeThousandToFourThousandModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGroupthreeThousandToFourThousandMaxOneWayFare",
						tGroupthreeThousandToFourThousandMaxOneWayFare.substring(0,
								tGroupthreeThousandToFourThousandMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGroupthreeThousandToFourThousandMaxReturnFare",
						tGroupthreeThousandToFourThousandMaxReturnFare.substring(0,
								tGroupthreeThousandToFourThousandMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupthreeThousandToFourThousandModesOfTravel",
						tGroupthreeThousandToFourThousandModesOfTravel);
				tGroupDetailRow.put("tGroupthreeThousandToFourThousandMaxOneWayFare",
						tGroupthreeThousandToFourThousandMaxOneWayFare);
				tGroupDetailRow.put("tGroupthreeThousandToFourThousandMaxReturnFare",
						tGroupthreeThousandToFourThousandMaxReturnFare);
			}
			String tGroupfourToFiveThousandModesOfTravel = "";
			String tGroupfourToFiveThousandMaxOneWayFare = "";
			String tGroupfourToFiveThousandMaxReturnFare = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 10L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupFTHTFTHDMATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGroupFTHTFTHDMATM : oldTravelGroupFTHTFTHDMATM) {
				tGroupfourToFiveThousandModesOfTravel += tGroupFTHTFTHDMATM.getTravelMode().getId() + ",";
				tGroupfourToFiveThousandMaxOneWayFare += tGroupFTHTFTHDMATM.getOneWayFare().toString() + ",";
				tGroupfourToFiveThousandMaxReturnFare += tGroupFTHTFTHDMATM.getReturnFare().toString() + ",";
			}
			if (!tGroupfourToFiveThousandModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGroupfourToFiveThousandModesOfTravel", tGroupfourToFiveThousandModesOfTravel
						.substring(0, tGroupfourToFiveThousandModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGroupfourToFiveThousandMaxOneWayFare", tGroupfourToFiveThousandMaxOneWayFare
						.substring(0, tGroupfourToFiveThousandMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGroupfourToFiveThousandMaxReturnFare", tGroupfourToFiveThousandMaxReturnFare
						.substring(0, tGroupfourToFiveThousandMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupfourToFiveThousandModesOfTravel", tGroupfourToFiveThousandModesOfTravel);
				tGroupDetailRow.put("tGroupfourToFiveThousandMaxOneWayFare", tGroupfourToFiveThousandMaxOneWayFare);
				tGroupDetailRow.put("tGroupfourToFiveThousandMaxReturnFare", tGroupfourToFiveThousandMaxReturnFare);
			}
			String tGroupfiveToSixThousandModesOfTravel = "";
			String tGroupfiveToSixThousandMaxOneWayFare = "";
			String tGroupfiveToSixThousandMaxReturnFare = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 11L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupFTHTSTHDMATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGroupFTHTSTHDMATM : oldTravelGroupFTHTSTHDMATM) {
				tGroupfiveToSixThousandModesOfTravel += tGroupFTHTSTHDMATM.getTravelMode().getId() + ",";
				tGroupfiveToSixThousandMaxOneWayFare += tGroupFTHTSTHDMATM.getOneWayFare().toString() + ",";
				tGroupfiveToSixThousandMaxReturnFare += tGroupFTHTSTHDMATM.getReturnFare().toString() + ",";
			}
			if (!tGroupfiveToSixThousandModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGroupfiveToSixThousandModesOfTravel", tGroupfiveToSixThousandModesOfTravel
						.substring(0, tGroupfiveToSixThousandModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGroupfiveToSixThousandMaxOneWayFare", tGroupfiveToSixThousandMaxOneWayFare
						.substring(0, tGroupfiveToSixThousandMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGroupfiveToSixThousandMaxReturnFare", tGroupfiveToSixThousandMaxReturnFare
						.substring(0, tGroupfiveToSixThousandMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupfiveToSixThousandModesOfTravel", tGroupfiveToSixThousandModesOfTravel);
				tGroupDetailRow.put("tGroupfiveToSixThousandMaxOneWayFare", tGroupfiveToSixThousandMaxOneWayFare);
				tGroupDetailRow.put("tGroupfiveToSixThousandMaxReturnFare", tGroupfiveToSixThousandMaxReturnFare);
			}
			String tGroupsixToSevenThousandModesOfTravel = "";
			String tGroupsixToSevenThousandMaxOneWayFare = "";
			String tGroupsixToSevenThousandMxReturnFare = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 12L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupSTHTSTHDMATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGroupSTHTSTHDMATM : oldTravelGroupSTHTSTHDMATM) {
				tGroupsixToSevenThousandModesOfTravel += tGroupSTHTSTHDMATM.getTravelMode().getId() + ",";
				tGroupsixToSevenThousandMaxOneWayFare += tGroupSTHTSTHDMATM.getOneWayFare().toString() + ",";
				tGroupsixToSevenThousandMxReturnFare += tGroupSTHTSTHDMATM.getReturnFare().toString() + ",";
			}
			if (!tGroupsixToSevenThousandModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGroupsixToSevenThousandModesOfTravel", tGroupsixToSevenThousandModesOfTravel
						.substring(0, tGroupsixToSevenThousandModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGroupsixToSevenThousandMaxOneWayFare", tGroupsixToSevenThousandMaxOneWayFare
						.substring(0, tGroupsixToSevenThousandMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGroupsixToSevenThousandMxReturnFare", tGroupsixToSevenThousandMxReturnFare
						.substring(0, tGroupsixToSevenThousandMxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupsixToSevenThousandModesOfTravel", tGroupsixToSevenThousandModesOfTravel);
				tGroupDetailRow.put("tGroupsixToSevenThousandMaxOneWayFare", tGroupsixToSevenThousandMaxOneWayFare);
				tGroupDetailRow.put("tGroupsixToSevenThousandMxReturnFare", tGroupsixToSevenThousandMxReturnFare);
			}
			String tGroupaboveSevenThousandModesOfTravel = "";
			String tGroupaboveSevenThousandMaxOneWayFare = "";
			String tGroupaboveSevenThousandMaxReturnFare = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("distanceMilesKms.id", 13L);
			criterias.put("presentStatus", 1);
			List<TravelGroupDistanceMilesKmsAllowedTravelMode> oldTravelGroupABVSTHDMATM = genericDAO
					.findByCriteria(TravelGroupDistanceMilesKmsAllowedTravelMode.class, criterias, entityManager);
			for (TravelGroupDistanceMilesKmsAllowedTravelMode tGABVSTHDMATM : oldTravelGroupABVSTHDMATM) {
				tGroupaboveSevenThousandModesOfTravel += tGABVSTHDMATM.getTravelMode().getId() + ",";
				tGroupaboveSevenThousandMaxOneWayFare += tGABVSTHDMATM.getOneWayFare().toString() + ",";
				tGroupaboveSevenThousandMaxReturnFare += tGABVSTHDMATM.getReturnFare().toString() + ",";
			}
			if (!tGroupaboveSevenThousandModesOfTravel.equals("")) {
				tGroupDetailRow.put("tGroupaboveSevenThousandModesOfTravel", tGroupaboveSevenThousandModesOfTravel
						.substring(0, tGroupaboveSevenThousandModesOfTravel.length() - 1));
				tGroupDetailRow.put("tGroupaboveSevenThousandMaxOneWayFare", tGroupaboveSevenThousandMaxOneWayFare
						.substring(0, tGroupaboveSevenThousandMaxOneWayFare.length() - 1));
				tGroupDetailRow.put("tGroupaboveSevenThousandMaxReturnFare", tGroupaboveSevenThousandMaxReturnFare
						.substring(0, tGroupaboveSevenThousandMaxReturnFare.length() - 1));
			} else {
				tGroupDetailRow.put("tGroupaboveSevenThousandModesOfTravel", tGroupaboveSevenThousandModesOfTravel);
				tGroupDetailRow.put("tGroupaboveSevenThousandMaxOneWayFare", tGroupaboveSevenThousandMaxOneWayFare);
				tGroupDetailRow.put("tGroupaboveSevenThousandMaxReturnFare", tGroupaboveSevenThousandMaxReturnFare);
			}
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("presentStatus", 1);
			TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses = genericDAO
					.getByCriteria(TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses.class, criterias,
							entityManager);
			tGroupDetailRow.put("tGroupCtryCapital",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getCountryCapital());
			tGroupDetailRow.put("tGroupStateCapital",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getStateCapital());
			tGroupDetailRow.put("tGrouptravelMaxMetroCity",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getMetroCity());
			tGroupDetailRow.put("tGrouptravelMaxOtherCity",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getOtherCities());
			tGroupDetailRow.put("tGrouptravelMaxTown", oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getTown());
			tGroupDetailRow.put("tGrouptravelMaxCountry",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getCounty());
			tGroupDetailRow.put("tGrouptravelMaxMunicipality",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getMunicipality());
			tGroupDetailRow.put("tGrouptravelMaxVillage",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getVillage());
			tGroupDetailRow.put("tGrouptravelMaxRemoteLoc",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getRemoteLocation());
			tGroupDetailRow.put("tGrouptravelMaxAwayCityTown",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getTwentyMilesAwayFromClosestCityTown());
			tGroupDetailRow.put("tGrouptravelMaxHillStation",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getHillStation());
			tGroupDetailRow.put("tGrouptravelMaxresort",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getResort());
			tGroupDetailRow.put("tGrouptravelMaxWarZone",
					oldtGroupMaxDailyLimitOtherOfficialPurposeExpenses.getConflictWarZonePlace());
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("presentStatus", 1);
			TravelGroupFixedDailyPerDIAM oldtGroupFixedDailyPerDIAM = genericDAO
					.getByCriteria(TravelGroupFixedDailyPerDIAM.class, criterias, entityManager);
			tGroupDetailRow.put("tGroupCtryCapitalDIAM", oldtGroupFixedDailyPerDIAM.getCountryCapital());
			tGroupDetailRow.put("tGroupStateCapitalDIAM", oldtGroupFixedDailyPerDIAM.getStateCapital());
			tGroupDetailRow.put("tGrouptravelMetroCityDIAM", oldtGroupFixedDailyPerDIAM.getMetroCity());
			tGroupDetailRow.put("tGrouptravelOtherCityDIAM", oldtGroupFixedDailyPerDIAM.getOtherCities());
			tGroupDetailRow.put("tGrouptravelTownDIAM", oldtGroupFixedDailyPerDIAM.getTown());
			tGroupDetailRow.put("tGrouptravelCountryDIAM", oldtGroupFixedDailyPerDIAM.getCounty());
			tGroupDetailRow.put("tGrouptravelMunicipalityDIAM", oldtGroupFixedDailyPerDIAM.getMunicipality());
			tGroupDetailRow.put("tGrouptravelVillageDIAM", oldtGroupFixedDailyPerDIAM.getVillage());
			tGroupDetailRow.put("tGrouptravelRemoteLocDIAM", oldtGroupFixedDailyPerDIAM.getRemoteLocation());
			tGroupDetailRow.put("tGrouptravelAwayCityTownDIAM",
					oldtGroupFixedDailyPerDIAM.getTwentyMilesAwayFromClosestCityTown());
			tGroupDetailRow.put("tGrouptravelHillStationDIAM", oldtGroupFixedDailyPerDIAM.getHillStation());
			tGroupDetailRow.put("tGrouptravelresortDIAM", oldtGroupFixedDailyPerDIAM.getResort());
			tGroupDetailRow.put("tGrouptravelWarZoneDIAM", oldtGroupFixedDailyPerDIAM.getConflictWarZonePlace());
			String travelGroupKlRemarks = "";
			String travelGroupKlMandatory = "";
			criterias.clear();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("travelgroup.id", tGroup.getId());
			criterias.put("presentStatus", 1);
			List<TravelGroupKnowledgeLibrary> existingTravelGroupKl = genericDAO
					.findByCriteria(TravelGroupKnowledgeLibrary.class, criterias, entityManager);
			for (TravelGroupKnowledgeLibrary tGroupKL : existingTravelGroupKl) {
				travelGroupKlRemarks += tGroupKL.getKlContent() + ",";
				travelGroupKlMandatory += tGroupKL.getKlMandatory() + ",";
			}
			if (!travelGroupKlRemarks.equals("")) {
				tGroupDetailRow.put("travelGroupKlRemarks",
						travelGroupKlRemarks.substring(0, travelGroupKlRemarks.length() - 1));
				tGroupDetailRow.put("travelGroupKlMandatory",
						travelGroupKlMandatory.substring(0, travelGroupKlMandatory.length() - 1));
			}
			tGroupDetailAn.add(tGroupDetailRow);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@Transactional
	public Result showExpenseGroup(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode results = Json.newObject();
		results.put("result", false);
		Http.Session session = request.session();
		// EntityManager entityManager=getEntityManager();
		Users users = null;
		try {
			JsonNode json = request.body().asJson();
			String useremail = json.findValue("userEmail").asText();
			String eGroupEntityId = json.findValue("eGroupEntityId").asText();
			ExpenseGroup eGroup = ExpenseGroup.findById(Long.parseLong(eGroupEntityId));
			if (eGroup != null) {
				results.put("result", true);
			}
			session.adding("email", useremail);
			users = getUserInfo(request);
			ArrayNode eGroupDetailAn = results.putArray("expenseGroupEntityDetails");
			ObjectNode eGroupDetailRow = Json.newObject();
			eGroupDetailRow.put("eGroupId", eGroup.getId());
			eGroupDetailRow.put("eGroupGroupName", eGroup.getExpenseGroupName());
			String eGroupexpenseItems = "";
			String eGroupexpenseItemMaximumPermittedAdvance = "";
			String eGroupexpenseItemMonthlyMonetoryLimitForReimbursement = "";
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("organization.id", users.getOrganization().getId());
			criterias.put("expenseGroup.id", eGroup.getId());
			criterias.put("presentStatus", 1);
			List<ExpenseGroupExpenseItemMonetoryClaim> oldeGroupExpenseItemsMonetoryClaim = genericDAO
					.findByCriteria(ExpenseGroupExpenseItemMonetoryClaim.class, criterias, entityManager);
			for (ExpenseGroupExpenseItemMonetoryClaim eGEIMC : oldeGroupExpenseItemsMonetoryClaim) {
				eGroupexpenseItems += eGEIMC.getSpecificsItem().getId() + ",";
				eGroupexpenseItemMaximumPermittedAdvance += eGEIMC.getMaximumPermittedAdvance().toString() + ",";
				eGroupexpenseItemMonthlyMonetoryLimitForReimbursement += eGEIMC
						.getMonthlyMonetoryLimitForReimbursement().toString() + ",";
			}
			if (!eGroupexpenseItems.equals("")) {
				eGroupDetailRow.put("eGroupexpenseItems",
						eGroupexpenseItems.substring(0, eGroupexpenseItems.length() - 1));
				eGroupDetailRow.put("eGroupexpenseItemMaximumPermittedAdvance", eGroupexpenseItemMaximumPermittedAdvance
						.substring(0, eGroupexpenseItemMaximumPermittedAdvance.length() - 1));
				eGroupDetailRow.put("eGroupexpenseItemMonthlyMonetoryLimitForReimbursement",
						eGroupexpenseItemMonthlyMonetoryLimitForReimbursement.substring(0,
								eGroupexpenseItemMonthlyMonetoryLimitForReimbursement.length() - 1));
			} else {
				eGroupDetailRow.put("eGroupexpenseItems", eGroupexpenseItems);
				eGroupDetailRow.put("eGroupexpenseItemMaximumPermittedAdvance",
						eGroupexpenseItemMaximumPermittedAdvance);
				eGroupDetailRow.put("eGroupexpenseItemMonthlyMonetoryLimitForReimbursement",
						eGroupexpenseItemMonthlyMonetoryLimitForReimbursement);
			}
			eGroupDetailAn.add(eGroupDetailRow);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, users.getEmail(), users.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
			List<String> errorList = getStackTrace(ex);
			return Results.ok(errorPage.render(ex, errorList));
		}
		log.log(Level.FINE, ">>>> End " + results);
		return Results.ok(results).withHeader("ContentType", "application/json");
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Result getUserClaimBranchProject(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		ObjectNode row = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			String type = json.findValue("type").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user) {
					ArrayNode an = null;
					if ("travel".equalsIgnoreCase(type)) {
						if (null != user.getBranch() && null != user.getBranch().getId()) {
							an = result.putArray("branches");
							row = Json.newObject();
							row.put("branchId", user.getBranch().getId());
							if (null != user.getBranch().getName()) {
								row.put("branchName", user.getBranch().getName());
							}
							an.add(row);
						}
					} else if ("expense".equalsIgnoreCase(type)) {
						if (null != user.getBranch() && null != user.getBranch().getId()) {
							an = result.putArray("branches");
							row = Json.newObject();
							row.put("branchId", user.getBranch().getId());
							if (null != user.getBranch().getName()) {
								row.put("branchName", user.getBranch().getName());
							}
							an.add(row);
						}
					}
					if ("travel".equalsIgnoreCase(type)) {
						StringBuilder sbr = new StringBuilder(
								"Select obj from ProjectBranches obj where obj.projectBranch.id=?1 and obj.branchOrganization.id=?2 and obj.presentStatus=1");
						ArrayList inparams = new ArrayList(2);
						inparams.add(user.getBranch().getId());
						inparams.add(user.getOrganization().getId());
						List<ProjectBranches> bnchPjcts = genericDAO.queryWithParams(sbr.toString(), entityManager,
								inparams);
						an = result.putArray("projects");
						for (ProjectBranches pjctBnch : bnchPjcts) {
							if (null != pjctBnch && null != pjctBnch.getProject()) {
								row = Json.newObject();
								if (null != pjctBnch.getProject().getId()) {
									row.put("projectId", pjctBnch.getProject().getId());
									if (null != pjctBnch.getProject().getName()) {
										row.put("projectName", pjctBnch.getProject().getName());
									}
								} else {
									result.put("projectId", "");
									result.put("projectName", "");
								}
								an.add(row);
							}
						}
					} else if ("expense".equalsIgnoreCase(type)) {
						StringBuilder sbr = new StringBuilder(
								"Select obj from ProjectBranches obj where obj.projectBranch.id=?1 and obj.branchOrganization.id=?2 and obj.presentStatus=1");
						ArrayList inparams = new ArrayList(2);
						inparams.add(user.getBranch().getId());
						inparams.add(user.getOrganization().getId());
						List<ProjectBranches> bnchPjcts = genericDAO.queryWithParams(sbr.toString(), entityManager,
								inparams);
						an = result.putArray("projects");
						for (ProjectBranches pjctBnch : bnchPjcts) {
							if (null != pjctBnch && null != pjctBnch.getProject()) {
								row = Json.newObject();
								if (null != pjctBnch.getProject().getId()) {
									row.put("projectId", pjctBnch.getProject().getId());
									if (null != pjctBnch.getProject().getName()) {
										row.put("projectName", pjctBnch.getProject().getName());
									}
								} else {
									result.put("projectId", "");
									result.put("projectName", "");
								}
								an.add(row);
							}
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
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getUserRelatedClaimsAvailable(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		ArrayNode an = result.putArray("groups");
		Http.Session session = request.session();
		ObjectNode row = null;
		Users user = null;
		TransactionPurpose transactionPurpose = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user) {
					String travelClaim = user.getUsersTravelClaimTxnQuestions();
					if (null != travelClaim && !"".equals(travelClaim)) {
						String[] travelClaimQuestions = travelClaim.split(",");
						if (travelClaimQuestions.length > 0) {
							for (int i = 0; i < travelClaimQuestions.length; i++) {
								row = Json.newObject();
								transactionPurpose = TransactionPurpose
										.findById(Long.parseLong(travelClaimQuestions[i]));
								if (null != transactionPurpose) {
									row.put("id", transactionPurpose.getId());
									row.put("transactionPurpose", transactionPurpose.getTransactionPurpose());
									an.add(row);
								}
							}
						}
					}
					String expenseClaim = user.getUsersExpenseClaimTxnQuestions();
					if (null != expenseClaim && !"".equals(expenseClaim)) {
						String[] expenseClaimQuestions = expenseClaim.split(",");
						if (expenseClaimQuestions.length > 0) {
							for (int i = 0; i < expenseClaimQuestions.length; i++) {
								row = Json.newObject();
								transactionPurpose = TransactionPurpose
										.findById(Long.parseLong(expenseClaimQuestions[i]));
								if (null != transactionPurpose) {
									row.put("id", transactionPurpose.getId());
									row.put("transactionPurpose", transactionPurpose.getTransactionPurpose());
									an.add(row);
								}
							}
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
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getSearchCriteriaData(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		ArrayNode an = null;
		ObjectNode row = null;
		StringBuilder query = new StringBuilder();
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				if (null != user) {
					if (null != user.getOrganization()) {
						List<Branch> branches = user.getOrganization().getBranches();
						if (!branches.isEmpty() && branches.size() > 0) {
							an = result.putArray("branches");
							for (Branch branch : branches) {
								if (null != branch) {
									row = Json.newObject();
									if (null != branch.getId()) {
										row.put("id", branch.getId());
									}
									if (null != branch.getName()) {
										row.put("name", branch.getName());
									}
									an.add(row);
								}
							}
							List<Project> projects = user.getOrganization().getProjects();
							if (!projects.isEmpty() && projects.size() > 0) {
								an = result.putArray("projects");
								for (Project project : projects) {
									if (null != project) {
										row = Json.newObject();
										if (null != project.getId()) {
											row.put("id", project.getId());
										}
										if (null != project.getName()) {
											row.put("name", project.getName());
										}
										an.add(row);
									}
								}
							}
							if (null != user.getOrganization().getId()) {
								query.append(
										"SELECT obj FROM Particulars obj WHERE obj.organization.id = ?1 AND obj.name = 'Expenses'");
								ArrayList inParams = new ArrayList(1);
								inParams.add(user.getOrganization().getId());
								List<Particulars> particulars = genericDAO.queryWithParams(query.toString(),
										entityManager, inParams);
								if (particulars.size() == 1) {
									Particulars particular = particulars.get(0);
									if (null != particular) {
										an = result.putArray("particulars");
										row = Json.newObject();
										if (null != particular.getId()) {
											row.put("id", particular.getId());
										}
										if (null != particular.getName()) {
											row.put("name", particular.getName());
										}
										an.add(row);
										if (null != particular.getId()) {
											query = new StringBuilder(
													"SELECT obj FROM Specifics obj WHERE obj.particularsId.id = ?1 AND obj.organization.id = ?2 AND obj.employeeClaimItem = 1");
											inParams.clear();
											inParams.add(particular.getId());
											inParams.add(user.getOrganization().getId());
											List<Specifics> specifics = genericDAO.queryWithParams(query.toString(),
													entityManager, inParams);
											if (!specifics.isEmpty() && specifics.size() > 0) {
												an = result.putArray("specifics");
												for (Specifics specific : specifics) {
													if (null != specific) {
														row = Json.newObject();
														if (null != specific.getId()) {
															row.put("id", specific.getId());
														}
														if (null != specific.getName()) {
															row.put("name", specific.getName());
														}
														an.add(row);
													}
												}
											}
										}
									}
								}
							}
						}
					}
					List<TravelMode> travelModes = genericDAO.findAll(TravelMode.class, false, false, entityManager);
					if (!travelModes.isEmpty() && travelModes.size() > 0) {
						an = result.putArray("travelModes");
						for (TravelMode travelMode : travelModes) {
							if (null != travelMode) {
								row = Json.newObject();
								if (null != travelMode.getId()) {
									row.put("id", travelMode.getId());
								}
								if (null != travelMode.getTravelModeName()) {
									row.put("name", travelMode.getTravelModeName());
								}
								an.add(row);
							}
						}
					}
					List<AccomodationType> accomodationTypes = genericDAO.findAll(AccomodationType.class, false, false,
							entityManager);
					if (!accomodationTypes.isEmpty() && accomodationTypes.size() > 0) {
						an = result.putArray("accomodationTypes");
						for (AccomodationType accomodationType : accomodationTypes) {
							if (null != accomodationType) {
								row = Json.newObject();
								if (null != accomodationType.getId()) {
									row.put("id", accomodationType.getId());
								}
								if (null != accomodationType.getAccomodationTypeName()) {
									row.put("name", accomodationType.getAccomodationTypeName());
								}
								an.add(row);
							}
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
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result locationOnTravelType(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = claimsService.locationOnTravelType(result, json, user);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result displayTravelEligibility(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = claimsService.displayTravelEligibility(result, json, user);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result submitForApproval(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result.put("useremail", user.getEmail());
				String role = "";
				List<UsersRoles> userRoles = UsersRoles.getUserRoleList(entityManager, user.getOrganization().getId(),
						user.getId(),
						user.getBranch().getId());
				for (UsersRoles roles : userRoles) {
					role += roles.getRole().getName() + ",";
				}
				role = role.substring(0, role.length() - 1);
				result.put("role", role);
				String claimTxnPurposeText = json.findValue("claimTxnPurposeText") != null
						? json.findValue("claimTxnPurposeText").asText()
						: null;
				if (claimTxnPurposeText.equals("Request For Travel Advance")) {
					result = claimsService.submitForApproval(result, json, user, entityManager, entitytransaction);
				} else if (claimTxnPurposeText.equals("Settle Travel Advance")) {
					claimsSettlementService.submitForApproval(result, json, user, entityManager, entitytransaction);
				}
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			if (ex instanceof IDOSException) {
				result.put("errorMessage", ((IDOSException) ex).getErrorDescription());
			}
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result approverAction(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = claimsService.approverAction(result, json, user, entityManager, entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End ");
		return Results.ok(result);
	}

	@Transactional
	public Result userAdvancesTxnApprovedButNotAccountedCount(Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Users user = null;
		String email = "Error"; // if email is not got set then set the error
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (null != user) {
				email = user.getEmail();
				result = claimsService.userAdvancesTxnApprovedButNotAccountedCount(result, json, user, entityManager,
						entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, email, ex);
			String strBuff = getStackTraceMessage(ex);
			if (user != null) {
				expService.sendExceptionReport(strBuff, email, user.getOrganization().getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName());
			} else {
				expService.sendExceptionReport(strBuff, email, "",
						Thread.currentThread().getStackTrace()[1].getMethodName());
			}
		} finally {
			// EntityManagerProvider.close();
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result userClaimsTransactions(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		try {
			entitytransaction.begin();
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = claimsService.userClaimsTransactions(result, json, user, entityManager, entitytransaction);
			}
			entitytransaction.commit();
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result exitingClaimsAdvanceTxnRefAndAmount(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = claimsService.exitingClaimsAdvanceTxnRefAndAmount(result, json, user, entityManager,
						entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result populateUserUnsettledTravelClaimAdvances(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = claimsSettlementService.populateUserUnsettledTravelClaimAdvances(result, json, user,
						entityManager, entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result displayUnsettledAdvancesDetails(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = claimsSettlementService.displayUnsettledAdvancesDetails(result, json, user, entityManager,
						entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result claimSettlementAccountantAction(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				result = claimsSettlementService.claimSettlementAccountantAction(result, json, user, entityManager,
						entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result bankAccountsForPayment(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		Branch txnBnch = null;
		ClaimTransaction claimTxn = null;
		result.put("result", false);
		ArrayNode an = result.putArray("availableBranchBankData");
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				String claimTxnEntityId = json.findValue("txnEntityId") != null ? json.findValue("txnEntityId").asText()
						: null;
				if (claimTxnEntityId != null && !claimTxnEntityId.equals("")) {
					claimTxn = ClaimTransaction.findById(Long.parseLong(claimTxnEntityId));
					result.put("entityId", claimTxn.getId());
					txnBnch = claimTxn.getTransactionBranch();
				}
				if (txnBnch != null) {
					List<BranchBankAccounts> bnchBankAccounts = txnBnch.getBranchBankAccounts();
					if (!bnchBankAccounts.isEmpty() && bnchBankAccounts.size() > 0) {
						result.put("result", true);
						for (BranchBankAccounts bnchBnkAct : bnchBankAccounts) {
							ObjectNode objNode = Json.newObject();
							objNode.put("bnchBankAccountsId", bnchBnkAct.getId());
							objNode.put("bnchBankAccountsName", bnchBnkAct.getBankName());
							an.add(objNode);
						}
					}
				}
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result search(Request request) {

		// EntityManager entityManager = getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = null;
		Users user = null;
		try {
			// entitytransaction.begin();
			JsonNode json = request.body().asJson();
			log.log(Level.FINE, ">>>> Start " + json);
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				user = getUserInfo(request);
				Long txnType = json.findValue("txnType").asLong();
				String txnRefNumber = json.findValue("txnRefNumber").asText();
				Long item = json.findValue("item").asLong();
				String status = json.findValue("status").asText();
				Integer claimType = json.findValue("claimType").asInt();
				Integer payMode = json.findValue("payMode").asInt();
				String travelMode = json.findValue("travelMode").asText();
				String accomodationMode = json.findValue("accomodationMode").asText();
				String fromDate = json.findValue("fromDate").asText();
				String toDate = json.findValue("toDate").asText();
				Long branch = json.findValue("branch").asLong();
				Long project = json.findValue("project").asLong();
				Double fromAmount = json.findValue("fromAmount").asDouble();
				Double toAmount = json.findValue("toAmount").asDouble();

				/*
				 * Sunil Namdev
				 * Integer documents = (null == json.findValue("documents").asText() ||
				 * "".equals(json.findValue("documents").asText())) ? 1 :
				 * json.findValue("documents").asInt();
				 * Integer remarks = (null == json.findValue("remarks").asText() ||
				 * "".equals(json.findValue("remarks").asText())) ? 1 :
				 * json.findValue("remarks").asInt();
				 */
				Integer remarks = (null == json.findValue("remarks").asText()
						|| "".equals(json.findValue("remarks").asText())) ? 0 : json.findValue("remarks").asInt();
				Integer documents = (null == json.findValue("documents").asText()
						|| "".equals(json.findValue("documents").asText())) ? 0 : json.findValue("documents").asInt();

				Long claimSearchTxnQuestion = json.findValue("claimSearchTxnQuestion").asLong();
				Integer claimSearchUserType = json.findValue("claimSearchUserType").asInt();
				result = claimsService.search(user, txnRefNumber, txnType, item, status, claimType, payMode, travelMode,
						accomodationMode, fromDate, toDate, branch, project, fromAmount, toAmount, remarks, documents,
						claimSearchTxnQuestion, claimSearchUserType, entityManager);
			}
			// entitytransaction.commit();
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }
			// log.log(Level.SEVERE, "Error", ex); Sunil
			log.log(Level.SEVERE, "Failed", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getCountries(Request request, final Integer type, final String name) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager = getEntityManager();
		// EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = null;
		Users user = null;
		try {
			// entitytransaction.begin();
			user = getUserInfo(request);
			result = claimsService.getCountries(user, type, name, entityManager);
			// entitytransaction.commit();
		} catch (Exception ex) {
			// if (entitytransaction.isActive()) {
			// entitytransaction.rollback();
			// }
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getClaimGstData(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		ArrayNode cgstRateList = result.putArray("cgstRateList");
		ArrayNode sgstRateList = result.putArray("sgstRateList");
		ArrayNode igstRateList = result.putArray("igstRateList");
		ArrayNode cessRateList = result.putArray("cessRateList");
		ArrayNode itemDataList = result.putArray("itemDataList");
		Users user = null;
		try {
			user = getUserInfo(request);
			if (null == user) {
				return unauthorized();
			}
			JsonNode json = request.body().asJson();
			String email = (json.findValue("email") == null || "".equals(json.findValue("email"))) ? null
					: json.findValue("email").asText();
			Integer mappingId = (json.findValue("mappingId") == null || "".equals(json.findValue("mappingId"))) ? null
					: json.findValue("mappingId").asInt();
			Long branchId = (json.findValue("branchId") == null || "".equals(json.findValue("branchId"))) ? null
					: json.findValue("branchId").asLong();
			if (branchId == null) {
				branchId = user.getBranch().getId();
			}
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				SPECIFICS_SERVICE.getTaxCOAChildsForBranch(39, entityManager, user, "1", branchId, sgstRateList);
				SPECIFICS_SERVICE.getTaxCOAChildsForBranch(40, entityManager, user, "1", branchId, cgstRateList);
				SPECIFICS_SERVICE.getTaxCOAChildsForBranch(41, entityManager, user, "1", branchId, igstRateList);
				SPECIFICS_SERVICE.getTaxCOAChildsForBranch(42, entityManager, user, "1", branchId, cessRateList);
				if (mappingId != null) {
					Specifics specificsForMapping = coaService.getSpecificsForMapping(user, mappingId.toString(),
							entityManager);
					if (specificsForMapping != null) {
						List<Specifics> childNodes = coaService.getCoaChildNodesByMapping(specificsForMapping.getId(),
								user, entityManager);
						for (Specifics specf : childNodes) {
							ObjectNode row = Json.newObject();
							row.put("id", specf.getId());
							row.put("name", specf.getName());
							row.put("specfaccountCode", specf.getAccountCode().toString());
							row.put("topLevelAccountCode", specf.getParticularsId().getAccountCode().toString());
							row.put("identificationForDataValid", specf.getIdentificationForDataValid());
							row.put("iscoachild", 0);
							itemDataList.add(row);
						}
					} else {
						throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
								"COA mapping is not found for mapping " + mappingId,
								"Claim mapping not found for " + mappingId);
					}

				}
			}
		} catch (Exception ex) {
			reportException(entityManager, null, user, ex, result);
		} catch (Throwable th) {
			reportThrowable(entityManager, null, user, th, result);
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getPendingEmployeeClaims(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		ArrayNode pendingClaims = result.putArray("pendingClaims");
		result.put("result", false);
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (null == user) {
				return unauthorized();
			}

			List<ClaimTransaction> pendingClaimList = ClaimTransaction.findEmployeePendingClaims(entityManager,
					user.getOrganization().getId());
			if (pendingClaimList != null && pendingClaimList.size() > 0) {
				for (ClaimTransaction claimTransaction : pendingClaimList) {
					ObjectNode row = Json.newObject();
					row.put("id", claimTransaction.getId());
					row.put("date", IdosConstants.IDOSDF.format(claimTransaction.getCreatedAt()));
					row.put("userName", claimTransaction.getCreatedBy().getFullName());
					row.put("branch", claimTransaction.getTransactionBranch().getName());
					row.put("purpose", claimTransaction.getTransactionPurpose().getTransactionPurpose());
					if (claimTransaction.getTransactionPurpose().getId() == 16
							|| claimTransaction.getTransactionPurpose().getId() == 18) {
						if (claimTransaction.getClaimsDueSettlement() > 0) {
							row.put("approvedAmount", claimTransaction.getClaimsDueSettlement());
						} else {
							row.put("approvedAmount", claimTransaction.getClaimsRequiredSettlement());
						}

					} else {
						row.put("approvedAmount", claimTransaction.getClaimsDueSettlement());
					}
					row.put("status", "Not Paid");
					pendingClaims.add(row);
				}
				result.put("result", true);
			}

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End ");
		return Results.ok(result);
	}

	@Transactional
	public Result settleEmpPendingClaim(Http.Request request) {
		log.log(Level.FINE, ">>>> Start");
		// EntityManager entityManager=getEntityManager();
		EntityTransaction entitytransaction = entityManager.getTransaction();
		ObjectNode result = Json.newObject();
		Http.Session session = request.session();
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			String email = json.findValue("email").asText();
			if (null != email && !"".equals(email)) {
				session.adding("email", email);
				user = getUserInfo(request);
				// Trial Balance impact for Cash and Bank Book
				CLAIM_ITEM_DETAILS_SERVICE.paymentForClaims(result, json, user, entityManager, entitytransaction);
			}
		} catch (Exception ex) {
			if (entitytransaction.isActive()) {
				entitytransaction.rollback();
			}
			log.log(Level.SEVERE, "Error", ex);

			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		log.log(Level.FINE, ">>>> End " + result);
		return Results.ok(result);
	}

	@Transactional
	public Result getPaidEmployeeClaims(Request request) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> Start");
		ObjectNode result = Json.newObject();
		ArrayNode paidClaims = result.putArray("paidClaims");
		result.put("result", false);
		Users user = null;
		try {
			JsonNode json = request.body().asJson();
			user = getUserInfo(request);
			if (null == user) {
				return unauthorized();
			}
			List<PaidClaimsDetails> OrgPaidClaims = PaidClaimsDetails.findOrgPaidClaims(entityManager,
					user.getOrganization().getId());
			if (OrgPaidClaims != null && OrgPaidClaims.size() > 0) {
				for (PaidClaimsDetails paidClaimsDetails : OrgPaidClaims) {
					ClaimTransaction claimTransaction = paidClaimsDetails.getTransaction();
					ObjectNode row = Json.newObject();
					row.put("id", claimTransaction.getId());
					row.put("date", IdosConstants.IDOSDF.format(claimTransaction.getCreatedAt()));
					row.put("userName", claimTransaction.getCreatedBy().getFullName());
					row.put("branch", claimTransaction.getTransactionBranch().getName());
					row.put("purpose", claimTransaction.getTransactionPurpose().getTransactionPurpose());
					row.put("approvedAmount", paidClaimsDetails.getPaidAmt());
					row.put("status", "PAID");
					row.put("payMode", paidClaimsDetails.getPayMode());
					row.put("payDate", IdosConstants.idosdf.format(paidClaimsDetails.getPaymentDate()));
					paidClaims.add(row);
				}
				result.put("result", true);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			String strBuff = getStackTraceMessage(ex);
			expService.sendExceptionReport(strBuff, user.getEmail(), user.getOrganization().getName(),
					Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, ">>>> End ");
		return Results.ok(result);
	}
}
