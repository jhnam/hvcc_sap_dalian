package com.hvcc.sap.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utility
 * 
 * @author Shortstop
 */
public class Utils {

	@SuppressWarnings("rawtypes")
	public static String mapToStr(Map map) {
		if(map == null || map.isEmpty())
			return "";
		
		StringBuffer buf = new StringBuffer();
		Iterator iter = map.keySet().iterator();
		
		while(iter.hasNext()) {
			String key = (String)iter.next();
			String value = (map.get(key) == null ? "" : map.get(key).toString());
			buf.append(key);
			buf.append(" : ");
			buf.append(value);
			buf.append(", ");
		}
		
		return buf.toString();
	}
	
	/**
	 * obj가 빈 문자열인지 체크 
	 * 
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
		return (obj == null || obj.toString().equals("")) ? true : false; 
	}
	
	/**
	 * SAP에서 받아온 데이터를 IFSEQ별로 그루핑한다.
	 * 
	 * @param results
	 * @return
	 */
	public static Map<String, Object> groupByIfseq(List<Map<String, Object>> results) {
		
		Map<String, Object> groupBySeq = new HashMap<String, Object>();
		int resultCnt = results.size();
		
		for(int i = 0 ; i < resultCnt ; i++) {
			Map<String, Object> record = (Map<String, Object>)results.get(i);
			String ifseq = (String)record.get("IFSEQ");
			List<Object> seqList = null;
			
			if(groupBySeq.containsKey(ifseq)) {
				seqList = (List<Object>)groupBySeq.get(ifseq);
			} else {
				seqList = new ArrayList<Object>();
				groupBySeq.put(ifseq, seqList);
			}
			
			seqList.add(record);
		}
		
		return groupBySeq;
	}
}
