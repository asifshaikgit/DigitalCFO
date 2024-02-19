package com.idos.dao;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sunil K. Namdev created on 20.07.2019
 */
public class BillOfMaterialTxnItemDAOImpl implements BillOfMaterialTxnItemDAO {

    @Override
    public void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException {
        ArrayList inparams = new ArrayList(2);
        inparams.add(user.getOrganization().getId());
        inparams.add(txnId);
        inparams.add(1);
        List<BillOfMaterialTxnItemModel> bomTxnItemsList = genericDao.queryWithParamsName(
                "from BillOfMaterialTxnItemModel obj where obj.organization.id = ?1 and obj.billOfMaterialTxn.id = ?2 and obj.presentStatus = ?3",
                em, inparams);
        for (BillOfMaterialTxnItemModel txnItem : bomTxnItemsList) {
            ObjectNode row = Json.newObject();
            row.put("id", txnItem.getId());
            if (txnItem.getExpense() != null) {
                row.put("itemName", txnItem.getExpense().getName());
                row.put("itemId", txnItem.getExpense().getId());
            } else {
                row.put("itemName", "");
                row.put("itemId", "");
            }
            row.put("pricePerUnit", IdosConstants.DECIMAL_FORMAT
                    .format(txnItem.getPricePerUnit() == null ? 0.0 : txnItem.getPricePerUnit()));
            row.put("noOfUnits", IdosConstants.DECIMAL_FORMAT2
                    .format((txnItem.getNoOfUnits() == null ? 0 : txnItem.getNoOfUnits())));
            if (txnItem.getVendor() != null)
                row.put("vendor", txnItem.getVendor().getName());
            else
                row.put("vendor", "");
            row.put("measureName", txnItem.getMeasureName() == null ? "" : txnItem.getMeasureName());
            row.put("oem", txnItem.getOem() == null ? "" : txnItem.getOem());
            row.put("totalPrice", IdosConstants.DECIMAL_FORMAT
                    .format(txnItem.getTotalPrice() == null ? 0.0 : txnItem.getTotalPrice()));
            row.put("availableUnits", IdosConstants.DECIMAL_FORMAT
                    .format(txnItem.getAvailableUnits() == null ? 0.0 : txnItem.getAvailableUnits()));
            row.put("committedUnits", IdosConstants.DECIMAL_FORMAT
                    .format(txnItem.getCommittedUnits() == null ? 0.0 : txnItem.getCommittedUnits()));
            row.put("orderedUnits", IdosConstants.DECIMAL_FORMAT
                    .format(txnItem.getOrderedUnits() == null ? 0.0 : txnItem.getOrderedUnits()));
            row.put("netUnits",
                    IdosConstants.DECIMAL_FORMAT.format(txnItem.getNetUnits() == null ? 0.0 : txnItem.getNetUnits()));
            row.put("fulfilledUnits", IdosConstants.decimalFormat
                    .format(txnItem.getFulfilledUnits() == null ? 0.0 : txnItem.getFulfilledUnits()));
            row.put("typeOfMaterial", txnItem.getTypeOfMaterial() == null ? "" : txnItem.getTypeOfMaterial());
            row.put("destinationGstin", txnItem.getDestinationGstin() == null ? "" : txnItem.getDestinationGstin());
            row.put("isFulfilled",
                    txnItem.getIsFulfilled() == null ? "" : (txnItem.getIsFulfilled() == 1 ? "Yes" : "No"));
            txnItemsAn.add(row);
        }
    }

    @Override
    public boolean updateFullfilDetail(Users user, EntityManager em, BillOfMaterialTxnModel bomTxn,
            double unitsToFullfil, Specifics specific, Branch branch, String mainTxnRefNo) throws IDOSException {
        String SQL = "from BillOfMaterialTxnItemModel obj where obj.organization.id= ?1 and obj.branch.id= ?2 and obj.billOfMaterialTxn.id = ?3 and obj.expense.id= ?4";
        ArrayList inparams = new ArrayList(4);
        inparams.add(user.getOrganization().getId());
        inparams.add(branch.getId());
        inparams.add(bomTxn.getId());
        inparams.add(specific.getId());
        List<BillOfMaterialTxnItemModel> bomItemsList = genericDao.queryWithParamsName(SQL, em, inparams);
        boolean isFullfilled = false;
        for (BillOfMaterialTxnItemModel bomItemTxn : bomItemsList) {
            if (bomItemTxn.getIsFulfilled() == null
                    || bomItemTxn.getIsFulfilled() != IdosConstants.FULFILLED_TRANACTION) {
                double fulfilledUnits = 0.0;
                double totalUnits = 0.0;
                if (bomItemTxn.getFulfilledUnits() != null) {
                    fulfilledUnits = bomItemTxn.getFulfilledUnits();
                }
                if (bomItemTxn.getNoOfUnits() != null) {
                    totalUnits = bomItemTxn.getNoOfUnits();
                }
                double remainingUnitsToFullFilled = totalUnits - fulfilledUnits;
                if (remainingUnitsToFullFilled > unitsToFullfil) {
                    bomItemTxn.setFulfilledUnits(fulfilledUnits + unitsToFullfil);
                    bomItemTxn.setIsFulfilled(IdosConstants.UN_FULFILLED_TRANACTION);
                } else if (remainingUnitsToFullFilled <= unitsToFullfil) {
                    bomItemTxn.setFulfilledUnits(remainingUnitsToFullFilled);
                    bomItemTxn.setIsFulfilled(IdosConstants.FULFILLED_TRANACTION);
                    isFullfilled = true;
                }
                if (bomItemTxn.getFulfilledTxnRefNo() != null) {
                    bomItemTxn.setFulfilledTxnRefNo(bomItemTxn.getFulfilledTxnRefNo() + "," + mainTxnRefNo);
                } else {
                    bomItemTxn.setFulfilledTxnRefNo(mainTxnRefNo);
                }
                genericDao.saveOrUpdate(bomItemTxn, user, em);
            }
        }
        if (isFullfilled
                && (bomTxn.getIsFulfilled() == null || bomTxn.getIsFulfilled() != IdosConstants.FULFILLED_TRANACTION)) {
            if (isAllItemsFullfilled(user, em, bomTxn)) {
                bomTxn.setIsFulfilled(IdosConstants.FULFILLED_TRANACTION);
                genericDao.saveOrUpdate(bomTxn, user, em);
            }
        }
        return true;
    }

    @Override
    public boolean isAllItemsFullfilled(Users user, EntityManager em, BillOfMaterialTxnModel bomTxn)
            throws IDOSException {
        List<BillOfMaterialTxnItemModel> bomItemsList = bomTxn.getBillOfMaterialTxnItemModels();
        boolean isFullfilled = true;
        for (BillOfMaterialTxnItemModel bomItemTxn : bomItemsList) {
            if (bomItemTxn.getIsFulfilled() == null
                    || bomItemTxn.getIsFulfilled() != IdosConstants.FULFILLED_TRANACTION) {
                isFullfilled = false;
            }
        }
        return isFullfilled;
    }
}
