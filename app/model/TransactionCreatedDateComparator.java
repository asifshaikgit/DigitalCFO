package model;

import java.util.Comparator;
import java.util.Date;

public class TransactionCreatedDateComparator implements Comparator<TransactionCreatedDateComparator> {
	private String incomeExpense;
	private Double credit;
	private Double debit;
	private Date createdDate;
	private Double balance;
	private String txnRefNumber;
	private String instrumentNumber;
	private String transactionPurpose;
	private String ledgerCustVend;
	private String instrumentDate;
	private String brsBankDate;

	public String getBrsBankDate() {
		return brsBankDate;
	}

	public void setBrsBankDate(String brsBankDate) {
		this.brsBankDate = brsBankDate;
	}
	public String getInstrumentDate() {
		return instrumentDate;
	}

	public void setInstrumentDate(String instrumentDate) {
		this.instrumentDate = instrumentDate;
	}

	public String getInstrumentNumber() {
		return instrumentNumber;
	}

	public void setInstrumentNumber(String instrumentNumber) {
		this.instrumentNumber = instrumentNumber;
	}

	public String getIncomeExpense() {
		return incomeExpense;
	}

	public void setIncomeExpense(String incomeExpense) {
		this.incomeExpense = incomeExpense;
	}

	public Double getCredit() {
		return credit;
	}

	public void setCredit(Double credit) {
		this.credit = credit;
	}

	public Double getDebit() {
		return debit;
	}

	public void setDebit(Double debit) {
		this.debit = debit;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	@Override
	public int compare(TransactionCreatedDateComparator o1, TransactionCreatedDateComparator o2) {
		return o1.getCreatedDate().compareTo(o2.getCreatedDate());
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public String getTxnRefNumber() {
		return txnRefNumber;
	}

	public void setTxnRefNumber(String txnRefNumber) {
		this.txnRefNumber = txnRefNumber;
	}
	
	public String getLedgerCustVend() {
		return ledgerCustVend;
	}
	
	public String getTransactionPurpose() {
		return transactionPurpose;
	}
	
	public void setLedgerCustVend(String ledgerCustVend) {
		this.ledgerCustVend = ledgerCustVend;
	}
	
	public void setTransactionPurpose(String transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
	}
	
	@Override
	public String toString() {
		return "TransactionCreatedDateComparator{" +
				"incomeExpense='" + incomeExpense + '\'' +
				", credit=" + credit +
				", debit=" + debit +
				", createdDate=" + createdDate +
				", balance=" + balance +
				", txnRefNumber='" + txnRefNumber + '\'' +
				", instrumentNumber='" + instrumentNumber + '\'' +
				", instrumentDate='" + instrumentDate + '\'' +
				", brsBankDate='" + brsBankDate + '\'' +
				'}';
	}
}
