package com.sample.spring.batch.payments.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;

/**
 * Interface for Infrastructure Configuration
 */
public interface InfrastructureConfiguration {

	@Bean
	public abstract DataSource dataSource();
	
}
