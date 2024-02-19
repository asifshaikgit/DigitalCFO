package service;

import com.idos.dao.GstTaxDAO;
import com.idos.dao.GstTaxDAOImpl;
import com.idos.util.IDOSException;

import model.Branch;
import model.Specifics;
import model.Users;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil Namdev on 05-07-2017.
 */
public interface GstTaxService extends BaseService {
    void saveUpdateBranchTax(String branchId, JsonNode json, Integer taxCategory, EntityManager entityManager,
            Users user) throws IDOSException;

    void saveInputTaxBranch(JsonNode json, EntityManager entityManager, Users user) throws IDOSException;

    void saveRcmTaxBranch(JsonNode json, EntityManager entityManager, Users user) throws IDOSException;

    void applyTaxRulesToEachBranchSpecifics(String specificsId, String branchId, JsonNode json,
            EntityManager entityManager, Users user) throws IDOSException;

    void applyTaxRulesToMultipleBranchSpecifics(String specificsId, String branchId, JsonNode json,
            EntityManager entityManager, Users user) throws IDOSException;

    ObjectNode getGstInTaxesCess4Branch(Long branchId, EntityManager entityManager, Users user) throws IDOSException;

    ObjectNode getRcmTaxesForSpecific(JsonNode json, EntityManager entityManager, Users user) throws IDOSException;

    void saveTaxableItemsForCompositionScheme(JsonNode json, EntityManager entityManager, Users user);

    ObjectNode getRcmTaxesForSpecificTypeOfSupply(JsonNode json, EntityManager entityManager, Users user)
            throws IDOSException;

    void saveInputTaxCOA(Specifics specific, Branch branch, EntityManager entityManager, Users user)
            throws IDOSException;
}
