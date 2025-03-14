package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.JobExecution;
import com.wgzhao.addax.admin.repository.JobExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(JobExecutionService.class);
    
    private final JobExecutionRepository jobExecutionRepository;
    
    @Autowired
    public JobExecutionService(JobExecutionRepository jobExecutionRepository) {
        this.jobExecutionRepository = jobExecutionRepository;
    }
    
    /**
     * 创建作业执行记录
     * @param jobId 作业ID
     * @param triggerType 触发类型
     * @return 执行记录ID
     */
    @Transactional
    public Long createJobExecution(Long jobId, String triggerType) {
        JobExecution execution = new JobExecution();
        execution.setJobId(jobId);
        execution.setStartTime(LocalDateTime.now());
        execution.setExecStatus("WAITING");
        execution.setTriggerType(triggerType);
        
        JobExecution saved = jobExecutionRepository.save(execution);
        logger.info("已创建作业执行记录，ID: {}, 作业ID: {}", saved.getId(), jobId);
        return saved.getId();
    }
    
    /**
     * 获取正在运行的作业执行记录
     * @param jobId 作业ID
     * @return 执行记录对象
     */
    public JobExecution getRunningJobExecution(Long jobId) {
        List<JobExecution> executions = jobExecutionRepository.findByJobIdAndExecStatusIn(
                jobId, List.of("WAITING", "RUNNING"));
        
        return executions.isEmpty() ? null : executions.get(0);
    }
    
    /**
     * 更新作业执行状态
     * @param executionId 执行记录ID
     * @param status 新状态
     */
    @Transactional
    public void updateJobExecutionStatus(Long executionId, String status) {
        JobExecution execution = jobExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Job execution not found: " + executionId));
        
        execution.setExecStatus(status);
        jobExecutionRepository.save(execution);
        logger.info("已更新作业执行状态，ID: {}, 状态: {}", executionId, status);
    }
    
    /**
     * 完成作业执行
     * @param executionId 执行记录ID
     * @param status 最终状态
     * @param errorMessage 错误信息
     */
    @Transactional
    public void completeJobExecution(Long executionId, String status, String errorMessage) {
        JobExecution execution = jobExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Job execution not found: " + executionId));
        
        LocalDateTime endTime = LocalDateTime.now();
        execution.setEndTime(endTime);
        execution.setExecStatus(status);
        execution.setErrorMessage(errorMessage);
        
        // 计算执行时长
        if (execution.getStartTime() != null) {
            Duration duration = Duration.between(execution.getStartTime(), endTime);
            execution.setDuration((int) duration.getSeconds());
        }
        
        jobExecutionRepository.save(execution);
        logger.info("已完成作业执行，ID: {}, 状态: {}", executionId, status);
    }
    
    /**
     * 获取作业的最近执行记录
     * @param jobId 作业ID
     * @param limit 记录数量限制
     * @return 执行记录列表
     */
    public List<JobExecution> getRecentJobExecutions(Long jobId, int limit) {
        return jobExecutionRepository.findByJobIdOrderByStartTimeDesc(jobId, limit);
    }
}