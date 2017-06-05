package com.twino.ls.base.scheduler;

import java.util.Collection;

public interface BatchJobDefinition<T> {

	void onJobStart(BatchOperationContext context);

	void onJobEnd(BatchOperationContext context);

	Collection<T> readItems(int startRow, int batchSize, BatchOperationContext context);

	boolean operateOnOneBatch(Collection<T> items, BatchOperationContext context);

	void operateOnItem(Long itemId, BatchOperationContext context);

	void onItemFailure(Long itemId, BatchOperationContext context);
}
