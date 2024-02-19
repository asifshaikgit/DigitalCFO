package model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;

import com.idos.util.IdosDaoConstants;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "BRANCH_has_SPECIFICS")
public class BranchSpecifics extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BranchSpecifics() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String FIND_BY_SPECIFIC_JPQL = "select obj from BranchSpecifics obj WHERE obj.organization.id = ?1 and obj.specifics.id = ?2 and obj.presentStatus=1";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_id")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization_id")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specifics_id")
	private Specifics specifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "specifics_particular_id")
	private Particulars particular;

	@Column(name = "budget_date")
	private Date budgetDate;

	@Column(name = "budget_amount_jan")
	private Double budgetAmountJan;

	@Column(name = "budget_amount_feb")
	private Double budgetAmountFeb;

	@Column(name = "budget_amount_mar")
	private Double budgetAmountMar;

	@Column(name = "budget_amount_apr")
	private Double budgetAmountApr;

	@Column(name = "budget_amount_may")
	private Double budgetAmountMay;

	@Column(name = "budget_amount_june")
	private Double budgetAmountJune;

	@Column(name = "budget_amount_july")
	private Double budgetAmountJuly;

	@Column(name = "budget_amount_aug")
	private Double budgetAmountAug;

	@Column(name = "budget_amount_sep")
	private Double budgetAmountSep;

	@Column(name = "budget_amount_oct")
	private Double budgetAmountOct;

	@Column(name = "budget_amount_nov")
	private Double budgetAmountNov;

	@Column(name = "budget_amount_dec")
	private Double budgetAmountDec;

	@Column(name = "total")
	private Double budgetTotal;

	@Column(name = "budget_deducted_amount_jan")
	private Double budgetDeductedAmountJan;

	@Column(name = "budget_deducted_amount_feb")
	private Double budgetDeductedAmountFeb;

	@Column(name = "budget_deducted_amount_mar")
	private Double budgetDeductedAmountMar;

	@Column(name = "budget_deducted_amount_apr")
	private Double budgetDeductedAmountApr;

	@Column(name = "budget_deducted_amount_may")
	private Double budgetDeductedAmountMay;

	@Column(name = "budget_deducted_amount_june")
	private Double budgetDeductedAmountJune;

	@Column(name = "budget_deducted_amount_july")
	private Double budgetDeductedAmountJuly;

	@Column(name = "budget_deducted_amount_aug")
	private Double budgetDeductedAmountAug;

	@Column(name = "budget_deducted_amount_sep")
	private Double budgetDeductedAmountSep;

	@Column(name = "budget_deducted_amount_oct")
	private Double budgetDeductedAmountOct;

	@Column(name = "budget_deducted_amount_nov")
	private Double budgetDeductedAmountNov;

	@Column(name = "budget_deducted_amount_dec")
	private Double budgetDeductedAmountDec;

	@Column(name = "total_deducted")
	private Double budgetDeductedTotal;

	@Column(name = "WALKIN_CUSTOMER_MAX_DISCOUNT")
	private Double walkinCustomerMaxDiscount;

	@Column(name = "OPENING_BALANCE")
	private Double openingBalance;

	@Column(name = "INV_OPENING_BALANCE_UNITS")
	private Double invOpeningBalUnits;

	@Column(name = "INV_OPENING_BALANCE_RATE")
	private Double invOpeningBalRate;

	@Column(name = "INV_OPENING_BALANCE")
	private Double invOpeningBalance;

	public Double getBudgetDeductedAmountJan() {
		return budgetDeductedAmountJan;
	}

	public void setBudgetDeductedAmountJan(Double budgetDeductedAmountJan) {
		this.budgetDeductedAmountJan = budgetDeductedAmountJan;
	}

	public Double getBudgetDeductedAmountFeb() {
		return budgetDeductedAmountFeb;
	}

	public void setBudgetDeductedAmountFeb(Double budgetDeductedAmountFeb) {
		this.budgetDeductedAmountFeb = budgetDeductedAmountFeb;
	}

	public Double getBudgetDeductedAmountMar() {
		return budgetDeductedAmountMar;
	}

	public void setBudgetDeductedAmountMar(Double budgetDeductedAmountMar) {
		this.budgetDeductedAmountMar = budgetDeductedAmountMar;
	}

	public Double getBudgetDeductedAmountApr() {
		return budgetDeductedAmountApr;
	}

	public void setBudgetDeductedAmountApr(Double budgetDeductedAmountApr) {
		this.budgetDeductedAmountApr = budgetDeductedAmountApr;
	}

	public Double getBudgetDeductedAmountMay() {
		return budgetDeductedAmountMay;
	}

	public void setBudgetDeductedAmountMay(Double budgetDeductedAmountMay) {
		this.budgetDeductedAmountMay = budgetDeductedAmountMay;
	}

	public Double getBudgetDeductedAmountJune() {
		return budgetDeductedAmountJune;
	}

	public void setBudgetDeductedAmountJune(Double budgetDeductedAmountJune) {
		this.budgetDeductedAmountJune = budgetDeductedAmountJune;
	}

	public Double getBudgetDeductedAmountJuly() {
		return budgetDeductedAmountJuly;
	}

	public void setBudgetDeductedAmountJuly(Double budgetDeductedAmountJuly) {
		this.budgetDeductedAmountJuly = budgetDeductedAmountJuly;
	}

	public Double getBudgetDeductedAmountAug() {
		return budgetDeductedAmountAug;
	}

	public void setBudgetDeductedAmountAug(Double budgetDeductedAmountAug) {
		this.budgetDeductedAmountAug = budgetDeductedAmountAug;
	}

	public Double getBudgetDeductedAmountSep() {
		return budgetDeductedAmountSep;
	}

	public void setBudgetDeductedAmountSep(Double budgetDeductedAmountSep) {
		this.budgetDeductedAmountSep = budgetDeductedAmountSep;
	}

	public Double getBudgetDeductedAmountOct() {
		return budgetDeductedAmountOct;
	}

	public void setBudgetDeductedAmountOct(Double budgetDeductedAmountOct) {
		this.budgetDeductedAmountOct = budgetDeductedAmountOct;
	}

	public Double getBudgetDeductedAmountNov() {
		return budgetDeductedAmountNov;
	}

	public void setBudgetDeductedAmountNov(Double budgetDeductedAmountNov) {
		this.budgetDeductedAmountNov = budgetDeductedAmountNov;
	}

	public Double getBudgetDeductedAmountDec() {
		return budgetDeductedAmountDec;
	}

	public void setBudgetDeductedAmountDec(Double budgetDeductedAmountDec) {
		this.budgetDeductedAmountDec = budgetDeductedAmountDec;
	}

	public Double getBudgetDeductedTotal() {
		return budgetDeductedTotal;
	}

	public void setBudgetDeductedTotal(Double budgetDeductedTotal) {
		this.budgetDeductedTotal = budgetDeductedTotal;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "branchSpecifics")
	private List<BranchSpecificsTaxFormula> branchSpecificsTaxesFormulas;

	public List<BranchSpecificsTaxFormula> getBranchSpecificsTaxesFormulas() {
		return branchSpecificsTaxesFormulas;
	}

	public void setBranchSpecificsTaxesFormulas(
			List<BranchSpecificsTaxFormula> branchSpecificsTaxesFormulas) {
		this.branchSpecificsTaxesFormulas = branchSpecificsTaxesFormulas;
	}

	public Double getBudgetTotal() {
		return budgetTotal;
	}

	public void setBudgetTotal(Double budgetTotal) {
		this.budgetTotal = budgetTotal;
	}

	public Date getBudgetDate() {
		return budgetDate;
	}

	public void setBudgetDate(Date budgetDate) {
		this.budgetDate = budgetDate;
	}

	public Double getBudgetAmountJan() {
		return budgetAmountJan;
	}

	public void setBudgetAmountJan(Double budgetAmountJan) {
		this.budgetAmountJan = budgetAmountJan;
	}

	public Double getBudgetAmountFeb() {
		return budgetAmountFeb;
	}

	public void setBudgetAmountFeb(Double budgetAmountFeb) {
		this.budgetAmountFeb = budgetAmountFeb;
	}

	public Double getBudgetAmountMar() {
		return budgetAmountMar;
	}

	public void setBudgetAmountMar(Double budgetAmountMar) {
		this.budgetAmountMar = budgetAmountMar;
	}

	public Double getBudgetAmountApr() {
		return budgetAmountApr;
	}

	public void setBudgetAmountApr(Double budgetAmountApr) {
		this.budgetAmountApr = budgetAmountApr;
	}

	public Double getBudgetAmountMay() {
		return budgetAmountMay;
	}

	public void setBudgetAmountMay(Double budgetAmountMay) {
		this.budgetAmountMay = budgetAmountMay;
	}

	public Double getBudgetAmountJune() {
		return budgetAmountJune;
	}

	public void setBudgetAmountJune(Double budgetAmountJune) {
		this.budgetAmountJune = budgetAmountJune;
	}

	public Double getBudgetAmountJuly() {
		return budgetAmountJuly;
	}

	public void setBudgetAmountJuly(Double budgetAmountJuly) {
		this.budgetAmountJuly = budgetAmountJuly;
	}

	public Double getBudgetAmountAug() {
		return budgetAmountAug;
	}

	public void setBudgetAmountAug(Double budgetAmountAug) {
		this.budgetAmountAug = budgetAmountAug;
	}

	public Double getBudgetAmountSep() {
		return budgetAmountSep;
	}

	public void setBudgetAmountSep(Double budgetAmountSep) {
		this.budgetAmountSep = budgetAmountSep;
	}

	public Double getBudgetAmountOct() {
		return budgetAmountOct;
	}

	public void setBudgetAmountOct(Double budgetAmountOct) {
		this.budgetAmountOct = budgetAmountOct;
	}

	public Double getBudgetAmountNov() {
		return budgetAmountNov;
	}

	public void setBudgetAmountNov(Double budgetAmountNov) {
		this.budgetAmountNov = budgetAmountNov;
	}

	public Double getBudgetAmountDec() {
		return budgetAmountDec;
	}

	public void setBudgetAmountDec(Double budgetAmountDec) {
		this.budgetAmountDec = budgetAmountDec;
	}

	public Particulars getParticular() {
		return particular;
	}

	public void setParticular(Particulars particular) {
		this.particular = particular;
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

	public Specifics getSpecifics() {
		return specifics;
	}

	public void setSpecifics(Specifics specifics) {
		this.specifics = specifics;
	}

	public Long getEntityComparableParamId() {
		return getBranch().getId();
	}

	public Double getWalkinCustomerMaxDiscount() {
		return walkinCustomerMaxDiscount;
	}

	public void setWalkinCustomerMaxDiscount(Double walkinCustomerMaxDiscount) {
		this.walkinCustomerMaxDiscount = walkinCustomerMaxDiscount;
	}

	public Double getOpeningBalance() {
		return this.openingBalance;
	}

	public void setOpeningBalance(Double openingBalance) {
		this.openingBalance = openingBalance;
	}

	public Double getInvOpeningBalUnits() {
		return this.invOpeningBalUnits;
	}

	public void setInvOpeningBalUnits(Double invOpeningBalUnits) {
		this.invOpeningBalUnits = invOpeningBalUnits;
	}

	public Double getInvOpeningBalRate() {
		return this.invOpeningBalRate;
	}

	public void setInvOpeningBalRate(Double invOpeningBalRate) {
		this.invOpeningBalRate = invOpeningBalRate;
	}

	public Double getInvOpeningBalance() {
		return this.invOpeningBalance;
	}

	public void setInvOpeningBalance(Double invOpeningBalance) {
		this.invOpeningBalance = invOpeningBalance;
	}

	public static List<BranchSpecifics> findBranchBySpecific(EntityManager entityManager, long orgid, long specificId) {
		List<BranchSpecifics> branchSpecificList = null;
		Query query = entityManager.createQuery(FIND_BY_SPECIFIC_JPQL);
		query.setParameter(1, orgid);
		query.setParameter(2, specificId);
		branchSpecificList = (List<BranchSpecifics>) query.getResultList();
		return branchSpecificList;
	}
}
