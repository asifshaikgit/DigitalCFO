package model;

/**
 * @author Sunil K Namdev
 */
public class ProfitNLossReport {
    private String curDateRange;
    private String prevDateRange;
    private String scheduleNo;
    private String particularName;
    private String currentValue;
    private String previousValue;
    private String totalPnL;
    private String totalPnLPrev;

    public String getTotalPnLPrev() {
        return this.totalPnLPrev;
    }

    public void setTotalPnLPrev(String totalPnLPrev) {
        this.totalPnLPrev = totalPnLPrev;
    }

    public String getTotalPnL() {
        return this.totalPnL;
    }

    public void setTotalPnL(String totalPnL) {
        this.totalPnL = totalPnL;
    }

    public String getScheduleNo() {
        return this.scheduleNo;
    }

    public void setScheduleNo(String scheduleNo) {
        this.scheduleNo = scheduleNo;
    }

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
}
