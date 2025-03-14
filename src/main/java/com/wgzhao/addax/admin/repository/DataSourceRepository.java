package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.DataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataSourceRepository extends JpaRepository<DataSource, Integer> {
    
    /**
     * 根据数据库类型查询数据源
     * @param dbType 数据库类型
     * @return 数据源列表
     */
    List<DataSource> findByDbType(String dbType);
}