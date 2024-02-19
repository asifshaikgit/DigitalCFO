package model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="PROJECT_LABOUR_POSITION_QUALIFICATION")
public class ProjectLabourPositionQualification extends AbstractBaseModel{
	
	@Column(name="qualification_name")
	private String qualificationName;
	
	public String getQualificationName() {
		return qualificationName;
	}

	public void setQualificationName(String qualificationName) {
		this.qualificationName = qualificationName;
	}

	public String getQualificationDegree() {
		return qualificationDegree;
	}

	public void setQualificationDegree(String qualificationDegree) {
		this.qualificationDegree = qualificationDegree;
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

	@Column(name="qualification_degree")
	private String qualificationDegree;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="labour_positionid")
	private ProjectLabourPosition labourPosition ;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="organization_id")
	private Organization organization;
	
	public Long getEntityComparableParamId(){
		return getLabourPosition().getId();
	}	

}
