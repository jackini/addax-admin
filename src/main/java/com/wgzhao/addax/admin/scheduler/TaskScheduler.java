package com.wgzhao.addax.admin.scheduler;

import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.service.CollectTaskService;
import com.wgzhao.addax.admin.service.TaskExecutionService;
import com.wgzhao.addax.admin.service.TaskQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.SchedulingException;
import java.util.Date;
import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class TaskScheduler {
    private final CollectTaskService collectTaskService;
    private final TaskExecutionService taskExecutionService;
    private final TaskQueueService taskQueueService;

    public TaskScheduler(CollectTaskService collectTaskService, 
                       TaskExecutionService taskExecutionService,
                       TaskQueueService taskQueueService) {
        this.collectTaskService = collectTaskService;
        this.taskExecutionService = taskExecutionService;
        this.taskQueueService = taskQueueService;
    }

    @Scheduled(fixedRate = 5000)
    public void pollCollectTasks() {
        List<CollectTask> pendingTasks = collectTaskService.getCollectTasks();
        pendingTasks.forEach(task -> {
            if (shouldExecute(task)) {
                log.info("Dispatching task: {}", task.getId());
                taskExecutionService.createPendingExecution(task);
                taskQueueService.enqueueTask(task.getId());
            }
        });
    }

    private boolean shouldExecute(CollectTask task) {
        if (task.getCronExpression() == null || task.getCronExpression().isEmpty()) {
            return false;
        }
        // 如果当前已经有正在运行的任务，不执行新的任务
        if (taskExecutionService.getRunningExecutionCount(task.getId()) > 0) {
            return false;
        }
        try {
            CronSequenceGenerator cronTrigger = new CronSequenceGenerator(task.getCronExpression());
            Date nextExecution = cronTrigger.next(Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC)));
            return LocalDateTime.now().isAfter(nextExecution.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        } catch (SchedulingException e) {
            log.error("Invalid cron expression for task {}: {}", task.getId(), task.getCronExpression());
            return false;
        }
    }
}