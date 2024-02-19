package model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="IDOS_NOTES_LINKED_TO_TRANSACTION")
public class IdosNotesToTransaction extends AbstractBaseModel {

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="NOTES_ID")
	private IDOSNotes idosNotes;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="INCOME_EXPENSE_TRANSACTION_ID")
	private Transaction incomeExpenseTransaction;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="CLAIMS_TRANSACTION_ID")
	private ClaimTransaction claimsTransaction;

	public Transaction getIncomeExpenseTransaction() {
		return incomeExpenseTransaction;
	}

	public void setIncomeExpenseTransaction(Transaction incomeExpenseTransaction) {
		this.incomeExpenseTransaction = incomeExpenseTransaction;
	}

	public ClaimTransaction getClaimsTransaction() {
		return claimsTransaction;
	}

	public void setClaimsTransaction(ClaimTransaction claimsTransaction) {
		this.claimsTransaction = claimsTransaction;
	}

	public IDOSNotes getIdosNotes() {
		return idosNotes;
	}

	public void setIdosNotes(IDOSNotes idosNotes) {
		this.idosNotes = idosNotes;
	}
}
