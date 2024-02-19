package model;

public class createPurchaseOrderModel {
	private Integer sNo;
	private String itemName;
	private String itemDescription;
	private String uOM;
	private Double pricePerUnit;
	private Double noOfUnits;
	private Double grossAmt;

	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getItemDescription() {
		return itemDescription;
	}
	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}
	public String getuOM() {
		return uOM;
	}
	public void setuOM(String uOM) {
		this.uOM = uOM;
	}
	public Double getPricePerUnit() {
		return pricePerUnit;
	}
	public void setPricePerUnit(Double pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}
	public Double getNoOfUnits() {
		return noOfUnits;
	}
	public void setNoOfUnits(Double noOfUnits) {
		this.noOfUnits = noOfUnits;
	}
	public Double getGrossAmt() {
		return grossAmt;
	}
	public void setGrossAmt(Double grossAmt) {
		this.grossAmt = grossAmt;
	}
	public Integer getsNo() {
		return sNo;
	}
	public void setsNo(Integer sNo) {
		this.sNo = sNo;
	}

}
