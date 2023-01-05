package com.kt.icis.samp.dbtodb.repository.dto;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table("devpilot_pjt_anals_hst")
public class ProjectAnalsDto implements Persistable<String> {

    @Transient
    private boolean isNew;

    @Id 
    @Column("pjt_anals_id")
    private String id;

    @Column("pjt_id")
    private String pjtId;

    @Column("comnd_api_cnt")
    private String comndApiCnt;

    @Column("comnd_svc_cnt")
    private String comndSvcCnt;

    @Column("comnd_repository_cnt")
    private String comndRepositoryCnt;

    @Column("comnd_entity_cnt")
    private String comndEntityCnt;

    @Column("query_api_cnt")
    private String queryApiCnt;

    @Column("query_svc_cnt")
    private String querySvcCnt;

    @Column("query_repository_cnt")
    private String queryRepositoryCnt;

    @Column("query_entity_cnt")
    private String queryEntityCnt;

    @Column("openfeign_cnt")
    private String openfeignCnt;

    @Column("item_rdgr_db_cnt")
    private String itemRdgrDbCnt;

    @Column("item_rdgr_file_cnt")
    private String itemRdgrFileCnt;

    @Column("item_rdgr_kafka_cnt")
    private String itemRdgrKafkaCnt;

    @Column("prcsr_cnt")
    private String prcsrCnt;

    @Column("item_wrtr_db_cnt")
    private String itemWrtrDbCnt;

    @Column("item_wrtr_file_cnt")
    private String itemWrtrFileCnt;

    @Column("item_wrtr_kafka_cnt")
    private String itemWrtrKafkaCnt;

    @Column("cret_id")
    private String cretId;

    @Column("chg_id")
    private String chgId;

    @Column("cret_dt")
    private String cretDt;

    @Column("chg_dt")
    private String chgDt;

    @Override
    public boolean isNew() {
        return true;
    }

    @Override
    public String getId() {
        return id;
    }
}
