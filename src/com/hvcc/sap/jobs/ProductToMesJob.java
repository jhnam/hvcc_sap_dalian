package com.hvcc.sap.jobs;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hvcc.sap.beijing.ProductToMes;
import com.hvcc.sap.util.DateUtils;

public class ProductToMesJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("This is BomToMes quartz job!!");
        ProductToMes bomToMes = new ProductToMes();
        
        Date fromDate = new Date();
        Date toDate = DateUtils.addDate(fromDate, 1);
        String fromDateStr = DateUtils.format(fromDate, "yyyyMMdd");
        String toDateStr = DateUtils.format(toDate, "yyyyMMdd");
        System.out.println("From Date: " + fromDateStr + ", To Date : " + toDateStr);
        
        try {
        	bomToMes.execute("X", fromDateStr, toDateStr);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

}
