package com.twino.ls.api

enum class TermUnit {
	MONTHS, DAYS
}

data class Term(val value: Int, val unit: TermUnit) {

	companion object Factory {
		@JvmStatic
		fun months(value: Int): Term = Term(value, TermUnit.MONTHS)

		@JvmStatic
		fun days(value: Int): Term = Term(value, TermUnit.DAYS)
	}

}