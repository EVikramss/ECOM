package com.ecom.entity;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class CustomerContact {

	@Id
	@Column(name = "customer_contact_key")
	private BigInteger customerContactkey;

	@Column(length = 16, nullable = false)
	private String fullName;

	@Column(length = 16, nullable = true)
	private String phone;

	@Column(length = 16, nullable = true)
	private String email;
	
	@Column(length = 36, nullable = true)
	private String sub;

	@OneToOne(mappedBy = "customerContact")
	@JsonBackReference
	private OrderData orderData;

	public BigInteger getCustomerContactkey() {
		return customerContactkey;
	}

	public void setCustomerContactkey(BigInteger customerContactkey) {
		this.customerContactkey = customerContactkey;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public OrderData getOrderData() {
		return orderData;
	}

	public void setOrderData(OrderData orderData) {
		this.orderData = orderData;
	}

	public String getSub() {
		return sub;
	}

	public void setSub(String sub) {
		this.sub = sub;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}
