package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Integer> {
    // You can add custom query methods here as needed
}