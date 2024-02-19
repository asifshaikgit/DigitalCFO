package com.idos.dao;

import com.idos.util.IdosConstants;
import model.Specifics;
import model.TransactionItems;
import play.db.jpa.JPAApi;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Sunil K. Namdev on 20-10-2019.
 */
public class TransactionItemDAOImpl implements TransactionItemDAO {
    @Override
    public List<TransactionItems> findTransItemsForVendorDateRange(EntityManager em, Long orgid, Long specId,
            Long vendId, Date toDate, Date fromDate, Long tdsPayableSpecificID) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, " Start ");

        ArrayList inparams = new ArrayList(8);
        inparams.add(orgid);
        inparams.add(specId);
        inparams.add(orgid);
        inparams.add(vendId);
        inparams.add(IdosConstants.TXN_STATUS_ACCOUNTED);
        // inparams.add(fromDate);
        // inparams.add(toDate);
        inparams.add(orgid);
        inparams.add(specId);
        inparams.add(tdsPayableSpecificID);
        List<TransactionItems> list = genericDao.queryWithParams(TDS_GROSS_JPQL, em, inparams);
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, " End " + list.size());
        return list;
    }

    @Override
    public Double getGrossAmountForSpecificAndVendor(EntityManager em, Long orgid, Long specId, Long vendId,
            Date toDate, Date fromDate) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, " Start ");
        Double totalGross = 0.0;
        ArrayList inparams = new ArrayList(5);
        inparams.add(orgid);
        inparams.add(specId);
        inparams.add(orgid);
        inparams.add(vendId);
        inparams.add(IdosConstants.TXN_STATUS_ACCOUNTED);
        // inparams.add(fromDate);
        // inparams.add(toDate);
        List<Object[]> list = genericDao.queryWithParamsNameGeneric(TDS_TOTAL_GROSS_JPQL, em, inparams);
        for (Object gorssData : list) {
            if (gorssData != null) {
                totalGross = (Double) (gorssData);
            }
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, " End " + totalGross);
        return totalGross;
    }

    @Override
    public List<TransactionItems> findByTransaction(EntityManager em, long orgid, long txnId) {
        // if(log.isLoggable(Level.FINE)) log.log(Level.FINE, " Start ");
        ArrayList inparams = new ArrayList(2);
        inparams.add(orgid);
        inparams.add(txnId);
        List<TransactionItems> list = genericDao.queryWithParamsName(BY_TXN_JPQL, em, inparams);
        // if(log.isLoggable(Level.FINE)) log.log(Level.FINE, " End " + list.size());
        return list;
    }
}
