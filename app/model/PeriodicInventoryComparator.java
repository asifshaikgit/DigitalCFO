package model;

import java.util.Comparator;
import java.util.Date;

public class PeriodicInventoryComparator implements Comparator<PeriodicInventoryComparator> {
	
	public String inventoryIncomeExpenseItemName;
	public Date createdDate;
	public Double units;
	public Double price;
	public Double amount;
	public String inventoryStockType;  // either incoming stock or outgoing stock

	@Override
	public int compare(PeriodicInventoryComparator o1,
			PeriodicInventoryComparator o2) {
		return o1.getCreatedDate().compareTo(o2.getCreatedDate());
	}

	public String getInventoryIncomeExpenseItemName() {
		return inventoryIncomeExpenseItemName;
	}

	public void setInventoryIncomeExpenseItemName(
			String inventoryIncomeExpenseItemName) {
		this.inventoryIncomeExpenseItemName = inventoryIncomeExpenseItemName;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Double getUnits() {
		return units;
	}

	public void setUnits(Double units) {
		this.units = units;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getInventoryStockType() {
		return inventoryStockType;
	}

	public void setInventoryStockType(String inventoryStockType) {
		this.inventoryStockType = inventoryStockType;
	}

}
