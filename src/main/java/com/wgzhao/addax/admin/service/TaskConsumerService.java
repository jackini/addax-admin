//package com.wgzhao.addax.admin.service;
//
//import com.wgzhao.addax.admin.constant.ExecStatus;
//import com.wgzhao.addax.admin.model.CollectTask_;
//import com.wgzhao.addax.admin.model.TaskExecution_;
//import com.wgzhao.addax.admin.repository.CollectTaskRepository;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.Duration;
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Service
//@Slf4j
//public class TaskConsumerService
//{
//    @Autowired
//    private TaskExecutionService taskExecutionService;
//    @Autowired
//    private CollectTaskRepository collectTaskRepository;
//    @Autowired
//    private SystemConfigService configService;
//
//    @Autowired
//    private RedisQueueService redisQueueService; // 队列服务，用于拉取任务
//
//
//    @Async("queueConsumer")
//    public void monitorTaskQueue() {
//        while (!Thread.currentThread().isInterrupted()) {
//            try {
//                // 阻塞式从 Redis 队列中获取下一任务
//                String taskId = redisQueueService.dequeueTask();
//                if (taskId != null) {
//                    log.info("消费任务: {}", taskId);
//                    processTask(Long.parseLong(taskId));
//                }
//            } catch (Exception ex) {
//                log.error("消费任务时出现异常: ", ex);
//            }
//        }
//    }
//
//    private void processTask(Long taskId) {
//        try {
//            TaskExecution_ execution = taskExecutionService.getTaskExecution(taskId);
//            CollectTask_ task = collectTaskRepository.findById(taskId)
//                .orElse(null);
//            if (task == null) {
//                log.error("Task not found: {}", taskId);
//                return;
//            }
//
//            // Update execution status to RUNNING
//            execution.setExecStatus(ExecStatus.RUNNING.getCode());
//            taskExecutionService.updateExecution(execution);
//
//            // Execute Addax job
//            boolean result = executeAddaxTask(taskId);
//
//            // Update collect task status
//            task.setTaskStatus(result ? "Y": "E");
//            collectTaskRepository.save(task);
//
//        } catch (Exception e) {
//            log.error("Failed to process task: {}", taskId, e);
//            handleTaskFailure(taskId, e);
//        }
//    }
//
//    private void handleTaskFailure(Long taskId, Exception e) {
//        // Update task status to failed and log error
//        TaskExecution_ execution = taskExecutionService.getTaskExecution(taskId);
//        execution.setExecStatus(ExecStatus.FAILED.getCode());
//        taskExecutionService.updateExecution(execution);
//
//        // Optionally implement retry logic here
//    }
//    
//    public void submitTask(long taskExecuteId) {
//        log.info("Submitting task to queue: {}", taskExecuteId);
//        // 异步执行任务
//        executeTask(taskExecuteId);
//    }
//    
//
//    @Async("taskExecutor")
//    protected void executeTask(long taskExecuteId) {
//        log.info("Start executing task: {}", taskExecuteId);
//        try {
//            // 调用任务执行服务执行Addax任务
//            boolean result = executeAddaxTask(taskExecuteId);
//            // 更新 CollectTask 表
//            updateCollectTaskStatus(taskExecuteId, result);
//        } catch (Exception e) {
//            updateCollectTaskStatus(taskExecuteId, false);
//        }
//    }
//    
//    @Async("updateCollect")
//    public void updateCollectTaskStatus(long taskExecuteId, boolean result) {
//        CollectTask_ collect = taskExecutionService.getCollect(taskExecuteId);
//        collect.setTaskStatus(result ? "Y": "E");
//        // save
//        collectTaskRepository.save(collect);
//    }
//
//    public boolean executeAddaxTask(long taskExecutionId) {
//        TaskExecution_ taskExecution = taskExecutionService.getTaskExecution(taskExecutionId);
//        LocalDateTime startTime = LocalDateTime.now();
//
//        TaskExecution_ execution = new TaskExecution_();
//        execution.setStartTime(startTime);
//        execution.setExecStatus(ExecStatus.RUNNING.getCode());
//        execution.setTriggerType("SCHEDULED");
//
//        try {
//            String addaxHome = configService.getAddaxHomePath();
//            Map<String, Object> taskJson = taskExecution.getExecutionJson();
//            // save the taskJson to a temporary file
//            Path jsonPath = Path.of(addaxHome + "/log/task-" + taskExecution.getId() + ".json");
//            Files.write(jsonPath, taskJson.toString().getBytes());
//            Process process = Runtime.getRuntime().exec(new String[] {
//                    "sh",
//                    addaxHome + "/bin/addax.sh",
//                    jsonPath.toString(),
//            });
//
//            boolean completed = process.waitFor(7200, TimeUnit.SECONDS);
//
//            if (!completed) {
//                process.destroyForcibly();
//                taskExecution.setExecStatus(ExecStatus.FAILED.getCode());
//                taskExecutionService.save(taskExecution);
//                throw new RuntimeException("Task timed out after 7200 seconds");
//            }
//
//            String output = readProcessOutput(process);
//            setTaskStatistic(taskExecution, output);
//            taskExecution.setLogPath(jsonPath.toString());
//            taskExecution.setEndTime(LocalDateTime.now());
//            taskExecution.setDuration((int) Duration.between(startTime, taskExecution.getEndTime()).getSeconds());
//            taskExecutionService.save(taskExecution);
//            return true;
//        } catch (Exception e) {
//            log.error("Addax task execution failed for task {}", taskExecutionId, e);
//            taskExecution.setExecStatus(ExecStatus.FAILED.getCode());
//            taskExecutionService.save(taskExecution);
//            return false;
//        }
//    }
//
//
//
//    private String readProcessOutput(Process process) throws Exception {
//        StringBuilder output = new StringBuilder();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
//        String line;
//        while ((line = reader.readLine()) != null) {
//            output.append(line).append("\n");
//        }
//
//        return output.toString();
//    }
//
//    /**
//     * 解析Addax输出的最后9行，提取统计信息
//     * Job start  at             : 2025-02-22 01:30:35
//     * Job end    at             : 2025-02-22 01:30:45
//     * Job took secs             :                  9s
//     * Average   bps             :              419B/s
//     * Average   rps             :              7rec/s
//     * Number of rec             :                  21
//     * Failed record             :                   0
//     * @param output Addax输出
//     */
//    private void setTaskStatistic(TaskExecution_ taskExecution, String output) {
//        // Split the output into lines
//        String[] lines = output.split("\n");
//        // Extract the last 9 lines
//        String[] stats = Arrays.copyOfRange(lines, lines.length - 9, lines.length);
//        taskExecution.setExecStatus(ExecStatus.SUCCESS.getCode());
//        taskExecution.setTotalRecords(Long.parseLong(stats[5].split(":")[1].trim()));
//        taskExecution.setSuccessRecords(taskExecution.getTotalRecords() - Long.parseLong(stats[6].split(":")[1].trim()));
//        taskExecution.setFailedRecords(Long.parseLong(stats[6].split(":")[1].trim()));
//        taskExecution.setBytesSpeed(Long.parseLong(stats[3].split(":")[1].trim().replace("B/s", "")));
//        taskExecution.setRecordsSpeed(Long.parseLong(stats[4].split(":")[1].trim().replace("rec/s", "")));
//    }
//
//    /**
//     * 在容器启动时自动调用 monitorTaskQueue()，开启队列监控
//     */
//    @PostConstruct
//    public void init() {
//        log.info("TaskQueueService 初始化，启动队列消费者线程");
//        monitorTaskQueue();
//    }
//
//    @PreDestroy
//    public void shutdown() {
//        log.info("TaskQueueService 正在关闭，停止队列监控线程");
//        // 线程自动退出时会处理中断状态
//    }
//}
