package com.idos.util;

import model.Transaction;
import model.Users;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Sunil K. Namdev on 5.9.2019
 */
public class UploadUtil {
    public static String getUploadDocuments(String existingList, String uploadDocs, Users user){
        ArrayList<String> existingUrlList = null;
        ArrayList <String> urlsToDeleteList = null;
        if(existingList != null){
            //urlsToDeleteList = new  ArrayList<>(5);
            String existingArray[] = existingList.split(",");
            existingUrlList = new ArrayList<>(1);

            for(int j=0; j < existingArray.length; j++){
                String []userAndUrl = existingArray[j].split("#");
                if(userAndUrl.length > 1){
                    existingUrlList.add(userAndUrl[1]);
                }else if(userAndUrl.length > 0 && (userAndUrl[0]).startsWith("http")){
                    existingUrlList.add(userAndUrl[0]);
                }
            }
        }

        StringBuilder tmpSb = null;
        if (uploadDocs != null && !uploadDocs.equals("")) {
            String uploadDocsArray[] = uploadDocs.split(",");
            for (int i = 0; i < uploadDocsArray.length; i++) {
                if(existingUrlList != null) {
                    for (int j = 0; j < existingUrlList.size(); j++) {
                        if (existingUrlList.get(j).equals(uploadDocsArray[i])) {
                            existingUrlList.remove(uploadDocsArray[i]);
                        }
                    }
                }
                if (tmpSb == null) {
                    tmpSb = new StringBuilder(user.getEmail()).append("#").append(uploadDocsArray[i]);
                } else {
                    tmpSb.append(",").append(user.getEmail()).append("#").append(uploadDocsArray[i]);
                }
            }
        }
        if(tmpSb != null) {
            return tmpSb.toString();
        }else{
            return null;
        }
    }

    public static List<String> getSupportingDocuments(String existingDocs, String email, String supportingdoc){
        StringBuilder txnDocument = new StringBuilder();
        List <String> list = new ArrayList<String>(2);
        if (supportingdoc != null && !supportingdoc.equals("")) {
            String suppdocarr[] = supportingdoc.split(",");
            if(existingDocs != null) {
                for (int i = 0; i < suppdocarr.length; i++) {
                    if (i == 0) {
                        txnDocument.append(email).append("#").append(suppdocarr[i]);
                    } else {
                        txnDocument.append(",").append(email).append("#").append(suppdocarr[i]);
                    }
                    if(existingDocs.indexOf(suppdocarr[i]) != 1){
                        existingDocs = removeSpecificUrl(existingDocs, suppdocarr[i]);
                    }
                }
                //String [] deleteDocsArr = existingDocs.toString().split(",");
            }else{
                for (int i = 0; i < suppdocarr.length; i++) {
                    if (i == 0) {
                        txnDocument.append(email).append("#").append(suppdocarr[i]);
                    } else {
                        txnDocument.append(",").append(email).append("#").append(suppdocarr[i]);
                    }
                }
            }
        }
        list.add(txnDocument.toString());
        list.add(existingDocs);
        return list;
    }

    public static String removeSpecificUrl(String s, String url) {
        String ns = null;
        if (s == null || s.length() == 0 || url == null || url.length() == 0) {
            return s;
        }
        int indx = s.indexOf(url);
        if(indx != -1){
            String [] arr = s.split(",");
            for (int i = 0, arrLength = arr.length; i < arrLength; i++) {
                String e = arr[i];
                if(e.indexOf(url) != -1){
                    if((s.indexOf(e+',') != -1)){
                    	ns = s.replace(e+",", "");
                    	break;
                    } else{
                        ns = s.replace(e, "");
                        break;
                    }
                }
            }
        }else{
            ns = s;
        }
        return  ns;
    }
}
