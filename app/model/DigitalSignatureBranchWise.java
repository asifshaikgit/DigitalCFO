package model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "DIGITAL_SIGNATURE_BRANCH_WISE")
public class DigitalSignatureBranchWise extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public DigitalSignatureBranchWise() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ORGANIZATION_ID")
	private Organization organization;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "BRANCH_ID")
	private Branch branch;

	@Column(name = "PERSON_NAME")
	public String personName;

	@Column(name = "DESIGNATION")
	public String designation;

	@Column(name = "PHONE_NO")
	public String phoneNo;

	@Column(name = "EMAIL_ID")
	public String emailId;

	@Column(name = "DIGITAL_SIGN_DOCUMENTS ")
	public String digitalSignDocuments;

	@Column(name = "REF_NO")
	public String refNo;

	@Column(name = "KYC_DETAILS")
	public String kycDetails;

	@Column(name = "VALIDITY_FROM")
	public String validityFrom;

	@Column(name = "VALIDITY_TO")
	public String validityTo;

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

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getDigitalSignDocuments() {
		return digitalSignDocuments;
	}

	public void setDigitalSignDocuments(String digitalSignDocuments) {
		this.digitalSignDocuments = digitalSignDocuments;
	}

	public String getRefNo() {
		return refNo;
	}

	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}

	public String getKycDetails() {
		return kycDetails;
	}

	public void setKycDetails(String kycDetails) {
		this.kycDetails = kycDetails;
	}

	public String getValidityFrom() {
		return validityFrom;
	}

	public void setValidityFrom(String validityFrom) {
		this.validityFrom = validityFrom;
	}

	public String getValidityTo() {
		return validityTo;
	}

	public void setValidityTo(String validityTo) {
		this.validityTo = validityTo;
	}

	public static DigitalSignatureBranchWise findByOrgAndBranch(EntityManager entityManager, Long orgid,
			Long branchid) {
		List<DigitalSignatureBranchWise> digiSignData = null;
		DigitalSignatureBranchWise result = null;
		try {
			Query query = entityManager.createQuery(
					"select obj from DigitalSignatureBranchWise obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.presentStatus=1 order by obj.id desc");
			query.setParameter(1, orgid);
			query.setParameter(2, branchid);
			digiSignData = query.getResultList();
			if (digiSignData.size() > 0)
				result = digiSignData.get(0);
		} catch (NoResultException ex) {
		}
		return result;
	}
}
