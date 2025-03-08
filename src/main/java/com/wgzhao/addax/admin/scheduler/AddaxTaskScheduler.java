package com.wgzhao.addax.admin.scheduler;

import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.service.CollectTaskService;
import com.wgzhao.addax.admin.service.TaskExecutionService;
import com.wgzhao.addax.admin.service.TaskQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.SchedulingException;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
@Slf4j
public class AddaxTaskScheduler
{
    private final CollectTaskService collectTaskService;
    private final TaskExecutionService taskExecutionService;
    private final TaskQueueService taskQueueService;

    private ZonedDateTime lastRunTime;

    public AddaxTaskScheduler(CollectTaskService collectTaskService,
                       TaskExecutionService taskExecutionService,
                       TaskQueueService taskQueueService) {
        this.collectTaskService = collectTaskService;
        this.taskExecutionService = taskExecutionService;
        this.taskQueueService = taskQueueService;
    }

    @Scheduled(fixedRate = 5000)
    public void pollExecuteTasks() {
        List<CollectTask> pendingTasks = collectTaskService.getNeedCollectTask();
        pendingTasks.forEach(task -> {
            if (shouldExecute(task)) {
                log.info("Dispatching task: {}", task.getId());
//                taskExecutionService.createPendingExecution(task);
                taskQueueService.enqueueTask(task.getId());
            }
        });
    }

    private boolean shouldExecute(CollectTask task) {
        if (task.getCronExpression() == null || task.getCronExpression().isEmpty()) {
            return false;
        }
        // 如果当前已经有正在运行的任务，不执行新的任务
        if (taskExecutionService.existTask(task.getId())) {
            return false;
        }
        try {
            CronExpression cronExpression =  CronExpression.parse(task.getCronExpression());
            ZonedDateTime now = ZonedDateTime.now();
            if (lastRunTime == null || cronExpression.next(lastRunTime).isBefore(now)) {
                lastRunTime = now;
                return true;
            }
            return false;
//            return LocalDateTime.now().isAfter(nextExecution.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        } catch (SchedulingException e) {
            log.error("Invalid cron expression for task {}: {}", task.getId(), task.getCronExpression());
            return false;
        }
    }
}