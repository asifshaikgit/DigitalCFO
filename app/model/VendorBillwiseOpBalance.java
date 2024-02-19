package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import com.idos.dao.GenericDAO;
import com.idos.util.IdosConstants;
import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.inject.Inject;

@Entity
@Table(name = "VENDOR_BILLWISE_OPENING_BAL")
public class VendorBillwiseOpBalance extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public VendorBillwiseOpBalance() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String BY_OGR_VEND_BRANCH_JPQL = "select DISTINCT(obj.branch) from VendorBillwiseOpBalance obj where obj.organization.id= ?1 and obj.vendor.id = ?2 and obj.presentStatus =1";

	private static final String BRANCH_OPENING_BALANCE_JPQL = "select obj from VendorBillwiseOpBalance obj where obj.organization.id= ?1 and branch.id = ?2 and obj.vendor.id = ?3 and obj.presentStatus =1";
	// private static final String BRANCH_OB_PENDING_JPQL = "select t1 from
	// VendorBillwiseOpBalance t1 where t1.organization.id= ?1 and t1.branch.id =
	// ?2 and t1.vendor.id = ?3 and t1.presentStatus = 1 and t1.openingBalance >
	// COALESCE((select sum(t2.vendorNetPayment) from Transaction t2 where
	// t2.transactionBranchOrganization.id = ?4 and t2.transactionBranch.id = ?5
	// and t2.transactionVendorCustomer.id = ?6 and t2.transactionPurpose.id = ?7
	// and t2.presentStatus = 1 and t2.typeIdentifier = ?8 group by
	// t2.paidInvoiceRefNumber), 0)";

	private static final String BRANCH_OB_PENDING_JPQL = "select t1 from VendorBillwiseOpBalance t1 where t1.organization.id= ?1 and t1.branch.id = ?2 and t1.vendor.id = ?3 and t1.presentStatus = 1 and t1.openingBalance > 0";

	private static final String BRANCH_OB_PAID_AMT_JPQL = "select sum(t2.vendorNetPayment) from Transaction t2 where t2.transactionBranchOrganization.id = ?1 and t2.transactionBranch.id = ?2 and t2.transactionVendorCustomer.id = ?3 and t2.transactionPurpose.id = ?4 and t2.typeIdentifier = ?5 and t2.presentStatus = 1 group by t2.paidInvoiceRefNumber";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "VENDOR_ID")
	private Vendor vendor;

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

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
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

	public void setBillAmount(Double billAmount) {
		this.billAmount = billAmount;
	}

	public Double getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(Double openingBalance) {
		this.openingBalance = openingBalance;
	}

	public static VendorBillwiseOpBalance findById(Long id) {
		return entityManager.find(VendorBillwiseOpBalance.class, id);
	}

	public static List<Branch> findBranchWithOpeningBalance(EntityManager entityManager, Long orgId, Long vendId) {
		Query query = entityManager.createQuery(BY_OGR_VEND_BRANCH_JPQL);
		query.setParameter(1, orgId);
		query.setParameter(2, vendId);
		List<Branch> resultList = query.getResultList();
		return resultList;
	}

	public static List<VendorBillwiseOpBalance> findOpeningBalance(EntityManager em, Long orgId, Long vendId,
			Long branchId) {
		Query query = entityManager.createQuery(BRANCH_OPENING_BALANCE_JPQL);
		query.setParameter(1, orgId);
		query.setParameter(2, branchId);
		query.setParameter(3, vendId);
		List<VendorBillwiseOpBalance> resultList = query.getResultList();
		return resultList;
	}

	public static List<VendorBillwiseOpBalance> getPendingOpeningBalance(EntityManager em, Long orgId, Long vendId,
			Long branchId, long tranPurpose, GenericDAO dao) {
		ArrayList inparams = new ArrayList(3);
		inparams.add(orgId);
		inparams.add(branchId);
		inparams.add(vendId);
		// inparams.add(orgId);
		// inparams.add(branchId);
		// inparams.add(vendId);
		// inparams.add(tranPurpose);
		// inparams.add(IdosConstants.TXN_TYPE_OPENING_BALANCE_BILLWISE_VEND);
		List<VendorBillwiseOpBalance> resultList = dao.queryWithParamsName(BRANCH_OB_PENDING_JPQL, em, inparams);
		return resultList;
	}

	public static double getPaidOpeningBalanceAmount(EntityManager em, Long orgId, Long vendId, Long branchId,
			long tranPurpose, GenericDAO dao, int txnIdentifier) {
		ArrayList inparams = new ArrayList(5);
		inparams.add(orgId);
		inparams.add(branchId);
		inparams.add(vendId);
		inparams.add(tranPurpose);
		inparams.add(txnIdentifier);
		List<Object[]> list = dao.queryWithParamsNameGeneric(BRANCH_OB_PAID_AMT_JPQL, em, inparams);
		double amount = 0.0;
		for (Object ob : list) {
			amount += (Double) (ob);
		}
		return amount;
	}
}
