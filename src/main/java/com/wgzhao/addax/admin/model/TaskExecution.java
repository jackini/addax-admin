package com.wgzhao.addax.admin.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_execution")
@Data
public class TaskExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private CollectTask task;

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

    @Lob
    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "log_path", length = 500)
    private String logPath;

    @Column(name = "execution_json")
    private String executionJson;

    @Column(name = "trigger_type", length = 20)
    private String triggerType;
}