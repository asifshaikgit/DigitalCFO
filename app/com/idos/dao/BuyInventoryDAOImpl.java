package com.idos.dao;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;

import model.*;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.List;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

/**
 * @auther Sunil K. Namdev created on 22.10.2018
 */
public class BuyInventoryDAOImpl implements BuyInventoryDAO {
    private static JPAApi jpaApi;

    @Override
    public void saveInventory4DebitNoteVendorFifo(Transaction txn, TransactionItems txnItem, Users user,
            EntityManager em, Specifics buyItem) throws IDOSException {
        List<Transaction> buyTxnList = Transaction.findByTxnReference(em,
                txn.getTransactionBranchOrganization().getId(), txn.getLinkedTxnRef());
        Transaction buyTxn = null;
        if (buyTxnList != null && !buyTxnList.isEmpty()) {
            buyTxn = buyTxnList.get(0);
        }
        List<TransactionItems> buyTxnItemList = TransactionItems.findByOrgTxnSpecific(em,
                user.getOrganization().getId(), buyTxn.getId(), buyItem.getId());
        TransactionItems buyTxnItem = buyTxnItemList.get(0);
        List<TradingInventory> buyTxnInvList = TradingInventory.findTradingInventory(em, user.getOrganization().getId(),
                txn.getTransactionBranch().getId(), txnItem.getTransactionSpecifics().getId(),
                IdosConstants.TRADING_INV_BUY, buyTxn.getId());
        TradingInventory buyTxnInventory = null;
        if (buyTxnInvList != null && !buyTxnInvList.isEmpty()) {
            buyTxnInventory = buyTxnInvList.get(0);
        }
        double debitQty = buyTxnItem.getNoOfUnits() - txnItem.getNoOfUnits();
        double qtyConvertedToIncomeUnit = debitQty * buyItem.getExpenseToIncomeConverstionRate();
        double debitGross = IdosUtil.convertStringToDouble(
                IdosConstants.decimalFormat.format(debitQty * buyTxnInventory.getCalcualtedRate()));
        TradingInventory inventory = new TradingInventory();
        inventory.setTransactionId(txn.getId());
        inventory.setDate(txn.getTransactionDate());
        inventory.setBranch(txn.getTransactionBranch());
        inventory.setOrganization(txn.getTransactionBranchOrganization());
        inventory.setUser(user);
        inventory.setTransactionSpecifics(buyItem);
        inventory.setTransactionType(IdosConstants.TRADING_INV_PURCHASE_RET);
        inventory.setTotalQuantity(debitQty * -1);
        inventory.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
        inventory.setGrossValue(debitGross * -1);
        inventory.setCalcualtedRate(buyTxnInventory.getCalcualtedRate());
        inventory.setInventoryType(IdosConstants.FIFO_INVENTORY);
        inventory.setQuantityMatchedWithSell(0d);
        genericDao.saveOrUpdate(inventory, user, em);
    }

    @Override
    public void saveInventory4DebitNoteVendorWac(Transaction txn, TransactionItems txnItem, Users user,
            EntityManager em, Specifics buyItem) throws IDOSException {
        List<Transaction> buyTxnList = Transaction.findByTxnReference(em,
                txn.getTransactionBranchOrganization().getId(), txn.getLinkedTxnRef());
        Transaction buyTxn = null;
        if (buyTxnList != null && !buyTxnList.isEmpty()) {
            buyTxn = buyTxnList.get(0);
        }
        List<TransactionItems> buyTxnItemList = TransactionItems.findByOrgTxnSpecific(em,
                user.getOrganization().getId(), buyTxn.getId(), buyItem.getId());
        TransactionItems buyTxnItem = buyTxnItemList.get(0);
        List<TradingInventory> buyTxnInvList = TradingInventory.findTradingInventory(em, user.getOrganization().getId(),
                txn.getTransactionBranch().getId(), txnItem.getTransactionSpecifics().getId(),
                IdosConstants.TRADING_INV_BUY, buyTxn.getId());
        TradingInventory buyTxnInventory = null;
        if (buyTxnInvList != null && !buyTxnInvList.isEmpty()) {
            buyTxnInventory = buyTxnInvList.get(0);
        }
        double debitQty = buyTxnItem.getNoOfUnits() - txnItem.getNoOfUnits();
        double qtyConvertedToIncomeUnit = debitQty * buyItem.getExpenseToIncomeConverstionRate();
        double debitGross = IdosUtil.convertStringToDouble(
                IdosConstants.decimalFormat.format(debitQty * buyTxnInventory.getCalcualtedRate()));
        TradingInventory inventory = new TradingInventory();
        inventory.setTransactionId(txn.getId());
        inventory.setDate(txn.getTransactionDate());
        inventory.setBranch(txn.getTransactionBranch());
        inventory.setOrganization(txn.getTransactionBranchOrganization());
        inventory.setUser(user);
        inventory.setTransactionSpecifics(buyItem);
        inventory.setTransactionType(IdosConstants.TRADING_INV_PURCHASE_RET);
        inventory.setTotalQuantity(debitQty * -1);
        inventory.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
        inventory.setGrossValue(debitGross * -1);
        inventory.setCalcualtedRate(buyTxnInventory.getCalcualtedRate());
        inventory.setInventoryType(IdosConstants.WAC_INVENTORY);
        inventory.setQuantityMatchedWithSell(0d);
        genericDao.saveOrUpdate(inventory, user, em);

        TradingInventory lastClosingInventory = INVENTORY_DAO.getClosingInventory(user.getOrganization().getId(),
                txn.getTransactionBranch().getId(), buyItem.getId(), txn.getTransactionDate(), em);
        if (lastClosingInventory != null) {
            double lastClosingConvertedQty = lastClosingInventory.getNoOfExpUnitsConvertedToIncUnits();
            double lastClosingRate = lastClosingInventory.getCalcualtedRate();
            double lastClosingGrossVal = lastClosingInventory.getGrossValue();
            double newClosingConvertedQty = lastClosingConvertedQty - debitQty;
            double newClosingGrossVal = lastClosingGrossVal - debitGross;
            double newClosingRate = IdosUtil.convertStringToDouble(
                    IdosConstants.decimalFormat.format(newClosingGrossVal / newClosingConvertedQty));
            TradingInventory closingInventory = new TradingInventory();
            closingInventory.setTransactionId(txn.getId());
            closingInventory.setDate(txn.getTransactionDate());
            closingInventory.setBranch(txn.getTransactionBranch());
            closingInventory.setOrganization(txn.getTransactionBranchOrganization());
            closingInventory.setUser(user);
            closingInventory.setTransactionSpecifics(buyItem);
            closingInventory.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL);
            closingInventory.setGrossValue(newClosingGrossVal);
            closingInventory.setNoOfExpUnitsConvertedToIncUnits(newClosingConvertedQty);
            closingInventory.setTotalQuantity(newClosingConvertedQty);
            closingInventory.setCalcualtedRate(newClosingRate);
            closingInventory.setInventoryType(IdosConstants.WAC_INVENTORY);
            closingInventory.setQuantityMatchedWithSell(0d);
            genericDao.saveOrUpdate(closingInventory, user, em);
        }
    }

    @Override
    public void saveInventory4CreditDebitVendorPrice(Transaction txn, TransactionItems txnItem, Users user,
            EntityManager em, Specifics buyItem) throws IDOSException {
        List<Transaction> buyTxnList = Transaction.findByTxnReference(em,
                txn.getTransactionBranchOrganization().getId(), txn.getLinkedTxnRef());
        Transaction buyTxn = null;
        if (buyTxnList != null && !buyTxnList.isEmpty()) {
            buyTxn = buyTxnList.get(0);
        }

        List<TradingInventory> buyTxnInvList = TradingInventory.findTradingInventory(em, user.getOrganization().getId(),
                txn.getTransactionBranch().getId(), txnItem.getTransactionSpecifics().getId(),
                IdosConstants.TRADING_INV_BUY, buyTxn.getId());
        TradingInventory buyTxnInventory = null;
        if (buyTxnInvList != null && !buyTxnInvList.isEmpty()) {
            buyTxnInventory = buyTxnInvList.get(0);
        }
        double newBuyTxnGross = txnItem.getPricePerUnit() * buyTxnInventory.getTotalQuantity();
        buyTxnInventory.setCalcualtedRate(txnItem.getPricePerUnit());
        buyTxnInventory.setGrossValue(newBuyTxnGross);
        if (buyTxnInventory.getPriceChangedTxn() == null) {
            buyTxnInventory.setPriceChangedTxn(txn.getTransactionRefNumber());
        } else {
            buyTxnInventory
                    .setPriceChangedTxn(buyTxnInventory.getPriceChangedTxn() + "," + txn.getTransactionRefNumber());
        }
        genericDao.saveOrUpdate(buyTxnInventory, user, em);

        if (IdosConstants.WAC_METHOD.equalsIgnoreCase(buyItem.getTradingInventoryCalcMethod())) {
            List<TradingInventory> buyTxnClosingInvList = TradingInventory.findTradingInventory(em,
                    user.getOrganization().getId(), txn.getTransactionBranch().getId(),
                    txnItem.getTransactionSpecifics().getId(), IdosConstants.TRADING_INV_CLOSING_BAL, buyTxn.getId());
            TradingInventory buyTxnClosingInventory = null;
            if (buyTxnClosingInvList != null && !buyTxnClosingInvList.isEmpty()) {
                buyTxnClosingInventory = buyTxnClosingInvList.get(0);
            }
            double openingGross = buyTxnClosingInventory.getGrossValue() - buyTxnInventory.getGrossValue();
            double closingGross = openingGross + newBuyTxnGross;
            double closingRate = IdosUtil.convertStringToDouble(
                    IdosConstants.decimalFormat.format(closingGross / buyTxnClosingInventory.getTotalQuantity()));
            buyTxnClosingInventory.setGrossValue(closingGross);
            buyTxnClosingInventory.setCalcualtedRate(closingRate);
            genericDao.saveOrUpdate(buyTxnClosingInventory, user, em);
            List<TradingInventory> txnInvList = TradingInventory.getListAfterMarkedInventory(em,
                    user.getOrganization().getId(), txn.getTransactionBranch().getId(),
                    txnItem.getTransactionSpecifics().getId(), buyTxnClosingInventory.getId());

            for (TradingInventory inventory : txnInvList) {
                TradingInventory closingInventory = null;
                List<TradingInventory> closingInvList = null;
                if (inventory.getTransactionType() == IdosConstants.TRADING_INV_BUY
                        || inventory.getTransactionType() == IdosConstants.TRADING_INV_PURCHASE_RET
                        || inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_EXP
                        || inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_EXP_CREDIT) {
                    closingInvList = TradingInventory.findTradingInventory(em, inventory.getOrganization().getId(),
                            inventory.getBranch().getId(), inventory.getTransactionSpecifics().getId(),
                            IdosConstants.TRADING_INV_CLOSING_BAL, inventory.getTransactionId());
                } else {
                    closingInvList = TradingInventory.findTradingInventory(em, inventory.getOrganization().getId(),
                            inventory.getBranch().getId(), inventory.getBuySpecifics().getId(),
                            IdosConstants.TRADING_INV_CLOSING_BAL, inventory.getTransactionId());
                }
                if (closingInvList != null && !closingInvList.isEmpty()) {
                    closingInventory = closingInvList.get(0);
                }
                if (inventory.getTransactionType() == IdosConstants.TRADING_INV_BUY) {
                    closingGross = inventory.getGrossValue() + closingGross;
                    closingRate = IdosUtil.convertStringToDouble(
                            IdosConstants.decimalFormat.format(closingGross / closingInventory.getTotalQuantity()));
                } else if (inventory.getTransactionType() == IdosConstants.TRADING_INV_SELL) {
                    double inventoryNewGross = IdosUtil.convertStringToDouble(
                            IdosConstants.decimalFormat.format(closingRate * inventory.getTotalQuantity()));
                    inventory.setCalcualtedRate(closingRate);
                    inventory.setGrossValue(inventoryNewGross);
                    closingGross = closingGross - inventoryNewGross;
                    closingRate = IdosUtil.convertStringToDouble(
                            IdosConstants.decimalFormat.format(closingGross / closingInventory.getTotalQuantity()));
                    genericDao.saveOrUpdate(inventory, user, em);
                } else if (inventory.getTransactionType() == IdosConstants.TRADING_INV_PURCHASE_RET) {
                    Transaction debitNoteTxn = Transaction.findById(inventory.getTransactionId());
                    buyTxnList = Transaction.findByTxnReference(em, inventory.getOrganization().getId(),
                            debitNoteTxn.getLinkedTxnRef());
                    buyTxn = null;
                    if (buyTxnList != null && !buyTxnList.isEmpty()) {
                        buyTxn = buyTxnList.get(0);
                    }
                    buyTxnInvList = TradingInventory.findTradingInventory(em, inventory.getOrganization().getId(),
                            inventory.getBranch().getId(), inventory.getTransactionSpecifics().getId(),
                            IdosConstants.TRADING_INV_BUY, buyTxn.getId());
                    buyTxnInventory = null;
                    if (buyTxnInvList != null && !buyTxnInvList.isEmpty()) {
                        buyTxnInventory = buyTxnInvList.get(0);
                    }
                    double inventoryNewGross = IdosUtil.convertStringToDouble(IdosConstants.decimalFormat
                            .format(buyTxnInventory.getCalcualtedRate() * inventory.getTotalQuantity()));
                    inventory.setCalcualtedRate(buyTxnInventory.getCalcualtedRate());
                    inventory.setGrossValue(inventoryNewGross);
                    closingGross = closingGross + inventoryNewGross;
                    closingRate = IdosUtil.convertStringToDouble(
                            IdosConstants.decimalFormat.format(closingGross / closingInventory.getTotalQuantity()));
                    genericDao.saveOrUpdate(inventory, user, em);
                } else if (inventory.getTransactionType() == IdosConstants.TRADING_INV_SALES_RET) {
                    Transaction creditNoteTxn = Transaction.findById(inventory.getTransactionId());
                    List<Transaction> sellTxnList = Transaction.findByTxnReference(em,
                            inventory.getOrganization().getId(), creditNoteTxn.getLinkedTxnRef());
                    Transaction sellTxn = null;
                    if (sellTxnList != null && !sellTxnList.isEmpty()) {
                        sellTxn = sellTxnList.get(0);
                    }

                    List<TradingInventory> sellTxnInvList = TradingInventory.findSellInventory(em,
                            inventory.getOrganization().getId(), inventory.getBranch().getId(),
                            inventory.getBuySpecifics().getId(), IdosConstants.TRADING_INV_SELL, sellTxn.getId());
                    TradingInventory sellTxnInventory = null;
                    if (sellTxnInvList != null && !sellTxnInvList.isEmpty()) {
                        sellTxnInventory = sellTxnInvList.get(0);
                    }

                    double inventoryNewGross = IdosUtil.convertStringToDouble(IdosConstants.decimalFormat
                            .format(sellTxnInventory.getCalcualtedRate() * inventory.getTotalQuantity()));
                    inventory.setCalcualtedRate(sellTxnInventory.getCalcualtedRate());
                    inventory.setGrossValue(inventoryNewGross);
                    closingGross = closingGross + inventoryNewGross;
                    closingRate = IdosUtil.convertStringToDouble(
                            IdosConstants.decimalFormat.format(closingGross / closingInventory.getTotalQuantity()));
                    genericDao.saveOrUpdate(inventory, user, em);
                } else if (inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_EXP) {
                    closingGross = inventory.getGrossValue() + closingGross;
                    closingRate = IdosUtil.convertStringToDouble(
                            IdosConstants.decimalFormat.format(closingGross / closingInventory.getTotalQuantity()));
                } else if (inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_INC
                        || inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_INC_DEBIT
                        || inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_EXP_CREDIT) {
                    double inventoryNewGross = IdosUtil.convertStringToDouble(
                            IdosConstants.decimalFormat.format(closingRate * inventory.getTotalQuantity()));
                    inventory.setCalcualtedRate(closingRate);
                    inventory.setGrossValue(inventoryNewGross);
                    if (inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_EXP_CREDIT) {
                        closingGross = closingGross + inventoryNewGross;
                    } else {
                        closingGross = closingGross - inventoryNewGross;
                    }
                    closingRate = IdosUtil.convertStringToDouble(
                            IdosConstants.decimalFormat.format(closingGross / closingInventory.getTotalQuantity()));
                    genericDao.saveOrUpdate(inventory, user, em);
                } /*
                   * else if(inventory.getTransactionType() ==
                   * IdosConstants.TRADING_INV_PJE_INC_DEBIT){
                   * double inventoryNewGross =
                   * Double.parseDouble(IdosConstants.decimalFormat.format(closingRate *
                   * inventory.getTotalQuantity()));
                   * inventory.setCalcualtedRate(closingRate);
                   * inventory.setGrossValue(inventoryNewGross);
                   * closingGross = closingGross - inventoryNewGross;
                   * closingRate =
                   * Double.parseDouble(IdosConstants.decimalFormat.format(closingGross/
                   * closingInventory.getTotalQuantity()));
                   * genericDao.saveOrUpdate(inventory, user, em);
                   * } else if(inventory.getTransactionType() ==
                   * IdosConstants.TRADING_INV_PJE_EXP_CREDIT){
                   * double inventoryNewGross =
                   * Double.parseDouble(IdosConstants.decimalFormat.format(closingRate *
                   * inventory.getTotalQuantity()));
                   * inventory.setCalcualtedRate(closingRate);
                   * inventory.setGrossValue(inventoryNewGross);
                   * closingGross = closingGross + inventoryNewGross;
                   * closingRate =
                   * Double.parseDouble(IdosConstants.decimalFormat.format(closingGross/
                   * closingInventory.getTotalQuantity()));
                   * genericDao.saveOrUpdate(inventory, user, em);
                   * }
                   */
                closingInventory.setCalcualtedRate(closingRate);
                closingInventory.setGrossValue(closingGross);
                genericDao.saveOrUpdate(closingInventory, user, em);
            }
        }
    }
}
