package com.idos.dao;

import com.idos.util.IDOSException;
import model.Users;
import model.Vendor;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * Created by Sunil K. Namdev on 15-07-2017.
 */
public interface VendorDAO extends BaseDAO {
      String VENDOR_JQL_MASTER = "select obj from Vendor obj where obj.name=?1 and obj.id=?2 and obj.presentStatus=1";
      String VENDOR_JQL_OTHERS = "select obj from Vendor obj where obj.name=?1 and obj.id=?2 and obj.presentStatus=0";

   public Vendor saveVendor(JsonNode json, Users user, EntityManager entityManager, short vendorType)
         throws IDOSException;
}
