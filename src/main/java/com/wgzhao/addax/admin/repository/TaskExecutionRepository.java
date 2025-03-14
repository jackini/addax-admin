package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.TaskExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {
    
    /**
     * 根据任务ID查询执行记录，按开始时间降序排序
     * @param collectId 任务ID
     * @param limit 限制数量
     * @return 执行记录列表
     */
    @Query(value = "SELECT t FROM TaskExecution t WHERE t.collectId = :collectId ORDER BY t.startTime DESC LIMIT :limit", 
           nativeQuery = true)
    List<TaskExecution> findByCollectIdOrderByStartTimeDesc(@Param("collectId") Long collectId, @Param("limit") int limit);
    
    /**
     * 根据执行状态查询执行记录
     * @param execStatus 执行状态
     * @return 执行记录列表
     */
    List<TaskExecution> findByExecStatus(String execStatus);
    
    /**
     * 统计指定状态的执行记录数量
     * @param execStatus 执行状态
     * @return 记录数量
     */
    long countByExecStatus(String execStatus);
    
    /**
     * 查询最近一次执行记录
     * @return 执行记录
     */
    Optional<TaskExecution> findTopByOrderByStartTimeDesc();
    
    /**
     * 查询指定时间段内的执行记录
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param pageable 分页参数
     * @return 分页执行记录
     */
    Page<TaskExecution> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * 查询指定任务ID和状态的执行记录
     * @param collectId 任务ID
     * @param execStatus 执行状态
     * @return 执行记录列表
     */
    List<TaskExecution> findByCollectIdAndExecStatus(Long collectId, String execStatus);
    
    /**
     * 删除指定任务ID的所有执行记录
     * @param collectId 任务ID
     */
    void deleteByCollectId(Long collectId);
}
