package com.kt.icis.samp.dbtodb.api;

import com.kt.icis.cmmnfrwk.batch.BatchJobOperator;
import com.kt.icis.samp.dbtodb.payload.in.BatchSampInPyId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.Callable;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BatchSampApi {

    private final BatchJobOperator operator;

    @PostMapping("/job/sync")
    public Callable<Object> runJob(@RequestBody BatchSampInPyId batchSampInPyId)  {
        return () -> {
            operator.start(batchSampInPyId.getJobNm());
            return null;
        };
    }

}
