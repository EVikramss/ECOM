package com.ecom.dto;

public class SearchFilters {

	private String category;
	private String subCategory;
	private String brand;
	private String desc;
	private Double priceRangeStart;
	private Double priceRangeEnd;
	private String sortField;
	private int sortOrder;
	private int pageSize;
	
	// attributes for pagination
	private float score;
	private int doc;
	private int shardIndex;
	public Double fieldVal;
	private int pageResults = 0;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Double getPriceRangeStart() {
		return priceRangeStart;
	}

	public void setPriceRangeStart(Double priceRangeStart) {
		this.priceRangeStart = priceRangeStart;
	}

	public Double getPriceRangeEnd() {
		return priceRangeEnd;
	}

	public void setPriceRangeEnd(Double priceRangeEnd) {
		this.priceRangeEnd = priceRangeEnd;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}

	public int getDoc() {
		return doc;
	}

	public void setDoc(int doc) {
		this.doc = doc;
	}

	public int getShardIndex() {
		return shardIndex;
	}

	public void setShardIndex(int shardIndex) {
		this.shardIndex = shardIndex;
	}

	public Double getFieldVal() {
		return fieldVal;
	}

	public void setFieldVal(Double fieldVal) {
		this.fieldVal = fieldVal;
	}

	public int getPageResults() {
		return pageResults;
	}

	public void setPageResults(int pageResults) {
		this.pageResults = pageResults;
	}
}
