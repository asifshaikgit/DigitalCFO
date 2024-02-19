package model.PLBSCOAMap;

import javax.persistence.*;

import model.AbstractBaseModel;
import model.Branch;
import model.Organization;

@Entity
@Table(name="PLBSCOAMAP")
public class PLBSCOAMap extends AbstractBaseModel{

	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;

	//Branch which initiated this transaction
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BRANCH_ID")
	private Branch branch;

	@Column(name="PLBSHEAD")
	private int plbsHead;
	
	@Column(name="COA_ID")
	private String coaId;

	/**
	 * @return the
	 */
	public  Organization getOrganizationId() {
		return organization;
	}

	/**
	 * @param organizationId the organizationId to set
	 */
	public  void setOrganizationId(Organization organizationId) {
		this.organization = organizationId;
	}

	/**
	 * @return the plbsHead
	 */
	public  int getPlbsHead() {
		return plbsHead;
	}

	/**
	 * @param plbsHead the plbsHead to set
	 */
	public  void setPlbsHead(int plbsHead) {
		this.plbsHead = plbsHead;
	}

	/**
	 * @return the coaId
	 */
	public  String getCoaId() {
		return coaId;
	}

	/**
	 * @param coaId the coaId to set
	 */
	public  void setCoaId(String coaId) {
		this.coaId = coaId;
	}
	
	
	
}
