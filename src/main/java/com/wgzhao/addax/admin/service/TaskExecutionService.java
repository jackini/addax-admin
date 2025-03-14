package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.TaskExecution;
import com.wgzhao.addax.admin.repository.TaskExecutionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class TaskExecutionService {
    
    private final TaskExecutionRepository taskExecutionRepository;
    
    /**
     * 创建任务执行记录
     * @param taskExecution 任务执行记录对象
     * @return 创建的任务执行记录
     */
    @Transactional
    public TaskExecution createTaskExecution(TaskExecution taskExecution) {
        if (taskExecution.getStartTime() == null) {
            taskExecution.setStartTime(LocalDateTime.now());
        }
        
        if (taskExecution.getExecStatus() == null) {
            taskExecution.setExecStatus("WAITING");
        }
        
        TaskExecution savedExecution = taskExecutionRepository.save(taskExecution);
        log.info("已创建任务执行记录，ID: {}, 任务ID: {}", savedExecution.getId(), savedExecution.getCollectId());
        return savedExecution;
    }
    
    /**
     * 根据ID获取任务执行记录
     * @param id 执行记录ID
     * @return 任务执行记录
     */
    public Optional<TaskExecution> getTaskExecutionById(Long id) {
        return taskExecutionRepository.findById(id);
    }
    
    /**
     * 更新任务执行记录
     * @param taskExecution 任务执行记录对象
     * @return 更新后的任务执行记录
     */
    @Transactional
    public Optional<TaskExecution> updateTaskExecution(TaskExecution taskExecution) {
        if (!taskExecutionRepository.existsById(taskExecution.getId())) {
            return Optional.empty();
        }
        
        TaskExecution updatedExecution = taskExecutionRepository.save(taskExecution);
        log.info("已更新任务执行记录，ID: {}", updatedExecution.getId());
        return Optional.of(updatedExecution);
    }
    
    /**
     * 删除任务执行记录
     * @param id 执行记录ID
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteTaskExecution(Long id) {
        if (!taskExecutionRepository.existsById(id)) {
            return false;
        }
        
        taskExecutionRepository.deleteById(id);
        log.info("已删除任务执行记录，ID: {}", id);
        return true;
    }
    
    /**
     * 获取所有任务执行记录（分页）
     * @param pageable 分页参数
     * @return 分页任务执行记录
     */
    public Page<TaskExecution> getAllTaskExecutions(Pageable pageable) {
        return taskExecutionRepository.findAll(pageable);
    }
    
    /**
     * 根据任务ID获取执行记录
     * @param collectId 任务ID
     * @param limit 限制数量
     * @return 任务执行记录列表
     */
    public List<TaskExecution> getTaskExecutionsByCollectId(Long collectId, int limit) {
        return taskExecutionRepository.findByCollectIdOrderByStartTimeDesc(collectId, limit);
    }
    
    /**
     * 根据执行状态获取任务执行记录
     * @param status 执行状态
     * @return 任务执行记录列表
     */
    public List<TaskExecution> getTaskExecutionsByStatus(String status) {
        return taskExecutionRepository.findByExecStatus(status);
    }
    
    /**
     * 获取任务执行统计信息
     * @return 统计信息
     */
    public Map<String, Object> getTaskExecutionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总执行次数
        long totalCount = taskExecutionRepository.count();
        stats.put("totalCount", totalCount);
        
        // 各状态执行次数
        long successCount = taskExecutionRepository.countByExecStatus("SUCCESS");
        long failedCount = taskExecutionRepository.countByExecStatus("FAILED");
        long runningCount = taskExecutionRepository.countByExecStatus("RUNNING");
        long waitingCount = taskExecutionRepository.countByExecStatus("WAITING");
        
        stats.put("successCount", successCount);
        stats.put("failedCount", failedCount);
        stats.put("runningCount", runningCount);
        stats.put("waitingCount", waitingCount);
        
        // 成功率
        double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0;
        stats.put("successRate", String.format("%.2f%%", successRate));
        
        // 最近一次执行
        Optional<TaskExecution> latestExecution = taskExecutionRepository.findTopByOrderByStartTimeDesc();
        latestExecution.ifPresent(execution -> stats.put("latestExecution", execution));
        
        return stats;
    }
    
    /**
     * 更新任务执行状态
     * @param id 执行记录ID
     * @param status 新状态
     * @return 更新后的任务执行记录
     */
    @Transactional
    public Optional<TaskExecution> updateTaskExecutionStatus(Long id, String status) {
        Optional<TaskExecution> executionOpt = taskExecutionRepository.findById(id);
        
        if (executionOpt.isPresent()) {
            TaskExecution execution = executionOpt.get();
            execution.setExecStatus(status);
            
            TaskExecution updatedExecution = taskExecutionRepository.save(execution);
            log.info("已更新任务执行状态，ID: {}, 状态: {}", id, status);
            return Optional.of(updatedExecution);
        }
        
        return Optional.empty();
    }
    
    /**
     * 完成任务执行
     * @param id 执行记录ID
     * @param status 最终状态
     * @param errorMessage 错误信息（可选）
     * @return 更新后的任务执行记录
     */
    @Transactional
    public Optional<TaskExecution> completeTaskExecution(Long id, String status, String errorMessage) {
        Optional<TaskExecution> executionOpt = taskExecutionRepository.findById(id);
        
        if (executionOpt.isPresent()) {
            TaskExecution execution = executionOpt.get();
            LocalDateTime endTime = LocalDateTime.now();
            execution.setEndTime(endTime);
            execution.setExecStatus(status);
            
            // 计算执行时长（秒）
            if (execution.getStartTime() != null) {
                Duration duration = Duration.between(execution.getStartTime(), endTime);
                execution.setDuration((int) duration.getSeconds());
            }
            
            // 如果有错误信息，可以保存到executionJson中
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Map<String, Object> executionData = new HashMap<>();
                executionData.put("errorMessage", errorMessage);
                // 这里可以使用JSON库将Map转换为JSON字符串
                // 例如：execution.setExecutionJson(objectMapper.writeValueAsString(executionData));
                // 简化起见，这里直接设置
                execution.setExecutionJson("{\"errorMessage\": \"" + errorMessage.replace("\"", "\\\"") + "\"}");
            }
            
            TaskExecution updatedExecution = taskExecutionRepository.save(execution);
            log.info("已完成任务执行，ID: {}, 状态: {}, 耗时: {}秒", 
                    id, status, updatedExecution.getDuration());
            return Optional.of(updatedExecution);
        }
        
        return Optional.empty();
    }
}
