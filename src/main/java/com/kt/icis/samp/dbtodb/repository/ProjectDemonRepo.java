package com.kt.icis.samp.dbtodb.repository;

import com.kt.icis.samp.dbtodb.repository.dto.ProjectAnalsDto;
import com.kt.icis.samp.dbtodb.repository.dto.ProjectDemonDto;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectDemonRepo extends
    PagingAndSortingRepository<ProjectDemonDto, String> {

    List<ProjectDemonDto> findByPjtId(String id);

    List<ProjectDemonDto> findAll();

    Optional<ProjectAnalsDto> findTopByPjtIdOrderByChgDtDesc(String id);
}
