package com.idos.dao;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IDOSException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Branch;
import model.BranchBankAccounts;
import model.Transaction;
import model.TransactionPurpose;
import model.Users;
import com.typesafe.config.Config;
import service.FileUploadService;
import service.FileUploadServiceImpl;

import java.util.List;

public interface TransactionDAO extends BaseDAO {
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

	ObjectNode documentRulePVS(Users user, String txnInv, String txnpaymentReceived, EntityManager entityManager) throws IDOSException;

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

	void setInvoiceQuotProfSerial(Users user, EntityManager entityManager, Transaction txn) throws IDOSException;

	void setInvoiceQuotProfGstinSerial(Users user, EntityManager entityManager, Transaction txn) throws IDOSException;

	// String getolInvoiceStr();
	String getAndDeleteSupportingDocument(String exisitingDocs, String email, String newSupportingDoc, Users user,
			EntityManager em) throws IDOSException;

	List<Transaction> findByOrgCustVendPaymentStatusLinkedTxn(long orgid, long branchId, long custVendId,
			List<Long> txnPurposeList, List<String> paymentStatusList, String txnStatus, String linkedTxnRef,
			EntityManager em);

	String LINKED_TXN_JPQL = "from Transaction obj WHERE obj.transactionBranchOrganization.id = ?1 and obj.transactionBranch.id = ?2 and obj.transactionVendorCustomer.id = ?3 and obj.transactionPurpose.id in (?4) and obj.paymentStatus in (?5) and obj.transactionStatus = ?6 and obj.linkedTxnRef = ?7 and obj.presentStatus=1";

}
