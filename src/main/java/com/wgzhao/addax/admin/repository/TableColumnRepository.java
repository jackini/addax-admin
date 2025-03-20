package com.wgzhao.addax.admin.repository;

import com.wgzhao.addax.admin.model.TableColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableColumnRepository
        extends JpaRepository<TableColumn, Long> {

    List<TableColumn> findByCollectId(Long id);

    List<TableColumn> findByCollectIdOrderByColumnPositionDesc(Long id);
}