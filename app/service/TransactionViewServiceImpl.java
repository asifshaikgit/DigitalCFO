package service;

import model.IdosProvisionJournalEntry;
import model.Transaction;
import model.TransactionPurpose;
import model.Users;
import com.fasterxml.jackson.databind.node.ArrayNode;
import pojo.GenericTransaction;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.idos.dao.BaseDAO.PROVISION_JOURNAL_ENTRY_DAO;
import static com.idos.dao.BaseDAO.TRANSACTION_SERVICE;

/**
 * @auther Sunil Namdev created on 26.12.2018
 */
public class TransactionViewServiceImpl implements TransactionViewService {
    public BigInteger getTrnsactionsList(Users user, ArrayNode recordArrNodes, EntityManager em, int fromRec,
            int maxRecord) {
        return TRANSACTION_VIEW_DAO.getTrnsactionsList(user, recordArrNodes, em, fromRec, maxRecord);
    }

    @Override
    public List<Object[]> getAccountingList(long id, long purposeId, EntityManager entityManager) {
        return TRANSACTION_VIEW_DAO.getAccountingList(id, purposeId, entityManager);
    }

    @Override
    public GenericTransaction getTypeofTransaction(String referenceNumber, EntityManager entityManager) {
        GenericTransaction genericTransaction = new GenericTransaction();

        if (referenceNumber.startsWith("TXN")) {
            Transaction transaction = TRANSACTION_SERVICE.findByReferenceNumber(referenceNumber, entityManager);

            populateTransaction(
                    genericTransaction,
                    transaction.getId(),
                    transaction.getTransactionRefNumber(),
                    transaction.getTransactionPurpose(),
                    transaction.getCreatedAt(),
                    transaction.getApproverActionBy(),
                    transaction.getCreatedBy());
        } else {
            IdosProvisionJournalEntry transaction = PROVISION_JOURNAL_ENTRY_DAO.findByReferenceNumber(referenceNumber,
                    entityManager);
            populateTransaction(
                    genericTransaction,
                    transaction.getId(),
                    transaction.getTransactionRefNumber(),
                    transaction.getTransactionPurpose(),
                    transaction.getCreatedAt(),
                    transaction.getApproverActionBy(),
                    transaction.getCreatedBy());
        }
        return genericTransaction;
    }

    private void populateTransaction(GenericTransaction genericTransaction, Long id, String transactionRefNumber,
            TransactionPurpose transactionPurpose, Date createdAt, Users approverActionBy, Users createdBy) {
        genericTransaction.setId(id);
        genericTransaction.setTransactionRefNumber(transactionRefNumber);
        genericTransaction.setTransactionPurpose(transactionPurpose.getId());
        genericTransaction.setDateCreated(new SimpleDateFormat("yyyy-MM-dd").format(createdAt));
        genericTransaction.setApproval(approverActionBy == null ? "-" : approverActionBy.getEmail());
        genericTransaction.setSubmitter(createdBy == null ? "-" : createdBy.getEmail());
    }
}
