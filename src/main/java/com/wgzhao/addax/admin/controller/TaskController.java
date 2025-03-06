package com.wgzhao.addax.admin.controller;


import com.wgzhao.addax.admin.model.CollectTaskRequest;
import com.wgzhao.addax.admin.service.CollectTaskService;
import com.wgzhao.addax.admin.service.TaskQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@Slf4j
public class TaskController {

    @Autowired
    private CollectTaskService collectTaskService;
    
    @Autowired
    private TaskQueueService taskQueueService;

    @PostMapping("/batch-execute")
    public ResponseEntity<?> batchExecuteTasks(@RequestBody List<Long> taskIds) {
        log.info("Received batch execution request for {} tasks", taskIds.size());
        
        List<CollectTaskRequest> requests = collectTaskService.prepareTaskRequests(taskIds);
        
        if (requests.isEmpty()) {
            return ResponseEntity.badRequest().body("No valid tasks found");
        }
        
        // 生成批次ID
        String batchId = UUID.randomUUID().toString();
        requests.forEach(req -> req.setBatchId(batchId));
        
        // 提交到队列
        taskQueueService.submitTasks(requests);
        
        return ResponseEntity.ok().body("Batch execution started with batch ID: " + batchId);
    }
    
    @PostMapping("/execute/{taskId}")
    public ResponseEntity<?> executeTask(@PathVariable Long taskId) {
        log.info("Received execution request for task ID: {}", taskId);
        
        CollectTaskRequest request = collectTaskService.prepareTaskRequest(taskId);
        
        if (request == null) {
            return ResponseEntity.badRequest().body("Task not found or not enabled");
        }
        
        // 生成批次ID
        String batchId = UUID.randomUUID().toString();
        request.setBatchId(batchId);
        
        // 提交到队列
        taskQueueService.submitTask(request);
        
        return ResponseEntity.ok().body("Task execution started with batch ID: " + batchId);
    }
}
