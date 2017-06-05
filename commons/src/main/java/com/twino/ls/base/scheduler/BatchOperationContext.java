package com.twino.ls.base.scheduler;


import com.twino.ls.base.model.OperationLog;

import java.time.LocalDateTime;

public class BatchOperationContext {

	private LocalDateTime when;

	private OperationLog operationLog;

	public BatchOperationContext(LocalDateTime when, OperationLog operationLog) {
		this.when = when;
		this.operationLog = operationLog;
	}

	public LocalDateTime getWhen() {
		return when;
	}

	public OperationLog getOperationLog() {
		return operationLog;
	}
}
