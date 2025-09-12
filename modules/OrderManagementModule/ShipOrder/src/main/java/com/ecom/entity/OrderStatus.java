package com.ecom.entity;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(indexes = { @Index(columnList = "order_key", unique = true), @Index(columnList = "status", unique = false) })
public class OrderStatus {

	@Id
	@Column(name = "order_status_key", length = 32)
	private BigInteger orderStatusKey;

	@Column(name = "status", nullable = false)
	private int status;

	@Column(name = "order_key", nullable = false)
	private BigInteger orderKey;

	@OneToOne(mappedBy = "orderStatus")
	@JsonBackReference
	private OrderData orderData;

	public BigInteger getOrderStatusKey() {
		return orderStatusKey;
	}

	public void setOrderStatusKey(BigInteger orderStatusKey) {
		this.orderStatusKey = orderStatusKey;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public OrderData getOrderData() {
		return orderData;
	}

	public void setOrderData(OrderData orderData) {
		this.orderData = orderData;
	}

	public BigInteger getOrderKey() {
		return orderKey;
	}

	public void setOrderKey(BigInteger orderKey) {
		this.orderKey = orderKey;
	}
}
