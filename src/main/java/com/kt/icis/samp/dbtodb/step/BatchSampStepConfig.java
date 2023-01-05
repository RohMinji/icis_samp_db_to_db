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
package com.kt.icis.samp.dbtodb.step;

import com.kt.icis.samp.dbtodb.repository.dto.ProjectAnalsDto;
import com.kt.icis.samp.dbtodb.repository.dto.ProjectDemonDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchSampStepConfig {

    private final BatchSampDbItemWriter writer;
    private final BatchSampDbItemReader reader;
    private final BatchSampItemProcessor processor;
    private final BatchSampStepListener listener;
    private final StepBuilderFactory stepBuilderFactory;

    @Value("${step-info.chunkSize}")
    private int CHUNK_SIZE;

    /**
     * samp step description
     */
    @Bean
    public Step sampStep()  {
        return stepBuilderFactory
                .get("sampStep")
                .<ProjectAnalsDto, ProjectDemonDto>chunk(CHUNK_SIZE)
                .reader(reader.reader())
                .processor(processor)
                .writer(writer.writer())
                .listener(listener)
                .build();
    }

}
