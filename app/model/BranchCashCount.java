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
@Table(name = "BRANCH_CASH_COUNT_DETAILS")
public class BranchCashCount extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BranchCashCount() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "date")
	private Date date;

	@Column(name = "notes_total")
	private Double notesTotal = 0.0;

	@Column(name = "coins_total")
	private Double coinsTotal = 0.0;

	@Column(name = "smaller_coins_total")
	private Double smallerCoinsTotal = 0.0;

	public Double getSmallerCoinsTotal() {
		return smallerCoinsTotal;
	}

	public void setSmallerCoinsTotal(Double smallerCoinsTotal) {
		this.smallerCoinsTotal = smallerCoinsTotal;
	}

	@Column(name = "grand_total")
	private Double grandTotal = 0.0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	@Column(name = "DOCUMENT_UPLOADED_FOR_PETTY_CASH_TRANSFER")
	private String docUploadedForPettyCashTransfer;

	public String getDocUploadedForPettyCashTransfer() {
		return docUploadedForPettyCashTransfer;
	}

	public void setDocUploadedForPettyCashTransfer(
			String docUploadedForPettyCashTransfer) {
		this.docUploadedForPettyCashTransfer = docUploadedForPettyCashTransfer;
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

	@Column(name = "supporting_document")
	private String supportingDocument;

	@Column(name = "CREDIT_AMOUNT")
	private Double creditAmount = 0.0;

	@Column(name = "DEBIT_AMOUNT")
	private Double debitAmount = 0.0;

	@Column(name = "RESULTANT_CASH")
	private Double resultantCash = 0.0;

	@Column(name = "TOTAL_MAINCASH_TO_PETTYCASH")
	private Double totalMainCashToPettyCash = 0.0;

	@Column(name = "RESULTANT_PETTY_CASH")
	private Double resultantPettyCash = 0.0;

	@Column(name = "DEBITTED_PETTY_CASH_AMOUNT")
	private Double debittedPettyCashAmount = 0.0;

	public Double getTotalMainCashToPettyCash() {
		return totalMainCashToPettyCash;
	}

	public void setTotalMainCashToPettyCash(Double totalMainCashToPettyCash) {
		this.totalMainCashToPettyCash = totalMainCashToPettyCash;
	}

	public Double getResultantPettyCash() {
		return resultantPettyCash;
	}

	public void setResultantPettyCash(Double resultantPettyCash) {
		this.resultantPettyCash = resultantPettyCash;
	}

	public Double getDebittedPettyCashAmount() {
		return debittedPettyCashAmount;
	}

	public void setDebittedPettyCashAmount(Double debittedPettyCashAmount) {
		this.debittedPettyCashAmount = debittedPettyCashAmount;
	}

	public String getSupportingDocument() {
		return supportingDocument;
	}

	public void setSupportingDocument(String supportingDocument) {
		this.supportingDocument = supportingDocument;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Double getNotesTotal() {
		return notesTotal;
	}

	public void setNotesTotal(Double notesTotal) {
		this.notesTotal = notesTotal;
	}

	public Double getCoinsTotal() {
		return coinsTotal;
	}

	public void setCoinsTotal(Double coinsTotal) {
		this.coinsTotal = coinsTotal;
	}

	public Double getGrandTotal() {
		return grandTotal;
	}

	public void setGrandTotal(Double grandTotal) {
		this.grandTotal = grandTotal;
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
	 * Find a BranchCashCount by id.
	 */
	public static BranchCashCount findById(Long id) {
		return entityManager.find(BranchCashCount.class, id);
	}
}
