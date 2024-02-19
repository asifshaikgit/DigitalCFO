package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="TRAVELGROUP_KNOWLEDGE_LIBRARY")
public class TravelGroupKnowledgeLibrary extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TRAVEL_GROUP_ID")
	private Travel_Group travelgroup;
	
	@Column(name="KL_CONTENT")
	private String klContent;
	
	@Column(name="KL_MANDATORY")
	private Integer klMandatory;

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	public Travel_Group getTravelgroup() {
		return travelgroup;
	}

	public void setTravelgroup(Travel_Group travelgroup) {
		this.travelgroup = travelgroup;
	}

	public String getKlContent() {
		return klContent;
	}

	public void setKlContent(String klContent) {
		this.klContent = klContent;
	}

	public Integer getKlMandatory() {
		return klMandatory;
	}

	public void setKlMandatory(Integer klMandatory) {
		this.klMandatory = klMandatory;
	}
}
