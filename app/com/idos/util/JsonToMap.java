package com.idos.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.jetty.util.log.Log;

public class JsonToMap {
	
	protected static Logger log = Logger.getLogger("utility");
	
	@SuppressWarnings("unused")
	public static Map<String, Object> toJavaMap(JsonNode o, Map<String, Object> b) {
		try {
		  Iterator<Entry<String, JsonNode>> ji = o.fields();
		  while (ji.hasNext()) {
			Entry<String, JsonNode> entry = ji.next();
			String key=entry.getKey();
			Object val = entry.getValue();
		    if (val.getClass()== ObjectNode.class) {
		      Map<String, Object> sub = new HashMap<String, Object>();
		      toJavaMap((JsonNode) val, sub);
		      b.put(key, sub);
		    } else {
		      b.put(key, val);
		    }
		  }
		}catch(Exception e) {
			e.printStackTrace();
	    }
		return b;
    }
	
	
	public static Map<String,String> getSigleMap(Map<String, Object> b){
		Map<String,String> jsonMap=new HashMap<String,String>();
		Object[] keyArray = b.keySet().toArray();
		for (int i = 0; i < keyArray.length; i++) {
			Object map=b.get(keyArray[i]);
			if(map instanceof HashMap){
				Object[] keyArray1 = ((HashMap) map).keySet().toArray();
				for(int j=0;j<keyArray1.length;j++){
					jsonMap.put(keyArray1[j].toString(), ((HashMap) map).get(keyArray1[j]).toString());
				}
			}else{
				jsonMap.put(keyArray[i].toString(), map.toString());
			}
		}
		return jsonMap;
	}
}
