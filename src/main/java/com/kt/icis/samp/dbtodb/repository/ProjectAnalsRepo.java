package com.kt.icis.samp.dbtodb.repository;

import com.kt.icis.samp.dbtodb.repository.dto.ProjectAnalsDto;
import org.springframework.data.repository.PagingAndSortingRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectAnalsRepo extends
    PagingAndSortingRepository<ProjectAnalsDto, String> {

    List<ProjectAnalsDto> findByPjtId(String id);

    List<ProjectAnalsDto> findAll();

    Optional<ProjectAnalsDto> findTopByPjtIdOrderByChgDtDesc(String id);
}
