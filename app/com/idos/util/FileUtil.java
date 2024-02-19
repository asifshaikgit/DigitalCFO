package com.idos.util;

import model.Organization;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import com.typesafe.config.ConfigFactory;
import javax.inject.Inject;
import play.Application;
/**
 * Created by Sunil Namdev on 19-04-2017.
 */
public class FileUtil {
  private static Application application;

  @Inject
  public FileUtil(Application application) {
    this.application = application;
  }
    private static final int  deploymentType = ConfigFactory.load().getInt("deployment.type");
    private static final int ONLINE = 1;
    private static final int OFFLINE = 2;									 
    public static byte[] readBytesFromFile(String filePath) throws IOException {
        File inputFile = new File(filePath);
        FileInputStream inputStream = new FileInputStream(inputFile);
        byte[] fileBytes = new byte[(int) inputFile.length()];
        inputStream.read(fileBytes);
        inputStream.close();
        return fileBytes;
    }

    public static void saveBytesToFile(String filePath, byte[] fileBytes) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(filePath);
        outputStream.write(fileBytes);
        outputStream.close();
    }

    public static String getFileName(String fileName) {
        if(fileName == null){
            return null;
        }
        fileName = fileName.replaceAll("[^a-zA-Z0-9\\._]+","");
        if(fileName.length() > 4){
            fileName = fileName.trim().substring(0, 4);
        }
        fileName = fileName + "_logo.png";
        return fileName;
    }

    public static double getFileSize(final File file)
    {
        if(file == null){
            return 0d;
        }
        double bytes = file.length();
        double kilobytes = (bytes / 1024);
        double megabytes = (kilobytes / 1024);
        double gigabytes = (megabytes / 1024);
        double terabytes = (gigabytes / 1024);
        double petabytes = (terabytes / 1024);
        double exabytes = (petabytes / 1024);
        double zettabytes = (exabytes / 1024);
        double yottabytes = (zettabytes / 1024);
        return kilobytes;
    }

    public static String resolveFile(String fileName) {
        fileName = fileName.replace("\\", "/");
        fileName = fileName.replaceAll("\\u0020", "%20");
        URI uri;
        String strPath=null;
        try {
            uri = new URI(fileName);
            strPath = uri.getPath();
        }
        catch (URISyntaxException e) {
            //log.log(Level.SEVERE, "Error", e);
        }
        return strPath;
    }

    public static String getCompanyLogo(Organization org) throws Exception{
        boolean isCreate = false;
        String companyLogo = org.getCompanyLogo();
        String logoFileName = org.getLogoFileName();
        if(deploymentType == ONLINE ){
        	if(companyLogo != null && !"".equals(companyLogo)){
	        	if(companyLogo.lastIndexOf("#") != -1){
					companyLogo = companyLogo.substring(companyLogo.lastIndexOf("#")+1);
	        	}
	            if ( companyLogo.startsWith("http")) {
	                isCreate = false;
	            }
        	}
        }else if (companyLogo != null && deploymentType == OFFLINE && !companyLogo.startsWith("http")) {
            isCreate = true;
        }

        if(isCreate){
            if (logoFileName != null && !"".equals(logoFileName)) {
                companyLogo = application.path().toString() + "/images/logo/" + logoFileName;
                File file = new File(companyLogo);
                if (!file.exists()) {
                    saveBytesToFile(companyLogo, org.getLogo());
                }
            }
        }
        return companyLogo;
    }

    public static String sanitizeFileName(String fileName){
        if(fileName == null){
            return fileName;
        }
        fileName = fileName.replaceAll("[^a-zA-Z0-9\\._]+","");
        if(fileName.startsWith(".")){
            fileName = "blob_"+ System.currentTimeMillis() + fileName;
        }
        if(fileName.length() <= 0){
            fileName = "blob"+Math.random();
        }
        return fileName;
    }
}
