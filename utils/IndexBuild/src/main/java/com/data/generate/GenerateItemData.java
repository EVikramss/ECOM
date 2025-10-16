package com.data.generate;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GenerateItemData {

	private static final String FIELD_SPACING = "|";

	// categories and pricing info
	private static final String[] TAX_CODES = new String[] { "0", "5", "18", "40" };
	private static final String[] CATEGORIES = new String[] { "Mobiles", "Fashion", "Electronics", "Home", "Books" };
	private static final Map<String, String[]> SUB_CATEGORIES = Map.of("Mobiles",
			new String[] { "Phones", "Covers", "Power Banks", "Tablets" }, "Fashion",
			new String[] { "Shirts", "Jeans", "Watches", "Wallets" }, "Electronics",
			new String[] { "TV", "Headphones", "Speakers", "Cameras" }, "Home",
			new String[] { "Lightning", "Kitchen & Cooking", "Garden & Outdoors", "HomeDecor" }, "Books",
			new String[] { "Fiction", "School", "Exam", "eBooks" });
	private static final Map<String, Double[]> subCatPriceRange = new HashMap<String, Double[]>();
	private static final Map<String, String[]> subCatTax = new HashMap<String, String[]>();

	private int itemCount = 100;
	private Random random = new Random();
	private DecimalFormat df = new DecimalFormat("0.00");

	public GenerateItemData(String[] args) {
		validateArgs(args);
		generateCategoryPriceRangeData();
		generateCategoryTaxData();
	}

	private void generateCategoryTaxData() {
		subCatTax.put("Phones", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("Covers", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("Power Banks", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("Tablets", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("Shirts", new String[] { TAX_CODES[1], "2500.0", "18", "-1" });
		subCatTax.put("Jeans", new String[] { TAX_CODES[1], "2500.0", "18", "-1" });
		subCatTax.put("Watches", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("Wallets", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("TV", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("Headphones", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("Speakers", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("Cameras", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("Lightning", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("Kitchen & Cooking", new String[] { TAX_CODES[1], "-1" });
		subCatTax.put("Garden & Outdoors", new String[] { TAX_CODES[1], "-1" });
		subCatTax.put("HomeDecor", new String[] { TAX_CODES[2], "-1" });
		subCatTax.put("Fiction", new String[] { TAX_CODES[0], "-1" });
		subCatTax.put("School", new String[] { TAX_CODES[0], "-1" });
		subCatTax.put("Exam", new String[] { TAX_CODES[0], "-1" });
		subCatTax.put("eBooks", new String[] { TAX_CODES[0], "-1" });
	}

	private void generateCategoryPriceRangeData() {
		subCatPriceRange.put("Phones", new Double[] { 10000.0, 100000.0 });
		subCatPriceRange.put("Covers", new Double[] { 400.0, 5000.0 });
		subCatPriceRange.put("Power Banks", new Double[] { 6000.0, 100000.0 });
		subCatPriceRange.put("Tablets", new Double[] { 15000.0, 100000.0 });
		subCatPriceRange.put("Shirts", new Double[] { 500.0, 10000.0 });
		subCatPriceRange.put("Jeans", new Double[] { 1500.0, 10000.0 });
		subCatPriceRange.put("Watches", new Double[] { 3000.0, 100000.0 });
		subCatPriceRange.put("Wallets", new Double[] { 3000.0, 50000.0 });
		subCatPriceRange.put("TV", new Double[] { 20000.0, 100000.0 });
		subCatPriceRange.put("Headphones", new Double[] { 1000.0, 10000.0 });
		subCatPriceRange.put("Speakers", new Double[] { 2000.0, 70000.0 });
		subCatPriceRange.put("Cameras", new Double[] { 5000.0, 100000.0 });
		subCatPriceRange.put("Lightning", new Double[] { 3000.0, 100000.0 });
		subCatPriceRange.put("Kitchen & Cooking", new Double[] { 10000.0, 500000.0 });
		subCatPriceRange.put("Garden & Outdoors", new Double[] { 10000.0, 100000.0 });
		subCatPriceRange.put("HomeDecor", new Double[] { 10000.0, 200000.0 });
		subCatPriceRange.put("Fiction", new Double[] { 300.0, 5000.0 });
		subCatPriceRange.put("School", new Double[] { 100.0, 1000.0 });
		subCatPriceRange.put("Exam", new Double[] { 1000.0, 10000.0 });
		subCatPriceRange.put("eBooks", new Double[] { 100.0, 10000.0 });
	}

	public static void main(String[] args) throws Exception {
		new GenerateItemData(args).generate();
	}

	private void validateArgs(String[] args) {
		if (args != null && args.length > 0) {
			try {
				System.out.println("Generating item count " + args[0]);
				itemCount = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Generate a flat file with generate item data rows. File format is::: "skuID
	 * price currency maxQty taxCode desc imgurl brand category subcategory"
	 * 
	 * @throws Exception
	 */
	private void generate() throws Exception {

		// generate items with randomized values
		List<String> itemList = IntStream.range(0, itemCount).parallel().mapToObj(counter -> {

			String category = getRandomCategory();
			String subcategory = getRandomSubCategory(category);
			double price = getRandomPrice(subcategory);
			String taxCode = getTaxCodeForSubCategory(subcategory, price);
			int maxQty = (int) (random.nextDouble() * 11.0);

			StringBuffer sb = new StringBuffer();

			sb.append("sku" + counter);
			sb.append(FIELD_SPACING);
			sb.append(df.format(price));
			sb.append(FIELD_SPACING);
			sb.append("INR");
			sb.append(FIELD_SPACING);
			sb.append(maxQty);
			sb.append(FIELD_SPACING);
			sb.append(taxCode);
			sb.append(FIELD_SPACING);
			sb.append(subcategory + " " + counter + " " + category);
			sb.append(FIELD_SPACING);
			sb.append(subcategory + ".jpg");
			sb.append(FIELD_SPACING);
			sb.append("Brand" + counter);
			sb.append(FIELD_SPACING);
			sb.append(category);
			sb.append(FIELD_SPACING);
			sb.append(subcategory);

			return sb.toString();
		}).collect(Collectors.toList());

		try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File("itemData")))) {
			itemList.stream().forEach(s -> {
				try {
					bos.write((s + System.lineSeparator()).getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private String getTaxCodeForSubCategory(String subcategory, double price) {
		String outputTaxCode = null;

		String[] taxArray = subCatTax.get(subcategory);
		for (int counter = 0; counter < taxArray.length; counter = counter + 2) {
			String taxCode = taxArray[counter];
			String upperLimit = taxArray[counter + 1];
			if (upperLimit.equals("-1") || (Double.parseDouble(upperLimit) <= price)) {
				outputTaxCode = taxCode;
				break;
			}
		}

		return outputTaxCode;
	}

	private double getRandomPrice(String subcategory) {
		Double[] range = subCatPriceRange.get(subcategory);
		Double lowerRange = range[0];
		Double upperRange = range[1];
		Double diff = upperRange - lowerRange;
		Double price = lowerRange + (diff * random.nextDouble());
		return price;
	}

	private String getRandomSubCategory(String category) {
		String[] subCategoryArray = SUB_CATEGORIES.get(category);

		int index = (int) (random.nextDouble() * (subCategoryArray.length));
		if (index >= subCategoryArray.length)
			index = subCategoryArray.length - 1;

		return subCategoryArray[index];
	}

	private String getRandomCategory() {
		int index = (int) (random.nextDouble() * (CATEGORIES.length));
		if (index >= CATEGORIES.length)
			index = CATEGORIES.length - 1;

		return CATEGORIES[index];
	}
}
