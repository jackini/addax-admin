package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.model.TaskExecuteResult;
import com.wgzhao.addax.admin.model.TaskExecution;
import com.wgzhao.addax.admin.repository.TaskExecutionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Service
@Slf4j
public class TaskExecutionService {
    @Autowired
    private TaskExecutionRepository taskExecutionRepository;
    
    @Autowired
    private SystemConfigService configService;
    
    @Autowired
    private AddaxJobGenerator jobGenerator;

    public CollectTaskResult executeAddaxTask(CollectTask task) {
        LocalDateTime startTime = LocalDateTime.now();
        
        TaskExecution execution = new TaskExecution();
        execution.setTaskId(task.getId());
        execution.setStartTime(startTime);
        execution.setStatus("RUNNING");
        execution.setTriggerType("SCHEDULED");
        taskExecutionRepository.save(execution);

        try {
            String addaxHome = configService.getAddaxHomePath();
            String jobConfigPath = jobGenerator.generateJobConfig(task);

            Process process = Runtime.getRuntime().exec(new String[] {
                "sh",
                addaxHome + "/bin/addax.sh",
                jobConfigPath
            });

            boolean completed = process.waitFor(task.getTimeoutSeconds(), TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroyForcibly();
                throw new RuntimeException("Task timed out after " + task.getTimeoutSeconds() + " seconds");
            }

            String output = readProcessOutput(process);
            CollectTaskResult result = parseAddaxOutput(output);
            result.setTaskId(task.getId());
            result.setStartTime(startTime);
            result.setEndTime(LocalDateTime.now());
            result.setDuration((int) Duration.between(startTime, result.getEndTime()).getSeconds());
            result.setLogPath(configService.getAddaxLogPath() + "/" + task.getId() + ".log");

            return result;
        } catch (Exception e) {
            log.error("Addax task execution failed for task {}", task.getId(), e);
            return CollectTaskResult.builder()
                .taskId(task.getId())
                .status("FAILED")
                .errorMessage(e.getMessage())
                .startTime(startTime)
                .endTime(LocalDateTime.now())
                .build();
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
    
    private TaskExecuteResult parseAddaxOutput(String output) {
        // 这里需要根据Addax的输出格式解析结果
        // 示例代码，实际实现需要根据Addax输出格式调整
        CollectTaskResult.CollectTaskResultBuilder builder = CollectTaskResult.builder();
        
        // 假设正常完成
        builder.status("SUCCESS");
        
        // 解析统计信息
        if (output.contains("Total records")) {
            // 示例代码，需要根据实际输出格式进行正则匹配
            builder.totalRecords(10000L);  // 示例值
            builder.successRecords(9950L); // 示例值
            builder.failedRecords(50L);    // 示例值
            builder.rejectedRecords(0L);   // 示例值
            builder.bytesSpeed(1024000L);  // 示例值
            builder.recordsSpeed(1000L);   // 示例值
        }
        
        return builder.build();
    }

    @Transactional
    public void saveTaskExecution(CollectTaskResult result) {
        TaskExecution execution = taskExecutionRepository.findByTaskIdAndBatchId(
            result.getTaskId(), result.getBatchId())
            .orElseThrow(() -> new RuntimeException("Task execution record not found"));
        
        // 更新执行记录
        execution.setEndTime(result.getEndTime());
        execution.setDuration(result.getDuration());
        execution.setStatus(result.getStatus());
        execution.setTotalRecords(result.getTotalRecords());
        execution.setSuccessRecords(result.getSuccessRecords());
        execution.setFailedRecords(result.getFailedRecords());
        execution.setRejectedRecords(result.getRejectedRecords());
        execution.setBytesSpeed(result.getBytesSpeed());
        execution.setRecordsSpeed(result.getRecordsSpeed());
        execution.setErrorMessage(result.getErrorMessage());
        execution.setAddaxLogPath(result.getLogPath());
        
        taskExecutionRepository.save(execution);
        
        log.info("Task execution record updated for task ID: {}", result.getTaskId());
    }

    // 找到所有需要运行的任务
    @Transactional
    public void createPendingExecution(CollectTask task) {
        TaskExecution execution = new TaskExecution();
        execution.setTaskId(task.getId());
        execution.setStartTime(LocalDateTime.now());
        execution.setStatus("WAITTING");
        execution.setTriggerType("SCHEDULED");
        taskExecutionRepository.save(execution);
    }

    @Transactional
    public void updateExecution(TaskExecution execution) {
        taskExecutionRepository.save(execution);
    }
}
