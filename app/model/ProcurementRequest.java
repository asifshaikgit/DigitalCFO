package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import play.db.jpa.JPAApi;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;

@Entity
@Table(name = "PROCUREMENT_REQUEST")
public class ProcurementRequest extends AbstractBaseModel {
	private static JPAApi jpaApi;
	private static EntityManager entityManager;

	public ProcurementRequest() {
		entityManager = EntityManagerProvider.getEntityManager();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ITEM_SPECIFICS")
	private Specifics specifics;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ITEM_SPECIFICS_PARTICULARS")
	private Particulars particular;

	@Column(name = "NO_OF_UNITS")
	private Integer noOfUnits;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch")
	private Branch procBranch;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branch_organization")
	private Organization procOrganization;

	@Column(name = "PROCUREMENT_STATUS")
	private String procurementStatus;

	public String getProcurementRemarks() {
		return procurementRemarks;
	}

	public void setProcurementRemarks(String procurementRemarks) {
		this.procurementRemarks = procurementRemarks;
	}

	@Column(name = "PROCUREMENT_REMARKS")
	private String procurementRemarks;

	public String getProcurementStatus() {
		return procurementStatus;
	}

	public void setProcurementStatus(String procurementStatus) {
		this.procurementStatus = procurementStatus;
	}

	public Branch getProcBranch() {
		return procBranch;
	}

	public void setProcBranch(Branch procBranch) {
		this.procBranch = procBranch;
	}

	public Organization getProcOrganization() {
		return procOrganization;
	}

	public void setProcOrganization(Organization procOrganization) {
		this.procOrganization = procOrganization;
	}

	public Specifics getSpecifics() {
		return specifics;
	}

	public void setSpecifics(Specifics specifics) {
		this.specifics = specifics;
	}

	public Particulars getParticular() {
		return particular;
	}

	public void setParticular(Particulars particular) {
		this.particular = particular;
	}

	public Integer getNoOfUnits() {
		return noOfUnits;
	}

	public void setNoOfUnits(Integer noOfUnits) {
		this.noOfUnits = noOfUnits;
	}

	/**
	 * Find a ProcurementRequest by id.
	 */
	public static ProcurementRequest findById(Long id) {
		return entityManager.find(ProcurementRequest.class, id);
	}
}
