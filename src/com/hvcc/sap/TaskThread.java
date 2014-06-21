package com.hvcc.sap;

import java.util.Date;
import java.util.logging.Logger;

import com.hvcc.sap.beijing.ActualToSap;
import com.hvcc.sap.beijing.MoveToSap;
import com.hvcc.sap.beijing.PrcActualToSap;
import com.hvcc.sap.beijing.ProductToMes;
import com.hvcc.sap.beijing.ParameterToMes;
import com.hvcc.sap.beijing.PlanToMes;
import com.hvcc.sap.beijing.ScrapToSap;
import com.hvcc.sap.util.DateUtils;

public class TaskThread extends Thread {

	private static final Logger LOGGER = Logger.getLogger(TaskThread.class.getName());
	private boolean running = true;
	private int count = 0;
	
	@Override
	public void run() {
		
		while(running) {
			this.sleepForSecs(Constants.EXE_INTERVAL);			
			count++;

			if(this.checkSap()) {
				// Processing line actual
				if(count % 10 == 0) {
					this.ifcPrcActual();
				
				// plan
				} else if(count % 13 == 0) {
					this.ifcPlan();
					
				// Packing move
				} else if(count % 15 == 0) {
					this.ifcMove();
					
				// product 
				} else if(count % 47 == 0) {
					this.ifcProduct();
				
				// parameter
				} else if(count % 49 == 0) {
					this.ifcParameter();
					
				// scrap
				} else if(count % 51 == 0) {
					this.ifcScrap();
					
				// GC
				} else if(count % 55 == 0) {
					this.garbageCollect();
					
				// Reset count
				} else if(count % 61 == 0) {
					count = 0;
					this.ifcActual();
					
				// Assembly line actual
				} else {
					this.ifcActual();
				}
			}
		}
	}
	
	private void sleepForSecs(long millisecond) {
		try {
			Thread.sleep(millisecond);
		} catch (InterruptedException e) {
		}
	}
	
	private boolean checkSap() {
		try {
			return SapConnectionPool.getInstance().isAlive();
		} catch(Throwable th) {
			LOGGER.severe("SAP Connection problem. Error : " + th.getMessage());
			return false;
		}
	}
	
	private void garbageCollect() {
		LOGGER.info("Garbage Collection Starting....");
		System.gc();
		LOGGER.info("Garbage Collecting Finished");
	}
	
	private void ifcActual() {
		LOGGER.info("Assy Actual ....");
		ActualToSap actual = new ActualToSap();
		actual.execute();
	}
	
	private void ifcPrcActual() {
		LOGGER.info("Processing Actual ....");
		PrcActualToSap actual = new PrcActualToSap();
		actual.execute();
	}
	
	private void ifcMove() {
		this.sleepForSecs(Constants.EXE_RFC_INTERVAL);
		LOGGER.info("Move ....");
		MoveToSap move = new MoveToSap();
		move.execute();
	}	
	
	private void ifcScrap() {
		this.sleepForSecs(Constants.EXE_RFC_INTERVAL);
		LOGGER.info("Scrap ....");
		ScrapToSap actual = new ScrapToSap();
		actual.execute();
	}

	private void ifcPlan() {
		this.sleepForSecs(Constants.EXE_RFC_INTERVAL);
		LOGGER.info("Plan ....");
		String[] dateInfo = this.getDateInfo();
		PlanToMes plan = new PlanToMes();
		plan.execute("", dateInfo[0], dateInfo[1]);
	}
	
	private void ifcProduct() {
		this.sleepForSecs(Constants.EXE_RFC_INTERVAL);
		LOGGER.info("Product ....");
		String[] dateInfo = this.getDateInfo();
        ProductToMes productToMes = new ProductToMes();
        productToMes.execute("", dateInfo[0], dateInfo[1]);
	}
	
	private void ifcParameter() {
		this.sleepForSecs(Constants.EXE_RFC_INTERVAL);
		LOGGER.info("Parameter ....");
		String[] dateInfo = this.getDateInfo();
        ParameterToMes paramToMes = new ParameterToMes();
        paramToMes.execute("", dateInfo[0], dateInfo[1]);
	}
	
	private String[] getDateInfo() {
		Date fromDate = new Date();
		/*Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -1);
		Date fromDate = c.getTime();*/
        String fromDateStr = DateUtils.format(fromDate, Constants.SAP_DATEFORMAT);
        String toDateStr = DateUtils.format(fromDate, Constants.SAP_DATEFORMAT);
        String[] str = new String[2];
        str[0] = fromDateStr;
        str[1] = toDateStr;
        return str;
	}
	 
}
