package com.ecom.common;

import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Util {

	private static Random random = new Random();

	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

	public static BigInteger generateKey() {
		long randomVal = Math.round(random.nextDouble() * 1000000.0);
		ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of("UTC"));
		String keyVal = dateTime.format(dtf) + randomVal;
		int lengthDiff = 23 - keyVal.length();
		return new BigInteger(keyVal + "0".repeat(lengthDiff));
	}

	public static boolean isValidString(String value) {
		return value != null && value.trim().length() > 0;
	}
}
