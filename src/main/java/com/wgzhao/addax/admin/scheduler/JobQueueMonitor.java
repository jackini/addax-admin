package com.wgzhao.addax.admin.scheduler;

import com.wgzhao.addax.admin.model.QueueStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 作业队列监控器
 * 负责监控队列状态并收集统计信息
 */
@Component
public class JobQueueMonitor {
    private static final Logger logger = LoggerFactory.getLogger(JobQueueMonitor.class);

    private final JobQueue jobQueue;

    // 统计数据
    private final AtomicLong totalEnqueued = new AtomicLong(0);
    private final AtomicLong totalDequeued = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalSucceeded = new AtomicLong(0);

    // 历史队列大小记录
    private final ConcurrentMap<LocalDateTime, Long> queueSizeHistory = new ConcurrentHashMap<>();
    private static final int HISTORY_SIZE = 24; // 保留24小时的历史记录

    @Autowired
    public JobQueueMonitor(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
    }

    /**
     * 记录作业入队
     */
    public void recordEnqueue() {
        totalEnqueued.incrementAndGet();
    }

    /**
     * 记录作业出队
     */
    public void recordDequeue() {
        totalDequeued.incrementAndGet();
    }

    /**
     * 记录作业执行成功
     */
    public void recordSuccess() {
        totalSucceeded.incrementAndGet();
    }

    /**
     * 记录作业执行失败
     */
    public void recordFailure() {
        totalFailed.incrementAndGet();
    }

    /**
     * 获取当前队列统计信息
     * @return 队列统计信息
     */
    public QueueStats getQueueStats() {
        QueueStats stats = new QueueStats();
        stats.setCurrentSize(jobQueue.getQueueSize());
        stats.setTotalEnqueued(totalEnqueued.get());
        stats.setTotalDequeued(totalDequeued.get());
        stats.setTotalSucceeded(totalSucceeded.get());
        stats.setTotalFailed(totalFailed.get());
        stats.setQueueSizeHistory(new ArrayList<>(queueSizeHistory.values()));
        stats.setLastUpdated(LocalDateTime.now());
        return stats;
    }

    /**
     * 定时记录队列大小
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void recordQueueSize() {
        LocalDateTime now = LocalDateTime.now();
        long size = jobQueue.getQueueSize();
        queueSizeHistory.put(now, size);

        // 清理旧记录，只保留最近HISTORY_SIZE条
        if (queueSizeHistory.size() > HISTORY_SIZE) {
            List<LocalDateTime> times = new ArrayList<>(queueSizeHistory.keySet());
            times.sort(LocalDateTime::compareTo);
            for (int i = 0; i < times.size() - HISTORY_SIZE; i++) {
                queueSizeHistory.remove(times.get(i));
            }
        }

        logger.info("队列大小记录: 时间={}, 大小={}", now, size);
    }

    /**
     * 重置统计数据
     */
    public void resetStats() {
        totalEnqueued.set(0);
        totalDequeued.set(0);
        totalSucceeded.set(0);
        totalFailed.set(0);
        queueSizeHistory.clear();
        logger.info("队列统计数据已重置");
    }
}