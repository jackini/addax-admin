package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.HdfsTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HdfsTemplateRepository extends JpaRepository<HdfsTemplate, Integer> {
    // You can add custom query methods here as needed
}