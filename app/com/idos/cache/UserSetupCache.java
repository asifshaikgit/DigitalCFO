package com.idos.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sunil Namdev created on 20-01-2018
 */
public class UserSetupCache {
    private static ConcurrentHashMap<Long, List<Long>> CREATOR_COA = new ConcurrentHashMap<Long, List<Long>>();
    private static ConcurrentHashMap<Long, List<Long>> APPROVER_COA = new ConcurrentHashMap<Long, List<Long>>();
    private static ConcurrentHashMap<Long, List<Long>> AUDITOR_COA = new ConcurrentHashMap<Long, List<Long>>();

    public static List<Long> getCreatorCOA(Long userid){
        return CREATOR_COA.get(userid);
    }

    public static boolean addCratorCOA(Long userid, List <Long> newCoaList){
        boolean ret = true;
        List coaList = CREATOR_COA.get(userid);
        if(coaList == null){
            CREATOR_COA.put(userid, newCoaList);
        }else{
            ret = false;
        }
        return ret;
    }

    public static boolean isCratorCOAPresent(Long userid){
        boolean ret = CREATOR_COA.containsKey(userid);
        return ret;
    }

}
