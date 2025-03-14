package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.TaskExecution;
import com.wgzhao.addax.admin.model.TaskExecution;
import com.wgzhao.addax.admin.repository.TaskExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutionService.class);
    
    private final TaskExecutionRepository taskExecutionRepository;
    
    @Autowired
    public TaskExecutionService(TaskExecutionRepository taskExecutionRepository) {
        this.taskExecutionRepository = taskExecutionRepository;
    }
    
    /**
    public TaskExecution getTaskExecution(long taskExecutionId)
    {
        return taskExecutionRepository.findById(taskExecutionId)
                .orElseThrow(() -> new RuntimeException("Task execution not found"));
    }

    @Transactional
    public void save(TaskExecution taskExecution) {
        taskExecutionRepository.save(taskExecution);
    }

    // 找到所有需要运行的任务
//    @Transactional
//    public void createPendingExecution(CollectTask task) {
//        TaskExecution execution = new TaskExecution();
//        execution.setTaskId(task.getId());
//        execution.setStartTime(LocalDateTime.now());
//        execution.setStatus("WAITING");
//        execution.setTriggerType("SCHEDULED");
//        taskExecutionRepository.save(execution);
//    }

    @Transactional
    public void updateExecution(TaskExecution execution) {
        taskExecutionRepository.save(execution);
    }

    // 从 task_execution 获取对应的 collect_task
    public CollectTask getCollect(long taskExecuteId)
    {
        long collectId = taskExecutionRepository.getCollectIdById(taskExecuteId);
        return collectTaskRepository.findById(collectId)
                .orElseThrow(() -> new RuntimeException("Collect task not found"));
    }

    public boolean existTask(long collectId)
    {
//        return taskExecutionRepository.countByCollectIdAndStartTimeAfter(collectId, LocalDateTime.now()) > 0;
        return taskExecutionRepository.countByExecStatusNotAndCollectId(ExecStatus.SUCCESS.getCode(), collectId) > 0;
    }

    public TaskExecution getLastExecution(Long collectId)
    {
        return taskExecutionRepository.findFirstByCollectIdAndStartTimeAfterOrderByStartTimeDesc(collectId, LocalDateTime.now());
    }

    public long createPendingExecution(CollectTask task) {
        TaskExecution execution = new TaskExecution();
        execution.setCollectId(task.getId());
        execution.setStartTime(LocalDateTime.now());
        execution.setExecStatus(ExecStatus.WAITING.getCode());
        execution.setTriggerType("SCHEDULED");
        execution.setExecutionJson(addaxJobGenerator.generateJobConfig(task));
        execution =  taskExecutionRepository.save(execution);
        return execution.getId();
    }
    
    /**
     * 创建任务执行记录
     * @param taskId 任务ID
     * @param triggerType 触发类型
     * @return 执行记录ID
     */
    @Transactional
    public Long createTaskExecution(Long taskId, String triggerType) {
        TaskExecution execution = new TaskExecution();
        execution.setCollectId(taskId);
        execution.setStartTime(LocalDateTime.now());
        execution.setExecStatus("WAITING");
        execution.setTriggerType(triggerType);
        
        TaskExecution saved = taskExecutionRepository.save(execution);
        logger.info("已创建任务执行记录，ID: {}, 任务ID: {}", saved.getId(), taskId);
        return saved.getId();
    }
    
    /**
     * 更新任务执行状态
     * @param executionId 执行记录ID
     * @param status 新状态
     */
    @Transactional
    public void updateTaskExecutionStatus(Long executionId, String status) {
        TaskExecution execution = taskExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Task execution not found: " + executionId));
        
        execution.setExecStatus(status);
        taskExecutionRepository.save(execution);
        logger.info("已更新任务执行状态，ID: {}, 状态: {}", executionId, status);
    }
    
    /**
     * 完成任务执行
     * @param executionId 执行记录ID
     * @param status 最终状态
     * @param errorMessage 错误信息（可选）
     */
    @Transactional
    public void completeTaskExecution(Long executionId, String status, String errorMessage) {
        TaskExecution execution = taskExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Task execution not found: " + executionId));
        
        LocalDateTime endTime = LocalDateTime.now();
        execution.setEndTime(endTime);
        execution.setExecStatus(status);
        
        // 计算执行时长
        if (execution.getStartTime() != null) {
            Duration duration = Duration.between(execution.getStartTime(), endTime);
            execution.setDuration((int) duration.getSeconds());
        }
        
        // 如果执行失败，记录错误信息到日志路径
        if ("FAILED".equals(status) && errorMessage != null) {
            String logPath = "logs/task_" + execution.getCollectId() + "_" + executionId + ".log";
            execution.setLogPath(logPath);
            
            // 这里可以实现将错误信息写入日志文件的逻辑
            logger.error("任务执行失败，ID: {}, 错误: {}", executionId, errorMessage);
        }
        
        taskExecutionRepository.save(execution);
        logger.info("已完成任务执行，ID: {}, 状态: {}", executionId, status);
    }
    
    /**
     * 更新任务执行统计信息
     * @param executionId 执行记录ID
     * @param totalRecords 总记录数
     * @param successRecords 成功记录数
     * @param failedRecords 失败记录数
     * @param rejectedRecords 拒绝记录数
     * @param bytesSpeed 字节速率
     * @param recordsSpeed 记录速率
     */
    @Transactional
    public void updateTaskExecutionStats(Long executionId, Long totalRecords, Long successRecords,
                                        Long failedRecords, Long rejectedRecords,
                                        Long bytesSpeed, Long recordsSpeed) {
        TaskExecution execution = taskExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Task execution not found: " + executionId));
        
        execution.setTotalRecords(totalRecords);
        execution.setSuccessRecords(successRecords);
        execution.setFailedRecords(failedRecords);
        execution.setRejectedRecords(rejectedRecords);
        execution.setBytesSpeed(bytesSpeed);
        execution.setRecordsSpeed(recordsSpeed);
        
        taskExecutionRepository.save(execution);
        logger.info("已更新任务执行统计信息，ID: {}, 总记录数: {}", executionId, totalRecords);
    }
    
    /**
     * 保存任务执行的JSON配置
     * @param executionId 执行记录ID
     * @param executionJson 执行JSON配置
     */
    @Transactional
    public void saveTaskExecutionJson(Long executionId, String executionJson) {
        TaskExecution execution = taskExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Task execution not found: " + executionId));
        
        execution.setExecutionJson(executionJson);
        taskExecutionRepository.save(execution);
        logger.info("已保存任务执行JSON配置，ID: {}", executionId);
    }
    
    /**
     * 获取任务的最近执行记录
     * @param taskId 任务ID
     * @param limit 记录数量限制
     * @return 执行记录列表
     */
    public List<TaskExecution> getRecentTaskExecutions(Long taskId, int limit) {
        return taskExecutionRepository.findByCollectIdOrderByStartTimeDesc(taskId, limit);
    }
}
