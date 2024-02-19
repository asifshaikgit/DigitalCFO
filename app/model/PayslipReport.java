package model;

public class PayslipReport {
	private long userId;
    private String earningHeadName1=null;
    private String earningVal1=null;
    private String earningHeadName2=null;
    private String earningVal2=null;
    private String earningHeadName3=null;
    private String earningVal3=null;
    private String earningHeadName4=null;
    private String earningVal4=null;
    private String earningHeadName5=null;
    private String earningVal5=null;
    private String earningHeadName6=null;
    private String earningVal6=null;
    private String earningHeadName7=null;
    private String earningVal7=null;
    
    private String deductionHeadName1=null;
    private String deductionVal1=null;
    private String deductionHeadName2=null;
    private String deductionVal2=null;
    private String deductionHeadName3=null;
    private String deductionVal3=null;
    private String deductionHeadName4=null;
    private String deductionVal4=null;
    private String deductionHeadName5=null;
    private String deductionVal5=null;
    private String deductionHeadName6=null;
    private String deductionVal6=null;
    private String deductionHeadName7=null;
    private String deductionVal7=null;
    
    private String totalEarningHeadName;
    private String totalEarning;
    private String totalDeductionHeadName;
    private String totalDeduction;
    private String netPayHeadName;
    private String netPay;
    
    private String payDays;
    private String eligibleDays;
    
    private String netPayInWords;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getEarningHeadName1() {
		return earningHeadName1;
	}

	public void setEarningHeadName1(String earningHeadName1) {
		this.earningHeadName1 = earningHeadName1;
	}

	public String getEarningVal1() {
		return earningVal1;
	}

	public void setEarningVal1(String earningVal1) {
		this.earningVal1 = earningVal1;
	}

	public String getEarningHeadName2() {
		return earningHeadName2;
	}

	public void setEarningHeadName2(String earningHeadName2) {
		this.earningHeadName2 = earningHeadName2;
	}

	public String getEarningVal2() {
		return earningVal2;
	}

	public void setEarningVal2(String earningVal2) {
		this.earningVal2 = earningVal2;
	}

	public String getEarningHeadName3() {
		return earningHeadName3;
	}

	public void setEarningHeadName3(String earningHeadName3) {
		this.earningHeadName3 = earningHeadName3;
	}

	public String getEarningVal3() {
		return earningVal3;
	}

	public void setEarningVal3(String earningVal3) {
		this.earningVal3 = earningVal3;
	}

	public String getEarningHeadName4() {
		return earningHeadName4;
	}

	public void setEarningHeadName4(String earningHeadName4) {
		this.earningHeadName4 = earningHeadName4;
	}

	public String getEarningVal4() {
		return earningVal4;
	}

	public void setEarningVal4(String earningVal4) {
		this.earningVal4 = earningVal4;
	}

	public String getEarningHeadName5() {
		return earningHeadName5;
	}

	public void setEarningHeadName5(String earningHeadName5) {
		this.earningHeadName5 = earningHeadName5;
	}

	public String getEarningVal5() {
		return earningVal5;
	}

	public void setEarningVal5(String earningVal5) {
		this.earningVal5 = earningVal5;
	}

	public String getEarningHeadName6() {
		return earningHeadName6;
	}

	public void setEarningHeadName6(String earningHeadName6) {
		this.earningHeadName6 = earningHeadName6;
	}

	public String getEarningVal6() {
		return earningVal6;
	}

	public void setEarningVal6(String earningVal6) {
		this.earningVal6 = earningVal6;
	}
	
	public String getTotalEarningHeadName() {
		return totalEarningHeadName;
	}
	
	public void setTotalEarningHeadName(String totalEarningHeadName) {
		this.totalEarningHeadName = totalEarningHeadName;
	}
	public String getDeductionHeadName1() {
		return deductionHeadName1;
	}

	public void setDeductionHeadName1(String deductionHeadName1) {
		this.deductionHeadName1 = deductionHeadName1;
	}

	public String getDeductionVal1() {
		return deductionVal1;
	}

	public void setDeductionVal1(String deductionVal1) {
		this.deductionVal1 = deductionVal1;
	}

	public String getDeductionHeadName2() {
		return deductionHeadName2;
	}

	public void setDeductionHeadName2(String deductionHeadName2) {
		this.deductionHeadName2 = deductionHeadName2;
	}

	public String getDeductionVal2() {
		return deductionVal2;
	}

	public void setDeductionVal2(String deductionVal2) {
		this.deductionVal2 = deductionVal2;
	}

	public String getDeductionHeadName3() {
		return deductionHeadName3;
	}

	public void setDeductionHeadName3(String deductionHeadName3) {
		this.deductionHeadName3 = deductionHeadName3;
	}

	public String getDeductionVal3() {
		return deductionVal3;
	}

	public void setDeductionVal3(String deductionVal3) {
		this.deductionVal3 = deductionVal3;
	}

	public String getDeductionHeadName4() {
		return deductionHeadName4;
	}

	public void setDeductionHeadName4(String deductionHeadName4) {
		this.deductionHeadName4 = deductionHeadName4;
	}

	public String getDeductionVal4() {
		return deductionVal4;
	}

	public void setDeductionVal4(String deductionVal4) {
		this.deductionVal4 = deductionVal4;
	}

	public String getDeductionHeadName5() {
		return deductionHeadName5;
	}

	public void setDeductionHeadName5(String deductionHeadName5) {
		this.deductionHeadName5 = deductionHeadName5;
	}

	public String getDeductionVal5() {
		return deductionVal5;
	}

	public void setDeductionVal5(String deductionVal5) {
		this.deductionVal5 = deductionVal5;
	}

	public String getDeductionHeadName6() {
		return deductionHeadName6;
	}

	public void setDeductionHeadName6(String deductionHeadName6) {
		this.deductionHeadName6 = deductionHeadName6;
	}

	public String getDeductionVal6() {
		return deductionVal6;
	}

	public void setDeductionVal6(String deductionVal6) {
		this.deductionVal6 = deductionVal6;
	}
	
	public String getTotalDeductionHeadName() {
		return totalDeductionHeadName;
	}
	
	public void setTotalDeductionHeadName(String totalDeductionHeadName) {
		this.totalDeductionHeadName = totalDeductionHeadName;
	}
	
	public String getNetPayHeadName() {
		return netPayHeadName;
	}
	
	public void setNetPayHeadName(String totalDeductionHeadName) {
		this.netPayHeadName = totalDeductionHeadName;
	}
	public String getTotalEarning() {
		return totalEarning;
	}

	public void setTotalEarning(String totalEarning) {
		this.totalEarning = totalEarning;
	}

	public String getTotalDeduction() {
		return totalDeduction;
	}

	public void setTotalDeduction(String totalDeduction) {
		this.totalDeduction = totalDeduction;
	}

	public String getNetPay() {
		return netPay;
	}

	public void setNetPay(String netPay) {
		this.netPay = netPay;
	}

	public String getNetPayInWords() {
		return netPayInWords;
	}
	
	public void setNetPayInWords(String netPayInWords) {
		this.netPayInWords = netPayInWords;
	}
	
	public String getPayDays() {
		return payDays;
	}

	public void setPayDays(String payDays) {
		this.payDays = payDays;}
	
	public String getEligibleDays() {
		return eligibleDays;
	}

	public void setEligibleDays(String eligibleDays) {
		this.eligibleDays = eligibleDays;}

	public String getEarningHeadName7() {
		return earningHeadName7;
	}

	public void setEarningHeadName7(String earningHeadName7) {
		this.earningHeadName7 = earningHeadName7;
	}

	public String getEarningVal7() {
		return earningVal7;
	}

	public void setEarningVal7(String earningVal7) {
		this.earningVal7 = earningVal7;
	}

	public String getDeductionHeadName7() {
		return deductionHeadName7;
	}

	public void setDeductionHeadName7(String deductionHeadName7) {
		this.deductionHeadName7 = deductionHeadName7;
	}

	public String getDeductionVal7() {
		return deductionVal7;
	}

	public void setDeductionVal7(String deductionVal7) {
		this.deductionVal7 = deductionVal7;
	}

}
