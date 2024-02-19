package service.plbscoamapperservice;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.idos.dao.GenericDAO;
import com.idos.dao.GenericJpaDAO;
import com.idos.dao.plbscoamapper.PLBSCOAMapperDAO;
import com.idos.dao.plbscoamapper.PLBSCOAMapperDAOImpl;
import java.util.logging.Level;
import com.idos.util.IdosConstants;
import model.BaseModel;
import model.PLBSCOAMap.PLBSCOAMap;
import model.Users;
import model.UsersRoles;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PLBSCOAMapperServiceImpl implements PLBSCOAMapperService {
	@Override
	public <T extends BaseModel> void savePLBSCOAMapping(final Long orgId, final int plBsHead, final String coaIds,
			final Users user, EntityManager entityManager) {
		plbscoaMapperDao.savePLBSCOAMapping(orgId, plBsHead, coaIds, user, entityManager);
	}

	@Override
	public ObjectNode fetchPLBSCOAMapping(Users user, EntityManager entityManager) {
		if (log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, ">>>>>> Start " + user);
		}
		ObjectNode result = Json.newObject();
		ArrayNode revenueFromOperations = result.putArray("revenueFromOperations");
		ArrayNode otherIncome = result.putArray("otherIncome");

		ArrayNode costOfMaterialsConsumed = result.putArray("costOfMaterialsConsumed");
		ArrayNode purchasesOfStockInTrade = result.putArray("purchasesOfStockInTrade");
		ArrayNode cIIOfFGWIPAndStockInTrade = result.putArray("cIIOfFGWIPAndStockInTrade");
		ArrayNode empBenefitsExpenseFinCosts = result.putArray("empBenefitsExpenseFinCosts");
		ArrayNode financeCosts = result.putArray("financeCosts");
		ArrayNode depreciationAmortization = result.putArray("depreciationAmortization");
		ArrayNode otherExpenses = result.putArray("otherExpenses");
		ArrayNode exceptionalItems = result.putArray("exceptionalItems");
		ArrayNode extraordinaryItems = result.putArray("extraordinaryItems");
		ArrayNode currentTax = result.putArray("currentTax");
		ArrayNode deferredTax = result.putArray("deferredTax");

		ArrayNode tangibleAssets = result.putArray("tangibleAssets");
		ArrayNode intangibleAssets = result.putArray("intangibleAssets");
		ArrayNode capitalWorkInProgress = result.putArray("capitalWorkInProgress");
		ArrayNode intangibleAssetsUnderDev = result.putArray("intangibleAssetsUnderDev");
		ArrayNode nonCurrentInvestments = result.putArray("nonCurrentInvestments");
		ArrayNode deferredTaxAssets = result.putArray("deferredTaxAssets");
		ArrayNode longTermLoansAndAdvances = result.putArray("longTermLoansAndAdvances");
		ArrayNode otherNonCurrentAssets = result.putArray("otherNonCurrentAssets");
		ArrayNode currentInvestments = result.putArray("currentInvestments");
		ArrayNode inventories = result.putArray("inventories");
		ArrayNode tradeReceivables = result.putArray("tradeReceivables");
		ArrayNode cashAndCashEquivalents = result.putArray("cashAndCashEquivalents");
		ArrayNode shortTermLoansAndAdvances = result.putArray("shortTermLoansAndAdvances");
		ArrayNode otherCurrentAssets = result.putArray("otherCurrentAssets");

		ArrayNode shareCapital = result.putArray("shareCapital");
		ArrayNode reservesAndSurplus = result.putArray("reservesAndSurplus");
		ArrayNode moneyRecAgainstShareWarrants = result.putArray("moneyRecAgainstShareWarrants");
		ArrayNode shareApplMoneyPendingAllotment = result.putArray("shareApplMoneyPendingAllotment");
		ArrayNode longTermBorrowings = result.putArray("longTermBorrowings");
		ArrayNode deferredTaxLiabilities = result.putArray("deferredTaxLiabilities");
		ArrayNode otherLongTermLiabilities = result.putArray("otherLongTermLiabilities");
		ArrayNode longTermProvisions = result.putArray("longTermProvisions");
		ArrayNode shortTermBorrowings = result.putArray("shortTermBorrowings");
		ArrayNode tradePayables = result.putArray("tradePayables");
		ArrayNode otherCurrentLiabilities = result.putArray("otherCurrentLiabilities");
		ArrayNode shortTermProvisions = result.putArray("shortTermProvisions");

		Map<String, Object> criterias = new HashMap<String, Object>(1);
		criterias.put("organization.id", user.getOrganization().getId());
		criterias.put("presentStatus", 1);
		List<PLBSCOAMap> plbscoamapList = genericDAO.findByCriteria(PLBSCOAMap.class, criterias, entityManager);
		for (PLBSCOAMap plbscoaMap : plbscoamapList) {
			switch (plbscoaMap.getPlbsHead()) {
				case IdosConstants.IN_REVENUE_FROM_OPERATIONS:
					revenueFromOperations.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.IN_OTHER_INCOME:
					otherIncome.add(plbscoaMap.getCoaId());
					break;

				case IdosConstants.EX_COST_OF_MATERIALS_CONSUMED:
					costOfMaterialsConsumed.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.EX_PURCHASES_OF_STOCK_IN_TRADE:
					purchasesOfStockInTrade.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.EX_CIIOFGIP_AND_STOCK_IN_TRADE:
					cIIOfFGWIPAndStockInTrade.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.EX_EMPLOYEE_BENEFITS_EXPENSE:
					empBenefitsExpenseFinCosts.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.EX_FINANCE_COSTS:
					financeCosts.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.EX_DEPRECIATION_AND_AMORTIZATION:
					depreciationAmortization.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.EX_OTHER_EXPENSES:
					otherExpenses.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.EX_EXCEPTIONAL_ITEMS:
					exceptionalItems.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.EX_EXTRAORDINARY_ITEMS:
					extraordinaryItems.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.EX_CURRENT_TAX:
					currentTax.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.EX_DEFERRED_TAX:
					deferredTax.add(plbscoaMap.getCoaId());
					break;

				// Start assets
				case IdosConstants.AS_TANGIBLE_ASSETS:
					tangibleAssets.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_INTANGIBLE_ASSETS:
					intangibleAssets.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_CAPITAL_WORK_IN_PROGRESS:
					capitalWorkInProgress.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_INTANGIBLE_ASSETS_UNDER_DEV:
					intangibleAssetsUnderDev.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_NON_CURRENT_INVESTMENTS:
					nonCurrentInvestments.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_DEFERRED_TAX_ASSETS_NET:
					deferredTaxAssets.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_LONG_TERM_LOANS_AND_ADVANCES:
					longTermLoansAndAdvances.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_OTHER_NON_CURRENT_ASSETS:
					otherNonCurrentAssets.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_CURRENT_INVESTMENTS:
					currentInvestments.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_INVENTORIES:
					inventories.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_TRADE_RECEIVABLES:
					tradeReceivables.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_CASH_AND_CASH_EQUIVALENTS:
					cashAndCashEquivalents.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_SHORT_TERM_LOANS_AND_ADVANCES:
					shortTermLoansAndAdvances.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.AS_OTHER_CURRENT_ASSETS:
					otherCurrentAssets.add(plbscoaMap.getCoaId());
					break;

				// Start Liabilties
				case IdosConstants.LI_SHARE_CAPITAL:
					shareCapital.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.LI_RESERVES_AND_SURPLUS:
					reservesAndSurplus.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.LI_MONEY_RECEIVED_AGAINST_SHARE_WARRANTS:
					moneyRecAgainstShareWarrants.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.LI_SHARE_APPLICATION_MONEY_PENDING_ALLOTMENT:
					shareApplMoneyPendingAllotment.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.LI_LONG_TERM_BORROWINGS:
					longTermBorrowings.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.LI_DEFERRED_TAX_LIABILITIES_NET:
					deferredTaxLiabilities.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.LI_OTHER_LONG_TERM_LIABILITIES:
					otherLongTermLiabilities.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.LI_LONG_TERM_PROVISIONS:
					longTermProvisions.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.LI_SHORT_TERM_BORROWINGS:
					shortTermBorrowings.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.LI_TRADE_PAYABLES:
					tradePayables.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.LI_OTHER_CURRENT_LIABILITIES:
					otherCurrentLiabilities.add(plbscoaMap.getCoaId());
					break;
				case IdosConstants.LI_SHORT_TERM_PROVISIONS:
					shortTermProvisions.add(plbscoaMap.getCoaId());
					break;
			}
		}
		return result;
	}
}
