package service;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.IDOSException;

import model.Transaction;
import model.TransactionPurpose;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

/**
 * Created by Sunil Namdev on 12-12-2016.
 */
public interface BuyTransactionService extends BaseService {
        Transaction submitForApproval(Users user, JsonNode json, EntityManager entityManager,
                        EntityTransaction entitytransaction, TransactionPurpose transactionPurpose, ObjectNode result)
                        throws IDOSException;

        Transaction submitForApprovalPurchaseReturn(Users user, JsonNode json, final EntityManager entityManager,
                        EntityTransaction entitytransaction, TransactionPurpose transactionPurpose, ObjectNode result)
                        throws IDOSException;

        // boolean submitForApprovalBuyOnCashAndCredit(Users user, JsonNode json, final
        // EntityManager entityManager, EntityTransaction entitytransaction,
        // TransactionPurpose transactionPurpose) throws IDOSException;
        Transaction submit4AccoutingBuyOnPetty(Users user, EntityManager entityManager, JsonNode json,
                        TransactionPurpose usertxnPurpose, ObjectNode result) throws IDOSException;

        public boolean getAdvanceDiscount(Users user, EntityManager entityManager, JsonNode json, ObjectNode result)
                        throws IDOSException;

        void calculateAndSaveTds(EntityManager em, Users user, Transaction txn) throws IDOSException;

        public void saveUpdateBudget4Items(Transaction txn, Users user, EntityManager em) throws IDOSException;

        boolean setTxnPaymentDetail(Users user, EntityManager em, Transaction txn, int paymentMode, long paymentBank,
                        String txnInstrumentNumber, String txnInstrumentDate, ObjectNode results) throws IDOSException;
}
