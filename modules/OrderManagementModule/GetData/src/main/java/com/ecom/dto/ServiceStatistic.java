package com.ecom.dto;

import java.math.BigInteger;
import java.sql.Timestamp;

public class ServiceStatistic {

	private BigInteger statsKey;
	private String service;
	private String functionName;
	private Timestamp fromTime;
	private Timestamp toTime;
	private double count;
	private double average;
	private double max;
	private double min;
	
	public BigInteger getStatsKey() {
		return statsKey;
	}

	public void setStatsKey(BigInteger statsKey) {
		this.statsKey = statsKey;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public Timestamp getFromTime() {
		return fromTime;
	}

	public void setFromTime(Timestamp fromTime) {
		this.fromTime = fromTime;
	}

	public Timestamp getToTime() {
		return toTime;
	}

	public void setToTime(Timestamp toTime) {
		this.toTime = toTime;
	}

	public double getCount() {
		return count;
	}

	public void setCount(double count) {
		this.count = count;
	}

	public double getAverage() {
		return average;
	}

	public void setAverage(double average) {
		this.average = average;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}
}
