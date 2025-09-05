package com.schedule.common;

import java.util.HashMap;
import java.util.Map;

public enum EntityNode {
	TEST("TEST", "node1");

	private static Map<String, String> entityNodeMap = new HashMap<String, String>();
	private String entity;

	EntityNode(String entity, String node) {
		this.entity = entity;
		setNodeForEntity(entity, node);
	}

	private void setNodeForEntity(String entity, String node) {
		entityNodeMap.put(entity, node);
	}

	public String getNodeForEntity() {
		return entityNodeMap.get(entity);
	}
}
