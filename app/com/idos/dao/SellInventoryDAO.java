package com.idos.dao;

import com.idos.util.IDOSException;
import model.Specifics;
import model.Transaction;
import model.TransactionItems;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @auther Sunil K. Namdev created on 18.10.2018
 */
public interface SellInventoryDAO extends BaseDAO {
    void saveInventory4CreditNoteCustomerFifo(Transaction txn, TransactionItems txnItemrow, Users user,
            EntityManager em, Specifics sellSpecific, Specifics buySpecifics) throws IDOSException;

    void saveInventory4CreditNoteCustomerWac(Transaction txn, TransactionItems txnItemrow, Users user, EntityManager em,
            Specifics sellSpecific, Specifics buySpecifics) throws IDOSException;
}
