package com.hvcc.sap.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hvcc.sap.beijing.ActualToSap;

public class ActualToSapJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("Actual To SAP Job started");
		ActualToSap actualToSap = new ActualToSap();
		actualToSap.execute();
		System.out.println("Actual To SAP Job finished");
	}

}
