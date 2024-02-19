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
import javax.persistence.Query;

import com.idos.dao.GenericDAO;
import com.idos.util.IdosUtil;
import play.data.validation.Constraints;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

@Entity
@Table(name = "BRANCH_BANK_ACCOUNTS")
public class BranchBankAccounts extends AbstractBaseModel {
    private static JPAApi jpaApi;
    private static EntityManager entityManager;

    public BranchBankAccounts() {
        entityManager = EntityManagerProvider.getEntityManager();
    }

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_type")
    private Integer accountType;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "opening_balance")
    private Double openingBalance;

    @Column(name = "authorized_signatory_name")
    private String AuthorizedSignatoryName;

    @Column(name = "authorized_signatory_email", length = 256)
    @Constraints.MaxLength(256)
    @Constraints.Email
    private String AuthorizedSignatoryEmail;

    @Column(name = "bank_address")
    private String bankAddress;

    @Column(name = "bank_number_country_phonecode")
    private String bankNumberPhnCtryCode;

    @Column(name = "bank_phone_number")
    private String phoneNumber;

    @Column(name = "swift_code")
    private String swiftCode;

    @Column(name = "ifsc_code")
    private String ifscCode;

    @Column(name = "checkbook_custody_name")
    private String checkBookCustodtName;

    @Column(name = "checkbook_custody_email", length = 256)
    @Constraints.MaxLength(256)
    @Constraints.Email
    private String checkBookCustodyEmail;

    @Column(name = "routing_number")
    private String routingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_organization_id")
    private Organization organization;

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

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = IdosUtil.escapeHtml(routingNumber);
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = IdosUtil.escapeHtml(bankName);
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = IdosUtil.escapeHtml(accountNumber);
    }

    public Double getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(Double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public String getAuthorizedSignatoryName() {
        return AuthorizedSignatoryName;
    }

    public void setAuthorizedSignatoryName(String authorizedSignatoryName) {
        AuthorizedSignatoryName = IdosUtil.escapeHtml(authorizedSignatoryName);
    }

    public String getAuthorizedSignatoryEmail() {
        return AuthorizedSignatoryEmail;
    }

    public void setAuthorizedSignatoryEmail(String authorizedSignatoryEmail) {
        AuthorizedSignatoryEmail = authorizedSignatoryEmail;
    }

    public String getBankAddress() {
        return bankAddress;
    }

    public void setBankAddress(String bankAddress) {
        this.bankAddress = IdosUtil.escapeHtml(bankAddress);
    }

    public String getBankNumberPhnCtryCode() {
        return bankNumberPhnCtryCode;
    }

    public void setBankNumberPhnCtryCode(String bankNumberPhnCtryCode) {
        this.bankNumberPhnCtryCode = bankNumberPhnCtryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public void setSwiftCode(String swiftCode) {
        this.swiftCode = IdosUtil.escapeHtml(swiftCode);
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = IdosUtil.escapeHtml(ifscCode);
    }

    public String getCheckBookCustodtName() {
        return checkBookCustodtName;
    }

    public void setCheckBookCustodtName(String checkBookCustodtName) {
        this.checkBookCustodtName = IdosUtil.escapeHtml(checkBookCustodtName);
    }

    public String getCheckBookCustodyEmail() {
        return checkBookCustodyEmail;
    }

    public void setCheckBookCustodyEmail(String checkBookCustodyEmail) {
        this.checkBookCustodyEmail = checkBookCustodyEmail;
    }

    /**
     * Find a BranchBankAccounts by id.
     */
    public static BranchBankAccounts findById(Long id) {
        return entityManager.find(BranchBankAccounts.class, id);
    }

    public static List<BranchBankAccounts> findByOrg(EntityManager entityManager, Organization org) {
        String sbquery = "select obj from BranchBankAccounts obj where obj.organization.id=?1";
		List<BranchBankAccounts> branches = null;
        Query query = entityManager.createQuery(sbquery);
        query.setParameter(1, org.getId());
		branches = query.getResultList();
		return branches;
    }

    /*public BranchBankAccountMapping getBranchBankMapping(EntityManager entityManager, BranchBankAccounts branchBank, Long branch) {
		try{
			Query query = entityManager.createQuery("select obj from BranchBankAccountMapping obj WHERE obj.organization.id=?1 and obj.branch.id = ?2 and obj.branchBankAccounts.id=?3");
			BranchBankAccountMapping branches = null;
			query.setParameter(1, branchBank.getOrganization().getId());
			query.setParameter(2, branch);
			query.setParameter(3, branchBank.getId());
			branches =  (BranchBankAccountMapping) query.getSingleResult();
			return branches;
		}	catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}*/

    public static boolean isBankAccountInvolve(Long orgId, Long branchId, String type, Long id, GenericDAO genericDAO,
            EntityManager entityManager) {

        BranchBankAccounts branchBankAccounts = findById(id);
        Map<String, Object> criterias = new HashMap<String, Object>(3);
        criterias.put("organization.id", orgId);
        criterias.put("branch.id", branchId);
        criterias.put("presentStatus", 1);
        if (branchBankAccounts != null) {
            if (type.equals("Bank")) {
                if (branchBankAccounts.getOpeningBalance() != null && branchBankAccounts.getOpeningBalance() != 0d) {
                    criterias.put("branchBankAccounts.id", id);
                } else {
                    return false;
                }

            }
        } else {
            return false;
        }
        List<TrialBalanceBranchBank> trialBalanceBranchBankList = genericDAO
                .findByCriteria(TrialBalanceBranchBank.class, criterias, entityManager);

        if (trialBalanceBranchBankList != null && !trialBalanceBranchBankList.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
}
