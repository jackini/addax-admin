package com.wgzhao.addax.admin.model;

import com.wgzhao.addax.admin.utils.JsonAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "task_execution")
@Data
public class TaskExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "task_id", nullable = false)
    private Integer taskId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private Integer duration;

    @Column(name = "exec_status", nullable = false, length = 20)
    private String status;

    @Column(name = "total_records")
    private Long totalRecords;

    @Column(name = "success_records")
    private Long successRecords;

    @Column(name = "failed_records")
    private Long failedRecords;

    @Column(name = "rejected_records")
    private Long rejectedRecords;

    @Column(name = "bytes_speed")
    private Long bytesSpeed;

    @Column(name = "records_speed")
    private Long recordsSpeed;

    @Column(name = "log_path", length = 500)
    private String logPath;

    @Column(name = "execution_json")
    @Convert(converter = JsonAttributeConverter.class)
    private Map<String, Object> executionJson;

    @Column(name = "trigger_type", length = 20)
    private String triggerType;
}