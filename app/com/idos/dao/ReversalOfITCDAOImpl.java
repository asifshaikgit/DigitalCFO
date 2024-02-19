package com.idos.dao;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import javax.inject.Inject;
import play.db.jpa.JPAApi;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;

import com.idos.util.CodeHelper;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import java.util.logging.Level;
import model.Branch;
import model.BranchTaxes;
import model.Specifics;
import model.Transaction;
import model.TransactionItems;
import model.TransactionPurpose;
import model.TrialBalanceCOAItems;
import model.TrialBalanceTaxes;
import model.Users;

public class ReversalOfITCDAOImpl implements ReversalOfITCDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public Transaction submitForAccounting(Users user, JsonNode json, EntityManager entityManager,
			EntityTransaction entitytransaction, TransactionPurpose usertxnPurpose, ObjectNode result)
			throws IDOSException {
		Transaction transaction = null;
		try {
			Long txnBranchId = json.findValue("txnforbranch") == null || "".equals(json.findValue("txnforbranch"))
					? null
					: json.findValue("txnforbranch").asLong();
			transaction = new Transaction();
			Branch transactionBranch = null;
			if (txnBranchId != null) {
				transactionBranch = Branch.findById(txnBranchId);
			}
			transaction.setTransactionPurpose(usertxnPurpose);
			transaction.setTransactionBranch(transactionBranch);
			transaction.setTransactionBranchOrganization(transactionBranch.getOrganization());
			transaction.setTransactionDate(Calendar.getInstance().getTime());
			transaction.setTransactionStatus("Accounted");
			String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
			transaction.setTransactionRefNumber(transactionNumber);
			genericDao.saveOrUpdate(transaction, user, entityManager);

			String itemListData = json.findValue("itemListData").toString();
			JSONArray expenseDetailsArray = new JSONArray(itemListData);
			Double grossAmount = 0d;
			for (int i = 0; i < expenseDetailsArray.length(); i++) {
				JSONObject rowItemData = new JSONObject(expenseDetailsArray.get(i).toString());
				Long branchId = rowItemData.get("branchId") == null || "".equals(rowItemData.getString("branchId"))
						? null
						: rowItemData.getLong("branchId");
				// Long itemId = rowItemData.get("itemId")== null ||
				// "".equals(rowItemData.getString("itemId")) ? null :
				// rowItemData.getLong("itemId");
				Long taxLedger = rowItemData.get("taxLedger") == null || "".equals(rowItemData.getString("taxLedger"))
						? null
						: rowItemData.getLong("taxLedger");
				Integer reasonForReversal = rowItemData.get("reasonForReversal") == null
						|| "".equals(rowItemData.getString("reasonForReversal")) ? null
								: rowItemData.getInt("reasonForReversal");
				Double revarsalAmount = rowItemData.get("revarsalAmount") == null
						|| "".equals(rowItemData.getString("revarsalAmount")) ? null
								: rowItemData.getDouble("revarsalAmount");
				TransactionItems transactionItem = new TransactionItems();
				transactionItem.setTransactionId(transaction);
				// Specifics specific = null;
				if (branchId != null) {
					Branch branch = Branch.findById(branchId);
					transactionItem.setBranch(branch);
				}
				// if (itemId != null) {
				// specific = Specifics.findById(itemId);
				// transactionItem.setTransactionSpecifics(specific);
				// if(i == 0) {
				// transaction.setTransactionSpecifics(specific);
				// }
				// }
				if (taxLedger != null) {
					transactionItem.setTax1ID(taxLedger);
				}
				if (revarsalAmount != null) {
					transactionItem.setGrossAmounReturned(revarsalAmount);
					grossAmount += revarsalAmount;
				} else {
					revarsalAmount = 0d;
				}

				if (reasonForReversal != null) {
					transactionItem.setReasonForReturn(reasonForReversal);
				}
				genericDao.saveOrUpdate(transactionItem, user, entityManager);

				// if(taxLedger != null && specific != null) {
				if (taxLedger != null) {
					BranchTaxes branchTaxes = BranchTaxes.findById(taxLedger);
					TrialBalanceTaxes trialBalTaxes = new TrialBalanceTaxes();
					trialBalTaxes.setTransactionId(transaction.getId());
					trialBalTaxes.setTransactionPurpose(transaction.getTransactionPurpose());
					trialBalTaxes.setDate(transaction.getTransactionDate());
					trialBalTaxes.setBranchTaxes(branchTaxes);
					trialBalTaxes.setTaxType(branchTaxes.getTaxType()); // output taxes
					trialBalTaxes.setBranch(transaction.getTransactionBranch());
					trialBalTaxes.setOrganization(transaction.getTransactionBranchOrganization());
					trialBalTaxes.setCreditAmount(revarsalAmount);
					// trialBalTaxes.setTransactionSpecifics(specific);
					// trialBalTaxes.setTransactionParticulars(specific.getParticularsId());
					genericDao.saveOrUpdate(trialBalTaxes, user, entityManager);
				}
			}

			transaction.setGrossAmount(grossAmount);
			transaction.setNetAmount(grossAmount);
			genericDao.saveOrUpdate(transaction, user, entityManager);
			Specifics spec = coaDAO.getSpecificsForMapping(user, "62", entityManager);
			if (spec == null) {
				throw new IDOSException(IdosConstants.TB_EXCEPTION_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
						"Specific is null", "Can't store TB for the Claim");
			}
			TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems();
			trialBalCOA.setTransactionSpecifics(spec);
			trialBalCOA.setTransactionId(transaction.getId());
			trialBalCOA.setTransactionPurpose(transaction.getTransactionPurpose());
			trialBalCOA.setTransactionParticulars(spec.getParticularsId());
			trialBalCOA.setDate(transaction.getTransactionDate());
			trialBalCOA.setBranch(transaction.getTransactionBranch());
			trialBalCOA.setOrganization(transaction.getTransactionBranchOrganization());
			trialBalCOA.setDebitAmount(grossAmount);
			genericDao.saveOrUpdate(trialBalCOA, user, entityManager);
			transactionService.sendStockWebSocketResponse(entityManager, transaction, user, result);
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
		}
		return transaction;
	}

}
