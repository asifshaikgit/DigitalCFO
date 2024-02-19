package model.balancesheet;

import java.io.Serializable;

public class BalanceSheetBean implements Serializable{
	private Double shareholderFundsTotal = 0D;
	private Double shareCapital = 0D;
	private Double reservesAndSurplus = 0D;
	private Double profitLossForPeriod = 0D;
	private Double profitLossForPeriodPlusReservesSurplus = 0D;
	private Double moneyReceivedAgainstShareWarrants = 0D;

	private Double shareApplicationMoneyPendingAllotment = 0D;

	private Double nonCurrentLiabilitiesTotal = 0D;
	private Double longTermBorrowings = 0D;
	private Double deferredTaxLiabilitiesNet = 0D;
	private Double otherLongTermLiabilities = 0D;
	private Double longTermProvisions = 0D;

	private Double currentLiabilitiesTotal = 0D;
	private Double shortTermBorrowings = 0D;
	private Double tradePayables = 0D;
	private Double otherCurrentLiabilities = 0D;
	private Double shortTermProvisions = 0D;
	private Double liabilityTotal = 0D;

	private Double shareholderFundsTotalPrvRpt = 0D;
	private Double shareCapitalPrvRpt = 0D;
	private Double reservesAndSurplusPrvRpt = 0D;
	private Double profitLossForPeriodPrvRpt = 0D;
	private Double profitLossForPeriodPlusReservesSurplusPrvRpt = 0D;
	private Double moneyReceivedAgainstShareWarrantsPrvRpt = 0D;

	private Double shareApplicationMoneyPendingAllotmentPrvRpt = 0D;

	private Double nonCurrentLiabilitiesTotalPrvRpt = 0D;
	private Double longTermBorrowingsPrvRpt = 0D;
	private Double deferredTaxLiabilitiesNetPrvRpt = 0D;
	private Double otherLongTermLiabilitiesPrvRpt = 0D;
	private Double longTermProvisionsPrvRpt = 0D;

	private Double currentLiabilitiesTotalPrvRpt = 0D;
	private Double shortTermBorrowingsPrvRpt = 0D;
	private Double tradePayablesPrvRpt = 0D;
	private Double otherCurrentLiabilitiesPrvRpt = 0D;
	private Double shortTermProvisionsPrvRpt = 0D;
	private Double liabilityTotalPrvRpt = 0D;

	/*************************************************************************/
	private Double nonCurrentAssetsTotal = 0D;
	private Double tangibleAssets = 0D;
	private Double intangibleAssets = 0D;
	private Double capitalWorkInProgress = 0D;
	private Double intangibleAssetsUnderDevelopment = 0D;
	private Double nonCurrentInvestments = 0D;
	private Double deferredTaxAssetsNet = 0D;
	private Double longTermLoansAndAdvances = 0D;
	private Double otherNonCurrentAssets = 0D;

	private Double currentAssetsTotal = 0D;
	private Double currentInvestments = 0D;
	private Double inventories = 0D;
	private Double tradeReceivables = 0D;
	private Double cashAndCashEquivalents = 0D;
	private Double shortTermLoansAndAdvances = 0D;
	private Double otherCurrentAssets = 0D;

	private Double assetTotal = 0D;

	private Double nonCurrentAssetsTotalPrvRpt = 0D;
	private Double tangibleAssetsPrvRpt = 0D;
	private Double intangibleAssetsPrvRpt = 0D;
	private Double capitalWorkInProgressPrvRpt = 0D;
	private Double intangibleAssetsUnderDevelopmentPrvRpt = 0D;
	private Double nonCurrentInvestmentsPrvRpt = 0D;
	private Double deferredTaxAssetsNetPrvRpt = 0D;
	private Double longTermLoansAndAdvancesPrvRpt = 0D;
	private Double otherNonCurrentAssetsPrvRpt = 0D;

	private Double currentAssetsTotalPrvRpt = 0D;
	private Double currentInvestmentsPrvRpt = 0D;
	private Double inventoriesPrvRpt = 0D;
	private Double tradeReceivablesPrvRpt = 0D;
	private Double cashAndCashEquivalentsPrvRpt = 0D;
	private Double shortTermLoansAndAdvancesPrvRpt = 0D;
	private Double otherCurrentAssetsPrvRpt = 0D;

	private Double assetTotalPrvRpt = 0D;


	public Double getCurrentLiabilitiesTotal() {
		return this.currentLiabilitiesTotal;
	}

	public void setCurrentLiabilitiesTotal(Double currentLiabilitiesTotal) {
		this.currentLiabilitiesTotal = currentLiabilitiesTotal;
	}

	public Double getCurrentLiabilitiesTotalPrvRpt() {
		return this.currentLiabilitiesTotalPrvRpt;
	}

	public void setCurrentLiabilitiesTotalPrvRpt(Double currentLiabilitiesTotalPrvRpt) {
		this.currentLiabilitiesTotalPrvRpt = currentLiabilitiesTotalPrvRpt;
	}

	public Double getNonCurrentAssetsTotal() {
		return this.nonCurrentAssetsTotal;
	}

	public void setNonCurrentAssetsTotal(Double nonCurrentAssetsTotal) {
		this.nonCurrentAssetsTotal = nonCurrentAssetsTotal;
	}

	public Double getCurrentAssetsTotal() {
		return this.currentAssetsTotal;
	}

	public void setCurrentAssetsTotal(Double currentAssetsTotal) {
		this.currentAssetsTotal = currentAssetsTotal;
	}

	public Double getNonCurrentAssetsTotalPrvRpt() {
		return this.nonCurrentAssetsTotalPrvRpt;
	}

	public void setNonCurrentAssetsTotalPrvRpt(Double nonCurrentAssetsTotalPrvRpt) {
		this.nonCurrentAssetsTotalPrvRpt = nonCurrentAssetsTotalPrvRpt;
	}

	public Double getCurrentAssetsTotalPrvRpt() {
		return this.currentAssetsTotalPrvRpt;
	}

	public void setCurrentAssetsTotalPrvRpt(Double currentAssetsTotalPrvRpt) {
		this.currentAssetsTotalPrvRpt = currentAssetsTotalPrvRpt;
	}




	public Double getNonCurrentLiabilitiesTotalPrvRpt() {
		return this.nonCurrentLiabilitiesTotalPrvRpt;
	}

	public void setNonCurrentLiabilitiesTotalPrvRpt(Double nonCurrentLiabilitiesTotalPrvRpt) {
		this.nonCurrentLiabilitiesTotalPrvRpt = nonCurrentLiabilitiesTotalPrvRpt;
	}






	public Double getShareholderFundsTotal() {
		return this.shareholderFundsTotal;
	}

	public void setShareholderFundsTotal(Double shareholderFundsTotal) {
		this.shareholderFundsTotal = shareholderFundsTotal;
	}
	/**
	 * @return the shareCapital
	 */
	public final Double getShareCapital() {
		return shareCapital;
	}
	/**
	 * @param shareCapital the shareCapital to set
	 */
	public final void setShareCapital(Double shareCapital) {
		this.shareCapital = shareCapital;
	}
	/**
	 * @return the reservesAndSurplus
	 */
	public final Double getReservesAndSurplus() {
		return reservesAndSurplus;
	}
	/**
	 * @param reservesAndSurplus the reservesAndSurplus to set
	 */
	public final void setReservesAndSurplus(Double reservesAndSurplus) {
		this.reservesAndSurplus = reservesAndSurplus;
	}

	public Double getProfitLossForPeriod() {
		return this.profitLossForPeriod;
	}

	public void setProfitLossForPeriod(Double profitLossForPeriod) {
		this.profitLossForPeriod = profitLossForPeriod;
	}

	public Double getProfitLossForPeriodPlusReservesSurplus() {
		return this.profitLossForPeriodPlusReservesSurplus;
	}

	public void setProfitLossForPeriodPlusReservesSurplus(Double profitLossForPeriodPlusReservesSurplus) {
		this.profitLossForPeriodPlusReservesSurplus = profitLossForPeriodPlusReservesSurplus;
	}

	/**
	 * @return the moneyReceivedAgainstShareWarrants
	 */
	public final Double getMoneyReceivedAgainstShareWarrants() {
		return moneyReceivedAgainstShareWarrants;
	}
	/**
	 * @param moneyReceivedAgainstShareWarrants the moneyReceivedAgainstShareWarrants to set
	 */
	public final void setMoneyReceivedAgainstShareWarrants(Double moneyReceivedAgainstShareWarrants) {
		this.moneyReceivedAgainstShareWarrants = moneyReceivedAgainstShareWarrants;
	}
	/**
	 * @return the shareApplicationMoneyPendingAllotment
	 */
	public final Double getShareApplicationMoneyPendingAllotment() {
		return shareApplicationMoneyPendingAllotment;
	}
	/**
	 * @param shareApplicationMoneyPendingAllotment the shareApplicationMoneyPendingAllotment to set
	 */
	public final void setShareApplicationMoneyPendingAllotment(Double shareApplicationMoneyPendingAllotment) {
		this.shareApplicationMoneyPendingAllotment = shareApplicationMoneyPendingAllotment;
	}

	public Double getNonCurrentLiabilitiesTotal() {
		return this.nonCurrentLiabilitiesTotal;
	}

	public void setNonCurrentLiabilitiesTotal(Double nonCurrentLiabilitiesTotal) {
		this.nonCurrentLiabilitiesTotal = nonCurrentLiabilitiesTotal;
	}
	/**
	 * @return the longTermBorrowings
	 */
	public final Double getLongTermBorrowings() {
		return longTermBorrowings;
	}
	/**
	 * @param longTermBorrowings the longTermBorrowings to set
	 */
	public final void setLongTermBorrowings(Double longTermBorrowings) {
		this.longTermBorrowings = longTermBorrowings;
	}
	/**
	 * @return the deferredTaxLiabilitiesNet
	 */
	public final Double getDeferredTaxLiabilitiesNet() {
		return deferredTaxLiabilitiesNet;
	}
	/**
	 * @param deferredTaxLiabilitiesNet the deferredTaxLiabilitiesNet to set
	 */
	public final void setDeferredTaxLiabilitiesNet(Double deferredTaxLiabilitiesNet) {
		this.deferredTaxLiabilitiesNet = deferredTaxLiabilitiesNet;
	}
	/**
	 * @return the otherLongTermLiabilities
	 */
	public final Double getOtherLongTermLiabilities() {
		return otherLongTermLiabilities;
	}
	/**
	 * @param otherLongTermLiabilities the otherLongTermLiabilities to set
	 */
	public final void setOtherLongTermLiabilities(Double otherLongTermLiabilities) {
		this.otherLongTermLiabilities = otherLongTermLiabilities;
	}
	/**
	 * @return the longTermProvisions
	 */
	public final Double getLongTermProvisions() {
		return longTermProvisions;
	}
	/**
	 * @param longTermProvisions the longTermProvisions to set
	 */
	public final void setLongTermProvisions(Double longTermProvisions) {
		this.longTermProvisions = longTermProvisions;
	}
	/**
	 * @return the shortTermBorrowings
	 */
	public final Double getShortTermBorrowings() {
		return shortTermBorrowings;
	}
	/**
	 * @param shortTermBorrowings the shortTermBorrowings to set
	 */
	public final void setShortTermBorrowings(Double shortTermBorrowings) {
		this.shortTermBorrowings = shortTermBorrowings;
	}
	/**
	 * @return the tradePayables
	 */
	public final Double getTradePayables() {
		return tradePayables;
	}
	/**
	 * @param tradePayables the tradePayables to set
	 */
	public final void setTradePayables(Double tradePayables) {
		this.tradePayables = tradePayables;
	}
	/**
	 * @return the otherCurrentLiabilities
	 */
	public final Double getOtherCurrentLiabilities() {
		return otherCurrentLiabilities;
	}
	/**
	 * @param otherCurrentLiabilities the otherCurrentLiabilities to set
	 */
	public final void setOtherCurrentLiabilities(Double otherCurrentLiabilities) {
		this.otherCurrentLiabilities = otherCurrentLiabilities;
	}
	/**
	 * @return the shortTermProvisions
	 */
	public final Double getShortTermProvisions() {
		return shortTermProvisions;
	}
	/**
	 * @param shortTermProvisions the shortTermProvisions to set
	 */
	public final void setShortTermProvisions(Double shortTermProvisions) {
		this.shortTermProvisions = shortTermProvisions;
	}
	/**
	 * @return the tangibleAssets
	 */
	public final Double getTangibleAssets() {
		return tangibleAssets;
	}
	/**
	 * @param tangibleAssets the tangibleAssets to set
	 */
	public final void setTangibleAssets(Double tangibleAssets) {
		this.tangibleAssets = tangibleAssets;
	}
	/**
	 * @return the intangibleAssets
	 */
	public final Double getIntangibleAssets() {
		return intangibleAssets;
	}
	/**
	 * @param intangibleAssets the intangibleAssets to set
	 */
	public final void setIntangibleAssets(Double intangibleAssets) {
		this.intangibleAssets = intangibleAssets;
	}
	/**
	 * @return the capitalWorkInProgress
	 */
	public final Double getCapitalWorkInProgress() {
		return capitalWorkInProgress;
	}
	/**
	 * @param capitalWorkInProgress the capitalWorkInProgress to set
	 */
	public final void setCapitalWorkInProgress(Double capitalWorkInProgress) {
		this.capitalWorkInProgress = capitalWorkInProgress;
	}
	/**
	 * @return the intangibleAssetsUnderDevelopment
	 */
	public final Double getIntangibleAssetsUnderDevelopment() {
		return intangibleAssetsUnderDevelopment;
	}
	/**
	 * @param intangibleAssetsUnderDevelopment the intangibleAssetsUnderDevelopment to set
	 */
	public final void setIntangibleAssetsUnderDevelopment(Double intangibleAssetsUnderDevelopment) {
		this.intangibleAssetsUnderDevelopment = intangibleAssetsUnderDevelopment;
	}
	/**
	 * @return the nonCurrentInvestments
	 */
	public final Double getNonCurrentInvestments() {
		return nonCurrentInvestments;
	}
	/**
	 * @param nonCurrentInvestments the nonCurrentInvestments to set
	 */
	public final void setNonCurrentInvestments(Double nonCurrentInvestments) {
		this.nonCurrentInvestments = nonCurrentInvestments;
	}
	/**
	 * @return the deferredTaxAssetsNet
	 */
	public final Double getDeferredTaxAssetsNet() {
		return deferredTaxAssetsNet;
	}
	/**
	 * @param deferredTaxAssetsNet the deferredTaxAssetsNet to set
	 */
	public final void setDeferredTaxAssetsNet(Double deferredTaxAssetsNet) {
		this.deferredTaxAssetsNet = deferredTaxAssetsNet;
	}
	/**
	 * @return the longTermLoansAndAdvances
	 */
	public final Double getLongTermLoansAndAdvances() {
		return longTermLoansAndAdvances;
	}
	/**
	 * @param longTermLoansAndAdvances the longTermLoansAndAdvances to set
	 */
	public final void setLongTermLoansAndAdvances(Double longTermLoansAndAdvances) {
		this.longTermLoansAndAdvances = longTermLoansAndAdvances;
	}
	/**
	 * @return the otherNonCurrentAssets
	 */
	public final Double getOtherNonCurrentAssets() {
		return otherNonCurrentAssets;
	}
	/**
	 * @param otherNonCurrentAssets the otherNonCurrentAssets to set
	 */
	public final void setOtherNonCurrentAssets(Double otherNonCurrentAssets) {
		this.otherNonCurrentAssets = otherNonCurrentAssets;
	}
	/**
	 * @return the currentInvestments
	 */
	public final Double getCurrentInvestments() {
		return currentInvestments;
	}
	/**
	 * @param currentInvestments the currentInvestments to set
	 */
	public final void setCurrentInvestments(Double currentInvestments) {
		this.currentInvestments = currentInvestments;
	}
	/**
	 * @return the inventories
	 */
	public final Double getInventories() {
		return inventories;
	}
	/**
	 * @param inventories the inventories to set
	 */
	public final void setInventories(Double inventories) {
		this.inventories = inventories;
	}
	/**
	 * @return the tradeReceivables
	 */
	public final Double getTradeReceivables() {
		return tradeReceivables;
	}
	/**
	 * @param tradeReceivables the tradeReceivables to set
	 */
	public final void setTradeReceivables(Double tradeReceivables) {
		this.tradeReceivables = tradeReceivables;
	}
	/**
	 * @return the cashAndCashEquivalents
	 */
	public final Double getCashAndCashEquivalents() {
		return cashAndCashEquivalents;
	}
	/**
	 * @param cashAndCashEquivalents the cashAndCashEquivalents to set
	 */
	public final void setCashAndCashEquivalents(Double cashAndCashEquivalents) {
		this.cashAndCashEquivalents = cashAndCashEquivalents;
	}
	/**
	 * @return the shortTermLoansAndAdvances
	 */
	public final Double getShortTermLoansAndAdvances() {
		return shortTermLoansAndAdvances;
	}
	/**
	 * @param shortTermLoansAndAdvances the shortTermLoansAndAdvances to set
	 */
	public final void setShortTermLoansAndAdvances(Double shortTermLoansAndAdvances) {
		this.shortTermLoansAndAdvances = shortTermLoansAndAdvances;
	}
	/**
	 * @return the otherCurrentAssets
	 */
	public final Double getOtherCurrentAssets() {
		return otherCurrentAssets;
	}
	/**
	 * @param otherCurrentAssets the otherCurrentAssets to set
	 */
	public final void setOtherCurrentAssets(Double otherCurrentAssets) {
		this.otherCurrentAssets = otherCurrentAssets;
	}

	/******************************************************************************************************************/
	public Double getShareholderFundsTotalPrvRpt() {
		return this.shareholderFundsTotalPrvRpt;
	}

	public void setShareholderFundsTotalPrvRpt(Double shareholderFundsTotalPrvRpt) {
		this.shareholderFundsTotalPrvRpt = shareholderFundsTotalPrvRpt;
	}

	/**
	 * @return the shareCapitalPrvRpt
	 */
	public final Double getShareCapitalPrvRpt() {
		return shareCapitalPrvRpt;
	}
	/**
	 * @param shareCapitalPrvRpt the shareCapitalPrvRpt to set
	 */
	public final void setShareCapitalPrvRpt(Double shareCapitalPrvRptPrvRpt) {
		this.shareCapitalPrvRpt = shareCapitalPrvRptPrvRpt;
	}
	/**
	 * @return the reservesAndSurplusPrvRpt
	 */
	public final Double getReservesAndSurplusPrvRpt() {
		return reservesAndSurplusPrvRpt;
	}
	/**
	 * @param reservesAndSurplusPrvRpt the reservesAndSurplusPrvRpt to set
	 */
	public final void setReservesAndSurplusPrvRpt(Double reservesAndSurplusPrvRpt) {
		this.reservesAndSurplusPrvRpt = reservesAndSurplusPrvRpt;
	}
	public Double getProfitLossForPeriodPrvRpt() {
		return this.profitLossForPeriodPrvRpt;
	}

	public void setProfitLossForPeriodPrvRpt(Double profitLossForPeriodPrvRpt) {
		this.profitLossForPeriodPrvRpt = profitLossForPeriodPrvRpt;
	}
	public Double getProfitLossForPeriodPlusReservesSurplusPrvRpt() {
		return this.profitLossForPeriodPlusReservesSurplusPrvRpt;
	}

	public void setProfitLossForPeriodPlusReservesSurplusPrvRpt(Double profitLossForPeriodPlusReservesSurplusPrvRpt) {
		this.profitLossForPeriodPlusReservesSurplusPrvRpt = profitLossForPeriodPlusReservesSurplusPrvRpt;
	}
	/**
	 * @return the moneyReceivedAgainstShareWarrantsPrvRpt
	 */
	public final Double getMoneyReceivedAgainstShareWarrantsPrvRpt() {
		return moneyReceivedAgainstShareWarrantsPrvRpt;
	}
	/**
	 * @param moneyReceivedAgainstShareWarrantsPrvRpt the moneyReceivedAgainstShareWarrantsPrvRpt to set
	 */
	public final void setMoneyReceivedAgainstShareWarrantsPrvRpt(Double moneyReceivedAgainstShareWarrantsPrvRpt) {
		this.moneyReceivedAgainstShareWarrantsPrvRpt = moneyReceivedAgainstShareWarrantsPrvRpt;
	}
	/**
	 * @return the shareApplicationMoneyPendingAllotmentPrvRpt
	 */
	public final Double getShareApplicationMoneyPendingAllotmentPrvRpt() {
		return shareApplicationMoneyPendingAllotmentPrvRpt;
	}
	/**
	 * @param shareApplicationMoneyPendingAllotmentPrvRpt the shareApplicationMoneyPendingAllotmentPrvRpt to set
	 */
	public final void setShareApplicationMoneyPendingAllotmentPrvRpt(Double shareApplicationMoneyPendingAllotmentPrvRpt) {
		this.shareApplicationMoneyPendingAllotmentPrvRpt = shareApplicationMoneyPendingAllotmentPrvRpt;
	}
	/**
	 * @return the longTermBorrowingsPrvRpt
	 */
	public final Double getLongTermBorrowingsPrvRpt() {
		return longTermBorrowingsPrvRpt;
	}
	/**
	 * @param longTermBorrowingsPrvRpt the longTermBorrowingsPrvRpt to set
	 */
	public final void setLongTermBorrowingsPrvRpt(Double longTermBorrowingsPrvRpt) {
		this.longTermBorrowingsPrvRpt = longTermBorrowingsPrvRpt;
	}
	/**
	 * @return the deferredTaxLiabilitiesNetPrvRpt
	 */
	public final Double getDeferredTaxLiabilitiesNetPrvRpt() {
		return deferredTaxLiabilitiesNetPrvRpt;
	}
	/**
	 * @param deferredTaxLiabilitiesNetPrvRpt the deferredTaxLiabilitiesNetPrvRpt to set
	 */
	public final void setDeferredTaxLiabilitiesNetPrvRpt(Double deferredTaxLiabilitiesNetPrvRpt) {
		this.deferredTaxLiabilitiesNetPrvRpt = deferredTaxLiabilitiesNetPrvRpt;
	}
	/**
	 * @return the otherLongTermLiabilitiesPrvRpt
	 */
	public final Double getOtherLongTermLiabilitiesPrvRpt() {
		return otherLongTermLiabilitiesPrvRpt;
	}
	/**
	 * @param otherLongTermLiabilitiesPrvRpt the otherLongTermLiabilitiesPrvRpt to set
	 */
	public final void setOtherLongTermLiabilitiesPrvRpt(Double otherLongTermLiabilitiesPrvRpt) {
		this.otherLongTermLiabilitiesPrvRpt = otherLongTermLiabilitiesPrvRpt;
	}
	/**
	 * @return the longTermProvisionsPrvRpt
	 */
	public final Double getLongTermProvisionsPrvRpt() {
		return longTermProvisionsPrvRpt;
	}
	/**
	 * @param longTermProvisionsPrvRpt the longTermProvisionsPrvRpt to set
	 */
	public final void setLongTermProvisionsPrvRpt(Double longTermProvisionsPrvRpt) {
		this.longTermProvisionsPrvRpt = longTermProvisionsPrvRpt;
	}
	/**
	 * @return the shortTermBorrowingsPrvRpt
	 */
	public final Double getShortTermBorrowingsPrvRpt() {
		return shortTermBorrowingsPrvRpt;
	}
	/**
	 * @param shortTermBorrowingsPrvRpt the shortTermBorrowingsPrvRpt to set
	 */
	public final void setShortTermBorrowingsPrvRpt(Double shortTermBorrowingsPrvRpt) {
		this.shortTermBorrowingsPrvRpt = shortTermBorrowingsPrvRpt;
	}
	/**
	 * @return the tradePayablesPrvRpt
	 */
	public final Double getTradePayablesPrvRpt() {
		return tradePayablesPrvRpt;
	}
	/**
	 * @param tradePayablesPrvRpt the tradePayablesPrvRpt to set
	 */
	public final void setTradePayablesPrvRpt(Double tradePayablesPrvRpt) {
		this.tradePayablesPrvRpt = tradePayablesPrvRpt;
	}
	/**
	 * @return the otherCurrentLiabilitiesPrvRpt
	 */
	public final Double getOtherCurrentLiabilitiesPrvRpt() {
		return otherCurrentLiabilitiesPrvRpt;
	}
	/**
	 * @param otherCurrentLiabilitiesPrvRpt the otherCurrentLiabilitiesPrvRpt to set
	 */
	public final void setOtherCurrentLiabilitiesPrvRpt(Double otherCurrentLiabilitiesPrvRpt) {
		this.otherCurrentLiabilitiesPrvRpt = otherCurrentLiabilitiesPrvRpt;
	}
	/**
	 * @return the shortTermProvisionsPrvRpt
	 */
	public final Double getShortTermProvisionsPrvRpt() {
		return shortTermProvisionsPrvRpt;
	}
	/**
	 * @param shortTermProvisionsPrvRpt the shortTermProvisionsPrvRpt to set
	 */
	public final void setShortTermProvisionsPrvRpt(Double shortTermProvisionsPrvRpt) {
		this.shortTermProvisionsPrvRpt = shortTermProvisionsPrvRpt;
	}
	/**
	 * @return the tangibleAssetsPrvRpt
	 */
	public final Double getTangibleAssetsPrvRpt() {
		return tangibleAssetsPrvRpt;
	}
	/**
	 * @param tangibleAssetsPrvRpt the tangibleAssetsPrvRpt to set
	 */
	public final void setTangibleAssetsPrvRpt(Double tangibleAssetsPrvRpt) {
		this.tangibleAssetsPrvRpt = tangibleAssetsPrvRpt;
	}
	/**
	 * @return the intangibleAssetsPrvRpt
	 */
	public final Double getIntangibleAssetsPrvRpt() {
		return intangibleAssetsPrvRpt;
	}
	/**
	 * @param intangibleAssetsPrvRpt the intangibleAssetsPrvRpt to set
	 */
	public final void setIntangibleAssetsPrvRpt(Double intangibleAssetsPrvRpt) {
		this.intangibleAssetsPrvRpt = intangibleAssetsPrvRpt;
	}
	/**
	 * @return the capitalWorkInProgressPrvRpt
	 */
	public final Double getCapitalWorkInProgressPrvRpt() {
		return capitalWorkInProgressPrvRpt;
	}
	/**
	 * @param capitalWorkInProgressPrvRpt the capitalWorkInProgressPrvRpt to set
	 */
	public final void setCapitalWorkInProgressPrvRpt(Double capitalWorkInProgressPrvRpt) {
		this.capitalWorkInProgressPrvRpt = capitalWorkInProgressPrvRpt;
	}
	/**
	 * @return the intangibleAssetsUnderDevelopmentPrvRpt
	 */
	public final Double getIntangibleAssetsUnderDevelopmentPrvRpt() {
		return intangibleAssetsUnderDevelopmentPrvRpt;
	}
	/**
	 * @param intangibleAssetsUnderDevelopmentPrvRpt the intangibleAssetsUnderDevelopmentPrvRpt to set
	 */
	public final void setIntangibleAssetsUnderDevelopmentPrvRpt(Double intangibleAssetsUnderDevelopmentPrvRpt) {
		this.intangibleAssetsUnderDevelopmentPrvRpt = intangibleAssetsUnderDevelopmentPrvRpt;
	}
	/**
	 * @return the nonCurrentInvestmentsPrvRpt
	 */
	public final Double getNonCurrentInvestmentsPrvRpt() {
		return nonCurrentInvestmentsPrvRpt;
	}
	/**
	 * @param nonCurrentInvestmentsPrvRpt the nonCurrentInvestmentsPrvRpt to set
	 */
	public final void setNonCurrentInvestmentsPrvRpt(Double nonCurrentInvestmentsPrvRpt) {
		this.nonCurrentInvestmentsPrvRpt = nonCurrentInvestmentsPrvRpt;
	}
	/**
	 * @return the deferredTaxAssetsNetPrvRpt
	 */
	public final Double getDeferredTaxAssetsNetPrvRpt() {
		return deferredTaxAssetsNetPrvRpt;
	}
	/**
	 * @param deferredTaxAssetsNetPrvRpt the deferredTaxAssetsNetPrvRpt to set
	 */
	public final void setDeferredTaxAssetsNetPrvRpt(Double deferredTaxAssetsNetPrvRpt) {
		this.deferredTaxAssetsNetPrvRpt = deferredTaxAssetsNetPrvRpt;
	}
	/**
	 * @return the longTermLoansAndAdvancesPrvRpt
	 */
	public final Double getLongTermLoansAndAdvancesPrvRpt() {
		return longTermLoansAndAdvancesPrvRpt;
	}
	/**
	 * @param longTermLoansAndAdvancesPrvRpt the longTermLoansAndAdvancesPrvRpt to set
	 */
	public final void setLongTermLoansAndAdvancesPrvRpt(Double longTermLoansAndAdvancesPrvRpt) {
		this.longTermLoansAndAdvancesPrvRpt = longTermLoansAndAdvancesPrvRpt;
	}
	/**
	 * @return the otherNonCurrentAssetsPrvRpt
	 */
	public final Double getOtherNonCurrentAssetsPrvRpt() {
		return otherNonCurrentAssetsPrvRpt;
	}
	/**
	 * @param otherNonCurrentAssetsPrvRpt the otherNonCurrentAssetsPrvRpt to set
	 */
	public final void setOtherNonCurrentAssetsPrvRpt(Double otherNonCurrentAssetsPrvRpt) {
		this.otherNonCurrentAssetsPrvRpt = otherNonCurrentAssetsPrvRpt;
	}
	/**
	 * @return the currentInvestmentsPrvRpt
	 */
	public final Double getCurrentInvestmentsPrvRpt() {
		return currentInvestmentsPrvRpt;
	}
	/**
	 * @param currentInvestmentsPrvRpt the currentInvestmentsPrvRpt to set
	 */
	public final void setCurrentInvestmentsPrvRpt(Double currentInvestmentsPrvRpt) {
		this.currentInvestmentsPrvRpt = currentInvestmentsPrvRpt;
	}
	/**
	 * @return the inventoriesPrvRpt
	 */
	public final Double getInventoriesPrvRpt() {
		return inventoriesPrvRpt;
	}
	/**
	 * @param inventoriesPrvRpt the inventoriesPrvRpt to set
	 */
	public final void setInventoriesPrvRpt(Double inventoriesPrvRpt) {
		this.inventoriesPrvRpt = inventoriesPrvRpt;
	}
	/**
	 * @return the tradeReceivablesPrvRpt
	 */
	public final Double getTradeReceivablesPrvRpt() {
		return tradeReceivablesPrvRpt;
	}
	/**
	 * @param tradeReceivablesPrvRpt the tradeReceivablesPrvRpt to set
	 */
	public final void setTradeReceivablesPrvRpt(Double tradeReceivablesPrvRpt) {
		this.tradeReceivablesPrvRpt = tradeReceivablesPrvRpt;
	}
	/**
	 * @return the cashAndCashEquivalentsPrvRpt
	 */
	public final Double getCashAndCashEquivalentsPrvRpt() {
		return cashAndCashEquivalentsPrvRpt;
	}
	/**
	 * @param cashAndCashEquivalentsPrvRpt the cashAndCashEquivalentsPrvRpt to set
	 */
	public final void setCashAndCashEquivalentsPrvRpt(Double cashAndCashEquivalentsPrvRpt) {
		this.cashAndCashEquivalentsPrvRpt = cashAndCashEquivalentsPrvRpt;
	}
	/**
	 * @return the shortTermLoansAndAdvancesPrvRpt
	 */
	public final Double getShortTermLoansAndAdvancesPrvRpt() {
		return shortTermLoansAndAdvancesPrvRpt;
	}
	/**
	 * @param shortTermLoansAndAdvancesPrvRpt the shortTermLoansAndAdvancesPrvRpt to set
	 */
	public final void setShortTermLoansAndAdvancesPrvRpt(Double shortTermLoansAndAdvancesPrvRpt) {
		this.shortTermLoansAndAdvancesPrvRpt = shortTermLoansAndAdvancesPrvRpt;
	}
	/**
	 * @return the otherCurrentAssetsPrvRpt
	 */
	public final Double getOtherCurrentAssetsPrvRpt() {
		return otherCurrentAssetsPrvRpt;
	}
	/**
	 * @param otherCurrentAssetsPrvRpt the otherCurrentAssetsPrvRpt to set
	 */
	public final void setOtherCurrentAssetsPrvRpt(Double otherCurrentAssetsPrvRpt) {
		this.otherCurrentAssetsPrvRpt = otherCurrentAssetsPrvRpt;
	}
	/**
	 * @return the liabilityTotal
	 */
	public final Double getLiabilityTotal() {
		this.liabilityTotal =  
			  this.shareCapital 
			+ this.reservesAndSurplus + this.profitLossForPeriod
			+ this.moneyReceivedAgainstShareWarrants 
			+ this.shareApplicationMoneyPendingAllotment 
			+ this.longTermBorrowings 
			+ this.deferredTaxLiabilitiesNet 
			+ this.otherLongTermLiabilities 
			+ this.longTermProvisions 
			+ this.shortTermBorrowings 
			+ this.tradePayables 
			+ this.otherCurrentLiabilities 
			+ this.shortTermProvisions;
		return this.liabilityTotal;
	}
	/**
	 * @param liabilityTotal the liabilityTotal to set
	 */
	public final void setLiabilityTotal(Double liabilityTotal) {
		this.liabilityTotal = liabilityTotal;
	}
	/**
	 * @return the assetTotal
	 */
	public final Double getAssetTotal() {
		this.assetTotal = 
				+ this.tangibleAssets
				+ this.intangibleAssets
				+ this.capitalWorkInProgress
				+ this.intangibleAssetsUnderDevelopment
				+ this.nonCurrentInvestments
				+ this.deferredTaxAssetsNet
				+ this.longTermLoansAndAdvances
				+ this.otherNonCurrentAssets
				+ this.currentInvestments
				+ this.inventories
				+ this.tradeReceivables
				+ this.cashAndCashEquivalents
				+ this.shortTermLoansAndAdvances
				+ this.otherCurrentAssets;
		return this.assetTotal;
	}
	/**
	 * @param assetTotal the assetTotal to set
	 */
	public final void setAssetTotal(Double assetTotal) {
		this.assetTotal = assetTotal;
	}
	/**
	 * @return the liabilityTotalPrvRpt
	 */
	public final Double getLiabilityTotalPrvRpt() {
		this.liabilityTotalPrvRpt =  
				  this.shareCapitalPrvRpt 
				+ this.reservesAndSurplusPrvRpt
						  +this.profitLossForPeriodPrvRpt
				+ this.moneyReceivedAgainstShareWarrantsPrvRpt 
				+ this.shareApplicationMoneyPendingAllotmentPrvRpt 
				+ this.longTermBorrowingsPrvRpt 
				+ this.deferredTaxLiabilitiesNetPrvRpt 
				+ this.otherLongTermLiabilitiesPrvRpt 
				+ this.longTermProvisionsPrvRpt 
				+ this.shortTermBorrowingsPrvRpt 
				+ this.tradePayablesPrvRpt 
				+ this.otherCurrentLiabilitiesPrvRpt 
				+ this.shortTermProvisionsPrvRpt;
		return this.liabilityTotalPrvRpt;
	}
	/**
	 * @param liabilityTotalPrvRpt the liabilityTotalPrvRpt to set
	 */
	public final void setLiabilityTotalPrvRpt(Double liabilityTotalPrvRpt) {
		this.liabilityTotalPrvRpt = liabilityTotalPrvRpt;
	}
	/**
	 * @return the assetTotalPrvRpt
	 */
	public final Double getAssetTotalPrvRpt() {
		this.assetTotalPrvRpt = 
				+ this.tangibleAssetsPrvRpt
				+ this.intangibleAssetsPrvRpt
				+ this.capitalWorkInProgressPrvRpt
				+ this.intangibleAssetsUnderDevelopmentPrvRpt
				+ this.nonCurrentInvestmentsPrvRpt
				+ this.deferredTaxAssetsNetPrvRpt
				+ this.longTermLoansAndAdvancesPrvRpt
				+ this.otherNonCurrentAssetsPrvRpt
				+ this.currentInvestmentsPrvRpt
				+ this.inventoriesPrvRpt
				+ this.tradeReceivablesPrvRpt
				+ this.cashAndCashEquivalentsPrvRpt
				+ this.shortTermLoansAndAdvancesPrvRpt
				+ this.otherCurrentAssetsPrvRpt;
		return assetTotalPrvRpt;
	}
	/**
	 * @param assetTotalPrvRpt the assetTotalPrvRpt to set
	 */
	public final void setAssetTotalPrvRpt(Double assetTotalPrvRpt) {
		this.assetTotalPrvRpt = assetTotalPrvRpt;
	}
	
	/**
	 * Method will just invoke the computation steps for total assets and liabilities.
	 * It should update the bean object.
	 */
	public void computeIntermidiateStepValues(){
		this.shareholderFundsTotal = this.shareCapital + this.reservesAndSurplus + this.profitLossForPeriod + this.moneyReceivedAgainstShareWarrants;
		this.profitLossForPeriodPlusReservesSurplus = this.reservesAndSurplus + this.profitLossForPeriod;
		this.nonCurrentLiabilitiesTotal = this.longTermBorrowings + this.deferredTaxLiabilitiesNet + this.otherLongTermLiabilities + this.longTermProvisions;
		this.currentLiabilitiesTotal = this.shortTermBorrowings + this.tradePayables + this.otherCurrentLiabilities + this.shortTermProvisions;

		this.shareholderFundsTotalPrvRpt = this.shareCapitalPrvRpt + this.reservesAndSurplusPrvRpt + this.profitLossForPeriodPrvRpt + this.moneyReceivedAgainstShareWarrantsPrvRpt;
		this.profitLossForPeriodPlusReservesSurplusPrvRpt = this.reservesAndSurplusPrvRpt + this.profitLossForPeriodPrvRpt;
		this.nonCurrentLiabilitiesTotalPrvRpt = this.longTermBorrowingsPrvRpt + this.deferredTaxLiabilitiesNetPrvRpt + this.otherLongTermLiabilitiesPrvRpt + this.longTermProvisionsPrvRpt;
		this.currentLiabilitiesTotalPrvRpt = this.shortTermBorrowingsPrvRpt + this.tradePayablesPrvRpt + this.otherCurrentLiabilitiesPrvRpt + this.shortTermProvisionsPrvRpt;

		this.nonCurrentAssetsTotal = this.tangibleAssets + this.intangibleAssets + this.capitalWorkInProgress + this.intangibleAssetsUnderDevelopment
				+ this.nonCurrentInvestments + this.deferredTaxAssetsNet  + this.longTermLoansAndAdvances + this.otherNonCurrentAssets;
		this.currentAssetsTotal = this.currentInvestments + this.inventories + this.tradeReceivables + this.cashAndCashEquivalents + this.shortTermLoansAndAdvances  + this.otherCurrentAssets;

		this.nonCurrentAssetsTotalPrvRpt = this.tangibleAssetsPrvRpt + this.intangibleAssetsPrvRpt + this.capitalWorkInProgressPrvRpt + this.intangibleAssetsUnderDevelopmentPrvRpt
				+ this.nonCurrentInvestmentsPrvRpt + this.deferredTaxAssetsNetPrvRpt  + this.longTermLoansAndAdvancesPrvRpt + this.otherNonCurrentAssetsPrvRpt;
		this.currentAssetsTotalPrvRpt = this.currentInvestmentsPrvRpt + this.inventoriesPrvRpt + this.tradeReceivablesPrvRpt + this.cashAndCashEquivalentsPrvRpt + this.shortTermLoansAndAdvancesPrvRpt  + this.otherCurrentAssetsPrvRpt;

		this.getLiabilityTotal();
		this.getLiabilityTotalPrvRpt();
		this.getAssetTotal();
		this.getAssetTotalPrvRpt();
	}
	
}
