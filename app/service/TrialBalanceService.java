package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.dao.*;
import com.idos.util.IDOSException;
import model.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Date;
import java.util.List;

public interface TrialBalanceService extends BaseService {
	ObjectMapper jsonObjectMapper = new ObjectMapper();
	String TB_LDGR_COA_ORG_JPQL = "select obj from TrialBalanceCOAItems obj where obj.organization.id=?1 and obj.transactionSpecifics.id = ?2 and obj.presentStatus=1 and obj.date between ?3 and ?4 order by obj.date";
	String TB_LDGR_COA_BRNCH_JPQL = "select obj from TrialBalanceCOAItems obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.transactionSpecifics.id = ?3 and obj.presentStatus=1 and obj.date  between ?4 and ?5 order by obj.date";

	String TB_LDGR_VENDCUST_ORG_JPQL = "select obj from TrialBalanceCustomerVendor obj where obj.organization.id=?1 and obj.vendor.id = ?2 and obj.vendorType=?3 and obj.presentStatus=1 and obj.date between ?4 and ?5 order by obj.date";
	String TB_LDGR_VENDCUST_BRNCH_JPQL = "select obj from TrialBalanceCustomerVendor obj where obj.organization.id=?1 and obj.branch.id=?2 and obj.vendor.id = ?3 and obj.vendorType=?4 and obj.presentStatus=1 and obj.date between ?5 and ?6 order by obj.date";

	void insertTrialBalance(Transaction transaction, Users user, EntityManager entityManager) throws IDOSException;

	String downloadTrialBalance(ObjectNode result, JsonNode json, Users user, EntityManager entityManager, String path);

	List<TrialBalance> displayTrialBalance(ObjectNode result, JsonNode json, Users user, EntityManager entityManager)
			throws IDOSException;

	ObjectNode exportTrialBalancePDF(String fromDate, String toDate, Users user, long branchId);

	void addTrialBalanceForCash(Users user, EntityManager entityManager, GenericDAO genericDAO,
			ClaimTransaction claimTransaction, Double amount, boolean isCredit);

	void addTrialBalanceForBank(Users user, EntityManager entityManager, GenericDAO genericDAO,
			ClaimTransaction claimTransaction, Double amount, boolean isCredit);

	void addTrialBalanceCOAItems(Users user, EntityManager entityManager, GenericDAO genericDAO,
			ClaimTransaction claimTransaction, Double amount, Specifics specifics, boolean isCredit)
			throws IDOSException;

	ObjectNode getTransactionForHead(Users user, EntityManager entityManager, JsonNode json) throws IDOSException;

	void fetchTxnGSTDetails(EntityManager em, Transaction txn, Users user, ObjectNode row);

	// void getTrialBalanceRoundingOffForSellTranTotal(TrialBalance tb, Users user,
	// String fromDate, String toDate, Specifics specf, Long branchId, EntityManager
	// em, Boolean isJournalEntryIncluded);
	void getProvisionJournalEntryHeads(EntityManager entityManager, IdosProvisionJournalEntry provisionJournalEntry,
			String headType, Long headID, StringBuilder itemName, StringBuilder creditItems);

	void saveTrialBalInterBranch(Transaction transaction, Users user, Integer typeIdentifier, EntityManager em,
			boolean isCredit);

	Boolean saveTrialBalanceForRoundOff(Organization org, Branch branch, Long txnId, TransactionPurpose txnPurpose,
			Date txnDate, Double roundOffAmount, Users user, EntityManager em, boolean isCredit) throws IDOSException;
}
