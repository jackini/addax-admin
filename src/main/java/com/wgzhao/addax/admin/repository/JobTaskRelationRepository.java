package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.JobTaskRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface JobTaskRelationRepository extends JpaRepository<JobTaskRelation, Long> {
    
    /**
     * 查找指定作业的任务关联
     * @param jobId 作业ID
     * @return 任务关联列表
     */
    List<JobTaskRelation> findByJobIdOrderByTaskOrder(Long jobId);
    
    /**
     * 查找指定任务的作业关联
     * @param taskId 任务ID
     * @return 作业关联列表
     */
    List<JobTaskRelation> findByTaskId(Long taskId);
    
    /**
     * 删除指定作业的所有任务关联
     * @param jobId 作业ID
     */
    @Modifying
    @Transactional
    void deleteByJobId(Long jobId);
    
    /**
     * 创建作业任务关联
     * @param jobId 作业ID
     * @param taskId 任务ID
     * @param taskOrder 任务顺序
     */
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO job_task_relation (job_id, task_id, task_order, create_time) VALUES (:jobId, :taskId, :taskOrder, NOW())", 
           nativeQuery = true)
    void createJobTaskRelation(@Param("jobId") Long jobId, @Param("taskId") Long taskId, @Param("taskOrder") int taskOrder);
    
    /**
     * 查找指定作业和任务的关联
     * @param jobId 作业ID
     * @param taskId 任务ID
     * @return 任务关联对象
     */
    JobTaskRelation findByJobIdAndTaskId(Long jobId, Long taskId);
}