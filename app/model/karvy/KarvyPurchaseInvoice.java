package model.karvy;

public class KarvyPurchaseInvoice {
	
	String processgstin="";
	String branchcode="";
	String supplierGSTIN="";
	String gstinInvoiceType= "R";
	String supplierName="";
	String invoiceNo="";
	String invoiceDate="";
	Double totalTaxableValue=0.0;
	Double totalInvoiceValue=0.0;
	Double totalTaxITC=0.0;
	String pos="";
	String reverseCharge="0";
	String supplyReverseCharge="";
	String AdvanceDocumentNo="";
	String AdvanceDocumentDate="";
	String hsnsac="";
	String description="";
	Double quantity=0.0;
	String uqc="Pcs";
	Double taxableValue=0.0;
    Double igstRate=0.0;  
    Double igstAmount=0.0;
    Double cgstRate=0.0;
    Double cgstAmount=0.0;
    Double sgstRate=0.0;
    Double sgstAmount=0.0;
    String compositedealer="No";
    Double utgstRate=0.0;
    Double utgstAmount=0.0;
    Double cessRate=0.0;
    Double cessamount=0.0;
    String eligibilityForITC="no";
    public String getCompositedealer() {
		return compositedealer;
	}
	public void setCompositedealer(String compositedealer) {
		this.compositedealer = compositedealer;
	}
	Double itcigstRate=0.0;
    Double itccgstRate=0.0;
    Double itcsgstRate=0.0;
    Double itcutgstRate=0.0;
    Double itcCessRate=0.0;
	public String getProcessgstin() {
		return processgstin;
	}
	public void setProcessgstin(String processgstin) {
		this.processgstin = processgstin;
	}
	public String getBranchcode() {
		return branchcode;
	}
	public void setBranchcode(String branchcode) {
		this.branchcode = branchcode;
	}
	public String getSupplierGSTIN() {
		return supplierGSTIN;
	}
	public void setSupplierGSTIN(String supplierGSTIN) {
		this.supplierGSTIN = supplierGSTIN;
	}
	public String getGstinInvoiceType() {
		return gstinInvoiceType;
	}
	public void setGstinInvoiceType(String gstinInvoiceType) {
		this.gstinInvoiceType = gstinInvoiceType;
	}
	public String getSupplierName() {
		return supplierName;
	}
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}
	public String getInvoiceNo() {
		return invoiceNo;
	}
	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}
	public String getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public Double getTotalTaxableValue() {
		return totalTaxableValue;
	}
	public void setTotalTaxableValue(Double totalTaxableValue) {
		this.totalTaxableValue = totalTaxableValue;
	}
	public Double getTotalInvoiceValue() {
		return totalInvoiceValue;
	}
	public void setTotalInvoiceValue(Double totalInvoiceValue) {
		this.totalInvoiceValue = totalInvoiceValue;
	}
	public Double getTotalTaxITC() {
		return totalTaxITC;
	}
	public void setTotalTaxITC(Double totalTaxITC) {
		this.totalTaxITC = totalTaxITC;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public String getReverseCharge() {
		return reverseCharge;
	}
	public void setReverseCharge(String reverseCharge) {
		this.reverseCharge = reverseCharge;
	}
	public String getSupplyReverseCharge() {
		return supplyReverseCharge;
	}
	public void setSupplyReverseCharge(String supplyReverseCharge) {
		this.supplyReverseCharge = supplyReverseCharge;
	}
	public String getAdvanceDocumentNo() {
		return AdvanceDocumentNo;
	}
	public void setAdvanceDocumentNo(String advanceDocumentNo) {
		AdvanceDocumentNo = advanceDocumentNo;
	}
	public String getAdvanceDocumentDate() {
		return AdvanceDocumentDate;
	}
	public void setAdvanceDocumentDate(String advanceDocumentDate) {
		AdvanceDocumentDate = advanceDocumentDate;
	}
	public String getHsnsac() {
		return hsnsac;
	}
	public void setHsnsac(String hsnsac) {
		this.hsnsac = hsnsac;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Double getQuantity() {
		return quantity;
	}
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	public String getUqc() {
		return uqc;
	}
	public void setUqc(String uqc) {
		this.uqc = uqc;
	}
	public Double getTaxableValue() {
		return taxableValue;
	}
	public void setTaxableValue(Double taxableValue) {
		this.taxableValue = taxableValue;
	}
	public Double getIgstRate() {
		return igstRate;
	}
	public void setIgstRate(Double igstRate) {
		this.igstRate = igstRate;
	}
	public Double getIgstAmount() {
		return igstAmount;
	}
	public void setIgstAmount(Double igstAmount) {
		this.igstAmount = igstAmount;
	}
	public Double getCgstRate() {
		return cgstRate;
	}
	public void setCgstRate(Double cgstRate) {
		this.cgstRate = cgstRate;
	}
	public Double getCgstAmount() {
		return cgstAmount;
	}
	public void setCgstAmount(Double cgstAmount) {
		this.cgstAmount = cgstAmount;
	}
	public Double getSgstRate() {
		return sgstRate;
	}
	public void setSgstRate(Double sgstRate) {
		this.sgstRate = sgstRate;
	}
	public Double getSgstAmount() {
		return sgstAmount;
	}
	public void setSgstAmount(Double sgstAmount) {
		this.sgstAmount = sgstAmount;
	}
	public Double getUtgstRate() {
		return utgstRate;
	}
	public void setUtgstRate(Double utgstRate) {
		this.utgstRate = utgstRate;
	}
	public Double getUtgstAmount() {
		return utgstAmount;
	}
	public void setUtgstAmount(Double utgstAmount) {
		this.utgstAmount = utgstAmount;
	}
	public Double getCessRate() {
		return cessRate;
	}
	public void setCessRate(Double cessRate) {
		this.cessRate = cessRate;
	}
	public Double getCessamount() {
		return cessamount;
	}
	public void setCessamount(Double cessamount) {
		this.cessamount = cessamount;
	}
	public String getEligibilityForITC() {
		return eligibilityForITC;
	}
	public void setEligibilityForITC(String eligibilityForITC) {
		this.eligibilityForITC = eligibilityForITC;
	}
	public Double getItcigstRate() {
		return itcigstRate;
	}
	public void setItcigstRate(Double itcigstRate) {
		this.itcigstRate = itcigstRate;
	}
	public Double getItccgstRate() {
		return itccgstRate;
	}
	public void setItccgstRate(Double itccgstRate) {
		this.itccgstRate = itccgstRate;
	}
	public Double getItcsgstRate() {
		return itcsgstRate;
	}
	public void setItcsgstRate(Double itcsgstRate) {
		this.itcsgstRate = itcsgstRate;
	}
	public Double getItcutgstRate() {
		return itcutgstRate;
	}
	public void setItcutgstRate(Double itcutgstRate) {
		this.itcutgstRate = itcutgstRate;
	}
	public Double getItcCessRate() {
		return itcCessRate;
	}
	public void setItcCessRate(Double itcCessRate) {
		this.itcCessRate = itcCessRate;
	}
    
    
	    


}
