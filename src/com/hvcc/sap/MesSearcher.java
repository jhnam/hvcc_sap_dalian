/**
 * 
 */
package com.hvcc.sap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MES Searcher
 * 
 * @author Shortstop
 */
public class MesSearcher {

	/**
	 * sql 문을 쿼리하여 리스트 형태로 리턴한다.
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> search(String sql) throws Exception {
		Connection conn = MesConnectionFactory.getInstance().getConnection();
		List<Map<String, Object>> outList = new ArrayList<Map<String, Object>>();
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCnt = rsmd.getColumnCount();
			
			while(rs.next()) {
				Map<String, Object> record = new HashMap<String, Object>();
				for(int i = 1 ; i <= columnCnt ; i++) {
					String column = rsmd.getColumnName(i);
					Object value = rs.getObject(column);
					record.put(column, value);
				}
				
				outList.add(record);
			}
		} catch (Exception e) {
			throw e;
			
		} finally {
			if(rs != null) {
				rs.close();
			}
			
			if(stmt != null) {
				stmt.close();
			}
		}
		
		return outList;
	}
}
