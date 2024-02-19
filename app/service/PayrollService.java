package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import model.Specifics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Users;
import model.payroll.PayrollTransaction;

public interface PayrollService extends BaseService {

	public int payrollApproverAction(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction, PayrollTransaction payrollTxn, Specifics specific) throws Exception;

	public void getPayrollTxnList(Users user, String roles, ArrayNode recordsArrayNode, EntityManager entityManager);
}