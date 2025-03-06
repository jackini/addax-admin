package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.TaskExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Integer> {
    
}
