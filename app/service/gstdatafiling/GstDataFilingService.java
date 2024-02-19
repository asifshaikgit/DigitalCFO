package service.gstdatafiling;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import model.ClaimTransaction;
import model.Transaction;
import model.Users;

import com.fasterxml.jackson.databind.JsonNode;

import service.BaseService;

import com.idos.util.IDOSException;

public interface GstDataFilingService extends BaseService {
	boolean saveGSTFilingData(Users user, EntityManager em, Transaction transaction) throws IDOSException;

	boolean saveGSTFilingDataForClaimTransaction(Users user, EntityManager entityManager,
			ClaimTransaction claimTransaction) throws IDOSException;

}
