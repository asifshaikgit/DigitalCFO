package service.balancesheetservice;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IDOSException;
import model.BalanceSheetReport;
import model.ProfitNLossReport;
import model.balancesheet.BalanceSheetBean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Users;
import service.BaseService;

import java.util.List;

/**
 * @author myidos
 *
 */
public interface BalanceSheetService extends BaseService {

	/**
	 * Method displayBalanceSheet
	 * 
	 * @param result
	 * @param json
	 * @param user
	 * @param entityManager
	 * @param entitytransaction
	 * @return
	 */
	public ObjectNode displayBalanceSheet(ObjectNode result, JsonNode json, Users user, EntityManager entityManager)
			throws IDOSException;

	public List<BalanceSheetReport> getBalanceSheetData(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) throws IDOSException;
}
