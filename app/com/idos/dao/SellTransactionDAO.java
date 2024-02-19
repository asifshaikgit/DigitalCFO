package com.idos.dao;

import com.idos.util.IDOSException;
import model.Specifics;
import model.Transaction;
import model.TransactionItems;
import model.Users;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.Date;

/**
 * Created by Sunil Namdev on 10-08-2017.
 */
public interface SellTransactionDAO extends BaseDAO {
        String TXN_HQL = "select (sum(availableAdvance)+sum(withholdingAmount)), sum(adjustmentFromAdvance) from TransactionItems where organization.id=?1 and branch.id=?2 and presentStatus=1 and transactionSpecifics.id=?3 and transaction.id IN (select id from Transaction where transactionPurpose.id=6 and  transactionBranchOrganization.id=?4 and transactionBranch.id=?5 and presentStatus=1 and destinationGstin=?6 and typeOfSupply=?7 and withWithoutTax=?8 and transactionVendorCustomer.id=?9 and transactionDate <= ?10)";

        String TXN_WALKIN_HQL = "select (sum(availableAdvance)+sum(withholdingAmount)), sum(adjustmentFromAdvance) from TransactionItems where organization.id=?1 and transactionSpecifics.id=?2 and presentStatus=1 and transaction.id IN (select id from Transaction where transactionPurpose.id=6 and transactionBranchOrganization.id=?3 and destinationGstin=?4 and presentStatus=1 and typeOfSupply=?5 and withWithoutTax=?6 and transactionUnavailableVendorCustomer=?7 and transactionDate <= ?8)";

        String TXN_ADJ_HQL = "select obj from TransactionItems obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.transactionSpecifics.id=?3 and obj.presentStatus=1 and obj.transaction.id IN (select id from Transaction where transactionPurpose.id=6 and transactionBranchOrganization.id=?4 and transactionBranch.id=?5 and destinationGstin=?6 and typeOfSupply=?7 and presentStatus=1 and withWithoutTax=?8 and transactionVendorCustomer.id=?9 and transactionDate <= ?10) order by obj.id";

        String TXN_WALKIN_ADJ_HQL = "select obj from TransactionItems obj where obj.organization.id=?1 and obj.transactionSpecifics.id=?2 and obj.presentStatus=1 and obj.transaction.id IN (select id from Transaction where transactionPurpose.id=6 and transactionBranchOrganization.id=?3 and destinationGstin=?4 and typeOfSupply=?5 and withWithoutTax=?6 and transactionUnavailableVendorCustomer=?7 and presentStatus=1 and transactionDate <= ?8) order by obj.id";

        String TXN_ADJ_BUY_HQL = "select obj from TransactionItems obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.transactionSpecifics.id=?3 and obj.presentStatus=1 and obj.transaction.id IN (select id from Transaction where transactionPurpose.id=8 and transactionBranchOrganization.id=?4 and transactionBranch.id=?5 and destinationGstin=?6 and typeOfSupply=?7 and presentStatus=1 and transactionVendorCustomer.id=?8 and transactionDate <= ?9) order by obj.id";

        String TXN_WALKIN_ADJ_BUY_HQL = "select obj from TransactionItems obj where obj.organization.id=?1 and obj.transactionSpecifics.id=?2 and obj.presentStatus=1 and obj.transaction.id IN (select id from Transaction where transactionPurpose.id=8 and transactionBranchOrganization.id=?3 and destinationGstin=?4 and typeOfSupply=?5 and transactionUnavailableVendorCustomer=?6 and presentStatus=1 and transactionDate <= ?7) order by obj.id";

        boolean getAdvanceDiscount(Users user, EntityManager entityManager, JsonNode json, ObjectNode result)
                        throws IDOSException;

        boolean saveAdvanceAdjustmentDetail(Users user, EntityManager entityManager, Specifics specific,
                        TransactionItems txnItem, Transaction txn, Double amountToAdj, Double tdsAmountToAdj,
                        Date txnDate)
                        throws IDOSException;

        boolean getShippingAddress(Users user, Transaction transaction, ObjectNode result, EntityManager entityManager);

        boolean getAdditionalDetails(Users user, Transaction transaction, ObjectNode result,
                        EntityManager entityManager);
}
