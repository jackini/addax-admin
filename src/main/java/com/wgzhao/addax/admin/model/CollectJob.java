package com.wgzhao.addax.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "collect_job")
public class CollectJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "job_name", nullable = false)
    private String jobName;
    
    @Column(name = "job_group")
    private String jobGroup;
    
    // 数据源相关字段
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
    
    // 模板相关字段
    @Column(name = "source_template_id", nullable = false)
    private Integer sourceTemplateId;
    
    @Column(name = "hdfs_template_id", nullable = false)
    private Integer hdfsTemplateId;
    
    // 过滤条件
    @Column(name = "where_condition", nullable = false)
    private String whereCondition;
    
    // 调度相关字段
    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;
    
    @Column(name = "job_status", nullable = false)
    private String jobStatus;
    
    @Column(name = "concurrent_flag", nullable = false)
    private Integer concurrentFlag;
    
    @Column(name = "timeout_secs")
    private Integer timeoutSecs;
    
    @Column(name = "retry_times")
    private Integer retryTimes;
    
    @Column(name = "retry_interval")
    private Integer retryInterval;
    
    // ETL相关字段
    @Column(name = "etl_freq", nullable = false)
    private String etlFreq;
    
    @Column(name = "need_create_table", nullable = false)
    private String needCreateTable;
    
    @Column(name = "need_update_meta", nullable = false)
    private String needUpdateMeta;
    
    @Column(name = "description")
    private String description;
    
    // 执行时间相关字段
    @Column(name = "next_fire_time")
    private LocalDateTime nextFireTime;
    
    @Column(name = "last_fire_time")
    private LocalDateTime lastFireTime;
    
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
        
        // 设置默认值
        if (whereCondition == null) {
            whereCondition = "1=1";
        }
        if (etlFreq == null) {
            etlFreq = "D";
        }
        if (needCreateTable == null) {
            needCreateTable = "Yn";
        }
        if (needUpdateMeta == null) {
            needUpdateMeta = "Ny";
        }
        if (jobStatus == null) {
            jobStatus = "N"; // 默认未运行
        }
        if (concurrentFlag == null) {
            concurrentFlag = 0; // 默认不允许并发
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}