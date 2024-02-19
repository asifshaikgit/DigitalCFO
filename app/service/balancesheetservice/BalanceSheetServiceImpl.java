package service.balancesheetservice;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.BalanceSheetReport;
import model.balancesheet.BalanceSheetBean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.logging.Level;
import model.Users;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class BalanceSheetServiceImpl implements BalanceSheetService {

	@Override
	public ObjectNode displayBalanceSheet(ObjectNode result, JsonNode json, Users user, EntityManager entityManager)
			throws IDOSException {
		result = BALANCE_SHEET_DAO.displayBalanceSheet(result, json, user, entityManager);
		return result;
	}

	@Override
	public List<BalanceSheetReport> getBalanceSheetData(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) throws IDOSException {
		List<BalanceSheetReport> list = null;
		BalanceSheetBean bsBean = BALANCE_SHEET_DAO.populateBalanceSheetBean(json, user, entityManager);
		if (bsBean != null) {
			bsBean.computeIntermidiateStepValues();
			list = new ArrayList<BalanceSheetReport>(42);
			BalanceSheetReport rptData = new BalanceSheetReport();
			rptData.setParticularName("I. EQUITY AND LIABILITIES");
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("(1) Shareholders' funds");
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (a) Share capital");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShareCapital()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShareCapitalPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (b) Reserves and surplus");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getReservesAndSurplus()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getReservesAndSurplusPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    Add: profit (loss) for this period");
			// rptData.setCurrentValue(bsBean.getProfitLossForPeriod() +", "+
			// bsBean.getProfitLossForPeriodPlusReservesSurplus() );
			// rptData.setPreviousValue(bsBean.getProfitLossForPeriodPrvRpt() +", "+
			// bsBean.getProfitLossForPeriodPlusReservesSurplusPrvRpt());
			rptData.setProfitLossForPeriod(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getProfitLossForPeriod()));
			rptData.setCurrentValue(
					IdosConstants.DECIMAL_FORMAT2.format(bsBean.getProfitLossForPeriodPlusReservesSurplus()));
			rptData.setProfitLossForPreviousPeriod(
					IdosConstants.DECIMAL_FORMAT2.format(bsBean.getProfitLossForPeriodPrvRpt()));
			rptData.setPreviousValue(
					IdosConstants.DECIMAL_FORMAT2.format(bsBean.getProfitLossForPeriodPlusReservesSurplusPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (c) Money received against share warrants");
			rptData.setCurrentValue(
					IdosConstants.DECIMAL_FORMAT2.format(bsBean.getMoneyReceivedAgainstShareWarrants()));
			rptData.setPreviousValue(
					IdosConstants.DECIMAL_FORMAT2.format(bsBean.getMoneyReceivedAgainstShareWarrantsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  Total");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShareholderFundsTotal()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShareholderFundsTotalPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("(2)Share application money pending allotment");
			rptData.setCurrentValue(
					IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShareApplicationMoneyPendingAllotment()));
			rptData.setPreviousValue(
					IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShareApplicationMoneyPendingAllotmentPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("(3) Non-current liabilities");
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (a) Long-term borrowings");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getLongTermBorrowings()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getLongTermBorrowingsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (b) Deferred tax liabilities (Net)");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getDeferredTaxLiabilitiesNet()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getDeferredTaxLiabilitiesNetPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (c) Other Long term liabilities");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getOtherLongTermLiabilities()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getOtherLongTermLiabilitiesPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (d) Long-term provisions");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getLongTermProvisions()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getLongTermProvisionsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  Total");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getNonCurrentLiabilitiesTotal()));
			rptData.setPreviousValue(
					IdosConstants.DECIMAL_FORMAT2.format(bsBean.getNonCurrentLiabilitiesTotalPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("(4) Current liabilities");
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (a) Short-term borrowings");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShortTermBorrowings()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShortTermBorrowingsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (b) Trade payables");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getTradePayables()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getTradePayablesPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (c) Other current liabilities");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getOtherCurrentLiabilities()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getOtherCurrentLiabilitiesPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (d) Short-term provisions");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShortTermProvisions()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShortTermProvisionsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  Total");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getCurrentLiabilitiesTotal()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getCurrentLiabilitiesTotalPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("TOTAL");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getLiabilityTotal()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getLiabilityTotalPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("II. ASSETS");
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("Non-current assets");
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("(1) (a) Fixed assets");
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (i) Tangible assets");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getTangibleAssets()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getTangibleAssetsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (ii) Intangible assets");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getIntangibleAssets()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getIntangibleAssetsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (iii) Capital work-in-progress");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getCapitalWorkInProgress()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getCapitalWorkInProgressPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("    (iv) Intangible assets under development");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getIntangibleAssetsUnderDevelopment()));
			rptData.setPreviousValue(
					IdosConstants.DECIMAL_FORMAT2.format(bsBean.getIntangibleAssetsUnderDevelopmentPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  (b) Non-current investments");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getNonCurrentInvestments()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getNonCurrentInvestmentsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  (c) Deferred tax assets (net)");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getDeferredTaxAssetsNet()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getDeferredTaxAssetsNetPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  (d) Long-term loans and advances");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getLongTermLoansAndAdvances()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getLongTermLoansAndAdvancesPrvRpt()));
			list.add(rptData);
			rptData = new BalanceSheetReport();
			rptData.setParticularName("  (e) Other non-current assets");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getOtherNonCurrentAssets()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getOtherNonCurrentAssetsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  Total");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getNonCurrentAssetsTotal()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getNonCurrentAssetsTotalPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("(2) Current assets");
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  (a) Current investments");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getCurrentInvestments()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getCurrentInvestmentsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  (b) Inventories");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getInventories()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getInventoriesPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  (c) Trade receivables");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getTradeReceivables()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getTradeReceivablesPrvRpt()));
			list.add(rptData);
			rptData = new BalanceSheetReport();
			rptData.setParticularName("  (d) Cash and cash equivalents");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getCashAndCashEquivalents()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getCashAndCashEquivalentsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  (e) Short-term loans and advances");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShortTermLoansAndAdvances()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getShortTermLoansAndAdvancesPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  (f) Other current assets");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getOtherCurrentAssets()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getOtherCurrentAssetsPrvRpt()));
			list.add(rptData);

			rptData = new BalanceSheetReport();
			rptData.setParticularName("  Total");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getCurrentAssetsTotal()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getCurrentAssetsTotalPrvRpt()));
			rptData.setTotal(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getAssetTotal()));
			rptData.setTotalPrev(IdosConstants.DECIMAL_FORMAT2.format(bsBean.getAssetTotalPrvRpt()));
			list.add(rptData);

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
		}

		String currPLFromDate = json.findValue("currPLFromDate") != null ? json.findValue("currPLFromDate").asText()
				: null;
		String currPLToDate = json.findValue("currPLToDate") != null ? json.findValue("currPLToDate").asText() : null;
		String prevPLFromDate = json.findValue("prevPLFromDate") != null ? json.findValue("prevPLFromDate").asText()
				: null;
		String prevPLToDate = json.findValue("prevPLToDate") != null ? json.findValue("prevPLToDate").asText() : null;
		try {
			BalanceSheetReport rpt = list.get(0);
			currPLFromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currPLFromDate));
			currPLToDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currPLToDate));
			rpt.setCurDateRange(currPLFromDate + " to " + currPLToDate);
			if (prevPLFromDate != null && !"".equals(prevPLFromDate) && prevPLToDate != null
					&& !"".equals(prevPLToDate)) {
				prevPLFromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevPLFromDate));
				prevPLToDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevPLToDate));
				rpt.setPrevDateRange(prevPLFromDate + " to " + prevPLToDate);
			}
		} catch (ParseException ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
					"date range given for P&L Calculation is wrong.", ex.getMessage());
		}
		return list;
	}
}
