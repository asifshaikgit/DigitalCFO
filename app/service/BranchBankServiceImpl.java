package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.dao.GenericDAO;
import com.idos.dao.trialbalance.TrialBalanceBankDAO;
import com.idos.dao.trialbalance.TrialBalanceBankDAOImpl;
import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;

import model.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;


import javax.transaction.Transactional;

import com.idos.dao.BranchBankDAO;
import com.idos.dao.BranchBankDAOImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BranchBankServiceImpl implements BranchBankService {

	@Override
	public ObjectNode branchBank(ObjectNode result, JsonNode json, Users user, EntityManager entityManager) {
		log.log(Level.FINE, ">>>> Start");
		result = branchBankDAO.branchBank(result, json, user, entityManager);
		return result;
	}

	@Override
	public ObjectNode branchBankDetails(ObjectNode result, JsonNode json, Users user, EntityManager entityManager) {
		result = branchBankDAO.branchBankDetails(result, json, user, entityManager);
		return result;
	}

	public Double updateBranchBankDetail(EntityManager entityManager, Users user, BranchBankAccounts bankAccount,
			Double txnNetAmount, boolean isCredit) throws IDOSException {
		Double creditAmount = null;
		Double debitAmount = null;
		Double resultantAmount = -1.0;
		Double amountBalance = null;

		StringBuilder newsbquery = new StringBuilder(
				"select obj from BranchBankAccountBalance obj WHERE obj.branch.id='")
				.append(bankAccount.getBranch().getId()).append("' AND obj.organization.id='")
				.append(bankAccount.getOrganization().getId()).append("' and obj.branchBankAccounts.id='")
				.append(bankAccount.getId()).append("' and obj.presentStatus=1 ORDER BY obj.date desc");
		List<BranchBankAccountBalance> branchBankAccountBal = genericDAO
				.executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);
		if (branchBankAccountBal.size() > 0) {
			if (branchBankAccountBal.get(0).getCreditAmount() == null) {
				creditAmount = 0.0;
			} else if (branchBankAccountBal.get(0).getCreditAmount() != null) {
				creditAmount = branchBankAccountBal.get(0).getCreditAmount();
			}
			if (branchBankAccountBal.get(0).getDebitAmount() == null) {
				debitAmount = 0.0;
			} else if (branchBankAccountBal.get(0).getDebitAmount() != null) {
				debitAmount = branchBankAccountBal.get(0).getDebitAmount();
			}

			if (branchBankAccountBal.get(0).getAmountBalance() != null) {
				amountBalance = branchBankAccountBal.get(0).getAmountBalance();
			} else {
				amountBalance = 0.0;
			}
			Double resultantAmtTmp = 0.0;
			if (isCredit) {
				creditAmount += txnNetAmount;
				resultantAmount = amountBalance + debitAmount - creditAmount;
				branchBankAccountBal.get(0).setCreditAmount(creditAmount);
			} else {
				resultantAmtTmp = debitAmount - creditAmount;
				debitAmount += txnNetAmount;
				resultantAmount = amountBalance + debitAmount - creditAmount;
				branchBankAccountBal.get(0).setDebitAmount(debitAmount);
			}
			branchBankAccountBal.get(0).setResultantCash(resultantAmount);
			if (resultantAmount >= 0 || (resultantAmtTmp != 0.0 && resultantAmtTmp < resultantAmount)) {
				genericDAO.saveOrUpdate(branchBankAccountBal.get(0), user, entityManager);
			}
		} else {
			throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
					IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Bank account not configured.");
		}
		return resultantAmount;
	}

	public boolean updateBranchBankDetailTransaction(EntityManager entityManager, Users user,
			BranchBankAccounts bankAccount, Double txnNetAmount, boolean isCredit, ObjectNode result, Date txnDate,
			Branch branch) throws IDOSException {
		Double creditAmount = 0.0;
		Double debitAmount = 0.0;
		Double resultantAmount = -1.0;
		Double amountBalance = 0.0;
		boolean branchBankDetailEntered = false;
		double onDateBalance = getBranchBankBalanceOnDate(user, txnDate, branch.getId(), bankAccount, entityManager);
		StringBuilder newsbquery = new StringBuilder(
				"select obj from BranchBankAccountBalance obj WHERE obj.branch.id='")
				.append(bankAccount.getBranch().getId()).append("' and obj.organization.id='")
				.append(bankAccount.getOrganization().getId())
				.append("' and obj.branchBankAccounts.id='").append(bankAccount.getId())
				.append("' and obj.presentStatus=1 ORDER BY obj.date desc");
		List<BranchBankAccountBalance> branchBankAccountBal = genericDAO
				.executeSimpleQueryWithLimit(newsbquery.toString(), entityManager, 1);

		if (branchBankAccountBal.size() > 0) {
			if (branchBankAccountBal.get(0).getCreditAmount() != null) {
				creditAmount = branchBankAccountBal.get(0).getCreditAmount();
			}
			if (branchBankAccountBal.get(0).getDebitAmount() != null) {
				debitAmount = branchBankAccountBal.get(0).getDebitAmount();
			}
			if (branchBankAccountBal.get(0).getAmountBalance() != null) {
				amountBalance = branchBankAccountBal.get(0).getAmountBalance();
			}
			if (isCredit) {
				creditAmount += txnNetAmount;
				resultantAmount = amountBalance + debitAmount - creditAmount;
				branchBankAccountBal.get(0).setCreditAmount(creditAmount);
				resultantAmount = onDateBalance - txnNetAmount;
			} else {
				debitAmount += txnNetAmount;
				resultantAmount = amountBalance + debitAmount - creditAmount;
				branchBankAccountBal.get(0).setDebitAmount(debitAmount);
				resultantAmount = onDateBalance + txnNetAmount;
			}
			branchBankAccountBal.get(0).setResultantCash(resultantAmount);
			if (resultantAmount >= 0) {
				genericDAO.saveOrUpdate(branchBankAccountBal.get(0), user, entityManager);
				branchBankDetailEntered = true;
			} else if (resultantAmount < 0) {
				if (bankAccount.getAccountType() == 1 || bankAccount.getAccountType() == 5
						|| bankAccount.getAccountType() == 6 || bankAccount.getAccountType() == 7
						|| bankAccount.getAccountType() == 9 || bankAccount.getAccountType() == 10
						|| bankAccount.getAccountType() == 11 || bankAccount.getAccountType() == 12) { // balance -ve is
																										// allowed
					genericDAO.saveOrUpdate(branchBankAccountBal.get(0), user, entityManager);
					branchBankDetailEntered = true; // since -ve is allowed, don't give alert msg in
													// configTransaction.js
				}
			}
			result.put("branchBankDetailEntered", branchBankDetailEntered);
			result.put("resultantAmount", resultantAmount);
		} else {
			throw new IDOSException(IdosConstants.NULL_KEY_EXC_ESMF, IdosConstants.BUSINESS_EXCEPTION,
					IdosConstants.NULL_KEY_EXC_ESMF_MSG, "Bank account is not configured.");
		}
		return branchBankDetailEntered;
	}

	@Override
	public double getBranchBankBalanceOnDate(Users user, Date txnDate, long branchId, BranchBankAccounts bank,
			EntityManager em) throws IDOSException {
		String fromDate = DateUtil.getCurrentFinacialStartDate(user.getOrganization().getFinancialStartDate());
		java.sql.Date fromDateDt = java.sql.Date.valueOf(fromDate); // DateUtil.convertStringToDate(fromDate,
																	// IdosConstants.MYSQLDF);
		TrialBalanceBankDAO das = new TrialBalanceBankDAOImpl();
		TrialBalance tb = das.getTrialBalance4SpecificBank(user, em, fromDateDt, txnDate, branchId, bank);
		if (tb == null)
			return 0.0;
		else
			return tb.getClosingBalance();
	}

	@Override
	public boolean getOrganizationBanks(ObjectNode result, Users user, EntityManager em) {
		return branchBankDAO.getOrganizationBanks(result, user, em);
	}
}
