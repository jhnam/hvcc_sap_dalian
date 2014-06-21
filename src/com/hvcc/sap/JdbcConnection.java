/**
 * 
 */
package com.hvcc.sap;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * JDBC Connection
 * 
 * @author Shortstop
 */
public class JdbcConnection implements IMesConnection {
	
	@Override
	public Connection getConnection() throws Exception {
		Class.forName(Constants.DB_DRIVER_CLASS).newInstance();
		Connection conn = DriverManager.getConnection(Constants.DB_URL, Constants.DB_USER, Constants.DB_PASSWORD);
		return conn;
	}

}
