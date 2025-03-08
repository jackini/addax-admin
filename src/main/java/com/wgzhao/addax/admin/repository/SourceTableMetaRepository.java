package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.SourceTableMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SourceTableMetaRepository extends JpaRepository<SourceTableMeta, Long> {

    List<SourceTableMeta> findByTaskId(Long id);
}