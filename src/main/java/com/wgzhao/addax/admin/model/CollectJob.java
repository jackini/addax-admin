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
    
    @Column(name = "description")
    private String description;
    
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
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}