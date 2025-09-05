package com.schedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "OrderStatus")
public class OrderStatus {

	@Id
	@Column(name = "orderstatuskey", length = 32)
	private String orderStatusKey;

	@Column(name = "status")
	private int status;

	@Column(name = "orderNo", length = 40)
	private String orderNo;

	public String getOrderStatusKey() {
		return orderStatusKey;
	}

	public void setOrderStatusKey(String orderStatusKey) {
		this.orderStatusKey = orderStatusKey;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
}
