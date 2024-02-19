package model;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;
import javax.persistence.Query;

/*
 * When transaction is processed based on TRANSACTION_PURPOSE(Buy/sell type) data will be entered in this table.
 * E.g. For Sell on cash of gross amt =1000 vitamins to say customer Matthew. Credit Balance of 1000 will be entered here for tranID=18 and taxes of say salestax=100, VAT=55
 * So specificId=vitamin and particularID=Income
 * It will be used later to display trialbalance for income->vitamins etc
 */
@Entity
@Table(name = "TRIALBALANCE_COAITEMS")
public class TrialBalanceCOAItems extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public TrialBalanceCOAItems() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static String JQL_TURNOVER_INTRA_STATE_QUERY = "select SUM(t1.creditAmount), SUM(t1.debitAmount) from TrialBalanceCOAItems t1, Transaction t2 where t1.organization.id = ?1 and t1.organization.id =t2.transactionBranchOrganization.id and t1.branch.id=t2.transactionBranch.id and t1.transactionId=t2.id and t1.transactionPurpose in (1,2,30,31) and t2.typeOfSupply not in(3,4,5) and (t1.transactionSpecifics.gstItemCategory not in (1,2,3) or t1.transactionSpecifics.gstItemCategory is null) and SUBSTRING(t2.sourceGstin,1,2) = SUBSTRING(t2.destinationGstin,1,2) and t1.presentStatus=1 and t1.date between ?2 and ?3";

	private static String JQL_TURNOVER_INTRA_STATE_QUERY_FOR_KARVY_JSON = "select SUM(t1.creditAmount), SUM(t1.debitAmount) from TrialBalanceCOAItems t1, Transaction t2 where t1.organization.id = ?1 and t2.transactionBranch.gstin=?2 and t1.organization.id =t2.transactionBranchOrganization.id and t1.branch.id=t2.transactionBranch.id and t1.transactionId=t2.id and t1.transactionPurpose in (1,2,30,31) and t2.typeOfSupply not in(3,4,5) and t1.transactionSpecifics.isCompositionScheme = 1 and (t1.transactionSpecifics.gstItemCategory not in (1,2,3) or t1.transactionSpecifics.gstItemCategory is null) and SUBSTRING(t2.sourceGstin,1,2) = SUBSTRING(t2.destinationGstin,1,2) and t1.presentStatus=1 and t1.date between ?3 and ?4";

	private static String JQL_TURNOVER_INTER_STATE_QUERY = "select SUM(t1.creditAmount), SUM(t1.debitAmount) from TrialBalanceCOAItems t1, Transaction t2 where t1.organization.id = ?1 and t1.organization.id =t2.transactionBranchOrganization.id and t1.branch.id=t2.transactionBranch.id and t1.transactionId=t2.id and t1.transactionPurpose in (1,2,30,31) and t2.typeOfSupply not in(3,4,5) and (t1.transactionSpecifics.gstItemCategory not in (1,2,3) or t1.transactionSpecifics.gstItemCategory is null) and SUBSTRING(t2.sourceGstin,1,2) != SUBSTRING(t2.destinationGstin,1,2) and t1.presentStatus=1 and t1.date between ?2 and ?3";

	private static String JQL_TURNOVER_NON_GST_QUERY = "select SUM(t1.creditAmount), SUM(t1.debitAmount) from TrialBalanceCOAItems t1, Transaction t2 where t1.organization.id = ?1 and t1.organization.id =t2.transactionBranchOrganization.id and t1.branch.id=t2.transactionBranch.id and t1.transactionId=t2.id and t1.transactionPurpose in (1,2,30,31) and t2.typeOfSupply not in(3,4,5) and t1.transactionSpecifics.gstItemCategory in (1,2,3) and t1.transactionSpecifics.accountCodeHirarchy like '/1%' and t1.presentStatus=1 and  t1.date between ?2 and ?3";

	private static String JQL_TURNOVER_EXPORT_QUERY = "select SUM(t1.creditAmount), SUM(t1.debitAmount) from TrialBalanceCOAItems t1, Transaction t2 where t1.organization.id = ?1 and t1.organization.id =t2.transactionBranchOrganization.id and t1.branch.id=t2.transactionBranch.id and t1.transactionId=t2.id and t1.transactionPurpose in (1,2,30,31) and t2.typeOfSupply in(3,4,5) and t1.transactionSpecifics.accountCodeHirarchy like '/1%' and t1.presentStatus=1 and t1.date between ?2 and ?3";

	// this is PK of transaction table
	@Column(name = "TRANSACTION_ID")
	private Long transactionId;

	// transaction type out of those 16 types of buy/sell on cash etc
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_PURPOSE")
	private TransactionPurpose transactionPurpose;

	// Chart of items ID of vitamins
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_SPECIFICS")
	private Specifics transactionSpecifics;

	// COA types like income/expense etc
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TRANSACTION_SPECIFICS_PARTICULARS")
	private Particulars transactionParticulars;

	// transaction date
	@Column(name = "DATE")
	private Date date;

	// Branch which initiated this transaction
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ID")
	private Branch branch;

	// organization id like IDOS
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ORGNIZATION_ID")
	private Organization organization;

	// for sell transaction this is affected
	@Column(name = "CREDIT_AMOUNT")
	private Double creditAmount = 0.0;

	@Column(name = "DEBIT_AMOUNT")
	private Double debitAmount = 0.0;

	@Column(name = "CLOSING_BALANCE")
	private Double closingBalance = 0.0;

	public Long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	public TransactionPurpose getTransactionPurpose() {
		return transactionPurpose;
	}

	public void setTransactionPurpose(TransactionPurpose transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
	}

	public Specifics getTransactionSpecifics() {
		return transactionSpecifics;
	}

	public void setTransactionSpecifics(Specifics transactionSpecifics) {
		this.transactionSpecifics = transactionSpecifics;
	}

	public Particulars getTransactionParticulars() {
		return transactionParticulars;
	}

	public void setTransactionParticulars(Particulars transactionParticulars) {
		this.transactionParticulars = transactionParticulars;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

	public Double getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(Double closingBalance) {
		this.closingBalance = closingBalance;
	}

	/**
	 * Find a TrialBalanceCOAItems by id.
	 */
	public static TrialBalanceCOAItems findById(Long id) {
		return entityManager.find(TrialBalanceCOAItems.class, id);
	}

	public static List<TrialBalanceCOAItems> getListByHead(Long headId) {
		String sql = "select obj from TrialBalanceCOAItems obj where obj.transactionSpecifics.id = ?1 and obj.presentStatus=1";
		Query jpaQry = entityManager.createQuery(sql);
		jpaQry.setParameter(1, headId);
		return jpaQry.getResultList();
	}

	public static Double findTournOverIntraState(Long orgid, Long branchId, Date startDate, Date endDate) {
		Double value = 0d;
		Query query = entityManager.createQuery(JQL_TURNOVER_INTRA_STATE_QUERY);
		query.setParameter(1, orgid);
		// query.setParameter(2, branchId);
		query.setParameter(2, startDate);
		query.setParameter(3, endDate);
		List<Object[]> results = query.getResultList();
		if (results != null && results.size() > 0) {
			Double val1 = 0d;
			Double val2 = 0d;
			if (results.get(0)[0] != null) {
				val1 = ((Double) results.get(0)[0]);
			}

			if (results.get(0)[1] != null) {
				val2 = ((Double) results.get(0)[1]);
			}
			value = val1 - val2;
		}

		return value;
	}

	public static Double findTournOverInterState(Long orgid, Long branchId, Date startDate, Date endDate) {
		Double value = 0d;
		Query query = entityManager.createQuery(JQL_TURNOVER_INTER_STATE_QUERY);
		query.setParameter(1, orgid);
		// query.setParameter(2, branchId);
		query.setParameter(2, startDate);
		query.setParameter(3, endDate);
		List<Object[]> results = query.getResultList();
		if (results != null && results.size() > 0) {
			Double val1 = 0d;
			Double val2 = 0d;
			if (results.get(0)[0] != null) {
				val1 = ((Double) results.get(0)[0]);
			}

			if (results.get(0)[1] != null) {
				val2 = ((Double) results.get(0)[1]);
			}
			value = val1 - val2;
		}

		return value;
	}

	public static Double findTournOverNonGST(Long orgid, Long branchId, Date startDate, Date endDate) {
		Double value = 0d;
		Query query = entityManager.createQuery(JQL_TURNOVER_NON_GST_QUERY);
		query.setParameter(1, orgid);
		query.setParameter(2, startDate);
		query.setParameter(3, endDate);
		Object[] results = (Object[]) query.getSingleResult();
		if (results != null && results.length > 0) {
			Double val1 = 0d;
			Double val2 = 0d;
			if (results[0] != null) {
				val1 = ((Double) results[0]);
			}

			if (results[1] != null) {
				val2 = ((Double) results[1]);
			}
			value = val1 - val2;
		}

		return value;
	}

	public static Double findTournOverExport(Long orgid, Long branchId, Date startDate, Date endDate) {
		Double value = 0d;
		Query query = entityManager.createQuery(JQL_TURNOVER_EXPORT_QUERY);
		query.setParameter(1, orgid);
		query.setParameter(2, startDate);
		query.setParameter(3, endDate);
		Object[] results = (Object[]) query.getSingleResult();
		if (results != null && results.length > 0) {
			Double val1 = 0d;
			Double val2 = 0d;
			if (results[0] != null) {
				val1 = ((Double) results[0]);
			}

			if (results[1] != null) {
				val2 = ((Double) results[1]);
			}
			value = val1 - val2;
		}

		return value;
	}

	public static Double findTournOverIntraStateForKarvy(Long orgid, String gstin, Date startDate, Date endDate) {
		Double value = 0d;
		Query query = entityManager.createQuery(JQL_TURNOVER_INTRA_STATE_QUERY_FOR_KARVY_JSON);
		query.setParameter(1, orgid);
		query.setParameter(2, gstin);
		query.setParameter(3, startDate);
		query.setParameter(4, endDate);
		List<Object[]> results = query.getResultList();
		if (results != null && results.size() > 0) {
			Double val1 = 0d;
			Double val2 = 0d;
			if (results.get(0)[0] != null) {
				val1 = ((Double) results.get(0)[0]);
			}

			if (results.get(0)[1] != null) {
				val2 = ((Double) results.get(0)[1]);
			}
			value = val1 - val2;
		}

		return value;
	}
}
