package com.ecom.exception;

public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private Object input;
	private String exceptionMessage;
	private StackTraceElement[] stackTraceElements;
	private String functionName;

	public ServiceException(Object input, String exceptionMessage, StackTraceElement[] stackTraceElements,
			String functionName) {
		super();
		this.input = input;
		this.exceptionMessage = exceptionMessage;
		this.stackTraceElements = stackTraceElements;
		this.functionName = functionName;
	}

	public Object getInput() {
		return input;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public StackTraceElement[] getStackTraceElements() {
		return stackTraceElements;
	}

	public String getFunctionName() {
		return functionName;
	}
}
