package com.wgzhao.addax.admin.model;

import jakarta.persistence.PreUpdate;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "collect_task")
public class CollectTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "source_id", nullable = false)
    private Integer sourceId;
    
    @Column(name = "source_schema")
    private String sourceSchema;
    
    @Column(name = "source_table", nullable = false)
    private String sourceTable;
    
    @Column(name = "target_schema")
    private String targetSchema;
    
    @Column(name = "target_table", nullable = false)
    private String targetTable;
    
    @Column(name = "source_template_id", nullable = false)
    private Integer sourceTemplateId;
    
    @Column(name = "hdfs_template_id", nullable = false)
    private Integer hdfsTemplateId;
    
    @Column(name = "where_condition", nullable = false)
    private String whereCondition;
    
    @Column(name = "task_status", nullable = false)
    private String taskStatus;
    
    @Column(name = "timeout_secs")
    private Integer timeoutSecs;
    
    @Column(name = "retry_times")
    private Integer retryTimes;
    
    @Column(name = "cron_expression")
    private String cronExpression;
    
    @Column(name = "etl_freq", nullable = false)
    private String etlFreq;
    
    @Column(name = "need_create_table", nullable = false)
    private String needCreateTable;
    
    @Column(name = "need_update_meta", nullable = false)
    private String needUpdateMeta;
    
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}