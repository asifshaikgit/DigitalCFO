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

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "PAID_CLAIM_DETAILS")
public class PaidClaimsDetails extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public PaidClaimsDetails() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String ORG_PAID_CLAIMS_JPQL = "select obj from PaidClaimsDetails obj where obj.organization.id= ?1 and obj.presentStatus=1";

	private static final long serialVersionUID = -9077507882580229320L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CLAIM_ID")
	private ClaimTransaction transaction;

	@Column(name = "PAID_AMT")
	private Double paidAmt;

	@Column(name = "PAYMENT_DATE")
	private Date paymentDate;

	@Column(name = "PAY_MODE")
	private String payMode;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public ClaimTransaction getTransaction() {
		return transaction;
	}

	public void setTransaction(ClaimTransaction transaction) {
		this.transaction = transaction;
	}

	public Double getPaidAmt() {
		return paidAmt;
	}

	public void setPaidAmt(Double paidAmt) {
		this.paidAmt = paidAmt;
	}

	public Date getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(Date paymentDate) {
		this.paymentDate = paymentDate;
	}

	public String getPayMode() {
		return payMode;
	}

	public void setPayMode(String payMode) {
		this.payMode = payMode;
	}

	public static PaidClaimsDetails findById(Long id) {
		return entityManager.find(PaidClaimsDetails.class, id);
	}

	public static List<PaidClaimsDetails> findOrgPaidClaims(EntityManager entityManager, Long orgid) {
		Query query = entityManager.createQuery(ORG_PAID_CLAIMS_JPQL);
		query.setParameter(1, orgid);
		List<PaidClaimsDetails> resultList = query.getResultList();
		return resultList;
	}
}
