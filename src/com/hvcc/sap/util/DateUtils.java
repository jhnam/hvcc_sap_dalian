package com.hvcc.sap.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

	public static Date addDate(Date date, int addDays) {
		Calendar c = Calendar.getInstance(); 
        c.setTime(date);
        c.add(Calendar.DATE, addDays);
        Date toDate = c.getTime();
        return toDate;
	}
	
	public static String format(Date date, String format) {
		DateFormat df = new SimpleDateFormat(format);
		return df.format(date);
	}	
}
