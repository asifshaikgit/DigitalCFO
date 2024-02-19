package model;

import java.util.Comparator;
import java.util.Date;

public class VendorCustomerStatementDateComparator implements Comparator<VendorCustomerStatementDateComparator> {
	public String accountName;
	public Double openingBalance;
	public Date createdDate;
	public Double debit;
	public Double credit;
	public Double closingBalance;
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public Double getOpeningBalance() {
		return openingBalance;
	}
	public void setOpeningBalance(Double openingBalance) {
		this.openingBalance = openingBalance;
	}
	public Double getDebit() {
		return debit;
	}
	public void setDebit(Double debit) {
		this.debit = debit;
	}
	public Double getCredit() {
		return credit;
	}
	public void setCredit(Double credit) {
		this.credit = credit;
	}
	public Double getClosingBalance() {
		return closingBalance;
	}
	public void setClosingBalance(Double closingBalance) {
		this.closingBalance = closingBalance;
	}
	
	@Override
	public int compare(VendorCustomerStatementDateComparator o1, VendorCustomerStatementDateComparator o2) {
		return o1.getCreatedDate().compareTo(o2.getCreatedDate());
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
}
