package com.ecom.dto;

import jakarta.validation.constraints.NotBlank;

public class CustomerContact {

	@NotBlank(message = "customerContact.salutation is required")
	private String salutation;

	@NotBlank(message = "customerContact.firstName is required")
	private String firstName;

	@NotBlank(message = "customerContact.lastName is required")
	private String lastName;

	@NotBlank(message = "customerContact.phone is required")
	private String phone;

	@NotBlank(message = "customerContact.email is required")
	private String email;

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
}
