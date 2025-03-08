package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.TaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {

    Long  getCollectIdById(long taskExecuteId);

    @Query(value = "SELECT COUNT(*) FROM TaskExecution e  WHERE e.collectId = :collectId AND e.startTime > CURRENT_DATE")
    int countValidTaskExecution(long collectId);
}
