package com.wgzhao.addax.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "field_mapping_rule")
@Getter
@Setter
@Data
public class FieldMapping
{
    @Id
    private Integer id;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "note")
    private String note;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

}
