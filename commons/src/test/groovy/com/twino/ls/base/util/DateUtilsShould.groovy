package com.twino.ls.base.util

import spock.lang.Specification

class DateUtilsShould extends Specification {

	void cleanup() {
		DateTimeUtils.setDeltaHours(0)
	}

	def "isLe"() {
		expect:
		DateTimeUtils.isLe(DateTimeUtils.date("2010-01-01"), DateTimeUtils.date("2010-01-01"))
		DateTimeUtils.isLe(DateTimeUtils.date("2010-01-01"), DateTimeUtils.date("2010-01-02"))
		!DateTimeUtils.isLe(DateTimeUtils.date("2010-01-02"), DateTimeUtils.date("2010-01-01"))

		DateTimeUtils.isLe(DateTimeUtils.dateTime("2010-01-01T01:15:00"), DateTimeUtils.dateTime("2010-01-01T01:15:00"))
		DateTimeUtils.isLe(DateTimeUtils.date("2010-01-01").atStartOfDay(), DateTimeUtils.date("2010-01-02").atStartOfDay())
		!DateTimeUtils.isLe(DateTimeUtils.date("2010-01-02").atStartOfDay(), DateTimeUtils.date("2010-01-01").atStartOfDay())
	}

	def "isL"() {
		expect:
		!DateTimeUtils.isL(DateTimeUtils.date("2010-01-01"), DateTimeUtils.date("2010-01-01"))
		DateTimeUtils.isL(DateTimeUtils.date("2010-01-01"), DateTimeUtils.date("2010-01-02"))
		!DateTimeUtils.isL(DateTimeUtils.date("2010-01-02"), DateTimeUtils.date("2010-01-01"))

		!DateTimeUtils.isL(DateTimeUtils.dateTime("2010-01-01T01:15:00"), DateTimeUtils.dateTime("2010-01-01T01:15:00"))
		DateTimeUtils.isL(DateTimeUtils.date("2010-01-01").atStartOfDay(), DateTimeUtils.date("2010-01-02").atStartOfDay())
		!DateTimeUtils.isL(DateTimeUtils.date("2010-01-02").atStartOfDay(), DateTimeUtils.date("2010-01-01").atStartOfDay())
	}

	def "isGe()"() {
		expect:
		DateTimeUtils.isGe(DateTimeUtils.date("2010-01-02"), DateTimeUtils.date("2010-01-01"))
		DateTimeUtils.isGe(DateTimeUtils.date("2010-01-02"), DateTimeUtils.date("2010-01-02"))
		!DateTimeUtils.isGe(DateTimeUtils.date("2010-01-01"), DateTimeUtils.date("2010-01-02"))

		DateTimeUtils.isGe(DateTimeUtils.dateTime("2010-01-01T01:15:00"), DateTimeUtils.dateTime("2010-01-01T01:15:00"))
		DateTimeUtils.isGe(DateTimeUtils.date("2010-01-02").atStartOfDay(), DateTimeUtils.date("2010-01-02").atStartOfDay())
		!DateTimeUtils.isGe(DateTimeUtils.date("2010-01-01").atStartOfDay(), DateTimeUtils.date("2010-01-02").atStartOfDay())
	}

	def "isG"() {
		expect:
		DateTimeUtils.isG(DateTimeUtils.date("2010-01-02"), DateTimeUtils.date("2010-01-01"))
		!DateTimeUtils.isG(DateTimeUtils.date("2010-01-02"), DateTimeUtils.date("2010-01-02"))
		!DateTimeUtils.isG(DateTimeUtils.date("2010-01-01"), DateTimeUtils.date("2010-01-02"))

		DateTimeUtils.isG(DateTimeUtils.dateTime("2010-01-02T01:15:00"), DateTimeUtils.dateTime("2010-01-01T01:15:00"))
		!DateTimeUtils.isG(DateTimeUtils.date("2010-01-02").atStartOfDay(), DateTimeUtils.date("2010-01-02").atStartOfDay())
		!DateTimeUtils.isG(DateTimeUtils.date("2010-01-01").atStartOfDay(), DateTimeUtils.date("2010-01-02").atStartOfDay())
	}

}
