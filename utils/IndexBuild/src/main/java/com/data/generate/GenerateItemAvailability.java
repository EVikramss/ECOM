package com.data.generate;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

public class GenerateItemAvailability {

	private static final String READ_FIELD_SPACING = "\\|";

	private Random random = new Random();

	public static void main(String[] args) throws Exception {
		new GenerateItemAvailability().createZip();
	}

	private void createZip() throws Exception {
		List<String[]> skuList = new ArrayList<String[]>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("itemData"))))) {
			String lineStr = null;
			while ((lineStr = br.readLine()) != null) {
				String[] skuArray = lineStr.split(READ_FIELD_SPACING);
				String[] subSKUArray = new String[] { skuArray[0] };
				skuList.add(subSKUArray);
			}
		}

		List<String> nodeList = new ArrayList<String>();
		for (int counter = 0; counter < 33; counter++) {
			nodeList.add("node" + counter);
		}

		try (GZIPOutputStream itemSupply = new GZIPOutputStream(
				new BufferedOutputStream(new FileOutputStream(new File("itemSply.gz"))));
				GZIPOutputStream itemInfo = new GZIPOutputStream(
						new BufferedOutputStream(new FileOutputStream(new File("itemInfo.gz"))))) {

			// add header
			/*
			 * byte[] headerBytes = ("itemID,node,avl,mov" +
			 * System.lineSeparator()).getBytes(); zos.write(headerBytes, 0,
			 * headerBytes.length);
			 */

			// add data
			skuList.parallelStream().forEach(ia -> {
				String itemID = ia[0];
				int totalavl = 0;

				for (int counter2 = 0; counter2 < nodeList.size(); counter2++) {
					String node = nodeList.get(counter2);
					int avl = (int) (random.nextDouble() * 10.0);
					try {
						byte[] dataBytes = (itemID + "," + node + "," + avl + System.lineSeparator()).getBytes();
						itemSupply.write(dataBytes, 0, dataBytes.length);
						totalavl += avl;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					byte[] dataBytes = (itemID + "," + "TEST" + "," + totalavl + ",0" + System.lineSeparator())
							.getBytes();
					itemInfo.write(dataBytes, 0, dataBytes.length);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}
}
