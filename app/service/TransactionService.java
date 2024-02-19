package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.dao.*;
import com.idos.util.IDOSException;
import model.Branch;
import model.BranchBankAccounts;
import model.IdosProvisionJournalEntry;
import model.Transaction;
import model.TransactionPurpose;
import model.Users;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public interface TransactionService extends BaseService {

	Transaction receiveSpecialAdjustmentsFromVendors(String txnPurpose, TransactionPurpose usertxnPurpose, Users user,
			String txnRSAAFVCreditVendor, String txnRSAAFVAmountReceived, String txnRSAAFVForProject,
			String txnreceiptdetails, String txnreceiptPaymentBank, String txnreceipttypebankdetails,
			String supportingdoc, String txnremarks, String klfollowednotfollowed, EntityTransaction entitytransaction,
			EntityManager em) throws IDOSException;

	Transaction paySpecialAdjustmentsToVendors(String txnPurpose, TransactionPurpose usertxnPurpose, Users user,
			String txnPSAATVCreditVendor, String txnPSAATVAmountPaid, String txnPSAATVForProject, String supportingdoc,
			String txnremarks, String klfollowednotfollowed, EntityTransaction entitytransaction, EntityManager em)
			throws IDOSException;

	ObjectNode approverCashBankReceivablePayables(Users user, EntityManager entityManager);

	ObjectNode branchWiseApproverCashBankReceivablePayables(Users user, EntityManager entityManager, String tabElement);

	ObjectNode wightedAverageForTransaction(Users user, Transaction transaction, String period,
			EntityManager entityManager);

	ObjectNode documentRule(Users user, String txnForExpItem, String txnForExpBranch, String txnForExpNetAmount)
			throws IDOSException;

	ObjectNode documentRulePVS(Users user, String txnInv, String txnpaymentReceived, EntityManager entityManager)
			throws IDOSException;

	ObjectNode accountHeadTransactions(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction);

	Transaction bankServices(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction) throws IDOSException;

	void cashBankBalanceEffectWithdrawalFromBank(BranchBankAccounts branchBankAccount, Branch branch, Double amount,
			EntityManager entityManager, EntityTransaction entitytransaction, Users user);

	void cashBankBalanceEffectDepositToBank(BranchBankAccounts branchBankAccount, Branch branch, Double amount,
			EntityManager entityManager, EntityTransaction entitytransaction, Users user);

	void cashBankBalanceEffectTransferFromOneBankToAnotherBank(BranchBankAccounts fromBranchBankAccount,
			Branch fromBranch, BranchBankAccounts toBranchBankAccount, Branch toBranch, Double amount,
			EntityManager entityManager, EntityTransaction entitytransaction, Users user);

	ObjectNode checkMaxDiscountForWalkinCust(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);

	ObjectNode branchCustomerVendorReceivablePayables(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager);

	ObjectNode overUnderOneEightyReceivablePayablesTxn(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction);

	String downloadOverUnderOneEightyDayaTxnExcel(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager, EntityTransaction entitytransaction, String path);

	void saveTransactionBRSDate(Users user, EntityManager entityManager, EntityTransaction entitytransaction,
			String transactionRef, String brsBankDate);

	void sendStockWebSocketResponse(EntityManager entityManager, Transaction transaction, Users user,
			ObjectNode result);

	void sendBankWebSocketResponse(EntityManager entityManager, Transaction transaction, Users user, ObjectNode result);

	boolean getTaxOnAdvOrAdj(JsonNode json, EntityManager entityManager, Users user, ObjectNode result)
			throws IDOSException;

	Transaction submitForAccountingRecCust(Users user, JsonNode json, EntityManager entityManager,
			EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

	Transaction submitForApprovalPayAdvToVend(Users user, JsonNode json, EntityManager entityManager,
			EntityTransaction entitytransaction, ObjectNode result) throws IDOSException;

	boolean submitForAccountPayAdvToVendAdv(Transaction txn, EntityManager entityManager, Users user)
			throws IDOSException;

	Transaction submitForApprovalNote(Users user, JsonNode json, EntityManager em, EntityTransaction entitytransaction,
			ObjectNode result) throws IDOSException;

	Transaction submitForApprovalVendorNote(Users user, JsonNode json, EntityManager em, EntityTransaction et,
			TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException;

	Transaction submitForApprovalInterBranchTransfer(Users user, JsonNode json, EntityManager em, EntityTransaction et,
			TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException;

	Transaction submitForApprovalRefundAmountRecivedAgainstInvoice(Users user, JsonNode json,
			EntityManager entityManager, EntityTransaction entitytransaction, TransactionPurpose usertxnPurpose,
			ObjectNode result) throws IDOSException;

	Transaction submitForApprovalRefundAdvanceRecived(Users user, JsonNode json, EntityManager em, EntityTransaction et,
			TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException;

	boolean getInclusiveTaxCalculated(JsonNode json, EntityManager entityManager, Users user, ObjectNode result)
			throws IDOSException;

	Transaction submitForApprovalPayVendor(Users user, JsonNode json, EntityManager em,
			TransactionPurpose txnPurposeObj, ObjectNode result) throws IDOSException;

	Transaction submitForApprovalTransferCashToPetty(Users user, JsonNode json, EntityManager em,
			TransactionPurpose txnPurposeObj, ObjectNode result) throws IDOSException;

	Transaction findByReferenceNumber(String referenceNumber, EntityManager entityManager);

}
