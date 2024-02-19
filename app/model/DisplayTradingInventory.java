package model;

public class DisplayTradingInventory {	
	public Long itemId;
	public String itemName;
	public String unitOfMeasure;
	public String sellItems;
	public Double openingBalQty=0.0;
	public Double openingBalRate=1.0;
	public Double openingBalValue=0.0;
	public Double buyQty=0.0;
	public Double buyRate=1.0;
	public Double buyValue=0.0;
	public Double sellQty=0.0;
	public Double sellRate=1.0;
	public Double sellValue=0.0;
	public Double closingBalQty=0.0;
	public Double closingBalRate=1.0;
	public Double closingBalValue=0.0;
	public String calcMethod;
	public Double salesAmount;
	public Double salesMarginAmount;
	public Double salesMarginPercent;

	
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
	public Double getOpeningBalQty() {
		return openingBalQty;
	}
	public void setOpeningBalQty(Double openingBalQty) {
		this.openingBalQty = openingBalQty;
	}
	public Double getOpeningBalRate() {
		return openingBalRate;
	}
	public void setOpeningBalRate(Double openingBalRate) {
		this.openingBalRate = openingBalRate;
	}
	public Double getOpeningBalValue() {
		return openingBalValue;
	}
	public void setOpeningBalValue(Double openingBalValue) {
		this.openingBalValue = openingBalValue;
	}
	public Double getBuyQty() {
		return buyQty;
	}
	public void setBuyQty(Double buyQty) {
		this.buyQty = buyQty;
	}
	public Double getBuyRate() {
		return buyRate;
	}
	public void setBuyRate(Double buyRate) {
		this.buyRate = buyRate;
	}
	public Double getBuyValue() {
		return buyValue;
	}
	public void setBuyValue(Double buyValue) {
		this.buyValue = buyValue;
	}
	public Double getSellQty() {
		return sellQty;
	}
	public void setSellQty(Double sellQty) {
		this.sellQty = sellQty;
	}
	public Double getSellRate() {
		return sellRate;
	}
	public void setSellRate(Double sellRate) {
		this.sellRate = sellRate;
	}
	public Double getSellValue() {
		return sellValue;
	}
	public void setSellValue(Double sellValue) {
		this.sellValue = sellValue;
	}
	public Double getClosingBalQty() {
		return closingBalQty;
	}
	public void setClosingBalQty(Double closingBalQty) {
		this.closingBalQty = closingBalQty;
	}
	public Double getClosingBalRate() {
		return closingBalRate;
	}
	public void setClosingBalRate(Double closingBalRate) {
		this.closingBalRate = closingBalRate;
	}
	public Double getClosingBalValue() {
		return closingBalValue;
	}
	public void setClosingBalValue(Double closingBalValue) {
		this.closingBalValue = closingBalValue;
	}
	public String getCalcMethod() {
		return calcMethod;
	}
	public void setCalcMethod(String calcMethod) {
		this.calcMethod = calcMethod;
	}

	public Double getSalesAmount() {
		return this.salesAmount;
	}

	public void setSalesAmount(Double salesAmount) {
		this.salesAmount = salesAmount;
	}

	public Double getSalesMarginAmount() {
		return this.salesMarginAmount;
	}

	public void setSalesMarginAmount(Double salesMarginAmount) {
		this.salesMarginAmount = salesMarginAmount;
	}

	public Double getSalesMarginPercent() {
		return this.salesMarginPercent;
	}

	public void setSalesMarginPercent(Double salesMarginPercent) {
		this.salesMarginPercent = salesMarginPercent;
	}

	@Override
	public String toString() {
		return "DisplayTradingInventory{" +
				"itemId=" + itemId +
				", itemName='" + itemName + '\'' +
				", unitOfMeasure='" + unitOfMeasure + '\'' +
				", sellItems='" + sellItems + '\'' +
				", openingBalQty=" + openingBalQty +
				", openingBalRate=" + openingBalRate +
				", openingBalValue=" + openingBalValue +
				", buyQty=" + buyQty +
				", buyRate=" + buyRate +
				", buyValue=" + buyValue +
				", sellQty=" + sellQty +
				", sellRate=" + sellRate +
				", sellValue=" + sellValue +
				", closingBalQty=" + closingBalQty +
				", closingBalRate=" + closingBalRate +
				", closingBalValue=" + closingBalValue +
				", calcMethod='" + calcMethod + '\'' +
				", salesAmount=" + salesAmount +
				", salesMarginAmount=" + salesMarginAmount +
				", salesMarginPercent=" + salesMarginPercent +
				'}';
	}
}
