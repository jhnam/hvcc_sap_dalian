/**
 * 
 */
package com.hvcc.sap.beijing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.hvcc.sap.MesSearcher;
import com.hvcc.sap.MesUpdater;
import com.hvcc.sap.RfcInvoker;
import com.hvcc.sap.util.Utils;

/**
 * Scrap Interface : MES --> SAP
 * 
 * @author Shortstop
 */
public class ScrapToSap {

	/**
	 * logger
	 */
	private static final Logger LOGGER = Logger.getLogger(ScrapToSap.class.getName());
	/**
	 * RFC Function Name
	 */
	public static final String RFC_FUNC_NAME = "ZPPG_EA_INLINE_SCRAP";
	
	/**
	 * call rfc 
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> callRfc(Map<String, Object> inputParams) throws Exception {
		List<String> outputParams = new ArrayList<String>();
		outputParams.add("EV_RESULT");
		outputParams.add("EV_MSG");
		outputParams.add("EV_IFSEQ");
		
		LOGGER.info("RFC [" + RFC_FUNC_NAME + "] Call!");
		Map<String, Object> output = new RfcInvoker().callFunction(RFC_FUNC_NAME, "IS_SCRAP", inputParams, outputParams);
		return output;
	}
	
	/**
	 * Select from MES Scrap Table
	 * 
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectScraps() throws Exception {
		String sql = "SELECT * FROM (SELECT MES_ID, IFSEQ, WERKS, ARBPL, EQUNR, LOGRP, VAART, ZVAART, MATNR, IDNRK, BUDAT, PDDAT, ERFMG, MEINS FROM INF_SAP_SCRAP WHERE IFRESULT = 'N' ORDER BY MES_ISTDT) WHERE ROWNUM <= 100";
		return new MesSearcher().search(sql);
	}
	
	/**
	 * Update status flag MES Scrap table
	 * 
	 * @param mesId
	 * @param status
	 * @return 
	 * @throws Exception
	 */
	public boolean updateStatus(String mesId, String status, String msg) throws Exception {
		String preparedSql = "UPDATE INF_SAP_SCRAP SET IFRESULT = ?, IFFMSG = ? WHERE MES_ID = ?";
		List parameters = new ArrayList();
		List param = new ArrayList();
		param.add(status);
		param.add(msg);
		param.add(mesId);
		parameters.add(param);
		int result = new MesUpdater().update(preparedSql, parameters);
		return result > 0;
	}
	
	/**
	 * 실행 
	 * 
	 * @throws Exception
	 */
	public void execute() {
		List<Map<String, Object>> scraps = null;
		try {
			scraps = this.selectScraps();
		} catch (Throwable th) {
			LOGGER.severe(th.getMessage());
			return;
		}			
			
		if(scraps != null && !scraps.isEmpty()) {
			int scrapCount = scraps.size();
				
			for(int i = 0 ; i < scrapCount ; i++) {
				Map<String, Object> inputParam = scraps.get(i);
				String mesId = (String)inputParam.remove("MES_ID");
				Map<String, Object> output = this.executeRecord(mesId, inputParam);
					
				if(output != null) {
					String ifseq = (String)output.get("EV_IFSEQ");
					String evResult = (String)output.get("EV_RESULT");
						
					if(evResult != null && evResult.equalsIgnoreCase("S")) {
						LOGGER.info("Scrap IFSEQ : " + ifseq);
					} else {
						String evMsg = (String)output.get("EV_MSG");
						LOGGER.info("Scrap Error Message : " + evMsg);
						
						if(evMsg.length() > 250) 
							evMsg = evMsg.substring(0, 250);
						
						try {
							this.updateStatus(mesId, evResult, evMsg);
						} catch (Throwable th) {
							LOGGER.severe("Failed to update status, Error : " + th.getMessage());
						}
					}
				}
			}
		} else {
			LOGGER.info("No scrap data to interface!");
		}
	}
	
	private Map<String, Object> executeRecord(String mesId, Map<String, Object> inputParam) {
		Utils.mapToStr(inputParam);
		Map<String, Object> output = null;
		
		try {
			output = this.callRfc(inputParam);
			this.updateStatus(mesId, "Y", null);
		} catch (Throwable th) {
			String msg = "Error - MES_ID : " + mesId + ", MSG : " + th.getMessage();
			LOGGER.severe(msg);
			
			if(msg.length() > 250) 
				msg = msg.substring(0, 250);
			
			try {
				this.updateStatus(mesId, "F", msg);
			} catch (Throwable e) {
				LOGGER.severe("Failed to update status, Error : " + e.getMessage());
			}		
		}
		
		return output;
	}
}
