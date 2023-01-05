package com.kt.icis.samp.dbtodb.step;

import com.kt.icis.cmmnfrwk.batch.BatchDbItemWriter;
import com.kt.icis.samp.dbtodb.repository.ProjectDemonRepo;
import com.kt.icis.samp.dbtodb.repository.dto.ProjectDemonDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class BatchSampDbItemWriter extends BatchDbItemWriter<ProjectDemonDto> {

    private final ProjectDemonRepo projectDemonRepo;

    @Value("${writer-info.methodNm}")
    private String methodName;

    public ItemWriter<? super ProjectDemonDto> writer() {
        return super.writer(projectDemonRepo, methodName);
    }
}
