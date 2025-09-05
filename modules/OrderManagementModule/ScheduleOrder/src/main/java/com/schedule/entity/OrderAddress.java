package com.schedule.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "OrderAddress")
public class OrderAddress {

	@Id
	@Column(name = "addresskey", length = 32)
	private String addresskey;

	@Column(name = "country", length = 3)
	private String country;

	@Column(name = "city", length = 16)
	private String city;

	@Column(name = "state", length = 16)
	private String state;

	@Column(name = "addressline1", length = 64)
	private String addressline1;

	@Column(name = "addressline2", length = 64)
	private String addressline2;

	@OneToOne
	@JsonBackReference
	private OrderData orderData;

	public String getAddresskey() {
		return addresskey;
	}

	public void setAddresskey(String addresskey) {
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
