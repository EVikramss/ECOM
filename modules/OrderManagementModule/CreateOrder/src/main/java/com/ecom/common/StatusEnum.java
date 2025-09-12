package com.ecom.common;

public enum StatusEnum {
	CREATED(0), SCHEDULED(1), SHIPPED(2), CANCELLED(3);

	private int status;

	StatusEnum(int status) {
		this.status = status;
	}

	public int getStatus() {
		return status;
	}
}
