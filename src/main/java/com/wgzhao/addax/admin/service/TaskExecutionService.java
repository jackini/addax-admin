package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.constant.ExecStatus;
import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.model.CollectTaskResult;
import com.wgzhao.addax.admin.model.TaskExecuteResult;
import com.wgzhao.addax.admin.model.TaskExecution;
import com.wgzhao.addax.admin.repository.CollectTaskRepository;
import com.wgzhao.addax.admin.repository.TaskExecutionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TaskExecutionService {

    @Autowired
    private SystemConfigService configService;

    @Autowired
    private TaskExecutionRepository taskExecutionRepository;

    @Autowired
    private CollectTaskRepository collectTaskRepository;

    @Autowired
    private AddaxJobGenerator addaxJobGenerator;


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
}
