package com.wgzhao.addax.admin.scheduler;

import com.wgzhao.addax.admin.model.CollectJob;
import com.wgzhao.addax.admin.repository.JobRepository;
import com.wgzhao.addax.admin.service.CronService;
import com.wgzhao.addax.admin.service.JobExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class JobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);
    
    private final JobRepository jobRepository;
    private final JobExecutionService jobExecutionService;
    private final CronService cronService;
    private final JobQueue jobQueue;
    
    // 定时扫描间隔(秒)
    private static final int SCAN_INTERVAL = 30;
    
    @Autowired
    public JobScheduler(JobRepository jobRepository, 
                        JobExecutionService jobExecutionService,
                        CronService cronService,
                        JobQueue jobQueue) {
        this.jobRepository = jobRepository;
        this.jobExecutionService = jobExecutionService;
        this.cronService = cronService;
        this.jobQueue = jobQueue;
    }
    
    /**
     * 定时扫描需要执行的作业
     */
    @Scheduled(fixedDelay = SCAN_INTERVAL * 1000)
    @Transactional
    public void scanJobs() {
        logger.info("开始扫描需要执行的作业...");
        
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        
        // 查询需要执行的作业
        List<CollectJob> jobsToExecute = jobRepository.findJobsToExecute(now);
        logger.info("找到 {} 个需要执行的作业", jobsToExecute.size());
        
        // 提交到作业队列
        for (CollectJob job : jobsToExecute) {
            try {
                // 计算下次执行时间
                LocalDateTime nextFireTime = cronService.getNextExecutionTime(job.getCronExpression(), now);
                
                // 更新作业状态和下次执行时间
                job.setLastFireTime(now);
                job.setNextFireTime(nextFireTime);
                job.setJobStatus("R"); // 设置为运行中
                jobRepository.save(job);
                
                // 创建作业执行记录
                jobExecutionService.createJobExecution(job.getId(), "SCHEDULED");
                
                // 提交到作业队列
                jobQueue.enqueue(job);
                
                logger.info("作业 [{}] 已提交到队列，下次执行时间: {}", job.getJobName(), nextFireTime);
            } catch (Exception e) {
                logger.error("提交作业 [{}] 到队列时发生错误", job.getJobName(), e);
            }
        }
    }
    /**
     * 定时扫描那些刚加入的采集表，根据 cron_express 初始化下次执行时间
     */
    @Scheduled(fixedDelay = 15 * 1000)
    public void scanNewJobs() {
        logger.info("开始扫描新加入的作业...");

        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 查询需要执行的作业
        List<CollectJob> newJobs = jobRepository.findNeedInitJobs();
        logger.info("找到 {} 个新加入的作业", newJobs.size());

        // 提交到作业队列
        for (CollectJob job : newJobs) {
            try {
                // 计算下次执行时间
                LocalDateTime nextFireTime = cronService.getNextExecutionTime(job.getCronExpression(), now);

                // 更新作业状态和下次执行时间
                job.setLastFireTime(now);
                job.setNextFireTime(nextFireTime);
                job.setJobStatus("P"); // 设置为等待中
                jobRepository.save(job);

                // 创建作业执行记录
                jobExecutionService.createJobExecution(job.getId(), "SCHEDULED");

                // 提交到作业队列
                jobQueue.enqueue(job);

                logger.info("作业 [{}] 已提交到队列，下次执行时间: {}", job.getJobName(), nextFireTime);
            } catch (Exception e) {
                logger.error("提交作业 [{}] 到队列时发生错误", job.getJobName(), e);
            }
        }
    }

    /**
     * 定时扫描在 task_execution 表中，状态为 WAITING 的任务，然后提交到队列
     */
}