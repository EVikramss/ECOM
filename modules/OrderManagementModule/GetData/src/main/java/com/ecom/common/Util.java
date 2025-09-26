package com.ecom.common;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;

import com.ecom.enums.DefaultTimeStampType;
import com.ecom.enums.QueryJoinType;
import com.querydsl.core.types.dsl.BooleanExpression;

public class Util {

	private static Random random = new Random();

	private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
	private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");

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

	/**
	 * Get timestamp from String, in case unable to - based on defaultValueIndicator
	 * fetch either past or future date
	 * 
	 * @param fromDate
	 * @param defaultValueIndicator
	 * @return
	 */
	public static Timestamp getTimeStampFromDate(String fromDate, DefaultTimeStampType defaultTimeStampType) {
		Timestamp output = null;
		if (isValidString(fromDate)) {
			Date parsedDate;
			try {
				parsedDate = sdf1.parse(fromDate);
				output = new Timestamp(parsedDate.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		if (output == null) {
			if (DefaultTimeStampType.PAST.equals(defaultTimeStampType)) {
				output = Timestamp.valueOf(LocalDateTime.of(1970, 1, 1, 0, 0));
			} else if (DefaultTimeStampType.TOMORROW.equals(defaultTimeStampType)) {
				LocalDate tomorrow = LocalDate.now().plusDays(1);
				output = Timestamp.valueOf(tomorrow.atStartOfDay());
			} else if (DefaultTimeStampType.FUTURE.equals(defaultTimeStampType)) {
				output = Timestamp.valueOf(LocalDateTime.of(2100, 1, 1, 0, 0));
			}
		}

		return output;
	}

	public static BooleanExpression appendDSLExpression(BooleanExpression finalQueryExpression,
			BooleanExpression queryExpression, QueryJoinType joinType) {
		return finalQueryExpression == null ? queryExpression
				: (QueryJoinType.And.equals(joinType) ? finalQueryExpression.and(queryExpression)
						: finalQueryExpression.or(queryExpression));
	}
}
