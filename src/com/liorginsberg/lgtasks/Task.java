package com.liorginsberg.lgtasks;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Task {
	
	private long taskID;
	private String title;
	private String desc;
	private Calendar calendarFrom;
	private Calendar calendarTo;
	private String location;
	private int isChecked;
	
	public boolean animated = false;

  
	public Task(long taskID, String title, String desc, String from, String to, String location, int isChecked) {
		this.taskID = taskID;
		this.title = title;
		this.desc = desc;
		
		if(!from.isEmpty()) {
			calendarFrom = stringDateToCalendar(from);
		}
		if(!to.isEmpty()){
			calendarTo = stringDateToCalendar(to);
		}
		
		this.location = location;
		this.isChecked = isChecked;
	}
	
	public static Calendar stringDateToCalendar(String date) {
		 SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
		 Date d = null;
		 try {
			 d = dateFormat.parse(date);
		 } catch (Exception e) {
			e.printStackTrace();
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(d);
		return calendar;
	}

	public long getTask_id() {
		return taskID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}


	public int isChecked() {
		return isChecked;
	}

	public void setChecked(int isChecked) {
		this.isChecked = isChecked;
	}
	
	public String getFromTo() {
		if(calendarFrom == null || calendarTo == null) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:dd", Locale.US); 
		StringBuilder sb = new StringBuilder();
		String test = sdf.format(calendarFrom.getTime());
		sb.append(test);
		sb.append(" - ");
		sb.append(sdf.format(calendarTo.getTime()));
		return sb.toString();
	}
	
	public Calendar getCalendarFrom() {
		return calendarFrom;
	}

	public void setCalendarFrom(Calendar calendarFrom) {
		this.calendarFrom = calendarFrom;
	}

	public Calendar getCalendarTo() {
		return calendarTo;
	}

	public void setCalendarTo(Calendar calendarTo) {
		this.calendarTo = calendarTo;
	}
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
