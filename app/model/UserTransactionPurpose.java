package model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Entity
@Table(name="USER_TRANSACTION_PURPOSE")
public class UserTransactionPurpose extends AbstractBaseModel {
	
	private static final String USR_TRX_PRPS = "select obj from UserTransactionPurpose obj where obj.user.id = ?1 and obj.organization.id = ?2 and obj.presentStatus = 1"; 
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="users_id")
	private Users user;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID")
	private Branch branch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ORGANIZATION_ID")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRANSACTION_PURPOSE_ID")
	private TransactionPurpose transactionPurpose;

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
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

	public TransactionPurpose getTransactionPurpose() {
		return transactionPurpose;
	}

	public void setTransactionPurpose(TransactionPurpose transactionPurpose) {
		this.transactionPurpose = transactionPurpose;
	}
	
	public Long getEntityComparableParamId(){
		return getTransactionPurpose().getId();
	}

	public static List<UserTransactionPurpose> getUserTransactionListByUserId(EntityManager em, Long orgId, Long userId){
		List<UserTransactionPurpose> userTxnPurposeList = null;
		Query query = em.createQuery(USR_TRX_PRPS);
		query.setParameter(1,userId);		
		query.setParameter(2,orgId);
		userTxnPurposeList = query.getResultList();
		return userTxnPurposeList;
	}

}
