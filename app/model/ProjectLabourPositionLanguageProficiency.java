package model;

import com.idos.util.IdosUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="PROJECT_LABOUR_LANGUAGE_PROFICIENCY")
public class ProjectLabourPositionLanguageProficiency extends AbstractBaseModel {
	
	@Column(name="LANGUAGE_PROFICIENCY")
	private String languaugeProficiency;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="LABOUR_POSITIONID")
	private ProjectLabourPosition labourPosition ;
	
	public String getLanguaugeProficiency() {
		return languaugeProficiency;
	}

	public void setLanguaugeProficiency(String languaugeProficiency) {
		this.languaugeProficiency =  IdosUtil.escapeHtml(languaugeProficiency);
	}

	public ProjectLabourPosition getLabourPosition() {
		return labourPosition;
	}

	public void setLabourPosition(ProjectLabourPosition labourPosition) {
		this.labourPosition = labourPosition;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="ORGANIZATION_ID")
	private Organization organization;
}
