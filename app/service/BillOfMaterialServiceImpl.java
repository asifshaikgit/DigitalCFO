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
public class BillOfMaterialServiceImpl implements BillOfMaterialService {

    @Override
    public Map<String, String> add(JsonNode json, Users user, EntityManager em) throws IDOSException {
        return BILL_OF_MATERIAL_DAO.add(json, user, em);
    }

    @Override
    public Map<String, String> update(JsonNode json, Users user, EntityManager em, long bomId) throws IDOSException {
        return BILL_OF_MATERIAL_DAO.update(json, user, em, bomId);
    }

    @Override
    public List<BillOfMaterialModel> getByOrg(Users user, EntityManager em) throws IDOSException {
        return BILL_OF_MATERIAL_DAO.getByOrg(user, em);
    }

    @Override
    public List<BillOfMaterialItemModel> getByIncomeAndBranch(Users user, EntityManager em, long branchId,
            long incomeId) throws IDOSException {
        return BILL_OF_MATERIAL_DAO.getByIncomeAndBranch(user, em, branchId, incomeId);
    }
}
