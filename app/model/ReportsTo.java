package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="REPORTS_TO")
public class ReportsTo extends AbstractBaseModel {
	
	@Column(name="REPORT_TO_NAME")
	private String reportsToName;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="organization_id")
	private Organization organization;

	public String getReportsToName() {
		return reportsToName;
	}

	public void setReportsToName(String reportsToName) {
		this.reportsToName = reportsToName;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
}
