package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.SourceTableMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SourceTableMetaRepository extends JpaRepository<SourceTableMeta, Long> {
    List<SourceTableMeta> findByTaskId(Long taskId);

    List<String> getColumnNameByTaskId(Long id);
}