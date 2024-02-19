package service.ProfitLoss;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.logging.Level;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.ProfitNLossReport;
import model.profitloss.ProfitLossBean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.idos.dao.profitandloss.ProfitLossDAO;
import com.idos.dao.profitandloss.ProfitLossDAOImpl;

import model.Users;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sunil Namdev
 */
public class ProfitLossServiceImpl implements ProfitLossService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * service.ProfitLoss.ProfitLossService#displayProfitLossReport(com.fasterxml.
	 * jackson.databind.node.ObjectNode, com.fasterxml.jackson.databind.JsonNode,
	 * model.Users, javax.persistence.EntityManager,
	 * javax.persistence.EntityTransaction)
	 */
	@Override
	public ProfitLossBean displayProfitLossReport(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) throws IDOSException {
		return PROFIT_LOSS_DAO.displayProfitLoss(result, json, user, entityManager);
	}

	@Override
	public ObjectNode saveUpdateInventoryData(JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) {
		ObjectNode result = PROFIT_LOSS_DAO.saveUpdateInventoryData(json, user, entityManager, entitytransaction);
		return result;
	}

	@Override
	public ObjectNode displayInvetory(JsonNode json, Users user, EntityManager entityManager) {
		return PROFIT_LOSS_DAO.displayInvetory(json, user, entityManager);
	}

	@Override
	public List<ProfitNLossReport> getProfitNLossData(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) throws IDOSException {
		List<ProfitNLossReport> list = null;
		ProfitLossBean profitLossBean = PROFIT_LOSS_DAO.displayProfitLoss(result, json, user, entityManager);
		if (profitLossBean != null) {
			profitLossBean.computeIntermidiateStepValues();
			list = new ArrayList<ProfitNLossReport>(20);
			ProfitNLossReport rptData = new ProfitNLossReport();
			rptData.setScheduleNo("I");
			rptData.setParticularName("Revenue from operations");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getRevenueFrmOpers()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getRevenueFrmOpersPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setScheduleNo("II");
			rptData.setParticularName("Other Income");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getOtherIncome()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getOtherIncomePrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setScheduleNo("III");
			rptData.setParticularName("Total Revenue (I + II)");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getTotRevenue()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getTotRevenuePrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setScheduleNo("IV");
			rptData.setParticularName("Expenses");
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setParticularName("    (a) Cost of materials consumed");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getCostOfmatConsumed()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getCostOfmatConsumedPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setParticularName("    (b) Purchases of Stock-in-Trade");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getPurchasesOfStockinTrade()));
			rptData.setPreviousValue(
					IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getPurchasesOfStockinTradePrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setParticularName(
					"    (c) Changes in inventories of finished goods work-in-progress and Stock-in-Trade");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getChnFinGud()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getChnFinGudPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setParticularName("    (d) Employee benefits expense");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getEmpBenExp()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getEmpBenExpPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setParticularName("    (e) Finance costs");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getFinanceCostsExp()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getFinanceCostsExpPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setParticularName("    (f) Depreciation and amortization expense");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getDeprecAmtExp()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getDeprecAmtExpPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setParticularName("    (g) Other expenses");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getOthExp()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getOthExpPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setParticularName("Total expenses");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getTotExp()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getTotExpPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setScheduleNo("V");
			rptData.setParticularName("Profit before exceptional and extraordinary items and tax (III - IV)");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getProfExtItmTx()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getProfExtItmTxPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setScheduleNo("VI");
			rptData.setParticularName("Exceptional items");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getExpItems()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getExpItemsPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setScheduleNo("VII");
			rptData.setParticularName("Profit before extraordinary items and tax (V - VI)");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getProfExtItmTx()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getProfExtItmTxPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setScheduleNo("VIII");
			rptData.setParticularName("Extraordinary items");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getExtrItms()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getExtrItmsPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setScheduleNo("IX");
			rptData.setParticularName("Profit before tax (VII- VIII)");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getProfBefrTx()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getProfBefrTxPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setScheduleNo("X");
			rptData.setParticularName("Tax expense:");
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setParticularName("    (1) Current tax");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getCurTx()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getCurTxPrvRpt()));
			list.add(rptData);

			rptData = new ProfitNLossReport();
			rptData.setParticularName("    (2) Deferred tax");
			rptData.setCurrentValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getDefTx()));
			rptData.setPreviousValue(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getDefTxPrvRpt()));
			rptData.setTotalPnL(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getProfForPeriod()));
			rptData.setTotalPnLPrev(IdosConstants.DECIMAL_FORMAT2.format(profitLossBean.getProfForPeriodPrvRpt()));
			list.add(rptData);
		}

		String currPLFromDate = json.findValue("currPLFromDate") != null ? json.findValue("currPLFromDate").asText()
				: null;
		String currPLToDate = json.findValue("currPLToDate") != null ? json.findValue("currPLToDate").asText() : null;
		String prevPLFromDate = json.findValue("prevPLFromDate") != null ? json.findValue("prevPLFromDate").asText()
				: null;
		String prevPLToDate = json.findValue("prevPLToDate") != null ? json.findValue("prevPLToDate").asText() : null;
		try {
			ProfitNLossReport rpt = list.get(0);
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
