package com.ecom.entity;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class OrderItemData {

	@Id
	@Column(length = 32)
	private BigInteger orderItemKey;

	@Column(nullable = false)
	private int lineno;

	@Column(nullable = false)
	private int status;

	@Column(nullable = false)
	private int qty;

	@Column(length = 40, nullable = false)
	private String sku;
	
	@Column(length = 5, nullable = false)
	private String taxCode;
	
	@Column(name = "descr", length = 80, nullable = false)
	private String desc;
	
	@Column(precision = 3, nullable = false)
	private float price;

	@ManyToOne
	@JsonBackReference
	@JoinColumn(name = "order_key")
	private OrderData orderData;

	public BigInteger getOrderItemKey() {
		return orderItemKey;
	}

	public void setOrderItemKey(BigInteger orderItemKey) {
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

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public OrderData getOrderData() {
		return orderData;
	}

	public void setOrderData(OrderData orderData) {
		this.orderData = orderData;
	}

	public String getTaxCode() {
		return taxCode;
	}

	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
}
