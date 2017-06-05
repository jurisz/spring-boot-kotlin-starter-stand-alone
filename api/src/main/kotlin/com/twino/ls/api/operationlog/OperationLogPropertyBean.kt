package com.twino.ls.api.operationlog

import java.time.LocalDateTime

data class OperationLogPropertyBean(var id: Long = 0,
									var name: String = "",
									var value: String = "",
									var created: LocalDateTime = LocalDateTime.now())