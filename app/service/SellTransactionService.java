package service;

import com.idos.dao.*;
import com.idos.util.IDOSException;
import model.Transaction;
import model.TransactionItems;
import model.Users;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

/**
 * Created by Sunil Namdev on 10-12-2016.
 */
public interface SellTransactionService extends BaseService {

        Transaction submitForApproval(Users user, JsonNode json, final EntityManager entityManager,
                        EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

        Transaction submitForApprovalSalesReturns(Users user, JsonNode json, EntityManager entityManager,
                        EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

        Transaction submitForAccounting(Users user, JsonNode json, EntityManager entityManager,
                        EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

        boolean completeAccounting(Users user, JsonNode json, EntityManager entityManager,
                        EntityTransaction entitytransaction, Transaction transaction, ObjectNode result)
                        throws IDOSException;

        boolean verifyItemInvetory(Users user, EntityManager entityManager, Transaction transaction, ObjectNode result)
                        throws IDOSException;

        boolean getAdvanceDiscount(Users user, EntityManager entityManager, JsonNode json, ObjectNode result)
                        throws IDOSException;

        boolean getShippingAddress(Users user, Transaction transaction, ObjectNode result, EntityManager entityManager);

        boolean getAdditionalDetails(Users user, Transaction transaction, ObjectNode result,
                        EntityManager entityManager);

        Transaction submitForCancellation(Users user, JsonNode json, EntityManager entityManager,
                        EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

        boolean setTxnPaymentDetail(Users user, EntityManager em, Transaction txn, int paymentMode, long paymentBank,
                        String txnInstrumentNumber, String txnInstrumentDate, ObjectNode result) throws IDOSException;
}
