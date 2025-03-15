package com.wgzhao.addax.admin.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 队列统计信息
 */
@Data
public class QueueStats {
    // 当前队列大小
    private long currentSize;

    // 总入队数量
    private long totalEnqueued;

    // 总出队数量
    private long totalDequeued;

    // 总成功数量
    private long totalSucceeded;

    // 总失败数量
    private long totalFailed;

    // 队列大小历史记录
    private List<Long> queueSizeHistory;

    // 最后更新时间
    private LocalDateTime lastUpdated;
}