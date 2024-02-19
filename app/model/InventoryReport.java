package model;

import java.util.Comparator;
import java.util.Date;

public class InventoryReport implements Comparator<InventoryReport> {
	
	public String particulars;
	public Date createdDate;
	public Double openingInventoryUnit;
	public Double purchaseUnit;
	public Double sellUnit;
	public Double closingInventoryUnit;
	
	public String getParticulars() {
		return particulars;
	}

	public void setParticulars(String particulars) {
		this.particulars = particulars;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Double getOpeningInventoryUnit() {
		return openingInventoryUnit;
	}

	public void setOpeningInventoryUnit(Double openingInventoryUnit) {
		this.openingInventoryUnit = openingInventoryUnit;
	}

	public Double getPurchaseUnit() {
		return purchaseUnit;
	}

	public void setPurchaseUnit(Double purchaseUnit) {
		this.purchaseUnit = purchaseUnit;
	}

	public Double getSellUnit() {
		return sellUnit;
	}

	public void setSellUnit(Double sellUnit) {
		this.sellUnit = sellUnit;
	}

	public Double getClosingInventoryUnit() {
		return closingInventoryUnit;
	}

	public void setClosingInventoryUnit(Double closingInventoryUnit) {
		this.closingInventoryUnit = closingInventoryUnit;
	}

	@Override
	public int compare(InventoryReport o1, InventoryReport o2) {
		return o1.getCreatedDate().compareTo(o2.getCreatedDate());
	}
}
