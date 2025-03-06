package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.model.CollectTaskResult;
import com.wgzhao.addax.admin.model.TaskExecution;
import com.wgzhao.addax.admin.repository.CollectTaskRepository;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TaskQueueService {
    @Autowired
    private TaskExecutionService taskExecutionService;
    @Autowired
    private CollectTaskRepository collectTaskRepository;

    private final BlockingQueue<Long> taskQueue = new LinkedBlockingQueue<>();

    public void enqueueTask(Long taskId) {
        taskQueue.offer(taskId);
    }

    @Async("queueConsumer")
    public void monitorTaskQueue() {
        while (true) {
            try {
                Long taskId = taskQueue.take();
                processTask(taskId);
            } catch (InterruptedException e) {
                log.error("Task queue monitoring interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processTask(Long taskId) {
        try {
            TaskExecution execution = taskExecutionService.getPendingExecution(taskId);
            CollectTask task = collectTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

            // Update execution status to RUNNING
            execution.setStatus("RUNNING");
            taskExecutionService.updateExecution(execution);

            // Execute Addax job
            CollectTaskResult result = taskExecutionService.executeAddaxTask(task);

            // Update execution record
            taskExecutionService.saveTaskExecution(result);

            // Update collect task status
            task.setStatus("C");
            collectTaskRepository.save(task);

        } catch (Exception e) {
            log.error("Failed to process task: {}", taskId, e);
            handleTaskFailure(taskId, e);
        }
    }

    private void handleTaskFailure(Long taskId, Exception e) {
        // Update task status to failed and log error
        TaskExecution execution = taskExecutionService.getExecution(taskId);
        execution.setStatus("FAILED");
        execution.setErrorMessage(e.getMessage());
        taskExecutionService.updateExecution(execution);

        // Optionally implement retry logic here
    }
    
    public void submitTask(CollectTaskRequest request) {
        log.info("Submitting task to queue: {}", request.getTaskName());
        // 异步执行任务
        executeTask(request);
    }
    
    public void submitTasks(Iterable<CollectTaskRequest> requests) {
        requests.forEach(this::submitTask);
    }
    
    @Async("taskExecutor")
    protected void executeTask(CollectTaskRequest request) {
        log.info("Start executing task: {}", request.getTaskName());
        try {
            // 调用任务执行服务执行Addax任务
            CollectTaskResult result = taskExecutionService.executeAddaxTask(request);
            // 处理执行结果
            processResult(result);
        } catch (Exception e) {
            log.error("Failed to execute task: {}", request.getTaskName(), e);
            // 构建失败结果并处理
            CollectTaskResult failedResult = CollectTaskResult.builder()
                .taskId(request.getTaskId())
                .batchId(request.getBatchId())
                .status("FAILED")
                .errorMessage(e.getMessage())
                .build();
            processResult(failedResult);
        }
    }
    
    @Async("resultProcessorExecutor")
    public void processResult(CollectTaskResult result) {
        log.info("Processing task result for task ID: {}, status: {}", result.getTaskId(), result.getStatus());
        try {
            // 更新任务执行记录
            taskExecutionService.saveTaskExecution(result);
        } catch (Exception e) {
            log.error("Failed to process task result: {}", result.getTaskId(), e);
        }
    }
}
