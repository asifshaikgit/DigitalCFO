package service.upload;

import model.upload.IdosUploadLog;
import service.BaseService;

import javax.persistence.EntityManager;
import service.EntityManagerProvider;

/**
 * @author $(USER) created on $(DATE)
 */
public interface UploadDataService extends BaseService {
    public int disableCsvFile(String fileName, EntityManager em, String tableName);

    public int isCsvFileExist(String fileName, EntityManager em, String tableName);

    public IdosUploadLog findByFileName(EntityManager em, String fileName);

}
