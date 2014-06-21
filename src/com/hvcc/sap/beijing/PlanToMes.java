/**
 * 
 */
package com.hvcc.sap.beijing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.hvcc.sap.MesUpdater;
import com.hvcc.sap.RfcSearcher;
import com.hvcc.sap.util.Utils;

/**
 * SAP에서 MES로 주간생산계획 데이터를 보내기
 * 
 * @author Shortstop
 */
public class PlanToMes {
	
	private static final Logger LOGGER = Logger.getLogger(PlanToMes.class.getName());
	public static final String INSERT_SQL = "INSERT INTO INF_SAP_PLAN(IFSEQ,WERKS,ARBPL,EQUNR,MATNR,KUNNR,CHARG,DISPD,ZSEQ1,ZSHIFT1,ZSEQ2,ZSHIFT2,ZSEQ3,ZSHIFT3,MEINS,ERDAT,ERZET,ERNAM,AEDAT,AEZET,AENAM,IFRESULT,IFFMSG,MES_STAT,MES_ISTDT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
	public static final String RFC_FUNC_NAME = "ZPPG_EA_PROD_PLANNING";
	public static final String RFC_OUT_TABLE = "ET_PLAN";
	
	/**
	 * call rfc 
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> callRfc(String ivCheck, String fromDateStr, String toDateStr) throws Exception {
		Map<String, Object> inputParams = new HashMap<String, Object>();
		inputParams.put("IV_WERKS", "CN10");
		inputParams.put("IV_FDATE", fromDateStr);
		inputParams.put("IV_TDATE", toDateStr);
		// 처음 요청일 경우 blank (성공한 건 안 내려감), 재전송 요청일 경우 'X' (무조건 모두 내림)
		inputParams.put("IV_CHECK", ivCheck);

		List<String> outputParams = new ArrayList<String>();
		outputParams.add("EV_IFRESULT");
		outputParams.add("EV_IFMSG");
		
		LOGGER.info("RFC [" + RFC_FUNC_NAME + "] Call!");
		Map<String, Object> output = new RfcSearcher().callFunction(RFC_FUNC_NAME, inputParams, outputParams, RFC_OUT_TABLE);
		return output;
	}
	
	/**
	 * Insert To Mes
	 * 
	 * @param results
	 * @throws Exception
	 */
	public int insertToMes(List<Map<String, Object>> results) throws Exception {
		
		if(results == null || results.isEmpty())
			return 0;
		
		List<List<Object>> parameters = new ArrayList<List<Object>>();
		int resultCnt = results.size();
		
		for(int i = 0 ; i < resultCnt ; i++) {
			Map<String, Object> record = (Map<String, Object>)results.get(i);
			LOGGER.info(Utils.mapToStr(record));
			List<Object> parameter = new ArrayList<Object>();
			// IFSEQ,WERKS,ARBPL,EQUNR,MATNR,KUNNR,CHARG,DISPD,ZSEQ1,ZSHIFT1,ZSEQ2,ZSHIFT2,ZSEQ3,ZSHIFT3,MEINS,
			// ERDAT,ERZET,ERNAM,AEDAT,AEZET,AENAM,IFRESULT,IFFMSG,MES_STAT,MES_ISTDT
			parameter.add(record.get("IFSEQ"));
			parameter.add(record.get("WERKS"));
			parameter.add(record.get("ARBPL"));
			parameter.add((record.get("EQUNR") == null || record.get("EQUNR").toString().equals("")) ? "EMPTY" : record.get("EQUNR"));
			parameter.add(record.get("MATNR"));
			parameter.add(record.get("KUNNR"));
			parameter.add((record.get("CHARG") == null || record.get("CHARG").toString().equals("")) ? "_" : record.get("CHARG"));
			parameter.add(record.get("DISPD"));
			parameter.add(record.get("ZSEQ1"));
			parameter.add(record.get("ZSHIFT1"));
			parameter.add(record.get("ZSEQ2"));
			parameter.add(record.get("ZSHIFT2"));
			parameter.add(record.get("ZSEQ3"));
			parameter.add(record.get("ZSHIFT3"));
			parameter.add(record.get("MEINS"));
			parameter.add(record.get("ERDAT"));
			parameter.add(record.get("ERZET"));
			parameter.add(record.get("ERNAM"));
			parameter.add(record.get("AEDAT"));
			parameter.add(record.get("AEZET"));
			parameter.add(record.get("AENAM"));
			//parameter.add(record.get("IFRESULT"));
			parameter.add("Y");
			parameter.add(record.get("IFMSG"));
			parameter.add("N");
			parameters.add(parameter);
		}

		return new MesUpdater().update(INSERT_SQL, parameters);
	}
	
	/**
	 * Process Plan Data
	 * 
	 * @param results
	 * @return
	 * @throws Exception
	 */
	private int processPlanData(List<Map<String, Object>> results) throws Exception {
		
		if(results == null || results.isEmpty())
			return 0;
		
		Map<String, Object> groupByData = Utils.groupByIfseq(results);
		Iterator iter = groupByData.keySet().iterator();
		int updCnt = 0;
		
		while(iter.hasNext()) {
			String ifseq = (String)iter.next();
			List list = (List)groupByData.get(ifseq);
			
			try {
				int cnt = this.insertToMes(list);
				updCnt = updCnt + cnt;
			} catch (Throwable th) {
				LOGGER.warning("Failed to insert plans to MES IFSEQ (" + ifseq + "), Error : " + th.getMessage());
			}
		}
		
		return updCnt;
	}
	
	/**
	 * @param ivCheck
	 * @param fromDateStr
	 * @param toDateStr
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void execute(String ivCheck, String fromDateStr, String toDateStr) {
		Map<String, Object> output = null;
		int resultCount = 0;

		try {
			output = this.callRfc(ivCheck, fromDateStr, toDateStr);			
		} catch (Exception e) {
			LOGGER.severe("Failed to get Plans From SAP! " + e.getMessage());
		}
		
		if(output != null) {
			if(output.containsKey("EV_IFRESULT")) {
				String ifResult = (String)output.get("EV_IFRESULT");
				String ifMsg = (String)output.get("EV_IFMSG");
				
				if("S".equals(ifResult)) {
					List<Map<String, Object>> results = (List<Map<String, Object>>)output.get(RFC_OUT_TABLE);
					
					if(results == null || results.isEmpty()) {
						LOGGER.info("Got 0 Plans From SAP!");
					} else {
						try {
							resultCount = this.processPlanData(results);
							LOGGER.info("Inserted (" + resultCount + ") Plans From SAP!");
						} catch (Throwable th) {
							LOGGER.severe("Failed to insert data to MES : " + th.getMessage());
						}
					}
				} else {
					LOGGER.info("Failed to get Plans From SAP! Error : " + ifMsg);
				}				
			}
		}
	}
}
