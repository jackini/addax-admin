package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.TaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {
    
    /**
     * 查找指定任务的执行记录，按开始时间降序排序
     * @param collectId 任务ID
     * @param limit 限制数量
     * @return 执行记录列表
     */
    @Query(value = "SELECT * FROM task_execution WHERE collect_id = :collectId ORDER BY start_time DESC LIMIT :limit", 
           nativeQuery = true)
    List<TaskExecution> findByCollectIdOrderByStartTimeDesc(@Param("collectId") Long collectId, @Param("limit") int limit);
    
    /**
     * 查找指定任务的指定状态的执行记录
     * @param collectId 任务ID
     * @param status 状态
     * @return 执行记录列表
     */
    List<TaskExecution> findByCollectIdAndExecStatus(Long collectId, String status);
    
    /**
     * 查找指定状态的执行记录
     * @param status 状态
     * @return 执行记录列表
     */
    List<TaskExecution> findByExecStatus(String status);
}
