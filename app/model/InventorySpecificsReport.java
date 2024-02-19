package model;

public class InventorySpecificsReport extends AbstractBaseModel {	
	public Long itemId;
	public String itemName;
	public String unitOfMeasure;
	public String sellItems;
	public String openingBalQty="0.0";
	public String openingBalRate="1.0";
	public String openingBalValue="0.0";
	public String buyQty="0.0";
	public String buyRate="1.0";
	public String buyValue="0.0";
	public String sellQty="0.0";
	public String sellRate="1.0";
	public String sellValue="0.0";
	public String closingBalQty="0.0";
	public String closingBalRate="1.0";
	public String closingBalValue="0.0";
	public String calcMethod;
	public String salesAmount;
	public String salesMarginAmount;
	public String salesMarginPercent;
	
	public Long getItemId() {
		return itemId;
	}
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}
	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = unitOfMeasure;
	}
	public String getSellItems() {
		return sellItems;
	}
	public void setSellItems(String sellItems) {
		this.sellItems = sellItems;
	}
	public String getOpeningBalQty() {
		return openingBalQty;
	}
	public void setOpeningBalQty(String openingBalQty) {
		this.openingBalQty = openingBalQty;
	}
	public String getOpeningBalRate() {
		return openingBalRate;
	}
	public void setOpeningBalRate(String openingBalRate) {
		this.openingBalRate = openingBalRate;
	}
	public String getOpeningBalValue() {
		return openingBalValue;
	}
	public void setOpeningBalValue(String openingBalValue) {
		this.openingBalValue = openingBalValue;
	}
	public String getBuyQty() {
		return buyQty;
	}
	public void setBuyQty(String buyQty) {
		this.buyQty = buyQty;
	}
	public String getBuyRate() {
		return buyRate;
	}
	public void setBuyRate(String buyRate) {
		this.buyRate = buyRate;
	}
	public String getBuyValue() {
		return buyValue;
	}
	public void setBuyValue(String buyValue) {
		this.buyValue = buyValue;
	}
	public String getSellQty() {
		return sellQty;
	}
	public void setSellQty(String sellQty) {
		this.sellQty = sellQty;
	}
	public String getSellRate() {
		return sellRate;
	}
	public void setSellRate(String sellRate) {
		this.sellRate = sellRate;
	}
	public String getSellValue() {
		return sellValue;
	}
	public void setSellValue(String sellValue) {
		this.sellValue = sellValue;
	}
	public String getClosingBalQty() {
		return closingBalQty;
	}
	public void setClosingBalQty(String closingBalQty) {
		this.closingBalQty = closingBalQty;
	}
	public String getClosingBalRate() {
		return closingBalRate;
	}
	public void setClosingBalRate(String closingBalRate) {
		this.closingBalRate = closingBalRate;
	}
	public String getClosingBalValue() {
		return closingBalValue;
	}
	public void setClosingBalValue(String closingBalValue) {
		this.closingBalValue = closingBalValue;
	}
	public String getCalcMethod() {
		return calcMethod;
	}
	public void setCalcMethod(String calcMethod) {
		this.calcMethod = calcMethod;
	}
	public String getSalesAmount() {
		return salesAmount;
	}
	public void setSalesAmount(String salesAmount) {
		this.salesAmount = salesAmount;
	}
	public String getSalesMarginAmount() {
		return salesMarginAmount;
	}
	public void setSalesMarginAmount(String salesMarginAmount) {
		this.salesMarginAmount = salesMarginAmount;
	}
	public String getSalesMarginPercent() {
		return salesMarginPercent;
	}
	public void setSalesMarginPercent(String salesMarginPercent) {
		this.salesMarginPercent = salesMarginPercent;
	}

	@Override
	public String toString() {
		return "InventorySpecificsReport{" +
				"itemId=" + itemId +
				", itemName='" + itemName + '\'' +
				", unitOfMeasure='" + unitOfMeasure + '\'' +
				", sellItems='" + sellItems + '\'' +
				", openingBalQty='" + openingBalQty + '\'' +
				", openingBalRate='" + openingBalRate + '\'' +
				", openingBalValue='" + openingBalValue + '\'' +
				", buyQty='" + buyQty + '\'' +
				", buyRate='" + buyRate + '\'' +
				", buyValue='" + buyValue + '\'' +
				", sellQty='" + sellQty + '\'' +
				", sellRate='" + sellRate + '\'' +
				", sellValue='" + sellValue + '\'' +
				", closingBalQty='" + closingBalQty + '\'' +
				", closingBalRate='" + closingBalRate + '\'' +
				", closingBalValue='" + closingBalValue + '\'' +
				", calcMethod='" + calcMethod + '\'' +
				", salesAmount='" + salesAmount + '\'' +
				", salesMarginAmount='" + salesMarginAmount + '\'' +
				", salesMarginPercent='" + salesMarginPercent + '\'' +
				'}';
	}
}
