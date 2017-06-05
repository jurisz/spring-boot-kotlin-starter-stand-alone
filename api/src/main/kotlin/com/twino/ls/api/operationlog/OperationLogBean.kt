package com.twino.ls.api.operationlog

import com.google.common.collect.Lists.newArrayList
import java.time.LocalDateTime


data class OperationLogBean(var id: Long = 0,
							var type: String = "",
							var title: String = "") {
	var startTime: LocalDateTime = LocalDateTime.now()
	var endTime: LocalDateTime = LocalDateTime.now()
	var created: LocalDateTime = LocalDateTime.now()
	var formattedDuration: String = ""
	var properties: List<OperationLogPropertyBean> = newArrayList()
}