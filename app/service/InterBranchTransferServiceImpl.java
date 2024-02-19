package service;

import com.idos.util.IDOSException;

import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;
import java.util.List;

public class InterBranchTransferServiceImpl implements InterBranchTransferService {

	@Override
	public Transaction submitForApproval(Users user, JsonNode json, EntityManager em, EntityTransaction et,
			TransactionPurpose txnPurpose, ObjectNode result) throws IDOSException {
		return INTER_BRANCH_TRANSFER_DAO.submitForApproval(user, json, em, et, txnPurpose, result);
	}

	@Override
	public void getTrialBalanceInterBranchTotal(TrialBalance tb, Users user, String fromDate, String toDate,
			Long branchId, EntityManager em) {
		INTER_BRANCH_TRANSFER_DAO.getTrialBalanceInterBranchTotal(tb, user, fromDate, toDate, branchId, em);
	}

	@Override
	public void getTrialBalanceInterBranch(List<TrialBalance> trialBalanceList, Users user, String fromDate,
			String toDate, Long branchId, EntityManager em) {
		INTER_BRANCH_TRANSFER_DAO.getTrialBalanceInterBranch(trialBalanceList, user, fromDate, toDate, branchId, em);
	}

	@Override
	public void createInterBranchMapping(Users user, EntityManager em, Branch branch) {
		INTER_BRANCH_TRANSFER_DAO.createInterBranchMapping(user, em, branch);
	}

}