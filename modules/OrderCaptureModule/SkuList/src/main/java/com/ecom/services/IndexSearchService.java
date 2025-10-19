package com.ecom.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.dto.ItemResult;
import com.ecom.dto.PageData;
import com.ecom.dto.SearchFilters;
import com.ecom.dto.SearchResult;

@Service
public class IndexSearchService {

	@Autowired
	private IndexSearcher indexSearcher;

	public SearchResult search(SearchFilters filters) throws Exception {

		// create output with blank result list
		SearchResult output = new SearchResult();
		List<ItemResult> itemResultList = new ArrayList<ItemResult>();
		output.setItemResultList(itemResultList);

		try {
			// build query
			Query query = buildQuery(filters);

			// create sort order
			Sort sort = buildSort(filters);

			// set count
			int pageSize = filters.getPageSize();
			if (pageSize == 0)
				pageSize = 30;

			// last result to paginate over
			ScoreDoc lastResult = PageData.toDoc(filters);

			// fetch results for query - conditional for sorting and pagination
			TopDocs results = (sort != null)
					? (lastResult != null ? indexSearcher.searchAfter(lastResult, query, pageSize, sort)
							: indexSearcher.search(query, pageSize, sort))
					: (lastResult != null ? indexSearcher.searchAfter(lastResult, query, pageSize)
							: indexSearcher.search(query, pageSize));

			// read from results if any
			if (results.totalHits.value > 0) {
				StoredFields storedFields = indexSearcher.storedFields();
				for (ScoreDoc hit : results.scoreDocs) {
					Document doc = storedFields.document(hit.doc);

					ItemResult result = new ItemResult();
					result.setBrand(doc.get("brand"));
					result.setCategory(doc.get("category"));
					result.setCurrency(doc.get("currency"));
					result.setDesc(doc.get("desc"));
					result.setImgUrl(doc.get("imgUrl"));
					result.setItemID(doc.get("itemID"));
					result.setMaxQty(doc.get("maxQty"));
					result.setPrice(Double.parseDouble(doc.get("price")));
					result.setSubcategory(doc.get("subcategory"));
					result.setTaxCode(doc.get("taxCode"));

					itemResultList.add(result);
				}

				// store last result
				lastResult = results.scoreDocs.length == 0 ? null : results.scoreDocs[results.scoreDocs.length - 1];
				output.setPageData(PageData.fromDoc(lastResult));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return output;
	}

	private Sort buildSort(SearchFilters filters) {

		String sortField = filters.getSortField();
		Sort sort = null;

		if (sortField != null)
			if ("price".equals(sortField)) {
				sort = new Sort(
						new SortField("price", SortField.Type.DOUBLE, filters.getSortOrder() > 0 ? true : false));
			} else {
				sort = new Sort(
						new SortField(sortField, SortField.Type.STRING, filters.getSortOrder() > 0 ? true : false));
			}

		return sort;
	}

	public static Query buildQuery(SearchFilters filters) throws Exception {

		String category = filters.getCategory();
		String subCategory = filters.getSubCategory();
		String brand = filters.getBrand();
		String desc = filters.getDesc();
		Double priceRangeStart = filters.getPriceRangeStart();
		Double priceRangeEnd = filters.getPriceRangeEnd();
		boolean anyFilterPresent = false;

		Analyzer analyzer = new StandardAnalyzer();

		BooleanQuery.Builder builder = new BooleanQuery.Builder();

		if (category != null && !category.isBlank()) {
			builder.add(new QueryParser("category", analyzer).parse(category), Occur.MUST);
			anyFilterPresent = true;
		}

		if (subCategory != null && !subCategory.isBlank()) {
			builder.add(new QueryParser("subcategory", analyzer).parse(subCategory), Occur.MUST);
			anyFilterPresent = true;
		}

		if (brand != null && !brand.isBlank()) {
			builder.add(new QueryParser("brand", analyzer).parse(brand), Occur.MUST);
			anyFilterPresent = true;
		}

		if (desc != null && !desc.isBlank()) {
			builder.add(new QueryParser("desc", analyzer).parse(desc), Occur.MUST);
			anyFilterPresent = true;
		}

		if (priceRangeStart != null || priceRangeEnd != null) {
			double min = priceRangeStart != null ? priceRangeStart : Double.NEGATIVE_INFINITY;
			double max = priceRangeEnd != null ? priceRangeEnd : Double.POSITIVE_INFINITY;
			builder.add(DoublePoint.newRangeQuery("price", min, max), Occur.MUST);
			anyFilterPresent = true;
		}
		
		if(!anyFilterPresent) {
			builder.add(new MatchAllDocsQuery(), Occur.MUST);
		}

		return builder.build();
	}
}
