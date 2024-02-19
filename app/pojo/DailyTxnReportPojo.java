package pojo;

import model.TransactionPurpose;

/**
 * Created by Ankush A. Sapkal on 30-10-2019.
 */

public class DailyTxnReportPojo {
   
	Integer noOfTxnCreated = 0;
	Integer noOfTxnApproved = 0;
	Integer noOfTxnPendingForAdditionalApprover= 0;
	Integer noOfTxnAccounted = 0;
	Long transactionPurposeId;
	String transactionPurposeName;
	
	public Integer getNoOfTxnCreated() {
		return noOfTxnCreated;
	}
	public void setNoOfTxnCreated(Integer noOfTxnCreated) {
		this.noOfTxnCreated = noOfTxnCreated;
	}
	public Integer getNoOfTxnApproved() {
		return noOfTxnApproved;
	}
	public void setNoOfTxnApproved(Integer noOfTxnApproved) {
		this.noOfTxnApproved = noOfTxnApproved;
	}
	public Integer getNoOfTxnPendingForAdditionalApprover() {
		return noOfTxnPendingForAdditionalApprover;
	}
	public void setNoOfTxnPendingForAdditionalApprover(Integer noOfTxnPendingForAdditionalApprover) {
		this.noOfTxnPendingForAdditionalApprover = noOfTxnPendingForAdditionalApprover;
	}
	public Integer getNoOfTxnAccounted() {
		return noOfTxnAccounted;
	}
	public void setNoOfTxnAccounted(Integer noOfTxnAccounted) {
		this.noOfTxnAccounted = noOfTxnAccounted;
	}
	public Long getTransactionPurposeId() {
		return transactionPurposeId;
	}
	public void setTransactionPurposeId(Long transactionPurposeId) {
		this.transactionPurposeId = transactionPurposeId;
	}
	public String getTransactionPurposeName() {
		return transactionPurposeName;
	}
	public void setTransactionPurposeName(String transactionPurposeName) {
		this.transactionPurposeName = transactionPurposeName;
	}
	
	
	
	
}
