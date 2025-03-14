package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.CollectTask_;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectTaskRepository extends JpaRepository<CollectTask_, Long> {
    List<CollectTask_> findByTaskStatus(String n);
    // You can add custom query methods here as needed
}