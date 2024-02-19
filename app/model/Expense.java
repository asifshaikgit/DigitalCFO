package model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "EXPENSE")
public class Expense extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public Expense() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "no_of_items")
	private Integer noOfItems;

	@Column(name = "unit_cost")
	private Double unitCost;

	@Column(name = "transaction_purpose")
	private Integer transactionPurpose;

	@Column(name = "total_cost")
	private Double totalCost;

	@Column(name = "payment_type")
	private Integer paymentType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "users_id")
	private Users users;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "users_branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "users_branch_organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specifics_id")
	private Specifics specifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specifics_particulars_id")
	private Particulars particulars;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id")
	private Vendor vendor;

	@Column(name = "status")
	private String status;

	@Column(name = "file_url")
	private String fileUrl;

	@Column(name = "action_date")
	private Date actionDate;

	@Column(name = "transaction_number")
	private String txnNumber;

	@Column(name = "remarks")
	private String remarks;

	@Column(name = "additional_approver_email")
	private String additionalApproverEmail;

	@Column(name = "next_approver_label_forwadedby")
	private String nextLabelForwadedBy;

	public String getNextLabelForwadedBy() {
		return nextLabelForwadedBy;
	}

	public void setNextLabelForwadedBy(String nextLabelForwadedBy) {
		this.nextLabelForwadedBy = nextLabelForwadedBy;
	}

	public String getAdditionalApproverEmail() {
		return additionalApproverEmail;
	}

	public void setAdditionalApproverEmail(String additionalApproverEmail) {
		this.additionalApproverEmail = additionalApproverEmail;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Date getActionDate() {
		return actionDate;
	}

	public void setActionDate(Date actionDate) {
		this.actionDate = actionDate;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public Particulars getParticulars() {
		return particulars;
	}

	public void setParticulars(Particulars particulars) {
		this.particulars = particulars;
	}

	public Integer getNoOfItems() {
		return noOfItems;
	}

	public void setNoOfItems(Integer noOfItems) {
		this.noOfItems = noOfItems;
	}

	public Double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(Double totalCost) {
		this.totalCost = totalCost;
	}

	public Users getUsers() {
		return users;
	}

	public void setUsers(Users users) {
		this.users = users;
	}

	public Specifics getSpecifics() {
		return specifics;
	}

	public void setSpecifics(Specifics specifics) {
		this.specifics = specifics;
	}

	public Integer getTransactionPurpose() {
		return transactionPurpose;
	}

	public void setTransactionPurpose(Integer transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
	}

	public Integer getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(Integer paymentType) {
		this.paymentType = paymentType;
	}

	public String getTxnNumber() {
		return txnNumber;
	}

	public void setTxnNumber(String txnNumber) {
		this.txnNumber = txnNumber;
	}

	/**
	 * list of expenses in raised status.
	 */
	public List<Expense> list(EntityManager entityManager, String str, Map<String, StringBuilder> raisedqueryMap) {
		StringBuilder sbquery = raisedqueryMap.get(str);
		List<Expense> expenses = entityManager.createQuery(sbquery.toString()).getResultList();
		return expenses;
	}

	/**
	 * list of expenses in raised status.
	 */
	public List<Expense> list1(EntityManager entityManager, String str, Map<String, StringBuilder> notRaisedqueryMap) {
		StringBuilder sbquery = notRaisedqueryMap.get(str);
		List<Expense> expenses = entityManager.createQuery(sbquery.toString()).getResultList();
		return expenses;
	}

	/**
	 * Find a expense by id.
	 */
	public static Expense findById(Long id) {
		return entityManager.find(Expense.class, id);
	}

	public Double getUnitCost() {
		return unitCost;
	}

	public void setUnitCost(Double unitCost) {
		this.unitCost = unitCost;
	}

	public static class Page {

		private final int pageSize;
		private final long totalRowCount;
		private final int pageIndex;
		private final List<Expense> list;

		public Page(List<Expense> data, long total, int page, int pageSize) {
			this.list = data;
			this.totalRowCount = total;
			this.pageIndex = page;
			this.pageSize = pageSize;
		}

		public long getTotalRowCount() {
			return totalRowCount;
		}

		public int getPageIndex() {
			return pageIndex;
		}

		public List<Expense> getList() {
			return list;
		}

		public boolean hasPrev() {
			return pageIndex > 1;
		}

		public boolean hasNext() {
			if (totalRowCount % pageSize == 0) {
				return false;
			} else {
				return (totalRowCount / pageSize) >= pageIndex;
			}
		}

		public String getDisplayXtoYofZ() {
			int start = ((pageIndex - 1) * pageSize + 1);
			int end = start + Math.min(pageSize, list.size()) - 1;
			return start + " to " + end + " of " + totalRowCount;
		}
	}
}
