package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.CollectTask;
import com.wgzhao.addax.admin.repository.JobTaskRelationRepository;
import com.wgzhao.addax.admin.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    
    private final TaskRepository taskRepository;
    private final JobTaskRelationRepository jobTaskRelationRepository;
    
    @Autowired
    public TaskService(TaskRepository taskRepository, JobTaskRelationRepository jobTaskRelationRepository) {
        this.taskRepository = taskRepository;
        this.jobTaskRelationRepository = jobTaskRelationRepository;
    }
    
    /**
     * 根据作业ID获取关联的任务列表
     * @param jobId 作业ID
     * @return 任务列表
     */
    public List<CollectTask> getTasksByJobId(Long jobId) {
        return taskRepository.findTasksByJobId(jobId);
    }
    
    /**
     * 更新任务状态
     * @param taskId 任务ID
     * @param status 新状态
     */
    @Transactional
    public void updateTaskStatus(Long taskId, String status) {
        CollectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        
        task.setTaskStatus(status);
        taskRepository.save(task);
        logger.info("已更新任务状态，ID: {}, 状态: {}", taskId, status);
    }
    
    /**
     * 创建新任务
     * @param task 任务对象
     * @return 保存后的任务对象
     */
    @Transactional
    public CollectTask createTask(CollectTask task) {
        CollectTask saved = taskRepository.save(task);
        logger.info("已创建新任务，ID: {}, 源表: {}", saved.getId(), saved.getSourceTable());
        return saved;
    }
    
    /**
     * 将任务关联到作业
     * @param jobId 作业ID
     * @param taskId 任务ID
     * @param order 执行顺序
     */
    @Transactional
    public void associateTaskWithJob(Long jobId, Long taskId, int order) {
        jobTaskRelationRepository.createJobTaskRelation(jobId, taskId, order);
        logger.info("已将任务 {} 关联到作业 {}, 执行顺序: {}", taskId, jobId, order);
    }
}