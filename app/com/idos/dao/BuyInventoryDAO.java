package com.idos.dao;

import com.idos.util.IDOSException;
import model.Specifics;
import model.Transaction;
import model.TransactionItems;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @auther Sunil K. Namdev created on 22.10.2018
 */
public interface BuyInventoryDAO extends BaseDAO {
    void saveInventory4DebitNoteVendorFifo(Transaction txn, TransactionItems txnItemrow, Users user, EntityManager em,
            Specifics buySpecifics) throws IDOSException;

    void saveInventory4DebitNoteVendorWac(Transaction txn, TransactionItems txnItemrow, Users user, EntityManager em,
            Specifics buySpecifics) throws IDOSException;

    void saveInventory4CreditDebitVendorPrice(Transaction txn, TransactionItems txnItem, Users user, EntityManager em,
            Specifics buyItem) throws IDOSException;
}
