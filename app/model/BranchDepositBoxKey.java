package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.idos.dao.GenericDAO;
import com.idos.util.IdosUtil;
import play.data.validation.Constraints;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

@Entity
@Table(name = "BRANCH_DEPOSITBOX_KEY")
public class BranchDepositBoxKey extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BranchDepositBoxKey() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "name")
	private String name;

	@Column(name = "email", length = 256)
	@Constraints.MaxLength(256)
	@Constraints.Email
	private String email;

	public String getCashierName() {
		return cashierName;
	}

	public void setCashierName(String cashierName) {
		this.cashierName = IdosUtil.escapeHtml(cashierName);
	}

	public String getCashierPhnNoCountryCode() {
		return cashierPhnNoCountryCode;
	}

	public void setCashierPhnNoCountryCode(String cashierPhnNoCountryCode) {
		this.cashierPhnNoCountryCode = cashierPhnNoCountryCode;
	}

	public String getCashierEmail() {
		return cashierEmail;
	}

	public void setCashierEmail(String cashierEmail) {
		this.cashierEmail = cashierEmail;
	}

	public String getCashierKnowledgeLibrary() {
		return cashierKnowledgeLibrary;
	}

	public void setCashierKnowledgeLibrary(String cashierKnowledgeLibrary) {
		this.cashierKnowledgeLibrary = cashierKnowledgeLibrary;
	}

	@Column(name = "country_phone_code")
	private String countryPhnCode;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "cashier_name")
	private String cashierName;

	@Column(name = "cashier_phone_number_country_code")
	private String cashierPhnNoCountryCode;

	@Column(name = "cashier_phone_number")
	private String cashierPhnNo;

	public String getCashierPhnNo() {
		return cashierPhnNo;
	}

	public void setCashierPhnNo(String cashierPhnNo) {
		this.cashierPhnNo = cashierPhnNo;
	}

	@Column(name = "cashier_email", length = 256)
	@Constraints.MaxLength(256)
	@Constraints.Email
	private String cashierEmail;

	@Column(name = "cashier_knowledge_library")
	private String cashierKnowledgeLibrary;

	@Column(name = "cashier_status")
	private Integer cashierStatus;

	public Integer getCashierStatus() {
		return cashierStatus;
	}

	public void setCashierStatus(Integer cashierStatus) {
		this.cashierStatus = cashierStatus;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	@Column(name = "PETTY_CASH_TXN_APPROVAL_REQD")
	private Integer pettyCashTxnApprovalRequired = 0;

	@Column(name = "APPROVAL_AMOUNT_LIMIT")
	private Double approvalAmountLimit;

	@Column(name = "PETTY_CASH_OPENING_BALANCE")
	private Double pettyCashOpeningBalance;

	public Double getPettyCashOpeningBalance() {
		return pettyCashOpeningBalance;
	}

	public void setPettyCashOpeningBalance(Double pettyCashOpeningBalance) {
		this.pettyCashOpeningBalance = pettyCashOpeningBalance;
	}

	public Integer getPettyCashTxnApprovalRequired() {
		return pettyCashTxnApprovalRequired;
	}

	public void setPettyCashTxnApprovalRequired(Integer pettyCashTxnApprovalRequired) {
		this.pettyCashTxnApprovalRequired = pettyCashTxnApprovalRequired;
	}

	public Double getApprovalAmountLimit() {
		return approvalAmountLimit;
	}

	public void setApprovalAmountLimit(Double approvalAmountLimit) {
		this.approvalAmountLimit = approvalAmountLimit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = IdosUtil.escapeHtml(name);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

	public String getCountryPhnCode() {
		return countryPhnCode;
	}

	public void setCountryPhnCode(String countryPhnCode) {
		this.countryPhnCode = countryPhnCode;
	}

	/**
	 * Find a BranchDepositBoxKey by id.
	 */
	public static BranchDepositBoxKey findById(Long id) {
		return entityManager.find(BranchDepositBoxKey.class, id);
	}

	@Column(name = "opening_balance")
	private Double openingBalance;

	public Double getOpeningBalance() {
		return openingBalance;
	}

	public void setOpeningBalance(Double openingBalance) {
		this.openingBalance = openingBalance;
	}

	public static boolean isCashOrPettyCashInvolve(Long orgId, Long branchId, String type, Long id,
			GenericDAO genericDAO, EntityManager entityManager) {

		BranchDepositBoxKey branchDepositBoxKey = findById(id);
		Map<String, Object> criterias = new HashMap<String, Object>();
		criterias.put("organization.id", orgId);
		criterias.put("branch.id", branchId);
		criterias.put("presentStatus", 1);

		if (branchDepositBoxKey != null) {
			if (type.equals("Cash")) {
				if (branchDepositBoxKey.getOpeningBalance() != null && branchDepositBoxKey.getOpeningBalance() != 0d) {
					criterias.put("branchDepositBoxKey.id", id);
					criterias.put("cashType", 1);

				} else {
					return false;
				}

			} else if (type.equals("PettyCash")) {
				if (branchDepositBoxKey.getPettyCashOpeningBalance() != null
						&& branchDepositBoxKey.getPettyCashOpeningBalance() != 0d) {
					criterias.put("branchDepositBoxKey.id", id);
					criterias.put("cashType", 2);
				} else {
					return false;
				}
			}
		} else {
			return false;
		}

		List<TrialBalanceBranchCash> trialBalanceBranchCashList = genericDAO
				.findByCriteria(TrialBalanceBranchCash.class, criterias, entityManager);

		if (trialBalanceBranchCashList != null && !trialBalanceBranchCashList.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
}
