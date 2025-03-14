package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.JobExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {
    
    /**
     * 根据作业ID查询执行记录
     * @param jobId 作业ID
     * @param pageable 分页参数
     * @return 执行记录列表
     */
    List<JobExecution> findByJobId(Long jobId, Pageable pageable);
    
    /**
     * 根据执行状态查询执行记录
     * @param execStatus 执行状态
     * @param pageable 分页参数
     * @return 分页执行记录
     */
    Page<JobExecution> findByExecStatus(String execStatus, Pageable pageable);
    
    /**
     * 查询指定时间段内的执行记录
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param pageable 分页参数
     * @return 分页执行记录
     */
    Page<JobExecution> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * 根据作业ID和执行状态查询执行记录
     * @param jobId 作业ID
     * @param execStatus 执行状态
     * @param pageable 分页参数
     * @return 分页执行记录
     */
    Page<JobExecution> findByJobIdAndExecStatus(Long jobId, String execStatus, Pageable pageable);
}