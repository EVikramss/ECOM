package com.ecom.entity;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(indexes = {@Index(columnList = "order_no", unique = true)})
public class OrderData {

	@Id
	@Column(name = "order_key")
	private BigInteger orderKey;

	@Column(name="order_no", length = 40, nullable = false)
	private String orderNo;

	private Timestamp orderDate;

	@Column(length = 40, nullable = false)
	private String entity;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "address_key")
	@JsonManagedReference
	private OrderAddress address;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "customer_contact_key")
	@JsonManagedReference
	private CustomerContact customerContact;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "orderData")
	@JsonManagedReference
	private Set<OrderItemData> itemData;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "order_status_key")
	@JsonManagedReference
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

	public BigInteger getOrderKey() {
		return orderKey;
	}

	public void setOrderKey(BigInteger orderKey) {
		this.orderKey = orderKey;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}
}
