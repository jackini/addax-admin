package com.wgzhao.addax.admin.model;

import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_config", uniqueConstraints = @UniqueConstraint(columnNames = {"config_group", "config_key"}))
@Data
public class SystemConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_group", nullable = false, length = 50)
    private String configGroup;

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    @Column(length = 500)
    private String description;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "create_by", length = 100)
    private String createBy;

    @Column(name = "update_by", length = 100)
    private String updateBy;
}