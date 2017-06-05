package com.twino.ls.base.scheduler;

import com.google.common.base.Preconditions;
import com.twino.ls.base.ReloadableProperties;
import com.twino.ls.base.model.BaseEntity;
import com.twino.ls.base.model.OperationLog;
import com.twino.ls.base.model.OperationLogRepository;
import com.twino.ls.base.security.SecurityHelper;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.beans.Introspector;
import java.time.LocalDateTime;
import java.util.Collection;

import static com.twino.ls.base.util.DateTimeUtils.date;
import static com.twino.ls.base.util.DateTimeUtils.now;


/**
 * Implementation need to extend class and define BatchJobDefinition and inject it
 * add Mbean definition to expose JMX
 * using project needs to have ReloadableProperties for configuration
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public abstract class AbstractBatchScheduler<E extends BaseEntity> implements Job {

	private static final String ENABLED_PROPERTY = "enabled";
	private static final String LOGGING_ENABLED_PROPERTY = "log.enabled";
	private static final String PERSIST_OPERATION_LOG_PROPERTY = "persist.operation.log";
	private static final String MAX_ITEMS_PROPERTY = "max.items";
	private static final String MAX_FAILURES_PROPERTY = "max.failures";
	private static final String BATCH_SIZE_PROPERTY = "batch.size";
	private static final int MAX_FAILURES_DEFAULT = 2000;
	private static final int BATCH_SIZE_DEFAULT = 1000;
	public static final String OPERATION_LOG_TYPE_SCHEDULER = "SCHEDULER";

	@Autowired
	private ReloadableProperties loansProperties;

	@Autowired
	private OperationLogRepository operationLogRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Autowired
	private ListableBeanFactory applicationContext;

	private BatchJobDefinition<E> batchJobDefinition;

	private BatchOperationContext batchOperationContext;
	private int maxFailures;
	private int maxItems;
	private int batchSize;
	private int itemsProcessed;
	private int itemsFailed;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			SchedulerContext schedulerCtx = context.getScheduler().getContext();
			ApplicationContext ctx = (ApplicationContext) schedulerCtx.get("applicationContext");
			ctx.getAutowireCapableBeanFactory().autowireBean(this);
		} catch (SchedulerException ex) {
			throw new JobExecutionException(ex);
		}
		if (!isEnabled()) {
			if (isLogInfoEnabled()) {
				logger().info("Scheduler: {} disabled", schedulerName());
			}
			return;
		}
		execute();
	}

	@ManagedOperation
	public final void execute() {
		execute(now());
	}

	@ManagedOperation
	public final void execute(String dateString) {
		LocalDateTime when = StringUtils.isBlank(dateString) ? now() : date(dateString).atStartOfDay();
		execute(when);
	}

	public void execute(LocalDateTime when) {
		readProperties();
		initializeJobBatchDefinition();
		boolean logInfoEnabled = isLogInfoEnabled();
		if (logInfoEnabled) {
			logger().info("Started scheduled job: {}", schedulerName());
		}
		boolean newLoginDone = securityContextLogin(schedulerName());
		createOperationContext(when);

		int startRow = 0;
		int rowCount;
		itemsProcessed = 0;
		itemsFailed = 0;
		int iterationCount = 0;

		try {
			batchJobDefinition.onJobStart(batchOperationContext);
			do {
				Collection<E> batch = readBatch(startRow, batchSize);
				iterationCount++;
				rowCount = batch.size();
				if (logInfoEnabled) {
					logger().info("run job iteration : {}, returned rowCount: {}", iterationCount, rowCount);
				}
				executeOnBatch(batch);
				startRow = startRow + batchSize;
				if (maxItemsProcessedLimitReached()) {
					logger().info("Max processed items reached, exiting");
					return;
				}
				if (maxFailuresLimitReached()) {
					logger().info("Max failures reached, exiting");
					return;
				}
			} while (rowCount != 0);

			batchJobDefinition.onJobEnd(batchOperationContext);

		} finally {
			persistOperationContext();
			if (newLoginDone) {
				securityContextLogout();
			}
		}
	}

	private Collection<E> readBatch(int startRow, int batchSize) {
		logger().debug("Reading batch data");
		TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
		txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		txTemplate.setReadOnly(true);
		Collection<E> items = txTemplate.execute((c) -> batchJobDefinition.readItems(startRow, batchSize, batchOperationContext));
		logger().debug("Loaded {} items", items.size());
		return items;
	}

	private void executeOnBatch(Collection<E> batch) {
		boolean batchFinished = batchJobDefinition.operateOnOneBatch(batch, batchOperationContext);
		if (!batchFinished) {
			for (E item : batch) {
				if (maxFailuresLimitReached() || maxItemsProcessedLimitReached()) {
					return;
				}
				executeSafely(item);
				itemsProcessed++;
			}
		}
	}

	private void executeSafely(E item) {
		try {
			TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
			txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			txTemplate.execute((c) -> {
				batchJobDefinition.operateOnItem(item.getId(), batchOperationContext);
				return null;
			});
		} catch (Exception e) {
			logger().error("Execution exception for: " + item, e);
			onItemFailureSafely(item);
			itemsFailed++;
		}
	}

	private void onItemFailureSafely(E item) {
		try {
			TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
			txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			txTemplate.execute((c) -> {
				batchJobDefinition.onItemFailure(item.getId(), batchOperationContext);
				return null;
			});
		} catch (Exception e) {
			logger().error("Failure handling failed too, for: " + item, e);
		}
	}

	private void createOperationContext(LocalDateTime when) {
		OperationLog operationLog = new OperationLog();
		operationLog.setType(OPERATION_LOG_TYPE_SCHEDULER);
		operationLog.setTitle(schedulerName());
		batchOperationContext = new BatchOperationContext(when, operationLog);
	}

	protected void persistOperationContext() {
		OperationLog operationLog = batchOperationContext.getOperationLog();
		operationLog.setEndDate(now());
		if (isLogInfoEnabled()) {
			logger().info("Finished scheduler: {} took: [ {} ], itemsProcessed: {}, itemsFailed {}",
					schedulerName(), operationLog.getFormattedDuration(), itemsProcessed, itemsFailed);
		}
		if (isOperationLogPersistEnabled()) {
			operationLog.addProperty("itemsProcessed", "" + itemsProcessed);
			operationLog.addProperty("itemsFailed", "" + itemsFailed);
			TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
			txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
			txTemplate.execute((c) -> operationLogRepository.save(operationLog));
		}
	}

	private synchronized boolean securityContextLogin(String schedulerName) {
		return SecurityHelper.setUser(StringUtils.abbreviate("scheduler=" + schedulerName, 100));
	}

	private synchronized void securityContextLogout() {
		SecurityHelper.removeUser();
	}

	private boolean maxFailuresLimitReached() {
		return itemsFailed > maxFailures;
	}

	private boolean maxItemsProcessedLimitReached() {
		return itemsProcessed > maxItems;
	}

	protected String schedulerName() {
		return getClass().getSimpleName();
	}

	private boolean isLogInfoEnabled() {
		//by default on
		return !"false".equals(getSchedulerPropertyValue(LOGGING_ENABLED_PROPERTY));
	}

	private boolean isOperationLogPersistEnabled() {
		//by default on
		return !"false".equals(getSchedulerPropertyValue(PERSIST_OPERATION_LOG_PROPERTY));
	}

	protected Logger logger() {
		return LoggerFactory.getLogger(getClass());
	}

	private void readProperties() {
		maxItems = getIntegerPropertyValue(MAX_ITEMS_PROPERTY, Integer.MAX_VALUE);
		batchSize = getIntegerPropertyValue(BATCH_SIZE_PROPERTY, BATCH_SIZE_DEFAULT);
		maxFailures = getIntegerPropertyValue(MAX_FAILURES_PROPERTY, MAX_FAILURES_DEFAULT);
	}

	@ManagedOperation
	public boolean isEnabled() {
		return "true".equals(getSchedulerPropertyValue(ENABLED_PROPERTY));
	}

	private String getSchedulerPropertyValue(String property) {
		String propertyName = String.format("scheduler.%s.%s", schedulerName(), property);
		return loansProperties.getProperty(propertyName, null);
	}

	private int getIntegerPropertyValue(String property, int defaultValue) {
		String value = getSchedulerPropertyValue(property);
		if (value != null) {
			try {
				return Integer.valueOf(value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		} else {
			return defaultValue;
		}
	}

	@SuppressWarnings("unchecked")
	private void initializeJobBatchDefinition() {
		String schedulerName = schedulerName();
		String jobName = Introspector.decapitalize(schedulerName.replace("Scheduler", "JobDefinition"));
		BatchJobDefinition<E> jobDefinition = applicationContext.getBean(jobName, BatchJobDefinition.class);
		Preconditions.checkNotNull(jobDefinition, "Job definition: {} bean not registered for scheduler: {}", jobName, schedulerName);
		this.batchJobDefinition = jobDefinition;
	}
}
