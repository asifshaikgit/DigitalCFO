package model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "BRANCH_BANK_ACCOUNT_BALANCE_DETAILS")
public class BranchBankAccountBalance extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BranchBankAccountBalance() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "AMOUNT_BALANCE")
	private Double amountBalance = 0.0;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "BRANCH_BANK_ACCOUNTSID")
	private BranchBankAccounts branchBankAccounts;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	@Column(name = "CREDIT_AMOUNT")
	private Double creditAmount = 0.0;

	@Column(name = "DEBIT_AMOUNT")
	private Double debitAmount = 0.0;

	@Column(name = "RESULTANT_CASH")
	private Double resultantCash = 0.0;

	@Column(name = "date")
	private Date date;

	@Column(name = "BALANCE_STATEMENT")
	private String balanceStatement;

	public String getBalanceStatement() {
		return balanceStatement;
	}

	public void setBalanceStatement(String balanceStatement) {
		this.balanceStatement = balanceStatement;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Double getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(Double creditAmount) {
		this.creditAmount = creditAmount;
	}

	public Double getDebitAmount() {
		return debitAmount;
	}

	public void setDebitAmount(Double debitAmount) {
		this.debitAmount = debitAmount;
	}

	public Double getResultantCash() {
		return resultantCash;
	}

	public void setResultantCash(Double resultantCash) {
		this.resultantCash = resultantCash;
	}

	public Double getAmountBalance() {
		return amountBalance;
	}

	public void setAmountBalance(Double amountBalance) {
		this.amountBalance = amountBalance;
	}

	public BranchBankAccounts getBranchBankAccounts() {
		return branchBankAccounts;
	}

	public void setBranchBankAccounts(BranchBankAccounts branchBankAccounts) {
		this.branchBankAccounts = branchBankAccounts;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	/**
	 * Find a BranchBankAccounts by id.
	 */
	public static BranchBankAccountBalance findById(Long id) {
		return entityManager.find(BranchBankAccountBalance.class, id);
	}
}
