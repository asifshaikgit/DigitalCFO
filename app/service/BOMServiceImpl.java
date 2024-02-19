package service;

import com.idos.util.IDOSException;
import model.BOMItemModel;
import model.BOMModel;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

/**
 * @author Harish Kumar created on 25.04.2023
 */
public class BOMServiceImpl implements BOMService {

    @Override
    public Map<String, String> add(JsonNode json, Users user, EntityManager em) throws IDOSException {
        return BOM_DAO.add(json, user, em);
    }

    @Override
    public Map<String, String> update(JsonNode json, Users user, EntityManager em, long bomId) throws IDOSException {
        return BOM_DAO.update(json, user, em, bomId);
    }

    @Override
    public List<BOMModel> getByOrg(Users user, EntityManager em) throws IDOSException {
        return BOM_DAO.getByOrg(user, em);
    }

    
}
