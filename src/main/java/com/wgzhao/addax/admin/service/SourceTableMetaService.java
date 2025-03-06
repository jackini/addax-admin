package com.wgzhao.addax.admin.service;


import com.wgzhao.addax.admin.model.SourceTableMeta;
import com.wgzhao.addax.admin.repository.SourceTableMetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SourceTableMetaService {
    private final SourceTableMetaRepository repository;

    @Autowired
    public SourceTableMetaService(SourceTableMetaRepository repository) {
        this.repository = repository;
    }

    public SourceTableMeta saveMeta(SourceTableMeta meta) {
        return repository.save(meta);
    }

    public void updateMeta(SourceTableMeta meta) {
        repository.save(meta);
    }

    public void deleteMeta(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public SourceTableMeta getById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<SourceTableMeta> getAllByTaskId(Long taskId) {
        return repository.findByTaskId(taskId);
    }

    @Transactional(readOnly = true)
    public List<SourceTableMeta> getAll() {
        return repository.findAll();
    }


    public List<String> getColumns(Long id) {
        return repository.getColumnNameByTaskId(id);
    }
}