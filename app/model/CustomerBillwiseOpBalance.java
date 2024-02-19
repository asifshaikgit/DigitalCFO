package model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import com.idos.util.IdosDaoConstants;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "CUSTOMER_BILLWISE_OPENING_BAL")
public class CustomerBillwiseOpBalance extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public CustomerBillwiseOpBalance() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String BY_OGR_CUST_BRANCH_JPQL = "select DISTINCT(obj.branch) from CustomerBillwiseOpBalance obj where obj.organization.id= ?1 and obj.customer.id = ?2 and obj.presentStatus =1";

	private static final String BRANCH_OPENING_BALANCE_JPQL = "select obj from CustomerBillwiseOpBalance obj where obj.organization.id= ?1 and branch.id = ?2 and obj.customer.id = ?3 and obj.billDate <= ?4 and obj.presentStatus = 1";

	private static final long serialVersionUID = 613050239473482298L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUSTOMER_ID")
	private Vendor customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ID")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@Column(name = "BILL_NO")
	private String billNo;

	@Column(name = "BILL_DATE")
	public Date billDate;

	@Column(name = "BILL_AMOUNT")
	private Double billAmount;

	@Column(name = "OPENING_BALANCE")
	private Double openingBalance;

	public Vendor getCustomer() {
		return customer;
	}

	public void setCustomer(Vendor customer) {
		this.customer = customer;
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

	public void setOrganization(
			Organization organization) {
		this.organization = organization;
	}

	public String getBillNo() {
		return billNo;
	}

	public void setBillNo(String billNo) {
		this.billNo = billNo;
	}

	public Date getBillDate() {
		return billDate;
	}

	public void setBillDate(Date billDate) {
		this.billDate = billDate;
	}

	public Double getBillAmount() {
		return billAmount;
	}

	public void setBillAmount(
			Double billAmount) {
		this.billAmount = billAmount;
	}

	public static CustomerBillwiseOpBalance findById(Long id) {
		return entityManager.find(CustomerBillwiseOpBalance.class, id);
	}

	public Double getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(Double openingBalance) {
		this.openingBalance = openingBalance;
	}

	public static List<CustomerBillwiseOpBalance> findOpeningBalance(EntityManager entityManager, Long orgId,
			Long vendId, Long branchId, Date date) {
		Query query = entityManager.createQuery(BRANCH_OPENING_BALANCE_JPQL);
		query.setParameter(1, orgId);
		query.setParameter(2, branchId);
		query.setParameter(3, vendId);
		query.setParameter(4, date);
		List<CustomerBillwiseOpBalance> resultList = query.getResultList();
		return resultList;
	}

	public static List<Branch> findBranchWithOpeningBalance(EntityManager entityManager, Long orgId, Long vendId) {
		Query query = entityManager.createQuery(BY_OGR_CUST_BRANCH_JPQL);
		query.setParameter(1, orgId);
		query.setParameter(2, vendId);
		List<Branch> resultList = query.getResultList();
		return resultList;
	}

}
