package com.idos.dao;

import java.util.Date;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Users;
import model.payroll.PayrollTransaction;

public interface PayrollDAO extends BaseDAO {

	public int payrollApproverAction(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction, PayrollTransaction payrollTxn) throws Exception;

	public void getPayrollTxnList(Users user, String roles, ArrayNode recordsArrayNode, EntityManager entityManager);

	public void getTrialBalancePayrollEarnItems(EntityManager entityManager, Users user, Long headId, Date fromDate,
			Date toDate, Long branchid, ArrayNode itemTransData);

	public void getTrialBalancePayrollDeduItems(EntityManager entityManager, Users user, Long headId, Date fromDate,
			Date toDate, Long branchid, ArrayNode itemTransData);

	ObjectNode fetchPayrollTransactionDetails(EntityManager entityManager, Users user, Long transactionID,
			Double debitAmount, Double creditAmount, Long transPurposeID, Long headId);
}
