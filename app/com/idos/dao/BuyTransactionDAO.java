package com.idos.dao;

import com.idos.util.IDOSException;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil K. Namdev on 10-10-2017.
 */
public interface BuyTransactionDAO extends BaseDAO {

    String VENDOR_TXN_HQL = "select sum(availableAdvance), sum(adjustmentFromAdvance) from TransactionItems where organization.id=?1 and branch.id=?2 and transactionSpecifics.id=?3 and presentStatus=1 and transaction.id IN (select id from Transaction where transactionPurpose.id=8 and  transactionBranchOrganization.id=?4 and transactionBranch.id=?5 and destinationGstin=?6 and typeOfSupply=?7 and transactionVendorCustomer.id=?8 and transactionDate <= ?9 and presentStatus=1 and transactionStatus = 'Accounted')";

    String BRANCH_SPECIFIC_JPQL = "select obj from BranchSpecifics obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.specifics.id=?3 and obj.particular.id=?4 and obj.presentStatus=1";

    Transaction submit4AccoutingBuyOnPetty(Users user, EntityManager entityManager, JsonNode json,
            TransactionPurpose usertxnPurpose, ObjectNode result) throws IDOSException;

    boolean getAdvanceDiscount(Users user, EntityManager entityManager, JsonNode json, ObjectNode result)
            throws IDOSException;

    void saveUpdateBudget(Branch txnBranch, TransactionItems txnItem, Users user, EntityManager em)
            throws IDOSException;

    void saveUpdateBudget4Items(Transaction txn, Users user, EntityManager em) throws IDOSException;

    void calculateAndSaveTds(EntityManager em, Users user, Transaction txn) throws IDOSException;
}
