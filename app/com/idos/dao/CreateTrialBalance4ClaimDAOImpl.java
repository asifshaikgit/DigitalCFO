package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.ClaimTransaction;
import model.Transaction;
import model.TransactionItems;
import model.TrialBalanceCOAItems;
import model.TrialBalanceUserAdvance;
import model.TrialBalanceUserClaims;
import model.Users;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

public class CreateTrialBalance4ClaimDAOImpl implements CreateTrialBalance4ClaimDAO {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	@Override
	public void saveTrialBalanceUserAdvance(ClaimTransaction claimTransaction, Users user, EntityManager em,
			Double amount, boolean isCredit) {
		TrialBalanceUserAdvance trialBalUserAdv = new TrialBalanceUserAdvance(); // adv to user account
		trialBalUserAdv.setTransactionId(claimTransaction.getId());
		trialBalUserAdv.setTransactionPurpose(claimTransaction.getTransactionPurpose());
		trialBalUserAdv.setDate(claimTransaction.getTransactionDate());
		trialBalUserAdv.setBranch(claimTransaction.getTransactionBranch());
		trialBalUserAdv.setOrganization(claimTransaction.getTransactionBranchOrganization());
		trialBalUserAdv.setUser(claimTransaction.getCreatedBy());
		trialBalUserAdv.setTransactionParticulars(claimTransaction.getAdvanceForExpenseItemsParticulars());
		trialBalUserAdv.setTransactionSpecifics(claimTransaction.getAdvanceForExpenseItems());
		if (isCredit) {
			trialBalUserAdv.setCreditAmount(amount);
		} else {
			trialBalUserAdv.setDebitAmount(amount);
		}
		genericDao.saveOrUpdate(trialBalUserAdv, user, em);
	}

	@Override
	public void saveTrialBalanceUserClaim(ClaimTransaction claimTransaction, Users user, EntityManager em,
			Double amount, boolean isCredit) {
		TrialBalanceUserClaims trialBalUserClaim = new TrialBalanceUserClaims(); // Claim to user account
		trialBalUserClaim.setTransactionId(claimTransaction.getId());
		trialBalUserClaim.setTransactionPurpose(claimTransaction.getTransactionPurpose());
		trialBalUserClaim.setDate(claimTransaction.getTransactionDate());
		trialBalUserClaim.setBranch(claimTransaction.getTransactionBranch());
		trialBalUserClaim.setOrganization(claimTransaction.getTransactionBranchOrganization());
		trialBalUserClaim.setUser(claimTransaction.getCreatedBy());
		trialBalUserClaim.setTransactionParticulars(claimTransaction.getAdvanceForExpenseItemsParticulars());
		trialBalUserClaim.setTransactionSpecifics(claimTransaction.getAdvanceForExpenseItems());
		if (isCredit) {
			trialBalUserClaim.setCreditAmount(amount);
		} else {
			trialBalUserClaim.setDebitAmount(amount);
		}
		genericDao.saveOrUpdate(trialBalUserClaim, user, em);
	}

	@Override
	public void insertTrialBalCOAItems(ClaimTransaction claimTransaction, Users user, EntityManager em, Double amount,
			boolean isCredit) {
		TrialBalanceCOAItems trialBalCOA = new TrialBalanceCOAItems();
		trialBalCOA.setTransactionSpecifics(claimTransaction.getAdvanceForExpenseItems());
		trialBalCOA.setTransactionId(claimTransaction.getId());
		trialBalCOA.setTransactionPurpose(claimTransaction.getTransactionPurpose());
		trialBalCOA.setTransactionParticulars(claimTransaction.getAdvanceForExpenseItemsParticulars());
		trialBalCOA.setDate(claimTransaction.getTransactionDate());
		trialBalCOA.setBranch(claimTransaction.getTransactionBranch());
		trialBalCOA.setOrganization(claimTransaction.getTransactionBranchOrganization());
		if (isCredit) {
			trialBalCOA.setCreditAmount(amount);
		} else {
			trialBalCOA.setDebitAmount(amount);
		}
		genericDao.saveOrUpdate(trialBalCOA, user, em);
	}
}
