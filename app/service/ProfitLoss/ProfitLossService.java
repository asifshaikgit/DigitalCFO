package service.ProfitLoss;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.EntityTransaction;

import com.idos.util.IDOSException;
import model.ProfitNLossReport;
import model.profitloss.ProfitLossBean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Users;
import service.BaseService;

import java.util.List;

/**
 * @author Sunil Namdev
 */
public interface ProfitLossService extends BaseService {

	/**
	 * Method displayProfitLossReport
	 * 
	 * @param result
	 * @param json
	 * @param user
	 * @param entityManager
	 * @param entitytransaction
	 * @return
	 */
	public ProfitLossBean displayProfitLossReport(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) throws IDOSException;

	public ObjectNode saveUpdateInventoryData(JsonNode json, Users user, EntityManager entityManager,
			EntityTransaction entitytransaction);

	public ObjectNode displayInvetory(JsonNode json, Users user, EntityManager entityManager);

	public List<ProfitNLossReport> getProfitNLossData(ObjectNode result, JsonNode json, Users user,
			EntityManager entityManager) throws IDOSException;
}
