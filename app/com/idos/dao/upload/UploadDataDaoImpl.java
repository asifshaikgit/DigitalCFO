package com.idos.dao.upload;

import com.idos.dao.BaseDAO;
import model.Users;
import model.upload.IdosFileLog;
import model.upload.IdosUploadLog;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class UploadDataDaoImpl implements UploadDataDao {

    @Override
    public IdosFileLog saveUploadedFileDeatils(EntityManager em, String fileName, Users user, String operation,
            String uploadDate) {
        IdosFileLog idosUpload = null;
        Query query = em.createQuery(UPLOAD_LOG_JPQL);
        query.setParameter("p1", fileName);
        query.setParameter("p2", operation);
        IdosUploadLog idosUploadLog = query.getResultList().size() > 0 ? (IdosUploadLog) query.getSingleResult() : null;
        if (idosUploadLog != null) {
            idosUpload = new IdosFileLog();
            if (operation.equals("append")) {
                idosUpload.setCreatedBy(user);
            }
            idosUpload.setModifiedBy(user);
            idosUpload.setFileName(fileName);
            idosUpload.setOperation(operation);
            idosUpload.setTimestamp(new java.util.Date());
            idosUpload.setTotalRecords(idosUploadLog.getTotalRecords());
            idosUpload.setRecordsInserted(idosUploadLog.getRecordsInserted());
            idosUpload.setRecordsUpdated(idosUploadLog.getRecordsUpdated());
            idosUpload.setRecordsIgnored(idosUploadLog.getRecordsIgnored());
            idosUpload.setRecordsDeleted(idosUploadLog.getRecordsDeleted());
            BaseDAO.genericDao.saveOrUpdate(idosUpload, user, em);

        }
        return idosUpload;
    }

    @Override
    public IdosUploadLog findByFileName(EntityManager em, String fileName) {
        IdosUploadLog idosUploadLog = null;
        String sbquery = "select obj from IdosUploadLog obj where obj.fileName = ?1";
        Query query = em.createQuery(sbquery);
        query.setParameter("p1", fileName);
        List<IdosUploadLog> list = query.getResultList();
        if (!list.isEmpty() && list.size() > 0) {
            idosUploadLog = list.get(0);
        }
        return idosUploadLog;
    }

    public static long getLineCount(File file) throws IOException {

        try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(file), 1024)) {

            byte[] c = new byte[1024];
            boolean empty = true,
                    lastEmpty = false;
            long count = 0;
            int read;
            while ((read = is.read(c)) != -1) {
                for (int i = 0; i < read; i++) {
                    if (c[i] == '\n') {
                        count++;
                        lastEmpty = true;
                    } else if (lastEmpty) {
                        lastEmpty = false;
                    }
                }
                empty = false;
            }

            if (!empty) {
                if (count == 0) {
                    count = 1;
                } else if (!lastEmpty) {
                    count++;
                }
            }

            return count;
        }
    }

    @Override
    public int disableCsvFile(String fileName, EntityManager em, String tableName) {
        StringBuilder sbr = new StringBuilder("update ").append(tableName).append(" set PRESENT_STATUS = 0");
        sbr.append(" where FILE_NAME = ?1");
        Query query = em.createQuery(sbr.toString());
        query.setParameter(1, fileName);
        return query.executeUpdate();
    }

    @Override
    public int isCsvFileExist(String fileName, EntityManager em, String tableName) {
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "START :: isCsvFileExist() filename: " + fileName + " table: " + tableName);
        StringBuilder sbr = new StringBuilder("select count(1) from ").append(tableName)
                .append(" where PRESENT_STATUS = 1 and FILE_NAME = ?1");
        Query query = em.createQuery(sbr.toString());
        query.setParameter(1, fileName);
        List<Object> list = query.getResultList();
        int count = 0;
        if (!list.isEmpty()) {
            count = ((BigDecimal) list.get(0)).intValue();
        }
        if (log.isLoggable(Level.FINE))
            log.log(Level.FINE, "END :: isCsvFileExist() " + count);
        return count;
    }

}
