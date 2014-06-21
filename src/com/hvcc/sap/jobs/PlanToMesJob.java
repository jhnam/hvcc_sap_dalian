package com.hvcc.sap.jobs;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.hvcc.sap.beijing.PlanToMes;
import com.hvcc.sap.util.DateUtils;

public class PlanToMesJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		PlanToMes planToMes = new PlanToMes();
        Date fromDate = new Date();
        Date toDate = DateUtils.addDate(fromDate, 1);
        String fromDateStr = DateUtils.format(fromDate, "yyyyMMdd");
        String toDateStr = DateUtils.format(toDate, "yyyyMMdd");
        planToMes.execute("X", fromDateStr, toDateStr);
	}
}
