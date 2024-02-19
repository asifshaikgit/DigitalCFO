package service;

import com.idos.util.IDOSException;
import model.BillOfMaterialItemModel;
import model.BillOfMaterialModel;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.List;
import java.util.Map;

/**
 * @author Sunil K. Namdev created on 29.01.2019
 */
public interface BillOfMaterialService extends BaseService {
    Map<String, String> add(JsonNode json, Users user, EntityManager em) throws IDOSException;

    Map<String, String> update(JsonNode json, Users user, EntityManager em, long bomId) throws IDOSException;

    List<BillOfMaterialModel> getByOrg(Users user, EntityManager em) throws IDOSException;

    List<BillOfMaterialItemModel> getByIncomeAndBranch(Users user, EntityManager em, long branchId, long incomeId)
            throws IDOSException;
}
