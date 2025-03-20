package com.wgzhao.addax.admin.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "table_column")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TableColumn
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "collect_id", nullable = false)
    private Long collectId;

    @Column(name = "column_name", nullable = false)
    private String columnName;

    @Column(name = "column_type", nullable = false)
    private String columnType;

    @Column(name = "column_precision", nullable = false)
    private Integer columnPrecision;

    @Column(name = "column_scale", nullable = false)
    private Integer columnScale;

    @Column(name = "column_position", nullable = false)
    private Integer columnPosition;

    @Column(name = "is_primary_key", nullable = false)
    private Boolean isPrimaryKey = false;

    @Column(name = "is_nullable", nullable = false)
    private Boolean isNullable = true;

    @Column(name = "column_comment")
    private String columnComment;

    @Column(name = "target_column_name")
    private String targetColumnName;

    @Column(name = "target_column_type")
    private String targetColumnType;

}