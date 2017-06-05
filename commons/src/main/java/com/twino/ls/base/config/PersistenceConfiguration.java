package com.twino.ls.base.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.sf.ehcache.CacheManager;
import org.hibernate.cache.ehcache.EhCacheRegionFactory;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.net.URISyntaxException;
import java.util.Properties;

@EnableTransactionManagement
@Configuration
@EnableJpaRepositories(basePackages = {"com.twino.ls"})
class PersistenceConfiguration {

	String[] PERSISTANCE_PACKAGES = {"com.twino.ls"};

	// @formatter:off
	@Autowired
	@Bean(destroyMethod = "close")
	public DataSource dataSource(
			//Heroku database url
			@Value("${db.host}") String host,
			@Value("${db.port:5432}") int port,
			@Value("${db.dbname}") String dbName,
			@Value("${db.user}") String user,
			@Value("${db.password}") String password,
			@Value("${db.dataSourceClass:org.postgresql.ds.PGSimpleDataSource}") String dataSourceClassName,
			@Value("${db.maximumPoolSize:10}") int maxPoolSize,
			@Value("${db.idleTimeoutSec:60}") int idleTimeoutSec
	) throws PropertyVetoException, URISyntaxException {
		// @formatter:on
		HikariConfig config = new HikariConfig();
		config.setMaximumPoolSize(maxPoolSize);
		config.setDataSourceClassName(dataSourceClassName);
		config.addDataSourceProperty("serverName", host);
		config.addDataSourceProperty("portNumber", port);
		config.addDataSourceProperty("databaseName", dbName);
		config.addDataSourceProperty("user", user);
		config.addDataSourceProperty("password", password);
		config.setIdleTimeout(idleTimeoutSec * 1000);
		config.setAutoCommit(false);
		return new HikariDataSource(config);
	}

	// @formatter:off
	@Bean
	public Properties hibernateProperties(
			@Value("${hibernate.hbm2ddl.auto}") String hbm2ddl,
			@Value("${hibernate.show_sql}") boolean showSql,
			@Value("${db.fetchSize:0}") int fetchSize,
			@Value("${hibernate.dialect:org.hibernate.dialect.PostgreSQLDialect}") String dialect
	) {
		// @formatter:on
		Properties properties = new Properties();
		properties.put("hibernate.dialect", dialect);
		properties.put("hibernate.show_sql", showSql);

		properties.put("hibernate.hbm2ddl.auto", hbm2ddl);
		properties.put("hibernate.jdbc.fetch_size", fetchSize);
		properties.put("hibernate.order_updates", true);
		properties.put("hibernate.cache.use_second_level_cache", true);
		properties.put("hibernate.cache.use_query_cache", true);
		properties.put("hibernate.cache.region.factory_class", EhCacheRegionFactory.class.getName());
		properties.put("hibernate.ejb.naming_strategy", ImprovedNamingStrategy.class.getName());
		properties.put("org.hibernate.envers.audit_table_prefix", "AUDIT_");
		properties.put("org.hibernate.envers.audit_table_suffix", "");

		return properties;
	}

	@Bean
	LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, @Qualifier("hibernateProperties") Properties properties) {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(dataSource);
		entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		entityManagerFactoryBean.setPackagesToScan(PERSISTANCE_PACKAGES);
		entityManagerFactoryBean.setJpaProperties(properties);
		return entityManagerFactoryBean;
	}

	@Bean
	PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}

	@Bean
	public CacheManager cacheManager() throws Exception {
		EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
		cacheManagerFactoryBean.setAcceptExisting(true);
		cacheManagerFactoryBean.afterPropertiesSet();
		return cacheManagerFactoryBean.getObject();
	}
}
