package com.wgzhao.addax.admin.scheduler;

import com.wgzhao.addax.admin.model.CollectJob;
import com.wgzhao.addax.admin.repository.JobRepository;
import com.wgzhao.addax.admin.service.ConfigService;
import com.wgzhao.addax.admin.service.JobExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Semaphore;

@Component
public class DailyCollectionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(DailyCollectionScheduler.class);
    
    private final JobRepository jobRepository;
    private final JobExecutionService jobExecutionService;
    private final JobQueue jobQueue;
    private final ConfigService configService;
    
    // 控制并发作业数的信号量
    private Semaphore concurrentJobsSemaphore;
    
    @Autowired
    public DailyCollectionScheduler(JobRepository jobRepository, 
                                   JobExecutionService jobExecutionService,
                                   JobQueue jobQueue,
                                   ConfigService configService) {
        this.jobRepository = jobRepository;
        this.jobExecutionService = jobExecutionService;
        this.jobQueue = jobQueue;
        this.configService = configService;
        
        // 初始化信号量
        int maxConcurrentJobs = configService.getIntValue("batch_collection", "max_concurrent_jobs", 5);
        this.concurrentJobsSemaphore = new Semaphore(maxConcurrentJobs);
    }
    
    /**
     * 每个工作日16:30触发批量采集
     * 注意：实际的cron表达式会从配置中读取
     */
    @Scheduled(cron = "${batch.collection.cron:0 30 16 * * MON-FRI}")
    public void triggerDailyCollection() {
        // 检查是否启用批量采集
        boolean enabled = configService.getBooleanValue("batch_collection", "enabled", true);
        if (!enabled) {
            logger.info("每日批量采集功能已禁用，跳过执行");
            return;
        }
        
        logger.info("开始执行每日批量采集调度...");
        
        // 检查今天是否是工作日（周一到周五）
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        
        if (dayOfWeek.getValue() >= 1 && dayOfWeek.getValue() <= 5) {
            logger.info("今天是工作日，开始批量采集任务");
            
            // 查询所有启用状态的作业
            List<CollectJob> activeJobs = jobRepository.findByJobStatus("N");
            logger.info("找到 {} 个可执行的作业", activeJobs.size());
            
            // 提交所有作业到队列
            for (CollectJob job : activeJobs) {
                try {
                    // 更新作业状态
                    job.setLastFireTime(now);
                    job.setJobStatus("R"); // 设置为运行中
                    jobRepository.save(job);
                    
                    // 创建作业执行记录
                    jobExecutionService.createJobExecution(job.getId(), "DAILY_BATCH");
                    
                    // 提交到作业队列
                    jobQueue.enqueue(job);
                    
                    logger.info("作业 [{}] 已提交到每日批量采集队列", job.getJobName());
                } catch (Exception e) {
                    logger.error("提交作业 [{}] 到批量采集队列时发生错误", job.getJobName(), e);
                }
            }
        } else {
            logger.info("今天不是工作日，跳过批量采集");
        }
    }
}