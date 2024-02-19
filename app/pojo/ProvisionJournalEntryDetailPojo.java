package pojo;

public class ProvisionJournalEntryDetailPojo {
	
	public String itemName ;
	public Double units ;
	public Double unitPrice ;
	public Double headAmount ;
	public String projectName ;
	private Long headID ;
	private Long headID2 ;
	private String headType ;
	private Integer isDebit ;
	
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public Double getUnits() {
		return units;
	}
	public void setUnits(Double units) {
		this.units = units;
	}
	public Double getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(Double unitPrice) {
		this.unitPrice = unitPrice;
	}
	public Double getHeadAmount() {
		return headAmount;
	}
	public void setHeadAmount(Double headAmount) {
		this.headAmount = headAmount;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public Long getHeadID() {
		return headID;
	}
	public void setHeadID(Long headID) {
		this.headID = headID;
	}
	public Long getHeadID2() {
		return headID2;
	}
	public void setHeadID2(Long headID2) {
		this.headID2 = headID2;
	}
	public String getHeadType() {
		return headType;
	}
	public void setHeadType(String headType) {
		this.headType = headType;
	}
	public Integer getIsDebit() {
		return isDebit;
	}
	public void setIsDebit(Integer isDebit) {
		this.isDebit = isDebit;
	}
}
