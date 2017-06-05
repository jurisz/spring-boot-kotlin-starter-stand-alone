package com.twino.ls.base.scheduler;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.text.ParseException;
import java.util.Properties;

public class QuartzSchedulerConfigurationHelper {

	private static final String SCHEDULER_INSTANCE_NAME = "ApplicationScheduler";

	public static Trigger createCronTrigger(Class<?> schedulerClass, String cronExpression) throws ParseException {
		CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
		trigger.setJobDetail(jobDetail(schedulerClass));
		trigger.setCronExpression(cronExpression);
		trigger.setName(schedulerClass.getSimpleName());
		trigger.afterPropertiesSet();
		return trigger.getObject();
	}

	public static Trigger simpleTrigger(Class<?> schedulerClass, long repeatInterval, long startDelay) throws ParseException {
		SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setJobDetail(jobDetail(schedulerClass));
		trigger.setName(schedulerClass.getSimpleName());
		trigger.setRepeatInterval(repeatInterval);
		trigger.setStartDelay(startDelay);
		trigger.afterPropertiesSet();
		return trigger.getObject();
	}

	private static JobDetail jobDetail(Class<?> schedulerClass) {
		JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
		jobDetail.setJobClass(schedulerClass);
		jobDetail.setName(schedulerClass.getSimpleName());
		jobDetail.setDurability(true);
		jobDetail.afterPropertiesSet();
		return jobDetail.getObject();
	}

	public static SchedulerFactoryBean createJdbcSchedulerFactory(Trigger[] triggers, ApplicationContext applicationContext, DataSource dataSource, PlatformTransactionManager txManager)
			throws Exception {
		SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
		factoryBean.setTriggers(triggers);
		factoryBean.setDataSource(dataSource);
		factoryBean.setTransactionManager(txManager);
		factoryBean.setStartupDelay(20);
		factoryBean.setQuartzProperties(quartzJdbcProperties());
		factoryBean.setApplicationContext(applicationContext);
		factoryBean.setApplicationContextSchedulerContextKey("applicationContext");
		factoryBean.setSchedulerName(SCHEDULER_INSTANCE_NAME);
		return factoryBean;
	}

	private static Properties quartzJdbcProperties() {
		Properties properties = new Properties();
		properties.put("org.quartz.scheduler.instanceName", SCHEDULER_INSTANCE_NAME);
		properties.put("org.quartz.scheduler.instanceId", "AUTO");
		properties.put("org.quartz.jobStore.misfireThreshold", "60000");
		properties.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
		properties.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
		properties.put("org.quartz.jobStore.useProperties", "false");

		properties.put("org.quartz.jobStore.tablePrefix", "qrtz_");
		properties.put("org.quartz.jobStore.isClustered", "true");
		properties.put("org.quartz.jobStore.clusterCheckinInterval", "20000");
		properties.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
		properties.put("org.quartz.threadPool.threadCount", "10");
		properties.put("org.quartz.threadPool.threadPriority", "5");
		return properties;
	}

}
