package com.wgzhao.addax.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "collect_task")
@Data
@Setter
@Getter
public class CollectTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    private DataSource source;

    @Column(name = "source_schema", length = 100)
    private String sourceSchema;

    @Column(name = "source_table", nullable = false, length = 200)
    private String sourceTable;

    @Column(name = "target_schema", length = 100)
    private String targetSchema;

    @Column(name = "target_table", nullable = false, length = 200)
    private String targetTable;

    @ManyToOne
    @JoinColumn(name = "source_template_id", nullable = false)
    private SourceTemplate sourceTemplate;

    @ManyToOne
    @JoinColumn(name = "hdfs_template_id", nullable = false)
    private HdfsTemplate hdfsTemplate;

    @Column(name = "where_condition", nullable = false, length = 200)
    private String whereCondition = "1=1";

    @Column(name = "task_status", nullable = false, length = 1)
    private String status = "N";

    @Column(name = "timeout_secs")
    private Integer timeoutSeconds = 7200;

    @Column(name = "retry_times")
    private Integer retryTimes = 3;

    @Column(name = "cron_expression")
    private String cronExpression;

    @Column(name = "etl_freq", nullable = false, length = 1)
    private String etlFrequency = "D";

    @Column(name = "need_create_table", nullable = false, length = 2)
    private String needCreateTable = "Yn";

    @Column(name = "need_update_meta", nullable = false, length = 2)
    private String needUpdateMeta = "Ny";

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
}