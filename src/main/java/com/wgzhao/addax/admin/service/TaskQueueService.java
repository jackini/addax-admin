package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.model.CollectTaskResult;
import com.wgzhao.addax.admin.model.TaskExecution;
import com.wgzhao.addax.admin.repository.CollectTaskRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import oracle.ucp.util.Task;
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
    @Autowired
    private SystemConfigService configService;

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
            TaskExecution execution = taskExecutionService.getTaskExecution(taskId);
            CollectTask task = collectTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

            // Update execution status to RUNNING
            execution.setStatus("RUNNING");
            taskExecutionService.updateExecution(execution);

            // Execute Addax job
            boolean result = executeAddaxTask(taskId);

            // Update collect task status
            task.setStatus(result ? "Y": "E");
            collectTaskRepository.save(task);

        } catch (Exception e) {
            log.error("Failed to process task: {}", taskId, e);
            handleTaskFailure(taskId, e);
        }
    }

    private void handleTaskFailure(Long taskId, Exception e) {
        // Update task status to failed and log error
        TaskExecution execution = taskExecutionService.getTaskExecution(taskId);
        execution.setStatus("FAILED");
        taskExecutionService.updateExecution(execution);

        // Optionally implement retry logic here
    }
    
    public void submitTask(long taskExecuteId) {
        log.info("Submitting task to queue: {}", taskExecuteId);
        // 异步执行任务
        executeTask(taskExecuteId);
    }
    

    @Async("taskExecutor")
    protected void executeTask(long taskExecuteId) {
        log.info("Start executing task: {}", taskExecuteId);
        try {
            // 调用任务执行服务执行Addax任务
            boolean result = executeAddaxTask(taskExecuteId);
            // 更新 CollectTask 表
            updateCollectTaskStatus(taskExecuteId, result);
        } catch (Exception e) {
            updateCollectTaskStatus(taskExecuteId, false);
        }
    }
    
    @Async("resultProcessorExecutor")
    public void updateCollectTaskStatus(long taskExecuteId, boolean result) {
        CollectTask collect = taskExecutionService.getCollect(taskExecuteId);
        collect.setStatus(result ? "Y": "E");
        // save
        collectTaskRepository.save(collect);
    }

    public boolean executeAddaxTask(long taskExecutionId) {
        TaskExecution taskExecution = taskExecutionService.getTaskExecution(taskExecutionId);
        LocalDateTime startTime = LocalDateTime.now();

        TaskExecution execution = new TaskExecution();
        execution.setStartTime(startTime);
        execution.setStatus("RUNNING");
        execution.setTriggerType("SCHEDULED");

        try {
            String addaxHome = configService.getAddaxHomePath();
            Map<String, Object> taskJson = taskExecution.getExecutionJson();
            // save the taskJson to a temporary file
            Path jsonPath = Path.of(addaxHome + "/log/task-" + taskExecution.getId() + ".json");
            Files.write(jsonPath, taskJson.toString().getBytes());
            Process process = Runtime.getRuntime().exec(new String[] {
                    "sh",
                    addaxHome + "/bin/addax.sh",
                    jsonPath.toString(),
            });

            boolean completed = process.waitFor(7200, TimeUnit.SECONDS);

            if (!completed) {
                process.destroyForcibly();
                taskExecution.setStatus("FAILED");
                taskExecutionService.save(taskExecution);
                throw new RuntimeException("Task timed out after 7200 seconds");
            }

            String output = readProcessOutput(process);
            setTaskStatistic(taskExecution, output);
            taskExecution.setLogPath(jsonPath.toString());
            taskExecution.setEndTime(LocalDateTime.now());
            taskExecution.setDuration((int) Duration.between(startTime, taskExecution.getEndTime()).getSeconds());
            taskExecutionService.save(taskExecution);
            return true;
        } catch (Exception e) {
            log.error("Addax task execution failed for task {}", taskExecutionId, e);
            taskExecution.setStatus("FAILED");
            taskExecutionService.save(taskExecution);
            return false;
        }
    }



    private String readProcessOutput(Process process) throws Exception {
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        return output.toString();
    }

    /**
     * 解析Addax输出的最后9行，提取统计信息
     * Job start  at             : 2025-02-22 01:30:35
     * Job end    at             : 2025-02-22 01:30:45
     * Job took secs             :                  9s
     * Average   bps             :              419B/s
     * Average   rps             :              7rec/s
     * Number of rec             :                  21
     * Failed record             :                   0
     * @param output Addax输出
     */
    private void setTaskStatistic(TaskExecution taskExecution, String output) {
        // Split the output into lines
        String[] lines = output.split("\n");
        // Extract the last 9 lines
        String[] stats = Arrays.copyOfRange(lines, lines.length - 9, lines.length);
        taskExecution.setStatus("SUCCESS");
        taskExecution.setTotalRecords(Long.parseLong(stats[5].split(":")[1].trim()));
        taskExecution.setSuccessRecords(taskExecution.getTotalRecords() - Long.parseLong(stats[6].split(":")[1].trim()));
        taskExecution.setFailedRecords(Long.parseLong(stats[6].split(":")[1].trim()));
        taskExecution.setBytesSpeed(Long.parseLong(stats[3].split(":")[1].trim().replace("B/s", "")));
        taskExecution.setRecordsSpeed(Long.parseLong(stats[4].split(":")[1].trim().replace("rec/s", "")));
    }
}
