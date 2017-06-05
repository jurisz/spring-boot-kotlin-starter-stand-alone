package com.twino.ls.base.scheduler;

import com.twino.ls.base.ReloadableProperties;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;

import static com.twino.ls.base.util.DateTimeUtils.date;
import static com.twino.ls.base.util.DateTimeUtils.now;


/**
 * Implementation need to extend class and define BatchJobDefinition and inject it
 * add Mbean definition to expose JMX
 * using project needs to have ReloadableProperties for configuration
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public abstract class AbstractScheduler implements Job {
	private static final String ENABLED_PROPERTY = "enabled";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	protected ReloadableProperties loansProperties;

	@Autowired
	protected PlatformTransactionManager transactionManager;

	protected abstract void execute(LocalDateTime when);

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
			logger.info("Scheduler: {} disabled", schedulerName());
			return;
		}
		execute();
	}

	@ManagedOperation
	public boolean isEnabled() {
		return "true".equals(getSchedulerPropertyValue(ENABLED_PROPERTY));
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

	protected String getSchedulerPropertyValue(String property) {
		String propertyName = String.format("scheduler.%s.%s", schedulerName(), property);
		return loansProperties.getProperty(propertyName, null);
	}

	protected String schedulerName() {
		return getClass().getSimpleName();
	}
}
