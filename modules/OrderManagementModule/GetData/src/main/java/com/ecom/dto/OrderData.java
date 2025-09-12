package com.ecom.dto;

import java.sql.Timestamp;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderData {

	@NotBlank(message = "orderNo is required")
	private String orderNo;
	
	private Timestamp orderDate;

	@NotBlank(message = "entity is required")
	private String entity;

	@Valid
	@NotNull(message = "address is required")
	private OrderAddress address;

	@Valid
	@NotNull(message = "customerContact is required")
	private CustomerContact customerContact;

	@Valid
	@NotNull(message = "itemData is required")
	private Set<OrderItemData> itemData;
	
	private OrderStatus orderStatus;

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public Timestamp getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Timestamp orderDate) {
		this.orderDate = orderDate;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public OrderAddress getAddress() {
		return address;
	}

	public void setAddress(OrderAddress address) {
		this.address = address;
	}

	public CustomerContact getCustomerContact() {
		return customerContact;
	}

	public void setCustomerContact(CustomerContact customerContact) {
		this.customerContact = customerContact;
	}

	public Set<OrderItemData> getItemData() {
		return itemData;
	}

	public void setItemData(Set<OrderItemData> itemData) {
		this.itemData = itemData;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}
}
