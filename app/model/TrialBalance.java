package model;

public class TrialBalance {
	private String accountName;
	private Double openingBalance;
	private Double debit;
	private Double credit;
	private Double closingBalance;
	private Long specId;
	private Long headid2;
	private String specfaccountCode;
	private String topLevelAccountCode;
	private String identificationForDataValid;
	private String  headType;
	private Double debitCredit;
	public String getHeadType() {
		return this.headType;
	}

	public void setHeadType(String headType) {
		this.headType = headType;
	}
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public Double getOpeningBalance() {
		return openingBalance;
	}
	public void setOpeningBalance(Double openingBalance) {
		this.openingBalance = openingBalance;
	}
	public Double getDebit() {
		return debit;
	}
	public void setDebit(Double debit) {
		this.debit = debit;
	}
	public Double getCredit() {
		return credit;
	}
	public void setCredit(Double credit) {
		this.credit = credit;
	}
	public Double getClosingBalance() {
		return closingBalance;
	}
	public void setClosingBalance(Double closingBalance) {
		this.closingBalance = closingBalance;
	}
	public Long getSpecId() {
		return specId;
	}
	public void setSpecId(Long specId) {
		this.specId = specId;
	}
	
	public String getSpecfaccountCode() {
		return specfaccountCode;
	}
	public void setSpecfaccountCode(String specfaccountCode) {
		this.specfaccountCode = specfaccountCode;
	}
	public String getTopLevelAccountCode() {
		return topLevelAccountCode;
	}
	public void setTopLevelAccountCode(String topLevelAccountCode) {
		this.topLevelAccountCode = topLevelAccountCode;
	}
	public String getIdentificationForDataValid() {
		return identificationForDataValid;
	}
	public void setIdentificationForDataValid(String identificationForDataValid) {
		this.identificationForDataValid = identificationForDataValid;
	}

	public Long getHeadid2() {
		return this.headid2;
	}

	public void setHeadid2(Long headid2) {
		this.headid2 = headid2;
	}
	
	public Double getDebitCredit() {
		return debitCredit;
	}

	public void setDebitCredit(Double debitCredit) {
		this.debitCredit = debitCredit;
	}
	
	@Override
	public String toString() {
		return "TrialBalance{" +
				"accountName='" + accountName + '\'' +
				", openingBalance=" + openingBalance +
				", debit=" + debit +
				", credit=" + credit +
				", closingBalance=" + closingBalance +
				", specId=" + specId +
				", specfaccountCode='" + specfaccountCode + '\'' +
				", topLevelAccountCode='" + topLevelAccountCode + '\'' +
				", identificationForDataValid='" + identificationForDataValid + '\'' +
				", headType='" + headType + '\'' +
				'}';
	}
}
