package model;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import service.EntityManagerProvider;

@Entity
@Table(name = "BRANCH_BANK_ACCOUNT_MAPPING")
public class BranchBankAccountMapping extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public BranchBankAccountMapping() {
		entityManager = EntityManagerProvider.getEntityManager();
	}
	private static final String ORG_BRANCH_SPEC_EQUL_HQL = "select obj from BranchBankAccountMapping obj WHERE obj.organization.id=?1 and obj.branch.id = ?2 and obj.branchBankAccounts.id=?3 and obj.specifics.id=?4";
	private static final String ORG_BRANCH_EQUL_HQL = "select obj from BranchBankAccountMapping obj WHERE obj.organization.id=?1 and obj.branch.id = ?2";
	private static final String ORG_BRANCH_BANK_ID = "select obj from BranchBankAccountMapping obj WHERE obj.branchBankAccounts.id=?1";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	public Long id;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "BANK_ACCOUNT_ID")
	private BranchBankAccounts branchBankAccounts;

	
	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "BANK_ACCOUNT_DETAILS_ID")
	private BranchBankAccountBalance branchBankAccountBalance;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ID")
	private Branch branch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ORGANIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SPECIFICS_ID")
	private Specifics specifics;


	public BranchBankAccounts getBranchBankAccounts() {
		return branchBankAccounts;
	}

	public void setBranchBankAccounts(BranchBankAccounts branchBankAccounts) {
		this.branchBankAccounts = branchBankAccounts;
	}

	public BranchBankAccountBalance getBranchBankAccountBalance() {
		return branchBankAccountBalance;
	}

	public void setBranchBankAccountBalance(BranchBankAccountBalance branchBankAccountBalance) {
		this.branchBankAccountBalance = branchBankAccountBalance;
	}

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Specifics getSpecifics() {
		return specifics;
	}

	public void setSpecifics(Specifics specifics) {
		this.specifics = specifics;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	/**
	 * Find a BranchBankAccountMapping by id.
	 */
	public static BranchBankAccountMapping findById(Long id) {
		return entityManager.find(BranchBankAccountMapping.class, id);
	}

	public static BranchBankAccountMapping findByBranchOrgAndBankId(EntityManager entityManager, Organization orgn, Branch branch, BranchBankAccounts branchBank, Specifics specs) {
		try{
			Query query = entityManager.createQuery(ORG_BRANCH_SPEC_EQUL_HQL);
			BranchBankAccountMapping branches = null;
			query.setParameter(1, orgn.getId());
			query.setParameter(2, branch.getId());
			query.setParameter(3, branchBank.getId());
			query.setParameter(4, specs.getId());
			branches =  (BranchBankAccountMapping) query.getSingleResult();
			return branches;
		}	catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static List<BranchBankAccountMapping> findByBranchOrgId(EntityManager entityManager, Organization orgn, Branch branch) {
		try{
			Query query = entityManager.createQuery(ORG_BRANCH_EQUL_HQL);
			List<BranchBankAccountMapping> branches = null;
			query.setParameter(1, orgn.getId());
			query.setParameter(2, branch.getId());
			branches = query.getResultList();
			return branches;
		}	catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static List<BranchBankAccountMapping> findByBankId(EntityManager entityManager, BranchBankAccounts branchBank) {
		try{
			Query query = entityManager.createQuery(ORG_BRANCH_BANK_ID);
			List<BranchBankAccountMapping> branches = null;
			query.setParameter(1, branchBank.getId());
			branches =  query.getResultList();
			return branches;
		}	catch(Exception e){
			return null;
		}
	}

	
  public static BranchBankAccountMapping findBySpecific(EntityManager entityManager, Specifics specs){
		try{
			Query query = entityManager.createQuery("select t1 from BranchBankAccountMapping t1 where t1.specifics.id=?1");
			BranchBankAccountMapping branches = null;
			query.setParameter(1, specs.getId());
			List<BranchBankAccountMapping> listBrancheSpecs= query.getResultList();
			if(listBrancheSpecs.size() > 0){
				branches = listBrancheSpecs.get(0);
			}
			return branches;
		}	catch(Exception e){
			e.printStackTrace();
			return null;
		}
  }
}
