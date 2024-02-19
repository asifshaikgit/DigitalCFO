package com.idos.dao.upload;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

import com.idos.dao.BaseDAO;
import model.upload.IdosFileLog;

import model.Users;
import model.upload.IdosUploadLog;

public interface UploadDataDao extends BaseDAO {

    public static String UPLOAD_LOG_JPQL = "select obj from IdosUploadLog obj where obj.status like '%completed' and obj.fileName= ?1 and obj.operation = ?2 order by obj.timestamp desc";

    public IdosFileLog saveUploadedFileDeatils(EntityManager em, String fileName, Users user, String operation,
            String uploadDate);

    public int isCsvFileExist(String fileName, EntityManager em, String tableName);

    public int disableCsvFile(String fileName, EntityManager em, String tableName);

    public IdosUploadLog findByFileName(EntityManager em, String fileName);

}
