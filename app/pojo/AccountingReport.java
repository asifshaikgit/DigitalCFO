package pojo;


import java.util.ArrayList;
import java.util.List;

public class AccountingReport {


    private String sroNumber;
    private String accountingDetails;
    private String creditBalance;
    private String debitBalance;

    public AccountingReport(String sroNumber, String accountingDetails, String creditBalance, String debitBalance) {
        this.sroNumber = sroNumber;
        this.accountingDetails = accountingDetails;
        this.creditBalance = creditBalance;
        this.debitBalance = debitBalance;
    }

    public String getSroNumber() {
        return sroNumber;
    }

    public void setSroNumber(String sroNumber) {
        this.sroNumber = sroNumber;
    }

    public String getAccountingDetails() {
        return accountingDetails;
    }

    public void setAccountingDetails(String accountingDetails) {
        this.accountingDetails = accountingDetails;
    }

    public String getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(String creditBalance) {
        this.creditBalance = creditBalance;
    }

    public String getDebitBalance() {
        return debitBalance;
    }

    public void setDebitBalance(String debitBalance) {
        this.debitBalance = debitBalance;
    }

    public static List<AccountingReport> creatAccountingReportList(List<Object[]> procedureList){
        List<AccountingReport> accountingReportList = new ArrayList<>();
        for(int i=0; i<procedureList.size(); i++){
            accountingReportList.add(new AccountingReport(
                    String.valueOf(procedureList.get(i)[0]),
                    String.valueOf(procedureList.get(i)[1]),
                    String.valueOf(procedureList.get(i)[2]),
                    String.valueOf(procedureList.get(i)[3])));
        }
        return accountingReportList;
    }

}
