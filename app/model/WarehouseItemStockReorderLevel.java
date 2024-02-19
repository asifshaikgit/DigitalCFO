package model;

import javax.persistence.*;

@Entity
@Table(name="WAREHOUSE_STOCK_REORDER_LEVEL_ALERT")
public class WarehouseItemStockReorderLevel extends AbstractBaseModel {
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_id")
	private Branch branch;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="branch_organization_id")
	private Organization organization;

	@ManyToOne(cascade = CascadeType.PERSIST, fetch=FetchType.LAZY)
	@JoinColumn(name="specifics_id")
	private Specifics specifics;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="specifics_particulars_id")
	private Particulars particular;
	
	@Column(name="REORDER_LEVEL")
	private Integer reorderLevel=0;
	
	@Column(name="REORDER_LEVEL_ALERT_USER")
	private String reorderLevelAlertUser;

	public Branch getBranch() {
		return branch;
	}

	public void setBranch(Branch branch) {
		this.branch = branch;
	}

	public Organization getOrganization() {
		return organization;
	}

	public void setOrganization(Organization organization) {
		this.organization = organization;
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

	public Integer getReorderLevel() {
		return reorderLevel;
	}

	public void setReorderLevel(Integer reorderLevel) {
		this.reorderLevel = reorderLevel;
	}

	public String getReorderLevelAlertUser() {
		return reorderLevelAlertUser;
	}

	public void setReorderLevelAlertUser(String reorderLevelAlertUser) {
		this.reorderLevelAlertUser = reorderLevelAlertUser;
	}
	
	public Long getEntityComparableParamId(){
		return getBranch().getId();
	}
}
