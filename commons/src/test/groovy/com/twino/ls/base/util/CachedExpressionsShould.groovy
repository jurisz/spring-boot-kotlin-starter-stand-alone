package com.twino.ls.base.util

import spock.lang.Specification

class CachedExpressionsShould extends Specification {
	private CachedExpressions expressions

	void setup() {
		expressions = new CachedExpressions()
	}

	def "value reading"() {
		given:
		Foo foo = new Foo()
		foo.bar = new Bar()
		foo.bar.prop = "abc"

		when:
		String value = expressions.getValue(foo, "bar.prop")

		then:
		"abc" == value

	}

	def "null reading works"() {
		given:
		Foo foo = new Foo()

		expect:
		null == expressions.getValue(foo, "bar.prop")
	}


	def "set value"() {
		given:
		Foo foo = new Foo()
		foo.bar = new Bar()
		foo.bar.prop = "abc"

		when:
		expressions.setValue(foo, "bar.prop", "cba")

		then:
		"cba" == foo.bar.prop
	}

	private class Foo {
		private Bar bar

		public Bar getBar() {
			return bar
		}
	}

	private class Bar {
		private String prop

		public String getProp() {
			return prop
		}

		public void setProp(String prop) {
			this.prop = prop
		}
	}
}
