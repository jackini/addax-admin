package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.TaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {

    Long  getCollectIdById(long taskExecuteId);

    int countByCollectIdAndStartTimeAfter(long collectId, LocalDateTime startTime);

    TaskExecution findFirstByCollectIdAndStartTimeAfterOrderByStartTimeDesc(Long collectId, LocalDateTime startTime);

    int countByExecStatusNotAndCollectId(String status, long collectId);
}
