package com.ecom.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.dto.SearchFilters;
import com.ecom.dto.SearchResult;
import com.ecom.services.IndexSearchService;

@RestController
@CrossOrigin(origins = {"*"})
public class IndexSearchController {

	@Autowired
	private IndexSearchService indexSearchService;

	@GetMapping("/getSkuList")
	public SearchResult searchIndex(@ModelAttribute SearchFilters filters) throws Exception {
		return indexSearchService.search(filters);
	}
}
