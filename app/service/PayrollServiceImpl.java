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

public class PayrollServiceImpl implements PayrollService {

	@Override
	public int payrollApproverAction(ObjectNode result, JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction, PayrollTransaction payrollTxn, Specifics specific) throws Exception {
		// return payrollDAO.payrollApproverAction(result, json, user, entityManager,
		// entitytransaction,payrollTxn, specific);
		return 0;
	}

	@Override
	public void getPayrollTxnList(Users user, String roles, ArrayNode recordsArrayNode, EntityManager entityManager) {
		payrollDAO.getPayrollTxnList(user, roles, recordsArrayNode, entityManager);
	}
}
