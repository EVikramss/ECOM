package com.ecom.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OrderItemData {

	private int lineno;

	private int status;

	@NotNull(message = "itemData.qty is required")
	@Min(1)
	private int qty;

	@NotBlank(message = "itemData.sku is required")
	private String sku;
	
	@NotBlank(message = "itemData.taxCode is required")
	private String taxCode;
	
	@NotBlank(message = "itemData.desc is required")
	private String desc;
	
	@NotBlank(message = "itemData.price is required")
	private float price;

	public int getLineno() {
		return lineno;
	}

	public void setLineno(int lineno) {
		this.lineno = lineno;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getTaxCode() {
		return taxCode;
	}

	public void setTaxCode(String taxCode) {
		this.taxCode = taxCode;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
}
