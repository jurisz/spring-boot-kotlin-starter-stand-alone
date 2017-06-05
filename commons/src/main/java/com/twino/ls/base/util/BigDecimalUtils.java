package com.twino.ls.base.util;

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class BigDecimalUtils {

	public static final int AMOUNT_SCALE = 2;

	public static final int MONEY_ROUND = ROUND_HALF_UP;

	public static final BigDecimal ZERO_AMOUNT = amount(0);

	private BigDecimalUtils() {
	}

	public static BigDecimal amount(BigDecimal amount) {
		return amount.setScale(AMOUNT_SCALE, MONEY_ROUND);
	}

	public static BigDecimal amount(int value) {
		return amount(new BigDecimal(value));
	}

	public static BigDecimal amount(double value) {
		return amount(new BigDecimal(value));
	}

	public static BigDecimal amount(String value) {
		if (isBlank(value)) {
			return null;
		}
		return amount(new BigDecimal(value.trim()));
	}

	public static boolean isEqual(BigDecimal value1, BigDecimal value2) {
		return value1.compareTo(value2) == 0;
	}

	public static boolean isLe(BigDecimal left, BigDecimal right) {
		return left.compareTo(right) <= 0;
	}

	public static boolean isGe(BigDecimal left, BigDecimal right) {
		return left.compareTo(right) >= 0;
	}

	public static boolean isG(BigDecimal left, BigDecimal right) {
		return left.compareTo(right) > 0;
	}

	public static boolean isL(BigDecimal left, BigDecimal right) {
		return left.compareTo(right) < 0;
	}

	public static boolean isInRange(BigDecimal amount, BigDecimal amountFrom, BigDecimal amountTo) {
		return isGe(amount, amountFrom) && isLe(amount, amountTo);
	}

	public static boolean isPositiveAmount(BigDecimal amount) {
		if (amount == null) {
			return false;
		}
		return isGe(amount, amount(0.01));
	}

	public static boolean isNegativeAmount(BigDecimal amount) {
		if (amount == null) {
			return false;
		}
		return isL(amount, ZERO_AMOUNT);
	}

	public static boolean isZero(BigDecimal value) {
		return isEqual(ZERO_AMOUNT, value);
	}

	public static BigDecimal round(BigDecimal amount, int scale) {
		return amount(amount.setScale(scale, BigDecimal.ROUND_HALF_UP));
	}
}
