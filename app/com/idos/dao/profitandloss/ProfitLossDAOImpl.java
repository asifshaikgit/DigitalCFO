package com.idos.dao.profitandloss;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.dao.TrialBalanceDAO;
import com.idos.util.IDOSException;
import model.*;
import model.PLBSCOAMap.PLBSCOAMap;

import org.apache.commons.lang3.StringUtils;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.profitloss.ProfitLossBean;

import com.idos.util.IdosConstants;
import org.json.JSONArray;
import org.json.JSONObject;
import play.db.jpa.JPAApi;
import play.libs.Json;

/**
 * @author Sunil Namdev
 */
public class ProfitLossDAOImpl implements ProfitLossDAO {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.idos.dao.profitandloss.ProfitLossDAO#displayProfitLoss(com.fasterxml.
	 * jackson.databind.node.ObjectNode, com.fasterxml.jackson.databind.JsonNode,
	 * model.Users, javax.persistence.EntityManager,
	 * javax.persistence.EntityTransaction)
	 */
	public ProfitLossBean displayProfitLoss(ObjectNode result, JsonNode json, Users user, EntityManager entityManager)
			throws IDOSException {
		String currPLFromDate = json.findValue("currPLFromDate") != null ? json.findValue("currPLFromDate").asText()
				: null;
		String currPLToDate = json.findValue("currPLToDate") != null ? json.findValue("currPLToDate").asText() : null;
		String prevPLFromDate = json.findValue("prevPLFromDate") != null ? json.findValue("prevPLFromDate").asText()
				: null;
		String prevPLToDate = json.findValue("prevPLToDate") != null ? json.findValue("prevPLToDate").asText() : null;
		try {
			currPLFromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currPLFromDate));
			currPLToDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(currPLToDate));
			if (prevPLFromDate != null && !"".equals(prevPLFromDate) && prevPLToDate != null
					&& !"".equals(prevPLToDate)) {
				prevPLFromDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevPLFromDate));
				prevPLToDate = IdosConstants.mysqldf.format(IdosConstants.idosdf.parse(prevPLToDate));
			}
		} catch (ParseException ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION,
					"date range given for P&L Calculation is wrong.", ex.getMessage());
		}
		final ProfitLossBean plBean = populateProfitLossBean(user, entityManager, currPLFromDate, currPLToDate,
				prevPLFromDate, prevPLToDate);

		// Update bean with intermediate step values needed to show on PL report.
		plBean.computeIntermidiateStepValues();
		ObjectMapper oMapper = new ObjectMapper();
		final JsonNode pljson = oMapper.convertValue(plBean, JsonNode.class);
		result.put("ProfitLossBean", pljson);
		// Result is added.
		result.put("result", true);
		return plBean;
	}

	private void setInventoryData(EntityManager entityManager, Users user, Double openBalCr, Double closeBalCr,
			Double openBalPr, Double closeBalPr, List<PLBSInventory> plbsInventoryList, int inventoryType) {
		PLBSInventory plbsInventory = null;
		if (plbsInventoryList.size() > 0) {
			plbsInventory = plbsInventoryList.get(0);
		} else {
			plbsInventory = new PLBSInventory();
			plbsInventory.setOrganization(user.getOrganization());
			plbsInventory.setInventoryType(inventoryType);
		}
		plbsInventory.setOpeningBalanceCr(openBalCr);
		plbsInventory.setClosingBalanceCr(closeBalCr);
		plbsInventory.setOpeningBalancePr(openBalPr);
		plbsInventory.setClosingBalancePr(closeBalPr);
		genericDao.saveOrUpdate(plbsInventory, user, entityManager);
	}

	public ObjectNode saveUpdateInventoryData(JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) {
		Double openRawMaterial = json.findValue("openRawMaterial") != null
				? json.findValue("openRawMaterial").asDouble()
				: 0D;
		Double closingRawMaterial = json.findValue("closingRawMaterial") != null
				? json.findValue("closingRawMaterial").asDouble()
				: 0D;
		Double prevopenRawMaterial = json.findValue("prevopenRawMaterial") != null
				? json.findValue("prevopenRawMaterial").asDouble()
				: 0D;
		Double prevclsingRawMaterial = json.findValue("prevclsingRawMaterial") != null
				? json.findValue("prevclsingRawMaterial").asDouble()
				: 0D;

		Double openconsumables = json.findValue("openconsumables") != null
				? json.findValue("openconsumables").asDouble()
				: 0D;
		Double closingconsumables = json.findValue("closingconsumables") != null
				? json.findValue("closingconsumables").asDouble()
				: 0D;
		Double prevopenconsumables = json.findValue("prevopenconsumables") != null
				? json.findValue("prevopenconsumables").asDouble()
				: 0D;
		Double prevclosingconsumables = json.findValue("prevclosingconsumables") != null
				? json.findValue("prevclosingconsumables").asDouble()
				: 0D;

		Double openfinishedGoods = json.findValue("openfinishedGoods") != null
				? json.findValue("openfinishedGoods").asDouble()
				: 0D;
		Double closingfinishedGoods = json.findValue("closingfinishedGoods") != null
				? json.findValue("closingfinishedGoods").asDouble()
				: 0D;
		Double prevopenfinishedGoods = json.findValue("prevopenfinishedGoods") != null
				? json.findValue("prevopenfinishedGoods").asDouble()
				: 0D;
		Double prevclosingfinishedGoods = json.findValue("prevclosingfinishedGoods") != null
				? json.findValue("prevclosingfinishedGoods").asDouble()
				: 0D;

		Double openworkInProgress = json.findValue("openworkInProgress") != null
				? json.findValue("openworkInProgress").asDouble()
				: 0D;
		Double closingworkInProgress = json.findValue("closingworkInProgress") != null
				? json.findValue("closingworkInProgress").asDouble()
				: 0D;
		Double prevopenworkInProgress = json.findValue("prevopenworkInProgress") != null
				? json.findValue("prevopenworkInProgress").asDouble()
				: 0D;
		Double prevclosingworkInProgress = json.findValue("prevclosingworkInProgress") != null
				? json.findValue("prevclosingworkInProgress").asDouble()
				: 0D;

		Double openstockInTrade = json.findValue("openstockInTrade") != null
				? json.findValue("openstockInTrade").asDouble()
				: 0D;
		Double closingstockInTrade = json.findValue("closingstockInTrade") != null
				? json.findValue("closingstockInTrade").asDouble()
				: 0D;
		Double prevopenstockInTrade = json.findValue("prevopenstockInTrade") != null
				? json.findValue("prevopenstockInTrade").asDouble()
				: 0D;
		Double prevclosingstockInTrade = json.findValue("prevclosingstockInTrade") != null
				? json.findValue("prevclosingstockInTrade").asDouble()
				: 0D;

		ObjectNode result = Json.newObject();
		Map<String, Object> criterias = new HashMap<String, Object>(2);
		for (int i = 0; i < 5; i++) {
			criterias.put("organization.id", user.getOrganization().getId());
			criterias.put("inventoryType", i + 1);
			criterias.put("presentStatus", 1);
			List<PLBSInventory> plbsInventoryList = genericDao.findByCriteria(PLBSInventory.class, criterias,
					entityManager);
			switch (i + 1) {
				case IdosConstants.RAW_MATERIAL:
					setInventoryData(entityManager, user, openRawMaterial, closingRawMaterial, prevopenRawMaterial,
							prevclsingRawMaterial, plbsInventoryList, i + 1);
					break;
				case IdosConstants.CONSUMABLES:
					setInventoryData(entityManager, user, openconsumables, closingconsumables, prevopenconsumables,
							prevclosingconsumables, plbsInventoryList, i + 1);
					break;
				case IdosConstants.FINISHED_GOODS:
					setInventoryData(entityManager, user, openfinishedGoods, closingfinishedGoods,
							prevopenfinishedGoods, prevclosingfinishedGoods, plbsInventoryList, i + 1);
					break;
				case IdosConstants.WORK_IN_PROGRESS:
					setInventoryData(entityManager, user, openworkInProgress, closingworkInProgress,
							prevopenworkInProgress, prevclosingworkInProgress, plbsInventoryList, i + 1);
					break;
				case IdosConstants.STOCK_IN_TRADE:
					setInventoryData(entityManager, user, openstockInTrade, closingstockInTrade, prevopenstockInTrade,
							prevclosingstockInTrade, plbsInventoryList, i + 1);
					break;
			}
			criterias.clear();
		}
		result.put("issuccess", 1);
		return result;
	}

	/**
	 * Method populateProfitLossBean - Fills the Profit Loss bean.
	 * 
	 * @param result
	 * @param json
	 * @param user
	 * @param entityManager
	 * @param entitytransaction
	 * @return
	 */
	@Override
	public ProfitLossBean populateProfitLossBean(Users user, EntityManager entityManager, String currPLFromDate,
			String currPLToDate, String prevPLFromDate, String prevPLToDate) throws IDOSException {
		Long organId = user.getOrganization().getId();
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE,
					"fromDate: " + currPLFromDate + " currToDate: " + currPLToDate + " organId: " + organId);
			log.log(Level.FINE, "HQL: " + curJpql);
		}
		Query query = entityManager.createNativeQuery(curJpql);
		query.setParameter(1, currPLFromDate);
		query.setParameter(2, currPLToDate);
		query.setParameter(3, organId);
		query.setParameter(4, organId);
		query.setParameter(5, currPLFromDate);
		query.setParameter(6, currPLToDate);
		query.setParameter(7, organId);
		query.setParameter(8, organId);
		List<Object[]> coaList = query.getResultList();
		ProfitLossBean profitLossBean = new ProfitLossBean();
		Double curIncome = null;
		Integer plbsHead = null;
		Double curr = 0D;
		Double curExp = null;

		for (Object[] plMapping : coaList) {
			if (null == plMapping[0])
				continue;
			plbsHead = (Integer) plMapping[0];
			curIncome = plMapping[1] == null ? 0.0 : ((Double) plMapping[1]);
			curExp = plMapping[3] == null ? 0.0 : ((Double) plMapping[3]);
			switch (plbsHead) {
				case IdosConstants.IN_REVENUE_FROM_OPERATIONS:
					curr = profitLossBean.getRevenueFrmOpers() == null ? 0 : profitLossBean.getRevenueFrmOpers();
					profitLossBean.setRevenueFrmOpers(curIncome + curr);
					break;
				case IdosConstants.IN_OTHER_INCOME:
					curr = profitLossBean.getOtherIncome() == null ? 0 : profitLossBean.getOtherIncome();
					profitLossBean.setOtherIncome(curIncome + curr);
					break;
				case IdosConstants.EX_COST_OF_MATERIALS_CONSUMED:
					curr = profitLossBean.getCostOfmatConsumed() == null ? 0 : profitLossBean.getCostOfmatConsumed();
					profitLossBean.setCostOfmatConsumed(curExp + curr);
					break;
				case IdosConstants.EX_PURCHASES_OF_STOCK_IN_TRADE:
					curr = profitLossBean.getPurchasesOfStockinTrade() == null ? 0
							: profitLossBean.getPurchasesOfStockinTrade();
					profitLossBean.setPurchasesOfStockinTrade(curExp + curr);
					break;
				case IdosConstants.EX_CIIOFGIP_AND_STOCK_IN_TRADE:
					curr = profitLossBean.getChnFinGud() == null ? 0 : profitLossBean.getChnFinGud();
					profitLossBean.setChnFinGud(curExp + curr);
					break;
				case IdosConstants.EX_EMPLOYEE_BENEFITS_EXPENSE:
					curr = profitLossBean.getEmpBenExp() == null ? 0 : profitLossBean.getEmpBenExp();
					profitLossBean.setEmpBenExp(curExp + curr);
					break;
				case IdosConstants.EX_FINANCE_COSTS:
					curr = profitLossBean.getFinanceCostsExp() == null ? 0 : profitLossBean.getFinanceCostsExp();
					profitLossBean.setFinanceCostsExp(curExp + curr);
					break;
				case IdosConstants.EX_DEPRECIATION_AND_AMORTIZATION:
					curr = profitLossBean.getDeprecAmtExp() == null ? 0 : profitLossBean.getDeprecAmtExp();
					profitLossBean.setDeprecAmtExp(curExp + curr);
					break;
				case IdosConstants.EX_OTHER_EXPENSES:
					curr = profitLossBean.getOthExp() == null ? 0 : profitLossBean.getOthExp();
					profitLossBean.setOthExp(curExp + curr);
					break;
				/*
				 * case "PROFIT BEFORE EXCEPTIONAL AND EXTRAORDINARY ITEMS AND TAX (III - IV)":
				 * profitLossBean.setProfBefrTx(curFig);
				 * break;
				 */
				case IdosConstants.EX_EXCEPTIONAL_ITEMS:
					curr = profitLossBean.getExpItems() == null ? 0 : profitLossBean.getExpItems();
					profitLossBean.setExpItems(curExp + curr);
					break;
				/*
				 * case "PROFIT BEFORE EXTRAORDINARY ITEMS AND TAX(V - VI)":
				 * profitLossBean.setProfExtItmTx(curFig);
				 * break;
				 */
				case IdosConstants.EX_EXTRAORDINARY_ITEMS:
					curr = profitLossBean.getExtrItms() == null ? 0 : profitLossBean.getExtrItms();
					profitLossBean.setExtrItms(curExp + curr);
					break;
				/*
				 * case "PROFIT BEFORE TAX (VII- VIII)":
				 * profitLossBean.setProfBefrTx(curFig);
				 * break;
				 */
				case IdosConstants.EX_CURRENT_TAX:
					curr = profitLossBean.getCurTx() == null ? 0 : profitLossBean.getCurTx();
					profitLossBean.setCurTx(curExp + curr);
					break;
				case IdosConstants.EX_DEFERRED_TAX:
					curr = profitLossBean.getDefTx() == null ? 0 : profitLossBean.getDefTx();
					profitLossBean.setDefTx(curExp + curr);
					break;
				/*
				 * case "PROFIT (LOSS) FOR THE PERIOD FROM CONTINUING OPERATIONS (VII-VIII)":
				 * profitLossBean.setProfContOprn(curFig);
				 * break;
				 * case "PROFIT/(LOSS) FROM DISCONTINUING OPERATIONS":
				 * {
				 * profitLossBean.setProfDisContOprn(curFig);
				 * break;
				 * }
				 * case "TAX EXPENSE OF DISCONTINUING OPERATIONS":
				 * {
				 * profitLossBean.setTxExpDisContOprn(curFig);
				 * break;
				 * }
				 * case "PROFIT/(LOSS) FROM DISCONTINUING OPERATIONS (AFTER TAX) (XII-XIII)":
				 * {
				 * profitLossBean.setProfDisContOprnAftTx(curFig);
				 * break;
				 * }
				 * case "PROFIT (LOSS) FOR THE PERIOD (XI + XIV)":
				 * {
				 * profitLossBean.setProfForPeriod(curFig);
				 * break;
				 * }
				 * case "EARNINGS PER EQUITY SHARE - BASIC":
				 * {
				 * profitLossBean.setEarnEqtShrBasic(curFig);
				 * break;
				 * }
				 * case "EARNINGS PER EQUITY SHARE - DILUTED":
				 * {
				 * profitLossBean.setEarnEqtShrDiluted(curFig);
				 * break;
				 * }
				 */

			}// End of switch.
		} // End of for loop.

		if (prevPLFromDate != null && !"".equals(prevPLFromDate) && prevPLToDate != null && !"".equals(prevPLToDate)) {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "prevPLFromDate: " + prevPLFromDate + " prevPLToDate: " + prevPLToDate
						+ " organId: " + organId);
				log.log(Level.FINE, "Prev HQL: " + prevJpql);
			}
			query = entityManager.createNativeQuery(prevJpql);
			query.setParameter(1, prevPLFromDate);
			query.setParameter(2, prevPLToDate);
			query.setParameter(3, organId);
			query.setParameter(4, organId);
			query.setParameter(5, prevPLFromDate);
			query.setParameter(6, prevPLToDate);
			query.setParameter(7, organId);
			query.setParameter(8, organId);
			coaList = null;
			coaList = query.getResultList();
			plbsHead = null;
			Double prevIncome = null;
			Double prev = 0D;
			Double prevExp = null;

			for (Object[] plMapping : coaList) {
				if (null == plMapping[0])
					continue;
				plbsHead = (Integer) plMapping[0];
				prevIncome = plMapping[2] == null ? 0.0 : ((Double) plMapping[2]);
				prevExp = plMapping[4] == null ? 0.0 : ((Double) plMapping[4]);

				switch (plbsHead) {
					case IdosConstants.IN_REVENUE_FROM_OPERATIONS:
						prev = profitLossBean.getRevenueFrmOpersPrvRpt() == null ? 0
								: profitLossBean.getRevenueFrmOpersPrvRpt();
						profitLossBean.setRevenueFrmOpersPrvRpt(prevIncome + prev);
						break;
					case IdosConstants.IN_OTHER_INCOME:
						prev = profitLossBean.getOtherIncomePrvRpt() == null ? 0
								: profitLossBean.getOtherIncomePrvRpt();
						profitLossBean.setOtherIncomePrvRpt(prevIncome + prev);
						break;
					case IdosConstants.EX_COST_OF_MATERIALS_CONSUMED:
						prev = profitLossBean.getCostOfmatConsumedPrvRpt() == null ? 0
								: profitLossBean.getCostOfmatConsumedPrvRpt();
						profitLossBean.setCostOfmatConsumedPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_PURCHASES_OF_STOCK_IN_TRADE:
						prev = profitLossBean.getPurchasesOfStockinTradePrvRpt() == null ? 0
								: profitLossBean.getPurchasesOfStockinTradePrvRpt();
						profitLossBean.setPurchasesOfStockinTradePrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_CIIOFGIP_AND_STOCK_IN_TRADE:
						prev = profitLossBean.getChnFinGudPrvRpt() == null ? 0 : profitLossBean.getChnFinGudPrvRpt();
						profitLossBean.setChnFinGudPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_EMPLOYEE_BENEFITS_EXPENSE:
						prev = profitLossBean.getEmpBenExpPrvRpt() == null ? 0 : profitLossBean.getEmpBenExpPrvRpt();
						profitLossBean.setEmpBenExpPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_FINANCE_COSTS:
						prev = profitLossBean.getFinanceCostsExpPrvRpt() == null ? 0
								: profitLossBean.getFinanceCostsExpPrvRpt();
						profitLossBean.setFinanceCostsExpPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_DEPRECIATION_AND_AMORTIZATION:
						prev = profitLossBean.getDeprecAmtExpPrvRpt() == null ? 0
								: profitLossBean.getDeprecAmtExpPrvRpt();
						profitLossBean.setDeprecAmtExpPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_OTHER_EXPENSES:
						prev = profitLossBean.getOthExpPrvRpt() == null ? 0 : profitLossBean.getOthExpPrvRpt();
						profitLossBean.setOthExpPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_EXCEPTIONAL_ITEMS:
						prev = profitLossBean.getExpItemsPrvRpt() == null ? 0 : profitLossBean.getExpItemsPrvRpt();
						profitLossBean.setExpItemsPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_EXTRAORDINARY_ITEMS:
						prev = profitLossBean.getExtrItmsPrvRpt() == null ? 0 : profitLossBean.getExtrItmsPrvRpt();
						profitLossBean.setExtrItmsPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_CURRENT_TAX:
						prev = profitLossBean.getCurTxPrvRpt() == null ? 0 : profitLossBean.getCurTxPrvRpt();
						profitLossBean.setCurTxPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_DEFERRED_TAX:
						prev = profitLossBean.getDefTxPrvRpt() == null ? 0 : profitLossBean.getDefTxPrvRpt();
						profitLossBean.setDefTxPrvRpt(prevExp + prev);
						break;
				}// End of switch.
			} // End of for loop.
		}
		calculateProfitLoss(profitLossBean, user, entityManager);
		return profitLossBean;
	}

	private void calculateProfitLoss(ProfitLossBean profitLossBean, Users user, EntityManager entityManager) {
		List<PLBSInventory> plbsInventoryList = null;
		Map<String, Object> criterias = new HashMap<String, Object>(2);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		plbsInventoryList = genericDao.findByCriteria(PLBSInventory.class, criterias, entityManager);
		for (PLBSInventory plbsInventory : plbsInventoryList) {
			if (IdosConstants.RAW_MATERIAL == plbsInventory.getInventoryType()
					|| IdosConstants.CONSUMABLES == plbsInventory.getInventoryType()) {
				Double costOfMaterialConsum = profitLossBean.getCostOfmatConsumed() == null ? 0
						: profitLossBean.getCostOfmatConsumed();
				costOfMaterialConsum += plbsInventory.getOpeningBalanceCr() - plbsInventory.getClosingBalanceCr();
				profitLossBean.setCostOfmatConsumed(costOfMaterialConsum);
				Double costOfMaterialConsumPr = profitLossBean.getCostOfmatConsumedPrvRpt() == null ? 0
						: profitLossBean.getCostOfmatConsumedPrvRpt();
				costOfMaterialConsumPr += plbsInventory.getOpeningBalancePr() - plbsInventory.getClosingBalancePr();
				profitLossBean.setCostOfmatConsumedPrvRpt(costOfMaterialConsumPr);
			} else if (IdosConstants.FINISHED_GOODS == plbsInventory.getInventoryType()
					|| IdosConstants.WORK_IN_PROGRESS == plbsInventory.getInventoryType()
					|| IdosConstants.STOCK_IN_TRADE == plbsInventory.getInventoryType()) {
				Double changeInInvfinishGooods = profitLossBean.getChnFinGud() == null ? 0
						: profitLossBean.getChnFinGud();
				changeInInvfinishGooods += plbsInventory.getOpeningBalanceCr() - plbsInventory.getClosingBalanceCr();
				profitLossBean.setChnFinGud(changeInInvfinishGooods);
				Double changeInInvfinishGooodsPr = profitLossBean.getChnFinGudPrvRpt() == null ? 0
						: profitLossBean.getChnFinGudPrvRpt();
				changeInInvfinishGooodsPr += plbsInventory.getOpeningBalancePr() - plbsInventory.getClosingBalancePr();
				profitLossBean.setChnFinGudPrvRpt(changeInInvfinishGooodsPr);
			}
		}
	}

	public ObjectNode displayInvetory(JsonNode json, Users user, EntityManager entityManager) {
		ObjectNode result = Json.newObject();

		List<PLBSInventory> plbsInventoryList = null;
		Map<String, Object> criterias = new HashMap<String, Object>(2);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		plbsInventoryList = genericDao.findByCriteria(PLBSInventory.class, criterias, entityManager);
		for (PLBSInventory plbsInventory : plbsInventoryList) {
			ArrayNode inventoryArray = result.putArray("row" + plbsInventory.getInventoryType());
			ObjectNode row = Json.newObject();
			row.put("openBalCr", plbsInventory.getOpeningBalanceCr());
			row.put("closeBalCr", plbsInventory.getClosingBalanceCr());
			row.put("openBalPr", plbsInventory.getOpeningBalancePr());
			row.put("closeBalPr", plbsInventory.getClosingBalancePr());
			inventoryArray.add(row);
		}
		return result;
	}

	@Override
	public ProfitLossBean getPnLWithoutOpeningBalanceOfItem(Users user, EntityManager entityManager,
			String currPLFromDate, String currPLToDate, String prevPLFromDate, String prevPLToDate)
			throws IDOSException {
		Long organId = user.getOrganization().getId();
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE,
					"fromDate: " + currPLFromDate + " currToDate: " + currPLToDate + " organId: " + organId);
			log.log(Level.FINE, "HQL: " + PROFIT_LOSS_JPQL_WITHOUT_OB);
		}
		Query query = entityManager.createNativeQuery(PROFIT_LOSS_JPQL_WITHOUT_OB);
		query.setParameter(1, currPLFromDate);
		query.setParameter(2, currPLToDate);
		query.setParameter(3, organId);
		query.setParameter(4, currPLFromDate);
		query.setParameter(5, currPLToDate);
		query.setParameter(6, organId);
		List<Object[]> coaList = query.getResultList();
		ProfitLossBean profitLossBean = new ProfitLossBean();
		Double curIncome = null;
		Integer plbsHead = null;
		Double curr = 0D;
		Double curExp = null;

		for (Object[] plMapping : coaList) {
			if (null == plMapping[0])
				continue;
			plbsHead = (Integer) plMapping[0];
			curIncome = plMapping[1] == null ? 0.0 : ((Double) plMapping[1]);
			curExp = plMapping[3] == null ? 0.0 : ((Double) plMapping[3]);
			switch (plbsHead) {
				case IdosConstants.IN_REVENUE_FROM_OPERATIONS:
					curr = profitLossBean.getRevenueFrmOpers() == null ? 0 : profitLossBean.getRevenueFrmOpers();
					profitLossBean.setRevenueFrmOpers(curIncome + curr);
					break;
				case IdosConstants.IN_OTHER_INCOME:
					curr = profitLossBean.getOtherIncome() == null ? 0 : profitLossBean.getOtherIncome();
					profitLossBean.setOtherIncome(curIncome + curr);
					break;
				case IdosConstants.EX_COST_OF_MATERIALS_CONSUMED:
					curr = profitLossBean.getCostOfmatConsumed() == null ? 0 : profitLossBean.getCostOfmatConsumed();
					profitLossBean.setCostOfmatConsumed(curExp + curr);
					break;
				case IdosConstants.EX_PURCHASES_OF_STOCK_IN_TRADE:
					curr = profitLossBean.getPurchasesOfStockinTrade() == null ? 0
							: profitLossBean.getPurchasesOfStockinTrade();
					profitLossBean.setPurchasesOfStockinTrade(curExp + curr);
					break;
				case IdosConstants.EX_CIIOFGIP_AND_STOCK_IN_TRADE:
					curr = profitLossBean.getChnFinGud() == null ? 0 : profitLossBean.getChnFinGud();
					profitLossBean.setChnFinGud(curExp + curr);
					break;
				case IdosConstants.EX_EMPLOYEE_BENEFITS_EXPENSE:
					curr = profitLossBean.getEmpBenExp() == null ? 0 : profitLossBean.getEmpBenExp();
					profitLossBean.setEmpBenExp(curExp + curr);
					break;
				case IdosConstants.EX_FINANCE_COSTS:
					curr = profitLossBean.getFinanceCostsExp() == null ? 0 : profitLossBean.getFinanceCostsExp();
					profitLossBean.setFinanceCostsExp(curExp + curr);
					break;
				case IdosConstants.EX_DEPRECIATION_AND_AMORTIZATION:
					curr = profitLossBean.getDeprecAmtExp() == null ? 0 : profitLossBean.getDeprecAmtExp();
					profitLossBean.setDeprecAmtExp(curExp + curr);
					break;
				case IdosConstants.EX_OTHER_EXPENSES:
					curr = profitLossBean.getOthExp() == null ? 0 : profitLossBean.getOthExp();
					profitLossBean.setOthExp(curExp + curr);
					break;
				/*
				 * case "PROFIT BEFORE EXCEPTIONAL AND EXTRAORDINARY ITEMS AND TAX (III - IV)":
				 * profitLossBean.setProfBefrTx(curFig);
				 * break;
				 */
				case IdosConstants.EX_EXCEPTIONAL_ITEMS:
					curr = profitLossBean.getExpItems() == null ? 0 : profitLossBean.getExpItems();
					profitLossBean.setExpItems(curExp + curr);
					break;
				/*
				 * case "PROFIT BEFORE EXTRAORDINARY ITEMS AND TAX(V - VI)":
				 * profitLossBean.setProfExtItmTx(curFig);
				 * break;
				 */
				case IdosConstants.EX_EXTRAORDINARY_ITEMS:
					curr = profitLossBean.getExtrItms() == null ? 0 : profitLossBean.getExtrItms();
					profitLossBean.setExtrItms(curExp + curr);
					break;
				/*
				 * case "PROFIT BEFORE TAX (VII- VIII)":
				 * profitLossBean.setProfBefrTx(curFig);
				 * break;
				 */
				case IdosConstants.EX_CURRENT_TAX:
					curr = profitLossBean.getCurTx() == null ? 0 : profitLossBean.getCurTx();
					profitLossBean.setCurTx(curExp + curr);
					break;
				case IdosConstants.EX_DEFERRED_TAX:
					curr = profitLossBean.getDefTx() == null ? 0 : profitLossBean.getDefTx();
					profitLossBean.setDefTx(curExp + curr);
					break;
			}// End of switch.
		} // End of for loop.

		if (prevPLFromDate != null && !"".equals(prevPLFromDate) && prevPLToDate != null && !"".equals(prevPLToDate)) {
			if (log.isLoggable(Level.FINE)) {
				log.log(Level.FINE, "prevPLFromDate: " + prevPLFromDate + " prevPLToDate: " + prevPLToDate
						+ " organId: " + organId);
				log.log(Level.FINE, "Prev HQL: " + prevJpql);
			}
			query = entityManager.createNativeQuery(prevJpql);
			query.setParameter(1, prevPLFromDate);
			query.setParameter(2, prevPLToDate);
			query.setParameter(3, organId);
			query.setParameter(4, organId);
			query.setParameter(5, prevPLFromDate);
			query.setParameter(6, prevPLToDate);
			query.setParameter(7, organId);
			query.setParameter(8, organId);
			coaList = null;
			coaList = query.getResultList();
			plbsHead = null;
			Double prevIncome = null;
			Double prev = 0D;
			Double prevExp = null;

			for (Object[] plMapping : coaList) {
				if (null == plMapping[0])
					continue;
				plbsHead = (Integer) plMapping[0];
				prevIncome = plMapping[2] == null ? 0.0 : ((Double) plMapping[2]);
				prevExp = plMapping[4] == null ? 0.0 : ((Double) plMapping[4]);

				switch (plbsHead) {
					case IdosConstants.IN_REVENUE_FROM_OPERATIONS:
						prev = profitLossBean.getRevenueFrmOpersPrvRpt() == null ? 0
								: profitLossBean.getRevenueFrmOpersPrvRpt();
						profitLossBean.setRevenueFrmOpersPrvRpt(prevIncome + prev);
						break;
					case IdosConstants.IN_OTHER_INCOME:
						prev = profitLossBean.getOtherIncomePrvRpt() == null ? 0
								: profitLossBean.getOtherIncomePrvRpt();
						profitLossBean.setOtherIncomePrvRpt(prevIncome + prev);
						break;
					case IdosConstants.EX_COST_OF_MATERIALS_CONSUMED:
						prev = profitLossBean.getCostOfmatConsumedPrvRpt() == null ? 0
								: profitLossBean.getCostOfmatConsumedPrvRpt();
						profitLossBean.setCostOfmatConsumedPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_PURCHASES_OF_STOCK_IN_TRADE:
						prev = profitLossBean.getPurchasesOfStockinTradePrvRpt() == null ? 0
								: profitLossBean.getPurchasesOfStockinTradePrvRpt();
						profitLossBean.setPurchasesOfStockinTradePrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_CIIOFGIP_AND_STOCK_IN_TRADE:
						prev = profitLossBean.getChnFinGudPrvRpt() == null ? 0 : profitLossBean.getChnFinGudPrvRpt();
						profitLossBean.setChnFinGudPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_EMPLOYEE_BENEFITS_EXPENSE:
						prev = profitLossBean.getEmpBenExpPrvRpt() == null ? 0 : profitLossBean.getEmpBenExpPrvRpt();
						profitLossBean.setEmpBenExpPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_FINANCE_COSTS:
						prev = profitLossBean.getFinanceCostsExpPrvRpt() == null ? 0
								: profitLossBean.getFinanceCostsExpPrvRpt();
						profitLossBean.setFinanceCostsExpPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_DEPRECIATION_AND_AMORTIZATION:
						prev = profitLossBean.getDeprecAmtExpPrvRpt() == null ? 0
								: profitLossBean.getDeprecAmtExpPrvRpt();
						profitLossBean.setDeprecAmtExpPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_OTHER_EXPENSES:
						prev = profitLossBean.getOthExpPrvRpt() == null ? 0 : profitLossBean.getOthExpPrvRpt();
						profitLossBean.setOthExpPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_EXCEPTIONAL_ITEMS:
						prev = profitLossBean.getExpItemsPrvRpt() == null ? 0 : profitLossBean.getExpItemsPrvRpt();
						profitLossBean.setExpItemsPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_EXTRAORDINARY_ITEMS:
						prev = profitLossBean.getExtrItmsPrvRpt() == null ? 0 : profitLossBean.getExtrItmsPrvRpt();
						profitLossBean.setExtrItmsPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_CURRENT_TAX:
						prev = profitLossBean.getCurTxPrvRpt() == null ? 0 : profitLossBean.getCurTxPrvRpt();
						profitLossBean.setCurTxPrvRpt(prevExp + prev);
						break;
					case IdosConstants.EX_DEFERRED_TAX:
						prev = profitLossBean.getDefTxPrvRpt() == null ? 0 : profitLossBean.getDefTxPrvRpt();
						profitLossBean.setDefTxPrvRpt(prevExp + prev);
						break;
				}// End of switch.
			} // End of for loop.
		}
		calculateProfitLoss(profitLossBean, user, entityManager);
		return profitLossBean;
	}
}
