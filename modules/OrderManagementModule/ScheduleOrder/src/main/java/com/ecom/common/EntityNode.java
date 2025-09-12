package com.ecom.common;

public enum EntityNode {
	TEST("node1");

	private String node;

	EntityNode(String node) {
		this.node = node;
	}

	public String getNode() {
		return node;
	}
}
