package com.idos.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SortMap {
	
	public static Map<String, Object> sortMap(Map<String, Object> b,String sortBy,String order){
		List<Map.Entry> a = new ArrayList<Map.Entry>(b.entrySet());
		if(sortBy.equals("value")){
			if(order.equals("asc")){
				Collections.sort(a,new Comparator() {
				   public int compare(Object o1, Object o2) {
				       Map.Entry e1 = (Map.Entry) o1;
				       Map.Entry e2 = (Map.Entry) o2;
				       return ((Comparable) e1.getValue()).compareTo(e2.getValue());
				   }
				});
			}
			if(order.equals("desc")){
				Collections.sort(a,new Comparator() {
					   public int compare(Object o1, Object o2) {
					       Map.Entry e1 = (Map.Entry) o1;
					       Map.Entry e2 = (Map.Entry) o2;
					       return ((Comparable) e2.getValue()).compareTo(e1.getValue());
					   }
					});
			}
		}
		if(sortBy.equals("key")){
			Map<String, Object> treeMap = new TreeMap<String, Object>(b);
		}
		return b;
	}
}
