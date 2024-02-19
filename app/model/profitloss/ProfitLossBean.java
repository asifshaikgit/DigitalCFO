package model.profitloss;

import java.io.Serializable;

public class ProfitLossBean implements Serializable {

	//Revenue from operations
	private Double revenueFrmOpers = 0D;
	
	//Other Income
	private Double otherIncome = 0D;

	//Total Revenue
	private Double totRevenue = 0D;

	//Cost of materials consumed
	private Double costOfmatConsumed = 0D;
	
	//Purchases of Stock-in-Trade
	private Double purchasesOfStockinTrade = 0D;

	//changes in inventories of finished goods work-in-progress and Stock-in-Trade
	private Double chnFinGud = 0D;
	
	//Employee benefits expense
	private Double empBenExp = 0D;

	//Finance costs
	private Double financeCostsExp = 0D;

	//Depreciation and amortization expense
	private Double deprecAmtExp = 0D;
	
	//other expenses
	private Double othExp = 0D;
	
	//Total expenses
	private Double totExp = 0D;
	
	//Profit before exceptional and extraordinary items and tax
	private Double profitExItmTx = 0D;
	
	//Exceptional items
	private Double expItems = 0D;
	
	//Profit before extraordinary items and tax 
	private Double profExtItmTx = 0D;
	
	//Extraordinary items
	private Double extrItms = 0D;
	
	//Profit before tax
	private Double profBefrTx = 0D;
	
	//Current tax
	private Double curTx = 0D;
	
	//Deferred tax
	private Double defTx = 0D;
	
	//Profit (Loss) for the period from continuing operations (VII-VIII)
	//private Double profContOprn = 0D;
	
	//Profit/(loss) from discontinuing operations
	//private Double profDisContOprn = 0D;
	
	//Tax expense of discontinuing operations
	//private Double txExpDisContOprn = 0D;
	
	//Profit/(loss) from Discontinuing operations (after tax) (XII-XIII)
	//private Double profDisContOprnAftTx = 0D;
	
	//Profit (Loss) for the period (XI + XIV)
	private Double profForPeriod = 0D;
	
	//Earnings per equity share - Basic
	private Double earnEqtShrBasic = 0D;
	
	//Earnings per equity shareDiluted
	private Double earnEqtShrDiluted = 0D;

	private Double openingBalanceReservesAndSurplus; // should not be used in PL & BS directly

	// Previous report duration --------
	//Revenue from operations
	private Double revenueFrmOpersPrvRpt = 0D;
	
	//Other Income
	private Double otherIncomePrvRpt = 0D;

	//Total Revenue
	private Double totRevenuePrvRpt = 0D;

	//Cost of materials consumed
	private Double costOfmatConsumedPrvRpt = 0D;
	
	//Purchases of Stock-in-Trade
	private Double purchasesOfStockinTradePrvRpt = 0D;

	//changes in inventories of finished goods work-in-progress and Stock-in-Trade
	private Double chnFinGudPrvRpt = 0D;
	
	//Employee benefits expense
	private Double empBenExpPrvRpt = 0D;

	//Finance costs
	private Double financeCostsExpPrvRpt = 0D;
	
	//Depreciation and amortization expense
	private Double deprecAmtExpPrvRpt = 0D;
	
	//other expenses
	private Double othExpPrvRpt = 0D;
	
	//Total expenses
	private Double totExpPrvRpt = 0D;
	
	//Profit before exceptional and extraordinary items and tax
	private Double profitExItmTxPrvRpt = 0D;
	
	//Exceptional items
	private Double expItemsPrvRpt = 0D;
	
	//Profit before extraordinary items and tax 
	private Double profExtItmTxPrvRpt = 0D;
	
	//Extraordinary items
	private Double extrItmsPrvRpt = 0D;
	
	//Profit before tax
	private Double profBefrTxPrvRpt = 0D;
	
	//Current tax
	private Double curTxPrvRpt = 0D;
	
	//Deferred tax
	private Double defTxPrvRpt = 0D;
	
	//Profit (Loss) for the period from continuing operations (VII-VIII)
	//private Double profContOprnPrvRpt = 0D;
	
	//Profit/(loss) from discontinuing operations
	//private Double profDisContOprnPrvRpt = 0D;
	
	//Tax expense of discontinuing operations
	//private Double txExpDisContOprnPrvRpt = 0D;
	
	//Profit/(loss) from Discontinuing operations (after tax) (XII-XIII)
	//private Double profDisContOprnAftTxPrvRpt = 0D;
	
	//Profit (Loss) for the period (XI + XIV)
	private Double profForPeriodPrvRpt = 0D;
	
	//Earnings per equity share - Basic
	private Double earnEqtShrBasicPrvRpt = 0D;
	
	//Earnings per equity shareDiluted
	private Double earnEqtShrDilutedPrvRpt = 0D;

	private Double openingBalanceReservesAndSurplusPrevRpt; // should not be used in PL & BS directly
	
	/**
	 * @return the revenueFrmOpers
	 */
	public final Double getRevenueFrmOpers() {
		return revenueFrmOpers;
	}

	/**
	 * @param revenueFrmOpers the revenueFrmOpers to set
	 */
	public final void setRevenueFrmOpers(Double revenueFrmOpers) {
		this.revenueFrmOpers = revenueFrmOpers;
	}

	/**
	 * @return the otherIncome
	 */
	public final Double getOtherIncome() {
		return otherIncome;
	}

	/**
	 * @param otherIncome the otherIncome to set
	 */
	public final void setOtherIncome(Double otherIncome) {
		this.otherIncome = otherIncome;
	}

	/**
	 * @return the totRevenue
	 */
	public final Double getTotRevenue() {
		return totRevenue;
	}

	/**
	 * @param totRevenue the totRevenue to set
	 */
	public final void setTotRevenue(Double totRevenue) {
		this.totRevenue = totRevenue;
	}

	/**
	 * @return the costOfmatConsumed
	 */
	public final Double getCostOfmatConsumed() {
		return costOfmatConsumed;
	}

	/**
	 * @param costOfmatConsumed the costOfmatConsumed to set
	 */
	public final void setCostOfmatConsumed(Double costOfmatConsumed) {
		this.costOfmatConsumed = costOfmatConsumed;
	}

	/**
	 * @return the purchasesOfStockinTrade
	 */
	public final Double getPurchasesOfStockinTrade() {
		return purchasesOfStockinTrade;
	}

	/**
	 * @param purchasesOfStockinTrade the purchasesOfStockinTrade to set
	 */
	public final void setPurchasesOfStockinTrade(Double purchasesOfStockinTrade) {
		this.purchasesOfStockinTrade = purchasesOfStockinTrade;
	}

	/**
	 * @return the chnFinGud
	 */
	public final Double getChnFinGud() {
		return chnFinGud;
	}

	/**
	 * @param chnFinGud the chnFinGud to set
	 */
	public final void setChnFinGud(Double chnFinGud) {
		this.chnFinGud = chnFinGud;
	}

	/**
	 * @return the empBenExp
	 */
	public final Double getEmpBenExp() {
		return empBenExp;
	}

	/**
	 * @param empBenExp the empBenExp to set
	 */
	public final void setEmpBenExp(Double empBenExp) {
		this.empBenExp = empBenExp;
	}

	/**
	 * @return the deprecAmtExp
	 */
	public final Double getDeprecAmtExp() {
		return deprecAmtExp;
	}

	/**
	 * @param deprecAmtExp the deprecAmtExp to set
	 */
	public final void setDeprecAmtExp(Double deprecAmtExp) {
		this.deprecAmtExp = deprecAmtExp;
	}

	/**
	 * @return the othExp
	 */
	public final Double getOthExp() {
		return othExp;
	}

	/**
	 * @param othExp the othExp to set
	 */
	public final void setOthExp(Double othExp) {
		this.othExp = othExp;
	}

	/**
	 * @return the totExp
	 */
	public final Double getTotExp() {
		return totExp;
	}

	/**
	 * @param totExp the totExp to set
	 */
	public final void setTotExp(Double totExp) {
		this.totExp = totExp;
	}

	/**
	 * @return the profitExItmTx
	 */
	public final Double getProfitExItmTx() {
		return profitExItmTx;
	}

	/**
	 * @param profitExItmTx the profitExItmTx to set
	 */
	public final void setProfitExItmTx(Double profitExItmTx) {
		this.profitExItmTx = profitExItmTx;
	}

	/**
	 * @return the expItems
	 */
	public final Double getExpItems() {
		return expItems;
	}

	/**
	 * @param expItems the expItems to set
	 */
	public final void setExpItems(Double expItems) {
		this.expItems = expItems;
	}

	/**
	 * @return the profExtItmTx
	 */
	public final Double getProfExtItmTx() {
		return profExtItmTx;
	}

	/**
	 * @param profExtItmTx the profExtItmTx to set
	 */
	public final void setProfExtItmTx(Double profExtItmTx) {
		this.profExtItmTx = profExtItmTx;
	}

	/**
	 * @return the extrItms
	 */
	public final Double getExtrItms() {
		return extrItms;
	}

	/**
	 * @param extrItms the extrItms to set
	 */
	public final void setExtrItms(Double extrItms) {
		this.extrItms = extrItms;
	}

	/**
	 * @return the profBefrTx
	 */
	public final Double getProfBefrTx() {
		return profBefrTx;
	}

	/**
	 * @param profBefrTx the profBefrTx to set
	 */
	public final void setProfBefrTx(Double profBefrTx) {
		this.profBefrTx = profBefrTx;
	}

	/**
	 * @return the curTx
	 */
	public final Double getCurTx() {
		return curTx;
	}

	/**
	 * @param curTx the curTx to set
	 */
	public final void setCurTx(Double curTx) {
		this.curTx = curTx;
	}

	/**
	 * @return the defTx
	 */
	public final Double getDefTx() {
		return defTx;
	}

	/**
	 * @param defTx the defTx to set
	 */
	public final void setDefTx(Double defTx) {
		this.defTx = defTx;
	}

	/*
	public final Double getProfContOprn() {
		return profContOprn;
	}

	public final void setProfContOprn(Double profContOprn) {
		this.profContOprn = profContOprn;
	}

	public final Double getProfDisContOprn() {
		return profDisContOprn;
	}

	public final void setProfDisContOprn(Double profDisContOprn) {
		this.profDisContOprn = profDisContOprn;
	}

	public final Double getTxExpDisContOprn() {
		return txExpDisContOprn;
	}

	public final void setTxExpDisContOprn(Double txExpDisContOprn) {
		this.txExpDisContOprn = txExpDisContOprn;
	}

	public final Double getProfDisContOprnAftTx() {
		return profDisContOprnAftTx;
	}

	public final void setProfDisContOprnAftTx(Double profDisContOprnAftTx) {
		this.profDisContOprnAftTx = profDisContOprnAftTx;
	} */

	/**
	 * @return the profForPeriod
	 */
	public final Double getProfForPeriod() {
		return profForPeriod;
	}

	/**
	 * @param profForPeriod the profForPeriod to set
	 */
	public final void setProfForPeriod(Double profForPeriod) {
		this.profForPeriod = profForPeriod;
	}

	/**
	 * @return the earnEqtShrBasic
	 */
	public final Double getEarnEqtShrBasic() {
		return earnEqtShrBasic;
	}

	/**
	 * @param earnEqtShrBasic the earnEqtShrBasic to set
	 */
	public final void setEarnEqtShrBasic(Double earnEqtShrBasic) {
		this.earnEqtShrBasic = earnEqtShrBasic;
	}

	/**
	 * @return the earnEqtShrDiluted
	 */
	public final Double getEarnEqtShrDiluted() {
		return earnEqtShrDiluted;
	}

	/**
	 * @param earnEqtShrDiluted the earnEqtShrDiluted to set
	 */
	public final void setEarnEqtShrDiluted(Double earnEqtShrDiluted) {
		this.earnEqtShrDiluted = earnEqtShrDiluted;
	}

	/**
	 * @return the revenueFrmOpersPrvRpt
	 */
	public final Double getRevenueFrmOpersPrvRpt() {
		return revenueFrmOpersPrvRpt;
	}

	/**
	 * @param revenueFrmOpersPrvRpt the revenueFrmOpersPrvRpt to set
	 */
	public final void setRevenueFrmOpersPrvRpt(Double revenueFrmOpersPrvRpt) {
		this.revenueFrmOpersPrvRpt = revenueFrmOpersPrvRpt;
	}

	/**
	 * @return the otherIncomePrvRpt
	 */
	public final Double getOtherIncomePrvRpt() {
		return otherIncomePrvRpt;
	}

	/**
	 * @param otherIncomePrvRpt the otherIncomePrvRpt to set
	 */
	public final void setOtherIncomePrvRpt(Double otherIncomePrvRpt) {
		this.otherIncomePrvRpt = otherIncomePrvRpt;
	}

	/**
	 * @return the totRevenuePrvRpt
	 */
	public final Double getTotRevenuePrvRpt() {
		return totRevenuePrvRpt;
	}

	/**
	 * @param totRevenuePrvRpt the totRevenuePrvRpt to set
	 */
	public final void setTotRevenuePrvRpt(Double totRevenuePrvRpt) {
		this.totRevenuePrvRpt = totRevenuePrvRpt;
	}

	/**
	 * @return the costOfmatConsumedPrvRpt
	 */
	public final Double getCostOfmatConsumedPrvRpt() {
		return costOfmatConsumedPrvRpt;
	}

	/**
	 * @param costOfmatConsumedPrvRpt the costOfmatConsumedPrvRpt to set
	 */
	public final void setCostOfmatConsumedPrvRpt(Double costOfmatConsumedPrvRpt) {
		this.costOfmatConsumedPrvRpt = costOfmatConsumedPrvRpt;
	}

	/**
	 * @return the purchasesOfStockinTradePrvRpt
	 */
	public final Double getPurchasesOfStockinTradePrvRpt() {
		return purchasesOfStockinTradePrvRpt;
	}

	/**
	 * @param purchasesOfStockinTradePrvRpt the purchasesOfStockinTradePrvRpt to set
	 */
	public final void setPurchasesOfStockinTradePrvRpt(Double purchasesOfStockinTradePrvRpt) {
		this.purchasesOfStockinTradePrvRpt = purchasesOfStockinTradePrvRpt;
	}

	/**
	 * @return the chnFinGudPrvRpt
	 */
	public final Double getChnFinGudPrvRpt() {
		return chnFinGudPrvRpt;
	}

	/**
	 * @param chnFinGudPrvRpt the chnFinGudPrvRpt to set
	 */
	public final void setChnFinGudPrvRpt(Double chnFinGudPrvRpt) {
		this.chnFinGudPrvRpt = chnFinGudPrvRpt;
	}

	/**
	 * @return the empBenExpPrvRpt
	 */
	public final Double getEmpBenExpPrvRpt() {
		return empBenExpPrvRpt;
	}

	public Double getFinanceCostsExp() {
		return this.financeCostsExp;
	}

	public void setFinanceCostsExp(Double financeCostsExp) {
		this.financeCostsExp = financeCostsExp;
	}

	public Double getFinanceCostsExpPrvRpt() {
		return this.financeCostsExpPrvRpt;
	}

	public void setFinanceCostsExpPrvRpt(Double financeCostsExpPrvRpt) {
		this.financeCostsExpPrvRpt = financeCostsExpPrvRpt;
	}

	/**
	 * @param empBenExpPrvRpt the empBenExpPrvRpt to set
	 */
	public final void setEmpBenExpPrvRpt(Double empBenExpPrvRpt) {
		this.empBenExpPrvRpt = empBenExpPrvRpt;
	}

	/**
	 * @return the deprecAmtExpPrvRpt
	 */
	public final Double getDeprecAmtExpPrvRpt() {
		return deprecAmtExpPrvRpt;
	}

	/**
	 * @param deprecAmtExpPrvRpt the deprecAmtExpPrvRpt to set
	 */
	public final void setDeprecAmtExpPrvRpt(Double deprecAmtExpPrvRpt) {
		this.deprecAmtExpPrvRpt = deprecAmtExpPrvRpt;
	}

	/**
	 * @return the othExpPrvRpt
	 */
	public final Double getOthExpPrvRpt() {
		return othExpPrvRpt;
	}

	/**
	 * @param othExpPrvRpt the othExpPrvRpt to set
	 */
	public final void setOthExpPrvRpt(Double othExpPrvRpt) {
		this.othExpPrvRpt = othExpPrvRpt;
	}

	/**
	 * @return the totExpPrvRpt
	 */
	public final Double getTotExpPrvRpt() {
		return totExpPrvRpt;
	}

	/**
	 * @param totExpPrvRpt the totExpPrvRpt to set
	 */
	public final void setTotExpPrvRpt(Double totExpPrvRpt) {
		this.totExpPrvRpt = totExpPrvRpt;
	}

	/**
	 * @return the profitExItmTxPrvRpt
	 */
	public final Double getProfitExItmTxPrvRpt() {
		return profitExItmTxPrvRpt;
	}

	/**
	 * @param profitExItmTxPrvRpt the profitExItmTxPrvRpt to set
	 */
	public final void setProfitExItmTxPrvRpt(Double profitExItmTxPrvRpt) {
		this.profitExItmTxPrvRpt = profitExItmTxPrvRpt;
	}

	/**
	 * @return the expItemsPrvRpt
	 */
	public final Double getExpItemsPrvRpt() {
		return expItemsPrvRpt;
	}

	/**
	 * @param expItemsPrvRpt the expItemsPrvRpt to set
	 */
	public final void setExpItemsPrvRpt(Double expItemsPrvRpt) {
		this.expItemsPrvRpt = expItemsPrvRpt;
	}

	/**
	 * @return the profExtItmTxPrvRpt
	 */
	public final Double getProfExtItmTxPrvRpt() {
		return profExtItmTxPrvRpt;
	}

	/**
	 * @param profExtItmTxPrvRpt the profExtItmTxPrvRpt to set
	 */
	public final void setProfExtItmTxPrvRpt(Double profExtItmTxPrvRpt) {
		this.profExtItmTxPrvRpt = profExtItmTxPrvRpt;
	}

	/**
	 * @return the extrItmsPrvRpt
	 */
	public final Double getExtrItmsPrvRpt() {
		return extrItmsPrvRpt;
	}

	/**
	 * @param extrItmsPrvRpt the extrItmsPrvRpt to set
	 */
	public final void setExtrItmsPrvRpt(Double extrItmsPrvRpt) {
		this.extrItmsPrvRpt = extrItmsPrvRpt;
	}

	/**
	 * @return the profBefrTxPrvRpt
	 */
	public final Double getProfBefrTxPrvRpt() {
		return profBefrTxPrvRpt;
	}

	/**
	 * @param profBefrTxPrvRpt the profBefrTxPrvRpt to set
	 */
	public final void setProfBefrTxPrvRpt(Double profBefrTxPrvRpt) {
		this.profBefrTxPrvRpt = profBefrTxPrvRpt;
	}

	/**
	 * @return the curTxPrvRpt
	 */
	public final Double getCurTxPrvRpt() {
		return curTxPrvRpt;
	}

	/**
	 * @param curTxPrvRpt the curTxPrvRpt to set
	 */
	public final void setCurTxPrvRpt(Double curTxPrvRpt) {
		this.curTxPrvRpt = curTxPrvRpt;
	}

	/**
	 * @return the defTxPrvRpt
	 */
	public final Double getDefTxPrvRpt() {
		return defTxPrvRpt;
	}

	/**
	 * @param defTxPrvRpt the defTxPrvRpt to set
	 */
	public final void setDefTxPrvRpt(Double defTxPrvRpt) {
		this.defTxPrvRpt = defTxPrvRpt;
	}

	/*
	 * @return the profContOprnPrvRpt
	 *
	public final Double getProfContOprnPrvRpt() {
		return profContOprnPrvRpt;
	}

	public final void setProfContOprnPrvRpt(Double profContOprnPrvRpt) {
		this.profContOprnPrvRpt = profContOprnPrvRpt;
	}
	*/

	/**
	 * @return the profDisContOprnPrvRpt
	 */
	//public final Double getProfDisContOprnPrvRpt() { 	return profDisContOprnPrvRpt; }

	/**
	 * @param profDisContOprnPrvRpt the profDisContOprnPrvRpt to set
	 */
	//public final void setProfDisContOprnPrvRpt(Double profDisContOprnPrvRpt) {
	//	this.profDisContOprnPrvRpt = profDisContOprnPrvRpt;
	//}

	/**
	 * @return the txExpDisContOprnPrvRpt
	 */
	//public final Double getTxExpDisContOprnPrvRpt() { 	return txExpDisContOprnPrvRpt; }

	/**
	 * @param txExpDisContOprnPrvRpt the txExpDisContOprnPrvRpt to set
	 */
	//public final void setTxExpDisContOprnPrvRpt(Double txExpDisContOprnPrvRpt) { this.txExpDisContOprnPrvRpt = txExpDisContOprnPrvRpt; }

	/**
	 * @return the profDisContOprnAftTxPrvRpt
	 */
	//public final Double getProfDisContOprnAftTxPrvRpt() {
	//	return profDisContOprnAftTxPrvRpt;
	//}

	/**
	 * @param profDisContOprnAftTxPrvRpt the profDisContOprnAftTxPrvRpt to set
	 */
	//public final void setProfDisContOprnAftTxPrvRpt(Double profDisContOprnAftTxPrvRpt) {
	//	this.profDisContOprnAftTxPrvRpt = profDisContOprnAftTxPrvRpt;
	//}

	/**
	 * @return the profForPeriodPrvRpt
	 */
	public final Double getProfForPeriodPrvRpt() {
		return profForPeriodPrvRpt;
	}

	/**
	 * @param profForPeriodPrvRpt the profForPeriodPrvRpt to set
	 */
	public final void setProfForPeriodPrvRpt(Double profForPeriodPrvRpt) {
		this.profForPeriodPrvRpt = profForPeriodPrvRpt;
	}

	/**
	 * @return the earnEqtShrBasicPrvRpt
	 */
	public final Double getEarnEqtShrBasicPrvRpt() {
		return earnEqtShrBasicPrvRpt;
	}

	/**
	 * @param earnEqtShrBasicPrvRpt the earnEqtShrBasicPrvRpt to set
	 */
	public final void setEarnEqtShrBasicPrvRpt(Double earnEqtShrBasicPrvRpt) {
		this.earnEqtShrBasicPrvRpt = earnEqtShrBasicPrvRpt;
	}

	/**
	 * @return the earnEqtShrDilutedPrvRpt
	 */
	public final Double getEarnEqtShrDilutedPrvRpt() {
		return earnEqtShrDilutedPrvRpt;
	}

	/**
	 * @param earnEqtShrDilutedPrvRpt the earnEqtShrDilutedPrvRpt to set
	 */
	public final void setEarnEqtShrDilutedPrvRpt(Double earnEqtShrDilutedPrvRpt) {
		this.earnEqtShrDilutedPrvRpt = earnEqtShrDilutedPrvRpt;
	}
	
	/**
	 * Method computeIntermidiateStepValues - calculates the intermidiate values used to show on the
	 * profit and loss report.
	 * @return
	 */
	public void computeIntermidiateStepValues(){
		//Total Revenue
		this.totRevenue = this.revenueFrmOpers + this.otherIncome;
		
		//Total Expenses
		this.totExp = this.costOfmatConsumed
					+ this.purchasesOfStockinTrade
					+ this.chnFinGud
					+ this.financeCostsExp
					+ this.empBenExp
					+ this.deprecAmtExp
					+ this.othExp;
		
		//Profit before exceptional and extraordinary items and tax
		this.profitExItmTx = this.totRevenue - this.totExp;
				
		//Profit Before Extraordinary Items And Tax 
		this.profExtItmTx = this.profitExItmTx- this.expItems;
				
		//Profit Before Tax
		this.profBefrTx = this.profExtItmTx - this.extrItms;
		
		//Profit (Loss) For The Period From Continuing Operations
		//this.profContOprn = this.profExtItmTx - this.extrItms;
		
		//Profit/(Loss) From Discontinuing Operations (After Tax)
		//this.profDisContOprnAftTx = this.profDisContOprn - this.txExpDisContOprn;
		
		//Profit (Loss) For The Period 
		//this.profForPeriod = this.profContOprn + this.profDisContOprnAftTx;

		this.profForPeriod = this.profBefrTx - (this.curTx + this.defTx);
		
		// ---- For previous report ---------
		
		//Total Revenue
		this.totRevenuePrvRpt = this.revenueFrmOpersPrvRpt + this.otherIncomePrvRpt;
		
		//Total Expenses
		this.totExpPrvRpt = this.costOfmatConsumedPrvRpt + this.purchasesOfStockinTradePrvRpt + this.chnFinGudPrvRpt
				+ this.empBenExpPrvRpt + this.financeCostsExpPrvRpt + this.deprecAmtExpPrvRpt + this.othExpPrvRpt;
		
		//Profit before exceptional and extraordinary items and tax
		this.profitExItmTxPrvRpt = this.totRevenuePrvRpt - this.totExpPrvRpt;
				
		//Profit Before Extraordinary Items And Tax 
		this.profExtItmTxPrvRpt = this.profitExItmTxPrvRpt - this.expItemsPrvRpt;
				
		//Profit Before Tax
		this.profBefrTxPrvRpt = this.profExtItmTxPrvRpt - this.extrItmsPrvRpt;
		
		//Profit (Loss) For The Period From Continuing Operations
		//this.profContOprnPrvRpt = this.profExtItmTxPrvRpt - this.extrItmsPrvRpt;
		
		//Profit/(Loss) From Discontinuing Operations (After Tax)
		//this.profDisContOprnAftTxPrvRpt = this.profDisContOprnPrvRpt - this.txExpDisContOprnPrvRpt;
		
		//Profit (Loss) For The Period 
		//this.profForPeriodPrvRpt = this.profContOprnPrvRpt + this.profDisContOprnAftTxPrvRpt;

		//Profit (Loss) For The Period
		this.profForPeriodPrvRpt = this.profBefrTxPrvRpt - (this.curTxPrvRpt + this.defTxPrvRpt)  ;
	}

	public Double getOpeningBalanceReservesAndSurplus() {
		return this.openingBalanceReservesAndSurplus;
	}

	public void setOpeningBalanceReservesAndSurplus(final Double openingBalanceReservesAndSurplus) {
		this.openingBalanceReservesAndSurplus = openingBalanceReservesAndSurplus;
	}

	public Double getOpeningBalanceReservesAndSurplusPrevRpt() {
		return this.openingBalanceReservesAndSurplusPrevRpt;
	}

	public void setOpeningBalanceReservesAndSurplusPrevRpt(final Double openingBalanceReservesAndSurplusPrevRpt) {
		this.openingBalanceReservesAndSurplusPrevRpt = openingBalanceReservesAndSurplusPrevRpt;
	}
}
