package com.wgzhao.addax.admin.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "database_source")
@Data
public class DataSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "db_type", nullable = false, length = 50)
    private String dbType;
    
    @Column(name = "connection_url", nullable = false, length = 500)
    private String connectionUrl;
    
    @Column(length = 100)
    private String username;
    
    private String pass;
    
    @Column(name = "driver_class", length = 200)
    private String driverClass;
    
    @Lob
    private String properties;
    
    private String note;
    
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
    
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    @Column(name = "is_enable", nullable = false)
    private Boolean enable;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
}