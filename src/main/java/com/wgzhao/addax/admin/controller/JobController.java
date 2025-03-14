package com.wgzhao.addax.admin.controller;

import com.wgzhao.addax.admin.model.CollectJob;
import com.wgzhao.addax.admin.model.JobExecution;
import com.wgzhao.addax.admin.service.CronService;
import com.wgzhao.addax.admin.service.JobExecutionService;
import com.wgzhao.addax.admin.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private static final Logger logger = LoggerFactory.getLogger(JobController.class);
    
    private final JobService jobService;
    private final JobExecutionService jobExecutionService;
    private final CronService cronService;
    
    @Autowired
    public JobController(JobService jobService, 
                        JobExecutionService jobExecutionService,
                        CronService cronService) {
        this.jobService = jobService;
        this.jobExecutionService = jobExecutionService;
        this.cronService = cronService;
    }
    
    /**
     * 获取所有作业
     */
    @GetMapping
    public ResponseEntity<List<CollectJob>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }
    
    /**
     * 获取指定作业
     */
    @GetMapping("/{id}")
    public ResponseEntity<CollectJob> getJob(@PathVariable Long id) {
        return jobService.getJob(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建新作业
     */
    @PostMapping
    public ResponseEntity<CollectJob> createJob(@RequestBody CollectJob job) {
        // 验证cron表达式
        if (!cronService.isValidCronExpression(job.getCronExpression())) {
            return ResponseEntity.badRequest().build();
        }
        
        // 计算下次执行时间
        job.setNextFireTime(cronService.getNextExecutionTime(job.getCronExpression(), LocalDateTime.now()));
        
        CollectJob createdJob = jobService.createJob(job);
        return ResponseEntity.ok(createdJob);
    }
    
    /**
     * 更新作业
     */
    @PutMapping("/{id}")
    public ResponseEntity<CollectJob> updateJob(@PathVariable Long id, @RequestBody CollectJob job) {
        if (!jobService.getJob(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        // 验证cron表达式
        if (!cronService.isValidCronExpression(job.getCronExpression())) {
            return ResponseEntity.badRequest().build();
        }
        
        job.setId(id);
        // 计算下次执行时间
        job.setNextFireTime(cronService.getNextExecutionTime(job.getCronExpression(), LocalDateTime.now()));
        
        CollectJob updatedJob = jobService.updateJob(job);
        return ResponseEntity.ok(updatedJob);
    }
    
    /**
     * 删除作业
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        if (!jobService.getJob(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        jobService.deleteJob(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 手动触发作业
     */
    @PostMapping("/{id}/trigger")
    public ResponseEntity<Map<String, Object>> triggerJob(@PathVariable Long id) {
        if (!jobService.getJob(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Long executionId = jobService.triggerJob(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("jobId", id);
        response.put("executionId", executionId);
        response.put("message", "Job triggered successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 暂停作业
     */
    @PostMapping("/{id}/pause")
    public ResponseEntity<CollectJob> pauseJob(@PathVariable Long id) {
        if (!jobService.getJob(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        CollectJob job = jobService.pauseJob(id);
        return ResponseEntity.ok(job);
    }
    
    /**
     * 恢复作业
     */
    @PostMapping("/{id}/resume")
    public ResponseEntity<CollectJob> resumeJob(@PathVariable Long id) {
        if (!jobService.getJob(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        CollectJob job = jobService.resumeJob(id);
        return ResponseEntity.ok(job);
    }
    
    /**
     * 获取作业执行历史
     */
    @GetMapping("/{id}/executions")
    public ResponseEntity<List<JobExecution>> getJobExecutions(@PathVariable Long id, 
                                                              @RequestParam(defaultValue = "10") int limit) {
        if (!jobService.getJob(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        List<JobExecution> executions = jobExecutionService.getRecentJobExecutions(id, limit);
        return ResponseEntity.ok(executions);
    }
    
    /**
     * 获取作业的下一次执行时间
     */
    @GetMapping("/{id}/next-executions")
    public ResponseEntity<Map<String, Object>> getNextExecutions(@PathVariable Long id, 
                                                               @RequestParam(defaultValue = "5") int count) {
        return jobService.getJob(id)
                .map(job -> {
                    LocalDateTime[] nextTimes = cronService.getNextExecutionTimes(
                            job.getCronExpression(), LocalDateTime.now(), count);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("jobId", id);
                    response.put("jobName", job.getJobName());
                    response.put("cronExpression", job.getCronExpression());
                    response.put("nextExecutions", nextTimes);
                    
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 关联任务到作业
     */
    @PostMapping("/{jobId}/tasks/{taskId}")
    public ResponseEntity<Map<String, Object>> associateTask(@PathVariable Long jobId, 
                                                           @PathVariable Long taskId,
                                                           @RequestParam(defaultValue = "1") int order) {
        if (!jobService.getJob(jobId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        jobService.associateTaskWithJob(jobId, taskId, order);
        
        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("taskId", taskId);
        response.put("order", order);
        response.put("message", "Task associated with job successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 解除任务与作业的关联
     */
    @DeleteMapping("/{jobId}/tasks/{taskId}")
    public ResponseEntity<Void> disassociateTask(@PathVariable Long jobId, @PathVariable Long taskId) {
        if (!jobService.getJob(jobId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        jobService.disassociateTaskFromJob(jobId, taskId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 获取作业关联的任务列表
     */
    @GetMapping("/{jobId}/tasks")
    public ResponseEntity<List<Map<String, Object>>> getJobTasks(@PathVariable Long jobId) {
        if (!jobService.getJob(jobId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Map<String, Object>> tasks = jobService.getTasksWithOrderByJobId(jobId);
        return ResponseEntity.ok(tasks);
    }
}