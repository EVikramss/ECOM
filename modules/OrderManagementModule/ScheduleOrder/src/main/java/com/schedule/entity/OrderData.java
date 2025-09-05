package com.schedule.entity;

import java.sql.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "OrderData")
public class OrderData {

	@Id
	@Column(name = "OrderNo", length = 40)
	private String orderNo;

	@Column(name = "OrderDate")
	private Date orderDate;

	@Column(name = "Entity", length = 40)
	private String entity;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonManagedReference
	private OrderAddress address;
	
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonManagedReference
	private CustomerContact customerContact;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "OrderNo")
	@JsonManagedReference
	private Set<OrderItemData> itemData;
	
	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public Date getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(Date orderDate) {
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
}
