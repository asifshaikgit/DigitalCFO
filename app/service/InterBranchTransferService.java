package service;

import com.idos.dao.GstTaxDAO;
import com.idos.dao.GstTaxDAOImpl;
import com.idos.util.IDOSException;

import model.Branch;
import model.Transaction;
import model.TransactionPurpose;
import model.TrialBalance;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

/**
 * Created by Sunil Namdev on 05-07-2017.
 */
public interface InterBranchTransferService extends BaseService {
	Transaction submitForApproval(Users user, JsonNode json, EntityManager em, EntityTransaction et,
			TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException;

	void getTrialBalanceInterBranchTotal(TrialBalance tb, Users user, String fromDate, String toDate, Long branchId,
			EntityManager em);

	void getTrialBalanceInterBranch(List<TrialBalance> trialBalanceList, Users user, String fromDate, String toDate,
			Long branchId, EntityManager em);

	void createInterBranchMapping(Users user, EntityManager em, Branch branch);
}
