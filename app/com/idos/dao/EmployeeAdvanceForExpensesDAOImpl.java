package com.idos.dao;

import actor.CreatorActor;
import com.idos.util.CodeHelper;
import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import controllers.Karvy.KarvyAuthorization;
import model.*;
import model.karvy.GSTFiling;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;
import service.*;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.*;
import javax.inject.Inject;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;

public class EmployeeAdvanceForExpensesDAOImpl implements EmployeeAdvanceForExpensesDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public ObjectNode employeeAdvanceForExpenseItems(ObjectNode result, JsonNode json, Users user, EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		result.put("result", false);
		ObjectNode row = null;
		ArrayNode an = result.putArray("expenseAdvanceItemsData");
		result.put("result", false);
		Map<String, Object> criterias = new HashMap<String, Object>(4);
		if (null != user) {
			ExpenseGroup expenseGroup = user.geteGroup();
			// String expGroupTxnPurposes= user.getUsersExpenseClaimTxnQuestions();
			List<ExpenseGroupExpenseItemMonetoryClaim> expenseGroupSpecifics = null;
			if (expenseGroup != null) {
				String query = "SELECT obj FROM ExpenseGroupExpenseItemMonetoryClaim obj WHERE obj.expenseGroup.id = "
						+ expenseGroup.getId();
				expenseGroupSpecifics = genericDao.executeSimpleQuery(query, em);
			}
			if (expenseGroupSpecifics != null && expenseGroupSpecifics.size() > 0) {
				result.put("result", true);
				for (ExpenseGroupExpenseItemMonetoryClaim expenseSpecific : expenseGroupSpecifics) {
					// String query = "SELECT obj FROM Specifics obj WHERE obj.organization.id = " +
					// user.getOrganization().getId() + " AND obj.employeeClaimItem = 1";
					/*
					 * List<Specifics> specifics = genericDao.executeSimpleQuery(query, em);
					 * if (!specifics.isEmpty() && specifics.size() > 0) {
					 * result.put("result", true);
					 * for (Specifics specific : specifics) {
					 */
					Specifics specific = expenseSpecific.getSpecificsItem();
					/*
					 * criterias.clear();
					 * criterias.put("user.id", user.getId());
					 * criterias.put("userRights.id", 1L);
					 * criterias.put("specifics.id", specific.getId());
					 * criterias.put("particulars.id", specific.getParticularsId().getId());
					 * UserRightSpecifics
					 * doesUserHasRightInSpecifics=genericDao.getByCriteria(UserRightSpecifics.
					 * class, criterias, em);
					 * if(doesUserHasRightInSpecifics!=null){
					 */
					if (null != specific) {
						row = Json.newObject();
						if (null != specific.getId()) {
							row.put("id", specific.getId());
						}
						if (null != specific.getName()) {
							row.put("itemName", specific.getName());
						}
						an.add(row);
					}
					// }
				}
			}
		}
		log.log(Level.FINE, "************* End " + result);
		return result;
	}

	@Override
	public ObjectNode displayUserEligibility(ObjectNode result, JsonNode json, Users user, EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		result.put("result", false);
		result.put("klcontentresult", false);
		ObjectNode row = null;
		ObjectNode specfrow = null;
		ArrayNode an = result.putArray("expenseAdvanceEligibilityData");
		ArrayNode klan = result.putArray("expenseAdvanceEligibilitySpecfKlData");
		ExpenseGroup usereGroup = user.geteGroup();
		Map<String, Object> criterias = new HashMap<String, Object>();
		if (usereGroup != null) {
			String specificsPrimKeyId = json.findValue("specificsPrimKeyId") != null
					? json.findValue("specificsPrimKeyId").asText()
					: null;
			if (specificsPrimKeyId != null && !specificsPrimKeyId.equals("")) {
				Specifics specf = Specifics.findById(Long.parseLong(specificsPrimKeyId));
				StringBuilder sbr = new StringBuilder("");
				sbr.append("select obj from ExpenseGroupExpenseItemMonetoryClaim obj where obj.organization='"
						+ user.getOrganization().getId() + "' and obj.specificsItem='" + specf.getId()
						+ "' and obj.specificsParticulars='" + specf.getParticularsId().getId()
						+ "' and obj.presentStatus=1 and obj.expenseGroup='" + usereGroup.getId() + "'");
				List<ExpenseGroupExpenseItemMonetoryClaim> eGroupExpenseMonetoryClaimList = genericDao
						.executeSimpleQuery(sbr.toString(), em);
				if (!eGroupExpenseMonetoryClaimList.isEmpty() && eGroupExpenseMonetoryClaimList.size() > 0) {
					result.put("result", true);
					ExpenseGroupExpenseItemMonetoryClaim eGroupExpenseMonetoryClaim = eGroupExpenseMonetoryClaimList
							.get(0);
					row = Json.newObject();
					Double maximumPermittedAdvance = eGroupExpenseMonetoryClaim.getMaximumPermittedAdvance();
					row.put("expenseAdvanceMaxPermittedAdvance",
							IdosConstants.decimalFormat.format(maximumPermittedAdvance));
					sbr.delete(0, sbr.length());
					sbr.append("select SUM(obj.newAmount) from ClaimTransaction obj where obj.createdBy='"
							+ user.getId() + "' and obj.advanceForExpenseItems='" + specf.getId()
							+ "' and obj.advanceForExpenseItemsParticulars='" + specf.getParticularsId().getId()
							+ "' and obj.transactionPurpose=17 and obj.settlementStatus!='SETTLED' and obj.presentStatus=1 and obj.transactionStatus='Accounted'");
					List<ClaimTransaction> expenseAdvanceRequestTakenList = genericDao
							.executeSimpleQuery(sbr.toString(), em);
					Double expenseAdvanceRequestTakenAmount = 0.0;
					if (expenseAdvanceRequestTakenList.size() > 0) {
						Object val = expenseAdvanceRequestTakenList.get(0);
						if (val != null) {
							expenseAdvanceRequestTakenAmount = Double.parseDouble(String.valueOf(val));
						}
					}
					sbr.delete(0, sbr.length());
					sbr.append("select SUM(obj.newAmount) from ClaimTransaction obj where obj.createdBy='"
							+ user.getId() + "' and obj.advanceForExpenseItems='" + specf.getId()
							+ "' and obj.advanceForExpenseItemsParticulars='" + specf.getParticularsId().getId()
							+ "' and obj.transactionPurpose=18 and obj.settlementStatus!='SETTLED' and obj.presentStatus=1 and obj.transactionStatus='Accounted'");
					List<ClaimTransaction> expenseAdvanceRequestSettlementList = genericDao
							.executeSimpleQuery(sbr.toString(), em);
					Double expenseAdvanceRequestSettlementAmount = 0.0;
					if (expenseAdvanceRequestSettlementList.size() > 0) {
						Object val = expenseAdvanceRequestSettlementList.get(0);
						if (val != null) {
							expenseAdvanceRequestSettlementAmount = Double.parseDouble(String.valueOf(val));
						}
					}
					Double expenseAdvanceResult = expenseAdvanceRequestTakenAmount
							- expenseAdvanceRequestSettlementAmount;
					row.put("expenseAdvanseUnsettledExistingAdvances",
							IdosConstants.decimalFormat.format(expenseAdvanceResult));
					sbr.delete(0, sbr.length());
					sbr.append("select SUM(obj.newAmount) from ClaimTransaction obj where obj.createdBy='"
							+ user.getId() + "' and obj.advanceForExpenseItems='" + specf.getId()
							+ "' and obj.advanceForExpenseItemsParticulars='" + specf.getParticularsId().getId()
							+ "' and obj.transactionPurpose=17 and obj.settlementStatus!='SETTLED' and obj.presentStatus=1 and obj.transactionStatus!='Accounted'");
					List<ClaimTransaction> expenseAdvanceRequestInProgressList = genericDao
							.executeSimpleQuery(sbr.toString(), em);
					Double expenseAdvanceRequestInProgress = 0.0;
					if (expenseAdvanceRequestInProgressList.size() > 0) {
						Object val = expenseAdvanceRequestInProgressList.get(0);
						if (val != null) {
							expenseAdvanceRequestInProgress = Double.parseDouble(String.valueOf(val));
						}
					}
					row.put("expenseAdvanceRequestInProgress",
							IdosConstants.decimalFormat.format(expenseAdvanceRequestInProgress));
					an.add(row);
				}
				criterias.clear();
				criterias.put("specifics.id", specf.getId());
				criterias.put("particulars.id", specf.getParticularsId().getId());
				criterias.put("presentStatus", 1);
				List<SpecificsKnowledgeLibrary> specfKlList = genericDao.findByCriteria(SpecificsKnowledgeLibrary.class,
						criterias, em);
				if (!specfKlList.isEmpty() && specfKlList.size() > 0) {
					String selBranch = json.findValue("selBranch") != null ? json.findValue("selBranch").asText()
							: null;
					if (selBranch != null && !selBranch.equals("")) {
						Branch bnch = Branch.findById(Long.parseLong(selBranch));
						for (SpecificsKnowledgeLibrary specfKl : specfKlList) {
							criterias.clear();
							criterias.put("branch.id", bnch.getId());
							criterias.put("organization.id", bnch.getOrganization().getId());
							criterias.put("specifics.id", specf.getId());
							criterias.put("particulars.id", specf.getParticularsId().getId());
							criterias.put("specificsKl.id", specfKl.getId());
							criterias.put("presentStatus", 1);
							SpecificsKnowledgeLibraryForBranch specfKlBnch = genericDao
									.getByCriteria(SpecificsKnowledgeLibraryForBranch.class, criterias, em);
							if (specfKlBnch != null) {
								result.put("klcontentresult", true);
								specfrow = Json.newObject();
								specfrow.put("klContent", specfKlBnch.getSpecificsKl().getKnowledgeLibraryContent());
								specfrow.put("klIsMandatory", specfKlBnch.getSpecificsKl().getIsMandatory());
								klan.add(specfrow);
							}
						}
					}
				}
			}
		}
		log.log(Level.FINE, "************* End " + result);
		return result;
	}

	public ObjectNode showExpenseClaimDetails(ObjectNode result, JsonNode json, Users user, EntityManager em) {
		log.log(Level.FINE, ">>>> Start");
		ClaimTransaction txn = null;
		try {
			ArrayNode txndetailan = result.putArray("transactiondetailsData");
			// ArrayNode txnItemsan = result.putArray("transactionItemdetailsData");

			String transactionEntityId = json.findValue("transactionEntityId").asText();
			txn = ClaimTransaction.findById(Long.parseLong(transactionEntityId));
			if (txn != null) {
				ObjectNode row = Json.newObject();
				row.put("id", txn.getId());
				row.put("transactionPurpose", txn.getTransactionPurpose().getTransactionPurpose());
				row.put("transactionPurposeVal", txn.getTransactionPurpose().getId());
				row.put("grossAmount", txn.getGrossAmount());
				row.put("transactionDate", IdosConstants.IDOSDF.format(txn.getTransactionDate()));
				if (txn.getTransactionBranch() != null) {
					row.put("branchName", txn.getTransactionBranch().getName());
					row.put("branchId", txn.getTransactionBranch().getId());
				} else {
					row.put("branchName", "");
					row.put("branchId", "");
				}
				if (txn.getTransactionProject() != null) {
					row.put("projectName", txn.getTransactionProject().getName());
				} else {
					row.put("projectName", "");
				}
				if (txn.getTransactionProject() != null) {
					row.put("projectID", txn.getTransactionProject().getId());
				} else {
					row.put("projectID", "");
				}
				row.put("createdBy", txn.getCreatedBy().getEmail());
				if (txn.getAdvanceForExpenseItems() != null) {
					row.put("itemName", txn.getAdvanceForExpenseItems().getName());
					row.put("itemId", txn.getAdvanceForExpenseItems().getId());
				} else {
					row.put("itemName", "");
					row.put("itemId", "");
				}
				row.put("grossAmount", txn.getGrossAmount());

				//
				if (txn.getTransactionPurpose().getId() == 16) {
					ArrayNode expenseDetailsArray = row.putArray("expenseDetailsArray"); // IdosConstants.TRAVEL_EXPENSES
					ArrayNode lodgingAndBoardDetailsArray = row.putArray("lodgingAndBoardDetailsArray"); // IdosConstants.BOARDING_LODGING
					ArrayNode otherExpensesDetailsArray = row.putArray("otherExpensesDetailsArray"); // IdosConstants.OTHER_EXPENSES
					ArrayNode fixedPerDiamDetailsArray = row.putArray("fixedPerDiamDetailsArray"); // IdosConstants.FIXED_PER_DIAM

					expenseDetailsArray = CLAIM_DETAILS_DAO.getClaimDetails(expenseDetailsArray, json, user, em, txn,
							IdosConstants.TRAVEL_EXPENSES);
					lodgingAndBoardDetailsArray = CLAIM_DETAILS_DAO.getClaimDetails(lodgingAndBoardDetailsArray, json,
							user, em, txn, IdosConstants.BOARDING_LODGING);
					otherExpensesDetailsArray = CLAIM_DETAILS_DAO.getClaimDetails(otherExpensesDetailsArray, json, user,
							em, txn, IdosConstants.OTHER_EXPENSES);
					fixedPerDiamDetailsArray = CLAIM_DETAILS_DAO.getClaimDetails(fixedPerDiamDetailsArray, json, user,
							em, txn, IdosConstants.FIXED_PER_DIAM);

				} else if (txn.getTransactionPurpose().getId() == 18) {
					ArrayNode incurredExpensesDetailssArray = row.putArray("incurredExpensesDetailssArray"); // IdosConstants.INCURRED_EXPENCES
					incurredExpensesDetailssArray = CLAIM_DETAILS_DAO.getClaimDetails(incurredExpensesDetailssArray,
							json, user, em, txn, IdosConstants.INCURRED_EXPENCES);
					row.put("claimNetAmt", txn.getGrossAmount());
					row.put("claimNetTax", txn.getClaimsNetTax());
				} else if (txn.getTransactionPurpose().getId() == 19) {
					ArrayNode reiEmbExpensesDetailsArray = row.putArray("reiEmbExpensesDetailsArray"); // IdosConstants.REIMBURSEMENT_EXPENSES
					reiEmbExpensesDetailsArray = CLAIM_DETAILS_DAO.getClaimDetails(reiEmbExpensesDetailsArray, json,
							user, em, txn, IdosConstants.REIMBURSEMENT_EXPENSES);
					row.put("claimNetAmt", txn.getGrossAmount());
					row.put("claimNetTax", txn.getClaimsNetTax());
					row.put("expenseReimbursementAmountRequired", (txn.getGrossAmount() + txn.getClaimsNetTax()));
				}
				//
				if ((txn.getTransactionPurpose().getId() == 18 || txn.getTransactionPurpose().getId() == 16)
						&& txn.getClaimsSettlementRefNumber() != null && txn.getClaimsSettlementRefNumber() != "") {
					row.put("claimSettlementRefNo", txn.getClaimsSettlementRefNumber());
					StringBuilder sbuffer = new StringBuilder("");
					sbuffer.append("select obj from ClaimTransaction obj where obj.transactionRefNumber='"
							+ txn.getClaimsSettlementRefNumber() + "' and obj.presentStatus=1");
					List<ClaimTransaction> settlementInProgress = genericDao.executeSimpleQuery(sbuffer.toString(), em);
					if (settlementInProgress != null && settlementInProgress.size() > 0) {
						ClaimTransaction previousClaimTxn = settlementInProgress.get(0);
						row.put("claimSettlementId", previousClaimTxn.getId());
					}
					if (txn.getAmountReturnInCaseOfDueToCompany() != null) {
						row.put("amtReturnInCaseOfDueToCompany", txn.getAmountReturnInCaseOfDueToCompany());
					}
					if (txn.getClaimsRequiredSettlement() != null) {
						row.put("amtDueFromCompany", txn.getClaimsRequiredSettlement());
					}
					if (txn.getClaimsReturnSettlement() != null) {
						row.put("amtDueToCompany", txn.getClaimsReturnSettlement());
					}
					if (txn.getTransactionPurpose().getId() == 16) {
						Specifics travelSpecifics = Specifics.findByOrganizationAndMappingID(entityManager,
								user.getOrganization(), IdosConstants.TRAVEL_EXPENSES_MAPPING_ID);
						Specifics boardingAndLodgingSpecifics = Specifics.findByOrganizationAndMappingID(entityManager,
								user.getOrganization(), IdosConstants.BOARDING_LODGING_MAPPING_ID);
						Specifics otherExpSpecifics = Specifics.findByOrganizationAndMappingID(entityManager,
								user.getOrganization(), IdosConstants.OTHER_EXPENSES_MAPPING_ID);
						Specifics fixePerDiemSpecifics = Specifics.findByOrganizationAndMappingID(entityManager,
								user.getOrganization(), IdosConstants.FIXED_PER_DIAM_MAPPING_ID);
						sbuffer = new StringBuilder("select obj from ClaimsSettlement obj where obj.transaction='"
								+ txn.getId() + "' and obj.presentStatus=1");
						List<ClaimsSettlement> travelExpenses = genericDao.executeSimpleQuery(sbuffer.toString(), em);
						for (ClaimsSettlement travelExp : travelExpenses) {
							if (travelSpecifics != null
									&& travelSpecifics.getId() == travelExp.getTransactionSpecifics().getId()) {
								row.put("expenseIncurredTravel", travelExp.getItemValue());
								row.put("expenseIncurredTravelItemAmount", travelExp.getItemGross());
								row.put("expenseIncurredTravelItemTax", travelExp.getItemTax());
							} else if (boardingAndLodgingSpecifics != null && boardingAndLodgingSpecifics
									.getId() == travelExp.getTransactionSpecifics().getId()) {
								row.put("expenseIncurredBnL", travelExp.getItemValue());
								row.put("expenseIncurredBnLItemAmount", travelExp.getItemGross());
								row.put("expenseIncurredBnLItemTax", travelExp.getItemTax());
							} else if (otherExpSpecifics != null
									&& otherExpSpecifics.getId() == travelExp.getTransactionSpecifics().getId()) {
								row.put("expenseIncurredOtherExpenses", travelExp.getItemValue());
								row.put("expenseIncurredOtherExpensesItemAmount", travelExp.getItemGross());
								row.put("expenseIncurredOtherExpensesItemTax", travelExp.getItemTax());
							} else if (fixePerDiemSpecifics != null
									&& fixePerDiemSpecifics.getId() == travelExp.getTransactionSpecifics().getId()) {
								row.put("expenseIncurredFixedPerDiam", travelExp.getItemValue());
								row.put("expenseIncurredFixedPerDiamItemAmount", travelExp.getItemGross());
								row.put("expenseIncurredFixedPerDiamItemTax", travelExp.getItemTax());
							}
						}
						if (txn.getClaimsDueSettlement() != null) {
							row.put("updatedUnsettledAmount", txn.getClaimsReturnSettlement());
						}
						if (txn.getClaimsDueSettlement() != null) {
							row.put("balanceUnsettledAgainstThisTxn", txn.getClaimsDueSettlement());
						}
						row.put("balanceAmount", (txn.getClaimsDueSettlement() - txn.getGrossAmount()));

					}
				}
				if (txn.getTransactionPurpose().getId() == 15) {
					String travelType = txn.getTravelType() != null ? txn.getTravelType() : "";
					int travelTypeVal = 2;
					if (travelType.equalsIgnoreCase("Domestic")) {
						travelTypeVal = 1;
					}
					String noOfPlacesToVisit = txn.getNumberOfPlacesToVisit() != null
							? txn.getNumberOfPlacesToVisit().toString()
							: "";
					String placesSelectedOrEntered = txn.getTravelFromToPlaces() != null ? txn.getTravelFromToPlaces()
							: "";
					String[] placesToVisit = placesSelectedOrEntered.split(",");
					String typeOfCity = txn.getTypeOfCity() != null ? txn.getTypeOfCity() : "";
					String appropriateDiatance = txn.getAppropriateDistance() != null ? txn.getAppropriateDistance()
							: "";
					String totalDays = txn.getTotalDays() != null ? txn.getTotalDays().toString() : "";
					row.put("travelType", travelType);
					row.put("travelTypeVal", travelTypeVal);
					row.put("noOfPlacesToVisit", noOfPlacesToVisit);
					row.put("fromPlace", placesToVisit[0]);
					row.put("toPlace", placesToVisit[1]);
					row.put("typeOfCity", typeOfCity);
					int typeOfCityVal = getTypeOfCityValue(typeOfCity);
					row.put("typeOfCityVal", typeOfCityVal);
					int disVal = getAppropriateDistanceValue(appropriateDiatance);
					row.put("appropriateDiatance", appropriateDiatance);
					row.put("appropriateDistanceVal", disVal);
					row.put("totalDays", totalDays);
				}
				txndetailan.add(row);
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			ex.printStackTrace();
		}
		log.log(Level.FINE, ">>>> End " + result);
		return result;
	}

	private int getTypeOfCityValue(String typeOfCity) {
		int typeOfCityVal;
		switch (typeOfCity) {
			case "Country capital":
				typeOfCityVal = 1;
				break;
			case "State Capital":
				typeOfCityVal = 2;
				break;
			case "Metro city":
				typeOfCityVal = 3;
				break;
			case "City (other than Metro, Country or State capital)":
				typeOfCityVal = 4;
				break;
			case "Town":
				typeOfCityVal = 5;
				break;
			case "County":
				typeOfCityVal = 6;
				break;
			case "Municipality":
				typeOfCityVal = 7;
				break;
			case "Village":
				typeOfCityVal = 8;
				break;
			case "Remote location":
				typeOfCityVal = 9;
				break;
			case "More than 20 miles from closest city or town":
				typeOfCityVal = 10;
				break;
			case "Hill station":
				typeOfCityVal = 11;
				break;
			case "Resort":
				typeOfCityVal = 12;
				break;
			case "Place of conflict / war zone":
				typeOfCityVal = 13;
				break;
			default:
				typeOfCityVal = 1;
				break;
		}
		return typeOfCityVal;
	}

	private int getAppropriateDistanceValue(String appropriateDiatance) {
		int disVal;
		switch (appropriateDiatance) {
			case "Less than 100":
				disVal = 1;
				break;
			case "100 to 250":
				disVal = 2;
				break;
			case "250 to 500":
				disVal = 3;
				break;
			case "500 to 1000":
				disVal = 4;
				break;
			case "1000 to 1500":
				disVal = 5;
				break;
			case "1500 to 2000":
				disVal = 6;
				break;
			case "2000 to 2500":
				disVal = 7;
				break;
			case "2500 to 3000":
				disVal = 8;
				break;
			case "3000 to 4000":
				disVal = 9;
				break;
			case "4000 to 5000":
				disVal = 10;
				break;
			case "5000 to 6000":
				disVal = 11;
				break;
			case "6000 to 7000":
				disVal = 12;
				break;
			case "Above 7000":
				disVal = 13;
				break;
			default:
				disVal = 1;
				break;
		}
		return disVal;
	}

	@Override
	public ObjectNode submitForApproval(ObjectNode result, JsonNode json, Users user, EntityManager em,
			EntityTransaction entitytransaction) throws IDOSException {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		String claimTxnPurposeText = json.findValue("claimTxnPurposeText") != null
				? json.findValue("claimTxnPurposeText").asText()
				: null;
		Long claimTxnPurposeVal = json.findValue("claimTxnPurposeVal") != null
				? json.findValue("claimTxnPurposeVal").asLong()
				: null;
		Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
				: json.findValue("txnEntityID").asLong();
		Map<String, Object> criterias = new HashMap<String, Object>();
		if (claimTxnPurposeText.equals("Request Advance For Expense")) {
			String advanceForExpTxnBnch = json.findValue("advanceForExpTxnBnch") != null
					? json.findValue("advanceForExpTxnBnch").asText()
					: null;
			String advanceForExpTxnPjct = json.findValue("advanceForExpTxnPjct") != null
					? json.findValue("advanceForExpTxnPjct").asText()
					: null;
			Long advanceForExpTxnItemSpecf = json.findValue("advanceForExpTxnItemSpecf") != null
					? json.findValue("advanceForExpTxnItemSpecf").asLong()
					: null;
			String expenseAdvanceConfDetailsStr = json.findValue("expenseAdvanceConfDetailsStr") != null
					? json.findValue("expenseAdvanceConfDetailsStr").asText()
					: null;
			String expenseAdvanceRequiredAmount = json.findValue("expenseAdvanceRequiredAmount") != null
					? json.findValue("expenseAdvanceRequiredAmount").asText()
					: null;
			String expenseAdvanceTotalAdvanceAmount = json.findValue("expenseAdvanceTotalAdvanceAmount") != null
					? json.findValue("expenseAdvanceTotalAdvanceAmount").asText()
					: null;
			String expenseAdvanceKlContents = json.findValue("expenseAdvanceKlContents") != null
					? json.findValue("expenseAdvanceKlContents").asText()
					: null;
			String expenseAdvanceklmandatoryfollowednotfollowed = json
					.findValue("expenseAdvanceklmandatoryfollowednotfollowed") != null
							? json.findValue("expenseAdvanceklmandatoryfollowednotfollowed").asText()
							: null;
			String expenseAdvancepurposeOfExpenseAdvance = json
					.findValue("expenseAdvancepurposeOfExpenseAdvance") != null
							? json.findValue("expenseAdvancepurposeOfExpenseAdvance").asText()
							: null;
			String expenseAdvancetxnRemarks = json.findValue("expenseAdvancetxnRemarks") != null
					? json.findValue("expenseAdvancetxnRemarks").asText()
					: null;
			String expenseAdvanceSupportingDocuments = json.findValue("expenseAdvanceSupportingDocuments") != null
					? json.findValue("expenseAdvanceSupportingDocuments").asText()
					: null;
			Branch bnch = null;
			Project pjct = null;
			Specifics specifics = null;
			Particulars particulars = null;
			String txnRemarks = null;
			String txnDocument = "";
			String debitCredit = "Debit";
			TransactionPurpose txnPurpose = null;
			ClaimTransaction claimTxn = null;
			entitytransaction.begin();
			if (txnEntityID > 0) {
				claimTxn = ClaimTransaction.findById(txnEntityID);
			} else {
				claimTxn = new ClaimTransaction();
			}
			claimTxn.setTransactionDate(Calendar.getInstance().getTime());
			txnPurpose = TransactionPurpose.findById(claimTxnPurposeVal);
			if (txnPurpose == null) {
				throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
						IdosConstants.NULL_KEY_EXC_ESMF_MSG, "cannot identify claim transaction or not found");
			}
			claimTxn.setTransactionPurpose(txnPurpose);
			if (advanceForExpTxnBnch != null && !advanceForExpTxnBnch.equals("")) {
				bnch = Branch.findById(Long.parseLong(advanceForExpTxnBnch));
				claimTxn.setTransactionBranch(bnch);
				claimTxn.setTransactionBranchOrganization(bnch.getOrganization());
			}
			if (advanceForExpTxnPjct != null && !advanceForExpTxnPjct.equals("")) {
				pjct = Project.findById(Long.parseLong(advanceForExpTxnPjct));
				claimTxn.setTransactionProject(pjct);
			}
			if (advanceForExpTxnItemSpecf != null && !advanceForExpTxnItemSpecf.equals("")) {
				specifics = Specifics.findById(advanceForExpTxnItemSpecf);
				claimTxn.setAdvanceForExpenseItems(specifics);
			}
			if (specifics != null) {
				particulars = specifics.getParticularsId();
				claimTxn.setAdvanceForExpenseItemsParticulars(particulars);
			} else {
				throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
						IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Expense item is not valid");
			}
			if (expenseAdvanceConfDetailsStr != null && !expenseAdvanceConfDetailsStr.equals("")) {
				claimTxn.setExpenseItemAdvanceEligibilityCurrentSettlementDetails(expenseAdvanceConfDetailsStr);
				claimTxn.setAdvanceEligibilityDetails(expenseAdvanceConfDetailsStr);
			}
			if (expenseAdvanceRequiredAmount != null && !expenseAdvanceRequiredAmount.equals("")) {
				claimTxn.setGrossAmount(Double.parseDouble(expenseAdvanceRequiredAmount));
				claimTxn.setNewAmount(Double.parseDouble(expenseAdvanceRequiredAmount));
				claimTxn.setClaimsDueSettlement(Double.parseDouble(expenseAdvanceRequiredAmount));
				claimTxn.setClaimsNetSettlement(Double.parseDouble(expenseAdvanceRequiredAmount));
			}
			if (expenseAdvanceTotalAdvanceAmount != null && !expenseAdvanceTotalAdvanceAmount.equals("")) {
				claimTxn.setTotalAdvanceForExpenseDuringTxn(Double.parseDouble(expenseAdvanceTotalAdvanceAmount));
			}
			if (expenseAdvanceklmandatoryfollowednotfollowed != null
					&& !expenseAdvanceklmandatoryfollowednotfollowed.equals("")) {
				claimTxn.setKlFollowStatus(Integer.parseInt(expenseAdvanceklmandatoryfollowednotfollowed));
			}
			if (expenseAdvanceKlContents != null && !expenseAdvanceKlContents.equals("")) {
				claimTxn.setPresentTxnRules(expenseAdvanceKlContents);
			}
			if (expenseAdvancepurposeOfExpenseAdvance != null && !expenseAdvancepurposeOfExpenseAdvance.equals("")) {
				claimTxn.setPurposeOfAdvance(expenseAdvancepurposeOfExpenseAdvance);
			}
			if (expenseAdvancetxnRemarks != null && !expenseAdvancetxnRemarks.equals("")) {
				if (claimTxn.getTxnRemarks() != null) {
					txnRemarks = claimTxn.getTxnRemarks() + "," + user.getEmail() + "#" + expenseAdvancetxnRemarks;
					claimTxn.setTxnRemarks(txnRemarks);
				} else {
					txnRemarks = user.getEmail() + "#" + expenseAdvancetxnRemarks;
					claimTxn.setTxnRemarks(txnRemarks);
				}
			}
			try {
				claimTxn.setSupportingDocuments(
						TRANSACTION_DAO.getAndDeleteSupportingDocument(claimTxn.getSupportingDocuments(),
								user.getEmail(), expenseAdvanceSupportingDocuments, user, em));
			} catch (IDOSException e) {
				log.log(Level.SEVERE, "Error", e);
			}
			claimTxn.setSettlementStatus("NOT-SETTLED");
			claimTxn.setTransactionStatus("Require Approval");
			criterias.clear();
			criterias.put("role.name", "APPROVER");
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("presentStatus", 1);
			List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);
			String approverEmails = "";
			String additionalApprovarUsers = "";
			String selectedAdditionalApproval = "";
			for (UsersRoles usrRoles : approverRole) {
				additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
				criterias.clear();
				criterias.put("user.id", usrRoles.getUser().getId());
				criterias.put("userRights.id", 2L);
				criterias.put("branch.id", bnch.getId());
				criterias.put("presentStatus", 1);
				UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class, criterias,
						em);
				if (userHasRightInBranch != null) {
					criterias.clear();
					criterias.put("user.id", usrRoles.getUser().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("specifics.id", specifics.getId());
					criterias.put("presentStatus", 1);
					UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class, criterias,
							em);
					if (userHasRightInCOA != null) {
						approverEmails += usrRoles.getUser().getEmail() + ",";
					}
				}
			}
			String accountantEmailsStr = "";
			criterias.clear();
			criterias.put("role.id", 5l);
			criterias.put("organization.id", user.getOrganization().getId());
			List<UsersRoles> accountantRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);
			for (UsersRoles usrRoles : accountantRole) {
				accountantEmailsStr += usrRoles.getUser().getEmail() + ",";
			}
			claimTxn.setDebitCredit(debitCredit);
			claimTxn.setApproverMails(approverEmails);
			claimTxn.setAccountantEmails(accountantEmailsStr);
			claimTxn.setAdditionalApproverEmails(additionalApprovarUsers);
			String transactionNumber = CodeHelper.getForeverUniqueID("CLAIMTXN", null);
			claimTxn.setTransactionRefNumber(transactionNumber);
			genericDao.saveOrUpdate(claimTxn, user, em);
			try {
				FILE_UPLOAD_SERVICE.updateUploadFileLogs(em, user, expenseAdvanceSupportingDocuments, claimTxn.getId(),
						IdosConstants.CLAIM_TXN_TYPE);
			} catch (IDOSException e) {
				log.log(Level.SEVERE, "Error", e);
			}
			entitytransaction.commit();
			result.put("transactionPrimId", claimTxn.getId());
			result.put("transactionRefNo", claimTxn.getTransactionRefNumber());
			sendSocketResponeToClient(claimTxn, user, result);
		} else if (claimTxnPurposeText.equals("Settle Advance For Expense")) {
			try {
				entitytransaction.begin();
				String availableUserUnsettledExpenseAdvances = json
						.findValue("availableUserUnsettledExpenseAdvances") != null
								? json.findValue("availableUserUnsettledExpenseAdvances").asText()
								: null;
				String unsettledUserExpenseAdvancesDetails = json
						.findValue("unsettledUserExpenseAdvancesDetails") != null
								? json.findValue("unsettledUserExpenseAdvancesDetails").asText()
								: null;
				String item1ExpIncurredOnThisTxnAmount = json.findValue("item1ExpIncurredOnThisTxnAmount") != null
						? json.findValue("item1ExpIncurredOnThisTxnAmount").asText()
						: null;

				String amtDueFromCompany = json.findValue("amtDueFromCompany") != null
						? json.findValue("amtDueFromCompany").asText()
						: null;
				String amtDueToCompany = json.findValue("amtDueToCompany") != null
						? json.findValue("amtDueToCompany").asText()
						: null;
				String amtReturnInCaseOfDueToCompany = json.findValue("amtReturnInCaseOfDueToCompany") != null
						? json.findValue("amtReturnInCaseOfDueToCompany").asText()
						: null;
				String amtUpdatedUnsettledAmount = json.findValue("amtUpdatedUnsettledAmount") != null
						? json.findValue("amtUpdatedUnsettledAmount").asText()
						: null;
				String amtTotalExpensesIncurredOnThisTxn = json.findValue("amtTotalExpensesIncurredOnThisTxn") != null
						? json.findValue("amtTotalExpensesIncurredOnThisTxn").asText()
						: null;
				String expenseAdvancetxnRemarks = json.findValue("expenseAdvancetxnRemarks") != null
						? json.findValue("expenseAdvancetxnRemarks").asText()
						: null;
				String expenseAdvanceSupportingDocuments = json.findValue("expenseAdvanceSupportingDocuments") != null
						? json.findValue("expenseAdvanceSupportingDocuments").asText()
						: null;
				String incurredExpensesDetails = json.findValue("incurredExpensesDetails").toString();
				ClaimTransaction previousClaimTxn = null;
				String txnRemarks = null;
				String txnDocument = "";
				String debitCredit = "";
				TransactionPurpose txnPurpose = null;
				ClaimTransaction newClaimTransaction = null;
				String travelExpenceTotalAmt = json.findValue("travelExpenceTotalAmt") != null
						? json.findValue("travelExpenceTotalAmt").asText()
						: null;
				String travelExpenceTotalTax = json.findValue("travelExpenceTotalTax") != null
						? json.findValue("travelExpenceTotalTax").asText()
						: null;
				if (txnEntityID > 0) {
					newClaimTransaction = ClaimTransaction.findById(txnEntityID);
				} else {
					newClaimTransaction = new ClaimTransaction();
				}
				newClaimTransaction.setTransactionDate(Calendar.getInstance().getTime());
				if (availableUserUnsettledExpenseAdvances != null
						&& !availableUserUnsettledExpenseAdvances.equals("")) {
					previousClaimTxn = ClaimTransaction.findById(Long.parseLong(availableUserUnsettledExpenseAdvances));
					newClaimTransaction.setClaimsSettlementRefNumber(previousClaimTxn.getTransactionRefNumber());
				}
				if (claimTxnPurposeVal != null && !claimTxnPurposeVal.equals("")) {
					txnPurpose = TransactionPurpose.findById(claimTxnPurposeVal);
				}
				newClaimTransaction.setTransactionPurpose(txnPurpose);
				newClaimTransaction.setTransactionBranch(previousClaimTxn.getTransactionBranch());
				newClaimTransaction
						.setTransactionBranchOrganization(previousClaimTxn.getTransactionBranchOrganization());
				newClaimTransaction.setTransactionProject(previousClaimTxn.getTransactionProject());
				newClaimTransaction.setAdvanceForExpenseItems(previousClaimTxn.getAdvanceForExpenseItems());
				newClaimTransaction
						.setAdvanceForExpenseItemsParticulars(previousClaimTxn.getAdvanceForExpenseItemsParticulars());
				newClaimTransaction.setExistingClaimsCurrentSettlementDetails(unsettledUserExpenseAdvancesDetails);
				newClaimTransaction.setAdvanceEligibilityDetails(unsettledUserExpenseAdvancesDetails);
				newClaimTransaction.setPurposeOfAdvance(previousClaimTxn.getPurposeOfAdvance());
				if (item1ExpIncurredOnThisTxnAmount != null) {
					newClaimTransaction.setClaimsNetSettlement(Double.parseDouble(item1ExpIncurredOnThisTxnAmount));
					newClaimTransaction.setNewAmount(Double.parseDouble(item1ExpIncurredOnThisTxnAmount));
					newClaimTransaction.setNewAmount(Double.parseDouble(amtTotalExpensesIncurredOnThisTxn));
					if (travelExpenceTotalAmt != null) {
						newClaimTransaction.setGrossAmount(Double.parseDouble(travelExpenceTotalAmt));
					}
					if (travelExpenceTotalTax != null) {
						newClaimTransaction.setClaimsNetTax(Double.parseDouble(travelExpenceTotalTax));
					}
				}
				if (amtDueFromCompany != null && !amtDueFromCompany.equals("")) {
					newClaimTransaction.setClaimsRequiredSettlement(Double.parseDouble(amtDueFromCompany));
				}
				if (amtDueToCompany != null && !amtDueToCompany.equals("")) {
					newClaimTransaction.setClaimsReturnSettlement(Double.parseDouble(amtDueToCompany));
				}
				if (amtReturnInCaseOfDueToCompany != null && !amtReturnInCaseOfDueToCompany.equals("")) {
					newClaimTransaction
							.setAmountReturnInCaseOfDueToCompany(Double.parseDouble(amtReturnInCaseOfDueToCompany));
				}
				if (amtUpdatedUnsettledAmount != null && !amtUpdatedUnsettledAmount.equals("")) {
					newClaimTransaction.setClaimsDueSettlement(Double.parseDouble(amtUpdatedUnsettledAmount));
				}
				if (expenseAdvancetxnRemarks != null && !expenseAdvancetxnRemarks.equals("")) {
					if (newClaimTransaction.getTxnRemarks() != null) {
						txnRemarks = newClaimTransaction.getTxnRemarks() + "," + user.getEmail() + "#"
								+ expenseAdvancetxnRemarks;
						newClaimTransaction.setTxnRemarks(txnRemarks);
					} else {
						txnRemarks = user.getEmail() + "#" + expenseAdvancetxnRemarks;
						newClaimTransaction.setTxnRemarks(txnRemarks);
					}
				}

				try {
					newClaimTransaction.setSupportingDocuments(
							TRANSACTION_DAO.getAndDeleteSupportingDocument(newClaimTransaction.getSupportingDocuments(),
									user.getEmail(), expenseAdvanceSupportingDocuments, user, em));
				} catch (IDOSException e) {
					log.log(Level.SEVERE, "Error", e);
				}
				String accountantEmailsStr = "";
				criterias.clear();
				criterias.put("role.id", 5l);
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("presentStatus", 1);
				List<UsersRoles> accountantRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);
				for (UsersRoles usrRoles : accountantRole) {
					accountantEmailsStr += usrRoles.getUser().getEmail() + ",";
				}
				newClaimTransaction.setAccountantEmails(accountantEmailsStr);
				if (newClaimTransaction.getClaimsRequiredSettlement() > 0.0) {
					newClaimTransaction.setTransactionStatus("Payment Due To Staff");
					debitCredit = "Debit";
				}
				if (newClaimTransaction.getClaimsReturnSettlement() > 0.0) {
					newClaimTransaction.setTransactionStatus("Payment Due From Staff");
					debitCredit = "Credit";
				}
				if (newClaimTransaction.getClaimsRequiredSettlement() == 0.0
						&& newClaimTransaction.getClaimsReturnSettlement() == 0.0) {
					newClaimTransaction.setTransactionStatus("No Due For Settlement");
					debitCredit = "Debit";
				}
				newClaimTransaction.setDebitCredit(debitCredit);
				newClaimTransaction.setSettlementStatus("NOT-SETTLED");
				String transactionNumber = CodeHelper.getForeverUniqueID("CLAIMTXN", null);
				newClaimTransaction.setTransactionRefNumber(transactionNumber);
				genericDao.saveOrUpdate(newClaimTransaction, user, em);
				try {
					FILE_UPLOAD_SERVICE.updateUploadFileLogs(em, user, expenseAdvanceSupportingDocuments,
							newClaimTransaction.getId(), IdosConstants.CLAIM_TXN_TYPE);
				} catch (IDOSException e) {
					log.log(Level.SEVERE, "Error", e);
				}
				Double claimTxnTravelExpenses = 0.0;
				if (item1ExpIncurredOnThisTxnAmount != null && !"".equals(item1ExpIncurredOnThisTxnAmount)) {
					ClaimsSettlement claimsSettlement = new ClaimsSettlement();
					claimsSettlement.setTransaction(newClaimTransaction);
					claimsSettlement.setItemName(IdosConstants.INCURRED_EXPENCES);
					claimsSettlement.setItemValue(Double.valueOf(item1ExpIncurredOnThisTxnAmount));
					claimsSettlement.setItemGross(Double.valueOf(travelExpenceTotalAmt));
					claimsSettlement.setItemTax(Double.valueOf(travelExpenceTotalTax));
					claimsSettlement.setTransactionSpecifics(previousClaimTxn.getAdvanceForExpenseItems());
					claimsSettlement.setOrganization(newClaimTransaction.getTransactionBranchOrganization());
					claimsSettlement.setBranch(newClaimTransaction.getTransactionBranch());
					genericDao.saveOrUpdate(claimsSettlement, user, em);

					if ((newClaimTransaction != null && newClaimTransaction.getId() != null)
							&& (claimsSettlement != null && claimsSettlement.getId() != null)) {
						CLAIM_DETAILS_DAO.saveClaimItemDetails(user, json, em, newClaimTransaction, claimsSettlement);
					}
				}
				entitytransaction.commit();
				result.put("transactionPrimId", newClaimTransaction.getId());
				result.put("transactionRefNo", newClaimTransaction.getTransactionRefNumber());
				sendSocketResponeToClient(newClaimTransaction, user, result);
			} catch (Exception e) {
				if (entitytransaction.isActive()) {
					entitytransaction.rollback();
				}
				if (log.isLoggable(Level.SEVERE)) {
					log.log(Level.SEVERE, "Error", e);
				}
			}
		} else if (claimTxnPurposeText.equals("Request For Expense Reimbursement")) {
			try {
				entitytransaction.begin();
				String reimbursementTxnBnch = json.findValue("reimbursementTxnBnch") != null
						? json.findValue("reimbursementTxnBnch").asText()
						: null;
				String reimbursementTxnPjct = json.findValue("reimbursementTxnPjct") != null
						? json.findValue("reimbursementTxnPjct").asText()
						: null;
				String reimbursementTxnItemSpecf = json.findValue("reimbursementTxnItemSpecf") != null
						? json.findValue("reimbursementTxnItemSpecf").asText()
						: null;
				String reimbursementExpenseReimbursementEligibilityDetailsDiv = json
						.findValue("reimbursementExpenseReimbursementEligibilityDetailsDiv") != null
								? json.findValue("reimbursementExpenseReimbursementEligibilityDetailsDiv").asText()
								: null;
				String reimbursementfollowedkl = json.findValue("reimbursementfollowedkl") != null
						? json.findValue("reimbursementfollowedkl").asText()
						: null;
				String reimbursementklcontents = json.findValue("reimbursementklcontents") != null
						? json.findValue("reimbursementklcontents").asText()
						: null;
				String reimbursementPurpose = json.findValue("reimbursementPurpose") != null
						? json.findValue("reimbursementPurpose").asText()
						: null;
				String reimbursementAmountEntered = json.findValue("reimbursementAmountEntered") != null
						? json.findValue("reimbursementAmountEntered").asText()
						: null;
				String reimbursementtxnRemarks = json.findValue("reimbursementtxnRemarks") != null
						? json.findValue("reimbursementtxnRemarks").asText()
						: null;
				String reimbursementSupportingDocuments = json.findValue("reimbursementSupportingDocuments") != null
						? json.findValue("reimbursementSupportingDocuments").asText()
						: null;
				String reiEmbExpensesDetails = json.findValue("reiEmbExpensesDetails").toString();
				String travelExpenceTotalAmt = json.findValue("travelExpenceTotalAmt") != null
						? json.findValue("travelExpenceTotalAmt").asText()
						: null;
				String travelExpenceTotalTax = json.findValue("travelExpenceTotalTax") != null
						? json.findValue("travelExpenceTotalTax").asText()
						: null;
				Branch bnch = null;
				Project pjct = null;
				Specifics specf = null;
				TransactionPurpose txnPurpose = null;
				String txnRemarks = null;
				String txnDocument = "";
				if (reimbursementTxnBnch != null) {
					bnch = Branch.findById(Long.parseLong(reimbursementTxnBnch));
				}
				if (reimbursementTxnPjct != null && reimbursementTxnPjct != "") {
					pjct = Project.findById(Long.parseLong(reimbursementTxnPjct));
				}
				if (reimbursementTxnItemSpecf != null) {
					specf = Specifics.findById(Long.parseLong(reimbursementTxnItemSpecf));
				}
				ClaimTransaction claimTxn = null;
				if (txnEntityID > 0) {
					claimTxn = ClaimTransaction.findById(txnEntityID);
				} else {
					claimTxn = new ClaimTransaction();
				}
				claimTxn.setTransactionDate(Calendar.getInstance().getTime());
				if (claimTxnPurposeVal != null && !claimTxnPurposeVal.equals("")) {
					txnPurpose = TransactionPurpose.findById(claimTxnPurposeVal);
					claimTxn.setTransactionPurpose(txnPurpose);
				}
				if (bnch != null) {
					claimTxn.setTransactionBranch(bnch);
					claimTxn.setTransactionBranchOrganization(bnch.getOrganization());
				}
				if (pjct != null) {
					claimTxn.setTransactionProject(pjct);
				}
				if (specf != null) {
					claimTxn.setAdvanceForExpenseItems(specf);
					claimTxn.setAdvanceForExpenseItemsParticulars(specf.getParticularsId());
				}
				if (reimbursementExpenseReimbursementEligibilityDetailsDiv != null) {
					claimTxn.setAdvanceEligibilityDetails(reimbursementExpenseReimbursementEligibilityDetailsDiv);
				}
				if (reimbursementfollowedkl != null && !reimbursementfollowedkl.equals("")) {
					claimTxn.setKlFollowStatus(Integer.parseInt(reimbursementfollowedkl));
				}
				if (reimbursementklcontents != null && !reimbursementklcontents.equals("")) {
					claimTxn.setPresentTxnRules(reimbursementklcontents);
				}
				if (reimbursementPurpose != null && !reimbursementPurpose.equals("")) {
					claimTxn.setPurposeOfAdvance(reimbursementPurpose);
				}
				if (reimbursementAmountEntered != null) {
					claimTxn.setClaimsNetSettlement(Double.parseDouble(reimbursementAmountEntered));
					claimTxn.setNewAmount(Double.parseDouble(reimbursementAmountEntered));
					claimTxn.setClaimsDueSettlement(Double.parseDouble(reimbursementAmountEntered));
					if (travelExpenceTotalAmt != null) {
						claimTxn.setGrossAmount(Double.parseDouble(travelExpenceTotalAmt));
					}
					if (travelExpenceTotalTax != null) {
						claimTxn.setClaimsNetTax(Double.parseDouble(travelExpenceTotalTax));
					}
				}
				if (reimbursementtxnRemarks != null && !reimbursementtxnRemarks.equals("")) {
					if (claimTxn.getTxnRemarks() != null) {
						txnRemarks = claimTxn.getTxnRemarks() + "," + user.getEmail() + "#" + reimbursementtxnRemarks;
						claimTxn.setTxnRemarks(txnRemarks);
					} else {
						txnRemarks = user.getEmail() + "#" + reimbursementtxnRemarks;
						claimTxn.setTxnRemarks(txnRemarks);
					}
				}

				try {
					claimTxn.setSupportingDocuments(
							TRANSACTION_DAO.getAndDeleteSupportingDocument(claimTxn.getSupportingDocuments(),
									user.getEmail(), reimbursementSupportingDocuments, user, em));
				} catch (IDOSException e) {
					log.log(Level.SEVERE, "Error", e);
				}
				criterias.clear();
				criterias.put("role.name", "APPROVER");
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("presentStatus", 1);
				List<UsersRoles> approverRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);
				String debitCredit = "Debit";
				String approverEmails = "";
				String additionalApprovarUsers = "";
				String selectedAdditionalApproval = "";
				for (UsersRoles usrRoles : approverRole) {
					additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
					criterias.clear();
					criterias.put("user.id", usrRoles.getUser().getId());
					criterias.put("userRights.id", 2L);
					criterias.put("branch.id", bnch.getId());
					criterias.put("presentStatus", 1);
					UserRightInBranch userHasRightInBranch = genericDao.getByCriteria(UserRightInBranch.class,
							criterias, em);
					if (userHasRightInBranch != null) {
						criterias.clear();
						criterias.put("user.id", usrRoles.getUser().getId());
						criterias.put("userRights.id", 2L);
						criterias.put("specifics.id", specf.getId());
						criterias.put("presentStatus", 1);
						UserRightSpecifics userHasRightInCOA = genericDao.getByCriteria(UserRightSpecifics.class,
								criterias, em);
						if (userHasRightInCOA != null) {
							approverEmails += usrRoles.getUser().getEmail() + ",";
						}
					}
				}
				String accountantEmailsStr = "";
				criterias.clear();
				criterias.put("role.id", 5l);
				criterias.put("organization.id", user.getOrganization().getId());
				criterias.put("presentStatus", 1);
				List<UsersRoles> accountantRole = genericDao.findByCriteria(UsersRoles.class, criterias, em);
				for (UsersRoles usrRoles : accountantRole) {
					accountantEmailsStr += usrRoles.getUser().getEmail() + ",";
				}
				claimTxn.setDebitCredit(debitCredit);
				claimTxn.setApproverMails(approverEmails);
				claimTxn.setAccountantEmails(accountantEmailsStr);
				claimTxn.setTransactionStatus("Require Approval");
				claimTxn.setSettlementStatus("NOT-SETTLED");
				String transactionNumber = CodeHelper.getForeverUniqueID("CLAIMTXN", null);
				claimTxn.setTransactionRefNumber(transactionNumber);
				genericDao.saveOrUpdate(claimTxn, user, em);
				try {
					FILE_UPLOAD_SERVICE.updateUploadFileLogs(em, user, reimbursementSupportingDocuments,
							claimTxn.getId(), IdosConstants.CLAIM_TXN_TYPE);
				} catch (IDOSException e) {
					log.log(Level.SEVERE, "Error", e);
				}
				if (claimTxn != null && claimTxn.getId() != null) {
					JSONArray reiEmbExpensesDetailsArray = new JSONArray(reiEmbExpensesDetails);
					for (int i = 0; i < reiEmbExpensesDetailsArray.length(); i++) {
						JSONObject rowItemData = new JSONObject(reiEmbExpensesDetailsArray.get(i).toString());
						CLAIM_DETAILS_DAO.saveClaimDetailsRow(user, rowItemData, IdosConstants.REIMBURSEMENT_EXPENSES,
								claimTxn, em, null);
					}
				}
				entitytransaction.commit();
				result.put("transactionPrimId", claimTxn.getId());
				result.put("transactionRefNo", claimTxn.getTransactionRefNumber());
				sendSocketResponeToClient(claimTxn, user, result);
				log.log(Level.FINE, "************* End " + result);
			} catch (Exception e) {
				// TODO: handle exception
				if (entitytransaction.isActive()) {
					entitytransaction.rollback();
				}
				log.log(Level.SEVERE, "Error", e);
			}
		}
		return result;
	}

	public void sendSocketResponeToClient(ClaimTransaction claimTransaction, Users user, ObjectNode result) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		Long id = claimTransaction.getId() != null ? claimTransaction.getId() : null;
		String branchName = claimTransaction.getTransactionBranch() != null
				? claimTransaction.getTransactionBranch().getName()
				: "";
		String projectName = claimTransaction.getTransactionProject() != null
				? claimTransaction.getTransactionProject().getName()
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
		Double expenseAdvanceTotalAdvanceAmount = claimTransaction.getTotalAdvanceForExpenseDuringTxn() != null
				? claimTransaction.getTotalAdvanceForExpenseDuringTxn()
				: 0.0;
		String expenseAdvancepurposeOfExpenseAdvance = claimTransaction.getPurposeOfAdvance() != null
				? claimTransaction.getPurposeOfAdvance()
				: "";
		Double dueFromCompany = claimTransaction.getClaimsRequiredSettlement() != null
				? claimTransaction.getClaimsRequiredSettlement()
				: 0.0;
		Double dueToCompany = claimTransaction.getClaimsReturnSettlement() != null
				? claimTransaction.getClaimsReturnSettlement()
				: 0.0;
		Double amountReturnInCaseOfDueToCompany = claimTransaction.getAmountReturnInCaseOfDueToCompany() != null
				? claimTransaction.getAmountReturnInCaseOfDueToCompany()
				: 0.0;
		TransactionViewResponse.addAdvanceExpenseTxn(id, branchName, projectName, txnQuestionName, txnOrgnName,
				travelType,
				noOfPlacesToVisit, placesSelectedOrEntered, typeOfCity, appropriateDiatance, totalDays,
				travelDetailedConfDescription, existingAdvance, claimuserAdvanveEligibility, adjustedAdvance,
				enteredAdvance, totalAdvance, purposeOfVisit, claimTxnRemarks, supportingDoc, claimdebitCredit,
				claimTxnStatus, claimapproverEmails, additionalApprovalEmails, claimselectedAdditionalApproval,
				creatorLabel, createdBy, transactionDate, approverLabel, approvedBy, accountantEmails, accountedLabel,
				accountedBy, txnSpecialStatus, paymentMode, expenseAdvanceTotalAdvanceAmount,
				expenseAdvancepurposeOfExpenseAdvance, itemName, itemParticularName, parentSpecificName, dueFromCompany,
				dueToCompany, amountReturnInCaseOfDueToCompany, instrumentNumber, instrumentDate, user.getEmail(),
				claimTxnRefNo, result);
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* End ");
		}
	}

	@Override
	public ObjectNode approverAction(ObjectNode result, JsonNode json, Users user, EntityManager em,
			EntityTransaction entitytransaction) throws IDOSException {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		entitytransaction.begin();
		String selectedApproverAction = json.findValue("selectedApproverAction").asText();
		String transactionPrimId = json.findValue("transactionPrimId").asText();
		String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
		String txnRmarks = json.findValue("txnRmarks").asText();
		BranchCashService branchCashService = new BranchCashServiceImpl();
		BranchBankService branchBankService = new BranchBankServiceImpl();
		TrialBalanceService trialBalanceService = new TrialBalanceServiceImpl();
		ClaimTransaction claimTransaction = ClaimTransaction.findById(Long.parseLong(transactionPrimId));
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
				claimTransaction.setSettlementStatus("SETTLED");
				saveAndUpdateSupportingDocAndRemarks(claimTransaction, user, suppDoc, txnRmarks);
				claimTransaction.setTransactionStatus("Rejected");
				claimTransaction.setModifiedBy(user);
				claimTransaction.setApproverActionBy(user);
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
			if (selectedApproverAction.equals("4")) {
				saveAndUpdateSupportingDocAndRemarks(claimTransaction, user, suppDoc, txnRmarks);
				addAdvanceToUserAccount(claimTransaction, user);
				claimTransaction.setTransactionStatus("Accounted");
				claimTransaction.setModifiedBy(user);
				claimTransaction.setAccountingActionBy(user);
				claimTransaction.setSettlementStatus("NOT-SETTLED");
				// *******TRIAL BALANCE CHANGES: START***********//
				// Request For Expense Reimbursement = Say for Chairs exp reimubursment=1000
				// then for Expense-chairs+1000 in 2nd column and Asssets-cash+1000 in 3rd
				// Column
				if (claimTransaction.getTransactionPurpose().getId() == 19) {
					Double totalTax = 0d;
					Map<String, Object> criterias = new HashMap<String, Object>();
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("branch.id", claimTransaction.getTransactionBranch().getId());
					criterias.put("transaction.id", claimTransaction.getId());
					criterias.put("presentStatus", 1);
					List<ClaimItemDetails> claimItemDetailsList = genericDao.findByCriteria(ClaimItemDetails.class,
							criterias, em);
					if (claimItemDetailsList != null && !claimItemDetailsList.isEmpty()) {
						for (ClaimItemDetails claimItemDetails : claimItemDetailsList) {
							totalTax += CLAIM_SETTLEMENT_DAO.saveClaimTrialBalanceGstTax(claimTransaction, user, em,
									claimItemDetails, claimTransaction.getAdvanceForExpenseItems());
						}
					}
					TRIAL_BALANCE_CLAIMS.insertTrialBalCOAItems(claimTransaction, user, em,
							claimTransaction.getNewAmount() - totalTax, false);

					TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserClaim(claimTransaction, user, em,
							claimTransaction.getNewAmount(), true);

				} else { // Request advance for expense, advance is given to user
					// When emp takes advance from company, it is debit amt for user
					TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserAdvance(claimTransaction, user, em,
							claimTransaction.getNewAmount(), false);
					TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserClaim(claimTransaction, user, em,
							claimTransaction.getNewAmount(), true);
				}

				// ***************TRIAL BALANCE CHNAGES: END**********//
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
		entitytransaction.commit();
		sendSocketResponeToClient(claimTransaction, user, result);
		log.log(Level.FINE, "************* End " + result);
		return result;
	}

	@Override
	public ObjectNode reimbursementApproverAction(ObjectNode result, JsonNode json, Users user, EntityManager em,
			EntityTransaction entitytransaction) throws IDOSException {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		entitytransaction.begin();
		String selectedApproverAction = json.findValue("selectedApproverAction") == null ? null
				: json.findValue("selectedApproverAction").asText();
		if (selectedApproverAction == null) {
			throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
					IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Invalid accounting action.");
		}
		Long transactionPrimId = json.findValue("transactionPrimId").asLong();
		String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
		String txnRmarks = json.findValue("txnRmarks").asText();
		ClaimTransaction claimTransaction = ClaimTransaction.findById(transactionPrimId);
		if (claimTransaction == null) {
			throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
					IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Claim transaction is not found: " + transactionPrimId);
		}
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
				claimTransaction.setSettlementStatus("SETTLED");
				saveAndUpdateSupportingDocAndRemarks(claimTransaction, user, suppDoc, txnRmarks);
				claimTransaction.setTransactionStatus("Rejected");
				claimTransaction.setModifiedBy(user);
				claimTransaction.setApproverActionBy(user);
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
			if (selectedApproverAction.equals("4")) {
				saveAndUpdateSupportingDocAndRemarks(claimTransaction, user, suppDoc, txnRmarks);
				addSettleReimbursementToUserAccount(claimTransaction, user);
				claimTransaction.setTransactionStatus("Accounted");
				claimTransaction.setModifiedBy(user);
				claimTransaction.setAccountingActionBy(user);
				// Double totalTax =
				// CLAIM_SETTLEMENT_DAO.saveClaimTrialBalanceGstTax(claimTransaction, user, em,
				// null, claimTransaction.getAdvanceForExpenseItems());

				if (claimTransaction.getTransactionPurpose().getId() == 19) {
					Double totalTax = 0d;
					Map<String, Object> criterias = new HashMap<String, Object>(4);
					criterias.put("organization.id", user.getOrganization().getId());
					criterias.put("branch.id", claimTransaction.getTransactionBranch().getId());
					criterias.put("transaction.id", claimTransaction.getId());
					criterias.put("presentStatus", 1);
					List<ClaimItemDetails> claimItemDetailsList = genericDao.findByCriteria(ClaimItemDetails.class,
							criterias, em);
					Specifics specific = claimTransaction.getAdvanceForExpenseItems();
					if (specific != null) {
						if (specific.getIsEligibleInputTaxCredit() == 1) {
							if (claimItemDetailsList != null && !claimItemDetailsList.isEmpty()) {
								for (ClaimItemDetails claimItemDetails : claimItemDetailsList) {
									totalTax += CLAIM_SETTLEMENT_DAO.saveClaimTrialBalanceGstTax(claimTransaction, user,
											em, claimItemDetails, claimTransaction.getAdvanceForExpenseItems());
								}
							}
						}
						TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserClaim(claimTransaction, user, em,
								claimTransaction.getNewAmount(), true);
						TRIAL_BALANCE_CLAIMS.insertTrialBalCOAItems(claimTransaction, user, em,
								claimTransaction.getNewAmount() - totalTax, false);
					} else {
						TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserClaim(claimTransaction, user, em,
								claimTransaction.getNewAmount(), true);
					}
				}
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
		String stbuf2 = ("select obj from GSTFiling obj where obj.organizationId.id=?1 and  obj.transactionPurpose.id=?2 and obj.agentName=?3 and obj.presentStatus=1 and obj.claimTransactionId.id=?4");
		ArrayList inparam2 = new ArrayList(4);
		inparam2.add(user.getOrganization().getId());
		inparam2.add(IdosConstants.REQUEST_FOR_EXPENSE_REIMBURSEMENT);
		inparam2.add("PWC");
		inparam2.add(claimTransaction.getId());
		List<GSTFiling> gstFilingsForClaimTransactions = null;
		gstFilingsForClaimTransactions = genericDao.queryWithParams(stbuf2.toString(), em, inparam2);
		log.log(Level.INFO, "SIZE OF TRANSACTION=" + gstFilingsForClaimTransactions.size());
		if (gstFilingsForClaimTransactions.size() <= 0) {
			KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
			karvyAPICall.saveGSTFilingDataForClaimTransaction(user, claimTransaction, em);
		}
		entitytransaction.commit();
		sendSocketResponeToClient(claimTransaction, user, result);
		log.log(Level.FINE, "************* End " + result);
		return result;
	}

	public void addSettleReimbursementToUserAccount(ClaimTransaction claimTransaction, Users user) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		Double addExpenseReimbursement = 0.0;
		Double settleExpenseReimbursement = 0.0;
		Users createdUser = claimTransaction.getCreatedBy();
		if (createdUser.getExpenseReimbursementAccountAmount() != null) {
			addExpenseReimbursement = createdUser.getExpenseReimbursementAccountAmount()
					+ claimTransaction.getNewAmount();
			createdUser.setExpenseReimbursementAccountAmount(addExpenseReimbursement);
		}
		if (createdUser.getExpenseReimbursementAccountAmount() == null) {
			addExpenseReimbursement = claimTransaction.getNewAmount();
			createdUser.setExpenseReimbursementAccountAmount(addExpenseReimbursement);
		}
		if (createdUser.getExpenseReimbursementSettledAmount() != null) {
			settleExpenseReimbursement = createdUser.getExpenseReimbursementSettledAmount()
					+ claimTransaction.getNewAmount();
			createdUser.setExpenseReimbursementSettledAmount(settleExpenseReimbursement);
		}
		if (createdUser.getExpenseReimbursementSettledAmount() == null) {
			settleExpenseReimbursement = claimTransaction.getNewAmount();
			createdUser.setExpenseReimbursementSettledAmount(settleExpenseReimbursement);
		}
		genericDao.saveOrUpdate(createdUser, user, entityManager);
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* End ");
		}
	}

	public void addAdvanceToUserAccount(ClaimTransaction claimTransaction, Users user) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		Double expenseAdvanceAccountAmount = 0.0;
		Users createdUser = claimTransaction.getCreatedBy();
		if (createdUser.getExpenseAdvanceAccountAmount() != null) {
			expenseAdvanceAccountAmount = createdUser.getExpenseAdvanceAccountAmount()
					+ claimTransaction.getNewAmount();
			createdUser.setExpenseAdvanceAccountAmount(expenseAdvanceAccountAmount);
		}
		if (createdUser.getExpenseAdvanceAccountAmount() == null) {
			expenseAdvanceAccountAmount = claimTransaction.getNewAmount();
			createdUser.setExpenseAdvanceAccountAmount(expenseAdvanceAccountAmount);
		}
		genericDao.saveOrUpdate(createdUser, user, entityManager);
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* End ");
		}
	}

	public void saveAndUpdateSupportingDocAndRemarks(ClaimTransaction claimTransaction, Users user, String suppDoc,
			String txnRmarks) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		if (suppDoc != null && !suppDoc.equals("")) {
			if (claimTransaction.getSupportingDocuments() != null) {
				claimTransaction.setSupportingDocuments(
						claimTransaction.getSupportingDocuments() + "," + user.getEmail() + "#" + suppDoc);
			} else {
				claimTransaction.setSupportingDocuments(user.getEmail() + "#" + suppDoc);
			}
		}
		if (txnRmarks != null && !txnRmarks.equals("")) {
			if (claimTransaction.getTxnRemarks() != null) {
				claimTransaction
						.setTxnRemarks(claimTransaction.getTxnRemarks() + "," + user.getEmail() + "#" + txnRmarks);
			} else {
				claimTransaction.setTxnRemarks(user.getEmail() + "#" + txnRmarks);
			}
		}
		genericDao.saveOrUpdate(claimTransaction, user, entityManager);
	}

	@Override
	public ObjectNode populateUserUnsettledExpenseAdvances(ObjectNode result, JsonNode json, Users user,
			EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		result.put("result", false);
		ArrayNode expenseAdvanceUnsettledDataAn = result.putArray("expenseAdvanceUnsettledData");
		List<ClaimTransaction> userClaimTransactionList = null;
		StringBuilder newsbquery = new StringBuilder("select obj from ClaimTransaction obj where obj.createdBy='"
				+ user.getId()
				+ "' and obj.transactionPurpose=17 and obj.claimsDueSettlement>=0.0 and obj.settlementStatus!='SETTLED' and obj.transactionStatus='Accounted' and obj.presentStatus=1 ORDER BY obj.createdAt desc");
		userClaimTransactionList = genericDao.executeSimpleQuery(newsbquery.toString(), em);
		for (ClaimTransaction claimTransaction : userClaimTransactionList) {
			StringBuilder sbuffer = new StringBuilder(
					"select SUM(obj.newAmount) from ClaimTransaction obj where obj.createdBy='" + user.getId()
							+ "' and obj.transactionPurpose=18 and obj.claimsSettlementRefNumber='"
							+ claimTransaction.getTransactionRefNumber()
							+ "' and obj.settlementStatus!='SETTLED' and obj.presentStatus=1 and obj.transactionStatus!='Accounted'");
			List<ClaimTransaction> settlementInProgress = genericDao.executeSimpleQuery(sbuffer.toString(), em);
			Double deductedValue = 0.0;
			if (settlementInProgress.size() > 0) {
				Object val = settlementInProgress.get(0);
				if (val != null) {
					deductedValue = Double.parseDouble(String.valueOf(val));
				}
			}
			sbuffer.delete(0, sbuffer.length());
			sbuffer.append(
					"select SUM(obj.amountReturnInCaseOfDueToCompany) from ClaimTransaction obj where obj.createdBy='"
							+ user.getId() + "' and obj.transactionPurpose=18 and obj.claimsSettlementRefNumber='"
							+ claimTransaction.getTransactionRefNumber()
							+ "' and obj.settlementStatus!='SETTLED' and obj.presentStatus=1 and obj.transactionStatus!='Accounted'");
			List<ClaimTransaction> settlementReturnedInCaseOfDueToCompanyProgress = genericDao
					.executeSimpleQuery(sbuffer.toString(), em);
			Double amountReturnInCaseOfDueToCompany = 0.0;
			if (settlementReturnedInCaseOfDueToCompanyProgress.size() > 0) {
				Object val = settlementReturnedInCaseOfDueToCompanyProgress.get(0);
				if (val != null) {
					amountReturnInCaseOfDueToCompany = Double.parseDouble(String.valueOf(val));
				}
			}
			Double resultVlaue = claimTransaction.getClaimsDueSettlement() - deductedValue
					- amountReturnInCaseOfDueToCompany;
			if (resultVlaue > 0.0) {
				result.put("result", true);
				ObjectNode row = Json.newObject();
				row.put("id", claimTransaction.getId());
				row.put("refNumberAmount", claimTransaction.getTransactionRefNumber() + "(" + resultVlaue + ")");
				expenseAdvanceUnsettledDataAn.add(row);
			}
		}
		return result;
	}

	@Override
	public ObjectNode displayUnsettledAdvances(ObjectNode result, JsonNode json, Users user, EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		result.put("result", false);
		ArrayNode userAdvExpTxnUnsettledAdvancesAn = result.putArray("userAdvExpTxnUnsettledAdvances");
		String previousAdvanceExpTxnPrimId = json.findValue("previousAdvanceExpTxnPrimId") != null
				? json.findValue("previousAdvanceExpTxnPrimId").asText()
				: null;
		ClaimTransaction prevClaimTxn = ClaimTransaction.findById(Long.parseLong(previousAdvanceExpTxnPrimId));
		if (prevClaimTxn != null) {
			Double totalAdvanceAgainstTxn = 0.0;
			Double settledTillDateTxn = 0.0;
			Double balanceUnsettledAgainstThisTxn = 0.0;
			ObjectNode row = Json.newObject();
			if (prevClaimTxn.getClaimsNetSettlement() != null) {
				totalAdvanceAgainstTxn = prevClaimTxn.getClaimsNetSettlement();
				if (prevClaimTxn.getClaimsDueSettlement() != null) {
					settledTillDateTxn = prevClaimTxn.getClaimsNetSettlement() - prevClaimTxn.getClaimsDueSettlement();
				}
			}
			if (prevClaimTxn.getClaimsDueSettlement() != null) {
				balanceUnsettledAgainstThisTxn = prevClaimTxn.getClaimsDueSettlement();
			}
			StringBuilder sbuffer = new StringBuilder("");
			sbuffer.append("select SUM(obj.newAmount) from ClaimTransaction obj where obj.createdBy='" + user.getId()
					+ "' and obj.transactionPurpose=18 and obj.claimsSettlementRefNumber='"
					+ prevClaimTxn.getTransactionRefNumber()
					+ "' and obj.settlementStatus!='SETTLED' and obj.presentStatus=1 and obj.transactionStatus!='Accounted'");
			List<ClaimTransaction> settlementInProgress = genericDao.executeSimpleQuery(sbuffer.toString(), em);
			Double deductedValue = 0.0;
			if (settlementInProgress.size() > 0) {
				Object val = settlementInProgress.get(0);
				if (val != null) {
					deductedValue = Double.parseDouble(String.valueOf(val));
				}
			}
			sbuffer.delete(0, sbuffer.length());
			sbuffer.append(
					"select SUM(obj.amountReturnInCaseOfDueToCompany) from ClaimTransaction obj where obj.createdBy='"
							+ user.getId() + "' and obj.transactionPurpose=18 and obj.claimsSettlementRefNumber='"
							+ prevClaimTxn.getTransactionRefNumber()
							+ "' and obj.settlementStatus!='SETTLED' and obj.presentStatus=1 and obj.transactionStatus!='Accounted'");
			List<ClaimTransaction> settlementReturnedInCaseOfDueToCompanyProgress = genericDao
					.executeSimpleQuery(sbuffer.toString(), em);
			Double amountReturnInCaseOfDueToCompany = 0.0;
			if (settlementReturnedInCaseOfDueToCompanyProgress.size() > 0) {
				Object val = settlementReturnedInCaseOfDueToCompanyProgress.get(0);
				if (val != null) {
					amountReturnInCaseOfDueToCompany = Double.parseDouble(String.valueOf(val));
				}
			}
			balanceUnsettledAgainstThisTxn = balanceUnsettledAgainstThisTxn - deductedValue
					- amountReturnInCaseOfDueToCompany;
			result.put("result", true);
			row.put("totalAdvanceAgainstTxn", IdosConstants.decimalFormat.format(totalAdvanceAgainstTxn));
			row.put("settledTillDate", IdosConstants.decimalFormat.format(settledTillDateTxn));
			row.put("itemNameExpIncurred", prevClaimTxn.getAdvanceForExpenseItems().getName());
			row.put("balanceUnsettledAgainstThisTxn",
					IdosConstants.decimalFormat.format(balanceUnsettledAgainstThisTxn));
			userAdvExpTxnUnsettledAdvancesAn.add(row);
		}
		return result;
	}

	@Override
	public ObjectNode expAdvanceSettlementAccountantAction(ObjectNode result, JsonNode json, Users user,
			EntityManager em, EntityTransaction entitytransaction) throws IDOSException {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		entitytransaction.begin();
		String transactionPrimId = json.findValue("claimTxnPrimId").asText();
		String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
		String txnRmarks = json.findValue("txnRmarks").asText();
		// String
		// claimSettlementValue=json.findValue("claimSettlementValue")!=null?json.findValue("claimSettlementValue").asText():null;
		BranchCashService branchCashService = new BranchCashServiceImpl();
		BranchBankService branchBankService = new BranchBankServiceImpl();
		TrialBalanceService trialBalanceService = new TrialBalanceServiceImpl();
		Map<String, Object> criterias = new HashMap<String, Object>();
		ClaimTransaction claimTransaction = ClaimTransaction.findById(Long.parseLong(transactionPrimId));
		ClaimTransaction settlementClaimTxn = null;
		String claimSettlementValue = null;
		if (claimTransaction.getClaimsSettlementRefNumber() != null) {
			criterias.clear();
			criterias.put("transactionRefNumber", claimTransaction.getClaimsSettlementRefNumber());
			criterias.put("presentStatus", 1);
			settlementClaimTxn = genericDao.getByCriteria(ClaimTransaction.class, criterias, em);
		}
		if (claimTransaction != null) {
			claimSettlementValue = claimTransaction.getTransactionStatus();
		}
		if (claimSettlementValue != null && settlementClaimTxn != null) {

			if (claimSettlementValue.equals("Payment Due To Staff")) {
				addExpenseAdvanceAccountAmountAndAddExpenseAdvanceSettleAmount(claimTransaction, settlementClaimTxn,
						user, em);
				TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserAdvance(claimTransaction, user, em,
						claimTransaction.getClaimsRequiredSettlement(), true);
				claimTransaction.setSettlementStatus("NOT-SETTLED");
			} else if (claimSettlementValue.equals("Payment Due From Staff")) {
				// for company getting back money from user as expense advances for which
				// settlement is being done is more than the expenditure on this txn
				// settleTravelClaimAdvanceForThisTxn(claimTransaction,settlementClaimTxn,user,em);
				// addAmountToConcernedBranchAndDeductFromUserAdvance(claimTransaction,settlementClaimTxn,json,user,em);
				int txnreceiptdetails = json.findValue("paymentDetails") != null
						? json.findValue("paymentDetails").asInt()
						: 0;
				String txnreceipttypebankdetails = json.findValue("bankInf") == null ? null
						: json.findValue("bankInf").asText();
				String txnreceiptPaymentBank = json.findValue("txnPaymentBank") != null
						? json.findValue("txnPaymentBank").asText()
						: null;
				claimTransaction.setReceiptDetailsDescription(txnreceipttypebankdetails);
				claimTransaction.setReceiptDetailType(txnreceiptdetails);
				claimTransaction.setTransactionStatus("Accounted");
				if (txnreceiptdetails == IdosConstants.PAYMODE_BANK) {
					String txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
							? json.findValue("txnInstrumentNum").asText()
							: null;
					String txnInstrumentDate = json.findValue("txnInstrumentDate") != null
							? json.findValue("txnInstrumentDate").asText()
							: null;
					if (txnInstrumentNumber != null && !"".equals(txnInstrumentNumber)) {
						claimTransaction.setInstrumentNumber(txnInstrumentNumber);
					}
					if (txnInstrumentDate != null && !"".equals(txnInstrumentDate)) {
						claimTransaction.setInstrumentDate(txnInstrumentDate);
					}
				}
				if (txnreceiptdetails == IdosConstants.PAYMODE_CASH) {
					Double resultantCash = branchCashService.updateBranchCashDetail(em, user,
							claimTransaction.getTransactionBranch(), claimTransaction.getClaimsReturnSettlement(),
							false, claimTransaction.getTransactionDate(), result);
					result.put("resultantCash", resultantCash);
					trialBalanceService.addTrialBalanceForCash(user, em, genericDao, claimTransaction,
							claimTransaction.getAmountReturnInCaseOfDueToCompany(), false); // getClaimsReturnSettlement();
				} else if (txnreceiptdetails == IdosConstants.PAYMODE_BANK) {
					if (txnreceiptPaymentBank != null && !txnreceiptPaymentBank.equals("")) {
						BranchBankAccounts bankAccount = BranchBankAccounts
								.findById(Long.parseLong(txnreceiptPaymentBank));
						if (bankAccount == null) {
							throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE,
									IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION,
									"Bank is not selected in transaction when payment mode is Bank.");
						}
						claimTransaction.setTransactionBranchBankAccount(bankAccount);
						// Double resultantAmount = branchBankService.updateBranchBankDetail(em,
						// genericDao, user, bankAccount,
						// claimTransaction.getAmountReturnInCaseOfDueToCompany(),
						// false);//getClaimsReturnSettlement();
						boolean branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(em, user,
								bankAccount, claimTransaction.getAmountReturnInCaseOfDueToCompany(), false, result,
								claimTransaction.getTransactionDate(), claimTransaction.getTransactionBranch());
						if (!branchBankDetailEntered) {
							return result; // since balance is in -ve don't make any changes in DB
						}
						trialBalanceService.addTrialBalanceForBank(user, em, genericDao, claimTransaction,
								claimTransaction.getAmountReturnInCaseOfDueToCompany(), false);
					}
				}
				deductExpenseAdvanceAccountAmountAndAddToExpenseReturnedAccountAmount(claimTransaction,
						settlementClaimTxn, user, em);
				TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserClaim(claimTransaction, user, em,
						claimTransaction.getClaimsRequiredSettlement(), true);
				// Advance for Expense should reflect above 2 effects in 3rd column = 2000+900
				// When user has spend more than advance taken e.g. he spent 1700 out of 1000
				// advance then company has to pay 700 to user
				claimTransaction.setSettlementStatus("SETTLED");

			} else if (claimSettlementValue.equals("No Due For Settlement")) { // for settling and closing the claim
																				// advance transaction as user
																				// expenditure is equivalent to the
																				// advances taken and adjustmeny if any
																				// on this particular transaction
				// claimTransaction.setTransactionStatus("Accounted");
				// settleTravelClaimAdvanceForThisTxn(claimTransaction,settlementClaimTxn,user,em);
				deductExpenseAdvanceAccountAmountAndAddToExpenseReturnedAccountAmount(claimTransaction,
						settlementClaimTxn, user, em);
				TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserClaim(claimTransaction, user, em,
						claimTransaction.getClaimsRequiredSettlement(), true);
			}
			settleTravelClaimAdvanceForThisTxn(claimTransaction, settlementClaimTxn, user, em);
			CLAIM_SETTLEMENT_DAO.addExpensesInTrialBalance(user, em, claimTransaction, false);
		}
		claimTransaction.setSupportingDocuments(TRANSACTION_DAO.getAndDeleteSupportingDocument(
				claimTransaction.getSupportingDocuments(), user.getEmail(), suppDoc, user, em));
		if (txnRmarks != null && !txnRmarks.equals("")) {
			if (claimTransaction.getTxnRemarks() != null) {
				claimTransaction
						.setTxnRemarks(claimTransaction.getTxnRemarks() + "," + user.getEmail() + "#" + txnRmarks);
			} else {
				claimTransaction.setTxnRemarks(user.getEmail() + "#" + txnRmarks);
			}
		}

		claimTransaction.setAccountingActionBy(user);
		claimTransaction.setTransactionStatus("Accounted");
		genericDao.saveOrUpdate(settlementClaimTxn, user, em);
		genericDao.saveOrUpdate(claimTransaction, user, em);
		FILE_UPLOAD_SERVICE.updateUploadFileLogs(em, user, suppDoc, claimTransaction.getId(),
				IdosConstants.CLAIM_TXN_TYPE);
		KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
		karvyAPICall.saveGSTFilingDataForClaimTransaction(user, claimTransaction, em);
		entitytransaction.commit();
		sendSocketResponeToClient(claimTransaction, user, result);
		return result;
	}

	private void settleTravelClaimAdvanceForThisTxn(ClaimTransaction claimTransaction,
			ClaimTransaction settlementClaimTxn, Users user, EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		if (settlementClaimTxn.getClaimsNetSettlement() != null) {
			settlementClaimTxn.setClaimsNetSettlement(claimTransaction.getClaimsNetSettlement());
		} else {
			settlementClaimTxn.setClaimsNetSettlement(claimTransaction.getClaimsNetSettlement());
		}
		if (claimTransaction.getAmountReturnInCaseOfDueToCompany() == null) {
			if (settlementClaimTxn.getClaimsDueSettlement() != null) {
				if (claimTransaction.getNewAmount() != null) {
					Double settleAmount = claimTransaction.getNewAmount();
					Double dueAmount = settlementClaimTxn.getClaimsDueSettlement();
					Double resultAmount = dueAmount - settleAmount;
					settlementClaimTxn.setClaimsDueSettlement(resultAmount);
				}
			} else {
				if (claimTransaction.getNewAmount() != null) {
					Double settleAmount = claimTransaction.getNewAmount();
					Double dueAmount = 0.0;
					Double resultAmount = dueAmount - settleAmount;
					settlementClaimTxn.setClaimsDueSettlement(resultAmount);
				}
			}
			settlementClaimTxn.setAmountReturnInCaseOfDueToCompany(0.0);
		}
		if (claimTransaction.getClaimsRequiredSettlement() != null) {
			settlementClaimTxn.setClaimsRequiredSettlement(claimTransaction.getClaimsRequiredSettlement());
		}
		if (claimTransaction.getClaimsReturnSettlement() != null) {
			settlementClaimTxn.setClaimsReturnSettlement(claimTransaction.getClaimsReturnSettlement());
		}
		if (claimTransaction.getAmountReturnInCaseOfDueToCompany() != null) {
			Double settleAmount = claimTransaction.getNewAmount();
			Double dueAmount = settlementClaimTxn.getClaimsDueSettlement();
			Double amountReturnedIncaseOfDue = claimTransaction.getAmountReturnInCaseOfDueToCompany();
			Double resultAmount = dueAmount - settleAmount - amountReturnedIncaseOfDue;
			settlementClaimTxn.setClaimsDueSettlement(resultAmount);
			if (settlementClaimTxn.getAmountReturnInCaseOfDueToCompany() != null) {
				settlementClaimTxn.setAmountReturnInCaseOfDueToCompany(
						settlementClaimTxn.getAmountReturnInCaseOfDueToCompany() + amountReturnedIncaseOfDue);
			} else {
				settlementClaimTxn.setAmountReturnInCaseOfDueToCompany(amountReturnedIncaseOfDue);
			}
		}
		genericDao.saveOrUpdate(settlementClaimTxn, user, em);
	}

	public void cashDeductionFromBranchCashBalanceInCaseOfRequiredSettlement(ClaimTransaction claimTransaction,
			Users user, EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		// debit to the branch cash count also effect on resultant cash
		Double creditAmount = null;
		Double debitAmount = null;
		Double resultantCash = null;
		Double mainToPettyCash = null;
		Double grandTotal = null;
		StringBuilder newsbquery = new StringBuilder("");
		newsbquery.append("select obj from BranchCashCount obj WHERE obj.branch.id='"
				+ claimTransaction.getTransactionBranch().getId() + "' AND obj.organization.id='"
				+ claimTransaction.getTransactionBranch().getOrganization().getId()
				+ "' and obj.presentStatus=1 ORDER BY obj.date desc");
		List<BranchCashCount> branchCashCount = genericDao.executeSimpleQueryWithLimit(newsbquery.toString(),
				entityManager, 1);
		if (branchCashCount.size() > 0) {
			if (branchCashCount.get(0).getCreditAmount() == null) {
				creditAmount = 0.0;
			}
			if (branchCashCount.get(0).getCreditAmount() != null) {
				creditAmount = branchCashCount.get(0).getCreditAmount();
			}
			if (branchCashCount.get(0).getDebitAmount() == null) {
				debitAmount = claimTransaction.getClaimsRequiredSettlement();
			}
			if (branchCashCount.get(0).getDebitAmount() != null) {
				debitAmount = branchCashCount.get(0).getDebitAmount() + claimTransaction.getClaimsRequiredSettlement();
			}
			if (branchCashCount.get(0).getTotalMainCashToPettyCash() != null) {
				mainToPettyCash = branchCashCount.get(0).getTotalMainCashToPettyCash();
			}
			if (branchCashCount.get(0).getTotalMainCashToPettyCash() != null) {
				mainToPettyCash = branchCashCount.get(0).getTotalMainCashToPettyCash();
			}
			if (branchCashCount.get(0).getTotalMainCashToPettyCash() == null) {
				mainToPettyCash = 0.0;
			}
			if (branchCashCount.get(0).getGrandTotal() != null) {
				grandTotal = branchCashCount.get(0).getGrandTotal();
			} else {
				grandTotal = 0.0;
			}
			resultantCash = grandTotal + creditAmount - debitAmount - mainToPettyCash;
			branchCashCount.get(0).setDebitAmount(debitAmount);
			branchCashCount.get(0).setResultantCash(resultantCash);
			genericDao.saveOrUpdate(branchCashCount.get(0), user, em);
		}
	}

	public void bankDeductionFromBranchBankBalanceInCaseOfRequiredSettlement(ClaimTransaction claimTransaction,
			Users user, String txnreceiptPaymentBank, EntityManager em) throws IDOSException {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		if (txnreceiptPaymentBank != null && !txnreceiptPaymentBank.equals("")) {
			Double creditAmount = null;
			Double debitAmount = null;
			Double resultantAmount = null;
			Double amountBalance = null;
			BranchBankAccounts bankAccount = BranchBankAccounts.findById(Long.parseLong(txnreceiptPaymentBank));
			if (bankAccount == null) {
				throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
						IdosConstants.INVALID_DATA_EXCEPTION,
						"Bank is not selected in transaction when payment mode is Bank.");
			}
			claimTransaction.setTransactionBranchBankAccount(bankAccount);
			StringBuilder newsbquery = new StringBuilder("");
			newsbquery.append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='"
					+ claimTransaction.getTransactionBranch().getId() + "' AND obj.organization.id='"
					+ claimTransaction.getTransactionBranchOrganization().getId() + "' and obj.branchBankAccounts.id='"
					+ bankAccount.getId() + "' and obj.presentStatus=1 ORDER BY obj.date desc");
			List<BranchBankAccountBalance> branchBankAccountBal = genericDao
					.executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
			if (branchBankAccountBal.size() > 0) {
				if (branchBankAccountBal.get(0).getCreditAmount() == null) {
					creditAmount = 0.0;
				}
				if (branchBankAccountBal.get(0).getCreditAmount() != null) {
					creditAmount = branchBankAccountBal.get(0).getCreditAmount();
				}
				if (branchBankAccountBal.get(0).getDebitAmount() == null) {
					debitAmount = claimTransaction.getClaimsRequiredSettlement();
				}
				if (branchBankAccountBal.get(0).getDebitAmount() != null) {
					debitAmount = branchBankAccountBal.get(0).getDebitAmount()
							+ claimTransaction.getClaimsRequiredSettlement();
				}
				if (branchBankAccountBal.get(0).getAmountBalance() != null) {
					amountBalance = branchBankAccountBal.get(0).getAmountBalance();
				} else {
					amountBalance = 0.0;
				}
				resultantAmount = amountBalance + creditAmount - debitAmount;
				branchBankAccountBal.get(0).setCreditAmount(creditAmount);
				branchBankAccountBal.get(0).setDebitAmount(debitAmount);
				branchBankAccountBal.get(0).setResultantCash(resultantAmount);
				genericDao.saveOrUpdate(branchBankAccountBal.get(0), user, em);
			}
		}
	}

	public void addExpenseAdvanceAccountAmountAndAddExpenseAdvanceSettleAmount(ClaimTransaction claimTransaction,
			ClaimTransaction settlementClaimTxn, Users user, EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		Double expenseAdvanceAccountAmount = 0.0;
		Double expenseAdvanceSettleAmount = 0.0;
		Users claimCreatedUser = claimTransaction.getCreatedBy();
		if (claimCreatedUser.getExpenseAdvanceAccountAmount() != null) {
			expenseAdvanceAccountAmount = claimCreatedUser.getExpenseAdvanceAccountAmount()
					+ claimTransaction.getClaimsRequiredSettlement();
		}
		if (claimCreatedUser.getTravelAdvanceAccountAmount() == null) {
			expenseAdvanceAccountAmount = claimTransaction.getClaimsRequiredSettlement();
		}
		if (claimCreatedUser.getExpenseAdvanceAettledAmount() != null) {
			expenseAdvanceSettleAmount = claimCreatedUser.getExpenseAdvanceAettledAmount()
					+ claimTransaction.getClaimsNetSettlement();
		}
		if (claimCreatedUser.getTravelAdvanceSettledAmount() == null) {
			expenseAdvanceSettleAmount = claimTransaction.getClaimsNetSettlement();
		}
		claimCreatedUser.setTravelAdvanceAccountAmount(expenseAdvanceAccountAmount);
		claimCreatedUser.setTravelAdvanceSettledAmount(expenseAdvanceSettleAmount);
		genericDao.saveOrUpdate(claimCreatedUser, user, em);
	}

	private void deductExpenseAdvanceAccountAmountAndAddToExpenseReturnedAccountAmount(
			ClaimTransaction claimTransaction, ClaimTransaction settlementClaimTxn, Users user, EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		Double travelAdvanceAccountAmount = 0.0;
		Double travelAdvanceReturnedAmount = 0.0;
		Double travelAdvanceSettleAmount = 0.0;
		Users claimCreatedUser = claimTransaction.getCreatedBy();
		if (claimCreatedUser.getTravelAdvanceAccountAmount() != null) {
			travelAdvanceAccountAmount = claimCreatedUser.getTravelAdvanceAccountAmount();
		}
		if (claimCreatedUser.getTravelAdvanceAccountAmount() == null) {
			travelAdvanceAccountAmount = travelAdvanceAccountAmount;
		}
		if (claimCreatedUser.getTravelAdvanceSettledAmount() != null) {
			travelAdvanceSettleAmount = claimCreatedUser.getTravelAdvanceSettledAmount()
					+ claimTransaction.getClaimsNetSettlement()
					+ claimTransaction.getAmountReturnInCaseOfDueToCompany();
		}
		if (claimCreatedUser.getTravelAdvanceSettledAmount() == null) {
			travelAdvanceSettleAmount = claimTransaction.getClaimsNetSettlement()
					+ claimTransaction.getAmountReturnInCaseOfDueToCompany();
		}
		if (claimCreatedUser.getTravelAdvanceReturnedAmount() != null) {
			travelAdvanceReturnedAmount = user.getTravelAdvanceReturnedAmount()
					+ claimTransaction.getAmountReturnInCaseOfDueToCompany();
		}
		if (claimCreatedUser.getTravelAdvanceReturnedAmount() == null) {
			travelAdvanceReturnedAmount = claimTransaction.getAmountReturnInCaseOfDueToCompany();
		}
		claimCreatedUser.setTravelAdvanceAccountAmount(travelAdvanceAccountAmount);
		claimCreatedUser.setTravelAdvanceSettledAmount(travelAdvanceSettleAmount);
		claimCreatedUser.setTravelAdvanceReturnedAmount(travelAdvanceReturnedAmount);
		genericDao.saveOrUpdate(user, user, em);
	}

	public void cashAdditionToBranchCashBalanceInCaseOfReturnSettlement(ClaimTransaction claimTransaction, Users user,
			EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		Double creditAmount = null;
		Double debitAmount = null;
		Double resultantCash = null;
		Double mainToPettyCash = null;
		Double grandTotal = null;
		StringBuilder newsbquery = new StringBuilder("");
		newsbquery.append("select obj from BranchCashCount obj WHERE obj.branch.id='"
				+ claimTransaction.getTransactionBranch().getId() + "' AND obj.organization.id='"
				+ claimTransaction.getTransactionBranch().getOrganization().getId()
				+ "' and obj.presentStatus=1 ORDER BY obj.date desc");
		List<BranchCashCount> branchCashCount = genericDao.executeSimpleQueryWithLimit(newsbquery.toString(),
				entityManager, 1);
		if (branchCashCount.size() > 0) {
			if (branchCashCount.get(0).getCreditAmount() == null) {
				creditAmount = claimTransaction.getAmountReturnInCaseOfDueToCompany();
			}
			if (branchCashCount.get(0).getCreditAmount() != null) {
				creditAmount = branchCashCount.get(0).getCreditAmount()
						+ claimTransaction.getAmountReturnInCaseOfDueToCompany();
			}
			if (branchCashCount.get(0).getDebitAmount() == null) {
				debitAmount = 0.0;
			}
			if (branchCashCount.get(0).getDebitAmount() != null) {
				debitAmount = branchCashCount.get(0).getDebitAmount();
			}
			if (branchCashCount.get(0).getTotalMainCashToPettyCash() != null) {
				mainToPettyCash = branchCashCount.get(0).getTotalMainCashToPettyCash();
			}
			if (branchCashCount.get(0).getTotalMainCashToPettyCash() != null) {
				mainToPettyCash = branchCashCount.get(0).getTotalMainCashToPettyCash();
			}
			if (branchCashCount.get(0).getTotalMainCashToPettyCash() == null) {
				mainToPettyCash = 0.0;
			}
			if (branchCashCount.get(0).getGrandTotal() != null) {
				grandTotal = branchCashCount.get(0).getGrandTotal();
			} else {
				grandTotal = 0.0;
			}
			resultantCash = grandTotal + creditAmount - debitAmount - mainToPettyCash;
			branchCashCount.get(0).setDebitAmount(debitAmount);
			branchCashCount.get(0).setResultantCash(resultantCash);
			genericDao.saveOrUpdate(branchCashCount.get(0), user, em);
		}
	}

	public void bankAdditionToBranchBankBalanceInCaseOfReturnSettlement(ClaimTransaction claimTransaction, Users user,
			String txnreceiptPaymentBank, EntityManager em) throws IDOSException {
		if (txnreceiptPaymentBank != null && !txnreceiptPaymentBank.equals("")) {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "************* Start ");
			}
			Double creditAmount = null;
			Double debitAmount = null;
			Double resultantAmount = null;
			Double amountBalance = null;
			BranchBankAccounts bankAccount = BranchBankAccounts.findById(Long.parseLong(txnreceiptPaymentBank));
			if (bankAccount == null) {
				throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
						IdosConstants.INVALID_DATA_EXCEPTION,
						"Bank is not selected in transaction when payment mode is Bank.");
			}
			claimTransaction.setTransactionBranchBankAccount(bankAccount);
			StringBuilder newsbquery = new StringBuilder("");
			newsbquery.append("select obj from BranchBankAccountBalance obj WHERE obj.branch.id='"
					+ claimTransaction.getTransactionBranch().getId() + "' AND obj.organization.id='"
					+ claimTransaction.getTransactionBranchOrganization().getId() + "' and obj.branchBankAccounts.id='"
					+ bankAccount.getId() + "' and obj.presentStatus=1 ORDER BY obj.date desc");
			List<BranchBankAccountBalance> branchBankAccountBal = genericDao
					.executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
			if (branchBankAccountBal.size() > 0) {
				if (branchBankAccountBal.get(0).getCreditAmount() == null) {
					creditAmount = claimTransaction.getAmountReturnInCaseOfDueToCompany();
				}
				if (branchBankAccountBal.get(0).getCreditAmount() != null) {
					creditAmount = branchBankAccountBal.get(0).getCreditAmount()
							+ claimTransaction.getAmountReturnInCaseOfDueToCompany();
				}
				if (branchBankAccountBal.get(0).getDebitAmount() == null) {
					debitAmount = 0.0;
				}
				if (branchBankAccountBal.get(0).getDebitAmount() != null) {
					debitAmount = branchBankAccountBal.get(0).getDebitAmount();
				}
				if (branchBankAccountBal.get(0).getAmountBalance() != null) {
					amountBalance = branchBankAccountBal.get(0).getAmountBalance();
				} else {
					amountBalance = 0.0;
				}
				resultantAmount = amountBalance + creditAmount - debitAmount;
				branchBankAccountBal.get(0).setCreditAmount(creditAmount);
				branchBankAccountBal.get(0).setDebitAmount(debitAmount);
				branchBankAccountBal.get(0).setResultantCash(resultantAmount);
				genericDao.saveOrUpdate(branchBankAccountBal.get(0), user, em);
			}
		}
	}

	@Override
	public ObjectNode getUserExpenseItemReimbursementAmountKl(ObjectNode result, JsonNode json, Users user,
			EntityManager em, EntityTransaction entitytransaction) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start ");
		}
		result.put("reimbursementresult", false);
		result.put("expenseitemklresult", false);
		ArrayNode reimbursementAmountDataAn = result.putArray("reimbursementAmountData");
		ArrayNode expenseItemSpecfKlDataAn = result.putArray("expenseItemSpecfKlData");
		String expenseItemValue = json.findValue("expenseItemValue") != null
				? json.findValue("expenseItemValue").asText()
				: null;
		String selectedUserBranch = json.findValue("selectedUserBranch") != null
				? json.findValue("selectedUserBranch").asText()
				: null;
		Specifics specf = null;
		Branch branch = null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		if (expenseItemValue != null) {
			specf = Specifics.findById(Long.parseLong(expenseItemValue));
		}
		if (selectedUserBranch != null) {
			branch = Branch.findById(Long.parseLong(selectedUserBranch));
		}
		ExpenseGroup usereGroup = user.geteGroup();
		if (usereGroup != null) {
			if (specf != null) {
				StringBuilder sbr = new StringBuilder("");
				sbr.append("select obj from ExpenseGroupExpenseItemMonetoryClaim obj where obj.organization='"
						+ user.getOrganization().getId() + "' and obj.specificsItem='" + specf.getId()
						+ "' and obj.specificsParticulars='" + specf.getParticularsId().getId()
						+ "' and obj.presentStatus=1 and obj.expenseGroup='" + usereGroup.getId() + "'");
				List<ExpenseGroupExpenseItemMonetoryClaim> eGroupExpenseMonetoryClaimList = genericDao
						.executeSimpleQuery(sbr.toString(), em);
				if (!eGroupExpenseMonetoryClaimList.isEmpty() && eGroupExpenseMonetoryClaimList.size() > 0) {
					List<String> firstAndLastDateOfMonth = DateUtil.returnFirstDayAndLastDayOfMonth();
					String firstDate = firstAndLastDateOfMonth.get(0);
					String lastDate = firstAndLastDateOfMonth.get(1);
					result.put("reimbursementresult", true);
					ExpenseGroupExpenseItemMonetoryClaim eGroupExpenseMonetoryClaim = eGroupExpenseMonetoryClaimList
							.get(0);
					ObjectNode row = Json.newObject();
					Double totalMonthlyMonetoryLimitForReimbursementForThisItem = eGroupExpenseMonetoryClaim
							.getMonthlyMonetoryLimitForReimbursement();
					Double thisMonthEmployeeAmountReimbursementForThisItemAccounted = 0.0;
					sbr.delete(0, sbr.length());
					sbr.append(
							"select SUM(obj.newAmount) from ClaimTransaction obj where obj.transactionBranchOrganization='"
									+ user.getOrganization().getId() + "' and obj.advanceForExpenseItems='"
									+ specf.getId() + "' and obj.advanceForExpenseItemsParticulars='"
									+ specf.getParticularsId().getId()
									+ "' and obj.transactionPurpose=19 and obj.transactionDate between '" + firstDate
									+ "' and '" + lastDate
									+ "' and obj.settlementStatus='SETTLED' and obj.transactionStatus='Accounted' and obj.presentStatus=1");
					List<ClaimTransaction> reimbursementAccountedList = genericDao.executeSimpleQuery(sbr.toString(),
							em);
					if (reimbursementAccountedList.size() > 0) {
						Object val = reimbursementAccountedList.get(0);
						if (val != null) {
							thisMonthEmployeeAmountReimbursementForThisItemAccounted = Double
									.parseDouble(String.valueOf(val));
						}
					}
					Double thisMonthEmployeeAmountReimbursementForThisItemInProgress = 0.0;
					sbr.delete(0, sbr.length());
					sbr.append(
							"select SUM(obj.newAmount) from ClaimTransaction obj where obj.transactionBranchOrganization='"
									+ user.getOrganization().getId() + "' and obj.advanceForExpenseItems='"
									+ specf.getId() + "' and obj.advanceForExpenseItemsParticulars='"
									+ specf.getParticularsId().getId()
									+ "' and obj.transactionPurpose=19 and obj.transactionDate between '" + firstDate
									+ "' and '" + lastDate
									+ "' and obj.settlementStatus!='SETTLED' and obj.transactionStatus!='Accounted' and obj.transactionStatus!='Rejected' and obj.presentStatus=1");
					List<ClaimTransaction> reimbursementInProgressList = genericDao.executeSimpleQuery(sbr.toString(),
							em);
					if (reimbursementInProgressList.size() > 0) {
						Object val = reimbursementInProgressList.get(0);
						if (val != null) {
							thisMonthEmployeeAmountReimbursementForThisItemInProgress = Double
									.parseDouble(String.valueOf(val));
						}
					}
					row.put("totalMonthlyMonetoryLimitForReimbursementForThisItem",
							IdosConstants.decimalFormat.format(totalMonthlyMonetoryLimitForReimbursementForThisItem));
					row.put("thisMonthEmployeeAmountReimbursementForThisItemAccounted", IdosConstants.decimalFormat
							.format(thisMonthEmployeeAmountReimbursementForThisItemAccounted));
					row.put("thisMonthEmployeeAmountReimbursementForThisItemInProgress", IdosConstants.decimalFormat
							.format(thisMonthEmployeeAmountReimbursementForThisItemInProgress));
					reimbursementAmountDataAn.add(row);
					criterias.clear();
					criterias.put("specifics.id", specf.getId());
					criterias.put("particulars.id", specf.getParticularsId().getId());
					criterias.put("presentStatus", 1);
					List<SpecificsKnowledgeLibrary> specfKlList = genericDao
							.findByCriteria(SpecificsKnowledgeLibrary.class, criterias, em);
					if (!specfKlList.isEmpty() && specfKlList.size() > 0) {
						String selBranch = json.findValue("selBranch") != null ? json.findValue("selBranch").asText()
								: null;
						if (selBranch != null && !selBranch.equals("")) {
							Branch bnch = Branch.findById(Long.parseLong(selBranch));
							for (SpecificsKnowledgeLibrary specfKl : specfKlList) {
								criterias.clear();
								criterias.put("branch.id", bnch.getId());
								criterias.put("organization.id", bnch.getOrganization().getId());
								criterias.put("specifics.id", specf.getId());
								criterias.put("particulars.id", specf.getParticularsId().getId());
								criterias.put("specificsKl.id", specfKl.getId());
								criterias.put("presentStatus", 1);
								SpecificsKnowledgeLibraryForBranch specfKlBnch = genericDao
										.getByCriteria(SpecificsKnowledgeLibraryForBranch.class, criterias, em);
								if (specfKlBnch != null) {
									result.put("expenseitemklresult", true);
									ObjectNode specfrow = Json.newObject();
									specfrow.put("klContent",
											specfKlBnch.getSpecificsKl().getKnowledgeLibraryContent());
									specfrow.put("klIsMandatory", specfKlBnch.getSpecificsKl().getIsMandatory());
									expenseItemSpecfKlDataAn.add(specfrow);
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
}
