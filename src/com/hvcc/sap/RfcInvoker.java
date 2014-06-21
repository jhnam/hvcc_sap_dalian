/**
 * 
 */
package com.hvcc.sap;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.Structure;

/**
 * RFC 호출하여 데이터를 SAP에 넘긴다.
 * 
 * @author Shortstop
 */
public class RfcInvoker {
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
	public Map<String, Object> callFunction(String funcName, String structureName, Map<String, Object> inputParams, List<String> outputParams) 
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

			JCO.ParameterList input = function.getImportParameterList();
			if(inputParams != null && !inputParams.isEmpty()) {
				Structure st = input.getStructure(structureName);
				Iterator<String> keyIter = inputParams.keySet().iterator();
				while(keyIter.hasNext()) {
					String key = keyIter.next();
					Object value = inputParams.get(key);
					st.setValue(value, key);
				}
				
				input.setValue(st, structureName);
			}

			connection.execute(function);
			
			// output parameters 처리 
			if(outputParams != null && !outputParams.isEmpty()) {
				JCO.ParameterList outputs = function.getExportParameterList();
				Iterator<String> outputParamIter = outputParams.iterator();
				while(outputParamIter.hasNext()) {
					String outputName = outputParamIter.next();
					retVal.put(outputName, outputs.getValue(outputName));
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
