package com.idos.dao;

import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @auther Sunil K. Namdev created on 16.02.2018
 */
public class InventoryDAOImpl implements InventoryDAO {
	@Override
	public void getMidInventory(ObjectNode result, JsonNode json, Users user, EntityManager em) throws IDOSException {
		String fromDateStr = json.findValue("fromDateStr") != null ? json.findValue("fromDateStr").asText() : null;
		String toDateStr = json.findValue("toDateStr") != null ? json.findValue("toDateStr").asText() : null;
		Long itemid = json.findValue("specificsID") != null ? json.findValue("specificsID").asLong() : 0L;
		String fromDate = null;
		String toDate = null;

		try {
			if (fromDateStr == null || fromDateStr.equals("")) { // if start date not specified, show Inventory from
																	// financial year start date of that org
				List<String> listOfFinYeardate = DateUtil.returnOrgFinancialStartEndDate(user.getOrganization());
				fromDate = listOfFinYeardate.get(0);
			} else {
				fromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(fromDateStr));
			}
			if (toDateStr == null || toDateStr.equals("")) {
				toDate = IdosConstants.mysqldf.format(Calendar.getInstance().getTime());
			} else {
				toDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(toDateStr));
			}

		} catch (ParseException ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
					"date range given for Inventory is wrong.", ex.getMessage());
		}

		Long organId = user.getOrganization().getId();
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "fromDate: " + fromDate + " currToDate: " + toDate + " organId: " + organId);
			log.log(Level.FINE, "HQL: " + BRANCH_ITEM_SQL);
		}

		Query query = em.createNativeQuery(BRANCH_ITEM_SQL);
		query.setParameter(1, organId);
		query.setParameter(2, itemid);
		query.setParameter(3, organId);
		query.setParameter(4, itemid);
		query.setParameter(5, fromDate);
		query.setParameter(6, organId);
		query.setParameter(7, itemid);
		query.setParameter(8, fromDate);
		query.setParameter(9, organId);
		query.setParameter(10, itemid);
		query.setParameter(11, fromDate);
		query.setParameter(12, toDate);
		query.setParameter(13, organId);
		query.setParameter(14, itemid);
		query.setParameter(15, fromDate);
		query.setParameter(16, toDate);

		List<Object[]> itemList = query.getResultList();
		long specificid;
		long branchid;
		Double oBalance;
		Double oBalanceUnit;
		Double buyUnit;
		Double buyAmount;
		Double saleUnit;
		Double saleAmount;
		Double cBalance;
		Double cBalanceUnit;
		Double txnSalesAmt;
		ArrayNode detailedInvan = result.putArray("midInventory");
		for (Object[] inventory : itemList) {
			if (null == inventory[0]) {
				continue;
			}
			ObjectNode row = Json.newObject();
			specificid = (int) inventory[0];
			branchid = (int) inventory[1];
			oBalanceUnit = inventory[2] == null ? 0.0 : ((Double) inventory[2]);
			oBalance = inventory[3] == null ? 0.0 : ((Double) inventory[3]);
			buyUnit = inventory[4] == null ? 0.0 : ((Double) inventory[4]);
			buyAmount = inventory[5] == null ? 0.0 : ((Double) inventory[5]);
			saleUnit = inventory[6] == null ? 0.0 : ((Double) inventory[6]);
			saleAmount = inventory[7] == null ? 0.0 : ((Double) inventory[7]);
			cBalanceUnit = inventory[8] == null ? 0.0 : ((Double) inventory[8]);
			cBalance = inventory[9] == null ? 0.0 : ((Double) inventory[9]);

			row.put("itemid", specificid);
			row.put("branchid", branchid);
			row.put("branch", Branch.findById(branchid).getName());
			row.put("openingBalance", IdosConstants.decimalFormat.format(oBalance));
			row.put("openingBalanceUnit", oBalanceUnit);
			row.put("buyUnit", buyUnit);
			row.put("buyAmount", IdosConstants.decimalFormat.format(buyAmount));
			row.put("saleUnit", saleUnit);
			row.put("saleAmount", IdosConstants.decimalFormat.format(saleAmount));
			row.put("closingUnit", cBalanceUnit);
			row.put("closingAmount", IdosConstants.decimalFormat.format(cBalance));
			if (inventory[10] != null) {
				txnSalesAmt = (Double) inventory[10];
				row.put("txnSaleAmount", IdosConstants.decimalFormat.format(txnSalesAmt));
				Double salesMargin = txnSalesAmt - saleAmount;
				row.put("saleMarginAmount", IdosConstants.decimalFormat.format(salesMargin));
				if (txnSalesAmt != 0)
					row.put("saleMarginPercent", IdosConstants.decimalFormat.format(salesMargin / txnSalesAmt));
				else
					row.put("saleMarginPercent", "");
			} else {
				row.put("txnSaleAmount", "");
				row.put("saleMarginAmount", "");
				row.put("saleMarginPercent", "");
			}
			detailedInvan.add(row);
		}
		result.put("status", true);
		Specifics specifics = Specifics.findById(itemid);
		result.put("itemName", specifics.getName());
		result.put("method",
				specifics.getTradingInventoryCalcMethod() == null ? "" : specifics.getTradingInventoryCalcMethod());

	}

	@Override
	public void getTxnLevelInventory(ObjectNode result, JsonNode json, Users user, EntityManager em)
			throws IDOSException {
		String fromDateStr = json.findValue("fromDateStr") != null ? json.findValue("fromDateStr").asText() : null;
		String toDateStr = json.findValue("toDateStr") != null ? json.findValue("toDateStr").asText() : null;
		Long itemid = json.findValue("specificsID") != null ? json.findValue("specificsID").asLong() : 0L;
		Long branchid = json.findValue("branchid") != null ? json.findValue("branchid").asLong() : 0L;
		Date fromDate = null;
		Date toDate = null;
		try {
			if (fromDateStr == null || fromDateStr.equals("")) { // if start date not specified, show Inventory from
																	// financial year start date of that org
				List<String> listOfFinYeardate = DateUtil.returnOrgFinancialStartEndDate(user.getOrganization());
				fromDate = IdosConstants.MYSQLDF.parse(listOfFinYeardate.get(0));
			} else {
				fromDate = IdosConstants.IDOSDF.parse(fromDateStr);
			}
			if (toDateStr == null || toDateStr.equals("")) {
				toDate = Calendar.getInstance().getTime();
			} else {
				toDate = IdosConstants.IDOSDF.parse(toDateStr);
			}

		} catch (ParseException ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
					"date range given for Inventory is wrong.", ex.getMessage());
		}

		Long organId = user.getOrganization().getId();
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "fromDate: " + fromDate + " currToDate: " + toDate + " organId: " + organId
					+ " itemid: " + itemid + " branchid=" + branchid);
		}
		Specifics specifics = Specifics.findById(itemid);
		if ("WAC".equalsIgnoreCase(specifics.getTradingInventoryCalcMethod())) {
			getWacInventory(result, organId, branchid, itemid, fromDate, toDate, em);
		} else {
			getFifoInventory(result, organId, branchid, itemid, fromDate, toDate, em);
		}
	}

	private DisplayTradingInventory getWacOpeningBalance(Long organId, Long branchid, Long itemid, Date fromDate,
			EntityManager em) {
		DisplayTradingInventory openBalanceDti = null;
		Query query = em.createQuery(BRANCH_TXN_OPEN_SQL);
		query.setParameter(1, organId);
		query.setParameter(2, branchid);
		query.setParameter(3, itemid);
		query.setParameter(4, organId);
		query.setParameter(5, branchid);
		query.setParameter(6, itemid);
		query.setParameter(7, fromDate);
		query.setParameter(8, organId);
		query.setParameter(9, branchid);
		query.setParameter(10, itemid);
		query.setParameter(11, fromDate);
		List<Object[]> openList = query.getResultList();
		if (log.isLoggable(Level.FINE))
			log.log(Level.FINE, "HQL: " + BRANCH_TXN_OPEN_SQL);
		if (openList.size() > 0) {
			openBalanceDti = new DisplayTradingInventory();
			Object[] inventory = openList.get(0);
			Double openBalanceUnit = inventory[1] == null ? 0.0 : ((Double) inventory[1]);
			Double openBalanceRate = inventory[2] == null ? 0.0 : ((Double) inventory[2]);
			Double openBalance = inventory[3] == null ? 0.0 : ((Double) inventory[3]);
			Double closingBalanceUnit = inventory[10] == null ? 0.0 : ((Double) inventory[10]);
			Double closingBalance = inventory[12] == null ? 0.0 : ((Double) inventory[12]);
			Double closingBalanceRate = 0.0;
			if (closingBalanceUnit != 0) {
				closingBalanceRate = closingBalance / closingBalanceUnit;
			}
			openBalanceDti.setOpeningBalQty(openBalanceUnit);
			openBalanceDti.setOpeningBalRate(openBalanceRate);
			openBalanceDti.setOpeningBalValue(openBalance);

			openBalanceDti.setClosingBalQty(closingBalanceUnit);
			openBalanceDti.setClosingBalRate(closingBalanceRate);
			openBalanceDti.setClosingBalValue(closingBalance);
			openBalanceDti.setItemName("OpeningBalance");
		}
		return openBalanceDti;
	}

	private void getWacInventory(ObjectNode result, Long organId, Long branchid, Long itemid, Date fromDate,
			Date toDate, EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "WAC");
		}
		long txnid;
		Date date;
		Double oBalance = 0.0;
		Double oBalanceUnit = 0.0;
		Double oBalanceRate = 0.0;
		Double cBalanceUnit = 0.0;
		Double cBalanceRate = 0.0;
		Double cBalance = 0.0;
		Double buyUnit;
		Double buyAmount;
		Double buyRate;
		Double saleUnit;
		Double saleAmount;
		Double saleRate = 0.0;
		Double txnSalesAmt;
		String oBalanceRateStr = "";
		String cBalanceRateStr = "";

		ArrayNode detailedInvan = result.putArray("inventory");
		DisplayTradingInventory dti = getWacOpeningBalance(organId, branchid, itemid, fromDate, em);
		if (null != dti) {
			ObjectNode row = Json.newObject();
			row.put("date", "");
			row.put("txnRef", "");
			row.put("txnName", dti.getItemName());
			if (dti.getOpeningBalValue() != null) {
				row.put("openingBalance", IdosConstants.decimalFormat.format(dti.getOpeningBalValue()));
			} else {
				row.put("openingBalance", 0.00);
			}
			row.put("openingBalanceUnit", dti.getOpeningBalQty());
			if (dti.getOpeningBalRate() != null) {
				row.put("openingBalanceRate", IdosConstants.decimalFormat.format(dti.getOpeningBalRate()));
			} else {
				row.put("openingBalanceRate", 0.00);
			}
			row.put("buyUnit", "");
			row.put("buyRate", "");
			row.put("buyAmount", "");
			row.put("saleUnit", "");
			row.put("saleRate", "");
			row.put("saleAmount", "");
			row.put("closingUnit", dti.getClosingBalQty());
			if (dti.getClosingBalValue() != null) {
				row.put("closingAmount", IdosConstants.decimalFormat.format(dti.getClosingBalValue()));
			} else {
				row.put("closingAmount", 0.00);
			}
			if (dti.getClosingBalRate() != null) {
				row.put("closingRate", IdosConstants.decimalFormat.format(dti.getClosingBalRate()));
			} else {
				row.put("closingRate", 0.00);
			}
			row.put("txnSaleAmount", "");
			row.put("saleMarginAmount", "");
			row.put("saleMarginPercent", "");
			cBalanceUnit = dti.getClosingBalQty();
			cBalance = dti.getClosingBalValue();
			cBalanceRate = dti.getClosingBalRate();
			detailedInvan.add(row);
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "HQL: " + BRANCH_TXN_SQL);
		}
		Query query = em.createQuery(BRANCH_TXN_SQL);
		query.setParameter(1, organId);
		query.setParameter(2, branchid);
		query.setParameter(3, itemid);
		query.setParameter(4, itemid);
		query.setParameter(5, fromDate);
		query.setParameter(6, toDate);
		query.setParameter(7, organId);
		query.setParameter(8, branchid);
		query.setParameter(9, itemid);
		query.setParameter(10, itemid);
		query.setParameter(11, fromDate);
		query.setParameter(12, toDate);

		List<Object[]> itemList = query.getResultList();

		for (Object[] inventory : itemList) {
			ObjectNode row = Json.newObject();
			txnid = Long.parseLong(((BigInteger) inventory[0]).toString());
			int txnType = inventory[15] == null ? 0 : (Integer.parseInt(inventory[15].toString()));
			String priceChangedTxn = (String) inventory[17];
			long txnPurpose = 0;
			String txnPurposeTxt = "";
			String txnRef = "";
			if (txnType == IdosConstants.TRADING_INV_PJE_EXP || txnType == IdosConstants.TRADING_INV_PJE_EXP_CREDIT
					|| txnType == IdosConstants.TRADING_INV_PJE_INC
					|| txnType == IdosConstants.TRADING_INV_PJE_INC_DEBIT) {
				IdosProvisionJournalEntry txnPje = IdosProvisionJournalEntry.findById(txnid);
				txnPurpose = txnPje.getTransactionPurpose().getId();
				txnRef = txnPje.getTransactionRefNumber();
				txnPurposeTxt = txnPje.getTransactionPurpose().getTransactionPurpose();
			} else {
				Transaction txn = Transaction.findById(txnid);
				txnPurpose = txn.getTransactionPurpose().getId();
				txnRef = txn.getTransactionRefNumber();
				txnPurposeTxt = txn.getTransactionPurpose().getTransactionPurpose();
				if (priceChangedTxn != null) {
					txnPurposeTxt = txnPurposeTxt + "*";
				}
			}
			date = (java.util.Date) inventory[1];
			oBalanceUnit += cBalanceUnit;
			// oBalanceUnit = cBalanceUnit;
			oBalance = cBalance;
			oBalanceRate = cBalanceRate;

			if (oBalanceRate > 0) {
				oBalanceRateStr = IdosConstants.decimalFormat.format(oBalanceRate);
			}

			buyUnit = inventory[5] == null ? 0.0 : ((Double) inventory[5]);
			buyRate = inventory[6] == null ? 0.0 : ((Double) inventory[6]);
			buyAmount = inventory[7] == null ? 0.0 : ((Double) inventory[7]);
			saleUnit = inventory[8] == null ? 0.0 : ((Double) inventory[8]);
			saleRate = inventory[9] == null ? 0.0 : ((Double) inventory[9]);
			saleAmount = inventory[10] == null ? 0.0 : ((Double) inventory[10]);
			cBalanceUnit += inventory[11] == null ? 0.0 : ((Double) inventory[11]);
			cBalanceRate = inventory[12] == null ? 0.0 : ((Double) inventory[12]);
			double txnClosingBalance = inventory[13] == null ? 0.0 : ((Double) inventory[13]);
			if (txnClosingBalance < 0 && txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER) {
				cBalance += (txnClosingBalance * -1);
			} else {
				cBalance += txnClosingBalance;
			}

			if (cBalanceUnit != 0) {
				cBalanceRate = cBalance / cBalanceUnit;
				cBalanceRateStr = IdosConstants.decimalFormat.format(cBalanceRate);
			}

			row.put("date", IdosConstants.MYSQLDF.format(date));
			row.put("txnRef", txnRef);
			row.put("txnName", txnPurposeTxt);
			row.put("openingBalance", IdosConstants.decimalFormat.format(oBalance));
			row.put("openingBalanceRate", oBalanceRateStr);
			row.put("openingBalanceUnit", oBalanceUnit);
			if (txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
					|| txnPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
					|| txnPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
					|| txnPurpose == IdosConstants.CREDIT_NOTE_VENDOR || txnPurpose == IdosConstants.DEBIT_NOTE_VENDOR
					|| txnType == IdosConstants.TRADING_INV_PJE_EXP
					|| txnType == IdosConstants.TRADING_INV_PJE_EXP_CREDIT
					|| (txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
							&& txnType == IdosConstants.TRADING_INV_BUY)) {
				row.put("buyUnit", buyUnit);
				row.put("buyRate", IdosConstants.decimalFormat.format(buyRate));
				row.put("buyAmount", IdosConstants.decimalFormat.format(buyAmount));
			} else {
				row.put("buyUnit", "");
				row.put("buyRate", "");
				row.put("buyAmount", "");
			}

			if (txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
					|| txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
					|| txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER
					|| txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER || txnType == IdosConstants.TRADING_INV_PJE_INC
					|| txnType == IdosConstants.TRADING_INV_PJE_INC_DEBIT
					|| (txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
							&& txnType == IdosConstants.TRADING_INV_SELL)) {
				row.put("saleUnit", saleUnit);
				// saleRate = (oBalance+buyAmount-saleAmount)/(oBalanceUnit + buyUnit -
				// saleUnit);
				// String saleRateStr = IdosConstants.decimalFormat.format(saleRate);
				row.put("saleRate", saleRate);
				row.put("saleAmount", IdosConstants.decimalFormat.format(saleAmount));
			} else {
				row.put("saleUnit", "");
				row.put("saleRate", "");
				row.put("saleAmount", "");
			}

			row.put("closingUnit", cBalanceUnit);
			row.put("closingRate", cBalanceRateStr);
			row.put("closingAmount", IdosConstants.decimalFormat.format(cBalance));

			if (inventory[14] != null) {
				txnSalesAmt = (Double) inventory[14];
				row.put("txnSaleAmount", IdosConstants.decimalFormat.format(txnSalesAmt));
				Double salesMargin = txnSalesAmt - saleAmount;
				row.put("saleMarginAmount", IdosConstants.decimalFormat.format(salesMargin));
				if (txnSalesAmt != 0) {
					row.put("saleMarginPercent", IdosConstants.decimalFormat.format(salesMargin / txnSalesAmt));
				} else {
					row.put("txnSaleAmount", "");
					row.put("saleMarginAmount", "");
					row.put("saleMarginPercent", "");
				}
			} else {
				row.put("txnSaleAmount", "");
				row.put("saleMarginAmount", "");
				row.put("saleMarginPercent", "");
			}
			detailedInvan.add(row);
			oBalanceUnit = 0.0;
		}

		ObjectNode row = Json.newObject();
		row.put("date", "");
		row.put("txnRef", "");
		row.put("txnName", "");
		row.put("openingBalance", IdosConstants.decimalFormat.format(cBalance));
		row.put("openingBalanceUnit", cBalanceUnit);
		row.put("openingBalanceRate", cBalanceRateStr);
		row.put("buyUnit", "");
		row.put("buyRate", "");
		row.put("buyAmount", "");
		row.put("saleUnit", "");
		row.put("saleRate", "");
		row.put("saleAmount", "");
		row.put("closingUnit", cBalanceUnit);
		row.put("closingAmount", IdosConstants.decimalFormat.format(cBalance));
		row.put("closingRate", cBalanceRateStr);
		row.put("txnSaleAmount", "");
		row.put("saleMarginAmount", "");
		row.put("saleMarginPercent", "");
		detailedInvan.add(row);
		result.put("status", true);
	}

	// Commented
	private void getWacInventory2(ObjectNode result, Long organId, Long branchid, Long itemid, Date fromDate,
			Date toDate, EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "WAC");
		}
		long txnid;
		Date date;
		Double oBalance = 0.0;
		Double oBalanceUnit = 0.0;
		Double oBalanceRate = 0.0;
		Double cBalanceUnit = 0.0;
		Double cBalanceRate = 0.0;
		Double cBalance = 0.0;
		Double buyUnit;
		Double buyAmount;
		Double buyRate;
		Double saleUnit;
		Double saleAmount;
		Double saleRate = 0.0;
		Double txnSalesAmt;
		String oBalanceRateStr = "";
		String cBalanceRateStr = "";

		ArrayNode detailedInvan = result.putArray("inventory");
		DisplayTradingInventory dti = getWacOpeningBalance(organId, branchid, itemid, fromDate, em);
		if (null != dti) {
			ObjectNode row = Json.newObject();
			row.put("date", "");
			row.put("txnRef", "");
			row.put("txnName", dti.getItemName());
			if (dti.getOpeningBalValue() != null) {
				row.put("openingBalance", IdosConstants.decimalFormat.format(dti.getOpeningBalValue()));
			} else {
				row.put("openingBalance", 0.00);
			}
			row.put("openingBalanceUnit", dti.getOpeningBalQty());
			if (dti.getOpeningBalRate() != null) {
				row.put("openingBalanceRate", IdosConstants.decimalFormat.format(dti.getOpeningBalRate()));
			} else {
				row.put("openingBalanceRate", 0.00);
			}
			row.put("buyUnit", "");
			row.put("buyRate", "");
			row.put("buyAmount", "");
			row.put("saleUnit", "");
			row.put("saleRate", "");
			row.put("saleAmount", "");
			row.put("closingUnit", dti.getClosingBalQty());
			if (dti.getClosingBalValue() != null) {
				row.put("closingAmount", IdosConstants.decimalFormat.format(dti.getClosingBalValue()));
			} else {
				row.put("closingAmount", 0.00);
			}
			if (dti.getClosingBalRate() != null) {
				row.put("closingRate", IdosConstants.decimalFormat.format(dti.getClosingBalRate()));
			} else {
				row.put("closingRate", 0.00);
			}
			row.put("txnSaleAmount", "");
			row.put("saleMarginAmount", "");
			row.put("saleMarginPercent", "");
			cBalanceUnit = dti.getClosingBalQty();
			cBalance = dti.getClosingBalValue();
			cBalanceRate = dti.getClosingBalRate();
			detailedInvan.add(row);
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "HQL: " + BRANCH_TXN_SQL);
		}
		Query query = em.createQuery(BRANCH_TXN_SQL);
		query.setParameter(1, organId);
		query.setParameter(2, branchid);
		query.setParameter(3, itemid);
		query.setParameter(4, itemid);
		query.setParameter(5, fromDate);
		query.setParameter(6, toDate);
		query.setParameter(7, organId);
		query.setParameter(8, branchid);
		query.setParameter(9, itemid);
		query.setParameter(10, itemid);
		query.setParameter(11, fromDate);
		query.setParameter(12, toDate);

		List<Object[]> itemList = query.getResultList();

		for (Object[] inventory : itemList) {
			ObjectNode row = Json.newObject();
			txnid = Long.parseLong(((BigInteger) inventory[0]).toString());
			int txnType = inventory[15] == null ? 0 : (Integer.parseInt(inventory[15].toString()));
			Transaction txn = null;
			IdosProvisionJournalEntry txnPje = null;
			long txnPurpose = 0;
			String txnPurposeTxt = "";
			String txnRef = "";
			if (txnType == IdosConstants.TRADING_INV_PJE_EXP || txnType == IdosConstants.TRADING_INV_PJE_INC) {
				txnPje = IdosProvisionJournalEntry.findById(txnid);
				txnPurpose = txnPje.getTransactionPurpose().getId();
				txnRef = txnPje.getTransactionRefNumber();
				txnPurposeTxt = txnPje.getTransactionPurpose().getTransactionPurpose();
			} else {
				txn = Transaction.findById(txnid);
				txnPurpose = txn.getTransactionPurpose().getId();
				txnRef = txn.getTransactionRefNumber();
				txnPurposeTxt = txn.getTransactionPurpose().getTransactionPurpose();
			}
			date = (java.util.Date) inventory[1];

			oBalanceUnit += cBalanceUnit;
			// oBalanceUnit = cBalanceUnit;
			oBalance = cBalance;
			oBalanceRate = cBalanceRate;

			if (oBalanceRate > 0) {
				oBalanceRateStr = IdosConstants.decimalFormat.format(oBalanceRate);
			}

			buyUnit = inventory[5] == null ? 0.0 : ((Double) inventory[5]);
			buyRate = inventory[6] == null ? 0.0 : ((Double) inventory[6]);
			buyAmount = inventory[7] == null ? 0.0 : ((Double) inventory[7]);
			saleUnit = inventory[8] == null ? 0.0 : ((Double) inventory[8]);
			saleRate = inventory[9] == null ? 0.0 : ((Double) inventory[9]);
			saleAmount = inventory[10] == null ? 0.0 : ((Double) inventory[10]);
			cBalanceUnit += inventory[11] == null ? 0.0 : ((Double) inventory[11]);
			cBalanceRate = inventory[12] == null ? 0.0 : ((Double) inventory[12]);
			cBalance += inventory[13] == null ? 0.0 : ((Double) inventory[13]);

			if (cBalanceUnit != 0) {
				cBalanceRate = cBalance / cBalanceUnit;
				cBalanceRateStr = IdosConstants.decimalFormat.format(cBalanceRate);
			}

			row.put("date", IdosConstants.MYSQLDF.format(date));
			row.put("txnRef", txnRef);
			row.put("txnName", txnPurposeTxt);
			row.put("openingBalance", IdosConstants.decimalFormat.format(oBalance));
			row.put("openingBalanceRate", oBalanceRateStr);
			row.put("openingBalanceUnit", oBalanceUnit);
			if (txnPurpose == IdosConstants.BUY_ON_CASH_PAY_RIGHT_AWAY
					|| txnPurpose == IdosConstants.BUY_ON_CREDIT_PAY_LATER
					|| txnPurpose == IdosConstants.BUY_ON_PETTY_CASH_ACCOUNT
					|| txnPurpose == IdosConstants.CREDIT_NOTE_VENDOR || txnPurpose == IdosConstants.DEBIT_NOTE_VENDOR
					|| txnType == IdosConstants.TRADING_INV_PJE_EXP
					|| (txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
							&& txnType == IdosConstants.TRADING_INV_BUY)) {
				row.put("buyUnit", buyUnit);
				row.put("buyRate", IdosConstants.decimalFormat.format(buyRate));
				row.put("buyAmount", IdosConstants.decimalFormat.format(buyAmount));
			} else {
				row.put("buyUnit", "");
				row.put("buyRate", "");
				row.put("buyAmount", "");
			}

			if (txnPurpose == IdosConstants.SELL_ON_CREDIT_COLLECT_PAYMENT_LATER
					|| txnPurpose == IdosConstants.SELL_ON_CASH_COLLECT_PAYMENT_NOW
					|| txnPurpose == IdosConstants.CREDIT_NOTE_CUSTOMER
					|| txnPurpose == IdosConstants.DEBIT_NOTE_CUSTOMER || txnType == IdosConstants.TRADING_INV_PJE_INC
					|| (txnPurpose == IdosConstants.TRANSFER_INVENTORY_ITEM_FROM_ONE_BRANCH_TO_ANOTHER
							&& txnType == IdosConstants.TRADING_INV_SELL)) {
				row.put("saleUnit", saleUnit);
				// saleRate = (oBalance+buyAmount-saleAmount)/(oBalanceUnit + buyUnit -
				// saleUnit);
				// String saleRateStr = IdosConstants.decimalFormat.format(saleRate);
				row.put("saleRate", saleRate);
				row.put("saleAmount", IdosConstants.decimalFormat.format(saleAmount));
			} else {
				row.put("saleUnit", "");
				row.put("saleRate", "");
				row.put("saleAmount", "");
			}
			row.put("closingUnit", cBalanceUnit);
			row.put("closingRate", cBalanceRateStr);
			row.put("closingAmount", IdosConstants.decimalFormat.format(cBalance));
			if (inventory[14] != null) {
				txnSalesAmt = (Double) inventory[14];
				row.put("txnSaleAmount", IdosConstants.decimalFormat.format(txnSalesAmt));
				Double salesMargin = txnSalesAmt - saleAmount;
				row.put("saleMarginAmount", IdosConstants.decimalFormat.format(salesMargin));
				if (txnSalesAmt != 0) {
					row.put("saleMarginPercent", IdosConstants.decimalFormat.format(salesMargin / txnSalesAmt));
				} else {
					row.put("txnSaleAmount", "");
					row.put("saleMarginAmount", "");
					row.put("saleMarginPercent", "");
				}
			} else {
				row.put("txnSaleAmount", "");
				row.put("saleMarginAmount", "");
				row.put("saleMarginPercent", "");
			}
			detailedInvan.add(row);
			// oBalanceUnit=0.0;
		}

		ObjectNode row = Json.newObject();
		row.put("date", "");
		row.put("txnRef", "");
		row.put("txnName", "");
		row.put("openingBalance", IdosConstants.decimalFormat.format(cBalance));
		row.put("openingBalanceUnit", cBalanceUnit);
		row.put("openingBalanceRate", cBalanceRateStr);
		row.put("buyUnit", "");
		row.put("buyRate", "");
		row.put("buyAmount", "");
		row.put("saleUnit", "");
		row.put("saleRate", "");
		row.put("saleAmount", "");
		row.put("closingUnit", cBalanceUnit);
		row.put("closingAmount", IdosConstants.decimalFormat.format(cBalance));
		row.put("closingRate", cBalanceRateStr);
		row.put("txnSaleAmount", "");
		row.put("saleMarginAmount", "");
		row.put("saleMarginPercent", "");
		detailedInvan.add(row);
		result.put("status", true);

	}

	private DisplayTradingInventory getFifoOpeningBalance(Long organId, Long branchid, Long itemid, Date fromDate,
			EntityManager em) {
		DisplayTradingInventory openBalanceDti = null;
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "********* Start HQL: " + FIFO_OB_TXN_SQL);
		}
		Query query = em.createNativeQuery(FIFO_OB_TXN_SQL);
		query.setParameter(1, organId);
		query.setParameter(2, branchid);
		query.setParameter(3, itemid);
		query.setParameter(4, organId);
		query.setParameter(5, branchid);
		query.setParameter(6, itemid);
		query.setParameter(7, fromDate);
		query.setParameter(8, organId);
		query.setParameter(9, branchid);
		query.setParameter(10, itemid);
		query.setParameter(11, fromDate);
		// query.setParameter(12, organId); query.setParameter(13, branchid);
		// query.setParameter(14, itemid); query.setParameter(15, fromDate);
		// query.setParameter(16, organId); query.setParameter(17, branchid);
		// query.setParameter(18, itemid); query.setParameter(19, fromDate);

		List<Object[]> openList = query.getResultList();
		if (openList.size() > 0) {
			openBalanceDti = new DisplayTradingInventory();
			Object[] inventory = openList.get(0);
			Double openBalanceUnit = inventory[0] == null ? 0.0 : ((Double) inventory[0]);
			Double rate = inventory[1] == null ? 0.0 : ((Double) inventory[1]);
			Double amount = inventory[2] == null ? 0.0 : ((Double) inventory[2]);

			Double openBalanceRate = rate;
			Double openBalance = amount;
			openBalanceDti.setOpeningBalQty(openBalanceUnit);
			openBalanceDti.setOpeningBalRate(openBalanceRate);
			openBalanceDti.setOpeningBalValue(openBalance);
			openBalanceDti.setClosingBalQty(openBalanceUnit);
			openBalanceDti.setClosingBalRate(openBalanceRate);
			openBalanceDti.setClosingBalValue(openBalance);
			openBalanceDti.setItemName("OpeningBalance");
		}
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "********* End  " + openBalanceDti);
		}
		return openBalanceDti;
	}

	/**
	 *
	 * @param result
	 * @param itemList
	 */
	private void getFifoInventory(ObjectNode result, Long organId, Long branchid, Long itemid, Date fromDate,
			Date toDate, EntityManager em) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "FIFO");
		}
		long txnid;
		Date date;
		double openBalance;
		double openBalanceUnit;
		double openBalanceRate;
		// Double closingBalance =0.0; Double closingBalanceUnit=0.0; Double
		// closingBalanceRate=0.0;
		double oBalance;
		double oBalanceUnit;
		double oBalanceRate;
		double buyUnit;
		double buyAmount;
		double buyRate;
		double saleUnit;
		double saleAmount;
		double saleRate = 0.0;
		double cBalance = 0.0;
		double cBalanceUnit = 0.0;
		double cBalanceRate = 0.0;
		double txnSalesAmt;
		String txnName;
		CopyOnWriteArrayList<DisplayTradingInventory> ocList = new CopyOnWriteArrayList<>();
		ArrayNode detailedInvan = result.putArray("inventory");
		DisplayTradingInventory dti = getFifoOpeningBalance(organId, branchid, itemid, fromDate, em);
		if (null != dti) {
			ObjectNode row = Json.newObject();
			row.put("date", "");
			row.put("txnRef", "");
			row.put("txnName", dti.getItemName());
			row.put("openingBalance", IdosConstants.decimalFormat.format(dti.getOpeningBalValue()));
			row.put("openingBalanceUnit", dti.getOpeningBalQty());
			row.put("openingBalanceRate", IdosConstants.decimalFormat.format(dti.getOpeningBalRate()));
			row.put("buyUnit", "");
			row.put("buyRate", "");
			row.put("buyAmount", "");
			row.put("saleUnit", "");
			row.put("saleRate", "");
			row.put("saleAmount", "");
			row.put("closingUnit", dti.getClosingBalQty());
			row.put("closingAmount", IdosConstants.decimalFormat.format(dti.getClosingBalValue()));
			row.put("closingRate", IdosConstants.decimalFormat.format(dti.getClosingBalRate()));
			row.put("txnSaleAmount", "");
			row.put("saleMarginAmount", "");
			row.put("saleMarginPercent", "");
			cBalanceUnit = dti.getClosingBalQty();
			cBalance = dti.getClosingBalValue();
			cBalanceRate = dti.getClosingBalRate();
			ocList.add(dti);
			detailedInvan.add(row);
		}
		ArrayList inparams = new ArrayList();
		inparams.add(organId);
		inparams.add(branchid);
		inparams.add(itemid);
		inparams.add(itemid);
		inparams.add(fromDate);
		inparams.add(toDate);
		List<TradingInventory> itemList = genericDao.queryWithParams(FIFO_TXN_JPQL, em, inparams);
		long prevTxnId = 0L;
		for (TradingInventory inventory : itemList) {
			oBalanceUnit = cBalanceUnit;
			oBalance = cBalance;
			oBalanceRate = cBalanceRate;
			ObjectNode row = Json.newObject();
			boolean isSale = false;
			String txnRef = "";
			txnid = inventory.getTransactionId();
			if (prevTxnId == txnid) {
				continue;
			}
			if (inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_EXP
					|| inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_EXP_CREDIT
					|| inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_INC
					|| inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_INC_DEBIT) {
				IdosProvisionJournalEntry txn = IdosProvisionJournalEntry.findById(txnid);
				txnName = txn.getTransactionPurpose().getTransactionPurpose();
				txnRef = txn.getTransactionRefNumber();
			} else {
				Transaction txn = Transaction.findById(txnid);
				txnName = txn.getTransactionPurpose().getTransactionPurpose();
				txnRef = txn.getTransactionRefNumber();
				if (inventory.getPriceChangedTxn() != null) {
					txnName = txnName + "*";
				}
			}
			date = inventory.getDate();
			buyUnit = inventory.getNoOfExpUnitsConvertedToIncUnits() == null ? 0.0
					: inventory.getNoOfExpUnitsConvertedToIncUnits();
			buyRate = inventory.getCalcualtedRate();
			buyAmount = inventory.getGrossValue();
			saleUnit = inventory.getTotalQuantity();
			saleRate = inventory.getCalcualtedRate();
			saleAmount = inventory.getGrossValue();

			cBalanceRate = inventory.getCalcualtedRate();
			Double itemGrossAmount = inventory.getTransactionGorss();

			row.put("date", IdosConstants.MYSQLDF.format(date));
			row.put("txnRef", txnRef);
			row.put("txnName", txnName);
			row.put("openingBalance", "");
			row.put("openingBalanceRate", "");
			row.put("openingBalanceUnit", "");
			if (inventory.getTransactionType() == IdosConstants.TRADING_INV_BUY
					|| inventory.getTransactionType() == IdosConstants.TRADING_INV_PURCHASE_RET
					|| inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_EXP
					|| inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_EXP_CREDIT) {
				row.put("buyUnit", buyUnit);
				row.put("buyAmount", IdosConstants.decimalFormat.format(buyAmount));
				row.put("buyRate", IdosConstants.decimalFormat.format(buyRate));
				cBalanceUnit = buyUnit;
				cBalanceRate = buyRate;
				cBalance = buyUnit * buyRate;
			} else {
				row.put("buyUnit", "");
				row.put("buyRate", "");
				row.put("buyAmount", "");
			}

			if (inventory.getTransactionType() == IdosConstants.TRADING_INV_SELL
					|| inventory.getTransactionType() == IdosConstants.TRADING_INV_SALES_RET
					|| inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_INC
					|| inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_INC_DEBIT) {
				row.put("saleUnit", saleUnit);
				row.put("saleRate", IdosConstants.decimalFormat.format(saleRate));
				row.put("saleAmount", IdosConstants.decimalFormat.format(saleAmount));
				isSale = true;
			} else {
				row.put("saleUnit", "");
				row.put("saleRate", "");
				row.put("saleAmount", "");
			}
			row.put("closingUnit", "");
			row.put("closingRate", "");
			row.put("closingAmount", "");

			if (itemGrossAmount != null && isSale) {
				txnSalesAmt = itemGrossAmount;
				double salesMargin = txnSalesAmt - saleAmount;
				if (txnSalesAmt != 0) {
					row.put("txnSaleAmount", IdosConstants.decimalFormat.format(txnSalesAmt));
					row.put("saleMarginAmount", IdosConstants.decimalFormat.format(salesMargin));
					row.put("saleMarginPercent", IdosConstants.decimalFormat.format(salesMargin / txnSalesAmt));
				} else {
					row.put("txnSaleAmount", "");
					row.put("saleMarginAmount", "");
					row.put("saleMarginPercent", "");
				}
			} else {
				row.put("txnSaleAmount", "");
				row.put("saleMarginAmount", "");
				row.put("saleMarginPercent", "");
			}

			if (isSale) {
				prevTxnId = txnid;
				for (DisplayTradingInventory openClosing : ocList) {
					ObjectNode rowoc = Json.newObject();
					rowoc.put("date", "");
					rowoc.put("txnRef", "#");
					rowoc.put("txnName", "");
					rowoc.put("openingBalance", IdosConstants.decimalFormat
							.format(openClosing.getOpeningBalRate() * openClosing.getOpeningBalQty()));
					rowoc.put("openingBalanceUnit", openClosing.getOpeningBalQty());
					rowoc.put("openingBalanceRate",
							IdosConstants.decimalFormat.format(openClosing.getOpeningBalRate()));
					rowoc.put("buyUnit", "");
					rowoc.put("buyRate", "");
					rowoc.put("buyAmount", "");
					rowoc.put("saleUnit", "");
					rowoc.put("saleRate", "");
					rowoc.put("saleAmount", "");
					rowoc.put("closingUnit", "");
					rowoc.put("closingAmount", "");
					rowoc.put("closingRate", "");
					rowoc.put("txnSaleAmount", "");
					rowoc.put("saleMarginAmount", "");
					rowoc.put("saleMarginPercent", "");
					detailedInvan.add(rowoc);
				}
				detailedInvan.add(row);

				List<TradingInventory> sellTxnInvList = TradingInventory.findSellInventory(em,
						inventory.getOrganization().getId(), inventory.getBranch().getId(),
						inventory.getBuySpecifics().getId(), IdosConstants.TRADING_INV_SELL, txnid);
				if (sellTxnInvList.size() > 1) {
					for (TradingInventory inventoryNxt : sellTxnInvList) {
						if (inventory.getId() == inventoryNxt.getId()) {
							adjustOpeningClosingFifo(ocList, saleUnit, row);
							continue;
						}
						saleUnit = inventoryNxt.getTotalQuantity();
						saleRate = inventoryNxt.getCalcualtedRate();
						saleAmount = inventoryNxt.getGrossValue();
						row = Json.newObject();
						row.put("date", "");
						row.put("txnRef", "");
						row.put("txnName", "");
						row.put("openingBalance", "");
						row.put("openingBalanceUnit", "");
						row.put("openingBalanceRate", "");
						row.put("buyUnit", "");
						row.put("buyRate", "");
						row.put("buyAmount", "");
						row.put("saleUnit", saleUnit);
						row.put("saleRate", IdosConstants.decimalFormat.format(saleRate));
						row.put("saleAmount", IdosConstants.decimalFormat.format(saleAmount));
						row.put("closingUnit", "");
						row.put("closingAmount", "");
						row.put("closingRate", "");
						row.put("txnSaleAmount", "");
						row.put("saleMarginAmount", "");
						row.put("saleMarginPercent", "");
						detailedInvan.add(row);
						adjustOpeningClosingFifo(ocList, saleUnit, row);
					}
				}

				boolean skipFirstClosingRow = false;
				if (inventory.getTransactionType() == IdosConstants.TRADING_INV_SALES_RET
						|| inventory.getTransactionType() == IdosConstants.TRADING_INV_PJE_INC_DEBIT) {
					DisplayTradingInventory dtinew = new DisplayTradingInventory();
					dtinew.setOpeningBalValue(saleAmount * -1);
					dtinew.setOpeningBalQty(saleUnit * -1);
					dtinew.setOpeningBalRate(saleRate);
					dtinew.setClosingBalValue(saleAmount * -1);
					dtinew.setClosingBalQty(saleUnit * -1);
					dtinew.setClosingBalRate(saleRate);
					if (ocList.size() > 0) {
						ocList.add(0, dtinew);
					}
					row.put("closingUnit", saleUnit * -1);
					row.put("closingRate", IdosConstants.decimalFormat.format(saleRate));
					row.put("closingAmount", IdosConstants.decimalFormat.format(saleAmount * -1));
					skipFirstClosingRow = true;
				}

				Iterator<DisplayTradingInventory> itr = ocList.iterator();
				DisplayTradingInventory openClosing = null;
				double saleUnitTmp = saleUnit;
				while (itr.hasNext()) {
					boolean isPrint = false;
					openClosing = itr.next();
					Double closingUnitTmp = openClosing.getClosingBalQty();
					if (saleUnitTmp > 0.0 && sellTxnInvList.size() <= 1) {
						if (closingUnitTmp > saleUnitTmp) {
							closingUnitTmp -= saleUnitTmp;
							openClosing.setClosingBalQty(closingUnitTmp);
							openClosing.setOpeningBalQty(closingUnitTmp);
							openClosing.setClosingBalValue(closingUnitTmp * openClosing.getClosingBalRate());
							isPrint = false;
							saleUnitTmp = 0.0;
							row.put("closingUnit", closingUnitTmp);
							row.put("closingRate", IdosConstants.decimalFormat.format(openClosing.getClosingBalRate()));
							row.put("closingAmount", IdosConstants.decimalFormat
									.format(openClosing.getClosingBalRate() * closingUnitTmp));
						} else if (closingUnitTmp == saleUnitTmp) {
							log.log(Level.FINE, "====" + saleUnitTmp + " = " + closingUnitTmp);
							ocList.remove(openClosing);
							saleUnitTmp = 0.0;
							isPrint = false;
						} else if (closingUnitTmp < saleUnitTmp) {
							saleUnitTmp -= closingUnitTmp;
							ocList.remove(openClosing);
							isPrint = false;
							// openClosing.setClosingBalQty(closingUnitTmp);
						}
					} else {
						if (skipFirstClosingRow) {
							isPrint = false;
						} else {
							isPrint = true;
						}
					}
					if (isPrint) {
						ObjectNode rowoc = Json.newObject();
						rowoc.put("date", "");
						rowoc.put("txnRef", "");
						rowoc.put("txnName", "");
						rowoc.put("openingBalance", "");
						rowoc.put("openingBalanceUnit", "");
						rowoc.put("openingBalanceRate", "");
						rowoc.put("buyUnit", "");
						rowoc.put("buyRate", "");
						rowoc.put("buyAmount", "");
						rowoc.put("saleUnit", "");
						rowoc.put("saleRate", "");
						rowoc.put("saleAmount", "");
						rowoc.put("closingUnit", closingUnitTmp);
						rowoc.put("closingAmount",
								IdosConstants.decimalFormat.format(openClosing.getClosingBalValue()));
						rowoc.put("closingRate", IdosConstants.decimalFormat.format(openClosing.getClosingBalRate()));
						rowoc.put("txnSaleAmount", "");
						rowoc.put("saleMarginAmount", "");
						rowoc.put("saleMarginPercent", "");
						detailedInvan.add(rowoc);
					}
					skipFirstClosingRow = false;
				}

			} else if (!isSale) {
				for (DisplayTradingInventory openClosing : ocList) {
					ObjectNode rowoc = Json.newObject();
					rowoc.put("date", "");
					rowoc.put("txnRef", "-");
					rowoc.put("txnName", "");
					rowoc.put("openingBalance", IdosConstants.decimalFormat
							.format(openClosing.getOpeningBalQty() * openClosing.getOpeningBalRate()));
					rowoc.put("openingBalanceUnit", openClosing.getOpeningBalQty());
					rowoc.put("openingBalanceRate",
							IdosConstants.decimalFormat.format(openClosing.getOpeningBalRate()));
					rowoc.put("buyUnit", "");
					rowoc.put("buyRate", "");
					rowoc.put("buyAmount", "");
					rowoc.put("saleUnit", "");
					rowoc.put("saleRate", "");
					rowoc.put("saleAmount", "");
					rowoc.put("closingUnit", openClosing.getClosingBalQty());
					rowoc.put("closingAmount", IdosConstants.decimalFormat
							.format((openClosing.getClosingBalRate() * openClosing.getClosingBalQty())));
					rowoc.put("closingRate", IdosConstants.decimalFormat.format(openClosing.getClosingBalRate()));
					rowoc.put("txnSaleAmount", "");
					rowoc.put("saleMarginAmount", "");
					rowoc.put("saleMarginPercent", "");
					detailedInvan.add(rowoc);
				}
				if (inventory.getTransactionType() != IdosConstants.TRADING_INV_PURCHASE_RET
						&& inventory.getTransactionType() != IdosConstants.TRADING_INV_PJE_EXP_CREDIT) {
					DisplayTradingInventory dtinew = new DisplayTradingInventory();
					dtinew.setOpeningBalValue(buyAmount);
					dtinew.setOpeningBalQty(buyUnit);
					dtinew.setOpeningBalRate(buyRate);
					dtinew.setClosingBalValue(buyAmount);
					dtinew.setClosingBalQty(buyUnit);
					dtinew.setClosingBalRate(buyRate);
					ocList.add(dtinew);
				}
				detailedInvan.add(row);
				for (DisplayTradingInventory openClosing : ocList) {
					ObjectNode rowoc = Json.newObject();
					rowoc.put("date", "");
					rowoc.put("txnRef", "$");
					rowoc.put("txnName", "");
					rowoc.put("openingBalance", "");
					rowoc.put("openingBalanceUnit", "");
					rowoc.put("openingBalanceRate", "");
					rowoc.put("buyUnit", "");
					rowoc.put("buyRate", "");
					rowoc.put("buyAmount", "");
					rowoc.put("saleUnit", "");
					rowoc.put("saleRate", "");
					rowoc.put("saleAmount", "");
					if (inventory.getTransactionType() == IdosConstants.TRADING_INV_PURCHASE_RET
							&& openClosing.getClosingBalRate() == buyRate) {
						rowoc.put("closingUnit", openClosing.getClosingBalQty() + buyUnit);
						rowoc.put("closingAmount", IdosConstants.decimalFormat.format(
								(openClosing.getClosingBalRate() * (openClosing.getClosingBalQty() + buyUnit))));
					} else {
						rowoc.put("closingUnit", openClosing.getClosingBalQty());
						rowoc.put("closingAmount", IdosConstants.decimalFormat
								.format((openClosing.getClosingBalRate() * openClosing.getClosingBalQty())));
					}
					rowoc.put("closingRate", IdosConstants.decimalFormat.format(openClosing.getClosingBalRate()));
					rowoc.put("txnSaleAmount", "");
					rowoc.put("saleMarginAmount", "");
					rowoc.put("saleMarginPercent", "");
					detailedInvan.add(rowoc);
				}
			}
		}
		result.put("status", true);
	}

	private boolean adjustOpeningClosingFifo(List<DisplayTradingInventory> ocList, double saleUnit, ObjectNode row) {
		Iterator<DisplayTradingInventory> itr = ocList.iterator();
		DisplayTradingInventory openClosing = null;
		double saleUnitTmp = saleUnit;
		boolean isPrint = false;
		while (itr.hasNext()) {
			isPrint = false;
			openClosing = itr.next();
			Double closingUnitTmp = openClosing.getClosingBalQty();
			if (saleUnitTmp > 0.0) {
				if (closingUnitTmp > saleUnitTmp) {
					closingUnitTmp -= saleUnitTmp;
					openClosing.setClosingBalQty(closingUnitTmp);
					openClosing.setOpeningBalQty(closingUnitTmp);
					openClosing.setClosingBalValue(closingUnitTmp * openClosing.getClosingBalRate());
					isPrint = false;
					saleUnitTmp = 0.0;
					/*
					 * row.put("closingUnit", closingUnitTmp);
					 * row.put("closingRate",
					 * IdosConstants.decimalFormat.format(openClosing.getClosingBalRate()));
					 * row.put("closingAmount",
					 * IdosConstants.decimalFormat.format(openClosing.getClosingBalRate() *
					 * closingUnitTmp));
					 */
				} else if (closingUnitTmp == saleUnitTmp) {
					log.log(Level.FINE, "====" + saleUnitTmp + " = " + closingUnitTmp);
					ocList.remove(openClosing);
					saleUnitTmp = 0.0;
					isPrint = false;
				} else if (closingUnitTmp < saleUnitTmp) {
					saleUnitTmp -= closingUnitTmp;
					ocList.remove(openClosing);
					isPrint = false;
					// openClosing.setClosingBalQty(closingUnitTmp);
				}
			}
		}
		return isPrint;
	}

	@Override
	public TradingInventory getClosingInventory(long orgid, long branchid, long specificid, Date date,
			EntityManager em) {
		String sbr = "select obj from TradingInventory obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.transactionType=4 and obj.transactionSpecifics.id=?3 and obj.presentStatus=1 and ( obj.date <= ?4 or obj.transactionId is NULL) order by obj.date desc, obj.createdAt desc";
		ArrayList inparams = new ArrayList(4);
		inparams.add(orgid);
		inparams.add(branchid);
		inparams.add(specificid);
		inparams.add(date);
		List<TradingInventory> inventories = genericDao.queryWithParams(sbr, em, inparams);
		TradingInventory inventory = null;
		if (inventories != null && !inventories.isEmpty()) {
			inventory = inventories.get(0);
		}
		return inventory;
	}

	/*
	 * not in use
	 * private void saveIncomeInventoryCredit(IdosProvisionJournalEntry transaction,
	 * Users user, EntityManager entityManager, double purchaseReturnQty, Branch
	 * branch, Specifics sellSpecific, Specifics buySpecific) {
	 * try {
	 * String tradInvCalcMethod = buySpecific.getTradingInventoryCalcMethod();
	 * double qtyConvertedToIncomeUnit = purchaseReturnQty *
	 * buySpecific.getExpenseToIncomeConverstionRate();
	 * if (tradInvCalcMethod.equalsIgnoreCase("FIFO")) {
	 * //get all buy trades, so that we can get rate for those to calculate gross
	 * for purchase return transactions
	 * StringBuffer sbr = new
	 * StringBuffer("select obj from TradingInventory obj where (obj.transactionType='1' or obj.transactionType='3') and obj.organization='"
	 * + user.getOrganization().getId() + "' and obj.branch='" + branch.getId() +
	 * "' and obj.transactionSpecifics='" + buySpecific.getId() +
	 * "' and obj.quantityMatchedWithSell < obj.noOfExpUnitsConvertedToIncUnits and obj.date  < '"
	 * + transaction.getTransactionDate() + "'");
	 * List<TradingInventory> buyTrades =
	 * genericDao.executeSimpleQuery(sbr.toString(), entityManager);
	 * Double purRetQty = qtyConvertedToIncomeUnit;
	 * double purRetQtyRemaining = purRetQty;
	 * 
	 * if (buyTrades != null && buyTrades.size() > 0) {
	 * if (purRetQtyRemaining > 0) {
	 * for (TradingInventory buytrade : buyTrades) {
	 * String linkedBuyTrades = "";
	 * double buyQty = buytrade.getNoOfExpUnitsConvertedToIncUnits();
	 * double buyQtyAdjustedWithSellQty = buytrade.getQuantityMatchedWithSell();
	 * double remainingBuyQuantity = 0.0;
	 * double purRetQtyMatched = 0.0;
	 * double purRetQtyGross = 0.0;
	 * if (buyQtyAdjustedWithSellQty < buyQty) {
	 * remainingBuyQuantity = buyQty - buyQtyAdjustedWithSellQty;
	 * if (purRetQtyRemaining <= remainingBuyQuantity) {
	 * purRetQtyGross = (buytrade.getCalcualtedRate()) * purRetQtyRemaining;
	 * purRetQtyMatched = purRetQtyRemaining;
	 * buytrade.setQuantityMatchedWithSell(buytrade.getQuantityMatchedWithSell() +
	 * purRetQtyRemaining);
	 * purRetQtyRemaining = 0;
	 * } else {
	 * purRetQtyGross = buytrade.getCalcualtedRate() * remainingBuyQuantity;
	 * purRetQtyMatched = remainingBuyQuantity;
	 * buytrade.setQuantityMatchedWithSell(buytrade.getQuantityMatchedWithSell() +
	 * remainingBuyQuantity);
	 * purRetQtyRemaining = purRetQtyRemaining - remainingBuyQuantity;
	 * }
	 * linkedBuyTrades = linkedBuyTrades + buytrade.getId() + ",";
	 * genericDao.saveOrUpdate(buytrade, user, entityManager);
	 * TradingInventory tradingInv = new TradingInventory();
	 * tradingInv.setTransactionType(IdosConstants.TRADING_INV_PURCHASE_RET);
	 * tradingInv.setTransactionId(transaction.getId());
	 * tradingInv.setDate(transaction.getTransactionDate());
	 * tradingInv.setBranch(branch);
	 * tradingInv.setOrganization(transaction.getProvisionMadeForOrganization());
	 * tradingInv.setUser(user);
	 * tradingInv.setTransactionSpecifics(buySpecific);
	 * tradingInv.setTotalQuantity(purRetQtyMatched * -1);
	 * tradingInv.setNoOfExpUnitsConvertedToIncUnits(purRetQtyMatched * -1); //need
	 * this when showing consolidated report
	 * tradingInv.setCalcualtedRate(buytrade.getCalcualtedRate());
	 * tradingInv.setLinkedBuyIds(linkedBuyTrades);
	 * tradingInv.setGrossValue(purRetQtyGross * -1);
	 * genericDao.saveOrUpdate(tradingInv, user, entityManager); //save multiple
	 * sell trades in TradingInventory as it matches with buy for single sell
	 * transaction
	 * if (purRetQtyRemaining <= 0) {
	 * break;
	 * }
	 * }
	 * }
	 * }
	 * }
	 * } else if (tradInvCalcMethod.equalsIgnoreCase("WAC")) {
	 * //get last closing transaction
	 * StringBuffer sbr = new
	 * StringBuffer("select obj from TradingInventory obj where obj.transactionType=4 and obj.transactionSpecifics.id='"
	 * + buySpecific.getId() + "' and obj.organization='" +
	 * user.getOrganization().getId() + "' and obj.branch='" + branch.getId() +
	 * "' order by obj.createdAt desc");
	 * List<TradingInventory> tradingInvList =
	 * genericDao.executeSimpleQueryWithLimit(sbr.toString(), entityManager, 1);
	 * if (tradingInvList != null && tradingInvList.size() > 0) {
	 * TradingInventory inv = (TradingInventory) tradingInvList.get(0);
	 * double lastclosingconvertedqty = inv.getNoOfExpUnitsConvertedToIncUnits();
	 * double lastclosingrate = inv.getCalcualtedRate();
	 * double lastclosinggrossval = inv.getGrossValue();
	 * 
	 * //for sell trade - use qty of sell transaction * last closing rate = gross
	 * for sell
	 * double purRetQty = qtyConvertedToIncomeUnit;
	 * double purRetGross = purRetQty * lastclosingrate;
	 * TradingInventory tradingInv = new TradingInventory();
	 * tradingInv.setTransactionId(transaction.getId());
	 * tradingInv.setDate(transaction.getTransactionDate());
	 * tradingInv.setBranch(branch);
	 * tradingInv.setOrganization(transaction.getProvisionMadeForOrganization());
	 * tradingInv.setUser(user);
	 * tradingInv.setTransactionSpecifics(buySpecific);
	 * 
	 * tradingInv.setTransactionType(IdosConstants.TRADING_INV_PURCHASE_RET);
	 * //buy=1, sell=2, opening=3, closing=4
	 * tradingInv.setTotalQuantity(purRetQty * -1);
	 * tradingInv.setNoOfExpUnitsConvertedToIncUnits(purRetQty * -1);
	 * tradingInv.setGrossValue(purRetGross * -1);
	 * tradingInv.setCalcualtedRate(lastclosingrate);
	 * genericDao.saveOrUpdate(tradingInv, user, entityManager); //save sell trade
	 * 
	 * 
	 * double newclosingconvertedqty = lastclosingconvertedqty - purRetQty;
	 * double newclosinggrossval = lastclosinggrossval - purRetGross;
	 * double newclosingrate =
	 * Double.parseDouble(IdosConstants.decimalFormat.format(newclosinggrossval /
	 * newclosingconvertedqty));
	 * TradingInventory tradingInvclosing = new TradingInventory();
	 * tradingInvclosing.setTransactionId(transaction.getId());
	 * tradingInvclosing.setDate(transaction.getTransactionDate());
	 * tradingInvclosing.setBranch(branch);
	 * tradingInvclosing.setOrganization(transaction.getProvisionMadeForOrganization
	 * ());
	 * tradingInvclosing.setUser(user);
	 * tradingInvclosing.setTransactionSpecifics(buySpecific);
	 * tradingInvclosing.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL);
	 * tradingInvclosing.setGrossValue(newclosinggrossval);
	 * tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(newclosingconvertedqty);
	 * tradingInvclosing.setCalcualtedRate(newclosingrate);
	 * genericDao.saveOrUpdate(tradingInvclosing, user, entityManager);
	 * }
	 * }
	 * } catch (Exception ex) {
	 * log.log(Level.SEVERE, "Error", ex);
	 * }
	 * }
	 * 
	 * 
	 * private void saveIncomeInventoryDebit(IdosProvisionJournalEntry transaction,
	 * Users user, EntityManager entityManager, double sellReturnQty, Branch branch,
	 * Specifics sellSpecific, Specifics buySpecifics) {
	 * try {
	 * String tradInvCalcMethod = sellSpecific.getTradingInventoryCalcMethod();
	 * if (tradInvCalcMethod.equalsIgnoreCase("FIFO") ||
	 * tradInvCalcMethod.equalsIgnoreCase("WAC")) {
	 * //get all sell trades, so that we can get rate for those to calculate gross
	 * for sales return transactions
	 * StringBuffer sbr = new
	 * StringBuffer("select obj from TradingInventory obj where obj.transactionType='2' and obj.organization='"
	 * + user.getOrganization().getId() + "' and obj.branch='" + branch.getId() +
	 * "' and obj.transactionSpecifics='" + sellSpecific.getId() +
	 * "' and obj.quantityMatchedWithSell < obj.totalQuantity and obj.date  < '" +
	 * transaction.getTransactionDate() + "'");
	 * List<TradingInventory> sellTrades =
	 * genericDao.executeSimpleQuery(sbr.toString(), entityManager);
	 * Double salesRetQty = sellReturnQty;
	 * double salesRetQtyRemaining = salesRetQty;
	 * 
	 * if (sellTrades != null && sellTrades.size() > 0) {
	 * if (salesRetQtyRemaining > 0) {
	 * for (TradingInventory selltrade : sellTrades) {
	 * String linkedBuyTrades = "";
	 * double sellQty = selltrade.getTotalQuantity();
	 * double sellQtyAdjustedWithSalesRetQty =
	 * selltrade.getQuantityMatchedWithSell();
	 * double remainingSellQuantity = 0.0;
	 * double salesRetQtyMatched = 0.0;
	 * double salesRetQtyGross = 0.0;
	 * if (sellQtyAdjustedWithSalesRetQty < sellQty) {
	 * remainingSellQuantity = sellQty - sellQtyAdjustedWithSalesRetQty;
	 * if (salesRetQtyRemaining <= remainingSellQuantity) {
	 * salesRetQtyGross = (selltrade.getCalcualtedRate()) * salesRetQtyRemaining;
	 * salesRetQtyMatched = salesRetQtyRemaining;
	 * selltrade.setQuantityMatchedWithSell(selltrade.getQuantityMatchedWithSell() +
	 * salesRetQtyRemaining);
	 * salesRetQtyRemaining = 0;
	 * salesRetQtyGross = selltrade.getCalcualtedRate() * remainingSellQuantity;
	 * salesRetQtyMatched = remainingSellQuantity;
	 * selltrade.setQuantityMatchedWithSell(selltrade.getQuantityMatchedWithSell() +
	 * remainingSellQuantity);
	 * salesRetQtyRemaining = salesRetQtyRemaining - remainingSellQuantity;
	 * }
	 * linkedBuyTrades = linkedBuyTrades + selltrade.getId() + ",";
	 * genericDao.saveOrUpdate(selltrade, user, entityManager);
	 * 
	 * TradingInventory tradingInv = new TradingInventory();
	 * tradingInv.setTransactionType(IdosConstants.TRADING_INV_SALES_RET);//buy=1,
	 * sell=2, opening=3, closing=4
	 * tradingInv.setTransactionId(transaction.getId());
	 * tradingInv.setDate(transaction.getTransactionDate());
	 * tradingInv.setBranch(branch);
	 * tradingInv.setOrganization(transaction.getProvisionMadeForOrganization());
	 * tradingInv.setUser(user);
	 * tradingInv.setTransactionSpecifics(sellSpecific);
	 * tradingInv.setBuySpecifics(buySpecifics);
	 * tradingInv.setTotalQuantity(salesRetQtyMatched * -1);
	 * tradingInv.setCalcualtedRate(selltrade.getCalcualtedRate());
	 * tradingInv.setLinkedBuyIds(linkedBuyTrades);
	 * tradingInv.setGrossValue(salesRetQtyGross * -1);
	 * genericDao.saveOrUpdate(tradingInv, user, entityManager);
	 * if (salesRetQtyRemaining <= 0) {
	 * break;
	 * }
	 * }
	 * }
	 * }
	 * }
	 * }
	 * if (tradInvCalcMethod.equalsIgnoreCase("WAC")) {
	 * //get last closing transaction
	 * StringBuffer sbr = new
	 * StringBuffer("select obj from TradingInventory obj where obj.transactionType=4 and obj.transactionSpecifics.id='"
	 * + buySpecifics.getId() + "' and obj.organization='" +
	 * user.getOrganization().getId() + "' and obj.branch='" + branch.getId() +
	 * "' order by obj.createdAt desc");
	 * 
	 * List<TradingInventory> tradingInvList =
	 * genericDao.executeSimpleQueryWithLimit(sbr.toString(), entityManager, 1);
	 * if (tradingInvList != null && tradingInvList.size() > 0) {
	 * TradingInventory inv = (TradingInventory) tradingInvList.get(0);
	 * double lastclosingconvertedqty = inv.getNoOfExpUnitsConvertedToIncUnits();
	 * double lastclosingrate = inv.getCalcualtedRate();
	 * double lastclosinggrossval = inv.getGrossValue();
	 * 
	 * double salesRetQty = sellReturnQty;
	 * double salesRetGross = salesRetQty * lastclosingrate;
	 * 
	 * Specifics buySpecific = sellSpecific.getLinkIncomeExpenseSpecifics();
	 * double newclosingconvertedqty = lastclosingconvertedqty + salesRetQty;
	 * double newclosinggrossval = lastclosinggrossval + salesRetGross;
	 * double newclosingrate =
	 * Double.parseDouble(IdosConstants.decimalFormat.format(newclosinggrossval /
	 * newclosingconvertedqty));
	 * TradingInventory tradingInvclosing = new TradingInventory();
	 * tradingInvclosing.setTransactionId(transaction.getId());
	 * tradingInvclosing.setDate(transaction.getTransactionDate());
	 * tradingInvclosing.setBranch(branch);
	 * tradingInvclosing.setOrganization(transaction.getProvisionMadeForOrganization
	 * ());
	 * tradingInvclosing.setUser(user);
	 * tradingInvclosing.setTransactionSpecifics(buySpecific);
	 * tradingInvclosing.setTransactionType(IdosConstants.TRADING_INV_CLOSING_BAL);
	 * tradingInvclosing.setGrossValue(newclosinggrossval);
	 * tradingInvclosing.setNoOfExpUnitsConvertedToIncUnits(newclosingconvertedqty);
	 * tradingInvclosing.setCalcualtedRate(newclosingrate);
	 * genericDao.saveOrUpdate(tradingInvclosing, user, entityManager);
	 * }
	 * }
	 * } catch (Exception ex) {
	 * log.log(Level.SEVERE, "Error", ex);
	 * }
	 * }
	 */

}
