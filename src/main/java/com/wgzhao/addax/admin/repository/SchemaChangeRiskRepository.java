package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.SchemaChangeRisk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchemaChangeRiskRepository extends JpaRepository<SchemaChangeRisk, Long> {
    // You can add custom query methods here as needed
}