package com.twino.ls.base.scheduler;

import java.util.Collection;

public abstract class BatchJobDefinitionAdapter<T> implements BatchJobDefinition<T> {
	@Override
	public void onJobStart(BatchOperationContext context) {
	}

	@Override
	public void onJobEnd(BatchOperationContext context) {
	}

	@Override
	public boolean operateOnOneBatch(Collection<T> items, BatchOperationContext context) {
		return false;
	}

	@Override
	public void onItemFailure(Long itemId, BatchOperationContext context) {
	}
}
