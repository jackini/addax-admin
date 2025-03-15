package com.wgzhao.addax.admin.service;

import com.wgzhao.addax.admin.model.FieldMapping;
import com.wgzhao.addax.admin.repository.FieldMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FieldMappingService
{
    @Autowired
    private FieldMappingRepository fieldMappingRepository;

    public FieldMapping save(FieldMapping fieldMapping) {
        return fieldMappingRepository.save(fieldMapping);
    }

    public void delete(Integer id) {
        fieldMappingRepository.deleteById(id);
    }

    public FieldMapping getById(Integer id) {
        return fieldMappingRepository.findById(id).orElse(null);
    }

    public List<FieldMapping> getAll() {
        return fieldMappingRepository.findAll();
    }

    public String getTargetTypeBySourceType(String sourceType) {
        return fieldMappingRepository.findTargetTypeBySourceType(sourceType)
                .map(FieldMapping::getTargetType)
                .orElse("string"); // Default to original type if not found
    }
}
