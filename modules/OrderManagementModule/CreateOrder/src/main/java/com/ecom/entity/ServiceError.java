package com.ecom.entity;

import java.math.BigInteger;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ServiceError {

	@Id
	private BigInteger errorKey;

	@Column(nullable = false)
	private String service;

	@Column(nullable = false)
	private String functionName;

	@Column(nullable = false)
	private Timestamp date;
	
	@Column(length = 512, nullable = false)
	private String errorMessage;

	@Column(length = 2048, nullable = false)
	private String input;

	@Column(length = 2048, nullable = false)
	private String stackTrace;

	public BigInteger getErrorKey() {
		return errorKey;
	}

	public void setErrorKey(BigInteger errorKey) {
		this.errorKey = errorKey;
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

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
}
