package service;

import com.idos.util.IDOSException;
import model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil K. Namdev on 05-07-2017.
 */
public class GstTaxServiceImpl implements GstTaxService {
    @Override
    public void saveUpdateBranchTax(String branchId, JsonNode json, Integer taxCategory, EntityManager entityManager,
            Users user) throws IDOSException {
        taxDao.saveUpdateBranchTax(branchId, json, taxCategory, entityManager, user);
    }

    @Override
    public void saveInputTaxBranch(JsonNode json, EntityManager entityManager, Users user) throws IDOSException {
        taxDao.saveInputTaxBranch(json, entityManager, user);
    }

    @Override
    public void saveRcmTaxBranch(JsonNode json, EntityManager entityManager, Users user) throws IDOSException {
        taxDao.saveRcmTaxBranch(json, entityManager, user);
    }

    @Override
    public void applyTaxRulesToEachBranchSpecifics(String specificsId, String branchId, JsonNode json,
            EntityManager entityManager, Users user) throws IDOSException {
        taxDao.applyTaxRulesToEachBranchSpecifics(specificsId, branchId, json, entityManager, user);
    }

    @Override
    public void applyTaxRulesToMultipleBranchSpecifics(String specificsId, String branchId, JsonNode json,
            EntityManager entityManager, Users user) throws IDOSException {
        taxDao.applyTaxRulesToMultipleBranchSpecifics(specificsId, branchId, json, entityManager, user);
    }

    @Override
    public ObjectNode getGstInTaxesCess4Branch(Long branchId, EntityManager entityManager, Users user)
            throws IDOSException {
        return taxDao.getGstInTaxesCess4Branch(branchId, entityManager, user);
    }

    @Override
    public ObjectNode getRcmTaxesForSpecific(JsonNode json, EntityManager entityManager, Users user)
            throws IDOSException {
        return taxDao.getRcmTaxesForSpecific(json, entityManager, user);
    }

    @Override
    public void saveTaxableItemsForCompositionScheme(JsonNode json, EntityManager entityManager, Users user) {
        taxDao.saveTaxableItemsForCompositionScheme(json, entityManager, user);
    }

    @Override
    public ObjectNode getRcmTaxesForSpecificTypeOfSupply(JsonNode json, EntityManager entityManager, Users user)
            throws IDOSException {
        return taxDao.getRcmTaxesForSpecificTypeOfSupply(json, entityManager, user);
    }

    @Override
    public void saveInputTaxCOA(Specifics specific, Branch branch, EntityManager entityManager, Users user)
            throws IDOSException {
        taxDao.saveInputTaxCOA(specific, branch, entityManager, user);
    }

}
