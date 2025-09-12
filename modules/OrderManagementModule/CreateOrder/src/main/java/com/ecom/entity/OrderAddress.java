package com.ecom.entity;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class OrderAddress {

	@Id
	@Column(name = "address_key")
	private BigInteger addresskey;

	@Column(length = 3, nullable = false)
	private String country;

	@Column(length = 16, nullable = false)
	private String city;

	@Column(length = 16, nullable = false)
	private String state;

	@Column(length = 64, nullable = false)
	private String addressline1;

	@Column(length = 64, nullable = false)
	private String addressline2;

	@OneToOne(mappedBy = "address")
	@JsonBackReference
	private OrderData orderData;

	public BigInteger getAddresskey() {
		return addresskey;
	}

	public void setAddresskey(BigInteger addresskey) {
		this.addresskey = addresskey;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getAddressline1() {
		return addressline1;
	}

	public void setAddressline1(String addressline1) {
		this.addressline1 = addressline1;
	}

	public String getAddressline2() {
		return addressline2;
	}

	public void setAddressline2(String addressline2) {
		this.addressline2 = addressline2;
	}

	public OrderData getOrderData() {
		return orderData;
	}

	public void setOrderData(OrderData orderData) {
		this.orderData = orderData;
	}
}
