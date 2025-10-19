package com.ecom.dto;

import java.util.List;

public class SearchResult {

	private List<ItemResult> itemResultList;
	private PageData pageData;

	public List<ItemResult> getItemResultList() {
		return itemResultList;
	}

	public void setItemResultList(List<ItemResult> itemResultList) {
		this.itemResultList = itemResultList;
	}

	public PageData getPageData() {
		return pageData;
	}

	public void setPageData(PageData pageData) {
		this.pageData = pageData;
	}

}
