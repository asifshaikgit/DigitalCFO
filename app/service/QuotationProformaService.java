package service;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.util.IDOSException;
import model.Branch;
import model.Transaction;
import model.Users;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.Date;

/**
 * Created by Sunil Namdev on 05-12-2016.
 */
public interface QuotationProformaService extends BaseService {
        TransactionItemsService transactionItemsService = new TransactionItemsServiceImpl();

        Transaction submitForApprovalQuotation(Users user, JsonNode json, EntityManager entityManager,
                        EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

        Transaction submitForApprovalProforma(Users user, JsonNode json, EntityManager entityManager,
                        EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

        boolean getQuotationProformaProjectBy(Branch branch, Users user, EntityManager entityManager,
                        long transactionPurposeID, Date fromDate, Date toDate, ObjectNode result);

        boolean getQuotationProformaItems(EntityManager entityManager, Users user, JsonNode json, Date startDate,
                        Date endDate, long transactionPurposeID, ObjectNode result) throws IDOSException;

        boolean getTransactionsForItem(EntityManager entityManager, Users user, JsonNode json, Date startDate,
                        Date endDate,
                        long transactionPurposeID, ObjectNode result) throws IDOSException;
}
