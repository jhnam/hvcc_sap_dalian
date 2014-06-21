/**
 * 
 */
package com.hvcc.sap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

/**
 * MES Updater
 * 
 * @author Shortstop
 */
public class MesUpdater {

	/**
	 * insert or update list
	 * 
	 * @param preparedSql
	 * @param parameters
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int update(String preparedSql, List<List<Object>> parameters) throws Exception {
		Connection conn = MesConnectionFactory.getInstance().getConnection();
		boolean autoCommit = conn.getAutoCommit();
		PreparedStatement pstmt = null;
		int processedCount = 0;
		
	    try {
	        conn.setAutoCommit(false);
	        pstmt = conn.prepareStatement(preparedSql);
	        Iterator iter = parameters.iterator();
	        
	        while(iter.hasNext()) {
	        	List<Object> list = (List<Object>)iter.next();
	        	for(int i = 0 ; i < list.size() ; i++) {
	        		pstmt.setObject(i + 1, list.get(i));
	        	}
	        	processedCount += pstmt.executeUpdate();
	        }
	        
            conn.commit();
	    } catch (Exception e) {
	    	conn.rollback();
	    	throw e;
	    	
	    } finally {
	    	conn.setAutoCommit(autoCommit);
	    	
	    	if(pstmt != null) {
	    		pstmt.close();
	    	}
	    	
	    	if(conn != null) {
	    		conn.close();
	    	}
	    }
	    
	    return processedCount;
	}
	
	public boolean update(String sql) throws Exception {
		Connection conn = MesConnectionFactory.getInstance().getConnection();
		Statement stmt = null;
		boolean result = false;
		
	    try {
	        stmt = conn.createStatement();
	        result = stmt.execute(sql);
	        
	    } catch (Exception e) {
	    	throw e;
	    	
	    } finally {	    	
	    	if(stmt != null) {
	    		stmt.close();
	    	}
	    	
	    	if(conn != null) {
	    		conn.close();
	    	}
	    }
	    
	    return result;
	}
}
