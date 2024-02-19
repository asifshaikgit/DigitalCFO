package com.idos.dao;

import com.idos.util.IDOSException;
import model.*;
// import com.fasterxml.jackson.databind.ser;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @auther Sunil Namdev created on 30.04.2018
 */
public interface ClaimSettlementDAO extends BaseDAO {
    Double saveClaimTrialBalanceGstTax(ClaimTransaction txn, Users user, EntityManager em, ClaimItemDetails item,
            Specifics specifics);

    void addExpensesInTrialBalance(Users user, EntityManager entityManager, ClaimTransaction claimTransaction,
            boolean isCredit) throws IDOSException;

    void saveTrialBalanceGstTax(ClaimTransaction txn, Users user, EntityManager entityManager, boolean isCredit,
            Double taxAmount, BranchTaxes branchTaxes, Specifics specifics);
}
