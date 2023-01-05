package com.kt.icis.samp.dbtodb.config;

import com.kt.icis.cmmnfrwk.batch.BatchConfigurer;
import com.kt.icis.cmmnfrwk.batch.BatchJobOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchSampConfig {

    private final DataSource dataSource;

    private final JobRegistry jobRegistry;

    private final JobLauncher jobLauncher;

    private final JobExplorer jobExplorer;

    private final JobRepository jobRepository;

    @Bean
    public BeanPostProcessor jobRegistryBean() throws Exception {
        JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
        postProcessor.setJobRegistry(jobRegistry);
        postProcessor.afterPropertiesSet();

        return postProcessor;
    }

    @Bean
    public BatchConfigurer batchConfigurer() {
        BatchConfigurer configurer = new BatchConfigurer();
        configurer.setDataSource(dataSource);
        configurer.setDatabaseType(DatabaseType.ORACLE);

        return configurer;
    }

    @Bean
    public BatchJobOperator batchJobOperator(){
        return new BatchJobOperator(jobLauncher,jobExplorer,jobRepository,jobRegistry);
    }

}
