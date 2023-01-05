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

import com.kt.icis.cmmnfrwk.batch.BatchJobListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchSampJobListener extends BatchJobListener {

    @Override
    public void beforeExec(JobExecution ex) {

    }

    @Override
    public void afterExec(JobExecution ex) {

    }

}
