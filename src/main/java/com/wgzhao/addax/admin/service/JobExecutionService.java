package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.CollectJob;
import com.wgzhao.addax.admin.model.JobExecution;
import com.wgzhao.addax.admin.repository.JobExecutionRepository;
import com.wgzhao.addax.admin.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class JobExecutionService {

    private final JobExecutionRepository jobExecutionRepository;
    private final JobRepository jobRepository;
    private final AddaxJsonGenerator addaxJsonGenerator;

    @Autowired
    public JobExecutionService(JobExecutionRepository jobExecutionRepository, 
                              JobRepository jobRepository,
                              AddaxJsonGenerator addaxJsonGenerator) {
        this.jobExecutionRepository = jobExecutionRepository;
        this.jobRepository = jobRepository;
        this.addaxJsonGenerator = addaxJsonGenerator;
    }

    /**
     * 创建作业执行记录
     * @param jobId 作业ID
     * @param triggerType 触发类型
     * @return 执行记录ID
     */
    @Transactional
    public Long createJobExecution(Long jobId, String triggerType) {
        Optional<CollectJob> jobOpt = jobRepository.findById(jobId);
        if (!jobOpt.isPresent()) {
            log.error("创建作业执行记录失败：作业不存在，ID: {}", jobId);
            throw new IllegalArgumentException("作业不存在");
        }

        CollectJob job = jobOpt.get();
        
        JobExecution execution = new JobExecution();
        execution.setJobId(jobId);
        execution.setStartTime(LocalDateTime.now());
        execution.setExecStatus("WAITING");
        execution.setTriggerType(triggerType);
        
        // 生成Addax任务定义JSON
        String addaxJson = addaxJsonGenerator.generateAddaxJson(job);
        execution.setAddaxJson(addaxJson);
        
        JobExecution savedExecution = jobExecutionRepository.save(execution);
        log.info("已创建作业执行记录，ID: {}, 作业ID: {}", savedExecution.getId(), jobId);
        
        return savedExecution.getId();
    }
    
    /**
     * 获取指定作业的最近执行记录
     * @param jobId 作业ID
     * @param limit 限制数量
     * @return 执行记录列表
     */
    public List<JobExecution> getRecentJobExecutions(Long jobId, int limit) {
        log.info("获取作业 {} 的最近 {} 条执行记录", jobId, limit);
        
        // 创建分页和排序条件，按开始时间降序排序
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "startTime"));
        
        // 查询数据库
        List<JobExecution> executions = jobExecutionRepository.findByJobId(jobId, pageable);
        
        log.info("获取到 {} 条执行记录", executions.size());
        return executions;
    }
    
    /**
     * 更新作业执行状态
     * @param executionId 执行记录ID
     * @param status 新状态
     * @return 更新后的执行记录
     */
    @Transactional
    public Optional<JobExecution> updateExecutionStatus(Long executionId, String status) {
        log.info("更新执行记录状态，ID: {}, 新状态: {}", executionId, status);
        
        Optional<JobExecution> executionOpt = jobExecutionRepository.findById(executionId);
        if (!executionOpt.isPresent()) {
            log.error("更新执行记录状态失败：执行记录不存在，ID: {}", executionId);
            return Optional.empty();
        }
        
        JobExecution execution = executionOpt.get();
        execution.setExecStatus(status);
        
        // 如果状态是完成状态，设置结束时间和持续时间
        if ("SUCCESS".equals(status) || "FAILED".equals(status) || "TIMEOUT".equals(status)) {
            LocalDateTime endTime = LocalDateTime.now();
            execution.setEndTime(endTime);
            
            // 计算持续时间（秒）
            if (execution.getStartTime() != null) {
                Duration duration = Duration.between(execution.getStartTime(), endTime);
                execution.setDuration((int) duration.getSeconds());
            }
        }
        
        JobExecution updatedExecution = jobExecutionRepository.save(execution);
        log.info("已更新执行记录状态，ID: {}, 状态: {}", executionId, status);
        
        return Optional.of(updatedExecution);
    }
    
    /**
     * 完成作业执行
     * @param executionId 执行记录ID
     * @param status 最终状态
     * @param errorMessage 错误信息（可选）
     * @return 更新后的执行记录
     */
    @Transactional
    public Optional<JobExecution> completeExecution(Long executionId, String status, String errorMessage) {
        log.info("完成执行记录，ID: {}, 状态: {}", executionId, status);
        
        Optional<JobExecution> executionOpt = jobExecutionRepository.findById(executionId);
        if (!executionOpt.isPresent()) {
            log.error("完成执行记录失败：执行记录不存在，ID: {}", executionId);
            return Optional.empty();
        }
        
        JobExecution execution = executionOpt.get();
        execution.setExecStatus(status);
        execution.setEndTime(LocalDateTime.now());
        
        // 计算持续时间（秒）
        if (execution.getStartTime() != null) {
            Duration duration = Duration.between(execution.getStartTime(), execution.getEndTime());
            execution.setDuration((int) duration.getSeconds());
        }
        
        // 设置错误信息
        if (errorMessage != null && !errorMessage.isEmpty()) {
            execution.setErrorMessage(errorMessage);
        }
        
        JobExecution updatedExecution = jobExecutionRepository.save(execution);
        log.info("已完成执行记录，ID: {}, 状态: {}, 耗时: {}秒", 
                executionId, status, updatedExecution.getDuration());
        
        return Optional.of(updatedExecution);
    }
    
    /**
     * 获取所有执行记录
     * @param pageable 分页参数
     * @return 分页执行记录
     */
    public Page<JobExecution> getAllExecutions(Pageable pageable) {
        log.info("获取所有执行记录，页码: {}, 大小: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        return jobExecutionRepository.findAll(pageable);
    }
    
    /**
     * 根据状态获取执行记录
     * @param status 执行状态
     * @param pageable 分页参数
     * @return 分页执行记录
     */
    public Page<JobExecution> getExecutionsByStatus(String status, Pageable pageable) {
        log.info("获取状态为 {} 的执行记录，页码: {}, 大小: {}", 
                status, pageable.getPageNumber(), pageable.getPageSize());
        
        return jobExecutionRepository.findByExecStatus(status, pageable);
    }
    
    /**
     * 获取指定时间段内的执行记录
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param pageable 分页参数
     * @return 分页执行记录
     */
    public Page<JobExecution> getExecutionsByTimeRange(LocalDateTime startDate, 
                                                     LocalDateTime endDate, 
                                                     Pageable pageable) {
        log.info("获取时间段 {} 至 {} 内的执行记录", startDate, endDate);
        
        return jobExecutionRepository.findByStartTimeBetween(startDate, endDate, pageable);
    }
    
    /**
     * 获取执行记录的Addax JSON配置
     * @param executionId 执行记录ID
     * @return Addax JSON配置
     */
    public Optional<String> getAddaxJson(Long executionId) {
        log.info("获取执行记录的Addax JSON配置，ID: {}", executionId);
        
        Optional<JobExecution> executionOpt = jobExecutionRepository.findById(executionId);
        if (!executionOpt.isPresent()) {
            log.error("获取Addax JSON失败：执行记录不存在，ID: {}", executionId);
            return Optional.empty();
        }
        
        JobExecution execution = executionOpt.get();
        return Optional.ofNullable(execution.getAddaxJson());
    }
    
    /**
     * 重新生成Addax JSON配置
     * @param executionId 执行记录ID
     * @return 更新后的执行记录
     */
    @Transactional
    public Optional<JobExecution> regenerateAddaxJson(Long executionId) {
        log.info("重新生成执行记录的Addax JSON配置，ID: {}", executionId);
        
        Optional<JobExecution> executionOpt = jobExecutionRepository.findById(executionId);
        if (!executionOpt.isPresent()) {
            log.error("重新生成Addax JSON失败：执行记录不存在，ID: {}", executionId);
            return Optional.empty();
        }
        
        JobExecution execution = executionOpt.get();
        Optional<CollectJob> jobOpt = jobRepository.findById(execution.getJobId());
        
        if (!jobOpt.isPresent()) {
            log.error("重新生成Addax JSON失败：作业不存在，ID: {}", execution.getJobId());
            return Optional.empty();
        }
        
        // 重新生成Addax JSON配置
        String addaxJson = addaxJsonGenerator.generateAddaxJson(jobOpt.get());
        execution.setAddaxJson(addaxJson);
        
        JobExecution updatedExecution = jobExecutionRepository.save(execution);
        log.info("已重新生成执行记录的Addax JSON配置，ID: {}", executionId);
        
        return Optional.of(updatedExecution);
    }
}