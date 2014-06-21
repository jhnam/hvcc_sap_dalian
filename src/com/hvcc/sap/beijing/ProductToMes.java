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
 * Product, BOM, Batch 정보를  SAP에서 가져와서 MES 인터페이스 테이블에 추가한다.
 * 
 * @author Shortstop
 */
public class ProductToMes {
	
	private static final Logger LOGGER = Logger.getLogger(ProductToMes.class.getName());
	public static final String INSERT_PRODUCT_SQL = "INSERT INTO INF_SAP_PRODUCT(IFSEQ, WERKS, MATNR, MAKTX, MTART, MEINS, MATKL, BESKZ, SOBSL, MMSTA, BSTMA, MPQNT, ERDAT, ERZET, ERNAM, AEDAT, AEZET, AENAM, IFRESULT, IFFMSG, MES_STAT, MES_ISTDT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
	public static final String INSERT_BOM_SQL = "INSERT INTO INF_SAP_BOM(IFSEQ, WERKS, MATNR, STLAN, STLAL, IDNRK, MENGE, MEINS, DATUV, DATUB, ERDAT, ERZET, ERNAM, AEDAT, AEZET, AENAM, IFRESULT, IFFMSG, MES_STAT, MES_ISTDT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
	public static final String INSERT_BATCH_SQL = "INSERT INTO INF_SAP_BATCH(IFSEQ,WERKS,MATNR,CHARG,NAME1,LVORM,ERDAT,ERZET,ERNAM,AEDAT,AEZET,AENAM,IFRESULT,IFFMSG,MES_STAT,MES_ISTDT) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE)";
	public static final String RFC_FUNC_NAME = "ZPPG_EA_MAT_BOM_MASTER";
	public static final String RFC_OUT_TABLE1 = "ET_MAT";
	public static final String RFC_OUT_TABLE2 = "ET_BOM";
	public static final String RFC_OUT_TABLE3 = "ET_BAT";
	
	/**
	 * RFC function call
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
		
		String[] outTables = new String[3];
		outTables[0] = RFC_OUT_TABLE1;
		outTables[1] = RFC_OUT_TABLE2;
		outTables[2] = RFC_OUT_TABLE3;
		Map<String, Object> output = new RfcSearcher().callFunction(RFC_FUNC_NAME, inputParams, outputParams, outTables);
		return output;
	}
	
	/**
	 * product data 처리
	 * 
	 * @param results
	 * @return
	 * @throws Exception
	 */
	private int processProductsData(List<Map<String, Object>> results) throws Exception {
		if(results == null || results.isEmpty())
			return 0;
		
		Map<String, Object> groupByData = Utils.groupByIfseq(results);
		Iterator iter = groupByData.keySet().iterator();
		int updCnt = 0;
		
		while(iter.hasNext()) {
			String ifseq = (String)iter.next();
			List list = (List)groupByData.get(ifseq);
			
			try {
				int cnt = this.createProducts(list);
				updCnt = updCnt + cnt;
			} catch (Throwable th) {
				LOGGER.warning("Failed to insert Products to MES IFSEQ (" + ifseq + "), Error : " + th.getMessage());
			}
		}
		
		return updCnt;
	}	
	
	/**
	 * create product data
	 * 
	 * @param results
	 * @throws Exception
	 */
	public int createProducts(List<Map<String, Object>> results) throws Exception {
		
		if(results == null || results.isEmpty()) 
			return 0;
		
		List<List<Object>> parameters = new ArrayList<List<Object>>();
		int resultCnt = results.size();
		
		for(int i = 0 ; i < resultCnt ; i++) {
			Map<String, Object> record = (Map<String, Object>)results.get(i);
			LOGGER.info(Utils.mapToStr(record));
			List<Object> parameter = new ArrayList<Object>();
			parameter.add(record.get("IFSEQ"));
			parameter.add(record.get("WERKS"));
			parameter.add(record.get("MATNR"));
			parameter.add(record.get("MAKTX"));
			parameter.add(record.get("MTART"));
			parameter.add(record.get("MEINS"));
			parameter.add(record.get("MATKL"));
			parameter.add(record.get("BESKZ"));
			parameter.add(record.get("SOBSL"));
			parameter.add(record.get("MMSTA"));
			parameter.add(record.get("BSTMA"));
			parameter.add(record.get("MPQNT"));
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

		return new MesUpdater().update(INSERT_PRODUCT_SQL, parameters);
	}
	
	/**
	 * bom data 처리
	 * 
	 * @param results
	 * @return
	 * @throws Exception
	 */
	private int processBomData(List<Map<String, Object>> results) throws Exception {
		if(results == null || results.isEmpty())
			return 0;
		
		Map<String, Object> groupByData = Utils.groupByIfseq(results);
		Iterator iter = groupByData.keySet().iterator();
		int updCnt = 0;
		
		while(iter.hasNext()) {
			String ifseq = (String)iter.next();
			List list = (List)groupByData.get(ifseq);
			
			try {
				int cnt = this.createBom(list);
				updCnt = updCnt + cnt;
			} catch (Throwable th) {
				LOGGER.warning("Failed to insert BOM to MES IFSEQ (" + ifseq + "), Error : " + th.getMessage());
			}
		}
		
		return updCnt;
	}
	
	/**
	 * create bom data
	 * 
	 * @param results
	 * @throws Exception
	 */
	public int createBom(List<Map<String, Object>> results) throws Exception {
		
		if(results == null || results.isEmpty()) 
			return 0;
		
		List<List<Object>> parameters = new ArrayList<List<Object>>();
		int resultCnt = results.size();
		
		for(int i = 0 ; i < resultCnt ; i++) {
			Map<String, Object> record = (Map<String, Object>)results.get(i);
			LOGGER.info(Utils.mapToStr(record));
			List<Object> parameter = new ArrayList<Object>();
			parameter.add(record.get("IFSEQ"));
			parameter.add(record.get("WERKS"));
			parameter.add(record.get("MATNR"));
			parameter.add(record.get("STLAN"));
			parameter.add(record.get("STLAL"));
			parameter.add(record.get("IDNRK"));
			parameter.add(record.get("MENGE"));
			parameter.add(record.get("MEINS"));
			parameter.add(record.get("DATUV"));
			parameter.add(record.get("DATUB"));
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

		return new MesUpdater().update(INSERT_BOM_SQL, parameters);
	}
	
	/**
	 * batch data 처리
	 * 
	 * @param results
	 * @return
	 * @throws Exception
	 */
	private int processBatchesData(List<Map<String, Object>> results) throws Exception {
		if(results == null || results.isEmpty())
			return 0;
		
		Map<String, Object> groupByData = Utils.groupByIfseq(results);
		Iterator iter = groupByData.keySet().iterator();
		int updCnt = 0;
		
		while(iter.hasNext()) {
			String ifseq = (String)iter.next();
			List list = (List)groupByData.get(ifseq);
			
			try {
				int cnt = this.createBatches(list);
				updCnt = updCnt + cnt;
			} catch (Throwable th) {
				LOGGER.warning("Failed to insert Batch to MES IFSEQ (" + ifseq + "), Error : " + th.getMessage());
			}
		}
		
		return updCnt;
	}	
	
	/**
	 * create product data
	 * 
	 * @param results
	 * @throws Exception
	 */
	public int createBatches(List<Map<String, Object>> results) throws Exception {
		
		if(results == null || results.isEmpty()) 
			return 0;
		
		List<List<Object>> parameters = new ArrayList<List<Object>>();
		int resultCnt = results.size();
		
		for(int i = 0 ; i < resultCnt ; i++) {
			Map<String, Object> record = (Map<String, Object>)results.get(i);
			LOGGER.info(Utils.mapToStr(record));
			List<Object> parameter = new ArrayList<Object>();
			// IFSEQ,WERKS,MATNR,CHARG,NAME1,LVORM,ERDAT,ERZET,ERNAM,AEDAT,AEZET,AENAM,IFRESULT,IFFMSG,MES_STAT,MES_ISTDT
			parameter.add(record.get("IFSEQ"));
			parameter.add(record.get("WERKS"));
			parameter.add(record.get("MATNR"));
			parameter.add(record.get("CHARG"));
			parameter.add(record.get("NAME1"));
			parameter.add(record.get("LVORM"));
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

		return new MesUpdater().update(INSERT_BATCH_SQL, parameters);
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
		} catch (Throwable th) {
			LOGGER.info("Failed to get BOM From SAP. " + th.getMessage());
		}
		
		if(output != null) {
			String ifResult = (String)output.get("EV_IFRESULT");
			String ifMsg = (String)output.get("EV_IFMSG");
			
			if("S".equals(ifResult)) {
				try {
					List<Map<String, Object>> prdResults = (List<Map<String, Object>>)output.get(RFC_OUT_TABLE1);
					resultCount = this.processProductsData(prdResults);
					LOGGER.info("Inserted Product (" + resultCount + ") From SAP!");
				} catch (Throwable th) {
					LOGGER.severe("Failed to insert Product to MES, ERROR : " + th.getMessage());
				}
				
				try {
					List<Map<String, Object>> bomResults = (List<Map<String, Object>>)output.get(RFC_OUT_TABLE2);
					resultCount = this.processBomData(bomResults);
					LOGGER.info("Inserted BOM (" + resultCount + ") From SAP!");
				} catch (Throwable th) {
					LOGGER.severe("Failed to insert BOM to MES, ERROR : " + th.getMessage());
				}
				
				try {
					List<Map<String, Object>> batchResults = (List<Map<String, Object>>)output.get(RFC_OUT_TABLE3);
					resultCount = this.processBatchesData(batchResults);
					LOGGER.info("Inserted Batch (" + resultCount + ") From SAP!");
				} catch (Throwable th) {
					LOGGER.severe("Failed to insert Batch to MES, ERROR : " + th.getMessage());
				}				
				
			} else {
				LOGGER.info("Failed to get (" + RFC_FUNC_NAME + ") data from SAP, Error : " + ifMsg);
			}
		}		
	}

}
