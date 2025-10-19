package com.ecom.dto;

import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.ScoreDoc;

public class PageData {

	private float score;
	private int doc;
	private int shardIndex;
	public Object[] fields;

	public static PageData fromDoc(ScoreDoc scoreDoc) {
		PageData output = null;

		if (scoreDoc != null) {
			output = new PageData();
			output.setDoc(scoreDoc.doc);
			output.setScore(scoreDoc.score);
			output.setShardIndex(scoreDoc.shardIndex);

			if (scoreDoc instanceof FieldDoc) {
				FieldDoc fieldDoc = (FieldDoc) scoreDoc;
				Object[] fields = fieldDoc.fields;

				Object[] copiedFields = new Object[fields.length];
				for (int counter = 0; counter < fields.length; counter++) {
					Object fieldVal = fields[counter];

					// copy numbers only
					if (fieldVal instanceof Number) {
						copiedFields[counter] = fieldVal;
					}
				}
				output.setFields(copiedFields);
			}
		}

		return output;
	}

	public static ScoreDoc toDoc(SearchFilters filters) {
		ScoreDoc output = null;

		Float score = filters.getScore();
		Integer doc = filters.getDoc();
		Integer shardIndex = filters.getShardIndex();
		Double fieldVal = filters.getFieldVal();
		int pageResults = filters.getPageResults();

		if (pageResults == 1) {
			if (fieldVal != null) {
				output = new FieldDoc(doc, score, new Object[] { fieldVal }, shardIndex);
			} else {
				output = new ScoreDoc(doc, score, shardIndex);
			}
		}

		return output;
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

	public Object[] getFields() {
		return fields;
	}

	public void setFields(Object[] fields) {
		this.fields = fields;
	}
}
