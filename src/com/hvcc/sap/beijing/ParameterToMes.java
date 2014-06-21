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
 * SAP에서 MES로 Machine, Parameter 데이터를 보내기
 * 
 * @author Shortstop
 */
public class ParameterToMes {
	
	private static final Logger LOGGER = Logger.getLogger(ParameterToMes.class.getName());
	public static final String MC_INSERT_SQL = "INSERT INTO INF_SAP_EQUIPMENT(IFSEQ, WERKS, ZPTYP, ZDEPT, ARBPL, ZMACN, ZKEY, KTEXT, ZTEXT, ERDAT, ERZET, ERNAM, AEDAT, AEZET, AENAM, IFRESULT, IFFMSG, MES_STAT, MES_ISTDT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
	public static final String PARAM_INSERT_SQL = "INSERT INTO INF_SAP_PARAMETER(IFSEQ, WERKS, ZPTYP, ZDEPT, ARBPL, ZMACN, MATNR, VERID, ZMKEY, ZUPH, LOTQT, VGW01, MEINS, ERDAT, ERZET, ERNAM, AEDAT, AEZET, AENAM, IFRESULT, IFFMSG, MES_STAT, MES_ISTDT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
	public static final String RFC_FUNC_NAME = "ZPPG_EA_PLAN_PARAM";
	public static final String RFC_OUT_TABLE_1 = "ET_ORG"; 		// MACHINE
	public static final String RFC_OUT_TABLE_2 = "ET_PARAM"; 	// PARAMETER
	
	/**
	 * call rfc
	 * 
	 * @param ivCheck
	 * @param fromDateStr
	 * @param toDateStr
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> callRfc(String ivCheck, String fromDateStr, String toDateStr) throws Exception {
		Map<String, Object> inputParams = new HashMap<String, Object>();
		inputParams.put("IV_WERKS", "CN10");
		inputParams.put("IV_FDATE", fromDateStr);
		inputParams.put("IV_TDATE", toDateStr);
		inputParams.put("IV_CHECK", ivCheck);

		List<String> outputParams = new ArrayList<String>();
		outputParams.add("EV_IFRESULT");
		outputParams.add("EV_IFMSG");
		
		LOGGER.info("RFC [" + RFC_FUNC_NAME + "] Call!");
		
		String[] outTables = new String[2];
		outTables[0] = RFC_OUT_TABLE_1;
		outTables[1] = RFC_OUT_TABLE_2;
		
		Map<String, Object> output = new RfcSearcher().callFunction(RFC_FUNC_NAME, inputParams, outputParams, outTables);
		return output;
	}
	
	/**
	 * machine data 처리
	 * 
	 * @param results
	 * @return
	 * @throws Exception
	 */
	private int processMachinesData(List<Map<String, Object>> results) throws Exception {
		if(results == null || results.isEmpty())
			return 0;
		
		Map<String, Object> groupByData = Utils.groupByIfseq(results);
		Iterator iter = groupByData.keySet().iterator();
		int updCnt = 0;
		
		while(iter.hasNext()) {
			String ifseq = (String)iter.next();
			List list = (List)groupByData.get(ifseq);
			
			try {
				int cnt = this.createMachines(list);
				updCnt = updCnt + cnt;
			} catch (Throwable th) {
				LOGGER.warning("Failed to insert Machines to MES IFSEQ (" + ifseq + "), Error : " + th.getMessage());
			}
		}
		
		return updCnt;
	}
	
	/**
	 * insert machine data
	 * 
	 * @param results
	 * @throws Exception
	 */
	private int createMachines(List<Map<String, Object>> results) throws Exception {
		List<List<Object>> parameters = new ArrayList<List<Object>>();
		int resultCnt = results.size();
		
		for(int i = 0 ; i < resultCnt ; i++) {
			Map<String, Object> record = (Map<String, Object>)results.get(i);
			LOGGER.info(Utils.mapToStr(record));
			// IFSEQ, WERKS, ZPTYP, ZDEPT, ARBPL, ZMACN, ZKEY, KTEXT, ZTEXT, ERDAT, ERZET, ERNAM, AEDAT, AEZET, AENAM, IFRESULT, IFFMSG, MES_STAT, MES_ISTDT
			List<Object> parameter = new ArrayList<Object>();
			parameter.add(record.get("IFSEQ"));
			parameter.add(record.get("WERKS"));
			parameter.add(record.get("ZPTYP"));
			parameter.add(record.get("ZDEPT"));
			parameter.add(record.get("ARBPL"));
			parameter.add(record.get("ZMACN"));
			parameter.add(record.get("ZKEY"));
			parameter.add(record.get("KTEXT"));
			parameter.add(record.get("ZTEXT"));
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

		return new MesUpdater().update(MC_INSERT_SQL, parameters);
	}
	
	/**
	 * parameter data 처리
	 * 
	 * @param results
	 * @return
	 * @throws Exception
	 */
	private int processParamsData(List<Map<String, Object>> results) throws Exception {
		if(results == null || results.isEmpty())
			return 0;
		
		Map<String, Object> groupByData = Utils.groupByIfseq(results);
		Iterator iter = groupByData.keySet().iterator();
		int updCnt = 0;
		
		while(iter.hasNext()) {
			String ifseq = (String)iter.next();
			List list = (List)groupByData.get(ifseq);
			
			try {
				int cnt = this.createParams(list);
				updCnt = updCnt + cnt;
			} catch (Throwable th) {
				LOGGER.warning("Failed to insert Params to MES IFSEQ (" + ifseq + "), Error : " + th.getMessage());
			}
		}
		
		return updCnt;
	}	
	
	/**
	 * create params
	 * 
	 * @param results
	 * @throws Exception
	 */
	public int createParams(List<Map<String, Object>> results) throws Exception {
		
		if(results == null || results.isEmpty())
			return 0;
		
		List<List<Object>> parameters = new ArrayList<List<Object>>();
		int resultCnt = results.size();
		
		for(int i = 0 ; i < resultCnt ; i++) {
			Map<String, Object> record = (Map<String, Object>)results.get(i);
			LOGGER.info(Utils.mapToStr(record));
			// IFSEQ, WERKS, ZPTYP, ZDEPT, ARBPL, ZMACN, MATNR, VERID, ZMKEY, ZUPH, LOTQT, VGW01, MEINS, ERDAT, ERZET, ERNAM, AEDAT, AEZET, AENAM, IFRESULT, IFFMSG, MES_STAT, MES_ISTDT
			List<Object> parameter = new ArrayList<Object>();
			parameter.add(record.get("IFSEQ"));
			parameter.add(record.get("WERKS"));
			parameter.add(record.get("ZPTYP"));
			parameter.add(record.get("ZDEPT"));
			parameter.add(record.get("ARBPL"));
			parameter.add(record.get("ZMACN"));
			parameter.add(record.get("MATNR"));
			parameter.add(record.get("VERID"));
			parameter.add(record.get("ZMKEY"));
			parameter.add(record.get("ZUPH"));
			parameter.add(record.get("LOTQT"));
			parameter.add(record.get("VGW01"));
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

		return new MesUpdater().update(PARAM_INSERT_SQL, parameters);
	}	
	
	/**
	 * 실행
	 * 
	 * @param ivCheck
	 * @param fromDateStr
	 * @param toDateStr
	 */
	@SuppressWarnings("unchecked")
	public void execute(String ivCheck, String fromDateStr, String toDateStr) {
		Map<String, Object> output = null;
		int resultCount = 0;

		try {
			output = this.callRfc(ivCheck, fromDateStr, toDateStr);
		} catch (Exception e) {
			LOGGER.severe("Failed to call (" + RFC_FUNC_NAME + ") From SAP!" + e.getMessage());
		}
		
		if(output != null) {
			String ifResult = (String)output.get("EV_IFRESULT");
			String ifMsg = (String)output.get("EV_IFMSG");
			
			if("S".equals(ifResult)) {
				List<Map<String, Object>> mcResults = (List<Map<String, Object>>)output.get(RFC_OUT_TABLE_1);
				try {
					resultCount = this.processMachinesData(mcResults);
					LOGGER.info("Inserted Machines (" + resultCount + ") From SAP!");
				} catch (Throwable th) {
					LOGGER.severe("Failed to insert machines to MES, ERROR : " + th.getMessage());
				}
				
				List<Map<String, Object>> paramResults = (List<Map<String, Object>>)output.get(RFC_OUT_TABLE_2);
				try {
					resultCount = this.processParamsData(paramResults);
					LOGGER.info("Inserted Params (" + resultCount + ") From SAP!");
				} catch (Throwable th) {
					LOGGER.severe("Failed to insert parameters to MES, ERROR : " + th.getMessage());
				}
			} else {
				LOGGER.info("Failed to get (" + RFC_FUNC_NAME + ") Data From SAP, Error : " + ifMsg);
			}
		}		
	}
	
}
