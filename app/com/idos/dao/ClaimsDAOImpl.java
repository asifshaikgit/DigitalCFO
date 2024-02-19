package com.idos.dao;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.UploadUtil;

import model.BillOfMaterialTxnModel;
import model.Branch;
import model.BranchBankAccountBalance;
import model.BranchBankAccounts;
import model.BranchCashCount;
import model.ClaimTransaction;
import model.DistanceMilesKm;
import model.IdosCountryStatesCity;
import model.IdosSubscriptionCountry;
import model.IdosSubscriptionCountryStates;
import model.Organization;
import model.Project;
import model.Transaction;
import model.TransactionPurpose;
import model.TravelGroupDistanceMilesKmsAllowedTravelMode;
import model.TravelGroupFixedDailyPerDIAM;
import model.TravelGroupKnowledgeLibrary;
import model.TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses;
import model.TravelGroupPermittedBoardingLodging;
import model.Travel_Group;
import model.TrialBalanceBranchBank;
import model.TrialBalanceBranchCash;
import model.TrialBalanceUserAdvance;
import model.TrialBalanceUserClaims;
import model.UserRightInBranch;
import model.Users;
import model.UsersRoles;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;
import actor.CreatorActor;
import controllers.Karvy.KarvyAuthorization;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.idos.util.CodeHelper;
import service.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;

public class ClaimsDAOImpl implements ClaimsDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	private static final ClaimsSettlementService claimsSettlementService = new ClaimsSettlementServiceImpl();

	@Override
	public ObjectNode locationOnTravelType(ObjectNode result, JsonNode json, Users user) {
		log.log(Level.FINE, "************* Start");
		result.put("result", false);
		ArrayNode fromToOptionAn = result.putArray("fromToOptionData");
		String claimTravelType = json.findValue("claimTravelType").asText();
		IdosSubscriptionCountry usDefaultCountry = new IdosSubscriptionCountry()
				.findById(91L);
		StringBuilder fromToOptionStr = new StringBuilder();
		if (claimTravelType != null) {
			if (claimTravelType.equals("1")) {
				// domestic
				fromToOptionStr.delete(0, fromToOptionStr.length());
				if (user.getOrganization().getCountry() != null) {
					if (user.getBranch().getCountry() != null) {
						IdosSubscriptionCountry idosCountry = new IdosSubscriptionCountry()
								.findById(user.getBranch().getCountry()
										.longValue());
						List<IdosSubscriptionCountryStates> domesticCountryStates = idosCountry
								.getIdosSubscriptionCountryStates();
						for (IdosSubscriptionCountryStates idosSubsCtryStates : domesticCountryStates) {
							fromToOptionStr.append("<option value=")
									.append(idosSubsCtryStates.getStateName())
									.append(">")
									.append(idosSubsCtryStates.getStateName())
									.append("</option>");
						}
						List<IdosCountryStatesCity> domesticCountryCity = idosCountry
								.getIdosSubscriptionCountryCities();
						for (IdosCountryStatesCity idosCtryStatesCities : domesticCountryCity) {
							fromToOptionStr.append("<option value=")
									.append(idosCtryStatesCities.getCityName())
									.append(">")
									.append(idosCtryStatesCities.getCityName())
									.append("</option>");
						}
					} else {
						IdosSubscriptionCountry idosCountry = new IdosSubscriptionCountry()
								.findById(user.getOrganization().getCountry()
										.longValue());
						List<IdosSubscriptionCountryStates> domesticCountryStates = idosCountry
								.getIdosSubscriptionCountryStates();
						for (IdosSubscriptionCountryStates idosSubsCtryStates : domesticCountryStates) {
							fromToOptionStr
									.append("<option value=")
									.append(idosSubsCtryStates.getStateName())
									.append(">"
											+ idosSubsCtryStates.getStateName())
									.append("</option>");
						}
						List<IdosCountryStatesCity> domesticCountryCity = idosCountry
								.getIdosSubscriptionCountryCities();
						for (IdosCountryStatesCity idosCtryStatesCities : domesticCountryCity) {
							fromToOptionStr
									.append("<option value=")
									.append(idosCtryStatesCities.getCityName())
									.append(">"
											+ idosCtryStatesCities
													.getCityName())
									.append("</option>");
						}
					}
				} else {
					if (user.getBranch().getCountry() != null) {
						IdosSubscriptionCountry idosCountry = new IdosSubscriptionCountry()
								.findById(user.getBranch().getCountry()
										.longValue());
						List<IdosSubscriptionCountryStates> domesticCountryStates = idosCountry
								.getIdosSubscriptionCountryStates();
						for (IdosSubscriptionCountryStates idosSubsCtryStates : domesticCountryStates) {
							fromToOptionStr.append("<option value=")
									.append(idosSubsCtryStates.getStateName())
									.append(">")
									.append(idosSubsCtryStates.getStateName())
									.append("</option>");
						}
						List<IdosCountryStatesCity> domesticCountryCity = idosCountry
								.getIdosSubscriptionCountryCities();
						for (IdosCountryStatesCity idosCtryStatesCities : domesticCountryCity) {
							fromToOptionStr.append("<option value=")
									.append(idosCtryStatesCities.getCityName())
									.append(">")
									.append(idosCtryStatesCities.getCityName())
									.append("</option>");
						}
					} else {
						List<IdosSubscriptionCountryStates> domesticCountryStates = usDefaultCountry
								.getIdosSubscriptionCountryStates();
						for (IdosSubscriptionCountryStates idosSubsCtryStates : domesticCountryStates) {
							fromToOptionStr.append("<option value=")
									.append(idosSubsCtryStates.getStateName())
									.append(">")
									.append(idosSubsCtryStates.getStateName())
									.append("</option>");
						}
						List<IdosCountryStatesCity> domesticCountryCity = usDefaultCountry
								.getIdosSubscriptionCountryCities();
						for (IdosCountryStatesCity idosCtryStatesCities : domesticCountryCity) {
							fromToOptionStr.append("<option value=")
									.append(idosCtryStatesCities.getCityName())
									.append(">")
									.append(idosCtryStatesCities.getCityName())
									.append("</option>");
						}
					}
				}
			}

			if (claimTravelType.equals("2")) {
				// international
				fromToOptionStr.delete(0, fromToOptionStr.length());
				List<IdosSubscriptionCountry> idosSubscriptionCountry = genericDao
						.findAll(IdosSubscriptionCountry.class, true, false,
								entityManager);
				for (IdosSubscriptionCountry idosSubscriptionCountryEntity : idosSubscriptionCountry) {
					fromToOptionStr
							.append("<option value=")
							.append(idosSubscriptionCountryEntity
									.getCountryName())
							.append(">")
							.append(idosSubscriptionCountryEntity
									.getCountryName())
							.append("</option>");
				}
				StringBuilder sbr = new StringBuilder("");
				sbr.append("select obj from IdosSubscriptionCountryStates obj where obj.presentStatus=1");
				List<IdosSubscriptionCountryStates> countryStates = genericDao
						.findAll(IdosSubscriptionCountryStates.class, true, false, entityManager);
				for (IdosSubscriptionCountryStates idosSubsCtryStates : countryStates) {
					fromToOptionStr.append("<option value=")
							.append(idosSubsCtryStates.getStateName())
							.append(">")
							.append(idosSubsCtryStates.getStateName())
							.append("</option>");
				}
				List<IdosCountryStatesCity> countryStateCity = genericDao
						.findAll(IdosCountryStatesCity.class, true, false,
								entityManager);
				for (IdosCountryStatesCity idosCtryStatesCities : countryStateCity) {
					fromToOptionStr.append("<option value=")
							.append(idosCtryStatesCities.getCityName())
							.append(">")
							.append(idosCtryStatesCities.getCityName())
							.append("</option>");
				}
			}
			// try{
			// MemcachedClient memcacheClient = new MemcachedClient(new
			// InetSocketAddress("localhost", 11211));
			// memcacheClient.delete("interNationalOptions");
			// if(memcacheClient.get("interNationalOptions")==null){
			// fromToOptionStr=() memcacheClient.get("interNationalOptions");
			// }
			// }catch(Exception ex){
			// log.log(Level.SEVERE, "Error", ex);
			// }
		}
		if (fromToOptionStr != null && !fromToOptionStr.equals("")) {
			result.put("result", true);
			ObjectNode efromToOptionRow = Json.newObject();
			efromToOptionRow.put("fromToOptionStr", fromToOptionStr.toString());
			efromToOptionRow.put("type", claimTravelType);
			fromToOptionAn.add(efromToOptionRow);
		}
		log.log(Level.FINE, "************* End " + result);
		return result;
	}

	@Override
	public ObjectNode displayTravelEligibility(ObjectNode result, JsonNode json, Users user) {
		log.log(Level.FINE, "************* Start");
		result.put("result", false);
		ArrayNode travelAn = result.putArray("travelData");
		ArrayNode bnLAn = result.putArray("bnLData");
		ArrayNode dOthExpensesAn = result.putArray("dOthExpensesData");
		ArrayNode fixedPerDIAMAn = result.putArray("fixedPerDIAMData");
		ArrayNode tGroupKLAn = result.putArray("tGroupKLData");
		String typeOfCity = json.findValue("typeOfCityStr") != null ? json.findValue("typeOfCityStr").asText() : null;
		String appropriateDistance = json.findValue("appropriateDistanceStr") != null
				? json.findValue("appropriateDistanceStr").asText()
				: null;
		Travel_Group tGroup = null;
		StringBuilder sbr = new StringBuilder("");
		if (user.gettGroup() != null) {
			tGroup = user.gettGroup();
		}
		if (tGroup != null) {
			result.put("result", true);
			String tGroupDnMKmsAlwdTravelModesStr = "";
			Double maxOneWayFare = null;
			Double maxReturnFare = null;
			Double higherMaxOneWayFare = 0.0;
			Double higherMaxReturnFare = 0.0;
			Integer cityType = Integer.parseInt(typeOfCity);
			if (appropriateDistance != null && !appropriateDistance.equals("")) {
				DistanceMilesKm distanceMiles = DistanceMilesKm.findById(Long.parseLong(appropriateDistance));
				sbr.append("select obj from TravelGroupDistanceMilesKmsAllowedTravelMode obj where obj.organization='"
						+ user.getOrganization().getId() + "' and obj.travelgroup='" + tGroup.getId()
						+ "'and obj.presentStatus=1 and obj.distanceMilesKms='" + distanceMiles.getId() + "'");
				List<TravelGroupDistanceMilesKmsAllowedTravelMode> tGroupDnMKmsAlwdTravelModes = genericDao
						.executeSimpleQuery(sbr.toString(), entityManager);
				if (!tGroupDnMKmsAlwdTravelModes.isEmpty() && tGroupDnMKmsAlwdTravelModes.size() > 0) {
					for (TravelGroupDistanceMilesKmsAllowedTravelMode tModes : tGroupDnMKmsAlwdTravelModes) {
						tGroupDnMKmsAlwdTravelModesStr += tModes.getTravelMode().getTravelModeName() + ",";
						maxOneWayFare = tModes.getOneWayFare();
						maxReturnFare = tModes.getReturnFare();
						if (maxOneWayFare > higherMaxOneWayFare) {
							higherMaxOneWayFare = maxOneWayFare;
						}
						if (maxReturnFare > higherMaxReturnFare) {
							higherMaxReturnFare = maxReturnFare;
						}
					}
				}
			}
			ObjectNode travelRow = Json.newObject();
			if (tGroupDnMKmsAlwdTravelModesStr.equals("")) {
				travelRow.put("allowedTravelModes", tGroupDnMKmsAlwdTravelModesStr);
			} else {
				travelRow.put("allowedTravelModes",
						tGroupDnMKmsAlwdTravelModesStr.substring(0, tGroupDnMKmsAlwdTravelModesStr.length() - 1));
			}
			travelRow.put("maxOneWayFare", IdosConstants.decimalFormat.format(higherMaxOneWayFare));
			travelRow.put("maxReturnFare", IdosConstants.decimalFormat.format(higherMaxReturnFare));
			travelAn.add(travelRow);
			sbr.delete(0, sbr.length());
			sbr.append("select obj from TravelGroupPermittedBoardingLodging obj where obj.organization='"
					+ user.getOrganization().getId() + "' and obj.travelgroup='" + tGroup.getId()
					+ "'and obj.presentStatus=1 and obj.cityType='" + cityType + "'");
			List<TravelGroupPermittedBoardingLodging> tGroupPermBnL = genericDao.executeSimpleQuery(sbr.toString(),
					entityManager);
			String tGroupPermAccomodationTypeStr = "";
			Double maxPermittedRoomCostPerNight = null;
			Double maxPermittedFoodCostPerDay = null;
			Double higherMaxPermittedRoomCostPerNight = 0.0;
			Double higherMaxPermittedFoodCostPerDay = 0.0;
			if (!tGroupPermBnL.isEmpty() && tGroupPermBnL.size() > 0) {
				for (TravelGroupPermittedBoardingLodging permBnL : tGroupPermBnL) {
					tGroupPermAccomodationTypeStr += permBnL.getAccomodationType().getAccomodationTypeName() + ",";
					maxPermittedRoomCostPerNight = permBnL.getMaxPermittedRoomCostPerNight();
					maxPermittedFoodCostPerDay = permBnL.getMaxPermittedFoodCostPerDay();
					if (maxPermittedRoomCostPerNight > higherMaxPermittedRoomCostPerNight) {
						higherMaxPermittedRoomCostPerNight = maxPermittedRoomCostPerNight;
					}
					if (maxPermittedFoodCostPerDay > higherMaxPermittedFoodCostPerDay) {
						higherMaxPermittedFoodCostPerDay = maxPermittedFoodCostPerDay;
					}
				}
			}
			ObjectNode bnLRow = Json.newObject();
			if (tGroupPermAccomodationTypeStr.equals("")) {
				bnLRow.put("accomodationType", tGroupPermAccomodationTypeStr);
			} else {
				bnLRow.put("accomodationType",
						tGroupPermAccomodationTypeStr.substring(0, tGroupPermAccomodationTypeStr.length() - 1));
			}
			bnLRow.put("maxPermittedRoomCostPerNight",
					IdosConstants.decimalFormat.format(higherMaxPermittedRoomCostPerNight));
			bnLRow.put("maxPermittedFoodCostPerDay",
					IdosConstants.decimalFormat.format(higherMaxPermittedFoodCostPerDay));
			bnLAn.add(bnLRow);
			sbr.delete(0, sbr.length());
			sbr.append(
					"select obj from TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses obj where obj.organization='"
							+ user.getOrganization().getId() + "' and obj.presentStatus=1 and obj.travelgroup='"
							+ tGroup.getId() + "'");
			List<TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses> dailyMaxOtherExpensesList = genericDao
					.executeSimpleQuery(sbr.toString(), entityManager);
			Double dailyOtherExpenses = 0.0;
			Double dailyPerDIAM = 0.0;
			if (!dailyMaxOtherExpensesList.isEmpty() && dailyMaxOtherExpensesList.size() > 0) {
				for (TravelGroupMaxDailyLimitOtherOfficialPurposeExpenses tGroupDailyMaxOtherExpenses : dailyMaxOtherExpensesList) {
					switch (cityType) {
						case 1:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getCountryCapital();
							break;
						case 2:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getStateCapital();
							break;
						case 3:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getMetroCity();
							break;
						case 4:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getOtherCities();
							break;
						case 5:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getTown();
							break;
						case 6:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getCounty();
							break;
						case 7:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getMunicipality();
							break;
						case 8:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getVillage();
							break;
						case 9:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getRemoteLocation();
							break;
						case 10:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getTwentyMilesAwayFromClosestCityTown();
							break;
						case 11:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getHillStation();
							break;
						case 12:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getResort();
							break;
						case 13:
							dailyOtherExpenses = tGroupDailyMaxOtherExpenses.getConflictWarZonePlace();
							break;
					}
				}
			}
			ObjectNode otherExpRow = Json.newObject();
			otherExpRow.put("dailyOtherExpenses", IdosConstants.decimalFormat.format(dailyOtherExpenses));
			dOthExpensesAn.add(otherExpRow);
			sbr.delete(0, sbr.length());
			sbr.append("select obj from TravelGroupFixedDailyPerDIAM obj where obj.organization='"
					+ user.getOrganization().getId() + "' and obj.presentStatus=1 and obj.travelgroup='"
					+ tGroup.getId() + "'");
			List<TravelGroupFixedDailyPerDIAM> tGroupFixedPerDiamList = genericDao.executeSimpleQuery(sbr.toString(),
					entityManager);
			if (!tGroupFixedPerDiamList.isEmpty() && tGroupFixedPerDiamList.size() > 0) {
				for (TravelGroupFixedDailyPerDIAM tGroupFxdDIAM : tGroupFixedPerDiamList) {
					switch (cityType) {
						case 1:
							dailyPerDIAM = tGroupFxdDIAM.getCountryCapital();
							break;
						case 2:
							dailyPerDIAM = tGroupFxdDIAM.getStateCapital();
							break;
						case 3:
							dailyPerDIAM = tGroupFxdDIAM.getMetroCity();
							break;
						case 4:
							dailyPerDIAM = tGroupFxdDIAM.getOtherCities();
							break;
						case 5:
							dailyPerDIAM = tGroupFxdDIAM.getTown();
							break;
						case 6:
							dailyPerDIAM = tGroupFxdDIAM.getCounty();
							break;
						case 7:
							dailyPerDIAM = tGroupFxdDIAM.getMunicipality();
							break;
						case 8:
							dailyPerDIAM = tGroupFxdDIAM.getVillage();
							break;
						case 9:
							dailyPerDIAM = tGroupFxdDIAM.getRemoteLocation();
							break;
						case 10:
							dailyPerDIAM = tGroupFxdDIAM.getTwentyMilesAwayFromClosestCityTown();
							break;
						case 11:
							dailyPerDIAM = tGroupFxdDIAM.getHillStation();
							break;
						case 12:
							dailyPerDIAM = tGroupFxdDIAM.getResort();
							break;
						case 13:
							dailyPerDIAM = tGroupFxdDIAM.getConflictWarZonePlace();
							break;
					}
				}
			}
			ObjectNode fxdDIAMRow = Json.newObject();
			fxdDIAMRow.put("dailyPerDIAM", IdosConstants.decimalFormat.format(dailyPerDIAM));
			fxdDIAMRow.put("availableAdvance", IdosConstants.decimalFormat
					.format(user.getTravelAdvanceAccountAmount() - user.getTravelAdvanceSettledAmount()));
			fixedPerDIAMAn.add(fxdDIAMRow);
			List<TravelGroupKnowledgeLibrary> tavelGroulKLS = tGroup.getTravelGroupkL();
			for (TravelGroupKnowledgeLibrary tGpkl : tavelGroulKLS) {
				ObjectNode klRow = Json.newObject();
				klRow.put("klContent", tGpkl.getKlContent());
				klRow.put("klIsMandatory", tGpkl.getKlMandatory());
				tGroupKLAn.add(klRow);
			}
		}
		log.log(Level.FINE, "************* End " + result);
		return result;
	}

	@Override
	public ObjectNode submitForApproval(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException {
		log.log(Level.FINE, "************* Start");
		Map<String, Object> criterias = new HashMap<String, Object>();
		// String
		// claimTxnPurposeText=json.findValue("claimTxnPurposeText")!=null?json.findValue("claimTxnPurposeText").asText():null;
		String claimTxnPurposeVal = json.findValue("claimTxnPurposeVal") != null
				? json.findValue("claimTxnPurposeVal").asText()
				: null;

		entitytransaction.begin();
		Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
				: json.findValue("txnEntityID").asLong();
		String claimtxnBranch = json.findValue("claimtxnBranch") != null ? json.findValue("claimtxnBranch").asText()
				: null;
		String claimtxnProject = json.findValue("claimtxnProject") != null ? json.findValue("claimtxnProject").asText()
				: null;
		String claimtravelType = json.findValue("claimtravelType") != null ? json.findValue("claimtravelType").asText()
				: null;
		String claimnoOfPlacesToVisit = json.findValue("claimnoOfPlacesToVisit") != null
				? json.findValue("claimnoOfPlacesToVisit").asText()
				: null;
		String claimplacesSelectedOrEntered = json.findValue("claimplacesSelectedOrEntered") != null
				? json.findValue("claimplacesSelectedOrEntered").asText()
				: null;
		String claimtypeOfCity = json.findValue("claimtypeOfCity") != null ? json.findValue("claimtypeOfCity").asText()
				: null;
		String claimappropriateDiatance = json.findValue("claimappropriateDiatance") != null
				? json.findValue("claimappropriateDiatance").asText()
				: null;
		String claimtotalDays = json.findValue("claimtotalDays") != null ? json.findValue("claimtotalDays").asText()
				: null;
		String claimtravelDetailedConfDescription = json.findValue("claimtravelDetailedConfDescription") != null
				? json.findValue("claimtravelDetailedConfDescription").asText()
				: null;
		String claimuserAdvanveEligibility = json.findValue("claimuserAdvanveEligibility") != null
				? json.findValue("claimuserAdvanveEligibility").asText()
				: null;
		String claimexistingAdvance = json.findValue("claimexistingAdvance") != null
				? json.findValue("claimexistingAdvance").asText()
				: null;
		String claimadjustedAdvance = json.findValue("claimadjustedAdvance") != null
				? json.findValue("claimadjustedAdvance").asText()
				: null;
		String claimSelectedClaimTxnForAdjustment = json.findValue("claimSelectedClaimTxnForAdjustment") != null
				? json.findValue("claimSelectedClaimTxnForAdjustment").asText()
				: null;
		String claimenteredAdvance = json.findValue("claimenteredAdvance") != null
				? json.findValue("claimenteredAdvance").asText()
				: null;
		String claimtotalAdvance = json.findValue("claimtotalAdvance") != null
				? json.findValue("claimtotalAdvance").asText()
				: null;
		String claimklmandatoryfollowednotfollowed = json.findValue("claimklmandatoryfollowednotfollowed") != null
				? json.findValue("claimklmandatoryfollowednotfollowed").asText()
				: null;
		String claimklcontents = json.findValue("claimklcontents") != null ? json.findValue("claimklcontents").asText()
				: null;
		String claimpurposeOfVisit = json.findValue("claimpurposeOfVisit") != null
				? json.findValue("claimpurposeOfVisit").asText()
				: null;
		String claimtxnRemarks = json.findValue("claimtxnRemarks") != null ? json.findValue("claimtxnRemarks").asText()
				: null;
		String claimsupportingDoc = json.findValue("claimsupportingDoc") != null
				? json.findValue("claimsupportingDoc").asText()
				: null;
		String debitCredit = "Debit";
		Branch txnBnch = null;
		Organization txnOrgn = null;
		Project txnProject = null;
		TransactionPurpose txnPurpose = null;
		String txnRemarks = null;
		String txnDocument = "";
		ClaimTransaction claimTransaction = null;
		if (txnEntityID > 0) {
			claimTransaction = ClaimTransaction.findById(txnEntityID);
		} else {
			claimTransaction = new ClaimTransaction();
		}
		// ClaimTransaction claimTransaction=new ClaimTransaction();
		if (claimTxnPurposeVal != null && !claimTxnPurposeVal.equals("")) {
			txnPurpose = TransactionPurpose.findById(Long.parseLong(claimTxnPurposeVal));
		}
		claimTransaction.setTransactionPurpose(txnPurpose);
		if (claimtxnBranch != null && !claimtxnBranch.equals("")) {
			txnBnch = Branch.findById(Long.parseLong(claimtxnBranch));
		}
		claimTransaction.setTransactionBranch(txnBnch);
		txnOrgn = txnBnch.getOrganization();
		claimTransaction.setTransactionBranchOrganization(txnOrgn);
		if (claimtxnProject != null && !claimtxnProject.equals("")) {
			txnProject = Project.findById(Long.parseLong(claimtxnProject));
		}
		claimTransaction.setTransactionProject(txnProject);
		if (claimtravelType != null && !claimtravelType.equals("")) {
			claimTransaction.setTravelType(claimtravelType);
		}
		if (claimnoOfPlacesToVisit != null && !claimnoOfPlacesToVisit.equals("")) {
			claimTransaction.setNumberOfPlacesToVisit(Integer.parseInt(claimnoOfPlacesToVisit));
		}
		if (claimplacesSelectedOrEntered != null && !claimplacesSelectedOrEntered.equals("")) {
			claimTransaction.setTravelFromToPlaces(claimplacesSelectedOrEntered);
		}
		if (claimtypeOfCity != null && !claimtypeOfCity.equals("")) {
			claimTransaction.setTypeOfCity(claimtypeOfCity);
		}
		if (claimappropriateDiatance != null && !claimappropriateDiatance.equals("")) {
			claimTransaction.setAppropriateDistance(claimappropriateDiatance);
		}
		if (claimtotalDays != null && !claimtotalDays.equals("")) {
			claimTransaction.setTotalDays(Integer.parseInt(claimtotalDays));
		}
		if (claimtravelDetailedConfDescription != null && !claimtravelDetailedConfDescription.equals("")) {
			claimTransaction.setTravelEligibilityDetails(claimtravelDetailedConfDescription);
		}
		if (claimuserAdvanveEligibility != null && !claimuserAdvanveEligibility.equals("")) {
			claimTransaction.setAdvanceEligibilityDetails(claimuserAdvanveEligibility);
		}
		if (claimexistingAdvance != null && !claimexistingAdvance.equals("")) {
			claimTransaction.setExistingAdvance(Double.parseDouble(claimexistingAdvance));
		}
		if (claimadjustedAdvance != null && !claimadjustedAdvance.equals("")) {
			claimTransaction.setAdjustedAdvance(Double.valueOf(claimadjustedAdvance));
		}
		if (claimenteredAdvance != null && !claimenteredAdvance.equals("")) {
			claimTransaction.setGrossAmount(Double.parseDouble(claimenteredAdvance));
			claimTransaction.setClaimsDueSettlement(Double.valueOf(claimenteredAdvance));
			claimTransaction.setNewAmount(Double.parseDouble(claimenteredAdvance));
			claimTransaction.setClaimsNetSettlement(Double.parseDouble(claimenteredAdvance));
		}
		if (claimtotalAdvance != null && !claimtotalAdvance.equals("")) {
			if (claimadjustedAdvance != null && !claimadjustedAdvance.equals("")) {
				claimTransaction.setGrossAmount(Double.parseDouble(claimenteredAdvance));
				claimTransaction.setClaimsDueSettlement(Double.valueOf(claimenteredAdvance));
				claimTransaction.setNewAmount(Double.parseDouble(claimenteredAdvance));
				if (claimSelectedClaimTxnForAdjustment != null && !claimSelectedClaimTxnForAdjustment.equals("")) {
					ClaimTransaction previousClaimTxnRefNo = ClaimTransaction
							.findById(Long.parseLong(claimSelectedClaimTxnForAdjustment));
					claimTransaction
							.setClaimsAdvanceAdjustmentsRefNumber(previousClaimTxnRefNo.getTransactionRefNumber());
					claimTransaction.setCreatedBy(user);
					deductAdjustedAdvanceAndUpdateClaimTransactionFromWhichAdjusted(claimTransaction, user,
							previousClaimTxnRefNo);
				}
				// logic to find the travel claim transaction from which advance is
				// adjusted and check for claims due payment and hence settle the claim
				// transaction
				// if due >0 not-settled id due==0 settled
			} else {
				claimTransaction.setGrossAmount(Double.parseDouble(claimenteredAdvance));
				claimTransaction.setNewAmount(Double.parseDouble(claimenteredAdvance));
				claimTransaction.setClaimsDueSettlement(Double.valueOf(claimenteredAdvance));
			}
		}
		if (claimklmandatoryfollowednotfollowed != null && !claimklmandatoryfollowednotfollowed.equals("")) {
			claimTransaction.setKlFollowStatus(Integer.parseInt(claimklmandatoryfollowednotfollowed));
		}
		if (claimklcontents != null && !claimklcontents.equals("")) {
			claimTransaction.setPresentTxnRules(claimklcontents);
		}
		claimTransaction.setTransactionDate(Calendar.getInstance().getTime());
		if (claimpurposeOfVisit != null && !claimpurposeOfVisit.equals("")) {
			claimTransaction.setPurposeOfVisit(claimpurposeOfVisit);
		}
		if (claimtxnRemarks != null && !claimtxnRemarks.equals("")) {
			if (claimTransaction.getTxnRemarks() != null) {
				txnRemarks = claimTransaction.getTxnRemarks() + "," + user.getEmail() + "#" + claimtxnRemarks;
				claimTransaction.setTxnRemarks(txnRemarks);
			} else {
				txnRemarks = user.getEmail() + "#" + claimtxnRemarks;
				claimTransaction.setTxnRemarks(txnRemarks);
			}
		}
		claimTransaction.setSupportingDocuments(TRANSACTION_DAO.getAndDeleteSupportingDocument(
				claimTransaction.getSupportingDocuments(), user.getEmail(), claimsupportingDoc, user, entityManager));

		claimTransaction.setSettlementStatus("NOT-SETTLED");
		claimTransaction.setTransactionStatus("Require Approval");
		// list of additional users all approver role users of thet organization
		criterias.clear();
		criterias.put("role.name", "APPROVER");
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
		String approverEmails = "";
		String additionalApprovarUsers = "";
		String selectedAdditionalApproval = "";
		for (UsersRoles usrRoles : approverRole) {
			additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
			criterias.clear();
			criterias.put("user.id", usrRoles.getUser().getId());
			criterias.put("userRights.id", 2L);
			criterias.put("branch.id", txnBnch.getId());
			criterias.put("presentStatus", 1);
			UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class, criterias,
					entityManager);
			if (userHasRightInBranch != null) {
				approverEmails += usrRoles.getUser().getEmail() + ",";
			}
		}
		String accountantEmailsStr = "";
		criterias.clear();
		criterias.put("role.id", 5l);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		List<UsersRoles> accountantRole = genericDao.findByCriteria(UsersRoles.class, criterias, entityManager);
		for (UsersRoles usrRoles : accountantRole) {
			accountantEmailsStr += usrRoles.getUser().getEmail() + ",";
		}
		claimTransaction.setDebitCredit(debitCredit);
		claimTransaction.setApproverMails(approverEmails);
		claimTransaction.setAccountantEmails(accountantEmailsStr);
		claimTransaction.setAdditionalApproverEmails(additionalApprovarUsers);
		String transactionNumber = CodeHelper.getForeverUniqueID("CLAIMTXN", null);
		claimTransaction.setTransactionRefNumber(transactionNumber);
		genericDao.saveOrUpdate(claimTransaction, user, entityManager);
		FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, claimsupportingDoc, claimTransaction.getId(),
				IdosConstants.CLAIM_TXN_TYPE);
		entitytransaction.commit();
		sendSocketResponeToClient(claimTransaction, user, result);

		log.log(Level.FINE, "************* End " + result);
		return result;
	}

	private void sendSocketResponeToClient(ClaimTransaction claimTransaction, Users user, ObjectNode result) {
		log.log(Level.FINE, "************* Start");
		Long id = claimTransaction.getId() != null ? claimTransaction.getId() : null;
		String branchName = claimTransaction.getTransactionBranch() != null
				? claimTransaction.getTransactionBranch().getName()
				: "";
		String projectName = claimTransaction.getTransactionProject() != null
				? claimTransaction.getTransactionProject().getName()
				: "";
		String txnQuestionName = claimTransaction.getTransactionPurpose() != null
				? claimTransaction.getTransactionPurpose().getTransactionPurpose()
				: "";
		String txnOrgnName = claimTransaction.getTransactionBranchOrganization() != null
				? claimTransaction.getTransactionBranchOrganization().getName()
				: "";
		String travelType = claimTransaction.getTravelType() != null ? claimTransaction.getTravelType() : "";
		String noOfPlacesToVisit = claimTransaction.getNumberOfPlacesToVisit() != null
				? claimTransaction.getNumberOfPlacesToVisit().toString()
				: "";
		String placesSelectedOrEntered = claimTransaction.getTravelFromToPlaces() != null
				? claimTransaction.getTravelFromToPlaces()
				: "";
		String typeOfCity = claimTransaction.getTypeOfCity() != null ? claimTransaction.getTypeOfCity() : "";
		String appropriateDiatance = claimTransaction.getAppropriateDistance() != null
				? claimTransaction.getAppropriateDistance()
				: "";
		String totalDays = claimTransaction.getTotalDays() != null ? claimTransaction.getTotalDays().toString() : "";
		String travelDetailedConfDescription = claimTransaction.getTravelEligibilityDetails() != null
				? claimTransaction.getTravelEligibilityDetails()
				: "";
		String claimuserAdvanveEligibility = claimTransaction.getAdvanceEligibilityDetails() != null
				? claimTransaction.getAdvanceEligibilityDetails()
				: "";
		Double existingAdvance = claimTransaction.getExistingAdvance() != null ? claimTransaction.getExistingAdvance()
				: 0.0;
		Double adjustedAdvance = claimTransaction.getAdjustedAdvance() != null ? claimTransaction.getAdjustedAdvance()
				: 0.0;
		Double enteredAdvance = claimTransaction.getGrossAmount() != null ? claimTransaction.getGrossAmount() : 0.0;
		Double totalAdvance = claimTransaction.getNewAmount() != null ? claimTransaction.getNewAmount() : 0.0;
		String purposeOfVisit = claimTransaction.getPurposeOfVisit() != null ? claimTransaction.getPurposeOfVisit()
				: "";
		String claimTxnRemarks = claimTransaction.getTxnRemarks() != null ? claimTransaction.getTxnRemarks() : "";
		String supportingDoc = claimTransaction.getSupportingDocuments() != null
				? claimTransaction.getSupportingDocuments()
				: "";
		String claimdebitCredit = claimTransaction.getDebitCredit() != null ? claimTransaction.getDebitCredit() : "";
		String claimTxnStatus = claimTransaction.getTransactionStatus() != null
				? claimTransaction.getTransactionStatus()
				: "";
		String claimapproverEmails = claimTransaction.getApproverMails() != null ? claimTransaction.getApproverMails()
				: "";
		String additionalApprovalEmails = claimTransaction.getAdditionalApproverEmails() != null
				? claimTransaction.getAdditionalApproverEmails()
				: "";
		String claimselectedAdditionalApproval = claimTransaction.getSelectedAdditionalApprover() != null
				? claimTransaction.getSelectedAdditionalApprover()
				: "";
		String creatorLabel = "Created By:";
		String createdBy = claimTransaction.getCreatedBy() != null ? claimTransaction.getCreatedBy().getEmail() : "";
		String approverLabel = "Approved By:";
		String approvedBy = claimTransaction.getApproverActionBy() != null
				? claimTransaction.getApproverActionBy().getEmail()
				: "";
		String accountantEmails = claimTransaction.getAccountantEmails() != null
				? claimTransaction.getAccountantEmails()
				: "";
		String accountedLabel = "Accounted By:";
		String accountedBy = claimTransaction.getAccountingActionBy() != null
				? claimTransaction.getAccountingActionBy().getEmail()
				: "";
		String transactionDate = claimTransaction.getTransactionDate() != null
				? IdosConstants.IDOSDF.format(claimTransaction.getTransactionDate())
				: "";
		String claimTxnRefNo = claimTransaction.getTransactionRefNumber() != null
				? claimTransaction.getTransactionRefNumber()
				: "";
		String txnSpecialStatus = "";
		String instrumentNumber = "";
		String instrumentDate = "";
		if (claimTransaction.getKlFollowStatus() != null) {
			if (claimTransaction.getKlFollowStatus() == 0) {
				txnSpecialStatus = "Rules Not Followed";
			}
		}
		String paymentMode = "";
		if (claimTransaction.getReceiptDetailType() != null) {
			if (claimTransaction.getReceiptDetailType() == 1) {
				paymentMode = "CASH";
			}
			if (claimTransaction.getReceiptDetailType() == 2) {
				paymentMode = "BANK";
				instrumentNumber = claimTransaction.getInstrumentNumber() != null
						? claimTransaction.getInstrumentNumber()
						: "";
				instrumentDate = claimTransaction.getInstrumentDate() != null ? claimTransaction.getInstrumentDate()
						: "";
			}
		}
		// Map<String, ActorRef> orgclaimregistrered = new HashMap<String, ActorRef>();
		// Object[] keyArray = CreatorActor.expenseregistrered.keySet().toArray();
		// for (int i = 0; i < keyArray.length; i++) {
		// StringBuilder sbquery = new StringBuilder("");
		// sbquery.append("select obj from Users obj WHERE obj.email ='" + keyArray[i] +
		// "' and obj.presentStatus=1");
		// List<Users> orgusers = genericDao.executeSimpleQuery(sbquery.toString(),
		// entityManager);
		// if (!orgusers.isEmpty() && orgusers.get(0).getOrganization().getId() ==
		// user.getOrganization().getId()) {
		// orgclaimregistrered.put(keyArray[i].toString(),
		// CreatorActor.expenseregistrered.get(keyArray[i]));
		// }
		// }
		TransactionViewResponse.addClaimTxn(id, branchName, projectName, txnQuestionName, txnOrgnName, travelType,
				noOfPlacesToVisit, placesSelectedOrEntered, typeOfCity, appropriateDiatance, totalDays,
				travelDetailedConfDescription, existingAdvance, claimuserAdvanveEligibility, adjustedAdvance,
				enteredAdvance, totalAdvance, purposeOfVisit, claimTxnRemarks, supportingDoc, claimdebitCredit,
				claimTxnStatus, claimapproverEmails, additionalApprovalEmails, claimselectedAdditionalApproval,
				creatorLabel, createdBy, transactionDate, approverLabel, approvedBy, accountantEmails, accountedLabel,
				accountedBy, txnSpecialStatus, paymentMode, instrumentNumber, instrumentDate,
				user.getEmail(), claimTxnRefNo, result);
		log.log(Level.FINE, "************* End ");
	}

	@Override
	public ObjectNode approverAction(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException {
		log.log(Level.FINE, "************* Start");
		BranchCashService branchCashService = new BranchCashServiceImpl();
		BranchBankService branchBankService = new BranchBankServiceImpl();
		TrialBalanceService trialBalanceService = new TrialBalanceServiceImpl();
		entitytransaction.begin();
		String selectedApproverAction = json.findValue("selectedApproverAction").asText();
		String transactionPrimId = json.findValue("transactionPrimId").asText();
		String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
		String txnRmarks = json.findValue("txnRmarks").asText();
		String txnInstrumentNumber = null;
		String txnInstrumentDate = null;
		ClaimTransaction claimTransaction = ClaimTransaction.findById(Long.parseLong(transactionPrimId));
		Map<String, Object> criterias = new HashMap<String, Object>();
		// logic for approval approved select action
		if (!claimTransaction.getTransactionStatus().equals("Approved")) {
			if (selectedApproverAction.equals("1")) {
				saveAndUpdateSupportingDocAndRemarks(claimTransaction, user, suppDoc, txnRmarks);
				claimTransaction.setTransactionStatus("Approved");
				claimTransaction.setModifiedBy(user);
				claimTransaction.setApproverActionBy(user);
			}
		}
		if (!claimTransaction.getTransactionStatus().equals("Rejected")) {
			if (selectedApproverAction.equals("2")) {
				saveAndUpdateSupportingDocAndRemarks(claimTransaction, user, suppDoc, txnRmarks);
				claimTransaction.setTransactionStatus("Rejected");
				claimTransaction.setSettlementStatus("SETTLED");
				claimTransaction.setModifiedBy(user);
				claimTransaction.setApproverActionBy(user);
				if (claimTransaction.getClaimsAdvanceAdjustmentsRefNumber() != null
						&& !claimTransaction.getClaimsAdvanceAdjustmentsRefNumber().equals("")) {
					criterias.clear();
					criterias.put("transactionRefNumber", claimTransaction.getClaimsAdvanceAdjustmentsRefNumber());
					ClaimTransaction prevClaimTxn = genericDao.getByCriteria(ClaimTransaction.class, criterias,
							entityManager);
					addAdjustedAdvanceAndUpdateClaimTransactionFromWhichAdjusted(claimTransaction, user, prevClaimTxn);
				}
			}
		}
		if (!claimTransaction.getTransactionStatus().equals("Require Additional Approval")) {
			if (selectedApproverAction.equals("3")) {
				saveAndUpdateSupportingDocAndRemarks(claimTransaction, user, suppDoc, txnRmarks);
				claimTransaction.setTransactionStatus("Require Additional Approval");
				claimTransaction.setModifiedBy(user);
				claimTransaction.setApproverActionBy(user);
				String selectedAddApproverEmail = json.findValue("selectedAddApproverEmail").asText();
				claimTransaction.setSelectedAdditionalApprover(selectedAddApproverEmail);
			}
		}
		if (!claimTransaction.getTransactionStatus().equals("Accounted")) {
			claimTransaction.setTransactionDate(Calendar.getInstance().getTime());
			if (selectedApproverAction.equals("4")) {
				saveAndUpdateSupportingDocAndRemarks(claimTransaction, user, suppDoc, txnRmarks);
				claimTransaction.setTransactionStatus("Accounted");
				claimTransaction.setModifiedBy(user);
				claimTransaction.setAccountingActionBy(user);
				claimTransaction.setSettlementStatus("NOT-SETTLED");
				// *******TRIAL BALANCE CHANGES: START***********//
				// When emp takes advance from company, it is debit amt for user

				TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserAdvance(claimTransaction, user, entityManager,
						claimTransaction.getNewAmount(), false);

				TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserClaim(claimTransaction, user, entityManager,
						claimTransaction.getNewAmount(), true);
				/*************** TRIAL BALANCE CHNAGES: END **********/

			}
		}
		if (!claimTransaction.getTransactionStatus().equals("Require Clarification")) {
			if (selectedApproverAction.equals("5")) {
				saveAndUpdateSupportingDocAndRemarks(claimTransaction, user, suppDoc, txnRmarks);
				claimTransaction.setTransactionStatus("Require Clarification");
				claimTransaction.setModifiedBy(user);
				claimTransaction.setApproverActionBy(user);
			}
		}
		if (!claimTransaction.getTransactionStatus().equals("Clarified")) {
			if (selectedApproverAction.equals("6")) {
				saveAndUpdateSupportingDocAndRemarks(claimTransaction, user, suppDoc, txnRmarks);
				claimTransaction.setTransactionStatus("Clarified");
				claimTransaction.setModifiedBy(user);
				claimTransaction.setApproverActionBy(user);
			}
		}
		if (selectedApproverAction.equals("7")) {
			claimTransaction.setModifiedBy(user);
			saveAndUpdateSupportingDocAndRemarks(claimTransaction, user, suppDoc, txnRmarks);
		}
		KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
		karvyAPICall.saveGSTFilingDataForClaimTransaction(user, claimTransaction, entityManager);
		entitytransaction.commit();
		if (claimTransaction.getTransactionPurpose().getTransactionPurpose().equals("Request For Travel Advance")) {
			sendSocketResponeToClient(claimTransaction, user, result);
		}
		if (claimTransaction.getTransactionPurpose().getTransactionPurpose().equals("Settle Travel Advance")) {
			claimsSettlementService.sendSocketResponeToClientSettlement(claimTransaction, user, result);
		}
		log.log(Level.FINE, "************* End " + result);
		return result;

	}

	/**
	 * Used in Request Travel Advance
	 * 
	 * @param claimTransaction
	 * @param user
	 * @param previousClaimTxn
	 */
	private void deductAdjustedAdvanceAndUpdateClaimTransactionFromWhichAdjusted(ClaimTransaction claimTransaction,
			Users user, ClaimTransaction previousClaimTxn) {
		log.log(Level.FINE, "************* Start");
		Double travelAdvanceAdjustedAmount = 0.0;
		Users createdUser = claimTransaction.getCreatedBy();
		if (createdUser.getTravelAdvanceAccountAmount() != null) {
			if (claimTransaction.getAdjustedAdvance() != null) {
				travelAdvanceAdjustedAmount = createdUser.getTravelAdvanceAccountAmount()
						- claimTransaction.getAdjustedAdvance();
				createdUser.setTravelAdvanceAccountAmount(travelAdvanceAdjustedAmount);
			} else {
				travelAdvanceAdjustedAmount = createdUser.getTravelAdvanceAccountAmount();
				createdUser.setTravelAdvanceAccountAmount(travelAdvanceAdjustedAmount);
			}
		}
		if (createdUser.getTravelAdvanceAccountAmount() == null) {
			if (claimTransaction.getAdjustedAdvance() != null) {
				travelAdvanceAdjustedAmount = claimTransaction.getAdjustedAdvance();
				createdUser.setTravelAdvanceAccountAmount(travelAdvanceAdjustedAmount);
			}
		}
		deductPreviousTravelClaimTxnAdvanceAccount(claimTransaction, user, previousClaimTxn);
		log.log(Level.FINE, "************* End ");
	}

	/**
	 * Used in Request Travel Advance
	 * 
	 * @param claimTransaction
	 * @param user
	 * @param previousClaimTxn
	 */
	private void deductPreviousTravelClaimTxnAdvanceAccount(ClaimTransaction claimTransaction, Users user,
			ClaimTransaction previousClaimTxn) {
		log.log(Level.FINE, "************* Start");
		Double claimsNetSettlement = 0.0;
		Double claimsDueSettlement = 0.0;
		Double claimsSpentSettlement = 0.0;
		Double claimsRequiredSettlement = 0.0;
		Double claimsReturnSettlement = 0.0;
		String claimSettlementStatus = null;
		if (previousClaimTxn.getClaimsNetSettlement() != null) {
			claimsNetSettlement = previousClaimTxn.getClaimsNetSettlement();
		}
		if (previousClaimTxn.getClaimsDueSettlement() != null) {
			claimsDueSettlement = previousClaimTxn.getClaimsDueSettlement();
		}
		if (previousClaimTxn.getClaimsSpentSettlement() != null) {
			claimsSpentSettlement = previousClaimTxn.getClaimsSpentSettlement();
		}
		if (previousClaimTxn.getClaimsRequiredSettlement() != null) {
			claimsRequiredSettlement = previousClaimTxn.getClaimsRequiredSettlement();
		}
		if (previousClaimTxn.getClaimsReturnSettlement() != null) {
			claimsReturnSettlement = previousClaimTxn.getClaimsReturnSettlement();
		}
		if (claimTransaction.getAdjustedAdvance() != null) {
			claimsDueSettlement -= claimTransaction.getAdjustedAdvance();
			claimsSpentSettlement += claimTransaction.getAdjustedAdvance();
		}
		if (claimsSpentSettlement > previousClaimTxn.getNewAmount()) {
			claimsRequiredSettlement = claimsSpentSettlement - previousClaimTxn.getNewAmount();
		} else {
			claimsReturnSettlement = previousClaimTxn.getNewAmount() - claimsSpentSettlement;
		}
		if (claimsDueSettlement == 0.0) {
			claimSettlementStatus = "SETTLED";
		} else {
			claimSettlementStatus = "NOT-SETTLED";
		}
		previousClaimTxn.setClaimsNetSettlement(claimsNetSettlement);
		previousClaimTxn.setClaimsDueSettlement(claimsDueSettlement);
		previousClaimTxn.setClaimsRequiredSettlement(claimsRequiredSettlement);
		previousClaimTxn.setClaimsReturnSettlement(claimsReturnSettlement);
		previousClaimTxn.setSettlementStatus(claimSettlementStatus);
		genericDao.saveOrUpdate(previousClaimTxn, user, entityManager);
		log.log(Level.FINE, "************* End ");
	}

	/**
	 * Used both
	 * 
	 * @param claimTransaction
	 * @param user
	 * @param previousClaimTxn
	 */
	private void addPreviousTravelClaimTxnAdvanceAccount(ClaimTransaction claimTransaction, Users user,
			ClaimTransaction previousClaimTxn) {
		log.log(Level.FINE, "************* Start");
		Double claimsNetSettlement = 0.0;
		Double claimsDueSettlement = 0.0;
		Double claimsSpentSettlement = 0.0;
		Double claimsRequiredSettlement = 0.0;
		Double claimsReturnSettlement = 0.0;
		String claimSettlementStatus = null;
		if (previousClaimTxn.getClaimsNetSettlement() != null) {
			claimsNetSettlement = previousClaimTxn.getClaimsNetSettlement();
		}
		if (previousClaimTxn.getClaimsDueSettlement() != null) {
			claimsDueSettlement = previousClaimTxn.getClaimsDueSettlement();
		}
		if (previousClaimTxn.getClaimsSpentSettlement() != null) {
			claimsSpentSettlement = previousClaimTxn.getClaimsSpentSettlement();
		}
		if (previousClaimTxn.getClaimsRequiredSettlement() != null) {
			claimsRequiredSettlement = previousClaimTxn.getClaimsRequiredSettlement();
		}
		if (previousClaimTxn.getClaimsReturnSettlement() != null) {
			claimsReturnSettlement = previousClaimTxn.getClaimsReturnSettlement();
		}
		if (claimTransaction.getAdjustedAdvance() != null) {
			claimsDueSettlement += claimTransaction.getAdjustedAdvance();
			claimsSpentSettlement -= claimTransaction.getAdjustedAdvance();
		}
		if (claimsSpentSettlement > previousClaimTxn.getNewAmount()) {
			claimsRequiredSettlement = claimsSpentSettlement - previousClaimTxn.getNewAmount();
		} else {
			claimsReturnSettlement = previousClaimTxn.getNewAmount() - claimsSpentSettlement;
		}
		if (claimsDueSettlement == 0.0) {
			claimSettlementStatus = "SETTLED";
		} else {
			claimSettlementStatus = "NOT-SETTLED";
		}
		previousClaimTxn.setClaimsNetSettlement(claimsNetSettlement);
		previousClaimTxn.setClaimsDueSettlement(claimsDueSettlement);
		previousClaimTxn.setClaimsRequiredSettlement(claimsRequiredSettlement);
		previousClaimTxn.setClaimsReturnSettlement(claimsReturnSettlement);
		previousClaimTxn.setSettlementStatus(claimSettlementStatus);
		genericDao.saveOrUpdate(previousClaimTxn, user, entityManager);

		log.log(Level.FINE, "************* End ");
	}

	/**
	 * USe both
	 * 
	 * @param claimTransaction
	 * @param user
	 * @param previousClaimTxn
	 */
	private void addAdjustedAdvanceAndUpdateClaimTransactionFromWhichAdjusted(ClaimTransaction claimTransaction,
			Users user, ClaimTransaction previousClaimTxn) {
		log.log(Level.FINE, "************* Start");
		Double travelAdvanceAdjustedAmount = 0.0;
		Users createdUser = claimTransaction.getCreatedBy();
		if (createdUser.getTravelAdvanceAccountAmount() != null) {
			if (claimTransaction.getAdjustedAdvance() != null) {
				travelAdvanceAdjustedAmount = createdUser.getTravelAdvanceAccountAmount()
						+ claimTransaction.getAdjustedAdvance();
				createdUser.setTravelAdvanceAccountAmount(travelAdvanceAdjustedAmount);
			} else {
				travelAdvanceAdjustedAmount = createdUser.getTravelAdvanceAccountAmount();
				createdUser.setTravelAdvanceAccountAmount(travelAdvanceAdjustedAmount);
			}
		}
		if (createdUser.getTravelAdvanceAccountAmount() == null) {
			if (claimTransaction.getAdjustedAdvance() != null) {
				travelAdvanceAdjustedAmount = claimTransaction.getAdjustedAdvance();
				createdUser.setTravelAdvanceAccountAmount(travelAdvanceAdjustedAmount);
			}
		}
		addPreviousTravelClaimTxnAdvanceAccount(claimTransaction, user, previousClaimTxn);
		log.log(Level.FINE, "************* End ");
	}

	/**
	 * used both
	 * 
	 * @param claimTransaction
	 * @param user
	 */
	@Override
	public void addAdvanceToUserAccount(ClaimTransaction claimTransaction, Users user) {
		log.log(Level.FINE, "************* Start");
		Double travelAdvanceAccountAmount = 0.0;
		Users createdUser = claimTransaction.getCreatedBy();
		if (createdUser.getTravelAdvanceAccountAmount() != null) {
			travelAdvanceAccountAmount = createdUser.getTravelAdvanceAccountAmount() + claimTransaction.getNewAmount();
			createdUser.setTravelAdvanceAccountAmount(travelAdvanceAccountAmount);
		}
		if (createdUser.getTravelAdvanceAccountAmount() == null) {
			travelAdvanceAccountAmount = claimTransaction.getNewAmount();
			createdUser.setTravelAdvanceAccountAmount(travelAdvanceAccountAmount);
		}
		genericDao.saveOrUpdate(createdUser, user, entityManager);
		log.log(Level.FINE, "************* End ");
	}

	/**
	 * used both
	 * 
	 * @param claimTransaction
	 * @param user
	 * @param suppDoc
	 * @param txnRmarks
	 */
	private void saveAndUpdateSupportingDocAndRemarks(ClaimTransaction claimTransaction, Users user, String suppDoc,
			String txnRmarks) throws IDOSException {
		log.log(Level.FINE, "************* Start");
		claimTransaction.setSupportingDocuments(TRANSACTION_DAO.getAndDeleteSupportingDocument(
				claimTransaction.getSupportingDocuments(), user.getEmail(), suppDoc, user, entityManager));
		if (txnRmarks != null && !txnRmarks.equals("")) {
			if (claimTransaction.getTxnRemarks() != null) {
				claimTransaction
						.setTxnRemarks(claimTransaction.getTxnRemarks() + "," + user.getEmail() + "#" + txnRmarks);
			} else {
				claimTransaction.setTxnRemarks(user.getEmail() + "#" + txnRmarks);
			}
		}
		genericDao.saveOrUpdate(claimTransaction, user, entityManager);
		FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, suppDoc, claimTransaction.getId(),
				IdosConstants.CLAIM_TXN_TYPE);
		log.log(Level.FINE, "************* End ");
	}

	@Override
	public ObjectNode userClaimsTransactions(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) {
		log.log(Level.FINE, "************* Start");
		result.put("result", false);
		ArrayNode userClaimTxnDataAn = result.putArray("userClaimTxnData");
		StringBuilder sb = new StringBuilder();
		int limit = json.findValue("limit").asInt();
		sb.append("select obj from UsersRoles obj where obj.user='" + user.getId()
				+ "' and obj.role NOT IN(1,2,9) and obj.presentStatus=1 ORDER BY obj.role.id");
		List<UsersRoles> userRoles = genericDao.executeSimpleQuery(sb.toString(), entityManager);
		String roles = "";
		for (UsersRoles role : userRoles) {
			if (!role.getRole().getName().equals("OFFICERS")) {
				roles += role.getRole().getName() + ",";
			}
		}
		roles = roles.substring(0, roles.length() - 1);
		List<ClaimTransaction> userClaimTransactionList = null;
		StringBuilder sbquery = null;
		// if role is only of creator
		if (roles.equals("CREATOR")) {
			sbquery = new StringBuilder("select obj from ClaimTransaction obj WHERE obj.createdBy ='")
					.append(user.getId())
					.append("' and obj.transactionBranchOrganization='").append(user.getOrganization().getId())
					.append("' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.equals("APPROVER")) {
			sbquery = new StringBuilder("select obj from ClaimTransaction obj WHERE (obj.approverActionBy='")
					.append(user.getId())
					.append("' or LOCATE('").append(user.getEmail())
					.append("',obj.approverMails)>0 or obj.selectedAdditionalApprover='")
					.append(user.getEmail()).append("') and obj.transactionBranchOrganization='")
					.append(user.getOrganization().getId())
					.append("' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.equals("CREATOR,APPROVER")) {
			sbquery = new StringBuilder("select obj from ClaimTransaction obj WHERE (obj.createdBy ='")
					.append(user.getId())
					.append("' or obj.approverActionBy='").append(user.getId()).append("' or LOCATE('")
					.append(user.getEmail())
					.append("',obj.approverMails)>0 or obj.selectedAdditionalApprover='").append(user.getEmail())
					.append("') and obj.transactionBranchOrganization='")
					.append(user.getOrganization().getId())
					.append("' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.equals("CREATOR,ACCOUNTANT")) {
			sbquery = new StringBuilder("");
			sbquery.append("select obj from ClaimTransaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.equals("CREATOR,CASHIER")) {
			sbquery = new StringBuilder("");
			sbquery.append("select obj from ClaimTransaction obj WHERE obj.createdBy ='" + user.getId()
					+ "' and obj.transactionBranchOrganization='" + user.getOrganization().getId()
					+ "' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.equals("CREATOR,APPROVER,ACCOUNTANT")) {
			sbquery = new StringBuilder("");
			sbquery.append("select obj from ClaimTransaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.equals("CREATOR,APPROVER,CASHIER")) {
			sbquery = new StringBuilder("");
			sbquery.append("select obj from ClaimTransaction obj WHERE (obj.createdBy ='" + user.getId()
					+ "' or obj.approverActionBy='" + user.getId() + "' or LOCATE('" + user.getEmail()
					+ "',obj.approverMails)>0 or obj.selectedAdditionalApprover='" + user.getEmail()
					+ "') and obj.transactionBranchOrganization='" + user.getOrganization().getId()
					+ "' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.equals("CREATOR,ACCOUNTANT,CASHIER")) {
			sbquery = new StringBuilder(
					"select obj from ClaimTransaction obj WHERE obj.transactionBranchOrganization='")
					.append(user.getOrganization().getId())
					.append("' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.equals("CREATOR,APPROVER,ACCOUNTANT,CASHIER")) {
			sbquery = new StringBuilder(
					"select obj from ClaimTransaction obj WHERE obj.transactionBranchOrganization='")
					.append(user.getOrganization().getId())
					.append("' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.equals("APPROVER,ACCOUNTANT")) {
			sbquery = new StringBuilder(
					"select obj from ClaimTransaction obj WHERE obj.transactionBranchOrganization='")
					.append(user.getOrganization().getId())
					.append("' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.equals("APPROVER,CASHIER")) {
			sbquery = new StringBuilder("select obj from ClaimTransaction obj WHERE (obj.approverActionBy='")
					.append(user.getId()).append("' or LOCATE('")
					.append(user.getEmail()).append("',obj.approverMails)>0 or obj.selectedAdditionalApprover='")
					.append(user.getEmail())
					.append("') and obj.transactionBranchOrganization='").append(user.getOrganization().getId())
					.append("' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.equals("APPROVER,ACCOUNTANT,CASHIER")) {
			sbquery = new StringBuilder(
					"select obj from ClaimTransaction obj WHERE obj.transactionBranchOrganization='")
					.append(user.getOrganization().getId())
					.append("' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.contains("CONTROLLER")) {
			sbquery = new StringBuilder(
					"select obj from ClaimTransaction obj WHERE obj.transactionBranchOrganization='")
					.append(user.getOrganization().getId())
					.append("' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.contains("ACCOUNTANT")) {
			sbquery = new StringBuilder(
					"select obj from ClaimTransaction obj WHERE obj.transactionBranchOrganization='")
					.append(user.getOrganization().getId())
					.append("' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		} else if (roles.contains("AUDITOR") && !roles.contains("MASTER ADMIN")) {
			sbquery = new StringBuilder(
					"select obj from ClaimTransaction obj WHERE obj.transactionBranchOrganization='")
					.append(user.getOrganization().getId())
					.append("' and obj.transactionStatus = 'Accounted' and obj.presentStatus=1 ORDER BY obj.transactionDate desc");
		}
		userClaimTransactionList = genericDao.executeSimpleQueryWithLimit(sbquery.toString(), entityManager, limit);
		for (ClaimTransaction claimTransaction : userClaimTransactionList) {
			result.put("result", true);
			Long id = claimTransaction.getId() != null ? claimTransaction.getId() : null;
			Long txnPurposeId = claimTransaction.getTransactionPurpose() != null
					? claimTransaction.getTransactionPurpose().getId()
					: null;
			String branchName = claimTransaction.getTransactionBranch() != null
					? claimTransaction.getTransactionBranch().getName()
					: "";
			String projectName = claimTransaction.getTransactionProject() != null
					? claimTransaction.getTransactionProject().getName()
					: "";
			String txnQuestionName = claimTransaction.getTransactionPurpose() != null
					? claimTransaction.getTransactionPurpose().getTransactionPurpose()
					: "";
			String txnOrgnName = claimTransaction.getTransactionBranchOrganization() != null
					? claimTransaction.getTransactionBranchOrganization().getName()
					: "";
			String travelType = claimTransaction.getTravelType() != null ? claimTransaction.getTravelType() : "";
			String noOfPlacesToVisit = claimTransaction.getNumberOfPlacesToVisit() != null
					? claimTransaction.getNumberOfPlacesToVisit().toString()
					: "";
			String placesSelectedOrEntered = claimTransaction.getTravelFromToPlaces() != null
					? claimTransaction.getTravelFromToPlaces()
					: "";
			String typeOfCity = claimTransaction.getTypeOfCity() != null ? claimTransaction.getTypeOfCity() : "";
			String appropriateDiatance = claimTransaction.getAppropriateDistance() != null
					? claimTransaction.getAppropriateDistance()
					: "";
			String totalDays = claimTransaction.getTotalDays() != null ? claimTransaction.getTotalDays().toString()
					: "";
			String travelDetailedConfDescription = claimTransaction.getTravelEligibilityDetails() != null
					? claimTransaction.getTravelEligibilityDetails()
					: "";
			String existingClaimsCurrentSettlementDetails = claimTransaction
					.getExistingClaimsCurrentSettlementDetails() != null
							? claimTransaction.getExistingClaimsCurrentSettlementDetails()
							: "";
			String claimuserAdvanveEligibility = claimTransaction.getAdvanceEligibilityDetails() != null
					? claimTransaction.getAdvanceEligibilityDetails()
					: "";
			String userExpenditureOnThisTxn = claimTransaction.getUserExpenditureOnThisTxn() != null
					? claimTransaction.getUserExpenditureOnThisTxn()
					: "";
			Double existingAdvance = claimTransaction.getExistingAdvance() != null
					? claimTransaction.getExistingAdvance()
					: 0.0;
			Double adjustedAdvance = claimTransaction.getAdjustedAdvance() != null
					? claimTransaction.getAdjustedAdvance()
					: 0.0;
			Double netSettlementAmount = claimTransaction.getClaimsNetSettlement() != null
					? claimTransaction.getClaimsNetSettlement()
					: null;
			Double dueSettlementAmount = claimTransaction.getClaimsDueSettlement() != null
					? claimTransaction.getClaimsDueSettlement()
					: null;
			Double requiredSettlement = claimTransaction.getClaimsRequiredSettlement() != null
					? claimTransaction.getClaimsRequiredSettlement()
					: null;
			Double returnSettlement = claimTransaction.getClaimsReturnSettlement() != null
					? claimTransaction.getClaimsReturnSettlement()
					: null;
			Double enteredAdvance = claimTransaction.getGrossAmount() != null ? claimTransaction.getGrossAmount() : 0.0;
			Double totalAdvance = claimTransaction.getNewAmount() != null ? claimTransaction.getNewAmount() : 0.0;
			String purposeOfVisit = null;
			if (claimTransaction.getPurposeOfVisit() != null
					&& !claimTransaction.getPurposeOfVisit().contains("Please Select")) {
				purposeOfVisit = claimTransaction.getPurposeOfVisit();
			} else {
				purposeOfVisit = "";
			}
			String claimTxnRemarks = claimTransaction.getTxnRemarks() != null ? claimTransaction.getTxnRemarks() : "";
			String supportingDoc = claimTransaction.getSupportingDocuments() != null
					? claimTransaction.getSupportingDocuments()
					: "";
			String claimdebitCredit = claimTransaction.getDebitCredit() != null ? claimTransaction.getDebitCredit()
					: "";
			String claimTxnStatus = claimTransaction.getTransactionStatus() != null
					? claimTransaction.getTransactionStatus()
					: "";
			String claimapproverEmails = claimTransaction.getApproverMails() != null
					? claimTransaction.getApproverMails()
					: "";
			String additionalApprovalEmails = claimTransaction.getAdditionalApproverEmails() != null
					? claimTransaction.getAdditionalApproverEmails()
					: "";
			String claimselectedAdditionalApproval = claimTransaction.getSelectedAdditionalApprover() != null
					? claimTransaction.getSelectedAdditionalApprover()
					: "";
			String creatorLabel = "Created By:";
			String createdBy = claimTransaction.getCreatedBy() != null ? claimTransaction.getCreatedBy().getEmail()
					: "";
			String approverLabel = "Approved By:";
			String approvedBy = claimTransaction.getApproverActionBy() != null
					? claimTransaction.getApproverActionBy().getEmail()
					: "";
			String accountantEmails = claimTransaction.getAccountantEmails() != null
					? claimTransaction.getAccountantEmails()
					: "";
			String accountedLabel = "Accounted By:";
			String accountedBy = claimTransaction.getAccountingActionBy() != null
					? claimTransaction.getAccountingActionBy().getEmail()
					: "";
			String transactionDate = claimTransaction.getTransactionDate() != null
					? IdosConstants.IDOSDF.format(claimTransaction.getTransactionDate())
					: "";
			String claimTxnReferenceNo = claimTransaction.getTransactionRefNumber() != null
					? claimTransaction.getTransactionRefNumber()
					: "";
			String txnSpecialStatus = "";
			if (claimTransaction.getKlFollowStatus() != null) {
				if (claimTransaction.getKlFollowStatus() == 0) {
					txnSpecialStatus = "Rules Not Followed";
				}
			}
			String paymentMode = "";
			if (claimTransaction.getReceiptDetailType() != null) {
				if (claimTransaction.getReceiptDetailType() == 1) {
					paymentMode = "CASH";
				}
				if (claimTransaction.getReceiptDetailType() == 2) {
					paymentMode = "BANK";
				}
			}
			Double expenseAdvanceTotalAdvanceAmount = claimTransaction.getTotalAdvanceForExpenseDuringTxn() != null
					? claimTransaction.getTotalAdvanceForExpenseDuringTxn()
					: 0.0;
			String expenseAdvancepurposeOfExpenseAdvance = claimTransaction.getPurposeOfAdvance() != null
					? claimTransaction.getPurposeOfAdvance()
					: "";
			String itemName = claimTransaction.getAdvanceForExpenseItems() != null
					? claimTransaction.getAdvanceForExpenseItems().getName()
					: "";
			String itemParticularName = claimTransaction.getAdvanceForExpenseItemsParticulars() != null
					? claimTransaction.getAdvanceForExpenseItemsParticulars().getName()
					: "";
			String parentSpecificName = "";
			if (claimTransaction.getAdvanceForExpenseItems() != null) {
				if (claimTransaction.getAdvanceForExpenseItems().getParentSpecifics() != null) {
					parentSpecificName = claimTransaction.getAdvanceForExpenseItems().getParentSpecifics().getName();
				}
			}
			Double dueFromCompany = claimTransaction.getClaimsRequiredSettlement() != null
					? claimTransaction.getClaimsRequiredSettlement()
					: 0.0;
			Double dueToCompany = claimTransaction.getClaimsReturnSettlement() != null
					? claimTransaction.getClaimsReturnSettlement()
					: 0.0;
			Double amountReturnInCaseOfDueToCompany = claimTransaction.getAmountReturnInCaseOfDueToCompany() != null
					? claimTransaction.getAmountReturnInCaseOfDueToCompany()
					: 0.0;
			ObjectNode event = Json.newObject();
			event.put("userroles", roles);
			event.put("id", id);
			event.put("txnPurposeId", txnPurposeId);
			event.put("branchName", branchName);
			event.put("projectName", projectName);
			event.put("txnQuestionName", txnQuestionName);
			event.put("txnOrgnName", txnOrgnName);
			event.put("claimtravelType", travelType);
			event.put("claimnoOfPlacesToVisit", noOfPlacesToVisit);
			event.put("claimplacesSelectedOrEntered", placesSelectedOrEntered);
			event.put("claimtypeOfCity", typeOfCity);
			event.put("claimappropriateDiatance", appropriateDiatance);
			event.put("claimtotalDays", totalDays);
			event.put("claimtravelDetailedConfDescription", travelDetailedConfDescription);
			event.put("existingClaimsCurrentSettlementDetails", existingClaimsCurrentSettlementDetails);
			event.put("claimexistingAdvance", IdosConstants.decimalFormat.format(existingAdvance));
			event.put("claimuserAdvanveEligibility", claimuserAdvanveEligibility);
			event.put("userExpenditureOnThisTxn", userExpenditureOnThisTxn);
			event.put("claimadjustedAdvance", IdosConstants.decimalFormat.format(adjustedAdvance));
			event.put("claimenteredAdvance", IdosConstants.decimalFormat.format(enteredAdvance));
			event.put("claimtotalAdvance", IdosConstants.decimalFormat.format(totalAdvance));
			event.put("netSettlementAmount", IdosConstants.decimalFormat.format(netSettlementAmount));
			event.put("dueSettlementAmount", IdosConstants.decimalFormat.format(dueSettlementAmount));
			event.put("requiredSettlement", IdosConstants.decimalFormat.format(requiredSettlement));
			event.put("returnSettlement", IdosConstants.decimalFormat.format(returnSettlement));
			event.put("claimpurposeOfVisit", purposeOfVisit);
			event.put("claimtxnRemarks", claimTxnRemarks);
			event.put("claimsupportingDoc", supportingDoc);
			event.put("debitCredit", claimdebitCredit);
			event.put("claimTxnStatus", claimTxnStatus);
			event.put("approverEmails", claimapproverEmails);
			event.put("additionalApprovarUsers", additionalApprovalEmails);
			event.put("selectedAdditionalApproval", claimselectedAdditionalApproval);
			event.put("useremail", user.getEmail());
			event.put("creatorLabel", creatorLabel);
			event.put("createdBy", createdBy);
			event.put("transactionDate", transactionDate);
			event.put("approverLabel", approverLabel);
			event.put("approvedBy", approvedBy);
			event.put("accountantEmails", accountantEmails);
			event.put("accountedLabel", accountedLabel);
			event.put("accountedBy", accountedBy);
			event.put("txnSpecialStatus", txnSpecialStatus);
			event.put("paymentMode", paymentMode);
			event.put("instrumentNumber",
					claimTransaction.getInstrumentNumber() == null ? "" : claimTransaction.getInstrumentNumber());
			event.put("instrumentDate",
					claimTransaction.getInstrumentDate() == null ? "" : claimTransaction.getInstrumentDate());
			event.put("expenseAdvanceTotalAdvanceAmount",
					IdosConstants.decimalFormat.format(expenseAdvanceTotalAdvanceAmount));
			event.put("expenseAdvancepurposeOfExpenseAdvance", expenseAdvancepurposeOfExpenseAdvance);
			event.put("itemName", itemName);
			event.put("itemParticularName", itemParticularName);
			event.put("parentSpecificName", parentSpecificName);
			event.put("dueFromCompany", IdosConstants.decimalFormat.format(dueFromCompany));
			event.put("dueToCompany", IdosConstants.decimalFormat.format(dueToCompany));
			event.put("amountReturnInCaseOfDueToCompany",
					IdosConstants.decimalFormat.format(amountReturnInCaseOfDueToCompany));
			event.put("claimTxnReferenceNo", claimTxnReferenceNo);
			userClaimTxnDataAn.add(event);
		}
		if (roles.contains("CREATOR") || roles.contains("APPROVER") || roles.contains("AUDITOR")
				|| roles.contains("ACCOUNTANT")) {
			sbquery = new StringBuilder("");
			sbquery.append("select COUNT(obj) from ClaimTransaction obj WHERE obj.transactionBranchOrganization=")
					.append(user.getOrganization().getId());
			sbquery.append(
					" AND obj.presentStatus = 1 AND (obj.transactionStatus = 'Require Approval' OR obj.transactionStatus = 'Require Additional Approval')");
			sbquery.append(" AND (obj.approverActionBy='" + user.getId()).append("' or LOCATE('")
					.append(user.getEmail())
					.append("',obj.approverMails)>0 or obj.selectedAdditionalApprover='" + user.getEmail())
					.append("' or obj.createdBy = ").append(user.getId());
			sbquery.append(" OR (obj.accountingActionBy='" + user.getId()).append("' or LOCATE('")
					.append(user.getEmail()).append("',obj.accountantEmails)>0").append("))");
			List<Transaction> list = genericDao.executeSimpleQueryWithLimit(sbquery.toString(), entityManager, 1);
			Object row = null;
			if (list.size() > 0) {
				row = list.get(0);
				result.put("approval", "Require Approval : " + row);
			}
			sbquery = new StringBuilder("");
			sbquery.append("select COUNT(obj) from ClaimTransaction obj WHERE obj.transactionBranchOrganization=")
					.append(user.getOrganization().getId());
			sbquery.append(" AND obj.presentStatus = 1 AND obj.transactionStatus = 'Approved'");
			sbquery.append(" AND (obj.approverActionBy='" + user.getId()).append("' or LOCATE('")
					.append(user.getEmail())
					.append("',obj.approverMails)>0 or obj.selectedAdditionalApprover='" + user.getEmail())
					.append("' or obj.createdBy = ").append(user.getId());
			sbquery.append(" OR (obj.accountingActionBy='" + user.getId()).append("' or LOCATE('")
					.append(user.getEmail()).append("',obj.accountantEmails)>0").append("))");
			list = genericDao.executeSimpleQueryWithLimit(sbquery.toString(), entityManager, 1);
			if (list.size() > 0) {
				row = list.get(0);
				result.put("approved", "Complete Accounting : " + row);
			}
		} else {
			result.put("approval", "");
			result.put("approved", "");
		}
		log.log(Level.FINE, "************* End " + userClaimTxnDataAn.size());
		return result;
	}

	@Override
	public ObjectNode exitingClaimsAdvanceTxnRefAndAmount(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) {
		log.log(Level.FINE, "************* Start");
		result.put("result", false);
		ArrayNode existingClimsDataAn = result.putArray("existingClimsData");
		List<ClaimTransaction> userClaimTransactionList = null;
		StringBuilder newsbquery = new StringBuilder("");
		String advanceAdjustmentAmount = json.findValue("advanceAdjustmentAmount") != null
				? json.findValue("advanceAdjustmentAmount").asText()
				: null;
		Double advanceAdjustmentAmountDouble = Double.parseDouble(advanceAdjustmentAmount);
		newsbquery.append("select obj from ClaimTransaction obj where obj.createdBy='" + user.getId()
				+ "' and obj.presentStatus=1 and obj.claimsDueSettlement>=" + advanceAdjustmentAmountDouble
				+ " and obj.settlementStatus!='SETTLED' ORDER BY obj.createdAt desc");
		userClaimTransactionList = genericDao.executeSimpleQuery(newsbquery.toString(), entityManager);
		for (ClaimTransaction claimTransaction : userClaimTransactionList) {
			result.put("result", true);
			ObjectNode row = Json.newObject();
			row.put("id", claimTransaction.getId());
			row.put("refNumberAmount",
					claimTransaction.getTransactionRefNumber() + "(" + claimTransaction.getClaimsDueSettlement() + ")");
			existingClimsDataAn.add(row);
		}
		log.log(Level.FINE, "************* End " + result);
		return result;
	}

	@Override
	public void saveClaimBRSDate(Users user, EntityManager entityManager, EntityTransaction entitytransaction,
			String transactionRef, String brsBankDate) {
		Map<String, Object> criterias = new HashMap<String, Object>();
		criterias.clear();
		criterias.put("transactionRefNumber", transactionRef);
		criterias.put("presentStatus", 1);
		ClaimTransaction transaction = genericDao.getByCriteria(ClaimTransaction.class, criterias, entityManager);
		if (null != transaction && null != transaction.getId()) {
			transaction.setBrsBankDate(brsBankDate);
			genericDao.saveOrUpdate(transaction, user, entityManager);
		}
	}

	/**
	 * currently not getting used
	 * 
	 * @param result
	 * @param json
	 * @param user
	 * @param entityManager
	 * @param entitytransaction
	 * @return
	 */
	@Override
	public ObjectNode userAdvancesTxnApprovedButNotAccountedCount(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction) {
		log.log(Level.FINE, "************* Start");
		result.put("result", false);
		result.put("settlementresult", false);
		result.put("expenseItemAdvanceResult", false);
		result.put("expenseItemAdvanceSettlementResult", false);
		ArrayNode claimApprovedNotAccountedAn = result.putArray("claimApprovedNotAccounted");
		ArrayNode claimSettlementCountAn = result.putArray("claimSettlementCount");
		ArrayNode expenseItemAdvanceCountAn = result.putArray("expenseItemAdvanceCount");
		ArrayNode expenseItemAdvanceSettlementResultCountAn = result
				.putArray("expenseItemAdvanceSettlementResultCount");
		StringBuilder newsbquery = new StringBuilder("");
		newsbquery.append("select obj from ClaimTransaction obj where (LOCATE('" + user.getEmail()
				+ "',obj.approverMails)>0 or LOCATE('" + user.getEmail()
				+ "',obj.accountantEmails)>0) and obj.transactionPurpose.transactionPurpose='Request For Travel Advance' and obj.presentStatus=1 and (obj.transactionStatus='Approved' or obj.transactionStatus='Require Accounting')");
		List<ClaimTransaction> userAdvancesTxnApprovedButNotAccountedCountList = genericDao
				.executeSimpleQuery(newsbquery.toString(), entityManager);
		StringBuilder newsbquery1 = new StringBuilder("");
		newsbquery1.append("select obj from ClaimTransaction obj where (LOCATE('" + user.getEmail()
				+ "',obj.accountantEmails)>0) and obj.transactionPurpose.transactionPurpose='Settle Travel Advance' and obj.presentStatus=1 and (obj.transactionStatus='Payment Due To Staff' or obj.transactionStatus='Payment Due From Staff' or obj.transactionStatus='No Due For Settlement')");
		List<ClaimTransaction> userClaimSettlementCountList = genericDao.executeSimpleQuery(newsbquery1.toString(),
				entityManager);
		StringBuilder newsbquery2 = new StringBuilder("");
		newsbquery2.append("select obj from ClaimTransaction obj where (LOCATE('" + user.getEmail()
				+ "',obj.approverMails)>0 or LOCATE('" + user.getEmail()
				+ "',obj.accountantEmails)>0) and obj.presentStatus=1 and obj.transactionPurpose.transactionPurpose='Request Advance For Expense' and (obj.transactionStatus='Approved' or obj.transactionStatus='Require Accounting')");
		List<ClaimTransaction> expenseItemAdvanceCountList = genericDao.executeSimpleQuery(newsbquery2.toString(),
				entityManager);
		StringBuilder newsbquery3 = new StringBuilder("");
		newsbquery3.append("select obj from ClaimTransaction obj where (LOCATE('" + user.getEmail()
				+ "',obj.accountantEmails)>0) and obj.transactionPurpose.transactionPurpose='Settle Advance For Expense' and obj.presentStatus=1 and (obj.transactionStatus='Payment Due To Staff' or obj.transactionStatus='Payment Due From Staff' or obj.transactionStatus='No Due For Settlement')");
		List<ClaimTransaction> expenseItemAdvanceSettlementCountList = genericDao
				.executeSimpleQuery(newsbquery3.toString(), entityManager);
		int count = 0;
		if (!userAdvancesTxnApprovedButNotAccountedCountList.isEmpty()
				&& userAdvancesTxnApprovedButNotAccountedCountList.size() > 0) {
			count = userAdvancesTxnApprovedButNotAccountedCountList.size();
			ObjectNode row = Json.newObject();
			result.put("result", true);
			row.put("count", count);
			claimApprovedNotAccountedAn.add(row);
		}
		int settlementCount = 0;
		if (!userClaimSettlementCountList.isEmpty() && userClaimSettlementCountList.size() > 0) {
			settlementCount = userClaimSettlementCountList.size();
			ObjectNode row = Json.newObject();
			result.put("settlementresult", true);
			row.put("settlementCount", settlementCount);
			claimSettlementCountAn.add(row);
		}
		int expenseItemAdvanceCount = 0;
		if (!expenseItemAdvanceCountList.isEmpty() && expenseItemAdvanceCountList.size() > 0) {
			expenseItemAdvanceCount = expenseItemAdvanceCountList.size();
			ObjectNode row = Json.newObject();
			result.put("expenseItemAdvanceResult", true);
			row.put("expenseItemAdvanceCount", expenseItemAdvanceCount);
			expenseItemAdvanceCountAn.add(row);
		}
		int expenseItemAdvanceSettlementResultCount = 0;
		if (!expenseItemAdvanceSettlementCountList.isEmpty() && expenseItemAdvanceSettlementCountList.size() > 0) {
			expenseItemAdvanceSettlementResultCount = expenseItemAdvanceSettlementCountList.size();
			ObjectNode row = Json.newObject();
			result.put("expenseItemAdvanceSettlementResult", true);
			row.put("expenseItemAdvanceSettlementResultCount", expenseItemAdvanceSettlementResultCount);
			expenseItemAdvanceSettlementResultCountAn.add(row);
		}

		log.log(Level.FINE, "************* End " + result);
		return result;
	}
}
