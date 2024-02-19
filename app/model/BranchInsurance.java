package model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.idos.util.IdosUtil;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.Query;

@Entity
@Table(name = "BRANCH_INSURANCE")
public class BranchInsurance extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;
	private static final String GET_BY_BRANCH_ID = "select obj from BranchInsurance obj WHERE obj.organization.id = ?1 and obj.branch.id = ?2 and obj.presentStatus=1";

	public BranchInsurance() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@Column(name = "insurence_type")
	private String insurenceType;

	@Column(name = "policy_number")
	private String policyNumber;

	@Column(name = "insurence_company")
	private String insurenceCompany;

	@Column(name = "annual_premium")
	private Double annualPremium;

	@Column(name = "policy_type")
	private String policyType;

	@Column(name = "policy_valid_from")
	private Date policyValidFrom;

	@Column(name = "policy_valid_to")
	private Date policyValidTo;

	@Column(name = "remarks")
	private String remarks;

	@Column(name = "alert_for_action")
	private String alertOfAction;

	@Column(name = "alert_for_information")
	private String alertOfInformation;

	@Column(name = "LAST_INSURENCE_VALIDITY_UPDATED_DATE")
	private Date lastInsurenceVlidityUpdatedDate;

	@Column(name = "LAST_INSURENCE_ANNUAL_PREMIUM_PAID_DATE")
	private Date lastInsurenceAnnualPremiumPaidDate;

	@Column(name = "LAST_INSURENCE_ANNUAL_PREMIUM_DUE_DATED")
	private Date lastInsurenceAnnualPremiumDueDated;

	public Date getLastInsurenceVlidityUpdatedDate() {
		return lastInsurenceVlidityUpdatedDate;
	}

	public void setLastInsurenceVlidityUpdatedDate(
			Date lastInsurenceVlidityUpdatedDate) {
		this.lastInsurenceVlidityUpdatedDate = lastInsurenceVlidityUpdatedDate;
	}

	public Date getLastInsurenceAnnualPremiumPaidDate() {
		return lastInsurenceAnnualPremiumPaidDate;
	}

	public void setLastInsurenceAnnualPremiumPaidDate(
			Date lastInsurenceAnnualPremiumPaidDate) {
		this.lastInsurenceAnnualPremiumPaidDate = lastInsurenceAnnualPremiumPaidDate;
	}

	public Date getLastInsurenceAnnualPremiumDueDated() {
		return lastInsurenceAnnualPremiumDueDated;
	}

	public void setLastInsurenceAnnualPremiumDueDated(
			Date lastInsurenceAnnualPremiumDueDated) {
		this.lastInsurenceAnnualPremiumDueDated = lastInsurenceAnnualPremiumDueDated;
	}

	public String getAlertOfAction() {
		return alertOfAction;
	}

	public void setAlertOfAction(String alertOfAction) {
		this.alertOfAction = alertOfAction;
	}

	public String getAlertOfInformation() {
		return alertOfInformation;
	}

	public void setAlertOfInformation(String alertOfInformation) {
		this.alertOfInformation = alertOfInformation;
	}

	public String getInsurenceType() {
		return insurenceType;
	}

	public void setInsurenceType(String insurenceType) {
		this.insurenceType = IdosUtil.escapeHtml(insurenceType);
	}

	public String getPolicyNumber() {
		return policyNumber;
	}

	public void setPolicyNumber(String policyNumber) {
		this.policyNumber = IdosUtil.escapeHtml(policyNumber);
	}

	public String getInsurenceCompany() {
		return insurenceCompany;
	}

	public void setInsurenceCompany(String insurenceCompany) {
		this.insurenceCompany = IdosUtil.escapeHtml(insurenceCompany);
	}

	public Double getAnnualPremium() {
		return annualPremium;
	}

	public void setAnnualPremium(Double annualPremium) {
		this.annualPremium = annualPremium;
	}

	public Date getPolicyValidFrom() {
		return policyValidFrom;
	}

	public void setPolicyValidFrom(Date policyValidFrom) {
		this.policyValidFrom = policyValidFrom;
	}

	public Date getPolicyValidTo() {
		return policyValidTo;
	}

	public void setPolicyValidTo(Date policyValidTo) {
		this.policyValidTo = policyValidTo;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = IdosUtil.escapeHtml(remarks);
	}

	@Column(name = "INSURANCE_POLICY_DOC_URL")
	private String insurancePolicyDocUrl;

	@Column(name = "INSURANCE_POLICY_EXPIRY_DATE")
	private Date insurancePolicyExpDt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	public String getPolicyType() {
		return policyType;
	}

	public void setPolicyType(String policyType) {
		this.policyType = policyType;
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

	public String getInsurancePolicyDocUrl() {
		return insurancePolicyDocUrl;
	}

	public void setInsurancePolicyDocUrl(String insurancePolicyDocUrl) {
		this.insurancePolicyDocUrl = insurancePolicyDocUrl;
	}

	public Date getInsurancePolicyExpDt() {
		return insurancePolicyExpDt;
	}

	public void setInsurancePolicyExpDt(Date insurancePolicyExpDt) {
		this.insurancePolicyExpDt = insurancePolicyExpDt;
	}

	/**
	 * Find a BranchInsurance by id.
	 */
	public static BranchInsurance findById(Long id) {
		return entityManager.find(BranchInsurance.class, id);
	}

	public static List<BranchInsurance> getBranchInsuranceList(EntityManager entityManager, Long orgId,
			Long branchID)
			throws Exception {
		List<BranchInsurance> branchInsuranceList = null;

		Query query = entityManager.createQuery(GET_BY_BRANCH_ID);
		query.setParameter(1, orgId);
		query.setParameter(2, branchID);
		branchInsuranceList = query.getResultList();
		return branchInsuranceList;
	}
}
