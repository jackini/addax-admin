package com.wgzhao.addax.admin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "source_template")
@Data
public class SourceTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(name = "fetch_size", nullable = false)
    private Integer fetchSize = 10000;
    
    @Column(name = "is_auto_pk", nullable = false)
    private Boolean autoPk = false;
}