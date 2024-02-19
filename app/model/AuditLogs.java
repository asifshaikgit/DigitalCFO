package model;

import com.idos.util.IdosUtil;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="AUDIT_LOGS")
public class AuditLogs extends AbstractBaseModel {
	
	@Column(name="entity_id")
	private Long entityId;

	@Column(name="entity_name")
	private String entityName;
	
	@Column(name="json_entity_Data")
	private String jsonEntityData;
	
	@Column(name="IP_ADDRESS")
	private String ipAddress;
	
	@Column(name="action")
	private String action;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="organization_id")
	private Organization organization;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_id")
	private Branch branch;

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

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getJsonEntityData() {
		return jsonEntityData;
	}

	public void setJsonEntityData(String jsonEntityData) {
		this.jsonEntityData = IdosUtil.escapeHtml(jsonEntityData);
	}
	
	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

}
