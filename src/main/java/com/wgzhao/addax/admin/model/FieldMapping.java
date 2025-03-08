package com.wgzhao.addax.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "field_mapping_rule")
@Getter
public class FieldMapping
{
    @Id
    private Integer id;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "target_type")
    private String targetType;
}
