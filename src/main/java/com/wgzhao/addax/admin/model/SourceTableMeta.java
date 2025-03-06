package com.wgzhao.addax.admin.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "source_table_meta")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class SourceTableMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "table_name", nullable = false)
    private String tableName;

    @Column(name = "schema_name")
    private String schemaName;

    @Column(name = "column_name", nullable = false)
    private String columnName;

    @Column(name = "column_type", nullable = false)
    private String columnType;

    @Column(name = "column_position", nullable = false)
    private Integer columnPosition;

    @Column(name = "is_primary_key", nullable = false)
    private Boolean isPrimaryKey = false;

    @Column(name = "is_nullable", nullable = false)
    private Boolean isNullable = true;

    @Column(name = "column_comment")
    private String columnComment;

    @Column(name = "last_check_time", nullable = false)
    private LocalDateTime lastCheckTime;

}