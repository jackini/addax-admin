package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.SourceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SourceTemplateRepository extends JpaRepository<SourceTemplate, Integer> {
    // You can add custom query methods here as needed
}