package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.CollectTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<CollectTask, Long> {
    
    /**
     * 根据作业ID查找关联的任务
     * @param jobId 作业ID
     * @return 任务列表
     */
    @Query("SELECT t FROM CollectTask t JOIN JobTaskRelation r ON t.id = r.taskId " +
           "WHERE r.jobId = :jobId ORDER BY r.taskOrder")
    List<CollectTask> findTasksByJobId(@Param("jobId") Long jobId);
    
    /**
     * 根据数据源ID查找任务
     * @param sourceId 数据源ID
     * @return 任务列表
     */
    List<CollectTask> findBySourceId(Integer sourceId);
    
    /**
     * 根据源表名查找任务
     * @param sourceTable 源表名
     * @return 任务列表
     */
    List<CollectTask> findBySourceTable(String sourceTable);
    
    /**
     * 根据任务状态查找任务
     * @param taskStatus 任务状态
     * @return 任务列表
     */
    List<CollectTask> findByTaskStatus(String taskStatus);
}