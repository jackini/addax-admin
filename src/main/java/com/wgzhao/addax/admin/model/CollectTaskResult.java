package com.wgzhao.addax.admin.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CollectTaskResult {
    private Long taskId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Long totalRecords;
    private Long successRecords;
    private Long failedRecords;
    private Long rejectedRecords;
    private Long bytesSpeed;
    private Long recordsSpeed;
}