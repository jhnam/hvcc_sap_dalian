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
 * Material Moving Interface MES To SAP
 *  
 * @author Shortstop
 */
public class MoveToSap {
	
	private static final Logger LOGGER = Logger.getLogger(ActualToSap.class.getName());
	public static final String RFC_FUNC_NAME = "ZMMG_EA_MES_MVT";
	
	/**
	 * call rfc 
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> callRfc(Map<String, Object> inputParams) throws Exception {
		List<String> outputParams = new ArrayList<String>();
		outputParams.add("EV_RESULT");
		outputParams.add("EV_MESSAGE");
		
		LOGGER.info("RFC [" + RFC_FUNC_NAME + "] Call!");
		Map<String, Object> output = new RfcInvoker().callFunction(RFC_FUNC_NAME, "CS_MOVE", inputParams, outputParams);
		return output;
	}
	
	/**
	 * Select from MES Actual Table
	 * 
	 * @throws Exception
	 */
	public List<Map<String, Object>> selectActuals() throws Exception {		
		String sql = "SELECT * FROM (SELECT IFSEQ, WERKS, MATNR, MENGE, ARBPL, BUDAT, MJAHR, MBLNR, VAART, LABEL FROM INF_SAP_MOVE WHERE IFRESULT = 'N' ORDER BY MES_ISTDT ASC) WHERE ROWNUM <= 50"; 
		return new MesSearcher().search(sql);
	}
	
	/**
	 * Update status flag MES Scrap table
	 * 
	 * @param mesId
	 * @param status
	 * @param msg
	 * @return 
	 * @throws Exception
	 */
	public boolean updateStatus(String mesId, String status, String msg) throws Exception {
		String preparedSql = "UPDATE INF_SAP_MOVE SET IFRESULT = ?, IFMESSAGE = ? WHERE IFSEQ = ?";
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
		List<Map<String, Object>> actuals = null;
		try {
			actuals = this.selectActuals();
		} catch (Throwable th) {
			LOGGER.severe(th.getMessage());
			return;
		}			
			
		if(actuals != null && !actuals.isEmpty()) {
			int actualCount = actuals.size();
				
			for(int i = 0 ; i < actualCount ; i++) {
				Map<String, Object> inputParam = actuals.get(i);
				String mesId = (String)inputParam.remove("IFSEQ");
				Map<String, Object> output = this.executeRecord(mesId, inputParam);
					
				if(output != null) {
					String evResult = (String)output.get("EV_RESULT");
						
					if(evResult != null && evResult.equalsIgnoreCase("S")) {
						LOGGER.info("Move Result : " + evResult);
					} else {
						String evMsg = (String)output.get("EV_MESSAGE");
						LOGGER.info("Actual Error Message : " + evMsg);
						
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
			LOGGER.info("No move data to interface!");
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
