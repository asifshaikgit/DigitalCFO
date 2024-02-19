package com.idos.cache;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @auther Sunil Namdev created on 20.01.2018
 */
public class UserTxnRuleSetupCache {
	private static final String INCOME = "INCOME";
	private static final String EXPENSE = "EXPENSE";
	private static final String ASSET = "ASSET";
	private static final String LIAB = "LIAB";
	private static final String FROM_AMT = "FROM_AMT";
	private static final String TO_AMT = "TO_AMT";
	public static final int ALL_HEAD = 0;
	public static final int INCOME_HEAD = 1;
	public static final int EXPENSE_HEAD = 2;
	public static final int ASSETS_HEAD = 3;
	public static final int LIABILITES_HEAD = 4;
	public static final int CREATOR = 1;
	public static final int APPROVER = 2;
	public static final int AUDITOR = 3;
	private static ConcurrentHashMap<String, HashMap<String, String>> CREATOR_INCOA = null;
	private static ConcurrentHashMap<String, HashMap<String, String>> CREATOR_EXCOA = null;
	private static ConcurrentHashMap<String, HashMap<String, String>> CREATOR_ASCOA = null;
	private static ConcurrentHashMap<String, HashMap<String, String>> CREATOR_LICOA = null;

	private static ConcurrentHashMap<String, HashMap<String, String>> APPROVER_INCOA  = null;
	private static ConcurrentHashMap<String, HashMap<String, String>> APPROVER_EXCOA  = null;
	private static ConcurrentHashMap<String, HashMap<String, String>> APPROVER_ASCOA  = null;
	private static ConcurrentHashMap<String, HashMap<String, String>> APPROVER_LICOA  = null;

	private static ConcurrentHashMap<String, HashMap<String, String>> AUDITOR_INCOA   = null;
	private static ConcurrentHashMap<String, HashMap<String, String>> AUDITOR_EXCOA   = null;
	private static ConcurrentHashMap<String, HashMap<String, String>> AUDITOR_ASCOA   = null;
	private static ConcurrentHashMap<String, HashMap<String, String>> AUDITOR_LICOA   = null;
	static {
		CREATOR_INCOA = new ConcurrentHashMap<String, HashMap<String, String>>();
		CREATOR_EXCOA = new ConcurrentHashMap<String, HashMap<String, String>>();
		CREATOR_ASCOA = new ConcurrentHashMap<String, HashMap<String, String>>();
		CREATOR_LICOA = new ConcurrentHashMap<String, HashMap<String, String>>();

		APPROVER_INCOA = new ConcurrentHashMap<String, HashMap<String, String>>();
		APPROVER_EXCOA = new ConcurrentHashMap<String, HashMap<String, String>>();
		APPROVER_ASCOA = new ConcurrentHashMap<String, HashMap<String, String>>();
		APPROVER_LICOA = new ConcurrentHashMap<String, HashMap<String, String>>();

		AUDITOR_INCOA = new ConcurrentHashMap<String, HashMap<String, String>>();
		AUDITOR_EXCOA = new ConcurrentHashMap<String, HashMap<String, String>>();
		AUDITOR_ASCOA = new ConcurrentHashMap<String, HashMap<String, String>>();
		AUDITOR_LICOA = new ConcurrentHashMap<String, HashMap<String, String>>();
	}
	public static String getCOA(String userid, int ruleType, int headType){
		String ret = null;
		if(ruleType == CREATOR && headType == INCOME_HEAD) {
			HashMap<String, String> mp = CREATOR_INCOA.get(userid);
			if(mp != null)
				ret = mp.get(INCOME);
		}else if(ruleType == CREATOR && headType == EXPENSE_HEAD){
			HashMap<String, String> mp = CREATOR_EXCOA.get(userid);
			if(mp != null)
				ret = mp.get(EXPENSE);
		}else if(ruleType == CREATOR && headType == ASSETS_HEAD){
			HashMap<String, String> mp = CREATOR_ASCOA.get(userid);
			if(mp != null)
				ret = mp.get(ASSET);
		}else if(ruleType == CREATOR && headType == LIABILITES_HEAD){
			HashMap<String, String> mp = CREATOR_LICOA.get(userid);
			if(mp != null)
				ret = mp.get(LIAB);
		}else if(ruleType == APPROVER && headType == INCOME_HEAD) {
			HashMap<String, String> mp = APPROVER_INCOA.get(userid);
			if(mp != null)
				ret = mp.get(INCOME);
		}else if(ruleType == APPROVER && headType == EXPENSE_HEAD){
			HashMap<String, String> mp = APPROVER_EXCOA.get(userid);
			if(mp != null)
				ret = mp.get(EXPENSE);
		}else if(ruleType == APPROVER && headType == ASSETS_HEAD){
			HashMap<String, String> mp = APPROVER_ASCOA.get(userid);
			if(mp != null)
				ret = mp.get(ASSET);
		}else if(ruleType == APPROVER && headType == LIABILITES_HEAD){
			HashMap<String, String> mp = APPROVER_LICOA.get(userid);
			if(mp != null)
				ret = mp.get(LIAB);
		}else if(ruleType == AUDITOR && headType == INCOME_HEAD) {
			HashMap<String, String> mp = AUDITOR_INCOA.get(userid);
			if(mp != null)
				ret = mp.get(INCOME);
		}else if(ruleType == AUDITOR && headType == EXPENSE_HEAD){
			HashMap<String, String> mp = AUDITOR_EXCOA.get(userid);
			if(mp != null)
				ret = mp.get(EXPENSE);
		}else if(ruleType == AUDITOR && headType == ASSETS_HEAD){
			HashMap<String, String> mp = AUDITOR_ASCOA.get(userid);
			if(mp != null)
				ret = mp.get(ASSET);
		}else if(ruleType == AUDITOR && headType == LIABILITES_HEAD){
			HashMap<String, String> mp = AUDITOR_LICOA.get(userid);
			if(mp != null)
				ret = mp.get(LIAB);
		}
		return ret;
	}

	public static String getFromAmount(String userid, int ruleType, int headType){
		String ret = null;
		if(ruleType == CREATOR && headType == INCOME_HEAD) {
			HashMap<String, String> mp = CREATOR_INCOA.get(userid);
			if(mp != null)
				ret = mp.get(FROM_AMT);
		}else if(ruleType == CREATOR && headType == EXPENSE_HEAD) {
			HashMap<String, String> mp = CREATOR_EXCOA.get(userid);
			if(mp != null)
				ret = mp.get(FROM_AMT);
		}else if(ruleType == CREATOR && headType == ASSETS_HEAD) {
			HashMap<String, String> mp = CREATOR_ASCOA.get(userid);
			if(mp != null)
				ret = mp.get(FROM_AMT);
		}else if(ruleType == CREATOR && headType == LIABILITES_HEAD) {
			HashMap<String, String> mp = CREATOR_LICOA.get(userid);
			if(mp != null)
				ret = mp.get(FROM_AMT);
		}else if(ruleType == APPROVER && headType == INCOME_HEAD) {
			HashMap<String, String> mp = APPROVER_INCOA.get(userid);
			if(mp != null)
				ret = mp.get(FROM_AMT);
		}else if(ruleType == APPROVER && headType == EXPENSE_HEAD) {
			HashMap<String, String> mp = APPROVER_EXCOA.get(userid);
			if(mp != null)
				ret = mp.get(FROM_AMT);
		}else if(ruleType == APPROVER && headType == ASSETS_HEAD) {
			HashMap<String, String> mp = APPROVER_ASCOA.get(userid);
			if(mp != null)
				ret = mp.get(FROM_AMT);
		}else if(ruleType == APPROVER && headType == LIABILITES_HEAD) {
			HashMap<String, String> mp = APPROVER_LICOA.get(userid);
			if(mp != null)
				ret = mp.get(FROM_AMT);
		}
		return ret;
	}
	public static String getToAmount(String userid, int ruleType, int headType){
		String ret = null;
		if(ruleType == CREATOR && headType == INCOME_HEAD) {
			HashMap<String, String> mp = CREATOR_INCOA.get(userid);
			if(mp != null)
				ret = mp.get(TO_AMT);
		}else if(ruleType == CREATOR && headType == EXPENSE_HEAD) {
			HashMap<String, String> mp = CREATOR_EXCOA.get(userid);
			if(mp != null)
				ret = mp.get(TO_AMT);
		}else if(ruleType == CREATOR && headType == ASSETS_HEAD) {
			HashMap<String, String> mp = CREATOR_ASCOA.get(userid);
			if(mp != null)
				ret = mp.get(TO_AMT);
		}else if(ruleType == CREATOR && headType == LIABILITES_HEAD) {
			HashMap<String, String> mp = CREATOR_LICOA.get(userid);
			if(mp != null)
				ret = mp.get(TO_AMT);
		}else if(ruleType == APPROVER && headType == INCOME_HEAD) {
			HashMap<String, String> mp = APPROVER_INCOA.get(userid);
			if(mp != null)
				ret = mp.get(TO_AMT);
		}else if(ruleType == APPROVER && headType == EXPENSE_HEAD) {
			HashMap<String, String> mp = APPROVER_EXCOA.get(userid);
			if(mp != null)
				ret = mp.get(TO_AMT);
		}else if(ruleType == APPROVER && headType == ASSETS_HEAD) {
			HashMap<String, String> mp = APPROVER_ASCOA.get(userid);
			if(mp != null)
				ret = mp.get(TO_AMT);
		}else if(ruleType == APPROVER && headType == LIABILITES_HEAD) {
			HashMap<String, String> mp = APPROVER_LICOA.get(userid);
			if(mp != null)
				ret = mp.get(TO_AMT);
		}
		return ret;
	}
	public static boolean putCOA(String userid, String newCoaList, String newFromAmt, String newToAmt, int ruleType, int headType) {
		boolean ret = true;
		if(ruleType == CREATOR && headType == INCOME_HEAD) {
			HashMap<String, String> mp = CREATOR_INCOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(INCOME, newCoaList);
			mp.put(FROM_AMT, newFromAmt);
			mp.put(TO_AMT, newToAmt);
			CREATOR_INCOA.put(userid, mp);
		}else if(ruleType == CREATOR && headType == EXPENSE_HEAD) {
			HashMap<String, String> mp = CREATOR_EXCOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(EXPENSE, newCoaList);
			mp.put(FROM_AMT, newFromAmt);
			mp.put(TO_AMT, newToAmt);
			CREATOR_EXCOA.put(userid, mp);
		}else if(ruleType == CREATOR && headType == ASSETS_HEAD) {
			HashMap<String, String> mp = CREATOR_ASCOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(ASSET, newCoaList);
			mp.put(FROM_AMT, newFromAmt);
			mp.put(TO_AMT, newToAmt);
			CREATOR_ASCOA.put(userid, mp);
		}else if(ruleType == CREATOR && headType == LIABILITES_HEAD) {
			HashMap<String, String> mp = CREATOR_LICOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(LIAB, newCoaList);
			mp.put(FROM_AMT, newFromAmt);
			mp.put(TO_AMT, newToAmt);
			CREATOR_LICOA.put(userid, mp);
		}else if(ruleType == APPROVER && headType == INCOME_HEAD) {
			HashMap<String, String> mp = APPROVER_INCOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(INCOME, newCoaList);
			mp.put(FROM_AMT, newFromAmt);
			mp.put(TO_AMT, newToAmt);
			APPROVER_INCOA.put(userid, mp);
		}else if(ruleType == APPROVER && headType == EXPENSE_HEAD) {
			HashMap<String, String> mp = APPROVER_EXCOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(EXPENSE, newCoaList);
			mp.put(FROM_AMT, newFromAmt);
			mp.put(TO_AMT, newToAmt);
			APPROVER_EXCOA.put(userid, mp);
		}else if(ruleType == APPROVER && headType == ASSETS_HEAD) {
			HashMap<String, String> mp = APPROVER_ASCOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(ASSET, newCoaList);
			mp.put(FROM_AMT, newFromAmt);
			mp.put(TO_AMT, newToAmt);
			APPROVER_ASCOA.put(userid, mp);
		}else if(ruleType == APPROVER && headType == LIABILITES_HEAD) {
			HashMap<String, String> mp = APPROVER_LICOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(LIAB, newCoaList);
			mp.put(FROM_AMT, newFromAmt);
			mp.put(TO_AMT, newToAmt);
			APPROVER_LICOA.put(userid, mp);
		}else if(ruleType == AUDITOR && headType == INCOME_HEAD) {
			HashMap<String, String> mp = AUDITOR_INCOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(INCOME, newCoaList);
			AUDITOR_INCOA.put(userid, mp);
		}else if(ruleType == AUDITOR && headType == EXPENSE_HEAD) {
			HashMap<String, String> mp = AUDITOR_EXCOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(EXPENSE, newCoaList);
			AUDITOR_EXCOA.put(userid, mp);
		}else if(ruleType == AUDITOR && headType == ASSETS_HEAD) {
			HashMap<String, String> mp = AUDITOR_ASCOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(ASSET, newCoaList);
			AUDITOR_ASCOA.put(userid, mp);
		}else if(ruleType == AUDITOR && headType == LIABILITES_HEAD) {
			HashMap<String, String> mp = AUDITOR_LICOA.get(userid);
			if (mp == null) {
				mp = new HashMap<String, String>(1);
			}else{
				mp.clear();
			}
			mp.put(LIAB, newCoaList);
			AUDITOR_LICOA.put(userid, mp);
		}
		return ret;
	}

	public static boolean isCratorCOAPresent(String userid){
		return  CREATOR_INCOA.containsKey(userid);
	}

	public static String getCOAList(String email, int ruleType) {
		String coaList = "";
		String creatCoa1 = UserTxnRuleSetupCache.getCOA(email, ruleType, UserTxnRuleSetupCache.INCOME_HEAD);
		String creatCoa2 = UserTxnRuleSetupCache.getCOA(email, ruleType, UserTxnRuleSetupCache.EXPENSE_HEAD);
		String creatCoa3 = UserTxnRuleSetupCache.getCOA(email, ruleType, UserTxnRuleSetupCache.ASSETS_HEAD);
		String creatCoa4 = UserTxnRuleSetupCache.getCOA(email, ruleType, UserTxnRuleSetupCache.LIABILITES_HEAD);

		if(creatCoa1 != null){
			coaList = creatCoa1;
		}
		if(creatCoa2 != null){
			coaList += "," + creatCoa2;
		}
		if(creatCoa3 != null){
			coaList += "," +  creatCoa3;
		}
		if(creatCoa4 != null){
			coaList += "," +  creatCoa4;
		}
		return coaList;
	}

	public static String getFromAmountList(String email, int ruleType) {
		String fromAmtList = "";
		String creatFrom1 = UserTxnRuleSetupCache.getFromAmount(email, ruleType, UserTxnRuleSetupCache.INCOME_HEAD);
		String creatFrom2 = UserTxnRuleSetupCache.getFromAmount(email, ruleType, UserTxnRuleSetupCache.EXPENSE_HEAD);
		String creatFrom3 = UserTxnRuleSetupCache.getFromAmount(email, ruleType, UserTxnRuleSetupCache.ASSETS_HEAD);
		String creatFrom4 = UserTxnRuleSetupCache.getFromAmount(email, ruleType, UserTxnRuleSetupCache.LIABILITES_HEAD);
		if(creatFrom1 != null){
			fromAmtList = creatFrom1;
		}
		if(creatFrom2 != null){
			fromAmtList += "," + creatFrom2;
		}
		if(creatFrom3 != null){
			fromAmtList += "," +  creatFrom3;
		}
		if(creatFrom4 != null){
			fromAmtList += "," +  creatFrom4;
		}
		return fromAmtList;
	}
	public static String getToAmountList(String email, int ruleType) {
		String toAmountList = "";
		String creatTo1 = UserTxnRuleSetupCache.getToAmount(email, ruleType, UserTxnRuleSetupCache.INCOME_HEAD);
		String creatTo2 = UserTxnRuleSetupCache.getToAmount(email, ruleType, UserTxnRuleSetupCache.EXPENSE_HEAD);
		String creatTo3 = UserTxnRuleSetupCache.getToAmount(email, ruleType, UserTxnRuleSetupCache.ASSETS_HEAD);
		String creatTo4 = UserTxnRuleSetupCache.getToAmount(email, ruleType, UserTxnRuleSetupCache.LIABILITES_HEAD);
		if(creatTo1 != null){
			toAmountList = creatTo1;
		}
		if(creatTo2 != null){
			toAmountList += "," + creatTo2;
		}
		if(creatTo3 != null){
			toAmountList += "," +  creatTo3;
		}
		if(creatTo4 != null){
			toAmountList += "," +  creatTo4;
		}
		return toAmountList;
	}

	public static boolean remove(String userid){
		CREATOR_INCOA.remove(userid);
		CREATOR_EXCOA.remove(userid);
		CREATOR_ASCOA.remove(userid);
		CREATOR_LICOA.remove(userid);
		return true;
	}
}
