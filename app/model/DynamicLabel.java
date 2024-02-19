package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="DYNAMIC_LABEL")
public class DynamicLabel extends AbstractBaseModel {
	
	@Column(name="label_dom_id")
	private String labelDomId;

	@Column(name="label_display_name")
	private String labelDisplayName;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="organization_id")
	private Organization organization;

	public String getLabelDomId() {
		return labelDomId;
	}

	public void setLabelDomId(String labelDomId) {
		this.labelDomId = labelDomId;
	}

	public String getLabelDisplayName() {
		return labelDisplayName;
	}

	public void setLabelDisplayName(String labelDisplayName) {
		this.labelDisplayName = labelDisplayName;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

}
