package service;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.dao.GenericDAO;
import com.idos.util.IDOSException;
import model.Branch;
import model.BranchBankAccounts;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Date;

public interface BranchBankService extends BaseService {
	public ObjectNode branchBank(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);

	public ObjectNode branchBankDetails(ObjectNode result, JsonNode json, Users user, EntityManager entityManager);

	public Double updateBranchBankDetail(EntityManager entityManager, Users user, BranchBankAccounts bankAccount,
			Double txnNetAmount, boolean isCredit) throws IDOSException;

	public boolean updateBranchBankDetailTransaction(EntityManager entityManager, Users user,
			BranchBankAccounts bankAccount, Double txnNetAmount, boolean isCredit, ObjectNode result, Date txnDate,
			Branch branch) throws IDOSException;

	public double getBranchBankBalanceOnDate(Users user, Date txnDate, long branchId, BranchBankAccounts bank,
			EntityManager em) throws IDOSException;

	public boolean getOrganizationBanks(ObjectNode result, Users user, EntityManager em);
}
