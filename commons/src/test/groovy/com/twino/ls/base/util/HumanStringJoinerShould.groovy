package com.twino.ls.base.util

import com.google.common.collect.Lists
import spock.lang.Specification

/**
 * Created by juris on 16.9.3.
 */
class HumanStringJoinerShould extends Specification {

	def "human joiner omits nulls and empty strings"() {
		given:
		String a = "a";
		String b = null;
		String c = "c";
		String d = " ";
		String e = "e";

		expect:
		HumanStringJoiner.on(" ").join(a, b, c, d, e) == "a c e"
		HumanStringJoiner.on(" ").join([a, b, c, d, e].toArray(new String[0])) == "a c e"
		HumanStringJoiner.on(" ").join(Lists.newArrayList(a, b, c, d, e)) == "a c e"
	}
}
