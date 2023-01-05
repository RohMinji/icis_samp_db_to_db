package com.kt.icis.samp.dbtodb.step;

import com.kt.icis.cmmnfrwk.batch.BatchDbItemReader;
import com.kt.icis.samp.dbtodb.repository.ProjectAnalsRepo;
import com.kt.icis.samp.dbtodb.repository.dto.ProjectAnalsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Component
public class BatchSampDbItemReader extends BatchDbItemReader<ProjectAnalsDto> {

    private final ProjectAnalsRepo projectAnalsRepo;

    @Value("${reader-info.methodNm}")
    private String methodName;

    @Value("${reader-info.chunkSize}")
    private int chunkSize;

    @Value("${reader-info.arguments}")
    private String arguments;

    private Map<String, Sort.Direction> sort;

    public ItemReader<? extends ProjectAnalsDto> reader() {
        return super.reader(projectAnalsRepo, methodName, chunkSize);
    }
}
