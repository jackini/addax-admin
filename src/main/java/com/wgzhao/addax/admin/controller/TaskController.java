package com.wgzhao.addax.admin.controller;


import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.scheduler.AddaxTaskScheduler;
import com.wgzhao.addax.admin.service.AddaxJobGenerator;
import com.wgzhao.addax.admin.service.CollectTaskService;
import com.wgzhao.addax.admin.service.TaskExecutionService;
import com.wgzhao.addax.admin.service.TaskQueueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@Slf4j
@AllArgsConstructor
public class TaskController {

    private CollectTaskService collectTaskService;
    
    private TaskQueueService taskQueueService;

    private AddaxJobGenerator addaxJobGenerator;

    private TaskExecutionService taskExecutionService;

    private AddaxTaskScheduler addaxTaskScheduler;


    @PostMapping("/batch-execute")
    public void batchExecuteTasks() {
        addaxTaskScheduler.pollExecuteTasks();
    }

    @PostMapping("/import-collect-tasks")
    public void batchImportCollectTasks(@RequestBody CollectTask collectTask) {
        if (collectTask == null) {
            return;
        }
        collectTaskService.save(collectTask);
    }
}
