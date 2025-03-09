package com.wgzhao.addax.admin.scheduler;

import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.model.TaskExecution;
import com.wgzhao.addax.admin.service.CollectTaskService;
import com.wgzhao.addax.admin.service.RedisQueueService;
import com.wgzhao.addax.admin.service.TaskConsumerService;
import com.wgzhao.addax.admin.service.TaskExecutionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.SchedulingException;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class AddaxTaskScheduler
{
    private final CollectTaskService collectTaskService;
    private final TaskExecutionService taskExecutionService;
    private final TaskConsumerService taskConsumerService;

    private RedisQueueService redisQueueService;

    @Scheduled(fixedRate = 5000)
    public void pollExecuteTasks()
    {
        List<CollectTask> pendingTasks = collectTaskService.getNeedCollectTask();
        log.info("Found {} tasks to execute", pendingTasks.size());
        pendingTasks.forEach(task -> {
            if (shouldExecute(task)) {
                log.info("execution with collect id: {}", task.getId());
                Long taskId = taskExecutionService.createPendingExecution(task);
                log.info("Dispatching task {}", taskId);
                redisQueueService.enqueueTask(taskId.toString());
            }
        });
    }

    private boolean shouldExecute(CollectTask task)
    {
        if (task.getCronExpression() == null || task.getCronExpression().isEmpty()) {
            return false;
        }
        // 如果当前已经有正在运行的任务，不执行新的任务
        if (taskExecutionService.existTask(task.getId())) {
            return false;
        }
        try {
            CronExpression cronExpression = CronExpression.parse(task.getCronExpression());
            ZonedDateTime now = ZonedDateTime.now();
            TaskExecution lastExecution = taskExecutionService.getLastExecution(task.getId());

            ZonedDateTime lastExecutionTime = (lastExecution != null && lastExecution.getStartTime() != null)
                    ? lastExecution.getStartTime().atZone(ZoneId.systemDefault())
                    : null;
            return lastExecutionTime == null || cronExpression.next(lastExecutionTime).isBefore(now);
//            return LocalDateTime.now().isAfter(nextExecution.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        catch (SchedulingException e) {
            log.error("Invalid cron expression for task {}: {}", task.getId(), task.getCronExpression());
            return false;
        }
    }
}