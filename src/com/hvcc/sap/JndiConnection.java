/**
 * 
 */
package com.hvcc.sap;

import java.sql.Connection;

import javax.sql.DataSource;
import javax.naming.InitialContext;

/**
 * MES JDBC Connection
 * 
 * @author Shortstop
 */
public class JndiConnection implements IMesConnection {
	
	/**
	 * JBOSS
	 */
	//public static final String JNDI_NAME = "java:jboss/datasources/mes";
	/**
	 * Tomcat
	 */
	public static final String JNDI_NAME = "java:comp/env/jdbc/oracle";

	@Override
	public Connection getConnection() throws Exception {
		DataSource dataSource = (DataSource) new InitialContext().lookup(JNDI_NAME);
		Connection conn = dataSource.getConnection();
		return conn;
	}
}
