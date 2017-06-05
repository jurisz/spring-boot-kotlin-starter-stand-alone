package com.twino.ls.base.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtils {

	private static final Logger log = LoggerFactory.getLogger(DateTimeUtils.class);

	private static int deltaHours;

	private DateTimeUtils() {
	}

	public static String getTimestamp() {
		DateTimeFormatter sdf = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
		return sdf.format(LocalDateTime.now());
	}

	public static String getTimestamp(String format) {
		DateTimeFormatter sdf = DateTimeFormatter.ofPattern(format);
		return sdf.format(LocalDateTime.now());
	}

	/**
	 * @param dateString in format yyyy-MM-dd
	 * @return
	 */
	public static LocalDate date(String dateString) {
		if (Strings.isNullOrEmpty(dateString)) {
			return null;
		}
		return LocalDate.parse(dateString);
	}

	/**
	 * @param dateString in format yyyy-MM-ddTHH:mm:ss
	 * @return
	 */
	public static LocalDateTime dateTime(String dateString) {
		if (Strings.isNullOrEmpty(dateString)) {
			return null;
		}
		return LocalDateTime.parse(dateString);
	}

	public static LocalDate today() {
		return now().toLocalDate();
	}

	public static LocalDateTime now() {
		return LocalDateTime.now().plusHours(deltaHours);
	}

	public static int getDeltaHours() {
		return deltaHours;
	}

	public static void setDeltaHours(int deltaHours) {
		Preconditions.checkNotNull(deltaHours, "Delta hours is null");
		log.warn("Updating deltaHours to value {}, this will affect now() and today() return values", deltaHours);
		DateTimeUtils.deltaHours = deltaHours;
	}

	public static boolean isLe(LocalDate date1, LocalDate date2) {
		Preconditions.checkNotNull(date1, "date1 is null");
		Preconditions.checkNotNull(date2, "date2 is null");
		return date1.compareTo(date2) <= 0;
	}

	public static boolean isL(LocalDate date1, LocalDate date2) {
		Preconditions.checkNotNull(date1, "date1 is null");
		Preconditions.checkNotNull(date2, "date2 is null");
		return date1.compareTo(date2) < 0;
	}

	public static boolean isGe(LocalDate date1, LocalDate date2) {
		return isLe(date2, date1);
	}

	public static boolean isG(LocalDate date1, LocalDate date2) {
		return isL(date2, date1);
	}

	public static boolean isLe(LocalDateTime date1, LocalDateTime date2) {
		Preconditions.checkNotNull(date1, "date1 is null");
		Preconditions.checkNotNull(date2, "date2 is null");
		return date1.compareTo(date2) <= 0;
	}

	public static boolean isL(LocalDateTime date1, LocalDateTime date2) {
		Preconditions.checkNotNull(date1, "date1 is null");
		Preconditions.checkNotNull(date2, "date2 is null");
		return date1.compareTo(date2) < 0;
	}

	public static boolean isGe(LocalDateTime date1, LocalDateTime date2) {
		return isLe(date2, date1);
	}

	public static boolean isG(LocalDateTime date1, LocalDateTime date2) {
		return isL(date2, date1);
	}

	public static <T extends ChronoLocalDate> T max(T one, T two) {
		return one.compareTo(two) > 0 ? one : two;
	}

	public static <T extends ChronoLocalDate> T min(T one, T two) {
		return one.compareTo(two) < 0 ? one : two;
	}

	public static LocalDate dateOrTodayOnEmpty(String date) {
		return StringUtils.isNotEmpty(date) ? date(date) : today();
	}

	public static LocalDate dateOrTodayOnNull(LocalDate date) {
		return date != null ? date : today();
	}

	public static LocalDateTime dateTimeOrNowOnEmpty(String date) {
		if (StringUtils.isNotEmpty(date)) {
			if (date.length() == 10) {
				return date(date).atStartOfDay();
			} else {
				return dateTime(date);
			}
		}
		return now();
	}

	public static LocalDateTime dateTimeOrNowOnNull(LocalDateTime date) {
		return date != null ? date : now();
	}

}
