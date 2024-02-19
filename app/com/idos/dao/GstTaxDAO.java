package com.idos.dao;

import com.idos.util.IDOSException;

import model.Branch;
import model.Specifics;
import model.Users;
import model.Vendor;
import model.VendorSpecific;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Date;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil K. Namdev on 05-07-2017.
 */
public interface GstTaxDAO extends BaseDAO {
        final String BRANCH_TAX_HQL = "select obj from BranchTaxes obj WHERE obj.organization.id=?1 and obj.branch.id = ?2 and obj.presentStatus = 1 and obj.taxType IN (12,13) order by obj.taxRate";
        final String BRANCH_RCM_HQL = "select obj from BranchSpecificsTaxFormula obj WHERE obj.branch.id = ?1 and obj.organization.id=?2 and obj.presentStatus=1 and obj.taxType IN (32,33) order by obj.taxRate";
        // final String SPECIFIC_BRANCH_RCM_HQL = "select obj from
        // BranchSpecificsTaxFormula obj WHERE obj.organization.id=?1 and obj.branch.id
        // = ?2 and obj.branchTaxes.taxType IN (32) and obj.specifics.id = ?3 and
        // obj.gstItemCategory = ?4 and obj.vendor.id = ?5 order by
        // obj.branchTaxes.taxRate";
        // final String SPECIFIC_BRANCH_RCM_ALL_HQL = "select obj from
        // BranchSpecificsTaxFormula obj WHERE obj.organization.id=?1 and obj.branch.id
        // = ?2 and obj.branchTaxes.taxType IN (32) and obj.specifics.id = ?3 and
        // obj.vendor.id = ?4 order by obj.branchTaxes.taxRate";
        final String SPECIFIC_BRANCH_RCM_HQL = "select obj from BranchSpecificsTaxFormula obj WHERE obj.organization.id= ?1 and obj.branch.id = ?2 and obj.specifics.id = ?3 and obj.specifics.id in (select obj1.specificsVendors.id from VendorSpecific obj1 where obj1.organization.id = ?4  and obj1.specificsVendors.id = ?5 and obj1.presentStatus = 1 and obj1.vendorSpecific.id = ?6) and obj.branchTaxes.taxType IN (?7) and obj.specifics.gstTypeOfSupply = ?8 and obj.applicableFrom <= ?9 and obj.presentStatus=1 ORDER by obj.applicableFrom DESC";
        final String CESS_RATE_HQL = "select obj from BranchSpecificsTaxFormula obj WHERE obj.organization.id = ?1 and obj.branch.id = ?2 and obj.specifics.id =?3 and obj.branchTaxes.taxType = ?4 and obj.branchTaxes.taxRate = ?5 and obj.vendor.id = ?6 and obj.presentStatus = 1";
        final String SPECIFIC_BRANCH_RCM_ALL_HQL = "select obj from BranchSpecificsTaxFormula obj WHERE obj.organization.id= ?1 and obj.branch.id = ?2 and obj.specifics.id = ?3 and obj.specifics.id in (select obj1.specificsVendors.id from VendorSpecific obj1 where obj1.organization.id = ?4 and obj1.specificsVendors.id = ?5 and obj1.presentStatus = 1 and obj1.vendorSpecific.id = ?6) and obj.branchTaxes.taxType IN (?7) and obj.applicableFrom <= ?8 and obj.presentStatus=1 ORDER by obj.applicableFrom DESC";

        void saveUpdateBranchTax(String branchId, JsonNode json, Integer taxCategory, EntityManager entityManager,
                        Users user) throws IDOSException;

        void saveInputTaxBranch(JsonNode json, EntityManager entityManager, Users user) throws IDOSException;

        void saveRcmTaxBranch(JsonNode json, EntityManager entityManager, Users user) throws IDOSException;

        void applyTaxRulesToEachBranchSpecifics(String specificsId, String branchId, JsonNode json,
                        EntityManager entityManager, Users user) throws IDOSException;

        void applyTaxRulesToMultipleBranchSpecifics(String specificsId, String branchId, JsonNode json,
                        EntityManager entityManager, Users user) throws IDOSException;

        ObjectNode getGstInTaxesCess4Branch(Long branchId, EntityManager entityManager, Users user);

        ObjectNode getRcmTaxesForSpecific(JsonNode json, EntityManager entityManager, Users user) throws IDOSException;

        void saveTaxableItemsForCompositionScheme(JsonNode json, EntityManager entityManager, Users user);

        ObjectNode getRcmTaxesForSpecificTypeOfSupply(JsonNode json, EntityManager entityManager, Users user)
                        throws IDOSException;

        void saveInputTaxCOA(Specifics specific, Branch branch, EntityManager entityManager, Users user)
                        throws IDOSException;

        public void saveRcmTaxBranchVendor(Branch branch, Vendor vendor, Specifics specific, Double gstTaxRate,
                        Double cessRate, Date applicableDate, EntityManager entityManager, Users user)
                        throws IDOSException;
}
