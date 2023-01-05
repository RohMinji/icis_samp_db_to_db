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
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class BatchSampItemProcessor implements ItemProcessor<ProjectAnalsDto, ProjectDemonDto> {

    @Override
    public ProjectDemonDto process(ProjectAnalsDto dto) {
        return ProjectDemonDto.builder()
                .id(dto.getId())
                .pjtId(dto.getPjtId())
                .chgDt(dto.getChgDt())
                .chgId(dto.getChgId())
                .cretDt(dto.getCretDt())
                .cretId(dto.getCretId())
                .comndApiCnt(dto.getComndApiCnt())
                .comndSvcCnt(dto.getComndSvcCnt())
                .comndRepositoryCnt(dto.getComndRepositoryCnt())
                .comndEntityCnt(dto.getComndEntityCnt())
                .queryApiCnt(dto.getQueryApiCnt())
                .querySvcCnt(dto.getQuerySvcCnt())
                .queryEntityCnt(dto.getQueryEntityCnt())
                .queryRepositoryCnt(dto.getQueryRepositoryCnt())
                .openfeignCnt(dto.getOpenfeignCnt())
                .itemRdgrDbCnt(dto.getItemRdgrDbCnt())
                .itemRdgrFileCnt(dto.getItemRdgrFileCnt())
                .itemRdgrKafkaCnt(dto.getItemRdgrKafkaCnt())
                .prcsrCnt(dto.getPrcsrCnt())
                .itemWrtrDbCnt(dto.getItemWrtrDbCnt())
                .itemWrtrFileCnt(dto.getItemWrtrFileCnt())
                .itemWrtrKafkaCnt(dto.getItemWrtrKafkaCnt())
            .build();
    }

}
