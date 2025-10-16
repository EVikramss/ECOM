package com.index;

import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class SearchIndex {

	public static void main(String[] args) throws Exception {
		FSDirectory indexDirectory = FSDirectory.open(Paths.get("index"));
		IndexReader reader = DirectoryReader.open(indexDirectory);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		QueryParser parser = new QueryParser("desc", new StandardAnalyzer());
		Query query = parser.parse("desc:Tablets");
		
		TopDocs results = searcher.search(query, 1000);
		for(ScoreDoc scoreDoc : results.scoreDocs) {
			Document doc = searcher.doc(scoreDoc.doc);
			System.out.println(doc.get("desc"));
			System.out.println(doc.get("itemID"));
		}
	}
}
