package service;

import model.Users;
import com.fasterxml.jackson.databind.node.ArrayNode;
import pojo.GenericTransaction;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.math.BigInteger;
import java.util.List;

/**
 * @auther Sunil Namdev created on 26.12.2018
 */
public interface TransactionViewService extends BaseService {
    public BigInteger getTrnsactionsList(Users user, ArrayNode recordArrNodes, EntityManager em, int fromRec,
            int maxRecord);

    List<Object[]> getAccountingList(long id, long purposeId, EntityManager entityManager);

    GenericTransaction getTypeofTransaction(String referenceNumber, EntityManager entityManager);
}
