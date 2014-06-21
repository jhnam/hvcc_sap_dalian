/**
 * 
 */
package com.hvcc.sap;

import java.sql.Connection;

/**
 * 
 * @author Shortstop
 */
public interface IMesConnection {

	/**
	 * JDBC Connection을 리턴한다.
	 * 
	 * @return
	 * @throws Exception
	 */
	public Connection getConnection() throws Exception;
}
