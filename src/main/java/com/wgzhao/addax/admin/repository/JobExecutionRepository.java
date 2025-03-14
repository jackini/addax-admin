package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {
    
    /**
     * 查找指定作业的执行记录，按开始时间降序排序
     * @param jobId 作业ID
     * @param limit 限制数量
     * @return 执行记录列表
     */
    @Query(value = "SELECT * FROM job_execution WHERE job_id = :jobId ORDER BY start_time DESC LIMIT :limit", 
           nativeQuery = true)
    List<JobExecution> findByJobIdOrderByStartTimeDesc(@Param("jobId") Long jobId, @Param("limit") int limit);
    
    /**
     * 查找指定作业的指定状态的执行记录
     * @param jobId 作业ID
     * @param statuses 状态列表
     * @return 执行记录列表
     */
    List<JobExecution> findByJobIdAndExecStatusIn(Long jobId, List<String> statuses);
    
    /**
     * 查找指定状态的执行记录
     * @param status 状态
     * @return 执行记录列表
     */
    List<JobExecution> findByExecStatus(String status);
}