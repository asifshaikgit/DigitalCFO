package service.gstdatafiling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.logging.Level;
import model.ClaimTransaction;
import model.ConfigParams;
import model.Transaction;
import model.TransactionItems;
import model.Users;
import model.karvy.GSTFiling;
import com.typesafe.config.ConfigFactory;

import com.fasterxml.jackson.databind.JsonNode;
import javax.inject.Inject;
import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.KARVY.AESShaEncryptionKARVY;

public class GstDataFilingServiceImpl implements GstDataFilingService {

	@Inject
	public GstDataFilingServiceImpl() {
	}

	@Override
	public boolean saveGSTFilingData(Users users, EntityManager entityManager, Transaction transaction)
			throws IDOSException {
		try {
			GSTFiling gstFiling = new GSTFiling();

			gstFiling.setTransactionId(transaction);
			gstFiling.setBranchId(transaction.getTransactionBranch());
			gstFiling.setOrganizationId(transaction.getTransactionBranchOrganization());
			gstFiling.setTransactionDate(transaction.getTransactionDate());
			gstFiling.setTransactionPurpose(transaction.getTransactionPurpose());
			gstFiling.setGstFilingStatus(0);
			final String companyowner = ConfigFactory.load().getString("company.owner");
			if (companyowner.equals("KARVY")) {
				gstFiling.setAgentName(ConfigParams.getInstance().getCompanyName());
			}
			// gstFiling.setAgentName("Karvy");
			else if (companyowner.equals("PWC")) {
				gstFiling.setAgentName(ConfigParams.getInstance().getCompanyName());
			} else {
				gstFiling.setAgentName(ConfigParams.getInstance().getCompanyName());
			}
			genericDAO.saveOrUpdate(gstFiling, users, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, users.getEmail(), ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on storing Customer", ex.getMessage());
		}
		log.log(Level.FINE, "******** End");
		return true;
	}

	@Override
	public boolean saveGSTFilingDataForClaimTransaction(Users user, EntityManager entityManager,
			ClaimTransaction claimTransaction) throws IDOSException {
		try {
			GSTFiling gstFiling = new GSTFiling();
			gstFiling.setClaimTransactionId(claimTransaction);
			gstFiling.setBranchId(claimTransaction.getTransactionBranch());
			gstFiling.setOrganizationId(claimTransaction.getTransactionBranchOrganization());
			gstFiling.setTransactionDate(claimTransaction.getTransactionDate());
			gstFiling.setTransactionPurpose(claimTransaction.getTransactionPurpose());
			gstFiling.setGstFilingStatus(0);
			final String companyowner = ConfigFactory.load().getString("company.owner");
			if (companyowner.equals("KARVY")) {
				gstFiling.setAgentName(ConfigParams.getInstance().getCompanyName());
			}
			// gstFiling.setAgentName("Karvy");
			else if (companyowner.equals("PWC")) {
				gstFiling.setAgentName(ConfigParams.getInstance().getCompanyName());
			} else {
				gstFiling.setAgentName(ConfigParams.getInstance().getCompanyName());
			}
			genericDAO.saveOrUpdate(gstFiling, user, entityManager);
		} catch (Exception ex) {
			log.log(Level.SEVERE, user.getEmail(), ex);
			throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
					"Error on storing Customer", ex.getMessage());
		}
		log.log(Level.FINE, "******** End");
		return true;
	}

}
