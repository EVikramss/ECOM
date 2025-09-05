package com.schedule.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CustomerContact")
public class CustomerContact {

	@Id
	@Column(name = "customerkey", length = 32)
	private String customerkey;

	@Column(name = "salutation", length = 3)
	private String salutation;

	@Column(name = "firstName", length = 16)
	private String firstName;

	@Column(name = "lastName", length = 16)
	private String lastName;

	@Column(name = "phone", length = 16)
	private String phone;

	@Column(name = "email", length = 16)
	private String email;

	@OneToOne
	@JsonBackReference
	private OrderData orderData;

	public String getCustomerkey() {
		return customerkey;
	}

	public void setCustomerkey(String customerkey) {
		this.customerkey = customerkey;
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
