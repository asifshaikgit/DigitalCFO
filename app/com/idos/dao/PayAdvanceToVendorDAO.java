package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.util.IDOSException;

import model.Transaction;
import model.Users;
import service.BranchBankService;
import service.BranchBankServiceImpl;
import service.BranchCashService;
import service.BranchCashServiceImpl;
import service.ChartOfAccountsService;
import service.ChartOfAccountsServiceImpl;
import service.TransactionItemsService;
import service.TransactionItemsServiceImpl;

public interface PayAdvanceToVendorDAO extends BaseDAO {
    String PAY_VENDOR_SPECIFIC_HQL = "select obj from VendorSpecific obj WHERE obj.organization.id=?1 and obj.specificsVendors.id=?2 and obj.vendorSpecific.id=?3 and obj.presentStatus=1";
    // ChartOfAccountsService CHART_OF_ACCOUNTS_SERVICE = new
    // ChartOfAccountsServiceImpl();
    // BranchCashService BRANCH_CASH_SERVICE = new BranchCashServiceImpl();
    // BranchBankService BRANCH_BANK_SERVICE = new BranchBankServiceImpl();
    TransactionItemsService TRANSACTION_ITEMS_SERVICE = new TransactionItemsServiceImpl();

    Transaction submitForAprroval(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

    public boolean submitForAccountPayAdvToVendAdv(Transaction txn, EntityManager entityManager, Users user)
            throws IDOSException;
}
