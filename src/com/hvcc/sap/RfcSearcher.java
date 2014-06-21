/**
 * 
 */
package com.hvcc.sap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Field;

/**
 * RFC 호출하여 결과 값을 리턴 받는다.
 * 
 * @author Shortstop
 */
public class RfcSearcher {

	/**
	 * RFC 호출하여 리턴값을 가공하여 Map 형태로 리턴한다.
	 * 
	 * @param funcName RFC Function name
	 * @param inputParams RFC Input Parameter Names
	 * @param outputParams RFC Output Parameter Names
	 * @param outTableName RFC Output Table Name
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> callFunction(String funcName, Map<String, Object> inputParams, List<String> outputParams, String outTableName) 
	throws Exception {
		
		Map<String, Object> retVal = new HashMap<String, Object>();
		SapConnectionPool	sapPool		= null;
		JCO.Client			connection	= null;
		IRepository			repository	= null;
		JCO.Function		function	= null; 

		try {
			sapPool = SapConnectionPool.getInstance();
			connection = sapPool.getConnection();
			repository = new JCO.Repository(" ", connection);
			function = sapPool.createFunction(repository, funcName);

			if (function == null) {
				throw new Exception("RFC [" + funcName + "] not found in SAP");
			}

			// input parameters 처리
			this.rfcInputParams(function, inputParams);

			// rfc function call
			connection.execute(function);
			
			// output parameters 처리 
			this.rfcOutParams(function, outputParams, retVal);
			
			// output table 처리 
			this.rfcOutTable(function, outTableName, retVal);
			
		} catch (Throwable th) {
			throw new Exception(th);
			
		} finally {
			repository = null;
			if (connection != null) { 
				sapPool.releaseConnection(connection); 
			}
		}
		
		return retVal;
	}
	
	private void rfcInputParams(JCO.Function function, Map<String, Object> inputParams) {
		
		JCO.ParameterList input = function.getImportParameterList();
		if(inputParams != null && !inputParams.isEmpty()) {
			Iterator<String> inputNameIter = inputParams.keySet().iterator();
			while(inputNameIter.hasNext()) {
				String inputName = inputNameIter.next();
				Object inputValue = inputParams.get(inputName);
				input.setValue(inputValue, inputName);
			}
		}
	}
	
	private void rfcOutParams(JCO.Function function, List<String> outputParams, Map<String, Object> retVal) {
		
		if(outputParams != null && !outputParams.isEmpty()) {
			JCO.ParameterList outputs = function.getExportParameterList();
			Iterator<String> outputParamIter = outputParams.iterator();
			while(outputParamIter.hasNext()) {
				String outputName = outputParamIter.next();
				Object outputValue = outputs.getValue(outputName);
				retVal.put(outputName, outputValue);
			}
		}
	}
	
	private void rfcOutTable(JCO.Function function, String outTableName, Map<String, Object> retVal) {
		
		if(outTableName != null && outTableName.trim() != "") {
			JCO.Table outTable = function.getTableParameterList().getTable(outTableName);
			List<Map<String, Object>> outList = new ArrayList<Map<String, Object>>();
			retVal.put(outTableName, outList);
		
			int outRowCount = outTable.getNumRows();
			int outFieldCount = outTable.getFieldCount();
		
			for(int i = 0 ; i < outRowCount ; i++) {
				outTable.setRow(i);					
				Map<String, Object> record = new HashMap<String, Object>();
				
				for(int j = 0 ; j < outFieldCount ; j++) {
					Field field = outTable.getField(j);
					record.put(field.getName(), outTable.getString(field.getName()));
				}
				outList.add(record);
			}
		}
	}
	
	/**
	 * RFC 호출하여 리턴값을 가공하여 Map 형태로 리턴한다.
	 * 
	 * @param funcName RFC Function name
	 * @param inputParams RFC Input Parameter Names
	 * @param outputParams RFC Output Parameter Names
	 * @param outTableNames RFC Output Table Name
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> callFunction(String funcName, Map<String, Object> inputParams, List<String> outputParams, String[] outTableNames) 
	throws Exception {
		
		Map<String, Object> retVal = new HashMap<String, Object>();
		SapConnectionPool	sapPool		= null;
		JCO.Client			connection	= null;
		IRepository			repository	= null;
		JCO.Function		function	= null; 

		try {
			sapPool = SapConnectionPool.getInstance();
			connection = sapPool.getConnection();
			repository = new JCO.Repository(" ", connection);
			function = sapPool.createFunction(repository, funcName);

			if (function == null) {
				throw new Exception("RFC [" + funcName + "] not found in SAP");
			}

			// input parameters 처리
			this.rfcInputParams(function, inputParams);

			// rfc 호출
			connection.execute(function);
			
			// output parameters 처리 
			this.rfcOutParams(function, outputParams, retVal);
			
			// output table 처리 
			if(outTableNames != null && outTableNames.length > 0) {
				for(int i = 0 ; i < outTableNames.length ; i++) {
					this.rfcOutTable(function, outTableNames[i], retVal);
				}
			}			
		} catch (Throwable th) {
			throw new Exception(th);
			
		} finally {
			repository = null;
			if (connection != null) { 
				sapPool.releaseConnection(connection); 
			}	
		}
		
		return retVal;
	}
}
