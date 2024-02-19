package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.ClaimTransaction;
import model.Users;

public interface CreateTrialBalance4ClaimDAO extends BaseDAO {

	public void saveTrialBalanceUserAdvance(ClaimTransaction claimTransaction, Users user, EntityManager em,
			Double amount, boolean isCredit);

	public void saveTrialBalanceUserClaim(ClaimTransaction claimTransaction, Users user, EntityManager em,
			Double amount, boolean isCredit);

	public void insertTrialBalCOAItems(ClaimTransaction claimTransaction, Users user, EntityManager em, Double amount,
			boolean isCredit);

}
