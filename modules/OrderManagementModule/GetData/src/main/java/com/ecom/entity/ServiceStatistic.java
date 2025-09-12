package com.ecom.entity;

import java.math.BigInteger;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ServiceStatistic {

	@Id
	private BigInteger statsKey;

	@Column(nullable = false)
	private String service;

	@Column(nullable = false)
	private String functionName;

	@Column(nullable = false)
	private Timestamp fromTime;

	@Column(nullable = false)
	private Timestamp toTime;

	@Column(nullable = false)
	private double count;

	@Column(nullable = false)
	private double average;

	@Column(nullable = false)
	private double max;

	@Column(nullable = false)
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

	public Timestamp getFrom() {
		return fromTime;
	}

	public void setFrom(Timestamp from) {
		this.fromTime = from;
	}

	public Timestamp getTo() {
		return toTime;
	}

	public void setTo(Timestamp to) {
		this.toTime = to;
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
