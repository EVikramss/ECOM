package com.schedule.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "OrderItemData")
public class OrderItemData {

	@Id
	@Column(name = "OrderItemKey", length = 32)
	private String orderItemKey;

	@Column(name = "lineno")
	private int lineno;

	@Column(name = "status")
	private int status;

	@Column(name = "qty")
	private int qty;

	@Column(name = "itemid", length = 40)
	private String itemid;

	@ManyToOne
	@JsonBackReference
	private OrderData orderData;

	public String getOrderItemKey() {
		return orderItemKey;
	}

	public void setOrderItemKey(String orderItemKey) {
		this.orderItemKey = orderItemKey;
	}

	public int getLineno() {
		return lineno;
	}

	public void setLineno(int lineno) {
		this.lineno = lineno;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public String getItemid() {
		return itemid;
	}

	public void setItemid(String itemid) {
		this.itemid = itemid;
	}

	public OrderData getOrderData() {
		return orderData;
	}

	public void setOrderData(OrderData orderData) {
		this.orderData = orderData;
	}
}
