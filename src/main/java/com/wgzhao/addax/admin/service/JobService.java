package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.CollectJob;
import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.model.JobTaskRelation;
import com.wgzhao.addax.admin.repository.JobRepository;
import com.wgzhao.addax.admin.repository.JobTaskRelationRepository;
import com.wgzhao.addax.admin.repository.TaskRepository;
import com.wgzhao.addax.admin.scheduler.JobQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);
    
    private final JobRepository jobRepository;
    private final TaskRepository taskRepository;
    private final JobTaskRelationRepository jobTaskRelationRepository;
    private final JobExecutionService jobExecutionService;
    private final JobQueue jobQueue;
    
    @Autowired
    public JobService(JobRepository jobRepository,
                     TaskRepository taskRepository,
                     JobTaskRelationRepository jobTaskRelationRepository,
                     JobExecutionService jobExecutionService,
                     JobQueue jobQueue) {
        this.jobRepository = jobRepository;
        this.taskRepository = taskRepository;
        this.jobTaskRelationRepository = jobTaskRelationRepository;
        this.jobExecutionService = jobExecutionService;
        this.jobQueue = jobQueue;
    }
    
    /**
     * 获取所有作业
     * @return 作业列表
     */
    public List<CollectJob> getAllJobs() {
        return jobRepository.findAll();
    }
    
    /**
     * 获取指定作业
     * @param id 作业ID
     * @return 作业对象
     */
    public Optional<CollectJob> getJob(Long id) {
        return jobRepository.findById(id);
    }
    
    /**
     * 创建新作业
     * @param job 作业对象
     * @return 保存后的作业对象
     */
    @Transactional
    public CollectJob createJob(CollectJob job) {
        // 设置默认值
        if (job.getJobStatus() == null) {
            job.setJobStatus("N"); // 未运行
        }
        
        if (job.getConcurrentFlag() == null) {
            job.setConcurrentFlag(0); // 默认不允许并发
        }
        
        if (job.getTimeoutSecs() == null) {
            job.setTimeoutSecs(7200); // 默认2小时超时
        }
        
        if (job.getRetryTimes() == null) {
            job.setRetryTimes(3); // 默认重试3次
        }
        
        if (job.getRetryInterval() == null) {
            job.setRetryInterval(60); // 默认重试间隔60秒
        }
        
        CollectJob savedJob = jobRepository.save(job);
        logger.info("已创建新作业，ID: {}, 名称: {}", savedJob.getId(), savedJob.getJobName());
        return savedJob;
    }
    
    /**
     * 更新作业
     * @param job 作业对象
     * @return 更新后的作业对象
     */
    @Transactional
    public CollectJob updateJob(CollectJob job) {
        CollectJob savedJob = jobRepository.save(job);
        logger.info("已更新作业，ID: {}, 名称: {}", savedJob.getId(), savedJob.getJobName());
        return savedJob;
    }
    
    /**
     * 删除作业
     * @param id 作业ID
     */
    @Transactional
    public void deleteJob(Long id) {
        // 先删除作业任务关联
        jobTaskRelationRepository.deleteByJobId(id);
        
        // 再删除作业
        jobRepository.deleteById(id);
        logger.info("已删除作业，ID: {}", id);
    }
    
    /**
     * 手动触发作业
     * @param id 作业ID
     * @return 执行记录ID
     */
    @Transactional
    public Long triggerJob(Long id) {
        CollectJob job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        
        // 创建执行记录
        Long executionId = jobExecutionService.createJobExecution(id, "MANUAL");
        
        // 更新作业状态
        job.setJobStatus("R"); // 运行中
        job.setLastFireTime(LocalDateTime.now());
        jobRepository.save(job);
        
        // 提交到作业队列
        jobQueue.enqueue(job);
        
        logger.info("已手动触发作业，ID: {}, 名称: {}", id, job.getJobName());
        return executionId;
    }
    
    /**
     * 暂停作业
     * @param id 作业ID
     * @return 更新后的作业对象
     */
    @Transactional
    public CollectJob pauseJob(Long id) {
        CollectJob job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        
        job.setJobStatus("P"); // 暂停
        CollectJob savedJob = jobRepository.save(job);
        
        logger.info("已暂停作业，ID: {}, 名称: {}", id, job.getJobName());
        return savedJob;
    }
    
    /**
     * 恢复作业
     * @param id 作业ID
     * @return 更新后的作业对象
     */
    @Transactional
    public CollectJob resumeJob(Long id) {
        CollectJob job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
        
        job.setJobStatus("N"); // 未运行
        CollectJob savedJob = jobRepository.save(job);
        
        logger.info("已恢复作业，ID: {}, 名称: {}", id, job.getJobName());
        return savedJob;
    }
    
    /**
     * 关联任务到作业
     * @param jobId 作业ID
     * @param taskId 任务ID
     * @param order 执行顺序
     */
    @Transactional
    public void associateTaskWithJob(Long jobId, Long taskId, int order) {
        // 检查是否已存在关联
        JobTaskRelation existing = jobTaskRelationRepository.findByJobIdAndTaskId(jobId, taskId);
        
        if (existing != null) {
            // 更新执行顺序
            existing.setTaskOrder(order);
            jobTaskRelationRepository.save(existing);
            logger.info("已更新任务 {} 与作业 {} 的关联顺序为 {}", taskId, jobId, order);
        } else {
            // 创建新关联
            jobTaskRelationRepository.createJobTaskRelation(jobId, taskId, order);
            logger.info("已将任务 {} 关联到作业 {}, 执行顺序: {}", taskId, jobId, order);
        }
    }
    
    /**
     * 解除任务与作业的关联
     * @param jobId 作业ID
     * @param taskId 任务ID
     */
    @Transactional
    public void disassociateTaskFromJob(Long jobId, Long taskId) {
        JobTaskRelation relation = jobTaskRelationRepository.findByJobIdAndTaskId(jobId, taskId);
        
        if (relation != null) {
            jobTaskRelationRepository.delete(relation);
            logger.info("已解除任务 {} 与作业 {} 的关联", taskId, jobId);
        }
    }
    
    /**
     * 获取作业关联的任务列表（包含执行顺序）
     * @param jobId 作业ID
     * @return 任务列表（包含执行顺序）
     */
    public List<Map<String, Object>> getTasksWithOrderByJobId(Long jobId) {
        List<JobTaskRelation> relations = jobTaskRelationRepository.findByJobIdOrderByTaskOrder(jobId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (JobTaskRelation relation : relations) {
            Optional<CollectTask> taskOpt = taskRepository.findById(relation.getTaskId());
            
            if (taskOpt.isPresent()) {
                CollectTask task = taskOpt.get();
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("taskId", task.getId());
                taskMap.put("sourceTable", task.getSourceTable());
                taskMap.put("targetTable", task.getTargetTable());
                taskMap.put("taskStatus", task.getTaskStatus());
                taskMap.put("order", relation.getTaskOrder());
                
                result.add(taskMap);
            }
        }
        
        return result;
    }
}