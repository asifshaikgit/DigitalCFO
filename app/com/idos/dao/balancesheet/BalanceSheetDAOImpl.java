package com.idos.dao.balancesheet;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.idos.util.DateUtil;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.PLBSCOAMap.PLBSCOAMap;
import model.PLBSInventory;
import model.Specifics;
import model.profitloss.ProfitLossBean;
import org.apache.commons.lang3.StringUtils;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Users;
import model.balancesheet.BalanceSheetBean;

public class BalanceSheetDAOImpl implements BalanceSheetDAO {
	private static String jpql = new String(
			"select sum(closingBalanceCr), sum(closingBalancePr) from PLBSInventory where organization.id=?1 and presentStatus=1");

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.idos.dao.balancesheet.BalanceSheetDAO#displayBalanceSheet(com.fasterxml.
	 * jackson.databind.node.ObjectNode, com.fasterxml.jackson.databind.JsonNode,
	 * model.Users, javax.persistence.EntityManager,
	 * javax.persistence.EntityTransaction)
	 */
	public ObjectNode displayBalanceSheet(ObjectNode result, JsonNode json, Users user, EntityManager entityManager)
			throws IDOSException {
		final BalanceSheetBean bsBean = this.populateBalanceSheetBean(json, user, entityManager);

		setInventoryDetails(bsBean, user, entityManager);
		// Method will just invoke the computation steps for total assets and
		// liabilities.
		// It should update the bean object.
		bsBean.computeIntermidiateStepValues();

		if (Double.compare(bsBean.getAssetTotal(), bsBean.getLiabilityTotal()) == 0) {
			result.put("isCurrentDiff", false);
		} else {
			result.put("isCurrentDiff", true);
		}

		if (Double.compare(bsBean.getAssetTotalPrvRpt(), bsBean.getLiabilityTotalPrvRpt()) == 0) {
			result.put("isPreviousDiff", false);
		} else {
			result.put("isPreviousDiff", true);
		}

		final JsonNode bsjson = new ObjectMapper().convertValue(bsBean, JsonNode.class);
		result.put("BalanceSheetBean", bsjson);
		result.put("result", true);
		return result;
	}

	/**
	 * Method populateBalanceSheetBean - Fills the balance sheet bean.
	 * 
	 * @param result
	 * @param json
	 * @param user
	 * @param entityManager
	 * @param entitytransaction
	 * @return
	 */
	@Override
	public BalanceSheetBean populateBalanceSheetBean(JsonNode json, Users user, EntityManager entityManager)
			throws IDOSException {
		log.log(Level.FINE, "********** " + json);
		String fromDate = DateUtil.getCurrentFinacialStartDate(user.getOrganization().getFinancialStartDate());
		String prevFromDate = fromDate;
		String currPLFromDate = json.findValue("currPLFromDate") != null ? json.findValue("currPLFromDate").asText()
				: null;
		String prevPLFromDate = json.findValue("prevPLFromDate") != null ? json.findValue("prevPLFromDate").asText()
				: null;
		String currBSToDate = json.findValue("currPLToDate") != null ? json.findValue("currPLToDate").asText() : null;
		String prevBSToDate = json.findValue("prevPLToDate") != null ? json.findValue("prevPLToDate").asText() : null;
		Double profForPeriod = json.findValue("profForPeriod") != null ? json.findValue("profForPeriod").asDouble()
				: 0D;
		Double profForPeriodPrvRpt = json.findValue("profForPeriodPrvRpt") != null
				? json.findValue("profForPeriodPrvRpt").asDouble()
				: 0D;

		try {
			if (currPLFromDate != null && !currPLFromDate.equals("")) {
				fromDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(currPLFromDate));
			}
			if (prevPLFromDate != null && !prevPLFromDate.equals("")) {
				prevFromDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(prevPLFromDate));
			}
			currBSToDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(currBSToDate));
			if (prevBSToDate != null && !"".equals(prevBSToDate)) {
				prevBSToDate = IdosConstants.MYSQLDF.format(IdosConstants.IDOSDF.parse(prevBSToDate));
			}
		} catch (ParseException ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
					"date range given for P&L Calculation is wrong.", ex.getMessage());
		}
		Long organId = user.getOrganization().getId();
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, "fromDate: " + fromDate + " currBSToDate: " + currBSToDate + " organId: " + organId);
			log.log(Level.FINE, "HQL: " + bsquery);
		}
		Query sql = entityManager.createNativeQuery(bsquery);

		sql.setParameter(1, currBSToDate);
		sql.setParameter(2, organId);
		sql.setParameter(3, organId);
		sql.setParameter(4, currBSToDate);
		sql.setParameter(5, organId);
		sql.setParameter(6, organId);
		sql.setParameter(7, currBSToDate);
		sql.setParameter(8, organId);
		sql.setParameter(9, organId);
		sql.setParameter(10, currBSToDate);
		sql.setParameter(11, organId);
		sql.setParameter(12, organId);
		sql.setParameter(13, currBSToDate);
		sql.setParameter(14, organId);
		sql.setParameter(15, organId);
		sql.setParameter(16, currBSToDate);
		sql.setParameter(17, organId);
		sql.setParameter(18, organId);
		sql.setParameter(19, currBSToDate);
		sql.setParameter(20, organId);
		sql.setParameter(21, organId);
		sql.setParameter(22, currBSToDate);
		sql.setParameter(23, organId);
		sql.setParameter(24, organId);
		sql.setParameter(25, currBSToDate);
		sql.setParameter(26, organId);
		sql.setParameter(27, organId);
		sql.setParameter(28, currBSToDate);
		sql.setParameter(29, organId);
		sql.setParameter(30, organId);
		sql.setParameter(31, currBSToDate);
		sql.setParameter(32, organId);
		sql.setParameter(33, organId);
		sql.setParameter(34, currBSToDate);
		sql.setParameter(35, organId);
		sql.setParameter(36, organId);
		sql.setParameter(37, currBSToDate);
		sql.setParameter(38, organId);
		sql.setParameter(39, organId);
		sql.setParameter(40, currBSToDate);
		sql.setParameter(41, organId);
		sql.setParameter(42, organId);
		sql.setParameter(43, currBSToDate);
		sql.setParameter(44, organId);
		sql.setParameter(45, organId);
		sql.setParameter(46, currBSToDate);
		sql.setParameter(47, organId);
		sql.setParameter(48, organId);
		sql.setParameter(49, currBSToDate);
		sql.setParameter(50, organId);
		sql.setParameter(51, organId);
		sql.setParameter(52, currBSToDate);
		sql.setParameter(53, organId);
		sql.setParameter(54, organId);
		sql.setParameter(55, currBSToDate);
		sql.setParameter(56, organId);
		sql.setParameter(57, organId);
		sql.setParameter(58, currBSToDate);
		sql.setParameter(59, organId);
		sql.setParameter(60, organId);
		sql.setParameter(61, currBSToDate);
		sql.setParameter(62, organId);
		sql.setParameter(63, organId);
		sql.setParameter(64, currBSToDate);
		sql.setParameter(65, organId);
		sql.setParameter(66, organId);
		sql.setParameter(67, currBSToDate);
		sql.setParameter(68, organId);
		sql.setParameter(69, organId);
		sql.setParameter(70, currBSToDate);
		sql.setParameter(71, organId);
		sql.setParameter(72, organId);
		sql.setParameter(73, currBSToDate);
		sql.setParameter(74, organId);
		sql.setParameter(75, organId);
		sql.setParameter(76, currBSToDate);
		sql.setParameter(77, organId);
		sql.setParameter(78, organId);
		sql.setParameter(79, currBSToDate);
		sql.setParameter(80, organId);
		sql.setParameter(81, organId);
		sql.setParameter(82, currBSToDate);
		sql.setParameter(83, organId);
		sql.setParameter(84, organId);
		sql.setParameter(85, currBSToDate);
		sql.setParameter(86, organId);
		sql.setParameter(87, organId);
		sql.setParameter(88, currBSToDate);
		sql.setParameter(89, organId);
		sql.setParameter(90, organId);
		sql.setParameter(91, currBSToDate);
		sql.setParameter(92, organId);
		sql.setParameter(93, organId);
		sql.setParameter(94, currBSToDate);
		sql.setParameter(95, organId);
		sql.setParameter(96, organId);
		sql.setParameter(97, currBSToDate);
		sql.setParameter(98, organId);
		sql.setParameter(99, organId);
		sql.setParameter(100, currBSToDate);
		sql.setParameter(101, organId);
		sql.setParameter(102, organId);
		sql.setParameter(103, currBSToDate);
		sql.setParameter(104, organId);
		sql.setParameter(105, organId);
		sql.setParameter(106, currBSToDate);
		sql.setParameter(107, organId);
		sql.setParameter(108, organId);
		sql.setParameter(109, currBSToDate);
		sql.setParameter(110, organId);
		sql.setParameter(111, organId);
		sql.setParameter(112, currBSToDate);
		sql.setParameter(113, organId);
		sql.setParameter(114, organId);
		sql.setParameter(115, currBSToDate);
		sql.setParameter(116, organId);
		sql.setParameter(117, organId);
		List<Object[]> bsValLst = sql.getResultList();
		BalanceSheetBean bsBean = new BalanceSheetBean();
		ProfitLossBean profitLossBean = getReservesAndSurplusOpeningBalance(user, entityManager, fromDate,
				prevFromDate);
		if (profitLossBean != null) {
			Double obReservesAndSurplus = profitLossBean.getOpeningBalanceReservesAndSurplus() != null
					? profitLossBean.getOpeningBalanceReservesAndSurplus()
					: 0.0;
			Double obReservesAndSurplusPrev = profitLossBean.getOpeningBalanceReservesAndSurplusPrevRpt() != null
					? profitLossBean.getOpeningBalanceReservesAndSurplusPrevRpt()
					: 0.0;
			bsBean.setReservesAndSurplus(obReservesAndSurplus);
			bsBean.setReservesAndSurplusPrvRpt(obReservesAndSurplusPrev);
		}

		Integer plbsHead = null;
		Double curAssets = 0D;
		Double curLiab = 0D;
		Double curr = 0D;
		Double prev = 0D;
		for (Object[] bsdBean : bsValLst) {
			if (null == bsdBean[0])
				continue;
			plbsHead = (Integer) bsdBean[0];
			curLiab = bsdBean[1] == null ? 0D : (Double) bsdBean[1];
			curAssets = bsdBean[3] == null ? 0D : (Double) bsdBean[3];
			switch (plbsHead) {
				case IdosConstants.LI_SHARE_CAPITAL:
					curr = bsBean.getShareCapital() == null ? 0D : bsBean.getShareCapital();
					curLiab += curr;
					bsBean.setShareCapital(curLiab);
					break;
				case IdosConstants.LI_RESERVES_AND_SURPLUS:
					curr = bsBean.getReservesAndSurplus() == null ? 0D : bsBean.getReservesAndSurplus();
					log.log(Level.FINE, "Head " + plbsHead + " =exist==== " + curr + " new " + curLiab);
					curLiab += curr;
					bsBean.setReservesAndSurplus(curLiab);
					break;
				case IdosConstants.LI_MONEY_RECEIVED_AGAINST_SHARE_WARRANTS: // "MONEY RECEIVED AGAINST SHARE WARRANTS":
					curr = bsBean.getMoneyReceivedAgainstShareWarrants() == null ? 0D
							: bsBean.getMoneyReceivedAgainstShareWarrants();
					curLiab += curr;
					bsBean.setMoneyReceivedAgainstShareWarrants(curLiab);
					break;
				case IdosConstants.LI_SHARE_APPLICATION_MONEY_PENDING_ALLOTMENT: // "SHARE APPLICATION MONEY PENDING
																					// ALLOTMENT":
					curr = bsBean.getShareApplicationMoneyPendingAllotment() == null ? 0D
							: bsBean.getShareApplicationMoneyPendingAllotment();
					curLiab += curr;
					bsBean.setShareApplicationMoneyPendingAllotment(curLiab);
					break;
				case IdosConstants.LI_LONG_TERM_BORROWINGS: // "LONG-TERM BORROWINGS":
					curr = bsBean.getLongTermBorrowings() == null ? 0D : bsBean.getLongTermBorrowings();
					curLiab += curr;
					bsBean.setLongTermBorrowings(curLiab);
					break;
				case IdosConstants.LI_DEFERRED_TAX_LIABILITIES_NET: // "DEFERRED TAX LIABILITIES (NET)":
					curr = bsBean.getDeferredTaxLiabilitiesNet() == null ? 0D : bsBean.getDeferredTaxLiabilitiesNet();
					curLiab += curr;
					bsBean.setDeferredTaxLiabilitiesNet(curLiab);
					break;
				case IdosConstants.LI_OTHER_LONG_TERM_LIABILITIES: // "OTHER LONG TERM LIABILITIES":
					curr = bsBean.getOtherLongTermLiabilities() == null ? 0D : bsBean.getOtherLongTermLiabilities();
					curLiab += curr;
					bsBean.setOtherLongTermLiabilities(curLiab);
					break;
				case IdosConstants.LI_LONG_TERM_PROVISIONS: // "LONG-TERM PROVISIONS":
					curr = bsBean.getLongTermProvisions() == null ? 0D : bsBean.getLongTermProvisions();
					curLiab += curr;
					bsBean.setLongTermProvisions(curLiab);
					break;
				case IdosConstants.LI_SHORT_TERM_BORROWINGS: // "SHORT-TERM BORROWINGS":
					curr = bsBean.getShortTermBorrowings() == null ? 0D : bsBean.getShortTermBorrowings();
					curLiab += curr;
					bsBean.setShortTermBorrowings(curLiab);
					break;
				case IdosConstants.LI_TRADE_PAYABLES: // "TRADE PAYABLES":
					curr = bsBean.getTradePayables() == null ? 0D : bsBean.getTradePayables();
					curLiab += curr;
					bsBean.setTradePayables(curLiab);
					break;
				case IdosConstants.LI_OTHER_CURRENT_LIABILITIES: // "OTHER CURRENT LIABILITIES":
					curr = bsBean.getOtherCurrentLiabilities() == null ? 0D : bsBean.getOtherCurrentLiabilities();
					curLiab += curr;
					bsBean.setOtherCurrentLiabilities(curLiab);
					break;
				case IdosConstants.LI_SHORT_TERM_PROVISIONS: // "SHORT-TERM PROVISIONS":
					curr = bsBean.getShortTermProvisions() == null ? 0D : bsBean.getShortTermProvisions();
					curLiab += curr;
					bsBean.setShortTermProvisions(curLiab);
					break;
				case IdosConstants.AS_TANGIBLE_ASSETS: // "TANGIBLE ASSETS":
					curr = bsBean.getTangibleAssets() == null ? 0D : bsBean.getTangibleAssets();
					log.log(Level.FINE,
							"PHEAD:" + plbsHead + "=======================> " + curr + "  curAssets: " + curAssets);
					curAssets += curr;
					bsBean.setTangibleAssets(curAssets);
					break;
				case IdosConstants.AS_INTANGIBLE_ASSETS: // "INTANGIBLE ASSETS":
					curr = bsBean.getIntangibleAssets() == null ? 0D : bsBean.getIntangibleAssets();
					curAssets += curr;
					bsBean.setIntangibleAssets(curAssets);
					break;
				case IdosConstants.AS_CAPITAL_WORK_IN_PROGRESS: // "CAPITAL WORK-IN-PROGRESS":
					curr = bsBean.getCapitalWorkInProgress() == null ? 0D : bsBean.getCapitalWorkInProgress();
					curAssets += curr;
					bsBean.setCapitalWorkInProgress(curAssets);
					break;

				case IdosConstants.AS_INTANGIBLE_ASSETS_UNDER_DEV: // "INTANGIBLE ASSETS UNDER DEVELOPMENT":
					curr = bsBean.getIntangibleAssetsUnderDevelopment() == null ? 0D
							: bsBean.getIntangibleAssetsUnderDevelopment();
					curAssets += curr;
					bsBean.setIntangibleAssetsUnderDevelopment(curAssets);
					break;
				case IdosConstants.AS_NON_CURRENT_INVESTMENTS: // "NON-CURRENT INVESTMENTS":
					curr = bsBean.getNonCurrentInvestments() == null ? 0D : bsBean.getNonCurrentInvestments();
					curAssets += curr;
					bsBean.setNonCurrentInvestments(curAssets);
					break;

				case IdosConstants.AS_DEFERRED_TAX_ASSETS_NET: // "DEFERRED TAX ASSETS (NET)":
					curr = bsBean.getDeferredTaxAssetsNet() == null ? 0D : bsBean.getDeferredTaxAssetsNet();
					curAssets += curr;
					bsBean.setDeferredTaxAssetsNet(curAssets);
					break;

				case IdosConstants.AS_LONG_TERM_LOANS_AND_ADVANCES: // "LONG-TERM LOANS AND ADVANCES":
					curr = bsBean.getLongTermLoansAndAdvances() == null ? 0D : bsBean.getLongTermLoansAndAdvances();
					curAssets += curr;
					bsBean.setLongTermLoansAndAdvances(curAssets);
					break;

				case IdosConstants.AS_OTHER_NON_CURRENT_ASSETS: // "OTHER NON-CURRENT ASSETS":
					curr = bsBean.getOtherNonCurrentAssets() == null ? 0D : bsBean.getOtherNonCurrentAssets();
					curAssets += curr;
					bsBean.setOtherNonCurrentAssets(curAssets);
					break;

				case IdosConstants.AS_CURRENT_INVESTMENTS: // "CURRENT INVESTMENTS":
					curr = bsBean.getCurrentInvestments() == null ? 0D : bsBean.getCurrentInvestments();
					curAssets += curr;
					bsBean.setCurrentInvestments(curAssets);
					break;

				case IdosConstants.AS_INVENTORIES: // "INVENTORIES":
					/*
					 * curr = bsBean.getInventories() == null ? 0D : bsBean.getInventories();
					 * curAssets +=curr;
					 * bsBean.setInventories(curAssets);
					 */
					break;

				case IdosConstants.AS_TRADE_RECEIVABLES: // "TRADE RECEIVABLES":
					curr = bsBean.getTradeReceivables() == null ? 0D : bsBean.getTradeReceivables();
					curAssets += curr;
					bsBean.setTradeReceivables(curAssets);
					break;

				case IdosConstants.AS_CASH_AND_CASH_EQUIVALENTS: // "CASH AND CASH EQUIVALENTS":
					curr = bsBean.getCashAndCashEquivalents() == null ? 0D : bsBean.getCashAndCashEquivalents();
					curAssets += curr;
					bsBean.setCashAndCashEquivalents(curAssets);
					break;

				case IdosConstants.AS_SHORT_TERM_LOANS_AND_ADVANCES: // "SHORT-TERM LOANS AND ADVANCES":
					curr = bsBean.getShortTermLoansAndAdvances() == null ? 0D : bsBean.getShortTermLoansAndAdvances();
					curAssets += curr;
					bsBean.setShortTermLoansAndAdvances(curAssets);
					break;

				case IdosConstants.AS_OTHER_CURRENT_ASSETS: // "OTHER CURRENT ASSETS":
					curr = bsBean.getOtherCurrentAssets() == null ? 0D : bsBean.getOtherCurrentAssets();
					curAssets += curr;
					bsBean.setOtherCurrentAssets(curAssets);
					break;
			}// End of switch.
		} // End of for loop.
		bsBean.setProfitLossForPeriod(profForPeriod);

		// Now populate prevFigures
		if (prevBSToDate != null && !"".equals(prevBSToDate)) {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "HQL Prev: " + bsquery);
			}
			sql = entityManager.createNativeQuery(bsquery);
			sql.setParameter(1, prevBSToDate);
			sql.setParameter(2, organId);
			sql.setParameter(3, organId);
			sql.setParameter(4, prevBSToDate);
			sql.setParameter(5, organId);
			sql.setParameter(6, organId);
			sql.setParameter(7, prevBSToDate);
			sql.setParameter(8, organId);
			sql.setParameter(9, organId);
			sql.setParameter(10, prevBSToDate);
			sql.setParameter(11, organId);
			sql.setParameter(12, organId);
			sql.setParameter(13, prevBSToDate);
			sql.setParameter(14, organId);
			sql.setParameter(15, organId);
			sql.setParameter(16, prevBSToDate);
			sql.setParameter(17, organId);
			sql.setParameter(18, organId);
			sql.setParameter(19, prevBSToDate);
			sql.setParameter(20, organId);
			sql.setParameter(21, organId);
			sql.setParameter(22, prevBSToDate);
			sql.setParameter(23, organId);
			sql.setParameter(24, organId);
			sql.setParameter(25, prevBSToDate);
			sql.setParameter(26, organId);
			sql.setParameter(27, organId);
			sql.setParameter(28, prevBSToDate);
			sql.setParameter(29, organId);
			sql.setParameter(30, organId);
			sql.setParameter(31, prevBSToDate);
			sql.setParameter(32, organId);
			sql.setParameter(33, organId);
			sql.setParameter(34, prevBSToDate);
			sql.setParameter(35, organId);
			sql.setParameter(36, organId);
			sql.setParameter(37, prevBSToDate);
			sql.setParameter(38, organId);
			sql.setParameter(39, organId);
			sql.setParameter(40, prevBSToDate);
			sql.setParameter(41, organId);
			sql.setParameter(42, organId);
			sql.setParameter(43, prevBSToDate);
			sql.setParameter(44, organId);
			sql.setParameter(45, organId);
			sql.setParameter(46, prevBSToDate);
			sql.setParameter(47, organId);
			sql.setParameter(48, organId);
			sql.setParameter(49, prevBSToDate);
			sql.setParameter(50, organId);
			sql.setParameter(51, organId);
			sql.setParameter(52, prevBSToDate);
			sql.setParameter(53, organId);
			sql.setParameter(54, organId);
			sql.setParameter(55, prevBSToDate);
			sql.setParameter(56, organId);
			sql.setParameter(57, organId);
			sql.setParameter(58, prevBSToDate);
			sql.setParameter(59, organId);
			sql.setParameter(60, organId);
			sql.setParameter(61, prevBSToDate);
			sql.setParameter(62, organId);
			sql.setParameter(63, organId);
			sql.setParameter(64, prevBSToDate);
			sql.setParameter(65, organId);
			sql.setParameter(66, organId);
			sql.setParameter(67, prevBSToDate);
			sql.setParameter(68, organId);
			sql.setParameter(69, organId);
			sql.setParameter(70, prevBSToDate);
			sql.setParameter(71, organId);
			sql.setParameter(72, organId);
			sql.setParameter(73, prevBSToDate);
			sql.setParameter(74, organId);
			sql.setParameter(75, organId);
			sql.setParameter(76, prevBSToDate);
			sql.setParameter(77, organId);
			sql.setParameter(78, organId);
			sql.setParameter(79, prevBSToDate);
			sql.setParameter(80, organId);
			sql.setParameter(81, organId);
			sql.setParameter(82, prevBSToDate);
			sql.setParameter(83, organId);
			sql.setParameter(84, organId);
			sql.setParameter(85, prevBSToDate);
			sql.setParameter(86, organId);
			sql.setParameter(87, organId);
			sql.setParameter(88, prevBSToDate);
			sql.setParameter(89, organId);
			sql.setParameter(90, organId);
			sql.setParameter(91, prevBSToDate);
			sql.setParameter(92, organId);
			sql.setParameter(93, organId);
			sql.setParameter(94, prevBSToDate);
			sql.setParameter(95, organId);
			sql.setParameter(96, organId);
			sql.setParameter(97, prevBSToDate);
			sql.setParameter(98, organId);
			sql.setParameter(99, organId);
			sql.setParameter(100, prevBSToDate);
			sql.setParameter(101, organId);
			sql.setParameter(102, organId);
			sql.setParameter(103, prevBSToDate);
			sql.setParameter(104, organId);
			sql.setParameter(105, organId);
			sql.setParameter(106, prevBSToDate);
			sql.setParameter(107, organId);
			sql.setParameter(108, organId);
			sql.setParameter(109, prevBSToDate);
			sql.setParameter(110, organId);
			sql.setParameter(111, organId);
			sql.setParameter(112, prevBSToDate);
			sql.setParameter(113, organId);
			sql.setParameter(114, organId);
			sql.setParameter(115, prevBSToDate);
			sql.setParameter(116, organId);
			sql.setParameter(117, organId);
			bsValLst.clear();
			bsValLst = sql.getResultList();
			Double prevAssets = 0D;
			Double prevLiab = 0D;
			for (Object[] bsdBean : bsValLst) {
				if (null == bsdBean[0])
					continue;
				plbsHead = (Integer) bsdBean[0];
				prevLiab = bsdBean[1] == null ? 0D : (Double) bsdBean[1];
				prevAssets = bsdBean[3] == null ? 0D : (Double) bsdBean[3];
				switch (plbsHead) {
					case IdosConstants.LI_SHARE_CAPITAL:
						curr = bsBean.getShareCapitalPrvRpt() == null ? 0D : bsBean.getShareCapitalPrvRpt();
						prevLiab += curr;
						bsBean.setShareCapitalPrvRpt(prevLiab);
						break;
					case IdosConstants.LI_RESERVES_AND_SURPLUS: // "RESERVES AND SURPLUSLI_RESERVES_AND_SURPLUS:
						curr = bsBean.getReservesAndSurplusPrvRpt() == null ? 0D : bsBean.getReservesAndSurplusPrvRpt();
						prevLiab += curr;
						bsBean.setReservesAndSurplusPrvRpt(prevLiab);
						break;
					case IdosConstants.LI_MONEY_RECEIVED_AGAINST_SHARE_WARRANTS: // "MONEY RECEIVED AGAINST SHARE
																					// WARRANTS":
						curr = bsBean.getMoneyReceivedAgainstShareWarrantsPrvRpt() == null ? 0D
								: bsBean.getMoneyReceivedAgainstShareWarrantsPrvRpt();
						prevLiab += curr;
						bsBean.setMoneyReceivedAgainstShareWarrantsPrvRpt(prevLiab);
						break;
					case IdosConstants.LI_SHARE_APPLICATION_MONEY_PENDING_ALLOTMENT: // "SHARE APPLICATION MONEY PENDING
																						// ALLOTMENT":
						curr = bsBean.getShareApplicationMoneyPendingAllotmentPrvRpt() == null ? 0D
								: bsBean.getShareApplicationMoneyPendingAllotmentPrvRpt();
						prevLiab += curr;
						bsBean.setShareApplicationMoneyPendingAllotmentPrvRpt(prevLiab);
						break;
					case IdosConstants.LI_LONG_TERM_BORROWINGS: // "LONG-TERM BORROWINGS":
						curr = bsBean.getLongTermBorrowingsPrvRpt() == null ? 0D : bsBean.getLongTermBorrowingsPrvRpt();
						prevLiab += curr;
						bsBean.setLongTermBorrowingsPrvRpt(prevLiab);
						break;
					case IdosConstants.LI_DEFERRED_TAX_LIABILITIES_NET: // "DEFERRED TAX LIABILITIES (NET)":
						curr = bsBean.getDeferredTaxLiabilitiesNetPrvRpt() == null ? 0D
								: bsBean.getDeferredTaxLiabilitiesNetPrvRpt();
						prevLiab += curr;
						bsBean.setDeferredTaxLiabilitiesNetPrvRpt(prevLiab);
						break;
					case IdosConstants.LI_OTHER_LONG_TERM_LIABILITIES: // "OTHER LONG TERM LIABILITIES":
						curr = bsBean.getOtherLongTermLiabilitiesPrvRpt() == null ? 0D
								: bsBean.getOtherLongTermLiabilitiesPrvRpt();
						prevLiab += curr;
						bsBean.setOtherLongTermLiabilitiesPrvRpt(prevLiab);
						break;
					case IdosConstants.LI_LONG_TERM_PROVISIONS: // "LONG-TERM PROVISIONS":
						curr = bsBean.getLongTermProvisionsPrvRpt() == null ? 0D : bsBean.getLongTermProvisionsPrvRpt();
						prevLiab += curr;
						bsBean.setLongTermProvisionsPrvRpt(prevLiab);
						break;
					case IdosConstants.LI_SHORT_TERM_BORROWINGS: // "SHORT-TERM BORROWINGS":
						curr = bsBean.getShortTermBorrowingsPrvRpt() == null ? 0D
								: bsBean.getShortTermBorrowingsPrvRpt();
						prevLiab += curr;
						bsBean.setShortTermBorrowingsPrvRpt(prevLiab);
						break;
					case IdosConstants.LI_TRADE_PAYABLES: // "TRADE PAYABLES":
						curr = bsBean.getTradePayablesPrvRpt() == null ? 0D : bsBean.getTradePayablesPrvRpt();
						prevLiab += curr;
						bsBean.setTradePayablesPrvRpt(prevLiab);
						break;
					case IdosConstants.LI_OTHER_CURRENT_LIABILITIES: // "OTHER CURRENT LIABILITIES":
						curr = bsBean.getOtherCurrentLiabilitiesPrvRpt() == null ? 0D
								: bsBean.getOtherCurrentLiabilitiesPrvRpt();
						prevLiab += curr;
						bsBean.setOtherCurrentLiabilitiesPrvRpt(prevLiab);
						break;
					case IdosConstants.LI_SHORT_TERM_PROVISIONS: // "SHORT-TERM PROVISIONS":
						curr = bsBean.getShortTermProvisionsPrvRpt() == null ? 0D
								: bsBean.getShortTermProvisionsPrvRpt();
						prevLiab += curr;
						bsBean.setShortTermProvisionsPrvRpt(prevLiab);
						break;
					case IdosConstants.AS_TANGIBLE_ASSETS: // "TANGIBLE ASSETS":
						curr = bsBean.getTangibleAssetsPrvRpt() == null ? 0D : bsBean.getTangibleAssetsPrvRpt();
						prevAssets += curr;
						bsBean.setTangibleAssetsPrvRpt(prevAssets);
						break;

					case IdosConstants.AS_INTANGIBLE_ASSETS: // "INTANGIBLE ASSETS":
						curr = bsBean.getIntangibleAssetsPrvRpt() == null ? 0D : bsBean.getIntangibleAssetsPrvRpt();
						prevAssets += curr;
						bsBean.setIntangibleAssetsPrvRpt(prevAssets);
						break;

					case IdosConstants.AS_CAPITAL_WORK_IN_PROGRESS: // "CAPITAL WORK-IN-PROGRESS":
						curr = bsBean.getCapitalWorkInProgressPrvRpt() == null ? 0D
								: bsBean.getCapitalWorkInProgressPrvRpt();
						prevAssets += curr;
						bsBean.setCapitalWorkInProgressPrvRpt(prevAssets);
						break;

					case IdosConstants.AS_INTANGIBLE_ASSETS_UNDER_DEV: // "INTANGIBLE ASSETS UNDER DEVELOPMENT":
						curr = bsBean.getIntangibleAssetsUnderDevelopmentPrvRpt() == null ? 0D
								: bsBean.getIntangibleAssetsUnderDevelopmentPrvRpt();
						prevAssets += curr;
						bsBean.setIntangibleAssetsUnderDevelopmentPrvRpt(prevAssets);
						break;
					case IdosConstants.AS_NON_CURRENT_INVESTMENTS: // "NON-CURRENT INVESTMENTS":
						curr = bsBean.getNonCurrentInvestmentsPrvRpt() == null ? 0D
								: bsBean.getNonCurrentInvestmentsPrvRpt();
						prevAssets += curr;
						bsBean.setNonCurrentInvestmentsPrvRpt(prevAssets);
						break;

					case IdosConstants.AS_DEFERRED_TAX_ASSETS_NET: // "DEFERRED TAX ASSETS (NET)":
						curr = bsBean.getDeferredTaxAssetsNetPrvRpt() == null ? 0D
								: bsBean.getDeferredTaxAssetsNetPrvRpt();
						prevAssets += curr;
						bsBean.setDeferredTaxAssetsNetPrvRpt(prevAssets);
						break;

					case IdosConstants.AS_LONG_TERM_LOANS_AND_ADVANCES: // "LONG-TERM LOANS AND ADVANCES":
						curr = bsBean.getLongTermLoansAndAdvancesPrvRpt() == null ? 0D
								: bsBean.getLongTermLoansAndAdvancesPrvRpt();
						prevAssets += curr;
						bsBean.setLongTermLoansAndAdvancesPrvRpt(prevAssets);
						break;

					case IdosConstants.AS_OTHER_NON_CURRENT_ASSETS: // "OTHER NON-CURRENT ASSETS":
						curr = bsBean.getOtherNonCurrentAssetsPrvRpt() == null ? 0D
								: bsBean.getOtherNonCurrentAssetsPrvRpt();
						prevAssets += curr;
						bsBean.setOtherNonCurrentAssetsPrvRpt(prevAssets);
						break;

					case IdosConstants.AS_CURRENT_INVESTMENTS: // "CURRENT INVESTMENTS":
						curr = bsBean.getCurrentInvestmentsPrvRpt() == null ? 0D : bsBean.getCurrentInvestmentsPrvRpt();
						prevAssets += curr;
						bsBean.setCurrentInvestmentsPrvRpt(prevAssets);
						break;

					case IdosConstants.AS_INVENTORIES: // "INVENTORIES":
						/*
						 * curr = bsBean.getInventoriesPrvRpt() == null ? 0D :
						 * bsBean.getInventoriesPrvRpt();
						 * prevAssets +=curr;
						 * bsBean.setInventoriesPrvRpt(prevAssets);
						 */
						break;

					case IdosConstants.AS_TRADE_RECEIVABLES: // "TRADE RECEIVABLES":
						curr = bsBean.getTradeReceivablesPrvRpt() == null ? 0D : bsBean.getTradeReceivablesPrvRpt();
						prevAssets += curr;
						bsBean.setTradeReceivablesPrvRpt(prevAssets);
						break;

					case IdosConstants.AS_CASH_AND_CASH_EQUIVALENTS: // "CASH AND CASH EQUIVALENTS":
						curr = bsBean.getCashAndCashEquivalentsPrvRpt() == null ? 0D
								: bsBean.getCashAndCashEquivalentsPrvRpt();
						prevAssets += curr;
						bsBean.setCashAndCashEquivalentsPrvRpt(prevAssets);
						break;

					case IdosConstants.AS_SHORT_TERM_LOANS_AND_ADVANCES: // "SHORT-TERM LOANS AND ADVANCES":
						curr = bsBean.getShortTermLoansAndAdvancesPrvRpt() == null ? 0D
								: bsBean.getShortTermLoansAndAdvancesPrvRpt();
						prevAssets += curr;
						bsBean.setShortTermLoansAndAdvancesPrvRpt(prevAssets);
						break;

					case IdosConstants.AS_OTHER_CURRENT_ASSETS: // "OTHER CURRENT ASSETS":
						curr = bsBean.getOtherCurrentAssetsPrvRpt() == null ? 0D : bsBean.getOtherCurrentAssetsPrvRpt();
						prevAssets += curr;
						bsBean.setOtherCurrentAssetsPrvRpt(prevAssets);
						break;
				}// End of switch.
				bsBean.setProfitLossForPeriodPrvRpt(profForPeriodPrvRpt);

			} // End of for loop.
		}
		return bsBean;
	}

	private void setInventoryDetails(BalanceSheetBean balanceSheetBean, Users user, EntityManager entityManager) {
		List<Object[]> plbsInventoryList = null;
		Query sql = entityManager.createQuery(jpql);
		sql.setParameter(1, user.getOrganization().getId());
		plbsInventoryList = sql.getResultList();
		Double closingBalaCr = 0d;
		Double closingBalaPr = 0d;
		for (Object[] custData : plbsInventoryList) {
			if (custData[0] != null) {
				closingBalaCr += Double.parseDouble(String.valueOf(custData[0]));
			}
			if (custData[1] != null) {
				closingBalaPr += Double.parseDouble(String.valueOf(custData[1]));
			}
		}
		balanceSheetBean.setInventories(closingBalaCr);
		balanceSheetBean.setInventoriesPrvRpt(closingBalaPr);
	}

	private ProfitLossBean getReservesAndSurplusOpeningBalance(Users user, EntityManager em, String endDate,
			String endDatePrev) throws IDOSException {
		log.log(Level.FINE, "********** Start " + endDate);
		String[] arr = DateUtil.getFinancialDate(user);
		String finStartDateStr = arr[0];
		/*
		 * Double openingBalance = 0.0;
		 * 
		 * String jpql =
		 * "select obj from PLBSCOAMap obj where obj.organization.id= ?1 and obj.plbsHead=401 and obj.coaId like 'item%' and obj.presentStatus=1"
		 * ;
		 * ArrayList inparams = new ArrayList(1);
		 * inparams.add(user.getOrganization().getId());
		 * List<PLBSCOAMap> plbscoaMaps = genericDao.queryWithParamsName(jpql, em,
		 * inparams);
		 * for(PLBSCOAMap map : plbscoaMaps) {
		 * if(map.getCoaId() != null) {
		 * String specificId = map.getCoaId().replaceAll("item", "");
		 * final Specifics specific = Specifics.findById(Long.parseLong(specificId));
		 * if (specific != null) {
		 * openingBalance += specific.getTotalOpeningBalance() != null ?
		 * specific.getTotalOpeningBalance() : 0.0;
		 * }
		 * }
		 * }
		 * log.log(Level.FINE, "********** Spec " + openingBalance);
		 */
		final ProfitLossBean plBean = PROFIT_LOSS_DAO.getPnLWithoutOpeningBalanceOfItem(user, em, finStartDateStr,
				endDate, finStartDateStr, endDatePrev);
		plBean.computeIntermidiateStepValues();
		Double openingBalancePrev = plBean.getProfForPeriodPrvRpt();
		plBean.setOpeningBalanceReservesAndSurplusPrevRpt(openingBalancePrev);
		Double openingBalance = plBean.getProfForPeriod();
		plBean.setOpeningBalanceReservesAndSurplus(openingBalance);
		log.log(Level.FINE, "********** End " + openingBalance);
		return plBean;
	}
}
