package com.wgzhao.addax.admin.repository;

import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.wgzhao.addax.admin.model.FieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FieldMappingRepository
        extends JpaRepository<FieldMapping, Integer>
{
    Optional<String> findTargetTypeBySourceType(String sourceType);
}
