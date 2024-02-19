package model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TrialBalanceLedgerReportList{
    @JsonProperty("itemTransData")
    private List<TrialBalanceLedgerReport> tbLedgerReportList;

    public List<TrialBalanceLedgerReport> getTrialBalanceLedgerReportList() {
        return tbLedgerReportList;
    }

    public void setTrialBalanceLedgerReportList(List<TrialBalanceLedgerReport> tbLedgerReportList) {
        this.tbLedgerReportList = tbLedgerReportList;
    }

}
