package com.ecom.dto;

import jakarta.validation.constraints.NotBlank;

public class OrderAddress {

	@NotBlank(message = "address.country is required")
	private String country;

	@NotBlank(message = "address.city is required")
	private String city;

	@NotBlank(message = "address.state is required")
	private String state;

	@NotBlank(message = "address.addressline1 is required")
	private String addressline1;

	@NotBlank(message = "address.addressline2 is required")
	private String addressline2;

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
}
