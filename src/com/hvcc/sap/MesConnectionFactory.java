/**
 * 
 */
package com.hvcc.sap;

import java.sql.Connection;

/**
 * Connection Factory
 * 
 * @author Shortstop
 */
public class MesConnectionFactory {

	private static MesConnectionFactory connFactory = null;
	private IMesConnection mesConn = null;
	
	public Connection getConnection() throws Exception {
		if(this.mesConn == null) {
			this.mesConn = (IMesConnection)Class.forName(Constants.DB_CONNECTOR_CLASS).newInstance();
		}
		
		return this.mesConn.getConnection();
	}
	
	public static MesConnectionFactory getInstance() {
		if(connFactory == null) {
			connFactory = new MesConnectionFactory();
		}
		
		return connFactory;
	}
}
