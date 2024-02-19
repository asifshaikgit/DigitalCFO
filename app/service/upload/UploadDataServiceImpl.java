package service.upload;

import model.upload.IdosUploadLog;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @author $(USER) created on $(DATE)
 */
public class UploadDataServiceImpl implements UploadDataService {
    @Override
    public int disableCsvFile(String fileName, EntityManager em, String tableName) {
        return UPLOAD_DAO.disableCsvFile(fileName, em, tableName);
    }

    @Override
    public int isCsvFileExist(String fileName, EntityManager em, String tableName) {
        return UPLOAD_DAO.isCsvFileExist(fileName, em, tableName);
    }

    @Override
    public IdosUploadLog findByFileName(EntityManager em, String fileName) {
        return UPLOAD_DAO.findByFileName(em, fileName);
    }
}
