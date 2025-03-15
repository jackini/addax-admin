package com.wgzhao.addax.admin.scheduler;

import com.wgzhao.addax.admin.executor.AddaxExecutor;
import com.wgzhao.addax.admin.model.CollectJob;
import com.wgzhao.addax.admin.model.JobExecution;
import com.wgzhao.addax.admin.repository.JobRepository;
import com.wgzhao.addax.admin.service.JobExecutionService;
import com.wgzhao.addax.admin.service.JobService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 作业执行器
 * 负责从队列获取作业并执行
 */
@Component
@AllArgsConstructor
public class JobExecutor {
    private static final Logger logger = LoggerFactory.getLogger(JobExecutor.class);

    private final JobQueue jobQueue;
    private final JobExecutionService jobExecutionService;
    private final JobQueueMonitor queueMonitor;
    private final JobRepository jobRepository;
    private final AddaxExecutor addaxExecutor;

    /**
     * 定时从队列获取作业并执行
     * 每5秒执行一次
     */
    @Scheduled(fixedRate = 5000)
    public void executeJobs() {
        logger.info("当前队列大小: {}", jobQueue.getQueueSize());
        CollectJob job = jobQueue.dequeue();
        if (job == null) {
            return;
        }

        logger.info("开始执行作业: {}", job.getJobName());

        try {
            // 创建执行记录
            JobExecution execution = jobExecutionService.createJobExecution(job.getId(), "SCHEDULED");

            // 执行作业逻辑
            logger.info("作业执行中...");

            addaxExecutor.executeAddax(execution, job);

            // 记录成功统计
            queueMonitor.recordSuccess();

            logger.info("作业执行成功: {}", job.getJobName());
        } catch (Exception e) {
            logger.error("作业执行失败: {}", job.getJobName(), e);

            // 记录失败统计
            queueMonitor.recordFailure();
        }
    }
}