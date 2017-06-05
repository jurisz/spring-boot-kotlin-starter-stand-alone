package com.twino.ls.base.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class HumanStringJoiner {

	private final Joiner joiner;

	/**
	 * guava joiner with skipNulls and skipEmpty
	 *
	 * @param separator
	 * @return
	 * @see Joiner
	 */
	public static HumanStringJoiner on(String separator) {
		return new HumanStringJoiner(separator);
	}

	private HumanStringJoiner(String separator) {
		joiner = Joiner.on(separator).skipNulls();
	}

	public final String join(Iterable<String> parts) {
		Iterable<String> notEmptyStrings = Iterables.filter(parts, StringUtils::isNotBlank);
		return joiner.join(notEmptyStrings);
	}

	public final String join(String[] parts) {
		return join(Arrays.asList(parts));
	}

	public final String join(String first, String... rest) {
		List<String> el = newArrayList(first);
		el.addAll(newArrayList(rest));
		return join(el);
	}

}
