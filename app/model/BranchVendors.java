package model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.idos.dao.GenericDAO;
import com.idos.util.IdosConstants;
import com.idos.util.IdosDaoConstants;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
@Table(name = "BRANCH_has_VENDORS")
public class BranchVendors extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BranchVendors() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String BRANCH_OB_PENDING_JPQL = "select t1 from BranchVendors t1 where t1.organization.id= ?1 and t1.branch.id = ?2  and t1.vendor.id = ?3 and t1.presentStatus = 1 and t1.openingBalance > COALESCE((select sum(t2.vendorNetPayment) from Transaction t2 where t2.transactionBranchOrganization.id = ?4 and t2.transactionBranch.id = ?5 and t2.transactionVendorCustomer.id = ?6 and t2.transactionPurpose.id = ?7 and t2.typeIdentifier = ?8 and t2.presentStatus = 1 group by t2.paidInvoiceRefNumber), 0)";
	private static final String FIND_BY_VENDOR_JPQL = "select obj from BranchVendors obj WHERE obj.organization.id = ?1 and obj.vendor.id = ?2 and obj.presentStatus=1";
	private static final String VENDOR_BRANCH_JPQL = "select obj from BranchVendors obj WHERE obj.organization.id = ?1 and obj.branch.id = ?2 and obj.vendor.id = ?3 and obj.presentStatus=1";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id")
	private Vendor vendor;

	@Column(name = "ORIGINAL_OPENING_BALANCE") // opening bal for customer receivables/vendor payables- this will not
												// change when Receive payment from customer
	private Double originalOpeningBalance = 0.0;

	@Column(name = "OPENING_BALANCE") // opening bal for customer receivables/vendor payables-this will change when
										// receive payment from customer
	private Double openingBalance = 0.0;

	@Column(name = "OPENING_BALANCE_ADVANCEPAID") // Opening balance for advance paid by customer/ to vendor
	private Double openingBalanceAdvPaid = 0.0;

	@Column(name = "ORIGINAL_OPENING_BALANCE_ADVANCEPAID") // advance paid opening bal for customer receivables/vendor
															// payables- this will not change when Receive payment from
															// customer
	private Double originalOpeningBalanceAdvPaid = 0.0;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Vendor getVendor() {
		return vendor;
	}

	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}

	public Long getEntityComparableParamId() {
		return getBranch().getId();
	}

	public Double getOriginalOpeningBalance() {
		return this.originalOpeningBalance;
	}

	public void setOriginalOpeningBalance(Double originalOpeningBalance) {
		this.originalOpeningBalance = originalOpeningBalance;
	}

	public Double getOpeningBalance() {
		return this.openingBalance;
	}

	public void setOpeningBalance(Double openingBalance) {
		this.openingBalance = openingBalance;
	}

	public Double getOpeningBalanceAdvPaid() {
		return this.openingBalanceAdvPaid;
	}

	public void setOpeningBalanceAdvPaid(Double openingBalanceAdvPaid) {
		this.openingBalanceAdvPaid = openingBalanceAdvPaid;
	}

	public Double getOriginalOpeningBalanceAdvPaid() {
		return this.originalOpeningBalanceAdvPaid;
	}

	public void setOriginalOpeningBalanceAdvPaid(Double originalOpeningBalanceAdvPaid) {
		this.originalOpeningBalanceAdvPaid = originalOpeningBalanceAdvPaid;
	}

	public static List<BranchVendors> findByVendor(EntityManager entityManager, long orgid, long vendor) {
		List<BranchVendors> branchVendList = null;
		Query query = entityManager.createQuery(FIND_BY_VENDOR_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, vendor);
		branchVendList = (List<BranchVendors>) query.getResultList();
		return branchVendList;
	}

	public static List<BranchVendors> getPendingOpeningBalance(EntityManager entityManager, Long orgId, Long vendId,
			Long branchId, long tranPurpose) {
		Query query = entityManager.createQuery(BRANCH_OB_PENDING_JPQL);
		query.setParameter(1, orgId);
		query.setParameter(2, branchId);
		query.setParameter(3, vendId);
		query.setParameter(4, orgId);
		query.setParameter(5, branchId);
		query.setParameter(6, vendId);
		query.setParameter(7, tranPurpose);
		query.setParameter(8, IdosConstants.TXN_TYPE_OPENING_BALANCE_VEND);
		List<BranchVendors> resultList = query.getResultList();
		return resultList;
	}

	public static BranchVendors findByVendorBranch(EntityManager entityManager, long orgid, long branchId,
			long vendor) {
		List<BranchVendors> branchVendList = null;
		Query query = entityManager.createQuery(VENDOR_BRANCH_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, branchId);
		query.setParameter(3, vendor);
		branchVendList = (List<BranchVendors>) query.getResultList();
		if (branchVendList.size() > 0) {
			return branchVendList.get(0);
		} else {
			return null;
		}
	}
}
