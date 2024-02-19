package com.idos.dao;

import com.idos.util.IDOSException;
import model.Transaction;
import model.Users;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import service.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

/**
 * Created by Sunil Namdev on 25-07-2017.
 */
public interface ReceiveFromCustomerDAO extends BaseDAO {
    ChartOfAccountsService CHART_OF_ACCOUNTS_SERVICE = new ChartOfAccountsServiceImpl();
    BranchCashService BRANCH_CASH_SERVICE = new BranchCashServiceImpl();
    BranchBankService BRANCH_BANK_SERVICE = new BranchBankServiceImpl();
    TransactionItemsService TRANSACTION_ITEMS_SERVICE = new TransactionItemsServiceImpl();

    Transaction submitForAccounting(Users user, JsonNode json, EntityManager entityManager,
            EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;
}
