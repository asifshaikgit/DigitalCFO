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
 * @author Harish Kumar created on 30.05.2023
 */
public class CreatePurchaseOrderTxnItemDAOImpl implements CreatePurchaseOrderTxnItemDAO {

    //fetching items to show on item list link in the transactions table
    @Override
    public void getListOfTxnItems(Users user, EntityManager em, long txnId, ArrayNode txnItemsAn) throws IDOSException {
        ArrayList inparams = new ArrayList(2);
        inparams.add(user.getOrganization().getId());
        inparams.add(txnId);
        inparams.add(1);
        List<PurchaseOrderTxnItemModel> bomTxnItemsList = genericDao.queryWithParamsName("from PurchaseOrderTxnItemModel obj where obj.organization.id = ?1 and obj.purchaseOrderTxn.id = ?2 and obj.presentStatus = ?3", em, inparams);
        for (PurchaseOrderTxnItemModel txnItem : bomTxnItemsList) {
            ObjectNode row = Json.newObject();
            row.put("id", txnItem.getId());
            if (txnItem.getExpense() != null) {
                row.put("itemName", txnItem.getExpense().getName());
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
            row.put("typeOfMaterial", txnItem.getTypeOfMaterial() == null ? "" : txnItem.getTypeOfMaterial());
            txnItemsAn.add(row);
        }
    }

    
    //fetching all the item rows of a PO transaction
    @Override
    public List<PurchaseOrderTxnItemModel> getAllItemsOfPO(Users user, EntityManager em, long txnId) throws IDOSException{
        List<PurchaseOrderTxnItemModel> itemList = null;
        ArrayList inparams = new ArrayList(2);
        inparams.add(user.getOrganization().getId());
        inparams.add(txnId);
        inparams.add(1);
        itemList = genericDao.queryWithParamsName("from PurchaseOrderTxnItemModel obj where obj.organization.id = ?1 and obj.purchaseOrderTxn.id = ?2 and obj.presentStatus = ?3", em, inparams);
        
        return itemList;

    }        
    
}

