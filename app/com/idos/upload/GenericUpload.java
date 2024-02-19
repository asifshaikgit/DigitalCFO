package com.idos.upload;
/*
 **************************************************************
 * Description:  This class can be used to upload data into any
 * 				 table specify in config file and do operation
 * 				 by executing the SP.
 *
 *				Steps:
 *				1) Generate the control file for the SQL loader.
 *				2) Spawn sqlldr process
 *				3) Move the files in the archive or error folder.
 *				4) Execute the SP
 *
 * Problem# 		Fixed by						Old Ver#
 *
 **************************************************************
 */

/**
 * @author Sunil K Namdev
 * @version 1.0
 * Date: 22.05.2020
 */

import com.idos.util.IDOSException;
import com.idos.util.IdosConstants;
import com.idos.util.SingletonDBConnection;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import controllers.BaseController;
import model.upload.IdosUploadLog;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.StringTokenizer;
import play.db.jpa.JPAApi;
import javax.persistence.EntityManager;
import service.EntityManagerProvider;
import javax.persistence.Query;

public class GenericUpload {
    Logger log = Logger.getLogger("GenericUpload");
    public final static String CONTROLFILEEXTENSION = ".ctl";
    public final static String DEFEXTENSION = ".log";
    public final static String DEF_URL = "jdbc:mysql://localhost:3306/idos";
    public final static String DEF_FILE_PATTERN = "UPLOAD";
    public final static int DEF_BATCH_COMMIT = 1000;
    public static JPAApi jpaApi;

    protected String controlFileDir;
    protected String inFileDir;
    protected String badFileDir;
    protected String discardFileDir;
    protected String sqlldrLogFileDir;
    protected String archiveDir;
    protected String failedDir;
    protected String url;
    protected String discardMax;
    protected String errors;
    private boolean responseRequired;
    protected String resFileDir;
    private String failureReasonColumn;
    private Config config;
    private EntityManager entityManager;

    public GenericUpload() {
        config = ConfigFactory.parseFile(new File("conf/application.conf"));
        if (!loadConfiguration()) {
            System.out.println("Failed to load configuration.");
            System.exit(-1);
        }
        entityManager = EntityManagerProvider.getEntityManager();
    }

    public boolean loadConfiguration() {
        log.log(Level.FINE, "START:: loadConfiguration()");
        url = ConfigFactory.load().getString("db.default.url");
        if (url == null) {
            log.log(Level.WARNING,
                    "Bad / Missing parameter 'default.url' in the section db' , Using Default " + DEF_URL);
            url = DEF_URL;
        }

        badFileDir = ConfigFactory.load().getString("UploadConfig.badFileDir");
        File tempFile = null;

        if (badFileDir != null)
            tempFile = new File(badFileDir);

        if (badFileDir == null || badFileDir.trim().length() == 0 || tempFile.exists() == false
                || tempFile.isDirectory() == false || tempFile.canWrite() == false) {
            log.log(Level.SEVERE, "Bad / Missing parameter 'badFileDir' in section 'UploadConfig' in config file.");
            return false;
        }

        controlFileDir = ConfigFactory.load().getString("UploadConfig.controlFileDir");
        tempFile = null;

        if (controlFileDir != null)
            tempFile = new File(controlFileDir);

        if (controlFileDir == null || controlFileDir.trim().length() == 0 || tempFile.exists() == false
                || tempFile.isDirectory() == false || tempFile.canWrite() == false) {
            log.log(Level.SEVERE, "Bad / Missing parameter 'controlFileDir' in section 'UploadConfig' in config file.");
            return false;
        }

        inFileDir = ConfigFactory.load().getString("UploadConfig.inFileDir");
        tempFile = null;

        if (inFileDir != null)
            tempFile = new File(inFileDir);

        if (inFileDir == null || inFileDir.trim().length() == 0 || tempFile.exists() == false
                || tempFile.isDirectory() == false || tempFile.canWrite() == false) {
            log.log(Level.SEVERE, "Bad / Missing parameter 'inFileDir' in section 'UploadConfig' in config file.");
            return false;
        }

        discardFileDir = ConfigFactory.load().getString("UploadConfig.discardFileDir");
        tempFile = null;

        if (discardFileDir != null)
            tempFile = new File(discardFileDir);

        if (discardFileDir == null || discardFileDir.trim().length() == 0 || tempFile.exists() == false
                || tempFile.isDirectory() == false || tempFile.canWrite() == false) {
            log.log(Level.SEVERE, "Bad / Missing parameter 'discardFileDir' in section 'UploadConfig' in config file.");
            return false;
        }

        sqlldrLogFileDir = ConfigFactory.load().getString("UploadConfig.sqlldrLogFileDir");
        tempFile = null;

        if (sqlldrLogFileDir != null)
            tempFile = new File(sqlldrLogFileDir);

        if (sqlldrLogFileDir == null || sqlldrLogFileDir.trim().length() == 0 || tempFile.exists() == false
                || tempFile.isDirectory() == false || tempFile.canWrite() == false) {
            log.log(Level.SEVERE,
                    "Bad / Missing parameter 'sqlldrLogFileDir' in section 'UploadConfig' in config file.");
            return false;
        }

        archiveDir = ConfigFactory.load().getString("UploadConfig.archiveDir");
        tempFile = null;

        if (archiveDir != null)
            tempFile = new File(archiveDir);

        if (archiveDir == null || archiveDir.trim().length() == 0 || tempFile.exists() == false
                || tempFile.isDirectory() == false || tempFile.canWrite() == false) {
            log.log(Level.SEVERE, "Bad / Missing parameter 'archiveDir' in section 'UploadConfig' in config file.");
            return false;
        }

        failedDir = ConfigFactory.load().getString("UploadConfig.failedDir");
        tempFile = null;

        if (failedDir != null)
            tempFile = new File(failedDir);

        if (failedDir == null || failedDir.trim().length() == 0 || tempFile.exists() == false
                || tempFile.isDirectory() == false || tempFile.canWrite() == false) {
            log.log(Level.SEVERE, "Bad / Missing parameter 'failedDir' in section 'UploadConfig' in config file.");
            return false;
        }

        discardMax = ConfigFactory.load().getString("UploadConfig.discardMax");

        if (discardMax == null || discardMax.trim().length() == 0) {
            log.log(Level.SEVERE,
                    "Bad / Missing parameter 'discardMax' in section 'UploadConfig' in config file. Using discard all");
            discardMax = null;
        }
        try {
            Integer.parseInt(discardMax);
        } catch (NumberFormatException e) {
            log.log(Level.SEVERE,
                    "Failed to parse discardMax parameter in the section 'UploadConfig' using Using discard all", e);
            discardMax = null;
        }

        errors = ConfigFactory.load().getString("UploadConfig.errors");
        if (errors == null || errors.trim().length() == 0) {
            log.log(Level.SEVERE,
                    "Bad / Missing parameter 'errors' in section 'UploadConfig' in config file. Allowing 50 errors");
            errors = null;
        }
        try {
            Integer.parseInt(errors);
        } catch (NumberFormatException e) {
            log.log(Level.SEVERE, "Failed to parse errors parameter in the section 'UploadConfig'. Allowing 50 errors",
                    e);
            errors = null;
        }

        responseRequired = ConfigFactory.load().getBoolean("UploadConfig.ordered.response.required");
        resFileDir = ConfigFactory.load().getString("UploadConfig.resFileDir");
        tempFile = null;
        if (resFileDir != null)
            tempFile = new File(resFileDir);

        if ((resFileDir == null || resFileDir.trim().length() == 0 || tempFile.exists() == false
                || tempFile.isDirectory() == false || tempFile.canWrite() == false) && responseRequired) {
            log.log(Level.SEVERE, "Bad / Missing parameter 'resFileDir' in section 'UploadConfig' in config file.");
            return false;
        }

        failureReasonColumn = ConfigFactory.load().getString("UploadConfig.failureReason.column");
        if ("".equals(failureReasonColumn) && responseRequired) {
            log.log(Level.SEVERE, "Cannot generate response file because failure reason column name is not configured");
            return false;
        }
        log.log(Level.FINE, "END:: loadConfiguration()");
        return true;
    }

    public boolean moveFile(String fileName, boolean code) {
        File file = new File(inFileDir + File.separator + fileName);
        return moveFile(file, code);
    }

    public boolean moveFile(File file, boolean code) {
        log.log(Level.FINE, "START:: file " + file + " code " + code);
        String filePath = new String();
        boolean returnFalg = false;
        if (code) {
            log.log(Level.FINE, "success to execute");
            filePath = archiveDir;
        } else {
            log.log(Level.FINE, "Failed to execute");
            filePath = failedDir;
        }

        try {
            log.log(Level.INFO, "Original file : " + file.getPath());
            File newfile = new File(filePath + File.separator + file.getName());
            log.log(Level.INFO, "Moveing to : " + newfile.getPath());
            if (!file.renameTo(newfile)) {
                if (!(copyfile(file, newfile) && file.delete())) {
                    log.log(Level.INFO, "Failed to rename file from " + file.getPath() + " to " + newfile.getPath());
                }
            }

            returnFalg = true;
        } catch (Exception e) {
            log.log(Level.SEVERE, "error", e);
            log.log(Level.SEVERE, "Error while archiving the processed file");
        }
        log.log(Level.FINE, "END:: returnFalg " + returnFalg);
        return returnFalg;
    }

    public boolean copyfile(File source, File dest) {
        log.log(Level.FINE, "START:: source " + source + "  dest " + dest);
        boolean returnFlag = false;
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest));

            int ch = -1;
            while ((ch = bis.read()) != -1) {
                bos.write(ch);
            }
            try {
                bis.close();
            } catch (Exception ex) {
                log.log(Level.SEVERE, "error", ex);
            }
            try {
                bos.close();
            } catch (Exception ex) {
                log.log(Level.SEVERE, "error", ex);
            }

            returnFlag = true;
        } catch (Exception e) {
            log.log(Level.SEVERE, "error", e);
        }
        log.log(Level.FINE, "END:: " + returnFlag);
        return returnFlag;
    }

    public boolean uploadData(String uploadType, String operation) throws IDOSException {
        log.log(Level.INFO, "START:: Processing started for upload type : " + uploadType + " operation: " + operation);
        boolean flag = false;
        boolean spFlag = true;
        boolean controlFileFlag = true;
        // EntityManager em = BaseController.getEntityManager();
        File directory = new File(inFileDir);
        if (directory.exists() == false || directory.isDirectory() == false || directory.canRead() == false) {
            return flag;
        }

        File[] list = directory.listFiles();
        if (list == null) {
            return flag;
        }

        String tableName = ConfigFactory.load().getString(uploadType + ".tableName");
        if (tableName == null || tableName.trim().length() == 0) {
            log.log(Level.WARNING,
                    "No table name configure under section " + uploadType + " for parameter 'tableName'. ");
            return false;
        }

        String fileName = null;
        String filePattern = ConfigFactory.load().getString(uploadType + ".fileNamePattern");

        if (filePattern == null) {
            log.log(Level.INFO, "filePattern is null using default format :" + DEF_FILE_PATTERN);
            filePattern = DEF_FILE_PATTERN;
        }
        filePattern = filePattern.toUpperCase();

        String spName = null;
        if ("update".equals(operation)) {
            spName = ConfigFactory.load().getString(uploadType + ".update.spToExecute");
        } else {
            spName = ConfigFactory.load().getString(uploadType + ".append.spToExecute");
        }
        if (spName == null || spName.trim().length() == 0) {
            log.log(Level.WARNING,
                    "No stored procedure configure under section" + uploadType + " for parameter 'spToExecute'. ");
            spFlag = false;
        }

        String batchCommitStr = ConfigFactory.load().getString(uploadType + ".batchCommit");
        int batchCommit = DEF_BATCH_COMMIT;
        try {
            batchCommit = Integer.parseInt(batchCommitStr);
        } catch (Exception e) {
            log.log(Level.WARNING,
                    "No batch commit configure under section" + uploadType + " for parameter 'batchCommit'. ");
        }

        for (int i = 0; i < list.length; i++) {
            File file = list[i];
            fileName = file.getName();
            if (file.isFile() && fileName.toUpperCase().startsWith(filePattern)) {
                log.log(Level.FINE, "processing file is : " + fileName);
                flag = false;
                String loadStr = generateMysqLoadDataFile(file, uploadType, controlFileFlag, fileName);
                if (loadStr != null) {
                    controlFileFlag = false;
                    int sqlldrStatus = spawnMysqlLoadDataProcess(uploadType, fileName, loadStr);
                    if (sqlldrStatus != 0 && sqlldrStatus != 2) {
                        log.log(Level.SEVERE, "loader Failed to upload data in database for file : " + fileName);
                        flag = false;
                    } else {
                        log.log(Level.FINE, "loader executed successfully for file : " + fileName);
                        flag = true;
                    }

                    if (spFlag && flag) {
                        log.log(Level.FINE,
                                "uploadType : " + uploadType + " operation: " + operation + " ,spName : " + spName
                                        + " ,sqlldrStatus : " + sqlldrStatus + " ,batchCommit : " + batchCommit
                                        + " ,responseRequired : " + responseRequired + " ,fileName : " + fileName);
                        if (executeSp(uploadType, spName, fileName, sqlldrStatus, batchCommit, operation)) {
                            log.log(Level.FINE, "SP executed successfully : " + spName);
                            flag = true;
                            if (responseRequired) {
                                writeResponse(uploadType, fileName, tableName, batchCommit);
                            }
                        } else {
                            log.log(Level.SEVERE, "SP executed unsuccessfully : " + spName);
                            flag = false;
                        }
                    }
                }
                moveFile(fileName, flag);
            }
        }
        log.log(Level.FINE, "END:: flag " + flag);
        return flag;
    }

    private int spawnMysqlLoadDataProcess(String uploadType, String inputFileName, String ldSql) throws IDOSException {
        log.log(Level.FINE, "START:: parameters are uploadType " + uploadType + " inputFileName " + inputFileName);
        int sqlldrStatus = 0;
        Connection conn = SingletonDBConnection.getInstance().getConnInst();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            if (!stmt.execute(ldSql)) {
                sqlldrStatus = -1;
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error while uploading data using SQL Loader");
            log.log(Level.SEVERE, "error", ex);
            sqlldrStatus = -1;
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                throw new IDOSException(IdosConstants.UNKNOWN_EXCEPTION_ERRCODE, IdosConstants.TECHNICAL_EXCEPTION,
                        e.getSQLState() + " " + e.getErrorCode(), e.getMessage());
            }
        }
        log.log(Level.FINE, "END:: sqlldrStatus " + sqlldrStatus);
        return sqlldrStatus;
    }

    /**
     * method used to execute SP.
     * 
     * @param uploadType
     * @param spName
     * @param fileName
     * @param sqlldrStatus
     * @param batchCommit
     * @return
     */
    public boolean executeSp(String uploadType, String spName, String fileName, int sqlldrStatus, int batchCommit,
            String operation) throws IDOSException {
        log.log(Level.FINE, "START:: uploadType " + uploadType + " operation: " + operation + " spName " + spName
                + " fileName " + fileName + " sqlldrStatus " + sqlldrStatus + " batchCommit " + batchCommit);
        // CallableStatement cs = null;
        String pool = "default";
        boolean returnValue = false;
        // Connection conn = SingletonDBConnection.getInstance().getConnInst();
        try {
            log.log(Level.FINE, "Calling SP " + spName + " now ...");
            String qu = " call " + spName + "(?, ?, ?, ?) ";
            Query query = entityManager.createNativeQuery(qu);
            query.setParameter(1, fileName.toLowerCase());
            query.setParameter(2, batchCommit);
            query.setParameter(3, operation);
            query.setParameter(4, sqlldrStatus);
            query.getResultList();

            log.log(Level.INFO, "Processing successfully completed for files. ");
            returnValue = true;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while processing file of types ");
            log.log(Level.SEVERE, "error", e);
        }
        log.log(Level.FINE, "END:: returnValue: " + returnValue);
        return returnValue;
    }

    // method used to generate control file
    public String generateMysqLoadDataFile(File file, String uploadType, boolean flag, String fileName) {
        log.log(Level.FINE,
                "START:: file " + file + " uploadType " + uploadType + " flag " + flag + " fileName: " + fileName);
        StringBuffer controlStr = null;
        try {
            if (!flag) {
                log.log(Level.INFO, "control file already generated");
                return null;
            }

            if (file.exists() == false || file.isDirectory() != false || file.canWrite() == false
                    || file.canRead() == false) {
                log.log(Level.SEVERE, "Insufficient privilages on the in file:" + file);
                return null;
            }

            controlStr = new StringBuffer();
            FileOutputStream controlfile = null;

            String tableName = ConfigFactory.load().getString(uploadType + ".tableName");
            String operation = ConfigFactory.load().getString(uploadType + ".operation");
            String terminatedBy = ConfigFactory.load().getString(uploadType + ".terminatedBy");
            String enclosedBy = ConfigFactory.load().getString(uploadType + ".enclosedBy");
            String options = ConfigFactory.load().getString(uploadType + ".options");
            String lineTerminatedBy = ConfigFactory.load().getString(uploadType + ".lineTerminatedBy");

            if (tableName == null || tableName.trim().equals("")) {
                log.log(Level.SEVERE,
                        "No table name  configure under section" + uploadType + " for parameter 'tableName'. ");
                return null;
            }

            if (terminatedBy == null || terminatedBy.trim().equals("")) {
                log.log(Level.SEVERE,
                        "No terminatedby  configure under section" + uploadType + " for parameter 'terminatedBy'. ");
                return null;
            }
            if ("'".equals(terminatedBy)) {
                terminatedBy = "\"" + terminatedBy + "\"";
            } else {
                terminatedBy = "'" + terminatedBy + "'";
            }

            if (enclosedBy != null && !enclosedBy.trim().equals("")) {
                if ("'".equals(enclosedBy)) {
                    enclosedBy = "\"" + enclosedBy + "\"";
                } else {
                    enclosedBy = "'" + enclosedBy + "'";
                }
            }

            if (lineTerminatedBy != null && !lineTerminatedBy.trim().equals("")) {
                if ("'".equals(lineTerminatedBy)) {
                    lineTerminatedBy = "\"" + lineTerminatedBy + "\"";
                } else {
                    lineTerminatedBy = "'" + lineTerminatedBy + "'";
                }
            }

            String fileABSPath = file.getAbsolutePath().replace("\\", "/");
            controlfile = new FileOutputStream(controlFileDir + File.separator + uploadType + CONTROLFILEEXTENSION);
            controlStr.append("LOAD DATA LOCAL INFILE " + "'" + fileABSPath + "'" + "\n");
            controlStr.append("INTO TABLE  " + tableName + "\n");
            controlStr.append("FIELDS TERMINATED BY " + terminatedBy + "\n");
            controlStr.append("ENCLOSED BY ").append(enclosedBy).append("\n");
            controlStr.append("LINES TERMINATED BY ").append("'\\").append("n'");
            controlStr.append("\n");
            controlStr.append("SET FILE_NAME='").append(file.getName()).append("'");
            controlStr.append(", USER_ID=").append(100);

            log.log(Level.FINE, "\n------->>>\n" + controlStr + "\n<<<---------\n");
            controlfile.write(controlStr.toString().getBytes());
            controlfile.close();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error generating the Control File for data upload");
            log.log(Level.SEVERE, "error", e);

        }
        log.log(Level.FINE, "END::generateControlFile( )");
        return controlStr.toString();
    }

    public void writeResponse(String uploadType, String fileName, String tableName, int commitCount) {
        StringBuffer query = new StringBuffer("SELECT ");
        List columnNameMap = ConfigFactory.load().getAnyRefList(uploadType + ".Response.Fields");
        int columnSize = columnNameMap.size();

        for (int j = 0; j < columnSize; j++) {
            if (j != 0) {
                query.append(",");
            }
            String columnName = (String) columnNameMap.get(j);

            if ("FAILURE_REASON".equalsIgnoreCase(columnName)) {
                columnName = "DECODE (FAILURE_REASON, NULL, NULL, '\"' || FAILURE_REASON || '\"')";
            }

            query.append(columnName);
        }

        query.append(" From ");
        query.append(tableName);
        query.append(" Where ");
        query.append(failureReasonColumn);
        query.append(" IS NOT NULL ");
        Connection conn = SingletonDBConnection.getInstance().getConnInst();
        Statement statement = null;
        ResultSet rs = null;
        int index = fileName.indexOf(".");
        if (index < 0) {
            fileName = fileName + "_failed";
        } else {
            fileName = fileName.substring(0, index) + "_failed" + fileName.substring(index, fileName.length());
        }
        fileName = resFileDir + File.separator + fileName;

        log.log(Level.INFO, "log.file = " + fileName);

        try {
            String sql = query.toString();
            log.log(Level.FINE, "Response query: " + sql);
            int count = 0;
            log.log(Level.FINE, "Writing response to file now ...");

            statement = conn.createStatement();
            rs = statement.executeQuery(sql);

            while (rs.next()) {
                StringBuffer sb = new StringBuffer();
                for (int j = 1; j <= columnSize; j++) {
                    if (j != 1) {
                        sb.append(",");
                    }
                    Object obj = rs.getObject(j);
                    if (obj != null) {
                        sb.append(obj);
                    } else {
                        sb.append("");
                    }
                }
                count++;
            }

            rs.close();
            statement.close();
            log.log(Level.FINE, "Total number of records written to response file is : " + count);

        } catch (Exception e) {
            log.log(Level.SEVERE, "Exception has occurred while generating response file");
            log.log(Level.SEVERE, "error", e);
            return;
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "Exception has occurred while closing the result set");
                    log.log(Level.SEVERE, "error", e);
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    log.log(Level.SEVERE, "Exception has occurred while closing the result statement", e);
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Exception has occurred while closing the database connection");
                    log.log(Level.SEVERE, "error", e);
                }
            }
        }
        deleteFromDatabase(tableName);
        log.log(Level.INFO, "Response file successfully generated...");
    }

    private void deleteFromDatabase(String tableName) {
        String deleteQuery = "truncate table " + tableName;
        Connection conn = SingletonDBConnection.getInstance().getConnInst();
        Statement statement = null;
        String pool = "default";
        try {
            log.log(Level.FINE, "Executing query " + deleteQuery + " now ...");
            statement = conn.createStatement();
            statement.executeUpdate(deleteQuery);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error while deleting the record from the database.");
            log.log(Level.SEVERE, "error", e);
        } finally {
            try {
                if (statement != null)
                    statement.close();

                if (conn != null)
                    conn.close();
            } catch (Exception e) {
                log.log(Level.SEVERE, "Exception while returning connection to the pool", e);
            }
        }
    }

    public static void main(String args[]) throws Exception {

        String someInst = "Need to have an instance of some class :-(";
        Config conf = ConfigFactory.parseFile(new File("conf/application.conf"));
        if (conf == null) {
            System.err.println("Failed to find configuration file 'application.conf' in CLASSPATH");
            System.exit(0);
        }
        System.out.println("Scanning configuration data ...");

        String uploadType = conf.getString("UploadConfig.UploadType");
        StringTokenizer tokens = new StringTokenizer(uploadType, ",");

        GenericUpload genericUpload = new GenericUpload();
        while (tokens.hasMoreTokens()) {
            genericUpload.uploadData(tokens.nextToken(), "append");
        }
        System.out.println("Execution Completed ");
        System.exit(0);
    }
}
