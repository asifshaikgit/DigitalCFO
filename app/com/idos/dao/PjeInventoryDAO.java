package com.idos.dao;

import com.idos.util.IDOSException;
import model.IdosProvisionJournalEntry;
import model.ProvisionJournalEntryDetail;
import model.Specifics;
import model.Users;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @auther Sunil K. Namdev created on 25.10.2018
 */
public interface PjeInventoryDAO extends BaseDAO {
    void saveTradingInventory(IdosProvisionJournalEntry txn, ProvisionJournalEntryDetail txnItemrow, Specifics specific,
            Users user, EntityManager em) throws IDOSException;
}
