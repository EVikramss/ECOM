package com.index.create;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

public class CreateItemDataIndex {

	private static final String FIELD_SPACING = "\\|";

	public static void main(String[] args) throws Exception {
		new CreateItemDataIndex().create();
	}

	private void create() throws Exception {
		FSDirectory indexDirectory = FSDirectory.open(Paths.get("index"));

		IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
		IndexWriter writer = new IndexWriter(indexDirectory, config);

		/*
		 * List<String[]> skuList = new ArrayList<String[]>(); try (BufferedReader br =
		 * new BufferedReader(new InputStreamReader(new FileInputStream(new
		 * File("itemData"))))) { String lineStr = null; while ((lineStr =
		 * br.readLine()) != null) { String[] skuArray = lineStr.split(FIELD_SPACING);
		 * String[] subSkuArray = new String[] { skuArray[0], skuArray[1], skuArray[5],
		 * skuArray[7], skuArray[8], skuArray[9] }; skuList.add(subSkuArray); } }
		 */

		List<String[]> skuList = new ArrayList<String[]>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("itemData"))))) {
			String lineStr = null;
			while ((lineStr = br.readLine()) != null) {
				String[] skuArray = lineStr.split(FIELD_SPACING);
				skuList.add(skuArray);
			}
		}

		for (int counter = 0; counter < skuList.size(); counter++) {
			String[] skuArray = skuList.get(counter);
			String itemID = skuArray[0];
			Double price = Double.parseDouble(skuArray[1]);
			String currency = skuArray[2];
			String maxQty = skuArray[3];
			String taxCode = skuArray[4];
			String desc = skuArray[5];
			String imgUrl = skuArray[6];
			String brand = skuArray[7];
			String category = skuArray[8];
			String subCategory = skuArray[9];

			Document document = new Document();
			document.add(new TextField("itemID", itemID, Store.YES));
			// document.add(new DoubleField("price", price, Store.YES));
			document.add(new DoublePoint("price", price));
			document.add(new StoredField("price", price));
			document.add(new DoubleDocValuesField("price", price));
			document.add(new TextField("currency", currency, Store.YES));
			document.add(new TextField("maxQty", maxQty, Store.YES));
			document.add(new TextField("taxCode", taxCode, Store.YES));
			document.add(new TextField("desc", desc, Store.YES));
			document.add(new TextField("imgUrl", imgUrl, Store.YES));
			document.add(new TextField("brand", brand, Store.YES));
			document.add(new TextField("category", category, Store.YES));
			document.add(new TextField("subcategory", subCategory, Store.YES));

			writer.addDocument(document);
		}

		writer.close();
	}
}
