package service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.*;
import model.*;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.inject.Inject;
import play.db.jpa.JPAApi;
import play.libs.Json;

public class StockServiceImpl implements StockService {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public void insertTradingInventory(Transaction txn, Users user, EntityManager em) throws IDOSException {
		Map<String, Object> criterias = new HashMap<String, Object>();
		criterias.put("transaction.id", txn.getId());
		criterias.put("presentStatus", 1);
		List<TransactionItems> listTransactionItems = genericDAO.findByCriteria(TransactionItems.class, criterias, em);
		for (TransactionItems txnItemrow : listTransactionItems) {
			Specifics txnSpecific = txnItemrow.getTransactionSpecifics();
			if ((txnSpecific != null && txnSpecific.getIsTradingInvenotryItem() != null
					&& txnSpecific.getIsTradingInvenotryItem() == 1)
					|| (txnSpecific.getIsCombinationSales() != null && txnSpecific.getIsCombinationSales() == 1)) {
				long transactionPur = txn.getTransactionPurpose().getId();
				if (transactionPur == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
						|| transactionPur == IdosConstants.BUY_ON_CREDIT_PAY_LATER
						|| transactionPur == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
						|| (IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == transactionPur
								&& txn.getTypeIdentifier() == 2)
						|| (IdosConstants.CREDIT_NOTE_VENDOR == transactionPur && txn.getTypeIdentifier() == 2)) {
					insertTradingInventoryBuyItem(txn, txnItemrow, user, em);
				} else if (transactionPur == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
						|| transactionPur == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
						|| (IdosConstants.DEBIT_NOTE_CUSTOMER == transactionPur && txn.getTypeIdentifier() == 2)
						|| (IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER == transactionPur
								&& txn.getTypeIdentifier() == 1)) {
					insertTradingInventorySellItem(txn, txnItemrow, user, em);
				} else if (txn.getTransactionPurpose().getId() == IdosConstants.PURCHASE_RETURNS) {
					insertTradingInvPurchaseReturn(txn, txnItemrow, user, em);
				} else if (txn.getTransactionPurpose().getId() == IdosConstants.SALES_RETURNS) {
					insertTradingInvSalesReturn(txn, txnItemrow, user, em);
				} else if ((IdosConstants.CREDIT_NOTE_CUSTOMER == transactionPur && txn.getTypeIdentifier() == 2)
						|| IdosConstants.CANCEL_INVOICE == transactionPur) {
					Specifics sellSpecific = txnItemrow.getTransactionSpecifics();
					Specifics buySpecifics = sellSpecific.getLinkIncomeExpenseSpecifics();
					String inventoryMethod = sellSpecific.getTradingInventoryCalcMethod();
					if (IdosConstants.FIFO_METHOD.equalsIgnoreCase(inventoryMethod)) {
						SELL_INVENTORY_DAO.saveInventory4CreditNoteCustomerFifo(txn, txnItemrow, user, em, sellSpecific,
								buySpecifics);
					} else if (IdosConstants.WAC_METHOD.equalsIgnoreCase(inventoryMethod)) {
						SELL_INVENTORY_DAO.saveInventory4CreditNoteCustomerWac(txn, txnItemrow, user, em, sellSpecific,
								buySpecifics);
					}
				} else if (IdosConstants.DEBIT_NOTE_VENDOR == transactionPur && txn.getTypeIdentifier() == 2) {
					Specifics buySpecific = txnItemrow.getTransactionSpecifics();
					String inventoryMethod = buySpecific.getTradingInventoryCalcMethod();
					if (IdosConstants.FIFO_METHOD.equalsIgnoreCase(inventoryMethod)) {
						BUY_INVENTORY_DAO.saveInventory4DebitNoteVendorFifo(txn, txnItemrow, user, em, buySpecific);
					} else if (IdosConstants.WAC_METHOD.equalsIgnoreCase(inventoryMethod)) {
						BUY_INVENTORY_DAO.saveInventory4DebitNoteVendorWac(txn, txnItemrow, user, em, buySpecific);
					}
				} else if (IdosConstants.DEBIT_NOTE_VENDOR == transactionPur && txn.getTypeIdentifier() == 1
						|| IdosConstants.CREDIT_NOTE_VENDOR == transactionPur && txn.getTypeIdentifier() == 1) {
					Specifics buySpecific = txnItemrow.getTransactionSpecifics();
					BUY_INVENTORY_DAO.saveInventory4CreditDebitVendorPrice(txn, txnItemrow, user, em, buySpecific);
				}
			}
		}
	}

	/*
	 * Insert into TRADING_INVENTORY
	 * For Buy: Insert Rate and GROSS value from txn, total_quantity will be in
	 * measure of buy txn
	 * For Sell: Insert Quantity, based on coNversion for buy=sell quantity, need to
	 * do conversion when making calculation.
	 * Find corresponding BUY txn to get RATE, calculate Gross for Sell using
	 * FIFO/weighted avg
	 */
	private void insertTradingInventoryBuyItem(Transaction txn, TransactionItems txnItemrow, Users user,
			EntityManager em) {
		try {
			Branch branch = null;
			if (txn.getTransactionPurpose()
					.getId() == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER) {
				branch = txn.getTransactionToBranch();
			} else {
				branch = txn.getTransactionBranch();
			}
			TradingInventory tradingInv = null;
			List<TradingInventory> tradingInvExistingList = TradingInventory.findTradingInventory(em,
					user.getOrganization().getId(), branch.getId(), txnItemrow.getTransactionSpecifics().getId(),
					IdosConstants.TRADING_INV_BUY, txn.getId());
			if (tradingInvExistingList == null || tradingInvExistingList.isEmpty()) {
				tradingInv = new TradingInventory();
			} else {
				tradingInv = tradingInvExistingList.get(0); // ideally about list should return only one row for given
															// txn Id
			}
			tradingInv.setTransactionId(txn.getId());
			tradingInv.setDate(txn.getTransactionDate());
			tradingInv.setIsBackdatedTransaction(txn.getIsBackdatedTransaction());
			tradingInv.setBranch(branch);
			tradingInv.setOrganization(txn.getTransactionBranchOrganization());
			tradingInv.setUser(user);
			tradingInv.setTransactionSpecifics(txnItemrow.getTransactionSpecifics());
			tradingInv.setTransactionType(IdosConstants.TRADING_INV_BUY); // buy=1, sell=2, opening=3, closing=4,
																			// purchase_ret=5, sales_ret=6
			tradingInv.setTotalQuantity(txnItemrow.getNoOfUnits());
			tradingInv.setGrossValue(txnItemrow.getGrossAmount());
			tradingInv.setTransactionGorss(txnItemrow.getGrossAmount());
			Specifics buySpecific = txnItemrow.getTransactionSpecifics();
			int inventoryType = IdosConstants.FIFO_INVENTORY;
			if (buySpecific.getTradingInventoryCalcMethod().equalsIgnoreCase("WAC")) {
				inventoryType = IdosConstants.WAC_INVENTORY;
			}
			double qtyConvertedToIncomeUnit = txnItemrow.getNoOfUnits()
					* buySpecific.getExpenseToIncomeConverstionRate(); // so buyqty=5bag, but 1bag=100chocolate pieces
																		// when selling chocolate, then put 5oo for qty
			double buyRate = new Double(
					IdosConstants.decimalFormat.format(txnItemrow.getGrossAmount() / qtyConvertedToIncomeUnit)); // store
																													// rate
																													// per
																													// piece
																													// say
																													// 1000/500
																													// =
																													// 20
																													// rs
																													// per
																													// chocolate
			tradingInv.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
			tradingInv.setQuantityMatchedWithSell(0d);
			tradingInv.setCalcualtedRate(buyRate);
			tradingInv.setInventoryType(inventoryType);
			genericDAO.saveOrUpdate(tradingInv, user, em); // save buy trade
			if (inventoryType == IdosConstants.WAC_INVENTORY) {
				TradingInventory tradingInvclosing = null;
				List<TradingInventory> tradingInvExistingCloseList = TradingInventory.findTradingInventory(em,
						user.getOrganization().getId(), branch.getId(), txnItemrow.getTransactionSpecifics().getId(),
						IdosConstants.TRADING_INV_CLOSING_BAL, txn.getId());
				if (tradingInvExistingCloseList == null || tradingInvExistingList.size() <= 0) {
					tradingInvclosing = new TradingInventory();
				} else {
					tradingInvclosing = tradingInvExistingCloseList.get(0); // ideally about list should return only one
																			// row for given txn Id
				}
				// TradingInventory tradingInvclosing = new TradingInventory();
				tradingInvclosing.setTransactionId(txn.getId());
				tradingInvclosing.setDate(txn.getTransactionDate());
				tradingInvclosing.setIsBackdatedTransaction(txn.getIsBackdatedTransaction());
				tradingInvclosing.setBranch(branch);
				tradingInvclosing.setOrganization(txn.getTransactionBranchOrganization());
				tradingInvclosing.setUser(user);
				tradingInvclosing.setTransactionSpecifics(buySpecific);
				tradingInvclosing.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL); // buy=1, sell=2,
																								// opening=3, closing=4
				tradingInvclosing.setQuantityMatchedWithSell(0d);
				// get last closing txn, for buy trade -add qty and add gross to last closing
				// qty and gross and calculate new closing rate=gross/qty
				Date transactionDate = txn.getTransactionDate();
				StringBuffer sbr = new StringBuffer(
						"select obj from TradingInventory obj where obj.transactionType=4 and obj.transactionSpecifics.id='"
								+ buySpecific.getId() + "' and obj.organization='" + user.getOrganization().getId()
								+ "' and obj.branch='" + txn.getTransactionBranch().getId() + "' and ( obj.date <= '"
								+ transactionDate
								+ "' or obj.transactionId is NULL) and obj.presentStatus=1 order by obj.date desc, obj.createdAt desc");
				List<TradingInventory> tradingInvList = genericDAO.executeSimpleQueryWithLimit(sbr.toString(), em, 1);
				if (tradingInvList != null && tradingInvList.size() > 0) {
					TradingInventory inv = (TradingInventory) tradingInvList.get(0);
					double lastclosingqty = 0d;
					if (inv.getTotalQuantity() != null) {
						lastclosingqty = inv.getTotalQuantity();
					}

					double lastclosingconvertedqty = inv.getNoOfExpUnitsConvertedToIncUnits();
					double lastclosinggrossval = inv.getGrossValue();
					double newclosingconvertedqty = lastclosingconvertedqty + qtyConvertedToIncomeUnit;
					double newclosinggrossval = lastclosinggrossval + txnItemrow.getGrossAmount();
					double newclosingrate = newclosinggrossval / newclosingconvertedqty;
					newclosingrate = IdosConstants.decimalFormat
							.parse(IdosConstants.decimalFormat.format(newclosingrate)).doubleValue();
					tradingInvclosing.setTotalQuantity(
							lastclosingqty + IdosUtil.convertStringToDouble(txnItemrow.getNoOfUnits().toString()));
					tradingInvclosing.setGrossValue(newclosinggrossval);
					tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(newclosingconvertedqty);
					tradingInvclosing.setCalcualtedRate(newclosingrate);
				} else {
					tradingInvclosing.setTotalQuantity(tradingInv.getTotalQuantity());
					tradingInvclosing.setGrossValue(tradingInv.getGrossValue());
					tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(qtyConvertedToIncomeUnit);
					tradingInvclosing.setCalcualtedRate(buyRate);
				}
				tradingInvclosing.setInventoryType(inventoryType);
				genericDAO.saveOrUpdate(tradingInvclosing, user, em); // save closing entry for buy trade
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
	}

	private void insertTradingInventorySellItem(Transaction txn, TransactionItems txnItemrow, Users user,
			EntityManager em) {
		try {
			Long sellSpecificId = txnItemrow.getTransactionSpecifics().getId();
			Specifics sellSpecific = txnItemrow.getTransactionSpecifics();
			if (sellSpecific.getIsCombinationSales() != null && sellSpecific.getIsCombinationSales() == 1) {
				StringBuilder newsbquery = new StringBuilder(); // specificId = laptop and combSpecificId = RAM, Monitor
																// etc
				newsbquery.append("select obj from SpecificsCombinationSales obj WHERE obj.specificsId.id = '"
						+ sellSpecificId + "' and obj.organization.id ='" + user.getOrganization().getId()
						+ "' and obj.presentStatus=1");
				List<SpecificsCombinationSales> specificsList = genericDAO.executeSimpleQuery(newsbquery.toString(),
						em);
				for (SpecificsCombinationSales combSpec : specificsList) {
					sellSpecific = combSpec.getCombSpecificsId();
					String tradInvCalcMethod = sellSpecific.getTradingInventoryCalcMethod();
					// get corresponding buy specific
					if (sellSpecific.getIsTradingInvenotryItem() != null
							&& sellSpecific.getIsTradingInvenotryItem() == 1
							&& sellSpecific.getLinkIncomeExpenseSpecifics() != null) {
						Long buySpecificId = sellSpecific.getLinkIncomeExpenseSpecifics().getId();
						Specifics buySpecific = Specifics.findById(buySpecificId); // Expense specific
						double sellQty = txnItemrow.getNoOfUnits() * combSpec.getOpeningBalUnits();
						if (tradInvCalcMethod.equalsIgnoreCase("FIFO")) {
							insertTradingInvSellFIFO(txn, sellQty, user, em, buySpecific, sellSpecific, txnItemrow);
						} else if (tradInvCalcMethod.equalsIgnoreCase("WAC")) {
							insertTradingInvSellWAC(txn, sellQty, user, em, buySpecific, sellSpecific, txnItemrow);
						}
					}
				}
			} else {
				String tradInvCalcMethod = sellSpecific.getTradingInventoryCalcMethod();
				// get corresponding buy specific
				if (sellSpecific.getLinkIncomeExpenseSpecifics() != null) {
					Long buySpecificId = sellSpecific.getLinkIncomeExpenseSpecifics().getId();
					Specifics buySpecific = Specifics.findById(buySpecificId); // Expense specific
					double sellQty = txnItemrow.getNoOfUnits();
					if (tradInvCalcMethod.equalsIgnoreCase("FIFO")) {
						insertTradingInvSellFIFO(txn, sellQty, user, em, buySpecific, sellSpecific, txnItemrow);
					} else if (tradInvCalcMethod.equalsIgnoreCase("WAC")) {
						insertTradingInvSellWAC(txn, sellQty, user, em, buySpecific, sellSpecific, txnItemrow);
					}
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
	}

	private void insertTradingInvSellFIFO(Transaction txn, double sellQty, Users user, EntityManager em,
			Specifics buySpecific, Specifics sellSpecific, TransactionItems txnItemrow) {
		// get all buy trades, so that we can get rate for those to calculate gross for
		// sell transactions
		StringBuffer sbr = new StringBuffer(
				"select obj from TradingInventory obj where (obj.transactionType='1' or obj.transactionType='3' or obj.transactionType='7' or obj.transactionType='9') and obj.organization='"
						+ user.getOrganization().getId() + "' and obj.branch='" + txn.getTransactionBranch().getId()
						+ "' and obj.presentStatus=1 and (obj.transactionSpecifics='" + buySpecific.getId()
						+ "' or obj.buySpecifics='" + buySpecific.getId()
						+ "') and obj.quantityMatchedWithSell < obj.noOfExpUnitsConvertedToIncUnits and obj.date  < '"
						+ txn.getTransactionDate() + "' order by obj.date");
		List<TradingInventory> buyTrades = genericDAO.executeSimpleQuery(sbr.toString(), em);
		// Double sellQty = Double.parseDouble(txnItemrow.getNoOfUnits().toString());
		double sellQtyRemaining = sellQty;
		// double totalGrossValueOfSell=0.0;
		// String linkedBuyTrades = "";
		// HashMap sellMap = new HashMap(); //not using this right now, might need for
		// detail display and for that need to store that too in new table as per Sell
		// Id of tradingInventory. Based on linkbuy we can show linked Buy trades too.
		if (buyTrades != null && buyTrades.size() > 0) {
			if (sellQtyRemaining > 0) { // Manali,here originally it was while(sellQtyRemaining > 0), but it seems in
										// some cases while will go to infinite loop and as such while not required, so
										// changed to if
				for (TradingInventory buytrade : buyTrades) {
					String linkedBuyTrades = "";
					double buyQty = buytrade.getNoOfExpUnitsConvertedToIncUnits(); // say 5 carton=5000, then get 5000
					double buyQtyAdjustedWithSellQty = buytrade.getQuantityMatchedWithSell();
					double remainingBuyQuantity = 0.0;
					double sellQtyMatched = 0.0;
					double sellQtyGross = 0.0;
					if (buyQtyAdjustedWithSellQty < buyQty) {
						remainingBuyQuantity = buyQty - buyQtyAdjustedWithSellQty;
						// sellQtyRate = buytrade.getCalcualtedRate();//for sell use same rate as buy by
						// FIFO method, but buy rate is based on its unit say 5bags=2000, we need rate
						// based on sellunit i.e. 5*200peices=2000 so 1 piece =20rs
						if (sellQtyRemaining <= remainingBuyQuantity) {// once it goes into this if, it means for this
																		// sell txn all buy trades are found
							sellQtyGross = (buytrade.getCalcualtedRate()) * sellQtyRemaining;
							// sellMap.put("sellQty", sellQtyRemaining);
							sellQtyMatched = sellQtyRemaining;
							buytrade.setQuantityMatchedWithSell(
									buytrade.getQuantityMatchedWithSell() + sellQtyRemaining);
							sellQtyRemaining = 0; // set buytrade quantity before making it 0 here
						} else { // now sell qty say 20 is more than this buy trade qty of 15, so adjust 15 from
									// this buy and then 5 from next buy
							sellQtyGross = buytrade.getCalcualtedRate() * remainingBuyQuantity;
							// sellMap.put("sellQty", remainingBuyQuantity);
							sellQtyMatched = remainingBuyQuantity;
							// linkedBuyTrades = linkedBuyTrades + buytrade.getId() + ",";
							buytrade.setQuantityMatchedWithSell(
									buytrade.getQuantityMatchedWithSell() + remainingBuyQuantity);
							sellQtyRemaining = sellQtyRemaining - remainingBuyQuantity;
						}
						// sellMap.put("sellRate", sellQtyRate);
						// sellMap.put("sellGross", sellQtyGross);
						linkedBuyTrades = linkedBuyTrades + buytrade.getId() + ",";

						genericDAO.saveOrUpdate(buytrade, user, em);// save this buy TradeInventory with updated
																	// quantityMatched so now next time totalqty -
																	// qtyMatched is available for next sell txn
						// totalGrossValueOfSell = totalGrossValueOfSell + sellQtyGross;
						TradingInventory tradingInv = new TradingInventory();
						tradingInv.setTransactionType(IdosConstants.TRADING_INV_SELL);// buy=1, sell=2, opening=3,
																						// closing=4
						tradingInv.setTransactionId(txn.getId());
						tradingInv.setDate(txn.getTransactionDate());
						tradingInv.setBranch(txn.getTransactionBranch());
						tradingInv.setOrganization(txn.getTransactionBranchOrganization());
						tradingInv.setUser(user);
						tradingInv.setTransactionSpecifics(sellSpecific);
						tradingInv.setBuySpecifics(buySpecific); // buy specific for this sell trade, so that we can
																	// group by buySpecific when showing consolidated
																	// sell trades for this buy item
						tradingInv.setTotalQuantity(sellQtyMatched);
						tradingInv.setCalcualtedRate(buytrade.getCalcualtedRate());
						tradingInv.setLinkedBuyIds(linkedBuyTrades);
						tradingInv.setGrossValue(sellQtyGross);
						tradingInv.setTransactionGorss(txnItemrow.getGrossAmount());
						tradingInv.setIsBackdatedTransaction(txn.getIsBackdatedTransaction());
						genericDAO.saveOrUpdate(tradingInv, user, em); // save multiple sell trades in TradingInventory
																		// as it matches with buy for single sell txn
						if (sellQtyRemaining <= 0) {
							break;
						}
					}
				}
			}
		}
	}

	private void insertTradingInvSellWAC(Transaction txn, double sellQty, Users user, EntityManager em,
			Specifics buySpecific, Specifics sellSpecific, TransactionItems txnItemrow) throws IDOSException {
		// get last closing txn
		StringBuffer sbr = new StringBuffer("");
		Date transactionDate = txn.getTransactionDate();
		sbr.append("select obj from TradingInventory obj where obj.transactionType=4 and obj.transactionSpecifics.id='"
				+ buySpecific.getId() + "' and obj.organization='" + user.getOrganization().getId()
				+ "' and obj.branch='" + txn.getTransactionBranch().getId() + "' and ( obj.date <= '" + transactionDate
				+ "' or obj.transactionId is NULL) and obj.presentStatus=1 order by obj.date desc, obj.createdAt desc");
		List<TradingInventory> tradingInvList = genericDAO.executeSimpleQueryWithLimit(sbr.toString(), em, 1);
		if (tradingInvList != null && tradingInvList.size() > 0) { // since it is sell we should have atleast one buy /
																	// opening bal txn
			TradingInventory inv = (TradingInventory) tradingInvList.get(0);
			double lastclosingconvertedqty = inv.getNoOfExpUnitsConvertedToIncUnits();
			double lastclosingrate = inv.getCalcualtedRate();
			double lastclosinggrossval = inv.getGrossValue() == null ? 0.0 : inv.getGrossValue();

			// for sell trade - use qty of sell txn * last closing rate = gross for sell
			double sellGross = IdosUtil
					.convertStringToDouble(IdosConstants.decimalFormat.format(sellQty * lastclosingrate));
			TradingInventory tradingInv = new TradingInventory();
			tradingInv.setTransactionId(txn.getId());
			tradingInv.setDate(txn.getTransactionDate());
			tradingInv.setIsBackdatedTransaction(txn.getIsBackdatedTransaction());
			tradingInv.setBranch(txn.getTransactionBranch());
			tradingInv.setOrganization(txn.getTransactionBranchOrganization());
			tradingInv.setUser(user);
			tradingInv.setTransactionSpecifics(sellSpecific);
			tradingInv.setBuySpecifics(buySpecific); // buy specific for this sell trade, so that we can group by
														// buySpecific when showing consolidated sell trades for this
														// buy item
			tradingInv.setTransactionType(IdosConstants.TRADING_INV_SELL); // buy=1, sell=2, opening=3, closing=4
			tradingInv.setTotalQuantity(sellQty);
			tradingInv.setGrossValue(sellGross);
			tradingInv.setTransactionGorss(txnItemrow.getGrossAmount());
			tradingInv.setCalcualtedRate(lastclosingrate);
			tradingInv.setInventoryType(IdosConstants.WAC_INVENTORY);
			genericDAO.saveOrUpdate(tradingInv, user, em); // save sell trade

			// for closing trade - newclosingqty = (closing qty - sell qty), newclosinggorss
			// = closinggross - gross for sell, newclosingrate
			// =newclosinggorss/newclosingqty
			double newclosingconvertedqty = lastclosingconvertedqty - sellQty;
			double newclosinggrossval = lastclosinggrossval - sellGross;
			double newclosingrate = IdosUtil.convertStringToDouble(
					IdosConstants.decimalFormat.format(newclosinggrossval / newclosingconvertedqty));
			TradingInventory tradingInvclosing = new TradingInventory();
			tradingInvclosing.setTransactionId(txn.getId());
			tradingInvclosing.setDate(txn.getTransactionDate());
			tradingInvclosing.setIsBackdatedTransaction(txn.getIsBackdatedTransaction());
			tradingInvclosing.setBranch(txn.getTransactionBranch());
			tradingInvclosing.setOrganization(txn.getTransactionBranchOrganization());
			tradingInvclosing.setUser(user);
			tradingInvclosing.setTransactionSpecifics(buySpecific); // for closing trade always use buyspecific because
																	// we might need to fetch this closing trade for buy
																	// txn and at that time we don't get sellspecific
																	// from buy trade
			tradingInvclosing.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL); // buy=1, sell=2, opening=3,
																							// closing=4
			tradingInvclosing.setGrossValue(newclosinggrossval);
			tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(newclosingconvertedqty);
			tradingInvclosing.setTotalQuantity(newclosingconvertedqty);
			tradingInvclosing.setCalcualtedRate(newclosingrate);
			tradingInvclosing.setInventoryType(IdosConstants.WAC_INVENTORY);
			genericDAO.saveOrUpdate(tradingInvclosing, user, em); // save closing entry for buy trade
		}
	}

	private void insertTradingInvPurchaseReturn(Transaction txn, TransactionItems txnItemrow, Users user,
			EntityManager em) {
		try {
			Long buySpecificId = txnItemrow.getTransactionSpecifics().getId(); // purchase return item is same as
																				// buyspecific id
			Specifics buySpecific = Specifics.findById(buySpecificId); // Expense specific
			String tradInvCalcMethod = buySpecific.getTradingInventoryCalcMethod();
			double qtyConvertedToIncomeUnit = txnItemrow.getNoOfUnits()
					* buySpecific.getExpenseToIncomeConverstionRate(); // so buyqty=5bag, but 1bag=100chocolate pieces
																		// when selling chocolate, then put 5oo for qty
			if (tradInvCalcMethod.equalsIgnoreCase("FIFO")) {
				// get all buy trades, so that we can get rate for those to calculate gross for
				// purchase return transactions
				StringBuffer sbr = new StringBuffer(
						"select obj from TradingInventory obj where (obj.transactionType='1' or obj.transactionType='3') and obj.organization='"
								+ user.getOrganization().getId() + "' and obj.branch='"
								+ txn.getTransactionBranch().getId()
								+ "' and obj.presentStatus=1 and obj.transactionSpecifics='" + buySpecific.getId()
								+ "' and obj.quantityMatchedWithSell < obj.noOfExpUnitsConvertedToIncUnits and obj.date  < '"
								+ txn.getTransactionDate() + "'");
				List<TradingInventory> buyTrades = genericDAO.executeSimpleQuery(sbr.toString(), em);
				Double purRetQty = qtyConvertedToIncomeUnit; // Double.parseDouble(txnItemrow.getNoOfUnits().toString());
				double purRetQtyRemaining = purRetQty;

				if (buyTrades != null && buyTrades.size() > 0) {
					if (purRetQtyRemaining > 0) { // Manali,here originally it was while(sellQtyRemaining > 0), but it
													// seems in some cases while will go to infinite loop and as such
													// while not required, so changed to if
						for (TradingInventory buytrade : buyTrades) {
							String linkedBuyTrades = "";
							double buyQty = buytrade.getNoOfExpUnitsConvertedToIncUnits(); // say 5 carton=5000, then
																							// get 5000
							double buyQtyAdjustedWithSellQty = buytrade.getQuantityMatchedWithSell();
							double remainingBuyQuantity = 0.0;
							double purRetQtyMatched = 0.0;
							double purRetQtyGross = 0.0;
							if (buyQtyAdjustedWithSellQty < buyQty) {
								remainingBuyQuantity = buyQty - buyQtyAdjustedWithSellQty;
								if (purRetQtyRemaining <= remainingBuyQuantity) {// once it goes into this if, it means
																					// for this sell txn all buy trades
																					// are found
									purRetQtyGross = (buytrade.getCalcualtedRate()) * purRetQtyRemaining;// for sell use
																											// same rate
																											// as buy by
																											// FIFO
																											// method,
																											// but buy
																											// rate is
																											// based on
																											// its unit
																											// say
																											// 5bags=2000,
																											// we need
																											// rate
																											// based on
																											// sellunit
																											// i.e.
																											// 5*200peices=2000
																											// so 1
																											// piece
																											// =20rs
									purRetQtyMatched = purRetQtyRemaining;
									buytrade.setQuantityMatchedWithSell(
											buytrade.getQuantityMatchedWithSell() + purRetQtyRemaining);
									purRetQtyRemaining = 0; // set buytrade quantity before making it 0 here
								} else { // now sell qty say 20 is more than this buy trade qty of 15, so adjust 15 from
											// this buy and then 5 from next buy
									purRetQtyGross = buytrade.getCalcualtedRate() * remainingBuyQuantity;
									purRetQtyMatched = remainingBuyQuantity;
									// linkedBuyTrades = linkedBuyTrades + buytrade.getId() + ",";
									buytrade.setQuantityMatchedWithSell(
											buytrade.getQuantityMatchedWithSell() + remainingBuyQuantity);
									purRetQtyRemaining = purRetQtyRemaining - remainingBuyQuantity;
								}
								linkedBuyTrades = linkedBuyTrades + buytrade.getId() + ",";
								genericDAO.saveOrUpdate(buytrade, user, em);// save this buy TradeInventory with updated
																			// quantityMatched so now next time totalqty
																			// - qtyMatched is available for next sell
																			// txn
								// totalGrossValueOfSell = totalGrossValueOfSell + sellQtyGross;
								TradingInventory tradingInv = new TradingInventory();
								tradingInv.setTransactionType(IdosConstants.TRADING_INV_PURCHASE_RET);// buy=1, sell=2,
																										// opening=3,
																										// closing=4,
																										// purc_ret=5
								tradingInv.setTransactionId(txn.getId());
								tradingInv.setDate(txn.getTransactionDate());
								tradingInv.setBranch(txn.getTransactionBranch());
								tradingInv.setOrganization(txn.getTransactionBranchOrganization());
								tradingInv.setUser(user);
								tradingInv.setTransactionSpecifics(buySpecific);
								tradingInv.setTotalQuantity(purRetQtyMatched * -1);
								tradingInv.setNoOfExpUnitsConvertedToIncUnits(purRetQtyMatched * -1); // need this when
																										// showing
																										// consolidated
																										// report
								tradingInv.setCalcualtedRate(buytrade.getCalcualtedRate());
								tradingInv.setLinkedBuyIds(linkedBuyTrades);
								tradingInv.setGrossValue(purRetQtyGross * -1);
								genericDAO.saveOrUpdate(tradingInv, user, em); // save multiple sell trades in
																				// TradingInventory as it matches with
																				// buy for single sell txn
								if (purRetQtyRemaining <= 0) {
									break;
								}
							}
						}
					}
				}
			} else if (tradInvCalcMethod.equalsIgnoreCase("WAC")) {
				// get last closing txn
				Date transactionDate = txn.getTransactionDate();
				StringBuffer sbr = new StringBuffer(
						"select obj from TradingInventory obj where obj.transactionType=4 and obj.transactionSpecifics.id='"
								+ buySpecific.getId() + "' and obj.organization='" + user.getOrganization().getId()
								+ "' and obj.branch='" + txn.getTransactionBranch().getId() + "' and ( obj.date <= '"
								+ transactionDate
								+ "' or obj.transactionId is NULL) and obj.presentStatus=1 order by obj.date desc, obj.createdAt desc");
				List<TradingInventory> tradingInvList = genericDAO.executeSimpleQueryWithLimit(sbr.toString(), em, 1);
				if (tradingInvList != null && tradingInvList.size() > 0) { // since it is sell we should have atleast
																			// one buy / opening bal txn
					TradingInventory inv = (TradingInventory) tradingInvList.get(0);
					double lastclosingconvertedqty = inv.getNoOfExpUnitsConvertedToIncUnits();
					double lastclosingrate = inv.getCalcualtedRate();
					double lastclosinggrossval = inv.getGrossValue();

					// for sell trade - use qty of sell txn * last closing rate = gross for sell
					double purRetQty = qtyConvertedToIncomeUnit;
					double purRetGross = purRetQty * lastclosingrate;
					TradingInventory tradingInv = new TradingInventory();
					tradingInv.setTransactionId(txn.getId());
					tradingInv.setDate(txn.getTransactionDate());
					tradingInv.setBranch(txn.getTransactionBranch());
					tradingInv.setOrganization(txn.getTransactionBranchOrganization());
					tradingInv.setUser(user);
					tradingInv.setTransactionSpecifics(buySpecific);
					// tradingInv.setBuySpecifics(buySpecific); //buy specific for this sell trade,
					// so that we can group by buySpecific when showing consolidated sell trades for
					// this buy item
					tradingInv.setTransactionType(IdosConstants.TRADING_INV_PURCHASE_RET); // buy=1, sell=2, opening=3,
																							// closing=4
					tradingInv.setTotalQuantity(purRetQty * -1);
					tradingInv.setNoOfExpUnitsConvertedToIncUnits(purRetQty * -1);
					tradingInv.setGrossValue(purRetGross * -1);
					tradingInv.setCalcualtedRate(lastclosingrate);
					genericDAO.saveOrUpdate(tradingInv, user, em); // save sell trade

					// for closing trade - newclosingqty = (closing qty - sell qty), newclosinggorss
					// = closinggross - gross for sell, newclosingrate
					// =newclosinggorss/newclosingqty
					double newclosingconvertedqty = lastclosingconvertedqty - purRetQty;
					double newclosinggrossval = lastclosinggrossval - purRetGross;
					double newclosingrate = IdosUtil.convertStringToDouble(
							IdosConstants.decimalFormat.format(newclosinggrossval / newclosingconvertedqty));
					TradingInventory tradingInvclosing = new TradingInventory();
					tradingInvclosing.setTransactionId(txn.getId());
					tradingInvclosing.setDate(txn.getTransactionDate());
					tradingInvclosing.setBranch(txn.getTransactionBranch());
					tradingInvclosing.setOrganization(txn.getTransactionBranchOrganization());
					tradingInvclosing.setUser(user);
					tradingInvclosing.setTransactionSpecifics(buySpecific); // for closing trade always use buyspecific
																			// because we might need to fetch this
																			// closing trade for buy txn and at that
																			// time we don't get sellspecific from buy
																			// trade
					tradingInvclosing.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL); // buy=1, sell=2,
																									// opening=3,
																									// closing=4
					tradingInvclosing.setGrossValue(newclosinggrossval);
					tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(newclosingconvertedqty);
					tradingInvclosing.setCalcualtedRate(newclosingrate);
					genericDAO.saveOrUpdate(tradingInvclosing, user, em); // save closing entry for buy trade
				}
			}
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
	}

	private void insertTradingInvSalesReturn(Transaction txn, TransactionItems txnItemrow, Users user,
			EntityManager em) {
		try {
			Long sellSpecificId = txnItemrow.getTransactionSpecifics().getId(); // sales return item
			Specifics sellSpecific = Specifics.findById(sellSpecificId); // INcome specific
			Long buySpecificId = sellSpecific.getLinkIncomeExpenseSpecifics().getId();
			Specifics buySpecifics = Specifics.findById(buySpecificId); // Expense specific
			String tradInvCalcMethod = sellSpecific.getTradingInventoryCalcMethod();
			if (tradInvCalcMethod.equalsIgnoreCase("FIFO") || tradInvCalcMethod.equalsIgnoreCase("WAC")) {
				// get all sell trades, so that we can get rate for those to calculate gross for
				// sales return transactions
				StringBuffer sbr = new StringBuffer(
						"select obj from TradingInventory obj where obj.transactionType='2' and obj.organization='"
								+ user.getOrganization().getId() + "' and obj.branch='"
								+ txn.getTransactionBranch().getId() + "' and obj.transactionSpecifics='"
								+ sellSpecific.getId()
								+ "' and obj.presentStatus=1 and obj.quantityMatchedWithSell < obj.totalQuantity and obj.date  < '"
								+ txn.getTransactionDate() + "'");
				List<TradingInventory> sellTrades = genericDAO.executeSimpleQuery(sbr.toString(), em);
				Double salesRetQty = IdosUtil.convertStringToDouble(txnItemrow.getNoOfUnits().toString());
				double salesRetQtyRemaining = salesRetQty;

				if (sellTrades != null && sellTrades.size() > 0) {
					if (salesRetQtyRemaining > 0) { // Manali,here originally it was while(sellQtyRemaining > 0), but it
													// seems in some cases while will go to infinite loop and as such
													// while not required, so changed to if
						for (TradingInventory selltrade : sellTrades) {
							String linkedBuyTrades = "";
							double sellQty = selltrade.getTotalQuantity(); // in case of sell, no conversion is
																			// required, it is actual quantity only
							double sellQtyAdjustedWithSalesRetQty = selltrade.getQuantityMatchedWithSell();
							double remainingSellQuantity = 0.0;
							double salesRetQtyMatched = 0.0;
							double salesRetQtyGross = 0.0;
							if (sellQtyAdjustedWithSalesRetQty < sellQty) {
								remainingSellQuantity = sellQty - sellQtyAdjustedWithSalesRetQty;
								if (salesRetQtyRemaining <= remainingSellQuantity) {// once it goes into this if, it
																					// means for this sell txn all buy
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
																												// so 1
																												// piece
																												// =20rs
									salesRetQtyMatched = salesRetQtyRemaining;
									selltrade.setQuantityMatchedWithSell(
											selltrade.getQuantityMatchedWithSell() + salesRetQtyRemaining);
									salesRetQtyRemaining = 0; // set buytrade quantity before making it 0 here
								} else { // now sell qty say 20 is more than this buy trade qty of 15, so adjust 15 from
											// this buy and then 5 from next buy
									salesRetQtyGross = selltrade.getCalcualtedRate() * remainingSellQuantity;
									salesRetQtyMatched = remainingSellQuantity;
									selltrade.setQuantityMatchedWithSell(
											selltrade.getQuantityMatchedWithSell() + remainingSellQuantity);
									salesRetQtyRemaining = salesRetQtyRemaining - remainingSellQuantity;
								}
								linkedBuyTrades = linkedBuyTrades + selltrade.getId() + ",";
								genericDAO.saveOrUpdate(selltrade, user, em);// save this buy TradeInventory with
																				// updated quantityMatched so now next
																				// time totalqty - qtyMatched is
																				// available for next sell txn

								TradingInventory tradingInv = new TradingInventory();
								tradingInv.setTransactionType(IdosConstants.TRADING_INV_SALES_RET);// buy=1, sell=2,
																									// opening=3,
																									// closing=4
								tradingInv.setTransactionId(txn.getId());
								tradingInv.setDate(txn.getTransactionDate());
								tradingInv.setBranch(txn.getTransactionBranch());
								tradingInv.setOrganization(txn.getTransactionBranchOrganization());
								tradingInv.setUser(user);
								tradingInv.setTransactionSpecifics(sellSpecific);
								tradingInv.setBuySpecifics(buySpecifics);
								tradingInv.setTotalQuantity(salesRetQtyMatched * -1);
								tradingInv.setCalcualtedRate(selltrade.getCalcualtedRate());
								tradingInv.setLinkedBuyIds(linkedBuyTrades);
								tradingInv.setGrossValue(salesRetQtyGross * -1);
								genericDAO.saveOrUpdate(tradingInv, user, em); // save multiple sell trades in
																				// TradingInventory as it matches with
																				// buy for single sell txn
								if (tradInvCalcMethod.equalsIgnoreCase("WAC")) {
									// get last closing txn
									StringBuffer sbr1 = new StringBuffer("");
									Date transactionDate = txn.getTransactionDate();
									sbr1.append(
											"select obj from TradingInventory obj where obj.transactionType=4 and obj.transactionSpecifics.id='"
													+ buySpecifics.getId() + "' and obj.organization='"
													+ user.getOrganization().getId() + "' and obj.branch='"
													+ txn.getTransactionBranch().getId() + "' and ( obj.date <= '"
													+ transactionDate
													+ "' or obj.transactionId is NULL) and obj.presentStatus=1  order by obj.date desc,obj.createdAt desc");
									List<TradingInventory> tradingInvList = genericDAO
											.executeSimpleQueryWithLimit(sbr1.toString(), em, 1);
									if (tradingInvList != null && tradingInvList.size() > 0) { // since it is sell we
																								// should have atleast
																								// one buy / opening bal
																								// txn
										TradingInventory inv = (TradingInventory) tradingInvList.get(0);
										double lastclosingconvertedqty = inv.getNoOfExpUnitsConvertedToIncUnits();
										double lastclosingrate = inv.getCalcualtedRate();
										double lastclosinggrossval = inv.getGrossValue();

										// for sell trade - use qty of sell txn * last closing rate = gross for sell
										// double salesRetQty = txnItemrow.getNoOfUnits();
										// double salesRetGross = salesRetQty * lastclosingrate;
										double salesRetQtyClosing = salesRetQtyMatched;
										double salesRetGross = salesRetQtyGross;
										// for closing trade - newclosingqty = (closing qty - sell qty), newclosinggorss
										// = closinggross - gross for sell, newclosingrate
										// =newclosinggorss/newclosingqty
										Specifics buySpecific = sellSpecific.getLinkIncomeExpenseSpecifics();
										double newclosingconvertedqty = lastclosingconvertedqty + salesRetQtyClosing;
										double newclosinggrossval = lastclosinggrossval + salesRetGross;
										double newclosingrate = IdosUtil
												.convertStringToDouble(IdosConstants.decimalFormat
														.format(newclosinggrossval / newclosingconvertedqty));
										TradingInventory tradingInvclosing = new TradingInventory();
										tradingInvclosing.setTransactionId(txn.getId());
										tradingInvclosing.setDate(txn.getTransactionDate());
										tradingInvclosing.setBranch(txn.getTransactionBranch());
										tradingInvclosing.setOrganization(txn.getTransactionBranchOrganization());
										tradingInvclosing.setUser(user);
										tradingInvclosing.setTransactionSpecifics(buySpecific); // for closing trade
																								// always use
																								// buyspecific because
																								// we might need to
																								// fetch this closing
																								// trade for buy txn and
																								// at that time we don't
																								// get sellspecific from
																								// buy trade
										tradingInvclosing.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL); // buy=1,
																														// sell=2,
																														// opening=3,
																														// closing=4
										tradingInvclosing.setGrossValue(newclosinggrossval);
										tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(newclosingconvertedqty);
										tradingInvclosing.setCalcualtedRate(newclosingrate);
										genericDAO.saveOrUpdate(tradingInvclosing, user, em); // save closing entry for
																								// buy trade
									}
								}
								if (salesRetQtyRemaining <= 0) {
									break;
								}
							}
						}
					}
				}
			}
			/*
			 * if (tradInvCalcMethod.equalsIgnoreCase("WAC")) {
			 * //get last closing txn
			 * StringBuffer sbr = new StringBuffer("");
			 * Date transactionDate = txn.getTransactionDate();
			 * sbr.
			 * append("select obj from TradingInventory obj where obj.transactionType=4 and obj.transactionSpecifics.id='"
			 * + buySpecifics.getId() + "' and obj.organization='" +
			 * user.getOrganization().getId() + "' and obj.branch='" +
			 * txn.getTransactionBranch().getId() + "' and ( obj.date <= '" +
			 * transactionDate +
			 * "' or obj.transactionId is NULL)  order by obj.date desc,obj.createdAt desc"
			 * );
			 * List<TradingInventory> tradingInvList =
			 * genericDAO.executeSimpleQueryWithLimit(sbr.toString(), em, 1);
			 * if (tradingInvList != null && tradingInvList.size() > 0) { //since it is sell
			 * we should have atleast one buy / opening bal txn
			 * TradingInventory inv = (TradingInventory) tradingInvList.get(0);
			 * double lastclosingconvertedqty = inv.getNoOfExpUnitsConvertedToIncUnits();
			 * double lastclosingrate = inv.getCalcualtedRate();
			 * double lastclosinggrossval = inv.getGrossValue();
			 * 
			 * //for sell trade - use qty of sell txn * last closing rate = gross for sell
			 * double salesRetQty = txnItemrow.getNoOfUnits();
			 * double salesRetGross = salesRetQty * lastclosingrate;
			 * /*TradingInventory tradingInv = new TradingInventory();
			 * tradingInv.setTransactionId(txn.getId());
			 * tradingInv.setDate(txn.getTransactionDate());
			 * tradingInv.setBranch(txn.getTransactionBranch());
			 * tradingInv.setOrganization(txn.getTransactionBranchOrganization());
			 * tradingInv.setUser(user);
			 * tradingInv.setTransactionSpecifics(sellSpecific);
			 * //tradingInv.setBuySpecifics(buySpecific); //buy specific for this sell
			 * trade, so that we can group by buySpecific when showing consolidated sell
			 * trades for this buy item
			 * tradingInv.setTransactionType(IdosConstants.TRADING_INV_SALES_RET); //buy=1,
			 * sell=2, opening=3, closing=4
			 * tradingInv.setTotalQuantity(salesRetQty*-1);
			 * tradingInv.setGrossValue(salesRetGross*-1);
			 * tradingInv.setCalcualtedRate(lastclosingrate);
			 * genericDAO.saveOrUpdate(tradingInv, user, em); //save sell trade
			 */

			// for closing trade - newclosingqty = (closing qty - sell qty), newclosinggorss
			// = closinggross - gross for sell, newclosingrate
			// =newclosinggorss/newclosingqty
			/*
			 * Specifics buySpecific = sellSpecific.getLinkIncomeExpenseSpecifics();
			 * double newclosingconvertedqty = lastclosingconvertedqty + salesRetQty;
			 * double newclosinggrossval = lastclosinggrossval + salesRetGross;
			 * double newclosingrate =
			 * Double.parseDouble(IdosConstants.decimalFormat.format(newclosinggrossval /
			 * newclosingconvertedqty));
			 * TradingInventory tradingInvclosing = new TradingInventory();
			 * tradingInvclosing.setTransactionId(txn.getId());
			 * tradingInvclosing.setDate(txn.getTransactionDate());
			 * tradingInvclosing.setBranch(txn.getTransactionBranch());
			 * tradingInvclosing.setOrganization(txn.getTransactionBranchOrganization());
			 * tradingInvclosing.setUser(user);
			 * tradingInvclosing.setTransactionSpecifics(buySpecific); //for closing trade
			 * always use buyspecific because we might need to fetch this closing trade for
			 * buy txn and at that time we don't get sellspecific from buy trade
			 * tradingInvclosing.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL);
			 * //buy=1, sell=2, opening=3, closing=4
			 * tradingInvclosing.setGrossValue(newclosinggrossval);
			 * tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(newclosingconvertedqty);
			 * tradingInvclosing.setCalcualtedRate(newclosingrate);
			 * genericDAO.saveOrUpdate(tradingInvclosing, user, em); //save closing entry
			 * for buy trade
			 * }
			 * }
			 */
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
	}

	@Override
	public ObjectNode getItemPresentStock(ObjectNode result, JsonNode json, Users user, EntityManager em,
			EntityTransaction entitytransaction) {
		log.log(Level.FINE, "============ Start");
		try {
			result.put("result", false);
			ArrayNode incomeStockan = result.putArray("incomeStockData");
			String incomeItem = json.findValue("incomeSpecificsId") != null
					? json.findValue("incomeSpecificsId").asText()
					: null;
			if (incomeItem != null && !incomeItem.equals("")) {
				Specifics specf = Specifics.findById(Long.parseLong(incomeItem));
				if (specf.getLinkIncomeExpenseSpecifics() != null) {
					// get purchase stock of linked expensed item with this income item
					Double purchaseStock = getPurchaseStockForThisIncomeLinkedExpenseItem(specf, user, em);
					Double sellStock = getSellStockForThisIncomeItem(specf, user, em);
					Double availableStock = purchaseStock - sellStock;
					result.put("result", true);
					ObjectNode row = Json.newObject();
					row.put("stockAvailable", availableStock);
					incomeStockan.add(row);
				} else {
					result.put("result", false);
				}
			} else {
				result.put("result", false);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	@Override
	public Double buyInventoryStockAvailable(ObjectNode result, long mappedExpenseItem, long branchId, Users user,
			EntityManager em) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "============ Start " + mappedExpenseItem + " " + branchId);
		Double stockAvailable = 0.0;
		try {
			result.put("result", false);
			ArrayNode expenseStockan = result.putArray("expInventoryStockData");

			Specifics buySpecific = Specifics.findById(mappedExpenseItem);
			Branch branch = Branch.findById(branchId);
			stockAvailable = getPurchaseStockForInventoryItem(branch, new Date(), buySpecific, user, em);
			result.put("result", true);
			ObjectNode row = Json.newObject();
			row.put("stockAvailable", stockAvailable);
			expenseStockan.add(row);
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			result.put("result", false);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "============ End " + result);
		return stockAvailable;
	}

	@Override
	public double branchSellStockAvailableCombSales(ObjectNode result, Users user, EntityManager em, JsonNode json) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "============ Start");
		double availableStock = 0.0;
		double availableStockCurr = 0.0;
		try {
			result.put("result", false);
			ArrayNode incomeStockan = result.putArray("incomeStockData");
			long sepecificID = json.findValue("incomeSpecificsId") == null ? 0
					: json.findValue("incomeSpecificsId").asLong();
			long branchID = json.findValue("branchId") == null ? 0 : json.findValue("branchId").asLong();
			long inputQty = json.findValue("inputQty") == null ? 0 : json.findValue("inputQty").asLong();
			String selectedTxnDate = json.findValue("txnDate") == null ? null : json.findValue("txnDate").asText();
			String txnForItemStr = json.findValue("txnForItem") == null ? "" : json.findValue("txnForItem").asText();
			availableStock = getbranchSellStockAvailableCombSales(result, sepecificID, branchID, selectedTxnDate,
					inputQty, txnForItemStr, user, em);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		return availableStock;
	}

	@Override
	public double getbranchSellStockAvailableCombSales(ObjectNode result, long sepecificID, long branchID,
			String selectedTxnDate, long inputQty, String txnForItemStr, Users user, EntityManager em) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "============ Start");
		double availableStock = 0.0;
		double availableStockCurr = 0.0;
		try {
			result.put("result", false);
			ArrayNode incomeStockan = result.putArray("incomeStockData");
			Date txnDate = null;
			if (selectedTxnDate != null) {
				txnDate = IdosConstants.IDOSDF.parse(selectedTxnDate);
			} else {
				txnDate = new Date();
			}
			boolean isBackDated = DateUtil.isBackDate(txnDate);

			Specifics specf = Specifics.findById(sepecificID);
			Branch bnch = Branch.findById(branchID);

			// IDOSWORK-170: Sell on cash/credit consider combination sales items too
			// String txnForItemStr = json.findValue("txnForItem").toString();
			JSONArray arrJSON = new JSONArray("[]");
			if (txnForItemStr != null && !"".equals(txnForItemStr)) {
				arrJSON = new JSONArray(txnForItemStr);
			}
			if (specf != null && specf.getIsCombinationSales() != null && specf.getIsCombinationSales() == 1) {
				result.put("result", true);
				Long coaId = specf.getId();
				StringBuilder newsbquery = new StringBuilder(); // specificId = laptop and combSpecificId = RAM, Monitor
																// etc
				newsbquery.append("select obj from SpecificsCombinationSales obj WHERE obj.specificsId.id = '" + coaId
						+ "' and obj.organization.id ='" + user.getOrganization().getId()
						+ "' and obj.presentStatus=1");
				List<SpecificsCombinationSales> specificsList = genericDAO.executeSimpleQuery(newsbquery.toString(),
						em);
				for (SpecificsCombinationSales combSpec : specificsList) {
					Specifics specificChildItem = combSpec.getCombSpecificsId(); // this is RAM,Monitor etc
					double openBalUnits = inputQty * combSpec.getOpeningBalUnits();
					if (specificChildItem != null && specificChildItem.getLinkIncomeExpenseSpecifics() != null) {
						// get purchase stock of linked expensed item with this income item
						Long buySpecificId = specificChildItem.getLinkIncomeExpenseSpecifics().getId();
						Specifics buySpecific = Specifics.findById(buySpecificId); // Expense specific
						double purchaseStock = getPurchaseStockForInventoryItem(bnch, txnDate, buySpecific, user, em);
						double sellStock = getSellStockForInventoryItem(bnch, txnDate, buySpecific, user, em);
						availableStock = purchaseStock - sellStock;

						if (isBackDated) {
							double purchaseStockNow = getPurchaseStockForInventoryItem(bnch, new Date(), buySpecific,
									user, em);
							double sellStockNow = getSellStockForInventoryItem(bnch, new Date(), buySpecific, user, em);
							double availableStockNow = purchaseStockNow - sellStockNow;
							if (availableStock > availableStockNow) {
								availableStock = availableStockNow;
							}
						}

						// get mutliple sell items mapped to this buy item
						Specifics sellSpecific = null;
						String sellItems = "";
						StringBuilder sbr = new StringBuilder("");
						sbr.append("select obj from Specifics obj where obj.organization='"
								+ user.getOrganization().getId() + "' and obj.linkIncomeExpenseSpecifics='"
								+ buySpecificId + "' and obj.presentStatus=1");
						List<Specifics> sellSpecificsList = genericDAO.executeSimpleQuery(sbr.toString(), em);
						if (sellSpecificsList != null && sellSpecificsList.size() > 0) {
							for (Specifics sellspecificItem : sellSpecificsList) {
								sellSpecific = Specifics.findById(sellspecificItem.getId());
								if (sellSpecific != null) {
									sellItems = sellItems + sellSpecific.getName() + ",";
								}
								// sellItems = sellItems.replaceAll(",$", "");
							}
						}
						// get data previously entered on screen
						double stockAvailableAfterAlreadyEnteredItemsOnScreen = availableStock;
						for (int i = 0; i < arrJSON.length(); i++) {
							JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
							long itemId = rowItemData.getLong("txnItems");
							Specifics prevItemSpecific = Specifics.findById(itemId);
							if (itemId == specificChildItem.getId().longValue()) {
								Long stockForThisItemAlreadyEnteredOnscreen = rowItemData.getLong("txnNoOfUnit");
								stockAvailableAfterAlreadyEnteredItemsOnScreen = availableStock
										- stockForThisItemAlreadyEnteredOnscreen.longValue();
							} else {
								if (prevItemSpecific.getId() != specf.getId()) { // ensure it is not same combination
																					// item in item list, if yes, then
																					// ignore it
									if (prevItemSpecific != null && prevItemSpecific.getIsCombinationSales() != null
											&& prevItemSpecific.getIsCombinationSales() == 1) {
										double combItemsOpenBalUnits = checkIfCurrentItemPartOfPreviouslyEnteredCombSalesItem(
												em, user, prevItemSpecific, specf);
										double combItemsUsed = combItemsOpenBalUnits * inputQty;
										stockAvailableAfterAlreadyEnteredItemsOnScreen = availableStock - combItemsUsed;
									}
								}
							}
						}
						ObjectNode row = Json.newObject();
						row.put("stockAvailable", availableStock);
						sellItems = sellItems.substring(0, sellItems.length() - 1);
						row.put("sellItems", sellItems);
						row.put("combOpeningBalUnits", combSpec.getOpeningBalUnits());
						if (stockAvailableAfterAlreadyEnteredItemsOnScreen < openBalUnits) {
							row.put("isStockAvailable", "false");
						} else {
							row.put("isStockAvailable", "true");
						}
						incomeStockan.add(row);
					}
				}
			} else if (specf != null && specf.getLinkIncomeExpenseSpecifics() != null) {
				// get purchase stock of linked expensed item with this income item
				Long buySpecificId = specf.getLinkIncomeExpenseSpecifics().getId();
				Specifics buySpecific = Specifics.findById(buySpecificId); // Expense specific
				double purchaseStock = getPurchaseStockForInventoryItem(bnch, txnDate, buySpecific, user, em);
				double sellStock = getSellStockForInventoryItem(bnch, txnDate, buySpecific, user, em);
				availableStock = purchaseStock - sellStock;

				if (isBackDated) {
					double purchaseStockNow = getPurchaseStockForInventoryItem(bnch, new Date(), buySpecific, user, em);
					double sellStockNow = getSellStockForInventoryItem(bnch, new Date(), buySpecific, user, em);
					double availableStockNow = purchaseStockNow - sellStockNow;
					if (availableStock > availableStockNow) {
						availableStock = availableStockNow;
					}
				}

				// get mutliple sell items mapped to this buy item
				Specifics sellSpecific = null;
				String sellItems = "";
				StringBuilder sbr = new StringBuilder("select obj from Specifics obj where obj.organization='"
						+ user.getOrganization().getId() + "' and obj.linkIncomeExpenseSpecifics='" + buySpecificId
						+ "' and obj.presentStatus=1");
				List<Specifics> sellSpecificsList = genericDAO.executeSimpleQuery(sbr.toString(), em);
				if (sellSpecificsList != null && sellSpecificsList.size() > 0) {
					for (Specifics sellspecificItem : sellSpecificsList) {
						sellSpecific = Specifics.findById(sellspecificItem.getId());
						if (sellSpecific != null) {
							sellItems = sellItems + sellSpecific.getName() + ",";
						}
					}
				}
				// get data previously entered on screen
				double stockAvailableAfterAlreadyEnteredItemsOnScreen = availableStock;
				for (int i = 0; i < arrJSON.length(); i++) {
					JSONObject rowItemData = new JSONObject(arrJSON.get(i).toString());
					long itemId = rowItemData.getLong("txnItems");
					Specifics prevItemSpecific = Specifics.findById(itemId);
					long specIdCurrent = specf.getId();
					/*
					 * if(itemId==specIdCurrent){//same item in itemslist
					 * Long stockForThisItemAlreadyEnteredOnscreen =
					 * rowItemData.getLong("txnNoOfUnit");
					 * availableStock = availableStock -
					 * stockForThisItemAlreadyEnteredOnscreen.longValue();
					 * }else{
					 */
					if (prevItemSpecific != null && prevItemSpecific.getIsCombinationSales() != null
							&& prevItemSpecific.getIsCombinationSales() == 1) {
						double combItemsOpenBalUnits = checkIfCurrentItemPartOfPreviouslyEnteredCombSalesItem(em, user,
								prevItemSpecific, specf);
						double combItemsUsed = combItemsOpenBalUnits * inputQty;
						stockAvailableAfterAlreadyEnteredItemsOnScreen = availableStock - combItemsUsed;
					}
				}
				result.put("result", true);
				ObjectNode row = Json.newObject();
				row.put("stockAvailable", availableStock);
				sellItems = sellItems.substring(0, sellItems.length() - 1);
				row.put("sellItems", sellItems);
				row.put("combOpeningBalUnits", 0);
				if (stockAvailableAfterAlreadyEnteredItemsOnScreen >= inputQty) {
					row.put("isStockAvailable", "true");
				} else {
					row.put("isStockAvailable", "false");
				}
				incomeStockan.add(row);
			} else {
				result.put("result", false);
			}

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "============ End " + availableStock);
		return availableStock;
	}

	public double checkIfCurrentItemPartOfPreviouslyEnteredCombSalesItem(EntityManager em, Users user,
			Specifics combSalesPrevious, Specifics itemCurrent) {
		double combItemsOpenBalUnits = 0.0;
		Long coaId = combSalesPrevious.getId();
		StringBuilder newsbquery = new StringBuilder(); // specificId = laptop and combSpecificId = RAM, Monitor etc
		newsbquery.append("select obj from SpecificsCombinationSales obj WHERE obj.specificsId.id = '" + coaId
				+ "' and obj.organization.id ='" + user.getOrganization().getId() + "' and obj.presentStatus=1");
		List<SpecificsCombinationSales> specificsList = genericDAO.executeSimpleQuery(newsbquery.toString(), em);
		for (SpecificsCombinationSales combSpec : specificsList) {
			Specifics specificChildItem = combSpec.getCombSpecificsId(); // this is RAM,Monitor etc
			if (specificChildItem.getId() == itemCurrent.getId()) {
				combItemsOpenBalUnits = combSpec.getOpeningBalUnits(); // say in Laptop, we have 4 RAM
			}
		}
		return combItemsOpenBalUnits;
	}

	@Override
	public double getBranchItemPresentStock(ObjectNode result, Users user, EntityManager em, long branchID,
			long sepecificID) {
		log.log(Level.FINE, "============ Start");
		double availableStock = 0.0;
		try {
			result.put("result", false);
			ArrayNode incomeStockan = result.putArray("incomeStockData");
			Specifics specf = Specifics.findById(sepecificID);
			Branch bnch = Branch.findById(branchID);
			// IDOSWORK-170: Sell on cash/credit consider combination sales items too
			if (specf != null && specf.getIsCombinationSales() != null && specf.getIsCombinationSales() == 1) {
				result.put("result", true);
				Long coaId = specf.getId();
				StringBuilder newsbquery = new StringBuilder(); // specificId = laptop and combSpecificId = RAM, Monitor
																// etc
				newsbquery.append("select obj from SpecificsCombinationSales obj WHERE obj.specificsId.id = '" + coaId
						+ "' and obj.organization.id ='" + user.getOrganization().getId()
						+ "' and obj.presentStatus=1");
				List<SpecificsCombinationSales> specificsList = genericDAO.executeSimpleQuery(newsbquery.toString(),
						em);
				for (SpecificsCombinationSales combSpec : specificsList) {
					Specifics specificChildItem = combSpec.getCombSpecificsId(); // this is RAM,Monitor etc
					if (specificChildItem != null && specificChildItem.getLinkIncomeExpenseSpecifics() != null) {
						// get purchase stock of linked expensed item with this income item
						Long buySpecificId = specificChildItem.getLinkIncomeExpenseSpecifics().getId();
						Specifics buySpecific = Specifics.findById(buySpecificId); // Expense specific
						double purchaseStock = getPurchaseStockForInventoryItem(bnch, new Date(), buySpecific, user,
								em);
						double sellStock = getSellStockForInventoryItem(bnch, new Date(), buySpecific, user, em);
						availableStock = purchaseStock - sellStock;

						// get mutliple sell items mapped to this buy item
						Specifics sellSpecific = null;
						String sellItems = "";
						StringBuilder sbr = new StringBuilder("");
						sbr.append("select obj from Specifics obj where obj.organization='"
								+ user.getOrganization().getId() + "' and obj.linkIncomeExpenseSpecifics='"
								+ buySpecificId + "' and obj.presentStatus=1");
						List<Specifics> sellSpecificsList = genericDAO.executeSimpleQuery(sbr.toString(), em);
						if (sellSpecificsList != null && sellSpecificsList.size() > 0) {
							for (Specifics sellspecificItem : sellSpecificsList) {
								sellSpecific = Specifics.findById(sellspecificItem.getId());
								if (sellSpecific != null) {
									sellItems = sellItems + sellSpecific.getName() + ",";
								}
								// sellItems = sellItems.replaceAll(",$", "");
							}
						}
						ObjectNode row = Json.newObject();
						row.put("stockAvailable", availableStock);
						sellItems = sellItems.substring(0, sellItems.length() - 1);
						row.put("sellItems", sellItems);
						row.put("combOpeningBalUnits", combSpec.getOpeningBalUnits());
						incomeStockan.add(row);
					}
				}
			} else if (specf != null && specf.getLinkIncomeExpenseSpecifics() != null) {
				// get purchase stock of linked expensed item with this income item
				Long buySpecificId = specf.getLinkIncomeExpenseSpecifics().getId();
				Specifics buySpecific = Specifics.findById(buySpecificId); // Expense specific
				double purchaseStock = getPurchaseStockForInventoryItem(bnch, new Date(), buySpecific, user, em);
				double sellStock = getSellStockForInventoryItem(bnch, new Date(), buySpecific, user, em);
				availableStock = purchaseStock - sellStock;

				// get mutliple sell items mapped to this buy item
				Specifics sellSpecific = null;
				String sellItems = "";
				StringBuilder sbr = new StringBuilder("");
				sbr.append("select obj from Specifics obj where obj.organization='" + user.getOrganization().getId()
						+ "' and obj.linkIncomeExpenseSpecifics='" + buySpecificId + "' and obj.presentStatus=1");
				List<Specifics> sellSpecificsList = genericDAO.executeSimpleQuery(sbr.toString(), em);
				if (sellSpecificsList != null && sellSpecificsList.size() > 0) {
					for (Specifics sellspecificItem : sellSpecificsList) {
						sellSpecific = Specifics.findById(sellspecificItem.getId());
						if (sellSpecific != null) {
							sellItems = sellItems + sellSpecific.getName() + ",";
						}
						// sellItems = sellItems.replaceAll(",$", "");
					}
				}
				result.put("result", true);
				ObjectNode row = Json.newObject();
				row.put("stockAvailable", availableStock);
				sellItems = sellItems.substring(0, sellItems.length() - 1);
				row.put("sellItems", sellItems);
				row.put("combOpeningBalUnits", 0);
				incomeStockan.add(row);
			} else {
				result.put("result", false);
			}

		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);
		}
		return availableStock;
	}

	public Double getPurchaseStockForThisIncomeLinkedExpenseItem(Specifics specf, Users user, EntityManager em) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "' and obj.transactionSpecifics='"
				+ specf.getLinkIncomeExpenseSpecifics().getId()
				+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or obj.transactionPurpose=25) and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		List<Transaction> expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	public Double getSellStockForThisIncomeItem(Specifics specf, Users user, EntityManager em) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "' and obj.transactionSpecifics='" + specf.getId()
				+ "' AND (obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		List<Transaction> expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	public Double getPurchaseStockForThisIncomeLinkedExpenseItem(Branch bnch, Specifics specf, Users user,
			EntityManager em) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		List<Transaction> expensestock = Collections.emptyList();
		StringBuffer sbr = new StringBuffer("");
		if (bnch != null) {
			sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionBranch='" + bnch.getId()
					+ "' and obj.transactionSpecifics='" + specf.getLinkIncomeExpenseSpecifics().getId()
					+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or obj.transactionPurpose=25) and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		}
		if (bnch == null) {
			sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionSpecifics='"
					+ specf.getLinkIncomeExpenseSpecifics().getId()
					+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or obj.transactionPurpose=25) and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		}
		expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	@Override
	public double getPurchaseStockForInventoryItem(Branch bnch, Date date, Specifics specf, Users user,
			EntityManager em) {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "============ Start branch " + bnch);
		double stock = 0.0;

		// get consolidated buy trades of date < fromdate or opening bal trades with
		// transactionType=3
		String sbr = null;
		String INV_BRANCH_SPECIFCS = "select SUM(obj.noOfExpUnitsConvertedToIncUnits) from TradingInventory obj where obj.organization.id= ?1 and obj.branch.id= ?2 and obj.transactionSpecifics.id= ?3 and obj.date <= ?4 and obj.transactionType in (1,3,5,7,9) and obj.presentStatus=1 group by obj.transactionSpecifics";
		String INV_ORG_SPECIFCS = "select SUM(obj.noOfExpUnitsConvertedToIncUnits) from TradingInventory obj where obj.organization.id= ?1 and obj.transactionSpecifics.id= ?2 and obj.date <= ?3 and obj.transactionType in (1,3,5,7,9) and obj.presentStatus=1 group by obj.transactionSpecifics";
		ArrayList inparams = new ArrayList(4);
		inparams.add(user.getOrganization().getId());
		if (bnch != null) {
			sbr = INV_BRANCH_SPECIFCS;
			inparams.add(bnch.getId());
		} else {
			sbr = INV_ORG_SPECIFCS;
		}
		inparams.add(specf.getId());
		inparams.add(date);

		List<Transaction> expensestock = genericDAO.queryWithParamsName(sbr, em, inparams);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "============ End " + stock);
		return stock;
	}

	@Override
	public double getSellStockForInventoryItem(Branch bnch, Date date, Specifics specf, Users user, EntityManager em)
			throws IDOSException {
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "============ Start");

		String INV_BRANCH_SPECIFCS = "select SUM(obj.totalQuantity) from TradingInventory obj where obj.organization.id= ?1 and obj.branch.id= ?2 and obj.transactionSpecifics.id= ?3 and obj.date <= ?4 and obj.transactionType in (2,6,8,10) and obj.presentStatus=1 group by obj.buySpecifics";
		String INV_ORG_SPECIFCS = "select SUM(obj.totalQuantity) from TradingInventory obj where obj.organization.id= ?1 and obj.transactionSpecifics.id= ?2 and obj.date <= ?3 and obj.transactionType in (2,6,8,10) and obj.presentStatus=1 group by obj.buySpecifics";

		StringBuilder sbr = new StringBuilder(
				"select SUM(obj.totalQuantity) from TradingInventory obj where obj.organization.id= ?1 and obj.presentStatus=1");
		ArrayList inparams = new ArrayList(3);
		inparams.add(user.getOrganization().getId());
		if (bnch != null) {
			sbr.append(" and obj.branch.id= ?2");
			inparams.add(bnch.getId());
			if (specf != null) {
				sbr.append(" and obj.buySpecifics.id= ?3");
				inparams.add(specf.getId());
				sbr.append(" and obj.date <= ?4 and obj.transactionType in (2,6,8,10) group by obj.buySpecifics");
			} else {
				sbr.append(" and obj.date <= ?3 and obj.transactionType in (2,6,8,10) group by obj.buySpecifics");
			}
		} else {
			if (specf != null) {
				sbr.append(" and obj.buySpecifics.id= ?2");
				inparams.add(specf.getId());
				sbr.append(" and obj.date <= ?3 and obj.transactionType in (2,6,8,10) group by obj.buySpecifics");
			} else {
				sbr.append(" and obj.date <= ?2 and obj.transactionType in (2,6,8,10) group by obj.buySpecifics");
			}
		}

		inparams.add(date);

		double stock = 0;
		List<Transaction> expensestock = genericDAO.queryWithParamsName(sbr.toString(), em, inparams);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = IdosUtil.convertStringToDouble(val.toString());
			}
		}
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "============ End " + stock);
		return stock;
	}

	public Double getSellStockForThisIncomeItem(Branch bnch, Specifics specf, Users user, EntityManager em) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		List<Transaction> expensestock = Collections.emptyList();
		StringBuffer sbr = new StringBuffer("");
		if (bnch != null) {
			sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionBranch='" + bnch.getId()
					+ "' and obj.transactionSpecifics='" + specf.getId()
					+ "' AND (obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		}
		if (bnch == null) {
			sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionSpecifics='" + specf.getId()
					+ "' AND (obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		}
		expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	public Double getPurchaseStockTransferredItem(Branch bnch, Specifics specf, Users user, EntityManager em) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "' and obj.inventoryTransferFromBranch='" + bnch.getId()
				+ "' and obj.transactionSpecifics='" + specf.getId()
				+ "' AND (obj.transactionPurpose=25) and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		List<Transaction> expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	public Double getPurchaseStockTransferredInProgressItem(Branch bnch, Specifics specf, Users user,
			EntityManager em) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "' and obj.inventoryTransferFromBranch='" + bnch.getId()
				+ "' and obj.transactionSpecifics='" + specf.getId()
				+ "' AND (obj.transactionPurpose=25) and obj.transactionStatus!='Accounted' and obj.transactionStatus!='Cancelled' and obj.presentStatus=1");
		List<Transaction> expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	@Override
	public ObjectNode getPeriodicInventoryInfo(ObjectNode result, JsonNode json, Users user, EntityManager em,
			EntityTransaction entitytransaction) {
		log.log(Level.FINE, "============ Start");
		try {
			result.put("result", false);
			ArrayNode pian = result.putArray("periodicInventoryData");
			String periodicInventoryForBranch = json.findValue("periodicInventoryForBranch") != null
					? json.findValue("periodicInventoryForBranch").asText()
					: null;
			String fmDate = json.findValue("periodicInventoryFromDate") != null
					? json.findValue("periodicInventoryFromDate").asText()
					: null;
			String tDate = json.findValue("periodicInventoryToDate") != null
					? json.findValue("periodicInventoryToDate").asText()
					: null;
			Branch branch = null;
			String fromDate = null;
			String toDate = null;
			List<PeriodicInventoryComparator> sortedPeriodicInventoryList = Collections.emptyList();
			if ((periodicInventoryForBranch == null && !periodicInventoryForBranch.equals(""))) {
				branch = Branch.findById(Long.parseLong(periodicInventoryForBranch));
			}
			if ((fmDate == null || fmDate.equals("")) && (tDate == null || tDate.equals(""))) {
				fromDate = DateUtil.returnOneMonthBackDate();
				toDate = IdosConstants.mysqldf.format(Calendar.getInstance().getTime());
			} else {
				fromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(fmDate));
				toDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(tDate));
			}
			sortedPeriodicInventoryList = periodicInventoryOnCriteria(branch, fromDate, toDate, user, em,
					entitytransaction);
			if (!sortedPeriodicInventoryList.isEmpty() && sortedPeriodicInventoryList.size() > 0) {
				result.put("result", true);
				for (PeriodicInventoryComparator piTxn : sortedPeriodicInventoryList) {
					ObjectNode row = Json.newObject();
					if (piTxn.getInventoryIncomeExpenseItemName() != null) {
						row.put("inventoryIncomeExpenseItemName", piTxn.getInventoryIncomeExpenseItemName());
					} else {
						row.put("inventoryIncomeExpenseItemName", "");
					}
					if (piTxn.getCreatedDate() != null) {
						row.put("createdDate", IdosConstants.idosdf.format(piTxn.getCreatedDate()));
					} else {
						row.put("createdDate", "");
					}
					if (piTxn.getUnits() != null) {
						row.put("units", piTxn.getUnits());
					} else {
						row.put("units", "");
					}
					if (piTxn.getPrice() != null) {
						row.put("price", IdosConstants.decimalFormat.format(piTxn.getPrice()));
					} else {
						row.put("price", "0.0");
					}
					if (piTxn.getAmount() != null) {
						row.put("amount", IdosConstants.decimalFormat.format(piTxn.getAmount()));
					} else {
						row.put("amount", "0.0");
					}
					if (piTxn.getInventoryStockType() != null) {
						row.put("inventoryStockType", piTxn.getInventoryStockType());
					} else {
						row.put("inventoryStockType", "");
					}
					pian.add(row);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public List<PeriodicInventoryComparator> periodicInventoryOnCriteria(Branch branch, String fromDate, String toDate,
			Users user, EntityManager em, EntityTransaction entitytransaction) {
		log.log(Level.FINE, "============ Start");
		StringBuffer txnsbr = new StringBuffer("");
		List<PeriodicInventoryComparator> piList = Collections.emptyList();
		txnsbr.delete(0, txnsbr.length());
		txnsbr.append(
				"select obj from Specifics obj where obj.particularsId.accountCode=1000000000000000000 and obj.organization='"
						+ user.getOrganization().getId()
						+ "' and obj.linkIncomeExpenseSpecifics IS NOT NULL and obj.presentStatus=1");
		List<Specifics> orgInventoryItem = genericDAO.executeSimpleQuery(txnsbr.toString(), em);
		txnsbr.delete(0, txnsbr.length());
		for (Specifics invItem : orgInventoryItem) {
			if (branch == null) {
				piList = periodicInventoryOnCriteriaOrganization(branch, fromDate, toDate, user, piList, txnsbr,
						invItem, em, entitytransaction);
			} else {
				piList = periodicInventoryOnCriteriaBranchOrganization(branch, fromDate, toDate, user, piList, txnsbr,
						invItem, em, entitytransaction);
			}
		}
		return piList;
	}

	public List<InventoryReport> inventoryReportOnCriteria(Branch branch, String fromDate, String toDate, Users user,
			EntityManager em, EntityTransaction entitytransaction) {
		log.log(Level.FINE, "============ Start");
		StringBuffer txnsbr = new StringBuffer("");
		List<InventoryReport> irList = Collections.emptyList();
		try {
			txnsbr.delete(0, txnsbr.length());
			txnsbr.append(
					"select obj from Specifics obj where obj.particularsId.accountCode=1000000000000000000 and obj.organization='"
							+ user.getOrganization().getId()
							+ "' and obj.linkIncomeExpenseSpecifics IS NOT NULL and obj.presentStatus=1");
			List<Specifics> orgInventoryItem = genericDAO.executeSimpleQuery(txnsbr.toString(), em);
			txnsbr.delete(0, txnsbr.length());
			List<String> dateList = DateUtil.returnListOfDateString(
					IdosConstants.idosdf.format(IdosConstants.mysqldf.parse(fromDate)),
					IdosConstants.idosdf.format(IdosConstants.mysqldf.parse(toDate)));
			for (String presentDate : dateList) {
				for (Specifics invItem : orgInventoryItem) {
					if (branch == null) {
						irList = inventoryReportOnCriteriaOrganization(branch, fromDate, toDate, presentDate, user,
								irList, txnsbr, invItem, em, entitytransaction);
					} else {
						irList = inventoryReportOnCriteriaBranchOrganization(branch, fromDate, toDate, presentDate,
								user, irList, txnsbr, invItem, em, entitytransaction);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return irList;
	}

	public List<InventoryReport> inventoryReportOnCriteriaOrganization(Branch branch, String fromDate, String toDate,
			String presentDate, Users user, List<InventoryReport> irList, StringBuffer txnsbr, Specifics invItem,
			EntityManager em, EntityTransaction entitytransaction) {
		log.log(Level.FINE, "============ Start");
		List<InventoryReport> inventoryrList = new ArrayList<InventoryReport>();
		inventoryrList.addAll(irList);
		try {
			InventoryReport invRep = new InventoryReport();
			Double openingStock = 0d;
			List<Branch> orgBranchList = user.getOrganization().getBranches();
			for (Branch bnch : orgBranchList) {
				Double returnOpeningStock = openingStockItemBranchOnDateCriteria(bnch, invItem, fromDate, toDate,
						presentDate, user, em);
				openingStock += returnOpeningStock;
			}
			Double presentDatePurchase = getPurchaseStockForThisIncomeLinkedExpenseItemOnDateOrg(branch, invItem, user,
					em, presentDate);
			Double presendDateSell = getSellStockForThisIncomeItemOnDateOrg(branch, invItem, user, em, presentDate);
			invRep.setParticulars(invItem.getLinkIncomeExpenseSpecifics().getName());
			invRep.setCreatedDate(
					IdosConstants.idosdf.parse(IdosConstants.idosdf.format(IdosConstants.mysqldf.parse(presentDate))));
			invRep.setOpeningInventoryUnit(openingStock);
			invRep.setPurchaseUnit(presentDatePurchase);
			invRep.setSellUnit(presendDateSell);
			Double closingStock = openingStock + presentDatePurchase - presendDateSell;
			invRep.setClosingInventoryUnit(closingStock);
			inventoryrList.add(invRep);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return inventoryrList;
	}

	public List<InventoryReport> inventoryReportOnCriteriaBranchOrganization(Branch branch, String fromDate,
			String toDate, String presentDate, Users user, List<InventoryReport> irList, StringBuffer txnsbr,
			Specifics invItem, EntityManager em, EntityTransaction entitytransaction) {
		List<InventoryReport> inventoryrList = new ArrayList<InventoryReport>();
		inventoryrList.addAll(irList);
		try {
			InventoryReport invRep = new InventoryReport();
			Double openingStock = 0d;
			Double returnOpeningStock = openingStockItemBranchOnDateCriteria(branch, invItem, fromDate, toDate,
					presentDate, user, em);
			openingStock += returnOpeningStock;
			Double presentDatePurchase = getPurchaseStockForThisIncomeLinkedExpenseItemOnDate(branch, invItem, user, em,
					presentDate);
			Double presendDateSell = getSellStockForThisIncomeItemOnDate(branch, invItem, user, em, presentDate);
			invRep.setParticulars(invItem.getLinkIncomeExpenseSpecifics().getName());
			invRep.setCreatedDate(
					IdosConstants.idosdf.parse(IdosConstants.idosdf.format(IdosConstants.mysqldf.parse(presentDate))));
			invRep.setOpeningInventoryUnit(openingStock);
			invRep.setPurchaseUnit(presentDatePurchase);
			invRep.setSellUnit(presendDateSell);
			Double closingStock = openingStock + presentDatePurchase - presendDateSell;
			invRep.setClosingInventoryUnit(closingStock);
			inventoryrList.add(invRep);
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
		}
		return inventoryrList;
	}

	public Double openingStockItemBranchOnDateCriteria(Branch branch, Specifics specifics, String fromDate,
			String toDate, String presentDate, Users user, EntityManager em) {
		Double openingStock = 0d;
		StringBuffer sbr = new StringBuffer("");
		String oneBackDate = DateUtil.returnOneBackDate(presentDate);
		sbr.append("select obj from Transaction obj where obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "' and obj.transactionBranch='" + branch.getId()
				+ "' and obj.transactionSpecifics='" + specifics.getLinkIncomeExpenseSpecifics().getId()
				+ "' AND (obj.transactionPurpose=26) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate<='"
				+ presentDate + "'");
		List<Transaction> openingInvItemList = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (!openingInvItemList.isEmpty() && openingInvItemList.size() > 0) {
			for (Transaction txn : openingInvItemList) {
				String openingUnitCreatedAt = IdosConstants.mysqldf.format(txn.getTransactionDate());
				openingStock = openingStock + txn.getNoOfUnits();
				sbr.delete(0, sbr.length());
				Double openingStockPurchase = getPurchaseStockForThisIncomeLinkedExpenseItemDateRange(branch, specifics,
						user, em, openingUnitCreatedAt, oneBackDate);
				openingStock = openingStock + openingStockPurchase;
				Double openingStockSell = getSellStockForThisIncomeItemDateRange(branch, specifics, user, em,
						openingUnitCreatedAt, oneBackDate);
				openingStock = openingStock - openingStockSell;
			}
		}
		return openingStock;
	}

	public List<PeriodicInventoryComparator> periodicInventoryOnCriteriaOrganization(Branch branch, String fromDate,
			String toDate, Users user, List<PeriodicInventoryComparator> piList, StringBuffer txnsbr, Specifics invItem,
			EntityManager em, EntityTransaction entitytransaction) {
		List<PeriodicInventoryComparator> periodicInvList = new ArrayList<PeriodicInventoryComparator>();
		periodicInvList.addAll(piList);
		try {
			// when in criteria branch is not selected for periodic Inventory display
			txnsbr.delete(0, txnsbr.length());
			Double openingIncomingInventory = 0d;
			Double openingOutgoingInventory = 0d;
			Double openingInventoryUnit = 0d;
			Double closingInventory = 0d;
			String oneBackDate = DateUtil.returnOneBackDate(fromDate);
			// query for opening unit of periodic inventory for this item
			txnsbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionSpecifics='"
					+ invItem.getLinkIncomeExpenseSpecifics().getId()
					+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or obj.transactionPurpose=25) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate<='"
					+ oneBackDate + "'");
			List<Transaction> txnlist = genericDAO.executeSimpleQuery(txnsbr.toString(), em);
			if (txnlist != null && txnlist.size() > 0 && !txnlist.isEmpty()) {
				if (txnlist.get(0) != null) {
					Object row = txnlist.get(0);
					openingIncomingInventory = Double.parseDouble(row.toString());
				}
			}
			txnlist.clear();
			txnsbr.delete(0, txnsbr.length());
			txnsbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionSpecifics='" + invItem.getId()
					+ "' AND (obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate<='"
					+ oneBackDate + "'");
			txnlist = genericDAO.executeSimpleQuery(txnsbr.toString(), em);
			if (txnlist != null && txnlist.size() > 0 && !txnlist.isEmpty()) {
				if (txnlist.get(0) != null) {
					Object row = txnlist.get(0);
					openingOutgoingInventory = Double.parseDouble(row.toString());
				}
			}
			openingInventoryUnit = openingIncomingInventory - openingOutgoingInventory;
			PeriodicInventoryComparator newObjPI = new PeriodicInventoryComparator();
			newObjPI.setInventoryIncomeExpenseItemName(invItem.getName());
			newObjPI.setCreatedDate(null);
			newObjPI.setInventoryStockType("Opening Inventory");
			newObjPI.setPrice(null);
			newObjPI.setAmount(null);
			newObjPI.setUnits(openingInventoryUnit);
			periodicInvList.add(newObjPI);
			txnsbr.delete(0, txnsbr.length());
			txnlist.clear();
			txnsbr.append("select obj from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and (obj.transactionSpecifics='"
					+ invItem.getLinkIncomeExpenseSpecifics().getId() + "' or obj.transactionSpecifics='"
					+ invItem.getId()
					+ "') AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or transactionPurpose=25 or obj.transactionPurpose=25 or obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate between '"
					+ fromDate + "' and '" + toDate + "' GROUP BY obj.transactionDate,obj.netAmount");
			txnlist = genericDAO.executeSimpleQuery(txnsbr.toString(), em);
			txnsbr.delete(0, txnsbr.length());
			txnsbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and (obj.transactionSpecifics='"
					+ invItem.getLinkIncomeExpenseSpecifics().getId() + "' or obj.transactionSpecifics='"
					+ invItem.getId()
					+ "') AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or obj.transactionPurpose=25 or obj.transactionPurpose=25 or obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate between '"
					+ fromDate + "' and '" + toDate + "' GROUP BY obj.transactionDate,obj.netAmount");
			List<Transaction> txnUnitslist = genericDAO.executeSimpleQuery(txnsbr.toString(), em);
			for (int i = 0; i < txnlist.size(); i++) {
				Transaction txn = txnlist.get(i);
				newObjPI = new PeriodicInventoryComparator();
				newObjPI.setInventoryIncomeExpenseItemName(txn.getTransactionSpecifics().getName());
				newObjPI.setCreatedDate(
						IdosConstants.idosdf.parse(IdosConstants.idosdf.format(txn.getTransactionDate())));
				String inventoryStockType = "";
				if (txn.getTransactionPurpose().getId() == 1 || txn.getTransactionPurpose().getId() == 2) {
					inventoryStockType = "Outgoing Inventory";
				}
				if (txn.getTransactionPurpose().getId() == 3 || txn.getTransactionPurpose().getId() == 4
						|| txn.getTransactionPurpose().getId() == 11) {
					inventoryStockType = "Incoming Inventory";
				}
				newObjPI.setInventoryStockType(inventoryStockType);
				newObjPI.setPrice(txn.getNetAmount());
				Object row = txnUnitslist.get(i);
				Double unit = Double.parseDouble(row.toString());
				newObjPI.setAmount(unit * txn.getNetAmount());
				newObjPI.setUnits(unit);
				periodicInvList.add(newObjPI);
			}
			// query for closing unit of periodic inventory for this item
			Double purchaseStock = getPurchaseStockForThisIncomeLinkedExpenseItem(invItem, user, em);
			Double sellStock = getSellStockForThisIncomeItem(invItem, user, em);
			closingInventory = purchaseStock - sellStock;
			newObjPI = new PeriodicInventoryComparator();
			newObjPI.setInventoryIncomeExpenseItemName(invItem.getName());
			newObjPI.setCreatedDate(null);
			newObjPI.setInventoryStockType("Closing Inventory");
			newObjPI.setPrice(null);
			newObjPI.setAmount(null);
			newObjPI.setUnits(closingInventory);
			periodicInvList.add(newObjPI);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return periodicInvList;
	}

	public List<PeriodicInventoryComparator> periodicInventoryOnCriteriaBranchOrganization(Branch branch,
			String fromDate, String toDate, Users user, List<PeriodicInventoryComparator> piList, StringBuffer txnsbr,
			Specifics invItem, EntityManager em, EntityTransaction entitytransaction) {
		List<PeriodicInventoryComparator> periodicInvList = new ArrayList<PeriodicInventoryComparator>();
		periodicInvList.addAll(piList);
		try {
			// when in criteria branch is selected for periodic Inventory display
			txnsbr.delete(0, txnsbr.length());
			Double openingIncomingInventory = 0d;
			Double openingOutgoingInventory = 0d;
			Double openingInventoryUnit = 0d;
			Double closingInventory = 0d;
			String oneBackDate = DateUtil.returnOneBackDate(fromDate);
			// query for opening unit of periodic inventory for this item
			txnsbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionBranch='" + branch.getId()
					+ "' and obj.transactionSpecifics='" + invItem.getLinkIncomeExpenseSpecifics().getId()
					+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or transactionPurpose=25) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate<='"
					+ oneBackDate + "'");
			List<Transaction> txnlist = genericDAO.executeSimpleQuery(txnsbr.toString(), em);
			if (txnlist != null && txnlist.size() > 0 && !txnlist.isEmpty()) {
				if (txnlist.get(0) != null) {
					Object row = txnlist.get(0);
					openingIncomingInventory = Double.parseDouble(row.toString());
				}
			}
			txnlist.clear();
			txnsbr.delete(0, txnsbr.length());
			txnsbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionBranch='" + branch.getId()
					+ "' and obj.transactionSpecifics='" + invItem.getId()
					+ "' AND (obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate<='"
					+ oneBackDate + "'");
			txnlist = genericDAO.executeSimpleQuery(txnsbr.toString(), em);
			if (txnlist != null && txnlist.size() > 0 && !txnlist.isEmpty()) {
				if (txnlist.get(0) != null) {
					Object row = txnlist.get(0);
					openingOutgoingInventory = Double.parseDouble(row.toString());
				}
			}
			openingInventoryUnit = openingIncomingInventory - openingOutgoingInventory;
			PeriodicInventoryComparator newObjPI = new PeriodicInventoryComparator();
			newObjPI.setInventoryIncomeExpenseItemName(invItem.getName());
			newObjPI.setCreatedDate(null);
			newObjPI.setInventoryStockType("Opening Inventory");
			newObjPI.setPrice(null);
			newObjPI.setAmount(null);
			newObjPI.setUnits(openingInventoryUnit);
			periodicInvList.add(newObjPI);
			txnsbr.delete(0, txnsbr.length());
			txnlist.clear();
			txnsbr.append("select obj from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionBranch='" + branch.getId()
					+ "' and (obj.transactionSpecifics='" + invItem.getLinkIncomeExpenseSpecifics().getId()
					+ "' or obj.transactionSpecifics='" + invItem.getId()
					+ "') AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or transactionPurpose=25 or obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate between '"
					+ fromDate + "' and '" + toDate + "' GROUP BY obj.transactionDate,obj.netAmount");
			txnlist = genericDAO.executeSimpleQuery(txnsbr.toString(), em);
			txnsbr.delete(0, txnsbr.length());
			txnsbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionBranch='" + branch.getId()
					+ "' and (obj.transactionSpecifics='" + invItem.getLinkIncomeExpenseSpecifics().getId()
					+ "' or obj.transactionSpecifics='" + invItem.getId()
					+ "') AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or transactionPurpose=25 or obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionStatus='Accounted' and obj.presentStatus=1 and obj.transactionDate between '"
					+ fromDate + "' and '" + toDate + "' GROUP BY obj.transactionDate,obj.netAmount");
			List<Transaction> txnUnitslist = genericDAO.executeSimpleQuery(txnsbr.toString(), em);
			for (int i = 0; i < txnlist.size(); i++) {
				Transaction txn = txnlist.get(i);
				newObjPI = new PeriodicInventoryComparator();
				newObjPI.setInventoryIncomeExpenseItemName(txn.getTransactionSpecifics().getName());
				newObjPI.setCreatedDate(
						IdosConstants.idosdf.parse(IdosConstants.idosdf.format(txn.getTransactionDate())));
				String inventoryStockType = "";
				if (txn.getTransactionPurpose().getId() == 1 || txn.getTransactionPurpose().getId() == 2) {
					inventoryStockType = "Outgoing Inventory";
				}
				if (txn.getTransactionPurpose().getId() == 3 || txn.getTransactionPurpose().getId() == 4
						|| txn.getTransactionPurpose().getId() == 11) {
					inventoryStockType = "Incoming Inventory";
				}
				newObjPI.setInventoryStockType(inventoryStockType);
				newObjPI.setPrice(txn.getNetAmount());
				Object row = txnUnitslist.get(i);
				Double unit = Double.parseDouble(row.toString());
				newObjPI.setAmount(unit * txn.getNetAmount());
				newObjPI.setUnits(unit);
				periodicInvList.add(newObjPI);
			}
			Double purchaseStock = getPurchaseStockForThisIncomeLinkedExpenseItem(branch, invItem, user, em);
			Double sellStock = getSellStockForThisIncomeItem(branch, invItem, user, em);
			closingInventory = purchaseStock - sellStock;
			newObjPI = new PeriodicInventoryComparator();
			newObjPI.setInventoryIncomeExpenseItemName(invItem.getName());
			newObjPI.setCreatedDate(null);
			newObjPI.setInventoryStockType("Closing Inventory");
			newObjPI.setPrice(null);
			newObjPI.setAmount(null);
			newObjPI.setUnits(closingInventory);
			periodicInvList.add(newObjPI);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return piList;
	}

	@Override
	public String exportPeriodicInventory(ObjectNode result, JsonNode json, Users user, EntityManager em,
			EntityTransaction entitytransaction, String path) throws Exception {
		log.log(Level.FINE, "============ Start");
		ArrayNode exportpian = result.putArray("piFileData");
		String branch = json.findValue("branch") != null ? json.findValue("branch").asText() : null;
		String fromDate = json.findValue("fromDate") != null ? json.findValue("fromDate").asText() : null;
		String piexporttype = json.findValue("piexporttype") != null ? json.findValue("piexporttype").asText() : null;
		String toDate = json.findValue("toDate") != null ? json.findValue("toDate").asText() : null;
		String orgName = user.getOrganization().getName().replaceAll("\\s", "");
		StringBuilder fileName = new StringBuilder(orgName).append("_periodicInventory.").append(piexporttype);
		Branch branchentity = null;
		Date fmDate = null;
		Date tDate = null;
		String fmDt = null;
		String toDt = null;
		List<PeriodicInventoryComparator> sortedPeriodicInventoryList = Collections.emptyList();
		if (branch != null && !branch.equals("")) {
			branchentity = Branch.findById(Long.parseLong(branch));
		}
		if (fromDate != null && !fromDate.equals("")) {
			fmDate = IdosConstants.mysqldf.parse(IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(fromDate)));
			fmDt = IdosConstants.mysqldf.format(fmDate);
		}
		if (toDate != null && !toDate.equals("")) {
			tDate = IdosConstants.mysqldf.parse(IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(toDate)));
			toDt = IdosConstants.mysqldf.format(tDate);
		}

		sortedPeriodicInventoryList = periodicInventoryOnCriteria(branchentity, fmDt, toDt, user, em,
				entitytransaction);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Map<String, Object> params = trialBalanceDao.getParams(branchentity, fromDate, toDate);
		out = dynReportService.generateStaticReport("periodicInventory", sortedPeriodicInventoryList, params,
				piexporttype, null);
		path = path + fileName;
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		FileOutputStream fileOut = new FileOutputStream(path);
		out.writeTo(fileOut);
		fileOut.close();
		return fileName.toString();
	}

	/*
	 * @Override
	 * public ObjectNode transferStockBnchtoBnch(ObjectNode result, JsonNode json,
	 * Users user, EntityManager em, EntityTransaction entitytransaction,
	 * Transaction txn) {
	 * log.log(Level.FINE, "============ Start");
	 * Map<String, Object> criterias = new HashMap<String, Object>();
	 * String invItem = json.findValue("invItem") != null ?
	 * json.findValue("invItem").asText() : null;
	 * String invTransferFromBranch = json.findValue("invTransferFromBranch") !=
	 * null ? json.findValue("invTransferFromBranch").asText() : null;
	 * String txnPurpose = json.findValue("txnPurpose") != null ?
	 * json.findValue("txnPurpose").asText() : null;
	 * String txnPurposeVal = json.findValue("txnPurposeVal") != null ?
	 * json.findValue("txnPurposeVal").asText() : null;
	 * String invFromBranchAvailableStock =
	 * json.findValue("invFromBranchAvailableStock") != null ?
	 * json.findValue("invFromBranchAvailableStock").asText() : null;
	 * String invFromBranchStockTransferInprogress =
	 * json.findValue("invFromBranchStockTransferInprogress") != null ?
	 * json.findValue("invFromBranchStockTransferInprogress").asText() : null;
	 * String numberOfUnitToTransfer = json.findValue("numberOfUnitToTransfer") !=
	 * null ? json.findValue("numberOfUnitToTransfer").asText() : null;
	 * String invTransferToBranch = json.findValue("invTransferToBranch") != null ?
	 * json.findValue("invTransferToBranch").asText() : null;
	 * String invTransferToBranchStock = json.findValue("invTransferToBranchStock")
	 * != null ? json.findValue("invTransferToBranchStock").asText() : null;
	 * String invResultantStock = json.findValue("invResultantStock") != null ?
	 * json.findValue("invResultantStock").asText() : null;
	 * String txnremarks = json.findValue("txnremarks") != null ?
	 * json.findValue("txnremarks").asText() : null;
	 * String supportingdoc = json.findValue("supportingdoc") != null ?
	 * json.findValue("supportingdoc").asText() : null;
	 * Specifics inventoryItem = null;
	 * Branch fromBranch = null;
	 * Branch toBranch = null;
	 * Integer fmBnchAvailableStock = null;
	 * Integer fmBnchStockTransferInProgress = null;
	 * Double noOfUnitToTransfer = null;
	 * Integer toBnchAvailableStock = null;
	 * Integer inventoryResultantStock = null;
	 * String txnRemarks = "";
	 * String txnDocument = "";
	 * TransactionPurpose transactionPurpose = null;
	 * if (txnPurposeVal != null && !txnPurposeVal.equals("")) {
	 * transactionPurpose =
	 * TransactionPurpose.findById(Long.parseLong(txnPurposeVal));
	 * }
	 * if (invItem != null && !invItem.equals("")) {
	 * inventoryItem = Specifics.findById(Long.parseLong(invItem));
	 * }
	 * if (invTransferFromBranch != null && !invTransferFromBranch.equals("")) {
	 * fromBranch = Branch.findById(Long.parseLong(invTransferFromBranch));
	 * }
	 * if (invTransferToBranch != null && !invTransferToBranch.equals("")) {
	 * toBranch = Branch.findById(Long.parseLong(invTransferToBranch));
	 * }
	 * if (invFromBranchAvailableStock != null &&
	 * !invFromBranchAvailableStock.equals("")) {
	 * fmBnchAvailableStock = Integer.parseInt(invFromBranchAvailableStock);
	 * }
	 * if (invFromBranchStockTransferInprogress != null &&
	 * !invFromBranchStockTransferInprogress.equals("")) {
	 * fmBnchStockTransferInProgress =
	 * Integer.parseInt(invFromBranchStockTransferInprogress);
	 * }
	 * if (numberOfUnitToTransfer != null && !numberOfUnitToTransfer.equals("")) {
	 * noOfUnitToTransfer = Double.parseDouble(numberOfUnitToTransfer);
	 * }
	 * if (invTransferToBranchStock != null && !invTransferToBranchStock.equals(""))
	 * {
	 * toBnchAvailableStock = Integer.parseInt(invTransferToBranchStock);
	 * }
	 * if (invResultantStock != null && !invResultantStock.equals("")) {
	 * inventoryResultantStock = Integer.parseInt(invResultantStock);
	 * }
	 * if (transactionPurpose != null) {
	 * txn.setTransactionPurpose(transactionPurpose);
	 * }
	 * if (inventoryItem != null) {
	 * txn.setTransactionSpecifics(inventoryItem);
	 * txn.setTransactionParticulars(inventoryItem.getParticularsId());
	 * }
	 * if (toBranch != null) {
	 * txn.setTransactionBranch(toBranch);
	 * txn.setTransactionBranchOrganization(toBranch.getOrganization());
	 * }
	 * if (fromBranch != null) {
	 * txn.setInventoryTransferFromBranch(fromBranch);
	 * txn.setInventoryTransferFromBranchOrganization(fromBranch.getOrganization());
	 * }
	 * txn.setAvailableStockFromBranch(fmBnchAvailableStock);
	 * txn.setStockTransferInProgress(fmBnchStockTransferInProgress);
	 * txn.setAvailableStockToBranch(toBnchAvailableStock);
	 * txn.setNoOfUnits(noOfUnitToTransfer);
	 * txn.setResultantStock(inventoryResultantStock);
	 * txn.setTransactionDate(Calendar.getInstance().getTime());
	 * if (!txnremarks.equals("") && txnremarks != null) {
	 * txnRemarks = user.getEmail() + "#" + txnremarks;
	 * txn.setRemarks(txnRemarks);
	 * }
	 * if (!supportingdoc.equals("") && supportingdoc != null) {
	 * String suppdocarr[] = supportingdoc.split(",");
	 * for (int i = 0; i < suppdocarr.length; i++) {
	 * if (txnDocument.equals("")) {
	 * txnDocument += user.getEmail() + "#" + suppdocarr[i];
	 * } else {
	 * txnDocument += "," + user.getEmail() + "#" + suppdocarr[i];
	 * }
	 * }
	 * txn.setSupportingDocs(txnDocument);
	 * }
	 * txn.setTransactionStatus("Require Approval");
	 * txn.setNetAmountResultDescription("Inventory Item " + inventoryItem.getName()
	 * + " stock transfer of quantity " + noOfUnitToTransfer + " from branch" +
	 * fromBranch.getName() + " to " + toBranch.getName() + "");
	 * //list of additional users all approver role users of thet organization
	 * criterias.clear();
	 * criterias.put("role.name", "APPROVER");
	 * criterias.put("organization.id", user.getOrganization().getId());
	 * List<UsersRoles> approverRole = genericDAO.findByCriteria(UsersRoles.class,
	 * criterias, em);
	 * String approverEmails = "";
	 * String additionalApprovarUsers = "";
	 * String selectedAdditionalApproval = "";
	 * 
	 * Boolean approver=null;
	 * for (UsersRoles usrRoles : approverRole) {
	 * approver=false;
	 * additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
	 * criterias.clear();
	 * criterias.put("user.id", usrRoles.getUser().getId());
	 * criterias.put("userRights.id", 2L);
	 * criterias.put("branch.id", toBranch.getId());
	 * 
	 * UserRightInBranch userHasRightInBranch =
	 * genericDAO.getByCriteria(UserRightInBranch.class, criterias, em);
	 * if (userHasRightInBranch != null) {
	 * 
	 * criterias.clear();
	 * criterias.put("user.id", usrRoles.getUser().getId());
	 * criterias.put("userRights.id", 2L);
	 * criterias.put("specifics.id",inventoryItem.getId());
	 * UserRightSpecifics userHasRightInCOA =
	 * genericDAO.getByCriteria(UserRightSpecifics.class, criterias, em);
	 * if (userHasRightInCOA!= null) {
	 * approver= true;
	 * }
	 * else{
	 * approver=false;
	 * }
	 * }
	 * if(approver){
	 * approverEmails += usrRoles.getUser().getEmail() + ",";
	 * }
	 * 
	 * 
	 * }
	 * txn.setApproverEmails(approverEmails);
	 * txn.setAdditionalApproverEmails(additionalApprovarUsers);
	 * //list of approver user
	 * String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
	 * txn.setTransactionRefNumber(transactionNumber);
	 * genericDAO.saveOrUpdate(txn, user, em);
	 * entitytransaction.commit();
	 * return result;
	 * }
	 */

	@Override
	@Deprecated
	public Transaction openingStockInventoryItem(ObjectNode result, JsonNode json, Users user, EntityManager em,
			EntityTransaction entitytransaction) throws IDOSException {
		Transaction txn = new Transaction();
		log.log(Level.FINE, "============ Start");
		String invItem = json.findValue("invItem") != null ? json.findValue("invItem").asText() : null;
		String invBranchOpeningBal = json.findValue("invBranchOpeningBal") != null
				? json.findValue("invBranchOpeningBal").asText()
				: null;
		String invAvailableStock = json.findValue("invAvailableStock") != null
				? json.findValue("invAvailableStock").asText()
				: null;
		String invNoOfUnitOfOpeningStock = json.findValue("invNoOfUnitOfOpeningStock") != null
				? json.findValue("invNoOfUnitOfOpeningStock").asText()
				: null;
		String invPurpose = json.findValue("invPurpose") != null ? json.findValue("invPurpose").asText() : null;
		String txnremarks = json.findValue("txnremarks") != null ? json.findValue("txnremarks").asText() : null;
		String supportingdoc = json.findValue("supportingdoc") != null ? json.findValue("supportingdoc").asText()
				: null;
		// String txnPurpose = json.findValue("txnPurpose") != null ?
		// json.findValue("txnPurpose").asText() : null;
		String txnPurposeVal = json.findValue("txnPurposeVal") != null ? json.findValue("txnPurposeVal").asText()
				: null;
		TransactionPurpose transactionPurpose = null;
		String txnRemarks = "";
		String txnDocument = "";
		Branch txnBnch = null;
		Specifics inventoryItem = null;
		Integer invAvailStock = null;
		Double invOpeningStock = null;
		String openingInvPurpose = null;
		Map<String, Object> criterias = new HashMap<String, Object>();
		if (txnPurposeVal != null && !txnPurposeVal.equals("")) {
			transactionPurpose = TransactionPurpose.findById(IdosUtil.convertStringToLong(txnPurposeVal));
		}
		if (invItem != null && !invItem.equals("")) {
			inventoryItem = Specifics.findById(IdosUtil.convertStringToLong(invItem));
		}
		if (invBranchOpeningBal != null && !invBranchOpeningBal.equals("")) {
			txnBnch = Branch.findById(IdosUtil.convertStringToLong(invBranchOpeningBal));
		}
		if (invAvailableStock != null && !invAvailableStock.equals("")) {
			invAvailStock = IdosUtil.convertStringToInt(invAvailableStock);
		}
		if (invNoOfUnitOfOpeningStock != null && !invNoOfUnitOfOpeningStock.equals("")) {
			invOpeningStock = IdosUtil.convertStringToDouble(invNoOfUnitOfOpeningStock);
		}
		if (invPurpose != null && !invPurpose.equals("")) {
			openingInvPurpose = invPurpose;
		}
		txn.setTransactionBranch(txnBnch);
		txn.setTransactionBranchOrganization(txnBnch.getOrganization());
		txn.setTransactionSpecifics(inventoryItem);
		txn.setTransactionParticulars(inventoryItem.getParticularsId());
		txn.setTransactionPurpose(transactionPurpose);
		txn.setNoOfUnits(invOpeningStock);
		txn.setAvailableStockFromBranch(invAvailStock);
		txn.setTransactionDate(Calendar.getInstance().getTime());
		txn.setNetAmountResultDescription("Opening Stock For Item " + inventoryItem.getName() + " For Branch "
				+ txnBnch.getName() + " is " + invOpeningStock + " ");
		if (!txnremarks.equals("") && txnremarks != null) {
			txnRemarks = user.getEmail() + "#" + txnremarks;
			txn.setRemarks(txnRemarks);
		}
		txn.setSupportingDocs(transactionDao.getAndDeleteSupportingDocument(txn.getSupportingDocs(), user.getEmail(),
				supportingdoc, user, em));
		txn.setTransactionStatus("Require Approval");
		// list of additional users all approver role users of thet organization
		criterias.clear();
		criterias.put("role.name", "APPROVER");
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		List<UsersRoles> approverRole = genericDAO.findByCriteria(UsersRoles.class, criterias, em);
		String approverEmails = "";
		String additionalApprovarUsers = "";
		String selectedAdditionalApproval = "";
		for (UsersRoles usrRoles : approverRole) {
			additionalApprovarUsers += usrRoles.getUser().getEmail() + ",";
			criterias.clear();
			criterias.put("user.id", usrRoles.getUser().getId());
			criterias.put("userRights.id", 2L);
			criterias.put("branch.id", txnBnch.getId());
			criterias.put("presentStatus", 1);
			UserRightInBranch userHasRightInBranch = genericDAO.getByCriteria(UserRightInBranch.class, criterias, em);
			if (userHasRightInBranch != null) {
				// check for right in chart of accounts
				criterias.clear();
				criterias.put("user.id", usrRoles.getUser().getId());
				criterias.put("userRights.id", 2L);
				criterias.put("specifics.id", inventoryItem.getId());
				criterias.put("presentStatus", 1);
				UserRightSpecifics userHasRightInCOA = genericDAO.getByCriteria(UserRightSpecifics.class, criterias,
						em);
				if (userHasRightInCOA != null) {
					boolean userAmtLimit = true;
					if (userAmtLimit == true) {
						approverEmails += usrRoles.getUser().getEmail() + ",";
					}
				}
			}
		}
		txn.setApproverEmails(approverEmails);
		txn.setAdditionalApproverEmails(additionalApprovarUsers);
		// list of approver user
		String transactionNumber = CodeHelper.getForeverUniqueID("TXN", null);
		txn.setTransactionRefNumber(transactionNumber);
		genericDAO.saveOrUpdate(txn, user, em);
		entitytransaction.commit();
		return txn;
	}

	@Override
	public Double openingStockInProgress(Branch bnch, Specifics specf, Users user, EntityManager em) {

		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select SUM(obj.noOfUnits) from Transaction obj where obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "' and obj.transactionBranch='" + bnch.getId()
				+ "' and obj.transactionSpecifics='" + specf.getId()
				+ "' AND (obj.transactionPurpose=26) and obj.transactionStatus!='Accounted' and obj.transactionStatus!='Cancelled' and obj.presentStatus=1");
		List<Transaction> expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	@Override
	public Double getPurchaseOpeningStock(Branch bnch, Specifics specf, Users user, EntityManager em) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		List<Transaction> expensestock = Collections.emptyList();
		StringBuffer sbr = new StringBuffer("");
		if (bnch != null) {
			sbr.append("select SUM(obj.noOfUnits) from Transaction obj where obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionBranch='" + bnch.getId()
					+ "' and obj.transactionSpecifics='" + specf.getId()
					+ "' AND (obj.transactionPurpose=26) and obj.transactionStatus='Accounted' and obj.presentStatus=1");
			expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		}
		if (bnch == null) {
			sbr.append("select SUM(obj.noOfUnits) from Transaction obj where obj.transactionBranchOrganization='"
					+ user.getOrganization().getId() + "' and obj.transactionSpecifics='" + specf.getId()
					+ "' AND (obj.transactionPurpose=26) and obj.transactionStatus='Accounted' and obj.presentStatus=1");
			expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		}
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	@Override
	public ObjectNode getReportInventoryInfo(ObjectNode result, JsonNode json, Users user, EntityManager em,
			EntityTransaction entitytransaction) {
		log.log(Level.FINE, "============ Start");
		try {
			result.put("result", false);
			ArrayNode iran = result.putArray("inventoryReportData");
			String reportInventoryForBranch = json.findValue("reportInventoryForBranch") != null
					? json.findValue("reportInventoryForBranch").asText()
					: null;
			String fmDate = json.findValue("reportInventoryFromDate") != null
					? json.findValue("reportInventoryFromDate").asText()
					: null;
			String tDate = json.findValue("reportInventoryToDate") != null
					? json.findValue("reportInventoryToDate").asText()
					: null;
			Branch branch = null;
			String fromDate = null;
			String toDate = null;
			List<InventoryReport> sortedInventoryReportList = Collections.emptyList();
			if ((reportInventoryForBranch != null && !reportInventoryForBranch.equals(""))) {
				branch = Branch.findById(Long.parseLong(reportInventoryForBranch));
			}
			if ((fmDate == null || fmDate.equals("")) && (tDate == null || tDate.equals(""))) {
				fromDate = DateUtil.returnOneMonthBackDate();
				toDate = IdosConstants.mysqldf.format(Calendar.getInstance().getTime());
			} else {
				fromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(fmDate));
				toDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(tDate));
			}
			sortedInventoryReportList = inventoryReportOnCriteria(branch, fromDate, toDate, user, em,
					entitytransaction);
			if (!sortedInventoryReportList.isEmpty() && sortedInventoryReportList.size() > 0) {
				result.put("result", true);
				for (InventoryReport invReport : sortedInventoryReportList) {
					ObjectNode row = Json.newObject();
					if (invReport.getParticulars() != null) {
						row.put("particulars", invReport.getParticulars());
					} else {
						row.put("particulars", "");
					}
					if (invReport.getCreatedDate() != null) {
						row.put("createdDate", IdosConstants.idosdf.format(invReport.getCreatedDate()));
					} else {
						row.put("createdDate", "");
					}
					if (invReport.getOpeningInventoryUnit() != null) {
						row.put("openingUnits", invReport.getOpeningInventoryUnit());
					} else {
						row.put("openingUnits", "");
					}
					if (invReport.getPurchaseUnit() != null) {
						row.put("purchaseUnit", invReport.getPurchaseUnit());
					} else {
						row.put("purchaseUnit", "");
					}
					if (invReport.getSellUnit() != null) {
						row.put("sellUnit", invReport.getSellUnit());
					} else {
						row.put("sellUnit", "");
					}
					if (invReport.getClosingInventoryUnit() != null) {
						row.put("closingUnits", invReport.getClosingInventoryUnit());
					} else {
						row.put("closingUnits", "");
					}
					iran.add(row);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	@Override
	public Double getPurchaseStockForThisIncomeLinkedExpenseItemDateRange(
			Branch bnch, Specifics specf, Users user,
			EntityManager em, String fromDate, String toDate) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "' and obj.transactionBranch='" + bnch.getId()
				+ "' and obj.transactionSpecifics='" + specf.getLinkIncomeExpenseSpecifics().getId()
				+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or obj.transactionPurpose=25) and obj.transactionDate between '"
				+ fromDate + "' and '" + toDate + "' and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		List<Transaction> expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	@Override
	public Double getSellStockForThisIncomeItemDateRange(Branch bnch, Specifics specf, Users user, EntityManager em,
			String fromDate, String toDate) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "' and obj.transactionSpecifics='" + specf.getId()
				+ "' AND (obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionDate between '"
				+ fromDate + "' and '" + toDate + "' and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		List<Transaction> expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	@Override
	public Double getPurchaseStockForThisIncomeLinkedExpenseItemOnDate(Branch bnch, Specifics specf, Users user,
			EntityManager em, String presentDate) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "' and obj.transactionBranch='" + bnch.getId()
				+ "' and obj.transactionSpecifics='" + specf.getLinkIncomeExpenseSpecifics().getId()
				+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or obj.transactionPurpose=25) and obj.transactionDate='"
				+ presentDate + "' and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		List<Transaction> expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	@Override
	public Double getSellStockForThisIncomeItemOnDate(Branch bnch,
			Specifics specf, Users user, EntityManager em,
			String presentDate) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "' and obj.transactionBranch='" + bnch.getId()
				+ "' and obj.transactionSpecifics='" + specf.getId()
				+ "' AND (obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionDate='" + presentDate
				+ "' and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		List<Transaction> expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	@Override
	public Double getPurchaseStockForThisIncomeLinkedExpenseItemOnDateOrg(
			Branch bnch, Specifics specf, Users user,
			EntityManager em, String presentDate) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "'  and obj.transactionSpecifics='"
				+ specf.getLinkIncomeExpenseSpecifics().getId()
				+ "' AND (obj.transactionPurpose=3 or obj.transactionPurpose=4 or obj.transactionPurpose=11 or obj.transactionPurpose=25) and obj.transactionDate='"
				+ presentDate + "' and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		List<Transaction> expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	@Override
	public Double getSellStockForThisIncomeItemOnDateOrg(Branch bnch,
			Specifics specf, Users user, EntityManager em,
			String presentDate) {
		log.log(Level.FINE, "============ Start");
		Double stock = 0d;
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select SUM(obj.noOfUnits) from Transaction obj WHERE obj.transactionBranchOrganization='"
				+ user.getOrganization().getId() + "' and obj.transactionSpecifics='" + specf.getId()
				+ "' AND (obj.transactionPurpose=1 or obj.transactionPurpose=2) and obj.transactionDate='" + presentDate
				+ "' and obj.transactionStatus='Accounted' and obj.presentStatus=1");
		List<Transaction> expensestock = genericDAO.executeSimpleQuery(sbr.toString(), em);
		if (expensestock.size() > 0) {
			Object val = expensestock.get(0);
			if (val != null) {
				stock = Double.valueOf(val.toString());
			}
		}
		return stock;
	}

	@Override
	public String exportReportInventory(ObjectNode result, JsonNode json, Users user, EntityManager em,
			EntityTransaction entitytransaction, String path) throws Exception {
		log.log(Level.FINE, "============ Start");
		result.put("result", false);
		ArrayNode exportpian = result.putArray("riFileData");
		String branch = json.findValue("branch") != null ? json.findValue("branch").asText() : null;
		String fromDate = json.findValue("fromDate") != null ? json.findValue("fromDate").asText() : null;
		String riexporttype = json.findValue("riexporttype") != null ? json.findValue("riexporttype").asText() : null;
		String toDate = json.findValue("toDate") != null ? json.findValue("toDate").asText() : null;
		String orgName = user.getOrganization().getName().replaceAll("\\s", "");
		StringBuilder fileName = new StringBuilder(orgName).append("inventoryReport.").append(riexporttype);
		Branch branchentity = null;
		Date tDate = null;
		String fmDt = null;
		String toDt = null;
		List<InventoryReport> sortedInventoryReportList = Collections.emptyList();
		if (branch != null && !branch.equals("")) {
			branchentity = Branch.findById(Long.parseLong(branch));
		}
		if (fromDate != null && !fromDate.equals("")) {
			fmDt = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(fromDate));
		}
		if (toDate != null && !toDate.equals("")) {
			toDt = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(toDate));
		}
		Long timeInMillis = Calendar.getInstance().getTimeInMillis();
		sortedInventoryReportList = inventoryReportOnCriteria(branchentity, fmDt, toDt, user, em, entitytransaction);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Map<String, Object> params = trialBalanceDao.getParams(branchentity, fromDate, toDate);
		out = dynReportService.generateStaticReport("inventoryReport", sortedInventoryReportList, params, riexporttype,
				null);
		path = path + fileName;
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		FileOutputStream fileOut = new FileOutputStream(path);
		out.writeTo(fileOut);
		fileOut.close();
		return fileName.toString();
	}

	@Override
	public ObjectNode getReportAllInventoryInfo(ObjectNode result, JsonNode json, Users user, EntityManager em) {
		log.log(Level.FINE, "============ Start: " + json);
		try {
			result.put("result", false);
			ArrayNode displayInvan = result.putArray("displayInventory");

			String reportAllInventorySpecifics = json.findValue("reportAllInventorySpecifics") != null
					? json.findValue("reportAllInventorySpecifics").asText()
					: null;
			String reportAllInventoryBranch = json.findValue("reportAllInventoryBranch") != null
					? json.findValue("reportAllInventoryBranch").asText()
					: null;
			String fmDate = json.findValue("reportInventoryFromDate") != null
					? json.findValue("reportInventoryFromDate").asText()
					: null;
			String tDate = json.findValue("reportInventoryToDate") != null
					? json.findValue("reportInventoryToDate").asText()
					: null;
			Branch branch = null;
			if (reportAllInventoryBranch != null && !reportAllInventoryBranch.equals("")) {
				branch = Branch.findById(Long.parseLong(reportAllInventoryBranch));
			}
			Specifics inventorySpecifics = null;
			String fromDate = null;
			String toDate = null;
			if (reportAllInventorySpecifics != null && !reportAllInventorySpecifics.equals("")) {
				inventorySpecifics = Specifics.findById(Long.parseLong(reportAllInventorySpecifics));
			}
			if (fmDate == null || fmDate.equals("")) { // if start date not specified, show Inventory from financial
														// year start date of that org
				List<String> listOfFinYeardate = DateUtil.returnOrgFinancialStartEndDate(user.getOrganization());
				fromDate = listOfFinYeardate.get(0);
			} else {
				fromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(fmDate));
			}
			if (tDate == null || tDate.equals("")) {
				toDate = IdosConstants.mysqldf.format(Calendar.getInstance().getTime());
			} else {
				toDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(tDate));
			}
			HashMap<Long, DisplayTradingInventory> displayInvMap = new HashMap<Long, DisplayTradingInventory>();
			consolidatedInventoryForOpeningBal(branch, inventorySpecifics, 1, displayInvMap, fromDate, em, user);
			consolidatedInventoryForOpeningBal(branch, inventorySpecifics, 2, displayInvMap, fromDate, em, user);
			consolidatedInventoryForBuy(branch, inventorySpecifics, displayInvMap, fromDate, toDate, em, user);
			consolidatedInventoryForMultipleSellMappedToOneBuy(branch, inventorySpecifics, displayInvMap, fromDate,
					toDate, em, user); // for sell trade
			Iterator invMap = displayInvMap.entrySet().iterator();
			while (invMap.hasNext()) {
				Map.Entry me = (Map.Entry) invMap.next();
				DisplayTradingInventory inv = (DisplayTradingInventory) me.getValue();
				ObjectNode row = Json.newObject();
				row.put("itemId", inv.getItemId());
				row.put("itemName", inv.getItemName());
				if (inv.getSellItems() != null) {
					row.put("sellItemNames", inv.getSellItems());
				} else {
					row.put("sellItemNames", "");
				}
				row.put("calcMethod", inv.getCalcMethod());
				row.put("openingQty", inv.getOpeningBalQty());
				row.put("openingBal", IdosConstants.decimalFormat.format(inv.getOpeningBalValue()));
				row.put("buyQty", inv.getBuyQty());
				row.put("buyVal", IdosConstants.decimalFormat.format(inv.getBuyValue()));
				row.put("sellQty", inv.getSellQty());
				row.put("sellVal", IdosConstants.decimalFormat.format(inv.getSellValue()));
				row.put("closingQty", inv.getClosingBalQty());
				row.put("closingBal", IdosConstants.decimalFormat.format(inv.getClosingBalValue()));
				row.put("unitOfMeasure", inv.getUnitOfMeasure());
				if (inv.getSalesAmount() != null) {
					row.put("salesAmount", IdosConstants.decimalFormat.format(inv.getSalesAmount()));
				} else {
					row.put("salesAmount", "");
				}
				if (inv.getSalesMarginAmount() != null) {
					row.put("salesMarginAmount", IdosConstants.decimalFormat.format(inv.getSalesMarginAmount()));
				} else {
					row.put("salesMarginAmount", "");
				}
				if (inv.getSalesMarginPercent() != null) {
					row.put("salesMarginPercent", IdosConstants.decimalFormat.format(inv.getSalesMarginPercent()));
				} else {
					row.put("salesMarginPercent", "");
				}

				displayInvan.add(row);
			}
			result.put("result", true);
		} catch (Exception ex) {
			log.log(Level.SEVERE, "Error", ex);

		}
		return result;
	}

	private void consolidatedInventoryForOpeningBal(Branch branch, Specifics inventorySpecifics, int transactionType,
			HashMap<Long, DisplayTradingInventory> displayInvMap, String fromDate, EntityManager em, Users user) {
		StringBuilder sbr = null;
		if (transactionType == IdosConstants.TRADING_INV_BUY) {
			sbr = new StringBuilder(
					"select obj.transactionSpecifics.id, SUM(obj.noOfExpUnitsConvertedToIncUnits), SUM(obj.quantityMatchedWithSell), SUM(obj.grossValue), obj.transactionType from TradingInventory obj where obj.organization='"
							+ user.getOrganization().getId()
							+ "' and ((obj.transactionType in (1,5,7,9) and obj.date  <= '" + fromDate
							+ "') OR (obj.transactionType='3'))");
			if (branch != null) {
				sbr.append(" and obj.branch='" + branch.getId() + "'");
			}
			if (inventorySpecifics != null) {
				sbr.append(" and obj.transactionSpecifics='" + inventorySpecifics.getId() + "'");
			}
			sbr.append(" and obj.presentStatus=1 group by obj.transactionSpecifics, obj.transactionType");
		} else if (transactionType == IdosConstants.TRADING_INV_SELL) {
			sbr = new StringBuilder(
					"select obj.buySpecifics.id, SUM(obj.totalQuantity), 0 as tmp, SUM(obj.grossValue), obj.transactionType from TradingInventory obj where obj.organization='"
							+ user.getOrganization().getId()
							+ "' and obj.transactionType in (2,6,8,10) and  obj.date <= '" + fromDate + "'");
			if (branch != null) {
				sbr.append(" and obj.branch='" + branch.getId() + "'");
			}
			if (inventorySpecifics != null) {
				sbr.append(" and obj.buySpecifics='" + inventorySpecifics.getId() + "'");
			}
			sbr.append(" and obj.presentStatus=1 group by obj.buySpecifics, obj.transactionType");
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "HQL: " + sbr);
		}
		String queryString = em.createQuery(sbr.toString()).unwrap(org.hibernate.Query.class).getQueryString();
		System.out.println("Generated Query: " + queryString);
		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();

		for (Object[] tradeData : txnLists) {
			Double qty = 0.0;
			Double grossAmt = 0.0;
			Long specificsId = Long.parseLong(tradeData[0].toString());
			Specifics specific = Specifics.findById(specificsId);
			// get mutliple sell items mapped to this buy item
			String sellItems = "";
			sbr = new StringBuilder(
					"select obj from Specifics obj where obj.organization='" + user.getOrganization().getId()
							+ "' and obj.linkIncomeExpenseSpecifics='" + specificsId + "' and obj.presentStatus=1");
			List<Specifics> sellSpecificsList = genericDAO.executeSimpleQuery(sbr.toString(), em);
			for (Specifics specifics : sellSpecificsList) {
				sellItems = sellItems + specifics.getName() + ",";
			}
			if (tradeData[1] != null) {
				qty = Double.parseDouble(String.valueOf(tradeData[1]));
			}
			if (tradeData[3] != null) {
				grossAmt = Double.parseDouble(String.valueOf(tradeData[3]));
			}
			if (displayInvMap.containsKey(specificsId)) {
				DisplayTradingInventory invrow = (DisplayTradingInventory) displayInvMap.get(specificsId);
				invrow.setClosingBalQty(invrow.getClosingBalQty() - qty);
				invrow.setClosingBalValue(invrow.getClosingBalValue() - grossAmt);
				invrow.setOpeningBalQty(invrow.getOpeningBalQty() - qty);
				invrow.setOpeningBalValue(invrow.getOpeningBalValue() - grossAmt);
			} else {
				DisplayTradingInventory invrow = new DisplayTradingInventory();
				invrow.setItemId(specific.getId());
				invrow.setItemName(specific.getName());
				invrow.setSellItems(sellItems);
				invrow.setOpeningBalQty(qty);
				invrow.setOpeningBalValue(grossAmt);
				invrow.setUnitOfMeasure(specific.getIncomeUnitsMeasure());
				invrow.setClosingBalQty(qty);
				invrow.setClosingBalValue(grossAmt);
				invrow.setCalcMethod("FIFO");
				displayInvMap.put(specific.getId(), invrow);
			}
		}
	}

	// Multiple sell items say LGTVSell1 (sold qty=2), LGTVSell2(qty=1) is mapped to
	// single buy(expense qty=5 ) item LGTVBuy
	// so need to get consolidated value of sell items(2+1=3 qty) for this single
	// buy item
	private void consolidatedInventoryForMultipleSellMappedToOneBuy(Branch branch, Specifics inventorySpecifics,
			HashMap<Long, DisplayTradingInventory> displayInvMap, String fromDate, String toDate, EntityManager em,
			Users user) {
		StringBuilder sbr = new StringBuilder(
				"select obj.buySpecifics.id,SUM(obj.totalQuantity),SUM(obj.grossValue), sum(obj.transactionGorss) from TradingInventory obj where obj.transactionType in (2,6,8,10) and obj.organization='"
						+ user.getOrganization().getId() + "' and obj.date  between '" + fromDate + "' and '" + toDate
						+ "'");
		if (branch != null) {
			sbr.append(" and obj.branch='" + branch.getId() + "'");
		}
		if (inventorySpecifics != null) {
			sbr.append(" and obj.buySpecifics='" + inventorySpecifics.getId() + "'");
		}
		sbr.append(" and obj.presentStatus=1 group by obj.buySpecifics");

		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "HQL: " + sbr);
		}

		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
		Double qty = 0.0;
		Double grossAmt = 0.0;
		Long buySpecificsId;
		if (!txnLists.isEmpty()) {
			for (Object[] tradeData : txnLists) {
				if (tradeData[0] != null && !"".equals(tradeData[0])) {
					buySpecificsId = Long.parseLong(tradeData[0].toString());
				} else {
					continue;
				}

				Specifics buySpecific = Specifics.findById(buySpecificsId);
				qty = Double.parseDouble(String.valueOf(tradeData[1]));
				grossAmt = Double.parseDouble(String.valueOf(tradeData[2]));
				Double txnGrossAmt = 0.0;
				if (tradeData[3] != null) {
					txnGrossAmt = Double.parseDouble(String.valueOf(tradeData[3]));
				}
				if (displayInvMap.containsKey(buySpecific.getId())) {
					DisplayTradingInventory invrow = (DisplayTradingInventory) displayInvMap.get(buySpecific.getId());
					invrow.setSellQty(qty);
					invrow.setSellValue(grossAmt);
					invrow.setClosingBalQty(invrow.getClosingBalQty() - qty);
					invrow.setClosingBalValue(invrow.getClosingBalValue() - grossAmt);
					if (tradeData[3] != null) {
						if (invrow.getSalesAmount() != null) {
							invrow.setSalesAmount(invrow.getSalesAmount() + txnGrossAmt);
						} else {
							invrow.setSalesAmount(txnGrossAmt);
						}
						Double saleMargin = 0.0;
						Double saleAmount = 0.0;
						if (invrow.getSalesAmount() != null) {
							saleAmount = invrow.getSalesAmount();
							saleMargin = saleAmount - invrow.getSellValue();
						}
						invrow.setSalesMarginAmount(saleMargin);
						if (saleAmount != 0) {
							invrow.setSalesMarginPercent(saleMargin / saleAmount);
						}
					}
				} else {
					// get mutliple sell items mapped to this buy item
					Specifics sellSpecific = null;
					String sellItems = "";
					sbr = new StringBuilder("");
					sbr.append("select obj from Specifics obj where obj.organization='" + user.getOrganization().getId()
							+ "' and obj.linkIncomeExpenseSpecifics='" + buySpecificsId + "' and obj.presentStatus=1");
					List<Specifics> sellSpecificsList = genericDAO.executeSimpleQuery(sbr.toString(), em);
					if (sellSpecificsList != null && sellSpecificsList.size() > 0) {
						for (Specifics sellspecificItem : sellSpecificsList) {
							sellSpecific = Specifics.findById(sellspecificItem.getId());
							if (sellSpecific != null) {
								sellItems = sellItems + sellSpecific.getName() + ",";
							}
							// sellItems = sellItems.replaceAll(",$", "");
						}
					}
					DisplayTradingInventory invrow = new DisplayTradingInventory();
					invrow.setItemId(buySpecific.getId());
					invrow.setItemName(buySpecific.getName()); // buy item name
					invrow.setSellItems(sellItems); // mapped multiple sell items
					invrow.setCalcMethod("FIFO");
					invrow.setSellQty(qty);
					invrow.setUnitOfMeasure(buySpecific.getIncomeUnitsMeasure());
					invrow.setSellValue(grossAmt);
					invrow.setClosingBalQty(invrow.getClosingBalQty() - qty);
					invrow.setClosingBalValue(invrow.getClosingBalValue() - grossAmt);
					if (tradeData[3] != null) {
						invrow.setSalesAmount(txnGrossAmt);
						Double saleMargin = 0.0;
						Double saleAmount = txnGrossAmt;
						saleMargin = saleAmount - invrow.getSellValue();
						invrow.setSalesMarginAmount(saleMargin);
						if (saleAmount != 0) {
							invrow.setSalesMarginPercent(saleMargin / saleAmount);
						}
					}
					displayInvMap.put(buySpecific.getId(), invrow);
				}
			}
		}
	}

	private void consolidatedInventoryForBuy(Branch branch, Specifics inventorySpecifics,
			HashMap<Long, DisplayTradingInventory> displayInvMap, String fromDate, String toDate, EntityManager em,
			Users user) throws IDOSException {

		// get consolidated buy trades and purchase return trades (for purchase return
		// qty is stored as -ve)
		StringBuilder sbr = new StringBuilder(
				"select obj.transactionSpecifics.id, SUM(obj.noOfExpUnitsConvertedToIncUnits), SUM(obj.grossValue) from TradingInventory obj where obj.transactionType in (1,5,7,9) and obj.organization='"
						+ user.getOrganization().getId() + "' and obj.date  between '" + fromDate + "' and '" + toDate
						+ "'");
		if (branch != null) {
			sbr.append(" and obj.branch='" + branch.getId() + "'");
		}
		if (inventorySpecifics != null) {
			sbr.append(" and obj.transactionSpecifics='" + inventorySpecifics.getId() + "'");
		}
		sbr.append(" and obj.presentStatus=1 group by obj.transactionSpecifics");
		List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
		Double qty = 0.0;
		Double grossAmt = 0.0;
		Long specificsId;
		if (!txnLists.isEmpty()) {
			for (Object[] tradeData : txnLists) {
				specificsId = (Long) tradeData[0];
				Specifics buySpecific = null;
				buySpecific = Specifics.findById(specificsId);
				qty = tradeData[1] == null ? 0.0 : (Double) tradeData[1];
				grossAmt = tradeData[2] == null ? 0.0 : (Double) tradeData[2];
				if (displayInvMap.containsKey(buySpecific.getId())) {
					DisplayTradingInventory invrow = (DisplayTradingInventory) displayInvMap.get(buySpecific.getId());
					invrow.setBuyQty(qty);
					invrow.setBuyValue(grossAmt);
					invrow.setClosingBalQty(invrow.getClosingBalQty() + qty);
					invrow.setClosingBalValue(invrow.getClosingBalValue() + grossAmt);
				} else {
					// get mutliple sell items mapped to this buy item
					Specifics sellSpecific = null;
					String sellItems = "";
					sbr = new StringBuilder("");
					sbr.append("select obj from Specifics obj where obj.organization='" + user.getOrganization().getId()
							+ "' and obj.linkIncomeExpenseSpecifics='" + specificsId + "' and obj.presentStatus=1");
					List<Specifics> sellSpecificsList = genericDAO.executeSimpleQuery(sbr.toString(), em);
					if (sellSpecificsList != null && sellSpecificsList.size() > 0) {
						for (Specifics sellspecificItem : sellSpecificsList) {
							sellSpecific = Specifics.findById(sellspecificItem.getId());
							if (sellSpecific != null) {
								sellItems = sellItems + sellSpecific.getName() + ",";
							}
							// sellItems = sellItems.replaceAll(",$", "");
						}
					}
					DisplayTradingInventory invrow = new DisplayTradingInventory();
					invrow.setItemId(buySpecific.getId());
					invrow.setItemName(buySpecific.getName());
					invrow.setSellItems(sellItems); // mapped multiple sell items
					invrow.setCalcMethod("FIFO");
					invrow.setBuyQty(qty);
					invrow.setUnitOfMeasure(buySpecific.getIncomeUnitsMeasure());
					invrow.setBuyValue(grossAmt);
					invrow.setClosingBalQty(invrow.getClosingBalQty() + qty);
					invrow.setClosingBalValue(invrow.getClosingBalValue() + grossAmt);
					displayInvMap.put(buySpecific.getId(), invrow);
				}
			}
		}
	}

	public ObjectNode displayDetailInventory(ObjectNode result, JsonNode json, Users user, EntityManager em,
			EntityTransaction entitytransaction) {
		ArrayNode detailedInvSpecificnInfoan = result.putArray("detailedInventorySpecInfo");
		ArrayNode detailedInvan = result.putArray("detailedInventory");
		try {
			StringBuilder sbr = new StringBuilder("");
			List<String> listOfFinYeardate = DateUtil.returnOrgFinancialStartEndDate(user.getOrganization());
			String fromDate = listOfFinYeardate.get(0);
			String toDate = IdosConstants.mysqldf.format(Calendar.getInstance().getTime());
			Long specificsId = json.findValue("specificsID").asLong();
			Specifics buySpecific = Specifics.findById(specificsId);
			ObjectNode rowMain = Json.newObject();
			rowMain.put("itemName", buySpecific.getName());

			// get sell specific
			String sellItemIds = "(";
			sbr.append("select obj from Specifics obj where obj.organization='" + user.getOrganization().getId()
					+ "' and obj.linkIncomeExpenseSpecifics='" + specificsId + "' and obj.presentStatus=1");
			List<Specifics> sellSpecificsList = genericDAO.executeSimpleQuery(sbr.toString(), em);
			if (sellSpecificsList != null && sellSpecificsList.size() > 0) {
				for (Specifics sellSpecificItem : sellSpecificsList) {
					sellItemIds = sellItemIds + sellSpecificItem.getId() + ",";
				}
				sellItemIds = sellItemIds.replaceAll(",$", "");
				sellItemIds = sellItemIds + ")";
			}

			result.put("result", true);
			rowMain.put("calcMethod", buySpecific.getTradingInventoryCalcMethod());
			detailedInvSpecificnInfoan.add(rowMain);
			double closingBalQty = 0.0;
			double closingBalVal = 0.0;
			// get consolidated opening bal: buy trades of date < fromdate or opening bal
			// trades with transactionType=3
			sbr = new StringBuilder("");
			sbr.append(
					"select obj.transactionSpecifics.id, obj.date, SUM(obj.noOfExpUnitsConvertedToIncUnits), SUM(obj.grossValue) from TradingInventory obj where obj.transactionSpecifics.id='"
							+ buySpecific.getId() + "' and obj.organization='" + user.getOrganization().getId()
							+ "' and ((obj.transactionType='1' and  obj.date  <= '" + fromDate
							+ "') OR (obj.transactionType='3')) and obj.presentStatus=1");
			if (log.isLoggable(Level.FINE))
				log.log(Level.FINE, "Hql: " + sbr);
			List<Object[]> txnLists = em.createQuery(sbr.toString()).getResultList();
			if (!txnLists.isEmpty()) {
				for (Object[] tradeData : txnLists) {
					if (tradeData[0] != null && tradeData[1] != null && tradeData[2] != null && tradeData[3] != null) {
						specificsId = new Long(tradeData[0].toString());
						Specifics specific = Specifics.findById(specificsId);
						if (tradeData[1] == null || tradeData[1].equals("")) { // if start date not specified, show
																				// Inventory from financial year start
																				// date of that org
							rowMain.put("openingBalDate", IdosConstants.mysqldf.format(fromDate));
						} else {
							rowMain.put("openingBalDate", IdosConstants.mysqldf.format(tradeData[1]));
						}
						closingBalQty = Double.parseDouble(String.valueOf(tradeData[2]));
						closingBalVal = Double.parseDouble(String.valueOf(tradeData[3]));
						rowMain.put("openingBalQty", closingBalQty);
						rowMain.put("openingBalVal", closingBalVal);
					} else {
						rowMain.put("openingBalQty", 0);
						rowMain.put("openingBalVal", 0);
					}
				}
			}
			// get Sell trades, type =2
			sbr = new StringBuilder("");
			sbr.append(
					"select obj from TradingInventory obj where obj.transactionType in (1,2,5,6) and (obj.transactionSpecifics.id in "
							+ sellItemIds + " OR obj.transactionSpecifics.id='" + buySpecific.getId()
							+ "') and obj.organization='" + user.getOrganization().getId()
							+ "' and obj.presentStatus=1 and obj.date  between '" + fromDate + "' and '" + toDate
							+ "' order by obj.date asc");
			List<TradingInventory> tradingInvList = genericDAO.executeSimpleQuery(sbr.toString(), em);
			if (tradingInvList != null && tradingInvList.size() > 0) {
				for (TradingInventory inv : tradingInvList) {
					ObjectNode row = Json.newObject();
					row.put("invDate", IdosConstants.mysqldf.format(inv.getDate()));
					if (inv.getTransactionType() == IdosConstants.TRADING_INV_BUY) { // buy txn
						row.put("buyQty", inv.getNoOfExpUnitsConvertedToIncUnits());
						row.put("buyRate", inv.getCalcualtedRate()); // (inv.getGrossValue() /
																		// inv.getNoOfExpUnitsConvertedToIncUnits())
																		// buyrate in db is as per unit of measeure of
																		// buy, we want it as per sell unit
						row.put("buyVal", inv.getGrossValue());
						row.put("sellItem", "");
						row.put("sellQty", "");
						row.put("sellRate", "");
						row.put("sellVal", "");
						// double buyQtyRemaining = inv.getNoOfExpUnitsConvertedToIncUnits() -
						// inv.getQuantityMatchedWithSell();
						double buyQtyRemaining = inv.getNoOfExpUnitsConvertedToIncUnits();
						closingBalQty = closingBalQty + buyQtyRemaining;
						closingBalVal = closingBalVal + (buyQtyRemaining * inv.getCalcualtedRate());
					} else if (inv.getTransactionType() == IdosConstants.TRADING_INV_SELL) { // buy txn
						Specifics sellItem = Specifics.findById(inv.getTransactionSpecifics().getId());
						row.put("buyQty", "");
						row.put("buyRate", "");
						row.put("buyVal", "");
						row.put("sellItem", sellItem.getName());
						row.put("sellQty", inv.getTotalQuantity());
						row.put("sellRate", inv.getCalcualtedRate());
						row.put("sellVal", inv.getGrossValue());
						closingBalQty = closingBalQty - inv.getTotalQuantity();
						closingBalVal = closingBalVal - inv.getGrossValue();
					} else if (inv.getTransactionType() == IdosConstants.TRADING_INV_PURCHASE_RET) { // purchase ret txn
						row.put("buyQty", inv.getNoOfExpUnitsConvertedToIncUnits());
						row.put("buyRate", inv.getCalcualtedRate()); // (inv.getGrossValue() /
																		// inv.getNoOfExpUnitsConvertedToIncUnits())
																		// buyrate in db is as per unit of measeure of
																		// buy, we want it as per sell unit
						row.put("buyVal", inv.getGrossValue());
						row.put("sellItem", "");
						row.put("sellQty", "");
						row.put("sellRate", "");
						row.put("sellVal", "");
						double buyQtyRemaining = inv.getNoOfExpUnitsConvertedToIncUnits();
						closingBalQty = closingBalQty + buyQtyRemaining;
						closingBalVal = closingBalVal + (buyQtyRemaining * inv.getCalcualtedRate());
					} else if (inv.getTransactionType() == IdosConstants.TRADING_INV_SALES_RET) { // sales return
						Specifics sellItem = Specifics.findById(inv.getTransactionSpecifics().getId());
						row.put("buyQty", "");
						row.put("buyRate", "");
						row.put("buyVal", "");
						row.put("sellItem", sellItem.getName());
						row.put("sellQty", inv.getTotalQuantity());
						row.put("sellRate", inv.getCalcualtedRate());
						row.put("sellVal", inv.getGrossValue());
						closingBalQty = closingBalQty - inv.getTotalQuantity();
						closingBalVal = closingBalVal - inv.getGrossValue();
					}
					detailedInvan.add(row);
				}
			}
			rowMain.put("closingBalDate", toDate);
			rowMain.put("closingBalQty", closingBalQty);
			rowMain.put("closingBalVal", closingBalVal);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	@Override
	public void getMidInventory(ObjectNode result, JsonNode json, Users user, EntityManager em) throws IDOSException {
		INVENTORY_DAO.getMidInventory(result, json, user, em);
	}

	@Override
	public void getTxnLevelInventory(ObjectNode result, JsonNode json, Users user, EntityManager em)
			throws IDOSException {
		INVENTORY_DAO.getTxnLevelInventory(result, json, user, em);
	}

	/*
	 * public InventorySpecificsReport getSpecificsInventoryInfor(Specifics
	 * specifics, Branch branch, Users user, EntityManager em) {
	 * log.log(Level.FINE, "============ Start");
	 * InventorySpecificsReport newInvSpecfReport = null;
	 * Specifics invSellItem = null;
	 * StringBuffer sbr = new StringBuffer("");
	 * sbr.
	 * append("select obj from Specifics obj where obj.particularsId.accountCode=1000000000000000000 and obj.organization='"
	 * + user.getOrganization().getId() + "' and obj.linkIncomeExpenseSpecifics='" +
	 * specifics.getId() + "'");
	 * List<Specifics> invSellItemList =
	 * genericDAO.executeSimpleQuery(sbr.toString(), em);
	 * if (!invSellItemList.isEmpty() && invSellItemList.size() > 0) {
	 * invSellItem = invSellItemList.get(0);
	 * }
	 * if (specifics != null && invSellItem != null) {
	 * newInvSpecfReport = new InventorySpecificsReport();
	 * newInvSpecfReport.setParticulars(specifics.getName());
	 * //opening balance for the inventory specific item whether in branch or whole
	 * organization
	 * Double purchaseOpeningStock = getPurchaseOpeningStock(branch, specifics,
	 * user, em);
	 * newInvSpecfReport.setInventoryOpeningBalanceUnit(purchaseOpeningStock);
	 * Double inventoryExpenseUnit =
	 * getPurchaseStockForThisIncomeLinkedExpenseItem(branch, invSellItem, user,
	 * em);
	 * newInvSpecfReport.setInventoryExpenseUnit(inventoryExpenseUnit);
	 * Double inventorySellUnit = getSellStockForThisIncomeItem(branch, invSellItem,
	 * user, em);
	 * newInvSpecfReport.setInventorySellUnit(inventorySellUnit);
	 * Double closingInventory = purchaseOpeningStock + inventoryExpenseUnit -
	 * inventorySellUnit;
	 * newInvSpecfReport.setInventoryClosingBalanceUnit(closingInventory);
	 * }
	 * return newInvSpecfReport;
	 * }
	 */

	public List<Specifics> listAllInventoryItems(Users user) {
		log.log(Level.FINE, "============ Start");
		List<Specifics> inventoryItemsList = new ArrayList<Specifics>();
		StringBuffer sbr = new StringBuffer("");
		sbr.append("select obj from Specifics obj where obj.organization='" + user.getOrganization().getId()
				+ "' and obj.particularsId.accountCode=1000000000000000000 and obj.linkIncomeExpenseSpecifics IS NOT NULL and obj.presentStatus=1");
		List<Specifics> specfTxnPurposeList = genericDAO.executeSimpleQuery(sbr.toString(), entityManager);
		if (!specfTxnPurposeList.isEmpty() && specfTxnPurposeList.size() > 0) {
			for (Specifics expSpecf : specfTxnPurposeList) {
				inventoryItemsList.add(expSpecf.getLinkIncomeExpenseSpecifics());
			}
		}
		return inventoryItemsList;
	}

	@Override
	public String exportInventory(ObjectNode result, JsonNode json, Users user, EntityManager em,
			EntityTransaction entitytransaction, String path) throws Exception {
		log.log(Level.FINE, "============ Start " + json);
		result.put("result", false);
		// ArrayNode exportrian = result.putArray("riFileData");
		// ArrayNode iritemsan = result.putArray("inventoryAllItemsData");
		// String specifics = json.findValue("reportAllInventorySpecifics") != null ?
		// json.findValue("reportAllInventorySpecifics").asText() : null;
		// String branch = json.findValue("reportAllInventoryBranch") != null ?
		// json.findValue("reportAllInventoryBranch").asText() : null;
		String irexporttype = json.findValue("irexporttype") != null ? json.findValue("irexporttype").asText() : null;
		String orgName = user.getOrganization().getName().replaceAll("\\s", "");
		StringBuilder fileName = new StringBuilder(orgName).append("inventoryItemReport.").append(irexporttype);

		List<InventorySpecificsReport> listInventorySpecificsReport = new ArrayList<InventorySpecificsReport>();
		String reportAllInventorySpecifics = json.findValue("reportAllInventorySpecifics") != null
				? json.findValue("reportAllInventorySpecifics").asText()
				: null;
		String reportAllInventoryBranch = json.findValue("reportAllInventoryBranch") != null
				? json.findValue("reportAllInventoryBranch").asText()
				: null;
		String fmDate = json.findValue("reportInventoryFromDate") != null
				? json.findValue("reportInventoryFromDate").asText()
				: null;
		String tDate = json.findValue("reportInventoryToDate") != null
				? json.findValue("reportInventoryToDate").asText()
				: null;
		Branch branch = null;
		if (reportAllInventoryBranch != null && !reportAllInventoryBranch.equals("")) {
			branch = Branch.findById(Long.parseLong(reportAllInventoryBranch));
		}
		Specifics inventorySpecifics = null;
		String fromDate = null;
		String toDate = null;
		if (reportAllInventorySpecifics != null && !reportAllInventorySpecifics.equals("")) {
			inventorySpecifics = Specifics.findById(Long.parseLong(reportAllInventorySpecifics));
		}
		if (fmDate == null || fmDate.equals("")) { // if start date not specified, show Inventory from financial year
													// start date of that org
			List<String> listOfFinYeardate = DateUtil.returnOrgFinancialStartEndDate(user.getOrganization());
			fromDate = listOfFinYeardate.get(0);
		} else {
			fromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(fmDate));
		}
		if (tDate == null || tDate.equals("")) {
			toDate = IdosConstants.mysqldf.format(Calendar.getInstance().getTime());
		} else {
			toDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(tDate));
		}
		HashMap<Long, DisplayTradingInventory> displayInvMap = new HashMap<Long, DisplayTradingInventory>();
		consolidatedInventoryForOpeningBal(branch, inventorySpecifics, 1, displayInvMap, fromDate, em, user); // for buy
																												// trade
		consolidatedInventoryForOpeningBal(branch, inventorySpecifics, 2, displayInvMap, fromDate, em, user); // for buy
																												// trade
		consolidatedInventoryForBuy(branch, inventorySpecifics, displayInvMap, fromDate, toDate, em, user); // for buy
																											// trade
		consolidatedInventoryForMultipleSellMappedToOneBuy(branch, inventorySpecifics, displayInvMap, fromDate, toDate,
				em, user); // for sell trade
		Iterator invMap = displayInvMap.entrySet().iterator();
		while (invMap.hasNext()) {
			Map.Entry me = (Map.Entry) invMap.next();
			DisplayTradingInventory inv = (DisplayTradingInventory) me.getValue();
			// ObjectNode row = Json.newObject();
			InventorySpecificsReport row = new InventorySpecificsReport();
			row.setItemId(inv.getItemId());
			row.setItemName(inv.getItemName());
			if (inv.getSellItems() != null) {
				row.setSellItems(inv.getSellItems());
			} else {
				row.setSellItems("");
			}
			row.setCalcMethod(inv.getCalcMethod());
			row.setOpeningBalQty(IdosConstants.decimalFormat.format(inv.getOpeningBalQty()));
			row.setOpeningBalValue(IdosConstants.decimalFormat.format(inv.getOpeningBalValue()));
			row.setBuyQty(IdosConstants.decimalFormat.format(inv.getBuyQty()));
			row.setBuyValue(IdosConstants.decimalFormat.format(inv.getBuyValue()));
			row.setSellQty(IdosConstants.decimalFormat.format(inv.getSellQty()));
			row.setSellValue(IdosConstants.decimalFormat.format(inv.getSellValue()));
			row.setClosingBalQty(IdosConstants.decimalFormat.format(inv.getClosingBalQty()));
			row.setClosingBalValue(IdosConstants.decimalFormat.format(inv.getClosingBalValue()));
			row.setUnitOfMeasure(inv.getUnitOfMeasure());
			if (inv.getSalesAmount() != null) {
				row.setSalesAmount(IdosConstants.decimalFormat.format(inv.getSalesAmount()));
			} else {
				row.setSalesAmount("");
			}
			if (inv.getSalesMarginAmount() != null) {
				row.setSalesMarginAmount(IdosConstants.decimalFormat.format(inv.getSalesMarginAmount()));
			} else {
				row.setSalesMarginAmount("");
			}
			if (inv.getSalesMarginPercent() != null) {
				row.setSalesMarginPercent(IdosConstants.decimalFormat.format(inv.getSalesMarginPercent()));
			} else {
				row.setSalesMarginPercent("");
			}

			listInventorySpecificsReport.add(row);
		}

		/*
		 * if (specifics != null && !specifics.equals("")) {
		 * inventorySpecifics = Specifics.findById(Long.parseLong(specifics));
		 * if (branch != null && !branch.equals("")) {
		 * branchentity = Branch.findById(Long.parseLong(branch));
		 * }
		 * InventorySpecificsReport inventorySpecificsReport =
		 * getSpecificsInventoryInfor(inventorySpecifics, branchentity, user, em);
		 * if (inventorySpecificsReport != null) {
		 * listInventorySpecificsReport.add(inventorySpecificsReport);
		 * }
		 * List<Specifics> invItem = listAllInventoryItems(user);
		 * for (Specifics invSpecf : invItem) {
		 * ObjectNode row = Json.newObject();
		 * row.put("id", invSpecf.getId());
		 * row.put("name", invSpecf.getName());
		 * iritemsan.add(row);
		 * }
		 * } else {
		 * List<Specifics> invItem = listAllInventoryItems(user);
		 * for (Specifics invSpecf : invItem) {
		 * ObjectNode row = Json.newObject();
		 * row.put("id", invSpecf.getId());
		 * row.put("name", invSpecf.getName());
		 * iritemsan.add(row);
		 * InventorySpecificsReport inventorySpecificsReport =
		 * getSpecificsInventoryInfor(invSpecf, branchentity, user, em);
		 * if (inventorySpecificsReport != null) {
		 * listInventorySpecificsReport.add(inventorySpecificsReport);
		 * }
		 * }
		 * }
		 */
		Map<String, Object> params = getInvParams(branch, inventorySpecifics, fromDate, toDate);
		ByteArrayOutputStream out = dynReportService.generateStaticReport("inventoryItemReport",
				listInventorySpecificsReport, params, irexporttype, null);
		path = path + fileName;
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		FileOutputStream fileOut = new FileOutputStream(path);
		out.writeTo(fileOut);
		fileOut.close();
		return fileName.toString();
	}

	public Map<String, Object> getInvParams(Branch branch, Specifics specifics, String fromDate, String toDate) {
		Map<String, Object> params = new HashMap<String, Object>(4);
		if (null == branch || null == branch.getName()) {
			params.put("branchName", "");
		} else {
			params.put("branchName", branch.getName());
		}
		if (null != specifics) {
			params.put("itemName", specifics.getName());
		} else {
			params.put("itemName", "");
		}
		params.put("fromDate", fromDate);
		params.put("toDate", toDate);

		return params;
	}
}
