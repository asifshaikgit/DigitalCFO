package model.karvy;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import model.AbstractBaseModel;
import model.Organization;
import model.Users;
import play.db.jpa.JPAApi;
import javax.inject.Inject;

@Entity
@Table(name = "EXTERNAL_USER_COMPANY_DETAILS")
public class ExternalUserCompanyDetails extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public ExternalUserCompanyDetails() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	private static final String EXT_USR_CMPNY_LIST_HQL = "select obj from ExternalUserCompanyDetails obj WHERE obj.extUserId.id=?1 and obj.externalUserStatus=1 and obj.presentStatus=1";
	private static final String EXT_USR_CMPNY_TO_BE_ADDED_LIST_HQL = "select obj from ExternalUserCompanyDetails obj WHERE obj.extUserId.id=?1 and obj.externalUserStatus=0 and obj.presentStatus=1";

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COMPANY_ORG_ID")
	private Organization org;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "EXTERNAL_USER_ID")
	private Users extUserId;

	@Column(name = "ACCESS_CODE")
	private String accessCode;

	@Column(name = "COMPANY_URL")
	private String companyUrl;

	@Column(name = "DATE_OF_ACCESS")
	private Date dateOfAccess;

	@Column(name = "DATE_OF_ACCEPTANCE")
	private Date dateOfAcceptance;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CORRESPONDING_USERID")
	private Users corrUserId;

	@Column(name = "ORG_OTP")
	private String orgOtp;

	public String getOrgOtp() {
		return orgOtp;
	}

	public void setOrgOtp(String orgOtp) {
		this.orgOtp = orgOtp;
	}

	public Users getCorrUserId() {
		return corrUserId;
	}

	public void setCorrUserId(Users corrUserId) {
		this.corrUserId = corrUserId;
	}

	public Date getDateOfAcceptance() {
		return dateOfAcceptance;
	}

	public void setDateOfAcceptance(Date dateOfAcceptance) {
		this.dateOfAcceptance = dateOfAcceptance;
	}

	public Organization getOrg() {
		return org;
	}

	public void setOrg(Organization org) {
		this.org = org;
	}

	public Users getExtUserId() {
		return extUserId;
	}

	public void setExtUserId(Users extUserId) {
		this.extUserId = extUserId;
	}

	public String getAccessCode() {
		return accessCode;
	}

	public void setAccessCode(String accessCode) {
		this.accessCode = accessCode;
	}

	public String getCompanyUrl() {
		return companyUrl;
	}

	public void setCompanyUrl(String companyUrl) {
		this.companyUrl = companyUrl;
	}

	public Date getDateOfAccess() {
		return dateOfAccess;
	}

	public void setDateOfAccess(Date dateOfAccess) {
		this.dateOfAccess = dateOfAccess;
	}

	public Integer getExternalUserStatus() {
		return externalUserStatus;
	}

	public void setExternalUserStatus(Integer externalUserStatus) {
		this.externalUserStatus = externalUserStatus;
	}

	@Column(name = "EXTERNAL_USER_STATUS")
	private Integer externalUserStatus;

	public static List<ExternalUserCompanyDetails> findCompanyList(EntityManager entityManager, Long extUserId) {
		List<ExternalUserCompanyDetails> companyList = null;
		Query query = entityManager.createQuery(EXT_USR_CMPNY_LIST_HQL);
		query.setParameter(1, extUserId);
		companyList = query.getResultList();
		return companyList;
	}

	public static List<ExternalUserCompanyDetails> findCompanyToBeAddedList(EntityManager entityManager,
			Long extUserId) {
		List<ExternalUserCompanyDetails> companyToBeAddedList = null;
		Query query = entityManager.createQuery(EXT_USR_CMPNY_TO_BE_ADDED_LIST_HQL);
		query.setParameter(1, extUserId);
		companyToBeAddedList = query.getResultList();
		return companyToBeAddedList;
	}

}
