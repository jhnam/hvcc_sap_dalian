/**
 * 
 */
package com.hvcc.sap;

import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;

/**
 * SAP Connection Pool
 * 
 * 접속 상태 체크, 리커넥트 가능
 * 
 * @author Shortstop
 */
public class SapConnectionPool {
	
	private String SID = null;
	private static SapConnectionPool instance = null;
	
	public SapConnectionPool(String sid, int maxcons, String client, String userid, String passwd, String lang, String server, String sysno) 
	throws Throwable {
		// Create SAP Connection Pool
		JCO.addClientPool(sid, maxcons, client, userid, passwd, lang, server, sysno);
		SID = sid;
	}

	public static SapConnectionPool getInstance() throws Throwable {
		if (instance == null) { 
			instance = new SapConnectionPool(
					Constants.SAP_SID, 
					Constants.SAP_MAX_CON, 
					Constants.SAP_CLIENT, 
					Constants.SAP_USER, 
					Constants.SAP_PASSWORD, 
					Constants.SAP_LANG, 
					Constants.SAP_HOST, 
					Constants.SAP_SYSTEM);
		}

		return instance;
	}
	
	/**
	 * connection이 살아있는지 체크
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean isAlive() throws Throwable {
		if(instance == null)
			return false;
		
		boolean isAlive = false;
		JCO.Client conn = instance.getConnection();
		
		try {
			isAlive = conn.isAlive();
		} catch(Throwable th) {
			throw th;
			
		} finally {
			instance.releaseConnection(conn);
		}
		
		return isAlive;
	}
	
	public JCO.Function createFunction(IRepository mRepository, String name) throws Exception {
		return mRepository.getFunctionTemplate(name.toUpperCase()).getFunction();
	}

	public JCO.Client getConnection() throws Exception {
		return JCO.getClient(SID);
	} 
	
	public void releaseConnection(JCO.Client connection) {
		try {
			JCO.releaseClient(connection);
		} catch (Throwable th) {
			System.out.println("SAPConnectionPool:releaseConnection Error : " + th.toString());
		}
	}
}
