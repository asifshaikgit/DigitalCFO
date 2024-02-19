package model;

/**
 * @author Sunil K Namdev
 */
public class BalanceSheetReport {
    private String curDateRange;
    private String prevDateRange;
    private String particularName;
    private String currentValue;
    private String previousValue;
    private String total;
    private String totalPrev;
    private String profitLossForPeriod;
    private String profitLossForPreviousPeriod;

    public String getParticularName() {
        return this.particularName;
    }

    public void setParticularName(String particularName) {
        this.particularName = particularName;
    }

    public String getCurrentValue() {
        return this.currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getPreviousValue() {
        return this.previousValue;
    }

    public void setPreviousValue(String previousValue) {
        this.previousValue = previousValue;
    }

    public String getTotal() {
        return this.total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getTotalPrev() {
        return this.totalPrev;
    }

    public void setTotalPrev(String totalPrev) {
        this.totalPrev = totalPrev;
    }

    public String getCurDateRange() {
        return this.curDateRange;
    }

    public void setCurDateRange(String curDateRange) {
        this.curDateRange = curDateRange;
    }

    public String getPrevDateRange() {
        return this.prevDateRange;
    }

    public void setPrevDateRange(String prevDateRange) {
        this.prevDateRange = prevDateRange;
    }

	public String getProfitLossForPeriod() {
		return profitLossForPeriod;
	}

	public void setProfitLossForPeriod(String profitLossForPeriod) {
		this.profitLossForPeriod = profitLossForPeriod;
	}

	public String getProfitLossForPreviousPeriod() {
		return profitLossForPreviousPeriod;
	}

	public void setProfitLossForPreviousPeriod(String profitLossForPreviousPeriod) {
		this.profitLossForPreviousPeriod = profitLossForPreviousPeriod;
	}
    
}
