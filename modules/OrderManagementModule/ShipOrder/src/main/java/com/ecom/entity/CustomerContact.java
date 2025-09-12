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

	@Column(length = 3, nullable = false)
	private String salutation;

	@Column(length = 16, nullable = false)
	private String firstName;

	@Column(length = 16, nullable = false)
	private String lastName;

	@Column(length = 16, nullable = false)
	private String phone;

	@Column(length = 16, nullable = false)
	private String email;

	@OneToOne(mappedBy = "customerContact")
	@JsonBackReference
	private OrderData orderData;

	public BigInteger getCustomerContactkey() {
		return customerContactkey;
	}

	public void setCustomerContactkey(BigInteger customerContactkey) {
		this.customerContactkey = customerContactkey;
	}

	public String getSalutation() {
		return salutation;
	}

	public void setSalutation(String salutation) {
		this.salutation = salutation;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
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
}
