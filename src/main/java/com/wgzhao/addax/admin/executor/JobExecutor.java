package com.wgzhao.addax.admin.executor;

import com.wgzhao.addax.admin.model.CollectJob;
import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.model.JobExecution;
import com.wgzhao.addax.admin.repository.JobRepository;
import com.wgzhao.addax.admin.scheduler.JobQueue;
import com.wgzhao.addax.admin.service.JobExecutionService;
import com.wgzhao.addax.admin.service.TaskExecutionService;
import com.wgzhao.addax.admin.service.TaskService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class JobExecutor {
    private static final Logger logger = LoggerFactory.getLogger(JobExecutor.class);
    
    private final JobQueue jobQueue;
    private final JobRepository jobRepository;
    private final JobExecutionService jobExecutionService;
    private final TaskService taskService;
    private final TaskExecutionService taskExecutionService;
    private final ExecutorService executorService;
    
    @Autowired
    public JobExecutor(JobQueue jobQueue, 
                      JobRepository jobRepository,
                      JobExecutionService jobExecutionService,
                      TaskService taskService,
                      TaskExecutionService taskExecutionService) {
        this.jobQueue = jobQueue;
        this.jobRepository = jobRepository;
        this.jobExecutionService = jobExecutionService;
        this.taskService = taskService;
        this.taskExecutionService = taskExecutionService;
        
        // 根据CPU核心数设置工作线程数
        int workerCount = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(workerCount);
        
        logger.info("作业执行器已初始化，工作线程数: {}", workerCount);
    }
    
    @PostConstruct
    public void init() {
        // 启动工作线程
        startWorkers();
    }
    
    private void startWorkers() {
        int workerCount = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < workerCount; i++) {
            final int workerId = i;
            executorService.submit(() -> {
                logger.info("工作线程 [{}] 已启动", workerId);
                processJobsFromQueue(workerId);
            });
        }
    }
    
    private void processJobsFromQueue(int workerId) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 从队列获取作业
                CollectJob job = jobQueue.dequeue();
                if (job == null) {
                    // 队列为空，等待一段时间
                    Thread.sleep(1000);
                    continue;
                }
                
                logger.info("工作线程 [{}] 开始处理作业 [{}]", workerId, job.getJobName());
                
                // 执行作业
                executeJob(job);
                
            } catch (InterruptedException e) {
                logger.info("工作线程 [{}] 被中断", workerId);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                logger.error("工作线程 [{}] 处理作业时发生错误", workerId, e);
            }
        }
    }
    
    /**
     * 执行作业
     * @param job 作业对象
     */
    private void executeJob(CollectJob job) {
        Long jobId = job.getId();
        JobExecution jobExecution = jobExecutionService.getRunningJobExecution(jobId);
        
        if (jobExecution == null) {
            logger.error("作业 [{}] 的执行记录不存在", job.getJobName());
            return;
        }
        
        try {
            // 更新作业执行状态为运行中
            jobExecutionService.updateJobExecutionStatus(jobExecution.getId(), "RUNNING");
            
            // 获取作业关联的任务列表
            List<CollectTask> tasks = taskService.getTasksByJobId(jobId);
            
            if (tasks.isEmpty()) {
                logger.warn("作业 [{}] 没有关联任务", job.getJobName());
                jobExecutionService.completeJobExecution(jobExecution.getId(), "SUCCESS", "No tasks associated with this job");
                updateJobStatus(job, "N"); // 设置为未运行状态
                return;
            }
            
            boolean allTasksSuccessful = true;
            
            // 按顺序执行任务
            for (CollectTask task : tasks) {
                logger.info("开始执行任务 [{}]", task.getSourceTable());
                
                // 更新任务状态为运行中
                taskService.updateTaskStatus(task.getId(), "R");
                
                // 创建任务执行记录
                Long taskExecutionId = taskExecutionService.createTaskExecution(task.getId(), "JOB_TRIGGERED");
                
                try {
                    // 执行具体的任务
                    boolean success = executeTask(task, taskExecutionId);
                    
                    if (!success) {
                        allTasksSuccessful = false;
                        logger.error("任务 [{}] 执行失败", task.getSourceTable());
                    }
                    
                } catch (Exception e) {
                    allTasksSuccessful = false;
                    logger.error("执行任务 [{}] 时发生异常", task.getSourceTable(), e);
                    taskExecutionService.completeTaskExecution(taskExecutionId, "FAILED", e.getMessage());
                    
                    // 根据作业配置决定是否继续执行后续任务
                    if (job.getConcurrentFlag() == 0) {
                        logger.info("由于任务 [{}] 失败且作业不允许并发执行，停止执行后续任务", task.getSourceTable());
                        break;
                    }
                } finally {
                    // 更新任务状态
                    taskService.updateTaskStatus(task.getId(), allTasksSuccessful ? "Y" : "E");
                }
            }
            
            // 更新作业执行状态
            String finalStatus = allTasksSuccessful ? "SUCCESS" : "FAILED";
            jobExecutionService.completeJobExecution(jobExecution.getId(), finalStatus, null);
            
            // 更新作业状态为未运行
            updateJobStatus(job, "N");
            
        } catch (Exception e) {
            logger.error("执行作业 [{}] 时发生异常", job.getJobName(), e);
            jobExecutionService.completeJobExecution(jobExecution.getId(), "FAILED", e.getMessage());
            updateJobStatus(job, "E"); // 设置为失败状态
        }
    }
    
    /**
     * 执行具体的任务
     * @param task 任务对象
     * @param taskExecutionId 任务执行ID
     * @return 执行是否成功
     */
    private boolean executeTask(CollectTask task, Long taskExecutionId) {
        try {
            // 这里实现具体的任务执行逻辑
            // 例如调用Addax执行数据采集任务
            
            // 模拟任务执行
            logger.info("正在执行任务 [{}]", task.getSourceTable());
            
            // TODO: 实现实际的任务执行逻辑
            // 例如：生成Addax配置JSON，调用Addax命令行工具执行
            
            // 模拟执行成功
            taskExecutionService.completeTaskExecution(taskExecutionId, "SUCCESS", null);
            return true;
            
        } catch (Exception e) {
            logger.error("执行任务时发生错误", e);
            taskExecutionService.completeTaskExecution(taskExecutionId, "FAILED", e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新作业状态
     * @param job 作业对象
     * @param status 新状态
     */
    private void updateJobStatus(CollectJob job, String status) {
        try {
            job.setJobStatus(status);
            jobRepository.save(job);
            logger.info("作业 [{}] 状态已更新为 [{}]", job.getJobName(), status);
        } catch (Exception e) {
            logger.error("更新作业状态时发生错误", e);
        }
    }
    
    @PreDestroy
    public void shutdown() {
        logger.info("正在关闭作业执行器...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.error("作业执行器无法完全关闭");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("作业执行器已关闭");
    }
}