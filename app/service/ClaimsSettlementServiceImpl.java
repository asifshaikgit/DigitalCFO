/* Project: IDOS 1.0
 * Module: Travel Advance Settlement
 * Filename: ClaimsSettlementServiceImpl.java
 * Component Realisation: Java Class
 * Prepared By: Sunil Namdev
 * Description: Modules to advance travel settlement
 * Copyright (c) 2016 IDOS

 * MODIFICATION HISTORY
 * Version		Date		   	Author		      Remarks
 * -------------------------------------------------------------------------
 *  0.1  Aug 30, 2016	                  		  - Initial Version
 * -------------------------------------------------------------------------
 */

package service;

import actor.CreatorActor;
import com.idos.util.CodeHelper;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import controllers.Karvy.KarvyAuthorization;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.db.jpa.JPAApi;
import play.libs.Json;
import play.mvc.WebSocket;
import pojo.TransactionViewResponse;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import akka.stream.javadsl.*;
import akka.actor.*;
import akka.NotUsed;
import java.util.logging.Level;

/**
 * Created by Sunil Namdev on 31-08-2016.
 */
public class ClaimsSettlementServiceImpl implements ClaimsSettlementService {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public ObjectNode submitForApproval(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
            EntityTransaction entitytransaction) throws IDOSException {
        Map<String, Object> criterias = new HashMap<String, Object>();
        entitytransaction.begin();
        Long txnEntityID = (json.findValue("txnEntityID") == null || "".equals(json.findValue("txnEntityID"))) ? 0l
                : json.findValue("txnEntityID").asLong();
        String claimTxnPurposeVal = json.findValue("claimTxnPurposeVal") != null
                ? json.findValue("claimTxnPurposeVal").asText()
                : null;
        String settlementClaimavailableUnsettledClaimAdvances = json
                .findValue("settlementClaimavailableUnsettledClaimAdvances") != null
                        ? json.findValue("settlementClaimavailableUnsettledClaimAdvances").asText()
                        : null;
        String settlementClaimunsettledClaimAdvancesDetails = json
                .findValue("settlementClaimunsettledClaimAdvancesDetails") != null
                        ? json.findValue("settlementClaimunsettledClaimAdvancesDetails").asText()
                        : null;
        String settlementClaimuserExpenditureOnThisTxn = json
                .findValue("settlementClaimuserExpenditureOnThisTxn") != null
                        ? json.findValue("settlementClaimuserExpenditureOnThisTxn").asText()
                        : null;
        String settlementClaimtotalExpensesIncurredOnThisTxn = json
                .findValue("settlementClaimtotalExpensesIncurredOnThisTxn") != null
                        ? json.findValue("settlementClaimtotalExpensesIncurredOnThisTxn").asText()
                        : null;
        String settlementClaimdueFromCompany = json.findValue("settlementClaimdueFromCompany") != null
                ? json.findValue("settlementClaimdueFromCompany").asText()
                : null;
        String settlementClaimdueToCompany = json.findValue("settlementClaimdueToCompany") != null
                ? json.findValue("settlementClaimdueToCompany").asText()
                : null;
        String settlementClaimamountReturnInCaseOfDueToCompany = json
                .findValue("settlementClaimamountReturnInCaseOfDueToCompany") != null
                        ? json.findValue("settlementClaimamountReturnInCaseOfDueToCompany").asText()
                        : null;
        String settlementClaimupdatedUnsettledAmount = json.findValue("settlementClaimupdatedUnsettledAmount") != null
                ? json.findValue("settlementClaimupdatedUnsettledAmount").asText()
                : null;
        String settlementClaimtxnRemarks = json.findValue("settlementClaimtxnRemarks") != null
                ? json.findValue("settlementClaimtxnRemarks").asText()
                : null;
        String settlementClaimsupportingDoc = json.findValue("settlementClaimsupportingDoc") != null
                ? json.findValue("settlementClaimsupportingDoc").asText()
                : null;

        ClaimTransaction previousClaimTxn = null;
        String txnRemarks = null;
        String txnDocument = "";
        String debitCredit = "";
        TransactionPurpose txnPurpose = null;
        ClaimTransaction newClaimTransaction = null;
        if (txnEntityID > 0) {
            newClaimTransaction = ClaimTransaction.findById(txnEntityID);
        } else {
            newClaimTransaction = new ClaimTransaction();
        }
        // ClaimTransaction newClaimTransaction = new ClaimTransaction();
        if (settlementClaimavailableUnsettledClaimAdvances != null
                && !settlementClaimavailableUnsettledClaimAdvances.equals("")) {
            previousClaimTxn = ClaimTransaction
                    .findById(Long.parseLong(settlementClaimavailableUnsettledClaimAdvances));
            newClaimTransaction.setClaimsSettlementRefNumber(previousClaimTxn.getTransactionRefNumber());
        }
        newClaimTransaction.setTransactionBranch(previousClaimTxn.getTransactionBranch());
        newClaimTransaction.setTransactionBranchOrganization(previousClaimTxn.getTransactionBranchOrganization());
        newClaimTransaction.setTransactionProject(previousClaimTxn.getTransactionProject());
        newClaimTransaction.setTravelType(previousClaimTxn.getTravelType());
        newClaimTransaction.setNumberOfPlacesToVisit(previousClaimTxn.getNumberOfPlacesToVisit());
        newClaimTransaction.setTravelFromToPlaces(previousClaimTxn.getTravelFromToPlaces());
        newClaimTransaction.setTypeOfCity(previousClaimTxn.getTypeOfCity());
        newClaimTransaction.setAppropriateDistance(previousClaimTxn.getAppropriateDistance());
        newClaimTransaction.setTotalDays(previousClaimTxn.getTotalDays());
        newClaimTransaction.setTravelEligibilityDetails(previousClaimTxn.getTravelEligibilityDetails());
        newClaimTransaction.setAdvanceEligibilityDetails(previousClaimTxn.getAdvanceEligibilityDetails());
        newClaimTransaction.setPurposeOfVisit(previousClaimTxn.getPurposeOfVisit());
        newClaimTransaction.setExistingClaimsCurrentSettlementDetails(settlementClaimunsettledClaimAdvancesDetails);
        newClaimTransaction.setUserExpenditureOnThisTxn(settlementClaimuserExpenditureOnThisTxn);
        newClaimTransaction.setTransactionDate(Calendar.getInstance().getTime());
        if (claimTxnPurposeVal != null && !claimTxnPurposeVal.equals("")) {
            txnPurpose = TransactionPurpose.findById(Long.parseLong(claimTxnPurposeVal));
        }
        newClaimTransaction.setTransactionPurpose(txnPurpose);
        if (settlementClaimtotalExpensesIncurredOnThisTxn != null
                && !settlementClaimtotalExpensesIncurredOnThisTxn.equals("")) {
            newClaimTransaction
                    .setClaimsNetSettlement(Double.parseDouble(settlementClaimtotalExpensesIncurredOnThisTxn));
            newClaimTransaction.setGrossAmount(Double.parseDouble(settlementClaimtotalExpensesIncurredOnThisTxn));
            newClaimTransaction.setNewAmount(Double.parseDouble(settlementClaimtotalExpensesIncurredOnThisTxn));
        }
        if (settlementClaimdueFromCompany != null && !settlementClaimdueFromCompany.equals("")) {
            newClaimTransaction.setClaimsRequiredSettlement(Double.parseDouble(settlementClaimdueFromCompany));
        }
        if (settlementClaimdueToCompany != null && !settlementClaimdueToCompany.equals("")) {
            newClaimTransaction.setClaimsReturnSettlement(Double.parseDouble(settlementClaimdueToCompany));
        }
        if (settlementClaimamountReturnInCaseOfDueToCompany != null
                && !settlementClaimamountReturnInCaseOfDueToCompany.equals("")) {
            newClaimTransaction.setAmountReturnInCaseOfDueToCompany(
                    Double.parseDouble(settlementClaimamountReturnInCaseOfDueToCompany));
        }
        if (settlementClaimupdatedUnsettledAmount != null && !settlementClaimupdatedUnsettledAmount.equals("")) {
            newClaimTransaction.setClaimsDueSettlement(Double.parseDouble(settlementClaimupdatedUnsettledAmount));
        }
        String accountantEmailsStr = "";
        criterias.clear();
        criterias.put("role.id", 5l);
        criterias.put("organization.id", user.getOrganization().getId());
        criterias.put("presentStatus", 1);
        List<UsersRoles> accountantRole = genericDAO.findByCriteria(UsersRoles.class, criterias, entityManager);
        for (UsersRoles usrRoles : accountantRole) {
            accountantEmailsStr += usrRoles.getUser().getEmail() + ",";
        }
        newClaimTransaction.setAccountantEmails(accountantEmailsStr);
        if (settlementClaimtxnRemarks != null && !settlementClaimtxnRemarks.equals("")) {
            if (newClaimTransaction.getTxnRemarks() != null) {
                txnRemarks = newClaimTransaction.getTxnRemarks() + "," + user.getEmail() + "#"
                        + settlementClaimtxnRemarks;
                newClaimTransaction.setTxnRemarks(txnRemarks);
            } else {
                txnRemarks = user.getEmail() + "#" + settlementClaimtxnRemarks;
                newClaimTransaction.setTxnRemarks(txnRemarks);
            }
        }
        newClaimTransaction.setSupportingDocuments(
                transactionDao.getAndDeleteSupportingDocument(newClaimTransaction.getSupportingDocuments(),
                        user.getEmail(), settlementClaimsupportingDoc, user, entityManager));
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
        genericDAO.saveOrUpdate(newClaimTransaction, user, entityManager);
        FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, settlementClaimsupportingDoc,
                newClaimTransaction.getId(), IdosConstants.CLAIM_TXN_TYPE);
        Double claimTxnTravelExpenses = 0.0;
        Double travelExpenceTotalAmt = 0.0;
        Double travelExpenceTotalTax = 0.0;
        if (json.findValue("claimTxnTravelExpenses") != null && !"".equals(json.findValue("claimTxnTravelExpenses"))) {
            claimTxnTravelExpenses = json.findValue("claimTxnTravelExpenses").asDouble();
            travelExpenceTotalAmt = json.findValue("travelExpenceTotalAmt").asDouble();
            travelExpenceTotalTax = json.findValue("travelExpenceTotalTax").asDouble();
            ClaimsSettlement claimsSettlement = new ClaimsSettlement();
            claimsSettlement.setTransaction(newClaimTransaction);
            claimsSettlement.setItemName(IdosConstants.TRAVEL_EXPENSES);
            claimsSettlement.setItemValue(claimTxnTravelExpenses);
            claimsSettlement.setItemGross(travelExpenceTotalAmt);
            claimsSettlement.setItemTax(travelExpenceTotalTax);
            claimsSettlement.setOrganization(newClaimTransaction.getTransactionBranchOrganization());
            claimsSettlement.setBranch(newClaimTransaction.getTransactionBranch());
            Specifics specifics = Specifics.findByOrganizationAndMappingID(entityManager, user.getOrganization(),
                    IdosConstants.TRAVEL_EXPENSES_MAPPING_ID);
            if (specifics == null) {
                throw new IDOSException(IdosConstants.COA_MAPPING_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        "COA Mapping is not found for Travel Expenses.", IdosConstants.COA_MAPPING_EXCEPTION);
            }
            claimsSettlement.setTransactionSpecifics(specifics);
            genericDAO.saveOrUpdate(claimsSettlement, user, entityManager);

            if ((newClaimTransaction != null && newClaimTransaction.getId() != null)
                    && (claimsSettlement != null && claimsSettlement.getId() != null)) {
                CLAIM_DETAILS_DAO.saveClaimItemDetails(user, json, entityManager, newClaimTransaction,
                        claimsSettlement);
            }
        }
        Double claimTxnBoardingLodging = 0.0;
        Double lnbExpenceTotalAmt = 0.0;
        Double lnbExpenceTotalTax = 0.0;
        if (json.findValue("claimTxnBoardingLodging") != null
                && !"".equals(json.findValue("claimTxnBoardingLodging"))) {
            claimTxnBoardingLodging = json.findValue("claimTxnBoardingLodging").asDouble();
            lnbExpenceTotalAmt = json.findValue("lnbExpenceTotalAmt").asDouble();
            lnbExpenceTotalTax = json.findValue("lnbExpenceTotalTax").asDouble();
            ClaimsSettlement claimsSettlement = new ClaimsSettlement();
            claimsSettlement.setTransaction(newClaimTransaction);
            claimsSettlement.setItemName(IdosConstants.BOARDING_LODGING);
            claimsSettlement.setItemValue(claimTxnBoardingLodging);
            claimsSettlement.setItemGross(lnbExpenceTotalAmt);
            claimsSettlement.setItemTax(lnbExpenceTotalTax);
            claimsSettlement.setOrganization(newClaimTransaction.getTransactionBranchOrganization());
            claimsSettlement.setBranch(newClaimTransaction.getTransactionBranch());
            Specifics specifics = Specifics.findByOrganizationAndMappingID(entityManager, user.getOrganization(),
                    IdosConstants.BOARDING_LODGING_MAPPING_ID);
            if (specifics == null) {
                throw new IDOSException(IdosConstants.COA_MAPPING_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        "COA Mapping is not found for Boarding & Lodging", IdosConstants.COA_MAPPING_EXCEPTION);
            }
            claimsSettlement.setTransactionSpecifics(specifics);
            genericDAO.saveOrUpdate(claimsSettlement, user, entityManager);
            if ((newClaimTransaction != null && newClaimTransaction.getId() != null)
                    && (claimsSettlement != null && claimsSettlement.getId() != null)) {
                CLAIM_DETAILS_DAO.saveClaimItemDetails(user, json, entityManager, newClaimTransaction,
                        claimsSettlement);
            }
        }
        Double claimTxnOtherExpenses = 0.0;
        Double otherExpenceTotalAmt = 0.0;
        Double otherExpenceTotalTax = 0.0;
        if (json.findValue("claimTxnOtherExpenses") != null && !"".equals(json.findValue("claimTxnOtherExpenses"))) {
            claimTxnOtherExpenses = json.findValue("claimTxnOtherExpenses").asDouble();
            otherExpenceTotalAmt = json.findValue("otherExpenceTotalAmt").asDouble();
            otherExpenceTotalTax = json.findValue("otherExpenceTotalTax").asDouble();
            ClaimsSettlement claimsSettlement = new ClaimsSettlement();
            claimsSettlement.setTransaction(newClaimTransaction);
            claimsSettlement.setItemName(IdosConstants.OTHER_EXPENSES);
            claimsSettlement.setItemValue(claimTxnOtherExpenses);
            claimsSettlement.setItemGross(otherExpenceTotalAmt);
            claimsSettlement.setItemTax(otherExpenceTotalTax);
            claimsSettlement.setOrganization(newClaimTransaction.getTransactionBranchOrganization());
            claimsSettlement.setBranch(newClaimTransaction.getTransactionBranch());
            Specifics specifics = Specifics.findByOrganizationAndMappingID(entityManager, user.getOrganization(),
                    IdosConstants.OTHER_EXPENSES_MAPPING_ID);
            if (specifics == null) {
                throw new IDOSException(IdosConstants.COA_MAPPING_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        "COA Mapping is not found for Other Expenses", IdosConstants.COA_MAPPING_EXCEPTION);
            }
            claimsSettlement.setTransactionSpecifics(specifics);
            genericDAO.saveOrUpdate(claimsSettlement, user, entityManager);
            if ((newClaimTransaction != null && newClaimTransaction.getId() != null)
                    && (claimsSettlement != null && claimsSettlement.getId() != null)) {
                CLAIM_DETAILS_DAO.saveClaimItemDetails(user, json, entityManager, newClaimTransaction,
                        claimsSettlement);
            }
        }
        Double claimTxnIncurredFixedPerDiam = 0.0;
        Double fixedPerDiamExpenceTotalAmt = 0.0;
        Double fixedPerDiamtravelExpenceTotalTax = 0.0;
        if (json.findValue("claimTxnIncurredFixedPerDiam") != null
                && !"".equals(json.findValue("claimTxnIncurredFixedPerDiam"))) {
            claimTxnIncurredFixedPerDiam = json.findValue("claimTxnIncurredFixedPerDiam").asDouble();
            fixedPerDiamExpenceTotalAmt = json.findValue("fixedPerDiamExpenceTotalAmt").asDouble();
            fixedPerDiamtravelExpenceTotalTax = json.findValue("fixedPerDiamtravelExpenceTotalTax").asDouble();
            ClaimsSettlement claimsSettlement = new ClaimsSettlement();
            claimsSettlement.setTransaction(newClaimTransaction);
            claimsSettlement.setItemName(IdosConstants.FIXED_PER_DIAM);
            claimsSettlement.setItemValue(claimTxnIncurredFixedPerDiam);
            claimsSettlement.setItemGross(fixedPerDiamExpenceTotalAmt);
            claimsSettlement.setItemTax(fixedPerDiamtravelExpenceTotalTax);
            claimsSettlement.setOrganization(newClaimTransaction.getTransactionBranchOrganization());
            claimsSettlement.setBranch(newClaimTransaction.getTransactionBranch());
            Specifics specifics = Specifics.findByOrganizationAndMappingID(entityManager, user.getOrganization(),
                    IdosConstants.FIXED_PER_DIAM_MAPPING_ID);
            if (specifics == null) {
                throw new IDOSException(IdosConstants.COA_MAPPING_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                        "COA Mapping is not found for Fixed Per Diam", IdosConstants.COA_MAPPING_EXCEPTION);
            }
            claimsSettlement.setTransactionSpecifics(specifics);
            genericDAO.saveOrUpdate(claimsSettlement, user, entityManager);
            if ((newClaimTransaction != null && newClaimTransaction.getId() != null)
                    && (claimsSettlement != null && claimsSettlement.getId() != null)) {
                CLAIM_DETAILS_DAO.saveClaimItemDetails(user, json, entityManager, newClaimTransaction,
                        claimsSettlement);
            }
        }

        entitytransaction.commit();
        sendSocketResponeToClientSettlement(newClaimTransaction, user, result);
        log.log(Level.FINE, "************* End " + result);
        return result;
    }

    @Override
    public ObjectNode claimSettlementAccountantAction(ObjectNode result, JsonNode json, Users user,
            EntityManager entityManager, EntityTransaction entitytransaction) throws IDOSException {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "************* Start ");
        }
        Map<String, Object> criterias = new HashMap<String, Object>();
        entitytransaction.begin();
        String transactionPrimId = json.findValue("claimTxnPrimId").asText();
        String suppDoc = json.findValue("suppDoc") != null ? json.findValue("suppDoc").asText() : null;
        String txnRmarks = json.findValue("txnRmarks").asText();
        String claimSettlementValue = json.findValue("claimSettlementValue") != null
                ? json.findValue("claimSettlementValue").asText()
                : null;
        ClaimTransaction claimTransaction = ClaimTransaction.findById(Long.parseLong(transactionPrimId));
        ClaimTransaction settlementClaimTxn = null;
        if (claimTransaction.getClaimsSettlementRefNumber() != null) {
            criterias.clear();
            criterias.put("transactionRefNumber", claimTransaction.getClaimsSettlementRefNumber());
            criterias.put("presentStatus", 1);
            settlementClaimTxn = genericDAO.getByCriteria(ClaimTransaction.class, criterias, entityManager);
        }
        if (claimTransaction != null) {
            claimSettlementValue = claimTransaction.getTransactionStatus();
        }

        if (claimSettlementValue != null && settlementClaimTxn != null) {

            claimTransaction.setTransactionStatus("Accounted");
            // settleTravelClaimAdvanceForThisTxn(claimTransaction, settlementClaimTxn,
            // user, entityManager);
            if (claimSettlementValue.equals("Payment Due To Staff")) {

                // TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserAdvance(claimTransaction, user,
                // entityManager, claimTransaction.getClaimsNetSettlement(), true);

                TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserClaim(claimTransaction, user, entityManager,
                        claimTransaction.getClaimsRequiredSettlement(), true);

                addTravelAdvanceAccountAmountAndAddTravelAdvanceSettleAmount(claimTransaction, settlementClaimTxn, user,
                        entityManager);
                CLAIM_SETTLEMENT_DAO.addExpensesInTrialBalance(user, entityManager, claimTransaction, false);
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
                    Double resultantCash = branchCashService.updateBranchCashDetail(entityManager, user,
                            claimTransaction.getTransactionBranch(), claimTransaction.getClaimsReturnSettlement(),
                            false, claimTransaction.getTransactionDate(), result);
                    result.put("resultantCash", resultantCash);
                    trialBalanceService.addTrialBalanceForCash(user, entityManager, genericDAO, claimTransaction,
                            claimTransaction.getAmountReturnInCaseOfDueToCompany(), false); // getClaimsReturnSettlement()
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
                        // false);//getClaimsReturnSettlement()
                        boolean branchBankDetailEntered = branchBankService.updateBranchBankDetailTransaction(
                                entityManager, user, bankAccount,
                                claimTransaction.getAmountReturnInCaseOfDueToCompany(), false, result,
                                claimTransaction.getTransactionDate(), claimTransaction.getTransactionBranch());
                        if (!branchBankDetailEntered) {
                            return result; // since balance is in -ve don't make any changes in DB
                        }
                        trialBalanceService.addTrialBalanceForBank(user, entityManager, genericDAO, claimTransaction,
                                claimTransaction.getAmountReturnInCaseOfDueToCompany(), false);
                    }
                }

                // Advance for Expense should reflect above 2 effects in 3rd column = 2000+900
                // When user has spend more than advance taken e.g. he spent 1700 out of 1000
                // advance then company has to pay 700 to user

                // TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserAdvance(claimTransaction, user,
                // entityManager, claimTransaction.getClaimsNetSettlement(), true);

                claimTransaction.setSettlementStatus("SETTLED");
                deductTravelAdvanceAccountAmountAndAddToTravelReturnedAccountAmount(claimTransaction,
                        settlementClaimTxn, user, entityManager);
                CLAIM_SETTLEMENT_DAO.addExpensesInTrialBalance(user, entityManager, claimTransaction, false);

            } else if (claimSettlementValue.equals("No Due For Settlement")) { // for settling and closing the claim
                                                                               // advance transaction as user
                                                                               // expenditure is equivalent to the
                                                                               // advances taken and adjustmeny if any
                                                                               // on this particular transaction
                // claimTransaction.setTransactionStatus("Accounted");
                // settleTravelClaimAdvanceForThisTxn(claimTransaction,settlementClaimTxn,user,em);
                // TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserAdvance(claimTransaction, user,
                // entityManager, claimTransaction.getClaimsNetSettlement(), true);
                claimTransaction.setSettlementStatus("NOT-SETTLED");
                CLAIM_SETTLEMENT_DAO.addExpensesInTrialBalance(user, entityManager, claimTransaction, false);
                deductTravelAdvanceAccountAmountAndAddToTravelReturnedAccountAmount(claimTransaction,
                        settlementClaimTxn, user, entityManager);
            }
            settleTravelClaimAdvanceForThisTxn(claimTransaction, settlementClaimTxn, user, entityManager);
            // CLAIM_SETTLEMENT_DAO.addExpensesInTrialBalance(user, entityManager,
            // claimTransaction, false);
        }
        claimTransaction.setSupportingDocuments(transactionDao.getAndDeleteSupportingDocument(
                claimTransaction.getSupportingDocuments(), user.getEmail(), suppDoc, user, entityManager));
        if (txnRmarks != null && !txnRmarks.equals("")) {
            if (claimTransaction.getTxnRemarks() != null) {
                claimTransaction
                        .setTxnRemarks(claimTransaction.getTxnRemarks() + "," + user.getEmail() + "#" + txnRmarks);
            } else {
                claimTransaction.setTxnRemarks(user.getEmail() + "#" + txnRmarks);
            }
        }
        claimTransaction.setTransactionStatus("Accounted");
        claimTransaction.setAccountingActionBy(user);
        genericDAO.saveOrUpdate(settlementClaimTxn, user, entityManager);
        genericDAO.saveOrUpdate(claimTransaction, user, entityManager);
        FILE_UPLOAD_SERVICE.updateUploadFileLogs(entityManager, user, suppDoc, claimTransaction.getId(),
                IdosConstants.CLAIM_TXN_TYPE);
        KarvyAuthorization karvyAPICall = new KarvyAuthorization(application);
        karvyAPICall.saveGSTFilingDataForClaimTransaction(user, claimTransaction, entityManager);
        entitytransaction.commit();
        sendSocketResponeToClientSettlement(claimTransaction, user, result);
        return result;
    }

    @Override
    public ObjectNode populateUserUnsettledTravelClaimAdvances(ObjectNode result, JsonNode json, Users user,
            EntityManager entityManager, EntityTransaction entitytransaction) {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "************* Start ");
        }
        result.put("result", false);
        ArrayNode userUnsettledClaimAdvancesAn = result.putArray("userUnsettledClaimAdvances");

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("userId", user.getId());
        List<ClaimTransaction> userClaimTransactionList = genericDAO
                .queryWithNamedParams(QUERY_USER_NON_SETTLED_TRANSACTIONS, entityManager, paramMap);

        for (ClaimTransaction claimTransaction : userClaimTransactionList) {
            StringBuilder sbuffer = new StringBuilder(
                    "select SUM(obj.newAmount) from ClaimTransaction obj where obj.createdBy='").append(user.getId())
                    .append("' and obj.transactionPurpose=16 and obj.claimsSettlementRefNumber='"
                            + claimTransaction.getTransactionRefNumber()
                            + "' and obj.settlementStatus!='SETTLED' and obj.transactionStatus!='Accounted' and obj.presentStatus=1");
            List<ClaimTransaction> settlementInProgress = genericDAO.executeSimpleQuery(sbuffer.toString(),
                    entityManager);
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
                            + user.getId() + "' and obj.transactionPurpose=16 and obj.claimsSettlementRefNumber='"
                            + claimTransaction.getTransactionRefNumber()
                            + "' and obj.settlementStatus!='SETTLED' and obj.transactionStatus!='Accounted' and obj.presentStatus=1");
            List<ClaimTransaction> settlementReturnedInCaseOfDueToCompanyProgress = genericDAO
                    .executeSimpleQuery(sbuffer.toString(), entityManager);
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
                userUnsettledClaimAdvancesAn.add(row);
            }
        }
        log.log(Level.FINE, "************* End " + result);
        return result;
    }

    @Override
    public ObjectNode displayUnsettledAdvancesDetails(ObjectNode result, JsonNode json, Users user,
            EntityManager entityManager, EntityTransaction entitytransaction) {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "************* Start ");
        }
        result.put("result", false);
        ArrayNode userTxnUnsettledClaimAdvancesDetailsAn = result.putArray("userTxnUnsettledClaimAdvancesDetails");
        String previousClaimTxnPrimId = json.findValue("previousClaimTxnPrimId") != null
                ? json.findValue("previousClaimTxnPrimId").asText()
                : null;
        ClaimTransaction prevClaimTxn = ClaimTransaction.findById(Long.parseLong(previousClaimTxnPrimId));
        if (prevClaimTxn != null) {
            Double totalAdvanceAgainstTxn = 0.0;
            Double settledTillDateTxn = 0.0;
            Double settledAgainstAdvanceInOtherTxn = 0.0;
            Double balanceUnsettledAgainstThisTxn = 0.0;
            ObjectNode row = Json.newObject();
            if (prevClaimTxn.getClaimsNetSettlement() != null) {
                totalAdvanceAgainstTxn = prevClaimTxn.getClaimsNetSettlement();
                if (prevClaimTxn.getClaimsDueSettlement() != null) {
                    settledTillDateTxn = prevClaimTxn.getClaimsNetSettlement() - prevClaimTxn.getClaimsDueSettlement();
                }
            }
            if (prevClaimTxn.getAdjustedAdvance() != null) {
                settledAgainstAdvanceInOtherTxn = prevClaimTxn.getAdjustedAdvance();
            }
            if (prevClaimTxn.getClaimsDueSettlement() != null) {
                balanceUnsettledAgainstThisTxn = prevClaimTxn.getClaimsDueSettlement();
            }
            StringBuilder sbuffer = new StringBuilder("");
            sbuffer.append("select SUM(obj.newAmount) from ClaimTransaction obj where obj.createdBy='" + user.getId()
                    + "' and obj.transactionPurpose=16 and obj.claimsSettlementRefNumber='"
                    + prevClaimTxn.getTransactionRefNumber()
                    + "' and obj.settlementStatus!='SETTLED' and obj.transactionStatus!='Accounted' and obj.presentStatus=1");
            List<ClaimTransaction> settlementInProgress = genericDAO.executeSimpleQuery(sbuffer.toString(),
                    entityManager);
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
                            + user.getId() + "' and obj.transactionPurpose=16 and obj.claimsSettlementRefNumber='"
                            + prevClaimTxn.getTransactionRefNumber()
                            + "' and obj.settlementStatus!='SETTLED' and obj.transactionStatus!='Accounted' and obj.presentStatus=1");
            List<ClaimTransaction> settlementReturnedInCaseOfDueToCompanyProgress = genericDAO
                    .executeSimpleQuery(sbuffer.toString(), entityManager);
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
            row.put("totalAdvanceAgainstTxn", IdosConstants.DECIMAL_FORMAT.format(totalAdvanceAgainstTxn));
            row.put("settledTillDateTxn", IdosConstants.DECIMAL_FORMAT.format(settledTillDateTxn));
            row.put("settledAgainstAdvanceInOtherTxn",
                    IdosConstants.DECIMAL_FORMAT.format(settledAgainstAdvanceInOtherTxn));
            row.put("balanceUnsettledAgainstThisTxn",
                    IdosConstants.DECIMAL_FORMAT.format(balanceUnsettledAgainstThisTxn));
            userTxnUnsettledClaimAdvancesDetailsAn.add(row);
        }
        log.log(Level.FINE, "************* End " + result);
        return result;
    }

    @Override
    public void sendSocketResponeToClientSettlement(ClaimTransaction claimTransaction, Users user, ObjectNode result) {
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
        String accountantEmails = claimTransaction.getAccountantEmails() != null
                ? claimTransaction.getAccountantEmails()
                : "";
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
        String creatorLabel = "Created By:";
        String createdBy = claimTransaction.getCreatedBy() != null ? claimTransaction.getCreatedBy().getEmail() : "";
        String accountedLabel = "Accounted By:";
        String accountedBy = claimTransaction.getAccountingActionBy() != null
                ? claimTransaction.getAccountingActionBy().getEmail()
                : "";
        String transactionDate = claimTransaction.getTransactionDate() != null
                ? IdosConstants.IDOSDF.format(claimTransaction.getTransactionDate())
                : "";
        Double amountReturnInCaseOfDueToCompany = claimTransaction.getAmountReturnInCaseOfDueToCompany();
        String claimTxnRefNo = claimTransaction.getTransactionRefNumber() != null
                ? claimTransaction.getTransactionRefNumber()
                : "";
        String paymentMode = "";
        String instrumentNumber = "";
        String instrumentDate = "";
        if (claimTransaction.getReceiptDetailType() != null) {
            if (claimTransaction.getReceiptDetailType() == 1) {
                paymentMode = "CASH";
            } else if (claimTransaction.getReceiptDetailType() == 2) {
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
        // List<Users> orgusers = genericDAO.executeSimpleQuery(sbquery.toString(),
        // entityManager);
        // if (!orgusers.isEmpty() && orgusers.get(0).getOrganization().getId() ==
        // user.getOrganization().getId()) {
        // orgclaimregistrered.put(keyArray[i].toString(),
        // CreatorActor.expenseregistrered.get(keyArray[i]));
        // }
        // }
        TransactionViewResponse.addClaimSettlementTxn(id, branchName, projectName, txnQuestionName, txnOrgnName,
                travelType,
                noOfPlacesToVisit, placesSelectedOrEntered, typeOfCity, appropriateDiatance, totalDays,
                travelDetailedConfDescription, existingClaimsCurrentSettlementDetails, claimuserAdvanveEligibility,
                userExpenditureOnThisTxn, netSettlementAmount, dueSettlementAmount, requiredSettlement,
                returnSettlement, purposeOfVisit, claimdebitCredit, claimTxnStatus, creatorLabel, createdBy,
                accountedLabel, accountedBy, transactionDate, paymentMode, accountantEmails, claimTxnRemarks,
                supportingDoc, instrumentNumber, instrumentDate, user.getEmail(),
                amountReturnInCaseOfDueToCompany, claimTxnRefNo, result);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "************* End ");
        }
    }

    private void settleTravelClaimAdvanceForThisTxn(ClaimTransaction claimTransaction,
            ClaimTransaction settlementClaimTxn, Users user, EntityManager entityManager) {
        log.log(Level.FINE, "************* Start " + claimTransaction);
        log.log(Level.FINE, "************* " + settlementClaimTxn);

        /*
         * if(settlementClaimTxn.getClaimsNetSettlement()!=null){
         * settlementClaimTxn.setClaimsNetSettlement(claimTransaction.
         * getClaimsNetSettlement());
         * }else{
         * settlementClaimTxn.setClaimsNetSettlement(claimTransaction.
         * getClaimsNetSettlement());
         * }
         */

        if (claimTransaction.getAmountReturnInCaseOfDueToCompany() == null) { // due from company
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
            // *******TRIAL BALANCE CHANGES: START***********//
            // When user has spend more than advance taken e.g. he spent 1700 out of 1000
            // advance then company has to pay 700 to user
            TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserAdvance(claimTransaction, user, entityManager,
                    claimTransaction.getClaimsRequiredSettlement(), false);

            // ***************TRIAL BALANCE CHNAGES: END**********//
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
            // *******TRIAL BALANCE CHANGES: START***********//
            // When user has spend less than advance taken e.g. he spent 700 out of 1000
            // advance then he has to pay 300 back to company
            Double temp = settleAmount + amountReturnedIncaseOfDue - claimTransaction.getClaimsRequiredSettlement();
            TRIAL_BALANCE_CLAIMS.saveTrialBalanceUserAdvance(claimTransaction, user, entityManager, temp, true);

        }
        genericDAO.saveOrUpdate(settlementClaimTxn, user, entityManager);

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "************* End ");
        }
    }

    private void addTravelAdvanceAccountAmountAndAddTravelAdvanceSettleAmount(ClaimTransaction claimTransaction,
            ClaimTransaction settlementClaimTxn, Users user, EntityManager entityManager) {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "************* Start ");
        }
        Double travelAdvanceAccountAmount = 0.0;
        Double travelAdvanceSettleAmount = 0.0;
        Users claimCreatedUser = claimTransaction.getCreatedBy();
        if (claimCreatedUser.getTravelAdvanceAccountAmount() != null) {
            travelAdvanceAccountAmount = claimCreatedUser.getTravelAdvanceAccountAmount()
                    + claimTransaction.getClaimsRequiredSettlement();
        }
        if (claimCreatedUser.getTravelAdvanceAccountAmount() == null) {
            travelAdvanceAccountAmount = claimTransaction.getClaimsRequiredSettlement();
        }
        if (claimCreatedUser.getTravelAdvanceSettledAmount() != null) {
            travelAdvanceSettleAmount = claimCreatedUser.getTravelAdvanceSettledAmount()
                    + claimTransaction.getClaimsNetSettlement();
        }
        if (claimCreatedUser.getTravelAdvanceSettledAmount() == null) {
            travelAdvanceSettleAmount = claimTransaction.getClaimsNetSettlement();
        }
        claimCreatedUser.setTravelAdvanceAccountAmount(travelAdvanceAccountAmount);
        claimCreatedUser.setTravelAdvanceSettledAmount(travelAdvanceSettleAmount);
        genericDAO.saveOrUpdate(claimCreatedUser, user, entityManager);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "************* End ");
        }
    }

    private void deductTravelAdvanceAccountAmountAndAddToTravelReturnedAccountAmount(ClaimTransaction claimTransaction,
            ClaimTransaction settlementClaimTxn, Users user, EntityManager entityManager) {
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
        genericDAO.saveOrUpdate(user, user, entityManager);

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "************* End ");
        }
    }
}
