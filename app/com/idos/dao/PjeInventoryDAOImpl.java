package com.idos.dao;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.IdosUtil;
import java.util.logging.Level;
import model.*;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import play.db.jpa.JPAApi;
import java.util.*;

/**
 * @auther Sunil K. Namdev created on 25.10.2018
 */
public class PjeInventoryDAOImpl implements PjeInventoryDAO {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    @Override
    public void saveTradingInventory(IdosProvisionJournalEntry txn, ProvisionJournalEntryDetail txnItemrow,
            Specifics specific, Users user, EntityManager em) throws IDOSException {
        if (specific.getAccountCodeHirarchy().startsWith("/1")) {
            saveTradingInventoryIncome(txn, txnItemrow, specific, user, em);
        } else if (specific.getAccountCodeHirarchy().startsWith("/2")) {
            int inventoryType = IdosConstants.FIFO_INVENTORY;
            if (specific.getTradingInventoryCalcMethod().equalsIgnoreCase("WAC")) {
                inventoryType = IdosConstants.WAC_INVENTORY;
            }
            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == txnItemrow.getIsDebit()
                    && inventoryType == IdosConstants.FIFO_INVENTORY) {
                saveTradingInventoryExpenseFifoReturn(txn, txnItemrow, specific, user, em, inventoryType);
            } else {
                saveTradingInventoryExpense(txn, txnItemrow, specific, user, em, inventoryType);
            }
        }
    }

    private void saveTradingInventoryExpense(IdosProvisionJournalEntry txn, ProvisionJournalEntryDetail txnItemrow,
            Specifics buySpecific, Users user, EntityManager em, int inventoryType) {
        try {
            Branch branch = null;
            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == txnItemrow.getIsDebit()) {
                branch = txn.getCreditBranch();
            } else {
                branch = txn.getDebitBranch();
            }
            double qtyConvertedToIncomeUnit = txnItemrow.getUnits() * buySpecific.getExpenseToIncomeConverstionRate();
            TradingInventory inventory = new TradingInventory();
            inventory.setTransactionId(txn.getId());
            inventory.setDate(txn.getTransactionDate());
            inventory.setBranch(branch);
            inventory.setOrganization(txn.getProvisionMadeForOrganization());
            inventory.setUser(user);
            inventory.setTransactionSpecifics(buySpecific);
            inventory.setTransactionGorss(txnItemrow.getHeadAmount());
            inventory.setCalcualtedRate(txnItemrow.getUnitPrice());
            inventory.setTotalQuantity(txnItemrow.getUnits());
            inventory.setGrossValue(txnItemrow.getHeadAmount());
            inventory.setTransactionType(IdosConstants.TRADING_INV_PJE_EXP);
            inventory.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
            inventory.setQuantityMatchedWithSell(0d);
            inventory.setInventoryType(inventoryType);
            genericDao.saveOrUpdate(inventory, user, em); // save buy trade
            TradingInventory lastClosingInventory = INVENTORY_DAO.getClosingInventory(user.getOrganization().getId(),
                    branch.getId(), buySpecific.getId(), txn.getTransactionDate(), em);
            if (inventoryType == IdosConstants.WAC_INVENTORY) {
                TradingInventory closingInventory = new TradingInventory();
                closingInventory.setTransactionId(txn.getId());
                closingInventory.setDate(txn.getTransactionDate());
                closingInventory.setOrganization(txn.getProvisionMadeForOrganization());
                closingInventory.setUser(user);
                closingInventory.setBranch(branch);
                closingInventory.setTransactionSpecifics(buySpecific);
                closingInventory.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL);
                closingInventory.setQuantityMatchedWithSell(0d);
                if (lastClosingInventory != null) {
                    double lastTotalClosingQty = lastClosingInventory.getTotalQuantity();
                    double lastClosingGrossVal = lastClosingInventory.getGrossValue();
                    double newClosingQty = 0.0;
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == txnItemrow.getIsDebit()) {
                        newClosingQty = lastTotalClosingQty - qtyConvertedToIncomeUnit;
                    } else {
                        newClosingQty = lastTotalClosingQty + qtyConvertedToIncomeUnit;
                    }
                    double newClosingGross = lastClosingGrossVal + inventory.getGrossValue();
                    double newClosingRate = IdosConstants.decimalFormat
                            .parse(IdosConstants.decimalFormat.format(newClosingGross / newClosingQty)).doubleValue();
                    closingInventory.setTotalQuantity(newClosingQty);
                    closingInventory.setGrossValue(newClosingGross);
                    closingInventory.setCalcualtedRate(newClosingRate);
                    closingInventory.setNoOfExpUnitsConvertedToIncUnits(newClosingQty);
                } else {
                    closingInventory.setTotalQuantity(txnItemrow.getUnits());
                    closingInventory.setGrossValue(txnItemrow.getHeadAmount());
                    closingInventory.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
                    double buyRate = new Double(
                            IdosConstants.decimalFormat.format(txnItemrow.getHeadAmount() / qtyConvertedToIncomeUnit));
                    closingInventory.setCalcualtedRate(buyRate);
                }
                closingInventory.setInventoryType(inventoryType);
                genericDao.saveOrUpdate(closingInventory, user, em); // save closing entry for buy trade

                if (txn.getIsBackdatedTransaction() != null && txn.getIsBackdatedTransaction() == 1) { // if backdated
                                                                                                       // JE
                                                                                                       // transaction,
                                                                                                       // then need to
                                                                                                       // adjust rates
                                                                                                       // after this
                                                                                                       // date for all
                                                                                                       // in
                                                                                                       // Trading_inventory
                                                                                                       // table
                    adjustTradingInventoryForBackDatedTransactionWAC(
                            closingInventory.getNoOfExpUnitsConvertedToIncUnits(), closingInventory.getCalcualtedRate(),
                            closingInventory.getGrossValue(), txn.getTransactionDate(), user, em, branch, buySpecific);
                }
            } else {
                if (txn.getIsBackdatedTransaction() != null && txn.getIsBackdatedTransaction() == 1) { // if backdated
                                                                                                       // JE
                                                                                                       // transaction,
                                                                                                       // then need to
                                                                                                       // adjust rates
                                                                                                       // after this
                                                                                                       // date for all
                                                                                                       // in
                                                                                                       // Trading_inventory
                                                                                                       // table
                    LinkedHashMap<Long, TradingInventory> buyTranFIFO = new LinkedHashMap<Long, TradingInventory>();
                    buyTranFIFO.put(inventory.getId(), inventory);
                    adjustTradingInventoryForBackDatedTransactionFIFO(txn.getTransactionDate(), user, em, branch,
                            buySpecific, buyTranFIFO);
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
    }

    private void saveTradingInventoryExpenseFifoReturn(IdosProvisionJournalEntry txn,
            ProvisionJournalEntryDetail txnItemrow, Specifics buySpecific, Users user, EntityManager em,
            int inventoryType) {
        try {
            Branch branch = null;
            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == txnItemrow.getIsDebit()) {
                branch = txn.getCreditBranch();
            } else {
                branch = txn.getDebitBranch();
            }
            String sbr = "select obj from TradingInventory obj where obj.transactionType in (1,3,6,7,10) and obj.organization.id=?1 and obj.branch.id=?2 and obj.presentStatus=1 and (obj.transactionSpecifics.id=?3 or obj.buySpecifics.id=?4) and obj.quantityMatchedWithSell < obj.noOfExpUnitsConvertedToIncUnits and obj.date < ?5 order by obj.id";
            ArrayList inparams = new ArrayList();
            inparams.add(user.getOrganization().getId());
            inparams.add(branch.getId());
            inparams.add(buySpecific.getId());
            inparams.add(buySpecific.getId());
            inparams.add(txn.getTransactionDate());
            List<TradingInventory> buyTrades = genericDao.queryWithParams(sbr, em, inparams);

            double returnQty = txnItemrow.getUnits();
            double remainingQty = returnQty;
            for (TradingInventory buyTrade : buyTrades) {
                String linkedBuyTrades = "";
                double buyQty = buyTrade.getNoOfExpUnitsConvertedToIncUnits();
                double buyQtyAdjustedWithSellQty = buyTrade.getQuantityMatchedWithSell();
                double remainingBuyQuantity = 0.0;
                double sellQtyMatched = 0.0;
                double sellQtyGross = 0.0;
                if (buyQtyAdjustedWithSellQty < buyQty) {
                    remainingBuyQuantity = buyQty - buyQtyAdjustedWithSellQty;
                    if (remainingQty <= remainingBuyQuantity) {
                        sellQtyGross = (buyTrade.getCalcualtedRate()) * remainingQty;
                        sellQtyMatched = remainingQty;
                        buyTrade.setQuantityMatchedWithSell(buyTrade.getQuantityMatchedWithSell() + remainingQty);
                        remainingQty = 0;
                    } else {
                        sellQtyGross = buyTrade.getCalcualtedRate() * remainingBuyQuantity;
                        sellQtyMatched = remainingBuyQuantity;
                        buyTrade.setQuantityMatchedWithSell(
                                buyTrade.getQuantityMatchedWithSell() + remainingBuyQuantity);
                        remainingQty = remainingQty - remainingBuyQuantity;
                    }
                    linkedBuyTrades = linkedBuyTrades + buyTrade.getId() + ",";
                    genericDao.saveOrUpdate(buyTrade, user, em);
                    TradingInventory inventory = new TradingInventory();
                    inventory.setTransactionId(txn.getId());
                    inventory.setDate(txn.getTransactionDate());
                    inventory.setOrganization(txn.getProvisionMadeForOrganization());
                    inventory.setUser(user);
                    inventory.setTransactionSpecifics(buySpecific);
                    inventory.setBuySpecifics(buySpecific);
                    inventory.setBranch(txn.getCreditBranch());
                    inventory.setTotalQuantity(sellQtyMatched);
                    inventory.setGrossValue(sellQtyGross);
                    inventory.setTransactionType(IdosConstants.TRADING_INV_PJE_EXP_CREDIT);
                    inventory.setCalcualtedRate(buyTrade.getCalcualtedRate());
                    inventory.setLinkedBuyIds(linkedBuyTrades);
                    inventory.setTransactionGorss(txnItemrow.getHeadAmount());
                    inventory.setInventoryType(IdosConstants.FIFO_INVENTORY);
                    inventory.setQuantityMatchedWithSell(0d);
                    genericDao.saveOrUpdate(inventory, user, em);
                    if (txn.getIsBackdatedTransaction() != null && txn.getIsBackdatedTransaction() == 1) { // if
                                                                                                           // backdated
                                                                                                           // JE
                                                                                                           // transaction,
                                                                                                           // then need
                                                                                                           // to adjust
                                                                                                           // rates
                                                                                                           // after this
                                                                                                           // date for
                                                                                                           // all in
                                                                                                           // Trading_inventory
                                                                                                           // table
                        LinkedHashMap<Long, TradingInventory> buyTranFIFO = new LinkedHashMap<Long, TradingInventory>();
                        buyTranFIFO.put(inventory.getId(), inventory);
                        adjustTradingInventoryForBackDatedTransactionFIFO(txn.getTransactionDate(), user, em, branch,
                                buySpecific, buyTranFIFO);
                    }
                    if (remainingQty <= 0) {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error", ex);
        }
    }

    private void saveTradingInventoryIncome(IdosProvisionJournalEntry txn, ProvisionJournalEntryDetail txnItemrow,
            Specifics sellSpecific, Users user, EntityManager em) throws IDOSException {

        if (sellSpecific.getIsCombinationSales() != null && sellSpecific.getIsCombinationSales() == 1) {
            StringBuilder newsbquery = new StringBuilder(
                    "select obj from SpecificsCombinationSales obj WHERE obj.specificsId.id = '" + sellSpecific.getId()
                            + "' and obj.organization.id ='" + user.getOrganization().getId()
                            + "' and obj.presentStatus=1");
            List<SpecificsCombinationSales> specificsList = genericDao.executeSimpleQuery(newsbquery.toString(), em);
            for (SpecificsCombinationSales combSpec : specificsList) {
                sellSpecific = combSpec.getCombSpecificsId();
                String tradInvCalcMethod = sellSpecific.getTradingInventoryCalcMethod();
                if (sellSpecific.getIsTradingInvenotryItem() != null && sellSpecific.getIsTradingInvenotryItem() == 1
                        && sellSpecific.getLinkIncomeExpenseSpecifics() != null) {
                    Long buySpecificId = sellSpecific.getLinkIncomeExpenseSpecifics().getId();
                    Specifics buySpecific = Specifics.findById(buySpecificId);
                    double sellQty = txnItemrow.getUnits() * combSpec.getOpeningBalUnits();
                    if (tradInvCalcMethod.equalsIgnoreCase("FIFO")) {
                        saveIncomeInventoryFIFO(txn, sellQty, user, em, buySpecific, sellSpecific, txnItemrow);
                    } else if (tradInvCalcMethod.equalsIgnoreCase("WAC")) {
                        saveIncomeInventoryWAC(txn, sellQty, user, em, buySpecific, sellSpecific, txnItemrow);
                    }
                }
            }
        } else {
            String tradInvCalcMethod = sellSpecific.getTradingInventoryCalcMethod();
            if (sellSpecific.getLinkIncomeExpenseSpecifics() != null) {
                Specifics buySpecific = sellSpecific.getLinkIncomeExpenseSpecifics();
                double sellQty = txnItemrow.getUnits();
                if (tradInvCalcMethod.equalsIgnoreCase("FIFO")) {
                    if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == txnItemrow.getIsDebit()) {
                        saveIncomeInventoryFIFO(txn, sellQty, user, em, buySpecific, sellSpecific, txnItemrow);
                    } else {
                        saveIncomeInventoryFifoReturn(txn, sellQty, user, em, buySpecific, sellSpecific, txnItemrow);
                    }
                } else if (tradInvCalcMethod.equalsIgnoreCase("WAC")) {
                    saveIncomeInventoryWAC(txn, sellQty, user, em, buySpecific, sellSpecific, txnItemrow);
                }
            }
        }

    }

    private void saveIncomeInventoryFIFO(IdosProvisionJournalEntry txn, double sellQty, Users user, EntityManager em,
            Specifics buySpecific, Specifics sellSpecific, ProvisionJournalEntryDetail txnItemrow) {
        if (sellQty <= 0) {
            return;
        }
        Long branchid = null;
        if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == txnItemrow.getIsDebit()) {
            branchid = txn.getCreditBranch().getId();
        } else {
            branchid = txn.getDebitBranch().getId();
        }
        String sbr = "select obj from TradingInventory obj where obj.transactionType in (1,3,6,7,10) and obj.organization.id=?1 and obj.branch.id=?2 and obj.presentStatus=1 and (obj.transactionSpecifics.id=?3 or obj.buySpecifics.id=?4) and obj.quantityMatchedWithSell < obj.noOfExpUnitsConvertedToIncUnits and obj.date < ?5 order by obj.date";
        ArrayList inparams = new ArrayList();
        inparams.add(user.getOrganization().getId());
        inparams.add(branchid);
        inparams.add(buySpecific.getId());
        inparams.add(buySpecific.getId());
        inparams.add(txn.getTransactionDate());
        List<TradingInventory> buyTrades = genericDao.queryWithParams(sbr, em, inparams);
        double sellQtyRemaining = sellQty;
        for (TradingInventory buytrade : buyTrades) {
            String linkedBuyTrades = "";
            double buyQty = buytrade.getNoOfExpUnitsConvertedToIncUnits();
            double buyQtyAdjustedWithSellQty = buytrade.getQuantityMatchedWithSell();
            double remainingBuyQuantity = 0.0;
            double sellQtyMatched = 0.0;
            double sellQtyGross = 0.0;
            if (buyQtyAdjustedWithSellQty < buyQty) {
                remainingBuyQuantity = buyQty - buyQtyAdjustedWithSellQty;
                if (sellQtyRemaining <= remainingBuyQuantity) {
                    sellQtyGross = (buytrade.getCalcualtedRate()) * sellQtyRemaining;
                    sellQtyMatched = sellQtyRemaining;
                    buytrade.setQuantityMatchedWithSell(buytrade.getQuantityMatchedWithSell() + sellQtyRemaining);
                    sellQtyRemaining = 0;
                } else {
                    sellQtyGross = buytrade.getCalcualtedRate() * remainingBuyQuantity;
                    sellQtyMatched = remainingBuyQuantity;
                    buytrade.setQuantityMatchedWithSell(buytrade.getQuantityMatchedWithSell() + remainingBuyQuantity);
                    sellQtyRemaining = sellQtyRemaining - remainingBuyQuantity;
                }
                linkedBuyTrades = linkedBuyTrades + buytrade.getId() + ",";
                genericDao.saveOrUpdate(buytrade, user, em);
                TradingInventory inventory = new TradingInventory();
                inventory.setTransactionId(txn.getId());
                inventory.setDate(txn.getTransactionDate());
                inventory.setOrganization(txn.getProvisionMadeForOrganization());
                inventory.setUser(user);
                inventory.setTransactionSpecifics(sellSpecific);
                inventory.setBuySpecifics(buySpecific);
                inventory.setBranch(txn.getCreditBranch());
                inventory.setTotalQuantity(sellQtyMatched);
                inventory.setGrossValue(sellQtyGross);
                inventory.setTransactionType(IdosConstants.TRADING_INV_PJE_INC);
                inventory.setCalcualtedRate(buytrade.getCalcualtedRate());
                inventory.setLinkedBuyIds(linkedBuyTrades);
                inventory.setTransactionGorss(txnItemrow.getHeadAmount());
                inventory.setInventoryType(IdosConstants.FIFO_INVENTORY);
                genericDao.saveOrUpdate(inventory, user, em);
                if (sellQtyRemaining <= 0) {
                    break;
                }
            }
        }
    }

    private void saveIncomeInventoryFifoReturn(IdosProvisionJournalEntry txn, double sellQty, Users user,
            EntityManager em, Specifics buySpecific, Specifics sellSpecific, ProvisionJournalEntryDetail txnItemrow)
            throws IDOSException {
        if (sellQty <= 0) {
            return;
        }
        Long branchid = null;
        if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == txnItemrow.getIsDebit()) {
            branchid = txn.getCreditBranch().getId();
        } else {
            branchid = txn.getDebitBranch().getId();
        }
        String sbr = "select obj from TradingInventory obj where obj.transactionType in (1,3,6,7,10) and obj.organization.id=?1 and obj.branch.id=?2 and obj.presentStatus=1 and (obj.transactionSpecifics.id=?3 or obj.buySpecifics.id=?4) and obj.quantityMatchedWithSell < obj.noOfExpUnitsConvertedToIncUnits and obj.date < ?5 order by obj.id";
        ArrayList inparams = new ArrayList(5);
        inparams.add(user.getOrganization().getId());
        inparams.add(branchid);
        inparams.add(buySpecific.getId());
        inparams.add(buySpecific.getId());
        inparams.add(txn.getTransactionDate());
        List<TradingInventory> buyTrades = genericDao.queryWithParams(sbr, em, inparams);
        if (buyTrades.size() > 0) {
            TradingInventory buyTrade = buyTrades.get(0);
            TradingInventory inventory = new TradingInventory();
            inventory.setTransactionId(txn.getId());
            inventory.setDate(txn.getTransactionDate());
            inventory.setOrganization(txn.getProvisionMadeForOrganization());
            inventory.setUser(user);
            inventory.setTransactionSpecifics(sellSpecific);
            inventory.setBuySpecifics(buySpecific);
            inventory.setBranch(txn.getDebitBranch());
            inventory.setTotalQuantity(txnItemrow.getUnits() * -1);
            inventory.setGrossValue(txnItemrow.getUnits() * buyTrade.getCalcualtedRate() * -1);
            inventory.setTransactionType(IdosConstants.TRADING_INV_PJE_INC_DEBIT);
            inventory.setCalcualtedRate(buyTrade.getCalcualtedRate());
            inventory.setTransactionGorss(txnItemrow.getHeadAmount());
            inventory.setInventoryType(IdosConstants.FIFO_INVENTORY);
            genericDao.saveOrUpdate(inventory, user, em);
        } else {
            throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION,
                    IdosConstants.INVALID_DATA_EXCEPTION, "Linked expense :" + buySpecific.getName()
                            + " item not found for income item: " + sellSpecific.getName() + " in inventory.");
        }
    }

    private void saveIncomeInventoryWAC(IdosProvisionJournalEntry txn, double sellQty, Users user, EntityManager em,
            Specifics buySpecific, Specifics sellSpecific, ProvisionJournalEntryDetail txnItemrow)
            throws IDOSException {
        Branch branch = null;
        if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == txnItemrow.getIsDebit()) {
            branch = txn.getCreditBranch();
        } else {
            branch = txn.getDebitBranch();
        }
        Long branchid = branch.getId();
        Date transactionDate = txn.getTransactionDate();// Manali: For backdated get the last trade on or before that
                                                        // date, it should hold true for normal trade as well
        // <=transactionDate introduced for backdated transaction, but if backdated is
        // before OB transaction, then take OB as last transaction so IS null added...
        TradingInventory lastClosingInventory = INVENTORY_DAO.getClosingInventory(user.getOrganization().getId(),
                branch.getId(), buySpecific.getId(), transactionDate, em);
        if (lastClosingInventory != null) {
            double lastClosingConvertedQty = lastClosingInventory.getNoOfExpUnitsConvertedToIncUnits();
            double lastClosingRate = lastClosingInventory.getCalcualtedRate();
            double lastClosingGrossVal = lastClosingInventory.getGrossValue();
            double sellGross = IdosUtil
                    .convertStringToDouble(IdosConstants.decimalFormat.format(sellQty * lastClosingRate));
            TradingInventory inventory = new TradingInventory();
            inventory.setTransactionId(txn.getId());
            inventory.setDate(txn.getTransactionDate());
            inventory.setOrganization(txn.getProvisionMadeForOrganization());
            inventory.setUser(user);
            inventory.setBranch(branch);
            inventory.setTransactionSpecifics(sellSpecific);
            inventory.setBuySpecifics(buySpecific);

            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == txnItemrow.getIsDebit()) {
                inventory.setTotalQuantity(sellQty);
                inventory.setGrossValue(sellGross);
                inventory.setCalcualtedRate(lastClosingRate);
                inventory.setTransactionType(IdosConstants.TRADING_INV_PJE_INC);
            } else {
                inventory.setTotalQuantity(sellQty * -1);
                inventory.setGrossValue(sellGross * -1);
                inventory.setCalcualtedRate(lastClosingRate);
                inventory.setTransactionType(IdosConstants.TRADING_INV_PJE_INC_DEBIT);
            }
            inventory.setTransactionGorss(txnItemrow.getHeadAmount());
            if (txn.getIsBackdatedTransaction() != null && txn.getIsBackdatedTransaction() == 1) {
                inventory.setIsBackdatedTransaction(1);
            }
            genericDao.saveOrUpdate(inventory, user, em);

            double newClosingConvertedQty = 0.0;
            double newClosingGrossVal = 0.0;
            double newClosingRate = 0.0;
            TradingInventory closingInventory = new TradingInventory();
            closingInventory.setTransactionId(txn.getId());
            closingInventory.setDate(txn.getTransactionDate());
            closingInventory.setOrganization(txn.getProvisionMadeForOrganization());
            closingInventory.setUser(user);
            closingInventory.setBranch(branch);
            closingInventory.setTransactionSpecifics(buySpecific);

            if (IdosConstants.PROVISION_JOURNAL_ENTRY_CREDIT == txnItemrow.getIsDebit()) {
                newClosingConvertedQty = lastClosingConvertedQty - sellQty;
                newClosingGrossVal = lastClosingGrossVal - sellGross;
                double tmp1 = newClosingGrossVal / newClosingConvertedQty;
                newClosingRate = IdosUtil.convertStringToDouble(IdosConstants.decimalFormat.format(tmp1));

                closingInventory.setGrossValue(newClosingGrossVal);
                closingInventory.setNoOfExpUnitsConvertedToIncUnits(newClosingConvertedQty);
                closingInventory.setCalcualtedRate(newClosingRate);
                closingInventory.setTotalQuantity(newClosingConvertedQty);
            } else {
                newClosingConvertedQty = lastClosingConvertedQty + sellQty;
                newClosingGrossVal = lastClosingGrossVal + sellGross;
                double tmp1 = newClosingGrossVal / newClosingConvertedQty;
                newClosingRate = IdosUtil.convertStringToDouble(IdosConstants.decimalFormat.format(tmp1));
                closingInventory.setGrossValue(newClosingGrossVal);
                closingInventory.setNoOfExpUnitsConvertedToIncUnits(newClosingConvertedQty);
                closingInventory.setTotalQuantity(newClosingConvertedQty);
                closingInventory.setCalcualtedRate(newClosingRate);
            }
            closingInventory.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL);
            if (txn.getIsBackdatedTransaction() != null && txn.getIsBackdatedTransaction() == 1) {
                closingInventory.setIsBackdatedTransaction(1);
            }
            genericDao.saveOrUpdate(closingInventory, user, em);
            if (txn.getIsBackdatedTransaction() != null && txn.getIsBackdatedTransaction() == 1) { // if backdated JE
                                                                                                   // transaction, then
                                                                                                   // need to adjust
                                                                                                   // rates after this
                                                                                                   // date for all in
                                                                                                   // Trading_inventory
                                                                                                   // table
                adjustTradingInventoryForBackDatedTransactionWAC(newClosingConvertedQty, newClosingRate,
                        newClosingGrossVal, txn.getTransactionDate(), user, em, branch, buySpecific);
            }
        }
    }

    public void adjustTradingInventoryForBackDatedTransactionWAC(Double lastclosingconvertedqty, Double lastclosingrate,
            Double lastclosinggrossval, Date transactionDate, Users user, EntityManager entityManager, Branch branch,
            Specifics buySpecific) throws IDOSException {
        // get all transactions after this backdated transaction is inserted at right
        // place, to adjust their rates in Trading inventory table
        HashMap<Long, Double> salesTranIdAndRate = new HashMap<Long, Double>();
        TradingInventory closingInvRow = null;
        StringBuffer sbr1 = new StringBuffer(
                "select obj from TradingInventory obj where obj.transactionType in (1,2,5,6,7,8,9,10) and (obj.transactionSpecifics.id='"
                        + buySpecific.getId() + "' or obj.buySpecifics.id='" + buySpecific.getId()
                        + "') and obj.organization='" + user.getOrganization().getId() + "' and obj.branch='"
                        + branch.getId() + "' and obj.presentStatus=1 and obj.date > '" + transactionDate
                        + "' order by date,obj.createdAt asc");
        List<TradingInventory> tradingInvList = genericDao.executeSimpleQuery(sbr1.toString(), entityManager);
        if (tradingInvList != null && tradingInvList.size() > 0) {
            for (TradingInventory inv : tradingInvList) {
                if (inv.getTransactionType() == IdosConstants.TRADING_INV_BUY
                        || inv.getTransactionType() == IdosConstants.TRADING_INV_PJE_EXP) {
                    double buyUnits = inv.getNoOfExpUnitsConvertedToIncUnits();
                    double buyGrossVal = inv.getGrossValue();
                    // For this trading inv buy, get its corresponding closing inv row based on
                    // trans id
                    List<TradingInventory> closingInvList = TradingInventory.findTradingInventory(entityManager,
                            user.getOrganization().getId(), branch.getId(), buySpecific.getId(),
                            IdosConstants.TRADING_INV_CLOSING_BAL, inv.getTransactionId());
                    if (closingInvList != null && closingInvList.size() > 0) {
                        if (buyUnits < 0) { // credit JE transaction, buyunits are -ve,so make it positive and then
                                            // common logic of sell transaction of subtraction is used below
                            buyUnits = buyUnits * -1;
                            buyGrossVal = buyGrossVal * -1;
                        }
                        closingInvRow = closingInvList.get(0);
                        lastclosingconvertedqty = lastclosingconvertedqty + buyUnits;
                        lastclosinggrossval = lastclosinggrossval + buyGrossVal;
                    }
                } else if (inv.getTransactionType() == IdosConstants.TRADING_INV_SELL
                        || inv.getTransactionType() == IdosConstants.TRADING_INV_PJE_INC) {
                    double sellUnits = inv.getTotalQuantity();
                    inv.setCalcualtedRate(lastclosingrate); // prev tran closing rate
                    double sellGrossVal = sellUnits * lastclosingrate;
                    inv.setGrossValue(IdosUtil.convertStringToDouble(IdosConstants.decimalFormat.format(sellGrossVal)));
                    salesTranIdAndRate.put(inv.getId(), lastclosingrate);
                    // For this trading inv buy, get its corresponding closing inv row based on
                    // trans id
                    List<TradingInventory> closingInvList = TradingInventory.findTradingInventory(entityManager,
                            user.getOrganization().getId(), branch.getId(), buySpecific.getId(),
                            IdosConstants.TRADING_INV_CLOSING_BAL, inv.getTransactionId());
                    if (closingInvList != null && closingInvList.size() > 0) {
                        if (sellUnits < 0) { // debit JE transaction, sellunits are -ve,so make it positive and then
                                             // common logic of sell transaction of subtraction is used below
                            sellUnits = sellUnits * -1;
                            sellGrossVal = sellGrossVal * -1;
                        }
                        closingInvRow = closingInvList.get(0);
                        lastclosingconvertedqty = lastclosingconvertedqty - sellUnits;
                        lastclosinggrossval = lastclosinggrossval - sellGrossVal;
                    }
                } else if (inv.getTransactionType() == IdosConstants.TRADING_INV_PURCHASE_RET) {
                    double purRetQty = inv.getNoOfExpUnitsConvertedToIncUnits() * -1; // it will be already -ve,so make
                                                                                      // it +ve
                    double purRetGrossVal = purRetQty * lastclosingrate;
                    inv.setCalcualtedRate(lastclosingrate); // prev tran closing rate
                    inv.setGrossValue(
                            IdosUtil.convertStringToDouble(IdosConstants.decimalFormat.format(purRetGrossVal * -1)));
                    // For this trading inv buy, get its corresponding closing inv row based on
                    // trans id
                    List<TradingInventory> closingInvList = TradingInventory.findTradingInventory(entityManager,
                            user.getOrganization().getId(), branch.getId(), buySpecific.getId(),
                            IdosConstants.TRADING_INV_CLOSING_BAL, inv.getTransactionId());
                    if (closingInvList != null && closingInvList.size() > 0) {
                        closingInvRow = closingInvList.get(0);
                        lastclosingconvertedqty = lastclosingconvertedqty - purRetQty;
                        lastclosinggrossval = lastclosinggrossval - purRetGrossVal;
                    }
                } else if (inv.getTransactionType() == IdosConstants.TRADING_INV_SALES_RET) {
                    // apply corresponding previous sales transaction rate for which this return is
                    // processed
                    String tradInvCorrespoindSalesId = inv.getLinkedBuyIds();
                    String[] salestranIds = null;
                    if (tradInvCorrespoindSalesId != null) {
                        salestranIds = tradInvCorrespoindSalesId.split(",");
                    }
                    Double correspondingSalesRate = lastclosingrate;
                    if (salestranIds != null && salestranIds.length > 0) {
                        correspondingSalesRate = salesTranIdAndRate.get(new Long(salestranIds[0])); // sale data with
                                                                                                    // new rate is not
                                                                                                    // commited to Db
                                                                                                    // yet, so
                                                                                                    // maintaining
                                                                                                    // hashmap
                    }
                    if (correspondingSalesRate == null) { // if not in salesTranIdAndRate map, means corresponding sale
                                                          // is done before this backdated transction date
                        TradingInventory tradingInvCorrespondingSalesTran = TradingInventory
                                .findById(new Long(salestranIds[0]));
                        correspondingSalesRate = tradingInvCorrespondingSalesTran.getCalcualtedRate();
                    }
                    double salesRetQty = inv.getTotalQuantity() * -1; // it will be already -ve, so make it +ve
                    double salesRetGrossVal = salesRetQty * correspondingSalesRate; // this will be -ve
                    inv.setCalcualtedRate(correspondingSalesRate); // corresponding sales tran calculated rate
                    inv.setGrossValue(
                            IdosUtil.convertStringToDouble(IdosConstants.decimalFormat.format(salesRetGrossVal * -1))); // since
                                                                                                                        // it
                                                                                                                        // is
                                                                                                                        // return
                                                                                                                        // enter
                                                                                                                        // as
                                                                                                                        // -ve
                                                                                                                        // qty
                                                                                                                        // and
                                                                                                                        // -ve
                                                                                                                        // gross
                    // For this trading inv buy, get its corresponding closing inv row based on
                    // trans id
                    List<TradingInventory> closingInvList = TradingInventory.findTradingInventory(entityManager,
                            user.getOrganization().getId(), branch.getId(), buySpecific.getId(),
                            IdosConstants.TRADING_INV_CLOSING_BAL, inv.getTransactionId());
                    if (closingInvList != null && closingInvList.size() > 0) {
                        closingInvRow = closingInvList.get(0);
                        lastclosingconvertedqty = lastclosingconvertedqty + salesRetQty;
                        lastclosinggrossval = lastclosinggrossval + salesRetGrossVal;
                    }
                }
                genericDao.saveOrUpdate(inv, user, entityManager);
                // for all trade types, modify closing qty,rate,value
                if (closingInvRow != null) {
                    double tmp1 = 0;
                    if (lastclosingconvertedqty != 0) {
                        tmp1 = lastclosinggrossval / lastclosingconvertedqty;
                    }
                    lastclosingrate = IdosUtil.convertStringToDouble(IdosConstants.decimalFormat.format(tmp1));
                    closingInvRow.setNoOfExpUnitsConvertedToIncUnits(lastclosingconvertedqty);
                    closingInvRow.setCalcualtedRate(lastclosingrate);
                    closingInvRow.setGrossValue(
                            IdosUtil.convertStringToDouble(IdosConstants.decimalFormat.format(lastclosinggrossval)));
                    genericDao.saveOrUpdate(closingInvRow, user, entityManager);
                }
            }
        }
    }

    public void adjustTradingInventoryForBackDatedTransactionFIFO(Date transactionDate, Users user,
            EntityManager entityManager, Branch branch, Specifics buySpecific,
            LinkedHashMap<Long, TradingInventory> buyTadesFIFO) {
        try {
            LinkedHashMap<Long, TradingInventory> sellTradeFIFO = new LinkedHashMap<Long, TradingInventory>();
            // get all transactions after this backdated transaction is inserted at right
            // place, to adjust their rates in Trading inventory table

            /*
             * Query query = entityManager.createQuery(BACKDATED_FIFO_TXN_SQL);
             * query.setParameter(1, user.getOrganization().getId()); query.setParameter(2,
             * branch.getId()); query.setParameter(3, buySpecific.getId());
             * query.setParameter(4, buySpecific.getId()); query.setParameter(5,
             * transactionDate);
             * 
             * List<Object[]> itemList = query.getResultList();
             * if(itemList!=null && itemList.size()>0){
             * for(Object[] inventory: itemList) {
             * ObjectNode row = Json.newObject();
             * long transactionId = Long.parseLong(((BigInteger) inventory[0]).toString());
             * //Date date= inventory[1] == null ? 0 : (new Date(inventory[1]));
             * double totalQuantity = inventory[2] == null ? 0 :
             * (Double.parseDouble(inventory[2].toString()));
             * double noOfExpUnitConvertedToIncUnits = inventory[3] == null ? 0 :
             * (Double.parseDouble(inventory[3].toString()));
             * double calculatedRate = inventory[4] == null ? 0 :
             * (Double.parseDouble(inventory[4].toString()));
             * double grossValue = inventory[5] == null ? 0 :
             * (Double.parseDouble(inventory[5].toString()));
             * int txnType = inventory[6] == null ? 0 :
             * (Integer.parseInt(inventory[6].toString()));
             * long id = inventory[7] == null ? 0 :
             * (Long.parseLong(inventory[7].toString()));
             * long transactionSpecificsId = inventory[8] == null ? 0 :
             * (Long.parseLong(inventory[8].toString()));
             * double transactionGross = inventory[9] == null ? 0 :
             * (Double.parseDouble(inventory[9].toString()));
             * 
             * TradingInventory inv = TradingInventory.findById(id);
             */
            StringBuffer sbr1 = new StringBuffer(
                    "select obj from TradingInventory obj where obj.transactionType in (1,2,5,6,7,8,9,10) and (obj.transactionSpecifics.id='"
                            + buySpecific.getId() + "' or obj.buySpecifics.id='" + buySpecific.getId()
                            + "') and obj.organization='" + user.getOrganization().getId()
                            + "' and obj.presentStatus=1 and obj.branch='" + branch.getId() + "' and obj.date > '"
                            + transactionDate + "' order by date asc,obj.createdAt asc");
            List<TradingInventory> tradingInvList = genericDao.executeSimpleQuery(sbr1.toString(), entityManager);
            if (tradingInvList != null && tradingInvList.size() > 0) {
                for (TradingInventory inv : tradingInvList) {
                    if (inv.getTransactionType() == IdosConstants.TRADING_INV_BUY
                            || inv.getTransactionType() == IdosConstants.TRADING_INV_PJE_EXP) {
                        inv.setQuantityMatchedWithSell(0.0); // reset the qty to 0, as its corresponding sale will come
                                                             // next
                        buyTadesFIFO.put(inv.getId(), inv);
                        genericDao.saveOrUpdate(inv, user, entityManager);
                    } else if (inv.getTransactionType() == IdosConstants.TRADING_INV_SELL
                            || inv.getTransactionType() == IdosConstants.TRADING_INV_PJE_INC) {
                        String buyIdsList = inv.getLinkedBuyIds();
                        if (buyIdsList != null) {
                            String[] buyIds = buyIdsList.split(",");
                            if (buyTadesFIFO.containsKey(IdosUtil.convertStringToLong(buyIds[0]))) { // if it doesn't
                                                                                                     // contain in
                                                                                                     // buyTadesFIFO, it
                                                                                                     // means it is
                                                                                                     // adjusted
                                                                                                     // properly BEFORE
                                                                                                     // backdated
                                                                                                     // transaction and
                                                                                                     // NO change
                                                                                                     // required
                                Transaction transaction = Transaction.findById(inv.getTransactionId()); // get
                                                                                                        // corresponding
                                                                                                        // sell
                                                                                                        // transaction
                                Specifics sellSpecific = inv.getTransactionSpecifics();
                                double sellQtyRemaining = inv.getTotalQuantity();
                                if (buyTadesFIFO != null && buyTadesFIFO.size() > 0) {
                                    if (sellQtyRemaining > 0) {
                                        for (Map.Entry<Long, TradingInventory> m : buyTadesFIFO.entrySet()) {
                                            String linkedBuyTrades = "";
                                            TradingInventory buytrade = (TradingInventory) m.getValue();
                                            double buyQty = buytrade.getNoOfExpUnitsConvertedToIncUnits(); // say 5
                                                                                                           // carton=5000,
                                                                                                           // then get
                                                                                                           // 5000
                                            double buyQtyAdjustedWithSellQty = buytrade.getQuantityMatchedWithSell();
                                            double remainingBuyQuantity = 0.0;
                                            double sellQtyMatched = 0.0;
                                            double sellQtyGross = 0.0;
                                            if (buyQtyAdjustedWithSellQty < buyQty) {
                                                remainingBuyQuantity = buyQty - buyQtyAdjustedWithSellQty;
                                                // sellQtyRate = buytrade.getCalcualtedRate();//for sell use same rate
                                                // as buy by FIFO method, but buy rate is based on its unit say
                                                // 5bags=2000, we need rate based on sellunit i.e. 5*200peices=2000 so 1
                                                // piece =20rs
                                                if (sellQtyRemaining <= remainingBuyQuantity) {// once it goes into this
                                                                                               // if, it means for this
                                                                                               // sell transaction all
                                                                                               // buy trades are found
                                                    sellQtyGross = (buytrade.getCalcualtedRate()) * sellQtyRemaining;
                                                    sellQtyMatched = sellQtyRemaining;
                                                    buytrade.setQuantityMatchedWithSell(
                                                            buytrade.getQuantityMatchedWithSell() + sellQtyRemaining);
                                                    sellQtyRemaining = 0; // set buytrade quantity before making it 0
                                                                          // here
                                                } else { // now sell qty say 20 is more than this buy trade qty of 15,
                                                         // so adjust 15 from this buy and then 5 from next buy
                                                    sellQtyGross = buytrade.getCalcualtedRate() * remainingBuyQuantity;
                                                    sellQtyMatched = remainingBuyQuantity;
                                                    linkedBuyTrades = linkedBuyTrades + buytrade.getId() + ",";
                                                    buytrade.setQuantityMatchedWithSell(
                                                            buytrade.getQuantityMatchedWithSell()
                                                                    + remainingBuyQuantity);
                                                    sellQtyRemaining = sellQtyRemaining - remainingBuyQuantity;
                                                }
                                                linkedBuyTrades = linkedBuyTrades + buytrade.getId() + ",";
                                                genericDao.saveOrUpdate(buytrade, user, entityManager);// save this buy
                                                                                                       // TradeInventory
                                                                                                       // with updated
                                                                                                       // quantityMatched
                                                                                                       // so now next
                                                                                                       // time totalqty
                                                                                                       // - qtyMatched
                                                                                                       // is available
                                                                                                       // for next sell
                                                                                                       // transaction

                                                TradingInventory inventory = new TradingInventory();
                                                inventory.setTransactionType(IdosConstants.TRADING_INV_SELL);// buy=1,
                                                                                                             // sell=2,
                                                                                                             // opening=3,
                                                                                                             // closing=4
                                                inventory.setTransactionId(transaction.getId());
                                                inventory.setDate(transaction.getTransactionDate());
                                                inventory.setBranch(transaction.getTransactionBranch());
                                                inventory.setOrganization(
                                                        transaction.getTransactionBranchOrganization());
                                                inventory.setUser(user);
                                                inventory.setTransactionSpecifics(sellSpecific);
                                                inventory.setBuySpecifics(buySpecific); // buy specific for this sell
                                                                                        // trade, so that we can group
                                                                                        // by buySpecific when showing
                                                                                        // consolidated sell trades for
                                                                                        // this buy item
                                                inventory.setTotalQuantity(sellQtyMatched);
                                                inventory.setCalcualtedRate(buytrade.getCalcualtedRate());
                                                inventory.setLinkedBuyIds(linkedBuyTrades);
                                                inventory.setGrossValue(sellQtyGross);
                                                inventory.setTransactionGorss(inv.getTransactionGorss());
                                                genericDao.saveOrUpdate(inventory, user, entityManager); // save
                                                                                                         // multiple
                                                                                                         // sell trades
                                                                                                         // in
                                                                                                         // TradingInventory
                                                                                                         // as it
                                                                                                         // matches with
                                                                                                         // buy for
                                                                                                         // single sell
                                                                                                         // transaction

                                                sellTradeFIFO.put(inventory.getId(), inventory);

                                                // delete prev might be multiple sell inserted as per matched buy FIFO
                                                buyTadesFIFO.remove(buytrade);
                                                Map<String, Object> criterias = new HashMap<String, Object>();
                                                criterias.put("id", inv.getId());
                                                genericDao.deleteByCriteria(TradingInventory.class, criterias,
                                                        entityManager);

                                                if (sellQtyRemaining <= 0) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (inv.getTransactionType() == IdosConstants.TRADING_INV_PURCHASE_RET) {
                        String buyIdsList = inv.getLinkedBuyIds();
                        if (buyIdsList != null) {
                            String[] buyIds = buyIdsList.split(",");
                            if (buyTadesFIFO.containsKey(IdosUtil.convertStringToLong(buyIds[0]))) { // if it doesn't
                                                                                                     // contain in
                                                                                                     // buyTadesFIFO, it
                                                                                                     // means it is
                                                                                                     // adjusted
                                                                                                     // properly BEFORE
                                                                                                     // backdated
                                                                                                     // transaction and
                                                                                                     // NO change
                                                                                                     // required
                                Transaction transaction = Transaction.findById(inv.getTransactionId()); // get
                                                                                                        // corresponding
                                                                                                        // sell
                                                                                                        // transaction
                                Double purRetQty = inv.getTotalQuantity() * -1; // make it +ve
                                double purRetQtyRemaining = purRetQty;

                                if (buyTadesFIFO != null && buyTadesFIFO.size() > 0) {
                                    if (purRetQtyRemaining > 0) { // Manali,here originally it was
                                                                  // while(sellQtyRemaining > 0), but it seems in some
                                                                  // cases while will go to infinite loop and as such
                                                                  // while not required, so changed to if
                                        for (Map.Entry<Long, TradingInventory> m : buyTadesFIFO.entrySet()) {
                                            String linkedBuyTrades = "";
                                            TradingInventory buytrade = (TradingInventory) m.getValue();
                                            double buyQty = buytrade.getNoOfExpUnitsConvertedToIncUnits(); // say 5
                                                                                                           // carton=5000,
                                                                                                           // then get
                                                                                                           // 5000
                                            double buyQtyAdjustedWithSellQty = buytrade.getQuantityMatchedWithSell();
                                            double remainingBuyQuantity = 0.0;
                                            double purRetQtyMatched = 0.0;
                                            double purRetQtyGross = 0.0;
                                            if (buyQtyAdjustedWithSellQty < buyQty) {
                                                remainingBuyQuantity = buyQty - buyQtyAdjustedWithSellQty;
                                                if (purRetQtyRemaining <= remainingBuyQuantity) {// once it goes into
                                                                                                 // this if, it means
                                                                                                 // for this sell
                                                                                                 // transaction all buy
                                                                                                 // trades are found
                                                    purRetQtyGross = (buytrade.getCalcualtedRate())
                                                            * purRetQtyRemaining;// for sell use same rate as buy by
                                                                                 // FIFO method, but buy rate is based
                                                                                 // on its unit say 5bags=2000, we need
                                                                                 // rate based on sellunit i.e.
                                                                                 // 5*200peices=2000 so 1 piece =20rs
                                                    purRetQtyMatched = purRetQtyRemaining;
                                                    buytrade.setQuantityMatchedWithSell(
                                                            buytrade.getQuantityMatchedWithSell() + purRetQtyRemaining);
                                                    purRetQtyRemaining = 0; // set buytrade quantity before making it 0
                                                                            // here
                                                } else { // now sell qty say 20 is more than this buy trade qty of 15,
                                                         // so adjust 15 from this buy and then 5 from next buy
                                                    purRetQtyGross = buytrade.getCalcualtedRate()
                                                            * remainingBuyQuantity;
                                                    purRetQtyMatched = remainingBuyQuantity;
                                                    buytrade.setQuantityMatchedWithSell(
                                                            buytrade.getQuantityMatchedWithSell()
                                                                    + remainingBuyQuantity);
                                                    purRetQtyRemaining = purRetQtyRemaining - remainingBuyQuantity;
                                                }
                                                linkedBuyTrades = linkedBuyTrades + buytrade.getId() + ",";
                                                genericDao.saveOrUpdate(buytrade, user, entityManager);// save this buy
                                                                                                       // TradeInventory
                                                                                                       // with updated
                                                                                                       // quantityMatched
                                                                                                       // so now next
                                                                                                       // time totalqty
                                                                                                       // - qtyMatched
                                                                                                       // is available
                                                                                                       // for next sell
                                                                                                       // transaction

                                                TradingInventory inventory = new TradingInventory();
                                                inventory.setTransactionType(IdosConstants.TRADING_INV_PURCHASE_RET);// buy=1,
                                                                                                                     // sell=2,
                                                                                                                     // opening=3,
                                                                                                                     // closing=4,
                                                                                                                     // purc_ret=5
                                                inventory.setTransactionId(transaction.getId());
                                                inventory.setDate(transaction.getTransactionDate());
                                                inventory.setBranch(transaction.getTransactionBranch());
                                                inventory.setOrganization(
                                                        transaction.getTransactionBranchOrganization());
                                                inventory.setUser(user);
                                                inventory.setTransactionSpecifics(buySpecific);
                                                inventory.setTotalQuantity(purRetQtyMatched * -1);
                                                inventory.setNoOfExpUnitsConvertedToIncUnits(purRetQtyMatched * -1); // need
                                                                                                                     // this
                                                                                                                     // when
                                                                                                                     // showing
                                                                                                                     // consolidated
                                                                                                                     // report
                                                inventory.setCalcualtedRate(buytrade.getCalcualtedRate());
                                                inventory.setLinkedBuyIds(linkedBuyTrades);
                                                inventory.setGrossValue(purRetQtyGross * -1);
                                                genericDao.saveOrUpdate(inventory, user, entityManager); // save
                                                                                                         // multiple
                                                                                                         // sell trades
                                                                                                         // in
                                                                                                         // TradingInventory
                                                                                                         // as it
                                                                                                         // matches with
                                                                                                         // buy for
                                                                                                         // single sell
                                                                                                         // transaction
                                                // delete prev might be multiple sell inserted as per matched buy FIFO
                                                Map<String, Object> criterias = new HashMap<String, Object>();
                                                criterias.put("id", inv.getId());
                                                genericDao.deleteByCriteria(TradingInventory.class, criterias,
                                                        entityManager);

                                                if (purRetQtyRemaining <= 0) {
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (inv.getTransactionType() == IdosConstants.TRADING_INV_SALES_RET) {
                        Double salesRetQty = inv.getTotalQuantity();
                        double salesRetQtyRemaining = salesRetQty;

                        if (sellTradeFIFO != null && sellTradeFIFO.size() > 0) {
                            if (salesRetQtyRemaining > 0) { // Manali,here originally it was while(sellQtyRemaining >
                                                            // 0), but it seems in some cases while will go to infinite
                                                            // loop and as such while not required, so changed to if
                                for (Map.Entry<Long, TradingInventory> m : sellTradeFIFO.entrySet()) {
                                    String linkedBuyTrades = "";
                                    TradingInventory selltrade = (TradingInventory) m.getValue();
                                    double sellQty = selltrade.getTotalQuantity(); // in case of sell, no conversion is
                                                                                   // required, it is actual quantity
                                                                                   // only
                                    double sellQtyAdjustedWithSalesRetQty = selltrade.getQuantityMatchedWithSell();
                                    double remainingSellQuantity = 0.0;
                                    double salesRetQtyMatched = 0.0;
                                    double salesRetQtyGross = 0.0;
                                    if (sellQtyAdjustedWithSalesRetQty < sellQty) {
                                        remainingSellQuantity = sellQty - sellQtyAdjustedWithSalesRetQty;
                                        if (salesRetQtyRemaining <= remainingSellQuantity) {// once it goes into this
                                                                                            // if, it means for this
                                                                                            // sell transaction all buy
                                                                                            // trades are found
                                            salesRetQtyGross = (selltrade.getCalcualtedRate()) * salesRetQtyRemaining;// for
                                                                                                                      // sell
                                                                                                                      // use
                                                                                                                      // same
                                                                                                                      // rate
                                                                                                                      // as
                                                                                                                      // buy
                                                                                                                      // by
                                                                                                                      // FIFO
                                                                                                                      // method,
                                                                                                                      // but
                                                                                                                      // buy
                                                                                                                      // rate
                                                                                                                      // is
                                                                                                                      // based
                                                                                                                      // on
                                                                                                                      // its
                                                                                                                      // unit
                                                                                                                      // say
                                                                                                                      // 5bags=2000,
                                                                                                                      // we
                                                                                                                      // need
                                                                                                                      // rate
                                                                                                                      // based
                                                                                                                      // on
                                                                                                                      // sellunit
                                                                                                                      // i.e.
                                                                                                                      // 5*200peices=2000
                                                                                                                      // so
                                                                                                                      // 1
                                                                                                                      // piece
                                                                                                                      // =20rs
                                            salesRetQtyMatched = salesRetQtyRemaining;
                                            selltrade.setQuantityMatchedWithSell(
                                                    selltrade.getQuantityMatchedWithSell() + salesRetQtyRemaining);
                                            salesRetQtyRemaining = 0; // set buytrade quantity before making it 0 here
                                        } else { // now sell qty say 20 is more than this buy trade qty of 15, so adjust
                                                 // 15 from this buy and then 5 from next buy
                                            salesRetQtyGross = selltrade.getCalcualtedRate() * remainingSellQuantity;
                                            salesRetQtyMatched = remainingSellQuantity;
                                            selltrade.setQuantityMatchedWithSell(
                                                    selltrade.getQuantityMatchedWithSell() + remainingSellQuantity);
                                            salesRetQtyRemaining = salesRetQtyRemaining - remainingSellQuantity;
                                        }
                                        linkedBuyTrades = linkedBuyTrades + selltrade.getId() + ",";
                                        genericDao.saveOrUpdate(selltrade, user, entityManager);// save this buy
                                                                                                // TradeInventory with
                                                                                                // updated
                                                                                                // quantityMatched so
                                                                                                // now next time
                                                                                                // totalqty - qtyMatched
                                                                                                // is available for next
                                                                                                // sell transaction

                                        Transaction transaction = Transaction.findById(inv.getTransactionId()); // get
                                                                                                                // corresponding
                                                                                                                // sell
                                                                                                                // transaction
                                        Specifics sellSpecific = inv.getTransactionSpecifics();

                                        TradingInventory inventory = new TradingInventory();
                                        inventory.setTransactionType(IdosConstants.TRADING_INV_SALES_RET);// buy=1,
                                                                                                          // sell=2,
                                                                                                          // opening=3,
                                                                                                          // closing=4
                                        inventory.setTransactionId(transaction.getId());
                                        inventory.setDate(transaction.getTransactionDate());
                                        inventory.setBranch(transaction.getTransactionBranch());
                                        inventory.setOrganization(transaction.getTransactionBranchOrganization());
                                        inventory.setUser(user);
                                        inventory.setTransactionSpecifics(sellSpecific);
                                        inventory.setBuySpecifics(buySpecific);
                                        inventory.setTotalQuantity(salesRetQtyMatched * -1);
                                        inventory.setCalcualtedRate(selltrade.getCalcualtedRate());
                                        inventory.setLinkedBuyIds(linkedBuyTrades);
                                        inventory.setGrossValue(salesRetQtyGross * -1);
                                        genericDao.saveOrUpdate(inventory, user, entityManager); // save multiple sell
                                                                                                 // trades in
                                                                                                 // TradingInventory as
                                                                                                 // it matches with buy
                                                                                                 // for single sell
                                                                                                 // transaction
                                        // delete prev might be multiple sell inserted as per matched buy FIFO
                                        Map<String, Object> criterias = new HashMap<String, Object>();
                                        criterias.put("id", inv.getId());
                                        genericDao.deleteByCriteria(TradingInventory.class, criterias, entityManager);
                                        if (salesRetQtyRemaining <= 0) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            log.log(Level.SEVERE, "Error", ex);
        }
    }
}
