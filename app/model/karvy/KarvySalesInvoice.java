package model.karvy;


public class KarvySalesInvoice {  
    String processgstin="";
    String branchcode="";
    String customergstin="";
	String customername="";    
    String invoiceno="";
    String invoicedate="";
    String invtype="R";
    Double invoicevalue=0.0;
    String statecode="";
    String pos=""; //billing address state code
    String attractreversecharge="";
    String reversecharge="";
    String ecommercegstin="";
    String advancedocumentno="";
    String advancedocumentdate="";
    Double advancedocumentamount=0.0;
    Double advancedocumenttax=0.0;
    Double Advancedocumentcess=0.0;
    String isexempted="Taxable"; 
    String hsnsac="";
    String description="";
    String productid="";
    String uqc="Pcs"; //unit of quantity
    Double quantity=0.0;
    Double taxablevalue=0.0;
    Double igstrate=0.0;  
    Double igstamount=0.0;
    Double cgstrate=0.0;
    Double cgstamount=0.0;
    Double sgstrate=0.0;
    Double sgstamount=0.0;
    Double cessamount=0.0;
    Double utgstrate=0.0;
    Double utgstamount=0.0;
    String Cancelled="No";
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
	public String getCustomergstin() {
		return customergstin;
	}
	public void setCustomergstin(String customergstin) {
		this.customergstin = customergstin;
	}
	public String getCustomername() {
		return customername;
	}
	public void setCustomername(String customername) {
		this.customername = customername;
	}
	public String getInvoiceno() {
		return invoiceno;
	}
	public void setInvoiceno(String invoiceno) {
		this.invoiceno = invoiceno;
	}
	public String getInvoicedate() {
		return invoicedate;
	}
	public void setInvoicedate(String invoicedate) {
		this.invoicedate = invoicedate;
	}
	public String getInvtype() {
		return invtype;
	}
	public void setInvtype(String invtype) {
		this.invtype = invtype;
	}
	public Double getInvoicevalue() {
		return invoicevalue;
	}
	public void setInvoicevalue(Double invoicevalue) {
		this.invoicevalue = invoicevalue;
	}
	public String getStatecode() {
		return statecode;
	}
	public void setStatecode(String statecode) {
		this.statecode = statecode;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public String getAttractreversecharge() {
		return attractreversecharge;
	}
	public void setAttractreversecharge(String attractreversecharge) {
		this.attractreversecharge = attractreversecharge;
	}
	public String getReversecharge() {
		return reversecharge;
	}
	public void setReversecharge(String reversecharge) {
		this.reversecharge = reversecharge;
	}
	public String getEcommercegstin() {
		return ecommercegstin;
	}
	public void setEcommercegstin(String ecommercegstin) {
		this.ecommercegstin = ecommercegstin;
	}
	public String getAdvancedocumentno() {
		return advancedocumentno;
	}
	public void setAdvancedocumentno(String advancedocumentno) {
		this.advancedocumentno = advancedocumentno;
	}
	public String getAdvancedocumentdate() {
		return advancedocumentdate;
	}
	public void setAdvancedocumentdate(String advancedocumentdate) {
		this.advancedocumentdate = advancedocumentdate;
	}
	public Double getAdvancedocumentamount() {
		return advancedocumentamount;
	}
	public void setAdvancedocumentamount(Double advancedocumentamount) {
		this.advancedocumentamount = advancedocumentamount;
	}
	public Double getAdvancedocumenttax() {
		return advancedocumenttax;
	}
	public void setAdvancedocumenttax(Double advancedocumenttax) {
		this.advancedocumenttax = advancedocumenttax;
	}
	public Double getAdvancedocumentcess() {
		return Advancedocumentcess;
	}
	public void setAdvancedocumentcess(Double advancedocumentcess) {
		Advancedocumentcess = advancedocumentcess;
	}
	public String getIsexempted() {
		return isexempted;
	}
	public void setIsexempted(String isexempted) {
		this.isexempted = isexempted;
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
	public String getProductid() {
		return productid;
	}
	public void setProductid(String productid) {
		this.productid = productid;
	}
	public String getUqc() {
		return uqc;
	}
	public void setUqc(String uqc) {
		this.uqc = uqc;
	}
	public Double getQuantity() {
		return quantity;
	}
	public void setQuantity(Double quantity) {
		this.quantity = quantity;
	}
	public Double getTaxablevalue() {
		return taxablevalue;
	}
	public void setTaxablevalue(Double taxablevalue) {
		this.taxablevalue = taxablevalue;
	}
	public Double getIgstrate() {
		return igstrate;
	}
	public void setIgstrate(Double igstrate) {
		this.igstrate = igstrate;
	}
	public Double getIgstamount() {
		return igstamount;
	}
	public void setIgstamount(Double igstamount) {
		this.igstamount = igstamount;
	}
	public Double getCgstrate() {
		return cgstrate;
	}
	public void setCgstrate(Double cgstrate) {
		this.cgstrate = cgstrate;
	}
	public Double getCgstamount() {
		return cgstamount;
	}
	public void setCgstamount(Double cgstamount) {
		this.cgstamount = cgstamount;
	}
	public Double getSgstrate() {
		return sgstrate;
	}
	public void setSgstrate(Double sgstrate) {
		this.sgstrate = sgstrate;
	}
	public Double getSgstamount() {
		return sgstamount;
	}
	public void setSgstamount(Double sgstamount) {
		this.sgstamount = sgstamount;
	}
	public Double getCessamount() {
		return cessamount;
	}
	public void setCessamount(Double cessamount) {
		this.cessamount = cessamount;
	}
	public Double getUtgstrate() {
		return utgstrate;
	}
	public void setUtgstrate(Double utgstrate) {
		this.utgstrate = utgstrate;
	}
	public Double getUtgstamount() {
		return utgstamount;
	}
	public void setUtgstamount(Double utgstamount) {
		this.utgstamount = utgstamount;
	}
	public String getCancelled() {
		return Cancelled;
	}
	public void setCancelled(String cancelled) {
		Cancelled = cancelled;
	}
    
    

}
