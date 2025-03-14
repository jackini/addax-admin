package com.wgzhao.addax.admin.controller;

import com.wgzhao.addax.admin.model.TaskExecution;
import com.wgzhao.addax.admin.service.TaskExecutionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/task-executions")
@Slf4j
@AllArgsConstructor
public class TaskExecutionController {
    
    private final TaskExecutionService taskExecutionService;
    
    /**
     * 创建任务执行记录
     * @param taskExecution 任务执行记录对象
     * @return 创建的任务执行记录
     */
    @PostMapping
    public ResponseEntity<TaskExecution> createTaskExecution(@RequestBody TaskExecution taskExecution) {
        log.info("创建任务执行记录，任务ID: {}", taskExecution.getCollectId());
        TaskExecution createdExecution = taskExecutionService.createTaskExecution(taskExecution);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExecution);
    }
    
    /**
     * 根据ID获取任务执行记录
     * @param id 执行记录ID
     * @return 任务执行记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskExecution> getTaskExecution(@PathVariable Long id) {
        log.info("获取任务执行记录，ID: {}", id);
        Optional<TaskExecution> taskExecution = taskExecutionService.getTaskExecutionById(id);
        return taskExecution.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新任务执行记录
     * @param id 执行记录ID
     * @param taskExecution 更新的任务执行记录对象
     * @return 更新后的任务执行记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskExecution> updateTaskExecution(
            @PathVariable Long id, 
            @RequestBody TaskExecution taskExecution) {
        
        log.info("更新任务执行记录，ID: {}", id);
        taskExecution.setId(id);
        Optional<TaskExecution> updatedExecution = taskExecutionService.updateTaskExecution(taskExecution);
        
        return updatedExecution.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 删除任务执行记录
     * @param id 执行记录ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskExecution(@PathVariable Long id) {
        log.info("删除任务执行记录，ID: {}", id);
        boolean deleted = taskExecutionService.deleteTaskExecution(id);
        
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取所有任务执行记录（分页）
     * @param page 页码
     * @param size 每页大小
     * @param sort 排序字段
     * @param direction 排序方向
     * @return 分页任务执行记录
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTaskExecutions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        log.info("获取所有任务执行记录，页码: {}, 大小: {}", page, size);
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<TaskExecution> taskExecutions = taskExecutionService.getAllTaskExecutions(pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("content", taskExecutions.getContent());
        response.put("currentPage", taskExecutions.getNumber());
        response.put("totalItems", taskExecutions.getTotalElements());
        response.put("totalPages", taskExecutions.getTotalPages());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据任务ID获取执行记录
     * @param collectId 任务ID
     * @param limit 限制数量
     * @return 任务执行记录列表
     */
    @GetMapping("/task/{collectId}")
    public ResponseEntity<List<TaskExecution>> getTaskExecutionsByCollectId(
            @PathVariable Long collectId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("获取任务的执行记录，任务ID: {}, 限制: {}", collectId, limit);
        List<TaskExecution> executions = taskExecutionService.getTaskExecutionsByCollectId(collectId, limit);
        return ResponseEntity.ok(executions);
    }
    
    /**
     * 根据执行状态获取任务执行记录
     * @param status 执行状态
     * @return 任务执行记录列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskExecution>> getTaskExecutionsByStatus(@PathVariable String status) {
        log.info("获取指定状态的任务执行记录，状态: {}", status);
        List<TaskExecution> executions = taskExecutionService.getTaskExecutionsByStatus(status);
        return ResponseEntity.ok(executions);
    }
    
    /**
     * 获取任务执行统计信息
     * @return 统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTaskExecutionStats() {
        log.info("获取任务执行统计信息");
        Map<String, Object> stats = taskExecutionService.getTaskExecutionStats();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 更新任务执行状态
     * @param id 执行记录ID
     * @param status 新状态
     * @return 更新后的任务执行记录
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskExecution> updateTaskExecutionStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        log.info("更新任务执行状态，ID: {}, 新状态: {}", id, status);
        Optional<TaskExecution> updatedExecution = taskExecutionService.updateTaskExecutionStatus(id, status);
        
        return updatedExecution.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 完成任务执行
     * @param id 执行记录ID
     * @param status 最终状态
     * @param errorMessage 错误信息（可选）
     * @return 更新后的任务执行记录
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<TaskExecution> completeTaskExecution(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String errorMessage) {
        
        log.info("完成任务执行，ID: {}, 状态: {}", id, status);
        Optional<TaskExecution> completedExecution = 
                taskExecutionService.completeTaskExecution(id, status, errorMessage);
        
        return completedExecution.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}