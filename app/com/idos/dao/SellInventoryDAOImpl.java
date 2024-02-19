package com.idos.dao;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;

import model.*;

import javax.inject.Inject;
import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.List;

/**
 * @auther Sunil K. Namdev created on 18.10.2018
 */
public class SellInventoryDAOImpl implements SellInventoryDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public void saveInventory4CreditNoteCustomerFifo(Transaction txn, TransactionItems txnItem, Users user,
            EntityManager em, Specifics sellItem, Specifics buyItem) throws IDOSException {
        List<Transaction> sellTxnList = Transaction.findByTxnReference(em,
                txn.getTransactionBranchOrganization().getId(), txn.getLinkedTxnRef());
        Transaction sellTxn = null;
        if (sellTxnList != null && !sellTxnList.isEmpty()) {
            sellTxn = sellTxnList.get(0);
        }
        List<TradingInventory> sellTxnInvList = TradingInventory.findTradingInventory(em,
                user.getOrganization().getId(), txn.getTransactionBranch().getId(),
                txnItem.getTransactionSpecifics().getId(), IdosConstants.TRADING_INV_SELL, sellTxn.getId());
        TradingInventory sellTxnInventory = null;
        if (sellTxnInvList != null && !sellTxnInvList.isEmpty()) {
            sellTxnInventory = sellTxnInvList.get(0);
        } else {
            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                    "FIFO Inventory not found", "Inventory not found for " + txnItem.getTransactionSpecifics().getId()
                            + " " + txnItem.getTransactionSpecifics().getName());
        }
        double creditGross = IdosUtil.convertStringToDouble(
                IdosConstants.decimalFormat.format(txnItem.getNoOfUnits() * sellTxnInventory.getCalcualtedRate()));
        TradingInventory inventory = new TradingInventory();
        inventory.setTransactionType(IdosConstants.TRADING_INV_SALES_RET);
        inventory.setTransactionId(txn.getId());
        inventory.setDate(txn.getTransactionDate());
        inventory.setBranch(txn.getTransactionBranch());
        inventory.setOrganization(txn.getTransactionBranchOrganization());
        inventory.setUser(user);
        inventory.setTransactionSpecifics(sellItem);
        inventory.setBuySpecifics(buyItem);
        inventory.setTotalQuantity(txnItem.getNoOfUnits() * -1);
        inventory.setCalcualtedRate(sellTxnInventory.getCalcualtedRate());
        inventory.setGrossValue(creditGross * -1);
        inventory.setNoOfExpUnitsConvertedToIncUnits(txnItem.getNoOfUnits());
        inventory.setTransactionGorss(txnItem.getGrossAmount());
        inventory.setInventoryType(IdosConstants.FIFO_INVENTORY);
        inventory.setQuantityMatchedWithSell(0d);
        genericDao.saveOrUpdate(inventory, user, em);
    }

    @Override
    public void saveInventory4CreditNoteCustomerWac(Transaction txn, TransactionItems txnItem, Users user,
            EntityManager em, Specifics sellItem, Specifics buyItem) throws IDOSException {
        List<Transaction> sellTxnList = Transaction.findByTxnReference(em,
                txn.getTransactionBranchOrganization().getId(), txn.getLinkedTxnRef());
        Transaction sellTxn = null;
        if (sellTxnList != null && !sellTxnList.isEmpty()) {
            sellTxn = sellTxnList.get(0);
        }
        List<TransactionItems> sellTxnItemList = TransactionItems.findByOrgTxnSpecific(em,
                user.getOrganization().getId(), sellTxn.getId(), sellItem.getId());
        TransactionItems sellTxnItem = sellTxnItemList.get(0);
        double creditQty = sellTxnItem.getNoOfUnits() - txnItem.getNoOfUnits();
        List<TradingInventory> sellTxnInvList = TradingInventory.findTradingInventory(em,
                user.getOrganization().getId(), txn.getTransactionBranch().getId(),
                txnItem.getTransactionSpecifics().getId(), IdosConstants.TRADING_INV_SELL, sellTxn.getId());
        TradingInventory sellTxnInventory = null;
        if (sellTxnInvList != null && !sellTxnInvList.isEmpty()) {
            sellTxnInventory = sellTxnInvList.get(0);
        } else {
            throw new IDOSException(IdosConstants.RECORD_NOT_FOUND, IdosConstants.BUSINESS_EXCEPTION,
                    "WAC Inventory not found", "Inventory not found for " + txnItem.getTransactionSpecifics().getId());
        }
        double creditGross = IdosUtil.convertStringToDouble(
                IdosConstants.decimalFormat.format(creditQty * sellTxnInventory.getCalcualtedRate()));
        TradingInventory inventory = new TradingInventory();
        inventory.setTransactionId(txn.getId());
        inventory.setDate(txn.getTransactionDate());
        inventory.setBranch(txn.getTransactionBranch());
        inventory.setOrganization(txn.getTransactionBranchOrganization());
        inventory.setUser(user);
        inventory.setTransactionSpecifics(sellItem);
        inventory.setBuySpecifics(buyItem); // buy specific for this sell trade, so that we can group by buySpecific
                                            // when showing consolidated sell trades for this buy item
        inventory.setTransactionType(IdosConstants.TRADING_INV_SALES_RET);
        inventory.setTotalQuantity(creditQty * -1);
        inventory.setGrossValue(creditGross * -1);
        inventory.setTransactionGorss(txnItem.getGrossAmount());
        inventory.setCalcualtedRate(sellTxnInventory.getCalcualtedRate());
        inventory.setNoOfExpUnitsConvertedToIncUnits(creditQty);
        inventory.setQuantityMatchedWithSell(0d);
        inventory.setInventoryType(IdosConstants.WAC_INVENTORY);
        genericDao.saveOrUpdate(inventory, user, em); // save sell trade

        TradingInventory lastClosingInventory = INVENTORY_DAO.getClosingInventory(user.getOrganization().getId(),
                txn.getTransactionBranch().getId(), buyItem.getId(), txn.getTransactionDate(), em);
        if (lastClosingInventory != null) {
            double lastClosingConvertedQty = lastClosingInventory.getNoOfExpUnitsConvertedToIncUnits();
            double lastClosingRate = lastClosingInventory.getCalcualtedRate();
            double lastClosingGrossVal = lastClosingInventory.getGrossValue();
            double newclosingconvertedqty = lastClosingConvertedQty + creditQty;
            double newclosinggrossval = lastClosingGrossVal + creditGross;
            double newclosingrate = IdosUtil.convertStringToDouble(
                    IdosConstants.decimalFormat.format(newclosinggrossval / newclosingconvertedqty));
            TradingInventory closingInventory = new TradingInventory();
            closingInventory.setTransactionId(txn.getId());
            closingInventory.setDate(txn.getTransactionDate());
            closingInventory.setBranch(txn.getTransactionBranch());
            closingInventory.setOrganization(txn.getTransactionBranchOrganization());
            closingInventory.setUser(user);
            closingInventory.setTransactionSpecifics(buyItem);
            closingInventory.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL);
            closingInventory.setGrossValue(newclosinggrossval);
            closingInventory.setNoOfExpUnitsConvertedToIncUnits(newclosingconvertedqty);
            closingInventory.setTotalQuantity(newclosingconvertedqty);
            closingInventory.setCalcualtedRate(newclosingrate);
            closingInventory.setQuantityMatchedWithSell(0d);
            closingInventory.setInventoryType(IdosConstants.WAC_INVENTORY);
            genericDao.saveOrUpdate(closingInventory, user, em); // save closing entry for buy trade
        }
    }
}
