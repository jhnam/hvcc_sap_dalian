package com.hvcc.sap;

import java.io.InputStream;
import java.util.Properties;

public class Constants {

	// SAP Configurations
	public static String SAP_SID;
	public static int SAP_MAX_CON;
	public static String SAP_CLIENT;
	public static String SAP_USER;
	public static String SAP_PASSWORD;
	public static String SAP_HOST;
	public static String SAP_SYSTEM;
	public static String SAP_LANG;
	
	// DB Configurations
	public static String DB_CONNECTOR_CLASS;
	public static String DB_DRIVER_CLASS;
	public static String DB_URL;
	public static String DB_USER;
	public static String DB_PASSWORD;
	
	// Thread Configurations
	public static String SAP_DATEFORMAT;
	public static long EXE_INTERVAL;
	public static long EXE_RFC_INTERVAL;
	
	static {
		InputStream is = Constants.class.getResourceAsStream("/resources/config.properties");
	    Properties props = new Properties();
	    try {
	    	props.load(is);
	    	SAP_SID = props.getProperty("sap.sid", "HCP");
	    	SAP_CLIENT = props.getProperty("sap.client", "110");
	    	SAP_HOST = props.getProperty("sap.ip", "sapprd.hvccglobal.com");
	    	
	    	SAP_MAX_CON = Integer.parseInt(props.getProperty("sap.max_con", "10"));
	    	SAP_USER = props.getProperty("sap.user", "C000-I002");
	    	SAP_PASSWORD = props.getProperty("sap.password", "hvccglobal");
	    	SAP_SYSTEM = props.getProperty("sap.system", "00");
	    	SAP_LANG = props.getProperty("sap.language", "ZH");
	    	
	    	DB_CONNECTOR_CLASS = props.getProperty("db.connector_class", "com.hvcc.sap.JdbcConnection");
	    	DB_DRIVER_CLASS = props.getProperty("db.driver_class", "oracle.jdbc.driver.OracleDriver");
	    	DB_URL = props.getProperty("db.url", "jdbc:oracle:thin:@192.168.2.81:1521:HVMES");
	    	DB_USER = props.getProperty("db.user", "HVCCB_MES");
	    	DB_PASSWORD = props.getProperty("db.password", "HVCCB_MES");
	    	
	    	SAP_DATEFORMAT = props.getProperty("sap.dateformat", "yyyyMMdd");
	    	EXE_INTERVAL = Integer.parseInt(props.getProperty("exe.interval", "60000"));
	    	EXE_RFC_INTERVAL = Integer.parseInt(props.getProperty("exe.rfc.interval", "5000"));
	    	
	    	System.out.println("SID : " + SAP_SID);
	    	System.out.println("SAP_CLIENT : " + SAP_CLIENT);
	    	System.out.println("SAP_HOST : " + SAP_HOST);
	    	System.out.println("SAP_MAX_CON : " + SAP_MAX_CON);
	    	System.out.println("SAP_USER : " + SAP_USER);
	    	System.out.println("SAP_PASSWORD : " + SAP_PASSWORD);
	    	System.out.println("SAP_SYSTEM : " + SAP_SYSTEM);
	    	System.out.println("SAP_LANG : " + SAP_LANG);
	    	
	    	System.out.println("DB_CONNECTOR_CLASS : " + DB_CONNECTOR_CLASS);
	    	System.out.println("DB_DRIVER_CLASS : " + DB_DRIVER_CLASS);
	    	System.out.println("DB_URL : " + DB_URL);
	    	System.out.println("DB_USER : " + DB_USER);
	    	System.out.println("DB_PASSWORD : " + DB_PASSWORD);
	    	
	    	System.out.println("SAP_DATEFORMAT : " + SAP_DATEFORMAT);
	    	System.out.println("EXE_INTERVAL : " + EXE_INTERVAL);
	    	System.out.println("EXE_RFC_INTERVAL : " + EXE_RFC_INTERVAL);	    	
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
	}
}
