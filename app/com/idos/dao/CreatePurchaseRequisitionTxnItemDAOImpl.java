package com.idos.dao;

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import model.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Harish Kumar created on 05.05.2023
 */
public class CreatePurchaseRequisitionTxnItemDAOImpl implements CreatePurchaseRequisitionTxnItemDAO {

    @Override
    public void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException {
        ArrayList inparams = new ArrayList(2);
        inparams.add(user.getOrganization().getId());
        inparams.add(txnId);
        inparams.add(1);
        List<PurchaseRequisitionTxnItemModel> bomTxnItemsList = genericDao.queryWithParamsName("from PurchaseRequisitionTxnItemModel obj where obj.organization.id = ?1 and obj.purchaseRequisitionTxn.id = ?2 and obj.presentStatus = ?3", em, inparams);
        for (PurchaseRequisitionTxnItemModel txnItem : bomTxnItemsList) {
            ObjectNode row = Json.newObject();
            row.put("id", txnItem.getId());
            if (txnItem.getExpense() != null) {
                row.put("itemName", txnItem.getExpense().getName());
                //row.put("itemName", "");
                row.put("itemId", txnItem.getExpense().getId());
            } else {
                row.put("itemName", "");
                row.put("itemId", "");
            }
            //row.put("pricePerUnit", IdosConstants.DECIMAL_FORMAT.format(txnItem.getPricePerUnit() == null ? 0.0 : txnItem.getPricePerUnit()));
            row.put("noOfUnits", IdosConstants.DECIMAL_FORMAT2.format((txnItem.getNoOfUnits() == null ? 0 : txnItem.getNoOfUnits())));
            if(txnItem.getVendor() != null)
                row.put("vendor", txnItem.getVendor().getName());
            else
                row.put("vendor", "");
            row.put("measureName", txnItem.getMeasureName() == null ? "" : txnItem.getMeasureName());
            row.put("oem", txnItem.getOem() == null ? "" : txnItem.getOem());
            //row.put("totalPrice", IdosConstants.DECIMAL_FORMAT.format(txnItem.getTotalPrice() == null ? 0.0 : txnItem.getTotalPrice()));
            //row.put("availableUnits", IdosConstants.DECIMAL_FORMAT.format(txnItem.getAvailableUnits() == null ? 0.0 : txnItem.getAvailableUnits()));
            //row.put("committedUnits", IdosConstants.DECIMAL_FORMAT.format(txnItem.getCommittedUnits() == null ? 0.0 : txnItem.getCommittedUnits()));
            //row.put("orderedUnits", IdosConstants.DECIMAL_FORMAT.format(txnItem.getOrderedUnits() == null ? 0.0 : txnItem.getOrderedUnits()));
            //row.put("netUnits", IdosConstants.DECIMAL_FORMAT.format(txnItem.getNetUnits() == null ? 0.0 : txnItem.getNetUnits()));
            //row.put("fulfilledUnits", IdosConstants.decimalFormat.format(txnItem.getFulfilledUnits() == null ? 0.0 : txnItem.getFulfilledUnits()));
            row.put("typeOfMaterial", txnItem.getTypeOfMaterial() == null ? "" : txnItem.getTypeOfMaterial());
            //row.put("destinationGstin", txnItem.getDestinationGstin() == null ? "" : txnItem.getDestinationGstin());
            //row.put("isFulfilled", txnItem.getIsFulfilled() == null ? "" : (txnItem.getIsFulfilled() == 1 ? "Yes" : "No"));
            txnItemsAn.add(row);
        }
    }

    
    
}
