package com.wgzhao.addax.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "schema_change_risk")
@Data
public class SchemaChangeRisk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    private CollectTask_ task;

    @Column(name = "source_table", nullable = false, length = 200)
    private String sourceTable;

    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType;

    @Column(name = "column_name", nullable = false, length = 100)
    private String columnName;

    @Column(name = "old_data_type", length = 100)
    private String oldDataType;

    @Column(name = "new_data_type", length = 100)
    private String newDataType;

    @Column(name = "is_processed", nullable = false)
    private Boolean processed = false;

    @Column(name = "detect_time", nullable = false)
    private LocalDateTime detectTime;

    @Column(name = "process_time")
    private LocalDateTime processTime;

    private String remarks;
}