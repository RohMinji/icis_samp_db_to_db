/**************************************************************************************
 * ICIS TR version 1.0
 *
 *  Copyright ⓒ 2022 kt/ktds corp. All rights reserved.
 *
 *  This is a proprietary software of kt corp, and you may not use this file except in
 *  compliance with license agreement with kt corp. Any redistribution or use of this
 *  software, with or without modification shall be strictly prohibited without prior written
 *  approval of kt corp, and the copyright notice above does not evidence any actual or
 *  intended publication of such software.
 *************************************************************************************/
package com.kt.icis.samp.dbtodb.job;

import com.kt.icis.samp.dbtodb.step.BatchSampStepConfig;
import com.kt.icis.samp.dbtodb.step.BatchSampTaskletStepConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchSampJobConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final BatchSampStepConfig batchSampStep;
    private final BatchSampTaskletStepConfig batchSampTaskletStep;
    private final BatchSampJobListener listener;
    private final BatchSampJobValidator validator;

    /**
     * samp job description
     */
    @Bean
    public Job sampJob() {

        return jobBuilderFactory
                .get("sampJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(batchSampStep.sampStep())
                .next(batchSampTaskletStep.sampTasklet())
                .validator(validator)
                .build();
    }
}
