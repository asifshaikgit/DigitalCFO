/* Project: IDOS 1.0
 * Module: IDOS utils
 * Filename: IdosUtil.java
 * Component Realisation: Java Class
 * Prepared By: Sunil Namdev
 * Description: keeps utils methods
 * Copyright (c) 2016 IDOS

 * MODIFICATION HISTORY
 * Version		Date		   	Author		      Remarks
 * -------------------------------------------------------------------------
 *  0.1  Aug 30, 2016	        Sunil Namdev	  - Initial Version
 * -------------------------------------------------------------------------
 */

package com.idos.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import model.Organization;
import model.TrialBalanceLedgerReport;
import model.Users;

import org.apache.commons.lang.RandomStringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;

public class IdosUtil {
	private static ObjectMapper jsonObjectMapper = new ObjectMapper();

	public static String getIntegrationKey() {
		StringBuilder key = new StringBuilder();
		key.append("ID");
		key.append(RandomStringUtils.random(12, true, true));
		key.append("OS");
		return key.toString();
	}

	/**
	 * This method checks whether the Object is String object, if it is a
	 * String, A blank string or a zero length string, are also considered to be
	 * null strings. This method is used to validate parameters passed to a
	 * function. The return value indicates whether the string is null or not,
	 * for null strings, “true” is returned and for all other cases, “false” is
	 * returned. if it is not a String, it simple checks for 'null' then returns
	 * true if it is null otherwise false.
	 *
	 * @param param - Object to be checked for null
	 * @return true if Object is null, false otherwise
	 */
	public static boolean isNull(Object param) {
		if (param instanceof String) {
			if (param == null || ((String) param).length() == 0 || ((String) param).trim().equals(""))
				return true;
		} else if (param == null) {
			return true;
		}
		return false;
	}

	public static String escapeHtml(String str){
		return org.apache.commons.lang3.StringEscapeUtils.escapeHtml4(str);
	}

	public static String unescapeHtml(String str){
		return org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4(str);
	}

	/*

	public static String escape(String str){

	public static final HashMap m = new HashMap();
	static {
		m.put(34, "&quot;"); // < - less-than
		m.put(60, "&lt;");   // < - less-than
		m.put(62, "&gt;");   // > - greater-than
		//User needs to map all html entities with their corresponding decimal values.
		//Please refer to below table for mapping of entities and integer value of a char
	}
		int len = str.length();
		StringBuilder newStr = new StringBuilder();
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			int ascii = (int) c;
			String entityName = (String) m.get(ascii);
			if (entityName == null) {
				if (c > 0x7F) {
					newStr.append("&#");
					newStr.append(Integer.toString(c, 10));
					newStr.append(';');
				} else {
					newStr.append(c);
				}
			} else {
				newStr.append(entityName);
			}
		}
		return newStr.toString();
	}*/

	public static String replaceFormatingChar(final String toBeEscaped){
		if(toBeEscaped == null){
			return null;
		}
		StringBuilder escapedStr = new StringBuilder();
		for (int i = 0; i < toBeEscaped.length(); i++) {
			if ((toBeEscaped.charAt(i) != '\n') && (toBeEscaped.charAt(i) != '\r') && (toBeEscaped.charAt(i) != '\t')) {
				escapedStr.append(toBeEscaped.charAt(i));
			}else{
				escapedStr.append(' ');
			}
		}
		return escapedStr.toString();
	}


	public static void seOrganization4Report(Map<String, Object> params, Organization org) throws Exception{
		String companyLogo = FileUtil.getCompanyLogo(org);
		if(companyLogo != null && !"".equals(companyLogo)) {
			params.put("companyLogo", companyLogo);
		}
		if(org.getName()!=null){
			params.put("companyName",org.getName());
		}
		if(org.getRegisteredAddress()!=null){
			String address = org.getRegisteredAddress().replaceAll("\\r\\n|\\r|\\n", " ");
			params.put("companyAddress", address);
		}
		if(org.getCorporateMail()!=null){
			params.put("companyEmail", org.getCorporateMail());
		}
		if(org.getRegisteredPhoneNumber()!=null){
			params.put("companyPhNo", org.getRegisteredPhoneNumber());
		}
		if(org.getWebUrl()!=null){
			if(org.getWebUrl().indexOf("#") != -1){
				String url = org.getWebUrl().substring(org.getWebUrl().lastIndexOf("#")+1);
				params.put("companyURL",url);
			}else{
				params.put("companyURL", org.getWebUrl());
			}
		}
	}
	
	public static List<String> getStateNames(){
		List<String> list = new ArrayList<String>(IdosConstants.STATE_CODE_MAPPING.values());
		Collections.sort(list);
		return list;
	}
	
	public static List<String> getStateCodes(){	
		List<String> list = new ArrayList<String>(IdosConstants.STATE_CODE_MAPPING.keySet());
		Collections.sort(list);
		return list;
	}
	
	public static String getStateCode(final String name) {
    	String res = null;
    	if (null != name && !"".equals(name)) {
    		Map<String, String> states = IdosConstants.STATE_CODE_MAPPING;	    		
    	    for (String key : states.keySet()) {
                if (states.get(key).equalsIgnoreCase(name)) {
                    return key;
                }
            }	    		
    	}
    	return res;
    }

     public static String getOrganizationName4Report(Users user){
		String orgName = user.getOrganization().getName() == null ? "" : user.getOrganization().getName().trim();
		orgName = orgName.replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "");
		if(orgName.indexOf("/") != -1){
			orgName = orgName.replaceAll("/", "");
		}
		if(orgName.indexOf("\\") != -1){
			orgName = orgName.replaceAll("\\\\", "");
		}
		orgName = orgName.replaceAll("[^a-zA-Z0-9]+","");
		if(orgName.length() > 8){
			orgName = orgName.substring(0, 7);
		}
		return orgName;
	}

	public static String getOrganizationName4Blob(String orgName){
		if(orgName == null){
			return orgName;
		}
		orgName = orgName.replaceAll("\\r\\n|\\r|\\n|\\t|\\s+", "");
		if(orgName.indexOf("/") != -1){
			orgName = orgName.replaceAll("/", "");
		}
		if(orgName.indexOf("\\") != -1){
			orgName = orgName.replaceAll("\\\\", "");
		}
		orgName = orgName.replaceAll("[^a-zA-Z0-9]+","");
		if(orgName.length() > 8){
			orgName = orgName.substring(0, 7);
		}
		return orgName;
	}

	public static void setUserItemsDetail( List<String> coaList,  List<String> fromAmt, List<String> toAmount, String itemid, String itemName, String headType, ArrayNode coaItemDataArray) {
		itemid = headType+itemid;
		ObjectNode row = Json.newObject();
		row.put("id", itemid );
		row.put("name", itemName);
		row.put("headType", headType);
		if (null != coaList) {
			int index = coaList.indexOf(itemid);
			if (index != -1) {
				row.put("isChecked", "checked");
				if (fromAmt != null && fromAmt.size() > index && (fromAmt.get(index) != null && !"".equals(fromAmt.get(index)))) {
					row.put("fromAmount", IdosConstants.decimalFormat.format(Double.parseDouble(fromAmt.get(index))));
				} else {
					row.put("fromAmount", 0.0);
				}
				if (toAmount != null && toAmount.size() > index  && (toAmount.get(index) != null && !"".equals(toAmount.get(index)))) {
					row.put("toAmount", IdosConstants.decimalFormat.format(Double.parseDouble(toAmount.get(index))));
				} else {
					row.put("toAmount", 0.0);
				}
			} else {
				row.put("isChecked", false);
				row.put("fromAmount", 0.0);
				row.put("toAmount", 0.0);
			}
		} else {
			row.put("isChecked", false);
			row.put("fromAmount", 0.0);
			row.put("toAmount", 0.0);
		}
		coaItemDataArray.add(row);
	}

	public static Date getFormatedDate(String dateString) throws IDOSException {
		Date txnDate = null;
        try {
            if (dateString != null && !"".equals(dateString)) {
            	dateString = dateString + " 23:59:59";
                txnDate = IdosConstants.IDOSDF.parse(dateString);
            } else {
                txnDate = new Date();
            }
        }catch (ParseException e){
            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION, IdosConstants.NULL_KEY_EXC_ESMF_MSG, "cannot parse date: " + dateString + " " + e.getMessage());
        }
         return txnDate;
	}
	
	public static Date getFormatedDateWithTime(String dateString) throws IDOSException {
		Date txnDate = null;
        try {
            if (dateString != null && !"".equals(dateString)) {
            	dateString = dateString + " 23:59:59";
                txnDate = IdosConstants.IDOSDTF.parse(dateString);
            } else {
                txnDate = new Date();
            }
        }catch (ParseException e){
            throw new IDOSException(IdosConstants.DATA_FORMAT_ERRCODE, IdosConstants.DATA_FORMAT_EXCEPTION, IdosConstants.NULL_KEY_EXC_ESMF_MSG, "cannot parse date: " + dateString + " " + e.getMessage());
        }
         return txnDate;
	}
	public static String removeLastChar(String s) {
		if (s == null || s.length() == 0) {
			return s;
		}
		return s.substring(0, s.length()-1);
	}

	/**
	 * Converts an <code>array</code> argument type to a Java List suitable for passing as one
	 * of the argument values to exchange.declare, queue.bind, queue.declare.
	 *
	 * @param arrayNode The array node pointing to the value of the array argument.
	 * @return A list with all members converted to the appropriate types.
	 * @throws IOException If an exception occurs parsing the nodes.
	 */
	public static List<TrialBalanceLedgerReport> convertArrayNodeToList(ArrayNode arrayNode) throws IOException {
		/* final List<TrialBalanceLedgerReport> list = new ArrayList<TrialBalanceLedgerReport>(arrayNode.size());
		ObjectMapper objectMapper = new ObjectMapper();
		for (Iterator<JsonNode> it = arrayNode.getElements(); it.hasNext(); ) {
			final JsonNode node = it.next();
			if (!node.isObject()) {
				throw new IllegalArgumentException("Invalid node: " + node);
			}
			
            String jsonstring = objectMapper.writeValueAsString(node);
			TrialBalanceLedgerReport newJsonNode = objectMapper.readValue(jsonstring, TrialBalanceLedgerReport.class);
			
			list.add(newJsonNode);
		} */
		final List<TrialBalanceLedgerReport> list = new ArrayList<>(arrayNode.size());
        ObjectMapper objectMapper = new ObjectMapper();
        for (JsonNode node : arrayNode) {
			if (!node.isObject()) {
				throw new IllegalArgumentException("Invalid node: " + node);
			}
			TrialBalanceLedgerReport newJsonNode = objectMapper.convertValue(node, TrialBalanceLedgerReport.class);
			list.add(newJsonNode);
		}
		return list;
	}

	public static List<TrialBalanceLedgerReport> removeDuplicates(List<TrialBalanceLedgerReport> list) {
		// Set set1 = new LinkedHashSet(list);
		Set set = new TreeSet(new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				if (((TrialBalanceLedgerReport) o1).getLedgerId().equalsIgnoreCase(((TrialBalanceLedgerReport) o2).getLedgerId())) {
					return 0;
				}
				return 1;
			}
		});
		set.addAll(list);
		final List newList = new ArrayList(set);
		return newList;
	}

/*new code added start*/
	static String gstinOldFormat = "[0-9]{2}[a-zA-Z]{5}[0-9]{4}[a-zA-Z]{1}[0-9A-Za-z]{3}";
	static String gstinFormat = "[0-9]{2}[a-zA-Z]{5}[0-9]{4}[a-zA-Z]{1}[0-9A-Za-z]{1}[Zz]{1}[0-9a-zA-Z]{1}";
				
	public static String branchAndMultiGstinValidate(String gstinCode, String vendorBranchId) throws IDOSException {
		String gstinCodeCheck = gstinCode;
		String checkgstincode;
		if((gstinCode != null && !"".equals(gstinCode) ) && (gstinCode.length() == 15)){
			checkgstincode = gstinCodeCheck ; 
			if(vendorBranchId == null || vendorBranchId.equals("")){
				if ((checkgstincode.trim()).matches(gstinFormat)) {
					gstinCode = checkgstincode ;
				}else {
					throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION, "Please Check GSTIN format and provide valid GSTIN: " + gstinCode);
				}
			}else{
				if (((checkgstincode.trim()).matches(gstinOldFormat)) || ((checkgstincode.trim()).matches(gstinFormat))) {
					gstinCode = checkgstincode ;
				}else{
					throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION, "Please Check the GSTIN format and provide correct GSTIN: " + gstinCode);	
				}
			}
		}else if((gstinCode == null && "".equals(gstinCode) ) || (gstinCode.length() < 15 && gstinCode.length() != 2) || gstinCode.length() > 15){
			throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION, "Invalid GSTIN! Please provide valid GSTIN: " + gstinCode);
		}
		return gstinCode;
	}

	public static String gstinValidate(Integer registeredOrUnReg, String gstinCode, String vendorId) throws IDOSException {
		String gstinCodeCheck = gstinCode;
		String checkGstinCode;
		if(registeredOrUnReg == 1 && ((gstinCode != null && !"".equals(gstinCode) ))&& (gstinCode.length() == 15)){
			checkGstinCode = gstinCodeCheck ; 
			if(vendorId == null || vendorId.equals("")){
				if ((checkGstinCode.trim()).matches(gstinFormat)) {
					gstinCode = checkGstinCode ;
				}else{
					throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION, "Please Check the GSTIN format and provide correct GSTIN: " + gstinCode);	
				}
			}else{
				if (((checkGstinCode.trim()).matches(gstinOldFormat)) || ((checkGstinCode.trim()).matches(gstinFormat))) {
					gstinCode = checkGstinCode ;
				}else{
					throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION, "Please Check the GSTIN format and provide correct GSTIN: " + gstinCode);	
				}	    		

			}			

		}else if(registeredOrUnReg == 1 && ((gstinCode == null && "".equals(gstinCode) ) || (gstinCode.length() < 15 || gstinCode.length() > 15))){
			throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION, "Invalid GSTIN! Please provide correct GSTIN." );

		}else if(registeredOrUnReg == 0 && (gstinCode != null && !"".equals(gstinCode) ) ){
			throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION, "GSTIN is not required for unregistered member ");

		}	    
		return gstinCode;
	}	

/*new code added end*/
	
	public static double convertStringToDouble(String numberString) throws IDOSException{
		double number = 0.0;
		if(numberString == null || "".equals(numberString) || "null".equalsIgnoreCase(numberString) || "undefined".equalsIgnoreCase(numberString)){
			return number;
		}
		try{
			number = Double.parseDouble(numberString);					
		}catch(NumberFormatException nfe){
			throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION, "String is not a valid number of type double : " + numberString + " " +nfe.getMessage() );
		}
		return number;
	}
	public static long convertStringToLong(String numberString) throws IDOSException{
		long number = 0L;
		if(numberString == null || "".equals(numberString) || "null".equalsIgnoreCase(numberString) || "undefined".equals(numberString) || "NaN".equals(numberString)){
			return number;
		}
		try{
			number = Long.parseLong(numberString);					
		}catch(NumberFormatException nfe){
			throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION, "String is not a valid number of type long : " + numberString + " " +nfe.getMessage() );
		}
		return number;
	}
	public static float convertStringToFloat(String numberString) throws IDOSException{
		float number = 0.0f;
		if(numberString == null || "".equals(numberString) || "null".equalsIgnoreCase(numberString) || "undefined".equals(numberString)){
			return number;
		}
		try{
			number = Float.parseFloat(numberString);						
		}catch(NumberFormatException nfe){
			throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION, "String is not a valid number of type float : " + numberString + " " +nfe.getMessage() );
		}
		return number;
	}
	public static int convertStringToInt(String numberString) throws IDOSException{
		int number = 0;
		if(numberString == null || "".equals(numberString) || "null".equalsIgnoreCase(numberString) || "undefined".equals(numberString)){
			return number;
		}
		try{
			number = Integer.parseInt(numberString);					
		}catch(NumberFormatException nfe){
			throw new IDOSException(IdosConstants.INVALID_DATA_ERRCODE, IdosConstants.BUSINESS_EXCEPTION, IdosConstants.INVALID_DATA_EXCEPTION, "String is not a valid number of type int : " + numberString + " " +nfe.getMessage() );
		}
		return number;
	}
}


