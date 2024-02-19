package com.idos.dao;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import play.libs.Json;
import service.*;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.db.jpa.JPAApi;
import javax.inject.Inject;

public class ClaimItemDetailsDAOImpl implements ClaimItemDetailsDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public boolean saveClaimItemDetails(Users user, JsonNode json, EntityManager entityManager,
			ClaimTransaction claimTransaction, ClaimsSettlement claimsSettlement) throws IDOSException {

		try {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, " >>>>>>>>>> Inserting Clain Details " + json);
			}

			if (claimsSettlement.getItemName().equals(IdosConstants.TRAVEL_EXPENSES)) {
				String expenseDetails = json.findValue("expenseDetails").toString();
				JSONArray expenseDetailsArray = new JSONArray(expenseDetails);
				for (int i = 0; i < expenseDetailsArray.length(); i++) {
					JSONObject rowItemData = new JSONObject(expenseDetailsArray.get(i).toString());
					saveClaimDetailsRow(user, rowItemData, IdosConstants.TRAVEL_EXPENSES, claimTransaction,
							entityManager, claimsSettlement.getId());
				}

			} else if (claimsSettlement.getItemName().equals(IdosConstants.BOARDING_LODGING)) {
				String lodgingAndBoardDetails = json.findValue("lodgingAndBoardDetails").toString();
				JSONArray lodgingAndBoardDetailsArray = new JSONArray(lodgingAndBoardDetails);
				for (int i = 0; i < lodgingAndBoardDetailsArray.length(); i++) {
					JSONObject rowItemData = new JSONObject(lodgingAndBoardDetailsArray.get(i).toString());
					saveClaimDetailsRow(user, rowItemData, IdosConstants.BOARDING_LODGING, claimTransaction,
							entityManager, claimsSettlement.getId());
				}
			} else if (claimsSettlement.getItemName().equals(IdosConstants.OTHER_EXPENSES)) {
				String otherExpensesDetails = json.findValue("otherExpensesDetails").toString();
				JSONArray otherExpensesDetailsArray = new JSONArray(otherExpensesDetails);
				for (int i = 0; i < otherExpensesDetailsArray.length(); i++) {
					JSONObject rowItemData = new JSONObject(otherExpensesDetailsArray.get(i).toString());
					saveClaimDetailsRow(user, rowItemData, IdosConstants.OTHER_EXPENSES, claimTransaction,
							entityManager, claimsSettlement.getId());
				}
			} else if (claimsSettlement.getItemName().equals(IdosConstants.FIXED_PER_DIAM)) {
				String fixedPerDiamDetails = json.findValue("fixedPerDiamDetails").toString();
				JSONArray fixedPerDiamDetailsArray = new JSONArray(fixedPerDiamDetails);
				for (int i = 0; i < fixedPerDiamDetailsArray.length(); i++) {
					JSONObject rowItemData = new JSONObject(fixedPerDiamDetailsArray.get(i).toString());
					saveClaimDetailsRow(user, rowItemData, IdosConstants.FIXED_PER_DIAM, claimTransaction,
							entityManager, claimsSettlement.getId());
				}
			} else if (claimsSettlement.getItemName().equals(IdosConstants.INCURRED_EXPENCES)) {
				String incurredExpensesDetails = json.findValue("incurredExpensesDetails").toString();
				JSONArray incurredExpensesDetailssArray = new JSONArray(incurredExpensesDetails);
				for (int i = 0; i < incurredExpensesDetailssArray.length(); i++) {
					JSONObject rowItemData = new JSONObject(incurredExpensesDetailssArray.get(i).toString());
					saveClaimDetailsRow(user, rowItemData, IdosConstants.INCURRED_EXPENCES, claimTransaction,
							entityManager, claimsSettlement.getId());
				}
			}
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, " >>>>>>>>>> End Clain Details Insertion");
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on save/update multiitems.", ex.getMessage());
		}
		return false;
	}

	@Override
	public boolean saveClaimDetailsRow(Users user, JSONObject rowItemData, String itemCategory,
			ClaimTransaction claimTransaction, EntityManager entityManager, Long claimsSettlementId)
			throws IDOSException {
		try {
			ClaimItemDetails claimItemDetails = null;
			String itemId = rowItemData.get("claimDetailsId") == null
					|| "".equals(rowItemData.getString("claimDetailsId")) ? null
							: rowItemData.getString("claimDetailsId");
			String claimItem = rowItemData.get("claimItem") == null || "".equals(rowItemData.getString("claimItem"))
					? null
					: rowItemData.getString("claimItem");
			String vendorName = rowItemData.get("vendorName") == null || "".equals(rowItemData.getString("vendorName"))
					? null
					: rowItemData.getString("vendorName");
			String isRegistered = rowItemData.get("isRegistered") == null
					|| "".equals(rowItemData.getString("isRegistered")) ? null : rowItemData.getString("isRegistered");
			String vendorGstin = rowItemData.get("vendorGstin") == null
					|| "".equals(rowItemData.getString("vendorGstin")) ? null : rowItemData.getString("vendorGstin");
			String vendorState = rowItemData.get("vendorState") == null
					|| "".equals(rowItemData.getString("vendorState")) ? null : rowItemData.getString("vendorState");
			String invoiceNo = rowItemData.get("invoiceNo") == null || "".equals(rowItemData.getString("invoiceNo"))
					? null
					: rowItemData.getString("invoiceNo");
			String invoiceDate = rowItemData.get("invoiceDate") == null
					|| "".equals(rowItemData.getString("invoiceDate")) ? null : rowItemData.getString("invoiceDate");
			// String itemName = rowItemData.get("itemName")== null ||
			// "".equals(rowItemData.getString("itemName")) ? null :
			// rowItemData.getString("itemName");
			// String hsnCode = rowItemData.get("hsnCode")== null ||
			// "".equals(rowItemData.getString("hsnCode")) ? null :
			// rowItemData.getString("hsnCode");
			// String productDesc = rowItemData.get("productDesc")== null ||
			// "".equals(rowItemData.getString("productDesc")) ? null :
			// rowItemData.getString("productDesc");
			String uqc = rowItemData.get("uqc") == null || "".equals(rowItemData.getString("uqc")) ? null
					: rowItemData.getString("uqc");
			String quantity = rowItemData.get("quantity") == null || "".equals(rowItemData.getString("quantity")) ? null
					: rowItemData.getString("quantity");
			String rate = rowItemData.get("rate") == null || "".equals(rowItemData.getString("rate")) ? null
					: rowItemData.getString("rate");
			String grossAmt = rowItemData.get("grossAmt") == null || "".equals(rowItemData.getString("grossAmt")) ? null
					: rowItemData.getString("grossAmt");
			Long sgstId = rowItemData.get("sgstID") == null || "".equals(rowItemData.getString("sgstID")) ? null
					: rowItemData.getLong("sgstID");
			Double sgstRate = rowItemData.get("sgstRate") == null || "".equals(rowItemData.getString("sgstRate")) ? null
					: rowItemData.getDouble("sgstRate");
			Double sgstAmt = rowItemData.get("sgstAmt") == null || "".equals(rowItemData.getString("sgstAmt")) ? null
					: rowItemData.getDouble("sgstAmt");
			Long cgstId = rowItemData.get("cgstID") == null || "".equals(rowItemData.getString("cgstID")) ? null
					: rowItemData.getLong("cgstID");
			Double cgstRate = rowItemData.get("cgstRate") == null || "".equals(rowItemData.getString("cgstRate")) ? null
					: rowItemData.getDouble("cgstRate");
			Double cgstAmt = rowItemData.get("cgstAmt") == null || "".equals(rowItemData.getString("cgstAmt")) ? null
					: rowItemData.getDouble("cgstAmt");
			Long igstId = rowItemData.get("igstID") == null || "".equals(rowItemData.getString("igstID")) ? null
					: rowItemData.getLong("igstID");
			Double igstRate = rowItemData.get("igstRate") == null || "".equals(rowItemData.getString("igstRate")) ? null
					: rowItemData.getDouble("igstRate");
			Double igstAmt = rowItemData.get("igstAmt") == null || "".equals(rowItemData.getString("igstAmt")) ? null
					: rowItemData.getDouble("igstAmt");
			Long cessId = rowItemData.get("cessID") == null || "".equals(rowItemData.getString("cessID")) ? null
					: rowItemData.getLong("cessID");
			Double cessRate = rowItemData.get("cessRate") == null || "".equals(rowItemData.getString("cessRate")) ? null
					: rowItemData.getDouble("cessRate");
			Double cessAmt = rowItemData.get("cessAmt") == null || "".equals(rowItemData.getString("cessAmt")) ? null
					: rowItemData.getDouble("cessAmt");
			Double netAmt = rowItemData.get("netAmt") == null || "".equals(rowItemData.getString("netAmt")) ? null
					: rowItemData.getDouble("netAmt");

			if (itemId != null) {
				ClaimItemDetails clmDetails = ClaimItemDetails.findById(entityManager, Long.parseLong(itemId));
				if (clmDetails != null) {
					claimItemDetails = clmDetails;
				} else {
					claimItemDetails = new ClaimItemDetails();
				}
			} else {
				claimItemDetails = new ClaimItemDetails();
			}
			if (claimItem != null) {
				claimItemDetails.setClaimSpecific(Long.parseLong(claimItem));
			}
			claimItemDetails.setOrganization(user.getOrganization());
			claimItemDetails.setBranch(user.getBranch());
			claimItemDetails.setTransaction(claimTransaction);
			claimItemDetails.setVendorName(vendorName);
			if (claimsSettlementId != null) {
				claimItemDetails.setClaimSettlementId(claimsSettlementId);
			}

			if (isRegistered != null) {
				claimItemDetails.setIsRegistered(Integer.parseInt(isRegistered));
			} else {
				claimItemDetails.setIsRegistered(0);
			}
			claimItemDetails.setVendorGstin(vendorGstin);
			if (vendorState != null) {
				String state = IdosConstants.STATE_CODE_MAPPING.get(vendorState);
				claimItemDetails.setVendorState(state);
			}
			claimItemDetails.setInvoiceBillRefNo(invoiceNo);
			if (invoiceDate != null) {
				claimItemDetails.setInvoiceBillRefDate(IdosConstants.IDOSDF.parse(invoiceDate));
			}
			// claimItemDetails.setItemServiceName(itemName);
			// claimItemDetails.setHsnOrSacCode(hsnCode);
			// claimItemDetails.setProductServiceDesc(productDesc);
			claimItemDetails.setUqc(uqc);
			if (quantity != null) {
				claimItemDetails.setQuantity(Double.parseDouble(quantity));
			}
			if (rate != null) {
				claimItemDetails.setRate(Double.parseDouble(rate));
			}
			if (grossAmt != null) {
				claimItemDetails.setGrossAmt(Double.parseDouble(grossAmt));
			}
			if (netAmt != null) {
				claimItemDetails.setNetAmount(netAmt);
			}
			if (sgstId != null) {
				claimItemDetails.setSgstId(sgstId);
				if (sgstRate != null) {
					claimItemDetails.setSgstRate(sgstRate);
				}
				if (sgstAmt != null) {
					claimItemDetails.setSgstAmt(sgstAmt);
				}
			}
			if (cgstId != null) {
				claimItemDetails.setCgstId(cgstId);
				if (cgstRate != null) {
					claimItemDetails.setCgstRate(cgstRate);
				}
				if (cgstAmt != null) {
					claimItemDetails.setCgstAmt(cgstAmt);
				}
			}

			if (igstId != null) {
				claimItemDetails.setIgstId(igstId);
				if (igstRate != null) {
					claimItemDetails.setIgstRate(igstRate);
				}
				if (igstAmt != null) {
					claimItemDetails.setIgstAmt(igstAmt);
				}
			}
			if (cessId != null) {
				claimItemDetails.setCessId(cessId);
				if (cessRate != null) {
					claimItemDetails.setCessRate(cessRate);
				}
				if (cessAmt != null) {
					claimItemDetails.setCessAmt(cessAmt);
				}
			}
			claimItemDetails.setItemCategory(itemCategory);
			genericDao.saveOrUpdate(claimItemDetails, user, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on save/update multiitems.", ex.getMessage());
		}
		return true;
	}

	@Override
	public ArrayNode getClaimDetails(ArrayNode dataList, JsonNode json, Users user, EntityManager entityManager,
			ClaimTransaction claimTransaction, String itemCategory) throws IDOSException {

		if (claimTransaction != null || claimTransaction.getId() != null) {
			Map<String, Object> criterias = new HashMap<String, Object>();
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("branch.id", user.getBranch().getId());
			criterias.put("transaction.id", claimTransaction.getId());
			criterias.put("itemCategory", itemCategory);
			criterias.put("presentStatus", 1);

			List<ClaimItemDetails> claimItemDetailsList = genericDao.findByCriteria(ClaimItemDetails.class, criterias,
					entityManager);
			if (claimItemDetailsList != null && !claimItemDetailsList.isEmpty()) {
				for (ClaimItemDetails claimItemDetails : claimItemDetailsList) {
					ObjectNode row = Json.newObject();
					String claimDetailsId = (claimItemDetails.getId() != null) ? claimItemDetails.getId().toString()
							: "";
					String claimItem = (claimItemDetails.getClaimSpecific() != null
							&& claimItemDetails.getClaimSpecific() != null)
									? claimItemDetails.getClaimSpecific().toString()
									: "";
					String vendorName = (claimItemDetails.getVendorName() != null) ? claimItemDetails.getVendorName()
							: "";
					String isRegistered = (claimItemDetails.getIsRegistered() != null)
							? claimItemDetails.getIsRegistered().toString()
							: "";
					String vendorGstin = (claimItemDetails.getVendorGstin() != null) ? claimItemDetails.getVendorGstin()
							: "";
					String vendorState = (claimItemDetails.getVendorState() != null) ? claimItemDetails.getVendorState()
							: "";
					String invoiceNo = (claimItemDetails.getInvoiceBillRefNo() != null)
							? claimItemDetails.getInvoiceBillRefNo()
							: "";
					String invoiceDate = (claimItemDetails.getInvoiceBillRefDate() != null)
							? IdosConstants.IDOSDF.format(claimItemDetails.getInvoiceBillRefDate())
							: "";
					// String itemName = (claimItemDetails.getItemServiceName() != null) ?
					// claimItemDetails.getItemServiceName() : "";
					// String hsnCode = (claimItemDetails.getHsnOrSacCode() != null) ?
					// claimItemDetails.getHsnOrSacCode() : "";
					String uqc = (claimItemDetails.getUqc() != null) ? claimItemDetails.getUqc() : "";
					// String productDesc = (claimItemDetails.getProductServiceDesc() != null) ?
					// claimItemDetails.getProductServiceDesc() : "";
					String quantity = (claimItemDetails.getQuantity() != null)
							? claimItemDetails.getQuantity().toString()
							: "";
					String rate = (claimItemDetails.getRate() != null) ? claimItemDetails.getRate().toString() : "";
					String grossAmt = (claimItemDetails.getGrossAmt() != null)
							? claimItemDetails.getGrossAmt().toString()
							: "";
					Double sgstRate = (claimItemDetails.getSgstRate() != null) ? claimItemDetails.getSgstRate() : 0d;
					String sgstAmt = (claimItemDetails.getSgstAmt() != null) ? claimItemDetails.getSgstAmt().toString()
							: "";
					Double cgstRate = (claimItemDetails.getCgstRate() != null) ? claimItemDetails.getCgstRate() : 0d;
					String cgstAmt = (claimItemDetails.getCgstAmt() != null) ? claimItemDetails.getCgstAmt().toString()
							: "";
					Double igstRate = (claimItemDetails.getIgstRate() != null) ? claimItemDetails.getIgstRate() : 0d;
					String igstAmt = (claimItemDetails.getIgstAmt() != null) ? claimItemDetails.getIgstAmt().toString()
							: "";
					Double cessRate = (claimItemDetails.getCessRate() != null) ? claimItemDetails.getCessRate() : 0d;
					String cessAmt = (claimItemDetails.getCessAmt() != null) ? claimItemDetails.getCessAmt().toString()
							: "";
					String sgstId = (claimItemDetails.getSgstId() != null) ? claimItemDetails.getSgstId().toString()
							: "";
					String cgstId = (claimItemDetails.getCgstId() != null) ? claimItemDetails.getCgstId().toString()
							: "";
					String igstId = (claimItemDetails.getIgstId() != null) ? claimItemDetails.getIgstId().toString()
							: "";
					String cessId = (claimItemDetails.getCessId() != null) ? claimItemDetails.getCessId().toString()
							: "";
					String netAmt = (claimItemDetails.getNetAmount() != null)
							? claimItemDetails.getNetAmount().toString()
							: "";
					row.put("claimDetailsId", claimDetailsId);
					row.put("claimItem", claimItem);
					row.put("vendorName", vendorName);
					row.put("isRegistered", isRegistered);
					row.put("vendorGstin", vendorGstin);
					row.put("vendorState", vendorState);
					row.put("invoiceNo", invoiceNo);
					row.put("invoiceDate", invoiceDate);
					// row.put("itemName", itemName);
					// row.put("hsnCode", hsnCode);
					// row.put("productDesc", productDesc);
					row.put("uqc", uqc);
					row.put("quantity", quantity);
					row.put("rate", rate);
					row.put("grossAmt", grossAmt);
					row.put("netAmt", netAmt);
					if (sgstRate != 0d) {
						row.put("sgstRate", sgstRate);
					} else {
						row.put("sgstRate", "");
					}
					row.put("sgstAmt", sgstAmt);
					if (cgstRate != 0d) {
						row.put("cgstRate", cgstRate);
					} else {
						row.put("cgstRate", "");
					}
					row.put("cgstAmt", cgstAmt);
					if (igstRate != 0d) {
						row.put("igstRate", igstRate);
					} else {
						row.put("igstRate", "");
					}
					row.put("igstAmt", igstAmt);
					if (cessRate != 0d) {
						row.put("cessRate", cessRate);
					} else {
						row.put("cessRate", "");
					}
					row.put("cessAmt", cessAmt);
					row.put("cgstId", cgstId);
					row.put("sgstId", sgstId);
					row.put("igstId", igstId);
					row.put("cessId", cessId);
					dataList.add(row);
				}
			}
		}
		return dataList;
	}

	@Override
	public ObjectNode paymentForClaims(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* Start Claim Payments");
		}
		BranchCashService branchCashService = new BranchCashServiceImpl();
		BranchBankService branchBankService = new BranchBankServiceImpl();
		TrialBalanceService trialBalanceService = new TrialBalanceServiceImpl();
		entitytransaction.begin();
		String transactionPrimId = json.findValue("transactionPrimId").asText();
		String txnInstrumentNumber = null;
		String txnInstrumentDate = null;
		Double settlementAmount = 0.0;
		String payMode = null;
		ClaimTransaction claimTransaction = ClaimTransaction.findById(Long.parseLong(transactionPrimId));
		if (claimTransaction.getTransactionPurpose().getId() == 16
				|| claimTransaction.getTransactionPurpose().getId() == 18) {
			if (claimTransaction.getClaimsDueSettlement() > 0) {
				settlementAmount = claimTransaction.getClaimsDueSettlement();
			} else {
				settlementAmount = claimTransaction.getClaimsRequiredSettlement();
			}
		} else {
			settlementAmount = claimTransaction.getClaimsDueSettlement();
		}

		int txnreceiptdetails = json.findValue("paymentDetails") != null ? json.findValue("paymentDetails").asInt() : 0;
		String txnreceipttypebankdetails = json.findValue("bankInf").asText();
		claimTransaction.setReceiptDetailType(txnreceiptdetails);
		if (txnreceiptdetails == IdosConstants.PAYMODE_CASH) {
			Double resultantCash = branchCashService.updateBranchCashDetail(entityManager, user,
					claimTransaction.getTransactionBranch(), settlementAmount, true,
					claimTransaction.getTransactionDate(), result);
			result.put("resultantCash", resultantCash);
			if (resultantCash < 0) {
				return result;
			}
			trialBalanceService.addTrialBalanceForCash(user, entityManager, genericDao, claimTransaction,
					settlementAmount, true);
			CLAIM_DAO.addAdvanceToUserAccount(claimTransaction, user);
			payMode = "CASH";
		} else if (txnreceiptdetails == IdosConstants.PAYMODE_BANK) {
			// Branch Bank Balance Deduction As Travel Advance Is Lended To The Employee
			// Acount
			Long txnreceiptPaymentBank = json.findValue("txnPaymentBank") != null
					|| !"".equals(json.findValue("txnPaymentBank")) ? json.findValue("txnPaymentBank").asLong() : null;
			txnInstrumentNumber = json.findValue("txnInstrumentNum") != null
					? json.findValue("txnInstrumentNum").asText()
					: null;
			txnInstrumentDate = json.findValue("txnInstrumentDate") != null
					? json.findValue("txnInstrumentDate").asText()
					: null;
			if (txnInstrumentNumber != null && !"".equals(txnInstrumentNumber)) {
				claimTransaction.setInstrumentNumber(txnInstrumentNumber);
			}
			if (txnInstrumentDate != null && !"".equals(txnInstrumentDate)) {
				claimTransaction.setInstrumentDate(txnInstrumentDate);
			}
			if (txnreceiptPaymentBank != null) {
				BranchBankAccounts bankAccount = BranchBankAccounts.findById(txnreceiptPaymentBank);
				if (bankAccount == null) {
					throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
							IdosConstants.INVALID_DATA_EXCEPTION,
							"Bank is not selected in transaction when payment mode is Bank.");
				}
				claimTransaction.setTransactionBranchBankAccount(bankAccount);
				boolean branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(entityManager,
						user, bankAccount, settlementAmount, true, result, claimTransaction.getTransactionDate(),
						claimTransaction.getTransactionBranch());
				if (!branchBankDetailEntered) {
					return result; // since balance is in -ve don't make any changes in DB
				}
				trialBalanceService.addTrialBalanceForBank(user, entityManager, genericDao, claimTransaction,
						settlementAmount, true);
				CLAIM_DAO.addAdvanceToUserAccount(claimTransaction, user);
			}
			payMode = "BANK";
		}
		TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserClaim(claimTransaction, user, entityManager,
				claimTransaction.getNewAmount(), false);
		claimTransaction.setReceiptDetailsDescription(txnreceipttypebankdetails);
		claimTransaction.setSettlementStatus("SETTLED");
		if (claimTransaction.getTransactionPurpose().getId() == IdosConstants.SETTLE_TRAVEL_ADVANCE
				|| claimTransaction.getTransactionPurpose().getId() == IdosConstants.SETTLE_ADVANCE_FOR_EXPENSE) {
			if (claimTransaction.getClaimsRequiredSettlement() > 0
					&& settlementAmount == claimTransaction.getClaimsRequiredSettlement()) {
				claimTransaction.setClaimsRequiredSettlement(0d);
			}
			claimTransaction.setSettlementStatus("SETTLED");
		}
		claimTransaction.setTransactionDate(new Date());
		claimTransaction.setModifiedBy(user);
		genericDao.saveOrUpdate(claimTransaction, user, entityManager);

		PaidClaimsDetails paidClaim = new PaidClaimsDetails();
		paidClaim.setOrganization(user.getOrganization());
		paidClaim.setTransaction(claimTransaction);
		paidClaim.setPaidAmt(settlementAmount);
		paidClaim.setPaymentDate(new Date());
		paidClaim.setPayMode(payMode);
		genericDao.saveOrUpdate(paidClaim, user, entityManager);

		entitytransaction.commit();
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "************* END Claim Payments");
		}
		return result;
	}
}
