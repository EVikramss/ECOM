package com.ecom.dto.search;

import org.springframework.data.domain.Sort.Direction;

import com.ecom.common.Util;

public class OrderSearch {

	private String orderNo;
	private String entity;
	private String matchType;
	private String fromDate;
	private String toDate;
	private int sortOrder;
	private int sortByField;

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getMatchType() {
		return matchType;
	}

	public void setMatchType(String matchType) {
		this.matchType = matchType;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public boolean anyNonBlankSearchParam() {
		return Util.isValidString(orderNo) || Util.isValidString(entity) || Util.isValidString(fromDate)
				|| Util.isValidString(toDate);
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public int getSortByField() {
		return sortByField;
	}

	public void setSortByField(int sortByField) {
		this.sortByField = sortByField;
	}

	public String getSortByAttribute() {
		// sort by order date by default
		String output = "orderKey";

		if (sortByField == 1) {
			output = "orderNo";
		} else if (sortByField == 2) {
			output = "entity";
		}

		return output;
	}

	public Direction getSortOrder() {
		if (sortOrder == 0) {
			return Direction.ASC;
		} else {
			return Direction.DESC;
		}
	}
}